package com.kh.maproot.service;

import java.text.DecimalFormat;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.maproot.dao.CertDao;
import com.kh.maproot.dto.CertDto;
import com.kh.maproot.error.TargetNotfoundException;

import jakarta.annotation.PostConstruct;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;

// 휴대폰 + 전화번호 인증 통합 서비스 구현
@Service
public class CertService {

	@Autowired
	private EmailService emailService;
	@Autowired
	private CertDao certDao;
	
	private DefaultMessageService messageService;
	
	private String apiKey = "NCSNVB2CRTYQIBIR";
    private String apiSecret = "PD5BYK7EJ687SJECJY7CKINVNDOBHG0B";
    private String domain = "https://api.coolsms.co.kr";
    private String fromNumber = "01050123993";
    
    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, domain);
    }
    
    
	// 랜덤번호 생성
	private String generateCertNumber() {
		Random r = new Random();
		int number = r.nextInt(1000000);
		DecimalFormat df = new DecimalFormat("000000");
		return df.format(number);
	}
	// DB 저장 코드
	private void saveCertNumber(String target, String certNumber){
		// 기존 내역 삭제
		certDao.delete(target);
		
		CertDto certDto = CertDto.builder()
					.certTarget(target)
					.certNumber(certNumber)
				.build();
		certDao.insert(certDto);
	}
	
	// 이메일 인증번호 발송
	@Transactional
	public void sendCertEmail(String email) {
		// 인증 번호 생성
		String certNumber = generateCertNumber();
		
		// 메일 발송
		emailService.sendCertNumber(email, certNumber);
		
		saveCertNumber(email, certNumber);
	}
	
	// 휴대폰 인증번호 발송
	@Transactional
	public void sendCertPhone(String phone) {
		// 인증번호 생성
		String certNumber = generateCertNumber();
		
		// 문자 발송
		Message message = new Message();
		message.setFrom(fromNumber);
		message.setTo(phone);
		message.setText("[사이트이름] 본인확인 인증번호는 [" + certNumber + "] 입니다");
		
		try {//문자 발송 성공
			messageService.sendOne(new SingleMessageSendingRequest(message));
		}
		catch (Exception e) {//발송 실패
			throw new TargetNotfoundException();
		}
		// DB 저장
		saveCertNumber(phone, certNumber);
	}
	
	// 인증번호 통합 검사
	@Transactional
	public boolean checkCertNumber(String target, String certNumber) {
		CertDto certDto = CertDto.builder()
					.certTarget(target)
					.certNumber(certNumber)
				.build();
		// DB 일치 여부 확인
		boolean isMatched = certDao.check(certDto);
		
		if(isMatched) {//일치하면 완료된 데이터 삭제
			certDao.delete(target);
			return true;
		}
		return false;
	}
	
}
