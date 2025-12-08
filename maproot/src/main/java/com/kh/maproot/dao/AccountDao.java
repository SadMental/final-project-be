package com.kh.maproot.dao;

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
	
	// 로그인 시 회원의 로그인 시간 업데이트 
	public void updateLoginTime(String accountId) {
		sqlSession.update("account.updateLoginTime", accountId);
	}
	
	//회원정보 수정
	// 전체 수정
	public boolean update(AccountDto accountDto) {
		return sqlSession.update("account.update", accountDto) > 0;
	}
	

}
