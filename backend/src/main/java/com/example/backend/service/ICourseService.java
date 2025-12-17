package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Course;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 课程基本信息表 服务类
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
public interface ICourseService extends IService<Course> {

    /**
     * 根据课程编号查找课程
     * @param courseId 课程编号
     * @return 课程对象
     */
    Course findByCourseId(String courseId);

    /**
     * 根据教师工号查找课程
     * @param teacherId 教师工号
     * @return 课程列表
     */
    List<Course> findByTeacherId(String teacherId);

    /**
     * 查找选课人数已满的课程
     * @return 课程列表
     */
    List<Course> findFullCourses();

    /**
     * 查找选课人数不足的课程(少于20人)
     * @return 课程列表
     */
    List<Course> findLowEnrollmentCourses();

    /**
     * 取消选课人数不足的课程
     * @return 被取消的课程列表
     */
    List<Course> cancelLowEnrollmentCourses();

    /**
     * 定时检查并取消选课人数不足的课程
     */
    void scheduledCancelLowEnrollmentCourses();

    /**
     * 发送课程取消通知给相关师生
     * @param course 被取消的课程
     */
    void sendCancellationNotification(Course course);
    
    /**
     * 获取带选课状态的课程列表
     * @param studentId 学生ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param courseName 课程名称过滤
     * @param teacherName 教师名称过滤
     * @param courseType 课程类型过滤
     * @return 带选课状态的课程分页列表
     */
    Page<Map<String, Object>> getCourseListWithEnrollmentStatus(
        String studentId, 
        Integer pageNum, 
        Integer pageSize, 
        String courseName, 
        String teacherName, 
        String courseType);
}
