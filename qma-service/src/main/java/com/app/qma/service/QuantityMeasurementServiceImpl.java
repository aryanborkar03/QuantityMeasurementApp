package com.app.qma.service;

import com.app.qma.dto.request.QuantityMeasurementDTO;
import com.app.qma.dto.response.QuantityDTO;
import com.app.qma.entity.QuantityMeasurementEntity;
import com.app.qma.exception.QuantityMeasurementException;
import com.app.qma.model.QuantityModel;
import com.app.qma.repository.QuantityMeasurementRepository;
import com.app.qma.unit.IMeasurable;
import com.app.qma.unit.LengthUnit;
import com.app.qma.unit.TemperatureUnit;
import com.app.qma.unit.VolumeUnit;
import com.app.qma.unit.WeightUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.DoubleBinaryOperator;

/**
 * Core QMA business logic with Redis caching and per-user history.
 *
 * User identity is resolved from the SecurityContext (populated by GatewayAuthFilter
 * from the X-Auth-User-Email header injected by the api-gateway). For unauthenticated
 * public ops the userEmail is null and the record is still saved for admin visibility.
 *
 * Every write:
 *   1. Execute calculation
 *   2. Persist entity (with userEmail stamped)
 *   3. Invalidate global + user-scoped Redis keys
 *
 * Every read (history / count):
 *   1. Check user-scoped Redis key  → return on HIT
 *   2. Query DB with userEmail filter
 *   3. Write result back to Redis
 */
@Slf4j
@Service
public class QuantityMeasurementServiceImpl implements IQuantityMeasurementService {

    private static final double EPSILON = 1e-6;

    private final QuantityMeasurementRepository repository;
    private final CacheService cache;

    public QuantityMeasurementServiceImpl(QuantityMeasurementRepository repository,
                                          CacheService cache) {
        this.repository = repository;
        this.cache      = cache;
    }

    private enum Operation    { COMPARE, CONVERT, ADD, SUBTRACT, DIVIDE }
    private enum ArithmeticOp { ADD, SUBTRACT, DIVIDE }

    // ── Write operations ──────────────────────────────────────────────────────

    @Override
    public QuantityMeasurementDTO compare(QuantityDTO thisQ, QuantityDTO thatQ) {
        String email = currentUserEmail();
        QuantityModel<IMeasurable> q1 = toModel(thisQ), q2 = toModel(thatQ);
        try {
            assertSameType(q1, q2, "compare");
            boolean result = compareBaseValues(q1, q2);
            QuantityMeasurementEntity entity = buildEntity(q1, q2, Operation.COMPARE,
                    String.valueOf(result), null, null, null, false, null, email);
            saveAndInvalidate(entity, q1, email);
            log.debug("COMPARE {} vs {} => {} [user={}]", q1, q2, result, email);
            return QuantityMeasurementDTO.fromEntity(entity);
        } catch (QuantityMeasurementException e) { saveError(q1, q2, Operation.COMPARE, e.getMessage(), email); throw e; }
        catch (Exception e)                       { saveError(q1, q2, Operation.COMPARE, e.getMessage(), email);
                                                    throw new QuantityMeasurementException("compare Error: " + e.getMessage(), e); }
    }

    @Override
    public QuantityMeasurementDTO convert(QuantityDTO thisQ, QuantityDTO targetQ) {
        String email = currentUserEmail();
        QuantityModel<IMeasurable> source = toModel(thisQ), target = toModel(targetQ);
        try {
            double result = (source.getUnit() instanceof TemperatureUnit)
                    ? convertTemp(source, target.getUnit())
                    : target.getUnit().convertFromBaseUnit(source.getUnit().convertToBaseUnit(source.getValue()));
            QuantityMeasurementEntity entity = buildEntity(source, target, Operation.CONVERT,
                    null, result, target.getUnit().getUnitName(),
                    target.getUnit().getMeasurementType(), false, null, email);
            saveAndInvalidate(entity, source, email);
            return QuantityMeasurementDTO.fromEntity(entity);
        } catch (Exception e) { saveError(source, target, Operation.CONVERT, e.getMessage(), email);
                                 throw new QuantityMeasurementException("convert Error: " + e.getMessage(), e); }
    }

    @Override
    public QuantityMeasurementDTO add(QuantityDTO thisQ, QuantityDTO thatQ) {
        return add(thisQ, thatQ, thisQ);
    }

