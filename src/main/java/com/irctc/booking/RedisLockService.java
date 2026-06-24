package com.irctc.booking;

import com.irctc.common.config.CacheKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisLockService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisLockService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean acquireLock(Long seatId, String journeyDate) {

        // Build the lock key — unique per seat per date
        // Why include date? Seat 32 can be booked on different dates
        // lock:seat:32:2026-12-25 and lock:seat:32:2026-12-26 are separate locks
        String lockKey = CacheKeys.SEAT_LOCK_PREFIX + seatId + ":" + journeyDate;

        // setIfAbsent = SETNX in Redis
        // Returns true if lock was acquired (key didn't exist)
        // Returns false if lock already exists (someone else has it)
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                lockKey,                            // key
                "LOCKED",                           // value
                CacheKeys.SEAT_LOCK_TTL_MINUTES,    // TTL = 10
                TimeUnit.MINUTES                    // minutes
        );
        // The TTL is CRITICAL — if our app crashes after acquiring lock
        // but before releasing it, the lock auto-expires after 10 minutes
        // Without TTL → lock stays forever → seat never bookable again

        if (Boolean.TRUE.equals(acquired)) {
            log.info("Lock ACQUIRED — seat: {} date: {}", seatId, journeyDate);
        } else {
            log.warn("Lock FAILED — seat: {} date: {} already locked", seatId, journeyDate);
        }

        return Boolean.TRUE.equals(acquired);
        // Boolean.TRUE.equals() instead of acquired == true
        // because acquired could be null if Redis is down
        // Boolean.TRUE.equals(null) returns false safely
    }

    // ── Release a lock on a seat ──────────────────────────────────────────
    public void releaseLock(Long seatId, String journeyDate) {
        String lockKey = CacheKeys.SEAT_LOCK_PREFIX + seatId + ":" + journeyDate;

        Boolean deleted = redisTemplate.delete(lockKey);

        if (Boolean.TRUE.equals(deleted)) {
            log.info("Lock RELEASED — seat: {} date: {}", seatId, journeyDate);
        } else {
            log.warn("Lock RELEASE FAILED — seat: {} date: {} not found", seatId, journeyDate);
        }
    }

    // ── Check if a seat is locked ─────────────────────────────────────────
    public boolean isLocked(Long seatId, String journeyDate) {
        String lockKey = CacheKeys.SEAT_LOCK_PREFIX + seatId + ":" + journeyDate;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
        // hasKey returns true if key exists in Redis
    }

    // ── Get remaining lock time in seconds ────────────────────────────────
    public Long getLockTTL(Long seatId, String journeyDate) {
        String lockKey = CacheKeys.SEAT_LOCK_PREFIX + seatId + ":" + journeyDate;
        return redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        // Returns seconds remaining until lock expires
        // Returns -2 if key doesn't exist
        // Returns -1 if key exists but has no expiry (shouldn't happen)
    }
}