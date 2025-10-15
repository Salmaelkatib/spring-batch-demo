package com.alibou.batch.student;

import com.alibou.batch.config.CustomSchedulerProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;


@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {
    private final JobLauncher jobLauncher;
    private final JobOperator jobOperator;
    @Qualifier("configDataContextRefresher")
    private final ContextRefresher contextRefresher;
    private final Job job;
    private final CustomSchedulerProcessor customSchedulerProcessor;

    @PostMapping("/start")
    public void importCsvToDBJob() {
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

    @PostMapping("/restart/{execId}")
    public void restart(@PathVariable Long execId) throws JobInstanceAlreadyCompleteException, NoSuchJobException, NoSuchJobExecutionException, JobParametersInvalidException, JobRestartException {
        jobOperator.restart(execId);
    }

    @PostMapping("/stop/{execId}")
    public void stop(@PathVariable Long execId) throws JobInstanceAlreadyCompleteException, NoSuchJobException, NoSuchJobExecutionException, JobParametersInvalidException, JobRestartException, JobExecutionNotRunningException {
        jobOperator.stop(execId);

    }



    @PostMapping("/reschedule")
    public String rescheduleJob(@RequestParam String jobName, @RequestParam String cronExp) {
        customSchedulerProcessor.reschedule(jobName, cronExp);
        return "Reschedule request sent for job: " + jobName + " with cron: " + cronExp;
    }

    @PostMapping("/next-run")
    public String getNextRun(@RequestParam String jobName) {
        try {
            String cronExp = customSchedulerProcessor.getCronForJob(jobName);
            if (cronExp == null) {
                return "No cron expression found for job: " + jobName;
            }
            CronExpression cron = CronExpression.parse(cronExp);
            ZonedDateTime next = cron.next(ZonedDateTime.now());
            return next != null ? next.toString() : "No next run time found";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


}
