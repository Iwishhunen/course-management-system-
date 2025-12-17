package com.example.backend.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 课程基本信息表
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@Getter
@Setter
@TableName("course")
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("course_id")
    private String courseId;

    @TableField("course_name")
    private String courseName;

    @TableField("credit")
    private BigDecimal credit;

    @TableField("max_students")
    private Integer maxStudents;

    @TableField("teacher_id")
    private String teacherId;

    /**
     * 课程类型代码
     */
    @TableField("type_code")
    private String typeCode;
}
