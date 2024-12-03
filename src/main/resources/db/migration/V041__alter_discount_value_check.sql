ALTER TABLE discount
DROP
CONSTRAINT discount_discount_value_check,
    ADD CONSTRAINT discount_discount_value_check CHECK (discount_value IS NULL OR
                                                        (discount_value IS NOT NULL AND discount_value >= 1 AND discount_value <= 100));