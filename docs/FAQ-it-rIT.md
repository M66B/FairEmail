<a name="top"></a>

# Supporto FairEmail

Se hai una domanda, sei pregato di controllare prima le seguenti domande frequenti. [At the bottom](#user-content-get-support), you can find out how to ask other questions, request features, and report bugs.

Se hai una domanda, sei pregato di controllare prima le seguenti domande frequenti (FAQ). [Ganz unten erfahren Sie](#user-content-get-support), wie Sie weitere Fragen stellen, Funktionen anfordern und Fehler melden können.

## Indice

* [Autorizzare profili](#user-content-authorizing-accounts)
* [Come...?](#user-content-howto)
* [Problemi noti](#user-content-known-problems)
* [Funzionalità pianificate](#user-content-planned-features)
* [Funzionalità frequentemente richieste](#user-content-frequently-requested-features)
* [Domande Frequenti](#user-content-frequently-asked-questions)
* [Richiedi supporto](#user-content-get-support)

## Autorizzare profili

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

## Problemi noti

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
* A [bug in OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2688) causes invalid PGP signatures when using a hardware token.

## Funzionalità pianificate

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

## Funzionalità frequentemente richieste

Il design si basa su molte discussioni e, se lo desideri, puoi discuterne anche tu [in questo forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168). Lo scopo del design è di essere minimalista (senza menu, pulsanti etc. inutili) e non distraente (niente colori stravaganti, animazioni, etc.). Tutte le cose mostrate dovrebbero esser utili in un modo o nell'altro e attentamente posizionate per un facile uso. Caratteri, dimensioni, colori, etc. dovrebbero essere di design materiale quando possibile.

## Domande Frequenti

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
* [(18) Perché l'anteprima del messaggio non viene sempre mostrata?](#user-content-faq18)
* [(19) Perché le funzionalità pro sono così costose?](#user-content-faq19)
* [(20) Posso ottenere un rimborso?](#user-content-faq20)
* [(21) Come abilitare la luce delle notifiche?](#user-content-faq21)
* [(22) Cosa significa errore dell' account/cartella ... ?](#user-content-faq22)
* [(23) Perché mi arriva una segnalazione.. ?](#user-content-faq23)
* [(24) Cos'è la navigazione dei messaggi sul server?](#user-content-faq24)
* [(25) Perché non posso selezionare/aprire/salvare un'immagine, allegato o file?](#user-content-faq25)
* [(26) Posso aiutarvi a tradurre FairEmail nella mia lingua?](#user-content-faq26)
* [(27) Come posso distinguere le immagini integrate da quelle esterne?](#user-content-faq27)
* [(28) Come posso gestire le notifiche della barra di stato?](#user-content-faq28)
* [(29) Come posso ricevere notifiche di messaggi per altre cartelle?](#user-content-faq29)
* [(30) Come posso usare le impostazioni rapide previste?](#user-content-faq30)
* [(31) Come posso usare i collegamenti rapidi previsti?](#user-content-faq31)
* [(32) Come posso controllare se leggere un'email è davvero sicuro?](#user-content-faq32)
* [(33) Perché gli indirizzi modificati dei mittenti non funzionano?](#user-content-faq33)
* [(34) Come vengono abbinate le identità?](#user-content-faq34)
* [(35) Perché dovrei fare attenzione alla visualizzazione di immagini, allegati, il messaggio originale e ad aprire collegamenti?](#user-content-faq35)
* [(36) Come sono crittografati i file delle impostazioni?](#user-content-faq36)
* [(37) Come vengono memorizzate le password?](#user-content-faq37)
* [(39) Come posso ridurre l'uso della batteria di FairEmail?](#user-content-faq39)
* [(40) Come posso ridurre l'uso dei dati di FairEmail?](#user-content-faq40)
* [(41) Come posso correggere l'errore 'Handshake non riuscito'?](#user-content-faq41)
* [(42) È possibile aggiungere un nuovo provider all'elenco dei provider?](#user-content-faq42)
* [(43) Riesci a mostrare l'originale ... ?](#user-content-faq43)
* [(44) È possibile mostrare le foto dei contatti/ identicon nella cartella 'messaggi inviati'?](#user-content-faq44)
* [(45) Come posso risolvere 'Questa chiave non è disponibile. Per usarla, devi importarla come una delle tue!' ?](#user-content-faq45)
* [(46) Perché la lista dei messaggi continua ad aggiornarsi?](#user-content-faq46)
* [(47) Come risolvo l'errore 'Nessun account primario o nessuna cartella bozze'?](#user-content-faq47)
* [~~(48) Come posso risolvere l'errore 'Nessun account primario o nessuna cartella di archivio' ?~~](#user-content-faq48)
* [(49) Come faccio a risolvere 'Un'app obsoleta ha inviato un percorso di file invece di un flusso di file'?](#user-content-faq49)
* [(50) È possibile aggiungere un'opzione per sincronizzare tutti i messaggi?](#user-content-faq50)
* [(51) Come vengono ordinate le cartelle?](#user-content-faq51)
* [(52) Perché ci vuole tempo per riconnettersi a un account?](#user-content-faq52)
* [(53) Si può attaccare la barra d'azione del messaggio in alto/in basso?](#user-content-faq53)
* [~~(54) Come uso un prefisso dello spazio del nome?~~](#user-content-faq54)
* [(55) Come posso contrassegnare tutti i messaggi come letti / spostati o eliminare tutti i messaggi?](#user-content-faq55)
* [(56) Puoi aggiungere il supporto per JMAP?](#user-content-faq56)
* [(57) Posso usare HTML nelle firme?](#user-content-faq57)
* [(58) Cosa significa un'icona dell'email aperta/chiusa?](#user-content-faq58)
* [(59) I messaggi originali possono essere aperti nel browser?](#user-content-faq59)
* [(60) Lo sapevi ...?](#user-content-faq60)
* [(61) Perché alcuni messaggi sono mostrati oscurati?](#user-content-faq61)
* [(62) Quali metodi di autenticazione sono supportati?](#user-content-faq62)
* [(63) Come sono ridimensionate le immagini per la visualizzazione su schermi?](#user-content-faq63)
* [~~(64) Puoi aggiungere azioni personalizzate per lo scorrimento a sinistra/destra?~~](#user-content-faq64)
* [(65) Perché alcuni allegati sono mostrati oscurati?](#user-content-faq65)
* [(66) FairEmail è disponibile nella Libreria di Google Play Family?](#user-content-faq66)
* [(67) Come posso posticipare le conversazioni?](#user-content-faq67)
* [~~(68) Perché Adobe Acrobat reader non apre gli allegati PDF / le app di Microsoft non aprono i documenti allegati?~~](#user-content-faq68)
* [(69) Puoi aggiungere lo scorrimento in su su un messaggio nuovo?](#user-content-faq69)
* [(70) Quando saranno auto-espansi i messaggi?](#user-content-faq70)
* [(71) Come uso le regole del filtro?](#user-content-faq71)
* [(72) Cosa sono le identità/i profili principali?](#user-content-faq72)
* [(73) È sicuro/efficiente spostare i messaggi attraverso i profili?](#user-content-faq73)
* [(74) Perché vedo messaggi duplicati?](#user-content-faq74)
* [(75) Puoi creare una versione iOS, Windows, Linux, etc?](#user-content-faq75)
* [(76) Cosa fa 'Cancella messaggi locali'?](#user-content-faq76)
* [(77) Perché a volte i messaggi sono mostrati con un piccolo ritardo?](#user-content-faq77)
* [(78) Come uso i programmi?](#user-content-faq78)
* [(79) Come uso la sincronizzazione su richiesta (manuale)?](#user-content-faq79)
* [~~(80) Come risolvo l'errore 'Impossibile caricare BODYSTRUCTURE'?~~](#user-content-faq80)
* [~~(81) Puoi rendere scuro lo sfondo del messaggio originale nel tema scuro?~~](#user-content-faq81)
* [(82) Cos'è un'immagine di tracciamento?](#user-content-faq82)
* [(84) A che servono i contatti locali?](#user-content-faq84)
* [(85) Perché un'identità non è disponibile?](#user-content-faq85)
* [~~(86) Cosa sono le 'caratteristiche di privacy extra'?~~](#user-content-faq86)
* [(87) Cosa significa 'credenziali non valide'?](#user-content-faq87)
* [(88) Come posso usare un account Yahoo, AOL o Sky?](#user-content-faq88)
* [(89) Come invio messaggi di solo testo semplice?](#user-content-faq89)
* [(90) Perché alcuni testi sono collegati senza essere un collegamento?](#user-content-faq90)
* [~~(91) Puoi aggiungere la sincronizzazione periodica per risparmiare energia della batteria?~~](#user-content-faq91)
* [(92) Puoi aggiungere il filtro dello spam, la verifica della firma DKIM e l'autorizzazione SPF?](#user-content-faq92)
* [(93) Puoi consentire l'installazione/archiviazione dei dati su supporti di archiviazione esterna (sdcard)?](#user-content-faq93)
* [(94) Cosa significa la striscia rossa/arancione alla fine dell'intestazione?](#user-content-faq94)
* [(95) Perché non tutte le app sono mostrate durante la selezione di un allegato o immagine?](#user-content-faq95)
* [(96) Dove posso trovare le impostazioni IMAP e SMTP?](#user-content-faq96)
* [(97) Cos'è 'pulizia'?](#user-content-faq97)
* [(98) Perché posso ancora scegliere i contatti dopo aver revocato i permessi dei contatti?](#user-content-faq98)
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

Gran parte, se non tutte, le altre app email non mostrano una notifica, con lo 'effetto laterale' che i nuovi messaggi spesso non sono o sono segnalati e inviati in ritardo.

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

Il tuo nome utente potrebbe esser facilmente indovinato, quindi non è sicuro.

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

Nota che la firma S/MIME con altri algoritmi oltre RSA è supportata, ma sappi che altri client email potrebbero non supportarla. La crittografia S/MIME è possibile solo con algoritmi simmetrici, il che significa in pratica usando RSA.

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
* *554 5.7.1 Servizio non disponibile; Host del client xxx.xxx.xxx.xxx bloccato*, sei pregato [di vedere qui](https://docs.gandi.net/en/gandimail/faq/error_types/554_5_7_1_service_unavailable.html)
* *501 Errore di sintassi – linea troppo lunga* è spesso causata dall'utilizzo di una lunga intestazione Autocrypt
* *503 5.5.0 Destinatario già specificato* significa principalmente che un indirizzo è in uso sia come indirizzo TO che CC
* *554 5.7.1 ... non è consentito trasmettere* significa che il server di posta elettronica non riconosce il nome utente/indirizzo email. Si prega di controllare il nome host e il nome utente/indirizzo email nelle impostazioni di identità.
* *550 Messaggio di spam rifiutato perché l'IP è elencato per ...* significa che il server dell'email ha rifiutato di inviare un messaggio dall'indirizzo di rete attuale (pubblico) perché usato erroneamente per inviare spam da qualcun altro (si spera) in precedenza. Sei pregato di provare ad abilitare la modalità aereo per 10 minuti per acquisire un nuovo indirizzo di rete.
* *550 Siamo spiacenti, impossibile inviare la tua email. La materia del soggetto, un collegamento o un allegato contiene potenzialmente spam, phishing o malware.* significa che il provider dell'email considera un messaggio in uscita come dannoso.
* *571 5.7.1 Il messaggio contiene spam o virus o il mittente è bloccato ...* significa che il server email ha considerato un messaggio in uscita come spam. Questo probabilmente significa che i filtri antispam del server email sono troppo rigorosi. Dovrai contattare il provider email per supporto a riguardo.
* *451 4.7.0 Errore temporaneo del server. Sei pregato di riprovare più tardi. PRX4 ...*: per favore [vedi qui](https://www.limilabs.com/blog/office365-temporary-server-error-please-try-again-later-prx4) o [vedi qui](https://judeperera.wordpress.com/2019/10/11/fixing-451-4-7-0-temporary-server-error-please-try-again-later-prx4/).
* *571 5.7.1 Accesso al relè negato*: sei pregato di ricontrollare il nome utente e l'indirizzo email nelle impostazioni avanzate di identità (tramite la configurazione manuale).

Se vuoi usare il server SMTP di Gmail per correggere un filtro anti-spam in uscita troppo rigido o migliorare la consegna dei messaggi:

* Verifica il tuo indirizzo email [qui](https://mail.google.com/mail/u/0/#settings/accounts) (dovrai usare un browser desktop)
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
**(23) Perché ricevo l'avviso ... ?**

*Generale*

Gli avvisi sono messaggi di notifica inviati dai server email.

*Troppe connessioni simultanee* o *Numero massimo di connessioni superato*

Questo avviso sarà inviato quando ci sono troppe connessioni della cartella per lo stesso profilo email in contemporanea.

Sono cause possibili:

* Ci sono client email multipli connessi allo stesso profilo
* Lo stesso client email è connesso diverse volte allo stesso profilo
* Le connessioni precedenti sono state terminate bruscamente per esempio perdendo bruscamente la connettività internet

Prima prova ad attendere un po' di tempo per vedere se il problema si risolve da solo, altrimenti:

* o passa alla verifica periodica dei messaggi nelle impostazioni di ricezione, che risulterà nell'apertura delle cartelle una per volta
* o imposta alcune cartelle a sondaggio invece di sincronizzare (tenere premuto a lungo la cartella nell'elenco della cartella, modificare le proprietà)

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
**(25) Perché non posso selezionare/aprire/salvare un'immagine, allegato o un file?**

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

* Servizio: usato per la notifica del servizio di sincronizzazione, vedi anche [questa FAQ](#user-content-faq2)
* Invio: usato per le notifiche del servizio di invio
* Notifiche: usato per le notifiche dei nuovi messaggi
* Avviso: usato per le notifiche di avviso
* Errore: usato per le notifiche di errore

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
* mostrare il numero di nuovi messaggi e contrassegnarli come visti (non letti)

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

* La chiave di 256 bit è generata con *PBKDF2WithHmacSHA1* usando un salt casuale sicuro di 128 bit e 65536 iterazioni
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

* Controlla che i messaggi vecchi siano stati rimossi dal server: disabilitato
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
**(43) Puoi mostrare l'originale di ... ?**

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
**(50) Puoi aggiungere un'opzione per sincornizzare tutti i messaggi?**

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
**(60) Sapevi ... ?**

* Sapevi che i messaggi stellati possono sempre essere sincronizzati/mantenuti? (abilitabile nelle impostazioni di ricezione)
* Sapevi che puoi premere a lungo l'icona 'scrivi messaggio' per andare alla cartella delle bozze?
* Sapevi che esiste un'opzione avanzata per contrassegnare i messaggi come letti quando sono spostati? (anche archiviare e cestinare è spostare)
* Sapevi che puoi selezionare il testo (o un indirizzo email) in ogni app sulle versioni recenti di Android e farle cercare a FairEmail?
* Sapevi che FairEmail ha una modalità tablet? Ruota il tuo dispositivo in modalità orizzontale e le conversazioni saranno aperte in una seconda colonna in caso di abbastanza spazio su schermo.
* Sapevi che puoi premere a lungo un modello di risposta per creare una bozza dal modello?
* Sapevi che puoi tenere premuto mentre scorri per selezionare un intervallo di messaggi?
* Sapevi che puoi provare a inviare i messaggi usando -scorri-il-dito-in-basso-per-aggironare nella casella della posta in uscita?
* Sapevi che puoi scorrere a sinistra o destra una conversazione per andare alla successiva o precedente?
* Sapevi che puoi toccare su un'immagine per vedere da dove verrà scaricata?
* Sapevi che puoi tenere premuto a lungo l'icona della cartella nella barra di azione per selezionare un account?
* Sapevi che puoi tenere premuto a lungo l'icona stella nel thread della conversazione per impostare una stella colorata?
* Sapevi che puoi aprire il pannello di navigazione scorrendo da sinistra, anche visualizzando una conversazione?
* Sapevi che puoi tenere premuto a lungo l'icona della persona per mostrare/nascondere i campi CC/BCC e ricordare lo stato di visibilità per la prossima volta?
* Sapevi che puoi inserire gli indirizzi email di un gruppo di contatti di Android tramite il menu a scorrimento a tre puntini?
* Sapevi che se selezioni il testo e clicchi rispondi, solo il testo selezionato sarà citato?
* Sapevi che puoi tenere premuto a lungo le icone del cestino (sia nella barra di azione in fondo che nel messaggio) per eliminare permanentemente un messaggio o una conversazione? (versione 1.1368+)
* Sapevi che puoi tenere premuta l'azione di invio per mostrare la finestra di invio, anche se disabilitata?
* Sapevi che puoi tenere premuta l'icona dello schermo intero per mostrare solo il testo del messaggio originale?
* Did you know that you can long press the answer button to reply to the sender? (since version 1.1562)

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

I [Certificati Gmail](https://en.wikipedia.org/wiki/Client_certificate) sono selezionabili nelle impostazioni del profilo e dell'identità.

L'[Indicazione del Nome del Server](https://en.wikipedia.org/wiki/Server_Name_Indication) è supportata da [tutte le versioni Android supportate](https://developer.android.com/training/articles/security-ssl).

<br />

<a name="faq63"></a>
**(63) Come sono ridimensionate le immagini per la visualizzazione sugli schermi?**

Le immagini in linea o allegate grandi in [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) e [JPEG](https://en.wikipedia.org/wiki/JPEG) saranno ridimensionate automaticamente per la visualizzazione sugli schermi. Questo perché i messaggi email sono di dimensioni limitate, in base al provider, principalmente tra 10 e 50 MB. Le immagini saranno ridimensionate di default a una larghezza e altezza massima di circa 1440 pixel e salvate con un rapporto di compressione del 90%. Le immagini sono ridimensionate usando fattori interi per ridurre l'uso della memoria e mantenere la qualità dell'immagine. Il ridimensionamento automatico delle immagini in linea e/o allegate e la dimensione di destinazione massima dell'immagine sono configurabili nelle impostazioni di invio.

Se vuoi ridimensionare le immagini in base al caso, puoi usare [Send Reduced](https://f-droid.org/en/packages/mobi.omegacentauri.SendReduced/) o un app simile.

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

"*You can't share in-app purchases and free apps with your family members.*"

See [here](https://support.google.com/googleone/answer/7007852) under "*See if content is eligible to be added to Family Library*", "*Apps & games*".

<br />

<a name="faq67"></a>
**(67) How can I snooze conversations?**

Multiple select one of more conversations (long press to start multiple selecting), tap the three dot button and select *Snooze ...*. Alternatively, in the expanded message view use *Snooze ...* in the message three-dots 'more' menu or the time-lapse action in the bottom action bar. Select the time the conversation(s) should snooze and confirm by tapping OK. The conversations will be hidden for the selected time and shown again afterwards. You will receive a new message notification as reminder.

It is also possible to snooze messages with [a rule](#user-content-faq71), which will also allow you to move messages to a folder to let them be auto snoozed.

You can show snoozed messages by unchecking *Filter out* > *Hidden* in the three dot overflow menu.

You can tap on the small snooze icon to see until when a conversation is snoozed.

By selecting a zero snooze duration you can cancel snoozing.

Third party apps do not have access to the Gmail snoozed messages folder.

<br />

<a name="faq68"></a>
**~~(68) Why can Adobe Acrobat reader not open PDF attachments / Microsoft apps not open attached documents?~~**

~~Adobe Acrobat reader and Microsoft apps still expects full access to all stored files,~~ ~~while apps should use the [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) since Android KitKat (2013)~~ ~~to have access to actively shared files only. This is for privacy and security reasons.~~

~~You can workaround this by saving the attachment and opening it from the Adobe Acrobat reader / Microsoft app,~~ ~~but you are advised to install an up-to-date and preferably open source PDF reader / document viewer,~~ ~~for example one listed [here](https://github.com/offa/android-foss#-document--pdf-viewer).~~

<br />

<a name="faq69"></a>
**(69) Can you add auto scroll up on new message?**

The message list is automatically scrolled up when navigating from a new message notification or after a manual refresh. Always automatically scrolling up on arrival of new messages would interfere with your own scrolling, but if you like you can enable this in the settings.

<br />

<a name="faq70"></a>
**(70) When will messages be auto expanded?**

When navigation to a conversation one message will be expanded if:

* C'è solo un messaggio nella conversazione
* C'è esattamente un messaggio non letto nella conversazione
* C'è esattamente un messaggio stellato (preferito) nella conversazione (dalla versione 1.1508)

There is one exception: the message was not downloaded yet and the message is too large to download automatically on a metered (mobile) connection. You can set or disable the maximum message size on the 'connection' settings tab.

Duplicate (archived) messages, trashed messages and draft messages are not counted.

Messages will automatically be marked read on expanding, unless this was disabled in the individual account settings.

<br />

<a name="faq71"></a>
**(71) How do I use filter rules?**

You can edit filter rules by long pressing a folder in the folder list of an account (tap the account name in the navigation/side menu).

New rules will be applied to new messages received in the folder, not to existing messages. You can check the rule and apply the rule to existing messages or, alternatively, long press the rule in the rule list and select *Execute now*.

You'll need to give a rule a name and you'll need to define the order in which a rule should be executed relative to other rules.

You can disable a rule and you can stop processing other rules after a rule has been executed.

The following rule conditions are available:

* Il mittente contiene o il mittente è il contatto
* Il destinatario contiene
* L'oggetto contiene
* Ha allegati (opzionale di tipo specifico)
* L'intestazione contiene
* Tempo assoluto (ricevuto) tra (dalla versione 1.1540)
* Tempo relativo (ricevuto) tra

All the conditions of a rule need to be true for the rule action to be executed. All conditions are optional, but there needs to be at least one condition, to prevent matching all messages. If you want to match all senders or all recipients, you can just use the @ character as condition because all email addresses will contain this character. If you want to match a domain name, you can use as a condition something like *@example.org*

Note that email addresses are formatted like this:

`
"Somebody" <somebody@example.org>`

You can use multiple rules, possibly with a *stop processing*, for an *or* or a *not* condition.

Matching is not case sensitive, unless you use [regular expressions](https://en.wikipedia.org/wiki/Regular_expression). Please see [here](https://developer.android.com/reference/java/util/regex/Pattern) for the documentation of Java regular expressions. You can test a regex [here](https://regexr.com/).

Note that a regular expression supports an *or* operator, so if you want to match multiple senders, you can do this:

`
.*alice@example\.org.*|.*bob@example\.org.*|.*carol@example\.org.*`

Note that [dot all mode](https://developer.android.com/reference/java/util/regex/Pattern#DOTALL) is enabled to be able to match [unfolded headers](https://tools.ietf.org/html/rfc2822#section-3.2.3).

You can select one of these actions to apply to matching messages:

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

An error in a rule condition can lead to a disaster, therefore irreversible actions are not supported.

Rules are applied directly after the message header has been fetched, but before the message text has been downloaded, so it is not possible to apply conditions to the message text. Note that large message texts are downloaded on demand on a metered connection to save on data usage.

If you want to forward a message, consider to use the move action instead. This will be more reliable than forwarding as well because forwarded messages might be considered as spam.

Since message headers are not downloaded and stored by default to save on battery and data usage and to save storage space it is not possible to preview which messages would match a header rule condition.

Some common header conditions (regex):

* *.&ast;Auto-Submitted:.&ast;* [RFC3834](https://tools.ietf.org/html/rfc3834)
* *.&ast;Content-Type: multiparte/segnala.&ast;* [RFC3462](https://tools.ietf.org/html/rfc3462)

In the three-dots *more* message menu there is an item to create a rule for a received message with the most common conditions filled in.

The POP3 protocol does not support setting keywords and moving or copying messages.

Using rules is a pro feature.

<br />

<a name="faq72"></a>
**(72) What are primary accounts/identities?**

The primary account is used when the account is ambiguous, for example when starting a new draft from the unified inbox.

Similarly, the primary identity of an account is used when the identity is ambiguous.

There can be just one primary account and there can be just one primary identity per account.

<br />

<a name="faq73"></a>
**(73) Is moving messages across accounts safe/efficient?**

Moving messages across accounts is safe because the raw, original messages will be downloaded and moved and because the source messages will be deleted only after the target messages have been added

Batch moving messages across accounts is efficient if both the source folder and target folder are set to synchronize, else FairEmail needs to connect to the folder(s) for each message.

<br />

<a name="faq74"></a>
**(74) Why do I see duplicate messages?**

Some providers, notably Gmail, list all messages in all folders, except trashed messages, in the archive (all messages) folder too. FairEmail shows all these messages in a non obtrusive way to indicate that these messages are in fact the same message.

Gmail allows one message to have multiple labels, which are presented to FairEmail as folders. This means that messages with multiple labels will be shown multiple times as well.

<br />

<a name="faq75"></a>
**(75) Can you make an iOS, Windows, Linux, etc version?**

A lot of knowledge and experience is required to successfully develop an app for a specific platform, which is why I develop apps for Android only.

<br />

<a name="faq76"></a>
**(76) What does 'Clear local messages' do?**

The folder menu *Clear local messages* removes messages from the device which are present on the server too. It does not delete messages from the server. This can be useful after changing the folder settings to not download the message content (text and attachments), for example to save space.

<br />

<a name="faq77"></a>
**(77) Why are messages sometimes shown with a small delay?**

Depending on the speed of your device (processor speed and maybe even more memory speed) messages might be displayed with a small delay. FairEmail is designed to dynamically handle a large number of messages without running out of memory. This means that messages needs to be read from a database and that this database needs to be watched for changes, both of which might cause small delays.

Some convenience features, like grouping messages to display conversation threads and determining the previous/next message, take a little extra time. Note that there is no *the* next message because in the meantime a new message might have been arrived.

When comparing the speed of FairEmail with similar apps this should be part of the comparison. It is easy to write a similar, faster app which just displays a lineair list of messages while possible using too much memory, but it is not so easy to properly manage resource usage and to offer more advanced features like conversation threading.

FairEmail is based on the state-of-the-art [Android architecture components](https://developer.android.com/topic/libraries/architecture/), so there is little room for performance improvements.

<br />

<a name="faq78"></a>
**(78) How do I use schedules?**

In the receive settings you can enable scheduling and set a time period and the days of the week *when* messages should be *received*. Note that an end time equal to or earlier than the start time is considered to be 24 hours later.

Automation, see below, can be used for more advanced schedules, like for example multiple synchronization periods per day or different synchronization periods for different days.

It is possible to install FairEmail in multiple user profiles, for example a personal and a work profile, and to configure FairEmail differently in each profile, which is another possibility to have different synchronization schedules and to synchronize a different set of accounts.

It is also possible to create [filter rules](#user-content-faq71) with a time condition and to snooze messages until the end time of the time condition. This way it is possible to *snooze* business related messages until the start of the business hours. This also means that the messages will be on your device for when there is (temporarily) no internet connection.

Note that recent Android versions allow overriding DND (Do Not Disturb) per notification channel and per app, which could be used to (not) silence specific (business) notifications. Please [see here](https://support.google.com/android/answer/9069335) for more information.

For more complex schemes you could set one or more accounts to manual synchronization and send this command to FairEmail to check for new messages:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL
```

For a specific account:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL --es account Gmail
```

You can also automate turning receiving messages on and off by sending these commands to FairEmail:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE
```

To enable/disable a specific account:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE --es account Gmail
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE --es account Gmail
```

Note that disabling an account will hide the account and all associated folders and messages.

To set the poll interval:

```
(adb shell) adb shell am start-foreground-service -a eu.faircode.email.INTERVAL --ei minutes nnn
```

Where *nnn* is one of 0, 15, 30, 60, 120, 240, 480, 1440. A value of 0 means push messages.

You can automatically send commands with for example [Tasker](https://tasker.joaoapps.com/userguide/en/intents.html):

```
Nuova attività: Qualcosa di riconoscibile
Categoria d'Azione: Intento Vario/Invio
Azione: eu.faircode.email.ENABLE
Target: Servizio
```

To enable/disable an account with the name *Gmail*:

```
Extra: account:Gmail
```

Account names are case sensitive.

Scheduling is a pro feature.

<br />

<a name="faq79"></a>
**(79) How do I use synchronize on demand (manual)?**

Normally, FairEmail maintains a connection to the configured email servers whenever possible to receive messages in real-time. If you don't want this, for example to be not disturbed or to save on battery usage, just disable receiving in the receive settings. This will stop the background service which takes care of automatic synchronization and will remove the associated status bar notification.

You can also enable *Synchronize manually* in the advanced account settings if you want to manually synchronize specific accounts only.

You can use pull-down-to-refresh in a message list or use the folder menu *Synchronize now* to manually synchronize messages.

If you want to synchronize some or all folders of an account manually, just disable synchronization for the folders (but not of the account).

You'll likely want to disabled [browse on server](#user-content-faq24) too.

<br />

<a name="faq80"></a>
**~~(80) How do I fix the error 'Unable to load BODYSTRUCTURE' ?~~**

~~The error message *Unable to load BODYSTRUCTURE* is caused by bugs in the email server,~~ ~~see [here](https://javaee.github.io/javamail/FAQ#imapserverbug) for more details.~~

~~FairEmail already tries to workaround these bugs, but if this fail you'll need to ask for support from your provider.~~

<br />

<a name="faq81"></a>
**~~(81) Can you make the background of the original message dark in the dark theme?~~**

~~The original message is shown as the sender has sent it, including all colors.~~ ~~Changing the background color would not only make the original view not original anymore, it can also result in unreadable messages.~~

<br />

<a name="faq82"></a>
**(82) What is a tracking image?**

Please see [here](https://en.wikipedia.org/wiki/Web_beacon) about what a tracking image exactly is. In short tracking images keep track if you opened a message.

FairEmail will in most cases automatically recognize tracking images and replace them by this icon:

![External image](https://github.com/M66B/FairEmail/blob/master/images/baseline_my_location_black_48dp.png)

Automatic recognition of tracking images can be disabled in the privacy settings.

<br />

<a name="faq84"></a>
**(84) What are local contacts for?**

Local contact information is based on names and addresses found in incoming and outgoing messages.

The main use of the local contacts storage is to offer auto completion when no contacts permission has been granted to FairEmail.

Another use is to generate [shortcuts](#user-content-faq31) on recent Android versions to quickly send a message to frequently contacted people. This is also why the number of times contacted and the last time contacted is being recorded and why you can make a contact a favorite or exclude it from favorites by long pressing it.

The list of contacts is sorted on number of times contacted and the last time contacted.

By default only names and addresses to whom you send messages to will be recorded. You can change this in the send settings.

<br />

<a name="faq85"></a>
**(85) Why is an identity not available?**

An identity is available for sending a new message or replying or forwarding an existing message only if:

* l'identità è impostata per la sincronizzazione (invia messaggi)
* il profilo associato è impostato per la sincronizzazione (ricevi messaggi)
* il profilo associato ha una cartella delle bozze

FairEmail will try to select the best identity based on the *to* address of the message replied to / being forwarded.

<br />

<a name="faq86"></a>
**~~(86) What are 'extra privacy features'?~~**

~~The advanced option *extra privacy features* enables:~~

* ~~Ricerca del proprietario dell'indirizzo IP di un link~~
* ~~Rilevamento e rimozione delle [immagini di tracciamento](#user-content-faq82)~~

<br />

<a name="faq87"></a>
**(87) What does 'invalid credentials' mean?**

The error message *invalid credentials* means either that the user name and/or password is incorrect, for example because the password was changed or expired, or that the account authorization has expired.

If the password is incorrect/expired, you will have to update the password in the account and/or identity settings.

If the account authorization has expired, you will have to select the account again. You will likely need to save the associated identity again as well.

<br />

<a name="faq88"></a>
**(88) How can I use a Yahoo, AOL or Sky account?**

The preferred way to set up a Yahoo account is by using the quick setup wizard, which will use OAuth instead of a password and is therefore safer (and easier as well).

To authorize a Yahoo, AOL, or Sky account you will need to create an app password. For instructions, please see here:

* [per Yahoo](https://help.yahoo.com/kb/generate-third-party-passwords-sln15241.html)
* [per AOL](https://help.aol.com/articles/Create-and-manage-app-password)
* [per Sky](https://www.sky.com/help/articles/getting-started-with-sky-yahoo-mail) (sotto *Altre app di email*)

Please see [this FAQ](#user-content-faq111) about OAuth support.

Note that Yahoo, AOL, and Sky do not support standard push messages. The Yahoo email app uses a proprietary, undocumented protocol for push messages.

Push messages require [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE) and the Yahoo email server does not report IDLE as capability:

```
Y1 CAPABILITY
* CAPABILITY IMAP4rev1 ID MOVE NAMESPACE XYMHIGHESTMODSEQ UIDPLUS LITERAL+ CHILDREN X-MSG-EXT UNSELECT OBJECTID
Y1 OK CAPABILITY completed
```

<br />

<a name="faq89"></a>
**(89) How can I send plain text only messages?**

By default FairEmail sends each message both as plain text and as HTML formatted text because almost every receiver expects formatted messages these days. If you want/need to send plain text messages only, you can enable this in the advanced identity options. You might want to create a new identity for this if you want/need to select sending plain text messages on a case-by-case basis.

<br />

<a name="faq90"></a>
**(90) Why are some texts linked while not being a link?**

FairEmail will automatically link not linked web links (http and https) and not linked email addresses (mailto) for your convenience. However, texts and links are not easily distinguished, especially not with lots of [top level domains](https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains) being words. This is why texts with dots are sometimes incorrectly recognized as links, which is better than not recognizing some links.

Links for the tel, geo, rtsp and xmpp protocols will be recognized too, but links for less usual or less safe protocols like telnet and ftp will not be recognized. The regex to recognize links is already *very* complex and adding more protocols will make it only slower and possibly cause errors.

Note that original messages are shown exactly as they are, which means also that links are not automatically added.

<br />

<a name="faq91"></a>
**~~(91) Can you add periodical synchronization to save battery power?~~**

~~Synchronizing messages is an expensive proces because the local and remote messages need to be compared,~~ ~~so periodically synchronizing messages will not result in saving battery power, more likely the contrary.~~

~~See [this FAQ](#user-content-faq39) about optimizing battery usage.~~

<br />

<a name="faq92"></a>
**(92) Can you add spam filtering, verification of the DKIM signature and SPF authorization?**

Spam filtering, verification of the [DKIM](https://en.wikipedia.org/wiki/DomainKeys_Identified_Mail) signature and [SPF](https://en.wikipedia.org/wiki/Sender_Policy_Framework) authorization is a task of email servers, not of an email client. Servers generally have more memory and computing power, so they are much better suited to this task than battery-powered devices. Also, you'll want spam filtered for all your email clients, possibly including web email, not just one email client. Moreover, email servers have access to information, like the IP address, etc of the connecting server, which an email client has no access to.

Spam filtering based on message headers might have been feasible, but unfortunately this technique is [patented by Microsoft](https://patents.google.com/patent/US7543076).

Recent versions of FairEmail can filter spam to a certain extend using a message classifier. Please see [this FAQ](#user-content-faq163) for more information about this.

Of course you can report messages as spam with FairEmail, which will move the reported messages to the spam folder and train the spam filter of the provider, which is how it is supposed to work. This can be done automatically with [filter rules](#user-content-faq71) too. Blocking the sender will create a filter rule to automatically move future messages of the same sender into the spam folder.

Note that the POP3 protocol gives access to the inbox only. So, it is won't be possible to report spam for POP3 accounts.

Note that you should not delete spam messages, also not from the spam folder, because the email server uses the messages in the spam folder to "learn" what spam messages are.

If you receive a lot of spam messages in your inbox, the best you can do is to contact the email provider to ask if spam filtering can be improved.

Also, FairEmail can show a small red warning flag when DKIM, SPF or [DMARC](https://en.wikipedia.org/wiki/DMARC) authentication failed on the receiving server. You can enable/disable [authentication verification](https://en.wikipedia.org/wiki/Email_authentication) in the display settings.

FairEmail can show a warning flag too if the domain name of the (reply) email address of the sender does not define an MX record pointing to an email server. This can be enabled in the receive settings. Be aware that this will slow down synchronization of messages significantly.

If the domain name of the sender and the domain name of the reply address differ, the warning flag will be shown too because this is most often the case with phishing messages. If desired, this can be disabled in the receive settings (from version 1.1506).

If legitimate messages are failing authentication, you should notify the sender because this will result in a high risk of messages ending up in the spam folder. Moreover, without proper authentication there is a risk the sender will be impersonated. The sender might use [this tool](https://www.mail-tester.com/) to check authentication and other things.

<br />

<a name="faq93"></a>
**(93) Can you allow installation/data storage on external storage media (sdcard)?**

FairEmail uses services and alarms, provides widgets and listens for the boot completed event to be started on device start, so it is not possible to store the app on external storage media, like an sdcard. See also [here](https://developer.android.com/guide/topics/data/install-location).

Messages, attachments, etc stored on external storage media, like an sdcard, can be accessed by other apps and is therefore not safe. See [here](https://developer.android.com/training/data-storage) for the details.

When needed you can save (raw) messages via the three-dots menu just above the message text and save attachments by tapping on the floppy icon.

If you need to save on storage space, you can limit the number of days messages are being synchronized and kept for. You can change these settings by long pressing a folder in the folder list and selecting *Edit properties*.

<br />

<a name="faq94"></a>
**(94) What does the red/orange stripe at the end of the header mean?**

The red/orange stripe at the left side of the header means that the DKIM, SPF or DMARC authentication failed. See also [this FAQ](#user-content-faq92).

<br />

<a name="faq95"></a>
**(95) Why are not all apps shown when selecting an attachment or image?**

For privacy and security reasons FairEmail does not have permissions to directly access files, instead the Storage Access Framework, available and recommended since Android 4.4 KitKat (released in 2013), is used to select files.

If an app is listed depends on if the app implements a [document provider](https://developer.android.com/guide/topics/providers/document-provider). If the app is not listed, you might need to ask the developer of the app to add support for the Storage Access Framework.

Android Q will make it harder and maybe even impossible to directly access files, see [here](https://developer.android.com/preview/privacy/scoped-storage) and [here](https://www.xda-developers.com/android-q-storage-access-framework-scoped-storage/) for more details.

<br />

<a name="faq96"></a>
**(96) Where can I find the IMAP and SMTP settings?**

The IMAP settings are part of the (custom) account settings and the SMTP settings are part of the identity settings.

<br />

<a name="faq97"></a>
**(97) What is 'cleanup' ?**

About each four hours FairEmail runs a cleanup job that:

* Rimuovi i testi dei vecchi messaggi
* Rimuovi i file dei vecchi allegati
* Rimuovi le immagini delle vecchie immagini
* Rimuove i contatti locali vecchi
* Rimuove le vecchie voci del registro

Note that the cleanup job will only run when the synchronize service is active.

<br />

<a name="faq98"></a>
**(98) Why can I still pick contacts after revoking contacts permissions?**

After revoking contacts permissions Android does not allow FairEmail access to your contacts anymore. However, picking contacts is delegated to and done by Android and not by FairEmail, so this will still be possible without contacts permissions.

<br />

<a name="faq99"></a>
**(99) Can you add a rich text or markdown editor?**

FairEmail provides common text formatting (bold, italic, underline, text size and color) via a toolbar that appears after selecting some text.

A [Rich text](https://en.wikipedia.org/wiki/Formatted_text) or [Markdown](https://en.wikipedia.org/wiki/Markdown) editor would not be used by many people on a small mobile device and, more important, Android doesn't support a rich text editor and most rich text editor open source projects are abandoned. See [here](https://forum.xda-developers.com/showpost.php?p=79061829&postcount=4919) for some more details about this.

<br />

<a name="faq100"></a>
**(100) How can I synchronize Gmail categories?**

You can synchronize Gmail categories by creating filters to label categorized messages:

* Crea un nuovo filtro tramite Gmail > Impostazioni (rotellina) > Filtri e Indirizzi Bloccati > Crea un nuovo filtro
* Inserisci una ricerca della categoria (vedi sotto) nel campo *Contiene le parole* e clicca *Crea filtro*
* Spunta *Applica all'etichetta* e seleziona un'etichetta e clicca *Crea filtro*

Possible categories:

```
categoria:social
categoria:aggiornamenti
categoria:forum
categoria:promozioni
```

Unfortunately, this is not possible for snoozed messages folder.

You can use *Force sync* in the three-dots menu of the unified inbox to let FairEmail synchronize the folder list again and you can long press the folders to enable synchronization.

<br />

<a name="faq101"></a>
**(101) What does the blue/orange dot at the bottom of the conversations mean?**

The dot shows the relative position of the conversation in the message list. The dot will be show orange when the conversation is the first or last in the message list, else it will be blue. The dot is meant as an aid when swiping left/right to go to the previous/next conversation.

The dot is disabled by default and can be enabled with the display settings *Show relative conversation position with a dot*.

<br />

<a name="faq102"></a>
**(102) How can I enable auto rotation of images?**

Images will automatically be rotated when automatic resizing of images is enabled in the settings (enabled by default). However, automatic rotating depends on the [Exif](https://en.wikipedia.org/wiki/Exif) information to be present and to be correct, which is not always the case. Particularly not when taking a photo with a camara app from FairEmail.

Note that only [JPEG](https://en.wikipedia.org/wiki/JPEG) and [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) images can contain Exif information.

<br />

<a name="faq104"></a>
**(104) What do I need to know about error reporting?**

* La segnalazione degli errori aiuterà a migliorare FairEmail
* La segnalazione degli errori è opzionale e su adesione
* La segnalazione degli errori può esser abilitata/disabilitata nelle impostazioni, sezione varie
* Le segnalazioni degli errori saranno inviate automaticamente e anonimamente a [Bugsnag](https://www.bugsnag.com/)
* Bugsnag per Android è [open-source](https://github.com/bugsnag/bugsnag-android)
* Vedi [qui](https://docs.bugsnag.com/platforms/android/automatically-captured-data/) per sapere quali dati saranno inviati in caso di errori
* Vedi [qui](https://docs.bugsnag.com/legal/privacy-policy/) per conoscere la politica della privacy di Bugsnag
* Le segnalazioni di errori saranno inviate a *sessions.bugsnag.com:443* e *notify.bugsnag.com:443*

<br />

<a name="faq105"></a>
**(105) How does the roam-like-at-home option work?**

FairEmail will check if the country code of the SIM card and the country code of the network are in the [EU roam-like-at-home countries](https://en.wikipedia.org/wiki/European_Union_roaming_regulations#Territorial_extent) and assumes no roaming if the country codes are equal and the advanced roam-like-at-home option is enabled.

So, you don't have to disable this option if you don't have an EU SIM or are not connected to an EU network.

<br />

<a name="faq106"></a>
**(106) Which launchers can show a badge count with the number of unread messages?**

Please [see here](https://github.com/leolin310148/ShortcutBadger#supported-launchers) for a list of launchers which can show the number of unread messages.

Note that Nova Launcher requires Tesla Unread, which is [not supported anymore](https://forum.xda-developers.com/android/general/bad-news-tesla-unread-devoloper-t3920415).

Note that the notification setting *Show launcher icon with number of new messages* needs to be enabled (default enabled).

Only *new* unread messages in folders set to show new message notifications will be counted, so messages marked unread again and messages in folders set to not show new message notification will not be counted.

Depending on what you want, the notification settings *Let the number of new messages match the number of notifications* needs to be enabled (default disabled). When enabled the badge count will be the same as the number of new message notifications. When disabled the badge count will be the number of unread messages, independent if they are shown in a notification or are new.

This feature depends on support of your launcher. FairEmail merely 'broadcasts' the number of unread messages using the ShortcutBadger library. If it doesn't work, this cannot be fixed by changes in FairEmail.

Some launchers display a dot or a '1' for [the monitoring notification](#user-content-faq2), despite FairEmail explicitly requesting not to show a *badge* for this notification. This could be caused by a bug in the launcher app or in your Android version. Please double check if the notification dot (badge) is disabled for the receive (service) notification channel. You can go to the right notification channel settings via the notification settings of FairEmail. This might not be obvious, but you can tap on the channel name for more settings.

FairEmail does send a new message count intent as well:

```
eu.faircode.email.NEW_MESSAGE_COUNT
```

The number of new, unread messages will be in an integer "*count*" parameter.

<br />

<a name="faq107"></a>
**(107) How do I use colored stars?**

You can set a colored star via the *more* message menu, via multiple selection (started by long pressing a message), by long pressing a star in a conversation or automatically by using [rules](#user-content-faq71).

You need to know that colored stars are not supported by the IMAP protocol and can therefore not be synchronized to an email server. This means that colored stars will not be visible in other email clients and will be lost on downloading messages again. However, the stars (without color) will be synchronized and will be visible in other email clients, when supported.

Some email clients use IMAP keywords for colors. However, not all servers support IMAP keywords and besides that there are no standard keywords for colors.

<br />

<a name="faq108"></a>
**~~(108) Can you add permanently delete messages from any folder?~~**

~~When you delete messages from a folder the messages will be moved to the trash folder, so you have a chance to restore the messages.~~ ~~You can permanently delete messages from the trash folder.~~ ~~Permanently delete messages from other folders would defeat the purpose of the trash folder, so this will not be added.~~

<br />

<a name="faq109"></a>
**~~(109) Why is 'select account' available in official versions only?~~**

~~Using *select account* to select and authorize Google accounts require special permission from Google for security and privacy reasons.~~ ~~This special permission can only be acquired for apps a developer manages and is responsible for.~~ ~~Third party builds, like the F-Droid builds, are managed by third parties and are the responsibility of these third parties.~~ ~~So, only these third parties can acquire the required permission from Google.~~ ~~Since these third parties do not actually support FairEmail, they are most likely not going to request the required permission.~~

~~You can solve this in two ways:~~

* ~~Passando alla versione ufficiale di FairEmail, vedi [qui](https://github.com/M66B/FairEmail/blob/master/README.md#downloads) le opzioni~~
* ~~Usando password specifiche dell'app, vedi [questa FAQ](#user-content-faq6)~~

~~Using *select account* in third party builds is not possible in recent versions anymore.~~ ~~In older versions this was possible, but it will now result in the error *UNREGISTERED_ON_API_CONSOLE*.~~

<br />

<a name="faq110"></a>
**(110) Why are (some) messages empty and/or attachments corrupt?**

Empty messages and/or corrupt attachments are probably being caused by a bug in the server software. Older Microsoft Exchange software is known to cause this problem. Mostly you can workaround this by disabling *Partial fetch* in the advanced account settings:

Settings > Manual setup > Accounts > tap account > tap advanced > Partial fetch > uncheck

After disabling this setting, you can use the message 'more' (three dots) menu to 'resync' empty messages. Alternatively, you can *Delete local messages* by long pressing the folder(s) in the folder list and synchronize all messages again.

Disabling *Partial fetch* will result in more memory usage.

<br />

<a name="faq111"></a>
**(111) Is OAuth supported?**

OAuth for Gmail is supported via the quick setup wizard. The Android account manager will be used to fetch and refresh OAuth tokens for selected on-device accounts. OAuth for non on-device accounts is not supported because Google requires a [yearly security audit](https://support.google.com/cloud/answer/9110914) ($15,000 to $75,000) for this. You can read more about this [here](https://www.theregister.com/2019/02/11/google_gmail_developer/).

OAuth for Outlook/Office 365, Yahoo, Mail.ru and Yandex is supported via the quick setup wizard.

<br />

<a name="faq112"></a>
**(112) Which email provider do you recommend?**

FairEmail is an email client only, so you need to bring your own email address. Note that this is clearly mentioned in the app description.

There are plenty of email providers to choose from. Which email provider is best for you depends on your wishes/requirements. Please see the websites of [Restore privacy](https://restoreprivacy.com/secure-email/) or [Privacy Tools](https://www.privacytools.io/providers/email/) for a list of privacy oriented email providers with advantages and disadvantages.

Some providers, like ProtonMail, Tutanota, use proprietary email protocols, which make it impossible to use third party email apps. Please see [this FAQ](#user-content-faq129) for more information.

Using your own (custom) domain name, which is supported by most email providers, will make it easier to switch to another email provider.

<br />

<a name="faq113"></a>
**(113) How does biometric authentication work?**

If your device has a biometric sensor, for example a fingerprint sensor, you can enable/disable biometric authentication in the navigation (hamburger) menu of the settings screen. When enabled FairEmail will require biometric authentication after a period of inactivity or after the screen has been turned off while FairEmail was running. Activity is navigation within FairEmail, for example opening a conversation thread. The inactivity period duration can be configured in the miscellaneous settings. When biometric authentication is enabled new message notifications will not show any content and FairEmail won't be visible on the Android recents screen.

Biometric authentication is meant to prevent others from seeing your messages only. FairEmail relies on device encryption for data encryption, see also [this FAQ](#user-content-faq37).

Biometric authentication is a pro feature.

<br />

<a name="faq114"></a>
**(114) Can you add an import for the settings of other email apps?**

The format of the settings files of most other email apps is not documented, so this is difficult. Sometimes it is possible to reverse engineer the format, but as soon as the settings format changes things will break. Also, settings are often incompatible. For example, FairEmail has unlike most other email apps settings for the number of days to synchronize messages and for the number of days to keep messages, mainly to save on battery usage. Moreover, setting up an account/identity with the quick setup wizard is simple, so it is not really worth the effort.

<br />

<a name="faq115"></a>
**(115) Can you add email address chips?**

Email address [chips](https://material.io/design/components/chips.html) look nice, but cannot be edited, which is quite inconvenient when you made a typo in an email address.

Note that FairEmail will select the address only when long pressing an address, which makes it easy to delete an address.

Chips are not suitable for showing in a list and since the message header in a list should look similar to the message header of the message view it is not an option to use chips for viewing messages.

Reverted [commit](https://github.com/M66B/FairEmail/commit/2c80c25b8aa75af2287f471b882ec87d5a5a5015).

<br />

<a name="faq116"></a>
**~~(116) How can I show images in messages from trusted senders by default?~~**

~~You can show images in messages from trusted senders by default by enabled the display setting *Automatically show images for known contacts*.~~

~~Contacts in the Android contacts list are considered to be known and trusted,~~ ~~unless the contact is in the group / has the label '*Untrusted*' (case insensitive).~~

<br />

<a name="faq38"></a>
<a name="faq117"></a>
**(117) Can you help me restore my purchase?**

First of all, a purchase will be available on all devices logged into the same Google account, *if* the app is installed via the same Google account too. You can select the account in the Play store app.

Google manages all purchases, so as a developer I have little control over purchases. So, basically the only thing I can do, is give some advice:

* Assicurati di avere una connessione a internet attiva e funzionante
* Assicurati di esser connesso con il giusto profilo di Google e che non ci sia niente di sbagliato con il tuo profilo di Google
* Assicurati di aver installato FairEmail tramite il giusto profilo di Google se ne hai configurato più di uno sul tuo dispositivo
* Assicurati che l'app del Play Store sia aggiornata, sei pregato di [vedere qui](https://support.google.com/googleplay/answer/1050566?hl=en)
* Apri l'app del Play Store e attendi almeno un minuto per dargli tempo di sincronizzarsi con i server di Google
* Apri FairEmail e naviga alla schermata delle funzionalità pro per far verificare gli acquisti a FairEmail; a volte aiuta toccare il pulsante *compra*

You can also try to clear the cache of the Play store app via the Android apps settings. Restarting the device might be necessary to let the Play store recognize the purchase correctly.

Note that:

* Se ottieni *ITEM_ALREADY_OWNED*, l'app del Play Store probabilmente dev'esser aggiornata, sei pregato di [vedere qui](https://support.google.com/googleplay/answer/1050566?hl=en)
* Gli acquisti sono memorizzati nel cloud di Google e non possono esser perduti
* Non c'è limite di tempo sugli acquisti, quindi non possono scadere
* Google non espone dettagli (nome, email, etc.) sui compratori agli sviluppatori
* Un'app come FairEmail non può selezionare che profilo di Google usare
* Potrebbe volerci un po' fino alla sincronizzazione di un'acquisto dell'app di Play Store a un altro dispositivo
* Gli acquisti del Play Store non sono utilizzabili senza il Play Store, che non è peraltro consentito dalle regole del Play Store

If you cannot solve the problem with the purchase, you will have to contact Google about it.

<br />

<a name="faq118"></a>
**(118) What does 'Remove tracking parameters' exactly?**

Checking *Remove tracking parameters* will remove all [UTM parameters](https://en.wikipedia.org/wiki/UTM_parameters) from a link.

<br />

<a name="faq119"></a>
**~~(119) Can you add colors to the unified inbox widget?~~**

~~The widget is designed to look good on most home/launcher screens by making it monochrome and by using a half transparent background.~~ ~~This way the widget will nicely blend in, while still being properly readable.~~

~~Adding colors will cause problems with some backgrounds and will cause readability problems, which is why this won't be added.~~

Due to Android limitations it is not possible to dynamically set the opacity of the background and to have rounded corners at the same time.

<br />

<a name="faq120"></a>
**(120) Why are new message notifications not removed on opening the app?**

New message notifications will be removed on swiping notifications away or on marking the associated messages read. Opening the app will not remove new message notifications. This gives you a choice to leave new message notifications as a reminder that there are still unread messages.

On Android 7 Nougat and later new message notifications will be [grouped](https://developer.android.com/training/notify-user/group). Tapping on the summary notification will open the unified inbox. The summary notification can be expanded to view individual new message notifications. Tapping on an individual new message notification will open the conversation the message it is part of. See [this FAQ](#user-content-faq70) about when messages in a conversation will be auto expanded and marked read.

<br />

<a name="faq121"></a>
**(121) How are messages grouped into a conversation?**

By default FairEmail groups messages in conversations. This can be turned of in the display settings.

FairEmail groups messages based on the standard *Message-ID*, *In-Reply-To* and *References* headers. FairEmail does not group on other criteria, like the subject, because this could result in grouping unrelated messages and would be at the expense of increased battery usage.

<br />

<a name="faq122"></a>
**~~(122) Why is the recipient name/email address show with a warning color?~~**

~~The recipient name and/or email address in the addresses section will be shown in a warning color~~ ~~when the sender domain name and the domain name of the *to* address do not match.~~ ~~Mostly this indicates that the message was received *via* an account with another email address.~~

<br />

<a name="faq123"></a>
**(123) What will happen when FairEmail cannot connect to an email server?**

If FairEmail cannot connect to an email server to synchronize messages, for example if the internet connection is bad or a firewall or a VPN is blocking the connection, FairEmail will retry one time after waiting 8 seconds while keeping the device awake (=use battery power). If this fails, FairEmail will schedule an alarm to retry after 15, 30 and eventually every 60 minutes and let the device sleep (=no battery usage).

Note that [Android doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby) does not allow to wake the device earlier than after 15 minutes.

*Force sync* in the three-dots menu of the unified inbox can be used to let FairEmail attempt to reconnect without waiting.

Sending messages will be retried on connectivity changes only (reconnecting to the same network or connecting to another network) to prevent the email server from blocking the connection permanently. You can pull down the outbox to retry manually.

Note that sending will not be retried in case of authentication problems and when the server rejected the message. In this case you can pull down the outbox to try again.

<br />

<a name="faq124"></a>
**(124) Why do I get 'Message too large or too complex to display'?**

The message *Message too large or too complex to display* will be shown if there are more than 100,000 characters or more than 500 links in a message. Reformatting and displaying such messages will take too long. You can try to use the original message view, powered by the browser, instead.

<br />

<a name="faq125"></a>
**(125) What are the current experimental features?**

*Message classification (version 1.1438+)*

Please see [this FAQ](#user-content-faq163) for details.

Since this is an experimental feature, my advice is to start with just one folder.

<br />

*Send hard bounce (version 1.1477+)*

Send a [Delivery Status Notification](https://tools.ietf.org/html/rfc3464) (=hard bounce) via the reply/answer menu.

Hard bounces will mostly be processed automatically because they affect the reputation of the email provider. The bounce address (=*Return-Path* header) is mostly very specific, so the email server can determine the sending account.

For some background, see for [this Wikipedia article](https://en.wikipedia.org/wiki/Bounce_message).

<br />

<a name="faq126"></a>
**(126) Can message previews be sent to my wearable?**

FairEmail fetches a message in two steps:

1. Recupera intestazioni del messaggio
1. Recupera testo e allegati del messaggio

Directly after the first step new messages will be notified. However, only until after the second step the message text will be available. FairEmail updates exiting notifications with a preview of the message text, but unfortunately wearable notifications cannot be updated.

Since there is no guarantee that a message text will always be fetched directly after a message header, it is not possible to guarantee that a new message notification with a preview text will always be sent to a wearable.

If you think this is good enough, you can enable the notification option *Only send notifications with a message preview to wearables* and if this does not work, you can try to enable the notification option *Show notifications with a preview text only*. Note that this applies to wearables not showing a preview text too, even when the Android Wear app says the notification has been sent (bridged).

If you want to have the full message text sent to your wearable, you can enable the notification option *Preview all text*. Note that some wearables are known to crash with this option enabled.

If you use a Samsung wearable with the Galaxy Wearable (Samsung Gear) app, you might need to enable notifications for FairEmail when the setting *Notifications*, *Apps installed in the future* is turned off in this app.

<br />

<a name="faq127"></a>
**(127) How can I fix 'Syntactically invalid HELO argument(s)'?**

The error *... Syntactically invalid HELO argument(s) ...* means that the SMTP server rejected the local IP address or host name. You can likely fix this error by enabling or disabling the advanced indentity option *Use local IP address instead of host name*.

<br />

<a name="faq128"></a>
**(128) How can I reset asked questions, for example to show images?**

You can reset asked questions via the three dots overflow menu in the miscellaneous settings.

<br />

<a name="faq129"></a>
**(129) Are ProtonMail, Tutanota supported?**

ProtonMail uses a proprietary email protocol and [does not directly support IMAP](https://protonmail.com/support/knowledge-base/imap-smtp-and-pop3-setup/), so you cannot use FairEmail to access ProtonMail.

Tutanota uses a proprietary email protocol and [does not support IMAP](https://tutanota.com/faq/#imap), so you cannot use FairEmail to access Tutanota.

<br />

<a name="faq130"></a>
**(130) What does message error ... mean?**

A series of lines with orangish or red texts with technical information means that debug mode was enabled in the miscellaneous settings.

The warning *No server found at ...* means that there was no email server registered at the indicated domain name. Replying to the message might not be possible and might result in an error. This could indicate a falsified email address and/or spam.

The error *... ParseException ...* means that there is a problem with a received message, likely caused by a bug in the sending software. FairEmail will workaround this is in most cases, so this message can mostly be considered as a warning instead of an error.

The error *...SendFailedException...* means that there was a problem while sending a message. The error will almost always include a reason. Common reasons are that the message was too big or that one or more recipient addresses were invalid.

The warning *Message too large to fit into the available memory* means that the message was larger than 10 MiB. Even if your device has plenty of storage space Android provides limited working memory to apps, which limits the size of messages that can be handled.

Please see [here](#user-content-faq22) for other error messages in the outbox.

<br />

<a name="faq131"></a>
**(131) Can you change the direction for swiping to previous/next message?**

If you read from left to right, swiping to the left will show the next message. Similarly, if you read from right to left, swiping to the right will show the next message.

This behavior seems quite natural to me, also because it is similar to turning pages.

Anyway, there is a behavior setting to reverse the swipe direction.

<br />

<a name="faq132"></a>
**(132) Why are new message notifications silent?**

Notifications are silent by default on some MIUI versions. Please see [here](http://en.miui.com/thread-3930694-1-1.html) how you can fix this.

There is a bug in some Android versions causing [setOnlyAlertOnce](https://developer.android.com/reference/android/app/Notification.Builder#setOnlyAlertOnce(boolean)) to mute notifications. Since FairEmail shows new message notifications right after fetching the message headers and FairEmail needs to update new message notifications after fetching the message text later, this cannot be fixed or worked around by FairEmail.

Android might rate limit the notification sound, which can cause some new message notifications to be silent.

<br />

<a name="faq133"></a>
**(133) Why is ActiveSync not supported?**

The Microsoft Exchange ActiveSync protocol [is patented](https://en.wikipedia.org/wiki/Exchange_ActiveSync#Licensing) and can therefore not be supported. For this reason you won't find many, if any, other email clients supporting ActiveSync.

Note that the desciption of FairEmail starts with the remark that non-standard protocols, like Microsoft Exchange Web Services and Microsoft ActiveSync are not supported.

<br />

<a name="faq134"></a>
**(134) Can you add deleting local messages?**

*POP3*

In the account settings (Settings, tap Manual setup, tap Accounts, tap account) you can enable *Leave deleted messages on server*.

*IMAP*

Since the IMAP protocol is meant to synchronize two ways, deleting a message from the device would result in fetching the message again when synchronizing again.

However, FairEmail supports hiding messages, either via the three-dots menu in the action bar just above the message text or by multiple selecting messages in the message list. Basically this is the same as "leave on server" of the POP3 protocol with the advantage that you can show the messages again when needed.

Note that it is possible to set the swipe left or right action to hide a message.

<br />

<a name="faq135"></a>
**(135) Why are trashed messages and drafts shown in conversations?**

Individual messages will rarely be trashed and mostly this happens by accident. Showing trashed messages in conversations makes it easier to find them back.

You can permanently delete a message using the message three-dots *delete* menu, which will remove the message from the conversation. Note that this irreversible.

Similarly, drafts are shown in conversations to find them back in the context where they belong. It is easy to read through the received messages before continuing to write the draft later.

<br />

<a name="faq136"></a>
**(136) How can I delete an account/identity/folder?**

Deleting an account/identity/folder is a little bit hidden to prevent accidents.

* Profilo: Impostazioni > Configurazione manuale > Profili > tocca il profilo
* Identità: Impostazioni> Configurazione manuale > Identità > tocca l'identità
* Cartella: Tieni premuta la cartella nell'elenco delle cartelle > Modifica proprietà

In the three-dots overflow menu at the top right there is an item to delete the account/identity/folder.

<br />

<a name="faq137"></a>
**(137) How can I reset 'Don't ask again'?**

You can reset all questions set to be not asked again in the miscellaneous settings.

<br />

<a name="faq138"></a>
**(138) Can you add calendar/contact/tasks/notes management?**

Calendar, contact, task and note management can better be done by a separate, specialized app. Note that FairEmail is a specialized email app, not an office suite.

Also, I prefer to do a few things very well, instead of many things only half. Moreover, from a security perspective, it is not a good idea to grant many permissions to a single app.

You are advised to use the excellent, open source [DAVx⁵](https://f-droid.org/packages/at.bitfire.davdroid/) app to synchronize/manage your calendars/contacts.

Most providers support exporting your contacts. Please [see here](https://support.google.com/contacts/answer/1069522) about how you can import contacts if synchronizing is not possible.

Note that FairEmail does support replying to calendar invites (a pro feature) and adding calendar invites to your personal calendar.

<br />

<a name="faq83"></a>
<a name="faq139"></a>
**(139) How do I fix 'User is authenticated but not connected'?**

In fact this Microsoft Exchange specific error is an incorrect error message caused by a bug in older Exchange server software.

The error *User is authenticated but not connected* might occur if:

* I messaggi push sono abilitati per troppe cartelle: vedi [questa FAQ](#user-content-faq23) per ulteriori informazioni e una soluzione
* La password del profilo è stata cambiata: cambiarla anche in FairEmail dovrebbe risolvere il problema
* L'indirizzo email di un alias è in uso come nome utente invece che come indirizzo email principale
* Uno schema di accesso errato è in uso per una casella condivisa: lo schema esatto è *nomeutente@dominio\SharedMailboxAlias*

The shared mailbox alias will mostly be the email address of the shared account, like this:

```
you@example.com\shared@example.com
```

Note that it should be a backslash and not a forward slash.

When using a shared mailbox, you'll likely want to enable the option *Synchronize shared folder lists* in the receive settings.

<br />

<a name="faq140"></a>
**(140) Why does the message text contain strange characters?**

Displaying strange characters is almost always caused by specifying no or an invalid character encoding by the sending software. FairEmail will assume [ISO 8859-1](https://en.wikipedia.org/wiki/ISO/IEC_8859-1) when no character set or when [US-ASCII](https://en.wikipedia.org/wiki/ASCII) was specified. Other than that there is no way to reliably determine the correct character encoding automatically, so this cannot be fixed by FairEmail. The right action is to complain to the sender.

<br />

<a name="faq141"></a>
**(141) How can I fix 'A drafts folder is required to send messages'?**

To store draft messages a drafts folder is required. In most cases FairEmail will automatically select the drafts folders on adding an account based on [the attributes](https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml) the email server sends. However, some email servers are not configured properly and do not send these attributes. In this case FairEmail tries to identify the drafts folder by name, but this might fail if the drafts folder has an unusual name or is not present at all.

You can fix this problem by manually selecting the drafts folder in the account settings (Settings, tap Manual setup, tap Accounts, tap account, at the bottom). If there is no drafts folder at all, you can create a drafts folder by tapping on the '+' button in the folder list of the account (tap on the account name in the navigation menu).

Some providers, like Gmail, allow enabling/disabling IMAP for individual folders. So, if a folder is not visible, you might need to enable IMAP for the folder.

Quick link for Gmail (will work on a desktop computer only): [https://mail.google.com/mail/u/0/#settings/labels](https://mail.google.com/mail/u/0/#settings/labels)

<br />

<a name="faq142"></a>
**(142) How can I store sent messages in the inbox?**

Generally, it is not a good idea to store sent messages in the inbox because this is hard to undo and could be incompatible with other email clients.

That said, FairEmail is able to properly handle sent messages in the inbox. FairEmail will mark outgoing messages with a sent messages icon for example.

The best solution would be to enable showing the sent folder in the unified inbox by long pressing the sent folder in the folder list and enabling *Show in unified inbox*. This way all messages can stay where they belong, while allowing to see both incoming and outgoing messages at one place.

If this is not an option, you can [create a rule](#user-content-faq71) to automatically move sent messages to the inbox or set a default CC/BCC address in the advanced identity settings to send yourself a copy.

<br />

<a name="faq143"></a>
**~~(143) Can you add a trash folder for POP3 accounts?~~**

[POP3](https://en.wikipedia.org/wiki/Post_Office_Protocol) is a very limited protocol. Basically only messages can be downloaded and deleted from the inbox. It is not even possible to mark a message read.

Since POP3 does not allow access to the trash folder at all, there is no way to restore trashed messages.

Note that you can hide messages and search for hidden messages, which is similar to a local trash folder, without suggesting that trashed messages can be restored, while this is actually not possible.

Version 1.1082 added a local trash folder. Note that trashing a message will permanently remove it from the server and that trashed messages cannot be restored to the server anymore.

<br />

<a name="faq144"></a>
**(144) How can I record voice notes?**

To record voice notes you can press this icon in the bottom action bar of the message composer:

![External image](https://github.com/M66B/FairEmail/blob/master/images/baseline_record_voice_over_black_48dp.png)

This requires a compatible audio recorder app to be installed. In particular [this common intent](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION) needs to be supported.

For example [this audio recorder](https://f-droid.org/app/com.github.axet.audiorecorder) is compatible.

Voice notes will automatically be attached.

<br />

<a name="faq145"></a>
**(145) How can I set a notification sound for an account, folder or sender?**

Account:

* Abilita *Notifiche separate* nelle impostazioni avanzate del profilo (Impostazioni, tocca Configurazione manuale, tocca Profili, tocca il profilo, tocca Avanzate)
* Tieni premuto il profilo nell'elenco dei profili (Impostazioni, tocca Configurazione manuale, tocca Profili) e seleziona *Modifica canale di notifica* per modificare il suono della notifica

Folder:

* Tieni premuta la cartella nell'elenco delle cartelle e seleziona *Crea canale di notifica*
* Tieni premuta la cartella nell'elenco delle cartelle e seleziona *Modifica canale di notifica* per modificare il suono della notifica

Sender:

* Apri un messaggio dal mittente ed espandilo
* Espandi la sezione degli indirizzi toccando sulla freccia in giù
* Tocca sull'icona della campanella o modifica un canale di notifica e cambia il suono della notifica

The order of precendence is: sender sound, folder sound, account sound and default sound.

Setting a notification sound for an account, folder or sender requires Android 8 Oreo or later and is a pro feature.

<br />

<a name="faq146"></a>
**(146) How can I fix incorrect message times?**

Since the sent date/time is optional and can be manipulated by the sender, FairEmail uses the server received date/time by default.

Sometimes the server received date/time is incorrect, mostly because messages were incorrectly imported from another server and sometimes due to a bug in the email server.

In these rare cases, it is possible to let FairEmail use either the date/time from the *Date* header (sent time) or from the *Received* header as a workaround. This can be changed in the advanced account settings: Settings, tap Manual setup, tap Accounts, tap account, tap Advanced.

This will not change the time of already synchronized messages. To solve this, long press the folder(s) in the folder list and select *Delete local messages* and *Synchronize now*.

<br />

<a name="faq147"></a>
**(147) What should I know about third party versions?**

You likely came here because you are using a third party build of FairEmail.

There is **only support** on the latest Play store version, the latest GitHub release and the F-Droid build, but **only if** the version number of the F-Droid build is the same as the version number of the latest GitHub release.

F-Droid builds irregularly, which can be problematic when there is an important update. Therefore you are advised to switch to the GitHub release.

The F-Droid version is built from the same source code, but signed differently. This means that all features are available in the F-Droid version too, except for using the Gmail quick setup wizard because Google approved (and allows) one app signature only. For all other email providers, OAuth access is only available in Play Store versions and Github releases, as the email providers only permit the use of OAuth for official builds.

Note that you'll need to uninstall the F-Droid build first before you can install a GitHub release because Android refuses to install the same app with a different signature for security reasons.

Note that the GitHub version will automatically check for updates. When desired, this can be turned off in the miscellaneous settings.

Please [see here](https://github.com/M66B/FairEmail/blob/master/README.md#user-content-downloads) for all download options.

If you have a problem with the F-Droid build, please check if there is a newer GitHub version first.

<br />

<a name="faq148"></a>
**(148) How can I use an Apple iCloud account?**

There is a built-in profile for Apple iCloud, so you should be able to use the quick setup wizard (other provider). If needed you can find the right settings [here](https://support.apple.com/en-us/HT202304) to manually set up an account.

When using two-factor authentication you might need to use an [app-specific password](https://support.apple.com/en-us/HT204397).

<br />

<a name="faq149"></a>
**(149) How does the unread message count widget work?**

The unread message count widget shows the number of unread messages either for all accounts or for a selected account, but only for the folders for which new message notifications are enabled.

Tapping on the notification will synchronize all folders for which synchronization is enabled and will open:

* la schermata di avvio quando tutti i profili sono selezionati
* un elenco delle cartelle quando un profilo specifico è stato selezionato e quando le notifiche dei nuovi messaggi sono abilitate per più cartelle
* un elenco di messaggi quando un profilo specifico è stato selezionato e quando le notifiche dei nuovi messaggi sono abilitate per una cartella

<br />

<a name="faq150"></a>
**(150) Can you add cancelling calendar invites?**

Cancelling calendar invites (removing calendar events) requires write calendar permission, which will result in effectively granting permission to read and write *all* calendar events of *all* calendars.

Given the goal of FairEmail, privacy and security, and given that it is easy to remove a calendar event manually, it is not a good idea to request this permission for just this reason.

Inserting new calendar events can be done without permissions with special [intents](https://developer.android.com/guide/topics/providers/calendar-provider.html#intents). Unfortunately, there exists no intent to delete existing calendar events.

<br />

<a name="faq151"></a>
**(151) Can you add backup/restore of messages?**

An email client is meant to read and write messages, not to backup and restore messages. Note that breaking or losing your device, means losing your messages!

Instead, the email provider/server is responsible for backups.

If you want to make a backup yourself, you could use a tool like [imapsync](https://imapsync.lamiral.info/).

Since version 1.1556 it is possible to export all messages of a POP3 folder in mbox format according to [RFC4155](https://www.ietf.org/rfc/rfc4155.txt), which might be useful to save sent messages if the email server doesn't.

If you want to import an mbox file to an existing email account, you can use Thunderbird on a desktop computer and the [ImportExportTools](https://addons.thunderbird.net/nl/thunderbird/addon/importexporttools/) add-on.

<br />

<a name="faq152"></a>
**(152) How can I insert a contact group?**

You can insert the email addresses of all contacts in a contact group via the three dots menu of the message composer.

You can define contact groups with the Android contacts app, please see [here](https://support.google.com/contacts/answer/30970) for instructions.

<br />

<a name="faq153"></a>
**(153) Why does permanently deleting Gmail message not work?**

You might need to change [the Gmail IMAP settings](https://mail.google.com/mail/u/0/#settings/fwdandpop) on a desktop browser to make it work:

* Quando contrassegno un messaggio in IMAP come eliminato: Eliminazione automatica disattivata - Attendi l'aggiornamento del server dal client.
* Quando un messaggio è contrassegnato come eliminato e rimosso dall'ultima cartella visibile di IMAP: Elimina immediatamente il messaggio per sempre

Note that archived messages can be deleted only by moving them to the trash folder first.

Some background: Gmail seems to have an additional message view for IMAP, which can be different from the main message view.

Another oddity is that a star (favorite message) set via the web interface cannot be removed with the IMAP command

```
STORE <message number> -FLAGS (\Flagged)
```

On the other hand, a star set via IMAP is being shown in the web interface and can be removed via IMAP.

<br />

<a name="faq154"></a>
**~~(154) Can you add favicons as contact photos?~~**

~~Besides that a [favicon](https://en.wikipedia.org/wiki/Favicon) might be shared by many email addresses with the same domain name~~ ~~and therefore is not directly related to an email address, favicons can be used to track you.~~

<br />

<a name="faq155"></a>
**(155) What is a winmail.dat file?**

A *winmail.dat* file is sent by an incorrectly configured Outlook client. It is a Microsoft specific file format ([TNEF](https://en.wikipedia.org/wiki/Transport_Neutral_Encapsulation_Format)) containing a message and possibly attachments.

You can find some more information about this file [here](https://support.mozilla.org/en-US/kb/what-winmaildat-attachment).

You can view it with for example the Android app [Letter Opener](https://play.google.com/store/apps/details?id=app.letteropener).

<br />

<a name="faq156"></a>
**(156) How can I set up an Office 365 account?**

An Office 365 account can be set up via the quick setup wizard and selecting *Office 365 (OAuth)*.

If the wizard ends with *AUTHENTICATE failed*, IMAP and/or SMTP might be disabled for the account. In this case you should ask the administrator to enable IMAP and SMTP. The procedure is documented [here](https://docs.microsoft.com/en-in/exchange/troubleshoot/configure-mailboxes/pop3-imap-owa-activesync-office-365).

If you've enabled *security defaults* in your organization, you might need to enable the SMTP AUTH protocol. Please [see here](https://docs.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/authenticated-client-smtp-submission) about how to.

<br />

<a name="faq157"></a>
**(157) How can I set up an Free.fr account?**

Veuillez [voir ici](https://free.fr/assistance/597.html) pour les instructions.

**SMTP est désactivé par défaut**, veuillez [voir ici](https://free.fr/assistance/2406.html) comment il peut être activé.

Veuillez [voir ici](http://jc.etiemble.free.fr/abc/index.php/trucs-astuces/configurer-smtp-free-fr) pour un guide détaillé.

<br />

<a name="faq103"></a>
<a name="faq158"></a>
**(158) Which camera / audio recorder do you recommend?**

To take photos and to record audio a camera and an audio recorder app are needed. The following apps are open source cameras and audio recorders:

* [Open Camera](https://play.google.com/store/apps/details?id=net.sourceforge.opencamera) ([F-Droid](https://f-droid.org/en/packages/net.sourceforge.opencamera/))
* [Audio Recorder versione 3.3.24+](https://play.google.com/store/apps/details?id=com.github.axet.audiorecorder) ([F-Droid](https://f-droid.org/packages/com.github.axet.audiorecorder/))

To record voice notes, etc, the audio recorder needs to support [MediaStore.Audio.Media.RECORD_SOUND_ACTION](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media#RECORD_SOUND_ACTION). Oddly, most audio recorders seem not to support this standard Android action.

<br />

<a name="faq159"></a>
**(159) What are Disconnect's tracker protection lists?**

Please see [here](https://disconnect.me/trackerprotection) for more information about Disconnect's tracker protection lists.

After downloading the lists in the privacy settings, the lists can optionally be used:

* per avvisare sui link di monitoraggio alla loro apertura
* per riconoscere le immagini di monitoraggio nei messaggi

Tracking images will be disabled only if the corresponding main 'disable' option is enabled.

Tracking images will not be recognized when the domain is classified as '*Content*', see [here](https://disconnect.me/trackerprotection#trackers-we-dont-block) for more information.

This command can be sent to FairEmail from an automation app to update the protection lists:

```
(adb shell) am start-foreground-service -a eu.faircode.email.DISCONNECT.ME
```

Updating once a week will probably be sufficient, please see [here](https://github.com/disconnectme/disconnect-tracking-protection/commits/master) for recent lists changes.

<br />

<a name="faq160"></a>
**(160) Can you add permanent deletion of messages without confirmation?**

Permanent deletion means that messages will *irreversibly* be lost, and to prevent this from happening accidentally, this always needs to be confirmed. Even with a confirmation, some very angry people who lost some of their messages through their own fault contacted me, which was a rather unpleasant experience :-(

Advanced: the IMAP delete flag in combination with the EXPUNGE command is not supportable because both email servers and not all people can handle this, risking unexpected loss of messages. A complicating factor is that not all email servers support [UID EXPUNGE](https://tools.ietf.org/html/rfc4315).

From version 1.1485 it is possible to temporarily enable debug mode in the miscellaneous settings to disable expunging messages. Note that messages with a *\Deleted* flag will not be shown in FairEmail.

<br />

<a name="faq161"></a>
**(161) Can you add a setting to change the primary and accent color?***

If I could, I would add a setting to select the primary and accent color right away, but unfortunately Android themes are fixed, see for example [here](https://stackoverflow.com/a/26511725/1794097), so this is not possible.

<br />

<a name="faq162"></a>
**(162) Is IMAP NOTIFY supported?***

Yes, [IMAP NOTIFY](https://tools.ietf.org/html/rfc5465) has been supported since version 1.1413.

IMAP NOTIFY support means that notifications for added, changed or deleted messages of all *subscribed* folders will be requested and if a notification is received for a subscribed folder, that the folder will be synchronized. Synchronization for subscribed folders can therefore be disable, saving folder connections to the email server.

**Important**: push messages (=always sync) for the inbox and subscription management (receive settings) need to be enabled.

**Important**: most email servers do not support this! You can check the log via the navigation menu if an email server supports the NOTIFY capability.

<br />

<a name="faq163"></a>
**(163) What is message classification?**

*This is an experimental feature!*

Message classification will attempt to automatically group emails into classes, based on their contents, using [Bayesian statistics](https://en.wikipedia.org/wiki/Bayesian_statistics). In the context of FairEmail, a folder is a class. So, for example, the inbox, the spam folder, a 'marketing' folder, etc, etc.

You can enable message classification in the miscellaneous settings. This will enable 'learning' mode only. The classifier will 'learn' from new messages in the inbox and spam folder by default. The folder property *Classify new messages in this folder* will enable or disable 'learning' mode for a folder. You can clear local messages (long press a folder in the folder list of an account) and synchronize the messages again to classify existing messages.

Each folder has an option *Automatically move classified messages to this folder* ('auto classification' for short). When this is turned on, new messages in other folders which the classifier thinks belong to that folder will be automatically moved.

The option *Use local spam filter* in the report spam dialog will turn on message classification in the miscellaneous settings and auto classification for the spam folder. Please understand that this is not a replacement for the spam filter of the email server and can result in [false positives and false negatives](https://en.wikipedia.org/wiki/False_positives_and_false_negatives). See also [this FAQ](#user-content-faq92).

A practical example: suppose there is a folder 'marketing' and auto message classification is enabled for this folder. Each time you move a message into this folder you'll train FairEmail that similar messages belong in this folder. Each time you move a message out of this folder you'll train FairEmail that similar messages do not belong in this folder. After moving some messages into the 'marketing' folder, FairEmail will start moving similar messages automatically into this folder. Or, the other way around, after moving some messages out of the 'marketing' folder, FairEmail will stop moving similar messages automatically into this folder. This will work best with messages with similar content (email addresses, subject and message text).

Classification should be considered as a best guess - it might be a wrong guess, or the classifier might not be confident enough to make any guess. If the classifier is unsure, it will simply leave an email where it is.

To prevent the email server from moving a message into the spam folder again and again, auto classification out of the spam folder will not be done.

The message classifier calculates the probability a message belongs in a folder (class). There are two options in the miscellaneous settings which control if a message will be automatically moved into a folder, provided that auto classification is enabled for the folder:

* *Probabilità minima della classe*: un messaggio sarà spostato solo quando la sicurezza che appartenga a una cartella è maggiore di questo valore (predefinito al 15%)
* *Differenza minima della classe*: un messaggio sarà spostato quando la differenza di confidenza nell'appartenenza a una classe e la successiva più probabile è maggiore di questo valore (predefinito al 50%)

Both conditions must be satisfied before a message will be moved.

Considering the default option values:

* Mele al 40% e banane al 30% sarebbero scartate perché la differenza di 25% è inferiore al minimo del 50%
* Mele al 10% e banane al 5% sarebbero scartate per la probabilità che le mele siano inferiori al minimo di 15%
* Mele al 50% e banane al 20% risulterebbe nella selezione delle mele

Classification is optimized to use as little resources as possible, but will inevitably use some extra battery power.

You can delete all classification data by turning classification in the miscellaneous settings three times off.

[Filter rules](#user-content-faq71) will be executed before classification.

Message classification is a pro feature, except for the spam folder.

<br />

<a name="faq164"></a>
**(164) Can you add customizable themes?**

Unfortunately, Android [does not support](https://stackoverflow.com/a/26511725/1794097) dynamic themes, which means all themes need [to be predefined](https://github.com/M66B/FairEmail/blob/master/app/src/main/res/values/styles.xml).

Since for each theme there needs to be a light, dark and black variant, it is not feasible to add for each color combination (literally millions) a predefined theme.

Moreover, a theme is more than just a few colors. For example themes with a yellow accent color use a darker link color for enough contrast.

The theme colors are based on the color circle of [Johannes Itten](https://en.wikipedia.org/wiki/Johannes_Itten).

<br />

<a name="faq165"></a>
**(165) Is Android Auto supported?**

Yes, Android Auto is supported, but only with the GitHub version, please [see here](https://forum.xda-developers.com/t/app-5-0-fairemail-fully-featured-open-source-privacy-oriented-email-app.3824168/post-83801249) about why.

For notification (messaging) support you'll need to enable the following notification options:

* *Usa il formato di notifica 'stile di messaggistica' di Android*
* Azioni di notifica: *Risposta diretta* e (contrassegna come) *Letto*

You can enable other notification actions too, if you like, but they are not supported by Android Auto.

The developers guide is [here](https://developer.android.com/training/cars/messaging).

<br />

<a name="faq166"></a>
**(166) Can I snooze a message across multiple devices?**

First of all, there is no standard for snoozing messages, so all snooze implementations are custom solutions.

Some email providers, like Gmail, move snoozed messages to a special folder. Unfortunately, third party apps have no access to this special folder.

Moving a message to another folder and back might fail and might not be possible if there is no internet connection. This is problematic because a message can be snoozed only after moving the message.

To prevent these issues, snoozing is done locally on the device by hiding the message while it is snoozing. Unfortunately, it is not possible to hide messages on the email server too.

<br />

## Ricevi supporto

FairEmail is supported on smartphones, tablets and ChromeOS only.

Only the latest Play store version and latest GitHub release are supported. The F-Droid build is supported only if the version number is the same as the version number of the latest GitHub release. This also means that downgrading is not supported.

There is no support on things that are not directly related to FairEmail.

There is no support on building and developing things by yourself.

Requested features should:

* essere utili a gran parte delle persone
* non complicare l'uso di FairEmail
* adattarsi alla filosofia di FairEmail (orientate alla privacy, con un occhio alla sicurezza)
* conformarsi agli standard comuni (IMAP, SMTP, ecc.)

Features not fulfilling these requirements will likely be rejected. This is also to keep maintenance and support in the long term feasible.

If you have a question, want to request a feature or report a bug, **please use [this form](https://contact.faircode.eu/?product=fairemailsupport)**.

GitHub issues are disabled due to frequent misusage.

<br />

Copyright &copy; 2018-2021 Marcel Bokhorst.
