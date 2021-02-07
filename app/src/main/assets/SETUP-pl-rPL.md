# Pomoc instalacyjna

Konfiguracja FairEmail jest całkiem prosta. Musisz dodać co najmniej jedno konto, aby otrzymywać wiadomości email i przynajmniej jedną tożsamość, jeśli chcesz wysłać wiadomości e-mail. The quick setup will add an account and an identity in one go for most providers.

## Wymagania

Do skonfigurowania kont i tożsamości wymagane jest połączenie internetowe.

## Szybka konfiguracja

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Konfiguracja konta - aby odbierać wiadomości e-mail

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Wybierz dostawcę z listy, wprowadź nazwę użytkownika, która jest najczęściej Twoim adresem e-mail i wprowadź hasło. Naciśnij *Sprawdź*, aby FairEmail mógł połączyć się z serwerem e-mail i pobrać listę folderów systemowych. Po zapoznaniu się z wyborem folderu systemowego możesz dodać konto, naciskając *Zapisz*.

If your provider is not in the list of providers (there are thousands of providers), select *Custom*. Wprowadź nazwę domeny, na przykład *gmail.com* i naciśnij *Pobierz ustawienia*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Więcej informacji na ten temat można znaleźć [tutaj](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Konfiguracja tożsamości - aby wysyłać wiadomości e-mail

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Wprowadź nazwę, która ma pojawiać się w adresie wysyłanych wiadomości e-mail i wybierz połączone konto. Doknij*Zapisz*, aby dodać tożsamość.

Jeśli konto zostało skonfigurowane ręcznie, prawdopodobnie musisz również ręcznie skonfigurować tożsamość. Wprowadź nazwę domeny, na przykład *gmail.com* i dotknij *Uzyskaj ustawienia*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Zobacz [to FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) na temat używania aliasów.

## Udziel uprawnień - aby uzyskać dostęp do informacji kontaktowych

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Skonfiguruj optymalizacje baterii - aby otrzymywać e-maile bezzwłocznie

W najnowszych wersjach Android usypia aplikacje, gdy ekran jest wyłączony przez pewien czas, aby zmniejszyć zużycie baterii. Jeśli chcesz otrzymywać nowe wiadomości e-mail bez opóźnień, wyłącz optymalizacje baterii dla FairEmail. Tap *Manage* and follow the instructions.

## Pytania lub problemy

Jeśli masz pytanie lub problem, [zobacz tutaj](https://github.com/M66B/FairEmail/blob/master/FAQ.md), aby uzyskać pomoc.