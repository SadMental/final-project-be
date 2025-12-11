package com.kh.maproot.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder

public class ReviewDto {

	    private int reviewNo;                    // 리뷰 PK
	    private int reviewScheduleNo;            // 어떤 일정(review_schedule_no)
	    private String reviewWriterType;         // USER / GUEST
	    private Integer reviewWriterAccountId;   // 회원일 경우 (nullable)
	    private String reviewWriterNickname;     // 화면 표시 닉네임
	    private String reviewContent;            // 리뷰 본문
	    private Timestamp reviewWriteTime;       // 작성 시간
	    private Timestamp reviewUpdateTime;      // 수정 시간
}
