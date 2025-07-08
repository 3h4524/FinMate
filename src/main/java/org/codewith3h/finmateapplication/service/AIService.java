package org.codewith3h.finmateapplication.service;

import org.codewith3h.finmateapplication.dto.request.BudgetPredictionRequest;
import org.codewith3h.finmateapplication.dto.response.BudgetPredictionResponse;
import org.codewith3h.finmateapplication.dto.response.RetrainResponse;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AIService {

    private final RestTemplate restTemplate;
    private final String pythonApiBaseUrl;

    public AIService(RestTemplate restTemplate,
                     @Value("${python.api.base-url:http://localhost:8000}") String pythonApiBaseUrl) {
        this.restTemplate = restTemplate;
        this.pythonApiBaseUrl = pythonApiBaseUrl;
    }

    /**
     * Gọi endpoint /predict-budgets của Python API để dự đoán ngân sách.
     * @param userId ID của người dùng cần dự đoán
     * @param jwtToken JWT token của admin
     * @return BudgetPredictionResponse chứa danh sách ngân sách và tiết kiệm
     */
    public BudgetPredictionResponse predictBudgets(Integer userId, String jwtToken) {
        String url = pythonApiBaseUrl + "/predict-budgets";

        // Tạo request body
        BudgetPredictionRequest request = new BudgetPredictionRequest();
        request.setUserId(userId);

        // Tạo headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtToken);

        // Tạo HttpEntity
        HttpEntity<BudgetPredictionRequest> entity = new HttpEntity<>(request, headers);

        // Gửi yêu cầu POST
        ResponseEntity<BudgetPredictionResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, BudgetPredictionResponse.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Failed to predict budgets: " + response.getStatusCode());
        }

        return response.getBody();
    }

    /**
     * Gọi endpoint /retrain-model của Python API để retrain mô hình.
     * @param jwtToken JWT token của admin
     * @return Thông báo trạng thái retrain
     */
    public RetrainResponse retrainModel(String jwtToken) {
        String url = pythonApiBaseUrl + "/retrain-model";

        // Tạo headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtToken);

        // Tạo HttpEntity (không cần body)
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Gửi yêu cầu POST
        ResponseEntity<RetrainResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, RetrainResponse.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Failed to retrain model: " + response.getStatusCode());
        }

        return response.getBody();
    }

}