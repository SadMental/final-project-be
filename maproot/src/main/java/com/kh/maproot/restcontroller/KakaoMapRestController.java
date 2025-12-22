package com.kh.maproot.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.maproot.dto.ScheduleDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDataWrapperDto;
import com.kh.maproot.dto.kakaomap.KakaoMapSearchAddressRequestDto;
import com.kh.maproot.dto.kakaomap.KakaoMapSearchDocument;
import com.kh.maproot.dto.tmap.TmapResponseDto;
import com.kh.maproot.schedule.vo.ScheduleInsertDataWrapperVO;
import com.kh.maproot.service.MapService;
import com.kh.maproot.vo.TokenVO;
import com.kh.maproot.vo.kakaomap.KakaoMapGeocoderRequestVO;
import com.kh.maproot.vo.kakaomap.KakaoMapGeocoderResponseVO;
import com.kh.maproot.vo.kakaomap.KakaoMapLocationVO;
import com.kh.maproot.vo.kakaomap.KakaoMapMultyRequestVO;
import com.kh.maproot.vo.kakaomap.KakaoMapRequestVO;
import com.kh.maproot.vo.kakaomap.KakaoMapResponseVO;
import com.kh.maproot.vo.tmap.TmapRequestVO;
import com.kh.maproot.vo.tmap.TmapResponseVO;

import lombok.extern.slf4j.Slf4j;

@CrossOrigin @Slf4j
@RestController @RequestMapping("/kakaoMap")
public class KakaoMapRestController {
	
	@Autowired
	private MapService mapService;
	
	@PostMapping("/search")
	public KakaoMapResponseVO search(@RequestBody List<KakaoMapLocationVO> location, @RequestParam String priority) {
//		List<LocationVO>에는 반드시 2개의 데이터만 들어있다. 
		log.debug("location = {}", location);
		String origin = location.removeFirst().getLngLat();
		String destination =location.removeFirst().getLngLat();
		
		KakaoMapRequestVO requestVO = KakaoMapRequestVO.builder()
					.origin(origin)
					.destination(destination)
//					.waypoints(null)
					.priority(priority)
//					.avoid(null)
					.roadevent(2)
					.alternatives(true)
//					.roadDetails(null)
//					.carType(null)
//					.carFuel(null)
//					.carHipass(null)
					.summary(false)
				.build();
		
		return mapService.direction(requestVO);
	}
	
	@PostMapping("/searchAll")
	public KakaoMapResponseVO searchAll(@RequestBody List<KakaoMapLocationVO> location, @RequestParam String priority) {
		// **1. 시작점 (Origin): no가 1인 요소를 찾아서 제거하고 가져옵니다.**
	    // 일반적으로 리스트의 순서상 첫 번째는 no가 1일 경우가 많으므로 1로 가정합니다.
	    Optional<KakaoMapLocationVO> originOpt = location.stream()
	        .filter(loc -> loc.getNo() != null && loc.getNo() == 1) // no가 1인 요소 필터링
	        .findFirst();

	    // **2. 도착점 (Destination): no가 리스트 크기와 일치하는 요소를 찾아서 제거하고 가져옵니다.**
	    // no가 0이나 리스트 크기-1이 아니라, 실제 경로 순서의 마지막 번호(리스트 크기)일 가능성이 높습니다.
	    // 여기서는 간단히 리스트의 크기를 마지막 번호로 가정합니다. (혹은 가장 큰 no를 찾을 수도 있습니다.)
	    int lastNo = location.size(); 
	    Optional<KakaoMapLocationVO> destinationOpt = location.stream()
	        .filter(loc -> loc.getNo() != null && loc.getNo() == lastNo) // no가 리스트 크기와 일치하는 요소 필터링
	        .findFirst();
	    
	    // 3. Optional의 존재 여부를 확인하고 해당 요소를 새로운 리스트로 만듭니다.
	    KakaoMapLocationVO origin = originOpt.orElseThrow(() -> new IllegalArgumentException("Origin location (no=1) not found."));
	    KakaoMapLocationVO destination = destinationOpt.orElseThrow(() -> new IllegalArgumentException("Destination location (no=" + lastNo + ") not found."));

	    // 4. 찾은 요소들을 리스트에서 제거합니다.	
	    location.remove(origin);
	    location.remove(destination);
		
		KakaoMapMultyRequestVO requestVO = KakaoMapMultyRequestVO.builder()
					.origin(origin)
					.destination(destination)
					.waypoints(location)
					.roadevent(2)
					.alternatives(true)
					.summary(false)
					.priority(priority)
//					.avoid(null)
//					.roadDetails(null)
//					.carType(null)
					.carFuel("GASOLINE")
//					.carHipass(null)
				.build();
		
		return mapService.directionMulty(requestVO);
	}
	
	@PostMapping("/searchForWalk")
	public TmapResponseVO searchForWalk(@RequestBody List<KakaoMapLocationVO> location, @RequestParam(required = false) String priority) {
		log.debug("location = {}", location);
		
		return mapService.walk(location, priority);
	}
	
	@PostMapping("/getAddress")
	public KakaoMapGeocoderResponseVO getAddress(@RequestBody KakaoMapLocationVO location) {
		log.debug("location = {}", location);
		KakaoMapGeocoderRequestVO requestVO = KakaoMapGeocoderRequestVO.builder()
					.x(String.valueOf(location.getX()))
					.y(String.valueOf(location.getY()))
					.inputCoord("WGS84") // 지원 좌표계: WGS84, WCONGNAMUL, CONGNAMUL, WTM, TM (기본값: WGS84)
				.build();
		return mapService.getAddress(requestVO);
	}
	
	@PostMapping("/searchAddress")
	public List<KakaoMapSearchDocument> searchAddress(@RequestBody KakaoMapSearchAddressRequestDto searchData) {
		return mapService.getMarkerData(searchData);
	}
	
	@PostMapping("/insertData")
	public ScheduleDto insertData(
			@RequestBody ScheduleInsertDataWrapperVO warpper,
			@RequestAttribute TokenVO tokenVO) {
		
		return mapService.insert(warpper, tokenVO);
	}
	
}
