package com.kh.maproot.service;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.kh.maproot.configuration.JwtProperties;
import com.kh.maproot.vo.ChatTokenRefreshVO;
import com.kh.maproot.vo.MemberRequestVO;
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
	
//	@MessageMapping("/message/{chatNo}")
//	public void member(@DestinationVariable long chatNo,
//			Message<MemberRequestVO> message) {
//		log.debug("chatNo = {}", chatNo);
//		log.debug("message = {}", message);
//		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//		String accessToken = accessor.getFirstNativeHeader("accessToken");
//		String refreshToken = accessor.getFirstNativeHeader("refreshToken");
//		log.debug("refreshToken = {}", refreshToken);
//		log.debug("accessToken = {}", accessToken);
//		if(accessToken == null || refreshToken == null) return;
//		
//		TokenVO tokenVO;
//	    try {
//	       tokenVO = tokenService.parse(accessToken);
//	       
//	       long ms = tokenService.getRemain(accessToken);
//	       
//	       if(ms >= jwtProperties.getRenewalLimit() * 60L * 1000L) {
//	    	   simpMessagingTemplate.convertAndSend(
//	    			   "/private/message/token/" + tokenVO.getLoginId(), 
//	    			   ChatTokenRefreshVO.builder()
//						.accessToken(tokenService.generateAccessToken(tokenVO))
//						.refreshToken(tokenService.generateRefreshToken(tokenVO))
//						.build()
//	    		);
//	       }
//	       MemberRequestVO requestVO = message.getPayload();
//	       log.debug("requestVO = {}", requestVO);
//	       
//	       String regex = "(.*?)(씨발|시발|병신|존나|개새끼|미친)(.*?)";
//	       Matcher matcher = Pattern.compile(regex).matcher(requestVO.getContent());
//	       if(matcher.find()) {
//	    	   simpMessagingTemplate.convertAndSend(
//	    			   "/private/member/warning/" + tokenVO.getLoginId(),
//	    			   SystemMessageVO.builder()
//	    			   .type("warning")
//	    			   .content("부적절한 언어 사용이 감지되었습니다.")
//	    			   .time(LocalDateTime.now())
//	    			   .build()
//	    		);
//	    	   chatService.sendChat(chatNo, requestVO, tokenVO);
//	    	   return;
//	       }
////	       if(matcher.find()) {//찾았어? while로 작성하면 안나올때까지 찾음
////				log.debug("욕설이 감지됨");
////				chatService.sendWarning(chatNo, requestVO, tokenVO);
////				return;
////			}
//	       simpMessagingTemplate.convertAndSend(
//	   			"/public/message",
//	   			MemberResponseVO.builder()
//	   				.loginId(tokenVO.getLoginId())
//	   				.content(requestVO.getContent())
//	   				.time(LocalDateTime.now())
//	   				.build()
//	   		);
//	       
//	       chatService.sendChat(chatNo, requestVO, tokenVO);
//	    }
//	    catch (Exception e) {
//			log.error("에러발생");
//		}
//	}
	
	// 태훈(중복 제거 후 경로 수정) / (+추가) MessageService보다는 MessageController가 좋아보임
	@MessageMapping("/message/{chatNo}")
	public void member(
			@DestinationVariable long chatNo,
			@Payload MemberRequestVO requestVO,
			@Header(name="accessToken", required=false) String accessToken,
			@Header(name = "refreshToken", required = false) String refreshToken){
		log.debug("chatNo = {}", chatNo);
		log.debug("requestVO = {}", requestVO);
		
		if(accessToken == null || refreshToken == null) {
			log.warn("토큰이 없습니다");
			return;
		}
		
		try {
			TokenVO tokenVO = tokenService.parse(accessToken);
			log.debug("현재 로그인한 ID: {}", tokenVO.getLoginId());
			
			// 1. 토큰 갱신
			long ms = tokenService.getRemain(accessToken);
			if(ms <= jwtProperties.getRenewalLimit() * 60L * 1000L) { // 조건부 등호 방향 확인 (남은 시간이 리미트보다 작으면 갱신)
				simpMessagingTemplate.convertAndSend(
					"/private/message/" + chatNo + "/token/" + tokenVO.getLoginId(), // 경로 명확화
					ChatTokenRefreshVO.builder()
						.accessToken(tokenService.generateAccessToken(tokenVO))
						.refreshToken(tokenService.generateRefreshToken(tokenVO))
						.build()
				);
			}
			// 2. 욕설 필터링
			String regex = "(.*?)(씨발|시발|병신|존나|개새끼|미친)(.*?)";
			
			if (requestVO.getContent() == null) {
			    log.warn("메시지 내용(content)이 null입니다. 처리를 중단합니다.");
			    return; 
			}
			
			Matcher matcher = Pattern.compile(regex).matcher(requestVO.getContent());
			
			if(matcher.find()) {
				// 경고 메시지 전송
				simpMessagingTemplate.convertAndSend(
					"/private/message/" + chatNo + "/warning/" + tokenVO.getLoginId(),
					SystemMessageVO.builder()
						.type("warning")
						.content("부적절한 언어 사용이 감지되었습니다.")
						.time(LocalDateTime.now())
						.build()
				);
				return; 
			}
			
			//3. 채팅 전송
			chatService.sendChat(chatNo, requestVO, tokenVO);
			
//			simpMessagingTemplate.convertAndSend(
//			    "/public/message/" + chatNo, 
//			    requestVO
//			);
		}
		catch (Exception e) {
			e.printStackTrace();
			log.error("메세지 처리 중 에러 발생 : {}", e.getMessage());
		}
		
	}
}

