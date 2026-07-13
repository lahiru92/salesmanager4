-- ============================================================
--  sales_manager – full schema (structure only, no data)
-- ============================================================

CREATE SCHEMA IF NOT EXISTS sales_manager;
SET search_path TO sales_manager;

-- ------------------------------------------------------------
--  Enums
-- ------------------------------------------------------------
CREATE TYPE payment_direction AS ENUM ('IN', 'OUT');
CREATE TYPE payment_type AS ENUM ('CASH', 'CHEQUE', 'BANK_TRANSFER');

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
    active              BOOLEAN DEFAULT TRUE,
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
    payment_method       VARCHAR, -- CASH, CHEQUE, BANK_TRANSFER
	direction            VARCHAR, -- IN, OUT (IN if supplier happens to pay me)
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


-- ------------------------------------------------------------
--  Views
-- -----------------------------------------------------------

-- =============================================================================
-- 1. OUTSTANDING GRNs PER SUPPLIER
--    One row per GRN that still has an unpaid balance.
--    Includes how much was originally on credit, how much has been paid,
--    and what remains — plus the due date and how many days overdue it is.
-- =============================================================================
CREATE VIEW OUTSTANDING_GRNS_PER_SUPPLIER AS
WITH grn_balances AS (
    SELECT
        g.id                                    AS grn_id,
        g.supplier_id,
        g.received_date,
        g.credit_due,
        g.total,
        g.credit                                AS credit_at_delivery,
        COALESCE(SUM(
		    CASE sp.direction
		        WHEN 'OUT' THEN  spa.allocated_amount   -- payment reduces what you owe
		        WHEN 'IN'  THEN -spa.allocated_amount   -- refund/credit increases what you owe
		    END
		), 0) AS paid_against_grn
    FROM grn g
    LEFT JOIN supplier_payment_allocation spa ON spa.grn_id = g.id
	LEFT JOIN supplier_payment sp             ON sp.id = spa.payment_id
    WHERE g.credit > 0                          -- only GRNs that had credit
    GROUP BY g.id, g.supplier_id, g.received_date, g.credit_due, g.total, g.credit
)
SELECT
    grn_id,
    supplier_id,
    received_date,
    credit_due,
    credit_at_delivery,
    paid_against_grn,
    credit_at_delivery - paid_against_grn                   AS outstanding_balance,
    CASE
        WHEN credit_due IS NULL THEN NULL
        ELSE (CURRENT_DATE - credit_due)
    END                                                     AS days_overdue,
    CASE
        WHEN credit_due IS NULL             THEN 'NO DUE DATE'
        WHEN CURRENT_DATE <= credit_due     THEN 'CURRENT'
        WHEN CURRENT_DATE - credit_due <= 30 THEN 'OVERDUE 1-30'
        WHEN CURRENT_DATE - credit_due <= 60 THEN 'OVERDUE 31-60'
        WHEN CURRENT_DATE - credit_due <= 90 THEN 'OVERDUE 61-90'
        ELSE                                     'OVERDUE 90+'
    END                                                     AS aging_bucket
FROM grn_balances
WHERE credit_at_delivery - paid_against_grn > 0             -- only open balances
ORDER BY supplier_id, credit_due NULLS LAST, grn_id;


-- =============================================================================
-- 2. TOTAL OUTSTANDING BALANCE PER SUPPLIER
--    One row per supplier — a summary of their entire open credit position.
-- =============================================================================

