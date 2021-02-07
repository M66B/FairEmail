# Telepítő súgó

A FairEmail telepítése nagyon egyszerű. Hozzá kell adnia legalább egy fiókot, hogy fogadhasson, és egy identitást, hogy küldhessen e-maileket. The quick setup will add an account and an identity in one go for most providers.

## Követelmények

Internetkapcsolat szükséges a fiókok és az identitások beállításához.

## Gyors beállítás

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Fiók beállítása - email fogadásához

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Válasszon egy szolgáltatót a listából, adja meg a felhasználónevet, ami legtöbbször az email-címe, és írja be a jelszavát. Koppintson az *Ellenőrzés*-re, hogy engedélyezze a FairEmail-nek, hogy kapcsolódjon az email szerverhez és lekérje a rendszermappák listáját. A rendszermappák listájának átnézése után hozzáadhatod a fiókot a *Mentés* megnyomásával.

If your provider is not in the list of providers (there are thousands of providers), select *Custom*. Írja be a domain nevet, például *gmail.com*, majd nyomjon a *Beállítások lekérése*-re. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Erről több információért kérlek nézd meg [ezt](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Identitás beállítása - email küldéshez

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Írja be a nevet, amit szeretne megjeleníteni a Feladó mezőben az elküldött e-maileknél, és válasszon ki egy kapcsolódó fiókot. Koppintson a *Mentés*-re, hogy hozzáadja az identitást.

Ha a fiók manuálisan lett konfigurálva, valószínűleg az identitást is manuálisan kell majd. Írja be a domain nevet, például *gmail.com*, majd koppintson a *Beállítások lekérése*-re. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Az aliasok használatáról lásd [ezt a GYIK-et](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9).

## Engedély megadása - a kapcsolat információk eléréséhez

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Akkumulátorhasználat optimalizálás beállítása - hogy folyamatosan fogadjon leveleket

Újabb Android verziókon az Android az alkalmazásokat alvó módba teszi, amikor ki van kapcsolva a képernyő egy ideig, hogy az akkumulátorhasználatot csökkentse. Ha késések nélkül szeretné megkapni az új e-maileket, akkor ajánlott kikapcsolni az akkumulátor optimalizálást a FairEmail esetében. Tap *Manage* and follow the instructions.

## Kérdések és problémák

Ha kérdése vagy problémája van, [ezen az oldalon](https://github.com/M66B/FairEmail/blob/master/FAQ.md) talál segítséget.