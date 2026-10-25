const euro = new Intl.NumberFormat('it-IT', {
  style: 'currency',
  currency: 'EUR',
  maximumFractionDigits: 0,
});

export function formatSalary(min: number | null, max: number | null): string {
  if (min !== null && max !== null) {
    return min === max ? euro.format(min) : `${euro.format(min)} – ${euro.format(max)}`;
  }
  if (min !== null) {
    return `da ${euro.format(min)}`;
  }
  if (max !== null) {
    return `fino a ${euro.format(max)}`;
  }
  return 'Stipendio non indicato';
}

/** Today's local date as yyyy-MM-dd (toISOString would use UTC and can be off by a day). */
export function todayIso(now = new Date()): string {
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
}
