set search_path to sales_manager;

CREATE TABLE purchase_order (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    supplier_id BIGINT,
    order_date DATE,
    status VARCHAR(20)
);

CREATE TABLE purchase_order_item (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    purchase_order_id BIGINT,
    item_id BIGINT,
    quantity INT,
    price DECIMAL(10,2)
);

CREATE TABLE employee (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    known_name VARCHAR,
    full_name VARCHAR,
    address_line_1 VARCHAR,
    address_line_2 VARCHAR,
    address_line_3 VARCHAR,
    address_line_4 VARCHAR,
    address_line_5 VARCHAR,
    phone_mobile VARCHAR,
    phone_home VARCHAR,
    phone_office VARCHAR,
    email_personal VARCHAR,
    email_office VARCHAR,
    date_of_birth DATE,
    nic_number VARCHAR,
    passport_number VARCHAR,
    drivers_license_no VARCHAR,
    designation VARCHAR,
    date_joined DATE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
)


CREATE TABLE grn (
    id  BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    purchase_order_id BIGINT,
    status VARCHAR,
    received_date DATE,
    supplier_id BIGINT,
    employee_id BIGINT
);

CREATE TABLE grn_item (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    grn_key BIGINT,
    grn_id  BIGINT,
    item_id BIGINT,
    item_name VARCHAR,
    ordered_qty DECIMAL(12,2),
    received_qty DECIMAL(12,2),
    rejected_qty DECIMAL(12,2),
    unit_price DECIMAL(12,2),
    ordered_price DECIMAL(12,2) 
);

alter table grn add column cash numeric(12,2);
alter table grn add column cheque numeric(12,2);
alter table grn add column credit numeric(12,2);
alter table grn add column total numeric(12,2);
alter table grn add column credit_due date;


drop table cash_transaction;
CREATE TABLE cash_transaction (
	id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	txn_date DATE  DEFAULT CURRENT_DATE,
	txn_type VARCHAR,
	amount   numeric(12,2),
	ref_type varchar,
	ref_id   bigint,
	txn_timestamp timestamp DEFAULT CURRENT_TIMESTAMP
)

CREATE TABLE cheque_transactions (
	id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	txn_date DATE,
	txn_type VARCHAR,
	cheque_no integer,
	cheque_date date,
	bank char(8),
	amount   numeric(12,2),
	ref_type varchar,
	ref_id   bigint,
	clearing_status varchar,
	txn_timestamp timestamp DEFAULT CURRENT_TIMESTAMP
)


CREATE TABLE creditor_transaction (
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    supplier_id     bigint,
    txn_date        DATE,
    txn_type        VARCHAR, -- PAYABLE, PAYMENT
    amount          numeric(12,2),
    due_date        date,
    ref_type        varchar, -- GRN
    ref_id          bigint,
    txn_timestamp   timestamp DEFAULT CURRENT_TIMESTAMP
)

CREATE VIEW creditor_balance AS
SELECT
    supplier_id,
    due_date,
    SUM(
        CASE
            WHEN txn_type = 'PAYABLE' THEN amount
            WHEN txn_type = 'PAYMENT' THEN -amount
            ELSE 0
        END
    ) AS balance
FROM creditor_transaction
GROUP BY supplier_id, due_date;

drop table supplier_payment;
CREATE TABLE supplier_payment (
	id                   bigint primary key generated always as identity,
	supplier_id          bigint,
	payment_method       varchar,
	total_payment_amount numeric(12,2),
	cheque_number        varchar,
	bank                 varchar,
	bank_account         varchar,
	reference_number     varchar,
	payment_date         date
);


drop table supplier_payment_allocation;
CREATE TABLE supplier_payment_allocation (
	id                   bigint primary key generated always as identity,
	payment_id           bigint references supplier_payment(id),
	grn_id               bigint references grn(id),
	allocated_amount     numeric(12,2)
)


