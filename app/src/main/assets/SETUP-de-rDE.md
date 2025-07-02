# Einrichtungshilfe

Das Einrichten von FairEmail ist ziemlich einfach. Sie müssen mindestens ein Konto hinzufügen, um E-Mails zu empfangen, und mindestens eine Identität, um E-Mails zu senden. Die Schnelleinrichtung wird ein Konto und eine Identität für die meisten großen Anbieter in einem Schritt hinzufügen.

## Voraussetzungen

Eine Internetverbindung ist erforderlich, um Konten und Identitäten einzurichten.

## Schnelleinrichtung

Wählen Sie den passenden Anbieter oder tippen Sie auf *»Anderer Anbieter«*. Geben Sie Ihren Namen, Ihre E-Mail-Adresse und Ihr Passwort ein; tippen Sie danach auf *»Überprüfen«*.

Dies funktioniert für die meisten E-Mail-Anbieter.

Wenn die Schnelleinrichtung nicht funktioniert, müssen Sie Konto und Identität manuell einrichten; siehe Anweisungen unten.

## Konto einrichten, um E-Mails zu empfangen

Um ein Konto hinzuzufügen, tippen Sie bitte auf *»Manuelle Einrichtung und weitere Optionen«*, *»Konten«*. Tippen Sie auf das 'Plus' und wählen Sie IMAP (oder POP3) aus. Wählen Sie einen Anbieter aus der Liste, geben Sie den Benutzernamen ein – dieser ist meistens Ihre E-Mail-Adresse – und geben Sie Ihr Passwort ein. Tippen Sie auf *Prüfen*, um FairEmail mit dem E-Mail-Server zu verbinden und eine Liste von Systemordnern zu laden. Nach der Überprüfung der Systemordnerauswahl können Sie das Konto hinzufügen, indem Sie auf *Speichern* tippen.

Wenn Ihr Anbieter nicht in der Liste der Anbieter ist, wählen Sie *Benutzerdefiniert*. Geben Sie den Domainnamen ein, zum Beispiel *gmail.com* und tippen Sie auf *»Einstellungen abrufen«*. Wenn Ihr Anbieter [Auto-discovery](https://tools.ietf.org/html/rfc6186) (automatische Konfiguration) unterstützt, wird FairEmail den Hostnamen und die Portnummer ausfüllen, ansonsten folgen Sie den Anweisungen Ihres E-Mail-Anbieters für den IMAP-Hostnamen, die Portnummer und das Verschlüsselungsprotokoll (SSL/TLS oder STARTTLS). Weitere Informationen dazu finden Sie [hier](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Identität einrichten, um E-Mails zu senden

Um eine Identität hinzuzufügen, tippen Sie auf *»Manuelle Einrichtung und Kontooptionen«*, *»Identitäten«* und unten auf das »Plus«. Bitte den Namen eingeben, der in der Absenderadresse der von Ihnen gesendeten E-Mails erscheinen soll und wählen Sie ein verknüpftes Konto aus. Tippen Sie auf *Speichern*, um die Identität hinzuzufügen.

Wenn das Konto manuell konfiguriert wurde, müssen Sie wahrscheinlich auch die Identität manuell konfigurieren. Geben Sie den Domainnamen ein, zum Beispiel *gmail.com* und tippen Sie auf *»Einstellungen abrufen«*. Wenn Ihr Anbieter [Auto-discovery](https://tools.ietf.org/html/rfc6186) (automatische Konfiguration) unterstützt, wird FairEmail den Hostnamen und die Portnummer ausfüllen, ansonsten folgen Sie den Anweisungen Ihres E-Mail-Anbieters für den SMTP-Hostnamen, die Portnummer und das Verschlüsselungsprotokoll (SSL/TLS oder STARTTLS).

Zur Verwendung von Alias-Adressen, bitte [diese F&A](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) beachten.

## Berechtigungen erteilen, um auf Kontaktinformationen zugreifen zu können

Wenn Sie E-Mail-Adressen nachschlagen, Kontaktfotos anzeigen lassen möchten usw., müssen Sie FairEmail die Berechtigung zum Lesen von Kontaktinformationen erteilen. Tippen Sie auf *Berechtigungen erteilen* und wählen Sie *Zulassen*.

## Akkuoptimierungen einrichten, um E-Mails fortlaufend zu erhalten

Bei aktuellen Android-Versionen schaltet Android Apps in den Standby-Modus, wenn der Bildschirm für einige Zeit ausgeschaltet ist, um den Akkuverbrauch zu reduzieren. Wenn Sie neue E-Mails ohne Verzögerung erhalten möchten, sollten Sie die Akkuoptimierung für FairEmail deaktivieren. Bitte tippen Sie auf *Verwalten* und folgen Sie den Anweisungen.

## Fragen oder Probleme

Wenn Sie eine Frage oder ein Problem haben, können Sie [hier](https://github.com/M66B/FairEmail/blob/master/FAQ.md) Hilfe erhalten.