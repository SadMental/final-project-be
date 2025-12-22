package com.kh.maproot.schedule.vo;

import lombok.Data;

@Data
public class SchedulePublicUpdateRequestVO {
    private Long scheduleNo;
    private boolean schedulePublic; // true=공개, false=비공개
}