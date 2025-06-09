package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.AllArgsConstructor;
import org.codewith3h.finmateapplication.dto.request.CreateUserRequest;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.mapper.UserMapper;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    UserRepository userRepository;
    UserMapper userMapper;

    public User createUser(CreateUserRequest createUserRequest) {
        logger.info("Creating user: {}", createUserRequest);
        User user = userMapper.toUser(createUserRequest);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }
}