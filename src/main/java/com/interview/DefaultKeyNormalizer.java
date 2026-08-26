package com.interview;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

public class DefaultKeyNormalizer implements KeyNormalizer {

    private static final int MAX_KEY_LENGTH = 20;

    @Override
    public TypedKey normalize(String key) {
        if (key == null || key.isBlank() || key.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException("Invalid argument");
        }

        return new TypedKey(KeyType.STRING, key.toLowerCase(Locale.ROOT));
    }

    @Override
    public TypedKey normalize(Number key) {
        if (key == null) {
            throw new IllegalArgumentException("Invalid argument");
        }

        BigInteger value;
        try {
            value = new BigDecimal(key.toString()).toBigIntegerExact();
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException("Invalid argument", e);
        }

        if (value.signum() <= 0) {
            throw new IllegalArgumentException("Invalid argument");
        }

        return new TypedKey(KeyType.NUMBER, value.toString());
    }
}