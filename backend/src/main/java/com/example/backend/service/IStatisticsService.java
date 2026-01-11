package com.example.backend.service;

import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface IStatisticsService {

    /**
     * 按课程统计选课情况
     * @return 选课统计列表
     */
    List<Map<String, Object>> getEnrollmentStatsByCourse();

    /**
     * 按班级统计选课情况
     * @return 班级选课统计列表
     */
    List<Map<String, Object>> getEnrollmentStatsByClass();

    /**
     * 按课程类型统计
     * @return 课程类型统计列表
     */
    List<Map<String, Object>> getCourseTypeStats();

    /**
     * 学生选课情况统计
     * @return 学生选课统计列表
     */
    List<Map<String, Object>> getStudentEnrollmentStats();

    /**
     * 教师开课情况统计
     * @return 教师开课统计列表
     */
    List<Map<String, Object>> getTeacherCourseStats();
}