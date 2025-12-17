package com.kh.maproot.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(exclude = {"accountPw"})
public class AccountDto {

	@NotBlank
	@Pattern(regexp = "^[a-z][a-z0-9]{4,19}$")
	private String accountId;
	
	@NotBlank
	@Pattern(regexp = "^(?=.*?[a-z]+)(?=.*?[0-9]+)(?=.*?[!@#$]+)[A-Za-z0-9!@#$]{8,16}$")
	private String accountPw;
	
	@NotBlank
	@Pattern(regexp = "^[가-힣0-9]{2,10}$")
	private String accountNickname;
	
	@Pattern(
			regexp = "^(19[0-9]{2}|20[0-9]{2})-((02-(0[1-9]|1[0-9]|2[0-9]))|((0[469]|11)-(0[1-9]|1[0-9]|2[0-9]|30))|((0[13578]|1[02])-(0[1-9]|1[0-9]|2[0-9]|3[01])))$")
	private String accountBirth;
	
	@NotBlank
	@Pattern(regexp = "^010[1-9][0-9]{7}$")
	private String accountContact;
	
	@Email
	private String accountEmail;
	private String accountGender;
	private String accountLevel;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private LocalDateTime accountJoin; //회원 가입시간
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private LocalDateTime accountLogin; // 회원 로그인 시간(가장 최근)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private LocalDateTime accountChange; // 회원 정보 수정 시간
	private Integer accountMaxSchedule; // 일정 등록 가능한 최대갯수 
	
}
