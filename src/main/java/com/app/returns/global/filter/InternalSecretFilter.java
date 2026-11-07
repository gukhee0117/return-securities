package com.app.returns.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/** 서비스 간 내부 API(/api/internal/**)는 X-Internal-Secret이 설정한 비밀과 같을 때만 통과시킨다. 비밀이 비어 있으면 전부 거절한다. */
public class InternalSecretFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Internal-Secret";

    private final byte[] secret;

    public InternalSecretFilter(String secret) {
        this.secret = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/internal/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String given = request.getHeader(HEADER);
        // 시간 차이로 비밀을 추측하지 못하도록 상수 시간 비교
        if (secret.length > 0
                && given != null
                && MessageDigest.isEqual(given.getBytes(StandardCharsets.UTF_8), secret)) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"인증이 필요합니다.\",\"data\":null}");
    }
}
