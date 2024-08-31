CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_product_name_trigram ON products USING gin (name gin_trgm_ops);