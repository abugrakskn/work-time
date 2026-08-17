CREATE TABLE task_status_history
(
    id BIGSERIAL PRIMARY KEY,

    task_id BIGINT NOT NULL,
    previous_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,

    changed_by_user_id BIGINT NOT NULL,
    changed_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_task_status_history_task
        FOREIGN KEY (task_id)
            REFERENCES tasks (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_task_status_history_user
        FOREIGN KEY (changed_by_user_id)
            REFERENCES users (id),

    CONSTRAINT chk_task_status_history_previous_status
        CHECK (
            previous_status IN (
                                'TODO',
                                'IN_PROGRESS',
                                'COMPLETED',
                                'CANCELLED'
                )
            ),

    CONSTRAINT chk_task_status_history_new_status
        CHECK (
            new_status IN (
                           'TODO',
                           'IN_PROGRESS',
                           'COMPLETED',
                           'CANCELLED'
                )
            ),

    CONSTRAINT chk_task_status_history_status_changed
        CHECK (previous_status <> new_status)
);

CREATE INDEX idx_task_status_history_task_changed_at
    ON task_status_history (
                            task_id,
                            changed_at DESC
        );

CREATE INDEX idx_task_status_history_changed_by_user
    ON task_status_history (changed_by_user_id);