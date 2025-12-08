package com.nddy.kakaopay.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nddy.kakaopay.dao.GiftcardDao;
import com.nddy.kakaopay.dao.KakaoPayDao;
import com.nddy.kakaopay.dao.PaymentDao;
import com.nddy.kakaopay.dao.PaymentDetailDao;
import com.nddy.kakaopay.dto.GiftcardDto;
import com.nddy.kakaopay.dto.KakaoPayDto;
import com.nddy.kakaopay.dto.PaymentDetailDto;
import com.nddy.kakaopay.dto.PaymentDto;
import com.nddy.kakaopay.vo.kakaopay.KakaoPayApproveResponseVO;
import com.nddy.kakaopay.vo.kakaopay.KakaoPayCancelResponseVO;
import com.nddy.kakaopay.vo.kakaopay.KakaoPayFlashVO;
import com.nddy.kakaopay.vo.kakaopay.KakaoPayQtyVO;

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
		paymentDao.cancelAll(paymentNo);
		paymentDetailDao.cancelAll(paymentNo);
		
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
