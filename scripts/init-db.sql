-- Initialize the tracking number database
CREATE TABLE IF NOT EXISTS tracking_numbers (
    id BIGSERIAL PRIMARY KEY,
    tracking_number VARCHAR(16) NOT NULL UNIQUE,
    origin_country_id VARCHAR(2) NOT NULL,
    destination_country_id VARCHAR(2) NOT NULL,
    weight DECIMAL(10,3) NOT NULL,
    customer_id UUID NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_slug VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_tracking_number ON tracking_numbers(tracking_number);
CREATE INDEX IF NOT EXISTS idx_customer_id ON tracking_numbers(customer_id);
CREATE INDEX IF NOT EXISTS idx_created_at ON tracking_numbers(created_at);
CREATE INDEX IF NOT EXISTS idx_origin_dest ON tracking_numbers(origin_country_id, destination_country_id);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger for updating updated_at
DROP TRIGGER IF EXISTS update_tracking_numbers_updated_at ON tracking_numbers;
CREATE TRIGGER update_tracking_numbers_updated_at
    BEFORE UPDATE ON tracking_numbers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