    @Override
    public QuantityMeasurementDTO add(QuantityDTO thisQ, QuantityDTO thatQ, QuantityDTO targetQ) {
        String email = currentUserEmail();
        QuantityModel<IMeasurable> q1 = toModel(thisQ), q2 = toModel(thatQ), target = toModel(targetQ);
        try {
            validateArithmetic(q1, q2, target.getUnit(), true);
            double result = target.getUnit().convertFromBaseUnit(arithmetic(q1, q2, ArithmeticOp.ADD));
            QuantityMeasurementEntity entity = buildEntity(q1, q2, Operation.ADD,
                    null, result, target.getUnit().getUnitName(),
                    target.getUnit().getMeasurementType(), false, null, email);
            saveAndInvalidate(entity, q1, email);
            return QuantityMeasurementDTO.fromEntity(entity);
        } catch (QuantityMeasurementException | IllegalArgumentException | UnsupportedOperationException e) {
            saveError(q1, q2, Operation.ADD, e.getMessage(), email);
            if (e instanceof QuantityMeasurementException qme) throw qme;
            throw new QuantityMeasurementException("add Error: " + e.getMessage(), e);
        } catch (Exception e) { saveError(q1, q2, Operation.ADD, e.getMessage(), email);
                                 throw new QuantityMeasurementException("add Error: " + e.getMessage(), e); }
    }

    @Override
    public QuantityMeasurementDTO subtract(QuantityDTO thisQ, QuantityDTO thatQ) {
        return subtract(thisQ, thatQ, thisQ);
    }

    @Override
    public QuantityMeasurementDTO subtract(QuantityDTO thisQ, QuantityDTO thatQ, QuantityDTO targetQ) {
        String email = currentUserEmail();
        QuantityModel<IMeasurable> q1 = toModel(thisQ), q2 = toModel(thatQ), target = toModel(targetQ);
        try {
            validateArithmetic(q1, q2, target.getUnit(), true);
            double result = target.getUnit().convertFromBaseUnit(arithmetic(q1, q2, ArithmeticOp.SUBTRACT));
            QuantityMeasurementEntity entity = buildEntity(q1, q2, Operation.SUBTRACT,
                    null, result, target.getUnit().getUnitName(),
                    target.getUnit().getMeasurementType(), false, null, email);
            saveAndInvalidate(entity, q1, email);
            return QuantityMeasurementDTO.fromEntity(entity);
        } catch (QuantityMeasurementException | IllegalArgumentException | UnsupportedOperationException e) {
            saveError(q1, q2, Operation.SUBTRACT, e.getMessage(), email);
            if (e instanceof QuantityMeasurementException qme) throw qme;
            throw new QuantityMeasurementException("subtract Error: " + e.getMessage(), e);
        } catch (Exception e) { saveError(q1, q2, Operation.SUBTRACT, e.getMessage(), email);
                                 throw new QuantityMeasurementException("subtract Error: " + e.getMessage(), e); }
    }

    @Override
    public QuantityMeasurementDTO divide(QuantityDTO thisQ, QuantityDTO thatQ) {
        String email = currentUserEmail();
        QuantityModel<IMeasurable> q1 = toModel(thisQ), q2 = toModel(thatQ);
        try {
            validateArithmetic(q1, q2, null, false);
            double result = arithmetic(q1, q2, ArithmeticOp.DIVIDE);
            QuantityMeasurementEntity entity = buildEntity(q1, q2, Operation.DIVIDE,
                    null, result, null, null, false, null, email);
            saveAndInvalidate(entity, q1, email);
            return QuantityMeasurementDTO.fromEntity(entity);
        } catch (ArithmeticException | QuantityMeasurementException e) { saveError(q1, q2, Operation.DIVIDE, e.getMessage(), email); throw e; }
        catch (Exception e) { saveError(q1, q2, Operation.DIVIDE, e.getMessage(), email);
                               throw new QuantityMeasurementException("divide Error: " + e.getMessage(), e); }
    }

    // ── Global history (admin / all-users) ────────────────────────────────────

    @Override
    public List<QuantityMeasurementDTO> getHistoryByOperation(String operation) {
        List<QuantityMeasurementDTO> cached = cache.getHistoryByOp(operation);
        if (cached != null) return cached;
        List<QuantityMeasurementDTO> result = QuantityMeasurementDTO.fromEntityList(
                repository.findByOperation(operation));
        cache.putHistoryByOp(operation, result);
        return result;
    }

