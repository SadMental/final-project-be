package com.kh.maproot.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.ScheduleDto;
import com.kh.maproot.dto.ScheduleRouteDto;
import com.kh.maproot.dto.ScheduleUnitDto;

@Repository
public class ScheduleRouteDao {
	@Autowired
	private SqlSession sqlSession;
	
	public ScheduleRouteDto insert(ScheduleRouteDto routeDto) {
		long sequence = sqlSession.selectOne("scheduleRoute.sequence");
		routeDto.setScheduleRouteNo(sequence);
		sqlSession.insert("scheduleRoute.insert", routeDto);
		return sqlSession.selectOne("scheduleRoute.detail", sequence);
		
	}

	public boolean deleteByScheduleNo(Long scheduleNo) {
		return sqlSession.delete("scheduleRoute.deleteByScheduleNo", scheduleNo) > 0;		
	}

	public List<ScheduleRouteDto> selectList(ScheduleDto scheduleDto) {
		List<ScheduleRouteDto> routeList = sqlSession.selectList("scheduleRoute.selectList", scheduleDto);
		return routeList.isEmpty()? List.of():routeList;
	}
}
