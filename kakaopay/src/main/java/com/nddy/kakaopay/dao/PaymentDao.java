package com.nddy.kakaopay.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nddy.kakaopay.dto.PaymentDto;
import com.nddy.kakaopay.vo.TokenVO;

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
}





