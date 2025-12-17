package com.example.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.config.RoleRequire;
import com.example.backend.entity.Course;
import com.example.backend.entity.Enrollment;
import com.example.backend.entity.Teacher;
import com.example.backend.service.ICourseService;
import com.example.backend.service.IEnrollmentService;
import com.example.backend.service.ITeacherService;

import common.Result;

import java.io.ByteArrayOutputStream;

/**
 * <p>
 * 教师基本信息表 前端控制器
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private ITeacherService teacherService;

    @Autowired
    private ICourseService courseService;

    @Autowired
    private IEnrollmentService enrollmentService;

    public static class UpdateCourseCapacityRequest {
        private String teacherId;
        private String courseId;
        private Integer maxStudents;

        public String getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(String teacherId) {
            this.teacherId = teacherId;
        }

        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        public Integer getMaxStudents() {
            return maxStudents;
        }

        public void setMaxStudents(Integer maxStudents) {
            this.maxStudents = maxStudents;
        }
    }

    public static class UpdateStudentScoreRequest {
        private String teacherId;
        private String courseId;
        private String studentId;
        private java.math.BigDecimal score;

        public String getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(String teacherId) {
            this.teacherId = teacherId;
        }

        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public java.math.BigDecimal getScore() {
            return score;
        }

        public void setScore(java.math.BigDecimal score) {
            this.score = score;
        }
    }

    /**
     * 新增教师
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody Teacher teacher) {
        try {
            boolean saved = teacherService.save(teacher);
            if (saved) {
                return Result.success(true, "添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID删除教师
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        try {
            boolean removed = teacherService.removeById(id);
            if (removed) {
                return Result.success(true, "删除成功");
            } else {
                return Result.error("删除失败，教师不存在");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除教师
     */
    @DeleteMapping("/delete/batch")
    public Result<Boolean> deleteBatch(@RequestBody List<String> ids) {
        try {
            boolean removed = teacherService.removeByIds(ids);
            if (removed) {
                return Result.success(true, "批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 修改教师信息
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Teacher teacher) {
        try {
            if (teacher.getTeacherId() == null) {
                return Result.error("ID不能为空");
            }
            boolean updated = teacherService.updateById(teacher);
            if (updated) {
                return Result.success(true, "修改成功");
            } else {
                return Result.error("修改失败，教师不存在");
            }
        } catch (Exception e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询教师
     */
    @GetMapping("/get/{id}")
    public Result<Teacher> getById(@PathVariable String id) {
        try {
            Teacher teacher = teacherService.getById(id);
            if (teacher != null) {
                return Result.success(teacher, "查询成功");
            } else {
                return Result.error("教师不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有教师
     */
    @GetMapping("/list")
    public Result<List<Teacher>> list() {
        try {
            List<Teacher> list = teacherService.list();
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询教师
     */
    @GetMapping("/page")
    public Result<Page<Teacher>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String teacherId,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String title) {
        try {
            Page<Teacher> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Teacher> queryWrapper = new QueryWrapper<>();

            if (teacherId != null && !teacherId.trim().isEmpty()) {
                queryWrapper.eq("teacher_id", teacherId);
            }

            // 模糊查询条件
            if (teacherName != null && !teacherName.trim().isEmpty()) {
                queryWrapper.like("name", teacherName);
            }
            if (title != null && !title.trim().isEmpty()) {
                queryWrapper.like("title", title);
            }

            // 排序
            queryWrapper.orderByDesc("teacher_id");

            Page<Teacher> result = teacherService.page(page, queryWrapper);
            return Result.success(result, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 条件查询（模糊或精确匹配）
     */
    @GetMapping("/query")
    public Result<List<Teacher>> query(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String value,
            @RequestParam(defaultValue = "false") Boolean exactMatch) {
        try {
            QueryWrapper<Teacher> queryWrapper = new QueryWrapper<>();

            if (key != null && value != null && !value.trim().isEmpty()) {
                if (exactMatch) {
                    // 精确匹配
                    queryWrapper.eq(key, value);
                } else {
                    // 模糊查询
                    queryWrapper.like(key, value);
                }
            }

            List<Teacher> list = teacherService.list(queryWrapper);
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取教师教授的课程列表
     */
    @GetMapping("/courses/{teacherId}")
    public Result<List<Map<String, Object>>> getTeacherCourses(@PathVariable String teacherId) {
        try {
            List<Map<String, Object>> courses = teacherService.getTeacherCourses(teacherId);
            return Result.success(courses, "课程列表查询成功");
        } catch (Exception e) {
            return Result.error("课程列表查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取课程的选课学生名单
     */
    @GetMapping("/courseStudents/{courseId}")
    public Result<List<Map<String, Object>>> getCourseStudents(@PathVariable String courseId) {
        try {
            List<Map<String, Object>> students = teacherService.getCourseStudents(courseId);
            return Result.success(students, "学生名单查询成功");
        } catch (Exception e) {
            return Result.error("学生名单查询失败：" + e.getMessage());
        }
    }

    @PutMapping("/courseStudentScore")
    @RoleRequire({ "TEACHER", "ADMIN" })
    public Result<Boolean> updateCourseStudentScore(@RequestBody UpdateStudentScoreRequest req) {
        try {
            if (req == null || req.getTeacherId() == null || req.getCourseId() == null || req.getStudentId() == null) {
                return Result.error("参数不能为空");
            }
            String teacherId = req.getTeacherId().trim();
            String courseId = req.getCourseId().trim();
            String studentId = req.getStudentId().trim();
            if (teacherId.isEmpty() || courseId.isEmpty() || studentId.isEmpty()) {
                return Result.error("参数不能为空");
            }

            Course course = courseService.getById(courseId);
            if (course == null) {
                return Result.error("课程不存在");
            }
            if (course.getTeacherId() == null || !teacherId.equals(course.getTeacherId())) {
                return Result.error("无权限录入该课程成绩");
            }

            Enrollment enrollment = enrollmentService.getOne(new QueryWrapper<Enrollment>()
                    .eq("student_id", studentId)
                    .eq("course_id", courseId)
                    .last("LIMIT 1"));
            if (enrollment == null) {
                return Result.error("该学生未选该课程");
            }

            enrollment.setScore(req.getScore());
            boolean updated = enrollmentService.updateById(enrollment);
            return updated ? Result.success(true, "成绩录入成功") : Result.error("成绩录入失败");
        } catch (Exception e) {
            return Result.error("成绩录入失败：" + e.getMessage());
        }
    }

    @PutMapping("/courseCapacity")
    @RoleRequire({ "TEACHER", "ADMIN" })
    public Result<Boolean> updateCourseCapacity(@RequestBody UpdateCourseCapacityRequest req) {
        try {
            if (req == null || req.getTeacherId() == null || req.getCourseId() == null
                    || req.getMaxStudents() == null) {
                return Result.error("参数不能为空");
            }

            String teacherId = req.getTeacherId().trim();
            String courseId = req.getCourseId().trim();
            Integer maxStudents = req.getMaxStudents();

            if (teacherId.isEmpty() || courseId.isEmpty()) {
                return Result.error("参数不能为空");
            }
            if (maxStudents <= 0) {
                return Result.error("容量必须大于0");
            }

            Course course = courseService.getById(courseId);
            if (course == null) {
                return Result.error("课程不存在");
            }
            if (course.getTeacherId() == null || !teacherId.equals(course.getTeacherId())) {
                return Result.error("无权限修改该课程容量");
            }

            int enrolled = enrollmentService.getEnrollmentCountByCourse(courseId);
            if (maxStudents < enrolled) {
                return Result.error("容量不能小于已选人数：" + enrolled);
            }

            course.setMaxStudents(maxStudents);
            boolean updated = courseService.updateById(course);
            if (updated) {
                return Result.success(true, "容量修改成功");
            }
            return Result.error("容量修改失败");
        } catch (Exception e) {
            return Result.error("容量修改失败：" + e.getMessage());
        }
    }

    /**
     * 获取教师课表
     */
    @GetMapping("/schedule/{teacherId}")
    @RoleRequire({ "TEACHER", "ADMIN" })
    public Result<List<Map<String, Object>>> getTeacherSchedule(@PathVariable String teacherId) {
        try {
            List<Map<String, Object>> schedule = teacherService.getTeacherSchedule(teacherId);
            return Result.success(schedule, "课表查询成功");
        } catch (Exception e) {
            return Result.error("课表查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/schedule/{teacherId}/export")
    @RoleRequire({ "TEACHER", "ADMIN" })
    public ResponseEntity<byte[]> exportTeacherSchedule(@PathVariable String teacherId) {
        try {
            List<Map<String, Object>> schedule = teacherService.getTeacherSchedule(teacherId);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("课表");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("课程编号");
            header.createCell(1).setCellValue("课程名称");
            header.createCell(2).setCellValue("星期");
            header.createCell(3).setCellValue("开始时间");
            header.createCell(4).setCellValue("结束时间");
            header.createCell(5).setCellValue("地点");
            header.createCell(6).setCellValue("学分");
            header.createCell(7).setCellValue("学生人数");

            int rowIdx = 1;
            for (Map<String, Object> item : schedule) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(String.valueOf(item.getOrDefault("courseId", "")));
                row.createCell(1).setCellValue(String.valueOf(item.getOrDefault("courseName", "")));
                row.createCell(2).setCellValue(String.valueOf(item.getOrDefault("weekDayChinese", "")));
                row.createCell(3).setCellValue(String.valueOf(item.getOrDefault("startTime", "")));
                row.createCell(4).setCellValue(String.valueOf(item.getOrDefault("endTime", "")));
                row.createCell(5).setCellValue(String.valueOf(item.getOrDefault("location", "")));
                row.createCell(6).setCellValue(String.valueOf(item.getOrDefault("credit", "")));
                row.createCell(7).setCellValue(String.valueOf(item.getOrDefault("studentCount", "")));
            }

            for (int i = 0; i <= 7; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();

            String filename = "schedule_" + teacherId + ".xlsx";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

            return ResponseEntity.ok().headers(headers).body(baos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\",\"data\":null}")
                            .getBytes());
        }
    }

    @GetMapping("/export")
    @RoleRequire({ "ADMIN" })
    public ResponseEntity<byte[]> exportTeachers() {
        try {
            List<Teacher> list = teacherService.list();

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("教师列表");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("工号");
            header.createCell(1).setCellValue("姓名");
            header.createCell(2).setCellValue("职称");
            header.createCell(3).setCellValue("邮箱");

            int rowIdx = 1;
            for (Teacher t : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(String.valueOf(t.getTeacherId()));
                row.createCell(1).setCellValue(String.valueOf(t.getName()));
                row.createCell(2).setCellValue(String.valueOf(t.getTitle()));
                row.createCell(3).setCellValue(String.valueOf(t.getEmail()));
            }

            for (int i = 0; i <= 3; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();

            String filename = "teachers.xlsx";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

            return ResponseEntity.ok().headers(headers).body(baos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\",\"data\":null}")
                            .getBytes());
        }
    }

    /**
     * 获取教师仪表盘统计数据
     */
    @GetMapping("/dashboard/stats")
    @RoleRequire({ "TEACHER", "ADMIN" })
    public Result<Map<String, Object>> getDashboardStats(@RequestParam String teacherId) {
        try {
            List<Map<String, Object>> courses = teacherService.getTeacherCourses(teacherId);

            int teachingCourses = courses.size();
            int totalStudents = 0;
            int scheduledClasses = 0;

            for (Map<String, Object> course : courses) {
                Object enrolledObj = course.get("currentEnrollment");
                if (enrolledObj instanceof Number number) {
                    totalStudents += number.intValue();
                }
                Object scheduledObj = course.get("scheduled");
                if (scheduledObj instanceof Boolean && (Boolean) scheduledObj) {
                    scheduledClasses += 1;
                }
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("teachingCourses", teachingCourses);
            stats.put("totalStudents", totalStudents);
            stats.put("scheduledClasses", scheduledClasses);
            return Result.success(stats, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public Result<Boolean> changePassword(
            @RequestParam String teacherId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            boolean success = teacherService.changePassword(teacherId, oldPassword, newPassword);
            if (success) {
                return Result.success(true, "密码修改成功");
            } else {
                return Result.error("密码修改失败，原密码错误或用户不存在");
            }
        } catch (Exception e) {
            return Result.error("密码修改失败：" + e.getMessage());
        }
    }
}
