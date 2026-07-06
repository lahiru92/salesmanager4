INSERT INTO USERS VALUES('admin','$2a$12$UBaPoRsJ63rn0M91tMOQqOB3wgwfzG7eAIz6lOIt4F7WNNItvABhm',1,1);
INSERT INTO AUTHORITIES VALUES('admin', 'ROLE_ADMIN');
INSERT INTO EMPLOYEE(KNOWN_NAME) VALUES('Test Employee 1');

INSERT INTO SUPPLIER (NAME, PHONE, EMAIL, ACTIVE, CONTACT_PERSON) VALUES
('Global Logistics Corp', '+1-555-0198', 'info@globallogistics.com', TRUE, 'Sarah Jenkins'),
('Apex Industrial Tools', '+1-555-0147', 'sales@apextools.com', TRUE, 'Michael Chang'),
('Vertex Office Supplies', '+44-20-7946-0958', 'orders@vertexoffice.co.uk', TRUE, 'Emma Watson'),
('Quantum Tech Components', NULL, 'support@quantumtech.io', FALSE, 'David Miller'),
('EcoPack Packaging Solutions', '+61-2-5550-1234', 'contact@ecopack.com.au', TRUE, 'Sofia Rodriguez');

INSERT INTO CATEGORY (NAME, NORMALIZED_NAME) VALUES
('Logistics & Shipping', 'LOGISTICS_AND_SHIPPING'),
('Industrial Hardware', 'INDUSTRIAL_HARDWARE'),
('Office Stationery', 'OFFICE_STATIONERY'),
('IT & Electronics', 'IT_AND_ELECTRONICS'),
('Eco Janitorial & Breakroom', 'ECO_JANITORIAL_AND_BREAKROOM');

INSERT INTO ITEM (CODE, NAME, CATEGORY_ID, UNIT, REORDER_LEVEL, ACTIVE, SUPPLIER_ID) VALUES
-- SUPPLIER 1: Global Logistics Corp (Category 10: Logistics & Shipping)
('LOG-BOX-01', 'Heavy Duty Shipping Box - Large', 1, 'BOX', 50.00, TRUE, 1),
('LOG-BOX-02', 'Heavy Duty Shipping Box - Medium', 1, 'BOX', 75.00, TRUE, 1),
('LOG-BOX-03', 'Heavy Duty Shipping Box - Small', 1, 'BOX', 100.00, TRUE, 1),
('LOG-TAPE-01', 'Packing Tape Clear 2-inch', 1, 'PCS', 200.00, TRUE, 1),
('LOG-TAPE-02', 'Fragile Warning Tape 2-inch', 1, 'PCS', 50.00, TRUE, 1),
('LOG-WRAP-01', 'Bubble Wrap Roll 50m', 1, 'PCS', 20.00, TRUE, 1),
('LOG-WRAP-02', 'Stretch Wrap Film Clear', 1, 'PCS', 30.00, TRUE, 1),
('LOG-PAL-01', 'Standard Wooden Pallet', 1, 'PCS', 15.00, TRUE, 1),
('LOG-PAL-02', 'Heavy Duty Plastic Pallet', 1, 'PCS', 10.00, TRUE, 1),
('LOG-LBL-01', 'Thermal Shipping Labels 4x6', 1, 'ROLL', 40.00, TRUE, 1),
('LOG-LBL-02', 'Address Labels Small', 1, 'ROLL', 25.00, TRUE, 1),
('LOG-ENV-01', 'Padded Mailing Envelopes A4', 1, 'PACK', 120.00, TRUE, 1),
('LOG-ENV-02', 'Poly Mailers 10x13', 1, 'PACK', 150.00, TRUE, 1),
('LOG-STR-01', 'Plastic Strapping Band 1000m', 1, 'PCS', 5.00, TRUE, 1),
('LOG-CLIP-01', 'Metal Strapping Seals', 1, 'BOX', 15.00, TRUE, 1),
('LOG-CST-01', 'Utility Box Cutter', 1, 'PCS', 30.00, TRUE, 1),
('LOG-BLD-01', 'Replacement Cutter Blades', 1, 'BOX', 20.00, TRUE, 1),
('LOG-MARK-01', 'Black Permanent Markers', 1,'BOX', 15.00, TRUE, 1),
('LOG-SCALE-01', 'Digital Shipping Scale 50kg', 1, 'PCS', 2.00, TRUE, 1),
('LOG-TIE-01', 'Nylon Cable Ties 300mm', 1, 'PACK', 50.00, TRUE, 1),

