# Wsparcie FairEmail

Jeśli masz pytanie, sprawdź najpierw najczęściej zadawane pytania. Na dole możesz dowiedzieć się, jak zadawać inne pytania, żądać funkcji i zgłaszać błędy.

## Spis treści

* [Autoryzacja kont](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-authorizing-accounts)
* [W jaki sposób...](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-howto)
* [Znane problemy](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-known-problems)
* [Planowane funkcje](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-planned-features)
* [Często żądane funkcje](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-frequently-requested-features)
* [Najczęściej zadawane pytania (FAQ)](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-frequently-asked-questions)
* [Pomoc](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-support)

## Autoryzacja kont

W większości przypadków szybka konfiguracja będzie w stanie automatycznie zidentyfikować prawidłową konfigurację.

Jeśli szybka konfiguracja się nie powiedzie, musisz ręcznie skonfigurować konto (aby odbierać e-mail) i tożsamość (aby wysyłać e-mail). W tym celu potrzebujesz adresów serwera i numerów portów IMAP i SMTP, niezależnie od tego, czy należy użyć SSL/TLS, czy STARTTLS oraz swoją nazwę użytkownika (głównie, ale nie zawsze adres e-mail) i hasło.

Wyszukiwanie * IMAP * i nazwa dostawcy jest w większości wystarczające, aby znaleźć odpowiednią dokumentację.

W niektórych przypadkach musisz włączyć zewnętrzny dostęp do swojego konta i/lub użyć specjalnego hasła (aplikacji), na przykład, gdy włączone jest uwierzytelnianie dwuskładnikowe.

Do autoryzacji:

