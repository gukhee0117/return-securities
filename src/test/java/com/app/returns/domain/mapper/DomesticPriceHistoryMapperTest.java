package com.app.returns.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.returns.domain.dto.DomesticPriceHistoryRowDTO;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
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

/** selectHistoryByTickers: 종목별 최근 N개 영업일 종가(asOf 이하, 오름차순). */
class DomesticPriceHistoryMapperTest {
    private static final LocalDate AS_OF = LocalDate.of(2026, 11, 17);

    private PooledDataSource dataSource;
    private SqlSession session;
    private DomesticPriceMapper mapper;

    /** 005930의 평일 종가 날짜(2026-09-01~11-17, 오름차순). 종가는 1000 + 순번이라 날짜와 값이 1:1로 대응한다. */
    private final List<LocalDate> days = new ArrayList<>();

    @BeforeEach
    void setup() throws Exception {
        dataSource =
                new PooledDataSource(
                        "org.h2.Driver",
                        "jdbc:h2:mem:hist_" + UUID.randomUUID() + ";MODE=MySQL",
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
        }
        for (LocalDate d = LocalDate.of(2026, 9, 1); !d.isAfter(AS_OF); d = d.plusDays(1)) {
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                days.add(d);
            }
        }
        for (int i = 0; i < days.size(); i++) {
            insert("005930", days.get(i), String.valueOf(1000 + i));
        }
        // 000660은 이력이 사흘뿐, 035420은 마지막 날 종가가 비정상(0)이고 그 전날은 NULL
        insert("000660", LocalDate.of(2026, 11, 13), "5000");
        insert("000660", LocalDate.of(2026, 11, 16), "5100");
        insert("000660", LocalDate.of(2026, 11, 17), "5200");
        insert("035420", LocalDate.of(2026, 11, 13), "7000");
        insert("035420", LocalDate.of(2026, 11, 16), null);
        insert("035420", LocalDate.of(2026, 11, 17), "0");
    }

    @AfterEach
    void cleanup() {
        if (session != null) session.close();
        if (dataSource != null) dataSource.forceCloseAll();
    }

    private void insert(String code, LocalDate date, String price) throws Exception {
        try (PreparedStatement ps =
                session.getConnection()
                        .prepareStatement(
                                "INSERT INTO domestic_base_price VALUES (?,?,?,'SYNTHETIC',CURRENT_TIMESTAMP)")) {
            ps.setString(1, code);
            ps.setObject(2, date);
            ps.setBigDecimal(3, price == null ? null : new BigDecimal(price));
            ps.executeUpdate();
        }
    }

    private List<DomesticPriceHistoryRowDTO> history(List<String> tickers, LocalDate asOf, int n) {
        return mapper.selectHistoryByTickers(tickers, asOf, asOf.minusDays(120), n);
    }

    @Test
    void 최근_N개를_날짜_오름차순으로_주고_마지막이_asOf_이하의_가장_최근_영업일이다() {
        var rows = history(List.of("005930"), AS_OF, 5);

        assertThat(rows).hasSize(5);
        assertThat(rows).extracting(DomesticPriceHistoryRowDTO::getTicker).containsOnly("005930");
        assertThat(rows)
                .extracting(DomesticPriceHistoryRowDTO::getBaseDate)
                .containsExactlyElementsOf(days.subList(days.size() - 5, days.size()));
        assertThat(rows.get(4).getBaseDate()).isEqualTo(AS_OF);
        assertThat(rows.get(4).getCloseKrw())
                .isEqualByComparingTo(String.valueOf(1000 + days.size() - 1));
        assertThat(rows.get(0).getCloseKrw())
                .isEqualByComparingTo(String.valueOf(1000 + days.size() - 5));
    }

    @Test
    void asOf_이후_날짜는_절대_포함하지_않고_asOf_당일은_포함한다() {
        // 11-17 당일 포함
        assertThat(history(List.of("005930"), AS_OF, 3).get(2).getBaseDate()).isEqualTo(AS_OF);
        // 과거 asOf: 그날 이전만, 마지막이 asOf 당일(평일이면)
        LocalDate past = LocalDate.of(2026, 10, 28);
        var rows = history(List.of("005930"), past, 30);
        assertThat(rows).isNotEmpty();
        assertThat(rows).allSatisfy(r -> assertThat(r.getBaseDate()).isBeforeOrEqualTo(past));
        assertThat(rows.get(rows.size() - 1).getBaseDate()).isEqualTo(past);
        // 주말 asOf(11-15 일요일)는 직전 영업일 11-13까지
        var weekend = history(List.of("005930"), LocalDate.of(2026, 11, 15), 3);
        assertThat(weekend.get(2).getBaseDate()).isEqualTo(LocalDate.of(2026, 11, 13));
    }

    @Test
    void 종목마다_따로_N개씩_고르고_종목_코드_순으로_묶어_준다() {
        var rows = history(List.of("005930", "000660"), AS_OF, 2);

        assertThat(rows)
                .extracting(DomesticPriceHistoryRowDTO::getTicker)
                .containsExactly("000660", "000660", "005930", "005930");
        assertThat(rows.get(0).getBaseDate()).isEqualTo(LocalDate.of(2026, 11, 16));
        assertThat(rows.get(1).getBaseDate()).isEqualTo(AS_OF);
    }

    @Test
    void 이력이_N개보다_적으면_있는_만큼만_주고_없는_종목은_누락된다() {
        var rows = history(List.of("000660", "999999"), AS_OF, 30);

        assertThat(rows).hasSize(3);
        assertThat(rows).extracting(DomesticPriceHistoryRowDTO::getTicker).containsOnly("000660");
        assertThat(history(List.of("999999"), AS_OF, 30)).isEmpty();
    }

    @Test
    void 데이터_시작보다_이른_asOf는_빈_결과다() {
        assertThat(history(List.of("005930"), LocalDate.of(2026, 8, 1), 30)).isEmpty();
    }

    @Test
    void fromDate_이전은_읽지_않는다() {
        var rows =
                mapper.selectHistoryByTickers(
                        List.of("005930"), AS_OF, LocalDate.of(2026, 11, 10), 30);

        // fromDate(11-10)는 포함하지 않는다: 11-11, 12, 13, 16, 17 다섯 날만
        assertThat(rows)
                .extracting(DomesticPriceHistoryRowDTO::getBaseDate)
                .containsExactly(
                        LocalDate.of(2026, 11, 11),
                        LocalDate.of(2026, 11, 12),
                        LocalDate.of(2026, 11, 13),
                        LocalDate.of(2026, 11, 16),
                        LocalDate.of(2026, 11, 17));
    }

    @Test
    void 가격이_없거나_0_이하인_행은_건너뛴다() {
        var rows = history(List.of("035420"), AS_OF, 30);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getBaseDate()).isEqualTo(LocalDate.of(2026, 11, 13));
        assertThat(rows.get(0).getCloseKrw()).isEqualByComparingTo("7000");
    }
}
