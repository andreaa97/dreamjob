import { Routes } from '@angular/router';

/**
 * Pagine dell'applicazione.
 * `loadComponent` con import() dinamico crea un file JavaScript separato per ogni pagina,
 * scaricato solo quando l'utente la apre (caricamento "lazy"): l'avvio resta leggero.
 * `title` imposta il titolo della scheda del browser.
 */
export const routes: Routes = [
  // la home porta agli annunci
  { path: '', pathMatch: 'full', redirectTo: 'annunci' },
  {
    path: 'annunci',
    title: 'Annunci · DreamJob',
    loadComponent: () => import('./jobs/jobs-page').then((m) => m.JobsPage),
  },
  {
    path: 'candidature',
    title: 'Candidature · DreamJob',
    loadComponent: () => import('./applications/applications-page').then((m) => m.ApplicationsPage),
  },
  // qualsiasi altro indirizzo: di nuovo agli annunci
  { path: '**', redirectTo: 'annunci' },
];
