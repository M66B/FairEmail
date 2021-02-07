# Pomoć pri postavljanju

Postavljanje FairEmail-a prilično je jednostavno. Trebate dodati barem jedan račun da biste primili e-poštu i barem jedan identitet ako želite slati e-poštu. The quick setup will add an account and an identity in one go for most providers.

## Zahtjevi

Za postavljanje računa i identiteta potrebna je internetska veza.

## Brzo postavljanje

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Postavljanje računa - za primanje e-pošte

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). S popisa odaberite provajdera, unesite korisničko ime, koje je uglavnom vaša adresa e-pošte i unesite svoju lozinku. Dodirnite *Provjeri* da biste se omogućili da se FairEmail poveže s poslužiteljem e-pošte i preuzme popis sistemskih mapa. Nakon pregleda izbora mape sustava možete dodati račun dodirom na *Spremi*.

If your provider is not in the list of providers (there are thousands of providers), select *Custom*. Unesite ime domene, primjerice *gmail.com* i dodirnite *Dohvati postavke*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Više o ovome pogledajte [ovdje](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Postavljanje identiteta - za slanje e-pošte

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Unesite ime koje želite da se pojavi u adresi e-pošte koju šaljete i odaberite povezani račun. Dodirnite *Spremi* da biste dodali identitet.

Ako je račun konfiguriran ručno, vjerojatno ćete morati i ručno konfigurirati identitet. Unesite ime domene, primjerice *gmail.com* i dodirnite *Dohvati postavke*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Pogledajte [ovaj FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) o korištenju aliasa.

## Davanje dozvola - za pristup podacima o kontaktima

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Postavljanje optimizacija za baterije - za kontinuirano primanje e-poruka

Na novijim verzijama Androida Android će staviti uređaje za spavanje kad je ekran neko vrijeme isključen kako bi smanjio potrošnju baterije. Ako želite primati nove poruke e-pošte bez odgađanja, trebali biste onemogućiti optimizaciju baterije za FairEmail. Tap *Manage* and follow the instructions.

## Pitanja ili problemi

Ako imate pitanje ili problem, molimo vas da [potražite ovdje](https://github.com/M66B/FairEmail/blob/master/FAQ.md) pomoć.