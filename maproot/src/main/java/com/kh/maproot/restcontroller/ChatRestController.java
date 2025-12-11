package com.kh.maproot.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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


//@CrossOrigin
@CrossOrigin(origins = "*")
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

	@PostMapping
	@Transactional
	public ChatDto create(@RequestBody ChatDto chatDto) {

	    // 1) 채팅방 생성 (DAO 내부에서 시퀀스 처리)
	    ChatDto resultDto = chatDao.insert(chatDto);

	    // 테스트용 계정
	    String userAccountId = "testuser1";
	    String counselorId = "testuser2";

	    // 2) 참여자 등록
	    chatDao.enter(resultDto.getChatNo(), userAccountId);
	    chatDao.enter(resultDto.getChatNo(), counselorId);

	    return resultDto;
	}

	//상담사 용 목록
	@GetMapping("list")
	public List<ChatDto> list() {
		return chatDao.selectList();
	}
	@GetMapping("/{chatNo}")
	public ChatDto detail(@PathVariable int chatNo) {
		return chatDao.selectOne(chatNo);
	}

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
