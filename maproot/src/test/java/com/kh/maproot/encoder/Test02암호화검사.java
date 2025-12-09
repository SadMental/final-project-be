package com.kh.maproot.encoder;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class Test02암호화검사 {

	@Test
	public void test() {
		List<String> data = List.of(
				"$2a$10$yxl5Cj7DSAXnIxKUkODyROWUG0NS/bdkcNZOYmpP1BZEnlE0adjG6",
				"$2a$10$txu8yd72816GZkwWN8HphOeHLCmb66kUVtwx123sg8mbkSTs1/X8m");
		String password = "Testuser1!";
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		for(String str : data) {
			log.debug("비밀번호 일치 여부 = {}", encoder.matches(password, str));
		}
	}
}
