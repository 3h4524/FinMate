package org.codewith3h.finmateapplication.exception;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    USER_NOT_FOUND(1001, "User not found"),
    NO_GOAL_FOUND(1002, "No goal found"),
    EXCEED_MAX_LENGTH_OF_NAME(1003, "Name exceeds maximum length: 100"),
    USER_ID_IS_REQUIRED(1004, "User id is required"),
    AMOUNT_MUST_BE_POSITIVE(1005, "Amount must be greater than 0"),
    GOAL_ID_IS_REQUIRED(1006, "Goal id is required"),

    ;


    int code;
    String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
