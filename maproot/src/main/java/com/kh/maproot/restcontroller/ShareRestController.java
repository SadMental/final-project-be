package com.kh.maproot.restcontroller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.maproot.dao.GuestDao;
import com.kh.maproot.dao.ShareLinkDao;
import com.kh.maproot.dto.GuestDto;
import com.kh.maproot.dto.ShareLinkDto;
import com.kh.maproot.schedule.vo.VerifyRequestVO;
import com.kh.maproot.service.ShareAuthService;
import com.kh.maproot.service.TokenService;
import com.kh.maproot.vo.AuthRequestVO;
import com.kh.maproot.vo.CreateGuestTokenRequestVO;
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
	
	//공유키 검사
	@PostMapping("/verify")
	public int token(@RequestBody VerifyRequestVO verifyRequestVO) {
		System.out.println("shareKey =" + verifyRequestVO);
		
		//charekey로 해당되는 일정 검색
		ShareLinkDto shareLinkDto = shareLinkDao.select(verifyRequestVO.getShareKey());
		System.out.println("shareLinkDto =" + shareLinkDto);
		return shareLinkDto.getTargetScheduleNo();
		
	}
	
	//비회원 최초 접속
	@PostMapping("/enter")
	public EnterGuestResponseVO enter(
			@RequestBody EnterGuestRequestVO enterGuestRequestVO,
			HttpServletRequest request) {
		System.out.println("접속 실행");
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
		
		EnterGuestResponseVO enterGuestResponseVO = EnterGuestResponseVO.builder()
				.guestKey(guestKey)
				.build();
		
		return enterGuestResponseVO;
		
	}
	//(최초 접속) 게스트 생성하고 -> 토큰 발행
	
	//토큰 발행
	@PostMapping("/token")
	public ResponseEntity<GuestTokenVO> createGuestToken(
			@RequestBody CreateGuestTokenRequestVO createGuestTokenRequestVO,
			HttpServletRequest request) {
		System.out.println("토큰 발행 실행");
		System.out.println("guestDto" + createGuestTokenRequestVO);
		//회원 조회
		GuestDto findDto = guestDao.selectByKey(createGuestTokenRequestVO.getGuestKey());
		
		if(findDto == null) {
			return ResponseEntity.badRequest().build();
		}
				
		//회원 정보 업데이트
		findDto.setGuestMtime(LocalDateTime.now()); //마지막 활동 시간
		findDto.setGuestLastIp(shareAuthService.getClientIp(request)); //마지막 ip
		guestDao.update(findDto);
		
		//토큰 발행
		String accessToken = tokenService.generateGuestAccessToken(findDto);
		
		GuestTokenVO guestTokenVO = GuestTokenVO.builder()
				.accessToken(accessToken)
				.guestNo(findDto.getGuestNo())
				.loginLevel("비회원")
				.build();
		
		return ResponseEntity.ok(guestTokenVO);
		
	}
	
	@PostMapping("/auth")
	public ResponseEntity<GuestTokenVO> auth(@RequestBody AuthRequestVO authRequestVO) {
		
		try {
			//토큰 검사
			GuestTokenVO guestTokenVO = tokenService.guestParse(authRequestVO.getAccessToken());
			return ResponseEntity.ok(guestTokenVO);			
			
		} catch (Exception e) {
			return ResponseEntity.status(401).build(); //인증 실패
		}
		
	}
}
