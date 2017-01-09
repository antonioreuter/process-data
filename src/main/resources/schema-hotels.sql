DROP TABLE hotels IF EXISTS;

CREATE TABLE hotels (
    id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    stars INT NOT NULL,
    contact VARCHAR(500) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    uri VARCHAR(500) NOT NULL,
    grade INT NOT NULL
 )