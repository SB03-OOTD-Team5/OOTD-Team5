/* 기본 속성 정의 추가 */
-- 속성 이름
INSERT INTO tbl_clothes_attributes (id, name, created_at)
VALUES ('11111111-aaaa-bbbb-cccc-000000000001', '계절', NOW()),
       ('11111111-aaaa-bbbb-cccc-000000000002', '성별', NOW()),
       ('11111111-aaaa-bbbb-cccc-000000000003', '색상', NOW()),
       ('11111111-aaaa-bbbb-cccc-000000000004', '소재', NOW()),
       ('11111111-aaaa-bbbb-cccc-000000000005', '스타일', NOW());

-- 계절 att_def
INSERT INTO tbl_clothes_attributes_defs (id, attribute_id, att_def, created_at)
VALUES (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000001', '봄', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000001', '여름', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000001', '가을', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000001', '겨울', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000001', '봄/가을', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000001', '사계절', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000001', '기타', NOW());

-- 성별 att_def
INSERT INTO tbl_clothes_attributes_defs (id, attribute_id, att_def, created_at)
VALUES (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000002', '남성', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000002', '여성', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000002', '기타', NOW());

-- 색상 att_def
INSERT INTO tbl_clothes_attributes_defs (id, attribute_id, att_def, created_at)
VALUES (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '블랙', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '그레이', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '화이트', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '브라운', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '블루', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '그린', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '핑크', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '레드', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '옐로우', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '베이지', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '퍼플', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '네이비', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '스카이블루', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '오렌지', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '민트', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '라벤더', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '카키', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '와인', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '실버', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '골드', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '기타', NOW());

-- 소재 att_def
INSERT INTO tbl_clothes_attributes_defs (id, attribute_id, att_def, created_at)
VALUES (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000004', '면', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000004', '린넨', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000004', '울', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000004', '폴리', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000004', '나일론', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000004', '레이온', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000004', '기모', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000004', '니트', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000004', '가죽', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000004', '데님', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000004', '기타', NOW());

-- 스타일 att_def
INSERT INTO tbl_clothes_attributes_defs (id, attribute_id, att_def, created_at)
VALUES (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000005', '캐주얼', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000005', '포멀', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000005', '스트릿', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000005', '스포티', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000005', '클래식', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000005', '빈티지', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000005', '기타', NOW());


/* 더미데이터 추가, 실제 ownerId로 변경 필요 */
-- 의상 더미데이터
-- delete from tbl_clothes where owner_id = '52cd7032-6bcb-4544-806a-72c73de25399';
INSERT INTO tbl_clothes (id, owner_id, name, type, image_url, created_at)
VALUES
-- 상의 (7)
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '화이트 티셔츠', 'TOP', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '블랙 반팔티', 'TOP', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '네이비 셔츠', 'TOP', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '핑크 블라우스', 'TOP', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '그레이 맨투맨', 'TOP', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '브라운 니트', 'TOP', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '민트 셔츠', 'TOP', null, NOW()),

-- 하의 (6)
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '블루 데님 팬츠', 'BOTTOM', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '블랙 슬랙스', 'BOTTOM', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '그레이 조거팬츠', 'BOTTOM', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '베이지 치노팬츠', 'BOTTOM', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '브라운 와이드팬츠', 'BOTTOM', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '블랙 미니스커트', 'BOTTOM', null, NOW()),

-- 원피스 (4)
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '블루 셔츠 원피스', 'DRESS', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '베이지 니트 원피스', 'DRESS', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '핑크 플로럴 원피스', 'DRESS', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '블랙 롱 원피스', 'DRESS', null, NOW()),

-- 아우터 (5)
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '베이지 트렌치코트', 'OUTER', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '블랙 가죽자켓', 'OUTER', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '그레이 후드집업', 'OUTER', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '브라운 코트', 'OUTER', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '네이비 패딩', 'OUTER', null, NOW()),

-- 신발 (4)
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '화이트 스니커즈', 'SHOES', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '블랙 로퍼', 'SHOES', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '브라운 부츠', 'SHOES', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '샌들', 'SHOES', null, NOW()),

-- 악세사리 (4)
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '베이지 볼캡', 'HAT', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '실버 목걸이', 'ACCESSORY', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '블랙 크로스백', 'BAG', null, NOW()),
(gen_random_uuid(), '52cd7032-6bcb-4544-806a-72c73de25399', '카키 머플러', 'SCARF', null, NOW());

