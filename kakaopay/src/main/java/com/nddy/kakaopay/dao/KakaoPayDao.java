package com.nddy.kakaopay.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nddy.kakaopay.dto.KakaoPayDto;

@Repository
public class KakaoPayDao {
	@Autowired
	private SqlSession sqlSession;
	
	public long origin() {
		return sqlSession.selectOne("kakaopay.origin");
	}
	
	public long update(KakaoPayDto kakaopayDto) {
		return sqlSession.update("kakaopay.update", kakaopayDto);
	}
}
