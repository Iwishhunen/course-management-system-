package com.example.backend.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.backend.tools.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, 
                             HttpServletResponse response, 
                             Object handler) throws Exception {
        // 处理OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 获取请求头中的token
        String token = request.getHeader("Authorization");

        // 如果是登录请求，则放行
        if (request.getRequestURI().contains("/auth/login")) {
            return true;
        }

        // token为空
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"msg\":\"未提供认证令牌\",\"data\":null}");
            return false;
        }

        // 验证token
        try {
            if (jwtUtil.isTokenExpired(token)) {
                response.setStatus(401);
                response.getWriter().write("{\"code\":401,\"msg\":\"认证令牌已过期\",\"data\":null}");
                return false;
            }
        } catch (IOException e) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"msg\":\"认证令牌无效\",\"data\":null}");
            return false;
        }

        return true;
    }
}