# Manual configuration

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fgithub.com%2FM66B%2FFairEmail%2Fblob%2Fmaster%2Ftutorials%2FMANUAL-CONFIG.md)

**NOTE:** If you have any troubles, check [this extensive FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md).
You may also ask in [this XDA Forum thread](https://forum.xda-developers.com/t/app-5-0-fairemail-fully-featured-open-source-privacy-oriented-email-app.3824168/)
or via [this contact form](https://contact.faircode.eu/?product=fairemailsupport). Bad reviews won't help you or the developer, so please try asking in person first.

**NOTE:** In most cases, an automatic configuration via the wizard described in the [first configuration guide](https://github.com/M66B/FairEmail/blob/master/tutorials/FIRST-CONFIG.md) should work right away. If you are using a custom domain & server and the wizard doesn't work as expected, you can enable automatic configuration by implementing [RFC 6186](https://datatracker.ietf.org/doc/html/rfc6186) within the DNS and / or [Thunderbird:Autoconfiguration](https://wiki.mozilla.org/Thunderbird:Autoconfiguration). An independent guide for both methods can be found in [Auto Discover | LD Documentation](https://doc.ld-online.net/e-mail/auto-discover).

## Accounts & identities

**Accounts** are basically your IMAP and POP3 accounts for reading mails. FairEmail uses your account to fetch _incoming_ mails.

**Identities** are SMTP accounts for sending mails. FairEmail will use the identity to send _outgoing_ mails.

## Adding a new email account

**NOTE:** This step requires a working internet connection.

Adding a new email account (account + identity) is pretty straightforward. Just follow these steps:

1. Open the "Main" settings
2. Click on "Manual setup and account options" below the wizard
3. Press the "Accounts" button
4. Click on the "+" icon to add a new account
5. Select your provider or "custom"
6. Enter your account credentials and modify the server name & port, if necessary
7. Press the "Check" button
8. Check the recognized default folders
9. Save with "Add related identity" enabled
10. Enter the Identity details (e.g. the FROM name and address)

Here are some screenshots to help you visualize these steps:

![Manual setup and account options](images/manualconf-01.png)
![Adding new IMAP / POP3 account](images/manualconf-02.png)
![Saving account](images/manualconf-03.png)

<br>

Thanks @[mkasimd](https://github.com/mkasimd/) for contributing this documentation.
