package com.kh.maproot.service;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Struct;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.maproot.dao.ScheduleDao;
import com.kh.maproot.dao.ScheduleMemberDao;
import com.kh.maproot.dao.ScheduleRouteDao;
import com.kh.maproot.dao.ScheduleTagDao;
import com.kh.maproot.dao.ScheduleUnitDao;
import com.kh.maproot.dto.ScheduleDto;
import com.kh.maproot.dto.ScheduleMemberDto;
import com.kh.maproot.dto.ScheduleRouteDto;
import com.kh.maproot.dto.ScheduleTagDto;
import com.kh.maproot.dto.ScheduleUnitDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDataDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDaysDto;
import com.kh.maproot.dto.kakaomap.KakaoMapRoutesDto;
import com.kh.maproot.schedule.vo.ScheduleCreateRequestVO;
import com.kh.maproot.schedule.vo.ScheduleInsertDataWrapperVO;
import com.kh.maproot.schedule.vo.ScheduleListResponseVO;
import com.kh.maproot.vo.kakaomap.KakaoMapCoordinateVO;
import com.kh.maproot.vo.kakaomap.KakaoMapLocationVO;

import lombok.extern.slf4j.Slf4j;


@Service @Slf4j
public class ScheduleService {
	
	@Autowired
	private ScheduleDao scheduleDao;
	@Autowired
	private ScheduleTagDao scheduleTagDao;
	@Autowired
	private ScheduleMemberDao scheduleMemberDao;
	@Autowired
	private ScheduleUnitDao scheduleUnitDao;
	@Autowired
	private ScheduleRouteDao scheduleRouteDao;
	
	@Transactional
	public ScheduleDto insert(ScheduleCreateRequestVO scheduleVO) {
		//일정 등록
				ScheduleDto scheduleDto = ScheduleDto.builder()
								.scheduleName(scheduleVO.getScheduleName())
								.scheduleOwner(scheduleVO.getScheduleOwner())
								.scheduleWtime(Timestamp.valueOf(LocalDateTime.now()))
								.scheduleStartDate(scheduleVO.getScheduleStartDate())
								.scheduleEndDate(scheduleVO.getScheduleEndDate())
								.build();
				
				Long sequence = scheduleDao.insert(scheduleDto);
				
				//태그 등록
				for(String tagName : scheduleVO.getTagNoList()) {
					ScheduleTagDto scheduleTagDto = ScheduleTagDto.builder()
							.scheduleNo(sequence)
							.tagName(tagName)
							.build();			
					
					scheduleTagDao.insert(scheduleTagDto);
				}
				
				//맴버 테이블에도 추가
				
				ScheduleMemberDto scheduleMemberDto = ScheduleMemberDto.builder()
						.scheduleNo(sequence)
						.accountId("testuser1")
						.scheduleMemberNickname("테스트유저1")
						.scheduleMemberRole("member")
						.scheduleMemberNotify("Y")
					.build();
				
				scheduleMemberDao.insert(scheduleMemberDto);
				
				return scheduleDto;
	}
	
