package com.kh.maproot.dao;


import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.CertDto;

@Repository
public class CertDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public void insert(CertDto certDto) {
		sqlSession.insert("cert.insert", certDto);
	}
	
	public boolean check(CertDto certDto) {
		int count = sqlSession.selectOne("cert.check", certDto);
		return count > 0;
	}
	
	public boolean delete(String certTarget) {
		return sqlSession.delete("cert.delete", certTarget) > 0;  
	}

}
