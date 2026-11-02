package com.boxhilltravel.payment.util;

import org.dromara.common.core.exception.ServiceException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;

/**
 * Converts display amounts to Stripe's minor currency unit.
 */
public final class MoneyMinorUnitConverter {

    private MoneyMinorUnitConverter() {
    }

    public static long toMinor(BigDecimal amount, String currency) {
        if (amount == null || amount.signum() < 0) {
            throw new ServiceException("Payment amount must not be negative");
        }
        String normalized = normalizeCurrency(currency);
        int scale;
        try {
            scale = Currency.getInstance(normalized.toUpperCase(Locale.ROOT)).getDefaultFractionDigits();
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("Unsupported payment currency: " + currency);
        }
        try {
            return amount.movePointRight(scale).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException ex) {
            throw new ServiceException("Payment amount has too many decimal places for " + normalized);
        }
    }

    public static String normalizeCurrency(String currency) {
        if (currency == null || !currency.trim().matches("[A-Za-z]{3}")) {
            throw new ServiceException("Payment currency is invalid");
        }
        return currency.trim().toLowerCase(Locale.ROOT);
    }

}
