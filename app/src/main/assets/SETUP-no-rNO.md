# Hjelp til oppsett

Det er ganske enkelt å sette opp FairEmail. Du må legge til minst en konto for å motta e-post og minst en identitet hvis du vil sende e-post. The quick setup will add an account and an identity in one go for most providers.

## Krav

Det kreves en internettforbindelse for å konfigurere kontoer og identiteter.

## Hurtigoppsett

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Oppsett av konto - for å motta e-post

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Velg en leverandør fra listen, skriv inn brukernavn (vanligvis e-postadressen din), og skriv deretter inn passordet ditt. Trykk på *Kontroller* for å la FairEmail koble til e-posttjeneren og hente en liste over systemmapper. Etter å ha gjennomgått valg av systemmappe, kan du legge til kontoen ved å trykke *Lagre*.

If your provider is not in the list of providers (there are thousands of providers), select *Custom*. Skriv inn domenenavnet, for eksempel *gmail.com* og trykk *Hent innstillinger*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). For mer om dette, vennligst se [her](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Oppsett av identitet - for å sende e-post

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Skriv inn navnet du vil skal vises i 'Fra-adressen' på e-postene du sender, og velg en tilkoblet konto. Trykk på *Lagre* for å legge til identiteten.

Hvis kontoen ble konfigurert manuelt, må du sannsynligvis konfigurere identiteten manuelt også. Skriv inn domenenavnet, for eksempel *gmail.com* og trykk *Hent innstillinger*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Se [OSS](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) angående bruk av aliaser.

## Gi tillatelser - for tilgang til kontaktinformasjon

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Konfigurer batterioptimaliseringer - for kontinuerlig å motta e-post

I nyere versjoner av Android, legges apper automatisk i dvale når skjermen er slått av en stund for å redusere batteribruken. Hvis du vil motta nye e-postmeldinger uten forsinkelser, bør du deaktivere batterioptimaliseringer for FairEmail. Tap *Manage* and follow the instructions.

## Spørsmål eller problemer

Hvis du har spørsmål eller problemer, kan du [se her](https://github.com/M66B/FairEmail/blob/master/FAQ.md) etter hjelp.