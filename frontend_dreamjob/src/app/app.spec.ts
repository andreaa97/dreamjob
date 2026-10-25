import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('mostra il marchio e la navigazione tra le sezioni', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('.brand')?.textContent).toContain('DreamJob');
    const links = [...compiled.querySelectorAll('nav a')].map((a) => a.getAttribute('href'));
    expect(links).toEqual(['/annunci', '/candidature']);
  });
});
