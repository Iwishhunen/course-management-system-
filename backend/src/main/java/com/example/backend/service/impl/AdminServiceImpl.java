package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.Admin;
import com.example.backend.entity.Course;
import com.example.backend.entity.Enrollmentperiod;
import com.example.backend.mapper.AdminMapper;
import com.example.backend.service.IAdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.service.ICourseService;
import com.example.backend.service.IEnrollmentService;
import com.example.backend.service.IEnrollmentperiodService;
import com.example.backend.service.IStudentService;
import com.example.backend.service.ITeacherService;
import com.example.backend.service.IStatisticsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 管理员基本信息表 服务实现类
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

    @Autowired
    private IEnrollmentperiodService enrollmentperiodService;

    @Autowired
    @Lazy
    private ICourseService courseService;

    @Autowired
    private IEnrollmentService enrollmentService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private ITeacherService teacherService;

    @Autowired
    private IStatisticsService statisticsService;

    @Override
    public Admin validateLogin(String adminId, String password) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("admin_id", adminId)
                .eq("password", password);
        return getOne(queryWrapper);
    }

    @Override
    public boolean setEnrollmentPeriod(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Enrollmentperiod period = new Enrollmentperiod();
            period.setStartTime(startTime);
            period.setEndTime(endTime);
            period.setIsActive(false); // 默认不激活

            return enrollmentperiodService.save(period);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Enrollmentperiod> getAllEnrollmentPeriods() {
        return enrollmentperiodService.list();
    }

    @Override
    public boolean activateEnrollmentPeriod(Integer periodId) {
        try {
            // 先将所有时间段设为非激活状态
            List<Enrollmentperiod> allPeriods = enrollmentperiodService.list();
            for (Enrollmentperiod period : allPeriods) {
                period.setIsActive(false);
                enrollmentperiodService.updateById(period);
            }

            // 激活指定的时间段
            Enrollmentperiod period = enrollmentperiodService.getById(periodId);
            if (period != null) {
                period.setIsActive(true);
                return enrollmentperiodService.updateById(period);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deactivateEnrollmentPeriod(Integer periodId) {
        try {
            Enrollmentperiod period = enrollmentperiodService.getById(periodId);
            if (period != null) {
                period.setIsActive(false);
                return enrollmentperiodService.updateById(period);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 统计各类实体数量
        stats.put("studentCount", studentService.count());
        stats.put("teacherCount", teacherService.count());
        stats.put("courseCount", courseService.count());
        stats.put("enrollmentCount", enrollmentService.count());

        // 统计选课人数不足的课程数量
        List<Course> lowEnrollmentCourses = courseService.findLowEnrollmentCourses();
        stats.put("lowEnrollmentCourseCount", lowEnrollmentCourses.size());

        // 统计活跃选课时间段数量
        QueryWrapper<Enrollmentperiod> periodQueryWrapper = new QueryWrapper<>();
        periodQueryWrapper.eq("is_active", true);
        List<Enrollmentperiod> activePeriods = enrollmentperiodService.list(periodQueryWrapper);

        LocalDateTime now = LocalDateTime.now();
        long activePeriodCount = activePeriods.stream()
                .filter(period -> now.isAfter(period.getStartTime()) && now.isBefore(period.getEndTime()))
                .count();
        stats.put("activePeriodCount", activePeriodCount);

        return stats;
    }

    @Override
    public List<Course> cancelLowEnrollmentCourses() {
        // 检查是否还有活跃的选课时间段
        QueryWrapper<Enrollmentperiod> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", true);
        List<Enrollmentperiod> activePeriods = enrollmentperiodService.list(queryWrapper);

        LocalDateTime now = LocalDateTime.now();
        boolean hasActivePeriod = false;
        for (Enrollmentperiod period : activePeriods) {
            if (now.isAfter(period.getStartTime()) && now.isBefore(period.getEndTime())) {
                hasActivePeriod = true;
                break;
            }
        }

        // 如果还有活跃的选课时间段，则不允许取消课程
        if (hasActivePeriod) {
            throw new RuntimeException("选课尚未结束，无法执行课程取消操作");
        }

        // 获取所有选课人数不足20人的课程
        List<Course> lowEnrollmentCourses = courseService.findLowEnrollmentCourses();

        // 为每门课程发送取消通知
        for (Course course : lowEnrollmentCourses) {
            courseService.sendCancellationNotification(course);
        }

        return lowEnrollmentCourses;
    }
}
