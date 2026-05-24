-- PostgreSQL Data Migration Script
-- This script helps migrate data from SQL Server to PostgreSQL

-- ============================================================================
-- SECTION 1: Pre-Migration Checks
-- ============================================================================

-- Check PostgreSQL version (should be 16+)
SELECT version();

-- Check current database
SELECT current_database();

-- Check existing tables
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public';

-- ============================================================================
-- SECTION 2: Data Type Mapping Reference
-- ============================================================================

-- SQL Server to PostgreSQL type mappings:
-- INT IDENTITY(1,1)     -> SERIAL or INTEGER GENERATED ALWAYS AS IDENTITY
-- NVARCHAR(n)           -> VARCHAR(n) or TEXT
-- DATETIME              -> TIMESTAMP or TIMESTAMP WITH TIME ZONE
-- BIT                   -> BOOLEAN
-- UNIQUEIDENTIFIER      -> UUID
-- MONEY                 -> NUMERIC(19,4) or DECIMAL(19,4)
-- IMAGE/VARBINARY(MAX)  -> BYTEA
-- NTEXT                 -> TEXT

-- ============================================================================
-- SECTION 3: Sample Data Migration for Customer Table
-- ============================================================================

-- If migrating from SQL Server, you would export data to CSV and import here
-- Example CSV import:

