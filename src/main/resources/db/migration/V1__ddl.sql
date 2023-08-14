CREATE TABLE organizations (
    id VARCHAR(55) PRIMARY KEY,
    title TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(55) NOT NULL,
    modified_at TIMESTAMP,
    modified_by VARCHAR(200),
    CONSTRAINT organizations_title_constraint UNIQUE (title)
);

CREATE TABLE organizations_localized (
    organization_id VARCHAR(55),
    locale VARCHAR(8) NOT NULL,
    title TEXT,
    FOREIGN KEY (organization_id) REFERENCES organizations (id),
    PRIMARY KEY(organization_id, locale)
);

CREATE TABLE handle (
    id VARCHAR(55) PRIMARY KEY,
    type VARCHAR(55),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT type_id_constraint UNIQUE (type, id)
);

CREATE TABLE learner (
    id UUID PRIMARY KEY,
    email VARCHAR(200) NOT NULL,
    image_url VARCHAR(200) NOT NULL,
    provider VARCHAR(50) DEFAULT 'local' NOT NULL,
    password VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(55) NOT NULL,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_by VARCHAR(200),
    CONSTRAINT learner_email_constraint UNIQUE (email)
);


CREATE TABLE learner_profile (
    id VARCHAR(55) PRIMARY KEY,
    learner_id UUID NOT NULL UNIQUE,
    first_name VARCHAR(200) NOT NULL,
    last_name VARCHAR(200) NOT NULL,
    dob DATE NOT NULL,
    FOREIGN KEY (learner_id) REFERENCES learner (id),
    FOREIGN KEY (id) REFERENCES handle (id)
);