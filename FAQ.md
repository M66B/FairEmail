# FairEmail support

If you have a question, please check the frequently asked questions below first.
At the bottom you can find how to ask other questions, request features and report bugs.


## Authorizing accounts

For support on authorizing an account you should consult the documentation of your provider.
Searching for *IMAP* and the name of the provider is mostly sufficient to find the right documentation.
To setup an account you need the IMAP and SMTP server addresses and port numbers,
whether STARTTLS should be used and your username and password.
In some cases you'll need to enable external access to your account and/or to use a special (app) password,
for instance when two factor authentication is enabled.

For authorizing:

* Gmail / G suite: see [question 6](#user-content-faq6)
* Outlook: see [question 14](#user-content-faq14)
* Microsoft Exchange: see [question 8](#user-content-faq8)


## Planned features

None at this moment.

Anything on this list is in random order and *might* be added in the near future.


## Known problems

None at this moment.


## Frequently requested features

* Swipe left/right to go to previous/next message: besides that swiping left/right is already being used to move messages to archive/trash, swiping also selects message text, so this will not work reliably.
* Rich text editor: besides that very few people would use this on a small mobile device, Android doesn't support a rich text editor and most rich text editor open source projects are abandoned.
* Widget to read e-mail: widgets can have limited user interaction only, so a widget to read e-mail would not be very useful. Moreover, it would be not very useful to duplicate functions which are already available in the app.
* Executing filter rules: filter rules should be executed on the server because a battery powered device with possibly an unstable internet connection is not suitable for this.
* Resize images: this is not a feature directly related to email and there are plenty of apps that can do this for you.
* Calendar events: opening the attached calendar file should open the related calendar app.
* Snooze/send timer: snoozing and delaying sending is not supported by [IMAP](https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol).
* Badge count: there is no standard Android API for this and third party solutions might stop working anytime. For example *ShortcutBadger* [has lots of problems](https://github.com/leolin310148/ShortcutBadger/issues). You can use the provided widget instead.
* Switch language: although it is possible to change the language of an app, Android is not designed for this. Better fix the translation in your language if needed, see [this FAQ](#user-content-faq26) about how to.
* Select identities to show in unified inbox: this would add complexity for something which would hardly be used.
* Better design: please let me know what you have in mind [in this forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168).
* Hide archived messages: hiding archived messages which exists in other folders too would have a performance impact.
* S/MIME encryption: only PGP encryption will be supported, see [this FAQ](#user-content-faq12) for more information.
* ActiveSync: there are no maintained, open source libraries providing the ActiveSync protocol, so this cannot be added.
* Automatically go to the next message on deleting a message: since the 'next' message can either be an older or a newer message this would be confusing. You can disable auto closing in the advanced options and use the bottom navigation bar instead.

Since FairEmail is meant to be privacy friendly, the following will not be added:

* Open links without confirmation
* Show original message without confirmation
* Hide addresses by default: addresses play an important role in determining if a message is authentic
* Direct file/folder access: for security/privacy reasons apps should use the [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider)

Confirmation or hiding the addresses section is just one tap, which is just a small price for better privacy.
Note that your contacts could unknowingly send malicious messages if they got infected with malware.


## Frequently Asked Questions

* [(1) Which permissions are needed and why?](#user-content-faq1)
* [(2) Why is there a permanent notification shown?](#user-content-faq2)
* [(3) What are operations and why are they pending?](#user-content-faq3)
* [(4) How can I use an invalid security certificate / IMAP STARTTLS / an empty password?](#user-content-faq4)
* [(5) How can I customize the message view?](#user-content-faq5)
* [(6) How can I login to Gmail / G suite?](#user-content-faq6)
* [(7) Why are messages in the outbox not moved to the sent folder?](#user-content-faq7)
* [(8) Can I use a Microsoft Exchange account?](#user-content-faq8)
* [(9) What are identities?](#user-content-faq9)
* [(11) Why is POP not supported?](#user-content-faq11)
* [~~(10) What does 'UIDPLUS not supported' mean?~~](#user-content-faq10)
* [(12) How does encryption/decryption work?](#user-content-faq12)
* [(13) How does search on server work?](#user-content-faq13)
* [(14) How can I setup Outlook with 2FA?](#user-content-faq14)
* [(15) Why does the message text keep loading?](#user-content-faq15)
* [(16) Why are messages not being synchronized?](#user-content-faq16)
* [~~(17) Why does manual synchronize not work?~~](#user-content-faq17)
* [(18) Why is the message preview not always shown?](#user-content-faq18)
* [(19) Why are the pro features so expensive?](#user-content-faq19)
* [(20) Can I get a refund?](#user-content-faq20)
* [(21) How do I enable the notification light?](#user-content-faq21)
* [(22) Why do I get 'Couldn't connect to host' ?](#user-content-faq22)
* [(23) Why do I get 'Too many simultaneous connections' ?](#user-content-faq23)
* [(24) What is browse messages on the server?](#user-content-faq24)
* [(25) Why can't I select/open/save an image, attachment or a file?](#user-content-faq25)
* [(26) Can I help to translate FairEmail in my own language?](#user-content-faq26)
* [(27) How can I differentiate external and embedded images?](#user-content-faq27)
* [(28) How can I manage status bar notifications?](#user-content-faq28)
* [(29) How can I get new message notifications for other folders?](#user-content-faq29)
* [(30) How can I use the provided quick settings?](#user-content-faq30)
* [(31) How can I use the provided shortcuts?](#user-content-faq31)
* [(32) How can I check if reading email is really safe?](#user-content-faq32)
* [(33) Why are edited sender addresses not working?](#user-content-faq33)
* [(34) How are identities matched?](#user-content-faq34)
* [(35) Why should I be careful with viewing images, attachments and the original message?](#user-content-faq35)
* [(36) How are settings files encrypted?](#user-content-faq36)
* [(37) How are passwords stored?](#user-content-faq37)
* [(38) Can you help me restore my Play store purchase?](#user-content-faq38)
* [(39) How can I reduce the battery usage of FairEmail?](#user-content-faq39)
* [(40) How can I reduce the network usage of FairEmail?](#user-content-faq40)
* [(41) How can I fix the error 'Handshake failed' ?](#user-content-faq41)
* [(42) Can you add a new provider to the list of providers?](#user-content-faq42)
* [(43) Can you show the original ... ?](#user-content-faq43)
* [(44) Can you show contact photos / identicons in the sent folder?](#user-content-faq44)
* [(45) How can I fix 'This key is not available. To use it, you must import it as one of your own!' ?](#user-content-faq45)
* [(46) Why does the message list keep refreshing?](#user-content-faq46)
* [(47) How can I solve 'No primary account or no drafts folder' ?](#user-content-faq47)
* [~~(48) How can I solve 'No primary account or no archive folder' ?~~](#user-content-faq48)
* [(49) How can I fix 'An outdated app sent a file path instead of a file stream' ?](#user-content-faq49)
* [(50) Can you add an option to synchronize all messages?](#user-content-faq50)
* [(51) How are folders sorted?](#user-content-faq51)
* [(52) Why does it take some time to reconnect to an account?](#user-content-faq52)
* [(53) Can you stick the message action bar to the top/bottom?](#user-content-faq53)
* [(54) How do I use a namespace prefix?](#user-content-faq54)
* [(55) How can I mark all messages as read / move or delete all messages?](#user-content-faq55)
* [(56) Can you add support for JMAP?](#user-content-faq56)
* [(57) Can I use HTML in signatures?](#user-content-faq57)
* [(58) What does an open/closed email icon mean?](#user-content-faq58)
* [(59) Can original messages be opened in the browser?](#user-content-faq59)
* [(60) Did you known ...?](#user-content-faq60)

[I have another question.](#support)

<a name="faq1"></a>
**(1) Which permissions are needed and why?**

* have full network access (INTERNET): to send and receive email
* view network connections (ACCESS_NETWORK_STATE): to monitor internet connectivity changes
* run at startup (RECEIVE_BOOT_COMPLETED): to start monitoring on device start
* in-app billing (BILLING): to allow in-app purchases
* foreground service (FOREGROUND_SERVICE): to run a foreground service on Android 9 Pie and later, see also the next question
* prevent device from sleeping (WAKE_LOCK): to keep the device awake while synchronizing messages
* Optional: read your contacts (READ_CONTACTS): to autocomplete addresses and to show photos
* Use accounts on the device (USE_CREDENTIALS): needed to select accounts on Android version 5.1 Lollipop and before (not used on later Android versions)
* Optional: find accounts on the device (GET_ACCOUNTS): to use [OAuth](https://en.wikipedia.org/wiki/OAuth) instead of passwords

<br />

<a name="faq2"></a>
**(2) Why is there a permanent notification shown?**

A permanent status bar notification with the number of accounts being synchronized, the number of messages to send and the number of operations pending (see next question) is shown
to prevent Android from killing the service that takes care of receiving and sending email.

Most, if not all, other email apps don't show a notification with the "side effect" that new email is often not or late being reported.

Background: this is necessary because of the introduction of [doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby) in Android 6 Marshmallow.

If you came here by clicking on the notification, you should known that the next click will open the unified inbox.

<br />

<a name="faq3"></a>
**(3) What are operations and why are they pending?**

The low priority status bar notification shows the number of pending operations, which can be:

* *add*: add message to remote folder
* *move*: move message to another remote folder
* *delete*: delete message from remote folder
* *send*: send message
* *seen*: mark message as read/unread in remote folder
* *answered*: mark message as answered in remote folder
* *flag*: add/remove star in remote folder
* *keyword*: add/remove IMAP flag in remote folder
* *headers*: download message headers
* *body*: download message text
* *attachment*: download attachment
* *sync*: synchronize local and remove folder

Operations are processed only when there is a connection to the email server or when manually synchronizing.
See also [this FAQ](#user-content-faq16).

<br />

<a name="faq4"></a>
**(4) How can I use an invalid security certificate / IMAP STARTTLS / an empty password?**

Invalid security certificate: you should try to fix this by contacting your provider or by getting a valid security certificate
because invalid security certificates are insecure and allow [man-in-the-middle attacks](https://en.wikipedia.org/wiki/Man-in-the-middle_attack).
If money is an obstacle, you can get free security certificates from [Let’s Encrypt](https://letsencrypt.org).

IMAP STARTTLS: the EFF [writes](https://www.eff.org/nl/deeplinks/2018/06/announcing-starttls-everywhere-securing-hop-hop-email-delivery):
"*Additionally, even if you configure STARTTLS perfectly and use a valid certificate, there’s still no guarantee your communication will be encrypted.*"

Empty password: your username is likely easily guessed, so this is very insecure.

If you still want to use an invalid security certificate, IMAP STARTTLS or an empty password,
you'll need to enable insecure connections in the account and/or identity settings.

<br />

<a name="faq5"></a>
**(5) How can I customize the message view?**

In the advanced settings you can enable or disable:

* *unified inbox*: to disable the unified inbox and to list the folders selected for the unified inbox instead
* *conversation threading*: to disable conversation threading and to show individual messages instead
* *compact message view*: for more condensed message items and a smaller message text font
* *show contact photos*: to hide contact photos
* *show identicons*: to show generated contact avatars
* *show message preview*: to show two lines of the message text

If the list of addresses is long, you can collapse the addresses section with the *less* icon at the top of the addresses section.

Unfortunately, it is impossible to make everybody happy and adding lots of settings would not only be confusing, but also never be sufficient.

<br />

<a name="faq6"></a>
**(6) How can I login to Gmail / G suite?**

Preferably select Gmail as provider and select an account on your device.

To login to Gmail / G suite you'll often need an app password, for example when two factor authentication is enabled.
See here for instructions: [https://support.google.com/accounts/answer/185833](https://support.google.com/accounts/answer/185833).

If this doesn't work, see here for more solutions: [https://support.google.com/mail/accounts/answer/78754](https://support.google.com/mail/accounts/answer/78754)

<br />

<a name="faq7"></a>
**(7) Why are messages in the outbox not moved to the sent folder?**

Messages in the outbox are moved to the sent folder as soon as your provider adds the message to the sent folder.
Note that this requires a sent folder to be selected and to be set to synchronizing.
If this doesn't happen, your provider might not keep track of sent messages or you might be using an SMTP server not related to the provider.
In these cases you can use the advanced identity setting *Store a copy of sent messages in* and select the sent folder.
There is a menu to move sent messages in the outbox to the sent folder.

<br />

<a name="faq8"></a>
**(8) Can I use a Microsoft Exchange account?**

You can use a Microsoft Exchange account if it is accessible via IMAP.
See here for more information: [https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793)

Please see [here](#frequently-requested-features) about ActiveSync support.

<br />

<a name="faq9"></a>
**(9) What are identities?**

Identities represent email addresses you are sending *from*.

Some providers allow you to have multiple email aliases.
You can configure these by setting the email address field to the alias address and setting the user name field to your main email address.

<br />

<a name="faq10"></a>
**~~(10) What does 'UIDPLUS not supported' mean?~~**

~~The error message *UIDPLUS not supported* means that your email provider does not provide the IMAP [UIDPLUS extension](https://tools.ietf.org/html/rfc4315).
This IMAP extension is required to implement two way synchronization, which is not an optional feature.
So, unless your provider can enable this extension, you cannot use FairEmail for this provider.~~

<br />

<a name="faq11"></a>
**(11) Why is POP not supported?**

Besides that any decent email provider supports [IMAP](https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol) these days,
using [POP](https://en.wikipedia.org/wiki/Post_Office_Protocol) will result in unnecessary battery usage and delayed new message notifications.
Moreover, POP is unsuitable for two way synchronization and more often than not people read email on different devices.

<br />

<a name="faq12"></a>
**(12) How does encryption/decryption work?**

First of all you need to install and configure [OpenKeychain](https://f-droid.org/en/packages/org.sufficientlysecure.keychain/).
To encrypt a message before sending, just select the menu *Encrypt*. Similarly, to decrypt a received message, just select the menu *Decrypt*.
Encryption is [Autocrypt](https://autocrypt.org/) compatible. For security reasons received messages are not decrypted automatically.
Encryption/decryption is a pro feature.

Sending inline PGP encrypted messages is not supported, see [here](https://josefsson.org/inline-openpgp-considered-harmful.html) about why not.

S/MIME is not supported because it is not used much and because key management is complex.
There are also [security concerns](https://security.stackexchange.com/a/83752).

Note that signed only or encrypted only messages are not supported, see here for some considerations:

* [OpenPGP Considerations Part I](https://k9mail.github.io/2016/11/24/OpenPGP-Considerations-Part-I.html)
* [OpenPGP Considerations Part II](https://k9mail.github.io/2017/01/30/OpenPGP-Considerations-Part-II.html)
* [OpenPGP Considerations Part III Autocrypt](https://k9mail.github.io/2018/02/26/OpenPGP-Considerations-Part-III-Autocrypt.html)

If you like, you can verify a signature by opening the *signature.asc* attachment.

<br />

<a name="faq13"></a>
**(13) How does search on server work?**

You can start searching for messages on sender, recipient, subject, keyword or message text by using the magnify glass in the action bar of a folder (not in the unified inbox because it could be a collection of folders).
First local messages will be searched and after that the server will execute the search.
Searching local messages is case insensitive and on partial text.
The message text of local messages will not be searched if the message text was not downloaded yet.
Searching by the server might be case sensitive or case insensitive and might be on partial text or whole words, depending on the provider.
Searching messages is a pro feature.

<br />

<a name="faq14"></a>
**(14) How can I setup Outlook with 2FA?**

To use Outlook with two factor authentication enabled, you need to create an app password.
See [here](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification) for the details.

Unfortunately, Outlook doesn't properly support OAuth for IMAP/SMTP connections, so there is no other way.

<br />

<a name="faq15"></a>
**(15) Why does the message text keep loading?**

The message header and message body are fetched separately from the server.
The message text of larger messages is not being pre-fetched on metered connections and need to be fetched on opening the message.
The message text will keep loading if there is no connection to the account, see also the next question.

In the advanced settings you can set the maximum size for automatically downloading of messages on metered connections.

Mobile connections are almost always metered and some (paid) Wi-Fi hotspots are too.

<br />

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

<br />

<a name="faq17"></a>
**~~(17) Why does manual synchronize not work?~~**

~~If the *Synchronize now* menu is dimmed, there is no connection to the account.~~

~~See the previous question for more information.~~

<br />

<a name="faq18"></a>
**(18) Why is the message preview not always shown?**

The preview of the message text cannot be shown if the message body has not been downloaded yet.
See also [this FAQ](#user-content-faq15).

<br />

<a name="faq19"></a>
**(19) Why are the pro features so expensive?**

The right question is "*why are there so many taxes and fees?*":

* VAT: 25% (depending on your country)
* Google fee: 30%
* Income tax: 50%

So, what is left for the developer is just a fraction of what you pay.

Note that only some convenience and advanced features, like the dark/black theme, need to be purchased,
which means that FairEmail is basically free to use.

Also note that most free apps will appear not to be sustainable in the end, whereas FairEmail is properly maintained and supported,
and that free apps may have a catch, like sending privacy sensitive information to the internet.

<br />

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

<br />

<a name="faq22"></a>
**(22) Why do I get 'Couldn't connect to host' ?**

The message *Couldn't connect to host ...* means that FairEmail was not able to connect to the email server.

Possible causes are:

* A firewall is blocking connections to the server
* The email server is refusing to accept the connection
* The host name or port number is invalid
* The are problems with the internet connection

If you are using a VPN, the VPN provider might block the connection because it is too aggressively trying to prevent spam.

<br />

<a name="faq23"></a>
**(23) Why do I get 'Too many simultaneous connections' ?**

The message *Too many simultaneous connections* is sent by the email server
when there are too many folder connections for the same email account at the same time.

Possible causes are:

* There are multiple email clients connected to the same account
* The same email client is connected multiple times to the same account
* The previous connection was terminated abruptly for example by abruptly losing internet connectivity, for example when turning on flight mode

If only FairEmail is connecting to the email server, first try to wait half an hour to see if the problem resolves itself,
else enable the folder settings '*Poll instead of synchronize*' for some folders.
The poll interval can be configured in the account settings.

The maximum number of simultaneous folder connections for Gmail is 15,
so you can synchronize at most 15 folders simultaneously on *all* your devices at the same time.
See [here](https://support.google.com/mail/answer/7126229) for details.

<br />

<a name="faq24"></a>
**(24) What is browse messages on the server?**

Browse messages on the server will fetch messages from the email server in real time
when you reach the end of the list of synchronized messages, even when the folder is set to not synchronize.
You can disable this feature under *Setup* > *Advanced options* > *Browse messages on the server*.

<br />

<a name="faq25"></a>
**(25) Why can't I select/open/save an image, attachment or a file?**

If a menu item to select/open/save a file is disabled (dimmed) or not available,
the [storage access framework](https://developer.android.com/guide/topics/providers/document-provider),
a standard Android component, is probably not present,
for example because your custom ROM does not include it or because it was removed.
FairEmail does not request storage permissions, so this framework is required to select files and folders.
No app, except maybe file managers, targeting Android 4.4 KitKat or later should ask for storage permissions because it would allow access to *all* files.

<br />

<a name="faq26"></a>
**(26) Can I help to translate FairEmail in my own language?**

Yes, you can translate the texts of FairEmail in your own language [here](https://crowdin.com/project/open-source-email).
Registration is free.

<br />

<a name="faq27"></a>
**(27) How can I differentiate external and embedded images?**

External image:

![External image](https://raw.githubusercontent.com/google/material-design-icons/master/image/1x_web/ic_image_black_48dp.png)

Embedded image:

![Embedded image](https://raw.githubusercontent.com/google/material-design-icons/master/image/1x_web/ic_photo_library_black_48dp.png)

Broken image:

![Broken image](https://raw.githubusercontent.com/google/material-design-icons/master/image/1x_web/ic_broken_image_black_48dp.png)

Note that downloading external images from a remote server can be used to record you did see a message, which you likely don't want if the message is spam or malicious.

<br />

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

On Android before Android 8 Oreo you can set the notification sound in the advanced options.

See [this FAQ](#user-content-faq21) if your device has a notification light.

<br />

<a name="faq29"></a>
**(29) How can I get new message notifications for other folders?**

Just long press a folder, select *Edit properties*,
and enable either *Show in unified inbox*
or *Notify new messages* (available on Android 7 Nougat and later only)
and tap *Save*.

<br />

<a name="faq30"></a>
**(30) How can I use the provided quick settings?**

There are quick settings (settings tiles) available to:

* globally enable/disable synchronization
* show the number of new messages and marking them as seen (not read)

Quick settings require Android 7.0 Nougat or later.
The usage of settings tiles is explained [here](https://support.google.com/android/answer/9083864).

<br />

<a name="faq31"></a>
**(31) How can I use the provided shortcuts?**

There are shortcuts available to:

* compose a message
* setup accounts, identities, etc

Shortcuts require Android 7.1 Nougat or later.
The usage of shortcuts is explained [here](https://support.google.com/android/answer/2781850).

<br />

<a name="faq32"></a>
**(32) How can I check if reading email is really safe?**

You can use the [Email Privacy Tester](https://www.emailprivacytester.com/) for this.

<br />

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

<br />

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

<br />

<a name="faq35"></a>
**(35) Why should I be careful with viewing images, attachments and the original message?**

Viewing remotely stored images (see also [this FAQ](#user-content-faq27)) might not only tell the sender that you have seen the message,
but will also leak your IP address.

Opening attachments or viewing an original message might execute scripts,
that might not only cause privacy sensitive information to leak, but can also be a security risk.

<br />

<a name="faq36"></a>
**(36) How are settings files encrypted?**

Short version: AES 256 bit

Long version:

* The 256 bit key is generated with *PBKDF2WithHmacSHA1* using a 128 bit secure random salt and 65536 iterations
* The cipher is *AES/CBC/PKCS5Padding*

<br />

<a name="faq37"></a>
**(37) How are passwords stored?**

Providers require passwords in plain text, so the background service that takes care of synchronizing messages needs to send passwords in plain text.
Since encrypting passwords would require a secret and the background service needs to know this secret, this could only be done by storing that secret.
Storing a secret together with encrypted passwords would not add anything, so passwords are stored in plain text in a safe, inaccessible place.
Recent Android versions encrypt all data anyway.

<br />

<a name="faq38"></a>
**(38) Can you help me restore my Play store purchase?**

Google manages all purchases, so as developer I have no control over purchases.
So, the only thing I can do, is give some advice:

* Make sure you have an active, working internet connection
* Make sure the Google Play store / Play services are not blocked in any way, for example by a firewall
* Make sure you are logged in with the right Google account and that there is nothing wrong with your Google account
* Open the Play store application and wait at least a minute to give it time to synchronize with the Google servers
* Open FairEmail to check the purchase again

Note that:

* Purchases are stored in the Google cloud and cannot get lost
* There is no time limit on purchases, so they cannot expire
* Google does not expose details (name, e-mail, etc) about buyers to developers
* An application like FairEmail cannot select which Google account to use

If you cannot solve the problem with the purchase, you will have to contact Google about it.

<br />

<a name="faq39"></a>
**(39) How can I reduce the battery usage of FairEmail?**

First of all, update to [the latest version](https://github.com/M66B/open-source-email/releases/).

It is inevitable that synchronizing messages will use battery power because it requires network access and accessing the messages database.

Reconnecting to an email server will use extra battery power, so an unstable internet connection will result in extra battery usage.

Recent Android versions by default report *app usage* as a percentage in the Android battery settings screen.
Confusingly, *app usage* is not the same as *battery usage*.
The app usage will be very high because FairEmail is using a foreground service which is considered as constant app usage by Android.
However, this doesn't mean that FairEmail is constantly using battery power.
The real battery usage can be seen by using the three dot overflow menu *Show full device usage*.
As a rule of thumb the battery usage should be below or in any case not be much higher than *Mobile network standby*.
If this isn't the case, please let me know.

Most of the battery usage, not considering viewing messages, is due to synchronization (receiving and sending) of messages.
So, to reduce the battery usage, set the number of days to synchronize message for to a lower value,
especially if there are a lot of recent messages in a folder.
Long press a folder name in the folders list to access this setting.

If you have at least once a day internet connectivity, it is sufficient to synchronize messages just for one day.

Note that you can set the number of days to *keep* messages for to a higher number than to *synchronize* messages for.
You could for example initially synchronize messages for a large number of days and after this has been completed
reduce the number of days to synchronize messages for, but leave the number of days to keep messages for.

Starred messages will always be synchronized,
which will allow you to keep older messages around while synchronizing messages for a limited number of days.

Disabling the folder option *Automatically download message texts and attachments*
will result in less network traffic and thus less battery usage.
You could disable this option for example for the sent folder and the archive.

If you got the message *This provider does not support push messages* while configuring an account,
consider switching to a modern provider which supports push messages (IMAP IDLE) to reduce battery usage.

If your device has an [AMOLED](https://en.wikipedia.org/wiki/AMOLED) screen,
you can save battery usage while viewing messages by switching to the black theme (this is a pro feature).

<br />

<a name="faq40"></a>
**(40) How can I reduce the network usage of FairEmail?**

You can reduce the network usage basically in the same way as reducing battery usage, see the previous question for suggestions.

Additionally, you can set FairEmail to download small messages and attachments on a metered (mobile, paid) connection only
or let FairEmail connect via unmetered connections only.
These advanced settings are accessible via *Setup* > *Advanced options*.

<br />

<a name="faq41"></a>
**(41) How can I fix the error 'Handshake failed' ?**

There are several possible causes, so please read to the end of this answer.

The error '*Handshake failed ... WRONG_VERSION_NUMBER*' might mean that you are trying to connect to an IMAP or SMTP server
without an encrypted connection, typically using port 143 (IMAP) and port 25 (SMTP).

Most providers provide encrypted connections using different ports, typically port 993 (IMAP) and port 465/587 (SMTP).

If your provider doesn't support encrypted connections, you should ask to make this possible.
If this isn't an option, you could enable *Allow insecure connections* both in the advanced settings AND the account/identity settings.

See also [this FAQ](#user-content-faq4).

The error '*Handshake failed ... SSLV3_ALERT_ILLEGAL_PARAMETER*' is either caused by a bug in the SSL protocol implementation
or by a too short DH key on the email server and can unfortunately not be fixed by FairEmail.

<br />

<a name="faq42"></a>
**(42) Can you add a new provider to the list of providers?**

If the provider is used by more than a few people, yes, with pleasure.

The following information is needed:

```
<provider
	name="Gmail"
	link="https://support.google.com/mail/answer/7126229" // setup instructions
	type="com.google"> // this is not needed
	<imap
		host="imap.gmail.com"
		port="993"
		starttls="false" />
	<smtp
		host="smtp.gmail.com"
		port="465"
		starttls="false" />
```

Connections *without* [STARTTLS](https://en.wikipedia.org/wiki/Opportunistic_TLS) are safer and therefore prefered.

Please make sure receiving and sending messages works properly.

See below about how to contact me.

<br />

<a name="faq43"></a>
**(43) Can you show the original ... ?**

Show original, shows the original message as the sender has sent it, including original margins, styling, etc.
FairEmail does and will not alter this in any way.

<br />

<a name="faq44"></a>
**(44) Can you show contact photos / identicons in the sent folder?**

Contact photos and identicons are always shown for the sender because this is necessary for conversation threads.
Getting contact photos for both the sender and receiver is not really an option because getting contact photo is an expensive operation.

<br />

<a name="faq45"></a>
**(45) How can I fix 'This key is not available. To use it, you must import it as one of your own!' ?**

You'll get the message *This key is not available. To use it, you must import it as one of your own!*
when trying to decrypt a message with a public key. To fix this you'll need to import the private key.

<br />

<a name="faq46"></a>
**(46) Why does the message list keep refreshing?**

If you see a 'spinner' at the top of the message list, the folder is still being synchronized with the remote server.
You can see the progress of the synchronization in the folder list. See the legend about what the icons and numbers mean.

The speed of your device and internet connection and the number of days to synchronize messages for determine how long synchronization will take.
Note that you shouldn't set the number of days to synchronize messages for to more than one day in most cases, see also [this FAQ](#user-content-faq39).

<br />

<a name="faq47"></a>
**(47) How can I solve 'No primary account or no drafts folder' ?**

You'll get the error message *No primary account or no drafts folder* when trying to compose a message
while there is no account set to be the primary account or when there is no drafts folder selected for the primary account.
This can happen for example when you start FairEmail to compose a message from another app.
FairEmail needs to know where to store the draft,
so you'll need to select one account to be the primary account and/or you'll need to select a drafts folder for the primary account.

<br />

<a name="faq48"></a>
**~~(48) How can I solve 'No primary account or no archive folder' ?~~**

~~You'll get the error message *No primary account or no archive folder* when searching for messages from another app.
FairEmail needs to know where to search,
so you'll need to select one account to be the primary account and/or you'll need to select a archive folder for the primary account.~~

<br />

<a name="faq49"></a>
**(49) How can I fix 'An outdated app sent a file path instead of a file stream' ?**

You likely selected or sent an attachment or image with an outdated file manager or an outdated app that assumes all apps still have storage permissions.
For security and privacy reasons modern apps like FairEmail have no full access to all files anymore.
This can result into the error message *An outdated app sent a file path instead of a file stream*
when a file name instead of a file stream is presented to FairEmail.
You can fix this by switching to an up-to-date file manager or an app designed for recent Android versions.

See also [question 25](#user-content-faq25).

<br />

<a name="faq50"></a>
**(50) Can you add an option to synchronize all messages?**

A synchronize all (download all) messages will not be added
because it can easily result in out of memory errors and the available storage space filling up.
It can also easily result in a lot of battery and data usage.
Mobile devices are just not very suitable to download and store years of messages.
You can better use the search on server function (see [question 13](#user-content-faq13)), which is faster and more efficient.
Note that searching through a lot of messages stored locally would only delay searching and use extra battery power.

<br />

<a name="faq51"></a>
**(51) How are folders sorted?**

Folders are sorted with special, system folders on top, followed by folders set to synchronize.
Within each category the folders are sorted on name.

Some providers prefix some folders with INBOX, but these folders are in fact just user folders, so they are sorted below the special, system folders.
It is not possible to make an exception for this because some other providers prefix all folders with INBOX.

Note that you can give folders a display name by long pressing on a folder name, which can be useful because the display name will be used for sorting.

<br />

<a name="faq52"></a>
**(52) Why does it take some time to reconnect to an account?**

There is no reliable way to know if an account connection was terminated gracefully or forcefully.
Trying to reconnect to an account while the account connection was terminated forcefully too often can result in problems
like [too many simultaneous connections](#user-content-faq23) or even the account being blocked.
To prevent such problems, FairEmail waits 90 seconds until trying to reconnect again.

<br />

<a name="faq53"></a>
**(53) Can you stick the message action bar to the top/bottom?**

The message action bar works on a message and the bottom action bar works on the conversation.
Since there is often more than one message in a conversation, this is not possible.

<br />

<a name="faq54"></a>
**(54) How do I use a namespace prefix?**

A namespace prefix is used to automatically remove the prefix providers sometimes add to folder names.

For example the Gmail spam folder is called:

```
[Gmail]/Spam
```

By setting the namespace prefix to *[Gmail]* FairEmail will automatically remove *[Gmail]/* from all folder names.

<br />

<a name="faq55"></a>
**(55) How can I mark all messages as read / move or delete all messages?**

You can use multiple select for this.
Long press the first message, don't lift your finger and slide down to the last message.
Then use the three dot action button to execute the desired action.

<br />

<a name="faq56"></a>
**(56) Can you add support for JMAP?**

There are almost no providers offering the [JMAP](https://jmap.io/) protocol,
so it is not worth to add support for this to FairEmail.

<br />

<a name="faq57"></a>
**(57) Can I use HTML in signatures?**

Yes, you can use HTML in signatures if you paste HTML formatted text into the signature field or use the *Edit as HTML* button.

See [here](https://stackoverflow.com/questions/44410675/supported-html-tags-on-android-textview) for which HTML tags are supported.

You can for example past this into the signature field:

This is *italic*, this is *bold* and this is [a link](https://example.org).

<br />

<a name="faq58"></a>
**(58) What does an open/closed email icon mean?**

The email icon in the folder list can be open (outlined) or closed (solid):

![External image](https://raw.githubusercontent.com/google/material-design-icons/master/communication/1x_web/ic_mail_outline_black_48dp.png)

Message bodies and attachments are not downloaded by default.

![External image](https://raw.githubusercontent.com/google/material-design-icons/master/communication/1x_web/ic_email_black_48dp.png)

Message bodies and attachments are downloaded by default.

<br />

<a name="faq59"></a>
**(59) Can original messages be opened in the browser?**

For security reasons the files with the original message texts are not accessible to other apps, so this is not possible.
In theory the [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) could be used to share these files,
but even Google's Chrome cannot handle this.

<br />

<a name="faq60"></a>
**(60) Did you know ... ?**

* Did you know that you can long press the 'write message' icon to go to the drafts folder?
* Did you know that you can long press the account name in the navigation menu to go to the inbox of that account?
* Did you know there is an advanced option to mark messages read when they are moved and that archiving and trashing is also moving?
* Did you know that you can select text (or an email address) in any app on recent Android versions and let FairEmail search for it? You'll need to set a primary account and an archive folder for this to work, so FairEmail knows where to search. There will be 'FairEmail' in the menu with copy, cut, etc.
* Did you know that FairEmail has a tablet mode? Rotate your device in landscape mode and conversation threads will be opened in a second column if there is enough screen space.

<br />


## Support

If you have another question, want to request a feature or report a bug, you can use [this forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168).
Registration is free.

If you are a supporter of the project, you can get limited personal support by using [this form](https://contact.faircode.eu/?product=fairemail%2B).
