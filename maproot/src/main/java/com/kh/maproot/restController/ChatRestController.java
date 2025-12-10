package com.kh.maproot.restController;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.maproot.dao.ChatDao;
import com.kh.maproot.dto.ChatDto;

@CrossOrigin
@RestController
@RequestMapping("/chat")
public class ChatRestController {
	@Autowired
	private ChatDao chatDao;
	
//	@PostMapping("/")
//	public ChatDto create(@RequestBody ChatDto chatDto,
//						@RequestAttribute TokenVO tokenVO) {
//		ChatDto resultDto = chatDao.insert(chatDto);
//		chatDao.enter(resultDto.getChatNo(), tokenVO.getLoginId());
//		return resultDto;
//	}
	@PostMapping("/")
	public ChatDto create(@RequestBody ChatDto chatDto) { 
	// 💡 토큰VO 제거. 요청 본문에는 chatDto (방생성 DTO)만 받습니다.
	    
	    // 🚨 임시: 테스트용 ID 하드코딩 (운영 시 반드시 토큰으로 변경해야 함)
	    String userAccountId = "temp_user_A"; 
	    String counselorId = "counselor_001";
	    
	    // 1. 방 생성 (chatNo 획득)
	    ChatDto resultDto = chatDao.insert(chatDto);
	    
	    // 2. 일반 회원 입장
	    chatDao.enter(resultDto.getChatNo(), userAccountId);
	    
	    // 3. 상담원 입장 (1:1 채팅 완성)
	    chatDao.enter(resultDto.getChatNo(), counselorId);
	    
	    // 이 시점에서 chatNo를 포함한 DTO 반환
	    return resultDto;
	}
	
//	//상담사 용 목록
//	@GetMapping("list")
//	public List<ChatDto> list() {
//		return chatDao.selectList();
//	}
//	@GetMapping("/{chatNo}")
//	public ChatDto detail(@PathVariable int chatNo) {
//		return chatDao.selectOne(chatNo);
//	}
//	
//	@PostMapping("/enter")
//	public void enter(@RequestBody ChatDto chatDto,
//			@RequestAttribute TokenVO tokenVO) {
//		ChatDto findDto = chatDao.selectOne(chatDto.getChatNo());
//		chatDao.enter(chatDto.getChatNo(), tokenVO.getLoginId());
//	}
//	@PostMapping("/check")
//	public Map<String, Boolean> check(@RequestBody ChatDto chatDto,
//			@RequestAttribute TokenVO tokenVO) {
//		return Map.of(
//			"result",
//			chatDao.check(chatDto.getChatNo(), tokenVO.getLoginId())
//		);
//	}
}
