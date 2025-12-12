package com.kh.maproot.dto.kakaomap;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class KakaoMapDaysDto {
	private List<String> markerIds; 
    private List<KakaoMapRoutesDto> routes;
}
