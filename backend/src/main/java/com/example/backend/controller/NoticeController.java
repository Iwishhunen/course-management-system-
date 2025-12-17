package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.config.RoleRequire;
import com.example.backend.entity.Notice;
import com.example.backend.service.INoticeService;
import common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private INoticeService noticeService;

    public static class PublishNoticeRequest {
        private String adminId;
        private String content;
        private List<String> targets;

        public String getAdminId() {
            return adminId;
        }

        public void setAdminId(String adminId) {
            this.adminId = adminId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<String> getTargets() {
            return targets;
        }

        public void setTargets(List<String> targets) {
            this.targets = targets;
        }
    }

    @PostMapping("/publish")
    @RoleRequire({ "ADMIN" })
    public Result<Boolean> publish(@RequestBody PublishNoticeRequest req) {
        try {
            if (req == null || req.getContent() == null || req.getContent().trim().isEmpty()) {
                return Result.error("通知内容不能为空");
            }
            if (req.getTargets() == null || req.getTargets().isEmpty()) {
                return Result.error("通知对象不能为空");
            }

            String publisherId = req.getAdminId() == null ? null : req.getAdminId().trim();
            LocalDateTime now = LocalDateTime.now();

            for (String t : req.getTargets()) {
                String target = t == null ? "" : t.trim().toLowerCase();
                if (target.isEmpty()) {
                    continue;
                }
                if (!target.equals("student") && !target.equals("teacher") && !target.equals("all")) {
                    return Result.error("通知对象不合法：" + target);
                }
                Notice n = new Notice();
                n.setContent(req.getContent().trim());
                n.setTargetRole(target);
                n.setPublisherId(publisherId);
                n.setPublishTime(now);
                noticeService.save(n);
            }

            return Result.success(true, "通知发送成功");
        } catch (Exception e) {
            return Result.error("通知发送失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    @RoleRequire({ "STUDENT", "TEACHER", "ADMIN" })
    public Result<List<Notice>> list(
            @RequestParam(required = false) String targetRole,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            int lim = limit == null ? 10 : limit;
            if (lim <= 0)
                lim = 10;
            if (lim > 50)
                lim = 50;

            QueryWrapper<Notice> qw = new QueryWrapper<>();
            if (targetRole != null && !targetRole.trim().isEmpty()) {
                String role = targetRole.trim().toLowerCase();
                if (role.equals("student") || role.equals("teacher")) {
                    qw.and(w -> w.eq("target_role", role).or().eq("target_role", "all"));
                } else if (role.equals("all")) {
                    qw.eq("target_role", "all");
                }
            }
            qw.orderByDesc("publish_time").last("LIMIT " + lim);

            List<Notice> list = noticeService.list(qw);
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/listForUser")
    @RoleRequire({ "STUDENT", "TEACHER", "ADMIN" })
    public Result<List<Notice>> listForUser(
            @RequestParam String userId,
            @RequestParam String role,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return Result.error("userId不能为空");
            }
            if (role == null || role.trim().isEmpty()) {
                return Result.error("role不能为空");
            }

            String normalizedRole = role.trim().toLowerCase();
            if (!normalizedRole.equals("student") && !normalizedRole.equals("teacher")
                    && !normalizedRole.equals("admin")) {
                return Result.error("role不合法：" + normalizedRole);
            }

            int lim = limit == null ? 10 : limit;
            if (lim <= 0)
                lim = 10;
            if (lim > 50)
                lim = 50;

            QueryWrapper<Notice> qw = new QueryWrapper<>();

            // 规则：
            // 1) all：所有用户可见
            // 2) role 且 target_user_id 为空：角色全员可见
            // 3) role 且 target_user_id = userId：定向给该用户
            qw.and(w -> w.eq("target_role", "all")
                    .or(x -> x.eq("target_role", normalizedRole).isNull("target_user_id"))
                    .or(x -> x.eq("target_role", normalizedRole).eq("target_user_id", userId.trim())));

            qw.orderByDesc("publish_time").last("LIMIT " + lim);

            List<Notice> list = noticeService.list(qw);
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
