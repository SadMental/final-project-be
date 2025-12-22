package com.kh.maproot.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.ReviewDto;
import com.kh.maproot.dto.ReviewUnitLinkDto;

@Repository
public class ReviewUnitLinkDao {

	@Autowired
	private SqlSession sqlSession;
	
	public boolean deleteBySelectUnit(int reviewNo, int scheduleUnitNo) {
		Map<String, Object> params = new HashMap<>();
		params.put("reviewNo", reviewNo);
		params.put("scheduleUnitNo", scheduleUnitNo);
		System.out.println("배열확인"+params);
		return sqlSession.delete("reviewUnitLink.deleteBySelectUnit", params) > 0;
		
	};
	
	public void insert(Integer reviewNo ,List<Integer> scheduleUnitList) {
		
		for(Integer scheduleUnitNo : scheduleUnitList) {
			ReviewUnitLinkDto reviewUnitLinkDto=
				ReviewUnitLinkDto.builder()
				.linkNo( sqlSession.selectOne("reviewUnitLink.sequence"))
				.reviewNo(reviewNo)
				.scheduleUnitNo(scheduleUnitNo)
				.build();
				
		 sqlSession.insert("reviewUnitLink.insert",reviewUnitLinkDto);
			
		}
	}

	public List<ReviewUnitLinkDto> selectByReviewNo( int reviewNo) {
		System.out.println("숫자확인"+reviewNo);
		return sqlSession.selectList("reviewUnitLink.selectByReviewNo", reviewNo);
	}
	
}
