package com.example.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Course;
import com.example.backend.entity.Enrollment;
import com.example.backend.entity.Enrollmentperiod;
import com.example.backend.entity.Schedule;
import com.example.backend.mapper.CourseMapper;
import com.example.backend.mapper.EnrollmentMapper;
import com.example.backend.mapper.ScheduleMapper;
import com.example.backend.service.IEnrollmentService;
import com.example.backend.service.IEnrollmentperiodService;

/**
 * <p>
 * 选课记录基本信息表 服务实现类
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@Service
public class EnrollmentServiceImpl extends ServiceImpl<EnrollmentMapper, Enrollment> implements IEnrollmentService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    public List<Enrollment> findByStudentId(String studentId) {
        QueryWrapper<Enrollment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        return list(queryWrapper);
    }

    @Override
    public List<Enrollment> findByCourseId(String courseId) {
        QueryWrapper<Enrollment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        return list(queryWrapper);
    }

    @Override
    public boolean isEnrolled(String studentId, String courseId) {
        QueryWrapper<Enrollment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId)
                .eq("course_id", courseId);
        return count(queryWrapper) > 0;
    }

    @Override
    public boolean enrollCourse(String studentId, String courseId) throws Exception {
        // 1. 检查是否在选课时间段内
        if (!isInEnrollmentPeriod()) {
            throw new Exception("不在选课时间段内，无法选课");
        }

        // 2. 检查是否已选该课程
        if (isEnrolled(studentId, courseId)) {
            throw new Exception("已选该课程，不能重复选择");
        }

        // 3. 检查课程是否已满
        if (isCourseFull(courseId)) {
            throw new Exception("课程人数已满，无法选择");
        }

        // 4. 检查时间是否冲突
        if (isScheduleConflict(studentId, courseId)) {
            throw new Exception("课程时间冲突，无法选择");
        }

        // 5. 检查学分是否超限（假设最多不超过30学分）
        BigDecimal totalCredits = getTotalCreditsByStudent(studentId);
        Course course = courseMapper.selectById(courseId);
        if (course != null) {
            BigDecimal courseCredit = course.getCredit() == null ? BigDecimal.ZERO : course.getCredit();
            if (totalCredits.add(courseCredit).compareTo(BigDecimal.valueOf(30)) > 0) {
                throw new Exception("选课总学分超过限制（30学分），无法选择");
            }
        }

        // 创建选课记录
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollment.setEnrollmentTime(LocalDateTime.now());

        // 保存选课记录
        return save(enrollment);
    }

    @Override
    public boolean dropCourse(String studentId, String courseId) {
        QueryWrapper<Enrollment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId)
                .eq("course_id", courseId);
        return remove(queryWrapper);
    }

    @Override
    public int getEnrollmentCountByStudent(String studentId) {
        QueryWrapper<Enrollment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        return Math.toIntExact(count(queryWrapper));
    }

    @Override
    public int getEnrollmentCountByCourse(String courseId) {
        QueryWrapper<Enrollment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        return Math.toIntExact(count(queryWrapper));
    }

    @Override
    public boolean isInEnrollmentPeriod() {
        // 查询当前有效的选课时间段
        QueryWrapper<Enrollmentperiod> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", true);
        List<Enrollmentperiod> periods = enrollmentperiodService.list(queryWrapper);

        LocalDateTime now = LocalDateTime.now();
        for (Enrollmentperiod period : periods) {
            if (now.isAfter(period.getStartTime()) && now.isBefore(period.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isScheduleConflict(String studentId, String courseId) {
        // 获取学生已选课程的所有时间安排
        List<Enrollment> enrollments = findByStudentId(studentId);
        for (Enrollment enrollment : enrollments) {
            // 获取已选课程的时间安排
            List<Schedule> existingSchedules = getSchedulesByCourseId(enrollment.getCourseId());
            // 获取新课程的时间安排
            List<Schedule> newSchedules = getSchedulesByCourseId(courseId);

            // 检查时间冲突
            for (Schedule existing : existingSchedules) {
                for (Schedule newSchedule : newSchedules) {
                    if (isTimeConflict(existing, newSchedule)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isCourseFull(String courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course != null) {
            int enrolledCount = getEnrollmentCountByCourse(courseId);
            return enrolledCount >= course.getMaxStudents();
        }
        return true;
    }

    @Override
    public BigDecimal getTotalCreditsByStudent(String studentId) {
        List<Enrollment> enrollments = findByStudentId(studentId);
        BigDecimal totalCredits = BigDecimal.ZERO;
        for (Enrollment enrollment : enrollments) {
            Course course = courseMapper.selectById(enrollment.getCourseId());
            if (course != null) {
                totalCredits = totalCredits.add(course.getCredit() == null ? BigDecimal.ZERO : course.getCredit());
            }
        }
        return totalCredits;
    }

    // 辅助方法：获取课程的时间安排
    private List<Schedule> getSchedulesByCourseId(String courseId) {
        QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        return scheduleMapper.selectList(queryWrapper);
    }

    // 辅助方法：判断两个时间安排是否冲突
    private boolean isTimeConflict(Schedule schedule1, Schedule schedule2) {
        // 判断星期是否相同
        if (!schedule1.getWeekDay().equals(schedule2.getWeekDay())) {
            return false;
        }

        // 判断时间是否重叠
        return !(schedule1.getEndTime().isBefore(schedule2.getStartTime()) ||
                schedule1.getStartTime().isAfter(schedule2.getEndTime()));
    }

    @Autowired
    private IEnrollmentperiodService enrollmentperiodService;
}
