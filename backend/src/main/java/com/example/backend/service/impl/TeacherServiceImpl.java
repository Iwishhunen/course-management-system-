package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.Course;
import com.example.backend.entity.Enrollment;
import com.example.backend.entity.Schedule;
import com.example.backend.entity.Student;
import com.example.backend.entity.Teacher;
import com.example.backend.mapper.TeacherMapper;
import com.example.backend.service.ICourseService;
import com.example.backend.service.IEnrollmentService;
import com.example.backend.service.IScheduleService;
import com.example.backend.service.IStudentService;
import com.example.backend.service.ITeacherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 教师基本信息表 服务实现类
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements ITeacherService {

    @Autowired
    private ICourseService courseService;

    @Autowired
    private IEnrollmentService enrollmentService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IScheduleService scheduleService;

    @Override
    public Teacher findByTeacherId(String teacherId) {
        QueryWrapper<Teacher> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacher_id", teacherId);
        return getOne(queryWrapper);
    }

    @Override
    public Teacher validateLogin(String teacherId, String password) {
        QueryWrapper<Teacher> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacher_id", teacherId)
                .eq("password", password);
        return getOne(queryWrapper);
    }

    @Override
    public boolean changePassword(String teacherId, String oldPassword, String newPassword) {
        Teacher teacher = findByTeacherId(teacherId);
        if (teacher != null && teacher.getPassword().equals(oldPassword)) {
            teacher.setPassword(newPassword);
            return updateById(teacher);
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> getTeacherCourses(String teacherId) {
        List<Map<String, Object>> courseList = new ArrayList<>();

        // 获取教师教授的所有课程
        QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
        courseQueryWrapper.eq("teacher_id", teacherId);
        List<Course> courses = courseService.list(courseQueryWrapper);

        // 遍历课程，获取选课人数等信息
        for (Course course : courses) {
            Map<String, Object> courseInfo = new HashMap<>();
            courseInfo.put("courseId", course.getCourseId());
            courseInfo.put("courseName", course.getCourseName());
            courseInfo.put("credit", course.getCredit());
            courseInfo.put("maxStudents", course.getMaxStudents());

            // 获取选课人数
            int enrollmentCount = enrollmentService.getEnrollmentCountByCourse(course.getCourseId());
            courseInfo.put("currentEnrollment", enrollmentCount);
            courseInfo.put("studentCount", enrollmentCount);
            courseInfo.put("availableSeats", course.getMaxStudents() - enrollmentCount);

            QueryWrapper<Schedule> scheduleQueryWrapper = new QueryWrapper<>();
            scheduleQueryWrapper.eq("course_id", course.getCourseId());
            scheduleQueryWrapper.orderByAsc("week_day").orderByAsc("start_time");
            List<Schedule> schedules = scheduleService.list(scheduleQueryWrapper);
            boolean scheduled = schedules != null && !schedules.isEmpty();
            courseInfo.put("scheduled", scheduled);

            if (schedules != null && !schedules.isEmpty()) {
                Schedule schedule = schedules.get(0);
                Map<String, Object> scheduleInfo = new HashMap<>();
                scheduleInfo.put("scheduleId", schedule.getScheduleId());
                scheduleInfo.put("weekday", schedule.getWeekDay());
                scheduleInfo.put("location", schedule.getLocation());
                scheduleInfo.put("startTime", toPeriodByStartTime(
                        schedule.getStartTime() != null ? schedule.getStartTime().toString() : null));
                scheduleInfo.put("endTime",
                        toPeriodByEndTime(schedule.getEndTime() != null ? schedule.getEndTime().toString() : null));
                Integer startWeek = schedule.getStartWeek() != null ? schedule.getStartWeek() : 1;
                Integer endWeek = schedule.getEndWeek() != null ? schedule.getEndWeek() : 16;
                scheduleInfo.put("startWeek", startWeek);
                scheduleInfo.put("endWeek", endWeek);
                courseInfo.put("scheduleInfo", scheduleInfo);
            }

            courseList.add(courseInfo);
        }

        return courseList;
    }

    @Override
    public List<Map<String, Object>> getTeacherSchedule(String teacherId) {
        List<Map<String, Object>> scheduleList = new ArrayList<>();

        QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
        courseQueryWrapper.eq("teacher_id", teacherId);
        List<Course> courses = courseService.list(courseQueryWrapper);

        Teacher teacher = findByTeacherId(teacherId);
        String teacherName = teacher != null ? teacher.getName() : "未知教师";

        for (Course course : courses) {
            QueryWrapper<Schedule> scheduleQueryWrapper = new QueryWrapper<>();
            scheduleQueryWrapper.eq("course_id", course.getCourseId());
            scheduleQueryWrapper.orderByAsc("week_day").orderByAsc("start_time");
            List<Schedule> schedules = scheduleService.list(scheduleQueryWrapper);

            if (schedules == null || schedules.isEmpty()) {
                continue;
            }

            int studentCount = enrollmentService.getEnrollmentCountByCourse(course.getCourseId());

            for (Schedule schedule : schedules) {
                Map<String, Object> scheduleInfo = new HashMap<>();
                scheduleInfo.put("scheduleId", schedule.getScheduleId());
                scheduleInfo.put("courseId", course.getCourseId());
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
                scheduleInfo.put("teacherName", teacherName);
                scheduleInfo.put("studentCount", studentCount);

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

    private Integer toPeriodByStartTime(String startTime) {
        if (startTime == null) {
            return null;
        }
        return switch (startTime) {
            case "08:00", "08:00:00" -> 1;
            case "08:50", "08:50:00" -> 2;
            case "09:55", "09:55:00" -> 3;
            case "10:45", "10:45:00" -> 4;
            case "11:35", "11:35:00" -> 5;
            case "14:00", "14:00:00" -> 6;
            case "14:50", "14:50:00" -> 7;
            case "15:40", "15:40:00" -> 8;
            case "16:45", "16:45:00" -> 9;
            case "17:35", "17:35:00" -> 10;
            case "09:50", "09:50:00" -> 3;
            case "11:40", "11:40:00" -> 5;
            case "13:30", "13:30:00" -> 6;
            case "15:20", "15:20:00" -> 8;
            case "17:10", "17:10:00" -> 10;
            case "19:00", "19:00:00" -> 10;
            default -> null;
        };
    }

    private Integer toPeriodByEndTime(String endTime) {
        if (endTime == null) {
            return null;
        }
        return switch (endTime) {
            case "08:45", "08:45:00" -> 1;
            case "09:35", "09:35:00" -> 2;
            case "10:40", "10:40:00" -> 3;
            case "11:30", "11:30:00" -> 4;
            case "12:20", "12:20:00" -> 5;
            case "14:45", "14:45:00" -> 6;
            case "15:35", "15:35:00" -> 7;
            case "16:25", "16:25:00" -> 8;
            case "17:30", "17:30:00" -> 9;
            case "18:20", "18:20:00" -> 10;
            case "10:25", "10:25:00" -> 3;
            case "12:15", "12:15:00" -> 5;
            case "15:55", "15:55:00" -> 8;
            case "19:35", "19:35:00" -> 10;
            case "11:25", "11:25:00" -> 4;
            case "13:15", "13:15:00" -> 5;
            case "15:05", "15:05:00" -> 7;
            case "16:55", "16:55:00" -> 9;
            case "18:45", "18:45:00" -> 10;
            case "20:35", "20:35:00" -> 10;
            default -> null;
        };
    }

    @Override
    public List<Map<String, Object>> getCourseStudents(String courseId) {
        List<Map<String, Object>> studentList = new ArrayList<>();

        // 获取选课该课程的所有学生ID
        List<Enrollment> enrollments = enrollmentService.findByCourseId(courseId);

        // 遍历选课记录，获取学生详细信息
        for (Enrollment enrollment : enrollments) {
            String studentId = enrollment.getStudentId();

            // 获取学生信息
            Student student = studentService.findByStudentId(studentId);
            if (student != null) {
                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("studentId", studentId);
                studentInfo.put("studentName", student.getName());
                studentInfo.put("major", student.getMajor());
                studentInfo.put("class", student.getClassId());
                studentInfo.put("enrollmentTime", enrollment.getEnrollmentTime());
                studentInfo.put("score", enrollment.getScore());

                studentList.add(studentInfo);
            }
        }

        return studentList;
    }
}
