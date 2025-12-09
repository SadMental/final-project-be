package com.kh.maproot.encoder;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class Test01단방향암호화 {
	
	@Test
	public void test() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		String password = "Testuser1!";
		String result = encoder.encode(password);
		
		log.debug(result);
		
	}

}
