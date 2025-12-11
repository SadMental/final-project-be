package com.kh.maproot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @AllArgsConstructor @NoArgsConstructor

public class TagDto {
	
	private int tagNo;
	private String tagName;
	private String tagCategory;
	
}
