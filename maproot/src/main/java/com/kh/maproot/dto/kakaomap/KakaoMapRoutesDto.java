package com.kh.maproot.dto.kakaomap;

import java.util.List;

import com.kh.maproot.vo.kakaomap.KakaoMapCoordinateVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class KakaoMapRoutesDto {
	private String routeKey;
	private Integer distance;
	private Integer duration;
	private List<KakaoMapCoordinateVO> linepath;
}
