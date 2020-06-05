# Telepítő súgó

A FairEmail telepítése nagyon egyszerű. Hozzá kell adnia legalább egy fiókot, hogy fogadhasson, és egy identitást, hogy küldhessen e-maileket. A gyors beállítás egyszerre hoz létre egy fiókot és egy identitást a legtöbb fő szolgáltató esetében.

## Követelmények

Internetkapcsolat szükséges a fiókok és az identitások beállításához.

## Gyors beállítás

Csak írd be a neved, email címed és jelszavad és koppints a *Tovább* gombra.

Ez működni fog a legtöbb email szolgáltató esetében.

Ha a gyors telepítés nem működik, egy másik módon kell majd fiókot és identitást beállítanod, a leírást lásd alább.

## Fiók beállítása - email fogadásához

Fiók hozzáadásához koppintson a *Fiókok kezelése*-re majd koppintson a narancssárga *Hozzáadás* gombra alul. Válasszon egy szolgáltatót a listából, adja meg a felhasználónevet, ami legtöbbször az email-címe, és írja be a jelszavát. Koppintson az *Ellenőrzés*-re, hogy engedélyezze a FairEmail-nek, hogy kapcsolódjon az email szerverhez és lekérje a rendszermappák listáját. A rendszermappák listájának átnézése után hozzáadhatod a fiókot a *Mentés* megnyomásával.

Ha a szolgáltatója nincs a listában, koppintson az *Egyéni*-re. Írja be a domain nevet, például *gmail.com*, majd nyomjon a *Beállítások lekérése*-re. Ha a szolgáltatód támogatja [az automata felfedezést](https://tools.ietf.org/html/rfc6186), a FairEmail ki fogja tölteni a hosztnevet és a port számát, máskülönben lásd a telepítési instrukciókat a szolgáltatódhoz a megfelelő IMAP hosztnévhez, portszámhoz és protokolloz (SSL/TLS vagy STARTTLS). Erről több információért kérlek nézd meg [ezt](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Identitás beállítása - email küldéshez

Az előbbiekhez hasonlóan, egy identitás hozzáadásához koppintson az *Identitás kezelése*-re, majd koppintson a narancssárga *Hozzáadás* gombra alul. Írja be a nevet, amit szeretne megjeleníteni a Feladó mezőben az elküldött e-maileknél, és válasszon ki egy kapcsolódó fiókot. Koppintson a *Mentés*-re, hogy hozzáadja az identitást.

Ha a fiók manuálisan lett konfigurálva, valószínűleg az identitást is manuálisan kell majd. Írja be a domain nevet, például *gmail.com*, majd koppintson a *Beállítások lekérése*-re. Ha a szolgáltató támogatja [az automata felfedezést](https://tools.ietf.org/html/rfc6186), a FairEmail ki fogja tölteni a hosztnevet és a portszámot, máskülönben lásd a telepítési instrukciókat a szolgáltatódhoz a megfelelő SMTP hosztnévhez, portszámhoz és protokolloz (SSL/TLS vagy STARTTLS).

Az aliasok használatáról lásd [ezt a GYIK-et](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9).

## Engedély megadása - a kapcsolat információk eléréséhez

Ha keresni szeretne email címeket, látni szeretne névjegyfotókat stb., akkor a névjegyek olvasása engedélyt meg kell adnia a FairEmailnek. Csak koppintson az *Engedélyek megadása*-ra, majd válassza ki az *Engedélyezés*-t.

## Akkumulátorhasználat optimalizálás beállítása - hogy folyamatosan fogadjon leveleket

Újabb Android verziókon az Android az alkalmazásokat alvó módba teszi, amikor ki van kapcsolva a képernyő egy ideig, hogy az akkumulátorhasználatot csökkentse. Ha késések nélkül szeretné megkapni az új e-maileket, akkor ajánlott kikapcsolni az akkumulátor optimalizálást a FairEmail esetében. Koppintson az *Akkumulátorhasználat optimalizálásának kikapcsolása* gombra és kövesse az utasításokat.

## Kérdések és problémák

Ha kérdése vagy problémája van, [ezen az oldalon](https://github.com/M66B/FairEmail/blob/master/FAQ.md) talál segítséget.