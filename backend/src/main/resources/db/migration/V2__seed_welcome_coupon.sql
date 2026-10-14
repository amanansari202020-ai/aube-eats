INSERT INTO coupons (code, discount_percent, expires_at, active)
VALUES ('HELLOAUBE', 15.00, DATE_ADD(UTC_TIMESTAMP(6), INTERVAL 1 YEAR), TRUE);