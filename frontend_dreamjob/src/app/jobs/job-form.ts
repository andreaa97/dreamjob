import { ChangeDetectionStrategy, Component, effect, inject, input, output } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Job, JobRequest } from '../core/models';

// Validatori con le stesse regole del JobRequest del backend
// (@NotBlank, @PositiveOrZero, salaryMax >= salaryMin): l'utente vede l'errore prima di inviare.

/** Campo obbligatorio che non può contenere solo spazi (come @NotBlank). */
function notBlank(control: AbstractControl<string>): ValidationErrors | null {
  return control.value.trim() ? null : { required: true };
}

/** Validatore di gruppo: confronta i due campi stipendio tra loro. */
function salaryRange(group: AbstractControl): ValidationErrors | null {
  const min = group.get('salaryMin')?.value as number | null;
  const max = group.get('salaryMax')?.value as number | null;
  return min !== null && max !== null && max < min ? { salaryRange: true } : null;
}

/**
 * Form per creare o modificare un annuncio.
 *
 * È un componente "di presentazione": non chiama il backend. Riceve il job da modificare
 * e lo stato di salvataggio come input, e comunica con il padre (JobsPage) tramite gli output
 * `save` (dati validi e già puliti) e `cancelled`.
 */
@Component({
  selector: 'app-job-form',
  imports: [ReactiveFormsModule],
  templateUrl: './job-form.html',
  styleUrl: './job-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JobForm {
  /** Il job da modificare, oppure null per crearne uno nuovo. */
  readonly job = input<Job | null>(null);
  /** true mentre il padre sta salvando: disabilita il pulsante di invio. */
  readonly saving = input(false);

  readonly save = output<JobRequest>();
  readonly cancelled = output<void>();

  // NonNullableFormBuilder: reset() riporta i campi al valore iniziale invece che a null
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly form = this.fb.group(
    {
      title: ['', notBlank],
      company: ['', notBlank],
      location: [''],
      // gli stipendi possono restare vuoti, quindi il tipo ammette null
      salaryMin: this.fb.control<number | null>(null, Validators.min(0)),
      salaryMax: this.fb.control<number | null>(null, Validators.min(0)),
      remoteFromItaly: [true],
    },
    { validators: salaryRange },
  );

  constructor() {
    // L'effect si riesegue ogni volta che cambia l'input `job`: quando si apre un altro
    // annuncio (o "Nuovo annuncio") il form viene riempito con i suoi dati, o svuotato.
    effect(() => {
      const job = this.job();
      this.form.reset({
        title: job?.title ?? '',
        company: job?.company ?? '',
        location: job?.location ?? '',
        salaryMin: job?.salaryMin ?? null,
        salaryMax: job?.salaryMax ?? null,
        remoteFromItaly: job?.remoteFromItaly ?? true,
      });
    });
  }

  /** Mostra l'errore di un campo solo dopo che l'utente ci è passato (touched). */
  protected showError(name: 'title' | 'company' | 'salaryMin' | 'salaryMax'): boolean {
    const control = this.form.controls[name];
    return control.invalid && control.touched;
  }

  /** Errore di gruppo "massimo < minimo", visibile dopo aver toccato uno dei due stipendi. */
  protected showSalaryRangeError(): boolean {
    const { salaryMin, salaryMax } = this.form.controls;
    return this.form.hasError('salaryRange') && (salaryMin.touched || salaryMax.touched);
  }

  protected submit(): void {
    if (this.form.invalid) {
      // segna tutti i campi come toccati, così compaiono tutti gli errori insieme
      this.form.markAllAsTouched();
      return;
    }
    // pulizia prima dell'invio: niente spazi ai lati, sede vuota inviata come null
    const value = this.form.getRawValue();
    this.save.emit({
      title: value.title.trim(),
      company: value.company.trim(),
      location: value.location.trim() || null,
      salaryMin: value.salaryMin,
      salaryMax: value.salaryMax,
      remoteFromItaly: value.remoteFromItaly,
    });
  }
}
