package com.kh.maproot.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PageVO {
	private int page = 1;//현재 페이지 번호
	private int size = 5;//한 페이지에 표시할 데이터 수
	private int dataCount;//총 데이터 수
	private int blockSize = 10;//표시할 블록 개수(10개)
	
	public int getBlockStart() {//블록 시작번호
		return (page-1) / blockSize * blockSize + 1;
	}
	public int getBlockFinish() {//블록 종료번호
		int number = (page-1) / blockSize * blockSize + blockSize;
		return Math.min(getTotalPage(), number);
	}
	public int getTotalPage() {//총 페이지 수
		return (dataCount - 1) / size + 1;
	}	
	public int getBegin() {
		return page * size - (size-1);
	}
	public int getEnd() {
		return page * size;
	}
	public boolean isFirstBlock() {
		return getBlockStart() == 1;
	}
	public int getPrevPage() {
		return getBlockStart() - 1;
	}
	public boolean isLastBlock() {
		return getBlockFinish() == getTotalPage();
	}
	public int getNextPage() {
		return getBlockFinish() + 1;
	}
}
