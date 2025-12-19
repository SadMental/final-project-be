package com.kh.maproot.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class GuestTokenVO {
	
	private String accessToken;
	private Number guestNo;
	private String loginLevel;

}
