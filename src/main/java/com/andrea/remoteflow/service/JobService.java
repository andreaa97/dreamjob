package com.andrea.remoteflow.service;

import com.andrea.remoteflow.dto.JobRequest;
import com.andrea.remoteflow.entity.Job;
import com.andrea.remoteflow.exception.JobHasApplicationException;
import com.andrea.remoteflow.exception.JobNotFoundException;
import com.andrea.remoteflow.repository.ApplicationRepository;
import com.andrea.remoteflow.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public JobService(JobRepository jobRepository,
                      ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public Job createJob(JobRequest request) {
        Job job = new Job(
                request.title(),
                request.company(),
                request.location(),
                request.salaryMin(),
                request.salaryMax(),
                request.remoteFromItaly()
        );
        return jobRepository.save(job);
    }

    @Transactional
    public Job updateJob(Long id, JobRequest request) {
        Job job = getJobById(id);

        job.setTitle(request.title());
        job.setCompany(request.company());
        job.setLocation(request.location());
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());
        job.setRemoteFromItaly(request.remoteFromItaly());

        return job; // managed entity, saved on commit
    }

    @Transactional(readOnly = true)
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
    }

    @Transactional
    public void deleteJob(Long id) {
        Job job = getJobById(id);

        // applications.job_id references the job: deleting it would violate the foreign key
        if (applicationRepository.existsByJobId(id)) {
            throw new JobHasApplicationException(id);
        }

        jobRepository.delete(job);
    }

    @Transactional(readOnly = true)
    public List<Job> search(Boolean remoteFromItaly, Long salaryMin) {
        if (remoteFromItaly != null && salaryMin != null)
            return jobRepository.findByRemoteFromItalyAndSalaryMinGreaterThanEqual(remoteFromItaly, salaryMin);
        if (remoteFromItaly != null) return jobRepository.findByRemoteFromItaly(remoteFromItaly);
        if (salaryMin != null) return jobRepository.findBySalaryMinGreaterThanEqual(salaryMin);
        return jobRepository.findAll();
    }
}
