# Telepítő súgó

A FairEmail telepítése nagyon egyszerű. Hozzá kell adnia legalább egy fiókot, hogy fogadhasson, és egy identitást, hogy küldhessen emaileket. A gyors beállítás egyszerre hoz létre egy fiókot és egy identitást a legtöbb fő szolgáltató esetében.

## Követelmények

Internetkapcsolat szükséges a fiókok és az identitások beállításához.

## Gyors beállítás

Csak írd be a neved, email címed és jelszavad és koppints a *Tovább* gombra.

Ez működni fog a legtöbb email szolgáltató esetében.

Ha a gyors telepítés nem működik, egy másik módon kell majd fiókot és identitást beállítanod, lásd lejjebb instrukciókért.

## Fiók beállítása - email fogadásához

Fiók hozzáadásához koppintson a *Fiókok kezelése*-re majd koppintson a narancssárga *Hozzáadás* gombra alul. Válasszon egy szolgáltatót a listából, adja meg a felhasználónevet, ami legtöbbször az email-címe, és írja be a jelszavát. Koppintson az *Ellenőrzés*-re hogy engedélyezze a FairEmail-nek hogy kapcsolódjon az email szerverhez és lekérje a rendszermappák listáját. A rendszermappák listájának átnézése után hozzáadhatod a fiókot a *Mentés* megnyomásával.

Ha a szolgáltatója nincs a listában, koppintson az *Egyéni*-re. Írja be a domain nevet, például *gmail.com* majd nyomjon a *beállítások lekérése*-re. Ha a szolgáltatód támogatja [az automata felfedezést](https://tools.ietf.org/html/rfc6186), a FairEmail ki fogja tölteni a hosztnévet és portszámát, máskülönben lásd a telepítési instrukciókat a szolgáltatódhoz a megfelelő IMAP hosztnévhez, portszámhoz és protokolloz (SSL/TLS vagy STARTTLS). Erről több információért kérlek nézd meg [ezt](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Identitás beállítása - email küldéshez

Hasonlóan egy identitás hozzáadásához koppintson az *Identitás kezelése*-re majd koppintson a narancssárga *Hozzáadás* gombra alul. Írd be a nevet amit szeretnél megjeleníttetni a Címzett címben az emaileknél amit küldesz és válassz ki egy linkelt fiókot. Koppints a *Mentés*-re hogy hozzáadd az identitást.

Ha a fiók manuálisan lett konfigurálva, valószínűleg az identitást is manuálisan kell majd. Írja be a domain nevet, például *gmail.com* majd koppintson a *Beállítások lekérése*-re. Ha a szolgáltatód támogatja [az automata felfedezést](https://tools.ietf.org/html/rfc6186), a FairEmail ki fogja tölteni a hosztnévet és portszámát, máskülönben lásd a telepítési instrukciókat a szolgáltatódhoz a megfelelő SMTP hosztnévhez, portszámhoz és protokolloz (SSL/TLS vagy STARTTLS).

Az aliasok használatáról lásd [ezt a GYIK-et](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9).

## Engedély megadása - a kapcsolat információk eléréséhez

Ha keresni szeretnél email címeket, látni szeretnél névjegyfotókat, stb, akkor a névjegyek olvasása engedélyt meg kell adnod. Csak koppints az *Engedélyek megadása*-ra, majd válaszd ki az *Engedélyezés*-t.

## Akkumulátorhasználat optimalizálás beállítása - hogy folyamatosan fogadjon leveleket

Újabb Android verziókon az Android az alkalmazásokat alvó módba rakja amikor ki van kapcsolva a képernyő egy ideig hogy az akkumulátorhasználatot csökkentse. Ha késések nélkül szeretné megkapni az új emaileket, akkor ajánlott kikapcsolni az akkumulátor optimalizálást a FairEmail-on. Koppintson az *Akkumulátorhasználat optimalizálásának kikapcsolása* gombra és kövesse az utasításokat.

## Kérdések és problémák

Ha kérdésed vagy problémád van, lásd [ezt az oldalt](https://github.com/M66B/FairEmail/blob/master/FAQ.md) segítségért.