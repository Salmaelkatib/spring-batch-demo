package cash.batch.controller;

import cash.jobscheduler.CustomSchedulerProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/demo/jobs/")
@RequiredArgsConstructor
public class DemoController {
    private final JobLauncher jobLauncher;
    private final JobOperator jobOperator;
    private final ApplicationContext applicationContext;
    private final CustomSchedulerProcessor customSchedulerProcessor;
    private final JobExplorer jobExplorer;

    @PostMapping("{jobName}/start")
    public ResponseEntity<String> startJob(@PathVariable String jobName) {
        try {
            Job job = (Job) applicationContext.getBean(jobName);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startAt",System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
            return ResponseEntity.ok("Job " + jobName + " started successfully with ID: " + jobExecution.getId());
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
    public ResponseEntity<String> restart(@PathVariable Long execId) throws JobInstanceAlreadyCompleteException, NoSuchJobException, NoSuchJobExecutionException, JobParametersInvalidException, JobRestartException {
        Long id = jobOperator.restart(execId);
        return ResponseEntity.ok("Job restarted successfully with Execution ID: " + id);
    }

    @PostMapping("{jobName}/stop/{execId}")
    public ResponseEntity<String> stop(@PathVariable Long execId, @PathVariable String jobName) throws JobInstanceAlreadyCompleteException, NoSuchJobException, NoSuchJobExecutionException, JobParametersInvalidException, JobRestartException, JobExecutionNotRunningException {

        JobExecution jobExecution = jobExplorer.getJobExecution(execId);
        assert jobExecution != null;
        System.out.println(jobExecution.isRunning());
        if(!jobExecution.isRunning()){
            throw new JobExecutionNotRunningException("Job execution is not running");
        }

        boolean stopped = jobOperator.stop(execId);
        return ResponseEntity.ok("Job stopped successfully with Execution ID: " + execId);
    }
    @PostMapping("{jobName}/reschedule")
    public ResponseEntity<String> rescheduleJob(@PathVariable String jobName , @RequestParam String cronExp) {
        customSchedulerProcessor.reschedule(jobName, cronExp);
        return ResponseEntity.ok("Job " + jobName + "rescheduled successfully with new cron Exp: " + cronExp);    }
}
