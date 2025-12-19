package com.kh.maproot.dto;

import java.security.Timestamp;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class GuestDto {
	
	 private int guestNo;           // PK
	    private String guestNickname;  // 닉네임
	    private String guestKey;       // 쿠키/로컬스토리지 저장 키
	    private String guestLastIp;    // 마지막 접속 IP
	    private LocalDateTime guestWtime;  // 등록 시간
	    private LocalDateTime guestMtime;  // 변경/활동 시간

}
