package com.kh.maproot.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.TagDto;

@Repository
public class TagDao {

	@Autowired
	private SqlSession sqlSession;
	
	public List<TagDto> selectAll() {
		return sqlSession.selectList("tag.selectAll");
	}
}
