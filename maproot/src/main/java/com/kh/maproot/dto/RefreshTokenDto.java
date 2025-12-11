package com.kh.maproot.dto;

import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshTokenDto {
	
	private Long refreshTokenNo;
	private String refreshTokenTarget;
	private String refreshTokenValue;
	private LocalDateTime refreshTokenTime;

}
