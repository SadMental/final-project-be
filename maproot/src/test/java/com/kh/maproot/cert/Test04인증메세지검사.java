package com.kh.maproot.cert;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kh.maproot.service.CertService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class Test04인증메세지검사 {
	
	@Autowired
	private CertService certService;
	
	@Test
	public void test() {
		
		boolean result = certService.checkCertNumber("khthk0411@gmail.com", "350202");
		
		log.info("인증 확인 결과 = {}", result);
		
		if(result) {
			log.info("인증 성공! (DB에서 데이터가 삭제되었습니다)");
		} else {
			log.info("인증 실패! (번호가 틀렸거나 데이터가 없습니다)");
		}
	}

}
