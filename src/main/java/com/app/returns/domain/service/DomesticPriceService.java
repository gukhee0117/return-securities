package com.app.returns.domain.service;

import com.app.returns.domain.dto.response.DomesticPriceHistoryResponseDTO;
import com.app.returns.domain.dto.response.DomesticPriceResponseDTO;
import java.time.LocalDate;
import java.util.List;

public interface DomesticPriceService {
    List<DomesticPriceResponseDTO> findLatest(List<String> tickers, LocalDate asOf);

    /** 종목별 asOf 이하 최근 days개 영업일 종가(날짜 오름차순). 종가가 없는 종목은 누락된다. */
    List<DomesticPriceHistoryResponseDTO> findHistory(
            List<String> tickers, LocalDate asOf, int days);
}
