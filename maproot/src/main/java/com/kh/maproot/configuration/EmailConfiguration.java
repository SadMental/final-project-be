package com.kh.maproot.configuration;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfiguration {
	
	@Autowired
	private EmailProperties emailProperties;
	
	@Bean
	public JavaMailSenderImpl sender() {
		// 메일 발송 도구 생성
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		
		//서비스 제공자 정보 설정
		sender.setHost("smtp.naver.com");
		sender.setPort(465);
		sender.setUsername(emailProperties.getUsername());
		sender.setPassword(emailProperties.getPassword());
		
		Properties props = new Properties();//추가 정보를 담을 저장소(String, String 형태의 Map)
		props.setProperty("mail.smtp.auth", "true");//이메일 발송에 인증을 사용(무조건 true)
		props.setProperty("mail.smtp.debug", "true");//이메일 발송과정을 자세하게 출력(오류 해결용)
		props.setProperty("mail.smtp.starttls.enable", "true");//STARTTLS 사용 (보안용 통신방식)
		
		props.setProperty("mail.smtp.ssl.enable", "true");
		props.setProperty("mail.smtp.ssl.trust", "smtp.naver.com");//신뢰할 수 있는 인증서 발급자 지정
		
		props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");//TLS 방식의 버전 선택
		sender.setJavaMailProperties(props);
		
		return sender;
	}

}
