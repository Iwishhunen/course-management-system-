package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Course;
import com.example.backend.entity.CourseCategories;
import com.example.backend.entity.Enrollment;
import com.example.backend.entity.Enrollmentperiod;
import com.example.backend.entity.Notice;
import com.example.backend.entity.Schedule;
import com.example.backend.entity.Student;
import com.example.backend.entity.Teacher;
import com.example.backend.mapper.CourseMapper;
import com.example.backend.mapper.ScheduleMapper;
import com.example.backend.service.ICourseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.service.ICourseCategoriesService;
import com.example.backend.service.IEmailService;
import com.example.backend.service.IEnrollmentService;
import com.example.backend.service.IEnrollmentperiodService;
import com.example.backend.service.INoticeService;
import com.example.backend.service.IStudentService;
import com.example.backend.service.ITeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程基本信息表 服务实现类
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    @Autowired
    private IEnrollmentService enrollmentService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private IEnrollmentperiodService enrollmentperiodService;

    @Autowired
    @Lazy
    private IStudentService studentService;

    @Autowired
    private ITeacherService teacherService;

    @Autowired
    private ICourseCategoriesService courseCategoriesService;

    @Autowired
    private IEmailService emailService;

    @Autowired
    private INoticeService noticeService;

    private boolean noticeExists(String targetRole, String targetUserId, String content) {
        QueryWrapper<Notice> qw = new QueryWrapper<>();
        qw.eq("target_role", targetRole)
                .eq("target_user_id", targetUserId)
                .eq("content", content);
        return noticeService.count(qw) > 0;
    }

    @Override
    public Course findByCourseId(String courseId) {
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        return getOne(queryWrapper);
    }

    @Override
    public List<Course> findByTeacherId(String teacherId) {
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacher_id", teacherId);
        return list(queryWrapper);
    }

    @Override
    public List<Course> findFullCourses() {
        return list().stream()
                .filter(course -> enrollmentService.getEnrollmentCountByCourse(course.getCourseId()) >= course
                        .getMaxStudents())
                .collect(Collectors.toList());
    }

    @Override
    public List<Course> findLowEnrollmentCourses() {
        return list().stream()
                .filter(course -> enrollmentService.getEnrollmentCountByCourse(course.getCourseId()) < 20)
                .collect(Collectors.toList());
    }

    @Override
    public List<Course> cancelLowEnrollmentCourses() {
        // 检查是否还有活跃的选课时间段
        if (isEnrollmentPeriodActive()) {
            throw new RuntimeException("选课尚未结束，无法执行课程取消操作");
        }

        List<Course> lowEnrollmentCourses = findLowEnrollmentCourses();
        for (Course course : lowEnrollmentCourses) {
            // 发送取消通知
            sendCancellationNotification(course);

            // 这里可以根据实际需求决定是否真的删除选课记录或者只是标记课程为已取消
        }
        return lowEnrollmentCourses;
    }

    @Override
    public void sendCancellationNotification(Course course) {
        // 获取选课该课程的所有学生
        List<Enrollment> enrollments = enrollmentService.findByCourseId(course.getCourseId());

        // 获取教师信息
        Teacher teacher = teacherService.findByTeacherId(course.getTeacherId());
        String teacherName = (teacher != null) ? teacher.getName() : "未知教师";

        // 发送邮件通知教师
        if (teacher != null && teacher.getEmail() != null && !teacher.getEmail().isEmpty()) {
            emailService.sendCourseCancellationNotificationToTeacher(teacher.getEmail(), course.getCourseName());
        }

        // 站内通知：定向通知教师
        if (teacher != null) {
            String content = "课程《" + course.getCourseName() + "》因选课人数不足20人已取消。";
            Notice n = new Notice();
            if (!noticeExists("teacher", teacher.getTeacherId(), content)) {
                n.setContent(content);
                n.setTargetRole("teacher");
                n.setTargetUserId(teacher.getTeacherId());
                n.setPublisherId(null);
                n.setPublishTime(LocalDateTime.now());
                noticeService.save(n);
            }
        }

        // 发送邮件通知每个学生
        for (Enrollment enrollment : enrollments) {
            Student student = studentService.findByStudentId(enrollment.getStudentId());
            if (student != null && student.getEmail() != null && !student.getEmail().isEmpty()) {
                emailService.sendCourseCancellationNotificationToStudent(
                        student.getEmail(),
                        course.getCourseName(),
                        teacherName);
            }

            // 站内通知：定向通知学生
            if (student != null) {
                String content = "课程《" + course.getCourseName() + "》因选课人数不足20人已取消。授课教师：" + teacherName;
                Notice n = new Notice();
                if (!noticeExists("student", student.getStudentId(), content)) {
                    n.setContent(content);
                    n.setTargetRole("student");
                    n.setTargetUserId(student.getStudentId());
                    n.setPublisherId(null);
                    n.setPublishTime(LocalDateTime.now());
                    noticeService.save(n);
                }
            }
        }
    }

    @Override
    public void scheduledCancelLowEnrollmentCourses() {
        // 只有在选课结束后才执行
        if (!isEnrollmentPeriodActive()) {
            List<Course> cancelledCourses = cancelLowEnrollmentCourses();
            if (!cancelledCourses.isEmpty()) {
                System.out.println("定时任务: 成功取消" + cancelledCourses.size() + "门选课人数不足的课程");
            }
        }
    }

    /**
     * 检查是否有活跃的选课时间段
     * 
     * @return 是否有活跃的选课时间段
     */
    private boolean isEnrollmentPeriodActive() {
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
    public Page<Map<String, Object>> getCourseListWithEnrollmentStatus(
            String studentId,
            Integer pageNum,
            Integer pageSize,
            String courseName,
            String teacherName,
            String courseType) {

        // 构建查询条件
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();

        // 添加过滤条件
        if (courseName != null && !courseName.trim().isEmpty()) {
            queryWrapper.like("course_name", courseName);
        }
        if (teacherName != null && !teacherName.trim().isEmpty()) {
            QueryWrapper<Teacher> teacherQueryWrapper = new QueryWrapper<>();
            teacherQueryWrapper.like("name", teacherName);
            List<Teacher> matchedTeachers = teacherService.list(teacherQueryWrapper);
            if (matchedTeachers.isEmpty()) {
                return new Page<>(pageNum, pageSize, 0);
            }
            Set<String> teacherIds = matchedTeachers.stream().map(Teacher::getTeacherId).collect(Collectors.toSet());
            queryWrapper.in("teacher_id", teacherIds);
        }
        if (courseType != null && !courseType.trim().isEmpty()) {
            queryWrapper.eq("type_code", courseType);
        }

        // 排序
        queryWrapper.orderByDesc("course_id");

        // 分页查询课程
        Page<Course> coursePage = new Page<>(pageNum, pageSize);
        Page<Course> pageResult = page(coursePage, queryWrapper);

        Map<String, CourseCategories> categoryMap = courseCategoriesService.list().stream()
                .collect(Collectors.toMap(CourseCategories::getTypeCode, Function.identity(), (a, b) -> a));

        // 构建结果
        Page<Map<String, Object>> resultMapPage = new Page<>(pageNum, pageSize, pageResult.getTotal());
        List<Map<String, Object>> records = pageResult.getRecords().stream().map(course -> {
            Map<String, Object> map = new HashMap<>();

            // 课程基本信息
            map.put("courseId", course.getCourseId());
            map.put("courseName", course.getCourseName());
            map.put("credit", course.getCredit());
            map.put("maxStudents", course.getMaxStudents());
            map.put("teacherId", course.getTeacherId());
            map.put("courseType", course.getTypeCode());

            CourseCategories categories = categoryMap.get(course.getTypeCode());
            map.put("courseTypeName", categories != null ? categories.getTypeName() : "未知");

            // 获取教师姓名
            Teacher teacher = teacherService.findByTeacherId(course.getTeacherId());
            map.put("teacherName", teacher != null ? teacher.getName() : "未知教师");

            // 获取选课统计信息
            int currentEnrollment = enrollmentService.getEnrollmentCountByCourse(course.getCourseId());
            map.put("currentEnrollment", currentEnrollment);
            map.put("availableSeats", course.getMaxStudents() - currentEnrollment);

            List<Schedule> schedules = scheduleMapper.selectList(new QueryWrapper<Schedule>()
                    .eq("course_id", course.getCourseId())
                    .orderByAsc("week_day").orderByAsc("start_time"));
            String scheduleInfo = (schedules == null || schedules.isEmpty()) ? ""
                    : schedules.stream()
                            .map(s -> {
                                String w = com.example.backend.tools.WeekDayUtil.getWeekDayChinese(s.getWeekDay());
                                String start = s.getStartTime() != null ? s.getStartTime().toString() : "";
                                String end = s.getEndTime() != null ? s.getEndTime().toString() : "";
                                String loc = s.getLocation() != null ? s.getLocation() : "";
                                return "周" + w + " " + start + "-" + end + " @" + loc;
                            })
                            .collect(Collectors.joining("; "));
            map.put("scheduleInfo", scheduleInfo);

            // 检查学生是否已选该课程
            boolean isSelected = enrollmentService.isEnrolled(studentId, course.getCourseId());
            map.put("selected", isSelected ? 1 : 0);

            return map;
        }).collect(Collectors.toList());

        resultMapPage.setRecords(records);
        return resultMapPage;
    }
}