package com.tus.group_project.dto;

import com.tus.group_project.model.Role;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDto {
    private Long id;       // ✅ Store user ID for HATEOAS links
    private String email;  // ✅ User email
    private Role role;     // ✅ Store a single role instead of a Set<Role>
}
