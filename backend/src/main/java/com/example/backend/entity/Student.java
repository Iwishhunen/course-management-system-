package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 学生基本信息表
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@Getter
@Setter
@TableName("student")
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("student_id")
    private String studentId;

    @TableField("name")
    private String name;

    @TableField("gender")
    private String gender;

    @TableField("major")
    private String major;

    @TableField("classID")
    private String classId;

    @TableField("email")
    private String email;

    @TableField("password")
    private String password;
}