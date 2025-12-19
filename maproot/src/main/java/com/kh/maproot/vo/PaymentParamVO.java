package com.kh.maproot.vo;

import java.util.List;

import com.kh.maproot.dto.PaymentDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentParamVO {
	private Integer begin;
	private Integer end;
	private String paymentOwner;
}
