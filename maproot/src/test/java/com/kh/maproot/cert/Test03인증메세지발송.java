package com.kh.maproot.cert;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kh.maproot.service.CertService;

@SpringBootTest
public class Test03인증메세지발송 {
	
	@Autowired
	private CertService certService;
	
	@Test
	public void test() {
		certService.sendCertPhone("01050123993");
	}

}
