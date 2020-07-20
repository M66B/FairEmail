# Installasjonshjelp

Det er ganske enkelt å sette opp FairEmail. Du må legge til minst en konto for å motta e-post og minst en identitet hvis du vil sende e-post. Det raske oppsettet vil legge til en konto og en identitet på en gang for de fleste store leverandører.

## Krav

Det kreves en internettforbindelse for å konfigurere kontoer og identiteter.

## Hurtig oppsett

Bare skriv inn navn, e-postadresse og passord og trykk *Gå*.

Dette vil fungere for de fleste store e-postleverandører.

Hvis hurtigoppsettet ikke fungerer, må du konfigurere en konto og en identitet på en annen måte, se nedenfor for instruksjoner.

## Oppsett av konto - for å motta e-post

For å legge til en konto, trykk på *Administrer kontoer* og trykk på den oransje *legg til* knappen nederst. Velg en leverandør fra listen, skriv inn brukernavnet, som for det meste er din e-postadresse, og skriv inn passordet ditt. Trykk på *Kontroller* for å la FairEmail koble til e-postserveren og hente en liste over systemmapper. Etter å ha gjennomgått valg av systemmappe, kan du legge til kontoen ved å trykke *Lagre*.

Hvis leverandøren din ikke er på listen over leverandører, velger du *Tilpasset*. Skriv inn domenenavnet, for eksempel *gmail.com* og trykk *Hent innstillinger*. Hvis leverandøren din støtter [auto-oppdagelse](https://tools.ietf.org/html/rfc6186), vil FairEmail fylle ut vertsnavnet og portnummeret, ellers sjekk installasjonsinstruksjonene til leverandøren din for riktig IMAP-vertsnavn, portnummer og protokoll (SSL/TLS eller STARTTLS). For mer om dette, vennligst se [her](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Oppsett identitet - for å sende e-post

For å legge til en identitet, trykk på *Administrer identitet* og trykk på den oransje *legg til* -knappen nederst. Skriv inn navnet du vil skal vises i fra adressen til e-postene du sender, og velg en lenket konto. Trykk på *Lagre* for å legge til identiteten.

Hvis kontoen ble konfigurert manuelt, må du sannsynligvis konfigurere identiteten manuelt. Skriv inn domenenavnet, for eksempel *gmail.com* og trykk *Hent innstillinger*. Hvis leverandøren din støtter [auto-oppdagelse](https://tools.ietf.org/html/rfc6186), vil FairEmail fylle ut vertsnavnet og portnummeret, ellers sjekk installasjonsinstruksjonene til leverandøren din for riktig SMTP vertsnavn, portnummer og protokoll (SSL/TLS eller STARTTLS).

Se [denne FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) om bruk av aliaser.

## Gi tillatelser - for å få tilgang til kontaktinformasjon

Hvis du vil slå opp e-postadresser, ha kontaktbilder vist osv., Må du gi tillatelse til å lese kontakter til FairEmail. Bare trykk på *Gi tillatelser* og velg *Tillat*.

## Konfigurer batterioptimaliseringer - for kontinuerlig å motta e-post

På nyere Android-versjoner vil Android legge appene i dvale når skjermen er slått av en stund for å redusere batteribruken. Hvis du vil motta nye e-postmeldinger uten forsinkelser, bør du deaktivere batterioptimaliseringer for FairEmail. Trykk på *Deaktiver batterioptimaliseringer* og følg instruksjonene.

## Spørsmål eller problemer

Hvis du har et spørsmål eller et problem, kan du [se her](https://github.com/M66B/FairEmail/blob/master/FAQ.md) for hjelp.