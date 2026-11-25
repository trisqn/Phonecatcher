CREATE TABLE warning_configuration (
    id INTEGER PRIMARY KEY,
    report_threshold INTEGER NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_warning_configuration_singleton CHECK (id = 1),
    CONSTRAINT ck_warning_configuration_threshold CHECK (report_threshold >= 2)
);

INSERT INTO warning_configuration (id, report_threshold)
VALUES (1, 2);

ALTER TABLE warning
    DROP CONSTRAINT ck_warning_status,
    ADD CONSTRAINT ck_warning_status
        CHECK (status IN ('ACTIVE', 'EXPIRED', 'WITHDRAWN', 'THRESHOLD_NOT_MET')),
    DROP CONSTRAINT ck_warning_deactivation_time,
    ADD CONSTRAINT ck_warning_deactivation_time CHECK (
        (status = 'ACTIVE' AND deactivated_at IS NULL)
        OR (status IN ('EXPIRED', 'WITHDRAWN', 'THRESHOLD_NOT_MET') AND deactivated_at IS NOT NULL)
    );
