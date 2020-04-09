# Telepítő súgó

A FairEmail telepítése nagyon egyszerű. Hozzá kell adnia legalább egy fiókot, hogy fogadhasson, és egy identitást, hogy küldhessen emaileket. A gyors beállítás egyszerre hoz létre egy fiókot és egy identitást a legtöbb fő szolgáltató esetében.

## Követelmények

Internetcsatlakozás szükséges a fiókok és az identitások létrehozásához.

## Gyors beállítás

Csak írd be a neved, email címed és jelszavad és koppints a *Tovább* gombra.

Ez működni fog a legtöbb email szolgáltató esetében.

Ha a gyors telepítés nem működik, egy másik módon kell majd fiókot és identitást beállítanod, lásd lejjebb instrukciókért.

## Fiók beállítása - email fogadásához

Fiók hozzáadásához koppintson a *Fiókok kezelése*-re majd koppintson a narancssárga *Hozzáadás* gombra alul. Válasszon egy szolgáltatót a listából, adja meg a felhasználónevet, ami legtöbbször az email-címe, és írja be a jelszavát. Koppintson az *Ellenőrzés*-re hogy engedélyezze a FairEmail-nek hogy kapcsolódjon az email szerverhez és lekérje a rendszermappák listáját. After reviewing the system folder selection you can add the account by tapping *Save*.

Ha a szolgáltatója nincs a listában, koppintson az *Egyéni*-re. Írja be a domain nevet, például *gmail.com* majd nyomjon a *beállítások lekérése*-re. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the hostname and port number, else check the setup instructions of your provider for the right IMAP hostname, port number and protocol (SSL/TLS or STARTTLS). For more about this, please see [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Identitás beállítása - email küldéshez

Similarly, to add an identity, tap *Manage identity* and tap the orange *add* button at the bottom. Enter the name you want to appear in the from address of the emails you send and select a linked account. Tap *Save* to add the identity.

If the account was configured manually, you likely need to configure the identity manually too. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the hostname and port number, else check the setup instructions of your provider for the right SMTP hostname, port number and protocol (SSL/TLS or STARTTLS).

Az aliasok használatáról lásd [ezt a GYIK-et](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9).

## Grant permissions - to access contact information

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant read contacts permission to FairEmail. Just tap *Grant permissions* and select *Allow*.

## Akkumulátorhasználat optimalizálás beállítása - hogy folyamatosan fogadjon leveleket

Újabb Android verziókon az Android az alkalmazásokat alvó módba rakja amikor ki van kapcsolva a képernyő egy ideig hogy az akkumulátorhasználatot csökkentse. Ha késések nélkül szeretné megkapni az új emaileket, akkor ajánlott kikapcsolni az akkumulátor optimalizálást a FairEmail-on. Koppintson az *Akkumulátorhasználat optimalizálásának kikapcsolása* gombra és kövesse az utasításokat.

## Kérdések és problémák

Ha kérdésed vagy problémád van, lásd [ezt az oldalt](https://github.com/M66B/FairEmail/blob/master/FAQ.md) segítségért.