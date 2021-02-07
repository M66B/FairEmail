# Pomoč za nastavitev

Nastavitev FairEmaila je dokaj enostavna. Dodati boste morali vsaj en račun za prejemanje e-pošte in vsaj eno identiteto, če želite pošiljati e-pošto. The quick setup will add an account and an identity in one go for most providers.

## Zahteve

Za nastavitev računov in identitet je zahtevana internetna povezava.

## Hitra nastavitev

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Nastavite račun - za prejemanje pošte

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). S seznama izberite ponudnika, vnesite uporabniško ime, ki je običajno vaše e-poštni naslov, in vnesite geslo. Dotaknite se *Preveri*, da FairEmailu omogočite povezavo z e-poštnim strežnikom in pridobitev seznama sistemskih map. Po pregledu izbire sistemskih map lahko račun dodate tako, da se dotaknete *Shrani*.

If your provider is not in the list of providers (there are thousands of providers), select *Custom*. Vnesite ime domene, na primer *gmail.com*, in se dotaknite *Dobi nastavitve*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Za več o tem glejte [tukaj](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Nastavite identiteto - za pošiljanje e-pošte

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Vnesite ime, ki ga želite prikazati v polju za pošiljatelja in izberite povezan račun. Dotaknite se *Shrani*, da dodate identiteto.

Če ste račun ročno nastavili, boste verjetno morali ročno nastaviti tudi identiteto. Vnesite ime domene, na primer *gmail.com*, in se dotaknite *Dobi nastavitve*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Glejte [ta pogosta vprašanja](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) za informacije o uporabi vzdevkov.

## Odobrite dovoljenja - za dostop do podatkov o stikih

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Nastavite optimizacijo baterije - za neprekinjeno prejemanje e-pošte

V novejših različicah bo Android programe preklopil v spanje, kadar je zaslon za nekaj časa izklopljen, da zmanjša porabo energije. Če želite nova sporočila prejemati brez zakasnitev, onemogočite optimizacijo baterije za FairEmail. Tap *Manage* and follow the instructions.

## Vprašanja ali težave

Če imate vprašanje ali težavo, za pomoč glejte [tukaj](https://github.com/M66B/FairEmail/blob/master/FAQ.md).