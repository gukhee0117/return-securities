package com.app.returns.domain.mapper;

import com.app.returns.domain.dto.FxRateDTO;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FxRateMapper {
    List<FxRateDTO> selectRates(
            @Param("currencies") List<String> currencies, @Param("asOf") LocalDate asOf);
}
