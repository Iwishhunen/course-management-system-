package common;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir")+"/backend";
        System.out.println("当前项目路径: " + projectPath);

        FastAutoGenerator.create(
                        "jdbc:mysql://localhost:3306/student_course_selection_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8",
                        "root",
                        "123456")
                .globalConfig(builder -> {
                    builder.author("Iwishhunnen")
                            .disableOpenDir()
                            .outputDir(projectPath + "/src/main/java");
                })
                .packageConfig(builder -> {
                    builder.parent("com.example.backend")
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            .pathInfo(Collections.singletonMap(
                                    OutputFile.xml,
                                    projectPath + "/src/main/resources/mapper"
                            ));
                })
                .strategyConfig(builder -> {
                    builder.addInclude(
                                    "student", "teacher", "admin", "course", "schedule",
                                    "enrollment", "enrollmentperiod", "course_categories"
                            )
                            .entityBuilder()
                            .enableLombok()
                            .enableTableFieldAnnotation()
                            .controllerBuilder()
                            .enableRestStyle()
                            .serviceBuilder()
                            .formatServiceFileName("I%sService")
                            .formatServiceImplFileName("%sServiceImpl");
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

        System.out.println("代码生成完成！");
    }
}