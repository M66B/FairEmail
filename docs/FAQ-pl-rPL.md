<a name="top"></a>

# Wsparcie FairEmail

[<img src="https://github.com/M66B/FairEmail/raw/master/app/src/main/resExtra/drawable/language_de.png" /> Deutsch](https://github.com/M66B/FairEmail/blob/master/docs/FAQ-de-rDE.md)<br /> [<img src="https://github.com/M66B/FairEmail/raw/master/app/src/main/resExtra/drawable/language_fr.png" /> Français](https://github.com/M66B/FairEmail/blob/master/docs/FAQ-fr-rFR.md)<br /> [<img src="https://github.com/M66B/FairEmail/raw/master/app/src/main/resExtra/drawable/language_es.png" /> Español](https://github.com/M66B/FairEmail/blob/master/docs/FAQ-es-rES.md)<br /> [<img src="https://github.com/M66B/FairEmail/raw/master/images/outline_translate_black_24dp.png" /> Other languages](https://github.com/M66B/FairEmail/blob/master/docs/)

W razie jakichkolwiek pytań, sprawdź najpierw poniższy FAQ. [At the bottom](#user-content-get-support), you can find out how to ask other questions, request features, and report bugs.

## Spis treści

* [Autoryzacja kont](#user-content-authorizing-accounts)
* [W jaki sposób...](#user-content-howto)
* [Znane problemy](#user-content-known-problems)
* [Planowane funkcje](#user-content-planned-features)
* [Często żądane funkcje](#user-content-frequently-requested-features)
* [Najczęściej zadawane pytania (FAQ)](#user-content-frequently-asked-questions)
* [Uzyskaj wsparcie](#user-content-get-support)

<h2><a name="authorizing-accounts"></a>Autoryzacja kont</h2>

W większości przypadków, szybki kreator ustawień będzie w stanie automatycznie rozpoznać poprawną konfigurację.

Jeśli szybki kreator konfiguracji nie powiedzie się, musisz ręcznie skonfigurować konto (aby otrzymywać e-mail) i tożsamość (aby wysłać e-mail). W tym celu potrzebujesz adresów serwera i numerów portów IMAP i SMTP, niezależnie od tego, czy należy użyć SSL/TLS, czy STARTTLS oraz swoją nazwę użytkownika (głównie, ale nie zawsze adres e-mail) i hasło.

Wyszukiwanie * IMAP * i nazwa dostawcy jest w większości wystarczające, aby znaleźć odpowiednią dokumentację.

W niektórych przypadkach musisz włączyć zewnętrzny dostęp do swojego konta i/lub użyć specjalnego hasła (aplikacji), na przykład, gdy włączone jest uwierzytelnianie dwuskładnikowe.

Autoryzacja:

* Gmail / G suite, zob. [pytanie 6](#user-content-faq6)
* Outlook / Live / Hotmail, zob. [pytanie 14](#user-content-faq14)
* Office 365, see [question 156](#user-content-faq156)
* Microsoft Exchange, zob. [pytanie 8](#user-content-faq8)
* Yahoo, AOL i Sky, zobacz [pytanie 88](#user-content-faq88)
* Apple iCloud, zob. [pytanie 148](#user-content-faq148)
* Free.fr, zob. [pytanie 157](#user-content-faq157)

[Tutaj](#user-content-faq22) znajdziesz opisy typowych komunikatów błędów i możliwych rozwiązań.

Powiązane pytania:

* [Czy OAuth jest wspierany?](#user-content-faq111)
* [Dlaczego ActiveSync nie jest obsługiwany? ](#user-content-faq133)

<a name="howto">

## W jaki sposób...?

* Zmienić nazwę konta: Ustawienia, dotknij Ręczna konfiguracja, dotknij Konta, dotknij konto
* Zmiana akcji gestu przesunięcia w lewo/w prawo: Ustawienia, Zachowanie, Ustaw akcję przesuwania
* Zmień hasło: Ustawienia, dotknij Ręczna konfiguracja, dotknij Konta, dotknij konto, zmień hasło
* Ustaw podpis: Ustawienia, dotknij Ręcznej konfiguracji, dotknij Tożsamości, dotknij tożsamości, Edytuj podpis.
* Dodanie adresów DW i UDW: dotknij ikony osób na końcu tematu
* Przejście do następnej/poprzedniej wiadomości dotyczącej archiwizowania/usuwania: w ustawieniach zachowania wyłącz * Automatycznie zamykaj konwersacje * i wybierz * Przejdź do następnej/poprzedniej rozmowy * dla * Po zamknięciu rozmowy *
* Dodanie folderu do wspólnej skrzynki odbiorczej: naciśnij długo folder na liście folderów i zaznacz * Pokaż we wspólnej skrzynce *
* Dodanie folderu do menu nawigacji: naciśnij długo folder na liście folderów i zaznacz * Pokaż w menu nawigacji *
* Załadować większą liczbę wiadomości: na liście folderów dotknij dłużej folder i wybierz *Pobierz więcej wiadomości*
* Usuń wiadomość, pomijając kosz: przytrzymaj dłużej ikonę kosza
* Usuń konto/tożsamość: Ustawienia, dotknij Ręczna konfiguracja, dotknij Konta/Tożsamości, dotknij konto/tożsamość, ikona kosza w prawym górnym rogu
* Usuń folderu: długo naciśnij folder na liście folderów, Edytuj właściwości, ikona kosza w górnym prawym rogu
* Cofnij wysyłanie: Wysłane, przeciągnij wiadomość na liście w lewo lub w prawo
* Przechowywanie wysyłanych wiadomości w skrzynce odbiorczej: [zobacz to FAQ](#user-content-faq142)
* Zmień foldery systemowe: Ustawienia, dotknij Ręcznej konfiguracji, dotknij Konta, dotknij konto u dołu
* Eksport/import ustawień: Ustawienia, menu nawigacyjne (lewa strona)

<h2><a name="known-problems"></a>Znane problemy</h2>

* ~~A [błąd w Androidzie 5.1 i 6](https://issuetracker.google.com/issues/37054851) powoduje, że aplikacje czasami pokazują nieprawidłowy format czasu. Przełączenie ustawienia Androida *Użyj 24-godzinnego formatu* może tymczasowo rozwiązać problem. Dodano obejście. ~~
* ~~A [błąd w Dysku Google](https://issuetracker.google.com/issues/126362828) powoduje, że pliki eksportowane do Dysku Google są puste. Google naprawił to.~~
* ~~ Błąd [ w Android X ](https://issuetracker.google.com/issues/78495471) powoduje, że FairEmail od czasu do czasu ulega awarii po długim naciśnięciu lub przesunięciu. Google naprawił to.~~
* ~~A [błąd w AndroidX ROOM](https://issuetracker.google.com/issues/138441698) powoduje czasami awarię "*... Wyjątek podczas obliczania bazy danych na żywo... Nie można odczytać wiersza ...*". Dodano obejście. ~~
* Błąd [w Android](https://issuetracker.google.com/issues/119872129) powoduje awarię FairEmail z "*... Nieprawidłowe powiadomienie wysłane ...*" na niektórych urządzeniach po aktualizacji FairEmail i kliknięciu na powiadomienie.
* Błąd [w Android](https://issuetracker.google.com/issues/62427912) czasem powoduje awarię z "*... Nie znaleziono ActivityRecord dla ... * ”po aktualizacji FairEmail. Ponowne zainstalowanie ([źródło](https://stackoverflow.com/questions/46309428/android-activitythread-reportsizeconfigurations-causes-app-to-freeze-with-black)) może rozwiązać problem.
* Błąd [w Android](https://issuetracker.google.com/issues/37018931) czasem powoduje awarię z "*... InputChannel nie jest inicjowany ... * na niektórych urządzeniach.
* ~~A [błąd w LineageOS](https://review.lineageos.org/c/LineageOS/android_frameworks_base/+/265273) czasami powoduje awarię z *... java.lang.ArrayIndexOutOfBoundsException: length=...; index=... ...*.~~
* Błąd w Nova Launcher na Androidzie 5.x powoduje awarię FairEmail z *java.lang.StackOverflowError* gdy Nova Launcher ma dostęp do usługi dostępności.
* ~~Wybór folderów czasami nie pokazuje żadnych folderów z nieznanych powodów. Wygląda na to, że zostało to naprawione.~~
* ~~A [błąd w AndroidX](https://issuetracker.google.com/issues/64729576) utrudnia przechwytywanie szybkiego przewijania. Dodano obejście. ~~
* ~~ Szyfrowanie za pomocą YubiKey skutkuje nieskończoną pętlą. Wydaje się, że jest to spowodowane błędem [w OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2507).~~
* Przewijanie do wewnętrznie połączonej lokalizacji w oryginalnych wiadomościach nie działa. Nie można tego naprawić, ponieważ oryginalny widok wiadomości znajduje się w przewijanym widoku.
* Podgląd tekstu wiadomości nie (zawsze) pojawia się na zegarku Samsung, ponieważ [setLocalOnly](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder.html#setLocalOnly(boolean)) wydaje się być ignorowany. Wiadomo, że podglądy wiadomości są wyświetlane poprawnie na Pebble 2, Fitbit Charge 3, Mi band 3 i Xiaomi Amazfit BIP. Zobacz również [ten FAQ](#user-content-faq126).
* [błąd w Android 6.0](https://issuetracker.google.com/issues/37068143) powoduje awarię z *... Invalid offset: ... Valid range is ...* when text is selected and tapping outside of the selected text. Ten błąd został naprawiony w Androidzie 6.0.1.
* Internal (anchor) links will not work because original messages are shown in an embedded WebView in a scrolling view (the conversation list). Jest to ograniczenie Androida, które nie może być naprawione.
* Language detection [is not working anymore](https://issuetracker.google.com/issues/173337263) on Pixel devices with (upgraded to?) Android 11
* A [bug in OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2688) causes invalid PGP signatures when using a hardware token.

<h2><a name="planned-features"></a>Planowane funkcje</h2>

* ~~Synchronizuj na żądanie (ręcznie)~~
* ~~Szyfrowanie półautomatyczne~~
* ~~Kopiowanie wiadomości~~
* ~~Kolorowe gwiazdki~~
* ~~Ustawienia powiadomień dla folderu~~
* ~~Wybierz lokalne obrazy do podpisów ~~ (nie zostanie dodane, ponieważ wymaga zarządzania plikami obrazów oraz ponieważ obrazy i tak nie są wyświetlane domyślnie w większości klientów poczty e-mail)
* ~~Pokaż wiadomości dopasowane przez regułę~~
* ~~[ManageSieve ](https://tools.ietf.org/html/rfc5804) ~~ (nie ma utrzymywanych bibliotek Java z odpowiednią licencją i bez zależności, a ponadto FairEmail ma własne reguły filtrowania)
* ~~Szukaj wiadomości z/bez załączników~~ (nie można tego dodać, ponieważ IMAP nie obsługuje wyszukiwania załączników)
* ~~Wyszukaj folder ~~ (problematyczne jest filtrowanie hierarchicznej listy folderów)
* Podpowiedzi wyszukiwania
* ~~[Autocrypt Setup Message](https://autocrypt.org/autocrypt-spec-1.0.0.pdf) (sekcja 4. )~~ (IMO nie jest dobrym pomysłem, aby klient poczty elektronicznej obsługiwał wrażliwe klucze szyfrowania do wyjątkowego użytku, podczas gdy OpenKeychain również może eksportować klucze)
* ~~Ogólne ujednolicone foldery ~~
* ~~Nowe harmonogramy powiadomień dla każdego konta~~ (zaimplementowane przez dodanie warunku czasowego do reguł, więc wiadomości mogą być odłożone w wybranych okresach)
* ~~Kopiuj konta i tożsamość~~
* ~~Pinch zoom ~~ (nie jest to możliwe w sposób niezawodny na przewijanej liście; zamiast tego można powiększyć pełny widok wiadomości)
* ~~Bardziej kompaktowy widok folderu~~
* ~~Utwórz listy i tabele ~~ (wymaga to edytora tekstu sformatowanego, patrz [ to FAQ ](#user-content-faq99))
* ~~Rozmiar tekstu powiększenia ~~
* ~~Wyświetlanie GIF~~
* ~~Motywy~~ (dodano szary jasny i ciemny motyw, ponieważ wydaje się, że tego chce większość ludzi)
* ~~Dowolny warunek czasowy~~ (żaden dzień nie pasuje do warunku od/do daty/czasu)
* ~~Wyślij jako załącznik~~
* ~~Widget dla wybranego konta~~
* ~~Przypomnij o załączeniu plików~~
* ~~Wybierz domeny do pokazania zdjęć~~ (będzie to zbyt skomplikowane, aby używać)
* ~~Wspólny widok wiadomości oznaczonych gwiazdką ~~ (jest to już specjalne wyszukiwanie)
* ~~Move notification action~~
* ~~Wsparcie S/MIME~~
* ~~Szukaj ustawień~~

Wszystko na tej liście jest w losowej kolejności i * może * zostać dodane w najbliższej przyszłości.

<h2><a name="frequently-requested-features"></a>Często żądane funkcje</h2>

Projekt opiera się na wielu dyskusjach i jeśli chcesz, możesz o nim dyskutować [na tym forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168). Projekt ma być minimalistyczny (bez zbędnych menu, przycisków itp.) I nie rozpraszać uwagi (brak fantazyjnych kolorów, animacji itp.). Wszystkie wyświetlane rzeczy powinny być użyteczne w ten czy inny sposób i powinny być umieszczone w sposób umożliwiający łatwe użytkowanie. Czcionki, rozmiary, kolory itp. Powinny być w miarę możliwości material design.

<h2><a name="frequently-asked-questions"></a>Najczęściej zadawane pytania (FAQ)</h2>

* [(1) Jakie uprawnienia są potrzebne i dlaczego?](#user-content-faq1)
* [(2) Dlaczego wyświetla się stałe powiadomienie?](#user-content-faq2)
* [(3) Czym są operacje i dlaczego są one w toku?](#user-content-faq3)
* [(4) Jak mogę użyć nieprawidłowego certyfikatu bezpieczeństwa / pustego hasła / zwykłego połączenia tekstowego?](#user-content-faq4)
* [(5) Jak mogę dostosować widok wiadomości?](#user-content-faq5)
* [(6) Jak mogę zalogować się do Gmail / G suite?](#user-content-faq6)
* [(7) Dlaczego wysłane wiadomości nie pojawiają się (bezpośrednio) w folderze Wysłane?](#user-content-faq7)
* [(8) Czy mogę korzystać z konta Microsoft Exchange?](#user-content-faq8)
* [(9) Czym są tożsamości / jak dodać alias?](#user-content-faq9)
* [~~(11) Dlaczego POP nie jest wspierany?~~](#user-content-faq11)
* [~~(10) Co oznacza „UIDPLUS nieobsługiwany”?~~](#user-content-faq10)
* [(12) Jak działa szyfrowanie / deszyfrowanie?](#user-content-faq12)
* [(13) Jak działa wyszukiwanie na urządzeniu/serwerze?](#user-content-faq13)
* [(14) Jak mogę utworzyć konto Outlook / Live / Hotmail?](#user-content-faq14)
* [(15) Dlaczego tekst wiadomości wciąż się ładuje?](#user-content-faq15)
* [(16) Dlaczego wiadomości nie są synchronizowane?](#user-content-faq16)
* [~~(17) Dlaczego ręczna synchronizacja nie działa?~~](#user-content-faq17)
* [(18) Dlaczego podgląd wiadomości nie zawsze jest wyświetlany?](#user-content-faq18)
* [(19) Dlaczego funkcje pro są tak drogie?](#user-content-faq19)
* [(20) Czy mogę otrzymać zwrot pieniędzy?](#user-content-faq20)
* [(21) Jak włączyć diodę powiadomień?](#user-content-faq21)
* [(22) Co oznacza błąd konta/folderu?](#user-content-faq22)
* [(23) Dlaczego otrzymuję ostrzeżenie.. ?](#user-content-faq23)
* [(24) Czym są wiadomości przeglądane na serwerze?](#user-content-faq24)
* [(25) Dlaczego nie mogę wybrać/otworzyć/zapisać obrazu, załącznika lub pliku?](#user-content-faq25)
* [(26) Czy mogę pomóc w tłumaczeniu FairEmail na mój język?](#user-content-faq26)
* [(27) W jaki sposób mogę odróżnić osadzone i zewnętrzne obrazy?](#user-content-faq27)
* [(28) Jak mogę zarządzać powiadomieniami na pasku statusu?](#user-content-faq28)
* [(29) Jak mogę otrzymywać powiadomienia o nowych wiadomościach dla innych folderów?](#user-content-faq29)
* [(30) Jak mogę użyć dostarczonych szybkich ustawień?](#user-content-faq30)
* [(31) W jaki sposób mogę korzystać z dostarczonych skrótów?](#user-content-faq31)
* [(32) Jak mogę sprawdzić, czy czytanie wiadomości e-mail jest naprawdę bezpieczne?](#user-content-faq32)
* [(33) Dlaczego edytowane adresy nadawcy nie działają?](#user-content-faq33)
* [(34) W jaki sposób dobierane są tożsamości?](#user-content-faq34)
* [(35) Dlaczego powinienem być ostrożny z przeglądaniem obrazów, załączników, oryginalnej wiadomości i otwieraniem linków?](#user-content-faq35)
* [(36) W jaki sposób zaszyfrowane są pliki ustawień?](#user-content-faq36)
* [(37) Jak przechowywane są hasła?](#user-content-faq37)
* [(39) Jak mogę zmniejszyć zużycie baterii przez FairEmail?](#user-content-faq39)
* [(40) Jak mogę zmniejszyć użycie sieci przez FairEmail?](#user-content-faq40)
* [(41) Jak mogę naprawić błąd "Handshake failed”?](#user-content-faq41)
* [(42) Czy możesz dodać nowego dostawcę do listy dostawców?](#user-content-faq42)
* [(43) Czy możesz pokazać oryginał...?](#user-content-faq43)
* [(44) Czy możesz pokazać zdjęcia kontaktów / identyfikatory w folderze wysłane?](#user-content-faq44)
* [(45) Jak mogę naprawić "Ten klucz nie jest dostępny. Aby go użyć, musisz zaimportować go jako swój własny! ?](#user-content-faq45)
* [(46) Dlaczego lista wiadomości ciągle się odświeża?](#user-content-faq46)
* [(47) Jak mogę rozwiązać błąd 'Brak konta głównego lub folderu szkiców' ?](#user-content-faq47)
* [~~(48) Jak mogę rozwiązać błąd 'Brak konta podstawowego lub brak folderu archiwum' ?~~](#user-content-faq48)
* [(49) Jak mogę naprawić błąd 'An outdated app sent a file path instead of a file stream' ?](#user-content-faq49)
* [(50) Czy możesz dodać opcję synchronizacji wszystkich wiadomości?](#user-content-faq50)
* [(51) Jak sortowane są foldery?](#user-content-faq51)
* [(52) Dlaczego ponowne połączenie z kontem zajmuje trochę czasu?](#user-content-faq52)
* [(53) Czy możesz przykleić pasek akcji wiadomości u góry/dołu?](#user-content-faq53)
* [~~ (54) Jak korzystać z prefiksu przestrzeni nazw? ~~](#user-content-faq54)
* [(55) Jak mogę oznaczyć wszystkie wiadomości jako przeczytane / przeniesione lub usunąć wszystkie wiadomości?](#user-content-faq55)
* [(56) Czy możesz dodać wsparcie dla JMAP?](#user-content-faq56)
* [(57) Czy mogę użyć HTML w podpisach?](#user-content-faq57)
* [(58) Co oznacza otwarta/zamknięta ikona e-mail?](#user-content-faq58)
* [(59) Czy oryginalne wiadomości mogą być otwierane w przeglądarce?](#user-content-faq59)
* [(60) Czy wiesz...?](#user-content-faq60)
* [(61) Dlaczego niektóre wiadomości są przyciemnione?](#user-content-faq61)
* [(62) Które metody uwierzytelniania są wspierane?](#user-content-faq62)
* [(63) Jak zmienia się rozmiar obrazków do wyświetlania na ekranach?](#user-content-faq63)
* [~~(64) Czy możesz dodać własne akcje dla przesunięcia w lewo/w prawo?~~](#user-content-faq64)
* [(65) Dlaczego niektóre załączniki są przyciemnione?](#user-content-faq65)
* [(66) Czy FairEmail jest dostępny w Google Play Family Library?](#user-content-faq66)
* [(67) Jak mogę uśpić/odłożyć rozmowy?](#user-content-faq67)
* [~~ (68) Dlaczego Adobe Acrobat nie może otwierać załączników PDF / aplikacje Microsoft nie mogą otwierać załączonych dokumentów? ~~](#user-content-faq68)
* [(69) Czy możesz dodać automatyczne przewijanie w górę nowej wiadomości?](#user-content-faq69)
* [(70) Kiedy wiadomości będą automatycznie rozwijane?](#user-content-faq70)
* [(71) Jak stosować zasady filtrowania?](#user-content-faq71)
* [(72) Czym są główne konta / tożsamości?](#user-content-faq72)
* [(73) Czy przenoszenie wiadomości między kontami jest bezpieczne / skuteczne?](#user-content-faq73)
* [(74) Dlaczego widzę duplikaty wiadomości?](#user-content-faq74)
* [(75) Czy potrafisz stworzyć wersję iOS, Windows, Linux itp.?](#user-content-faq75)
* [(76) Do czego służy funkcja „Usuń wiadomości lokalne”?](#user-content-faq76)
* [(77) Dlaczego wiadomości są czasami wyświetlane z małym opóźnieniem?](#user-content-faq77)
* [(78) Jak korzystać z harmonogramów?](#user-content-faq78)
* [(79) Jak korzystać z synchronizacji na żądanie (ręcznej)?](#user-content-faq79)
* [~~(80) Jak mogę naprawić błąd 'Unable to load BODYSTRUCTURE'?~~](#user-content-faq80)
* [~~ (81) Czy mogę zaciemnić tło oryginalnej wiadomości w ciemnym motywie? ~~](#user-content-faq81)
* [(82) Czym jest obrazek śledzący?](#user-content-faq82)
* [(84) Do czego służą kontakty lokalne?](#user-content-faq84)
* [(85) Dlaczego tożsamość nie jest dostępna?](#user-content-faq85)
* [~~ (86) Czym są „dodatkowe funkcje prywatności”? ~~](#user-content-faq86)
* [(87) Co oznaczają „nieprawidłowe poświadczenia”?](#user-content-faq87)
* [(88) Jak mogę korzystać z konta Yahoo, AOL lub Sky?](#user-content-faq88)
* [(89) Jak mogę wysyłać tylko zwykły tekst?](#user-content-faq89)
* [(90) Dlaczego niektóre teksty są połączone, chociaż nie są linkami?](#user-content-faq90)
* [~~(91) Czy możesz dodać okresową synchronizację w celu oszczędzania energii baterii?~~](#user-content-faq91)
* [(92) Czy możesz dodać filtrowanie spamu, weryfikację podpisu DKIM i autoryzację SPF?](#user-content-faq92)
* [(93) Czy możesz zezwolić na instalację / przechowywanie danych na zewnętrznych nośnikach pamięci (sdcard)?](#user-content-faq93)
* [(94) Co oznacza czerwony/pomarańczowy pasek na końcu nagłówka?](#user-content-faq94)
* [(95) Dlaczego nie wszystkie aplikacje są wyświetlane przy wyborze załącznika lub obrazu?](#user-content-faq95)
* [(96) Gdzie mogę znaleźć ustawienia IMAP i SMTP?](#user-content-faq96)
* [(97) Czym jest „czyszczenie”?](#user-content-faq97)
* [(98) Dlaczego nadal mogę wybierać kontakty po wyłączeniu uprawnień do kontaktów?](#user-content-faq98)
* [(99) Czy możesz dodać edytor tekstu sformatowanego?](#user-content-faq99)
* [(100) Jak mogę zsynchronizować kategorie Gmail?](#user-content-faq100)
* [(101) Co oznacza niebiesko-pomarańczowa kropka na dole rozmowy?](#user-content-faq101)
* [(102) Jak mogę włączyć automatyczne obracanie zdjęć?](#user-content-faq102)
* [(103) Jak mogę nagrywać audio?](#user-content-faq158)
* [(104) Co muszę wiedzieć o zgłaszaniu błędów?](#user-content-faq104)
* [(105) Jak działa opcja Roaming jak w domu?](#user-content-faq105)
* [(106) Które launchery mogą pokazać licznik z liczbą nieprzeczytanych wiadomości?](#user-content-faq106)
* [Jak używać kolorowych gwiazdek?](#user-content-faq107)
* [~~(108) Czy można trwale usunąć wiadomości z dowolnego folderu?~~](#user-content-faq108)
* [~~ (109) Dlaczego funkcja „wybierz konto” jest dostępna tylko w oficjalnych wersjach? ~~](#user-content-faq109)
* [(110) Dlaczego (niektóre) wiadomości są puste i / lub załączniki są uszkodzone?](#user-content-faq110)
* [(111) Czy OAuth jest wspierany?](#user-content-faq111)
* [(112) Którego dostawcę poczty e-mail polecasz?](#user-content-faq112)
* [(113) Jak działa uwierzytelnianie biometryczne?](#user-content-faq113)
* [(114) Czy możesz dodać import dla ustawień innych aplikacji e-mail?](#user-content-faq114)
* [(115) Czy możesz dodać chipy na adres e-mail?](#user-content-faq115)
* [~~ (116) Jak domyślnie wyświetlać obrazy w wiadomościach od zaufanych nadawców? ~~](#user-content-faq116)
* [(117) Czy możesz mi pomóc przywrócić mój zakup?](#user-content-faq117)
* [(118) Co dokładnie oznacza „Usuń parametry śledzenia”?](#user-content-faq118)
* [~~ (119) Czy możesz dodać kolory do widżetu wspólnej skrzynki odbiorczej? ~~](#user-content-faq119)
* [(120) Dlaczego nowe powiadomienia nie zostały usunięte przy otwieraniu aplikacji?](#user-content-faq120)
* [(121) W jaki sposób wiadomości są pogrupowane w rozmowę?](#user-content-faq121)
* [~~ (122) Dlaczego nazwa / adres e-mail odbiorcy są wyświetlane w kolorze ostrzegawczym? ~~](#user-content-faq122)
* [(123) Co się stanie, gdy FairEmail nie będzie mógł połączyć się z serwerem e-mail?](#user-content-faq123)
* [(124) Dlaczego otrzymuję komunikat „Wiadomość za duża lub zbyt skomplikowana, aby ją wyświetlić”?](#user-content-faq124)
* [(125) Jakie są obecne funkcje eksperymentalne?](#user-content-faq125)
* [(126) Czy podgląd wiadomości można wysyłać do mojego urządzenia do noszenia?](#user-content-faq126)
* [(127) Jak mogę naprawić 'Syntactically invalid HELO argument(s)'?](#user-content-faq127)
* [(128) Jak mogę zresetować zadawane pytania, na przykład w celu wyświetlenia zdjęć?](#user-content-faq128)
* [(129) Czy obsługiwane są ProtonMail, Tutanota?](#user-content-faq129)
* [(130) Co oznacza błąd wiadomości?](#user-content-faq130)
* [(131) Czy możesz zmienić kierunek przesuwania do poprzedniej / następnej wiadomości?](#user-content-faq131)
* [(132) Dlaczego powiadomienia o nowych wiadomościach są ciche?](#user-content-faq132)
* [(133) Dlaczego ActiveSync nie jest obsługiwany?](#user-content-faq133)
* [(134) Czy możesz dodać usuwanie lokalnych wiadomości?](#user-content-faq134)
* [(135) Dlaczego w rozmowach pokazywane są usunięte wiadomości i wersje robocze?](#user-content-faq135)
* [(136) Jak mogę usunąć konto/tożsamość/folder?](#user-content-faq136)
* [(137) Jak mogę zresetować 'Nie pytaj ponownie'?](#user-content-faq137)
* [(138) Can you add calendar/contact/tasks/notes management?](#user-content-faq138)
* [(139) Jak naprawić błąd „Użytkownik jest uwierzytelniony, ale nie ma połączenia”?](#user-content-faq139)
* [(140) Dlaczego tekst wiadomości zawiera dziwne znaki?](#user-content-faq140)
* [(141) Jak mogę naprawić „Folder szkiców jest wymagany do wysyłania wiadomości”?](#user-content-faq141)
* [(142) Jak mogę przechowywać wysłane wiadomości w skrzynce odbiorczej?](#user-content-faq142)
* [~~(143) Czy możesz dodać folder kosza dla kont POP3?~~](#user-content-faq143)
* [(144) Jak mogę nagrywać notatki głosowe?](#user-content-faq144)
* [(145) Jak mogę ustawić dźwięk powiadomienia dla konta, folderu lub nadawcy?](#user-content-faq145)
* [(146) Jak mogę naprawić nieprawidłowe czasy wiadomości?](#user-content-faq146)
* [(147) Co powinienem wiedzieć o wersjach firm/stron trzecich?](#user-content-faq147)
* [(148) Jak mogę używać konta Apple iCloud?](#user-content-faq148)
* [(149) Jak działa widget nieprzeczytanej liczby wiadomości?](#user-content-faq149)
* [(150) Czy możesz dodać anulowanie zaproszeń do kalendarza?](#user-content-faq150)
* [(151) Czy możesz dodać kopię zapasową/przywracania wiadomości?](#user-content-faq151)
* [(152) W jaki sposób mogę wstawić grupę kontaktową?](#user-content-faq152)
* [(153) Dlaczego trwale usunięcie wiadomości Gmaila nie działa?](#user-content-faq153)
* [~~(154) Czy możesz dodać favikony jako zdjęcia kontaktowe?~~](#user-content-faq154)
* [(155) Co to jest plik winmail.dat?](#user-content-faq155)
* [(156) Jak mogę założyć konto Office 365?](#user-content-faq156)
* [(157) Jak mogę utworzyć konto Free.fr?](#user-content-faq157)
* [(158) Którą kamerę / rejestrator audio rekomendujesz?](#user-content-faq158)
* [(159) Czym są listy ochrony przed śledzeniem Disconnect?](#user-content-faq159)
* [(160) Czy możesz dodać trwałe usuwanie wiadomości bez potwierdzenia?](#user-content-faq160)
* [(161) Can you add a setting to change the primary and accent color?](#user-content-faq161)
* [(162) Is IMAP NOTIFY supported?](#user-content-faq162)
* [(163) What is message classification?](#user-content-faq163)
* [(164) Can you add customizable themes?](#user-content-faq164)
* [(165) Is Android Auto supported?](#user-content-faq165)
* [(166) Can I snooze a message across multiple devices?](#user-content-faq166)

[Mam kolejne pytanie.](#user-content-support)

<a name="faq1"></a>
**(1) Jakie uprawnienia są potrzebne i dlaczego**

Wymagane są następujące uprawnienia Androida:

* *Pełny dostęp do sieci* (INTERNET): aby wysyłać i odbierać wiadomości e-mail
* *wyświetl połączenia sieciowe* (ACCESS_NETWORK_STATE): aby monitorować zmiany w łączności z Internetem
* *Uruchom przy starcie* (RECEIVE_BOOT_COMPLETED): aby rozpocząć monitorowanie przy starcie urządzenia
* *usługa pierwszoplanowa* (FOREGROUND_SERVICE): aby uruchomić usługę pierwszoplanową na Android 9 Pie i później, zobacz również następne pytanie
* *zapobiegaj uśpieniu* (WAKE_LOCK): aby urządzenie było wybudzone podczas synchronizacji wiadomości
* *płatności w aplikacji* (BILLING): aby zezwolić na zakupy w aplikacji
* *schedule exact alarm* (SCHEDULE_EXACT_ALARM): to use exact alarm scheduling (Android 12 and later)
* Opcjonalnie: *Dostęp do kontaktów* (READ_CONTACTS): aby automatycznie uzupełniać adresy, wyświetlać zdjęcia kontaktów i [wybierać kontakty](https://developer.android.com/guide/components/intents-common#PickContactDat)
* Opcjonalnie: *przeczytaj zawartość karty SD* (READ_EXTERNAL_STORAGE): aby akceptować pliki z innych, przestarzałych aplikacji, zobacz również [to FAQ](#user-content-faq49)
* Opcjonalnie: *użyj czytnika linii* (USE_FINGERPRINT) i użyj *sprzętu biometrycznego* (USE_BIOMETRIC): aby użyć uwierzytelniania biometrycznego
* Opcjonalnie: *znajdź konta na urządzeniu* (GET_ACCOUNTS): aby wybrać konto podczas używania szybkiej konfiguracji Gmail
* Android 5. Lollipop i wcześniej: *użyj kont na urządzeniu* (USE_CREDENTIALS): aby wybrać konto przy użyciu szybkiej konfiguracji Gmail (nie wymaga późniejszych wersji Androida)
* Android 5.1 Lollipop i wcześniej: *Przeczytaj profil* (READ_PROFILE): aby przeczytać swoją nazwę podczas korzystania z szybkiej konfiguracji Gmail (nie wymaga późniejszych wersji Androida)

[Uprawnienia opcjonalne](https://developer.android.com/training/permissions/requesting) są obsługiwane tylko na Androidzie 6 Marshmallow i później. Na wcześniejszych wersjach Androida zostaniesz poproszony o przyznanie opcjonalnych uprawnień do instalacji FairEmail.

Następujące uprawnienia są potrzebne, aby wyświetlić liczbę nieprzeczytanych wiadomości jako znacznik (patrz również [to FAQ](#user-content-faq106)):

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
* *me.Wszystkie.badger.permission.BADGE_COUNT_READ*
* *me.Wszystkie.badger.permission.BADGE_COUNT_WRITE*

FairEmail będzie przechowywać listę adresów, z których otrzymujesz wiadomości i na które wysyłasz wiadomości i użyje tej listy do sugestii dotyczących kontaktów, gdy FairEmail nie będzie miał przyznanych żadnych uprawnień do kontaktów. Oznacza to, że możesz korzystać z FairEmail bez dostawcy kontaktów Androida (książki adresowej). Pamiętaj, że nadal możesz wybierać kontakty bez przyznawania uprawnień do kontaktów FairEmail, tylko sugerowanie kontaktów nie będzie działać bez uprawnień do kontaktów.

<br />

<a name="faq2"></a>
**(2) Dlaczego wyświetla się stałe powiadomienie?**

Wyświetlane jest powiadomienie stałe o niskim priorytecie na pasku statusu, z liczbą monitorowanych kont i liczbą oczekujących operacji (patrz następne pytanie) aby uniemożliwić Androidowi zabicie usługi, która dba o ciągłe otrzymywanie wiadomości e-mail. Było to [ już konieczne ](https://developer.android.com/reference/android/app/Service.html#startForeground(int,%20android.app.Notification)), ale wraz z wprowadzeniem [ trybu Doze ](https://developer.android.com/training/monitoring-device-state/doze-standby) w Androidzie 6 Marshmallow jest to bardziej niż kiedykolwiek konieczne. Tryb Doze zatrzyma wszystkie aplikacje, gdy ekran jest wyłączony na jakiś czas, chyba że aplikacja uruchomiła usługę pierwszoplanową, która wymaga pokazywania powiadomień na pasku stanu.

Większość, jeśli nie wszystkie inne aplikacje e-mail nie wyświetlają powiadomienia z „efektem ubocznym” polegającym na tym, że nowe wiadomości często nie są zgłaszane lub są opóźnione oraz, że wiadomości nie są wysyłane lub są opóźniane.

Android najpierw pokazuje ikony powiadomień na pasku stanu o wysokim priorytecie i ukrywa ikonę powiadomień FairEmail, jeśli nie ma już miejsca na wyświetlanie ikon. W praktyce oznacza to, że powiadomienie na pasku stanu nie zajmuje miejsca na pasku, chyba że jest dostępna przestrzeń.

Powiadomienia na pasku stanu można wyłączyć za pomocą ustawień powiadomień FairEmail:

* Android 8 Oreo and later: tap the *Receive channel* button and disable the channel via the Android settings (this won't disable new message notifications)
* Android 7 Nougat i wcześniej: włączone * Użyj usługi w tle do synchronizacji wiadomości *, ale pamiętaj, aby przeczytać uwagę pod ustawieniem

Możesz przełączyć się na okresową synchronizację wiadomości w ustawieniach odbioru, aby usunąć powiadomienie, ale pamiętaj, że może to zużywać więcej baterii. Więcej informacji o zużyciu baterii znajdziesz w [tutaj](#user-content-faq39).

Android 8 Oreo może również pokazywać powiadomienie na pasku stanu z tekstem *Aplikacje są uruchomione w tle*. Zobacz [tutaj](https://www.reddit.com/r/Android/comments/7vw7l4/psa_turn_off_background_apps_notification/) jak możesz wyłączyć to powiadomienie.

Niektóre osoby sugerowały użycie [ Firebase Cloud Messaging ](https://firebase.google.com/docs/cloud-messaging/) (FCM) zamiast usługi Android z powiadomieniem na pasku stanu, ale wymagałoby to od dostawców poczty e-mail wysyłania wiadomości FCM lub centralnego serwera, na którym gromadzone są wszystkie wiadomości wysyłające wiadomości FCM. Pierwsze z nich się nie wydarzy, a drugie miałoby znaczący wpływ na prywatność.

Jeśli trafiłeś tutaj, klikając powiadomienie, powinieneś wiedzieć, że następne kliknięcie otworzy wspólną skrzynkę odbiorczą.

<br />

<a name="faq3"></a>
**(3) Czym są operacje i dlaczego są one w toku?**

Powiadomienie na pasku stanu o niskim priorytecie pokazuje liczbę oczekujących operacji, którymi mogą być:

* *add*: dodaj wiadomość do zdalnego folderu
* *move*: przenieś wiadomość do innego zdalnego folderu
* *copy*: kopiuj wiadomość do innego zdalnego folderu
* *fetch*: pobierz wiadomość zmienioną (pushed)
* *delete*: usuń wiadomość ze zdalnego folderu
* *seen*: oznacz wiadomość jako przeczytaną/nieprzeczytaną w zdalnym folderze
* *answered*: oznacz wiadomość jako odpowiedź w zdalnym folderze
* *flag*: dodaj/usuń gwiazdkę w zdalnym folderze
* *keyword*: dodaj/usuń flagę IMAP w zdalnym folderze
* *label*: ustaw / zresetuj etykietę Gmail w zdalnym folderze
* *headers*: pobierz nagłówki wiadomości
* *raw*: pobierz nieprzetworzoną wiadomość
* *body*: pobierz tekst wiadomości
* *attachment*: pobierz załącznik
* *sync*: synchronizuj lokalne i zdalne wiadomości
* *subscribe*: subskrybuj zdalny folder
* *purge*: delete all messages from remote folder
* *send*: wyślij wiadomość
* *exists*: sprawdź, czy wiadomość istnieje
* *rule*: wykonaj regułę na treści
* *expunge*: permanently delete messages

Operacje są przetwarzane tylko wtedy, gdy istnieje połączenie z serwerem e-mail lub podczas ręcznej synchronizacji. Zobacz również to [FAQ](#user-content-faq16).

<br />

<a name="faq4"></a>
**(4) Jak mogę użyć nieprawidłowego certyfikatu bezpieczeństwa / pustego hasła / zwykłego połączenia tekstowego?**

*... Niezaufane... nie w certyfikacie ...*
<br />
*... Nieprawidłowy certyfikat bezpieczeństwa (nie można zweryfikować tożsamości serwera) ...*

Może to być spowodowane używaniem nieprawidłowej nazwy hosta, więc najpierw sprawdź nazwę hosta w zaawansowanych ustawieniach tożsamości/konta (dotknij Ręcznej konfiguracji). Please see the documentation of the email provider about the right host name.

Powinieneś spróbować to naprawić, kontaktując się z dostawcą lub uzyskując ważny certyfikat bezpieczeństwa ponieważ nieprawidłowe certyfikaty bezpieczeństwa są niepewne i umożliwiają [ ataki typu man-in-the-middle ](https://en.wikipedia.org/wiki/Man-in-the-middle_attack). Jeśli pieniądze są przeszkodą, możesz otrzymać darmowe certyfikaty bezpieczeństwa od [Let’s Encrypt](https://letsencrypt.org).

Szybkim, ale niebezpiecznym rozwiązaniem (nie zalecanym), jest włączenie *Niezabezpieczonych połączeń* w zaawansowanych ustawieniach identyfikacyjnych (menu nawigacji, dotknij *Ustawienia*, dotknij *Ustawienia ręczne*, dotknij *Tożsamości*, dotknij tożsamość, dotknij *Zaawansowane*).

Alternatively, you can accept the fingerprint of invalid server certificates like this:

1. Make sure you are using a trusted internet connection (no public Wi-Fi networks, etc)
1. Go to the setup screen via the navigation menu (swipe from the left side inwards)
1. Dotknij Ręczna konfiguracja, dotknij Konta/Tożsamości i dotknij wadliwego konta i tożsamości
1. Check/save the account and identity
1. Tick the checkbox below the error message and save again

This will "pin" the server certificate to prevent man-in-the-middle attacks.

Pamiętaj, że starsze wersje Androida mogą nie rozpoznawać nowszych urzędów certyfikacji, takich jak Let's Encrypt, powodując, że połączenia są uważane za niebezpieczne, patrz także [ tutaj ](https://developer.android.com/training/articles/security-ssl).

<br />

*Nie znaleziono kotwicy zaufania dla ścieżki certyfikacji*

*... java.security.cert.CertPathValidatorException: Trust anchor for certification path not found ...* oznacza, że ​​domyślny menedżer zaufania Androida nie mógł zweryfikować łańcucha certyfikatów serwera.

This could be due to the root certificate not being installed on your device or because intermediate certificates are missing, for example because the email server didn't send them.

You can fix the first problem by downloading and installing the root certificate from the website of the provider of the certificate.

The second problem should be fixed by changing the server configuration or by importing the intermediate certificates on your device.

You can pin the certificate too, see above.

<br />

*Puste hasło*

Your username is likely easily guessed, so this is pretty insecure, unless the SMTP server is available via a restricted local network or a VPN only.

*Zwykłe połączenie tekstowe*

Twoja nazwa użytkownika i hasło oraz wszystkie wiadomości będą wysyłane i odbierane w postaci niezaszyfrowanej, co jest ** bardzo niepewne ** ponieważ [ atak typu man-in-the-middle ](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) jest bardzo prosty na nieszyfrowanym połączeniu.

Jeśli nadal chcesz używać nieprawidłowego certyfikatu bezpieczeństwa, pustego hasła lub połączenia zwykłego tekstu musisz włączyć niezabezpieczone połączenia w ustawieniach konta i/lub tożsamości. STARTTLS powinien być wybrany dla połączeń tekstowych. Jeśli włączysz niezabezpieczone połączenia, powinieneś łączyć się tylko za pośrednictwem prywatnych, zaufanych sieci i nigdy za pośrednictwem sieci publicznych, takich jakie są oferowane w hotelach, portach lotniczych itp.

<br />

<a name="faq5"></a>
**(5) Jak mogę dostosować widok wiadomości?**

W menu trzech kropek możesz włączyć, wyłączyć lub wybrać:

* *rozmiar tekstu*: dla trzech różnych rozmiarów czcionki
* *widok kompaktowy*: dla bardziej zwartych elementów wiadomości i mniejszej czcionki tekstowej

In the display section of the settings you can enable or disable for example:

* *Wspólna skrzynka*: aby wyłączyć wspólną skrzynkę odbiorczą i wyświetlić listę folderów wybranych dla wspólnej skrzynki odbiorczej
* *Tabular style*: to show a linear list instead of cards
* *Grupuj według daty*: pokaż nagłówek daty powyżej wiadomości z tą samą datą
* *Konwersacje w wątkach*: aby wyłączyć wątek konwersacji i wyświetlić indywidualne wiadomości
* *Pokaż pasek akcji konwersacji*: aby wyłączyć dolny pasek nawigacji
* *Highlight color*: to select a color for the sender of unread messages
* *Pokaż zdjęcia kontaktu*: aby ukryć zdjęcia kontaktu
* *Pokaż nazwy i adresy e-mail*: aby pokazać nazwy lub pokazać nazwy i adresy e-mail
* *Temat wyświetlaj kursywą*: aby wyświetlić temat wiadomości jako zwykły tekst
* *Pokaż gwiazdki*: aby ukryć gwiazdki (ulubione)
* *Show message preview*: to show 1-4 lines of the message text
* *Domyślnie pokazuj szczegóły adresu*: aby rozszerzyć sekcję adresów domyślnie
* *Automatycznie pokaż oryginalną wiadomość dla znanych kontaktów*: aby automatycznie wyświetlić oryginalne wiadomości dla kontaktów na twoim urządzeniu, przeczytaj [to FAQ](#user-content-faq35)
* *Automatycznie pokazuj obrazy dla znanych kontaktów*: aby automatycznie pokazać zdjęcia dla kontaktów na Twoim urządzeniu, przeczytaj [to FAQ](#user-content-faq35)

Pamiętaj, że podgląd wiadomości można wyświetlić tylko wtedy, gdy tekst wiadomości został pobrany. Większe wiadomości nie są pobierane (domyślnie) w sieciach komórkowych (zazwyczaj mobilnych). Możesz to zmienić w ustawieniach połączenia.

Niektórzy ludzie proszą:

* aby pokazać temat pogrubiony, ale pogrubienie jest już używane do podświetlenia nieprzeczytanych wiadomości
* aby przesunąć gwiazdkę w lewo, ale o wiele łatwiej jest operować gwiazdą po prawej stronie

<br />

<a name="faq6"></a>
**(6) Jak mogę zalogować się do Gmail / G suite?**

If you use the Play store or GitHub version of FairEmail, you can use the quick setup wizard to easily setup a Gmail account and identity. The Gmail quick setup wizard is not available for third party builds, like the F-Droid build because Google approved the use of OAuth for official builds only.

If you don't want to use or can't use an on-device Google account, for example on recent Huawei devices, you can either enable access for "less secure apps" and use your account password (not advised) or enable two factor authentication and use an app specific password. To use a password you'll need to set up an account and identity via the manual setup instead of via the quick setup wizard.

**Important**: sometimes Google issues this alert:

*[ALERT] Please log in via your web browser: https://support.google.com/mail/accounts/answer/78754 (Failure)*

This Google security check is triggered more often with *less secure apps* enabled, less with an app password, and hardly when using an on-device account (OAuth).

Zobacz [ten FAQ](#user-content-faq111), aby dowiedzieć się dlaczego można używać tylko kont na urządzeniu.

Pamiętaj, że hasło jest wymagane w przypadku włączenia uwierzytelniania dwuskładnikowego. After enabling two factor authentication there will be this error message:

*[ALERT] Application-specific password required: https://support.google.com/mail/accounts/answer/185833 (Failure)*

<br />

*Hasło specyficzne dla aplikacji*

Zobacz [tutaj](https://support.google.com/accounts/answer/185833) jak wygenerować hasło dla danej aplikacji.

<br />

*Włącz "Mniej bezpieczne aplikacje"*

**Ważne**: użycie tej metody nie jest zalecane, ponieważ jest mniej wiarygodne.

**Ważne**: Konta Gsuite autoryzowane z nazwą użytkownika/hasłem przestaną działać [w najbliższej przyszłości](https://gsuiteupdates.googleblog.com/2019/12/less-secure-apps-oauth-google-username-password-incorrect.html).

Zobacz [tutaj](https://support.google.com/accounts/answer/6010255) jak włączyć "mniej bezpieczne aplikacje" lub przejdź [bezpośrednio do ustawienia](https://www.google.com/settings/security/lesssecureapps).

Jeśli korzystasz z wielu kont Gmail, upewnij się, że zmieniłeś ustawienie „mniej bezpiecznych aplikacji” dla odpowiednich kont.

Pamiętaj, że musisz opuścić ekran ustawień „mniej bezpiecznych aplikacji” za pomocą strzałki wstecz, aby zastosować ustawienie.

Jeśli używasz tej metody, powinieneś użyć [silnego hasła](https://en.wikipedia.org/wiki/Password_strength) dla swojego konta Gmail, co i tak jest dobrym pomysłem. Zauważ, że samo użycie protokołu IMAP [ standardowego ](https://tools.ietf.org/html/rfc3501) nie jest mniej bezpieczne.

Gdy „mniej bezpieczne aplikacje” nie są włączone, pojawi się błąd * Uwierzytelnianie nie powiodło się - nieprawidłowe dane logowania * dla kont (IMAP) i * Nazwa użytkownika i hasło nie są akceptowane * dla tożsamości (SMTP).

<br />

*Główne*

Możesz otrzymać ostrzeżenie "*Zaloguj się przez przeglądarkę internetową*". Dzieje się tak, gdy Google uzna, że sieć łącząca Cię z Internetem (może to być VPN) jest niebezpieczna. Można temu zapobiec za pomocą kreatora konfiguracji poczty Gmail lub określonego hasła aplikacji.

Instrukcje Google znajdują się [ tutaj ](https://support.google.com/mail/answer/7126229) i [ tutaj ](https://support.google.com/mail/accounts/answer/78754) w celu rozwiązania problemów.

<br />

<a name="faq7"></a>
**(7) Dlaczego wysłane wiadomości nie pojawiają się (bezpośrednio) w folderze Wysłane?**

Wysłane wiadomości są zwykle przenoszone ze skrzynki nadawczej do folderu Wysłane, gdy tylko dostawca doda wysłane wiadomości do folderu. Wymaga to wybrania folderu Wysłane w ustawieniach konta i ustawienia synchronizacji folderu Wysłane.

Niektórzy dostawcy nie śledzą wysłanych wiadomości lub używany serwer SMTP może nie być powiązany z dostawcą. W takich przypadkach FairEmail automatycznie doda wysłane wiadomości do folderu Wysłane podczas synchronizacji folderu, co nastąpi po wysłaniu wiadomości. Spowoduje to dodatkowy ruch internetowy.

~~ Jeśli tak się nie stanie, twój dostawca może nie śledzić wysłanych wiadomości lub możesz używać serwera SMTP niezwiązanego z dostawcą. ~~ ~~ W takich przypadkach możesz włączyć zaawansowane ustawienie tożsamości * Przechowuj wysłane wiadomości *, aby umożliwić FairEmail dodawanie wysłanych wiadomości do folderu Wysłane zaraz po wysłaniu wiadomości. ~~ ~~ Pamiętaj, że włączenie tego ustawienia może spowodować zduplikowanie wiadomości, jeśli Twój dostawca doda również wysłane wiadomości do folderu Wysłane. ~~ ~~ Uważaj również, ponieważ włączenie tego ustawienia spowoduje dodatkowe wykorzystanie danych, szczególnie podczas wysyłania wiadomości z dużymi załącznikami. ~~

~~ Jeśli wysłane wiadomości w skrzynce nadawczej nie zostaną znalezione w folderze Wysłane podczas pełnej synchronizacji, zostaną również przeniesione ze skrzynki nadawczej do folderu Wysłane. ~~ ~~ Pełna synchronizacja ma miejsce przy ponownym połączeniu z serwerem lub podczas synchronizacji okresowej lub ręcznej. ~~ ~~ Prawdopodobnie będziesz chciał włączyć ustawienie zaawansowane * Przechowuj wysłane wiadomości *, aby szybciej przenieść wiadomości do wysłanego folderu. ~~

<br />

<a name="faq8"></a>
**(8) Czy mogę korzystać z konta Microsoft Exchange?**

Protokół Microsoft Exchange Web Services [jest stopniowo wycofywany](https://techcommunity.microsoft.com/t5/Exchange-Team-Blog/Upcoming-changes-to-Exchange-Web-Services-EWS-API-for-Office-365/ba-p/608055). So, it makes little sense to add this protocol anymore.

Możesz użyć konta Microsoft Exchange, jeśli jest ono dostępne za pośrednictwem protokołu IMAP, co w większości przypadków ma miejsce. Zobacz [tutaj](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793) aby uzyskać więcej informacji.

Zauważ, że opis FairEmail zaczyna się od uwagi, iż niestandardowe protokoły, takie jak Microsoft Exchange Web Services i Microsoft ActiveSync nie są obsługiwane.

Zobacz [tutaj](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040), aby uzyskać dokumentację Microsoft dotyczącą konfiguracji klienta e-mail. Istnieje również rozdział dotyczący wspólnych błędów i rozwiązań w zakresie połączeń.

Niektóre starsze wersje serwera Exchange zawierają błąd powodujący pustą wiadomość i uszkodzenie załączników. Obejście problemu znajduje się tutaj [FAQ](#user-content-faq110).

Zobacz [FAQ](#user-content-faq133) o wsparciu ActiveSync.

Zobacz tutaj [FAQ](#user-content-faq111) na temat wsparcia OAuth.

<br />

<a name="faq9"></a>
**(9) Czym są tożsamości / jak dodać alias?**

Tożsamości reprezentują adresy e-mail, z których wysyłasz * z * za pośrednictwem serwera e-mail (SMTP).

Niektórzy dostawcy dopuszczają posiadanie wielu aliasów. Możesz je skonfigurować, ustawiając pole adresu e-mail dodatkowej tożsamości na adres aliasu i ustawienie pola nazwy użytkownika na główny adres e-mail.

Pamiętaj, że możesz skopiować tożsamość przez długie przytrzymanie.

Alternatywnie możesz włączyć opcję * Zezwól na edycję adresu nadawcy * w ustawieniach zaawansowanych istniejącej tożsamości, aby edytować nazwę użytkownika podczas tworzenia nowej wiadomości, jeśli twój dostawca na to pozwala.

FairEmail automatycznie zaktualizuje hasła powiązanych tożsamości podczas aktualizacji hasła powiązanego konta lub powiązanej tożsamości.

Zobacz [ten FAQ](#user-content-faq33), jak edytować nazwy użytkownika adresów e-mail.

<br />

<a name="faq10"></a>
**~~(10) Co oznacza „UIDPLUS nieobsługiwany”?~~**

~~ Komunikat o błędzie * UIDPLUS nieobsługiwany * oznacza, że ​​Twój dostawca poczty e-mail nie zapewnia rozszerzenia IMAP [ UIDPLUS ](https://tools.ietf.org/html/rfc4315). To rozszerzenie IMAP jest wymagane do wdrożenia dwukierunkowej synchronizacji, która nie jest funkcją opcjonalną. Dopóki Twój dostawca nie włączy tego rozszerzenia, nie możesz używać FairEmail dla tego dostawcy. ~~

<br />

<a name="faq11"></a>
**~~(11) Dlaczego POP nie jest wspierany?~**

~~Poza tym, każdy przyzwoity dostawca poczty elektronicznej obsługuje teraz [IMAP](https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol)~~ ~~używanie [POP](https://en.wikipedia.org/wiki/Post_Office_Protocol) spowoduje niepotrzebne dodatkowe zużycie baterii i opóźni powiadomienia o nowych wiadomościach.~~ ~~Co więcej, POP jest nieodpowiednie dla dwukierunkowej synchronizacji i częściej ludzie czytają i piszą wiadomości na różnych urządzeniach.~~

~~Zasadniczo, POP obsługuje tylko pobieranie i usuwanie wiadomości z skrzynki odbiorczej.~~ ~~Więc, częste operacje takie jak ustawianie atrybutów wiadomości (przeczytana, oznaczona gwiazdką, odpowiedziana, itp.), dodawanie (kopia zapasowa) i przenoszenie wiadomości nie są możliwe.~~

~~Zobacz również [co pisze o tym Google](https://support.google.com/mail/answer/7104828).~~

~~Na przykład [Gmail może importować wiadomości](https://support.google.com/mail/answer/21289) z innego konta POP, ~~ ~~które może być użyte jako rozwiązanie dodatkowe, gdy dostawca nie obsługuje IMAP.~~

~~tl;dr; rozważ przełączenie na IMAP.~~

<br />

<a name="faq12"></a>
**(12) Jak działa szyfrowanie/deszyfrowanie?**

Communication with email servers is always encrypted, unless you explicitly turned this off. This question is about optional end-to-end encryption with PGP or S/MIME. The sender and recipient should first agree on this and exchange signed messages to transfer their public key to be able to send encrypted messages.

<br />

*Główne*

[Zobacz tutaj](https://en.wikipedia.org/wiki/Public-key_cryptography) jak działa szyfrowanie kluczami publicznymi/prywatnymi.

Szyfrowanie w skrócie:

* **Wychodzące** wiadomości są zaszyfrowane przy użyciu **klucza publicznego** odbiorcy
* **Przychodzące** wiadomości są odszyfrowane przy użyciu **klucza prywatnego** odbiorcy

Podpisywanie w skrócie:

* **Wychodzące** wiadomości są podpisane przy użyciu **klucza prywatnego** nadawcy
* **Przychodzące** wiadomości są sprawdzane przy użyciu **klucza prywatnego** nadawcy

Aby podpisać/zaszyfrować wiadomość, po prostu wybierz odpowiednią metodę w oknie dialogowym wysyłania. Zawsze możesz otworzyć okno dialogowe wysyłania za pomocą menu trzech kropek, na wypadek, gdybyś wybrał wcześniej opcję * Nie pokazuj ponownie *.

Aby zweryfikować podpis lub odszyfrować otrzymaną wiadomość, otwórz wiadomość i dotknij ikony gestu lub kłódki tuż poniżej paska akcji wiadomości.

Przy pierwszym wysłaniu podpisanej/zaszyfrowanej wiadomości możesz zostać poproszony o klucz do podpisu. Następnym razem FairEmail automatycznie zapisze wybrany klucz w użytej tożsamości. Jeśli musisz zresetować klucz, po prostu zapisz tożsamość lub naciśnij długo tożsamość na liście tożsamości i wybierz * Resetuj klucz *. Wybrany klucz jest widoczny na liście tożsamości. Jeśli konieczne jest wybranie klucza indywidualnie dla każdego przypadku, możesz tworzyć wiele tożsamości dla tego samego konta z tym samym adresem e-mail.

In the encryption settings you can select the default encryption method (PGP or S/MIME), enable *Sign by default*, *Encrypt by default* and *Automatically decrypt messages*, but be aware that automatic decryption is not possible if user interaction is required, like selecting a key or reading a security token.

Tekst wiadomości/załączniki do zaszyfrowania oraz tekst wiadomości/załączniki odszyfrowane są przechowywane tylko lokalnie i nigdy nie zostaną dodane do zdalnego serwera. Jeśli chcesz cofnąć deszyfrowanie, możesz użyć pozycji menu * Resynchronizuj * w menu z trzema kropkami na pasku akcji wiadomości.

<br />

*PGP*

Najpierw musisz zainstalować i skonfigurować [OpenKeychain](https://f-droid.org/en/packages/org.sufficientlysecure.keychain/). FairEmail został przetestowany z OpenKeychain w wersji 5.4. Nowsze wersje będą kompatybilne, ale wcześniejsze wersje mogą nie być.

** Ważne **: wiadomo, że aplikacja OpenKeychain (po cichu) ulega awarii, gdy aplikacja wywołująca (FairEmail) nie jest jeszcze autoryzowana i otrzymuje istniejący klucz publiczny. Można to obejść, próbując wysłać podpisaną/zaszyfrowaną wiadomość do nadawcy z nieznanym kluczem publicznym.

** Ważne **: jeśli aplikacja OpenKeychain nie może (już) znaleźć klucza, konieczne może być zresetowanie wcześniej wybranego klucza. Można to zrobić przez długie naciśnięcie identyfikatora na liście tożsamości (Ustawienia, dotknij Ręczna konfiguracja, dotknij Tożsamości).

** Ważne **: aby aplikacje takie jak FairEmail niezawodnie łączyły się z usługą OpenKeychain w celu szyfrowania/deszyfrowania wiadomości, może być konieczne wyłączenie optymalizacji baterii w aplikacji OpenKeychain.

**Important**: the OpenKeychain app reportedly needs contacts permission to work correctly.

** Ważne **: w niektórych wersjach/urządzeniach z Androidem konieczne jest włączenie * Pokaż wyskakujące okna podczas pracy w tle * w dodatkowych uprawnieniach ustawień aplikacji na Androida aplikacji OpenKeychain. Bez tego pozwolenia szkic zostanie zapisany, ale wyskakujące okno OpenKeychain w celu potwierdzenia/wyboru może się nie pojawić.

FairEmail will send the [Autocrypt](https://autocrypt.org/) header for use by other email clients, but only for signed and encrypted messages because too many email servers have problems with the often long Autocrypt header. Note that the most secure way to start an encrypted email exchange is by sending signed messages first. Received Autocrypt headers will be sent to the OpenKeychain app for storage on verifying a signature or decrypting a message.

Although this shouldn't be necessary for most email clients, you can attach your public key to a message and if you use *.key* as extension, the mime type will correctly be *application/pgp-keys*.

Ze względów bezpieczeństwa cała obsługa klucza jest delegowana do aplikacji OpenKey chain. Oznacza to również, że FairEmail nie przechowuje kluczy PGP.

Obsługiwane są wbudowane PGP w odebranych wiadomościach, ale wbudowane podpisy PGP i wbudowane PGP w wiadomościach wychodzących nie są obsługiwane, zobacz [ tutaj ](https://josefsson.org/inline-openpgp-considered-harmful.html) o tym, dlaczego.

Wiadomości tylko podpisane lub zaszyfrowane nie są dobrym pomysłem, zobacz tutaj, dlaczego:

* [Rozważania OpenPGP Część I](https://k9mail.github.io/2016/11/24/OpenPGP-Considerations-Part-I.html)
* [Rozważania OpenPGP Część II](https://k9mail.github.io/2017/01/30/OpenPGP-Considerations-Part-II.html)
* [Rozważania OpenPGP Część III Auto-szyfrowanie](https://k9mail.github.io/2018/02/26/OpenPGP-Considerations-Part-III-Autocrypt.html)

Wiadomości 'tylko podpisane' są obsługiwane, wiadomości 'tylko szyfrowane' nie są obsługiwane.

Częste błędy:

* *No key*: there is no PGP key available for one of the listed email addresses
* *Missing key for encryption*: there is probably a key selected in FairEmail that does not exist in the OpenKeychain app anymore. Resetting the key (see above) will probably fix this problem.
* *Key for signature verification is missing*: the public key for the sender is not available in the OpenKeychain app. This can also be caused by Autocrypt being disabled in the encryption settings or by the Autocrypt header not being sent.

<br />

*S/MIME*

Szyfrowanie wiadomości wymaga klucza/y publicznego odbiorcy (-ów). Podpisanie wiadomości wymaga klucza prywatnego.

Klucze prywatne są przechowywane przez system Android i można je importować za pomocą zaawansowanych ustawień zabezpieczeń systemu Android. There is a shortcut (button) for this in the encryption settings. Android poprosi Cię o ustawienie kodu PIN, wzoru lub hasła, jeśli wcześniej tego nie zrobiłeś. Jeśli masz urządzenie Nokia z Androidem 9, [przeczytaj to najpierw](https://nokiamob.net/2019/08/10/a-bug-prevents-nokia-1-owners-from-unlocking-their-screen-even-with-right-pin-pattern/).

Pamiętaj, że certyfikaty mogą zawierać wiele kluczy do różnych celów, na przykład do uwierzytelniania, szyfrowania i podpisywania. Android importuje tylko pierwszy klucz, więc aby zaimportować wszystkie klucze, certyfikat należy najpierw podzielić. Nie jest to proste i zaleca się, aby poprosić dostawcę certyfikatu o wsparcie.

Note that S/MIME signing with other algorithms than RSA is supported, but be aware that other email clients might not support this. S/MIME encryption is possible with asymmetric algorithms only, which means in practice using RSA.

Domyślną metodą szyfrowania jest PGP, ale następnym razem ostatnia używana metoda szyfrowania zostanie zapamiętana dla wybranej tożsamości. You can long press on the send button to change the encryption method for an identity. If you use both PGP and S/MIME encryption for the same email address, it might be useful to copy the identity, so you can change the encryption method by selecting one of the two identities. You can long press an identity in the list of identities (via manual setup in the main setup screen) to copy an identity.

Aby zezwolić na różne klucze prywatne dla tego samego adresu e-mail, FairEmail zawsze pozwoli Ci wybrać klucz, gdy istnieje wiele tożsamości z tym samym adresem e-mail dla tego samego konta.

Public keys are stored by FairEmail and can be imported when verifying a signature for the first time or via the encryption settings (PEM or DER format).

FairEmail weryfikuje zarówno podpis, jak i cały łańcuch certyfikatów.

Częste błędy:

* * No certificate found matching targetContraints*: prawdopodobnie oznacza to, że używasz starej wersji FairEmail
* *unable to find valid certification path to requested target *: zasadniczo oznacza to, że nie znaleziono jednego lub więcej certyfikatów pośrednich lub głównych
* *Private key does not match any encryption keys*: wybrany klucz nie może być użyty do odszyfrowania wiadomości, prawdopodobnie dlatego, że jest to nieprawidłowy klucz
* *No private key*: nie wybrano żadnego certyfikatu lub żaden certyfikat nie był dostępny w magazynie kluczy Androida

W przypadku gdy łańcuch certyfikatów jest nieprawidłowy, możesz kliknąć przycisk informacyjny aby wyświetlić wszystkie certyfikaty. Po szczegółach dotyczących certyfikatu wyświetlany jest wystawca lub „selfSign”. Certyfikat jest samopodpisany, gdy podmiot i wystawca są tacy sami. Certyfikaty z urzędu certyfikacji (CA) są oznaczone „[ keyCertSign ](https://tools.ietf.org/html/rfc5280#section-4.2.1.3)”. Certyfikaty znalezione w magazynie kluczy Androida są oznaczone „Android”.

Prawidłowy łańcuch wygląda tak:

```
Your certificate > zero or more intermediate certificates > CA (root) certificate marked with "Android"
```

Pamiętaj, że łańcuch certyfikatów zawsze będzie nieprawidłowy, jeśli nie będzie można znaleźć certyfikatu kotwicy(anchor) w magazynie kluczy Androida, co jest podstawą weryfikacji certyfikatu S / MIME.

Zobacz [ tutaj ](https://support.google.com/pixelphone/answer/2844832?hl=en), jak importować certyfikaty do magazynu kluczy Androida.

Korzystanie z wygasłych kluczy, wbudowanych zaszyfrowanych / podpisanych wiadomości i sprzętowych tokenów zabezpieczających nie jest obsługiwane.

Jeśli szukasz darmowego (testowego) certyfikatu S/MIME, zapoznaj się z opcjami [ tutaj ](http://kb.mozillazine.org/Getting_an_SMIME_certificate). [ Przeczytaj to najpierw ](https://davidroessli.com/logs/2019/09/free-smime-certificates-in-2019/#update20191219) jeśli chcesz poprosić o certyfikat S/MIME Actalis.

Jak wyodrębnić klucz publiczny z certyfikatu S/MIME:

```
openssl pkcs12 -in filename.pfx/p12 -clcerts -nokeys -out cert.pem
```

Możesz dekodować podpisy S/MIME, itp., [tutaj](https://lapo.it/asn1js/).

<br />

*pretty Easy privacy*

There is still [no approved standard](https://tools.ietf.org/id/draft-birk-pep-00.html) for pretty Easy privacy (p≡p) and not many people are using it.

However, FairEmail can send and receive PGP encrypted messages, which are compatible with p≡p. Also, FairEmail understands incoming p≡p messages since version 1.1519, so the encrypted subject will be shown and the embedded message text will be shown more nicely.

<br />

Podpisywanie/szyfrowanie S/MIME jest funkcją pro, ale wszystkie inne operacje PGP i S/MIME są do użytku darmowego.

<br />

<a name="faq13"></a>
**(13) Jak działa wyszukiwanie na urządzeniu/serwerze?**

Możesz rozpocząć wyszukiwanie wiadomości od nadawcy (od), odbiorcy (do, DW, UDW), tematu, słów kluczowych lub tekstu wiadomości, używając ikony lupy na pasku akcji folderu. Możesz także wyszukiwać w dowolnej aplikacji, wybierając * Wyszukaj e-mail * w menu podręcznym kopiuj/wklej.

Wyszukiwanie we wspólnej skrzynce odbiorczej spowoduje wyszukiwanie we wszystkich folderach wszystkich kont. Wyszukiwanie na liście folderów spowoduje wyszukiwanie tylko na powiązanym koncie a wyszukiwanie w folderze spowoduje wyszukiwanie tylko w tym folderze.

Wiadomości będą najpierw wyszukiwane na urządzeniu. Na dole pojawi się przycisk akcji z ikoną wyszukiwania ponownie, aby kontynuować wyszukiwanie na serwerze. Możesz wybrać folder, w którym chcesz kontynuować wyszukiwanie.

Protokół IMAP nie wspiera wyszukiwania w więcej niż jednym folderze jednocześnie. Wyszukiwanie na serwerze jest kosztowną operacją, dlatego nie można wybrać wielu folderów.

Wyszukiwanie lokalnych wiadomości nie rozróżnia wielkości liter i częściowego tekstu. Tekst wiadomości lokalnej (na urządzeniu) nie będzie przeszukiwany, jeśli tekst wiadomości nie został jeszcze pobrany. Wyszukiwanie na serwerze może być wrażliwe na wielkość liter lub niewrażliwe na wielkość liter i może zawierać częściowy tekst lub całe słowa, w zależności od dostawcy.

Niektóre serwery nie obsługują wyszukiwania w tekście wiadomości, gdy istnieje duża liczba wiadomości. W tym przypadku istnieje możliwość wyłączenia wyszukiwania w tekście wiadomości.

It is possible to use Gmail search operators by prefixing a search command with *raw:*. If you configured just one Gmail account, you can start a raw search directly on the server by searching from the unified inbox. If you configured multiple Gmail accounts, you'll first need to navigate to the folder list or the archive (all messages) folder of the Gmail account you want to search in. Please [see here](https://support.google.com/mail/answer/7190) for the possible search operators. For example:

`
raw:larger:10M`

Searching through a large number of messages on the device is not very fast because of two limitations:

* [ sqlite ](https://www.sqlite.org/), silnik bazy danych Androida ma limit wielkości rekordów, uniemożliwiający przechowywanie tekstów wiadomości w bazie danych
* Aplikacje na Androida mają ograniczoną pamięć do pracy, nawet jeśli urządzenie ma wystarczająco dużo dostępnej pamięci

Oznacza to, że wyszukiwanie tekstu wiadomości wymaga, aby pliki zawierające teksty wiadomości były otwierane jeden po drugim aby sprawdzić, czy szukany tekst jest zawarty w pliku, co jest stosunkowo długim procesem.

W ustawieniach *Różne* możesz włączyć *Utwórz indeks wyszukiwania *, aby znacznie zwiększyć szybkość wyszukiwania na urządzeniu, ale pamiętaj, że zwiększy to zużycie baterii i przestrzeni dyskowej. Indeks wyszukiwania opiera się na słowach, więc wyszukiwanie fragmentów tekstu nie jest możliwe. Wyszukiwanie przy użyciu indeksu wyszukiwania jest domyślnie ORAZ, więc wyszukiwanie np.*czerwone jabłko* spowoduje wyszukanie słów "czerwone" ORAZ "jabłko". Słowa oddzielone przecinkami powodują wyszukanie LUB, więc na przykład * jabłko, banan * wyszuka "jabłko" LUB "banan". Obydwa działania można łączyć, więc wyszukiwanie *czerwone jabłko, banan * spowoduje wyszukanie "czerwone jabłko" LUB "banan". Korzystanie z indeksu wyszukiwania jest funkcją pro.

From version 1.1315 it is possible to use search expressions like this:

```
apple +banana -cherry ?nuts
```

This will result in searching like this:

```
("apple" AND "banana" AND NOT "cherry") OR "nuts"
```

Search expressions can be used for searching on the device via the search index and for searching on the email server, but not for searching on the device without search index for performance reasons.

Searching on the device is a free feature, using the search index and searching on the server is a pro feature. Note that you can download as many messages to your device as you like. The easiest way is to use the menu item *Fetch more messages* in the three-dots menu of the start screen.

<br />

<a name="faq14"></a>
**(14) Jak mogę utworzyć konto Outlook / Live / Hotmail?**

Konto Outlook / Live / Hotmail można skonfigurować za pomocą szybkiego kreatora konfiguracji i wybraniu *Outlook*.

W przypadku użycia konta Outlook, Live lub Hotmail z uwierzytelnianiem dwuskładnikowym, musisz utworzyć hasło aplikacji. Szczegóły znajdziesz [tutaj](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification).

Zobacz [tutaj](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) instrukcje Microsoftu.

For setting up an Office 365 account, please see [this FAQ](#user-content-faq156).

<br />

<a name="faq15"></a>
**(15) Dlaczego tekst wiadomości wciąż się ładuje?**

Nagłówek i treść wiadomości są pobierane oddzielnie od serwera. The message text of larger messages is not being pre-fetched on metered connections and will be fetched on demand on expanding a message. The message text will keep loading if there is no connection to the account, see also the next question, or if there other operations, like synchronizing messages, are being executed.

Możesz sprawdzić listę kont i folderów pod kątem stanu kont i folderów (zobacz legendę na temat znaczenia ikon) oraz listę operacji dostępną w głównym menu nawigacyjnym dla operacji oczekujących (zobacz ten [FAQ ](#user-content-faq3)) aby sprawdzić znaczenie operacji.

If FairEmail is holding off because of prior connectivity issues, please see [this FAQ](#user-content-faq123), you can force synchronization via the three dots menu.

W ustawieniach odbierania można ustawić maksymalny rozmiar dla automatycznego pobierania wiadomości przy połączeniach taryfowych.

Łączność komórkowa jest prawie zawsze taryfowa i niektóre (płatne) hotspoty Wi-Fi również.

<br />

<a name="faq16"></a>
**(16) Dlaczego wiadomości nie są synchronizowane?**

Możliwe przyczyny braku synchronizacji wiadomości (wysłanych lub odebranych) to:

* Konto lub folder(y) nie są ustawione do synchronizacji
* Ustawiono zbyt niską liczbę dni do synchronizacji wiadomości
* Brak użytecznego połączenia z Internetem
* Serwer e-mail jest tymczasowo nieosiągalny
* Android zatrzymał usługę synchronizacji

A zatem sprawdź swoje konto i ustawienia folderów i sprawdź, czy konta/foldery są połączone (zobacz legendę w menu nawigacyjnym dla znaczenia ikon).

Jeśli są jakiekolwiek komunikaty o błędach, zobacz [ten FAQ](#user-content-faq22).

Na niektórych urządzeniach, gdzie istnieje wiele aplikacji konkurujących o pamięć, Android, jako ostateczność, może zatrzymać usługę synchronizacji.

Niektóre wersje Androida zbyt agresywnie zatrzymują aplikacje i usługi. Zobacz [tę dedykowaną stronę](https://dontkillmyapp.com/) i [ten problem z Androidem](https://issuetracker.google.com/issues/122098785), aby uzyskać więcej informacji.

Wyłączenie optymalizacji baterii (etap 3 konfiguracji) zmniejsza szansę, że Android zatrzyma usługę synchronizacji.

In case of successive connection errors, FairEmail will hold off increasingly longer to not drain the battery of your device. This is described in [this FAQ](#user-content-faq123).

<br />

<a name="faq17"></a>
**~(17) Dlaczego ręczna synchronizacja nie działa?~**

~~ Jeśli menu * Synchronizuj teraz * jest przyciemnione, nie ma połączenia z kontem. ~~

~~Zobacz poprzednie pytanie, aby uzyskać więcej informacji.~~

<br />

<a name="faq18"></a>
**(18) Dlaczego podgląd wiadomości nie zawsze jest wyświetlany?**

Podgląd tekstu wiadomości nie może być wyświetlany, jeśli treść wiadomości nie została jeszcze pobrana. Zobacz również [ten FAQ](#user-content-faq15).

<br />

<a name="faq19"></a>
**(19) Dlaczego funkcje pro są tak drogie?**

First of all, **FairEmail is basically free to use** and only some advanced features need to be purchased.

Zuerst, **FairEmail ist grundsätzlich kostenlos** und nur einige erweiterte Funktionen müssen gekauft werden.

Tout d'abord, **FairEmail est au fond gratuit** et seulement quelques fonctionnalités avancées doivent être achetés.

Please see the Play store description of the app or [see here](https://email.faircode.eu/#pro) for a complete list of pro features.

Właściwe pytanie brzmi: „* dlaczego jest tyle podatków i opłat? *”:

* VAT: 25 % (w zależności od kraju)
* Opłata Google: 30 %
* Podatek dochodowy: 50 %
* <sub>Opłata PayPal: 5–10 % w zależności od kraju/kwoty</sub>

A zatem to, co pozostało dla dewelopera, to tylko ułamek tego, co płacisz.

Zwróć też uwagę, że większość darmowych aplikacji ostatecznie nie będzie stabilna, podczas gdy FairEmail jest odpowiednio utrzymany i wspierany oraz, że darmowe aplikacje mogą mieć pewien problem, na przykład wysyłanie poufnych informacji do Internetu. There are no privacy violating ads in the app either.

I have been working on FairEmail almost every day for more than two years, so I think the price is more than reasonable. Z tego powodu również nie będzie zniżek.

<br />

<a name="faq20"></a>
**(20) Czy mogę otrzymać zwrot pieniędzy?**

Jeśli zakupiona funkcja pro nie działa zgodnie z zamierzeniem i nie jest to spowodowane problemem w darmowych funkcjach i nie mogę rozwiązać problemu w odpowiednim czasie, możesz otrzymać zwrot pieniędzy. We wszystkich innych przypadkach zwrot nie jest możliwy. W żadnym wypadku nie ma możliwości zwrotu kosztów w przypadku problemów związanych z darmowymi funkcjami, ponieważ nie było za nie opłaty i można było je ocenić bez żadnych ograniczeń. Jako sprzedawca biorę odpowiedzialność za dostarczenie tego, co zostało obiecane i spodziewam się, że weźmiesz odpowiedzialność za zaznajomienie się z tym, co kupujesz.

<a name="faq21"></a>
**(21) Jak włączyć diodę powiadomień?**

Before Android 8 Oreo: there is an advanced option in the notification settings of the app for this.

Android 8 Oreo and later: please see [here](https://developer.android.com/training/notify-user/channels) about how to configure notification channels. You can use the button *Default channel* in the notification settings of the app to directly go to the right Android notification channel settings.

Pamiętaj, że aplikacje nie mogą zmieniać ustawień powiadomień, w tym ustawień diody powiadomień na Androidzie 8 Oreo i nowszych.

Sometimes it is necessary to disable the setting *Show message preview in notifications* or to enable the settings *Show notifications with a preview text only* to workaround bugs in Android. Może to dotyczyć dźwięków powiadomień, jak również wibracji.

Ustawienie koloru światła w wersji niższej niż Android 8 nie jest wspierane a na Androidzie 8 lub nowszym nie jest możliwe.

<br />

<a name="faq22"></a>
**(22) Co oznacza błąd konta/folderu ... ?**

FairEmail nie ukrywa błędów jak to robią podobne aplikacje, więc łatwiej jest zdiagnozować problemy.

FairEmail automatycznie spróbuje, z opóźnieniem, połączyć się ponownie. Opóźnienie to zostanie podwojone po każdej nieudanej próbie, aby zapobiec rozładowaniu akumulatora i trwałego zablokowania. Please see [this FAQ](#user-content-faq123) for more information about this.

Istnieją błędy ogólne i błędy specyficzne dla kont Gmail (zobacz poniżej).

**Błędy ogólne**

<a name="authfailed"></a>
The error *... **Authentication failed** ...* or *... AUTHENTICATE failed ...* oznacza, że nazwa użytkownika lub hasło jest nieprawidłowe. Niektórzy dostawcy oczekują nazwy użytkownika jako *nazwa użytkownika*, inni Twojego pełnego adresu e-mail *username@example.com*. When copying/pasting to enter a username or password, invisible characters might be copied, which could cause this problem as well. Some password managers are known to do this incorrectly too. The username might be case sensitive, so try lowercase characters only. The password is almost always case sensitive. Some providers require using an app password instead of the account password, so please check the documentation of the provider. Sometimes it is necessary to enable external access (IMAP/SMTP) on the website of the provider first. Other possible causes are that the account is blocked or that logging in has been administratively restricted in some way, for example by allowing to login from certain networks / IP addresses only.

W razie potrzeby możesz zaktualizować hasło w ustawieniach konta: menu nawigacji (po lewej stronie), dotknij *Ustawienia*, dotknij *Ręczna konfiguracja*, dotknij *Konta* i dotknij konto. Changing the account password will in most cases automatically change the password of related identities too. If the account was authorized with OAuth via the quick setup wizard instead of with a password, you can run the quick setup wizard again and tick *Authorize existing account again* to authenticate the account again. Note that this requires a recent version of the app.

Błąd *... Too many bad auth attempts ...* prawdopodobnie oznacza, że używasz hasła do konta Yahoo zamiast hasła aplikacji. Please see [this FAQ](#user-content-faq88) about how to set up a Yahoo account.

Komunikat *... +OK ...* prawdopodobnie oznacza, że port POP3 (zazwyczaj numer portu 995) jest używany dla konta IMAP (zazwyczaj numer portu 993).

Błędy *... invalid greeting ...*, *... requires valid address ...* i *... Parameter to HELO does not conform to RFC syntax ...* można prawdopodobnie rozwiązać, zmieniając zaawansowane ustawienie tożsamości * Użyj lokalnego adresu IP zamiast nazwy hosta *.

Błąd *... Couldn't connect to host ...* means that there was no response from the email server within a reasonable time (20 seconds by default). Mostly this indicates internet connectivity issues, possibly caused by a VPN or by a firewall app. You can try to increase the connection timeout in the connection settings of FairEmail, for when the email server is really slow.

Błąd *... Connection refused ...* means that the email server or something between the email server and the app, like a firewall, actively refused the connection.

Błąd *... Network unreachable ...* means that the email server was not reachable via the current internet connection, for example because internet traffic is restricted to local traffic only.

Błąd *... Host is unresolved ...*, *... Unable to resolve host ...* or *... No address associated with hostname ...* means that the address of the email server could not be resolved into an IP address. This might be caused by a VPN, ad blocking or an unreachable or not properly working (local) [DNS](https://en.wikipedia.org/wiki/Domain_Name_System) server.

Błąd *... Software caused connection abort ...* oznacza, że ​​serwer e-mail lub coś między FairEmail a serwerem e-mail aktywnie zakończyło istniejące połączenie. Może się to zdarzyć na przykład w przypadku nagłej utraty łączności. Typowym przykładem jest włączenie trybu samolotowego.

Błędy *... BYE Logging out ...*, *... Connection reset ...* mean that the email server or something between the email server and the app, for example a router or a firewall (app), actively terminated an existing connection.

Błąd *... Connection closed by peer ...* może być spowodowany przez nieaktualizowany serwer Exchange, więcej informacji można znaleźć [ tutaj ](https://blogs.technet.microsoft.com/pki/2010/09/30/sha2-and-windows/).

Błędy *... Read error ...*, *... Write error ...*, *... Read timed out ...*, *... Broken pipe ...* mean that the email server is not responding anymore or that the internet connection is bad.

<a name="connectiondropped"></a>
Błąd *... Connection dropped by server? ...* means that the email server unexpectedly terminated the connection. This sometimes happen when there were too many connections in a too short time or when a wrong password was used for too many times. In this case, please make sure your password is correct and disable receiving in the receive settings for about 30 minutes and try again. If needed, see [this FAQ](#user-content-faq23) about how you can reduce the number of connections.

Błąd *... Unexpected end of zlib input stream ...*oznacza, że ​​nie wszystkie dane zostały odebrane, prawdopodobnie z powodu złego lub przerwanego połączenia.

Błąd *... connection failure ...* może wskazywać [Zbyt wiele jednoczesnych połączeń](#user-content-faq23).

Ostrzeżenie *... Unsupported encoding...* oznacza, że zestaw znaków wiadomości jest nieznany lub nie jest obsługiwany. FairEmail założy ISO-8859-1 (Latin1), co w większości przypadków spowoduje poprawne wyświetlenie wiadomości.

Błąd *... Login Rate Limit Hit ...* means that there were too many login attempts with an incorrect password. Please double check your password or authenticate the account again with the quick setup wizard (OAuth only).

Błąd *... NO mailbox selected READ-ONLY ...* indicates [this Zimbra problem](https://sebastian.marsching.com/wiki/Network/Zimbra#Mailbox_Selected_READ-ONLY_Error_in_Thunderbird).

Zobacz [tutaj](#user-content-faq4) dla błędów *... Untrusted ... not in certificate ...*, *... Invalid security certificate (Can't verify identity of server) ...* lub *... Trust anchor for certification path not found ...*

Proszę [zobacz tutaj](#user-content-faq127) dla błędu *... Syntactically invalid HELO argument(s) ...*.

Zobacz [tutaj](#user-content-faq41) dla błędu *... Handshake failed ...*.

Zobacz [tutaj](https://linux.die.net/man/3/connect), aby dowiedzieć się co znaczą kody błędów takie jak EHOSTUNREACH i ETIMEDOUT.

Możliwe przyczyny to:

* Zapora lub router blokuje połączenia z serwerem
* Nazwa hosta lub numer portu jest nieprawidłowy
* There are problems with the internet connection
* There are problems with resolving domain names (Yandex: try to disable private DNS in the Android settings)
* The email server is refusing to accept (external) connections
* Serwer e-mail odmawia przyjęcia wiadomości, na przykład dlatego, że jest zbyt duża lub zawiera niedopuszczalne linki
* Istnieje zbyt wiele połączeń z serwerem, zobacz również następne pytanie

Wiele publicznych sieci Wi-Fi blokuje wychodzące wiadomości e-mail, aby zapobiec rozsyłaniu spamu. Czasem możesz obejść to używając innego portu SMTP. Zobacz dokumentację dostawcy dotyczącą możliwych do użycia numerów portów.

Jeśli używasz [VPN](https://en.wikipedia.org/wiki/Virtual_private_network), dostawca usługi VPN może zablokować połączenie, ponieważ zbyt agresywnie próbuje zapobiec wysyłaniu spamu. Zauważ, że [Google Fi](https://fi.google.com/) również używa VPN.

**Send errors**

SMTP servers can reject messages for [a variety of reasons](https://en.wikipedia.org/wiki/List_of_SMTP_server_return_codes). Too large messages and triggering the spam filter of an email server are the most common reasons.

* The attachment size limit for Gmail [is 25 MB](https://support.google.com/mail/answer/6584)
* The attachment size limit for Outlook and Office 365 [is 20 MB](https://support.microsoft.com/en-us/help/2813269/attachment-size-exceeds-the-allowable-limit-error-when-you-add-a-large)
* The attachment size limit for Yahoo [is 25 MB](https://help.yahoo.com/kb/SLN5673.html)
* *554 5.7.1 Service unavailable; Client host xxx.xxx.xxx.xxx blocked*, please [see here](https://docs.gandi.net/en/gandimail/faq/error_types/554_5_7_1_service_unavailable.html)
* *501 Syntax error - line too long* jest często spowodowany użyciem długiego nagłówka Autocrypt
* *503 5.5.0 Recipient already specified* przeważnie oznacza, że adres jest używany zarówno jako adres DO, jak i DW
* *554 5.7.1 ... not permitted to relay* means that the email server does not recognize the username/email address. Please double check the host name and username/email address in the identity settings.
* *550 Spam message rejected because IP is listed by ...* means that the email server rejected to send a message from the current (public) network address because it was misused to send spam by (hopefully) somebody else before. Please try to enable flight mode for 10 minutes to acquire a new network address.
* *550 We're sorry, but we can't send your email. Either the subject matter, a link, or an attachment potentially contains spam, or phishing or malware.* means that the email provider considers an outgoing message as harmful.
* *571 5.7.1 Message contains spam or virus or sender is blocked ...* means that the email server considered an outgoing message as spam. This probably means that the spam filters of the email server are too strict. You'll need to contact the email provider for support on this.
* *451 4.7.0 Temporary server error. Please try again later. PRX4 ...*: please [see here](https://www.limilabs.com/blog/office365-temporary-server-error-please-try-again-later-prx4) or [see here](https://judeperera.wordpress.com/2019/10/11/fixing-451-4-7-0-temporary-server-error-please-try-again-later-prx4/).
* *571 5.7.1 Relay access denied*: please double check the username and email address in the advanced identity settings (via the manual setup).

If you want to use the Gmail SMTP server to workaround a too strict outgoing spam filter or to improve delivery of messages:

* Verify your email address [here](https://mail.google.com/mail/u/0/#settings/accounts) (you'll need to use a desktop browser for this)
* Zmień ustawienia tożsamości w ten sposób (Ustawienia, dotknij Ręczna konfiguracja, dotknij Tożsamości, dotknij tożsamość):

&emsp;&emsp;Username: *your Gmail address*<br /> &emsp;&emsp;Password: *[an app password](#user-content-faq6)*<br /> &emsp;&emsp;Host: *smtp.gmail.com*<br /> &emsp;&emsp;Port: *465*<br /> &emsp;&emsp;Encryption: *SSL/TLS*<br /> &emsp;&emsp;Reply to address: *your email address* (advanced identity settings)<br />

<br />

**Błędy Gmail**

Autoryzacja konfiguracji kont Gmail za pomocą szybkiego kreatora wymaga okresowego odświeżania za pomocą [ Menedżera kont Androida ](https://developer.android.com/reference/android/accounts/AccountManager). Wymaga to uprawnień do kontaktu/konta oraz połączenia z Internetem.

In case of errors it is possible to authorize/restore a Gmail account again via the Gmail quick setup wizard.

Błąd *... Uwierzytelnianie nie powiodło się ... Konto nie znalezione ...* oznacza, że poprzednio autoryzowane konto Gmail zostało usunięte z urządzenia.

Błędy *... Uwierzytelnianie nie powiodło się ... No token ...* means that the Android account manager failed to refresh the authorization of a Gmail account.

Błąd *... Authentication failed ... network error ...* means that the Android account manager was not able to refresh the authorization of a Gmail account due to problems with the internet connection

Błąd *... Uwierzytelnianie nie powiodło się ... Invalid credentials ...* could be caused by changing the account password or by having revoked the required account/contacts permissions. In case the account password was changed, you'll need to authenticate the Google account in the Android account settings again. In case the permissions were revoked, you can start the Gmail quick setup wizard to grant the required permissions again (you don't need to setup the account again).

Błąd *... ServiceDisabled ...* might be caused by enrolling in the [Advanced Protection Program](https://landing.google.com/advancedprotection/): "*To read your email, you can (must) use Gmail - You won’t be able to use your Google Account with some (all) apps & services that require access to sensitive data like your emails*", see [here](https://support.google.com/accounts/answer/7519408?hl=en&ref_topic=9264881).

The error *... 334 ... OAUTH2 asked for more ...* probably means that the account needs to be authorized again, which you can do with the quick setup wizard in the settings.

W razie wątpliwości możesz poprosić o [wsparcie](#user-content-support).

<br />

<a name="faq23"></a>
**(23) Dlaczego otrzymuję ostrzeżenie ... ?**

*Główne*

Alerty to wiadomości ostrzegawcze wysyłane przez serwery e-mail.

*Zbyt wiele jednoczesnych połączeń* lub *Przekroczono maksymalną liczbę połączeń*

Ten alert zostanie wysłany, gdy będzie zbyt wiele połączeń do folderów dla tego samego konta e-mail jednocześnie.

Możliwe przyczyny to:

* Istnieje wiele klientów poczty e-mail podłączonych do tego samego konta
* Ten sam klient poczty e-mail jest wielokrotnie podłączony do tego samego konta
* Poprzednie połączenia zostały nagle zakończone, na przykład przez utratę łączności z Internetem

Najpierw spróbuj poczekać jakiś czas, aby sprawdzić, czy problem sam się rozwiązał, w przeciwnym razie:

* albo przełącz się na okresowe sprawdzanie wiadomości w ustawieniach odbioru, co spowoduje otwarcie folderów pojedynczo
* lub ustaw niektóre foldery do odpytywania zamiast synchronizacji (długie naciśnięcie folderu na liście folderów, edycja właściwości)

An easy way to configure periodically checking for messages for all folders except the inbox is to use *Apply to all ...* in the three-dots menu of the folder list and to tick the bottom two advanced checkboxes.

Maksymalna liczba jednoczesnych połączeń folderów dla Gmail to 15, więc możesz synchronizować maksymalnie 15 folderów jednocześnie na *wszystkich* Twoich urządzeniach w tym samym czasie. Z tego powodu foldery Gmail * użytkownika * są domyślnie ustawione na odpytywanie zamiast ciągłej synchronizacji. Gdy jest to konieczne lub pożądane, możesz to zmienić przez długie naciśnięcie folderu na liście folderów i wybranie *Edytuj właściwości*. Szczegóły znajdziesz [tutaj](https://support.google.com/mail/answer/7126229).

Podczas korzystania z serwera Dovecot możesz zmienić ustawienie [ mail_max_userip_connections ](https://doc.dovecot.org/settings/dovecot_core_settings/#mail-max-userip-connections).

Note that it will take the email server a while to discover broken connections, for example due to going out of range of a network, which means that effectively only half of the folder connections are available. For Gmail this would be just 7 connections.

<br />

<a name="faq24"></a>
**(24) Czym są wiadomości przeglądane na serwerze?**

Przeglądanie wiadomości na serwerze będzie pobierać wiadomości z serwera pocztowego w czasie rzeczywistym po dotarciu do końca listy zsynchronizowanych wiadomości, nawet gdy folder jest ustawiony na brak synchronizacji. Możesz wyłączyć tę funkcję w zaawansowanych ustawieniach konta.

<br />

<a name="faq25"></a>
**(25) Dlaczego nie mogę wybrać/otworzyć/zapisać obrazu, załącznika lub pliku?**

Gdy element menu służący do wyboru/otwierania/zapisywania pliku jest wyłączony (przyciemniony) lub pojawia się komunikat * Struktura dostępu do pamięci niedostępna *, [ struktura dostępu do pamięci ](https://developer.android.com/guide/topics/providers/document-provider), standardowy komponent Androida, prawdopodobnie nie istnieje. Może to być spowodowane tym, że Twój niestandardowy (custom) ROM nie zawiera go lub został usunięty (dezaktywowany).

FairEmail nie wymaga uprawnień do pamięci, więc ten komponent jest wymagany do wybierania plików i folderów. Żadna aplikacja, z wyjątkiem być może menedżerów plików dla systemu Android 4.4 KitKat lub nowszych, nie powinna prosić o pozwolenie na przechowywanie, ponieważ pozwoliłoby to na dostęp do * wszystkich plików *.

Dostęp do pamięci zapewnia pakiet * com.android.documentsui *, który jest widoczny jako aplikacja * Pliki * w niektórych wersjach Androida (godne uwagi OxygenOS).

Możesz włączyć strukturę dostępu do pamięci (ponownie) za pomocą tego polecenia adb:

```
pm install -k --user 0 com.android.documentsui
```

Alternatywnie, możesz ponownie włączyć aplikację *Pliki* za pomocą ustawień aplikacji Android.

<br />

<a name="faq26"></a>
**(26) Czy mogę pomóc w tłumaczeniu FairEmail na mój język?**

Tak, możesz przetłumaczyć teksty FairEmail na własny język [na Crowdin](https://crowdin.com/project/open-source-email). Rejestracja jest darmowa.

If you would like your name or alias to be included in the list of contributors in *About* the app, please [contact me](https://contact.faircode.eu/?product=fairemailsupport).

<br />

<a name="faq27"></a>
**(27) W jaki sposób mogę odróżnić osadzone i zewnętrzne obrazy?**

Obraz zewnętrzny:

![Obraz zewnętrzny](https://github.com/M66B/FairEmail/blob/master/images/baseline_image_black_48dp.png)

Obraz wbudowany:

![Wbudowany obraz](https://github.com/M66B/FairEmail/blob/master/images/baseline_photo_library_black_48dp.png)

Obraz uszkodzony:

![Uszkodzony obraz](https://github.com/M66B/FairEmail/blob/master/images/baseline_broken_image_black_48dp.png)

Pamiętaj, że pobieranie obrazów zewnętrznych ze zdalnego serwera może służyć do rejestrowania, że ​​zobaczyłeś wiadomość, czego prawdopodobnie nie chcesz, jeśli wiadomość jest spamem lub jest złośliwa.

<br />

<a name="faq28"></a>
**(28) Jak mogę zarządzać powiadomieniami na pasku statusu?**

In the notification settings you'll find a button *Manage notifications* to directly navigate to the Android notifications settings for FairEmail.

W Android 8 Oreo i nowszych możesz zarządzać właściwościami poszczególnych kanałów powiadomień, na przykład aby ustawić określony dźwięk powiadomienia lub wyświetlić powiadomienia na ekranie blokady.

FairEmail ma następujące kanały powiadomień:

* Usługa: używana do powiadamiania o usłudze synchronizacji, zobacz również [to FAQ](#user-content-faq2)
* Wyślij: używane do powiadamiania o usłudze wysyłania
* Powiadomienia: używane do powiadomień o nowych wiadomościach
* Ostrzeżenia: używane do powiadomień ostrzegawczych
* Błędy: używane do powiadomień o błędach

Zobacz [tutaj](https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels) szczegóły dotyczące kanałów powiadomień. W skrócie: dotknij nazwy kanału powiadomień, aby uzyskać dostęp do ustawień kanału.

Na Androidzie przed Androidem 8 Oreo możesz ustawić dźwięk powiadomień w ustawieniach.

Zobacz [to FAQ](#user-content-faq21) jeśli twoje urządzenie ma diodę powiadomień.

<br />

<a name="faq29"></a>
**(29) Jak mogę otrzymywać powiadomienia o nowych wiadomościach dla innych folderów?**

Po prostu naciśnij folder, wybierz *Edytuj właściwości*, i włącz *Pokaż we wspólnej skrzynce* lub *Powiadom o nowych wiadomościach* (dostępne tylko na Androidzie 7 Nougat i później) i dotknij *Zapisz*.

<br />

<a name="faq30"></a>
**(30) Jak mogę użyć dostarczonych szybkich ustawień?**

Dostępne są szybkie ustawienia (pola ustawień) dla:

* globalnie włącz/wyłącz synchronizację
* pokaż liczbę nowych wiadomości i oznacz je jako widoczne (nie przeczytane)

Szybkie ustawienia wymagają Android 7.0 Nougat lub nowszego. Użycie skrótów ustawień jest wyjaśnione [tutaj](https://support.google.com/android/answer/9083864).

<br />

<a name="faq31"></a>
**(31) W jaki sposób mogę korzystać z dostarczonych skrótów?**

There are shortcuts available to compose a new message to a favorite contact.

Skróty wymagają Androida 7.1 Nougat lub nowszego. Użycie skrótów jest wyjaśnione [tutaj](https://support.google.com/android/answer/2781850).

It is also possible to create shortcuts to folders by long pressing a folder in the folder list of an account and selecting *Add shortcut*.

<br />

<a name="faq32"></a>
**(32) Jak mogę sprawdzić, czy czytanie wiadomości e-mail jest naprawdę bezpieczne?**

Możesz w tym celu użyć [Email Privacy Tester](https://www.emailprivacytester.com/).

<br />

<a name="faq33"></a>
**(33) Dlaczego edytowane adresy nadawcy nie działają?**

Większość dostawców akceptuje zweryfikowane adresy tylko podczas wysyłania wiadomości, aby zapobiec spamowi.

Na przykład Google modyfikuje takie nagłówki wiadomości dla * niezweryfikowanych * adresów:

```
From: Somebody <somebody@example.org>
X-Google-Original-From: Somebody <somebody+extra@example.org>
```

Oznacza to, że edytowany adres nadawcy został automatycznie zastąpiony zweryfikowanym adresem przed wysłaniem wiadomości.

Pamiętaj, że jest to niezależne od odbierania wiadomości.

<br />

<a name="faq34"></a>
**(34) W jaki sposób dobierane są tożsamości?**

Tożsamości są zgodne z oczekiwaniami, dopasowane do konta. W przypadku wiadomości przychodzących * Do *, * DW *, * UDW *, * z * i * (X-)delivered/envelope/original-to * zostaną sprawdzone (w tej kolejności) a dla wiadomości wychodzących (wersje robocze, skrzynka nadawcza i wysłane) sprawdzane będą tylko adresy * z *. Equal addresses have precedence over partially matching addresses, except for *delivered-to* addresses.

The matched address will be shown as *via* in the addresses section of received messages (between the message header and message text).

Pamiętaj, że tożsamości muszą być włączone, aby można je było dopasować i że tożsamość innych kont nie będzie brana pod uwagę.

Dopasowanie zostanie wykonane tylko raz po otrzymaniu wiadomości, więc zmiana konfiguracji nie zmieni istniejących wiadomości. Możesz wyczyścić wiadomości lokalne przez długie naciśnięcie folderu na liście folderów i zsynchronizować wiadomości ponownie.

It is possible to configure a [regex](https://en.wikipedia.org/wiki/Regular_expression) in the identity settings to match **the username** of an email address (the part before the @ sign).

Zauważ, że nazwa domeny (części po znaku @) zawsze musi odpowiadać nazwie domeny tożsamości.

If you like to match a catch-all email address, this regex is mostly okay:

```
.*
```

Jeśli chcesz dopasować adresy e-mail specjalnego przeznaczenia abc@example.com i xyx@example.com i chcesz mieć zapasowy adres e-mail main@example.com, możesz zrobić coś takiego:

* Tożsamość: abc@example.com; regex: **(?i)abc**
* Tożsamość: xyz@example.com; regex: **(?i)xyz**
* Tożsamość: main@example.com; regex: **^(?i)((?!abc|xyz).)\*$**

Dopasowane tożsamości mogą być użyte do pokolorowania wiadomości kodowych. The identity color takes precedence over the folder and account color. Ustawienie kolorów tożsamości jest funkcją pro.

<br />

<a name="faq35"></a>
**(35) Dlaczego powinienem być ostrożny z przeglądaniem obrazów, załączników, oryginalnej wiadomości i otwieraniem linków?**

Wyświetlanie zdalnie przechowywanych obrazów (zobacz również [ten FAQ](#user-content-faq27)) może nie tylko poinformować nadawcę, że widziałeś wiadomość, ale spowodować, że wycieknie również twój adres IP. Zobacz również to pytanie: [Dlaczego link e-mail jest bardziej niebezpieczny niż link wyszukiwarki?](https://security.stackexchange.com/questions/241139/why-emails-link-is-more-dangerous-than-web-searchs-link).

Otwieranie załączników lub przeglądanie oryginalnej wiadomości może ładować zdalną treść i wykonywać skrypty, co może nie tylko spowodować wyciek poufnych informacji, ale może również stanowić zagrożenie bezpieczeństwa.

Pamiętaj, że Twoje kontakty mogą nieświadomie wysyłać złośliwe wiadomości, jeśli zostaną zainfekowane złośliwym oprogramowaniem.

FairEmail ponownie formatuje wiadomości, powodując, że wyglądają one inaczej niż oryginalne, ale także odkrywa linki służące do phishingu.

Zauważ, że formatowane wiadomości są często bardziej czytelne niż oryginalne wiadomości, ponieważ marginesy są usuwane, a kolory czcionek i rozmiary są znormalizowane.

Aplikacja Gmail domyślnie wyświetla obrazy pobierając obrazy przez serwer proxy Google. Ponieważ obrazy są pobierane z serwera źródłowego [w czasie rzeczywistym](https://blog.filippo.io/how-the-new-gmail-image-proxy-works-and-what-this-means-for-you/), jest to jeszcze mniej bezpieczne, ponieważ Google jest również zaangażowane bez zapewnienia wielu korzyści.

Domyślnie możesz wyświetlać obrazy i oryginalne wiadomości dla zaufanych nadawców indywidualnie, zaznaczając *Nie pytaj ponownie o ...*.

Jeśli chcesz zresetować domyślne aplikacje *Otwórz z*, proszę [zobacz tutaj](https://support.google.com/pixelphone/answer/6271667).

<br />

<a name="faq36"></a>
**(36) W jaki sposób zaszyfrowane są pliki ustawień?**

Krótka wersja: AES 256 bitów

Długa wersja:

* Klucz 256 bitów jest generowany z *PBKDF2WithHmacSHA1* przy użyciu 128 bitowej bezpiecznej losowej soli i 65536 iteracji
* Szyfr to *AES/CBC/PKCS5Padding*

<br />

<a name="faq37"></a>
**(37) Jak przechowywane są hasła?**

Wszystkie obsługiwane wersje Androida [szyfrują wszystkie dane użytkownika](https://source.android.com/security/encryption), więc wszystkie dane, w tym nazwy użytkowników, hasła, wiadomości itp., są przechowywane szyfrowane.

If the device is secured with a PIN, pattern or password, you can make the account and identity passwords visible. If this is a problem because you are sharing the device with other people, consider to use [user profiles](https://www.howtogeek.com/333484/how-to-set-up-multiple-user-profiles-on-android/).

<br />

<a name="faq39"></a>
**(39) Jak mogę zmniejszyć zużycie baterii przez FairEmail?**

Ostatnie wersje Androida domyślnie raportują *użycie aplikacji* jako procent na ekranie ustawień baterii Androida. ** Myląco * użycie aplikacji * nie jest takie samo jak * zużycie baterii * i nie jest nawet bezpośrednio związane ze zużyciem baterii! ** Użycie aplikacji (podczas używania) będzie bardzo wysokie, ponieważ FairEmail korzysta z usługi pierwszego planu, która jest uważana przez Androida za ciągłe korzystanie z aplikacji. Nie oznacza to jednak, że FairEmail stale zużywa energię baterii. Rzeczywiste zużycie baterii można zobaczyć przechodząc do tego ekranu:

*Ustawienia Androida*, *Bateria*, menu trzech kropek *Użycie baterii*, menu trzech kropek *Pokaż pełne użycie urządzenia*

Z reguły zużycie baterii powinno być niższe lub nie powinno być znacznie wyższe niż *Tryb czuwania w sieci komórkowej*. Jeśli tak nie jest, włącz *Optymalizuj automatycznie* w ustawieniach odbierania. Jeśli to nie pomoże, proszę [poprosić o wsparcie](https://contact.faircode.eu/?product=fairemailsupport).

Jest nieuniknione, że synchronizacja wiadomości zużyje energię baterii, ponieważ wymaga dostępu do sieci i dostępu do bazy danych wiadomości.

Jeśli porównujesz zużycie baterii FairEmail z innym klientem e-mail, upewnij się, że inny klient poczty e-mail jest podobnie skonfigurowany. Na przykład porównywanie zawsze synchronizowanych (wiadomości push) i (rzadkie) okresowe sprawdzanie nowych wiadomości nie jest uczciwym porównaniem.

Ponowne połączenie z serwerem e-mail wykorzysta dodatkową moc baterii, więc niestabilne połączenie internetowe spowoduje dodatkowe zużycie baterii. Also, some email servers prematurely terminate idle connections, while [the standard](https://tools.ietf.org/html/rfc2177) says that an idle connection should be kept open for 29 minutes. In these cases you might want to synchronize periodically, for example each hour, instead of continuously. Note that polling frequently (more than every 30-60 minutes) will likely use more battery power than synchronizing always because connecting to the server and comparing the local and remote messages are expensive operations.

[Na niektórych urządzeniach](https://dontkillmyapp.com/) konieczne jest wyłączenie </em> optymalizacji baterii (etap 3) aby połączenia z serwerami e-mail były otwarte. In fact, leaving battery optimizations enabled can result in extra battery usage for all devices, even though this sounds contradictory!

Większość zużycia baterii, bez uwzględniania oglądania wiadomości, wynika z synchronizacji (odbieranie i wysyłanie) wiadomości. Aby zmniejszyć zużycie baterii, ustaw liczbę dni synchronizacji wiadomości na niższą wartość, szczególnie jeśli w folderze jest wiele niedawnych wiadomości. Przytrzymaj nazwę folderu na liście folderów i wybierz *Edytuj właściwości*, aby uzyskać dostęp do tego ustawienia.

Jeśli masz przynajmniej raz dziennie połączenie z Internetem, wystarczy zsynchronizować wiadomości tylko na jeden dzień.

Pamiętaj, że możesz ustawić liczbę dni na * przechowywanie * wiadomości na wyższą liczbę niż na * synchronizowanie * wiadomości. You could for example initially synchronize messages for a large number of days and after this has been completed reduce the number of days to synchronize messages, but leave the number of days to keep messages. After decreasing the number of days to keep messages, you might want to run the cleanup in the miscellaneous settings to remove old files.

W ustawieniach odbierania możesz włączyć zawsze synchronizowanie wiadomości oznaczonych gwiazdką, co pozwoli ci zachować starsze wiadomości podczas synchronizacji wiadomości przez ograniczoną liczbę dni.

Wyłączenie opcji folderu *Automatycznie pobieraj treści wiadomości i załączniki* spowoduje zmniejszenie ruchu sieciowego, a tym samym zmniejszenie zużycia baterii. Możesz wyłączyć tę opcję, na przykład dla folderu Wysłane i Archiwum.

Synchronizacja wiadomości w nocy jest w większości nieprzydatna, więc możesz zaoszczędzić na zużyciu baterii, nie synchronizując w nocy. W ustawieniach możesz wybrać harmonogram synchronizacji wiadomości (jest to funkcja pro).

FairEmail domyślnie synchronizuje listę folderów przy każdym połączeniu. Ponieważ foldery zwykle nie są tworzone, zmieniane i usuwane bardzo często, możesz zaoszczędzić trochę danych i baterii wyłączając to w ustawieniach odbioru.

FairEmail będzie domyślnie sprawdzać, czy stare wiadomości zostały usunięte z serwera przy każdym połączeniu. Jeśli nie masz nic przeciwko temu, że stare wiadomości, które zostały usunięte z serwera są nadal widoczne w FairEmail, możesz zaoszczędzić trochę danych i baterii wyłączając to w ustawieniach odbioru.

Niektórzy dostawcy nie stosują się do standardu IMAP i nie utrzymują wystarczająco długo połączenia, zmuszając FairEmail do częstego ponawiania połączenia, powodując dodatkowe zużycie baterii. Możesz przejrzeć * Log * poprzez główne menu nawigacyjne, aby sprawdzić, czy często dochodzi do ponownych połączeń (połączenie zamknięte / zerowane, błąd odczytu / zapisu / przekroczenie limitu czasu itp.). Można to obejść, obniżając interwał utrzymywania aktywności w zaawansowanych ustawieniach konta do na przykład 9 lub 15 minut. Pamiętaj, że optymalizacje baterii muszą zostać wyłączone w kroku konfiguracji 3, aby niezawodnie utrzymywać połączenia aktywne.

Niektórzy dostawcy wysyłają co dwie minuty coś takiego: '*Still herej*' skutkującego ruchem sieciowym i wybudzającym się urządzeniem i powodującym niepotrzebne dodatkowe zużycie baterii. Możesz sprawdzić * Log * w głównym menu nawigacyjnym, aby sprawdzić, czy twój dostawca to robi. Jeśli Twój dostawca używa [ Dovecot ](https://www.dovecot.org/) jako serwera IMAP, możesz poprosić swojego dostawcę o zmianę ustawienia [ imap_idle_notify_interval ](https://wiki.dovecot.org/Timeouts) na wyższą albo najlepiej aby to wyłączył. Jeśli Twój dostawca nie jest w stanie lub nie chce tego zmienić/wyłączyć, powinieneś rozważyć przełączanie się na synchronizację okresową zamiast ciągłej. Możesz to zmienić w ustawieniach odbierania.

Jeśli otrzymałeś wiadomość *Ten dostawca nie obsługuje wiadomości push* podczas konfigurowania konta, rozważ przejście na nowoczesnego dostawcę obsługującego wiadomości push (IMAP IDLE) w celu zmniejszenia zużycia baterii.

Jeśli twoje urządzenie ma ekran [AMOLED](https://en.wikipedia.org/wiki/AMOLED), możesz zaoszczędzić baterię podczas oglądania wiadomości przełączając motyw na czarny.

If auto optimize in the receive settings is enabled, an account will automatically be switched to periodically checking for new messages when the email server:

* Wysyła '*Still here*' w przeciągu 3 minut
* Serwer e-mail nie wspiera wiadomości push
* Odstęp między keep-alive jest krótszy niż 12 minut

In addition, the trash and spam folders will be automatically set to checking for new messages after three successive [too many simultaneous connections](#user-content-faq23) errors.

<br />

<a name="faq40"></a>
**(40) How can I reduce the data usage of FairEmail?**

You can reduce the data usage basically in the same way as reducing battery usage, see the previous question for suggestions.

It is inevitable that data will be used to synchronize messages.

If the connection to the email server is lost, FairEmail will always synchronize the messages again to make sure no messages were missed. If the connection is unstable, this can result in extra data usage. In this case, it is a good idea to decrease the number of days to synchronize messages to a minimum (see the previous question) or to switch to periodically synchronizing of messages (receive settings).

To reduce data usage, you could change these advanced receive settings:

* Check if old messages were removed from the server: disable
* Synchronize (shared) folder list: disable

Domyślnie FairEmail nie pobiera tekstów wiadomości i załączników większych niż 256 kB w przypadku połączenia internetowego taryfowego (sieć komórkowa lub płatna Wi-Fi). Możesz to zmienić w ustawieniach połączenia.

<br />

<a name="faq41"></a>
**(41) Jak mogę naprawić błąd 'Handshake failed' ?**

Istnieje kilka możliwych przyczyn, więc proszę przeczytać do końca tę odpowiedź.

Błąd '*Handshake failed ... WRONG_VERSION_NUMBER ... * ”może oznaczać, że próbujesz połączyć się z serwerem IMAP lub SMTP bez szyfrowanego połączenia, zwykle przy użyciu portu 143 (IMAP) i portu 25 (SMTP) lub, że używany jest zły protokół (SSL/TLS lub STARTTLS).

Większość dostawców zapewnia zaszyfrowane połączenia za pomocą różnych portów, zazwyczaj portu 993 (IMAP) i portu 465/587 (SMTP).

Jeśli Twój dostawca nie obsługuje zaszyfrowanych połączeń, powinieneś to zgłosić, aby to umożliwił. Jeśli to nie jest możliwe, możesz włączyć *Zezwalaj na niezabezpieczone połączenia* zarówno w ustawieniach zaawansowanych oraz w ustawieniach konta/tożsamości.

Zobacz również [ten FAQ](#user-content-faq4).

Błąd '*Handshake failed ... SSLV3_ALERT_ILLEGAL_PARAMETER ... /0>' jest spowodowany błędem w implementacji protokołu SSL lub zbyt krótkim kluczem DH na serwerze e-mail i niestety nie może być naprawiony przez FairEmail.</p>

Błąd '*Handshake failed ... HANDSHAKE_FAILURE_ON_CLIENT_HELLO ...*' może być spowodowany przez dostawcę nadal używającego RC4, , który nie jest już obsługiwany od [Android 7](https://developer.android.com/about/versions/nougat/android-7.0-changes.html#tls-ssl).

Błąd '*Handshake failed ... UNSUPPORTED_PROTOCOL or TLSV1_ALERT_PROTOCOL_VERSION ...*' might be caused by enabling hardening connections in the connection settings or by Android not supporting older protocols anymore, like SSLv3.

Android 8 Oreo i nowsze już [nie wspierają](https://developer.android.com/about/versions/oreo/android-8.0-changes#security-all) SSLv3. Nie ma sposobu na obejście tego przy braku wsparcia RC4 i SSLv3, ponieważ zostało to całkowicie usunięte z Androida (to daje do myślenia).

Możesz użyć [tej strony](https://ssl-tools.net/mailservers) lub [tej strony](https://www.immuniweb.com/ssl/) aby sprawdzić czy nie występują problemy SSL/TLS serwerów e-mail.

<br />

<a name="faq42"></a>
**(42) Czy możesz dodać nowego dostawcę do listy dostawców?**

Jeśli z usługodawcy korzysta więcej niż kilka osób, tak, z przyjemnością.

Potrzebne są następujące informacje:

```
<provider
    name="Gmail"
    link="https://support.google.com/mail/answer/7126229" // link to the instructions of the provider
    type="com.google"> // to nie jest potrzebne
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

EFR [writes](https://www.eff.org/nl/deeplinks/2018/06/announcing-starttls-everywhere-securing-hop-hop-email-delivery): "*Dodatkowo, nawet jeśli skonfigurujesz STARTTLS idealnie i używasz ważnego certyfikatu, nadal nie ma gwarancji, że Twoja komunikacja zostanie zaszyfrowana.*"

Zatem czyste połączenia SSL są bezpieczniejsze niż korzystanie z [STARTTLS](https://en.wikipedia.org/wiki/Opportunistic_TLS) i dlatego są preferowane.

Upewnij się, że odbieranie i wysyłanie wiadomości działa poprawnie przed skontaktowaniem się ze mną, aby dodać dostawcę.

Zobacz poniżej jak się ze mną skontaktować.

<br />

<a name="faq43"></a>
**(43) Czy możesz pokazać oryginał... ?**

Pokaż oryginał, pokazuje oryginalną wiadomość, jak wysłał ją nadawca, w tym oryginalne czcionki, kolory, marginesy itp. FairEmail w żaden sposób nie zmieni tego, z wyjątkiem żądania [ TEXT_AUTOSIZING ](https://developer.android.com/reference/android/webkit/WebSettings.LayoutAlgorithm), który * spróbuje * sprawić, że mały tekst będzie bardziej czytelny.

<br />

<a name="faq44"></a>
**~~(44) Czy możesz pokazać zdjęcia kontaktów / identyfikatory w folderze wysłane?~~**

~~Zdjęcia kontaktu i ikony są zawsze wyświetlane dla nadawcy, ponieważ jest to niezbędne dla wątków konwersacji.~~ ~~Pobieranie zdjęć kontaktowych zarówno dla nadawcy, jak i odbiorcy, nie jest tak naprawdę opcją, ponieważ uzyskanie zdjęcia kontaktu jest kosztowną operacją.~~

<br />

<a name="faq45"></a>
**(45) Jak mogę naprawić 'Ten klucz nie jest dostępny.<0> Aby go użyć, musisz zaimportować go jako swój własny! ?**

Otrzymasz wiadomość *Ten klucz nie jest dostępny. Aby go użyć, musisz zaimportować go jako własny!* podczas próby odszyfrowania wiadomości za pomocą klucza publicznego. Aby to naprawić, musisz zaimportować klucz prywatny.

<br />

<a name="faq46"></a>
**(46) Dlaczego lista wiadomości ciągle się odświeża?**

Jeśli na górze listy wiadomości znajduje się „spinner”, folder jest nadal synchronizowany ze zdalnym serwerem. Możesz zobaczyć postęp synchronizacji na liście folderów. Zobacz legendę o tym, co oznaczają ikony i liczby.

The speed of your device and internet connection and the number of days to synchronize messages determine how long synchronization will take. Note that you shouldn't set the number of days to synchronize messages to more than one day in most cases, see also [this FAQ](#user-content-faq39).

<br />

<a name="faq47"></a>
**(47) Jak mogę rozwiązać błąd 'Brak konta głównego lub folderu szkiców' ?**

Otrzymasz komunikat o błędzie *Brak konta podstawowego lub folderu szkiców * podczas próby utworzenia wiadomości, gdy nie ma ustawionego konta głównego lub gdy nie wybrano folderu szkiców dla konta głównego. Może się to zdarzyć na przykład podczas uruchamiania FairEmail w celu tworzenia wiadomości z innej aplikacji. FairEmail musi wiedzieć, gdzie przechowywać szkic, więc musisz wybrać jedno konto jako konto główne i/lub musisz wybrać folder szkiców dla konta głównego.

Może się to również zdarzyć, gdy próbujesz odpowiedzieć na wiadomość lub przesłać wiadomość z konta bez folderu szkiców, gdy nie ma konta podstawowego lub gdy konto podstawowe nie ma folderu szkiców.

Aby uzyskać więcej informacji, zobacz [ten FAQ](#user-content-faq141).

<br />

<a name="faq48"></a>
**~~(48) Jak mogę rozwiązać błąd 'Brak konta podstawowego lub brak folderu archiwum' ?~~**

~~Otrzymasz komunikat o błędzie *Brak konta podstawowego lub folderu archiwum* podczas wyszukiwania wiadomości z innej aplikacji. FairEmail musi wiedzieć, gdzie szukać, więc musisz wybrać jedno konto, które będzie kontem głównym i/lub musisz wybrać folder archiwum dla konta głównego.~~

<br />

<a name="faq49"></a>
**(49) Jak mogę naprawić błąd 'An outdated app sent a file path instead of a file stream' ?**

Prawdopodobnie wybrałeś lub wysłałeś załącznik lub obrazek z przestarzałego menedżera plików lub nieaktualną aplikacją, która zakłada, że wszystkie aplikacje nadal mają uprawnienia do przechowywania. Ze względów bezpieczeństwa i prywatności, nowoczesne aplikacje takie jak FairEmail nie mają już pełnego dostępu do wszystkich plików. Może to skutkować komunikatem o błędzie *Nieaktualna aplikacja wysłała ścieżkę pliku zamiast strumienia pliku* jeśli nazwa pliku zamiast strumienia pliku jest udostępniana FairEmail, ponieważ FairEmail nie może losowo otwierać plików.

Możesz to naprawić, przechodząc do aktualnego menedżera plików lub aplikacji przeznaczonej dla najnowszych wersji Androida. Alternatywnie, możesz przyznać FairEmail dostęp do odczytu miejsca na urządzeniu w ustawieniach aplikacji Android. Zauważ, że to rozwiązanie już [nie zadziała na Androidzie Q](https://developer.android.com/preview/privacy/scoped-storage).

Zobacz również [pytanie 25](#user-content-faq25) i [co pisze Google na ten temat](https://developer.android.com/training/secure-file-sharing/share-file#RespondToRequest).

<br />

<a name="faq50"></a>
**(50) Czy możesz dodać opcję synchronizacji wszystkich wiadomości?**

You can synchronize more or even all messages by long pressing a folder (inbox) in the folder list of an account (tap on the account name in the navigation menu) and selecting *Synchronize more* in the popup menu.

<br />

<a name="faq51"></a>
**(51) Jak sortowane są foldery?**

Foldery są najpierw sortowane według kolejności kont (domyślnie według nazwy konta) oraz w ramach konta ze specjalnymi folderami systemowymi u góry, a następnie folderami zsynchronizowanymi. W każdej kategorii foldery są sortowane według (wyświetlanej) nazwy. Możesz ustawić wyświetlaną nazwę, naciskając długo folder na liście folderów i wybierając * Edytuj właściwości *.

The navigation (hamburger) menu item *Order folders* in the settings can be used to manually order the folders.

<br />

<a name="faq52"></a>
**(52) Dlaczego ponowne połączenie z kontem zajmuje trochę czasu?**

Nie ma wiarygodnego sposobu, aby dowiedzieć się, czy połączenie z kontem zostało zakończone w sposób wdzięczny czy wymuszony. Próba ponownego połączenia z kontem, gdy połączenie z kontem zostało przymusowo przerwane, może spowodować problemy takie jak [zbyt wiele jednoczesnych połączeń](#user-content-faq23) lub nawet zablokowanie konta. W celu zapobiegania takim problemom, FairEmail czeka 90 sekund przed ponownym połączeniem.

Możesz przytrzymać przycisk *Ustawienia* w menu nawigacji, aby natychmiast się połączyć ponownie.

<br />

<a name="faq53"></a>
**(53) Czy możesz przykleić pasek akcji wiadomości u góry/dołu?**

Pasek akcji wiadomości działa na pojedynczej wiadomości, a dolny pasek akcji działa na wszystkich wiadomościach w konwersacji. Ponieważ w rozmowie często jest więcej niż jedna wiadomość, nie jest to możliwe. Ponadto istnieją pewne działania specyficzne dla wiadomości, takie jak przekazywanie.

Przenoszenie paska akcji wiadomości na dół wiadomości nie jest wizualnie atrakcyjne, ponieważ istnieje już pasek akcji rozmowy u dołu ekranu.

Pamiętaj, że nie ma wielu aplikacji e-mail, które wyświetlają rozmowę jako listę rozwijanych wiadomości. Ma to wiele zalet, ale powoduje również konieczność podjęcia określonych działań.

<br />

<a name="faq54"></a>
**~~ (54) Jak korzystać z prefiksu przestrzeni nazw? ~~**

~~ Prefiks przestrzeni nazw służy do automatycznego usuwania dostawców prefiksów, które czasami dodają do nazw folderów. ~~

~~Na przykład katalog spam Gmail spam jest nazywany:~~

```
[Gmail]/Spam
```

~~ Ustawiając prefiks przestrzeni nazw na * [Gmail] * FairEmail automatycznie usunie * [Gmail] / * ze wszystkich nazw folderów. ~~

<br />

<a name="faq55"></a>
**(55) Jak mogę oznaczyć wszystkie wiadomości jako przeczytane / przenieść lub usunąć wszystkie wiadomości?**

Możesz użyć do tego wielokrotnego wyboru. Długo naciśnij pierwszą wiadomość, nie podnoś palca i przesuń w dół do ostatniej wiadomości. Następnie użyj przycisku z trzema kropkami, aby wykonać żądane czynności.

<br />

<a name="faq56"></a>
**(56) Czy możesz dodać wsparcie dla JMAP?**

Nie ma prawie żadnych dostawców oferujących protokół [JMAP](https://jmap.io/), więc dodawanie wsparcia do FairEmaila nie jest warte dużego wysiłku.

<br />

<a name="faq57"></a>
**(57) Can I use HTML in signatures?**

Yes, you can use [HTML](https://en.wikipedia.org/wiki/HTML). In the signature editor you can switch to HTML mode via the three-dots menu.

Note that if you switch back to the text editor that not all HTML might be rendered as-is because the Android text editor is not able to render all HTML. Similarly, if you use the text editor, the HTML might be altered in unexpected ways.

If you want to use preformatted text, like [ASCII art](https://en.wikipedia.org/wiki/ASCII_art), you should wrap the text in a *pre* element, like this:

```
<pre>
  |\_/|
 / @ @ \
( > º < )
 `>>x<<´
 /  O  \
 </pre>
```

<br />

<a name="faq58"></a>
**(58) Co oznacza otwarta/zamknięta ikona e-mail?**

Ikona e-mail na liście folderów może być otwarta (niepełna) lub zamknięta (pełna):

![Obraz zewnętrzny](https://github.com/M66B/FairEmail/blob/master/images/baseline_mail_outline_black_48dp.png)

Treść wiadomości i załączniki nie są domyślnie pobierane.

![Obraz zewnętrzny](https://github.com/M66B/FairEmail/blob/master/images/baseline_email_black_48dp.png)

Treść wiadomości i załączniki są pobierane domyślnie.

<br />

<a name="faq59"></a>
**(59) Czy oryginalne wiadomości mogą być otwierane w przeglądarce?**

Ze względów bezpieczeństwa pliki z oryginalnymi treściami wiadomości nie są dostępne dla innych aplikacji, więc nie jest to możliwe. W teorii [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) może być użyta do udostępniania tych plików, ale nawet Google Chrome nie może tego obsłużyć.

<br />

<a name="faq60"></a>
**(60) Czy wiesz, ... ?**

* Czy wiesz, że wiadomości oznaczone gwiazdką można zawsze synchronizować / przechowywać? (można to włączyć w ustawieniach odbierania)
* Czy wiesz, że możesz długo nacisnąć ikonę 'napisz' aby przejść do folderu szkiców?
* Czy wiesz, że istnieje zaawansowana opcja oznaczania wiadomości jako przeczytane podczas ich przenoszenia? (archiwizacja i usuwanie to również przenoszenie)
* Czy wiesz, że możesz wybrać tekst (lub adres e-mail) w dowolnej aplikacji w najnowszych wersjach Androida i pozwolić FairEmail na wyszukiwanie?
* Czy wiesz, że FairEmail ma tryb tabletu? Obróć urządzenie w trybie poziomym, a wątki konwersacji zostaną otwarte w drugiej kolumnie, jeśli jest wystarczająco miejsca na ekranie.
* Czy wiesz, że możesz długo nacisnąć szablon odpowiedzi, aby utworzyć wersję roboczą wiadomości z szablonu?
* Czy wiesz, że możesz długo nacisnąć, przytrzymać i przesunąć, aby wybrać zakres wiadomości?
* Czy wiesz, że możesz ponowić wysyłanie wiadomości poprzez przeciąganie w dół, aby odświeżyć w skrzynce odbiorczej?
* Czy wiesz, że możesz przesunąć rozmowę w lewo lub w prawo, aby przejść do następnej lub poprzedniej rozmowy?
* Czy wiesz, że możesz kliknąć na obraz, aby zobaczyć, skąd zostanie pobrany (wyświetlić link)?
* Czy wiesz, że możesz przytrzymać ikonę folderu na pasku akcji, aby wybrać konto?
* Czy wiesz, że możesz długo nacisnąć ikonę gwiazdy w wątku konwersacji, aby ustawić kolorową gwiazdkę?
* Czy wiesz, że możesz otworzyć menu nawigacji, przesuwając palcem od lewej strony, nawet podczas oglądania rozmowy?
* Czy wiesz, że możesz długo nacisnąć ikonę osoby, aby wyświetlić/ukryć pola DW/UDW i zapamiętać stan widoczności następnym razem?
* Did you know that you can insert the email addresses of an Android contact group via the three dots overflow menu?
* Czy wiesz, że jeśli wybierzesz tekst i dotkniesz odpowiedz, tylko wybrany tekst zostanie zacytowany?
* Did you know that you can long press the trash icons (both in the message and the bottom action bar) to permanently delete a message or conversation? (version 1.1368+)
* Czy wiesz, że możesz długo nacisnąć przycisk wysyłania, aby wyświetlić okno dialogowe wysyłania, nawet jeśli zostało wyłączone?
* Czy wiesz, że możesz przytrzymać ikonę pełnego ekranu, aby wyświetlić tylko oryginalny tekst wiadomości?
* Did you know that you can long press the answer button to reply to the sender? (since version 1.1562)

<br />

<a name="faq61"></a>
**(61) Dlaczego niektóre wiadomości są przyciemnione?**

Wiadomości pokazane jako przyciemnione (wyszarzone) to wiadomości przeniesione lokalnie, dla których przeniesienie nie zostało jeszcze potwierdzone przez serwer. Może się to zdarzyć, gdy nie ma połączenia z serwerem lub kontem (jeszcze). Te wiadomości zostaną zsynchronizowane po połączeniu z serwerem i utworzeniu konta lub, jeśli tak się nigdy nie stanie, zostanią usunięte, jeśli są zbyt stare, aby je zsynchronizować.

Może być konieczne ręczne zsynchronizowanie folderu, na przykład poprzez przeciągnięcie w dół.

Możesz zobaczyć te wiadomości, ale nie możesz przenieść tych wiadomości ponownie, dopóki poprzedni ruch nie zostanie potwierdzony.

Oczekujące [operacje](#user-content-faq3) są wyświetlane w widoku operacji dostępnym z głównego menu nawigacji.

<br />

<a name="faq62"></a>
**(62) Które metody uwierzytelniania są obsługiwane?**

Następujące metody uwierzytelniania są obsługiwane i używane w tej kolejności:

* CRAM-MD5
* LOGIN
* PLAIN
* NTLM (untested)
* XOAUTH2 ([Gmail](https://developers.google.com/gmail/imap/xoauth2-protocol), [Yandex](https://tech.yandex.com/oauth/))

Metody uwierzytelniania SASL, poza CRAM-MD5, nie są obsługiwane ponieważ [JavaMail dla Android](https://javaee.github.io/javamail/Android) nie obsługuje uwierzytelniania SASL.

Jeśli Twój dostawca wymaga nieobsługiwanej metody uwierzytelniania, prawdopodobnie otrzymasz komunikat o błędzie *uwierzytelnianie nie powiodło się*.

[Client certificates](https://en.wikipedia.org/wiki/Client_certificate) can be selected in the account and identity settings.

[Server Name Indication](https://en.wikipedia.org/wiki/Server_Name_Indication) jest obsługiwane przez [wszystkie obsługiwane wersje Androida](https://developer.android.com/training/articles/security-ssl).

<br />

<a name="faq63"></a>
**(63) Jak zmienia się rozmiar obrazków do wyświetlania na ekranach?**

Duże wbudowane lub dołączone obrazy [ PNG ](https://en.wikipedia.org/wiki/Portable_Network_Graphics) i [ JPEG ](https://en.wikipedia.org/wiki/JPEG) zostaną automatycznie dostosowane do wyświetlania na ekranach. Wynika to z faktu, że wiadomości e-mail są ograniczone pod względem wielkości, w zależności od dostawcy w większości jest to od 10 do 50 MB. Obrazki będą domyślnie przeskalowane do maksymalnej szerokości i wysokości około 1440 pikseli i zapisane przy współczynniku kompresji 90 %. Obrazy są zmniejszane przy użyciu współczynników liczby całkowitej, aby zmniejszyć zużycie pamięci i zachować jakość obrazu. Automatyczna zmiana rozmiaru wstawionych i/lub załączonych obrazów oraz maksymalny rozmiar docelowego obrazu można skonfigurować w ustawieniach wysyłania.

Jeśli chcesz zmienić rozmiar zdjęć w poszczególnych przypadkach, możesz użyć [ Send Reduced ](https://f-droid.org/en/packages/mobi.omegacentauri.SendReduced/) lub podobnej aplikacji.

<br />

<a name="faq64"></a>
**~~(64) Czy możesz dodać własne akcje dla przesunięcia w lewo/prawo?~~**

~~Najbardziej naturalną rzeczą do zrobienia podczas przesuwania wpisu w lewo lub w prawo, jest usunięcie wpisu z listy.~~ ~~Najbardziej naturalną akcją w kontekście aplikacji e-mail jest przenoszenie wiadomości z folderu do innego folderu.~~ ~~Możesz wybrać folder, do którego chcesz przenieść w ustawieniach konta.~~

~~Inne działania, takie jak oznaczanie wiadomości przeczytanych i uśpionych są dostępne za pomocą wielokrotnego wyboru.~~ ~~Możesz długo nacisnąć wiadomość, aby rozpocząć wielokrotny wybór. Zobacz również [to pytanie](#user-content-faq55).~~

~~Przesuwanie w lewo lub w prawo, aby oznaczyć wiadomość przeczytaną lub nieprzeczytaną, jest nienaturalne, ponieważ wiadomość najpierw wychodzi, a później wraca w innym kształcie.~~ ~~Pamiętaj, że istnieje zaawansowana opcja automatycznego oznaczania wiadomości podczas przenoszenia,~~ ~~, który w większości przypadków jest idealnym zamiennikiem sekwencji odczytania i przeniesienia do jakiegoś folderu.~~ ~~Możesz również oznaczyć wiadomości przeczytane z powiadomień o nowych wiadomościach.~~

~~Jeśli chcesz przeczytać wiadomość później, możesz ją ukryć do określonego czasu używając menu *uśpij*.~~

<br />

<a name="faq65"></a>
**(65) Dlaczego niektóre załączniki są przyciemnione?**

Wbudowane załączniki (obrazy) są przyciemnione. [ Załączniki wbudowane ](https://tools.ietf.org/html/rfc2183) powinny zostać pobrane i wyświetlone automatycznie, ale ponieważ FairEmail nie zawsze automatycznie pobiera załączniki, zobacz także [ ten FAQ ](#user-content-faq40), FairEmail pokazuje wszystkie typy załączników. Aby rozróżnić załączniki wbudowane i zwykłe, załączniki wbudowane są wyświetlane jako przyciemnione.

<br />

<a name="faq66"></a>
**(66) Czy FairEmail jest dostępny w bibliotece rodzinnej Google Play?**

"*You can't share in-app purchases and free apps with your family members.*"

See [here](https://support.google.com/googleone/answer/7007852) under "*See if content is eligible to be added to Family Library*", "*Apps & games*".

<br />

<a name="faq67"></a>
**(67) Jak mogę uśpić rozmowy?**

Wybierz jedną lub więcej konwersacji (naciśnij długo, aby rozpocząć wielokrotne wybieranie), dotknij przycisku z trzema kropkami i wybierz * Odłóż ... *. Alternatively, in the expanded message view use *Snooze ...* in the message three-dots 'more' menu or the time-lapse action in the bottom action bar. Wybierz czas odłożenia konwersacji i potwierdź, naciskając OK. Rozmowy zostaną ukryte przez wybrany czas i pojawią się ponownie później. Otrzymasz powiadomienie o nowej wiadomości jako przypomnienie.

It is also possible to snooze messages with [a rule](#user-content-faq71), which will also allow you to move messages to a folder to let them be auto snoozed.

Możesz wyświetlić odłożone wiadomości, naciskając opcję * Odfiltruj* & # 062; * Ukryta * w menu z trzema kropkami.

Możesz dotknąć małą ikonę drzemki, aby zobaczyć, do kiedy rozmowa została odłożona.

Wybierając zerowy czas odłożenia możesz anulować drzemkę.

Third party apps do not have access to the Gmail snoozed messages folder.

<br />

<a name="faq68"></a>
**~~ (68) Dlaczego Adobe Acrobat nie może otwierać załączników PDF / aplikacje Microsoft nie mogą otwierać załączonych dokumentów? ~~**

~~Adobe Acrobat reader i aplikacje Microsoft nadal oczekują pełnego dostępu do wszystkich przechowywanych plików, ~~ ~~kiedy to aplikacje powinny używać [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) od Android KitKat (2013)~~, ~~aby mieć dostęp tylko do aktywnie udostępnianych plików. To jest z powodów prywatności i bezpieczeństwa.~~

~~Możesz to obejść zapisując załącznik i otwierając go w Adobe Acrobat reader / aplikacji Microsoft, ~~ ~~ale zaleca się zainstalowanie aktualnego i najlepiej otwartego czytnika PDF / czytnika dokumentów, ~~ ~~na przykład jeden z wymienionych [tutaj](https://github.com/offa/android-foss#-document--pdf-viewer).~~

<br />

<a name="faq69"></a>
**(69) Czy możesz dodać automatyczne przewijanie w górę przy nadejściu nowej wiadomości?**

Lista wiadomości jest automatycznie przewijana do góry podczas nawigacji z powiadomienia o nowej wiadomości lub po ręcznym odświeżeniu. Zawsze automatycznie przewijanie w górę po pojawieniu się nowych wiadomości będzie kolidowało z Twoim własnym przewijaniem, ale jeśli chcesz, możesz to włączyć w ustawieniach.

<br />

<a name="faq70"></a>
**(70) Kiedy wiadomości będą automatycznie rozwijane?**

Podczas nawigacji do rozmowy jedna wiadomość zostanie rozwinięta, jeśli:

* W konwersacji jest tylko jedna wiadomość
* W rozmowie jest dokładnie jedna nieprzeczytana wiadomość
* W rozmowie istnieje dokładnie jedna wiadomość oznaczona gwiazdką (ulubiona) (od wersji 1.1508)

Istnieje jeden wyjątek: wiadomość nie została jeszcze pobrana i wiadomość jest zbyt duża, aby pobrać automatycznie za pomocą połączenia taryfowego (komórkowego). Możesz ustawić lub wyłączyć maksymalny rozmiar wiadomości w ustawieniach połączenia.

Zduplikowane (zarchiwizowane) wiadomości, wiadomości w koszu i szkic wiadomości nie są liczone.

Wiadomości będą automatycznie oznaczane jako przeczytane po rozwinięciu, chyba że zostało to wyłączone w indywidualnych ustawieniach konta.

<br />

<a name="faq71"></a>
**(71) Jak stosować zasady filtrowania?**

You can edit filter rules by long pressing a folder in the folder list of an account (tap the account name in the navigation/side menu).

Nowe reguły zostaną zastosowane dla nowych wiadomości otrzymanych w folderze, a nie do istniejących wiadomości. Możesz sprawdzić regułę i zastosować regułę do istniejących wiadomości lub, alternatywnie, przytrzymaj regułę na liście reguł i wybierz *Wykonaj teraz*.

Musisz nadać nazwę reguły i musisz zdefiniować kolejność, w której reguła powinna być wykonywana w stosunku do innych reguł.

Możesz wyłączyć regułę i zatrzymać przetwarzanie innych reguł po wykonaniu reguły.

Dostępne są następujące warunki reguły:

* Sender contains or sender is contact
* Odbiorca zawiera
* Tytuł zawiera
* Has attachments (optional of specific type)
* Nagłówek zawiera
* Absolute time (received) between (since version 1.1540)
* Względny czas (otrzymany) między

Wszystkie warunki reguły muszą być spełnione, aby akcja reguły mogła zostać wykonana. Wszystkie warunki są opcjonalne, ale musi być co najmniej jeden warunek, aby nie pasować do wszystkich wiadomości. If you want to match all senders or all recipients, you can just use the @ character as condition because all email addresses will contain this character. If you want to match a domain name, you can use as a condition something like *@example.org*

Note that email addresses are formatted like this:

`
"Somebody" <somebody@example.org>`

Możesz użyć wielu reguł, prawdopodobnie z * zatrzymaniem przetwarzania *, dla warunku * lub * albo * nie *.

Dopasowanie nie uwzględnia wielkości liter, chyba że użyjesz [ wyrażeń regularnych ](https://en.wikipedia.org/wiki/Regular_expression). Proszę zobacz [tutaj](https://developer.android.com/reference/java/util/regex/Pattern) dokumentację wyrażeń regularnych Java. Możesz przetestować regex [tutaj](https://regexr.com/).

Note that a regular expression supports an *or* operator, so if you want to match multiple senders, you can do this:

`
.*alice@example\.org.*|.*bob@example\.org.*|.*carol@example\.org.*`

Zauważ, że włączony jest tryb [ dot all mode ](https://developer.android.com/reference/java/util/regex/Pattern#DOTALL) aby móc dopasować [ rozwinięte nagłówki ](https://tools.ietf.org/html/rfc2822#section-3.2.3).

Możesz wybrać jedną z tych akcji do zastosowania do pasujących wiadomości:

* Brak działań (przydatne dla *nie*)
* Oznacz jako przeczytaną
* Oznacz jako nieprzeczytaną
* Ukryj
* Pomiń powiadomienie
* Odłóż
* Dodaj gwiazdkę
* Ustaw ważność (priorytet lokalny)
* Dodaj słowo kluczowe
* Przenieś
* Kopiuj (Gmail: etykieta)
* Answer/forward (with template)
* Tekst na mowę (nadawca i temat)
* Automatyzacja (Tasker, itp.)

An error in a rule condition can lead to a disaster, therefore irreversible actions are not supported.

Reguły są stosowane bezpośrednio po pobraniu nagłówka wiadomości, ale przed pobraniem tekstu wiadomości, dlatego nie można zastosować warunków do tekstu wiadomości. Zauważ, że duże wiadomości są pobierane na żądanie przy połączeniu taryfowym, aby oszczędzić użycie danych.

Jeśli chcesz przekazać wiadomość, rozważ użycie akcji przenoszenia. Będzie to bardziej wiarygodne niż przesyłanie wiadomości, ponieważ przesyłane wiadomości mogą być uważane za spam.

Since message headers are not downloaded and stored by default to save on battery and data usage and to save storage space it is by default not possible to preview which messages would match a header rule condition. You can enable downloading message headers in the connection settings and check headers conditions anyway (since version 1.1599).

Some common header conditions (regex):

* *.&ast;Auto-Submitted:.&ast;* [RFC3834](https://tools.ietf.org/html/rfc3834)
* *.&ast;Content-Type: multipart/report.&ast;* [RFC3462](https://tools.ietf.org/html/rfc3462)

W wiadomości, w menu z trzema kropkami znajduje się element do utworzenia reguły * Utwórz regułę *dla odebranej wiadomości z wypełnionymi najczęstszymi warunkami.

Protokół POP3 nie obsługuje ustawiania słów kluczowych oraz przenoszenia lub kopiowania wiadomości.

Używanie reguł jest funkcją pro.

<br />

<a name="faq72"></a>
**(72) Czym są podstawowe konta/tożsamości?**

Konto główne jest używane, gdy konto jest niejednoznaczne, na przykład podczas uruchamiania nowego projektu ze zunifikowanej skrzynki odbiorczej.

Podobnie główna tożsamość konta jest używana, gdy tożsamość jest niejednoznaczna.

Może być tylko jedno konto podstawowe i może być tylko jedna podstawowa tożsamość na konto.

<br />

<a name="faq73"></a>
**(73) Czy przenoszenie wiadomości jest bezpieczne/wydajne?**

Przenoszenie wiadomości pomiędzy kontami jest bezpieczne, ponieważ surowe, oryginalne wiadomości zostaną pobrane i przeniesione oraz ponieważ wiadomości źródłowe zostaną usunięte dopiero po dodaniu wiadomości w miejscu docelowym

Przenoszenie wielu wiadomości pomiędzy kontami jest wydajne, jeśli zarówno folder źródłowy, jak i folder docelowy są ustawione na synchronizację, w przeciwnym razie FairEmail musi połączyć się z folderem(ami) dla każdej wiadomości.

<br />

<a name="faq74"></a>
**(74) Dlaczego widzę duplikaty wiadomości?**

Niektórzy dostawcy, zwłaszcza Gmail, wykazują wszystkie wiadomości we wszystkich folderach, również w folderze archiwum (wszystkie wiadomości), z wyjątkiem wiadomości w koszu. FairEmail pokazuje wszystkie te wiadomości w sposób nie narzucający się, aby wskazać, że te wiadomości są w rzeczywistości tą samą wiadomością.

Gmail pozwala jednej wiadomości na posiadanie wielu etykiet, które są prezentowane w FairEmail jako foldery. Oznacza to, że wiadomości z wieloma etykietami będą również wyświetlane wielokrotnie.

<br />

<a name="faq75"></a>
**(75) Czy możesz stworzyć wersję dla iOS, Windows, Linux, itd?**

Potrzeba jest mnóstwo wiedzy i doświadczenia, aby pomyślnie rozwijać aplikację dla konkretnej platformy, dlatego opracowuję aplikacje tylko dla Androida.

<br />

<a name="faq76"></a>
**(76) Do czego służy funkcja „Usuń wiadomości lokalne”?**

Menu folderów * Usuń wiadomości lokalne * usuwa wiadomości tylko z urządzenia. Wiadomości pozostają obecne na serwerze. To nie usuwa wiadomości z serwera. Może to być przydatne po zmianie ustawień folderu, aby nie pobierać treści wiadomości (tekstu i załączników), na przykład w celu zaoszczędzenia miejsca.

<br />

<a name="faq77"></a>
**(77) Dlaczego wiadomości są czasami wyświetlane z małym opóźnieniem?**

W zależności od prędkości urządzenia (szybkości procesora, a może nawet prędkości pamięci) wiadomości mogą być wyświetlane z niewielkim opóźnieniem. FairEmail jest zaprojektowany do dynamicznej obsługi dużej liczby wiadomości bez wyczerpywania pamięci. Oznacza to, że wiadomości muszą być odczytywane z bazy danych i ta baza danych musi być monitorowana pod kątem zmian, oba działania mogą spowodować niewielkie opóźnienia.

Niektóre wygodne funkcje, takie jak grupowanie wiadomości do wyświetlania wątków konwersacji i określania poprzedniej/następnej wiadomości, wymagają trochę dodatkowego czasu. Zauważ, że nie ma * następnej * wiadomości, ponieważ w międzyczasie mogła pojawić się nowa wiadomość.

Porównując prędkość FairEmail z podobnymi aplikacjami, powinno to być częścią porównania. Łatwo jest napisać podobną, szybszą aplikację, która tylko wyświetla listę wiadomości używając zbyt dużej ilości pamięci, ale nie jest to łatwe do prawidłowego zarządzania zasobami i oferowania bardziej zaawansowanych funkcji, takich jak wątek konwersacji.

FairEmail opiera się na najnowocześniejszych [ komponentach architektury Androida ](https://developer.android.com/topic/libraries/architecture/), więc jest mało miejsca na poprawę wydajności.

<br />

<a name="faq78"></a>
**(78) Jak korzystać z harmonogramów?**

In the receive settings you can enable scheduling and set a time period and the days of the week *when* messages should be *received*. Należy pamiętać, że czas zakończenia równy lub wcześniejszy niż czas rozpoczęcia uważa się za 24 godziny później.

Automation, see below, can be used for more advanced schedules, like for example multiple synchronization periods per day or different synchronization periods for different days.

Możliwe jest zainstalowanie FairEmail w wielu profilach użytkowników, na przykład profil osobisty i służbowy, oraz skonfigurowanie FairEmail inaczej w każdym profilu, co jest kolejną możliwością posiadania różnych harmonogramów synchronizacji i synchronizacji innego zestawu kont.

It is also possible to create [filter rules](#user-content-faq71) with a time condition and to snooze messages until the end time of the time condition. This way it is possible to *snooze* business related messages until the start of the business hours. This also means that the messages will be on your device for when there is (temporarily) no internet connection.

Note that recent Android versions allow overriding DND (Do Not Disturb) per notification channel and per app, which could be used to (not) silence specific (business) notifications. Please [see here](https://support.google.com/android/answer/9069335) for more information.

W przypadku bardziej złożonych schematów możesz ustawić jedno lub więcej kont do ręcznej synchronizacji i wysłać to polecenie do FairEmail w celu sprawdzenia nowych wiadomości:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL
```

Dla konkretnego konta:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL --es account Gmail
```

Możesz również zautomatyzować włączanie i wyłączanie odbierania wiadomości, wysyłając te polecenia do FairEmail:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE
```

Aby włączyć/wyłączyć określone konto:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE --es account Gmail
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE --es account Gmail
```

Pamiętaj, że wyłączenie konta spowoduje ukrycie konta oraz wszystkich powiązanych folderów i wiadomości. From version 1.1600 an account will be disabled/enabled by setting the account to manual/automatic sync, so the folders and messages keep being accessible.

To set the poll interval:

```
(adb shell) adb shell am start-foreground-service -a eu.faircode.email.INTERVAL --ei minutes nnn
```

Where *nnn* is one of 0, 15, 30, 60, 120, 240, 480, 1440. A value of 0 means push messages.

Możesz automatycznie wysyłać polecenia, np. [Tasker](https://tasker.joaoapps.com/userguide/en/intents.html):

```
Nowe zadanie: Coś rozpoznawalnego
Kategoria akcji: Różne / Wyślij zamiar
Działanie: eu.faircode.email.ENABLE
Cel: usługa
```

Aby włączyć/wyłączyć konto o nazwie *Gmail*:

```
Dodatki: konto:Gmail
```

W nazwach kont rozróżniana jest wielkość liter.

Harmonogram jest funkcją pro.

<br />

<a name="faq79"></a>
**(79) Jak korzystać z synchronizacji na żądanie (ręcznej)?**

Zazwyczaj, gdy tylko jest to możliwe, FairEmail utrzymuje połączenie ze skonfigurowanymi serwerami e-mail, aby odbierać wiadomości w czasie rzeczywistym. Jeśli tego nie chcesz, aby nie przeszkadzać lub na przykład aby oszczędzać baterię, po prostu wyłącz odbiór w ustawieniach odbioru. To zatrzyma usługę w tle, która zajmuje się automatyczną synchronizacją i usunie powiązane powiadomienie na pasku statusu.

Możesz również włączyć *Synchronizuj ręcznie* w zaawansowanych ustawieniach konta, jeśli chcesz ręcznie synchronizować tylko określone konta.

Możesz użyć przeciągnięcia w dół na liście wiadomości, aby odświeżyć lub użyć menu folderu *Synchronizuj teraz*, aby ręcznie zsynchronizować wiadomości.

Jeśli chcesz ręcznie zsynchronizować niektóre lub wszystkie foldery konta, po prostu wyłącz synchronizację dla folderów (ale nie konto).

Prawdopodobnie chcesz wyłączyć [przeglądaj również na serwerze](#user-content-faq24).

<br />

<a name="faq80"></a>
**~~(80) Jak mogę naprawić błąd 'Unable to load BODYSTRUCTURE' ?~~**

~~Komunikat o błędzie *Unable to load BODYSTRUCTURE* jest spowodowany błędami na serwerze e-mail,~~ ~~po więcej szczegółów zobacz [tutaj](https://javaee.github.io/javamail/FAQ#imapserverbug).~~

~~FairEmail już próbuje obsłużyć te błędy, ale jeśli to się nie uda, musisz poprosić swojego dostawcę o wsparcie.~~

<br />

<a name="faq81"></a>
**~~(81) Czy możesz zmienić tło oryginalnej wiadomości na ciemne przy ciemnym motywie?~~**

~~Oryginalna wiadomość jest wyświetlana tak jak nadawca wysłał ją wraz ze wszystkimi kolorami.~~ ~~Zmiana koloru tła nie tylko sprawi, że oryginalny widok już nie jest oryginalny, może również skutkować nieczytelnymi wiadomościami.~~

<br />

<a name="faq82"></a>
**(82) Co to jest obraz śledzący?**

Zobacz [tutaj](https://en.wikipedia.org/wiki/Web_beacon) informacje o tym, co dokładnie jest obraz śledzenia. Obrazki śledzące sprawdzają czy otworzyłeś wiadomość.

FairEmail w większości przypadków automatycznie rozpozna śledzące obrazy i zastąpi je tą ikoną:

![Obraz zewnętrzny](https://github.com/M66B/FairEmail/blob/master/images/baseline_my_location_black_48dp.png)

Automatyczne rozpoznawanie obrazów śledzących może być wyłączone w ustawieniach prywatności.

<br />

<a name="faq84"></a>
**(84) Co czego służą kontakty lokalne?**

Lokalne informacje kontaktowe opierają się na nazwach i adresach znalezionych w przychodzących i wychodzących wiadomościach.

Głównym zastosowaniem lokalnego magazynu kontaktów jest oferowanie automatycznego uzupełniania, gdy FairEmail nie otrzymał zgody na dostęp do kontaktów.

Innym sposobem jest wygenerowanie [skrótów](#user-content-faq31) na najnowszych wersjach Androida, aby szybko wysłać wiadomość do częstych kontaktów. Z tego powodu rejestrowana jest liczba kontaktów i ostatni kontakt i dlaczego możesz ustawić kontakt jako ulubiony lub wykluczyć go z ulubionych przez długie naciśnięcie.

Lista kontaktów jest sortowana według liczby nawiązanych i ostatnich kontaktów.

Domyślnie tylko nazwy i adresy, do których wysyłasz wiadomości, będą rejestrowane. Możesz to zmienić w ustawieniach wysyłania.

<br />

<a name="faq85"></a>
**(85) Dlaczego tożsamość nie jest dostępna?**

Tożsamość jest dostępna do wysłania nowej wiadomości lub odpowiedzi lub przesłania istniejącej wiadomości tylko wtedy, gdy:

* tożsamość jest ustawiona do synchronizacji (wysyłanie wiadomości)
* powiązane konto jest ustawione do synchronizacji (odbierania wiadomości)
* powiązane konto ma folder szkiców

FairEmail spróbuje wybrać najlepszą tożsamość na podstawie adresu *do* wiadomości odebranej / przekazywanej.

<br />

<a name="faq86"></a>
**~~(86) Jakie są 'dodatkowe funkcje prywatności'?~**

~~Opcja zaawansowana *dodatkowe funkcje prywatności* włącza:~~

* ~~Szukanie właściciela adresu IP odnośnika~~
* ~~Wykrywanie i usuwanie [obrazów śledzących](#user-content-faq82)~~

<br />

<a name="faq87"></a>
**(87) Co oznacza „nieprawidłowe dane uwierzytelniające”?**

Komunikat o błędzie *nieprawidłowe dane logowania* oznacza, że nazwa użytkownika i/lub hasło są nieprawidłowe, na przykład dlatego, że hasło zostało zmienione lub wygasło, lub że autoryzacja konta wygasła.

Jeśli hasło jest niepoprawne/wygasło, musisz zaktualizować hasło w ustawieniach konta i/lub identyfikacji.

Jeśli autoryzacja konta wygasła, musisz ponownie wybrać konto. Prawdopodobnie będziesz musiał ponownie zapisać powiązaną tożsamość.

<br />

<a name="faq88"></a>
**(88) Jak mogę korzystać z konta Yahoo, AOL lub Sky?**

The preferred way to set up a Yahoo account is by using the quick setup wizard, which will use OAuth instead of a password and is therefore safer (and easier as well).

Aby autoryzować konto Yahoo, AOL lub Sky, musisz utworzyć hasło aplikacji. Instrukcje znajdują się tutaj:

* [dla Yahoo](https://help.yahoo.com/kb/generate-third-party-passwords-sln15241.html)
* [dla AOL](https://help.aol.com/articles/Create-and-manage-app-password)
* [ dla Sky ](https://www.sky.com/help/articles/getting-started-with-sky-yahoo-mail) (w * Other email apps *)

Zobacz tutaj [FAQ](#user-content-faq111) na temat wsparcia OAuth.

Pamiętaj, że Yahoo, AOL i Sky nie obsługują standardowych wiadomości push. Aplikacja e-mail Yahoo używa zastrzeżonego, nieudokumentowanego protokołu dla wiadomości push.

Wiadomości push wymagają [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE) a serwer e-mail Yahoo nie zgłasza zdolności IDLE:

```
Y1 CAPABILITY
* CAPABILITY IMAP4rev1 ID MOVE NAMESPACE XYMHIGHESTMODSEQ UIDPLUS LITERAL+ CHILDREN X-MSG-EXT UNSELECT OBJECTID
Y1 OK CAPABILITY completed
```

<br />

<a name="faq89"></a>
**(89) Jak mogę wysyłać wiadomości czystym tekstem?**

Domyślnie FairEmail wysyła każdą wiadomość zarówno jako zwykły tekst, jak i jako tekst sformatowany HTML, ponieważ prawie każdy odbiorca oczekuje obecnie sformatowanych wiadomości. Jeśli chcesz/chcesz wysyłać tylko wiadomości tekstowe, możesz to włączyć w zaawansowanych opcjach tożsamości. Możesz utworzyć nową tożsamość, jeśli chcesz / musisz wybrać wysyłanie zwykłych wiadomości tekstowych dla poszczególnych przypadków.

<br />

<a name="faq90"></a>
**(90) Dlaczego niektóre teksty są linkami, podczas gdy nie są linkami?**

Dla Twojej wygody FairEmail automatycznie połączy niepowiązane linki internetowe (http i https) i niepowiązane adresy e-mail (mailto). Jednak teksty i linki nie są łatwe do odróżnienia, zwłaszcza gdy wiele [domen najwyższego poziomu](https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains) to słowa. Dlatego też teksty z kropkami są czasami niepoprawnie uznawane za linki, co jest lepsze niż brak rozpoznawania niektórych linków.

Links for the tel, geo, rtsp and xmpp protocols will be recognized too, but links for less usual or less safe protocols like telnet and ftp will not be recognized. The regex to recognize links is already *very* complex and adding more protocols will make it only slower and possibly cause errors.

Note that original messages are shown exactly as they are, which means also that links are not automatically added.

<br />

<a name="faq91"></a>
**~~(91) Czy możesz dodać okresową synchronizację w celu oszczędzania energii baterii?~~**

~~Synchronizowanie wiadomości jest kosztownym procesem, ponieważ lokalne i zdalne wiadomości muszą być porównane,~~ ~~~więc okresowa synchronizacja wiadomości nie spowoduje oszczędności energii baterii, co bardziej prawdopodobne wręcz przeciwnie.~~

~~Zobacz [ten FAQ](#user-content-faq39) o optymalizacji użycia baterii.~~

<br />

<a name="faq92"></a>
**(92) Czy możesz dodać filtrowanie spamu, weryfikację podpisu DKIM i autoryzację SPF?**

Filtrowanie spamu, weryfikacja podpisu [DKIM](https://en.wikipedia.org/wiki/DomainKeys_Identified_Mail) i autoryzacja [SPF](https://en.wikipedia.org/wiki/Sender_Policy_Framework) jest zadaniem serwerów e-mail, a nie klienta e-mail. Serwery mają zazwyczaj więcej pamięci i mocy obliczeniowej, więc są znacznie lepiej dostosowane do tego zadania niż urządzenia zasilane baterią. Będziesz także chciał filtrować spam dla wszystkich swoich klientów e-mail, w tym również poczty e-mail, a nie tylko jednego klienta. Ponadto serwery e-mail mają dostęp do informacji, takich jak adres IP, itp. serwera łączącego, do którego klient poczty elektronicznej nie ma dostępu.

Spam filtering based on message headers might have been feasible, but unfortunately this technique is [patented by Microsoft](https://patents.google.com/patent/US7543076).

Recent versions of FairEmail can filter spam to a certain extend using a message classifier. Please see [this FAQ](#user-content-faq163) for more information about this.

Oczywiście możesz zgłaszać w FairEmail wiadomości jako spam, który przeniesie zgłoszone wiadomości do folderu spam i wyszkoli sposób w jaki ma funkcjonować filtr spamu dostawcy. Można to zrobić automatycznie z [zasadami filtrowania](#user-content-faq71). Blocking the sender will create a filter rule to automatically move future messages of the same sender into the spam folder.

Note that the POP3 protocol gives access to the inbox only. So, it is won't be possible to report spam for POP3 accounts.

Note that you should not delete spam messages, also not from the spam folder, because the email server uses the messages in the spam folder to "learn" what spam messages are.

If you receive a lot of spam messages in your inbox, the best you can do is to contact the email provider to ask if spam filtering can be improved.

FairEmail może również pokazać mały czerwony znacznik ostrzegawczy gdy DKIM, SPF lub uwierzytelnianie [DMARC](https://en.wikipedia.org/wiki/DMARC) nie powiodło się na serwerze odbierającym. Możesz włączyć/wyłączyć [weryfikację uwierzytelniania](https://en.wikipedia.org/wiki/Email_authentication) w ustawieniach wyświetlania.

FairEmail może również wyświetlić flagę ostrzegawczą, jeśli nazwa domeny adresu e-mail (odpowiedź) nadawcy nie definiuje rekordu MX wskazującego na serwer e-mail. Ta opcja może być włączona w ustawieniach odbierania. Pamiętaj, że to znacznie spowolni synchronizację wiadomości.

If the domain name of the sender and the domain name of the reply address differ, the warning flag will be shown too because this is most often the case with phishing messages. If desired, this can be disabled in the receive settings (from version 1.1506).

Jeżeli prawowite wiadomości nie są uwierzytelniane, powinieneś powiadomić nadawcę, ponieważ spowoduje to wysokie ryzyko pojawienia się wiadomości w folderze spam. Ponadto bez prawidłowego uwierzytelniania istnieje ryzyko, że pod nadawcę można się podszyć. Nadawca może użyć [tego narzędzia](https://www.mail-tester.com/) do sprawdzania uwierzytelniania i innych rzeczy.

<br />

<a name="faq93"></a>
**(93) Czy możesz zezwolić na instalację/przechowywanie danych na zewnętrznych nośnikach pamięci (karta SD)?**

FairEmail korzysta z usług i alarmów, zapewnia widżety i nasłuchuje podczas uruchamiania, więc nie jest możliwe przechowywanie aplikacji na zewnętrznych nośnikach pamięci, jak karta pamięci. Zobacz również [tutaj](https://developer.android.com/guide/topics/data/install-location).

Wiadomości, załączniki, itp., przechowywane na zewnętrznych nośnikach, jak karta pamięci, mogą być dostępne dla innych aplikacji i dlatego nie są bezpieczne. Szczegóły znajdziesz w [tutaj](https://developer.android.com/training/data-storage).

W razie potrzeby możesz zapisać wiadomości (Raw) za pomocą menu trzech kropek tuż nad tekstem wiadomości i zapisać załączniki naciskając ikonę dyskietki.

Jeśli chcesz zaoszczędzić miejsce, możesz ograniczyć liczbę dni synchronizacji i przechowywania wiadomości. Gdy jest to konieczne lub pożądane, możesz to zmienić przez długie naciśnięcie folderu na liście folderów i wybranie *Edytuj właściwości*.

<br />

<a name="faq94"></a>
**(94) Co oznacza czerwony/pomarańczowy pasek na końcu nagłówka?**

Czerwony/pomarańczowy pasek po lewej stronie nagłówka oznacza, że uwierzytelnienie DKIM, SPF lub DMARC nie powiodło się. Zobacz również [ten FAQ](#user-content-faq92).

<br />

<a name="faq95"></a>
**(95) Dlaczego nie wszystkie aplikacje są wyświetlane przy wyborze załącznika lub obrazu?**

Ze względów prywatności i bezpieczeństwa FairEmail nie ma uprawnień do bezpośredniego dostępu do plików, zamiast tego używa Storage Access Framework do dostępu do pamięci, jest to dostępne i rekomendowane od Androida 4.4 KitKat (wydany w 2013 r.) i jest to używane do wybierania plików.

To czy aplikacja jest dostępna na liście, zależy od tego, czy aplikacja implementuje [document provider](https://developer.android.com/guide/topics/providers/document-provider). Jeśli aplikacja nie jest na liście, być może będziesz musiał poprosić twórcę aplikacji o dodanie wsparcia dla Storage Access Framework.

Android Q utrudni i może nawet uniemożliwi bezpośredni dostęp do plików, zobacz [tutaj](https://developer.android.com/preview/privacy/scoped-storage) i [tutaj](https://www.xda-developers.com/android-q-storage-access-framework-scoped-storage/) aby uzyskać więcej informacji.

<br />

<a name="faq96"></a>
**(96) Gdzie mogę znaleźć ustawienia IMAP i SMTP?**

Ustawienia IMAP są częścią (niestandardowych) ustawień konta, a ustawienia SMTP są częścią ustawień identyfikacyjnych.

<br />

<a name="faq97"></a>
**(97) Co to jest 'czyszczenie' ?**

Co około cztery godziny FairEmail wykonuje zadanie czyszczenia, które:

* Usuwa stare treści wiadomości
* Usuwa stare pliki załączników
* Usuwa stare pliki obrazów
* Usuwa stare kontakty lokalne
* Usuwa stare wpisy dziennika

Zauważ, że zadanie czyszczenia zostanie uruchomione tylko wtedy, gdy usługa synchronizacji jest aktywna.

<br />

<a name="faq98"></a>
**(98) Dlaczego nadal mogę wybrać kontakty po odwołaniu uprawnień do kontaktów?**

Po cofnięciu uprawnień do kontaktów Android nie zezwala na dostęp FairEmail do Twoich kontaktów. Wybieranie kontaktów jest jednak przekazywane i wykonywane przez Androida, a nie przez FairEmail, więc będzie to możliwe bez uprawnień do kontaktów.

<br />

<a name="faq99"></a>
**(99) Czy możesz dodać Rich text lub Markdown edytor?**

FairEmail zapewnia ogólne formatowanie tekstu (pogrubienie, kursywa, podkreślenia, rozmiar tekstu i kolor) przez pasek narzędzi, który pojawia się po wybraniu jakiegoś tekstu.

Edytor [Rich text](https://en.wikipedia.org/wiki/Formatted_text) lub [Markdown](https://en.wikipedia.org/wiki/Markdown) nie byłby używany przez wiele osób na małym urządzeniu mobilnym oraz co ważniejsze, Android nie obsługuje rich text edytora i najbardziej rozbudowane projekty edytorów tekstu open source są porzucane. Zobacz [tutaj](https://forum.xda-developers.com/showpost.php?p=79061829&postcount=4919), aby uzyskać więcej informacji na ten temat.

<br />

<a name="faq100"></a>
**(100) Jak mogę zsynchronizować kategorie Gmail?**

Możesz zsynchronizować kategorie Gmail, tworząc filtry do oznaczania wiadomości skategoryzowanych:

* Utwórz nowy filtr przez Gmail > Ustawienia (koło) > Filtry i zablokowane adresy > Utwórz nowy filtr
* Wprowadź wyszukiwanie kategorii (patrz poniżej) w polu * Zawiera słowa * i kliknij * Utwórz filtr *
* Zaznacz * Zastosuj etykietę *, wybierz etykietę i kliknij * Utwórz filtr *

Możliwe kategorie:

```
kategoria: społeczna
kategoria: aktualizacje
kategoria: fora
kategoria: promocje
```

Niestety nie jest to możliwe w przypadku folderu odłożonych wiadomości.

Możesz użyć *Wymuś synchronizację* w menu trzech kropek wspólnej skrzynki odbiorczej, aby umożliwić FairEmail zsynchronizowanie listy folderów i długo nacisnąć foldery, aby włączyć synchronizację.

<br />

<a name="faq101"></a>
**(101) Co oznacza niebiesko-pomarańczowa kropka na dole rozmowy?**

Kropka pokazuje względne położenie rozmowy na liście wiadomości. Kropka będzie wyświetlana na pomarańczowo, gdy rozmowa będzie pierwszą lub ostatnią na liście wiadomości, w przeciwnym razie będzie niebieska. Kropka ma służyć jako pomoc przy przesuwaniu w lewo / w prawo, aby przejść do poprzedniej / następnej rozmowy.

Kropka jest domyślnie wyłączona i może być włączona w ustawieniach wyświetlania *Pokaż względną pozycję rozmowy za pomocą kropki*.

<br />

<a name="faq102"></a>
**(102) Jak mogę włączyć automatyczne obracanie obrazów?**

Obrazy będą automatycznie obracane po włączeniu automatycznej zmiany rozmiaru obrazów w ustawieniach (domyślnie włączone). Jednakże automatyczna rotacja zależy od danych [Exif](https://en.wikipedia.org/wiki/Exif), które będą obecne i będą poprawne, co nie zawsze ma miejsce. Szczególnie, nie ma miejsca przy robieniu zdjęć za pomocą aplikacji aparatu z FairEmail.

Zauważ, że tylko obrazy [JPEG](https://en.wikipedia.org/wiki/JPEG) i [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) mogą zawierać dane Exif.

<br />

<a name="faq104"></a>
**(104) Co muszę wiedzieć o zgłaszaniu błędów?**

* Raportowanie błędów pomoże ulepszyć FairEmail
* Zgłaszanie błędów jest opcjonalne i opt-in
* Raportowanie błędów może być włączone/wyłączone w ustawieniach, sekcja Różne
* Raporty o błędach będą automatycznie wysyłane anonimowo do [Bugsnag](https://www.bugsnag.com/)
* Bugsnag dla Androida jest [otwarto-źródłowy](https://github.com/bugsnag/bugsnag-android)
* Zobacz [tutaj](https://docs.bugsnag.com/platforms/android/automatically-captured-data/) informacje o tym, jakie dane zostaną wysłane w przypadku błędów
* Zobacz [tutaj](https://docs.bugsnag.com/legal/privacy-policy/) politykę prywatności Bugsnag
* Raporty o błędach zostaną wysłane do *sessions.bugsnag.com:443* i *notify.bugsnag.com:443*

<br />

<a name="faq105"></a>
**(105) Jak działa opcja Roaming jak w domu?**

FairEmail sprawdzi, czy kod kraju karty SIM i kod kraju sieci znajdują się w [krajach UE (roaming jak w domu)](https://en.wikipedia.org/wiki/European_Union_roaming_regulations#Territorial_extent) i nie zakłada roamingu, jeśli kody kraju są takie same i włączona jest zaawansowana opcja roaming jak w domu.

Ie musisz więc wyłączać tej opcji, jeśli nie masz karty SIM UE lub nie jesteś podłączony do sieci UE.

<br />

<a name="faq106"></a>
**(106) Które launchery mogą pokazać licznik z liczbą nieprzeczytanych wiadomości?**

[zobacz tutaj ](https://github.com/leolin310148/ShortcutBadger#supported-launchers), aby wyświetlić listę launcherów, które mogą pokazać liczbę nieprzeczytanych wiadomości. Standard Android [does not support this](https://developer.android.com/training/notify-user/badges).

Zauważ, że Nova Launcher wymaga Tesla Unread, który [nie jest już wspierany](https://forum.xda-developers.com/android/general/bad-news-tesla-unread-devoloper-t3920415).

Pamiętaj, że ustawienia powiadomień *Pokaż ikonę launchera z liczbą nowych wiadomości* muszą być włączone (domyślnie włączone).

Liczone będą tylko * nowe * nieprzeczytane wiadomości w folderach skonfigurowanych do wyświetlania powiadomień o nowych wiadomościach, więc wiadomości oznaczone jako nieprzeczytane ponownie, a wiadomości w folderach ustawionych tak, aby nie wyświetlały powiadomienia o nowej wiadomości, nie będą liczone.

Depending on what you want, the notification settings *Let the number of new messages match the number of notifications* needs to be enabled (default disabled). When enabled the badge count will be the same as the number of new message notifications. When disabled the badge count will be the number of unread messages, independent if they are shown in a notification or are new.

Ta funkcja zależy od wsparcia Twojego launchera. FairEmail tylko 'transmituje' liczbę nieprzeczytanych wiadomości za pomocą biblioteki ShortcutBadger. Jeśli to nie działa, nie może to zostać naprawione przez zmiany w FairEmail.

Some launchers display a dot or a '1' for [the monitoring notification](#user-content-faq2), despite FairEmail explicitly requesting not to show a *badge* for this notification. Może to być spowodowane błędem w aplikacji launchera lub w Twojej wersji Androida. Please double check if the notification dot (badge) is disabled for the receive (service) notification channel. Możesz przejść do odpowiednich ustawień kanału powiadomień poprzez ustawienia powiadomień FairEmail. To może nie być oczywiste, ale możesz dotknąć nazwy kanału, aby uzyskać więcej ustawień.

FairEmail wysyła nową liczbę wiadomości:

```
eu.faircode.email.NEW_MESSAGE_COUNT
```

Liczba nowych, nieprzeczytanych wiadomości będzie w parametrze "*count*".

<br />

<a name="faq107"></a>
**(107) Jak używać kolorowych gwiazdek?**

Możesz ustawić kolorową gwiazdkę za pomocą menu wiadomości *więcej* poprzez wybór wielu (rozpoczęty przez długie naciśnięcie wiadomości), przez długie naciśnięcie gwiazdy w konwersacji lub automatyczne użycie [reguły](#user-content-faq71).

Musisz wiedzieć, że kolorowe gwiazdki nie są obsługiwane przez protokół IMAP i dlatego nie mogą być zsynchronizowane z serwerem e-mail. Oznacza to, że kolorowe gwiazdy nie będą widoczne w innych klientach poczty elektronicznej i zostaną utracone podczas ponownego pobierania wiadomości. Jednakże gwiazdy (bez koloru) będą synchronizowane i będą widoczne w innych klientach e-mail, jeśli są obsługiwane.

Niektórzy klienci poczty elektronicznej używają słów kluczowych IMAP dla kolorów. Jednak nie wszystkie serwery obsługują słowa kluczowe IMAP i poza tym nie ma standardowych słów kluczowych dla kolorów.

<br />

<a name="faq108"></a>
**~(108) Czy możesz dodać trwałe usuwanie wiadomości z dowolnego folderu?~**

~~Po usunięciu wiadomości z folderu wiadomości zostaną przeniesione do folderu kosza, więc masz szansę przywrócić wiadomości.~~ ~~Możesz trwale usunąć wiadomości z folderu kosza.~~ ~~Trwałe usuwanie wiadomości z innych folderów zniszczyłoby cel folderu kosza, więc nie zostanie to dodane.~~

<br />

<a name="faq109"></a>
**~~ (109) Dlaczego funkcja „wybierz konto” jest dostępna tylko w oficjalnych wersjach? ~~**

~~ Korzystanie z * wybierz konto * do wyboru i autoryzacji kont Google wymaga specjalnego zezwolenia od Google ze względów bezpieczeństwa i prywatności. ~~ ~~ To specjalne uprawnienie można uzyskać tylko dla aplikacji, którymi programista zarządza i za które jest odpowiedzialny. ~~ ~~ Kompilacje stron trzecich, podobnie jak kompilacje F-Droid, są zarządzane przez strony trzecie i są odpowiedzialne za te strony trzecie. ~~ ~~ Tak więc tylko te osoby trzecie mogą uzyskać wymagane pozwolenie od Google. ~~ ~~ Ponieważ te strony trzecie faktycznie nie obsługują FairEmail, najprawdopodobniej nie będą żądać wymaganego pozwolenia. ~~

~~Możesz to rozwiązać na dwa sposoby:~~

* ~~Przełącz na oficjalną wersję FairEmail, zobacz [tutaj](https://github.com/M66B/FairEmail/blob/master/README.md#downloads) dla opcji~~
* ~~ Używaj haseł specyficznych dla aplikacji, zobacz [ to FAQ ](#user-content-faq6)~~

~~ Używanie * wybierz konto * w kompilacjach innych firm nie jest już możliwe w najnowszych wersjach. ~~ ~~ W starszych wersjach było to możliwe, ale spowoduje to błąd * UNREGISTERED_ON_API_CONSOLE *. ~~

<br />

<a name="faq110"></a>
**(110) Dlaczego (niektóre) wiadomości są puste i / lub załączniki są uszkodzone?**

Puste wiadomości i/lub uszkodzone załączniki są prawdopodobnie spowodowane błędem w oprogramowaniu serwera. Wiadomo, że starsze oprogramowanie Microsoft Exchange powoduje ten problem. W większości możesz to zrobić wyłączając *Pobieranie częściowe* w zaawansowanych ustawieniach konta:

Ustawienia > Ręczna konfiguracja > Konta > dotknij konto > dotknij Zaawansowane > Odznacz > Pobieranie częściowe

Po wyłączeniu tego ustawienia możesz użyć menu „więcej” wiadomości (trzy kropki), aby „zsynchronizować” puste wiadomości. Alternatywnie, możesz *Usunąć wiadomości lokalne * przez długie naciśnięcie folderu(ów) na liście folderów i zsynchronizować wszystkie wiadomości ponownie.

Wyłączenie *Pobieranie częściowe* spowoduje większe użycie pamięci.

<br />

<a name="faq111"></a>
**(111) Czy OAuth jest wspierane?**

OAuth dla Gmail jest obsługiwany za pomocą kreatora szybkich ustawień. Menedżer kont Android będzie używany do pobierania i odświeżania tokenów OAuth dla wybranych kont na urządzeniu. OAuth dla kont innych niż na urządzeniu nie jest obsługiwany ponieważ Google wymaga w tym celu [rocznego audytu bezpieczeństwa](https://support.google.com/cloud/answer/9110914) (15,000 do 75,000 USD). You can read more about this [here](https://www.theregister.com/2019/02/11/google_gmail_developer/).

OAuth for Outlook/Office 365, Yahoo, Mail.ru and Yandex is supported via the quick setup wizard.

The OAuth [jump page](https://oauth.faircode.eu/) exists for when [Android App Links](https://developer.android.com/training/app-links/verify-site-associations) are not available, for example when using a non Play store version of the app, or do not work for some reason.

<br />

<a name="faq112"></a>
**(112) Którego dostawcę poczty elektronicznej polecasz?**

FairEmail is an email client only, so you need to bring your own email address. Note that this is clearly mentioned in the app description.

There are plenty of email providers to choose from. Który dostawca poczty e-mail jest dla Ciebie najlepszy, zależy od Twoich życzeń/wymagań. Odwiedź strony internetowe [ Restore privacy ](https://restoreprivacy.com/secure-email/) lub [ Privacy Tools ](https://www.privacytools.io/providers/email/) i sprawdź listę dostawców e-mail zorientowanych na prywatność z zaletami i wadami.

Some providers, like ProtonMail, Tutanota, use proprietary email protocols, which make it impossible to use third party email apps. Please see [this FAQ](#user-content-faq129) for more information.

Używanie własnej (niestandardowej) nazwy domeny, obsługiwanej przez większość dostawców poczty e-mail, ułatwi przejście do innego dostawcy poczty e-mail.

<br />

<a name="faq113"></a>
**(113) Jak działa uwierzytelnianie biometryczne?**

If your device has a biometric sensor, for example a fingerprint sensor, you can enable/disable biometric authentication in the navigation (hamburger) menu of the settings screen. Po włączeniu FairEmail będzie wymagał uwierzytelniania biometrycznego po okresie bezczynności lub po wyłączeniu ekranu podczas działania FairEmail. Aktywnością jest nawigacja w FairEmail, na przykład otwieranie wątku konwersacji. Czas trwania okresu braku aktywności może być skonfigurowany w ustawieniach różnych. Gdy uwierzytelnianie biometryczne jest włączone, nowe powiadomienia nie będą wyświetlać żadnych treści, a FairEmail nie będzie widoczny na ekranie ostatnich aplikacji Android.

Uwierzytelnianie biometryczne ma na celu uniemożliwienie innym wyświetlania tylko Twoich wiadomości. FairEmail opiera się na szyfrowaniu danych na urządzeniu, zobacz również [ten FAQ](#user-content-faq37).

Uwierzytelnianie biometryczne jest funkcją pro.

<br />

<a name="faq114"></a>
**(114) Czy możesz dodać import dla ustawień z innych aplikacji e-mail?**

Format ustawień większości innych aplikacji e-mail nie jest udokumentowany, więc jest to trudne. Czasami możliwy jest 'reverse engineer' formatu, ale jak tylko format ustawień się zmieni, wszystko zostanie przerwane. Ponadto ustawienia są często niekompatybilne. For example, FairEmail has unlike most other email apps settings for the number of days to synchronize messages and for the number of days to keep messages, mainly to save on battery usage. Moreover, setting up an account/identity with the quick setup wizard is simple, so it is not really worth the effort.

<br />

<a name="faq115"></a>
**(115) Czy możesz dodać chipy na adres e-mail?**

Adres e-mail [ chips ](https://material.io/design/components/chips.html) wygląda ładnie, ale nie można go edytować, co jest dość niewygodne, gdy popełniłeś literówkę w adresie e-mail.

Zauważ, że FairEmail wybierze adres tylko po długim naciśnięciu adresu, co ułatwia usunięcie adresu.

Chipy nie nadają się do wyświetlania na liście a ponieważ nagłówek wiadomości na liście powinien wyglądać podobnie do nagłówka wiadomości w widoku wiadomości, nie można używać chipów do przeglądania wiadomości.

Cofnięto [ potwierdzenie ](https://github.com/M66B/FairEmail/commit/2c80c25b8aa75af2287f471b882ec87d5a5a5015).

<br />

<a name="faq116"></a>
**~~(116) Jak mogę domyślnie pokazywać obrazy w wiadomościach od zaufanych nadawców?~~**

~~Możesz domyślnie pokazywać obrazy w wiadomościach od zaufanych nadawców poprzez włączenie ustawienia wyświetlania *Automatycznie pokazuj obrazy dla znanych kontaktów*.~~

~~Kontakty na liście kontaktów Androida są uznawane za znane i zaufane,~~ ~~chyba że kontakt jest w grupie / ma etykietę '*Niezaufane*' (wielkość liter nie ma znaczenia).~~

<br />

<a name="faq38"></a>
<a name="faq117"></a>
**(117) Czy możesz mi pomóc przywrócić mój zakup?**

First of all, a purchase will be available on all devices logged into the same Google account, *if* the app is installed via the same Google account too. You can select the account in the Play store app.

Google manages all purchases, so as a developer I have little control over purchases. A zatem zasadniczo jedyną rzeczą, którą mogę zrobić, jest udzielenie porady w zakresie:

* Upewnij się, że masz aktywne, działające połączenie internetowe
* Upewnij się, że jesteś zalogowany na właściwym koncie Google i że nie ma błędów z Twoim kontem Google
* Make sure you installed FairEmail via the right Google account if you configured multiple Google accounts on your device
* Make sure the Play store app is up to date, please [see here](https://support.google.com/googleplay/answer/1050566?hl=en)
* Open the Play store app and wait at least a minute to give it time to synchronize with the Google servers
* Open FairEmail and navigate to the pro features screen to let FairEmail check the purchases; sometimes it help to tap the *buy* button

Możesz również spróbować wyczyścić pamięć podręczną aplikacji Sklep Play za pomocą ustawień aplikacji Android. Ponowne uruchomienie urządzenia może być konieczne, aby sklep Play rozpoznał zakupy poprawnie.

Zauważ, że:

* If you get *ITEM_ALREADY_OWNED*, the Play store app probably needs to be updated, please [see here](https://support.google.com/googleplay/answer/1050566?hl=en)
* Zakupy są przechowywane w chmurze Google i nie mogą zostać utracone
* Nie ma ograniczeń czasowych, więc nie mogą one wygasnąć
* Google nie ujawnia szczegółów (nazwa, e-mail, itp.) o nabywcach programistom
* An app like FairEmail cannot select which Google account to use
* Może to zająć chwilę zanim aplikacja Sklep Play zsynchronizuje zakup na innym urządzeniu
* Play Store purchases cannot be used without the Play Store, which is also not allowed by Play Store rules

Jeśli nie możesz rozwiązać problemu z zakupem, będziesz musiał skontaktować się z Google.

<br />

<a name="faq118"></a>
**(118) Co dokładnie oznacza „Usuń parametry śledzenia”?**

Zaznaczenie *Usuń parametry śledzenia* usunie wszystkie [parametry UTM](https://en.wikipedia.org/wiki/UTM_parameters) z linku.

<br />

<a name="faq119"></a>
**~~(119) Czy potrafisz dodać kolory do jednolitego widżetu skrzynki odbiorczej?~~**

~~Widżet jest zaprojektowany tak, aby wyglądał dobrze na większości ekranów domowych/launchera poprzez uczynienie go monochromatycznym i używanie półprzezroczystego tła.~~ ~~W ten sposób widżet będzie ładnie wmieszany, ale nadal będzie odpowiednio czytelny.~~

~~Dodanie kolorów spowoduje problemy z niektórymi tłami i spowoduje problemy z czytelnością i dlatego nie zostanie to dodane.~~

Ze względu na ograniczenia Androida nie jest możliwe dynamiczne ustawienie przezroczystości tła i jednoczesne zaokrąglanie rogów.

<br />

<a name="faq120"></a>
**(120) Dlaczego nowe powiadomienia nie zostały usunięte przy otwieraniu aplikacji?**

Powiadomienia o nowych wiadomościach zostaną usunięte po przesunięciu powiadomień lub oznaczeniu powiązanych wiadomości jako przeczytanych. Otwarcie aplikacji nie usunie powiadomień o nowych wiadomościach. To daje Ci możliwość pozostawiania powiadomień o nowych wiadomościach jako przypomnienie, że nadal są nieprzeczytane wiadomości.

Na Androidzie 7 Nougat i nowszym powiadomienia o nowych wiadomościach będą [pogrupowane](https://developer.android.com/training/notify-user/group). Naciśnięcie powiadomienia podsumowującego otworzy wspólną skrzynkę odbiorczą. Powiadomienie podsumowujące można rozwinąć, aby wyświetlić indywidualne powiadomienia o nowych wiadomościach. Stuknięcie w powiadomienie o nowej wiadomości otworzy rozmowę, której jest częścią. Zobacz [ten FAQ](#user-content-faq70) o tym, kiedy wiadomości w konwersacji będą automatycznie rozwijane i oznaczone e jako przeczytane.

<br />

<a name="faq121"></a>
**(121) Jak wiadomości są pogrupowane w rozmowę?**

Domyślnie FariEmail grupuje wiadomości w rozmowach. Ta opcja może być wyłączona w ustawieniach wyświetlania.

FairEmail grupuje wiadomości w oparciu o standard nagłówków *Message-ID*, *In-Reply-Do* i *Referencje*. FairEmail nie grupuje po innych kryteriach, takich jak temat, ponieważ może to skutkować grupowaniem niepowiązanych wiadomości i byłoby to kosztem zwiększonego zużycia baterii.

<br />

<a name="faq122"></a>
**~~(122) Dlaczego nazwa/adres e-mail odbiorcy są wyświetlane w kolorze ostrzegawczym?~~**

~~Nazwa odbiorcy i/lub adres e-mail w sekcji adresów będą wyświetlane w kolorze ostrzeżenia~~ ~~gdy nazwa domeny nadawcy i nazwa domeny *do* nie pasują.~~ ~~Często wskazuje to, że wiadomość została odebrana *przez* konto z innym adresem e-mail.~~

<br />

<a name="faq123"></a>
**(123) Co się stanie, gdy FairEmail nie będzie mógł połączyć się z serwerem e-mail?**

If FairEmail cannot connect to an email server to synchronize messages, for example if the internet connection is bad or a firewall or a VPN is blocking the connection, FairEmail will retry one time after waiting 8 seconds while keeping the device awake (=use battery power). If this fails, FairEmail will schedule an alarm to retry after 15, 30 and eventually every 60 minutes and let the device sleep (=no battery usage).

Pamiętaj, że [Android doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby) nie pozwala wybudzić urządzenia wcześniej niż po 15 minutach.

* Wymuś synchronizację * w menu z trzema kropkami we wspólnej skrzynce odbiorczej można użyć, aby umożliwić FairEmail próbę ponownego połączenia bez czekania.

Wysyłanie wiadomości zostanie ponowione tylko przy zmianie połączenia (ponowne połączenie z tą samą siecią lub połączenie z inną siecią), aby zapobiec stałemu blokowaniu połączenia przez serwer e-mail. Możesz pociągnąć w dół skrzynkę odbiorczą, aby ręcznie spróbować ponownie.

Pamiętaj, że wysyłanie nie zostanie ponowione w przypadku problemów z uwierzytelnianiem i gdy serwer odrzucił wiadomość. In this case you can pull down the outbox to try again.

<br />

<a name="faq124"></a>
**(124) Dlaczego otrzymuję komunikat 'Wiadomość za duża lub zbyt skomplikowana, aby ją wyświetlić'?**

Powiadomienie *Wiadomość jest zbyt duża lub zbyt skomplikowana, aby ją wyświetlić * będzie wyświetlana, jeśli jest w niej więcej niż 100,000 znaków lub w wiadomości jest więcej niż 500 linków. Ponowne formatowanie i wyświetlenie takich wiadomości zajmie zbyt długo. Możesz zamiast tego spróbować użyć oryginalnego widoku wiadomości, wspieranego przez przeglądarkę.

<br />

<a name="faq125"></a>
**(125) Jakie są obecne funkcje eksperymentalne?**

*Message classification (version 1.1438+)*

Please see [this FAQ](#user-content-faq163) for details.

Since this is an experimental feature, my advice is to start with just one folder.

<br />

*Send hard bounce (version 1.1477+)*

Send a [Delivery Status Notification](https://tools.ietf.org/html/rfc3464) (=hard bounce) via the reply/answer menu.

Hard bounces will mostly be processed automatically because they affect the reputation of the email provider. The bounce address (=*Return-Path* header) is mostly very specific, so the email server can determine the sending account.

For some background, see for [this Wikipedia article](https://en.wikipedia.org/wiki/Bounce_message).

<br />

*Translate button (version 1.1600+)*

Please see [this FAQ](#user-content-faq167) about how to configure DeepL.

<br />

<a name="faq126"></a>
**(126) Czy podgląd wiadomości można wysyłać do mojego urządzenia do noszenia?**

FairEmail pobiera wiadomość w dwóch krokach:

1. Pobranie nagłówków wiadomości
1. Pobranie tekstu wiadomości i załączników

Bezpośrednio po pierwszym kroku nastąpi powiadomienie o nowych wiadomościach. Jednak dopiero po drugim kroku, tekst wiadomości będzie dostępny. FairEmail aktualizuje powiadomienia z podglądem tekstu wiadomości, ale niestety powiadomienia nie mogą być aktualizowane.

Ponieważ nie ma gwarancji, że tekst wiadomości będzie zawsze pobierany bezpośrednio po nagłówku wiadomości, nie można zagwarantować, że powiadomienie o nowej wiadomości z tekstem podglądu będzie zawsze wysyłane do urządzenia do noszenia.

If you think this is good enough, you can enable the notification option *Only send notifications with a message preview to wearables* and if this does not work, you can try to enable the notification option *Show notifications with a preview text only*. Note that this applies to wearables not showing a preview text too, even when the Android Wear app says the notification has been sent (bridged).

Jeśli chcesz mieć pełny tekst wiadomości na urządzeniu do noszenia, możesz włączyć opcję powiadomienia *Podgląd całego tekstu*. Pamiętaj, że niektóre urządzenia do noszenia są znane z awarii przy włączonej tej opcji.

Jeśli obsługujesz urządzenie do noszenia Samsung za pomocą aplikacji Galaxy Wearable (Samsung Gear), może być konieczne włączenie powiadomień dla FairEmail, gdy ustawienie *Powiadomienia*, *Aplikacje zainstalowane w przyszłości* jest wyłączone w tej aplikacji.

<br />

<a name="faq127"></a>
**(127) Jak mogę naprawić 'Syntactically invalid HELO argument(s)'?**

Błąd *... Syntactically invalid HELO argument(s) ...* oznacza, że serwer SMTP odrzucił lokalny adres IP lub nazwę hosta. Prawdopodobnie możesz naprawić ten błąd, włączając lub wyłączając opcję zaawansowaną tożsamości * Użyj lokalnego adresu IP zamiast nazwy hosta *.

<br />

<a name="faq128"></a>
**(128) Jak mogę zresetować zadane pytania, na przykład aby pokazać obrazy?**

Możesz zresetować wszystkie pytania ustawione, aby nie były zadawane ponownie w ustawieniach różnych.

<br />

<a name="faq129"></a>
**(129) Czy wspierany jest ProtonMail, Tutanota?**

ProtonMail używa zastrzeżonego protokołu e-mail i [nie obsługuje bezpośrednio IMAP](https://protonmail.com/support/knowledge-base/imap-smtp-and-pop3-setup/), więc nie możesz użyć FairEmail do uzyskania dostępu do ProtonMail.

Tutanota używa zastrzeżonego protokołu e-mail i [nie obsługuje IMAP](https://tutanota.com/faq/#imap), więc nie możesz użyć FairEmail do uzyskania dostępu do Tutanota.

<br />

<a name="faq130"></a>
**(130) Co oznacza błąd wiadomości ... ?**

Eria wierszy z pomarańczowymi lub czerwonymi tekstami z informacjami technicznymi oznacza, że ​​w ustawieniach Różne włączono tryb debugowania.

Ostrzeżenie *Nie znaleziono serwera na ...* oznacza, że nie zarejestrowano żadnego serwera e-mail na wskazanej nazwie domeny. Odpowiedź na wiadomość może nie być możliwe i może spowodować błąd. Może to wskazywać na sfałszowany adres e-mail i/lub spam.

Błąd *... ParseException ... * oznacza, że ​​występuje problem z otrzymaną wiadomością, prawdopodobnie spowodowany błędem w oprogramowaniu wysyłającym. FairEmail w większości przypadków znajdzie rozwiązanie, więc ta wiadomość może być uważana za ostrzeżenie zamiast błędu.

Błąd *...SendFailedException...* oznacza, że wystąpił problem podczas wysyłania wiadomości. Błąd prawie zawsze będzie zawierał powód. Powodem jest to, że wiadomość była zbyt duża lub jeden lub więcej adresów odbiorców było nieprawidłowych.

Ostrzeżenie *Wiadomość jest zbyt duża, aby zmieścić się w dostępnej pamięci* oznacza, że wiadomość była większa niż 10 MB. Nawet jeśli urządzenie ma mnóstwo miejsca na dysku Android zapewnia ograniczoną ilość pamięci roboczej aplikacji, które z kolei ogranicza rozmiar wiadomości mogące być obsługiwane.

Zobacz [tutaj](#user-content-faq22) inne komunikaty o błędach w skrzynce wychodzącej.

<br />

<a name="faq131"></a>
**(131) Czy możesz zmienić kierunek przesuwania do poprzedniej / następnej wiadomości?**

Jeśli czytasz od lewej do prawej, przesunięcie w lewo pokaże następną wiadomość. Podobnie, jeśli czytasz od prawej do lewej, przesunięcie w prawo pokaże następną wiadomość.

To zachowanie wydaje mi się całkiem naturalne, również dlatego, że jest podobne do odwracania stron.

W każdym razie, istnieje ustawienie zachowań, aby odwrócić kierunek przesunięcia.

<br />

<a name="faq132"></a>
**(132) Dlaczego nowe powiadomienia wiadomości są ciche?**

Powiadomienia są domyślnie ciche na niektórych wersjach MIUI. Zobacz jak możesz to naprawić [tutaj](http://en.miui.com/thread-3930694-1-1.html).

W niektórych wersjach Androida występuje błąd powodując wyciszenie powiadomień przez [ setOnlyAlertOnce ](https://developer.android.com/reference/android/app/Notification.Builder#setOnlyAlertOnce(boolean)). Ponieważ FairEmail pokazuje powiadomienia o nowych wiadomościach zaraz po pobraniu nagłówków wiadomości i FairEmail musi zaktualizować powiadomienia o nowych wiadomościach po pobraniu tekstu wiadomości później, to nie może być naprawione ani obsłużone przez FairEmail.

Android może ograniczać dźwięk powiadomień, co może spowodować wyciszenie niektórych nowych wiadomości.

<br />

<a name="faq133"></a>
**(133) Dlaczego ActiveSync nie jest wspierany?**

Protokół Microsoft Exchange ActiveSync [jest opatentowany](https://en.wikipedia.org/wiki/Exchange_ActiveSync#Licensing) i dlatego nie może być wspierany. Z tego powodu nie znajdziesz wielu klientów, jeśli w ogóle, poczty elektronicznej obsługujących ActiveSync.

Zauważ, że opis FairEmail zaczyna się od uwagi, iż niestandardowe protokoły, takie jak Microsoft Exchange Web Services i Microsoft ActiveSync nie są obsługiwane.

<br />

<a name="faq134"></a>
**(134) Czy możesz dodać usuwanie lokalnych wiadomości?**

*POP3*

W ustawieniach konta (Ustawienia, dotknij Ręczna konfiguracja, dotknij Konta, dotknij konto) możesz włączyć *Pozostaw usunięte wiadomości na serwerze*.

*IMAP*

Ponieważ protokół IMAP ma na celu synchronizację dwukierunkową, usunięcie wiadomości z urządzenia spowodowałoby ponowne pobranie wiadomości podczas ponownej synchronizacji.

FairEmail obsługuje jednak ukrywanie wiadomości, poprzez menu trzech kropek na pasku akcji tuż nad tekstem wiadomości lub przez wielokrotne wybieranie wiadomości na liście wiadomości. Basically this is the same as "leave on server" of the POP3 protocol with the advantage that you can show the messages again when needed.

Note that it is possible to set the swipe left or right action to hide a message.

<br />

<a name="faq135"></a>
**(135) Dlaczego usunięte wiadomości i szkice wyświetlane są w rozmowach?**

Pojedyncze wiadomości są rzadko usuwane i najczęściej dzieje się to przypadkowo. Wyświetlanie usuniętych wiadomości w rozmowach ułatwia ich ponowne odnalezienie.

Możesz trwale usunąć wiadomość za pomocą menu trzech kropek *usuń*, co usunie wiadomość z konwersacji. Zauważ, że to jest nieodwracalne.

Podobnie, szkice są wyświetlane w rozmowach, aby znaleźć je ponownie w kontekście, w którym się znajdują. Łatwo jest odczytać otrzymane wiadomości przed dalszym pisaniem wersji roboczej później.

<br />

<a name="faq136"></a>
**(136) Jak mogę usunąć konto/tożsamość/folder?**

Usuwanie konta/tożsamości/folderu jest trochę ukryte, aby zapobiec wypadkom.

* Konto: Ustawienia > Ręczna konfiguracja > Konta > dotknij konto
* Tożsamość: Ustawienia > Ręczna konfiguracja > Tożsamości > dotknij tożsamości
* Folder: Przytrzymaj folder na liście folderów > Edytuj właściwości

W menu trzech kropek w prawym górnym rogu znajduje się pozycja do usunięcia konta/tożsamość/folderu.

<br />

<a name="faq137"></a>
**(137) Jak mogę zresetować 'Nie pytaj ponownie'?**

Możesz zresetować wszystkie pytania ustawione, aby nie były zadawane ponownie w ustawieniach różnych.

<br />

<a name="faq138"></a>
**(138) Can you add calendar/contact/tasks/notes management?**

Calendar, contact, task and note management can better be done by a separate, specialized app. Zauważ, że FairEmail jest specjalną aplikacją e-mailową, a nie zestawem biurowym.

Also, I prefer to do a few things very well, instead of many things only half. Moreover, from a security perspective, it is not a good idea to grant many permissions to a single app.

Zaleca się korzystanie z doskonałej, otwartej aplikacji [DAVx⁵](https://f-droid.org/packages/at.bitfire.davdroid/) do synchronizacji/zarządzania kalendarzem/kontaktami.

Większość dostawców obsługuje eksport Twoich kontaktów. [Zobacz tutaj](https://support.google.com/contacts/answer/1069522) jak można zaimportować kontakty, jeśli synchronizacja nie jest możliwa.

Zauważ, że FairEmail wspiera odpowiadanie na zaproszenia do kalendarza (funkcja pro) i dodawanie zaproszeń do osobistego kalendarza.

<br />

<a name="faq83"></a>
<a name="faq139"></a>
**(139) Jak naprawić błąd 'Użytkownik jest uwierzytelniony, ale nie połączony'?**

W rzeczywistości ten konkretny błąd Microsoft Exchange jest nieprawidłowym komunikatem o błędzie spowodowanym przez błąd w starszym oprogramowaniu serwera Exchange.

Błąd *Użytkownik jest uwierzytelniony, ale nie połączony* może wystąpić jeśli:

* Wiadomości push są włączone dla zbyt wielu folderów: zobacz [ten FAQ](#user-content-faq23), aby uzyskać więcej informacji i obrazy
* Hasło do konta zostało zmienione: jego zmiana również w FairEmail powinna rozwiązać problem
* Adres e-mail aliasu jest używany jako nazwa użytkownika zamiast głównego adresu e-mail
* Niepoprawny schemat logowania jest używany dla udostępnionej skrzynki pocztowej: prawidłowym schematem jest *username@domain\SharedMailboxAlias*

The shared mailbox alias will mostly be the email address of the shared account, like this:

```
you@example.com\shared@example.com
```

Note that it should be a backslash and not a forward slash.

When using a shared mailbox, you'll likely want to enable the option *Synchronize shared folder lists* in the receive settings.

<br />

<a name="faq140"></a>
**(140) Dlaczego tekst wiadomości zawiera dziwne znaki?**

Wyświetlanie dziwnych znaków jest prawie zawsze spowodowane brakiem określenia lub nieprawidłowego kodowania znaków przez oprogramowanie wysyłające. FairEmail założy [ISO 8859-1](https://en.wikipedia.org/wiki/ISO/IEC_8859-1) gdy nie podano żadnych znaków lub gdy [US-ASCII](https://en.wikipedia.org/wiki/ASCII) został określony. Poza tym, nie ma sposobu na automatyczne i wiarygodne określenie poprawnego kodowania znaków, więc FairEmail nie może tego naprawić. Właściwym działaniem jest zgłoszenie do nadawcy.

<br />

<a name="faq141"></a>
**(141) Jak mogę naprawić 'Folder szkiców jest wymagany do wysyłania wiadomości'?**

Aby przechowywać szkice wiadomości, wymagany jest folder szkiców. W większości przypadków FairEmail automatycznie wybierze foldery szkiców przy dodawaniu konta w oparciu o [atrybuty](https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml), które wysyła serwer e-mail. Niektóre serwery e-mail nie są jednak poprawnie skonfigurowane i nie wysyłają tych atrybutów. W tym przypadku FairEmail próbuje zidentyfikować folder szkiców według nazwy, ale to może się nie powieść, jeśli folder szkiców ma nietypową nazwę lub w ogóle nie jest obecny.

Możesz rozwiązać ten problem ręcznie wybierając folder szkiców w ustawieniach konta (Ustawienia, dotknij Ręcznej konfiguracji, dotknij Konta, dotknij konto u dołu). Jeśli w ogóle nie ma folderu szkiców, możesz utworzyć folder szkiców naciskając przycisk '+' na liście folderów konta (dotknij nazwy konta w menu nawigacji).

Niektórzy dostawcy, jak Gmail, umożliwiają włączanie/wyłączanie IMAP dla poszczególnych folderów. Więc jeśli folder nie jest widoczny, może być konieczne włączenie IMAP dla folderu.

Quick link for Gmail (will work on a desktop computer only): [https://mail.google.com/mail/u/0/#settings/labels](https://mail.google.com/mail/u/0/#settings/labels)

<br />

<a name="faq142"></a>
**(142) Jak przechowywać wysłane wiadomości w skrzynce odbiorczej?**

Ogólnie, nie jest dobrym pomysłem na przechowywanie wysłanych wiadomości w skrzynce odbiorczej, ponieważ jest to trudne do cofnięcia i może być niekompatybilne z innymi klientami poczty elektronicznej.

To mówiąc, FairEmail jest w stanie poprawnie obsługiwać wysłane wiadomości w skrzynce odbiorczej. FairEmail oznaczy wychodzące wiadomości na przykład ikoną wysłanej wiadomości.

Najlepszym rozwiązaniem byłoby pokazanie folderu Wysłane we wspólnej skrzynce odbiorczej, przez długie naciśnięcie folderu Wysłane na liście folderów i włączenie * Pokaż we wspólnej skrzynce odbiorczej *. W ten sposób wszystkie wiadomości mogą pozostać tam, gdzie należą, umożliwiając jednocześnie wyświetlanie wiadomości przychodzących i wychodzących w jednym miejscu.

Jeśli nie jest to rozwiązaniem, możesz [ utworzyć regułę ](#user-content-faq71), aby automatycznie przenosić wysłane wiadomości do skrzynki odbiorczej lub ustaw domyślny adres DW/UDW w zaawansowanych ustawieniach tożsamości, aby wysłać sobie kopię.

<br />

<a name="faq143"></a>
**~~(143) Czy możesz dodać folder kosza dla kont POP3?~~**

[POP3](https://en.wikipedia.org/wiki/Post_Office_Protocol) jest bardzo ograniczonym protokołem. Zasadniczo tylko wiadomości mogą zostać pobrane i usunięte z skrzynki odbiorczej. Nie można nawet oznaczyć wiadomości jako przeczytanej.

Ponieważ POP3 w ogóle nie pozwala na dostęp do folderu kosza, nie ma możliwości przywrócenia wiadomości z kosza.

Pamiętaj, że możesz ukrywać wiadomości i wyszukiwać ukryte wiadomości, podobne do lokalnego folderu kosza, bez sugerowania, że ​​usunięte wiadomości można przywrócić, podczas gdy w rzeczywistości nie jest to możliwe.

Wersja 1.1082 dodała lokalny folder śmieci. Pamiętaj, że usunięcie wiadomości spowoduje trwałe usunięcie jej z serwera i nie będzie można przywrócić usuniętych wiadomości na serwerze.

<br />

<a name="faq144"></a>
**(144) Jak mogę nagrać notatki głosowe?**

Aby nagrywać notatki głosowe, naciśnij tę ikonę w dolnym pasku akcji kompozytora wiadomości:

![Obraz zewnętrzny](https://github.com/M66B/FairEmail/blob/master/images/baseline_record_voice_over_black_48dp.png)

Wymaga to zainstalowania kompatybilnej aplikacji do nagrywania dźwięku. W szczególności [ ten wspólny zamiar ](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION) musi być wspierany.

Na przykład [ta aplikacja do nagrywania dźwięku](https://f-droid.org/app/com.github.axet.audiorecorder) jest kompatybilna.

Notatki głosowe zostaną automatycznie dołączone.

<br />

<a name="faq145"></a>
**(145) Jak mogę ustawić dźwięk powiadomienia dla konta, folderu lub nadawcy?**

Konto:

* Włącz *Oddzielne powiadomienia* w zaawansowanych ustawieniach konta (Ustawienia, dotknij Ręczna konfiguracja, dotknij Konta, dotknij konto, Zaawansowane)
* Long press the account in the account list (Settings, tap Manual setup, tap Accounts) and select *Edit notification channel* to change the notification sound

Folder:

* Przytrzymaj folder na liście folderów i wybierz *Utwórz kanał powiadomień*
* Przytrzymaj folder na liście folderów i wybierz *Edytuj kanał powiadomień* aby zmienić dźwięk powiadomień

Nadawca:

* Otwórz wiadomość od nadawcy i rozwiń ją
* Rozwiń sekcję adresów naciskając strzałkę w dół
* Dotknij ikony dzwonka, aby utworzyć lub edytować kanał powiadomień i zmienić dźwięk powiadomień

Kolejność występowania jest następująca: dźwięk nadawcy, dźwięk folderu, dźwięk konta i dźwięk domyślny.

Ustawienie dźwięku powiadomień dla konta, folderu lub nadawcy wymaga Androida 8 Oreo lub nowszego i jest funkcją pro.

<br />

<a name="faq146"></a>
**(146) Jak mogę naprawić niepoprawne czasy wiadomości?**

Ponieważ data i godzina wysłania są opcjonalne i mogą być manipulowane przez nadawcę, FairEmail domyślnie używa otrzymanej daty/godziny serwera.

Czasami data/godzina odebrania serwera jest nieprawidłowa, głównie dlatego, że wiadomości zostały nieprawidłowo zaimportowane z innego serwera, a czasami z powodu błędu w serwerze poczty elektronicznej.

W takich rzadkich przypadkach możliwe jest, aby FairEmail użył daty/czasu z nagłówka *Data* (wysłany czas) lub nagłówka *Odebrano* jako obejście problemu. Można to zmienić w zaawansowanych ustawieniach konta: Ustawienia, dotknij Ręczna konfiguracja, dotknij Konta, dotknij konto, dotknij Zaawansowane.

To nie zmieni czasu już zsynchronizowanych wiadomości. Aby rozwiązać ten problem, naciśnij długo folder(y) na liście folderów i wybierz *Usuń lokalne wiadomości* i *Synchronizuj teraz*.

<br />

<a name="faq147"></a>
**(147) Co powinienem wiedzieć o wersjach firm trzecich?**

Prawdopodobnie przyszedłeś tutaj, ponieważ używasz innej kompilacji FairEmail.

There is **only support** on the latest Play store version, the latest GitHub release and the F-Droid build, but **only if** the version number of the F-Droid build is the same as the version number of the latest GitHub release.

F-Droid tworzy nieregularnie, co może być problematyczne, gdy jest ważna aktualizacja. Z tego względu zaleca się przełączenie na wersję GitHub.

Wersja F-Droid jest zbudowana z tego samego kodu źródłowego, ale inaczej podpisana. This means that all features are available in the F-Droid version too, except for using the Gmail quick setup wizard because Google approved (and allows) one app signature only. For all other email providers, OAuth access is only available in Play Store versions and Github releases, as the email providers only permit the use of OAuth for official builds.

Zauważ, że musisz najpierw odinstalować wersję F-Droid zanim będziesz mógł zainstalować wersję GitHub, ponieważ Android odmawia instalacji tej samej aplikacji z innym podpisem ze względów bezpieczeństwa.

Pamiętaj, że wersja GitHub automatycznie sprawdza dostępność aktualizacji. Gdy jest to pożądane, można to wyłączyć w ustawieniach różnych.

[Zobacz tutaj](https://github.com/M66B/FairEmail/blob/master/README.md#user-content-downloads), aby zobaczyć wszystkie opcje pobierania.

Jeśli masz problem z wersją F-Droid, sprawdź najpierw czy jest nowsza wersja GitHub.

<br />

<a name="faq148"></a>
**(148) Jak mogę używać konta Apple iCloud?**

There is a built-in profile for Apple iCloud, so you should be able to use the quick setup wizard (other provider). If needed you can find the right settings [here](https://support.apple.com/en-us/HT202304) to manually set up an account.

Podczas korzystania z uwierzytelniania dwuskładnikowego może być konieczne użycie hasła [dla danej aplikacji](https://support.apple.com/en-us/HT204397).

<br />

<a name="faq149"></a>
**(149) Jak działa widżet licznika nieprzeczytanych wiadomości?**

Widżet licznika nieprzeczytanych wiadomości pokazuje liczbę nieprzeczytanych wiadomości dla wszystkich kont lub dla wybranego konta, ale tylko dla folderów, dla których włączono powiadomienia o nowych wiadomościach.

Dotknięcie powiadomienia spowoduje synchronizację wszystkich folderów, dla których synchronizacja jest włączona i zostanie otwarty:

* ekran początkowy jeśli wybrane były wszystkie konta
* lista folderów, gdy wybrano konkretne konto i gdy powiadomienia o nowych wiadomościach są włączone dla wielu folderów
* lista wiadomości, gdy wybrano konkretne konto i gdy powiadomienia o nowych wiadomościach są włączone dla jednego folderu

<br />

<a name="faq150"></a>
**(150) Czy możesz dodać anulowanie zaproszeń do kalendarza?**

Anulowanie zaproszeń do kalendarza (usuwanie wydarzeń z kalendarza) wymaga uprawnień do zapisu, co spowoduje przyznanie uprawnień do odczytu i zapisu *wszystkich* wydarzeń kalendarza *wszystkich* kalendarzy.

Biorąc pod uwagę cel FairEmail, prywatność i bezpieczeństwo oraz fakt, że łatwo jest ręcznie usunąć wydarzenie kalendarza, nie jest dobrym pomysłem, aby poprosić o to uprawnienie tylko z tego powodu.

Wstawianie nowych wydarzeń w kalendarzu można wykonywać bez uprawnień ze specjalnymi [ intencjami ](https://developer.android.com/guide/topics/providers/calendar-provider.html#intents). Niestety nie ma zamiaru usunąć istniejących wydarzeń kalendarza.

<br />

<a name="faq151"></a>
**(151) Czy możesz dodać kopię zapasową/przywracanie wiadomości?**

Klient poczty e-mail ma na celu odczytywanie i zapisywanie wiadomości, a nie tworzenie kopii zapasowej i przywracanie wiadomości. Zauważ, że uszkodzenie lub utrata urządzenia oznacza utratę wiadomości!

Zamiast tego dostawca/serwer poczty e-mail jest odpowiedzialny za kopie zapasowe.

Jeśli chcesz samodzielnie utworzyć kopię zapasową, możesz użyć narzędzia takiego jak [imapsync](https://imapsync.lamiral.info/).

Since version 1.1556 it is possible to export all messages of a POP3 folder in mbox format according to [RFC4155](https://www.ietf.org/rfc/rfc4155.txt), which might be useful to save sent messages if the email server doesn't.

Jeśli chcesz zaimportować plik mbox do istniejącego konta e-mail, możesz użyć Thunderbirda na komputerze stacjonarnym i dodatku [ImportExportTools](https://addons.thunderbird.net/nl/thunderbird/addon/importexporttools/).

<br />

<a name="faq152"></a>
**(152) W jaki sposób mogę wstawić grupę kontaktową?**

Możesz wstawić adresy e-mail wszystkich kontaktów w grupie kontaktów za pomocą menu trzech kropek w kompozytorze wiadomości.

Możesz zdefiniować grupy kontaktów z aplikacją na Androida, zobacz [tutaj](https://support.google.com/contacts/answer/30970) aby uzyskać instrukcje.

<br />

<a name="faq153"></a>
**(153) Dlaczego trwale usunięcie wiadomości Gmail nie działa?**

Być może będziesz musiał zmienić [ustawienia IMAP Gmail](https://mail.google.com/mail/u/0/#settings/fwdandpop) w przeglądarce stacjonarnej, aby to działało:

* Gdy zaznaczam wiadomość w IMAP jako usuniętą: Auto-Expunge wyłączone - poczekaj na aktualizację serwera.
* Gdy wiadomość zostanie oznaczona jako usunięta i usunięta z ostatniego widocznego folderu IMAP: natychmiast usuń wiadomość na zawsze

Zauważ, że zarchiwizowane wiadomości mogą zostać usunięte tylko poprzez przeniesienie ich do folderu kosza.

Trochę tła: Gmail wydaje się mieć dodatkowy widok wiadomości dla IMAP, który może różnić się od głównego widoku wiadomości.

Another oddity is that a star (favorite message) set via the web interface cannot be removed with the IMAP command

```
STORE <message number> -FLAGS (\Flagged)
```

On the other hand, a star set via IMAP is being shown in the web interface and can be removed via IMAP.

<br />

<a name="faq154"></a>
**~(154) Czy możesz dodać favikony jako zdjęcia kontaktowe?~**

~~ Poza tym [ favicon ](https://en.wikipedia.org/wiki/Favicon) może być współdzielony przez wiele adresów e-mail o tej samej nazwie domeny ~~ ~~ i dlatego nie jest bezpośrednio związany z adresem e-mail, favicony można wykorzystać do śledzenia. ~~

<br />

<a name="faq155"></a>
**(155) Co to jest plik winmail.dat?**

Plik *winmail.dat* jest wysyłany przez nieprawidłowo skonfigurowanego klienta Outlook. Jest to specyficzny format pliku Microsoft ([TNEF](https://en.wikipedia.org/wiki/Transport_Neutral_Encapsulation_Format)) zawierający wiadomość i ewentualnie załączniki.

Więcej informacji na temat tego pliku można znaleźć [tutaj](https://support.mozilla.org/en-US/kb/what-winmaildat-attachment).

Możesz go obejrzeć w aplikacji na Androida, np. [Letter Opener](https://play.google.com/store/apps/details?id=app.letteropener).

<br />

<a name="faq156"></a>
**(156) How can I set up an Office 365 account?**

An Office 365 account can be set up via the quick setup wizard and selecting *Office 365 (OAuth)*.

Jeśli kreator kończy się z informacją *AUTHENTICATE failed*, IMAP i/lub SMTP mogą być wyłączone dla konta. W takim przypadku powinieneś poprosić administratora o włączenie IMAP i SMTP. Procedura jest udokumentowana [tutaj](https://docs.microsoft.com/en-in/exchange/troubleshoot/configure-mailboxes/pop3-imap-owa-activesync-office-365).

If you've enabled *security defaults* in your organization, you might need to enable the SMTP AUTH protocol. Please [see here](https://docs.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/authenticated-client-smtp-submission) about how to.

<br />

<a name="faq157"></a>
**(157) Jak mogę ustawić konto Free.fr?**

Veuillez [voir ici](https://free.fr/assistance/597.html) pour les instructions.

**SMTP est désactivé par défaut**, veuillez [voir ici](https://free.fr/assistance/2406.html) comment il peut être activé.

Veuillez [voir ici](http://jc.etiemble.free.fr/abc/index.php/trucs-astuces/configurer-smtp-free-fr) pour un guide détaillé.

<br />

<a name="faq103"></a>
<a name="faq158"></a>
**(158) Którą aplikację kamerę / rejestrator audio polecasz?**

Aby robić zdjęcia i nagrywać dźwięk, potrzebny jest aparat i aplikacja do rejestrowania dźwięku. Następujące aplikacje to kamery i rejestratory audio otwarto-źródłowe:

* [Open Camera](https://play.google.com/store/apps/details?id=net.sourceforge.opencamera) ([F-Droid](https://f-droid.org/en/packages/net.sourceforge.opencamera/))
* [Audio Recorder version 3.3.24+](https://play.google.com/store/apps/details?id=com.github.axet.audiorecorder) ([F-Droid](https://f-droid.org/packages/com.github.axet.audiorecorder/))

Aby nagrywać notatki głosowe itp., rejestrator audio musi obsługiwać [MediaStore.Audio.Media.RECORD_SOUND_ACTION](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media#RECORD_SOUND_ACTION). Wygląda na to, że większość aplikacji do nagrywania dźwięku nie obsługuje tej standardowej akcji Android.

<br />

<a name="faq159"></a>
**(159) Czym są listy ochrony przed śledzeniem Disconnect?**

Zobacz [tutaj](https://disconnect.me/trackerprotection), aby uzyskać więcej informacji o listach ochrony przed śledzeniem.

Po pobraniu list w ustawieniach prywatności, listy mogą być opcjonalnie używane:

* aby ostrzec przed linkami śledzącymi podczas otwierania linków
* aby rozpoznać obrazy śledzące w wiadomościach

Obrazy śledzące zostaną wyłączone tylko wtedy, gdy włączona jest odpowiednia główna opcja 'wyłącz'.

Obrazy śledzące nie zostaną rozpoznane, gdy domena jest sklasyfikowana jako '*Content*', zobacz [tutaj](https://disconnect.me/trackerprotection#trackers-we-dont-block), aby uzyskać więcej informacji.

Ta komenda może zostać wysłana do FairEmail z aplikacji automatyzującej aby zaktualizować listy ochrony:

```
(adb shell) am start-foreground-service -a eu.faircode.email.DISCONNECT.ME
```

Aktualizacja raz w tygodniu prawdopodobnie wystarczy, zobacz [tutaj](https://github.com/disconnectme/disconnect-tracking-protection/commits/master), aby przeczytać zmiany w ostatnich listach.

<br />

<a name="faq160"></a>
**(160) Can you add permanent deletion of messages without confirmation?**

Permanent deletion means that messages will *irreversibly* be lost, and to prevent this from happening accidentally, this always needs to be confirmed. Even with a confirmation, some very angry people who lost some of their messages through their own fault contacted me, which was a rather unpleasant experience :-(

Since version 1.1601 it is possible to disable confirmation of permanent deletion of individual messages.

Note that the POP3 protocol can download messages from the inbox only. So, deleted messages cannot be uploaded to the inbox again. This means that messages can only be permanently deleted when using a POP3 account.

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

The option *Use local spam filter* in the report spam dialog will turn on message classification in the miscellaneous settings and auto classification for the spam folder. Please understand that this is not a replacement for the spam filter of the email server and can result in [false positives and false negatives](https://en.wikipedia.org/wiki/False_positives_and_false_negatives). Zobacz również [ten FAQ](#user-content-faq92).

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
**(165) Is Android Auto supported?**

Yes, Android Auto is supported, but only with the GitHub version, please [see here](https://forum.xda-developers.com/t/app-5-0-fairemail-fully-featured-open-source-privacy-oriented-email-app.3824168/post-83801249) about why.

For notification (messaging) support you'll need to enable the following notification options:

* *Użyj formatu powiadomień w stylu wiadomości systemu Android*
* Notification actions: *Direct reply* and (mark as) *Read*

You can enable other notification actions too, if you like, but they are not supported by Android Auto.

The developers guide is [here](https://developer.android.com/training/cars/messaging).

<br />

<a name="faq166"></a>
**(166) Can I snooze a message across multiple devices?**

First of all, there is no standard for snoozing messages, so all snooze implementations are custom solutions.

Some email providers, like Gmail, move snoozed messages to a special folder. Unfortunately, third party apps have no access to this special folder.

Moving a message to another folder and back might fail and might not be possible if there is no internet connection. This is problematic because a message can be snoozed only after moving the message.

To prevent these issues, snoozing is done locally on the device by hiding the message while it is snoozing. Unfortunately, it is not possible to hide messages on the email server too.

<br />

<a name="faq167"></a>
**(167) How can I use DeepL?**

1. Enable [experimental features](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq125) in the miscellaneous settings
1. [Subscribe to](https://www.deepl.com/pro#developer) the DeepL API Free or Pro plan (credit card required)
1. [Copy](https://www.deepl.com/pro-account/plan) the authentication key
1. In the message composer tap on the faint translate button (文A), select *Configure* and paste the key

You might want to read the [privacy policy](https://www.deepl.com/privacy/) of DeepL.

This feature requires an internet connection and is not available in the Play store version.

<br />

<h2><a name="get-support"></a>Wsparcie</h2>

FairEmail is supported on Android smartphones and tablets and ChromeOS only.

Wspierana jest tylko najnowsza wersja ze Sklepu Play i najnowsze wydanie GitHub. Kompilacja F-Droid jest obsługiwana tylko wtedy, gdy numer wersji jest taki sam jak numer wersji najnowszej wersji GitHub. Oznacza to również, że obniżanie wersji nie jest wspierane.

Nie ma wsparcia dla rzeczy, które nie są bezpośrednio związane z FairEmail.

There is no support on building and developing things by yourself.

Żądane funkcje powinny:

* być przydatne dla większości ludzi
* nie komplikować użycia FairEmail
* pasować do filozofii FairEmail (zorientowanej na prywatność i bezpieczeństwo)
* być zgodne ze wspólnymi standardami (IMAP, SMTP, itp.)

Funkcje, które nie spełniają tych wymogów, zostaną prawdopodobnie odrzucone. This is also to keep maintenance and support in the long term feasible.

If you have a question, want to request a feature or report a bug, **please use [this form](https://contact.faircode.eu/?product=fairemailsupport)**.

Zgłoszenia na GitHub są wyłączone z powodu częstych nieprawidłowych zastosowań.

<br />

Prawa autorskie &copy; 2018-2021 Marcel Bokhorst.
