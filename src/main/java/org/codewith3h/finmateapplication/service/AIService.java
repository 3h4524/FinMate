package org.codewith3h.finmateapplication.service;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.BudgetPredictionRequest;
import org.codewith3h.finmateapplication.dto.response.AiStatsResponse;
import org.codewith3h.finmateapplication.dto.response.BudgetPredictionResponse;
import org.codewith3h.finmateapplication.dto.response.RetrainResponse;
import org.codewith3h.finmateapplication.entity.ModelTrainingHistory;
import org.codewith3h.finmateapplication.entity.UserCategory;
import org.codewith3h.finmateapplication.mapper.ModelTrainingMapper;
import org.codewith3h.finmateapplication.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Data
@Slf4j
public class AIService {

    private final RestTemplate restTemplate;
    private final String pythonApiBaseUrl;
    private final ModelTrainingMapper modelTrainingMapper;

    private EmailService emailService;
    private ModelTrainingRepository modelTrainingRepository;
    private TransactionRepository transactionRepository;
    private CategoryRepository categoryRepository;
    private UserCategoryRepository userCategoryRepository;

    public AIService(RestTemplate restTemplate,
                     @Value("${python.api.base-url:http://localhost:8000}") String pythonApiBaseUrl,
                     ModelTrainingMapper modelTrainingMapper,
                     EmailService emailService,
                     ModelTrainingRepository modelTrainingRepository,
                     CategoryRepository categoryRepository,
                     UserCategoryRepository userCategoryRepository,
                     TransactionRepository transactionRepository
                     ) {
        this.restTemplate = restTemplate;
        this.pythonApiBaseUrl = pythonApiBaseUrl;
        this.modelTrainingMapper = modelTrainingMapper;
        this.emailService = emailService;
        this.modelTrainingRepository = modelTrainingRepository;
        this.categoryRepository = categoryRepository;
        this.userCategoryRepository = userCategoryRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Gọi endpoint /predict-budgets của Python API để dự đoán ngân sách.
     *
     * @param userId   ID của người dùng cần dự đoán
     * @param jwtToken JWT token của admin
     * @return BudgetPredictionResponse chứa danh sách ngân sách và tiết kiệm
     */
    public BudgetPredictionResponse predictBudgets(Integer userId, String jwtToken) throws MessagingException {
        String url = pythonApiBaseUrl + "/predict-budgets";

        BudgetPredictionRequest request = new BudgetPredictionRequest();
        request.setUserId(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtToken);

        HttpEntity<BudgetPredictionRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<BudgetPredictionResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, BudgetPredictionResponse.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Failed to predict budgets: " + response.getStatusCode());
        }

        if (response.getBody() != null && response.getBody().getBudgets() != null) {
            emailService.sendBudgetRecommendation(userId, response.getBody().getBudgets());
            log.info("Budget recommendation sent to user: {}", userId);
        } else {
            log.warn("No budget predictions available for user: {}", userId);
        }

        return response.getBody();
    }

    /**
     * Gọi endpoint /retrain-model của Python API để retrain mô hình.
     *
     * @param jwtToken JWT token của admin
     * @return Thông báo trạng thái retrain
     */
    @Transactional
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
        RetrainResponse retrainResponse = response.getBody();
        ModelTrainingHistory modelTrainingHistory = modelTrainingMapper.toEntity(retrainResponse);
        modelTrainingRepository.save(modelTrainingHistory);
        return retrainResponse;
    }

    @Transactional(readOnly = true)
    public Page<RetrainResponse> getModelTrainings(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ModelTrainingHistory> modelTrainingHistories = modelTrainingRepository.findAll(pageable);
        return modelTrainingHistories.map(modelTrainingMapper::toResponseDto);
    }

    public AiStatsResponse getStatsModel(){
        List<ModelTrainingHistory> trainings = modelTrainingRepository.findAll();

        int totalTraining = trainings.size();
        long successCount = trainings.stream()
                .filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus()))
                .count();

        double avgDuration = trainings.stream()
                .filter(t -> t.getTrainingDurationSeconds() != null)
                .mapToDouble(ModelTrainingHistory::getTrainingDurationSeconds)
                .average()
                .orElse(0.0);

        Optional<LocalDateTime> lastUpdated = trainings.stream()
                .map(ModelTrainingHistory::getTrainingTimestamp)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());

        double avgAccuracy = trainings.stream()
                .filter(t -> t.getMse() != null)
                .mapToDouble(t -> 1.0 - t.getMse())
                .average()
                .orElse(0.0);

        Integer totalTransactions = transactionRepository.findAll().size();
        Integer totalCategories =  categoryRepository.findAll().size();
        Integer totalUserCategory =   userCategoryRepository.findAll().size();

        AiStatsResponse response = AiStatsResponse.builder()
                .totalTraining(totalTraining)
                .accuracy((float) avgAccuracy)
                .successRate(totalTraining == 0 ? 0 : (int) ((successCount * 100.0) / totalTraining))
                .avgDuration((float) avgDuration)
                .lastUpdated(lastUpdated.map(LocalDateTime::toLocalDate).orElse(null))
                .totalTransaction(totalTransactions)
                .totalCategories(totalCategories + totalUserCategory)
                .build();

        return response;
    }
}