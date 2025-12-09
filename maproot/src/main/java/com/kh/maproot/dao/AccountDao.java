package com.kh.maproot.dao;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.AccountDto;
import com.kh.maproot.error.TargetNotfoundException;

@Repository
public class AccountDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	// 회원가입
	public void insert(AccountDto accountDto) {
		sqlSession.insert("account.insert", accountDto);
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
		AccountDto accountDto = sqlSession.selectOne("account.detail", accountId);
		if(accountDto == null) throw new TargetNotfoundException("아이디가 존재하지 않습니다");
		return accountDto;
	}
	
	
	

}
