package cash.batch.config;

import cash.batch.student.Student;
import cash.batch.student.StudentRepository;
import cash.batch.teacher.Teacher;
import cash.batch.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @Bean
    // reads lines from a csv file and maps lines to a Student object
    public FlatFileItemReader<Student> studentReader() {
        FlatFileItemReader<Student> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/students.csv"));
        itemReader.setName("csvReader1");
        itemReader.setLinesToSkip(1);  //skips the header in the beginning of the file
        itemReader.setLineMapper(studentLineMapper());
        return itemReader;
    }

    // reads lines from a csv file and maps lines to a Teacher object
    public FlatFileItemReader<Teacher> teacherReader() {
        FlatFileItemReader<Teacher> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/teachers.csv"));
        itemReader.setName("csvReader2");
        itemReader.setLinesToSkip(1);  //skips the header in the beginning of the file
        itemReader.setLineMapper(teacherLineMapper());
        return itemReader;
    }

    @Bean
    // uses custom processor
    public StudentProcessor processor() {
        return new StudentProcessor();
    }


    @Bean
    public RepositoryItemWriter<Student> studentWriter() {
        RepositoryItemWriter<Student> writer = new RepositoryItemWriter<>();
        writer.setRepository(studentRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public RepositoryItemWriter<Teacher> teacherWriter() {
        RepositoryItemWriter<Teacher> writer = new RepositoryItemWriter<>();
        writer.setRepository(teacherRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    // define the cvsImport step
    public Step StudentCsvImport() {
        return new StepBuilder("StudentCsvImport", jobRepository)
                .<Student, Student>chunk(1000, platformTransactionManager)
                .reader(studentReader())
                .processor(processor())
                .writer(studentWriter())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    // define the cvsImport step
    public Step TeacherCsvImport() {
        return new StepBuilder("TeacherCsvImport", jobRepository)
                .<Teacher, Teacher>chunk(1000, platformTransactionManager)
                .reader(teacherReader())
                .processor(item -> item)
                .writer(teacherWriter())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    // declares the job name, steps and their order.
    public Job importStudents() {
        return new JobBuilder("importStudents", jobRepository)
                .start(StudentCsvImport())
                .build();

    }

    @Bean
    // declares the job name, steps and their order.
    public Job importTeachers() {
        return new JobBuilder("importTeachers", jobRepository)
                .start(TeacherCsvImport())
                .build();
    }
    

    // helps to transform each line read from the csv file to a Student object
    private LineMapper<Student> studentLineMapper() {
        DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "age");  //columns or attributes

        BeanWrapperFieldSetMapper<Student> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Student.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }
    private LineMapper<Teacher> teacherLineMapper() {
        DefaultLineMapper<Teacher> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "age");  //columns or attributes

        BeanWrapperFieldSetMapper<Teacher> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Teacher.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        // Async pool (tune for your workload). For blocking behavior, return new SyncTaskExecutor()
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(4);
        exec.setMaxPoolSize(8);
        exec.setQueueCapacity(100);
        exec.setThreadNamePrefix("batch-");
        exec.initialize();
        return exec;
    }

    @Bean
    public JobLauncher jobLauncher(
            JobRepository jobRepository,
            @Qualifier("taskExecutor") TaskExecutor taskExecutor
    ) {
        var launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(taskExecutor); // async if ThreadPoolTaskExecutor; blocking if SyncTaskExecutor
        return launcher;
    }


}
