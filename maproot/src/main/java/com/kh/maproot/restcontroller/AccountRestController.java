package com.kh.maproot.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.maproot.dao.AccountDao;
import com.kh.maproot.dto.AccountDto;
import com.kh.maproot.error.TargetNotfoundException;
import com.kh.maproot.service.AccountService;
import com.kh.maproot.service.CertService;
import com.kh.maproot.service.TokenService;
import com.kh.maproot.vo.AccountLoginResponseVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "회원 관리 컨트롤러")
@CrossOrigin
@RestController
@RequestMapping("/account")
public class AccountRestController {

	@Autowired
	private AccountDao accountDao;
	@Autowired
	private AccountService accountService;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Operation(
			summary = "신규 회원 가입", // [1] 짧은 제목
			description = "사용자가 입력한 정보를 바탕으로 새로운 회원을 등록합니다.<br>"
					+ "비밀번호는 암호화되어 저장되며, <strong>아이디, 닉네임, 이메일, 연락처는 중복될 수 없습니다.</strong>", // [2] 상세 설명 (HTML 태그 사용 가능)
			responses = {
				@ApiResponse(
					responseCode = "200", 
					description = "회원 가입 성공",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class)) // [3] 응답 본문이 없음을 명시
				),
				@ApiResponse(
					responseCode = "400", 
					description = "잘못된 요청 (필수 입력값 누락, 유효성 검사 실패)",
					content = @Content
				),
				@ApiResponse(
					responseCode = "409", 
					description = "중복된 데이터 존재 (아이디, 닉네임, 연락처 등)", // [4] 409 Conflict 추가 추천
					content = @Content
				),
				@ApiResponse(
					responseCode = "500", 
					description = "서버 내부 에러 (DB 연결 실패 등)",
					content = @Content
				)
			}
		)
	// 회원가입
	@PostMapping("/join")
	public void insert(AccountDto accountDto) {
		accountService.join(accountDto);
	}
	
	@Operation(
			summary = "로그인 (토큰 발급)", 
			description = "회원의 아이디와 비밀번호를 검증하여 <strong>Access Token</strong>과 <strong>Refresh Token</strong>을 발급합니다.<br>"
					+ "로그인 성공 시 반환되는 <code>accessToken</code>을 복사하여, 우측 상단 <strong>[Authorize]</strong> 버튼에 등록하면 인증된 상태로 다른 API를 테스트할 수 있습니다.",
			responses = {
				@ApiResponse(
					responseCode = "200", 
					description = "로그인 성공",
					content = @Content(
						mediaType = "application/json", 
						schema = @Schema(implementation = AccountLoginResponseVO.class)
					)
				),
				@ApiResponse(
					responseCode = "404", 
					description = "로그인 실패 (아이디가 없거나 비밀번호가 일치하지 않음)",
					content = @Content(
						mediaType = "text/plain",
						examples = @ExampleObject(value = "로그인 정보 오류")
					)
				),
				@ApiResponse(
					responseCode = "500", 
					description = "서버 내부 오류",
					content = @Content
				)
			}
		)
	@PostMapping("/login")
	public AccountLoginResponseVO login(@RequestBody AccountDto accountDto) {
		AccountDto findDto = accountDao.selectOne(accountDto.getAccountId());
		if(findDto == null) throw new TargetNotfoundException("로그인 정보 오류");
		// 비밀번호 검사
		boolean isValid = passwordEncoder.matches(accountDto.getAccountPw(), findDto.getAccountPw());
		if(!isValid) throw new TargetNotfoundException("로그인 정보 오류");
		
		// 로그인 성공
		return AccountLoginResponseVO.builder()
				.loginId(findDto.getAccountId())//아이디
				.loginLevel(findDto.getAccountLevel())//등급
				.accessToken(tokenService.generateAccessToken(findDto))//액세스토큰
				.refreshToken(tokenService.generateRefreshToken(findDto))//갱신토큰
			.build();
	}
	
	
}
