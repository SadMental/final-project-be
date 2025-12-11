package com.kh.maproot.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import com.kh.maproot.error.UnauthorizationException;
import com.kh.maproot.service.TokenService;
import com.kh.maproot.vo.TokenVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AccountInterceptor implements HandlerInterceptor{
	
	@Autowired
	private TokenService tokenService;
	
	// 사용자가 보낸 요청의 헤더에 있는 Authorization 분석 및 판정
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// [1] OPTIONS 요청 통과
		if(request.getMethod().equalsIgnoreCase("options")) {
			return true;
		}
		// [2] Authorization 헤더 검사
		try {// 정상적인 로그인 상태
			String authorization = request.getHeader("Authorization");
			if(authorization == null)//헤더가 없음 = 비회원
				throw new UnauthorizationException();//플랜 B로 던져!
			
			//토큰 해석
			TokenVO tokenVO = tokenService.parse(authorization);
			//이어지는 컨트롤러에서 사용 가능하도록 넘기겠다
			//-> 컨트롤러에서는 @RequestAttribute TokenVO tokenVO로 수신 가능
			request.setAttribute("tokenVO", tokenVO);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			response.sendError(401);//Unauthorized
			return false;
		}
	}

}
