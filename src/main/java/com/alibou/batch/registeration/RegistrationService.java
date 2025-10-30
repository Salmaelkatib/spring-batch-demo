package com.alibou.batch.registeration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final ServiceRegistryRepository serviceRegistryRepository;
    private final JobRegistryRepository jobRegistryRepository;
    private final Environment environment;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port}")
    private String serverPort;

    @Value("${service.description}")
    private String serviceDescription;

    @Value("${cronExp}")
    private String cronExp;

    public void registerService() {
        try {
            // Get local host information
            //String hostAddress = InetAddress.getLocalHost().getHostAddress();
            String hostAddress = "localhost";

            // Check if service already exists
            ServiceRegistry existingService = serviceRegistryRepository.findByServiceName(serviceName);

            if (existingService != null) {
                log.info("Service '{}' already exists, updating...", serviceName);

                // Update existing service
                existingService.setLastUpdated(new Date());
                existingService.setIp(hostAddress);
                existingService.setPort(serverPort);
                existingService.setServiceDescription(serviceDescription);

                serviceRegistryRepository.save(existingService);
                log.info("Service '{}' updated successfully", serviceName);

                // Update existing jobs
                updateExistingJobs(existingService);
            } else {
                // Create new service
                ServiceRegistry newService = new ServiceRegistry();
                newService.setServiceName(serviceName);
                newService.setIp(hostAddress);
                newService.setPort(serverPort);
                newService.setServiceDescription(serviceDescription);
                newService.setCreateTime(new Date());
                newService.setLastUpdated(new Date());

                newService = serviceRegistryRepository.save(newService);
                log.info("Service '{}' registered successfully", serviceName);

                // Register jobs for new service
                registerJobs(newService);
            }
        } catch (Exception e) {
            log.error("Error registering service: {}", e.getMessage(), e);
        }
    }

    private void registerJobs(ServiceRegistry serviceRegistry) {
        // Here you would typically scan for jobs in your application
        // For this example, we'll add a few placeholder jobs
        String jobName = "importStudents";
        String jobDescription = "Process student data";
        String path = "/students/jobs/importStudents/";
        registerJob(
            serviceRegistry,
            jobName,
            jobDescription,
            path,
            cronExp
        );


        // Register any other jobs your application has
        // You could use ApplicationContext to scan for beans with @CustomScheduled
    }

    private void updateExistingJobs(ServiceRegistry serviceRegistry) {
        // Get all jobs associated with this service
        List<JobRegistry> existingJobs = jobRegistryRepository.findByServiceRegistry(serviceRegistry);

        // Update existing jobs and register new ones
        Map<String, JobRegistry> jobMap = existingJobs.stream()
                .collect(Collectors.toMap(JobRegistry::getJobName, job -> job));

        String jobName = "importStudents";
        String jobDescription = "Process student data";
        String path = "/students/jobs/importStudents/";
        // Update student job
        if (jobMap.containsKey(jobName)) {
            JobRegistry job = jobMap.get(jobName);
            job.setJobDescription(jobDescription);
            job.setPath(path);
            job.setCronExpression(cronExp);
            job.setLastUpdated(new Date());
            jobRegistryRepository.save(job);
            log.info("Updated job: {}", job.getJobName());
        } else {
            registerJob(
                serviceRegistry, 
                jobName,
                jobDescription,
                path,
                cronExp
            );
        }

        // Update other jobs as needed
    }

    private void registerJob(
            ServiceRegistry serviceRegistry,
            String jobName,
            String jobDescription,
            String path,
            String cronExpression) {

        JobRegistry job = new JobRegistry();
        job.setJobName(jobName);
        job.setJobDescription(jobDescription);
        job.setPath(path);
        job.setCronExpression(cronExpression);
        job.setCreateTime(new Date());
        job.setLastUpdated(new Date());
        job.setServiceRegistry(serviceRegistry);

        jobRegistryRepository.save(job);
        log.info("Registered job: {}", jobName);
    }
}
