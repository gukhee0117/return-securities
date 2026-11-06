-- =====================================================================
-- 증권사 시스템 DDL (MariaDB / MySQL 문법) - 별도 DB
-- RIA 관리자는 이 테이블을 직접 SELECT 하지 않고 API로만 접근
-- domestic_stock_balance는 RIA 시스템(01)으로 이동됨 -> 여기 없음
-- 소프트 삭제 원칙(§8-11): general_account 등 물리 삭제 금지, status로만
-- =====================================================================

CREATE DATABASE IF NOT EXISTS securities DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE securities;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS fx_daily_rate;
DROP TABLE IF EXISTS foreign_daily_close;
DROP TABLE IF EXISTS domestic_base_price;
DROP TABLE IF EXISTS domestic_trade;
DROP TABLE IF EXISTS registrable_stock;
DROP TABLE IF EXISTS copy_foreign_product;
DROP TABLE IF EXISTS general_account;
DROP TABLE IF EXISTS general_customer;
SET FOREIGN_KEY_CHECKS = 1;

-- 증권사 고객 마스터 (RIA customer와 동일 ci_hash 규칙)
-- [SEED] data-generator가 채우는 테이블. 컬럼 추가/삭제 시 생성기 INSERT(컬럼목록+값)도 수정 후 재생성 필요 (seed.sql 직접 편집 금지).
CREATE TABLE general_customer (
    general_customer_id BIGINT      NOT NULL AUTO_INCREMENT,
    ci_hash             VARCHAR(64) NOT NULL COMMENT 'HMAC-SHA256(정규화 주민번호, 공유 PEPPER). RIA/myData와 동일 값',
    name                VARCHAR(50) NOT NULL COMMENT '이름',
    birth_date          DATE        NOT NULL COMMENT '생년월일',
    PRIMARY KEY (general_customer_id),
    UNIQUE KEY uk_general_customer_ci_hash (ci_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='증권사 고객';

-- 일반계좌 (해외+국내 주식 보관)
-- [SEED] data-generator가 채우는 테이블. 컬럼 추가/삭제 시 생성기 INSERT(컬럼목록+값)도 수정 후 재생성 필요 (seed.sql 직접 편집 금지).
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
-- [SEED] data-generator가 채우는 테이블. 컬럼 추가/삭제 시 생성기 INSERT(컬럼목록+값)도 수정 후 재생성 필요 (seed.sql 직접 편집 금지).
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
-- [SEED] data-generator가 채우는 테이블. 컬럼 추가/삭제 시 생성기 INSERT(컬럼목록+값)도 수정 후 재생성 필요 (seed.sql 직접 편집 금지).
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
-- [NO-SEED] 계산 데이터(서비스/골든 시나리오로 생성). 생성기 무관.
CREATE TABLE domestic_trade (
    trade_id            BIGINT        NOT NULL AUTO_INCREMENT,
    general_customer_id BIGINT        NOT NULL COMMENT '거래 주체(사람). RIA는 ci_hash로 조회→내부 id 변환',
    trade_type          VARCHAR(10)   NOT NULL COMMENT 'BUY/SELL',
    stock_code          VARCHAR(20)   NOT NULL COMMENT '국내종목코드',
    qty                 DECIMAL(15,4) NOT NULL COMMENT '수량',
    price               DECIMAL(15,4) NOT NULL COMMENT '체결가',
    executed_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '체결 시각',
    maria_order_id      BIGINT        NULL     COMMENT 'maria domestic_order_id (FK 없음, 대사·중복 방지)',
    PRIMARY KEY (trade_id),
    UNIQUE KEY uk_domestic_trade_maria_order (maria_order_id),
    KEY idx_domestic_trade_pull (general_customer_id, trade_id),
    CONSTRAINT chk_domestic_trade_type CHECK (trade_type IN ('BUY','SELL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='국내매매 시뮬(RIA 계좌 국내투자, ci_hash pull)';

-- 시세 엔진 기준가(최근 종가). 증권사에 국내 종목 마스터가 없어 FK 없음(domestic_trade.stock_code와 같은 방식)
-- [NO-SEED] 하루 1번 공공데이터 API로 받아 저장. 생성기 무관.
CREATE TABLE domestic_base_price (
    stock_code  VARCHAR(20)   NOT NULL COMMENT 'domestic_trade.stock_code와 같은 코드',
    base_date   DATE          NOT NULL COMMENT '종가 기준일',
    close_price DECIMAL(15,4) NOT NULL COMMENT '종가',
    source      VARCHAR(20)   NOT NULL COMMENT 'PUBLIC_DATA 등 출처',
    fetched_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '받은 시각',
    PRIMARY KEY (stock_code, base_date),
    CONSTRAINT chk_dbp_price CHECK (close_price > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='시세 엔진 기준가(최근 종가)';

-- 해외 일별 종가(그 날짜의 종가). 앱은 price_date < 업무일 중 최신 행을 전일 종가로 쓴다.
-- [SEED] data-generator가 채운다(MarketData.close).
CREATE TABLE foreign_daily_close (
    ticker      VARCHAR(20)   NOT NULL,
    price_date  DATE          NOT NULL,
    close_price DECIMAL(15,4) NOT NULL,
    PRIMARY KEY (ticker, price_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='해외 일별 종가(시연용 시드 시세)';

-- 그날 앱이 쓰는 고시·확정 환율. 1단위 기준(JPY도 1엔).
-- [SEED] data-generator가 채운다(MarketData.baseRate/finalRate).
CREATE TABLE fx_daily_rate (
    currency   VARCHAR(3)    NOT NULL,
    price_date DATE          NOT NULL,
    base_rate  DECIMAL(15,6) NOT NULL,
    final_rate DECIMAL(15,6) NOT NULL,
    PRIMARY KEY (currency, price_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='일별 환율(시연용 시드 시세)';

-- FOREIGN KEYS (증권사 내부만)
ALTER TABLE general_account   ADD CONSTRAINT fk_general_account__customer      FOREIGN KEY (general_customer_id) REFERENCES general_customer (general_customer_id);
ALTER TABLE registrable_stock ADD CONSTRAINT fk_rs__general_account            FOREIGN KEY (general_account_id)  REFERENCES general_account (general_account_id);
ALTER TABLE registrable_stock ADD CONSTRAINT fk_rs__copy_foreign_product       FOREIGN KEY (foreign_product_id)  REFERENCES copy_foreign_product (foreign_product_id);
ALTER TABLE domestic_trade    ADD CONSTRAINT fk_domestic_trade__customer        FOREIGN KEY (general_customer_id) REFERENCES general_customer (general_customer_id);
