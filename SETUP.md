# Setup help

Setting up FairEmail is fairly simple.
You'll need to add at least one account to receive email and at least one identity if you want to send email.
The quick setup will add an account and an identity in one go for most major providers.


## Requirements

An internet connection is required to setup accounts and identities.


## Quick setup

Just enter your name, email address and password and tap *Go*.

If you have a Google account, preferably use *Select account* instead of entering an email address and a password.

This will work for most major email providers.

If the quick setup doesn't work, you'll need to setup an account and an identity in another way, see below for instructions.


## Setup account - to receive email

To add an account, tap on *Manage accounts* and tap on the orange *add* button at the bottom.
Select a provider from the list, enter the username, which is mostly your email address and enter your password.
If you use Gmail, tap *Select account* to fill in the username and password.
Tap *Check* to let FairEmail connect to the email server and fetch a list of system folders.
After reviewing the system folder selection you can add the account by tapping *Save*.

If your provider is not in the list of providers, select *Custom*.
Enter the domain name, for example *gmail.com* and tap *Get settings*.
If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number,
else check the setup instructions of your provider for the right IMAP host name and port number.
For more about this, please see [here](https://github.com/M66B/open-source-email/blob/master/FAQ.md#authorizing-accounts).


## Setup identity - to send email

Similarly, to add an identity, tap on *Manage identity* and tap on the orange *add* button at the bottom.
Enter the name you want to appear in de from address of the emails you send and select a linked account.
Tap *Save* to add the identity.

See [this FAQ](https://github.com/M66B/open-source-email/blob/master/FAQ.md#FAQ9) about using aliases.


## Grant permissions - to access contact information

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant read contacts permission to FairEmail.
Just tap *Grant permissions* and select *Allow*.


## Setup battery optimizations - to continuously receive email

On recent Android versions, Android will put apps to sleep when the screen is off for some time to reduce battery usage.
If you want to receive new emails without delays, you should disable battery optimizations for FairEmail.
Tap *Disable battery optimizations* and follow the instructions.


## Questions

If you have a question or problem, please [see here](https://github.com/M66B/open-source-email/blob/master/FAQ.md).
