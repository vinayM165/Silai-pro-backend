-- 1. roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    permissions_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE,
    role_id BIGINT,
    pin_hash VARCHAR(255),
    password_hash VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- 3. customers
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    whatsapp VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    city VARCHAR(100),
    profile_photo_url VARCHAR(255),
    date_of_birth DATE,
    customer_type VARCHAR(50),
    reference VARCHAR(100),
    notes TEXT,
    date_joined DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. measurement_categories
CREATE TABLE IF NOT EXISTS measurement_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. measurement_fields
CREATE TABLE IF NOT EXISTS measurement_fields (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_type VARCHAR(20) NOT NULL, -- Enum mapped as string
    unit VARCHAR(20),
    is_required BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    options_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES measurement_categories(id) ON DELETE CASCADE
);

-- 6. measurements
CREATE TABLE IF NOT EXISTS measurements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    taken_by BIGINT,
    taken_at TIMESTAMP,
    notes TEXT,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES measurement_categories(id),
    FOREIGN KEY (taken_by) REFERENCES users(id)
);

-- 7. measurement_values
CREATE TABLE IF NOT EXISTS measurement_values (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    measurement_id BIGINT NOT NULL,
    field_id BIGINT NOT NULL,
    field_value VARCHAR(255),
    FOREIGN KEY (measurement_id) REFERENCES measurements(id) ON DELETE CASCADE,
    FOREIGN KEY (field_id) REFERENCES measurement_fields(id)
);

-- 8. orders
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    order_no VARCHAR(50) UNIQUE NOT NULL,
    items_json TEXT,
    total_amount DECIMAL(10, 2),
    advance_paid DECIMAL(10, 2),
    status VARCHAR(30) NOT NULL,
    delivery_date DATE,
    priority VARCHAR(20),
    special_instructions TEXT,
    trial_date DATE,
    assigned_to BIGINT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (assigned_to) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 9. invoices
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    invoice_no VARCHAR(50) UNIQUE NOT NULL,
    subtotal DECIMAL(10, 2),
    discount DECIMAL(10, 2),
    tax_rate DECIMAL(5, 2),
    tax_amount DECIMAL(10, 2),
    total DECIMAL(10, 2),
    payment_status VARCHAR(20) NOT NULL,
    invoice_date DATE,
    due_date DATE,
    notes TEXT,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 10. payments
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    mode VARCHAR(30) NOT NULL,
    payment_date DATE NOT NULL,
    received_by BIGINT,
    receipt_no VARCHAR(50),
    notes TEXT,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    FOREIGN KEY (received_by) REFERENCES users(id)
);

-- 11. message_templates
CREATE TABLE IF NOT EXISTS message_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    variables_json TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 12. messages_sent
CREATE TABLE IF NOT EXISTS messages_sent (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    template_id BIGINT,
    content TEXT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_by BIGINT,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (template_id) REFERENCES message_templates(id),
    FOREIGN KEY (sent_by) REFERENCES users(id)
);

-- 13. shop_settings
CREATE TABLE IF NOT EXISTS shop_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 14. audit_logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    performed_by BIGINT,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (performed_by) REFERENCES users(id)
);

-- Indexes for performance
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_name ON customers(name);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_invoices_order_id ON invoices(order_id);
CREATE INDEX idx_invoices_payment_status ON invoices(payment_status);