-- Step 1: Create temporary staging table (if needed)
CREATE TEMP TABLE customer_staging (
    id INTEGER,
    info VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Step 2: Import from CSV (example)
-- COPY customer_staging(id, info, created_at, updated_at)
-- FROM '/path/to/customer_export.csv'
-- DELIMITER ','
-- CSV HEADER;

-- Step 3: Insert into main table with data transformation
-- INSERT INTO customer (id, info, created_at, updated_at)
-- SELECT 
--     id,
--     info,
--     created_at AT TIME ZONE 'UTC',  -- Convert timezone if needed
--     updated_at AT TIME ZONE 'UTC'
-- FROM customer_staging;

-- Step 4: Update sequence to match imported data
-- SELECT setval('customer_id_seq', (SELECT MAX(id) FROM customer));

-- ============================================================================
-- SECTION 4: Data Validation Queries
-- ============================================================================

-- Count records in customer table
SELECT COUNT(*) as total_records FROM customer;

-- Check for NULL values
SELECT 
    COUNT(*) as total_records,
    COUNT(id) as non_null_ids,
    COUNT(info) as non_null_info,
    COUNT(created_at) as non_null_created,
    COUNT(updated_at) as non_null_updated
FROM customer;

-- Check data ranges
SELECT 
    MIN(created_at) as earliest_record,
    MAX(created_at) as latest_record,
    MIN(id) as min_id,
    MAX(id) as max_id
FROM customer;

-- Sample data preview
SELECT * FROM customer LIMIT 10;

-- ============================================================================
-- SECTION 5: Common SQL Server to PostgreSQL Query Conversions
-- ============================================================================

-- SQL Server: SELECT TOP 10 * FROM customer
-- PostgreSQL:
SELECT * FROM customer LIMIT 10;

-- SQL Server: SELECT GETDATE()
-- PostgreSQL:
SELECT CURRENT_TIMESTAMP;

-- SQL Server: SELECT DATEADD(day, 7, created_at) FROM customer
-- PostgreSQL:
SELECT created_at + INTERVAL '7 days' FROM customer;

-- SQL Server: SELECT DATEDIFF(day, created_at, updated_at) FROM customer
-- PostgreSQL:
SELECT (updated_at - created_at) FROM customer;

-- SQL Server: SELECT LEN(info) FROM customer
-- PostgreSQL:
SELECT LENGTH(info) FROM customer;

-- SQL Server: SELECT ISNULL(info, 'N/A') FROM customer
-- PostgreSQL:
SELECT COALESCE(info, 'N/A') FROM customer;

-- SQL Server: String concatenation with +
-- SELECT first_name + ' ' + last_name FROM customer
-- PostgreSQL:
-- SELECT first_name || ' ' || last_name FROM customer;

-- ============================================================================
-- SECTION 6: Index Creation for Performance
-- ============================================================================

-- Create indexes for frequently queried columns
CREATE INDEX IF NOT EXISTS idx_customer_created_at ON customer(created_at);
CREATE INDEX IF NOT EXISTS idx_customer_updated_at ON customer(updated_at);

-- Full-text search index (if needed)
CREATE INDEX IF NOT EXISTS idx_customer_info_fts 
ON customer USING gin(to_tsvector('english', info));

-- ============================================================================
-- SECTION 7: Performance Optimization
-- ============================================================================

-- Analyze table for query optimization
ANALYZE customer;

-- Vacuum table to reclaim space
VACUUM customer;

-- Update table statistics
VACUUM ANALYZE customer;

-- ============================================================================
-- SECTION 8: Data Integrity Checks
-- ============================================================================

-- Check for duplicate IDs
SELECT id, COUNT(*) 
FROM customer 
GROUP BY id 
HAVING COUNT(*) > 1;

-- Check for orphaned records (if you have foreign keys)
-- Example: Check for customers without orders
-- SELECT c.id, c.info
-- FROM customer c
-- LEFT JOIN orders o ON c.id = o.customer_id
-- WHERE o.id IS NULL;

-- ============================================================================
-- SECTION 9: Backup and Recovery
-- ============================================================================

-- Create backup of customer table
-- pg_dump -U postgres -d modresorts -t customer -f customer_backup.sql

-- Restore from backup
-- psql -U postgres -d modresorts -f customer_backup.sql

-- Export to CSV
-- COPY customer TO '/path/to/customer_export.csv' DELIMITER ',' CSV HEADER;

-- Import from CSV
-- COPY customer FROM '/path/to/customer_import.csv' DELIMITER ',' CSV HEADER;

-- ============================================================================
-- SECTION 10: Post-Migration Verification
-- ============================================================================

-- Verify record counts match source database
SELECT 
    'customer' as table_name,
    COUNT(*) as record_count,
    pg_size_pretty(pg_total_relation_size('customer')) as table_size
FROM customer;

-- Check for data quality issues
SELECT 
    COUNT(*) as total_records,
    COUNT(DISTINCT id) as unique_ids,
    COUNT(CASE WHEN info IS NULL OR info = '' THEN 1 END) as empty_info,
    MIN(LENGTH(info)) as min_info_length,
    MAX(LENGTH(info)) as max_info_length,
    AVG(LENGTH(info))::INTEGER as avg_info_length
FROM customer;

-- Verify timestamps are in correct timezone
SELECT 
    id,
    info,
    created_at,
    updated_at,
    EXTRACT(TIMEZONE FROM created_at) as created_tz,
    EXTRACT(TIMEZONE FROM updated_at) as updated_tz
FROM customer
LIMIT 5;

-- ============================================================================
-- SECTION 11: Cleanup
-- ============================================================================

-- Drop staging tables if used
-- DROP TABLE IF EXISTS customer_staging;

-- Reset sequences if needed
-- SELECT setval('customer_id_seq', (SELECT MAX(id) FROM customer));

-- ============================================================================
-- SECTION 12: Migration Completion Checklist
-- ============================================================================

-- [ ] All tables created successfully
-- [ ] All data imported without errors
-- [ ] Record counts match source database
-- [ ] Indexes created for performance
-- [ ] Foreign key constraints validated
-- [ ] Data types converted correctly
-- [ ] Timestamps in correct timezone
-- [ ] No NULL values in NOT NULL columns
-- [ ] Sequences updated to correct values
-- [ ] Performance tested with sample queries
-- [ ] Backup created before going live

-- ============================================================================
-- NOTES
-- ============================================================================

-- 1. Always test migration on a copy of production data first
-- 2. Document any data transformations applied during migration
-- 3. Keep SQL Server database available for rollback if needed
-- 4. Monitor PostgreSQL performance after migration
-- 5. Update application connection strings after successful migration
-- 6. Train team on PostgreSQL-specific features and syntax differences

-- ============================================================================
-- END OF MIGRATION SCRIPT
-- ============================================================================
