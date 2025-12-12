package com.kh.maproot.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.kh.maproot.dao.ScheduleRouteDao;
import com.kh.maproot.dao.ScheduleUnitDao;
import com.kh.maproot.dto.ScheduleRouteDto;
import com.kh.maproot.dto.ScheduleUnitDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDataDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDaysDto;
import com.kh.maproot.dto.kakaomap.KakaoMapRoutesDto;
import com.kh.maproot.utils.GeometryUtils;
import com.kh.maproot.vo.kakaomap.KakaoMapGeocoderRequestVO;
import com.kh.maproot.vo.kakaomap.KakaoMapGeocoderResponseVO;
import com.kh.maproot.vo.kakaomap.KakaoMapLocationVO;
import com.kh.maproot.vo.kakaomap.KakaoMapMultyRequestVO;
import com.kh.maproot.vo.kakaomap.KakaoMapRequestVO;
import com.kh.maproot.vo.kakaomap.KakaoMapResponseVO;

import lombok.extern.slf4j.Slf4j;

@Service @Slf4j
public class KakaoMapService {
	@Autowired @Qualifier("kakaomapWebClient")
	private WebClient mapClient;
	
	@Autowired @Qualifier("kakaomapGeocoder")
	private WebClient geoClient;
	
	@Autowired
	private ScheduleUnitDao scheduleUnitDao;
	
	@Autowired
	private ScheduleRouteDao scheduleRouteDao;
	
	
	
