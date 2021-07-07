# Hulp bij instellen

Het instellen van FairEmail is vrij eenvoudig. U moet ten minste één account toevoegen om e-mail te ontvangen en ten minste één identiteit om e-mail te verzenden. Snel instellen voegt in één keer een account en een identiteit toe voor de meeste grote providers.

## Vereisten

Een internetverbinding is vereist om accounts en identiteiten in te stellen.

## Snel instellen

Selecteer gewoon de juiste provider of *Andere provider* en voer uw naam, e-mailadres en wachtwoord in en tik *Controleer*.

Dit werkt voor de meeste e-mailproviders.

Als het snelle instellen niet werkt, moet je een account en een identiteit op een andere manier instellen, zie hieronder voor instructies.

## Account instellen - om e-mail te ontvangen

Om een account toe te voegen, tik op *Handmatige setup en meer opties*, tik *Accounts* en tik op de 'plus' knop onderaan en selecteer IMAP (of POP3). Selecteer een provider uit de lijst, voer de gebruikersnaam in, meestal uw e-mailadres en voer uw wachtwoord in. Tik *Controleer* om FairEmail te laten verbinden met de e-mailserver en een lijst van systeemmappen op te laten halen. Na het controleren van de selectie van systeemmappen kunt u het account toevoegen door op *Bewaren* te klikken.

Als uw provider niet op de lijst van providers staat (er zijn duizenden providers), selecteer dan *Anders*. Voer de domeinnaam in, bijvoorbeeld *gmail.com* en tik op *Instellingen ophalen*. Als uw provider [auto-discovery](https://tools.ietf.org/html/rfc6186)ondersteunt, zal FairEmail de hostnaam en poortnummer invullen, controleer anders de installatie-instructies van uw provider voor de juiste IMAP host naam, poortnummer en encryptie protocol (SSL/TLS of STARTTLS). Voor meer informatie, zie [hier](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Identiteit instellen - om e-mail te verzenden

Hetzelfde geldt voor het toevoegen van een identiteit, tik op *Handmatige setup en meer opties*, tik *Identiteiten* en tik onderaan op de knop 'plus'. Voer de naam in die u wilt laten verschijnen in het 'van' adres van de e-mails die u verzendt en selecteer een gekoppeld account. Tik op *Opslaan* om de identiteit toe te voegen.

Als het account handmatig werd geconfigureerd, moet u waarschijnlijk ook de identiteit handmatig configureren. Voer de domeinnaam in, bijvoorbeeld *gmail.com* en tik op *Instellingen ophalen*. Als uw provider [auto-discovery](https://tools.ietf.org/html/rfc6186) ondersteunt, zal FairEmail de hostnaam en poortnummer invullen, controleer anders de installatie-instructies van uw provider voor de juiste SMTP host naam, poortnummer en encryptie protocol (SSL/TLS of STARTTLS).

Zie [deze FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) over het gebruik van aliassen.

## Toestemmingen verlenen - om contact informatie op te vragen

Als u e-mailadressen wilt opzoeken, contactfoto's wilt weergeven, enz. moet u toestemming geven om contactinformatie te lezen aan FairEmail. Tik op *Toestemmingen verlenen* en selecteer *Toestaan*.

## Accuoptimalisaties instellen - om voortdurend e-mail te ontvangen

Recente Android versies zetten apps in de slaapstand wanneer het scherm enige tijd uit is om het accugebruik te verminderen. Als u nieuwe e-mails zonder vertraging wilt ontvangen, dan moet u de accuoptimalisaties voor FairEmail uitschakelen. Tik op *Beheren* en volg de instructies.

## Vragen of problemen

Als u een vraag of probleem heeft, [zie hier](https://github.com/M66B/FairEmail/blob/master/FAQ.md) voor hulp.