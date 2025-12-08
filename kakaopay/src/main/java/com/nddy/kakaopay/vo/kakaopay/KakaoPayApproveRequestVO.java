package com.nddy.kakaopay.vo.kakaopay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class KakaoPayApproveRequestVO {
	private String partnerOrderId;
	private String partnerUserId;
	private String tid;
	private String pgToken;
}
