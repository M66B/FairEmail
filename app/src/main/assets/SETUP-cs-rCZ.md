# Nápověda k nastavení

Nastavení FairEmailu je poměrně jednoduché. Potřebujete přidat alespoň jeden účet, abyste mohli e-mailové zprávy přijímat, a alespoň jednu identitu, pokud chcete e-maily odesílat. The quick setup will add an account and an identity in one go for most providers.

## Požadavky

Pro nastavení účtů a identit je nutné připojení k internetu.

## Rychlé nastavení

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Nastavení účtu - pro příjem e-mailů

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Zvolte poskytovatele ze seznamu, zadejte uživatelské jméno, což je obvykle vaše e-mailová adresa, a zadejte své heslo. Stiskněte *Ověřit* a nechte FairEmail připojit se k e-mailovému serveru a načíst seznam systémových složek. Po kontrole vybraných systémových složek můžete účet přidat stisknutím *Uložit*.

If your provider is not in the list of providers (there are thousands of providers), select *Custom*. Zadejte název domény, například *gmail.com* a stiskněte *Získat nastavení*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Pro více informací se prosím podívejte [sem](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Nastavení identity - pro odesílání e-mailů

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Zadejte jméno, které si přejete zobrazit v adresním poli "Od:" u Vámi odeslaných e-mailů a zvolte připojený účet. Stiskněte *Uložit* pro přidání identity.

Pokud byl účet nastaven ručně, pravděpodobně bude potřeba nastavit ručně i identitu. Zadejte název domény, například *gmail.com* a stiskněte *Získat nastavení*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Přečtěte si [tyto časté dotazy](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) o používání aliasů.

## Udělení oprávnění - pro přístup k informacím kontaktů

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Nastavení optimalizace výdrže baterie - pro průběžný příjem e-mailů

V posledních verzích Androidu, pokud je obrazovka po nějaký čas vypnuta, jsou aplikace uspávány pro snížení spotřeby baterie. Chcete-li přijímat nové e-maily bez prodlení, měli byste pro FairEmail zakázat optimalizaci výdrže baterie. Tap *Manage* and follow the instructions.

## Otázky či problémy

Máte-li dotaz nebo problém, podívejte se prosím [sem](https://github.com/M66B/FairEmail/blob/master/FAQ.md) pro pomoc.