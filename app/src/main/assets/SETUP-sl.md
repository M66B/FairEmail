# Pomoč za nastavitev

Nastavitev FairMaila je dokaj enostavna. Dodati boste morali vsaj en račun za prejemanje e-pošte in vsaj eno identiteto, če želite pošiljati e-pošto. Hitra nastavitev bo dodala račun in identiteto v enem koraku za vse glavne ponudnike.

## Zahteve

Za nastavitev računov in identitet je zahtevana internetna povezava.

## Hitra nastavitev

Vnesite ime, e-poštni naslov in geslo, in se dotaknite *Pojdi*.

To bo delovalo za vse glavne e-poštne ponudnike.

Če hitra nastavitev ne deluje, boste morali račun in identiteto nastaviti na drug način. Glejte spodaj za navodila.

## Nastavite račun - za prejemanje pošte

Za dodajanje računa se dotaknite *Upravljaj z računi* in se spodaj dotaknite oranžnega gumba *Dodaj*. S seznama izberite ponudnika, vnesite uporabniško ime, ki je običajno vaše e-poštni naslov, in vnesite geslo. Dotaknite se *Preveri*, da FairMailu omogočite povezavo z e-poštnim strežnikom in pridobitev seznama sistemskih map. Po pregledu izbire sistemskih map lahko račun dodate tako, da se dotaknete *Shrani*.

Če vašega ponudnika ni na seznamu, izberite *Po meri*. Vnesite ime domene, na primer *gmail.com*, in se dotaknite *Dobi nastavitve*. Če vaš ponudnik podpira [samodejno odkrivanje](https://tools.ietf.org/html/rfc6186), bo FairMail izpolnil ime gostitelja in število vrat. V nasprotnem primeru si za pravilno ime gostitelja IMAP, število vrat in protokol (SSL/TLS ali STARTTLS) oglejte navodila za nastavitev pri vašem ponudniku. Za več o tem glejte [tukaj](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Nastavite identiteto - za pošiljanje e-pošte

Podobno, kot za dodajanje računa, se za dodajanje identitete dotaknite *Upravljaj z identiteto* in se spodaj dotaknite oranžnega gumba *Dodaj*. Vnesite ime, ki ga želite prikazati v polju za pošiljatelja in izberite povezan račun. Dotaknite se *Shrani*, da dodate identiteto.

Če ste račun ročno nastavili, boste verjetno morali ročno nastaviti tudi identiteto. Vnesite ime domene, na primer *gmail.com*, in se dotaknite *Dobi nastavitve*. Če vaš ponudnik podpira [samodejno odkrivanje](https://tools.ietf.org/html/rfc6186), bo FairMail izpolnil ime gostitelja in število vrat. V nasprotnem primeru si za pravilno ime gostitelja SMTP, število vrat in protokol (SSL/TLS ali STARTTLS) oglejte navodila za nastavitev pri vašem ponudniku.

Glejte [ta pogosta vprašanja](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) za informacije o uporabi vzdevkov.

## Odobrite dovoljenja - za dostop do podatkov o stikih

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant read contacts permission to FairEmail. Just tap *Grant permissions* and select *Allow*.

## Setup battery optimizations - to continuously receive emails

On recent Android versions, Android will put apps to sleep when the screen is off for some time to reduce battery usage. If you want to receive new emails without delays, you should disable battery optimizations for FairEmail. Tap *Disable battery optimizations* and follow the instructions.

## Questions or problems

If you have a question or problem, please [see here](https://github.com/M66B/FairEmail/blob/master/FAQ.md) for help.