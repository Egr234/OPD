package com.projectmanagement.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    public static boolean isOverdue(LocalDate deadline, String status) {
        if (deadline == null || "COMPLETED".equals(status)) {
            return false;
        }
        return deadline.isBefore(LocalDate.now());
    }
}