	public KakaoMapResponseVO direction(KakaoMapRequestVO requestVO) {
		KakaoMapResponseVO response = mapClient.get() 
				.uri(uriBuilder -> uriBuilder
				        .path("/v1/directions") // baseUrl 이후의 경로만 지정
				        .queryParam("origin", requestVO.getOrigin()) // 쿼리 파라미터로 데이터 전달
				        .queryParam("destination", requestVO.getDestination())
				        .queryParam("summary", requestVO.getSummary())
				        .queryParam("alternatives", requestVO.getAlternatives())
				        .queryParam("priority", requestVO.getPriority())
				        .queryParam("roadevent", requestVO.getRoadevent())
				        .build()
				    )
			.retrieve() // 응답을 수신하겠다
				.onStatus(HttpStatusCode::isError, clientResponse ->
					clientResponse.bodyToMono(String.class).map(body -> {
						log.error("Error body = {}", body);
						return new RuntimeException("Status: " + clientResponse.statusCode() + ", body: " + body);
					})
				) // 오류 체크용
				.bodyToMono(KakaoMapResponseVO.class)
				.block(); // 동기적으로 변환하여 응답이 올때까지 기다려라. (RestTemplate과 같아짐)
		
		return response;
	}
	public KakaoMapResponseVO directionMulty(KakaoMapMultyRequestVO requestVO) {
		KakaoMapResponseVO response = mapClient.post() 
				.uri("/v1/waypoints/directions")
				.bodyValue(requestVO)
				.retrieve() // 응답을 수신하겠다
				.onStatus(HttpStatusCode::isError, clientResponse ->
				clientResponse.bodyToMono(String.class).map(body -> {
					log.error("Error body = {}", body);
					return new RuntimeException("Status: " + clientResponse.statusCode() + ", body: " + body);
				})
						) // 오류 체크용
				.bodyToMono(KakaoMapResponseVO.class)
				.block(); // 동기적으로 변환하여 응답이 올때까지 기다려라. (RestTemplate과 같아짐)
		
		return response;
	}
	public KakaoMapGeocoderResponseVO getAddress(KakaoMapGeocoderRequestVO requestVO) {
		KakaoMapGeocoderResponseVO response = geoClient.get()
				.uri(uriBuilder -> uriBuilder
				        .path("/coord2address") // 🚨 baseUrl 이후의 경로만 지정
				        .queryParam("x", requestVO.getX()) // 🚨 쿼리 파라미터로 데이터 전달
				        .queryParam("y", requestVO.getY())
				        .queryParam("input_coord", requestVO.getInputCoord())
				        .build()
				    )
			.retrieve() // 응답을 수신하겠다
				.bodyToMono(KakaoMapGeocoderResponseVO.class) // 데이터는 한번에 오고(Mono) 형태는 Map이다 (연속적으로 오면 Flux)
				.block(); // 동기적으로 변환하여 응답이 올때까지 기다려라. (RestTemplate과 같아짐)
		
		return response;
		
	}
	@Transactional
	public void insert(KakaoMapDataDto datas) {
		Map<String, KakaoMapDaysDto> daysMap = datas.getDays();
	    Map<String, KakaoMapLocationVO> markerMap = datas.getMarkerData();
	    
	    List<ScheduleUnitDto> unitEntities = new ArrayList<>();
	    List<ScheduleRouteDto> routeEntities = new ArrayList<>();
	    
	    Long tempScheduleNo = 56L; 
	    
	    // ==========================================
	    // A. 일자별 순회하며 마커(Unit)와 경로(Route) 동시 처리
	    // ==========================================
	    for(String dayNumStr : daysMap.keySet()) {
	        KakaoMapDaysDto day = daysMap.get(dayNumStr);
	        Integer scheduleDay = Integer.parseInt(dayNumStr); // 일자 (1, 2, 3...)
	        
	        // 1. 마커 순서 처리 (ScheduleUnitDto 변환)
	        List<String> markerOrderList = day.getMarkerIds(); // 일자별 방문 순서대로의 마커 ID 리스트 (가정)
	        
	        if(markerOrderList != null) {
	            for (String markerId : markerOrderList) {
	                // 해당 마커의 상세 정보 조회 (markerMap 활용)
	                KakaoMapLocationVO vo = markerMap.get(markerId); 
	                
	                if (vo != null) {
	                    ScheduleUnitDto unitDto = ScheduleUnitDto.builder()
	                        .scheduleNo(tempScheduleNo) 
	                        .scheduleKey(markerId) 
	                        .scheduleUnitContent(vo.getContent())
	                        // ... 기타 마커 상세 정보 (좌표, 이름 등)
	                        .scheduleUnitLat(vo.getY()) 
	                        .scheduleUnitLng(vo.getX()) 
	                        .scheduleUnitName(vo.getName())
	                        // **핵심: 일자 및 순서 매핑**
	                        .scheduleUnitTime(0) // 해당 세부 일정에서 소요되는 시간데이터는 아직 미정이기에 임시로 0을 입력해둠
	                        .scheduleUnitDay(scheduleDay)
	                        .scheduleUnitPosition(vo.getNo())
	                        .build();
	                    
	                    unitEntities.add(unitDto);
	                }
	            }
	        }
	        
	        // 2. 경로 데이터 처리 (ScheduleRouteDto 변환)
	        List<KakaoMapRoutesDto> routes = day.getRoutes();
	        for(KakaoMapRoutesDto route : routes) {
	            String ordinateString = GeometryUtils.toOrdinateString(route.getLinepath());
	            String[] tempKey = route.getRouteKey().split("##");
	            
	            ScheduleRouteDto routeDto = ScheduleRouteDto.builder()
	                .scheduleNo(tempScheduleNo)
	                .scheduleRouteKey(route.getRouteKey())
	                .scheduleRouteTime(route.getDuration())
	                .scheduleRouteDistance(route.getDistance())
	                .ordinateString(ordinateString)
	                .scheduleRoutePriority(route.getPriority())
	                .tempStartKey(tempKey[0])
	                .tempEndKey(tempKey[1])
	                .build();
	            
	            routeEntities.add(routeDto);
	        }
	    }
	    
	    // ==========================================
	    // B. 실제 DB 저장 (Unit 데이터 먼저 저장)
	    // ==========================================
	    
	    // 경로 데이터에 저장할 UnitNo를 위한 임시 Map
	    Map<String, Long> keyMaps = new HashMap<>();
	    
	    // 세부 일정 데이터 저장
	    for(ScheduleUnitDto unitDto : unitEntities) {
	    	scheduleUnitDao.insert(unitDto);
	    	keyMaps.put(unitDto.getScheduleKey(), unitDto.getScheduleUnitNo());
	    }
	    
	    log.debug("keyMaps = {}", keyMaps);
	    // 경로 데이터 저장
	    for(ScheduleRouteDto routeDto : routeEntities) {
	    	Long startUnitNo = keyMaps.get(routeDto.getTempStartKey());
	        Long endUnitNo = keyMaps.get(routeDto.getTempEndKey());
	        
	        routeDto.setScheduleRouteStart(startUnitNo);
	        routeDto.setScheduleRouteEnd(endUnitNo);
	    	
	    	scheduleRouteDao.insert(routeDto);
	    }
	}
}
