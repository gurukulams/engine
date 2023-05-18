CREATE TABLE categories (
    id VARCHAR(55) PRIMARY KEY,
    title TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(55) NOT NULL,
    modified_at TIMESTAMP,
    modified_by VARCHAR(200),
    CONSTRAINT categories_title_constraint UNIQUE (title)
);

CREATE TABLE categories_localized (
    category_id VARCHAR(55),
    locale VARCHAR(8) NOT NULL,
    title TEXT,
    FOREIGN KEY (category_id) REFERENCES categories (id),
    PRIMARY KEY(category_id, locale)
);