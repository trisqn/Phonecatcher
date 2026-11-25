ALTER TABLE dispute
    ADD CONSTRAINT ck_dispute_review_comment CHECK (
        (status = 'SUBMITTED' AND review_comment IS NULL)
        OR (status IN ('ACCEPTED', 'REJECTED') AND NULLIF(BTRIM(review_comment), '') IS NOT NULL)
    );
