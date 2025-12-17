package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.config.RoleRequire;
import com.example.backend.entity.Course;
import com.example.backend.service.ICourseService;
import com.example.backend.service.ITeacherService;
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
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程基本信息表 前端控制器
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private ICourseService courseService;

    @Autowired
    private ITeacherService teacherService;

    @PostMapping("/reassignTeachers")
    @RoleRequire({ "ADMIN" })
    public Result<Map<String, Object>> reassignTeachers() {
        try {
            List<com.example.backend.entity.Teacher> teachers = teacherService.list();
            if (teachers == null || teachers.isEmpty()) {
                return Result.error("没有可用教师，无法重新分配");
            }
            List<String> teacherIds = teachers.stream()
                    .map(com.example.backend.entity.Teacher::getTeacherId)
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());

            List<Course> courses = courseService.list();
            int updated = 0;
            for (Course course : courses) {
                if (course == null || course.getCourseId() == null || course.getCourseId().isEmpty()) {
                    continue;
                }
                int idx = Math.floorMod(course.getCourseId().hashCode(), teacherIds.size());
                String newTeacherId = teacherIds.get(idx);
                if (!newTeacherId.equals(course.getTeacherId())) {
                    course.setTeacherId(newTeacherId);
                    courseService.updateById(course);
                    updated++;
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("teachers", teacherIds.size());
            data.put("courses", courses.size());
            data.put("updated", updated);
            return Result.success(data, "重新分配完成");
        } catch (Exception e) {
            return Result.error("重新分配失败：" + e.getMessage());
        }
    }

    /**
     * 新增课程
     */
    @PostMapping("/add")
    @RoleRequire({ "ADMIN" })
    public Result<Boolean> add(@RequestBody Course course) {
        try {
            boolean saved = courseService.save(course);
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
     * 根据ID删除课程
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        try {
            boolean removed = courseService.removeById(id);
            if (removed) {
                return Result.success(true, "删除成功");
            } else {
                return Result.error("删除失败，课程不存在");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除课程
     */
    @DeleteMapping("/delete/batch")
    public Result<Boolean> deleteBatch(@RequestBody List<String> ids) {
        try {
            boolean removed = courseService.removeByIds(ids);
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
     * 修改课程信息
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Course course) {
        try {
            if (course.getCourseId() == null) {
                return Result.error("ID不能为空");
            }
            boolean updated = courseService.updateById(course);
            if (updated) {
                return Result.success(true, "修改成功");
            } else {
                return Result.error("修改失败，课程不存在");
            }
        } catch (Exception e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询课程
     */
    @GetMapping("/get/{id}")
    public Result<Course> getById(@PathVariable String id) {
        try {
            Course course = courseService.getById(id);
            if (course != null) {
                return Result.success(course, "查询成功");
            } else {
                return Result.error("课程不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有课程
     */
    @GetMapping("/list")
    public Result<List<Course>> list() {
        try {
            List<Course> list = courseService.list();
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询课程（包含学生选课状态）
     */
    @GetMapping("/listWithEnrollment")
    public Result<Page<Map<String, Object>>> listWithEnrollment(
            @RequestParam String studentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String courseType) {
        try {
            Page<Map<String, Object>> page = courseService.getCourseListWithEnrollmentStatus(
                    studentId, pageNum, pageSize, courseName, teacherName, courseType);
            return Result.success(page, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/export")
    @RoleRequire({ "ADMIN" })
    public ResponseEntity<byte[]> exportCourses() {
        try {
            List<Course> list = courseService.list();
            Map<String, String> teacherNameMap = teacherService.list().stream()
                    .collect(Collectors.toMap(
                            com.example.backend.entity.Teacher::getTeacherId,
                            com.example.backend.entity.Teacher::getName,
                            (a, b) -> a));

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("课程列表");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("课程编号");
            header.createCell(1).setCellValue("课程名称");
            header.createCell(2).setCellValue("学分");
            header.createCell(3).setCellValue("容量");
            header.createCell(4).setCellValue("任课教师");
            header.createCell(5).setCellValue("课程类型");

            int rowIdx = 1;
            for (Course c : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(String.valueOf(c.getCourseId()));
                row.createCell(1).setCellValue(String.valueOf(c.getCourseName()));
                row.createCell(2).setCellValue(String.valueOf(c.getCredit()));
                row.createCell(3).setCellValue(String.valueOf(c.getMaxStudents()));
                row.createCell(4).setCellValue(String.valueOf(teacherNameMap.getOrDefault(c.getTeacherId(), "未知教师")));
                row.createCell(5).setCellValue(String.valueOf(c.getTypeCode()));
            }

            for (int i = 0; i <= 5; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();

            String filename = "courses.xlsx";
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
     * 分页查询课程
     */
    @GetMapping("/page")
    public Result<Page<Course>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String typeCode) {
        try {
            Page<Course> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Course> queryWrapper = new QueryWrapper<>();

            // 模糊查询条件
            if (courseId != null && !courseId.trim().isEmpty()) {
                String courseIdTrim = courseId.trim();
                queryWrapper.like("course_id", courseIdTrim);
            }
            if (courseName != null && !courseName.trim().isEmpty()) {
                queryWrapper.like("course_name", courseName.trim());
            }
            if (teacherName != null && !teacherName.trim().isEmpty()) {
                QueryWrapper<com.example.backend.entity.Teacher> teacherQueryWrapper = new QueryWrapper<>();
                teacherQueryWrapper.like("name", teacherName.trim());
                List<com.example.backend.entity.Teacher> matchedTeachers = teacherService.list(teacherQueryWrapper);
                if (matchedTeachers.isEmpty()) {
                    return Result.success(new Page<>(pageNum, pageSize, 0), "查询成功");
                }
                Set<String> teacherIds = matchedTeachers.stream().map(com.example.backend.entity.Teacher::getTeacherId)
                        .collect(Collectors.toSet());
                queryWrapper.in("teacher_id", teacherIds);
            }
            if (typeCode != null && !typeCode.trim().isEmpty()) {
                queryWrapper.eq("type_code", typeCode.trim());
            }

            // 排序
            queryWrapper.orderByDesc("course_id");

            Page<Course> result = courseService.page(page, queryWrapper);
            return Result.success(result, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 条件查询（模糊或精确匹配）
     */
    @GetMapping("/query")
    public Result<List<Course>> query(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String value,
            @RequestParam(defaultValue = "false") Boolean exactMatch) {
        try {
            QueryWrapper<Course> queryWrapper = new QueryWrapper<>();

            if (key != null && value != null && !value.trim().isEmpty()) {
                if (exactMatch) {
                    // 精确匹配
                    queryWrapper.eq(key, value);
                } else {
                    // 模糊查询
                    queryWrapper.like(key, value);
                }
            }

            List<Course> list = courseService.list(queryWrapper);
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 选课结束后取消选课人数不足的课程
     */
    @PostMapping("/postEnrollmentCancel")
    @RoleRequire({ "ADMIN" })
    public Result<List<Course>> cancelLowEnrollmentCoursesPostEnrollment() {
        try {
            List<Course> cancelledCourses = courseService.cancelLowEnrollmentCourses();
            return Result.success(cancelledCourses, "选课结束后成功取消" + cancelledCourses.size() + "门选课人数不足的课程");
        } catch (Exception e) {
            return Result.error("取消课程失败：" + e.getMessage());
        }
    }

    /**
     * 查找选课人数不足的课程（仅用于预览）
     */
    @GetMapping("/previewLowEnrollment")
    public Result<List<Course>> previewLowEnrollmentCourses() {
        try {
            List<Course> courses = courseService.findLowEnrollmentCourses();
            return Result.success(courses, "查询成功，共找到" + courses.size() + "门选课人数不足的课程");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}