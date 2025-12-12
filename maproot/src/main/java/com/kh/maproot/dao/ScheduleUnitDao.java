package com.kh.maproot.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.ScheduleUnitDto;

import lombok.extern.slf4j.Slf4j;

@Repository @Slf4j
public class ScheduleUnitDao {
	@Autowired
	private SqlSession sqlSession;
	
	public ScheduleUnitDto insert(ScheduleUnitDto unitDto) {
		long sequence = sqlSession.selectOne("scheduleUnit.sequence");
		unitDto.setScheduleUnitNo(sequence);
		sqlSession.insert("scheduleUnit.insert", unitDto);
		return sqlSession.selectOne("scheduleUnit.detail", sequence);
		
	}
}
