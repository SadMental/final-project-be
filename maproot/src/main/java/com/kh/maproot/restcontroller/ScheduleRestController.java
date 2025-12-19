package com.kh.maproot.restcontroller;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kh.maproot.aop.AccountInterceptor;
import com.kh.maproot.dao.ScheduleDao;
import com.kh.maproot.dao.ScheduleMemberDao;
import com.kh.maproot.dao.ScheduleRouteDao;
import com.kh.maproot.dao.ScheduleTagDao;
import com.kh.maproot.dao.ScheduleUnitDao;
import com.kh.maproot.dao.TagDao;
import com.kh.maproot.dto.ScheduleDto;
import com.kh.maproot.dto.ScheduleMemberDto;
import com.kh.maproot.dto.ScheduleRouteDto;
import com.kh.maproot.dto.ScheduleTagDto;
import com.kh.maproot.dto.ScheduleUnitDto;
import com.kh.maproot.dto.TagDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDataDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDaysDto;
import com.kh.maproot.dto.kakaomap.KakaoMapRoutesDto;
import com.kh.maproot.schedule.vo.ScheduleCreateRequestVO;
import com.kh.maproot.schedule.vo.ScheduleInsertDataWrapperVO;
import com.kh.maproot.schedule.vo.ScheduleListResponseVO;
import com.kh.maproot.service.EmailService;
import com.kh.maproot.service.ScheduleService;
import com.kh.maproot.vo.kakaomap.KakaoMapCoordinateVO;
import com.kh.maproot.vo.kakaomap.KakaoMapLocationVO;

import lombok.extern.slf4j.Slf4j;

@CrossOrigin
@RestController @Slf4j
@RequestMapping("/schedule")
public class ScheduleRestController {

	
	@Autowired
	private TagDao tagDao;
	@Autowired
	private ScheduleMemberDao scheduleMemberDao;
	@Autowired
	private ScheduleService scheduleService;

	
	@GetMapping("/tagList")
	public List<TagDto> tagList() {
		return tagDao.selectAll();
	}
	
	@PostMapping("/insert")
	public ScheduleDto insert(@RequestBody ScheduleCreateRequestVO scheduleVO) {
		
		return scheduleService.insert(scheduleVO);
	}
	
	@GetMapping("/list/{accountId}")
	public List<ScheduleListResponseVO> list(
			@PathVariable String accountId
			) {
		System.out.println("데이터확인"+accountId);
		//회원에 따른 일정 찾기 (스케쥴 맴버 테이블에서 검색)
		return scheduleService.loadScheduleList(accountId);
	}
	
	@GetMapping("/memberList/{scheduleNo}")
	public List<ScheduleMemberDto> selectMemberList(@PathVariable Long scheduleNo) {
		return scheduleMemberDao.selectByScheduleNo(scheduleNo);
	}
	
	@PostMapping("/detail")
	public ScheduleInsertDataWrapperVO detail(@RequestBody ScheduleDto scheduleDto) throws Exception{
		
	    return scheduleService.loadScheduleData(scheduleDto);
	}
}
