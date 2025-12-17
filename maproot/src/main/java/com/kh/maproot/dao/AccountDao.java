package com.kh.maproot.dao;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.AccountDto;

@Repository
public class AccountDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	// 회원가입
	public void insert(AccountDto accountDto) {
		sqlSession.insert("account.insert", accountDto);
	}
	// 회원 프로필
	public void connect(String accountId, long attachmentNo) {
		Map<String, Object> params = new HashMap<>();
		params.put("accountId", accountId);
		params.put("attachmentNo", String.valueOf(attachmentNo));
		sqlSession.insert("account.connect", params);
	}
	public long findAttach(String accountId) {
		return sqlSession.selectOne("account.findAttach", accountId);
	}
	// 중복검사
	public int countByAccountId(String accountId) {
		return sqlSession.selectOne("account.countByAccountId", accountId);
	}
	public int countByAccountNickname(String accountNickname) {
		return sqlSession.selectOne("account.countByAccountNickname", accountNickname);
	}
	public int countByAccountContact(String accountContact) {
		return sqlSession.selectOne("account.countByAccountContact", accountContact);
	}
	public int countByAccountEmail(String accountEmail) {
		return sqlSession.selectOne("account.countByAccountEmail", accountEmail);
	}
	
	// 로그인 시 회원의 로그인 시간 업데이트 
	public void updateLoginTime(String accountId) {
		sqlSession.update("account.updateLoginTime", accountId);
	}
	// 전체 수정
	public boolean update(AccountDto accountDto) {
		return sqlSession.update("account.update", accountDto) > 0;
	}
	// 부분 수정
	public boolean updatePw(String accountPw, String accountId) {
		Map<String, Object> params = new HashMap<>();
		params.put("accountId", accountId);
		params.put("accountPw", accountPw);
		return sqlSession.update("account.updatePw", params) > 0;
	}
	public boolean updateContact(String accountContact, String accountId) {
		Map<String, Object> params = new HashMap<>();
		params.put("accountId", accountId);
		params.put("accountContact", accountContact);
		return sqlSession.update("account.updateContact", params) > 0;
	}
	
	// 회원탈퇴 
	public boolean delete(String accountId) {
		return sqlSession.delete("account.delete", accountId) > 0;
	}
	
	// 회원조회(아이디)
	public AccountDto selectOne(String accountId) {
		return sqlSession.selectOne("account.detail", accountId);
	}
	
	//아이디 찾기
	public String findAccountId(String accountContact, String accountEmail) {
		Map<String, Object> params = new HashMap<>();
		params.put("accountContact", accountContact);
		params.put("accountEmail", accountEmail);
		
		return sqlSession.selectOne("account.findAccountId", params);
	}
	
	// 카카오페이 관련
	public AccountDto originMaxSchedule(String accountId) {
		return sqlSession.selectOne("account.originMaxSchedule", accountId);
	}
	
	public long updateMaxSchedule(AccountDto accountDto) {
		return sqlSession.update("account.updateMaxSchedule", accountDto);
	}

}
