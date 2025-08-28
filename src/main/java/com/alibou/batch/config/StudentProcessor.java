package com.alibou.batch.config;

import com.alibou.batch.student.Student;
import org.springframework.batch.item.ItemProcessor;

public class StudentProcessor implements ItemProcessor<Student,Student> {

    @Override
    public Student process(Student student) {
        // all the business logic required to process the read data goes here
        return student;
    }
}
