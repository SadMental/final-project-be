package com.kh.maproot.cert;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kh.maproot.service.CertService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class Test02이메일발송 {
	
	@Autowired
	private CertService certService;
	
	@Test
	public void test() {
		String targetEmail = "khthk0411@gmail.com";
		
		log.info("이메일 발송 시작...");
		certService.sendCertEmail(targetEmail);
		log.info("이메일 발송 완료!");
		
	}

}
