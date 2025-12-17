|      |      |      |      |      |      |      |      |      |      |      |      |      |
| ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- |
|      |      |      |      |      |      |      |      |      |      |      |      |      |
|      |      |      |      |      |      |      |      |      |      |      |      |      |
|      |      |      |      |      |      |      |      |      |      |      |      |      |
|      |      |      |      |      |      |      |      |      |      |      |      |      |



# 武汉理工大学数据库系统综合实验-学生选课系统

## 创建数据库(student_course_selection_db)

### 创建表

**学生表（Student）**

| 字段名     | 数据类型 | 长度 | 是否主键 | 是否为空 |         说明         |
| ---------- | -------- | ---- | -------- | -------- | :------------------: |
| student_id | VARCHAR  | 10   | 是       | 否       |  学号，唯一标识学生  |
| name       | VARCHAR  | 50   | 否       | 否       |       学生姓名       |
| gender     | CHAR     | 2    | 否       | 是       |         性别         |
| major      | VARCHAR  | 50   | 否       | 是       |         专业         |
| class      | VARCHAR  | 20   | 否       | 是       |         班级         |
| email      | VARCHAR  | 50   | 否       | 是       |   邮箱（用于通知）   |
| password   | VARCHAR  | 128  | 否       | 否       | 登录密码（加密存储） |

**教师表（Teacher）**

| 字段名     | 数据类型 | 长度 | 是否主键 | 是否为空 | 说明                 |
| ---------- | -------- | ---- | -------- | -------- | -------------------- |
| teacher_id | VARCHAR  | 10   | 是       | 否       | 工号，唯一标识教师   |
| name       | VARCHAR  | 50   | 否       | 否       | 教师姓名             |
| title      | VARCHAR  | 20   | 否       | 是       | 职称                 |
| email      | VARCHAR  | 50   | 否       | 是       | 邮箱                 |
| password   | VARCHAR  | 128  | 否       | 否       | 登录密码（加密存储） |

**系统管理员表（Admin）**

| 字段名   | 数据类型 | 长度 | 是否主键 | 是否为空 | 说明                 |
| -------- | -------- | ---- | -------- | -------- | -------------------- |
| admin_id | VARCHAR  | 10   | 是       | 否       | 管理员ID             |
| name     | VARCHAR  | 50   | 否       | 否       | 管理员姓名           |
| password | VARCHAR  | 128  | 否       | 否       | 登录密码（加密存储） |

**课程表（Course）**

| 字段名       | 数据类型 | 长度 | 是否主键 | 是否为空 | 说明                 |
| ------------ | -------- | ---- | -------- | -------- | -------------------- |
| course_id    | VARCHAR  | 10   | 是       | 否       | 课程编号             |
| course_name  | VARCHAR  | 100  | 否       | 否       | 课程名称             |
| credit       | INT      | 3    | 否       | 否       | 学分                 |
| max_students | INT      | 180  | 否       | 否       | 最大选课人数         |
| teacher_id   | VARCHAR  | 10   | 否       | 否       | 主讲教师工号（外键） |

**上课安排表（Schedule）**

| 字段名      | 数据类型 | 长度 | 是否主键 | 是否为空 | 说明               |
| ----------- | -------- | ---- | -------- | -------- | ------------------ |
| schedule_id | INT      | 10   | 是       | 否       | 上课安排ID（自增） |
| course_id   | VARCHAR  | 10   | 否       | 否       | 课程编号（外键）   |
| week_day    | INT      | 2    | 否       | 否       | 星期几（1-7）      |
| start_time  | TIME     | 20   | 否       | 否       | 开始时间           |
| end_time    | TIME     | 20   | 否       | 否       | 结束时间           |
| location    | VARCHAR  | 50   | 否       | 是       | 教室地点           |

**选课记录表（Enrollment)**

| 字段名          | 数据类型 | 长度 | 是否主键 | 是否为空 | 说明               |
| --------------- | -------- | ---- | -------- | -------- | ------------------ |
| enrollment_id   | INT      | 10   | 是       | 否       | 选课记录ID（自增） |
| student_id      | VARCHAR  | 10   | 否       | 否       | 学生ID（外键）     |
| course_id       | VARCHAR  | 10   | 否       | 否       | 课程ID（外键）     |
| enrollment_time | DATETIME | 20   | 否       | 否       | 选课时间           |

