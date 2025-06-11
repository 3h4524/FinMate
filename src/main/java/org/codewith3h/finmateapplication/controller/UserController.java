package org.codewith3h.finmateapplication.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.request.CreateUserRequest;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/users")
public class UserController {

    UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody CreateUserRequest request) {
        User savedUser = userService.createUser(request);

        ApiResponse<User> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("User created successfully.");
        apiResponse.setResult(savedUser);

        return ResponseEntity.ok(apiResponse);
    }
}
