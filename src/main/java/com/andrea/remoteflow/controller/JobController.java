package com.andrea.remoteflow.controller;

import com.andrea.remoteflow.dto.JobRequest;
import com.andrea.remoteflow.entity.Job;
import com.andrea.remoteflow.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Job createJob(@Valid @RequestBody JobRequest request) {
        return jobService.createJob(request);
    }

    @GetMapping
    public List<Job> getJobs(@RequestParam(required = false) Boolean remoteFromItaly,
                             @RequestParam(required = false) Long salaryMin) {
        return jobService.search(remoteFromItaly, salaryMin);
    }

    @GetMapping("/{id}")
    public Job getJobById(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    @PutMapping("/{id}")
    public Job updateJob(
            @PathVariable Long id,
            @RequestBody JobRequest job
    ) {
        return jobService.updateJob(id, job);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
    }

    /*@GetMapping
    public List<Job> checkItaly(@RequestParam Boolean remoteFromItaly) {
        return jobService.remote(remoteFromItaly);

        for (Job value : jobsFromItaly) {
            if (!value.isRemoteFromItaly()) {
                jobsFromItaly.remove(value);
            }
        } mia soluzione
        //jobsFromItaly.removeIf(value -> !value.isRemoteFromItaly());

        //jobsFromItaly.removeIf(Job::isRemoteFromItaly); unica riga con lambda
    }*/

}