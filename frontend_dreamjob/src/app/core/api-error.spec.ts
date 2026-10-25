import { HttpErrorResponse } from '@angular/common/http';
import { apiErrorMessage } from './api-error';

const httpError = (status: number) => new HttpErrorResponse({ status });

describe('apiErrorMessage', () => {
  it('spiega che il backend non è raggiungibile (rete assente o proxy senza backend)', () => {
    expect(apiErrorMessage(httpError(0))).toContain('Impossibile contattare il server');
    expect(apiErrorMessage(httpError(502))).toContain('Impossibile contattare il server');
  });

  it('usa il messaggio specifico per il conflitto (409)', () => {
    expect(apiErrorMessage(httpError(409), 'Hai già una candidatura.')).toBe(
      'Hai già una candidatura.',
    );
  });

  it('gestisce validazione, elemento mancante ed errori del server', () => {
    expect(apiErrorMessage(httpError(400))).toContain('Dati non validi');
    expect(apiErrorMessage(httpError(404))).toContain('non esiste più');
    expect(apiErrorMessage(httpError(500))).toContain('Errore del server (500)');
  });

  it("legge l'errore HTTP anche se è avvolto in Error.cause", () => {
    const wrapped = new Error('resource error', { cause: httpError(404) });
    expect(apiErrorMessage(wrapped)).toContain('non esiste più');
  });

  it('ha un messaggio generico per errori non HTTP', () => {
    expect(apiErrorMessage(new Error('boom'))).toContain('errore inatteso');
  });
});
