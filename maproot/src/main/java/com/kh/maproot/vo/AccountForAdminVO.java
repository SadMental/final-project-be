package com.kh.maproot.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountForAdminVO {
    // 1. AccountDto의 주요 필드 (직접 선언하거나 상속)
    private String accountId;
    private String accountNickname;
    private String accountEmail;
    private String accountContact;
    private String accountLevel;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime accountJoin;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime accountLogin;

    // 2. 관리용 추가 필드
    private int accountMaxSchedule; // 최대 허용 개수
    private int scheduleCount;      // 현재 생성한 개수 (쿼리의 AS 이름과 일치)
    
}