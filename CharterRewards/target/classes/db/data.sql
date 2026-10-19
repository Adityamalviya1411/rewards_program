-- Clear existing data and reset IDs to allow re-runs
TRUNCATE TABLE transactions, customers RESTART IDENTITY CASCADE;

-- Customers
INSERT INTO customers (name, email) VALUES
    ('Aditya Malviya', 'aditya@example.com'),
    ('Shreya Gupta',     'shreya@example.com'),
    ('Himanshu Sharma',   'himanshu.sharma@example.com'),
    ('Preeyanshu Singh',   'preeyanshu.singh@example.com');

-- Aditya Malviya (customer_id = 1)
-- January
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (1, 120.00, '2024-01-05');  -- 90 pts
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (1,  75.00, '2024-01-18');  -- 25 pts
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (1,  45.00, '2024-01-25');  --  0 pts
-- February
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (1, 200.00, '2024-02-10');  -- 250 pts
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (1,  55.00, '2024-02-20');  --   5 pts
-- March
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (1, 110.00, '2024-03-08');  -- 70 pts
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (1,  90.00, '2024-03-22');  -- 40 pts

-- Shreya Gupta (customer_id = 2)
-- January
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (2,  60.00, '2024-01-12');  -- 10 pts
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (2, 150.00, '2024-01-19');  -- 150 pts
-- February
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (2,  30.00, '2024-02-05');  --   0 pts
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (2, 130.00, '2024-02-14');  -- 110 pts
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (2,  80.00, '2024-02-28');  --  30 pts
-- March
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (2, 175.00, '2024-03-03');  -- 200 pts
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (2,  50.00, '2024-03-17');  --   0 pts

-- Himanshu Sharma (customer_id = 3)
-- January
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (3, 300.00, '2024-01-07');  -- 550 pts
-- February
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (3,  40.00, '2024-02-11');  --   0 pts
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (3, 105.00, '2024-02-22');  --  60 pts
-- March
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (3,  95.00, '2024-03-14');  --  45 pts
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (3, 250.00, '2024-03-29');  -- 450 pts

-- Preeyanshu Singh (customer_id = 4)
-- January
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (4,  20.00, '2024-01-09');  --   0 pts
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (4,  50.00, '2024-01-23');  --   0 pts
-- February
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (4, 100.00, '2024-02-16');  --  50 pts
-- March
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (4, 115.00, '2024-03-11');  --  80 pts
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (4,  70.00, '2024-03-25');  --  20 pts
