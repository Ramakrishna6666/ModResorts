-- PostgreSQL Migration Script
-- Creates the CUSTOMER table for ModResorts application

-- Drop table if exists (for clean migration)
DROP TABLE IF EXISTS CUSTOMER CASCADE;

-- Create CUSTOMER table with PostgreSQL-specific features
CREATE TABLE CUSTOMER (
    id SERIAL PRIMARY KEY,
    info VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create index on info column for better query performance
CREATE INDEX idx_customer_info ON CUSTOMER(info);

-- Add comments for documentation
COMMENT ON TABLE CUSTOMER IS 'Stores customer information for ModResorts application';
COMMENT ON COLUMN CUSTOMER.id IS 'Primary key - auto-incrementing customer ID';
COMMENT ON COLUMN CUSTOMER.info IS 'Customer information text';
COMMENT ON COLUMN CUSTOMER.created_at IS 'Timestamp when record was created';
COMMENT ON COLUMN CUSTOMER.updated_at IS 'Timestamp when record was last updated';

-- Insert sample data for testing
INSERT INTO CUSTOMER (info) VALUES 
    ('John Doe - Premium Member - Las Vegas Resort'),
    ('Jane Smith - Gold Member - Miami Beach Resort'),
    ('Robert Johnson - Silver Member - San Francisco Resort'),
    ('Maria Garcia - Premium Member - Paris Resort'),
    ('David Lee - Gold Member - Barcelona Resort');

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
    BEFORE UPDATE ON CUSTOMER
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Grant permissions (adjust as needed for your environment)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON CUSTOMER TO modresorts_app;
-- GRANT USAGE, SELECT ON SEQUENCE customer_id_seq TO modresorts_app;
