package org.codewith3h.finmateapplication.exception;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    USER_NOT_FOUND(1001, "User not found"),
    NO_GOAL_FOUND(1002, "No goal found"),
    EXCEED_MAX_LENGTH_OF_NAME(1003, "Name exceeds maximum length: 100"),
    USER_ID_IS_REQUIRED(1004, "User id is required"),
    AMOUNT_MUST_BE_POSITIVE(1005, "Amount must be greater than 0"),
    GOAL_ID_IS_REQUIRED(1006, "Goal id is required"),
    TRANSACTION_NOT_FOUND_EXCEPTION(1007, "Transaction not found"),
    CATEGORY_NOT_FOUND_EXCEPTION(1008, "Category not found"),
    INVALID_FREQUENCY_EXCEPTION(1009, "Invalid Frequency"),
    INVALID_INPUT(1010, "Invalid input data"),
    BUDGET_EXISTS(1011, "Budget already exists"),
    ;


    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
