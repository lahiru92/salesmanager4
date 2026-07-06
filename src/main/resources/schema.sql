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

CREATE TABLE EMPLOYEE (
    ID                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    KNOWN_NAME          VARCHAR,
    FULL_NAME           VARCHAR,
    ADDRESS_LINE1       VARCHAR,
    ADDRESS_LINE2       VARCHAR,
    ADDRESS_LINE3       VARCHAR,
    ADDRESS_LINE4       VARCHAR,
    ADDRESS_LINE5       VARCHAR,
    PHONE_MOBILE        VARCHAR,
    PHONE_HOME          VARCHAR,
    PHONE_OFFICE        VARCHAR,
    EMAIL_PERSONAL      VARCHAR,
    EMAIL_OFFICE        VARCHAR,
    DATE_OF_BIRTH       DATE,
    NIC_NUMBER          VARCHAR,
    PASSPORT_NUMBER     VARCHAR,
    DRIVERS_LICENSE_NO  VARCHAR,
    DESIGNATION         VARCHAR,
    DATE_JOINED         DATE,
    CREATED_BY          BIGINT,
    CREATED_AT          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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