package com.app.returns.domain.mapper;

import com.app.returns.domain.dto.ForeignPriceDTO;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ForeignPriceMapper {
    List<ForeignPriceDTO> selectPreviousCloses(
            @Param("tickers") List<String> tickers, @Param("asOf") LocalDate asOf);
}
