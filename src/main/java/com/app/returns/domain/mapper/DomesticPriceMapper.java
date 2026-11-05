package com.app.returns.domain.mapper;

import com.app.returns.domain.dto.DomesticPriceDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DomesticPriceMapper {
    List<DomesticPriceDTO> selectLatestByTickers(@Param("tickers") List<String> tickers);
}
