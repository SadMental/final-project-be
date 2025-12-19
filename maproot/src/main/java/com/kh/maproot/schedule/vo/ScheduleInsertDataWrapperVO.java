package com.kh.maproot.schedule.vo;

import com.kh.maproot.dto.ScheduleDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDataDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduleInsertDataWrapperVO {
	private KakaoMapDataDto data;
	private ScheduleDto scheduleDto;
}
