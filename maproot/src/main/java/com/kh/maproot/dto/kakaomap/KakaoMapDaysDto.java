package com.kh.maproot.dto.kakaomap;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class KakaoMapDaysDto {
	private List<String> markerIds; 
	// Map<이동수단(CAR/WALK), Map<우선순위(RECOMMEND 등), List<경로데이터>>>
	Map<String, Map<String, List<KakaoMapRoutesDto>>> routes;
}
