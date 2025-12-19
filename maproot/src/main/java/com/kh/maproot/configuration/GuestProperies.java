package com.kh.maproot.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "custom.guest")
public class GuestProperies {

	private String keyStr;
	private String issuer;
	private int expiration;//엑세스토큰 만료시간(시간)
	private int renewalLimit;//갱신처리 기준 시간
	
}
