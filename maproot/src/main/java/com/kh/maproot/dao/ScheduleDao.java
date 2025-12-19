package com.kh.maproot.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.ScheduleDto;
import com.kh.maproot.error.TargetNotfoundException;

@Repository
public class ScheduleDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public Long insert(ScheduleDto scheduleDto) {
		Long sequence = sqlSession.selectOne("schedule.sequence");
		scheduleDto.setScheduleNo(sequence);
		sqlSession.insert("schedule.insert", scheduleDto);
		return sequence;
	}
	
	public ScheduleDto selectByScheduleNo(Long scheduleNo) {
		ScheduleDto scheduleDto = sqlSession.selectOne("schedule.selectByScheduleNo", scheduleNo);
		if(scheduleDto == null) throw new TargetNotfoundException();
		return scheduleDto;
	}
	public ScheduleDto selectByScheduleNo(ScheduleDto scheduleDto) {
		return selectByScheduleNo(scheduleDto.getScheduleNo());
	}

	public ScheduleDto updateUnit(ScheduleDto scheduleDto) {
		sqlSession.update("schedule.updateUnit", scheduleDto);
		 return selectByScheduleNo(scheduleDto.getScheduleNo());
	}

}
