package com.example.backend.service;

import com.example.backend.entity.Teacher;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 教师基本信息表 服务类
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
public interface ITeacherService extends IService<Teacher> {

    /**
     * 根据工号查找教师
     * 
     * @param teacherId 教师工号
     * @return 教师对象
     */
    Teacher findByTeacherId(String teacherId);

    /**
     * 验证教师登录
     * 
     * @param teacherId 教师工号
     * @param password  密码
     * @return 教师对象
     */
    Teacher validateLogin(String teacherId, String password);

    /**
     * 修改密码
     * 
     * @param teacherId   教师工号
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(String teacherId, String oldPassword, String newPassword);

    /**
     * 获取教师教授的课程列表
     * 
     * @param teacherId 教师工号
     * @return 课程列表
     */
    List<Map<String, Object>> getTeacherCourses(String teacherId);

    /**
     * 获取课程的选课学生名单
     * 
     * @param courseId 课程ID
     * @return 学生名单
     */
    List<Map<String, Object>> getCourseStudents(String courseId);

    /**
     * 获取教师课表
     * 
     * @param teacherId 教师工号
     * @return 课表信息
     */
    List<Map<String, Object>> getTeacherSchedule(String teacherId);
}
