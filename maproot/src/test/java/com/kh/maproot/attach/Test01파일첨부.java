package com.kh.maproot.attach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import com.kh.maproot.MaprootApplication;
import com.kh.maproot.service.AttachmentService;

@SpringBootTest(classes = MaprootApplication.class)
public class Test01파일첨부 {
	
	@Autowired
	private AttachmentService attachmentService;
	
	@Test
	public void test() throws IllegalStateException, IOException {
		// 0. 경로 확인 로그 (디버깅용)
		String userHome = System.getProperty("user.home");
		System.out.println("==========================================");
		System.out.println("현재 사용자 홈 경로: " + userHome);
		
		// 1. 테스트용 가짜 파일 생성 (MockMultipartFile)
		// 파라미터 순서: 파라미터명, 파일명, 파일타입(MIME), 파일내용(byte[])
		String fileName = "my_test_photo.jpg";
		String contentType = "image/jpeg";
		byte[] content = "테스트용 이미지 데이터입니다".getBytes(StandardCharsets.UTF_8);

		MockMultipartFile attach = new MockMultipartFile(
				"attach", 		// 컨트롤러에서 받을 이름 (보통 input type="file"의 name)
				fileName, 		// 업로드할 파일 이름
				contentType, 	// 파일의 타입
				content			// 파일의 내용
		);
		
		// 2. 서비스 실행 (실제 DB insert + 실제 파일 저장 발생)
		Long attachmentNo = attachmentService.save(attach);
		System.out.println("DB 저장 완료! 시퀀스 번호: " + attachmentNo);
		
		// 3. 결과 검증 (Assertion)
		File dir = new File(userHome, "upload");
		File target = new File(dir, String.valueOf(attachmentNo));
		
		System.out.println("확인할 파일 경로: " + target.getAbsolutePath());

		// 파일이 실제로 존재하는지 검증 (실패 시 메시지 출력)
		assertTrue(target.exists(), "파일이 생성되지 않았습니다. 경로를 확인하세요: " + target.getAbsolutePath());
		
		// 파일 크기가 일치하는지 검증
		assertEquals(content.length, target.length(), "파일 내용이 손상되었거나 크기가 다릅니다.");
		
		System.out.println("테스트 성공! 파일이 정상적으로 생성되었습니다.");
		System.out.println("==========================================");
	}
}