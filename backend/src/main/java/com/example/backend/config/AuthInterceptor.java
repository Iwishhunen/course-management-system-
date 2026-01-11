package com.example.backend.config;

import com.example.backend.tools.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.lang.reflect.Method;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        // 检查是否有RoleRequire注解
        if (method.isAnnotationPresent(RoleRequire.class)) {
            RoleRequire roleRequired = method.getAnnotation(RoleRequire.class);
            String[] requiredRoles = roleRequired.value();

            // 从请求头获取token
            String token = request.getHeader("Authorization");

            if (token == null || token.isEmpty()) {
                responseUnauthorized(response, "未提供认证令牌");
                return false;
            }

            try {
                // 验证token是否过期
                if (jwtUtil.isTokenExpired(token)) {
                    responseUnauthorized(response, "认证令牌已过期");
                    return false;
                }

                // 获取用户角色
                String userRole = jwtUtil.getRoleFromToken(token);

                // 检查权限
                if (requiredRoles.length > 0) {
                    boolean hasPermission = false;
                    for (String role : requiredRoles) {
                        if (role.equals(userRole)) {
                            hasPermission = true;
                            break;
                        }
                    }

                    if (!hasPermission) {
                        responseForbidden(response, "权限不足");
                        return false;
                    }
                }
            } catch (Exception e) {
                responseUnauthorized(response, "认证令牌无效");
                return false;
            }
        }

        return true;
    }

    private void responseUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"" + message + "\",\"data\":null}");
    }

    private void responseForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(403);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"code\":403,\"msg\":\"" + message + "\",\"data\":null}");
    }
}