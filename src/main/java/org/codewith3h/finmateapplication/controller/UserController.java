package org.codewith3h.finmateapplication.controller;

import lombok.RequiredArgsConstructor;
import org.codewith3h.finmateapplication.dto.request.RegisterRequest;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.UserDto;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.codewith3h.finmateapplication.service.UserService;
import org.codewith3h.finmateapplication.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@Validated
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getUserProfile(HttpServletRequest request) {
        log.info("Received get user profile request.");
        ApiResponse<UserDto> response = new ApiResponse<>();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setCode(9999);
            response.setMessage("Unauthorized: Invalid or missing token");
            return ResponseEntity.status(401).body(response);
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                response.setCode(9999);
                response.setMessage("Unauthorized: Invalid token");
                return ResponseEntity.status(401).body(response);
            }

            Integer userId = jwtUtil.extractId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            UserDto userDto = UserDto.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole())
                    .isPremium(user.getIsPremium())
                    .verified(user.getVerified())
                    .build();

            response.setCode(1000);
            response.setMessage("Success");
            response.setResult(userDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting user profile: {}", e.getMessage());
            response.setCode(9999);
            response.setMessage("Error getting user profile");
            return ResponseEntity.status(500).body(response);
        }
    }
}
