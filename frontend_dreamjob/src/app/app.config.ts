import { provideHttpClient } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';

/**
 * Configurazione globale dell'applicazione, usata da `bootstrapApplication` in main.ts.
 * - provideBrowserGlobalErrorListeners: gli errori non gestiti finiscono nel gestore di Angular
 * - provideRouter: le pagine definite in app.routes.ts
 * - provideHttpClient: HttpClient per chiamare il backend (usa fetch di default)
 */
export const appConfig: ApplicationConfig = {
  providers: [provideBrowserGlobalErrorListeners(), provideRouter(routes), provideHttpClient()],
};
