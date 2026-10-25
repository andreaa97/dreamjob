// Tipi dei dati scambiati con il backend Spring Boot.
// Devono restare allineati ai DTO e alle entita' Java (package com.andrea.remoteflow).

/** Annuncio di lavoro, come restituito da /api/jobs (entita' Job). */
export interface Job {
  id: number;
  title: string;
  company: string;
  location: string | null;
  /** stipendio annuo in euro, null se non indicato */
  salaryMin: number | null;
  salaryMax: number | null;
  remoteFromItaly: boolean;
}

/** Body di POST/PUT /api/jobs (JobRequest): un Job senza id, che lo assegna il database. */
export type JobRequest = Omit<Job, 'id'>;

/** Filtri della ricerca annunci; null significa "nessun filtro su questo campo". */
export interface JobFilters {
  remoteFromItaly: boolean | null;
  salaryMin: number | null;
}

/**
 * Stati di una candidatura, nello stesso ordine dell'enum Java ApplicationStatus.
 * `as const` fa nascere da questa lista sia i valori sia il tipo ApplicationStatus.
 */
export const APPLICATION_STATUSES = [
  'SAVED',
  'APPLIED',
  'INTERVIEW',
  'OFFER',
  'REJECTED',
  'WITHDRAWN',
] as const;

export type ApplicationStatus = (typeof APPLICATION_STATUSES)[number];

/** Etichetta in italiano di ogni stato; Record obbliga a non dimenticarne nessuno. */
export const STATUS_LABELS: Record<ApplicationStatus, string> = {
  SAVED: 'Salvata',
  APPLIED: 'Inviata',
  INTERVIEW: 'Colloquio',
  OFFER: 'Offerta ricevuta',
  REJECTED: 'Rifiutata',
  WITHDRAWN: 'Ritirata',
};

/** Candidatura, come restituita da /api/applications (ApplicationResponse). */
export interface Application {
  id: number;
  jobId: number;
  jobTitle: string;
  status: ApplicationStatus;
  /** data ISO, yyyy-MM-dd */
  appliedAt: string | null;
}

/** Body di POST/PUT /api/applications (ApplicationRequest). */
export interface ApplicationRequest {
  jobId: number;
  status: ApplicationStatus;
  appliedAt: string | null;
}
