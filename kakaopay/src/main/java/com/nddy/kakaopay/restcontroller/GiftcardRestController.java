package com.nddy.kakaopay.restcontroller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nddy.kakaopay.dao.GiftcardDao;
import com.nddy.kakaopay.dto.GiftcardDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name="상품권 목록 컨트롤러")

@CrossOrigin
@RestController
@RequestMapping("/giftcard") @Slf4j
public class GiftcardRestController {
	@Autowired
	private GiftcardDao giftcardDao;
	
	@Operation(summary = "상품권 목록 조회", description = "Giftcard-List-GiftcardDto")
	@GetMapping("/")
	public List<GiftcardDto> list(){
		List<GiftcardDto> giftcardDto = giftcardDao.selectList();
		log.debug("ASdadasd");
		log.debug(giftcardDto.toString());
		return giftcardDto;
	}
	
	@Operation(summary = "상품권 상세 조회", description = "Giftcard-GiftcardDto")
	@GetMapping("/{giftcardNo}")
	public GiftcardDto detail(@PathVariable long giftcardNo){
		return giftcardDao.selectOne(giftcardNo);
	}
}