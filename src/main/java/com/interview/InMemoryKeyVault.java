package com.interview;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryKeyVault implements KeyVault {

    private final Map<TypedKey, String> storage = new ConcurrentHashMap<>();
    private final KeyNormalizer keyNormalizer;

    public InMemoryKeyVault() {
        this(new DefaultKeyNormalizer());
    }

    public InMemoryKeyVault(KeyNormalizer keyNormalizer) {
        this.keyNormalizer = keyNormalizer;
    }

    @Override
    public String store(String key, String value) {
        return doStore(keyNormalizer.normalize(key), value);
    }

    @Override
    public String store(Number key, String value) {
        return doStore(keyNormalizer.normalize(key), value);
    }

    @Override
    public String get(String key) {
        return doGet(keyNormalizer.normalize(key));
    }

    @Override
    public String get(Number key) {
        return doGet(keyNormalizer.normalize(key));
    }

    private String doStore(TypedKey key, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Invalid argument");
        }

        return storage.put(key, value);
    }

    private String doGet(TypedKey key) {
        String value = storage.get(key);

        if (value == null) {
            throw new IllegalArgumentException("No such key in vault");
        }

        return value;
    }
}