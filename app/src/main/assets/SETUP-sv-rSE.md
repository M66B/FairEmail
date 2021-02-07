# Hjälp med installation

Att ställa in FairEmail är ganska enkelt. Du måste lägga till minst ett konto för att få e-post och minst en identitet om du vill skicka e-post. The quick setup will add an account and an identity in one go for most providers.

## Krav

En Internetanslutning krävs för att ställa in konton och identiteter.

## Snabbinstallation

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Ställ in konto - för att ta emot e-post

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Välj en leverantör från listan, ange användarnamnet, vilket är mestadels din e-postadress och ange ditt lösenord. Tryck på *Kontrollera* för att låta FairEmail ansluta till e-postservern och hämta en lista över systemmappar. Efter att ha granskat valet av systemmapp kan du lägga till kontot genom att trycka på *Spara*.

If your provider is not in the list of providers (there are thousands of providers), select *Custom*. Ange domännamnet, till exempel *gmail.com* och tryck på *Hämta inställningar*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). För mer information om detta, vänligen se [här](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Ställ in identitet - för att skicka e-post

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Ange det namn du vill visa i de från adressen till de e-postmeddelanden du skickar och välj ett länkat konto. Tryck på *Spara* för att lägga till identiteten.

Om kontot konfigurerades manuellt behöver du troligen också konfigurera identiteten manuellt. Ange domännamnet, till exempel *gmail.com* och tryck på *Hämta inställningar*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Se [Vanliga frågor](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) om hur du använder alias.

## Bevilja behörigheter - för att få tillgång till kontaktinformation

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Ställ in batterioptimeringar - för att kontinuerligt ta emot e-post

På de senaste Android-versionerna kommer Android att sätta appar i viloläge när skärmen är avstängd under en tid för att minska batterianvändningen. Om du vill ta emot nya e-postmeddelanden utan förseningar bör du inaktivera batterioptimeringar för FairEmail. Tap *Manage* and follow the instructions.

## Frågor eller problem

Om du har en fråga eller ett problem, vänligen [se här](https://github.com/M66B/FairEmail/blob/master/FAQ.md) för hjälp.