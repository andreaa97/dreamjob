package com.andrea.remoteflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Dati in ingresso per creare o aggiornare un job.
 * <p>
 * Le stesse regole sono ripetute nel form del frontend, cosi' l'utente vede l'errore
 * prima di inviare; il backend resta comunque l'unica garanzia.
 *
 * @param title           ruolo, obbligatorio e non solo spazi
 * @param company         azienda, obbligatoria e non solo spazi
 * @param location        sede (facoltativa)
 * @param salaryMin       stipendio annuo minimo, facoltativo, mai negativo
 * @param salaryMax       stipendio annuo massimo, facoltativo, mai negativo
 * @param remoteFromItaly se il lavoro si puo' fare in remoto dall'Italia (obbligatorio)
 */
public record JobRequest(
        @NotBlank String title,
        @NotBlank String company,
        String location,
        @PositiveOrZero Long salaryMin,
        @PositiveOrZero Long salaryMax,
        @NotNull Boolean remoteFromItaly
) {

    /**
     * Regola che coinvolge due campi: se ci sono entrambi, il massimo non puo' essere minore
     * del minimo. Bean Validation la controlla come una proprieta' "salaryRangeValid";
     * {@code @JsonIgnore} evita che compaia nel JSON.
     */
    @JsonIgnore
    @AssertTrue(message = "salaryMax must be greater than or equal to salaryMin")
    public boolean isSalaryRangeValid() {
        return salaryMin == null || salaryMax == null || salaryMax >= salaryMin;
    }
}
