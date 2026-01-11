package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Enrollmentperiod;
import com.example.backend.service.IEnrollmentperiodService;
import common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 选课时间段基本信息表 前端控制器
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@RestController
@RequestMapping("/enrollmentperiod")
public class EnrollmentperiodController {

    @Autowired
    private IEnrollmentperiodService enrollmentperiodService;

    /**
     * 新增选课时间段
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody Enrollmentperiod enrollmentperiod) {
        try {
            boolean saved = enrollmentperiodService.save(enrollmentperiod);
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
     * 根据ID删除选课时间段
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        try {
            boolean removed = enrollmentperiodService.removeById(id);
            if (removed) {
                return Result.success(true, "删除成功");
            } else {
                return Result.error("删除失败，选课时间段不存在");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除选课时间段
     */
    @DeleteMapping("/delete/batch")
    public Result<Boolean> deleteBatch(@RequestBody List<Integer> ids) {
        try {
            boolean removed = enrollmentperiodService.removeByIds(ids);
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
     * 修改选课时间段信息
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Enrollmentperiod enrollmentperiod) {
        try {
            if (enrollmentperiod.getPeriodId() == null ) {
                return Result.error("ID不能为空");
            }
            boolean updated = enrollmentperiodService.updateById(enrollmentperiod);
            if (updated) {
                return Result.success(true, "修改成功");
            } else {
                return Result.error("修改失败，选课时间段不存在");
            }
        } catch (Exception e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询选课时间段
     */
    @GetMapping("/get/{id}")
    public Result<Enrollmentperiod> getById(@PathVariable Integer id) {
        try {
            Enrollmentperiod enrollmentperiod = enrollmentperiodService.getById(id);
            if (enrollmentperiod != null) {
                return Result.success(enrollmentperiod, "查询成功");
            } else {
                return Result.error("选课时间段不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有选课时间段
     */
    @GetMapping("/list")
    public Result<List<Enrollmentperiod>> list() {
        try {
            List<Enrollmentperiod> list = enrollmentperiodService.list();
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询选课时间段
     */
    @GetMapping("/page")
    public Result<Page<Enrollmentperiod>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Boolean isActive) {
        try {
            Page<Enrollmentperiod> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Enrollmentperiod> queryWrapper = new QueryWrapper<>();

            // 查询条件
            if (isActive != null) {
                queryWrapper.eq("is_active", isActive);
            }

            // 排序
            queryWrapper.orderByDesc("period_id");

            Page<Enrollmentperiod> result = enrollmentperiodService.page(page, queryWrapper);
            return Result.success(result, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 条件查询（模糊或精确匹配）
     */
    @GetMapping("/query")
    public Result<List<Enrollmentperiod>> query(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String value,
            @RequestParam(defaultValue = "false") Boolean exactMatch) {
        try {
            QueryWrapper<Enrollmentperiod> queryWrapper = new QueryWrapper<>();

            if (key != null && value != null && !value.trim().isEmpty()) {
                if (exactMatch) {
                    // 精确匹配
                    queryWrapper.eq(key, value);
                } else {
                    // 模糊查询
                    queryWrapper.like(key, value);
                }
            }

            List<Enrollmentperiod> list = enrollmentperiodService.list(queryWrapper);
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
