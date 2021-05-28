--this extension is used by GIN index that allow using the index when a like is used
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_online_merchant_search_name ON online_merchant USING gin (searchable_name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_offline_merchant_search_name ON offline_merchant USING gin (searchable_name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_offline_merchant_lat_lon on offline_merchant (latitude, longitude);
