package com.kh.maproot.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.maproot.dao.GiftcardDao;
import com.kh.maproot.dao.KakaoPayDao;
import com.kh.maproot.dao.PaymentDao;
import com.kh.maproot.dao.PaymentDetailDao;
import com.kh.maproot.dto.GiftcardDto;
import com.kh.maproot.dto.KakaoPayDto;
import com.kh.maproot.dto.PaymentDetailDto;
import com.kh.maproot.dto.PaymentDto;
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
	private GiftcardDao giftcardDao;
	@Autowired
	private KakaoPayDao kakaoPayDao;

	@Transactional
	public void insert(KakaoPayApproveResponseVO responseVO,
			KakaoPayFlashVO flashVO) {
		long paymentNo = paymentDao.sequence();
		paymentDao.insert(PaymentDto.builder() .paymentNo(paymentNo)
				 .paymentOwner(responseVO.getPartnerUserId()).paymentTid(responseVO.getTid())
				 .paymentName(responseVO.getItemName())
				 .paymentTotal(responseVO.getAmount().getTotal())
				 .paymentRemain(responseVO.getAmount().getTotal()).build());
		 
		for (KakaoPayQtyVO qtyVO : flashVO.getQtyList()) 
		{ 
			long paymentDetailNo =
			paymentDetailDao.sequence(); GiftcardDto giftcardDto = giftcardDao.selectOne(qtyVO.getNo());
			paymentDetailDao.insert(PaymentDetailDto.builder()
			.paymentDetailNo(paymentDetailNo) 
			.paymentDetailOrigin(paymentNo)
			.paymentDetailItemNo(qtyVO.getNo())
			.paymentDetailItemName(giftcardDto.getGiftcardName())
			.paymentDetailItemPrice(giftcardDto.getGiftcardPrice())
			.paymentDetailQty(qtyVO.getQty()) .build()); 
		}
		
		long gap = 0L;
		List<PaymentDetailDto> purchaseList = paymentDetailDao.selectList(paymentNo);
		for (PaymentDetailDto paymentDetailDto : purchaseList) 
		{
			GiftcardDto giftcardDto = giftcardDao.selectOne(paymentDetailDto.getPaymentDetailItemNo());
			gap += (giftcardDto.getGiftcardPoint() * paymentDetailDto.getPaymentDetailQty());
		}
		
		long origin = kakaoPayDao.origin();
		long result = origin + gap;
		
		kakaoPayDao.update(KakaoPayDto.builder()
				.kakaopayValue(result)
				.kakaopayOwner("nodvic")
				.build());
	}

	@Transactional
	public void cancel(long paymentNo) {
		
		long gap = 0L;
		List<PaymentDetailDto> purchaseList = paymentDetailDao.selectList(paymentNo);
		for (PaymentDetailDto paymentDetailDto : purchaseList) 
		{
			if (!paymentDetailDto.getPaymentDetailStatus().equals("취소")) 
			{				
				GiftcardDto giftcardDto = giftcardDao.selectOne(paymentDetailDto.getPaymentDetailItemNo());
				gap += (giftcardDto.getGiftcardPoint() * paymentDetailDto.getPaymentDetailQty());
			}
		}
		
		paymentDao.cancelAll(paymentNo);
		paymentDetailDao.cancelAll(paymentNo);
		// 251209 이윤석.
		// 위 Dao 실행시 결제 상세 내역이 모두 '취소'가 되어 환불이 0원 된다
		// 부분 취소한 애들부터 검사하고 전체를 '취소'로 바꾼다
		
		long origin = kakaoPayDao.origin();
		long result = origin - gap;
		
		kakaoPayDao.update(KakaoPayDto.builder()
				.kakaopayValue(result)
				.kakaopayOwner("nodvic")
				.build());
	}

	@Transactional
	public void cancelUnit(PaymentDetailDto paymentDetailDto, KakaoPayCancelResponseVO responseVO) {
		paymentDao.cancelUnit(paymentDetailDto.getPaymentDetailOrigin(),
				responseVO.getCancelAvailableAmount().getTotal());
		paymentDetailDao.cancelUnit(paymentDetailDto);
		
		long gap = 0L;
		GiftcardDto giftcardDto = giftcardDao.selectOne(paymentDetailDto.getPaymentDetailItemNo());
		gap += (giftcardDto.getGiftcardPoint() * paymentDetailDto.getPaymentDetailQty());
			
		long origin = kakaoPayDao.origin();
		long result = origin - gap;
		
		kakaoPayDao.update(KakaoPayDto.builder()
				.kakaopayValue(result)
				.kakaopayOwner("nodvic")
				.build());
	}

}
