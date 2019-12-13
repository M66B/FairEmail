# Aiuto impostazione

La configurazione di FairEmail è abbastanza semplice. Dovrai aggiungere almeno un account per ricevere email e almeno un'identità se vuoi inviare email. La configurazione rapida aggiungerà un account e un'identità in un solo colpo per la maggior parte dei principali provider.

## Requisiti

È necessaria una connessione internet per configurare account e identità.

## Configurazione rapida

Inserisci il tuo nome, indirizzo email e password e tocca *Vai*.

Questo funzionerà per la maggior parte dei principali provider di posta elettronica.

Se la configurazione rapida non funziona, dovrai impostare un account e un'identità in un altro modo, vedere sotto per le istruzioni.

## Imposta account - per ricevere email

Per aggiungere un account, tocca *Gestisci account* e tocca il pulsante arancione *aggiungi* in basso. Seleziona un provider dalla lista, inserisci il nome utente, che di solito corrisponde al tuo indirizzo email e inserisci la password. Tocca *Controlla* per consentire a FairEmail di connettersi al server email e recuperare una lista di cartelle di sistema. Dopo aver esaminato la selezione della cartella di sistema è possibile aggiungere l'account toccando *Salva*.

Se il tuo provider non è nella lista dei provider, seleziona *Custom*. Inserisci il nome del dominio, ad esempio *gmail.com* e tocca *Ottieni impostazioni*. Se il tuo provider supporta [l'auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail riempirà il nome host e il numero di porta, altrimenti controlla le istruzioni di installazione del tuo provider per il corretto nome host IMAP, il numero di porta e il protocollo (SSL/TLS o STARTTLS). Per ulteriori informazioni, si prega di vedere [qui](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Imposta identità - per inviare email

Analogamente, per aggiungere un'identità, tocca *Gestisci identità* e tocca il pulsante arancione *aggiungi* in basso. Inserisci il nome che vuoi appaia nell'indirizzo del mittente e seleziona un account collegato. Tocca *Salva* per aggiungere l'identità.

Se l'account è stato configurato manualmente, è probabile che sia necessario configurare l'identità anche manualmente. Inserisci il nome del dominio, ad esempio *gmail.com* e tocca *Ottieni impostazioni*. Se il tuo provider supporta [l'auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail riempirà il nome host e il numero di porta, altrimenti controlla le istruzioni di installazione del tuo provider per il nome host SMTP corretto, il numero di porta e il protocollo (SSL/TLS o STARTTLS).

Vedi [questa FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sull'utilizzo degli alias.

## Concedi permessi - per accedere alle informazioni di contatto

Se vuoi cercare indirizzi email, visualizzare le foto dei contatto, ecc, dovrai concedere il permesso di lettura dei contatti a FairEmail. Basta toccare *Concedi permessi* e selezionare *Consenti*.

## Imposta ottimizzazioni batteria - per ricevere e-mail senza soluzione di continuità

Nelle versioni recenti di Android, Android metterà le app in standby quando lo schermo è spento per un certo tempo per ridurre l'uso della batteria. Se vuoi ricevere nuove email senza ritardi, dovresti disabilitare le ottimizzazioni della batteria per FairEmail. Tocca *Disabilita le ottimizzazioni della batteria* e segui le istruzioni.

## Domande o problemi

Se hai una domanda o un problema, per favore [vedi qui](https://github.com/M66B/FairEmail/blob/master/FAQ.md) per aiuto.