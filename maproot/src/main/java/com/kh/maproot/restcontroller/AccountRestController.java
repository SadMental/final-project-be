package com.kh.maproot.restcontroller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.maproot.dao.AccountDao;
import com.kh.maproot.dao.AccountLikeDao;
import com.kh.maproot.dao.RefreshTokenDao;
import com.kh.maproot.dto.AccountDto;
import com.kh.maproot.error.TargetNotfoundException;
import com.kh.maproot.error.UnauthorizationException;
import com.kh.maproot.schedule.vo.ScheduleListResponseVO;
import com.kh.maproot.service.AccountService;
import com.kh.maproot.service.AttachmentService;
import com.kh.maproot.service.TokenService;
import com.kh.maproot.vo.AccountLoginResponseVO;
import com.kh.maproot.vo.AccountRefreshVO;
import com.kh.maproot.vo.TokenVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "회원 관리 컨트롤러")
@CrossOrigin @Slf4j
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
	@Autowired
	private RefreshTokenDao refreshTokenDao;
	
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
	public void insert(@ModelAttribute AccountDto accountDto,
			@RequestParam(required = false) MultipartFile attach) throws IllegalStateException, IOException {
		accountService.join(accountDto, attach);
	}
	// 아이디 중복검사
	@GetMapping("/accountId/{accountId}")
	public boolean checkAccountId(@PathVariable String accountId) {
		int count = accountDao.countByAccountId(accountId);
		return count == 0;
	}
	// 닉네임 중복검사
	@GetMapping("/accountNickname/{accountNickname}")
	public boolean checkAccountNickname(@PathVariable String accountNickname) {
		int count = accountDao.countByAccountNickname(accountNickname);
		return count == 0;
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
		
		// 로그인 시간 업데이트
		accountDao.updateLoginTime(accountDto.getAccountId());
		
		// 로그인 성공
		return AccountLoginResponseVO.builder()
				.loginId(findDto.getAccountId())//아이디
				.loginLevel(findDto.getAccountLevel())//등급
				.accessToken(tokenService.generateAccessToken(findDto))//액세스토큰
				.refreshToken(tokenService.generateRefreshToken(findDto))//갱신토큰
			.build();
	}
	
	// 토큰 갱신
	@PostMapping("/refresh")
	public AccountLoginResponseVO refresh(@RequestBody AccountRefreshVO accountRefreshVO) {
		String refreshToken = accountRefreshVO.getRefreshToken();
		if(refreshToken == null || refreshToken.isEmpty())
			throw new UnauthorizationException();
		
		TokenVO tokenVO = tokenService.parse(refreshToken);
		
		boolean isValid = tokenService.checkRefreshToken(tokenVO, refreshToken);
		if(!isValid) throw new TargetNotfoundException();
		
		// 재생성 후 반환
		return AccountLoginResponseVO.builder()
					.loginId(tokenVO.getLoginId())
					.loginLevel(tokenVO.getLoginLevel())
					.accessToken(tokenService.generateAccessToken(tokenVO))
				.build();
	}
	// 로그아웃
	@DeleteMapping("/logout")
	public void logout(@RequestHeader("Authorization") String bearerToken) {
		TokenVO tokenVO = tokenService.parse(bearerToken);
		refreshTokenDao.deleteByTarget(tokenVO.getLoginId());
	}
	
	//아이디찾기
	@Operation(summary = "아이디 찾기", description = "휴대폰 번호 또는 이메일로 아이디를 조회합니다.")
	@PostMapping("/findId")
	public String findId(
			@RequestParam(required = false) String accountContact, 
			@RequestParam(required = false) String accountEmail) {
		String result = accountDao.findAccountId(accountContact, accountEmail);
		if(result == null) throw new TargetNotfoundException("일치하는 회원 정보가 없습니다");
		return result;
	}
	
	// 비밀번호 변경
	@PatchMapping("/changePw")
	public void changePw(@RequestBody AccountDto accountDto) {
		AccountDto findDto = accountDao.selectOne(accountDto.getAccountId());
		if(findDto == null) throw new TargetNotfoundException("존재하지 않는 회원");
		// 비밀번호 암호화
		String newPw = passwordEncoder.encode(accountDto.getAccountPw());
		accountDao.updatePw(newPw, findDto.getAccountId());
	}
	
	// 마이페이지
	@GetMapping("/mypage")
	public AccountDto mypage(@RequestAttribute TokenVO tokenVO) {
		AccountDto accountDto = accountDao.selectOne(tokenVO.getLoginId());
		if(accountDto == null) throw new TargetNotfoundException();
		
		Long attachmentNo = accountDao.findAttach(accountDto.getAccountId());
		
		accountDto.setAttachmentNo(attachmentNo);
		
		return accountDto;
	}
	// 부분수정(연락처, 비밀번호 제외)
	@PatchMapping("/edit")
	public void update(
			@RequestBody AccountDto accountDto,
			@RequestAttribute TokenVO tokenVO) {
		String loginId = tokenVO.getLoginId();
		AccountDto originDto = accountDao.selectOne(loginId);
		if(originDto == null) throw new TargetNotfoundException("존재하지 않는 파일");
		
		accountDto.setAccountId(loginId);
		accountDao.update(accountDto);
	}
	
	// 회원 탈퇴 (비밀번호 확인 후에 삭제)
	@PostMapping("/withdraw")
	public void withdraw(
			@RequestAttribute TokenVO tokenVO,
			@RequestBody AccountDto accountDto) {
		String loginId = tokenVO.getLoginId();
		
		// 프론트에서 보내는 평문 비밀번호
		String rawPassword = accountDto.getAccountPw();
		
		accountService.drop(loginId, rawPassword);
	}
	
	// 회원의 일정 좋아요 토글
	@PostMapping("/scheduleLike/{scheduleNo}")
	public int toggleScheduleLike( // void -> int 로 변경
	        @RequestAttribute TokenVO tokenVO,
	        @PathVariable Long scheduleNo) {
	    String loginId = tokenVO.getLoginId();
	    
	    // 서비스 메서드가 토글 수행 후 '전체 좋아요 개수'를 반환하도록 호출
	    return accountService.toggleSchedulelike(loginId, scheduleNo);
	@PostMapping("/dropAdmin")
	public void dropAdmin(
			@RequestAttribute TokenVO tokenVO,
			@RequestBody AccountDto accountDto) {
		accountService.dropAdmin(accountDto.getAccountId(), tokenVO);
	}
}
