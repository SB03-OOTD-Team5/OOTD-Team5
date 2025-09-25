-- Notification 데이터 삽입
INSERT INTO tbl_notification (id, receiver_id, title, content, level, created_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '알림1', '내용1', 'INFO', '2024-01-01T08:00:00Z'),
    ('22222222-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '알림2', '내용2', 'INFO', '2024-01-01T08:00:00Z'),
    ('33333333-3333-3333-3333-333333333333', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '알림3', '내용3', 'INFO', '2024-01-01T09:00:00Z'),
    ('44444444-4444-4444-4444-444444444444', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '알림4', '내용4', 'INFO', '2024-01-01T10:00:00Z');
