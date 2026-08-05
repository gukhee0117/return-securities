-- =====================================================================
-- 증권사 시스템 DDL (MariaDB / MySQL 문법) - 별도 DB
-- RIA 관리자는 이 테이블을 직접 SELECT 하지 않고 API로만 접근
-- domestic_stock_balance는 RIA 시스템(01)으로 이동됨 -> 여기 없음
-- 소프트 삭제 원칙(§8-11): general_account 등 물리 삭제 금지, status로만
-- =====================================================================

CREATE DATABASE IF NOT EXISTS securities DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE securities;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS domestic_trade;
DROP TABLE IF EXISTS registrable_stock;
DROP TABLE IF EXISTS copy_foreign_product;
DROP TABLE IF EXISTS general_account;
DROP TABLE IF EXISTS general_customer;
SET FOREIGN_KEY_CHECKS = 1;

-- 증권사 고객 마스터 (RIA customer와 동일 ci_hash 규칙)
CREATE TABLE general_customer (
    general_customer_id BIGINT      NOT NULL AUTO_INCREMENT,
    ci_hash             VARCHAR(64) NOT NULL COMMENT 'HMAC-SHA256(정규화 주민번호, 공유 PEPPER). RIA/myData와 동일 값',
    name                VARCHAR(50) NOT NULL COMMENT '이름',
    birth_date          DATE        NOT NULL COMMENT '생년월일',
    PRIMARY KEY (general_customer_id),
    UNIQUE KEY uk_general_customer_ci_hash (ci_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='증권사 고객';

-- 일반계좌 (해외+국내 주식 보관)
CREATE TABLE general_account (
    general_account_id  BIGINT      NOT NULL AUTO_INCREMENT COMMENT '증권사 시스템 자체 PK',
    general_customer_id BIGINT      NOT NULL COMMENT '증권사 고객',
    account_no          VARCHAR(10) NOT NULL COMMENT '계좌번호',
    account_type        VARCHAR(30) NOT NULL COMMENT 'BROKERAGE(종합위탁)/CMA/IRP/PENSION_SAVINGS(연금저축)/ISA (값목록 팀 확정 대기)',
    status              VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' COMMENT '물리삭제 금지, 소프트 삭제(ACTIVE/CLOSED)',
    PRIMARY KEY (general_account_id),
    CONSTRAINT chk_general_account_type   CHECK (account_type IN ('BROKERAGE','CMA','IRP','PENSION_SAVINGS','ISA')),
    CONSTRAINT chk_general_account_status CHECK (status IN ('ACTIVE','CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='일반계좌';

-- 해외 종목 마스터(증권사측 복제)
CREATE TABLE copy_foreign_product (
    foreign_product_id BIGINT       NOT NULL AUTO_INCREMENT,
    ticker             VARCHAR(20)  NOT NULL COMMENT '종목코드',
    name               VARCHAR(100) NOT NULL COMMENT '종목명',
    market             VARCHAR(50)  NULL     COMMENT '거래소',
    currency           VARCHAR(10)  NULL     COMMENT '거래통화',
    type               VARCHAR(15)  NOT NULL COMMENT 'FOREIGN_STOCK/ETF/ETN',
    PRIMARY KEY (foreign_product_id),
    CONSTRAINT chk_copy_foreign_product_type CHECK (type IN ('FOREIGN_STOCK','ETF','ETN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='해외 종목 마스터(복제)';

-- 입고 대상 종목 (2025.12.23 기준 보유수량)
CREATE TABLE registrable_stock (
    registrable_stock_id BIGINT        NOT NULL AUTO_INCREMENT,
    general_account_id   BIGINT        NOT NULL COMMENT '출처 일반계좌',
    foreign_product_id   BIGINT        NOT NULL COMMENT '해외 종목',
    held_qty             DECIMAL(15,4) NOT NULL COMMENT '2025.12.23 기준 보유수량',
    source_broker        VARCHAR(20)   NULL     COMMENT '타사 이전 시 증권사명',
    recorded_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록일 (export recored_at 오타 수정)',
    purchase_date        DATETIME      NOT NULL COMMENT '매수일자',
    purchase_price       DECIMAL(15,4) NOT NULL COMMENT '매수단가',
    purchase_currency    VARCHAR(10)   NOT NULL COMMENT '취득통화',
    purchase_fx_rate     DECIMAL(15,4) NOT NULL COMMENT '매수 결제일 기준환율',
    PRIMARY KEY (registrable_stock_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='입고 대상 종목';

-- 국내주식 매매 시뮬레이터 (RIA 계좌 국내투자를 증권사가 무작위 생성 → RIA가 ci_hash로 pull)
-- 식별: general_customer_id(내부 PK). 외부 API는 ci_hash로 받고 경계에서 id로 변환
CREATE TABLE domestic_trade (
    trade_id            BIGINT        NOT NULL AUTO_INCREMENT,
    general_customer_id BIGINT        NOT NULL COMMENT '거래 주체(사람). RIA는 ci_hash로 조회→내부 id 변환',
    trade_type          VARCHAR(10)   NOT NULL COMMENT 'BUY/SELL',
    stock_code          VARCHAR(20)   NOT NULL COMMENT '국내종목코드',
    qty                 DECIMAL(15,4) NOT NULL,
    price               DECIMAL(15,4) NOT NULL COMMENT '체결가',
    executed_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (trade_id),
    KEY idx_domestic_trade_pull (general_customer_id, trade_id),
    CONSTRAINT chk_domestic_trade_type CHECK (trade_type IN ('BUY','SELL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='국내매매 시뮬(RIA 계좌 국내투자, ci_hash pull)';

-- FOREIGN KEYS (증권사 내부만)
ALTER TABLE general_account   ADD CONSTRAINT fk_general_account__customer      FOREIGN KEY (general_customer_id) REFERENCES general_customer (general_customer_id);
ALTER TABLE registrable_stock ADD CONSTRAINT fk_rs__general_account            FOREIGN KEY (general_account_id)  REFERENCES general_account (general_account_id);
ALTER TABLE registrable_stock ADD CONSTRAINT fk_rs__copy_foreign_product       FOREIGN KEY (foreign_product_id)  REFERENCES copy_foreign_product (foreign_product_id);
ALTER TABLE domestic_trade    ADD CONSTRAINT fk_domestic_trade__customer        FOREIGN KEY (general_customer_id) REFERENCES general_customer (general_customer_id);
