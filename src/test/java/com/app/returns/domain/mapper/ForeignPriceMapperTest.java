package com.app.returns.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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

class ForeignPriceMapperTest {
    private PooledDataSource dataSource;
    private SqlSession session;
    private ForeignPriceMapper foreign;
    private FxRateMapper fx;

    @BeforeEach
    void setup() throws Exception {
        dataSource =
                new PooledDataSource(
                        "org.h2.Driver",
                        "jdbc:h2:mem:fprice_" + UUID.randomUUID() + ";MODE=MySQL",
                        "sa",
                        "");
        var config =
                new Configuration(
                        new Environment("test", new JdbcTransactionFactory(), dataSource));
        config.setMapUnderscoreToCamelCase(true);
        for (String resource :
                List.of(
                        "mappers/foreignprice/ForeignPriceMapper.xml",
                        "mappers/fxrate/FxRateMapper.xml")) {
            try (var reader = Resources.getResourceAsReader(resource)) {
                new XMLMapperBuilder(reader, config, resource, config.getSqlFragments()).parse();
            }
        }
        session = new SqlSessionFactoryBuilder().build(config).openSession(true);
        foreign = session.getMapper(ForeignPriceMapper.class);
        fx = session.getMapper(FxRateMapper.class);
        try (var statement = session.getConnection().createStatement()) {
            statement.execute(
                    "CREATE TABLE foreign_daily_close (ticker VARCHAR(20), price_date DATE, close_price DECIMAL(15,4), PRIMARY KEY(ticker, price_date))");
            statement.execute(
                    "INSERT INTO foreign_daily_close VALUES ('QQQ','2026-03-22',570.3),('QQQ','2026-03-23',571.1),('QQQ','2026-11-17',600.5),('AAPL','2026-03-22',200)");
            statement.execute(
                    "CREATE TABLE fx_daily_rate (currency VARCHAR(3), price_date DATE, base_rate DECIMAL(15,6), final_rate DECIMAL(15,6), PRIMARY KEY(currency, price_date))");
            statement.execute(
                    "INSERT INTO fx_daily_rate VALUES ('USD','2026-03-23',1500,1512.5),('USD','2026-11-17',1510,1510),('JPY','2026-03-23',9.5,9.6)");
        }
    }

    @AfterEach
    void cleanup() {
        if (session != null) session.close();
        if (dataSource != null) dataSource.forceCloseAll();
    }

    @Test
    void foreignPicksLatestRowStrictlyBeforeAsOf() {
        var march = foreign.selectPreviousCloses(List.of("QQQ"), LocalDate.of(2026, 3, 23));
        assertThat(march).hasSize(1);
        assertThat(march.get(0).getPreviousClose()).isEqualByComparingTo("570.3");
        assertThat(march.get(0).getPreviousCloseDate()).isEqualTo(LocalDate.of(2026, 3, 22));
        var end = foreign.selectPreviousCloses(List.of("QQQ"), LocalDate.of(2026, 12, 31));
        assertThat(end.get(0).getPreviousClose()).isEqualByComparingTo("600.5");
        assertThat(end.get(0).getPreviousCloseDate()).isEqualTo(LocalDate.of(2026, 11, 17));
    }

    @Test
    void foreignOmitsMissingTickersAndDatesBeforeFirstRow() {
        assertThat(foreign.selectPreviousCloses(List.of("QQQ"), LocalDate.of(2026, 3, 22)))
                .isEmpty();
        var rows =
                foreign.selectPreviousCloses(
                        List.of("QQQ", "NOPE", "AAPL"), LocalDate.of(2026, 3, 23));
        assertThat(rows).extracting(r -> r.getTicker()).containsExactly("AAPL", "QQQ");
    }

    @Test
    void fxReturnsBaseAndFinalRateOnOrBeforeAsOf() {
        var rows = fx.selectRates(List.of("USD", "JPY"), LocalDate.of(2026, 3, 23));
        assertThat(rows).hasSize(2);
        var usd =
                rows.stream().filter(r -> r.getCurrency().equals("USD")).findFirst().orElseThrow();
        assertThat(usd.getBaseRate()).isEqualByComparingTo("1500");
        assertThat(usd.getFinalRate()).isEqualByComparingTo("1512.5");
        assertThat(usd.getRateDate()).isEqualTo(LocalDate.of(2026, 3, 23));
        var later = fx.selectRates(List.of("USD"), LocalDate.of(2026, 12, 31));
        assertThat(later.get(0).getRateDate()).isEqualTo(LocalDate.of(2026, 11, 17));
        assertThat(fx.selectRates(List.of("USD"), LocalDate.of(2026, 3, 22))).isEmpty();
    }
}
