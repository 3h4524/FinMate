package org.codewith3h.finmateapplication.scheduler;

import com.nimbusds.jose.JOSEException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.response.BudgetPredictionResponse;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.enums.FeatureCode;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.codewith3h.finmateapplication.service.AIService;
import org.codewith3h.finmateapplication.service.EmailService;
import org.codewith3h.finmateapplication.service.FeatureService;
import org.codewith3h.finmateapplication.util.JwtUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIRecommendationSchedule {

    private final UserRepository userRepository;
    private final FeatureService featureService;
    private final AIService aiService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Transactional
    @Scheduled(cron = "0 0 8 1 * *")
    public void scheduleBudgetRecommendation() throws JOSEException, MessagingException {
        List<User> premiumUser = userRepository.findAllByIsPremium(true);
        String token = jwtUtil.generateTokenForExternalSystem();

        for(User user : premiumUser) {
            if(featureService.userHasFeature(user.getId(), FeatureCode.SMART_REMINDER.name())){
                BudgetPredictionResponse budgetPredictionResponse = aiService.predictBudgets(user.getId(), token);
            }
        }
    }
}
