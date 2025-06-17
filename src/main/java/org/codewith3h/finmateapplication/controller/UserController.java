package org.codewith3h.finmateapplication.controller;

import lombok.RequiredArgsConstructor;
import org.codewith3h.finmateapplication.dto.request.RegisterRequest;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import lombok.Builder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Slf4j
@Validated
@RequiredArgsConstructor
@Builder
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;


}
