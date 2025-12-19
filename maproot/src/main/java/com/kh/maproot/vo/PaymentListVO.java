package com.kh.maproot.vo;

import java.util.List;

import com.kh.maproot.dto.PaymentDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentListVO {
	private int page;//요청한 페이지 번호
	private int count;//전체 데이터 개수
	private int size;//요청한 데이터 개수
	private int begin, end;//현재 요청한 페이지에 대한 행 값
	private boolean last;//마지막인지 아닌지
	private List<PaymentDto> list;//조회한 결과
}