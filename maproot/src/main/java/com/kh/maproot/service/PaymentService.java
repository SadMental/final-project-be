package com.kh.maproot.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.maproot.dao.AccountDao;
import com.kh.maproot.dao.PaymentDao;
import com.kh.maproot.dao.PaymentDetailDao;
import com.kh.maproot.dao.ShopDao;
import com.kh.maproot.dto.AccountDto;
import com.kh.maproot.dto.PaymentDetailDto;
import com.kh.maproot.dto.PaymentDto;
import com.kh.maproot.dto.ShopDto;
import com.kh.maproot.error.TargetNotfoundException;
import com.kh.maproot.vo.kakaopay.KakaoPayApproveResponseVO;
import com.kh.maproot.vo.kakaopay.KakaoPayCancelResponseVO;
import com.kh.maproot.vo.kakaopay.KakaoPayFlashVO;
import com.kh.maproot.vo.kakaopay.KakaoPayQtyVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentService {
	@Autowired
	private PaymentDao paymentDao;
	@Autowired
	private PaymentDetailDao paymentDetailDao;
	@Autowired
	private ShopDao shopDao;
	@Autowired
	private AccountDao accountDao;

	@Transactional
	public void insert(KakaoPayApproveResponseVO responseVO,
			KakaoPayFlashVO flashVO) {
		long paymentNo = paymentDao.sequence();
		paymentDao.insert(PaymentDto.builder() .paymentNo(paymentNo)
				 .paymentOwner(responseVO.getPartnerUserId())
				 .paymentTid(responseVO.getTid())
				 .paymentName(responseVO.getItemName())
				 .paymentTotal(responseVO.getAmount().getTotal())
				 .paymentRemain(responseVO.getAmount().getTotal()).build());
		 
		for (KakaoPayQtyVO qtyVO : flashVO.getQtyList()) 
		{ 
			long paymentDetailNo =
			paymentDetailDao.sequence(); 
			ShopDto shopDto = shopDao.selectOne(qtyVO.getNo());
			paymentDetailDao.insert(PaymentDetailDto.builder()
			.paymentDetailNo(paymentDetailNo) 
			.paymentDetailOrigin(paymentNo)
			.paymentDetailItemNo(qtyVO.getNo())
			.paymentDetailItemName(shopDto.getShopName())
			.paymentDetailItemPrice(shopDto.getShopPrice())
			.paymentDetailQty(qtyVO.getQty()) .build()); 
		}
		
		int gap = 0;
		List<PaymentDetailDto> purchaseList = paymentDetailDao.selectList(paymentNo);
		for (PaymentDetailDto paymentDetailDto : purchaseList)
			gap += getGap(paymentDetailDto);
		
		updateMaxSchedule(responseVO.getPartnerUserId(), gap);
	}

	@Transactional
	public void cancel(long paymentNo) {
		
		int gap = 0;
		List<PaymentDetailDto> purchaseList = paymentDetailDao.selectList(paymentNo);
		for (PaymentDetailDto paymentDetailDto : purchaseList)
			gap += getGap(paymentDetailDto);
		
		paymentDao.cancelAll(paymentNo);
		paymentDetailDao.cancelAll(paymentNo);
		// 251209 이윤석.
		// 위 Dao 실행시 결제 상세 내역이 모두 '취소'가 되어 환불이 0원 된다
		// 부분 취소한 애들부터 검사하고 전체를 '취소'로 바꾼다
		
		PaymentDto paymentDto = paymentDao.whoCancelAll(paymentNo);
		if (paymentDto == null)
			throw new TargetNotfoundException("존재하지 않는 거래");
		
		updateMaxSchedule(paymentDto.getPaymentOwner(), -gap);
	}

	@Transactional
	public void cancelUnit(PaymentDetailDto paymentDetailDto, KakaoPayCancelResponseVO responseVO) {
		paymentDao.cancelUnit(paymentDetailDto.getPaymentDetailOrigin(),
				responseVO.getCancelAvailableAmount().getTotal());
		paymentDetailDao.cancelUnit(paymentDetailDto);
		
		int gap = getGap(paymentDetailDto);			
		updateMaxSchedule(responseVO.getPartnerUserId(), -gap);
	}
	
	public void updateMaxSchedule(String accountId, int addMaxSchedule) 
	{
		AccountDto accountDto = accountDao.originMaxSchedule(accountId);
		if (accountDto == null)
			throw new TargetNotfoundException("존재하지 않는 회원");
		int result = accountDto.getAccountMaxSchedule() + addMaxSchedule;
		accountDto.setAccountId(accountId);
		accountDto.setAccountMaxSchedule(result);
		accountDao.updateMaxSchedule(accountDto);
	}
	
	public int getGap(PaymentDetailDto paymentDetailDto) 
	{
		if (!paymentDetailDto.getPaymentDetailStatus().equals("취소")) 
		{				
			ShopDto shopDto = shopDao.selectOne(paymentDetailDto.getPaymentDetailItemNo());
			return (shopDto.getShopValue() * paymentDetailDto.getPaymentDetailQty());
		}
		return 0;
	}
}