-- SUPPLIER 2: Apex Industrial Tools (Category 11: Industrial Hardware)
('IND-WRE-01', 'Adjustable Wrench 10-inch', 2, 'PCS', 15.00, TRUE, 2),
('IND-WRE-02', 'Adjustable Wrench 12-inch', 2, 'PCS', 10.00, TRUE, 2),
('IND-SCR-01', 'Phillips Screwdriver PH2', 2, 'PCS', 40.00, TRUE, 2),
('IND-SCR-02', 'Slotted Screwdriver 6mm', 2, 'PCS', 40.00, TRUE, 2),
('IND-PLR-01', 'Combination Pliers 8-inch', 2, 'PCS', 25.00, TRUE, 2),
('IND-PLR-02', 'Long Nose Pliers 6-inch', 2, 'PCS', 20.00, TRUE, 2),
('IND-HAM-01', 'Claw Hammer 16oz', 2, 'PCS', 15.00, TRUE, 2),
('IND-HAM-02', 'Sledgehammer 4lb', 2, 'PCS', 5.00, TRUE, 2),
('IND-DRL-01', 'Cordless Rotary Drill 18V', 2, 'PCS', 8.00, TRUE, 2),
('IND-BIT-01', 'HSS Drill Bit Set 13pc', 2, 'BOX', 12.00, TRUE, 2),
('IND-SAW-01', 'Hacksaw Frame 12-inch', 2, 'PCS', 10.00, TRUE, 2),
('IND-BLD-02', 'Hacksaw Replacement Blades', 2, 'PACK', 30.00, TRUE, 2),
('IND-GPL-01', 'Safety Goggles Clear', 2, 'PCS', 50.00, TRUE, 2),
('IND-GLV-01', 'Heavy Duty Leather Gloves', 2, 'PAIR', 60.00, TRUE, 2),
('IND-MSK-01', 'N95 Particulate Mask', 2, 'BOX', 25.00, TRUE, 2),
('IND-TAP-01', 'Measuring Tape 8m/26ft', 2, 'PCS', 35.00, TRUE, 2),
('IND-LVL-01', 'Spirit Level 600mm', 2, 'PCS', 12.00, TRUE, 2),
('IND-WDG-01', 'WD-40 Lubricant 400ml', 2, 'PCS', 45.00, TRUE, 2),
('IND-CLN-01', 'Heavy Duty C-Clamp 6-inch', 2, 'PCS', 15.00, TRUE, 2),
('IND-BOLT-01', 'M8 Hex Bolts 50mm', 2, 'BOX', 20.00, TRUE, 2),

