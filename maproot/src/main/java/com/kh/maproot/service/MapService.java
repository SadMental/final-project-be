package com.kh.maproot.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.kh.maproot.dao.ScheduleDao;
import com.kh.maproot.dao.ScheduleRouteDao;
import com.kh.maproot.dao.ScheduleUnitDao;
import com.kh.maproot.dto.ScheduleDto;
import com.kh.maproot.dto.ScheduleRouteDto;
import com.kh.maproot.dto.ScheduleUnitDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDataDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDataWrapperDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDaysDto;
import com.kh.maproot.dto.kakaomap.KakaoMapRoutesDto;
import com.kh.maproot.dto.kakaomap.KakaoMapSearchAddressRequestDto;
import com.kh.maproot.dto.kakaomap.KakaoMapSearchDocument;
import com.kh.maproot.dto.kakaomap.KakaoMapSearchMeta;
import com.kh.maproot.dto.kakaomap.KakaoMapSearchResponseDto;
import com.kh.maproot.dto.tmap.TmapFeatureDto;
import com.kh.maproot.dto.tmap.TmapGeometryDto;
import com.kh.maproot.dto.tmap.TmapResponseDto;
import com.kh.maproot.error.UnauthorizationException;
import com.kh.maproot.schedule.vo.ScheduleInsertDataWrapperVO;
import com.kh.maproot.utils.GeometryUtils;
import com.kh.maproot.vo.TokenVO;
import com.kh.maproot.vo.kakaomap.KakaoMapGeocoderRequestVO;
import com.kh.maproot.vo.kakaomap.KakaoMapGeocoderResponseVO;
import com.kh.maproot.vo.kakaomap.KakaoMapLocationVO;
import com.kh.maproot.vo.kakaomap.KakaoMapMultyRequestVO;
import com.kh.maproot.vo.kakaomap.KakaoMapRequestVO;
import com.kh.maproot.vo.kakaomap.KakaoMapResponseVO;
import com.kh.maproot.vo.tmap.TmapCoordinateVO;
import com.kh.maproot.vo.tmap.TmapRequestVO;
import com.kh.maproot.vo.tmap.TmapResponseVO;

import lombok.extern.slf4j.Slf4j;

@Service @Slf4j
public class MapService {

	@Autowired @Qualifier("kakaomapWebClient")
	private WebClient mapClient;
	
	@Autowired @Qualifier("kakaomapLocal")
	private WebClient localClient;
	
	@Autowired @Qualifier("TmapWebClient")
	private WebClient tmapClient;
	
	@Autowired
	private ScheduleUnitDao scheduleUnitDao;
	
	@Autowired
	private ScheduleRouteDao scheduleRouteDao;
	
	@Autowired
	private ScheduleDao scheduleDao;	
	
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
		KakaoMapGeocoderResponseVO response = localClient.get()
				.uri(uriBuilder -> uriBuilder
				        .path("/geo/coord2address") // 🚨 baseUrl 이후의 경로만 지정
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
	
    public List<KakaoMapSearchDocument> getMarkerData(KakaoMapSearchAddressRequestDto requestVO) {
        String query = (String) requestVO.getQuery();
        // 전체 결과를 담을 리스트 (document List)
        List<KakaoMapSearchDocument> accumulatedDocuments = new ArrayList<>();

        // 1페이지부터 재귀 호출 시작
        // (query, page, 누적 리스트)를 전달
        return roopSearch(query, 1, accumulatedDocuments);
    }

    private List<KakaoMapSearchDocument> roopSearch(String query, int currentPage, List<KakaoMapSearchDocument> accumulatedDocuments) {
        // API 호출 (currentPage를 사용)
    	KakaoMapSearchResponseDto response = localClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/keyword")
                        .queryParam("query", query)
                        .queryParam("page", currentPage) // 현재 페이지 번호를 사용
                        .build()
                )
                .retrieve()
                .bodyToMono(KakaoMapSearchResponseDto.class)
                .block();

        // 응답 데이터 파싱
        KakaoMapSearchMeta meta = response.getMeta();
        List<KakaoMapSearchDocument> documents = response.getDocuments();
        boolean isEnd = (Boolean) meta.isEnd();

        // 1. 현재 페이지의 documents를 누적 리스트에 추가
        if (documents != null) {
            accumulatedDocuments.addAll(documents);
        }

        // 2. 종료 조건 확인
        // - isEnd가 true이거나 (마지막 페이지)
        // - 페이지가 45를 초과하면 (카카오맵 최대 페이지 제한)
        if (isEnd || currentPage >= 45) {
            log.info("검색 종료. 총 {}개 데이터 누적.", accumulatedDocuments.size());
            return accumulatedDocuments; // 최종 결과 반환
        } else {
            // 3. 다음 페이지를 요청하며 재귀 호출
            return roopSearch(query, currentPage + 1, accumulatedDocuments);
        }
    }
	
