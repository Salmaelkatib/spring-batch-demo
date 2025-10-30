package com.alibou.batch.student;

import com.alibou.batch.config.CustomSchedulerProcessor;
import com.alibou.batch.registeration.JobRegistry;
import com.alibou.batch.registeration.JobRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/students/jobs/importStudents/")
@RequiredArgsConstructor
public class StudentController {
    private final JobLauncher jobLauncher;
    private final JobOperator jobOperator;
    private final org.springframework.batch.core.configuration.JobRegistry jobRegistry;
    @Qualifier("configDataContextRefresher")
    private final ContextRefresher contextRefresher;
    private final Job job;
    private final CustomSchedulerProcessor customSchedulerProcessor;
    private final JobRegistryRepository jobRegistryRepository;


    @PostMapping("/start")
    public ResponseEntity<String> importCsvToDBJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();
        JobExecution jobExecution;
        try {
            jobExecution = jobLauncher.run(job, jobParameters);
        } catch (JobExecutionAlreadyRunningException
                 | JobRestartException
                 | JobInstanceAlreadyCompleteException
                 | JobParametersInvalidException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error running job: " + e.getMessage());
        }
        return ResponseEntity.ok(jobExecution.getId().toString());
    }

    @PostMapping("/restart/{execId}")
    public ResponseEntity<String> restart(@PathVariable Long execId) throws JobInstanceAlreadyCompleteException, NoSuchJobException, NoSuchJobExecutionException, JobParametersInvalidException, JobRestartException {
        Long id;
        try{
            id = jobOperator.restart(execId);
        }catch(Exception e){
            jobRegistry.getJobNames().forEach(System.out::println);
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().body("Error restarting job: " + e.getMessage());
        }
        return ResponseEntity.ok(id.toString());
    }

    @PostMapping("/stop/{execId}")
    public ResponseEntity<String> stop(@PathVariable Long execId) throws NoSuchJobExecutionException, JobExecutionNotRunningException {
        jobRegistry.getJobNames().forEach(System.out::println);
        Boolean stopped;
        try{
            jobOperator.stop(execId);
            stopped = true;
        }catch (Exception e){
            return ResponseEntity.internalServerError().body("Error stopping job: " + e.getMessage());
        }
        return ResponseEntity.ok("Job stopped: " + stopped);

    }



    @PostMapping("/reschedule")
    public String rescheduleJob(@RequestParam String cronExp) {
        customSchedulerProcessor.reschedule("importStudents", cronExp);
        JobRegistry jobRegistry = jobRegistryRepository.findByJobName("importStudents");
        jobRegistry.setCronExpression(cronExp);
        jobRegistryRepository.save(jobRegistry);
        return "Reschedule request sent for job: " + "importStudents" + " with cron: " + cronExp;
    }
}
