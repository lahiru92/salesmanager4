-- =============================================================================
-- SUPPLIER PURCHASE & CREDITOR MANAGEMENT — TEST DATA
-- =============================================================================
-- Extends existing data: GRN ids 1-3, payment ids 1-5, allocation ids 1-8
-- New supplier IDs: 5, 7  |  Employee: 4 (new), 2 (existing)
-- New GRN ids 4–12  |  Payment ids 6–16  |  Allocation ids 9–20
--
-- GRN column convention (cash+cheque+credit = total):
--   cash   = amount handed over in cash at time of delivery
--   cheque = cheque handed over at time of delivery
--   credit = amount left on credit (incl. bank transfers settled later)
--            NOTE: bank transfers are not a GRN column; they are recorded
--            in supplier_payment only. A same-day bank transfer still
--            creates a credit row on GRN and is immediately settled.
-- =============================================================================


-- =============================================================================
-- SCENARIO A: Full CASH payment at delivery — no credit
-- Delivery person hands over full cash on the spot. GRN closed immediately.
-- =============================================================================
INSERT INTO grn (id, purchase_order_id, status, received_date, supplier_id, employee_id,
                 cash, cheque, credit, total, credit_due)
OVERRIDING SYSTEM VALUE VALUES (4, NULL, 'APPROVED', '2026-06-05', 5, 4,
        85000.00, 0.00, 0.00, 85000.00, NULL);

INSERT INTO supplier_payment (id, supplier_id, payment_method, total_payment_amount,
                               cheque_number, bank, bank_account, reference_number, payment_date)
OVERRIDING SYSTEM VALUE VALUES (6, 5, 'CASH', 85000.00,
        NULL, NULL, NULL, NULL, '2026-06-05');

INSERT INTO supplier_payment_allocation (id, payment_id, grn_id, allocated_amount)
OVERRIDING SYSTEM VALUE VALUES (9, 6, 4, 85000.00);


-- =============================================================================
-- SCENARIO B: Full CHEQUE payment at delivery — no credit
-- Cheque handed to delivery person on the day. No outstanding balance.
-- =============================================================================
INSERT INTO grn (id, purchase_order_id, status, received_date, supplier_id, employee_id,
                 cash, cheque, credit, total, credit_due)
OVERRIDING SYSTEM VALUE VALUES (5, NULL, 'APPROVED', '2026-06-06', 5, 4,
        0.00, 210000.00, 0.00, 210000.00, NULL);

INSERT INTO supplier_payment (id, supplier_id, payment_method, total_payment_amount,
                               cheque_number, bank, bank_account, reference_number, payment_date)
OVERRIDING SYSTEM VALUE VALUES (7, 5, 'CHEQUE', 210000.00,
        'CHQ-009214', 'Commercial Bank', NULL, NULL, '2026-06-06');

INSERT INTO supplier_payment_allocation (id, payment_id, grn_id, allocated_amount)
OVERRIDING SYSTEM VALUE VALUES (10, 7, 5, 210000.00);


-- =============================================================================
-- SCENARIO C: Full BANK TRANSFER — same day, fully settled (no remaining credit)
-- GRN records credit=total (no cash/cheque column for BT); bank transfer
-- payment immediately clears it the same day.
-- =============================================================================
INSERT INTO grn (id, purchase_order_id, status, received_date, supplier_id, employee_id,
                 cash, cheque, credit, total, credit_due)
OVERRIDING SYSTEM VALUE VALUES (6, NULL, 'APPROVED', '2026-06-07', 7, 4,
        0.00, 0.00, 540000.00, 540000.00, '2026-06-07');

INSERT INTO supplier_payment (id, supplier_id, payment_method, total_payment_amount,
                               cheque_number, bank, bank_account, reference_number, payment_date)
OVERRIDING SYSTEM VALUE VALUES (8, 7, 'BANK_TRANSFER', 540000.00,
        NULL, 'Sampath Bank', '007-1-002-9875432', 'TXN-20260607-0042', '2026-06-07');

INSERT INTO supplier_payment_allocation (id, payment_id, grn_id, allocated_amount)
OVERRIDING SYSTEM VALUE VALUES (11, 8, 6, 540000.00);


