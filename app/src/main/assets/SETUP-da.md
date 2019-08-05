# Opsætningshjælp

At opsætte FairEmail er ret enkelt. Du skal tilføje mindst én konto for at modtage e-mail og mindst én identitet for at sende e-mail. Den hurtige opsætning tilføjer på én gang en konto og en identitet for de fleste større udbydere.

## Forudsætninger

En Internetforbindelse kræves for at opsætte konti og identiteter.

## Hurtig opsætning

Angiv blot dit navn, e-mail og adgangskode, og tryk på *Go*.

Dette vil fungere for de fleste større udbydere.

Fungerer hurtig-opsætningen ikke, så opsæt på anden vis en konto og en identitet (se instruktioner nedenfor).

## Opsæt konto - for at modtage e-mail

To add an account, tap on *Manage accounts* and tap on the orange *add* button at the bottom. Select a provider from the list, enter the username, which is mostly your email address and enter your password. Tap *Check* to let FairEmail connect to the email server and fetch a list of system folders. After reviewing the system folder selection you can add the account by tapping *Save*.

If your provider is not in the list of providers, select *Custom*. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and protocol (SSL/TLS or STARTTLS). For more about this, please see [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Set up identity - to send email

Similarly, to add an identity, tap on *Manage identity* and tap on the orange *add* button at the bottom. Enter the name you want to appear in de from address of the emails you send and select a linked account. Tap *Save* to add the identity.

If the account was configured manually, you likely need to configure the identity manually too. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and protocol (SSL/TLS or STARTTLS).

See [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) about using aliases.

## Grant permissions - to access contact information

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant read contacts permission to FairEmail. Just tap *Grant permissions* and select *Allow*.

## Setup battery optimizations - to continuously receive email

On recent Android versions, Android will put apps to sleep when the screen is off for some time to reduce battery usage. If you want to receive new emails without delays, you should disable battery optimizations for FairEmail. Tap *Disable battery optimizations* and follow the instructions.

## Questions or problems

If you have a question or problem, please [see here](https://github.com/M66B/FairEmail/blob/master/FAQ.md) or use [this contact form](https://contact.faircode.eu/?product=fairemailsupport) to ask for help (you can use the transaction number "*setup help*").