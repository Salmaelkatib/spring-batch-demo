package com.alibou.batch.teacher;

import com.alibou.batch.config.CustomScheduled;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component
public class TeacherScheduler {
    private final JobLauncher jobLauncher;
    private final Job job;
    private static int x = 0;

    public TeacherScheduler(JobLauncher jobLauncher, @Qualifier("importTeachers") Job job) {
        this.jobLauncher = jobLauncher;
        this.job = job;
    }

    @CustomScheduled(cronExp = "${cronExp}", jobName = "importTeachers")
    public void execute() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(job, jobParameters);
        } catch (JobExecutionAlreadyRunningException
                 | JobRestartException
                 | JobInstanceAlreadyCompleteException
                 | JobParametersInvalidException e) {
            e.printStackTrace();
        }
    }

}
