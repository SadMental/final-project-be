package com.kh.maproot.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduleRouteDto {
	private Long scheduleRouteNo;
	private Long scheduleNo;
	private Integer scheduleUnitDay;
	private String scheduleRouteKey;
	private Long scheduleRouteStart;
	private Long scheduleRouteEnd;
	private Integer scheduleRouteTime;
	private Integer scheduleRouteDistance;
	private String scheduleRoutePriority;
	private String scheduleRouteType;
	private Object scheduleRouteGeom;
	
	@JsonIgnore
	private String ordinateString; // Geom에 들어가기위한 문자열
	@JsonIgnore
	private String tempStartKey; // db에 start_no를 저장하기위해 사용하려는 임시 변수
	@JsonIgnore
	private String tempEndKey; // db에 end_no를 저장하기위해 사용하려는 임시 변수
}
