package com.kh.maproot.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor @Builder
public class ShareLinkResponseVO {
	
	private boolean valid;
	private int targetScheduleNo;

}
