package com.kh.maproot.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "custom.jwt")
public class JwtProperties {
	private String keyStr;//custom.jwt.key-str을 불러와서 저장하세요
	private String issuer;
	private int expiration;//엑세스토큰 만료시간(분)
	private int refreshExpiration;//갱신토큰 만료시간(일)
	private int renewalLimit;//갱신처리 기준 시간

}
