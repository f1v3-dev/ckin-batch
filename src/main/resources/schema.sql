-- ckin_api 데이터베이스의 쿠폰 관련 테이블 생성
CREATE TABLE IF NOT EXISTS Member (
    member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_birth DATE NOT NULL,
    member_name VARCHAR(100),
    member_email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS CouponTemplate (
    coupontemplate_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_type_id INT NOT NULL,
    state TINYINT NOT NULL DEFAULT 1,
    template_name VARCHAR(100),
    discount_amount DECIMAL(10,2),
    min_order_amount DECIMAL(10,2),
    valid_days INT DEFAULT 30
);

CREATE TABLE IF NOT EXISTS Coupon (
    coupon_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    coupontemplate_id BIGINT NOT NULL,
    coupon_expiration_date DATE,
    coupon_issue_date DATE,
    coupon_used_date DATE,
    FOREIGN KEY (member_id) REFERENCES Member(member_id),
    FOREIGN KEY (coupontemplate_id) REFERENCES CouponTemplate(coupontemplate_id)
);

-- 테스트용 회원 데이터 (이번 달 생일자들)
INSERT IGNORE INTO Member (member_birth, member_name, member_email)
VALUES
    ('1990-09-15', '김철수', 'kim@test.com'),
    ('1985-09-20', '이영희', 'lee@test.com'),
    ('1992-09-25', '박민수', 'park@test.com'),
    ('1988-09-10', '최영수', 'choi@test.com');

-- PendingBook에서 Book으로 이관을 위한 테이블들
CREATE TABLE IF NOT EXISTS PendingBook
(
    pending_book_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    author           VARCHAR(255),
    publisher        VARCHAR(255),
    publication_date DATE,
    isbn             VARCHAR(20),
    price            DECIMAL(10, 2),
    stock            INT         DEFAULT 0,
    description      TEXT,
    status           VARCHAR(20) DEFAULT 'PENDING',
    created_date     DATETIME    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Book
(
    book_id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    title                 VARCHAR(255) NOT NULL,
    author                VARCHAR(255),
    publisher             VARCHAR(255),
    publication_date      DATE,
    isbn                  VARCHAR(20),
    price                 DECIMAL(10, 2),
    stock                 INT         DEFAULT 0,
    description           TEXT,
    status                VARCHAR(20) DEFAULT 'ACTIVE',
    created_date          DATETIME    DEFAULT CURRENT_TIMESTAMP,
    migrated_from_pending BIGINT,
    INDEX idx_isbn (isbn),
    INDEX idx_title (title)
);

-- 테스트용 PendingBook 데이터
INSERT IGNORE INTO PendingBook (title, author, publisher, publication_date, isbn, price, stock, description, status)
VALUES ('스프링 배치 완벽 가이드', '마이클 민엘라', '에이콘출판', '2021-03-15', '9788960881234', 35000.00, 100, '스프링 배치를 활용한 대용량 데이터 처리',
        'PENDING'),
       ('자바 성능 튜닝', '스콧 오크스', '한빛미디어', '2020-08-20', '9788968481567', 28000.00, 150, '자바 애플리케이션 성능 최적화 가이드', 'PENDING'),
       ('클린 코드', '로버트 C. 마틴', '인사이트', '2013-12-24', '9788966260959', 33000.00, 200, '애자일 소프트웨어 장인 정신', 'PENDING'),
       ('이펙티브 자바', '조슈아 블로크', '인사이트', '2018-11-01', '9788966262281', 36000.00, 180, '자바 플랫폼 모범 사례', 'PENDING'),
       ('데이터베이스 설계와 구축', '김연희', '한빛아카데미', '2022-02-10', '9788973389012', 29000.00, 120, '실무 중심 데이터베이스 설계', 'PENDING');
