package com.nddy.kakaopay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GiftcardDto {
	private Long giftcardNo;
	private String giftcardName;
	private String giftcardContent;
	private Integer giftcardPrice;
	private Integer giftcardPoint;
}


