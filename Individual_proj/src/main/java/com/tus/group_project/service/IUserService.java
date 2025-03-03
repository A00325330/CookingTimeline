package com.tus.group_project.service;

import com.tus.group_project.model.User;

import java.util.Optional;

public interface IUserService {
    Optional<User> getUserById(Long id);
}
