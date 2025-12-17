package com.kh.maproot.vo.tmap;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TmapResponseVO {
	String routeKey;
	Integer totalDistance;
	List<Integer> distance;
	Integer totalDuration;
	List<Integer> duration;
	String priority;
	List<List<TmapCoordinateVO>> linepath;
	String type;
}
