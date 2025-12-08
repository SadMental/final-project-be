package com.nddy.kakaopay.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class PaymentDto {
	private Long paymentNo;
	private String paymentOwner;
	private String paymentTid;
	private String paymentName;
	private Integer paymentTotal;
	private Integer paymentRemain;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private LocalDateTime paymentTime;
}
