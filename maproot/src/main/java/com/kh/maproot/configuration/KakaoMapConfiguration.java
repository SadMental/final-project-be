package com.kh.maproot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KakaoMapConfiguration {

	@Bean(name = "kakaomapWebClient")
	public WebClient webClient() {
		return WebClient.builder()
				.baseUrl("https://apis-navi.kakaomobility.com") // 시작주소 지정
				.defaultHeader("Authorization", "KakaoAK 2be850ebcd2e3cb4e67e989a5398494c")
				.defaultHeader("Content-Type", "application/json") // 전송데이터 유형설정
				.exchangeStrategies(ExchangeStrategies.builder()
		                .codecs(configurer -> configurer
		                    .defaultCodecs()
		                    .maxInMemorySize(10 * 1024 * 1024)) // 10MB 설정
		                .build())
		.build();
	}
	
}
