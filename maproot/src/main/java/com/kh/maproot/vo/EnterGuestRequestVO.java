package com.kh.maproot.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnterGuestRequestVO {
	
	private String guestNickname;

}
