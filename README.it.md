# MySearchWidget

> [!NOTE]
> **L'intero progetto è stato realizzato in vibe coding.In questo repository non esiste codice scritto da un umano.**

**Un widget di ricerca Android personalizzabile, con colori Material You, gradienti, testo formattato e scorciatoie alle tue app.**

[English](README.md) · **Italiano**

Ispirato all’aspetto della barra di **Google Circle to Search**, MySearchWidget permette di creare più widget indipendenti e configurarli attraverso un’interfaccia **Material 3 Expressive**, con anteprima in tempo reale.

![Widget MySearchWidget con tema scuro, campo di ricerca e due pulsanti](assets/screenshots/widget.png)

## Funzionalità

### Ricerca e azioni

- Digita la query nel widget e inviala al browser predefinito, usando il motore di ricerca scelto.
- In alternativa, tocca il campo di ricerca per aprire la ricerca nell’app Google.
- Scegli un motore predefinito oppure aggiungi motori personalizzati.
- Assegna azioni al logo a sinistra e a **0, 1, 2 o 3 pulsanti** a destra: azioni predefinite, apertura di app o scorciatoie raggruppate per app.
- Crea più widget sulla home, ciascuno con aspetto e azioni indipendenti.

### Aspetto

- **Altezza del widget:** imposta un valore da 16 a 256 dp, anche decimale, in **Layout**. Il valore predefinito è **64 dp**, con un pulsante di ripristino. In modalità automatica, icone, testo e aree di tocco si ridimensionano insieme; la larghezza disponibile ne limita la dimensione. Per altezze maggiori, amplia verticalmente lo spazio del widget sulla Home.
- **Dimensioni e spaziature indipendenti:** regola larghezza della capsula esterna e del campo di ricerca, altezza del campo, dimensione del logo e delle icone, larghezza e altezza dello sfondo di ogni pulsante, margini laterali, spazi prima del logo e tra logo e testo e distanza prima di ogni pulsante. I controlli delle dimensioni e delle spaziature degli elementi permettono un valore manuale in dp o il dimensionamento automatico. Il ripristino automatico segue l’altezza generale del widget. Gli elementi si ridimensionano attorno a centri fissi senza spostare quelli vicini. Logo e testo seguono il campo di ricerca quando il relativo collegamento è attivo. Le dimensioni eccessive si adattano ai limiti del widget; misure grandi possono sovrapporsi.
- **Posizione degli elementi:** la scheda **Layout** nella barra inferiore raccoglie posizione, dimensioni e spaziature. Regola gli scostamenti X/Y in dp con campi numerici o frecce a passo personalizzabile; puoi sovrapporre gli elementi o spostarli fuori dall’area visibile. Il ripristino della posizione azzera gli scostamenti.
- **Ordine dei livelli:** in **Layout → Ordine dei livelli**, la cima della lista rappresenta il primo piano. Le frecce portano ogni elemento sopra o sotto gli altri; l’ordine è indipendente dai collegamenti e viene salvato nei backup.
- **Collegamenti:** i lucchetti collegano logo e testo al campo di ricerca e ogni icona al proprio pulsante, per default. Spostando il padre si spostano anche gli elementi collegati; spostando un figlio gli altri rimangono fermi. Scollegare o ricollegare conserva la posizione corrente.
- **Blocca proporzioni:** il lucchetto con linee di collegamento accanto a larghezza e altezza lega le due misure per capsula esterna, campo di ricerca e ogni pulsante. Il lucchetto è chiuso per default. Il blocco mantiene il rapporto corrente; lo sblocco permette modifiche indipendenti. La scelta viene salvata per ciascun elemento e inclusa nei backup.
- **Material You:** usa la palette dinamica del sistema.
- **Colori manuali e gradienti:** personalizza capsula esterna, campo di ricerca, logo, sfondo di ogni pulsante e relativa icona, con valori separati per tema chiaro e scuro.
- Scegli i colori con selettore grafico, slider, codici HEX/RGB e preset. I gradienti hanno due colori e direzione regolabile graficamente o in gradi.
- Regola opacità e arrotondamento degli sfondi. Per i pulsanti scegli tra cerchio/quadrato, squircle, fiore, clover, leaf, pebble, scallop e teardrop.
- Scegli icone predefinite o importa immagini. Logo e pulsanti hanno ciascuno lo switch **Icona monocromatica**, attivo di default. Disattivalo per mantenere colori e trasparenza delle immagini importate o usare i colori nativi dei simboli Google e Chrome. I simboli Material sono già a colore singolo.
- I colori originali hanno priorità su Material You solo per quell’icona. Tinta e gradienti sono disabilitati mentre i colori originali sono attivi; i valori salvati tornano disponibili riattivando lo stile monocromatico.
- Visualizza l’anteprima su fondo solido oppure sul ritaglio centrale del wallpaper Home.

