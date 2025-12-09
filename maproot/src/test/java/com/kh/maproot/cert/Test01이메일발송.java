package com.kh.maproot.cert;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.kh.maproot.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class Test01이메일발송 {
	
	@Autowired
	private EmailService emailService;
	
	@Test
	public void test() {
		emailService.sendEmail("khthk0411@gmail.com", 
				"테스트메일2", "테스트메일2");
	}

}
