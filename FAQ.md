# FairEmail

If you have a question, please check the frequently asked questions below first.
At the bottom you can find how to ask other questions, request features and report bugs.


## Authorizing accounts

For support on authorizing an account you should consult the documentation of your provider.
Searching for *IMAP* and the name of the provider is mostly sufficient to find the right documentation.
To setup an account you need the IMAP and SMTP server addresses and port numbers,
whether STARTTLS should be used and your username and password.
In some cases you'll need to enable external access to your account and/or to use a special (app) password,
for instance when two factor authentication is enabled.

For:

* Gmail / G suite: see [question 6](#user-content-faq6)
* Outlook: see [question 14](#user-content-faq14)
* Microsoft Exchange: see [question 8](#user-content-faq8)


## Planned features

* Notifications per account
* Fixed action bar conversations
* Password protected export file
* Signature per identity
* Keep conversations open (for previous/next navigation)
* Microsoft OAuth

Anything on this list is in random order and *might* be added in the near future.


## Frequently requested features

* Previewing message text in notifications: this is not possible because the message text is initially not downloaded.
* Swipe left/right to go to previous/next message: swiping also selects message text, so this would not work reliably.
* Rich text editor: besides that very few people would use this on a small mobile device, Android doesn't support a rich text editor and most rich text editor open source projects are abandoned.
* Widget to read e-mail: widgets can have limited user interaction only, so a widget to read e-mail would not be very useful.
* Executing filter rules: filter rules should be executed on the server because a battery powered device with possibly an unstable internet connection is not suitable for this.
* Resize images: this is not a feature directly related to email and there are plenty of apps that can do this for you.
* Calendar events: opening the attached calendar file should open the related calendar app.
* Snooze timer: snoozed emails are not supported by [IMAP](https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol).
* Badge count: there is no standard Android API for this and third party solutions might stop working anytime.
* Shortcut frequently contacted: Android [doesn't support this anymore](https://developer.android.com/guide/topics/providers/contacts-provider#ObsoleteData).
* Pull down to refresh: new messages are received in real-time, so manual refreshing is not needed, see also [this FAQ](#user-content-faq2).
* Switch language: although it is possible to change the language of an app, Android is not designed for this. Better fix the translation in your language if needed, see [this FAQ](#user-content-faq26) about how to.
* Notification per account: this would result in multiple icons in the status bar and most people don't like this. Note that the account colors are shown in the notifications too.

Since FairEmail is meant to be privacy friendly, the following will not be added:

* Open links without confirmation
* Hide addresses by default: addresses play an important role in determining if a message is authentic

Confirmation or hiding the addresses section is just one tap, which is just a small price for better privacy.

Note that your contacts could unknowingly send malicious messages if they got infected with malware.


## Frequently Asked Questions

<a name="faq1"></a>
**(1) Which permissions are needed and why?**

* have full network access (INTERNET): to send and receive email
* view network connections (ACCESS_NETWORK_STATE): to monitor internet connectivity changes
* run at startup (RECEIVE_BOOT_COMPLETED): to start monitoring on device start
* in-app billing (BILLING): to allow in-app purchases
* foreground service (FOREGROUND_SERVICE): to run a foreground service on Android 9 Pie and later, see also the next question
* prevent device from sleeping (WAKE_LOCK): to keep the device awake while synchronizing messages
* Optional: read your contacts (READ_CONTACTS): to autocomplete addresses and to show photos
* Optional: find accounts on the device (GET_ACCOUNTS): to use [OAuth](https://en.wikipedia.org/wiki/OAuth) instead of passwords

<a name="faq2"></a>
**(2) Why is there a permanent notification shown?**

A permanent status bar notification with the number of accounts being synchronized and the number of operations pending is shown
to prevent Android from killing the service that takes care of receiving and sending email.

Most, if not all, other email apps don't show a notification with the "side effect" that new email is often not or late being reported.

Background: this is necessary because of the introduction of [doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby) in Android 6 Marshmallow.

If you came here by clicking on the notification, you should known that the next click will open the unified inbox.

<a name="faq3"></a>
**(3) What are operations and why are they pending?**

The low priority status bar notification shows the number of pending operations, which can be:

* add: add message to remote folder
* move: move message to another remote folder
* delete: delete message from remote folder
* send: send message
* seen: mark message as seen/unseen in remote folder
* flag: add/remove stars
* headers: download message headers
* body: download message text
* attachment: download attachment

Operations are processed only when there is a connection to the email server or when manually synchronizing.
See also [this FAQ](#user-content-faq16).

<a name="faq4"></a>
**(4) How can I use an invalid security certificate / IMAP STARTTLS / an empty password?**

Invalid security certificate: you should try to fix this by contacting your provider or by getting a valid security certificate
because invalid security certificates are insecure and allow [man-in-the-middle attacks](https://en.wikipedia.org/wiki/Man-in-the-middle_attack).
If money is an obstacle, you can get free security certificates from [Let’s Encrypt](https://letsencrypt.org).

IMAP STARTTLS: the EFF [writes](https://www.eff.org/nl/deeplinks/2018/06/announcing-starttls-everywhere-securing-hop-hop-email-delivery):
"*Additionally, even if you configure STARTTLS perfectly and use a valid certificate, there’s still no guarantee your communication will be encrypted.*"

Empty password: your username is likely easily guessed, so this is very insecure.

If you still want to use an invalid security certificate, IMAP STARTTLS or an empty password,
you'll need to enable insecure connections in the advanced settings and also in the account and/or identity settings.
Additionally, IMAP STARTTLS needs to be enabled in the account settings too.

<a name="faq5"></a>
**(5) How can I customize the message view?**

In the advanced settings you can enable or disable:

* *compact message view*: for more condensed message items and a smaller message text font
* *show contact photos*: to hide contact photos
* *show identicons*: to show generated contact avatars
* *show message preview*: to show two lines of the message text

If the list of addresses is long, you can collapse the addresses section with the *less* icon at the top of the addresses section.

Unfortunately, it is impossible to make everybody happy and adding lots of settings would not only be confusing, but also never be sufficient.

<a name="faq6"></a>
**(6) How can I login to Gmail / G suite?**

Preferably select Gmail as provider and select an account on your device.

To login to Gmail / G suite you'll often need an app password, for example when two factor authentication is enabled.
See here for instructions: [https://support.google.com/accounts/answer/185833](https://support.google.com/accounts/answer/185833).

If this doesn't work, see here for more solutions: [https://support.google.com/mail/accounts/answer/78754](https://support.google.com/mail/accounts/answer/78754)

<a name="faq7"></a>
**(7) Why are messages in the outbox not moved to the sent folder?**

Messages in the outbox are moved to the sent folder as soon as your provider adds the message to the sent folder.
Note that this requires a sent folder to be selected and to be set to synchronizing.
If this doesn't happen, your provider might not keep track of sent messages or you might be using an SMTP server not related to the provider.
In these cases you can use the advanced identity setting *Store a copy of sent messages in* and select the sent folder.
There is a menu to move sent messages in the outbox to the sent folder.

<a name="faq8"></a>
**(8) Can I use a Microsoft Exchange account?**

You can use a Microsoft Exchange account if it is accessible via IMAP.
ActiveSync is not supported at this moment.
See here for more information: [https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793)

<a name="faq9"></a>
**(9) What are identities?**

Identities represent email addresses you are sending *from*.

Some providers allow you to have multiple email aliases.
You can configure these by setting the email address field to the alias address and setting the user name field to your main email address.

<a name="faq10"></a>
**(10) What does 'UIDPLUS not supported' mean?**

The error message *UIDPLUS not supported* means that your email provider does not provide the IMAP [UIDPLUS extension](https://tools.ietf.org/html/rfc4315).
This IMAP extension is required to implement two way synchronization, which is not an optional feature.
So, unless your provider can enable this extension, you cannot use FairEmail for this provider.

<a name="faq11"></a>
**(11) Why is POP not supported?**

Besides that any decent email provider supports [IMAP](https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol) these days,
using [POP](https://en.wikipedia.org/wiki/Post_Office_Protocol) will result in unnecessary battery usage and delayed new message notifications.
Moreover, POP is unsuitable for two way synchronization and more often than not people read email on different devices.

<a name="faq12"></a>
**(12) How does encryption/decryption work?**

First of all you need to install and configure [OpenKeychain](https://f-droid.org/en/packages/org.sufficientlysecure.keychain/).
To encrypt a message before sending, just select the menu *Encrypt*. Similarly, to decrypt a received message, just select the menu *Decrypt*.
Encryption is [Autocrypt](https://autocrypt.org/) compatible. For security reasons received messages are not decrypted automatically.
Encryption/decryption is a pro feature.

Sending inline PGP encrypted messages is not supported, see [here](https://josefsson.org/inline-openpgp-considered-harmful.html) about why not.

Note that signed only or encrypted only messages are not supported, see here for some considerations:

* [OpenPGP Considerations Part I](https://k9mail.github.io/2016/11/24/OpenPGP-Considerations-Part-I.html)
* [OpenPGP Considerations Part II](https://k9mail.github.io/2017/01/30/OpenPGP-Considerations-Part-II.html)
* [OpenPGP Considerations Part III Autocrypt](https://k9mail.github.io/2018/02/26/OpenPGP-Considerations-Part-III-Autocrypt.html)

If you like, you can verify a signature by opening the *signature.asc* attachment.

<a name="faq13"></a>
**(13) How does search on server work?**

You can start searching for messages on sender, recipient, subject or message text by using the magnify glass in the action bar of a folder (not in the unified inbox because it could be a collection of folders).
First local messages will be searched and after that the server will execute the search.
Searching local messages is case insensitive and on partial text.
The message text of local messages will not be searched if the message text was not downloaded yet.
Searching by the server might be case sensitive or case insensitive and might be on partial text or whole words, depending on the provider.
Searching messages is a pro feature.

<a name="faq14"></a>
**(14) How can I setup Outlook with 2FA?**

To use Outlook with two factor authentication enabled, you need to create an app password.
See [here](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification) for the details.

<a name="faq15"></a>
**(15) Why does the message text keep loading?**

The message header and message body are fetched separately from the server.
The message text of larger messages is not being pre-fetched on metered connections and need to be fetched on opening the message.
The message text will keep loading if there is no connection to the account, see also the next question.

In the advanced settings you can set the maximum size for automatically downloading of messages on metered connections.

Mobile connections are almost always metered and some (paid) Wi-Fi hotspots are too.

<a name="faq16"></a>
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

<a name="faq17"></a>
**(17) Why does manual synchronize not work?**

If the *Synchronize now* menu is dimmed, there is no connection to the account.

See the previous question for more information.

<a name="faq18"></a>
**(18) Why is the message preview not always shown?**

The preview of the message text cannot be shown if the message body has not been downloaded yet.
See also [this FAQ](#user-content-faq15).

<a name="faq19"></a>
**(19) Why are the pro features so expensive?**

The right question is "*why are there so many taxes and fees?*":

* VAT: 25% (depending on your country)
* Google fee: 30%
* Income tax: 50%

So, what is left for the developer is just a fraction of what you pay.

Note that only some convenience and advanced features need to be purchased, which means that FairEmail is basically free to use.

Also note that most free apps will appear not to be sustainable in the end, whereas FairEmail is properly maintained and supported,
and that free apps may have a catch, like sending privacy sensitive information to the internet.

<a name="faq20"></a>
**(20) Can I get a refund?**

If a purchased pro feature doesn't work as intended
and this isn't caused by a problem in the free features
and I cannot fix the problem in a timely manner, you can get a refund.
In all other cases there is no refund possible.
In no circumstances there is a refund possible for any problem related to the free features,
since there wasn't paid anything for them and because they can be evaluated without any limitation.
I take my responsibility as seller to deliver what has been promised
and I expect that you take responsibility for informing yourself of what you are buying.

<a name="faq21"></a>
**(21) How do I enable the notification light?**

Before Android 8 Oreo: there is an advanced option in the setup for this.

Android 8 Oreo and later: see [here](https://developer.android.com/training/notify-user/channels) about how to configure notification channels.
You can use the button *Manage notifications* in the setup to directly go to the Android notification settings.

<a name="faq22"></a>
**(22) Why do I get 'Couldn't connect to host' ?**

The message *Couldn't connect to host ...* means that FairEmail was not able to connect to the email server.

Possible causes are:

* A firewall is blocking connections to the server
* The email server is refusing to accept the connection
* The host name or port number is invalid
* The are problems with the internet connection

If you are using a VPN, the VPN provider might block the connection because it is too aggressively trying to prevent spam.

<a name="faq23"></a>
**(23) Why do I get 'Too many simultaneous connections' ?**

The message *Too many simultaneous connections* is sent by the email server when there are too many connections to the same email account at the same time.

Possible causes are:

* There are multiple email clients connected to the same account
* The same email client is connected multiple times to the same account
* The previous connection was terminated abruptly for example by losing internet connectivity

<a name="faq24"></a>
**(24) What is browse messages on the server?**

Browse messages on the server will fetch messages from the email server in real time
when you reach the end of the list of synchronized messages, even when the folder is set to not synchronize.
You can disable this feature under *Setup* > *Advanced options* > *Browse messages on the server*.

<a name="faq25"></a>
**(25) Why can't I select/open/save an image, attachment or a file?**

If a menu item to select/open/save a file is disabled (dimmed),
the [storage access framework](https://developer.android.com/guide/topics/providers/document-provider),
a standard Android component, is probably not present,
for example because your custom ROM does not include it or because it was removed.
FairEmail does not request storage permissions, so this framework is required to select files and folders.
No app, except maybe file managers, targeting Android 4.4 KitKat or later should ask for storage permissions because it would allow access to *all* files.

<a name="faq26"></a>
**(26) Can I help to translate FairEmail in my own language?**

Yes, you can translate the texts of FairEmail in your own language [here](https://crowdin.com/project/open-source-email).
Registration is free.

<a name="faq27"></a>
**(27) Why are images shown without tapping 'Show images' ?**

There are two types of images:

* Images embedded into a message
* Images stored on a remote server

Embedded images, also visible as an attachment, are always shown, but images stored on a remote server need to be downloaded by tapping *Show images*.
Note that downloading images from a remote server can be used to record you did see a message, which you likely don't want if the message is spam or malicious.

<a name="faq28"></a>
**(28) How can I manage status bar notifications?**

In the setup you'll find a button *Manage notifications* to directly navigate to the Android notifications settings for FairEmail.

On Android 8.0 Oreo and later you can manage the properties of the individual notification channels,
for example to set a specific notification sound or to show notifications on the lock screen.

FairEmail has the following notification channels:

* Service: used for the foreground service notification, see also [this FAQ](#user-content-faq2)
* Notifications: used for new message notifications
* Error: used for error notifications

See [here](https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels) for details on notification channels.
In short: tap on the notification channel name to access the channel settings.

See [this FAQ](#user-content-faq21) if your device has a notification light.

<a name="faq29"></a>
**(29) How can I get new message notifications for other folders?**

There are only notifications for new messages in the folders of the unified inbox.
However, you can make any folder part of the unified inbox.
Just long press the folder, select *Edit properties*, enable *Show in unified inbox* and tap *Save*.

<a name="faq30"></a>
**(30) How can I use the provided quick settings?**

There are quick settings (settings tiles) available to:

* globally enable/disable synchronization
* show the number of new messages and marking them as seen (not read)

Quick settings require Android 7.0 Nougat or later.
The usage of settings tiles is explained [here](https://support.google.com/android/answer/9083864).

<a name="faq31"></a>
**(31) How can I use the provided shortcuts?**

There are shortcuts available to:

* compose a message
* setup accounts, identities, etc

Shortcuts require Android 7.1 Nougat or later.
The usage of shortcuts is explained [here](https://support.google.com/android/answer/2781850).

<a name="faq32"></a>
**(32) How can I check if reading email is really safe?**

You can use the [Email Privacy Tester](https://www.emailprivacytester.com/) for this.

<a name="faq33"></a>
**(33) Why are edited sender addresses not working?**

Most providers accept validated addresses only when sending messages to prevent spam.

For example Google modifies the message headers like this:

```
From: Somebody <somebody@example.org>
X-Google-Original-From: Somebody <somebody+extra@example.org>
```

This means that the edited sender address was automatically replaced by a validated address before sending the message.

Note that this is independent of receiving messages.

<a name="faq34"></a>
**(34) How are identities matched?**

Identities are matched on e-mail address in this order:

1. *To* header address
1. *To* header address without extra (see [this FAQ](#user-content-faq33))
1. *From* header address
1. *From* header address without extra
1. *Delivered-To* header address

Matched identities can be used to color code messages. The identity color takes precedence over the account color.
Setting colors is a pro feature.

<a name="faq35"></a>
**(35) Why should I be careful with viewing images, attachments and the original message?**

Viewing remotely stored images (see also [this FAQ](#user-content-faq27)) might not only tell the sender that you have seen the message,
but will also leak your IP address.

Opening attachments or viewing an original message might execute scripts,
that might not only cause privacy sensitive information to leak, but can also be a security risk.


## Support

If you have another question, want to request a feature or report a bug, you can use [this forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168).
Registration is free.
