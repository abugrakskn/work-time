CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    due_date DATE,
    status VARCHAR(50) NOT NULL,
    project_id BIGINT NOT NULL,
    assigned_user_id BIGINT,

    CONSTRAINT fk_tasks_project
        FOREIGN KEY (project_id)
            REFERENCES projects(id),

    CONSTRAINT fk_tasks_assigned_user
        FOREIGN KEY (assigned_user_id)
            REFERENCES users(id)
);