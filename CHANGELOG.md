# Changelog

## 1.13.2 — 2026-10-08

### English

- Settings organised into grouped Material 3 Expressive cards, with icons, descriptions and dedicated detail screens.
- Layout groups overall height, button count, layer order and separate Dimensions/Position panels. Hidden buttons retain their settings.
- Proportional scaling preserves the widget composition when launcher padding or available space changes. The preview uses the launcher dimensions.
- Undo and Redo for configuration edits.
- Light/Dark preview toggle, with automatic preview of the colour variant being edited and manual override.
- Haptic feedback enabled by default and configurable in Actions for each interactive area. A button and its icon share one setting.
- Automatic placeholder follows the selected search engine, including custom engines, while preserving formatting and custom text.
- Existing backups remain compatible.

### Italiano

- Impostazioni organizzate in gruppi di card Material 3 Expressive, con icone, descrizioni e schermate di dettaglio.
- Layout raccoglie altezza generale, numero di pulsanti, ordine dei livelli e pannelli Dimensioni/Posizione separati. I pulsanti nascosti conservano le proprie impostazioni.
- Ridimensionamento proporzionale per mantenere la composizione del widget quando cambiano il riempimento o lo spazio disponibile nel launcher. L’anteprima usa le dimensioni comunicate dal launcher.
- Annulla e Ripeti per le modifiche alla configurazione.
- Pulsante Chiaro/Scuro per l’anteprima, sincronizzata con la variante di colore in modifica e comunque invertibile manualmente.
- Feedback aptico attivo di default e configurabile in Azioni per ogni area interattiva. Pulsante e icona condividono la stessa impostazione.
- Testo a riposo automatico legato al motore selezionato, anche personalizzato, conservando formattazione e testi inseriti manualmente.
- Compatibilità con i backup precedenti mantenuta.

## 1.12.0 — 2026-10-05

### English

- Separate **Reset default dimensions** and **Reset position (0, 0)** buttons for each element in Layout.
- Size reset restores automatic sizing, with 64 dp for the outer height and 15 sp for text. Position reset clears X/Y offsets without changing sizes.
- Movement and proportion locks retain their settings. Changes apply to the widget only after **Add** or **Save**.

### Italiano

- Pulsanti separati **Ripristina dimensioni predefinite** e **Ripristina posizione (0, 0)** per ogni elemento in Layout.
- Il ripristino delle dimensioni riattiva il dimensionamento automatico, con 64 dp per l’altezza esterna e 15 sp per il testo. Il ripristino della posizione azzera X/Y senza cambiare le dimensioni.
- I lucchetti di collegamento e delle proporzioni mantengono le proprie impostazioni. Le modifiche si applicano al widget solo dopo **Aggiungi** o **Salva**.

## 1.11.0 — 2026-10-04

### English

- **Layout** is the first configuration section and groups element position, dimensions and stacking controls.
- Independent dimensions in dp, automatic sizing and padlocks to maintain width/height proportions, enabled by default.
- X/Y coordinates and arrow buttons with an adjustable step; elements can overlap or move outside the visible widget area.
- Movement links between the search field and its logo/text, and between each button and its icon, enabled by default.
- Layer ordering with controls to move each element above or below the others.
- Widget edits and applied templates remain in the preview until **Add** or **Save** is pressed.
- Layout settings, links and layer order are included in backups; existing backups remain importable.
- A GitHub link on the app home screen opens the project repository.

### Italiano

- **Layout** è la prima sezione del configuratore e raccoglie i controlli di posizione, dimensioni e sovrapposizione degli elementi.
- Dimensioni indipendenti in dp, dimensionamento automatico e lucchetti per mantenere le proporzioni, attivi per default.
- Coordinate X/Y e frecce con passo regolabile; gli elementi possono sovrapporsi o uscire dall’area visibile del widget.
- Collegamenti tra campo di ricerca e logo/testo, e tra ogni pulsante e la sua icona, attivi per default.
- Ordine dei livelli con controlli per portare ciascun elemento sopra o sotto gli altri.
- Le modifiche del widget e i modelli applicati restano in anteprima fino alla pressione di **Aggiungi** o **Salva**.
- Layout, collegamenti e ordine dei livelli sono inclusi nei backup; i backup esistenti restano importabili.
- Un collegamento GitHub nella home dell’app apre il repository del progetto.

## 1.10.0 — 2026-09-28

### English

- Adjust the widget height from 16 to 256 dp using a slider or a numeric value, including decimals.
- Restore the default height of 64 dp with a dedicated button.
- Icons, text and touch areas scale with the widget height within the available width.
- Existing configurations and older backups keep the default height of 64 dp.

### Italiano

- Regola l’altezza del widget da 16 a 256 dp con uno slider o un valore numerico, anche decimale.
- Ripristina l’altezza predefinita di 64 dp con un pulsante dedicato.
- Icone, testo e aree di tocco si ridimensionano con l’altezza del widget entro la larghezza disponibile.
- Le configurazioni esistenti e i vecchi backup mantengono l’altezza predefinita di 64 dp.

## 1.9.0 — 2026-09-19

### English

- Widget list: now shows only saved configurations associated with IDs that are still registered, avoiding default entries for unconfigured launcher slots.
- Selection and trash button: a long press activates selection; you can add other items and use the single trash button in the top-right corner, with confirmation.
- “Follow system” theme: the widget switches between light and dark without opening the configurator.

### Italiano

- Elenco dei widget: ora mostra soltanto configurazioni salvate associate a ID ancora registrati, evitando di generare voci predefinite per gli slot non configurati del launcher.
- Selezione e cestino: un tocco prolungato attiva la selezione; puoi aggiungere altri elementi e usare l’unico cestino in alto a destra, con conferma.
- Tema “Segui sistema”: il widget passa tra chiaro e scuro senza aprire il configuratore.

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
