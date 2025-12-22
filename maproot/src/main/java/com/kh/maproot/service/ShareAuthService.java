package com.kh.maproot.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.kh.maproot.dao.GuestDao;
import com.kh.maproot.dto.GuestDto;
import com.kh.maproot.vo.GuestTokenVO;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ShareAuthService {

	@Autowired
	private TokenService tokenService;
	@Autowired
	private GuestDao guestDao;
	
	public String getClientIp(HttpServletRequest request) {
		System.out.println(request);
		return request.getRemoteAddr(); // 접속한 사람 IP
	}
	
	//토큰 발행
	public GuestTokenVO createGuestToken(
			@RequestBody String guestKey,
			HttpServletRequest request) {
		
		//회원 조회
		GuestDto findDto = guestDao.selectByKey(guestKey);

				
		//회원 정보 업데이트
		findDto.setGuestMtime(LocalDateTime.now()); //마지막 활동 시간
		findDto.setGuestLastIp(getClientIp(request)); //마지막 ip
		guestDao.update(findDto);
		
		//토큰 발행
		String accessToken = tokenService.generateGuestAccessToken(findDto);
		
		GuestTokenVO guestTokenVO = GuestTokenVO.builder()
				.accessToken(accessToken)
				.guestNo(findDto.getGuestNo())
				.loginLevel("비회원")
				.build();
		
		return guestTokenVO;
}
	

}
