package com.halcyon.hotel.util;

public final class CardUtils {

    private CardUtils() {}

    public static String detectBrand(String digitsOnly) {
        if (digitsOnly.startsWith("4")) return "Visa";
        if (digitsOnly.matches("^5[1-5].*")) return "Mastercard";
        if (digitsOnly.matches("^3[47].*")) return "American Express";
        if (digitsOnly.startsWith("6")) return "Discover";
        return "Card";
    }

    public static String stripSpaces(String raw) {
        return raw == null ? "" : raw.replaceAll("[\\s-]", "");
    }

    public static boolean isValidCardNumber(String digitsOnly) {
        if (!digitsOnly.matches("^\\d{12,19}$")) return false;
        return passesLuhn(digitsOnly);
    }

    /** Standard Luhn checksum used by all major card networks. */
    private static boolean passesLuhn(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = number.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    public static String last4(String digitsOnly) {
        return digitsOnly.substring(digitsOnly.length() - 4);
    }
}
