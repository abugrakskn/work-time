CREATE TABLE time_entries (
                              id BIGSERIAL PRIMARY KEY,

                              user_id BIGINT NOT NULL,
                              task_id BIGINT NOT NULL,

                              start_time TIMESTAMP NOT NULL,
                              end_time TIMESTAMP,
                              duration_minutes INTEGER,
                              description VARCHAR(1000),

                              CONSTRAINT fk_time_entries_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users(id),

                              CONSTRAINT fk_time_entries_task
                                  FOREIGN KEY (task_id)
                                      REFERENCES tasks(id),

                              CONSTRAINT chk_time_entries_end_time
                                  CHECK (
                                      end_time IS NULL
                                          OR end_time >= start_time
                                      ),

                              CONSTRAINT chk_time_entries_duration
                                  CHECK (
                                      duration_minutes IS NULL
                                          OR duration_minutes >= 0
                                      )
);

CREATE UNIQUE INDEX uq_time_entries_active_user
    ON time_entries (user_id)
    WHERE end_time IS NULL;

CREATE INDEX idx_time_entries_user
    ON time_entries (user_id);

CREATE INDEX idx_time_entries_task
    ON time_entries (task_id);

CREATE INDEX idx_time_entries_start_time
    ON time_entries (start_time);