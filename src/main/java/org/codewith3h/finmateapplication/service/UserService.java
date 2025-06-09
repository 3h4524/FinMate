package org.codewith3h.finmateapplication.service;

import org.codewith3h.finmateapplication.dto.request.CreateUserRequest;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.mapper.UserMapper;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;

    public User createUser(CreateUserRequest createUserRequest) {
        User user = new  User();
        user.setName(createUserRequest.getName());
        user.setEmail(createUserRequest.getEmail());

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPasswordHash(passwordEncoder.encode(createUserRequest.getPassword()));
        return  userRepository.save(user);
    }
}
