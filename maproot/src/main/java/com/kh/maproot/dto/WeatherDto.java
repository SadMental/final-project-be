package com.kh.maproot.dto;

import java.security.Timestamp;
import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder

public class WeatherDto {
	
	 private int weatherNo;          // NUMBER PK
	    private Date weatherDate;       // DATE (java.sql.Date)
	    private String weatherLocation; // 지역 정보
	    private String weatherType;     // '맑음', '비', '흐림'
	    private Double weatherTempMin;  // NUMBER(4,1)
	    private Double weatherTempMax;  // NUMBER(4,1)
	    private String weatherData;     // CLOB → String으로 받음 (JSON/API 원본)
	    private Timestamp weatherWtime; // 등록시간

}