-- 의상 속성값 연결
-- drop table if exists tbl_clothes_attributes_values;
-- ===========================================
-- CLOTHES ATTRIBUTES VALUES (매핑 더미)
-- ===========================================

-- 상의 ----------------------------------------
INSERT INTO tbl_clothes_attributes_values (id, clothes_id, attribute_id, def_value, created_at)
SELECT gen_random_uuid(), id, '11111111-aaaa-bbbb-cccc-000000000001', '여름', NOW()
FROM tbl_clothes
WHERE name = '화이트 티셔츠';
INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '화이트 티셔츠'),
        '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '화이트 티셔츠'),
        '11111111-aaaa-bbbb-cccc-000000000003', '화이트', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '화이트 티셔츠'),
        '11111111-aaaa-bbbb-cccc-000000000004', '면', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '화이트 티셔츠'),
        '11111111-aaaa-bbbb-cccc-000000000005', '캐주얼', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 반팔티'),
        '11111111-aaaa-bbbb-cccc-000000000001', '여름', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 반팔티'),
        '11111111-aaaa-bbbb-cccc-000000000002', '남성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 반팔티'),
        '11111111-aaaa-bbbb-cccc-000000000003', '블랙', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 반팔티'),
        '11111111-aaaa-bbbb-cccc-000000000004', '면', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 반팔티'),
        '11111111-aaaa-bbbb-cccc-000000000005', '스트릿', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '네이비 셔츠'),
        '11111111-aaaa-bbbb-cccc-000000000001', '봄/가을', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '네이비 셔츠'),
        '11111111-aaaa-bbbb-cccc-000000000002', '남성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '네이비 셔츠'),
        '11111111-aaaa-bbbb-cccc-000000000003', '네이비', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '네이비 셔츠'),
        '11111111-aaaa-bbbb-cccc-000000000004', '면', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '네이비 셔츠'),
        '11111111-aaaa-bbbb-cccc-000000000005', '포멀', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '핑크 블라우스'),
        '11111111-aaaa-bbbb-cccc-000000000001', '봄', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '핑크 블라우스'),
        '11111111-aaaa-bbbb-cccc-000000000002', '여성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '핑크 블라우스'),
        '11111111-aaaa-bbbb-cccc-000000000003', '핑크', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '핑크 블라우스'),
        '11111111-aaaa-bbbb-cccc-000000000004', '레이온', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '핑크 블라우스'),
        '11111111-aaaa-bbbb-cccc-000000000005', '포멀', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 맨투맨'),
        '11111111-aaaa-bbbb-cccc-000000000001', '가을', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 맨투맨'),
        '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 맨투맨'),
        '11111111-aaaa-bbbb-cccc-000000000003', '그레이', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 맨투맨'),
        '11111111-aaaa-bbbb-cccc-000000000004', '기모', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 맨투맨'),
        '11111111-aaaa-bbbb-cccc-000000000005', '캐주얼', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 니트'),
        '11111111-aaaa-bbbb-cccc-000000000001', '겨울', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 니트'),
        '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 니트'),
        '11111111-aaaa-bbbb-cccc-000000000003', '브라운', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 니트'),
        '11111111-aaaa-bbbb-cccc-000000000004', '니트', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 니트'),
        '11111111-aaaa-bbbb-cccc-000000000005', '클래식', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '민트 셔츠'),
        '11111111-aaaa-bbbb-cccc-000000000001', '봄', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '민트 셔츠'),
        '11111111-aaaa-bbbb-cccc-000000000002', '남성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '민트 셔츠'),
        '11111111-aaaa-bbbb-cccc-000000000003', '민트', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '민트 셔츠'),
        '11111111-aaaa-bbbb-cccc-000000000004', '면', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '민트 셔츠'),
        '11111111-aaaa-bbbb-cccc-000000000005', '캐주얼', NOW());

-- 하의 ----------------------------------------
INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블루 데님 팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000001', '사계절', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블루 데님 팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블루 데님 팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000003', '블루', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블루 데님 팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000004', '데님', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블루 데님 팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000005', '캐주얼', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 슬랙스'),
        '11111111-aaaa-bbbb-cccc-000000000001', '봄/가을', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 슬랙스'),
        '11111111-aaaa-bbbb-cccc-000000000002', '남성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 슬랙스'),
        '11111111-aaaa-bbbb-cccc-000000000003', '블랙', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 슬랙스'),
        '11111111-aaaa-bbbb-cccc-000000000004', '폴리', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 슬랙스'),
        '11111111-aaaa-bbbb-cccc-000000000005', '포멀', NOW());

