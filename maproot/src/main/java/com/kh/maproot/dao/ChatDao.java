package com.kh.maproot.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.ChatDto;

@Repository
public class ChatDao {
	@Autowired
	private SqlSession sqlSession;
	
	public ChatDto insert(ChatDto chatDto) {
	//		int sequence = sqlSession.selectOne("chat.sequence");
	//		chatDto.setChatNo(sequence);
		sqlSession.insert("chat.insert", chatDto);
		return chatDto;
	}
	
	public List<ChatDto> selectList() {
		return sqlSession.selectList("chat.list");
	}
	
	public ChatDto selectOne(int chatNo) {
		return sqlSession.selectOne("chat.detail", chatNo);
	}
	
	public void enter(int chatNo, String accountId) {
		Map<String, Object> params = new HashMap<>();
		params.put("partyChat", chatNo);
		params.put("partyAccount", accountId);
		sqlSession.insert("chat.enter", params);
	}
	
	public boolean leave(int chatNo, String accountId) {
		Map<String, Object> params = new HashMap<>();
		params.put("partyChat", chatNo);
		params.put("partyAccount", accountId);
		
		int result = sqlSession.delete("chat.leave", params);
		return result > 0;
	}
	
	public boolean check(int chatNo, String accountId) {
		Map<String, Object> params = new HashMap<>();
		params.put("partyChat", chatNo);
		params.put("partyAccount", accountId);
		int count = sqlSession.selectOne("chat.check", params);
		return count > 0;
	}
}
