package org.codewith3h.finmateapplication;

import lombok.RequiredArgsConstructor;
import org.codewith3h.finmateapplication.entity.Category;
import org.codewith3h.finmateapplication.entity.Transaction;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.entity.UserCategory;
import org.codewith3h.finmateapplication.repository.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntityResolver {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final TransactionRepository transactionRepository;
    private final RecurringTransactionRepository  recurringTransactionRepository;

    public User resolverUser(Integer Id){
        return userRepository.getReferenceById(Id);
    }

    public Category resolverCategory(Integer Id){
        return categoryRepository.getReferenceById(Id);
    }

    public Transaction resolverTransaction(Integer Id){
        return transactionRepository.getReferenceById(Id);
    }

    public UserCategory resolverUserCategory(Integer Id){
        return userCategoryRepository.getReferenceById(Id);
    }
}
