package com.app.returns.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.app.returns.domain.market.TradeCandidate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DomesticTradeGeneratorMapperTest {
    private PooledDataSource dataSource;
    private SqlSession session;
    private DomesticTradeMapper mapper;

    @BeforeEach
    void setup() throws Exception {
        dataSource =
                new PooledDataSource(
                        "org.h2.Driver",
                        "jdbc:h2:mem:trade_" + UUID.randomUUID() + ";MODE=MySQL",
                        "sa",
                        "");
        var config =
                new Configuration(
                        new Environment("test", new JdbcTransactionFactory(), dataSource));
        config.setMapUnderscoreToCamelCase(true);
        config.getTypeAliasRegistry().registerAliases("com.app.returns.domain.dto");
        String resource = "mappers/domestictrade/DomesticTradeMapper.xml";
        try (var reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, config, resource, config.getSqlFragments()).parse();
        }
        session = new SqlSessionFactoryBuilder().build(config).openSession(true);
        mapper = session.getMapper(DomesticTradeMapper.class);
        try (var s = session.getConnection().createStatement()) {
            s.execute(
                    "CREATE TABLE general_customer (general_customer_id BIGINT PRIMARY KEY, ci_hash VARCHAR(64))");
            s.execute(
                    "CREATE TABLE domestic_trade (trade_id BIGINT AUTO_INCREMENT PRIMARY KEY, general_customer_id BIGINT NOT NULL,"
                            + " trade_type VARCHAR(10) NOT NULL, stock_code VARCHAR(20) NOT NULL, qty DECIMAL(15,4) NOT NULL,"
                            + " price DECIMAL(15,4) NOT NULL, executed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                            + " maria_order_id BIGINT NULL, CONSTRAINT uk_maria UNIQUE (maria_order_id))");
            s.execute(
                    "INSERT INTO general_customer VALUES (1,'"
                            + "a".repeat(64)
                            + "'),(2,'"
                            + "b".repeat(64)
                            + "'),(3,'"
                            + "c".repeat(64)
                            + "')");
            s.execute(
                    "INSERT INTO domestic_trade (general_customer_id,trade_type,stock_code,qty,price) VALUES"
                            + " (1,'BUY','005930',1,100),(1,'SELL','005930',1,100),(2,'BUY','000660',1,100)");
        }
    }

    @AfterEach
    void cleanup() {
        if (session != null) session.close();
        if (dataSource != null) dataSource.forceCloseAll();
    }

    @Test
    void 후보는_거래한_고객만_중복_없이() {
        List<TradeCandidate> candidates = mapper.selectTradeCandidates();
        assertThat(candidates)
                .extracting(TradeCandidate::getGeneralCustomerId)
                .containsExactlyInAnyOrder(1L, 2L);
        assertThat(candidates)
                .filteredOn(c -> c.getGeneralCustomerId() == 1L)
                .extracting(TradeCandidate::getCiHash)
                .containsExactly("a".repeat(64));
    }

    @Test
    void 생성_거래는_넘긴_체결_시각으로_들어가고_같은_주문번호는_거부된다() throws Exception {
        LocalDateTime at = LocalDateTime.of(2026, 11, 18, 10, 0, 5);
        mapper.insertGeneratedTrade(
                1L, "BUY", "005930", new BigDecimal("3"), new BigDecimal("85500"), at, 77L);
        try (var s = session.getConnection().createStatement();
                var r =
                        s.executeQuery(
                                "SELECT executed_at, trade_type, qty FROM domestic_trade WHERE maria_order_id = 77")) {
            assertThat(r.next()).isTrue();
            assertThat(r.getTimestamp(1).toLocalDateTime()).isEqualTo(at);
            assertThat(r.getString(2)).isEqualTo("BUY");
            assertThat(r.getBigDecimal(3)).isEqualByComparingTo("3");
        }
        assertThatThrownBy(
                        () ->
                                mapper.insertGeneratedTrade(
                                        1L,
                                        "BUY",
                                        "005930",
                                        BigDecimal.ONE,
                                        BigDecimal.ONE,
                                        at,
                                        77L))
                .isNotNull();
    }
}
