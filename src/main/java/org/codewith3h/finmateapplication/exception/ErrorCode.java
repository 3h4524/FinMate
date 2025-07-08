package org.codewith3h.finmateapplication.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND(1001, "User not found", HttpStatus.BAD_REQUEST),
    NO_GOAL_FOUND(1002, "No goal found", HttpStatus.BAD_REQUEST),
    EXCEED_MAX_LENGTH_OF_NAME(1003, "Name exceeds maximum length: 100", HttpStatus.BAD_REQUEST),
    USER_ID_IS_REQUIRED(1004, "User id is required", HttpStatus.BAD_REQUEST),
    AMOUNT_MUST_BE_POSITIVE(1005, "Amount must be greater than 0", HttpStatus.BAD_REQUEST),
    GOAL_ID_IS_REQUIRED(1006, "Goal id is required", HttpStatus.BAD_REQUEST),
    TRANSACTION_NOT_FOUND_EXCEPTION(1007, "Transaction not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND_EXCEPTION(1008, "Category not found", HttpStatus.NOT_FOUND),
    INVALID_FREQUENCY_EXCEPTION(1009, "Invalid Frequency", HttpStatus.BAD_REQUEST),
    INVALID_INPUT(1010, "Invalid input data", HttpStatus.BAD_REQUEST),
    BUDGET_EXISTS(1011, "Budget already exists", HttpStatus.BAD_REQUEST),
    NO_WALLET_FOR_USER_EXCEPTION(1012, "No wallet for given user", HttpStatus.NOT_FOUND),
    NEGATIVE_BALANCE_NOT_ALLOWED(1013, "Negative balance not allowed", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED_EXCEPTION(1014, "Email already exists", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED_EXCEPTION(1015, "Email not verified", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_FOUND_EXCEPTION(1016, "Email not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(1017, "User already exists", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1018, "Password is incorrect", HttpStatus.BAD_REQUEST),
    INVALID_VERIFICATION_CODE_EXCEPTION(1019, "Verification code is incorrect or expiry", HttpStatus.BAD_REQUEST),
    INCORRECT_VERIFICATION_CODE_EXCEPTION(1020, "Incorrect verification code", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_VERIFIED_EXCEPTION(1021, "Email already verified", HttpStatus.BAD_REQUEST),
    IN_RESENT_OTP_EXCEPTION(1022, "Please wait before requesting another verification code", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1023, "Incorrect token for verification", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1024, "Token is expired", HttpStatus.BAD_REQUEST),
    PREMIUM_REQUIRED(1025, "This feature requires premium to active", HttpStatus.BAD_REQUEST),
    FEATURE_NOT_FOUND(1026, "Feature not found with feature code", HttpStatus.NOT_FOUND),
    PREMIUM_PACKAGE_NOT_FOUND(1027, "Premium package not found", HttpStatus.NOT_FOUND),
    CANNOT_CREATE_PAYMENT_EXCEPTION(1028, "Cannot create payment transaction", HttpStatus.BAD_REQUEST),
    DURATION_DATE_NOT_FOUND(1029, "Duration date not found", HttpStatus.NOT_FOUND),
    BUDGET_NOT_FOUND(1030, "Budget not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED(1031, "Unauthorized access", HttpStatus.FORBIDDEN),
    RECURRING_TRANSACTION_NOT_FOUND(1032, "Recurring transaction not found", HttpStatus.NOT_FOUND),
    EXCEED_FREE_CREATE_CUSTOM_CATEGORY(1033, "The number of custom categories has been exceeded", HttpStatus.BAD_REQUEST),
    EXCEED_CREATE_RECURRING_CATEGORY(1034, "The number of  recurring categories has been exceeded", HttpStatus.BAD_REQUEST),
    COUPON_NOT_FOUND(1035, "Coupon not found", HttpStatus.NOT_FOUND),
    EXCEED_FREE_CREATE_GOAL(1036, "The number of financial goals has been exceeded", HttpStatus.BAD_REQUEST),
    CAN_NOT_PROCESS_EXPIRED_SUBSCRIPTION(1037, "Failed to process expired subscriptions", HttpStatus.BAD_REQUEST),
    EXPIRED_COUPON(1038, "Coupon has expired", HttpStatus.BAD_REQUEST),
    UNAVAILABLE_COUPON(1039, "Coupon temporarily unavailable", HttpStatus.BAD_REQUEST),
    EXCEED_MAX_USAGE_COUPON(1040, "Coupon usage limit exceeded", HttpStatus.BAD_REQUEST),
    PACKAGE_ID_IS_REQUIRED(1041, "Package id is required", HttpStatus.BAD_REQUEST),
    YOU_ALREADY_USED_THIS_COUPON(1042, "You have already used this coupon code", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT(1043, "Invalid email format", HttpStatus.BAD_REQUEST),
    BUDGET_LIMIT_EXCEEDED(1044, "You have reached the 3 budget limit for regular users.", HttpStatus.BAD_REQUEST),
    ADMIN_LOG_NOT_FOUND(1045, "Admin log not found", HttpStatus.NOT_FOUND),
    ;

    private int code;
    private String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

