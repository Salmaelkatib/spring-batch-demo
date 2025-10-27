package com.alibou.batch.controller;

import com.alibou.batch.config.CustomSchedulerProcessor;
import com.alibou.batch.registeration.JobRegistry;
import com.alibou.batch.registeration.JobRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/demo/jobs/")
@RequiredArgsConstructor
public class DemoController {
    private final JobLauncher jobLauncher;
    private final JobLocator jobLocator;
    private final JobOperator jobOperator;
    @Qualifier("configDataContextRefresher")
    private final ContextRefresher contextRefresher;
    private final ApplicationContext applicationContext;
    private final CustomSchedulerProcessor customSchedulerProcessor;
    private final JobRegistryRepository jobRegistryRepository;

//    @PostMapping("/start")
//    public ResponseEntity<String> importCsvToDBJob() {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addLong("startAt", System.currentTimeMillis())
//                .toJobParameters();
//        JobExecution jobExecution;
//        try {
//            jobExecution = jobLauncher.run(job, jobParameters);
//        } catch (JobExecutionAlreadyRunningException
//                 | JobRestartException
//                 | JobInstanceAlreadyCompleteException
//                 | JobParametersInvalidException e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body("Error running job: " + e.getMessage());
//        }
//        return ResponseEntity.ok(""+jobExecution.getId());
//    }

    @PostMapping("{jobName}/start")
    public ResponseEntity<String> startJob(@PathVariable String jobName) {
        try {
            Job job = (Job) applicationContext.getBean(jobName);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startAt",System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
            return ResponseEntity.ok("Job " + jobName + "started successfully with ID: " + jobExecution.getId());
        } catch (NoSuchBeanDefinitionException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job Bean not found: " + jobName);
        } catch (JobExecutionAlreadyRunningException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Job " + jobName + " is already running.");
        } catch (JobInstanceAlreadyCompleteException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Job " + jobName + " has already completed.");
        } catch (JobRestartException | JobParametersInvalidException e) {
            return ResponseEntity.badRequest().body("Cannot start job: " + jobName +" invalid parameters.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Unexpected error while starting job: " + e.getMessage());
        }
    }

    @PostMapping("{jobName}/restart/{execId}")
    public void restart(@PathVariable Long execId) throws JobInstanceAlreadyCompleteException, NoSuchJobException, NoSuchJobExecutionException, JobParametersInvalidException, JobRestartException {
        jobOperator.restart(execId);
    }

    @PostMapping("{jobName}/stop/{execId}")
    public void stop(@PathVariable Long execId) throws JobInstanceAlreadyCompleteException, NoSuchJobException, NoSuchJobExecutionException, JobParametersInvalidException, JobRestartException, JobExecutionNotRunningException {
        jobOperator.stop(execId);

    }
    @PostMapping("{jobName}/reschedule")
    public String rescheduleJob(@PathVariable String jobName , @RequestParam String cronExp) {
        customSchedulerProcessor.reschedule(jobName, cronExp);
        JobRegistry jobRegistry = jobRegistryRepository.findByJobName(jobName);
        if (jobRegistry == null) {
            throw new RuntimeException("Job not found: " + jobName);
        }
        jobRegistry.setCronExpression(cronExp);
        jobRegistryRepository.save(jobRegistry);
        return "Reschedule request sent for job: " + jobName + " with cron: " + cronExp;
    }
}
