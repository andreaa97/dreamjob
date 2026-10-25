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

interface Message {
  kind: 'success' | 'error';
  text: string;
}

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

  protected readonly applications = rxResource({
    stream: () => this.api.list(),
    defaultValue: [],
  });

  protected readonly statusFilter = signal<ApplicationStatus | null>(null);
  protected readonly busyId = signal<number | null>(null);
  protected readonly message = signal<Message | null>(null);

  // value() throws while the resource is in error state
  private readonly all = computed(() =>
    this.applications.hasValue() ? this.applications.value() : [],
  );

  protected readonly total = computed(() => this.all().length);

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

  // most recent first; applications without a date at the end
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

  protected toggleFilter(status: ApplicationStatus): void {
    this.statusFilter.update((current) => (current === status ? null : status));
  }

  protected changeStatus(application: Application, select: HTMLSelectElement): void {
    this.save(application, { status: select.value as ApplicationStatus }, () => {
      select.value = application.status;
    });
  }

  protected changeDate(application: Application, input: HTMLInputElement): void {
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
          this.applications.update((list) => list.filter((a) => a.id !== application.id));
          this.notify('success', `Candidatura per "${application.jobTitle}" eliminata.`);
        },
        error: (error) => {
          this.notify('error', apiErrorMessage(error));
          this.applications.reload();
        },
      });
  }

  /** Sends the whole application (the API expects a full PUT) with the changed fields. */
  private save(
    application: Application,
    changes: Partial<ApplicationRequest>,
    revert: () => void,
  ): void {
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
          this.applications.update((list) => list.map((a) => (a.id === updated.id ? updated : a)));
          this.message.set(null);
        },
        error: (error) => {
          // the control already shows the new value: put back the saved one
          revert();
          this.notify('error', apiErrorMessage(error));
        },
      });
  }

  private notify(kind: Message['kind'], text: string): void {
    this.message.set({ kind, text });
  }
}