	@Transactional
	public ScheduleInsertDataWrapperVO loadScheduleData(ScheduleDto scheduleDto) throws Exception {
		List<ScheduleUnitDto> unitList = scheduleUnitDao.selectList(scheduleDto);
		List<ScheduleRouteDto> routeList = scheduleRouteDao.selectList(scheduleDto);
		
		Map<String, KakaoMapLocationVO> markerMap = unitList.stream()
				.collect(Collectors.toMap(ScheduleUnitDto::getScheduleKey, unit -> {
					return KakaoMapLocationVO.builder()
								.no(unit.getScheduleUnitPosition())
								.x(unit.getScheduleUnitLng())
								.y(unit.getScheduleUnitLat())
								.name(unit.getScheduleUnitName())
								.content(unit.getScheduleUnitContent())
							.build();
				}));
		log.debug("markerMap = {}", markerMap);
		
		Map<String, KakaoMapDaysDto> daysMap = new HashMap<>();
		
		for(ScheduleUnitDto unit : unitList) {
			String dayKey = String.valueOf(unit.getScheduleUnitDay());
			daysMap.computeIfAbsent(dayKey, k -> KakaoMapDaysDto.builder()
						.markerIds(new ArrayList<>())
						.routes(new ArrayList<>())
					.build());
			daysMap.get(dayKey).getMarkerIds().add(unit.getScheduleKey());
		}
//		log.debug("daysMap = {}", daysMap);
		
		for (ScheduleRouteDto route : routeList) {
	        String dayKey = String.valueOf(route.getScheduleUnitDay());
	        
	        if (daysMap.containsKey(dayKey)) {
	            // 리액트에서 사용하던 경로 데이터 구조로 변환
	        	KakaoMapRoutesDto routeDto = KakaoMapRoutesDto.builder()
	                .routeKey(route.getScheduleRouteKey())
	                .priority(route.getScheduleRoutePriority()) // RECOMMEND, TIME, DISTANCE
	                .type(route.getScheduleRouteType())         // CAR, WALK
	                .distance(route.getScheduleRouteDistance())
	                .duration(route.getScheduleRouteTime())
	                .linepath(convertGeomToList(route.getScheduleRouteGeom())) // 이미 파싱된 JSON 또는 문자열
	                .build();
	            
	        	daysMap.get(dayKey).getRoutes().add(routeDto);
	        }
	    }
		KakaoMapDataDto data = KakaoMapDataDto.builder()
					            .days(daysMap)
					            .markerData(markerMap)
					            .build();
//		log.debug("markerMap = {}", markerMap);
//		log.debug("response = {}", response);
		ScheduleDto selectedSchedule = scheduleDao.selectByScheduleNo(scheduleDto);
		
		ScheduleInsertDataWrapperVO response = ScheduleInsertDataWrapperVO.builder()
					.data(data)
					.scheduleDto(selectedSchedule)
				.build();
		
		return response;
	}
	
	public List<KakaoMapCoordinateVO> convertGeomToList(Object geomObj) throws Exception {
	    List<KakaoMapCoordinateVO> path = new ArrayList<>();
	    
	    if (geomObj instanceof java.sql.Struct) {
	        Struct struct = (Struct) geomObj;
	        
	        // SDO_GEOMETRY 구조: [GTYPE, SRID, POINT, ELEM_INFO, ORDINATES]
	        // 5번째 속성인 ORDINATES(index 4)가 좌표 배열입니다.
	        Object[] attributes = struct.getAttributes();
	        Array ordinatesArray = (Array) attributes[4]; 
	        
	        if (ordinatesArray != null) {
	            // 소수점 유실 방지를 위해 BigDecimal 또는 double로 캐스팅
	            Object[] ordinates = (Object[]) ordinatesArray.getArray();
	            
	            for (int i = 0; i < ordinates.length; i += 2) {
	                double x = ((BigDecimal) ordinates[i]).doubleValue();   // 경도(Lng)
	                double y = ((BigDecimal) ordinates[i+1]).doubleValue(); // 위도(Lat)
	                
	                path.add(new KakaoMapCoordinateVO(x, y));
	            }
	        }
	    }
	    return path;
	}
	
	@Transactional
	public List<ScheduleListResponseVO> loadScheduleList(String accountId) {
		List<ScheduleMemberDto> list = scheduleMemberDao.selectByAccountId(accountId);
		
		//일정 내용 찾기 (찾은 dto의 pk로 검색)

		List<ScheduleListResponseVO> voList = new ArrayList<>(); 
		
		for(ScheduleMemberDto scheduleMemberDto : list) {
			
			Long scheduleNo = scheduleMemberDto.getScheduleNo();
			
			ScheduleDto findScheduleDto = scheduleDao.selectByScheduleNo(scheduleNo);
			ScheduleUnitDto unitFirst = scheduleUnitDao.selectFirstUnit(scheduleNo);
			Integer unitCount = scheduleUnitDao.selectUnitCount(scheduleNo);
			Integer memberCount = scheduleMemberDao.selectMemberCount(scheduleNo);
			
			ScheduleListResponseVO scheduleListResponseVO = ScheduleListResponseVO.builder()
					.scheduleNo(scheduleMemberDto.getScheduleNo())
					.scheduleName(findScheduleDto.getScheduleName())
					.scheduleState(findScheduleDto.getScheduleState())
					.schedulePublic(findScheduleDto.getSchedulePublic())
					.scheduleOwner(findScheduleDto.getScheduleOwner())
					.scheduleStartDate(findScheduleDto.getScheduleStartDate())
					.scheduleEndDate(findScheduleDto.getScheduleEndDate())
					.unitFirst(unitFirst)
					.unitCount(unitCount)
					.memberCount(memberCount)
					.scheduleImage("공란")
					.build();
			
			voList.add(scheduleListResponseVO);
			
		}
		
		//검색된 일정들 VO로 전송
		
		return voList;
	}

}
