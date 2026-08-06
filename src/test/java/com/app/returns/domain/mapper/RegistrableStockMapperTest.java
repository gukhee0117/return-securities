package com.app.returns.domain.mapper;

import com.app.returns.domain.dto.RegistrableStockDTO;
import com.app.returns.domain.dto.request.RegistrableStockRequestDTO;
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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrableStockMapperTest {

    private static PooledDataSource dataSource;
    private static SqlSessionFactory sqlSessionFactory;

    private SqlSession sqlSession;
    private RegistrableStockMapper registrableStockMapper;

    @BeforeAll
    static void configureMyBatis() throws IOException {
        try (Reader reader = Resources.getResourceAsReader("mybatis-registrablestock-test-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }
        dataSource = (PooledDataSource) sqlSessionFactory
                .getConfiguration()
                .getEnvironment()
                .getDataSource();
    }

    @BeforeEach
    void setUpDatabase() throws SQLException {
        resetSchema();
        sqlSession = sqlSessionFactory.openSession(true);
        registrableStockMapper = sqlSession.getMapper(RegistrableStockMapper.class);
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
    @DisplayName("일치하는 계좌·상품이 있으면 값을 반환한다")
    void findHeldQtyReturnsValueWhenMatchExists() {
        insertRegistrableStock(1L, 1L, BigDecimal.valueOf(100));

        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .generalAccountId(1L)
                .foreignProductId(1L)
                .build();

        Optional<RegistrableStockDTO> result = registrableStockMapper.findHeldQty(request);

        assertThat(result).isPresent();
        assertThat(result.get().getHeldQty()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("계좌는 같지만 상품이 다르면 결과가 없다")
    void findHeldQtyReturnsEmptyWhenProductDiffers() {
        insertRegistrableStock(1L, 1L, BigDecimal.valueOf(100));

        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .generalAccountId(1L)
                .foreignProductId(2L)
                .build();

        Optional<RegistrableStockDTO> result = registrableStockMapper.findHeldQty(request);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("상품은 같지만 계좌가 다르면 결과가 없다")
    void findHeldQtyReturnsEmptyWhenAccountDiffers() {
        insertRegistrableStock(1L, 1L, BigDecimal.valueOf(100));

        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .generalAccountId(2L)
                .foreignProductId(1L)
                .build();

        Optional<RegistrableStockDTO> result = registrableStockMapper.findHeldQty(request);

        assertThat(result).isEmpty();
    }

    private void resetSchema() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
            statement.execute("""
                    CREATE TABLE registrable_stock (
                        registrable_stock_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        general_account_id BIGINT NOT NULL,
                        foreign_product_id BIGINT NOT NULL,
                        held_qty DECIMAL(15, 4) NOT NULL,
                        source_broker VARCHAR(20),
                        recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        purchase_date DATETIME NOT NULL,
                        purchase_price DECIMAL(15, 4) NOT NULL,
                        purchase_currency VARCHAR(10) NOT NULL,
                        purchase_fx_rate DECIMAL(15, 4) NOT NULL
                    )
                    """);
        }
    }

    private void insertRegistrableStock(Long generalAccountId, Long foreignProductId, BigDecimal heldQty) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO registrable_stock
                    (general_account_id, foreign_product_id, held_qty, purchase_date, purchase_price, purchase_currency, purchase_fx_rate)
                    VALUES (%d, %d, %s, '%s', 150.25, 'USD', 1320.5)
                    """.formatted(
                    generalAccountId, foreignProductId, heldQty, LocalDateTime.now()
            ));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}