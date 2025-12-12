package com.kh.maproot.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.maproot.dao.ChatDao;
import com.kh.maproot.dto.ChatDto;
import com.kh.maproot.error.TargetNotfoundException;
import com.kh.maproot.vo.TokenVO;

@CrossOrigin
@RestController
@RequestMapping("/chat")
public class ChatRestController {

    @Autowired
    private ChatDao chatDao;

    @PostMapping
    @Transactional
    public ChatDto create(
            @RequestBody ChatDto chatDto,
            @RequestAttribute TokenVO tokenVO) {

        chatDto.setChatStatus("WAITING");

        ChatDto resultDto = chatDao.insert(chatDto);

        chatDao.enter(resultDto.getChatNo(), tokenVO.getLoginId());

        return resultDto;
    }

    //상담사 용 목록
    @GetMapping("/counselor/list")
    public List<ChatDto> counselorList(@RequestAttribute TokenVO tokenVO) {
        String accountLevel = tokenVO.getLoginLevel();

        if (!"상담사".equals(accountLevel)) {
            throw new TargetNotfoundException("접근 권한이 없습니다.");
        }

        String counselorId = tokenVO.getLoginId();
        return chatDao.selectCounselorList(counselorId);
    }

    // 3. 관리자용 전체 목록 조회 (필터링 없음)
    @GetMapping("/admin/list")
    public List<ChatDto> adminList() {
        return chatDao.selectAllList();
    }

    @GetMapping("/{chatNo}")
    public ChatDto detail(@PathVariable int chatNo) {
        return chatDao.selectOne(chatNo);
    }

    // 채팅방 상태 및 상담원 배정/해제
    @PostMapping("/status")
    @Transactional
    public boolean changeStatus(
            @RequestBody ChatDto chatDto,
            @RequestAttribute TokenVO tokenVO) {

        String loginId = tokenVO.getLoginId();
        String loginLevel = tokenVO.getLoginLevel();

        if ("ACTIVE".equals(chatDto.getChatStatus())) {

            if (!"상담사".equals(loginLevel)) {
                throw new RuntimeException("상담사만 상담을 시작할 수 있습니다.");
            }

            chatDto.setChatId(loginId);
            chatDto.setChatLevel("상담사");
        }
        else {
            chatDto.setChatId(null);
            chatDto.setChatLevel(null);
        }

        return chatDao.changeStatus(chatDto);
    }

    // 채팅방 참여 (party 테이블에 등록)
    @PostMapping("/enter")
    public void enter(
            @RequestBody Map<String, Object> data,
            @RequestAttribute TokenVO tokenVO) {

        int chatNo = (Integer) data.get("chatNo");
        String accountId = tokenVO.getLoginId();

        chatDao.enter(chatNo, accountId);
    }

    // 참여 여부 확인
    @PostMapping("/check")
    public Map<String, Boolean> check(
            @RequestBody Map<String, Object> data,
            @RequestAttribute TokenVO tokenVO) {

        int chatNo = (Integer) data.get("chatNo");
        String accountId = tokenVO.getLoginId();

        return Map.of(
                "result",
                chatDao.check(chatNo, accountId)
        );
    }
}
