package com.nddy.kakaopay.vo.kakaopay;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class KakaoPayFlashVO {
	private String partnerOrderId;
	private String partnerUserId;
	private String tid;
	private String returnUrl;
	private List<KakaoPayQtyVO> qtyList;
}
