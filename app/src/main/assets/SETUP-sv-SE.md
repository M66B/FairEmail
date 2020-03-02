# Hjälp med installation

Att ställa in FairEmail är ganska enkelt. Du måste lägga till minst ett konto för att få e-post och minst en identitet om du vill skicka e-post. Snabbinstallationen kommer att lägga till ett konto och en identitet på en gång för de flesta stora leverantörer.

## Krav

En Internetanslutning krävs för att ställa in konton och identiteter.

## Snabbinstallation

Skriv bara in ditt namn, e-postadress och lösenord och tryck på *Kör*.

Detta kommer att fungera för de flesta stora e-postleverantörer.

Om snabbinstallationen inte fungerar måste du konfigurera ett konto och en identitet på ett annat sätt, se nedan för instruktioner.

## Ställ in konto - för att ta emot e-post

Om du vill lägga till ett konto trycker du på *Hantera konton* och trycker på den orange knappen *lägg till* längst ner. Välj en leverantör från listan, ange användarnamnet, vilket är mestadels din e-postadress och ange ditt lösenord. Tryck på *Kontrollera* för att låta FairEmail ansluta till e-postservern och hämta en lista över systemmappar. Efter att ha granskat valet av systemmapp kan du lägga till kontot genom att trycka på *Spara*.

Om din leverantör inte finns i listan över leverantörer väljer du *Anpassad*. Ange domännamnet, till exempel *gmail.com* och tryck på *Hämta inställningar*. Om din leverantör stöder [automatisk upptäckt](https://tools.ietf.org/html/rfc6186), fyller FairEmail i värdnamnet och portnumret, annars kontrollerar du installationsinstruktionerna för din leverantör för rätt IMAP-värdnamn, portnummer och protokoll (SSL/TLS eller STARTTLS). För mer information om detta, vänligen se [här](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Ställ in identitet - för att skicka e-post

På samma sätt trycker du på *Hantera identitet* om du vill lägga till en identitet och trycker på den orange knappen *lägg till* längst ner. Ange det namn du vill visa i de från adressen till de e-postmeddelanden du skickar och välj ett länkat konto. Tryck på *Spara* för att lägga till identiteten.

Om kontot konfigurerades manuellt behöver du troligen också konfigurera identiteten manuellt. Ange domännamnet, till exempel *gmail.com* och tryck på *Hämta inställningar*. Om din leverantör stöder [automatisk upptäckt](https://tools.ietf.org/html/rfc6186), fyller FairEmail i värdnamnet och portnumret, annars kontrollerar du installationsinstruktionerna för din leverantör för rätt SMTP-värdnamn, portnummer och protokoll (SSL/TLS eller STARTTLS).

Se [Vanliga frågor](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) om hur du använder alias.

## Bevilja behörigheter - för att få tillgång till kontaktinformation

Om du vill slå upp e-postadresser, att kontaktfoton visas, o.s.v, måste du bevilja tillstånd för läsning av kontakter av FairEmail. Tryck bara på *Bevilja behörigheter* och välj *Tillåt*.

## Ställ in batterioptimeringar - för att kontinuerligt ta emot e-post

På de senaste Android-versionerna kommer Android att sätta appar i viloläge när skärmen är avstängd under en tid för att minska batterianvändningen. Om du vill ta emot nya e-postmeddelanden utan förseningar bör du inaktivera batterioptimeringar för FairEmail. Tryck på *Inaktivera batterioptimeringar* och följ instruktionerna.

## Frågor eller problem

Om du har en fråga eller ett problem, vänligen [se här](https://github.com/M66B/FairEmail/blob/master/FAQ.md) för hjälp.