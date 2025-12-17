package com.example.backend.mapper;

import com.example.backend.entity.Admin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 管理员基本信息表 Mapper 接口
 * </p>
 *
 * @author Iwishhunnen
 * @since 2025-12-06
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

}
