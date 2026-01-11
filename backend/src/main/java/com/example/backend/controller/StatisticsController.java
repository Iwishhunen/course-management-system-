package com.example.backend.controller;

import com.example.backend.config.RoleRequire;
import com.example.backend.service.IStatisticsService;
import common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统计分析控制器
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private IStatisticsService statisticsService;

    /**
     * 按课程统计选课情况
     */
    @GetMapping("/enrollment/course")
    @RoleRequire({"ADMIN"})
    public Result<List<Map<String, Object>>> getEnrollmentStatsByCourse() {
        try {
            List<Map<String, Object>> stats = statisticsService.getEnrollmentStatsByCourse();
            return Result.success(stats, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 按班级统计选课情况
     */
    @GetMapping("/enrollment/class")
    @RoleRequire({"ADMIN"})
    public Result<List<Map<String, Object>>> getEnrollmentStatsByClass() {
        try {
            List<Map<String, Object>> stats = statisticsService.getEnrollmentStatsByClass();
            return Result.success(stats, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 按课程类型统计
     */
    @GetMapping("/course/type")
    @RoleRequire({"ADMIN"})
    public Result<List<Map<String, Object>>> getCourseTypeStats() {
        try {
            List<Map<String, Object>> stats = statisticsService.getCourseTypeStats();
            return Result.success(stats, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 学生选课情况统计
     */
    @GetMapping("/student/enrollment")
    @RoleRequire({"ADMIN"})
    public Result<List<Map<String, Object>>> getStudentEnrollmentStats() {
        try {
            List<Map<String, Object>> stats = statisticsService.getStudentEnrollmentStats();
            return Result.success(stats, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 教师开课情况统计
     */
    @GetMapping("/teacher/course")
    @RoleRequire({"ADMIN"})
    public Result<List<Map<String, Object>>> getTeacherCourseStats() {
        try {
            List<Map<String, Object>> stats = statisticsService.getTeacherCourseStats();
            return Result.success(stats, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}