CREATE VIEW OUTSTANDING_BALANCE_PER_SUPPLIER AS
WITH grn_balances AS (
    SELECT
        g.supplier_id,
        g.credit                                AS credit_at_delivery,
        COALESCE(SUM(
		    CASE sp.direction
		        WHEN 'OUT' THEN  spa.allocated_amount   -- payment reduces what you owe
		        WHEN 'IN'  THEN -spa.allocated_amount   -- refund/credit increases what you owe
		    END
		), 0) AS paid_against_grn
    FROM grn g
    LEFT JOIN supplier_payment_allocation spa ON spa.grn_id = g.id
	LEFT JOIN supplier_payment sp             ON sp.id = spa.payment_id
    WHERE g.credit > 0
    GROUP BY g.id, g.supplier_id, g.credit
)
SELECT
    supplier_id,
    COUNT(*)                                            AS open_grn_count,
    SUM(credit_at_delivery)                             AS total_credit_issued,
    SUM(paid_against_grn)                               AS total_paid,
    SUM(credit_at_delivery - paid_against_grn)          AS total_outstanding
FROM grn_balances
WHERE credit_at_delivery - paid_against_grn > 0
GROUP BY supplier_id
ORDER BY total_outstanding DESC;


-- =============================================================================
-- 3. SUPPLIER AGING 
--    One row per supplier with the outstanding balance broken into time buckets:
--      CURRENT    — due date not yet passed (or no due date)
--      1-30       — 1 to 30 days overdue
--      31-60      — 31 to 60 days overdue
--      61-90      — 61 to 90 days overdue
--      90+        — more than 90 days overdue
-- =============================================================================
DROP VIEW IF EXISTS SUPPLIER_AGING;
CREATE VIEW SUPPLIER_AGING AS
WITH grn_balances AS (
    SELECT
        g.id                                    AS grn_id,
        g.supplier_id,
        g.credit_due,
        g.credit                                AS credit_at_delivery,
        COALESCE(SUM(
		    CASE sp.direction
		        WHEN 'OUT' THEN  spa.allocated_amount   -- payment reduces what we owe
		        WHEN 'IN'  THEN -spa.allocated_amount   -- refund/credit increases what we owe
		    END
		), 0) AS paid_against_grn
    FROM grn g
    LEFT JOIN supplier_payment_allocation spa ON spa.grn_id = g.id
	LEFT JOIN supplier_payment sp             ON sp.id = spa.payment_id
    WHERE g.credit > 0
    GROUP BY g.id, g.supplier_id, g.credit_due, g.credit
),
open_balances AS (
    SELECT
        supplier_id,
        credit_due,
        credit_at_delivery - paid_against_grn   AS outstanding
    FROM grn_balances
    WHERE credit_at_delivery - paid_against_grn > 0
)
SELECT
    b.supplier_id, 
	s.name                                                          AS supplier_name,

    -- Total outstanding
    SUM(outstanding)                                                AS total_outstanding,

    -- Current: not yet overdue (or no due date set)
    SUM(outstanding) FILTER (
        WHERE credit_due IS NULL OR CURRENT_DATE <= credit_due
    )                                                               AS current_amount,

    -- 1-30 days overdue
    SUM(outstanding) FILTER (
        WHERE credit_due IS NOT NULL
          AND CURRENT_DATE - credit_due BETWEEN 1 AND 30
    )                                                               AS overdue_1_30,

    -- 31-60 days overdue
    SUM(outstanding) FILTER (
        WHERE credit_due IS NOT NULL
          AND CURRENT_DATE - credit_due BETWEEN 31 AND 60
    )                                                               AS overdue_31_60,

    -- 61-90 days overdue
    SUM(outstanding) FILTER (
        WHERE credit_due IS NOT NULL
          AND CURRENT_DATE - credit_due BETWEEN 61 AND 90
    )                                                               AS overdue_61_90,

    -- Over 90 days
    SUM(outstanding) FILTER (
        WHERE credit_due IS NOT NULL
          AND CURRENT_DATE - credit_due > 90
    )                                                               AS overdue_90_plus

FROM open_balances b
LEFT JOIN supplier s ON s.supplier_id = b.supplier_id
GROUP BY b.supplier_id, s.name
ORDER BY total_outstanding DESC;