-- =============================================================================
-- SCENARIO D: ZERO payment at delivery — full amount on credit, NOT YET PAID
-- Trusted supplier; goods received with no upfront payment at all.
-- Credit due date is 30 days out. No payment rows exist.
-- =============================================================================
INSERT INTO grn (id, purchase_order_id, status, received_date, supplier_id, employee_id,
                 cash, cheque, credit, total, credit_due)
OVERRIDING SYSTEM VALUE VALUES (7, NULL, 'APPROVED', '2026-06-10', 7, 4,
        0.00, 0.00, 320000.00, 320000.00, '2026-07-10');

-- *** No supplier_payment or allocation records — fully outstanding creditor ***


-- =============================================================================
-- SCENARIO E: Partial CASH at delivery + credit → later settled by CASH
--             given to the delivery person on the next delivery run.
-- Step 1 (delivery day): 40,000 cash paid upfront.
-- Step 2 (next delivery): 120,000 cash handed to driver — credit fully cleared.
-- =============================================================================
INSERT INTO grn (id, purchase_order_id, status, received_date, supplier_id, employee_id,
                 cash, cheque, credit, total, credit_due)
OVERRIDING SYSTEM VALUE VALUES (8, NULL, 'APPROVED', '2026-06-08', 5, 4,
        40000.00, 0.00, 120000.00, 160000.00, '2026-07-08');

-- Step 1: cash at delivery
INSERT INTO supplier_payment (id, supplier_id, payment_method, total_payment_amount,
                               cheque_number, bank, bank_account, reference_number, payment_date)
OVERRIDING SYSTEM VALUE VALUES (9, 5, 'CASH', 40000.00,
        NULL, NULL, NULL, NULL, '2026-06-08');

INSERT INTO supplier_payment_allocation (id, payment_id, grn_id, allocated_amount)
OVERRIDING SYSTEM VALUE VALUES (12, 9, 8, 40000.00);

-- Step 2: cash to delivery person on next visit (full credit settlement)
INSERT INTO supplier_payment (id, supplier_id, payment_method, total_payment_amount,
                               cheque_number, bank, bank_account, reference_number, payment_date)
OVERRIDING SYSTEM VALUE VALUES (10, 5, 'CASH', 120000.00,
        NULL, NULL, NULL, NULL, '2026-06-15');

INSERT INTO supplier_payment_allocation (id, payment_id, grn_id, allocated_amount)
OVERRIDING SYSTEM VALUE VALUES (13, 10, 8, 120000.00);


-- =============================================================================
-- SCENARIO F: Small CASH deposit at delivery + large credit →
--             PARTIAL bank transfer later — balance STILL OUTSTANDING
-- Step 1 (delivery): 50,000 cash.
-- Step 2 (later):    200,000 bank transfer.
-- Remaining:         200,000 still unpaid (creditor outstanding).
-- =============================================================================
INSERT INTO grn (id, purchase_order_id, status, received_date, supplier_id, employee_id,
                 cash, cheque, credit, total, credit_due)
OVERRIDING SYSTEM VALUE VALUES (9, NULL, 'APPROVED', '2026-06-09', 7, 4,
        50000.00, 0.00, 400000.00, 450000.00, '2026-07-15');

-- Step 1: cash at delivery
INSERT INTO supplier_payment (id, supplier_id, payment_method, total_payment_amount,
                               cheque_number, bank, bank_account, reference_number, payment_date)
OVERRIDING SYSTEM VALUE VALUES (11, 7, 'CASH', 50000.00,
        NULL, NULL, NULL, NULL, '2026-06-09');

INSERT INTO supplier_payment_allocation (id, payment_id, grn_id, allocated_amount)
OVERRIDING SYSTEM VALUE VALUES (14, 11, 9, 50000.00);

-- Step 2: partial bank transfer — 200,000 of 400,000 credit paid
INSERT INTO supplier_payment (id, supplier_id, payment_method, total_payment_amount,
                               cheque_number, bank, bank_account, reference_number, payment_date)
OVERRIDING SYSTEM VALUE VALUES (12, 7, 'BANK_TRANSFER', 200000.00,
        NULL, 'Peoples Bank', '112-2-001-0045678', 'TXN-20260620-0118', '2026-06-20');

