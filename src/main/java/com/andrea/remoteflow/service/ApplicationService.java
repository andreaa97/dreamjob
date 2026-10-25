package com.andrea.remoteflow.service;

import com.andrea.remoteflow.dto.ApplicationRequest;
import com.andrea.remoteflow.entity.Application;
import com.andrea.remoteflow.entity.Job;
import com.andrea.remoteflow.exception.ApplicationNotFoundException;
import com.andrea.remoteflow.exception.DuplicateApplicationException;
import com.andrea.remoteflow.exception.JobNotFoundException;
import com.andrea.remoteflow.repository.ApplicationRepository;
import com.andrea.remoteflow.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    @Transactional
    public Application createApplication(ApplicationRequest request) {
        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new JobNotFoundException(request.jobId()));

        if (applicationRepository.existsByJobId(job.getId())) {
            throw new DuplicateApplicationException(job.getId());
        }

        Application application = new Application();
        application.setJob(job);
        application.setStatus(request.status());
        application.setAppliedAt(request.appliedAt());
        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    @Transactional
    public Application updateApplication(Long id, ApplicationRequest request) {
        Application application = getApplicationById(id);

        // only re-check the duplicate rule if the job is actually changing
        if (!application.getJob().getId().equals(request.jobId())) {
            Job job = jobRepository.findById(request.jobId())
                    .orElseThrow(() -> new JobNotFoundException(request.jobId()));
            if (applicationRepository.existsByJobId(job.getId())) {
                throw new DuplicateApplicationException(job.getId());
            }
            application.setJob(job);
        }

        application.setStatus(request.status());
        application.setAppliedAt(request.appliedAt());
        return application; // managed entity, saved on commit
    }

    @Transactional
    public void deleteApplication(Long id) {
        applicationRepository.delete(getApplicationById(id));
    }
}