package com.kh.maproot.error;

// 사용처 : 중복 검사
public class TargetAlreadyExistsException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	public TargetAlreadyExistsException() {
		super();
	}
	public TargetAlreadyExistsException(String message) {
		super(message);
	}

}
