package com.example.backend.service;

import com.example.backend.entity.Admin;
import com.example.backend.entity.Student;
import com.example.backend.entity.Teacher;
import com.example.backend.tools.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private IAdminService adminService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private ITeacherService teacherService;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> authenticate(String username, String password, String userType) {
        Map<String, Object> result = new HashMap<>();
        String token = null;
        String role = null;
        Object user = null;

        switch (userType.toLowerCase()) {
            case "admin":
                Admin admin = adminService.validateLogin(username, password);
                if (admin != null) {
                    token = jwtUtil.generateToken(username, "ADMIN");
                    role = "ADMIN";
                    user = admin;
                    ((Admin) user).setPassword(null); // 清除密码信息
                }
                break;
            case "student":
                Student student = studentService.validateLogin(username, password);
                if (student != null) {
                    token = jwtUtil.generateToken(username, "STUDENT");
                    role = "STUDENT";
                    user = student;
                    ((Student) user).setPassword(null); // 清除密码信息
                }
                break;
            case "teacher":
                Teacher teacher = teacherService.validateLogin(username, password);
                if (teacher != null) {
                    token = jwtUtil.generateToken(username, "TEACHER");
                    role = "TEACHER";
                    user = teacher;
                    ((Teacher) user).setPassword(null); // 清除密码信息
                }
                break;
        }

        if (token != null) {
            result.put("token", token);
            result.put("role", role);
            result.put("user", user);
        }

        return result;
    }
}