* Gmail / G suite, zob. [pytanie 6](#user-content-faq6)
* Outlook / Live / Hotmail, zob. [pytanie 14](#user-content-faq14)
* Office365, zob. [pytanie 14](#user-content-faq156)
* Microsoft Exchange, zob. [pytanie 8](#user-content-faq8)
* Yahoo, AOL i Sky, zobacz [pytanie 88](#user-content-faq88)
* Apple iCloud, zob. [pytanie 148](#user-content-faq148)
* Free.fr, zob. [pytanie 157](#user-content-faq157)

Zobacz [tutaj](#user-content-faq22) opisy typowych komunikatów o błędach i rozwiązań.

Powiązane pytania:

* [Czy OAuth jest wspierany?](#user-content-faq111)
* [Dlaczego ActiveSync nie jest obsługiwany? ](#user-content-faq133)

<a name="howto">

## W jaki sposób...?

* Zmienić nazwę konta: Konfiguracja, krok 1, Zarządzaj, dotknij konta
* Zmienić akcję gestu przesunięcia w lewo/w prawo: Ustawienia, Zachowanie, Ustaw akcję przesuwania
* Zmienić hasło: Ustawienia, krok 1, Zarządzaj, dotknij konta, zmień hasło
* Ustawić podpis: Konfiguracja, krok 2, Zarządzaj, dotknij tożsamość, edytuj podpis.
* Dodać adresy DW i UDW: dotknij ikony osób na końcu tematu
* Przejść do następnej/poprzedniej wiadomości dotyczącej archiwizowania/usuwania: w ustawieniach zachowania wyłącz * Automatycznie zamykaj konwersacje * i wybierz * Przejdź do następnej/poprzedniej rozmowy * dla * Po zamknięciu rozmowy *
* Dodać folder do wspólnej skrzynki odbiorczej: naciśnij długo folder na liście folderów i zaznacz * Pokaż we wspólnej skrzynce *
* Dodać folder do menu nawigacji: naciśnij długo folder na liście folderów i zaznacz * Pokaż w menu nawigacji *
* Załadować więcej wiadomości: przytrzymaj folder na liście folderów, wybierz *Synchronizuj więcej wiadomości*
* Usunąć wiadomość, pomijając kosz: w menu z trzema kropkami tuż nad tekstem wiadomości * Usuń * lub alternatywnie usuń zaznaczenie folderu Kosz w ustawieniach konta
* Usunąć konto/tożsamość: Ustawienia, krok 1/2, Zarządzaj, dotknij konta/tożsamość, menu trzech kropek, Usuń
* Usunąć folder: długo naciśnij folder na liście folderów, Edytuj właściwości, menu trzech kropek, Usuń
* Cofnąć wysyłanie: Wysłanie, dotknij wiadomości, dotknij przycisku cofnij
* Przechować wysłane wiadomości w skrzynce odbiorczej: [zobacz to FAQ](#user-content-faq142)
* Zmienić foldery systemowe: Ustawienia, krok 1, Zarządzaj, dotknij konta, lista na dole
* Eksport/import ustawień: Ustawienia, menu nawigacji/hamburger (trzy paski u góry)

## Znane problemy

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
* Podgląd tekstu wiadomości nie (zawsze) pojawia się na zegarku Samsung, ponieważ [setLocalOnly](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder.html#setLocalOnly(boolean)) wydaje się być ignorowany. Wiadomo, że teksty podglądu wiadomości są wyświetlane poprawnie na Pebble 2, Fitbit Charge 3 i Mi band 3. Zobacz również [ten FAQ](#user-content-faq126).

## Planowane funkcje

* ~~Synchronizuj na żądanie (ręcznie)~~
* ~~Szyfrowanie półautomatyczne~~
* ~~Kopiowanie wiadomości~~
* ~~Kolorowe gwiazdki~~
* ~~Ustawienia powiadomień dla folderu~~
* ~~ Wybierz lokalne obrazy do podpisów ~~ (nie zostanie dodane, ponieważ wymaga zarządzania plikami obrazów oraz ponieważ obrazy i tak nie są wyświetlane domyślnie w większości klientów poczty e-mail)
* ~~Pokaż wiadomości dopasowane przez regułę~~
* ~~[ ManageSieve ](https://tools.ietf.org/html/rfc5804) ~~ (nie ma utrzymywanych bibliotek Java z odpowiednią licencją i bez zależności, a ponadto FairEmail ma własne reguły filtrowania)
* ~~Szukaj wiadomości z/bez załączników~~ (nie można tego dodać, ponieważ IMAP nie obsługuje wyszukiwania załączników)
* ~~ Wyszukaj folder ~~ (problematyczne jest filtrowanie hierarchicznej listy folderów)
* Podpowiedzi wyszukiwania
* ~~[Autocrypt Setup Message](https://autocrypt.org/autocrypt-spec-1.0.0.pdf) (sekcja 4. )~~ (IMO nie jest dobrym pomysłem, aby klient poczty elektronicznej obsługiwał wrażliwe klucze szyfrowania do wyjątkowego użytku, podczas gdy OpenKeychain również może eksportować klucze)
* ~~ Ogólne ujednolicone foldery ~~
* ~~Nowe harmonogramy powiadomień dla każdego konta~~ (zaimplementowane przez dodanie warunku czasowego do reguł, więc wiadomości mogą być odłożone w wybranych okresach)
* ~~Kopiuj konta i tożsamość~~
* ~~ Pinch zoom ~~ (nie jest to możliwe w sposób niezawodny na przewijanej liście; zamiast tego można powiększyć pełny widok wiadomości)
* ~~Bardziej kompaktowy widok folderu~~
* ~~ Utwórz listy i tabele ~~ (wymaga to edytora tekstu sformatowanego, patrz [ to FAQ ](#user-content-faq99))
* ~~Rozmiar tekstu powiększenia ~~
* ~~Wyświetlanie GIF~~
* ~~Motywy~~ (dodano szary jasny i ciemny motyw, ponieważ wydaje się, że tego chce większość ludzi)
* ~~Dowolny warunek czasowy~~ (żaden dzień nie pasuje do warunku od/do daty/czasu)
* ~~Wyślij jako załącznik~~
* ~~Widget dla wybranego konta~~
* ~~Przypomnij o załączeniu plików~~
* ~~Wybierz domeny do pokazania zdjęć~~ (będzie to zbyt skomplikowane, aby używać)
* ~~ Wspólny widok wiadomości oznaczonych gwiazdką ~~ (jest to już specjalne wyszukiwanie)
* ~~Move notification action~~
* ~~Wsparcie S/MIME~~
* ~~Szukaj ustawień~~

Wszystko na tej liście jest w losowej kolejności i * może * zostać dodane w najbliższej przyszłości.

## Często żądane funkcje

Projekt opiera się na wielu dyskusjach i jeśli chcesz, możesz o nim dyskutować [na tym forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168). Projekt ma być minimalistyczny (bez zbędnych menu, przycisków itp.) I nie rozpraszać uwagi (brak fantazyjnych kolorów, animacji itp.). Wszystkie wyświetlane rzeczy powinny być użyteczne w ten czy inny sposób i powinny być umieszczone w sposób umożliwiający łatwe użytkowanie. Czcionki, rozmiary, kolory itp. Powinny być w miarę możliwości material design.

## Najczęściej zadawane pytania (FAQ)

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
* [(35) Dlaczego powinienem być ostrożny z przeglądaniem obrazów, załączników i oryginalnej wiadomości?](#user-content-faq35)
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
* [~~(57) Czy mogę użyć HTML w podpisach?~~](#user-content-faq57)
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
* [(103) Jak mogę nagrywać audio?](#user-content-faq103)
* [(104) Co muszę wiedzieć o zgłaszaniu błędów?](#user-content-faq104)
* [(105) Jak działa opcja Roaming jak w domu?](#user-content-faq105)
* [(106) Które launchery mogą pokazać licznik z liczbą nieprzeczytanych wiadomości?](#user-content-faq106)
* [(107) Jak stosować kolorowe gwiazdki?](#user-content-faq107)
* [(108) Czy można trwale usunąć wiadomości z dowolnego folderu?](#user-content-faq108)
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
* [(138) Czy możesz dodać kalendarz / zarządzanie kontaktami / synchronizację?](#user-content-faq138)
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
* [(156) Jak mogę utworzyć konto Office365?](#user-content-faq156)
* [(157) Jak mogę utworzyć konto Free.fr?](#user-content-faq157)
* [(158) Którą kamerę / rejestrator audio rekomendujesz?](#user-content-faq158)
* [(159) Czym są listy ochrony przed śledzeniem Disconnect?](#user-content-faq159)

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
* Opcjonalne: *przeczytaj swoje kontakty* (READ_CONTACTS): aby automatycznie uzupełniać adresy i pokazać zdjęcia
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

* Android 8 Oreo i później: naciśnij przycisk *Kanał usługi* i wyłącz kanał powiadomień za pomocą ustawień Androida
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
* *send*: wyślij wiadomość
* *exists*: sprawdź, czy wiadomość istnieje
* *rule*: wykonaj regułę na treści

Operacje są przetwarzane tylko wtedy, gdy istnieje połączenie z serwerem e-mail lub podczas ręcznej synchronizacji. Zobacz również to [FAQ](#user-content-faq16).

<br />

<a name="faq4"></a>
**(4) Jak mogę użyć nieprawidłowego certyfikatu bezpieczeństwa / pustego hasła / zwykłego połączenia tekstowego?**

*... Niezaufane... nie w certyfikacie ...*
<br />
*... Nieprawidłowy certyfikat bezpieczeństwa (nie można zweryfikować tożsamości serwera) ...*

Powinieneś spróbować to naprawić, kontaktując się z dostawcą lub uzyskując ważny certyfikat bezpieczeństwa ponieważ nieprawidłowe certyfikaty bezpieczeństwa są niepewne i umożliwiają [ ataki typu man-in-the-middle ](https://en.wikipedia.org/wiki/Man-in-the-middle_attack). Jeśli pieniądze są przeszkodą, możesz otrzymać darmowe certyfikaty bezpieczeństwa od [Let’s Encrypt](https://letsencrypt.org).

Alternatywnie możesz zaakceptować odcisk palca wyświetlony poniżej komunikatu o błędzie, jeśli skonfigurujesz konto i/lub tożsamość w kroku 1 i 2 konfiguracji (nie jest to możliwe w przypadku korzystania z kreatora szybkiej konfiguracji). Pamiętaj, że należy upewnić się, że połączenie z internetem jest bezpieczne.

Pamiętaj, że starsze wersje Androida mogą nie rozpoznawać nowszych urzędów certyfikacji, takich jak Let's Encrypt, powodując, że połączenia są uważane za niebezpieczne, patrz także [ tutaj ](https://developer.android.com/training/articles/security-ssl).

*Nie znaleziono kotwicy zaufania dla ścieżki certyfikacji*

*... java.security.cert.CertPathValidatorException: Trust anchor for certification path not found ...* oznacza, że ​​domyślny menedżer zaufania Androida nie mógł zweryfikować łańcucha certyfikatów serwera.

Powinieneś naprawić konfigurację serwera lub zaakceptować odcisk palca pokazany poniżej komunikatu o błędzie.

Pamiętaj, że ten problem może być spowodowany tym, że serwer nie wysyła również wszystkich certyfikatów pośrednich.

*Puste hasło*

Twoja nazwa użytkownika jest łatwa do odgadnięcia, więc jest to niepewne.

*Zwykłe połączenie tekstowe*

Twoja nazwa użytkownika i hasło oraz wszystkie wiadomości będą wysyłane i odbierane w postaci niezaszyfrowanej, co jest ** bardzo niepewne ** ponieważ [ atak typu man-in-the-middle ](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) jest bardzo prosty na nieszyfrowanym połączeniu.

Jeśli nadal chcesz używać nieprawidłowego certyfikatu bezpieczeństwa, pustego hasła lub połączenia zwykłego tekstu musisz włączyć niezabezpieczone połączenia w ustawieniach konta i/lub tożsamości. STARTTLS powinien być wybrany dla połączeń tekstowych. Jeśli włączysz niezabezpieczone połączenia, powinieneś łączyć się tylko za pośrednictwem prywatnych, zaufanych sieci i nigdy za pośrednictwem sieci publicznych, takich jakie są oferowane w hotelach, portach lotniczych itp.

<br />

<a name="faq5"></a>
**(5) Jak mogę dostosować widok wiadomości?**

W menu trzech kropek możesz włączyć, wyłączyć lub wybrać:

* *rozmiar tekstu*: dla trzech różnych rozmiarów czcionki
* *widok kompaktowy*: dla bardziej zwartych elementów wiadomości i mniejszej czcionki tekstowej

W ustawieniach wyświetlania możesz włączyć lub wyłączyć:

* *Wspólna skrzynka*: aby wyłączyć wspólną skrzynkę odbiorczą i wyświetlić listę folderów wybranych dla wspólnej skrzynki odbiorczej
* *Grupuj według daty*: pokaż nagłówek daty powyżej wiadomości z tą samą datą
* *Konwersacje w wątkach*: aby wyłączyć wątek konwersacji i wyświetlić indywidualne wiadomości
* *Pokaż zdjęcia kontaktu*: aby ukryć zdjęcia kontaktu
* *Pokaż ikony kontaktów*: aby wyświetlić wygenerowane awatary kontaktu
* *Pokaż nazwy i adresy e-mail*: aby pokazać nazwy lub pokazać nazwy i adresy e-mail
* *Temat wyświetlaj kursywą*: aby wyświetlić temat wiadomości jako zwykły tekst
* *Pokaż gwiazdki*: aby ukryć gwiazdki (ulubione)
* *Pokaż podgląd wiadomości*: aby wyświetlić dwie linie tekstu wiadomości
* *Domyślnie pokazuj szczegóły adresu*: aby rozszerzyć sekcję adresów domyślnie
* *Użyj czcionki o stałej szerokości dla tekstu wiadomości*: aby użyć czcionki o stałej szerokości dla tekstów wiadomości
* *Automatycznie pokaż oryginalną wiadomość dla znanych kontaktów*: aby automatycznie wyświetlić oryginalne wiadomości dla kontaktów na twoim urządzeniu, przeczytaj [to FAQ](#user-content-faq35)
* *Automatycznie pokazuj obrazy dla znanych kontaktów*: aby automatycznie pokazać zdjęcia dla kontaktów na Twoim urządzeniu, przeczytaj [to FAQ](#user-content-faq35)
* *Pokaż pasek akcji konwersacji*: aby wyłączyć dolny pasek nawigacji

Pamiętaj, że podgląd wiadomości można wyświetlić tylko wtedy, gdy tekst wiadomości został pobrany. Większe wiadomości nie są pobierane (domyślnie) w sieciach komórkowych (zazwyczaj mobilnych). Możesz to zmienić w ustawieniach.

Jeśli lista adresów jest długa, możesz zwinąć sekcję adresów za pomocą ikony *less* u góry sekcji adresów.

Niektórzy ludzie proszą:

* aby pokazać temat pogrubiony, ale pogrubienie jest już używane do podświetlenia nieprzeczytanych wiadomości
* aby wyświetlić większy lub mniejszy adres lub temat, ale mogłoby to zakłócić opcję rozmiaru tekstu
* aby przesunąć gwiazdkę w lewo, ale o wiele łatwiej jest operować gwiazdą po prawej stronie

Niestety, nie można uszczęśliwić wszystkich, a dodanie wielu ustawień byłoby nie tylko mylące, ale także nigdy wystarczające.

<br />

<a name="faq6"></a>
**(6) Jak mogę zalogować się do Gmail / G suite?**

Możesz użyć kreatora szybkiej konfiguracji, aby łatwo skonfigurować konto Gmail i tożsamość.

Jeśli nie chcesz korzystać z konta Gmail znajdującego się na urządzeniu, możesz włączyć dostęp dla „mniej bezpiecznych aplikacji” i użyć hasła do konta lub włączyć uwierzytelnianie dwuskładnikowe i użyć hasła aplikacji. Zobacz to [FAQ](#user-content-faq111) dlaczego nie jest to możliwe na kontach innych niż na urządzeniu.

Pamiętaj, że hasło jest wymagane w przypadku włączenia uwierzytelniania dwuskładnikowego.

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

*Ogólnie*

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

Możesz użyć konta Microsoft Exchange, jeśli jest ono dostępne za pośrednictwem protokołu IMAP, co w większości przypadków ma miejsce. Zobacz [tutaj](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793) aby uzyskać więcej informacji.

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

Zobacz tutaj[FAQ ](#user-content-faq33), aby edytować nazwę użytkownika adresów e-mail.

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

W ustawieniach szyfrowania możesz wybrać domyślną metodę szyfrowania (PGP lub S/MIME), włącz * Domyślnie podpisuj *, * Domyślnie szyfruj * i * Automatycznie odszyfruj wiadomości *, ale pamiętaj, że automatyczne odszyfrowanie nie jest możliwe, jeśli wymagana jest interakcja użytkownika, na przykład wybranie klucza lub odczyt tokenu zabezpieczającego.

Tekst wiadomości/załączniki do zaszyfrowania oraz tekst wiadomości/załączniki odszyfrowane są przechowywane tylko lokalnie i nigdy nie zostaną dodane do zdalnego serwera. Jeśli chcesz cofnąć deszyfrowanie, możesz użyć pozycji menu * Resynchronizuj * w menu z trzema kropkami na pasku akcji wiadomości.

*PGP*

Najpierw musisz zainstalować i skonfigurować [OpenKeychain](https://f-droid.org/en/packages/org.sufficientlysecure.keychain/). FairEmail został przetestowany z OpenKeychain w wersji 5.4. Nowsze wersje będą kompatybilne, ale wcześniejsze wersje mogą nie być.

** Ważne **: wiadomo, że aplikacja OpenKeychain (po cichu) ulega awarii, gdy aplikacja wywołująca (FairEmail) nie jest jeszcze autoryzowana i otrzymuje istniejący klucz publiczny. Można to obejść, próbując wysłać podpisaną/zaszyfrowaną wiadomość do nadawcy z nieznanym kluczem publicznym.

** Ważne **: jeśli aplikacja OpenKeychain nie może (już) znaleźć klucza, konieczne może być zresetowanie wcześniej wybranego klucza. Można to zrobić przez długie naciśnięcie tożsamości na liście tożsamości (Ustawienia, krok 2, Zarządzaj).

** Ważne **: aby aplikacje takie jak FairEmail niezawodnie łączyły się z usługą OpenKeychain w celu szyfrowania/deszyfrowania wiadomości, może być konieczne wyłączenie optymalizacji baterii w aplikacji OpenKeychain.

** Ważne **: w niektórych wersjach/urządzeniach z Androidem konieczne jest włączenie * Pokaż wyskakujące okna podczas pracy w tle * w dodatkowych uprawnieniach ustawień aplikacji na Androida aplikacji OpenKeychain. Bez tego pozwolenia szkic zostanie zapisany, ale wyskakujące okno OpenKeychain w celu potwierdzenia/wyboru może się nie pojawić.

FairEmail wyśle ​​nagłówki [ Autocrypt ](https://autocrypt.org/) do wykorzystania przez innych klientów poczty e-mail i wyśle otrzymane nagłówki Autocrypt do aplikacji OpenKeychain w celu przechowywania.

Ze względów bezpieczeństwa cała obsługa klucza jest delegowana do aplikacji OpenKey chain. Oznacza to również, że FairEmail nie przechowuje kluczy PGP.

Obsługiwane są wbudowane PGP w odebranych wiadomościach, ale wbudowane podpisy PGP i wbudowane PGP w wiadomościach wychodzących nie są obsługiwane, zobacz [ tutaj ](https://josefsson.org/inline-openpgp-considered-harmful.html) o tym, dlaczego.

Wiadomości tylko podpisane lub zaszyfrowane nie są dobrym pomysłem, zobacz tutaj, dlaczego:

* [Rozważania OpenPGP Część I](https://k9mail.github.io/2016/11/24/OpenPGP-Considerations-Part-I.html)
* [Rozważania OpenPGP Część II](https://k9mail.github.io/2017/01/30/OpenPGP-Considerations-Part-II.html)
* [Rozważania OpenPGP Część III Auto-szyfrowanie](https://k9mail.github.io/2018/02/26/OpenPGP-Considerations-Part-III-Autocrypt.html)

Wiadomości 'tylko podpisane' są obsługiwane, wiadomości 'tylko szyfrowane' nie są obsługiwane.

*S/MIME*

Szyfrowanie wiadomości wymaga klucza/y publicznego odbiorcy (-ów). Podpisanie wiadomości wymaga klucza prywatnego.

Klucze prywatne są przechowywane przez system Android i można je importować za pomocą zaawansowanych ustawień zabezpieczeń systemu Android. Istnieje skrót (przycisk) do tego w ustawieniach prywatności. Android poprosi Cię o ustawienie kodu PIN, wzoru lub hasła, jeśli wcześniej tego nie zrobiłeś. Jeśli masz urządzenie Nokia z Androidem 9, [przeczytaj to najpierw](https://nokiamob.net/2019/08/10/a-bug-prevents-nokia-1-owners-from-unlocking-their-screen-even-with-right-pin-pattern/).

Pamiętaj, że certyfikaty mogą zawierać wiele kluczy do różnych celów, na przykład do uwierzytelniania, szyfrowania i podpisywania. Android importuje tylko pierwszy klucz, więc aby zaimportować wszystkie klucze, certyfikat należy najpierw podzielić. Nie jest to proste i zaleca się, aby poprosić dostawcę certyfikatu o wsparcie.

Domyślną metodą szyfrowania jest PGP, ale następnym razem ostatnia używana metoda szyfrowania zostanie zapamiętana dla wybranej tożsamości. Możesz ponownie potrzebować włączyć opcje wysyłania w menu trzech kropek, aby móc wybrać metodę szyfrowania.

To allow different private keys for the same email address, FairEmail will always let you select a key when there are multiple identities with the same email address for the same account.

Klucze publiczne są przechowywane przez FairEmail i mogą zostać zaimportowane przy pierwszej weryfikacji podpisu lub za pomocą ustawień prywatności (format PEM lub DER).

FairEmail weryfikuje zarówno podpis, jak i cały łańcuch certyfikatów.

Częste błędy:

* * No certificate found matching targetContraints*: prawdopodobnie oznacza to, że używasz starej wersji FairEmail
* *unable to find valid certification path to requested target *: zasadniczo oznacza to, że nie znaleziono jednego lub więcej certyfikatów pośrednich lub głównych
* *Private key does not match any encryption keys*: wybrany klucz nie może być użyty do odszyfrowania wiadomości, prawdopodobnie dlatego, że jest to nieprawidłowy klucz
* *No private key*: nie wybrano żadnego certyfikatu lub żaden certyfikat nie był dostępny w magazynie kluczy Androida

W przypadku gdy łańcuch certyfikatów jest nieprawidłowy, możesz kliknąć przycisk informacyjny aby wyświetlić wszystkie certyfikaty. Po szczegółach dotyczących certyfikatu wyświetlany jest wystawca lub „selfSign”. Certyfikat jest samopodpisany, gdy podmiot i wystawca są tacy sami. Certyfikaty z urzędu certyfikacji (CA) są oznaczone „[keyCertSign](https://tools.ietf.org/html/rfc5280#section-4.2.1.3)”. Certyfikaty znalezione w magazynie kluczy Androida są oznaczone 'Android'.

Prawidłowy łańcuch wygląda tak:

```
Your certificate > zero or more intermediate certificates > CA (root) certificate marked with "Android"
```

Note that a certificate chain will always be invalid when no anchor certificate can be found in the Android key store, which is fundamental to S/MIME certificate validation.

Zobacz [tutaj](https://support.google.com/pixelphone/answer/2844832?hl=en), jak możesz zaimportować certyfikaty do magazynu kluczy Androida.

The use of expired keys, inline encrypted/signed messages and hardware security tokens is not supported.

Jeśli szukasz darmowego (testowego) certyfikatu S/MIME, zapoznaj się z opcjami [tutaj](http://kb.mozillazine.org/Getting_an_SMIME_certificate). [ Przeczytaj to najpierw ](https://davidroessli.com/logs/2019/09/free-smime-certificates-in-2019/#update20191219) jeśli chcesz poprosić o certyfikat S/MIME Actalis.

Jak wyodrębnić klucz publiczny z certyfikatu S/MIME:

```
openssl pkcs12 -in filename.pfx/p12 -clcerts -nokeys -out cert.pem
```

Możesz dekodować podpisy S/MIME, itp., [tutaj](https://lapo.it/asn1js/).

Podpisywanie/szyfrowanie S/MIME jest funkcją pro, ale wszystkie inne operacje PGP i S/MIME są do użytku darmowego.

<br />

<a name="faq13"></a>
**(13) Jak działa wyszukiwanie na urządzeniu/serwerze?**

Możesz rozpocząć wyszukiwanie wiadomości od nadawcy (od), odbiorcy (do, dw, udw), tematu, słów kluczowych lub tekstu wiadomości, używając ikony lupy na pasku akcji folderu. You can also search from any app by selecting *Search email* in the copy/paste popup menu.

Searching in the unified inbox will search in all folders of all accounts, searching in the folder list will search in the associated account only and searching in a folder will search in that folder only.

Wiadomości będą najpierw wyszukiwane na urządzeniu. Na dole pojawi się przycisk akcji z ikoną wyszukiwania ponownie, aby kontynuować wyszukiwanie na serwerze. Możesz wybrać folder, w którym chcesz kontynuować wyszukiwanie.

Protokół IMAP nie wspiera wyszukiwania w więcej niż jednym folderze jednocześnie. Wyszukiwanie na serwerze jest kosztowną operacją, dlatego nie można wybrać wielu folderów.

Wyszukiwanie lokalnych wiadomości nie rozróżnia wielkości liter i częściowego tekstu. Tekst wiadomości w lokalnych wiadomościach nie będzie przeszukiwany, jeśli tekst wiadomości nie został jeszcze pobrany. Wyszukiwanie na serwerze może być wrażliwe na wielkość liter lub niewrażliwe na wielkość liter i może zawierać częściowy tekst lub całe słowa, w zależności od dostawcy.

Niektóre serwery nie obsługują wyszukiwania w tekście wiadomości, gdy istnieje duża liczba wiadomości. W takim przypadku istnieje opcja wyłączenia wyszukiwania w tekście wiadomości.

Przeszukiwanie dużej liczby wiadomości nie jest bardzo szybkie z powodu dwóch ograniczeń:

* [ sqlite ](https://www.sqlite.org/), silnik bazy danych Androida ma limit wielkości rekordów, uniemożliwiający przechowywanie tekstów wiadomości w bazie danych
* Aplikacje na Androida mają ograniczoną pamięć do pracy, nawet jeśli urządzenie ma wystarczająco dużo dostępnej pamięci

Oznacza to, że wyszukiwanie tekstu wiadomości wymaga, aby pliki zawierające teksty wiadomości były otwierane jeden po drugim, aby sprawdzić, czy szukany tekst jest zawarty w pliku, co jest stosunkowo kosztownym procesem.

W ustawieniach *różne* możesz włączyć *Utwórz indeks wyszukiwania*, aby znacznie zwiększyć szybkość wyszukiwania na urządzeniu, ale pamiętaj, że zwiększy to zużycie baterii i przestrzeni dyskowej. Indeks wyszukiwania opiera się na słowach, więc wyszukiwanie fragmentów tekstu nie jest możliwe. Searching using the search index is by default AND, so searching for *apple orange* will search for apple AND orange. Words separated by commas result in searching for OR, so for example *apple, orange* will search for apple OR orange. Obydwa działania można łączyć, więc wyszukiwanie *jabłko, pomarańcza banan* spowoduje wyszukanie jabłko LUB (pomarańcza i banan). Korzystanie z indeksu wyszukiwania jest funkcją pro.

Wyszukiwanie wiadomości na urządzeniu jest funkcją darmową, wyszukiwanie wiadomości na serwerze jest funkcją pro.

<br />

<a name="faq14"></a>
**(14) Jak mogę ustawić konto Outlook / Live / Hotmail?**

Konto Outlook / Live / Hotmail można skonfigurować za pomocą szybkiego kreatora konfiguracji i wybraniu *Outlook*.

Aby użyć konta Outlook, Live lub Hotmail z uwierzytelnianiem dwuskładnikowym, musisz utworzyć hasło aplikacji. Szczegóły znajdziesz [tutaj](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification).

Zobacz [tutaj](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) instrukcje Microsoftu.

W celu skonfigurowania konta Office365, zobacz [ten FAQ](#user-content-faq156).

<br />

<a name="faq15"></a>
**(15) Dlaczego tekst wiadomości wciąż się ładuje?**

Nagłówek i treść wiadomości są pobierane oddzielnie od serwera. The message text of larger messages is not being pre-fetched on metered connections and need to be fetched on opening the message. Tekst wiadomości będzie wciąż ładowany, jeśli nie ma połączenia z kontem, zobacz również następne pytanie.

Możesz sprawdzić listę kont i folderów pod kątem stanu kont i folderów (zobacz legendę na temat znaczenia ikon) oraz listę operacji dostępną w głównym menu nawigacyjnym dla operacji oczekujących (zobacz [ten FAQ](#user-content-faq3)), aby sprawdzić znaczenie operacji.

W ustawieniach odbierania możesz ustawić maksymalny rozmiar dla automatycznego pobierania wiadomości przy połączeniach taryfowych.

Łączność komórkowa jest prawie zawsze taryfowa i niektóre (płatne) hotspoty Wi-Fi również.

<br />

<a name="faq16"></a>
**(16) Dlaczego wiadomości nie są synchronizowane?**

Możliwymi przyczynami braku synchronizacji wiadomości (wysłanych lub odebranych) są:

* Konto lub folder(y) nie są ustawione do synchronizacji
* Ustawiono zbyt niską liczbę dni do synchronizacji wiadomości
* Brak użytecznego połączenia z Internetem
* Serwer e-mail jest tymczasowo nieosiągalny
* Android zatrzymał usługę synchronizacji

So, check your account and folder settings and check if the accounts/folders are connected (see the legend in the navigation menu for the meaning of the icons).

Jeśli są jakiekolwiek komunikaty o błędach, zobacz [ten FAQ](#user-content-faq22).

Na niektórych urządzeniach, gdzie istnieje wiele aplikacji konkurujących o pamięć, Android może w ostateczności zatrzymać usługę synchronizacji.

Niektóre wersje Androida zbyt agresywnie zatrzymują aplikacje i usługi. Zobacz [tę dedykowaną stronę](https://dontkillmyapp.com/) i [ten problem z Androidem](https://issuetracker.google.com/issues/122098785), aby uzyskać więcej informacji.

Wyłączenie optymalizacji baterii (etap 4 konfiguracji) zmniejsza szansę, że Android zatrzyma usługę synchronizacji.

<br />

<a name="faq17"></a>
**~~(17) Dlaczego ręczna synchronizacja nie działa?~~**

~~Jeśli menu *Synchronizuj teraz* jest przyciemnione, nie ma połączenia z kontem.~~

~~Zobacz poprzednie pytanie, aby uzyskać więcej informacji.~~

<br />

<a name="faq18"></a>
**(18) Dlaczego podgląd wiadomości nie zawsze jest wyświetlany?**

Podgląd tekstu wiadomości nie może być wyświetlony, jeśli treść wiadomości nie została jeszcze pobrana. Zobacz również [ten FAQ](#user-content-faq15).

<br />

<a name="faq19"></a>
**(19) Dlaczego funkcje pro są tak drogie?**

Właściwe pytanie brzmi: „*dlaczego jest tyle podatków i opłat?*”:

* VAT: 25 % (w zależności od kraju)
* Opłata Google: 30 %
* Podatek dochodowy: 50 %
* <sub>Opłata PayPal: 5–10 % w zależności od kraju/kwoty</sub>

A zatem to, co pozostało dla dewelopera, to tylko ułamek tego, co płacisz.

Note that only some convenience and advanced features need to be purchased which means that FairEmail is basically free to use.

Also note that most free apps will appear not to be sustainable in the end, whereas FairEmail is properly maintained and supported, and that free apps may have a catch, like sending privacy sensitive information to the internet.

Od ponad półtora roku, prawie codziennie pracowałem nad FairEmail, więc uważam, że cena jest na rozsądnym poziomie. Z tego powodu również nie będzie zniżek.

<br />

<a name="faq20"></a>
**(20) Czy mogę otrzymać zwrot pieniędzy?**

Jeśli zakupiona funkcja pro nie działa zgodnie z zamierzeniem i nie jest to spowodowane problemem w darmowych funkcjach i nie mogę rozwiązać problemu w odpowiednim czasie, możesz otrzymać zwrot pieniędzy. We wszystkich innych przypadkach zwrot nie jest możliwy. W żadnym wypadku nie ma możliwości zwrotu kosztów w przypadku problemów związanych z darmowymi funkcjami, ponieważ nie było za nie opłaty i można było je ocenić bez żadnych ograniczeń. Jako sprzedawca biorę odpowiedzialność za dostarczenie tego, co zostało obiecane i spodziewam się, że weźmiesz odpowiedzialność za zaznajomienie się z tym, co kupujesz.

<a name="faq21"></a>
**(21) Jak włączyć diodę powiadomień?**

Przed Androidem 8 Oreo: do ustawienia tego dostępna jest zaawansowana opcja.

Android 8 Oreo i nowsze: zobacz [tutaj](https://developer.android.com/training/notify-user/channels) jak skonfigurować kanały powiadomień. Możesz użyć przycisku *Zarządzaj powiadomieniami* w konfiguracji, aby bezpośrednio przejść do ustawień powiadomień Androida. Pamiętaj, że aplikacje nie mogą zmieniać ustawień powiadomień, w tym ustawień diody powiadomień na Androidzie 8 Oreo i nowszych. Aplikacje zaprojektowane i skierowane na starsze wersje Androida mogą nadal kontrolować zawartość powiadomień, ale takich aplikacji nie można już aktualizować, a najnowsze wersje Androida wyświetlają ostrzeżenie, że takie aplikacje są nieaktualne.

Czasami należy wyłączyć ustawienie *Pokaż podgląd wiadomości w powiadomieniach* lub włączyć ustawienie *Pokaż powiadomienia tylko z podglądem tekstu* w celu obejścia błędu w systemie Android. Może to dotyczyć dźwięków powiadomień, jak również wibracji.

Ustawienie koloru światła w wersji niższej niż Android 8 nie jest wspierane a na Androidzie 8 lub nowszym nie jest możliwe.

<br />

<a name="faq22"></a>
**(22) Co oznacza błąd konta/folderu ... ?**

FairEmail nie ukrywa błędów jak to robią podobne aplikacje, więc łatwiej jest zdiagnozować problemy.

FairEmail automatycznie spróbuje, z opóźnieniem, połączyć się ponownie. Opóźnienie to zostanie podwojone po każdej nieudanej próbie, aby zapobiec rozładowaniu akumulatora i trwałego zablokowania.

Istnieją błędy ogólne i błędy specyficzne dla kont Gmail (zobacz poniżej).

**Błędy ogólne**

Błąd *... Uwierzytelnianie nie powiodło się ...* lub *... AUTHENTICATE failed ...* zwykle oznacza, że nazwa użytkownika lub hasło jest nieprawidłowe. Niektórzy dostawcy oczekują nazwy użytkownika jako *nazwa użytkownika*, inni Twojego pełnego adresu e-mail *username@example.com*. Używanie metody kopiuj/wklej do wprowadzania nazwy użytkownika lub hasła może powodować kopiowanie niewidocznych znaków, co może również doprowadzić do tego problemu. Inne możliwe przyczyny to zablokowanie konta lub w pewien sposób administracyjne ograniczenie logowania, na przykład zezwolenie na logowanie tylko z niektórych sieci / adresów IP.

Błąd *... Too many bad auth attempts ...* prawdopodobnie oznacza, że używasz hasła do konta Yahoo zamiast hasła aplikacji. Zobacz [ten FAQ](#user-content-faq88) na temat konfiguracji konta Yahoo.

Komunikat *... +OK ...* prawdopodobnie oznacza, że port POP3 (zazwyczaj numer portu 995) jest używany dla konta IMAP (zazwyczaj numer portu 993).

Błędy *... invalid greeting ...*, *... requires valid address ...* i *... Parameter to HELO does not conform to RFC syntax ...* can likely be solved by changing the advanced identity setting *Use local IP address instead of host name*.

Błędy *... Nie można połączyć się z hostem ...*, *... Połączenie odrzucone ...* lub *... Sieć nieosiągalna ...* oznacza, że FairEmail nie był w stanie połączyć się z serwerem e-mail.

Błąd *... Host is unresolved ...* or "*... Unable to resolve host ...* means that the address of the email server could not be resolved. Może to być spowodowane przez blokowanie reklam lub nieosiągalny lub nieprawidłowo działający serwer [DNS](https://en.wikipedia.org/wiki/Domain_Name_System).

Błąd *... Oprogramowanie spowodowało przerwanie połączenia ...* oznacza, że serwer e-mail lub coś pomiędzy FairEmail a serwerem e-mail aktywnie zakończyło istniejące połączenie. Może się to zdarzyć na przykład w przypadku nagłej utraty łączności. Typowym przykładem jest włączenie trybu samolotowego.

Błąd *... BYE Logging out ...*, *... Connection reset by peer ...* or *... Broken pipe ...* oznacza, że serwer e-mail aktywnie zakończył istniejące połączenie.

Błąd *... Connection closed by peer ...* może być spowodowany przez nieaktualizowany serwer Exchange, więcej informacji można znaleźć [tutaj](https://blogs.technet.microsoft.com/pki/2010/09/30/sha2-and-windows/).

Błędy *... Błąd odczytu ...*, *... Błąd zapisu ...*, *... Read timed out ...* oznaczają, że ​​serwer e-mail nie odpowiada lub jest złe połączenie internetowe.

Błąd *... Unexpected end of zlib input stream ...*oznacza, że ​​nie wszystkie dane zostały odebrane, prawdopodobnie z powodu złego lub przerwanego połączenia.

Błąd *... błąd połączenia ...* może wskazywać na [Zbyt wiele jednoczesnych połączeń](#user-content-faq23).

Ostrzeżenie *... Niewpierane kodowanie ...* oznacza, że zestaw znaków wiadomości jest nieznany lub nie jest obsługiwany. FairEmail założy ISO-8859-1 (Latin1), co w większości przypadków spowoduje poprawne wyświetlenie wiadomości.

Zobacz [tutaj](#user-content-faq4) dla błędów *... Untrusted ... not in certificate ...*, *... Nieprawidłowy certyfikat bezpieczeństwa (nie można zweryfikować tożsamości serwera) ...* lub *... Trust anchor for certification path not found ...*

Zobacz [tutaj](#user-content-faq127) dla błędu *... Syntactically invalid HELO argument(s) ...*.

Zobacz [tutaj](#user-content-faq41) dla błędu *... Handshake failed ...*.

Zobacz [tutaj](https://linux.die.net/man/3/connect), aby dowiedzieć się co znaczą kody błędów takie jak EHOSTUNREACH i ETIMEDOUT.

Możliwymi przyczynami są:

* Zapora lub router blokuje połączenia z serwerem
* Nazwa hosta lub numer portu jest nieprawidłowy
* Istnieją problemy z połączeniem internetowym
* Serwer e-mail odmawia akceptowania połączeń
* Serwer e-mail odmawia przyjęcia wiadomości, na przykład dlatego, że jest zbyt duża lub zawiera niedopuszczalne linki
* Istnieje zbyt wiele połączeń z serwerem, zobacz również następne pytanie

Wiele publicznych sieci Wi-Fi blokuje wychodzące wiadomości e-mail, aby zapobiec rozsyłaniu spamu. Czasem możesz obejść to używając innego portu SMTP. Zobacz dokumentację dostawcy dotyczącą możliwych do użycia numerów portów.

Jeśli używasz [VPN](https://en.wikipedia.org/wiki/Virtual_private_network), dostawca usługi VPN może zablokować połączenie, ponieważ zbyt agresywnie próbuje zapobiec wysyłaniu spamu. Zauważ, że [Google Fi](https://fi.google.com/) również używa VPN.

**Błędy Gmail**

Autoryzacja konfiguracji kont Gmail za pomocą szybkiego kreatora wymaga okresowego odświeżania za pomocą [Menedżera kont Androida](https://developer.android.com/reference/android/accounts/AccountManager). Wymaga to uprawnień do kontaktów/konta oraz połączenia z Internetem.

Błąd *... Uwierzytelnianie nie powiodło się ... Konto nie znalezione ...* oznacza, że poprzednio autoryzowane konto Gmail zostało usunięte z urządzenia.

Błędy *... Uwierzytelnianie nie powiodło się ... Brak tokenu przy odświeżeniu ...* oznacza, że menedżer konta Android nie odświeżył autoryzacji konta Gmail.

Błąd *... Uwierzytelnianie nie powiodło się ... Nieprawidłowe dane logowania ... błąd sieci ... * oznacza, że menedżer konta Android nie był w stanie odświeżyć autoryzacji konta Gmail z powodu problemów z połączeniem internetowym

Błąd *... Uwierzytelnianie nie powiodło się ... Nieprawidłowe dane logowania...* mogą być spowodowane cofnięciem wymaganych uprawnień do konta/kontaktów. Po prostu uruchom kreatora (ale nie wybieraj konta), aby ponownie przyznać wymagane uprawnienia.

Błąd *... ServiceDisabled ...* might be caused by enrolling in the [Advanced Protection Program](https://landing.google.com/advancedprotection/): "*To read your email, you can (must) use Gmail - You won’t be able to use your Google Account with some (all) apps & services that require access to sensitive data like your emails*", see [here](https://support.google.com/accounts/answer/7519408?hl=en&ref_topic=9264881).

W razie wątpliwości możesz poprosić o [wsparcie](#user-content-support).

<br />

<a name="faq23"></a>
**(23) Dlaczego otrzymuję ostrzeżenie ... ?**

*Ogólne*

Alerty to wiadomości ostrzegawcze wysyłane przez serwery e-mail.

*Zbyt wiele jednoczesnych połączeń* lub *Przekroczono maksymalną liczbę połączeń*

Ten alert zostanie wysłany, gdy będzie zbyt wiele jednoczesnych połączeń do folderów dla tego samego konta e-mail.

Możliwymi przyczynami są:

* Istnieje wiele klientów poczty e-mail podłączonych do tego samego konta
* Ten sam klient poczty e-mail jest wielokrotnie podłączony do tego samego konta
* Poprzednie połączenia zostały nagle zakończone, na przykład przez utratę łączności z Internetem

Najpierw spróbuj odczekać jakiś czas, aby sprawdzić, czy problem sam się rozwiązał, w przeciwnym razie:

* albo przełącz się na okresowe sprawdzanie wiadomości w ustawieniach odbioru, co spowoduje otwarcie folderów pojedynczo
* lub ustaw niektóre foldery do odpytywania zamiast synchronizacji (długie naciśnięcie folderu na liście folderów, edycja właściwości)

Maksymalna liczba jednoczesnych połączeń do folderów dla Gmail to 15, więc możesz w tym samym czasie synchronizować maksymalnie 15 folderów jednocześnie na *wszystkich* Twoich urządzeniach. Z tego powodu foldery Gmail *użytkownika* są domyślnie ustawione na odpytywanie zamiast ciągłej synchronizacji. Gdy jest to konieczne lub pożądane, możesz to zmienić przez długie naciśnięcie folderu na liście folderów i wybranie *Edytuj właściwości*. Szczegóły znajdziesz [tutaj](https://support.google.com/mail/answer/7126229).

When using a Dovecot server, you might want to change the setting [mail_max_userip_connections](https://doc.dovecot.org/settings/dovecot_core_settings/#mail-max-userip-connections).

<br />

<a name="faq24"></a>
**(24) Czym jest przeglądanie wiadomości na serwerze?**

Przeglądanie wiadomości na serwerze będzie pobierać wiadomości z serwera pocztowego w czasie rzeczywistym po dotarciu do końca listy zsynchronizowanych wiadomości, nawet gdy folder jest ustawiony na brak synchronizacji. Możesz wyłączyć tę funkcję w zaawansowanych ustawieniach konta.

<br />

<a name="faq25"></a>
**(25) Dlaczego nie mogę wybrać/otworzyć/zapisać obrazu, załącznika lub pliku?**

When a menu item to select/open/save a file is disabled (dimmed) or when you get the message *Storage access framework not available*, the [storage access framework](https://developer.android.com/guide/topics/providers/document-provider), a standard Android component, is probably not present. This might be because your custom ROM does not include it or because it was actively removed (debloated).

FairEmail does not request storage permissions, so this framework is required to select files and folders. No app, except maybe file managers, targeting Android 4.4 KitKat or later should ask for storage permissions because it would allow access to *all* files.

The storage access framework is provided by the package *com.android.documentsui*, which is visible as *Files* app on some Android versions (notable OxygenOS).

You can enable the storage access framework (again) with this adb command:

```
pm install -k --user 0 com.android.documentsui
```

Alternatively, you might be able to enable the *Files* app again using the Android app settings.

<br />

<a name="faq26"></a>
**(26) Czy mogę pomóc w tłumaczeniu FairEmail na mój własny język?**

Tak, możesz przetłumaczyć teksty FairEmail na własny język [na Crowdin](https://crowdin.com/project/open-source-email). Rejestracja jest darmowa.

<br />

<a name="faq27"></a>
**(27) W jaki sposób mogę odróżnić osadzone i zewnętrzne obrazy?**

Obraz zewnętrzny:

![Obraz zewnętrzny](https://raw.githubusercontent.com/google/material-design-icons/master/image/1x_web/ic_image_black_48dp.png)

Obraz osadzony:

![Obraz osadzony](https://raw.githubusercontent.com/google/material-design-icons/master/image/1x_web/ic_photo_library_black_48dp.png)

Obraz uszkodzony:

![Obraz uszkodzony](https://raw.githubusercontent.com/google/material-design-icons/master/image/1x_web/ic_broken_image_black_48dp.png)

Note that downloading external images from a remote server can be used to record you did see a message, which you likely don't want if the message is spam or malicious.

<br />

<a name="faq28"></a>
**(28) Jak mogę zarządzać powiadomieniami na pasku statusu?**

In the setup you'll find a button *Manage notifications* to directly navigate to the Android notifications settings for FairEmail.

On Android 8.0 Oreo and later you can manage the properties of the individual notification channels, for example to set a specific notification sound or to show notifications on the lock screen.

FairEmail ma następujące kanały powiadomień:

* Usługa: używana do powiadamiania o usłudze synchronizacji, zobacz również [to FAQ](#user-content-faq2)
* Wyślij: używane do powiadamiania o usłudze wysyłania
* Powiadomienia: używane do powiadomień o nowych wiadomościach
* Ostrzeżenia: używane do powiadomień ostrzegawczych
* Błędy: używane do powiadomień o błędach

Zobacz [tutaj](https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels) szczegóły dotyczące kanałów powiadomień. W skrócie: dotknij nazwy kanału powiadomień, aby uzyskać dostęp do ustawień kanału.

Na Androidzie przed Androidem 8 Oreo możesz ustawić dźwięk powiadomień w ustawieniach.

Zobacz [ten FAQ](#user-content-faq21) jeśli twoje urządzenie ma diodę powiadomień.

<br />

<a name="faq29"></a>
**(29) How can I get new message notifications for other folders?**

Po prostu naciśnij folder, wybierz *Edytuj właściwości*, i włącz *Pokaż we wspólnej skrzynce* lub *Powiadom o nowych wiadomościach* (dostępne tylko na Androidzie 7 Nougat i później) i dotknij *Zapisz*.

<br />

<a name="faq30"></a>
**(30) Jak mogę użyć dostarczonych szybkich ustawień?**

There are quick settings (settings tiles) available to:

* globalnie włącz/wyłącz synchronizację
* pokaż liczbę nowych wiadomości i oznacz je jako widoczne (nie przeczytane)

Quick settings require Android 7.0 Nougat or later. The usage of settings tiles is explained [here](https://support.google.com/android/answer/9083864).

<br />

<a name="faq31"></a>
**(31) W jaki sposób mogę korzystać z dostarczonych skrótów?**

There are shortcuts available to:

* stwórz nową wiadomość do ulubionego kontaktu
* zakładanie kont, tożsamości itp

Skróty wymagają Androida 7.1 Nougat lub nowszego. Użycie skrótów jest wyjaśnione [tutaj](https://support.google.com/android/answer/2781850).

<br />

<a name="faq32"></a>
**(32) Jak mogę sprawdzić, czy czytanie wiadomości e-mail jest naprawdę bezpieczne?**

Możesz w tym celu użyć [Email Privacy Tester](https://www.emailprivacytester.com/).

<br />

<a name="faq33"></a>
**(33) Why are edited sender addresses not working?**

Most providers accept validated addresses only when sending messages to prevent spam.

For example Google modifies the message headers like this for *unverified* addresses:

```
From: Somebody <somebody@example.org>
X-Google-Original-From: Somebody <somebody+extra@example.org>
```

This means that the edited sender address was automatically replaced by a verified address before sending the message.

Pamiętaj, że jest to niezależne od odbierania wiadomości.

<br />

<a name="faq34"></a>
** (34) W jaki sposób dopasowywane są tożsamości? **

Identities are as expected matched by account. W przypadku wiadomości przychodzących *do*, *dw*, *udw*, *od* i *(X-)delivered/envelope/original-to* adresy zostaną sprawdzone (w tej kolejności) a dla wiadomości wychodzących (wersje robocze, skrzynka nadawcza i wysłane) sprawdzane będą tylko adresy *od*.

Pasujący adres zostanie wyświetlony jako *przez* w sekcji adresów.

Pamiętaj, że tożsamości muszą być włączone, aby można je było dopasować, i że tożsamość innych kont nie będzie brana pod uwagę.

Dopasowanie zostanie wykonane tylko raz po otrzymaniu wiadomości, więc zmiana konfiguracji nie zmieni istniejących wiadomości. Możesz wyczyścić wiadomości lokalne przez długie naciśnięcie folderu na liście folderów i zsynchronizowanie wiadomości ponownie.

Możliwe jest skonfigurowanie [regex](https://en.wikipedia.org/wiki/Regular_expression) w ustawieniach tożsamości, aby dopasować nazwę użytkownika adresu e-mail (część przed znakiem @).

Zauważ, że nazwa domeny (ciąg po znaku @) zawsze musi odpowiadać nazwie domeny tożsamości.

Jeśli chcesz dopasować adresy e-mail specjalnego przeznaczenia abc@example.com i xyx@example.com i chcesz mieć zastępczy adres e-mail main@example.com, możesz zrobić coś takiego:

* Tożsamość: abc@example.com; regex: **(?i)abc**
* Tożsamość: xyz@example.com; regex: **(?i)xyz**
* Tożsamość: main@example.com; regex: **^(?i)((?!abc|xyz).)\*$**

Dopasowane tożsamości mogą być użyte do kolorowania wiadomości. Kolor tożsamości ma pierwszeństwo przed kolorem konta. Ustawienie kolorów tożsamości jest funkcją pro.

<br />

<a name="faq35"></a>
**(35) Dlaczego powinienem być ostrożny z przeglądaniem obrazów, załączników i oryginalnej wiadomości?**

Wyświetlanie zdalnie przechowywanych obrazów (patrz także [ten FAQ](#user-content-faq27)) może nie tylko poinformować nadawcę, że widziałeś wiadomość, ale spowodować, że wycieknie również twój adres IP.

Otwieranie załączników lub przeglądanie oryginalnej wiadomości może ładować zdalną treść i wykonywać skrypty, co może nie tylko spowodować wyciek poufnych informacji, ale może również stanowić zagrożenie bezpieczeństwa.

Pamiętaj, że Twoje kontakty mogą nieświadomie wysyłać złośliwe wiadomości, jeśli zostaną zainfekowane złośliwym oprogramowaniem.

FairEmail ponownie formatuje wiadomości, powodując, że wyglądają one inaczej niż oryginalne, ale także odkrywa linki służące do phishingu.

Zauważ, że formatowane wiadomości są często bardziej czytelne niż oryginalne wiadomości, ponieważ marginesy są usuwane, a kolory czcionek i rozmiary są znormalizowane.

Aplikacja Gmail domyślnie wyświetla obrazy pobierając obrazy przez serwer proxy Google. Ponieważ obrazy są pobierane z serwera źródłowego [w czasie rzeczywistym](https://blog.filippo.io/how-the-new-gmail-image-proxy-works-and-what-this-means-for-you/), jest to jeszcze mniej bezpieczne, ponieważ Google jest również zaangażowane bez zapewnienia wielu korzyści.

Domyślnie możesz wyświetlać obrazy i oryginalne wiadomości dla zaufanych nadawców indywidualnie, zaznaczając *Nie pytaj ponownie ...*.

Jeśli chcesz zresetować domyślne aplikacje *Otwórz z*, proszę [zobacz tutaj](https://www.androidauthority.com/how-to-set-default-apps-android-clear-621269/).

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

<br />

<a name="faq39"></a>
**(39) Jak mogę zmniejszyć zużycie baterii przez FairEmail?**

Ostatnie wersje Androida domyślnie raportują *użycie aplikacji* jako procent na ekranie ustawień baterii Androida. ** Myląco *użycie aplikacji* to nie to samo co *zużycie baterii* i nie jest nawet bezpośrednio związane ze zużyciem baterii!** Użycie aplikacji (podczas używania) będzie bardzo wysokie, ponieważ FairEmail korzysta z usługi pierwszego planu, która jest uważana przez Androida za ciągłe korzystanie z aplikacji. Nie oznacza to jednak, że FairEmail stale zużywa energię baterii. Rzeczywiste zużycie baterii można zobaczyć przechodząc do tego ekranu:

*Ustawienia Androida*, *Bateria*, menu trzech kropek *Użycie baterii*, menu trzech kropek *Pokaż pełne użycie urządzenia*

Z reguły zużycie baterii powinno być niższe lub nie powinno być znacznie wyższe niż *Tryb czuwania w sieci komórkowej*. Jeśli tak się nie stanie, proszę dać mi znać.

Jest nieuniknione, że synchronizacja wiadomości zużyje energię baterii, ponieważ wymaga dostępu do sieci i dostępu do bazy danych wiadomości.

Jeśli porównujesz zużycie baterii FairEmail z innym klientem e-mail, upewnij się, że inny klient poczty e-mail jest podobnie skonfigurowany. Na przykład porównywanie zawsze synchronizowanych (wiadomości push) i (rzadkie) okresowe sprawdzanie nowych wiadomości nie jest uczciwym porównaniem.

Ponowne połączenie z serwerem e-mail wykorzysta dodatkową moc baterii, więc niestabilne połączenie internetowe spowoduje dodatkowe zużycie baterii. W tym przypadku możesz chcieć okresowo synchronizować, na przykład co godzinę, zamiast stale synchronizować. Pamiętaj, że częste sprawdzanie (częściej niż co 30-60 minut) prawdopodobnie zużywa więcej energii baterii niż synchronizacja stała ponieważ połączenie z serwerem i porównanie wiadomości lokalnych i zdalnych to kosztowne operacje.

[Na niektórych urządzeniach](https://dontkillmyapp.com/) konieczne jest *wyłączenie* optymalizacji baterii (etap konfiguracji 4), aby połączenia z serwerami e-mail były otwarte.

Większość zużycia baterii, bez uwzględniania oglądania wiadomości, wynika z synchronizacji (odbieranie i wysyłanie) wiadomości. Aby zmniejszyć zużycie baterii, ustaw liczbę dni synchronizacji wiadomości na niższą wartość, szczególnie jeśli w folderze jest wiele niedawnych wiadomości. Przytrzymaj nazwę folderu na liście folderów i wybierz *Edytuj właściwości*, aby uzyskać dostęp do tego ustawienia.

Jeśli masz przynajmniej raz dziennie połączenie z Internetem, wystarczy zsynchronizować wiadomości tylko na jeden dzień.

Pamiętaj, że możesz ustawić liczbę dni na *przechowywanie* wiadomości na wyższą liczbę niż na *synchronizowanie* wiadomości. Możesz na przykład wstępnie zsynchronizować wiadomości z dużej ilości dni, a po zakończeniu zmniejszyć liczbę dni do synchronizacji wiadomości, ale pozostawić liczbę dni, przez które wiadomości będą przechowywane.

W ustawieniach odbierania możesz włączyć stałe synchronizowanie wiadomości oznaczonych gwiazdką, co pozwoli ci zachować starsze wiadomości podczas synchronizacji wiadomości dla ograniczonej liczby dni.

Wyłączenie opcji folderu *Automatycznie pobieraj treści wiadomości i załączniki* spowoduje zmniejszenie ruchu sieciowego, a tym samym zmniejszenie zużycia baterii. Możesz wyłączyć tę opcję, na przykład dla folderu wysłane i archiwum.

Synchronizacja wiadomości w nocy jest w większości nieprzydatna, więc możesz zaoszczędzić na zużyciu baterii, nie synchronizując w nocy. W ustawieniach możesz wybrać harmonogram synchronizacji wiadomości (jest to funkcja pro).

FairEmail domyślnie synchronizuje listę folderów przy każdym połączeniu. Ponieważ foldery zwykle nie są tworzone, zmieniane i usuwane bardzo często, możesz zaoszczędzić trochę danych i baterii wyłączając to w ustawieniach odbioru.

FairEmail będzie domyślnie sprawdzać, czy stare wiadomości zostały usunięte z serwera przy każdym połączeniu. Jeśli nie masz nic przeciwko temu, że stare wiadomości, które zostały usunięte z serwera są nadal widoczne w FairEmail, możesz zaoszczędzić trochę danych i baterii wyłączając to w ustawieniach odbioru.

Niektórzy dostawcy nie stosują się do standardu IMAP i nie utrzymują wystarczająco długo połączenia, zmuszając FairEmail do częstego ponawiania połączenia, powodując dodatkowe zużycie baterii. Możesz przejrzeć *Log* poprzez główne menu nawigacyjne, aby sprawdzić, czy często dochodzi do ponownych połączeń (połączenie zamknięte/zresetowane/błąd odczytu/zapisu/przekroczenie limitu czasu, itp.). Można to obejść, obniżając interwał utrzymywania aktywności w zaawansowanych ustawieniach konta do na przykład 9 lub 15 minut. Pamiętaj, że optymalizacje baterii muszą zostać wyłączone w kroku konfiguracji 4, aby niezawodnie utrzymywać aktywne połączenia.

Niektórzy dostawcy wysyłają co dwie minuty coś takiego: '*Still here*' skutkującego ruchem sieciowym i wybudzającym urządzeniem i powodującym niepotrzebne dodatkowe zużycie baterii. Możesz sprawdzić *Log* w głównym menu nawigacyjnym, aby sprawdzić, czy twój dostawca tak robi. Jeśli Twój dostawca używa [Dovecot](https://www.dovecot.org/) jako serwera IMAP, możesz poprosić swojego dostawcę o zmianę ustawienia [imap_idle_notify_interval](https://wiki.dovecot.org/Timeouts) na wyższą wartość, albo najlepiej aby to wyłączył. Jeśli Twój dostawca nie jest w stanie lub nie chce tego zmienić/wyłączyć, powinieneś rozważyć przełączanie się na synchronizację okresową zamiast ciągłej. Możesz to zmienić w ustawieniach odbierania.

Jeśli otrzymałeś wiadomość *Ten dostawca nie obsługuje wiadomości push* podczas konfigurowania konta, rozważ przejście na nowoczesnego dostawcę obsługującego wiadomości push (IMAP IDLE) w celu zmniejszenia zużycia baterii.

Jeśli twoje urządzenie ma ekran [AMOLED](https://en.wikipedia.org/wiki/AMOLED), możesz zaoszczędzić baterię podczas oglądania wiadomości przełączając motyw na czarny.

Domyślnie włączona jest automatyczna optymalizacja w ustawieniach odbioru, co spowoduje przełączenie konta na okresowe sprawdzanie nowych wiadomości, gdy serwer e-mail:

* Wysyła '*Still here*' w przeciągu 3 minut
* Serwer e-mail nie wspiera wiadomości push
* Odstęp między keep-alive jest krótszy niż 12 minut

<br />

<a name="faq40"></a>
**(40) Jak mogę zmniejszyć użycie sieci przez FairEmail?**

Możesz zmniejszyć zużycie danych w ten sam sposób, co zmniejszenie zużycia baterii, zobacz poprzednie pytanie, aby uzyskać sugestie.

Domyślnie FairEmail nie pobiera tekstów wiadomości i załączników większych niż 256 kB w przypadku połączenia taryfowego (sieć komórkowa lub płatna Wi-Fi). Możesz to zmienić w ustawieniach połączenia.

<br />

<a name="faq41"></a>
**(41) Jak mogę naprawić błąd 'Handshake failed' ?**

Istnieje kilka możliwych przyczyn, więc proszę przeczytać do końca tę odpowiedź.

Błąd '*Handshake failed ... WRONG_VERSION_NUMBER ... *' może oznaczać, że próbujesz połączyć się z serwerem IMAP lub SMTP bez szyfrowanego połączenia, zwykle przy użyciu portu 143 (IMAP) i portu 25 (SMTP) lub, że używany jest zły protokół (SSL/TLS lub STARTTLS).

Większość dostawców zapewnia zaszyfrowane połączenia za pomocą różnych portów, zazwyczaj portu 993 (IMAP) i portu 465/587 (SMTP).

Jeśli Twój dostawca nie obsługuje zaszyfrowanych połączeń, powinieneś to zgłosić, aby to umożliwił. Jeśli to nie jest możliwe, możesz włączyć *Zezwalaj na niezabezpieczone połączenia* zarówno w ustawieniach zaawansowanych oraz w ustawieniach konta/tożsamości.

Zobacz również [ten FAQ](#user-content-faq4).

Błąd '*Handshake failed ... SSLV3_ALERT_ILLEGAL_PARAMETER ... *' jest spowodowany błędem w implementacji protokołu SSL lub zbyt krótkim kluczem DH na serwerze e-mail i niestety nie może być naprawiony przez FairEmail.

Błąd '*Handshake failed ... HANDSHAKE_FAILURE_ON_CLIENT_HELLO ...*' może być spowodowany przez dostawcę nadal używającego RC4, który nie jest już obsługiwany od [Android 7](https://developer.android.com/about/versions/nougat/android-7.0-changes.html#tls-ssl).

Błąd '*Handshake failed ... UNSUPPORTED_PROTOCOL ...*' może być spowodowane włączeniem wzmocnionych połączeń w ustawieniach połączenia lub przez Androida nieobsługującego starszych protokołów, jak SSLv3.

Android 8 Oreo i nowsze już [nie wspierają](https://developer.android.com/about/versions/oreo/android-8.0-changes#security-all) SSLv3. Nie ma sposobu na obejście tego przy braku wsparcia RC4 i SSLv3, ponieważ zostało to całkowicie usunięte z Androida (to daje do myślenia).

Możesz użyć [tej strony](https://ssl-tools.net/mailservers) lub [tej strony](https://www.immuniweb.com/ssl/), aby sprawdzić czy nie występują problemy SSL/TLS serwerów e-mail.

<br />

<a name="faq42"></a>
**(42) Czy możesz dodać nowego dostawcę do listy dostawców?**

Jeśli z usługodawcy korzysta więcej niż kilka osób, tak, z przyjemnością.

Potrzebna jest następująca informacja:

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

EFF [pisze](https://www.eff.org/nl/deeplinks/2018/06/announcing-starttls-everywhere-securing-hop-hop-email-delivery): "*Ponadto, nawet jeśli doskonale skonfigurujesz STARTTLS i użyjesz ważnego certyfikatu, nadal nie ma gwarancji, że Twoja komunikacja będzie szyfrowana.*"

Zatem czyste połączenia SSL są bezpieczniejsze niż korzystanie z [STARTTLS](https://en.wikipedia.org/wiki/Opportunistic_TLS) i dlatego są preferowane.

Upewnij się, że odbieranie i wysyłanie wiadomości działa poprawnie przed skontaktowaniem się ze mną, aby dodać dostawcę.

Zobacz poniżej jak się ze mną skontaktować.

<br />

<a name="faq43"></a>
**(43) Czy możesz pokazać oryginał ... ?**

Pokaż oryginał, pokazuje oryginalną wiadomość, jak wysłał ją nadawca, w tym oryginalne czcionki, kolory, marginesy itp. FairEmail nie zmienia tego w żaden sposób, z wyjątkiem żądania [TEXT_AUTOSIZING](https://developer.android.com/reference/android/webkit/WebSettings.LayoutAlgorithm), który *spróbuje* uczynić mały tekst bardziej czytelnym.

<br />

<a name="faq44"></a>
**~~(44) Czy możesz pokazać zdjęcia kontaktów / identyfikatory w folderze wysłane?~~**

~~Zdjęcia kontaktu i ikony są zawsze wyświetlane dla nadawcy, ponieważ jest to niezbędne dla wątków konwersacji.~~ ~~Pobieranie zdjęć kontaktowych zarówno dla nadawcy, jak i odbiorcy, nie jest tak naprawdę opcją, ponieważ uzyskanie zdjęcia kontaktu jest kosztowną operacją.~~

<br />

<a name="faq45"></a>
**(45) Jak mogę naprawić 'Ten klucz nie jest dostępny. Aby go użyć, musisz zaimportować go jako swój własny!' ?**

Otrzymasz wiadomość *Ten klucz nie jest dostępny. Aby go użyć, musisz zaimportować go jako własny!* podczas próby odszyfrowania wiadomości za pomocą klucza publicznego. Aby to naprawić, musisz zaimportować klucz prywatny.

<br />

<a name="faq46"></a>
**(46) Dlaczego lista wiadomości ciągle się odświeża?**

Jeśli na górze listy wiadomości znajduje się 'spinner', folder jest nadal synchronizowany ze zdalnym serwerem. Możesz zobaczyć postęp synchronizacji na liście folderów. Zobacz legendę o tym, co oznaczają ikony i liczby.

Szybkość urządzenia i połączenia z internetem oraz liczba dni do synchronizacji wiadomości decyduje jak długo zajmie synchronizacja. Pamiętaj, że w większości przypadków nie powinieneś ustawiać liczby dni do synchronizacji wiadomości na więcej niż jeden dzień, zobacz również [ten FAQ](#user-content-faq39).

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
**(49) Jak mogę naprawić 'Nieaktualna aplikacja wysłała ścieżkę pliku zamiast strumienia plików'?**

Prawdopodobnie wybrałeś lub wysłałeś załącznik lub obrazek z przestarzałego menedżera plików lub nieaktualną aplikacją, która zakłada, że wszystkie aplikacje nadal mają uprawnienia do pamięci. Ze względów bezpieczeństwa i prywatności, nowoczesne aplikacje takie jak FairEmail nie mają już pełnego dostępu do wszystkich plików. Może to skutkować komunikatem o błędzie *Nieaktualna aplikacja wysłała ścieżkę pliku zamiast strumienia pliku* jeśli nazwa pliku zamiast strumienia pliku jest udostępniana FairEmail, ponieważ FairEmail nie może losowo otwierać plików.

Możesz to naprawić, przechodząc na zaktualizowaną wersję menedżera plików lub aplikacji przeznaczonej dla najnowszych wersji Androida. Alternatywnie, możesz przyznać FairEmail dostęp do odczytu miejsca na urządzeniu w ustawieniach aplikacji Android. Zauważ, że to rozwiązanie już [nie zadziała na Androidzie Q](https://developer.android.com/preview/privacy/scoped-storage).

Zobacz również [pytanie 25](#user-content-faq25) i [co pisze Google na ten temat](https://developer.android.com/training/secure-file-sharing/share-file#RespondToRequest).

<br />

<a name="faq50"></a>
**(50) Czy możesz dodać opcję synchronizacji wszystkich wiadomości?**

Synchronizuj wszystkie (pobieraj wszystkie) wiadomości nie zostanie dodane, ponieważ może to z łatwością doprowadzić do błędów typu brak pamięci i zapełnienia dostępnej przestrzeni dyskowej. Może to również z łatwością doprowadzić do dużego zużycia baterii i danych. Urządzenia mobilne po prostu nie nadają się do pobierania i przechowywania wieloletnich wiadomości. Skorzystaj lepiej z funkcji wyszukiwania na serwerze (patrz [pytanie 13](#user-content-faq13)), co jest szybsze i skuteczniejsze. Zauważ, że wyszukiwanie w wielu wiadomościach przechowywanych lokalnie wydłuży wyszukiwanie i zużyje dodatkową energię baterii.

<br />

<a name="faq51"></a>
**(51) Jak sortowane są foldery?**

Foldery są najpierw sortowane według kolejności kont (domyślnie według nazwy konta) oraz w ramach konta ze specjalnymi folderami systemowymi u góry, a następnie folderami synchronizowanymi. W każdej kategorii foldery są sortowane według (wyświetlanej) nazwy. Możesz ustawić wyświetlaną nazwę, naciskając długo folder na liście folderów i wybierając * Edytuj właściwości *.

W menu ustawień (hamburger) *Uporządkuj foldery* może być używane do ręcznego ustawiania kolejności folderów.

<br />

<a name="faq52"></a>
**(52) Dlaczego ponowne połączenie z kontem zajmuje trochę czasu?**

Nie ma niezawodnego sposobu, aby dowiedzieć się, czy połączenie z kontem zostało zakończone prawidłowo czy siłowo. Próba ponownego połączenia z kontem, gdy połączenie z kontem zostało siłowo przerwane, może spowodować problemy takie jak [zbyt wiele jednoczesnych połączeń](#user-content-faq23) lub nawet zablokowanie konta. W celu zapobiegania takim problemom, FairEmail czeka 90 sekund przed ponownym połączeniem.

Możesz przytrzymać przycisk *Ustawienia* w menu nawigacji, aby natychmiast się połączyć ponownie.

<br />

<a name="faq53"></a>
**(53) Czy możesz przykleić pasek akcji wiadomości u góry/dołu?**

Pasek akcji wiadomości działa na pojedynczej wiadomości, a dolny pasek akcji działa na wszystkich wiadomościach w konwersacji. Ponieważ w rozmowie często jest więcej niż jedna wiadomość, nie jest to możliwe. Ponadto istnieją pewne działania specyficzne dla wiadomości, takie jak przesyłanie dalej.

Przenoszenie paska akcji wiadomości na dół wiadomości nie jest wizualnie atrakcyjne, ponieważ istnieje już pasek akcji rozmowy u dołu ekranu.

Pamiętaj, że nie ma wielu, jeśli w ogóle, aplikacji e-mail, które wyświetlają rozmowę jako listę rozwijanych wiadomości. Ma to wiele zalet, ale powoduje również konieczność podjęcia określonych działań.

<br />

<a name="faq54"></a>
**~~(54) How do I use a namespace prefix?~~**

~~A namespace prefix is used to automatically remove the prefix providers sometimes add to folder names.~~

~~Na przykład katalog spam Gmail spam jest nazywany:~~

```
[Gmail]/Spam
```

~~By setting the namespace prefix to *[Gmail]* FairEmail will automatically remove *[Gmail]/* from all folder names.~~

<br />

<a name="faq55"></a>
**(55) Jak mogę oznaczyć wszystkie wiadomości jako przeczytane / przenieść lub usunąć wszystkie wiadomości?**

Możesz użyć do tego wielokrotnego wyboru. Długo naciśnij pierwszą wiadomość, nie podnoś palca i przesuń w dół do ostatniej wiadomości. Następnie użyj przycisku z trzema kropkami, aby wykonać żądane czynności.

<br />

<a name="faq56"></a>
**(56) Czy możesz dodać wsparcie dla JMAP?**

Nie ma prawie żadnych dostawców oferujących protokół [JMAP](https://jmap.io/), więc dodawanie wsparcia do FairEmaila nie jest warte tak dużego wysiłku.

<br />

<a name="faq57"></a>
**(57) ~~Czy mogę użyć HTML w podpisach?~~**

~~Tak, możesz użyć HTML w podpisach, jeśli wkleisz sformatowany tekst w polu podpisu lub użyj menu *Edytuj jako HTML*, aby ręcznie wprowadzić HTML.~~

~~ Pamiętaj, że umieszczenie linków i obrazów w wiadomościach zwiększy prawdopodobieństwo, że wiadomość będzie postrzegana jako spam, ~~ ~~ szczególnie, gdy wysyłasz komuś wiadomość po raz pierwszy. ~~

~~Zobacz [tutaj](https://stackoverflow.com/questions/44410675/supported-html-tags-on-android-textview), które tagi HTML są wspierane.~~

<br />

<a name="faq58"></a>
**(58) Co oznacza otwarta (zarysowana)/zamknięta(pełna) ikona e-mail?**

Ikona e-mail na liście folderów może być otwarta (zarysowana) lub zamknięta (pełna):

![Obraz zewnętrzny](https://raw.githubusercontent.com/google/material-design-icons/master/communication/1x_web/ic_mail_outline_black_48dp.png)

Treść wiadomości i załączniki nie są domyślnie pobierane.

![Obraz zewnętrzny](https://raw.githubusercontent.com/google/material-design-icons/master/communication/1x_web/ic_email_black_48dp.png)

Treść wiadomości i załączniki są pobierane domyślnie.

<br />

<a name="faq59"></a>
**(59) Czy oryginalne wiadomości mogą być otwierane w przeglądarce?**

Ze względów bezpieczeństwa pliki z oryginalnymi treściami wiadomości nie są dostępne dla innych aplikacji, więc nie jest to możliwe. W teorii [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) może być użyta do udostępniania tych plików, ale nawet Google Chrome nie może tego obsłużyć.

<br />

<a name="faq60"></a>
**(60) Czy wiesz, że ... ?**

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
* Czy wiesz, że jeśli wybierzesz tekst i dotkniesz odpowiedz, tylko wybrany tekst zostanie zacytowany?

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

* LOGIN
* PLAIN
* CRAM-MD5
* XOAUTH2 ([Gmail](https://developers.google.com/gmail/imap/xoauth2-protocol), [Yandex](https://tech.yandex.com/oauth/))
* NTLM (untested)

Metody uwierzytelniania SASL, poza CRAM-MD5, nie są obsługiwane ponieważ [JavaMail dla Android](https://javaee.github.io/javamail/Android) nie obsługuje uwierzytelniania SASL.

Jeśli Twój dostawca wymaga nieobsługiwanej metody uwierzytelniania, prawdopodobnie otrzymasz komunikat o błędzie *uwierzytelnianie nie powiodło się*.

[Server Name Indication](https://en.wikipedia.org/wiki/Server_Name_Indication) jest wspierane przez [wszystkie obsługiwane wersje Androida](https://developer.android.com/training/articles/security-ssl).

<br />

<a name="faq63"></a>
**(63) Jak rozmiary obrazków są dostosowywane do wyświetlania na ekranach?**

Duże wbudowane lub dołączone obrazy [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) i [JPEG](https://en.wikipedia.org/wiki/JPEG) zostaną automatycznie dostosowane do wyświetlania na ekranach. Wynika to z faktu, że wiadomości e-mail są ograniczone pod względem wielkości, w zależności od dostawcy w większości jest to od 10 do 50 MB. Obrazki będą domyślnie przeskalowane do maksymalnej szerokości i wysokości około 1440 pikseli i zapisane przy współczynniku kompresji 90 %. Obrazy są zmniejszane przy użyciu współczynników liczby całkowitej, aby zmniejszyć zużycie pamięci i zachować jakość obrazu. Automatyczna zmiana rozmiaru wstawionych i/lub załączonych obrazów oraz maksymalny rozmiar docelowego obrazu można skonfigurować w ustawieniach wysyłania.

Jeśli chcesz zmieniać rozmiar obrazów indywidualnie, możesz użyć [Send Reduced](https://f-droid.org/en/packages/mobi.omegacentauri.SendReduced/) lub podobnej aplikacji.

<br />

<a name="faq64"></a>
**~~(64) Czy możesz dodać własne akcje dla przesunięcia w lewo/prawo?~~**

~~Najbardziej naturalną rzeczą do zrobienia podczas przesuwania wpisu w lewo lub w prawo, jest usunięcie wpisu z listy.~~ ~~Najbardziej naturalną akcją w kontekście aplikacji e-mail jest przenoszenie wiadomości z folderu do innego folderu.~~ ~~Możesz wybrać folder, do którego chcesz przenieść w ustawieniach konta.~~

~~Inne działania, takie jak oznaczanie wiadomości przeczytanych i uśpionych są dostępne za pomocą wielokrotnego wyboru.~~ ~~Możesz długo nacisnąć wiadomość, aby rozpocząć wielokrotny wybór. Zobacz również [to pytanie](#user-content-faq55).~~

~~Przesuwanie w lewo lub w prawo, aby oznaczyć wiadomość przeczytaną lub nieprzeczytaną, jest nienaturalne, ponieważ wiadomość najpierw wychodzi, a później wraca w innym stanie.~~ ~~Pamiętaj, że istnieje zaawansowana opcja automatycznego oznaczania wiadomości podczas przenoszenia,~~ ~~który w większości przypadków jest idealnym zamiennikiem sekwencji odczytania i przeniesienia do jakiegoś folderu.~~ ~~Możesz również oznaczyć wiadomości przeczytane z powiadomień o nowych wiadomościach.~~

~~Jeśli chcesz przeczytać wiadomość później, możesz ją ukryć do określonego czasu używając menu *odłóż*.~~

<br />

<a name="faq65"></a>
**(65) Dlaczego niektóre załączniki są przyciemnione?**

Wbudowane załączniki (obrazy) są przyciemnione. [Inline attachments](https://tools.ietf.org/html/rfc2183) are supposed to be downloaded and shown automatically, but since FairEmail doesn't always download attachments automatically, see also [this FAQ](#user-content-faq40), FairEmail shows all attachment types. Aby rozróżnić załączniki wstawione i zwykłe, załączniki wstawione są wyświetlane jako przyciemnione.

<br />

<a name="faq66"></a>
**(66) Czy FairEmail jest dostępny w bibliotece rodzinnej Google Play?**

Cena FairEmail jest zbyt niska, niższa niż w większości podobnych aplikacji, i jest [zbyt wiele opłat i podatków](#user-content-faq19), sam Google już pobiera 30%, aby istniało uzasadnienie udostępnienia FairEmail w [bibliotece rodzinej Google Play](https://support.google.com/googleone/answer/7007852).

<br />

<a name="faq67"></a>
**(67) Jak mogę uśpić/odłożyć rozmowy?**

Wybierz jedną lub więcej konwersacji (naciśnij długo, aby rozpocząć wielokrotne wybieranie), dotknij przycisku z trzema kropkami i wybierz *Odłóż ...*. Alternatywnie, w rozwiniętym widoku wiadomości użyj *Odłóż ...* w menu trzech kropek 'więcej' lub akcji timelapse na dolnym pasku akcji. Wybierz czas, w którym konwersacja(e) mają być odłożone i potwierdź naciskając OK. Rozmowy zostaną ukryte przez wybrany czas i pojawią się ponownie po tym czasie. Otrzymasz powiadomienie o nowej wiadomości jako przypomnienie.

Można również użyć [reguły](#user-content-faq71), aby odłożyć wiadomości.

Możesz wyświetlić odłożone wiadomości, naciskając opcję *Odfiltruj* > *Ukryte* w menu z trzema kropkami.

Możesz dotknąć małą ikonę odłożenia, aby zobaczyć, do kiedy rozmowa została odłożona.

Wybierając zerowy okres odłożenia możesz anulować odłożenie.

<br />

<a name="faq68"></a>
**~~(68) Dlaczego Adobe Acrobat nie może otwierać załączników PDF / aplikacje Microsoft nie mogą otwierać załączonych dokumentów?~~**

~~Adobe Acrobat reader i aplikacje Microsoft nadal oczekują pełnego dostępu do wszystkich przechowywanych plików, ~~ ~~kiedy to aplikacje powinny używać [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) od Android KitKat (2013)~~, ~~aby mieć dostęp tylko do aktywnie udostępnianych plików. To jest z powodów prywatności i bezpieczeństwa.~~

~~Możesz to obejść zapisując załącznik i otwierając go w Adobe Acrobat reader / aplikacji Microsoft, ~~ ~~ale zaleca się zainstalowanie aktualnego i najlepiej otwartego czytnika PDF / czytnika dokumentów, ~~ ~~na przykład jeden z wymienionych [tutaj](https://github.com/offa/android-foss#-document--pdf-viewer).~~

<br />

<a name="faq69"></a>
**(69) Czy możesz dodać automatyczne przewijanie w górę przy nadejściu nowej wiadomości?**

Lista wiadomości jest automatycznie przewijana do góry podczas nawigacji z powiadomienia o nowej wiadomości lub po ręcznym odświeżeniu. Stale automatycznie przewijanie w górę po pojawieniu się nowych wiadomości będzie kolidowało z Twoim własnym przewijaniem, ale jeśli chcesz, możesz to włączyć w ustawieniach.

<br />

<a name="faq70"></a>
**(70) Kiedy wiadomości będą automatycznie rozwijane?**

Podczas nawigacji do rozmowy jedna wiadomość zostanie rozwinięta, jeśli:

* W konwersacji jest tylko jedna wiadomość
* W rozmowie jest dokładnie jedna nieprzeczytana wiadomość

Istnieje jeden wyjątek: wiadomość nie została jeszcze pobrana i wiadomość jest zbyt duża, aby pobrać automatycznie za pomocą połączenia taryfowego (komórkowego). Możesz ustawić lub wyłączyć maksymalny rozmiar wiadomości w ustawieniach połączenia.

Zduplikowane (zarchiwizowane) wiadomości, wiadomości w koszu i szkice wiadomości nie są liczone.

Wiadomości będą automatycznie oznaczane jako przeczytane po rozwinięciu, chyba że zostało to wyłączone w indywidualnych ustawieniach konta.

<br />

<a name="faq71"></a>
**(71) Jak stosować zasady filtrowania?**

Możesz edytować reguły filtrowania przez długie naciśnięcie folderu na liście folderów.

Nowe reguły zostaną zastosowane dla nowych wiadomości otrzymanych w folderze, a nie do już istniejących wiadomości. Możesz sprawdzić regułę i zastosować regułę do istniejących wiadomości lub, alternatywnie, przytrzymaj regułę na liście reguł i wybierz *Wykonaj teraz*.

Musisz nadać nazwę regule i musisz zdefiniować kolejność, w której reguła powinna być wykonywana w stosunku do innych reguł.

Możesz wyłączyć regułę i zatrzymać przetwarzanie innych reguł po wykonaniu reguły.

Dostępne są następujące warunki reguły:

* Nadawca zawiera
* Odbiorca zawiera
* Temat zawiera
* Zawiera załączniki
* Nagłówek zawiera
* Dzień/czas pomiędzy

Wszystkie warunki reguły muszą być spełnione, aby akcja reguły mogła zostać wykonana. Wszystkie warunki są opcjonalne, ale musi być co najmniej jeden warunek, aby zapobiec dopasowaniu do wszystkich wiadomości. Jeśli chcesz dopasować wszystkich nadawców lub wszystkich odbiorców, możesz użyć znaku @ jako warunku, ponieważ każdy adres e-mail będzie zawierał ten znak.

Możesz użyć wielu reguł, prawdopodobnie z *zatrzymaniem przetwarzania*, dla warunku *lub* albo *nie*.

Dopasowanie nie uwzględnia wielkości liter, chyba że użyjesz [wyrażeń regularnych](https://en.wikipedia.org/wiki/Regular_expression). Proszę zobacz [tutaj](https://developer.android.com/reference/java/util/regex/Pattern) dokumentację regularnych wyrażeń Java. Możesz przetestować regex [tutaj](https://regexr.com/).

Note that [dot all mode](https://developer.android.com/reference/java/util/regex/Pattern#DOTALL) is enabled to be able to match [unfolded headers](https://tools.ietf.org/html/rfc2822#section-3.2.3).

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
* Odpowiedź (z szablonu)
* Tekst na mowę (nadawca i temat)
* Automatyzacja (Tasker, itp.)

Reguły są stosowane bezpośrednio po pobraniu nagłówka wiadomości, ale przed pobraniem tekstu wiadomości, dlatego nie można zastosować warunków do tekstu wiadomości. Zauważ, że duże wiadomości są pobierane na żądanie przy połączeniu taryfowym, aby oszczędzić użycie danych.

Jeśli chcesz przesłać dalej wiadomość, rozważ użycie akcji przenoszenia. Będzie to bardziej wiarygodne niż przesyłanie dalej wiadomości, ponieważ przesłane dalej wiadomości mogą być uznane za spam.

Ponieważ nagłówki wiadomości nie są pobierane i przechowywane domyślnie, aby zaoszczędzić baterię, dane oraz miejsce na dysku nie jest możliwy podgląd, które wiadomości odpowiadałyby warunkowi reguły nagłówka.

W wiadomości, w menu z trzema kropkami znajduje się element do utworzenia reguły *Utwórz regułę* dla odebranej wiadomości z wypełnionymi najczęstszymi warunkami.

Protokół POP3 nie obsługuje ustawiania słów kluczowych oraz przenoszenia lub kopiowania wiadomości.

Używanie reguł jest funkcją pro.

<br />

<a name="faq72"></a>
**(72) Czym są podstawowe konta/tożsamości?**

Konto podstawowe jest używane, gdy konto nie jest jednoznacznie określone, na przykład podczas uruchamiania nowego szkicu ze zunifikowanej skrzynki odbiorczej.

Podobnie, główna tożsamość konta jest używana, gdy tożsamość nie jest jednoznacznie określona.

Może być tylko jedno konto podstawowe i może być tylko jedna podstawowa tożsamość na konto.

<br />

<a name="faq73"></a>
**(73) Czy przenoszenie wiadomości pomiędzy kontami jest bezpieczne/wydajne?**

Przenoszenie wiadomości pomiędzy kontami jest bezpieczne, ponieważ oryginalne wiadomości zostaną pobrane i przeniesione oraz ponieważ wiadomości źródłowe zostaną usunięte dopiero po dodaniu wiadomości w miejscu docelowym

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

Menu folderu *Wyczyść wiadomości lokalne* usuwa wiadomości z urządzenia, które są również obecne na serwerze. To nie usuwa wiadomości z serwera. Może to być przydatne po zmianie ustawień folderu, aby nie pobierać treści wiadomości (tekstu i załączników), na przykład w celu zaoszczędzenia miejsca.

<br />

<a name="faq77"></a>
**(77) Dlaczego wiadomości są czasami wyświetlane z małym opóźnieniem?**

W zależności od prędkości urządzenia (szybkości procesora, a może nawet bardziej prędkości pamięci) wiadomości mogą być wyświetlane z niewielkim opóźnieniem. FairEmail jest zaprojektowany do dynamicznej obsługi dużej liczby wiadomości bez wyczerpywania pamięci. Oznacza to, że wiadomości muszą być odczytywane z bazy danych i ta baza danych musi być monitorowana pod kątem zmian, oba działania mogą spowodować niewielkie opóźnienia.

Niektóre wygodne funkcje, takie jak grupowanie wiadomości do wyświetlania wątków konwersacji i określania poprzedniej/następnej wiadomości, wymagają trochę dodatkowego czasu. Note that there is no *the* next message because in the meantime a new message might have been arrived.

Porównując prędkość FairEmail z podobnymi aplikacjami, powinno to być częścią porównania. Łatwo jest napisać podobną, szybszą aplikację, która tylko wyświetla listę wiadomości używając zbyt dużej ilości pamięci, ale nie jest to łatwe do prawidłowego zarządzania zasobami i oferowania bardziej zaawansowanych funkcji, takich jak wątek konwersacji.

FairEmail opiera się na najnowocześniejszych [komponentach architektury Androida](https://developer.android.com/topic/libraries/architecture/), więc jest mało miejsca na poprawę wydajności.

<br />

<a name="faq78"></a>
**(78) Jak korzystać z harmonogramów?**

W ustawieniach odbierania możesz włączyć planowanie i ustawić okres czasu oraz dni tygodnia, kiedy wiadomości powinny być odbierane.

Note that an end time equal to or earlier than the start time is considered to be 24 hours later.

For more complex schemes you could set one or more accounts to manual synchronization and send this command to FairEmail to check for new messages:

```
(adb shell) am startservice -a eu.faircode.email.POLL
```

Dla konkretnego konta:

```
(adb shell) am startservice -a eu.faircode.email.POLL --es account Gmail
```

You can also automate turning receiving messages on and off by sending these commands to FairEmail:

```
(adb shell) am startservice -a eu.faircode.email.ENABLE
(adb shell) am startservice -a eu.faircode.email.DISABLE
```

Aby włączyć/wyłączyć określone konto:

```
(adb shell) am startservice -a eu.faircode.email.ENABLE --es account Gmail
(adb shell) am startservice -a eu.faircode.email.DISABLE --es account Gmail
```

Pamiętaj, że wyłączenie konta spowoduje ukrycie konta oraz wszystkich powiązanych folderów i wiadomości.

Możesz automatycznie wysyłać polecenia korzystając z, np. [Tasker](https://tasker.joaoapps.com/userguide/en/intents.html):

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

Automatyzacja może zostać użyta do bardziej zaawansowanych harmonogramów, jak na przykład wiele okresów synchronizacji dziennie lub różne okresy synchronizacji dla różnych dni.

Możliwe jest zainstalowanie FairEmail w wielu profilach użytkowników, na przykład profil osobisty i służbowy, oraz skonfigurowanie FairEmail inaczej w każdym profilu, co jest kolejną możliwością posiadania różnych harmonogramów synchronizacji i synchronizacji innego zestawu kont.

Można również utworzyć [reguły](#user-content-faq71) z warunkiem czasu i odkładaniem wiadomości do końca warunku. W ten sposób można odłożyć wiadomości związane z pracą do początku godzin pracy. Oznacza to również, że wiadomości będą na twoim urządzeniu, gdy nie będzie połączenia z Internetem, na przykład podczas lotu.


Harmonogram jest funkcją pro.

<br />

<a name="faq79"></a>
**(79) Jak korzystać z synchronizacji na żądanie (ręcznej)?**

Zazwyczaj, gdy tylko jest to możliwe, FairEmail utrzymuje połączenie ze skonfigurowanymi serwerami e-mail, aby odbierać wiadomości w czasie rzeczywistym. Jeśli tego nie chcesz, aby nie przeszkadzać lub na przykład, aby oszczędzać baterię, po prostu wyłącz odbiór w ustawieniach odbierania. To zatrzyma usługę w tle, która zajmuje się automatyczną synchronizacją i usunie powiązane powiadomienie na pasku statusu.

Możesz również włączyć *Synchronizuj ręcznie* w zaawansowanych ustawieniach konta, jeśli chcesz ręcznie synchronizować tylko określone konta.

Możesz użyć przeciągnięcia w dół na liście wiadomości, aby odświeżyć lub użyć menu folderu *Synchronizuj teraz*, aby ręcznie zsynchronizować wiadomości.

Jeśli chcesz ręcznie synchronizować niektóre lub wszystkie foldery konta, po prostu wyłącz synchronizację dla folderów (ale nie dla konta).

Prawdopodobnie chcesz wyłączyć również [przeglądaj na serwerze](#user-content-faq24).

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

Zobacz [tutaj](https://en.wikipedia.org/wiki/Web_beacon) informacje o tym, czym dokładnie jest obraz śledzenia. Obrazy śledzące sprawdzają czy otworzyłeś wiadomość.

FairEmail w większości przypadków automatycznie rozpozna obrazy śledzące i zastąpi je tą ikoną:

![Obraz zewnętrzny](https://raw.githubusercontent.com/google/material-design-icons/master/maps/1x_web/ic_my_location_black_48dp.png)

Automatyczne rozpoznawanie obrazów śledzących może być wyłączone w ustawieniach prywatności.

<br />

<a name="faq84"></a>
**(84) Do czego służą kontakty lokalne?**

Local contact information is based on names and addresses found in incoming and outgoing messages.

The main use of the local contacts storage is to offer auto completion when no contacts permission has been granted to FairEmail.

Another use is to generate [shortcuts](#user-content-faq31) on recent Android versions to quickly send a message to frequently contacted people. This is also why the number of times contacted and the last time contacted is being recorded and why you can make a contact a favorite or exclude it from favorites by long pressing it.

The list of contacts is sorted on number of times contacted and the last time contacted.

By default only names and addresses to whom you send messages to will be recorded. Możesz to zmienić w ustawieniach wysyłania.

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

Jeśli hasło jest niepoprawne/wygasło, musisz zaktualizować hasło w ustawieniach konta i/lub ustawieniach tożsamości.

Jeśli autoryzacja konta wygasła, musisz ponownie wybrać konto. You will likely need to save the associated identity again as well.

<br />

<a name="faq88"></a>
**(88) Jak mogę korzystać z konta Yahoo, AOL lub Sky?**

Aby autoryzować konto Yahoo, AOL lub Sky, musisz utworzyć hasło aplikacji. Instrukcje znajdują się tutaj:

* [dla Yahoo](https://help.yahoo.com/kb/generate-third-party-passwords-sln15241.html)
* [dla AOL](https://help.aol.com/articles/Create-and-manage-app-password)
* [dla Sky](https://www.sky.com/help/articles/getting-started-with-sky-yahoo-mail) (w *Inne aplikacje e-mail*)

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

Domyślnie FairEmail wysyła każdą wiadomość zarówno jako zwykły tekst, jak i jako tekst sformatowany HTML, ponieważ prawie każdy odbiorca oczekuje obecnie sformatowanych wiadomości. Jeśli chcesz/chcesz wysyłać tylko wiadomości tekstowe, możesz to włączyć w zaawansowanych opcjach tożsamości. Możesz chcieć utworzyć nową tożsamość do tego celu, jeśli chcesz/potrzebujesz wybrać wysyłanie zwykłych wiadomości tekstowych dla poszczególnych przypadków.

<br />

<a name="faq90"></a>
**(90) Dlaczego niektóre teksty wyświetlają się jako linki, podczas gdy nie są linkami?**

Dla Twojej wygody FairEmail automatycznie połączy niepowiązane linki internetowe (http i https) i niepowiązane adresy e-mail (mailto). Jednak teksty i linki nie są łatwe do odróżnienia, zwłaszcza gdy wiele [domen najwyższego poziomu](https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains) to słowa. Dlatego też teksty z kropkami są czasami niepoprawnie uznawane za linki, co jest lepsze niż brak rozpoznawania niektórych linków.

Łącza do mniej popularnych protokołów, takich jak telnet i ftp, nie będą automatycznie łączone.

<br />

<a name="faq91"></a>
**~~(91) Czy możesz dodać okresową synchronizację w celu oszczędzania energii baterii?~~**

~~Synchronizowanie wiadomości jest kosztownym procesem, ponieważ lokalne i zdalne wiadomości muszą być porównane,~~ ~~~więc okresowa synchronizacja wiadomości nie spowoduje oszczędności energii baterii, co bardziej prawdopodobne wręcz przeciwnie.~~

~~Zobacz [ten FAQ](#user-content-faq39) o optymalizacji użycia baterii.~~


<br />

<a name="faq92"></a>
**(92) Czy możesz dodać filtrowanie spamu, weryfikację podpisu DKIM i autoryzację SPF?**

Filtrowanie spamu, weryfikacja podpisu [DKIM](https://en.wikipedia.org/wiki/DomainKeys_Identified_Mail) i autoryzacja [SPF](https://en.wikipedia.org/wiki/Sender_Policy_Framework) jest zadaniem serwerów e-mail, a nie klienta e-mail. Serwery mają zazwyczaj więcej pamięci i mocy obliczeniowej, więc są znacznie lepiej dostosowane do tego zadania niż urządzenia zasilane baterią. Ponadto chcesz, aby spam był filtrowany dla wszystkich klientów poczty e-mail, prawdopodobnie w tym poczty internetowej, a nie tylko jednego klienta poczty e-mail. Ponadto serwery poczty elektronicznej mają dostęp do informacji, takich jak adres IP, itp. serwera łączącego, do których klient poczty elektronicznej nie ma dostępu.

Oczywiście możesz zgłaszać w FairEmail wiadomości jako spam, który przeniesie zgłoszone wiadomości do folderu spam i wyszkoli metodę, w jaki ma funkcjonować filtr spamu dostawcy. Można to zrobić również automatycznie z [zasadami filtrowania](#user-content-faq71).

FairEmail może również pokazać mały czerwony znacznik ostrzegawczy gdy DKIM, SPF lub uwierzytelnianie [DMARC](https://en.wikipedia.org/wiki/DMARC) nie powiodło się na serwerze odbierającym. Możesz włączyć/wyłączyć [weryfikację uwierzytelniania](https://en.wikipedia.org/wiki/Email_authentication) w ustawieniach wyświetlania.

FairEmail can show a warning flag too if the domain name of the (reply) email address of the sender does not define an MX record pointing to an email server. Ta opcja może być włączona w ustawieniach odbierania. Pamiętaj, że to znacznie spowolni synchronizację wiadomości.

If legitimate messages are failing authentication, you should notify the sender because this will result in a high risk of messages ending up in the spam folder. Moreover, without proper authentication there is a risk the sender will be impersonated. The sender might use [this tool](https://www.mail-tester.com/) to check authentication and other things.

<br />

<a name="faq93"></a>
**(93) Can you allow installation/data storage on external storage media (sdcard)?**

FairEmail uses services and alarms, provides widgets and listens for the boot completed event to be started on device start, so it is not possible to store the app on external storage media, like an sdcard. Zobacz również [tutaj](https://developer.android.com/guide/topics/data/install-location).

Wiadomości, załączniki, itp., przechowywane na zewnętrznych nośnikach, jak karta pamięci, mogą być dostępne dla innych aplikacji i dlatego nie są bezpieczne. Szczegóły znajdziesz w [tutaj](https://developer.android.com/training/data-storage).

When needed you can save (raw) messages via the three-dots menu just above the message text and save attachments by tapping on the floppy icon.

If you need to save on storage space, you can limit the number of days messages are being synchronized and kept for. You can change these settings by long pressing a folder in the folder list and selecting *Edit properties*.

<br />

<a name="faq94"></a>
**(94) Co oznacza czerwony/pomarańczowy pasek na końcu nagłówka?**

Czerwony/pomarańczowy pasek po lewej stronie nagłówka oznacza, że uwierzytelnienie DKIM, SPF lub DMARC nie powiodło się. Zobacz również [ten FAQ](#user-content-faq92).

<br />

<a name="faq95"></a>
**(95) Dlaczego nie wszystkie aplikacje są wyświetlane przy wyborze załącznika lub obrazu?**

Ze względów prywatności i bezpieczeństwa FairEmail nie ma uprawnień do bezpośredniego dostępu do plików, zamiast tego używa Storage Access Framework do dostępu do pamięci, jest to dostępne i rekomendowane od Androida 4.4 KitKat (wydany w 2013 r.) i jest to używane do wybierania plików.

To, czy aplikacja znajduje się na liście, zależy od tego, czy aplikacja implementuje [dostawcę dokumentów](https://developer.android.com/guide/topics/providers/document-provider). Jeśli aplikacja nie jest na liście, być może będziesz musiał poprosić twórcę aplikacji o dodanie wsparcia dla Storage Access Framework.

Android Q utrudni i może nawet uniemożliwi bezpośredni dostęp do plików, zobacz [tutaj](https://developer.android.com/preview/privacy/scoped-storage) i [tutaj](https://www.xda-developers.com/android-q-storage-access-framework-scoped-storage/), aby uzyskać więcej informacji.

<br />

<a name="faq96"></a>
**(96) Gdzie mogę znaleźć ustawienia IMAP i SMTP?**

Ustawienia IMAP są częścią (własnych) ustawień konta, a ustawienia SMTP są częścią ustawień identyfikacyjnych.

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

Po cofnięciu uprawnień do kontaktów Android nie zezwala na dostęp FairEmail do Twoich kontaktów. Jednakże wybieranie kontaktów jest delegowane i wykonywane przez Androida, a nie przez FairEmail, więc będzie to nadal możliwe bez uprawnień do kontaktów.

<br />

<a name="faq99"></a>
**(99) Can you add a rich text or markdown editor?**

FairEmail zapewnia ogólne formatowanie tekstu (pogrubienie, kursywa, podkreślenie, rozmiar tekstu i kolor) przez pasek narzędzi, który pojawia się po wybraniu jakiegoś tekstu.

A [Rich text](https://en.wikipedia.org/wiki/Formatted_text) or [Markdown](https://en.wikipedia.org/wiki/Markdown) editor would not be used by many people on a small mobile device and, more important, Android doesn't support a rich text editor and most rich text editor open source projects are abandoned. Zobacz [tutaj](https://forum.xda-developers.com/showpost.php?p=79061829&postcount=4919), aby uzyskać więcej informacji na ten temat.

<br />

<a name="faq100"></a>
**(100) Jak mogę zsynchronizować kategorie Gmail?**

Możesz zsynchronizować kategorie Gmail, tworząc filtry do oznaczania wiadomości skategoryzowanych:

* Utwórz nowy filtr za pośrednictwem Gmaila > Ustawienia (koło) > Filtry i zablokowane adresy > Utwórz nowy filtr
* Wpisz wyszukiwanie kategorii (patrz poniżej) w polu * Zawiera słowa * i kliknij * Utwórz filtr *
* Zaznacz * Zastosuj etykietę *, wybierz etykietę i kliknij * Utwórz filtr *

Możliwe kategorie:

```
kategoria: społeczna
kategoria: aktualizacje
kategoria: fora
kategoria: promocje
```

Niestety nie jest to możliwe w przypadku folderu odłożonych wiadomości.

You can use *Force sync* in the three-dots menu of the unified inbox to let FairEmail synchronize the folder list again and you can long press the folders to enable synchronization.

<br />

<a name="faq101"></a>
**(101) Co oznacza niebiesko-pomarańczowa kropka na dole rozmowy?**

Kropka pokazuje względne położenie rozmowy na liście wiadomości. Kropka będzie wyświetlana na pomarańczowo, gdy rozmowa będzie pierwszą lub ostatnią na liście wiadomości, w przeciwnym razie będzie niebieska. Kropka ma służyć jako pomoc przy przesuwaniu w lewo/w prawo, aby przejść do poprzedniej/następnej rozmowy.

Kropka jest domyślnie wyłączona i może być włączona w ustawieniach wyświetlania *Pokaż względną pozycję rozmowy za pomocą kropki*.

<br />

<a name="faq102"></a>
**(102) Jak mogę włączyć automatyczne obracanie obrazów?**

Obrazy będą automatycznie obracane po włączeniu automatycznej zmiany rozmiaru obrazów w ustawieniach (domyślnie włączone). Jednakże automatyczna rotacja zależy od danych [Exif](https://en.wikipedia.org/wiki/Exif), które będą obecne i będą poprawne, co nie zawsze ma miejsce. Szczególnie nie, przy robieniu zdjęć za pomocą aplikacji aparatu z FairEmail.

Zauważ, że tylko obrazy [JPEG](https://en.wikipedia.org/wiki/JPEG) i [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) mogą zawierać dane Exif.

<br />

<a name="faq103"></a>
**(103) Jak mogę nagrywać dźwięk?**

Możesz nagrywać dźwięk, jeśli masz zainstalowaną aplikację do nagrywania która wspiera [RECORD_SOUND_ACTION](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media#RECORD_SOUND_ACTION). Jeśli nie ma zainstalowanej żadnej obsługiwanej aplikacji, FairEmail nie wyświetli akcji/ikony nagrywania.

Niestety i zaskakujące jest, że większość aplikacji do nagrywania tego nie wspiera (powinny).

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

Nie musisz więc wyłączać tej opcji, jeśli nie masz karty SIM UE lub nie jesteś podłączony do sieci UE.

<br />

<a name="faq106"></a>
**(106) Które launchery mogą pokazać licznik z liczbą nieprzeczytanych wiadomości?**

[Zobacz tutaj](https://github.com/leolin310148/ShortcutBadger#supported-launchers), aby wyświetlić listę launcherów, które mogą pokazać liczbę nieprzeczytanych wiadomości.

Pamiętaj, że ustawienia powiadomień *Pokaż ikonę launchera z liczbą nowych wiadomości* muszą być włączone (domyślnie włączone).

Only *new* unread messages in folders set to show new message notifications will be counted, so messages marked unread again and messages in folders set to not show new message notification will not be counted.

W zależności od tego, co chcesz, ustawienie powiadomień *Niech liczba nowych wiadomości odpowiada liczbie powiadomień* musi być włączona lub wyłączona.

Ta funkcja zależy od wsparcia Twojego launchera. FairEmail tylko 'transmituje' liczbę nieprzeczytanych wiadomości za pomocą biblioteki ShortcutBadger. Jeśli to nie działa, nie może to zostać naprawione przez zmiany w FairEmail.

Some launchers display '1' for [the monitoring notification](#user-content-faq2), despite FairEmail explicitly requesting not to show a badge for this notification. This could be caused by a bug in the launcher app or in your Android version. Please double check if the notification dot is disabled for the receive (service) notification channel. You can go to the right notification channel settings via the notification settings of FairEmail. This might not be obvious, but you can tap on the channel name for more settings.

Zauważ, że Tesla Unread [nie jest już obsługiwana](https://forum.xda-developers.com/android/general/bad-news-tesla-unread-devoloper-t3920415).

FairEmail does send a new message count intent as well:

```
eu.faircode.email.NEW_MESSAGE_COUNT
```

Liczba nowych, nieprzeczytanych wiadomości będzie liczbą całkowitą w parametrze "*count*".

<br />

<a name="faq107"></a>
**(107) Jak używać kolorowych gwiazdek?**

Możesz ustawić kolorową gwiazdkę za pomocą menu wiadomości *więcej* poprzez wybór wielu (rozpoczęty przez długie naciśnięcie wiadomości), przez długie naciśnięcie gwiazdy w konwersacji lub automatyczne użycie [reguły](#user-content-faq71).

Musisz wiedzieć, że kolorowe gwiazdki nie są obsługiwane przez protokół IMAP i dlatego nie mogą być zsynchronizowane z serwerem e-mail. Oznacza to, że kolorowe gwiazdy nie będą widoczne w innych klientach poczty elektronicznej i zostaną utracone podczas ponownego pobierania wiadomości. Jednakże gwiazdy (bez koloru) będą synchronizowane i będą widoczne w innych klientach e-mail, jeśli są obsługiwane.

Niektórzy klienci poczty elektronicznej używają słów kluczowych IMAP dla kolorów. Jednak nie wszystkie serwery obsługują słowa kluczowe IMAP i poza tym nie ma standardowych słów kluczowych dla kolorów.

<br />

<a name="faq108"></a>
**~~(108) Czy możesz dodać trwałe usuwanie wiadomości z dowolnego folderu?~~**

~~Kiedy usuwasz wiadomości z folderu zostaną one przeniesione do folderu kosza, więc masz szansę przywrócić wiadomości.~~ ~~Możesz trwale usunąć wiadomości z folderu kosza.~~ ~~Trwałe usuwanie wiadomości z innych folderów zniszczyłoby cel folderu kosza, więc nie zostanie to dodane.~~

<br />

<a name="faq109"></a>
**~~(109) Dlaczego funkcja 'wybierz konto' jest dostępna tylko w oficjalnych wersjach?~~**

~~ Korzystanie z *wybierz konto* do wyboru i autoryzacji kont Google wymaga specjalnego zezwolenia od Google ze względów bezpieczeństwa i prywatności. ~~ ~~ To specjalne uprawnienie można uzyskać tylko dla aplikacji, którymi programista zarządza i za które jest odpowiedzialny. ~~ ~~ Kompilacje stron trzecich, podobnie jak kompilacje F-Droid, są zarządzane przez strony trzecie i są odpowiedzialne za te strony trzecie. ~~ ~~ Tak więc tylko te osoby trzecie mogą uzyskać wymagane pozwolenie od Google. ~~ ~~ Ponieważ te strony trzecie faktycznie nie obsługują FairEmail, najprawdopodobniej nie będą żądać wymaganego pozwolenia. ~~

~~Możesz to rozwiązać na dwa sposoby:~~

* ~~ Przejdź do oficjalnej wersji FairEmail, sprawdź [ tutaj ](https://github.com/M66B/FairEmail/blob/master/README.md#downloads), aby zobaczyć opcje ~~
* ~~ Używaj haseł specyficznych dla aplikacji, zobacz [ to FAQ ](#user-content-faq6)~~

~~Korzystanie z *wybierz konto* w kompilacjach innych firm nie jest już możliwe w najnowszych wersjach.~~ ~~W starszych wersjach było to możliwe, ale spowoduje to błąd *UNREGISTERED_ON_API_CONSOLE*.~~

<br />

<a name="faq110"></a>
**(110) Dlaczego (niektóre) wiadomości są puste i/lub załączniki są uszkodzone?**

Puste wiadomości i/lub uszkodzone załączniki są prawdopodobnie spowodowane błędem w oprogramowaniu serwera. Wiadomo, że starsze oprogramowanie Microsoft Exchange powoduje ten problem. W większości możesz to zrobić wyłączając *Pobieranie częściowe* w zaawansowanych ustawieniach konta:

Ustawienia > Krok 1 > Zarządzaj > Dotknij konta > Dotknij zaawansowane > Pobieranie częściowe > odznacz

Po wyłączeniu tego ustawienia możesz użyć menu „więcej” wiadomości (trzy kropki), aby „zsynchronizować” puste wiadomości. Alternatywnie, możesz *Usunąć wiadomości lokalne * przez długie naciśnięcie folderu(ów) na liście folderów i zsynchronizować wszystkie wiadomości ponownie.

Wyłączenie *Pobieranie częściowe* spowoduje większe użycie pamięci.

<br />

<a name="faq111"></a>
**(111) Czy OAuth jest wspierane?**

OAuth dla Gmail jest obsługiwany za pomocą kreatora szybkich ustawień. Menedżer kont Android będzie używany do pobierania i odświeżania tokenów OAuth dla wybranych kont na urządzeniu. OAuth dla kont innych niż na urządzeniu nie jest obsługiwany, ponieważ Google wymaga w tym celu [rocznego audytu bezpieczeństwa](https://support.google.com/cloud/answer/9110914) (15,000 do 75,000 USD).

OAuth dla Yandex jest wspierany przy użyciu kreatora szybkich ustawień.

OAuth dla Office365 konta jest wspierane, ale Microsoft nie oferuje OAuth dla kont Outlook, Live i Hotmail (jeszcze?).

Zażądano dostępu OAuth dla Yahoo, ale Yahoo nigdy nie odpowiedziało na to żądanie. OAuth dla AOL [zostało dezaktywowane](https://www.programmableweb.com/api/aol-open-auth) przez AOL. Verizon jest właścicielem zarówno AOL, jak i Yahoo!, pod wspólną nazwą [Oath inc](https://en.wikipedia.org/wiki/Verizon_Media). Można zatem założyć, że OAuth również nie jest już wspierane przez Yahoo.

<br />

<a name="faq112"></a>
**(112) Którego dostawcę poczty elektronicznej polecasz?**

Dostawca poczty e-mail, który jest dla Ciebie najlepszy, zależy od Twoich życzeń/wymagań. Odwiedź strony internetowe [Restore privacy](https://restoreprivacy.com/secure-email/) lub [Privacy Tools](https://www.privacytools.io/providers/email/) i sprawdź listę dostawców e-mail zorientowanych na prywatność z zaletami i wadami.

Pamiętaj, że nie wszyscy dostawcy obsługują standardowe protokoły e-mail, zobacz [ten FAQ](#user-content-faq129), aby uzyskać więcej informacji.

Using your own (custom) domain name, which is supported by most email providers, will make it easier to switch to another email provider.

<br />

<a name="faq113"></a>
**(113) Jak działa uwierzytelnianie biometryczne?**

Jeśli urządzenie posiada czujnik biometryczny, na przykład czujnik linii papilarnych, możesz włączyć/wyłączyć uwierzytelnianie biometryczne w menu nawigacyjnym (hamburger) ekranu konfiguracji. Po włączeniu FairEmail będzie wymagał uwierzytelniania biometrycznego po okresie bezczynności lub po wyłączeniu ekranu podczas działania FairEmail. Aktywnością jest nawigacja w FairEmail, na przykład otwieranie wątku konwersacji. Czas trwania okresu braku aktywności może być skonfigurowany w ustawieniach różnych. Gdy uwierzytelnianie biometryczne jest włączone, nowe powiadomienia nie będą wyświetlać żadnych treści, a FairEmail nie będzie widoczny na ekranie ostatnich aplikacji Android.

Uwierzytelnianie biometryczne ma na celu uniemożliwienie innym wyświetlania Twoich wiadomości. FairEmail opiera się na szyfrowaniu danych na urządzeniu, zobacz również [ten FAQ](#user-content-faq37).

Uwierzytelnianie biometryczne jest funkcją pro.

<br />

<a name="faq114"></a>
**(114) Czy możesz dodać import dla ustawień z innych aplikacji e-mail?**

Format ustawień większości innych aplikacji e-mail nie jest udokumentowany, więc jest to trudne. Czasem możliwa jest 'inżynieria wsteczna' formatu, ale jak tylko format ustawień się zmieni, wszystko zostanie zepsute. Ponadto ustawienia są często niekompatybilne. Na przykład, FairEmail ma w odróżnieniu od większości ustawień innych aplikacji poczty elektronicznej liczbę dni na synchronizację wiadomości oraz liczbę dni na przechowywanie wiadomości, głównie aby oszczędzać użycie baterii. Ponadto utworzenie konta/tożsamości przy użyciu szybkiej konfiguracji jest proste, więc naprawdę nie jest to warte wysiłku.

<br />

<a name="faq115"></a>
**(115) Can you add email address chips?**

Email address [chips](https://material.io/design/components/chips.html) look nice, but cannot be edited, which is quite inconvenient when you made a typo in an email address.

Zauważ, że FairEmail zaznaczy adres tylko po długim naciśnięciu adresu, co ułatwia jego usunięcie.

Chips are not suitable for showing in a list and since the message header in a list should look similar to the message header of the message view it is not an option to use chips for viewing messages.

Cofnięto [commit](https://github.com/M66B/FairEmail/commit/2c80c25b8aa75af2287f471b882ec87d5a5a5015).

<br />

<a name="faq116"></a>
**~~(116) Jak mogę domyślnie pokazywać obrazy w wiadomościach od zaufanych nadawców?~~**

~~Możesz domyślnie pokazywać obrazy w wiadomościach od zaufanych nadawców poprzez włączenie ustawienia wyświetlania *Automatycznie pokazuj obrazy dla znanych kontaktów*.~~

~~Kontakty na liście kontaktów Androida są uznawane za znane i zaufane,~~ ~~chyba, że kontakt jest w grupie / ma etykietę '*Niezaufany*' (wielkość liter nie ma znaczenia).~~

<br />

<a name="faq38"></a>
<a name="faq117"></a>
**(117) Czy możesz mi pomóc przywrócić mój zakup?**

Google zarządza wszystkimi zakupami, więc jako deweloper mam niewielką kontrolę nad zakupami. A zatem zasadniczo jedyną rzeczą, którą mogę zrobić, jest udzielenie porady w zakresie:

* Upewnij się, że masz aktywne, działające połączenie internetowe
* Upewnij się, że jesteś zalogowany na właściwym koncie Google i że nie ma błędów z Twoim kontem Google
* Otwórz aplikację Sklep Play i poczekaj przynajmniej minutę, aby dać jej czas na synchronizację z serwerami Google
* Otwórz FairEmail i przejdź do ekranu funkcji pro, aby umożliwić FairEmail sprawdzenie zakupów

Możesz również spróbować wyczyścić pamięć podręczną aplikacji Sklep Play za pomocą ustawień aplikacji Android. Ponowne uruchomienie urządzenia może być konieczne, aby sklep Play rozpoznał zakupy poprawnie.

Zauważ, że:

* Zakupy są przechowywane w chmurze Google i nie mogą zostać utracone
* Nie ma ograniczeń czasowych, więc nie mogą one wygasnąć
* Google nie ujawnia programistom szczegółów (nazwa, e-mail, itp.) o nabywcach
* Aplikacja taka jak FairEmail nie może wybrać konta Google, którego chcesz użyć
* Może to zająć chwilę zanim aplikacja Sklep Play zsynchronizuje zakup na innym urządzeniu

Jeśli nie możesz rozwiązać problemu z zakupem, będziesz musiał skontaktować się z Google.

<br />

<a name="faq118"></a>
**(118) Co dokładnie oznacza 'Usuń parametry śledzenia'?**

Zaznaczenie *Usuń parametry śledzenia* usunie wszystkie [parametry UTM](https://en.wikipedia.org/wiki/UTM_parameters) z linku.

<br />

<a name="faq119"></a>
**~~(119) Czy możesz dodać kolory do widżetu wspólnej skrzynki odbiorczej?~~**

~~Widżet jest zaprojektowany tak, aby wyglądał dobrze na większości ekranów domowych/launchera poprzez uczynienie go monochromatycznym i używanie półprzezroczystego tła.~~ ~~W ten sposób widżet będzie ładnie wtopiony, ale nadal będzie odpowiednio czytelny.~~

~~Dodanie kolorów spowoduje problemy z niektórymi tłami i spowoduje problemy z czytelnością i dlatego nie zostanie to dodane.~~

Ze względu na ograniczenia Androida nie jest możliwe jednoczesne dynamiczne ustawienie przezroczystości tła i zaokrąglanie rogów.

<br />

<a name="faq120"></a>
**(120) Dlaczego powiadomienia o nowej wiadomości nie została usunięte przy otwieraniu aplikacji?**

Powiadomienia o nowych wiadomościach zostanie usunięte po przesunięciu powiadomienia lub oznaczeniu powiązanych wiadomości jako przeczytane. Otwarcie aplikacji nie usunie powiadomień o nowych wiadomościach. To daje Ci możliwość pozostawiania powiadomień o nowych wiadomościach jako przypomnienie, że nadal są nieprzeczytane wiadomości.

Na Androidzie 7 Nougat i nowszym powiadomienia o nowych wiadomościach będą [zgrupowane](https://developer.android.com/training/notify-user/group). Naciśnięcie powiadomienia podsumowującego otworzy wspólną skrzynkę odbiorczą. Powiadomienie podsumowujące można rozwinąć, aby wyświetlić indywidualne powiadomienia o nowych wiadomościach. Dotknięcie w powiadomienie o nowej wiadomości otworzy rozmowę, której wiadomość jest częścią. Zobacz [ten FAQ](#user-content-faq70) o tym, kiedy wiadomości w konwersacji będą automatycznie rozwijane i oznaczone jako przeczytane.

<br />

<a name="faq121"></a>
**(121) Jak wiadomości są grupowane w rozmowie?**

Domyślnie FariEmail grupuje wiadomości w rozmowach. Ta opcja może być wyłączona w ustawieniach wyświetlania.

FairEmail grupuje wiadomości w oparciu o standard nagłówków *Message-ID*, *In-Reply-Do* i *Referencje*. FairEmail nie grupuje po innych kryteriach, takich jak temat, ponieważ może to skutkować grupowaniem niepowiązanych wiadomości i byłoby to kosztem zwiększonego zużycia baterii.

<br />

<a name="faq122"></a>
**~~(122) Dlaczego nazwa/adres e-mail odbiorcy są wyświetlane w kolorze ostrzegawczym?~~**

~~Nazwa odbiorcy i/lub adres e-mail w sekcji adresów będą wyświetlane w kolorze ostrzeżenia~~ ~~gdy nazwa domeny nadawcy i nazwa domeny *do* nie pasują.~~ ~~Często wskazuje to, że wiadomość została odebrana *przez* konto z innym adresem e-mail.~~

<br />

<a name="faq123"></a>
**(123) Co się stanie, gdy FairEmail nie będzie mógł połączyć się z serwerem e-mail?**

Gdy FairEmail nie może połączyć się z serwerem e-mail, aby odebrać wiadomości, na przykład gdy połączenie internetowe jest złe lub zapora sieciowa lub VPN blokuje połączenie, FairEmail będzie czekał 8, 16 i 32 sekundy utrzymując urządzenie wybudzone (=zużywająć energię baterii) i spróbuje ponownie się połączyć. Jeśli to się nie powiedzie, FairEmail zaplanuje alarm w celu powtórzenia po 15, 30 i 60 minutach i pozwoli na uśpienie urządzenia (=brak zużycia baterii).

Pomiędzy zmianami w połączeniach jest 90 sekund przerwy, aby dać serwerowi e-mail możliwość wykrycia faktu przerwania poprzedniego połączenia. Jest to konieczne, ponieważ połączenie internetowe urządzenia mobilnego często jest tracone nagle i aby zapobiec problemowi opisanemu w [tym FAQ](#user-content-faq23).

Pamiętaj, że [Android doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby) nie zezwala wybudzać urządzenia wcześniej niż po 15 minutach.

*Wymuś synchronizację* w menu z trzema kropkami we wspólnej skrzynce odbiorczej można użyć, aby umożliwić FairEmail próbę ponownego połączenia bezzwłocznie.

Wysyłanie wiadomości zostanie ponowione tylko przy zmianie połączenia (ponowne połączenie z tą samą siecią lub połączenie z inną siecią), aby zapobiec stałemu blokowaniu połączenia przez serwer e-mail. Możesz pociągnąć w dół skrzynkę odbiorczą, aby ręcznie ponowić.

Pamiętaj, że wysyłanie nie zostanie ponowione w przypadku problemów z uwierzytelnianiem i gdy serwer odrzucił wiadomość. W takim przypadku możesz otworzyć/rozwinąć wiadomość i użyć ikony Cofnij, aby przenieść wiadomość do folderu Wersje robocze, ewentualnie zmienić ją i wysłać ponownie.

<br />

<a name="faq124"></a>
**(124) Dlaczego otrzymuję komunikat 'Wiadomość za duża lub zbyt skomplikowana, aby ją wyświetlić'?**

Powiadomienie *Wiadomość jest zbyt duża lub zbyt skomplikowana, aby ją wyświetlić * będzie wyświetlana, jeśli jest w niej więcej niż 100,000 znaków lub w wiadomości jest więcej niż 500 linków. Formatowanie i wyświetlenie takich wiadomości zajmie zbyt długo. Możesz zamiast tego spróbować użyć oryginalnego widoku wiadomości, wspieranego przez przeglądarkę.

<br />

<a name="faq125"></a>
**(125) Jakie są aktualne funkcje eksperymentalne?**

* ...

<br />

<a name="faq126"></a>
**(126) Czy podgląd wiadomości można wysyłać do urządzenia do noszenia?**

FairEmail pobiera wiadomość w dwóch krokach:

1. Pobranie nagłówków wiadomości
1. Pobranie tekstu wiadomości i załączników

Bezpośrednio po pierwszym kroku nastąpi powiadomienie o nowych wiadomościach. Jednak dopiero po drugim kroku, tekst wiadomości będzie dostępny. FairEmail aktualizuje powiadomienia z podglądem tekstu wiadomości, ale niestety powiadomienia na urządzeniach do noszenia nie mogą być aktualizowane.

Ponieważ nie ma gwarancji, że tekst wiadomości będzie zawsze pobierany bezpośrednio po nagłówku wiadomości, nie można zagwarantować, że powiadomienie o nowej wiadomości z tekstem podglądu będzie zawsze wysyłane do urządzenia do noszenia.

Jeśli uważasz, że jest to wystarczająco dobre, możesz włączyć opcję powiadomień *Wysyłaj powiadomienia z podglądem wiadomości tylko do urządzeń do noszenia*.

Jeśli chcesz mieć pełny tekst wiadomości na urządzeniu do noszenia, możesz włączyć opcję powiadomienia *Podgląd całego tekstu*. Pamiętaj, że niektóre urządzenia do noszenia są znane z awarii przy tej opcji włączonej.

Jeśli obsługujesz urządzenie do noszenia Samsung za pomocą aplikacji Galaxy Wearable (Samsung Gear), może być konieczne włączenie powiadomień dla FairEmail, gdy ustawienie *Powiadomienia*, *Aplikacje zainstalowane w przyszłości* jest wyłączone w tej aplikacji.

<br />

<a name="faq127"></a>
**(127) Jak mogę naprawić 'Syntactically invalid HELO argument(s)'?**

Błąd *... Syntactically invalid HELO argument(s) ...* oznacza, że serwer SMTP odrzucił lokalny adres IP lub nazwę hosta. Prawdopodobnie możesz naprawić ten błąd, włączając lub wyłączając opcję zaawansowaną tożsamości *Użyj lokalnego adresu IP zamiast nazwy hosta*.

<br />

<a name="faq128"></a>
**(128) Jak mogę zresetować zadane pytania, na przykład, aby pokazać obrazy?**

Możesz zresetować wszystkie zadane pytania, aby nie były zadawane ponownie w ustawieniach różnych.

<br />

<a name="faq129"></a>
**(129) Czy wspierany jest ProtonMail, Tutanota?**

ProtonMail używa zastrzeżonego protokołu e-mail i [nie obsługuje bezpośrednio IMAP](https://protonmail.com/support/knowledge-base/imap-smtp-and-pop3-setup/), więc nie możesz użyć FairEmail do uzyskania dostępu do ProtonMail.

Tutanota używa zastrzeżonego protokołu e-mail i [nie obsługuje IMAP](https://tutanota.com/faq/#imap), więc nie możesz użyć FairEmail do uzyskania dostępu do Tutanota.

<br />

<a name="faq130"></a>
**(130) Co oznacza błąd wiadomości ... ?**

Seria wierszy z pomarańczowymi lub czerwonymi tekstami z informacjami technicznymi oznacza, że ​​w ustawieniach Różne włączono tryb debugowania.

Ostrzeżenie *Nie znaleziono serwera na ...* oznacza, że nie zarejestrowano żadnego serwera e-mail na wskazanej nazwie domeny. Odpowiedź na wiadomość może nie być możliwa i może spowodować błąd. Może to wskazywać na sfałszowany adres e-mail i/lub spam.

Błąd *... ParseException ... * oznacza, że ​​występuje problem z otrzymaną wiadomością, prawdopodobnie spowodowany błędem w oprogramowaniu wysyłającym. FairEmail w większości przypadków znajdzie rozwiązanie, więc ta wiadomość może być uważana za ostrzeżenie zamiast błędu.

Błąd *...SendFailedException...* oznacza, że wystąpił problem podczas wysyłania wiadomości. Błąd prawie zawsze będzie zawierał powód. Powodem jest to, że wiadomość była zbyt duża lub jeden lub więcej adresów odbiorców było nieprawidłowych.

Ostrzeżenie *Wiadomość jest zbyt duża, aby zmieścić się w dostępnej pamięci* oznacza, że wiadomość była większa niż 10 MB. Nawet jeśli urządzenie ma mnóstwo miejsca na dysku Android zapewnia ograniczoną ilość pamięci roboczej aplikacji, które z kolei ogranicza rozmiar wiadomości mogące być obsługiwane.

Zobacz [tutaj](#user-content-faq22) inne komunikaty o błędach w skrzynce wychodzącej.

<br />

<a name="faq131"></a>
**(131) Czy możesz zmienić kierunek przesuwania do poprzedniej/następnej wiadomości?**

Jeśli czytasz od lewej do prawej, przesunięcie w lewo pokaże następną wiadomość. Podobnie, jeśli czytasz od prawej do lewej, przesunięcie w prawo pokaże następną wiadomość.

To zachowanie wydaje mi się całkiem naturalne, również dlatego, że jest podobne do odwracania stron.

W każdym razie, istnieje ustawienie zachowań, aby odwrócić kierunek przesunięcia.

<br />

<a name="faq132"></a>
**(132) Dlaczego nowe powiadomienia wiadomości są ciche?**

Powiadomienia są domyślnie ciche na niektórych wersjach MIUI. Zobacz [tutaj](http://en.miui.com/thread-3930694-1-1.html) jak możesz to naprawić.

W niektórych wersjach Androida występuje błąd powodując wyciszenie powiadomień przez [setOnlyAlertOnce](https://developer.android.com/reference/android/app/Notification.Builder#setOnlyAlertOnce(boolean)). Ponieważ FairEmail pokazuje powiadomienia o nowych wiadomościach zaraz po pobraniu nagłówków wiadomości i FairEmail musi zaktualizować powiadomienia o nowych wiadomościach po pobraniu tekstu wiadomości później, to nie może być naprawione ani obsłużone przez FairEmail.

Android może ograniczać liczbę powiadomień dźwiękiem, co może spowodować wyciszenie niektórych nowych wiadomości.

<br />

<a name="faq133"></a>
**(133) Dlaczego ActiveSync nie jest wspierany?**

Protokół Microsoft Exchange ActiveSync [jest opatentowany](https://en.wikipedia.org/wiki/Exchange_ActiveSync#Licensing) i dlatego nie może być wspierany. Z tego powodu nie znajdziesz wielu, jeśli w ogóle, klientów poczty elektronicznej obsługujących ActiveSync.

Protokół Microsoft Exchange Web Services [jest stopniowo wycofywany](https://techcommunity.microsoft.com/t5/Exchange-Team-Blog/Upcoming-changes-to-Exchange-Web-Services-EWS-API-for-Office-365/ba-p/608055).

Zauważ, że opis FairEmail zaczyna się od uwagi, iż niestandardowe protokoły, takie jak Microsoft Exchange Web Services i Microsoft ActiveSync nie są wspierane.

<br />

<a name="faq134"></a>
**(134) Czy możesz dodać usuwanie lokalnych wiadomości?**

*POP3*

W ustawieniach konta (Konfiguracja, krok 1, Zarządzaj, dotknij konta) możesz włączyć *Pozostaw usunięte wiadomości na serwerze*.

*IMAP*

Ponieważ protokół IMAP ma na celu synchronizację dwukierunkową, usunięcie wiadomości z urządzenia spowodowałoby ponowne pobranie wiadomości podczas ponownej synchronizacji.

FairEmail obsługuje jednak ukrywanie wiadomości, poprzez menu trzech kropek na pasku akcji tuż nad tekstem wiadomości lub przez wielokrotne wybieranie wiadomości na liście wiadomości.

Możliwe jest również ustawienie akcji przesunięcia w lewo lub w prawo, w celu ukrycia wiadomości.

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

* Konto: Ustawienia > Krok 1 > Zarządzaj > Stuknij konto
* Tożsamość: Ustawienia > Krok 2 > Zarządzaj > Dotknij tożsamości
* Folder: Przytrzymaj folder na liście folderów > Edytuj właściwości

W menu trzech kropek w prawym górnym rogu znajduje się pozycja do usunięcia konta/tożsamości/folderu.

<br />

<a name="faq137"></a>
**(137) Jak mogę zresetować 'Nie pytaj ponownie'?**

Możesz zresetować wszystkie pytania ustawione, aby nie były zadawane ponownie w ustawieniach różnych.

<br />

<a name="faq138"></a>
**(138) Czy możesz dodać kalendarz/zarządzanie kontaktami/synchronizacją?**

Kalendarz i zarządzanie kontaktami mogą być lepiej wykonane za pomocą oddzielnej, specjalistycznej aplikacji.

Zaleca się korzystanie z doskonałej, otwartej aplikacji [DAVx⁵](https://f-droid.org/packages/at.bitfire.davdroid/) do synchronizacji/zarządzania kalendarzem/kontaktami.

Większość dostawców wspiera eksport Twoich kontaktów. [Zobacz tutaj](https://support.google.com/contacts/answer/1069522) jak można zaimportować kontakty, jeśli synchronizacja nie jest możliwa.

Zauważ, że FairEmail wspiera odpowiadanie na zaproszenia do kalendarza (funkcja pro) i dodawanie zaproszeń do osobistego kalendarza.

<br />

<a name="faq83"></a>
<a name="faq139"></a>
**(139) Jak naprawić błąd 'Użytkownik jest uwierzytelniony, ale nie połączony'?**

W rzeczywistości ten konkretny błąd Microsoft Exchange jest nieprawidłowym komunikatem o błędzie spowodowanym przez błąd w starszym oprogramowaniu serwera Exchange.

Błąd *Użytkownik jest uwierzytelniony, ale nie połączony* może wystąpić jeśli:

* Hasło do konta zostało zmienione: jego zmiana również w FairEmail powinna rozwiązać problem
* Wiadomości push są włączone dla zbyt wielu folderów: zobacz [ten FAQ](#user-content-faq23), aby uzyskać więcej informacji i rozwiązań
* Adres e-mail aliasu jest używany jako nazwa użytkownika zamiast głównego adresu e-mail
* Używany jest niepoprawny schemat logowania do wspólnej skrzynki pocztowej: prawidłowy schemat to * nazwa użytkownika @ domena\alias udostępnionej skrzynki pocztowej ​​*

The shared mailbox alias will mostly be the email address of the shared account, like this:

```
you@example.com\shared@example.com
```

Note that it should be a backslash and not a forward slash.

<br />

<a name="faq140"></a>
**(140) Dlaczego tekst wiadomości zawiera dziwne znaki?**

Displaying strange characters is almost always caused by specifying no or an invalid character encoding by the sending software. FairEmail will assume [ISO 8859-1](https://en.wikipedia.org/wiki/ISO/IEC_8859-1) when no character set or when [US-ASCII](https://en.wikipedia.org/wiki/ASCII) was specified. Poza tym, nie ma sposobu na automatyczne i wiarygodne określenie poprawnego kodowania znaków, więc FairEmail nie może tego naprawić. Właściwym działaniem jest zgłoszenie tego nadawcy.

<br />

<a name="faq141"></a>
**(141) How can I fix 'A drafts folder is required to send messages'?**

To store draft messages a drafts folder is required. In most cases FairEmail will automatically select the drafts folders on adding an account based on [the attributes](https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml) the email server sends. Jednakże, niektóre serwery e-mail nie są jednak poprawnie skonfigurowane i nie wysyłają tych atrybutów. In this case FairEmail tries to identify the drafts folder by name, but this might fail if the drafts folder has an unusual name or is not present at all.

You can fix this problem by manually selecting the drafts folder in the account settings (Setup, step 1, tap account, at the bottom). If there is no drafts folder at all, you can create a drafts folder by tapping on the '+' button in the folder list of the account (tap on the account name in the navigation menu).

Some providers, like Gmail, allow enabling/disabling IMAP for individual folders. So, if a folder is not visible, you might need to enable IMAP for the folder.

Quick link for Gmail: [https://mail.google.com/mail/u/0/#settings/labels](https://mail.google.com/mail/u/0/#settings/labels)

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

![External image](https://raw.githubusercontent.com/google/material-design-icons/master/action/1x_web/ic_record_voice_over_black_48dp.png)

This requires a compatible audio recorder app to be installed. In particular [this common intent](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION) needs to be supported.

For example [this audio recorder](https://f-droid.org/app/com.github.axet.audiorecorder) is compatible.

Voice notes will automatically be attached.

<br />

<a name="faq145"></a>
**(145) How can I set a notification sound for an account, folder or sender?**

Account:

* Włącz * Oddzielne powiadomienia * w zaawansowanych ustawieniach konta (Ustawienia, krok 1, Zarządzaj, dotknij konta, dotknij Zaawansowane)
* Naciśnij i przytrzymaj konto na liście kont (Konfiguracja, krok 1, Zarządzaj) i wybierz *Edytuj kanał powiadomień*, aby zmienić dźwięk powiadomienia

Folder:

* Przytrzymaj folder na liście folderów i wybierz *Utwórz kanał powiadomień*
* Przytrzymaj folder na liście folderów i wybierz *Edytuj kanał powiadomień*, aby zmienić dźwięk powiadomień

Sender:

* Otwórz wiadomość od nadawcy i rozwiń ją
* Rozwiń sekcję adresów naciskając strzałkę w dół
* Dotknij ikony dzwonka, aby utworzyć lub edytować kanał powiadomień i zmienić dźwięk powiadomień

The order of precendence is: sender sound, folder sound, account sound and default sound.

Setting a notification sound for an account, folder or sender requires Android 8 Oreo or later and is a pro feature.

<br />

<a name="faq146"></a>
**(146) How can I fix incorrect message times?**

Since the sent date/time is optional and can be manipulated by the sender, FairEmail uses the server received date/time.

Sometimes the received date/time is incorrect, mostly because messages were incorrectly imported from another server and sometimes due to a bug in the email server.

In this rare case you can enable the account option *Use date header sent time instead of server received time* (Setup, step 1, Manage, tap account, tap Advanced) as a workaround.

This will not change the time of already synchronized messages. To solve this, long press the folder(s) in the folder list and select *Delete local messages* and *Synchronize now*.

<br />

<a name="faq147"></a>
**(147) What should I know about third party versions?**

You likely came here because you are using a third party build of FairEmail.

The F-Droid build is supported, but any other unofficial build is not supported.

F-Droid builds irregularly, which can be problematic when there is an important update. Therefore you are advised to switch to the GitHub release.

The F-Droid version is built from the same source code, but signed differently. This means that all features are available in the F-Droid version too, except for using the Gmail quick setup wizard because Google approved (and allows) one signature only.

Note that you'll need to uninstall the F-Droid build first before you can install a GitHub release because Android refuses to install the same app with a different signature for security reasons.

Note that the GitHub version will automatically check for updates. When desired, this can be turned off in the miscellaneous settings.

Please [see here](https://github.com/M66B/FairEmail/blob/master/README.md#user-content-downloads) for all download options.

If you have a problem with the F-Droid build, please check if there is a newer version first.

<br />

<a name="faq148"></a>
**(148) How can I use an Apple iCloud account?**

There is a built-in profile for Apple iCloud, but if needed you can find the right settings [here](https://support.apple.com/en-us/HT202304).

When using two-factor authentication you might need to use an [app-specific password](https://support.apple.com/en-us/HT204397).

<br />

<a name="faq149"></a>
**(149) How does the unread message count widget work?**

The unread message count widget shows the number of unread messages either for all accounts or for a selected account, but only for the folders for which new message notifications are enabled.

Tapping on the notification will synchronize all folders for which synchronization is enabled and will open:

* ekran początkowy jeśli wybrane były wszystkie konta
* lista folderów, gdy wybrano konkretne konto i gdy powiadomienia o nowych wiadomościach są włączone dla wielu folderów
* lista wiadomości, gdy wybrano konkretne konto i gdy powiadomienia o nowych wiadomościach są włączone dla jednego folderu

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

* Kiedy zaznaczysz wiadomość w IMAP jako usuniętą: Automatyczne wygasanie wyłączone - Poczekaj, aż klient zaktualizuje serwer.
* Gdy wiadomość zostanie oznaczona jako usunięta i usunięta z ostatniego widocznego folderu IMAP: natychmiast usuń wiadomość na zawsze

Note that archived messages can be deleted only by moving them to the trash folder first.

Some background: Gmail seems to have an additional message view for IMAP, which can be different from the main message view.

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
**(156) How can I set up an Office365 account?**

An Office365 account can be set up via the quick setup wizard and selecting *Office365 (OAuth)*.

If the wizard ends with *AUTHENTICATE failed*, IMAP and/or SMTP might be disabled for the account. In this case you should ask the administrator to enable IMAP and SMTP. The procedure is documented [here](https://docs.microsoft.com/en-in/exchange/troubleshoot/configure-mailboxes/pop3-imap-owa-activesync-office-365).

<br />

<a name="faq157"></a>
**(157) How can I set up an Free.fr account?**

Veuillez [voir ici](https://free.fr/assistance/597.html) pour les instructions.

**SMTP est désactivé par défaut**, veuillez [voir ici](https://free.fr/assistance/2406.html) comment il peut être activé.

Veuillez [voir ici](http://jc.etiemble.free.fr/abc/index.php/trucs-astuces/configurer-smtp-free-fr) pour un guide détaillé.

<br />

<a name="faq158"></a>
**(158) Which camera / audio recorder do you recommend?**

To take photos and to record audio a camera and an audio recorder app are needed. The following apps are open source cameras and audio recorders:

* [Open Camera](https://play.google.com/store/apps/details?id=net.sourceforge.opencamera) ([F-Droid](https://f-droid.org/en/packages/net.sourceforge.opencamera/))
* [Audio Recorder](https://play.google.com/store/apps/details?id=com.github.axet.audiorecorder) ([F-Droid](https://f-droid.org/packages/com.github.axet.audiorecorder/))

To record voice notes, etc, the audio recorder needs to support [MediaStore.Audio.Media.RECORD_SOUND_ACTION](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media#RECORD_SOUND_ACTION). Oddly, most audio recorders seem not to support this standard Android action.

<br />

<a name="faq159"></a>
**(159) What are Disconnect's tracker protection lists?**

Please see [here](https://disconnect.me/trackerprotection) for more information about Disconnect's tracker protection lists.

After downloading the lists in the privacy settings, the lists can optionally be used:

* aby ostrzec przed linkami śledzącymi podczas otwierania linków
* aby rozpoznać obrazy śledzące w wiadomościach

Tracking images will be disabled only if the corresponding main 'disable' option is enabled.

Tracking images will not be recognized when the domain is classified as '*Content*', see [here](https://disconnect.me/trackerprotection#trackers-we-dont-block) for more information.

This command can be sent to FairEmail from an automation app to update the protection lists:

```
(adb shell) am startservice -a eu.faircode.email.DISCONNECT.ME
```

Updating once a week will probably be sufficient, please see [here](https://github.com/disconnectme/disconnect-tracking-protection/commits/master) for recent lists changes.

<br />

## Wsparcie

Only the latest Play store version and latest GitHub release are supported. This also means that downgrading is not supported.

Requested features should:

* być przydatne dla większości ludzi
* nie komplikować użycia FairEmail
* pasować do filozofii FairEmail (zorientowanej na prywatność i bezpieczeństwo)
* zgodne z popularnymi standardami (IMAP, SMTP, itp.)

Features not fulfilling these requirements will likely be rejected. This is also to keep maintenance and support in the long run feasible.

If you have a question, want to request a feature or report a bug, please use [this form](https://contact.faircode.eu/?product=fairemailsupport).

GitHub issues are disabled due to frequent misusage.

Copyright &copy; 2018-2020 Marcel Bokhorst.
