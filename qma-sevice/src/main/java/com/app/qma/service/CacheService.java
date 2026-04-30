package com.app.qma.service;

import com.app.qma.dto.request.QuantityMeasurementDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * Redis cache wrapper.
 *
 * Global key schema:
 *   qma:history:op:{operation}          → List<QuantityMeasurementDTO>
 *   qma:history:type:{measureType}      → List<QuantityMeasurementDTO>
 *   qma:history:errored                 → List<QuantityMeasurementDTO>
 *   qma:count:{operation}               → Long
 *
 * User-scoped key schema:
 *   qma:user:{email}:history:all                    → List<QuantityMeasurementDTO>
 *   qma:user:{email}:history:op:{operation}         → List<QuantityMeasurementDTO>
 *   qma:user:{email}:history:type:{measurementType} → List<QuantityMeasurementDTO>
 *   qma:user:{email}:history:errored                → List<QuantityMeasurementDTO>
 *   qma:user:{email}:count:{operation}              → Long
 *
 * Cache failures are swallowed so Redis going down never breaks the main flow.
 */
@Slf4j
@Service
public class CacheService {

    private static final String PREFIX           = "qma:";
    private static final String HISTORY_OP_KEY   = PREFIX + "history:op:";
    private static final String HISTORY_TYPE_KEY = PREFIX + "history:type:";
    private static final String HISTORY_ERR_KEY  = PREFIX + "history:errored";
    private static final String COUNT_KEY        = PREFIX + "count:";
    private static final String USER_PREFIX      = PREFIX + "user:";

    private final RedisTemplate<String, Object> redis;

    @Value("${app.cache.ttl-seconds:600}")
    private long ttlSeconds;

    public CacheService(RedisTemplate<String, Object> redis) {
        this.redis = redis;
    }

    // Global reads
    public List<QuantityMeasurementDTO> getHistoryByOp(String op)         { return tryGet(HISTORY_OP_KEY + op); }
    public List<QuantityMeasurementDTO> getHistoryByType(String type)      { return tryGet(HISTORY_TYPE_KEY + type); }
    public List<QuantityMeasurementDTO> getErrorHistory()                   { return tryGet(HISTORY_ERR_KEY); }
    public Long getCount(String op)                                         { return tryGetLong(COUNT_KEY + op); }

    // Global writes
    public void putHistoryByOp(String op, List<QuantityMeasurementDTO> d)   { safeSet(HISTORY_OP_KEY + op, d); }
    public void putHistoryByType(String t, List<QuantityMeasurementDTO> d)  { safeSet(HISTORY_TYPE_KEY + t, d); }
    public void putErrorHistory(List<QuantityMeasurementDTO> d)             { safeSet(HISTORY_ERR_KEY, d); }
    public void putCount(String op, long count)                             { safeSet(COUNT_KEY + op, count); }

    // User-scoped reads
    public List<QuantityMeasurementDTO> getUserHistory(String email)                    { return tryGet(uk(email,"history:all")); }
    public List<QuantityMeasurementDTO> getUserHistoryByOp(String email, String op)     { return tryGet(uk(email,"history:op:"+op)); }
    public List<QuantityMeasurementDTO> getUserHistoryByType(String email, String type) { return tryGet(uk(email,"history:type:"+type)); }
    public List<QuantityMeasurementDTO> getUserErrorHistory(String email)               { return tryGet(uk(email,"history:errored")); }
    public Long getUserCount(String email, String op)                                   { return tryGetLong(uk(email,"count:"+op)); }

    // User-scoped writes
    public void putUserHistory(String email, List<QuantityMeasurementDTO> d)                    { safeSet(uk(email,"history:all"),d); }
    public void putUserHistoryByOp(String email, String op, List<QuantityMeasurementDTO> d)     { safeSet(uk(email,"history:op:"+op),d); }
    public void putUserHistoryByType(String email, String type, List<QuantityMeasurementDTO> d) { safeSet(uk(email,"history:type:"+type),d); }
    public void putUserErrorHistory(String email, List<QuantityMeasurementDTO> d)               { safeSet(uk(email,"history:errored"),d); }
    public void putUserCount(String email, String op, long count)                               { safeSet(uk(email,"count:"+op),count); }

    /**
     * Invalidates all stale global and user-scoped keys after a save.
     */
    public void invalidateAfterSave(String operation, String measurementType,
                                    boolean isError, String userEmail) {
        safeDelete(HISTORY_OP_KEY   + operation);
        safeDelete(HISTORY_TYPE_KEY + measurementType);
        safeDelete(COUNT_KEY        + operation);
        if (isError) safeDelete(HISTORY_ERR_KEY);

        if (userEmail != null) {
            safeDelete(uk(userEmail, "history:all"));
            safeDelete(uk(userEmail, "history:op:"   + operation));
            safeDelete(uk(userEmail, "history:type:" + measurementType));
            safeDelete(uk(userEmail, "count:"        + operation));
            if (isError) safeDelete(uk(userEmail, "history:errored"));
        }
    }

    // Helpers
    private String uk(String email, String suffix) { return USER_PREFIX + email + ":" + suffix; }

    @SuppressWarnings("unchecked")
    private List<QuantityMeasurementDTO> tryGet(String key) {
        try {
            Object v = redis.opsForValue().get(key);
            if (v instanceof List<?> l) { log.debug("Cache HIT — {}", key); return (List<QuantityMeasurementDTO>) l; }
        } catch (Exception e) { log.warn("Redis read error ({}): {}", key, e.getMessage()); }
        log.debug("Cache MISS — {}", key);
        return null;
    }

    private Long tryGetLong(String key) {
        try {
            Object v = redis.opsForValue().get(key);
            if (v instanceof Long l)    return l;
            if (v instanceof Integer i) return i.longValue();
        } catch (Exception e) { log.warn("Redis read error ({}): {}", key, e.getMessage()); }
        return null;
    }

    private void safeSet(String key, Object value) {
        try { redis.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds)); log.debug("Cache SET — {}", key); }
        catch (Exception e) { log.warn("Redis write error ({}): {}", key, e.getMessage()); }
    }

    private void safeDelete(String key) {
        try { redis.delete(key); log.debug("Cache EVICT — {}", key); }
        catch (Exception e) { log.warn("Redis delete error ({}): {}", key, e.getMessage()); }
    }
}
