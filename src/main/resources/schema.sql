
/****** 사용자 & 인증 ******/
-- 사용자 테이블
CREATE TABLE IF NOT EXISTS tbl_users
(
    id                       UUID                     PRIMARY KEY,
    name                     VARCHAR(50)              NOT NULL,
    email                    VARCHAR(100)             UNIQUE NOT NULL,
    password                 VARCHAR(100)             NULL,
    role                     VARCHAR(10)              NOT NULL,
    is_locked                BOOLEAN                  NOT NULL,
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at               TIMESTAMP WITH TIME ZONE,
    temp_password            VARCHAR(100)             NULL,
    temp_password_expired_at TIMESTAMP WITH TIME ZONE NULL,
    -- constraints
    CONSTRAINT check_role CHECK (role IN ('USER', 'ADMIN'))
);

-- 인증 테이블
CREATE TABLE IF NOT EXISTS tbl_oauth_users
(
    id                       UUID         PRIMARY KEY,
    user_id                  UUID         NOT NULL,
    provider                 VARCHAR(20)  NOT NULL,
    provider_id              VARCHAR(300) NOT NULL,
    -- constraints
    CONSTRAINT fk_oauth_user_user FOREIGN KEY (user_id) REFERENCES tbl_users (id),
    CONSTRAINT check_provider CHECK (provider IN ('GOOGLE', 'KAKAO'))

);

-- 프로필 테이블
CREATE TABLE IF NOT EXISTS tbl_profiles
(
    id                       UUID                     PRIMARY KEY,
    user_id                  UUID                     NOT NULL,
    name                     VARCHAR(50)              NOT NULL, -- 추후 멀티프로필용 이름 추가
    gender                   VARCHAR(10),
    birth_date               DATE,
    profile_image_url        TEXT,
    location_id              UUID,
    temperature_sensitivity  INT                      ,
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at               TIMESTAMP WITH TIME ZONE,
    -- constraints
    CONSTRAINT check_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    CONSTRAINT check_temperature_sensitivity CHECK (temperature_sensitivity BETWEEN 1 AND 5),
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES tbl_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_profiles_location FOREIGN KEY (location_id) REFERENCES tbl_locations (id) ON DELETE SET NULL
);

/****** 의상 ******/
-- 의상 테이블
CREATE TABLE IF NOT EXISTS tbl_clothes
(
    id                       UUID                     PRIMARY KEY,
    owner_id                 UUID                     NOT NULL,
    name                     VARCHAR(100)             NOT NULL,
    type                     VARCHAR(20)              NOT NULL,
    image_url                TEXT,
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at               TIMESTAMP WITH TIME ZONE,
    -- constraints
    CONSTRAINT check_type CHECK (type IN ('TOP', 'BOTTOM', 'DRESS', 'OUTER', 'UNDERWEAR',
                                          'ACCESSORY', 'SHOES', 'SOCKS', 'HAT', 'BAG', 'SCARF', 'ETC')),
    CONSTRAINT fk_user_clothes FOREIGN KEY (owner_id) REFERENCES tbl_users (id) ON DELETE CASCADE
);

-- 의상 속성 테이블
CREATE TABLE IF NOT EXISTS tbl_clothes_attributes
(
    id                        UUID                     PRIMARY KEY,
    name                      VARCHAR(50)              NOT NULL,
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    -- constraints
    CONSTRAINT ux_attr_name UNIQUE (name)
);

-- 의상 선택지 정의 테이블(카테고리별 허용 값)
CREATE TABLE IF NOT EXISTS tbl_clothes_attributes_defs
(
    id                        UUID                     PRIMARY KEY,
    attribute_id              UUID                     NOT NULL,
    att_def                   VARCHAR(50)              NOT NULL,
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    -- constraints
    CONSTRAINT fk_attributes_defs_attr FOREIGN KEY (attribute_id) REFERENCES tbl_clothes_attributes (id) ON DELETE CASCADE,
    CONSTRAINT ux_attrdef_attr_attdef UNIQUE (attribute_id, att_def)
);

-- 의상 속성 값 연결 테이블
CREATE TABLE IF NOT EXISTS tbl_clothes_attributes_values
(
    id                        UUID PRIMARY KEY,
    clothes_id                UUID NOT NULL,
    attribute_id             UUID NOT NULL,
    def_value                 VARCHAR(50) NOT NULL,
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL,
    -- constraints
    CONSTRAINT fk_attr_values_clothes FOREIGN KEY (clothes_id) REFERENCES tbl_clothes (id) ON DELETE CASCADE,
    CONSTRAINT fk_attr_values_attr FOREIGN KEY (attribute_id) REFERENCES tbl_clothes_attributes (id) ON DELETE CASCADE,
    CONSTRAINT uk_cav_clothes_attribute UNIQUE (clothes_id, attribute_id)
);

