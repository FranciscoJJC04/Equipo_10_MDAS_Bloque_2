package es.uco.pw.pw2526.util;

import java.time.LocalDate;

/**
 * Utilidades de validacion reutilizables para controlar reglas basicas de entrada.
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static String requireNonEmpty(String value, String message) {
        if (value == null || value.isEmpty()) {
            return message;
        }
        return null;
    }

    public static String requireNonNull(Object value, String message) {
        if (value == null) {
            return message;
        }
        return null;
    }

    public static String requirePositive(int value, String message) {
        if (value <= 0) {
            return message;
        }
        return null;
    }

    public static String requireDateRange(LocalDate start, LocalDate end, String requiredMessage, String orderMessage) {
        if (start == null || end == null) {
            return requiredMessage;
        }
        if (end.isBefore(start)) {
            return orderMessage;
        }
        return null;
    }
}