**选课时间段表（EnrollmentPeriod）**

| 字段名     | 数据类型 | 长度 | 是否主键 | 是否为空 | 说明             |
| ---------- | -------- | ---- | -------- | -------- | ---------------- |
| period_id  | INT      | 20   | 是       | 否       | 时间段ID（自增） |
| start_time | DATETIME | 20   | 否       | 否       | 选课开始时间     |
| end_time   | DATETIME | 20   | 否       | 否       | 选课结束时间     |
| is_active  | BOOLEAN  | 3    | 否       | 否       | 是否当前有效     |

**课程类型表（course_categories)**

| 字段名             | 数据类型 | 长度  | 是否主键 | 是否为空 | 说明     |
| ------------------ | -------- | ----- | -------- | -------- | -------- |
| type_code          | VARCHAR  | 20    | 是       | 否       | 类型代码 |
| type_name          | VARCHAR  | 100   | 否       | 否       | 类型名称 |
| credit_requirement | DECIMAL  | (5,2) | 否       | 否       | 学分要求 |

教学管理信息系统
——学生选课及课程安排数据库综合实验
1	需求分析
1.1	任务描述
大学同时开设多门课程。每门课程有一个主讲教师，有多名学生选修；一个学生可选修多门课程并获得相应的学分和成绩；上课的基本单位是“次”（一次2学时），每一次课都规定了上课时间和教室
1.1.1	系统目标
(1)	实用性原则：真正的为学生的实际选课服务，按照学生选课及课程安排的实际流程，设计出实用的学生选课系统。
(2)	可靠性原则：首先要确保该系统没有逻辑冲突（例如一个学生在同一时间段选了两门课）；其次要将用户角色分开，分为三类，老师、学生、管理员，分别给予不同的权限。
(3)	友好性原则：本学生选课系统面向的用户是老师和学生，所以系统操作上要求简单、方便、便于用户使用。
(4)	可扩展性原则：采用开发的标准和接口，便于系统向更大的规模和功能扩展。
1.1.2	系统的功能需求
根据学生选课系统的理念，我以用户角色为分类，分析该系统需要满足的需求：
 (1)	系统管理员
管理员维护整个系统，设置选课时段：
选课前：学生可以登录但无法选课，同时发布课程和教师的基本情况。
选课时：A.限制最大选课人数，防止系统崩溃；B.数据备份和恢复等。
选课后：A.学生只能查询，管理员对选课结果进行统计；B.取消选课人数不足20人的课程，并发送邮件通知教师和学生。
（2）教师
查询课程的基本情况、学生情况。
（3）学生
选课前：在选课系统中查询课程、教师信息。
选课时：提交选课申请，撤销申请，查询选课情况，进行个人信息修改。
选课后：查看课表。
1.1.3	系统的非功能性需求
(1)	性能需求：为了不耽误学生和老师的时间，页面加载的时间不应超过3秒。
(2)	可靠性需求：数据不能出现错误、丢失。
(3)	易用性需求：操作不应做的过于复杂，同时按钮的功能应该简单明了。
(4)	可扩展性需求：系统应该具有可扩展性，若想添加其他功能不需要修改整个系统。
1.2	需求分析
1.2.1	数据字典
 (1)	数据项的描述
a. 学生表（Student）
表格 1.1-学生表
字段名	数据类型	长度	是否主键	是否为空	说明
student_id	VARCHAR	10	是	否	学号，唯一标识学生
name	VARCHAR	50	否	否	学生姓名
gender	CHAR	2	否	是	性别
major	VARCHAR	50	否	是	专业
class	VARCHAR	20	否	是	班级
email	VARCHAR	50	否	是	邮箱（用于通知）
password	VARCHAR	128	否	否	登录密码（加密存储）
   b.教师表（Teacher）
表格 1.2-教师表
字段名	数据类型	长度	是否主键	是否为空	说明
teacher_id	VARCHAR	10	是	否	工号，唯一标识教师
name	VARCHAR	50	否	否	教师姓名
title	VARCHAR	20	否	是	职称
email	VARCHAR	50	否	是	邮箱
password	VARCHAR	128	否	否	登录密码（加密存储）
  c.系统管理员表（Admin）
