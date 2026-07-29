package com.app.returns.domain.member.service;

import com.app.returns.domain.member.dto.response.MemberResponseDTO;

public interface MemberService {
  // 회원 정보 조회
  public MemberResponseDTO getMemberById(String id);
}
