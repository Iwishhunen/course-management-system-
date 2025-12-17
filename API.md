# 课程管理系统接口文档

## 1. 基础信息

- **后端服务地址**
  - `http://localhost:8090`
- **Swagger 文档**
  - Swagger UI：`/swagger-ui.html`
  - OpenAPI JSON：`/api-docs`

## 2. 认证与鉴权

### 2.1 JWT 认证

- 除 `/auth/login` 外，其它接口默认都需要在请求头携带 Token：
  - Header：`Authorization: <token>`
  - 注意：当前实现中 **不使用** `Bearer <token>` 前缀。

### 2.2 角色鉴权（`@RoleRequire`）

- 部分接口使用 `@RoleRequire({"ADMIN" | "TEACHER" | "STUDENT"})` 进行角色限制。
- 未标注 `@RoleRequire` 的接口：
  - 仍然需要登录 Token（JWT 拦截器会拦截）。
  - 但**不做角色限制**（任何已登录用户都可能访问）。

## 3. 通用返回格式

后端统一使用 `common.Result<T>` 返回：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

- **成功**：`code = 200`
- **业务错误**：`code = 500`（通常为后端捕获异常或校验失败后返回）

### 3.1 认证/权限错误

拦截器会直接返回 HTTP 状态码，并返回 JSON：

- **未登录/Token 无效/过期**：HTTP `401`

```json
{ "code": 401, "msg": "未提供认证令牌", "data": null }
```

- **权限不足**：HTTP `403`

```json
{ "code": 403, "msg": "权限不足", "data": null }
```

## 4. 接口列表

> 说明：以下“权限”同时包含 **是否需要登录** 与 **角色限制**。

---

# 4.1 认证模块（AuthController）

## 4.1.1 登录

- **URL**：`POST /auth/login`
- **权限**：无需登录
- **请求参数**（`application/x-www-form-urlencoded` 或 query 参数）
  - `username`：账号（管理员为数字 ID；学生/教师为字符串 ID）
  - `password`：密码
  - `userType`：用户类型（`admin` / `student` / `teacher`）

- **响应数据**：`Map<String,Object>`
  - `token`：JWT token
  - `role`：`ADMIN` / `STUDENT` / `TEACHER`
  - `user`：对应用户对象（已清除 `password` 字段）

**响应示例**：

```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "<jwt>",
    "role": "STUDENT",
    "user": {
      "studentId": "2021001001",
      "name": "张三",
      "gender": "男",
      "major": "计算机科学与技术",
      "classId": "计科2101",
      "email": "zhangsan@example.com",
      "password": null
    }
  }
}
```

---

# 4.2 管理员模块（AdminController）

> Base：`/admin`

## 4.2.1 新增管理员

- **URL**：`POST /admin/add`
- **权限**：需登录，角色：`ADMIN`
- **Body**：JSON（`Admin`）

```json
{ "adminId": "A001", "name": "管理员甲", "password": "123456" }
```

## 4.2.2 删除管理员

- **URL**：`DELETE /admin/delete/{id}`
- **权限**：需登录（无角色限制）
- **Path**
  - `id`：管理员 ID

## 4.2.3 批量删除管理员

- **URL**：`DELETE /admin/delete/batch`
- **权限**：需登录（无角色限制）
- **Body**：`List<Long>`

## 4.2.4 修改管理员

- **URL**：`PUT /admin/update`
- **权限**：需登录（无角色限制）
- **Body**：JSON（`Admin`）

## 4.2.5 查询管理员（按 ID）

- **URL**：`GET /admin/get/{id}`
- **权限**：需登录（无角色限制）

## 4.2.6 管理员列表

- **URL**：`GET /admin/list`
- **权限**：需登录（无角色限制）

## 4.2.7 管理员分页

- **URL**：`GET /admin/page`
- **权限**：需登录（无角色限制）
- **Query**
  - `pageNum`：默认 1
  - `pageSize`：默认 10
  - `name`：可选
  - `account`：可选

## 4.2.8 管理员条件查询

- **URL**：`GET /admin/query`
- **权限**：需登录（无角色限制）
- **Query**
  - `key`：字段名
  - `value`：查询值
  - `exactMatch`：默认 `false`

## 4.2.9 管理员登录（注意）

- **URL**：`POST /admin/login`
- **权限**：由于 JWT 拦截器仅放行 `/auth/login`，此接口在当前配置下**可能无法在未登录时调用**。
- **建议**：统一使用 `/auth/login`。

## 4.2.10 修改密码

- **URL**：`PUT /admin/change-password`
- **权限**：需登录（无角色限制）
- **Query**
  - `id`
  - `oldPassword`
  - `newPassword`

## 4.2.11 设置选课时间段

- **URL**：`POST /admin/enrollmentPeriod`
- **权限**：需登录（无角色限制）
- **Body**

```json
{ "startTime": "2025-12-01 00:00:00", "endTime": "2025-12-31 23:59:59" }
```

## 4.2.12 获取所有选课时间段

