package org.codewith3h.finmateapplication.enums;

import lombok.Getter;

@Getter
public enum LimitCount {
    CUSTOM_USER_CATEGORY(3),
    RECURRING_TRANSACTION(4),
    FINANCIAL_GOAL(3),
    ;

    private int count;

    LimitCount(int count){
        this.count = count;
    }
}
