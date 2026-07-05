-- =============================================================================
-- CREDITOR MANAGEMENT QUERIES
-- 1. Outstanding GRNs per supplier
-- 2. Total outstanding balance per supplier
-- 3. Supplier aging (current / 30 / 60 / 90+ days)
-- =============================================================================


-- -----------------------------------------------------------------------------
-- SHARED CTE  (used by all three queries)
-- Calculates the remaining unpaid credit for every GRN.
-- A GRN is "outstanding" when credit > 0 AND not fully settled.
-- -----------------------------------------------------------------------------
-- grn.credit = credit portion recorded at delivery
-- SUM(allocated_amount) = what has since been paid against this GRN
-- outstanding = credit - paid_so_far


-- =============================================================================
-- 1. OUTSTANDING GRNs PER SUPPLIER
--    One row per GRN that still has an unpaid balance.
--    Includes how much was originally on credit, how much has been paid,
--    and what remains — plus the due date and how many days overdue it is.
-- =============================================================================

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
--    Useful for a creditor ledger overview or payment prioritisation.
-- =============================================================================

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
-- 3. SUPPLIER AGING REPORT
--    One row per supplier with the outstanding balance broken into time buckets:
--      CURRENT    — due date not yet passed (or no due date)
--      1-30       — 1 to 30 days overdue
--      31-60      — 31 to 60 days overdue
--      61-90      — 61 to 90 days overdue
--      90+        — more than 90 days overdue
-- =============================================================================

