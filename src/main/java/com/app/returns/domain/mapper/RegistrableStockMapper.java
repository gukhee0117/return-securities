package com.app.returns.domain.mapper;

import com.app.returns.domain.dto.RegistrableStockDTO;
import com.app.returns.domain.dto.request.RegistrableStockRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import java.util.Optional;

@Mapper
public interface RegistrableStockMapper {
    Optional<RegistrableStockDTO> findHeldQty(RegistrableStockRequestDTO requestDTO);
}
