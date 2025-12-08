package com.nddy.kakaopay.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.nddy.kakaopay.configuration.KakaoPayProperties;
import com.nddy.kakaopay.vo.kakaopay.KakaoPayApproveRequestVO;
import com.nddy.kakaopay.vo.kakaopay.KakaoPayApproveResponseVO;
import com.nddy.kakaopay.vo.kakaopay.KakaoPayCancelRequestVO;
import com.nddy.kakaopay.vo.kakaopay.KakaoPayCancelResponseVO;
import com.nddy.kakaopay.vo.kakaopay.KakaoPayOrderRequestVO;
import com.nddy.kakaopay.vo.kakaopay.KakaoPayOrderResponseVO;
import com.nddy.kakaopay.vo.kakaopay.KakaoPayReadyRequestVO;
import com.nddy.kakaopay.vo.kakaopay.KakaoPayReadyResponseVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KakaoPayService {

	@Autowired
	private WebClient webClient;
	@Autowired
	private KakaoPayProperties kakaoPayProperties;

	public KakaoPayReadyResponseVO ready(KakaoPayReadyRequestVO requestVO) {
		Map<String, String> body = new HashMap<>();
		body.put("cid", kakaoPayProperties.getCid());
		body.put("partner_order_id", requestVO.getPartnerOrderId());
		body.put("partner_user_id", requestVO.getPartnerUserId());
		body.put("item_name", requestVO.getItemName());
		body.put("quantity", "1");
		body.put("total_amount", String.valueOf(requestVO.getTotalAmount()));
		body.put("tax_free_amount", "0");

		String currentPath = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
		body.put("approval_url", currentPath + "/success/" + requestVO.getPartnerOrderId());
		body.put("cancel_url", currentPath + "/cancel/" + requestVO.getPartnerOrderId());
		body.put("fail_url", currentPath + "/fail/" + requestVO.getPartnerOrderId());

		KakaoPayReadyResponseVO response = webClient.post()
				.uri("/online/v1/payment/ready")
				.bodyValue(body)
				.retrieve()
				.bodyToMono(KakaoPayReadyResponseVO.class)
				.block();
		
		return response;
	}

	public KakaoPayApproveResponseVO approve(KakaoPayApproveRequestVO requestVO) {
		Map<String, String> body = new HashMap<>();
		body.put("cid", kakaoPayProperties.getCid());
		body.put("partner_order_id", requestVO.getPartnerOrderId());
		body.put("partner_user_id", requestVO.getPartnerUserId());
		body.put("tid", requestVO.getTid());
		body.put("pg_token", requestVO.getPgToken());
		
		KakaoPayApproveResponseVO response = webClient.post()
				.uri("/online/v1/payment/approve")
				.bodyValue(body)
				.retrieve()
				.bodyToMono(KakaoPayApproveResponseVO.class)
				.block();
		
		return response;
	}

	public KakaoPayOrderResponseVO order(KakaoPayOrderRequestVO requestVO) {
		Map<String, String> body = new HashMap<>();
		body.put("cid", kakaoPayProperties.getCid());
		body.put("tid", requestVO.getTid());

		KakaoPayOrderResponseVO responseVO = webClient.post()
				.uri("/online/v1/payment/order")
				.bodyValue(body)
				.retrieve()
				.bodyToMono(KakaoPayOrderResponseVO.class)
				.block();

		return responseVO;
	}

	public KakaoPayCancelResponseVO cancel(KakaoPayCancelRequestVO requestVO) {
		Map<String, String> body = new HashMap<>();
		body.put("cid", kakaoPayProperties.getCid());
		body.put("tid", requestVO.getTid());
		body.put("cancel_amount", String.valueOf(requestVO.getCancelAmount()));
		body.put("cancel_tax_free_amount", "0");

		KakaoPayCancelResponseVO responseVO = webClient.post()
				.uri("/online/v1/payment/cancel")
				.bodyValue(body)
				.retrieve()
				.bodyToMono(KakaoPayCancelResponseVO.class)
				.block();

		return responseVO;
	}
}
