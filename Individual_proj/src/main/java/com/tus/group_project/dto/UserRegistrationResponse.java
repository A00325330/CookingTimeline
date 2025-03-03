package com.tus.group_project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor // ✅ This automatically creates a constructor with all fields
public class UserRegistrationResponse {
    private String message;
}
