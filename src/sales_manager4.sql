--
-- PostgreSQL database dump
--

-- Dumped from database version 13.4
-- Dumped by pg_dump version 13.4

-- Started on 2026-06-16 00:34:16

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 202 (class 1259 OID 26312)
-- Name: authorities; Type: TABLE; Schema: sales_manager; Owner: postgres
--

CREATE TABLE sales_manager.authorities (
    username character varying(50) NOT NULL,
    authority character varying(50) NOT NULL
);


ALTER TABLE sales_manager.authorities OWNER TO postgres;

--
-- TOC entry 204 (class 1259 OID 26331)
-- Name: category; Type: TABLE; Schema: sales_manager; Owner: postgres
--

CREATE TABLE sales_manager.category (
    category_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    normalized_name character varying(100) NOT NULL
);


ALTER TABLE sales_manager.category OWNER TO postgres;

--
-- TOC entry 203 (class 1259 OID 26329)
-- Name: category_category_id_seq; Type: SEQUENCE; Schema: sales_manager; Owner: postgres
--

ALTER TABLE sales_manager.category ALTER COLUMN category_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME sales_manager.category_category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 216 (class 1259 OID 26406)
-- Name: employee; Type: TABLE; Schema: sales_manager; Owner: postgres
--

CREATE TABLE sales_manager.employee (
    id bigint NOT NULL,
    known_name character varying,
    full_name character varying,
    address_line1 character varying,
    address_line2 character varying,
    address_line3 character varying,
    address_line4 character varying,
    address_line5 character varying,
    phone_mobile character varying,
    phone_home character varying,
    phone_office character varying,
    email_personal character varying,
    email_office character varying,
    date_of_birth date,
    nic_number character varying,
    passport_number character varying,
    drivers_license_no character varying,
    designation character varying,
    date_joined date,
    created_by bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE sales_manager.employee OWNER TO postgres;

--
-- TOC entry 215 (class 1259 OID 26404)
-- Name: employee_id_seq; Type: SEQUENCE; Schema: sales_manager; Owner: postgres
--

ALTER TABLE sales_manager.employee ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME sales_manager.employee_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 218 (class 1259 OID 27193)
-- Name: grn; Type: TABLE; Schema: sales_manager; Owner: postgres
--

CREATE TABLE sales_manager.grn (
    id bigint NOT NULL,
    purchase_order_id bigint,
    status character varying,
    received_date date,
    supplier_id bigint,
    employee_id bigint
);


ALTER TABLE sales_manager.grn OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 27191)
-- Name: grn_id_seq; Type: SEQUENCE; Schema: sales_manager; Owner: postgres
--

ALTER TABLE sales_manager.grn ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME sales_manager.grn_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 220 (class 1259 OID 27203)
-- Name: grn_item; Type: TABLE; Schema: sales_manager; Owner: postgres
--

CREATE TABLE sales_manager.grn_item (
    id bigint NOT NULL,
    grn_key bigint,
    grn_id bigint,
    item_id bigint,
    item_name character varying,
    ordered_qty numeric(12,2),
    received_qty numeric(12,2),
    rejected_qty numeric(12,2),
    unit_price numeric(12,2),
    ordered_price numeric(12,2)
);


ALTER TABLE sales_manager.grn_item OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 27201)
-- Name: grn_item_id_seq; Type: SEQUENCE; Schema: sales_manager; Owner: postgres
--

ALTER TABLE sales_manager.grn_item ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME sales_manager.grn_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 208 (class 1259 OID 26350)
-- Name: item; Type: TABLE; Schema: sales_manager; Owner: postgres
--

CREATE TABLE sales_manager.item (
    item_id bigint NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(200) NOT NULL,
    category_id bigint NOT NULL,
    unit character varying(20) NOT NULL,
    reorder_level numeric(10,2) DEFAULT 0,
    active boolean DEFAULT true,
    supplier_id bigint
);


ALTER TABLE sales_manager.item OWNER TO postgres;

--
-- TOC entry 207 (class 1259 OID 26348)
-- Name: item_item_id_seq; Type: SEQUENCE; Schema: sales_manager; Owner: postgres
--

ALTER TABLE sales_manager.item ALTER COLUMN item_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME sales_manager.item_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 212 (class 1259 OID 26381)
-- Name: purchase_order; Type: TABLE; Schema: sales_manager; Owner: postgres
--

