package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Schedule;
import com.example.backend.service.IScheduleService;
import common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 上课安排基本信息表 前端控制器
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private IScheduleService scheduleService;

    /**
     * 新增上课安排
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody Schedule schedule) {
        try {
            if (schedule == null) {
                return Result.error("参数不能为空");
            }
            if (schedule.getStartWeek() == null) {
                schedule.setStartWeek(1);
            }
            if (schedule.getEndWeek() == null) {
                schedule.setEndWeek(16);
            }
            if (schedule.getStartWeek() != null && schedule.getEndWeek() != null
                    && schedule.getStartWeek() > schedule.getEndWeek()) {
                return Result.error("教学周范围不合法");
            }
            if (schedule.getScheduleId() == null) {
                Schedule max = scheduleService
                        .getOne(new QueryWrapper<Schedule>().orderByDesc("schedule_id").last("LIMIT 1"));
                int nextId = (max != null && max.getScheduleId() != null) ? (max.getScheduleId() + 1) : 1;
                schedule.setScheduleId(nextId);
            }
            boolean saved = scheduleService.save(schedule);
            if (saved) {
                return Result.success(true, "添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    @GetMapping("/listByCourse")
    public Result<List<Schedule>> listByCourse(@RequestParam String courseId) {
        try {
            if (courseId == null || courseId.trim().isEmpty()) {
                return Result.error("courseId不能为空");
            }
            List<Schedule> list = scheduleService.list(new QueryWrapper<Schedule>()
                    .eq("course_id", courseId)
                    .orderByAsc("week_day").orderByAsc("start_time"));
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/upsertByCourse")
    public Result<Boolean> upsertByCourse(@RequestBody Schedule schedule) {
        try {
            if (schedule == null || schedule.getCourseId() == null || schedule.getCourseId().trim().isEmpty()) {
                return Result.error("courseId不能为空");
            }

            if (schedule.getStartWeek() == null) {
                schedule.setStartWeek(1);
            }
            if (schedule.getEndWeek() == null) {
                schedule.setEndWeek(16);
            }
            if (schedule.getStartWeek() != null && schedule.getEndWeek() != null
                    && schedule.getStartWeek() > schedule.getEndWeek()) {
                return Result.error("教学周范围不合法");
            }

            Schedule existing = scheduleService.getOne(new QueryWrapper<Schedule>()
                    .eq("course_id", schedule.getCourseId())
                    .orderByAsc("week_day").orderByAsc("start_time")
                    .last("LIMIT 1"));

            if (existing != null && existing.getScheduleId() != null) {
                schedule.setScheduleId(existing.getScheduleId());
                boolean updated = scheduleService.updateById(schedule);
                return updated ? Result.success(true, "更新成功") : Result.error("更新失败");
            }

            if (schedule.getScheduleId() == null) {
                Schedule max = scheduleService
                        .getOne(new QueryWrapper<Schedule>().orderByDesc("schedule_id").last("LIMIT 1"));
                int nextId = (max != null && max.getScheduleId() != null) ? (max.getScheduleId() + 1) : 1;
                schedule.setScheduleId(nextId);
            }
            boolean saved = scheduleService.save(schedule);
            return saved ? Result.success(true, "添加成功") : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID删除上课安排
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        try {
            boolean removed = scheduleService.removeById(id);
            if (removed) {
                return Result.success(true, "删除成功");
            } else {
                return Result.error("删除失败，上课安排不存在");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除上课安排
     */
    @DeleteMapping("/delete/batch")
    public Result<Boolean> deleteBatch(@RequestBody List<Integer> ids) {
        try {
            boolean removed = scheduleService.removeByIds(ids);
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
     * 修改上课安排信息
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Schedule schedule) {
        try {
            if (schedule.getScheduleId() == null) {
                return Result.error("ID不能为空");
            }
            if (schedule.getStartWeek() == null) {
                schedule.setStartWeek(1);
            }
            if (schedule.getEndWeek() == null) {
                schedule.setEndWeek(16);
            }
            if (schedule.getStartWeek() != null && schedule.getEndWeek() != null
                    && schedule.getStartWeek() > schedule.getEndWeek()) {
                return Result.error("教学周范围不合法");
            }
            boolean updated = scheduleService.updateById(schedule);
            if (updated) {
                return Result.success(true, "修改成功");
            } else {
                return Result.error("修改失败，上课安排不存在");
            }
        } catch (Exception e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询上课安排
     */
    @GetMapping("/get/{id}")
    public Result<Schedule> getById(@PathVariable Integer id) {
        try {
            Schedule schedule = scheduleService.getById(id);
            if (schedule != null) {
                return Result.success(schedule, "查询成功");
            } else {
                return Result.error("上课安排不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有上课安排
     */
    @GetMapping("/list")
    public Result<List<Schedule>> list() {
        try {
            List<Schedule> list = scheduleService.list();
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询上课安排
     */
    @GetMapping("/page")
    public Result<Page<Schedule>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String location) {
        try {
            Page<Schedule> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();

            // 查询条件
            if (courseId != null && !courseId.trim().isEmpty()) {
                queryWrapper.eq("course_id", courseId);
            }
            if (location != null && !location.trim().isEmpty()) {
                queryWrapper.like("location", location);
            }

            // 排序
            queryWrapper.orderByDesc("schedule_id");

            Page<Schedule> result = scheduleService.page(page, queryWrapper);
            return Result.success(result, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 条件查询（模糊或精确匹配）
     */
    @GetMapping("/query")
    public Result<List<Schedule>> query(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String value,
            @RequestParam(defaultValue = "false") Boolean exactMatch) {
        try {
            QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();

            if (key != null && value != null && !value.trim().isEmpty()) {
                if (exactMatch) {
                    // 精确匹配
                    queryWrapper.eq(key, value);
                } else {
                    // 模糊查询
                    queryWrapper.like(key, value);
                }
            }

            List<Schedule> list = scheduleService.list(queryWrapper);
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
