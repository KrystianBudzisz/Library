INSERT INTO book_categories (category_name)
VALUES ('Horror'),
       ('Fantasy'),
       ('Science Fiction'),
       ('Romance'),
       ('Thriller'),
       ('Non-Fiction');
INSERT INTO customers (first_name, last_name, email, email_confirmed, confirmation_token)
VALUES ('John', 'Doe', 'john.doe@example.com', FALSE, 'token123'),
       ('Jane', 'Doe', 'jane.doe@example.com', TRUE, 'token456');
INSERT INTO books (author, title, category_id, version)
VALUES ('Stephen King', 'IT', (SELECT id FROM book_categories WHERE category_name = 'Horror'), 1),
       ('J.R.R. Tolkien', 'The Hobbit', (SELECT id FROM book_categories WHERE category_name = 'Fantasy'), 1),
       ('Isaac Asimov', 'Foundation', (SELECT id FROM book_categories WHERE category_name = 'Science Fiction'), 1);
INSERT INTO subscriptions (customer_id, author, category_id, version)
VALUES ((SELECT id FROM customers WHERE email = 'john.doe@example.com'), 'Stephen King',
        (SELECT id FROM book_categories WHERE category_name = 'Horror'), 1),
       ((SELECT id FROM customers WHERE email = 'jane.doe@example.com'), 'J.R.R. Tolkien',
        (SELECT id FROM book_categories WHERE category_name = 'Fantasy'), 1);