package com.andrea.remoteflow.repository;

import com.andrea.remoteflow.entity.Application;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // every response needs the job title: load the job in the same query (no N+1, no lazy loading)
    @Override
    @EntityGraph(attributePaths = "job")
    List<Application> findAll();

    @Override
    @EntityGraph(attributePaths = "job")
    Optional<Application> findById(Long id);

    boolean existsByJobId(Long jobId);
}
