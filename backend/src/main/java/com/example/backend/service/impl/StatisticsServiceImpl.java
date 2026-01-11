package com.example.backend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.Course;
import com.example.backend.entity.Enrollment;
import com.example.backend.entity.Student;
import com.example.backend.entity.Teacher;
import com.example.backend.mapper.CourseCategoriesMapper;
import com.example.backend.mapper.CourseMapper;
import com.example.backend.mapper.EnrollmentMapper;
import com.example.backend.mapper.ScheduleMapper;
import com.example.backend.mapper.StatisticsMapper;
import com.example.backend.mapper.StudentMapper;
import com.example.backend.mapper.TeacherMapper;
import com.example.backend.service.IStatisticsService;

/**
 * 统计服务实现类
 */
@Service
public class StatisticsServiceImpl implements IStatisticsService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private CourseCategoriesMapper courseCategoriesMapper;

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Override
    public List<Map<String, Object>> getEnrollmentStatsByCourse() {
        // 使用数据库视图提高查询效率
        return statisticsMapper.selectEnrollmentStats();
    }

    /*
     * @Override
     * public List<Map<String, Object>> getEnrollmentStatsByCourse() {
     * List<Map<String, Object>> result = new ArrayList<>();
     * 
     * // 查询所有课程
     * List<Course> courses = courseMapper.selectList(null);
     * for (Course course : courses) {
     * Map<String, Object> stats = new HashMap<>();
     * 
     * // 获取教师信息
     * Teacher teacher = teacherMapper.selectById(course.getTeacherId());
     * 
     * // 统计选课人数
     * QueryWrapper<Enrollment> enrollmentQueryWrapper = new QueryWrapper<>();
     * enrollmentQueryWrapper.eq("course_id", course.getCourseId());
     * int enrolledCount =
     * Math.toIntExact(enrollmentMapper.selectCount(enrollmentQueryWrapper));
     * 
     * // 计算选课率
     * int maxStudents = course.getMaxStudents();
     * double enrollmentRate = maxStudents > 0 ? (double) enrolledCount /
     * maxStudents * 100 : 0;
     * 
     * // 判断状态
     * String status = enrolledCount < 20 ? "人数不足" : "正常";
     * 
     * stats.put("courseId", course.getCourseId());
     * stats.put("courseName", course.getCourseName());
     * stats.put("teacherName", teacher != null ? teacher.getName() : "未知教师");
     * stats.put("credit", course.getCredit());
     * stats.put("maxStudents", maxStudents);
     * stats.put("enrolledCount", enrolledCount);
     * stats.put("enrollmentRate", String.format("%.2f%%", enrollmentRate));
     * stats.put("status", status);
     * 
     * result.add(stats);
     * }
     * 
     * // 按选课率降序排序
     * result.sort((a, b) -> {
     * double rateA =
     * Double.parseDouble(a.get("enrollmentRate").toString().replace("%", ""));
     * double rateB =
     * Double.parseDouble(b.get("enrollmentRate").toString().replace("%", ""));
     * return Double.compare(rateB, rateA);
     * });
     * 
     * return result;
     * }
     */

    @Override
    public List<Map<String, Object>> getEnrollmentStatsByClass() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查询所有班级
        QueryWrapper<Student> classQueryWrapper = new QueryWrapper<>();
        classQueryWrapper.select("classID").groupBy("classID");
        List<Map<String, Object>> classes = studentMapper.selectMaps(classQueryWrapper);

        for (Map<String, Object> classInfo : classes) {
            String className = (String) classInfo.get("classID");
            if (className == null || className.isEmpty()) {
                continue;
            }

            Map<String, Object> stats = new HashMap<>();

            // 查询该班级的学生数量
            QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
            studentQueryWrapper.eq("classID", className);
            int studentCount = Math.toIntExact(studentMapper.selectCount(studentQueryWrapper));

            // 查询该班级学生的选课总数
            // 先查询该班级所有学生
            List<Student> students = studentMapper.selectList(studentQueryWrapper);
            int totalEnrollments = 0;
            BigDecimal totalCredits = BigDecimal.ZERO;

            for (Student student : students) {
                // 查询该学生的选课记录
                QueryWrapper<Enrollment> enrollmentQueryWrapper = new QueryWrapper<>();
                enrollmentQueryWrapper.eq("student_id", student.getStudentId());
                List<Enrollment> enrollments = enrollmentMapper.selectList(enrollmentQueryWrapper);
                totalEnrollments += enrollments.size();

                // 计算总学分
                for (Enrollment enrollment : enrollments) {
                    Course course = courseMapper.selectById(enrollment.getCourseId());
                    if (course != null) {
                        totalCredits = totalCredits
                                .add(course.getCredit() == null ? BigDecimal.ZERO : course.getCredit());
                    }
                }
            }

            // 平均每人选课数
            double avgEnrollments = studentCount > 0 ? (double) totalEnrollments / studentCount : 0;

            // 平均每人学分
            BigDecimal avgCredits = studentCount > 0
                    ? totalCredits.divide(BigDecimal.valueOf(studentCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            stats.put("className", className);
            stats.put("studentCount", studentCount);
            stats.put("totalEnrollments", totalEnrollments);
            stats.put("totalCredits", totalCredits);
            stats.put("avgEnrollments", String.format("%.2f", avgEnrollments));
            stats.put("avgCredits", avgCredits.toPlainString());

            result.add(stats);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getCourseTypeStats() {
        // 使用数据库视图提高查询效率
        return statisticsMapper.selectCourseTypeStats();
    }

    /*
     * @Override
     * public List<Map<String, Object>> getCourseTypeStats() {
     * List<Map<String, Object>> result = new ArrayList<>();
     * 
     * // 查询所有课程类型
     * List<CourseCategories> categories = courseCategoriesMapper.selectList(null);
     * for (CourseCategories category : categories) {
     * Map<String, Object> stats = new HashMap<>();
     * 
     * // 统计该类型课程数量
     * QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
     * courseQueryWrapper.eq("type_code", category.getTypeCode());
     * int courseCount =
     * Math.toIntExact(courseMapper.selectCount(courseQueryWrapper));
     * 
     * // 统计该类型课程总学分
     * List<Course> courses = courseMapper.selectList(courseQueryWrapper);
     * int totalCredits = 0;
     * int totalEnrollments = 0;
     * double totalEnrollmentRate = 0;
     * int rateCount = 0;
     * 
     * for (Course course : courses) {
     * totalCredits += course.getCredit();
     * 
     * // 统计选课人数
     * QueryWrapper<Enrollment> enrollmentQueryWrapper = new QueryWrapper<>();
     * enrollmentQueryWrapper.eq("course_id", course.getCourseId());
     * int enrolledCount =
     * Math.toIntExact(enrollmentMapper.selectCount(enrollmentQueryWrapper));
     * 
     * // 计算选课率
     * int maxStudents = course.getMaxStudents();
     * if (maxStudents > 0) {
     * totalEnrollmentRate += (double) enrolledCount / maxStudents * 100;
     * rateCount++;
     * }
     * 
     * totalEnrollments += enrolledCount;
     * }
     * 
     * // 平均选课率
     * double avgEnrollmentRate = rateCount > 0 ? totalEnrollmentRate / rateCount :
     * 0;
     * 
     * stats.put("typeCode", category.getTypeCode());
     * stats.put("typeName", category.getTypeName());
     * stats.put("creditRequirement", category.getCreditRequirement());
     * stats.put("courseCount", courseCount);
     * stats.put("totalCredits", totalCredits);
     * stats.put("totalEnrollments", totalEnrollments);
     * stats.put("avgEnrollmentRate", String.format("%.2f%%", avgEnrollmentRate));
     * 
     * result.add(stats);
     * }
     * 
     * return result;
     * }
     */

    @Override
    public List<Map<String, Object>> getStudentEnrollmentStats() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查询所有学生
        List<Student> students = studentMapper.selectList(null);
        for (Student student : students) {
            Map<String, Object> stats = new HashMap<>();

            // 查询该学生的选课记录
            QueryWrapper<Enrollment> enrollmentQueryWrapper = new QueryWrapper<>();
            enrollmentQueryWrapper.eq("student_id", student.getStudentId());
            List<Enrollment> enrollments = enrollmentMapper.selectList(enrollmentQueryWrapper);

            // 统计选课门数和总学分
            int courseCount = enrollments.size();
            BigDecimal totalCredits = BigDecimal.ZERO;

            for (Enrollment enrollment : enrollments) {
                Course course = courseMapper.selectById(enrollment.getCourseId());
                if (course != null) {
                    totalCredits = totalCredits.add(course.getCredit() == null ? BigDecimal.ZERO : course.getCredit());
                }
            }

            stats.put("studentId", student.getStudentId());
            stats.put("studentName", student.getName());
            stats.put("major", student.getMajor());
            stats.put("class", student.getClassId());
            stats.put("courseCount", courseCount);
            stats.put("totalCredits", totalCredits);

            result.add(stats);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getTeacherCourseStats() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查询所有教师
        List<Teacher> teachers = teacherMapper.selectList(null);
        for (Teacher teacher : teachers) {
            Map<String, Object> stats = new HashMap<>();

            // 查询该教师开设的课程
            QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
            courseQueryWrapper.eq("teacher_id", teacher.getTeacherId());
            List<Course> courses = courseMapper.selectList(courseQueryWrapper);

            // 统计课程门数和总学分
            int courseCount = courses.size();
            BigDecimal totalCredits = BigDecimal.ZERO;
            int totalMaxStudents = 0;
            int totalEnrolledStudents = 0;

            for (Course course : courses) {
                totalCredits = totalCredits.add(course.getCredit() == null ? BigDecimal.ZERO : course.getCredit());
                totalMaxStudents += course.getMaxStudents();

                // 统计该课程的选课人数
                QueryWrapper<Enrollment> enrollmentQueryWrapper = new QueryWrapper<>();
                enrollmentQueryWrapper.eq("course_id", course.getCourseId());
                int enrolledCount = Math.toIntExact(enrollmentMapper.selectCount(enrollmentQueryWrapper));
                totalEnrolledStudents += enrolledCount;
            }

            stats.put("teacherId", teacher.getTeacherId());
            stats.put("teacherName", teacher.getName());
            stats.put("title", teacher.getTitle());
            stats.put("courseCount", courseCount);
            stats.put("totalCredits", totalCredits);
            stats.put("totalMaxStudents", totalMaxStudents);
            stats.put("totalEnrolledStudents", totalEnrolledStudents);

            result.add(stats);
        }

        return result;
    }
}