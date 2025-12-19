package com.kh.maproot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

// nPhwLBLZEa7mpEtS3laFM2oHAVJrxDuJQUo0Er42

@Configuration
public class TmapConfiguration {
	
	@Bean(name = "TmapWebClient")
	public WebClient webClient() {
		return WebClient.builder()
				.baseUrl("https://apis.openapi.sk.com/tmap") // 시작주소 지정
				.defaultHeader("appKey", "nPhwLBLZEa7mpEtS3laFM2oHAVJrxDuJQUo0Er42")
				.defaultHeader("Content-Type", "application/json") // 전송데이터 유형설정
				.exchangeStrategies(ExchangeStrategies.builder()
		                .codecs(configurer -> configurer
		                    .defaultCodecs()
		                    .maxInMemorySize(10 * 1024 * 1024)) // 10MB 설정
		                .build())
		.build();
	}
}
