# Guida alla configurazione

Configurare FairEmail è semplice. Aggiungi almeno un profilo (account di posta) per ricevere le email e almeno un'identità se vuoi inviarle. La configurazione rapida aggiungerà un profilo e un'identità in modalità guidata (valida per i principali provider di posta).

## Requisiti

È necessaria una connessione internet per configurare i profili e le identità.

## Configurazione rapida

Seleziona il tuo provider dalla lista o tocca *Altri provider*, inserisci i tuoi dati di accesso e infine tocca *Controlla*.

non sarà necessario configurare altro.

Se la configurazione rapida non funziona, dovrai impostare manualmente un profilo e un'identità: leggi le istruzioni qui sotto.

## Configura il profilo - per ricevere le email

Per aggiungere un profilo, tocca *Configurazione manuale e altre opzioni*, tocca *Profili* e il pulsante 'più' in fondo e seleziona IMAP (o POP3). Seleziona un provider dall'elenco, inserisci il nome utente, che è quasi sempre il tuo indirizzo email, e la tua password. Tocca *Controlla* per collegare FairEmail al server e recuperare l'elenco delle cartelle di posta. Controlla le cartelle selezionate, poi conferma l'aggiunta dell'account con il tasto *Salva*.

I provider di posta sono migliaia: se il tuo non è nell'elenco, seleziona *Personalizzato*. Inserisci il nome del dominio (es. *gmail.com*) e tocca *Scarica le impostazioni*. Se il tuo provider supporta [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail compilerà il nome dell'host e il numero di porta; altrimenti cerca le impostazioni più recenti sul sito del tuo provider. I parametri obbligatori sono: host IMAP, numero di porta e protocollo di crittografia (SSL/TLS o STARTTLS). Clicca [qui](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts) se ti occorrono altre informazioni.

## Configura l'identità - per inviare le email

Per aggiungere un'identità, tocca *Configurazione manuale e altre opzioni*, poi *Identità* e infine il pulsante + ('più') in fondo. Inserisci il tuo nome (quello che apparirà ai destinatari come mittente), poi scegli un account a cui collegarlo. Tocca *Salva* per aggiungere l'identità.

Se il profilo è stato configurato manualmente, potresti dover configurare manualmente anche l'identità. Inserisci il nome del dominio, ad esempio *gmail.com* e tocca *Scarica le impostazioni*. Se il tuo provider supporta [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail compilerà il nome dell'host e il numero di porta; altrimenti cerca le impostazioni più recenti sul sito del tuo provider. I parametri obbligatori sono: host IMAP, numero di porta e protocollo di crittografia (SSL/TLS o STARTTLS).

Per l'utilizzo degli alias, vedi [questa FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9).

## Configura l'accesso ai contatti (Rubrica di sistema)

Se vuoi cercare gli indirizzi email dei tuoi contatti Google, visualizzare le foto e rendere più semplice l'interazione con la rubrica di sistema, dovrai autorizzare FairEmail a leggere le informazioni di contatto. Basta toccare *Autorizza* e di seguito *Consenti*.

## Consentire a FairEmail di ricevere le email in tempo reale

Per limitare al massimo l'uso della batteria, Android chiude automaticamente le app in background dopo un certo numero di minuti. Le uniche app che possono funzionare in background sono quelle di sistema e quelle a cui l'utente concede le autorizzazioni necessarie. Tocca *Gestisci* e segui le istruzioni.

## Altro

Se hai una domanda o un problema, sei pregato di [controllare qui](https://github.com/M66B/FairEmail/blob/master/FAQ.md) per trovare una risposta o chiedere assistenza.