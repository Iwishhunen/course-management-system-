create table student(
    student_id VARCHAR(10) not null,
    name VARCHAR(20) not null,
    gender CHAR(2) check ( gender IN ('男','女') ),
    major VARCHAR(50) not null ,
    classID VARCHAR(20) not null ,
    email VARCHAR(50) not null ,
    password VARCHAR(128) not null,

    PRIMARY KEY (student_id),
    UNIQUE KEY uk_student_email(email),
    INDEX idx_student_major(major),
    INDEX idx_student_class(classID)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='学生基本信息表';
create table teacher(
    teacher_id VARCHAR(10) not null ,
    name VARCHAR(50) not null ,
    title VARCHAR(20) ,
    email VARCHAR(50) not null ,
    password VARCHAR(128) not null ,

    PRIMARY KEY (teacher_id),
    UNIQUE KEY uk_teacher_email(email)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='教师基本信息表';
create table admin(
    admin_id VARCHAR(10) not null ,
    name VARCHAR(50) not null ,
    password VARCHAR(128) not null ,

    PRIMARY KEY (admin_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='管理员基本信息表';
create table course(
    course_id VARCHAR(10) not null ,
    course_name VARCHAR(100) not null ,
    credit DECIMAL(3,1) not null ,
    max_students INT(180) not null ,
    teacher_id VARCHAR(10) not null,
    type_code VARCHAR(20) COMMENT '课程类型代码',
    PRIMARY KEY (course_id),
    CONSTRAINT fk_course_type FOREIGN KEY (type_code)
        REFERENCES course_categories(type_code) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id),
    -- 为常用查询字段添加索引
    INDEX idx_course_name (course_name),
    INDEX idx_course_teacher (teacher_id),
    INDEX idx_course_type (type_code),
    INDEX idx_course_max_students (max_students),
    CONSTRAINT chk_course_credit CHECK (credit > 0 ),  -- 假设学分范围
    CONSTRAINT chk_course_max_students CHECK (max_students > 0 AND max_students <= 180)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='课程基本信息表';
-- 为course表添加type_code字段
ALTER TABLE course ADD COLUMN type_code VARCHAR(20);

-- 添加外键约束
ALTER TABLE course ADD CONSTRAINT fk_course_type
FOREIGN KEY (type_code) REFERENCES course_categories(type_code);

-- 添加索引以提高查询性能
CREATE INDEX idx_course_type ON course(type_code);
create table schedule(
    schedule_id INT(10) not null ,
    course_id VARCHAR(10) not null ,
    week_day INT(2) not null ,
    start_time TIME(6) not null ,
    end_time TIME(6) not null ,
    start_week INT(2) not null DEFAULT 1,
    end_week INT(2) not null DEFAULT 16,
    location VARCHAR(50) not null ,

    PRIMARY KEY (schedule_id),
    FOREIGN KEY (course_id) REFERENCES course(course_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='上课安排基本信息表';
CREATE TABLE Enrollment (
    enrollment_id INT(10) AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(10) NOT NULL,
    course_id VARCHAR(10) NOT NULL,
    enrollment_time DATETIME NOT NULL,

    score DECIMAL(5,2) NULL,

    -- 添加外键约束
    FOREIGN KEY (student_id) REFERENCES student(student_id),
    FOREIGN KEY (course_id) REFERENCES course(course_id),

    -- 添加唯一约束，防止重复选课
    UNIQUE KEY unique_enrollment (student_id, course_id),

    -- 添加索引以提高查询性能
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id),
    INDEX idx_enrollment_time (enrollment_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='选课记录基本信息表';

CREATE TABLE course_session (
    session_id INT(10) AUTO_INCREMENT PRIMARY KEY,
    course_id VARCHAR(10) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    location VARCHAR(50) NOT NULL,
    FOREIGN KEY (course_id) REFERENCES course(course_id),
    INDEX idx_course_session_course (course_id),
    INDEX idx_course_session_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='课程课次信息表';
CREATE TABLE EnrollmentPeriod (
    period_id INT(20) AUTO_INCREMENT PRIMARY KEY,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,

    -- 添加索引以提高查询性能
    INDEX idx_time_range (start_time, end_time),
    INDEX idx_is_active (is_active),

    -- 确保时间范围的合理性
    CHECK (end_time > start_time)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='选课时间段基本信息表';
CREATE TABLE course_categories (
    type_code VARCHAR(20) PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL,
    credit_requirement DECIMAL(5,2) NOT NULL,

    -- 确保学分要求为非负数
    CHECK (credit_requirement >= 0),

    -- 添加唯一约束和索引
    UNIQUE KEY unique_type_name (type_name),
    INDEX idx_credit_requirement (credit_requirement)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='课程类型基本信息表';

CREATE TABLE notice (
    notice_id INT(10) AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(2000) NOT NULL,
    target_role VARCHAR(20) NOT NULL,
    target_user_id VARCHAR(10) NULL,
    publisher_id VARCHAR(10) NULL,
    publish_time DATETIME NOT NULL,
    INDEX idx_notice_publish_time (publish_time),
    INDEX idx_notice_target_role (target_role),
    INDEX idx_notice_target_user_id (target_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='系统通知表';

CREATE VIEW StudentCourseView AS
SELECT
    s.student_id,
    s.name AS student_name,
    c.course_id,
    c.course_name,
    c.credit,
    t.name AS teacher_name,
    COUNT(e.student_id) AS enrolled_students,
    c.max_students,
    CASE
        WHEN EXISTS (
            SELECT 1 FROM Enrollment
            WHERE student_id = s.student_id AND course_id = c.course_id
        ) THEN '已选'
        ELSE '未选'
    END AS enrollment_status
FROM
    student s
    CROSS JOIN course c
    JOIN teacher t ON c.teacher_id = t.teacher_id
    LEFT JOIN Enrollment e ON c.course_id = e.course_id
GROUP BY
    s.student_id, c.course_id, c.course_name, c.credit, t.name, c.max_students;
CREATE VIEW StudentScheduleView AS
SELECT
    e.student_id,
    s.name AS student_name,
    c.course_id,
    c.course_name,
    sch.week_day,
    sch.start_time,
    sch.end_time,
    sch.location,
    t.name AS teacher_name
FROM
    Enrollment e
    JOIN student s ON e.student_id = s.student_id
    JOIN course c ON e.course_id = c.course_id
    JOIN schedule sch ON c.course_id = sch.course_id
    JOIN teacher t ON c.teacher_id = t.teacher_id
ORDER BY
    sch.week_day, sch.start_time;
CREATE VIEW TeacherCourseView AS
SELECT
    t.teacher_id,
    t.name AS teacher_name,
    c.course_id,
    c.course_name,
    c.credit,
    c.max_students,
    COUNT(e.student_id) AS current_enrollment,
    sch.week_day,
    sch.start_time,
    sch.end_time,
    sch.location
FROM
    teacher t
    JOIN course c ON t.teacher_id = c.teacher_id
    LEFT JOIN Enrollment e ON c.course_id = e.course_id
    LEFT JOIN schedule sch ON c.course_id = sch.course_id
GROUP BY
    t.teacher_id, c.course_id, sch.schedule_id;
CREATE VIEW CourseStudentListView AS
SELECT
    c.course_id,
    c.course_name,
    e.student_id,
    s.name AS student_name,
    s.major,
    s.classID,
    e.enrollment_time
FROM
    course c
    JOIN Enrollment e ON c.course_id = e.course_id
    JOIN student s ON e.student_id = s.student_id
ORDER BY
    c.course_id, s.classID, s.student_id;
CREATE VIEW EnrollmentStatsView AS
SELECT
    c.course_id,
    c.course_name,
    t.name AS teacher_name,
    c.credit,
    c.max_students,
    COUNT(e.student_id) AS enrolled_count,
    ROUND((COUNT(e.student_id) * 100.0 / c.max_students), 2) AS enrollment_rate,
    CASE
        WHEN COUNT(e.student_id) < 20 THEN '人数不足'
        ELSE '正常'
    END AS status
FROM
    course c
    JOIN teacher t ON c.teacher_id = t.teacher_id
    LEFT JOIN Enrollment e ON c.course_id = e.course_id
GROUP BY
    c.course_id, c.course_name, t.name, c.credit, c.max_students
ORDER BY
    enrollment_rate DESC;
CREATE VIEW ScheduleConflictView AS
SELECT
    e1.student_id,  -- 修正：使用 e1.student_id 而不是 s1.student_id
    st.name AS student_name,
    c1.course_name AS course1,
    c2.course_name AS course2,
    s1.week_day,
    s1.start_time,
    s1.end_time,
    s2.start_time AS conflict_start_time,
    s2.end_time AS conflict_end_time,
    s1.location AS location1,
    s2.location AS location2
FROM
    Enrollment e1
    JOIN Enrollment e2 ON e1.student_id = e2.student_id AND e1.course_id < e2.course_id
    JOIN schedule s1 ON e1.course_id = s1.course_id
    JOIN schedule s2 ON e2.course_id = s2.course_id
    JOIN student st ON e1.student_id = st.student_id
    JOIN course c1 ON e1.course_id = c1.course_id
    JOIN course c2 ON e2.course_id = c2.course_id
WHERE
    s1.week_day = s2.week_day
    AND (
        (s1.start_time < s2.end_time AND s1.end_time > s2.start_time)
    );
CREATE VIEW EnrollmentPeriodStatusView AS
SELECT
    period_id,
    start_time,
    end_time,
    is_active,
    CASE
        WHEN NOW() BETWEEN start_time AND end_time THEN '进行中'
        WHEN NOW() < start_time THEN '未开始'
        ELSE '已结束'
    END AS current_status
FROM
    EnrollmentPeriod
ORDER BY
    start_time;
CREATE VIEW CourseDetailView AS
SELECT
    c.course_id,
    c.course_name,
    c.credit,
    c.max_students,
    t.teacher_id,
    t.name AS teacher_name,
    t.title AS teacher_title,
    c.type_code,
    cat.type_name AS course_type,
    cat.credit_requirement,
    COUNT(DISTINCT e.student_id) AS current_enrollment,
    c.max_students - COUNT(DISTINCT e.student_id) AS available_seats,
    ROUND((COUNT(DISTINCT e.student_id) * 100.0 / c.max_students), 2) AS enrollment_rate,
    sch.week_day,
    sch.start_time,
    sch.end_time,
    sch.location
FROM
    course c
    JOIN teacher t ON c.teacher_id = t.teacher_id
    LEFT JOIN course_categories cat ON c.type_code = cat.type_code
    LEFT JOIN Enrollment e ON c.course_id = e.course_id
    LEFT JOIN schedule sch ON c.course_id = sch.course_id
GROUP BY
    c.course_id, c.course_name, c.credit, c.max_students,
    t.teacher_id, t.name, t.title, c.type_code, cat.type_name, cat.credit_requirement,
    sch.week_day, sch.start_time, sch.end_time, sch.location;
CREATE VIEW CourseSummaryView AS
SELECT
    c.course_id,
    c.course_name,
    c.credit,
    c.max_students,
    t.teacher_id,
    t.name AS teacher_name,
    t.title AS teacher_title,
    c.type_code,
    cat.type_name AS course_type,
    cat.credit_requirement,
    COUNT(DISTINCT e.student_id) AS current_enrollment,
    c.max_students - COUNT(DISTINCT e.student_id) AS available_seats,
    ROUND((COUNT(DISTINCT e.student_id) * 100.0 / c.max_students), 2) AS enrollment_rate,
    GROUP_CONCAT(
        DISTINCT CONCAT(
            '周',
            CASE sch.week_day
                WHEN 1 THEN '一' WHEN 2 THEN '二' WHEN 3 THEN '三'
                WHEN 4 THEN '四' WHEN 5 THEN '五' WHEN 6 THEN '六'
                WHEN 7 THEN '日' ELSE ''
            END,
            ' ',
            TIME_FORMAT(sch.start_time, '%H:%i'),
            '-',
            TIME_FORMAT(sch.end_time, '%H:%i'),
            ' @',
            sch.location
        )
        ORDER BY sch.week_day, sch.start_time
        SEPARATOR '; '
    ) AS schedule_info,
    GROUP_CONCAT(
        DISTINCT CONCAT(sch.week_day, '-', TIME_FORMAT(sch.start_time, '%H%i'))
        ORDER BY sch.week_day, sch.start_time
        SEPARATOR ','
    ) AS schedule_sort_key
FROM
    course c
    JOIN teacher t ON c.teacher_id = t.teacher_id
    LEFT JOIN course_categories cat ON c.type_code = cat.type_code
    LEFT JOIN Enrollment e ON c.course_id = e.course_id
    LEFT JOIN schedule sch ON c.course_id = sch.course_id
GROUP BY
    c.course_id, c.course_name, c.credit, c.max_students,
    t.teacher_id, t.name, t.title, c.type_code, cat.type_name, cat.credit_requirement;
CREATE VIEW CourseTypeStatsView AS
SELECT
    cat.type_code,
    cat.type_name,
    cat.credit_requirement,
    COUNT(DISTINCT c.course_id) AS course_count,
    IFNULL(SUM(c.credit), 0) AS total_credits,
    COUNT(DISTINCT e.student_id) AS total_enrollments,
    ROUND(
        IFNULL(
            AVG(
                (SELECT COUNT(DISTINCT e2.student_id)
                 FROM Enrollment e2
                 WHERE e2.course_id = c.course_id) * 100.0 /
                NULLIF(c.max_students, 0)
            ),
        0),
    2) AS avg_enrollment_rate
FROM
    course_categories cat
    LEFT JOIN course c ON cat.type_code = c.type_code
    LEFT JOIN Enrollment e ON c.course_id = e.course_id
GROUP BY
    cat.type_code, cat.type_name, cat.credit_requirement;

-- 插入测试数据
-- 插入课程类型数据
INSERT INTO course_categories (type_code, type_name, credit_requirement) VALUES
('REQUIRED', '必修课', 10.0),
('ELECTIVE', '选修课', 6.0),
('GENERAL', '通识课', 4.0);

-- 插入学生测试数据
INSERT INTO student (student_id, name, gender, major, classID, email, password) VALUES
('2021001001', '张三', '男', '计算机科学与技术', '计科2101', 'zhangsan@example.com', '123456'),
('2021001002', '李四', '女', '软件工程', '软工2101', 'lisi@example.com', '123456'),
('2021001003', '王五', '男', '信息安全', '信安2101', 'wangwu@example.com', '123456');

-- 插入教师测试数据
INSERT INTO teacher (teacher_id, name, title, email, password) VALUES
('T001', '陈教授', '教授', 'chen@university.edu', '123456'),
('T002', '刘副教授', '副教授', 'liu@university.edu', '123456'),
('T003', '赵讲师', '讲师', 'zhao@university.edu', '123456');

-- 插入管理员测试数据
INSERT INTO admin (admin_id, name, password) VALUES
('A001', '管理员甲', '123456'),
('A002', '管理员乙', '123456');

-- 插入选课时间段数据
INSERT INTO EnrollmentPeriod (start_time, end_time, is_active) VALUES
('2025-12-01 00:00:00', '2025-12-31 23:59:59', TRUE);
