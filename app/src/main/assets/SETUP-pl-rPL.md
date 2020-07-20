# Pomoc instalacyjna

Konfiguracja FairEmail jest całkiem prosta. Musisz dodać co najmniej jedno konto, aby otrzymywać wiadomości email i przynajmniej jedną tożsamość, jeśli chcesz wysłać wiadomości e-mail. Szybka konfiguracja doda konto i tożsamość za jednym razem dla większości głównych dostawców.

## Wymagania

Do skonfigurowania kont i tożsamości wymagane jest połączenie internetowe.

## Szybka konfiguracja

Po prostu wpisz swoje imię i nazwisko, adres e-mail i hasło, a następnie naciśnij przycisk *Idź*.

To działa dla większości głównych dostawców poczty e-mail.

Jeśli szybka konfiguracja nie zadziała, musisz skonfigurować konto i tożsamość w inny sposób, patrz poniżej, aby uzyskać instrukcje.

## Konfiguracja konta - aby odbierać wiadomości e-mail

Aby dodać konto, naciśnij *Zarządzaj kontami* i naciśnij pomarańczowy przycisk *dodaj* na dole. Wybierz dostawcę z listy, wprowadź nazwę użytkownika, która jest najczęściej Twoim adresem e-mail i wprowadź hasło. Naciśnij *Sprawdź*, aby FairEmail mógł połączyć się z serwerem e-mail i pobrać listę folderów systemowych. Po zapoznaniu się z wyborem folderu systemowego możesz dodać konto, naciskając *Zapisz*.

Jeśli Twój dostawca nie znajduje się na liście dostawców, wybierz *Własne*. Wprowadź nazwę domeny, na przykład *gmail.com* i naciśnij *Pobierz ustawienia*. Jeśli Twój dostawca obsługuje [automatyczne wykrywanie](https://tools.ietf.org/html/rfc6186), FairEmail wypełni nazwę hosta i numer portu, w przeciwnym razie sprawdź instrukcje instalacji swojego dostawcy, aby uzyskać prawidłową nazwę hosta IMAP, numer portu i protokół (SSL/TLS lub STARTTLS). Więcej informacji na ten temat można znaleźć [tutaj](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Konfiguracja tożsamości - aby wysyłać wiadomości e-mail

Podobnie, aby dodać tożsamość, naciśnij *Zarządzaj tożsamościami* i naciśnij pomarańczowy przycisk *dodaj* na dole. Wprowadź nazwę, która ma pojawiać się w adresie wysyłanych wiadomości e-mail i wybierz połączone konto. Doknij*Zapisz*, aby dodać tożsamość.

Jeśli konto zostało skonfigurowane ręcznie, prawdopodobnie musisz również ręcznie skonfigurować tożsamość. Wprowadź nazwę domeny, na przykład *gmail.com* i dotknij *Uzyskaj ustawienia*. Jeśli Twój dostawca obsługuje [automatyczne wykrywanie](https://tools.ietf.org/html/rfc6186), FairEmail wypełni nazwę hosta i numer portu, w przeciwnym razie sprawdź instrukcje konfiguracji swojego dostawcy dla właściwej nazwy hosta SMTP, numeru portu i protokołu (SSL/TLS lub STARTTLS).

Zobacz [to FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) na temat używania aliasów.

## Udziel uprawnień - aby uzyskać dostęp do informacji kontaktowych

Jeśli chcesz wyszukiwać adresy e-mail, pokazywać zdjęcia kontaktów, itp. musisz udzielić FairEmail uprawnienia do odczytu kontaktów. Po prostu dotknij *Przyznaj uprawnienia* i wybierz *Zezwól*.

## Skonfiguruj optymalizacje baterii - aby otrzymywać e-maile bezzwłocznie

W najnowszych wersjach Android usypia aplikacje, gdy ekran jest wyłączony przez pewien czas, aby zmniejszyć zużycie baterii. Jeśli chcesz otrzymywać nowe wiadomości e-mail bez opóźnień, wyłącz optymalizacje baterii dla FairEmail. Dotknij *Wyłącz optymalizacje baterii* i postępuj zgodnie z instrukcjami.

## Pytania lub problemy

Jeśli masz pytanie lub problem, [zobacz tutaj](https://github.com/M66B/FairEmail/blob/master/FAQ.md), aby uzyskać pomoc.