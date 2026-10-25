-- ============================================================
-- Dati di prova per DreamJob (solo sviluppo, NON e' una migrazione Flyway)
--
-- Esecuzione con il database di docker-compose:
--   macOS / Linux / Git Bash:
--     docker exec -i remote_job_container psql -U postgres -d portfolio < db/dati-di-test.sql
--   PowerShell:
--     Get-Content db\dati-di-test.sql | docker exec -i remote_job_container psql -U postgres -d portfolio
--
-- Si puo' rieseguire: inserisce solo i job (titolo + azienda) e le candidature che mancano.
-- Per ripartire da zero, prima dello script esegui:
--   TRUNCATE applications, jobs RESTART IDENTITY;
-- ============================================================

-- 12 job: remoti e non, stipendi sopra e sotto le soglie dei filtri,
-- valori mancanti (sede, stipendio minimo, massimo o entrambi) e minimo = massimo.
INSERT INTO jobs (title, company, location, salary_min, salary_max, remote_from_italy)
SELECT v.title, v.company, v.location, v.salary_min, v.salary_max, v.remote_from_italy
FROM (VALUES
    ('Backend Java Developer',          'Umbrella',          'Remote',      37000, 50000, TRUE),
    ('Spring Boot Engineer',            'Globex',            'Milano',      45000, 60000, FALSE),
    ('Senior Java Engineer',            'Initech',           'Remote (EU)', 55000, 70000, TRUE),
    ('Full Stack Developer Java/Angular','Hooli',            'Torino',      38000, 48000, TRUE),
    ('Java Microservices Developer',    'Stark Industries',  'Remote',      50000, 65000, TRUE),
    ('Junior Backend Developer',        'Wayne Enterprises', 'Bologna',     26000, 32000, FALSE),
    ('DevOps Engineer',                 'Cyberdyne',         'Remote',      48000, 62000, TRUE),
    ('Kotlin Backend Developer',        'Soylent',           'Roma',        42000, 42000, FALSE),
    ('Software Engineer',               'Tyrell',            NULL,          NULL,  NULL,  TRUE),
    ('Tech Lead Java',                  'Massive Dynamic',   'Remote',      70000, NULL,  TRUE),
    ('Backend Developer',               'Aperture',          'Napoli',      NULL,  40000, FALSE),
    ('Java Consultant',                 'Vandelay',          'Remote',      35000, 45000, TRUE)
) AS v (title, company, location, salary_min, salary_max, remote_from_italy)
WHERE NOT EXISTS (
    SELECT 1 FROM jobs j WHERE j.title = v.title AND j.company = v.company
);

-- 6 candidature, una per ogni stato (una senza data).
-- Gli altri 6 job restano liberi: si possono candidare ed eliminare.
INSERT INTO applications (job_id, status, applied_at)
SELECT j.id, v.status, v.applied_at
FROM (VALUES
    ('Backend Java Developer',       'Umbrella',          'APPLIED',   DATE '2026-10-01'),
    ('Spring Boot Engineer',         'Globex',            'INTERVIEW', DATE '2026-10-07'),
    ('Senior Java Engineer',         'Initech',           'OFFER',     DATE '2026-09-20'),
    ('Full Stack Developer Java/Angular', 'Hooli',        'REJECTED',  DATE '2026-09-12'),
    ('Junior Backend Developer',     'Wayne Enterprises', 'WITHDRAWN', DATE '2026-09-05'),
    ('Java Microservices Developer', 'Stark Industries',  'SAVED',     NULL)
) AS v (title, company, status, applied_at)
JOIN jobs j ON j.title = v.title AND j.company = v.company
WHERE NOT EXISTS (
    SELECT 1 FROM applications a WHERE a.job_id = j.id
);

-- ============================================================
-- Risultati attesi (con solo questi dati nel database)
--
-- GET /api/jobs                                        -> 12 job
-- GET /api/jobs?remoteFromItaly=true                   -> 8
-- GET /api/jobs?remoteFromItaly=false                  -> 4
-- GET /api/jobs?salaryMin=40000                        -> 6 (i job senza stipendio minimo sono esclusi)
-- GET /api/jobs?remoteFromItaly=true&salaryMin=40000   -> 4
-- GET /api/applications                                -> 6, una per stato
--
-- Regole di business da provare:
-- POST /api/applications per "Backend Java Developer"  -> 409 (ha gia' una candidatura)
-- DELETE /api/jobs/{id} di "Spring Boot Engineer"      -> 409 (ha una candidatura)
-- DELETE /api/jobs/{id} di "DevOps Engineer"           -> 204 (nessuna candidatura)
-- ============================================================
