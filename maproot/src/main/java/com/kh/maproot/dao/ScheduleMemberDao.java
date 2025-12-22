package com.kh.maproot.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.ScheduleMemberDto;
import com.kh.maproot.schedule.vo.ScheduleCreateRequestVO;

@Repository
public class ScheduleMemberDao {

	@Autowired
	private SqlSession sqlSession;
	
	public List<ScheduleMemberDto> selectByAccountId (String accountId) {

		Map<String, Object> params = new HashMap<>();
		params.put("accountId", accountId);
		
		return sqlSession.selectList("scheduleMember.select", params);
	}

	public Integer selectMemberCount(Long scheduleNo) {
		return sqlSession.selectOne("scheduleMember.selectMemberCount", scheduleNo);
	}

	public void insert(ScheduleMemberDto scheduleMemberDto) {
		sqlSession.insert("scheduleMember.insert", scheduleMemberDto);
	}
	
	public List<ScheduleMemberDto> selectByScheduleNo(Long scheduleNo) {
	System.out.println("selectByScheduleNo dao 진입 => scheduleNo = "+ scheduleNo);
		Map<String, Object> params = new HashMap<>();
		params.put("scheduleNo", scheduleNo);
		
		return sqlSession.selectList("scheduleMember.select", params);
	}
	
	public boolean exists(Long scheduleNo, String accountId) {
	    Integer result = sqlSession.selectOne("scheduleMember.exists", Map.of(
	        "scheduleNo", scheduleNo,
	        "accountId", accountId
	    ));
	    return result != null && result == 1;
	}



}
