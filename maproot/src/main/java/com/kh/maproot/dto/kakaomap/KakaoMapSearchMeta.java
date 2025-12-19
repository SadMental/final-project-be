package com.kh.maproot.dto.kakaomap;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class KakaoMapSearchMeta {
	Integer totalCount;
	Integer pageableCount;
	boolean isEnd;
	KakaoMapSearchSameNameDto sameName;
}
