# Opsætningshjælp

At opsætte FairEmail er ret enkelt. Du skal tilføje mindst én konto for at modtage e-mail og mindst én identitet for at sende e-mail. Hurtigopsætningen tilføjer på én gang en konto og identitet for de fleste større udbydere.

## Forudsætninger

En Internetforbindelse kræves for at opsætte konti og identiteter.

## Hurtig opsætning

Ma vælger blot den relevante udbyder eller *Anden udbyder*, angiver sit navn, e-mailadresse og adgangskode, og trykker på *Tjek*.

Dette vil virke for de fleste e-mailudbydere.

Fungerer hurtigopsætningen ikke, så skal en konto og identitet opsættes manuelt (tjek instruktionerne nedenfor).

## Opsæt konto - for at modtage e-mail

For kontotilføjelse trykkes på *Manuel opsætning og flere muligheder*, *Konti*, 'plus'-knappen nederst, og valg af IMAP (eller POP3). Vælg en udbyder på listen, angiv brugernavnet, som for det meste er din e-mail, og angiv dit adgangskode. Tryk *Tjek* for at lade FairEmail forbinde til e-mailserveren og hente en liste over systemmapper. Efter gennemgang af systemmappeudvalget kan du tilføje kontoen ved at trykke på*Gem*.

Er udbyderen (af hvilke der er tusindvis) ikke på udbyderlisten, så vælg *Tilpasset*. Angiv domænenavnet, f.eks. *gmail.com*, og tryk på *Hent indstillinger*. Understøtter udbyderen [auto-discovery](https://tools.ietf.org/html/rfc6186), auto-udfylder FairEmail værtsnavn og portnummer, ellers tjek udbyderens opsætningsvejledning vedr. korrekt IMAP-værtsnavn, portnummer og protokol (SSL/TLS eller STARTTLS). For mere om dette, se [hér](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Opsæt identitet - for at sende e-mail

Tilsvarende for identitetstilføjelse, tryk på *Manuel opsætning og flere muligheder*, *Identiteter* og knappen 'plus' nederst. Angiv det navn, du ønsker vist i fra-adressen i e-mails, du sender, og vælg en tilknyttet konto. Tryk *Gem* for at tilføje identiteten.

Blev kontoen opsat manuelt, skal du sandsynligvis også opsætte identiteten manuelt. Angiv domænenavnet, f.eks. *gmail.com*, og tryk på *Hent indstillinger*. Understøtter udbyderen [auto-discovery](https://tools.ietf.org/html/rfc6186), auto-udfylder FairEmail værtsnavn og portnummer, ellers tjek udbyderens opsætningsvejledning vedr. korrekt SMTP-værtsnavn, portnummer og krypteringsprotokol (SSL/TLS eller STARTTLS).

Se [denne FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) vedr. brug af aliasser.

## Giv tilladelser - for at tilgå kontaktoplysninger

Ønkes e-mailadresseopslag, kontaktfotovisning mv., skal FairEmail tildeles læs kontaktinformation-tilladelse. Tryk blot på *Tildel* og vælg *Tillad*.

## Opsæt batterioptimeringer - til løbende modtagelse af e-mail

I nyere Android-versioner sættes apps i dvale for at reducere batteriforbruget, når skærmen har været slukket i nogen tid. Vil du gerne modtage nye e-mails uden forsinkelser, skal du deaktivere batterioptimeringer for FairEmail. Tryk på *Håndtér* og følg vejledningen.

## Spørgsmål eller problemer

Har du et spørgsmål eller problem, så [kig venligst her](https://github.com/M66B/FairEmail/blob/master/FAQ.md) for hjælp.