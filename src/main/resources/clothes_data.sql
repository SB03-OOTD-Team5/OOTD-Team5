/*
 의상 더미 데이터
 ownerId값 변경하고 insert
 */
-- delete from tbl_clothes where owner_id = '52cd7032-6bcb-4544-806a-72c73de25399';
SET app.owner_id = '52cd7032-6bcb-4544-806a-72c73de25399';

/* 기본 속성 정의 추가 */
-- 속성 이름
INSERT INTO tbl_clothes_attributes (id, name, created_at)
VALUES ('11111111-aaaa-bbbb-cccc-000000000001', '계절', NOW()),
       ('11111111-aaaa-bbbb-cccc-000000000002', '성별', NOW()),
       ('11111111-aaaa-bbbb-cccc-000000000003', '색상', NOW()),
       ('11111111-aaaa-bbbb-cccc-000000000004', '소재', NOW()),
       ('11111111-aaaa-bbbb-cccc-000000000005', '스타일', NOW())
ON CONFLICT (id) DO NOTHING;

-- 계절 att_def
INSERT INTO tbl_clothes_attributes_defs (id, attribute_id, att_def, created_at)
VALUES (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000001', '봄', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000001', '여름', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000001', '가을', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000001', '겨울', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000001', '봄/가을', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000001', '사계절', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000001', '기타', NOW())
ON CONFLICT (attribute_id, att_def) DO NOTHING;

-- 성별 att_def
INSERT INTO tbl_clothes_attributes_defs (id, attribute_id, att_def, created_at)
VALUES (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000002', '남성', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000002', '여성', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000002', '공용', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000002', '기타', NOW())
ON CONFLICT (attribute_id, att_def) DO NOTHING;

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
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000003', '기타', NOW())
ON CONFLICT (attribute_id, att_def) DO NOTHING;

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
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000004', '기타', NOW())
ON CONFLICT (attribute_id, att_def) DO NOTHING;

-- 스타일 att_def
INSERT INTO tbl_clothes_attributes_defs (id, attribute_id, att_def, created_at)
VALUES (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000005', '캐주얼', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000005', '포멀', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000005', '스트릿', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000005', '스포티', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000005', '클래식', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000005', '빈티지', NOW()),
       (gen_random_uuid(), '11111111-aaaa-bbbb-cccc-000000000005', '기타', NOW())
ON CONFLICT (attribute_id, att_def) DO NOTHING;

/* 더미데이터 추가, 실제 ownerId로 변경 필요 */
-- 의상 더미데이터
WITH owner AS (SELECT current_setting('app.owner_id')::uuid AS owner_id),
     new_items(name, type) AS (
    VALUES
        -- TOP
        ('화이트 티셔츠', 'TOP'),
        ('블랙 반팔티', 'TOP'),
        ('데님 데일리 셔츠', 'TOP'),
        ('핑크 블라우스', 'TOP'),
        ('그레이 맨투맨', 'TOP'),
        ('브라운 니트', 'TOP'),
        ('피그먼트 페이퍼셔츠', 'TOP'),
        ('화이트 셔츠', 'TOP'),
        ('스트라이프 티셔츠', 'TOP'),
        ('오버핏 포켓 체크 셔츠', 'TOP'),
        ('옥스포드 오버핏 셔츠', 'TOP'),
        ('그레이 후드티', 'TOP'),
        ('브이넥 니트', 'TOP'),
        ('레드 자수 맨투맨', 'TOP'),

        -- BOTTOM
        ('블루 데님 팬츠', 'BOTTOM'),
        ('투턱 와이드 슬랙스', 'BOTTOM'),
        ('그레이 조거팬츠', 'BOTTOM'),
        ('베이지 치노 팬츠', 'BOTTOM'),
        ('커브드 팬츠_코듀로이 브라운', 'BOTTOM'),
        ('블랙 미니스커트', 'BOTTOM'),
        ('아이보리 와이드팬츠', 'BOTTOM'),
        ('카고 스트랩 팬츠', 'BOTTOM'),
        ('카키 카고팬츠', 'BOTTOM'),
        ('원 턱 테이퍼드 슬랙스', 'BOTTOM'),
        ('블랙 조거팬츠', 'BOTTOM'),
        ('스트레이트 슬랙스', 'BOTTOM'),

        -- DRESS
        ('네이비 체크 원피스', 'DRESS'),
        ('베이지 니트 원피스', 'DRESS'),
        ('플레어 퍼프 원피스', 'DRESS'),
        ('블랙 롱 원피스', 'DRESS'),
        ('네이비 랩 원피스', 'DRESS'),
        ('베이지 셔츠 원피스', 'DRESS'),

        -- OUTER
        ('베이지 트렌치코트', 'OUTER'),
        ('블랙 가죽자켓', 'OUTER'),
        ('그레이 후드집업', 'OUTER'),
        ('브라운 숏 코트', 'OUTER'),
        ('네이비 패딩', 'OUTER'),
        ('블루 데님자켓', 'OUTER'),
        ('아이보리 롱 코트', 'OUTER'),
        ('카키 야상', 'OUTER'),
        ('아이보리 가디건', 'OUTER'),
        ('그레이 라운드 가디건', 'OUTER'),

        -- SHOES
        ('화이트 스니커즈', 'SHOES'),
        ('블랙 로퍼', 'SHOES'),
        ('브라운 부츠', 'SHOES'),
        ('샌들', 'SHOES'),
        ('브라운 스니커즈', 'SHOES'),
        ('베이지 로퍼', 'SHOES'),
        ('그레이 첼시부츠', 'SHOES'),
        ('화이트 슬리퍼', 'SHOES'),

        -- ACCESSORY
        ('실버 목걸이', 'ACCESSORY'),
        ('골드 귀걸이', 'ACCESSORY'),
        ('브라운 벨트', 'ACCESSORY'),
        ('실버 반지', 'ACCESSORY'),
        ('골드 반지', 'ACCESSORY'),
        ('실버 팔찌', 'ACCESSORY'),
        ('레더 시계', 'ACCESSORY'),
        ('캔버스 벨트', 'ACCESSORY'),
        ('골드 목걸이', 'ACCESSORY'),
        ('헤어밴드', 'ACCESSORY'),

        -- HAT
        ('블랙 버킷햇', 'HAT'),
        ('브라운 페도라', 'HAT'),
        ('카키 베레모', 'HAT'),
        ('베이지 볼캡', 'HAT'),
        ('블랙 비니', 'HAT'),

        -- BAG
        ('미니 백팩', 'BAG'),
        ('캔버스 토트백', 'BAG'),
        ('블랙 메신저백', 'BAG'),
        ('블랙 크로스백', 'BAG'),
        ('베이지 토트백', 'BAG'),

        -- SCARF
        ('실크 스카프', 'SCARF'),
        ('체크 머플러', 'SCARF'),
        ('카키 머플러', 'SCARF'),

        -- ETC
        ('우산', 'ETC'),
        ('양산', 'ETC'),
        ('휴대용선풍기', 'ETC'),
        ('장갑', 'ETC'),
        ('손수건', 'ETC'),

        -- SOCKS
        ('기본 화이트 양말', 'SOCKS'),
        ('기본 블랙 양말', 'SOCKS'),
        ('그레이 크루삭스', 'SOCKS')
)
INSERT INTO tbl_clothes (id, owner_id, name, type, image_url, created_at)
SELECT gen_random_uuid(), owner.owner_id, ni.name, ni.type, NULL, NOW()
FROM new_items ni
         CROSS JOIN owner
WHERE NOT EXISTS (
    SELECT 1 FROM tbl_clothes c
    WHERE c.owner_id = owner.owner_id
      AND c.name = ni.name
);

-- 의상 속성값 연결
-- drop table if exists tbl_clothes_attributes_values;
-- ===========================================
-- CLOTHES ATTRIBUTES VALUES (매핑 더미)
-- ===========================================

-- 상의 ----------------------------------------
WITH owner AS (SELECT current_setting('app.owner_id')::uuid AS owner_id),
     mapping(name, attribute_id, def_value) AS (
    VALUES
        ('화이트 티셔츠', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '사계절'),
        ('화이트 티셔츠', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('화이트 티셔츠', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '화이트'),
        ('화이트 티셔츠', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '면'),
        ('화이트 티셔츠', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '캐주얼'),

        ('블랙 반팔티', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '여름'),
        ('블랙 반팔티', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '남성'),
        ('블랙 반팔티', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '블랙'),
        ('블랙 반팔티', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '면'),
        ('블랙 반팔티', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '스트릿'),

        ('데님 데일리 셔츠', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '봄/가을'),
        ('데님 데일리 셔츠', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '남성'),
        ('데님 데일리 셔츠', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '블랙'),
        ('데님 데일리 셔츠', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '데님'),
        ('데님 데일리 셔츠', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '캐주얼'),

        ('핑크 블라우스', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '봄'),
        ('핑크 블라우스', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '여성'),
        ('핑크 블라우스', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '핑크'),
        ('핑크 블라우스', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '레이온'),
        ('핑크 블라우스', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '포멀'),

        ('그레이 맨투맨', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '가을'),
        ('그레이 맨투맨', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('그레이 맨투맨', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '그레이'),
        ('그레이 맨투맨', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '기모'),
        ('그레이 맨투맨', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '캐주얼'),

        ('브라운 니트', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '겨울'),
        ('브라운 니트', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('브라운 니트', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '브라운'),
        ('브라운 니트', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '니트'),
        ('브라운 니트', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '클래식'),

        ('피그먼트 페이퍼셔츠', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '봄'),
        ('피그먼트 페이퍼셔츠', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '남성'),
        ('피그먼트 페이퍼셔츠', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '그린'),
        ('피그먼트 페이퍼셔츠', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '면'),
        ('피그먼트 페이퍼셔츠', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '캐주얼'),

        ('화이트 셔츠','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'봄/가을'),
        ('화이트 셔츠','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('화이트 셔츠','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'화이트'),
        ('화이트 셔츠','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'면'),
        ('화이트 셔츠','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'포멀'),

        ('스트라이프 티셔츠','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'여름'),
        ('스트라이프 티셔츠','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('스트라이프 티셔츠','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'네이비'),
        ('스트라이프 티셔츠','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'면'),
        ('스트라이프 티셔츠','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'캐주얼'),

        ('오버핏 포켓 체크 셔츠','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'봄/가을'),
        ('오버핏 포켓 체크 셔츠','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'남성'),
        ('오버핏 포켓 체크 셔츠','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'브라운'),
        ('오버핏 포켓 체크 셔츠','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'면'),
        ('오버핏 포켓 체크 셔츠','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'빈티지'),

        ('옥스포드 오버핏 셔츠','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'봄/가을'),
        ('옥스포드 오버핏 셔츠','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('옥스포드 오버핏 셔츠','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'스카이블루'),
        ('옥스포드 오버핏 셔츠','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'린넨'),
        ('옥스포드 오버핏 셔츠','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'캐주얼'),

        ('그레이 후드티','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'가을'),
        ('그레이 후드티','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('그레이 후드티','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'그레이'),
        ('그레이 후드티','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'면'),
        ('그레이 후드티','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'스트릿'),

        ('브이넥 니트','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'겨울'),
        ('브이넥 니트','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('브이넥 니트','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'베이지'),
        ('브이넥 니트','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'니트'),
        ('브이넥 니트','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'클래식'),

        ('레드 자수 맨투맨','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'봄/가을'),
        ('레드 자수 맨투맨','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('레드 자수 맨투맨','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'레드'),
        ('레드 자수 맨투맨','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'면'),
        ('레드 자수 맨투맨','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'캐주얼')
)
INSERT INTO tbl_clothes_attributes_values (id, clothes_id, attribute_id, def_value, created_at)
SELECT gen_random_uuid(), c.id, m.attribute_id, m.def_value, NOW()
FROM mapping m
         JOIN tbl_clothes c
              ON c.name = m.name
                  AND c.owner_id = (SELECT owner_id FROM owner)
ON CONFLICT (clothes_id, attribute_id) DO NOTHING;

-- 하의 ----------------------------------------
WITH owner AS (SELECT current_setting('app.owner_id')::uuid AS owner_id),
     mapping(name, attribute_id, def_value) AS (
    VALUES
        ('블루 데님 팬츠', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '사계절'),
        ('블루 데님 팬츠', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('블루 데님 팬츠', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '블루'),
        ('블루 데님 팬츠', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '데님'),
        ('블루 데님 팬츠', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '캐주얼'),

        ('투턱 와이드 슬랙스', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '봄/가을'),
        ('투턱 와이드 슬랙스', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('투턱 와이드 슬랙스', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '그레이'),
        ('투턱 와이드 슬랙스', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '폴리'),
        ('투턱 와이드 슬랙스', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '포멀'),

        ('그레이 조거팬츠', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '가을'),
        ('그레이 조거팬츠', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('그레이 조거팬츠', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '그레이'),
        ('그레이 조거팬츠', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '면'),
        ('그레이 조거팬츠', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '스포티'),

        ('베이지 치노 팬츠', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '봄/가을'),
        ('베이지 치노 팬츠', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '여성'),
        ('베이지 치노 팬츠', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '베이지'),
        ('베이지 치노 팬츠', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '면'),
        ('베이지 치노 팬츠', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '캐주얼'),

        ('커브드 팬츠_코듀로이 브라운', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '가을'),
        ('커브드 팬츠_코듀로이 브라운', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('커브드 팬츠_코듀로이 브라운', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '브라운'),
        ('커브드 팬츠_코듀로이 브라운', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '면'),
        ('커브드 팬츠_코듀로이 브라운', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '캐주얼'),

        ('블랙 미니스커트', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '여름'),
        ('블랙 미니스커트', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '여성'),
        ('블랙 미니스커트', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '블랙'),
        ('블랙 미니스커트', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '폴리'),
        ('블랙 미니스커트', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '캐주얼'),

        ('아이보리 와이드팬츠', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '사계절'),
        ('아이보리 와이드팬츠', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('아이보리 와이드팬츠', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '베이지'),
        ('아이보리 와이드팬츠', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '면'),
        ('아이보리 와이드팬츠', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '클래식'),

        ('카고 스트랩 팬츠', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '봄/가을'),
        ('카고 스트랩 팬츠', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('카고 스트랩 팬츠', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '베이지'),
        ('카고 스트랩 팬츠', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '면'),
        ('카고 스트랩 팬츠', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '스트릿'),

        ('카키 카고팬츠', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '가을'),
        ('카키 카고팬츠', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('카키 카고팬츠', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '카키'),
        ('카키 카고팬츠', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '나일론'),
        ('카키 카고팬츠', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '스트릿'),

        ('원 턱 테이퍼드 슬랙스', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '기타'),
        ('원 턱 테이퍼드 슬랙스', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('원 턱 테이퍼드 슬랙스', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '그레이'),
        ('원 턱 테이퍼드 슬랙스', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '기모'),
        ('원 턱 테이퍼드 슬랙스', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '포멀'),

        ('블랙 조거팬츠', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '가을'),
        ('블랙 조거팬츠', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('블랙 조거팬츠', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '블랙'),
        ('블랙 조거팬츠', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '폴리'),
        ('블랙 조거팬츠', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '스포티'),

        ('스트레이트 슬랙스', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '여름'),
        ('스트레이트 슬랙스', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('스트레이트 슬랙스', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '블루'),
        ('스트레이트 슬랙스', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '데님'),
        ('스트레이트 슬랙스', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '캐주얼'))
INSERT INTO tbl_clothes_attributes_values (id, clothes_id, attribute_id, def_value, created_at)
SELECT gen_random_uuid(), c.id, m.attribute_id, m.def_value, NOW()
FROM mapping m
         JOIN tbl_clothes c
              ON c.name = m.name
                  AND c.owner_id = (SELECT owner_id FROM owner)
ON CONFLICT (clothes_id, attribute_id) DO NOTHING;

-- ===========================================
-- 원피스 (DRESS)
-- ===========================================
WITH owner AS (SELECT current_setting('app.owner_id')::uuid AS owner_id),
     mapping(name, attribute_id, def_value) AS (
    VALUES
        ('네이비 체크 원피스', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '봄'),
        ('네이비 체크 원피스', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '여성'),
        ('네이비 체크 원피스', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '블루'),
        ('네이비 체크 원피스', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '면'),
        ('네이비 체크 원피스', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '포멀'),

        ('베이지 니트 원피스', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '가을'),
        ('베이지 니트 원피스', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '여성'),
        ('베이지 니트 원피스', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '베이지'),
        ('베이지 니트 원피스', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '니트'),
        ('베이지 니트 원피스', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '클래식'),

        ('플레어 퍼프 원피스', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '여름'),
        ('플레어 퍼프 원피스', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '여성'),
        ('플레어 퍼프 원피스', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '핑크'),
        ('플레어 퍼프 원피스', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '레이온'),
        ('플레어 퍼프 원피스', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '빈티지'),

        ('블랙 롱 원피스', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '가을'),
        ('블랙 롱 원피스', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '여성'),
        ('블랙 롱 원피스', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '블랙'),
        ('블랙 롱 원피스', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '울'),
        ('블랙 롱 원피스', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '클래식'),

        ('네이비 랩 원피스','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'여름'),
        ('네이비 랩 원피스','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'여성'),
        ('네이비 랩 원피스','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'네이비'),
        ('네이비 랩 원피스','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'레이온'),
        ('네이비 랩 원피스','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'포멀'),

        ('베이지 셔츠 원피스','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'봄/가을'),
        ('베이지 셔츠 원피스','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'여성'),
        ('베이지 셔츠 원피스','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'베이지'),
        ('베이지 셔츠 원피스','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'레이온'),
        ('베이지 셔츠 원피스','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'클래식')
)
INSERT INTO tbl_clothes_attributes_values (id, clothes_id, attribute_id, def_value, created_at)
SELECT gen_random_uuid(), c.id, m.attribute_id, m.def_value, NOW()
FROM mapping m
         JOIN tbl_clothes c
              ON c.name = m.name
                  AND c.owner_id = (SELECT owner_id FROM owner)
ON CONFLICT (clothes_id, attribute_id) DO NOTHING;

-- ===========================================
-- 아우터 (OUTER)
-- ===========================================
WITH owner AS (SELECT current_setting('app.owner_id')::uuid AS owner_id),
     mapping(name, attribute_id, def_value) AS (
    VALUES
        ('베이지 트렌치코트', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '봄/가을'),
        ('베이지 트렌치코트', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('베이지 트렌치코트', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '베이지'),
        ('베이지 트렌치코트', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '나일론'),
        ('베이지 트렌치코트', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '클래식'),

        ('블랙 가죽자켓', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '가을'),
        ('블랙 가죽자켓', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '남성'),
        ('블랙 가죽자켓', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '블랙'),
        ('블랙 가죽자켓', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '가죽'),
        ('블랙 가죽자켓', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '스트릿'),

        ('그레이 후드집업', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '봄/가을'),
        ('그레이 후드집업', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('그레이 후드집업', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '그레이'),
        ('그레이 후드집업', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '면'),
        ('그레이 후드집업', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '캐주얼'),

        ('브라운 숏 코트', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '겨울'),
        ('브라운 숏 코트', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('브라운 숏 코트', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '브라운'),
        ('브라운 숏 코트', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '울'),
        ('브라운 숏 코트', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '클래식'),

        ('네이비 패딩', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '겨울'),
        ('네이비 패딩', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('네이비 패딩', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '네이비'),
        ('네이비 패딩', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '폴리'),
        ('네이비 패딩', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '스포티'),

        ('블루 데님자켓','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'봄/가을'),
        ('블루 데님자켓','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('블루 데님자켓','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'블루'),
        ('블루 데님자켓','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'데님'),
        ('블루 데님자켓','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'캐주얼'),

        ('아이보리 롱 코트','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'겨울'),
        ('아이보리 롱 코트','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('아이보리 롱 코트','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'화이트'),
        ('아이보리 롱 코트','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'울'),
        ('아이보리 롱 코트','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'클래식'),

        ('카키 야상','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'가을'),
        ('카키 야상','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'남성'),
        ('카키 야상','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'카키'),
        ('카키 야상','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'나일론'),
        ('카키 야상','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'스트릿'),

        ('아이보리 가디건','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'봄/가을'),
        ('아이보리 가디건','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('아이보리 가디건','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'베이지'),
        ('아이보리 가디건','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'니트'),
        ('아이보리 가디건','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'캐주얼'),

        ('그레이 라운드 가디건','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'봄/가을'),
        ('그레이 라운드 가디건','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('그레이 라운드 가디건','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'그레이'),
        ('그레이 라운드 가디건','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'니트'),
        ('그레이 라운드 가디건','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'캐주얼')

)
INSERT INTO tbl_clothes_attributes_values (id, clothes_id, attribute_id, def_value, created_at)
SELECT gen_random_uuid(), c.id, m.attribute_id, m.def_value, NOW()
FROM mapping m
         JOIN tbl_clothes c
              ON c.name = m.name
                  AND c.owner_id = (SELECT owner_id FROM owner)
ON CONFLICT (clothes_id, attribute_id) DO NOTHING;

-- ========================================
-- 신발 (SHOES)
-- ===========================================
WITH owner AS (SELECT current_setting('app.owner_id')::uuid AS owner_id),
     mapping(name, attribute_id, def_value) AS (
    VALUES
        ('화이트 스니커즈', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '사계절'),
        ('화이트 스니커즈', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('화이트 스니커즈', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '화이트'),
        ('화이트 스니커즈', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '폴리'),
        ('화이트 스니커즈', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '캐주얼'),

        ('블랙 로퍼', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '가을'),
        ('블랙 로퍼', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '남성'),
        ('블랙 로퍼', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '블랙'),
        ('블랙 로퍼', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '가죽'),
        ('블랙 로퍼', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '포멀'),

        ('브라운 부츠', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '겨울'),
        ('브라운 부츠', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '여성'),
        ('브라운 부츠', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '브라운'),
        ('브라운 부츠', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '가죽'),
        ('브라운 부츠', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '클래식'),

        ('샌들', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '여름'),
        ('샌들', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '여성'),
        ('샌들', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '베이지'),
        ('샌들', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '폴리'),
        ('샌들', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '캐주얼'),

        ('브라운 스니커즈','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'봄/가을'),
        ('브라운 스니커즈','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('브라운 스니커즈','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'브라운'),
        ('브라운 스니커즈','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'폴리'),
        ('브라운 스니커즈','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'스포티'),

        ('베이지 로퍼','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'봄/가을'),
        ('베이지 로퍼','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'남성'),
        ('베이지 로퍼','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'베이지'),
        ('베이지 로퍼','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'가죽'),
        ('베이지 로퍼','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'포멀'),

        ('그레이 첼시부츠','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'겨울'),
        ('그레이 첼시부츠','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('그레이 첼시부츠','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'그레이'),
        ('그레이 첼시부츠','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'가죽'),
        ('그레이 첼시부츠','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'클래식'),

        ('화이트 슬리퍼','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'여름'),
        ('화이트 슬리퍼','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('화이트 슬리퍼','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'화이트'),
        ('화이트 슬리퍼','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'폴리'),
        ('화이트 슬리퍼','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'캐주얼')
)
INSERT INTO tbl_clothes_attributes_values (id, clothes_id, attribute_id, def_value, created_at)
SELECT gen_random_uuid(), c.id, m.attribute_id, m.def_value, NOW()
FROM mapping m
         JOIN tbl_clothes c
              ON c.name = m.name
                  AND c.owner_id = (SELECT owner_id FROM owner)
ON CONFLICT (clothes_id, attribute_id) DO NOTHING;

-- ===========================================
-- 악세사리 (ACCESSORY)
-- ===========================================
WITH owner AS (SELECT current_setting('app.owner_id')::uuid AS owner_id),
     mapping(name, attribute_id, def_value) AS (
    VALUES
        ('골드 귀걸이','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('골드 귀걸이','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'여성'),
        ('골드 귀걸이','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'골드'),
        ('골드 귀걸이','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'기타'),
        ('골드 귀걸이','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'포멀'),

        ('실버 목걸이', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '사계절'),
        ('실버 목걸이', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '여성'),
        ('실버 목걸이', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '실버'),
        ('실버 목걸이', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '기타'),
        ('실버 목걸이', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '포멀'),

        ('브라운 벨트','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('브라운 벨트','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('브라운 벨트','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'브라운'),
        ('브라운 벨트','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'가죽'),
        ('브라운 벨트','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'클래식'),

        ('실버 반지','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('실버 반지','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('실버 반지','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'실버'),
        ('실버 반지','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'기타'),
        ('실버 반지','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'포멀'),

        ('골드 반지','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('골드 반지','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'여성'),
        ('골드 반지','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'골드'),
        ('골드 반지','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'기타'),
        ('골드 반지','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'포멀'),

        ('실버 팔찌','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('실버 팔찌','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('실버 팔찌','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'실버'),
        ('실버 팔찌','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'기타'),
        ('실버 팔찌','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'캐주얼'),

        ('레더 시계','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('레더 시계','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('레더 시계','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'브라운'),
        ('레더 시계','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'가죽'),
        ('레더 시계','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'클래식'),

        ('캔버스 벨트','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('캔버스 벨트','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('캔버스 벨트','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'베이지'),
        ('캔버스 벨트','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'면'),
        ('캔버스 벨트','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'캐주얼'),

        ('골드 목걸이','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('골드 목걸이','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'여성'),
        ('골드 목걸이','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'골드'),
        ('골드 목걸이','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'기타'),
        ('골드 목걸이','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'포멀'),

        ('헤어밴드','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'여름'),
        ('헤어밴드','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'여성'),
        ('헤어밴드','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'베이지'),
        ('헤어밴드','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'기타'),
        ('헤어밴드','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'캐주얼')
)
INSERT INTO tbl_clothes_attributes_values (id, clothes_id, attribute_id, def_value, created_at)
SELECT gen_random_uuid(), c.id, m.attribute_id, m.def_value, NOW()
FROM mapping m
         JOIN tbl_clothes c
              ON c.name = m.name
                  AND c.owner_id = (SELECT owner_id FROM owner)
ON CONFLICT (clothes_id, attribute_id) DO NOTHING;

-- ===========================================
-- 모자 (HAT)
-- ===========================================
WITH owner AS (SELECT current_setting('app.owner_id')::uuid AS owner_id),
     mapping(name, attribute_id, def_value) AS (
    VALUES
        ('베이지 볼캡', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '사계절'),
        ('베이지 볼캡', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('베이지 볼캡', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '베이지'),
        ('베이지 볼캡', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '면'),
        ('베이지 볼캡', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '캐주얼'),

        ('블랙 비니','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'겨울'),
        ('블랙 비니','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('블랙 비니','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'블랙'),
        ('블랙 비니','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'니트'),
        ('블랙 비니','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'스트릿'),

        ('블랙 버킷햇','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('블랙 버킷햇','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('블랙 버킷햇','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'블랙'),
        ('블랙 버킷햇','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'면'),
        ('블랙 버킷햇','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'스트릿'),

        ('브라운 페도라','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'가을'),
        ('브라운 페도라','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('브라운 페도라','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'브라운'),
        ('브라운 페도라','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'울'),
        ('브라운 페도라','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'클래식'),

        ('카키 베레모','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'가을'),
        ('카키 베레모','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('카키 베레모','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'카키'),
        ('카키 베레모','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'울'),
        ('카키 베레모','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'클래식')
)
INSERT INTO tbl_clothes_attributes_values (id, clothes_id, attribute_id, def_value, created_at)
SELECT gen_random_uuid(), c.id, m.attribute_id, m.def_value, NOW()
FROM mapping m
         JOIN tbl_clothes c
              ON c.name = m.name
                  AND c.owner_id = (SELECT owner_id FROM owner)
ON CONFLICT (clothes_id, attribute_id) DO NOTHING;

-- ===========================================
-- 가방 (BAG)
-- ===========================================
WITH owner AS (SELECT current_setting('app.owner_id')::uuid AS owner_id),
     mapping(name, attribute_id, def_value) AS (
    VALUES
        ('블랙 크로스백', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '사계절'),
        ('블랙 크로스백', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('블랙 크로스백', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '블랙'),
        ('블랙 크로스백', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '가죽'),
        ('블랙 크로스백', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '캐주얼'),

        ('베이지 토트백','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('베이지 토트백','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('베이지 토트백','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'베이지'),
        ('베이지 토트백','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'가죽'),
        ('베이지 토트백','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'캐주얼'),

        ('미니 백팩','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('미니 백팩','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('미니 백팩','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'블랙'),
        ('미니 백팩','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'나일론'),
        ('미니 백팩','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'스트릿'),

        ('캔버스 토트백','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('캔버스 토트백','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('캔버스 토트백','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'베이지'),
        ('캔버스 토트백','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'면'),
        ('캔버스 토트백','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'캐주얼'),

        ('블랙 메신저백','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('블랙 메신저백','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('블랙 메신저백','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'블랙'),
        ('블랙 메신저백','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'나일론'),
        ('블랙 메신저백','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'캐주얼')
)
INSERT INTO tbl_clothes_attributes_values (id, clothes_id, attribute_id, def_value, created_at)
SELECT gen_random_uuid(), c.id, m.attribute_id, m.def_value, NOW()
FROM mapping m
         JOIN tbl_clothes c
              ON c.name = m.name
                  AND c.owner_id = (SELECT owner_id FROM owner)
ON CONFLICT (clothes_id, attribute_id) DO NOTHING;

-- ===========================================
-- 스카프 (SCARF)
-- ===========================================
WITH owner AS (SELECT current_setting('app.owner_id')::uuid AS owner_id),
     mapping(name, attribute_id, def_value) AS (
    VALUES
        ('카키 머플러', '11111111-aaaa-bbbb-cccc-000000000001'::uuid, '겨울'),
        ('카키 머플러', '11111111-aaaa-bbbb-cccc-000000000002'::uuid, '공용'),
        ('카키 머플러', '11111111-aaaa-bbbb-cccc-000000000003'::uuid, '카키'),
        ('카키 머플러', '11111111-aaaa-bbbb-cccc-000000000004'::uuid, '울'),
        ('카키 머플러', '11111111-aaaa-bbbb-cccc-000000000005'::uuid, '클래식'),

        ('실크 스카프','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'봄'),
        ('실크 스카프','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'여성'),
        ('실크 스카프','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'네이비'),
        ('실크 스카프','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'레이온'),
        ('실크 스카프','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'포멀'),

        ('체크 머플러','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'겨울'),
        ('체크 머플러','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('체크 머플러','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'브라운'),
        ('체크 머플러','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'울'),
        ('체크 머플러','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'클래식')
)
INSERT INTO tbl_clothes_attributes_values (id, clothes_id, attribute_id, def_value, created_at)
SELECT gen_random_uuid(), c.id, m.attribute_id, m.def_value, NOW()
FROM mapping m
         JOIN tbl_clothes c
              ON c.name = m.name
                  AND c.owner_id = (SELECT owner_id FROM owner)
ON CONFLICT (clothes_id, attribute_id) DO NOTHING;

-- ===========================================
-- 기타 (ETC)
-- ===========================================
WITH owner AS (SELECT current_setting('app.owner_id')::uuid AS owner_id),
     mapping(name, attribute_id, def_value) AS (
    VALUES
        ('우산','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('우산','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('우산','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'기타'),
        ('우산','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'기타'),
        ('우산','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'기타'),

        ('양산','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'여름'),
        ('양산','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('양산','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'화이트'),
        ('양산','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'기타'),
        ('양산','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'기타'),

        ('장갑','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'겨울'),
        ('장갑','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('장갑','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'네이비'),
        ('장갑','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'울'),
        ('장갑','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'클래식'),

        ('손수건','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('손수건','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('손수건','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'베이지'),
        ('손수건','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'면'),
        ('손수건','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'캐주얼'),

        ('휴대용선풍기','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'여름'),
        ('휴대용선풍기','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('휴대용선풍기','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'화이트'),
        ('휴대용선풍기','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'기타'),
        ('휴대용선풍기','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'기타')
)
INSERT INTO tbl_clothes_attributes_values (id, clothes_id, attribute_id, def_value, created_at)
SELECT gen_random_uuid(), c.id, m.attribute_id, m.def_value, NOW()
FROM mapping m
         JOIN tbl_clothes c
              ON c.name = m.name
                  AND c.owner_id = (SELECT owner_id FROM owner)
ON CONFLICT (clothes_id, attribute_id) DO NOTHING;

-- ===========================================
-- 양말 (SOCKS)
-- ===========================================
WITH owner AS (SELECT current_setting('app.owner_id')::uuid AS owner_id),
     mapping(name, attribute_id, def_value) AS (
    VALUES
        ('기본 화이트 양말','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('기본 화이트 양말','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('기본 화이트 양말','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'화이트'),
        ('기본 화이트 양말','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'면'),
        ('기본 화이트 양말','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'기타'),

        ('기본 블랙 양말','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('기본 블랙 양말','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('기본 블랙 양말','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'블랙'),
        ('기본 블랙 양말','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'면'),
        ('기본 블랙 양말','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'기타'),

        ('그레이 크루삭스','11111111-aaaa-bbbb-cccc-000000000001'::uuid,'사계절'),
        ('그레이 크루삭스','11111111-aaaa-bbbb-cccc-000000000002'::uuid,'공용'),
        ('그레이 크루삭스','11111111-aaaa-bbbb-cccc-000000000003'::uuid,'그레이'),
        ('그레이 크루삭스','11111111-aaaa-bbbb-cccc-000000000004'::uuid,'폴리'),
        ('그레이 크루삭스','11111111-aaaa-bbbb-cccc-000000000005'::uuid,'기타')
)
INSERT INTO tbl_clothes_attributes_values (id, clothes_id, attribute_id, def_value, created_at)
SELECT gen_random_uuid(), c.id, m.attribute_id, m.def_value, NOW()
FROM mapping m
         JOIN tbl_clothes c
              ON c.name = m.name
                  AND c.owner_id = (SELECT owner_id FROM owner)
ON CONFLICT (clothes_id, attribute_id) DO NOTHING;
