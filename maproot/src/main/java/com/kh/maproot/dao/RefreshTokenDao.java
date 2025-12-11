package com.kh.maproot.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.RefreshTokenDto;

@Repository
public class RefreshTokenDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public void insert(RefreshTokenDto refreshTokenDto) {
		sqlSession.insert("refreshToken.insert", refreshTokenDto);
	}
	
	public RefreshTokenDto selectOne(RefreshTokenDto refreshTokenDto) {
		return sqlSession.selectOne("refreshToken.detail", refreshTokenDto);
	}
	
	public boolean delete(Long refreshTokenNo) {
		return sqlSession.delete("refreshToken.delete", refreshTokenNo) > 0 ;
	}
	
	public boolean deleteByTarget(String refreshTokenTarget) {
		return sqlSession.delete("refreshToken.deleteByTarget", refreshTokenTarget) > 0 ;
	}

}