### Testo

- Personalizza il **placeholder**, cioè il testo visualizzato prima della digitazione: font, dimensione, peso, grassetto, corsivo, sottolineatura, barrato, colori e gradienti.
- Applica la formattazione all’intero placeholder oppure a una selezione di testo.
- Configura separatamente lo stile della query: si applica all’intero testo digitato.
- Usa l’editor visuale oppure la modalità **BBCode**.
- Il font incluso è **Google Sans Regular 14.000**, sotto licenza [SIL OFL 1.1](LICENSE-Google-Sans.txt); puoi importare altri font TTF, TTC e OTF.

### Configurazione e backup

- Cinque sezioni: **Layout, Aspetto, Azioni, Ricerca e Backup**. Layout è la prima sezione e si apre quando inizi a configurare un widget.
- In fondo alla schermata iniziale, il collegamento con il logo GitHub apre il repository del progetto.
- Interfaccia in inglese e italiano, con cambio immediato tramite **EN / IT** in alto a destra e scelta persistente.
- Esportazione e importazione della configurazione del singolo widget, inclusi layout, ordine dei livelli, collegamenti degli spostamenti, font e immagini importati.
- Libreria di modelli riutilizzabili. I backup vengono validati prima dell’importazione; applicarli a un widget è un passaggio separato con conferma.

## Gestione dell’elenco e cestino

Tieni premuto un widget o un modello importato per attivare la selezione. Tocca altri elementi per selezionarli insieme, poi usa il **cestino in alto a destra** e conferma. **Annulla** o il tasto Indietro chiudono la selezione senza rimuovere nulla.

- **Modelli importati:** vengono eliminati dalla libreria; i widget ai quali sono stati applicati mantengono le proprie impostazioni.
- **Widget:** la voce viene nascosta dall’elenco, conservando le impostazioni del widget sulla Home. Per rimuovere il widget dalla Home usa il launcher. Aprendo di nuovo la sua configurazione dal launcher e salvandola, la voce torna visibile nell’elenco.

## Download

