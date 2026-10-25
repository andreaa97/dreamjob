// Mirror of the Spring Boot API (com.andrea.remoteflow.dto / entity).

export interface Job {
  id: number;
  title: string;
  company: string;
  location: string | null;
  salaryMin: number | null;
  salaryMax: number | null;
  remoteFromItaly: boolean;
}

export type JobRequest = Omit<Job, 'id'>;

export interface JobFilters {
  remoteFromItaly: boolean | null;
  salaryMin: number | null;
}

export const APPLICATION_STATUSES = [
  'SAVED',
  'APPLIED',
  'INTERVIEW',
  'OFFER',
  'REJECTED',
  'WITHDRAWN',
] as const;

export type ApplicationStatus = (typeof APPLICATION_STATUSES)[number];

export const STATUS_LABELS: Record<ApplicationStatus, string> = {
  SAVED: 'Salvata',
  APPLIED: 'Inviata',
  INTERVIEW: 'Colloquio',
  OFFER: 'Offerta ricevuta',
  REJECTED: 'Rifiutata',
  WITHDRAWN: 'Ritirata',
};

export interface Application {
  id: number;
  jobId: number;
  jobTitle: string;
  status: ApplicationStatus;
  /** ISO date, yyyy-MM-dd */
  appliedAt: string | null;
}

export interface ApplicationRequest {
  jobId: number;
  status: ApplicationStatus;
  appliedAt: string | null;
}
