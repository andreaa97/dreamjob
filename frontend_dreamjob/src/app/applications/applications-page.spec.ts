import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Application } from '../core/models';
import { ApplicationsPage } from './applications-page';

const APPLICATIONS: Application[] = [
  {
    id: 1,
    jobId: 1,
    jobTitle: 'Backend Java Developer',
    status: 'APPLIED',
    appliedAt: '2026-10-01',
  },
  {
    id: 2,
    jobId: 2,
    jobTitle: 'Spring Boot Engineer',
    status: 'INTERVIEW',
    appliedAt: '2026-10-07',
  },
  { id: 3, jobId: 3, jobTitle: 'Java Consultant', status: 'SAVED', appliedAt: null },
];

describe('ApplicationsPage', () => {
  let fixture: ComponentFixture<ApplicationsPage>;
  let http: HttpTestingController;
  let element: HTMLElement;

  const rowTitles = () =>
    [...element.querySelectorAll('tbody th')].map((th) => th.textContent?.trim());
  const statusSelect = (title: string) =>
    element.querySelector<HTMLSelectElement>(
      `select[aria-label="Stato della candidatura per ${title}"]`,
    )!;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ApplicationsPage);
    element = fixture.nativeElement;

    TestBed.tick();
    http.expectOne('/api/applications').flush(APPLICATIONS);
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('ordina dalla più recente e mette in fondo quelle senza data', () => {
    expect(rowTitles()).toEqual([
      'Spring Boot Engineer',
      'Backend Java Developer',
      'Java Consultant',
    ]);
    expect(statusSelect('Spring Boot Engineer').value).toBe('INTERVIEW');
  });

  it('conta le candidature per stato e filtra cliccando uno stato', async () => {
    const interview = element.querySelector<HTMLButtonElement>(
      '.pipeline__step[data-status="INTERVIEW"]',
    )!;
    expect(interview.textContent).toContain('1');

    interview.click();
    await fixture.whenStable();

    expect(interview.getAttribute('aria-pressed')).toBe('true');
    expect(rowTitles()).toEqual(['Spring Boot Engineer']);
  });

  it('cambiando lo stato invia la candidatura completa con PUT', async () => {
    const select = statusSelect('Backend Java Developer');
    select.value = 'OFFER';
    select.dispatchEvent(new Event('change'));

    const request = http.expectOne({ method: 'PUT', url: '/api/applications/1' });
    expect(request.request.body).toEqual({ jobId: 1, status: 'OFFER', appliedAt: '2026-10-01' });
    request.flush({ ...APPLICATIONS[0], status: 'OFFER' });
    await fixture.whenStable();

    expect(element.querySelector('.pipeline__step[data-status="OFFER"]')?.textContent).toContain(
      '1',
    );
    expect(element.querySelector('.pipeline__step[data-status="APPLIED"]')?.textContent).toContain(
      '0',
    );
  });

  it('se il salvataggio fallisce rimette lo stato precedente e lo segnala', async () => {
    const select = statusSelect('Backend Java Developer');
    select.value = 'REJECTED';
    select.dispatchEvent(new Event('change'));

    http
      .expectOne({ method: 'PUT', url: '/api/applications/1' })
      .flush(null, { status: 404, statusText: 'Not Found' });
    await fixture.whenStable();

    expect(select.value).toBe('APPLIED');
    expect(element.querySelector('[role="alert"]')?.textContent).toContain('non esiste più');
  });
});
