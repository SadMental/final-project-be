package com.kh.maproot.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder

public class ScheduleMemberDto {

	private int scheduleMemberNo;             // PK
    private int ScheduleNo;     // 어떤 일정에 속해 있는지
    private String accountId;  // 회원일 경우
    private String scheduleMemberNickname;    // 화면 표시 이름
    private String scheduleMemberRole;        // 'owner' / 'member'
    private String scheduleMemberNotify;      // 'Y' / 'N'
    private Timestamp scheduleMemberWtime;    // 가입 시간 / 생성 시간
}
