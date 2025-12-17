package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 管理员基本信息表
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@Getter
@Setter
@TableName("admin")
public class Admin implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("admin_id")
    private String adminId;

    @TableField("name")
    private String name;

    @TableField("password")
    private String password;
}
