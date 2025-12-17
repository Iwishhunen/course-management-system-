package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.Course;
import com.example.backend.entity.Enrollment;
import com.example.backend.entity.Schedule;
import com.example.backend.entity.Student;
import com.example.backend.entity.Teacher;
import com.example.backend.mapper.StudentMapper;
import com.example.backend.service.ICourseService;
import com.example.backend.service.IEnrollmentService;
import com.example.backend.service.IScheduleService;
import com.example.backend.service.IStudentService;
import com.example.backend.service.ITeacherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 学生基本信息表 服务实现类
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {

    @Autowired
    private IEnrollmentService enrollmentService;

    @Autowired
    private IScheduleService scheduleService;

    @Autowired
    @Lazy
    private ICourseService courseService;

    @Autowired
    private ITeacherService teacherService;

    @Override
    public Student findByStudentId(String studentId) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        return getOne(queryWrapper);
    }

    @Override
    public Student validateLogin(String studentId, String password) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId)
                .eq("password", password);
        return getOne(queryWrapper);
    }

    @Override
    public boolean changePassword(String studentId, String oldPassword, String newPassword) {
        Student student = findByStudentId(studentId);
        if (student != null && student.getPassword().equals(oldPassword)) {
            student.setPassword(newPassword);
            return updateById(student);
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> getStudentSchedule(String studentId) {
        List<Map<String, Object>> scheduleList = new ArrayList<>();

        // 获取学生的所有选课记录
        List<Enrollment> enrollments = enrollmentService.findByStudentId(studentId);

        // 遍历选课记录，获取课程和上课时间安排
        for (Enrollment enrollment : enrollments) {
            String courseId = enrollment.getCourseId();

            // 获取课程信息
            Course course = courseService.findByCourseId(courseId);
            if (course == null)
                continue;

            // 获取该课程的所有时间安排
            QueryWrapper<Schedule> scheduleQueryWrapper = new QueryWrapper<>();
            scheduleQueryWrapper.eq("course_id", courseId);
            scheduleQueryWrapper.orderByAsc("week_day").orderByAsc("start_time");
            List<Schedule> schedules = scheduleService.list(scheduleQueryWrapper);

            // 构造课表信息
            for (Schedule schedule : schedules) {
                Map<String, Object> scheduleInfo = new HashMap<>();
                scheduleInfo.put("courseId", courseId);
                scheduleInfo.put("courseName", course.getCourseName());
                scheduleInfo.put("weekDay", schedule.getWeekDay());
                scheduleInfo.put("weekDayChinese",
                        com.example.backend.tools.WeekDayUtil.getWeekDayChinese(schedule.getWeekDay()));
                scheduleInfo.put("startTime", schedule.getStartTime());
                scheduleInfo.put("endTime", schedule.getEndTime());
                Integer startWeek = schedule.getStartWeek() != null ? schedule.getStartWeek() : 1;
                Integer endWeek = schedule.getEndWeek() != null ? schedule.getEndWeek() : 16;
                scheduleInfo.put("startWeek", startWeek);
                scheduleInfo.put("endWeek", endWeek);
                scheduleInfo.put("location", schedule.getLocation());
                scheduleInfo.put("credit", course.getCredit());

                Teacher teacher = teacherService.findByTeacherId(course.getTeacherId());
                scheduleInfo.put("teacherName", teacher != null ? teacher.getName() : "未知教师");

                scheduleList.add(scheduleInfo);
            }
        }

        scheduleList.sort((a, b) -> {
            Integer w1 = (Integer) a.getOrDefault("weekDay", 0);
            Integer w2 = (Integer) b.getOrDefault("weekDay", 0);
            int cmp = Integer.compare(w1, w2);
            if (cmp != 0) {
                return cmp;
            }
            Object s1 = a.get("startTime");
            Object s2 = b.get("startTime");
            return Objects.toString(s1, "").compareTo(Objects.toString(s2, ""));
        });

        return scheduleList;
    }
}