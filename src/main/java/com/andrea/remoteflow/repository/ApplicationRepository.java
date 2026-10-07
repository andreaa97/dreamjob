package com.andrea.remoteflow.repository;

import com.andrea.remoteflow.entity.Application;
import com.andrea.remoteflow.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Boolean existsByJobId(Long jobId);

}
