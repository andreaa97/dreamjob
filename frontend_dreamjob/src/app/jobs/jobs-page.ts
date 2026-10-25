import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { apiErrorMessage } from '../core/api-error';
import { ApplicationApi } from '../core/application-api';
import { formatSalary, todayIso } from '../core/format';
import { JobApi } from '../core/job-api';
import { Job, JobFilters, JobRequest, STATUS_LABELS } from '../core/models';
import { JobForm } from './job-form';

interface Message {
  kind: 'success' | 'error';
  text: string;
}

const NO_FILTERS: JobFilters = { remoteFromItaly: null, salaryMin: null };

@Component({
  selector: 'app-jobs-page',
  imports: [JobForm, RouterLink],
  templateUrl: './jobs-page.html',
  styleUrl: './jobs-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JobsPage {
  private readonly jobApi = inject(JobApi);
  private readonly applicationApi = inject(ApplicationApi);

  protected readonly statusLabels = STATUS_LABELS;

  protected readonly filters = signal<JobFilters>(NO_FILTERS);
  protected readonly filtersActive = computed(
    () => this.filters().remoteFromItaly !== null || this.filters().salaryMin !== null,
  );

  // the backend filters the jobs; the request is re-sent whenever the filters change
  protected readonly jobs = rxResource({
    params: () => this.filters(),
    stream: ({ params }) => this.jobApi.search(params),
    defaultValue: [],
  });

  // used to show, on each job, whether there is already an application for it
  protected readonly applications = rxResource({
    stream: () => this.applicationApi.list(),
    defaultValue: [],
  });

  protected readonly rows = computed(() => {
    // value() throws while a resource is in error state
    const jobs = this.jobs.hasValue() ? this.jobs.value() : [];
    const applications = this.applications.hasValue() ? this.applications.value() : [];
    const applicationByJob = new Map(applications.map((a) => [a.jobId, a]));
    return jobs.map((job) => ({
      job,
      salary: formatSalary(job.salaryMin, job.salaryMax),
      application: applicationByJob.get(job.id),
    }));
  });

  protected readonly loadError = computed(() => {
    const error = this.jobs.error();
    return error ? apiErrorMessage(error) : null;
  });

  protected readonly editing = signal<Job | 'new' | null>(null);
  protected readonly saving = signal(false);
  protected readonly busyJobId = signal<number | null>(null);
  protected readonly message = signal<Message | null>(null);

  protected setRemoteFilter(value: string): void {
    const remoteFromItaly = value === '' ? null : value === 'true';
    this.filters.update((f) => ({ ...f, remoteFromItaly }));
  }

  protected setSalaryFilter(value: string): void {
    const salary = value === '' ? NaN : Number(value);
    const salaryMin = Number.isFinite(salary) && salary >= 0 ? salary : null;
    this.filters.update((f) => ({ ...f, salaryMin }));
  }

  protected resetFilters(): void {
    this.filters.set(NO_FILTERS);
  }

  protected openNew(): void {
    this.message.set(null);
    this.editing.set('new');
  }

  protected edit(job: Job): void {
    this.message.set(null);
    this.editing.set(job);
  }

  protected closeForm(): void {
    this.editing.set(null);
  }

  protected save(request: JobRequest): void {
    const editing = this.editing();
    if (!editing) {
      return;
    }
    const isNew = editing === 'new';
    this.saving.set(true);
    (isNew ? this.jobApi.create(request) : this.jobApi.update(editing.id, request))
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (job) => {
          this.editing.set(null);
          this.notify('success', `Annuncio "${job.title}" ${isNew ? 'creato' : 'aggiornato'}.`);
          this.jobs.reload();
        },
        error: (error) => this.notify('error', apiErrorMessage(error)),
      });
  }

  protected apply(job: Job): void {
    this.busyJobId.set(job.id);
    this.applicationApi
      .create({ jobId: job.id, status: 'APPLIED', appliedAt: todayIso() })
      .pipe(finalize(() => this.busyJobId.set(null)))
      .subscribe({
        next: () => {
          this.notify('success', `Candidatura per "${job.title}" registrata come inviata oggi.`);
          this.applications.reload();
        },
        error: (error) => {
          this.notify(
            'error',
            apiErrorMessage(error, 'Hai già una candidatura per questo annuncio.'),
          );
          this.applications.reload();
        },
      });
  }

  protected remove(job: Job): void {
    const blockedMessage = `"${job.title}" ha una candidatura: eliminala dalla pagina Candidature prima di eliminare l'annuncio.`;
    // the backend would answer 409: say it straight away, without asking for confirmation first
    if (this.rows().some((row) => row.job.id === job.id && row.application)) {
      this.notify('error', blockedMessage);
      return;
    }
    if (!confirm(`Eliminare l'annuncio "${job.title}"?`)) {
      return;
    }
    this.busyJobId.set(job.id);
    this.jobApi
      .delete(job.id)
      .pipe(finalize(() => this.busyJobId.set(null)))
      .subscribe({
        next: () => {
          const editing = this.editing();
          if (editing !== 'new' && editing?.id === job.id) {
            this.editing.set(null);
          }
          this.notify('success', `Annuncio "${job.title}" eliminato.`);
          this.jobs.reload();
        },
        error: (error) => {
          this.notify('error', apiErrorMessage(error, blockedMessage));
          this.applications.reload();
        },
      });
  }

  private notify(kind: Message['kind'], text: string): void {
    this.message.set({ kind, text });
  }
}
