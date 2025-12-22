package com.kh.maproot.restcontroller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.kh.maproot.dao.AccountDao;
import com.kh.maproot.dao.GuestDao;
import com.kh.maproot.dao.ScheduleMemberDao;
import com.kh.maproot.dao.ShareLinkDao;
import com.kh.maproot.dto.AccountDto;
import com.kh.maproot.dto.GuestDto;
import com.kh.maproot.dto.ScheduleMemberDto;
import com.kh.maproot.dto.ShareLinkDto;
import com.kh.maproot.schedule.vo.InsertScheduleMemberVO;
import com.kh.maproot.schedule.vo.VerifyRequestVO;
import com.kh.maproot.service.ShareAuthService;
import com.kh.maproot.service.TokenService;
import com.kh.maproot.vo.AuthRequestVO;
import com.kh.maproot.vo.EnterGuestRequestVO;
import com.kh.maproot.vo.EnterGuestResponseVO;
import com.kh.maproot.vo.GuestTokenVO;
import com.kh.maproot.vo.ShareLinkResponseVO;
import com.kh.maproot.vo.TokenVO;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
@RequestMapping("/share")
public class ShareRestController {

	@Autowired
	private TokenService tokenService;
	@Autowired
	private ShareLinkDao shareLinkDao;
	@Autowired
	private GuestDao guestDao;
	@Autowired
	private ShareAuthService shareAuthService;
	@Autowired
	private ScheduleMemberDao scheduleMemberDao;
	@Autowired
	private AccountDao accountDao;
	
	//공유키 검사
	@PostMapping("/verify")
	public ResponseEntity<Integer> token(@RequestBody VerifyRequestVO verifyRequestVO) {

		if(verifyRequestVO == null || verifyRequestVO.getShareKey() == null || verifyRequestVO.getShareKey().isBlank() ) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "shareKey is required");
		}
		
		//charekey로 해당되는 일정 검색
		ShareLinkDto shareLinkDto = shareLinkDao.select(verifyRequestVO.getShareKey());
		
		if(shareLinkDto == null) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid shareKey");
		}
		
		return ResponseEntity.ok(shareLinkDto.getTargetScheduleNo());
		
	}
	
	//비회원 최초 접속
	@PostMapping("/enter")
	public EnterGuestResponseVO enter(
			@RequestBody EnterGuestRequestVO enterGuestRequestVO,
			HttpServletRequest request) {
		System.out.println("비회원 최초 접속 실행");
		System.out.println("enterGuestRequestVO : "+enterGuestRequestVO + ", request =" + request);
		//guestKey 발급
		String guestKey = UUID.randomUUID().toString();
		System.out.println("guestKey : "+guestKey);

		//IP 확인
		String ip = shareAuthService.getClientIp(request);
		System.out.println("ip : "+ip);

		//게스트 생성
		GuestDto guestDto = GuestDto.builder()
				.guestNo(guestDao.sequence())
				.guestNickname(enterGuestRequestVO.getGuestNickname())
				.guestKey(guestKey)
				.guestLastIp(ip)
				.guestWtime(LocalDateTime.now())
				.build();
		System.out.println("guestDto : "+guestDto);

		//닉네임 추가
		guestDao.insert(guestDto);
		
		//(최초 접속) 게스트 생성하고 -> 토큰 발행
		GuestTokenVO guestTokenVO = shareAuthService.createGuestToken(guestKey, request);
		
		EnterGuestResponseVO enterGuestResponseVO = EnterGuestResponseVO.builder()
				.guestKey(guestKey)
				.accessToken(guestTokenVO.getAccessToken())
				.guestNo(guestTokenVO.getGuestNo())
				.loginLevel("비회원")
				.guestNickname(guestDto.getGuestNickname())
				.build();
		
		System.out.println(" 토큰 발행 완료 ");
		return enterGuestResponseVO;
		
	}
	
	@PostMapping("/auth")
	public ResponseEntity<GuestTokenVO> auth(@RequestHeader("Authorization") String authorization) {
		
		try {
			//토큰 검사
			
			System.out.println(" 토큰 검사");
			
			GuestTokenVO guestTokenVO = tokenService.guestParse(authorization);
			return ResponseEntity.ok(guestTokenVO);			
			
		} catch (Exception e) {
			System.out.println(" 토큰 검사 실패");
			return ResponseEntity.status(401).build(); //인증 실패
			
		}
		
	}
	
	//중복 검사
	@PostMapping("/nickname/{nickname}")
	public boolean existNickname(@PathVariable String nickname) {
		System.out.println(nickname);
		return guestDao.selectByNickname(nickname);
	}
	
	//공유받은 링크로 들어온 회원 멤버리스트에 추가하기
	@PostMapping("/member/{scheduleNo}")
	public void insertScheduleMember(
			@PathVariable Long scheduleNo, @RequestBody InsertScheduleMemberVO insertScheduleMemberVO
			) {
		
		AccountDto findDto = accountDao.selectOne(insertScheduleMemberVO.getAccountId());
		
	    if (scheduleMemberDao.exists(scheduleNo, insertScheduleMemberVO.getAccountId())) {
	        return;
	    }
		
		ScheduleMemberDto scheduleMemberDto = ScheduleMemberDto.builder()
				.scheduleNo(scheduleNo)
				.accountId(findDto.getAccountId())
				.scheduleMemberNickname(findDto.getAccountNickname())
				.scheduleMemberRole("member")
				.scheduleMemberNotify("Y")
				.build();
		
		scheduleMemberDao.insert(scheduleMemberDto);
	}
}
