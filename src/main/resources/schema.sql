-- ckin_api 데이터베이스의 쿠폰 관련 테이블 생성
CREATE TABLE IF NOT EXISTS Member (
    member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_birth DATE NOT NULL,
    member_name VARCHAR(100),
    member_email VARCHAR(100)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS CouponTemplate (
    coupontemplate_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_type_id INT NOT NULL,
    state TINYINT NOT NULL DEFAULT 1,
    template_name VARCHAR(100),
    discount_amount DECIMAL(10,2),
    min_order_amount DECIMAL(10,2),
    valid_days INT DEFAULT 30
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS Coupon (
    coupon_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    coupontemplate_id BIGINT NOT NULL,
    coupon_expiration_date DATE,
    coupon_issue_date DATE,
    coupon_used_date DATE,
    FOREIGN KEY (member_id) REFERENCES Member(member_id),
    FOREIGN KEY (coupontemplate_id) REFERENCES CouponTemplate(coupontemplate_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 테스트용 회원 데이터 (이번 달 생일자들)
INSERT IGNORE INTO Member (member_birth, member_name, member_email)
VALUES
    ('1990-09-15', '김철수', 'kim@test.com'),
    ('1985-09-20', '이영희', 'lee@test.com'),
    ('1992-09-25', '박민수', 'park@test.com'),
    ('1988-09-10', '최영수', 'choi@test.com');

-- PendingBook에서 Book으로 이관을 위한 테이블들 (실제 구조와 동일하게)
-- 실제 PendingBook 테이블은 이미 존재한다고 가정하고 테스트 데이터만 추가

-- 테스트용 PendingBook 데이터 (실제 테이블 구조에 맞게)
INSERT IGNORE INTO PendingBook (title, isbn, publisher, published_date, regular_price, discount_rate, sale_price, stock, description, status)
VALUES ('스프링 배치 완벽 가이드', '9788960881234', '에이콘출판', '2021-03-15', 35000, 10, 31500, 100, '스프링 배치를 활용한 대용량 데이터 처리', 'APPROVED'),
       ('자바 성능 튜닝', '9788968481567', '한빛미디어', '2020-08-20', 28000, 15, 23800, 150, '자바 애플리케이션 성능 최적화 가이드', 'APPROVED'),
       ('클린 코드', '9788966260959', '인사이트', '2013-12-24', 33000, 5, 31350, 200, '애자일 소프트웨어 장인 정신', 'APPROVED'),
       ('이펙티브 자바', '9788966262281', '인사이트', '2018-11-01', 36000, 10, 32400, 180, '자바 플랫폼 모범 사례', 'APPROVED'),
       ('데이터베이스 설계와 구축', '9788973389012', '한빛아카데미', '2022-02-10', 29000, 8, 26680, 120, '실무 중심 데이터베이스 설계', 'APPROVED'),
       ('Spring Boot 실전 활용', '9788960881235', '인프런', '2023-01-15', 32000, 12, 28160, 90, 'Spring Boot를 활용한 웹 애플리케이션 개발', 'APPROVED'),
       ('JPA 프로그래밍', '9788960881236', '에이콘출판', '2022-05-20', 40000, 15, 34000, 80, 'JPA를 활용한 객체지향 프로그래밍', 'APPROVED'),
       ('마이크로서비스 패턴', '9788960881237', '한빛미디어', '2021-08-10', 38000, 10, 34200, 110, '마이크로서비스 아키텍처 패턴', 'APPROVED'),
       ('카프카 완벽 가이드', '9788960881238', '한빛미디어', '2022-11-05', 42000, 8, 38640, 70, 'Apache Kafka 실무 가이드', 'APPROVED'),
       ('쿠버네티스 인 액션', '9788960881239', '에이콘출판', '2023-03-20', 45000, 12, 39600, 60, 'Kubernetes 완벽 가이드', 'APPROVED'),
       ('도커 완벽 가이드', '9788960881240', '길벗', '2022-07-15', 35000, 10, 31500, 85, 'Docker 컨테이너 기술', 'APPROVED'),
       ('리액트 완벽 가이드', '9788960881241', '골든래빗', '2023-02-10', 39000, 15, 33150, 95, 'React 프론트엔드 개발', 'APPROVED'),
       ('Vue.js 프로젝트', '9788960881242', '한빛미디어', '2022-09-25', 33000, 8, 30360, 105, 'Vue.js를 활용한 SPA 개발', 'APPROVED'),
       ('Node.js 교과서', '9788960881243', '길벗', '2023-04-05', 36000, 12, 31680, 75, 'Node.js 백엔드 개발', 'APPROVED'),
       ('파이썬 머신러닝', '9788960881244', '길벗', '2022-12-20', 48000, 10, 43200, 65, '파이썬을 활용한 머신러닝', 'APPROVED'),
       ('딥러닝 입문', '9788960881245', '제이펍', '2023-01-30', 52000, 15, 44200, 55, '딥러닝 기초와 실습', 'APPROVED'),
       ('알고리즘 문제 해결 전략', '9788960881246', '인사이트', '2022-06-10', 44000, 8, 40480, 88, '프로그래밍 대회 알고리즘', 'APPROVED'),
       ('네트워크 보안 실무', '9788960881247', '피어슨에듀케이션', '2023-05-15', 41000, 12, 36080, 78, '네트워크 보안 기술', 'APPROVED'),
       ('운영체제 개념', '9788960881248', '퍼스트북', '2022-10-08', 46000, 10, 41400, 92, '운영체제 핵심 개념', 'APPROVED'),
       ('컴파일러 설계', '9788960881249', '에이콘출판', '2023-06-25', 55000, 15, 46750, 45, '컴파일러 이론과 실습', 'APPROVED');
