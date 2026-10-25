import { HttpErrorResponse } from '@angular/common/http';

/**
 * Turns an HTTP error from the backend into a message for the user.
 * `conflictMessage` explains the 409 in the context of the action that caused it.
 */
export function apiErrorMessage(
  error: unknown,
  conflictMessage = "L'operazione è in conflitto con i dati esistenti.",
): string {
  // resources may wrap the original error in Error.cause
  const cause = error instanceof Error && error.cause !== undefined ? error.cause : error;
  const response = error instanceof HttpErrorResponse ? error : cause;

  if (!(response instanceof HttpErrorResponse)) {
    return 'Si è verificato un errore inatteso. Riprova.';
  }

  switch (response.status) {
    case 0: // network error
    case 502: // the dev-server proxy answers 502 when the backend is down
    case 503:
    case 504:
      return 'Impossibile contattare il server: controlla di essere online e che il backend sia avviato.';
    case 400:
      return 'Dati non validi: controlla i campi e riprova.';
    case 404:
      return "L'elemento non esiste più: forse è stato eliminato. Aggiorna la pagina.";
    case 409:
      return conflictMessage;
    default:
      return response.status >= 500
        ? `Errore del server (${response.status}): controlla che il backend sia avviato e funzionante.`
        : `Richiesta non riuscita (${response.status}). Riprova.`;
  }
}
