package com.kh.maproot.attach;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.kh.maproot.MaprootApplication;
import com.kh.maproot.dao.AccountDao;
import com.kh.maproot.dto.AccountDto;
import com.kh.maproot.service.AccountService;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = MaprootApplication.class) @Slf4j
public class Test02회원가입시사진첨부 {
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private AccountDao accountDao; // 결과 검증을 위해 Dao 주입
	
	@Test
//	@Transactional // 테스트가 끝나면 DB에 저장된 회원정보는 자동으로 롤백(삭제)됩니다.
	public void test() throws IllegalStateException, IOException {
		// 1. 중복되지 않는 임의의 회원 정보 생성 (테스트용)
		String testId = "testuser12";
		String testNick = "테스트유저12";
		// 전화번호 형식 (010 + 8자리 난수)
		String testContact = "010" + String.valueOf((int)(Math.random() * 89999999) + 10000000);
		
		AccountDto accountDto = new AccountDto();
		accountDto.setAccountId(testId);
		accountDto.setAccountPw("test1234!");
		accountDto.setAccountNickname(testNick);
		accountDto.setAccountContact(testContact);
		// [주의] AccountDto의 다른 필수 필드(이메일, 생년월일 등)가 있다면 여기서 꼭 채워주세요!
		accountDto.setAccountEmail(testId + "@test.com");
		accountDto.setAccountBirth("2000-01-01"); 
		accountDto.setAccountLevel("일반회원");

		// 2. 가짜 프로필 이미지 파일 생성
		String fileName = "my_profile.jpg";
		String contentType = "image/jpeg";
		byte[] content = "fake image content".getBytes(StandardCharsets.UTF_8);
		
		MockMultipartFile attach = new MockMultipartFile(
				"attach", fileName, contentType, content
		);
		
		// 3. 서비스 실행 (회원가입 + 파일업로드 + 연결 로직 수행)
		System.out.println("==========================================");
		System.out.println("회원가입 시도 ID: " + testId);
		
		accountService.join(accountDto, attach);
		
		System.out.println("서비스 실행 완료");

		// 4. 검증 (Assertion)
		// 4-1. DB 연결 확인: 해당 아이디로 저장된 파일 번호 조회
		Long attachmentNo = accountDao.findAttach(testId);
		System.out.println("DB에 저장된 프로필 사진 번호: " + attachmentNo);
		
		assertNotNull(attachmentNo, "실패: 프로필 사진이 DB에 연결되지 않았습니다.");
		
		// 4-2. 실제 파일 생성 확인
		File dir = new File(System.getProperty("user.home"), "upload");
		File target = new File(dir, String.valueOf(attachmentNo));
//		
		System.out.println("확인할 파일 경로: " + target.getAbsolutePath());
		assertTrue(target.exists(), "실패: 실제 파일이 디스크에 생성되지 않았습니다.");
//		
//		// (선택) 테스트로 생성된 파일은 @Transactional로 지워지지 않으므로 직접 삭제
//		if(target.exists()) {
//			target.delete();
//			System.out.println("테스트용 파일 삭제 완료");
//		}
		System.out.println("테스트 성공!");
		System.out.println("==========================================");
	}
}