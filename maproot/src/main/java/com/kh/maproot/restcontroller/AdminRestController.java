package com.kh.maproot.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.maproot.dao.AccountDao;
import com.kh.maproot.dao.PaymentDao;
import com.kh.maproot.dao.ScheduleDao;
import com.kh.maproot.dto.PaymentDto;
import com.kh.maproot.dto.ScheduleDto;
import com.kh.maproot.vo.AccountComplexSearchVO;
import com.kh.maproot.vo.AccountForAdminVO;
import com.kh.maproot.vo.AccountListVO;
import com.kh.maproot.vo.PageVO;
import com.kh.maproot.vo.PaymentListVO;
import com.kh.maproot.vo.PaymentSearchVO;
import com.kh.maproot.vo.ScheduleListVO;
import com.kh.maproot.vo.ScheduleSearchVO;
import com.kh.maproot.vo.TokenVO;

import lombok.extern.slf4j.Slf4j;

@RestController
@CrossOrigin @Slf4j
@RequestMapping("/admin")
public class AdminRestController {
	@Autowired
	private AccountDao accountDao;
	@Autowired
	private PaymentDao paymentDao;
	@Autowired
	private ScheduleDao scheduleDao;
	
	@GetMapping("/account/list/page/{page}")
	public AccountListVO list(@RequestAttribute TokenVO tokenVO, @PathVariable int page) {
		PageVO pageVO = new PageVO();
		pageVO.setPage(page);
		pageVO.setSize(10);
		pageVO.setDataCount(accountDao.count());
		List<AccountForAdminVO> list = accountDao.selectListForAdmin(tokenVO, pageVO);
		
		return AccountListVO.builder()
				.page(pageVO.getPage())
				.size(pageVO.getSize())
				.count(pageVO.getDataCount())
				.begin(pageVO.getBegin())
				.end(pageVO.getEnd())
				.last(pageVO.getPage() >= pageVO.getTotalPage())
				.list(list)
			.build();
	}
	
	@PostMapping("/schedule/list")
	public ScheduleListVO scheduleList(@RequestBody ScheduleSearchVO searchVO ,@RequestAttribute TokenVO tokenVO){
		PageVO pageVO = new PageVO();
		pageVO.setPage(searchVO.getPage());
		pageVO.setSize(10);
		pageVO.setDataCount(scheduleDao.countForSearch(searchVO, tokenVO));
		List<ScheduleDto> list = scheduleDao.selectListForSearch(searchVO, pageVO);
		
		return ScheduleListVO.builder()
				.page(pageVO.getPage())
				.size(pageVO.getSize())
				.count(pageVO.getDataCount())
				.begin(pageVO.getBegin())
				.end(pageVO.getEnd())
				.last(pageVO.getPage() >= pageVO.getTotalPage())
				.list(list)
			.build();
	}
	
	@PostMapping("/complexSearch")
	public AccountListVO complexSearch(@RequestBody AccountComplexSearchVO searchVO, @RequestAttribute TokenVO tokenVO){
		log.debug("searchVO = {}", searchVO);
		PageVO pageVO = new PageVO();
		pageVO.setPage(searchVO.getPage());
		pageVO.setSize(10);
		pageVO.setDataCount(accountDao.countForComplex(searchVO));
		List<AccountForAdminVO> list = accountDao.complexSearch(searchVO, pageVO, tokenVO);
		
		return AccountListVO.builder()
				.page(pageVO.getPage())
				.size(pageVO.getSize())
				.count(pageVO.getDataCount())
				.begin(pageVO.getBegin())
				.end(pageVO.getEnd())
				.last(pageVO.getPage() >= pageVO.getTotalPage())
				.list(list)
			.build();
	}
	
	@PostMapping("/payment/list")
	public PaymentListVO payment(@RequestBody PaymentSearchVO searchVO, @RequestAttribute TokenVO tokenVO){
		PageVO pageVO = new PageVO();
		pageVO.setPage(searchVO.getPage());
		pageVO.setSize(10);
		pageVO.setDataCount(paymentDao.countByPagingAll(searchVO, tokenVO));
		
		List<PaymentDto> list = paymentDao.selectListAll(searchVO, pageVO);
		
		return PaymentListVO.builder()
					.page(pageVO.getPage())
					.size(pageVO.getSize())
					.count(pageVO.getDataCount())
					.begin(pageVO.getBegin())
					.end(pageVO.getEnd())
					.last(pageVO.getPage() >= pageVO.getTotalPage())
					.list(list)
				.build();
	}
}
