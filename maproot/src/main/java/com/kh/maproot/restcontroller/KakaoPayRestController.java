package com.kh.maproot.restcontroller;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.maproot.dao.ShopDao;
import com.kh.maproot.dao.PaymentDao;
import com.kh.maproot.dao.PaymentDetailDao;
import com.kh.maproot.dto.ShopDto;
import com.kh.maproot.error.TargetNotfoundException;
import com.kh.maproot.service.KakaoPayService;
import com.kh.maproot.service.PaymentService;
import com.kh.maproot.vo.TokenVO;
import com.kh.maproot.vo.kakaopay.KakaoPayApproveRequestVO;
import com.kh.maproot.vo.kakaopay.KakaoPayApproveResponseVO;
import com.kh.maproot.vo.kakaopay.KakaoPayFlashVO;
import com.kh.maproot.vo.kakaopay.KakaoPayQtyVO;
import com.kh.maproot.vo.kakaopay.KakaoPayReadyRequestVO;
import com.kh.maproot.vo.kakaopay.KakaoPayReadyResponseVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Tag(name="카카오페이 결제 컨트롤러")

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/kakaopay")
public class KakaoPayRestController {
	@Autowired
	private KakaoPayService kakaoPayService;
	@Autowired
	private ShopDao shopDao;
	@Autowired
	private PaymentService paymentService;
	@Autowired
	private PaymentDao paymentDao;
	@Autowired
	private PaymentDetailDao paymentDetailDao;

	private Map<String, KakaoPayFlashVO> flashMap = Collections.synchronizedMap(new HashMap<>());
	
	/*
	@Operation(
		deprecated = false//비추천 여부(향후 사용 중지 예정이라면 true를 작성)
		, description = "카카오페이 물품 구매"//기능에 대한 설명
		, responses = {//예상되는 응답 코드
			@ApiResponse(
					responseCode = "200"//상태코드
					, description = "검사 성공"//설명
					, content = @Content(//결과 메세지의 형태 및 샘플
						mediaType = "text/plain"//일반 글자
						, schema = @Schema(implementation = Boolean.class)
						, examples = {
							@ExampleObject(value = "true"),
							@ExampleObject(value = "false")
						}
					)
			),
			@ApiResponse(
				responseCode = "500"//상태코드
				, description = "서버 오류"//설명
				, content = @Content(//결과 메세지의 형태 및 샘플
					//mediaType = "text/plain"//일반 글자
					schema = @Schema(implementation = String.class)
					, examples = {
						@ExampleObject(value = "server error")
					}
				)
			)
		}
	)
	 * */
	@Operation(summary = "카카오페이 구매", description = "KakaoPay-Purchase")
	@PostMapping("/buy")
	public KakaoPayReadyResponseVO buy(
			@RequestBody List<KakaoPayQtyVO> qtyList,
			@RequestHeader("Frontend-Url") String frontendUrl, 
			@RequestAttribute TokenVO tokenVO, 
			@RequestHeader("Authorization") String bearerToken) {
		
		if (qtyList == null || qtyList.isEmpty())
			throw new TargetNotfoundException();

		StringBuffer buffer = new StringBuffer();
		int total = 0;
		for (KakaoPayQtyVO qtyVO : qtyList) {
			ShopDto shopDto = shopDao.selectOne(qtyVO.getNo());
			if (buffer.isEmpty()) {
				buffer.append(shopDto.getShopName());
			}
			total += shopDto.getShopPrice() * qtyVO.getQty();
		}

		if (qtyList.size() >= 2) {
			buffer.append(" 외 ");
			buffer.append(qtyList.size() - 1);
			buffer.append("건");
		}

		KakaoPayReadyRequestVO requestVO = KakaoPayReadyRequestVO.builder()
				.partnerOrderId(UUID.randomUUID().toString())
				.partnerUserId(tokenVO.getLoginId())
				.itemName(buffer.toString())// 상품명
				.totalAmount(total)
				.build();

		KakaoPayReadyResponseVO responseVO = kakaoPayService.ready(requestVO);

		flashMap.put(
				requestVO.getPartnerOrderId(),
				KakaoPayFlashVO.builder()
						.partnerOrderId(requestVO.getPartnerOrderId())
						.partnerUserId(requestVO.getPartnerUserId())
						.tid(responseVO.getTid())
						.returnUrl(frontendUrl)
						.qtyList(qtyList)
						.build());

		// [4] 결과를 반환한다
		return responseVO;
	}

	@Operation(summary = "카카오페이 구매 성공", description = "KakaoPay-Purchase-Success")
	@GetMapping("/buy/success/{partnerOrderId}")
	public void success(@PathVariable String partnerOrderId,
			@RequestParam("pg_token") String pgToken,
			HttpServletResponse response) throws IOException {
		
		KakaoPayFlashVO flashVO = flashMap.remove(partnerOrderId);

		KakaoPayApproveRequestVO requestVO = KakaoPayApproveRequestVO.builder()
				.partnerOrderId(flashVO.getPartnerOrderId())
				.partnerUserId(flashVO.getPartnerUserId())
				.tid(flashVO.getTid())
				.pgToken(pgToken)
				.build();

		KakaoPayApproveResponseVO responseVO = kakaoPayService.approve(requestVO);

		paymentService.insert(responseVO, flashVO);

		response.sendRedirect(flashVO.getReturnUrl() + "/success");
	}

	@Operation(summary = "카카오페이 구매 취소", description = "KakaoPay-Purchase-Cancel")
	@GetMapping("/buy/cancel/{partnerOrderId}")
	public void cancel(@PathVariable String partnerOrderId,
			HttpServletResponse response) throws IOException {
		KakaoPayFlashVO flashVO = flashMap.remove(partnerOrderId);
		response.sendRedirect(flashVO.getReturnUrl() + "/cancel");
	}

	@Operation(summary = "카카오페이 구매 실패", description = "KakaoPay-Purchase-Fail")
	@GetMapping("/buy/fail/{partnerOrderId}")
	public void fail(@PathVariable String partnerOrderId,
			HttpServletResponse response) throws IOException {
		KakaoPayFlashVO flashVO = flashMap.remove(partnerOrderId);
		response.sendRedirect(flashVO.getReturnUrl() + "/fail");
	}
}
