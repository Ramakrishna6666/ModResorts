-- PostgreSQL Database Schema for ModResorts Application
-- This script creates the necessary tables and indexes for the application

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS customer CASCADE;

-- Create customer table with PostgreSQL best practices
CREATE TABLE customer (
    id SERIAL PRIMARY KEY,
    info VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create index on created_at for better query performance
CREATE INDEX idx_customer_created_at ON customer(created_at);

-- Add comments for documentation
COMMENT ON TABLE customer IS 'Stores customer information for ModResorts application';
COMMENT ON COLUMN customer.id IS 'Auto-incrementing primary key';
COMMENT ON COLUMN customer.info IS 'Customer information text';
COMMENT ON COLUMN customer.created_at IS 'Timestamp when record was created';
COMMENT ON COLUMN customer.updated_at IS 'Timestamp when record was last updated';

-- Insert sample data for testing
INSERT INTO customer (info) VALUES 
    ('John Doe - Premium Member - john.doe@example.com'),
    ('Jane Smith - Gold Member - jane.smith@example.com'),
    ('Bob Johnson - Silver Member - bob.johnson@example.com'),
    ('Alice Williams - Premium Member - alice.williams@example.com'),
    ('Charlie Brown - Standard Member - charlie.brown@example.com');

-- Create function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to call the function before update
CREATE TRIGGER update_customer_updated_at
    BEFORE UPDATE ON customer
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Grant permissions (adjust based on your PostgreSQL user setup)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON customer TO modresorts_user;
-- GRANT USAGE, SELECT ON SEQUENCE customer_id_seq TO modresorts_user;
