# Pomoc instalacyjna

Konfiguracja FairEmail jest całkiem prosta. Musisz dodać co najmniej jedno konto, aby otrzymywać wiadomości email i przynajmniej jedną tożsamość, jeśli chcesz wysłać wiadomości e-mail. Szybka konfiguracja doda konto i tożsamość w jednym kroku dla większości głównych dostawców.

## Wymagania

Do skonfigurowania kont i tożsamości wymagane jest połączenie internetowe.

## Szybka konfiguracja

Po prostu wybierz odpowiedniego dostawcę lub *Innego dostawcę* i wprowadź swoje imię i nazwisko, adres e-mail oraz hasło, a następnie dotknij *Sprawdź*.

To zadziała w przypadku większości dostawców poczty e-mail.

Jeśli szybka konfiguracja nie zadziała, musisz ręcznie skonfigurować konto i tożsamość. Instrukcje znajdują się poniżej.

## Konfiguracja konta - aby odbierać wiadomości e-mail

Aby dodać konto, dotknij *Konfiguracja ręczna i więcej opcji*, dotknij *Konta*, dotknij przycisku 'plus' u dołu i wybierz IMAP (lub POP3). Wybierz dostawcę z listy, wprowadź nazwę użytkownika, która jest najczęściej Twoim adresem e-mail i wprowadź hasło. Naciśnij *Sprawdź*, aby FairEmail mógł połączyć się z serwerem e-mail i pobrać listę folderów systemowych. Po zapoznaniu się z wyborem folderu systemowego możesz dodać konto, naciskając *Zapisz*.

Jeśli Twojego dostawcy nie ma na liście dostawców, jest tysiące dostawców, wybierz opcję *Niestandardowy*. Wprowadź nazwę domeny, na przykład *gmail.com* i naciśnij *Pobierz ustawienia*. Jeśli Twój dostawca wspiera [automatyczne wykrywanie](https://tools.ietf.org/html/rfc6186), FairEmail wypełni nazwę hosta i numer portu, w przeciwnym razie sprawdź instrukcje konfiguracji swojego dostawcy dotyczące prawidłowej nazwy hosta IMAP, numeru portu i protokołu szyfrowania (SSL/TLS lub STARTTLS). Więcej informacji na ten temat można znaleźć [tutaj](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Konfiguracja tożsamości - aby wysyłać wiadomości e-mail

Podobnie, aby dodać tożsamość, dotknij *Konfiguracja ręczna i więcej opcji*, dotknij *Tożsamości* i dotknij przycisku 'plus' u dołu. Wprowadź nazwę, która ma pojawiać się w adresie wysyłanych wiadomości e-mail i wybierz połączone konto. Doknij*Zapisz*, aby dodać tożsamość.

Jeśli konto zostało skonfigurowane ręcznie, prawdopodobnie musisz również ręcznie skonfigurować tożsamość. Wprowadź nazwę domeny, na przykład *gmail.com* i dotknij *Uzyskaj ustawienia*. Jeśli Twój dostawca wspiera [automatyczne wykrywanie](https://tools.ietf.org/html/rfc6186), FairEmail wypełni nazwę hosta i numer portu, w przeciwnym razie sprawdź instrukcje konfiguracji swojego dostawcy, aby uzyskać odpowiednią nazwę hosta SMTP, numer portu i protokół szyfrowania (SSL/TLS lub STARTTLS).

Zobacz [to FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) na temat używania aliasów.

## Udziel uprawnień - aby uzyskać dostęp do informacji kontaktowych

Jeśli chcesz wyszukiwać adresy e-mail, wyświetlać zdjęcia kontaktów itp., musisz zezwolić na odczytywanie informacji kontaktowych przez FairEmail. Po prostu dotknij *Przyznaj* i wybierz *Zezwól*.

## Skonfiguruj optymalizacje baterii - aby otrzymywać e-maile bezzwłocznie

W najnowszych wersjach Android usypia aplikacje, gdy ekran jest wyłączony przez pewien czas, aby zmniejszyć zużycie baterii. Jeśli chcesz otrzymywać nowe wiadomości e-mail bez opóźnień, wyłącz optymalizacje baterii dla FairEmail. Dotknij *Zarządzaj* i postępuj zgodnie z instrukcjami.

## Pytania lub problemy

Jeśli masz pytanie lub problem, [zobacz tutaj](https://github.com/M66B/FairEmail/blob/master/FAQ.md), aby uzyskać pomoc.