-- =============================================================================
-- 4 : UNALLOCATED PAYMENT CREDITS PER SUPPLIER
--    Identifies payments where the supplier was paid more than what has
--    been allocated to GRNs — i.e. the supplier has a credit balance in
--    our favour that can be applied to future invoices.
-- =============================================================================
CREATE VIEW UNALLOCATED_PAYMENT_CR_PER_SUPPLIER AS
SELECT
    sp.supplier_id,
    sp.id                                               AS payment_id,
    sp.payment_date,
    sp.payment_method,
    sp.total_payment_amount,
    COALESCE(SUM(spa.allocated_amount), 0)              AS allocated,
    CASE sp.direction
	    WHEN 'OUT' THEN sp.total_payment_amount - COALESCE(SUM(spa.allocated_amount), 0)
	    WHEN 'IN'  THEN NULL  -- IN payments are refunds, not credits to apply forward
	END AS unallocated_credit
FROM supplier_payment sp
LEFT JOIN supplier_payment_allocation spa ON spa.payment_id = sp.id
GROUP BY sp.id, sp.supplier_id, sp.payment_date, sp.payment_method, sp.total_payment_amount
HAVING sp.direction = 'OUT'
	AND sp.total_payment_amount - COALESCE(SUM(spa.allocated_amount), 0) > 0
ORDER BY sp.supplier_id, sp.payment_date;

-- ------------------------------------------------------------
--  Customer
-- ------------------------------------------------------------
CREATE TABLE customer (
    customer_id    BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name           VARCHAR(150) NOT NULL,
    phone          VARCHAR(30),
    email          VARCHAR(100),
    active         BOOLEAN DEFAULT TRUE,
    contact_person VARCHAR(150)
);

-- ------------------------------------------------------------
--  Invoice
-- ------------------------------------------------------------

CREATE TABLE invoice (
    id           BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    status       VARCHAR,
    invoice_date DATE,
    customer_id  BIGINT,
    employee_id  BIGINT,
    cash         NUMERIC(12,2),
    cheque       NUMERIC(12,2),
    credit       NUMERIC(12,2),
    total        NUMERIC(12,2),
    credit_due   DATE
);

CREATE TABLE invoice_item (
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    invoice_key BIGINT,
    invoice_id  BIGINT,
    item_id     BIGINT,
    item_name   VARCHAR,
    quantity    NUMERIC(12,2),
    free_qty    NUMERIC(12,2),
    unit_price  NUMERIC(12,2),
    discount    NUMERIC(12,2)
);

-- ------------------------------------------------------------
--  Customer payments (receipts)
-- ------------------------------------------------------------
CREATE TABLE customer_payment (
    id                   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    customer_id          BIGINT,
    payment_method       VARCHAR, -- CASH, CHEQUE, BANK_TRANSFER
    direction            VARCHAR, -- IN (customer pays us), OUT (we refund the customer)
    total_payment_amount NUMERIC(12,2),
    cheque_number        VARCHAR,
    bank                 VARCHAR,
    bank_account         VARCHAR,
    reference_number     VARCHAR,
    payment_date         DATE,
    collected_by         BIGINT   -- employee (salesman) who collected the money
);

CREATE TABLE customer_payment_allocation (
    id               BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    payment_id       BIGINT REFERENCES customer_payment(id),
    invoice_id       BIGINT REFERENCES invoice(id),
    allocated_amount NUMERIC(12,2)
);

