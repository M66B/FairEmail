# Aiuto configurazione

Configurare FairEmail è abbastanza semplice. Dovrai aggiungere almeno un profilo per ricevere le email e almeno un'identità se vuoi inviare le email. The quick setup will add an account and an identity in one go for most providers.

## Requisiti

Una connessione internet è necessaria per configurare profili e identità.

## Configurazione rapida

Basta selezionare il provider appropriato o *Altro provider* e inserire il tuo nome, indirizzo email e password e cliccare *Controlla*.

Questo funzionerà per gran parte dei provider di email.

Se la configurazione rapida non funziona, dovrai impostare un account e un'identità manualmente, vedi qui sotto per le istruzioni.

## Configura il profilo - per ricevere le email

Per aggiungere un account, clicca *Configurazione manuale e più opzioni*, clicca *Account* e clicca il pulsante 'plus' in basso e seleziona IMAP (o POP3). Seleziona un provider dall'elenco, inserisci il nome utente, solitamente il tuo indirizzo email, e inserisci la tua password. Tocca *Controlla* per far connettere FairEmail al server email e recuperare un elenco delle cartelle di sistema. Dopo aver revisionato la selezione delle cartelle del sistema, puoi aggiungere il tuo profilo toccando *Salva*.

If your provider is not in the list of providers (there are thousands of providers), select *Custom*. Inserisci il nome del dominio, ad esempio *gmail.com* e tocca *Ottieni impostazioni*. Se il tuo provider supporta [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail compilerà l'host name e il numero di porta, altrimenti controlla le istruzioni di configurazione del tuo provider per il giusto host name IMAP, il numero di porta e il protocollo di crittografia (SSL/TLS o STARTTLS). Per altro a riguardo, sei pregato di vedere [qui](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configura l'identità - per inviare email

Allo stesso modo, per aggiungere un'identità, clicca *Configurazione manuale e più opzioni*, clicca *Identità* e clicca il pulsante 'plus' in basso. Inserisci il nome che vuoi compaia nell'indirizzo del mittente delle email che invii e seleziona un profilo collegato. Tocca *Salva* per aggiungere l'identità.

Se il profilo è stato configurato manualmente, potresti dover configurare manualmente anche l'identità. Inserisci il nome del dominio, ad esempio *gmail.com* e tocca *Ottieni impostazioni*. Se il tuo provider supporta [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail compilerà l'host name e il numero di porta, altrimenti controlla le istruzioni di configurazione del tuo provider per il giusto host name SMTP, il numero di porta e il protocollo di crittografia (SSL/TLS o STARTTLS).

Vedi [questa FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sull'utilizzo degli alias.

## Concedi i permessi - per accedere alle informazioni di contatto

Se si desidera cercare gli indirizzi email, avere le foto di contatto mostrate, ecc, è necessario concedere l'autorizzazione per leggere le informazioni di contatto a FairEmail. Basta cliccare*Concedi permessi* e selezionare *Consenti*.

## Configura le ottimizzazioni della batteria - per ricevere continuamente le email

Sulle versioni Android recenti, Android metterà in standby le app quando lo schermo è spento per un po' di tempo per ridurre l'uso della batteria. Se vuoi ricevere le nuove email senza ritardo, dovresti disabilitare le ottimizzazioni della batteria per FairEmail. Clicca *Gestisci* e segui le istruzioni.

## Domande o problemi

Se hai una domanda o un problema, sei pregato di [vedere qui](https://github.com/M66B/FairEmail/blob/master/FAQ.md) per aiuto.