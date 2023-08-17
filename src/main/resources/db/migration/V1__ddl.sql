CREATE TABLE communities (
    id VARCHAR(55) PRIMARY KEY,
    title TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(55) NOT NULL,
    modified_at TIMESTAMP,
    modified_by VARCHAR(200),
    CONSTRAINT communities_title_constraint UNIQUE (title)
);

CREATE TABLE communities_localized (
    community_id VARCHAR(55),
    locale VARCHAR(8) NOT NULL,
    title TEXT,
    FOREIGN KEY (community_id) REFERENCES communities (id),
    PRIMARY KEY(community_id, locale)
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

CREATE TABLE events (
    id VARCHAR(20) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    location VARCHAR(200) NOT NULL,
    starts_at TIMESTAMP,
    ends_at TIMESTAMP,
    description TEXT,
    organizer VARCHAR(200),
    max_attendees INT,
    CONSTRAINT events_title_constraint UNIQUE (title)
);

CREATE TABLE events_localized (
    event_id VARCHAR(55),
    locale VARCHAR(8) NOT NULL,
    title TEXT,
    FOREIGN KEY (event_id) REFERENCES events (id),
    PRIMARY KEY(event_id, locale)
);