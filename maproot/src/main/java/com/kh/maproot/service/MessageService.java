package com.kh.maproot.service;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.kh.maproot.configuration.JwtProperties;
import com.kh.maproot.vo.ChatTokenRefreshVO;
import com.kh.maproot.vo.MemberRequestVO;
import com.kh.maproot.vo.MemberResponseVO;
import com.kh.maproot.vo.SystemMessageVO;
import com.kh.maproot.vo.TokenVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class MessageService {
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private JwtProperties jwtProperties;
	@Autowired
	private ChatService chatService;
	
	@MessageMapping("/message/{chatNo}")
	public void member(@DestinationVariable long chatNo,
			Message<MemberRequestVO> message) {
		log.debug("chatNo = {}", chatNo);
		log.debug("message = {}", message);
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		String accessToken = accessor.getFirstNativeHeader("accessToken");
		String refreshToken = accessor.getFirstNativeHeader("refreshToken");
		log.debug("refreshToken = {}", refreshToken);
		log.debug("accessToken = {}", accessToken);
		if(accessToken == null || refreshToken == null) return;
		
		TokenVO tokenVO;
	    try {
	       tokenVO = tokenService.parse(accessToken);
	       
	       long ms = tokenService.getRemain(accessToken);
	       
	       if(ms >= jwtProperties.getRenewalLimit() * 60L * 1000L) {
	    	   simpMessagingTemplate.convertAndSend(
	    			   "/private/message/token/" + tokenVO.getLoginId(), 
	    			   ChatTokenRefreshVO.builder()
						.accessToken(tokenService.generateAccessToken(tokenVO))
						.refreshToken(tokenService.generateRefreshToken(tokenVO))
						.build()
	    		);
	       }
	       MemberRequestVO requestVO = message.getPayload();
	       log.debug("requestVO = {}", requestVO);
	       
	       String regex = "(.*?)(씨발|시발|병신|존나|개새끼|미친)(.*?)";
	       Matcher matcher = Pattern.compile(regex).matcher(requestVO.getContent());
	       if(matcher.find()) {
	    	   simpMessagingTemplate.convertAndSend(
	    			   "/private/member/warning/" + tokenVO.getLoginId(),
	    			   SystemMessageVO.builder()
	    			   .type("warning")
	    			   .content("부적절한 언어 사용이 감지되었습니다.")
	    			   .time(LocalDateTime.now())
	    			   .build()
	    		);
	    	   chatService.sendChat(chatNo, requestVO, tokenVO);
	    	   return;
	       }
//	       if(matcher.find()) {//찾았어? while로 작성하면 안나올때까지 찾음
//				log.debug("욕설이 감지됨");
//				chatService.sendWarning(chatNo, requestVO, tokenVO);
//				return;
//			}
	       simpMessagingTemplate.convertAndSend(
	   			"/public/message",
	   			MemberResponseVO.builder()
	   				.loginId(tokenVO.getLoginId())
	   				.content(requestVO.getContent())
	   				.time(LocalDateTime.now())
	   				.build()
	   		);
	       
	       chatService.sendChat(chatNo, requestVO, tokenVO);
	    }
	    catch (Exception e) {
			log.error("에러발생");
		}
	}
}
