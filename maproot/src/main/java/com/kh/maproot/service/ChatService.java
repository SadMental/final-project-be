package com.kh.maproot.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.maproot.dao.ChatDao;
import com.kh.maproot.dao.MessageDao;
import com.kh.maproot.dto.ChatDto;
import com.kh.maproot.dto.MessageDto;
import com.kh.maproot.error.TargetNotfoundException;
import com.kh.maproot.vo.MemberRequestVO;
import com.kh.maproot.vo.TokenVO;

@Service
public class ChatService {
	@Autowired
	private MessageDao messageDao;
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;
	@Autowired
	private ChatDao chatDao;
	
	@Transactional
	public void sendWaiting(long chatNo) {
		MessageDto messageDto = messageDao.insert(MessageDto.builder()
				.messageType("system")
				.messageContent("상담사와 연결하는 중 입니다")
				.messageChat(chatNo)
			.build()
		);
		
//		simpMessagingTemplate.convertAndSend(
//			"/public/message/" + chatNo + "/system", messageDto
//		);
		simpMessagingTemplate.convertAndSend(
				"/public/message/" + chatNo, messageDto
			);
	}
	
//	@Transactional
//	public void sendAgentAssigned(long chatNo, String accountId) {
//		MessageDto messageDto = messageDao.insert(MessageDto.builder()
//				.messageType("system")
//				.messageContent("상담사와 연결되었습니다")
//				.messageChat(chatNo)
//			.build()
//		);
//		
//		simpMessagingTemplate.convertAndSend(
//			"/public/message/" + chatNo + "/system", messageDto
//		);
//	}
	@Transactional
	public void sendAgentAssigned(ChatDto chatDto, String accountId) {
		//1. DB 상태  업데이트(Active) 
		chatDto.setChatId(accountId);
		chatDto.setChatLevel("상담사");
		chatDto.setChatStatus("ACTIVE");
		
		chatDao.changeStatus(chatDto);
		
		// 상담사도 참여자 명단에 추가
		chatDao.enter(chatDto.getChatNo(), accountId);
		
		//2. 기존 메세지 전송 
		long chatNo = chatDto.getChatNo();
		
		MessageDto messageDto = MessageDto.builder()
					.messageType("system")
					.messageContent("상담사와 연결되었습니다")
					.messageChat(chatNo)
				.build();
		
		//DB에 저장
		messageDao.insert(messageDto);
		
//		simpMessagingTemplate.convertAndSend("/public/message/" + chatNo + "/system", messageDto);
		simpMessagingTemplate.convertAndSend("/public/message/" + chatNo, messageDto);
	}
	
	@Transactional
	public void sendChatEnd(long chatNo) {
		MessageDto messageDto = messageDao.insert(MessageDto.builder()
				.messageType("system")
				.messageContent("상담사와의 연결이 종료되었습니다.")
				.messageChat(chatNo)
			.build()
		);
		
		simpMessagingTemplate.convertAndSend(
//			"/public/message/" + chatNo + "/system", messageDto
				"/public/message/" + chatNo, messageDto
		);
	}
	
//	@Transactional
//	public void sendChat(long chatNo, MemberRequestVO requestVO, TokenVO tokenVO) {
//		MessageDto messageDto = messageDao.insert(
//			MessageDto.builder()
//				.messageChat(chatNo)
//				.messageType("TALK")//프론트엔드에서 보내는 messageType 불일치 (chat--->TALK 수정)
//				.messageContent(requestVO.getContent())
//				.messageSender(tokenVO.getLoginId())
//				.messageTime(LocalDateTime.now()) //시간 추가
//			.build()
//		);
//		simpMessagingTemplate.convertAndSend(
//			    "/public/message/" + chatNo, messageDto 
//			);
//	}
	@Transactional
	public void sendChat(long chatNo, MemberRequestVO requestVO, TokenVO tokenVO) {
	    // 로그 추가: 실제 어떤 데이터가 들어오는지 확인
	    System.out.println("전송 시도 - 방번호: " + chatNo + ", 보낸이: " + tokenVO.getLoginId());

	    MessageDto messageDto = MessageDto.builder()
	            .messageChat(chatNo)
	            .messageType("TALK") 
	            .messageContent(requestVO.getContent())
	            .messageSender(tokenVO.getLoginId())
	            .messageTime(LocalDateTime.now())
	        .build();

	    messageDao.insert(messageDto);
	    
	    simpMessagingTemplate.convertAndSend("/public/message/" + chatNo, messageDto);
	}
	
	@Transactional
	public void sendWarning(long chatNo, MemberRequestVO requestVO, TokenVO tokenVO) {
		MessageDto messageDto = messageDao.insert(MessageDto.builder()
					.messageChat(chatNo)
					.messageType("warning")
					.messageContent("욕설은 사용하실 수 없습니다")
				.build());
		
		simpMessagingTemplate.convertAndSend(
//				"/private/message/"+chatNo+"/warning/"+tokenVO.getLoginId(), messageDto
				"/private/message/"+chatNo+ tokenVO.getLoginId(), messageDto
		);
	}
	
	@Transactional
	public void assignAgent(long chatNo, String loginId, String loginLevel) {

	    ChatDto chat = chatDao.selectOne(chatNo);

	    // 이미 배정된 상담사가 본인인 경우 -> 바로 종료
	    if ("ACTIVE".equals(chat.getChatStatus()) && loginId.equals(chat.getChatId())) {
	        return; 
	    }

	    // 다른 사람이 이미 상담 중인 경우
	    if (!"WAITING".equals(chat.getChatStatus())) {
	        throw new IllegalStateException("이미 다른 상담사가 상담 중입니다.");
	    }

	    // 권한 체크
	    if (!"상담사".equals(loginLevel)) {
	        throw new IllegalStateException("상담사만 배정할 수 있습니다");
	    }

	    ChatDto updateDto = new ChatDto();
	    updateDto.setChatNo(chatNo);
	    updateDto.setChatStatus("ACTIVE");
	    updateDto.setChatId(loginId);
	    updateDto.setChatLevel("상담사");

	    chatDao.changeStatus(updateDto);

	    // party 중복 방지
	    if (!chatDao.check(chatNo, loginId)) {
	        chatDao.enter(chatNo, loginId);
	    }
	}

	@Transactional
	public void closeChat(long chatNo, String loginId, String loginLevel) {

	    ChatDto chatDto = chatDao.selectOne(chatNo);

	    if (!"ACTIVE".equals(chatDto.getChatStatus())) {
	        throw new IllegalStateException("진행 중인 상담만 종료할 수 있습니다");
	    }

	    if (!"상담사".equals(loginLevel)
	        && !loginId.equals(chatDto.getChatId())) {
	        throw new IllegalStateException("상담 종료 권한이 없습니다");
	    }

	    // chat_id / chat_level 반드시 null 처리
	    ChatDto updateDto = new ChatDto();
	    updateDto.setChatNo(chatNo);
	    updateDto.setChatStatus("CLOSED");
	    updateDto.setChatId(null);
	    updateDto.setChatLevel(null);

	    chatDao.changeStatus(updateDto);

	    // party 정리
	    chatDao.leave(chatNo, chatDto.getChatId());

	    // 시스템 메시지
	    sendChatEnd(chatNo);
	}

}