- **URL**：`GET /admin/enrollmentPeriods`
- **权限**：需登录（无角色限制）

## 4.2.13 激活选课时间段

- **URL**：`PUT /admin/enrollmentPeriod/activate/{periodId}`
- **权限**：需登录（无角色限制）

## 4.2.14 取消激活选课时间段

- **URL**：`PUT /admin/enrollmentPeriod/deactivate/{periodId}`
- **权限**：需登录（无角色限制）

## 4.2.15 管理员仪表盘统计

- **URL**：`GET /admin/dashboard/stats`
- **权限**：需登录，角色：`ADMIN`
- **响应**：`Map<String,Object>`（具体字段以实现为准）

## 4.2.16 手动取消人数不足课程

- **URL**：`POST /admin/cancelLowEnrollmentCourses`
- **权限**：需登录，角色：`ADMIN`
- **响应**：被取消的课程列表（`List<Course>`）

---

# 4.3 学生模块（StudentController）

> Base：`/student`

## 4.3.1 新增学生

- **URL**：`POST /student/add`
- **权限**：需登录（无角色限制）
- **Body**：`Student`

## 4.3.2 删除学生

- **URL**：`DELETE /student/delete/{id}`
- **权限**：需登录（无角色限制）

## 4.3.3 批量删除学生

- **URL**：`DELETE /student/delete/batch`
- **权限**：需登录（无角色限制）
- **Body**：`List<String>`

## 4.3.4 修改学生

- **URL**：`PUT /student/update`
- **权限**：需登录（无角色限制）

## 4.3.5 查询学生（按 ID）

- **URL**：`GET /student/get/{id}`
- **权限**：需登录（无角色限制）

## 4.3.6 学生列表

- **URL**：`GET /student/list`
- **权限**：需登录（无角色限制）

## 4.3.7 学生分页

- **URL**：`GET /student/page`
- **权限**：需登录（无角色限制）
- **Query**
  - `pageNum`，`pageSize`
  - `studentName`（可选）
  - `className`（可选）

## 4.3.8 学生条件查询

- **URL**：`GET /student/query`
- **权限**：需登录（无角色限制）
- **Query**：`key` / `value` / `exactMatch`

## 4.3.9 获取学生课表

- **URL**：`GET /student/schedule/{studentId}`
- **权限**：需登录，角色：`STUDENT` / `ADMIN`
- **响应**：`List<Map<String,Object>>`（来自课表查询）

## 4.3.10 学生仪表盘统计

- **URL**：`GET /student/dashboard/stats`
- **权限**：需登录，角色：`STUDENT` / `ADMIN`
- **Query**
  - `studentId`
- **响应字段**（当前实现返回）
  - `selectedCourses`
  - `totalCredits`
  - `enrollmentPeriod`

---

# 4.4 教师模块（TeacherController）

> Base：`/teacher`

## 4.4.1 新增教师

- **URL**：`POST /teacher/add`
- **权限**：需登录（无角色限制）

## 4.4.2 删除教师

- **URL**：`DELETE /teacher/delete/{id}`
- **权限**：需登录（无角色限制）

## 4.4.3 批量删除教师

- **URL**：`DELETE /teacher/delete/batch`
- **权限**：需登录（无角色限制）

## 4.4.4 修改教师

- **URL**：`PUT /teacher/update`
- **权限**：需登录（无角色限制）

## 4.4.5 查询教师（按 ID）

- **URL**：`GET /teacher/get/{id}`
- **权限**：需登录（无角色限制）

## 4.4.6 教师列表

- **URL**：`GET /teacher/list`
- **权限**：需登录（无角色限制）

## 4.4.7 教师分页

- **URL**：`GET /teacher/page`
- **权限**：需登录（无角色限制）
- **Query**：`teacherName` / `title`

## 4.4.8 教师条件查询

- **URL**：`GET /teacher/query`
- **权限**：需登录（无角色限制）

## 4.4.9 获取教师教授课程列表

- **URL**：`GET /teacher/courses/{teacherId}`
- **权限**：需登录（无角色限制）
- **响应**：`List<Map<String,Object>>`

## 4.4.10 获取课程学生名单

- **URL**：`GET /teacher/courseStudents/{courseId}`
- **权限**：需登录（无角色限制）
- **响应**：`List<Map<String,Object>>`

## 4.4.11 教师仪表盘统计

- **URL**：`GET /teacher/dashboard/stats`
- **权限**：需登录，角色：`TEACHER` / `ADMIN`
- **Query**：`teacherId`
- **响应字段**（当前实现返回）
  - `teachingCourses`
  - `totalStudents`
  - `scheduledClasses`

## 4.4.12 教师修改密码

- **URL**：`PUT /teacher/change-password`
- **权限**：需登录（无角色限制）
- **Query**
  - `teacherId`
  - `oldPassword`
  - `newPassword`

---

# 4.5 课程模块（CourseController）

> Base：`/course`

## 4.5.1 新增课程

