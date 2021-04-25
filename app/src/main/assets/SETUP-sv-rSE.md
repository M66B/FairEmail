# Hjälp med installation

Att ställa in FairEmail är ganska enkelt. Du måste lägga till minst ett konto för att få e-post och minst en identitet om du vill skicka e-post. Snabbinstallationen kommer att lägga till ett konto och en identitet på en gång för de flesta stora leverantörer.

## Krav

En Internetanslutning krävs för att ställa in konton och identiteter.

## Snabbinstallation

Välj bara lämplig leverantör eller *Annan leverantör* och ange ditt namn, e-postadress och lösenord och tryck på *Kontrollera*.

Detta kommer att fungera för de flesta e-postleverantörer.

Om snabbinstallationen inte fungerade måste du ställa in ett konto och en identitet manuellt, se nedan för instruktioner.

## Ställ in konto - för att ta emot e-post

För att lägga till ett konto tryck på *Manuell inställning och fler alternativ*, tryck på *Konton* och tryck på 'plus' knappen längst ner och välj IMAP (eller POP3). Välj en leverantör från listan, ange användarnamnet, vilket är mestadels din e-postadress och ange ditt lösenord. Tryck på *Kontrollera* för att låta FairEmail ansluta till e-postservern och hämta en lista över systemmappar. Efter att ha granskat valet av systemmapp kan du lägga till kontot genom att trycka på *Spara*.

Om din leverantör (som det finns tusentals av) inte finns med i listan över leverantörer, välj *Custom*. Ange domännamnet, till exempel *gmail.com* och tryck på *Hämta inställningar*. Om din leverantör stödjer [auto-discovery](https://tools.ietf.org/html/rfc6186), fyller FairEmail i värdnamnet och portnumret, om inte kontrollerar du installationsinstruktionerna för din leverantör för rätt IMAP-värdnamn, portnummer och protokoll (SSL/TLS eller STARTTLS). För mer information om detta, vänligen se [här](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Ställ in identitet - för att skicka e-post

På samma sätt trycker du på *Manuell installation och fler alternativ*, för att lägga till en identitet. tryck på *Identiteter* och tryck på 'plus' knappen längst ned. Ange det namn du vill visa i de från adressen till de e-postmeddelanden du skickar och välj ett länkat konto. Tryck på *Spara* för att lägga till identiteten.

Om kontot konfigurerades manuellt behöver du troligen också konfigurera identiteten manuellt. Ange domännamnet, till exempel *gmail.com* och tryck på *Hämta inställningar*. Om din leverantör stödjer [auto-discovery](https://tools.ietf.org/html/rfc6186), fyller FairEmail i värdnamnet och portnumret, om inte kontrollerar du installationsinstruktionerna för din leverantör för rätt SMTP-värdnamn, portnummer och protokoll (SSL/TLS eller STARTTLS).

Se [Vanliga frågor](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) om hur du använder alias.

## Bevilja behörigheter - för att få tillgång till kontaktinformation

Om du vill kunna slå upp e-postadresser, visa kontaktfoton, o. s. v, måste du bevilja att FairEmail får tillgång till att läsa kontaktinformation. Tryck bara på *Bevilja behörigheter* och välj *Tillåt*.

## Ställ in batterioptimeringar - för att kontinuerligt ta emot e-post

På de senaste Android-versionerna kommer Android att sätta appar i viloläge när skärmen är avstängd under en tid för att minska batterianvändningen. Om du vill ta emot nya e-postmeddelanden utan förseningar bör du inaktivera batterioptimeringar för FairEmail. Tryck på *Hantera* och följ instruktionerna.

## Frågor eller problem

Om du har en fråga eller ett problem, vänligen [se här](https://github.com/M66B/FairEmail/blob/master/FAQ.md) för hjälp.