-- PostgreSQL Database Migration Script for ModResorts Application
-- Target Database: PostgreSQL 16
-- Schema: public

-- ============================================================================
-- SCHEMA SETUP
-- ============================================================================

-- Create schema if not exists (public schema is default in PostgreSQL)
CREATE SCHEMA IF NOT EXISTS public;

-- Set search path to public schema
SET search_path TO public;

-- ============================================================================
-- TABLE DEFINITIONS
-- ============================================================================

-- Customer table with PostgreSQL-specific features
CREATE TABLE IF NOT EXISTS customer (
    id SERIAL PRIMARY KEY,
    info TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- Add comments for documentation (PostgreSQL feature)
COMMENT ON TABLE customer IS 'Stores customer information for ModResorts application';
COMMENT ON COLUMN customer.id IS 'Auto-incrementing primary key';
COMMENT ON COLUMN customer.info IS 'Customer information in text format';
COMMENT ON COLUMN customer.created_at IS 'Timestamp when record was created';
COMMENT ON COLUMN customer.updated_at IS 'Timestamp when record was last updated';
COMMENT ON COLUMN customer.is_active IS 'Flag indicating if customer is active';

-- ============================================================================
-- INDEXES
-- ============================================================================

-- Index for faster queries on active customers
CREATE INDEX IF NOT EXISTS idx_customer_active 
ON customer(is_active) 
WHERE is_active = true;

-- Index for timestamp-based queries
CREATE INDEX IF NOT EXISTS idx_customer_created_at 
ON customer(created_at DESC);

-- Full-text search index on info column (PostgreSQL specific)
CREATE INDEX IF NOT EXISTS idx_customer_info_fts 
ON customer USING gin(to_tsvector('english', info));

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- Function to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to call the update function
DROP TRIGGER IF EXISTS trigger_update_customer_timestamp ON customer;
CREATE TRIGGER trigger_update_customer_timestamp
    BEFORE UPDATE ON customer
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- SAMPLE DATA (Optional - for testing)
-- ============================================================================

-- Insert sample customer data
INSERT INTO customer (info) VALUES 
    ('John Doe - Premium Member - john.doe@example.com'),
    ('Jane Smith - Gold Member - jane.smith@example.com'),
    ('Bob Johnson - Silver Member - bob.johnson@example.com'),
    ('Alice Williams - Premium Member - alice.williams@example.com'),
    ('Charlie Brown - Standard Member - charlie.brown@example.com')
ON CONFLICT DO NOTHING;

-- ============================================================================
-- VIEWS
-- ============================================================================

-- View for active customers only
CREATE OR REPLACE VIEW active_customers AS
SELECT 
    id,
    info,
    created_at,
    updated_at
FROM customer
WHERE is_active = true
ORDER BY created_at DESC;

COMMENT ON VIEW active_customers IS 'View showing only active customers';

-- ============================================================================
-- GRANTS AND PERMISSIONS
-- ============================================================================

-- Grant appropriate permissions to application user
-- Note: Replace 'modresorts_user' with your actual database user
-- GRANT SELECT, INSERT, UPDATE, DELETE ON customer TO modresorts_user;
-- GRANT USAGE, SELECT ON SEQUENCE customer_id_seq TO modresorts_user;
-- GRANT SELECT ON active_customers TO modresorts_user;

-- ============================================================================
-- PERFORMANCE OPTIMIZATION
-- ============================================================================

-- Analyze table for query optimization
ANALYZE customer;

-- Vacuum table to reclaim storage
VACUUM ANALYZE customer;

-- ============================================================================
-- MIGRATION VERIFICATION
-- ============================================================================

-- Verify table structure
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'customer'
ORDER BY ordinal_position;

-- Verify indexes
SELECT 
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'customer'
  AND schemaname = 'public';

-- Check row count
SELECT COUNT(*) as total_customers FROM customer;
SELECT COUNT(*) as active_customers FROM customer WHERE is_active = true;

-- ============================================================================
-- ROLLBACK SCRIPT (if needed)
-- ============================================================================

-- Uncomment the following lines to rollback the migration:
-- DROP VIEW IF EXISTS active_customers;
-- DROP TRIGGER IF EXISTS trigger_update_customer_timestamp ON customer;
-- DROP FUNCTION IF EXISTS update_updated_at_column();
-- DROP TABLE IF EXISTS customer CASCADE;
