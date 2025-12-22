package com.kh.maproot.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.maproot.dao.AccountDao;
import com.kh.maproot.dao.AccountLikeDao;
import com.kh.maproot.dao.ScheduleDao;
import com.kh.maproot.dto.AccountDto;
import com.kh.maproot.dto.ScheduleDto;
import com.kh.maproot.error.NeedPermissionException;
import com.kh.maproot.error.TargetAlreadyExistsException;
import com.kh.maproot.error.TargetNotfoundException;
import com.kh.maproot.error.UnauthorizationException;
import com.kh.maproot.vo.TokenVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountService {
	
	@Autowired
	private AccountDao accountDao;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private AttachmentService attachmentService;
	@Autowired
	private AccountLikeDao accountLikeDao;
	@Autowired
	private ScheduleDao scheduleDao;
	// 회원가입을 위한 서비스
	@Transactional
	public void join(AccountDto accountDto, MultipartFile attach) throws IllegalStateException, IOException {
		// [1] 아이디 중복검사
		if(accountDao.countByAccountId(accountDto.getAccountId()) > 0)
			throw new TargetAlreadyExistsException("이미 존재하는 아이디입니다");
		
		// [2] 닉네임 중복검사
		if(accountDao.countByAccountNickname(accountDto.getAccountNickname()) > 0)
			throw new TargetAlreadyExistsException("이미 존재하는 닉네임입니다");
		
		// [3] 연락처 중복검사
		if(accountDao.countByAccountContact(accountDto.getAccountContact()) > 0)
			throw new TargetAlreadyExistsException("이미 존재하는 전화번호입니다");
		
		// 비밀번호 암호화
		String encryptPassword = passwordEncoder.encode(accountDto.getAccountPw());
		accountDto.setAccountPw(encryptPassword);
		
		// 등록
		accountDao.insert(accountDto);
		
		// 회원 프로필 추가(회원프로필은 등록 후 해야함)
		if(attach != null && attach.isEmpty() == false) {
			Long attachmentNo = attachmentService.save(attach);
			accountDao.connect(accountDto.getAccountId(), attachmentNo);
		}
		
		
	}
	
	// 회원 탈퇴 서비스
	@Transactional
	public boolean drop(String accountId, String accountPw) {
		// 1. DB에 존재하는 회원정보를 조회
		AccountDto accountDto = accountDao.selectOne(accountId);
		if(accountDto == null) throw new TargetNotfoundException();
		
		// 2. 비밀번호 비교
		boolean isValid = passwordEncoder.matches(accountPw, accountDto.getAccountPw());
		if(!isValid) throw new UnauthorizationException("비밀번호가 일치하지 않습니다");
		
		// 3. 회원 프로필 사진 조회
		try {
			Long attachmentNo = accountDao.findAttach(accountId);
			attachmentService.delete(attachmentNo);
		}
		catch (Exception e) {
			
		}
		// 4. 회원 정보 삭제 
		accountDao.delete(accountId);
		
		return true;
	}
	
	// 회원의 일정 좋아요
	@Transactional
	public int toggleSchedulelike(String accountId, Long scheduleNo) {
	    // 1. 사용자 확인
	    AccountDto accountDto = accountDao.selectOne(accountId);
	    if(accountDto == null) throw new TargetNotfoundException("회원이 존재하지 않습니다");
	    
	    // 2. 스케줄 확인
	    ScheduleDto scheduleDto = scheduleDao.selectByScheduleNo(scheduleNo);
	    if(scheduleDto == null) throw new TargetNotfoundException("존재하지 않는 일정");
	    
	    // 3. 이미 좋아요 했는지 확인 및 토글
	    boolean isChecked = accountLikeDao.check(accountId, scheduleNo);
	    if(isChecked) {
	        accountLikeDao.delete(accountId, scheduleNo); // 삭제
	    } else {
	        accountLikeDao.insert(accountId, scheduleNo); // 추가
	    }

	    // [핵심] 4. 변경된 후의 '총 좋아요 개수'를 조회해서 반환 (return)
	    // DAO에 scheduleNo에 해당하는 좋아요 개수를 세는 메서드(count)가 필요합니다.
	    return accountLikeDao.countLikes(scheduleNo);
	}
	
	
	@Transactional
	public boolean dropAdmin(String accountId, TokenVO tokenVO) {
		if(!tokenVO.getLoginLevel().equals("관리자")) throw new NeedPermissionException();
		
		// 1. DB에 존재하는 회원정보를 조회
		AccountDto findtDto = accountDao.selectOne(accountId);
		if(findtDto == null) throw new TargetNotfoundException();
		
		// 3. 회원 프로필 사진 조회
		try {
			Long attachmentNo = accountDao.findAttach(findtDto.getAccountId());
			attachmentService.delete(attachmentNo);
		}
		catch (Exception e) {
			
		}
		// 4. 회원 정보 삭제 
		accountDao.delete(findtDto.getAccountId());
		
		return true;
	}
}
