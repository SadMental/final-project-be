package com.kh.maproot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.maproot.configuration.JwtProperties;

@Service
public class GuestTokenService {

	@Autowired
	private JwtProperties jwtProperties;
	
}