-- ===========================================
-- 하의 (BOTTOM)
-- ===========================================
INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 조거팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000001', '가을', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 조거팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 조거팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000003', '그레이', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 조거팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000004', '면', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 조거팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000005', '스포티', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 치노팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000001', '봄/가을', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 치노팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000002', '남성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 치노팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000003', '베이지', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 치노팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000004', '면', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 치노팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000005', '캐주얼', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 와이드팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000001', '가을', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 와이드팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000002', '여성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 와이드팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000003', '브라운', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 와이드팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000004', '폴리', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 와이드팬츠'),
        '11111111-aaaa-bbbb-cccc-000000000005', '빈티지', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 미니스커트'),
        '11111111-aaaa-bbbb-cccc-000000000001', '여름', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 미니스커트'),
        '11111111-aaaa-bbbb-cccc-000000000002', '여성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 미니스커트'),
        '11111111-aaaa-bbbb-cccc-000000000003', '블랙', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 미니스커트'),
        '11111111-aaaa-bbbb-cccc-000000000004', '폴리', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 미니스커트'),
        '11111111-aaaa-bbbb-cccc-000000000005', '캐주얼', NOW());

-- ===========================================
-- 원피스 (DRESS)
-- ===========================================
INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블루 셔츠 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000001', '봄', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블루 셔츠 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000002', '여성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블루 셔츠 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000003', '블루', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블루 셔츠 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000004', '면', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블루 셔츠 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000005', '포멀', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 니트 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000001', '가을', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 니트 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000002', '여성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 니트 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000003', '베이지', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 니트 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000004', '니트', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 니트 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000005', '클래식', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '핑크 플로럴 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000001', '여름', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '핑크 플로럴 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000002', '여성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '핑크 플로럴 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000003', '핑크', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '핑크 플로럴 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000004', '레이온', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '핑크 플로럴 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000005', '빈티지', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 롱 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000001', '겨울', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 롱 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000002', '여성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 롱 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000003', '블랙', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 롱 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000004', '울', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 롱 원피스'),
        '11111111-aaaa-bbbb-cccc-000000000005', '클래식', NOW());

-- ===========================================
-- 아우터 (OUTER)
-- ===========================================
INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 트렌치코트'),
        '11111111-aaaa-bbbb-cccc-000000000001', '봄/가을', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 트렌치코트'),
        '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 트렌치코트'),
        '11111111-aaaa-bbbb-cccc-000000000003', '베이지', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 트렌치코트'),
        '11111111-aaaa-bbbb-cccc-000000000004', '나일론', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 트렌치코트'),
        '11111111-aaaa-bbbb-cccc-000000000005', '클래식', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 가죽자켓'),
        '11111111-aaaa-bbbb-cccc-000000000001', '가을', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 가죽자켓'),
        '11111111-aaaa-bbbb-cccc-000000000002', '남성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 가죽자켓'),
        '11111111-aaaa-bbbb-cccc-000000000003', '블랙', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 가죽자켓'),
        '11111111-aaaa-bbbb-cccc-000000000004', '가죽', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 가죽자켓'),
        '11111111-aaaa-bbbb-cccc-000000000005', '스트릿', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 후드집업'),
        '11111111-aaaa-bbbb-cccc-000000000001', '봄/가을', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 후드집업'),
        '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 후드집업'),
        '11111111-aaaa-bbbb-cccc-000000000003', '그레이', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 후드집업'),
        '11111111-aaaa-bbbb-cccc-000000000004', '면', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '그레이 후드집업'),
        '11111111-aaaa-bbbb-cccc-000000000005', '캐주얼', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 코트'),
        '11111111-aaaa-bbbb-cccc-000000000001', '겨울', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 코트'),
        '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 코트'),
        '11111111-aaaa-bbbb-cccc-000000000003', '브라운', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 코트'),
        '11111111-aaaa-bbbb-cccc-000000000004', '울', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 코트'),
        '11111111-aaaa-bbbb-cccc-000000000005', '클래식', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '네이비 패딩'),
        '11111111-aaaa-bbbb-cccc-000000000001', '겨울', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '네이비 패딩'),
        '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '네이비 패딩'),
        '11111111-aaaa-bbbb-cccc-000000000003', '네이비', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '네이비 패딩'),
        '11111111-aaaa-bbbb-cccc-000000000004', '폴리', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '네이비 패딩'),
        '11111111-aaaa-bbbb-cccc-000000000005', '스포티', NOW());

