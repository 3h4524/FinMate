package org.codewith3h.finmateapplication.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error")
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
