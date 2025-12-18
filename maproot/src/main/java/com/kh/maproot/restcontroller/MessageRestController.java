package com.kh.maproot.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.maproot.dao.ChatDao;
import com.kh.maproot.dao.MessageDao;
import com.kh.maproot.dto.MessageDto;
import com.kh.maproot.error.NeedPermissionException;
import com.kh.maproot.vo.MessageVO;
import com.kh.maproot.vo.TokenVO;

@CrossOrigin
@RestController
@RequestMapping("/message")
public class MessageRestController {
	@Autowired
	private MessageDao messageDao;
	@Autowired
	private ChatDao chatDao;
	
	@GetMapping("/messageOrigin/{messageOrigin}")
	public MessageVO chatmessage(@PathVariable long messageOrigin, @RequestAttribute TokenVO tokenVO) {
		
		boolean isEnter = chatDao.check(messageOrigin, tokenVO.getLoginId());
		if(isEnter == false) throw new NeedPermissionException();
		
		List<MessageDto> message = messageDao.selectList(messageOrigin);
		
		boolean isLast = message.isEmpty() ? true : 
			messageDao.checkLast(message.get(message.size()-1));
		return MessageVO.builder()
			.message(message)
			.last(isLast)
		.build();
	}
	
	@GetMapping("/messageOrigin/{messageOrigin}/messageNo/{messageNo}")
	public MessageVO roomMessages(
			@PathVariable long messageOrigin, @PathVariable long messageNo,
			@RequestAttribute TokenVO tokenVO) {
		boolean isEnter = chatDao.check(messageOrigin, tokenVO.getLoginId());
		if(isEnter == false) throw new NeedPermissionException();
		List<MessageDto> messages = messageDao.selectList(messageOrigin, messageNo);
		boolean isLast = messages.isEmpty() ? true : 
							messageDao.checkLast(messages.get(messages.size()-1));
		return MessageVO.builder()
					.message(messages)
					.last(isLast)
				.build();
	}
}
