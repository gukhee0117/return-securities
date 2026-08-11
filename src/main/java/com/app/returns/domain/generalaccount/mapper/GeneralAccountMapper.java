package com.app.returns.domain.generalaccount.mapper;

import com.app.returns.domain.generalaccount.dto.GeneralAccountDTO;
import com.app.returns.domain.generalaccount.dto.request.GeneralAccountRequestDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface GeneralAccountMapper {
    //ciHash,AccountId로 조회
    Optional<GeneralAccountDTO> findByCiHashAndGeneralAccountId(GeneralAccountRequestDTO requestDTO);
    //ciHash값을 가진 고객 여부 확인
    boolean existsCustomerByCiHash(String ciHash);
    //계좌 자체의 존재 여부 확인
    boolean existsGeneralAccountById(Long generalAccountId);

}
