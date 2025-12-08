package com.nddy.kakaopay.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TokenVO {
	private String loginId;
	private String loginLevel;
}
