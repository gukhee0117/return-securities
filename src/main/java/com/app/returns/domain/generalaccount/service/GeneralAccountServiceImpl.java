package com.app.returns.domain.generalaccount.service;

import com.app.returns.domain.generalaccount.dto.GeneralAccountDTO;
import com.app.returns.domain.generalaccount.dto.request.GeneralAccountRequestDTO;
import com.app.returns.domain.generalaccount.dto.response.GeneralAccountResponseDTO;
import com.app.returns.domain.generalaccount.exception.GeneralAccountException;
import com.app.returns.domain.generalaccount.exception.GeneralAccountNotFoundException;
import com.app.returns.domain.generalaccount.mapper.GeneralAccountMapper;
import com.app.returns.domain.generalaccount.type.GeneralStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GeneralAccountServiceImpl implements GeneralAccountService{
    private final GeneralAccountMapper generalAccountMapper;

    @Override
    public GeneralAccountResponseDTO verifyGeneralAccount(GeneralAccountRequestDTO requestDTO){
        if(!generalAccountMapper.existsCustomerByCiHash(requestDTO.getCiHash())){
            throw new GeneralAccountNotFoundException(
                    "고객을 찾을 수 없습니다."
            );

        }

        if(!generalAccountMapper.existsGeneralAccountById(requestDTO.getGeneralAccountId())){
            throw new GeneralAccountNotFoundException(
                    "계좌를 찾을 수 없습니다."
            );
        }

        GeneralAccountDTO account = generalAccountMapper.findByCiHashAndGeneralAccountId(
                requestDTO.getCiHash(), requestDTO.getGeneralAccountId())
                .orElseThrow(()->new GeneralAccountException(
                        "해당 고객 소유의 계좌가 아닙니다."
                ));

        if(account.getStatus()== GeneralStatus.CLOSED){
            throw new GeneralAccountException(
                    "해지된 일반계좌입니다."
            );
        }



        return new GeneralAccountResponseDTO(account);

    }


}