-- SUPPLIER 3: Vertex Office Supplies (Category 12: Office Stationery)
('OFF-PPR-A4', 'Premium A4 Copy Paper 80gsm', 3, 'BOX', 40.00, TRUE, 3),
('OFF-PPR-A3', 'Premium A3 Copy Paper 80gsm', 3, 'BOX', 10.00, TRUE, 3),
('OFF-PEN-BLU', 'Ballpoint Pens Blue 50pc', 3, 'BOX', 15.00, TRUE, 3),
('OFF-PEN-BLK', 'Ballpoint Pens Black 50pc', 3, 'BOX', 15.00, TRUE, 3),
('OFF-PEN-RED', 'Ballpoint Pens Red 50pc', 3, 'BOX', 8.00, TRUE, 3),
('OFF-HLR-01', 'Highlighter Assorted 4pc', 3, 'PACK', 20.00, TRUE, 3),
('OFF-NOT-01', 'Sticky Notes 3x3 Yellow', 3, 'PACK', 50.00, TRUE, 3),
('OFF-NOT-02', 'Sticky Notes Cube Assorted', 3, 'PCS', 30.00, TRUE, 3),
('OFF-NRA-01', 'Spiral Notebook A5 Ruled', 3, 'PCS', 60.00, TRUE, 3),
('OFF-NRB-01', 'Executive Notebook A4 Hardcover', 3, 'PCS', 25.00, TRUE, 3),
('OFF-FLD-01', 'Lever Arch File A4 Black', 3, 'PCS', 80.00, TRUE, 3),
('OFF-FLD-02', 'Expanding Document Wallet', 3, 'PCS', 40.00, TRUE, 3),
('OFF-STP-01', 'Desktop Stapler 25 Sheet', 3, 'PCS', 15.00, TRUE, 3),
('OFF-STP-02', 'Staples No. 10 5000pc', 3, 'BOX', 30.00, TRUE, 3),
('OFF-STP-03', 'Staple Remover', 3, 'PCS', 15.00, TRUE, 3),
('OFF-CLIP-02', 'Paper Clips 33mm 100pc', 3, 'BOX', 50.00, TRUE, 3),
('OFF-CLIP-03', 'Binder Clips 25mm 12pc', 3, 'BOX', 45.00, TRUE, 3),
('OFF-SCI-01', 'Office Scissors 8-inch', 3, 'PCS', 20.00, TRUE, 3),
('OFF-GLU-01', 'Glue Stick 21g', 3, 'PCS', 35.00, TRUE, 3),
('OFF-CAL-01', '12-Digit Desktop Calculator', 3, 'PCS', 10.00, TRUE, 3),

-- SUPPLIER 4: Quantum Tech Components (Category 4: IT & Electronics)
('TEC-MOU-01', 'Wireless Optical Mouse', 4, 'PCS', 25.00, TRUE, 4),
('TEC-KEY-01', 'USB Wired Keyboard US Layout', 4, 'PCS', 20.00, TRUE, 4),
('TEC-KEY-02', 'Wireless Keyboard & Mouse Combo', 4, 'PCS', 15.00, TRUE, 4),
('TEC-MON-24', '24-inch Full HD Monitor', 4, 'PCS', 8.00, TRUE, 4),
('TEC-MON-27', '27-inch 4K UHD Monitor', 4, 'PCS', 5.00, TRUE, 4),
('TEC-CBL-HDMI', 'HDMI 2.0 Cable 2m', 4, 'PCS', 40.00, TRUE, 4),
('TEC-CBL-CAT6', 'Cat6 Ethernet Cable 5m', 4, 'PCS', 35.00, TRUE, 4),
('TEC-CBL-USBC', 'USB-C Charging Cable 1m', 4, 'PCS', 50.00, TRUE, 4),
('TEC-ADP-01', 'USB-C Multiport Hub', 4, 'PCS', 12.00, TRUE, 4),
('TEC-HDD-1TB', 'External Hard Drive 1TB', 4, 'PCS', 15.00, TRUE, 4),
('TEC-SSD-500', 'Internal NVMe SSD 500GB', 4, 'PCS', 18.00, TRUE, 4),
('TEC-SSD-1TB', 'Internal NVMe SSD 1TB', 4, 'PCS', 12.00, TRUE, 4),
('TEC-RAM-08', 'DDR4 Desktop RAM 8GB', 4, 'PCS', 20.00, TRUE, 4),
('TEC-RAM-16', 'DDR4 Desktop RAM 16GB', 4, 'PCS', 15.00, TRUE, 4),
('TEC-USB-64', 'USB 3.0 Flash Drive 64GB', 4, 'PCS', 60.00, TRUE, 4),
('TEC-HDS-01', 'USB Office Headset with Mic', 4, 'PCS', 15.00, TRUE, 4),
('TEC-CAM-01', '1080p HD Pro Webcam', 4, 'PCS', 10.00, TRUE, 4),
('TEC-POW-01', 'Surge Protector 6-Way 3m', 4, 'PCS', 22.00, TRUE, 4),
('TEC-POW-02', 'Uninterruptible Power Supply 850VA', 4, 'PCS', 6.00, TRUE, 4),
('TEC-AIR-01', 'Compressed Air Duster 400ml', 4, 'PCS', 30.00, FALSE, 4), -- Inactive testing

