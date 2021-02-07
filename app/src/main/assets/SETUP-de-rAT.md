# Einrichtungshilfe

Es ist ziemlich einfach FairEmail einzurichten. Sie müssen mindestens ein Konto hinzufügen um E-Mails zu empfangen, und mindestens eine Identität um E-Mails zu senden. Die Schnelleinrichtung (Assistent) richtet in einem Vorgang ein Konto und eine Identität für die meisten großen E-Mail-Anbieter ein.

## Anforderungen

Für die Einrichtung von Konten und Identitäten ist eine Internetverbindung erforderlich.

## Schnelleinrichtung

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Konto einrichten – um E-Mails zu empfangen

Um ein Konto hinzuzufügen, tippen Sie auf *Manuelle Einrichtung und weitere Optionen*, Tippen Sie auf *Konten* und tippen Sie unten auf 'plus' und wählen Sie IMAP (oder POP3). Wählen Sie einen Anbieter aus der Liste, geben Sie den Benutzernamen ein, der meistens Ihre E-Mail-Adresse ist, und geben Sie Ihr Passwort ein. Tippen Sie auf *Prüfen* um FairEmail mit dem E-Mail-Server zu verbinden und eine Liste von Systemordnern zu laden. Nach der Überprüfung der Systemordnerauswahl können Sie das Konto hinzufügen, indem Sie auf *Speichern* klicken.

If your provider is not in the list of providers (there are thousands of providers), select *Custom*. Geben Sie den Domain-Namen ein, zum Beispiel *gmail.com* und tippen Sie auf *Einstellungen abrufen*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Weitere Informationen dazu finden Sie [hier](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Identität einrichten – um E-Mail zu senden

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Bitte den Namen eingeben, der in der Absenderadresse der von Ihnen gesendeten E-Mails erscheinen soll, und wählen Sie ein verknüpftes Konto aus. Tippen Sie auf *Speichern*, um die Identität hinzuzufügen.

Wenn das Konto manuell konfiguriert wurde, müssen Sie wahrscheinlich auch die Identität manuell konfigurieren. Geben Sie den Domain-Namen ein, zum Beispiel *gmail.com* und tippen dann auf *Einstellungen abrufen*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Zur Verwendung von Alias-Adressen bitte [diese F&A](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) beachten.

## Berechtigungen gewähren – um auf Kontaktinformationen zugreifen zu können

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Akku-Optimierungen einrichten, um E-Mails fortlaufend zu erhalten

Bei den aktuellen Android-Versionen wird Android alle Apps zum Schlafen bringen, wenn der Bildschirm für einige Zeit ausgeschaltet ist, um die Akkunutzung zu reduzieren. Wenn Sie neue E-Mails ohne Verzögerung erhalten möchten, sollten Sie die Akkuoptimierung für FairEmail deaktivieren. Tap *Manage* and follow the instructions.

## Fragen oder Probleme

Wenn Sie eine Frage oder ein Problem haben, können Sie [hier Hilfe](https://github.com/M66B/FairEmail/blob/master/FAQ.md) erhalten.