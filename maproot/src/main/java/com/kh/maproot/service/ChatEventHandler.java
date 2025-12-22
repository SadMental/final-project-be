package com.kh.maproot.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import com.kh.maproot.dao.ChatDao;
import com.kh.maproot.vo.TokenVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ChatEventHandler {
	
	private final ChatService chatService;
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;
	@Autowired
	private ChatDao chatDao;
	@Autowired
	private TokenService tokenService;
	
	private Map<String, String> sessions = 
			Collections.synchronizedMap(new HashMap<>());
	private Map<String, String> nickName = 
			Collections.synchronizedMap(new HashMap<>());

	ChatEventHandler(ChatService chatService) {
        this.chatService = chatService;
    }
	
	@EventListener
	public void enter(SessionConnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		
		String chatNoStr = accessor.getFirstNativeHeader("chatNo");
		String accessToken = accessor.getFirstNativeHeader("accessToken");
		String refreshToken = accessor.getFirstNativeHeader("refreshToken");
		
		if(chatNoStr == null || accessToken == null || refreshToken == null) return;
		
		TokenVO tokenVO = tokenService.parse(accessToken);
		
		sessions.put(accessor.getSessionId(), chatNoStr);
		nickName.put(accessor.getSessionId(), tokenVO.getLoginId());
		
		log.debug("세션 연결 저장: ID={}, ChatNo={}, User={}", 
                accessor.getSessionId(), chatNoStr, tokenVO.getLoginId());
	}
	
//	@EventListener
//	public void leave(SessionConnectedEvent event) {
//		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//		
//		String chatNoStr = sessions.remove(accessor.getSessionId());
//		String loginId = nickName.remove(accessor.getSessionId());
//		
//		if(chatNoStr == null) return;
//		
//		chatService.sendChatEnd(Integer.parseInt(chatNoStr));
//		
//		log.debug("세션 해제 처리: ID={}, ChatNo={}, User={}", 
//                accessor.getSessionId(), chatNoStr, loginId);
//	}
	@EventListener
	public void leave(SessionConnectEvent event) {
	    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

	    String chatNoStr = sessions.remove(accessor.getSessionId());
	    String loginId = nickName.remove(accessor.getSessionId());

	    if(chatNoStr == null) return;

	    chatService.sendChatEnd(Long.parseLong(chatNoStr));

	    log.debug("세션 해제 처리: ID={}, ChatNo={}, User={}",
	        accessor.getSessionId(), chatNoStr, loginId);
	}
}
