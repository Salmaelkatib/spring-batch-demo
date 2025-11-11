package cash.batch.config;

import cash.batch.student.Student;
import org.springframework.batch.item.ItemProcessor;

public class StudentProcessor implements ItemProcessor<Student,Student> {

    @Override
    public Student process(Student student) {
        student.setId(null);
        // all the business logic required to process the read data goes here
        return student;
    }
}
