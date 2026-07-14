-- ============================================================
--  SALES_MANAGER – FULL SCHEMA (STRUCTURE ONLY, NO DATA)
--  H2 DATABASE COMPATIBLE VERSION
-- ============================================================

-- CREATE SCHEMA IF NOT EXISTS SALES_MANAGER;
-- SET SCHEMA SALES_MANAGER;

-- ------------------------------------------------------------
--  AUTHENTICATION
-- ------------------------------------------------------------

CREATE TABLE USERS (
    USERNAME   VARCHAR(50) PRIMARY KEY,
    PASSWORD   VARCHAR(500) NOT NULL,
    ENABLED    BOOLEAN      NOT NULL,
    EMPLOYEE_ID BIGINT
);

CREATE TABLE AUTHORITIES (
    USERNAME  VARCHAR(50) NOT NULL REFERENCES USERS(USERNAME),
    AUTHORITY VARCHAR(50) NOT NULL
);

CREATE UNIQUE INDEX IX_AUTH_USERNAME ON AUTHORITIES (USERNAME, AUTHORITY);

-- ------------------------------------------------------------
--  CATEGORY
-- ------------------------------------------------------------

CREATE TABLE CATEGORY (
    CATEGORY_ID     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    NAME            VARCHAR(100) NOT NULL,
    NORMALIZED_NAME VARCHAR(100) NOT NULL UNIQUE
);

-- ------------------------------------------------------------
--  SUPPLIER
-- ------------------------------------------------------------

CREATE TABLE SUPPLIER (
    SUPPLIER_ID    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    NAME           VARCHAR(150) NOT NULL,
    PHONE          VARCHAR(30),
    EMAIL          VARCHAR(100),
    ACTIVE         BOOLEAN DEFAULT TRUE,
    CONTACT_PERSON VARCHAR(150)
);

-- ------------------------------------------------------------
--  ITEM
-- ------------------------------------------------------------

CREATE TABLE ITEM (
    ITEM_ID       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    CODE          VARCHAR(50)  NOT NULL UNIQUE,
    NAME          VARCHAR(200) NOT NULL,
    CATEGORY_ID   BIGINT       NOT NULL REFERENCES CATEGORY(CATEGORY_ID),
    UNIT          VARCHAR(20)  NOT NULL,
    REORDER_LEVEL NUMERIC(10,2) DEFAULT 0,
    ACTIVE        BOOLEAN DEFAULT TRUE,
    SUPPLIER_ID   BIGINT
);

-- ------------------------------------------------------------
--  STOCK TRANSACTION
-- ------------------------------------------------------------

CREATE TABLE STOCK_TRANSACTION (
    STOCK_TXN_ID   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ITEM_ID        BIGINT       NOT NULL REFERENCES ITEM(ITEM_ID),
    TXN_DATE       TIMESTAMP    NOT NULL,
    TXN_TYPE       CHAR(3)      NOT NULL,
    QUANTITY       NUMERIC(12,2) NOT NULL,
    UNIT_COST      NUMERIC(12,2),
    REFERENCE_TYPE VARCHAR(30),
    REFERENCE_ID   BIGINT,
    REMARKS        VARCHAR(255)
    --CONSTRAINT CHK_TXN_TYPE CHECK (TXN_TYPE IN ('IN ', 'OUT'))
);

CREATE INDEX IDX_STOCK_ITEM_DATE ON STOCK_TRANSACTION (ITEM_ID, TXN_DATE);
CREATE INDEX IDX_STOCK_TXN_TYPE  ON STOCK_TRANSACTION (TXN_TYPE);

-- ------------------------------------------------------------
--  EMPLOYEE
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
--  PURCHASE ORDER
-- ------------------------------------------------------------