INSERT INTO supplier_payment_allocation (id, payment_id, grn_id, allocated_amount)
OVERRIDING SYSTEM VALUE VALUES (15, 12, 9, 200000.00);

-- Outstanding on GRN 9: 450,000 - 50,000 - 200,000 = 200,000 unpaid


-- =============================================================================
-- SCENARIO G: TWO GRNs from the same supplier settled by ONE lump BANK TRANSFER
-- Both deliveries taken on credit; a single bank transfer clears both.
-- GRN 10: 275,000  |  GRN 11: 125,000  |  Combined: 400,000
-- =============================================================================
INSERT INTO grn (id, purchase_order_id, status, received_date, supplier_id, employee_id,
                 cash, cheque, credit, total, credit_due)
OVERRIDING SYSTEM VALUE VALUES (10, NULL, 'APPROVED', '2026-06-10', 5, 2,
        0.00, 0.00, 275000.00, 275000.00, '2026-07-10');

INSERT INTO grn (id, purchase_order_id, status, received_date, supplier_id, employee_id,
                 cash, cheque, credit, total, credit_due)
OVERRIDING SYSTEM VALUE VALUES (11, NULL, 'APPROVED', '2026-06-12', 5, 2,
        0.00, 0.00, 125000.00, 125000.00, '2026-07-12');

-- Single bank transfer that covers both GRNs
INSERT INTO supplier_payment (id, supplier_id, payment_method, total_payment_amount,
                               cheque_number, bank, bank_account, reference_number, payment_date)
OVERRIDING SYSTEM VALUE VALUES (13, 5, 'BANK_TRANSFER', 400000.00,
        NULL, 'Commercial Bank', '001-5-009-1123456', 'TXN-20260622-0205', '2026-06-22');

INSERT INTO supplier_payment_allocation (id, payment_id, grn_id, allocated_amount)
OVERRIDING SYSTEM VALUE VALUES
    (16, 13, 10, 275000.00),
    (17, 13, 11, 125000.00);


-- =============================================================================
-- SCENARIO H: CASH + CHEQUE at delivery, then credit settled by CHEQUE
--             handed to the delivery person on the next visit.
-- Step 1 (delivery): 30,000 cash + 100,000 cheque paid upfront.
-- Step 2 (next delivery): 250,000 cheque given to driver — fully settled.
-- =============================================================================
INSERT INTO grn (id, purchase_order_id, status, received_date, supplier_id, employee_id,
                 cash, cheque, credit, total, credit_due)
OVERRIDING SYSTEM VALUE VALUES (12, NULL, 'APPROVED', '2026-06-11', 7, 4,
        30000.00, 100000.00, 250000.00, 380000.00, '2026-07-11');

-- Step 1a: cash at delivery
INSERT INTO supplier_payment (id, supplier_id, payment_method, total_payment_amount,
                               cheque_number, bank, bank_account, reference_number, payment_date)
OVERRIDING SYSTEM VALUE VALUES (14, 7, 'CASH', 30000.00,
        NULL, NULL, NULL, NULL, '2026-06-11');

INSERT INTO supplier_payment_allocation (id, payment_id, grn_id, allocated_amount)
OVERRIDING SYSTEM VALUE VALUES (18, 14, 12, 30000.00);

-- Step 1b: cheque at delivery
INSERT INTO supplier_payment (id, supplier_id, payment_method, total_payment_amount,
                               cheque_number, bank, bank_account, reference_number, payment_date)
OVERRIDING SYSTEM VALUE VALUES (15, 7, 'CHEQUE', 100000.00,
        'CHQ-004417', 'Nations Trust Bank', NULL, NULL, '2026-06-11');

INSERT INTO supplier_payment_allocation (id, payment_id, grn_id, allocated_amount)
OVERRIDING SYSTEM VALUE VALUES (19, 15, 12, 100000.00);

-- Step 2: cheque given to delivery person on next visit (full credit settlement)
INSERT INTO supplier_payment (id, supplier_id, payment_method, total_payment_amount,
                               cheque_number, bank, bank_account, reference_number, payment_date)
OVERRIDING SYSTEM VALUE VALUES (16, 7, 'CHEQUE', 250000.00,
        'CHQ-004488', 'Nations Trust Bank', NULL, NULL, '2026-06-18');

