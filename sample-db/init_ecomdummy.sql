-- =========================================================================================
-- E-Commerce Dummy Database Generator (ecomdummy)
-- Run this script in MySQL Workbench to create the database and generate massive data.
-- =========================================================================================

CREATE DATABASE IF NOT EXISTS ecomdummy;
USE ecomdummy;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS customers;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Categories Table
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- 2. Products Table
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
);

-- 3. Customers Table
CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(20),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Orders Table
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- 5. Order Items Table
CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT
);

-- =========================================================================================
-- STORED PROCEDURE TO GENERATE MASSIVE DATA
-- =========================================================================================

DELIMITER //

DROP PROCEDURE IF EXISTS generate_ecommerce_data //

CREATE PROCEDURE generate_ecommerce_data(
    IN p_num_customers INT,
    IN p_num_products INT,
    IN p_num_orders INT
)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE random_cat_id INT;
    DECLARE random_cust_id INT;
    DECLARE random_prod_id INT;
    DECLARE random_qty INT;
    DECLARE random_price DECIMAL(10,2);
    DECLARE current_order_id INT;
    
    -- Disable foreign key checks and autocommit for much faster insertion
    SET FOREIGN_KEY_CHECKS = 0;
    SET autocommit = 0;

    -- Insert 10 Categories manually
    INSERT INTO categories (name, description) VALUES
    ('Electronics', 'Gadgets, devices, and accessories'),
    ('Clothing', 'Apparel for men and women'),
    ('Home & Kitchen', 'Furniture, decor, and appliances'),
    ('Sports & Outdoors', 'Fitness equipment and outdoor gear'),
    ('Books', 'Fiction, non-fiction, and academic books'),
    ('Toys & Games', 'Toys for kids and board games'),
    ('Beauty & Personal Care', 'Cosmetics, skincare, and grooming'),
    ('Automotive', 'Car accessories and parts'),
    ('Grocery', 'Daily food and household supplies'),
    ('Pet Supplies', 'Food and accessories for pets');

    -- Generate Customers
    SET i = 1;
    WHILE i <= p_num_customers DO
        INSERT INTO customers (first_name, last_name, email, phone, registration_date)
        VALUES (
            CONCAT('Customer', i),
            CONCAT('LastName', i),
            CONCAT('customer', i, '@example.com'),
            CONCAT('9', LPAD(FLOOR(RAND() * 1000000000), 9, '0')),
            TIMESTAMPADD(DAY, -FLOOR(RAND() * 1000), CURRENT_TIMESTAMP)
        );
        SET i = i + 1;
    END WHILE;

    -- Generate Products
    SET i = 1;
    WHILE i <= p_num_products DO
        SET random_cat_id = FLOOR(1 + (RAND() * 10));
        INSERT INTO products (category_id, name, price, stock, created_at)
        VALUES (
            random_cat_id,
            CONCAT('Product Name ', i, ' - ', random_cat_id),
            ROUND(RAND() * 5000 + 10, 2), -- Price between 10 and 5010
            FLOOR(RAND() * 1000),         -- Stock between 0 and 1000
            TIMESTAMPADD(DAY, -FLOOR(RAND() * 1000), CURRENT_TIMESTAMP)
        );
        SET i = i + 1;
    END WHILE;

    -- Generate Orders & Order Items
    SET i = 1;
    WHILE i <= p_num_orders DO
        SET random_cust_id = FLOOR(1 + (RAND() * p_num_customers));
        
        -- Insert Order
        INSERT INTO orders (customer_id, order_date, status, total_amount)
        VALUES (
            random_cust_id,
            TIMESTAMPADD(DAY, -FLOOR(RAND() * 365), CURRENT_TIMESTAMP),
            ELT(FLOOR(1 + (RAND() * 5)), 'PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'),
            0 -- Will be updated after inserting items
        );
        
        SET current_order_id = LAST_INSERT_ID();
        
        -- Generate 1 to 5 items per order
        SET @num_items = FLOOR(1 + (RAND() * 5));
        SET @j = 1;
        SET @order_total = 0;
        
        WHILE @j <= @num_items DO
            SET random_prod_id = FLOOR(1 + (RAND() * p_num_products));
            SET random_qty = FLOOR(1 + (RAND() * 5));
            
            -- Get price from product (simulated random price here for speed)
            SET random_price = ROUND(RAND() * 5000 + 10, 2);
            
            INSERT INTO order_items (order_id, product_id, quantity, unit_price)
            VALUES (current_order_id, random_prod_id, random_qty, random_price);
            
            SET @order_total = @order_total + (random_qty * random_price);
            SET @j = @j + 1;
        END WHILE;
        
        -- Update order total
        UPDATE orders SET total_amount = @order_total WHERE order_id = current_order_id;
        
        SET i = i + 1;
    END WHILE;

    -- Re-enable constraints and commit
    SET FOREIGN_KEY_CHECKS = 1;
    COMMIT;
    SET autocommit = 1;
    
END //

DELIMITER ;

-- =========================================================================================
-- EXECUTE THE STORED PROCEDURE
-- This will generate:
-- 10,000 Customers
-- 5,000 Products
-- 20,000 Orders (and up to 100,000 order items)
-- (Execution might take 10-30 seconds depending on your machine)
-- =========================================================================================

CALL generate_ecommerce_data(10000, 5000, 20000);

-- Query the results
SELECT 'Categories generated:' AS summary, COUNT(*) FROM categories
UNION ALL
SELECT 'Products generated:', COUNT(*) FROM products
UNION ALL
SELECT 'Customers generated:', COUNT(*) FROM customers
UNION ALL
SELECT 'Orders generated:', COUNT(*) FROM orders
UNION ALL
SELECT 'Order Items generated:', COUNT(*) FROM order_items;
