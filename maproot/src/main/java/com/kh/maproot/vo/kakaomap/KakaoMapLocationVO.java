package com.kh.maproot.vo.kakaomap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class KakaoMapLocationVO {
	private Integer no;
	private Double x; // lng
	private Double y; // lat
	private String name;
	private Integer angle;
	private String content;
	
	public String getLngLat() {
		return x + "," + y;
	}
}
