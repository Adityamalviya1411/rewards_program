DELETE FROM transactions;
DELETE FROM customers;

-- Customers
INSERT INTO customers (name, email) VALUES
    ('Aditya Malviya',    'aditya@example.com'),
    ('Shreya Gupta',      'shreya@example.com'),
    ('Himanshu Sharma',   'himanshu.sharma@example.com'),
    ('Preeyanshu Singh',  'preeyanshu.singh@example.com');

-- Aditya Malviya (id=1): Jan=115pts, Feb=255pts, Mar=110pts => total=480
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (1, 120.00, '2024-01-05');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (1,  75.00, '2024-01-18');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (1,  45.00, '2024-01-25');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (1, 200.00, '2024-02-10');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (1,  55.00, '2024-02-20');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (1, 110.00, '2024-03-08');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (1,  90.00, '2024-03-22');

-- Shreya Gupta (id=2): Jan=160pts, Feb=140pts, Mar=200pts => total=500
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (2,  60.00, '2024-01-12');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (2, 150.00, '2024-01-19');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (2,  30.00, '2024-02-05');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (2, 130.00, '2024-02-14');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (2,  80.00, '2024-02-28');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (2, 175.00, '2024-03-03');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (2,  50.00, '2024-03-17');

-- Himanshu Sharma (id=3): Jan=550pts, Feb=60pts, Mar=495pts => total=1105
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (3, 300.00, '2024-01-07');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (3,  40.00, '2024-02-11');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (3, 105.00, '2024-02-22');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (3,  95.00, '2024-03-14');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (3, 250.00, '2024-03-29');

-- Preeyanshu Singh (id=4): Jan=0pts, Feb=50pts, Mar=100pts => total=150
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (4,  20.00, '2024-01-09');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (4,  50.00, '2024-01-23');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (4, 100.00, '2024-02-16');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (4, 115.00, '2024-03-11');
INSERT INTO transactions (customer_id, amount, transaction_date) VALUES (4,  70.00, '2024-03-25');
