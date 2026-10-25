import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Job, JobFilters, JobRequest } from './models';

@Injectable({ providedIn: 'root' })
export class JobApi {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/jobs';

  search(filters: JobFilters): Observable<Job[]> {
    // only send the filters that are set: the backend treats a missing param as "any"
    let params = new HttpParams();
    if (filters.remoteFromItaly !== null) {
      params = params.set('remoteFromItaly', filters.remoteFromItaly);
    }
    if (filters.salaryMin !== null) {
      params = params.set('salaryMin', filters.salaryMin);
    }
    return this.http.get<Job[]>(this.url, { params });
  }

  create(job: JobRequest): Observable<Job> {
    return this.http.post<Job>(this.url, job);
  }

  update(id: number, job: JobRequest): Observable<Job> {
    return this.http.put<Job>(`${this.url}/${id}`, job);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
