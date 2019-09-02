# Nápověda k nastavení

Nastavení FairEmailu je poměrně jednoduché. Potřebujete přidat alespoň jeden účet aby jste mohli e-mailové zprávy přijímat a alespoň jednu identitu pokud chcete e-maily odesílat. Rychlé nastavení přidá účet i identitu v jednom kroku pro většinu hlavních poskytovatelů.

## Požadavky

Pro nastavení účtů a identit je nutné připojení k internetu.

## Rychlé nastavení

Stačí zadat své jméno, e-mailovou adresu a heslo a stisknout *Přejít*.

Toto funguje pro většinu hlavních e-mailových poskytovatelů.

Pokud rychlé nastavení nefunguje, budete účet a identitu muset nastavit jiným způsobem, viz. níže.

## Nastavení účtu - pro příjem e-mailů

To add an account, tap *Manage accounts* and tap the orange *add* button at the bottom. Select a provider from the list, enter the username, which is mostly your email address and enter your password. Tap *Check* to let FairEmail connect to the email server and fetch a list of system folders. After reviewing the system folder selection you can add the account by tapping *Save*.

If your provider is not in the list of providers, select *Custom*. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the hostname and port number, else check the setup instructions of your provider for the right IMAP hostname, port number and protocol (SSL/TLS or STARTTLS). For more about this, please see [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Nastavení identity - pro odesílání e-mailů

Similarly, to add an identity, tap *Manage identity* and tap the orange *add* button at the bottom. Enter the name you want to appear in de from address of the emails you send and select a linked account. Tap *Save* to add the identity.

If the account was configured manually, you likely need to configure the identity manually too. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the hostname and port number, else check the setup instructions of your provider for the right SMTP hostname, port number and protocol (SSL/TLS or STARTTLS).

See [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) about using aliases.

## Grant permissions - to access contact information

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant read contacts permission to FairEmail. Just tap *Grant permissions* and select *Allow*.

## Setup battery optimizations - to continuously receive emails

On recent Android versions, Android will put apps to sleep when the screen is off for some time to reduce battery usage. If you want to receive new emails without delays, you should disable battery optimizations for FairEmail. Tap *Disable battery optimizations* and follow the instructions.

## Otázky nebo problémy

Máte-li nějaký dotaz či problém, podívejte se prosím [sem](https://github.com/M66B/FairEmail/blob/master/FAQ.md) nebo pro požádání o pomoc použijte [tento kontaktní formulář](https://contact.faircode.eu/?product=fairemailsupport) (jako "transaction number" můžete uvést "*setup help*").