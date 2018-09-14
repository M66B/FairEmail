# FairEmail

If you have a feature request or found a bug, you can rquest or report it [as an issue](https://github.com/M66B/open-source-email/issues).

If you have a question, please check the frequently asked questions below first. At the bottom you can find how to ask other questions.

## Frequently Asked Questions

<a name="FAQ1"></a>
**(1) Which permissions are needed and why?**

* Full network access (INTERNET): to send and receive email
* View network connections (ACCESS_NETWORK_STATE): to monitor internet connectivity changes
* Run at startup (RECEIVE_BOOT_COMPLETED): to start monitoring on device start
* In-app billing (BILLING): to allow in-app purchases
* Foreground service (FOREGROUND_SERVICE): to run a foreground service on Android 9 Pie and later, see also the next question
* Optional: read your contacts (READ_CONTACTS): to autocomplete addresses and to show photos
* Optional: find accounts on the device (GET_ACCOUNTS): to use [OAuth](https://en.wikipedia.org/wiki/OAuth) instead of passwords

<a name="FAQ2"></a>
**(2) Why is there a permanent notification shown?**

A permanent status bar notification with the number of accounts being synchronized and the number of operations pending is shown
to prevent Android from killing the service that takes care of receiving and sending email.

Most, if not all, other email apps don't show a notification with the "side effect" that new email is often not or late being reported.

<a name="FAQ3"></a>
**(3) What are operations and why are they pending?**

The low priority status bar notification shows the number of pending operations, which can be:

* seen: mark message as seen/unseen in remote folder
* add: add message to remote folder
* move: move message to another remote folder
* delete: delete message from remote folder
* send: send message
* attachment: download attachment
* headers: download message headers
* flag: star/unstar remote message

Operations are processed only when there is a connection to the email server or when manually synchronizing.
See also [this FAQ](#FAQ16).

<a name="FAQ4"></a>
**(4) What is a valid security certificate?**

Valid security certificates are officially signed (not self signed) and have matching a host name.

<a name="FAQ5"></a>
**(5) What does 'no IDLE support' mean?**

Without [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE) emails need to be periodically fetched,
which is a waste of battery power and internet bandwidth and will delay notification of new emails.
Since the goal of FairEmail is to offer safe and fast email, providers without IMAP IDLE are not supported.
You should consider this a problem of the provider, not of the app.
Almost all email providers offer IMAP IDLE, with as notable exception Yahoo!

<a name="FAQ6"></a>
**(6) How can I login to Gmail / G suite?**

To login to Gmail / G suite you'll often need an app password, for example when two factor authentication is enabled.
See here for instructions: [https://support.google.com/accounts/answer/185833](https://support.google.com/accounts/answer/185833).

If this doesn't work, see here for more solutions: [https://support.google.com/mail/accounts/answer/78754](https://support.google.com/mail/accounts/answer/78754)

<a name="FAQ7"></a>
**(7) Why are messages in the outbox not moved to the sent folder?**

Messages in the outbox are moved to the sent folder as soon as your provider adds the message to the sent folder.
If this doesn't happen, your provider might not keep track of sent messages or you might be using an SMTP server not related to the provider.
In these cases you can enable the account option *Store sent messages* to let the app move messages from the outbox to the sent folder after sending.

<a name="FAQ8"></a>
**(8) Can I use a Microsoft exchange account?**

If you can use a Microsoft exchange account depends on if the exchange account is accessible via IMAP.
ActiveSync is not supported at this moment.
See here for more information: [https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793)

<a name="FAQ9"></a>
**(9) What are identities?**

Identities represent email addresses you are sending *from*.

Some providers allow you to have multiple email aliases.
You can configure these by setting the email address field to the alias address and setting the user name field to your main email address.

<a name="FAQ10"></a>
**(10) What does 'UIDPLUS not supported' mean?**

The error message *UIDPLUS not supported* means that your email provider does not provide the IMAP [UIDPLUS extension](https://tools.ietf.org/html/rfc4315).
This IMAP extension is required to implement two way synchronization, which is not an optional feature.
So, unless your provider can enable this extension, you cannot use FairEmail for this provider.

<a name="FAQ11"></a>
**(11) Why is STARTTLS for IMAP not supported?**

STARTTLS starts with an unencrypted connection and is therefore not secure.
All known IMAP servers support IMAP with STARTTLS, so there is no need to support STARTTLS for IMAP.
If you encounter an IMAP server that requires STARTTLS, please [create an issue](https://github.com/M66B/open-source-email/issues/new).

<a name="FAQ13"></a>
**(13) How does progressive search work?**

You can start searching for messages on sender, subject or text by using the magnify glass in the action bar of a folder.
First messages are searched on device, then the server is requested to search.
Scrolling down will download more messages from the server.
Searching on device is case insensitive and on partial text.
Searching on the server might be case sensitive or case insensitive and might be on partial text or whole words, depending on the provider.
Progressive search is a pro feature.

<a name="FAQ14"></a>
**(14) How can I setup Outlook with 2FA?**

To use Outlook with two factor authentication enabled, you need to create an app password.
See [here](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification) for the details.

<a name="FAQ15"></a>
**(15) Can you add ... ?**

* More themes: the goal is to keep the app as simple as possible, so this will not be added.
* LED notifications/vibrations: there are less and less devices with a notification light and if there is one, it can mostly be managed by Android, so there is little point in adding support for this.
* Encryption: there is too little interest in sending/receiving encrypted messages to justifiy putting effort into this.
* POP/poll support: besides that any decent provider is supporting / should support IMAP, polling does consume extra battery power and will delay notification of new messages, so this will not be added.
* Multiple select: swiping is easier and doesn't have the risk of accidental touches, so multiple select would not add anything.

<a name="FAQ16"></a>
**(16) Why are messages not being synchronized?**

Possible causes of messages not being synchronized (sent or received) are:

* The account or folder(s) or not set to synchronize
* The number of days to synchronize is set to low
* There is no usable internet connection
* The email server is temporarily not available

So, check your account and folder settings and check if the accounts/folders are connected (see the legend menu for the meaning of the icons).

<a name="FAQ17"></a>
**(17) Why does manual synchronize not work?**

If the *Synchronize now* menu is dimmed, there is no connection to the account.

See the previous question for more information.

<a name="FAQ18"></a>
**(18) When should I enabled 'Store sent messages' ?**

The option *Store sent messages* should be enabled only
when the email server does not store sent messages in the sent folder (it should)
or when an SMTP server not related to the account is being used.

<a name="FAQ19"></a>
**(19) Why are the pro features so expensive?**

The right question is "*why are there so many taxes and fees?*":

* VAT: 25% (depending on your country)
* Google fee: 30%
* Income tax: 50%

So, what is left for the developer is just a fraction of what you pay.

Note that only some convenience and advanced features need to be purchased, which means that FairEmail is basically free to use.

Also note that most free apps will appear not to be sustainable in the end, whereas FairEmail is properly maintained and supported,
and that free apps may have a catch, like sending privacy sensitive information to the internet.

<a name="FAQ20"></a>
**(20) Can I get a refund?**

If a purchased pro feature doesn't work as intended
and this isn't caused by a problem in the free features
and I cannot fix the problem in a timely manner, you can get a refund.
In all other cases there is no refund possible.
In no circumstances there is a refund possible for any problem related to the free features,
since there wasn't paid anything for them and because they can be evaluated without any limitation.
I take my responsibility as seller to deliver what has been promised
and I expect that you take responsibility for informing yourself of what you are buying.

<br>

If you have another question, you can use [this forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168).
Registration is free.
