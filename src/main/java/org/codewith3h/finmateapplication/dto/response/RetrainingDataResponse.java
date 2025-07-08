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
    private List<Transaction> transactions;
    private List<Goal> goals;
    private List<Category> defaultCategories;
    private List<UserCategory> userCategories;
}
