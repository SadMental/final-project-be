package com.kh.maproot.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.maproot.dao.ShopDao;
import com.kh.maproot.dto.ShopDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name="상품권 목록 컨트롤러")

@CrossOrigin
@RestController
@RequestMapping("/shop") 
@Slf4j
public class ShopRestController {
	@Autowired
	private ShopDao shopDao;
	
	@Operation(summary = "상품권 목록 조회", description = "Shop-List-ShopDto")
	@GetMapping("/")
	public List<ShopDto> list(){
		List<ShopDto> shopDto = shopDao.selectList();
		return shopDto;
	}
	
	@Operation(summary = "상품권 상세 조회", description = "Shop-ShopDto")
	@GetMapping("/{shopItemNo}")
	public ShopDto detail(@PathVariable long shopNo){
		return shopDao.selectOne(shopNo);
	}
}