Scarica l’APK e i sorgenti corrispondenti dalla sezione [Releases](https://github.com/alessio89g/MySearchWidget/releases). Ogni release include anche i checksum SHA-256.

Per compilare il progetto consulta le [istruzioni di build](docs/BUILD.md). I risultati delle verifiche e i loro limiti sono nelle [note di collaudo](docs/TESTING.md).

## Requisiti e primi passi

- **Android 12 o successivo** e un launcher che supporti i widget.
- Un browser per le ricerche web; l’app Google per le funzioni che la richiedono.

1. Installa l’APK di MySearchWidget, consentendo l’installazione da quella sorgente se Android lo richiede.
2. Apri l’app e scegli **Aggiungi widget**, oppure usa il selettore widget del launcher.
3. Seleziona l’istanza da configurare, personalizzala e premi **Salva**.
4. Tocca il campo di ricerca per iniziare oppure usa il logo e i pulsanti per le azioni assegnate.

Le modifiche e i modelli applicati restano in anteprima fino alla pressione di **Aggiungi** o **Salva**. **Annulla** e il tasto Indietro permettono di scartare le modifiche senza aggiornare il widget.

La lingua iniziale è **inglese**. Tocca **EN** per passare all’italiano. Il placeholder automatico è **“Search the web” / “Cerca sul web”**; un testo personalizzato non viene tradotto automaticamente.

> L’APK è firmato con una **chiave di debug**. Per aggiornare un’installazione esistente è necessaria una firma compatibile.

## Impostazioni iniziali e controlli disabilitati

| Opzione | Default per un nuovo widget | Comportamento |
| --- | --- | --- |
| Colori Material You | Attivo | Colori e gradienti manuali sono grigi e non modificabili. Disattiva l’opzione per personalizzarli. |
| Ricerca con l’app Google al tocco del campo | Disattivo | Se attivata, la gestione dei motori di ricerca diventa grigia e non modificabile: la ricerca viene gestita dall’app Google. |

**Le impostazioni manuali e i motori salvati non vengono eliminati.** Tornano utilizzabili disattivando l’opzione corrispondente.

La ricerca Google apre la sua interfaccia di ricerca; focus e tastiera dipendono dalla versione dell’app Google. Gli stili della query del widget non modificano l’interfaccia Google.

## Colori delle icone

In **Aspetto → Logo** e **Aspetto → Pulsante N → Icona**, lo switch **Icona monocromatica** permette di scegliere tra una tinta personalizzabile e i colori originali dell’icona. La modalità monocromatica è attiva di default e la scelta è indipendente per ogni elemento.

Con i colori originali, l’immagine conserva la propria trasparenza e ignora tinta, gradiente e opacità della tinta. Le icone Material hanno normalmente un riempimento nero: la modalità monocromatica permette di adattarle al contrasto del tema.

## Scorciatoie delle app

Il selettore raggruppa le scorciatoie per app ed esclude le app senza voci disponibili. Alcune azioni possono essere visibili ma disabilitate, con una spiegazione.

Per accedere alle scorciatoie dinamiche pubblicate dalle app:

1. In **Azioni**, scegli **Abilita accesso temporaneo** e imposta temporaneamente MySearchWidget come app Home.
2. Torna alla configurazione, seleziona **Scorciatoie → app → azione** e salva.
3. Usa **Ripristina il launcher** per tornare al launcher abituale.

Il cambio di launcher è reale e temporaneo; una schermata di recupero permette di raggiungere le impostazioni Home. Le scorciatoie fissate possono essere avviate anche dopo il ripristino del launcher precedente.

La disponibilità dipende dalle scorciatoie effettivamente pubblicate e abilitate dalle altre app. Dopo una reinstallazione o il trasferimento su un altro telefono, le scorciatoie dinamiche devono essere selezionate e fissate nuovamente.

## Wallpaper e permessi

L’anteprima wallpaper è opzionale. Per leggere lo sfondo Home può essere necessario concedere accesso alle immagini e, su alcuni sistemi, l’accesso speciale a tutti i file: l’app lo propone separatamente con una spiegazione.

Se l’accesso viene negato o lo sfondo non è leggibile, per esempio con un wallpaper animato, resta il fondo solido. L’app non scansiona la galleria e non include il wallpaper nei backup.

MySearchWidget non richiede permessi **Internet, microfono o sovrapposizione ad altre app**. Le ricerche online e le azioni vocali vengono gestite dalle app esterne selezionate.

## Esempi BBCode

Placeholder con formattazione parziale:

```text
[b]Cerca[/b] [gradient=#00C8FF,#00D99B,45]sul web[/gradient]
```

Stile globale della query:

```text
[b][i]{query}[/i][/b]
```

Sono disponibili anche `[u]`, `[s]`, `[size=20]`, `[weight=500]`, `[font=default]` e `[color=#FF0000]`, con i rispettivi tag di chiusura. Per la query, `{query}` deve comparire una sola volta e avere un unico stile. Dopo la modifica, premi **Applica BBCode**. I colori manuali richiedono Material You disattivato.

## Compatibilità e limiti

- Dimensioni e posizionamento del widget dipendono anche dalla griglia e dal ridimensionamento del launcher.
- Il comportamento delle azioni Google e delle scorciatoie dipende dalle app installate e dalle loro versioni.
- Il progetto è indipendente e non è affiliato a Google. L’ispirazione grafica non implica la presenza della funzione Circle to Search.

## Licenza e crediti

Il codice originale del progetto è distribuito con la **MySearchWidget Non-Commercial Source-Available License 1.1**, una licenza personalizzata. Il testo completo e vincolante è disponibile in inglese nel file [LICENSE](LICENSE).

- **Consentiti:** uso non commerciale, studio, copia, modifica, fork e ridistribuzione non commerciale.
- **Vietati:** uso commerciale, vendita e sfruttamento del software in prodotti o servizi commerciali, inclusa la monetizzazione tramite pubblicità.
- **Versioni non modificate:** non occorre consegnare i sorgenti; è sufficiente un collegamento al [repository originale](https://github.com/alessio89g/MySearchWidget) nei crediti.
- **Versioni modificate:** la ridistribuzione deve includere i sorgenti completi corrispondenti, comprese le modifiche, le istruzioni e gli script di compilazione. Il solo link a un repository non basta.
- **Crediti obbligatori:** ogni ridistribuzione, modificata o meno, deve citare MySearchWidget e collegare il repository originale nei crediti. Vedi [CREDITS.md](CREDITS.md).
- **Stessi termini:** le versioni modificate devono mantenere questa licenza, gli avvisi di attribuzione e una descrizione delle modifiche. Le modifiche mantenute private non devono essere pubblicate.

Il progetto è **source available per uso non commerciale**, non open source secondo la [definizione OSI](https://opensource.org/osd), che non consente restrizioni all’uso commerciale.

Le risorse di terzi sono escluse da questa licenza e mantengono i rispettivi termini: le icone Material sono distribuite sotto [Apache 2.0](LICENSE-Material-Icons.txt); il font Google Sans è distribuito sotto [SIL OFL 1.1](LICENSE-Google-Sans.txt). I marchi e le altre risorse di terzi non vengono rilicenziati da questo progetto.

Il logo GitHub proviene da Octicons ed è distribuito con [licenza MIT](LICENSE-Octicons.txt).
