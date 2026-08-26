package com.interview;

public interface KeyNormalizer {

    TypedKey normalize(String key);

    TypedKey normalize(Number key);
}
