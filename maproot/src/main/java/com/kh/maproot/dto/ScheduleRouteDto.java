package com.kh.maproot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduleRouteDto {
	private Long scheduleRouteNo;
	private Long scheduleNo;
	private String scheduleRouteKey;
	private Integer scheduleRouteStart;
	private Integer scheduleRouteEnd;
	private Integer scheduleRouteTime;
	private Integer scheduleRouteDistance;
	private String ordinateString; // Geom에 들어가기위한 문자열
	private String scheduleRoutePriority;
}
