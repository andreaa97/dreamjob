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

/** Messaggio di esito mostrato in cima alla pagina. */
interface Message {
  kind: 'success' | 'error';
  text: string;
}

const NO_FILTERS: JobFilters = { remoteFromItaly: null, salaryMin: null };

/**
 * Pagina "Annunci": lista dei job con filtri, creazione/modifica tramite JobForm,
 * eliminazione e pulsante "Candidati".
 *
 * Lo stato della pagina è fatto di signal: il template li legge e Angular lo aggiorna
 * solo quando cambiano. I dati del backend arrivano tramite `rxResource`, che gestisce
 * da solo caricamento, errori e nuove richieste.
 */
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

  // Il filtro lo applica il backend. `params` legge il signal dei filtri: quando cambia,
  // rxResource annulla l'eventuale richiesta in corso e ne invia una nuova.
  // defaultValue: la lista vale [] finché la prima risposta non arriva.
  protected readonly jobs = rxResource({
    params: () => this.filters(),
    stream: ({ params }) => this.jobApi.search(params),
    defaultValue: [],
  });

  // Serve per mostrare, su ogni annuncio, se c'è già una candidatura e in che stato è.
  protected readonly applications = rxResource({
    stream: () => this.applicationApi.list(),
    defaultValue: [],
  });

  // Dati pronti per il template: per ogni job la fascia di stipendio già formattata e
  // l'eventuale candidatura. computed() viene ricalcolato solo se jobs o applications cambiano.
  protected readonly rows = computed(() => {
    // value() lancia un'eccezione se la resource è in errore: in quel caso lista vuota
    const jobs = this.jobs.hasValue() ? this.jobs.value() : [];
    const applications = this.applications.hasValue() ? this.applications.value() : [];
    // Map jobId -> candidatura: ricerca immediata invece di scorrere la lista per ogni job
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

  /** Cosa mostra il form: un job da modificare, 'new' per crearne uno, null se chiuso. */
  protected readonly editing = signal<Job | 'new' | null>(null);
  protected readonly saving = signal(false);
  /** Job su cui è in corso un'operazione (candidatura o eliminazione): pulsanti disabilitati. */
  protected readonly busyJobId = signal<number | null>(null);
  protected readonly message = signal<Message | null>(null);

  /** Valore della select: '' = tutti, 'true' = remoti, 'false' = non remoti. */
  protected setRemoteFilter(value: string): void {
    const remoteFromItaly = value === '' ? null : value === 'true';
    this.filters.update((f) => ({ ...f, remoteFromItaly }));
  }

  /** Campo vuoto o non valido = nessun filtro sullo stipendio. */
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

  /** Riceve i dati dal JobForm: crea (POST) o aggiorna (PUT) a seconda di cosa è aperto. */
  protected save(request: JobRequest): void {
    const editing = this.editing();
    if (!editing) {
      return;
    }
    const isNew = editing === 'new';
    this.saving.set(true);
    (isNew ? this.jobApi.create(request) : this.jobApi.update(editing.id, request))
      // finalize: riabilita il pulsante sia in caso di successo sia di errore
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (job) => {
          this.editing.set(null);
          this.notify('success', `Annuncio "${job.title}" ${isNew ? 'creato' : 'aggiornato'}.`);
          // ricarica la lista mantenendo i filtri correnti
          this.jobs.reload();
        },
        error: (error) => this.notify('error', apiErrorMessage(error)),
      });
  }

  /** Pulsante "Candidati": crea una candidatura con stato "Inviata" e data di oggi. */
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
          // 409: la candidatura esiste già (es. creata da un'altra scheda)
          this.notify(
            'error',
            apiErrorMessage(error, 'Hai già una candidatura per questo annuncio.'),
          );
          // in ogni caso ricarichiamo, così la card mostra lo stato reale
          this.applications.reload();
        },
      });
  }

  protected remove(job: Job): void {
    const blockedMessage = `"${job.title}" ha una candidatura: eliminala dalla pagina Candidature prima di eliminare l'annuncio.`;
    // il backend risponderebbe 409: lo diciamo subito, senza prima chiedere conferma
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
          // se il job eliminato era aperto nel form, chiudiamo il form
          const editing = this.editing();
          if (editing !== 'new' && editing?.id === job.id) {
            this.editing.set(null);
          }
          this.notify('success', `Annuncio "${job.title}" eliminato.`);
          this.jobs.reload();
        },
        error: (error) => {
          // 409 se nel frattempo è stata creata una candidatura: aggiorniamo le candidature
          this.notify('error', apiErrorMessage(error, blockedMessage));
          this.applications.reload();
        },
      });
  }

  private notify(kind: Message['kind'], text: string): void {
    this.message.set({ kind, text });
  }
}
