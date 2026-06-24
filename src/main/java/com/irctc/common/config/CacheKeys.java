package com.irctc.common.config;

public final class CacheKeys {

    private CacheKeys() {}

    public static final String SEARCH_PREFIX = "search:";
  // 2 min payment expires
    public static final long SEARCH_TTL_MINUTES = 2;

    public static final String SEAT_LOCK_PREFIX = "lock:seat:";

    public static final long SEAT_LOCK_TTL_MINUTES = 10;
}