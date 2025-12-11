package com.kh.maproot.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.maproot.dao.ScheduleDao;
import com.kh.maproot.dao.TagDao;
import com.kh.maproot.dto.ScheduleDto;
import com.kh.maproot.dto.TagDto;

@CrossOrigin
@RestController
@RequestMapping("/schedule")
public class ScheduleRestController {
	
	@Autowired
	private TagDao tagDao;
	@Autowired
	private ScheduleDao scheduleDao;
	
	@GetMapping("/tagList")
	public List<TagDto> tagList() {
		return tagDao.selectAll();
	}
	
	@PostMapping("/insert")
	public ScheduleDto insert(@RequestBody ScheduleDto scheduleDto) {
		return scheduleDao.insert(scheduleDto) ;
	}

}
