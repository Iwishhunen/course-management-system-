package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 课程类型基本信息表
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@Getter
@Setter
@TableName("course_categories")
public class CourseCategories implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("type_code")
    private String typeCode;

    @TableField("type_name")
    private String typeName;

    @TableField("credit_requirement")
    private BigDecimal creditRequirement;
}
