import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { apiErrorMessage } from '../core/api-error';
import { ApplicationApi } from '../core/application-api';
import {
  APPLICATION_STATUSES,
  Application,
  ApplicationRequest,
  ApplicationStatus,
  STATUS_LABELS,
} from '../core/models';

/** Messaggio di esito mostrato in cima alla pagina. */
interface Message {
  kind: 'success' | 'error';
  text: string;
}

/**
 * Pagina "Candidature": riepilogo per stato (cliccabile come filtro) e tabella in cui
 * stato e data si salvano appena vengono cambiati.
 *
 * Le candidature vengono caricate una volta; dopo ogni modifica la lista locale viene
 * aggiornata con la risposta del backend, senza ricaricare tutto.
 */
@Component({
  selector: 'app-applications-page',
  imports: [RouterLink],
  templateUrl: './applications-page.html',
  styleUrl: './applications-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationsPage {
  private readonly api = inject(ApplicationApi);

  protected readonly statuses = APPLICATION_STATUSES;
  protected readonly labels = STATUS_LABELS;

  // senza `params`: la richiesta parte una sola volta, all'apertura della pagina
  protected readonly applications = rxResource({
    stream: () => this.api.list(),
    defaultValue: [],
  });

  /** Stato selezionato nel riepilogo; null = mostra tutte. */
  protected readonly statusFilter = signal<ApplicationStatus | null>(null);
  /** Candidatura che si sta salvando o eliminando: i suoi controlli sono disabilitati. */
  protected readonly busyId = signal<number | null>(null);
  protected readonly message = signal<Message | null>(null);

  // value() lancia un'eccezione se la resource è in errore: in quel caso lista vuota
  private readonly all = computed(() =>
    this.applications.hasValue() ? this.applications.value() : [],
  );

  protected readonly total = computed(() => this.all().length);

  /** Numero di candidature per ogni stato, compresi quelli a zero. */
  protected readonly counts = computed(() => {
    const counts = Object.fromEntries(this.statuses.map((s) => [s, 0])) as Record<
      ApplicationStatus,
      number
    >;
    for (const application of this.all()) {
      counts[application.status]++;
    }
    return counts;
  });

  // Righe della tabella: filtrate per stato e ordinate dalla più recente; quelle senza data
  // vanno in fondo. Le date yyyy-MM-dd si possono confrontare come stringhe.
  // filter() crea un nuovo array, quindi sort() non modifica il valore della resource.
  protected readonly visible = computed(() => {
    const filter = this.statusFilter();
    return this.all()
      .filter((a) => filter === null || a.status === filter)
      .sort((a, b) => (b.appliedAt ?? '').localeCompare(a.appliedAt ?? '') || b.id - a.id);
  });

  protected readonly loadError = computed(() => {
    const error = this.applications.error();
    return error ? apiErrorMessage(error) : null;
  });

  /** Clic su uno stato del riepilogo: lo seleziona, oppure lo deseleziona se era già attivo. */
  protected toggleFilter(status: ApplicationStatus): void {
    this.statusFilter.update((current) => (current === status ? null : status));
  }

  // Le select e i campi data ricevono l'elemento HTML: se il salvataggio fallisce,
  // il metodo di ripristino rimette nel controllo il valore che è ancora salvato.
  protected changeStatus(application: Application, select: HTMLSelectElement): void {
    this.save(application, { status: select.value as ApplicationStatus }, () => {
      select.value = application.status;
    });
  }

  protected changeDate(application: Application, input: HTMLInputElement): void {
    // data cancellata = candidatura senza data (null)
    this.save(application, { appliedAt: input.value || null }, () => {
      input.value = application.appliedAt ?? '';
    });
  }

  protected remove(application: Application): void {
    if (!confirm(`Eliminare la candidatura per "${application.jobTitle}"?`)) {
      return;
    }
    this.busyId.set(application.id);
    this.api
      .delete(application.id)
      .pipe(finalize(() => this.busyId.set(null)))
      .subscribe({
        next: () => {
          // tolta dalla lista locale, senza una nuova richiesta al backend
          this.applications.update((list) => list.filter((a) => a.id !== application.id));
          this.notify('success', `Candidatura per "${application.jobTitle}" eliminata.`);
        },
        error: (error) => {
          this.notify('error', apiErrorMessage(error));
          this.applications.reload();
        },
      });
  }

  /** Invia la candidatura completa (l'API vuole un PUT con tutti i campi) con i campi cambiati. */
  private save(
    application: Application,
    changes: Partial<ApplicationRequest>,
    revert: () => void,
  ): void {
    // parte dai valori salvati e sovrascrive solo quelli cambiati
    const request: ApplicationRequest = {
      jobId: application.jobId,
      status: application.status,
      appliedAt: application.appliedAt,
      ...changes,
    };
    this.busyId.set(application.id);
    this.api
      .update(application.id, request)
      .pipe(finalize(() => this.busyId.set(null)))
      .subscribe({
        next: (updated) => {
          // sostituisce la candidatura con quella restituita dal backend: contatori e
          // ordinamento si aggiornano da soli perché derivano dalla lista (computed)
          this.applications.update((list) => list.map((a) => (a.id === updated.id ? updated : a)));
          this.message.set(null);
        },
        error: (error) => {
          // il controllo mostra già il nuovo valore: rimettiamo quello salvato
          revert();
          this.notify('error', apiErrorMessage(error));
        },
      });
  }

  private notify(kind: Message['kind'], text: string): void {
    this.message.set({ kind, text });
  }
}
