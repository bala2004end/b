-- =======================================================
-- AI Database Assistant Sample Database Script
-- Database: sample_company_db
-- =======================================================

CREATE DATABASE IF NOT EXISTS sample_company_db;
USE sample_company_db;

-- 1. Departments Table
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS departments;

CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    budget DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Employees Table
CREATE TABLE employees (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    hire_date DATE NOT NULL,
    job_title VARCHAR(50) NOT NULL,
    salary DECIMAL(10, 2) NOT NULL,
    department_id INT,
    FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE SET NULL
);

-- 3. Categories Table
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    description TEXT
);

-- 4. Products Table
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(150) NOT NULL,
    category_id INT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL
);

-- 5. Customers Table
CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Orders Table
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- 7. Order Items Table
CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- =======================================================
-- POPULATE SAMPLE DATA
-- =======================================================

-- Insert Departments
INSERT INTO departments (department_name, location, budget) VALUES
('Engineering', 'Bangalore', 2500000.00),
('Sales & Marketing', 'Mumbai', 1800000.00),
('Human Resources', 'Delhi', 800000.00),
('Finance', 'Hyderabad', 1200000.00),
('Customer Support', 'Pune', 950000.00);

-- Insert Employees
INSERT INTO employees (first_name, last_name, email, phone, hire_date, job_title, salary, department_id) VALUES
('Aarav', 'Sharma', 'aarav.sharma@example.com', '9876543210', '2024-01-15', 'Senior Software Engineer', 1450000.00, 1),
('Priya', 'Patel', 'priya.patel@example.com', '9876543211', '2024-02-01', 'Tech Lead', 1850000.00, 1),
('Rohan', 'Verma', 'rohan.verma@example.com', '9876543212', '2026-07-10', 'Software Engineer', 950000.00, 1),
('Ananya', 'Rao', 'ananya.rao@example.com', '9876543213', '2026-07-20', 'Sales Manager', 1300000.00, 2),
('Vikram', 'Singh', 'vikram.singh@example.com', '9876543214', '2023-05-12', 'Marketing Specialist', 820000.00, 2),
('Neha', 'Gupta', 'neha.gupta@example.com', '9876543215', '2026-07-05', 'HR Manager', 1100000.00, 3),
('Siddharth', 'Nair', 'siddharth.nair@example.com', '9876543216', '2022-11-01', 'Financial Analyst', 1050000.00, 4),
('Kavya', 'Reddy', 'kavya.reddy@example.com', '9876543217', '2026-07-18', 'Support Lead', 750000.00, 5);

-- Insert Categories
INSERT INTO categories (category_name, description) VALUES
('Electronics', 'Gadgets, laptops, smartphones and accessories'),
('Furniture', 'Office and home ergonomic furniture'),
('Stationery', 'Office supplies and writing equipment'),
('Software Licenses', 'Enterprise and developer cloud subscriptions');

-- Insert Products
INSERT INTO products (product_name, category_id, price, stock_quantity) VALUES
('MacBook Pro M3 16-inch', 1, 249999.00, 15),
('Dell UltraSharp 27" 4K Monitor', 1, 45000.00, 8),
('Logitech MX Master 3S Mouse', 1, 9995.00, 45),
('Keychron K2 Mechanical Keyboard', 1, 8499.00, 12),
('Ergonomic Mesh Office Chair', 2, 18500.00, 5),
('Standing Desk (Electric Adjustable)', 2, 34999.00, 18),
('Premium Executive Journal Pack', 3, 1200.00, 120),
('Jet Black Gel Pen Box (Pack of 50)', 3, 650.00, 250),
('JetBrains All Products Pack License', 4, 28900.00, 100),
('AWS Cloud Credit Voucher ₹50k', 4, 50000.00, 30);

-- Insert Customers
INSERT INTO customers (name, email, phone, city, state) VALUES
('TechCorp Solutions', 'contact@techcorp.io', '9123456780', 'Bangalore', 'Karnataka'),
('Apex Global Media', 'procurement@apexglobal.com', '9123456781', 'Mumbai', 'Maharashtra'),
('Innovate Labs', 'info@innovatelabs.in', '9123456782', 'Hyderabad', 'Telangana'),
('Skyline Enterprises', 'admin@skyline.org', '9123456783', 'Delhi', 'Delhi'),
('Quantum Analytics', 'billing@quantumanalytics.co', '9123456784', 'Pune', 'Maharashtra');

-- Insert Orders
INSERT INTO orders (customer_id, order_date, total_amount, status) VALUES
(1, '2026-06-10 10:30:00', 544993.00, 'COMPLETED'),
(2, '2026-06-15 14:20:00', 63500.00, 'COMPLETED'),
(3, '2026-07-01 09:15:00', 142495.00, 'COMPLETED'),
(4, '2026-07-05 16:45:00', 18500.00, 'COMPLETED'),
(5, '2026-07-22 11:00:00', 107898.00, 'COMPLETED');

-- Insert Order Items
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
-- Order 1: TechCorp Solutions (Total > ₹50000)
(1, 1, 2, 249999.00), -- 2 MacBook Pros
(1, 2, 1, 45000.00),  -- 1 Monitor
-- Order 2: Apex Global Media (Total > ₹50000)
(2, 2, 1, 45000.00),  -- 1 Monitor
(2, 5, 1, 18500.00),  -- 1 Chair
-- Order 3: Innovate Labs (Total > ₹50000)
(3, 6, 3, 34999.00),  -- 3 Standing Desks
(3, 4, 4, 8499.00),   -- 4 Keyboards
-- Order 4: Skyline Enterprises
(4, 5, 1, 18500.00),  -- 1 Chair
-- Order 5: Quantum Analytics (Total > ₹50000)
(5, 9, 3, 28900.00),  -- 3 JetBrains Packs
(5, 3, 2, 9995.00);   -- 2 Mice

-- Views & Indexes for RAG schema loading demonstration
CREATE OR REPLACE VIEW vw_department_salary_summary AS
SELECT 
    d.department_id,
    d.department_name,
    COUNT(e.employee_id) AS total_employees,
    COALESCE(SUM(e.salary), 0) AS total_salary_expense,
    COALESCE(AVG(e.salary), 0) AS average_salary
FROM departments d
LEFT JOIN employees e ON d.department_id = e.department_id
GROUP BY d.department_id, d.department_name;

CREATE INDEX idx_employee_hire_date ON employees(hire_date);
CREATE INDEX idx_products_stock ON products(stock_quantity);
CREATE INDEX idx_orders_customer ON orders(customer_id);
