package com.kh.maproot.restcontroller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.maproot.dao.AttachmentDao;
import com.kh.maproot.dto.AttachmentDto;
import com.kh.maproot.error.TargetNotfoundException;
import com.kh.maproot.service.AttachmentService;

@CrossOrigin
@RestController
@RequestMapping("/attachment")
public class AttachmentRestController {
	// 사진을 화면에 불러오기 위한 REST 컨트롤러

	@Autowired
	private AttachmentDao attachmentDao;
	@Autowired
	private AttachmentService attachmentService;
	
	@GetMapping("/")
	public ResponseEntity<ByteArrayResource> download(@RequestParam long attachmentNo) throws IOException{
		// 1. DB에서 정보 조회
		AttachmentDto attachmentDto = attachmentDao.selectOne(attachmentNo);
		if(attachmentDto == null) throw new TargetNotfoundException("존재하지 않는 파일");
		
		// 2. 파일 불러오기
		ByteArrayResource resource = attachmentService.load(attachmentNo);
		
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_ENCODING, StandardCharsets.UTF_8.name())
				.header(HttpHeaders.CONTENT_TYPE, attachmentDto.getAttachmentType())
				.contentLength(attachmentDto.getAttachmentSize())
				.header(HttpHeaders.CONTENT_DISPOSITION, 
						ContentDisposition.attachment()
						.filename(attachmentDto.getAttachmentName(), StandardCharsets.UTF_8)
						.build().toString())
				.body(resource);
	}
}
