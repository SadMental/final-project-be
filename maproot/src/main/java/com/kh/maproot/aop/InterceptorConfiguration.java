package com.kh.maproot.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfiguration implements WebMvcConfigurer{
	
	@Autowired
	private AccountInterceptor accountInterceptor;
	@Autowired
	private TokenRenewalInterceptor tokenRenewalInterceptor;
	
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accountInterceptor)
                    .addPathPatterns(
                        "/account/logout",
                        "/chat/**",
                        "/kakaopay/buy",
                        "/payment/**"
                    )
                    .excludePathPatterns(
                        
                    );
        registry.addInterceptor(tokenRenewalInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns(
                            "/account/refresh",
                            "/account/join",
                            "/account/login",
                            "/account/logout"
                            );
    }
}
