import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { todayIso } from '../core/format';
import { Application, Job } from '../core/models';
import { JobsPage } from './jobs-page';

const JOBS: Job[] = [
  {
    id: 1,
    title: 'Backend Java Developer',
    company: 'Umbrella',
    location: 'Remote',
    salaryMin: 37000,
    salaryMax: 50000,
    remoteFromItaly: true,
  },
  {
    id: 2,
    title: 'Spring Boot Engineer',
    company: 'Globex',
    location: 'Milano',
    salaryMin: 45000,
    salaryMax: 60000,
    remoteFromItaly: false,
  },
];

const APPLICATIONS: Application[] = [
  {
    id: 10,
    jobId: 2,
    jobTitle: 'Spring Boot Engineer',
    status: 'INTERVIEW',
    appliedAt: '2026-10-07',
  },
];

describe('JobsPage', () => {
  let fixture: ComponentFixture<JobsPage>;
  let http: HttpTestingController;
  let element: HTMLElement;

  const cards = () => [...element.querySelectorAll<HTMLElement>('.job')];
  const card = (title: string) => cards().find((c) => c.textContent?.includes(title))!;
  const button = (container: HTMLElement, label: string) =>
    [...container.querySelectorAll('button')].find((b) => b.textContent?.trim() === label)!;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(JobsPage);
    element = fixture.nativeElement;

    // the resources send their requests from an effect
    TestBed.tick();
    http.expectOne((r) => r.url === '/api/jobs').flush(JOBS);
    http.expectOne('/api/applications').flush(APPLICATIONS);
    await fixture.whenStable();
  });

  afterEach(() => {
    http.verify();
    vi.restoreAllMocks();
  });

  it('mostra gli annunci con fascia di stipendio e stato della candidatura', () => {
    expect(cards().length).toBe(2);

    const free = card('Backend Java Developer');
    expect(free.textContent).toContain("Remote dall'Italia");
    expect(free.textContent?.replace(/\s/g, ' ')).toContain('37.000 € – 50.000 €');
    expect(button(free, 'Candidati')).toBeTruthy();

    const applied = card('Spring Boot Engineer');
    expect(applied.querySelector('.status')?.textContent).toContain('Colloquio');
    expect(button(applied, 'Candidati')).toBeUndefined();
  });

  it('cambiando un filtro richiede di nuovo gli annunci al backend', async () => {
    const select = element.querySelector('select')!;
    select.value = 'true';
    select.dispatchEvent(new Event('change'));
    TestBed.tick();

    const request = http.expectOne((r) => r.url === '/api/jobs');
    expect(request.request.params.get('remoteFromItaly')).toBe('true');
    request.flush([JOBS[0]]);
    await fixture.whenStable();

    expect(cards().length).toBe(1);
    expect(element.textContent).toContain('Azzera filtri');
  });

  it('"Candidati" registra una candidatura inviata oggi', async () => {
    button(card('Backend Java Developer'), 'Candidati').click();

    const request = http.expectOne({ method: 'POST', url: '/api/applications' });
    expect(request.request.body).toEqual({ jobId: 1, status: 'APPLIED', appliedAt: todayIso() });
    request.flush({
      id: 11,
      jobId: 1,
      jobTitle: 'Backend Java Developer',
      status: 'APPLIED',
      appliedAt: todayIso(),
    });

    TestBed.tick();
    http.expectOne('/api/applications').flush([
      ...APPLICATIONS,
      {
        id: 11,
        jobId: 1,
        jobTitle: 'Backend Java Developer',
        status: 'APPLIED',
        appliedAt: todayIso(),
      },
    ]);
    await fixture.whenStable();

    expect(element.querySelector('[role="status"]')?.textContent).toContain(
      'Candidatura per "Backend Java Developer"',
    );
    expect(card('Backend Java Developer').querySelector('.status')?.textContent).toContain(
      'Inviata',
    );
  });

  it('non elimina un annuncio che ha una candidatura e spiega perché', async () => {
    const confirmSpy = vi.spyOn(window, 'confirm');

    button(card('Spring Boot Engineer'), 'Elimina').click();
    await fixture.whenStable();

    expect(confirmSpy).not.toHaveBeenCalled();
    expect(element.querySelector('[role="alert"]')?.textContent).toContain('ha una candidatura');
    // nessuna DELETE inviata: lo controlla http.verify() in afterEach
  });

  it('elimina un annuncio senza candidature dopo la conferma', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    button(card('Backend Java Developer'), 'Elimina').click();
    http.expectOne({ method: 'DELETE', url: '/api/jobs/1' }).flush(null);

    TestBed.tick();
    http.expectOne((r) => r.url === '/api/jobs').flush([JOBS[1]]);
    await fixture.whenStable();

    expect(cards().length).toBe(1);
    expect(element.querySelector('[role="status"]')?.textContent).toContain('eliminato');
  });
});

describe('JobsPage con backend non raggiungibile', () => {
  it('mostra un messaggio chiaro e permette di riprovare', async () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(JobsPage);

    TestBed.tick();
    http.expectOne((r) => r.url === '/api/jobs').error(new ProgressEvent('error'));
    http.expectOne('/api/applications').error(new ProgressEvent('error'));
    await fixture.whenStable();

    const alert = (fixture.nativeElement as HTMLElement).querySelector('[role="alert"]');
    expect(alert?.textContent).toContain('Impossibile contattare il server');
    expect(alert?.textContent).toContain('Riprova');
    http.verify();
  });
});
