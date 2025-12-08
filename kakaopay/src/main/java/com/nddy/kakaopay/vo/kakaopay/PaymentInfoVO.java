package com.nddy.kakaopay.vo.kakaopay;

import java.util.List;

import com.nddy.kakaopay.dto.PaymentDetailDto;
import com.nddy.kakaopay.dto.PaymentDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class PaymentInfoVO {
	private PaymentDto paymentDto;
	private List<PaymentDetailDto> paymentDetailList;
	private KakaoPayOrderResponseVO responseVO;
}
