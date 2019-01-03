# Setup help

Setting up FairEmail is fairly simple.
You'll need to add at least one account to receive email and at least one identity if you want to send email.


## Requirements

An internet connection is required to setup accounts and identities.


## Quick setup

Just enter your name, email address and password and tap *Quick setup*.

If you have a Google account, you can use *Select account* instead of entering an email address and a password.

This will work for most major email providers.

If the quick setup doesn't work, you'll need to setup an account and an identity in another way, see below for instructions.

To use Gmail, you'll need to enable access for "less secure" apps,
see [here](https://support.google.com/accounts/answer/6010255) for Google's instructions
or go [directy to the setting](https://www.google.com/settings/security/lesssecureapps).
Less secure is relative and other providers just allow access in the same way,
but if you are concerned you can complete the setup by setting up an account and an identity in another way too, see below.


## Account - to receive email

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


## Identity - to send email

Similarly, to add an identity, tap on *Manage identity* and tap on the orange *add* button at the bottom.
Enter the name you want to appear in de from address of the emails you send and select a linked account.
Tap *Save* to add the identity.

See [this FAQ](https://github.com/M66B/open-source-email/blob/master/FAQ.md#FAQ9) about using aliases.


## Permissions - to access contact information

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant read contacts permission to FairEmail.
Just tap *Grant permissions* and select *Allow*.


## Battery optimizations - to continuously receive email

On recent Android versions, Android will put apps to sleep when the screen is off for some time to reduce battery usage.
If you want to receive new emails without delays, you should disable battery optimizations for FairEmail.
Tap *Disable battery optimizations* and follow the instructions.


## Questions

If you have a question or problem, please [see here](https://github.com/M66B/open-source-email/blob/master/FAQ.md).
