-- PostgreSQL Database Schema Migration Script
-- Migration from SQL Server to PostgreSQL
-- Database: modresorts
-- Schema: public

-- ============================================
-- Create Database (run as superuser)
-- ============================================
-- CREATE DATABASE modresorts
--     WITH 
--     OWNER = postgres
--     ENCODING = 'UTF8'
--     LC_COLLATE = 'en_US.UTF-8'
--     LC_CTYPE = 'en_US.UTF-8'
--     TABLESPACE = pg_default
--     CONNECTION LIMIT = -1;

-- ============================================
-- Connect to modresorts database
-- ============================================
-- \c modresorts

-- ============================================
-- Create Schema
-- ============================================
CREATE SCHEMA IF NOT EXISTS public;

-- ============================================
-- Set search path
-- ============================================
SET search_path TO public;

-- ============================================
-- Create Customer Table
-- ============================================
-- Drop table if exists (for clean migration)
DROP TABLE IF EXISTS public.customer CASCADE;

-- Create customer table with PostgreSQL data types
CREATE TABLE public.customer (
    id SERIAL PRIMARY KEY,
    info VARCHAR(500) NOT NULL,
    name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Create Indexes
-- ============================================
CREATE INDEX idx_customer_email ON public.customer(email);
CREATE INDEX idx_customer_name ON public.customer(name);

-- ============================================
-- Insert Sample Data
-- ============================================
INSERT INTO public.customer (info, name, email, phone, address) VALUES
    ('Premium customer with loyalty status', 'John Doe', 'john.doe@example.com', '+1-555-0101', '123 Main St, New York, NY 10001'),
    ('Regular customer', 'Jane Smith', 'jane.smith@example.com', '+1-555-0102', '456 Oak Ave, Los Angeles, CA 90001'),
    ('VIP customer with special privileges', 'Bob Johnson', 'bob.johnson@example.com', '+1-555-0103', '789 Pine Rd, Chicago, IL 60601'),
    ('New customer registration', 'Alice Williams', 'alice.williams@example.com', '+1-555-0104', '321 Elm St, Houston, TX 77001'),
    ('Corporate account holder', 'Charlie Brown', 'charlie.brown@example.com', '+1-555-0105', '654 Maple Dr, Phoenix, AZ 85001');

-- ============================================
-- Create Update Trigger for updated_at
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_customer_updated_at
    BEFORE UPDATE ON public.customer
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- Grant Permissions
-- ============================================
-- Grant permissions to application user (adjust username as needed)
-- GRANT CONNECT ON DATABASE modresorts TO app_user;
-- GRANT USAGE ON SCHEMA public TO app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;

-- ============================================
-- Verification Queries
-- ============================================
-- Verify table creation
SELECT table_name, table_type 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Verify data insertion
SELECT COUNT(*) as customer_count FROM public.customer;

-- Display sample data
SELECT id, info, name, email FROM public.customer LIMIT 5;

-- ============================================
-- Migration Notes
-- ============================================
-- 1. PostgreSQL uses lowercase identifiers by default
-- 2. SERIAL type replaces SQL Server IDENTITY
-- 3. VARCHAR without length defaults to unlimited in PostgreSQL
-- 4. TIMESTAMP replaces SQL Server DATETIME
-- 5. TEXT type for large text fields
-- 6. Triggers used for automatic timestamp updates
-- 7. Schema is explicitly set to 'public' (PostgreSQL default)
