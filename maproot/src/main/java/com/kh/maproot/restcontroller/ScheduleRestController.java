package com.kh.maproot.restcontroller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Struct;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.maproot.aop.AccountInterceptor;
import com.kh.maproot.dao.ScheduleDao;
import com.kh.maproot.dao.ScheduleMemberDao;
import com.kh.maproot.dao.ScheduleRouteDao;
import com.kh.maproot.dao.ScheduleTagDao;
import com.kh.maproot.dao.ScheduleUnitDao;
import com.kh.maproot.dao.ShareLinkDao;
import com.kh.maproot.dao.TagDao;
import com.kh.maproot.dto.ScheduleDto;
import com.kh.maproot.dto.ScheduleMemberDto;
import com.kh.maproot.dto.ScheduleRouteDto;
import com.kh.maproot.dto.ScheduleTagDto;
import com.kh.maproot.dto.ScheduleUnitDto;
import com.kh.maproot.dto.ShareLinkDto;
import com.kh.maproot.dto.TagDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDataDto;
import com.kh.maproot.dto.kakaomap.KakaoMapDaysDto;
import com.kh.maproot.dto.kakaomap.KakaoMapRoutesDto;
import com.kh.maproot.schedule.vo.ScheduleCreateRequestVO;
import com.kh.maproot.schedule.vo.ScheduleInsertDataWrapperVO;
import com.kh.maproot.schedule.vo.ScheduleListResponseVO;
import com.kh.maproot.schedule.vo.SchedulePublicUpdateRequestVO;
import com.kh.maproot.service.TokenService;
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
	private ScheduleUnitDao scheduleUnitDao;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private ShareLinkDao shareLinkDao;
	@Autowired
	private ScheduleService scheduleService;

	
	@GetMapping("/tagList")
	public List<TagDto> tagList() {
		return tagDao.selectAll();
	}
	
	@PostMapping("/insert")
	public ScheduleDto insert(
			@ModelAttribute ScheduleCreateRequestVO scheduleVO,
			@RequestParam(required = false) MultipartFile attach) throws IllegalStateException, IOException {
		
		return scheduleService.insert(scheduleVO, attach);
	}
	
	
	@GetMapping("/memberList/{scheduleNo}")
	public List<ScheduleMemberDto> selectMemberList(@PathVariable Long scheduleNo) {
		return scheduleMemberDao.selectByScheduleNo(scheduleNo);
	}
	
	//일정 공유
	@GetMapping("/share/{scheduleNo}")
	public String shareSchedule(@PathVariable int scheduleNo) {
//	랜덤한 shareKey 생성 (UUID, SecureRandom 등)
	String shareKey = UUID.randomUUID().toString();
	System.out.println("shareKey : "+shareKey);
//	만료 시간 계산 (예: +7일)
	LocalDateTime expireTime = LocalDateTime.now().plusDays(7);
	System.out.println("expireTime : " + expireTime);
		
//	share_link 테이블에 insert
	ShareLinkDto shareLinkDto = ShareLinkDto.builder()
			.shareKey(shareKey)
			.targetScheduleNo(scheduleNo)
			.expireTime(expireTime)
			.build();
			
	shareLinkDao.insert(shareLinkDto);
		
//	생성된 shareKey 반환
		return shareKey;
	}

	@PostMapping("/detail")
	public ScheduleInsertDataWrapperVO detail(@RequestBody ScheduleDto scheduleDto) throws Exception{
		
	    return scheduleService.loadScheduleData(scheduleDto);
	}
	
	// 전체 일정 목록(회원일때, 아닐때)
	@GetMapping("/")
	public List<ScheduleListResponseVO> listAll(){
		return scheduleService.loadScheduleList();
	}
	@GetMapping("/list/{accountId}")
	public List<ScheduleListResponseVO> list(
			@PathVariable String accountId
			) {
		return scheduleService.loadScheduleList(accountId);
	}
	
	//public 변경
	@PatchMapping("/public")
    public ResponseEntity<Void> updateSchedulePublic(
            @RequestBody SchedulePublicUpdateRequestVO vo
    ) {
		System.out.println("숫자확인"+vo);
        scheduleService.updateSchedulePublic(vo.getScheduleNo(), vo.isSchedulePublic());
        return ResponseEntity.noContent().build();
    }
}
