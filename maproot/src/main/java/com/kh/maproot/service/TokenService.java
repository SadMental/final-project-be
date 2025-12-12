package com.kh.maproot.service;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.maproot.configuration.JwtProperties;
import com.kh.maproot.dao.RefreshTokenDao;
import com.kh.maproot.dto.AccountDto;
import com.kh.maproot.dto.RefreshTokenDto;
import com.kh.maproot.error.UnauthorizationException;
import com.kh.maproot.vo.TokenVO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class TokenService {
	
	@Autowired
	private JwtProperties jwtProperties;
	
	@Autowired
	private RefreshTokenDao refreshTokenDao;
	
	// 엑세스 토큰 발급
	public String generateAccessToken(AccountDto accountDto) {
		// [1] 만료시간 설정
		Date expire = calculateExpireDate(Calendar.MINUTE, jwtProperties.getExpiration());
		
		return createJwt(accountDto, expire);
	}
	public String generateAccessToken(TokenVO tokenVO) {
		return generateAccessToken(AccountDto.builder()
				.accountId(tokenVO.getLoginId())
				.accountLevel(tokenVO.getLoginLevel())
			.build());
	}
	
	// 리프레쉬 토큰 발급
	public String generateRefreshToken(AccountDto accountDto) {
		// [1] 만료시간 설정
		Date expire = calculateExpireDate(Calendar.DATE, jwtProperties.getRefreshExpiration());
		
		// [2] 토큰 생성
		String token = createJwt(accountDto, expire);
		
		// [2] 같은 아이디로 저장된 발행 내역을 모두 삭제 
		refreshTokenDao.deleteByTarget(accountDto.getAccountId());
		
		// [3] DB 저장
		refreshTokenDao.insert(RefreshTokenDto.builder()
					.refreshTokenTarget(accountDto.getAccountId())
					.refreshTokenValue(token)
				.build());
		// [4] 토큰 반환
		return token;
	}
	public String generateRefreshToken(TokenVO tokenVO) {
		return generateRefreshToken(AccountDto.builder()
				.accountId(tokenVO.getLoginId())
				.accountLevel(tokenVO.getLoginLevel())
			.build());
	}
//	public TokenVO parse(String authorization) {
//		Claims claims = getClaims(authorization);
//		return TokenVO.builder()
//				.loginId((String)claims.get("loginId"))
//				.loginLevel((String)claims.get("loginLevel"))
//			.build();
//	}

	public TokenVO parse(String authorization) {
		if(authorization.startsWith("Bearer ") == false)
			throw new UnauthorizationException();
	
		String token = authorization.substring(7);
		
		SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getKeyStr().getBytes(StandardCharsets.UTF_8));
		Claims claims = (Claims) Jwts.parser()
				.verifyWith(key)
				.requireIssuer(jwtProperties.getIssuer())
			.build()
				.parse(token)
				.getPayload();
		
		return TokenVO.builder()
					.loginId((String)claims.get("loginId"))
					.loginLevel((String)claims.get("loginLevel"))
				.build();
	}
	
	
	//JWT 토큰의 만료까지 남은 시간을 구하는 기능
//	public long getRemain(String bearerToken) {
//		Claims claims = getClaims(bearerToken);
//		Date expire = claims.getExpiration();//만료시각 추출
//		Date now = new Date();
//		
//		return expire.getTime() - now.getTime();
//	}
	
	public long getRemain(String bearerToken) {
		if(bearerToken.startsWith("Bearer ") == false)
			throw new UnauthorizationException();
		
		String token = bearerToken.substring(7);
		
		SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getKeyStr().getBytes(StandardCharsets.UTF_8));
		Claims claims = (Claims) Jwts.parser()
				.verifyWith(key)
				.requireIssuer(jwtProperties.getIssuer())
			.build()
				.parse(token)
				.getPayload();
		
		Date expire = claims.getExpiration();
		Date now = new Date();
		return expire.getTime() - now.getTime();
	}
	
	// refreshToken이 올바른지 검사하는 메소드
	public boolean checkRefreshToken(TokenVO tokenVO, String refreshToken) {
		// 조회
		RefreshTokenDto refreshTokenDto = refreshTokenDao.selectOne(
				RefreshTokenDto.builder()
					.refreshTokenTarget(tokenVO.getLoginId())
					.refreshTokenValue(refreshToken.substring(7))
				.build()
			);
		// 결과 없음 = 인증 불가
		if(refreshTokenDto == null) return false;
		
		// 사용자의 모든 내역 삭제
		refreshTokenDao.deleteByTarget(tokenVO.getLoginId());
		
		return true;
	}
	
	// sercret-key 생성
	private SecretKey getSecretKey() {
		return Keys.hmacShaKeyFor(jwtProperties.getKeyStr().getBytes(StandardCharsets.UTF_8));
	}
	// Jwt 문자열 생성
	private String createJwt(AccountDto accountDto,Date expireDate) {
		return Jwts.builder()
				.signWith(getSecretKey())
				.expiration(expireDate)//토큰의 만료 시각 설정
				.issuedAt(new Date())//발행 시각 설정
				.issuer(jwtProperties.getIssuer())//발행자 (위변조 방지용)
				.claim("loginId", accountDto.getAccountId())//정보 추가(key,value)
				.claim("loginLevel", accountDto.getAccountLevel())//정보 추가(key,value)
				.compact();
	}
	// 토큰 파싱 및 Claims 추출 로직 (Bearer 제거 + 파싱)
	private Claims getClaims(String bearerToken) {
		if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
			throw new UnauthorizationException();
		}
		
		String token = bearerToken.substring(7);
		
		return Jwts.parser()
				.verifyWith(getSecretKey())
				.requireIssuer(jwtProperties.getIssuer())
				.build()
				.parseSignedClaims(token) // 최신 jjwt 버전에서는 parseSignedClaims 권장
				.getPayload();
	}
	//만료시간 설정
	private Date calculateExpireDate(int unit, int amount) {//시간(unit), 단위(amount)
		Calendar c = Calendar.getInstance();
		c.add(unit, amount);
		return c.getTime();
	}
}
