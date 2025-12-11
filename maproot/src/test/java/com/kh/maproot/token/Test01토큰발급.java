package com.kh.maproot.token;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kh.maproot.dto.AccountDto;
import com.kh.maproot.service.TokenService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class Test01토큰발급 {
	
	@Autowired
	private TokenService tokenService;
	
	@Test
	public void test() {
		String token = tokenService.generateRefreshToken(AccountDto.builder()
					.accountId("testuser1")
					.accountLevel("일반회원")
				.build());
		log.debug("토큰 = {}", token);
	}
	

}
