-- ============================================================
--  sales_manager – full schema (structure only, no data)
-- ============================================================

CREATE SCHEMA IF NOT EXISTS sales_manager;
SET search_path TO sales_manager;

-- ------------------------------------------------------------
--  Authentication
-- ------------------------------------------------------------

CREATE TABLE users (
    username   VARCHAR(50) PRIMARY KEY,
    password   VARCHAR(500) NOT NULL,
    enabled    BOOLEAN      NOT NULL,
    employee_id BIGINT
);

CREATE TABLE authorities (
    username  VARCHAR(50) NOT NULL REFERENCES users(username),
    authority VARCHAR(50) NOT NULL
);

CREATE UNIQUE INDEX ix_auth_username ON authorities (username, authority);

-- ------------------------------------------------------------
--  Category
-- ------------------------------------------------------------

CREATE TABLE category (
    category_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name            VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL UNIQUE
);

-- ------------------------------------------------------------
--  Supplier
-- ------------------------------------------------------------

CREATE TABLE supplier (
    supplier_id    BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name           VARCHAR(150) NOT NULL,
    phone          VARCHAR(30),
    email          VARCHAR(100),
    active         BOOLEAN DEFAULT TRUE,
    contact_person VARCHAR(150)
);

-- ------------------------------------------------------------
--  Item
-- ------------------------------------------------------------

CREATE TABLE item (
    item_id       BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    code          VARCHAR(50)  NOT NULL UNIQUE,
    name          VARCHAR(200) NOT NULL,
    category_id   BIGINT       NOT NULL REFERENCES category(category_id),
    unit          VARCHAR(20)  NOT NULL,
    reorder_level NUMERIC(10,2) DEFAULT 0,
    active        BOOLEAN DEFAULT TRUE,
    supplier_id   BIGINT
);

CREATE INDEX idx_stock_item_date ON stock_transaction (item_id, txn_date);
CREATE INDEX idx_stock_txn_type  ON stock_transaction (txn_type);

-- ------------------------------------------------------------
--  Stock transaction
-- ------------------------------------------------------------

CREATE TABLE stock_transaction (
    stock_txn_id   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    item_id        BIGINT       NOT NULL REFERENCES item(item_id),
    txn_date       TIMESTAMP    NOT NULL,
    txn_type       CHAR(3)      NOT NULL,
    quantity       NUMERIC(12,2) NOT NULL,
    unit_cost      NUMERIC(12,2),
    reference_type VARCHAR(30),
    reference_id   BIGINT,
    remarks        VARCHAR(255),
    CONSTRAINT chk_txn_type CHECK (txn_type = ANY (ARRAY['IN '::CHAR(3), 'OUT'::CHAR(3)]))
);

-- ------------------------------------------------------------
--  Employee
-- ------------------------------------------------------------

CREATE TABLE employee (
    id                  BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    known_name          VARCHAR,
    full_name           VARCHAR,
    address_line1       VARCHAR,
    address_line2       VARCHAR,
    address_line3       VARCHAR,
    address_line4       VARCHAR,
    address_line5       VARCHAR,
    phone_mobile        VARCHAR,
    phone_home          VARCHAR,
    phone_office        VARCHAR,
    email_personal      VARCHAR,
    email_office        VARCHAR,
    date_of_birth       DATE,
    nic_number          VARCHAR,
    passport_number     VARCHAR,
    drivers_license_no  VARCHAR,
    designation         VARCHAR,
    date_joined         DATE,
    created_by          BIGINT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
--  Purchase order
-- ------------------------------------------------------------

CREATE TABLE purchase_order (
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    supplier_id BIGINT,
    order_date  DATE,
    status      VARCHAR(20),
    created_by  BIGINT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE purchase_order_item (
    id                 BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    purchase_order_key BIGINT,
    purchase_order_id  BIGINT,
    item_id            BIGINT,
    quantity           INTEGER,
    price              NUMERIC(10,2)
);

-- ------------------------------------------------------------
--  Goods received note (GRN)
-- ------------------------------------------------------------

CREATE TABLE grn (
    id                BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    purchase_order_id BIGINT,
    status            VARCHAR,
    received_date     DATE,
    supplier_id       BIGINT,
    employee_id       BIGINT,
    cash              NUMERIC(12,2),
    cheque            NUMERIC(12,2),
    credit            NUMERIC(12,2),
    total             NUMERIC(12,2),
    credit_due        DATE
);

CREATE TABLE grn_item (
    id            BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    grn_key       BIGINT,
    grn_id        BIGINT,
    item_id       BIGINT,
    item_name     VARCHAR,
    ordered_qty   NUMERIC(12,2),
    received_qty  NUMERIC(12,2),
    rejected_qty  NUMERIC(12,2),
    unit_price    NUMERIC(12,2),
    ordered_price NUMERIC(12,2)
);

-- ------------------------------------------------------------
--  Cash & cheque transactions
-- ------------------------------------------------------------

CREATE TABLE cash_transaction (
    id            BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    txn_date      DATE      DEFAULT CURRENT_DATE,
    txn_type      VARCHAR,
    amount        NUMERIC(12,2),
    ref_type      VARCHAR,
    ref_id        BIGINT,
    txn_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cheque_transactions (
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    txn_date        DATE,
    txn_type        VARCHAR,
    cheque_no       INTEGER,
    cheque_date     DATE,
    bank            CHAR(8),
    amount          NUMERIC(12,2),
    ref_type        VARCHAR,
    ref_id          BIGINT,
    clearing_status VARCHAR,
    txn_timestamp   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
--  Creditor / supplier ledger
-- ------------------------------------------------------------

CREATE TABLE creditor_transaction (
    id            BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    supplier_id   BIGINT,
    txn_date      DATE,
    txn_type      VARCHAR,        -- PAYABLE | PAYMENT
    amount        NUMERIC(12,2),
    due_date      DATE,
    ref_type      VARCHAR,        -- e.g. GRN
    ref_id        BIGINT,
    txn_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE VIEW creditor_balance AS
SELECT
    supplier_id,
    due_date,
    SUM(
        CASE
            WHEN txn_type = 'PAYABLE' THEN  amount
            WHEN txn_type = 'PAYMENT' THEN -amount
            ELSE 0
        END
    ) AS balance
FROM creditor_transaction
GROUP BY supplier_id, due_date;

-- ------------------------------------------------------------
--  Supplier payments
-- ------------------------------------------------------------

CREATE TABLE supplier_payment (
    id                   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    supplier_id          BIGINT,
    payment_method       VARCHAR,
    total_payment_amount NUMERIC(12,2),
    cheque_number        VARCHAR,
    bank                 VARCHAR,
    bank_account         VARCHAR,
    reference_number     VARCHAR,
    payment_date         DATE
);

CREATE TABLE supplier_payment_allocation (
    id               BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    payment_id       BIGINT REFERENCES supplier_payment(id),
    grn_id           BIGINT REFERENCES grn(id),
    allocated_amount NUMERIC(12,2)
);