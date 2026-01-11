package com.example.backend.service;

import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.Enrollment;

/**
 * <p>
 * 选课记录基本信息表 服务类
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
public interface IEnrollmentService extends IService<Enrollment> {

    /**
     * 根据学生ID查找选课记录
     * @param studentId 学生ID
     * @return 选课记录列表
     */
    List<Enrollment> findByStudentId(String studentId);

    /**
     * 根据课程ID查找选课记录
     * @param courseId 课程ID
     * @return 选课记录列表
     */
    List<Enrollment> findByCourseId(String courseId);

    /**
     * 检查学生是否已选某门课程
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否已选
     */
    boolean isEnrolled(String studentId, String courseId);

    /**
     * 学生选课
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否选课成功
     */
    boolean enrollCourse(String studentId, String courseId) throws Exception;

    /**
     * 学生退课
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否退课成功
     */
    boolean dropCourse(String studentId, String courseId);

    /**
     * 获取学生已选课程数量
     * @param studentId 学生ID
     * @return 已选课程数量
     */
    int getEnrollmentCountByStudent(String studentId);

    /**
     * 获取课程已选人数
     * @param courseId 课程ID
     * @return 已选人数
     */
    int getEnrollmentCountByCourse(String courseId);

    /**
     * 检查当前是否处于选课时间段
     * @return 是否处于选课时间段
     */
    boolean isInEnrollmentPeriod();

    /**
     * 检查课程时间是否冲突
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否时间冲突
     */
    boolean isScheduleConflict(String studentId, String courseId);

    /**
     * 检查课程是否已满
     * @param courseId 课程ID
     * @return 是否已满
     */
    boolean isCourseFull(String courseId);

    /**
     * 获取学生已选课程总学分
     * @param studentId 学生ID
     * @return 已选课程总学分
     */
    BigDecimal getTotalCreditsByStudent(String studentId);
}
