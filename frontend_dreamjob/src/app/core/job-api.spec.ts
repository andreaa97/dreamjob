import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { JobApi } from './job-api';

describe('JobApi', () => {
  let api: JobApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(JobApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('senza filtri non invia parametri', () => {
    api.search({ remoteFromItaly: null, salaryMin: null }).subscribe();

    const request = http.expectOne('/api/jobs');
    expect(request.request.params.keys()).toEqual([]);
    request.flush([]);
  });

  it('invia solo i filtri impostati, compreso remoteFromItaly=false', () => {
    api.search({ remoteFromItaly: false, salaryMin: 40000 }).subscribe();

    const request = http.expectOne((r) => r.url === '/api/jobs');
    expect(request.request.params.get('remoteFromItaly')).toBe('false');
    expect(request.request.params.get('salaryMin')).toBe('40000');
    request.flush([]);
  });

  it('elimina con DELETE sul singolo job', () => {
    api.delete(7).subscribe();

    const request = http.expectOne('/api/jobs/7');
    expect(request.request.method).toBe('DELETE');
    request.flush(null, { status: 204, statusText: 'No Content' });
  });
});
