package org.codewith3h.finmateapplication.enums;

import lombok.Data;
import lombok.Getter;

@Getter
public enum FeatureCode {
    EXPORT_REPORT("Xuất báo cáo"),
    UNLIMITED_CUSTOM_CATEGORY("Tạo danh mục tùy chỉnh không giới hạn"),
    UNLIMITED_BUDGET("Tạo ngân sách không giới hạn"),
    SMART_REMINDER("Nhắc nhở thông minh"),
    SYSTEM_ALERTS("Cảnh báo hệ thống");
    private final String displayName;

    FeatureCode(String displayName) {
        this.displayName = displayName;
    }


}