package com.kh.maproot.service;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ShareAuthService {

	public String getClientIp(HttpServletRequest request) {
		System.out.println(request);
		return request.getRemoteAddr(); // 접속한 사람 IP
	}
}
