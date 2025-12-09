package com.kh.maproot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class EmailService {
	
	@Autowired
	private JavaMailSenderImpl sender;
	
	private String senderEmail = "thk0411@naver.com";
	
	// 메일 발송
	@Transactional
	public void sendEmail(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(senderEmail);
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);
		sender.send(message);
	}
	
	// 인증메일 발송
	@Transactional
	public void sendCertNumber(String target, String certNumber) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(senderEmail);
		message.setTo(target);
		message.setSubject("[KH정보교육원] 인증번호를 확인하세요");
		message.setText("인증번호는 ["+certNumber+"] 입니다");
		sender.send(message);
	}

}
