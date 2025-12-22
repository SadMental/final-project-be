package com.kh.maproot.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Struct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.maproot.dao.AccountDao;
import com.kh.maproot.dao.ScheduleDao;
import com.kh.maproot.dao.ScheduleMemberDao;
import com.kh.maproot.dao.ScheduleRouteDao;
import com.kh.maproot.dao.ScheduleTagDao;
import com.kh.maproot.dao.ScheduleUnitDao;
import com.kh.maproot.dto.AccountDto;
import com.kh.maproot.dto.ScheduleDto;
import com.kh.maproot.dto.ScheduleMemberDto;
import com.kh.maproot.dto.ScheduleRouteDto;
import com.kh.maproot.dto.ScheduleTagDto;
import com.kh.maproot.dto.ScheduleUnitDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDataDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDaysDto;
import com.kh.maproot.dto.kakaomap.KakaoMapRoutesDto;
import com.kh.maproot.error.NeedPermissionException;
import com.kh.maproot.error.UnauthorizationException;
import com.kh.maproot.schedule.vo.ScheduleCreateRequestVO;
import com.kh.maproot.schedule.vo.ScheduleInsertDataWrapperVO;
import com.kh.maproot.schedule.vo.ScheduleListResponseVO;
import com.kh.maproot.schedule.vo.ScheduleStateResponseVO;
import com.kh.maproot.vo.TokenVO;
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
	@Autowired
	private AttachmentService attachmentService;
	@Autowired
	private AccountDao accountDao;
	
	@Transactional
	public ScheduleDto insert(ScheduleCreateRequestVO scheduleVO, MultipartFile attach) throws IllegalStateException, IOException {
		//일정 등록
				ScheduleDto scheduleDto = ScheduleDto.builder()
								.scheduleName(scheduleVO.getScheduleName())
								.scheduleOwner(scheduleVO.getScheduleOwner())
								.scheduleWtime(LocalDateTime.now())
								.scheduleStartDate(scheduleVO.getScheduleStartDate())
								.scheduleEndDate(scheduleVO.getScheduleEndDate())
								.schedulePublic(scheduleVO.getSchedulePublic())
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
				
				AccountDto accountDto = accountDao.selectOne(scheduleVO.getScheduleOwner());

				accountDto.setAttachmentNo(
						accountDto.getAttachmentNo() != null && accountDto.getAttachmentNo() > 0
					        ? accountDto.getAttachmentNo()
					        : null
					);
				
				ScheduleMemberDto scheduleMemberDto = ScheduleMemberDto.builder()
						.scheduleNo(sequence)
						.accountId(accountDto.getAccountId())
						.scheduleMemberNickname(accountDto.getAccountNickname())
						.scheduleMemberRole("owner")
						.scheduleMemberNotify("Y")
					.build();
				
				scheduleMemberDao.insert(scheduleMemberDto);
				
				// 프로필 이미지 추가(insert이후) 
				if(attach != null && attach.isEmpty() == false){
					Long attachmentNo = attachmentService.save(attach);
					scheduleDao.connect(sequence, attachmentNo);
				}
				
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
						.routes(new HashMap<>())
					.build());
			daysMap.get(dayKey).getMarkerIds().add(unit.getScheduleKey());
		}
//		log.debug("daysMap = {}", daysMap);
		
		for (ScheduleRouteDto route : routeList) {
	        String dayKey = String.valueOf(route.getScheduleUnitDay());
	        
	        if (daysMap.containsKey(dayKey)) {
	            KakaoMapDaysDto dayDto = daysMap.get(dayKey);
	            
	            // 중첩 맵 구조 확보 (Type -> Priority -> List)
	            String type = route.getScheduleRouteType();         // CAR, WALK
	            String priority = route.getScheduleRoutePriority(); // RECOMMEND, TIME, DISTANCE
	            
	            // Type 맵 확보
	            Map<String, List<KakaoMapRoutesDto>> typeMap = 
	                dayDto.getRoutes().computeIfAbsent(type, k -> new HashMap<>());
	            
	            // Priority 리스트 확보
	            List<KakaoMapRoutesDto> routeListForPriority = 
	                typeMap.computeIfAbsent(priority, k -> new ArrayList<>());

	            // DTO 생성 (내부 필드에서 type, priority가 제거되었다면 제외)
	            KakaoMapRoutesDto routeDto = KakaoMapRoutesDto.builder()
	                .routeKey(route.getScheduleRouteKey())
	                .distance(route.getScheduleRouteDistance())
	                .duration(route.getScheduleRouteTime())
	                .linepath(convertGeomToList(route.getScheduleRouteGeom()))
	                .build();
	            
	            // 최종 리스트에 추가
	            routeListForPriority.add(routeDto);
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
	
	// 1. 전체 공개 일정 (파라미터 없이 호출 -> null 전달)
	public List<ScheduleListResponseVO> loadScheduleList() {
	    return scheduleDao.selectScheduleList(null);
	}

	// 2. 특정 회원 일정 (ID 전달)
	public List<ScheduleListResponseVO> loadScheduleList(String accountId) {
	    return scheduleDao.selectScheduleList(accountId);
	}
	
	//약속전/진행중/종료
	public String scheduleState(LocalDateTime start, LocalDateTime end) {

	    if (start == null) {
	        throw new IllegalArgumentException("약속 시작시간은 필수입니다.");
	    }

	    LocalDateTime now = LocalDateTime.now();

	    // 종료시간 없으면 시작 + 1일
	    LocalDateTime resolvedEnd = (end != null) ? end : start.plusDays(1);

	    // 약속 전
	    if (now.isBefore(start)) {
	        return "약속전";
	    }

	    // 종료 (종료를 먼저 판정)
	    if (!now.isBefore(resolvedEnd)) {
	        return "종료";
	    }

	    // 진행 중
	    return "진행중";
	}
	
	@Transactional
	public void updateSchedulePublic(Long scheduleNo, boolean schedulePublic) {
	    String yn = schedulePublic ? "Y" : "N";
	    int updated = scheduleDao.updateSchedulePublic(scheduleNo, yn);
	}

	public ScheduleStateResponseVO refreshStateByNow(long scheduleNo) {

        ScheduleDto dto = scheduleDao.selectByScheduleNo(scheduleNo);
        System.out.println("dto=" + dto);
        if (dto == null) throw new IllegalArgumentException("일정이 존재하지 않습니다: " + scheduleNo);
        
        String next = scheduleState(dto.getScheduleStartDate(), dto.getScheduleEndDate());
        String prev = dto.getScheduleState();

        boolean changed = (prev == null) || !prev.equals(next);
        if (changed) {
            scheduleDao.updateScheduleState(scheduleNo, next);
        }
        prev = (prev == null) ? null : prev.trim();
        
        
        
        return ScheduleStateResponseVO.builder()
                .scheduleNo(scheduleNo)
                .scheduleState(next)
                .changed(changed)
                .build();
    }
	
	@Transactional
	public boolean delete(Long scheduleNo, TokenVO tokenVO) {
		ScheduleDto scheduleDto = scheduleDao.selectByScheduleNo(scheduleNo);
		if(!scheduleDto.getScheduleOwner().equals(tokenVO.getLoginId())) throw new NeedPermissionException();
		scheduleRouteDao.deleteByScheduleNo(scheduleNo);
		scheduleUnitDao.deleteByScheduleNo(scheduleNo);
		return scheduleDao.delete(scheduleNo);
	}
	 

}
