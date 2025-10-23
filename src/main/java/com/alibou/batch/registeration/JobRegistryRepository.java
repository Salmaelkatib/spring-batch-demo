package com.alibou.batch.registeration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRegistryRepository extends JpaRepository<JobRegistry,Long> {
    List<JobRegistry> findByServiceRegistry(ServiceRegistry serviceRegistry);

    JobRegistry findByJobName(String importStudents);
}
