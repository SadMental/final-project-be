package com.kh.maproot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ShopDto {
	private Long shopNo;
	private String shopName;
	private String shopDesc;
	private Integer shopPrice;
	private Integer shopValue;
}