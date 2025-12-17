package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.CourseSession;
import com.example.backend.mapper.CourseSessionMapper;
import com.example.backend.service.ICourseSessionService;
import org.springframework.stereotype.Service;

@Service
public class CourseSessionServiceImpl extends ServiceImpl<CourseSessionMapper, CourseSession>
        implements ICourseSessionService {

}