    @Override
    public List<QuantityMeasurementDTO> getHistoryByMeasurementType(String measurementType) {
        List<QuantityMeasurementDTO> cached = cache.getHistoryByType(measurementType);
        if (cached != null) return cached;
        List<QuantityMeasurementDTO> result = QuantityMeasurementDTO.fromEntityList(
                repository.findByThisMeasurementType(measurementType));
        cache.putHistoryByType(measurementType, result);
        return result;
    }

    @Override
    public List<QuantityMeasurementDTO> getErrorHistory() {
        List<QuantityMeasurementDTO> cached = cache.getErrorHistory();
        if (cached != null) return cached;
        List<QuantityMeasurementDTO> result = QuantityMeasurementDTO.fromEntityList(
                repository.findByErrorTrue());
        cache.putErrorHistory(result);
        return result;
    }

    @Override
    public long getOperationCount(String operation) {
        Long cached = cache.getCount(operation);
        if (cached != null) return cached;
        long count = repository.countByOperationAndErrorFalse(operation);
        cache.putCount(operation, count);
        return count;
    }

    // ── User-scoped history ───────────────────────────────────────────────────

    @Override
    public List<QuantityMeasurementDTO> getMyHistory(String userEmail) {
        List<QuantityMeasurementDTO> cached = cache.getUserHistory(userEmail);
        if (cached != null) return cached;
        List<QuantityMeasurementDTO> result = QuantityMeasurementDTO.fromEntityList(
                repository.findByUserEmail(userEmail));
        cache.putUserHistory(userEmail, result);
        return result;
    }

    @Override
    public List<QuantityMeasurementDTO> getMyHistoryByOperation(String userEmail, String operation) {
        List<QuantityMeasurementDTO> cached = cache.getUserHistoryByOp(userEmail, operation);
        if (cached != null) return cached;
        List<QuantityMeasurementDTO> result = QuantityMeasurementDTO.fromEntityList(
                repository.findByUserEmailAndOperation(userEmail, operation));
        cache.putUserHistoryByOp(userEmail, operation, result);
        return result;
    }

    @Override
    public List<QuantityMeasurementDTO> getMyHistoryByMeasurementType(String userEmail, String measurementType) {
        List<QuantityMeasurementDTO> cached = cache.getUserHistoryByType(userEmail, measurementType);
        if (cached != null) return cached;
        List<QuantityMeasurementDTO> result = QuantityMeasurementDTO.fromEntityList(
                repository.findByUserEmailAndThisMeasurementType(userEmail, measurementType));
        cache.putUserHistoryByType(userEmail, measurementType, result);
        return result;
    }

    @Override
    public List<QuantityMeasurementDTO> getMyErrorHistory(String userEmail) {
        List<QuantityMeasurementDTO> cached = cache.getUserErrorHistory(userEmail);
        if (cached != null) return cached;
        List<QuantityMeasurementDTO> result = QuantityMeasurementDTO.fromEntityList(
                repository.findByUserEmailAndErrorTrue(userEmail));
        cache.putUserErrorHistory(userEmail, result);
        return result;
    }

