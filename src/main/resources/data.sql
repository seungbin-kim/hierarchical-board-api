INSERT INTO authority
VALUES ('ROLE_USER');
INSERT INTO authority
VALUES ('ROLE_ADMIN');

INSERT INTO users(user_id, email, nickname, name, birthday, password, created_at, modified_at)
VALUES (0, 'kim@a.b', '김아무개', '김', '1997-05-28', '$2a$10$6V.DbpQvKh79DqunMfPbkOdmd0xpjxIQwlgqslLdKA1.lmh7jWKSO',
        current_timestamp, current_timestamp);

INSERT INTO user_authority(authority_name, user_id) VALUES ('ROLE_USER', 0);
INSERT INTO user_authority(authority_name, user_id) VALUES ('ROLE_ADMIN', 0);