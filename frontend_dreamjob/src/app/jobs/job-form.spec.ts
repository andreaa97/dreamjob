import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Job, JobRequest } from '../core/models';
import { JobForm } from './job-form';

describe('JobForm', () => {
  let fixture: ComponentFixture<JobForm>;
  let element: HTMLElement;
  let saved: JobRequest[];

  const input = (name: string) =>
    element.querySelector<HTMLInputElement>(`[formControlName="${name}"]`)!;

  const type = (name: string, value: string) => {
    const field = input(name);
    field.value = value;
    field.dispatchEvent(new Event('input'));
    field.dispatchEvent(new Event('blur'));
  };

  const submit = async () => {
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  };

  beforeEach(async () => {
    fixture = TestBed.createComponent(JobForm);
    element = fixture.nativeElement;
    saved = [];
    fixture.componentInstance.save.subscribe((request) => saved.push(request));
    await fixture.whenStable();
  });

  it('non invia il form con campi obbligatori vuoti e mostra gli errori', async () => {
    type('title', '   ');
    await submit();

    expect(saved).toEqual([]);
    expect(element.textContent).toContain('Inserisci il ruolo.');
    expect(element.textContent).toContain("Inserisci l'azienda.");
  });

  it('blocca uno stipendio massimo minore del minimo, come il backend', async () => {
    type('title', 'Backend Developer');
    type('company', 'Acme');
    type('salaryMin', '50000');
    type('salaryMax', '30000');
    await submit();

    expect(saved).toEqual([]);
    expect(element.textContent).toContain('Il massimo deve essere maggiore o uguale al minimo.');
  });

  it('invia i dati puliti: testo senza spazi ai lati e sede vuota come null', async () => {
    type('title', '  Backend Developer ');
    type('company', 'Acme');
    type('salaryMin', '40000');
    await submit();

    expect(saved).toEqual([
      {
        title: 'Backend Developer',
        company: 'Acme',
        location: null,
        salaryMin: 40000,
        salaryMax: null,
        remoteFromItaly: true,
      },
    ]);
  });

  it('precompila il form con il job da modificare', async () => {
    const job: Job = {
      id: 3,
      title: 'Spring Boot Engineer',
      company: 'Globex',
      location: 'Milano',
      salaryMin: 45000,
      salaryMax: 60000,
      remoteFromItaly: false,
    };
    fixture.componentRef.setInput('job', job);
    await fixture.whenStable();

    expect(input('title').value).toBe('Spring Boot Engineer');
    expect(input('salaryMax').value).toBe('60000');
    expect(input('remoteFromItaly').checked).toBe(false);
    expect(element.querySelector('h2')?.textContent).toContain('Modifica annuncio');
  });
});
