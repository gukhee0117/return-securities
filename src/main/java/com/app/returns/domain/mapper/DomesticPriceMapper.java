package com.app.returns.domain.mapper;

import com.app.returns.domain.dto.DomesticPriceDTO;
import com.app.returns.domain.dto.DomesticPriceHistoryRowDTO;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DomesticPriceMapper {
    List<String> selectStockCodes();

    List<DomesticPriceDTO> selectLatestByTickers(
            @Param("tickers") List<String> tickers, @Param("asOf") LocalDate asOf);

    /** 종목별로 asOf 이하(fromDate 초과) 최근 days개의 일별 종가를 종목 코드·날짜 오름차순으로. 가격이 없거나 0 이하인 행은 건너뛴다. */
    List<DomesticPriceHistoryRowDTO> selectHistoryByTickers(
            @Param("tickers") List<String> tickers,
            @Param("asOf") LocalDate asOf,
            @Param("fromDate") LocalDate fromDate,
            @Param("days") int days);
}
