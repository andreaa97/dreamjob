import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Application, ApplicationRequest } from './models';

@Injectable({ providedIn: 'root' })
export class ApplicationApi {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/applications';

  list(): Observable<Application[]> {
    return this.http.get<Application[]>(this.url);
  }

  create(application: ApplicationRequest): Observable<Application> {
    return this.http.post<Application>(this.url, application);
  }

  update(id: number, application: ApplicationRequest): Observable<Application> {
    return this.http.put<Application>(`${this.url}/${id}`, application);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
