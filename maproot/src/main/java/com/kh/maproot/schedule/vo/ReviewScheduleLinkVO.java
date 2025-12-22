package com.kh.maproot.schedule.vo;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data	@AllArgsConstructor @NoArgsConstructor @Builder
public class ReviewScheduleLinkVO {
	
	 	private int reviewNo;                    // 리뷰 PK
	    private int scheduleNo;            // 대표 일정(review_schedule_no)
	    private String reviewWriterType;         // USER / GUEST
	    private String accountId;   // 회원일 경우 (nullable)
	    private String reviewWriterNickname;     // 화면 표시 닉네임
	    private String reviewContent;            // 리뷰 본문
	    private Timestamp reviewWtime;       // 작성 시간
	    private Timestamp reviewEtime;      // 수정 시간
	    private Integer scheduleUnitNo;  // 일정 세부 구간
	    private Long attachmentNo; //회원 이미지
}