-- =============================================================================
-- D1. OUTSTANDING INVOICES PER CUSTOMER
--     One row per invoice that still has an unpaid balance.
-- =============================================================================
CREATE VIEW OUTSTANDING_INVOICES_PER_CUSTOMER AS
WITH invoice_balances AS (
    SELECT
        i.id                                    AS invoice_id,
        i.customer_id,
        i.invoice_date,
        i.credit_due,
        i.total,
        i.credit                                AS credit_at_sale,
        COALESCE(SUM(
            CASE cp.direction
                WHEN 'IN'  THEN  cpa.allocated_amount   -- receipt reduces what they owe
                WHEN 'OUT' THEN -cpa.allocated_amount   -- refund increases what they owe
            END
        ), 0) AS paid_against_invoice
    FROM invoice i
    LEFT JOIN customer_payment_allocation cpa ON cpa.invoice_id = i.id
    LEFT JOIN customer_payment cp             ON cp.id = cpa.payment_id
    WHERE i.credit > 0                          -- only invoices that had credit
    GROUP BY i.id, i.customer_id, i.invoice_date, i.credit_due, i.total, i.credit
)
SELECT
    invoice_id,
    customer_id,
    invoice_date,
    credit_due,
    credit_at_sale,
    paid_against_invoice,
    credit_at_sale - paid_against_invoice                   AS outstanding_balance,
    CASE
        WHEN credit_due IS NULL THEN NULL
        ELSE (CURRENT_DATE - credit_due)
    END                                                     AS days_overdue,
    CASE
        WHEN credit_due IS NULL             THEN 'NO DUE DATE'
        WHEN CURRENT_DATE <= credit_due     THEN 'CURRENT'
        WHEN CURRENT_DATE - credit_due <= 30 THEN 'OVERDUE 1-30'
        WHEN CURRENT_DATE - credit_due <= 60 THEN 'OVERDUE 31-60'
        WHEN CURRENT_DATE - credit_due <= 90 THEN 'OVERDUE 61-90'
        ELSE                                     'OVERDUE 90+'
    END                                                     AS aging_bucket
FROM invoice_balances
WHERE credit_at_sale - paid_against_invoice > 0             -- only open balances
ORDER BY customer_id, credit_due NULLS LAST, invoice_id;


-- =============================================================================
-- D2. TOTAL OUTSTANDING BALANCE PER CUSTOMER
-- =============================================================================
CREATE VIEW OUTSTANDING_BALANCE_PER_CUSTOMER AS
WITH invoice_balances AS (
    SELECT
        i.customer_id,
        i.credit                                AS credit_at_sale,
        COALESCE(SUM(
            CASE cp.direction
                WHEN 'IN'  THEN  cpa.allocated_amount
                WHEN 'OUT' THEN -cpa.allocated_amount
            END
        ), 0) AS paid_against_invoice
    FROM invoice i
    LEFT JOIN customer_payment_allocation cpa ON cpa.invoice_id = i.id
    LEFT JOIN customer_payment cp             ON cp.id = cpa.payment_id
    WHERE i.credit > 0
    GROUP BY i.id, i.customer_id, i.credit
)
SELECT
    customer_id,
    COUNT(*)                                            AS open_invoice_count,
    SUM(credit_at_sale)                                 AS total_credit_issued,
    SUM(paid_against_invoice)                           AS total_paid,
    SUM(credit_at_sale - paid_against_invoice)          AS total_outstanding
FROM invoice_balances
WHERE credit_at_sale - paid_against_invoice > 0
GROUP BY customer_id
ORDER BY total_outstanding DESC;


