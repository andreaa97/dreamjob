// Formattatore di valuta creato una sola volta: euro in formato italiano, senza decimali
// (es. "37.000 €").
const euro = new Intl.NumberFormat('it-IT', {
  style: 'currency',
  currency: 'EUR',
  maximumFractionDigits: 0,
});

/**
 * Testo della fascia di stipendio di un annuncio, gestendo i valori mancanti:
 * "37.000 € – 50.000 €", "da 40.000 €", "fino a 60.000 €" o "Stipendio non indicato".
 */
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

/**
 * La data di oggi, nel fuso locale, come yyyy-MM-dd (il formato che il backend si aspetta).
 * Non si usa toISOString() perché converte in UTC: dopo mezzanotte darebbe il giorno prima.
 */
export function todayIso(now = new Date()): string {
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
}
