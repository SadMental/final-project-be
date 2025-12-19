package com.kh.maproot.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class ShareLinkDto {

    // 공유 링크 번호 (PK)
    private int shareNo;

    // 공유용 키 (URL에 노출)
    private String shareKey;

    // 공유 대상 스케줄 번호
    private int targetScheduleNo;

    // 만료 시각
    private LocalDateTime expireTime;

    // 생성 시각
    private LocalDateTime wtime;
}