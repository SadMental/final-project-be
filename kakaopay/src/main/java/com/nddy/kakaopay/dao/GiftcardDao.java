package com.nddy.kakaopay.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nddy.kakaopay.dto.GiftcardDto;

@Repository
public class GiftcardDao {
	@Autowired
	private SqlSessionTemplate  sqlSession;
	
	public List<GiftcardDto> selectList() {
		return sqlSession.selectList("giftcard.list");
	}
	public GiftcardDto selectOne(Long giftcardNo) {
		return sqlSession.selectOne("giftcard.list", giftcardNo);
	}
}