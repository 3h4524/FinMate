package org.codewith3h.finmateapplication.controller;

import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.request.CreateUserRequest;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody CreateUserRequest request) {
        User savedUser = userService.createUser(request);
        return ResponseEntity.ok(new ApiResponse<>(savedUser, "User created successfully."));
    }
}