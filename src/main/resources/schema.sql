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

CREATE TABLE SUPPLIER_PAYMENT (
    ID                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    SUPPLIER_ID          BIGINT,
    PAYMENT_METHOD       VARCHAR,
    DIRECTION            VARCHAR,
    TOTAL_PAYMENT_AMOUNT NUMERIC(12,2),
    CHEQUE_NUMBER        VARCHAR,
    BANK                 VARCHAR,
    BANK_ACCOUNT         VARCHAR,
    REFERENCE_NUMBER     VARCHAR,
    PAYMENT_DATE         DATE
);

CREATE TABLE SUPPLIER_PAYMENT_ALLOCATION (
    ID               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    PAYMENT_ID       BIGINT REFERENCES SUPPLIER_PAYMENT(ID),
    GRN_ID           BIGINT REFERENCES GRN(ID),
    ALLOCATED_AMOUNT NUMERIC(12,2)
);