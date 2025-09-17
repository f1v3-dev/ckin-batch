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

-- 생일 쿠폰 템플릿 데이터 (template_type_id = 1)
INSERT IGNORE INTO CouponTemplate (template_type_id, state, template_name, discount_amount, min_order_amount, valid_days)
VALUES (1, 1, '생일 축하 쿠폰', 5000.00, 20000.00, 30);

-- 테스트용 회원 데이터 (이번 달 생일자들)
INSERT IGNORE INTO Member (member_birth, member_name, member_email)
VALUES
    ('1990-09-15', '김철수', 'kim@test.com'),
    ('1985-09-20', '이영희', 'lee@test.com'),
    ('1992-09-25', '박민수', 'park@test.com'),
    ('1988-09-10', '최영수', 'choi@test.com');
