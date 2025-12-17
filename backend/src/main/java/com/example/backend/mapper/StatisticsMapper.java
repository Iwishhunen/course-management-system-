package com.example.backend.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 统计数据访问接口
 */
public interface StatisticsMapper {

    /**
     * 查询选课统计视图数据
     * @return 选课统计数据列表
     */
    @Select("SELECT * FROM EnrollmentStatsView")
    List<Map<String, Object>> selectEnrollmentStats();

    /**
     * 查询课程类型统计视图数据
     * @return 课程类型统计数据列表
     */
    @Select("SELECT * FROM CourseTypeStatsView")
    List<Map<String, Object>> selectCourseTypeStats();
}