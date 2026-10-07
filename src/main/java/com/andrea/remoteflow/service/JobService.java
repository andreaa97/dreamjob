package com.andrea.remoteflow.service;

import com.andrea.remoteflow.dto.JobRequest;
import com.andrea.remoteflow.entity.Job;
import com.andrea.remoteflow.exception.JobNotFoundException;
import com.andrea.remoteflow.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public Job createJob(JobRequest request) {
        Job job = new Job(
                request.getTitle(),
                request.getCompany(),
                request.getLocation(),
                request.getSalaryMin(),
                request.getSalaryMax(),
                request.getRemoteFromItaly()
        );
        return jobRepository.save(job);
    }

    @Transactional
    public Job updateJob(Long id, JobRequest request) {
        Job job = getJobById(id);

        job.setTitle(request.getTitle());
        job.setCompany(request.getCompany());
        job.setLocation(request.getLocation());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setRemoteFromItaly(request.getRemoteFromItaly());

        return job; // managed entity, saved on commit
    }
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() ->
                        new JobNotFoundException(id));
    }

    public void deleteJob(Long id) {

        Job job = getJobById(id);

        jobRepository.delete(job);
    }

    public List<Job> search(Boolean remoteFromItaly, Long salaryMin) {
        if (remoteFromItaly != null && salaryMin != null)
            return jobRepository.findByRemoteFromItalyAndSalaryMinGreaterThanEqual(remoteFromItaly, salaryMin);
        if (remoteFromItaly != null) return jobRepository.findByRemoteFromItaly(remoteFromItaly);
        if (salaryMin != null) return jobRepository.findBySalaryMinGreaterThanEqual(salaryMin);
        return jobRepository.findAll();
    }
}