    @Override
    public long getMyOperationCount(String userEmail, String operation) {
        Long cached = cache.getUserCount(userEmail, operation);
        if (cached != null) return cached;
        long count = repository.countByUserEmailAndOperationAndErrorFalse(userEmail, operation);
        cache.putUserCount(userEmail, operation, count);
        return count;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Resolves the currently authenticated user's email from the SecurityContext.
     * Returns null for anonymous (unauthenticated) requests on public endpoints.
     */
    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName(); // GatewayAuthFilter sets principal to the email string
        }
        return null;
    }

    private void saveAndInvalidate(QuantityMeasurementEntity entity,
                                   QuantityModel<IMeasurable> q1, String userEmail) {
        repository.save(entity);
        cache.invalidateAfterSave(
                entity.getOperation(),
                q1.getUnit().getMeasurementType(),
                entity.isError(),
                userEmail);
    }

    private QuantityModel<IMeasurable> toModel(QuantityDTO dto) {
        if (dto == null) throw new IllegalArgumentException("QuantityDTO cannot be null");
        return new QuantityModel<>(dto.getValue(), resolveUnit(dto.getMeasurementType(), dto.getUnit()));
    }

    private IMeasurable resolveUnit(String type, String unit) {
        return switch (type) {
            case "LengthUnit"      -> LengthUnit.valueOf(unit);
            case "WeightUnit"      -> WeightUnit.valueOf(unit);
            case "VolumeUnit"      -> VolumeUnit.valueOf(unit);
            case "TemperatureUnit" -> TemperatureUnit.valueOf(unit);
            default -> throw new IllegalArgumentException("Unsupported measurement type: " + type);
        };
    }

    private <U extends IMeasurable> boolean compareBaseValues(QuantityModel<U> q1, QuantityModel<U> q2) {
        return Math.abs(q1.getUnit().convertToBaseUnit(q1.getValue())
                      - q2.getUnit().convertToBaseUnit(q2.getValue())) < EPSILON;
    }

    private <U extends IMeasurable> double convertTemp(QuantityModel<U> source, U target) {
        return target.convertFromBaseUnit(source.getUnit().convertToBaseUnit(source.getValue()));
    }

    private <U extends IMeasurable> void assertSameType(QuantityModel<U> q1, QuantityModel<U> q2, String op) {
        String t1 = q1.getUnit().getMeasurementType(), t2 = q2.getUnit().getMeasurementType();
        if (!t1.equals(t2))
            throw new QuantityMeasurementException(op + " Error: Cannot operate on different categories: " + t1 + " and " + t2);
    }

    private <U extends IMeasurable> void validateArithmetic(QuantityModel<U> q1, QuantityModel<U> q2,
                                                             U targetUnit, boolean targetRequired) {
        if (q1 == null || q2 == null) throw new IllegalArgumentException("Operands cannot be null");
        String t1 = q1.getUnit().getMeasurementType(), t2 = q2.getUnit().getMeasurementType();
        if (!t1.equals(t2))
            throw new IllegalArgumentException("Cannot perform arithmetic on different categories: " + t1 + " and " + t2);
        if ("TemperatureUnit".equals(t1))
            throw new UnsupportedOperationException("Arithmetic is not supported for temperature units");
        if (targetRequired && targetUnit == null)
            throw new IllegalArgumentException("Target unit is required");
    }

    private <U extends IMeasurable> double arithmetic(QuantityModel<U> q1, QuantityModel<U> q2, ArithmeticOp op) {
        double b1 = q1.getUnit().convertToBaseUnit(q1.getValue());
        double b2 = q2.getUnit().convertToBaseUnit(q2.getValue());
        if (op == ArithmeticOp.DIVIDE && b2 == 0) throw new ArithmeticException("Division by zero is not allowed");
        DoubleBinaryOperator fn = switch (op) {
            case ADD      -> Double::sum;
            case SUBTRACT -> (a, b) -> a - b;
            case DIVIDE   -> (a, b) -> a / b;
        };
        return fn.applyAsDouble(b1, b2);
    }

    private QuantityMeasurementEntity buildEntity(
            QuantityModel<IMeasurable> q1, QuantityModel<IMeasurable> q2,
            Operation op, String resultString, Double resultValue,
            String resultUnit, String resultType, boolean isError,
            String errorMessage, String userEmail) {
        QuantityMeasurementEntity e = new QuantityMeasurementEntity();
        e.setUserEmail(userEmail);
        e.setThisValue(q1.getValue());
        e.setThisUnit(q1.getUnit().getUnitName());
        e.setThisMeasurementType(q1.getUnit().getMeasurementType());
        e.setThatValue(q2.getValue());
        e.setThatUnit(q2.getUnit().getUnitName());
        e.setThatMeasurementType(q2.getUnit().getMeasurementType());
        e.setOperation(op.name().toLowerCase());
        e.setResultString(resultString);
        e.setResultValue(resultValue);
        e.setResultUnit(resultUnit);
        e.setResultMeasurementType(resultType);
        e.setError(isError);
        e.setErrorMessage(errorMessage);
        return e;
    }

    private void saveError(QuantityModel<IMeasurable> q1, QuantityModel<IMeasurable> q2,
                           Operation op, String msg, String userEmail) {
        try {
            QuantityMeasurementEntity e = buildEntity(q1, q2, op, null, null, null, null, true, msg, userEmail);
            saveAndInvalidate(e, q1, userEmail);
        } catch (Exception ex) {
            log.error("Failed to persist error entity: {}", ex.getMessage());
        }
    }
}
