package com.example.backend.service;

import com.example.backend.entity.Student;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 学生基本信息表 服务类
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
public interface IStudentService extends IService<Student> {

    /**
     * 根据学号查找学生
     * @param studentId 学号
     * @return 学生对象
     */
    Student findByStudentId(String studentId);

    /**
     * 验证学生登录
     * @param studentId 学号
     * @param password 密码
     * @return 学生对象
     */
    Student validateLogin(String studentId, String password);

    /**
     * 修改密码
     * @param studentId 学号
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(String studentId, String oldPassword, String newPassword);

    /**
     * 获取学生的课表
     * @param studentId 学生ID
     * @return 课表信息
     */
    List<Map<String, Object>> getStudentSchedule(String studentId);
}
