package com.kh.maproot.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.ScheduleDto;
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
	
	public ScheduleUnitDto selectFirstUnit(Long scheduleNo) {
		return sqlSession.selectOne("scheduleUnit.selectFirstUnit", scheduleNo);
	}
	
	//총 일정 개수
	public Integer selectUnitCount(Long scheduleNo) {
		return sqlSession.selectOne("scheduleUnit.selectUnitCount", scheduleNo);	
	}

	public boolean selectUnitForKey(String scheduleKey) {
		return sqlSession.selectOne("scheduleUnit.selectUnitForKey", scheduleKey) != null;
	}

	public boolean deleteByScheduleNo(Long scheduleNo) {
		return sqlSession.delete("scheduleUnit.deleteByScheduleNo", scheduleNo) > 0;
	}

	public List<ScheduleUnitDto> selectList(ScheduleDto scheduleDto) {
		List<ScheduleUnitDto> unitList = sqlSession.selectList("scheduleUnit.selectList", scheduleDto);
		return unitList.isEmpty()? List.of():unitList;
	}
}
