package com.andrea.remoteflow.repository;

import com.andrea.remoteflow.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByRemoteFromItaly(Boolean remoteFromItaly);
    List<Job> findBySalaryMinGreaterThanEqual(Long salaryMin);
    List<Job> findByRemoteFromItalyAndSalaryMinGreaterThanEqual(Boolean remoteFromItaly, Long salaryMin);
}
