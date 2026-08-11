package com.app.returns.domain.generalaccount.mapper;

import com.app.returns.domain.generalaccount.dto.GeneralAccountDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface GeneralAccountMapper {
    //ciHash,AccountId로 조회
    Optional<GeneralAccountDTO> findByCiHashAndGeneralAccountId(@Param("ciHash") String ciHash,
                                                                @Param("generalAccountId") Long generalAccountId);
    //ciHash값을 가진 고객 여부 확인
    boolean existsCustomerByCiHash(String ciHash);
    //계좌 자체의 존재 여부 확인
    boolean existsGeneralAccountById(Long generalAccountId);

}
