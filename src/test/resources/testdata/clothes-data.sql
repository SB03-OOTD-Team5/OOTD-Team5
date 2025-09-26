-- Clothes 데이터 삽입
INSERT INTO tbl_clothes (id, owner_id, name, type, image_url, created_at, updated_at)
VALUES ('aaaaaaaa-0000-0000-0000-aaaaaaaaaaaa', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '흰 티셔츠', 'TOP', null, '2024-01-01 10:00:00+00', NOW()),

       ('bbbbbbbb-0000-0000-0000-aaaaaaaaaaaa', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '청바지', 'BOTTOM', null, '2024-01-01 09:00:00+00', NOW()),

       ('11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '운동화1', 'SHOES', null, '2024-01-01 08:00:00+00', NOW()),

       ('22222222-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '운동화2', 'SHOES', null, '2024-01-01 08:00:00+00', NOW()),

       ('33333333-3333-3333-3333-333333333333', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '운동화3', 'SHOES', null, '2024-01-01 08:00:00+00', NOW());