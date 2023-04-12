package com.example.auth.security;


import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 未授权的情况下返回给前端特定的json
 *
 * Created by berg on 2023/4/8.
 */
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        // todo: 返回你想要的json串
        String example = "{\"status\": 401, \"message\":\"Unauthorized\"}";
        response.getWriter().write(example);
    }
}
