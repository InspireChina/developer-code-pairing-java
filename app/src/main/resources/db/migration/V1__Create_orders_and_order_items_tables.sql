-- Create orders table
CREATE
    TABLE
        orders(
            id VARCHAR(36) PRIMARY KEY,
            order_number VARCHAR(20) NOT NULL UNIQUE,
            user_id VARCHAR(36) NOT NULL,
            merchant_id VARCHAR(36) NOT NULL, -- Delivery information
            recipient_name VARCHAR(100) NOT NULL,
            recipient_phone VARCHAR(11) NOT NULL,
            address VARCHAR(500) NOT NULL, -- Remark
            remark VARCHAR(200), -- Status
            status VARCHAR(20) NOT NULL, -- Pricing information
            items_total DECIMAL(
                10,
                2
            ) NOT NULL,
            packaging_fee DECIMAL(
                10,
                2
            ) NOT NULL,
            delivery_fee DECIMAL(
                10,
                2
            ) NOT NULL,
            final_amount DECIMAL(
                10,
                2
            ) NOT NULL, -- Timestamps
            created_at TIMESTAMP NOT NULL,
            updated_at TIMESTAMP NOT NULL
        );

-- Create indexes for orders table
CREATE
    INDEX idx_user_id ON
    orders(user_id);

CREATE
    INDEX idx_merchant_id ON
    orders(merchant_id);

CREATE
    INDEX idx_order_number ON
    orders(order_number);

CREATE
    INDEX idx_created_at ON
    orders(created_at);

-- Create order_items table
CREATE
    TABLE
        order_items(
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            order_id VARCHAR(36) NOT NULL,
            dish_id VARCHAR(36) NOT NULL,
            dish_name VARCHAR(200) NOT NULL,
            quantity INT NOT NULL,
            price DECIMAL(
                10,
                2
            ) NOT NULL,
            FOREIGN KEY(order_id) REFERENCES orders(id) ON
            DELETE
                CASCADE
        );

-- Create index for order_items table
CREATE
    INDEX idx_order_id ON
    order_items(order_id);
