package com.tus.group_project.mapper;

import com.tus.group_project.dto.UserDto;
import com.tus.group_project.dto.UserRegistrationDto;
import com.tus.group_project.model.Role;
import com.tus.group_project.model.User;

import java.util.Set;
import java.util.HashSet;

public class UserMapper {
    private UserMapper() {}

    /**
     * Converts a UserRegistrationDto to a User entity.
     */
    public static void toUser(UserRegistrationDto userRegDto, User user) {
        user.setEmail(userRegDto.getEmail());
        user.setPassword(userRegDto.getPassword());

        // ✅ Fix for new Set<Role> structure
        Set<Role> roles = new HashSet<>();
        roles.add(userRegDto.getRole()); // Assuming role is provided in DTO
        user.setRoles(roles);
    }

    /**
     * Converts a User entity to a UserDto.
     */
    public static void toUserDto(User user, UserDto userDto) {
        userDto.setEmail(user.getEmail());

        // ✅ Fix: Convert Set<Role> to a single role string (assuming one role per user)
        userDto.setRole(user.getRoles().stream().findFirst().orElse(Role.USER)); // Default to USER if empty
    }
}
