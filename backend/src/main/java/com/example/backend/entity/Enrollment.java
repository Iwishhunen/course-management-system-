package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 选课记录基本信息表
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@Getter
@Setter
@TableName("enrollment")
public class Enrollment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "enrollment_id", type = IdType.AUTO)
    private Integer enrollmentId;

    @TableField("student_id")
    private String studentId;

    @TableField("course_id")
    private String courseId;

    @TableField("enrollment_time")
    private LocalDateTime enrollmentTime;

    @TableField("score")
    private BigDecimal score;
}
