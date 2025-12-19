package com.kh.maproot.dto;


import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class ScheduleDto {
	
    private Long scheduleNo;         // NUMBER
    private String scheduleName;    // VARCHAR2(100)
    private Integer scheduleView;       // NUMBER
    private Integer scheduleLike;       // NUMBER     
    private String scheduleOwner;   // REFERENCES account(account_id)
    private String schedulePublic;  // CHAR(1) 'Y' or 'N'
    private String scheduleState;   // VARCHAR2(20) ('open','close','progress')
    private LocalDateTime scheduleWtime; // TIMESTAMP
    private LocalDateTime scheduleEtime; // TIMESTAMP
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduleStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduleEndDate;

}
