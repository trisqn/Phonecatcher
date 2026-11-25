ALTER TABLE warning_configuration
    ADD COLUMN expiration_days INTEGER NOT NULL DEFAULT 60,
    ADD CONSTRAINT ck_warning_configuration_expiration_days CHECK (expiration_days >= 1);
