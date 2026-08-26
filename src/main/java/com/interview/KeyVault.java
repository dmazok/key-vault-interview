package com.interview;

public interface KeyVault {

    String store(String key, String value);

    String store(Number key, String value);

    String get(String key);

    String get(Number key);
}