- **URL**：`POST /course/add`
- **权限**：需登录，角色：`ADMIN`
- **Body**：`Course`

## 4.5.2 删除课程

- **URL**：`DELETE /course/delete/{id}`
- **权限**：需登录（无角色限制）

## 4.5.3 批量删除课程

- **URL**：`DELETE /course/delete/batch`
- **权限**：需登录（无角色限制）

## 4.5.4 修改课程

- **URL**：`PUT /course/update`
- **权限**：需登录（无角色限制）

## 4.5.5 查询课程（按 ID）

- **URL**：`GET /course/get/{id}`
- **权限**：需登录（无角色限制）

## 4.5.6 课程列表

- **URL**：`GET /course/list`
- **权限**：需登录（无角色限制）

## 4.5.7 课程分页（含选课状态）

- **URL**：`GET /course/listWithEnrollment`
- **权限**：需登录（无角色限制）
- **Query**
  - `studentId`（必填）
  - `pageNum`，`pageSize`
  - `courseName`（可选）
  - `teacherName`（可选）
  - `courseType`（可选）

## 4.5.8 课程分页

- **URL**：`GET /course/page`
- **权限**：需登录（无角色限制）
- **Query**
  - `pageNum`，`pageSize`
  - `courseName`（可选）
  - `typeCode`（可选）

## 4.5.9 课程条件查询

- **URL**：`GET /course/query`
- **权限**：需登录（无角色限制）

## 4.5.10 选课结束后取消人数不足课程

- **URL**：`POST /course/postEnrollmentCancel`
- **权限**：需登录，角色：`ADMIN`

## 4.5.11 预览人数不足课程

- **URL**：`GET /course/previewLowEnrollment`
- **权限**：需登录（无角色限制）

---

# 4.6 课程类型模块（CourseCategoriesController）

> Base：`/courseCategories`

- `POST /courseCategories/add`
- `DELETE /courseCategories/delete/{id}`
- `DELETE /courseCategories/delete/batch`
- `PUT /courseCategories/update`
- `GET /courseCategories/get/{id}`
- `GET /courseCategories/list`
- `GET /courseCategories/page`
- `GET /courseCategories/query`

**权限**：以上接口均需登录（无角色限制）。

---

# 4.7 上课安排模块（ScheduleController）

> Base：`/schedule`

- `POST /schedule/add`
- `DELETE /schedule/delete/{id}`
- `DELETE /schedule/delete/batch`
- `PUT /schedule/update`
- `GET /schedule/get/{id}`
- `GET /schedule/list`
- `GET /schedule/page`
- `GET /schedule/query`

**权限**：以上接口均需登录（无角色限制）。

---

# 4.8 选课模块（EnrollmentController）

> Base：`/enrollment`

## 4.8.1 CRUD

- `POST /enrollment/add`
- `DELETE /enrollment/delete/{id}`
- `DELETE /enrollment/delete/batch`
- `PUT /enrollment/update`
- `GET /enrollment/get/{id}`
- `GET /enrollment/list`
- `GET /enrollment/page`
- `GET /enrollment/query`

**权限**：需登录（无角色限制）。

## 4.8.2 学生选课

- **URL**：`POST /enrollment/enroll`
- **权限**：需登录（无角色限制）
- **Query**
  - `studentId`
  - `courseId`

## 4.8.3 学生退课

- **URL**：`DELETE /enrollment/drop`
- **权限**：需登录（无角色限制）
- **Query**
  - `studentId`
  - `courseId`

---

# 4.9 选课时间段模块（EnrollmentperiodController）

> Base：`/enrollmentperiod`

- `POST /enrollmentperiod/add`
- `DELETE /enrollmentperiod/delete/{id}`
- `DELETE /enrollmentperiod/delete/batch`
- `PUT /enrollmentperiod/update`
- `GET /enrollmentperiod/get/{id}`
- `GET /enrollmentperiod/list`
- `GET /enrollmentperiod/page`
- `GET /enrollmentperiod/query`

**权限**：以上接口均需登录（无角色限制）。

---

# 4.10 统计分析模块（StatisticsController）

> Base：`/statistics`，**权限：需登录，角色 `ADMIN`**

- `GET /statistics/enrollment/course`：按课程统计选课情况
- `GET /statistics/enrollment/class`：按班级统计选课情况
- `GET /statistics/course/type`：按课程类型统计
- `GET /statistics/student/enrollment`：学生选课情况统计
- `GET /statistics/teacher/course`：教师开课情况统计

---

## 5. 备注与建议

- **权限标注不完整**：大量 CRUD 接口未加 `@RoleRequire`，意味着只要登录就可能访问。
- **建议**：
  - 对管理端 CRUD（学生/教师/课程/排课/课程类型等）补充 `@RoleRequire({"ADMIN"})`。
  - 对教师端接口补充 `@RoleRequire({"TEACHER","ADMIN"})`。
  - 对学生端接口补充 `@RoleRequire({"STUDENT","ADMIN"})`。