-- =============================================================================
-- D3. CUSTOMER AGING
-- =============================================================================
CREATE VIEW CUSTOMER_AGING AS
WITH invoice_balances AS (
    SELECT
        i.id                                    AS invoice_id,
        i.customer_id,
        i.credit_due,
        i.credit                                AS credit_at_sale,
        COALESCE(SUM(
            CASE cp.direction
                WHEN 'IN'  THEN  cpa.allocated_amount
                WHEN 'OUT' THEN -cpa.allocated_amount
            END
        ), 0) AS paid_against_invoice
    FROM invoice i
    LEFT JOIN customer_payment_allocation cpa ON cpa.invoice_id = i.id
    LEFT JOIN customer_payment cp             ON cp.id = cpa.payment_id
    WHERE i.credit > 0
    GROUP BY i.id, i.customer_id, i.credit_due, i.credit
),
open_balances AS (
    SELECT
        customer_id,
        credit_due,
        credit_at_sale - paid_against_invoice   AS outstanding
    FROM invoice_balances
    WHERE credit_at_sale - paid_against_invoice > 0
)
SELECT
    b.customer_id,
    c.name                                                          AS customer_name,

    SUM(outstanding)                                                AS total_outstanding,

    SUM(outstanding) FILTER (
        WHERE credit_due IS NULL OR CURRENT_DATE <= credit_due
    )                                                               AS current_amount,

    SUM(outstanding) FILTER (
        WHERE credit_due IS NOT NULL
          AND CURRENT_DATE - credit_due BETWEEN 1 AND 30
    )                                                               AS overdue_1_30,

    SUM(outstanding) FILTER (
        WHERE credit_due IS NOT NULL
          AND CURRENT_DATE - credit_due BETWEEN 31 AND 60
    )                                                               AS overdue_31_60,

    SUM(outstanding) FILTER (
        WHERE credit_due IS NOT NULL
          AND CURRENT_DATE - credit_due BETWEEN 61 AND 90
    )                                                               AS overdue_61_90,

    SUM(outstanding) FILTER (
        WHERE credit_due IS NOT NULL
          AND CURRENT_DATE - credit_due > 90
    )                                                               AS overdue_90_plus

FROM open_balances b
LEFT JOIN customer c ON c.customer_id = b.customer_id
GROUP BY b.customer_id, c.name
ORDER BY total_outstanding DESC;


-- =============================================================================
-- D4. UNALLOCATED RECEIPTS PER CUSTOMER
--     Receipts (IN) where the customer paid more than what has been
--     allocated to invoices — a credit balance in the customer's favour.
-- =============================================================================
CREATE VIEW UNALLOCATED_PAYMENT_DR_PER_CUSTOMER AS
SELECT
    cp.customer_id,
    cp.id                                               AS payment_id,
    cp.payment_date,
    cp.payment_method,
    cp.total_payment_amount,
    COALESCE(SUM(cpa.allocated_amount), 0)              AS allocated,
    CASE cp.direction
        WHEN 'IN'  THEN cp.total_payment_amount - COALESCE(SUM(cpa.allocated_amount), 0)
        WHEN 'OUT' THEN NULL  -- OUT payments are refunds, not credits to apply forward
    END AS unallocated_credit
FROM customer_payment cp
LEFT JOIN customer_payment_allocation cpa ON cpa.payment_id = cp.id
GROUP BY cp.id, cp.customer_id, cp.payment_date, cp.payment_method, cp.total_payment_amount
HAVING cp.direction = 'IN'
    AND cp.total_payment_amount - COALESCE(SUM(cpa.allocated_amount), 0) > 0
ORDER BY cp.customer_id, cp.payment_date;

-- =============================================================================
-- D5. Unposted customer refunds (OUT payments with unallocated amounts)
-- =============================================================================
CREATE VIEW UNPOSTED_CUSTOMER_REFUNDS AS
SELECT
    cp.id              AS payment_id,
    cp.customer_id,
    cp.payment_date,
    cp.total_payment_amount,
    COALESCE(SUM(cpa.allocated_amount), 0)              AS allocated_to_invoices,
    cp.total_payment_amount
        - COALESCE(SUM(cpa.allocated_amount), 0)        AS unposted_refund
FROM customer_payment cp
LEFT JOIN customer_payment_allocation cpa ON cpa.payment_id = cp.id
WHERE cp.direction = 'OUT'
GROUP BY cp.id, cp.customer_id, cp.payment_date, cp.total_payment_amount
HAVING cp.total_payment_amount - COALESCE(SUM(cpa.allocated_amount), 0) > 0
ORDER BY cp.payment_date;

