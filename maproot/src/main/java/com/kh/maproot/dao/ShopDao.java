package com.kh.maproot.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.ShopDto;

@Repository
public class ShopDao {
	@Autowired
	private SqlSessionTemplate  sqlSession;
	
	public List<ShopDto> selectList() {
		return sqlSession.selectList("shop.list");
	}
	public ShopDto selectOne(Long shopNo) {
		return sqlSession.selectOne("shop.list", shopNo);
	}
}