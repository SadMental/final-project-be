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
import com.kh.maproot.error.TargetNotfoundException;
import com.kh.maproot.service.ChatService;
import com.kh.maproot.vo.TokenVO;

import lombok.extern.slf4j.Slf4j;

@CrossOrigin
@RestController @Slf4j
@RequestMapping("/chat")
public class ChatRestController {

    @Autowired
    private ChatDao chatDao;
    @Autowired
    private ChatService chatService;

    @PostMapping
    @Transactional
    public ChatDto create(
            @RequestBody ChatDto chatDto,
            @RequestAttribute TokenVO tokenVO) {
    	log.debug("tokenVO1 = {}", tokenVO);
        chatDto.setChatStatus("WAITING");

        ChatDto resultDto = chatDao.insert(chatDto);

        chatDao.enter(resultDto.getChatNo(), tokenVO.getLoginId());

        return resultDto;
    }

    //상담사 용 목록
    @GetMapping("/counselor/list")
    public List<ChatDto> counselorList(@RequestAttribute TokenVO tokenVO) {
        String accountLevel = tokenVO.getLoginLevel();

        if (!"상담사".equals(accountLevel)) {
            throw new TargetNotfoundException("접근 권한이 없습니다.");
        }
        //log.debug("tokenVO2 = {}", tokenVO);
        String counselorId = tokenVO.getLoginId();
        
        List<ChatDto> list = chatDao.selectCounselorList(counselorId);
        
        return list;
    }

    @GetMapping("/{chatNo}")
    public ChatDto detail(@PathVariable int chatNo) {
        return chatDao.selectOne(chatNo);
    }

    // 채팅방 상태 및 상담원 배정/해제
    // 태훈 - 서비스를 구현해 서비스만 호출
    @PostMapping("/status")
    public void changeStatus(
    		@RequestAttribute TokenVO tokenVO,
    		@RequestBody ChatDto chatDto) {
    	String loginId = tokenVO.getLoginId();
    	String loginLevel = tokenVO.getLoginLevel();
    	
    	// 1. 상담 시작(tokenVO에서 검사한 정보를 토대로 확인)
    	if("ACTIVE".equals(chatDto.getChatStatus())) {
    		if(!"상담사".equals(loginLevel)) {
    			throw new TargetNotfoundException("상담사가 아닙니다");
    		}
    		// 위에서 통과하면 서비스 호출
    		chatService.sendAgentAssigned(chatDto, loginId);
    	}
    	
    	String status = chatDto.getChatStatus();

    	if ("ACTIVE".equals(status)) {
            chatService.assignAgent(
                chatDto.getChatNo(),
                tokenVO.getLoginId(),
                tokenVO.getLoginLevel()
            );
            return;
        }

        if ("CLOSED".equals(status)) {
            chatService.closeChat(
                chatDto.getChatNo(),
                tokenVO.getLoginId(),
                tokenVO.getLoginLevel()
            );
            return;
        }

        throw new IllegalArgumentException("허용되지 않은 상태 변경");

//        chatService.assignAgent(
//            chatDto.getChatNo(),
//            tokenVO.getLoginId(),
//            tokenVO.getLoginLevel()
//        );
    }

    // 채팅방 참여 (party 테이블에 등록)
    @PostMapping("/enter")
    public void enter(
            @RequestBody Map<String, Object> data,
            @RequestAttribute TokenVO tokenVO) {
    	log.debug("tokenVO5 = {}", tokenVO);
        int chatNo = (Integer) data.get("chatNo");
        String accountId = tokenVO.getLoginId();

        chatDao.enter(chatNo, accountId);
    }

    // 참여 여부 확인
    @PostMapping("/check")
    @Transactional
    public Map<String, Boolean> check(
            @RequestBody Map<String, Object> data,
            @RequestAttribute TokenVO tokenVO) {
    	log.debug("tokenVO6 = {}", tokenVO);
        int chatNo = (Integer) data.get("chatNo");
        String accountId = tokenVO.getLoginId();

        return Map.of(
                "result",
                chatDao.check(chatNo, accountId)
        );
    }
}
