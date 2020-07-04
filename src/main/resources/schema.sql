DROP TABLE IF EXISTS TREKKING_DETAILS;

CREATE TABLE ORDER_DETAIL(
	order_id INT AUTO_INCREMENT PRIMARY KEY,
	order_code varchar(255) NOT NULL,
	customer_name varchar(255) NOT NULL,
	customer_address varchar(255) NOT NULL,
	cutomer_number varchar(255) NOT NULL,
	product_id varchar(255) NOT NULL,
	product_name varchar(255) NOT NULL,
	product_price varchar(255) NOT NULL
);