表格 1.3-系统管理员表
字段名	数据类型	长度	是否主键	是否为空	说明
admin_id	VARCHAR	10	是	否	管理员ID
name	VARCHAR	50	否	否	管理员姓名
password	VARCHAR	128	否	否	登录密码（加密存储）
  d.课程表（Course）
表格 1.4-课程表
字段名	数据类型	长度	是否主键	是否为空	说明
course_id	VARCHAR	10	是	否	课程编号
course_name	VARCHAR	100	否	否	课程名称
credit	INT	3	否	否	学分
max_students	INT	180	否	否	最大选课人数
teacher_id	VARCHAR	10	否	否	主讲教师工号（外键）
  e.上课安排表（Schedule）
表格 1.5-上课安排表
字段名	数据类型	长度	是否主键	是否为空	说明
schedule_id	INT	10	是	否	上课安排ID（自增）
course_id	VARCHAR	10	否	否	课程编号（外键）
week_day	INT	2	否	否	星期几（1-7）
start_time	TIME	20	否	否	开始时间
end_time	TIME	20	否	否	结束时间
location	VARCHAR	50	否	是	教室地点
  f.选课记录表
表格 1.6-选课记录表
字段名	数据类型	长度	是否主键	是否为空	说明
enrollment_id	INT	10	是	否	选课记录ID（自增）
student_id	VARCHAR	10	否	否	学生ID（外键）
course_id	VARCHAR	10	否	否	课程ID（外键）
enrollment_time	DATETIME	20	否	否	选课时间

f.选课时间段表（EnrollmentPeriod）
表格 1.7-选课时间段表
字段名	数据类型	长度	是否主键	是否为空	说明
period_id	INT	20	是	否	时间段ID（自增）
start_time	DATETIME	20	否	否	选课开始时间
end_time	DATETIME	20	否	否	选课结束时间
is_active	BOOLEAN	3	否	否	是否当前有效
g.课程类型表（course_categories）
表格 1.8-课程类型表
字段名	数据类型	长度	是否主键	是否为空	说明
type_code	VARCHAR	20	是	否	类型代码
type_name	VARCHAR	100	否	否	类型名称
credit_requirement	DECIMAL	(5,2)	否	否	学分要求

(2)	数据流的描述
数据流编号：F1
数据流名称：选课申请
简述：学生提交选课请求
数据流来源：学生
数据流去向：选课系统
数据流组成：学生ID、课程ID、申请时间
数据流量：5000/天
高峰流量：20000/天

数据流编号：F2
数据流名称：选课结果
简述：返回选课成功或失败的结果
数据流来源：选课系统
数据流去向：学生
数据流组成：学生ID、课程ID、选课状态（成功/失败），失败原因
数据流量：5000/天
高峰流量：20000/天

数据流编号：F3
数据流名称：课程查询请求
简述：学生或老师查询课程信息
数据流来源：学生/老师
数据流去向：选课系统
数据流组成：查询条件（课程名、教师、时间等）
数据流量：3000/天
高峰流量：30000/天
数据流编号：F4
数据流名称：课程信息
简述：返回课程详细信息
数据流来源：选课系统
数据流去向：学生/老师
数据流组成：课程ID、课程名称、学分、教师、上课时间、地点、已选人数、最大人数
数据流量：3000/天
高峰流量：20000/天

数据流编号：F5
数据流名称：选课统计请求
简述：管理员请求选课统计信息
数据流来源：管理员
数据流去向：选课系统
数据流组成：统计条件（如按课程、按班级等）
数据流量：10/天
高峰流量：50/天

数据流编号：F6
数据流名称：选课统计结果
简述：返回选课统计结果
数据流来源：选课系统
数据流去向：管理员
数据流组成：课程ID，课程名称，选课人数，统计时间
数据流量：10/天
高峰流量：50/天

数据流编号：F7
数据流名称：课程取消通知
简述：通知教师和学生课程取消
数据流来源：选课系统
数据流去向：教师，学生
数据流组成：课程ID，课程名称，取消原因，通知时间
数据流量：5/天
高峰流量：30/天
(3)	处理逻辑的描述
处理逻辑编号： P1
处理逻辑名称： 选课处理
简述： 处理学生的选课申请
输入的数据流： 选课申请（F1）
处理描述： 验证选课资格，然后处理选课
输出的数据流： 选课结果（F2）
处理频率： 5000次/天

