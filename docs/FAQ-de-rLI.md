<a name="top"></a>

# FairEmail - Support

Wenn Sie eine Frage haben, überprüfen Sie bitte zuerst die folgenden häufig gestellten Fragen. [Am Ende der Seite](#user-content-get-support) erfahren Sie, wie Sie weitere Fragen stellen, Funktionen anfragen und Fehler melden können.

Wenn Sie eine Frage haben, überprüfen Sie bitte zuerst die folgenden häufig gestellten Fragen. [Am Ende der Seite](#user-content-get-support) erfahren Sie, wie Sie weitere Fragen stellen, Funktionen anfragen und Fehler melden können.

## Übersicht

* [Konten autorisieren](#user-content-authorizing-accounts)
* [Wie kann ich …?](#user-content-howto)
* [Bekannte Probleme](#user-content-known-problems)
* [Geplante Funktionen](#user-content-planned-features)
* [Häufig gewünschte Funktionen](#user-content-frequently-requested-features)
* [Häufig gestellte Fragen](#user-content-frequently-asked-questions)
* [Hilfe erhalten](#user-content-get-support)

<h2><a name="authorizing-accounts"></a>Konten autorisieren</h2>

In den meisten Fällen kann der Schnelleinrichtungs-Assistent automatisch die richtige Konfiguration ermitteln.

Sollte der Schnelleinrichtungs-Assistent fehlschlagen, müssen Sie Ihr Konto für das Versenden und Empfangen von E-Mails manuell einrichten. Hierzu benötigen Sie die Adressen der IMAP- und SMTP-Server sowie die jeweiligen Portnummern. Verwenden Sie SSL/TLS oder STARTTLS und Ihren Benutzernamen (meistens, aber nicht immer Ihre E-Mail-Adresse) mit dem dazugehörigen Passwort.

Die Suche nach *IMAP* und dem Namen des E-Mail-Anbieters reichen meist aus, um die richtige Anleitung zu finden.

In einigen Fällen müssen Sie den externen Zugriff auf Ihr Konto erlauben und/oder ein spezielles (App-)Passwort verwenden, wenn beispielsweise eine Zwei-Faktor-Authentifizierung aktiviert ist.

Zur Autorisierung:

* Gmail / G Suite: siehe [Frage 6](#user-content-faq6)
* Outlook / Live / Hotmail: siehe [Frage 14](#user-content-faq14)
* Office365: siehe [Frage 14](#user-content-faq156)
* Microsoft Exchange: siehe [Frage 8](#user-content-faq8)
* Yahoo und Sky: siehe [Frage 88](#user-content-faq88)
* Apple iCloud: siehe [Frage 148](#user-content-faq148)
* Free.fr: siehe [Frage 157](#user-content-faq157)

[Hier](#user-content-faq22) finden Sie häufige Fehlermeldungen und Lösungen.

Ähnliche Fragen:

* [Wird OAuth unterstützt?](#user-content-faq111)
* [Warum wird ActiveSync nicht unterstützt?](#user-content-faq133)

<a name="howto">

## Wie kann ich …?

* Kontonamen ändern: Einstellungen → Manuelle Einrichtung und Kontooptionen → Konten → Konto auswählen
* Aktion für Links-/Rechtswischen ändern: Einstellungen → Verhalten → Wischgesten festlegen
* Passwort ändern: Einstellungen → Manuelle Einrichtung und Kontooptionen → Konten → Konto auswählen → Passwort ändern
* Signatur festlegen: Einstellungen → Manuelle Einrichtung und Kontooptionen → Identitäten → Identität auswählen → Signatur bearbeiten.
* Kopie- und Blindkopie-Adressen (CC und BCC) hinzufügen: auf das Bild der Person am Ende des Betreffs tippen
* Zur nächsten/vorherigen Nachricht beim Archiviren/Löschen gehen: in den Einstellungen unter »Verhalten« *Unterhaltungen automatisch schließen* deaktivieren und unter *Beim schließen einer Unterhaltung* bitte *Zur nächsten/vorherigen Unterhaltung wechseln* auswählen
* Einen Ordner zum Sammeleingang hinzufügen: lange auf den Ordner in der Ordnerliste drücken und *Im Sammeleingang anzeigen* ankreuzen
* Einen Ordner zum Navigationsmenü hinzufügen: lange auf den Ordner in der Ordnerliste drücken und *Im Navigationsmenü anzeigen* ankreuzen
* Weitere Nachrichten laden: lange auf den Ordner in der Ordnerliste drücken und *Weitere Nachrichten abrufen* auswählen
* Um eine Nachricht direkt zu löschen und den Papierkorb zu umgehen, halten sie das Löschen-Symbol gedrückt
* Konto/Identität löschen: Einstellungen → Manuelle Einrichtung und Kontooptionen → Konten/Identitäten → Konto/Identität auswählen → Drei-Punkte-Menü → Löschen
* Ordner löschen: lange auf den Ordner in der Ordnerliste drücken -> Eigenschaften -> Drei-Punkte-Menü -> Löschen
* Senden rückgängig machen: Postausgang, dann Nachricht nach links oder rechts schieben
* Gesendete Nachrichten im Posteingang speichern: [Siehe diese F&A](#user-content-faq142)
* Systemordner ändern: Einstellungen → Manuelle Einrichtung und Kontooptionen → Konten → Konto auswählen → im unteren Bereich
* Einstellungen exportieren/importieren: Einstellungen, Navigationsmenü (linke Seite)

<h2><a name="known-problems"></a>Bekannte Probleme</h2>

* ~~Ein [Fehler in Android 5.1 und 6](https://issuetracker.google.com/issues/37054851) führt dazu, dass Apps manchmal ein falsches Zeitformat anzeigen. Das Ein/Ausschalten des *24-Stunden-Formats* in den Android Einstellungen könnte das Problem vorübergehend beheben. Eine vorübergehende Lösung wurde hinzugefügt.~~
* ~~Ein [Fehler in Google Drive](https://issuetracker.google.com/issues/126362828) bewirkt, dass die nach Google Drive exportierten Dateien leer sind. Google hat dies repariert.~~
* ~~Ein [Fehler in AndroidX](https://issuetracker.google.com/issues/78495471) lässt FairEmail bei langem Drücken oder Wischen gelegentlich abstürzen. Google hat dies repariert.~~
* ~~Ein [Fehler im AndroidX ROOM](https://issuetracker.google.com/issues/138441698) verursacht manchmal einen Absturz mit "*… Ausnahme beim Berechnen der Datenbank Live-Daten ... Konnte Zeile ...*" nicht lesen. Ein Workaround wurde hinzugefügt.~~
* Ein [Bug im Android](https://issuetracker.google.com/issues/119872129) verursacht manchmal einen FairEmail-Absturz mit "*... Fehlerhafte Benachrichtigung ...*" auf einigen Geräten nach dem Aktualisieren von FairEmail und dem Tippen auf eine Benachrichtigung.
* Ein [Bug in Android](https://issuetracker.google.com/issues/62427912) verursacht manchmal einen Absturz mit "*... ActivityRecord nicht gefunden für ...*" nach dem Update von FairEmail. Eine Neuinstallation ([quelle](https://stackoverflow.com/questions/46309428/android-activitythread-reportsizeconfigurations-causes-app-to-freeze-with-black)) könnte das Problem beheben.
* Ein [Fehler in Android](https://issuetracker.google.com/issues/37018931) verursacht manchmal einen Absturz mit *... Der Eingabekanal wurde auf einigen Geräten nicht initialisiert ...*.
* ~~A [bug in LineageOS](https://review.lineageos.org/c/LineageOS/android_frameworks_base/+/265273) verursacht manchmal einen Absturz mit *... java.lang.ArrayIndexOutOfBoundsException: length=...; index=... ...*.~~
* Ein Fehler in Nova Launcher unter Android 5.x lässt FairEmail mit einem *java.lang.StackOverflowError* abstürzen, wenn Nova Launcher einen Zugriff auf die Bedienungshilfen hat.
* ~~Die Ordnerauswahl zeigt manchmal aus noch unbekannten Gründen keine Ordner an. Dies scheint behoben zu sein.~~
* ~~Ein [Bug in AndroidX](https://issuetracker.google.com/issues/64729576) macht es schwer, den Schnellscroller zu fassen. Ein Workaround wurde hinzugefügt.~~
* ~~Die Verschlüsselung mit YubiKey führt zu einer Endlosschleife. Dies scheint durch einen [Fehler in OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2507) verursacht zu werden.~~
* Der Bildlauf zu einer intern verknüpften Stelle in Originalnachrichten funktioniert nicht. Dies kann nicht behoben werden, da die Original-Nachrichten-Ansicht in einer Scroll-Ansicht enthalten ist.
* Eine Vorschau eines Nachrichtentextes wird auf Samsung-Uhren nicht (immer) angezeigt, weil [setLocalOnly](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder.html#setLocalOnly(boolean)) wohl ignoriert wird. Bisher ist nur bekannt, dass Nachrichtenvorschautexte auf den Smart-Armbändern „Pebble 2”, „Fitbit Charge 3”, „Mi Band 3” und „Xiaomi Amazfit BIP” korrekt angezeigt werden. Siehe auch [diese FAQ](#user-content-faq126).
* Ein [Fehler in Android 6.0](https://issuetracker.google.com/issues/37068143) verursacht einen Absturz mit * … Ungültiger Offset: ... Der gültige Bereich ist …* wenn Text ausgewählt ist und außerhalb des ausgewählten Textes angetippt wird. Dieser Fehler wurde in Android 6.0.1 behoben.
* Interne (Anker-)Links funktionieren nicht, da die Originalnachrichten in einer eingebetteten Web-Ansicht in einer scrollenden Ansicht (der Konversationsliste) angezeigt werden. Dies ist eine Einschränkung von Android, die nicht behoben oder umgangen werden kann.
* Die Erkennung der Sprache [funktioniert nicht mehr](https://issuetracker.google.com/issues/173337263) auf Pixel-Geräten mit (Update auf?) Android 11
* Ein [Fehler in OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2688) verursacht ungültige PGP-Signaturen bei der Verwendung eines Hardware-Tokens.

<h2><a name="planned-features"></a>Geplante Funktionen</h2>

* ~~Synchronisieren bei Bedarf (manuell)~~
* ~~Halbautomatische Verschlüsselung~~
* ~~Nachricht kopieren~~
* ~~Farbige Sterne~~
* ~~Benachrichtigungseinstellungen pro Ordner~~
* ~~~Das Wählen von lokalen Bildern für Signaturen~~ (dies wird nicht hinzugefügt, weil es eine Bildverwaltung erfordert und weil Bilder in den meisten E-Mail-Clients ohnehin nicht standardmäßig angezeigt werden)
* ~~Nachrichten zu einem Regel-Treffer anzeigen~~
* ~~[ManageSieve](https://tools.ietf.org/html/rfc5804)~~ (es gibt keine gepflegten Java-Bibliotheken mit einer geeigneten Lizenz und ohne Abhängigkeiten und außerdem hat FairEmail eigene Filterregeln)
* ~~Suche nach Nachrichten mit/ohne Anhänge~~ (dies kann nicht hinzugefügt werden, da IMAP die Suche nach Anhängen nicht unterstützt)
* ~~Nach einem Ordner suchen~~ (das Filtern einer hierarchischen Ordnerliste ist problematisch)
* ~~Suchvorschläge~~
* ~~[Autocrypt Setup Message](https://autocrypt.org/autocrypt-spec-1.0.0.pdf) (section 4.4)~~ (Meiner Meinung nach ist es keine gute Idee, einen E-Mail-Client für einen Ausnahmefall sensible Verschlüsselungscodes verarbeiten zu lassen, während OpenKeychain auch Schlüssel exportieren kann)
* ~~Generische vereinheitlichte Ordner~~
* ~~Neue Benachrichtigungszeitpläne für Nachrichten pro Konto~~ (implementiert durch Hinzufügen einer Zeitbedingung zu den Regeln, so dass Nachrichten während ausgewählter Zeiträume zurückgestellt werden können)
* ~~Kopieren von Konten und Identitäten~~
* ~~Pinch zoom~~ (nicht zuverlässig möglich in einer Scroll-Liste; stattdessen kann die gesamte Nachrichtenansicht vergrößert werden)
* ~~Mehr kompakte Ordneransicht~~
* ~~Listen und Tabellen erstellen~~ (dies erfordert einen Rich-Text-Editor, siehe [diese FAQ](#user-content-faq99))
* ~~Pinch Zoom für die Textgröße~~
* ~~GIF-Anzeige~~
* ~~Themes~~ (ein graues Design mit hellem und dunklem Hintergrund wurden hinzugefügt, weil dies das ist, was die meisten Leute anscheinend wollen)
* ~~Eine Regel-Bedingung für jeden Tag~~ (ein Tag passt nicht wirklich in die von/zu Datum/Uhrzeit-Bedingung)
* ~~als Anhang senden~~
* ~~Widget für das ausgewählte Konto~~
* ~~Erinnerung, um Dateien anzuhängen~~
* ~~Domain-Auswahl, für die Bilder gezeigt werden dürfen~~ (dies wird zu kompliziert in der Verwendung)
* ~~Anzeige markierter Nachrichten~~ (dafür gibt es bereits eine spezielle Suche)
* ~~Benachrichtigungsaktion verschieben~~
* ~~S/MIME-Unterstützung~~
* ~~Einstellungen durchsuchen~~

Alles auf dieser Liste ist in zufälliger Reihenfolge und *könnte* in naher Zukunft hinzugefügt werden.

<h2><a name="frequently-requested-features"></a>Häufig angefragte Funktionen</h2>

Das Design basiert auf vielen Diskussionen und wenn du möchtest, kannst du auch [in diesem Forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168) darüber diskutieren. Ziel des Designs ist es, minimalistisch (keine unnötigen Menüs, Knöpfe usw.) und nicht ablenkend (keine ausgefallenen Farben, Animationen usw.), zu sein. Alle angezeigten Dinge sollten auf die eine oder andere Weise nützlich sein und sorgfältig positioniert werden, um sie einfach zu verwenden zu können. Schriften, Größen, Farben usw. sollten nach Möglichkeit im Materialdesign gestaltet sein.

<h2><a name="frequently-asked-questions"></a>Häufig gestellte Fragen</h2>

* [(1) Welche Berechtigungen werden benötigt und warum?](#user-content-faq1)
* [(2) Warum wird eine permanente Benachrichtigung angezeigt?](#user-content-faq2)
* [(3) Was sind Vorgänge und warum sind sie anhängig?](#user-content-faq3)
* [(4) Wie kann ich ein ungültiges Sicherheitszertifikat / leeres Passwort / Klartextverbindung verwenden?](#user-content-faq4)
* [(5) Wie kann ich die Nachrichtenansicht anpassen?](#user-content-faq5)
* [(6) Wie kann ich mich bei Gmail / G Suite anmelden?](#user-content-faq6)
* [(7) Warum werden gesendete Nachrichten nicht (direkt) im Gesendet-Ordner angezeigt?](#user-content-faq7)
* [(8) Kann ich ein Microsoft-Exchange-Konto verwenden?](#user-content-faq8)
* [(9) Was sind Identitäten / Wie füge ich einen Alias hinzu?](#user-content-faq9)
* [~~(11) Warum wird POP nicht unterstützt?~~](#user-content-faq11)
* [~~(10) Was bedeutet »UIDPLUS nicht unterstützt«?~~](#user-content-faq10)
* [(12) Wie funktioniert die Ver- und Entschlüsselung?](#user-content-faq12)
* [(13) Wie funktioniert die Suche auf dem Gerät bzw. auf den Servern?](#user-content-faq13)
* [(14) Wie kann ich ein Outlook-/Live-/Hotmail-Konto einrichten?](#user-content-faq14)
* [(15) Warum wird der Nachrichtentext weiterhin geladen?](#user-content-faq15)
* [(16) Warum werden Nachrichten nicht synchronisiert?](#user-content-faq16)
* [~~(17) Warum funktioniert die manuelle Synchronisation nicht?~~](#user-content-faq17)
* [(18) Warum wird die Nachrichtenvorschau nicht immer angezeigt?](#user-content-faq18)
* [(19) Warum sind die Pro-Funktionen so teuer?](#user-content-faq19)
* [(20) Kann ich eine Rückerstattung erhalten?](#user-content-faq20)
* [(21) Wie aktiviere ich das Benachrichtigungslicht?](#user-content-faq21)
* [(22) Was bedeutet ein Konto-/Ordnerfehler …?](#user-content-faq22)
* [(23) Warum bekomme ich einen Alarm? ?](#user-content-faq23)
* [(24) Was bedeutet das Anzeigen / Suchen von Nachrichten auf dem Server?](#user-content-faq24)
* [(25) Warum kann ich ein Bild, einen Anhang oder eine Datei nicht auswählen, öffnen oder speichern?](#user-content-faq25)
* [(26) Kann ich bei der Übersetzung von FairEmail in meine Muttersprache helfen?](#user-content-faq26)
* [(27) Wie kann ich zwischen eingebetteten und externen Grafiken unterscheiden?](#user-content-faq27)
* [(28) Wie kann ich Benachrichtigungen in der Statusleiste verwalten?](#user-content-faq28)
* [(29) Wie kann ich Benachrichtigungen über neue Nachrichten für andere Ordner erhalten?](#user-content-faq29)
* [(30) Wie kann ich die bereitgestellten Schnelleinrichtungen verwenden?](#user-content-faq30)
* [(31) Wie kann ich die bereitgestellten Tastenkombinationen verwenden?](#user-content-faq31)
* [(32) Wie kann ich überprüfen, ob das Lesen von E-Mails wirklich sicher ist?](#user-content-faq32)
* [(33) Warum funktionieren bearbeitete Absenderadressen nicht?](#user-content-faq33)
* [(34) Wie werden Identitäten abgeglichen?](#user-content-faq34)
* [(35) Warum sollte ich vorsichtig beim Öffnen von Bildern, Anhängen, der originialen Nachricht oder Links sein?](#user-content-faq35)
* [(36) Wie werden Einstellungsdateien verschlüsselt?](#user-content-faq36)
* [(37) Wie werden Passwörter gespeichert?](#user-content-faq37)
* [(39) Wie kann ich den Akkuverbrauch von FairEmail verringern?](#user-content-faq39)
* [(40) Wie kann ich die Datennutzung von FairEmail verringern?](#user-content-faq40)
* [(41) Wie kann ich den Fehler »Handshake failed« beheben?](#user-content-faq41)
* [(42) Können Sie einen neuen Anbieter zur Liste der Anbieter hinzufügen?](#user-content-faq42)
* [(43) Können Sie das Original anzeigen?](#user-content-faq43)
* [(44) Können Sie Kontaktfotos / Ident-Icons im Gesendeten Ordner anzeigen?](#user-content-faq44)
* [(45) Wie kann ich folgendes beheben? »Dieser Schlüssel ist nicht verfügbar. Um es zu verwenden, müssen Sie es selbst importieren!« ?](#user-content-faq45)
* [(46) Warum wird die Nachrichtenliste immer wieder aktualisiert?](#user-content-faq46)
* [(47) Wie löse ich den Fehler »Kein primäres Konto oder kein Ordner für Entwürfe«?](#user-content-faq47)
* [~~(48) Wie löse ich den Fehler »Kein primäres Konto oder kein Archivordner«?~~](#user-content-faq48)
* [(49) Wie behebe ich »Eine veraltete App hat einen Dateipfad anstelle eines Datei-Streams gesendet«?](#user-content-faq49)
* [(50) Kann man eine Option hinzufügen, um alle Nachrichten zu synchronisieren?](#user-content-faq50)
* [(51) Wie werden die Ordner sortiert?](#user-content-faq51)
* [(52) Wieso dauert es so lange Zeit, um sich wieder mit einem Konto zu verbinden?](#user-content-faq52)
* [(53) Können Sie die Aktionsleiste nach oben/unten verlagern?](#user-content-faq53)
* [~~(54) Wie benutze ich ein Namensraumpräfix?~~](#user-content-faq54)
* [(55) Wie kann ich alle Nachrichten als gelesen markieren, verschieben oder löschen?](#user-content-faq55)
* [(56) Können Sie Unterstützung für JMAP hinzufügen?](#user-content-faq56)
* [(57) Kann ich HTML in Signaturen verwenden?](#user-content-faq57)
* [(58) Was bedeutet ein geöffnetes/geschlossenes E-Mail-Symbol?](#user-content-faq58)
* [(59) Können Original-Nachrichten im Browser geöffnet werden?](#user-content-faq59)
* [(60) Wussten Sie …?](#user-content-faq60)
* [(61) Warum werden einige Nachrichten verdunkelt angezeigt?](#user-content-faq61)
* [(62) Welche Authentifizierungsmethoden werden unterstützt?](#user-content-faq62)
* [(63) Wie werden Bilder für die Anzeige auf den Bildschirmen skaliert?](#user-content-faq63)
* [~~(64) Kann man benutzerdefinierte Aktionen zum Links-/Rechtswischen hinzufügen?~~](#user-content-faq64)
* [(65) Warum werden einige Anhänge abgedunkelt angezeigt?](#user-content-faq65)
* [(66) Gibt es FairEmail in der Google-Play-Familienmediathek?](#user-content-faq66)
* [(67) Wie kann ich Konversationen zurückstellen?](#user-content-faq67)
* [~~(68) Warum kann Adobe Acrobat Reader keine PDF-Anhänge öffnen / Microsoft-Apps keine angehängten Dokumente öffnen?~~](#user-content-faq68)
* [(69) Gibt es eine Option »Bei neuen Nachrichten automatisch nach oben rollen«?](#user-content-faq69)
* [(70) Wann werden Nachrichten automatisch erweitert?](#user-content-faq70)
* [(71) Wie verwende ich Filterregeln?](#user-content-faq71)
* [(72) Was sind Hauptkonten/-identitäten?](#user-content-faq72)
* [(73) Ist das Verschieben von Nachrichten zwischen verschiedenen Konten sicher/effizient?](#user-content-faq73)
* [(74) Wieso sehe ich Nachrichten doppelt?](#user-content-faq74)
* [(75) Können Sie eine iOS-, Windows-, Linux- usw. Version erstellen?](#user-content-faq75)
* [(76) Was macht »Lokale Nachrichten löschen«?](#user-content-faq76)
* [(77) Warum werden Mitteilungen manchmal mit einer kleinen Verzögerung angezeigt?](#user-content-faq77)
* [(78) Wie verwende ich Zeitpläne?](#user-content-faq78)
* [(79) Wie verwende ich Synchronisieren auf Anfrage (manuell)?](#user-content-faq79)
* [~~(80) Wie kann ich den Fehler »Unable to load BODYSTRUCTURE« beheben?~~](#user-content-faq80)
* [~~(81) Kann der Hintergrund der ursprünglichen Nachricht im dunklen Modus dunkel gemacht werden?~~](#user-content-faq81)
* [(82) Was ist ein Nachverfolgungsbild?](#user-content-faq82)
* [(84) Wozu gibt es lokale Kontakte?](#user-content-faq84)
* [(85) Warum ist eine Identität nicht verfügbar?](#user-content-faq85)
* [~~(86) Was sind »zusätzliche Privatsphärenfunktionen«?~~](#user-content-faq86)
* [(87) Was bedeutet »ungültige Anmeldedaten«?](#user-content-faq87)
* [(88) Wie kann ich ein Yahoo-, AOL- oder Sky-Konto verwenden?](#user-content-faq88)
* [(89) Wie kann ich Nur-Text-Nachrichten senden?](#user-content-faq89)
* [(90) Warum sind einige Texte als Link formatiert, ohne ein gültiger Link zu sein?](#user-content-faq90)
* [~~(91) Können Sie eine periodische Synchronisierung hinzufügen, um den Akku zu schonen?~~](#user-content-faq91)
* [(92) Können Sie Spamfilter, Überprüfung der DKIM-Signatur und SPF-Legitimierung hinzufügen?](#user-content-faq92)
* [(93) Ist eine Installation oder das Verlagern des Datenspeichers auf einen externen Datenträger (sdcard) möglich?](#user-content-faq93)
* [(94) Was bedeutet der rot-orangefarbene Streifen am Ende des Nachrichtenkopfes?](#user-content-faq94)
* [(95) Warum werden nicht alle Apps angezeigt, wenn ein Anhang oder ein Bild ausgewählt wird?](#user-content-faq95)
* [(96) Wo finde ich die IMAP- und SMTP-Einstellungen?](#user-content-faq96)
* [(97) Was ist »Bereinigen« ?](#user-content-faq97)
* [(98) Warum kann ich immer noch Kontakte auswählen, nachdem ich Kontaktberechtigungen entzogen habe?](#user-content-faq98)
* [(99) Gibt es einen Rich-Text- oder Markdown-Editor?](#user-content-faq99)
* [(100) Wie kann ich Google Mail-Kategorien synchronisieren?](#user-content-faq100)
* [(101) Was bedeutet der blaue/orangefarbene Punkt am unteren Ende der Konversationen?](#user-content-faq101)
* [(102) Wie kann ich die automatische Drehung von Bildern aktivieren?](#user-content-faq102)
* [(103) Wie kann ich Audio aufnehmen?](#user-content-faq158)
* [(104) Was muss ich über die Fehlermeldung wissen?](#user-content-faq104)
* [(105) Wie funktioniert die »Roaming wie zu Hause«-Option?](#user-content-faq105)
* [(106) Welche Launcher können die Anzahl der ungelesenen Nachrichten als Plakette anzeigen?](#user-content-faq106)
* [(107) Wie verwende ich farbige Sterne?](#user-content-faq107)
* [~~(108) Kann man Nachrichten aus beliebigen Ordnern dauerhaft löschen?~~](#user-content-faq108)
* [~~(109) Warum ist »Konto auswählen« nur in offiziellen Versionen verfügbar?~~](#user-content-faq109)
* [(110) Warum sind (einige) Nachrichten leer und/oder Anhänge beschädigt?](#user-content-faq110)
* [(111) Wird OAuth unterstützt?](#user-content-faq111)
* [(112) Welchen E-Mail-Provider empfehlen Sie?](#user-content-faq112)
* [(113) Wie funktioniert die biometrische Authentifizierung?](#user-content-faq113)
* [(114) Kann ein Import der Einstellungen anderer E-Mail-Apps hinzugefügt werden?](#user-content-faq114)
* [(115) Können E-Mail-Adressen-Chips hinzufügt werden?](#user-content-faq115)
* [~~(116) Wie kann ich Bilder in Nachrichten von vertrauenswürdigen Absendern standardmäßig anzeigen? ~~](#user-content-faq116)
* [Können Sie mir helfen, meinen Kauf wiederherzustellen?](#user-content-faq117)
* [(118) Was bedeutet »Nachverfolgungsparameter entfernen« genau?](#user-content-faq118)
* [~~(119) Können Sie Farben dem Sammeleingangs-Widget hinzufügen?~~](#user-content-faq119)
* [(120) Warum werden neue Nachrichten beim Öffnen der App nicht entfernt?](#user-content-faq120)
* [(121) Wie werden Nachrichten zu einer Konversation gruppiert?](#user-content-faq121)
* [~~(122) Warum wird Empfängername/E-Mail-Adresse mit einer Warnfarbe angezeigt?~~](#user-content-faq122)
* [(123) Was geschieht, wenn FairEmail keine Verbindung zu einem E-Mail-Server herstellen kann?](#user-content-faq123)
* [(124) Warum erhalte ich den Hinweis »Nachricht zu groß oder zu komplex, um sie anzuzeigen«?](#user-content-faq124)
* [(125) Was sind die aktuellen experimentellen Funktionen?](#user-content-faq125)
* [(126) Können Nachrichtenvorschauen an mein Wearable gesendet werden?](#user-content-faq126)
* [(127) Wie kann ich den Fehler »Syntaktisch ungültige HELO-Argumente« beheben?](#user-content-faq127)
* [(128) Wie kann ich die gestellten Fragen zurücksetzen, zum Beispiel um Bilder zu zeigen?](#user-content-faq128)
* [(129) Wird ProtonMail, Tutanota unterstützt?](#user-content-faq129)
* [(130) Was bedeutet die Meldung »Fehler …«?](#user-content-faq130)
* [(131) Kann man die Richtung für das Wischen für vorherige/nächste Nachricht ändern?](#user-content-faq131)
* [(132) Warum sind Benachrichtigungen für neue Nachrichten stumm?](#user-content-faq132)
* [(133) Warum wird ActiveSync nicht unterstützt?](#user-content-faq133)
* [(134) Wie kann ich Nachrichten nur in der App löschen?](#user-content-faq134)
* [(135) Warum werden gelöschte Nachrichten oder Entwürfe in Konversationen angezeigt?](#user-content-faq135)
* [(136) Wie kann ich ein Konto/Identität/Ordner löschen?](#user-content-faq136)
* [(137) Wie kann ich »Nicht erneut fragen« zurücksetzen?](#user-content-faq137)
* [(138) Können Kalender/Kontakte/Aufgaben/Notizverwaltung hinzugefügt werden?](#user-content-faq138)
* [(139) Wie behebe ich »Benutzer ist angemeldet, aber nicht verbunden«?](#user-content-faq139)
* [(140) Warum enthält der Nachrichtentext seltsame Zeichen?](#user-content-faq140)
* [(141) Wie kann ich »Ein Entwürfe-Ordner ist erforderlich, um Nachrichten zu senden« beheben?](#user-content-faq141)
* [(142) Wie kann ich gesendete Nachrichten im Posteingang speichern?](#user-content-faq142)
* [~~(143) Gibt es einen Papierkorb für POP3-Konten? ~~](#user-content-faq143)
* [(144) Wie kann ich Sprachnotizen aufnehmen?](#user-content-faq144)
* [(145) Wie kann ich einen Benachrichtigungston für ein Konto, einen Ordner oder einen Absender festlegen?](#user-content-faq145)
* [(146) Wie kann ich falsche Zeiten von Nachrichten beheben?](#user-content-faq146)
* [(147) Was sollte ich über Drittanbieter-Versionen wissen?](#user-content-faq147)
* [(148) Wie kann ich ein Apple-iCloud-Konto verwenden?](#user-content-faq148)
* [(149) Wie funktioniert das Widget für ungelesene Nachrichten?](#user-content-faq149)
* [(150) Gibt es eine Möglichkeit zum Ablehnen von Kalendereinladungen?](#user-content-faq150)
* [(151) Gibt es eine Datensicherung/Wiederherstellung von Nachrichten?](#user-content-faq151)
* [(152) Wie kann ich eine Kontaktgruppe einfügen?](#user-content-faq152)
* [(153) Warum funktioniert das dauerhafte Löschen von Gmail-Nachrichten nicht?](#user-content-faq153)
* [~~(154) Kann man Favicons als Kontaktfotos verwenden?~~](#user-content-faq154)
* [(155) Was ist eine winmail.dat-Datei?](#user-content-faq155)
* [(156) Wie kann ich ein Office365-Konto einrichten?](#user-content-faq156)
* [(157) Wie kann ich ein Free.fr-Konto einrichten?](#user-content-faq157)
* [(158) Welche/r Kamera/Audiorekorder ist empfehlenswert?](#user-content-faq158)
* [(159) Was sind Disconnects Tracker-Schutzlisten?](#user-content-faq159)
* [(160) Kannst du eine dauerhafte Löschung von Nachrichten ohne Bestätigung hinzufügen?](#user-content-faq160)
* [(161) Kannst du eine Einstellung zum Ändern der primären und akzentuierenden Farbe hinzufügen?](#user-content-faq161)
* [(162) Wird IMAP NOTIFY unterstützt?](#user-content-faq162)
* [(163) Was ist Nachrichtenklassifizierung?](#user-content-faq163)
* [(164) Können Sie anpassbare Designs hinzufügen?](#user-content-faq164)
* [(165) Wird Android Auto unterstützt?](#user-content-faq165)
* [(166) Kann ich eine Nachricht über mehrere Geräte hinweg zurückstellen?](#user-content-faq166)

[Ich habe eine andere Frage.](#user-content-support)

<a name="faq1"></a>
**(1) Welche Berechtigungen werden benötigt und warum?**

Die folgenden Android-Berechtigungen sind notwendig:

* *Auf alle Netzwerke zugreifen * (INTERNET): E-Mails senden und empfangen
* *Netzwerkverbindungen abrufen* (ACCESS_NETWORK_STATE): Um Änderungen bei der Internetverbindung zu überwachen
* *Beim Start ausführen* (RECEIVE_BOOT_COMPLETED): Wird für den automatischen Start der App nach dem Einschalten des Geräts benötigt
* *Vordergrunddienste verwenden* (FOREGROUND_SERVICE): um einen Vordergrund-Dienst auf Android 9 Pie und später auszuführen, siehe auch die nächste Frage
* *Ruhezustand deaktivieren* (WAKE_LOCK): hält das Gerät wach, während die Nachrichten synchronisiert werden
* *Google Play-Rechnungsdienst* (BILLING): Erlaubt In-App-Käufe
* *Plane einen exakten Alarm* (SCHEDULE_EXACT_ALARM): Um exakte Alarmplanung zu verwenden (Android 12 und höher)
* Optional: *Einlesen Ihrer Kontakte* (READ_CONTACTS): zum automatischen Vervollständigen von Adressen, zum Anzeigen von Kontaktfotos und [zum Auswählen von Kontakten](https://developer.android.com/guide/components/intents-common#PickContactDat)
* Optional: *SD-Karteninhalte lesen* (READ_EXTERNAL_STORAGE): um Dateien von anderen, veralteten Apps anzunehmen, siehe auch [diese häufig gestellten Fragen (FAQ)](#user-content-faq49)
* Optional: *Fingerabdruckhardware nutzen* (USE_FINGERPRINT) und *biometrische Hardware nutzen* (USE_BIOMETRIC): um biometrische Authentifizierung zu verwenden
* Optional: *Konten auf dem Gerät suchen* (GET_ACCOUNTS): Um ein Konto auszuwählen. (wenn die Gmail Schnelleinrichtung verwendet wird)
* Android 5.1 Lollipop und früher: *Benutzen Sie Konten auf dem Gerät* (USE_CREDENTIALS): Wählen Sie ein Konto bei der Verwendung der Google-Mail-Schnelleinstellung (nicht bei späteren Android-Versionen erforderlich)
* Android 5.1 Lollipop und früher: *Profil lesen* (READ_PROFILE): um Ihren Namen bei der Verwendung der Gmail-Schnelleinrichtung zu lesen (nicht bei späteren Android-Versionen erforderlich)

[Optionale Berechtigungen](https://developer.android.com/training/permissions/requesting) werden nur auf Android 6 Marshmallow und später unterstützt. Bei früheren Android-Versionen werden Sie aufgefordert, die optionalen Berechtigungen bei der Installation von FairEmail zu erteilen.

Die folgenden Berechtigungen werden benötigt, um die Anzahl ungelesener Nachrichten auf dem App-Icon anzuzeigen (siehe auch [diese FAQ](#user-content-faq106)):

* *com.sec.android.provider.badge.permission.READ*
* *com.sec.android.provider.badge.permission.WRITE*
* *com.htc.launcher.permission.READ_SETTINGS*
* *com.htc.launcher.permission.UPDATE_SHORTCUT*
* *com.sonyericsson.home.permission.BROADCAST_BADGE*
* *com.sonymobile.home.permission.PROVIDER_INSERT_BADGE*
* *com.anddoes.launcher.permission.UPDATE_COUNT*
* *com.majeur.launcher.permission.UPDATE_BADGE*
* *com.huawei.android.launcher.permission.CHANGE_BADGE*
* *com.huawei.android.launcher.permission.READ_SETTINGS*
* *com.huawei.android.launcher.permission.WRITE_SETTINGS*
* *android.permission.READ_APP_BADGE*
* *com.oppo.launcher.permission.READ_SETTINGS*
* *com.oppo.launcher.permission.WRITE_SETTINGS*
* *me.everything.badger.permission.BADGE_COUNT_READ*
* *me.everything.badger.permission.BADGE_COUNT_WRITE*

FairEmail wird eine Liste der E-Mail-Adressen aufbewahren, von denen Sie Nachrichten erhalten und an die Sie Nachrichten gesendet haben und wird diese Liste für Kontaktvorschläge verwenden, wenn FairEmail keine Berechtigung für das Lesen der Kontakte erteilt wurde. Das bedeutet, dass Sie FairEmail ohne den Android-Kontaktanbieter (das Adressbuch) verwenden können. Beachten Sie, dass Sie immer noch Kontakte auswählen können, ohne FairEmail die Berechtigung auf Kontakte zu erteilen, nur vorgeschlagene Kontakte werden ohne Kontaktberechtigungen nicht funktionieren.

<br />

<a name="faq2"></a>
**(2) Warum wird eine permanente Benachrichtigung angezeigt?**

Eine permanente Statusleiste mit niedriger Priorität mit der Anzahl der zu überwachenden Konten und der Anzahl der ausstehenden Operationen (siehe die nächste Frage) wird angezeigt, um zu verhindern, dass Android den Dienst beendet, der sich um den kontinuierlichen Empfang von E-Mails kümmert. Das war [immer notwendig](https://developer.android.com/reference/android/app/Service.html#startForeground(int,%20android.app.Notification)), aber mit der Einführung des [Doze-Modus](https://developer.android.com/training/monitoring-device-state/doze-standby) in Android 6 Marshmallow ist das nun notwendiger denn je. Der Doze-Modus beendet alle Apps, wenn der Bildschirm für einige Zeit ausgeschaltet ist, es sei denn, die App hat einen Vordergrund-Dienst gestartet, was die Anzeige einer Statusleisten-Benachrichtigung erfordert.

Die meisten, wenn nicht gar alle anderen E-Mail-Apps zeigen keine Benachrichtigungen an, was zu den "Nebeneffekten" führt, dass es keinen oder nur einen verspäteten Hinweis auf neu eingegangene Nachrichten gibt oder ausgehende Nachrichten nicht oder nur verzögert versandt werden.

Android zeigt Symbole von Benachrichtigungen mit hoher Priorität in der Statusleiste zuerst an und blendet das Symbol der FairEmail-Benachrichtigung aus, wenn kein Platz mehr für die Anzeige von Symbolen vorhanden ist. In der Praxis bedeutet dies, dass die Statusleistenbenachrichtigung keinen Platz in der Statusleiste einnimmt, es sei denn, es ist Platz vorhanden.

Die Statusleisten-Benachrichtigung kann über die Benachrichtigungseinstellungen von FairEmail deaktiviert werden:

* Android 8 Oreo und später: Tippen Sie auf *Kanal empfangen* und deaktivieren Sie den Kanal über die Android-Einstellungen (dies wird keine neuen Nachrichten deaktivieren)
* Android 7 Nougat und vorher: aktiviert *Hintergrunddienst verwenden, um Nachrichten zu synchronisieren*, aber lesen Sie die Bemerkung unter der Einstellung

Sie können in den Empfangseinstellungen auf periodische Synchronisierung von Nachrichten umschalten, um die Benachrichtigung zu entfernen, aber beachten Sie, dass dies möglicherweise mehr Batteriestrom verbraucht. Siehe [hier](#user-content-faq39) für weitere Details zum Batterieverbrauch.

Android 8 Oreo zeigt möglicherweise auch eine Benachrichtigung in der Statusleiste mit dem Text *Apps werden im Hintergrund ausgeführt*. Bitte lesen Sie [hier](https://www.reddit.com/r/Android/comments/7vw7l4/psa_turn_off_background_apps_notification/) nach, wie Sie diese Benachrichtigung deaktivieren können.

Einige Leute haben vorgeschlagen, [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/) (FCM) anstelle eines Android-Dienstes mit einer Statusleistenbenachrichtigung zu verwenden, Dies würde jedoch E-Mail-Provider erfordern, die FCM-Nachrichten senden, oder einen zentralen Server, auf dem alle Nachrichten gesammelt werden, die FCM-Nachrichten senden. Ersteres wird nicht passieren und letzteres hätte erhebliche Auswirkungen auf die Privatsphäre.

Wenn Sie durch Klicken auf die Benachrichtigung hierhergekommen sind, sollten Sie wissen, dass der nächste Klick den vereinheitlichten Posteingang öffnet.

<br />

<a name="faq3"></a>
**(3) Was sind Vorgänge und warum sind sie anstehend?**

Die Benachrichtigung in der Statusleiste mit niedriger Priorität zeigt die Anzahl der anstehenden Vorgänge an, die sein können:

* *Hinzufügen*: Nachricht zum Remote-Ordner hinzufügen
* *Verschieben*: Nachricht in einen anderen Remote-Ordner verschieben
* *kopieren*: Nachricht in einen anderen Remote-Ordner kopieren
* *Abruf*: Abruf der geänderten (gedrückten) Nachricht
* *Löschen*: Lösche Nachricht aus dem Remote-Ordner
* *Gesehen*: Markiere Nachricht als gelesen/ungelesen im Remote-Ordner
* *Beantwortet*: Markiere die Nachricht als beantwortet im Remote-Ordner
* *Hinzufügen*: Nachricht zum entfernten Ordner hinzufügen
* *Stichwort*: IMAP-Markierungen im entfernten Ordner hinzufügen/entfernen
* *Label*: Gmail-Label im entfernten Ordner festlegen/zurücksetzen
* *Kopfzeilen*: Nachrichtenüberschriften herunterladen
* *Roh*: Rohnachricht herunterladen
* *body*: Nachrichtentext herunterladen
* *Anhang*: Anhang herunterladen
* *Synchronisation*: lokale und entfernte Nachrichten synchronisieren
* *Abonnieren*: entfernten Ordner abonnieren
* *Bereinigen*: Lösche alle Nachrichten aus dem entfernten Ordner
* *Senden*: Nachricht senden
* *Existiert*: Prüfen, ob Nachricht existiert
* *Regel*: Regel im Text ausführen
* *expunge*: Nachrichten dauerhaft löschen

Vorgänge werden nur verarbeitet, wenn eine Verbindung zum E-Mail-Server besteht oder wenn manuell synchronisiert wird. Siehe auch [diese FAQ](#user-content-faq16).

<br />

<a name="faq4"></a>
**(4) Wie kann ich ein ungültiges Sicherheitszertifikat / leeres Passwort / Klartextverbindung verwenden?**

*... Nicht vertrauenswürdig ... nicht im Zertifikat ...*
<br />
*... Ungültiges Sicherheitszertifikat (Identität des Servers kann nicht verifiziert werden) ...*

Das kann durch die Verwendung eines falschen Hostnamens verursacht werden. Überprüfen Sie daher zunächst den Hostnamen in den erweiterten Identitäts-/Kontoeinstellungen (Manuelle Einrichtung und Kontooptionen). Bitte informieren Sie sich in der Dokumentation des E-Mail-Providers über den richtigen Hostnamen.

Sie sollten versuchen, dies zu beheben, indem Sie sich an Ihren Provider wenden oder ein gültiges Sicherheitszertifikat besorgen denn ungültige Sicherheitszertifikate sind unsicher und ermöglichen [Man-in-the-Middle-Angriffe](https://en.wikipedia.org/wiki/Man-in-the-middle_attack). Wenn Geld ein Hindernis ist, können Sie kostenlose Sicherheitszertifikate von [Let's Encrypt](https://letsencrypt.org) erhalten.

Die schnelle, aber unsichere Lösung (nicht empfohlen) ist, *Sichere Verbindungen* in den erweiterten Identitätseinstellungen zu aktivieren (Navigationsmenü  → *Einstellungen* → *Manuelle Einrichtung und Kontooptionen* → *Identitäten* → Identität auswählen → *Erweitert*).

Alternativ können Sie den Fingerabdruck von ungültigen Serverzertifikaten auch so akzeptieren:

1. Stellen Sie sicher, dass Sie eine vertrauenswürdige Internetverbindung verwenden (keine öffentlichen WLAN-Netzwerke usw.)
1. Gehen Sie zu den Einstellungen über das Navigationsmenü (wischen Sie von der linken Seite nach innen)
1. Manuelle Einrichtung und Kontooptionen → Konten/Identitäten → fehlerhaftes Konto oder Identität auswählen
1. Prüfen/Speichern Sie Konto und Identität
1. Markieren Sie das Kontrollkästchen unterhalb der Fehlermeldung und speichern Sie erneut

Dadurch wird das Server-Zertifikat "gepinnt", um Man-in-the-Middle-Angriffe zu verhindern.

Beachten Sie, dass ältere Android-Versionen neuere Zertifizierungsstellen wie Let's Encrypt möglicherweise nicht erkennen, wodurch Verbindungen als unsicher eingestuft werden, siehe auch [hier](https://developer.android.com/training/articles/security-ssl).

<br />

*Vertrauensanker für Zertifizierungspfad nicht gefunden*

*... java.security.cert.CertPathValidatorException: Vertrauensanker für Zertifizierungspfad nicht gefunden ...* bedeutet, dass der Standard-Android-Trust-Manager nicht in der Lage war, die Server-Zertifikatskette zu überprüfen.

Das kann daran liegen, dass das Stammzertifikat nicht auf Ihrem Gerät installiert ist oder weil Zwischenzertifikate fehlen, z. B. weil der E-Mail-Server sie nicht gesendet hat.

Das erste Problem können Sie beheben, indem Sie das Stammzertifikat von der Website des Zertifikatsanbieters herunterladen und installieren.

Das zweite Problem sollte durch Änderung der Serverkonfiguration oder durch Importieren der Zwischenzertifikate auf Ihrem Gerät behoben werden.

Sie können das Zertifikat auch anpinnen, siehe oben.

<br />

*Leeres Passwort*

Ihr Benutzername ist wahrscheinlich leicht zu erraten, daher ist dieser äußerst unsicher, es sei denn, der SMTP-Server ist nur über ein eingeschränktes lokales Netzwerk oder ein VPN verfügbar.

*Klartextverbindung*

Ihr Benutzername und Passwort sowie alle Nachrichten werden unverschlüsselt gesendet und empfangen, was **sehr unsicher** ist denn ein [Man-in-the-Middle-Angriff](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) ist bei einer unverschlüsselten Verbindung sehr einfach.

Wenn Sie dennoch ein ungültiges Sicherheitszertifikat, ein leeres Passwort oder eine Klartextverbindung verwenden möchten müssen Sie unsichere Verbindungen in den Konto- und/oder Identitätseinstellungen aktivieren. Für reine Textverbindungen sollte STARTTLS gewählt werden. Wenn Sie unsichere Verbindungen aktivieren, sollten Sie sich nur über private, vertrauenswürdige Netzwerke verbinden und niemals über öffentliche Netzwerke, wie sie in Hotels, Flughäfen usw. angeboten werden.

<br />

<a name="faq5"></a>
**(5) Wie kann ich die Nachrichtenansicht anpassen?**

Im Drei-Punkte-Menü können Sie die folgenden Optionen aktivieren, deaktivieren oder auswählen:

* *Textgröße*: für drei verschiedene Schriftgrößen
* *kompakte Ansicht*: für eine mehr verdichtete Nachrichtendarstellung und eine kleinere Text-Schriftart

Im Anzeigebereich der Einstellungen können Sie z. B. aktivieren oder deaktivieren:

* *Einheitlicher Posteingang*: Schalten Sie diese Option aus, um stattdessen die Ordner separat aufzulisten, die für den einheitlichen Posteingang ausgewählt wurden
* *Tabellenstil*: zum Anzeigen einer linearen Liste anstelle von Karten
* *Nach Datum gruppieren*: Datumskopf über Nachrichten mit gleichem Datum anzeigen
* *Konversationsüberlagerung*: um die Konversationsüberlagerung zu deaktivieren und stattdessen einzelne Nachrichten anzuzeigen
* *Konversationsaktionsleiste*: zum Deaktivieren der unteren Navigationsleiste
* *Hervorhebungsfarbe*: zum Auswählen einer Farbe für den Absender von ungelesenen Nachrichten
* *Kontaktfotos anzeigen*: zum Ausblenden von Kontaktfotos
* *Namen und E-Mail-Adressen anzeigen*: um Namen anzuzeigen oder um Namen und E-Mail-Adressen anzuzeigen
* *Betreff kursiv anzeigen*: um den Betreff der Nachricht als normalen Text anzuzeigen
* *Sterne anzeigen*: zum Ausblenden von Sternen (Favoriten)
* *Nachrichtenvorschau anzeigen*: um 1-4 Zeilen des Nachrichtentextes anzuzeigen
* *Adressdetails standardmäßig anzeigen*: um den Adressbereich standardmäßig zu erweitern
* *Automatisch Originalnachricht für bekannte Kontakte anzeigen*: um automatisch Originalnachrichten für Kontakte auf Ihrem Gerät anzuzeigen, lesen Sie bitte [diese FAQ](#user-content-faq35)
* *Automatisch Bilder für bekannte Kontakte anzeigen*: um automatisch Bilder für Kontakte auf Ihrem Gerät anzuzeigen, lesen Sie bitte [diese FAQ](#user-content-faq35)

Beachten Sie, dass Nachrichten nur dann in der Vorschau angezeigt werden können, wenn der Nachrichtentext heruntergeladen wurde. Größere Nachrichtentexte werden in gebührenpflichtigen (in der Regel mobilen) Netzen standardmäßig nicht heruntergeladen. Sie können dies in den Verbindungseinstellungen ändern.

Manche Leute fragen:

* wie man den Betreff fett anzeigen lassen kann, obwohl eine fette Schrift bereits verwendet wird, um ungelesene Nachrichten hervorzuheben
* um den Stern nach links zu bewegen, aber es ist viel einfacher, den Stern auf der rechten Seite zu bedienen

<br />

<a name="faq6"></a>
**(6) Wie kann ich mich bei Gmail / G-Suite anmelden?**

Wenn Sie die Play Store- oder GitHub-Version von FairEmail verwenden, können Sie den Schnelleinrichtungsassistenten verwenden, um ein Gmail-Konto und eine Identität einfach einzurichten. Der Gmail-Schnelleinrichtungsassistent ist für Builds von Drittanbietern, wie dem F-Droid-Build, nicht verfügbar da Google die Verwendung von OAuth nur für offizielle Builds freigegeben hat.

Wenn Sie kein geräteinternes Google-Konto verwenden möchten oder können, z. B. auf neueren Huawei-Geräten, können Sie entweder den Zugriff für "weniger sichere Apps" aktivieren und Ihr Kontopasswort verwenden (nicht empfohlen) oder die Zwei-Faktor-Authentifizierung aktivieren und ein App-spezifisches Passwort verwenden. Um ein Kennwort zu verwenden, müssen Sie ein Konto und eine Identität über die manuelle Einrichtung einrichten, anstatt über den Schnelleinrichtungsassistenten.

**Wichtig**: Manchmal gibt Google diese Warnung aus:

*[ALERT] Bitte loggen Sie sich über Ihren Webbrowser ein: https://support.google.com/mail/accounts/answer/78754 (Fehlfunktion)*

Diese Google-Sicherheitsprüfung wird häufiger ausgelöst, wenn *wenig sichere Apps* aktiviert sind, weniger mit einem App-Passwort und kaum bei Verwendung eines On-Device-Kontos (OAuth).

Bitte lesen Sie [diese FAQ](#user-content-faq111), warum nur geräteinterne Konten verwendet werden können.

Beachten Sie, dass ein App-spezifisches Passwort erforderlich ist, wenn die Zwei-Faktor-Authentifizierung aktiviert ist.

<br />

*App-spezifisches Passwort*

Lesen Sie [hier](https://support.google.com/accounts/answer/185833), wie Sie ein App-spezifisches Passwort erzeugen können.

<br />

*Aktivieren Sie "Weniger sichere Apps"*

**Wichtig**: Die Verwendung dieser Methode wird nicht empfohlen, da sie weniger zuverlässig ist.

**Wichtig**: Gsuite-Konten, die mit einem Benutzernamen/Passwort autorisiert wurden, werden [in naher Zukunft nicht mehr funktionieren](https://gsuiteupdates.googleblog.com/2019/12/less-secure-apps-oauth-google-username-password-incorrect.html).

Siehe [hier](https://support.google.com/accounts/answer/6010255), wie Sie "weniger sichere Apps" aktivieren können oder gehen Sie [direkt zu der Einstellung](https://www.google.com/settings/security/lesssecureapps).

Wenn Sie mehrere Gmail-Konten verwenden, stellen Sie sicher, dass Sie die Einstellung "weniger sichere Apps" des/der richtigen Kontos/Konten ändern.

Beachten Sie, dass Sie den Einstellungsbildschirm "Weniger sichere Apps" mit dem Zurück-Pfeil verlassen müssen, um die Einstellung zu übernehmen.

Wenn Sie diese Methode verwenden, sollten Sie ein [starkes Passwort](https://en.wikipedia.org/wiki/Password_strength) für Ihr Gmail-Konto verwenden, was ohnehin eine gute Idee ist. Beachten Sie, dass die Verwendung des [Standard](https://tools.ietf.org/html/rfc3501) IMAP-Protokolls an sich nicht weniger sicher ist.

Wenn "weniger sichere Anwendungen" nicht aktiviert ist, erhalten Sie den Fehler *Authentifizierung fehlgeschlagen - ungültige Anmeldeinformationen* für Konten (IMAP) und *Benutzername und Passwort nicht akzeptiert* für Identitäten (SMTP).

<br />

*Allgemein*

Sie erhalten möglicherweise die Meldung "*Bitte melden Sie sich über Ihren Webbrowser an*". Dies geschieht, wenn Google das Netzwerk, das Sie mit dem Internet verbindet (dies könnte ein VPN sein), als unsicher einstuft. Dies kann mit dem Gmail-Schnelleinrichtungsassistenten oder einem App-spezifischen Passwort verhindert werden.

Siehe [hier](https://support.google.com/mail/answer/7126229) für die Anweisungen von Google und [hier](https://support.google.com/mail/accounts/answer/78754) für die Fehlerbehebung.

<br />

<a name="faq7"></a>
**(7) Warum erscheinen gesendete Nachrichten nicht (direkt) im Ordner "Gesendet"?**

Gesendete Nachrichten werden normalerweise vom Postausgang in den Ordner "Gesendet" verschoben, sobald Ihr Provider gesendete Nachrichten in den Ordner "Gesendet" hinzufügt. Dazu muss in den Kontoeinstellungen ein Sendeordner ausgewählt und der Sendeordner auf Synchronisierung eingestellt sein.

Einige Provider führen keine Aufzeichnungen über gesendete Nachrichten oder der verwendete SMTP-Server ist möglicherweise nicht mit dem Provider verbunden. In diesen Fällen fügt FairEmail bei der Synchronisierung des Sendeordners, die nach dem Versenden einer Nachricht erfolgt, die gesendeten Nachrichten automatisch dem Sendeordner hinzu. Beachten Sie, dass dies zu zusätzlichem Internetverkehr führt.

~~Wenn dies nicht geschieht, kann es sein, dass Ihr Provider die gesendeten Nachrichten nicht nachverfolgt oder dass Sie einen SMTP-Server verwenden, der nicht mit dem Provider verbunden ist. ~~In diesen Fällen können Sie die erweiterte Identitätseinstellung *Gesendete Nachrichten speichern* aktivieren, damit FairEmail gesendete Nachrichten direkt nach dem Senden einer Nachricht dem Ordner "Gesendet" hinzufügt.~~ ~~Beachten Sie, dass das Aktivieren dieser Einstellung zu doppelten Nachrichten führen kann, wenn Ihr Provider gesendete Nachrichten ebenfalls zum Ordner "Gesendet" hinzufügt.~~ ~~Beachten Sie auch, dass das Aktivieren dieser Einstellung zu einer zusätzlichen Datennutzung führt, vor allem beim Senden von Nachrichten mit großen Anhängen.~~

~~Wenn gesendete Nachrichten im Postausgang bei einer Vollsynchronisation nicht im Ordner "Gesendet" gefunden werden, werden sie auch aus dem Postausgang in den Ordner "Gesendet" verschoben. ~~Eine vollständige Synchronisierung findet statt, wenn die Verbindung zum Server wiederhergestellt wird oder wenn die Synchronisierung periodisch oder manuell erfolgt. ~~Wahrscheinlich möchten Sie stattdessen die erweiterte Einstellung *Gesendete Nachrichten speichern* aktivieren, um Nachrichten früher in den Ordner "Gesendet" zu verschieben.~~

<br />

<a name="faq8"></a>
**(8) Kann ich ein Microsoft Exchange-Konto verwenden?**

Das Microsoft Exchange Web Services-Protokoll [wird schrittweise abgebaut](https://techcommunity.microsoft.com/t5/Exchange-Team-Blog/Upcoming-changes-to-Exchange-Web-Services-EWS-API-for-Office-365/ba-p/608055). Es macht also wenig Sinn, dieses Protokoll noch hinzuzufügen.

Sie können ein Microsoft Exchange-Konto verwenden, wenn es über IMAP erreichbar ist, was meistens der Fall ist. Siehe [hier](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793) für weitere Informationen.

Beachten Sie, dass die Beschreibung von FairEmail mit der Bemerkung beginnt dass Nicht-Standard-Protokolle, wie Microsoft Exchange Web Services und Microsoft ActiveSync nicht unterstützt werden.

Bitte sehen Sie [hier](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) für die Microsoft-Dokumentation zur Konfiguration eines E-Mail-Clients. Außerdem gibt es einen Abschnitt über häufige Verbindungsfehler und Lösungen.

Einige ältere Exchange-Server-Versionen haben einen Fehler, der leere Nachrichten und beschädigte Anhänge verursacht. Bitte lesen Sie [diese FAQ](#user-content-faq110) für einen Workaround.

Bitte beachten Sie [diese FAQ](#user-content-faq133) zur ActiveSync-Unterstützung.

Bitte beachten Sie [diese FAQ](#user-content-faq111) zur OAuth-Unterstützung.

<br />

<a name="faq9"></a>
**(9) Was sind Identitäten / wie füge ich einen Alias hinzu?**

Identitäten stellen E-Mail-Adressen dar, die Sie *von* über einen E-Mail (SMTP)-Server senden.

Bei einigen Providern können Sie mehrere Aliasnamen haben. Sie können diese konfigurieren, indem Sie das E-Mail-Adressfeld einer zusätzlichen Identität auf die Alias-Adresse und das Feld für den Benutzernamen auf Ihre Haupt-E-Mail-Adresse setzen.

Beachten Sie, dass Sie eine Identität kopieren können, indem Sie sie lange drücken.

Alternativ können Sie *Bearbeiten der Absenderadresse zulassen* in den erweiterten Einstellungen einer bestehenden Identität aktivieren, um den Benutzernamen beim Verfassen einer neuen Nachricht zu bearbeiten, wenn Ihr Provider dies zulässt.

FairEmail aktualisiert automatisch die Passwörter der zugehörigen Identitäten, wenn Sie das Passwort des zugehörigen Kontos oder einer zugehörigen Identität aktualisieren.

Siehe [diese FAQ](#user-content-faq33) zum Bearbeiten des Benutzernamens von E-Mail-Adressen.

<br />

<a name="faq10"></a>
**~~(10) Was bedeutet 'UIDPLUS nicht unterstützt'?~~**

~~Die Fehlermeldung *UIDPLUS nicht unterstützt* bedeutet, dass Ihr E-Mail-Anbieter die IMAP-Erweiterung [UIDPLUS](https://tools.ietf.org/html/rfc4315) nicht zur Verfügung stellt. Diese IMAP-Erweiterung ist erforderlich, um die Zwei-Wege-Synchronisation zu implementieren, die keine optionale Funktion ist. Wenn Ihr Provider diese Erweiterung also nicht aktivieren kann, können Sie FairEmail für diesen Provider nicht verwenden.~~

<br />

<a name="faq11"></a>
**~~(11) Warum wird POP nicht unterstützt?~~**

~~Abgesehen davon, dass jeder anständige E-Mail-Anbieter heutzutage [IMAP](https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol) unterstützt,~~ ~~Die Verwendung von [POP](https://en.wikipedia.org/wiki/Post_Office_Protocol) führt zu unnötigem zusätzlichen Batterieverbrauch und verzögerten Benachrichtigungen über neue Nachrichten.~~ ~~Darüber hinaus ist POP für die Zwei-Wege-Synchronisation ungeeignet, und die meisten Leute lesen und schreiben Nachrichten heutzutage auf verschiedenen Geräten.~~

~~Grundsätzlich unterstützt POP nur das Herunterladen und Löschen von Nachrichten aus dem Posteingang.~~ ~~Daher sind gängige Operationen wie das Setzen von Nachrichtenattributen (gelesen, mit Sternchen versehen, beantwortet usw.), das Hinzufügen (Sichern) und Verschieben von Nachrichten nicht möglich.~~

~~Siehe auch [was Google dazu schreibt](https://support.google.com/mail/answer/7104828).~~

~~Zum Beispiel kann [Gmail](https://support.google.com/mail/answer/21289) Nachrichten von einem anderen POP-Konto importieren,~~ ~~Was als Abhilfe genutzt werden kann, wenn Ihr Provider IMAP nicht unterstützt.~~

~~Zusammenfassung; Überlegen Sie, ob Sie zu IMAP wechseln wollen.~~

<br />

<a name="faq12"></a>
**(12) Wie funktioniert die Verschlüsselung/Entschlüsselung?**

Die Kommunikation mit E-Mail-Servern ist immer verschlüsselt, es sei denn, Sie haben dies explizit ausgeschaltet. Diese Frage bezieht sich auf die optionale Ende-zu-Ende-Verschlüsselung mit PGP oder S/MIME. Absender und Empfänger sollten sich zunächst darauf einigen und signierte Nachrichten austauschen, um ihren öffentlichen Schlüssel zu übertragen, damit sie verschlüsselte Nachrichten senden können.

<br />

*Allgemein*

Bitte sehen Sie [hier](https://en.wikipedia.org/wiki/Public-key_cryptography), wie die Verschlüsselung mit öffentlichen/privaten Schlüsseln funktioniert.

Verschlüsselung in Kurzform:

* **Ausgehende** Nachrichten werden mit dem **öffentlichen Schlüssel** des Empfängers verschlüsselt
* **Eingehende** Nachrichten werden mit dem **privaten Schlüssel** des Empfängers entschlüsselt

Signieren in Kurzform:

* **Ausgehende** Nachrichten sind mit dem **privaten Schlüssel** des Absenders signiert
* **Eingehende** Nachrichten werden mit dem **öffentlichen Schlüssel** des Absenders überprüft

Um eine Nachricht zu signieren/verschlüsseln, wählen Sie einfach die entsprechende Methode im Sendedialog. Sie können den Sendedialog jederzeit über das Drei-Punkte-Menü öffnen, falls Sie zuvor *Nicht mehr anzeigen* gewählt haben.

Um eine Signatur zu überprüfen oder eine empfangene Nachricht zu entschlüsseln, öffnen Sie die Nachricht und tippen Sie einfach auf die Geste oder das Vorhängeschloss-Symbol direkt unter der Aktionsleiste der Nachricht.

Wenn Sie zum ersten Mal eine signierte/verschlüsselte Nachricht senden, werden Sie möglicherweise nach einem Signierschlüssel gefragt. FairEmail speichert den gewählten Signierschlüssel automatisch in der verwendeten Identität für das nächste Mal. Wenn Sie den Signierungsschlüssel zurücksetzen müssen, speichern Sie einfach die Identität oder drücken Sie lange auf die Identität in der Liste der Identitäten und wählen Sie *Signierungsschlüssel zurücksetzen*. Der ausgewählte Signierungsschlüssel ist in der Liste der Identitäten sichtbar. Wenn Sie einen Schlüssel von Fall zu Fall auswählen müssen, können Sie mehrere Identitäten für dasselbe Konto mit derselben E-Mail-Adresse erstellen.

In den Verschlüsselungseinstellungen können Sie die Standardverschlüsselungsmethode (PGP oder S/MIME) auswählen, *Standardmäßig unterschreiben*, *Standmäßig verschlüsseln* und *Nachrichten automatisch entschlüsseln* aktivieren, Beachten Sie jedoch, dass eine automatische Entschlüsselung nicht möglich ist, wenn eine Benutzerinteraktion erforderlich ist, wie z. B. die Auswahl eines Schlüssels oder das Lesen eines Sicherheitstokens.

Der zu verschlüsselnde Nachrichtentext/die zu verschlüsselnden Anhänge und der entschlüsselte Nachrichtentext/die entschlüsselten Anhänge werden nur lokal gespeichert und werden niemals auf dem Remote-Server hinzugefügt. Wenn Sie die Entschlüsselung rückgängig machen wollen, können Sie den Menüpunkt *Resync* im Drei-Punkte-Menü der Nachrichtenaktionsleiste verwenden.

<br />

*PGP*

Sie müssen zuerst [OpenKeychain](https://f-droid.org/en/packages/org.sufficientlysecure.keychain/) installieren und konfigurieren. FairEmail wurde mit OpenKeychain Version 5.4 getestet. Spätere Versionen sind höchstwahrscheinlich kompatibel, aber frühere Versionen sind es möglicherweise nicht.

**Wichtig**: die OpenKeychain-App ist dafür bekannt, (stillschweigend) abzustürzen, wenn die aufrufende App (FairEmail) noch nicht autorisiert ist und einen bestehenden öffentlichen Schlüssel erhält. Sie können dieses Problem umgehen, indem Sie versuchen, eine signierte/verschlüsselte Nachricht an einen Absender mit einem unbekannten öffentlichen Schlüssel zu senden.

**Wichtig**: Wenn die OpenKeychain-App einen Schlüssel nicht (mehr) findet, müssen Sie eventuell einen zuvor ausgewählten Schlüssel zurücksetzen. Das kann durch langes Drücken einer Identität in der Liste der Identitäten erfolgen (Einstellungen → Manuelle Einrichtung und Kontooptionen → Identitäten).

**Wichtig**: damit sich Apps wie FairEmail zuverlässig mit dem OpenKeychain-Dienst verbinden können, um Nachrichten zu verschlüsseln/entschlüsseln, kann es notwendig sein, die Akku-Optimierungen für die OpenKeychain-App zu deaktivieren.

**Wichtig**: Die OpenKeychain-App benötigt vermutlich die Nutzung der Kontakte, um korrekt zu funktionieren.

**Wichtig**: auf einigen Android-Versionen / Geräten ist es notwendig, *Popups anzeigen, während sie im Hintergrund laufen* zu aktivieren. in den zusätzlichen Berechtigungen der Android-App-Einstellungen der OpenKeychain-App. Ohne diese Berechtigung wird der Entwurf zwar gespeichert, aber das OpenKeychain-Popup zum Bestätigen/Auswählen erscheint möglicherweise nicht.

FairEmail sendet, nur für signierte und verschlüsselte Nachrichten, da zu viele E-Mail-Server Probleme mit dem oft langen Header haben, einen [Autocrypt](https://autocrypt.org/)-Header zur Verwendung durch andere E-Mail-Clients. Beachten Sie, dass die sicherste Art, einen verschlüsselten E-Mail-Austausch zu beginnen, darin besteht, dass zuerst signierte Nachrichten gesendet werden. Empfangene Autocrypt Header werden an die OpenKeychain App gesendet, um eine Signatur zu überprüfen oder eine Nachricht zu entschlüsseln.

Obwohl dies für die meisten E-Mail-Clients nicht notwendig sein sollte, können Sie Ihren Public-Key an eine Nachricht anhängen und wenn Sie *.key* als Dateiendung verwenden, ist der korrekte Mime-Typ *application/pgp-keys*.

Alle Schlüsselbearbeitung wird aus Sicherheitsgründen an die OpenKey-Chainapp übertragen. Das bedeutet auch, dass FairEmail keine PGP-Schlüssel speichert.

Inline verschlüsseltes PGP in empfangenen Nachrichten wird unterstützt, aber Inline-PGP-Signaturen und Inline-PGP in ausgehenden Nachrichten werden nicht unterstützt, siehe [hier](https://josefsson.org/inline-openpgp-considered-harmful.html) warum nicht.

Nur signierte oder nur verschlüsselte Nachrichten sind keine gute Idee. Bitte sehen Sie hier nach, warum nicht:

* [OpenPGP-Überlegungen Teil I](https://k9mail.github.io/2016/11/24/OpenPGP-Considerations-Part-I.html)
* [OpenPGP-Überlegungen Teil II](https://k9mail.github.io/2017/01/30/OpenPGP-Considerations-Part-II.html)
* [OpenPGP-Überlegungen Teil III Autocrypt](https://k9mail.github.io/2018/02/26/OpenPGP-Considerations-Part-III-Autocrypt.html)

Nur signierte Nachrichten werden unterstützt, verschlüsselte Nachrichten nicht.

Häufige Fehler:

* *Kein Schlüssel*: kein PGP-Schlüssel für eine der aufgeführten E-Mail-Adressen verfügbar
* *Verschlüsselungsschlüssel fehlt*: es wurde wahrscheinlich in FairEmail ein Schlüssel ausgewählt, der in der OpenKeychain-App nicht mehr vorhanden ist. Das Zurücksetzen des Schlüssels (siehe oben) wird dieses Problem eventuell beheben.
* *Schlüssel zur Signaturüberprüfung fehlt*: Der öffentliche Schlüssel für den Absender ist in der OpenKeychain App nicht verfügbar. Dies kann auch dadurch verursacht werden, dass Autocrypt in den Verschlüsselungseinstellungen deaktiviert wird oder der Autocrypt-Header nicht gesendet wird.

<br />

*S/MIME*

Die Verschlüsselung einer Nachricht erfordert den Public Key der/desEmpfänger(s). Das Signieren einer Nachricht erfordert Ihren Private Key.

Private Schlüssel werden von Android gespeichert und können über die erweiterten Sicherheitseinstellungen von Android importiert werden. Dafür gibt es einen Knopf in den Datenschutzeinstellungen. Android wird Sie bitten, eine PIN, ein Ensperrungsmuster oder ein Passwort festzulegen, falls Sie dies zuvor nicht getan haben. Wenn Sie ein Nokia-Gerät mit Android 9 haben, [lesen Sie bitte zuerst das](https://nokiamob.net/2019/08/10/a-bug-prevents-nokia-1-owners-from-unlocking-their-screen-even-with-right-pin-pattern/).

Beachten Sie, dass Zertifikate mehrere Schlüssel für mehrere Zwecke enthalten können, zum Beispiel zur Authentifizierung, Verschlüsselung und Signierung. Android importiert nur den ersten Schlüssel, um alle Schlüssel zu importieren, muss das Zertifikat zuerst aufgeteilt werden. Das ist nicht sehr einfach, und es wird empfohlen, den Zertifikatlieferanten um Unterstützung zu bitten.

Beachten Sie, dass S/MIME-Signierung mit anderen Algorithmen als RSA zwar unterstützt wird, aber andere E-Mail-Clients dies möglicherweise nicht unterstützen. S/MIME-Verschlüsselung ist nur mit asymmetrischen Algorithmen möglich, das heißt in der Praxis RSA.

Die Standard-Verschlüsselungsmethode ist PGP, aber die zuletzt verwendete Verschlüsselungsmethode wird für die ausgewählte Identität gespeichert. Sie können lange auf den Sendenknopf drücken, um die Verschlüsselungsmethode für eine Identität zu ändern. Wenn Sie sowohl PGP als auch S/MIME-Verschlüsselung für die gleiche E-Mail-Adresse verwenden, kann es sinnvoll sein, die Identität zu kopieren, damit Sie die Verschlüsselungsmethode ändern können, indem Sie eine der beiden Identitäten auswählen. Sie können in der Liste der Identitäten lange auf eine Identität drücken (über die manuelle Einrichtung im Hauptbildschirm), um eine Identität zu kopieren.

Um verschiedene Private Keys für die gleiche E-Mail-Adresse nutzen zu können, lässt FairEmail Sie immer einen Key auswählen, wenn es mehrere Identitäten mit der gleichen E-Mail-Adresse für denselben Account gibt.

Öffentliche Schlüssel werden bei FairEmail gespeichert und können bei der erstmaligen Überprüfung einer Signatur oder über die Privatsphäre-Einstellungen (PEM oder DER Format) importiert werden.

FairEmail überprüft sowohl die Unterschrift als auch die gesamte Zertifikatskette.

Häufige Fehler:

* *Kein Zertifikat gefunden, das mit targetContraints übereinstimmt*: Das heißt meistens, dass Sie eine alte Version von FairEmail nutzen
* *Es konnte kein gültiger Zertifizierungspfad für das gewünschte Ziel gefunden werden*: Im Prinzip heißt das, dass ein oder mehrere gleichzeitige oder ein Root-Zertifikat nicht gefunden wurden
* *Privater Schlüssel stimmt mit keinem Verschlüsselungsschlüssel überein*: Der ausgewählte Schlüssel kann nicht verwendet werden, um die Nachricht zu entschlüsseln, wahrscheinlich weil es der falsche Schlüssel ist
* *Kein privater Schlüssel*: Es wurde kein Zertifikat ausgewählt oder kein Zertifikat im Android-Schlüsselgeschäft verfügbar

Wenn die Zertifizierungskette falsch ist, können sie auf den kleinen Info-Knopf drücken, um alle Zertifikate anzuzeigen. Unter den Zertifikatdetails befinden sich der Aussteller des Zertifikats oder "SelfSugn". Ein Zertifikat ist selbst-signiert ("self-signed"), wenn der Empfänger und der Aussteller gleich sind. Zertifikate einer Zertifikatsbehörde (CA) werden mit "[keyCertSign](https://tools.ietf.org/html/rfc5280#section-4.2.1.3) gekennzeichnet. Zertifikate im Android-Schlüsselspeicher werden mit »Android« gekennzeichnet.

Eine gültige Kette sieht so aus:

```
Your certificate > zero or more intermediate certificates > CA (root) certificate marked with "Android"
```

Beachten Sie, dass eine Zertifikatskette immer ungültig ist, wenn im Android-Schlüsselspeicher kein Ankerzertifikat gefunden werden kann - was für die Validierung von S/MIME-Zertifikaten von grundlegender Bedeutung ist.

Siehe [hier](https://support.google.com/pixelphone/answer/2844832?hl=en), wie Sie Zertifikate in den Android-Key-Store importieren können.

Die Verwendung von abgelaufenen Schlüsseln, inline verschlüsselten oder signierten Nachrichten und Hardware-Sicherheitstokens wird nicht unterstützt.

Wenn Sie ein kostenloses (Test-) S/MIME Zertifikat suchen, finden Sie die Optionen [hier](http://kb.mozillazine.org/Getting_an_SMIME_certificate). Achten sie darauf, [das](https://davidroessli.com/logs/2019/09/free-smime-certificates-in-2019/#update20191219) zuerst zu lesen, wenn sie ein solches Zertifikat erhalten wollen. Wenn Sie ein günstiges S/MIME-Zertifikat suchen, habe ich mit [Certum](https://www.certum.eu/en/smime-certificates/) gute Erfahrungen gemacht.

Wie man einen öffentlichen Schlüssel aus einem S/MIME-Zertifikat extrahiert:

```
openssl pkcs12 -in filename.pfx/p12 -clcerts -nokeys -out cert.pem
```

Sie können S/MIME-Signaturen usw. dekodieren, siehe [hier](https://lapo.it/asn1js/).

<br />

*ziemlich simple Privatsphäre*

Es gibt immer noch [keinen anerkannten Standard](https://tools.ietf.org/id/draft-birk-pep-00.html) für ziemlich einfache Privatsphäre (p= p), und nicht viele Benutzer verwenden ihn.

FairEmail kann jedoch verschlüsselte PGP Nachrichten senden und empfangen, die mit p=p kompatibel sind. FairEmail kann seit Version 1. 519 auch eingehende p=p Nachrichten verarbeiten, sodass der verschlüsselte Betreff angezeigt wird und der eingebettete Nachrichtentext schöner angezeigt wird.

<br />

Mit S/MIME signieren oder verschlüsseln ist eine Pro-Funktion, aber alle anderen PGP- und S/MIME-Vorgänge sind kostenlos nutzbar.

<br />

<a name="faq13"></a>
**(13) Wie funktioniert die Suche auf dem Gerät/Server?**

Sie können die Suche nach Nachrichten über Absender (Von), Empfänger (An, CC, BCC), Betreff, Schlüsselwörter oder Nachrichtentext starten, indem Sie die Lupe in der Aktionsleiste eines Ordners verwenden. Sie können auch von jeder App aus suchen, indem Sie *E-Mails suchen* im Kopieren/Einfügen-Popup-Menü auswählen.

Die Suche im vereinheitlichten Posteingang sucht in allen Ordnern aller Konten, die Suche in der Ordnerliste sucht nur in dem zugehörigen Konto und die Suche in einem Ordner sucht nur in diesem Ordner.

Es wird zuerst auf dem Gerät nach Nachrichten gesucht. Am unteren Rand befindet sich eine Aktionsschaltfläche mit einem Symbol für "Erneut suchen", um die Suche auf dem Server fortzusetzen. Sie können auswählen, in welchem Ordner Sie die Suche fortsetzen möchten.

Das IMAP-Protokoll unterstützt nicht die gleichzeitige Suche in mehr als einem Ordner. Die Suche auf dem Server ist ein aufwendiger Vorgang, daher ist es nicht möglich, mehrere Ordner auszuwählen.

Die Suche nach lokalen Nachrichten erfolgt unabhängig von der Groß-/Kleinschreibung und auf Teiltexten. Der Nachrichtentext von lokalen Nachrichten wird nicht durchsucht, wenn der Nachrichtentext noch nicht heruntergeladen wurde. Die Suche auf dem Server kann je nach Anbieter die Groß- und Kleinschreibung beachten und sich auf Teiltexte oder ganze Wörter beziehen.

Einige Server können die Suche im Nachrichtentext nicht bewältigen, wenn es eine große Anzahl von Nachrichten gibt. Für diesen Fall gibt es eine Option, um die Suche im Nachrichtentext zu deaktivieren.

Für diesen Fall gibt es eine Option, um die Suche im Nachrichtentext zu deaktivieren. Es ist möglich, Gmail-Suchoperatoren zu verwenden, indem einem Suchbefehl *raw:* vorangestellt wird. Wenn Sie nur ein Gmail-Konto konfiguriert haben, können Sie eine Rohsuche direkt auf dem Server starten, indem Sie aus dem vereinheitlichten Posteingang suchen. Wenn Sie mehrere Gmail-Konten konfiguriert haben, müssen Sie zunächst zur Ordnerliste oder zum Archivordner (alle Nachrichten) des Gmail-Kontos navigieren, in dem Sie suchen möchten. Bitte [sehen Sie hier](https://support.google.com/mail/answer/7190) für die möglichen Suchoperatoren. Zum Beispiel:

`
raw:größer:10M`

Das Durchsuchen einer großen Anzahl von Nachrichten auf dem Gerät ist aufgrund von zwei Einschränkungen nicht sehr schnell:

* [sqlite](https://www.sqlite.org/), die Datenbank-Engine von Android hat ein Datensatzgrößenlimit und verhindert, dass Nachrichten in der Datenbank gespeichert werden
* Android-Apps können nur mit limitiertem Arbeitsspeicher arbeiten, auch wenn das Gerät viel Speicher zur Verfügung hat

Das bedeutet, dass die Suche nach einem Nachrichtentext erfordert, dass die Dateien, die die Nachrichtentexte enthalten, einzeln geöffnet werden müssen um zu prüfen, ob der gesuchte Text in der Datei enthalten ist, was ein relativ aufwendiger Prozess ist.

In den *Sonstigen Einstellungen* können Sie *Suchindex aufbauen* aktivieren, um die Geschwindigkeit der Suche auf dem Gerät deutlich zu erhöhen, aber seien Sie sich bewusst, dass dies den Batterie- und Speicherplatzverbrauch erhöht. Der Suchindex basiert auf Wörtern, eine Suche nach Teiltexten ist also nicht möglich. Die Suche über den Suchindex ist standardmäßig UND, so dass die Suche nach *Apfel Orange* nach Apfel UND Orange sucht. Durch Kommas getrennte Wörter führen zu einer Suche nach ODER, so dass z. B. *Apfel, Orange* nach Apfel ODER Orange suchen wird. Beide können kombiniert werden, so dass die Suche nach *Apfel, Orange Banane* nach Apfel ODER (Orange UND Banane) sucht. Die Verwendung des Suchindex ist eine Pro-Funktion.

Ab Version 1.1315 ist es möglich, Suchausdrücke wie diesen zu verwenden:

```
Apfel +Banane -Kirsche ?Nüsse
```

Dies führt zu einer Suche wie dieser:

```
(»Apfel« UND »Banane« UND NICHT »Kirsche«) ODER »Nüsse«
```

Suchausdrücke können für die Suche auf dem Gerät über den Suchindex und für die Suche auf dem E-Mail-Server verwendet werden, aber aus Leistungsgründen nicht für die Suche auf dem Gerät ohne Suchindex.

Die Suche auf dem Gerät ist eine kostenlose Funktion, die Nutzung des Suchindex und die Suche auf dem Server ist eine Pro-Funktion.

<br />

<a name="faq14"></a>
**(14) Wie kann ich ein Outlook / Live / Hotmail-Konto einrichten?**

Ein Outlook / Live / Hotmail-Konto kann über den Schnelleinrichtungsassistenten und die Auswahl von *Outlook* eingerichtet werden.

Um ein Outlook-, Live- oder Hotmail-Konto mit aktivierter Zwei-Faktor-Authentifizierung zu verwenden, müssen Sie ein App-Kennwort erstellen. Siehe [hier](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification) für die Details.

Siehe [hier](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) für die Anweisungen von Microsoft.

Zum Einrichten eines Office 365-Kontos lesen Sie bitte [diese FAQ](#user-content-faq156).

<br />

<a name="faq15"></a>
**(15) Warum wird der Meldungstext immer wieder geladen?**

Der Nachrichtenkopf und der Nachrichtentext werden separat vom Server geholt. Der Nachrichtentext größerer Nachrichten wird bei gebührenpflichtigen Verbindungen nicht vorgeholt und wird bei Bedarf beim Erweitern einer Nachricht geholt. Der Nachrichtentext wird später weiter geladen, wenn aktuell keine Verbindung zum Konto besteht, siehe auch die nächste Frage, oder wenn andere Vorgänge wie die Synchronisierung von Nachrichten ausgeführt werden.

Sie können die Konto- und Ordnerliste für den Konto- und Ordnerstatus überprüfen (siehe die Legende für die Bedeutung der Symbole) und die über das Hauptnavigationsmenü zugängliche Vorgangsliste für ausstehende Vorgänge (siehe [diese FAQ](#user-content-faq3) für die Bedeutung der Vorgänge).

Wenn FairEmail wegen vorheriger Verbindungsprobleme (siehe [diese FAQ](#user-content-faq123)) die Synchronisation erzwingt, können Sie dies über das Drei-Punkte-Menü erzwingen.

In den Empfangseinstellungen können Sie die maximale Größe für das automatische Herunterladen von Nachrichten bei gebührenpflichtigen Verbindungen einstellen.

Mobile Verbindungen sind fast immer gebührenpflichtig und einige (kostenpflichtige) Wi-Fi-Hotspots sind es auch.

<br />

<a name="faq16"></a>
**(16) Warum werden die Nachrichten nicht synchronisiert?**

Mögliche Ursachen für nicht synchronisierte (gesendete oder empfangene) Nachrichten sind:

* Konto oder Ordner(e) sind nicht zum Synchronisieren gesetzt
* Die Anzahl der Tage, für die die Nachricht synchronisiert werden soll, ist zu niedrig gesetzt
* Keine Internetverbindung
* Der E-Mail-Server ist vorübergehend nicht verfügbar
* Android hat die Synchronisation gestoppt

Überprüfen Sie also Ihre Konto- und Ordnereinstellungen und prüfen Sie, ob die Konten/Ordner verbunden sind (siehe Legende im Navigationsmenü für die Bedeutung der Symbole).

Wenn es irgendwelche Fehlermeldungen gibt, lesen Sie bitte [diese FAQ](#user-content-faq22).

Auf einigen Geräten, wo viele Anwendungen um Speicher konkurrieren, kann Android den Synchronisierungsdienst als letzte Möglichkeit stoppen.

Einige Android-Versionen stoppen Apps und Dienste zu aggressiv. Siehe [diese Website](https://dontkillmyapp.com/) und [dieses Android-Problem](https://issuetracker.google.com/issues/122098785) für weitere Informationen.

Das Deaktivieren von Batterieoptimierungen (Setup-Schritt 3) verringert die Chance, dass Android den Synchronisierungsdienst stoppt.

Bei aufeinanderfolgenden Verbindungsfehlern wartet FairEmail mit jedem Mal länger, um den Akku Ihres Geräts nicht zu entladen. Dies wird in [in dieser FAQ](#user-content-faq123) beschrieben.

<br />

<a name="faq17"></a>
**~~(17) Warum funktioniert die manuelle Synchronisation nicht?~~**

~~Wenn das *jetzt synchronisieren*-Menü abgeschaltet ist, gibt es keine Verbindung zum Konto.~~

~~Siehe die vorherige Frage für weitere Informationen.~~

<br />

<a name="faq18"></a>
**(18) Warum wird die Nachrichtenvorschau nicht immer angezeigt?**

Die Vorschau des Nachrichtentextes kann nicht angezeigt werden, wenn der Nachrichtentext noch nicht heruntergeladen wurde. Siehe auch [diese häufig gestellten Fragen (FAQ)](#user-content-faq15).

<br />

<a name="faq19"></a>
**(19) Warum sind die Pro-Funktionen so teuer?**

Zuallererst: **FairEmail ist grundsätzlich kostenlos zu nutzen** und nur einige erweiterte Funktionen müssen gekauft werden.

Zuerst: **FairEmail ist grundsätzlich kostenlos**, nur einige erweiterte Funktionen müssen gekauft werden.

Zunächst einmal ist **FairEmail grundsätzlich kostenlos** und nur einige erweiterte Funktionen müssen gekauft werden.

Bitte sehen Sie sich die Beschreibung der App im PlayStore an oder [siehe hier](https://email.faircode.eu/#pro) für eine vollständige Liste der Pro-Features.

Die richtige Frage ist: »*Warum gibt es so viele Steuern und Gebühren?*«:

* MwSt.: 25 % (je nach Land)
* Google-Gebühr: 30 %
* Einkommenssteuer: 50 %
* <sub>PayPal-Gebühr: 5-10 % abhängig vom Land/Betrag</sub>

Was für den Entwickler übrig bleibt, ist also nur ein Bruchteil dessen, was Sie bezahlen.

Beachten Sie auch, dass die meisten kostenlosen Apps am Ende nicht langfristig einsetzbar sind, während FairEmail ordnungsgemäß gewartet und unterstützt wird; und dass kostenlose Anwendungen einen Haken haben können, wie z.B. das Senden vertraulicher Informationen ins Internet. Es gibt in der App auch keine Privatsphäre verletztende Werbung.

Ich arbeite seit mehr als zwei Jahren fast jeden Tag an FairEmail, daher halte ich den Preis für mehr als angemessen. Aus diesem Grund gibt es auch keine Rabatte.

<br />

<a name="faq20"></a>
**(20) Kann ich eine Rückerstattung erhalten?**

Wenn eine erworbene Pro-Funktion nicht wie geplant funktioniert, dies nicht durch ein Problem in den kostenlosen Funktionen verursacht wird und ich das Problem nicht rechtzeitig beheben kann, erhalten Sie eine Rückerstattung. In allen anderen Fällen ist keine Rückerstattung möglich. Unter keinen Umständen ist eine Rückerstattung für Probleme im Zusammenhang mit den kostenlosen Funktionen möglich, da diese nicht kostenpflichtig sind und diese uneingeschränkt genutzt werden können. Ich übernehme meine Verantwortung als Verkäufer, um die versprochenen Funktionen zu liefern, und ich erwarte, dass Sie die Verantwortung übernehmen, sich darüber zu informieren, was Sie kaufen.

<a name="faq21"></a>
**(21) Wie aktiviere ich das Benachrichtigungslicht?**

Vor Android 8 Oreo: Es gibt eine erweiterte Option im Setup dafür.

Android 8 Oreo und später: Siehe [hier](https://developer.android.com/training/notify-user/channels) über die Konfiguration von Benachrichtigungskanälen. Sie können die Schaltfläche *Standardkanal* in den Benachrichtigungseinstellungen der App verwenden um direkt zu den richtigen Einstellungen für den Android Benachrichtigungskanal zu gelangen.

Beachte, dass Apps die Benachrichtigungseinstellungen, einschließlich der Benachrichtigungslichteinstellung, auf Android 8 Oreo und späte nicht mehr ändern können.

Manchmal ist es notwendig, die Einstellung *Nachrichtenvorschau in Benachrichtigungen anzeigen* zu deaktivieren oder die Einstellungen *Benachrichtigungen nur mit Vorschautext anzeigen * zu aktivieren, um einen Fehler in Android zu beheben. Dies kann auch für Benachrichtigungstöne und Vibrationen gelten.

Das Setzen einer Lichtfarbe vor Android 8 wird nicht unterstützt und ist auf Android 8 und später nicht möglich.

<br />

<a name="faq22"></a>
**(22) Was bedeutet Konto/Ordnerfehler ... ?**

FairEmail versteckt keine Fehler, wie es ähnliche Apps meistens tun, daher ist es einfacher, Probleme zu diagnostizieren.

FairEmail versucht automatisch, sich nach einer Verzögerung erneut zu verbinden. Diese Verzögerung wird sich nach jedem fehlgeschlagenen Versuch verdoppeln, um das Entladen der Batterie zu verhindern und zu verhindern, dass sie dauerhaft gesperrt wird. Weitere Informationen finden Sie in den [Häufig gestellten Fragen (FAQ)](#user-content-faq123).

Es gibt allgemeine Fehler und Fehler spezifisch für Gmail-Konten (siehe unten).

**Allgemeine Fehler**

<a name="authfailed"></a>
Der Fehler *... **Authentifizierung fehlgeschlagen** ...* oder *... „Authentifizierung fehlgeschlagen …”* bedeutet wahrscheinlich, dass Ihr Benutzername oder Passwort ungültig ist. Einige Anbieter erwarten als Benutzername nur den *Benutzernamen* und andere jedoch Ihre vollständige E-Mail-Adresse (*benutzername@beispiel.de*). Beim Kopieren/Einfügen eines Benutzernamens oder Passworts können unsichtbare Zeichen kopiert werden, was auch zu diesem Problem führen kann. Einige Passwortmanager sind dafür bekannt, diesen Fehler ebenfalls zu machen. Beim Benutzernamen wird möglicherweise die Groß- und Kleinschreibung berücksichtigt, versuchen Sie also nur Kleinbuchstaben. Beim Passwort wird fast immer zwischen Groß- und Kleinschreibung unterschieden. Einige Anbieter verlangen die Verwendung eines App-Passworts anstelle des Kontopassworts, daher prüfen Sie bitte die Dokumentation des Anbieters. Manchmal ist es notwendig, den externen Zugriff (IMAP/SMTP) erst auf der Website des Providers zu aktivieren. Andere mögliche Ursachen sind, dass das Konto gesperrt ist oder dass die Anmeldung auf irgendeine Weise administrativ eingeschränkt wurde, z. B. indem die Anmeldung nur von bestimmten Netzwerken / IP-Adressen erlaubt ist.

Bei Bedarf können Sie ein Passwort in den Kontoeinstellungen aktualisieren: Navigationsmenü (linkes Seitenmenü), *Einstellungen* → *Manuelle Einrichtung und Kontooptionen* → *Konten* → Konto auswählen. Das Ändern des Kontopassworts ändert in den meisten Fällen automatisch auch das Passwort der zugehörigen Identitäten. Wenn das Konto mit OAuth über den Schnelleinrichtungsassistenten statt mit einem Passwort autorisiert wurde, können Sie den Schnelleinrichtungsassistenten erneut ausführen und *Vorhandenes Konto erneut autorisieren* ankreuzen, um das Konto erneut zu authentifizieren. Beachten Sie, dass dafür eine aktuelle Version der App erforderlich ist.

Der Fehler *... Zu viele schlechte Authentifizierungsversuche ...* bedeutet wahrscheinlich, dass Sie ein Yahoo-Kontopasswort anstelle eines App-Passworts verwenden. Bitte lesen Sie [diese FAQ](#user-content-faq88), wie Sie ein Yahoo-Konto einrichten können.

Die Meldung *... +OK ...* bedeutet wahrscheinlich, dass ein POP3-Port (normalerweise Portnummer 995) für ein IMAP-Konto (normalerweise Portnummer 993) verwendet wird.

Die Fehler *... ungültige Ansage ...*, *... erfordert gültige Adresse ...* und *... Parameter an HELO entspricht nicht der RFC-Syntax ...* kann wahrscheinlich durch Ändern der erweiterten Identitätseinstellung *Lokale IP-Adresse statt Hostname verwenden* gelöst werden.

Der Fehler *... Couldn't connect to host ...* bedeutet, dass es innerhalb einer angemessenen Zeit (standardmäßig 20 Sekunden) keine Antwort vom E-Mail-Server gab. Meistens deutet dies auf Probleme mit der Internetverbindung hin, möglicherweise verursacht durch ein VPN oder eine Firewall-App. Sie können versuchen, den Verbindungstimeout in den Verbindungseinstellungen von FairEmail zu erhöhen, für den Fall, dass der E-Mail-Server wirklich langsam ist.

Der Fehler *... Verbindung verweigert ...* bedeutet, dass der E-Mail-Server oder etwas zwischen dem E-Mail-Server und der App, wie z. B. eine Firewall, die Verbindung aktiv abgelehnt hat.

Der Fehler *... Netzwerk nicht erreichbar ...* bedeutet, dass der E-Mail-Server über die aktuelle Internetverbindung nicht erreichbar war, z. B. weil der Internetverkehr nur auf den lokalen Verkehr beschränkt ist.

Der Fehler *... Host ist nicht aufgelöst ...*, *... Host konnte nicht aufgelöst werden ...* oder *... Dem Hostnamen ist keine Adresse zugeordnet ...* bedeutet, dass die Adresse des E-Mail-Servers nicht in eine IP-Adresse aufgelöst werden konnte. Dies kann durch ein VPN, eine Werbeblockierung oder einen nicht erreichbaren oder nicht richtig funktionierenden (lokalen) [DNS](https://en.wikipedia.org/wiki/Domain_Name_System)-Server verursacht werden.

Der Fehler *... Software verursachte Verbindungsabbruch ...* bedeutet, dass der E-Mail-Server oder etwas zwischen FairEmail und dem E-Mail-Server eine bestehende Verbindung aktiv abgebrochen hat. Dies kann z. B. passieren, wenn die Verbindung abrupt unterbrochen wurde. Ein typisches Beispiel ist das Einschalten des Flugmodus.

Die Fehler *... BYE Abmelden ...*, *... Verbindungsabbruch ...* bedeutet, dass der E-Mail-Server oder etwas zwischen dem E-Mail-Server und der App, zum Beispiel ein Router oder eine Firewall (App), eine bestehende Verbindung aktiv beendet hat.

Der Fehler *... Connection closed by peer ...* kann durch einen nicht aktualisierten Exchange-Server verursacht werden, siehe [hier](https://blogs.technet.microsoft.com/pki/2010/09/30/sha2-and-windows/) für weitere Informationen.

Die Fehler *... Lesefehler ...*, *... Schreibfehler ...*, *... Zeitüberschreitung beim Lesen … *, *... Broken pipe ...* bedeutet, dass der E-Mail-Server nicht mehr antwortet oder dass die Internetverbindung schlecht ist.

<a name="connectiondropped"></a>
Der Fehler *... Verbindung vom Server abgebrochen? ...* bedeutet, dass der E-Mail-Server die Verbindung unerwartet beendet hat. Dies kann passieren, wenn zu viele Verbindungen in zu kurzer Zeit aufgebaut wurden oder ein falsches Passwort zu oft verwendet wurde. Vergewissern Sie sich in diesem Fall, dass Ihr Passwort korrekt ist und deaktivieren Sie den Empfang in den Empfangseinstellungen für ca. 30 Minuten und versuchen Sie es erneut. Bei Bedarf lesen Sie in [dieser FAQ](#user-content-faq23) nach, wie Sie die Anzahl der Verbindungen reduzieren können.

Der Fehler *... Unerwartetes Ende des zlib-Eingangsstroms ...* bedeutet, dass nicht alle Daten empfangen wurden, möglicherweise aufgrund einer schlechten oder unterbrochenen Verbindung.

Der Fehler *... Verbindungsfehler ...* könnte auf [Zu viele gleichzeitige Verbindungen](#user-content-faq23) hinweisen.

Der Fehler *... Nicht unterstützte Kodierung ...* bedeutet, dass der Zeichensatz der Nachricht unbekannt ist oder nicht unterstützt wird. FairEmail geht von ISO-8859-1 (latin1) aus, was in den meisten Fällen dazu führt, dass die Nachricht korrekt angezeigt wird.

Der Fehler *... Anmelderaten-Beschränkung überschritten …* bedeutet, dass es zu viele Anmeldeversuche mit einem falschen Passwort gab. Bitte überprüfen Sie Ihr Passwort oder authentifizieren Sie das Konto erneut mit dem Schnelleinstellungsassistenten (nur OAuth).

[Siehe hier](#user-content-faq4) für die Fehler *... Nicht vertrauenswürdig ... nicht im Zertifikat ...*, *... Ungültiges Sicherheitszertifikat (Kann die Identität des Servers nicht überprüfen) ...* oder *... Trust Anchor für Zertifizierungspfad nicht gefunden ...*

[Siehe hier](#user-content-faq127) für den Fehler *... Syntaktisch ungültige(s) HELO-Argument(e) ...*.

[Siehe hier](#user-content-faq41) für den Fehler *... Handshake fehlgeschlagen ...*.

[Siehe hier](https://linux.die.net/man/3/connect), was Fehlercodes wie EHOSTUNREACH oder ETIMEOUT bedeuten.

Mögliche Ursachen sind:

* Eine Firewall oder ein Router blockiert Verbindungen zum Server
* Hostname oder Portnummer ist ungültig
* Es gibt Probleme mit der Internetverbindung
* Es gibt Probleme bei der Auflösung von Domainnamen (Yandex: versuchen Sie, privates DNS in den Android-Einstellungen zu deaktivieren)
* Der E-Mail-Server lehnt die Annahme von (externen) Verbindungen ab
* Der E-Mail-Server lehnt die Annahme einer Nachricht ab, zum Beispiel weil sie zu groß ist oder unzulässige Links enthält
* Es gibt zu viele Verbindungen zum Server, siehe dazu auch die nächste Frage

Viele öffentliche WLAN-Netzwerke blockieren ausgehende E-Mails, um Spam zu verhindern. Manchmal können Sie dies mit einem anderen SMTP-Port umgehen. Lesen Sie die Dokumentation des Anbieters für die nutzbaren Portnummern.

Wenn sie ein [VPN](https://en.wikipedia.org/wiki/Virtual_private_network)-Netzwerk verwenden, kann es sein, dass der VPN-Provider die Verbindung blockiert, weil er etwas zu aggressiv verucht, Spam zu blockieren. Beachten Sie, dass [Google Fi](https://fi.google.com/) auch eine VPN verwendet.

**Sendefehler**

SMTP-Server können Nachrichten aus [einer Reihe von Gründen](https://en.wikipedia.org/wiki/List_of_SMTP_server_return_codes) ablehnen. Zu große Nachrichten und das Auslösen des Spam-Filters eines E-Mail-Servers sind die häufigsten Gründe.

* Die Größe von Anhängen für Google Mail [beträgt 25 MB](https://support.google.com/mail/answer/6584)
* Das Limit für die Größe von Anhängen für Outlook und Office 365 [beträgt 20 MB](https://support.microsoft.com/en-us/help/2813269/attachment-size-exceeds-the-allowable-limit-error-when-you-add-a-large)
* Das Limit für die Größe von Anhängen für Yahoo [beträgt 25 MB](https://help.yahoo.com/kb/SLN5673.html)
* *554 5.7.1 Service nicht verfügbar; Client Host xxx.xxx.xxx.xxx blockiert*, bitte [hier schauen](https://docs.gandi.net/en/gandimail/faq/error_types/554_5_7_1_service_unavailable.html)
* *501 Syntaxfehler — Zeile zu lang* wird oft durch die Verwendung von langen Autocrypt-Kopfzeilen verursacht
* *503 5.5.0 Empfänger bereits angegeben* bedeutet hauptsächlich, dass eine Adresse sowohl als TO als auch als CC Adresse verwendet wird
* *554 5.7.1 … nicht zur Weiterleitung zugelassen* bedeutet, dass der E-Mail-Server den Benutzernamen/E-Mail-Adresse nicht erkennen konnte. Bitte überprüfen Sie den Hostnamen, Benutzernamen und E-Mail-Adresse in den Identitätseinstellungen.
* *550 Spam-Nachricht abgelehnt, weil die IP von .. aufgelistet wird.* bedeutet, dass der E-Mail-Server es abgelehnt hat, eine Nachricht von der aktuellen (öffentlichen) Netzwerk-Adresse zu senden, weil sie zuvor dazu missbraucht wurde, Spam durch (hoffentlich) jemanden anderen zu senden. Bitte versuchen Sie, den Flugmodus für 10 Minuten zu aktivieren, um eine neue Netzwerkadresse zu erhalten.
* *550 Es tut uns leid, aber wir können Ihre E-Mail nicht senden. Entweder der Betreff, ein Link oder ein Anhang enthält möglicherweise Spam, Phishing oder Malware.* bedeutet, dass der E-Mail-Anbieter eine ausgehende Nachricht als schädlich betrachtet.
* *571 5.7.1 Nachricht enthält Spam oder einen Virus oder der Absender ist blockiert ...* bedeutet, dass der E-Mail-Server eine ausgehende Nachricht als Spam betrachtet. Dies bedeutet wahrscheinlich, dass die Spamfilter des E-Mail-Servers zu streng sind. Sie müssen den E-Mail-Anbieter kontaktieren, um Unterstützung zu erhalten.
* *451 4.7.0 Temporärer Serverfehler. Bitte versuchen Sie es später erneut. PRX4 …*: bitte [siehe hier](https://www.limilabs.com/blog/office365-temporary-server-error-please-try-again-later-prx4) oder [siehe hier](https://judeperera.wordpress.com/2019/10/11/fixing-451-4-7-0-temporary-server-error-please-try-again-later-prx4/).
* *571 5.7.1 Relay access denied*: Bitte tippen Sie den Benutzernamen und die E-Mail-Adresse in den erweiterten Einstellungen der betroffenen Identität (per manuellem Setup) doppelt an.

Wenn Sie den SMTP-Server von Gmaill verwenden wollen, um einen zu strengen Spam-Filter zu umgehen oder um die Zustellung von Nachrichten zu verbessern:

* Überprüfen Sie Ihre E-Mail-Adresse [hier](https://mail.google.com/mail/u/0/#settings/accounts) (Sie müssen dafür einen Desktop-Browser verwenden)
* Die Identitätseinstellungen folgendermaßen ändern (Einstellungen → Manuelle Einrichtung und Kontooptionen → Identitäten → Identität auswählen):

&emsp;&emsp;Benutzername: *Ihre Gmail-Adresse*<br /> &emsp;&emsp;Passwort: *[ein App-Passwort](#user-content-faq6)*<br /> &emsp;&emsp;Host: *smtp.gmail.com*<br /> &emsp;&emsp;Port: *465*<br /> &emsp;&emsp;Verschlüsselung: *SSL/TLS*<br /> &emsp;&emsp;Antwortadresse: *Ihre E-Mail-Adresse* (erweiterte Identitätseinstellungen)<br />

<br />

**Gmail-Fehler**

Die Autorisierung von Gmail-Konten mit dem Schnellassistenten muss regelmäßig über den [Android Account Manager](https://developer.android.com/reference/android/accounts/AccountManager) aktualisiert werden. Dies erfordert Kontakt-/Konto-Berechtigungen und eine Internetverbindung.

Im Falle von Fehlern ist es möglich, ein Google Mail-Konto erneut über den Google Mail-Schnelleinstellungs-Assistenten zu autorisieren/wiederherzustellen.

Der Fehler *... Authentifizierung fehlgeschlagen ... Konto nicht gefunden ...* bedeutet, dass ein zuvor autorisiertes Google Mail-Konto vom Gerät entfernt wurde.

Die Fehler *... Authentifizierung fehlgeschlagen ... Kein Token beim Aktualisieren ...* bedeutet, dass der Android Account-Manager die Autorisierung eines Google Mail-Kontos nicht aktualisieren konnte.

Der Fehler *... Ungültige Anmeldedaten ... Netzwerkfehler ...* bedeutet, dass der Android-Account-Manager aufgrund von Problemen mit der Internetverbindung die Autorisierung eines Google Mail-Kontos nicht aktualisieren konnte

Der Fehler *... Authentifizierung fehlgeschlagen ... Ungültige Anmeldeinformationen ...* könnten durch die Änderung des Kontopassworts verursacht werden oder durch den Entzug der erforderlichen Konto-/Kontaktberechtigungen. Falls das Kontopasswort geändert wurde, müssen Sie das Google-Konto in den Android-Kontoeinstellungen erneut authentifizieren. Falls die Berechtigungen entzogen wurden, können Sie den Gmail-Schnelleinrichtungsassistenten starten, um die erforderlichen Berechtigungen wieder zu erteilen (Sie müssen das Konto nicht erneut einrichten).

Der Fehler *... ServiceDisabled ...* kann durch die Anmeldung beim [Erweiterten Schutzprogramm](https://landing.google.com/advancedprotection/) verursacht werden: "*Um Ihre E-Mails zu lesen, können (müssen) Sie Google Mail verwenden - Sie können Ihr Google-Konto mit einigen (allen) Apps & Diensten, die Zugriff auf sensible Daten wie Ihre E-Mails benötigen, nicht verwenden*", siehe [hier](https://support.google.com/accounts/answer/7519408?hl=en&ref_topic=9264881).

Im Zweifelsfall können Sie nach [Support](#user-content-support) fragen.

<br />

<a name="faq23"></a>
**(23) Warum erhalte ich Alarm ... ?**

*Allgemein*

Alarme sind Warnmeldungen, die von E-Mail-Servern gesendet werden.

*Zu viele gleichzeitige Verbindungen* oder *Maximale Anzahl von Verbindungen überschritten*

Diese Warnung wird gesendet, wenn es zu viele Ordnerverbindungen für dasselbe E-Mail-Konto zur gleichen Zeit gibt.

Mögliche Ursachen sind:

* Es sind mehrere E-Mail-Clients mit demselben Konto verbunden
* Derselbe E-Mail-Client ist mehrfach mit demselben Konto verbunden
* Frühere Verbindungen wurden abrupt beendet, z. B. durch plötzlichen Verlust der Internetverbindung

Versuchen Sie zunächst, einige Zeit zu warten, um zu sehen, ob sich das Problem von selbst löst. Ansonsten:

* entweder in den Empfangseinstellungen auf periodische Überprüfung auf Nachrichten umschalten, was dazu führt, dass die Ordner nacheinander geöffnet werden
* oder setzen Sie einige Ordner auf Abfrage statt auf Synchronisation (langes Drücken auf Ordner in der Ordnerliste, Eigenschaften bearbeiten)

Eine einfache Möglichkeit, die periodische Prüfung auf Nachrichten für alle Ordner außer dem Posteingang zu konfigurieren ist, im Drei-Punkte-Menü der Ordnerliste *Auf alle anwenden ...* zu verwenden und die unteren beiden erweiterten Kontrollkästchen zu aktivieren.

Die maximale Anzahl der gleichzeitigen Ordnerverbindungen für Google Mail beträgt 15, Sie können also maximal 15 Ordner gleichzeitig auf *allen* Ihren Geräten synchronisieren. Aus diesem Grund sind Gmail *Benutzer*-Ordner standardmäßig auf Polling statt auf "Immer synchronisieren" eingestellt. Wenn nötig oder gewünscht, können Sie dies ändern, indem Sie einen Ordner in der Ordnerliste lange drücken und *Eigenschaften bearbeiten* wählen. Siehe [hier](https://support.google.com/mail/answer/7126229) für Details.

Wenn Sie einen Dovecot-Server verwenden, möchten Sie möglicherweise die Einstellung [mail_max_userip_connections](https://doc.dovecot.org/settings/dovecot_core_settings/#mail-max-userip-connections) ändern.

Beachten Sie, dass der E-Mail-Server eine Weile braucht, um unterbrochene Verbindungen zu erkennen, z. B. durch das Verlassen der Reichweite eines Netzwerks, was bedeutet, dass effektiv nur die Hälfte der Ordnerverbindungen verfügbar ist. Für Gmail wären dies nur 7 Verbindungen.

<br />

<a name="faq24"></a>
**(24) Was heißt Nachrichten auf dem Server durchsuchen?**

Nachrichten auf dem Server durchsuchen holt die Nachrichten in Echtzeit vom E-Mail-Server wenn Sie das Ende der Liste der synchronisierten Nachrichten erreichen, auch wenn der Ordner auf nicht synchronisieren eingestellt ist. Sie können diese Funktion in den erweiterten Kontoeinstellungen deaktivieren.

<br />

<a name="faq25"></a>
**(25) Warum kann ich ein Bild, einen Anhang oder eine Datei nicht auswählen/öffnen/speichern?**

Wenn ein Menüpunkt zum Auswählen/Öffnen/Speichern einer Datei deaktiviert (abgeblendet) ist oder wenn Sie die Meldung *Speicherzugriffsframework nicht verfügbar* erhalten, ist wahrscheinlich das [Speicherzugriffs-Framework](https://developer.android.com/guide/topics/providers/document-provider), eine Standard-Android-Komponente, nicht vorhanden. Dies kann daran liegen, dass Ihr benutzerdefiniertes ROM es nicht enthält oder dass es aktiv entfernt wurde (debloated).

FairEmail fragt keine Speicherberechtigungen ab, so dass dieses Framework für die Auswahl von Dateien und Ordnern erforderlich ist. Keine App, außer vielleicht Dateimanager, die auf Android 4.4 KitKat oder höher abzielt, sollte nach Speicherberechtigungen fragen, da dies den Zugriff auf *alle* Dateien erlauben würde.

Das Framework für den Speicherzugriff wird durch das Paket *com.android.documentsui* bereitgestellt, das auf einigen Android-Versionen (vor allem OxygenOS) als *Files*-App sichtbar ist.

Mit diesem Adb-Befehl können Sie das Storage Access Framework (wieder) aktivieren:

```
pm install -k --user 0 com.android.documentsui
```

Alternativ können Sie die App *Dateien* auch über die Einstellungen der Android-App wieder aktivieren.

<br />

<a name="faq26"></a>
**(26) Kann ich helfen, FairEmail in meine eigene Sprache zu übersetzen?**

Ja, Sie können die Texte von FairEmail in Ihre eigene Sprache [auf Crowdin](https://crowdin.com/project/open-source-email) übersetzen. Die Registrierung ist kostenlos.

Wenn Sie möchten, dass Ihr Name oder Alias in die Liste der Mitwirkenden in *Über* der App aufgenommen wird, wenden Sie sich bitte [an mich](https://contact.faircode.eu/?product=fairemailsupport).

<br />

<a name="faq27"></a>
**(27) Wie kann ich zwischen eingebetteten und externen Bildern unterscheiden?**

Externes Bild:

![Externes Bild](https://github.com/M66B/FairEmail/blob/master/images/baseline_image_black_48dp.png)

Eingebettetes Bild:

![Eingebettetes Bild](https://github.com/M66B/FairEmail/blob/master/images/baseline_photo_library_black_48dp.png)

Defektes Bild:

![Defektes Bild](https://github.com/M66B/FairEmail/blob/master/images/baseline_broken_image_black_48dp.png)

Beachten Sie, dass das Herunterladen externer Bilder von einem entfernten Server verwendet werden kann, um eine Nachricht zu speichern, was Sie wahrscheinlich nicht wollen, wenn die Nachricht Spam oder bösartig ist.

<br />

<a name="faq28"></a>
**(28) Wie kann ich Statusleisten-Benachrichtigungen verwalten?**

In der Einrichtung finden Sie den Knopf *Benachrichtigungen verwalten*, um direkt zu den Android-Benachrichtigungseinstellungen für FairEmail zu navigieren.

Auf Android 8.0 Oreo und später können Sie die Eigenschaften der einzelnen Benachrichtigungskanäle verwalten, zum Beispiel, um einen bestimmten Benachrichtigungston zu setzen oder um Benachrichtigungen auf dem Sperrbildschirm anzuzeigen.

FairEmail hat folgende Benachrichtigungskanäle:

* Service: Wird für die Benachrichtigung des Synchronisationsdienstes verwendet, siehe auch [ diese häufig gestellten Fragen (FAQ)](#user-content-faq2)
* Send: Wird für die Benachrichtigung des Sendedienstes verwendet
* Notifications: Wird für neue Nachrichten verwendet
* Warning: Für Warnhinweise verwendet
* Error: Wird für Fehlerbenachrichtigungen verwendet

Siehe [hier](https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels) für Details zu den Benachrichtigungskanälen. Kurzum: Tippen Sie auf den Namen des Benachrichtigungskanals, um auf die Kanaleinstellungen zuzugreifen.

Auf Android vor Android 8 Oreo können Sie den Benachrichtigungston in den Einstellungen einstellen.

Sehen Sie sich [diese häufig gestellten Fragen (FAQ)](#user-content-faq21) an, wenn Ihr Gerät ein Benachrichtigungslicht hat.

<br />

<a name="faq29"></a>
**(29) Wie kann ich Benachrichtigungen über neue Nachrichten für andere Ordner erhalten?**

Drücke Sie einfach lange auf einen Ordner, wählen Sie *Eigenschaften bearbeiten*, und aktivieren Sie entweder *Im Gemeinsamen Posteingang anzeigen* oder *Neue Nachrichten benachrichtigen* (verfügbar nur für Android 7 Nougat und später) und tippen Sie auf *Speichern*.

<br />

<a name="faq30"></a>
**(30) Wie kann ich die angegebenen Schnelleinstellungen verwenden?**

Es stehen Schnelleinstellungen (im Einstellungs-Menü) zur Verfügung:

* global die Synchronisierung aktivieren/deaktivieren
* zeige die Anzahl neuer Nachrichten und markiere sie als gesehen (nicht gelesen)

Schnelleinstellungen erfordern Android 7.0 Nougat oder höher. Die Verwendung von Schnelleinstellungen wird hier [erklärt](https://support.google.com/android/answer/9083864).

<br />

<a name="faq31"></a>
**(31) Wie kann ich die angegebenen Verknüpfungen verwenden?**

Es stehen Verknüpfungen zur Verfügung, um eine neue Nachricht an einen bevorzugten Kontakt zu verfassen.

Verknüpfungen erfordern Android 7.1 Nougat oder höher. Die Verwendung von Verknüpfungen wird hier [erklärt.](https://support.google.com/android/answer/2781850).

Es ist auch möglich, Verknüpfungen zu Ordnern durch langes Drücken eines Ordners in der Ordnerliste eines Kontos zu erstellen und *Verknüpfung hinzufügen* auszuwählen.

<br />

<a name="faq32"></a>
**(32) Wie kann ich überprüfen, ob das Lesen von E-Mails wirklich sicher ist?**

Hierfür können Sie den [E-Mail Privacy Tester](https://www.emailprivacytester.com/) verwenden.

<br />

<a name="faq33"></a>
**(33) Warum funktionieren bearbeitete Absenderadressen nicht?**

Die meisten Anbieter akzeptieren nur validierte Adressen zum Versenden von Nachrichten, um Spam zu verhindern.

Zum Beispiel ändert Google die Nachrichtenheader wie diese für *nicht überprüfte* Adressen:

```
Von: Jemand <somebody@example.org>
X-Google-Original-Von: Jemand <somebody+extra@example.org>
```

Das bedeutet, dass die bearbeitete Absenderadresse vor dem Senden der Nachricht automatisch durch eine verifizierte Adresse ersetzt wird.

Beachten Sie bitte, das dies keinen Einfluss auf das Empfangen von Nachrichten hat.

<br />

<a name="faq34"></a>
**(34) Wie stimmen Identitäten überein?**

Identitäten werden nach den Anforderungen des Kontos angepasst. Für eingehende Nachrichten werden *an*, *cc*, *bcc*, *von* und *(X-)versendet/Umschlag/Original an* Adressen überprüft (in dieser Reihenfolge), für ausgehende Nachrichten (Entwürfe, Ausgang und Gesendet) werden nur die *von* Adressen überprüft. Gleiche Adressen haben Vorrang vor teilweise übereinstimmenden Adressen, mit Ausnahme von *zugestellt-an*-Adressen.

Die übereinstimmende Adresse wird als *versendet über* im Adressbereich der empfangenen Nachrichten angezeigt (zwischen der Kopfzeile und dem Nachrichtentext).

Beachten Sie, dass Identitäten aktiviert werden müssen, um damit übereinstimmen zu können, und dass Identitäten anderer Konten nicht berücksichtigt werden.

Das Anpassen erfolgt nur beim Empfang einer Nachricht, sodass das Ändern der Konfiguration bestehende Nachrichten nicht ändert. Sie könnten lokale Nachrichten löschen, indem Sie lange auf einen Ordner in der Ordnerliste drücken und die Nachrichten erneut synchronisieren.

Es ist möglich, einen [Regex](https://en.wikipedia.org/wiki/Regular_expression) (Regulärer Ausdruck) in den Identitätseinstellungen zu konfigurieren, um **den Benutzernamen** einer E-Mail-Adresse (den Teil vor dem @-Zeichen) abzugleichen.

Beachten Sie, dass der Domain-Name (die Teile nach dem @-Zeichen) immer gleich dem Domain-Namen der Identität sein muss.

Wenn SIe eine Catch-All-E-Mail-Adresse eintragen möchten, ist dieser Regex meistens in Ordnung:

```
.*
```

Wenn SIe E-Mail Adressen für spezielle Zwecke (z.B. abc@example.com und xyz@example.com) eintragen möchten, aber zusätzlich eine Reserve-Adresse wie main@example.com, sollten sie diese Schritte befolgen:

* Identität: abc@example.com; Regex: **(?i)abc**
* Identität: xyz@example.com; Regex: **(?i)xyz**
* Identität: main@example.com; Regex: **^(?i)((?!abc|xyz).)\*$**

Passende Identitäten können verwendet werden, um Code-Nachrichten einzufärben. Identitätsfarben haben Vorrang vor Ordner- und Kontofarben. Das Festlegen von Identitätsfarben ist ein Pro-Funktion.

<br />

<a name="faq35"></a>
**(35) Warum sollte ich bei Ansehen von Bildern, Anhängen und der Original-Nachricht vorsichtig sein?**

Beim Anschauen von in der Ferne gespeicherten Bildern (siehe auch [diese FAQ](#user-content-faq27)) kann der Absender nicht nur wissen, dass Sie die Nachricht gesehen haben, er wird auch Ihre IP-Adresse kennen. Siehe auch diese Frage: [Warum ist der Link von E-Mails gefährlicher als der Link der Websuche?](https://security.stackexchange.com/questions/241139/why-emails-link-is-more-dangerous-than-web-searchs-link).

Das Öffnen von Anhängen oder das Betrachten einer Originalnachricht kann entfernte Inhalte laden und Skripte ausführen, die nicht nur vertrauliche Informationen verbreiten könnten, sondern auch ein Sicherheitsrisiko darstellen können.

Beachten Sie, dass Ihre Kontakte unwissentlich bösartige Nachrichten senden könnten, wenn sie mit Malware infiziert sind.

FairEmail formatiert Nachrichten wieder und lässt Nachrichten anders aussehen als das Original, aber deckt auch Phishing-Links auf.

Beachten Sie, dass neu formatierte Nachrichten oft besser lesbar sind als ursprüngliche Nachrichten, da die Ränder entfernt und Schriftfarben und -größen standardisiert werden.

Die Google Mail-App zeigt standardmäßig Bilder an, indem sie die Bilder über einen Google-Proxy-Server herunterlädt. Da die Bilder vom Quellserver [in Echtzeit](https://blog.filippo.io/how-the-new-gmail-image-proxy-works-and-what-this-means-for-you/) heruntergeladen werden, ist dies ist sogar noch weniger sicher, da Google auch ohne großen Nutzen beteiligt ist.

Sie können Bilder und Originalnachrichten standardmäßig für vertrauenswürdige Absender von Fall zu Fall anzeigen, indem Sie *Nicht erneut danach fragen für ...* im Dialog ankreuzen.

Wenn Sie die Standard *Öffnen mit*-Apps zurücksetzen möchten, lesen Sie bitte [diese FAQ](https://www.androidauthority.com/how-to-set-default-apps-android-clear-621269/).

<br />

<a name="faq36"></a>
**(36) Wie werden Einstellungsdateien verschlüsselt?**

Kurze Version: AES 256 Bit

Lange Version:

* Der 256-Bit-Schlüssel wird mit *PBKDF2WithHmacSHA1* erstellt, durch ein 128 Bit sicheres random salt und 65536 Wiederholungen
* Die Verschlüsselung ist *AES/CBC/PKCS5Padding*

<br />

<a name="faq37"></a>
**(37) Wie werden Passwörter gespeichert?**

Alle unterstützten Android-Versionen [verschlüsseln alle Benutzerdaten](https://source.android.com/security/encryption), so dass alle Daten, einschließlich Benutzernamen, Passwörter, Nachrichten usw., verschlüsselt gespeichert werden.

Wenn das Gerät mit einer PIN, einem Muster oder einem Passwort gesichert ist, können Sie das Konto und die Identitätskennwörter sichtbar machen. Wenn dies ein Problem ist, weil Sie das Gerät mit anderen Personen teilen, erwägen die Verwendung von [Benutzerprofilen](https://www.howtogeek.com/333484/how-to-set-up-multiple-user-profiles-on-android/).

<br />

<a name="faq39"></a>
**(39) Wie kann ich den Akkuverbrauch von FairEmail verringern?**

Aktuelle Android-Versionen melden die *App-Nutzung* standardmäßig als Prozentsatz in den Android-Akkueinstellungen. **Verwirrenderweise ist die *App-Nutzung* nicht identisch mit dem *Akkuverbrauch* und steht nicht einmal im direkten Zusammenhang mit dem Akkuverbrauch!** Die App-Nutzung (während der Verwendung) ist sehr hoch, da FairEmail einen Vordergrunddienst nutzt, der von Android als konstante App-Nutzung angesehen wird. Dies bedeutet jedoch nicht, dass FairEmail ständig Akkuleistung verbraucht. Der tatsächliche Akkuverbrauch kann auf folgendem Bildschirm gesehen werden:

*Android-Einstellungen* → *Akku* → Drei-Punkte-Menü → *Akkunutzung* → Drei-Punkte-Menü → *Geräteverbrauch anzeigen*

In der Regel sollte der Akkuverbrauch kleiner oder in jedem Fall nicht viel höher sein als *Mobilfunknetz-Standby*. Wenn dies nicht der Fall ist, aktivieren Sie bitte *»Automatische Optimierung«* in den Empfangseinstellungen. Wenn das nicht hilft, bitten [fragen Sie nach Unterstützung](https://contact.faircode.eu/?product=fairemailsupport).

Es ist unvermeidlich, dass das Synchronisieren von Nachrichten Akkustrom benötigt, da es Netzwerkzugriff und Zugriff auf die Nachrichtendatenbank erfordert.

Wenn Sie den Akkuverbrauch von FairEmail mit einem anderen E-Mail-Client vergleichen, stellen Sie bitte sicher, dass der andere E-Mail-Client ähnlich eingerichtet ist. Zum Beispiel ist es kein fairer Vergleich, stetige Synchronisation (Push-Nachrichten) und (seltenere) periodische Überprüfungen nach neuen Nachrichten zu vergleichen.

Die Wiederverbindung zu einem E-Mail-Server verbraucht zusätzliche Akkuleistung, so dass eine instabile Internetverbindung einen zusätzlichen Akkuverbrauch zur Folge hat. Auch einige E-Mail-Server beenden vorzeitig Leerlaufverbindungen, während [der Standard](https://tools.ietf.org/html/rfc2177) sagt, dass eine Leerlaufverbindung für 29 Minuten offen gehalten werden sollte. In diesen Fällen möchten Sie vielleicht periodisch synchronisieren, zum Beispiel jede Stunde, anstatt ständig zu synchronisieren. Beachten Sie, dass das häufige periodische Abfragen (mehr als alle 30-60 Minuten) wahrscheinlich mehr Akkuleistung als die ständige Synchronisierung verbrauchen wird, da eine Verbindung zum Server und der Vergleich der lokalen und entfernten Nachrichten aufwändig sind.

[Bei einigen Geräten](https://dontkillmyapp.com/) ist es notwendig, die Akku-Optimierung *auszuschalten* (im Einrichtungs-Menü Schritt 3), um eine Verbindung zu Mail-Servern ständig offen zu halten. Wenn man die Batterieoptimierung aktiviert, kann dies zu einem zusätzlichen Batterieverbrauch für alle Geräte führen, auch wenn dies widersprüchlich klingt!

Der größte Teil des Akkuverbrauchs, nicht berücksichtigt das Anzeigen von Nachrichten, ist auf die Synchronisierung (Empfangen und Senden) von Nachrichten zurückzuführen. Um den Akkuverbrauch zu verringern, setzen Sie die Anzahl der Tage, um die Nachricht zu synchronisieren, auf einen niedrigeren Wert, besonders wenn es viele neue Nachrichten in einem Ordner gibt. Drücken Sie lange auf einen Ordnernamen in der Ordnerliste und wählen Sie *Eigenschaften bearbeiten* um auf diese Einstellung zuzugreifen.

Wenn Sie mindestens einmal am Tag über eine Internetverbindung verfügen, reicht es aus, Nachrichten nur für einen Tag zu synchronisieren.

Beachten Sie, dass Sie die Anzahl der Tage zum *Halten* von Nachrichten auf eine höhere Zahl setzen können, als die Zahl der Tage, *für die Nachrichten synchronisiert* werden sollen. Sie könnten z. B. zunächst Nachrichten für eine große Anzahl von Tagen synchronisieren und danach die Anzahl der Tage zum Synchronisieren von Nachrichten reduzieren, aber die Anzahl der Tage zum Aufbewahren von Nachrichten belassen. Nachdem Sie die Anzahl der Tage verringert haben, die Nachrichten aufbewahrt werden sollen, sollten Sie die Bereinigung in den verschiedenen Einstellungen ausführen, um alte Dateien zu entfernen.

In den Empfangseinstellungen können Sie aktivieren, dass markierte Nachrichten immer synchronisiert werden sollen, was auch ältere (markierte) Nachrichten behält, während alle anderen Nachrichten nur für eine begrenzten Anzahl von Tagen synchronisiert werden.

Deaktivieren der Ordneroption *Automatisch Nachrichtentexte und Anhänge herunterzuladen* führt zu weniger Netzwerkverkehr und somit weniger Akkuverbrauch. Sie können diese Option zum Beispiel für den Ordner mit gesendeten Nachrichten und das Archiv deaktivieren.

Das Synchronisieren von Nachrichten in der Nacht ist meist nicht nützlich, daher können Sie beim Batterieverbrauch sparen, indem Sie nicht in der Nacht synchronisieren. In den Einstellungen können Sie einen Zeitplan für die Synchronisation von Nachrichten auswählen (dies ist eine Pro-Funktion).

FairEmail wird standardmäßig die Ordnerliste bei jeder Verbindung synchronisieren. Da Ordner nicht häufig neu erstellt, umbenannt oder gelöscht werden, können Sie einen bestimmten Netzwerk- und Akkuverbrauch sparen, indem Sie dies in den Empfangseinstellungen deaktivieren.

FairEmail prüft standardmäßig, bei jeder Verbindung, ob alte Nachrichten vom Server gelöscht wurden. Wenn Sie nichts dagegen haben, dass alte Nachrichten, die vom Server gelöscht wurden, in FairEmail immer noch sichtbar sind, können Sie einen bestimmten Netzwerk- und Akkuverbrauch sparen, indem Sie dies in den Empfangseinstellungen deaktivieren.

Einige Anbieter folgen nicht dem IMAP-Standard und halten die Verbindungen nicht lange genug offen, was FairEmail zwingt, häufig wieder neu zu verbinden und dadurch einen zusätzlichen Akkuverbrauch zu verursachen. Sie können das *Log* über das Hauptmenü überprüfen, ob es häufige Neu-Verbindungen gibt (Verbindung geschlossen/zurücksetzen, Lese-/Schreib-Fehler/Timeout usw.). Sie können dies umgehen, indem Sie in den erweiterten Kontoeinstellungen das Intervall zum Offenhalten der Verbindung auf z.B. 9 oder 15 Minuten senken. Beachten Sie, dass die Batterieoptimierungen im Setup-Schritt 3 deaktiviert werden müssen, um die Verbindungen zuverlässig zu erhalten.

Einige Anbieter senden alle zwei Minuten so etwas wie '*noch hier*' , was zu Netzwerkverkehr auf Ihrem Gerät führt und unnötigen Batterieverbrauch verursacht. Sie können im *Log* über das Hauptmenü überprüfen, ob Ihr Anbieter dies tut. Wenn Ihr Provider [Dovecot](https://www.dovecot.org/) als IMAP-Server verwendet, könnten Sie Ihren Provider bitten, die Einstellung [imap_idle_notify_interval](https://wiki.dovecot.org/Timeouts) auf einen höheren Wert zu ändern oder besser noch, es ganz zu deaktivieren. Wenn Ihr Provider nicht in der Lage oder willens ist, dies zu ändern/zu deaktivieren, sollten Sie eine Synchronisierung in regelmäßigen Abständen anstelle einer kontinuierlichen Synchronisierung in Betracht ziehen. Sie können dies in den Empfangseinstellungen ändern.

Wenn Sie die Nachricht erhalten haben *Dieser Anbieter unterstützt keine Push-Nachrichten* während der Kontenkonfiguration, erwägen Sie den Wechsel zu einem modernen Provider, der Push-Nachrichten (IMAP IDLE) unterstützt, um den Akkuverbrauch zu verringern.

Wenn Ihr Gerät einen [AMOLED](https://en.wikipedia.org/wiki/AMOLED) Bildschirm besitzt, können Sie Akkuverbrauch während der Anzeige der Nachrichten durch den Wechsel zum schwarzen Theme sparen.

Wenn automatische Optimierung in den Empfangseinstellungen aktiviert ist, wird ein Konto automatisch auf periodische Überprüfungen nach neuen Nachrichten umgestellt, wenn der E-Mail-Server:

* Sagt '*noch hier*' innerhalb von 3 Minuten
* Der E-Mail-Server keine Push-Nachrichten unterstützt
* Das Intervall zum Offenhalten einer Verbindung kleiner als 12 Minuten ist

Zusätzlich werden der Papierkorb und der Spam-Ordner automatisch auf ein entsprechendes Intervall zum Prüfen auf neue Nachrichten gesetzt nach drei aufeinanderfolgenden Fehlern [zu viele gleichzeitigen Verbindungen](#user-content-faq23).

<br />

<a name="faq40"></a>
**(40) Wie kann ich die Datennutzung von FairEmail reduzieren?**

Sie können den Datenverbrauch grundsätzlich auf die gleiche Weise reduzieren wie den Batterieverbrauch, lesen Sie die vorherige Frage für Vorschläge.

Es ist unvermeidlich, dass Daten verwendet werden, um Nachrichten zu synchronisieren.

Wenn die Verbindung zum E-Mail-Server verloren geht, wird FairEmail die Nachrichten immer wieder synchronisieren, um sicherzustellen, dass keine Nachrichten verpasst wurden. Wenn die Verbindung instabil ist, kann dies zu einer zusätzlichen Datennutzung führen. In diesem Fall ist es ratsam, die Anzahl der Tage für die Synchronisierung von Nachrichten auf ein Minimum zu reduzieren (siehe vorherige Frage) oder auf periodische Synchronisation der Nachrichten zu wechseln (Empfangseinstellungen).

Um den Datenverbrauch zu verringern, können Sie diese erweiterten Empfangseinstellungen ändern:

* Überprüfen, ob alte Nachrichten vom Server entfernt wurden
* (freigegebene) Ordnerliste synchronisieren: deaktivieren

Standardmäßig lädt FairEmail keine Nachrichtentexte und Anhänge, die größer als 256 KiB sind, wenn eine kostenpflichtige Internetverbindung (mobile oder gebührenpflichtige WLAN) vorhanden ist. Sie können dies in den Verbindungseinstellungen ändern.

<br />

<a name="faq41"></a>
**(41) Wie kann ich den Fehler 'Handshake fehlgeschlagen' beheben?**

Es gibt mehrere mögliche Ursachen, also lesen Sie bitte bis zum Ende dieser Antwort.

Der Fehler '*Handshake fehlgeschlagen ... WRONG_VERSION_NUMBER ...*' könnte bedeuten, dass Sie versuchen, eine Verbindung zu einem IMAP oder SMTP Server ohne verschlüsselte Verbindung herzustellen, typischerweise unter Verwendung von Port 143 (IMAP) und Port 25 (SMTP); oder dass ein falsches Protokoll (SSL/TLS oder STARTTLS) verwendet wird.

Die meisten Anbieter bieten verschlüsselte Verbindungen über verschiedene Ports, typischerweise Port 993 (IMAP) und Port 465/587 (SMTP).

Falls Ihr Provider keine verschlüsselten Verbindungen unterstützt, sollten Sie darum bitten, dies zu ermöglichen. Wenn dies keine Option ist, können Sie *unsichere Verbindungen zulassen* sowohl in den erweiterten Einstellungen UND den Konto/Identitätseinstellungen aktivieren.

Siehe auch [diese häufig gestellten Fragen (FAQ)](#user-content-faq4).

Der Fehler '*Handshake fehlgeschlagen ... SSLV3_ALERT_ILLEGAL_PARAMETER ...*' wird entweder durch einen Fehler in der SSL-Protokoll-Implementierung oder durch einen zu kurzen DH-Schlüssel auf dem E-Mail-Server verursacht und kann leider nicht durch FairEmail behoben werden.

Der Fehler '*Handshake fehlgeschlagen ... HANDSHAKE_FAILURE_ON_CLIENT_HELLO ...*' könnte durch den Provider verursacht werden, der immer noch RC4 verwendet, das seit [Android 7](https://developer.android.com/about/versions/nougat/android-7.0-changes.html#tls-ssl) nicht mehr unterstützt wird.

Der Fehler '*Handshake fehlgeschlagen ... UNSUPPORTED_PROTOCOL oder TLSV1_ALERT_PROTOCOL_VERSION ...*' kann durch das Aktivieren von abgehärteten Verbindungen in den Verbindungseinstellungen verursacht werden; oder durch Android, das ältere Protokolle, wie SSLv3, nicht mehr unterstützt.

Android 8 Oreo und später [unterstützen](https://developer.android.com/about/versions/oreo/android-8.0-changes#security-all) SSLv3 nicht mehr. Es gibt keine Möglichkeit, das Problem ohne Unterstützung von RC4 und SSLv3 zu umgehen, da es komplett aus Android entfernt wurde (was etwas heißen soll).

Sie können [diese Website](https://ssl-tools.net/mailservers) oder [diese Website](https://www.immuniweb.com/ssl/) verwenden, um auf SSL/TLS-Probleme von E-Mail-Servern zu überprüfen.

<br />

<a name="faq42"></a>
**(42) Könne Sie einen neuen Anbieter zur Liste der Anbieter hinzufügen?**

Wenn der Provider von mehr als ein paar Leuten benutzt wird, ja, mit Freude.

Folgende Informationen werden benötigt:

```
<provider
    name="Gmail"
    link="https://support.google.com/mail/answer/7126229" // link to the instructions of the provider
    type="com.google"> // das wird nicht benötigt
    <imap
        host="imap.gmail.com"
        port="993"
        starttls="false" />
    <smtp
        host="smtp.gmail.com"
        port="465"
        starttls="false" />
</provider>
```

Das EFF [schreibt](https://www.eff.org/nl/deeplinks/2018/06/announcing-starttls-everywhere-securing-hop-hop-email-delivery): "*Zusätzlich, auch wenn Sie STARTTLS perfekt konfigurieren und ein gültiges Zertifikat verwenden, gibt es noch keine Garantie dafür, dass Ihre Kommunikation verschlüsselt wird.*"

Also sind reine SSL-Verbindungen sicherer als die Verwendung von [STARTTLS](https://en.wikipedia.org/wiki/Opportunistic_TLS) und daher bevorzugt.

Bitte stellen Sie sicher, dass das Empfangen und Senden von Nachrichten richtig funktioniert, bevor Sie mich kontaktieren, um einen Anbieter hinzuzufügen.

Sehen Sie weiter unten, wie Sie mich kontaktieren können.

<br />

<a name="faq43"></a>
**(43) Kann das Original angezeigt werden?**

»Original anzeigen« zeigt die Originalnachricht, wie der Absender sie gesendet hat, einschließlich Originalschriften, -farben, -ränder usw. Fair E-Mail ändert dies nicht ab und wird es auch nicht auf irgendeine Weise tun, außer, um [TEXT_AUTOSIZING](https://developer.android.com/reference/android/webkit/WebSettings.LayoutAlgorithm) zu erfragen, welches *versuchen wird*, kleinen Text besser lesbar zu machen.

<br />

<a name="faq44"></a>
**~~(44) Können Sie Kontaktfotos / Identicons im Gesendet-Ordner anzeigen?~~**

~~Kontaktfotos und Identicons werden immer für den Absender angezeigt, da dies für Unterhaltungshinweise notwendig ist.~~ ~~Kontaktfotos für Absender und Empfänger zu erhalten ist keine wirkliche Option, da das Kontakt-Foto eine teure Operation ist.~~

<br />

<a name="faq45"></a>
**(45) Wie kann ich »Dieser Schlüssel ist nicht verfügbar. Um es zu verwenden, müssen Sie es selbst importieren!« ?**

Sie werden eine Nachricht *»Dieser Schlüssel ist nicht verfügbar. Um es zu verwenden, müssen Sie es als einen Ihrer eigenen importieren!«* erhalten. Wenn Sie versuchen, eine Nachricht mit einem öffentlichen Schlüssel zu entschlüsseln. Um es zu beheben, müssen Sie den privaten Schlüssel importieren.

<br />

<a name="faq46"></a>
**(46) Warum wird die Nachrichtenliste immer aktualisiert?**

Wenn Sie einen 'Spinner' oben auf der Nachrichtenliste sehen, wird der Ordner noch immer mit dem entfernten Server synchronisiert. Sie können den Fortschritt der Synchronisation in der Ordnerliste sehen. Siehe die Legende darüber, was die Symbole und Zahlen bedeuten.

Die Geschwindigkeit Ihres Geräts und Ihrer Internetverbindung sowie die Anzahl der Tage für die Synchronisierung von Nachrichten bestimmen, wie lange der Synchronisierungsvorgang dauern wird. Beachten Sie, dass Sie die Anzahl der Tage zum Synchronisieren von Nachrichten in den meisten Fällen nicht auf mehr als einen Tag einstellen sollten, siehe auch [diese FAQ](#user-content-faq39).

<br />

<a name="faq47"></a>
(47) Wie löse ich den Fehler »Kein primäres Konto oder kein Ordner für Entwürfe« ?

Sie erhalten die Fehlermeldung *Kein Primärkonto oder kein Entwürfe-Ordner* beim Versuch, eine Nachricht zu verfassen, während kein Konto als Hauptkonto gesetzt ist oder wenn kein Entwürfe-Ordner für das Hauptkonto ausgewählt ist. Dies kann zum Beispiel passieren, wenn Sie FairEmail starten, um eine Nachricht aus einer anderen App zu erstellen. FairEmail muss wissen, wo der Entwurf gespeichert werden soll, daher müssen Sie ein Konto als Hauptkonto und/oder einen Entwürfe-Ordner für das Hauptkonto auswählen.

Dies kann auch passieren, wenn Sie versuchen, auf eine Nachricht zu antworten oder eine Nachricht von einem Konto ohne Entwürfe-Ordner weiterzuleiten, wenn es kein Hauptkonto gibt oder wenn das Hauptkonto keinen Entwürfe-Ordner hat.

Siehe [diese häufig gestellten Fragen (FAQ)](#user-content-faq141) für mehr Informationen.

<br />

<a name="faq48"></a>
**~~(48) Wie löse ich den Fehler »Kein primäres Konto oder kein Archivordner«?~~**

~~Sie erhalten die Fehlermeldung *Kein primäres Konto oder kein Archivordner*, wenn sie nach Nachrichten von einer anderen App aus suchen. FairEmail muss wissen, wo Sie suchen wollen, daher müssen Sie ein Konto als Hauptkonto festlegen und/oder einen Archivordner für das Hauptkonto auswählen.~~

<br />

<a name="faq49"></a>
**(49) Wie behebe ich 'Eine veraltete App hat einen Dateipfad anstelle eines Datei-Streams gesendet' ?**

Sie haben wahrscheinlich einen Anhang oder ein Bild mit einem veralteten Dateimanager oder einer veralteten App ausgewählt, die davon ausgeht, dass alle Apps noch Speicherrechte haben. Aus Sicherheits- und Datenschutzgründen haben moderne Apps wie FairEmail keinen vollen Zugriff auf alle Dateien mehr. Dies kann zu der Fehlermeldung *Eine veraltete App hat einen Dateipfad anstelle eines Dateistroms gesendet* führen, wenn ein Dateiname statt eines Datei-Streams mit FairEmail geteilt wird, da FairEmail keine Dateien zufällig öffnen kann.

Sie können dies beheben, indem Sie zu einem aktuellen Dateimanager oder einer App wechseln, die für aktuelle Android-Versionen entwickelt wurde. Alternativ können Sie FairEmail Lesezugriff auf den Speicherplatz ihres Geräts in den Android-App-Einstellungen gewähren. Beachten Sie, dass diese Lösung [nicht auf Android Q](https://developer.android.com/preview/privacy/scoped-storage) funktioniert.

Siehe auch [Frage 25](#user-content-faq25) und [was Google dazu schreibt](https://developer.android.com/training/secure-file-sharing/share-file#RespondToRequest).

<br />

<a name="faq50"></a>
**(50) Kannst du eine Option hinzufügen, um alle Nachrichten zu synchronisieren?**

Sie können mehr oder sogar alle Nachrichten durch langes Drücken eines Ordners (Posteingang) in der Ordnerliste eines Kontos synchronisieren. Tippen Sie auf den Kontonamen im Navigationsmenü und wählen *Mehr synchronisieren* im Kontextmenü.

<br />

<a name="faq51"></a>
**(51) Wie werden Ordner sortiert?**

Ordner werden zuerst nach der Reihenfolge der Accounts sortiert (standardmäßig nach Name), und innerhalb eines Accounts befinden sich ganz oben Systemordner, gefolgt von Ordnern nit aktivierter Synchronisierung. In jeder Kategorie werden die Ordner nach ihren (Anzeige-) Namen sortiert. Sie können den Anzeigenamen durch langes Drücken eines Ordners in der Ordnerliste festlegen und *Eigenschaften bearbeiten* auswählen.

Die Navigation (Hamburger) im *Ordner-Menü* in den Einstellungen kann verwendet werden, um die Ordner manuell zu sortieren.

<br />

<a name="faq52"></a>
**(52) Warum braucht es einige Zeit, um sich wieder mit einem Konto zu verbinden?**

Es gibt keinen zuverlässigen Weg, um zu erfahren, ob eine Verbindung zu einem Konto ordentlich oder außerordentlich beendet wurde. Der Versuch, eine Verbindung zu einem Konto wiederherzustellen, während die Verbindung zu einem Konto erzwungen wird, kann zu Problemen wie [zu viele gleichzeitige Verbindungen](#user-content-faq23) oder sogar einem gesperrten Konto führen. Um solche Probleme zu vermeiden, wartet FairEmail 90 Sekunden, bis versucht wird, sich erneut zu verbinden.

Sie können *Einstellungen* im Navigationsmenü lange drücken, um sofort wieder zu verbinden.

<br />

<a name="faq53"></a>
**(53) Kannst du die Benachrichtigungsleiste nach oben/unten setzen?**

Die Message-Aktionsleiste funktioniert auf einer einzigen Nachricht und die untere Aktionsleiste wirkt auf alle Nachrichten in der Unterhaltung. Da es oft mehr als eine Nachricht in einem Gespräch gibt, ist dies nicht möglich. Darüber hinaus gibt es ganz bestimmte Aktionen, die nur für einzelne Nachrichten wirken sollen, wie z.B. die Weiterleitung.

Das Verschieben der Nachrichten-Aktionsleiste an den unteren Rand der Nachricht ist visuell nicht ansprechend, da es bereits eine Aktionsleiste für die gesamte Unterhaltung am unteren Rand des Bildschirms gibt.

Beachten Sie, dass es nicht viele E-Mail-Apps gibt, die eine Unterhaltung als Liste erweiterbarer Nachrichten anzeigen. Das hat viele Vorteile, aber auch die Notwendigkeit von separaten Maßnahmen für einzelne Nachrichten.

<br />

<a name="faq54"></a>
**~~(54) Wie verwende ich einen Namespace Präfix?~~**

~~Ein Namespace-Präfix wird verwendet, um die Präfixe, die manche Anbieter automatisch setzen, wieder zu entfernen.~~

~~Zum Beispiel wird der Gmail-Spam-Ordner genannt:~~

```
[Gmail]/Spam
```

~~Durch das Setzen des Namensraum-Präfix auf *[Gmail]* wird FairEmail automatisch *[Gmail]/* von allen Ordnernamen entfernen.~~

<br />

<a name="faq55"></a>
**(55) Wie kann ich alle Nachrichten als gelesen markieren / verschieben oder alle Nachrichten löschen?**

Sie können dafür die Mehrfachauswahl verwenden. Drücken Sie lange auf die erste Nachricht, heben Sie nicht den Finger und gleiten Sie nach unten zur letzten Nachricht. Dann benutzen Sie die Drei-Punkt-Schaltfläche um die gewünschte Aktion auszuführen.

<br />

<a name="faq56"></a>
**(56) Kannst du Unterstützung für JMAP hinzufügen?**

Es gibt fast keine Anbieter, die das [JMAP](https://jmap.io/) Protokoll anbieten, damit ist es nicht viel Mühe wert, in FairEmail Support dafür hinzuzufügen.

<br />

<a name="faq57"></a>
**(57) Kann ich HTML in Signaturen verwenden?**

Ja, Sie können [HTML](https://en.wikipedia.org/wiki/HTML) verwenden. Im Signatur-Editor können Sie über das Drei-Punkte-Menü in den HTML-Modus wechseln.

Beachten Sie, dass, wenn Sie zurück zum Texteditor wechseln, nicht alle HTML-Befehle so gerendert werden könnten, wie es ist, weil der Android Texteditor nicht in der Lage ist, alle HTML-Befehle zu rendern. Ebenso könnte das HTML bei Verwendung des Texteditors auf unerwartete Art und Weise verändert werden.

Wenn Sie vorformatierten Text verwenden möchten, wie [ASCII Art](https://en.wikipedia.org/wiki/ASCII_art), Sie sollten den Text in ein *pre* Element einbinden, wie dies:

```
<pre>
  |\_/|
 / @ @ \
( > o < )
 `>>x<<<unk>
 / O \
 </pre>
```

<br />

<a name="faq58"></a>
**(58) Was bedeutet ein geöffnet/geschlossenes E-Mail-Icon?**

Das E-Mail-Symbol in der Ordnerliste kann geöffnet (umrissen) oder geschlossen sein (gefüllt):

![Externes Bild](https://github.com/M66B/FairEmail/blob/master/images/baseline_mail_outline_black_48dp.png)

Nachrichtentexte und Anhänge werden standardmäßig nicht heruntergeladen.

![Externes Bild](https://github.com/M66B/FairEmail/blob/master/images/baseline_email_black_48dp.png)

Nachrichtentexte und Anhänge werden standardmäßig heruntergeladen.

<br />

<a name="faq59"></a>
**(59) Kann man Originalnachrichten im Browser öffnen?**

Aus Sicherheitsgründen sind die Dateien mit den Originaltexten für andere Apps nicht zugänglich, so dass dies nicht möglich ist. Theoretisch könnte das [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) verwendet werden, um diese Dateien freizugeben. Aber selbst Google Chrome kann dies nicht tun.

<br />

<a name="faq60"></a>
**(60) Wussten Sie ... ?**

* Wussten Sie, dass markierte Nachrichten immer synchronisiert/gespeichert werden können? (dies kann in den Empfangseinstellungen aktiviert werden)
* Wussten Sie, dass Sie das Symbol "Nachricht schreiben" lange drücken können, um in den Ordner "Entwürfe" zu wechseln?
* Wussten Sie, dass es eine erweiterte Option gibt, Nachrichten automatisch als gelesen zu markieren, wenn sie verschoben werden? (Archivieren und wegwerfen sind auch Verschiebungen)
* Wussten Sie, dass Sie Text (oder eine E-Mail-Adresse) in jeder App für die neuesten Android-Versionen auswählen kannst und FairEmail nach ihm suchen lassen kannst?
* Wussten Sie, dass FairEmail einen Tablet-Modus hat? Drehen Sie Ihr Gerät ins Querformat und Gesprächsthreads werden in einer zweiten Spalte geöffnet, wenn genügend Bildschirmplatz vorhanden ist.
* Wussten Sie, dass Sie eine Antwortvorlage lange drücken können, um aus der Vorlage einen Entwurf zu erstellen?
* Wussten Sie, dass Sie lange drücken, halten und wischen können, um eine Reihe von Nachrichten auszuwählen?
* Wussten Sie, dass man erneut versuchen kann, Nachrichten zu versenden, indem man in der Outbox herunterzieht?
* Wussten Sie, dass Sie eine Unterhaltung links oder rechts wischen können, um zur nächsten oder vorherigen Unterhaltung zu gehen?
* Wusstest du, dass du ein Bild antippen kannst, um zu sehen, woher es heruntergeladen wird?
* Wussten Sie, dass Sie das Ordnersymbol in der Aktionsleiste lange drücken können, um ein Konto auszuwählen?
* Wussten Sie, dass Sie das Sternsymbol in einem Gespräch lange drücken können, um einen farbigen Stern zu setzen?
* Wussten Sie, dass Sie das Navigationsmenü öffnen können, indem Sie von links wischen, auch wenn Sie gerade eine Unterhaltung ansehen?
* Wusste Sie, dass Sie das Personen-Symbol lange drücken können, um die CC/BCC-Felder anzuzeigen/zu verstecken und deren Sichtbarkeitsstatus fürs nächste Mal zu speichern?
* Wussten Sie, dass Sie die E-Mail-Adressen einer Android-Kontaktgruppe über das 3-Punkte-Überlaufmenü einfügen können?
* Wussten Sie, dass, wenn Sie Text auswählen und danach auf antworten drücken, nur der ausgewählte Text zitiert wird?
* Wussten Sie, dass Sie die Papierkorbsymbole (sowohl in der Nachricht als auch in der unteren Aktionsleiste) lange drücken können, um eine Nachricht oder Unterhaltung dauerhaft zu löschen? (Version 1.1368+)
* Wussten Sie, dass Sie die Sende-Aktion lange halten können, um den Sende-Dialog anzuzeigen, auch wenn er deaktiviert wurde?
* Wussten Sie, dass Sie das Vollbildsymbol lange drücken können, um nur den Originaltext (ohne den Nachrichtenkopf) anzuzeigen?
* Wussten Sie, dass Sie die Schaltfläche „Antworten” lange gedrückt halten können, um dem Absender zu antworten? (seit Version 1.1562)

<br />

<a name="faq61"></a>
**(61) Warum werden einige Nachrichten verdunkelt angezeigt?**

Die verdunkelt angezeigten Nachrichten (grau) sind lokal verschobene Nachrichten, für die der Umzug noch nicht vom Server bestätigt ist. Dies kann passieren, wenn aktuell keine Verbindung zum Server oder zum Konto besteht. Diese Nachrichten werden nach einer Verbindung zum Server synchronisiert oder wenn dies nie geschieht, weerden sie gelöscht, wenn sie zu alt sind, um synchronisiert zu werden.

Möglicherweise müssen Sie den Ordner manuell synchronisieren, indem Sie zum Beispiel nach unten ziehen.

Sie können diese Nachrichten ansehen, aber Sie können diese Nachrichten nicht erneut verschieben, bis der vorherige Schritt vom Server bestätigt wurde.

Ausstehende [Vorgänge](#user-content-faq3) können angesehen werden in der Vorgangs-Ansicht, die vom Haupt-Menü aus zugänglich ist.

<br />

<a name="faq62"></a>
**(62) Welche Authentifizierungsmethoden werden unterstützt?**

Die folgenden Authentifizierungsmethoden werden unterstützt und in dieser Reihenfolge verwendet:

* CRAM-MD5
* ANMELDEN
* KLARTEXT
* NTLM (nicht überprüft)
* XOAUTH2 ([Gmail](https://developers.google.com/gmail/imap/xoauth2-protocol), [Yandex](https://tech.yandex.com/oauth/))

SASL-Authentifizierungsmethoden außer CRAM-MD5 werden nicht unterstützt, weil [JavaMail für Android](https://javaee.github.io/javamail/Android) keine SASL-Authentifizierung unterstützt.

Wenn Ihr Provider eine nicht unterstützte Authentifizierungsmethode benötigt, erhalten Sie wahrscheinlich die Fehlermeldung *Authentifizierung fehlgeschlagen*.

[Client-Zertifikate](https://en.wikipedia.org/wiki/Client_certificate) können in den Konto- und Identitätseinstellungen ausgewählt werden.

[Server Name Indication](https://en.wikipedia.org/wiki/Server_Name_Indication) wird von [allen Android-Versionen, die das beherrschen](https://developer.android.com/training/articles/security-ssl) unterstützt.

<br />

<a name="faq63"></a>
**(63) Wie werden Bilder für die Anzeige auf den Bildschirmen skaliert?**

Große Inline oder angehängte [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) und [JPEG](https://en.wikipedia.org/wiki/JPEG) Bilder werden automatisch für die Anzeige auf den Bildschirmen verändert. Das liegt daran, dass E-Mail-Nachrichten in der Größe begrenzt sind, abhängig vom Anbieter meist zwischen 10 und 50 MB. Bilder werden standardmäßig auf eine maximale Breite und Höhe von etwa 1440 Pixeln verkleinert und mit einer Kompressionsrate von 90 % gespeichert. Bilder werden unter Verwendung ganzer Zahlenfaktoren herunterskaliert, um die Speicherauslastung zu reduzieren und die Bildqualität zu erhalten. Die automatische Gößenänderung von Inline-Bildern und/oder angehängten Bildern und die maximale Zielbild-Größe können in den Sendeeinstellungen eingestellt werden.

Wenn Sie die Größe der Bilder von Fall zu Fall ändern möchten, können Sie [Ermäßigt senden](https://f-droid.org/en/packages/mobi.omegacentauri.SendReduced/) oder eine ähnliche App verwenden.

<br />

<a name="faq64"></a>
**~~(64) Kannst du benutzerdefinierte Aktionen zum Wischen nach links/rechts hinzufügen?~~**

~~Das Natürlichste beim Wischen eines Listeneintrags nach links oder rechts ist, den Eintrag aus der Liste zu entfernen.~~ ~~Die natürlichste Aktion im Kontext einer E-Mail-App ist die Verschiebung der Nachricht aus dem Ordner in einen anderen Ordner.~~ ~~Sie können den zu Ziel-Ordner in den Kontoeinstellungen auswählen.~~

~~Andere Aktionen, wie zum Beispiel das Markieren von Nachrichten als gelesen und zurückgestellt, sind über mehrere Auswahlen verfügbar.~~ ~~Sie können eine Nachricht lange drücken, um eine Mehrfach-Auswahl zu starten. Siehe auch [diese Frage](#user-content-faq55).~~

~~Wischen nach links oder rechts, um eine Nachricht als gelesen oder ungelesen zu markieren, ist unnatürlich, weil die Nachricht zuerst weg geht und später wiederkommt in einer anderen Form.~~ ~~Beachten Sie, dass es eine erweiterte Option gibt, um Nachrichten automatisch beim Verschieben zu markieren,~~ ~~, was in den meisten Fällen ein perfekter Ersatz ist für die Sequenz 'als gelesen markieren und in einen Ordner verschieben'. ~~ ~~Sie können auch Nachrichten von der Benachrichtigung aus als gelesen markieren~~

~~Wenn Sie eine Nachricht später lesen möchten, können Sie sie bis zu einer bestimmten Zeit ausblenden, indem Sie das Menü *Zurückstellen* verwenden.~~

<br />

<a name="faq65"></a>
**(65) Warum werden einige Anhänge gedimmt angezeigt?**

Inline-Anhänge (Bilder) werden verdunkelt angezeigt. [Inline-Anhänge](https://tools.ietf.org/html/rfc2183) sollten automatisch heruntergeladen und angezeigt werden, aber da FairEmail nicht immer automatisch Anhänge herunterlädt, lesen Sie bitte auch [diese FAQ](#user-content-faq40), zeigt FairEmail trotzdem alle Anhänge an. Um Inline-Anhänge und normale Anhänge zu unterscheiden, werden Inline-Anhänge verdunkelt angeezeigt.

<br />

<a name="faq66"></a>
**(66) Ist FairEmail in der Google-Play-Familienmediathek verfügbar?**

*Sie können In-App-Käufe und kostenlose Apps nicht mit Ihren Familienmitgliedern teilen.*

Unter *[»Feststellen, ob Inhalte hinzugefügt werden können«](https://support.google.com/googleone/answer/7007852)* und *»Apps und Spiele«*, sehen Sie, ob Inhalte berechtigt sind, zur Familienmediathek hinzugefügt zu werden.

<br />

<a name="faq67"></a>
**(67) Wie kann ich Unterhaltungen zurückstellen?**

Wählen Sie eine oder mehrere Unterhaltungen aus (drücken Sie lange, um die Mehrfachauswahl zu starten), tippen Sie auf die Drei-Punkte-Taste und wählen Sie *Zurückstellen …*. Alternativ in der erweiterten Nachrichtenansicht *Zurückstellen …* im Drei-Punkte-Menü der Nachricht oder die Zeitrafferaktion in der unteren Aktionsleiste. Wählen Sie die Zeit, welche die Unterhaltung(en) zurückgestellt werden soll, und bestätigen dieses, indem Sie auf OK klicken. Die Unterhaltungen werden für die gewählte Zeit ausgeblendet und anschließend wieder angezeigt. Sie werden eine neue Benachrichtigung als Erinnerung erhalten.

Es ist auch möglich, Nachrichten mit [einer Regel](#user-content-faq71) zurückzustellen, mit der Sie auch Nachrichten in einen Ordner verschieben können, damit sie automatisch zurückgestellt werden.

Sie können zurückgestellte Nachrichten anzeigen, indem Sie *Filtern* > *Ausblenden* im Drei-Punkte-Auswahlmenü abwählen.

Sie können das kleine Symbol „Zurückstellen” antippen, um anzuzeigen, bis zu welchem Zeitpunkt eine Unterhaltung zurückgestellt wurde.

Durch Auswahl einer Null, können Sie das Zurückstellen abbrechen.

Drittanbieter-Apps haben bei Gmail keinen Zugriff auf den Nachrichtenordner für zurückgestellte Nachrichten.

<br />

<a name="faq68"></a>
**~~(68) Warum kann Adobe-Acrobat-Reader keine PDF-Anhänge öffnen / Microsoft-Apps keine angehängten Dokumente öffnen?~~**

~~Adobe Acrobat Reader und Microsoft-Apps erwarten weiterhin vollen Zugriff auf alle gespeicherten Dateien,~~ ~~während Apps seit Android KitKat (2013)~~ das [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) verwenden sollten, ~~ ~~um nur auf aktiv freigegebene Dateien zugreifen zu können. Dieses ist aus Gründen der Privatsphäre und Sicherheit so.~~

~~Sie können dies umgehen, indem Sie den Anhang speichern und ihn mit dem Adobe Acrobat Reader / der Microsoft-App öffnen,~~ ~~aber es wird empfohlen, einen aktuellen und vorzugsweise quelloffenen PDF-Reader / Dokumentenbetrachter zu installieren,~~ ~~zum Beispiel einen, der [hier](https://github.com/offa/android-foss#-document--pdf-viewer) aufgelistet ist.~~

<br />

<a name="faq69"></a>
**(69) Kannst du automatisches Hochscrollen bei Erhalt einer neuen Nachricht hinzufügen?**

Die Nachrichtenliste wird automatisch nach oben gescrollt, wenn Sie von einer neuen Nachrichtenbenachrichtigung oder nach einer manuellen Aktualisierung navigieren. Immer automatisch nach oben scrollen, wenn neue Nachrichten erscheinen, würde Ihr eigenes Scrollen stören, aber wenn Sie wünschen, können Sie dies in den Einstellungen aktivieren.

<br />

<a name="faq70"></a>
**(70) Wann werden Nachrichten automatisch erweitert?**

Wenn Sie zu einer Unterhaltung navigieren, wird eine Nachricht erweitert, wenn:

* Es nur eine Nachricht in der Unterhaltung gibt
* Es genau eine ungelesene Nachricht in der Unterhaltung gibt
* Es genau eine gekennzeichnete (favorisierte) Nachricht in der Konversation gibt (ab Version 1.1508)

Es gibt eine Ausnahme: Die Nachricht wurde noch nicht heruntergeladen und die Nachricht ist zu groß, um sie automatisch über eine kostenpflichtigen (Mobil) Verbindung herunterzuladen. Sie können die maximale Nachrichtengröße auf der Registerkarte 'Verbindung' einstellen oder deaktivieren.

Doppelte (archivierte) Nachrichten, gelöschte Nachrichten und Nachrichten-Entwürfe werden nicht gezählt.

Nachrichten werden bei der Erweiterung automatisch als gelesen markiert, es sei denn, dies wurde in den jeweiligen Kontoeinstellungen deaktiviert.

<br />

<a name="faq71"></a>
**(71) Wie verwende ich Filterregeln?**

Sie können Filterregeln durch langes Drücken eines Ordners in der Ordnerliste eines Kontos bearbeiten (tippen Sie auf den Kontonamen im Navigations/Seitenmenü).

Neue Regeln werden auf neue Nachrichten angewendet, die im Ordner empfangen werden, nicht auf bestehende Nachrichten. Sie können die Regel überprüfen und die Regel auf bestehende Nachrichten anwenden oder alternativ lange die Regel in der Regelliste drücken und *Ausführen* wählen.

Sie müssen einer Regel einen Namen geben und Sie müssen die Reihenfolge festlegen, in der eine Regel im Verhältnis zu anderen Regeln ausgeführt werden soll.

Sie können eine Regel deaktivieren und Sie können die Verarbeitung anderer Regeln beenden, nachdem eine Regel ausgeführt wurde.

Folgende Regelbedingungen sind verfügbar:

* Absender enthält oder Absender ist Kontakt
* Empfänger enthält
* Betreff enthält
* Hat Anhänge (optional für einen bestimmten Typ)
* Kopfzeile enthält
* Absolute Zeit (empfangen) zwischen (seit Version 1.1540)
* Relative Zeit (empfangen) zwischen

Alle Bedingungen einer Regel müssen für die Ausführung der Regelaktion zutreffen. Alle Bedingungen sind optional, aber es muss mindestens eine Bedingung geben, um zu verhindern, dass alle Nachrichten passen. Wenn Sie alle Absender oder alle Empfänger ntzen möchten, können Sie einfach das @-Zeichen als Bedingung verwenden, da alle E-Mail-Adressen dieses Zeichen enthalten. Wenn Sie einen Domainnamen abgleichen möchten, können Sie etwas wie *@example.org* als Bedingung verwenden

Beachten Sie, dass E-Mail-Adressen wie folgt formatiert sind:

`
"Jemand" <somebody@example.org>`

Sie können mehrere Regeln verwenden, möglicherweise mit einer *Stop-Verarbeitung*, für eine *oder* oder eine *nicht* Bedingung.

Bei der Bedingungs-Prüfung wird keine Groß-/Kleinschreibung beachtet, es sei denn, Sie verwenden [reguläre Ausdrücke](https://en.wikipedia.org/wiki/Regular_expression). Bitte siehe [hier](https://developer.android.com/reference/java/util/regex/Pattern) für die Dokumentation regulärer Java-Ausdrücke. Du kannst eine RegEx [hier](https://regexr.com/) testen.

Beachten Sie, dass ein regulärer Ausdruck einen *oder* Operator unterstützt. Wenn also mehrere Absender zutreffen sollen, können Sie folgendes tun:

`
.*alice@example\.org.*|.*bob@example\.org.*|.*carol@example\.org.*`

Beachten Sie, dass [Punkt All-Modus](https://developer.android.com/reference/java/util/regex/Pattern#DOTALL) aktiviert ist, sodass [entsperrte Header](https://tools.ietf.org/html/rfc2822#section-3.2.3) entsprechend geprüft werden können.

Sie können eine dieser Aktionen für passende Nachrichten auswählen:

* Keine Aktion (nützlich für *nichts*)
* Als gelesen markieren
* Als ungelesen markieren
* Ausblenden
* Benachrichtigung unterdrücken
* Zurückstellen
* Stern hinzufügen
* Lokale Priorität setzen
* Stichwort hinzufügen
* Verschieben
* Kopieren (Gmail: Label)
* Antworten / Weiterleiten (mit Vorlage)
* Text-zu-Sprache (Absender und Betreff)
* Automatisierung (Tasker usw.)

Ein Fehler in der Bedingung der Filterregel kann zu einem Desaster führen. Daher werden unumkerhbare Aktionen nicht unterstützt.

Regeln werden direkt angewendet, nachdem die Kopfzeilen der Nachricht abgerufen wurden, aber bevor der Nachrichtentext heruntergeladen wurde. Damit ist es nicht möglich, Bedingungen auf den Nachrichtentext anzuwenden. Beachten Sie, dass große Nachrichtentexte bei getakteten Verbindungen erst auf Anfrage heruntergeladen werden, um Datennutzung zu sparen.

Wenn Sie eine Nachricht weiterleiten wollen, sollten Sie stattdessen die Aktion "Verschieben" verwenden. Dies wird auch zuverlässiger sein als weiterzuleiten, da weitergeleitete Nachrichten als Spam angesehen werden können.

Da Nachrichtenheader standardmäßig nicht heruntergeladen und gespeichert werden, um Akku, Datenverbrauch und Speicherplatz zu sparen, ist es nicht möglich, eine Vorschau zu sehen, welche Nachrichten zu einer Headerregel-Bedingung passen.

Einige häufige Header-Bedingungen (Regex):

* *.&ast;Auto-Submitted:.&ast;* [RFC3834](https://tools.ietf.org/html/rfc3834)
* *.&ast;Content-Type: multipart/report.&ast;* [RFC3462](https://tools.ietf.org/html/rfc3462)

Im Drei-Punkte-Menü *mehr* gibt es ein Element, um eine Regel für eine empfangene Nachricht mit den häufigsten Bedingungen zu erstellen.

Das POP3-Protokoll unterstützt nicht das Setzen von Schlüsselwörtern und das Verschieben oder Kopieren von Nachrichten.

Die Verwendung von Regeln ist ein Pro-Feature.

<br />

<a name="faq72"></a>
**(72) Was sind Primärkonten/Identitäten?**

Das Primärkonto wird verwendet, wenn das Konto nicht eindeutig ist. Zum Beispiel wenn ein neuer Entwurf aus dem einheitlichen Posteingang verfasst wird.

Ebenso wird die primäre Identität eines Kontos verwendet, wenn die Identität uneindeutig ist.

Es können nur ein Primärkonto sowie eine primäre Identität pro Konto existieren.

<br />

<a name="faq73"></a>
**(73) Ist das Verschieben von Nachrichten über Konten hinweg sicher/effizient?**

Das Verschieben von Nachrichten über Konten hinweg ist sicher, weil die rohen, ursprünglichen Nachrichten heruntergeladen und verschoben werden und weil die Quellnachrichten erst gelöscht werden, nachdem die Zielnachrichten hinzugefügt wurden

Das Stapelverschieben von Nachrichten über Konten hinweg ist effizient, wenn sowohl der Quellordner als auch der Zielordner auf Synchronisation eingestellt sind, andernfalls muss FairEmail für jede Nachricht eine Verbindung zu dem/den Ordner(n) herstellen.

<br />

<a name="faq74"></a>
**(74) Warum sehe ich doppelte Nachrichten?**

Bei einigen Anbietern, insbesondere bei Google Mail, werden alle Nachrichten in allen Ordnern, mit Ausnahme der gelöschten Nachrichten, auch im Archivordner (alle Nachrichten) aufgelistet. FairEmail zeigt alle diese Nachrichten in einer nicht aufdringlichen Art und Weise an, um darauf hinzuweisen, dass diese Nachrichten tatsächlich die gleiche Nachricht sind.

In Gmail kann eine Nachricht mehrere Labels haben, die FairEmail als Ordner präsentiert werden. Das bedeutet, dass Nachrichten mit mehreren Labels auch mehrfach angezeigt werden.

<br />

<a name="faq75"></a>
**(75) Können Sie eine iOS-, Windows-, Linux- usw. Version erstellen?**

Eine Menge Wissen und Erfahrung ist erforderlich, um erfolgreich eine App für eine bestimmte Plattform zu entwickeln. Deshalb entwickle ich nur Apps für Android.

<br />

<a name="faq76"></a>
**(76) Was macht 'Lösche lokale Nachrichten'?**

Die Option *Lokale Nachrichten löschen* entfernt Nachrichten vom Gerät, die auf dem Server noch vorhanden sind. Sie löscht keine Nachrichten vom Server. Dies kann nützlich sein, wenn die Ordnereinstellungen geändert werden, um den Nachrichteninhalt (Text und Anhänge) nicht herunterzuladen, zum Beispiel um Speicherplatz zu sparen.

<br />

<a name="faq77"></a>
**(77) Warum werden Nachrichten manchmal mit einer kleinen Verzögerung angezeigt?**

Abhängig von der Geschwindigkeit Ihres Geräts (Prozessorgeschwindigkeit und vielleicht sogar noch mehr Speichergeschwindigkeit) können Nachrichten mit einer kleinen Verzögerung angezeigt werden. FairEmail ist so konzipiert, dass eine große Anzahl von Nachrichten dynamisch bearbeitet werden kann, ohne dass der Speicher voll wird. Das bedeutet, dass Nachrichten aus einer Datenbank gelesen werden müssen sowie diese Datenbank auf Änderungen überwacht werden muss; beides kann zu kleinen Verzögerungen führen.

Einige Komfortfunktionen, wie z. B. das Gruppieren von Nachrichten zur Anzeige von Gesprächsthemen und das Ermitteln der vorherigen/nächsten Nachricht, benötigen etwas mehr Zeit. Beachten Sie, dass es keine *die* nächste Nachricht gibt, da in der Zwischenzeit eine neue Nachricht eingetroffen sein könnte.

Wenn Sie die Geschwindigkeit von FairEmail mit ähnlichen Apps vergleichen, sollte dies ein Teil des Vergleichs sein. Es ist einfach, eine ähnliche, schnellere App zu schreiben, die nur eine Zeilenliste von Nachrichten anzeigt und dabei möglicherweise zu viel Speicher verbraucht, aber es ist nicht so einfach, die Ressourcennutzung richtig zu verwalten und fortgeschrittenere Funktionen wie Konversationsthreading anzubieten.

FairEmail basiert auf den modernsten [Android-Architekturkomponenten](https://developer.android.com/topic/libraries/architecture/), daher gibt es wenig Spielraum für Leistungsverbesserungen.

<br />

<a name="faq78"></a>
**(78) Wie verwende ich Zeitpläne?**

In den Empfangseinstellungen können Sie die Terminierung aktivieren und einen Zeitraum und die Wochentage festlegen, an denen *Meldungen **empfangen* werden sollen. Beachten Sie, dass eine Endzeit, die gleich oder früher als die Startzeit ist, als 24 Stunden später angesehen wird.

Automatisierung (siehe unten) kann für erweiterte Zeitpläne verwendet werden, zum Beispiel für mehrere Synchronisationszeiträume pro Tag oder unterschiedliche Synchronisationszeiträume für verschiedeneTage.

FairEmail kann in mehreren Benutzerprofilen, zum Beispiel in einem persönlichen und einem Arbeitsprofil, installiert und in jedem dieser Profile anders konfiguriert werden, was eine weitere Möglichkeit ist, verschiedene Synchronisationspläne zu nutzen und verschidene Konten zu synchronisieren.

Außerdem ist es möglich, [Filterregeln](#user-content-faq71) mit einer Zeitbedingung zu erstellen und Meldungen bis zum Ende der Zeitbedingung zurückzustellen. Auf diese Weise ist es möglich, dienstliche Nachrichten bis zum Beginn der Arbeitszeit *zurückzustellen*. Dies bedeutet auch, dass die Nachrichten auf Ihrem Gerät verfügbar sind, wenn es (vorübergehend) keine Internetverbindung gibt.

Beachten Sie, dass aktuelle Android-Versionen DND (Do Not Disturb) pro Benachrichtigungskanal und pro App überschreiben können, was genutzt werden kann, um bestimmte (geschäftliche) Benachrichtigungen (nicht) zu blockieren. Weitere Informationen finden Sie [hier](https://support.google.com/android/answer/9069335).

Für komplexere Schemata können Sie ein oder mehrere Konten auf manuelle Synchronisierung setzen und den folgenden Befehl an FairEmail senden, um nach neuen Nachrichten zu suchen:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL
```

Für ein bestimmtes Konto:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL --es account Gmail
```

Sie können auch das Ein- und Ausschalten des Empfangs von Nachrichten automatisieren, indem Sie die folgenden Befehle an FairEmail schicken:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE
```

Um ein bestimmtes Konto zu aktivieren/deaktivieren:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE --es account Gmail
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE --es account Gmail
```

Beachten Sie, dass das Deaktivieren eines Kontos das Konto und alle zugehörigen Ordner und Nachrichten ausblenden wird.

Um das Abfrageintervall einzustellen:

```
(adb shell) adb shell am start-foreground-service -a eu.faircode.email.INTERVAL --ei minutes nnn
```

Wobei *nnn* einer von diesen ist: 0, 15, 30, 60, 120, 240, 480, 1440. Ein Wert von 0 bedeutet Push-Benachrichtigungen.

Sie können Befehle automatisch senden, zum Beispiel [Tasker](https://tasker.joaoapps.com/userguide/en/intents.html):

```
Neue Aufgabe: Etwas wiedererkennbares
Kategorie der Aktion: Versch./Sendeabsicht
Aktion: eu.faircode.email.ENABLE
Ziel: Service
```

Um ein Konto mit dem Namen *Gmail* zu aktivieren/deaktivieren:

```
Extras: Konto:Gmail
```

Kontobezeichnung bitte Groß- und Kleinschreibung beachten.

Plannung ist eine Pro-Funktion.

<br />

<a name="faq79"></a>
**(79) Wie kann ich bei Bedarf syncronisieren (Handbuch)?**

Normalerweise hält FairEmail wann immer möglich eine Verbindung zu den konfigurierten E-Mail-Servern, um Nachrichten in Echtzeit zu empfangen. Wenn sie nicht wollen, zum Beispiel nicht gestört zu werden oder um Akku zu sparen, deaktivieren sie den Empfang in der Empfangseinstellungen. Dadurch wird der Hintergrunddienst, der sich um die automatische Synchronisierung kümmert, gestoppt; sowie die zugehörige Benachrichtigung entfernt.

Sie können auch die *manuelle Synchronisierung* in den erweiterten Kontoeinstellungen aktivieren, wenn sie bestimmte Konten nur manull synchronisieren wollen.

Sie können in einer Nachrichtenliste herunterziehen oder im Ordnermenü auf *Jetzt synchronisieren* tippen, um die Nachrichten manuell zu synchronisieren.

Wenn sie einige oder alle Ordner eines Kontos manuell synchronisieren möchten, deakrivieren Sie einfach die manuelle Synchronisation für die jeweiligen Ordner, aber nicht für das gesamte Konto.

Vermutlich möchten Sie auch [den Server](#user-content-faq24) nicht mehr durchsuchen.

<br />

<a name="faq80"></a>
**~~(80) Wie behebe ich den Fehler »Unable to load BODYSTRUCTURE« ?~~**

~~Die Fehlermeldung *Unable to load BODYSTRUCTURE* wird durch Fehler im E-Mail-Server verursacht,~~ ~~Siehe [hier](https://javaee.github.io/javamail/FAQ#imapserverbug) für weitere Details.~~ ~

~~FairEmail versucht bereits, diese Fehler zu umgehen, aber falls das fehlschlägt, müssen Sie Unterstützung von Ihrem Anbieter erbitten. ~~

<br />

<a name="faq81"></a>
**~~(81) Kann der Hintergrund der ursprünglichen Nachricht im dunklen Modus dunkel gemacht werden?~~**

~~Die ursprüngliche Nachricht wird so, wie es der Absender gesendet hat angezeigt, einschließlich aller Farben.~~ ~~Das Ändern der Hintergrundfarbe würde nicht nur die ursprüngliche Ansicht nicht mehr original machen, sondern kann auch unlesbare Nachrichten verursachen.~~

<br />

<a name="faq82"></a>
**(82) Was ist ein Verfolgungsbild?**

Bitte [hier](https://en.wikipedia.org/wiki/Web_beacon) nachsehen, was ein Verfolgungsbild genau ist. In kleinen Verfolgungsbildern wird festgehalten, ob Sie eine Nachricht geöffnet haben.

FairEmail erkennt in den meisten Fällen automatisch Verfolgungsbilder und ersetzt diese durch dieses Symbol:

![Externes Bild](https://github.com/M66B/FairEmail/blob/master/images/baseline_my_location_black_48dp.png)

Die automatische Erkennung von Verfolgungsbildern kann in den Privatsphäreneinstellungen deaktiviert werden.

<br />

<a name="faq84"></a>
**(84) Wofür gibt es lokale Kontakte?**

Lokale Kontaktinformationen basieren auf Namen und Adressen in eingehenden und ausgehenden Nachrichten.

Die Hauptnutzung von lokalen Kontakten besteht darin, die automatische Vervollständigung auch dann anzubieten, wenn FairEmail kein Zugriff auf die Android-Kontakte erteilt wurde.

Eine weitere Verwendung ist die Erzeugung von [Verknüpfungen](#user-content-faq31) für aktuelle Android-Versionen, um schnell eine Nachricht an häufig kontaktierte Personen zu senden. Dies ist auch der Grund, warum die Anzahl der Kontakte sowie das letzte Mal, als diese kontaktiert wurden, aufgezeichnet werden; und, warum Sie einen Kontakt zu einem Favoriten machen oder ihn durch langes Drücken von Favoriten ausschließen können.

Die Kontaktliste ist nach der Anzahl der Nachrichten und der letzten Kontaktierung sortiert.

Standardmäßig werden nur Namen und Adressen aufgezeichnet, an die Sie Nachrichten senden. Dies können Sie in dern Sende-Einstelungen ändern.

<br />

<a name="faq85"></a>
**(85) Warum ist eine Identität nicht verfügbar?**

Eine Identität (zur Versendung einer Nachricht oder zum Antworten) ist nur verfügbar, wenn:

* die Identität ist zum synchronisieren gesetzt (Nachrichten senden)
* das zugeordnete Konto wird synchronisiert (Nachrichten empfangen)
* das zugehörige Konto hat einen Entwürfe-Ordner

FairEmail versucht die passendste Identität, basierend auf der *Empfänger*-Adresse der Nachricht, auf die geantwortet oder die weitergeleitet weden soll, zu finden.

<br />

<a name="faq86"></a>
**~~(86) Was sind »zusätzliche Privatsphärenfunktionen«?~~**

~~Die erweiterte Option *zusätzliche Privatsphärenfunktionen* aktiviert:~~

* ~~Suche nach dem Besitzer der IP-Adresse eines Link~~
* ~~Erkennung und Entfernung von [Verfolgungsbildern](#user-content-faq82)~~

<br />

<a name="faq87"></a>
**(87) Was bedeutet "ungültige Anmeldedaten"?**

Die Fehlermeldung *ungültige Anmeldedaten* bedeutet, dass der Benutzername und/oder das Passwort falsch sind. Zum Beispiel könnte das Passwort geändert worden oder abgelaufen sein, oder die Konto-Autorisierung ist abgelaufen.

Wenn das Paswort falsch bzw. abgelaufen ist, müssen Sie es in den Account- und/oder in den Identitätseinstellungen aktualisieren.

Wenn die Autorisierung des Kontos abgelaufen ist, müssen Sie das Konto erneut auswählen. Warcheinlich müssen sie auch die zugehörige Identität wieder speichern.

<br />

<a name="faq88"></a>
**(88) Wie kann ich ein Yahoo, AOL oder Sky-Konto verwenden?**

Die bevorzugte Art, ein Yahoo-Konto einzurichten, ist der Schnelleinrichtungsassistent, der OAuth anstatt eines Passworts verwendet, was sicherer (und einfacher) ist.

Um ein Yahoo-, AOL- oder Sky-Konto zu autorisieren, müssen Sie ein App-Passwort erstellen. Für Anweisungen siehe hier:

* [für Yahoo](https://help.yahoo.com/kb/generate-third-party-passwords-sln15241.html)
* [für AOL](https://help.aol.com/articles/Create-and-manage-app-password)
* [für Sky](https://www.sky.com/help/articles/getting-started-with-sky-yahoo-mail) (unter *anderen E-Mail-Apps*)

Siehe [diese häufig gestellten Fragen (FAQ)](#user-content-faq111) zu OAuth Support.

Beachten Sie, dass Yahoo, AOL und Sky keine Standard Push-Benachrichtigungen unterstützen. Die Yahoo-Mailapp verwendet ein proprietäres, nicht dokumentiertes Protokoll für Push-Nachrichten.

Push-Benachritigungen erfordern [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE), aber der Yahoo-Mailserver meldet IDLE nicht als nutzbar:

```
Y1 CAPABILITY
* CAPABILITY IMAP4rev1 ID MOVE NAMESPACE XYMHIGHESTMODSEQ UIDPLUS LITERAL+ CHILDREN X-MSG-EXT UNSELECT OBJECTID
Y1 OK CAPABILITY completed
```

<br />

<a name="faq89"></a>
**(89) Wie kann ich Reintext-Nachrichten senden?**

Standardmäßig sendet FairEmail jede Nachricht sowohl als Klartext als auch als HTML formatierten Text, da fast jeder Empfänger heutzutage formatierte Nachrichten erwartet. Wenn Sie nur reine Textnachrichten senden möchten, können Sie dies in den erweiterten Identitätsoptionen aktivieren. Wenn Sie von Fall zu Fall zu reinen Text-Nachrichten wechseln wollen, sollten Sie dafür ein neues Profil anlegen.

<br />

<a name="faq90"></a>
**(90) Warum sind einige Texte als Link formatiert, auch wenn Sie kein Link sind?**

FairEmail formatiert zu Ihrer Bequemlichkeit automatisch nicht verlinkte URLs (http & https) und Email-Adressen (mailto). Texte und Links sind jedoch nicht leicht zu unterscheiden, besonders nicht mit vielen [Top-Level-Domains](https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains) als Worte. Deshalb werden Texte mit Punkten manchmal fehlerhaft als Links erkannt, was besser ist, als wenn einige Links gar nicht erkannt werden.

Links für das tel, geo, rtsp und xmpp Protokoll werden ebenfalls erkannt, aber Links für weniger übliche oder weniger sichere Protokolle wie Telnet und ftp nicht. Das Regex zur Erkennung von Links ist bereits *sehr* kompliziert, das Hinzufügen weiterer Protokolle würde es langsamer und fehleranfälliger machen.

Beachten Sie, dass Originalnachrichten genau so angezeigt werden, wie sie empfangen wurden, was auch bedeutet, dass Links nicht automatisch hinzugefügt werden.

<br />

<a name="faq91"></a>
**~~(91) Können Sie eine periodische Synchronisierung hinzufügen, um den Akku zu schonen?~~**

~~Synchronisieren von Nachrichten ist ein ressourcenaufwändiger Prozess, denn die lokal gespeicherten Nachrichten müssen mit den Nachrichten auf dem Server verglichen werden,~~ ~~daher würde eine periodische Synchronisierung keine Akkuleistung sparen, sondern eher zum Gegenteil führen.~~

~~Siehe [diese F&A](#user-content-faq39) über die Optimierung der Akkunutzung~~

<br />

<a name="faq92"></a>
**(92) Können Sie Spamfilter, Überprüfung der DKIM-Signatur und SPF-Autorisierung hinzufügen?**

Spamfilter, Überprüfung der [DKIM](https://en.wikipedia.org/wiki/DomainKeys_Identified_Mail)-Signatur und [SPF](https://en.wikipedia.org/wiki/Sender_Policy_Framework)-Autorisierung ist eine Aufgabe von E-Mail-Servern, nicht die eines E-Mail-Clients. Server verfügen im Allgemeinen über mehr Speicher- und Rechenleistung, daher sind sie wesentlich besser für diese Aufgabe geeignet als batteriebetriebene Geräte. Außerdem möchten Sie, dass Spam für alle Ihre E-Mail-Clients und möglicherweise auch für Web-E-Mails gefiltert wird, nicht nur für einen E-Mail-Client. Außerdem haben E-Mail-Server Zugriff auf Informationen des verbindenden Servers, wie der IP-Adresse usw., auf die ein E-Mail-Programm keinen Zugriff hat.

Spam-Filterung, die auf Nachrichten-Kopfzeilen basiert, wäre möglich gewesen, aber leider ist diese Technik [von Microsoft patentiert](https://patents.google.com/patent/US7543076).

Neueste Versionen von FairEmail können Spam mit Hilfe eines Nachrichtenklassifikators erweitert filtern. Siehe [diese häufig gestellten Fragen (FAQ)](#user-content-faq163) für mehr Informationen.

Natürlich können Sie Nachrichten als Spam mit FairEmail melden, wodurch die gemeldeten Nachrichten in den Spam-Ordner verschoben werden und SIe und den Spam-Filter ihres Providers trainieren. Dieser Vorgang kann auch mit [Filterregeln](#user-content-faq71) automatisiert werden. Das Blockieren des Absenders erstellt eine Filterregel, die zukünftige Nachrichten dieses Absenders automatisch in den Spam-Ordner verschiebt.

Beachten Sie, dass das POP3-Protokoll nur Zugriff auf den Posteingang gewährt. Es ist also nicht möglich, Spam für POP3-Konten zu melden.

Beachten Sie, dass Sie Spam-Nachrichten nicht löschen sollten, auch nicht aus dem Spam-Ordner. E-Mail-Server nutzen die Nachrichten im Spam-Ordner, um die Erkennung von zukünftigen Spam-Nachrichten zu trainieren.

Wenn Sie viele Spam-Nachrichten in Ihrem Posteingang erhalten, sollten Sie den E-Mail-Anbieter kontaktieren und anfragen, ob dessen Spam-Filter verbessert werden könnte.

FairEmail kann auch eine kleine rote Warnmeldung anzeigen, wenn eine DKIM, SPF oder [DMARC](https://en.wikipedia.org/wiki/DMARC) Authentifizierung auf dem Empfangsserver fehlgeschlagen ist. Sie können diese [Authentifizierungsüberprüfung](https://en.wikipedia.org/wiki/Email_authentication) in den Anzeigeeinstellungen aktivieren bzw. deaktivieren.

FairEmail kann auch ein Warnzeichen anzeigen, wenn für den Domänennamen der (Antwort-) E-Mail-Adresse des Absenders keinen MX-Eintrag hinterlegt ist, mit dem auf einen E-Mail-Server verwiesen wird. Dies kann in den Empfangseinstellungen aktiviert werden. Beachten Sie, dass dies die Synchronisierung von Nachrichten erheblich verlangsamt.

Wenn sich der Domainname des Absenders und der Domainname der Antwortadresse unterscheiden, wird ebenfalls das Warnsymbol angezeigt, da dies am häufigsten bei Phishing-Nachrichten der Fall ist. Falls gewünscht, kann dies (ab Version 1.1506). in den Empfangseinstellungen deaktiviert werden.

Wenn bei legitimen Nachrichten die Authentifizierung fehlschlägt, sollten Sie den Absender benachrichtigen, da dies mit hoher Wahrscheinlichkeit dazu führen kann, dass Nachrichten im Spam-Ordner landen. Darüber hinaus besteht ohne ordnungsgemäße Authentifizierung das Risiko, dass die Identität des Absenders vorgetäuscht werden kann. Der Absender könnte [dieses Werkzeug](https://www.mail-tester.com/) verwenden, um die Authentifizierung und weitere Punkte zu überprüfen.

<br />

<a name="faq93"></a>
**(93) Ist eine Installation oder das Verlagern des Datenspeichers auf einen externen Datenträger (SD-Karte) möglich?**

FairEmail nutzt Dienste und Alarme, bietet Widgets und achtet darauf, dass das Gerät komplett gebootet hat, damit die App beim Start geöffnet werden kann. Deshalb ist es nicht möglich, die App auf einem externen Speichermedium, wie einer SD-Karte, zu speichern. Siehe auch [hier](https://developer.android.com/guide/topics/data/install-location).

Nachrichten, Anhänge usw., die auf externen Speichermedien, wie einer SD-Karte, gespeichert sind, können von anderen Apps abgerufen werden und sind daher nicht sicher. Siehe [hier](https://developer.android.com/training/data-storage) für Details.

Bei Bedarf können Sie (Roh-)Nachrichten über das Drei-Punkte-Menü direkt über dem Nachrichtentext speichern und Anhänge speichern, indem Sie auf das Diskettensymbol tippen.

Wenn Sie Speicherplatz sparen müssen, können Sie die Anzahl der Tage begrenzen, für die Nachrichten synchronisiert und aufbewahrt werden. Sie können diese Einstellungen ändern, indem Sie lange auf einen Ordner in der Ordnerliste drücken und *Eigenschaften bearbeiten* auswählen.

<br />

<a name="faq94"></a>
**(94) Was bedeutet der rot-orangefarbene Streifen am Ende des Headers?**

Der rot-orangefarbene Streifen auf der linken Seite des Headers bedeutet, dass die Authentifizierung von DKIM, SPF oder DMARC fehlgeschlagen ist. Siehe auch [diese F&A](#user-content-faq92).

<br />

<a name="faq95"></a>
**(95) Warum werden nicht alle Apps angezeigt, wenn ein Anhang oder ein Bild ausgewählt wird?**

Aus Datenschutz- und Sicherheitsgründen hat FairEmail keine Berechtigungen, um auf Dateien direkt zuzugreifen. Für diesen Zweck wird das Storage Access Framework verwendet und empfohlen, das seit Android 4.4 KitKat (veröffentlicht 2013) verfügbar ist.

Ob eine App aufgelistet ist, hängt davon ab, ob die App einen [Dokumentenanbieter](https://developer.android.com/guide/topics/providers/document-provider) implementiert. Wenn die App nicht aufgelistet ist, müssen Sie den Entwickler der App bitten, Unterstützung für das Storage Access Framework hinzuzufügen.

Android Q macht es schwieriger und vielleicht sogar unmöglich, direkt auf Dateien zuzugreifen, siehe [hier](https://developer.android.com/preview/privacy/scoped-storage) und [hier](https://www.xda-developers.com/android-q-storage-access-framework-scoped-storage/) für weitere Details.

<br />

<a name="faq96"></a>
**(96) Wo finde ich die IMAP-und SMTP-Einstellungen?**

Die IMAP-Einstellungen sind Teil der (benutzerdefinierten) Kontoeinstellungen und die SMTP-Einstellungen sind Teil der Identitätseinstellungen.

<br />

<a name="faq97"></a>
**(97) Was ist 'Bereinigen' ?**

Ungefähr alle vier Stunden führt FairEmail eine Bereinigung durch, der:

* alte Nachrichten entfernt
* alte Anhänge entfernt
* alte Bilddateien entfernt
* alte lokale Kontakte entfernt
* alte Logeinträge entfernt

Beachten Sie, dass die Bereinigung nur durchgeführt wird, wenn der Synchronisierungsdienst aktiv ist.

<br />

<a name="faq98"></a>
**(98) Warum kann ich immer noch Kontakte auswählen, wenn ich Kontaktberechtigungen widerrufen habe?**

Nach dem Widerrufen der Kontaktberechtigungen erlaubt Android FairEmail keinen Zugriff mehr auf Ihre Kontakte. Das Aussuchen von Kontakten wird jedoch an Android delegiert und nicht durch FairEmail, so dass dies ohne Kontaktberechtigungen möglich ist.

<br />

<a name="faq99"></a>
**(99) Kannst du einen Rich-Text oder Markdown-Editor hinzufügen?**

FairEmail bietet eine einfache Textformatierung (fett, kursiv, unterstrichen, Textgröße und -farbe) über eine Symbolleiste, die nach der Auswahl eines Textes erscheint.

Ein [Rich-Text](https://en.wikipedia.org/wiki/Formatted_text) oder [Markdown](https://en.wikipedia.org/wiki/Markdown) Editor würde nicht von vielen Leuten auf einem kleinen mobilen Gerät verwendet werden und, wichtiger, Android unterstützt keinen Rich-Text-Editor und die meisten Rich Text-Editor Open-Source-Projekte wurden aufgegeben. Siehe [hier](https://forum.xda-developers.com/showpost.php?p=79061829&postcount=4919) für weitere Details darüber.

<br />

<a name="faq100"></a>
**(100) Wie kann ich Google-Mail-Kategorien synchronisieren?**

Sie können Google-Mail-Kategorien synchronisieren, indem Sie Filter erstellen, um kategorisierte Nachrichten zu kennzeichnen:

* Erstellen Sie einen neuen Filter über Google Mail > Einstellungen (Rad) > Filter und blockierte Adressen > Erstellen Sie einen neuen Filter
* Geben Sie eine Kategoriesuche (siehe unten) in das Feld *Hat die Wörter* ein und klicken Sie auf *Filter erstellen*
* Überprüfen Sie *Beschriftung anwenden* und wählen Sie eine Beschriftung aus und klicken Sie auf *Filter erstellen*

Mögliche Kategorien:

```
category:social
category:updates
category:forums
category:promotions
```

Leider ist das für den Ordner »Zurückgestellte Nachrichten« nicht möglich.

Sie können *Synchronisation erzwingen* im Drei-Punkte-Menü des Sammeleingangs verwenden, um FairEmail erneut die Ordnerliste synchronisieren zu lassen und Sie können einen Ordner lange drücken, um die Synchronisierung zu aktivieren.

<br />

<a name="faq101"></a>
**(101) Was bedeutet der blaue/orangefarbene Punkt am unteren Ende der Unterhaltung?**

Der Punkt zeigt die relative Position der Unterhaltung in der Nachrichtenliste an. Der Punkt wird orange angezeigt, wenn die Unterhaltung der erste oder letzte in der Nachrichtenliste ist. Andernfalls ist er blau. Der Punkt ist als Hilfsmittel gedacht, wenn man links/rechts wischt, um zur vorherigen/nächsten Unterhaltung zu gelangen.

Der Punkt ist standardmäßig deaktiviert und kann mit den Anzeigeeinstellungen *relative Konversationsposition mit einem Punkt anzeigen* aktiviert werden.

<br />

<a name="faq102"></a>
**(102) Wie kann ich die automatische Drehung von Bildern aktivieren?**

Bilder werden automatisch gedreht, wenn die automatische Größe der Bilder in den Einstellungen aktiviert ist (standardmäßig aktiviert). Jedoch hängt die automatische Drehung von den [Exif-](https://en.wikipedia.org/wiki/Exif)Informationen ab, welche vorhanden und richtig sein müssen, was nicht immer der Fall ist. Insbesondere nicht beim Fotografieren mit einer Kamara-App aus FairEmail.

Bitte beachten Sie, dass nur [JPEG-](https://en.wikipedia.org/wiki/JPEG) und [PNG-](https://en.wikipedia.org/wiki/Portable_Network_Graphics)Bilder Exif-Informationen enthalten können.

<br />

<a name="faq104"></a>
**(104) Was muss ich über Fehlerberichte wissen?**

* Fehlerberichte helfen FairEmail zu verbessern
* Fehlermeldung ist optional und opt-in
* Fehlerberichte können in den Einstellungen aktiviert/deaktiviert werden, Abschnitt Verschiedenes
* Fehlerberichte werden automatisch anonym an [Bugsnag](https://www.bugsnag.com/) gesendet
* Bugsnag für Android ist [Open Source](https://github.com/bugsnag/bugsnag-android)
* Siehe [hier](https://docs.bugsnag.com/platforms/android/automatically-captured-data/) darüber, welche Daten bei Fehlern gesendet werden
* Siehe [hier](https://docs.bugsnag.com/legal/privacy-policy/) für die Datenschutzerklärung von Bugsnag
* Fehlerberichte werden an *sessions.bugsnag.com:443* und *notify.bugsnag.com:443* gesendet

<br />

<a name="faq105"></a>
**(105) Wie funktioniert die roam-like-at-home-Option?**

FairEmail prüft, ob der Ländercode der SIM-Karte und der Ländercode des Netzes in den [EU-Roam-like-at-home-Ländern](https://en.wikipedia.org/wiki/European_Union_roaming_regulations#Territorial_extent) liegen und nimmt kein Roaming an, wenn die Ländercodes gleich sind und die erweiterte Roam-like-at-home-Option aktiviert ist.

Sie müssen diese Option also nicht deaktivieren, wenn Sie keine EU-SIM-Karte haben oder nicht mit einem EU-Netzwerk verbunden sind.

<br />

<a name="faq106"></a>
**(106) Welche Launcher können die Anzahl ungelesener Nachrichten als Badge anzeigen?**

[Finden Sie hier](https://github.com/leolin310148/ShortcutBadger#supported-launchers) eine Liste von Launchern, die die Anzahl der ungelesenen Nachrichten anzeigen können.

Beachten Sie, dass Nova Launcher „TeslaUnread” benötigt, was [nicht mehr unterstützt wird](https://forum.xda-developers.com/android/general/bad-news-tesla-unread-devoloper-t3920415).

Beachten Sie, dass die Benachrichtigungseinstellung *Startsymbol mit Anzahl der neuen Nachrichten anzeigen* aktiviert sein muss (Standard aktiviert).

Nur *neue* ungelesene Nachrichten in Ordnern, die für die Anzeige neuer Nachrichten gesetzt sind, werden gezählt, so dass Nachrichten, die erneut als ungelesen markiert werden und Nachrichten in Ordnern, die auf keine neue Nachrichtenbenachrichtigung eingestellt sind, nicht gezählt werden.

Je nachdem, was Sie wollen, muss die Benachrichtigungseinstellungen *Lassen Sie die Anzahl der neuen Nachrichten mit der Anzahl der Benachrichtigungen* übereinstimmen (Standard deaktiviert) gesetzt werden. Wenn diese Funktion aktiviert ist, entspricht die Zahl der Kennzeichnung der Anzahl der Benachrichtigungen über neue Nachrichten. Wenn diese Funktion deaktiviert ist, wird die Anzahl der ungelesenen Nachrichten angezeigt, unabhängig davon, ob sie in einer Benachrichtigung angezeigt werden oder neu sind.

This feature depends on support of your launcher. FairEmail merely 'broadcasts' the number of unread messages using the ShortcutBadger library. If it doesn't work, this cannot be fixed by changes in FairEmail.

Some launchers display a dot or a '1' for [the monitoring notification](#user-content-faq2), despite FairEmail explicitly requesting not to show a *badge* for this notification. This could be caused by a bug in the launcher app or in your Android version. Please double check if the notification dot (badge) is disabled for the receive (service) notification channel. You can go to the right notification channel settings via the notification settings of FairEmail. This might not be obvious, but you can tap on the channel name for more settings.

FairEmail does send a new message count intent as well:

```
eu.faircode.email.NEW_MESSAGE_COUNT
```

The number of new, unread messages will be in an integer "*count*" parameter.

<br />

<a name="faq107"></a>
**(107) How do I use colored stars?**

You can set a colored star via the *more* message menu, via multiple selection (started by long pressing a message), by long pressing a star in a conversation or automatically by using [rules](#user-content-faq71).

You need to know that colored stars are not supported by the IMAP protocol and can therefore not be synchronized to an email server. This means that colored stars will not be visible in other email clients and will be lost on downloading messages again. However, the stars (without color) will be synchronized and will be visible in other email clients, when supported.

Some email clients use IMAP keywords for colors. However, not all servers support IMAP keywords and besides that there are no standard keywords for colors.

<br />

<a name="faq108"></a>
**~~(108) Can you add permanently delete messages from any folder?~~**

~~When you delete messages from a folder the messages will be moved to the trash folder, so you have a chance to restore the messages.~~ ~~You can permanently delete messages from the trash folder.~~ ~~Permanently delete messages from other folders would defeat the purpose of the trash folder, so this will not be added.~~

<br />

<a name="faq109"></a>
**~~(109) Why is 'select account' available in official versions only?~~**

~~Using *select account* to select and authorize Google accounts require special permission from Google for security and privacy reasons.~~ ~~This special permission can only be acquired for apps a developer manages and is responsible for.~~ ~~Third party builds, like the F-Droid builds, are managed by third parties and are the responsibility of these third parties.~~ ~~So, only these third parties can acquire the required permission from Google.~~ ~~Since these third parties do not actually support FairEmail, they are most likely not going to request the required permission.~~

~~You can solve this in two ways:~~

* ~~Wechseln Sie zur offiziellen Version von FairEmail, siehe [hier](https://github.com/M66B/FairEmail/blob/master/README.md#downloads) für die Optionen~~
* ~~App-spezifische Passwörter verwenden, siehe [diese FAQ](#user-content-faq6)~~

~~Using *select account* in third party builds is not possible in recent versions anymore.~~ ~~In older versions this was possible, but it will now result in the error *UNREGISTERED_ON_API_CONSOLE*.~~

<br />

<a name="faq110"></a>
**(110) Why are (some) messages empty and/or attachments corrupt?**

Empty messages and/or corrupt attachments are probably being caused by a bug in the server software. Older Microsoft Exchange software is known to cause this problem. Mostly you can workaround this by disabling *Partial fetch* in the advanced account settings:

Settings > Manual setup > Accounts > tap account > tap advanced > Partial fetch > uncheck

After disabling this setting, you can use the message 'more' (three dots) menu to 'resync' empty messages. Alternatively, you can *Delete local messages* by long pressing the folder(s) in the folder list and synchronize all messages again.

Deaktivieren von *Teilabruf* führt zu mehr Speicherverbrauch.

<br />

<a name="faq111"></a>
**(111) Is OAuth supported?**

OAuth for Gmail is supported via the quick setup wizard. The Android account manager will be used to fetch and refresh OAuth tokens for selected on-device accounts. OAuth for non on-device accounts is not supported because Google requires a [yearly security audit](https://support.google.com/cloud/answer/9110914) ($15,000 to $75,000) for this. You can read more about this [here](https://www.theregister.com/2019/02/11/google_gmail_developer/).

OAuth for Outlook/Office 365, Yahoo, Mail.ru and Yandex is supported via the quick setup wizard.

<br />

<a name="faq112"></a>
**(112) Which email provider do you recommend?**

FairEmail is an email client only, so you need to bring your own email address. Note that this is clearly mentioned in the app description.

There are plenty of email providers to choose from. Which email provider is best for you depends on your wishes/requirements. Please see the websites of [Restore privacy](https://restoreprivacy.com/secure-email/) or [Privacy Tools](https://www.privacytools.io/providers/email/) for a list of privacy oriented email providers with advantages and disadvantages.

Some providers, like ProtonMail, Tutanota, use proprietary email protocols, which make it impossible to use third party email apps. Please see [this FAQ](#user-content-faq129) for more information.

Using your own (custom) domain name, which is supported by most email providers, will make it easier to switch to another email provider.

<br />

<a name="faq113"></a>
**(113) How does biometric authentication work?**

If your device has a biometric sensor, for example a fingerprint sensor, you can enable/disable biometric authentication in the navigation (hamburger) menu of the settings screen. When enabled FairEmail will require biometric authentication after a period of inactivity or after the screen has been turned off while FairEmail was running. Activity is navigation within FairEmail, for example opening a conversation thread. The inactivity period duration can be configured in the miscellaneous settings. When biometric authentication is enabled new message notifications will not show any content and FairEmail won't be visible on the Android recents screen.

Biometric authentication is meant to prevent others from seeing your messages only. FairEmail relies on device encryption for data encryption, see also [this FAQ](#user-content-faq37).

Biometric authentication is a pro feature.

<br />

<a name="faq114"></a>
**(114) Can you add an import for the settings of other email apps?**

The format of the settings files of most other email apps is not documented, so this is difficult. Sometimes it is possible to reverse engineer the format, but as soon as the settings format changes things will break. Also, settings are often incompatible. For example, FairEmail has unlike most other email apps settings for the number of days to synchronize messages and for the number of days to keep messages, mainly to save on battery usage. Moreover, setting up an account/identity with the quick setup wizard is simple, so it is not really worth the effort.

<br />

<a name="faq115"></a>
**(115) Can you add email address chips?**

Email address [chips](https://material.io/design/components/chips.html) look nice, but cannot be edited, which is quite inconvenient when you made a typo in an email address.

Note that FairEmail will select the address only when long pressing an address, which makes it easy to delete an address.

Chips are not suitable for showing in a list and since the message header in a list should look similar to the message header of the message view it is not an option to use chips for viewing messages.

Reverted [commit](https://github.com/M66B/FairEmail/commit/2c80c25b8aa75af2287f471b882ec87d5a5a5015).

<br />

<a name="faq116"></a>
**~~(116) How can I show images in messages from trusted senders by default?~~**

~~You can show images in messages from trusted senders by default by enabled the display setting *Automatically show images for known contacts*.~~

~~Contacts in the Android contacts list are considered to be known and trusted,~~ ~~unless the contact is in the group / has the label '*Untrusted*' (case insensitive).~~

<br />

<a name="faq38"></a>
<a name="faq117"></a>
**(117) Can you help me restore my purchase?**

First of all, a purchase will be available on all devices logged into the same Google account, *if* the app is installed via the same Google account too. You can select the account in the Play store app.

Google manages all purchases, so as a developer I have little control over purchases. So, basically the only thing I can do, is give some advice:

* Stellen Sie sicher, dass Sie über eine stabile Internetverbindung verfügen
* Stellen Sie sicher, dass Sie mit dem richtigen Google-Konto angemeldet sind und dass alles mit Ihrem Google-Konto in Ordnung ist
* Stellen Sie sicher, dass Sie FairEmail über das richtige Google-Konto installiert haben, wenn mehrere Google-Konten auf Ihrem Gerät eingerichtet wurden
* Stelle sicher, dass die Play Store App aktuell ist, bitte [hier](https://support.google.com/googleplay/answer/1050566?hl=en) überprüfen
* Öffnen Sie die App des Play Store und warten Sie mindestens eine Minute, um ihr Zeit zu geben, sich mit den Google-Servern zu synchronisieren
* Öffnen Sie FairEmail und wechseln Sie zum Bildschirm mit den Profifunktionen, damit FairEmail die Einkäufe überprüfen kann. Oftmals hilft es, auf die Schaltfläche *Kaufen* zu tippen

You can also try to clear the cache of the Play store app via the Android apps settings. Restarting the device might be necessary to let the Play store recognize the purchase correctly.

Note that:

* Wenn Sie *ITEM_ALREADY_OWNED*erhalten, muss die Play Store App wahrscheinlich aktualisiert werden, bitte [hier finden](https://support.google.com/googleplay/answer/1050566?hl=en)
* Einkäufe werden in der Google Cloud gespeichert und können nicht verloren gehen
* Bei Einkäufen gibt es keine Zeitbegrenzung, daher können sie nicht auslaufen
* Google gibt keine Details (Name, E-Mail usw.) über Käufer an Entwickler weiter
* Eine App wie FairEmail kann nicht auswählen, welches Google-Konto verwendet werden soll
* Es kann eine Weile dauern, bis die Play Store-App einen Kauf mit einem anderen Gerät synchronisiert hat
* Käufe über den Play Store können ohne den Play Store nicht verwendet werden, was nach den Play-Store-Regeln ebenfalls nicht zulässig ist

If you cannot solve the problem with the purchase, you will have to contact Google about it.

<br />

<a name="faq118"></a>
**(118) What does 'Remove tracking parameters' exactly?**

Checking *Remove tracking parameters* will remove all [UTM parameters](https://en.wikipedia.org/wiki/UTM_parameters) from a link.

<br />

<a name="faq119"></a>
**~~(119) Can you add colors to the unified inbox widget?~~**

~~The widget is designed to look good on most home/launcher screens by making it monochrome and by using a half transparent background.~~ ~~This way the widget will nicely blend in, while still being properly readable.~~

~~Adding colors will cause problems with some backgrounds and will cause readability problems, which is why this won't be added.~~

Due to Android limitations it is not possible to dynamically set the opacity of the background and to have rounded corners at the same time.

<br />

<a name="faq120"></a>
**(120) Why are new message notifications not removed on opening the app?**

New message notifications will be removed on swiping notifications away or on marking the associated messages read. Opening the app will not remove new message notifications. This gives you a choice to leave new message notifications as a reminder that there are still unread messages.

On Android 7 Nougat and later new message notifications will be [grouped](https://developer.android.com/training/notify-user/group). Tapping on the summary notification will open the unified inbox. The summary notification can be expanded to view individual new message notifications. Tapping on an individual new message notification will open the conversation the message it is part of. See [this FAQ](#user-content-faq70) about when messages in a conversation will be auto expanded and marked read.

<br />

<a name="faq121"></a>
**(121) How are messages grouped into a conversation?**

By default FairEmail groups messages in conversations. This can be turned of in the display settings.

FairEmail groups messages based on the standard *Message-ID*, *In-Reply-To* and *References* headers. FairEmail does not group on other criteria, like the subject, because this could result in grouping unrelated messages and would be at the expense of increased battery usage.

<br />

<a name="faq122"></a>
**~~(122) Why is the recipient name/email address show with a warning color?~~**

~~The recipient name and/or email address in the addresses section will be shown in a warning color~~ ~~when the sender domain name and the domain name of the *to* address do not match.~~ ~~Mostly this indicates that the message was received *via* an account with another email address.~~

<br />

<a name="faq123"></a>
**(123) What will happen when FairEmail cannot connect to an email server?**

If FairEmail cannot connect to an email server to synchronize messages, for example if the internet connection is bad or a firewall or a VPN is blocking the connection, FairEmail will retry one time after waiting 8 seconds while keeping the device awake (=use battery power). If this fails, FairEmail will schedule an alarm to retry after 15, 30 and eventually every 60 minutes and let the device sleep (=no battery usage).

Note that [Android doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby) does not allow to wake the device earlier than after 15 minutes.

*Force sync* in the three-dots menu of the unified inbox can be used to let FairEmail attempt to reconnect without waiting.

Sending messages will be retried on connectivity changes only (reconnecting to the same network or connecting to another network) to prevent the email server from blocking the connection permanently. You can pull down the outbox to retry manually.

Note that sending will not be retried in case of authentication problems and when the server rejected the message. In this case you can pull down the outbox to try again.

<br />

<a name="faq124"></a>
**(124) Why do I get 'Message too large or too complex to display'?**

The message *Message too large or too complex to display* will be shown if there are more than 100,000 characters or more than 500 links in a message. Reformatting and displaying such messages will take too long. You can try to use the original message view, powered by the browser, instead.

<br />

<a name="faq125"></a>
**(125) What are the current experimental features?**

*Message classification (version 1.1438+)*

Please see [this FAQ](#user-content-faq163) for details.

Since this is an experimental feature, my advice is to start with just one folder.

<br />

*Send hard bounce (version 1.1477+)*

Send a [Delivery Status Notification](https://tools.ietf.org/html/rfc3464) (=hard bounce) via the reply/answer menu.

Hard bounces will mostly be processed automatically because they affect the reputation of the email provider. The bounce address (=*Return-Path* header) is mostly very specific, so the email server can determine the sending account.

Für einige Hintergründe siehe [diesen Wikipedia-Artikel](https://en.wikipedia.org/wiki/Bounce_message).

<br />

<a name="faq126"></a>
**(126) Can message previews be sent to my wearable?**

FairEmail ruft eine Nachricht in zwei Schritten ab:

1. Nachrichtenköpfe abrufen
1. Nachrichtentext und Anhänge abrufen

Directly after the first step new messages will be notified. However, only until after the second step the message text will be available. FairEmail updates exiting notifications with a preview of the message text, but unfortunately wearable notifications cannot be updated.

Since there is no guarantee that a message text will always be fetched directly after a message header, it is not possible to guarantee that a new message notification with a preview text will always be sent to a wearable.

If you think this is good enough, you can enable the notification option *Only send notifications with a message preview to wearables* and if this does not work, you can try to enable the notification option *Show notifications with a preview text only*. Note that this applies to wearables not showing a preview text too, even when the Android Wear app says the notification has been sent (bridged).

If you want to have the full message text sent to your wearable, you can enable the notification option *Preview all text*. Note that some wearables are known to crash with this option enabled.

If you use a Samsung wearable with the Galaxy Wearable (Samsung Gear) app, you might need to enable notifications for FairEmail when the setting *Notifications*, *Apps installed in the future* is turned off in this app.

<br />

<a name="faq127"></a>
**(127) How can I fix 'Syntactically invalid HELO argument(s)'?**

The error *... Syntactically invalid HELO argument(s) ...* means that the SMTP server rejected the local IP address or host name. You can likely fix this error by enabling or disabling the advanced indentity option *Use local IP address instead of host name*.

<br />

<a name="faq128"></a>
**(128) How can I reset asked questions, for example to show images?**

You can reset asked questions via the three dots overflow menu in the miscellaneous settings.

<br />

<a name="faq129"></a>
**(129) Are ProtonMail, Tutanota supported?**

ProtonMail uses a proprietary email protocol and [does not directly support IMAP](https://protonmail.com/support/knowledge-base/imap-smtp-and-pop3-setup/), so you cannot use FairEmail to access ProtonMail.

Tutanota uses a proprietary email protocol and [does not support IMAP](https://tutanota.com/faq/#imap), so you cannot use FairEmail to access Tutanota.

<br />

<a name="faq130"></a>
**(130) What does message error ... mean?**

A series of lines with orangish or red texts with technical information means that debug mode was enabled in the miscellaneous settings.

The warning *No server found at ...* means that there was no email server registered at the indicated domain name. Replying to the message might not be possible and might result in an error. This could indicate a falsified email address and/or spam.

The error *... ParseException ...* means that there is a problem with a received message, likely caused by a bug in the sending software. FairEmail will workaround this is in most cases, so this message can mostly be considered as a warning instead of an error.

The error *...SendFailedException...* means that there was a problem while sending a message. The error will almost always include a reason. Common reasons are that the message was too big or that one or more recipient addresses were invalid.

The warning *Message too large to fit into the available memory* means that the message was larger than 10 MiB. Even if your device has plenty of storage space Android provides limited working memory to apps, which limits the size of messages that can be handled.

Please see [here](#user-content-faq22) for other error messages in the outbox.

<br />

<a name="faq131"></a>
**(131) Can you change the direction for swiping to previous/next message?**

If you read from left to right, swiping to the left will show the next message. Similarly, if you read from right to left, swiping to the right will show the next message.

This behavior seems quite natural to me, also because it is similar to turning pages.

Anyway, there is a behavior setting to reverse the swipe direction.

<br />

<a name="faq132"></a>
**(132) Why are new message notifications silent?**

Notifications are silent by default on some MIUI versions. Please see [here](http://en.miui.com/thread-3930694-1-1.html) how you can fix this.

There is a bug in some Android versions causing [setOnlyAlertOnce](https://developer.android.com/reference/android/app/Notification.Builder#setOnlyAlertOnce(boolean)) to mute notifications. Since FairEmail shows new message notifications right after fetching the message headers and FairEmail needs to update new message notifications after fetching the message text later, this cannot be fixed or worked around by FairEmail.

Android might rate limit the notification sound, which can cause some new message notifications to be silent.

<br />

<a name="faq133"></a>
**(133) Why is ActiveSync not supported?**

The Microsoft Exchange ActiveSync protocol [is patented](https://en.wikipedia.org/wiki/Exchange_ActiveSync#Licensing) and can therefore not be supported. For this reason you won't find many, if any, other email clients supporting ActiveSync.

Note that the desciption of FairEmail starts with the remark that non-standard protocols, like Microsoft Exchange Web Services and Microsoft ActiveSync are not supported.

<br />

<a name="faq134"></a>
**(134) Can you add deleting local messages?**

*POP3*

In the account settings (Settings, tap Manual setup, tap Accounts, tap account) you can enable *Leave deleted messages on server*.

*IMAP*

Since the IMAP protocol is meant to synchronize two ways, deleting a message from the device would result in fetching the message again when synchronizing again.

However, FairEmail supports hiding messages, either via the three-dots menu in the action bar just above the message text or by multiple selecting messages in the message list. Basically this is the same as "leave on server" of the POP3 protocol with the advantage that you can show the messages again when needed.

Note that it is possible to set the swipe left or right action to hide a message.

<br />

<a name="faq135"></a>
**(135) Why are trashed messages and drafts shown in conversations?**

Individual messages will rarely be trashed and mostly this happens by accident. Showing trashed messages in conversations makes it easier to find them back.

You can permanently delete a message using the message three-dots *delete* menu, which will remove the message from the conversation. Note that this irreversible.

Similarly, drafts are shown in conversations to find them back in the context where they belong. It is easy to read through the received messages before continuing to write the draft later.

<br />

<a name="faq136"></a>
**(136) How can I delete an account/identity/folder?**

Deleting an account/identity/folder is a little bit hidden to prevent accidents.

* Account: Settings > Manual setup > Accounts > tap account
* Identity: Settings > Manual setup > Identities > tap identity
* Folder: Long press the folder in the folder list > Edit properties

In the three-dots overflow menu at the top right there is an item to delete the account/identity/folder.

<br />

<a name="faq137"></a>
**(137) How can I reset 'Don't ask again'?**

You can reset all questions set to be not asked again in the miscellaneous settings.

<br />

<a name="faq138"></a>
**(138) Can you add calendar/contact/tasks/notes management?**

Calendar, contact, task and note management can better be done by a separate, specialized app. Note that FairEmail is a specialized email app, not an office suite.

Also, I prefer to do a few things very well, instead of many things only half. Moreover, from a security perspective, it is not a good idea to grant many permissions to a single app.

You are advised to use the excellent, open source [DAVx⁵](https://f-droid.org/packages/at.bitfire.davdroid/) app to synchronize/manage your calendars/contacts.

Most providers support exporting your contacts. Please [see here](https://support.google.com/contacts/answer/1069522) about how you can import contacts if synchronizing is not possible.

Note that FairEmail does support replying to calendar invites (a pro feature) and adding calendar invites to your personal calendar.

<br />

<a name="faq83"></a>
<a name="faq139"></a>
**(139) How do I fix 'User is authenticated but not connected'?**

In fact this Microsoft Exchange specific error is an incorrect error message caused by a bug in older Exchange server software.

The error *User is authenticated but not connected* might occur if:

* Push messages are enabled for too many folders: see [this FAQ](#user-content-faq23) for more information and a workaround
* The account password was changed: changing it in FairEmail too should fix the problem
* An alias email address is being used as username instead of the primary email address
* An incorrect login scheme is being used for a shared mailbox: the right scheme is *username@domain\SharedMailboxAlias*

The shared mailbox alias will mostly be the email address of the shared account, like this:

```
you@example.com\shared@example.com
```

Note that it should be a backslash and not a forward slash.

When using a shared mailbox, you'll likely want to enable the option *Synchronize shared folder lists* in the receive settings.

<br />

<a name="faq140"></a>
**(140) Why does the message text contain strange characters?**

Displaying strange characters is almost always caused by specifying no or an invalid character encoding by the sending software. FairEmail will assume [ISO 8859-1](https://en.wikipedia.org/wiki/ISO/IEC_8859-1) when no character set or when [US-ASCII](https://en.wikipedia.org/wiki/ASCII) was specified. Other than that there is no way to reliably determine the correct character encoding automatically, so this cannot be fixed by FairEmail. The right action is to complain to the sender.

<br />

<a name="faq141"></a>
**(141) How can I fix 'A drafts folder is required to send messages'?**

To store draft messages a drafts folder is required. In most cases FairEmail will automatically select the drafts folders on adding an account based on [the attributes](https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml) the email server sends. However, some email servers are not configured properly and do not send these attributes. In this case FairEmail tries to identify the drafts folder by name, but this might fail if the drafts folder has an unusual name or is not present at all.

You can fix this problem by manually selecting the drafts folder in the account settings (Settings, tap Manual setup, tap Accounts, tap account, at the bottom). If there is no drafts folder at all, you can create a drafts folder by tapping on the '+' button in the folder list of the account (tap on the account name in the navigation menu).

Some providers, like Gmail, allow enabling/disabling IMAP for individual folders. So, if a folder is not visible, you might need to enable IMAP for the folder.

Quick link for Gmail (will work on a desktop computer only): [https://mail.google.com/mail/u/0/#settings/labels](https://mail.google.com/mail/u/0/#settings/labels)

<br />

<a name="faq142"></a>
**(142) How can I store sent messages in the inbox?**

Generally, it is not a good idea to store sent messages in the inbox because this is hard to undo and could be incompatible with other email clients.

That said, FairEmail is able to properly handle sent messages in the inbox. FairEmail will mark outgoing messages with a sent messages icon for example.

The best solution would be to enable showing the sent folder in the unified inbox by long pressing the sent folder in the folder list and enabling *Show in unified inbox*. This way all messages can stay where they belong, while allowing to see both incoming and outgoing messages at one place.

If this is not an option, you can [create a rule](#user-content-faq71) to automatically move sent messages to the inbox or set a default CC/BCC address in the advanced identity settings to send yourself a copy.

<br />

<a name="faq143"></a>
**~~(143) Can you add a trash folder for POP3 accounts?~~**

[POP3](https://en.wikipedia.org/wiki/Post_Office_Protocol) is a very limited protocol. Basically only messages can be downloaded and deleted from the inbox. It is not even possible to mark a message read.

Since POP3 does not allow access to the trash folder at all, there is no way to restore trashed messages.

Note that you can hide messages and search for hidden messages, which is similar to a local trash folder, without suggesting that trashed messages can be restored, while this is actually not possible.

Version 1.1082 added a local trash folder. Note that trashing a message will permanently remove it from the server and that trashed messages cannot be restored to the server anymore.

<br />

<a name="faq144"></a>
**(144) How can I record voice notes?**

To record voice notes you can press this icon in the bottom action bar of the message composer:

![Externes Bild](https://github.com/M66B/FairEmail/blob/master/images/baseline_record_voice_over_black_48dp.png)

This requires a compatible audio recorder app to be installed. In particular [this common intent](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION) needs to be supported.

For example [this audio recorder](https://f-droid.org/app/com.github.axet.audiorecorder) is compatible.

Voice notes will automatically be attached.

<br />

<a name="faq145"></a>
**(145) How can I set a notification sound for an account, folder or sender?**

Account:

* Enable *Separate notifications* in the advanced account settings (Settings, tap Manual setup, tap Accounts, tap account, tap Advanced)
* Long press the account in the account list (Settings, tap Manual setup, tap Accounts) and select *Edit notification channel* to change the notification sound

Folder:

* Long press the folder in the folder list and select *Create notification channel*
* Long press the folder in the folder list and select *Edit notification channel* to change the notification sound

Sender:

* Open a message from the sender and expand it
* Expand the addresses section by tapping on the down arrow
* Tap on the bell icon to create or edit a notification channel and to change the notification sound

The order of precendence is: sender sound, folder sound, account sound and default sound.

Setting a notification sound for an account, folder or sender requires Android 8 Oreo or later and is a pro feature.

<br />

<a name="faq146"></a>
**(146) How can I fix incorrect message times?**

Since the sent date/time is optional and can be manipulated by the sender, FairEmail uses the server received date/time by default.

Sometimes the server received date/time is incorrect, mostly because messages were incorrectly imported from another server and sometimes due to a bug in the email server.

In these rare cases, it is possible to let FairEmail use either the date/time from the *Date* header (sent time) or from the *Received* header as a workaround. This can be changed in the advanced account settings: Settings, tap Manual setup, tap Accounts, tap account, tap Advanced.

This will not change the time of already synchronized messages. To solve this, long press the folder(s) in the folder list and select *Delete local messages* and *Synchronize now*.

<br />

<a name="faq147"></a>
**(147) What should I know about third party versions?**

You likely came here because you are using a third party build of FairEmail.

There is **only support** on the latest Play store version, the latest GitHub release and the F-Droid build, but **only if** the version number of the F-Droid build is the same as the version number of the latest GitHub release.

F-Droid builds irregularly, which can be problematic when there is an important update. Therefore you are advised to switch to the GitHub release.

The F-Droid version is built from the same source code, but signed differently. This means that all features are available in the F-Droid version too, except for using the Gmail quick setup wizard because Google approved (and allows) one app signature only. For all other email providers, OAuth access is only available in Play Store versions and Github releases, as the email providers only permit the use of OAuth for official builds.

Note that you'll need to uninstall the F-Droid build first before you can install a GitHub release because Android refuses to install the same app with a different signature for security reasons.

Note that the GitHub version will automatically check for updates. When desired, this can be turned off in the miscellaneous settings.

Please [see here](https://github.com/M66B/FairEmail/blob/master/README.md#user-content-downloads) for all download options.

If you have a problem with the F-Droid build, please check if there is a newer GitHub version first.

<br />

<a name="faq148"></a>
**(148) How can I use an Apple iCloud account?**

There is a built-in profile for Apple iCloud, so you should be able to use the quick setup wizard (other provider). If needed you can find the right settings [here](https://support.apple.com/en-us/HT202304) to manually set up an account.

When using two-factor authentication you might need to use an [app-specific password](https://support.apple.com/en-us/HT204397).

<br />

<a name="faq149"></a>
**(149) How does the unread message count widget work?**

The unread message count widget shows the number of unread messages either for all accounts or for a selected account, but only for the folders for which new message notifications are enabled.

Tapping on the notification will synchronize all folders for which synchronization is enabled and will open:

* the start screen when all accounts were selected
* a folder list when a specific account was selected and when new message notifications are enabled for multiple folders
* a list of messages when a specific account was selected and when new message notifications are enabled for one folder

<br />

<a name="faq150"></a>
**(150) Can you add cancelling calendar invites?**

Cancelling calendar invites (removing calendar events) requires write calendar permission, which will result in effectively granting permission to read and write *all* calendar events of *all* calendars.

Given the goal of FairEmail, privacy and security, and given that it is easy to remove a calendar event manually, it is not a good idea to request this permission for just this reason.

Inserting new calendar events can be done without permissions with special [intents](https://developer.android.com/guide/topics/providers/calendar-provider.html#intents). Unfortunately, there exists no intent to delete existing calendar events.

<br />

<a name="faq151"></a>
**(151) Can you add backup/restore of messages?**

An email client is meant to read and write messages, not to backup and restore messages. Note that breaking or losing your device, means losing your messages!

Instead, the email provider/server is responsible for backups.

If you want to make a backup yourself, you could use a tool like [imapsync](https://imapsync.lamiral.info/).

Since version 1.1556 it is possible to export all messages of a POP3 folder in mbox format according to [RFC4155](https://www.ietf.org/rfc/rfc4155.txt), which might be useful to save sent messages if the email server doesn't.

If you want to import an mbox file to an existing email account, you can use Thunderbird on a desktop computer and the [ImportExportTools](https://addons.thunderbird.net/nl/thunderbird/addon/importexporttools/) add-on.

<br />

<a name="faq152"></a>
**(152) How can I insert a contact group?**

You can insert the email addresses of all contacts in a contact group via the three dots menu of the message composer.

You can define contact groups with the Android contacts app, please see [here](https://support.google.com/contacts/answer/30970) for instructions.

<br />

<a name="faq153"></a>
**(153) Why does permanently deleting Gmail message not work?**

You might need to change [the Gmail IMAP settings](https://mail.google.com/mail/u/0/#settings/fwdandpop) on a desktop browser to make it work:

* When I mark a message in IMAP as deleted: Auto-Expunge off - Wait for the client to update the server.
* When a message is marked as deleted and expunged from the last visible IMAP folder: Immediately delete the message forever

Note that archived messages can be deleted only by moving them to the trash folder first.

Some background: Gmail seems to have an additional message view for IMAP, which can be different from the main message view.

Another oddity is that a star (favorite message) set via the web interface cannot be removed with the IMAP command

```
STORE <message number> -FLAGS (\Flagged)
```

On the other hand, a star set via IMAP is being shown in the web interface and can be removed via IMAP.

<br />

<a name="faq154"></a>
**~~(154) Can you add favicons as contact photos?~~**

~~Besides that a [favicon](https://en.wikipedia.org/wiki/Favicon) might be shared by many email addresses with the same domain name~~ ~~and therefore is not directly related to an email address, favicons can be used to track you.~~

<br />

<a name="faq155"></a>
**(155) What is a winmail.dat file?**

A *winmail.dat* file is sent by an incorrectly configured Outlook client. It is a Microsoft specific file format ([TNEF](https://en.wikipedia.org/wiki/Transport_Neutral_Encapsulation_Format)) containing a message and possibly attachments.

You can find some more information about this file [here](https://support.mozilla.org/en-US/kb/what-winmaildat-attachment).

You can view it with for example the Android app [Letter Opener](https://play.google.com/store/apps/details?id=app.letteropener).

<br />

<a name="faq156"></a>
**(156) How can I set up an Office 365 account?**

An Office 365 account can be set up via the quick setup wizard and selecting *Office 365 (OAuth)*.

If the wizard ends with *AUTHENTICATE failed*, IMAP and/or SMTP might be disabled for the account. In this case you should ask the administrator to enable IMAP and SMTP. The procedure is documented [here](https://docs.microsoft.com/en-in/exchange/troubleshoot/configure-mailboxes/pop3-imap-owa-activesync-office-365).

If you've enabled *security defaults* in your organization, you might need to enable the SMTP AUTH protocol. Please [see here](https://docs.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/authenticated-client-smtp-submission) about how to.

<br />

<a name="faq157"></a>
**(157) How can I set up an Free.fr account?**

Veuillez [voir ici](https://free.fr/assistance/597.html) pour les instructions.

**SMTP est désactivé par défaut**, veuillez [voir ici](https://free.fr/assistance/2406.html) comment il peut être activé.

Veuillez [voir ici](http://jc.etiemble.free.fr/abc/index.php/trucs-astuces/configurer-smtp-free-fr) pour un guide détaillé.

<br />

<a name="faq103"></a>
<a name="faq158"></a>
**(158) Which camera / audio recorder do you recommend?**

To take photos and to record audio a camera and an audio recorder app are needed. The following apps are open source cameras and audio recorders:

* [Open Camera](https://play.google.com/store/apps/details?id=net.sourceforge.opencamera) ([F-Droid](https://f-droid.org/en/packages/net.sourceforge.opencamera/))
* [Audio Recorder version 3.3.24+](https://play.google.com/store/apps/details?id=com.github.axet.audiorecorder) ([F-Droid](https://f-droid.org/packages/com.github.axet.audiorecorder/))

To record voice notes, etc, the audio recorder needs to support [MediaStore.Audio.Media.RECORD_SOUND_ACTION](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media#RECORD_SOUND_ACTION). Oddly, most audio recorders seem not to support this standard Android action.

<br />

<a name="faq159"></a>
**(159) What are Disconnect's tracker protection lists?**

Please see [here](https://disconnect.me/trackerprotection) for more information about Disconnect's tracker protection lists.

After downloading the lists in the privacy settings, the lists can optionally be used:

* to warn about tracking links on opening links
* to recognize tracking images in messages

Tracking images will be disabled only if the corresponding main 'disable' option is enabled.

Tracking images will not be recognized when the domain is classified as '*Content*', see [here](https://disconnect.me/trackerprotection#trackers-we-dont-block) for more information.

This command can be sent to FairEmail from an automation app to update the protection lists:

```
(adb shell) am start-foreground-service -a eu.faircode.email.DISCONNECT.ME
```

Updating once a week will probably be sufficient, please see [here](https://github.com/disconnectme/disconnect-tracking-protection/commits/master) for recent lists changes.

<br />

<a name="faq160"></a>
**(160) Can you add permanent deletion of messages without confirmation?**

Permanent deletion means that messages will *irreversibly* be lost, and to prevent this from happening accidentally, this always needs to be confirmed. Even with a confirmation, some very angry people who lost some of their messages through their own fault contacted me, which was a rather unpleasant experience :-(

Advanced: the IMAP delete flag in combination with the EXPUNGE command is not supportable because both email servers and not all people can handle this, risking unexpected loss of messages. A complicating factor is that not all email servers support [UID EXPUNGE](https://tools.ietf.org/html/rfc4315).

From version 1.1485 it is possible to temporarily enable debug mode in the miscellaneous settings to disable expunging messages. Note that messages with a *\Deleted* flag will not be shown in FairEmail.

<br />

<a name="faq161"></a>
**(161) Can you add a setting to change the primary and accent color?***

If I could, I would add a setting to select the primary and accent color right away, but unfortunately Android themes are fixed, see for example [here](https://stackoverflow.com/a/26511725/1794097), so this is not possible.

<br />

<a name="faq162"></a>
**(162) Is IMAP NOTIFY supported?***

Yes, [IMAP NOTIFY](https://tools.ietf.org/html/rfc5465) has been supported since version 1.1413.

IMAP NOTIFY support means that notifications for added, changed or deleted messages of all *subscribed* folders will be requested and if a notification is received for a subscribed folder, that the folder will be synchronized. Synchronization for subscribed folders can therefore be disable, saving folder connections to the email server.

**Important**: push messages (=always sync) for the inbox and subscription management (receive settings) need to be enabled.

**Important**: most email servers do not support this! You can check the log via the navigation menu if an email server supports the NOTIFY capability.

<br />

<a name="faq163"></a>
**(163) What is message classification?**

*This is an experimental feature!*

Message classification will attempt to automatically group emails into classes, based on their contents, using [Bayesian statistics](https://en.wikipedia.org/wiki/Bayesian_statistics). In the context of FairEmail, a folder is a class. So, for example, the inbox, the spam folder, a 'marketing' folder, etc, etc.

You can enable message classification in the miscellaneous settings. This will enable 'learning' mode only. The classifier will 'learn' from new messages in the inbox and spam folder by default. The folder property *Classify new messages in this folder* will enable or disable 'learning' mode for a folder. You can clear local messages (long press a folder in the folder list of an account) and synchronize the messages again to classify existing messages.

Each folder has an option *Automatically move classified messages to this folder* ('auto classification' for short). When this is turned on, new messages in other folders which the classifier thinks belong to that folder will be automatically moved.

The option *Use local spam filter* in the report spam dialog will turn on message classification in the miscellaneous settings and auto classification for the spam folder. Please understand that this is not a replacement for the spam filter of the email server and can result in [false positives and false negatives](https://en.wikipedia.org/wiki/False_positives_and_false_negatives). See also [this FAQ](#user-content-faq92).

A practical example: suppose there is a folder 'marketing' and auto message classification is enabled for this folder. Each time you move a message into this folder you'll train FairEmail that similar messages belong in this folder. Each time you move a message out of this folder you'll train FairEmail that similar messages do not belong in this folder. After moving some messages into the 'marketing' folder, FairEmail will start moving similar messages automatically into this folder. Or, the other way around, after moving some messages out of the 'marketing' folder, FairEmail will stop moving similar messages automatically into this folder. This will work best with messages with similar content (email addresses, subject and message text).

Classification should be considered as a best guess - it might be a wrong guess, or the classifier might not be confident enough to make any guess. If the classifier is unsure, it will simply leave an email where it is.

To prevent the email server from moving a message into the spam folder again and again, auto classification out of the spam folder will not be done.

The message classifier calculates the probability a message belongs in a folder (class). There are two options in the miscellaneous settings which control if a message will be automatically moved into a folder, provided that auto classification is enabled for the folder:

* *Minimum class probability*: a message will only be moved when the confidence it belongs in a folder is greater than this value (default 15 %)
* *Minimum class difference*: a message will only be moved when the difference in confidence between one class and the next most likely class is greater than this value (default 50 %)

Both conditions must be satisfied before a message will be moved.

Considering the default option values:

* Apples 40 % and bananas 30 % would be disregarded because the difference of 25 % is below the minimum of 50 %
* Apples 10 % and bananas 5 % would be disregarded because the probability for apples is below the minimum of 15 %
* Apples 50 % and bananas 20 % would result in selecting apples

Classification is optimized to use as little resources as possible, but will inevitably use some extra battery power.

You can delete all classification data by turning classification in the miscellaneous settings three times off.

[Filter rules](#user-content-faq71) will be executed before classification.

Message classification is a pro feature, except for the spam folder.

<br />

<a name="faq164"></a>
**(164) Can you add customizable themes?**

Unfortunately, Android [does not support](https://stackoverflow.com/a/26511725/1794097) dynamic themes, which means all themes need [to be predefined](https://github.com/M66B/FairEmail/blob/master/app/src/main/res/values/styles.xml).

Since for each theme there needs to be a light, dark and black variant, it is not feasible to add for each color combination (literally millions) a predefined theme.

Moreover, a theme is more than just a few colors. For example themes with a yellow accent color use a darker link color for enough contrast.

The theme colors are based on the color circle of [Johannes Itten](https://en.wikipedia.org/wiki/Johannes_Itten).

<br />

<a name="faq165"></a>
**(165) Wird Android Auto unterstützt?**

Ja, Android Auto wird unterstützt, aber nur mit der GitHub-Version, bitte [hier nachsehen](https://forum.xda-developers.com/t/app-5-0-fairemail-fully-featured-open-source-privacy-oriented-email-app.3824168/post-83801249) warum.

Für die Unterstützung von Benachrichtigungen (Meldungen) müssen Sie die folgenden Benachrichtigungsoptionen aktivieren:

* *Benachrichtigungsformat im Android-Nachrichtenstil verwenden*
* Benachrichtigungsaktionen: *direkte Antwort* und (markieren als) *gelesen*

Wenn Sie möchten, können Sie auch andere Benachrichtigungsaktionen aktivieren, aber diese werden von Android Auto nicht unterstützt.

Die Anleitung für Entwickler ist [hier](https://developer.android.com/training/cars/messaging).

<br />

<a name="faq166"></a>
**(166) Kann ich eine Nachricht über mehrere Geräte hinweg zurückstellen?**

Erstens gibt es keinen Standard für das Zurückstellen von Nachrichten, so dass alle Umsetzungen benutzerdefinierte Lösungen sind.

Einige E-Mail-Anbieter, wie z. B. Gmail, verschieben zurückgestellte Nachrichten in einen speziellen Ordner. Leider haben Drittanbieter-Apps keinen Zugriff auf diesen speziellen Ordner.

Das Verschieben einer Nachricht in einen anderen Ordner und zurück könnte fehlschlagen und ist möglicherweise nicht möglich, wenn keine Internetverbindung besteht. Das ist problematisch, weil eine Nachricht erst nach dem Verschieben der Nachricht zurückgestellt werden kann.

Um diese Probleme zu vermeiden, wird das Schlummern lokal auf dem Gerät durchgeführt, indem die Nachricht während des Schlummerns versteckt wird. Leider ist es nicht möglich, Nachrichten auch auf dem E-Mail-Server zu verstecken.

<br />

<h2><a name="get-support"></a>Hilfe erhalten</h2>

FairEmail wird nur auf Android-Smartphones und -Tablets und ChromeOS unterstützt.

Es werden nur die neuesten Versionen aus dem Play Store und von GitHub unterstützt. Die F-Droid-Version wird nur unterstützt, wenn die Versionsnummer mit der neuesten GitHub-Version übereinstimmt. Das bedeutet auch, dass eine Herabstufung nicht unterstützt wird.

Es gibt keine Hilfe für Dinge, die nicht direkt mit FairEmail zusammenhängen.

Es gibt keine Unterstützung beim Selberbauen und Entwickeln von eigenen Dingen.

Angefragte Funktionen sollten:

* für die meisten Menschen nützlich sein
* die Nutzung von FairEmail nicht verkomplizieren
* zur Philosophie von FairEmail passen (privatsphären- und sicherheitsorientiert)
* den gängigen Standards entsprechen (IMAP, SMTP usw.)

Funktionen, die diese Anforderungen nicht erfüllen, werden wahrscheinlich abgelehnt. Das soll auch langfristig die Wartung und Unterstützung ermöglichen.

Wenn Sie eine Frage haben, eine Funktion wünschen oder einen Fehler melden möchten, **benutzen Sie bitte [dieses Formular](https://contact.faircode.eu/?product=fairemailsupport)**.

GitHub-Issues sind wegen häufigen Missbrauchs deaktiviert.

<br />

Urheberrecht &copy; 2018-2021 Marcel Bokhorst.
