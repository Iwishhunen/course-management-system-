package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Enrollment;
import com.example.backend.service.IEnrollmentService;
import common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 选课记录基本信息表 前端控制器
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@RestController
@RequestMapping("/enrollment")
public class EnrollmentController {

    @Autowired
    private IEnrollmentService enrollmentService;

    /**
     * 新增选课记录
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody Enrollment enrollment) {
        try {
            boolean saved = enrollmentService.save(enrollment);
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
     * 根据ID删除选课记录
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        try {
            boolean removed = enrollmentService.removeById(id);
            if (removed) {
                return Result.success(true, "删除成功");
            } else {
                return Result.error("删除失败，选课记录不存在");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除选课记录
     */
    @DeleteMapping("/delete/batch")
    public Result<Boolean> deleteBatch(@RequestBody List<Integer> ids) {
        try {
            boolean removed = enrollmentService.removeByIds(ids);
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
     * 修改选课记录信息
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Enrollment enrollment) {
        try {
            if (enrollment.getEnrollmentId() == null ) {
                return Result.error("ID不能为空");
            }
            boolean updated = enrollmentService.updateById(enrollment);
            if (updated) {
                return Result.success(true, "修改成功");
            } else {
                return Result.error("修改失败，选课记录不存在");
            }
        } catch (Exception e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询选课记录
     */
    @GetMapping("/get/{id}")
    public Result<Enrollment> getById(@PathVariable Integer id) {
        try {
            Enrollment enrollment = enrollmentService.getById(id);
            if (enrollment != null) {
                return Result.success(enrollment, "查询成功");
            } else {
                return Result.error("选课记录不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有选课记录
     */
    @GetMapping("/list")
    public Result<List<Enrollment>> list() {
        try {
            List<Enrollment> list = enrollmentService.list();
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询选课记录
     */
    @GetMapping("/page")
    public Result<Page<Enrollment>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String courseId) {
        try {
            Page<Enrollment> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Enrollment> queryWrapper = new QueryWrapper<>();

            // 查询条件
            if (studentId != null && !studentId.trim().isEmpty()) {
                queryWrapper.eq("student_id", studentId);
            }
            if (courseId != null && !courseId.trim().isEmpty()) {
                queryWrapper.eq("course_id", courseId);
            }

            // 排序
            queryWrapper.orderByDesc("enrollment_id");

            Page<Enrollment> result = enrollmentService.page(page, queryWrapper);
            return Result.success(result, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 条件查询（模糊或精确匹配）
     */
    @GetMapping("/query")
    public Result<List<Enrollment>> query(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String value,
            @RequestParam(defaultValue = "false") Boolean exactMatch) {
        try {
            QueryWrapper<Enrollment> queryWrapper = new QueryWrapper<>();

            if (key != null && value != null && !value.trim().isEmpty()) {
                if (exactMatch) {
                    // 精确匹配
                    queryWrapper.eq(key, value);
                } else {
                    // 模糊查询
                    queryWrapper.like(key, value);
                }
            }

            List<Enrollment> list = enrollmentService.list(queryWrapper);
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 学生选课
     */
    @PostMapping("/enroll")
    public Result<Boolean> enrollCourse(@RequestParam String studentId, @RequestParam String courseId) {
        try {
            boolean enrolled = enrollmentService.enrollCourse(studentId, courseId);
            if (enrolled) {
                return Result.success(true, "选课成功");
            } else {
                return Result.error("选课失败");
            }
        } catch (Exception e) {
            return Result.error("选课失败：" + e.getMessage());
        }
    }

    /**
     * 学生退课
     */
    @DeleteMapping("/drop")
    public Result<Boolean> dropCourse(@RequestParam String studentId, @RequestParam String courseId) {
        try {
            boolean dropped = enrollmentService.dropCourse(studentId, courseId);
            if (dropped) {
                return Result.success(true, "退课成功");
            } else {
                return Result.error("退课失败");
            }
        } catch (Exception e) {
            return Result.error("退课失败：" + e.getMessage());
        }
    }
}
