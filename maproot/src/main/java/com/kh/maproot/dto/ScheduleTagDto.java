package com.kh.maproot.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class ScheduleTagDto {

	private Long scheduleNo;
	private String tagName;
	
}
