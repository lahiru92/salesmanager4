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