package com.kh.maproot.service;

import java.util.ArrayList;
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
		// 1. 입력 데이터 추출
	    Map<String, KakaoMapDaysDto> daysMap = datas.getDays();
	    Map<String, KakaoMapLocationVO> markerMap = datas.getMarkerData();
	    
	    // DB 저장을 위한 최종 DTO 리스트 정의
	    List<ScheduleUnitDto> unitEntities = new ArrayList<>();
	    List<ScheduleRouteDto> routeEntities = new ArrayList<>();

	    // 임시 스케줄 번호 (DB INSERT 시 필요)
	    // 실제로는 Service Layer에서 Sequence 등으로 발급받아야 합니다.
	    Long tempScheduleNo = 56L; 

	    // ==========================================
	    // A. 마커 데이터 처리 (ScheduleUnitDto 변환)
	    // ==========================================
	    // 마커 데이터는 Day 정보에 관계 없이 전체 마커 맵에서 추출
	    for (String markerId : markerMap.keySet()) {
	        KakaoMapLocationVO vo = markerMap.get(markerId);
	        
	        
	        ScheduleUnitDto unitDto = ScheduleUnitDto.builder()
	            .scheduleNo(tempScheduleNo) 
	            .scheduleKey(markerId) // 마커의 UUID를 scheduleKey로 저장 (논의된 클라이언트 UUID)
	            .scheduleUnitContent(vo.getContent())
	            .scheduleUnitTime(0) // 마커에 머무는 시간 (입력 데이터에 없으면 0 또는 null)
	            .scheduleUnitLat(vo.getY()) // 위도
	            .scheduleUnitLng(vo.getX()) // 경도
	            .scheduleUnitName(vo.getName())
	            // .scheduleUnitPosition은 Day 정보 루프에서 업데이트 필요
	            .scheduleUnitPosition(0)
	            // .scheduleUnitDay는 Day 정보 루프에서 업데이트 필요
	            .scheduleUnitDay(0)
	            .build();
	        
	        unitEntities.add(unitDto);
	    }
	    
	    log.debug(">>> [DB DTO] 생성된 Unit 엔티티 개수: {}", unitEntities.size());

	    // ==========================================
	    // B. 경로 및 순서 데이터 처리 (ScheduleRouteDto 변환)
	    // ==========================================
	    for(String dayNumStr : daysMap.keySet()) {
	        KakaoMapDaysDto day = daysMap.get(dayNumStr);
	        
	        // 1. 마커 순서 (Position) 업데이트 (ScheduleUnitDto에 일자 및 순서 매핑)
	

	        // 2. 경로 데이터 (Routes) 변환
	        List<KakaoMapRoutesDto> routes = day.getRoutes();
	        for(KakaoMapRoutesDto route : routes) {
	            
	            // String.valueOf(route.getLinepath()) 대신 Utility 함수 사용
	            String ordinateString = GeometryUtils.toOrdinateString(route.getLinepath());
	            
	            ScheduleRouteDto routeDto = ScheduleRouteDto.builder()
	                .scheduleNo(tempScheduleNo)
	                .scheduleRouteKey(route.getRouteKey()) // UUID A-B
	                .scheduleRouteStart(1) // 임시 값. 실제로는 UUID로 Unit No를 조회해야 함.
	                .scheduleRouteEnd(2)   // 임시 값. 실제로는 UUID로 Unit No를 조회해야 함.
	                .scheduleRouteTime(route.getDuration())
	                .scheduleRouteDistance(route.getDistance())
	                .ordinateString(ordinateString) // SDO_GEOMETRY용 문자열
	                .scheduleRoutePriority(route.getPriority())
	                .build();
	            
	            routeEntities.add(routeDto);
	        }
	    }

	    log.debug(">>> [DB DTO] 생성된 Route 엔티티 개수: {}", routeEntities.size());
	    log.debug(">>> [SAMPLE] 첫 번째 Route Ordinate String: {}", routeEntities.get(0).getOrdinateString());
	    
	    // ==========================================
	    // 3. 실제 DB 저장
	    // ==========================================
	    for(ScheduleUnitDto unitDto : unitEntities) {
	    	scheduleUnitDao.insert(unitDto);
	    	
	    }
	    for(ScheduleRouteDto routeDto : routeEntities) {
	    	scheduleRouteDao.insert(routeDto);
	    	
	    }
	}
}
