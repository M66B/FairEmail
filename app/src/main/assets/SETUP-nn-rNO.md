# Hjelp til oppsett

Det er ganske enkelt å sette opp FairEmail. Du må legge til minst en konto for å motta e-post og minst en identitet hvis du vil sende e-post. Hurtigoppsettet vil legge til konto og identitet på en gang for de fleste store leverandører.

## Krav

Det kreves en internettforbindelse for å konfigurere kontoer og identiteter.

## Hurtigoppsett

Bare velg riktig leverandør eller *Annen leverandør* og skriv inn navn, e-postadresse og passord og trykk *Sjekk*.

Dette vil fungere for de fleste e-postleverandører.

Hvis hurtigoppsettet ikke fungerer, må du angi en konto og en identitet manuelt, se nedenfor for instruksjoner.

## Oppsett av konto - for å motta e-post

Trykk på *Manuell oppsett og flere alternativer*for å legge til en konto. Trykk på *Kontoer* og trykk på 'pluss' knappen nederst og velg IMAP (eller POP3). Velg en leverandør fra listen, skriv inn brukernavn (vanligvis e-postadressen din), og skriv deretter inn passordet ditt. Trykk på *Kontroller* for å la FairEmail koble til e-posttjeneren og hente en liste over systemmapper. Etter å ha gjennomgått valg av systemmappe, kan du legge til kontoen ved å trykke *Lagre*.

Hvis leverandøren din ikke er på listen over leverandører, det er tusenvis av leverandører, velg *Tilpasset*. Skriv inn domenenavnet, for eksempel *gmail.com* og trykk *Hent innstillinger*. Hvis leverandøren din støtter [auto-oppdagelse](https://tools.ietf.org/html/rfc6186), vil FairEmail fylle ut vertsnavnet og portnummeret, den vil også sjekke installasjonsinstruksjonene til leverandøren din for riktig SMTP vertsnavn, portnummer og protokoll (SSL/TLS eller STARTTLS). For mer om dette, vennligst se [her](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Oppsett av identitet - for å sende e-post

For å legge til en identitet, trykk *Manuelt oppsett og flere alternativer*, trykk *Identiteter* og trykk på 'pluss' knappen nederst. Skriv inn navnet du vil skal vises i 'Fra-adressen' på e-postene du sender, og velg en tilkoblet konto. Trykk på *Lagre* for å legge til identiteten.

Hvis kontoen ble konfigurert manuelt, må du sannsynligvis konfigurere identiteten manuelt også. Skriv inn domenenavnet, for eksempel *gmail.com* og trykk *Hent innstillinger*. Hvis leverandøren din støtter [auto-oppdagelse](https://tools.ietf.org/html/rfc6186), vil FairEmail fylle ut vertsnavnet og portnummeret, den vil også sjekke installasjonsinstruksjonene til leverandøren din for riktig SMTP vertsnavn, portnummer og protokoll (SSL/TLS eller STARTTLS).

Se [OSS](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) angående bruk av aliaser.

## Gi tillatelser - for tilgang til kontaktinformasjon

Hvis du vil slå opp e-postadresser, vise kontaktbilder, osv., må du gi FairEmail tillatelse til å lese kontakter. Bare trykk på *Gi tillatelser* og velg *Tillat*.

## Konfigurer batterioptimaliseringer - for kontinuerlig å motta e-post

I nyere versjoner av Android, legges apper automatisk i dvale når skjermen er slått av en stund for å redusere batteribruken. Hvis du vil motta nye e-postmeldinger uten forsinkelser, bør du deaktivere batterioptimaliseringer for FairEmail. Trykk på *Administrer* og følg instruksjonene.

## Spørsmål eller problemer

Hvis du har spørsmål eller problemer, kan du [se her](https://github.com/M66B/FairEmail/blob/master/FAQ.md) etter hjelp.