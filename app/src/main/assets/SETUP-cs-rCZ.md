# Nápověda k nastavení

Nastavení FairEmailu je poměrně jednoduché. Potřebujete přidat alespoň jeden účet, abyste mohli e-mailové zprávy přijímat, a alespoň jednu identitu, pokud chcete e-maily odesílat. Rychlé nastavení přidá účet i identitu v jediném kroku pro většinu hlavních poskytovatelů.

## Požadavky

Pro nastavení účtů a identit je nutné připojení k internetu.

## Rychlé nastavení

Stačí zvolit vhodného poskytovatele nebo *Jiný poskytovatel* a zadat své jméno, e-mailovou adresu a heslo, a stisknout *Ověřit*.

Toto funguje pro většinu e-mailových poskytovatelů.

Pokud rychlé nastavení nezafunguje, budete účet a identitu muset nastavit ručně, viz. instrukce níže.

## Nastavení účtu - pro příjem e-mailů

Pro přidání účtu stiskněte *Ruční nastavení a další možnosti*, dále *Účty*, poté tlačítko „plus“ v dolní části a vyberte IMAP (nebo POP3). Zvolte poskytovatele ze seznamu, zadejte uživatelské jméno, což je obvykle vaše e-mailová adresa, a zadejte své heslo. Stiskem *Ověřit* nechte FairEmail připojit se k e-mailovému serveru a načíst seznam systémových složek. Po kontrole vybraných systémových složek můžete účet přidat stisknutím *Uložit*.

Pokud váš poskytovatel není na seznamu poskytovatelů (existují tisíce poskytovatelů), vyberte *Vlastní*. Zadejte název domény, například *gmail.com* a stiskněte *Získat nastavení*. Podporuje-li Váš poskytovatel [automatické zjišťování](https://tools.ietf.org/html/rfc6186), FairEmail automaticky předvyplní název hostitele a číslo portu. V opačném případě postupujte dle instrukcí svého poskytovatele pro nastavení IMAP - správného názvu hostitele, čísla portu a šifrovacího protokolu (SSL/TLS nebo STARTTLS). Pro více informací se prosím podívejte [sem](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Nastavení identity - pro odesílání e-mailů

Podobně, chcete-li přidat identitu, stiskněte *Ruční nastavení a další možnosti*, dále *Identity* a poté tlačítko „plus“ v dolní části. Zadejte jméno, které si přejete zobrazit v adresním poli „Od:“ u Vámi odeslaných e-mailů a zvolte propojený účet. Stiskněte *Uložit* pro přidání identity.

Pokud byl účet nastaven ručně, pravděpodobně bude potřeba nastavit ručně i identitu. Zadejte název domény, například *gmail.com* a stiskněte *Získat nastavení*. Podporuje-li Váš poskytovatel [automatické zjišťování](https://tools.ietf.org/html/rfc6186), FairEmail automaticky předvyplní název hostitele a číslo portu. V opačném případě postupujte dle instrukcí svého poskytovatele pro nastavení SMTP - správného názvu hostitele, čísla portu a šifrovacího protokolu (SSL/TLS nebo STARTTLS).

Přečtěte si [tyto časté dotazy](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) o používání aliasů.

## Udělení oprávnění - pro přístup k informacím kontaktů

Pro hledání e-mailových adres, zobrazení fotografií kontaktů, apod., musíte FairEmailu udělit oprávnění ke čtení kontaktních informací. Stačí stisknout *Udělit* a zvolit *Povolit*.

## Nastavení optimalizace výdrže baterie - pro průběžný příjem e-mailů

V posledních verzích Androidu jsou aplikace uspávány pro snížení spotřeby baterie, pokud je obrazovka po nějaký čas vypnuta. Chcete-li přijímat nové e-maily bez prodlení, měli byste pro FairEmail zakázat optimalizaci výdrže baterie. Stiskněte *Spravovat* a postupujte podle pokynů.

## Otázky či problémy

Máte-li dotaz nebo problém, podívejte se prosím [sem](https://github.com/M66B/FairEmail/blob/master/FAQ.md) pro pomoc.