package com.tominnokoe.util;

/**
 * CSV/Excelインジェクション（数式インジェクション）対策。
 * セル値の先頭が {@code = + - @} で始まる場合、Excel等で開いた際に数式として実行されるのを防ぐため、
 * シングルクォートを前置してエスケープする。
 */
public final class SpreadsheetSanitizer {

    private SpreadsheetSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        char first = value.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@') {
            return "'" + value;
        }
        return value;
    }
}
