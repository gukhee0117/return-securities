package com.app.returns.domain.mapper;

import com.app.returns.domain.dto.DomesticTradeDTO;
import com.app.returns.domain.dto.request.DomesticTradeRequestDTO;
import com.app.returns.domain.market.TradeCandidate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DomesticTradeMapper {
    List<DomesticTradeDTO> selectByCiHash(DomesticTradeRequestDTO requestDTO);

    /** 국내 거래 이력이 있는 고객(거래 생성기 후보). */
    List<TradeCandidate> selectTradeCandidates();

    /**
     * maria가 체결한 주문을 증권사 거래로 남긴다. 체결 시각은 maria 업무시계 값(기본값인 실제 시각을 쓰지 않는다). maria 주문번호가 같으면 UNIQUE
     * 위반이다.
     */
    int insertGeneratedTrade(
            @Param("generalCustomerId") Long generalCustomerId,
            @Param("tradeType") String tradeType,
            @Param("stockCode") String stockCode,
            @Param("qty") BigDecimal qty,
            @Param("price") BigDecimal price,
            @Param("executedAt") LocalDateTime executedAt,
            @Param("mariaOrderId") Long mariaOrderId);
}
