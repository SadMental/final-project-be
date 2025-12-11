package com.kh.maproot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder

public class ReviewUnitLink {
	
	 private int linkNo;          // PK
	    private int reviewNo;        // 리뷰
	    private int scheduleUnitNo;  // 일정 세부 구간

}