处理逻辑编号： P2
处理逻辑名称： 课程查询处理
简述： 处理课程查询请求
输入的数据流： 课程查询请求（3）
处理描述： 根据查询条件从课程数据存储中检索课程信息
输出的数据流： 课程信息（F4）
处理频率： 3000次/天

处理逻辑编号： P3
处理逻辑名称： 选课统计处理
简述： 处理选课统计请求
输入的数据流： 选课统计申请（F5）
处理描述： 根据统计条件，从选课记录和课程数据中统计选课信息
输出的数据流： 选课统计结果（F6）
处理频率： 10次/天

处理逻辑编号： P4
处理逻辑名称： 课程取消处理
简述： 处理选课人数不足的课程取消
输入的数据流： 选课统计结果
处理描述：检查选课人数不足20人的课程，生成取消通知，并更新课程状态 
输出的数据流： 选课取消通知（F7）
处理频率： 1次/选课阶段
(4)	数据存储的描述
数据存储编号：D1
数据存储名称：学生信息
简述：存储学生的基本信息
数据存储组成：学生ID，姓名，性别，专业，班级，邮箱，密码
关键字：学生ID
相关联的处理：P1

数据存储编号：D2
数据存储名称：课程信息
简述：存储课程的基本信息
数据存储组成：课程ID，课程名称，学分，最大人数，教师ID 
关键字：课程ID
相关联的处理：P1，P2，P3

数据存储编号：D3
数据存储名称：选课记录 
简述：存储学生选课记录
数据存储组成：选课记录，学生ID，课程ID，成绩，选课时间 
关键字：选课记录 ID
相关联的处理：P1，P3

数据存储编号：D4
数据存储名称：上课安排
简述：存储课程的上课时间安排
数据存储组成：安排ID，课程ID，星期，开始时间，结束时间，教室 
关键字：安排ID
相关联的处理：P1，P2

数据存储编号：D5
数据存储名称：教师信息
简述：存储教师的基本信息
数据存储组成：教师ID，姓名，职称，邮箱，密码
关键字：教师ID
相关联的处理：P2

数据存储编号：D6
数据存储名称：选课时间段
简述：存储选课的时间段设置
数据存储组成：时间段ID，开始时间，结束时间，是否有效 
关键字：时间段ID
相关联的处理：P1
1.2.2	数据流图
教务系统选课管理子系统数据流图如图1.1所示：

2	数据库设计
2.1	概念结构设计
将需求分析得到的用户需求抽象为信息结构即概念模型的过程就是概念结构设计。
根据需求分析形成的数据字典和数据流图，抽象得到的实体有：
	学生（学号，姓名，性别，专业，班级，邮箱，密码）
教师（工号，姓名，职称，邮箱，密码）
管理员（管理员ID，姓名，密码）
课程（课程编号，课程名称，学分，最大选课人数，主讲教师工号）
上课安排（安排ID，课程编号，星期几，开始时间，结束时间，教室地点）
选课时间段（时间段ID，开始时间，结束时间，是否有效）
选课记录（选课记录ID，学生ID，课程ID，选课时间）
根据实体与属性划分原则得到的实体有：
		课程类型（类型代码，类型名称，学分要求）
实体之间的联系如下：
	一个学生可以选择多门课程，一门课程可以被多个学生选择。
一个教师可以主讲多门课程，一门课程只有一个主讲教师。
一门课程可以有多个上课安排，一个上课安排只属于一门课程。
一个选课时间段内可以产生多个选课记录，一个选课记录只在一个选课时间段内产生。
一个学生可以有多条选课记录，一条选课记录只属于一个学生。
一个课程可以有多条选课记录，一条选课记录只对应一门课程。
管理员可以设置多个选课时间段，一个选课时间段只能由管理员设置。
教学管理选课子系统E-R图如图2.1所示。
2.2	逻辑结构设计
逻辑设计的任务就是把概念设计阶段设计的E-R图转换为与选用DBMS产品所支持的数据模型相符合的逻辑结构。
实体转换的关系模式有学生、教师、课程类型、课程、上课安排、选课记录、选课时间段、管理员。
"选课"联系与选课记录关系模式合并。
"主讲"联系与课程关系模式合并。
"安排"联系与上课安排关系模式合并。
"时间段控制"联系与选课记录关系模式合并。
"分类"联系与课程关系模式合并。
"设置"联系与选课时间段关系模式合并。

