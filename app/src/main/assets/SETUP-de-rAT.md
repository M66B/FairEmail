# Einrichtungshilfe

Es ist ziemlich einfach FairEmail einzurichten. Sie müssen mindestens ein Konto hinzufügen um E-Mails zu empfangen, und mindestens eine Identität um E-Mails zu senden. Die Schnelleinrichtung wird ein Konto und eine Identität in einem Schritt für die meisten großen Anbieter hinzufügen.

## Anforderungen

Für die Einrichtung von Konten und Identitäten ist eine Internetverbindung erforderlich.

## Schnelleinrichtung

Wählen Sie einfach den passenden Anbieter oder *anderen Anbieter* aus und geben Sie Ihren Namen, Ihre E-Mail-Adresse und Ihr Passwort ein und tippen Sie auf *Überprüfen*.

Dies funktioniert für die meisten E-Mail-Anbieter.

Wenn die Schnelleinrichtung nicht funktioniert, müssen Sie Konto und Identität manuell einrichten, siehe Anweisungen unten.

## Konto einrichten – um E-Mails zu empfangen

Um ein Konto hinzuzufügen, tippen Sie auf *Manuelle Einrichtung und weitere Optionen*, Tippen Sie auf *Konten* und tippen Sie unten auf 'plus' und wählen Sie IMAP (oder POP3). Wählen Sie einen Anbieter aus der Liste, geben Sie den Benutzernamen ein, der meistens Ihre E-Mail-Adresse ist, und geben Sie Ihr Passwort ein. Tippen Sie auf *Prüfen* um FairEmail mit dem E-Mail-Server zu verbinden und eine Liste von Systemordnern zu laden. Nach der Überprüfung der Systemordnerauswahl können Sie das Konto hinzufügen, indem Sie auf *Speichern* klicken.

Wenn Ihr Anbieter nicht in der Liste der Anbieter ist, wählen Sie *Benutzerdefiniert*. Geben Sie den Domain-Namen ein, zum Beispiel *gmail.com* und tippen Sie auf *Einstellungen abrufen*. Wenn Ihr Anbieter [Auto-discovery](https://tools.ietf.org/html/rfc6186) (automatische Konfiguration) unterstützt, wird FairEmail den Hostnamen und die Portnummer ausfüllen, so dass Sie die richtigen Angaben Ihres Providers für IMAP-Hostnamen, Port-Nummer und Protokoll (SSL/TLS oder STARTTLS) nur noch überprüfen müssen. Weitere Informationen dazu finden Sie [hier](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Identität einrichten – um E-Mail zu senden

Um eine Identität hinzuzufügen, tippen Sie *Manuelle Einrichtung und weitere Optionen*, tippen Sie auf *Identitäten* und tippen Sie unten auf 'plus'. Bitte den Namen eingeben, der in der Absenderadresse der von Ihnen gesendeten E-Mails erscheinen soll, und wählen Sie ein verknüpftes Konto aus. Tippen Sie auf *Speichern*, um die Identität hinzuzufügen.

Wenn das Konto manuell konfiguriert wurde, müssen Sie wahrscheinlich auch die Identität manuell konfigurieren. Geben Sie den Domain-Namen ein, zum Beispiel *gmail.com* und tippen dann auf *Einstellungen abrufen*. Wenn Ihr Anbieter [Auto-discovery](https://tools.ietf.org/html/rfc6186) (automatische Konfiguration) unterstützt, wird FairEmail den Hostnamen und die Portnummer ausfüllen, so dass Sie die richtigen Angaben Ihres Providers für IMAP-Hostnamen, Port-Nummer und Protokoll (SSL/TLS oder STARTTLS) nur noch überprüfen müssen.

Zur Verwendung von Alias-Adressen bitte [diese F&A](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) beachten.

## Berechtigungen gewähren – um auf Kontaktinformationen zugreifen zu können

Wenn Sie nach E-Mail-Adressen suchen möchten, Kontaktfotos anzeigen lassen möchten, müssen Sie FairEmail die Erlaubnis erteilen, die Kontaktinformationen zu lesen. Einfach auf *Berechtigungen erteilen* tippen und *Zulassen* auswählen.

## Akku-Optimierungen einrichten, um E-Mails fortlaufend zu erhalten

Bei den aktuellen Android-Versionen wird Android alle Apps zum Schlafen bringen, wenn der Bildschirm für einige Zeit ausgeschaltet ist, um die Akkunutzung zu reduzieren. Wenn Sie neue E-Mails ohne Verzögerung erhalten möchten, sollten Sie die Akkuoptimierung für FairEmail deaktivieren. Bitte auf *Verwalten* tippen und den Anweisungen folgen.

## Fragen oder Probleme

Wenn Sie eine Frage oder ein Problem haben, können Sie [hier Hilfe](https://github.com/M66B/FairEmail/blob/master/FAQ.md) erhalten.