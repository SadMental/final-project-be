package com.kh.maproot.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.ScheduleDto;

@Repository
public class ScheduleDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public ScheduleDto insert(ScheduleDto scheduleDto) {
		int sequence = sqlSession.selectOne("schedule.sequence");
		scheduleDto.setScheduleNo(sequence);
		sqlSession.insert("schedule.insert", scheduleDto);
		return scheduleDto;
	}

}
