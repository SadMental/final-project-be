package com.kh.maproot.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.maproot.dao.PaymentDao;
import com.kh.maproot.dao.PaymentDetailDao;
import com.kh.maproot.dto.PaymentDetailDto;
import com.kh.maproot.dto.PaymentDto;
import com.kh.maproot.error.NeedPermissionException;
import com.kh.maproot.error.TargetNotfoundException;
import com.kh.maproot.service.KakaoPayService;
import com.kh.maproot.service.PaymentService;
import com.kh.maproot.vo.TokenVO;
import com.kh.maproot.vo.kakaopay.KakaoPayCancelRequestVO;
import com.kh.maproot.vo.kakaopay.KakaoPayCancelResponseVO;
import com.kh.maproot.vo.kakaopay.KakaoPayOrderRequestVO;
import com.kh.maproot.vo.kakaopay.KakaoPayOrderResponseVO;
import com.kh.maproot.vo.kakaopay.PaymentInfoVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name="상품 관리 컨트롤러")

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/payment")
public class PaymentRestController {
	@Autowired
	private PaymentDao paymentDao;
	@Autowired
	private PaymentDetailDao paymentDetailDao;
	@Autowired
	private KakaoPayService kakaoPayService;
	@Autowired
	private PaymentService paymentService;


	@Operation(summary = "카카오페이 내 결제내역 조회", description = "KakaoPay-PaymentDto")
	@GetMapping("/account")
	public List<PaymentDto> listByOwner(@RequestAttribute TokenVO tokenVO) {
		return paymentDao.selectList(tokenVO);
	}

	@Operation(summary = "카카오페이 내 결제내역 상세 조회", description = "KakaoPay-List-PaymentDetailDto")
	@GetMapping("/{paymentNo}")
	public PaymentInfoVO detail(@PathVariable long paymentNo,
			@RequestAttribute TokenVO tokenVO) {
		
		PaymentDto paymentDto = paymentDao.selectOne(paymentNo);
		if (paymentDto == null)
			throw new TargetNotfoundException();

		boolean isOwner = paymentDto.getPaymentOwner().equals(tokenVO.getLoginId());
		if (isOwner == false)
			throw new NeedPermissionException();

		List<PaymentDetailDto> paymentDetailList = paymentDetailDao.selectList(paymentNo);

		KakaoPayOrderResponseVO responseVO = kakaoPayService.order(
				KakaoPayOrderRequestVO.builder()
						.tid(paymentDto.getPaymentTid())
						.build());

		return PaymentInfoVO.builder()
				.paymentDto(paymentDto)
				.paymentDetailList(paymentDetailList)
				.responseVO(responseVO)
				.build();
	}


	@Operation(summary = "카카오페이 결제 전체 취소", description = "KakaoPay-CancelAll")
	@DeleteMapping("/{paymentNo}")
	public void cancel(@PathVariable long paymentNo,
			@RequestAttribute TokenVO tokenVO) {

		PaymentDto paymentDto = paymentDao.selectOne(paymentNo);
		if (paymentDto == null)
			throw new TargetNotfoundException();

		boolean isOwner = paymentDto.getPaymentOwner().equals(tokenVO.getLoginId());
		if (isOwner == false)
			throw new NeedPermissionException();

		KakaoPayCancelRequestVO requestVO = KakaoPayCancelRequestVO.builder()
				.tid(paymentDto.getPaymentTid())
				.cancelAmount(paymentDto.getPaymentRemain())
				.build();

		KakaoPayCancelResponseVO responseVO = kakaoPayService.cancel(requestVO);

		paymentService.cancel(paymentNo);
	}


	@Operation(summary = "카카오페이 결제 부분 취소", description = "KakaoPay-CancelUnit")
	@DeleteMapping("/detail/{paymentDetailNo}")
	public void cancelUnit(@PathVariable long paymentDetailNo, 
			@RequestAttribute TokenVO tokenVO) {
		PaymentDetailDto paymentDetailDto = paymentDetailDao.selectOne(paymentDetailNo);
		if (paymentDetailDto == null)
			throw new TargetNotfoundException();

		PaymentDto paymentDto = paymentDao.selectOne(paymentDetailDto.getPaymentDetailOrigin());
		if (paymentDto == null)
			throw new TargetNotfoundException();

		// 본인 정보인지 확인
		boolean isOwner = paymentDto.getPaymentOwner().equals(tokenVO.getLoginId());
		if (isOwner == false)
			throw new NeedPermissionException();

		if (paymentDetailDto.getPaymentDetailStatus().equals("취소"))
			throw new NeedPermissionException();

		KakaoPayCancelRequestVO requestVO = KakaoPayCancelRequestVO.builder()
				.tid(paymentDto.getPaymentTid())
				.cancelAmount(paymentDetailDto.getPaymentDetailTotal())
				.build();
		KakaoPayCancelResponseVO responseVO = kakaoPayService.cancel(requestVO);

		paymentService.cancelUnit(paymentDetailDto, responseVO);
	}
}
