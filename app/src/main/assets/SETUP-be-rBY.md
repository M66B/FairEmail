# Setup help

Setting up FairEmail is fairly simple. Для атрымання электроннай пошты вам будзе неабходна дадаць хаця б адзін уліковы запіс і хаця б адзін ідэнтыфікатар для яе адпраўкі. Хуткае наладжванне дазволіць за адзін крок хутка дадаць уліковы запіс і ідэнтыфікатар для большасці асноўных пастаўшчыкоў.

## Requirements

An internet connection is required to set up accounts and identities.

## Quick setup

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

Калі хуткае наладжванне не спрацуе, вам прыйдзецца наладзіць уліковы запіс і ідэнтыфікатар уручную. Інструкцыі глядзіце ніжэй.

## Set up account - to receive email

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Select a provider from the list, enter the username, which is mostly your email address and enter your password. Націсніце кнопку *Праверыць*, каб дазволіць FairEmail падключыцца да сервера электроннай пошты і атрымаць спіс сістэмных папак. Пасля выбару сістэмнай папкі вы можаце дадаць уліковы запіс, націснуўшы *Захаваць*.

If your provider is not in the list of providers, there are thousands of providers, select *Custom*. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). For more about this, please see [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Каб адпраўляць электронную пошту наладзьце ідэнтыфікатар

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Enter the name you want to appear in the from address of the emails you send and select a linked account. Tap *Save* to add the identity.

If the account was configured manually, you likely need to configure the identity manually too. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

See [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) about using aliases.

## Grant permissions - to access contact information

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Setup battery optimizations - to continuously receive emails

On recent Android versions, Android will put apps to sleep when the screen is off for some time to reduce battery usage. If you want to receive new emails without delays, you should disable battery optimizations for FairEmail. Tap *Manage* and follow the instructions.

## Questions or problems

If you have a question or problem, please [see here](https://github.com/M66B/FairEmail/blob/master/FAQ.md) for help.