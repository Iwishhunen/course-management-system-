package com.example.backend.service.impl;

import com.example.backend.service.IEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

/**
 * 邮件服务实现类
 */
@Service
public class EmailServiceImpl implements IEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    private volatile boolean mailEnabled = true;

    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        if (!mailEnabled) {
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            mailEnabled = false;
            e.printStackTrace();
        }
    }

    @Override
    public void sendHtmlMail(String to, String subject, String content) {
        if (!mailEnabled) {
            return;
        }
        MimeMessage message = mailSender.createMimeMessage();
        try {
            // 设置编码为UTF-8
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            mailEnabled = false;
            e.printStackTrace();
        }
    }

    @Override
    public void sendCourseCancellationNotificationToStudent(String studentEmail, String courseName,
            String teacherName) {
        if (studentEmail == null || studentEmail.isEmpty()) {
            return;
        }

        String subject = "课程取消通知";
        String content = String.format("<html><body>" +
                "<h2>课程取消通知</h2>" +
                "<p>尊敬的同学：</p>" +
                "<p>您选择的课程<strong>%s</strong>因选课人数不足20人已被取消。</p>" +
                "<p>如有疑问，请联系任课教师：<strong>%s</strong>。</p>" +
                "<br>" +
                "<p>感谢您的理解与支持！</p>" +
                "</body></html>", courseName, teacherName);

        sendHtmlMail(studentEmail, subject, content);
    }

    @Override
    public void sendCourseCancellationNotificationToTeacher(String teacherEmail, String courseName) {
        if (teacherEmail == null || teacherEmail.isEmpty()) {
            return;
        }

        String subject = "课程取消通知";
        String content = String.format("<html><body>" +
                "<h2>课程取消通知</h2>" +
                "<p>尊敬的老师：</p>" +
                "<p>您开设的课程<strong>%s</strong>因选课人数不足20人已被取消。</p>" +
                "<br>" +
                "<p>感谢您的理解与支持！</p>" +
                "</body></html>", courseName);

        sendHtmlMail(teacherEmail, subject, content);
    }
}