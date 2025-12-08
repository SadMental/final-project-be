package com.nddy.kakaopay.error;

public class TargetNotfoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public TargetNotfoundException() {
		super();
	}
	public TargetNotfoundException(String message) {
		super(message);
	}
}
