# Pomoc s nastavením

Nastavenie FairEmail je veľmi jednoduché. Potrebujete pridať aspoň jeden účet pre príjem e-mailov a aspoň jednu identitu ak chcete odosielať e-maily. Rýchle nastavenie umožní pridať účet a identitu jedným ťahom pre väčšinu hlavných poskytovateľov.

## Požiadavky

Internetové pripojenie je potrebné pre nastavenie účtov a identít.

## Rýchle nastavenie

Stačí vybrať príslušného poskytovateľa alebo *Iného poskytovateľa*, zadajte svoje meno, e-mailovú adresu a heslo a ťuknúť na položku *Skontrolovať*.

Toto bude fungovať pre väčšinu poskytovateľov e-mailových služieb.

Ak rýchle nastavenie nefunguje, musíte si účet a identitu nastaviť ručne, pokyny nájdete nižšie.

## Nastaviť účet - pre príjem e-mailov

Ak chcete pridať konto, ťuknite na položku *Ručné nastavenie a ďalšie možnosti*, ťuknite na položku *Konta*, ťuknite na tlačidlo "plus" v spodnej časti a vyberte položku IMAP (alebo POP3). Zvoľte si poskytovateľa zo zoznamu, vložte používateľské meno, ktoré je väčšinou vaša e-mailová adresa a zadajte heslo. Klepnite na *Skontrolovať* aby sa mohol FairEmail pripojiť do e-mailového serveru a vyzdvihnúť zoznam systémových priečinkov. Po skontrolovaní voľby systémového priečinka môžete pridať účet klepnutím na *Uložiť*.

Ak sa váš poskytovateľ nenachádza v zozname poskytovateľov, ktorých sú tisíce, vyberte možnosť *Vlastný*. Vložte názov domény, napríklad *gmail.com* a klepnite na *Získať nastavenia*. Ak váš poskytovateľ podporuje [automatické zisťovanie](https://tools.ietf.org/html/rfc6186), FairEmail vyplní názov hostiteľa a číslo portu, v opačnom prípade skontrolujte pokyny na nastavenie svojho poskytovateľa, aby ste zistili správny názov hostiteľa IMAP, číslo portu a šifrovací protokol (SSL/TLS alebo STARTTLS). Pre viac informácií, pozrite [tu](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Nastaviť identitu - pre odosielanie e-mailov

Podobne, ak chcete pridať identitu, klepnite na *Ručné nastavenie a ďalšie možnosti*, klepnite na *Identity* a klepnite na tlačidlo "plus" v spodnej časti. Vložte meno, ktoré sa má zobrazovať v adresnom poli "Od:" v e-mailoch, ktoré pošlete a zvoľte si pripojený účet. Klepnite na *Uložiť* pre uloženie identity.

Ak bol účet nastavovaný ručne, pravdepodobne budete tiež musieť nastaviť identitu manuálne. Vložte názov domény, napríklad *gmail.com* a klepnite na *Získať nastavenia*. Ak váš poskytovateľ podporuje [automatické zisťovanie](https://tools.ietf.org/html/rfc6186), FairEmail vyplní názov hostiteľa a číslo portu, v opačnom prípade skontrolujte pokyny na nastavenie vášho poskytovateľa, aby ste zistili správny názov hostiteľa SMTP, číslo portu a šifrovací protokol (SSL/TLS alebo STARTTLS).

Viď [tieto často kladené otázky](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) o používaní aliasov.

## Udeliť povolenia - pre prístup ku kontaktným informáciám

Ak chcete vyhľadávať e-mailové adresy, zobrazovať fotografie kontaktov atď., musíte službe FairEmail udeliť povolenie na čítanie kontaktných informácií. Stačí ťuknúť na položku *Dovoliť* a vybrať možnosť *Povoliť*.

## Nastaviť optimalizáciu batérie - pre neustály príjem e-mailov

Na nedávnych verziách Androidu, systém uspí aplikácie pri zhasnutí obrazovky na dlhšiu dobu pre zníženie spotreby batérie. Ak chcete prijímať nové e-maily bez oneskorenia, mali by ste zakázať optimalizáciu batérie pre FairEmail. Ťuknite na položku *Správa* a postupujte podľa pokynov.

## Otázky alebo problémy

Ak máte otázku alebo problém, [pozrite sem](https://github.com/M66B/FairEmail/blob/master/FAQ.md) pre pomoc.