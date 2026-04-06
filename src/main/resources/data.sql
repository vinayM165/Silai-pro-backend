-- =============================================================================
-- SilaiPro – Seed Data
-- Default roles and admin user
-- =============================================================================

-- ── 1. Default Roles ─────────────────────────────────────────────────────────

INSERT INTO roles (name, permissions_json) VALUES
(
  'Admin',
  '["CUSTOMER_CREATE","CUSTOMER_EDIT","CUSTOMER_DELETE","CUSTOMER_VIEW","MEASUREMENT_ADD","MEASUREMENT_VIEW","MEASUREMENT_PRINT","MEASUREMENT_CONFIG","ORDER_CREATE","ORDER_EDIT","ORDER_DELETE","ORDER_VIEW","BILLING_CREATE","PAYMENT_RECORD","ACCOUNTS_VIEW","ACCOUNTS_REPORT","MESSAGE_SEND","STAFF_MANAGE","SETTINGS_MANAGE"]'
),
(
  'Manager',
  '["CUSTOMER_CREATE","CUSTOMER_EDIT","CUSTOMER_DELETE","CUSTOMER_VIEW","MEASUREMENT_ADD","MEASUREMENT_VIEW","MEASUREMENT_PRINT","MEASUREMENT_CONFIG","ORDER_CREATE","ORDER_EDIT","ORDER_DELETE","ORDER_VIEW","BILLING_CREATE","PAYMENT_RECORD","ACCOUNTS_VIEW","ACCOUNTS_REPORT","MESSAGE_SEND"]'
),
(
  'Staff',
  '["CUSTOMER_CREATE","CUSTOMER_EDIT","CUSTOMER_VIEW","MEASUREMENT_ADD","MEASUREMENT_VIEW","ORDER_CREATE","ORDER_EDIT","ORDER_VIEW","BILLING_CREATE","PAYMENT_RECORD","MESSAGE_SEND"]'
),
(
  'View Only',
  '["CUSTOMER_VIEW","MEASUREMENT_VIEW","MEASUREMENT_PRINT","ORDER_VIEW"]'
);

-- ── 2. Default Admin User ─────────────────────────────────────────────────────
-- Password: admin123  (BCrypt hash)
-- PIN:      1234      (BCrypt hash)
INSERT INTO users (name, phone, email, role_id, password_hash, pin_hash, is_active)
SELECT
  'Admin',
  '9999999999',
  'admin@silaipro.com',
  r.id,
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- admin123
  '$2a$10$d9rJEfbDuWBFMAtEVyxYjuoQnxRpMCbNz4SfRdHo6jGnnJ9cBOlCG', -- 1234
  TRUE
FROM roles r WHERE r.name = 'Admin';

-- ── 3. Default Shop Settings ──────────────────────────────────────────────────

INSERT INTO shop_settings (setting_key, setting_value) VALUES
('SHOP_NAME',              'SilaiPro Boutique'),
('SHOP_LOGO_URL',          ''),
('SHOP_ADDRESS',           ''),
('SHOP_PHONE',             ''),
('SHOP_WHATSAPP',          ''),
('GST_NUMBER',             ''),
('CURRENCY_SYMBOL',        '₹'),
('DEFAULT_UNIT',           'inches'),
('TAX_RATE',               '0'),
('INVOICE_PREFIX',         'INV'),
('ORDER_PREFIX',           'ORD'),
('SESSION_TIMEOUT_HOURS',  '8'),
('CUSTOMER_TYPES',         'Regular,VIP,Wholesale,Walk-in');
