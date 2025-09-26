-- User 삽입
INSERT INTO tbl_users
(id, name, email, password, role, is_locked, created_at, updated_at, temp_password,
 temp_password_expired_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '쪼쪼',
        'zzo@email.com',
        'zzo1234!',
        'USER',
        FALSE,
        '2024-01-01 00:00:00+00',
        NOW(),
        NULL,
        NULL);
