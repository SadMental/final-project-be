package com.kh.maproot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduleUnitDto {
	private Long scheduleUnitNo;
	private Long scheduleNo;
	private String scheduleKey; // uuid값
	private String scheduleUnitContent;
	private Integer scheduleUnitTime;
	private Double scheduleUnitLat;
	private Double scheduleUnitLng;
	private Integer scheduleUnitPosition;
	private String scheduleUnitName;
	private Integer scheduleUnitDay;
}
