# Setuphilfe

FairEmail einzurichten ist ziemlich einfach. Sie müssen mindestens ein Konto hinzufügen, um E-Mail zu erhalten, und mindestens eine Identität, wenn Sie E-Mail senden möchten. Die Schnelleinstellung wird ein Konto und eine Identität in einem Vorgang für die meisten großen Anbieter erstellen.

## Anforderungen

Für die Einrichtung von Konten und Identitäten ist eine Internetverbindung erforderlich.

## Schnelleinrichtung

Geben Sie einfach Ihren Namen, Ihre E-Mail-Adresse und Ihr Passwort ein und tippen Sie auf *prüfen*.

Dies funktioniert für die meisten großen E-Mail-Anbieter.

Wenn die Schnelleinrichtung nicht funktioniert, müssen Sie ein Konto und eine Identität auf andere Weise einrichten, siehe unten für Anweisungen dazu.

## Konto einrichten - um E-Mail zu erhalten

Um ein Konto hinzuzufügen, tippen Sie unter 'Konten einrichten' auf *verwalten* und tippen Sie dann auf die orange *Plus-Zeichen* Taste unten. Wählen Sie einen Anbieter aus der Liste, geben Sie den Benutzernamen ein, der meistens Ihre E-Mail-Adresse ist, und geben Sie Ihr Passwort ein. Tippen Sie auf *Prüfen* um FairEmail mit dem E-Mail-Server zu verbinden und eine Liste von Systemordnern zu laden. Nach der Überprüfung der Systemordner-Auswahl können Sie das Konto hinzufügen, indem Sie auf *Speichern*klicken.

Wenn Ihr Provider nicht in der Liste der Anbieter ist, wählen Sie *Benutzerdefiniert*. Geben Sie den Domain-Namen ein, zum Beispiel *gmail.com* und tippen Sie auf *Einstellungen abrufen*. Wenn Ihr Provider [Auto-discovery](https://tools.ietf.org/html/rfc6186)unterstützt, wird FairEmail den Hostnamen und die Portnummer ausfüllen, so dass Sie die Angaben Ihres Providers für den richtigen IMAP-Hostnamen, Port-Nummer und Protokoll (SSL/TLS oder STARTTLS) nur noch überprüfen müssen. Weitere Informationen dazu finden Sie [hier](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Identität einrichten - um E-Mail zu senden

Vergleichbar mit der Konto-Einrichtung, tippen Sie unter 'Identitäten einrichten' auf *Verwalten* und tippen Sie dann auf die orange *Plus-Zeichen* Taste unten. Geben Sie den Namen ein, den Sie in E-Mails, die Sie von dieser Absender-Adresse senden, erscheinen möchten, und wählen Sie ein verknüpftes Konto. Tippen Sie auf *Speichern*, um die Identität hinzuzufügen.

Wenn das Konto manuell konfiguriert wurde, müssen Sie wahrscheinlich auch die Identität manuell konfigurieren. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and protocol (SSL/TLS or STARTTLS).

See [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) about using aliases.

## Grant permissions - to access contact information

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant read contacts permission to FairEmail. Just tap *Grant permissions* and select *Allow*.

## Setup battery optimizations - to continuously receive email

On recent Android versions, Android will put apps to sleep when the screen is off for some time to reduce battery usage. If you want to receive new emails without delays, you should disable battery optimizations for FairEmail. Tap *Disable battery optimizations* and follow the instructions.

## Questions or problems

If you have a question or problem, please [see here](https://github.com/M66B/FairEmail/blob/master/FAQ.md) or use [this contact form](https://contact.faircode.eu/?product=fairemailsupport) to ask for help (you can use the transaction number "*setup help*").