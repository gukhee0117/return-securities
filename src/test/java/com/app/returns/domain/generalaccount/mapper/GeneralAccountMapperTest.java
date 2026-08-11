package com.app.returns.domain.generalaccount.mapper;

import com.app.returns.domain.generalaccount.dto.GeneralAccountDTO;
import com.app.returns.domain.generalaccount.dto.request.GeneralAccountRequestDTO;
import com.app.returns.domain.generalaccount.type.GeneralStatus;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GeneralAccountMapperTest {

    private static PooledDataSource dataSource;
    private static SqlSessionFactory sqlSessionFactory;

    private SqlSession sqlSession;
    private GeneralAccountMapper generalAccountMapper;

    @BeforeAll
    static void configureMyBatis() throws IOException {
        try (Reader reader = Resources.getResourceAsReader("mybatis-generalaccount-test-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }
        dataSource = (PooledDataSource) sqlSessionFactory.getConfiguration()
                .getEnvironment()
                .getDataSource();
    }

    @BeforeEach
    void setUpDatabase() throws SQLException {
        resetSchema();
        sqlSession = sqlSessionFactory.openSession(true);
        generalAccountMapper = sqlSession.getMapper(GeneralAccountMapper.class);
    }

    @AfterEach
    void closeSession() {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }

    @AfterAll
    static void closeDataSource() {
        if (dataSource != null) {
            dataSource.forceCloseAll();
        }
    }

    @Test
    @DisplayName("CI에 해당하는 고객의 존재 여부를 true와 false로 구분한다")
    void existsCustomerByCiHashDistinguishesExistingAndMissingCustomer() {
        insertCustomer(1L, "ci-owner");

        assertThat(generalAccountMapper.existsCustomerByCiHash("ci-owner")).isTrue();
        assertThat(generalAccountMapper.existsCustomerByCiHash("ci-missing")).isFalse();
    }

    @Test
    @DisplayName("ID에 해당하는 일반계좌의 존재 여부를 true와 false로 구분한다")
    void existsGeneralAccountByIdDistinguishesExistingAndMissingAccount() {
        insertCustomer(1L, "ci-owner");
        insertAccount(10L, 1L, "1000000001", "ACTIVE");

        assertThat(generalAccountMapper.existsGeneralAccountById(10L)).isTrue();
        assertThat(generalAccountMapper.existsGeneralAccountById(999L)).isFalse();
    }

    @Test
    @DisplayName("계좌 ID와 소유자 CI가 모두 일치할 때만 일반계좌를 반환한다")
    void findByCiHashAndGeneralAccountIdRequiresMatchingOwner() {
        insertCustomer(1L, "ci-owner");
        insertCustomer(2L, "ci-other");
        insertAccount(10L, 1L, "1000000001", "ACTIVE");

        GeneralAccountRequestDTO ownerRequest = request("ci-owner", 10L);
        GeneralAccountRequestDTO otherRequest = request("ci-other", 10L);

        Optional<GeneralAccountDTO> ownerResult =
                generalAccountMapper.findByCiHashAndGeneralAccountId(ownerRequest);
        Optional<GeneralAccountDTO> otherResult =
                generalAccountMapper.findByCiHashAndGeneralAccountId(otherRequest);

        assertThat(ownerResult).isPresent();
        assertThat(ownerResult.orElseThrow().getAccountNo()).isEqualTo("1000000001");
        assertThat(otherResult).isEmpty();
    }

    @Test
    @DisplayName("CLOSED 문자열을 GeneralStatus.CLOSED Enum으로 매핑한다")
    void findByCiHashAndGeneralAccountIdMapsClosedStatus() {
        insertCustomer(1L, "ci-owner");
        insertAccount(10L, 1L, "1000000001", "CLOSED");

        GeneralAccountDTO result = generalAccountMapper
                .findByCiHashAndGeneralAccountId(request("ci-owner", 10L))
                .orElseThrow();

        assertThat(result.getStatus()).isEqualTo(GeneralStatus.CLOSED);
    }

    private GeneralAccountRequestDTO request(String ciHash, Long generalAccountId) {
        return GeneralAccountRequestDTO.builder()
                .ciHash(ciHash)
                .generalAccountId(generalAccountId)
                .build();
    }

    private void resetSchema() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
            statement.execute("""
                    CREATE TABLE general_customer (
                        general_customer_id BIGINT PRIMARY KEY,
                        ci_hash VARCHAR(64) NOT NULL UNIQUE,
                        name VARCHAR(50) NOT NULL,
                        birth_date DATE NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE general_account (
                        general_account_id BIGINT PRIMARY KEY,
                        general_customer_id BIGINT NOT NULL,
                        account_no VARCHAR(10) NOT NULL,
                        account_type VARCHAR(30) NOT NULL,
                        status VARCHAR(10) NOT NULL,
                        FOREIGN KEY (general_customer_id) REFERENCES general_customer (general_customer_id)
                    )
                    """);
        }
    }

    private void insertCustomer(Long customerId, String ciHash) {
        execute("""
                INSERT INTO general_customer (general_customer_id, ci_hash, name, birth_date)
                VALUES (%d, '%s', '테스트 고객', DATE '1990-01-01')
                """.formatted(customerId, ciHash));
    }

    private void insertAccount(Long accountId, Long customerId, String accountNo, String status) {
        execute("""
                INSERT INTO general_account
                    (general_account_id, general_customer_id, account_no, account_type, status)
                VALUES (%d, %d, '%s', 'BROKERAGE', '%s')
                """.formatted(accountId, customerId, accountNo, status));
    }

    private void execute(String sql) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
