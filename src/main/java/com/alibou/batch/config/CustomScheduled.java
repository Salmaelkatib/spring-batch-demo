package com.alibou.batch.config;
import org.springframework.batch.core.Job;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomScheduled {
    String cronExp();
    String jobName();
}