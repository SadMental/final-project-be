package com.nddy.kakaopay.vo.kakaopay;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class KakaoPayReadyRequestVO {
	private String itemName;
	private int totalAmount;
	private String partnerOrderId;
	private String partnerUserId;
}
