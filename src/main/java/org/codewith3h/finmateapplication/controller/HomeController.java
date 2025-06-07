package org.codewith3h.finmateapplication.controller;

import jakarta.servlet.http.HttpSession;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import lombok.Builder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
@Validated
@Builder
public class HomeController {
    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/home")
    public String home(HttpSession session) {
        try {
            String userEmail = (String) session.getAttribute("userEmail");
            if (userEmail == null) {
                log.warn("Unauthorized access attempt to /home");
                return "redirect:/login.html";
            }

            User user = userService.findByEmail(userEmail);
            if (user == null) {
                log.warn("User not found for email: {}", userEmail);
                return "redirect:/login.html";
            }

            log.info("User {} accessed home page", userEmail);
            return "home";
        } catch (Exception e) {
            log.error("Error accessing home page: {}", e.getMessage());
            return "redirect:/login.html";
        }
    }
} 