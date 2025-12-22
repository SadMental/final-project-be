package com.kh.maproot.kakaomap;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;

import com.kh.maproot.MaprootApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = MaprootApplication.class)
public class KakaoMapTest2 {
	
	@Autowired @Qualifier("kakaomapLocal")
	private WebClient webClient;
	
	@Test
	public void test() {		
		// Body 준비
		
		Map response = webClient.get() // 포스트 요철
				.uri(uriBuilder -> uriBuilder
				        .path("/geo/coord2address") // 🚨 baseUrl 이후의 경로만 지정
				        .queryParam("x", "127.1112") // 🚨 쿼리 파라미터로 데이터 전달
				        .queryParam("y", "37.3949")
				        .build()
				    )
			.retrieve() // 응답을 수신하겠다
				.bodyToMono(Map.class) // 데이터는 한번에 오고(Mono) 형태는 Map이다 (연속적으로 오면 Flux)
				.block(); // 동기적으로 변환하여 응답이 올때까지 기다려라. (RestTemplate과 같아짐)

		// 출력 (다음 테스트를 위해 정보 확인_
		for(Object name : response.keySet()) {
			Object value = response.get(name);
			log.debug("{} = {}", name, value);
		}		
	}
}
