package com.kh.maproot.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MessageDto {
	private int messageNo;
	private String messageType;
	private int messageChat;
	private String messageSender;
	private String messageContent;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SS")
	private LocalDateTime messageTime;
}
