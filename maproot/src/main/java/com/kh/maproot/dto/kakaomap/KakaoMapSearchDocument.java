package com.kh.maproot.dto.kakaomap;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class KakaoMapSearchDocument {
	String id;
	String placeName;
	String categoryName;
	String categoryGroupCode;
	String categoryGroupName;
	String phone;
	String addressName;
	String roadAddressName;
	String x;
	String y;
	String placeUrl;
	String distance;
}
