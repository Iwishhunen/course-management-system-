package com.example.backend.service;

/**
 * 邮件服务接口
 */
public interface IEmailService {

    /**
     * 发送简单文本邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    void sendSimpleMail(String to, String subject, String content);

    /**
     * 发送HTML格式邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content HTML内容
     */
    void sendHtmlMail(String to, String subject, String content);

    /**
     * 发送课程取消通知邮件给学生
     * @param studentEmail 学生邮箱
     * @param courseName 课程名称
     * @param teacherName 教师姓名
     */
    void sendCourseCancellationNotificationToStudent(String studentEmail, String courseName, String teacherName);

    /**
     * 发送课程取消通知邮件给教师
     * @param teacherEmail 教师邮箱
     * @param courseName 课程名称
     */
    void sendCourseCancellationNotificationToTeacher(String teacherEmail, String courseName);
}