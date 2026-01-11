package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.config.RoleRequire;
import com.example.backend.entity.Admin;
import com.example.backend.entity.Course;
import com.example.backend.entity.Enrollmentperiod;
import com.example.backend.service.IAdminService;
import common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 管理员基本信息表 前端控制器
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private IAdminService adminService;

    /**
     * 新增管理员
     */
    @PostMapping("/add")
    @RoleRequire({ "ADMIN" })
    public Result<Boolean> add(@RequestBody Admin admin) {
        try {
            boolean saved = adminService.save(admin);
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
     * 根据ID删除管理员
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        try {
            boolean removed = adminService.removeById(id);
            if (removed) {
                return Result.success(true, "删除成功");
            } else {
                return Result.error("删除失败，管理员不存在");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除管理员
     */
    @DeleteMapping("/delete/batch")
    public Result<Boolean> deleteBatch(@RequestBody List<String> ids) {
        try {
            boolean removed = adminService.removeByIds(ids);
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
     * 修改管理员信息
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Admin admin) {
        try {
            if (admin.getAdminId() == null) {
                return Result.error("ID不能为空");
            }
            boolean updated = adminService.updateById(admin);
            if (updated) {
                return Result.success(true, "修改成功");
            } else {
                return Result.error("修改失败，管理员不存在");
            }
        } catch (Exception e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询管理员
     */
    @GetMapping("/get/{id}")
    public Result<Admin> getById(@PathVariable String id) {
        try {
            Admin admin = adminService.getById(id);
            if (admin != null) {
                return Result.success(admin, "查询成功");
            } else {
                return Result.error("管理员不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有管理员
     */
    @GetMapping("/list")
    public Result<List<Admin>> list() {
        try {
            List<Admin> list = adminService.list();
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询管理员
     */
    @GetMapping("/page")
    public Result<Page<Admin>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String account) {
        try {
            Page<Admin> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();

            // 模糊查询条件
            if (name != null && !name.trim().isEmpty()) {
                queryWrapper.like("name", name);
            }
            if (account != null && !account.trim().isEmpty()) {
                queryWrapper.like("admin_id", account);
            }

            // 排序
            queryWrapper.orderByDesc("admin_id");

            Page<Admin> result = adminService.page(page, queryWrapper);
            return Result.success(result, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 条件查询（模糊或精确匹配）
     */
    @GetMapping("/query")
    public Result<List<Admin>> query(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String value,
            @RequestParam(defaultValue = "false") Boolean exactMatch) {
        try {
            QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();

            if (key != null && value != null && !value.trim().isEmpty()) {
                if (exactMatch) {
                    // 精确匹配
                    queryWrapper.eq(key, value);
                } else {
                    // 模糊查询
                    queryWrapper.like(key, value);
                }
            }

            List<Admin> list = adminService.list(queryWrapper);
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<Admin> login(@RequestBody Admin admin) {
        try {
            if (admin.getAdminId() == null || admin.getPassword() == null) {
                return Result.error("账号或密码不能为空");
            }

            QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("admin_id", admin.getAdminId())
                    .eq("password", admin.getPassword());

            Admin loginAdmin = adminService.getOne(queryWrapper);
            if (loginAdmin != null) {
                // 移除密码返回前端（安全考虑）
                loginAdmin.setPassword(null);
                return Result.success(loginAdmin, "登录成功");
            } else {
                return Result.error("账号或密码错误");
            }
        } catch (Exception e) {
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public Result<Boolean> changePassword(
            @RequestParam String id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            Admin admin = adminService.getById(id);
            if (admin == null) {
                return Result.error("管理员不存在");
            }

            // 验证旧密码
            if (!admin.getPassword().equals(oldPassword)) {
                return Result.error("旧密码错误");
            }

            // 更新密码
            admin.setPassword(newPassword);
            boolean updated = adminService.updateById(admin);
            if (updated) {
                return Result.success(true, "密码修改成功");
            } else {
                return Result.error("密码修改失败");
            }
        } catch (Exception e) {
            return Result.error("密码修改失败：" + e.getMessage());
        }
    }

    /**
     * 设置选课时间段
     */
    @PostMapping("/enrollmentPeriod")
    public Result<Boolean> setEnrollmentPeriod(
            @RequestBody Map<String, String> payload) {
        try {
            String startTimeStr = payload.get("startTime");
            String endTimeStr = payload.get("endTime");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startTimeStr, formatter);
            LocalDateTime end = LocalDateTime.parse(endTimeStr, formatter);

            boolean saved = adminService.setEnrollmentPeriod(start, end);
            if (saved) {
                return Result.success(true, "选课时间段设置成功");
            } else {
                return Result.error("选课时间段设置失败");
            }
        } catch (Exception e) {
            return Result.error("选课时间段设置失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有选课时间段
     */
    @GetMapping("/enrollmentPeriods")
    public Result<List<Enrollmentperiod>> getAllEnrollmentPeriods() {
        try {
            List<Enrollmentperiod> periods = adminService.getAllEnrollmentPeriods();
            return Result.success(periods, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 激活选课时间段
     */
    @PutMapping("/enrollmentPeriod/activate/{periodId}")
    public Result<Boolean> activateEnrollmentPeriod(@PathVariable Integer periodId) {
        try {
            boolean activated = adminService.activateEnrollmentPeriod(periodId);
            if (activated) {
                return Result.success(true, "选课时间段激活成功");
            } else {
                return Result.error("选课时间段激活失败");
            }
        } catch (Exception e) {
            return Result.error("选课时间段激活失败：" + e.getMessage());
        }
    }

    /**
     * 取消激活选课时间段
     */
    @PutMapping("/enrollmentPeriod/deactivate/{periodId}")
    public Result<Boolean> deactivateEnrollmentPeriod(@PathVariable Integer periodId) {
        try {
            boolean deactivated = adminService.deactivateEnrollmentPeriod(periodId);
            if (deactivated) {
                return Result.success(true, "选课时间段取消激活成功");
            } else {
                return Result.error("选课时间段取消激活失败");
            }
        } catch (Exception e) {
            return Result.error("选课时间段取消激活失败：" + e.getMessage());
        }
    }

    /**
     * 获取仪表板统计数据
     */
    @GetMapping("/dashboard/stats")
    @RoleRequire({ "ADMIN" })
    public Result<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            return Result.success(stats, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 手动触发取消选课人数不足20人的课程
     */
    @PostMapping("/cancelLowEnrollmentCourses")
    @RoleRequire({ "ADMIN" })
    public Result<List<Course>> cancelLowEnrollmentCourses() {
        try {
            List<Course> cancelledCourses = adminService.cancelLowEnrollmentCourses();
            return Result.success(cancelledCourses, "成功取消" + cancelledCourses.size() + "门选课人数不足的课程");
        } catch (Exception e) {
            return Result.error("取消课程失败：" + e.getMessage());
        }
    }
}