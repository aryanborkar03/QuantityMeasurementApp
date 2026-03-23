
-- =============================================================================
-- Quantity Measurement Application — Database Schema
-- UC16: JDBC Integration for Persistent Storage
--
-- This schema is provided for reference/MySQL use. When using H2, the
-- QuantityMeasurementDatabaseRepository auto-creates the table at startup
-- using the embedded CREATE TABLE IF NOT EXISTS statement.
--
-- For MySQL, run this script once to initialize the database:
--   mysql -u root -p < schema.sql
-- =============================================================================

-- Create database (MySQL only — H2 does not need this)
-- CREATE DATABASE IF NOT EXISTS quantity_measurement;
-- USE quantity_measurement;

-- =============================================================================
-- Main measurement entity table
-- Stores every measurement operation result (compare, convert, add, etc.)
-- =============================================================================
CREATE TABLE IF NOT EXISTS quantity_measurement_entity (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    this_value            DOUBLE       NOT NULL,
    this_unit             VARCHAR(50)  NOT NULL,
    this_measurement_type VARCHAR(50)  NOT NULL,
    that_value            DOUBLE,
    that_unit             VARCHAR(50),
    that_measurement_type VARCHAR(50),
    operation             VARCHAR(20)  NOT NULL,
    result_value          DOUBLE,
    result_unit           VARCHAR(50),
    result_measurement_type VARCHAR(50),
    result_string         VARCHAR(255),
    is_error              BOOLEAN      DEFAULT FALSE,
    error_message         VARCHAR(500),
    created_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_operation        (operation),
    INDEX idx_measurement_type (this_measurement_type),
    INDEX idx_created_at       (created_at)
);
   
-- =============================================================================
-- History/audit table for tracking entity change counts
-- Records how many times each entity has been referenced in operations
-- =============================================================================
CREATE TABLE IF NOT EXISTS quantity_measurement_history (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_id       BIGINT NOT NULL,
    operation_count INT    DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (entity_id) REFERENCES quantity_measurement_entity(id)
);
