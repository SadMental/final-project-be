package com.kh.maproot.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.GuestDto;

@Repository
public class GuestDao {

	@Autowired
	private SqlSession sqlSession;
	
	public int sequence() {
		return sqlSession.selectOne("guest.sequence");
	}
	
	public void insert(GuestDto guestDto) {
		sqlSession.insert("guest.insert", guestDto);
	}
	
	public GuestDto selectByKey(String guestKey) {
		return sqlSession.selectOne("guest.select", guestKey);
	}

	public boolean update(GuestDto guestDto) {
		return sqlSession.update("guest.update", guestDto) > 0;
	}
	
	public GuestDto selectByGuestNo(int guestNo) {
		return sqlSession.selectOne("guest.selectOne", guestNo);
	}
	
	//중복검사
	public boolean selectByNickname(String guestNickname) {
		  GuestDto guest = sqlSession.selectOne(
			        "guest.selectByNickname", guestNickname
			    );
			    return guest != null;
	}
}
