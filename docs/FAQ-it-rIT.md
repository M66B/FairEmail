<a name="top"></a>

# Supporto di FairEmail

Se hai una domanda, sei pregato di controllare prima le seguenti domande frequenti. [In fondo](#user-content-get-support) puoi scoprire come porre altre domande, richiedere funzionalità e segnalare i bug.

Se hai una domanda, sei pregato di controllare prima le seguenti domande frequenti (FAQ). [In fondo](#user-content-get-support) puoi scoprire come porre altre domande, richiedere funzionalità e segnalare i bug.

## Indice

* [Autorizzare profili](#user-content-authorizing-accounts)
* [Come...?](#user-content-howto)
* [Problemi noti](#user-content-known-problems)
* [Funzionalità pianificate](#user-content-planned-features)
* [Funzionalità frequentemente richieste](#user-content-frequently-requested-features)
* [Domande Frequenti](#user-content-frequently-asked-questions)
* [Richiedi supporto](#user-content-get-support)

<h2><a name="authorizing-accounts"></a>Autorizzare profili</h2>

In gran parte dei casi, la procedura guidata di configurazione rapida potrà identificare automaticamente la configurazione corretta.

Se la procedura guidata di configurazione rapida dovesse fallire, dovrai configurare manualmente un profilo (per ricevere le email) e un'identità (per inviarle). Per questo ti serviranno gli indirizzi del server IMAP e SMTP e i numeri di porta, se dovrebbe esser usato SSL/TLS o STARTTLS e il tuo nome utente (principalmente, ma non sempre, il tuo indirizzo email) e la tua password.

Cercando l'*IMAP* e il nome del provider è più che sufficiente trovare la giusta documentazione.

In alcuni casi, dovrai abilitare l'accesso esterno al tuo profilo e/o usare una password (dell'app) speciale, ad esempio quando è abilitata l'autenticazione a due fattori.

Per autorizzare:

* Gmail / G suite, vedi la [domanda 6](#user-content-faq6)
* Outlook / Live / Hotmail, vedi la [domanda 14](#user-content-faq14)
* Office 365, vedi la [domanda 14](#user-content-faq156)
* Microsoft Exchange, vedi la [domanda 8](#user-content-faq8)
* Yahoo, AOL e Sky, vedi la [domanda 88](#user-content-faq88)
* Apple iCloud, vedi la [domanda 148](#user-content-faq148)
* Free.fr, vedi la [domanda 157](#user-content-faq157)

Sei pregato di vedere [qui](#user-content-faq22) i messaggi di errore e le soluzioni comuni.

Domande correlate:

* [OAuth è supportato?](#user-content-faq111)
* [Perché ActiveSync non è supportato?](#user-content-faq133)

<a name="howto">

## Come...?

* Cambiare il nome del profilo: Impostazioni, tocca Configurazione manuale, tocca Profili, tocca il profilo
* Cambiare la destinazione di scorrimento a sinistra/destra: Impostazioni, tocca la pagina del Comportamento, Imposta azioni di scorrimento
* Cambiare la password: Impostazioni, tocca Configurazione manuale, tocca Profili, tocca il profilo, cambia la password
* Impostare una firma: Impostazioni, tocca Configurazione manuale, tocca Identità, tocca l'identità, Modifica firma.
* Aggiungere indirizzi CC e CCN: tocca l'icona della persona alla fine dell'oggetto
* Andare al messaggio successivo/precedente all'archiviazione/eliminazione: nelle impostazioni di comportamento disabilita *Chiudi automaticamente le conversazioni* e seleziona *Vai alla conversazione successiva/precedente* per *Alla chiusura di una conversazione*
* Aggiungere una cartella alla casella unificata: tieni premuta la cartella nell'elenco delle cartelle e spunta *Mostra nella casella unificata*
* Aggiungere una cartella al menu di navigazione: tieni premuta la cartella nell'elenco delle cartelle e spunta *Mostra nel menu di navigazione*
* Caricare altri messaggi: tieni premuta una cartella nell'elenco delle cartelle, seleziona *Recupera altri messaggi*
* Eliminare un messaggio, evitando il cestino: tieni premuta l'icona del cestino
* Eliminare un profilo/un'identità: Impostazioni, tocca Configurazione manuale, tocca Profili/Identità, tocca il profilo/l'identità, icona del cestino in alto a destra
* Eliminare una cartella: tieni premuta la cartella nell'elenco delle cartelle, Modifica proprietà, icona del cestino in alto a destra
* Annullare l'invio: Posta in uscita, fai scorrere il messaggio nell'elenco a sinistra o destra
* Conservare i messaggi inviati in posta in arrivo: sei pregato di [vedere questa FAQ](#user-content-faq142)
* Cambiare le cartelle di sistema: Impostazioni, tocca Configurazione manuale, tocca Profili, tocca il profilo, in basso
* Impostazioni di esportazione/importazione: Impostazioni, menu di navigazione (lato sinistro)

<h2><a name="known-problems"></a>Problemi noti</h2>

* ~~Un [bug in Android 5.1 e 6](https://issuetracker.google.com/issues/37054851) causa all'app di mostrare un formato orario errato. Attivare/disattivare l'impostazione di Android *Utilizza formato a 24 ore* potrebbe risolvere temporaneamente il problema. È stata aggiunta una soluzione.~~
* ~~Un [bug di Google Drive](https://issuetracker.google.com/issues/126362828) causa che i file esportati su Google Drive siano vuoti. Google ha risolto questo problema.~~
* ~~Un [bug di AndroidX](https://issuetracker.google.com/issues/78495471) causa l'arresto anomalo occasionale di FairEmail tenendo premuto o scorrendo. Google ha corretto questo problema.~~
* ~~Un [bug in AndroidX ROOM](https://issuetracker.google.com/issues/138441698) causa talvolta un arresto anomalo con "*... Eccezione calcolando i dati live del database ... Impossibile leggere la riga ...*". È stata aggiunta una soluzione.~~
* Un [bug di Android](https://issuetracker.google.com/issues/119872129) causa l'arresto anomalo di FairEmail con "*... Notifica errata pubblicata ...*" su alcuni dispositivi una volta aggiornato FairEmail e toccando su una notifica.
* Un [bug di Android](https://issuetracker.google.com/issues/62427912) provoca talvolta un arresto anomalo con "*... ActivityRecord non trovato per ...*" dopo l'aggiornamento di FairEmail. La reinstallazione ([sorgente](https://stackoverflow.com/questions/46309428/android-activitythread-reportsizeconfigurations-causes-app-to-freeze-with-black)) potrebbe risolvere il problema.
* Un [bug di Android](https://issuetracker.google.com/issues/37018931) causa talvolta un arresto anomalo con *... InputChannel non è inizializzato ...* su alcuni dispositivi.
* ~~Un [bug in LineageOS](https://review.lineageos.org/c/LineageOS/android_frameworks_base/+/265273) causa talvolta un arresto anomalo con *... java.lang.ArrayIndexOutOfBoundsException: lenght=...; index=... ...*.~~
* Un bug di Nova Launcher su Android 5.x causa l'arresto anomalo di FairEmail con un *java.lang.StackOverflowError* quando Nova Launcher ha accesso al servizio di accessibilità.
* ~~Il selettore delle cartelle non mostra talvolta le cartelle per motivi ancora sconosciuti. Sembra essere risolto.~~
* ~~Un [bug di AndroidX](https://issuetracker.google.com/issues/64729576) rende difficile prendere il cursore rapido. È stata aggiunta una soluzione.~~
* ~~La crittografia con YubiKey risulta in un ciclo infinito. Questo sembra esser causato da un [bug in OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2507).~~
* Lo scorrimento a una posizione collegata internamente nei messaggi originali non funziona. Questo non è risolvibile perché la vista originale del messaggio è contenuta in una vista di scorrimento.
* Un'anteprima del testo di un messaggio non appare (sempre) sugli orologi Samsung perché [setLocalOnly](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder.html#setLocalOnly(boolean)) sembra esser ignorato. I testi di anteprima del messaggio sono noti per esser visualizzati correttamente sugli indossabili Pebble 2, Fitbit Charge 3, Mi band 3 e Xiaomi Amazfit BIP. Vedi anche [questa FAQ](#user-content-faq126).
* Un [errore di Android 6.0](https://issuetracker.google.com/issues/37068143) causa un arresto anomalo con *... Scostamento non valido: ... L'intervallo valido è ...* quando il testo è selezionato, toccando al di fuori di esso. Questo bug è stato risolto in Android 6.0.1.
* I collegamenti interni (ancoraggi) non funzioneranno perché i messaggi originali sono mostrati in una WebView incorporata in una vista di scorrimento (l'elenco delle conversazioni). Questa è una limitazione irrisolvibile o aggirabile di Android.
* Il rilevamento della lingua [non funziona più](https://issuetracker.google.com/issues/173337263) sui dispositivi Pixel con (aggiornati a?) Android 11
* Un [bug di OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2688) causa firme PGP non valide usando un token hardware.

<h2><a name="planned-features"></a>Funzionalità pianificate</h2>

* ~~Sincronizzazione su richiesta (manuale)~~
* ~~Crittografia semiautomatica~~
* ~~Copia messaggio~~
* ~~Stelle colorate~~
* ~~Impostazioni di notifica per cartella~~
* ~~Selezione immagini locali per le firme~~ (non sarà aggiunto perché richiede la gestione dei file immagine e poiché le immagini non sono comunque mostrate di default in gran parte dei client email)
* ~~Mostra messaggi abbinati a una regola~~
* ~~[ManageSieve](https://tools.ietf.org/html/rfc5804)~~ (non esiste alcuna libreria Java mantenuta con una licenza adatta e senza dipendenze e, oltre ciò, FairEmail ha le proprie regole del filtro)
* ~~Cerca messaggi con/senza allegati~~ (ciò non è implementabile perché IMAP non supporta la ricerca degli allegati)
* ~~Cerca una cartella~~ (filtrare un elenco gerarchico di cartelle è problematico)
* ~~Suggerimenti di ricerca~~
* ~~[Messaggio di Configurazione di Autocrypt](https://autocrypt.org/autocrypt-spec-1.0.0.pdf) (sezione 4.4)~~ (Per me non è una buona idea permettere a un client email di gestire le chiavi crittografiche sensibili per un caso d'uso eccezionale, mentre OpenKeychain può anche esportarle)
* ~~Cartelle unificate generiche~~
* ~~Pianificazioni delle notifiche dei nuovi messaggi per profilo~~ (implementato aggiungendo una condizione temporale alle regole così che i messaggi siano posticipabili durante i periodi selezionati)
* ~~Copiare profili e identità~~
* ~~Zoom pizzicato~~ (impossibile affidabilmente in una lista a scorrimento; la vista completa dei messaggi è invece zoomabile)
* ~~Vista più compatta delle cartelle~~
* ~~Comporre elenchi e tabelle~~ (richiede un editor di rich text, vedi [questa FAQ](#user-content-faq99))
* ~~Zoom pizzicato della dimensione del testo~~
* ~~Mostra GIF~~
* ~~Temi~~ (un tema grigio chiaro e scuro è stato aggiunto perché è ciò che gran parte delle persone sembrano volere)
* ~~Condizione per ogni orario del giorno~~ (ogni giorno non si adegua davvero alla condizione da/a data/ora)
* ~~Invia come allegato~~
* ~~Widget per il profilo selezionato~~
* ~~Ricorda di allegare i file~~
* ~~Seleziona i domini per cui mostrare le immagini~~ (sarebbe troppo complicato da usare)
* ~~Visualizzazione unificata dei messaggi stellati~~ (esiste già una ricerca speciale per questo)
* ~~Azione di spostamento della notifica~~
* ~~Supporto S/MIME~~
* ~~Cerca impostazioni~~

Ogni cosa in questo elenco è in ordine casuale e *potrebbe* esser aggiunta nel futuro prossimo.

<h2><a name="frequently-requested-features"></a>Funzionalità frequentemente richieste</h2>

Il design si basa su molte discussioni e, se lo desideri, puoi discuterne anche tu [in questo forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168). Lo scopo del design è di essere minimalista (senza menu, pulsanti etc. inutili) e non distraente (niente colori stravaganti, animazioni, etc.). Tutte le cose mostrate dovrebbero esser utili in un modo o nell'altro e attentamente posizionate per un facile uso. Caratteri, dimensioni, colori, etc. dovrebbero essere di design materiale quando possibile.

<h2><a name="frequently-asked-questions"></a>Domande Frequenti</h2>

* [(1) Quali autorizzazioni sono necessarie e perché?](#user-content-faq1)
* [(2) Perché viene mostrata una notifica permanente?](#user-content-faq2)
* [(3) Quali sono le operazioni e perché sono in sospeso?](#user-content-faq3)
* [(4) Come posso usare un certificato di sicurezza non valido / una password vuota / una connessione in testo semplice?](#user-content-faq4)
* [(5) Come posso personalizzare la visualizzazione dei messaggi?](#user-content-faq5)
* [(6) Come posso accedere a Gmail / G suite?](#user-content-faq6)
* [(7) Perché i messaggi inviati non appaiono (direttamente) nella cartella inviati?](#user-content-faq7)
* [(8) Posso usare un profilo di Microsoft Exchange?](#user-content-faq8)
* [(9) Cosa sono le identità / come aggiungo un alias?](#user-content-faq9)
* [~~(11) Perché POP non è supportato?~~](#user-content-faq11)
* [~~(10) Cosa significa 'UIDPLUS non supportato'?~~](#user-content-faq10)
* [(12) Come funziona la crittografia/decrittografia?](#user-content-faq12)
* [(13) Come funziona la ricerca su dispositivo/server?](#user-content-faq13)
* [(14) Come posso configurare un profilo di Outlook / Live / Hotmail?](#user-content-faq14)
* [(15) Perché il testo del messaggio continua a caricare?](#user-content-faq15)
* [(16) Perché i messaggi non vengono sincronizzati?](#user-content-faq16)
* [~~(17) Perché la sincronizzazione manuale non funziona?~~](#user-content-faq17)
* [(18) Perché l'anteprima del messaggio non è sempre mostrata?](#user-content-faq18)
* [(19) Perché le funzionalità pro sono così costose?](#user-content-faq19)
* [(20) Posso ottenere un rimborso?](#user-content-faq20)
* [(21) Come abilito il LED di notifica?](#user-content-faq21)
* [(22) Cosa significa errore del profilo/della cartella... ?](#user-content-faq22)
* [(23) Perché ricevo l'avviso...? ?](#user-content-faq23)
* [(24) Cos'è sfoglia i messaggi sul server?](#user-content-faq24)
* [(25) Perché non posso selezionare/aprire/salvare un'immagine, un allegato o un file?](#user-content-faq25)
* [(26) Posso aiutare a tradurre FairEmail nella mia lingua?](#user-content-faq26)
* [(27) Come posso distinguere tra immagini incorporate ed esterne?](#user-content-faq27)
* [(28) Come posso gestire le notifiche della barra di stato?](#user-content-faq28)
* [(29) Come posso ricevere le notifiche dei nuovi messaggi per le altre cartelle?](#user-content-faq29)
* [(30) Come posso usare le impostazioni rapide fornite?](#user-content-faq30)
* [(31) Come posso usare le scorciatoie fornite?](#user-content-faq31)
* [(32) Come posso verificare che leggere le email sia davvero sicuro?](#user-content-faq32)
* [(33) Perché gli indirizzi modificati del mittente non funzionano?](#user-content-faq33)
* [(34) Come sono abbinate le identità?](#user-content-faq34)
* [(35) Perché dovrei esser attento a visualizzare immagini, allegati, il messaggio originale e ad aprire i collegamenti?](#user-content-faq35)
* [(36) Come sono crittografati i file delle impostazioni?](#user-content-faq36)
* [(37) Come sono memorizzate le password?](#user-content-faq37)
* [(39) Come posso ridurre l'uso della batteria di FairEmail?](#user-content-faq39)
* [(40) Come posso ridurre l'uso dei dati di FairEmail?](#user-content-faq40)
* [(41) Come posso correggere l'errore 'Handshake fallito' ?](#user-content-faq41)
* [(42) Puoi aggiungere un nuovo fornitore all'elenco dei fornitori?](#user-content-faq42)
* [(43) Puoi mostrare l'originale di...?](#user-content-faq43)
* [(44) Puoi mostrare foto / identicon nella cartella inviati?](#user-content-faq44)
* [(45) Come posso risolvere 'Questa chiave non è disponibile. Per usarla, devi importarla come una delle tue!' ?](#user-content-faq45)
* [(46) Perché l'elenco dei messaggi continua a ricaricarsi?](#user-content-faq46)
* [(47) Come risolvo l'errore 'Nessun profilo principale o cartella delle bozze' ?](#user-content-faq47)
* [~~(48) Come risolvo l'errore 'Nessun profilo principale o cartella d'archivio' ?~~](#user-content-faq48)
* [(49) Come risolvo 'Un'app obsoleta ha inviato un percorso del file invece di un flusso di file'?](#user-content-faq49)
* [(50) Puoi aggiungere un'opzione per sincronizzare tutti i messaggi?](#user-content-faq50)
* [(51) Come sono ordinate le cartelle?](#user-content-faq51)
* [(52) Perché ci vuole del tempo per riconnettersi a un profilo?](#user-content-faq52)
* [(53) Puoi attaccare la barra d'azione del messaggio in cima/in fondo?](#user-content-faq53)
* [~~(54) Come uso un prefisso dello spazio del nome?~~](#user-content-faq54)
* [(55) Come posso segnare tutti i messaggi come letti / spostarli o eliminarli?](#user-content-faq55)
* [(56) Puoi aggiungere il supporto per JMAP?](#user-content-faq56)
* [(57) Posso usare HTML nelle firme?](#user-content-faq57)
* [(58) Cosa significa l'icona di un'email aperta/chiusa?](#user-content-faq58)
* [(59) I messaggi originali sono apribili nel browser?](#user-content-faq59)
* [(60) Sapevi...?](#user-content-faq60)
* [(61) Perché alcuni messaggi sono mostrati oscurati?](#user-content-faq61)
* [(62) Che metodi di autenticazione sono supportati?](#user-content-faq62)
* [(63) Come sono ridimensionate le immagini per la visualizzazione sugli schermi?](#user-content-faq63)
* [~~(64) Puoi aggiungere azioni personalizzate per scorrere a sinistra/destra?~~](#user-content-faq64)
* [(65) Perché alcuni allegati sono mostrati oscurati?](#user-content-faq65)
* [(66) FairEmail è disponibile nella Libreria di Famiglia di Google Play?](#user-content-faq66)
* [(67) Come posso posticipare le conversazioni?](#user-content-faq67)
* [~~(68) Perché Adobe Acrobat reader non apre gli allegati PDF / le app di Microsoft non aprono i documenti allegati?~~](#user-content-faq68)
* [(69) Puoi aggiungere lo scorrimento automatico in su ai nuovi messaggi?](#user-content-faq69)
* [(70) Quando saranno auto-espansi i messaggi?](#user-content-faq70)
* [(71) Come uso le regole del filtro?](#user-content-faq71)
* [(72) Cosa sono i profili/le identità principali?](#user-content-faq72)
* [(73) Spostare messaggi tra profili è sicuro/efficiente?](#user-content-faq73)
* [(74) Perché vedo i messaggi duplicati?](#user-content-faq74)
* [(75) Puoi fare una versione iOS, Windows, Linux, etc.?](#user-content-faq75)
* [(76) Cosa fa 'Elimina messaggi locali'?](#user-content-faq76)
* [(77) Perché a volte i messaggi sono mostrati con un lieve ritardo?](#user-content-faq77)
* [(78) Come uso le pianificazioni?](#user-content-faq78)
* [(79) Come uso la sincronizzazione su richiesta (manuale)?](#user-content-faq79)
* [~~(80) Come risolvo l'errore 'Impossibile caricare BODYSTRUCTURE'?~~](#user-content-faq80)
* [~~(81) Puoi rendere scuro lo sfondo del messaggio originale nel tema scuro?~~](#user-content-faq81)
* [(82) Cos'è un'immagine di monitoraggio?](#user-content-faq82)
* [(84) A cosa servono i contatti locali?](#user-content-faq84)
* [(85) Perché un'identità non è disponibile?](#user-content-faq85)
* [~~(86) Cosa sono le 'funzionalità extra della privacy''?~~](#user-content-faq86)
* [(87) Cosa significa 'credenziali non valide'?](#user-content-faq87)
* [(88) Come posso usare un profilo di Yahoo, AOL o Sky?](#user-content-faq88)
* [(89) Come posso inviare messaggi di solo testo semplice?](#user-content-faq89)
* [(90) Perché alcuni testi sono collegati senza essere link?](#user-content-faq90)
* [~~(91) Puoi aggiungere la sincronizzazione periodica per risparmiare batteria?~~](#user-content-faq91)
* [(92) Puoi aggiungere il filtraggio dello spam, la verifica della firma DKIM e l'autorizzazione SPF?](#user-content-faq92)
* [(93) Puoi consentire l'installazione/archiviazione dei dati su multimedia di archiviazione esterna (scheda sd)?](#user-content-faq93)
* [(94) Cosa significa la striscia rossa/arancione alla fine dell'intestazione?](#user-content-faq94)
* [(95) Perché non sono mostrate tutte le app selezionando un allegato o un'immagine?](#user-content-faq95)
* [(96) Dove posso trovare le impostazioni di IMAP e SMTP?](#user-content-faq96)
* [(97) Cos'è la 'pulizia'?](#user-content-faq97)
* [(98) Perché posso ancora selezionare i contatti dopo aver revocato i permessi della rubrica?](#user-content-faq98)
* [(99) Puoi aggiungere un editor del rich text o di markdown?](#user-content-faq99)
* [(100) Come posso sincronizzare le categorie di Gmail?](#user-content-faq100)
* [(101) Cosa significa il puntino blu/arancione in fondo alle conversazioni?](#user-content-faq101)
* [(102) Come posso abilitare la rotazione automatica delle immagini?](#user-content-faq102)
* [(103) Come registro l'audio?](#user-content-faq158)
* [(104) Cosa devo sapere sulla segnalazione degli errori?](#user-content-faq104)
* [(105) Come funziona l'opzione roam-like-at-home?](#user-content-faq105)
* [(106) Quali launcher possono mostrare un distintivo di conteggio con il numero di messaggi non letti?](#user-content-faq106)
* [(107) Come uso le stelle colorate?](#user-content-faq107)
* [~~(108) Puoi aggiungere l'eliminazione permanente dei messaggi da ogni cartella?~~](#user-content-faq108)
* [~~(109) Perché 'seleziona profilo' è disponibile solo nelle versioni ufficiali?~~](#user-content-faq109)
* [(110) Perché (alcuni) messaggi sono vuoti e/o gli allegati sono corrotti?](#user-content-faq110)
* [(111) OAuth è supportato?](#user-content-faq111)
* [(112) Che provider email consigli?](#user-content-faq112)
* [(113) Come funziona l'autenticazione biometrica?](#user-content-faq113)
* [(114) Puoi aggiungere un'importazione per le impostazioni di altre app email?](#user-content-faq114)
* [(115) Puoi aggiungere i chip dell'indirizzo email?](#user-content-faq115)
* [~~(116) Come posso mostrare le immagini nei messaggi dai mittenti fidati di default?~~](#user-content-faq116)
* [(117) Puoi aiutarmi a ripristinare il mio acquisto?](#user-content-faq117)
* [(118) Cosa fa esattamente 'Rimuovi parametri di monitoraggio'?](#user-content-faq118)
* [~~(119) Puoi aggiungere i colori al widget della casella di posta in arrivo unificata?~~](#user-content-faq119)
* [(120) Perché le notifiche di nuovo messaggio non sono rimosse all'apertura dell'app?](#user-content-faq120)
* [(121) Come sono raggruppati i messaggi in una conversazione?](#user-content-faq121)
* [~~(122) Perché il nome/indirizzo email del destinatario è mostrato con un colore di avviso?~~](#user-content-faq122)
* [(123) Cosa succederà quando FairEmail non potrà connettersi a un server email?](#user-content-faq123)
* [(124) Perché ottengo 'Messaggio troppo grande o troppo complesso da mostrare'?](#user-content-faq124)
* [(125) Quali sono le correnti funzionalità sperimentali?](#user-content-faq125)
* [(126) Le anteprime dei messaggi sono inviabili al mio indossabile?](#user-content-faq126)
* [(127) Come posso correggere 'Argomenti HELO sintatticamente non validi'?](#user-content-faq127)
* [(128) Come posso ripristinare le domande fatte, ad esempio per mostrare le immagini?](#user-content-faq128)
* [(129) ProtonMail e Tutanota sono supportati?](#user-content-faq129)
* [(130) Cosa significa il messaggio di errore...?](#user-content-faq130)
* [(131) Puoi modificare la direzione per lo scorrimento al messaggio precedente/successivo?](#user-content-faq131)
* [(132) Perché le notifiche dei nuovi messaggi sono silenziate?](#user-content-faq132)
* [(133) Perché ActiveSync non è supportato?](#user-content-faq133)
* [(134) Puoi aggiungere l'eliminazione dei messaggi locali?](#user-content-faq134)
* [(135) Perché i messaggi cestinati e di bozza sono mostrati nelle conversazioni?](#user-content-faq135)
* [(135) Come posso eliminare un profilo/un'identità/una cartella?](#user-content-faq136)
* [(137) Come posso ripristinare 'Non chiedere più'?](#user-content-faq137)
* [(138) Puoi aggiungere la gestione del calendario/rubrica/attività/note?](#user-content-faq138)
* [(139) Come correggo 'Utente autenticato ma non connesso'?](#user-content-faq139)
* [(140) Perché il testo del massaggio contiene caratteri strani?](#user-content-faq140)
* [(141) Come posso correggere 'Una cartella delle bozze è necessaria per inviare i messaggi'?](#user-content-faq141)
* [(142) Come posso conservare i messaggi inviati nella posta in arrivo?](#user-content-faq142)
* [~~(143) Puoi aggiungere una cartella del cestino per i profili POP3?~~](#user-content-faq143)
* [(144) Come posso registrare le note vocali?](#user-content-faq144)
* [(145) Come posso impostare un suono di notifica per un profilo, una cartella o un mittente?](#user-content-faq145)
* [(146) Come posso correggere gli orari scorretti dei messaggi?](#user-content-faq146)
* [(147) Cosa dovrei sapere sulle versioni di terze parti?](#user-content-faq147)
* [(148) Come posso usare un profilo di Apple iCloud?](#user-content-faq148)
* [(149) Come funziona il widget di conteggio dei messaggi non letti?](#user-content-faq149)
* [(150) Puoi aggiungere l'annullamento degli inviti del calendario?](#user-content-faq150)
* [(151) Puoi aggiungere il backup/ripristino dei messaggi?](#user-content-faq151)
* [(152) Come posso inserire un gruppo di contatto?](#user-content-faq152)
* [(153) Perché l'eliminazione permanente del messaggio di Gmail non funziona?](#user-content-faq153)
* [~~(154) Puoi aggiungere i favicon come foto di contatto?~~](#user-content-faq154)
* [(155) Cos'è un file winmail.dat?](#user-content-faq155)
* [(156) Come posso configurare un profilo di Office 365?](#user-content-faq156)
* [(157) Come posso configurare un profilo di Free.fr?](#user-content-faq157)
* [(158) Che fotocamera / registratore audio consigli?](#user-content-faq158)
* [(159) Cosa sono gli elenchi di protezione del tracciatore di Disconnect?](#user-content-faq159)
* [(160) Puoi aggiungere l'eliminazione permanente dei messaggi senza conferma?](#user-content-faq160)
* [(161) Puoi aggiungere un'impostazione per modificare i colori primari e secondari?](#user-content-faq161)
* [(162) IMAP NOTIFY è supportato?](#user-content-faq162)
* [(163) Cos'è la classificazione dei messaggi?](#user-content-faq163)
* [(164) Puoi aggiungere i temi personalizzabili?](#user-content-faq164)
* [(165) Android Auto è supportato?](#user-content-faq165)
* [(166) Posso posticipare un messaggio su più dispositivi?](#user-content-faq166)

[Ho un'altra domanda.](#user-content-support)

<a name="faq1"></a>
**(1) Quali autorizzazioni sono necessarie e perché?**

Sono necessarie le seguenti autorizzazioni di Android:

* *avere accesso completo alla rete* (INTERNET): per inviare e ricevere le email
* *visualizzare le connessioni di rete* (ACCESS_NETWORK_STATE): per monitorare le modifiche alla connettività di internet
* *eseguire all'avvio* (RECEIVE_BOOT_COMPLETED): per avviare il monitoraggio all'avvio del dispositivo
* *servizio in primo piano* (FOREGROUND_SERVICE): per eseguire un servizio in primo piano su Android 9 Pie e successive, vedi anche la prossima domanda
* *prevenire che il dispositivo vada in riposo* (WAKE_LOCK): per mantenere attivo il dispositivo sincronizzando i messaggi
* *fatturazione integrata* (BILLING): per consentire gli acquisti in app
* *pianificare la sveglia esatta* (SCHEDULE_EXACT_ALARM): per usare la pianificazione della sveglia esatta (Android 12 e successive)
* Opzionale: *leggere la tua rubrica* (READ_CONTACTS): per auto completare gli indirizzi, mostrare le foto di contatto e [selezionare i contatti](https://developer.android.com/guide/components/intents-common#PickContactDat)
* Opzionale: *leggere i contenuti della tua scheda SD* (READ_EXTERNAL_STORAGE): per accettare file da altre app obsolete, vedi anche [questa FAQ](#user-content-faq49)
* Opzionale: *usare l'hardware delle impronte digitali* (USE_FINGERPRINT) e usare l'*hardware biometrico* (USE_BIOMETRIC): per usare l'autenticazione biometrica
* Opzionale: *trovare profili sul dispositivo* (GET_ACCOUNTS): per selezionare un profilo usando la configurazione rapida di Gmail
* Android 5.1 Lollipop e precedenti: *usare i profili sul dispositivo* (USE_CREDENTIALS): per selezionare un profilo usando la configurazione rapida di Gmail (non richiesto sulle versioni successive di Android)
* Android 5.1 Lollipop e precedenti: *Leggere il profilo* (READ_PROFILE): per leggere il tuo nome usando la configurazione rapida di Gmail (non richiesto sulle versioni successive di Android)

Le [Autorizzazioni opzionali](https://developer.android.com/training/permissions/requesting) sono supportate solo su Android 6 Marshmallow e successive. Sulle versioni precedenti di Android ti sarà chiesto di concedere le autorizzazioni opzionali all'installazione di FairEmail.

Le seguenti autorizzazioni sono necessarie per mostrare il conteggio dei messaggi non letti come un dispositivo (vedi anche [questa FAQ](#user-content-faq106)):

* *com.sec.android.provider.badge.permission.READ*
* *com.sec.android.provider.badge.permission.WRITE*
* *com.htc.launcher.permission.READ_SETTINGS*
* *com.htc.launcher.permission.UPDATE_SHORTCUT*
* *com.sonyericsson.home.permission.BROADCAST_BADGE*
* *com.sonymobile.home.permission.PROVIDER_INSERT_BADGE*
* *com.anddoes.launcher.permission.UPDATE_COUNT*
* *com.majeur.launcher.permission.UPDATE_BADGE*
* *com.huawei.android.launcher.permission.CHANGE_BADGE*
* *com.huawei.android.launcher.permission.READ_SETTINGS*
* *com.huawei.android.launcher.permission.WRITE_SETTINGS*
* *android.permission.READ_APP_BADGE*
* *com.oppo.launcher.permission.READ_SETTINGS*
* *com.oppo.launcher.permission.WRITE_SETTINGS*
* *me.everything.badger.permission.BADGE_COUNT_READ*
* *me.everything.badger.permission.BADGE_COUNT_WRITE*

FairEmail manterrà un elenco di indirizzi da cui ricevi e invii i messaggi e userà questo elenco per suggerire i contatti quando non è concesso alcun permesso dei contatti a FairEmail. Questo significa che puoi usare FairEmail senza il provider dei contatti di Android (rubrica). Si noti che puoi ancora selezionare i contatti senza concedere le autorizzazioni dei contatti a FairEmail, il solo suggerimento di essi non funzionerà senza le autorizzazioni.

<br />

<a name="faq2"></a>
**(2) Perché viene mostrata una notifica permanente?**

Una notifica sulla barra di stato permanente a bassa priorità con il numero di profili monitorati e di operazioni in sospeso (vedi la prossima domanda) è mostrata per prevenire che Android termini il servizio che si occupa della continua ricezione di email. Questo era [già necessario](https://developer.android.com/reference/android/app/Service.html#startForeground(int,%20android.app.Notification)), ma con l'introduzione della [modalità standby](https://developer.android.com/training/monitoring-device-state/doze-standby) in Android 6 Marshmallow ciò è diventato più necessario che mai. La modalità standby interromperà tutte le app quando lo schermo è spento per un po', a meno che l'app non avvii un servizio in primo piano, che richiede la visualizzazione di una notifica sulla barra di stato.

Gran parte delle altre app email, se non tutte, non mostrano una notifica, con l'"effetto collaterale" che i nuovi messaggi spesso non sono segnalati o sono segnalati in ritardo e che i messaggi non sono inviati o sono inviati in ritardo.

Android mostra per prime le icone delle notifiche ad alta priorità nella barra di stato e nasconde le icone delle notifiche di FairEmail se non c'è più spazio per mostrarne altre. In pratica ciò significa che la notifica della barra di stato non richiede spazio nella barra di stato, a meno che non ci sia spazio disponibile.

La notifica della barra di stato è disabilitabile tramite le impostazioni di notifica di FairEmail:

* Android 8 Oreo e successive: tocca il pulsante *Canale di ricezione* e disabilitalo tramite le impostazioni di Android (questo non disabiliterà le notifiche dei nuovi messaggi)
* Android 7 Nougat e precedenti: abilita *Usa il servizio in background per sincronizzare i messaggi*, ma assicurati di leggere l'osservazione sotto l'impostazione

Puoi passare a sincronizzare periodicamente i messaggi nelle impostazioni di ricezione per rimuovere la notifica, ma sappi che ciò potrebbe usare più energia della batteria. Vedi [qui](#user-content-faq39) per ulteriori dettagli sull'uso della batteria.

Android 8 Oreo potrebbe anche mostrare una notifica della barra di stato con il testo *App in esecuzione in background*. Sei pregato di vedere [qui](https://www.reddit.com/r/Android/comments/7vw7l4/psa_turn_off_background_apps_notification/) come disabilitare questa notifica.

Alcune persone hanno suggerito di usare [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/) (FCM) invece di un servizio Android con una notifica della barra di stato, ma questo richiederebbe ai provider email di inviare messaggi FCM o un server centrale dove siano raccolti tutti i messaggi inviati da FCM. La prima non succederà e l'ultima avrebbe significative implicazioni sulla privacy.

Se sei arrivato qui cliccando su una notifica, dovresti sapere che il prossimo click aprirà la casella di posta unificata.

<br />

<a name="faq3"></a>
**(3) Quali sono le operazioni e perché sono in sospeso?**

La notifica della barra di stato a bassa priorità mostra il numero di operazioni in sospeso, che possono essere:

* *add*: aggiungi messaggio alla cartella remota
* *move*: sposta messaggio in un'altra cartella remota
* *copy*: copia messaggio in un'altra cartella remota
* *fetch*: recupera il messaggio modificato (push)
* *delete*: elimina il messaggio dalla cartella remota
* *seen*: contrassegna il messaggio come letto/non letto nella cartella remota
* *answered*: contrassegna il messaggio come risposto nella cartella remota
* *flag*: aggiunge/rimuove la stella nella cartella remota
* *keyword*: aggiunge/rimuove il flag IMAP nella cartella remota
* *label*: imposta/ripristina l'etichetta di Gmail nella cartella remota
* *headers*: scarica le intestazioni dei messaggi
* *raw*: scarica il messaggio non elaborato
* *body*: scarica il testo del messaggio
* *attachment*: scarica l'allegato
* *sync*: sincronizza i messaggi locali e remoti
* *subscribe*: iscriviti alla cartella remota
* *purge*: elimina tutti i messaggi dalla cartella remota
* *send*: invia messaggio
* *exists*: controlla se il messaggio esiste
* *rule*: esegui regola sul corpo del testo
* *expunge*: elimina permanentemente i messaggi

Le operazioni sono elaborate solo quando c'è una connessione al server email o sincronizzando manualmente. Vedi anche [questa FAQ](#user-content-faq16).

<br />

<a name="faq4"></a>
**(4) Come posso usare un certificato di sicurezza non valido / password vuota / connessione di testo semplice?**

*... Non affidabile... non nel certificato...*
<br />
*... Ceritificato di sicurezza non valido (Impossibile verificare l'identità del server)...*

Questo può esser causato dall'uso di un nome errato dell'host, quindi prima ricontrollalo nelle impostazioni avanzate dell'identità/del profilo (tocca Configurazione manuale). Sei pregato di consultare la documentazione del provider email sul giusto nome dell'host.

Dovresti provare a risolverlo contattando il tuo provider o ottenendo un certificato di sicurezza valido, poiché quelli invalidi non sono sicuri e consentono [attacchi man-in-the-middle](https://en.wikipedia.org/wiki/Man-in-the-middle_attack). Se il denaro è un ostacolo, puoi ottenere certificati di sicurezza gratuiti da [Let' Encrypt](https://letsencrypt.org).

La soluzione rapida e non sicura (sconsigliata), è abilitare le *Connessioni non sicure* nelle impostazioni avanzate dell'identità (menu di navigazione, tocca *Impostazioni*, tocca *Configurazione manuale*, tocca *Identità*, tocca l'identità, tocca *Avanzate*).

Altrimenti, puoi accettare l'impronta digitale dei certificati non validi del server come segue:

1. Assicurati di star usando una connessione a internet affidabile (nessuna rete Wi-Fi pubblica, etc.)
1. Vai alla schermata di configurazione tramite il menu di navigazione (scorri verso l'interno dal lato sinistro)
1. Tocca Configurazione manuale, tocca Profili/Identità e tocca il profilo e l'identità difettosi
1. Controlla/salva il profilo e l'identità
1. Spunta la casella sotto al messaggio d'errore e salva di nuovo

Ciò "fisserà" il certificato del server per prevenire attacchi man-in-the-middle.

Nota che le versioni più vecchie di Android potrebbero non riconoscere autorità di certificazione più nuove come Let's Encrypt considerando le connessioni come non sicure, vedi anche [qui](https://developer.android.com/training/articles/security-ssl).

<br />

*Ancora di fiducia per il percorso di certificazione non trovata*

*... java.security.cert.CertPathValidatorException: Ancora di fiducia per il percorso di certificazione non trovata ...* significa che il gestore di fiducia predefinito di Android non è riuscito a verificare la catena di certificati del server.

Questo potrebbe esser dovuto al fatto che il certificato di root non è installato sul tuo dispositivo o perché mancano certificati intermedi, ad esempio perché il server email non li ha inviati.

Puoi risolvere il primo problema scaricando e installando il certificato di root dal sito web del provider del certificato.

Il secondo problema si dovrebbe risolvere modificando la configurazione del server o importando i certificati intermedi sul tuo dispositivo.

Puoi anche fissare il certificato, vedi sopra.

<br />

*Password vuota*

Il tuo nome utente è probabilmente facile da indovinare, quindi ciò è piuttosto insicuro, a meno che il server SMTP non sia disponibile tramite una rete locale limitata o solo tramite una VPN.

*Connessione di testo semplice*

Il tuo nome utente, la password e tutti i messaggi saranno inviati e ricevuti non crittografati, il che è **molto insicuro** perché un [attacco man-in-the-middle](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) è molto facile su una connessione non crittografata.

Se vuoi ancora usare un certificato di sicurezza non valido, una password vuota o una connessione di testo semplice, dovrai abilitare le connessioni non sicure nelle impostazioni del profilo e/o dell'identità. STARTTLS dovrebbe esser selezionato per le connessioni di testo semplice. Se abiliti le connessioni non sicure, dovresti connetterti solo tramite reti private e affidabili e mai via reti pubbliche, come quelle offerte in hotel, aeroporti, etc.

<br />

<a name="faq5"></a>
**(5) Come posso personalizzare la visualizzazione dei messaggi?**

Nel menu a tre puntini a scorrimento puoi abilitare, disabilitare o selezionare:

* *dimensione del testo*: per tre differenti dimensioni del font
* *vista compatta*: per altri elementi del messaggio compresso e un più piccolo font del testo del messaggio

Nella sezione di visualizzazione delle impostazioni puoi, ad esempio, abilitare o disabilitare:

* *Casella di posta in arrivo unificata*: per disabilitare la casella di posta in arrivo unificata ed elencare invece l'elenco delle cartelle selezionate per essa
* *Stile tabulare*: per mostrare un elenco lineare invece che delle carte
* *Raggruppa per data*: mostra l'intestazione della data sui messaggi con la stessa data
* *Threading della conversazione*: per disabilitare il threading della conversazione e mostrare piuttosto i singoli messaggi
* *Barra d'azione della conversazione*: per disabilitare la barra inferiore di navigazione
* *Colore d'evidenziazione*: per selezionare un colore per il mittente dei messaggi non letti
* *Mostra foto di contatto*: per nascondere le foto di contatto
* *Mostra nomi e indirizzi email*: per mostrare nomi o nomi e indirizzi email
* *Mostra oggetto in corsivo*: per mostrare l'oggetto del messaggio come testo normale
* *Mostra stelle*: per nascondere le stelle (preferiti)
* *Mostra anteprima del messaggio*: per mostrare 1-4 righe del testo del messaggio
* *Mostra i dettagli dell'indirizzo di default*: per espandere la sezione degli indirizzi di default
* *Mostra automaticamente il messaggio originale per i contatti noti*: per mostrare automaticamente i messaggi originali per i contatti sul tuo dispositivo, sei pregato di leggere [questa FAQ](#user-content-faq35)
* *Mostra automaticamente le immagini per i contatti noti*: per mostrare automaticamente le immagini per i contatti sul tuo dispositivo, sei pregato di leggere [questa FAQ](#user-content-faq35)

Nota che i messaggi sono visibili in anteprima solo quando il testo del messaggio è stato scaricato. I testi più grandi non sono scaricati di default su reti a consumo (generalmente mobili). Puoi cambiarlo nelle impostazioni di connessione.

Alcune persone chiedono:

* di mostrare l'oggetto in grassetto, ma il grassetto è già usato per evidenziare i messaggi non letti
* di spostare la stella a sinistra, ma è molto più facile adoperarla sul lato destro

<br />

<a name="faq6"></a>
**(6) Come posso accedere a Gmail / G suite?**

Se usi la versione di FairEmail del Play Store o di GitHub, puoi usare la procedura guidata di configurazione rapida per configurare facilmente il profilo e l'identità di Gmail. La procedura guidata di configurazione rapida di Gmail non è disponibile per build di terze parti, come quella di F-Droid perché Google ha approvato l'uso di OAuth per le sole build ufficiali.

Se non vuoi o non puoi usare un profilo Google sul dispositivo, ad esempio sui dispositivi recenti di Huawei, puoi abilitare l'accesso per "app meno sicure" e usare la password del tuo profilo (sconsigliato) o abilitare l'autenticazione a due fattori e usare una password specifica dell'app. Per usare una password dovrai configurare un profilo e un'identità tramite la configurazione manuale invece che tramite la procedura guidata di configurazione rapida.

**Importante**: a volte Google rilascia questo avviso:

*[ALERT] Sei pregato di accedere tramite il tuo browser web: https://support.google.com/mail/accounts/answer/78754 (Fallimento)*

Questo controllo di sicurezza di Google è causato più spesso con *app meno sicure* abilitato, meno con una password dell'app e difficilmente usando un profilo sul dispositivo (OAuth).

Sei pregato di vedere [questa FAQ](#user-content-faq111) sul perché solo i profili sul dispositivo sono utilizzabili.

Nota che una password specifica dell'app è necessaria quando è abilitata l'autenticazione a due fattori.

<br />

*Password specifica dell'app*

Vedi [qui](https://support.google.com/accounts/answer/185833) come generare una password specifica dell'app.

<br />

*Abilita "App meno sicure"*

**Importante**: usare questo metodo è sconsigliato perché meno affidabile.

**Importante**: I profili di Gsuite autorizzati con un nome utente/password smetteranno di funzionare [nel futuro prossimo](https://gsuiteupdates.googleblog.com/2019/12/less-secure-apps-oauth-google-username-password-incorrect.html)-.

Vedi [qui](https://support.google.com/accounts/answer/6010255) come abilitare le "app meno sicure" o vai [direttamente alle impostazioni](https://www.google.com/settings/security/lesssecureapps).

Se usi più profili di Gmail, assicurati di cambiare l'impostazione "app meno sicure" dei profili corretti.

Sappi che devi lasciare la schermata delle impostazioni "app meno sicure" usando la freccia indietro per applicare la modifica.

Se usi questo metodo, dovresti usare una [password forte](https://en.wikipedia.org/wiki/Password_strength) per il tuo profilo Gmail, che è comunque una buona idea. Nota che usare il protocollo IMAP [standard](https://tools.ietf.org/html/rfc3501) in sé non è meno sicuro.

Quando "app meno sicure" non è abilitato, otterrai l'errore *Autenticazione fallita - credenziali non valide* per i profili (IMAP) e *Nome utente e Password rifiutati* per le identità (SMTP).

<br />

*Generale*

Potresti ricevere l'avviso "*Sei pregato di accedere tramite il tuo browser web*". Questo si verifica quando Google considera la rete che ti connette a internet (potrebbe essere una VPN) come non sicura. Questo si può prevenire usando la procedura guidata di configurazione rapida di Gmail o una password specifica dell'app.

Vedi [qui](https://support.google.com/mail/answer/7126229) le istruzioni di Google e [qui](https://support.google.com/mail/accounts/answer/78754) per la risoluzione dei problemi.

<br />

<a name="faq7"></a>
**(7) Perché i messaggi inviati non appaiono (direttamente) nella cartella inviati?**

I messaggi inviati sono normalmente spostati dalla posta in uscita alla cartella delle email inviate appena il tuo provider aggiunge i messaggi inviati alla loro cartella. Questo richiede che una cartella di inviati sia selezionata nelle impostazioni del profilo e che, questa, sia impostata per la sincronizzazione.

Alcuni provider non tengono traccia dei messaggi inviati o il server SMTP usato potrebbe non esser correlato al provider. In questi casi FairEmail, aggiungerà automaticamente i messaggi inviati alla cartella degli inviati alla sincronizzazione della loro cartella, il che succederà dopo l'invio di un messaggio. Nota che questo risulterà in traffico internet aggiuntivo.

~~Se non succede, il tuo provider potrebbe non monitorare i messaggi inviati o potresti star usando un server SMTP non correlato al provider.~~ ~~In questi casi puoi abilitare l'impostazione di identità avanzata *Conserva i messaggi inviati* per permettere a FairEmail di aggiungere i messaggi inviati alla cartella inviati appena inviato un messaggio.~~ ~~Nota che abilitare questa impostazione potrebbe risultare in messaggi duplicati se anche il tuo provider aggiunge i messaggi inviati alla loro cartella.~~ ~~Inoltre, sappi che abilitare quest'impostazione risulterà in uso supplementare dei dati, specialmente inviando messaggi con grandi allegati.~~

~~Se i messaggi inviati in posta in uscita non si trovano nella cartella inviati a una sincronizzazione completa, saranno spostati anch'essi dalla posta in uscita alla cartella inviati.~~ ~~Una sincronizzazione completa si verifica riconnettendosi al server o sincronizzando periodicamente o manualmente.~~ ~~Potresti voler abilitare l'impostazione avanzata *Conserva messaggi inviati* invece di spostare i messaggi alla cartella inviati prima.~~

<br />

<a name="faq8"></a>
**(8) Posso usare un profilo di Microsoft Exchange?**

Il protocollo dei Servizi Web di Microsoft Exchange [è in fase di eliminazione](https://techcommunity.microsoft.com/t5/Exchange-Team-Blog/Upcoming-changes-to-Exchange-Web-Services-EWS-API-for-Office-365/ba-p/608055). Quindi, ormai, potrebbe aver poco senso aggiungere questo protocollo.

Puoi usare un profilo di Microsoft Exchange se è accessibile via IMAP, com'è principalmente il caso. Vedi [qui](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793) per ulteriori informazioni.

Nota che la descrizione di FairEmail inizia con l'osservazione che i protocolli non standard, come i Servizi Web di Microsoft Exchange e Microsoft ActiveSync non sono supportati.

Sei pregato di vedere [qui](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) per la documentazione Microsoft sulla configurazione di un client email. Esiste anche una sezione sugli errori di connessione comuni e le loro soluzioni.

Alcune versioni più vecchie del server di Exchange hanno un bug che causa messaggi vuoti e allegati corrotti. Sei pregato di vedere [questa FAQ](#user-content-faq110) per trovare una soluzione.

Sei pregato di vedere [questa FAQ](#user-content-faq133) sul supporto di ActiveSync.

Sei pregato di vedere [questa FAQ](#user-content-faq111) sul supporto di OAuth.

<br />

<a name="faq9"></a>
**(9) Cosa sono le identità / come aggiungo un alias?**

Le identità rappresentano gli indirizzi email *da* cui stai inviando tramite un server email (SMTP).

Alcuni provider ti consentono di avere diversi alias. Puoi configurarli impostando il campo dell'indirizzo email di un'identità aggiuntiva all'indirizzo dell'alias e impostando il campo del nome utente all'indirizzo email principale.

Nota che puoi copiare un'identità tenendola premuta.

In alternativa, puoi abilitare *Consenti la modifica dell'indirizzo del mittente* nelle impostazioni avanzate di un'identità esistente per modificare il nome utente componendo un nuovo messaggio, se il tuo provider lo consente.

FairEmail aggiornerà automaticamente le password delle identità correlate aggiornando la password del profilo associato o di un'identità correlata.

Vedi [questa FAQ](#user-content-faq33) sulla modifica del nome utente degli indirizzi email.

<br />

<a name="faq10"></a>
**~~(10) Cosa significa 'UIDPLUS non supportato'?~~**

~~Il messaggio d'errore *UIDPLUS non supportato* significa che il tuo provider email non fornisce l'[estensione UIDPLUS](https://tools.ietf.org/html/rfc4315) di IMAP. Quest'estensione di IMAP è necessaria per implementare la sincronizzazione a due fasi, che non è una funzionalità opzionale. Quindi, a meno che il tuo provider possa abilitare quest'estensione, non puoi usare FairEmail per questo provider.~~

<br />

<a name="faq11"></a>
**~~(11) Perché POP non è supportato?~~**

~~A parte che ogni provider email decente supporta [IMAP](https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol) ad oggi,~~ ~~usare [POP](https://en.wikipedia.org/wiki/Post_Office_Protocol) risulterà in un uso extra non necessario della batteria e notifiche dei nuovi messaggi ritardate.~~ ~~Peraltro, POP è inadatto alla sincronizzazione a due fasi e più spesso che mai, oggi, le persone leggono e scrivono messaggi su dispositivi differenti.~~

~~Fondamentalmente, POP supportato solo il download e l'eliminazione dei messaggi dalla posta in arrivo.~~ ~~Quindi, operazioni comuni come l'impostazione degli attributi del messaggio (letto, preferito, risposto, etc.), l'aggiunta (backup) e spostamento dei messaggi, sono impossibili.~~

~~Vedi anche [cosa scrive Google a riguardo](https://support.google.com/mail/answer/7104828).~~

~~Per esempio [Gmail può importare i messaggi](https://support.google.com/mail/answer/21289) da un altro profilo POP,~~ ~~utilizzabile come soluzione quando il tuo provider non supporta IMAP.~~

~~In breve, considera di passare a IMAP.~~

<br />

<a name="faq12"></a>
**(12) Come funziona la crittografia/decrittografia?**

La comunicazione con i server email è sempre crittografata, a meno che tu non l'abbia esplicitamente disattivata. Questa domanda è sulla crittografia end-to-end facoltativa con PGP o S/MIME. Il mittente e il destinatario dovrebbero prima acconsentire e scambiarsi messaggi firmati per trasferire la loro chiave pubblica e poter inviare messaggi crittografati.

<br />

*Generale*

Sei pregato di [vedere qui](https://en.wikipedia.org/wiki/Public-key_cryptography) come funziona la crittografia a chiave pubblica/privata.

La crittografia in breve:

* I messaggi **in uscita** sono crittografati con la **chiave pubblica** del destinatario
* I messaggi **in entrata** sono decrittografati con la **chiave privata** del destinatario

Le firme in breve:

* I messaggi **in uscita** sono firmati con la **chiave privata** del mittente
* I messaggi **in entrata** sono verificati con la **chiave pubblica** del mittente

Per firmare/crittografare un messaggio, basta selezionare il metodo appropriato nella finestra di invio. Puoi sempre aprire la finestra di invio usando il menu di trabocco a tre puntini nel caso tu abbia selezionato prima *Non mostrare più*.

Per verificare una firma o decrittografare un messaggio ricevuto, aprilo e tocca il gesto o l'icona del lucchetto proprio sotto la barra d'azione del messaggio.

La prima volta che invii un messaggio firmato/crittografato potrebbe esserti chiesta una chiave di firma. FairEmail memorizzerà automaticamente la chiave di firma selezionata nell'identità usata per la volta successiva. Se devi ripristinarla, basta salvare l'identità o premerla a lungo nell'elenco delle identità e selezionare *Ripristina chiave di firma*. La chiave di firma selezionata è visibile nell'elenco delle identità. Se devi selezionare una chiave in base al caso, puoi creare più identità per lo stesso profilo con lo stesso indirizzo email.

Nelle impostazioni di crittografia, puoi selezionare il metodo predefinito (PGP o S/MIME), abilitare la *Firma predefinita*, *Crittografia predefinita* e *Decrittografare automaticamente i messaggi*, ma sappi che la decrittografia automatica è impossibile se l'interazione dell'utente è necessaria, come selezionando una chiave o leggendo un token di sicurezza.

Il testo/gli allegati del messaggio da crittografare e il testo/allegati del messaggio decrittografato sono conservati solo localmente e non saranno mai aggiunti al server remoto. Se vuoi annullare la decrittografia, puoi usare l'elemento *resync* del menu nel menu a tre puntini della barra d'azione del messaggio.

<br />

*PGP*

Dovrai prima installare e configurare [OpenKeychain](https://f-droid.org/en/packages/org.sufficientlysecure.keychain/). FairEmail è stato testato con la versione 5.4 di OpenKeychain. Le versioni successive saranno più probabilmente compatibili, ma le versioni precedenti potrebbero non esserlo.

**Importante**: l'app OpenKeychain è nota per i suoi arresti anonimi (improvvisi) quando la chiamata dell'app (FairEmail) non è ancora autorizzata e sta ricevendo una chiave pubblica esistente. Puoi risolvere ciò provando a inviare un messaggio firmato/crittografato a un mittente con una chiave pubblica sconosciuta.

**Importante**: se l'app di OpenKeychain non trova (più) una chiave, potresti doverne ripristinare una selezionata precedentemente. Questo si può fare premendo a lungo un'identità nell'elenco di identità (Impostazioni, tocca Configurazione manuale, tocca Identità).

**Importante**: per far connettere affidabilmente le app come FairEmail al servizio di OpenKeychain per crittografare/decrittografare i messaggi, potrebbe esser necessario disabilitare le ottimizzazioni della batteria per l'app di OpenKeychain.

**Importante**: l'app di OpenKeychain necessita dell'autorizzazione dei contatti per funzionare correttamente.

**Importante**: su alcune versioni / dispositivi di Android è necessario abilitare *Mostra popup con l'esecuzione in background* nelle autorizzazioni aggiuntive delle impostazioni delle app di Android dell'app OpenKeychain. Senza questo permesso, la bozza sarà salvata, ma il popup di OpenKeychain per confermare/selezionare potrebbe non comparire.

FairEmail invierà l'intestazione di [Autocrypt](https://autocrypt.org/) per l'uso da altri client di email, ma solo per i messaggi firmati e crittografati perché troppi server email hanno problemi con le intestazioni spesso lunghe di Autocrypt. Nota che il modo più sicuro per avviare uno scambio di email crittografate è inviando prima i messaggi firmati. Le intestazioni Autocrypt ricevute saranno inviate all'app di OpenKeychain per l'archiviazione alla verifica di una firma o decrittografare un messaggio.

Sebbene questo non dovrebbe esser necessario per gran parte dei client email, puoi allegare la tua chiave pubblica a un messaggio e se usi *.key* come estensione, il tipo di mime sarà correttamente *application/pgp-keys*.

Tutta la gestione delle chiavi è delegata all'app OpenKeychain per motivi di sicurezza. Questo significa anche che FairEmail non conserva le chiavi PGP.

Il PGP crittografato in linea nei messaggi ricevuti è supportato, ma le firme PGP in linea e i PGP in linea nei messaggi in uscita non sono supportati, vedi [qui](https://josefsson.org/inline-openpgp-considered-harmful.html) perché no.

I messaggi solo firmati o solo crittografati non sono una buona idea, sei pregato di vedere qui perché non lo sono:

* [Considerazioni su OpenPGP Parte I](https://k9mail.github.io/2016/11/24/OpenPGP-Considerations-Part-I.html)
* [Considerazioni su OpenPGP Parte II](https://k9mail.github.io/2017/01/30/OpenPGP-Considerations-Part-II.html)
* [Considerazioni su OpenPGP Parte III Autocrypt](https://k9mail.github.io/2018/02/26/OpenPGP-Considerations-Part-III-Autocrypt.html)

I messaggi solo firmati sono supportati, quelli non crittografati no.

Errori comuni:

* *Nessuna chiave*: non è disponibile alcuna chiave PGP per uno degli indirizzi email elencati
* *Chiave mancante per la crittografia*: probabilmente una chiave selezionata in FairEmail non esiste più nell'app di OpenKeychain. Ripristinare la chiave (vedi sopra) probabilmente risolverà questo problema.
* *Chiave per la verifica della firma mancante*: la chiave pubblica per il mittente non è disponibile nell'app di OpenKeychain. Questo può esser causato da Autocrypt se disabilitato nelle impostazioni di crittografia o dal mancato invio dell'intestazione di Autocrypt.

<br />

*S/MIME*

Crittografare un messaggio richiede le chiavi pubbliche dei destinatari. Firmare un messaggio richiede la tua chiave privata.

Le chiavi private sono conservate da Android e sono importabili tramite le impostazioni avanzate di sicurezza di Android. Esiste una scorciatoia (pulsante) per questo nelle impostazioni di crittografia. Android ti chiederà di impostare un PIN, schema o password se non lo hai fatto prima. Se hai un dispositivo Nokia con Android 9, sei pregato di [leggere prima questo](https://nokiamob.net/2019/08/10/a-bug-prevents-nokia-1-owners-from-unlocking-their-screen-even-with-right-pin-pattern/).

Nota che i certificati possono contenere più chiavi per più scopi, ad esempio per l'autenticazione, la crittografia e la firma. Android importa solo la prima chiave, quindi per importarle tutte, il certificato dev'esser diviso. Non è molto banale e ti si consiglia di chiedere supporto al fornitore del certificato.

Nota che la firma S/MIME con altri algoritmi oltre RSA è supportata, ma sappi che altri client email potrebbero non supportarla. La crittografia S/MIME è possibile solo con algoritmi asimmetrici, il che significa in pratica usando RSA.

Il metodo di crittografia predefinito è PGP, ma l'ultimo metodo di crittografia usato sarà ricordato per l'identità selezionata per la volta successiva. Puoi premere a lungo il pulsante di invio per modificare il metodo di crittografia per un'identità. Se usi sia la crittografia PGP che S/MIME per lo stesso indirizzo email, potrebbe essere utile copiare l'identità, così da cambiare il metodo di crittografia selezionandone una delle due. Puoi premere a lungo su un'identità nell'elenco delle identità (tramite configurazione manuale nella schermata principale di configurazione) per copiare un'identità.

Per consentire chiavi private differenti per lo stesso indirizzo email, FairEmail ti farà sempre selezionare una chiave quando ci sono identità multiple con lo stesso indirizzo email per lo stesso profilo.

Le chiavi pubbliche sono conservate da FairEmail e sono importabili verificando una firma per la prima volta o tramite le impostazioni di crittografia (formato PEM o DER).

FairEmail verifica sia la firma che l'intera catena di certificati.

Errori comuni:

* *Nessun certificato corrispondente a targetContraints trovato*: questo potrebbe significare che stai usando una vecchia versione di FairEmail
* *Impossibile trovare il percorso valido di certificazione all'obiettivo richiesto*: fondamentalmente ciò significa che non sono stati trovati uno o più certificati intermedi o di root
* *La chiave privata non corrisponde ad alcuna chiave di crittografia*: la chiave selezionata non è utilizzabile per decrittografare il messaggio, probabilmente perché è errata
* *Nessuna chiave privata*: nessun certificato selezionato o disponibile nel keystore di Android

Nel caso in cui la catena del certificato sia scorretta, puoi toccare sul piccolo pulsante di informazioni per mostrare tutti i certificati. Dopo i dettagli del certificato è mostrato il mittente o "selfSign". Un certificato è auto-firmato quando l'oggetto e l'emittente corrispondono. I certificati da un'autorità di certificazione (CA) sono contrassegnati con "[keyCertSign](https://tools.ietf.org/html/rfc5280#section-4.2.1.3)". I certificati trovati nella memoria di chiavi di Android sono contrassegnati con "Android".

Una catena valida somiglia a questa:

```
Il tuo certificato > zero o più certificati intermedi > certificato CA (di root) contrassegnato con "Android"
```

Nota che una catena di certificati sarà sempre non valida quando non è trovato alcun certificato d'ancoraggio nella memoria di chiavi di Android, fondamentale per la convalida del certificato S/MIME.

Sei pregato di vedere [qui](https://support.google.com/pixelphone/answer/2844832?hl=en) come puoi importare i certificati nella memoria di chiavi di Android.

L'uso delle chiavi scadute, dei messaggi crittografati/firmati in linea e i token di sicurezza hardware non è supportato.

Se stai cercando un certificato (di prova) gratuito S/MIME, vedi [qui](http://kb.mozillazine.org/Getting_an_SMIME_certificate) per le opzioni. Sei pregato di accertarti di [leggere prima questo](https://davidroessli.com/logs/2019/09/free-smime-certificates-in-2019/#update20191219) se vuoi richiedere un certificato S/MIME Actalis. Se stai cercando un certificato S/MIME economico, ho avuto una buona esperienza con [Certum](https://www.certum.eu/en/smime-certificates/).

Come estrarre una chiave pubblica da un certificato S/MIME:

```
openssl pkcs12 -in filename.pfx/p12 -clcerts -nokeys -out cert.pem
```

Puoi decrittografare le firme S/MIME, etc., [qui](https://lapo.it/asn1js/).

<br />

*pretty Easy privacy*

Non esiste ancora [alcuno standard approvato](https://tools.ietf.org/id/draft-birk-pep-00.html) per pretty Easy privacy (p≡p) e non molte persone lo usano.

Tuttavia, FairEmail può inviare e ricevere messaggi crittografati PGP, compatibili con p≡p. Inoltre, FairEmail riconosce i messaggi p≡p in arrivo dalla versione 1.1519, quindi l'oggetto crittografato sarà mostrato e il testo del messaggio incorporato sarà mostrato più piacevolmente.

<br />

La firma/crittografia S/MIME è una funzionalità pro, ma tutte le altre operazioni PGP e S/MIME sono gratuite.

<br />

<a name="faq13"></a>
**(13) Come funziona la ricerca su dispositivo/server?**

Puoi avviare la ricerca di messaggi per mittente (da), destinatario (a, cc, ccn), oggetto, parole chiave o testo del messaggio usando la lente di ingrandimento nella barra d'azione di una cartella. Puoi anche cercare da ogni app selezionando *Cerca email* nel menu popup copia/incolla.

Cercare nella casella unificata cercherà in tutte le cartelle di tutti i profili, cercando cercare nell'elenco delle cartelle cercherà solo nel profilo associato e cercare in una cartella cercherà solo in quella cartella.

I messaggi saranno cercati prima sul dispositivo. Ci sarà un pulsante di azione con un'icona per ripetere la ricerca in fondo per continuare la ricerca sul server. Puoi selezionare in quale cartella continuare la ricerca.

Il protocollo IMAP non supporta la ricerca in più di una cartella in contemporanea. La ricerca sul server è un'operazione costosa, dunque non è possibile selezionare più cartelle.

La ricerca dei messaggi locali non è sensibile alle maiuscole e al testo parziale. Il testo del messaggio dei messaggi locali non sarà cercato se il testo del messaggio non è ancora stato caricato. La ricerca sul server potrebbe dipendere o meno dalle maiuscole e potrebbe essere su testo parziale o parole intere, in base al fornitore.

Alcuni server non riescono a gestire la ricerca nel testo del messaggio quando c'è un gran numero di messaggi. Per questo caso esiste un'opzione per disabilitare la ricerca nel testo del messaggio.

Si possono usare gli operatori di ricerca di Google prefissando un comando di ricerca con *raw:*. Se hai configurato solo un profilo Gmail, puoi avviare la ricerca grezza direttamente sul server, cercando dalla casella di posta unificata. Se hai configurato più profili Gmail, prima dovrai navigare all'elenco delle cartelle o alla cartella dell'archivio (tutti i messaggi) del profilo Gmail in cui vuoi cercare. Sei pregato di [vedere qui](https://support.google.com/mail/answer/7190) per tutti i possibili operatori di ricerca. Ad esempio:

`
raw:larger:10M`

Cercare in un gran numero di messaggi sul dispositivo non è molto veloce per due limitazioni:

* [sqlite](https://www.sqlite.org/), il motore del database di Android ha un limite di dimensioni del registro, impedendo ai testi dei messaggi di esser memorizzati nel database
* Le app di Android hanno memoria limitata per funzionare, anche se il dispositivo ne ha molta disponibile

Questo significa che cercare il testo di un messaggio richiede che i file contenenti i testi del messaggio siano aperti uno per uno per controllare se il testo ricercato sia contenuto nel file, il che è un processo relativamente espansivo.

Nelle *impostazioni varie* puoi abilitare *Crea indice di ricerca* per aumentare significativamente la velocità di ricerca sul dispositivo, ma sappi che questo aumenterà l'uso della batteria e dello spazio di archiviazione. L'indice di ricerca si basa sulle parole, quindi non è possibile cercare testi parziali. Cercare usando l'indice di ricerca è predefinito ad E, quindi cercare *mela arancia* cercherà mela E arancia. Le parole separate da virgole risulteranno nella ricerca per O, quindi ad esempio *mela, arancia* cercherà mela O arancia. Entrambi sono combinabili, quindi cercare *mela, arancia banana* cercherà mela O (arancia E banana). L'uso dell'indice di ricerca è una funzionalità pro.

Dalla versione 1.1315 è possibile usare espressioni di ricerca come questa:

```
mela +banana -ciliegia ?noci
```

Questo risulterà nel cercare in questo modo:

```
("mela" E "banana" E NON "ciliegia") O "noci"
```

Le espressioni di ricerca sono utilizzabili per cercare sul dispositivo tramite l'indice di ricerca e sul server email, ma non per cercare sul dispositivo senza l'indice di ricerca per motivi di prestazioni.

La ricerca sul dispositivo è una funzionalità gratuita, usare l'indice di ricerca e cercare sul server è una funzionalità pro.

<br />

<a name="faq14"></a>
**(14) Come posso configurare un profilo di Outlook / Live / Hotmail?**

Un profilo Outlook / Live / Hotmail si può configurare tramite la procedura guidata di configurazione rapida e selezionando *Outlook*.

Per usare un profilo di Outlook, Live o Hotmail con l'autenticazione a due fattori abilitata, devi creare una password dell'app. Vedi [qui](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification) per i dettagli.

Vedi [qui](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) per le istruzioni di Microsoft.

Per configurare un profilo di Office 365, sei pregato di vedere [questa FAQ](#user-content-faq156).

<br />

<a name="faq15"></a>
**(15) Perché il testo del messaggio continua a caricare?**

L'intestazione e il corpo del messaggio sono recuperati separatamente dal server. Il testo del messaggio di messaggi più grandi non è recuperato in anticipo su connessioni misurate e sarà recuperato su domanda all'espansione di un messaggio. Il testo del messaggio continuerà a caricare se non c'è connessione al profilo, vedi anche la domanda successiva, o se ci sono altre operazioni in esecuzione, come la sincronizzazione dei messaggi.

Puoi controllare il profilo e l'elenco delle cartelle per lo stato del profilo e della cartella (vedi la legenda per il significato delle icone) e l'elenco di operazioni accessibile tramite il menu principale di navigazione per le operazioni in sospeso (vedi [questa FAQ](#user-content-faq3) per il significato delle operazioni).

Se FairEmail si sta bloccando per problemi di connettività precedenti, sei pregato di vedere [questa FAQ](#user-content-faq123), puoi forzare la sincronizzazione tramite il menu a tre puntini.

Nelle impostazioni di ricezione puoi configurare la dimensione massima per scaricare automaticamente i messaggi su connessioni misurate.

Le connessioni mobili sono quasi sempre misurate e lo sono anche alcuni hotspot Wi-Fi (pagati).

<br />

<a name="faq16"></a>
**(16) Perché i messaggi non vengono sincronizzati?**

Possibili cause della mancata sincronizzazione dei messaggi (inviati o ricevuti) sono:

* Il profilo o le cartelle non sono impostati per la sincronizzazione
* Il numero di giorni per cui sincronizzare i messaggi è troppo basso
* Non c'è alcuna connessione utilizzabile a internet
* Il server email non è temporaneamente disponibile
* Android ha arrestato il servizio di sincronizzazione

Quindi, controlla il tuo profilo e le impostazioni della cartella e verifica se i profili/le cartelle sono connesse (vedi la legenda nel menu di navigazione per il significato delle icone).

Se ci sono messaggi di errore, sei pregato di vedere [questa FAQ](#user-content-faq22).

Su alcuni dispositivi, dove ci sono molte applicazioni che competono per la memoria, Android potrebbe interrompere il servizio di sincronizzazione come ultima risorsa.

Alcune versioni di Android arrestano le app e i servizi troppo aggressivamente. Vedi [questo sito dedicato](https://dontkillmyapp.com/) e [questo problema di Android](https://issuetracker.google.com/issues/122098785) per ulteriori informazioni.

Disabilitare le ottimizzazioni della batteria (fase 3 della configurazione) riduce la possibilità che Android arresterà il servizio di sincronizzazione.

In caso di errori consecutivi di connessione, FairEmail impiegherà sempre di più per non drenare la batteria del tuo dispositivo. Questo è descritto in [questa FAQ](#user-content-faq123).

<br />

<a name="faq17"></a>
**~~(17) Perché la sincronizzazione manuale non funziona?~~**

~~Se il menu *Sincronizza ora* è oscurato, non c'è connessione al profilo.~~

~~Vedi la domanda precedente per ulteriori informazioni.~~

<br />

<a name="faq18"></a>
**(18) Perché l'anteprima del messaggio non è sempre mostrata?**

L'anteprima del testo del messaggio non è mostrabile se il corpo del messaggio non è ancora stato scaricato. Vedi anche [questa FAQ](#user-content-faq15).

<br />

<a name="faq19"></a>
**(19) Perché le funzionalità pro sono così costose?**

Prima di tutto, **FairEmail è fondamentalmente gratuito** e solo alcune funzionalità avanzate devono esser acquistate.

Prima di tutto, **FairEmail è fondamentalmente gratuito** e solo alcune funzionalità avanzate devono esser acquistate.

Prima di tutto, **FairEmail è fondamentalmente gratuito** e solo alcune funzionalità avanzate devono esser acquistate.

Sei pregato di consultare la descrizione dell'app del Play Store o [vedere qui](https://email.faircode.eu/#pro) per un elenco completo delle funzionalità pro.

La domanda corretta è "*perché ci sono così tante tasse e commissioni?*":

* IVA: 25% (in base al tuo paese)
* Commissione di Google: 30%
* Tassa sul reddito: 50%
* <sub>Quota di PayPal: 5-10% in base al paese/importo</sub>

Quindi, quanto rimane allo sviluppatore è solo una frazione di ciò che paghi.

Nota anche che gran parte delle app gratuite non compariranno sostenibili alla fine, mentre FairEmail è mantenuto e supportato propriamente, e che le app gratuite potrebbero contenere una fregatura, come l'invio di informazioni sensibili in Internet. Non ci sono nemmeno annunci che violano la privacy nell'app.

Ho lavorato a FairEmail quasi ogni giorno per oltre due anni, quindi penso che il prezzo sia più che ragionevole. Per questo motivo non ci saranno nemmeno sconti.

<br />

<a name="faq20"></a>
**(20) Posso ottenere un rimborso?**

Se una funzionalità pro acquistata non funziona come previsto e questo non è causato da un problema nelle funzionalità gratuite e non posso risolvere il problema tempestivamente, riceverai un rimborso. In tutti gli altri casi non è possibile alcun rimborso. In nessuna circostanza è possibile un rimborso per qualsiasi problema correlato alle funzionalità gratuite, poiché non è stato pagato nulla per esse e perché sono valutabili senza alcun limite. Mi prendo la mia responsabilità come venditore di consegnare quanto promesso e mi aspetto che vi prendiate la responsabilità di informarvi su cosa state comprando.

<a name="faq21"></a>
**(21) Come abilito il LED di notifica?**

Prima di Android 8 Oreo: c'è un'opzione avanzata nelle impostazioni di notifica dell'app per questo.

Android 8 Oreo e successive: sei pregato di vedere [qui](https://developer.android.com/training/notify-user/channels) come configurare i canali di notifica. Puoi usare il pulsante *Canale predefinito* nelle impostazioni di notifica dell'app per andare direttamente alle impostazioni del canale di notifica di Android.

Nota che le app non posso più modificare le impostazioni di notifica, incluse quelle del LED di notifica, su Android 8 Oreo e successive.

A volte è necessario disabilitare l'impostazione *Mostra anteprima del messaggio nelle notifiche* o abilitare le impostazioni *Mostra le notifiche solo con un testo d'anteprima* per risolvere i bug in Android. Questo potrebbe applicarsi anche ai suoni di notifica e le vibrazioni.

Impostare un colore della luce prima di Android 8 non è supportato ed è impossibile su Android 8 e successive.

<br />

<a name="faq22"></a>
**(22) Cosa significa errore del profilo/della cartella... ?**

FairEmail non nasconde gli errori come app simili fanno spesso, così che sia più facile diagnosticare i problemi.

FairEmail proverà automaticamente a riconnettersi dopo un ritardo. Questo ritardo sarà raddoppiato dopo ogni tentativo fallito per prevenire il drenaggio della batteria e che si blocchi permanentemente. Sei pregato di vedere [questa FAQ](#user-content-faq123) per ulteriori informazioni a riguardo.

Esistono errori generali e specifici ai profili di Gmail (vedi sotto).

**Errori generali**

<a name="authfailed"></a>
L'errore *... **Autenticazione fallita** ...* o *... AUTHENTICATE fallita ...* potrebbe significare che il tuo nome utente o la password fossero errati. Alcuni provider prevedono come nome utente solo *nomeutente* e altri il tuo indirizzo email completo *nomeutente@esempio.com*. Quando copi/incolli per inserire un nome utente o una password, potrebbero esser copiati caratteri invisibili, che potrebbero anch'essi causare tale problema. Inoltre, alcuni gestori di password sono noti per farlo erroneamente. Il nome utente potrebbe esser sensibile alle maiuscole, quindi prova con soli caratteri minuscoli. La password è quasi sempre sensibile alle maiuscole. Alcuni provider richiedono l'uso di una password dell'app invece di quella del profilo, sei quindi pregato di controllare la documentazione del provider. A volte è prima necessario abilitare l'accesso esterno (IMAP/SMTP) sul sito web del provider. Altre cause possibili sono che il profilo sia bloccato o l'accesso sia stato limitato amministrativamente in qualche modo, ad esempio consentendo l'accesso solo da certe reti / certi indirizzi IP.

Se necessario, puoi aggiornare una password nelle impostazioni del profilo: menu di navigazione (menu laterale sinistro), tocca *Impostazioni*, tocca *Configurazione manuale*, tocca *Profili* e tocca sul profilo. Cambiare la password del profilo in gran parte dei casi modificherà automaticamente anche la password delle identità correlate. Se il profilo è stato autorizzato tramite la procedura guidata di configurazione rapida invece che con una password, puoi eseguire di nuovo la procedura guidata di configurazione rapida e spuntare *Autorizza di nuovo profilo esistente* per autenticare nuovamente il profilo. Nota che questo richiede una versione recente dell'app.

L'errore *... Troppi tentativi di autenticazione errati ...* potrebbe significare che stai usando una password del profilo di Yahoo invece di una password dell'app. Sei pregato di vedere [questa FAQ](#user-content-faq88) su come configurare un profilo di Yahoo.

Il messaggio *... +OK ...* potrebbe significare che una porta POP3 (di solito la porta numero 995) è in uso per un profilo IMAP (di solito la porta numero 993).

Gli errori *... saluto non valido ...*, *... richiede indirizzo valido ...* e *... Parametro a HELO non conforme alla sintassi RFC ...* potrebbero esser risolti cambiando l'impostazione avanzata di identità *Usa indirizzo IP locale invece del nome dell'host*.

L'errore *... Impossibile connettere all'host ...* significa che non c'è stata alcuna risposta dal server dell'email entro un tempo ragionevole (di default, 20 secondi). Principalmente indica problemi di connettività a internet, possibilmente causati da una VPN o un'app del firewall. Puoi provare ad aumentare il timeout di connessione nelle impostazioni di connessione di FairEmail, per quando il server email è davvero lento.

L'errore *... Connessione rifiutata ...* significa che il server email o qualcosa tra di esso e l'app, come un firewall, ha rifiutato attivamente la connessione.

L'errore *... Rete irraggiungibile ...* significa che il server email era irraggiungibile tramite la corrente connessione a internet, ad esempio perché il traffico internet è limitato al solo traffico locale.

L'errore *... Host irrisolto ...*, *... Impossibile risolvere l'host ...* o *... Nessun indirizzo associato al nome dell'host ...* significa che non è stato possibile risolvere l'indirizzo del server email in un indirizzo IP. Questo potrebbe esser causato da una VPN, blocco annunci o un server [DNS](https://en.wikipedia.org/wiki/Domain_Name_System) (locale) irraggiungibile o funzionante impropriamente.

L'errore *... Il software ha causato l'annullamento ...* significa che il server email o qualcosa tra FairEmail e il server email ha terminato attivamente una connessione esistente. Questo può verificarsi ad esempio quando la connettività è bruscamente persa. Un esempio tipico è l'attivazione della modalità aereo.

Gli errori *... BYE Disconnessione ...*, *... Ripristino della connessione ...* significa che il server email o qualcosa tra di esso e l'app, ad esempio un router o un firewall (app) ha terminato attivamente una connessione esistente.

L'errore *... Connessione chiusa da pari ...* potrebbe esser causato da un server non aggiornato di Exchange, vedi [qui](https://blogs.technet.microsoft.com/pki/2010/09/30/sha2-and-windows/) per ulteriori informazioni.

Gli errori *... Errore di lettura ...*, *... Errore di scrittura ...*, *... Lettura scaduta ...*, *... Conduttura rotta ...* significa che il server email non risponde più o che la connessione a internet non è buona.

<a name="connectiondropped"></a>
L'errore *... Connessione staccata dal server? ...* significa che il server email ha terminato in modo imprevisto la connessione. Questo si verifica talvolta quando c'erano troppe connessioni un tempo troppo breve o quando è stata usata per troppe volte una password errata. In questo caso, sei pregato di assicurarti che sia corretta e di disabilitare la ricezione nelle impostazioni di ricezione per circa 30 minuti e riprovare. Se necessario, vedi [questa FAQ](#user-content-faq23) su come puoi ridurre il numero di connessioni.

L'errore *... Interruzione imprevista del flusso di input di zlib ...* significa che non sono stati ricevuti tutti i dati, possibilmente a causa di una connessione non buona o interrotta.

L'errore *... connessione fallita ...* potrebbe indicare [Troppe connessioni simultanee](#user-content-faq23).

L'avviso *... Crittografia non supportata ...* significa che la serie di caratteri del messaggio è sconosciuta o non supportata. FairEmail presumerà ISO-8859-1 (Latin1), che in molti casi risulterà nella visualizzazione corretta del messaggio.

L'errore *... Superato il Limite del Tasso di Accesso ...* significa che si sono verificati troppi tentativi di accesso con una password errata. Sei pregato di ricontrollare la tua password o autenticare di nuovo il profilo con la procedura guidata di configurazione rapida (solo OAuth).

Sei pregato di [vedere qui](#user-content-faq4) per gli errori *... Non fidata ... non nel certificato ...*, *... Certificato di sicurezza non valido (Impossibile verificare l'identità del server) ...* o *... Ancoraggio di fiducia per il percorso di certificazione non trovato ...*

Sei pregato di [vedere qui](#user-content-faq127) per l'errore *... Argomenti HELO sintatticamente non validi ...*.

Sei pregato di [vedere qui](#user-content-faq41) per l'errore *... Handshake fallito ...*.

Vedi [qui](https://linux.die.net/man/3/connect) cosa significano i codici di errore come EHOSTUNREACH e ETIMEDOUT.

Sono cause possibili:

* Un firewall o router che sta bloccando le connessioni al server
* Il nome dell'host o il numero di porta non è valido
* Ci sono problemi con la connessione a internet
* Ci sono problemi con la risoluzione dei nomi di dominio (Yandex: prova a disabilitare il DNS privato nelle impostazioni di Android)
* Il server email si sta rifiutando di accettare le connessioni (esterne)
* Il server email si sta rifiutando di accettare un messaggio, ad esempio perché troppo grande o contenente link inaccettabili
* Ci sono troppe connessioni al server, vedi anche la prossima domanda

Molte reti Wi-Fi pubbliche bloccano le email in uscita per prevenire lo spam. A volte puoi risolvere ciò usando un'altra porta SMTP. Vedi la documentazione del provider per i numeri di porta utilizzabili.

Se stai usando una [VPN](https://en.wikipedia.org/wiki/Virtual_private_network), il suo fornitore potrebbe bloccare la connessione perché prova troppo aggressivamente a prevenire lo spam. Nota che anche [Google Fi](https://fi.google.com/) usa una VPN.

**Errori di invio**

I server SMTP possono rifiutare i messaggi per [diversi motivi](https://en.wikipedia.org/wiki/List_of_SMTP_server_return_codes). Messaggi troppo grandi e l'innesco del filtro antispam di un server email sono i motivi più comuni.

* Il limite della dimensione dell'allegato per Gmail [è 25 MB](https://support.google.com/mail/answer/6584)
* Il limite delle dimensioni dell'allegato per Outlook e Office 365 [è 20 MB](https://support.microsoft.com/en-us/help/2813269/attachment-size-exceeds-the-allowable-limit-error-when-you-add-a-large)
* Il limite della dimensione dell'allegato per Yahoo [è 25 MB](https://help.yahoo.com/kb/SLN5673.html)
* *554 5.7.1 Servizio non disponibile; host del client xxx.xxx.xxx.xxx bloccato*, sei pregato [di vedere qui](https://docs.gandi.net/en/gandimail/faq/error_types/554_5_7_1_service_unavailable.html)
* *501 Errore di sintassi – riga troppo lunga* è spesso causata dall'uso di una lunga intestazione di Autocrypt
* *503 5.5.0 Destinatario già specificato* significa principalmente che un indirizzo è in uso sia come indirizzo TO che CC
* *554 5.7.1 ... non è consentito trasmettere* significa che il server email non riconosce il nome utente/indirizzo email. Sei pregato di ricontrollare il nome dell'host e il nome utente/indirizzo email nelle impostazioni dell'identità.
* *550 Messaggio di spam rifiutato perché l'IP è elencato da...* significa che il server email ha rifiutato di inviare un messaggio dall'indirizzo di rete corrente (pubblico) perché usato erroneamente per inviare spam da qualcun altro (si spera) in precedenza. Sei pregato di provare ad abilitare la modalità aereo per 10 minuti per acquisire un nuovo indirizzo di rete.
* *550 Siamo spiacenti, impossibile inviare la tua email. La materia del soggetto, un collegamento o un allegato contiene potenzialmente spam, phishing o malware.* significa che il provider dell'email considera un messaggio in uscita come dannoso.
* *571 5.7.1 Il messaggio contiene spam o virus o il mittente è bloccato ...* significa che il server email ha considerato un messaggio in uscita come spam. Questo probabilmente significa che i filtri antispam del server email sono troppo rigorosi. Dovrai contattare il provider email per supporto a riguardo.
* *451 4.7.0 Errore temporaneo del server. Sei pregato di riprovare più tardi. PRX4 ...*: per favore [vedi qui](https://www.limilabs.com/blog/office365-temporary-server-error-please-try-again-later-prx4) o [vedi qui](https://judeperera.wordpress.com/2019/10/11/fixing-451-4-7-0-temporary-server-error-please-try-again-later-prx4/).
* *571 5.7.1 Accesso al relè negato*: sei pregato di ricontrollare il nome utente e l'indirizzo email nelle impostazioni avanzate di identità (tramite la configurazione manuale).

Se vuoi usare il server SMTP di Gmail per correggere un filtro anti-spam in uscita troppo rigido o migliorare la consegna dei messaggi:

* Verifica [qui](https://mail.google.com/mail/u/0/#settings/accounts) il tuo indirizzo email (dovrai usare un browser desktop per questo)
* Cambia le impostazioni di identità come segue (Impostazioni, tocca Configurazione manuale, tocca Identità, tocca l'identità):

&emsp;&emsp;Nome Utente: *il tuo indirizzo Gmail*<br /> &emsp;&emsp;Password: *[una password dell'app](#user-content-faq6)*<br /> &emsp;&emsp;Host: *smtp.gmail.com*<br /> &emsp;&emsp;Porta: *465*<br /> &emsp;&emsp;Crittografia: *SSL/TLS*<br /> &emsp;&emsp;Rispondi all'indirizzo: *il tuo indirizzo email* (impostazioni avanzate d'identità)<br />

<br />

**Errori di Gmail**

L'autorizzazione della configurazione dei profili di Gmail con la procedura guidata deve esser aggiornata periodicamente tramite il [gestore profili di Android](https://developer.android.com/reference/android/accounts/AccountManager). Questo richiede le autorizzazioni di contatto/profilo e la connettività a internet.

Nel caso di errori è possibile autorizzare/ripristinare un profilo di Gmail tramite la procedura guidata di configurazione rapida di Gmail.

L'errore *... Autenticazione fallita ... Profilo non trovato ...* significa che un profilo precedentemente autorizzato di Gmail è stato rimosso dal dispositivo.

Gli errori *... Autenticazione fallita ... Nessun token ...* significano che il gestore del profilo Android non è riuscito a ricaricare l'autorizzazione di un profilo di Gmail.

L'errore *... Autenticazione fallita ... errore di rete ...* significa che il gestore del profilo di Android non è riuscito a ricaricare l'autorizzazione di un profilo Gmail a causa di problemi con la connessione a internet

L'errore *... Autenticazione fallita ... Credenziali non valide ...* potrebbe esser causato dalla modifica della password del profilo o dalla revoca delle autorizzazioni necessarie del profilo/dei contatti. Nel caso in cui la password del profilo sia stata cambiata, dovrai autenticare di nuovo il profilo di Google nelle impostazioni del profilo Android. Nel caso in cui i permessi siano stati revocati, puoi avviare la procedura guidata di configurazione rapida di Gmail per concedere di nuovo le autorizzazioni necessarie (non devi riconfigurare il profilo).

L'errore *... ServiceDisabled ...* potrebbe esser causato dall'iscrizione al [Programma di Protezione Avanzata](https://landing.google.com/advancedprotection/): "*Per leggere le tue email, puoi (devi) usare Gmail - Non potrai usare il tuo Profilo di Google con alcune (tutte) le app e i servizi che richiedono accesso ai dati sensibili come le tue email*", vedi [qui](https://support.google.com/accounts/answer/7519408?hl=en&ref_topic=9264881).

Quando in dubbio, puoi richiedere [supporto](#user-content-support).

<br />

<a name="faq23"></a>
**(23) Perché ricevo l'avviso...?**

*Generale*

Gli avvisi sono messaggi di notifica inviati dai server email.

*Troppe connessioni simultanee* o *Numero massimo di connessioni superato*

Questo avviso sarà inviato quando ci sono troppe connessioni della cartella per lo stesso profilo email in contemporanea.

Sono cause possibili:

* Ci sono più client email connessi allo stesso profilo
* Lo stesso client email è connesso diverse volte allo stesso profilo
* Le connessioni precedenti sono state terminate bruscamente, ad esempio, perdendo bruscamente la connettività a internet

Prima prova ad attendere un po' di tempo per vedere se il problema si risolve da solo, altrimenti:

* passa periodicamente al controllo dei messaggi nelle impostazioni di ricezione, il che risulterà nell'apertura delle cartelle una per volta, o
* imposta alcune cartelle per sondare invece che sincronizzare (tienile premute nell'elenco delle cartelle, modifica le proprietà)

Un modo facile per configurare periodicamente la verifica dei messaggi per tutte le cartelle tranne la posta in arrivo è usare *Applica a tutti ...* nel menu a tre puntini dell'elenco delle cartelle e spuntare le ultime due caselle avanzate.

Il numero massimo di connessioni simultanee delle cartelle per Gmail è 15, quindi puoi sincronizzare massimo 15 cartelle simultaneamente su *tutti* i tuoi dispositivi in contemporanea. Per questo motivo le cartelle dell'*utente* di Gmail sono impostate per sondare di default invece che sincronizzarsi sempre. Quando necessario o desiderato, puoi cambiare ciò tenendo premuta una cartella nell'elenco delle cartelle e selezionando *Modifica proprietà*. Vedi [qui](https://support.google.com/mail/answer/7126229) per i dettagli.

Usando un server di Dovecot, potresti voler modificare l'impostazione [mail_max_userip_connections](https://doc.dovecot.org/settings/dovecot_core_settings/#mail-max-userip-connections).

Nota che il server email impiegherà un po' per scoprire le connessioni corrotte, per esempio a causa dell'uscita dal raggio di una rete, il che significa che sono disponibili efficientemente solo metà delle connessioni delle cartelle. Per Gmail queste sarebbero solo 7 connessioni.

<br />

<a name="faq24"></a>
**(24) Cos'è sfoglia i messaggi sul server?**

Sfogliare i messaggi sul server recupererà i messaggi dal server email in tempo reale raggiungendo la fine dell'elenco dei messaggi sincronizzati, anche quando la cartella è impostata per non sincronizzarsi. Puoi disabilitare questa funzionalità nelle impostazioni avanzate del profilo.

<br />

<a name="faq25"></a>
**(25) Perché non posso selezionare/aprire/salvare un'immagine, un allegato o un file?**

Quando l'elemento di un menu per selezionare/aprire/salvare un file è disabilitato (oscurato) o quando ricevi il messaggio *Framework di accesso all'archiviazione non disponibile*, il [framework di accesso all'archiviazione](https://developer.android.com/guide/topics/providers/document-provider), un componente standard di Android, probabilmente non è presente. Questo potrebbe essere perché la tua ROM personalizzata non lo include o perché è stato rimosso attivamente (sgonfiato).

FairEmail non richiede i permessi di archiviazione, quindi questo framework è necessario per selezionare file e cartelle. Nessuna app, tranne forse i gestori dei file, mirati ad Android 4.4 KitKat o successive dovrebbero chiedere i permessi di archiviazione perché avrebbero accesso a *tutti* i file.

Il framework di accesso all'archiviazione è fornito dal pacchetto *com.android.documentsui*, visibile come app di *File* sulle versioni di Android (è notevole OxygenOS).

Puoi abilitare (di nuovo) il framework di accesso all'archiviazione con questo comando adb:

```
pm install -k --user 0 com.android.documentsui
```

In alternativa, potresti abilitare l'app *File* di nuovo usando le impostazioni dell'app di Android.

<br />

<a name="faq26"></a>
**(26) Posso aiutare a tradurre FairEmail nella mia lingua?**

Sì, puoi tradurre i testi di FairEmail nella tua lingua [su Crowdin](https://crowdin.com/project/open-source-email). La registrazione è gratuita.

Se vorresti che il tuo nome o alias fosse incluso nell'elenco dei collaboratori in *Info* sull'app, sei pregato di [contattarmi](https://contact.faircode.eu/?product=fairemailsupport).

<br />

<a name="faq27"></a>
**(27) Come posso distinguere tra immagini incorporate ed esterne?**

Immagine esterna:

![Immagine esterna](https://github.com/M66B/FairEmail/blob/master/images/baseline_image_black_48dp.png)

Immagine incorporata:

![Immagine incorporata](https://github.com/M66B/FairEmail/blob/master/images/baseline_photo_library_black_48dp.png)

Immagine corrotta:

![Immagine corrotta](https://github.com/M66B/FairEmail/blob/master/images/baseline_broken_image_black_48dp.png)

Nota che scaricare immagini esterne da un server remoto è utilizzabile per registrare che hai visualizzato un messaggio, cosa che probabilmente non vorresti se il messaggio è di spam o malevolo.

<br />

<a name="faq28"></a>
**(28) Come posso gestire le notifiche della barra di stato?**

Nelle impostazioni di notifica troverai un pulsante *Gestione notifiche* per navigare direttamente alle impostazioni di notifica di Android per FairEmail.

Su Android 8.0 Oreo e successive puoi gestire le proprietà dei singoli canali di notifica, ad esempio per impostare un suono di notifica specifico o mostrare le notifiche sul blocco schermo.

FairEmail ha i seguenti canali di notifica:

* Servizio: usato per notificare il servizio di sincronizzazione, vedi anche [questa FAQ](#user-content-faq2)
* Invio: usato per notificare del servizio di invio
* Notifiche: usato per le notifiche dei nuovi messaggi
* Avviso: usato per le notifiche d'avviso
* Errore: usato per le notifiche d'errore

Vedi [qui](https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels) i dettagli sui canali di notifica. In breve: tocca sul nome del canale di notifica per accedere alle impostazioni del canale.

Su Android prima di Android 8 Oreo puoi impostare il suono della notifica nelle impostazioni.

Vedi [questa FAQ](#user-content-faq21) se il tuo dispositivo ha un LED di notifica.

<br />

<a name="faq29"></a>
**(29) Come posso ricevere le notifiche dei nuovi messaggi per le altre cartelle?**

Basta tenere premuta una cartella, selezionare *Modifica proprietà*, e abilitare *Mostra nella casella unificata* o *Notifica nuovi messaggi* (disponibile solo su Android 7 Nougat e successive) e toccare *Salva*.

<br />

<a name="faq30"></a>
**(30) Come posso usare le impostazioni rapide fornite?**

Ci sono impostazioni rapide (pannelli delle impostazioni) disponibili per:

* abilitare/disabilitare globalmente la sincronizzazione
* mostrare il numero di nuovi messaggi è segnarli come visti (non letti)

Le impostazioni rapide richiedono Android 7.0 o successive. L'uso dei pannelli delle impostazioni è spiegato [qui](https://support.google.com/android/answer/9083864).

<br />

<a name="faq31"></a>
**(31) Come posso usare le scorciatoie fornite?**

Ci sono scorciatoie disponibili per comporre un nuovo messaggio a un contatto preferito.

Le scorciatoie richiedono Android 7.1 Nougat o successive. L'uso delle scorciatoie è spiegato [qui](https://support.google.com/android/answer/2781850).

È anche possibile creare scorciatoie alle cartelle tenendo premuta una cartella nell'elenco delle cartelle di un profilo e selezionando *Aggiungi scorciatoia*.

<br />

<a name="faq32"></a>
**(32) Come posso verificare che leggere le email sia davvero sicuro?**

Per questo puoi usare l'[Email Privacy Tester](https://www.emailprivacytester.com/) (Test della Privacy dell'Email).

<br />

<a name="faq33"></a>
**(33) Perché gli indirizzi modificati del mittente non funzionano?**

Gran parte dei provider accettano indirizzi convalidati solo inviando messaggi per prevenire lo spam.

Per esempio, Google modifica simili intestazioni dei messaggi per gli indirizzi *non verificati*:

```
Da: Qualcuno <somebody@example.org>
X-Google-Original-From: Qualcuno <somebody+extra@example.org>
```

Questo significa che l'indirizzo modificato del mittente è automaticamente stato sostituito da un indirizzo verificato prima dell'invio del messaggio.

Nota che questo è indipendente dalla ricezione dei messaggi.

<br />

<a name="faq34"></a>
**(34) Come sono abbinate le identità?**

Le identità sono abbinate, come previsto, dal profilo. Per i messaggi in arrivo gli indirizzi *a*, *cc*, *ccn*, *da* e *(X-)delivered/envelope/original-to* saranno controllati (in questo ordine) e per i messaggi in uscita (bozze, in uscita e inviati) solo gli indirizzi *da* saranno controllati. Gli indirizzi uguali hanno la precedenza su indirizzi parzialmente corrispondenti, tranne per gli indirizzi *delivered-to* (consegnato a).

L'indirizzo abbinato sarà mostrato come *tramite* nella sezione degli indirizzi dei messaggi ricevuti (tra intestazione e testo del messaggio).

Nota che le identità devono esser abilitate per poter esser abbinate e che le identità di altri profili non saranno considerate.

L'abbinamento avverrà solo una volta alla ricezione di un messaggio, quindi cambiare la configurazione non cambierà i messaggi esistenti. Però, potresti cancellare i messaggi locali tenendo premuta una cartella nell'elenco delle cartelle e sincronizzando di nuovo i messaggi.

Puoi configurare un [regex](https://en.wikipedia.org/wiki/Regular_expression) nelle impostazioni dell'identità per abbinare **il nome utente** di un indirizzo email (la parte prima del simbolo @).

Nota che il nome di dominio (le parti dopo il simbolo @), devono sempre essere uguali al nome di dominio dell'identità.

Se ti piace abbinare un indirizzo email unico, questo regex va principalmente bene:

```
.*
```

Se ti piace abbinare gli indirizzi email a scopo speciale abc@esempio.com e xyz@esempio.com, e vorresti anche avere un indirizzo email di ripiego principale@esempio.com, potresti fare qualcosa del genere:

* Identità: abc@example.com; regex: **(?i)abc**
* Identità: xyz@example.com; regex: **(?i)xyz**
* Identità: main@example.com; regex: **^(?i)((?!abc|xyz).)\*$**

Le identità abbinate sono utilizzabili per colorare i messaggi di codice. Il colore dell'identità ha la precedenza sul colore della cartella e del profilo. Impostare i colori dell'identità è una funzionalità pro.

<br />

<a name="faq35"></a>
**(35) Perché dovrei esser attento a visualizzare immagini, allegati, il messaggio originale e ad aprire i collegamenti?**

Visualizzare remotamente le immagini archiviate (vedi anche [questa FAQ](#user-content-faq27)) e aprire link potrebbe non solo dire che hai visto il messaggio, ma anche rilevare il tuo indirizzo IP. Vedi anche questa domanda: [Perché il link dell'email è più pericoloso di quello della ricerca web?](https://security.stackexchange.com/questions/241139/why-emails-link-is-more-dangerous-than-web-searchs-link).

Aprire allegati o visualizzare un messaggio originale potrebbe caricare contenuti remoti ed eseguire script, che potrebbero non solo causare la rilevazione di informazioni sensibili alla privacy, ma potrebbero anche essere un rischio di sicurezza.

Nota che i tuoi contatti potrebbero inviare inconsapevolmente messaggi malevoli se infettati da malware.

FairEmail riformatta i messaggi rendendoli diversi dall'originale, ma scoprendo anche i link di phishing.

Nota che i messaggi riformattati sono spesso più leggibili degli originali per la rimozione dei margini, e la standardizzazione dei colori del font e delle dimensioni.

L'app di Gmail mostra di default le immagini scaricandole tramite un server proxy di Google. Poiché le immagini sono scaricate dal server sorgente [in tempo reale](https://blog.filippo.io/how-the-new-gmail-image-proxy-works-and-what-this-means-for-you/), questo è persino meno sicuro poiché anche Google è coinvolto, senza fornire maggiori benefici.

Puoi mostrare di default le immagini e i messaggi originali per i mittenti fidati in base al caso spuntando *Non chiedere di nuovo di ...*.

Se vuoi ripristinare le app predefinite *Apri con*, sei pregato di [vedere qui](https://www.androidauthority.com/how-to-set-default-apps-android-clear-621269/).

<br />

<a name="faq36"></a>
**(36) Come sono crittografati i file delle impostazioni?**

Versione breve: AES 256 bit

Versione lunga:

* La chiave di 256 bit è generata con *PBKDF2WithHmacSHA1* usando un salt casuale sicuro a 128 bit e 65536 iterazioni
* La cifra è *AES/CBC/PKCS5Padding*

<br />

<a name="faq37"></a>
**(37) Come sono memorizzate le password?**

Tutte le versioni supportate di Android [crittografano tutti i dati dell'utente](https://source.android.com/security/encryption), quindi tutti i dati, inclusi i nomi utente, le password, i messaggi, etc., sono memorizzati crittografati.

Se il dispositivo è sicuro con un PIN, uno schema o una password, puoi rendere visibili le password del profilo e dell'identità. Se questo è un problema perché condividi il dispositivo con altre persone, considera di usare i [profili utente](https://www.howtogeek.com/333484/how-to-set-up-multiple-user-profiles-on-android/).

<br />

<a name="faq39"></a>
**(39) Come posso ridurre l'uso della batteria di FairEmail?**

Le versioni recenti di Android di default segnalano l'*uso dell'app* come una percentuale nella schermata delle impostazioni della batteria di Android. **In modo confusionario, l'*uso dell'app* non è uguale all'*uso della batteria* e non è nemmeno direttamente correlato all'uso della batteria!** L'uso dell'app (mentre in uso) sarà molto alto perché FairEmail usa un servizio in primo piano, considerato come un uso costante dell'app da Android. Tuttavia, questo non significa che FairEmail usa costantemente energia della batteria. Il vero uso della batteria è visibile navigando a questa schermata:

*Impostazioni di Android*, *Batteria*, menu a tre puntini dell'*Uso della batteria*, menu a tre puntini *Mostra uso completo del dispositivo*

In generale, l'uso della batteria dovrebbe essere inferiore o in ogni caso non di molto maggiore dello *Standby della rete mobile*. Se questo non è il caso, sei pregato di attivare l'*Ottimizzazione automatica* nelle impostazioni di ricezione. Se questo non aiuta, sei pregato di [chiedere supporto](https://contact.faircode.eu/?product=fairemailsupport).

È inevitabile che sincronizzare i messaggi userà energia della batteria perché richiede accesso alla rete e al database dei messaggi.

Se confronti l'uso della batteria di FairEmail con un altro client email, sei pregato di assicurarti che l'altro client email sia configurato similmente. Per esempio, confrontare la sincronizzazione continua (messaggi push) e il controllo periodico (raro) dei nuovi messaggi non è un confronto equo.

Riconnettersi a un server email userà energia extra della batteria, quindi una connessione a internet instabile risulterà in un uso extra della batteria. Inoltre, alcuni server terminano prematuramente le connessioni inattive, mentre [lo standard](https://tools.ietf.org/html/rfc2177) dice che una connessione inattiva dovrebbe esser mantenuta aperta per 29 minuti. In questi casi potresti voler sincronizzare periodicamente, per esempio ogni ora, invece che continuamente. Nota che sondare frequentemente (più che ogni 30-60 minuti) potrebbe usare maggiore energia della batteria che sincronizzando sempre, poiché connettersi al server e confrontare i messaggi locali e remoti sono operazioni espansive.

[Su alcuni dispositivi](https://dontkillmyapp.com/) è necessario *disabilitare* le ottimizzazioni della batteria (fase 3 della configurazione) per mantenere aperte le connessioni ai server email. Difatti, lasciare abilitate le ottimizzazioni della batteria può risultare in un uso extra della batteria per tutti i dispositivi, anche se sembra contraddittorio!

Gran parte dell'uso della batteria, non considerando la visualizzazione dei messaggi, dipende dalla sincronizzazione dei messaggi (ricezione e invio). Quindi, per ridurre l'uso della batteria, imposta il numero di giorni per cui sincronizzare i messaggi a un valore inferiore, specialmente se ci sono molti messaggi recenti in una cartella. Tieni premuto il nome di una cartella nell'elenco delle cartelle e seleziona *Modifica proprietà* per accedere a quest'impostazione.

Se hai connettività a internet almeno una volta al giorno, è sufficiente sincronizzare i messaggi solo per un giorno.

Nota che puoi impostare il numero di giorni per cui *mantenere* i messaggi a un numero maggiore di quello per *sincronizzare* i messaggi. Potresti ad esempio sincronizzare inizialmente i messaggi per un gran numero di giorni e, una volta completato ciò, ridurre il numero di giorni per sincronizzare i messaggi, lasciando il numero di giorni per mantenere i messaggi. Dopo aver diminuito il numero di giorni per mantenere i messaggi, potresti voler eseguire la pulizia nelle impostazioni varie per rimuovere i vecchi file.

Nelle impostazioni di ricezione puoi abilitare la sincronizzazione continua dei messaggi preferiti, che ti consentirà di mantenere in giro i più vecchi messaggi, sincronizzandoli per un numero limitato di giorni.

Disabilitare l'opzione della cartella *Scarica automaticamente i testi e gli allegati del messaggio* risulterà in un minore traffico di rete e dunque un minore uso della batteria. Potresti disabilitare quest'opzione per esempio per la cartella inviati e l'archivio.

Sincronizzare i messaggi di notte è principalmente inutile, quindi puoi risparmiare sull'uso della batteria non sincronizzando di notte. Nelle impostazioni puoi selezionare un piano per la sincronizzazione dei messaggi (questa è una funzionalità pro).

FairEmail sincronizzerà di default l'elenco delle cartelle a ogni connessione. Poiché le cartelle sono per lo più create, rinominate ed eliminate poco spesso, puoi risparmiare uso della rete e della batteria disabilitando ciò nelle impostazioni di ricezione.

FairEmail controllerà di default se i vecchi messaggi sono stati eliminati dal server a ogni connessione. Se non ti importa che i vecchi messaggi eliminati dal server siano ancora visibili in FairEmail, puoi risparmiare uso della rete e della batteria disabilitando ciò nelle impostazioni di ricezione.

Alcuni provider non seguono lo standard IMAP e non mantengono aperte abbastanza a lungo le connessioni, forzando spesso FairEmail a riconnettersi, causando uso extra della batteria. Puoi ispezionare il *Registro* tramite il menu di navigazione principale per verificare se ci sono riconnessioni frequenti (connessione chiusa/ripristinata, errore/timeout di lettura/scrittura, etc.). Puoi risolvere ciò abbassando l'intervallo di mantenimento in vita nelle impostazioni avanzate del profilo a, per esempio, 9 o 15 minuti. Nota che le ottimizzazioni della batteria devono esser disabilitate nella fase 3 della configurazione per mantenere le connessioni in modo affidabile.

Alcuni provider inviano ogni due minuti qualcosa come '*Ancora qui*' risultando in traffico di rete e nell'attivazione del tuo dispositivo, e causando un uso non necessario ed extra della batteria. Puoi ispezionare il *Registro* tramite il menu di navigazione principale per verificare se il tuo provider lo stia facendo. Se il tuo provider usa [Dovecot](https://www.dovecot.org/) come server IMAP, potresti chiedere al tuo provider di cambiare l'impostazione [imap_idle_notify_interval](https://wiki.dovecot.org/Timeouts) a un valore superiore o, meglio ancora, disabilitarla. Se il tuo provider non è capace o disposto a cambiare/disabilitare ciò, dovresti considerare di passare alla sincronizzazione periodica invece che continua. Puoi cambiarla nelle impostazioni di ricezione.

Se ricevi il messaggio *Questo provider non supporta i messaggi push* configurando un profilo, considera di passare a un provider moderno che li supporti (IMAP IDLE) per ridurre l'uso della batteria.

Se il tuo dispositivo ha uno schermo [AMOLED](https://en.wikipedia.org/wiki/AMOLED), puoi risparmiare sull'uso della batteria visualizzando i messaggi passando al tema scuro.

Se l'ottimizzazione automatica è abilitata nelle impostazioni di ricezione, un profilo sarà automaticamente cambiato per controllare periodicamente in cerca di nuovi messaggi quando il server email:

* Dice '*Ancora qui*' entro 3 minuti
* Il server email non supporta i messaggi push
* L'intervallo di mantenimento in vita è inferiore a 12 minuti

Inoltre, le cartelle del cestino e di spam saranno automaticamente impostate per cercare nuovi messaggi dopo tre errori consecutivi [troppe connessioni simultanee](#user-content-faq23).

<br />

<a name="faq40"></a>
**(40) Come posso ridurre l'uso dei dati di FairEmail?**

Puoi ridurre l'uso dei dati fondamentalmente allo stesso modo che riducendo l'uso della batteria, vedi la domanda precedente per suggerimenti.

È inevitabile che i dati saranno usati per sincronizzare i messaggi.

Se la connessione al server email è persa, FairEmail risincronizzerà sempre i messaggi per assicurarsi di non essersene persi alcuni. Se la connessione è instabile, può risultare in un uso extra dei dati. In questo caso, è una buona idea diminuire il numero di giorni di sincronizzazione dei messaggi a un minimo (vedi la domanda precedente) o passare alla sincronizzazione periodica dei messaggi (impostazioni di ricezione).

Per ridurre l'uso dei dati, potresti cambiare queste impostazioni avanzate di ricezione:

* Controlla se i vecchi messaggi sono stati rimossi dal server: disabilitato
* Sincronizzazione (condivisa) dell'elenco delle cartelle: disabilitato

Di default, FairEmail non scarica testi e allegati dei messaggi più grandi di 256 KiB quando sotto una connessione a internet misurata (mobile o Wi-Fi a pagamento). Puoi cambiare ciò nelle impostazioni di connessione.

<br />

<a name="faq41"></a>
**(41) Come posso correggere l'errore 'Handshake fallito' ?**

Ci sono diverse cause possibili, quindi sei pregato di leggere fino alla fine di questa risposta.

L'errore '*Handshake fallito ... WRONG_VERSION_NUMBER ...*' potrebbe significare che stai provando a connetterti a un server IMAP o SMTP senza una connessione crittografata, tipicamente usando la porta 143 (IMAP) e la porta 25 (SMT) o che sia in uso un protocollo errato (SSL/TLS o STARTTLS).

Gran parte dei provider forniscono connessioni crittografate usando diverse porte, tipicamente la porta 993 (IMAP) e la porta 465/587 (SMTP).

Se il tuo provider non supporta le connessioni crittografate, dovresti chiedere di renderlo possibile. Se non è possibile, potresti abilitare *Consenti connessioni non sicure* sia nelle impostazioni avanzate CHE nelle impostazioni del profilo/identità.

Vedi anche [questa FAQ](#user-content-faq4).

L'errore '*Handshake fallito ... SSLV3_ALERT_ILLEGAL_PARAMETER ...*' è causato da un bug nell'implementazione del protocollo SSL o da una chiave DH troppo breve sul server dell'email e sfortunatamente non è risolvibile in FairEmail.

L'errore '*Handshake fallito ... HANDSHAKE_FAILURE_ON_CLIENT_HELLO ...*' potrebbe esser causato dal provider che usa ancora RC4, non più supportato da [Android 7](https://developer.android.com/about/versions/nougat/android-7.0-changes.html#tls-ssl).

L'errore '*Handshake fallito ... UNSUPPORTED_PROTOCOL or TLSV1_ALERT_PROTOCOL_VERSION ...*' potrebbe esser causato dall'abilitazione di connessioni indurite nelle impostazioni di connessione o da Android che non supporta più i protocolli più vecchi, come SSLv3.

Android 8 Oreo e successive [non supportano](https://developer.android.com/about/versions/oreo/android-8.0-changes#security-all) più SSLv3. Non c'è modo di risolvere la mancanza al supporto di RC4 e SSLv3 perché è stato completamente rimosso da Android (che dovrebbe dire qualcosa).

Puoi usare [questo sito web](https://ssl-tools.net/mailservers) o [questo sito web](https://www.immuniweb.com/ssl/) per cercare i problemi di SSL/TLS dei server email.

<br />

<a name="faq42"></a>
**(42) Puoi aggiungere un nuovo fornitore all'elenco dei fornitori?**

Se il provider è usato da più che poche persone, sì, con piacere.

Sono necessarie le seguenti informazioni:

```
<provider
    name="Gmail"
    link="https://support.google.com/mail/answer/7126229" // link to the instructions of the provider
    type="com.google"> // questo non è necessario
    <imap
        host="imap.gmail.com"
        port="993"
        starttls="false" />
    <smtp
        host="smtp.gmail.com"
        port="465"
        starttls="false" />
</provider>
```

L'EFF [scrive](https://www.eff.org/nl/deeplinks/2018/06/announcing-starttls-everywhere-securing-hop-hop-email-delivery): "*Inoltre, anche se configuri perfettamente STARTTLS e usi un certificato valido, non c'è ancora alcuna garanzia che la tua comunicazione sarà crittografata.*"

Quindi, le connessioni SSL pure sono più sicure che usare [STARTTLS](https://en.wikipedia.org/wiki/Opportunistic_TLS) e dunque preferite.

Sei pregato di assicurarti che la ricezione e l'invio dei messaggi funzionino bene prima di contattarmi per aggiungere un provider.

Vedi sotto come contattarmi.

<br />

<a name="faq43"></a>
**(43) Puoi mostrare l'originale di...?**

Mostra l'originale, mostra il messaggio originale come inviato dal mittente, inclusi i font, colori, margini, etc. originali. Fair Email non altera e mai li altererà in alcun modo, tranne che per richiedere [TEXT_AUTOSIZING](https://developer.android.com/reference/android/webkit/WebSettings.LayoutAlgorithm), che *tenterà* di rendere più leggibili i piccoli testi.

<br />

<a name="faq44"></a>
**~~(44) Puoi mostrare foto / identicon nella cartella inviati?~~**

~~Le foto di contatto e gli identicon sono sempre mostrati per il mittente perché necessari per i thread di conversazione.~~ ~~Ottenere le foto di contatto sia del mittente che del destinatario non è propriamente un'opzione perché ottenere la foto di contatto è un'operazione costosa.~~

<br />

<a name="faq45"></a>
**(45) Come posso risolvere 'Questa chiave non è disponibile. Per usarla, devi importarla come una delle tue!' ?**

Riceverai il messaggio *Questa chiave non è disponibile. Per usarla, devi importarla come tua!* provando a decrittografare un messaggio con una chiave pubblica. Per correggere ciò dovrai importare la chiave privata.

<br />

<a name="faq46"></a>
**(46) Perché l'elenco dei messaggi continua a ricaricarsi?**

Se vedi un 'caricamento' sull'elenco dei messaggi, la cartella è ancora in sincronizzazione con il server remoto. Puoi vedere il progresso della sincronizzazione nell'elenco delle cartelle. Vedi la legenda su cosa significano le icone e i numeri.

La velocità del tuo dispositivo e della connessione a internet e il numero di giorni per sincronizzare i messaggi determinano quanto impiegherà la tua sincronizzazione. Nota che non dovresti impostare il numero di giorni per sincronizzare i messaggi a più di un giorno in gran parte dei casi, vedi anche [questa FAQ](#user-content-faq39).

<br />

<a name="faq47"></a>
**(47) Come risolvo l'errore 'Nessun profilo principale o cartella delle bozze' ?**

Otterrai l'errore *Nessun profilo principale o cartella delle bozze* provando a comporre un messaggio mentre non è impostato alcun profilo come principale o quando non c'è alcuna cartella delle bozze selezionata per il profilo principale. Questo può succedere ad esempio avviando FairEmail per comporre un messaggio da un'altra app. FairEmail deve sapere dove memorizzare la bozza, quindi dovrai selezionare un profilo affinché sia quello principale e/o dovrai selezionare una cartella di bozze per il profilo principale.

Questo può anche verificarsi provando a rispondere a un messaggio o inoltrare un messaggio da un profilo privo della cartella delle bozze, mentre non esiste alcun profilo principale o quando questo non ha una cartella delle bozze.

Sei pregato di vedere [questa FAQ](#user-content-faq141) per ulteriori informazioni.

<br />

<a name="faq48"></a>
**~~(48) Come risolvo l'errore 'Nessun profilo principale o cartella d'archivio' ?~~**

~~Otterrai il messaggio d'errore *Nessun profilo principale o cartella d'archivio* cercando messaggi da un'altra app. FairEmail deve sapere dove cercare, quindi dovrai selezionare un profilo che sia il principale e/o dovrai selezionare una cartella d'archivio per il profilo principale.~~

<br />

<a name="faq49"></a>
**(49) Come risolvo 'Un'app obsoleta ha inviato un percorso del file invece di un flusso di file' ?**

Potresti aver selezionato o inviato un allegato o immagine con un gestore del file obsoleto o un'app obsoleta che presuma che tutte le app abbiano permessi di archiviazione. Per motivi di sicurezza e privacy le app moderne come FairEmail non hanno più l'accesso completo a tutti i file. Questo può risultare nel messaggio di errore *Un app obsoleta ha inviato un percorso del file invece di un flusso di file* se un nome del file invece che un flusso del file è condiviso con FairEmail perché FairEmail non può aprire casualmente i file.

Puoi correggerlo passando a un gestore dei file aggiornato o un'app progettata per le versioni recenti di Android. In alternativa, puoi garantire l'accesso di lettura a FairEmail allo spazio di archiviazione sul tuo dispositivo nelle impostazioni delle app di Android. Nota che questa soluzione [non funzionerà più su Android Q](https://developer.android.com/preview/privacy/scoped-storage).

Vedi anche la [domanda 25](#user-content-faq25) e [cosa scrive Google a riguardo](https://developer.android.com/training/secure-file-sharing/share-file#RespondToRequest).

<br />

<a name="faq50"></a>
**(50) Puoi aggiungere un'opzione per sincronizzare tutti i messaggi?**

Puoi sincronizzare più messaggi o persino tutti tenendo premuta una cartella (in arrivo) nell'elenco delle cartelle di un profilo (tocca sul nome del profilo nel menu di navigazione) e selezionando *Sincronizzazione di più* nel menu del popup.

<br />

<a name="faq51"></a>
**(51) Come sono ordinate le cartelle?**

Le cartelle sono prima ordinate sull'ordine del profilo (di default sul nome del profilo) ed entro un profilo con cartelle speciali di sistema in alto, seguite da cartelle impostate per sincronizzare. Entro ogni categoria le cartelle sono ordinate per nome (su schermo). Puoi impostare il nome mostrato tenendo premuto una cartella nell'elenco delle cartelle e selezionando *Modifica proprietà*.

L'elemento del menu di navigazione (hamburger) *Ordina cartelle* nelle impostazioni è utilizzabile per ordinare manualmente le cartelle.

<br />

<a name="faq52"></a>
**(52) Perché ci vuole del tempo per riconnettersi a un profilo?**

Non c'è modo affidabile per sapere se la connessione di un profilo è stata terminata con grazia o con forza. Provare a riconnettersi a un profilo mentre viene terminata forzatamente la connessione del profilo troppo spesso può risultare in problemi come [troppe connessioni simultanee](#user-content-faq23) o persino il blocco del profilo. Per prevenire tali problemi, FairEmail attende 90 secondi fino a provare a riconnettersi.

Puoi tenere premute le *Impostazioni* nel menu di navigazione per riconnettersi immediatamente.

<br />

<a name="faq53"></a>
**(53) Puoi attaccare la barra d'azione del messaggio in cima/in fondo?**

La barra d'azione del messaggio funziona su un solo messaggio e la barra d'azione inferiore funziona su tutti i messaggi nella conversazione. Poiché spesso c'è più di un messaggio in una conversazione, questo è impossibile. Inoltre, ci sono abbastanza azioni specifiche del messaggio, come l'inoltro.

Spostare la barra d'azione del messaggio sotto al messaggio è visualmente non attraente perché c'è già una barra d'azione della conversazione in fondo alla schermata.

Nota che non ci sono molte app di email, se presenti, che mostrano una conversazione come un elenco di messaggi espandibili. Questo ha molti vantaggi, ma causa anche il bisogno di azioni specifiche del messaggio.

<br />

<a name="faq54"></a>
**~~(54) Come uso un prefisso dello spazio del nome?~~**

~~Il prefisso di uno spazio del nome è usato per rimuovere automaticamente il prefisso che i provider talvolta aggiungono ai nomi della cartella.~~

~~Per esempio la cartella di spam di Gmail è chiamata:~~

```
[Gmail]/Spam
```

~~Impostando il prefisso dello spazio del nome in *[Gmail]*, FairEmail rimuoverà automaticamente *[Gmail]/* da tutti i nomi delle cartelle.~~

<br />

<a name="faq55"></a>
**(55) Come posso segnare tutti i messaggi come letti / spostarli o eliminarli?**

Puoi usare la selezione multipla per questo. Tieni premuto il primo messaggio, non sollevare il dito e scorri in già all'ultimo messaggio. Poi usa il pulsante d'azione a tre puntini per eseguire l'azione desiderata.

<br />

<a name="faq56"></a>
**(56) Puoi aggiungere il supporto per JMAP?**

Quasi nessun provider offre il protocollo [JMAP](https://jmap.io/), quindi non vale la pena di aggiungere supporto per questo a FairEmail.

<br />

<a name="faq57"></a>
**(57) Posso usare HTML nelle firme?**

Sì, puoi usare [HTML](https://en.wikipedia.org/wiki/HTML). Nell'editor della firma puoi passare alla modalità HTML tramite il menu a tre puntini.

Nota che se torni all'editor di testo non tutto lo HTML potrebbe esser renderizzato così com'è perché l'editor di testo di Android non è capace di renderizzarlo tutto. Similmente, se usi l'editor di testo, HTML potrebbe esser alterato in modi imprevisti.

Se vuoi usare il testo preformattato, come l'[arte ASCII](https://en.wikipedia.org/wiki/ASCII_art), dovresti avvolgere il testo in un elemento *pre*, come questo:

```
<pre>
  |\_/|
 / @ @ \
( > º < )
 `>>x<<´
 /  O  \
 </pre>
```

<br />

<a name="faq58"></a>
**(58) Cosa significa l'icona di un'email aperta/chiusa?**

L'icona dell'email nell'elenco delle cartelle può essere aperta (delineata) o chiusa (solida):

![Immagine esterna](https://github.com/M66B/FairEmail/blob/master/images/baseline_mail_outline_black_48dp.png)

I corpi e gli allegati dei messaggi non sono scaricati di default.

![Immagine esterna](https://github.com/M66B/FairEmail/blob/master/images/baseline_email_black_48dp.png)

I corpi e gli allegati dei messaggi sono scaricati di default.

<br />

<a name="faq59"></a>
**(59) I messaggi originali sono apribili nel browser?**

Per motivi di sicurezza i file con i testi del messaggio originale non sono accessibili alle altre app, quindi questo è impossibile. In teoria, il [Framework d'Accesso all'Archiviazione](https://developer.android.com/guide/topics/providers/document-provider) sarebbe utilizzabile per condividere questi file, ma nemmeno Google Chrome può gestirlo.

<br />

<a name="faq60"></a>
**(60) Sapevi...?**

* Sapevi che i messaggi stellati possono sono sempre sincronizzabili/mantenibili? (abilitabile nelle impostazioni di ricezione)
* Sapevi che puoi tenere premuta l'icona 'scrivi messaggio' per andare alla cartella delle bozze?
* Sapevi che esiste un'opzione avanzata per contrassegnare i messaggi come letti quando sono spostati? (archiviare e cestinare sono anch'essi spostare)
* Sapevi che puoi selezionare il testo (o un indirizzo email) in ogni app sulle versioni recenti di Android e farli cercare a FairEmail?
* Sapevi che FairEmail ha una modalità tablet? Ruota il tuo dispositivo in orizzontale e le conversazioni saranno aperte in una seconda colonna se c'è abbastanza spazio su schermo.
* Sapevi che puoi tenere premuto un modello di risposta per creare un messaggio di bozza dal modello?
* Sapevi che puoi tenere premuto, trattenere e scorrere per selezionare un intervallo di messaggi?
* Sapevi che puoi riprovare a inviare i messaggi usando lo scorrimento in giù per ricaricare nella posta in uscita?
* Sapevi che puoi scorrere una conversazione a sinistra o destra per andare alla conversazione successiva o precedente?
* Sapevi che puoi toccare su un'immagine per vedere da dove sarà scaricata?
* Sapevi che puoi tener premuta l'icona della cartella nella barra d'azione per selezionare un profilo?
* Sapevi che puoi tener premuta l'icona della stella in una conversazione per impostarne una colorata?
* Sapevi che puoi aprire il pannello di navigazione scorrendo da sinistra, anche visualizzando una conversazione?
* Sapevi che puoi tenere premuta l'icona della persona per mostrare/nascondere i campi CC/CCN e ricordare lo stato di visibilità per la volta successiva?
* Sapevi che puoi inserire gli indirizzi email di un gruppo di contatto di Android tramite il menu di trabocco a tre punti?
* Sapevi che se selezioni il testo e tocchi rispondi, solo quello selezionato sarà citato?
* Sapevi che puoi tenere premuto le icone del cestino (sia nella barra d'azione in basso che del messaggio) per eliminare permanentemente un messaggio o una conversazione? (versione 1.1368+)
* Sapevi che puoi tener premuta l'azione di invio per mostrare la finestra di invio, anche se è disabilitata?
* Sapevi che puoi tener premuta l'icona dello schermo intero per mostrare solo il testo del messaggio originale?
* Sapevi che puoi tener premuto il pulsante di risposta per rispondere al mittente? (dalla versione 1.1562)

<br />

<a name="faq61"></a>
**(61) Perché alcuni messaggi sono mostrati oscurati?**

I messaggi mostrati oscurati (ingrigiti) sono messaggi localmente spostati per cui lo spostamento non è ancora confermato dal server. Questo può succedere quando non c'è (ancora) alcuna connessione al server o il profilo. Questi messaggi saranno sincronizzati dopo una connessione al server e il profilo è stato creato o, se non si verifica mai, sarà eliminato se troppo vecchi da sincronizzare.

Potresti dover sincronizzare manualmente la cartella, per esempio tirando in giù.

Puoi visualizzare questi messaggi, ma non puoi rispostarli finché lo spostamento precedente è stato confermato.

Le [operazioni](#user-content-faq3) in sospeso sono mostrate nella vista delle operazioni accessibile dal menu di navigazione principale.

<br />

<a name="faq62"></a>
**(62) Che metodi di autenticazione sono supportati?**

I metodi di autenticazione seguenti sono supportati e usati in questo ordine:

* CRAM-MD5
* ACCESSO
* SEMPLICE
* NTLM (non testato)
* XOAUTH2 ([Gmail](https://developers.google.com/gmail/imap/xoauth2-protocol), [Yandex](https://tech.yandex.com/oauth/))

I metodi di autenticazione SASL, oltre a CRAM-MD5, non sono supportati perché [JavaMail per Android](https://javaee.github.io/javamail/Android) non supporta l'autenticazione SASL.

Se il tuo provider richiede un metodo di autenticazione non supportato, potresti ottenere il messaggio *autenticazione fallita*.

I [Certificati del client](https://en.wikipedia.org/wiki/Client_certificate) sono selezionabili nelle impostazioni del profilo e dell'identità.

L'[Indicazione del Nome del Server](https://en.wikipedia.org/wiki/Server_Name_Indication) è supportata da [tutte le versioni supportate di Android](https://developer.android.com/training/articles/security-ssl).

<br />

<a name="faq63"></a>
**(63) Come sono ridimensionate le immagini per la visualizzazione sugli schermi?**

Le grandi immagini in linea o allegate in [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) e [JPEG](https://en.wikipedia.org/wiki/JPEG) saranno ridimensionate automaticamente per la visualizzazione sugli schermi. Questo perché i messaggi email hanno dimensioni limitate, in base al provider, prevalentemente tra 10 e 50 MB. Le immagini saranno ridimensionate di default a una larghezza e altezza di circa 1440 pixel e salvate con un rapporto di compressione del 90%. Le immagini sono ridimensionate usando fattori interi per ridurre l'uso della memoria e mantenere la qualità dell'immagine. Il ridimensionamento automatico delle immagini in linea e/o allegate e la dimensione d'arrivo massima dell'immagine sono configurabili nelle impostazioni di invio.

Se vuoi ridimensionare le immagini in base al caso, puoi usare [Send Reduced](https://f-droid.org/en/packages/mobi.omegacentauri.SendReduced/) o app simili.

<br />

<a name="faq64"></a>
**~~(64) Puoi aggiungere azioni personalizzate per scorrere a sinistra/destra?~~**

~~La cosa più naturale da fare scorrendo a sinistra o a destra in un elenco è rimuovere la voce dall'elenco.~~ ~~L'azione più naturale nel contesto di un'app di email è spostare il messaggio dalla cartella a un'altra.~~ ~~Puoi selezionare la cartella da spostare nelle impostazioni del profilo.~~

~~Altre azioni, come contrassegnare i messaggi come letti e posticiparli sono disponibili tramite la selezione multipla.~~ ~~Puoi tenere premuto un messaggio per avviare la selezione multipla. Vedi anche [questa domanda](#user-content-faq55).~~

~~Scorrere a sinistra o destra per contrassegnare un messaggio come letto o non letto non è naturale perché il messaggio se ne va e poi torna in una forma differente. ~~Nota che esiste un'opzione avanzata per contrassegnare automaticamente i messaggi come letti allo spostamento.~~ ~~che in molti casi è una perfetta sostituzione per la sequenza di segna come letto e sposta a qualche cartella.~~ ~~Puoi anche contrassegnare i messaggi come letti dalle notifiche dei nuovi messaggi.~~

~~Se vuoi leggere più tardi un messaggio, puoi nasconderlo fino a un orario specifico usando il menu *posticipa*.~~

<br />

<a name="faq65"></a>
**(65) Perché alcuni allegati sono mostrati oscurati?**

Gli allegati in linea (immagine) sono mostrati oscurati. Gli [allegati in linea](https://tools.ietf.org/html/rfc2183) dovrebbero esser scaricati e mostrati automaticamente, ma poiché non sempre FairEmail li scarica automaticamente, vedi anche [questa FAQ](#user-content-faq40), FairEmail mostra tutti i tipi di allegati. Per distinguere gli allegati in linea e regolari, i primi sono mostrati oscurati.

<br />

<a name="faq66"></a>
**(66) FairEmail è disponibile nella Libreria di Famiglia di Google Play?**

"*Non puoi condividere gli acquisti in app e le app gratuite con i membri della tua famiglia.*"

Vedi [qui](https://support.google.com/googleone/answer/7007852) sotto "*Vedi se il contenuto è idoneo all'aggiunta alla Libreria di Famiglia*", "*App e giochi*".

<br />

<a name="faq67"></a>
**(67) Come posso posticipare le conversazioni?**

Seleziona più di una conversazione (tieni premuto per avviare la selezione multipla), tocca il pulsante a tre punti e seleziona *Posticipa...*. Altrimenti, nella vista espansa del messaggio usa *Posticipa...* nel menu 'altro' a tre puntini o l'azione del timelapse nella barra d'azione in basso. Seleziona l'orario a cui dovrebbe esser posticipata la conversazione e conferma toccando OK. Le conversazioni saranno nascoste per il tempo selezionato e mostrate di nuovo in seguito. Riceverai la notifica di un nuovo messaggio come promemoria.

Puoi anche posticipare i messaggi con [una regola](#user-content-faq71), che inoltre ti consentirà di spostare i messaggi in una cartella per posticiparli in automatico.

Puoi mostrare i messaggi posticipati deselezionando *Filtra* > *Nascosti* nel menu di trabocco a tre puntini.

Puoi toccare sulla piccola icona di posticipazione finché una conversazione non è posticipata.

Selezionando una durata di posticipazione di zero, puoi annullarla.

Le app di terze parti non hanno accesso alla cartella dei messaggi posticipati di Gmail.

<br />

<a name="faq68"></a>
**~~(68) Perché Adobe Acrobat reader non apre gli allegati PDF / le app di Microsoft non aprono i documenti allegati?~~**

~~Adobe Acrobat Reader e le app di Microsoft prevedono ancora l'accesso completo a tutti i file archiviati,~~ ~~mentre le app dovrebbero usare il [Framework di Accesso all'Archiviazione](https://developer.android.com/guide/topics/providers/document-provider) da Android KitKat (2013)~~ ~~per avere accesso ai soli file condivisi attivamente. Questo è per motivi di sicurezza e privacy.~~

~~Puoi risolverlo salvando l'allegato e aprendolo dall'app di Adobe Acrobat Reader / Microsoft,~~ ~~ma si consiglia di installare un visualizzatore di documenti / PDF reader aggiornato e preferibilmente open-source,~~ ~~per esempio quelle elencate [qui](https://github.com/offa/android-foss#-document--pdf-viewer).~~

<br />

<a name="faq69"></a>
**(69) Puoi aggiungere lo scorrimento automatico a un nuovo messaggio?**

L'elenco dei messaggi viene scorso in su automaticamente navigando dalla notifica di un messaggio nuovo o dopo un aggiornamento manuale. Scorrere sempre in su automaticamente all'arrivo dei nuovi messaggi interferirebbe con il tuo scorrimento, ma se vuoi puoi abilitarlo nelle impostazioni.

<br />

<a name="faq70"></a>
**(70) Quando saranno automaticamente espansi i messaggi?**

Quando la navigazione al messaggio di una conversazione sarà espansa se:

* C'è solo un messaggio nella conversazione
* C'è esattamente un messaggio non letto nella conversazione
* C'è esattamente un messaggio stellato (preferito) nella conversazione (dalla versione 1.1508)

Esiste un'eccezione: il messaggio non è ancora stato scaricato ed è troppo grande per il download automatico su una connessione misurata (mobile). Puoi impostare o disabilitare la dimensione massima del messaggio sulla scheda delle impostazioni di 'connessione'.

I messaggi duplicati (archiviati), cestinati e le bozze non sono contate.

I messaggi saranno automaticamente contrassegnati come letti all'espansione, a meno che non sia disabilitato nelle impostazioni del singolo profilo.

<br />

<a name="faq71"></a>
**(71) Come uso le regole del filtro?**

Puoi modificare le regole del filtro premendo a lungo una cartella nell'elenco delle cartelle di un profilo (tocca il nome del profilo nel menu laterale/di navigazione).

Le nuove regole saranno applicate ai nuovi messaggi ricevuti nella cartella, non ai messaggi esistenti. Puoi controllare la regole a applicarla ai messaggi esistenti, o in alternativa, premendo a lungo la regola nell'elenco della regola e selezionare *Esegui ora*.

Dovrai dare un nome alla regola e dovrai definire l'ordine in cui una regola dovrebbe essere eseguita in relazione ad altre regole.

Puoi disabilitare una regola e puoi interrompere l'elaborazione delle altre dopo l'esecuzione di una.

Le seguenti condizioni della regola sono disponibili:

* Il mittente contiene o il mittente è il contatto
* Il destinatario contiene
* L'oggetto contiene
* Ha allegati (opzionale di tipo specifico)
* L'intestazione contiene
* Tempo assoluto (ricevuto) tra (dalla versione 1.1540)
* Tempo relativo (ricevuto) tra

Tutte le condizioni di una regola devono essere vere per l'esecuzione dell'azione della regola. Tutte le condizioni sono opzionali, ma ci deve essere almeno una condizione, per prevenire l'abbinamento di tutti i messaggi. Se vuoi abbinare tutti i mittenti o tutti i destinatari, puoi semplicemente usare il carattere @ come condizione perché tutti gli indirizzi email lo conterranno. Se vuoi abbinare un nome di dominio, puoi usare come una condizione qualcosa del tipo *@example.org*

Nota che gli indirizzi email sono formattati come segue:

`
"Qualcuno" <somebody@example.org>`

Puoi usare più regole, possibilmente con *interrompi elaborazione*, per una condizione *o* o *non*.

L'abbinamento non dipende dalle maiuscole, a meno che tu non usi le [espressioni regolari](https://en.wikipedia.org/wiki/Regular_expression). Sei pregato di vedere [qui](https://developer.android.com/reference/java/util/regex/Pattern) la documentazione delle espressioni regolari di Java. Puoi testare [qui](https://regexr.com/) una regex.

Nota che un'espressione regolare supporti un operatore *o*, quindi se vuoi abbinare più mittenti, puoi fare questo:

`
.*alice@example\.org.*|.*bob@example\.org.*|.*carol@example\.org.*`

Nota che la [modalità dot all](https://developer.android.com/reference/java/util/regex/Pattern#DOTALL) è abilitata per poter abbinare le [intestazioni senza cartelle](https://tools.ietf.org/html/rfc2822#section-3.2.3).

Puoi selezionare una di queste azioni da applicare ai messaggi corrispondenti:

* Nessun'azione (utile per *non*)
* Segna come letto
* Segna come non letto
* Nascondi
* Sopprimi notifica
* Posticipa
* Aggiungi stella
* Imposta l'importanza (priorità locale)
* Aggiungi parola chiave
* Sposta
* Copia (Gmail: etichetta)
* Rispondi/inoltra (con modello)
* Sintesi vocale (mittente e oggetto)
* Automazione (Tasker, etc.)

Un errore nella condizione di una regola può condurre a un disastro, dunque le azioni irreversibili non sono supportate.

Le regole si applicano direttamente dopo il recupero dell'intestazione del messaggio, ma prima dello scaricamento del testo del messaggio, quindi è impossibile applicare le condizioni al testo del messaggio. Nota che i testi di messaggi grandi sono scaricati su richiesta su una connessione misurata per risparmiare sull'uso dei dati.

Se vuoi inoltrare un messaggio, considera invece di usare l'azione di spostamento. Questa sarà più affidabile dell'inoltro anche perché i messaggi inoltrati potrebbero essere considerati come spam.

Poiché le intestazioni del messaggio non sono scaricate e archiviate di default per risparmiare sull'uso della batteria e dei dati e per risparmiare spazio di archiviazione, è impossibile vedere in anteprima quali messaggi corrisponderebbero alla condizione di una regola dell'intestazione.

Alcune condizioni dell'intestazione comuni (regex):

* *.&ast;Auto-Submitted:.&ast;* [RFC3834](https://tools.ietf.org/html/rfc3834)
* *.&ast;Content-Type: multiparte/segnala.&ast;* [RFC3462](https://tools.ietf.org/html/rfc3462)

Nel menu dei messaggi *altro* a tre puntini, c'è un elemento per creare una regola per un messaggio ricevuto con le condizioni più comuni compilate.

Il protocollo POP3 non supporta l'impostazione delle parole chiave e lo spostamento o copia dei messaggi.

Usare le regole è una funzionalità pro.

<br />

<a name="faq72"></a>
**(72) Cosa sono i profili/le identità principali?**

Il profilo principale è usato quando il profilo è ambiguo, ad esempio avviando una nuova bozza dalla casella in arrivo unificata.

Similmente, l'identità principale di un profilo è usata quando l'identità è ambigua.

Può esistere solo un profilo principale e può esserci solo un'identità principale per profilo.

<br />

<a name="faq73"></a>
**(73) Spostare messaggi tra profili è sicuro/efficiente?**

Spostare i messaggi tra profili è sicuro perché i messaggi originali e non elaborati saranno scaricati e spostati e perché i messaggi sorgente saranno eliminati solo dopo che i messaggi di destinazione sono stati aggiunti

Lo spostamento della partita di messaggi tra i profili è efficiente se sia la cartella sorgente che di arrivo sono impostate per la sincronizzazione, altrimenti FairEmail deve connettersi alle cartelle per ogni messaggio.

<br />

<a name="faq74"></a>
**(74) Perché vedo i messaggi duplicati?**

Alcuni provider, in particolare Gmail, elencano tutti i messaggi in tutte le cartelle, tranne quelli cestinati, anche nella cartella dell'archivio (tutti i messaggi). FairEmail mostra tutti questi messaggi in un modo non invadente per indicare che questi sono difatti gli stessi.

Gmail consente a un messaggio di avere più etichette, presentate come cartelle in FairEmail. Ciò significa anche che i messaggi con più etichette saranno mostrati più volte.

<br />

<a name="faq75"></a>
**(75) Puoi fare una versione iOS, Windows, Linux, etc.?**

Sono necessarie molte conoscenze ed esperienza per sviluppare correttamente un'app per una piattaforma specifica, per cui sviluppo app solo per Android.

<br />

<a name="faq76"></a>
**(76) Cosa fa 'Elimina messaggi locali'?**

Il menu delle cartelle *Elimina messaggi locali* rimuove i messaggi dal dispositivo, presenti anche sul server. Non elimina i messaggi dal server. Questo può esser utile dopo aver cambiato le impostazioni della cartella per non scaricare il contenuto del messaggio (testo e allegati), ad esempio per risparmiare spazio.

<br />

<a name="faq77"></a>
**(77) Perché a volte i messaggi sono mostrati con un lieve ritardo?**

In base alla velocità del tuo dispositivo (velocità del processore e forse ancora di più la velocità della memoria), i messaggi potrebbero esser mostrati con un lieve ritardo. FairEmail è progettata per gestire dinamicamente un gran numero di messaggi senza esaurire la memoria. Questo significa che i messaggi devono esser letti da un database e che, questo, deve esser controllato per le modifiche, che potrebbero entrambe causare lievi ritardi.

Alcune funzionalità di convenzione, come il raggruppamento dei messaggi per mostrare i thread di conversazione e determinare il messaggio precedente/successivo, impiegano del tempo extra. Nota che non esiste *il* messaggio successivo perché nel mentre potrebbe esser arrivato un nuovo messaggio.

Quando si confronta la velocità di FairEmail con app simili, questo dovrebbe far parte del confronto. È facile scrivere un'app simile e più veloce che mostri solo un elenco lineare di messaggi, possibilmente usando poca memoria, ma non è così facile gestire propriamente l'uso delle risorse e offrire funzionalità più avanzate come il threading della conversazione.

FairEmail si base sui [componenti dell'architettura di Android](https://developer.android.com/topic/libraries/architecture/) all'avanguardia, quindi c'è poco spazio per miglioramenti delle prestazioni.

<br />

<a name="faq78"></a>
**(78) Come uso le pianificazioni?**

Nelle impostazioni di ricezione puoi abilitare la pianificazione e impostare un periodo di tempo e i giorni della settimana *in cui* dovrebbero esser *ricevuti* i messaggi. Nota che un orario finale pari o precedente all'orario iniziale è considerato esser dopo 24 ore.

L'automazione, vedi sotto, è utilizzabile per pianificazioni più avanzate, come per esempio i periodi giornalieri multipli di sincronizzazione o diversi periodi di sincronizzazione per diversi giorni.

FairEmail è installabile in diversi profili utente, ad esempio uno personale e uno di lavoro, e configurare diversamente FairEmail per ogni profilo, che è un'altra possibilità per avere piani di sincronizzazione differenti e sincronizzare una diversa serie di profili.

Si possono anche creare le [regole del filtro](#user-content-faq71) con una condizione temporale e posticipare i messaggi fino all'orario finale della condizione temporale. Così si possono *posticipare* i messaggi aziendali fino all'inizio dell'orario lavorativo. Questo significa anche che i messaggi saranno sul tuo dispositivo per quando non c'è una connessione a internet (temporaneamente).

Nota che le versioni recenti di Android consentono la sovrascrizione di DND (Non Disturbare) per canale di notifica e per app, utilizzabile per (non) silenziare notifiche (aziendali) specifiche. Sei pregato di [vedere qui](https://support.google.com/android/answer/9069335) per ulteriori informazioni.

Per ulteriori schemi complessi potresti impostare uno o più profili per la sincronizzazione manuale e inviare questo comando a FairEmail per cercare nuovi messaggi:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL
```

Per un profilo specifico:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL --es account Gmail
```

Puoi anche automatizzare l'attivazione e disattivazione dei messaggi inviando questi comandi a FairEmail:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE
```

Per abilitare/disabilitare un profilo specifico:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE --es account Gmail
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE --es account Gmail
```

Nota che disabilitare un profilo nasconderà il profilo e tutte le cartelle associate e i messaggi.

Per impostare l'intervallo di sondaggio:

```
(adb shell) adb shell am start-foreground-service -a eu.faircode.email.INTERVAL --ei minutes nnn
```

Dove *nnn* è tra 0, 15, 30, 60, 120, 240, 480, 1440. Un valore di 0 equivale ai messaggi push.

Puoi inviare automaticamente i comandi con ad esempio [Tasker](https://tasker.joaoapps.com/userguide/en/intents.html):

```
Nuova attività: Qualcosa di riconoscibile
Categoria dell'Azione: Intento Vario/Invio
Azione: eu.faircode.email.ENABLE
Target: Servizio
```

Per abilitare/disabilitare un profilo con il nome *Gmail*:

```
Extra: account:Gmail
```

I nomi del profilo sono sensibili alle maiuscole.

La pianificazione è una funzionalità pro.

<br />

<a name="faq79"></a>
**(79) Come uso la sincronizzazione su richiesta (manuale)?**

Normalmente, FairEmail mantiene una connessione ai server email configurati quando è possibile ricevere i messaggi in tempo reale. Se non vuoi, per esempio non vuoi esser disturbato o vuoi risparmiare sull'uso della batteria, basta disabilitare la ricezione nelle impostazioni di ricezione. Questo interromperà il servizio in background che si occupa della sincronizzazione automatica e rimuoverà la notifica della barra di stato associata.

Puoi anche abilitare la *Sincronizzazione manuale* nelle impostazioni avanzate del profilo se vuoi sincronizzare solo manualmente i profili specifici.

Puoi usare lo scorrimento in giù per ricaricare in un elenco di messaggi o usare il menu delle cartelle *Sincronizza ora* per sincronizzare manualmente i messaggi.

Se vuoi sincronizzare alcune o tutte le cartelle di un profilo manualmente, basta disabilitare la sincronizzazione per le cartelle (ma non del profilo).

Potresti anche voler disabilitare [naviga sul server](#user-content-faq24).

<br />

<a name="faq80"></a>
**~~(80) Come risolvo l'errore 'Impossibile caricare BODYSTRUCTURE'?~~**

~~Il messaggio d'errore *Impossibile caricare BODYSTRUCTURE* è causato da bug nel server email,~~ ~~vedi [qui](https://javaee.github.io/javamail/FAQ#imapserverbug) per ulteriori dettagli.~~

~~FairEmail prova già a risolvere questi bug, ma se fallisce dovrà chiedere supporto dal tuo provider.~~

<br />

<a name="faq81"></a>
**~~(81) Puoi rendere scuro lo sfondo del messaggio originale nel tema scuro?~~**

~~Il messaggio originale è mostrato come inviato dal mittente, colori inclusi.~~ ~~Cambiare il colore di sfondo non solo non renderà più originale la vista originale, risulterà anche in messaggi illeggibili.~~

<br />

<a name="faq82"></a>
**(82) Cos'è un'immagine di monitoraggio?**

Sei pregato di vedere [qui](https://en.wikipedia.org/wiki/Web_beacon) su cosa sia esattamente un'immagine di monitoraggio. In breve le immagini di monitoraggio tengono traccia del fatto che tu abbia aperto un messaggio.

FairEmail in gran parte dei casi riconoscerà automaticamente le immagini di monitoraggio e le sostituirà con quest'icona:

![Immagine esterna](https://github.com/M66B/FairEmail/blob/master/images/baseline_my_location_black_48dp.png)

Il riconoscimento automatico delle immagini di monitoraggio è disabilitabile nelle impostazioni della privacy.

<br />

<a name="faq84"></a>
**(84) A cosa servono i contatti locali?**

Le informazioni locali di contatto si basano su nomi e indirizzi trovati nei messaggi in entrata e in uscita.

L'uso principale dell'archiviazione locale dei contatti è offrire l'auto-completamento quando non sono concessi i permessi dei contatti a FairEmail.

Un altro uso è generare [scorciatoie](#user-content-faq31) sulle versioni recenti di Android per inviare rapidamente un messaggio alle persone frequentemente contattate. Ecco anche perché il numero di volte di contatto e l'ultimo contatto sono registrati e perché puoi contattare un preferito o escluderlo dai preferiti tenendolo premuto.

L'elenco dei contatti è ordinato per numero di volte di contatto e ultimo contatto.

Di default solo i nomi e gli indirizzi cui invii i messaggi saranno registrati. Puoi modificare ciò nelle impostazioni di invio.

<br />

<a name="faq85"></a>
**(85) Perché un'identità non è disponibile?**

Un'identità è disponibile per inviare un nuovo messaggio o rispondere o inoltrare un messaggio esistente solo se:

* l'identità è impostata per la sincronizzazione (invia messaggi)
* il profilo associato è impostato per la sincronizzazione (ricevi messaggi)
* il profilo associato ha una cartella delle bozze

FairEmail proverà a selezionare l'identità migliore in base all'indirizzo *a* del messaggio a cui si è risposto / inoltrato.

<br />

<a name="faq86"></a>
**~~(86) Cosa sono le 'funzionalità extra della privacy''?~~**

~~L'opzione avanzate *funzionalità della privacy extra* abilita:~~

* ~~Ricerca del proprietario dell'indirizzo IP di un link~~
* ~~Rilevamento e rimozione delle [immagini di tracciamento](#user-content-faq82)~~

<br />

<a name="faq87"></a>
**(87) Cosa significa 'credenziali non valide'?**

Il messaggio d'errore delle *credenziali invalide* significa che il nome utente e/o la password sono errati, per esempio perché la password è stata modificata o è scaduta, o che l'autorizzazione del profilo è scaduta.

Se la password è errata/scaduta, dovrai aggiornarla nelle impostazioni del profilo e/o dell'identità.

Se l'autorizzazione del profilo è scaduta, dovrai selezionare di nuovo il profilo. Potresti anche dover salvare di nuovo l'identità associata.

<br />

<a name="faq88"></a>
**(88) Come posso usare un profilo di Yahoo, AOL o Sky?**

Il modo preferito per configurare un profilo di Yahoo, è usando la procedura guidata di configurazione rapida che userà OAuth invece di una password e sarà dunque più sicuro (e anche più facile).

Per autorizzare un profilo di Yahoo, AOL o Sky dovrai creare una password dell'app. Per istruzioni, sei pregato di vedere qui:

* [per Yahoo](https://help.yahoo.com/kb/generate-third-party-passwords-sln15241.html)
* [per AOL](https://help.aol.com/articles/Create-and-manage-app-password)
* [per Sky](https://www.sky.com/help/articles/getting-started-with-sky-yahoo-mail) (sotto *Altre app di email*)

Sei pregato di vedere [questa FAQ](#user-content-faq111) sul supporto di OAuth.

Nota che Yahoo, AOL e Sky non supportano i messaggi push standard. L'app email di Yahoo usa un protocollo proprietario non documentato per i messaggi push.

I messaggi push richiedono [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE) e i server email di Yahoo non segnalano IDLE come una capacità:

```
Y1 CAPABILITY
* CAPABILITY IMAP4rev1 ID MOVE NAMESPACE XYMHIGHESTMODSEQ UIDPLUS LITERAL+ CHILDREN X-MSG-EXT UNSELECT OBJECTID
Y1 OK CAPABILITY completato
```

<br />

<a name="faq89"></a>
**(89) Come posso inviare messaggi di solo testo semplice?**

Di default FairEmail invia ogni messaggio sia come testo semplice che come testo HTML formattato perché quasi ogni destinatario prevede messaggi formattati di questi giorni. Se vuoi/devi inviare solo messaggi in testo semplice, puoi abilitarlo nelle opzioni di identità avanzate. Potresti voler creare una nuova identità per farlo se vuoi/devi selezionare l'invio dei messaggi di testo semplice in base al caso.

<br />

<a name="faq90"></a>
**(90) Perché alcuni testi sono collegati senza essere link?**

FairEmail collegherà automaticamente i collegamenti web non collegati (http e https) e gli indirizzi email non collegati (mailto) per la tua comodità. Tuttavia, i testi e i link non sono facilmente distini, specialmente non con molti [domini di alto livello](https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains) che sono parole. Questo perché i testi con punti sono talvolta riconosciuti erroneamente come collegamenti, il che è meglio che non riconoscere alcuni link.

I collegamenti per i protocolli tel, geo, rtsp e xmpp saranno anch'essi riconosciuti, ma quelli per protocolli meno solito o meno sicuri come telnet e ftp non saranno riconosciuti. La regex per riconoscere i link è già *molto* complessa e aggiungere altri protocolli la renderà solo più lenta, causando possibilmente errori.

Nota che i messaggi originali sono mostrati esattamente come sono, il che significa anche che i link non sono aggiunti automaticamente.

<br />

<a name="faq91"></a>
**~~(91) Puoi aggiungere la sincronizzazione periodica per risparmiare batteria?~~**

~~Sincronizzare i messaggi è un processo costoso perché i messaggi locali e remoti devono esser confrontati,~~ ~~quindi sincronizzare periodicamente i messaggi non risulterà in un risparmio di energia della batteria, più probabilmente il contrario.~~

~~Vedi [questa FAQ](#user-content-faq39) sull'ottimizzazione dell'uso della batteria.~~

<br />

<a name="faq92"></a>
**(92) Puoi aggiungere il filtraggio dello spam, la verifica della firma DKIM e l'autorizzazione SPF?**

Il filtraggio dello spam, la verifica della firma [DKIM](https://en.wikipedia.org/wiki/DomainKeys_Identified_Mail) e l'autorizzazione di [SPF](https://en.wikipedia.org/wiki/Sender_Policy_Framework) sono compiti dei server email, non di un client email. I server hanno generalmente più memoria e potenza di calcolo, quindi sono molto più adatti a quest'attività che i dispositivi alimentati a batteria. Inoltre, vorrai che lo spam sia filtrato per tutti i tuoi client email, possibilmente includendo l'email web, non solo uno dei client email. Peraltro, i server email hanno accesso a informazioni come l'indirizzo IP, etc. del server di connessione, a cui il client email non ha alcun accesso.

Il filtraggio dello spam basato sulle intestazioni dei messaggi sarebbe potuto esser fattibile, ma sfortunatamente questa tecnica è [brevettata da Microsoft](https://patents.google.com/patent/US7543076).

Le versioni recenti di FairEmail possono filtrare lo spam fino a un certo punto, usando un classificatore di messaggi. Sei pregato di vedere [questa FAQ](#user-content-faq163) per ulteriori informazioni a riguardo.

Certamente puoi segnalare i messaggi come spam con FairEmail, il che li sposterà alla cartella spam e addestrerà il filtro del provider, così come dovrebbe funzionare. Questo può anche esser fatto automaticamente con le [regole del filtro](#user-content-faq71). Bloccare il mittente creerà una regola del filtro per spostare automaticamente i messaggi futuri dello stesso mittente alla cartella dello spam.

Nota che il protocollo POP3 da accesso solo alla posta in arrivo. Quindi non sarà possibile segnalare spam per i profili POP3.

Nota che non dovresti eliminare i messaggi di spam nemmeno dalla cartella di spam, perché il server email li usa per "imparare" cosa sono i messaggi di spam.

Se ricevi molti messaggi di spam nella tua posta in arrivo, il meglio che puoi fare è contattare il provider email per chiedere se possa esser migliorato il filtraggio antispam.

Inoltre, FairEmail può mostrare una piccola bandiera rossa di avviso quando l'autenticazione DKIM, SPF o [DMARC](https://en.wikipedia.org/wiki/DMARC) fallisce al server di ricezione. Puoi abilitare/disabilitare la [verifica dell'autenticazione](https://en.wikipedia.org/wiki/Email_authentication) nelle impostazioni di visualizzazione.

FairEmail può anche mostrare una bandiera di avviso se il nome del dominio dell'indirizzo email (di risposta) del mittente non definisce un registro MX mirato a un server email. Questo è abilitabile nelle impostazioni di ricezione. Sappi che questo rallenterà la sincronizzazione dei messaggi significativamente.

Se il nome del dominio del mittente e dell'indirizzo di risposta differiscono, anche la bandiera d'avviso sarà mostrata perché questo è più spesso il caso con i messaggi di phishing. Se lo desideri, questo è disabilitabile nelle impostazioni di ricezione (dalla versione 1.1506).

Se i messaggi legittimi falliscono l'autenticazione, dovresti notificare il mittente, perché questo risulterà in un alto rischio per i messaggi di finire nella cartella di spam. Inoltre, senza un'autenticazione adatta esiste un rischio che il mittente sia impersonato. Il mittente potrebbe usare [questo strumento](https://www.mail-tester.com/) per verificare l'autenticazione e altre cose.

<br />

<a name="faq93"></a>
**(93) Puoi consentire l'installazione/archiviazione dei dati su multimedia di archiviazione esterna (scheda sd)?**

FairEmail usa servizi e avvisi, fornisce widget e ascolta l'evento di avvio completato per avviarsi all'accensione del dispositivo, quindi è impossibile memorizzare l'app sui multimedia di archiviazione esterna, come in una scheda sd. Vedi anche [qui](https://developer.android.com/guide/topics/data/install-location).

Messaggi, allegati, etc. memorizzati sul multimedia di archiviazione esterna, come una scheda sd, sono accessibili da altre app e dunque non sono sicuri. Vedi [qui](https://developer.android.com/training/data-storage) per i dettagli.

Quando necessario puoi salvare i messaggi (grezzi) tramite il menu a tre puntini proprio sopra il testo del messaggio e salvare gli allegati toccando l'icona del floppy disk.

Se devi salvare su spazio di archiviazione, puoi limitare il numero di messaggi giornalieri sincronizzati e mantenuti. Puoi modificare queste impostazioni tenendo premuta una cartella nell'elenco delle cartelle e selezionando *Modifica proprietà*.

<br />

<a name="faq94"></a>
**(94) Cosa significa la striscia rossa/arancione alla fine dell'intestazione?**

La striscia rossa/arancione sul lato sinistro dell'intestazione indica che l'autenticazione DKIM, SPF o DMARC è fallita. Vedi anche [questa FAQ](#user-content-faq92).

<br />

<a name="faq95"></a>
**(95) Perché non sono mostrate tutte le app selezionando un allegato o un'immagine?**

Per motivi di privacy e sicurezza, FairEmail non ha autorizzazioni per accedere direttamente ai file, a differenza del Framework di Accesso all'Archiviazione, disponibile e consigliato da Android 4.4 KitKat (rilasciata nel 2013), usato per selezionare i file.

Che un'app sia elencata dipende da se l'app implementi un [provider di documenti](https://developer.android.com/guide/topics/providers/document-provider). Se l'app non è elencata, potresti dover chiedere allo sviluppatore dell'app di aggiungere supporto al Framework di Acceso all'Archiviazione.

Android Q renderà più difficile e forse persino impossibile accedere direttamente ai file, vedi [qui](https://developer.android.com/preview/privacy/scoped-storage) e [qui](https://www.xda-developers.com/android-q-storage-access-framework-scoped-storage/) per ulteriori dettagli.

<br />

<a name="faq96"></a>
**(96) Dove posso trovare le impostazioni di IMAP e SMTP?**

Le impostazioni IMAP sono parte delle impostazioni (personalizzate) del profilo e le impostazioni SMTP fanno parte delle impostazioni di identità.

<br />

<a name="faq97"></a>
**(97) Cos'è la 'pulizia' ?**

Circa ogni quattro ore, FairEmail esegue un lavoro di pulizia che:

* Rimuovi i testi dei vecchi messaggi
* Rimuovi i file dei vecchi allegati
* Rimuovi le immagini delle vecchie immagini
* Rimuove i contatti locali vecchi
* Rimuove le vecchie voci del registro

Nota che il lavoro di pulizia sarà eseguito solo quando il servizio di sincronizzazione è attivo.

<br />

<a name="faq98"></a>
**(98) Perché posso ancora selezionare i contatti dopo aver revocato i permessi della rubrica?**

Dopo aver revocato i permessi di contatto, Android non consentirà più l'accesso ai tuoi contatti a FairEmail. Tuttavia, selezionare i contatti è delegato a e fatto da Android e non da FairEmail, quindi è ancora possibile senza i permessi della rubrica.

<br />

<a name="faq99"></a>
**(99) Puoi aggiungere un editor del rich text o di markdown?**

FairEmail fornisce la formattazione comune del testo (grassetto, corsivo, sottolineato, dimensione e colore del testo) tramite una barra degli strumenti che compare selezionando del testo.

Un editor del [Rich text](https://en.wikipedia.org/wiki/Formatted_text) o di [Markdown](https://en.wikipedia.org/wiki/Markdown) non sarebbe usato da molte persone su un piccolo dispositivo mobile e, soprattutto, Android non supporta un editor di rich text e gran parte dei progetti open source di questi editor sono abbandonati. Vedi [qui](https://forum.xda-developers.com/showpost.php?p=79061829&postcount=4919) per ulteriori dettagli a riguardo.

<br />

<a name="faq100"></a>
**(100) Come posso sincronizzare le categorie di Gmail?**

Puoi sincronizzare le categorie Gmail creando filtri per etichettare i messaggi categorizzati:

* Crea un nuovo filtro tramite Gmail > Impostazioni (rotellina) > Filtri e Indirizzi Bloccati > Crea un nuovo filtro
* Inserisci una ricerca della categoria (vedi sotto) nel campo *Contiene le parole* e clicca *Crea filtro*
* Spunta *Applica all'etichetta* e seleziona un'etichetta e clicca *Crea filtro*

Categorie possibili:

```
categoria:social
categoria:aggiornamenti
categoria:forum
categoria:promozioni
```

Sfortunatamente, questo è impossibile per la cartella dei messaggi posticipati.

Puoi usare *Forza sincronizzazione* nel menu a tre puntini della casella unificata per far risincronizzare l'elenco delle cartelle a FairEmail e puoi tenere premute le cartelle per abilitare la sincronizzazione.

<br />

<a name="faq101"></a>
**(101) Cosa significa il puntino blu/arancione in fondo alle conversazioni?**

Il puntino mostra la posizione relativa della conversazione nell'elenco dei messaggi. Il puntino sarà mostrato arancione quando la conversazione è la prima o l'ultima dell'elenco dei messaggi, altrimenti sarà blu. Il puntino è intesto come un aiuto scorrendo a sinistra/destra per andare alla conversazione precedente/successiva.

Il puntino è disabilitato di default ed è abilitabile con le impostazioni di visualizzazione *Mostra la posizione relativa della conversazione con un puntino*.

<br />

<a name="faq102"></a>
**(102) Come posso abilitare la rotazione automatica delle immagini?**

Le immagini saranno ruotate automaticamente quando il ridimensionamento automatico delle immagini è abilitato nelle impostazioni (abilitato di default). Tuttavia, la rotazione automatica dipende dalla presenza e correttezza dell'informazione [Exif](https://en.wikipedia.org/wiki/Exif), il che non è sempre il caso. In particolare non lo è scattando una foto con un'app fotocamera da FairEmail.

Nota che solo le immagini [JPEG](https://en.wikipedia.org/wiki/JPEG) e [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) possono contenere le informazioni Exif.

<br />

<a name="faq104"></a>
**(104) Cosa devo sapere sulla segnalazione degli errori?**

* La segnalazione degli errori aiuterà a migliorare FairEmail
* La segnalazione degli errori è opzionale e su adesione
* La segnalazione degli errori può esser abilitata/disabilitata nelle impostazioni, sezione varie
* Le segnalazioni di errori saranno anonimamente inviate a [Bugsnag](https://www.bugsnag.com/)
* Bugsnag per Android è [open-source](https://github.com/bugsnag/bugsnag-android)
* Vedi [qui](https://docs.bugsnag.com/platforms/android/automatically-captured-data/) su quali dati saranno inviati in caso di errori
* Vedi [qui](https://docs.bugsnag.com/legal/privacy-policy/) per la politica sulla privacy di Bugsnag
* Le segnalazioni d'errore saranno inviate a *sessions.bugsnag.com:443* e *notify.bugsnag.com:443*

<br />

<a name="faq105"></a>
**(105) Come funziona l'opzione roam-like-at-home?**

FairEmail verificherà se il codice del paese della scheda SIM e della rete sono tra i [paesi roam-like-at-home dell'UE](https://en.wikipedia.org/wiki/European_Union_roaming_regulations#Territorial_extent) e presume che non ci sia roaming se i codici del paese sono uguali e l'opzione roam-like-at-home avanzata è abilitata.

Quindi, non devi disabilitare quest'opzione se non hai una SIM UE o non sei connesso a una rete UE.

<br />

<a name="faq106"></a>
**(106) Quali launcher possono mostrare un distintivo di conteggio con il numero di messaggi non letti?**

Sei pregato di [vedere qui](https://github.com/leolin310148/ShortcutBadger#supported-launchers) per un elenco di launcher che possano mostrare il numero di messaggi non letti.

Nota che il Nova Launcher richiede Tesla Unread, che [non è più supportato](https://forum.xda-developers.com/android/general/bad-news-tesla-unread-devoloper-t3920415).

Nota che l'impostazione di notifica *Mostra icona del launcher con il numero di nuovi messaggi* deve esser abilitata (abilitata di default).

Solo i *nuovi* messaggi non letti nelle cartelle impostate per mostrare le notifiche dei nuovi messaggi saranno contati, quindi quelli segnati di nuovo come non letti nelle cartelle impostate per non mostrare le notifiche dei nuovi messaggi non saranno contati.

In base a ciò che vuoi, le impostazioni di notifica *Abbina il numero di nuovi messaggi al numero di notifiche* devono esser impostate (disabilitate di default). Quando abilitato, il distintivo di conteggio sarà pari al numero delle notifiche dei nuovi messaggi. Quando disabilitato, sarà il numero di messaggi non letti, indipendentemente dal fatto che siano mostrati in una notifica o siano nuovi.

Questa funzionalità dipende dal supporto del tuo launcher. FairEmail 'trasmette' appena il numero di messaggi non letti usando la libreria ShortcutBadger. Se non funziona, non è risolvibile da modifiche in FairEmail.

Alcuni launcher mostrano un puntino o un '1' per [la notifica di monitoraggio](#user-content-faq2) sebbene FairEmail richieda esplicitamente di non mostrare un *distintivo* per questa notifica. Questo potrebbe esser causato da un bug nell'app del launcher o nella tua versione di Android. Sei pregato di ricontrollare se il punto di notifica (distintivo) è disabilitato per il canale di notifica della ricezione (servizio). Puoi andare alle impostazioni del giusto canale di notifica tramite le impostazioni di notifica di FairEmail. Questo potrebbe non esser ovvio, ma puoi toccare sul nome del canale per altre impostazioni.

FairEmail invia anche un nuovo intento di conteggio dei nuovi messaggi:

```
eu.faircode.email.NEW_MESSAGE_COUNT
```

Il numero dei nuovi messaggi non letti sarà un parametro intero "*count*".

<br />

<a name="faq107"></a>
**(107) Come uso le stelle colorate?**

Puoi impostare una stella colorata tramite il menu dei messaggi *altro*, tramite la seleziona multipla (avviata tenendo premuto un messaggio), tenendo premuta una stella in una conversazione o automaticamente usando le [regole](#user-content-faq71).

Devi sapere che le stelle colorate non sono supportate dal protocollo IMAP e possono dunque non esser sincronizzate a un server email. Ciò significa che le stelle colorate non saranno visibili in altri client email e saranno perse riscaricando i messaggi. Tuttavia, le stelle (senza colore) saranno sincronizzate e visibili in altri client email, quando supportate.

Alcuni client email usando le parole chiave IMAP per i colori. Tuttavia, non tutti i server supportano le parole chiave IMAP e, inoltre, non esistono parole chiave standard per i colori.

<br />

<a name="faq108"></a>
**~~(108) Puoi aggiungere l'eliminazione permanente dei messaggi da ogni cartella?~~**

~~Quando elimini i messaggi da una cartella, saranno spostati al cestino, quindi potrai ripristinarli.~~ ~~Puoi eliminare permanentemente i messaggi dal cestino.~~ ~~Eliminare permanentemente i messaggi da altre cartelle sconfiggerà lo scopo del cestino, quindi ciò non sarà aggiunto.~~

<br />

<a name="faq109"></a>
**~~(109) Perché 'seleziona profilo' è disponibile solo nelle versioni ufficiali?~~**

~~Usare *seleziona profilo* per selezionare e autorizzare profili Google richiede permessi speciali da Google per motivi di sicurezza e privacy.~~ ~~Questo permesso speciale è acquisibile solo per le app gestite da uno sviluppatore e di cui è responsabile.~~ ~~Le build di terze parti, come quelle di F-Droid, sono gestite da terze parti e sono responsabilità di queste terze parti.~~ ~~Quindi, solo queste possono acquisire il permesso richiesto da Google.~~ ~~Poiché queste terze parti non supportano realmente FairEmail, molto probabilmente non richiederanno il permesso necessario.~~

~~Puoi risolvere ciò in due modi:~~

* ~~Passando alla versione ufficiale di FairEmail, vedi [qui](https://github.com/M66B/FairEmail/blob/master/README.md#downloads) le opzioni~~
* ~~Usando password specifiche dell'app, vedi [questa FAQ](#user-content-faq6)~~

~~Usare *seleziona profilo* nelle build di terze parti non è più possibile nelle versioni recenti.~~ ~~Nelle versioni più vecchie era possibile, ma risulterà ora nell'errore *UNREGISTERED_ON_API_CONSOLE*.~~

<br />

<a name="faq110"></a>
**(110) Perché (alcuni) messaggi sono vuoti e/o gli allegati sono corrotti?**

I messaggi vuoti e/o gli allegati corrotti sono probabilmente causati da un bug nel software del server. I più vecchi software di Microsoft Exchange sono noti per causare questo problema. Principalmente puoi risolvere ciò disabilitando il *Recupero parziale* nelle impostazioni avanzate del profilo:

Impostazioni > Configurazione manuale > Profili > tocca il profilo > tocca avanzate > Recupero parziale > deseleziona

Dopo aver disabilitato quest'impostazione, puoi usare il menu 'altro' (tre puntini) del messaggio per 'risincronizzare' i messaggi vuoti. Altrimenti, puoi *Eliminare i messaggi locali* tenendo premute le cartelle nell'elenco delle cartelle e risincronizzare tutti i messaggi.

Disabilitare il *Recupero parziale* risulterà in un maggiore uso della memoria.

<br />

<a name="faq111"></a>
**(111) OAuth è supportato?**

OAuth per Gmail è supportato tramite la procedura guidata di configurazione rapida. Il gestore del profilo Android sarà usato per recuperare e ricaricare i token OAuth per i profili selezionati sul dispositivo. OAuth per i profili non sul dispositivo non è supportato perché Google richiede un [controllo annuale della sicurezza](https://support.google.com/cloud/answer/9110914) (da $15.000 a $75.000) per questo. Puoi leggere di più a riguardo [qui](https://www.theregister.com/2019/02/11/google_gmail_developer/).

OAuth per Outlook/Office 365, Yahoo, Mail.ru e Yandex è supportato tramite la procedura guidata di configurazione rapida.

<br />

<a name="faq112"></a>
**(112) Che provider email consigli?**

FairEmail è solo un client email, quindi necessiti del tuo indirizzo email. Nota che è chiaramente menzionato nella descrizione dell'app.

Ci sono molti provider email tra cui scegliere. Quale è migliore per te dipende dai tuoi desideri/requisiti. Sei pregato di vedere questi siti web di [Ripristino della privacy](https://restoreprivacy.com/secure-email/) e [Strumenti di Privacy](https://www.privacytools.io/providers/email/) per un elenco di provider email orientati alla privacy con vantaggi e svantaggi.

Alcuni provider, come ProtonMail e Tutanota, usano protocolli email proprietari, che rendono impossibile usare app email di terze parti. Sei pregato di vedere [questa FAQ](#user-content-faq129) per ulteriori informazioni.

Usare il proprio nome di dominio (personalizzato), supportato da gran parte dei provider email, faciliterà il passaggio a un altro provider email.

<br />

<a name="faq113"></a>
**(113) Come funziona l'autenticazione biometrica?**

Se il tuo dispositivo ha un sensore biometrico, ad esempio un sensore di impronte digitali, puoi abilitare/disabilitare l'autenticazione biometrica nel menu di navigazione (ad hamburger) della schermata delle impostazioni. Quando abilitato, FairEmail richiederà l'autenticazione biometrica dopo un periodo di attività o dopo lo spegnimento dello schermo durante l'esecuzione di FairEmail. L'attività è la navigazione in FairEmail, ad esempio, l'apertura di un thread di conversazione. La durata del periodo di inattività è configurabile nelle impostazioni varie. Quando l'autenticazione biometrica è abilitata, le notifiche dei nuovi messaggi non mostreranno alcun contenuto e FairEmail non sarà visibile sulla schermata recenti di Android.

L'autenticazione biometrica è intesa solo per prevenire che altri vedano i tuoi messaggi. FairEmail si basa sulla crittografia del dispositivo per crittografare i dati, vedi anche [questa FAQ](#user-content-faq37).

L'autenticazione biometrica è una funzionalità pro.

<br />

<a name="faq114"></a>
**(114) Puoi aggiungere un'importazione per le impostazioni di altre app email?**

Il formato dei file di impostazioni di gran parte delle altre app email non è documentato, quindi è difficile. A volte è possibile invertire l'ingegneria del formato, ma appena il formato delle impostazioni cambia, le cose inizieranno a rompersi. Inoltre, spesso le impostazioni sono incompatibili. Per esempio, FairEmail ha a differenza di gran parte delle altre app email, impostazioni per il numero di giorni per sincronizzare messaggi e per il numero di giorni per mantenere i messaggi, principalmente per risparmiare sull'uso della batteria. Inoltre, la configurazione di un profilo/identità con la procedura guidata di configurazione rapida è semplice, quindi non vale davvero la pena.

<br />

<a name="faq115"></a>
**(115) Puoi aggiungere i chip dell'indirizzo email?**

I [chip](https://material.io/design/components/chips.html) dell'indirizzo email sembrano belli, ma non sono modificabili, il che è alquanto scomodo quando fai un errore in un indirizzo email.

Nota che FairEmail selezionerà solo l'indirizzo tenendolo premuto, facilitandone l'eliminazione.

I chip non sono adatti alla visualizzazione in un elenco e poiché l'intestazione del messaggio in un elenco dovrebbe somigliare all'intestazione del messaggio nella visualizzazione messaggi, usare i chip per visualizzarli non è un'opzione.

[Commit](https://github.com/M66B/FairEmail/commit/2c80c25b8aa75af2287f471b882ec87d5a5a5015) ripristinata.

<br />

<a name="faq116"></a>
**~~(116) Come posso mostrare le immagini nei messaggi dai mittenti fidati di default?~~**

~~Puoi mostrare le immagini nei messaggi da mittenti fidati di default abilitando l'impostazione di visualizzazione *Mostra automaticamente le immagini per i contatti noti*.~~

~~I contatti nella rubrica di Android sono considerati noti e fidati,~~ ~~a meno che il contatto non sia nel gruppo / abbia l'etichetta '*Inaffidabile*' (sensibile alle maiuscole).~~

<br />

<a name="faq38"></a>
<a name="faq117"></a>
**(117) Puoi aiutarmi a ripristinare il mio acquisto?**

Prima di tutto, un acquisto sarà disponibile su tutti i dispositivi connessi allo stesso profilo Google, *se* l'app è anche installata tramite lo stesso profilo Google. Puoi selezionare il profilo nell'app del Play Store.

Google gestisce tutti gli acquisti, quindi come sviluppatore ho poco controllo su di essi. Quindi, fondamentalmente, l'unica cosa che posso fare, è dare dei consigli:

* Assicurati di avere una connessione a internet attiva e funzionante
* Assicurati di esser connesso con il giusto profilo di Google e che non ci sia niente di sbagliato con il tuo profilo di Google
* Assicurati di aver installato FairEmail tramite il giusto profilo di Google se ne hai configurato più di uno sul tuo dispositivo
* Assicurati che l'app del Play Store sia aggiornata, sei pregato di [vedere qui](https://support.google.com/googleplay/answer/1050566?hl=en)
* Apri l'app del Play Store e attendi almeno un minuto per dargli tempo di sincronizzarsi con i server di Google
* Apri FairEmail e naviga alla schermata delle funzionalità pro per far verificare gli acquisti a FairEmail; a volte aiuta toccare il pulsante *compra*

Puoi anche provare a cancellare la cache dell'app del Play Store tramite le impostazioni delle app di Android. Potrebbe esser necessario riavviare il dispositivo per far riconoscere correttamente l'acquisto al Play Store.

Nota che:

* Se ottieni *ITEM_ALREADY_OWNED*, l'app del Play Store probabilmente dev'esser aggiornata, sei pregato di [vedere qui](https://support.google.com/googleplay/answer/1050566?hl=en)
* Gli acquisti sono memorizzati nel cloud di Google e non possono esser perduti
* Non c'è limite di tempo sugli acquisti, quindi non possono scadere
* Google non espone dettagli (nome, email, etc.) sui compratori agli sviluppatori
* Un'app come FairEmail non può selezionare che profilo di Google usare
* Potrebbe volerci un po' fino alla sincronizzazione di un'acquisto dell'app di Play Store a un altro dispositivo
* Gli acquisti del Play Store non sono utilizzabili senza il Play Store, che non è peraltro consentito dalle regole del Play Store

Se non puoi risolvere il problema con l'acquisto, dovrai contattare Google a riguardo.

<br />

<a name="faq118"></a>
**(118) Cosa fa esattamente 'Rimuovi parametri di monitoraggio'?**

Spuntare *Rimuovi parametri di monitoraggio*, rimuoverà tutti i [Parametri UTM](https://en.wikipedia.org/wiki/UTM_parameters) da un collegamento.

<br />

<a name="faq119"></a>
**~~(119) Puoi aggiungere i colori al widget della casella di posta in arrivo unificata?~~**

~~Il widget è progettato per avere un bell'aspetto su gran parte delle schermate home/del launcher rendendolo monocromo e usando uno sfondo mezzo trasparente.~~ ~~Così, il widget si fonderà piacevolmente, rimanendo comunque propriamente leggibile.~~

~~Aggiungere colori causerà problemi con alcuni sfondi e problemi di leggibilità, per cui non saranno aggiunti.~~

A causa di limitazioni di Android è impossibile impostare dinamicamente l'opacità dello sfondo e avere angoli arrotondati allo stesso tempo.

<br />

<a name="faq120"></a>
**(120) Perché le notifiche di nuovo messaggio non sono rimosse all'apertura dell'app?**

Le notifiche dei nuovi messaggi saranno rimosse facendo scorrere via le notifiche o contrassegnando i messaggi associati come letti. Aprire l'app non rimuoverà le notifiche dei nuovi messaggi. Questo ti da la scelta di lasciare le notifiche dei nuovi messaggi come promemoria che ci siano ancora messaggi non letti.

Su Android 7 Nougat e successive le notifiche dei nuovi messaggi saranno [raggruppate](https://developer.android.com/training/notify-user/group). Toccare sulla notifica di riepilogo aprirà la casella unificata. La notifica di riepilogo è espandibile per vedere le singole notifiche dei nuovi messaggi. Toccare su una singola notifica del nuovo messaggio aprirà la conversazione di cui questo fa parte. Vedi [questa FAQ](#user-content-faq70) su quando i messaggi in una conversazione saranno automaticamente espansi e contrassegnati letti.

<br />

<a name="faq121"></a>
**(121) Come sono raggruppati i messaggi in una conversazione?**

Di default, FairEmail raggruppa i messaggi in conversazioni. Questo è disattivabile nelle impostazioni di visualizzazione.

FairEmail raggruppa i messaggi in base alle intestazioni standard *Message-ID*, *In-Reply-To* e *References*. FairEmail non raggruppa su altri criteri, come l'oggetto, perché ciò potrebbe risultare nel raggruppamento di messaggi non correlati e andrebbe alle spese di un aumento dell'uso della batteria.

<br />

<a name="faq122"></a>
**~~(122) Perché il nome/indirizzo email del destinatario è mostrato con un colore di avviso?~~**

~~Il nome e/o l'indirizzo email del destinatario nella sezione degli indirizzi saranno mostrati in un colore d'avviso~~ ~~quando il nome di dominio del mittente e il nome di dominio dell'indirizzo *a* non corrispondono.~~ ~~Questo indica principalmente che il messaggio è stato ricevuto *tramite* un profilo con un altro indirizzo email.~~

<br />

<a name="faq123"></a>
**(123) Cosa succederà quando FairEmail non potrà connettersi a un server email?**

Se FairEmail non può connettersi a un server email per sincronizzare i messaggi, ad esempio se la connessione a internet è cattiva o un firewall o una VPN stanno bloccando la connessione, FairEmail riproverà una volta dopo aver atteso 8 secondi tenendo attivo il dispositivo (=uso di energia della batteria). Se questo fallisce, FairEmail pianificherà un avviso per riprovare dopo 15, 30 ed eventualmente ogni 60 minuti e lascerà il dispositivo in standby (=nessun uso della batteria).

Nota che la [modalità di riposo di Android](https://developer.android.com/training/monitoring-device-state/doze-standby) non consente al dispositivo di attivarsi prima del passaggio di 15 minuti.

*Forza sincronizzazione* nel menu a tre puntini della casella unificata è utilizzabile per far tentare a FairEmail la riconnessione senza attendere.

L'invio dei messaggi sarà riprovato solo al cambiare della connettività (riconnessione alla stessa rete o connessione a un'altra) per prevenire al server email di bloccare permanentemente la connessione. Puoi scorrere in giù nella casella della posta in uscita per riprovare manualmente.

Nota che l'invio non sarà riprovato in caso di problemi di autenticazione e quando il server ha rifiutato il messaggio. In questo caso puoi scorrere in giù nella posta in uscita per riprovare.

<br />

<a name="faq124"></a>
**(124) Perché ottengo 'Messaggio troppo grande o troppo complesso da mostrare'?**

Il messaggio *Messaggio troppo grande o troppo complesso da mostrare* sarà mostrato se ci sono oltre 100.000 caratteri o oltre 500 link in un messaggio. Riformattare e mostrare tali messaggi impiegherà troppo tempo. Puoi piuttosto provare a usare la vista del messaggio originale, alimentata dal browser.

<br />

<a name="faq125"></a>
**(125) Quali sono le correnti funzionalità sperimentali?**

*Classificazione dei messaggi (versione 1.1438+)*

Sei pregato di vedere [questa FAQ](#user-content-faq163) per i dettagli.

Poiché è una funzionalità sperimentale, consiglio di iniziare con solo una cartella.

<br />

*Invia hard bounce (versione 1.1477+)*

Invia una [Notifica dello Stato di Consegna](https://tools.ietf.org/html/rfc3464) (=hard bounce) tramite il menu di risposta.

Gli hard bounce saranno prevalentemente elaborati automaticamente perché influenzano la reputazione del provider email. L'indirizzo di bounce (rimbalzo) (=intestazione *Return-Path*) è per lo più molto specifico, quindi il server email può determinare il profilo di invio.

Per un po' di background, leggi [questo articolo di Wikipedia](https://en.wikipedia.org/wiki/Bounce_message).

<br />

<a name="faq126"></a>
**(126) Le anteprime dei messaggi sono inviabili al mio indossabile?**

FairEmail recupera un messaggio in due passaggi:

1. Recupera intestazioni del messaggio
1. Recupera testo e allegati del messaggio

Direttamente dopo la prima fase saranno notificati i nuovi messaggi. Tuttavia, solo dopo la seconda fase sarà disponibile il testo del messaggio. FairEmail aggiorna le notifiche esistenti con un'anteprima del testo del messaggio, ma sfortunatamente le notifiche dell'indossabile non sono aggiornabili.

Poiché non esiste garanzia che il testo di un messaggio sia sempre recuperato direttamente dopo la sua intestazione, non è possibile garantire che una nuova notifica del messaggio con un testo in anteprima sarà sempre inviata a un indossabile.

Se pensi che sia abbastanza, puoi abilitare l'opzione di notifica *Invia solo le notifiche con un'anteprima di messaggio agli indossabile* e se non funziona, puoi provare ad abilitare l'opzione di notifica *Mostra solo le notifiche con un testo di anteprima*. Nota che questo si applica anche agli indossabili che non mostrano un testo di anteprima, anche quando l'app Android Wear dice che la notifica è stata inviata (collegata).

Se vuoi che l'intero testo del messaggio sia inviato al tuo indossabile, puoi abilitare l'opzione di notifica *Anteprima del testo completo*. Nota che alcuni indossabili sono noti per arrestarsi anomalmente quando quest'opzione è abilitata.

Se usi un indossabile di Samsung con l'app Galaxy Wearable (Samsung Gear), potresti dover abilitare le notifiche per FairEmail quando l'impostazione *Notifiche*, *App installate in futuro* è disattivato in quest'app.

<br />

<a name="faq127"></a>
**(127) Come posso correggere 'Argomenti HELO sintatticamente non validi'?**

L'errore*... Argomenti HELO sintatticamente non validi ...* significa che il server SMTP ha rifiutato l'indirizzo IP locale o il nome dell'host. Potresti risolvere questo errore abilitando o disabilitando l'opzione di identità avanzata *Usa indirizzo IP locale invece del nome dell'host*.

<br />

<a name="faq128"></a>
**(128) Come posso ripristinare le domande fatte, ad esempio per mostrare le immagini?**

Puoi ripristinare le domande fatte tramite il menu di trabocco a tre puntini nelle impostazioni varie.

<br />

<a name="faq129"></a>
**(129) ProtonMail e Tutanota sono supportate?**

ProtonMail usa un protocollo email proprietario e [non supporta direttamente IMAP](https://protonmail.com/support/knowledge-base/imap-smtp-and-pop3-setup/), quindi non puoi usare FairEmail per accedervi.

Tutanota usa un protocollo email proprietario e [non supporta IMAP](https://tutanota.com/faq/#imap) quindi non puoi usare FairEmail per accedervi.

<br />

<a name="faq130"></a>
**(130) Cosa significa il messaggio di errore...?**

Una serie di righe di testo arancioni o rosse con informazioni tecniche significano che la modalità di debug è stata abilitata nelle impostazioni varie.

L'avviso *Nessun server trovato a ...* significa che non esisteva alcun server email registrato al nome di dominio indicato. Rispondere al messaggio potrebbe essere impossibile e risultare in un errore. Questo potrebbe indicare un indirizzo email falsificato e/o spam.

L'errore*... ParseException...* significa che c'è un problema con un messaggio ricevuto, probabilmente causato da un bug nel software di invio. FairEmail risolverà ciò in gran parte dei casi, quindi questo messaggio è principalmente considerabile come un avviso invece che un errore.

L'errore*...SendFailedException...* significa che si è verificato un problema inviando un messaggio. L'errore includerà quasi sempre un motivo. Motivi comuni sono che il messaggio era troppo grande o che uno o più indirizzi dei destinatari non erano validi.

L'avviso *Messaggio troppo grande per la memoria disponibile* significa che il messaggio era più grande di 10 MiB. Anche se il tuo dispositivo ha molto spazio di archiviazione, Android fornisce memoria operativa limitata alle app, limitando la dimensione dei messaggi gestibili.

Sei pregato di vedere [qui](#user-content-faq22) per altri messaggi d'errore nella posta in uscita.

<br />

<a name="faq131"></a>
**(131) Puoi modificare la direzione per lo scorrimento al messaggio precedente/successivo?**

Se leggi da sinistra a destra, scorrere a sinistra mostrerà il messaggio successivo. Similmente, se leggi da destra a sinistra, scorrere a destra mostrerà il messaggio successivo.

Questo comportamento mi sembra alquanto naturale, anche perché è simile al voltare le pagine.

Tuttavia, esiste un'impostazione del comportamento per invertire la direzione di scorrimento.

<br />

<a name="faq132"></a>
**(132) Perché le notifiche dei nuovi messaggi sono silenziate?**

Le notifiche sono silenziate di default su alcune versioni MIUI. Sei pregato di vedere [qui](http://en.miui.com/thread-3930694-1-1.html) come puoi risolverlo.

Esiste un bug in alcune versioni di Android che causa a [setOnlyAlertOnce](https://developer.android.com/reference/android/app/Notification.Builder#setOnlyAlertOnce(boolean)) di silenziare le notifiche. Poiché FairEmail mostra le notifiche dei nuovi messaggi subito dopo averne recuperato le intestazioni, FairEmail deve aggiornare le notifiche dei nuovi messaggi dopo aver recuperato il testo del messaggio, questo non è risolvibile o aggirabile in FairEmail.

Android potrebbe limitare il suono di notifica, il che potrebbe causare ad alcune notifiche dei nuovi messaggi di esser silenziate.

<br />

<a name="faq133"></a>
**(133) Perché ActiveSync non è supportato?**

Il protocollo ActiveSync di Microsoft Exchange [ è brevettato](https://en.wikipedia.org/wiki/Exchange_ActiveSync#Licensing) e dunque non può esser supportato. Per questo motivo non troverai molti, se presenti, altri client email che supportano ActiveSync.

Nota che la descrizione di FairEmail inizia con l'osservazione che i protocolli non standard, come i Servizi Web di Microsoft Exchange e Microsoft ActiveSync non sono supportati.

<br />

<a name="faq134"></a>
**(134) Puoi aggiungere l'eliminazione dei messaggi locali?**

*POP3*

Nelle impostazioni del profilo (Impostazioni, tocca Configurazione manuale, tocca Profili, tocca il profilo), puoi abilitare *Lascia i messaggi eliminati sul server*.

*IMAP*

Poiché il protocollo IMAP è inteso per sincronizzarsi in due modi, eliminare un messaggio dal dispositivo risulterà nel suo recupero alla nuova sincronizzazione.

Tuttavia, FairEmail supporta i messaggi nascosti, tramite il menu a tre puntini nella barra d'azione proprio sopra il testo del messaggio o selezionando più messaggi nell'elenco dei messaggi. Fondamentalmente, questo equivale a "lascia sul server" del protocollo POP3, con il vantaggio che puoi mostrare di nuovo i messaggi quando necessario.

Nota che puoi impostare l'azione di scorrimento a sinistra o destra per nascondere un messaggio.

<br />

<a name="faq135"></a>
**(135) Perché i messaggi cestinati e di bozza sono mostrati nelle conversazioni?**

I singoli messaggi saranno raramente cestinati e prevalentemente ciò avviene per errore. Mostrare i messaggi cestinati nelle conversazioni ne semplifica il ritrovamento.

Puoi eliminare permanentemente un messaggio usando il menu a tre puntini *elimina*, che rimuoverà il messaggio dalla conversazione. Nota che ciò è irreversibile.

Similmente, le bozze sono mostrate nelle conversazioni per ritrovarle nel contesto cui appartengono. Leggere tramite i messaggi ricevuti è facile prima di continuare a scrivere la bozza in seguito.

<br />

<a name="faq136"></a>
**(135) Come posso eliminare un profilo/un'identità/una cartella?**

Eliminare un profilo/identità/cartella è un po' nascosto per prevenire incidenti.

* Profilo: Impostazioni > Configurazione manuale > Profili > tocca il profilo
* Identità: Impostazioni> Configurazione manuale > Identità > tocca l'identità
* Cartella: Tieni premuta la cartella nell'elenco delle cartelle > Modifica proprietà

Nel menu di trabocco a tre puntini in alto a destra esiste un elemento per eliminare il profilo/l'identità/la cartella.

<br />

<a name="faq137"></a>
**(137) Come posso ripristinare 'Non chiedermelo più'?**

Puoi ripristinare tutte le domande impostate per non esser più chieste nelle impostazioni varie.

<br />

<a name="faq138"></a>
**(138) Puoi aggiungere la gestione del calendario/rubrica/attività/note?**

La gestione del calendario, della rubrica, delle attività e delle note può esser fatta meglio da un'app separata e specializzata. Nota che FairEmail è un'app email specializzata, non una suite d'ufficio.

Inoltre, preferisco fare poche cose molto bene, che molte cose solo a metà. Peraltro, da una prospettiva di sicurezza, non è una buona idea garantire molti permessi a una sola app.

Si consiglia di usare l'eccellente app open source [DAVx⁵](https://f-droid.org/packages/at.bitfire.davdroid/) per sincronizzare/gestire i tuoi calendari/contatti.

Gran parte dei provider supportano l'esportazione dei tuoi contatti. Sei pregato di [vedere qui](https://support.google.com/contacts/answer/1069522) come puoi importare i contatti se la sincronizzazione è impossibile.

Nota che FairEmail supporta la risposta agli inviti del calendario (una funzionalità pro) e l'aggiunta degli inviti del calendario al tuo calendario personale.

<br />

<a name="faq83"></a>
<a name="faq139"></a>
**(139) Come correggo 'Utente autenticato ma non connesso'?**

Difatti, questo errore specifico di Microsoft Exchange è un messaggio errato di errore causato da un bug nei vecchi software del server di Microsoft Exchange.

L'errore *Utente autenticato ma non connesso* potrebbe verificarsi se:

* I messaggi push sono abilitati per troppe cartelle: vedi [questa FAQ](#user-content-faq23) per ulteriori informazioni e una soluzione
* La password del profilo è stata cambiata: cambiarla anche in FairEmail dovrebbe risolvere il problema
* L'indirizzo email di un alias è in uso come nome utente invece che come indirizzo email principale
* Uno schema di accesso errato è in uso per una casella condivisa: lo schema esatto è *nomeutente@dominio\SharedMailboxAlias*

L'alias della casella condivisa sarà prevalentemente l'indirizzo email del profilo condiviso, come questo:

```
you@example.com\shared@example.com
```

Nota che dovrebbe essere un backslash, non uno slash.

Usando una casella condivisa, potresti voler abilitare l'opzione *Sincronizza elenchi delle cartelle condivise* nelle impostazioni di ricezione.

<br />

<a name="faq140"></a>
**(140) Perché il testo del messaggio contiene caratteri strani?**

Mostrare caratteri strani è quasi sempre causato dalla specificazione di nessuna o un'invalida codifica del carattere, dal software di invio. FairEmail presumerà [ISO 8859-1](https://en.wikipedia.org/wiki/ISO/IEC_8859-1) quando non è impostato alcun carattere o quando [US-ASCII](https://en.wikipedia.org/wiki/ASCII) è stato specificato. Oltre al fatto che non c'è modo di determinare affidabilmente e automaticamente la codifica corretta del carattere, questo non è risolvibile da FairEmail. L'azione corretta è lamentarsi con il mittente.

<br />

<a name="faq141"></a>
**(141) Come posso correggere 'Una cartella delle bozze è necessaria per inviare i messaggi'?**

Per memorizzare le bozze dei messaggi è necessaria una cartella delle bozze. In gran parte dei casi, FairEmail selezionerà automaticamente le cartelle delle bozze all'aggiunta di un profilo, in base [agli attributi](https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml) inviati dal server email. Tuttavia, alcuni server email non sono propriamente configurati e non inviano tali attributi. In questo caso, FairEmail prova a identificare la cartella delle bozze per nome, ma questo potrebbe fallire se la cartella delle bozze ha un nome insolito o non è affatto presente.

Puoi risolvere questo problema selezionando manualmente la cartella delle bozze nelle impostazioni del profilo (Impostazioni, tocca Configurazione manuale, tocca Profili, tocca il profilo, in basso). Se non c'è alcuna cartella delle bozze, puoi crearne una toccando sul pulsante '+' nell'elenco delle cartelle del profilo (tocca sul nome del profilo nel menu di navigazione).

Alcuni provider, come Gmail, consentono di abilitare/disabilitare IMAP per le singole cartelle. Quindi, se una cartella è invisibile, potresti dover abilitare IMAP per essa.

Collegamento rapido per Gmail (funzionerà solo su un computer): [https://mail.google.com/mail/u/0/#settings/labels](https://mail.google.com/mail/u/0/#settings/labels)

<br />

<a name="faq142"></a>
**(142) Come posso conservare i messaggi inviati nella posta in arrivo?**

Generalmente, non è una buona idea conservare i messaggi inviati nella casella della posta in arrivo, perché è difficile da annullare e potrebbe esser incompatibile con altri client email.

Detto ciò, FairEmail può propriamente gestire i messaggi inviati nella posta in arrivo. FairEmail contrassegnerà i messaggi in uscita ad esempio con un'icona dei messaggi inviati.

La soluzione migliore sarebbe abilitare la visualizzazione della cartella inviati nella casella unificata, tenendo premuta la cartella inviati nell'elenco delle cartelle e abilitando *Mostra nella casella unificata*. Così, tutti i messaggi possono rimanere a dove appartengono, consentendo di vedere sia i messaggi in entrata che in uscita in un posto.

Se questa non è un'opzione, puoi [creare una regola](#user-content-faq71) per spostare automaticamente i messaggi inviati alla posta in arrivo o impostare un indirizzo CC/CCN predefinito nelle impostazioni avanzate dell'identità per inviarti una copia.

<br />

<a name="faq143"></a>
**~~(143) Puoi aggiungere una cartella del cestino per i profili POP3?~~**

[POP3](https://en.wikipedia.org/wiki/Post_Office_Protocol) è un protocollo davvero limitato. Di base i messaggi sono scaricabili ed eliminabili dalla casella. Non è nemmeno possibile contrassegnare un messaggio come letto.

Poiché POP3 non consente alcun accesso al cestino, non c'è modo di ripristinare i messaggi cestinati.

Nota che puoi nascondere e cercare i messaggi, similmente a una cartella del cestino locale, senza suggerire che i messaggi cestinati siano ripristinabili, mentre non è in realtà possibile.

La versione 1.1082 ha aggiunto una cartella locale del cestino. Nota che cestinare un messaggio lo rimuoverà permanentemente dal server e che i mesaggi cestinati non saranno più ripristinabili sul server.

<br />

<a name="faq144"></a>
**(144) Come posso registrare le note vocali?**

Per registrare le note vocali puoi premere quest'icona nella barra d'azione in basso al compositore di messaggi:

![Immagine esterna](https://github.com/M66B/FairEmail/blob/master/images/baseline_record_voice_over_black_48dp.png)

Questo richiede un'app di registrazione vocale compatibile installata. In particolare [questo intento comune](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION) dev'esser supportato.

Per esempio, [questo registratore audio](https://f-droid.org/app/com.github.axet.audiorecorder) è compatibile.

Le note vocali saranno automaticamente allegate.

<br />

<a name="faq145"></a>
**(145) Come posso impostare un suono di notifica per un profilo, una cartella o un mittente?**

Profilo:

* Abilita *Notifiche separate* nelle impostazioni avanzate del profilo (Impostazioni, tocca Configurazione manuale, tocca Profili, tocca il profilo, tocca Avanzate)
* Tieni premuto il profilo nell'elenco dei profili (Impostazioni, tocca Configurazione manuale, tocca Profili) e seleziona *Modifica canale di notifica* per modificare il suono della notifica

Cartella:

* Tieni premuta la cartella nell'elenco delle cartelle e seleziona *Crea canale di notifica*
* Tieni premuta la cartella nell'elenco delle cartelle e seleziona *Modifica canale di notifica* per modificare il suono della notifica

Mittente:

* Apri un messaggio dal mittente ed espandilo
* Espandi la sezione degli indirizzi toccando sulla freccia in giù
* Tocca sull'icona della campanella o modifica un canale di notifica e cambia il suono della notifica

L'ordine di precedenza è: suono del mittente, suono della cartella, suono del profilo e suono predefinito.

Impostare un suono di notifica per un profilo, una cartella o un mittente richiede Android 8 Oreo e successive ed è una funzionalità pro.

<br />

<a name="faq146"></a>
**(146) Come posso correggere gli orari scorretti dei messaggi?**

Poiché la data/ora di invio è opzionale e manipolabile dal mittente, FairEmail usa la data/ora di ricezione del server di default.

A volte la data/ora di ricezione del server è errata, principalmente perché i messaggi sono stati importati erroneamente da un altro server e, a volte, a causa di un bug nel server email.

In questi rari casi, è possibile far usare a FairEmail la data/ora dall'intestazione della *Data* (ora di invio) o dall'intestazione *Ricevuto* come soluzione. Questo si può cambiare nelle impostazioni avanzate del profilo: Impostazioni, tocca Configurazione manuale, tocca Profili, tocca il profilo, tocca Avanzate.

Questo non cambierà l'ora dei messaggi già sincronizzati. Per risolverlo, tieni premute le cartelle nell'elenco delle cartelle e seleziona *Elimina messaggi locali* e *Sincronizza ora*.

<br />

<a name="faq147"></a>
**(147) Cosa dovrei sapere sulle versioni di terze parti?**

Potresti esser venuto qui perché usi una build di terze parti di FairEmail.

Esiste il **supporto solo** sull'ultima versione del Play Store, l'ultima release di GitHub e la build di F-Droid, ma **solo se** il numero della versione della build di F-Droid è uguale al numero della versione dell'ultima release di GitHub.

F-Droid è irregolarmente costruito, il che può esser problematico quando c'è un aggiornamento importante. Dunque, si consiglia di passare alla release di GitHub.

La versione di F-Droid è creata dallo stesso codice sorgente, ma diversamente firmata. Questo significa che tutte le funzionalità sono disponibili anche nella versione di F-Droid, eccetto per l'uso della procedura guidata di configurazione rapida di Gmail, poiché Google ha approvato (e consente) solo la firma di un'app. Per tutti gli altri provider email, l'accesso di OAuth è disponibile solo nelle versioni del Play Store e le release di GitHub, poiché i provider email permettono l'uso di OAuth solo per le build ufficiali.

Nota che dovrai disinstallare la build di F-Droid prima di poter installare una release di GitHub, poiché Android si rifiuta di installare la stessa app con una firma differente per motivi di sicurezza.

Nota che la versione di GitHub controllerà automaticamente gli aggiornamenti. Quando desiderato, questo è disattivabile nelle impostazioni varie.

Sei pregato di [vedere qui](https://github.com/M66B/FairEmail/blob/master/README.md#user-content-downloads) per tutte le opzioni di download.

Se hai un problema con la build di F-Droid, sei pregato di controllare prima, se + presente una nuova versione di GitHub.

<br />

<a name="faq148"></a>
**(148) Come posso usare un profilo di Apple iCloud?**

Esiste un profilo integrato per Apple iCloud, quindi dovresti poter usare la procedura guidata di configurazione rapida (altro provider). Se necessario puoi trovare [qui](https://support.apple.com/en-us/HT202304) le giuste impostazioni per configurare manualmente un profilo.

Usando l'autenticazione a due fattori potresti dover usare una [password specifica dell'app](https://support.apple.com/en-us/HT204397).

<br />

<a name="faq149"></a>
**(149) Come funziona il widget di conteggio dei messaggi non letti?**

Il widget di conteggio dei messaggi non letti mostra il numero di messaggi non letti per tutti i profili o per un profilo selezionato, ma solo per le cartelle per cui sono abilitate le notifiche dei nuovi messaggi.

Toccare sulla notifica sincronizzerà tutte le cartelle per cui la sincronizzazione è abilitata e aprirà:

* la schermata di avvio quando tutti i profili sono selezionati
* un elenco delle cartelle quando un profilo specifico è stato selezionato e quando le notifiche dei nuovi messaggi sono abilitate per più cartelle
* un elenco di messaggi quando un profilo specifico è stato selezionato e quando le notifiche dei nuovi messaggi sono abilitate per una cartella

<br />

<a name="faq150"></a>
**(150) Puoi aggiungere l'annullamento degli inviti del calendario?**

Annullare gli inviti del calendario (rimuovere gli eventi del calendario) richiede i permessi di scrittura del calendario, che risulteranno nel garantire efficientemente i permessi di lettura e scrittura di *tutti* gli eventi del calendario di *tutti* i calendari.

Dato l'obiettivo di FairEmail, privacy e sicurezza, e dato che è facile rimuovere manualmente un evento del calendario, non è una buona idea richiedere questo permesso semplicemente per questo motivo.

Inserire nuovi eventi del calendario può esser fatto senza permessi con gli [intenti](https://developer.android.com/guide/topics/providers/calendar-provider.html#intents) speciali. Sfortunatamente, non esiste alcun intento per eliminare gli eventi esistenti del calendario.

<br />

<a name="faq151"></a>
**(151) Puoi aggiungere il backup/ripristino dei messaggi?**

Un client email è inteso per leggere e scrivere email, non per il backup e ripristino dei messaggi. Nota che rompere o perdere il tuo dispositivo, significa perdere i tuoi messaggi!

Invece, il provider/server email è responsabile dei backup.

Se vuoi fare da solo un backup, potresti usare uno strumento come [imapsync](https://imapsync.lamiral.info/).

Dalla versione 1.1556 è possibile esportare tutti i messaggi di una cartella POP3 nel formato mbox in base a [RFC4155](https://www.ietf.org/rfc/rfc4155.txt), che potrebbe esser utile per salvare i messaggi inviati se il server email non lo fa.

Se vuoi importare un file mbox a un profilo email esistente, puoi usare Thunderbird su un computer fisso e l'addon [ImportExportTools](https://addons.thunderbird.net/nl/thunderbird/addon/importexporttools/).

<br />

<a name="faq152"></a>
**(152) Come posso inserire un gruppo di contatto?**

Puoi inserire gli indirizzi email di tutti i contatti in un gruppo di contatto tramite il menu a tre puntini del compositore dei messaggi.

Puoi definire i gruppi di contatto con l'app Rubrica di Android, sei pregato di vedere [qui](https://support.google.com/contacts/answer/30970) per le istruzioni.

<br />

<a name="faq153"></a>
**(153) Perché l'eliminazione permanente del messaggio di Gmail non funziona?**

Potresti dover modificare le [impostazioni IMAP di Gmail](https://mail.google.com/mail/u/0/#settings/fwdandpop) su un browser desktop per farlo funzionare:

* Quando contrassegno un messaggio in IMAP come eliminato: Eliminazione automatica disattivata - Attendi l'aggiornamento del server dal client.
* Quando un messaggio è contrassegnato come eliminato e rimosso dall'ultima cartella visibile di IMAP: Elimina immediatamente il messaggio per sempre

Nota che i messaggi archiviati sono eliminabili solo spostandoli prima dalla cartella del cestino.

Un po' di background: Gmail sembra avere una vista aggiuntiva del messaggio per IMAP, che può differire da quella principale.

Un'altra stranezza è che una stella (messaggio preferito) impostata tramite l'interfaccia web non è removibile con il comando IMAP

```
STORE <message number> -FLAGS (\Flagged)
```

D'altra parte, una stella impostata tramite IMAP è mostrata nell'interfaccia web ed è removibile tramite IMAP.

<br />

<a name="faq154"></a>
**~~(154) Puoi aggiungere i favicon come foto di contatto?~~**

~~Oltre al fatto che un [favicon](https://en.wikipedia.org/wiki/Favicon) potrebbe esser condiviso da molti indirizzi email con lo stesso nome di dominio~~ ~~e dunque non sia direttamente correlato a un indirizzo email, i favicon sono utilizzabili per monitorarti.~~

<br />

<a name="faq155"></a>
**(155) Cos'è un file winmail.dat?**

Un file *winmail.dat* è inviato da un client di Outlook configurato erroneamente. Si tratta di un formato del file specifico di Microsoft ([TNEF](https://en.wikipedia.org/wiki/Transport_Neutral_Encapsulation_Format)) contenente un messaggio e possibilmente allegati.

Puoi trovare qualche altra informazione su questo file [qui](https://support.mozilla.org/en-US/kb/what-winmaildat-attachment).

Puoi visualizzarlo ad esempio con l'app Android [Letter Opener](https://play.google.com/store/apps/details?id=app.letteropener).

<br />

<a name="faq156"></a>
**(156) Come posso configurare un profilo Office 365?**

Un profilo di Office 365 è configurabile tramite la procedura guidata di configurazione rapida e selezionando *Office 365 (OAuth)*.

Se la procedura guidata si conclude con *AUTHENTICATE fallita*, IMAP e/o SMTP potrebbero esser disabilitati per il profilo. In questo caso dovresti chiedere all'amministratore di abilitare IMAP e SMTP. La procedura è documentata [qui](https://docs.microsoft.com/en-in/exchange/troubleshoot/configure-mailboxes/pop3-imap-owa-activesync-office-365).

Se hai abilitato i *predefiniti di sicurezza* nella tua organizzazione, potresti dover abilitare il protocollo SMTP AUTH. Sei pregato di [vedere qui](https://docs.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/authenticated-client-smtp-submission) come farlo.

<br />

<a name="faq157"></a>
**(157) Come posso configurare un profilo Free.fr?**

Siete pregati di [vedere qui](https://free.fr/assistance/597.html) per le istruzioni.

**SMTP è disattivato di default**, siete pregati di [vedere qui](https://free.fr/assistance/2406.html) come è attivabile.

Siete pregati di [vedere qui](http://jc.etiemble.free.fr/abc/index.php/trucs-astuces/configurer-smtp-free-fr) per una guida dettagliata.

<br />

<a name="faq103"></a>
<a name="faq158"></a>
**(158) Che fotocamera / registratore audio consigli?**

Per scattare foto e registrare audio sono necessari un'app della fotocamera e una di registrazione audio. Le seguenti app sono fotocamere e registratori audio open source:

* [Open Camera](https://play.google.com/store/apps/details?id=net.sourceforge.opencamera) ([F-Droid](https://f-droid.org/en/packages/net.sourceforge.opencamera/))
* [Audio Recorder versione 3.3.24+](https://play.google.com/store/apps/details?id=com.github.axet.audiorecorder) ([F-Droid](https://f-droid.org/packages/com.github.axet.audiorecorder/))

Per registrare note vocali, etc., il registratore audio deve supportare [MediaStore.Audio.Media.RECORD_SOUND_ACTION](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media#RECORD_SOUND_ACTION). Stranamente, gran parte dei registratori audio sembra non supportare quest'azione standard di Android.

<br />

<a name="faq159"></a>
**(159) Cosa sono gli elenchi di protezione del tracciatore di Disconnect?**

Sei pregato di vedere [qui](https://disconnect.me/trackerprotection) per ulteriori informazioni sugli elenchid i protezione del tracciatore di Disconnect.

Dopo aver scaricato gli elenchi nelle impostazioni di privacy, gli elenchi sono opzionalmente utilizzabili:

* per avvisare sui link di monitoraggio alla loro apertura
* per riconoscere le immagini di monitoraggio nei messaggi

Le immagini di monitoraggio saranno disabilitate solo se l'opzione 'disabilita' principale corrispondente è abilitata.

Le immagini di monitoraggio non saranno riconosciute quando il dominio è classificato come '*Contenuto*', vedi [qui](https://disconnect.me/trackerprotection#trackers-we-dont-block) per ulteriori informazioni.

Questo comando è inviabile a FairEmail da un'app di automazione per aggiornare gli elenchi di protezione:

```
(adb shell) am start-foreground-service -a eu.faircode.email.DISCONNECT.ME
```

Aggiornarli una volta a settimana sarà probabilmente sufficiente, sei pregato di vedere [qui](https://github.com/disconnectme/disconnect-tracking-protection/commits/master) per le modifiche recenti agli elenchi.

<br />

<a name="faq160"></a>
**(160) Puoi aggiungere l'eliminazione permanente dei messaggi senza conferma?**

L'eliminazione permanente significa che i messaggi saranno *irreversibilmente* persi e per prevenirlo dal succedere accidentalmente, ciò necessita sempre di esser confermato. Anche con una conferma, alcune persone molto arrabbiate che hanno perso alcuni dei loro messaggi per colpa propria mi hanno contattato, che è stata un'esperienza piuttosto spiacevole :-(

Avanzate: la bandiera di eliminazione di IMAP con il comando EXPUNGE (ELIMINA) non è supportabile perché sia i server email che non tutte le persone possono gestirla, rischiando perdite impreviste di messaggi. Un fattore complicante è che non tutti i server email supportano [UID EXPUNGE](https://tools.ietf.org/html/rfc4315).

Dalla versione 1.1485 è possibile abilitare temporaneamente la modalità di debug nelle impostazioni varie per disabilitare l'eliminazione dei messaggi. Nota che i messaggi con un flag *\Eliminato* non saranno mostrati in FairEmail.

<br />

<a name="faq161"></a>
**(161) Puoi aggiungere un'impostazione per modificare i colori primari e secondari?***

Se potessi, aggiungerei subito un'impostazione per modificare i colori primari e secondari, ma sfortunatamente i temi di Android sono fissi, vedi ad esempio [qui](https://stackoverflow.com/a/26511725/1794097), quindi è impossibile.

<br />

<a name="faq162"></a>
**(162) IMAP NOTIFY è supportato?***

Sì, [IMAP NOTIFY](https://tools.ietf.org/html/rfc5465) è stato supportato dalla versione 1.1413.

Il supporto a IMAP NOTIFY significa che le notifiche per i messaggi aggiunti, modificati o eliminati di tutte le cartelle *iscritte* saranno richieste e se una notifica è ricevuta per una cartella iscritta, che la cartella sarà sincronizzata. La sincronizzazione per le cartelle iscritte può dunque esser disabilitata, salvando le connessioni della cartella al server email.

**Importante**: i messaggi push (=sincronizza sempre) per la posta in arrivo e la gestione delle iscrizioni (impostazioni di ricevimento) devono esser abilitati.

**Importante**: gran parte dei server email non lo supportano! Puoi controllare il registro tramite il menu di navigazione se un server email supporta la funzionalità NOTIFY.

<br />

<a name="faq163"></a>
**(163) Cos'è la classificazione dei messaggi?**

*Questa è una funzionalità sperimentale!*

La classificazione dei messaggi tenterà di raggruppare automaticamente le email in classi, in base ai loro contenuti, usando le [Statistiche Bayesiane](https://en.wikipedia.org/wiki/Bayesian_statistics) (Inferenza Bayesiana). Nel contesto di FairEmail, una cartella è una classe. Quindi, ad esempio, la posta in arrivo, la cartella di spam, una cartella di 'marketing', etc.

Puoi abilitare la classificazione dei messaggi nelle impostazioni varie. Questo abiliterà la modalità di solo 'apprendimento'. Il classificatore 'imparerà' dai nuovi messaggi nelle cartelle di posta in arrivo e spam di default. La proprietà della cartella *Classifica i nuovi messaggi in questa cartella* abiliterà o disabiliterà la modalità di 'apprendimento' per una cartella. Puoi cancellare i messaggi locali (tieni premuta una cartella nell'elenco delle cartelle di un profilo) e risincronizzare i messaggi per classificare quelli esistenti.

Ogni cartella ha un'opzione *Sposta automaticamente i messaggi a questa cartella* ('classificazione automatica' in breve). Quando è attivata, i nuovi messaggi in altre cartelle che il classificatore pensa appartengano a quella cartella saranno spostati automaticamente.

L'opzione *Usa filtro antispam locale* nella finestra di segnalazione dello spam attiverà la classificazione dei messaggi nelle impostazioni varie e la classificazione automatica per la cartella di spam. Sei pregato di capire che questa non è una sostituzione del filtro antispam del server email e può risultare in [falsi positivi e falsi negativi](https://en.wikipedia.org/wiki/False_positives_and_false_negatives). Vedi anche [questa FAQ](#user-content-faq92).

Un esempio pratico: supponi che esista una cartella di 'marketing' e la classificazione automatica dei messaggi sia abilitata per questa cartella. Ogni volta che sposti un messaggio in questa cartella, addestrerai FairEmail sul fatto che simili messaggi appartengano a questa cartella. Ogni volta che sposti un messaggio al di fuori di questa cartella, addestrerai FairEmail che messaggi simili non appartengono a questa cartella. Dopo aver spostato dei messaggi nella cartella di 'marketing', FairEmail inizierà a spostare i messaggi simili, automaticamente, in questa cartella. O, viceversa, dopo averne spostati alcuni al di fuori della cartella di 'marketing', FairEmail smetterà di spostare automaticamente i messaggi simili al suo interno. Questo funzionerà meglio con i messaggi con contenuti simili (indirizzi email, oggetto e testo del messaggio).

La classificazione dovrebbe esser considerata come una migliore ipotesi, potrebbe essere un'ipotesi errata, o il classificatore potrebbe non esser abbastanza sicuro di farne una. Se il classificatore è insicuro, lascerà semplicemente un'email dove si trova.

Per prevenire che il server email sposti ancora e ancora un messaggio nella cartella di spam, la classificazione automatica al di fuori della cartella di spam non sarà effettuata.

Il classificatore dei messaggi calcola la probabilità che un messaggio appartenga a una cartella (classe). Esistono due opzioni nelle impostazioni varie che controllano se un messaggio sarà automaticamente spostato in una cartella, ammesso che la classificazione automatica sia abilitata per la cartella:

* *Probabilità minima della classe*: un messaggio sarà spostato solo quando la sicurezza che appartenga a una cartella è maggiore di questo valore (predefinito al 15%)
* *Differenza minima della classe*: un messaggio sarà spostato quando la differenza di confidenza nell'appartenenza a una classe e la successiva più probabile è maggiore di questo valore (predefinito al 50%)

Entrambe le condizioni devono esser soddisfatte prima dello spostamento di un messaggio.

Considerando i valori predefiniti dell'opzione:

* Mele al 40% e banane al 30% sarebbero scartate perché la differenza di 25% è inferiore al minimo del 50%
* Mele al 10% e banane al 5% sarebbero scartate per la probabilità che le mele siano inferiori al minimo di 15%
* Mele al 50% e banane al 20% risulterebbe nella selezione delle mele

La classificazione è ottimizzata per usare meno risorse possibili, ma userà inevitabilmente dell'energia extra della batteria.

Puoi eliminare tutti i dati di classificazione disattivandola tre volte nelle impostazioni varie.

Le [regole del filtro](#user-content-faq71) saranno eseguite prima della classificazione.

La classificazione dei messaggi è una funzionalità pro, tranne per la cartella di spam.

<br />

<a name="faq164"></a>
**(164) Puoi aggiungere i temi personalizzabili?**

Sfortunatamente, Android [non supporta](https://stackoverflow.com/a/26511725/1794097) i temi dinamici, il che significa che tutti i temi devono [essere predefiniti](https://github.com/M66B/FairEmail/blob/master/app/src/main/res/values/styles.xml).

Poiché per ogni tema serve una variante leggera, scura e nera, non è fattibile aggiungere un tema predefinito per ogni combinazione di colori (letteralmente milioni).

Peraltro, un tema è più che solo pochi colori. Per esempio i temi con un colore secondario giallo usano un colore dei link più scuro per avere abbastanza contrasto.

I colori del tema si basano sul cerchio dei colori di [Johannes Itten](https://en.wikipedia.org/wiki/Johannes_Itten).

<br />

<a name="faq165"></a>
**(165) Android Auto è supportato?**

Sì, Android Auto è supportato, ma solo con la versione di GitHub, sei pregato di [vedere qui](https://forum.xda-developers.com/t/app-5-0-fairemail-fully-featured-open-source-privacy-oriented-email-app.3824168/post-83801249) perché.

Per il supporto delle notifiche (messaggistica), dovrai abilitare le seguenti opzioni di notifica:

* *Usa il formato di notifica 'stile di messaggistica' di Android*
* Azioni di notifica: *Risposta diretta* e (contrassegna come) *Letto*

Se vuoi, puoi anche abilitare altre azioni di notifica, ma non sono supportate da Android Auto.

La guida degli sviluppatori è [qui](https://developer.android.com/training/cars/messaging).

<br />

<a name="faq166"></a>
**(166) Posso posticipare un messaggio su più dispositivi?**

Prima di tutto, non esiste uno standard per posticipare i messaggi, quindi tutte le implementazioni di posticipazione sono soluzioni personalizzate.

Alcuni provider email, come Gmail, spostano i messaggi posticipati a una cartella speciale. Sfortunatamente, le app di terze parti non hanno alcun accesso a questa cartella speciale.

Spostando un messaggio a un'altra cartella e indietro potrebbe fallire ed essere impossibile senza alcuna connessione a internet. Questo è problematico perché un messaggio è positicipabile solo dopo averlo spostato.

Per prevenire questi problemi, la posticipazione è effettuata localmente sul dispositivo, nascondendo il messaggio mentre è posticipato. Sfortunatamente, è impossibile nascondere i messaggi anche sul server email.

<br />

<h2><a name="get-support"></a>Ricevi supporto</h2>

FairEmail è supportata solo su smartphone e tablet Android e ChromeOS.

Sono supportate solo le ultime versioni del Play Store e di GitHub. La build di F-Droid è supportata solo se il numero della versione corrisponde a quello dell'ultima versione di GitHub. Questo significa anche che il downgrade non è supportato.

Non c'è alcun supporto su cose non direttamente correlate a FairEmail.

Non c'è alcun supporto sulle build e lo sviluppo di cose per conto proprio.

Le funzionalità richieste dovrebbero:

* essere utili a gran parte delle persone
* non complicare l'uso di FairEmail
* adattarsi alla filosofia di FairEmail (orientate alla privacy, con un occhio alla sicurezza)
* conformarsi agli standard comuni (IMAP, SMTP, ecc.)

Le funzionalità non soddisfacenti questi requisiti saranno probabilmente respinte. Questo serve anche a mantenere manutenzione e supporto fattibili a lungo termine.

Se hai una domanda, vuoi richiedere una funzionalità o segnalare un bug **sei pregato di usare [questo form](https://contact.faircode.eu/?product=fairemailsupport)**.

I problemi di GitHub sono disabilitati a causa di usi impropri frequenti.

<br />

Copyright &copy; 2018-2021 Marcel Bokhorst.
