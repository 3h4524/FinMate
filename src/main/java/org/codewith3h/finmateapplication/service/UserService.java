package org.codewith3h.finmateapplication.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.request.CreateUserRequest;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.mapper.UserMapper;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private UserMapper userMapper;

    public User createUser(CreateUserRequest createUserRequest) {
        User user = userMapper.toUser(createUserRequest);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return  userRepository.save(user);
    }
}
