package org.codewith3h.finmateapplication.exception;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    TRANSACTION_NOT_FOUND_EXCEPTION(1001, "Transaction not found"),
    USER_NOT_FOUND_EXCEPTION(1002, "User not found"),
    CATEGORY_NOT_FOUND_EXCEPTION(1003, "Category not found"),
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
