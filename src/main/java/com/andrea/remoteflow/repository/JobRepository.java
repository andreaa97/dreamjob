package com.andrea.remoteflow.repository;

import com.andrea.remoteflow.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Accesso alla tabella {@code jobs}.
 * <p>
 * Le query dei filtri sono "derivate" dal nome del metodo: Spring Data legge
 * {@code findByRemoteFromItalyAndSalaryMinGreaterThanEqual} e genera
 * {@code WHERE remote_from_italy = ? AND salary_min >= ?}. I job con stipendio minimo
 * {@code null} non soddisfano mai {@code >=} e restano esclusi dal filtro sullo stipendio.
 */
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByRemoteFromItaly(Boolean remoteFromItaly);
    List<Job> findBySalaryMinGreaterThanEqual(Long salaryMin);
    List<Job> findByRemoteFromItalyAndSalaryMinGreaterThanEqual(Boolean remoteFromItaly, Long salaryMin);
}
