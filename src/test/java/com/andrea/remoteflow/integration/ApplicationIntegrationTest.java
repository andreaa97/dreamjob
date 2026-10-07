package com.andrea.remoteflow.integration;

import com.andrea.remoteflow.entity.Application;
import com.andrea.remoteflow.entity.ApplicationStatus;
import com.andrea.remoteflow.repository.ApplicationRepository;
import com.andrea.remoteflow.repository.JobRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test di integrazione: avvia l'intera applicazione Spring (controller, service,
 * repository, Hibernate) con un database H2 in memoria, e chiama le API via MockMvc.
 *
 * Le proprieta' del datasource sono sovrascritte qui, quindi il test NON tocca
 * il database configurato in application.properties.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:remoteflow-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    // Database pulito prima di ogni test (prima le candidature, poi i job: foreign key)
    @BeforeEach
    void cleanDatabase() {
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
    }

    // ------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------

    private String jobJson(String title, long salaryMin, long salaryMax, boolean remoteFromItaly) {
        return """
                {
                  "title": "%s",
                  "company": "Acme",
                  "location": "Remote",
                  "salaryMin": %d,
                  "salaryMax": %d,
                  "remoteFromItaly": %b
                }
                """.formatted(title, salaryMin, salaryMax, remoteFromItaly);
    }

    private String applicationJson(Long jobId, String status, String appliedAt) {
        return """
                {
                  "jobId": %d,
                  "status": "%s",
                  "appliedAt": "%s"
                }
                """.formatted(jobId, status, appliedAt);
    }

    private long createJob(String title, long salaryMin, long salaryMax, boolean remoteFromItaly) throws Exception {
        String body = mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson(title, salaryMin, salaryMax, remoteFromItaly)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long createApplication(long jobId, String status) throws Exception {
        String body = mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationJson(jobId, status, "2026-10-07")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    // ============================================================
    // Flusso completo CRUD
    // ============================================================

    @Test
    @DisplayName("flusso completo: crea job, crea candidatura, leggi, aggiorna, elimina")
    void fullCrudFlow() throws Exception {
        long jobId = createJob("Backend Java Developer", 35000, 50000, true);

        // CREATE
        String created = mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationJson(jobId, "APPLIED", "2026-10-07")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").value(jobId))
                .andExpect(jsonPath("$.jobTitle").value("Backend Java Developer"))
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.appliedAt").value("2026-10-07"))
                .andReturn().getResponse().getContentAsString();

        long applicationId = ((Number) JsonPath.read(created, "$.id")).longValue();

        // READ (lista e singola)
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(applicationId));

        mockMvc.perform(get("/api/applications/{id}", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPLIED"));

        // UPDATE
        mockMvc.perform(put("/api/applications/{id}", applicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationJson(jobId, "INTERVIEW", "2026-10-07")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTERVIEW"));

        // DELETE
        mockMvc.perform(delete("/api/applications/{id}", applicationId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/applications/{id}", applicationId))
                .andExpect(status().isNotFound());

        // eliminare la candidatura NON deve eliminare il job
        assertEquals(0, applicationRepository.count());
        assertEquals(1, jobRepository.count());
    }

    // ============================================================
    // Persistenza reale nel database
    // ============================================================

    @Test
    @DisplayName("la candidatura creata e' davvero salvata nel database")
    void applicationIsPersisted() throws Exception {
        long jobId = createJob("Backend Java Developer", 35000, 50000, true);
        long applicationId = createApplication(jobId, "APPLIED");

        Application saved = applicationRepository.findById(applicationId).orElseThrow();

        assertEquals(jobId, saved.getJob().getId());
        assertEquals(ApplicationStatus.APPLIED, saved.getStatus());
    }

    @Test
    @DisplayName("l'update viene salvato al commit anche senza chiamare save()")
    void updateIsPersistedOnCommit() throws Exception {
        long jobId = createJob("Backend Java Developer", 35000, 50000, true);
        long applicationId = createApplication(jobId, "APPLIED");

        mockMvc.perform(put("/api/applications/{id}", applicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationJson(jobId, "OFFER", "2026-10-09")))
                .andExpect(status().isOk());

        // rileggiamo direttamente dal database, non dalla risposta HTTP
        Application reloaded = applicationRepository.findById(applicationId).orElseThrow();
        assertEquals(ApplicationStatus.OFFER, reloaded.getStatus());
        assertEquals("2026-10-09", reloaded.getAppliedAt().toString());
    }

    // ============================================================
    // Regole di business
    // ============================================================

    @Test
    @DisplayName("una seconda candidatura per lo stesso job restituisce 409")
    void duplicateApplicationReturnsConflict() throws Exception {
        long jobId = createJob("Backend Java Developer", 35000, 50000, true);
        createApplication(jobId, "APPLIED");

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationJson(jobId, "APPLIED", "2026-10-08")))
                .andExpect(status().isConflict());

        assertEquals(1, applicationRepository.count());
    }

    @Test
    @DisplayName("spostare una candidatura su un job gia' occupato restituisce 409")
    void updateToOccupiedJobReturnsConflict() throws Exception {
        long job1 = createJob("Job uno", 30000, 40000, true);
        long job2 = createJob("Job due", 30000, 40000, true);
        createApplication(job1, "APPLIED");
        long applicationOnJob2 = createApplication(job2, "APPLIED");

        mockMvc.perform(put("/api/applications/{id}", applicationOnJob2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationJson(job1, "APPLIED", "2026-10-07")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("candidatura per un job inesistente restituisce 404")
    void applicationForMissingJobReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationJson(999999L, "APPLIED", "2026-10-07")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("candidatura inesistente restituisce 404")
    void missingApplicationReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/applications/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // Validazione
    // ============================================================

    @Test
    @DisplayName("candidatura senza jobId e status restituisce 400")
    void applicationWithoutRequiredFieldsReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "appliedAt": "2026-10-07" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("status non valido restituisce 400")
    void invalidStatusReturnsBadRequest() throws Exception {
        long jobId = createJob("Backend Java Developer", 35000, 50000, true);

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationJson(jobId, "PIPPO", "2026-10-07")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("job con titolo vuoto restituisce 400")
    void jobWithBlankTitleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson("", 30000, 40000, true)))
                .andExpect(status().isBadRequest());

        assertEquals(0, jobRepository.count());
    }

    @Test
    @DisplayName("job con salaryMax minore di salaryMin restituisce 400")
    void jobWithInvalidSalaryRangeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson("Dev", 50000, 30000, true)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // Filtri sui job
    // ============================================================

    @Test
    @DisplayName("il filtro combinato remoteFromItaly + salaryMin applica entrambe le condizioni")
    void combinedJobFilterAppliesBothConditions() throws Exception {
        createJob("Remoto ben pagato", 45000, 60000, true);   // deve comparire
        createJob("Remoto poco pagato", 30000, 40000, true);  // stipendio troppo basso
        createJob("In sede ben pagato", 50000, 70000, false); // non remoto

        mockMvc.perform(get("/api/jobs")
                        .param("remoteFromItaly", "true")
                        .param("salaryMin", "40000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Remoto ben pagato"));
    }

    @Test
    @DisplayName("senza filtri restituisce tutti i job")
    void noFiltersReturnsAllJobs() throws Exception {
        createJob("Job A", 30000, 40000, true);
        createJob("Job B", 40000, 50000, false);

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}