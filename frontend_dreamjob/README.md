# DreamJob – frontend

Interfaccia Angular per il backend Spring Boot di DreamJob: annunci di lavoro e candidature.

- **Annunci** (`/annunci`): lista con filtri (remote dall'Italia, stipendio minimo), creazione,
  modifica ed eliminazione; il pulsante "Candidati" registra una candidatura inviata oggi.
- **Candidature** (`/candidature`): riepilogo per stato (cliccabile per filtrare), cambio di stato
  e data salvati subito, eliminazione.

## Requisiti

- Node.js **22.22.3+**, **24.15+** o **26+** (lo richiede Angular CLI 22)
- Il backend in esecuzione su `http://localhost:8080` (vedi la cartella principale del repository)

## Sviluppo

```bash
npm install
npm start
```

Apri `http://localhost:4200/`. In sviluppo le chiamate a `/api` sono inoltrate al backend da
`proxy.conf.json`, quindi non serve configurare CORS. Se il backend è spento l'interfaccia lo
segnala con un messaggio e un pulsante "Riprova".

## Test e build

```bash
npm test -- --watch=false   # unit test con Vitest
npm run build               # build di produzione in dist/
npx prettier --check "src/**/*.{ts,html,scss}"
```

## Struttura

```
src/app/
  core/           modelli, servizi HTTP (JobApi, ApplicationApi), messaggi di errore, formattazione
  jobs/           pagina Annunci e form di creazione/modifica
  applications/   pagina Candidature
```

I componenti sono standalone, usano i signal e `ChangeDetectionStrategy.OnPush`; i dati arrivano
dal backend tramite `rxResource`. Lo stile condiviso (colori, pulsanti, campi, tema scuro) è in
`src/styles.scss`.
