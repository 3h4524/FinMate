package org.codewith3h.finmateapplication.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionResponse {
    Integer transactionId;
    Integer userId;
    Integer categoryId;
    Integer userCategoryId;
    String categoryName;
    String userCategoryName;
    String icon;
    BigDecimal amount;
    String note;
        String type;
    LocalDate transactionDate;
    String paymentMethod;
    String location;
    String imageUrl;
    Boolean isRecurring;
    String recurringPattern;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
