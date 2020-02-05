CREATE TABLE customer
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name    VARCHAR(50),
    last_name     VARCHAR(50) NOT NULL,
    date_of_birth VARCHAR(10),

    CONSTRAINT pk_customer PRIMARY KEY (ID)
);

CREATE TABLE account
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT         NOT NULL,
    currency    VARCHAR(3)     NOT NULL,
    balance     DECIMAL(19, 4) NOT NULL DEFAULT 0,
    version     BIGINT         not null,

    CONSTRAINT pk_account PRIMARY KEY (ID),
    CONSTRAINT fk_customer_id FOREIGN KEY (CUSTOMER_ID) REFERENCES CUSTOMER (ID)
);

CREATE TABLE supported_currencies
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    currency VARCHAR(3) NOT NULL,

    CONSTRAINT pk_supported_currencies PRIMARY KEY (ID)
);

CREATE TABLE currency_exchange_rate
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_currency VARCHAR(3)     NOT NULL,
    target_currency VARCHAR(3)     NOT NULL,
    exchange_rate   DECIMAL(19, 4) NOT NULL,

    CONSTRAINT pk_currency_exchange_rate PRIMARY KEY (ID)
)