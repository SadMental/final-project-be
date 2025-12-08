package com.nddy.kakaopay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentDetailDto {
	private Long paymentDetailNo;
	private Long paymentDetailOrigin;
	private Long paymentDetailItemNo;
	private String paymentDetailItemName;
	private Integer paymentDetailItemPrice;
	private Integer paymentDetailQty;
	private String paymentDetailStatus;
	
	public Integer getPaymentDetailTotal() {
		return paymentDetailItemPrice * paymentDetailQty;
	}
}




