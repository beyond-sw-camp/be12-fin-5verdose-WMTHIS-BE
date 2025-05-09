package com.example.be12fin5verdosewmthisbe.security;

import com.example.be12fin5verdosewmthisbe.common.CustomException;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {


        String path = request.getServletPath();

        // 인증 없이 허용할 경로들
        if (path.equals("/api/user/login") ||
                path.equals("/api/user/signup") ||
                path.equals("/api/user/searchinfo") ||
                path.equals("/api/user/updatepassword") ||
                path.equals("/api/email/sendcode") ||
                path.equals("/api/email/authcode") ||
                path.equals("/api/user/smssend") ||
                path.equals("/api/user/phoneverify") ||
                path.equals("/api/user/isLogin") ||
                path.equals("/api/email/sendcodeifpwfind") ||
                path.equals("/api/actuator/health") ||
                path.equals("/api/store/register")
        )
        {

            filterChain.doFilter(request, response); // 인증 없이 통과
            return;
        }


        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {

            String email = jwtTokenProvider.getEmailFromToken(token); // 또는 getUsernameFromToken()
            if(email == null) {
                // emailUrl이 토큰에 없을때
                throw new CustomException(ErrorCode.TOKEN_EMAIL_NOT_FOUND);
            }
            var authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            throw new CustomException(ErrorCode.TOKEN_NOT_VALIDATE);
        }

        filterChain.doFilter(request, response);
    }
}
