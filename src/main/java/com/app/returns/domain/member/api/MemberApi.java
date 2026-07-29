package com.app.returns.domain.member.api;


import com.app.returns.domain.member.dto.response.MemberResponseDTO;
import com.app.returns.domain.member.service.MemberService;
import com.app.returns.global.response.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberApi {

    private final MemberService memberService;

    @GetMapping("/{mid}")
    public ResponseEntity<ApiResponseDTO<MemberResponseDTO>> getMember(@PathVariable String mid) {
        MemberResponseDTO member = memberService.getMemberById(mid);
        return ResponseEntity.ok(ApiResponseDTO.of("회원 조회 성공", member));
    }
}