选课子系统的关系模式如下：
学生（学生ID，姓名，性别，专业，班级，邮箱，密码）
教师（教师ID，姓名，职称，邮箱，密码）
管理员（管理员ID，姓名，密码）
课程（课程编号，课程名称，学分，最大选课人数，教师ID，类型代码）
上课安排（安排ID，课程编号，星期几，开始时间，结束时间，教室地点）
选课记录（选课记录ID，学生ID，课程ID，选课时间，时间段ID）
选课时间段（时间段ID，开始时间，结束时间，是否有效）
课程类型（类型代码，类型名称，学分要求）

定义用户子模式如下：
学生选课视图（学生ID，姓名，课程名称，学分，教师姓名，选课状态）
课程信息视图（课程编号，课程名称，学分，教师姓名，已选人数，最大人数）
上课安排视图（课程名称，星期几，开始时间，结束时间，教室，教师姓名）
选课统计视图（课程编号，课程名称，选课人数，最大人数）
2.3	物理结构设计
为一个给定的逻辑数据模型选取一个最适合应用环境的物理结构的过程就是数据库的物理设计。数据库在物理设备上的存储结构与存取方法称为物理结构。
学生选课数据库的数据文件、日志文件存放到指定的硬盘上，该硬盘最好不安装操作系统、DBMS等软件，数据库备份文件存放到移动硬盘。
根据处理需求，建立相关索引，如表3.1所示：
表格 2.1
关系模式	索引属性列	索引类型
学生	姓名	B树索引
选课记录	选课时间(降序)	B树索引
选课记录	(学生ID, 课程ID)	唯一索引
课程	课程名称	B树索引
上课安排	(星期几, 开始时间)	复合索引
选课时间段	是否有效	哈希索引
3	数据库实施
3.1.1	MySQL8.0安装
MySQL安装向导提供了清晰直观的流程来安装核心组件：
核心服务器组件
(1)	MySQL Server： 数据库引擎本身，是核心服务。
(2)	MySQL Router： 轻量级中间件，提供应用程序与后端MySQL服务器之间的透明路由。
(3)	MySQL Shell： 高级的客户端和代码编辑器，支持SQL、Python和JavaScript。
(4)	MySQL Workbench： 官方的可视化数据库设计、管理、开发和迁移工具。
(5)	MySQL Connectors： 提供用于不同编程语言（如J/DBI, ODBC, .NET, Python, Java等）连接MySQL的驱动。
(6)	示例和文档： 包含示例数据库、帮助文档和手册。
MySQL安装程序所需的软件组件：
(1)	Microsoft Visual C++ Redistributable： 运行MySQL所必需的组件（如2015, 2017, 2019等版本），安装程序通常会自动检测并安装。
(2)	.NET Framework 4.5.2 或更高版本： 运行MySQL Workbench和部分管理工具所必需。
MySQL 8.0所需的网络协议：
(1)	TCP/IP： 默认且最主要的通信协议。
(2)	Socket（Unix/Linux系统）或Named Pipes/Shared Memory（Windows系统）： 用于本地连接。
MySQL 8.0所需的软件：
(1)	现代网页浏览器（如Chrome, Firefox, Edge）： 用于访问在线文档和某些Web组件。
(2)	对于图形化界面，需要Windows 7或更高版本，或支持X11的Linux/Unix系统。
MySQL 8.0 Community (64-bit) 所需的软硬件（最低/推荐配置）：
(1)	处理器： 64位，与Intel/AMD x86_64架构兼容 / 多核处理器。
(2)	内存： 1 GB / 4 GB 或更高，取决于数据库负载。
(3)	硬盘： 至少2 GB空闲空间 / SSD硬盘以获得最佳性能，空间根据数据量决定。
(4)	显示器： VGA 1024x768 / 更高分辨率以更好地使用Workbench。
3.1.2	Mysql 8.0配置
使用图形化实用工具和命令行工具进一步配置MySQL。
(1)	MySQL Workbench
MySQL Workbench 是官方的集成可视化环境，用于SQL开发、数据库建模、服务器配置、用户管理、数据迁移以及性能监控。
(2)	MySQL Installer（Windows）
用于安装、升级或更改MySQL产品系列中的组件。
(3)	MySQL Command-Line Client
原生命令行客户端，用于连接服务器并执行SQL语句和批处理脚本。
(4)	MySQL Configuration File (my.ini 或 my.cnf)
可以手动编辑此文本文件来配置服务器的各种参数，如端口、数据目录、内存设置、字符集等。
(5)	MySQL Administrator (已过时，功能被Workbench取代)
旧版图形化管理工具，其功能现已整合到MySQL Workbench中。
(6)	Performance Schema & sys Schema
Performance Schema用于收集数据库服务器性能数据，而sys Schema提供易于理解的视图、函数和过程，帮助用户分析Performance Schema收集的数据。
(7)	MySQL Shell & X DevAPI
MySQL Shell是一个高级交互式客户端，支持SQL、JavaScript和Python。它通过X DevAPI支持关系型和文档型（NoSQL）操作。
3.1.3	Mysql 8.0 管理
服务器管理
(1)	启动/停止服务： 可以通过Windows服务管理器、命令行（net start mysql80）或MySQL Workbench的Server Administration模块来管理MySQL服务。
(2)	配置服务器： 主要通过编辑my.ini（Windows）或my.cnf（Linux）配置文件，或使用SET命令在运行时动态调整系统变量。
管理数据库引擎服务
可以使用操作系统服务管理器、MySQL Workbench或从命令提示符处启动、暂停、停止和配置MySQL服务。
备份和还原
(1)	逻辑备份： 使用mysqldump命令行工具创建SQL语句格式的备份。使用mysql命令行工具或MySQL Workbench Data Export/Restore功能进行还原。
(2)	物理备份： 使用MySQL Enterprise Backup（商业版）进行高性能的热备份。社区版用户可使用文件系统快照（如LVM）或第三方工具（如Percona XtraBackup）。
(3)	二进制日志备份： 用于实现基于时间点的恢复。
使用MySQL Upgrade升级MySQL
MySQL提供了mysql_upgrade命令行工具（在8.0.16之前），用于在版本升级后检查并更新系统表。从8.0.16版本开始，此功能已集成到服务器中，在服务器启动时自动执行。
自动化管理
在数据库管理员的日常工作中，自动化是提高效率和可靠性的关键。MySQL通过事件调度器和外部工具支持自动化。
3.2	数据库创建
3.2.1	创建数据库
在datagrip中手动创建student_course_selection_db数据库
3.2.2	定义基本表
在student_course_selection_db数据库上，根据关系模式，定义基本表。表结构如1.2.1中数据项描述中的表相同。
3.2.3	定义视图
(1)	学生选课视图
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
(2)	学生个人课表视图
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
(3)	教师授课信息视图
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
(4)	课程学生名单视图
CREATE VIEW CourseStudentListView AS
SELECT
    c.course_id,
    c.course_name,
    e.student_id,
    s.name AS student_name,
    s.major,
    s.class,
    e.enrollment_time
FROM
    course c
    JOIN Enrollment e ON c.course_id = e.course_id
    JOIN student s ON e.student_id = s.student_id
ORDER BY
    c.course_id, s.class, s.student_id;
(5)	选课统计视图
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
(6)	课程时间冲突检测视图
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
(7)	选课时间段状态视图
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
(8)	课程详细信息视图
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
(9)	课程汇总视图
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
(10)	按课程类型统计的视图
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

   2. AS avg_enrollment_rate
  FROM
      course_categories cat
      LEFT JOIN course c ON cat.type_code = c.type_code
      LEFT JOIN Enrollment e ON c.course_id = e.course_id
  GROUP BY
      cat.type_code, cat.type_name, cat.credit_requirement;
  3.3	数据加载
  按照设计的数据结构，使用excel组织课程类型信息，课程信息，教师信息，上课安排信息，选课时间段信息，学生信息。
  使用Ms SQL Server的导入数据向导将数据加载到student_course_selection_db数据库中。

  
