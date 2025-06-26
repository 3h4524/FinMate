package org.codewith3h.finmateapplication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Status {
    ACTIVE("ACTIVE"),
    CANCELLED("CANCELLED"),
    PENDING("PENDING"),
    COMPLETED("COMPLETED"),
    IN_PROGRESS("IN_PROGRESS"),
    FAILED("FAILED"),
    EXPIRED("EXPIRED"),
    ;

    private final String statusString;
}
