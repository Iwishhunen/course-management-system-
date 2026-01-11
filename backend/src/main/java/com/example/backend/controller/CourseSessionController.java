package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.config.RoleRequire;
import com.example.backend.entity.CourseSession;
import com.example.backend.service.ICourseSessionService;
import common.Result;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/courseSession")
public class CourseSessionController {

    @Autowired
    private ICourseSessionService courseSessionService;

    @GetMapping("/listByCourse")
    public Result<List<CourseSession>> listByCourse(@RequestParam String courseId) {
        try {
            if (courseId == null || courseId.trim().isEmpty()) {
                return Result.error("courseId不能为空");
            }
            List<CourseSession> list = courseSessionService.list(
                    new QueryWrapper<CourseSession>().eq("course_id", courseId).orderByAsc("start_time"));
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/add")
    @RoleRequire({ "TEACHER", "ADMIN" })
    public Result<Boolean> add(@RequestBody CourseSession session) {
        try {
            if (session == null || session.getCourseId() == null || session.getCourseId().trim().isEmpty()) {
                return Result.error("courseId不能为空");
            }
            boolean saved = courseSessionService.save(session);
            return saved ? Result.success(true, "添加成功") : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    @PutMapping("/update")
    @RoleRequire({ "TEACHER", "ADMIN" })
    public Result<Boolean> update(@RequestBody CourseSession session) {
        try {
            if (session == null || session.getSessionId() == null) {
                return Result.error("sessionId不能为空");
            }
            boolean updated = courseSessionService.updateById(session);
            return updated ? Result.success(true, "修改成功") : Result.error("修改失败");
        } catch (Exception e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @RoleRequire({ "TEACHER", "ADMIN" })
    public Result<Boolean> delete(@PathVariable Integer id) {
        try {
            boolean removed = courseSessionService.removeById(id);
            return removed ? Result.success(true, "删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
