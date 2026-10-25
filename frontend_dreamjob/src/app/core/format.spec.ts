import { formatSalary, todayIso } from './format';

// Intl separates number and € with a non-breaking space
const normalize = (text: string) => text.replace(/\s/g, ' ');

describe('formatSalary', () => {
  it('mostra la fascia quando ci sono minimo e massimo', () => {
    expect(normalize(formatSalary(37000, 50000))).toBe('37.000 € – 50.000 €');
  });

  it('gestisce un solo estremo o nessuno', () => {
    expect(normalize(formatSalary(40000, null))).toBe('da 40.000 €');
    expect(normalize(formatSalary(null, 60000))).toBe('fino a 60.000 €');
    expect(formatSalary(null, null)).toBe('Stipendio non indicato');
  });

  it('mostra un solo valore se minimo e massimo coincidono', () => {
    expect(normalize(formatSalary(45000, 45000))).toBe('45.000 €');
  });
});

describe('todayIso', () => {
  it('usa la data locale nel formato yyyy-MM-dd', () => {
    expect(todayIso(new Date(2026, 0, 5, 23, 30))).toBe('2026-01-05');
  });
});
