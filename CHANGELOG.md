# Changelog

## 1.9.0 — 2026-09-19

### English

- The widget library excludes unconfigured launcher slots and entries no longer associated with a widget.
- Long-press an item to select it, select multiple items and use the shared trash button. Templates are deleted; widget entries are hidden without resetting their Home settings.
- Widgets set to **System** follow light/dark mode without opening the configurator or running a background service.

### Italiano

- L’elenco dei widget esclude gli slot del launcher non configurati e le voci non più associate a un widget.
- Tocco prolungato per selezionare gli elementi, selezione multipla e cestino unico. I modelli vengono eliminati; le voci widget vengono nascoste senza azzerare le impostazioni sulla Home.
- I widget impostati su **Sistema** seguono il tema chiaro/scuro senza aprire il configuratore o eseguire un servizio in background.

## 1.8.1 — 2026-09-14

### English

- Renamed the **Circle** subtab to **Button shape**.
- Updated the **Flower** shape.
- Added **Clover, Leaf, Pebble, Scallop and Teardrop**.
- Backups created before this update remain compatible.

### Italiano

- Rinominata la sotto-scheda **Cerchio** in **Forma pulsante**.
- Aggiornata la forma **Fiore**.
- Aggiunte **Clover, Leaf, Pebble, Scallop e Teardrop**.
- Mantenuta la compatibilità con i backup creati prima dell’aggiornamento.

## 1.7.0 — 2026-09-13

### English

- Added an independent **Monochrome icon** switch to the logo and each action button, enabled by default for new and existing configurations.
- Turning it off preserves imported image colors and transparency and enables the native palettes of the Google and Chrome symbols. Material symbols retain their source single-color fill.
- Original colors bypass Material You tint, saved icon gradients and tint opacity. Manual color controls are disabled with localized explanations; their saved values are preserved.
- The setting persists in configurations, templates and backups. Older backups retain monochrome defaults; the new field is validated as a boolean.
- Updated both READMEs with setup instructions and backup compatibility notes.

Validation: debug build succeeded; 9 JVM and 38 Android tests passed; lint reported 0 errors and 64 warnings. Android tests ran on an AOSP API 35 emulator. The debug signing certificate matches 1.6.1, allowing installation as an update.

### Italiano

- Aggiunto lo switch indipendente **Icona monocromatica** per logo e ciascun pulsante, attivo di default nelle configurazioni nuove ed esistenti.
- Disattivandolo si conservano colori e trasparenza delle immagini importate e si usano le palette native dei simboli Google e Chrome. I simboli Material mantengono il riempimento originale a colore singolo.
- I colori originali ignorano tinta Material You, gradienti e opacità della tinta salvata. I controlli manuali sono disabilitati con spiegazioni tradotte, conservando i valori impostati.
- La scelta persiste in configurazioni, modelli e backup. I vecchi backup mantengono il default monocromatico; il nuovo campo viene validato come booleano.
- Aggiornati entrambi i README con istruzioni e note di compatibilità dei backup.

Verifiche: build debug riuscita; 9 test JVM e 38 test Android superati; lint con 0 errori e 64 avvisi. Test Android eseguiti su emulatore AOSP API 35. La firma di debug coincide con quella della 1.6.1 e consente l’installazione come aggiornamento.