-- SUPPLIER 5: EcoPack Packaging Solutions (Category 14: Eco Janitorial & Breakroom)
('ECO-PPR-01', 'Recycled Paper Towels Roll', 5, 'PACK', 40.00, TRUE, 5),
('ECO-TIS-01', 'Eco Toilet Tissue 2-ply', 5, 'PACK', 50.00, TRUE, 5),
('ECO-CUP-08', 'Compostable Coffee Cups 8oz', 5, 'BOX', 15.00, TRUE, 5),
('ECO-CUP-12', 'Compostable Coffee Cups 12oz', 5, 'BOX', 12.00, TRUE, 5),
('ECO-LID-01', 'Compostable Cup Lids', 5, 'BOX', 20.00, TRUE, 5),
('ECO-STR-02', 'Paper Drinking Straws', 5, 'BOX', 25.00, TRUE, 5),
('ECO-BAG-01', 'Biodegradable Trash Bags 30L', 5, 'PACK', 45.00, TRUE, 5),
('ECO-BAG-02', 'Biodegradable Trash Bags 50L', 5, 'PACK', 35.00, TRUE, 5),
('ECO-CLN-02', 'Eco All-Purpose Cleaner 1L', 5, 'LITER', 24.00, TRUE, 5),
('ECO-CLN-03', 'Eco Dishwashing Liquid 5L', 5, 'LITER', 10.00, TRUE, 5),
('ECO-SOAP-01', 'Organic Hand Soap Liquid 500ml', 5, 'PCS', 30.00, TRUE, 5),
('ECO-SOAP-02', 'Organic Hand Soap Refill 5L', 5, 'LITER', 8.00, TRUE, 5),
('ECO-WIP-01', 'Biodegradable Surface Wipes', 5, 'PACK', 40.00, TRUE, 5),
('ECO-SPO-01', 'Cellulose Cleaning Sponges 4pc', 5, 'PACK', 35.00, TRUE, 5),
('ECO-SPO-02', 'Wooden Coffee Stirrers 1000pc', 5, 'BOX', 15.00, TRUE, 5),
('ECO-PLT-01', 'Sugarcane Bagasse Plates 9-inch', 5, 'PACK', 30.00, TRUE, 5),
('ECO-BOW-01', 'Sugarcane Bagasse Bowls 12oz', 5, 'PACK', 25.00, TRUE, 5),
('ECO-CUT-01', 'Wooden Fork Set 100pc', 5, 'PACK', 20.00, TRUE, 5),
('ECO-CUT-02', 'Wooden Knife Set 100pc', 5, 'PACK', 20.00, TRUE, 5),
('ECO-CUT-03', 'Wooden Spoon Set 100pc', 5, 'PACK', 20.00, TRUE, 5);


-- ================================================================
-- GRN
-- ================================================================
INSERT INTO "PUBLIC"."GRN"("ID", "PURCHASE_ORDER_ID", "STATUS", "RECEIVED_DATE", "SUPPLIER_ID", "EMPLOYEE_ID", "CASH", "CHEQUE", "CREDIT", "TOTAL", "CREDIT_DUE") OVERRIDING SYSTEM VALUE VALUES(1000, NULL, 'DRAFT', DATE '2026-06-01', 2, 1, 2500.00, 0.00, 90000.00, 92500.00, DATE '2026-06-30');
INSERT INTO "PUBLIC"."GRN_ITEM"("ID", "GRN_KEY", "GRN_ID", "ITEM_ID", "ITEM_NAME", "ORDERED_QTY", "RECEIVED_QTY", "REJECTED_QTY", "UNIT_PRICE", "ORDERED_PRICE") OVERRIDING SYSTEM VALUE VALUES(1000, 0, 1000, 21, 'Adjustable Wrench 10-inch', NULL, 50.00, 0.00, 1850.00, NULL);
