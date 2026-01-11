package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.config.RoleRequire;
import com.example.backend.entity.Student;
import com.example.backend.service.IEnrollmentService;
import com.example.backend.service.IStudentService;
import common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 学生基本信息表 前端控制器
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IEnrollmentService enrollmentService;

    /**
     * 新增学生
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody Student student) {
        try {
            boolean saved = studentService.save(student);
            if (saved) {
                return Result.success(true, "添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    @GetMapping("/export")
    @RoleRequire({ "ADMIN" })
    public ResponseEntity<byte[]> exportStudents() {
        try {
            List<Student> list = studentService.list();

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("学生列表");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("学号");
            header.createCell(1).setCellValue("姓名");
            header.createCell(2).setCellValue("性别");
            header.createCell(3).setCellValue("专业");
            header.createCell(4).setCellValue("班级");
            header.createCell(5).setCellValue("邮箱");

            int rowIdx = 1;
            for (Student s : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(String.valueOf(s.getStudentId()));
                row.createCell(1).setCellValue(String.valueOf(s.getName()));
                row.createCell(2).setCellValue(String.valueOf(s.getGender()));
                row.createCell(3).setCellValue(String.valueOf(s.getMajor()));
                row.createCell(4).setCellValue(String.valueOf(s.getClassId()));
                row.createCell(5).setCellValue(String.valueOf(s.getEmail()));
            }

            for (int i = 0; i <= 5; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();

            String filename = "students.xlsx";
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
     * 根据ID删除学生
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        try {
            boolean removed = studentService.removeById(id);
            if (removed) {
                return Result.success(true, "删除成功");
            } else {
                return Result.error("删除失败，学生不存在");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除学生
     */
    @DeleteMapping("/delete/batch")
    public Result<Boolean> deleteBatch(@RequestBody List<String> ids) {
        try {
            boolean removed = studentService.removeByIds(ids);
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
     * 修改学生信息
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Student student) {
        try {
            if (student.getStudentId() == null) {
                return Result.error("ID不能为空");
            }
            boolean updated = studentService.updateById(student);
            if (updated) {
                return Result.success(true, "修改成功");
            } else {
                return Result.error("修改失败，学生不存在");
            }
        } catch (Exception e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询学生
     */
    @GetMapping("/get/{id}")
    public Result<Student> getById(@PathVariable String id) {
        try {
            Student student = studentService.getById(id);
            if (student != null) {
                return Result.success(student, "查询成功");
            } else {
                return Result.error("学生不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有学生
     */
    @GetMapping("/list")
    public Result<List<Student>> list() {
        try {
            List<Student> list = studentService.list();
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询学生
     */
    @GetMapping("/page")
    public Result<Page<Student>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String classId) {
        try {
            Page<Student> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Student> queryWrapper = new QueryWrapper<>();

            if (studentId != null && !studentId.trim().isEmpty()) {
                queryWrapper.eq("student_id", studentId);
            }

            // 模糊查询条件
            if (studentName != null && !studentName.trim().isEmpty()) {
                queryWrapper.like("name", studentName);
            }
            if (major != null && !major.trim().isEmpty()) {
                queryWrapper.like("major", major);
            }
            if (classId != null && !classId.trim().isEmpty()) {
                queryWrapper.like("classID", classId);
            }

            // 排序
            queryWrapper.orderByDesc("student_id");

            Page<Student> result = studentService.page(page, queryWrapper);
            return Result.success(result, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 条件查询（模糊或精确匹配）
     */
    @GetMapping("/query")
    public Result<List<Student>> query(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String value,
            @RequestParam(defaultValue = "false") Boolean exactMatch) {
        try {
            QueryWrapper<Student> queryWrapper = new QueryWrapper<>();

            if (key != null && value != null && !value.trim().isEmpty()) {
                if (exactMatch) {
                    // 精确匹配
                    queryWrapper.eq(key, value);
                } else {
                    // 模糊查询
                    queryWrapper.like(key, value);
                }
            }

            List<Student> list = studentService.list(queryWrapper);
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取学生课表
     */
    @GetMapping("/schedule/{studentId}")
    @RoleRequire({ "STUDENT", "ADMIN" })
    public Result<List<Map<String, Object>>> getStudentSchedule(@PathVariable String studentId) {
        try {
            List<Map<String, Object>> schedule = studentService.getStudentSchedule(studentId);
            return Result.success(schedule, "课表查询成功");
        } catch (Exception e) {
            return Result.error("课表查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/schedule/{studentId}/export")
    @RoleRequire({ "STUDENT", "ADMIN" })
    public ResponseEntity<byte[]> exportStudentSchedule(@PathVariable String studentId) {
        try {
            List<Map<String, Object>> schedule = studentService.getStudentSchedule(studentId);

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
            header.createCell(7).setCellValue("任课教师");

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
                row.createCell(7).setCellValue(String.valueOf(item.getOrDefault("teacherName", "")));
            }

            for (int i = 0; i <= 7; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();

            String filename = "schedule_" + studentId + ".xlsx";
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
     * 获取学生仪表盘统计数据
     */
    @GetMapping("/dashboard/stats")
    @RoleRequire({ "STUDENT", "ADMIN" })
    public Result<Map<String, Object>> getDashboardStats(@RequestParam String studentId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("selectedCourses", enrollmentService.getEnrollmentCountByStudent(studentId));
            stats.put("totalCredits", enrollmentService.getTotalCreditsByStudent(studentId));
            stats.put("enrollmentPeriod", enrollmentService.isInEnrollmentPeriod() ? "进行中" : "未在选课时间段");
            return Result.success(stats, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
