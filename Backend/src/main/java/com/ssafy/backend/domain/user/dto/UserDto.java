package com.ssafy.backend.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserDto {
	private String profileImage;
	private String email;
	private String name;
	private String nickname;
}
