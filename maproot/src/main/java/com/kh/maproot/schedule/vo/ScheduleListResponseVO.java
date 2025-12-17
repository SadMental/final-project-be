package com.kh.maproot.schedule.vo;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kh.maproot.dto.ScheduleDto;
import com.kh.maproot.dto.ScheduleMemberDto;
import com.kh.maproot.dto.ScheduleUnitDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder

public class ScheduleListResponseVO {
	
	 private Integer scheduleNo;
	  private String scheduleName;
	  private String scheduleState;   // open/close/progress
	  private String schedulePublic;  // Y/N
	  private String scheduleOwner; // REFERENCES account(account_id)
	  @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
	  private LocalDateTime scheduleStartDate;
	  @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
	  private LocalDateTime scheduleEndDate;
	  
	  private ScheduleUnitDto unitFirst; //제일 상단 세부일정
	  private Integer unitCount;     // 세부일정 개수
	  private Integer memberCount;   // 참여자 수
	  private String scheduleImage;   // 대표 이미지
    
}
