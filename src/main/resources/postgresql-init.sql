-- PostgreSQL Database Initialization Script
-- ModResorts Application Database Schema

-- Create database (run as superuser)
-- CREATE DATABASE modresorts
--     WITH 
--     OWNER = postgres
--     ENCODING = 'UTF8'
--     LC_COLLATE = 'en_US.UTF-8'
--     LC_CTYPE = 'en_US.UTF-8'
--     TABLESPACE = pg_default
--     CONNECTION LIMIT = -1;

-- Connect to the database
\c modresorts;

-- Create schema (if not using public)
-- CREATE SCHEMA IF NOT EXISTS public;

-- Set search path
SET search_path TO public;

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS customer CASCADE;

-- Create customer table
-- PostgreSQL uses lowercase table and column names by default
-- Following PostgreSQL naming conventions (snake_case)
CREATE TABLE customer (
    id SERIAL PRIMARY KEY,
    info VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for better query performance
CREATE INDEX idx_customer_info ON customer(info);

-- Insert sample data
INSERT INTO customer (info) VALUES 
    ('John Doe - Premium Member - Las Vegas'),
    ('Jane Smith - Gold Member - San Francisco'),
    ('Bob Johnson - Silver Member - Miami'),
    ('Alice Williams - Premium Member - Paris'),
    ('Charlie Brown - Gold Member - Barcelona'),
    ('Diana Prince - Premium Member - Cork');

-- Create a function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_customer_updated_at 
    BEFORE UPDATE ON customer 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Grant permissions (adjust as needed for your environment)
-- GRANT ALL PRIVILEGES ON DATABASE modresorts TO postgres;
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;

-- Display table structure
\d customer;

-- Display sample data
SELECT * FROM customer;

-- PostgreSQL-specific optimizations
-- Analyze table for query planner
ANALYZE customer;

-- Create a view for customer summary (optional)
CREATE OR REPLACE VIEW customer_summary AS
SELECT 
    COUNT(*) as total_customers,
    COUNT(CASE WHEN info LIKE '%Premium%' THEN 1 END) as premium_members,
    COUNT(CASE WHEN info LIKE '%Gold%' THEN 1 END) as gold_members,
    COUNT(CASE WHEN info LIKE '%Silver%' THEN 1 END) as silver_members
FROM customer;

-- Display summary
SELECT * FROM customer_summary;

COMMENT ON TABLE customer IS 'Customer information table for ModResorts application';
COMMENT ON COLUMN customer.id IS 'Primary key - auto-incrementing customer ID';
COMMENT ON COLUMN customer.info IS 'Customer information string';
COMMENT ON COLUMN customer.created_at IS 'Timestamp when record was created';
COMMENT ON COLUMN customer.updated_at IS 'Timestamp when record was last updated';
