package com.kh.maproot.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * REST API 전용 CORS 설정
 * 
 * - WebSocket(/ws) CORS 설정과 분리하여 관리한다
 * - React 개발 서버(http://localhost:5173)에서
 *   axios 등을 통해 호출하는 REST 요청을 허용한다
 * - 채팅 관련 사전 검증 API (/chat/**)만 개방한다
 * 
 * ※ WebSocket CORS는 WebSocketConfiguration에서 별도로 처리한다
 */

@Configuration
public class CorsConfig implements WebMvcConfigurer {
	@Override
	public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/*") // 모든 경로에 대해
                 .allowedOrigins("http://localhost:5173/", "http://192.168.20.16:5173") // 특정 IP만 허용할 수도 있지만,
//                .allowedOriginPatterns("") // 개발 중에는 모든 출처(IP) 허용 (추천)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 모든 HTTP 메서드 허용
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 쿠키/인증 정보 포함 허용
    }
}
