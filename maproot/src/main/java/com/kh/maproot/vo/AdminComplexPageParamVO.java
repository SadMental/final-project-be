package com.kh.maproot.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminComplexPageParamVO {
	private Integer begin;
	private Integer end;
	private AccountComplexSearchVO accountComplexSearchVO;
}
