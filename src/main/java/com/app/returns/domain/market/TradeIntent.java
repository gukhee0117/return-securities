package com.app.returns.domain.market;

/** 이번에 누가(candidate) 사는지 파는지(side: BUY/SELL). 종목·수량은 maria가 고른다. */
public record TradeIntent(TradeCandidate candidate, String side) {}
