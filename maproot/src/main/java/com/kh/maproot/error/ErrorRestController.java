package com.kh.maproot.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin
@RestControllerAdvice
public class ErrorRestController {
	
	@ExceptionHandler(TargetNotfoundException.class)
	public ResponseEntity<String> notFound(TargetNotfoundException e){
		return ResponseEntity.notFound().build();
	}
	
	@ExceptionHandler(UnauthorizationException.class)
	public ResponseEntity<String> unauthorize(UnauthorizationException e){
		return ResponseEntity.status(401).build();
	}
	
	@ExceptionHandler(NeedPermissionException.class)
	public ResponseEntity<String> needPermission(NeedPermissionException e){
		return ResponseEntity.status(403).build();
	}
	
	@ExceptionHandler(TargetAlreadyExistsException.class)
	public ResponseEntity<String> TargetAlreadyExists(TargetAlreadyExistsException e){
		return ResponseEntity.status(409).build();
	}
	
	// 나머지 모든 예외
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> all(Exception e){
		log.error("예외 발생", e);
		return ResponseEntity.internalServerError().build();
	}
	

}
