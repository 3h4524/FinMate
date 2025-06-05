package org.codewith3h.finmateapplication.specification;

import org.codewith3h.finmateapplication.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

public class TransactionSpecification {

    public static Specification<Transaction> hasUserId(Integer userId) {
        return ((root, query, criteriaBuilder) ->
                userId == null ? null : criteriaBuilder.equal(root.get("userId"), userId));
    }
}
