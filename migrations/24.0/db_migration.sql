BEGIN;

ALTER TABLE console.user_token
    ADD additional_info character varying;

COMMIT;
