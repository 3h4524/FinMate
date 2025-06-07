package org.codewith3h.finmateapplication.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.codewith3h.finmateapplication.dto.response.MessageResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import lombok.Builder;

@Controller
@Slf4j
@Validated
@Builder
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    @ResponseBody
    public ResponseEntity<MessageResponse> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
        
        String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
        log.error("Error occurred: {} - {}", statusCode, errorMessage);
        
        return ResponseEntity
            .status(statusCode != null ? statusCode : 500)
            .body(new MessageResponse(errorMessage));
    }
} 