	@Transactional
	public ScheduleDto insert(ScheduleInsertDataWrapperVO wrapper, TokenVO tokenVO) {
		ScheduleDto scheduleDto = wrapper.getScheduleDto();
		Long scheduleNo = scheduleDto.getScheduleNo(); 
		ScheduleDto findDto = scheduleDao.selectByScheduleNo(scheduleNo);
		if(!findDto.getScheduleOwner().equals(tokenVO.getLoginId())) throw new UnauthorizationException();
		
		scheduleRouteDao.deleteByScheduleNo(scheduleNo);
		scheduleUnitDao.deleteByScheduleNo(scheduleNo);
	    
		KakaoMapDataDto data = wrapper.getData();

		Map<String, KakaoMapDaysDto> daysMap = data.getDays();
	    Map<String, KakaoMapLocationVO> markerMap = data.getMarkerData();
	    
	    List<ScheduleUnitDto> unitEntities = new ArrayList<>();
	    List<ScheduleRouteDto> routeEntities = new ArrayList<>();
	    
	    
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
	                        .scheduleNo(scheduleNo) 
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
	                .scheduleNo(scheduleNo)
	                .scheduleUnitDay(scheduleDay)
	                .scheduleRouteKey(route.getRouteKey())
	                .scheduleRouteTime(route.getDuration())
	                .scheduleRouteDistance(route.getDistance())
	                .ordinateString(ordinateString)
	                .scheduleRoutePriority(route.getPriority())
	                .scheduleRouteType(route.getType())
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
	    
	    // 경로 데이터 저장
	    for(ScheduleRouteDto routeDto : routeEntities) {
	    	Long startUnitNo = keyMaps.get(routeDto.getTempStartKey());
	        Long endUnitNo = keyMaps.get(routeDto.getTempEndKey());
	        
	        routeDto.setScheduleRouteStart(startUnitNo);
	        routeDto.setScheduleRouteEnd(endUnitNo);
	    	
	    	scheduleRouteDao.insert(routeDto);
	    }
	    
	    return scheduleDao.updateUnit(scheduleDto);
	}
	