-- =============================================================================
-- 5: Unposted supplier refunds (IN payments with unallocated amounts)
-- =============================================================================
CREATE VIEW UNPOSTED_SUPPLIER_REFUNDS AS
SELECT
    sp.id              AS payment_id,
    sp.supplier_id,
    sp.payment_date,
    sp.total_payment_amount,
    COALESCE(SUM(spa.allocated_amount), 0)              AS allocated_to_grns,
    sp.total_payment_amount
        - COALESCE(SUM(spa.allocated_amount), 0)        AS unposted_refund
FROM supplier_payment sp
LEFT JOIN supplier_payment_allocation spa ON spa.payment_id = sp.id
WHERE sp.direction = 'IN'
GROUP BY sp.id, sp.supplier_id, sp.payment_date, sp.total_payment_amount
HAVING sp.total_payment_amount - COALESCE(SUM(spa.allocated_amount), 0) > 0
ORDER BY sp.payment_date;
-- ------------------------------------------------------------
--  Cash balancing
--  1. Each salesman hands the day's collected cash to the cashier
--     (part of it may already be deposited via bank CDM machines).
--  2. Once every salesman is verified, the whole cash drawer is
--     balanced and the day is closed.
-- ------------------------------------------------------------

CREATE TABLE cash_handover (
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    handover_date   DATE,
    employee_id     BIGINT,          -- salesman handing over
    status          VARCHAR,         -- VERIFIED
    expected_amount NUMERIC(12,2),   -- system-expected cash at verification time
    declared_cash   NUMERIC(12,2),   -- physical cash handed to the cashier
    cdm_total       NUMERIC(12,2),   -- total of the CDM deposits below
    variance        NUMERIC(12,2),   -- declared_cash + cdm_total - expected_amount
    remarks         VARCHAR,
    verified_by     BIGINT,          -- cashier employee
    verified_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cash_handover_deposit (
    id                BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    cash_handover_key BIGINT,
    cash_handover_id  BIGINT REFERENCES cash_handover(id),
    bank              VARCHAR,
    reference_number  VARCHAR,       -- CDM slip / receipt number
    amount            NUMERIC(12,2)
);

CREATE TABLE cash_drawer_session (
    id               BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    session_date     DATE UNIQUE,
    status           VARCHAR,        -- CLOSED | REOPENED
    opening_balance  NUMERIC(12,2),
    handover_cash    NUMERIC(12,2),  -- sum of verified handover physical cash
    other_cash_in    NUMERIC(12,2),  -- supplier cash refunds received (IN)
    cash_out         NUMERIC(12,2),  -- supplier cash payments (OUT)
    other_income     NUMERIC(12,2),  -- ledger cash income of the day
    expenses         NUMERIC(12,2),  -- ledger cash expenses of the day
    expected_closing NUMERIC(12,2),  -- opening + handover_cash + other_cash_in + other_income - cash_out - expenses
    counted_closing  NUMERIC(12,2),
    variance         NUMERIC(12,2),  -- counted_closing - expected_closing
    remarks          VARCHAR,
    closed_by        BIGINT,
    closed_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
--  Other income & expenses ledger
--  Supplier commissions, scrap sales (income); fuel, repairs
--  and similar overheads (expense).
-- ------------------------------------------------------------

CREATE TABLE ledger_category (
    id     BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name   VARCHAR(100) NOT NULL,
    kind   VARCHAR,               -- INCOME | EXPENSE
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE ledger_entry (
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    entry_date     DATE,
    kind           VARCHAR,       -- INCOME | EXPENSE (copied from the category at save time)
    category_id    BIGINT REFERENCES ledger_category(id),
    description    VARCHAR,
    amount         NUMERIC(12,2),
    payment_method VARCHAR,       -- CASH, CHEQUE, BANK_TRANSFER
    supplier_id    BIGINT,        -- optional: commission-paying supplier
    employee_id    BIGINT,        -- recorded by
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
