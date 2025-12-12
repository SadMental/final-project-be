package com.kh.maproot.dto.kakaomap;

import java.util.Map;

import com.kh.maproot.vo.kakaomap.KakaoMapLocationVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class KakaoMapDataDto {
	private Map<String, KakaoMapDaysDto> days;
	private Map<String, KakaoMapLocationVO> markerData;
}
