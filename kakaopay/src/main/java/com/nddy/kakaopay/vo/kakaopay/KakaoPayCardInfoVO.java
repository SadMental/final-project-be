package com.nddy.kakaopay.vo.kakaopay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KakaoPayCardInfoVO {
	private String kakaopayPurchaseCorp;
	private String kakaopayPurchaseCorpCode;
	private String kakaopayIssuerCorp;
	private String kakaopayIssuerCorpCode;
	private String bin;
	private String cardType;
	private String installMonth;
	private String approvedId;
	private String cardMid;
	private String interestFreeInstall;
	private String cardItemCode;
}






