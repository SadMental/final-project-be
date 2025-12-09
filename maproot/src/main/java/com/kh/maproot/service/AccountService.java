package com.kh.maproot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.maproot.dao.AccountDao;
import com.kh.maproot.dto.AccountDto;
import com.kh.maproot.error.TargetNotfoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountService {
	
	@Autowired
	private AccountDao accountDao;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	// 회원가입을 위한 서비스
	@Transactional
	public void join(AccountDto accountDto) {
		// [1] 아이디 중복검사
		if(accountDao.countByAccountId(accountDto.getAccountId()) > 0)
			throw new TargetNotfoundException("이미 존재하는 아이디입니다");
		
		// [2] 닉네임 중복검사
		if(accountDao.countByAccountNickname(accountDto.getAccountNickname()) > 0)
			throw new TargetNotfoundException("이미 존재하는 닉네임입니다");
		
		// [3] 연락처 중복검사
		if(accountDao.countByAccountContact(accountDto.getAccountContact()) > 0)
			throw new TargetNotfoundException("이미 존재하는 전화번호입니다");
		
		// 비밀번호 암호화
		String encryptPassword = passwordEncoder.encode(accountDto.getAccountPw());
		accountDto.setAccountPw(encryptPassword);
		
		// 등록
		accountDao.insert(accountDto);
	}
}
