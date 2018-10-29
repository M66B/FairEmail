# FairEmail

If you have a question, please check the frequently asked questions below first.
At the bottom you can find how to ask other questions, request features and report bugs.

## Frequently Asked Questions

<a name="FAQ1"></a>
**(1) Which permissions are needed and why?**

* have full network access (INTERNET): to send and receive email
* view network connections (ACCESS_NETWORK_STATE): to monitor internet connectivity changes
* run at startup (RECEIVE_BOOT_COMPLETED): to start monitoring on device start
* in-app billing (BILLING): to allow in-app purchases
* foreground service (FOREGROUND_SERVICE): to run a foreground service on Android 9 Pie and later, see also the next question
* prevent device from sleeping (WAKE_LOCK): to keep the device awake while synchronizing messages
* Optional: read your contacts (READ_CONTACTS): to autocomplete addresses and to show photos
* Optional: find accounts on the device (GET_ACCOUNTS): to use [OAuth](https://en.wikipedia.org/wiki/OAuth) instead of passwords

<a name="FAQ2"></a>
**(2) Why is there a permanent notification shown?**

A permanent status bar notification with the number of accounts being synchronized and the number of operations pending is shown
to prevent Android from killing the service that takes care of receiving and sending email.

Most, if not all, other email apps don't show a notification with the "side effect" that new email is often not or late being reported.

Background: this is because of the introduction of [doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby) in Android 6 Marshmallow.

<a name="FAQ3"></a>
**(3) What are operations and why are they pending?**

The low priority status bar notification shows the number of pending operations, which can be:

* add: add message to remote folder
* move: move message to another remote folder
* delete: delete message from remote folder
* send: send message
* seen: mark message as seen/unseen in remote folder
* flag: star/unstar remote message
* headers: download message headers
* body: download message text
* attachment: download attachment

Operations are processed only when there is a connection to the email server or when manually synchronizing.
See also [this FAQ](#FAQ16).

<a name="FAQ4"></a>
**(4) What is a valid security certificate?**

Valid security certificates are officially signed (not self signed) and have matching a host name.

<a name="FAQ5"></a>
~~**(5) What does 'no IDLE support' mean?**~~

~~Without [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE) emails need to be periodically fetched,~~
~~which is a waste of battery power and internet bandwidth and will delay notification of new emails.~~
~~Since the goal of FairEmail is to offer safe and fast email, providers without IMAP IDLE are not supported.~~
~~You should consider this a problem of the provider, not of the app.~~
~~Almost all email providers offer IMAP IDLE, with as notable exception Yahoo!~~

<a name="FAQ6"></a>
**(6) How can I login to Gmail / G suite?**

Preferably select Gmail as provider and select an account on your device.

To login to Gmail / G suite you'll often need an app password, for example when two factor authentication is enabled.
See here for instructions: [https://support.google.com/accounts/answer/185833](https://support.google.com/accounts/answer/185833).

If this doesn't work, see here for more solutions: [https://support.google.com/mail/accounts/answer/78754](https://support.google.com/mail/accounts/answer/78754)

<a name="FAQ7"></a>
**(7) Why are messages in the outbox not moved to the sent folder?**

Messages in the outbox are moved to the sent folder as soon as your provider adds the message to the sent folder.
If this doesn't happen, your provider might not keep track of sent messages or you might be using an SMTP server not related to the provider.
In these cases you can enable the account option *Store sent messages* to let the app move messages from the outbox to the sent folder after sending.
There is a menu to move sent messages to the sent folder after enabling this option.

<a name="FAQ8"></a>
**(8) Can I use a Microsoft Exchange account?**

You can use a Microsoft Exchange account if it is accessible via IMAP.
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
~~**(11) Why is STARTTLS for IMAP not supported?**~~

~~STARTTLS starts with a not encrypted connection and is therefore not secure.~~
~~All well known IMAP servers support IMAP with a plain SSL connection, so there is no need to support STARTTLS for IMAP.~~
~~If you encounter an IMAP server that requires STARTTLS, please let me know.~~

~~For more background information, please see [this article](https://www.eff.org/nl/deeplinks/2018/06/announcing-starttls-everywhere-securing-hop-hop-email-delivery).~~

~~tl;dr; "*Additionally, even if you configure STARTTLS perfectly and use a valid certificate, there’s still no guarantee your communication will be encrypted.*"~~

<a name="FAQ13"></a>
**(13) How does search on server work?**

You can start searching for messages on sender, recipient, subject or message text by using the magnify glass in the action bar of a folder (not in the unified inbox because it could be a collection of folders).
The server executes the search. Scrolling down will fetch more messages from the server.
Searching by the server might be case sensitive or case insensitive and might be on partial text or whole words, depending on the provider.
Search on server is a pro feature.

<a name="FAQ14"></a>
**(14) How can I setup Outlook with 2FA?**

To use Outlook with two factor authentication enabled, you need to create an app password.
See [here](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification) for the details.

<a name="FAQ15"></a>
**(15) Can you add ... ?**

* More themes: the goal is to keep the app as simple as possible, so no more themes will not be added.
* Preview message text: this is not always possible because the message text is initially not downloaded for larger messages and besides that the subject is supposed to tell what the message is about.
* Filter rules: filter rules should be executed on the server because a battery powered device with possibly an unstable internet connection is not suitable for executing filter rules.

<a name="FAQ16"></a>
**(16) Why are messages not being synchronized?**

Possible causes of messages not being synchronized (sent or received) are:

* The account or folder(s) are not set to synchronize
* The number of days to synchronize is set to low
* There is no usable internet connection
* The email server is temporarily not available
* Android stopped the synchronization service

So, check your account and folder settings and check if the accounts/folders are connected (see the legend menu for the meaning of the icons).

On some devices, where there are lots of applications competing for memory, Android may stop the synchronization service as a last resort.
Some Android versions,
in particular of Huawei (see [here](https://www.forbes.com/sites/bensin/2016/07/04/push-notifications-not-coming-through-to-your-huawei-phone-heres-how-to-fix-it/) for a fix)
or Xiaomi (see [here](https://www.forbes.com/sites/bensin/2016/11/17/how-to-fix-push-notifications-on-xiaomis-miui-8-for-real/) for a fix)
stop apps and services too aggressively.

<a name="FAQ17"></a>
**(17) Why does manual synchronize not work?**

If the *Synchronize now* menu is dimmed, there is no connection to the account.

See the previous question for more information.

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

<a name="FAQ21"></a>
**(21) How do I enable the notification light?**

Before Android 8 Oreo: there is an advanced option in the setup for this.

Android 8 Oreo and later: see [here](https://developer.android.com/training/notify-user/channels) about how to configure notification channels.

<a name="FAQ22"></a>
**(22) Why do I get 'Couldn't connect to host' ?**

The message *Couldn't connect to host ...* means that FairEmail was not able to connect to the email server.

Possible causes are:

* A firewall is blocking connections to the server
* The email server is refusing to accept the connection
* The host name or port number is invalid
* The are problems with the internet connection

If you are using a VPN, the VPN provider might block the connection because it is too aggressively trying to prevent spam.

<a name="FAQ23"></a>
**(23) Why do I get 'Too many simultaneous connections' ?**

The message *Too many simultaneous connections* is sent by the email server when there are too many connections to the same email account at the same time.

Possible causes are:

* There are multiple email clients connected to the same account
* The same email client is connected multiple times to the same account
* The previous connection was terminated abruptly for example by losing internet connectivity

<a name="FAQ24"></a>
**(24) What is browse messages on the server?**

Browse messages on the server will fetch messages from the email server in real time
when you reach the end of the list of synchronized messages, even when the folder is set to not synchronize.
You can disable this feature under *Setup* > *Advanced options* > *Browse messages on the server*.

<a name="FAQ25"></a>
**(25) Why can't I select an image, attachment or a file to export/import?**

If a menu item to select a file is disabled (dimmed),
likely the [storage access framework](https://developer.android.com/guide/topics/providers/document-provider),
a standard Android component, is not present,
for example because your custom ROM does not include it or because it was removed.
FairEmail does not request storage permissions, so this framework is required to select files and folders.
No app, except maybe file managers, targetting Android 4.4 KitKat or later should ask for storage permissions because it would allow access to *all* files.

<a name="FAQ26"></a>
**(26) Can I help to translate FairEmail in my own language?**

Yes, you can translate the texts of FairEmail in your own language [here](https://crowdin.com/project/open-source-email).
Registration is free.

<br>

If you have another question, want to request a feature or report a bug, you can use [this forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168).
Registration is free.
