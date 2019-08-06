# Pomoc instalacyjna

Konfiguracja FairEmail jest dość prosta. Musisz dodać co najmniej jedno konto, aby otrzymywać wiadomości email i przynajmniej jedną tożsamość, jeśli chcesz wysłać wiadomości email. Szybka konfiguracja doda konto i tożsamość za jednym razem dla większości głównych dostawców.

## Wymagania

Do skonfigurowania kont i tożsamości wymagane jest połączenie internetowe.

## Szybka konfiguracja

Po prostu wpisz swoje imię i nazwisko, adres email i hasło, a następnie naciśnij przycisk *Idź*.

To działa dla większości głównych dostawców poczty email.

Jeśli szybka konfiguracja nie zadziała, musisz skonfigurować konto i tożsamość w inny sposób, patrz poniżej, aby uzyskać instrukcje.

## Konfiguracja konta - aby odbierać wiadomości email

Aby dodać konto, naciśnij *Zarządzaj kontami* i naciśnij pomarańczowy przycisk *dodaj* na dole. Wybierz dostawcę z listy, wprowadź nazwę użytkownika, która jest najczęściej Twoim adresem email i wprowadź hasło. Naciśnij *Sprawdź*, aby FairEmail mógł połączyć się z serwerem email i pobrać listę folderów systemowych. Po zapoznaniu się z wyborem folderu systemowego możesz dodać konto, naciskając *Zapisz*.

Jeśli Twój dostawca nie znajduje się na liście dostawców, wybierz *Własne*. Wprowadź nazwę domeny, na przykład *gmail.com* i naciśnij *Pobierz ustawienia*. Jeśli Twój dostawca obsługuje [automatyczne wykrywanie](https://tools.ietf.org/html/rfc6186), FairEmail wypełni nazwę hosta i numer portu, w przeciwnym razie sprawdź instrukcje instalacji swojego dostawcy, aby uzyskać prawidłową nazwę hosta IMAP, numer portu i protokół (SSL/TLS lub STARTTLS). Więcej informacji na ten temat można znaleźć [tutaj](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Konfiguracja tożsamości - aby wysyłać wiadomości email

Podobnie, aby dodać tożsamość, naciśnij *Zarządzaj tożsamościami* i naciśnij pomarańczowy przycisk *dodaj* na dole. Enter the name you want to appear in de from address of the emails you send and select a linked account. Tap *Save* to add the identity.

If the account was configured manually, you likely need to configure the identity manually too. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the hostname and port number, else check the setup instructions of your provider for the right SMTP hostname, port number and protocol (SSL/TLS or STARTTLS).

See [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) about using aliases.

## Grant permissions - to access contact information

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant read contacts permission to FairEmail. Just tap *Grant permissions* and select *Allow*.

## Setup battery optimizations - to continuously receive emails

On recent Android versions, Android will put apps to sleep when the screen is off for some time to reduce battery usage. If you want to receive new emails without delays, you should disable battery optimizations for FairEmail. Tap *Disable battery optimizations* and follow the instructions.

## Questions or problems

If you have a question or problem, please [see here](https://github.com/M66B/FairEmail/blob/master/FAQ.md) or use [this contact form](https://contact.faircode.eu/?product=fairemailsupport) to ask for help (you can use the transaction number "*setup help*").