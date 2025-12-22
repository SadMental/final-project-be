package com.kh.maproot.schedule.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ScheduleStateResponseVO {
    private long scheduleNo;
    private String scheduleState;
    private boolean changed;
}