# Guida di configurazione

Configurare FairEmail è abbastanza semplice. Dovrai aggiungere almeno un profilo per ricevere le email e almeno un'identità se vuoi inviarle. La configurazione rapida aggiungerà un profilo e un'identità in una sola volta per gran parte dei principali fornitori.

## Requisiti

È necessaria una connessione internet per configurare i profili e le identità.

## Configurazione rapida

Basta selezionare il provider appropriato o *Altri provider* e inserire il tuo nome, l'indirizzo email e la password, e toccare *Controlla*.

Questo funzionerà per gran parte dei provider email.

Se la configurazione rapida non funziona, dovrai configurare manualmente un profilo e un'identità, vedi sotto per le istruzioni.

## Configura il profilo - per ricevere le email

Per aggiungere un profilo, tocca *Configurazione manuale e altre opzioni*, tocca *Profili* e il pulsante 'più' in fondo e seleziona IMAP (o POP3). Seleziona un provider dall'elenco, inserisci il nome utente, che è prevalentemente il tuo indirizzo email e inserisci la tua password. Tocca *Controlla* per far connettere FairEmail al server email e recuperare un elenco delle cartelle di sistema. Dopo aver revisionato la selezione delle cartelle di sistema, puoi aggiungere il profilo toccando *Salva*.

Se il tuo provider non è nell'elenco, ce ne sono a migliaia, seleziona *Personalizzato*. Inserisci il nome del dominio, ad esempio *gmail.com* e tocca *Ottieni le impostazioni*. Se il tuo provider supporta [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail compilerà il nome dell'host e il numero di porta, altrimenti controlla le istruzioni di configurazione del tuo provider per il giusto nome dell'host IMAP, numero di porta e protocollo di crittografia (SSL/TLS o STARTTLS). Per altro a riguardo, sei pregato di vedere [qui](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configura l'identità - per inviare le email

Similmente, per aggiungere un'identità, tocca *Configurazione manuale e altre opzioni*, tocca *Identità* e il pulsante 'più' in fondo. Inserisci il nome che vuoi compaia nell'indirizzo del mittente delle email che invii e seleziona un profilo collegato. Tocca *Salva* per aggiungere l'identità.

Se il profilo è stato configurato manualmente, potresti dover configurare manualmente anche l'identità. Inserisci il nome del dominio, ad esempio *gmail.com* e tocca *Ottieni le impostazioni*. Se il tuo provider supporta [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail compilerà il nome dell'host e il numero di porta, altrimenti controlla le istruzioni di configurazione del tuo provider per il giusto nome dell'host SMTP, numero di porta e protocollo di crittografia (SSL/TLS o STARTTLS).

Vedi [questa FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sull'uso degli alias.

## Concedi le autorizzazioni - per accedere alle informazioni di contatto

Se vuoi cercare gli indirizzi email, visualizzare le foto di contatto, etc., dovrai concedere le autorizzazioni per leggere le informazioni di contatto a FairEmail. Basta toccare su *Autorizza* e selezionare *Consenti*.

## Configura le ottimizzazioni della batteria - per ricevere costantemente le email

Sulle versioni recenti di Android, Android metterà in standby le app quando lo schermo è spento per un po' di tempo per ridurre l'uso della batteria. Se vuoi ricevere le nuove email senza ritardo, dovresti disabilitare le ottimizzazioni della batteria per FairEmail. Tocca *Gestisci* e segui le istruzioni.

## Domande o problemi

Se hai una domanda o un problema, sei pregato di [vedere qui](https://github.com/M66B/FairEmail/blob/master/FAQ.md) per aiuto.