package com.tus.group_project.service.impl;

import com.tus.group_project.dao.UserRepository;
import com.tus.group_project.model.User;
import com.tus.group_project.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
}
