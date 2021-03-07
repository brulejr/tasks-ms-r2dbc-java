CREATE TABLE IF NOT EXISTS t_task (
    task_id SERIAL PRIMARY KEY,
    guid UUID NOT NULL,
    name VARCHAR(64) NOT NULL,
    description VARCHAR(256),
    created_by VARCHAR(64) DEFAULT 'SYSTEM',
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_by VARCHAR(64) DEFAULT 'SYSTEM',
    modified_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_history (
    history_id SERIAL PRIMARY KEY,
    entity_type VARCHAR(64) NOT NULL,
    entity_id NUMBER,
    event_type VARCHAR(64) NOT NULL,
    created_by VARCHAR(64) DEFAULT 'SYSTEM',
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_lookup_value (
    lkval_id SERIAL PRIMARY KEY,
    entity_type VARCHAR(64) NOT NULL,
    entity_id NUMBER,
    lookup_value_type VARCHAR(64) NOT NULL,
    lookup_value VARCHAR(64) NOT NULL
);
