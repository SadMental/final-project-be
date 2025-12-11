package com.kh.maproot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class KakaoPayDto {
	private Long kakaopayValue;
	private String kakaopayOwner;
}