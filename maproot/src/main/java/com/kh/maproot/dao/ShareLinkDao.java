package com.kh.maproot.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.ShareLinkDto;

@Repository
public class ShareLinkDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public void insert(ShareLinkDto shareLinkDto) {
		sqlSession.insert("shareLink.insert", shareLinkDto);
	}
	
	public ShareLinkDto select(String shareKey) {
		return sqlSession.selectOne("shareLink.select",shareKey);
	}

}
