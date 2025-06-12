//package org.codewith3h.finmateapplication.controller;
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.codewith3h.finmateapplication.service.UserService;
//import org.codewith3h.finmateapplication.util.JwtUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//
//@Controller
//@Slf4j
//@Validated
//public class HomeController {
//    private final UserService userService;
//    private final JwtUtil jwtUtil;
//
//    public HomeController(UserService userService, JwtUtil jwtUtil) {
//        this.userService = userService;
//        this.jwtUtil = jwtUtil;
//    }
//
//    @GetMapping("/home")
//    public String home(HttpServletRequest request) {
//        try {
//            String authHeader = request.getHeader("Authorization");
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                log.warn("Unauthorized access attempt to /home: Missing or invalid Authorization header");
//                return "redirect:/login.html";
//            }
//
//            String token = authHeader.substring(7);
//            if (jwtUtil.validateToken(token) || !jwtUtil.validateToken(token)) {
//                log.warn("Unauthorized access attempt to /home: Invalidated or expired token");
//                return "redirect:/login.html";
//            }
//
//            String userEmail = jwtUtil.extractEmail(token);
//            if (userEmail == null) {
//                log.warn("Unauthorized access attempt to /home: No email found in token");
//                return "redirect:/login.html";
//            }
//
//            var response = userService.getUserProfileByEmail(userEmail);
//            if (!response.isSuccess()) {
//                log.warn("User profile not found for email from token: {}", userEmail);
//                return "redirect:/login.html";
//            }
//
//            log.info("User {} accessed home page via JWT", userEmail);
//            return "home";
//        } catch (Exception e) {
//            log.error("Error accessing home page: {}", e.getMessage(), e);
//            return "redirect:/login.html";
//        }
//    }
//}