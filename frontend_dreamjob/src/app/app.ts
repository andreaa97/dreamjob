import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

/**
 * Componente radice: la cornice comune a tutte le pagine (barra in alto con la navigazione).
 * La pagina corrente viene mostrata al posto di `<router-outlet />` nel template.
 *
 * Tutti i componenti usano OnPush: Angular li aggiorna solo quando cambia un loro input,
 * un signal letto nel template o arriva un evento, invece che a ogni ciclo.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {}
