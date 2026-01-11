package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.CourseCategories;
import com.example.backend.service.ICourseCategoriesService;
import common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 课程类型基本信息表 前端控制器
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@RestController
@RequestMapping("/courseCategories")
public class CourseCategoriesController {

    @Autowired
    private ICourseCategoriesService courseCategoriesService;

    /**
     * 新增课程类型
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody CourseCategories courseCategories) {
        try {
            boolean saved = courseCategoriesService.save(courseCategories);
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
     * 根据ID删除课程类型
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        try {
            boolean removed = courseCategoriesService.removeById(id);
            if (removed) {
                return Result.success(true, "删除成功");
            } else {
                return Result.error("删除失败，课程类型不存在");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除课程类型
     */
    @DeleteMapping("/delete/batch")
    public Result<Boolean> deleteBatch(@RequestBody List<String> ids) {
        try {
            boolean removed = courseCategoriesService.removeByIds(ids);
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
     * 修改课程类型信息
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody CourseCategories courseCategories) {
        try {
            if (courseCategories.getTypeCode() == null ) {
                return Result.error("ID不能为空");
            }
            boolean updated = courseCategoriesService.updateById(courseCategories);
            if (updated) {
                return Result.success(true, "修改成功");
            } else {
                return Result.error("修改失败，课程类型不存在");
            }
        } catch (Exception e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询课程类型
     */
    @GetMapping("/get/{id}")
    public Result<CourseCategories> getById(@PathVariable String id) {
        try {
            CourseCategories courseCategories = courseCategoriesService.getById(id);
            if (courseCategories != null) {
                return Result.success(courseCategories, "查询成功");
            } else {
                return Result.error("课程类型不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有课程类型
     */
    @GetMapping("/list")
    public Result<List<CourseCategories>> list() {
        try {
            List<CourseCategories> list = courseCategoriesService.list();
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询课程类型
     */
    @GetMapping("/page")
    public Result<Page<CourseCategories>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String typeName) {
        try {
            Page<CourseCategories> page = new Page<>(pageNum, pageSize);
            QueryWrapper<CourseCategories> queryWrapper = new QueryWrapper<>();

            // 模糊查询条件
            if (typeName != null && !typeName.trim().isEmpty()) {
                queryWrapper.like("type_name", typeName);
            }

            // 排序
            queryWrapper.orderByDesc("type_code");

            Page<CourseCategories> result = courseCategoriesService.page(page, queryWrapper);
            return Result.success(result, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 条件查询（模糊或精确匹配）
     */
    @GetMapping("/query")
    public Result<List<CourseCategories>> query(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String value,
            @RequestParam(defaultValue = "false") Boolean exactMatch) {
        try {
            QueryWrapper<CourseCategories> queryWrapper = new QueryWrapper<>();

            if (key != null && value != null && !value.trim().isEmpty()) {
                if (exactMatch) {
                    // 精确匹配
                    queryWrapper.eq(key, value);
                } else {
                    // 模糊查询
                    queryWrapper.like(key, value);
                }
            }

            List<CourseCategories> list = courseCategoriesService.list(queryWrapper);
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
