INSERT INTO fraud_category (code, name, warning_title)
VALUES
    ('POLICE', 'Politseitöötaja', 'Politseinikuna esinev petis'),
    ('POSTAL_WORKER', 'Postitöötaja', 'Postitöötajana esinev petis'),
    ('BANK_EMPLOYEE', 'Pangatöötaja', 'Pangatöötajana esinev petis');

ALTER TABLE phone_number
    ADD CONSTRAINT ck_phone_number_e164
    CHECK (normalized_number ~ '^\+[1-9][0-9]{1,14}$');
