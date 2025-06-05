package org.codewith3h.finmateapplication.exception;


import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    USER_NOT_FOUND(1001, "User not found"),
    NO_GOAL_FOUND(1002, "No goal found"),
    ;


    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
