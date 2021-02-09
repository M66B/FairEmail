# Pomoc s nastavením

Nastavenie FairEmail je veľmi jednoduché. Potrebujete pridať aspoň jeden účet pre príjem e-mailov a aspoň jednu identitu ak chcete odosielať e-maily. The quick setup will add an account and an identity in one go for most major providers.

## Požiadavky

Internetové pripojenie je potrebné pre nastavenie účtov a identít.

## Rýchle nastavenie

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Nastaviť účet - pre príjem e-mailov

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Zvoľte si poskytovateľa zo zoznamu, vložte používateľské meno, ktoré je väčšinou vaša e-mailová adresa a zadajte heslo. Klepnite na *Skontrolovať* aby sa mohol FairEmail pripojiť do e-mailového serveru a vyzdvihnúť zoznam systémových priečinkov. Po skontrolovaní voľby systémového priečinka môžete pridať účet klepnutím na *Uložiť*.

If your provider is not in the list of providers, there are thousands of providers, select *Custom*. Vložte názov domény, napríklad *gmail.com* a klepnite na *Získať nastavenia*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Pre viac informácií, pozrite [tu](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Nastaviť identitu - pre odosielanie e-mailov

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Vložte meno, ktoré sa má zobrazovať v adresnom poli "Od:" v e-mailoch, ktoré pošlete a zvoľte si pripojený účet. Klepnite na *Uložiť* pre uloženie identity.

Ak bol účet nastavovaný ručne, pravdepodobne budete tiež musieť nastaviť identitu manuálne. Vložte názov domény, napríklad *gmail.com* a klepnite na *Získať nastavenia*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Viď [tieto často kladené otázky](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) o používaní aliasov.

## Udeliť povolenia - pre prístup ku kontaktným informáciám

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Nastaviť optimalizáciu batérie - pre neustály príjem e-mailov

Na nedávnych verziách Androidu, systém uspí aplikácie pri zhasnutí obrazovky na dlhšiu dobu pre zníženie spotreby batérie. Ak chcete prijímať nové e-maily bez oneskorenia, mali by ste zakázať optimalizáciu batérie pre FairEmail. Tap *Manage* and follow the instructions.

## Otázky alebo problémy

Ak máte otázku alebo problém, [pozrite sem](https://github.com/M66B/FairEmail/blob/master/FAQ.md) pre pomoc.