package com.kh.maproot.utils;

import java.util.List;
import java.util.stream.Collectors;

import com.kh.maproot.vo.kakaomap.KakaoMapCoordinateVO;

public class GeometryUtils {
	public static String toOrdinateString(List<KakaoMapCoordinateVO> linepath) {
        if (linepath == null || linepath.isEmpty()) return "";
        return linepath.stream()
                .map(coord -> coord.getLng() + ", " + coord.getLat())
                .collect(Collectors.joining(", "));
    }
}