WITH grn_balances AS (
    SELECT
        g.id                                    AS grn_id,
        g.supplier_id,
        g.credit_due,
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
    supplier_id,

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

FROM open_balances
GROUP BY supplier_id
ORDER BY total_outstanding DESC;


-- =============================================================================
-- 4 : UNALLOCATED PAYMENT CREDITS PER SUPPLIER
--    Identifies payments where the supplier was paid more than what has
--    been allocated to GRNs — i.e. the supplier has a credit balance in
--    your favour that can be applied to future invoices.
-- =============================================================================

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



SELECT * FROM GRN WHERE SUPPLIER_ID = 8;
SELECT * FROM SUPPLIER_PAYMENT;
SELECT * FROM SUPPLIER_PAYMENT_ALLOCATION;



select 
g.id,
g.total,
g.cash,
g.cheque,
g.credit,
(g.total - coalesce(sum(pa.allocated_amount),0)) as outstanding

from grn g
left join supplier_payment_allocation pa on g.id = pa.grn_id
group by
g.id,
g.total,
g.cash,
g.cheque,
g.credit
;

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
set search_path to sales_manager;

select * from grn;
select * from supplier_payment;
select * from supplier_payment_allocation;

update grn set status = 'DRAFT' where id = 31;

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


SELECT
    SUM(g.credit) - COALESCE(SUM(spa.allocated_amount), 0) AS total_outstanding
FROM grn g
LEFT JOIN supplier_payment_allocation spa ON spa.grn_id = g.id
WHERE g.supplier_id = 6;

SELECT * FROM GRN;
select * from supplier_payment_allocation;

SELECT
  g.id,
  s.name,
  g.credit_due,
  g.total,
  COALESCE(pa.total_allocated, 0) AS total_paid,
  (g.total - COALESCE(pa.total_allocated,0)) AS outstanding_balance
FROM 
  grn g
LEFT JOIN (
  SELECT grn_id, sum(allocated_amount) AS total_allocated
  FROM supplier_payment_allocation
  GROUP BY grn_id
) pa ON g.id = pa.grn_id
LEFT JOIN supplier s ON s.supplier_id = g.supplier_id
WHERE
  (g.total - COALESCE(pa.total_allocated,0)) > 0;




-- views
CREATE VIEW outstanding_grns_per_supplier AS
WITH grn_balances AS (
    SELECT
        g.id                                    AS grn_id,
        g.supplier_id,
        g.received_date,
        g.credit_due,
        g.total,
        g.credit                                AS credit_at_delivery,
        COALESCE(SUM(spa.allocated_amount), 0)  AS paid_against_grn
    FROM grn g
    LEFT JOIN supplier_payment_allocation spa ON spa.grn_id = g.id
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

select * from outstanding_grns_per_supplier;

WITH grn_balances AS (
    SELECT
        g.supplier_id,
        g.credit                                AS credit_at_delivery,
        COALESCE(SUM(spa.allocated_amount), 0)  AS paid_against_grn
    FROM grn g
    LEFT JOIN supplier_payment_allocation spa ON spa.grn_id = g.id
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



SELECT
    g.id                                                          AS grn_id,
    g.supplier_id,
	s.name                                                        AS supplier_name,
    g.received_date,
    g.credit_due,
    g.credit                                                      AS original_credit_amount,
    COALESCE(SUM(spa.allocated_amount), 0)                        AS total_allocated,
    g.credit - COALESCE(SUM(spa.allocated_amount), 0)             AS outstanding_amount
FROM grn g
LEFT JOIN supplier_payment_allocation spa ON spa.grn_id = g.id
LEFT JOIN supplier s ON s.supplier_id = spa.supplier_id
WHERE g.credit > 0
GROUP BY g.id, g.supplier_id, g.received_date, g.credit_due, g.credit
-- HAVING g.credit - COALESCE(SUM(spa.allocated_amount), 0) > 0
ORDER BY g.supplier_id, g.credit_due;




-- Outstanding grns
SELECT
    g.id                                              AS grn_id,
    g.supplier_id,
	s.name AS supplier_name,
    g.received_date,
    g.credit_due,
    g.total,
    -- Amount paid at time of GRN (cash/cheque/bank transfer recorded directly on GRN)
    COALESCE(g.cash, 0) + COALESCE(g.cheque, 0)      AS paid_on_receipt,
    -- Amount allocated from supplier_payment records
    COALESCE(SUM(spa.allocated_amount), 0)            AS allocated_from_payments,
    -- Outstanding balance
    g.total
        -- - COALESCE(g.cash, 0)
        -- - COALESCE(g.cheque, 0)
        - COALESCE(SUM(spa.allocated_amount), 0)      AS outstanding_balance
FROM grn g
LEFT JOIN supplier_payment_allocation spa ON spa.grn_id = g.id
LEFT JOIN supplier s ON s.supplier_id = g.supplier_id
WHERE g.status = 'APPROVED'   -- adjust to your status values
GROUP BY
    g.id, g.supplier_id, s.name, g.received_date, g.credit_due, g.total,
    g.cash, g.cheque
HAVING
    g.total
        -- - COALESCE(g.cash, 0)
        -- - COALESCE(g.cheque, 0)
        - COALESCE(SUM(spa.allocated_amount), 0) > 0
ORDER BY g.received_date, g.id;

SELECT NAME FROM SUPPLIER EXCEPT
SELECT DISTINCT SUPPLIER_NAME FROM OUTSTANDING_GRNS_VIEW;

-- Outstanding grns per supplier
SELECT * FROM OUTSTANDING_GRNS_VIEW WHERE SUPPLIER_ID = 8;


-- Total outstanding per supplier

SELECT
    g.id,
    g.supplier_id,
	s.name AS supplier_name,
    g.total,
    -- Outstanding balance
    g.total
        - COALESCE(SUM(spa.allocated_amount), 0)    AS outstanding_balance
FROM grn g
LEFT JOIN supplier_payment_allocation spa ON spa.grn_id = g.id
LEFT JOIN supplier s ON s.supplier_id = g.supplier_id
WHERE g.status = 'APPROVED'   -- adjust to your status values
AND G.SUPPLIER_ID  = 8
GROUP BY
    g.id,g.supplier_id, s.name,g.total;

SELECT SUM()



SELECT NAME FROM SUPPLIER EXCEPT
SELECT DISTINCT SUPPLIER_NAME FROM OUTSTANDING_GRNS_VIEW;






SELECT * FROM GRN;
SELECT * FROM SUPPLIER_PAYMENT WHERE SUPPLIER_ID = 8;
SELECT * FROM SUPPLIER_PAYMENT_ALLOCATION WHERE PAYMENT_ID = 14;

SELECT * FROM OUTSTANDING_GRNS_VIEW WHERE SUPPLIER_ID = 8;
/*
GRN 35 30000
GRN 36  4000
UNALC  16000
*/

INSERT INTO supplier_payment (supplier_id, payment_method, direction, total_payment_amount,
                               cheque_number, bank, bank_account, reference_number, payment_date)
VALUES (8, 'CASH', 'IN', 52339,
        NULL, NULL, NULL, 'Refund', '2026-07-02');

-- INSERT INTO supplier_payment_allocation (payment_id, grn_id, allocated_amount)
-- VALUES (14, 35, 30000.00),(14,36,4000);


SELECT
	SUPPLIER_ID, 
	PAYMENT_METHOD, 
	TOTAL_PAYMENT_AMOUNT,
	GRN_ID, 
	ALLOCATED_AMOUNT
FROM SUPPLIER_PAYMENT P
INNER JOIN SUPPLIER_PAYMENT_ALLOCATION A ON A.PAYMENT_ID = P.ID
WHERE P.SUPPLIER_ID = 8

;

SELECT * FROM GRN G
WHERE G.SUPPLIER_ID = 8;


-- Unallocated payments
SELECT
    sp.supplier_id,
    sp.id                                               AS payment_id,
    sp.payment_date,
    sp.payment_method,
    sp.total_payment_amount,
    COALESCE(SUM(spa.allocated_amount), 0)              AS allocated,
    sp.total_payment_amount
        - COALESCE(SUM(spa.allocated_amount), 0)        AS unallocated_credit
FROM supplier_payment sp
LEFT JOIN supplier_payment_allocation spa ON spa.payment_id = sp.id
GROUP BY sp.id, sp.supplier_id, sp.payment_date, sp.payment_method, sp.total_payment_amount
HAVING sp.total_payment_amount - COALESCE(SUM(spa.allocated_amount), 0) > 0
ORDER BY sp.supplier_id, sp.payment_date;

select * from outstanding_balance_per_supplier;
select * from outstanding_grns_per_supplier;
select * from supplier_aging;
select * from unallocated_payment_cr_per_supplier;
select * from unposted_supplier_refunds;

