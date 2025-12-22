package com.kh.maproot.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.maproot.dto.PaymentDto;
import com.kh.maproot.error.NeedPermissionException;
import com.kh.maproot.vo.PageVO;
import com.kh.maproot.vo.PaymentParamVO;
import com.kh.maproot.vo.PaymentSearchVO;
import com.kh.maproot.vo.TokenVO;

@Repository
public class PaymentDao {
	@Autowired
	private SqlSession sqlSession;
	
	public long sequence() {
		return sqlSession.selectOne("payment.sequence");
	}
	public void insert(PaymentDto paymentDto) {
		sqlSession.insert("payment.insert", paymentDto);
	}
	public List<PaymentDto> selectList(TokenVO tokenVO) {
		return sqlSession.selectList("payment.listByOwner", tokenVO);
	}
	public PaymentDto selectOne(long paymentNo) {
		return sqlSession.selectOne("payment.detail", paymentNo);
	}
	
	public boolean cancelAll(long paymentNo) {
		return sqlSession.update("payment.cancelAll", paymentNo) > 0;
	}
	public boolean cancelUnit(long paymentNo, int paymentRemain) {
		Map<String, Object> params = new HashMap<>();
		params.put("paymentNo", paymentNo);
		params.put("paymentRemain", paymentRemain);
		return sqlSession.update("payment.cancelUnit", params) > 0;
	}
	
	public PaymentDto whoCancelAll(long paymentNo) {
		return sqlSession.selectOne("payment.whoCancelAll", paymentNo);
	}
	
	public int count(String paymentOwner) {
		return sqlSession.selectOne("payment.countByPaging", paymentOwner);
	}
	public List<PaymentDto> selectList(PageVO pageVO, String paymentOwner) {
		PaymentParamVO params = PaymentParamVO.builder()
				.begin(pageVO.getBegin())
				.end(pageVO.getEnd())
				.paymentOwner(paymentOwner)
				.build(); 
		return sqlSession.selectList("payment.listByPaging", params);
	}
	
	public List<PaymentDto> selectListAll(PaymentSearchVO searchVO, PageVO pageVO) {
	    // 1. 두 객체를 하나로 묶을 Map 생성
	    Map<String, Object> params = new HashMap<>();
	    params.put("search", searchVO);
	    params.put("page", pageVO);
	    
	    // 2. Map을 파라미터로 전달
	    return sqlSession.selectList("payment.listByPagingAll", params);
	}
	
	public int countByPagingAll(PaymentSearchVO searchVO, TokenVO tokenVO) {
	    if(!tokenVO.getLoginLevel().equals("관리자")) throw new NeedPermissionException();
	    
	    Map<String, Object> params = new HashMap<>();
	    params.put("search", searchVO); // XML 내의 search.column과 매핑됨
	    
	    return sqlSession.selectOne("payment.countByPagingAll", params);
	}
}