-- ===========================================
-- 신발 (SHOES)
-- ===========================================
INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '화이트 스니커즈'),
        '11111111-aaaa-bbbb-cccc-000000000001', '사계절', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '화이트 스니커즈'),
        '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '화이트 스니커즈'),
        '11111111-aaaa-bbbb-cccc-000000000003', '화이트', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '화이트 스니커즈'),
        '11111111-aaaa-bbbb-cccc-000000000004', '폴리', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '화이트 스니커즈'),
        '11111111-aaaa-bbbb-cccc-000000000005', '캐주얼', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 로퍼'),
        '11111111-aaaa-bbbb-cccc-000000000001', '가을', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 로퍼'),
        '11111111-aaaa-bbbb-cccc-000000000002', '남성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 로퍼'),
        '11111111-aaaa-bbbb-cccc-000000000003', '블랙', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 로퍼'),
        '11111111-aaaa-bbbb-cccc-000000000004', '가죽', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 로퍼'),
        '11111111-aaaa-bbbb-cccc-000000000005', '포멀', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 부츠'),
        '11111111-aaaa-bbbb-cccc-000000000001', '겨울', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 부츠'),
        '11111111-aaaa-bbbb-cccc-000000000002', '여성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 부츠'),
        '11111111-aaaa-bbbb-cccc-000000000003', '브라운', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 부츠'),
        '11111111-aaaa-bbbb-cccc-000000000004', '가죽', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '브라운 부츠'),
        '11111111-aaaa-bbbb-cccc-000000000005', '클래식', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '샌들'),
        '11111111-aaaa-bbbb-cccc-000000000001', '여름', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '샌들'),
        '11111111-aaaa-bbbb-cccc-000000000002', '여성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '샌들'),
        '11111111-aaaa-bbbb-cccc-000000000003', '베이지', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '샌들'),
        '11111111-aaaa-bbbb-cccc-000000000004', '폴리', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '샌들'),
        '11111111-aaaa-bbbb-cccc-000000000005', '캐주얼', NOW());

-- ===========================================
-- 악세사리 (HAT, ACCESSORY, BAG, SCARF)
-- ===========================================
INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 볼캡'),
        '11111111-aaaa-bbbb-cccc-000000000001', '사계절', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 볼캡'),
        '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 볼캡'),
        '11111111-aaaa-bbbb-cccc-000000000003', '베이지', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 볼캡'),
        '11111111-aaaa-bbbb-cccc-000000000004', '면', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '베이지 볼캡'),
        '11111111-aaaa-bbbb-cccc-000000000005', '캐주얼', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '실버 목걸이'),
        '11111111-aaaa-bbbb-cccc-000000000001', '사계절', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '실버 목걸이'),
        '11111111-aaaa-bbbb-cccc-000000000002', '여성', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '실버 목걸이'),
        '11111111-aaaa-bbbb-cccc-000000000003', '실버', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '실버 목걸이'),
        '11111111-aaaa-bbbb-cccc-000000000004', '기타', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '실버 목걸이'),
        '11111111-aaaa-bbbb-cccc-000000000005', '포멀', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 크로스백'),
        '11111111-aaaa-bbbb-cccc-000000000001', '사계절', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 크로스백'),
        '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 크로스백'),
        '11111111-aaaa-bbbb-cccc-000000000003', '블랙', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 크로스백'),
        '11111111-aaaa-bbbb-cccc-000000000004', '가죽', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '블랙 크로스백'),
        '11111111-aaaa-bbbb-cccc-000000000005', '캐주얼', NOW());

INSERT INTO tbl_clothes_attributes_values
VALUES (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '카키 머플러'),
        '11111111-aaaa-bbbb-cccc-000000000001', '겨울', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '카키 머플러'),
        '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '카키 머플러'),
        '11111111-aaaa-bbbb-cccc-000000000003', '카키', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '카키 머플러'),
        '11111111-aaaa-bbbb-cccc-000000000004', '울', NOW()),
       (gen_random_uuid(), (SELECT id FROM tbl_clothes WHERE name = '카키 머플러'),
        '11111111-aaaa-bbbb-cccc-000000000005', '클래식', NOW());