CREATE TABLE PURCHASE_ORDER (
    ID          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    SUPPLIER_ID BIGINT,
    ORDER_DATE  DATE,
    STATUS      VARCHAR(20),
    CREATED_BY  BIGINT,
    CREATED_AT  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE PURCHASE_ORDER_ITEM (
    ID                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    PURCHASE_ORDER_KEY BIGINT,
    PURCHASE_ORDER_ID  BIGINT,
    ITEM_ID            BIGINT,
    QUANTITY           INTEGER,
    PRICE              NUMERIC(10,2)
);

-- ------------------------------------------------------------
--  GOODS RECEIVED NOTE (GRN)
-- ------------------------------------------------------------

CREATE TABLE GRN (
    ID                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    PURCHASE_ORDER_ID BIGINT,
    STATUS            VARCHAR,
    RECEIVED_DATE     DATE,
    SUPPLIER_ID       BIGINT,
    EMPLOYEE_ID       BIGINT,
    CASH              NUMERIC(12,2),
    CHEQUE            NUMERIC(12,2),
    CREDIT            NUMERIC(12,2),
    TOTAL             NUMERIC(12,2),
    CREDIT_DUE        DATE
);

CREATE TABLE GRN_ITEM (
    ID            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    GRN_KEY       BIGINT,
    GRN_ID        BIGINT,
    ITEM_ID       BIGINT,
    ITEM_NAME     VARCHAR,
    ORDERED_QTY   NUMERIC(12,2),
    RECEIVED_QTY  NUMERIC(12,2),
    REJECTED_QTY  NUMERIC(12,2),
    UNIT_PRICE    NUMERIC(12,2),
    ORDERED_PRICE NUMERIC(12,2)
);

-- ------------------------------------------------------------
--  CASH & CHEQUE TRANSACTIONS
-- ------------------------------------------------------------

CREATE TABLE CASH_TRANSACTION (
    ID            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    TXN_DATE      DATE      DEFAULT CURRENT_DATE,
    TXN_TYPE      VARCHAR,
    AMOUNT        NUMERIC(12,2),
    REF_TYPE      VARCHAR,
    REF_ID        BIGINT,
    TXN_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE CHEQUE_TRANSACTIONS (
    ID              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    TXN_DATE        DATE,
    TXN_TYPE        VARCHAR,
    CHEQUE_NO       INTEGER,
    CHEQUE_DATE     DATE,
    BANK            CHAR(8),
    AMOUNT          NUMERIC(12,2),
    REF_TYPE        VARCHAR,
    REF_ID          BIGINT,
    CLEARING_STATUS VARCHAR,
    TXN_TIMESTAMP   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
--  CREDITOR / SUPPLIER LEDGER
-- ------------------------------------------------------------

CREATE TABLE CREDITOR_TRANSACTION (
    ID            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    SUPPLIER_ID   BIGINT,
    TXN_DATE      DATE,
    TXN_TYPE      VARCHAR,        -- PAYABLE | PAYMENT
    AMOUNT        NUMERIC(12,2),
    DUE_DATE      DATE,
    REF_TYPE      VARCHAR,        -- E.G. GRN
    REF_ID        BIGINT,
    TXN_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE VIEW CREDITOR_BALANCE AS
SELECT
    SUPPLIER_ID,
    DUE_DATE,
    SUM(
        CASE
            WHEN TXN_TYPE = 'PAYABLE' THEN  AMOUNT
            WHEN TXN_TYPE = 'PAYMENT' THEN -AMOUNT
            ELSE 0
        END
    ) AS BALANCE
FROM CREDITOR_TRANSACTION
GROUP BY SUPPLIER_ID, DUE_DATE;

-- ------------------------------------------------------------
--  SUPPLIER PAYMENTS
-- ------------------------------------------------------------
CREATE TABLE supplier_payment (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
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
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
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
        WHEN DATEDIFF(DAY, CURRENT_DATE, credit_due) <= 30 THEN 'OVERDUE 1-30'
        WHEN DATEDIFF(DAY, CURRENT_DATE, credit_due) <= 60 THEN 'OVERDUE 31-60'
        WHEN DATEDIFF(DAY, CURRENT_DATE, credit_due) <= 90 THEN 'OVERDUE 61-90'
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
          AND DATEDIFF(DAY, CURRENT_DATE, CREDIT_DUE) BETWEEN 1 AND 30
    )                                                               AS overdue_1_30,

    -- 31-60 days overdue
    SUM(outstanding) FILTER (
        WHERE credit_due IS NOT NULL
          AND DATEDIFF(DAY, CURRENT_DATE, CREDIT_DUE) BETWEEN 31 AND 60
    )                                                               AS overdue_31_60,

    -- 61-90 days overdue
    SUM(outstanding) FILTER (
        WHERE credit_due IS NOT NULL
          AND DATEDIFF(DAY, CURRENT_DATE, CREDIT_DUE) BETWEEN 61 AND 90
    )                                                               AS overdue_61_90,

    -- Over 90 days
    SUM(outstanding) FILTER (
        WHERE credit_due IS NOT NULL
          AND DATEDIFF(DAY, CURRENT_DATE, CREDIT_DUE) > 90
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