	public TmapResponseVO walk(List<KakaoMapLocationVO> location, String priority) {
		
		KakaoMapLocationVO start = location.get(0);
	    KakaoMapLocationVO end = location.get(location.size() - 1);
	    String passList = null;
	    if (location.size() > 2 && location.size() <= 7) { // 최대 7지점 (Start 1 + Pass 5 + End 1)
	        // 경유지는 1번 인덱스부터 끝에서 두 번째 인덱스까지입니다.
	        passList = location.subList(1, location.size() - 1).stream()
	            .map(marker -> String.format("%.6f,%.6f", marker.getX(), marker.getY()))
	            .collect(Collectors.joining("_"));
	    }
	    
		Map<String, Integer> convertPriority = new HashMap<>();
		convertPriority.put("RECOMMEND", 0);
		convertPriority.put("TIME", 10);
		convertPriority.put("DISTANCE", 30);
		
		TmapRequestVO requestVO = TmapRequestVO.builder()
				.startX(start.getX())
				.startY(start.getY())
				.startName(start.getName())
				.endX(end.getX())
				.endY(end.getY())
				.endName(end.getName())
//					.endPolid(null)
				.passList(passList)
				.searchOption(convertPriority.get(priority != null ? priority : "RECOMMEND"))
//					.reqCoordType(null)
//					.resCoordType(null)
//					.sort(null)
				.build();
		
		TmapResponseDto response = tmapClient.post()
				.uri(uriBuilder -> uriBuilder
						.path("/routes/pedestrian")
						.queryParam("version", 1) 
						.build())
				.bodyValue(requestVO) 
				.retrieve()
				.bodyToMono(TmapResponseDto.class)
				.block();
	
		TmapResponseVO responseVO = TmapResponseVO.builder()
					.priority(priority)
					.distance(new ArrayList<>())
					.totalDistance(0)
					.duration(new ArrayList<>())
					.totalDuration(0)
					.linepath(new ArrayList<>())
					.type("WALK")
				.build();
		
		// 현재 처리 중인 구간의 누적 거리/시간
	    int currentSegmentDistance = 0;
	    int currentSegmentTime = 0;
	    
	    List<TmapCoordinateVO> currentSegmentPath = new ArrayList<>();

	    for(TmapFeatureDto feature : response.getFeatures()) {
	        // --- LineString: 전체 및 현재 구간 거리/시간/좌표 누적 ---
	        if(feature.getGeometry().getType().equalsIgnoreCase("LineString")) {
	            
	        	// 전체 누적
	            int featureDistance = feature.getProperties().getDistance();
	            int featureTime = feature.getProperties().getTime();
	            responseVO.setTotalDistance(responseVO.getTotalDistance() + featureDistance);
	            responseVO.setTotalDuration(responseVO.getTotalDuration() + featureTime);
	            
	            // 현재 구간 누적
	            currentSegmentDistance += featureDistance;
	            currentSegmentTime += featureTime;

	            // LinePath 좌표 추출 및 누적 
	            TmapGeometryDto geometry = feature.getGeometry();
	            List<Object> rawCoordinates = geometry.getCoordinates();
	            List<List<Double>> lineCoordinates = new ArrayList<>();
	            
	            for(Object outerItem : rawCoordinates) {
	                if(outerItem instanceof List) {
	                    @SuppressWarnings("unchecked")
	                    List<Double> coordPair = (List<Double>) outerItem;
	                    lineCoordinates.add(coordPair);
	                }
	            }
	            for(List<Double> coordPair : lineCoordinates) {
	                if(coordPair.size() == 2) {
	                    // 현재 구간 경로에 좌표 추가
	                    currentSegmentPath.add(TmapCoordinateVO.builder().lng(coordPair.get(0)).lat(coordPair.get(1)).build());                       
	                }
	            }

	        // --- Point: 구간 종료 지점(경유지/도착지) 확인 및 저장 ---
	        } else if (feature.getGeometry().getType().equalsIgnoreCase("Point")) {
	            
	        	String pointType = feature.getProperties().getPointType();
	            
	            // 🚩 2. Point Type이 경유지(PP, PP1~PP5) 또는 도착지(EP)인지 확인
	            // SP(출발지)와 GP(일반 안내점)는 무시합니다.
	            if (pointType.startsWith("PP") || pointType.equalsIgnoreCase("EP")) {
	                
	                // 3. 구간 완료: 누적된 거리와 시간을 리스트에 저장
	                responseVO.getDistance().add(currentSegmentDistance);
	                responseVO.getDuration().add(currentSegmentTime);
	                
	                // Note: LineString이 하나도 없는데 PP/EP가 나오는 예외 상황 방지를 위해 비어있지 않은지 확인하는 것이 좋습니다.
	                if (!currentSegmentPath.isEmpty()) {
	                    responseVO.getLinepath().add(currentSegmentPath);
	                }
	                
	                // 4. 다음 구간을 위해 누적 변수를 리셋
	                currentSegmentDistance = 0;
	                currentSegmentTime = 0;
	                
	                currentSegmentPath = new ArrayList<>();
	            }
	        }
	    }
	    log.debug("responseVO = {}", responseVO);
		
		return responseVO;
	}
}
