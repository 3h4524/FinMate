package org.codewith3h.finmateapplication.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Status {
    ACTIVE,
    CANCELLED,
    PENDING,
    COMPLETED,
    IN_PROGRESS,
    FAILED,
    EXPIRED,
    ;

}