CREATE TABLE sales_manager.purchase_order (
    id bigint NOT NULL,
    supplier_id bigint,
    order_date date,
    status character varying(20),
    created_by bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE sales_manager.purchase_order OWNER TO postgres;

--
-- TOC entry 211 (class 1259 OID 26379)
-- Name: purchase_order_id_seq; Type: SEQUENCE; Schema: sales_manager; Owner: postgres
--

ALTER TABLE sales_manager.purchase_order ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME sales_manager.purchase_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 214 (class 1259 OID 26395)
-- Name: purchase_order_item; Type: TABLE; Schema: sales_manager; Owner: postgres
--

CREATE TABLE sales_manager.purchase_order_item (
    id bigint NOT NULL,
    purchase_order_key bigint,
    purchase_order_id bigint,
    item_id bigint,
    quantity integer,
    price numeric(10,2)
);


ALTER TABLE sales_manager.purchase_order_item OWNER TO postgres;

--
-- TOC entry 213 (class 1259 OID 26393)
-- Name: purchase_order_item_id_seq; Type: SEQUENCE; Schema: sales_manager; Owner: postgres
--

ALTER TABLE sales_manager.purchase_order_item ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME sales_manager.purchase_order_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 210 (class 1259 OID 26366)
-- Name: stock_transaction; Type: TABLE; Schema: sales_manager; Owner: postgres
--

CREATE TABLE sales_manager.stock_transaction (
    stock_txn_id bigint NOT NULL,
    item_id bigint NOT NULL,
    txn_date timestamp without time zone NOT NULL,
    txn_type character(3) NOT NULL,
    quantity numeric(12,2) NOT NULL,
    unit_cost numeric(12,2),
    reference_type character varying(30),
    reference_id bigint,
    remarks character varying(255),
    CONSTRAINT chk_txn_type CHECK ((txn_type = ANY (ARRAY['IN'::bpchar, 'OUT'::bpchar])))
);


ALTER TABLE sales_manager.stock_transaction OWNER TO postgres;

--
-- TOC entry 209 (class 1259 OID 26364)
-- Name: stock_transaction_stock_txn_id_seq; Type: SEQUENCE; Schema: sales_manager; Owner: postgres
--

ALTER TABLE sales_manager.stock_transaction ALTER COLUMN stock_txn_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME sales_manager.stock_transaction_stock_txn_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 206 (class 1259 OID 26342)
-- Name: supplier; Type: TABLE; Schema: sales_manager; Owner: postgres
--

CREATE TABLE sales_manager.supplier (
    supplier_id bigint NOT NULL,
    name character varying(150) NOT NULL,
    phone character varying(30),
    email character varying(100),
    active boolean DEFAULT true,
    contact_person character varying(150)
);


ALTER TABLE sales_manager.supplier OWNER TO postgres;

--
-- TOC entry 205 (class 1259 OID 26340)
-- Name: supplier_supplier_id_seq; Type: SEQUENCE; Schema: sales_manager; Owner: postgres
--

ALTER TABLE sales_manager.supplier ALTER COLUMN supplier_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME sales_manager.supplier_supplier_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 201 (class 1259 OID 26306)
-- Name: users; Type: TABLE; Schema: sales_manager; Owner: postgres
--

CREATE TABLE sales_manager.users (
    username character varying(50) NOT NULL,
    password character varying(100) NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    employee_id bigint
);


ALTER TABLE sales_manager.users OWNER TO postgres;

--
-- TOC entry 3067 (class 0 OID 26312)
-- Dependencies: 202
-- Data for Name: authorities; Type: TABLE DATA; Schema: sales_manager; Owner: postgres
--

INSERT INTO sales_manager.authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('lahiru', 'ROLE_USER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('dog', 'ROLE_USER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('fish', 'ROLE_USER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('lahiru', 'ROLE_MANAGER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('admin2', 'ROLE_ADMIN,USER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('admin3', 'ROLE_ADMIN');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('admin3', 'ROLE_USER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('kapil', 'ROLE_CLERK');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('kapil', 'ROLE_STOREKEEPER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('kapil', 'ROLE_USER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('multi', 'ROLE_ADMIN');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('multi', 'ROLE_CLERK');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('multi', 'ROLE_MANAGER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('multi', 'ROLE_SALESMAN');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('multi', 'ROLE_STOREKEEPER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('multi', 'ROLE_USER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('cat', 'ROLE_USER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('cat', 'ROLE_HR');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('kent', 'ROLE_CLERK');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('test1', 'ROLE_USER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('parrot', 'ROLE_USER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('wdw', 'ROLE_STOREKEEPER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('aa', 'ROLE_USER');
INSERT INTO sales_manager.authorities (username, authority) VALUES ('aa2', 'ROLE_USER');


--
-- TOC entry 3069 (class 0 OID 26331)
-- Dependencies: 204
-- Data for Name: category; Type: TABLE DATA; Schema: sales_manager; Owner: postgres
--

INSERT INTO sales_manager.category (category_id, name, normalized_name) OVERRIDING SYSTEM VALUE VALUES (1, 'Category 1', 'category 1');
INSERT INTO sales_manager.category (category_id, name, normalized_name) OVERRIDING SYSTEM VALUE VALUES (2, 'Category 2', 'category 2');
INSERT INTO sales_manager.category (category_id, name, normalized_name) OVERRIDING SYSTEM VALUE VALUES (3, 'Category 3', 'category 3');
INSERT INTO sales_manager.category (category_id, name, normalized_name) OVERRIDING SYSTEM VALUE VALUES (4, 'Rice', 'rice');
INSERT INTO sales_manager.category (category_id, name, normalized_name) OVERRIDING SYSTEM VALUE VALUES (5, 'Milk Powder', 'milk powder');
INSERT INTO sales_manager.category (category_id, name, normalized_name) OVERRIDING SYSTEM VALUE VALUES (6, 'Cheese', 'cheese');
INSERT INTO sales_manager.category (category_id, name, normalized_name) OVERRIDING SYSTEM VALUE VALUES (7, 'Bath soap', 'bath soap');
INSERT INTO sales_manager.category (category_id, name, normalized_name) OVERRIDING SYSTEM VALUE VALUES (8, 'Laundry Soap', 'laundry soap');
INSERT INTO sales_manager.category (category_id, name, normalized_name) OVERRIDING SYSTEM VALUE VALUES (9, 'Shampoo', 'shampoo');
INSERT INTO sales_manager.category (category_id, name, normalized_name) OVERRIDING SYSTEM VALUE VALUES (10, 'Biscuit', 'biscuit');


--
-- TOC entry 3081 (class 0 OID 26406)
-- Dependencies: 216
-- Data for Name: employee; Type: TABLE DATA; Schema: sales_manager; Owner: postgres
--

INSERT INTO sales_manager.employee (id, known_name, full_name, address_line1, address_line2, address_line3, address_line4, address_line5, phone_mobile, phone_home, phone_office, email_personal, email_office, date_of_birth, nic_number, passport_number, drivers_license_no, designation, date_joined, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (1, 'Saman', 'Saman Bandara', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Clerk', NULL, NULL, '2026-04-24 17:16:29.89802');
INSERT INTO sales_manager.employee (id, known_name, full_name, address_line1, address_line2, address_line3, address_line4, address_line5, phone_mobile, phone_home, phone_office, email_personal, email_office, date_of_birth, nic_number, passport_number, drivers_license_no, designation, date_joined, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (2, 'Brown', 'Brown Brown', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-02 00:48:04.952267');


--
-- TOC entry 3083 (class 0 OID 27193)
-- Dependencies: 218
-- Data for Name: grn; Type: TABLE DATA; Schema: sales_manager; Owner: postgres
--

INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (2, NULL, NULL, '2026-06-11', 4, NULL);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (5, NULL, 'DRAFT', '2026-06-13', 4, 2);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (6, NULL, 'DRAFT', '2026-05-13', 4, 2);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (7, NULL, 'DRAFT', '2026-05-13', 4, 2);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (8, NULL, 'DRAFT', '2026-05-13', 4, 2);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (10, NULL, 'DRAFT', '2026-05-13', 4, 2);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (12, NULL, 'DRAFT', '2026-05-13', 4, 2);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (14, NULL, 'DRAFT', '2026-05-13', 4, 2);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (4, NULL, 'APPROVED', '2026-06-04', 6, 2);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (3, 2, 'APPROVED', '2026-05-13', 4, 2);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (17, NULL, 'APPROVED', '2026-06-14', 6, 2);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (16, NULL, 'APPROVED', '2026-05-13', 4, 2);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (15, NULL, 'APPROVED', '2026-05-16', 4, 2);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (11, NULL, 'APPROVED', '2026-05-13', 4, 2);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (9, NULL, 'APPROVED', '2026-05-13', 4, 2);
INSERT INTO sales_manager.grn (id, purchase_order_id, status, received_date, supplier_id, employee_id) OVERRIDING SYSTEM VALUE VALUES (13, NULL, 'APPROVED', '2026-05-13', 4, 2);


--
-- TOC entry 3085 (class 0 OID 27203)
-- Dependencies: 220
-- Data for Name: grn_item; Type: TABLE DATA; Schema: sales_manager; Owner: postgres
--

INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (1, 0, 2, 14, 'Munchee Chocolate Biscuit', NULL, 300.00, 2.00, 188.00, NULL);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (6, 0, 4, 29, 'Signal 100g', NULL, 200.00, 0.00, 121.00, NULL);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (7, 1, 4, 30, 'Signal herbal 50g', NULL, 300.00, 0.00, 125.00, NULL);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (65, 0, 17, 30, 'Signal herbal 50g', NULL, 130.00, 20.00, 85.00, NULL);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (66, 1, 17, 29, 'Signal 100g', NULL, 400.00, 0.00, 176.00, NULL);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (67, 2, 17, 22, 'Sunlight Soap', NULL, 200.00, 0.00, 116.00, NULL);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (8, 0, 5, 26, 'Munchee lemon puff', NULL, 20.00, 12.00, 33.00, NULL);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (9, 0, 6, 24, 'Tikiri Mari 80g', 200.00, 200.00, 20.00, 231.00, 231.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (10, 1, 6, 27, 'Munchee ginger 80g', 310.00, 300.00, 20.00, 143.23, 150.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (11, 2, 6, 14, 'Munchee Chocolate Biscuit', 275.00, 250.00, 0.00, 123.11, 120.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (12, 3, 6, 26, 'Munchee lemon puff', 500.00, 499.00, 19.00, 276.33, 276.33);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (13, 0, 7, 24, 'Tikiri Mari 80g', 200.00, 200.00, 20.00, 231.00, 231.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (14, 1, 7, 27, 'Munchee ginger 80g', 310.00, 300.00, 20.00, 143.23, 150.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (15, 2, 7, 14, 'Munchee Chocolate Biscuit', 275.00, 250.00, 0.00, 123.11, 120.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (16, 3, 7, 26, 'Munchee lemon puff', 500.00, 499.00, 19.00, 276.33, 276.33);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (17, 0, 8, 24, 'Tikiri Mari 80g', 200.00, 200.00, 30.00, 231.00, 231.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (18, 1, 8, 27, 'Munchee ginger 80g', 310.00, 300.00, 20.00, 143.23, 150.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (19, 2, 8, 14, 'Munchee Chocolate Biscuit', 275.00, 250.00, 0.00, 123.11, 120.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (20, 3, 8, 26, 'Munchee lemon puff', 500.00, 499.00, 19.00, 276.33, 276.33);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (21, 0, 9, 24, 'Tikiri Mari 80g', 200.00, 200.00, 30.00, 231.00, 231.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (22, 1, 9, 27, 'Munchee ginger 80g', 310.00, 300.00, 20.00, 143.23, 150.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (23, 2, 9, 14, 'Munchee Chocolate Biscuit', 275.00, 250.00, 0.00, 123.11, 120.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (24, 3, 9, 26, 'Munchee lemon puff', 500.00, 499.00, 19.00, 276.33, 276.33);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (25, 0, 10, 24, 'Tikiri Mari 80g', 200.00, 200.00, 20.00, 231.00, 231.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (26, 1, 10, 27, 'Munchee ginger 80g', 310.00, 300.00, 20.00, 143.23, 150.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (27, 2, 10, 14, 'Munchee Chocolate Biscuit', 275.00, 250.00, 0.00, 123.11, 120.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (28, 3, 10, 26, 'Munchee lemon puff', 500.00, 499.00, 19.00, 276.33, 276.33);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (29, 0, 11, 24, 'Tikiri Mari 80g', 200.00, 200.00, 0.00, 231.00, 231.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (30, 1, 11, 27, 'Munchee ginger 80g', 310.00, 300.00, 0.00, 143.23, 150.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (31, 2, 11, 14, 'Munchee Chocolate Biscuit', 275.00, 250.00, 0.00, 123.11, 120.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (32, 3, 11, 26, 'Munchee lemon puff', 500.00, 499.00, 19.00, 276.33, 276.33);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (33, 0, 12, 24, 'Tikiri Mari 80g', 200.00, 50.00, 0.00, 231.00, 231.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (34, 1, 12, 27, 'Munchee ginger 80g', 310.00, 300.00, 20.00, 143.23, 150.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (35, 2, 12, 14, 'Munchee Chocolate Biscuit', 275.00, 250.00, 0.00, 123.11, 120.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (36, 3, 12, 26, 'Munchee lemon puff', 500.00, 499.00, 19.00, 276.33, 276.33);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (37, 0, 13, 24, 'Tikiri Mari 80g', 200.00, 99.00, 0.00, 231.00, 231.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (38, 1, 13, 27, 'Munchee ginger 80g', 310.00, 300.00, 20.00, 143.23, 150.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (39, 2, 13, 14, 'Munchee Chocolate Biscuit', 275.00, 250.00, 0.00, 123.11, 120.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (40, 3, 13, 26, 'Munchee lemon puff', 500.00, 499.00, 19.00, 276.33, 276.33);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (41, 0, 14, 24, 'Tikiri Mari 80g', 200.00, 999.00, 0.00, 231.00, 231.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (42, 1, 14, 27, 'Munchee ginger 80g', 310.00, 300.00, 20.00, 143.23, 150.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (43, 2, 14, 14, 'Munchee Chocolate Biscuit', 275.00, 250.00, 0.00, 123.11, 120.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (44, 3, 14, 26, 'Munchee lemon puff', 500.00, 499.00, 19.00, 276.33, 276.33);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (45, 0, 15, 24, 'Tikiri Mari 80g', 200.00, 200.00, 0.00, 231.00, 231.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (46, 1, 15, 27, 'Munchee ginger 80g', 310.00, 300.00, 20.00, 143.23, 150.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (47, 2, 15, 14, 'Munchee Chocolate Biscuit', 275.00, 250.00, 0.00, 123.11, 120.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (48, 3, 15, 26, 'Munchee lemon puff', 500.00, 499.00, 19.00, 276.33, 276.33);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (49, 0, 16, 24, 'Tikiri Mari 80g', 200.00, 999.00, 0.00, 231.00, 231.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (50, 1, 16, 27, 'Munchee ginger 80g', 310.00, 300.00, 20.00, 143.23, 150.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (51, 2, 16, 14, 'Munchee Chocolate Biscuit', 275.00, 250.00, 0.00, 123.11, 120.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (52, 3, 16, 26, 'Munchee lemon puff', 500.00, 499.00, 19.00, 276.33, 276.33);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (61, 0, 3, 24, 'Tikiri Mari 80g', 200.00, 777.00, 0.00, 231.00, 231.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (62, 1, 3, 27, 'Munchee ginger 80g', 310.00, 300.00, 20.00, 143.23, 150.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (63, 2, 3, 14, 'Munchee Chocolate Biscuit', 275.00, 250.00, 0.00, 123.11, 120.00);
INSERT INTO sales_manager.grn_item (id, grn_key, grn_id, item_id, item_name, ordered_qty, received_qty, rejected_qty, unit_price, ordered_price) OVERRIDING SYSTEM VALUE VALUES (64, 3, 3, 26, 'Munchee lemon puff', 500.00, 499.00, 19.00, 276.33, 276.33);


--
-- TOC entry 3073 (class 0 OID 26350)
-- Dependencies: 208
-- Data for Name: item; Type: TABLE DATA; Schema: sales_manager; Owner: postgres
--

INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (17, 'ITEM007', 'Product G', 3, 'pcs', 25.00, false, NULL);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (11, 'ITEM001', 'Araliya Samba 10kg', 4, 'pcs', 10.00, true, 5);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (24, '3020200', 'Tikiri Mari 80g', 1, 'nos', 200.00, true, 4);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (23, '102021', 'Wonderlight', 1, 'nos', 100.00, true, 3);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (22, '102', 'Sunlight Soap', 8, 'nos', 300.00, true, 6);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (25, '102022', 'Abcd', 7, 'nos', 899.00, true, 2);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (14, 'ITEM004', 'Munchee Chocolate Biscuit', 10, 'pcs', 20.00, true, 4);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (26, '3020300', 'Munchee lemon puff', 10, 'nos', 500.00, true, 4);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (27, '302080', 'Munchee ginger 80g', 10, 'nos', 200.00, true, 4);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (28, 'U1020', 'Signal toothpaste 50g', 1, 'nos', 100.00, true, 6);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (29, 'u2012', 'Signal 100g', 1, 'nos', 100.00, true, 6);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (30, 'u2000', 'Signal herbal 50g', 1, 'nos', 100.00, true, 6);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (16, 'ITEM006', 'Product F', 1, 'box', 12.00, true, NULL);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (19, 'ITEM009', 'Rice', 4, 'pcs', 7.00, true, NULL);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (12, 'ITEM002', 'Bola Saban', 7, 'pcs', 15.00, true, NULL);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (13, 'ITEM003', 'Kothmale Cheese Wedges 180g', 6, 'box', 5.00, true, NULL);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (15, 'ITEM005', 'Product E', 2, 'pcs', 8.00, true, NULL);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (21, '10010', 'Pelawatta Milk Powder', 5, 'nos', 50.00, true, NULL);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (20, 'ITEM010', 'Product J', 3, 'box', 10.00, true, NULL);
INSERT INTO sales_manager.item (item_id, code, name, category_id, unit, reorder_level, active, supplier_id) OVERRIDING SYSTEM VALUE VALUES (18, 'ITEM008', 'Hibiscus Shampoo', 9, 'pcs', 18.00, true, NULL);


--
-- TOC entry 3077 (class 0 OID 26381)
-- Dependencies: 212
-- Data for Name: purchase_order; Type: TABLE DATA; Schema: sales_manager; Owner: postgres
--

INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (3, 2, '2026-03-18', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (6, 2, '2026-03-18', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (7, 3, '2026-03-18', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (8, 2, '2026-03-18', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (9, 3, '2026-03-18', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (10, 2, '2026-03-18', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (11, 3, '2026-03-18', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (12, 1, '2026-03-18', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (13, 2, '2026-03-18', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (14, 2, '2026-03-18', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (15, 3, NULL, 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (16, 4, '2026-03-18', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (17, 3, '2026-03-18', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (18, 4, '2026-03-18', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (19, NULL, NULL, 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (20, NULL, NULL, 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (21, NULL, NULL, 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (22, NULL, NULL, 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (23, NULL, NULL, 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (24, NULL, NULL, 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (25, NULL, NULL, 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (26, 5, '2026-04-24', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (27, 5, '2026-05-02', 'DRAFT', NULL, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (28, 3, '2026-05-02', 'DRAFT', 2, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (30, 2, '2026-05-03', 'DRAFT', 0, '2026-05-03 13:01:23');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (29, 3, '2026-05-03', 'DRAFT', 0, '2026-05-03 13:04:15');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (31, 3, '2026-05-02', 'DRAFT', 0, '2026-05-04 12:41:47.888308');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (33, 5, '2026-05-04', 'DRAFT', 2, '2026-05-04 12:49:39.366416');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (32, 5, '2026-05-04', 'DRAFT', 2, '2026-05-04 12:51:53.796862');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (35, 4, '2026-05-04', 'DRAFT', 0, '2026-05-10 23:55:31.427326');
INSERT INTO sales_manager.purchase_order (id, supplier_id, order_date, status, created_by, created_at) OVERRIDING SYSTEM VALUE VALUES (34, 2, '2026-05-05', 'DRAFT', 2, '2026-06-13 17:27:10.201449');


--
-- TOC entry 3079 (class 0 OID 26395)
-- Dependencies: 214
-- Data for Name: purchase_order_item; Type: TABLE DATA; Schema: sales_manager; Owner: postgres
--

INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (1, 0, 6, 12, 1, 1.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (2, 0, 7, 12, 2, 22.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (3, 1, 7, 14, 4, 44.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (4, 0, 8, 13, 2, 2.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (5, 0, 9, 13, 2, 22.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (6, 0, 10, 15, 677, 88.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (7, 0, 11, 15, 3773, 222.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (8, 1, 11, 17, 334, 433.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (9, 2, 11, 18, 332, 3434.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (10, 0, 12, 21, 33, 33.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (11, 1, 12, NULL, NULL, NULL);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (12, 0, 13, 12, 2, 2.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (13, 0, 14, NULL, 200, 11.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (14, 1, 14, NULL, NULL, NULL);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (15, 0, 15, NULL, 500, 35.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (16, 1, 15, NULL, 100, 1432.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (17, 2, 15, NULL, 60, 800.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (18, 0, 16, NULL, 7, 88.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (19, 0, 17, 18, 400, 88.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (20, 0, 18, 14, 300, 112.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (21, 1, 18, 24, 500, 64.50);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (22, 2, 18, 11, 20, 1800.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (23, 0, 19, 21, 6, 8.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (24, 1, 19, 14, 8, 7.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (25, 2, 19, 23, 4, 3.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (26, 0, 20, 21, 2, 2.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (27, 1, 20, 21, 3, 3.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (28, 0, 21, 19, 2, 3.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (29, 0, 22, 11, 3, 4.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (30, 0, 23, 23, 2, 3.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (31, 1, 23, 15, 2, 3.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (32, 0, 26, 19, 3, 3.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (33, 1, 26, 14, 4, 4.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (34, 0, 27, 25, 37, 8833.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (35, 1, 27, 11, 33, 34234.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (36, 0, 28, 22, 33, 333.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (37, 0, 29, 13, 200, 276.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (38, 1, 29, 11, 20, 5443.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (39, 2, 29, 24, 300, 76.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (40, 3, 29, 21, 75, 1045.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (41, 0, 30, 21, 20, 1043.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (42, 0, 31, 22, 33, 333.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (45, 0, 33, 19, 50, 2333.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (46, 0, 32, 19, 50, 2333.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (52, 0, 35, 19, 33, 43.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (53, 1, 35, 15, 78, 9899.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (61, 0, 34, 14, 203, 180.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (62, 1, 34, 13, 9999, 990.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (63, 2, 34, 15, 76, 88.00);
INSERT INTO sales_manager.purchase_order_item (id, purchase_order_key, purchase_order_id, item_id, quantity, price) OVERRIDING SYSTEM VALUE VALUES (64, 3, 34, 11, 8, 88.00);


--
-- TOC entry 3075 (class 0 OID 26366)
-- Dependencies: 210
-- Data for Name: stock_transaction; Type: TABLE DATA; Schema: sales_manager; Owner: postgres
--

INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (1, 11, '2026-01-02 00:00:00', 'IN ', 200.00, 24.99, 'invoice', 1, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (2, 11, '2026-01-02 00:00:00', 'OUT', 50.00, 24.99, 'sale', 1, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (3, 11, '2026-02-22 16:28:06.153678', 'IN ', 25.00, NULL, 'ADJUSTMENT', NULL, 'test
');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (4, 14, '2026-02-22 17:03:03.483581', 'IN ', 25.00, NULL, 'ADJUSTMENT', NULL, 'test 2');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (5, 11, '2026-02-22 17:26:19.100974', 'IN ', 25.00, 2.00, 'ADJUSTMENT', NULL, 'aaa');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (6, 12, '2026-02-22 18:12:51.294952', 'IN ', 32.00, 120.00, 'ADJUSTMENT', NULL, 'aa');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (7, 12, '2026-02-22 18:14:26.740237', 'OUT', 10.00, 0.00, 'ADJUSTMENT', NULL, 'qq');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (8, 11, '2026-02-23 04:45:34.760362', 'IN ', 25.00, 22.00, 'ADJUSTMENT', NULL, 'qq');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (9, 19, '2026-02-23 04:48:50.170176', 'IN ', 2.00, 22.00, 'ADJUSTMENT', NULL, 'w');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (10, 12, '2026-02-23 04:55:20.203714', 'IN ', 3.00, 45.00, 'ADJUSTMENT', NULL, 'wewe');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (11, 19, '2026-02-23 05:17:21.370656', 'IN ', 4.00, 2.00, 'ADJUSTMENT', NULL, 'ww');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (12, 19, '2026-03-01 10:56:13.413638', 'IN ', 34.00, 100.00, 'ADJUSTMENT', NULL, 'test');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (13, 11, '2026-03-01 11:01:01.923765', 'IN ', 33.00, 999.00, 'ADJUSTMENT', NULL, 'fwesf');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (14, 11, '2026-03-01 11:04:49.826159', 'IN ', 88.00, 999.00, 'ADJUSTMENT', NULL, 'f');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (15, 11, '2026-03-01 15:26:31.365028', 'IN ', 88.00, 88.00, 'ADJUSTMENT', NULL, 'yyy');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (16, 11, '2026-03-01 15:29:47.346776', 'IN ', 8.00, 88.00, 'ADJUSTMENT', NULL, 'yyy');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (17, 23, '2026-03-17 14:43:17.485607', 'IN ', 1000.00, 56.00, 'ADJUSTMENT', NULL, 'Initial stock count');
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (18, 24, '2026-06-14 08:32:27.295004', 'IN ', 777.00, 231.00, 'GRN', 3, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (19, 27, '2026-06-14 08:32:27.410949', 'IN ', 280.00, 143.23, 'GRN', 3, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (20, 14, '2026-06-14 08:32:27.425757', 'IN ', 250.00, 123.11, 'GRN', 3, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (21, 26, '2026-06-14 08:32:27.430174', 'IN ', 480.00, 276.33, 'GRN', 3, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (22, 30, '2026-06-15 23:15:41.718141', 'IN ', 110.00, 85.00, 'GRN', 17, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (23, 29, '2026-06-15 23:15:41.784102', 'IN ', 400.00, 176.00, 'GRN', 17, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (24, 22, '2026-06-15 23:15:41.784102', 'IN ', 200.00, 116.00, 'GRN', 17, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (25, 24, '2026-06-15 23:32:31.347778', 'IN ', 999.00, 231.00, 'GRN', 16, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (26, 27, '2026-06-15 23:32:31.365919', 'IN ', 280.00, 143.23, 'GRN', 16, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (27, 14, '2026-06-15 23:32:31.379014', 'IN ', 250.00, 123.11, 'GRN', 16, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (28, 26, '2026-06-15 23:32:31.392243', 'IN ', 480.00, 276.33, 'GRN', 16, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (29, 24, '2026-06-15 23:34:10.156149', 'IN ', 200.00, 231.00, 'GRN', 15, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (30, 27, '2026-06-15 23:34:10.17116', 'IN ', 280.00, 143.23, 'GRN', 15, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (31, 14, '2026-06-15 23:34:10.197819', 'IN ', 250.00, 123.11, 'GRN', 15, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (32, 26, '2026-06-15 23:34:10.217172', 'IN ', 480.00, 276.33, 'GRN', 15, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (33, 24, '2026-06-16 00:14:07.958718', 'IN ', 200.00, 231.00, 'GRN', 11, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (34, 27, '2026-06-16 00:14:08.027382', 'IN ', 300.00, 143.23, 'GRN', 11, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (35, 14, '2026-06-16 00:14:08.039273', 'IN ', 250.00, 123.11, 'GRN', 11, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (36, 26, '2026-06-16 00:14:08.039273', 'IN ', 480.00, 276.33, 'GRN', 11, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (37, 24, '2026-06-16 00:14:16.653029', 'IN ', 170.00, 231.00, 'GRN', 9, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (38, 27, '2026-06-16 00:14:16.661377', 'IN ', 280.00, 143.23, 'GRN', 9, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (39, 14, '2026-06-16 00:14:16.675319', 'IN ', 250.00, 123.11, 'GRN', 9, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (40, 26, '2026-06-16 00:14:16.675319', 'IN ', 480.00, 276.33, 'GRN', 9, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (41, 24, '2026-06-16 00:21:45.34425', 'IN ', 99.00, 231.00, 'GRN', 13, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (42, 27, '2026-06-16 00:21:45.34425', 'IN ', 280.00, 143.23, 'GRN', 13, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (43, 14, '2026-06-16 00:21:45.362922', 'IN ', 250.00, 123.11, 'GRN', 13, NULL);
INSERT INTO sales_manager.stock_transaction (stock_txn_id, item_id, txn_date, txn_type, quantity, unit_cost, reference_type, reference_id, remarks) OVERRIDING SYSTEM VALUE VALUES (44, 26, '2026-06-16 00:21:45.366955', 'IN ', 480.00, 276.33, 'GRN', 13, NULL);


--
-- TOC entry 3071 (class 0 OID 26342)
-- Dependencies: 206
-- Data for Name: supplier; Type: TABLE DATA; Schema: sales_manager; Owner: postgres
--

INSERT INTO sales_manager.supplier (supplier_id, name, phone, email, active, contact_person) OVERRIDING SYSTEM VALUE VALUES (2, 'Pelawatta Kurunegala', '0711123422', 'asirik@gmail.com', true, 'Asiri Kumara');
INSERT INTO sales_manager.supplier (supplier_id, name, phone, email, active, contact_person) OVERRIDING SYSTEM VALUE VALUES (1, 'fewfwef', '3r27', 'ff@ewf.cdsf', false, 'awfe');
INSERT INTO sales_manager.supplier (supplier_id, name, phone, email, active, contact_person) OVERRIDING SYSTEM VALUE VALUES (3, 'Swadeshi', '0872332211', 'surangab@swadeshi.com', true, 'Suranga Bandara');
INSERT INTO sales_manager.supplier (supplier_id, name, phone, email, active, contact_person) OVERRIDING SYSTEM VALUE VALUES (4, 'Munchee', '0772332212', 'minura_silva@cbl.lk', true, 'Minura Silva');
INSERT INTO sales_manager.supplier (supplier_id, name, phone, email, active, contact_person) OVERRIDING SYSTEM VALUE VALUES (6, 'Uniliver', '0781122334', 'mendiskk@uniliver.lk', true, 'Mendis');
INSERT INTO sales_manager.supplier (supplier_id, name, phone, email, active, contact_person) OVERRIDING SYSTEM VALUE VALUES (5, 'Araliya mIlls', '0712323322', '', true, 'Sugath');


--
-- TOC entry 3066 (class 0 OID 26306)
-- Dependencies: 201
-- Data for Name: users; Type: TABLE DATA; Schema: sales_manager; Owner: postgres
--

INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('multi', '$2a$10$QQZZ9v4edL2CdLfKx2cj1.IStPtPWwfyBIzo/coCUHyPDJs/3woDu', true, NULL);
INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('test1', '$2a$10$9QiG2tBuf1a6ieIZj2OIUOR0tUVs0T9Y0n414dRGX2n5wn7XwTcpC', true, NULL);
INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('parrot', '$2a$10$M7.v5Lsaj1V.34/Xi6AE.O29kXpsiL5zShLAYEJekQRpD8I0/N7O2', true, NULL);
INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('wdw', '$2a$10$4jcJYbdeuMobN.eI0icC4evP.cQVcYKhIjiFWiGj85iDCI.cqPaau', true, NULL);
INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('aa2', '$2a$10$aT5btEDy.4SlaQZcBW9KuuOLHcMwCN41r7qZDVD.UXWPq2L0gvxma', false, NULL);
INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('fish', '$2a$10$svnxKi.UkltofUdUS8F9Te3TAz4UpgrYt3gP7vrK8DNamv.D3N7Qe', false, NULL);
INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('kapil', '$2a$10$.9/RgyH4kgJvPG1XStt8w.Y6BhbzN1Fa/diyE3uqt/2HQnReUoYP6', false, NULL);
INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('admin3', '$2a$10$rgM7qawSKtWADFvp16/svOrpuv1U27OTcbuEeDTBIcg.n4M/aXZCu', false, NULL);
INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('kent', '$2a$10$d7xOdYFGVR.oDRwIVGm22uf81Y/NbbRkjszu5Boj68fOzINpKLk5.', false, NULL);
INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('aa', '$2a$10$5AX4wYvBUxJK3ZVHkXncAumKuigghg0EeuPadAkGB3niJuAYfOvIq', true, NULL);
INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('cat', '$2a$10$x.j82UlLlHImQt.JkDorS...7wGoGAy/38jLXzdGg9VhEgNTVY9De', true, NULL);
INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('admin2', '$2a$10$wI7X497Q4s16QStgQatUvedBGylbUT.jGuU1pw2g75zGRLAMRXLo.', true, NULL);
INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('lahiru', '$2a$10$968AZyzpDaceEco4TTaBCOI3f1rHGnVDPBfKqs5mblKwPTuv/iRka', false, NULL);
INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('admin', '$2a$10$vLN/THwDMj25X7kVn/xjO.VGEKL1gIYy.ECeZ5NwV2SVcQaMhebEu', true, 0);
INSERT INTO sales_manager.users (username, password, enabled, employee_id) VALUES ('dog', '$2a$10$rR5TtmBgfv8ItBqx0o09G.7GHVDY1ORNDV7nmXNDa0DBrY.EYSbsq', true, 2);


--
-- TOC entry 3091 (class 0 OID 0)
-- Dependencies: 203
-- Name: category_category_id_seq; Type: SEQUENCE SET; Schema: sales_manager; Owner: postgres
--

SELECT pg_catalog.setval('sales_manager.category_category_id_seq', 10, true);


--
-- TOC entry 3092 (class 0 OID 0)
-- Dependencies: 215
-- Name: employee_id_seq; Type: SEQUENCE SET; Schema: sales_manager; Owner: postgres
--

SELECT pg_catalog.setval('sales_manager.employee_id_seq', 2, true);


--
-- TOC entry 3093 (class 0 OID 0)
-- Dependencies: 217
-- Name: grn_id_seq; Type: SEQUENCE SET; Schema: sales_manager; Owner: postgres
--

SELECT pg_catalog.setval('sales_manager.grn_id_seq', 17, true);


--
-- TOC entry 3094 (class 0 OID 0)
-- Dependencies: 219
-- Name: grn_item_id_seq; Type: SEQUENCE SET; Schema: sales_manager; Owner: postgres
--

SELECT pg_catalog.setval('sales_manager.grn_item_id_seq', 67, true);


--
-- TOC entry 3095 (class 0 OID 0)
-- Dependencies: 207
-- Name: item_item_id_seq; Type: SEQUENCE SET; Schema: sales_manager; Owner: postgres
--

SELECT pg_catalog.setval('sales_manager.item_item_id_seq', 30, true);


--
-- TOC entry 3096 (class 0 OID 0)
-- Dependencies: 211
-- Name: purchase_order_id_seq; Type: SEQUENCE SET; Schema: sales_manager; Owner: postgres
--

SELECT pg_catalog.setval('sales_manager.purchase_order_id_seq', 35, true);


--
-- TOC entry 3097 (class 0 OID 0)
-- Dependencies: 213
-- Name: purchase_order_item_id_seq; Type: SEQUENCE SET; Schema: sales_manager; Owner: postgres
--

SELECT pg_catalog.setval('sales_manager.purchase_order_item_id_seq', 64, true);


--
-- TOC entry 3098 (class 0 OID 0)
-- Dependencies: 209
-- Name: stock_transaction_stock_txn_id_seq; Type: SEQUENCE SET; Schema: sales_manager; Owner: postgres
--

SELECT pg_catalog.setval('sales_manager.stock_transaction_stock_txn_id_seq', 44, true);


--
-- TOC entry 3099 (class 0 OID 0)
-- Dependencies: 205
-- Name: supplier_supplier_id_seq; Type: SEQUENCE SET; Schema: sales_manager; Owner: postgres
--

SELECT pg_catalog.setval('sales_manager.supplier_supplier_id_seq', 6, true);


--
-- TOC entry 2910 (class 2606 OID 26337)
-- Name: category category_normalized_name_key; Type: CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.category
    ADD CONSTRAINT category_normalized_name_key UNIQUE (normalized_name);


--
-- TOC entry 2912 (class 2606 OID 26335)
-- Name: category category_pkey; Type: CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.category
    ADD CONSTRAINT category_pkey PRIMARY KEY (category_id);


--
-- TOC entry 2928 (class 2606 OID 26414)
-- Name: employee employee_pkey; Type: CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.employee
    ADD CONSTRAINT employee_pkey PRIMARY KEY (id);


--
-- TOC entry 2932 (class 2606 OID 27210)
-- Name: grn_item grn_item_pkey; Type: CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.grn_item
    ADD CONSTRAINT grn_item_pkey PRIMARY KEY (id);


--
-- TOC entry 2930 (class 2606 OID 27200)
-- Name: grn grn_pkey; Type: CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.grn
    ADD CONSTRAINT grn_pkey PRIMARY KEY (id);


--
-- TOC entry 2916 (class 2606 OID 26358)
-- Name: item item_code_key; Type: CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.item
    ADD CONSTRAINT item_code_key UNIQUE (code);


--
-- TOC entry 2918 (class 2606 OID 26356)
-- Name: item item_pkey; Type: CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.item
    ADD CONSTRAINT item_pkey PRIMARY KEY (item_id);


--
-- TOC entry 2926 (class 2606 OID 26399)
-- Name: purchase_order_item purchase_order_item_pkey; Type: CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.purchase_order_item
    ADD CONSTRAINT purchase_order_item_pkey PRIMARY KEY (id);


--
-- TOC entry 2924 (class 2606 OID 26385)
-- Name: purchase_order purchase_order_pkey; Type: CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.purchase_order
    ADD CONSTRAINT purchase_order_pkey PRIMARY KEY (id);


--
-- TOC entry 2922 (class 2606 OID 26371)
-- Name: stock_transaction stock_transaction_pkey; Type: CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.stock_transaction
    ADD CONSTRAINT stock_transaction_pkey PRIMARY KEY (stock_txn_id);


--
-- TOC entry 2914 (class 2606 OID 26347)
-- Name: supplier supplier_pkey; Type: CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.supplier
    ADD CONSTRAINT supplier_pkey PRIMARY KEY (supplier_id);


--
-- TOC entry 2907 (class 2606 OID 26311)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (username);


--
-- TOC entry 2919 (class 1259 OID 26377)
-- Name: idx_stock_item_date; Type: INDEX; Schema: sales_manager; Owner: postgres
--

CREATE INDEX idx_stock_item_date ON sales_manager.stock_transaction USING btree (item_id, txn_date);


--
-- TOC entry 2920 (class 1259 OID 26378)
-- Name: idx_stock_txn_type; Type: INDEX; Schema: sales_manager; Owner: postgres
--

CREATE INDEX idx_stock_txn_type ON sales_manager.stock_transaction USING btree (txn_type);


--
-- TOC entry 2908 (class 1259 OID 26320)
-- Name: ix_auth_username; Type: INDEX; Schema: sales_manager; Owner: postgres
--

CREATE UNIQUE INDEX ix_auth_username ON sales_manager.authorities USING btree (username, authority);


--
-- TOC entry 2933 (class 2606 OID 26315)
-- Name: authorities fk_authorities_users; Type: FK CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.authorities
    ADD CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES sales_manager.users(username);


--
-- TOC entry 2934 (class 2606 OID 26359)
-- Name: item fk_item_category; Type: FK CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.item
    ADD CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES sales_manager.category(category_id);


--
-- TOC entry 2935 (class 2606 OID 26372)
-- Name: stock_transaction fk_stock_item; Type: FK CONSTRAINT; Schema: sales_manager; Owner: postgres
--

ALTER TABLE ONLY sales_manager.stock_transaction
    ADD CONSTRAINT fk_stock_item FOREIGN KEY (item_id) REFERENCES sales_manager.item(item_id);


-- Completed on 2026-06-16 00:34:17

--
-- PostgreSQL database dump complete
--

