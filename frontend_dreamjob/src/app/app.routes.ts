import { Routes } from '@angular/router';

export const routes: Routes = [
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
  { path: '**', redirectTo: 'annunci' },
];
