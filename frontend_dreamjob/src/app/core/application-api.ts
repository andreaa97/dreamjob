import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Application, ApplicationRequest } from './models';

/**
 * Chiamate HTTP per le candidature (/api/applications).
 * Stessa impostazione di JobApi: istanza unica, URL relativi, Observable da sottoscrivere.
 */
@Injectable({ providedIn: 'root' })
export class ApplicationApi {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/applications';

  /** GET /api/applications: tutte le candidature, con id e titolo del job. */
  list(): Observable<Application[]> {
    return this.http.get<Application[]>(this.url);
  }

  /** POST /api/applications: 409 se il job ha gia' una candidatura, 404 se il job non esiste. */
  create(application: ApplicationRequest): Observable<Application> {
    return this.http.post<Application>(this.url, application);
  }

  /** PUT /api/applications/{id}: vanno inviati tutti i campi, non solo quelli cambiati. */
  update(id: number, application: ApplicationRequest): Observable<Application> {
    return this.http.put<Application>(`${this.url}/${id}`, application);
  }

  /** DELETE /api/applications/{id}: il job collegato resta. */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
