package com.kh.maproot.vo.kakaomap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class KakaoMapCoordinateVO {
	private double lng; // 경도
	private double lat; // 위도
}
