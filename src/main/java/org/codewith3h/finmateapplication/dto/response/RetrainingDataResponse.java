package org.codewith3h.finmateapplication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codewith3h.finmateapplication.entity.Category;
import org.codewith3h.finmateapplication.entity.Goal;
import org.codewith3h.finmateapplication.entity.Transaction;
import org.codewith3h.finmateapplication.entity.UserCategory;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RetrainingDataResponse {
    private List<TransactionResponse> transactions;
    private List<GoalResponse> goals;
    private List<CategoryResponse> defaultCategories;
    private List<CategoryResponse> userCategories;
}
