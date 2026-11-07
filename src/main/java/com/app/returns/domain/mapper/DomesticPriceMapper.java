package com.app.returns.domain.mapper;

import com.app.returns.domain.dto.DomesticPriceDTO;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DomesticPriceMapper {
    List<String> selectStockCodes();

    List<DomesticPriceDTO> selectLatestByTickers(
            @Param("tickers") List<String> tickers, @Param("asOf") LocalDate asOf);
}
