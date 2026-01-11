package com.example.backend.service;

import com.example.backend.entity.Admin;
import com.example.backend.entity.Enrollmentperiod;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 管理员基本信息表 服务类
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
public interface IAdminService extends IService<Admin> {

    /**
     * 验证管理员登录
     * 
     * @param adminId  管理员ID
     * @param password 密码
     * @return 管理员对象
     */
    Admin validateLogin(String adminId, String password);

    /**
     * 设置选课时间段
     * 
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 是否设置成功
     */
    boolean setEnrollmentPeriod(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取所有选课时间段
     * 
     * @return 选课时间段列表
     */
    List<Enrollmentperiod> getAllEnrollmentPeriods();

    /**
     * 激活指定的选课时间段
     * 
     * @param periodId 时间段ID
     * @return 是否激活成功
     */
    boolean activateEnrollmentPeriod(Integer periodId);

    /**
     * 取消激活选课时间段
     * 
     * @param periodId 时间段ID
     * @return 是否取消成功
     */
    boolean deactivateEnrollmentPeriod(Integer periodId);

    /**
     * 取消选课人数不足20人的课程
     * 
     * @return 被取消的课程列表
     */
    List<com.example.backend.entity.Course> cancelLowEnrollmentCourses();

    /**
     * 获取综合统计信息
     * 
     * @return 统计信息Map
     */
    java.util.Map<String, Object> getDashboardStats();
}
