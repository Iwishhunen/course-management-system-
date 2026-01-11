package com.example.backend.controller;

import com.example.backend.service.AuthService;
import common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestParam String username,
                                           @RequestParam String password,
                                           @RequestParam String userType) {
        try {
            Map<String, Object> authResult = authService.authenticate(username, password, userType);
            if (authResult.containsKey("token")) {
                return Result.success(authResult, "登录成功");
            } else {
                return Result.error("用户名或密码错误");
            }
        } catch (Exception e) {
            return Result.error("登录失败：" + e.getMessage());
        }
    }
}
