import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Job, JobFilters, JobRequest } from './models';

/**
 * Chiamate HTTP per gli annunci (/api/jobs).
 *
 * `providedIn: 'root'` crea un'unica istanza condivisa da tutta l'app.
 * Gli URL sono relativi: in sviluppo proxy.conf.json inoltra /api al backend su :8080.
 * I metodi restituiscono Observable "freddi": la richiesta parte solo con subscribe().
 */
@Injectable({ providedIn: 'root' })
export class JobApi {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/jobs';

  /** GET /api/jobs con i filtri impostati. */
  search(filters: JobFilters): Observable<Job[]> {
    // invia solo i filtri valorizzati: per il backend un parametro assente vuol dire "qualsiasi"
    let params = new HttpParams();
    if (filters.remoteFromItaly !== null) {
      params = params.set('remoteFromItaly', filters.remoteFromItaly);
    }
    if (filters.salaryMin !== null) {
      params = params.set('salaryMin', filters.salaryMin);
    }
    return this.http.get<Job[]>(this.url, { params });
  }

  /** POST /api/jobs: restituisce il job creato, con l'id. */
  create(job: JobRequest): Observable<Job> {
    return this.http.post<Job>(this.url, job);
  }

  /** PUT /api/jobs/{id}: sostituisce tutti i campi. */
  update(id: number, job: JobRequest): Observable<Job> {
    return this.http.put<Job>(`${this.url}/${id}`, job);
  }

  /** DELETE /api/jobs/{id}: fallisce con 409 se il job ha una candidatura. */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