INSERT INTO supplier_payment_allocation (id, payment_id, grn_id, allocated_amount)
OVERRIDING SYSTEM VALUE VALUES (20, 16, 12, 250000.00);


-- =============================================================================
-- SUMMARY — ALL GRNs (existing + new)
-- =============================================================================
-- GRN | Supp | Total    | Cash     | Cheque   | Credit   | Paid     | Outstanding | Scenario
-- ----+------+----------+----------+----------+----------+----------+-------------+---------
--  1  |   3  | 130,000  |  50,000  |       0  |  80,000  | 130,000  |           0 | existing — partial cash + credit (settled later BT)
--  2  |   3  | 300,000  | 100,000  | 150,000  |  50,000  | 270,000  |      30,000 | existing — mixed; credit PARTIALLY paid (30k still due)
--  3  |   3  | 175,000  |       0  |       0  | 175,000  | 175,000  |           0 | existing — full credit (settled by cheque)
--  4  |   5  |  85,000  |  85,000  |       0  |       0  |  85,000  |           0 | A: Full immediate CASH
--  5  |   5  | 210,000  |       0  | 210,000  |       0  | 210,000  |           0 | B: Full immediate CHEQUE
--  6  |   7  | 540,000  |       0  |       0  | 540,000  | 540,000  |           0 | C: Full immediate BANK TRANSFER (same-day)
--  7  |   7  | 320,000  |       0  |       0  | 320,000  |       0  |     320,000 | D: Zero payment — 100% credit, FULLY UNPAID
--  8  |   5  | 160,000  |  40,000  |       0  | 120,000  | 160,000  |           0 | E: Partial cash; credit settled by cash to driver
--  9  |   7  | 450,000  |  50,000  |       0  | 400,000  | 250,000  |     200,000 | F: Deposit + credit; partial BT; 200k OUTSTANDING
-- 10  |   5  | 275,000  |       0  |       0  | 275,000  | 275,000  |           0 | G: Full credit; lump BT (with GRN11) — SETTLED
-- 11  |   5  | 125,000  |       0  |       0  | 125,000  | 125,000  |           0 | G: Full credit; lump BT (with GRN10) — SETTLED
-- 12  |   7  | 380,000  |  30,000  | 100,000  | 250,000  | 380,000  |           0 | H: Cash+cheque; credit settled by cheque to driver
--
-- Outstanding creditors after all inserts:
--   Supplier 3, GRN  2:  30,000 due (partial credit remaining)
--   Supplier 7, GRN  7: 320,000 due (zero payment made)
--   Supplier 7, GRN  9: 200,000 due (partial bank transfer made)
--   Total outstanding:  550,000
-- =============================================================================


-- =============================================================================
-- QUICK VERIFICATION QUERIES  (run these after inserting)
-- =============================================================================
 
/*
-- 1. GRN credit balance — outstanding credit per GRN
SELECT
    g.id                                              AS grn_id,
    g.received_date,
    g.total,
    g.credit                                          AS credit_at_delivery,
    COALESCE(SUM(spa.allocated_amount), 0)            AS total_allocated,
    g.credit - COALESCE(SUM(spa.allocated_amount), 0) AS outstanding_credit
FROM grn g
LEFT JOIN supplier_payment_allocation spa ON spa.grn_id = g.id
GROUP BY g.id, g.received_date, g.total, g.credit
ORDER BY g.id;
 
-- 2. Payments and their allocations
SELECT
    sp.id            AS payment_id,
    sp.payment_date,
    sp.payment_method,
    sp.total_payment_amount,
    COALESCE(SUM(spa.allocated_amount), 0) AS allocated,
    sp.total_payment_amount
        - COALESCE(SUM(spa.allocated_amount), 0)      AS unallocated
FROM supplier_payment sp
LEFT JOIN supplier_payment_allocation spa ON spa.payment_id = sp.id
GROUP BY sp.id, sp.payment_date, sp.payment_method, sp.total_payment_amount
ORDER BY sp.id;
 
-- 3. Supplier outstanding balance (supplier_id = 3)
SELECT
    SUM(g.credit) - COALESCE(SUM(spa.allocated_amount), 0) AS total_outstanding
FROM grn g
LEFT JOIN supplier_payment_allocation spa ON spa.grn_id = g.id
WHERE g.supplier_id = 3;
*/
 