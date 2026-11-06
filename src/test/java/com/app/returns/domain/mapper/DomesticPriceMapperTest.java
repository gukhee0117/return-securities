package com.app.returns.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.returns.domain.dto.response.DomesticPriceResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.LocalDate;
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

class DomesticPriceMapperTest {
    private PooledDataSource dataSource;
    private SqlSession session;
    private DomesticPriceMapper mapper;

    @BeforeEach
    void setup() throws Exception {
        dataSource =
                new PooledDataSource(
                        "org.h2.Driver",
                        "jdbc:h2:mem:price_" + UUID.randomUUID() + ";MODE=MySQL",
                        "sa",
                        "");
        var config =
                new Configuration(
                        new Environment("test", new JdbcTransactionFactory(), dataSource));
        config.setMapUnderscoreToCamelCase(true);
        String resource = "mappers/domesticprice/DomesticPriceMapper.xml";
        try (var reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, config, resource, config.getSqlFragments()).parse();
        }
        session = new SqlSessionFactoryBuilder().build(config).openSession(true);
        mapper = session.getMapper(DomesticPriceMapper.class);
        try (var statement = session.getConnection().createStatement()) {
            statement.execute(
                    "CREATE TABLE domestic_base_price (stock_code VARCHAR(20), base_date DATE, close_price DECIMAL(15,4), source VARCHAR(20), fetched_at TIMESTAMP, PRIMARY KEY(stock_code, base_date))");
            statement.execute(
                    "INSERT INTO domestic_base_price VALUES ('005930','2026-11-03',70000,'PUBLIC_DATA','2026-11-03 18:00:00'),('005930','2026-11-17',71000,'PUBLIC_DATA','2026-11-17 18:00:00'),('000660','2026-11-16',150000,'PUBLIC_DATA','2026-11-16 18:00:00'),('035420','2026-11-17',200000,'PUBLIC_DATA','2026-11-17 18:00:00')");
        }
    }

    @AfterEach
    void cleanup() {
        if (session != null) session.close();
        if (dataSource != null) dataSource.forceCloseAll();
    }

    @Test
    void returnsLatestStoredRowPerRequestedTickerAndPreservesMetadata() throws Exception {
        var rows =
                mapper.selectLatestByTickers(
                        List.of("005930", "000660", "999999"), LocalDate.of(2026, 11, 17));
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).getTicker()).isEqualTo("000660");
        var samsung = rows.get(1);
        assertThat(samsung.getPriceKrw()).isEqualByComparingTo("71000");
        assertThat(samsung.getBaseDate()).isEqualTo(LocalDate.of(2026, 11, 17));
        assertThat(samsung.getPriceAt()).isEqualTo(LocalDateTime.of(2026, 11, 17, 18, 0));
        assertThat(samsung.getSource()).isEqualTo("PUBLIC_DATA");
        var json =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .valueToTree(new DomesticPriceResponseDTO(samsung));
        assertThat(json.get("ticker").asText()).isEqualTo("005930");
        assertThat(json.get("priceKrw").decimalValue())
                .isEqualByComparingTo(new BigDecimal("71000"));
    }

    @Test
    void updatedPriceIsVisibleOnTheNextReadWithoutChangingHistory() throws Exception {
        assertThat(
                        mapper.selectLatestByTickers(List.of("005930"), LocalDate.of(2026, 11, 17))
                                .get(0)
                                .getPriceKrw())
                .isEqualByComparingTo("71000");
        try (var statement = session.getConnection().createStatement()) {
            statement.execute(
                    "UPDATE domestic_base_price SET close_price=72000, fetched_at='2026-11-18 09:00:00' WHERE stock_code='005930' AND base_date='2026-11-17'");
        }
        session.clearCache();
        var updated =
                mapper.selectLatestByTickers(List.of("005930"), LocalDate.of(2026, 11, 17)).get(0);
        assertThat(updated.getPriceKrw()).isEqualByComparingTo("72000");
        assertThat(updated.getPriceAt()).isEqualTo(LocalDateTime.of(2026, 11, 18, 9, 0));
        try (var statement = session.getConnection().createStatement();
                var result =
                        statement.executeQuery(
                                "SELECT close_price FROM domestic_base_price WHERE stock_code='005930' AND base_date='2026-11-03'")) {
            assertThat(result.next()).isTrue();
            assertThat(result.getBigDecimal(1)).isEqualByComparingTo("70000");
        }
    }

    @Test
    void invalidLatestPriceIsOmittedWithoutFallingBackToOlderPositivePrice() throws Exception {
        for (String price : List.of("0", "-1", "NULL")) {
            try (var statement = session.getConnection().createStatement()) {
                statement.execute(
                        "UPDATE domestic_base_price SET close_price="
                                + price
                                + " WHERE stock_code='005930' AND base_date='2026-11-17'");
            }
            session.clearCache();
            assertThat(mapper.selectLatestByTickers(List.of("005930"), LocalDate.of(2026, 11, 17)))
                    .isEmpty();
        }
    }

    @Test
    void asOfPicksLatestRowOnOrBeforeAndReturnsPreviousClose() throws Exception {
        var onDay =
                mapper.selectLatestByTickers(List.of("005930"), LocalDate.of(2026, 11, 17)).get(0);
        assertThat(onDay.getPriceKrw()).isEqualByComparingTo("71000");
        assertThat(onDay.getPreviousCloseKrw()).isEqualByComparingTo("70000");
        var afterLast =
                mapper.selectLatestByTickers(List.of("005930"), LocalDate.of(2026, 12, 31)).get(0);
        assertThat(afterLast.getBaseDate()).isEqualTo(LocalDate.of(2026, 11, 17));
        assertThat(afterLast.getPreviousCloseKrw()).isEqualByComparingTo("70000");
        var between =
                mapper.selectLatestByTickers(List.of("005930"), LocalDate.of(2026, 11, 10)).get(0);
        assertThat(between.getBaseDate()).isEqualTo(LocalDate.of(2026, 11, 3));
        assertThat(between.getPreviousCloseKrw()).isNull();
        assertThat(mapper.selectLatestByTickers(List.of("005930"), LocalDate.of(2026, 3, 1)))
                .isEmpty();
    }
}
