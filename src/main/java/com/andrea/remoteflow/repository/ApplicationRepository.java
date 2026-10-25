package com.andrea.remoteflow.repository;

import com.andrea.remoteflow.entity.Application;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Accesso alla tabella {@code applications}.
 * <p>
 * Spring Data genera l'implementazione: {@link JpaRepository} fornisce save, findById,
 * findAll, delete...; i metodi aggiunti qui vengono tradotti in query dal loro nome.
 */
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // Ogni risposta mostra il titolo del job, quindi il job va caricato insieme alla candidatura.
    // @EntityGraph lo fa con una JOIN nella stessa query: niente query in piu' per ogni
    // candidatura (problema N+1) e niente LazyInitializationException fuori dalla transazione.
    @Override
    @EntityGraph(attributePaths = "job")
    List<Application> findAll();

    @Override
    @EntityGraph(attributePaths = "job")
    Optional<Application> findById(Long id);

    // "exists" + "ByJobId": diventa SELECT ... WHERE job_id = ? senza caricare il job
    boolean existsByJobId(Long jobId);
}
