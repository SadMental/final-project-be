package com.kh.maproot.restcontroller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kh.maproot.configuration.EncryptConfiguration;
import com.kh.maproot.dao.ReviewDao;
import com.kh.maproot.dao.ReviewUnitLinkDao;
import com.kh.maproot.dao.ScheduleDao;
import com.kh.maproot.dao.ScheduleMemberDao;
import com.kh.maproot.dao.ScheduleUnitDao;
import com.kh.maproot.dao.reviewScheduleLinkDao;
import com.kh.maproot.dto.ReviewDto;
import com.kh.maproot.dto.ReviewUnitLinkDto;
import com.kh.maproot.dto.ScheduleDto;
import com.kh.maproot.dto.ScheduleMemberDto;
import com.kh.maproot.dto.ScheduleUnitDto;
import com.kh.maproot.schedule.vo.ReviewRequestVO;
import com.kh.maproot.schedule.vo.ReviewResponseVO;
import com.kh.maproot.schedule.vo.ReviewScheduleLinkVO;
import org.springframework.web.bind.annotation.RequestParam;


@CrossOrigin
@RestController
@RequestMapping("/review")
public class ScheduleReviewRestcontroller {

    private final EncryptConfiguration encryptConfiguration;

	@Autowired
	private ReviewDao reviewDao;
	@Autowired
	private ReviewUnitLinkDao reviewUnitLinkDao;
	@Autowired
	private ScheduleDao scheduleDao;
	@Autowired
	private reviewScheduleLinkDao reviewScheduleLinkDao;
	@Autowired
	private ScheduleUnitDao scheduleUnitDao;
	@Autowired
	private ScheduleMemberDao scheduleMemberDao;

    ScheduleReviewRestcontroller(EncryptConfiguration encryptConfiguration) {
        this.encryptConfiguration = encryptConfiguration;
    }
	
	@GetMapping("/list/{scheduleNo}")
	public List<ReviewResponseVO> list(@PathVariable int scheduleNo) {

	    // 1) 대표 일정에 해당되는 리뷰+일정들(조인 결과) 모두 가져오기
	    List<ReviewScheduleLinkVO> rows =
	        reviewScheduleLinkDao.selectByScheduleNo(scheduleNo);

	    // 2) reviewNo 기준으로 모을 Map
	    Map<Integer, ReviewResponseVO> map = new LinkedHashMap<>();

	    // 3) 한 줄씩 돌면서 그룹핑
	    for (ReviewScheduleLinkVO row : rows) {
	        // 이 리뷰번호(reviewNo)로 이미 만든 ReviewResponseVO가 있으면 꺼내고,
	        // 없으면 새로 만들어서 map에 넣어라
	        ReviewResponseVO vo = map.computeIfAbsent(
	            row.getReviewNo(),
	            key -> ReviewResponseVO.builder()
	                    .reviewNo(row.getReviewNo())
	                    .scheduleNo(row.getScheduleNo())
	                    .reviewWriterType(row.getReviewWriterType())
	                    .accountId(row.getAccountId())
	                    .reviewWriterNickname(row.getReviewWriterNickname())
	                    .reviewContent(row.getReviewContent())
	                    .reviewWtime(row.getReviewWtime())
	                    .reviewEtime(row.getReviewEtime())
	                    .scheduleUnitNoList(new ArrayList<>()) // 처음엔 빈 리스트
	                    .build()
	        );
	        System.out.println(row);
	        // 4) 세부 일정 번호가 있으면 리스트에 추가
	        Integer unitNo = row.getScheduleUnitNo();
	        if (unitNo != null) { // null 아니면
	            vo.getScheduleUnitNoList().add(unitNo);
	        }
	    }

	    // 5) Map에 모인 값들만 꺼내서 List로 변환해서 반환
	    return new ArrayList<>(map.values());
	}
	
	
	
	@PostMapping("/insert")
	public ReviewDto insert(@RequestBody ReviewRequestVO reviewRequestVO ) {
		
		//유저인지, 게스트인지 확인 필요(유저 정보는 백엔드에서 해결)
		


		//댓글 등록 (추후 사진 이미지 추가 필요)
		ReviewDto reviewDto = ReviewDto.builder()
				.scheduleNo(reviewRequestVO.getScheduleNo())
				.reviewWriterType("USER")
				.accountId("testuser1")
				.reviewWriterNickname("테스트유저1")
				.reviewContent(reviewRequestVO.getReviewContent())
				.reviewWtime(Timestamp.valueOf(LocalDateTime.now()))
				.build();
							
				int sequence = reviewDao.insert(reviewDto);
				reviewDto.setReviewNo(sequence);
				
				//댓글-세부일정 연결
				reviewUnitLinkDao.insert(reviewDto.getReviewNo(), reviewRequestVO.getScheduleUnitList());
				
			return reviewDto;
	}
	
	@DeleteMapping("/{reviewNo}")
	public void delete(@PathVariable int reviewNo) {
		reviewDao.delete(reviewNo);
	}
	
	@PatchMapping("/{reviewNo}")
	public boolean update(@PathVariable int reviewNo ,@RequestBody ReviewDto reviewDto) {
		reviewDto.setReviewNo(reviewNo);
		return reviewDao.update(reviewDto);
	}
	
	//댓글에 따른 세부 일정 조회
	@GetMapping("/unit/{reviewNo}")
	public List<ReviewUnitLinkDto> unitList(@PathVariable int reviewNo) {
		System.out.println(reviewNo);
		System.out.println(reviewUnitLinkDao.selectByReviewNo(reviewNo));
		return reviewUnitLinkDao.selectByReviewNo(reviewNo);
	}
	
	//세부 일정 삭제
	@DeleteMapping("/unit/{reviewNo}")
	public void deleteScheduleUnit(
			@PathVariable int reviewNo,
			@RequestParam int scheduleUnitNo) {
		reviewUnitLinkDao.deleteBySelectUnit(reviewNo, scheduleUnitNo);
	}
	
	//그룹원 조회
	@GetMapping("/member/{scheduleNo}")
	public List<ScheduleMemberDto> selectByScheduleNo(@PathVariable int scheduleNo) {
		return scheduleMemberDao.selectByScheduleNo(scheduleNo);
	}
}
