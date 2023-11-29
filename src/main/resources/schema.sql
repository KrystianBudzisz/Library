CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    email_confirmed BOOLEAN DEFAULT FALSE,
    confirmation_token VARCHAR(255),
    CONSTRAINT uk_email UNIQUE (email)
    );

CREATE TABLE IF NOT EXISTS book_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_category_name UNIQUE (category_name)
    );

CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    category_id BIGINT,
    version INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uk_author_title UNIQUE (author, title),
    FOREIGN KEY (category_id) REFERENCES book_categories(id) ON DELETE CASCADE
    );
 CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT,
    author VARCHAR(100),
    category_id BIGINT,
    version INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uk_subscription_customer_author_category UNIQUE (customer_id, author, category_id),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES book_categories(id)
    );
