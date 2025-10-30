package com.alibou.batch.config;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
@EnableScheduling
@Getter
public class CustomSchedulerProcessor implements BeanPostProcessor, EmbeddedValueResolverAware {

    private final TaskScheduler taskScheduler;
    private final Map<String, Runnable> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<String, String> cronExpressions = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();
    private StringValueResolver valueResolver;

    public CustomSchedulerProcessor() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("dynamic-scheduler-");
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.valueResolver = resolver;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            CustomScheduled annotation = method.getAnnotation(CustomScheduled.class);
            if (annotation != null) {
                String resolvedCronExp = valueResolver.resolveStringValue(annotation.cronExp());
                scheduleMethod(bean, method, resolvedCronExp, annotation.jobName());
            }
        }
        return bean;
    }

    private void scheduleMethod(Object bean, Method method, String cronExp, String jobName) {
        ReflectionUtils.makeAccessible(method);

        // Dynamically resolve jobName if empty
        String resolvedJobName = jobName;
        if (jobName == null || jobName.isEmpty()) {
            try {
                for (java.lang.reflect.Field field : bean.getClass().getDeclaredFields()) {
                    if (org.springframework.batch.core.Job.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        org.springframework.batch.core.Job job = (org.springframework.batch.core.Job) field.get(bean);
                        if (job != null) {
                            resolvedJobName = job.getName();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Could not resolve jobName dynamically", e);
            }
        }

        Runnable task = () -> {
            try {
                ReflectionUtils.invokeMethod(method, bean);
            } catch (Exception e) {
                log.error("Error running scheduled task", e);
            }
        };


        log.info("Scheduling method {} (jobName={}) with cron {}", method.getName(), resolvedJobName, cronExp);

        ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(cronExp));

        scheduledTasks.put(resolvedJobName, task);
        cronExpressions.put(resolvedJobName, cronExp);
        scheduledFutures.put(resolvedJobName, future);
    }



    public void reschedule(String jobName, String newCron) {
        Runnable task = scheduledTasks.get(jobName);
        ScheduledFuture<?> oldFuture = scheduledFutures.get(jobName);

        if (task == null) {
            log.warn("No scheduled task found for jobName: {}", jobName);
            return;
        }

        // Cancel the old future if it exists
        if (oldFuture != null) {
            oldFuture.cancel(false);
        }

        // Schedule the new task and store its future
        ScheduledFuture<?> newFuture = taskScheduler.schedule(task, new CronTrigger(newCron));
        scheduledFutures.put(jobName, newFuture);
        cronExpressions.put(jobName, newCron);

        log.info("Rescheduled {} with new cron {}", jobName, newCron);
    }

}