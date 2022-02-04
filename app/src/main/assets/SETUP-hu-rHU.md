# Telepítő súgó

A FairEmail telepítése nagyon egyszerű. Hozzá kell adnia legalább egy fiókot, hogy fogadhasson, és egy identitást, hogy küldhessen e-maileket. A gyors varázsló a fiók hozzáadása mellett hozzáadja az azonosítót is a legtöbb szolgáltatónál.

## Követelmények

Internetkapcsolat szükséges a fiókok és az identitások beállításához.

## Gyors beállítás

Csak válassza a megfelelő szolgáltatót vagy a *Más szolgáltató* -t és adja meg nevét, címét és jelszavát, majd nyomjon az *Ellenőrzés* -re.

Ez működni fog a legtöbb email szolgáltatónál.

Ha a gyors varázsló nem működik, akkor kézzel kell beállítani a fiókot és az azonosítót, lásd alul az instrukciókat.

## Fiók beállítása - email fogadásához

Fiók hozzáadásához, noymjon a *Kézi beállítás és több opció* panelre, azon belül *Fiókok*, majd a 'plusz' ikonnal állítsa be az IMAP-ot (vagy POP3-at). Válasszon egy szolgáltatót a listából, adja meg a felhasználónevet, ami legtöbbször az email-címe, és írja be a jelszavát. Koppintson az *Ellenőrzés*-re, hogy engedélyezze a FairEmail-nek, hogy kapcsolódjon az email szerverhez és lekérje a rendszermappák listáját. A rendszermappák listájának átnézése után hozzáadhatod a fiókot a *Mentés* megnyomásával.

Ha az Ön szolgáltatója nincs a listán, több ezer más szolgáltatót is támogat az app, válassza az *Egyéni* részt. Írja be a domain nevet, például *gmail.com*, majd nyomjon a *Beállítások lekérése*-re. Ha a szolgáltatója támogatja az [automatikus-felfedezést](https://tools.ietf.org/html/rfc6186), FairEmail automatikusan kitölti a hoszt és port számokat, máskülönben kérdezze le a saját szolgáltatójától a megfelelő IMAP hoszt nevet, port számokat és titkosítási protokollt (SSL/TLS vagy STARTTLS). Erről több információért kérlek nézd meg [ezt](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Identitás beállítása - email küldéshez

Hasonlóan, az azonosító hozzáadásához nyomjon a *Kézi beállítás és további opciókra*, majd az *Azonosítókra*, és a 'plusz' gombra. Írja be a nevet, amit szeretne megjeleníteni a Feladó mezőben az elküldött e-maileknél, és válasszon ki egy kapcsolódó fiókot. Koppintson a *Mentés*-re, hogy hozzáadja az identitást.

Ha a fiók manuálisan lett konfigurálva, valószínűleg az identitást is manuálisan kell majd. Írja be a domain nevet, például *gmail.com*, majd koppintson a *Beállítások lekérése*-re. Ha a szolgáltatója támogatja az [automatikus-felfedezést](https://tools.ietf.org/html/rfc6186), FairEmail automatikusan kitölti a hoszt és port számokat, máskülönben kérdezze le a saját szolgáltatójától a megfelelő IMAP hoszt nevet, port számokat és titkosítási protokollt (SSL/TLS vagy STARTTLS).

Az aliasok használatáról lásd [ezt a GYIK-et](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9).

## Engedély megadása - a kapcsolat információk eléréséhez

Ha látni akarja névjegyei címét, fotóit, stb., akkor engedélyeznie kell a FairEmail számára a névjegyekhez való hozzáférést. Nyomjon a *Hozzáférés* gombra, és válassza az *Engedélyezt*.

## Akkumulátorhasználat optimalizálás beállítása - hogy folyamatosan fogadjon leveleket

Újabb Android verziókon az Android az alkalmazásokat alvó módba teszi, amikor ki van kapcsolva a képernyő egy ideig, hogy az akkumulátorhasználatot csökkentse. Ha késések nélkül szeretné megkapni az új e-maileket, akkor ajánlott kikapcsolni az akkumulátor optimalizálást a FairEmail esetében. Nyomjon a *Kezelés* gombra és kövesse az utasításokat.

## Kérdések és problémák

Ha kérdése vagy problémája van, [ezen az oldalon](https://github.com/M66B/FairEmail/blob/master/FAQ.md) talál segítséget.