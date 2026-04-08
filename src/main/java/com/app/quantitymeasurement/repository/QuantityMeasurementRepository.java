package com.app.quantitymeasurement.repository;

import com.app.quantitymeasurement.entity.QuantityMeasurementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface QuantityMeasurementRepository
        extends JpaRepository<QuantityMeasurementEntity, Long> {

    /**
     * Returns all records whose {@code operation} field matches the given value.
     *
     * @param operation operation name to filter by (e.g., {@code "compare"}, {@code "add"})
     * @return matching records
     */
    List<QuantityMeasurementEntity> findByOperation(String operation);

    /**
     * Returns all records whose {@code thisMeasurementType} field matches the given value.
     *
     * @param measurementType measurement category to filter by (e.g., {@code "LengthUnit"})
     * @return matching records
     */
    List<QuantityMeasurementEntity> findByThisMeasurementType(String measurementType);

    /**
     * Returns all records created after the given date/time.
     * Useful for time-range queries and recent-operation reports.
     *
     * @param date lower bound (exclusive)
     * @return records with {@code createdAt} after {@code date}
     */
    List<QuantityMeasurementEntity> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Returns all successful (non-error) records for the given operation.
     *
     * @param operation operation name to filter by
     * @return non-error records for the operation
     */
    @Query("SELECT q FROM QuantityMeasurementEntity q " +
           "WHERE q.operation = :operation AND q.error = false")
    List<QuantityMeasurementEntity> findSuccessfulByOperation(@Param("operation") String operation);

    /**
     * Counts the number of successful (non-error) records for the given operation.
     *
     * @param operation operation name to count
     * @return count of successful records
     */
    long countByOperationAndErrorFalse(String operation);

    /**
     * Returns all records that represent failed operations ({@code error = true}).
     *
     * @return error records
     */
    List<QuantityMeasurementEntity> findByErrorTrue();
}
