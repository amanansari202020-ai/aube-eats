CREATE TABLE roles (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE app_users (
  id BINARY(16) NOT NULL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  refresh_token_hash VARCHAR(64),
  refresh_expires_at TIMESTAMP(6) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE TABLE user_roles (
  user_id BINARY(16) NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES app_users(id),
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE addresses (
  id BINARY(16) NOT NULL PRIMARY KEY,
  user_id BINARY(16) NOT NULL,
  label VARCHAR(80) NOT NULL,
  street VARCHAR(180) NOT NULL,
  apartment VARCHAR(100),
  city VARCHAR(80) NOT NULL,
  postal_code VARCHAR(16) NOT NULL,
  country_code VARCHAR(2) NOT NULL DEFAULT 'US',
  CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES app_users(id)
);

CREATE TABLE categories (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80) NOT NULL UNIQUE,
  description VARCHAR(240),
  active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE menu_items (
  id VARCHAR(64) NOT NULL PRIMARY KEY,
  category_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  description VARCHAR(600) NOT NULL,
  price DECIMAL(9,2) NOT NULL,
  image_url VARCHAR(500),
  vegetarian BOOLEAN NOT NULL DEFAULT FALSE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_menu_category FOREIGN KEY (category_id) REFERENCES categories(id),
  INDEX idx_menu_active_category (active, category_id)
);

CREATE TABLE carts (
  id BINARY(16) NOT NULL PRIMARY KEY,
  user_id BINARY(16) NOT NULL UNIQUE,
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES app_users(id)
);

CREATE TABLE cart_items (
  id BINARY(16) NOT NULL PRIMARY KEY,
  cart_id BINARY(16) NOT NULL,
  menu_item_id VARCHAR(64) NOT NULL,
  quantity INT NOT NULL,
  UNIQUE KEY uk_cart_item (cart_id, menu_item_id),
  CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id),
  CONSTRAINT fk_cart_items_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

CREATE TABLE customer_orders (
  id BINARY(16) NOT NULL PRIMARY KEY,
  user_id BINARY(16) NOT NULL,
  status VARCHAR(24) NOT NULL,
  total DECIMAL(10,2) NOT NULL,
  delivery_address VARCHAR(300) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_orders_user_created (user_id, created_at),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES app_users(id)
);

CREATE TABLE order_items (
  id BINARY(16) NOT NULL PRIMARY KEY,
  order_id BINARY(16) NOT NULL,
  menu_item_id VARCHAR(64) NOT NULL,
  item_name VARCHAR(120) NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(9,2) NOT NULL,
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES customer_orders(id),
  CONSTRAINT fk_order_items_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

CREATE TABLE payments (
  id BINARY(16) NOT NULL PRIMARY KEY,
  order_id BINARY(16) NOT NULL UNIQUE,
  method VARCHAR(24) NOT NULL,
  status VARCHAR(24) NOT NULL,
  provider_payment_id VARCHAR(120),
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES customer_orders(id)
);

CREATE TABLE coupons (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(48) NOT NULL UNIQUE,
  discount_percent DECIMAL(5,2) NOT NULL,
  expires_at TIMESTAMP(6) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE reviews (
  id BINARY(16) NOT NULL PRIMARY KEY,
  user_id BINARY(16) NOT NULL,
  menu_item_id VARCHAR(64) NOT NULL,
  rating INT NOT NULL,
  comment VARCHAR(1000),
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_review_user_item (user_id, menu_item_id),
  CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES app_users(id),
  CONSTRAINT fk_reviews_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');
INSERT INTO categories (name, description) VALUES
  ('Bagels', 'Hand rolled and kettle boiled'),
  ('Plates', 'A slower, softer start'),
  ('Coffee', 'Carefully pulled and steeped');

INSERT INTO menu_items (id, category_id, name, description, price, image_url, vegetarian, active) VALUES
  ('sesame', 1, 'The Sesame', 'Whipped scallion cream cheese, cucumber, dill.', 9.00, 'https://images.unsplash.com/photo-1627308595229-7830a5c91f9f?auto=format&fit=crop&w=900&q=85', TRUE, TRUE),
  ('sunrise', 1, 'Sunrise Club', 'Soft egg, cheddar, chili crisp, honey butter.', 12.00, 'https://images.unsplash.com/photo-1525351484163-7529414344d8?auto=format&fit=crop&w=900&q=85', TRUE, TRUE),
  ('lox', 1, 'The Sunday Lox', 'Smoked salmon, caper cream cheese, red onion.', 15.00, 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=900&q=85', FALSE, TRUE),
  ('garden', 1, 'Little Green Thing', 'Avocado, pickled shallot, lemon, seed crunch.', 13.00, 'https://images.unsplash.com/photo-1525351484163-7529414344d8?auto=format&fit=crop&w=900&q=85', TRUE, TRUE),
  ('tomato', 2, 'Tomato & Good Oil', 'Heirloom tomato, whipped ricotta, basil oil.', 13.00, 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=900&q=85', TRUE, TRUE),
  ('granola', 2, 'A Very Good Bowl', 'Brown-butter granola, yogurt, orchard fruit.', 11.00, 'https://images.unsplash.com/photo-1517673132405-a56a62b18caf?auto=format&fit=crop&w=900&q=85', TRUE, TRUE),
  ('latte', 3, 'The Slow Latte', 'Double espresso, oat milk, a little maple.', 6.00, 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&w=900&q=85', TRUE, TRUE),
  ('coldbrew', 3, 'Window Seat Cold Brew', '18-hour steep, chocolate notes, no hurry.', 5.00, 'https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?auto=format&fit=crop&w=900&q=85', TRUE, TRUE);