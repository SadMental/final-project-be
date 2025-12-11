package com.kh.maproot.dto;

import java.security.Timestamp;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NegativeOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class ScheduleDto {
	
    private Integer scheduleNo;         // NUMBER
    private String scheduleName;    // VARCHAR2(100)
    private Integer scheduleView;       // NUMBER
    private Integer scheduleLike;       // NUMBER     
    private String scheduleOwner;   // REFERENCES account(account_id)
    private String schedulePublic;  // CHAR(1) 'Y' or 'N'
    private String scheduleState;   // VARCHAR2(20) ('open','close','progress')
    private Timestamp scheduleWtime; // TIMESTAMP
    private Timestamp scheduleEtime; // TIMESTAMP
    private LocalDate scheduleStartDate;
    private LocalDate scheduleEndDate;

}