/****** 피드 ******/
-- 피드 테이블
CREATE TABLE IF NOT EXISTS tbl_feeds
(
    id                        UUID                     PRIMARY KEY,
    author_id                 UUID                     NOT NULL,
    weather_id                UUID                     NOT NULL,
    content                   TEXT                     NOT NULL,
    comment_count             BIGINT                   NOT NULL DEFAULT 0,
    like_count                BIGINT                   NOT NULL DEFAULT 0,
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                TIMESTAMP WITH TIME ZONE
);

-- 피드 의상 연결 테이블
CREATE TABLE IF NOT EXISTS tbl_feed_clothes
(
    id                        UUID                     PRIMARY KEY,
    feed_id                   UUID                     NOT NULL,
    clothes_id                UUID                     NOT NULL,
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL,
    -- constraints
    CONSTRAINT fk_feed_clothes_feed FOREIGN KEY (feed_id) REFERENCES tbl_feeds (id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_clothes_clothes FOREIGN KEY (clothes_id) REFERENCES tbl_clothes (id) ON DELETE CASCADE,
    CONSTRAINT uq_feed_clothes UNIQUE (feed_id, clothes_id)
);

-- 피드 댓글 테이블
CREATE TABLE IF NOT EXISTS tbl_feed_comments
(
    id                        UUID                     PRIMARY KEY,
    author_id                 UUID                     NOT NULL,
    feed_id                   UUID                     NOT NULL,
    content                   TEXT                     NOT NULL,
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                TIMESTAMP WITH TIME ZONE,
    -- constraints
    CONSTRAINT fk_feed_comments_feed FOREIGN KEY (feed_id) REFERENCES tbl_feeds (id) ON DELETE CASCADE
);

-- 피드 댓글 좋아요 테이블
CREATE TABLE IF NOT EXISTS tbl_feed_likes
(
    id                        UUID                     PRIMARY KEY,
    feed_id                   UUID                     NOT NULL,
    user_id                   UUID                     NOT NULL,
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL,
    -- constraints
    CONSTRAINT fk_feed_likes_feed FOREIGN KEY (feed_id) REFERENCES tbl_feeds (id) ON DELETE CASCADE,
    CONSTRAINT uq_feed_like UNIQUE (feed_id, user_id)
);

/****** 날씨 ******/
-- 날씨 테이블
CREATE TABLE IF NOT EXISTS tbl_weathers
(
    id                        UUID                     PRIMARY KEY,
    location_id               UUID                     NOT NULL,
    forecasted_at             TIMESTAMP WITH TIME ZONE NOT NULL, -- 예보 산출 시각
    forecast_at               TIMESTAMP WITH TIME ZONE NOT NULL, -- 예보 대상 시각
    sky_status                VARCHAR(20)              NOT NULL,
    precipitation_type        VARCHAR(20),
    precipitation_amount      DOUBLE PRECISION,
    precipitation_probability DOUBLE PRECISION,
    humidity                  DOUBLE PRECISION,
    humidity_compared         DOUBLE PRECISION,
    temperature               DOUBLE PRECISION,
    temperature_compared      DOUBLE PRECISION,
    temperature_min           DOUBLE PRECISION,
    temperature_max           DOUBLE PRECISION,
    windspeed                 DOUBLE PRECISION,
    windspeed_level           VARCHAR(20),
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL,
    -- constraints
    CONSTRAINT check_sky_status CHECK (sky_status IN ('CLEAR','MOSTLY_CLOUDY','CLOUDY')),
    CONSTRAINT check_precipitation_type CHECK (precipitation_type IN ('NONE','RAIN','RAIN_SNOW','SNOW','SHOWER')),
    CONSTRAINT check_windspeed_level CHECK (windspeed_level IN ('WEAK','MODERATE','STRONG')),
    CONSTRAINT fk_tbl_weathers_locations FOREIGN KEY (location_id) REFERENCES tbl_locations (id) ON DELETE CASCADE
);

/****** 위치 ******/
-- 위치 테이블
CREATE TABLE IF NOT EXISTS tbl_locations
(
    id                        UUID                     PRIMARY KEY,
    latitude                  NUMERIC(8, 4)            NOT NULL,
    longitude                 NUMERIC(8, 4)            NOT NULL,
    x_coord                   INTEGER,
    y_coord                   INTEGER,
    location_names            VARCHAR(100),
    location_code             VARCHAR(20),
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL,
    -- constraints
    CONSTRAINT uq_locations UNIQUE (latitude,longitude)

);

/****** DM ******/
-- DM 채팅방
CREATE TABLE IF NOT EXISTS tbl_dm_rooms (
    id                        UUID                     PRIMARY KEY,
    dm_key                    VARCHAR(80)              NOT NULL UNIQUE,
    user1_id                  UUID
        REFERENCES tbl_users(id) ON DELETE SET NULL, -- 탈퇴 시 NULL
    user2_id                  UUID
        REFERENCES tbl_users(id) ON DELETE SET NULL, -- 탈퇴 시 NULL
    created_at                TIMESTAMPTZ              NOT NULL DEFAULT now()
);

-- DM 메시지
CREATE TABLE IF NOT EXISTS tbl_dm_messages (
    id                        UUID                     PRIMARY KEY,
    room_id                   UUID                     NOT NULL
        REFERENCES tbl_dm_rooms(id) ON DELETE CASCADE,
    sender_id                 UUID
        REFERENCES tbl_users(id) ON DELETE SET NULL,
    content                   TEXT                     NOT NULL,
    created_at                TIMESTAMPTZ              NOT NULL DEFAULT now()
);

/****** 팔로우 ******/
CREATE TABLE IF NOT EXISTS tbl_follows
(
    id              UUID                        PRIMARY KEY,
    followee_id     UUID                        NOT NULL,
    follower_id     UUID                        NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL,
    -- constraints
    CONSTRAINT fk_follows_followee FOREIGN KEY (followee_id) REFERENCES tbl_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES tbl_users (id) ON DELETE CASCADE,
    CONSTRAINT uq_follows UNIQUE (followee_id, follower_id)
);


/****** 알림 ******/
-- 알림 테이블
CREATE TABLE IF NOT EXISTS tbl_notifications
(
    id                        UUID                     PRIMARY KEY,
    receiver_id               UUID                     NOT NULL,
    title                     VARCHAR                  NOT NULL,
    content                   TEXT                     NOT NULL,
    level                     VARCHAR(10)              NOT NULL,
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    -- constraints
    CONSTRAINT check_level CHECK (level IN ('INFO', 'WARNING', 'ERROR')),
    CONSTRAINT fk_user_notification FOREIGN KEY (receiver_id) REFERENCES tbl_users (id) ON DELETE CASCADE
);

/* 인덱스 설정 */
-- tbl_weathers index
CREATE INDEX idx_tbl_weathers_location_forecasted_at
    ON tbl_weathers (location_id, forecasted_at);

CREATE INDEX idx_tbl_weathers_locations
    ON tbl_weathers (location_id);

-- tbl_locations index
CREATE INDEX idx_tbl_locations_lat_lon
    ON tbl_locations (latitude, longitude);

-- tbl_clothes index
CREATE INDEX idx_tbl_clothes_owner_id
    ON tbl_clothes (owner_id);

-- DM 메세지 인덱스 (마지막 메세지부터 조회)
CREATE INDEX IF NOT EXISTS idx_dm_messages_room_created
    ON tbl_dm_messages(room_id, created_at DESC);

CREATE INDEX IF NOT EXISTS ix_cav_clothes_attr
    ON tbl_clothes_attributes_values (clothes_id, attribute_id);

CREATE INDEX IF NOT EXISTS ix_cav_attr_defvalue
    ON tbl_clothes_attributes_values (attribute_id, def_value);

-- tbl_feed index
CREATE INDEX IF NOT EXISTS idx_feeds_createdat_id
    ON tbl_feeds(created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_feeds_likecount_id
    ON tbl_feeds(like_count DESC, id DESC);

-- tbl_feed_clothes index
CREATE INDEX IF NOT EXISTS idx_feed_clothes_feed_id
    ON tbl_feed_clothes(feed_id);

CREATE INDEX IF NOT EXISTS idx_feed_clothes_clothes_id
    ON tbl_feed_clothes(clothes_id);

-- tbl_feed_comments index
CREATE INDEX IF NOT EXISTS idx_feed_comments_feed_id
    ON tbl_feed_comments(feed_id);

-- tbl_feed_likes index
CREATE INDEX IF NOT EXISTS idx_feed_likes_feed_id
    ON tbl_feed_likes(feed_id);

-- tbl_follows index
CREATE INDEX IF NOT EXISTS idx_follows_followee_id
    ON tbl_follows(followee_id);

CREATE INDEX IF NOT EXISTS idx_follows_follower_id
    ON tbl_follows(follower_id);


/* tbl_profiles - name 컬럼 추가 & 수정 */

-- 컬럼 없으면 추가
ALTER TABLE tbl_profiles ADD COLUMN IF NOT EXISTS name VARCHAR(50);

--  NULL인 행만 데이터 채우기
UPDATE tbl_profiles p
SET name = (
    SELECT u.name
    FROM tbl_users u
    WHERE u.id = p.user_id
)
WHERE p.name IS NULL;

ALTER TABLE tbl_profiles ALTER COLUMN name SET NOT NULL;

ALTER TABLE tbl_locations
    ADD CONSTRAINT uq_locations UNIQUE (latitude, longitude);

ALTER TABLE tbl_locations
    ADD COLUMN location_code VARCHAR(20);

-- tbl_notifications_receiver index
CREATE INDEX IF NOT EXISTS idx_notifications_receiver_created
    ON tbl_notifications (receiver_id, created_at DESC);

