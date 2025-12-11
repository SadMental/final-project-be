package com.kh.maproot.aop;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.maproot.configuration.JwtProperties;
import com.kh.maproot.dto.AccountDto;
import com.kh.maproot.service.TokenService;
import com.kh.maproot.vo.TokenVO;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 모든 주소에 적용할 인터셉터
@Service
public class TokenRenewalInterceptor implements HandlerInterceptor{
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private JwtProperties jwtProperties;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// [1] OPTIONS 요청인 경우
		if(request.getMethod().equalsIgnoreCase("options")) {
			return true;
		}
		// [2] Authorization 헤더가 없는 경우(비회원)
		String bearerToken = request.getHeader("Authorization");
		if(bearerToken == null) {
			return true;
		}
		
		// [3] 액세스 토큰의 남은 시간이 충분한 경우 (10분 이상)
		try {
			long ms = tokenService.getRemain(bearerToken);
			// 10분 이상 남았다면
			if(ms >= jwtProperties.getRenewalLimit() * 60L * 1000L) {
				return true;
			}
			// 토큰의 남은 시간이 촉박하면 재발급
			TokenVO tokenVO = tokenService.parse(bearerToken);
			String newAccessToken = tokenService.generateAccessToken(
				AccountDto.builder()
					.accountId(tokenVO.getLoginId())
					.accountLevel(tokenVO.getLoginLevel())
				.build()
			);
			response.setHeader("Access-Control-Expose-Headers", "Access-Token");//노출시킬 헤더명을 기재
			response.setHeader("Access-Token", newAccessToken);//최근방식(x- 없이 의미 명확하게)
			
			return true;
		}
		catch (ExpiredJwtException e) {
			response.setStatus(401);
			response.setContentType("application/json; charset=UTF-8");
			Map<String, String> body = new HashMap<>();
			body.put("status", "401");
			body.put("message", "TOKEN_EXPIRED");
			ObjectMapper mapper = new ObjectMapper();//JSON 수동 생성기
			String json = mapper.writeValueAsString(body);//JSON 생성
			response.getWriter().write(json);//내보내도록 처리
			
			return false;//진행중인 요청 차단
		}
	}
}
