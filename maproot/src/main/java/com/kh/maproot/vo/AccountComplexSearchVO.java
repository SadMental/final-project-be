package com.kh.maproot.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown =  true)
public class AccountComplexSearchVO {
	// 관리자 검사
	private List<String> accountLevelList;
	
	private String accountId;
	private String accountNickname;
	private String accountEmail;
	private String accountContact;
	private String accountBirth;
	private String beginAccountJoin, endAccountJoin;
	private Integer minSchedule, maxSchedule;
	private Integer minScheduleCount, maxScheduleCount;
	private Integer page;
	
}
