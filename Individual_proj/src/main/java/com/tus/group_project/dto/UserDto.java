package com.tus.group_project.dto;

import com.tus.group_project.model.Role;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDto {
	private String email;
	private Role role;
}
