import { HttpErrorResponse } from '@angular/common/http';

/**
 * Trasforma un errore HTTP del backend in un messaggio comprensibile per l'utente.
 * `conflictMessage` spiega il 409 nel contesto dell'azione che l'ha causato
 * (es. "Hai già una candidatura per questo annuncio.").
 */
export function apiErrorMessage(
  error: unknown,
  conflictMessage = "L'operazione è in conflitto con i dati esistenti.",
): string {
  // le resource di Angular possono avvolgere l'errore originale in Error.cause
  const cause = error instanceof Error && error.cause !== undefined ? error.cause : error;
  const response = error instanceof HttpErrorResponse ? error : cause;

  if (!(response instanceof HttpErrorResponse)) {
    return 'Si è verificato un errore inatteso. Riprova.';
  }

  switch (response.status) {
    case 0: // errore di rete: nessuna risposta dal server
    case 502: // il proxy di sviluppo risponde 502 quando il backend è spento
    case 503:
    case 504:
      return 'Impossibile contattare il server: controlla di essere online e che il backend sia avviato.';
    case 400: // validazione fallita (@Valid nel backend)
      return 'Dati non validi: controlla i campi e riprova.';
    case 404: // l'elemento è stato eliminato nel frattempo (es. da un'altra scheda)
      return "L'elemento non esiste più: forse è stato eliminato. Aggiorna la pagina.";
    case 409: // regola di business violata: il chiamante sa spiegare quale
      return conflictMessage;
    default:
      return response.status >= 500
        ? `Errore del server (${response.status}): controlla che il backend sia avviato e funzionante.`
        : `Richiesta non riuscita (${response.status}). Riprova.`;
  }
}
