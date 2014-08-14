# --- !Ups

CREATE TABLE products (
    id long NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ean long NOT NULL,
    name varchar DEFAULT NULL,
    description varchar DEFAULT NULL
);

CREATE TABLE warehouses (
    id long NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code varchar NOT NULL,
    name varchar DEFAULT NULL
);

CREATE TABLE stock_items (
    id long NOT NULL AUTO_INCREMENT PRIMARY KEY,
    productId long NOT NULL,
    warehouseId long NOT NULL,
    quantity long NOT NULL,
    FOREIGN KEY(productId) REFERENCES products(id),
    FOREIGN KEY(warehouseId) REFERENCES warehouses(id)
);

# --- !Downs

DROP TABLE IF EXISTS products;

DROP TABLE IF EXISTS warehouses;

DROP TABLE IF EXISTS stock_items;
