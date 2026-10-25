import { ChangeDetectionStrategy, Component, effect, inject, input, output } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Job, JobRequest } from '../core/models';

// same rules as the backend JobRequest: @NotBlank, @PositiveOrZero, salaryMax >= salaryMin
function notBlank(control: AbstractControl<string>): ValidationErrors | null {
  return control.value.trim() ? null : { required: true };
}

function salaryRange(group: AbstractControl): ValidationErrors | null {
  const min = group.get('salaryMin')?.value as number | null;
  const max = group.get('salaryMax')?.value as number | null;
  return min !== null && max !== null && max < min ? { salaryRange: true } : null;
}

@Component({
  selector: 'app-job-form',
  imports: [ReactiveFormsModule],
  templateUrl: './job-form.html',
  styleUrl: './job-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JobForm {
  /** The job to edit, or null to create a new one. */
  readonly job = input<Job | null>(null);
  readonly saving = input(false);

  readonly save = output<JobRequest>();
  readonly cancelled = output<void>();

  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly form = this.fb.group(
    {
      title: ['', notBlank],
      company: ['', notBlank],
      location: [''],
      salaryMin: this.fb.control<number | null>(null, Validators.min(0)),
      salaryMax: this.fb.control<number | null>(null, Validators.min(0)),
      remoteFromItaly: [true],
    },
    { validators: salaryRange },
  );

  constructor() {
    // refill the form whenever a different job is opened
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

  protected showError(name: 'title' | 'company' | 'salaryMin' | 'salaryMax'): boolean {
    const control = this.form.controls[name];
    return control.invalid && control.touched;
  }

  protected showSalaryRangeError(): boolean {
    const { salaryMin, salaryMax } = this.form.controls;
    return this.form.hasError('salaryRange') && (salaryMin.touched || salaryMax.touched);
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
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
