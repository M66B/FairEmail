<a name="top"></a>
# FairEmail support

If you have a question, please check the following frequently asked questions first.
[At the bottom](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-get-support),
you can find out how to ask other questions, request features, and report bugs.

Wenn Sie eine Frage haben, überprüfen Sie bitte zuerst die nachstehenden häufig gestellten Fragen (FAQ).
[Ganz unten erfahren Sie](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-get-support),
wie Sie weitere Fragen stellen, Funktionen anfordern und Fehler melden können.

## Index

* [Authorizing accounts](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-authorizing-accounts)
* [How to ...?](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-howto)
* [Known problems](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-known-problems)
* [Planned features](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-planned-features)
* [Frequently requested features](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-frequently-requested-features)
* [Frequently Asked Questions](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-frequently-asked-questions)
* [Get support](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-get-support)

## Authorizing accounts

In most cases, the quick setup wizard will be able to automatically identify the correct configuration.

If the quick setup wizard fails, you'll need to manually set up an account (to receive email) and an identity (to send email).
For this you'll need the IMAP and SMTP server addresses and port numbers, whether SSL/TLS or STARTTLS should be used
and your username (mostly, but not always, your email address) and your password.

Searching for *IMAP* and the name of the provider is mostly sufficient to find the right documentation.

In some cases, you'll need to enable external access to your account and/or to use a special (app) password,
for instance when two-factor authentication is enabled.

For authorizing:

* Gmail / G suite, see [question 6](#user-content-faq6)
* Outlook / Live / Hotmail, see [question 14](#user-content-faq14)
* Office 365, see [question 14](#user-content-faq156)
* Microsoft Exchange, see [question 8](#user-content-faq8)
* Yahoo, AOL and Sky, see [question 88](#user-content-faq88)
* Apple iCloud, see [question 148](#user-content-faq148)
* Free.fr, see [question 157](#user-content-faq157)

Please see [here](#user-content-faq22) for common error messages and solutions.

Related questions:

* [Is OAuth supported?](#user-content-faq111)
* [Why is ActiveSync not supported?](#user-content-faq133)

<a name="howto">

## How to ...?

* Change the account name: Settings, tap Manual setup, tap Accounts, tap account
* Change the swipe left/right target: Settings, tab page Behavior, Set swipe actions
* Change password: Settings, tap Manual setup, tap Accounts, tap account, change password
* Set a signature: Settings, tap Manual setup, tap Identities, tap identity, Edit signature.
* Add CC and BCC addresses: tap the people's icon at the end of the subject
* Go to the next/previous message on archive/delete: in the behavior settings disable *Automatically close conversations* and select *Go to next/previous conversation* for *On closing a conversation*
* Add a folder to the unified inbox: long press the folder in the folder list and tick *Show in unified inbox*
* Add a folder to the navigation menu: long press the folder in the folder list and tick *Show in navigation menu*
* Load more messages: long press a folder in the folder list, select *Fetch more messages*
* Delete a message, skipping trash: long press the trash icon
* Delete an account/identity: Settings, tap Manual setup, tap Accounts/Identities, tap the account/identity, trash icon top right
* Delete a folder: long press the folder in the folder list, Edit properties, trash icon top right
* Undo send: Outbox, swipe the message in the list left or right
* Store sent messages in the inbox: please [see this FAQ](#user-content-faq142)
* Change system folders: Settings, tap Manual setup, tap Accounts, tap account, at the bottom
* Export/import settings: Settings, navigation (left side) menu

## Known problems

* ~~A [bug in Android 5.1 and 6](https://issuetracker.google.com/issues/37054851) causes apps to sometimes show a wrong time format. Toggling the Android setting *Use 24-hour format* might temporarily solve the issue. A workaround was added.~~
* ~~A [bug in Google Drive](https://issuetracker.google.com/issues/126362828) causes files exported to Google Drive to be empty. Google has fixed this.~~
* ~~A [bug in AndroidX](https://issuetracker.google.com/issues/78495471) causes FairEmail to occasionally crash on long pressing or swiping. Google has fixed this.~~
* ~~A [bug in AndroidX ROOM](https://issuetracker.google.com/issues/138441698) causes sometimes a crash with "*... Exception while computing database live data ... Couldn't read row ...*". A workaround was added.~~
* A [bug in Android](https://issuetracker.google.com/issues/119872129) causes FairEmail to crash with "*... Bad notification posted ...*" on some devices once after updating FairEmail and tapping on a notification.
* A [bug in Android](https://issuetracker.google.com/issues/62427912) sometimes causes a crash with "*... ActivityRecord not found for ...*" after updating FairEmail. Reinstalling ([source](https://stackoverflow.com/questions/46309428/android-activitythread-reportsizeconfigurations-causes-app-to-freeze-with-black)) might fix the problem.
* A [bug in Android](https://issuetracker.google.com/issues/37018931) sometimes causes a crash with *... InputChannel is not initialized ...* on some devices.
* ~~A [bug in LineageOS](https://review.lineageos.org/c/LineageOS/android_frameworks_base/+/265273) sometimes causes a crash with *... java.lang.ArrayIndexOutOfBoundsException: length=...; index=... ...*.~~
* A bug in Nova Launcher on Android 5.x causes FairEmail to crash with a *java.lang.StackOverflowError* when Nova Launcher has access to the accessibility service.
* ~~The folder selector sometimes shows no folders for yet unknown reasons. This seems to be fixed.~~
* ~~A [bug in AndroidX](https://issuetracker.google.com/issues/64729576) makes it hard to grap the fast scroller. A workaround was added.~~
* ~~Encryption with YubiKey results into an infinite loop. This seems to be caused by a [bug in OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2507).~~
* Scrolling to an internally linked location in original messages does not work. This can't be fixed because the original message view is contained in a scrolling view.
* A preview of a message text doesn't (always) appear on Samsung watches because [setLocalOnly](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder.html#setLocalOnly(boolean)) seem to be ignored. Message preview texts are known to be displayed correctly on Pebble 2, Fitbit Charge 3, Mi band 3, and Xiaomi Amazfit BIP wearables. See also [this FAQ](#user-content-faq126).
* A [bug in Android 6.0](https://issuetracker.google.com/issues/37068143) causes a crash with *... Invalid offset: ... Valid range is ...* when text is selected and tapping outside of the selected text. This bug has been fixed in Android 6.0.1.
* Internal (anchor) links will not work because original messages are shown in an embedded WebView in a scrolling view (the conversation list). This is an Android limitation which cannot be fixed or worked around.
* Language detection [is not working anymore](https://issuetracker.google.com/issues/173337263) on Pixel devices with (upgraded to?) Android 11

## Planned features

* ~~Synchronize on demand (manual)~~
* ~~Semi-automatic encryption~~
* ~~Copy message~~
* ~~Colored stars~~
* ~~Notification settings per folder~~
* ~~Select local images for signatures~~ (this will not be added because it requires image file management and because images are not shown by default in most email clients anyway)
* ~~Show messages matched by a rule~~
* ~~[ManageSieve](https://tools.ietf.org/html/rfc5804)~~ (there are no maintained Java libraries with a suitable license and without dependencies and besides that, FairEmail has its own filter rules)
* ~~Search for messages with/without attachments~~ (this cannot be added because IMAP doesn't support searching for attachments)
* ~~Search for a folder~~ (filtering a hierarchical folder list is problematic)
* ~~Search suggestions~~
* ~~[Autocrypt Setup Message](https://autocrypt.org/autocrypt-spec-1.0.0.pdf) (section 4.4)~~ (IMO it is not a good idea to let an email client handle sensitive encryption keys for an exceptional use case while OpenKeychain can export keys too)
* ~~Generic unified folders~~
* ~~New per account message notification schedules~~ (implemented by adding a time condition to rules so messages can be snoozed during selected periods)
* ~~Copy accounts and identities~~
* ~~Pinch zoom~~ (not reliably possible in a scrolling list; the full message view can be zoomed instead)
* ~~More compact folder view~~
* ~~Compose lists and tables~~ (this requires a rich text editor, see [this FAQ](#user-content-faq99))
* ~~Pinch zoom text size~~
* ~~Display GIFs~~
* ~~Themes~~ (a grey light and dark theme were added because this is what most people seems to want)
* ~~Any day time condition~~ (any day doesn't really fit into the from/to date/time condition)
* ~~Send as attachment~~
* ~~Widget for selected account~~
* ~~Remind to attach files~~
* ~~Select domains to show images for~~ (this will be too complicated to use)
* ~~Unified starred messages view~~ (there is already a special search for this)
* ~~Move notification action~~
* ~~S/MIME support~~
* ~~Search for settings~~

Anything on this list is in random order and *might* be added in the near future.

## Frequently requested features

The design is based on many discussions and if you like you can discuss about it [in this forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168) too.
![1618746203399](https://user-images.githubusercontent.com/82519618/115144268-4f5de400-a03b-11eb-85d8-c8186c7857a7.jpg)

![1618746264978](https://user-images.githubusercontent.com/82519618/115144289-74525700-a03b-11eb-84b9-77ce3672e3e2.jpg)
The goal of the design is to be minimalistic (no unnecessary menus, buttons, etc) and non distracting (no fancy colors, animations, etc).
All displayed things should be useful in one or another way and should be carefully positioned for easy usage.
Fonts, sizes, colors, etc should be material design whenever possible.

## Frequently Asked Questions

* [(1) Which permissions are needed and why?](#user-content-faq1)
* [(2) Why is there a permanent notification shown?](#user-content-faq2)
* [(3) What are operations and why are they pending?](#user-content-faq3)
* [(4) How can I use an invalid security certificate / empty password / plain text connection?](#user-content-faq4)
* [(5) How can I customize the message view?](#user-content-faq5)
* [(6) How can I login to Gmail / G suite?](#user-content-faq6)
* [(7) Why are sent messages not appearing (directly) in the sent folder?](#user-content-faq7)
* [(8) Can I use a Microsoft Exchange account?](#user-content-faq8)
* [(9) What are identities / how do I add an alias?](#user-content-faq9)
* [~~(11) Why is POP not supported?~~](#user-content-faq11)
* [~~(10) What does 'UIDPLUS not supported' mean?~~](#user-content-faq10)
* [(12) How does encryption/decryption work?](#user-content-faq12)
* [(13) How does search on device/server work?](#user-content-faq13)
* [(14) How can I set up an Outlook / Live / Hotmail account?](#user-content-faq14)
* [(15) Why does the message text keep loading?](#user-content-faq15)
* [(16) Why are messages not being synchronized?](#user-content-faq16)
* [~~(17) Why does manual synchronize not work?~~](#user-content-faq17)
* [(18) Why is the message preview not always shown?](#user-content-faq18)
* [(19) Why are the pro features so expensive?](#user-content-faq19)
* [(20) Can I get a refund?](#user-content-faq20)
* [(21) How do I enable the notification light?](#user-content-faq21)
* [(22) What does account/folder error ... mean?](#user-content-faq22)
* [(23) Why do I get alert .. ?](#user-content-faq23)
* [(24) What is browse messages on the server?](#user-content-faq24)
* [(25) Why can't I select/open/save an image, attachment or a file?](#user-content-faq25)
* [(26) Can I help to translate FairEmail in my own language?](#user-content-faq26)
* [(27) How can I distinguish between embedded and external images?](#user-content-faq27)
* [(28) How can I manage status bar notifications?](#user-content-faq28)
* [(29) How can I get new message notifications for other folders?](#user-content-faq29)
* [(30) How can I use the provided quick settings?](#user-content-faq30)
* [(31) How can I use the provided shortcuts?](#user-content-faq31)
* [(32) How can I check if reading email is really safe?](#user-content-faq32)
* [(33) Why are edited sender addresses not working?](#user-content-faq33)
* [(34) How are identities matched?](#user-content-faq34)
* [(35) Why should I be careful with viewing images, attachments, the original message, and opening links?](#user-content-faq35)
* [(36) How are settings files encrypted?](#user-content-faq36)
* [(37) How are passwords stored?](#user-content-faq37)
* [(39) How can I reduce the battery usage of FairEmail?](#user-content-faq39)
* [(40) How can I reduce the data usage of FairEmail?](#user-content-faq40)
* [(41) How can I fix the error 'Handshake failed' ?](#user-content-faq41)
* [(42) Can you add a new provider to the list of providers?](#user-content-faq42)
* [(43) Can you show the original ... ?](#user-content-faq43)
* [(44) Can you show contact photos / identicons in the sent folder?](#user-content-faq44)
* [(45) How can I fix 'This key is not available. To use it, you must import it as one of your own!' ?](#user-content-faq45)
* [(46) Why does the message list keep refreshing?](#user-content-faq46)
* [(47) How do I solve the error 'No primary account or no drafts folder' ?](#user-content-faq47)
* [~~(48) How do I solve the error 'No primary account or no archive folder' ?~~](#user-content-faq48)
* [(49) How do I fix 'An outdated app sent a file path instead of a file stream' ?](#user-content-faq49)
* [(50) Can you add an option to synchronize all messages?](#user-content-faq50)
* [(51) How are folders sorted?](#user-content-faq51)
* [(52) Why does it take some time to reconnect to an account?](#user-content-faq52)
* [(53) Can you stick the message action bar to the top/bottom?](#user-content-faq53)
* [~~(54) How do I use a namespace prefix?~~](#user-content-faq54)
* [(55) How can I mark all messages as read / move or delete all messages?](#user-content-faq55)
* [(56) Can you add support for JMAP?](#user-content-faq56)
* [(57) Can I use HTML in signatures?](#user-content-faq57)
* [(58) What does an open/closed email icon mean?](#user-content-faq58)
* [(59) Can original messages be opened in the browser?](#user-content-faq59)
* [(60) Did you known ...?](#user-content-faq60)
* [(61) Why are some messages shown dimmed?](#user-content-faq61)
* [(62) Which authentication methods are supported?](#user-content-faq62)
* [(63) How are images resized for displaying on screens?](#user-content-faq63)
* [~~(64) Can you add custom actions for swipe left/right?~~](#user-content-faq64)
* [(65) Why are some attachments shown dimmed?](#user-content-faq65)
* [(66) Is FairEmail available in the Google Play Family Library?](#user-content-faq66)
* [(67) How can I snooze conversations?](#user-content-faq67)
* [~~(68) Why can Adobe Acrobat reader not open PDF attachments / Microsoft apps not open attached documents?~~](#user-content-faq68)
* [(69) Can you add auto scroll up on new message?](#user-content-faq69)
* [(70) When will messages be auto expanded?](#user-content-faq70)
* [(71) How do I use filter rules?](#user-content-faq71)
* [(72) What are primary accounts/identities?](#user-content-faq72)
* [(73) Is moving messages across accounts safe/efficient?](#user-content-faq73)
* [(74) Why do I see duplicate messages?](#user-content-faq74)
* [(75) Can you make an iOS, Windows, Linux, etc version?](#user-content-faq75)
* [(76) What does 'Clear local messages' do?](#user-content-faq76)
* [(77) Why are messages sometimes shown with a small delay?](#user-content-faq77)
* [(78) How do I use schedules?](#user-content-faq78)
* [(79) How do I use synchronize on demand (manual)?](#user-content-faq79)
* [~~(80) How do I fix the error 'Unable to load BODYSTRUCTURE'?~~](#user-content-faq80)
* [~~(81) Can you make the background of the original message dark in the dark theme?~~](#user-content-faq81)
* [(82) What is a tracking image?](#user-content-faq82)
* [(84) What are local contacts for?](#user-content-faq84)
* [(85) Why is an identity not available?](#user-content-faq85)
* [~~(86) What are 'extra privacy features'?~~](#user-content-faq86)
* [(87) What does 'invalid credentials' mean?](#user-content-faq87)
* [(88) How can I use a Yahoo, AOL or Sky account?](#user-content-faq88)
* [(89) How can I send plain text only messages?](#user-content-faq89)
* [(90) Why are some texts linked while not being a link?](#user-content-faq90)
* [~~(91) Can you add periodical synchronization to save battery power?~~](#user-content-faq91)
* [(92) Can you add spam filtering, verification of the DKIM signature and SPF authorization?](#user-content-faq92)
* [(93) Can you allow installation/data storage on external storage media (sdcard)?](#user-content-faq93)
* [(94) What does the red/orange stripe at the end of the header mean?](#user-content-faq94)
* [(95) Why are not all apps shown when selecting an attachment or image?](#user-content-faq95)
* [(96) Where can I find the IMAP and SMTP settings?](#user-content-faq96)
* [(97) What is 'cleanup' ?](#user-content-faq97)
* [(98) Why can I still pick contacts after revoking contacts permissions?](#user-content-faq98)
* [(99) Can you add a rich text or markdown editor?](#user-content-faq99)
* [(100) How can I synchronize Gmail categories?](#user-content-faq100)
* [(101) What does the blue/orange dot at the bottom of the conversations mean?](#user-content-faq101)
* [(102) How can I enable auto rotation of images?](#user-content-faq102)
* [(103) How can I record audio?](#user-content-faq158)
* [(104) What do I need to know about error reporting?](#user-content-faq104)
* [(105) How does the roam-like-at-home option work?](#user-content-faq105)
* [(106) Which launchers can show a badge count with the number of unread messages?](#user-content-faq106)
* [(107) How do I use colored stars?](#user-content-faq107)
* [~~(108) Can you add permanently delete messages from any folder?~~](#user-content-faq108)
* [~~(109) Why is 'select account' available in official versions only?~~](#user-content-faq109)
* [(110) Why are (some) messages empty and/or attachments corrupted?](#user-content-faq110)
* [(111) Is OAuth supported?](#user-content-faq111)
* [(112) Which email provider do you recommend?](#user-content-faq112)
* [(113) How does biometric authentication work?](#user-content-faq113)
* [(114) Can you add an import for the settings of other email apps?](#user-content-faq114)
* [(115) Can you add email address chips?](#user-content-faq115)
* [~~(116) How can I show images in messages from trusted senders by default?~~](#user-content-faq116)
* [(117) Can you help me restore my purchase?](#user-content-faq117)
* [(118) What does 'Remove tracking parameters' exactly?](#user-content-faq118)
* [~~(119) Can you add colors to the unified inbox widget?~~](#user-content-faq119)
* [(120) Why are new message notifications not removed on opening the app?](#user-content-faq120)
* [(121) How are messages grouped into a conversation?](#user-content-faq121)
* [~~(122) Why is the recipient name/email address show with a warning color?~~](#user-content-faq122)
* [(123) What will happen when FairEmail cannot connect to an email server?](#user-content-faq123)
* [(124) Why do I get 'Message too large or too complex to display'?](#user-content-faq124)
* [(125) What are the current experimental features?](#user-content-faq125)
* [(126) Can message previews be sent to my wearable?](#user-content-faq126)
* [(127) How can I fix 'Syntactically invalid HELO argument(s)'?](#user-content-faq127)
* [(128) How can I reset asked questions, for example to show images?](#user-content-faq128)
* [(129) Are ProtonMail, Tutanota supported?](#user-content-faq129)
* [(130) What does message error ... mean?](#user-content-faq130)
* [(131) Can you change the direction for swiping to previous/next message?](#user-content-faq131)
* [(132) Why are new message notifications silent?](#user-content-faq132)
* [(133) Why is ActiveSync not supported?](#user-content-faq133)
* [(134) Can you add deleting local messages?](#user-content-faq134)
* [(135) Why are trashed messages and drafts shown in conversations?](#user-content-faq135)
* [(136) How can I delete an account/identity/folder?](#user-content-faq136)
* [(137) How can I reset 'Don't ask again'?](#user-content-faq137)
* [(138) Can you add calendar/contact/tasks/notes management?](#user-content-faq138)
* [(139) How do I fix 'User is authenticated but not connected'?](#user-content-faq139)
* [(140) Why does the message text contain strange characters?](#user-content-faq140)
* [(141) How can I fix 'A drafts folder is required to send messages'?](#user-content-faq141)
* [(142) How can I store sent messages in the inbox?](#user-content-faq142)
* [~~(143) Can you add a trash folder for POP3 accounts?~~](#user-content-faq143)
* [(144) How can I record voice notes?](#user-content-faq144)
* [(145) How can I set a notification sound for an account, folder or sender?](#user-content-faq145)
* [(146) How can I fix incorrect message times?](#user-content-faq146)
* [(147) What should I know about third party versions?](#user-content-faq147)
* [(148) How can I use an Apple iCloud account?](#user-content-faq148)
* [(149) How does the unread message count widget work?](#user-content-faq149)
* [(150) Can you add cancelling calendar invites?](#user-content-faq150)
* [(151) Can you add backup/restore of messages?](#user-content-faq151)
* [(152) How can I insert a contact group?](#user-content-faq152)
* [(153) Why does permanently deleting Gmail message not work?](#user-content-faq153)
* [~~(154) Can you add favicons as contact photos?~~](#user-content-faq154)
* [(155) What is a winmail.dat file?](#user-content-faq155)
* [(156) How can I set up an Office 365 account?](#user-content-faq156)
* [(157) How can I set up an Free.fr account?](#user-content-faq157)
* [(158) Which camera / audio recorder do you recommend?](#user-content-faq158)
* [(159) What are Disconnect's tracker protection lists?](#user-content-faq159)
* [(160) Can you add permanent deletion of messages without confirmation?](#user-content-faq160)
* [(161) Can you add a setting to change the primary and accent color?](#user-content-faq161)
* [(162) Is IMAP NOTIFY supported?](#user-content-faq162)
* [(163) What is message classification?](#user-content-faq163)
* [(164) Can you add customizable themes?](#user-content-faq164)
* [(165) Is Android Auto supported?](#user-content-faq165)
* [(166) Can I snooze a message across multiple devices?](#user-content-faq166)

[I have another question.](#user-content-support)

<a name="faq1"></a>
**(1) Which permissions are needed and why?**

The following Android permissions are needed:

* *have full network access* (INTERNET): to send and receive email
* *view network connections* (ACCESS_NETWORK_STATE): to monitor internet connectivity changes
* *run at startup* (RECEIVE_BOOT_COMPLETED): to start monitoring on device start
* *foreground service* (FOREGROUND_SERVICE): to run a foreground service on Android 9 Pie and later, see also the next question
* *prevent device from sleeping* (WAKE_LOCK): to keep the device awake while synchronizing messages
* *in-app billing* (BILLING): to allow in-app purchases
* *schedule exact alarm* (SCHEDULE_EXACT_ALARM): to use exact alarm scheduling (Android 12 and later)
* Optional: *read your contacts* (READ_CONTACTS): to auto complete addresses, to show contact photos and [to pick contacts](https://developer.android.com/guide/components/intents-common#PickContactDat)
* Optional: *read the contents of your SD card* (READ_EXTERNAL_STORAGE): to accept files from other, outdated apps, see also [this FAQ](#user-content-faq49)
* Optional: *use fingerprint hardware* (USE_FINGERPRINT) and use *biometric hardware* (USE_BIOMETRIC): to use biometric authentication
* Optional: *find accounts on the device* (GET_ACCOUNTS): to select an account when using the Gmail quick setup
* Android 5.1 Lollipop and before: *use accounts on the device* (USE_CREDENTIALS): to select an account when using the Gmail quick setup (not requested on later Android versions)
* Android 5.1 Lollipop and before: *Read profile* (READ_PROFILE): to read your name when using the Gmail quick setup (not requested on later Android versions)

[Optional permissions](https://developer.android.com/training/permissions/requesting) are supported on Android 6 Marshmallow and later only.
On earlier Android versions you will be asked to grant the optional permissions on installing FairEmail.

The following permissions are needed to show the count of unread messages as a badge (see also [this FAQ](#user-content-faq106)):

* *com.sec.android.provider.badge.permission.READ*
* *com.sec.android.provider.badge.permission.WRITE*
* *com.htc.launcher.permission.READ_SETTINGS*
* *com.htc.launcher.permission.UPDATE_SHORTCUT*
* *com.sonyericsson.home.permission.BROADCAST_BADGE*
* *com.sonymobile.home.permission.PROVIDER_INSERT_BADGE*
* *com.anddoes.launcher.permission.UPDATE_COUNT*
* *com.majeur.launcher.permission.UPDATE_BADGE*
* *com.huawei.android.launcher.permission.CHANGE_BADGE*
* *com.huawei.android.launcher.permission.READ_SETTINGS*
* *com.huawei.android.launcher.permission.WRITE_SETTINGS*
* *android.permission.READ_APP_BADGE*
* *com.oppo.launcher.permission.READ_SETTINGS*
* *com.oppo.launcher.permission.WRITE_SETTINGS*
* *me.everything.badger.permission.BADGE_COUNT_READ*
* *me.everything.badger.permission.BADGE_COUNT_WRITE*

FairEmail will keep a list of addresses you receive messages from and send messages to
and will use this list for contact suggestions when no contacts permissions is granted to FairEmail.
This means you can use FairEmail without the Android contacts provider (address book).
Note that you can still pick contacts without granting contacts permissions to FairEmail,
only suggesting contacts won't work without contacts permissions.

<br />

<a name="faq2"></a>
**(2) Why is there a permanent notification shown?**

A low priority permanent status bar notification with the number of accounts being monitored and the number of operations pending (see the next question) is shown
to prevent Android from killing the service that takes care of continuous receiving email.
This was [already necessary](https://developer.android.com/reference/android/app/Service.html#startForeground(int,%20android.app.Notification)),
but with the introduction of [doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby) in Android 6 Marshmallow this is more than ever necessary.
Doze mode will stop all apps when the screen is off for some time, unless the app did start a foreground service, which requires showing a status bar notification.

Most, if not all, other email apps don't show a notification
with the "side effect" that new messages are often not or late being reported and that messages are not or late being sent.

Android shows icons of high priority status bar notifications first and will hide the icon of FairEmail's notification if there is no space to show icons anymore.
In practice this means that the status bar notification doesn't take space in the status bar, unless there is space available.

The status bar notification can be disabled via the notification settings of FairEmail:

* Android 8 Oreo and later: tap the *Receive channel* button and disable the channel via the Android settings (this won't disable new message notifications)
* Android 7 Nougat and before: enabled *Use background service to synchronize messages*, but be sure to read the remark below the setting

You can switch to periodically synchronization of messages in the receive settings to remove the notification, but be aware that this might use more battery power.
See [here](#user-content-faq39) for more details about battery usage.

Android 8 Oreo might also show a status bar notification with the text *Apps are running in the background*.
Please see [here](https://www.reddit.com/r/Android/comments/7vw7l4/psa_turn_off_background_apps_notification/) about how you can disable this notification.

Some people suggested to use [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/) (FCM) instead of an Android service with a status bar notification,
but this would require email providers to send FCM messages or a central server where all messages are collected sending FCM messages.
The first is not going to happen and the last would have significant privacy implications.

If you came here by clicking on the notification, you should know that the next click will open the unified inbox.

<br />

<a name="faq3"></a>
**(3) What are operations and why are they pending?**

The low priority status bar notification shows the number of pending operations, which can be:

* *add*: add message to remote folder
* *move*: move message to another remote folder
* *copy*: copy message to another remote folder
* *fetch*: fetch changed (pushed) message
* *delete*: delete message from remote folder
* *seen*: mark message as read/unread in remote folder
* *answered*: mark message as answered in remote folder
* *flag*: add/remove star in remote folder
* *keyword*: add/remove IMAP flag in remote folder
* *label*: set/reset Gmail label in remote folder
* *headers*: download message headers
* *raw*: download raw message
* *body*: download message text
* *attachment*: download attachment
* *sync*: synchronize local and remote messages
* *subscribe*: subscribe to remote folder
* *purge*: delete all messages from remote folder
* *send*: send message
* *exists*: check if message exists
* *rule*: execute rule on body text
* *expunge*: permanently delete messages

Operations are processed only when there is a connection to the email server or when manually synchronizing.
See also [this FAQ](#user-content-faq16).

<br />

<a name="faq4"></a>
**(4) How can I use an invalid security certificate / empty password / plain text connection?**

*... Untrusted ... not in certificate ...*
<br />
*... Invalid security certificate (Can't verify identity of server) ...*

This can be caused by using an incorrect host name, so first double check the host name in the advanced identity/account settings (tap Manual setup).
Please see the documentation of the email provider about the right host name.

You should try to fix this by contacting your provider or by getting a valid security certificate
because invalid security certificates are insecure and allow [man-in-the-middle attacks](https://en.wikipedia.org/wiki/Man-in-the-middle_attack).
If money is an obstacle, you can get free security certificates from [Let’s Encrypt](https://letsencrypt.org).

The quick, but unsafe solution (not advised), is to enable *Insecure connections* in the advanced identity settings
(navigation menu, tap *Settings*, tap *Manual setup*, tap *Identities*, tap the identity, tap *Advanced*).

Alternatively, you can accept the fingerprint of invalid server certificates like this:

1. Make sure you are using a trusted internet connection (no public Wi-Fi networks, etc)
1. Go to the setup screen via the navigation menu (swipe from the left side inwards)
1. Tap Manual setup, tap Accounts/Identities and tap the faulty account and identity
1. Check/save the account and identity
1. Tick the checkbox below the error message and save again

This will "pin" the server certificate to prevent man-in-the-middle attacks.

Note that older Android versions might not recognize newer certification authorities like Let’s Encrypt causing connections to be considered insecure,
see also [here](https://developer.android.com/training/articles/security-ssl).

<br />

*Trust anchor for certification path not found*

*... java.security.cert.CertPathValidatorException: Trust anchor for certification path not found ...*
means that the default Android trust manager was not able to verify the server certificate chain.

This could be due to the root certificate not being installed on your device
or because intermediate certificates are missing, for example because the email server didn't send them.

You can fix the first problem by downloading and installing the root certificate from the website of the provider of the certificate.

The second problem should be fixed by changing the server configuration or by importing the intermediate certificates on your device.

You can pin the certificate too, see above.

<br />

*Empty password*

Your username is likely easily guessed, so this is insecure.

*Plain text connection*

Your username and password and all messages will be sent and received unencrypted, which is **very insecure**
because a [man-in-the-middle attack](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) is very simple on an unencrypted connection.

If you still want to use an invalid security certificate, an empty password or a plain text connection
you'll need to enable insecure connections in the account and/or identity settings.
STARTTLS should be selected for plain text connections.
If you enable insecure connections, you should connect via private, trusted networks only and never via public networks, like offered in hotels, airports, etc.

<br />

<a name="faq5"></a>
**(5) How can I customize the message view?**

In the three dot overflow menu you can enable or disable or select:

* *text size*: for three different font sizes
* *compact view*: for more condensed message items and a smaller message text font

In the display section of the settings you can enable or disable for example:

* *Unified inbox*: to disable the unified inbox and to list the folders selected for the unified inbox instead
* *Tabular style*: to show a linear list instead of cards
* *Group by date*: show date header above messages with the same date
* *Conversation threading*: to disable conversation threading and to show individual messages instead
* *Conversation action bar*: to disable the bottom navigation bar
* *Highlight color*: to select a color for the sender of unread messages
* *Show contact photos*: to hide contact photos
* *Show names and email addresses*: to show names or to show names and email addresses
* *Show subject italic*: to show the message subject as normal text
* *Show stars*: to hide stars (favorites)
* *Show message preview*: to show 1-4 lines of the message text
* *Show address details by default*: to expand the addresses section by default
* *Automatically show original message for known contacts*: to automatically show original messages for contacts on your device, please read [this FAQ](#user-content-faq35)
* *Automatically show images for known contacts*: to automatically show images for contacts on your device, please read [this FAQ](#user-content-faq35)

Note that messages can be previewed only when the message text was downloaded.
Larger message texts are not downloaded by default on metered (generally mobile) networks.
You can change this in the connection settings.

Some people ask:

* to show the subject bold, but bold is already being used to highlight unread messages
* to move the star to the left, but it is much easier to operate the star on the right side

<br />

<a name="faq6"></a>
**(6) How can I login to Gmail / G suite?**

If you use the Play store or GitHub version of FairEmail,
you can use the quick setup wizard to easily setup a Gmail account and identity.
The Gmail quick setup wizard is not available for third party builds, like the F-Droid build
because Google approved the use of OAuth for official builds only.

If you don't want to use or can't use an on-device Google account, for example on recent Huawei devices,
you can either enable access for "less secure apps" and use your account password (not advised)
or enable two factor authentication and use an app specific password.
To use a password you'll need to set up an account and identity via the manual setup instead of via the quick setup wizard.

**Important**: sometimes Google issues this alert:

*[ALERT] Please log in via your web browser: https://support.google.com/mail/accounts/answer/78754 (Failure)*

This Google security check is triggered more often with *less secure apps* enabled, less with an app password, and hardly when using an on-device account (OAuth).

Please see [this FAQ](#user-content-faq111) on why only on-device accounts can be used.

Note that an app specific password is required when two factor authentication is enabled.

<br />

*App specific password*

See [here](https://support.google.com/accounts/answer/185833) about how to generate an app specific password.

<br />

*Enable "Less secure apps"*

**Important**: using this method is not recommended because it is less reliable.

**Important**: Gsuite accounts authorized with a username/password will stop working [in the near future](https://gsuiteupdates.googleblog.com/2019/12/less-secure-apps-oauth-google-username-password-incorrect.html).

See [here](https://support.google.com/accounts/answer/6010255) about how to enable "less secure apps"
or go [directy to the setting](https://www.google.com/settings/security/lesssecureapps).

If you use multiple Gmail accounts, make sure you change the "less secure apps" setting of the right account(s).

Be aware that you need to leave the "less secure apps" settings screen by using the back arrow to apply the setting.

If you use this method, you should use a [strong password](https://en.wikipedia.org/wiki/Password_strength) for your Gmail account, which is a good idea anyway.
Note that using the [standard](https://tools.ietf.org/html/rfc3501) IMAP protocol in itself is not less secure.

When "less secure apps" is not enabled,
you'll get the error *Authentication failed - invalid credentials* for accounts (IMAP)
and *Username and Password not accepted* for identities (SMTP).

<br />

*General*

You might get the alert "*Please log in via your web browser*".
This happens when Google considers the network that connects you to the internet (this could be a VPN) to be unsafe.
This can be prevented by using the Gmail quick setup wizard or an app specific password.

See [here](https://support.google.com/mail/answer/7126229) for Google's instructions
and [here](https://support.google.com/mail/accounts/answer/78754) for troubleshooting.

<br />

<a name="faq7"></a>
**(7) Why are sent messages not appearing (directly) in the sent folder?**

Sent messages are normally moved from the outbox to the sent folder as soon as your provider adds sent messages to the sent folder.
This requires a sent folder to be selected in the account settings and the sent folder to be set to synchronizing.

Some providers do not keep track of sent messages or the used SMTP server might not be related to the provider.
In these cases FairEmail, will automatically add sent messages to the sent folder on synchronizing the sent folder, which will happen after a message have been sent.
Note that this will result in extra internet traffic.

~~If this doesn't happen, your provider might not keep track of sent messages or you might be using an SMTP server not related to the provider.~~
~~In these cases you can enable the advanced identity setting *Store sent messages* to let FairEmail add sent messages to the sent folder right after sending a message.~~
~~Note that enabling this setting might result in duplicate messages if your provider adds sent messages to the sent folder too.~~
~~Also beware that enabling this setting will result in extra data usage, especially when when sending messages with large attachments.~~

~~If sent messages in the outbox are not found in the sent folder on a full synchronize, they will be moved from the outbox to the sent folder too.~~
~~A full synchronize happens when reconnecting to the server or when synchronizing periodically or manually.~~
~~You'll likely want to enable the advanced setting *Store sent messages* instead to move messages to the sent folder sooner.~~

<br />

<a name="faq8"></a>
**(8) Can I use a Microsoft Exchange account?**

The Microsoft Exchange Web Services protocol [is being phased out](https://techcommunity.microsoft.com/t5/Exchange-Team-Blog/Upcoming-changes-to-Exchange-Web-Services-EWS-API-for-Office-365/ba-p/608055).
So, it makes little sense to add this protocol anymore.

You can use a Microsoft Exchange account if it is accessible via IMAP, which is mostly the case.
See [here](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793) for more information.

Note that the desciption of FairEmail starts with the remark
that non-standard protocols, like Microsoft Exchange Web Services and Microsoft ActiveSync are not supported.

Please see [here](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040)
for the Microsoft documentation about configuring an email client.
There is also a section about common connection errors and solutions.

Some older Exchange server versions have a bug causing empty message and corrupt attachments.
Please see [this FAQ](#user-content-faq110) for a workaround.

Please see [this FAQ](#user-content-faq133) about ActiveSync support.

Please see [this FAQ](#user-content-faq111) about OAuth support.

<br />

<a name="faq9"></a>
**(9) What are identities / how do I add an alias?**

Identities represent email addresses you are sending *from* via an email (SMTP) server.

Some providers allow you to have multiple aliases.
You can configure these by setting the email address field of an additional identity to the alias address
and setting the user name field to your main email address.

Note that you can copy an identity by long pressing it.

Alternatively, you can enable *Allow editing sender address* in the advanced settings of an existing identity to edit the username when composing a new message,
if your provider allows this.

FairEmail will automatically update the passwords of related identities when you update the password of the associated account or a related identity.

See [this FAQ](#user-content-faq33) on editing the username of email addresses.

<br />

<a name="faq10"></a>
**~~(10) What does 'UIDPLUS not supported' mean?~~**

~~The error message *UIDPLUS not supported* means that your email provider does not provide the IMAP [UIDPLUS extension](https://tools.ietf.org/html/rfc4315).
This IMAP extension is required to implement two way synchronization, which is not an optional feature.
So, unless your provider can enable this extension, you cannot use FairEmail for this provider.~~

<br />

<a name="faq11"></a>
**~~(11) Why is POP not supported?~~**

~~Besides that any decent email provider supports [IMAP](https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol) these days,~~
~~using [POP](https://en.wikipedia.org/wiki/Post_Office_Protocol) will result in unnecessary extra battery usage and delayed new message notifications.~~
~~Moreover, POP is unsuitable for two way synchronization and more often than not people read and write messages on different devices these days.~~

~~Basically, POP supports only downloading and deleting messages from the inbox.~~
~~So, common operations like setting message attributes (read, starred, answered, etc), adding (backing up) and moving messages is not possible.~~

~~See also [what Google writes about it](https://support.google.com/mail/answer/7104828).~~

~~For example [Gmail can import messages](https://support.google.com/mail/answer/21289) from another POP account,~~
~~which can be used as a workaround for when your provider doesn't support IMAP.~~

~~tl;dr; consider to switch to IMAP.~~

<br />

<a name="faq12"></a>
**(12) How does encryption/decryption work?**

Communication with email servers is always encrypted, unless you explicitly turned this off.
This question is about optional end-to-end encryption with PGP or S/MIME.
The sender and recipient should first agree on this and exchange signed messages to transfer their public key to be able to send encrypted messages.

<br />

*General*

Please [see here](https://en.wikipedia.org/wiki/Public-key_cryptography) about how public/private key encryption works.

Encryption in short:

* **Outgoing** messages are encrypted with the **public key** of the recipient
* **Incoming** messages are decrypted with the **private key** of the recipient

Signing in short:

* **Outgoing** messages are signed with the **private key** of the sender
* **Incoming** messages are verified with the **public key** of the sender

To sign/encrypt a message, just select the appropriate method in the send dialog.
You can always open the send dialog using the three-dots overflow menu in case you selected *Don't show again* before.

To verify a signature or to decrypt a received message, open the message and just tap the gesture or padlock icon just below the message action bar.

The first time you send a signed/encrypted message you might be asked for a sign key.
FairEmail will automatically store the selected sign key in the used identity for the next time.
If you need to reset the sign key, just save the identity or long press the identity in the list of identities and select *Reset sign key*.
The selected sign key is visible in the list of identities.
If need to select a key on a case by case basis, you can create multiple identities for the same account with the same email address.

In the encryption settings you can select the default encryption method (PGP or S/MIME),
enable *Sign by default*, *Encrypt by default* and *Automatically decrypt messages*,
but be aware that automatic decryption is not possible if user interaction is required, like selecting a key or reading a security token.

The to be encrypted message text/attachments and the decrypted message text/attachments are stored locally only and will never be added to the remote server.
If you want to undo decryption, you can use the *resync* menu item in the three-dots menu of the message action bar.

<br />

*PGP*

You'll need to install and configure [OpenKeychain](https://f-droid.org/en/packages/org.sufficientlysecure.keychain/) first.
FairEmail was tested with OpenKeychain version 5.4. Later versions will most likely be compatible, but earlier versions might not be.

**Important**: the OpenKeychain app is known to (silently) crash when the calling app (FairEmail) is not authorized yet and is getting an existing public key.
You can workaround this by trying to send a signed/encrypted message to a sender with an unknown public key.

**Important**: if the OpenKeychain app cannot find a key (anymore), you might need to reset a previously selected key.
This can be done by long pressing an identity in the list of identities (Settings, tap Manual setup, tap Identities).

**Important**: to let apps like FairEmail reliably connect to the OpenKeychain service to encrypt/decrypt messages,
it might be necessary to disable battery optimizations for the OpenKeychain app.

**Important**: the OpenKeychain app reportedly needs contacts permission to work correctly.

**Important**: on some Android versions / devices it is necessary to enable *Show popups while running in background*
in the additional permissions of the Android app settings of the OpenKeychain app.
Without this permission the draft will be saved, but the OpenKeychain popup to confirm/select might not appear.

FairEmail will send the [Autocrypt](https://autocrypt.org/) header for use by other email clients,
but only for signed and encrypted messages because too many email servers have problems with the often long Autocrypt header.
Note that the most secure way to start an encrypted email exchange is by sending signed messages first.
Received Autocrypt headers will be sent to the OpenKeychain app for storage on verifying a signature or decrypting a message.

Although this shouldn't be necessary for most email clients, you can attach your public key to a message
and if you use *.key* as extension, the mime type will correctly be *application/pgp-keys*.

All key handling is delegated to the OpenKey chain app for security reasons. This also means that FairEmail does not store PGP keys.

Inline encrypted PGP in received messages is supported, but inline PGP signatures and inline PGP in outgoing messages is not supported,
see [here](https://josefsson.org/inline-openpgp-considered-harmful.html) about why not.

Signed-only or encrypted-only messages are not a good idea, please see here about why not:

* [OpenPGP Considerations Part I](https://k9mail.github.io/2016/11/24/OpenPGP-Considerations-Part-I.html)
* [OpenPGP Considerations Part II](https://k9mail.github.io/2017/01/30/OpenPGP-Considerations-Part-II.html)
* [OpenPGP Considerations Part III Autocrypt](https://k9mail.github.io/2018/02/26/OpenPGP-Considerations-Part-III-Autocrypt.html)

Signed-only messages are supported, encrypted-only messages are not supported.

Common errors:

* *No key*: there is no PGP key available for one of the listed email addresses
* *Missing key for encryption*: there is probably a key selected in FairEmail that does not exist in the OpenKeychain app anymore. Resetting the key (see above) will probably fix this problem.
* *Key for signature verification is missing*: the public key for the sender is not available in the OpenKeychain app. This can also be caused by Autocrypt being disabled in the encryption settings or by the Autocrypt header not being sent.

<br />

*S/MIME*

Encrypting a message requires the public key(s) of the recipient(s). Signing a message requires your private key.

Private keys are stored by Android and can be imported via the Android advanced security settings.
There is a shortcut (button) for this in the encryption settings.
Android will ask you to set a PIN, pattern, or password if you didn't before.
If you have a Nokia device with Android 9, please [read this first](https://nokiamob.net/2019/08/10/a-bug-prevents-nokia-1-owners-from-unlocking-their-screen-even-with-right-pin-pattern/).

Note that certificates can contains multiple keys for multiple purposes,  for example for authentication, encryption and signing.
Android only imports the first key, so to import all the keys, the certificate must first be split.
This is not very trivial and you are advised to ask the certificate supplier for support.

Note that S/MIME signing with other algorithms than RSA is supported, but be aware that other email clients might not support this.
S/MIME encryption is possible with symmetric algorithms only, which means in practice using RSA.

The default encryption method is PGP, but the last used encryption method will be remembered for the selected identity for the next time.
You can long press on the send button to change the encryption method for an identity.
If you use both PGP and S/MIME encryption for the same email address, it might be useful to copy the identity,
so you can change the encryption method by selecting one of the two identities.
You can long press an identity in the list of identities (via manual setup in the main setup screen) to copy an identity.

To allow different private keys for the same email address, FairEmail will always let you select a key when there are multiple identities with the same email address for the same account.

Public keys are stored by FairEmail and can be imported when verifying a signature for the first time or via the encryption settings (PEM or DER format).

FairEmail verifies both the signature and the complete certificate chain.

Common errors:

* *No certificate found matching targetContraints*: this likely means you are using an old version of FairEmail
* *unable to find valid certification path to requested target*: basically this means one or more intermediate or root certificates were not found
* *Private key does not match any encryption keys*: the selected key cannot be used to decrypt the message, probably because it is the incorrect key
* *No private key*: no certificate was selected or no certificate was available in the Android keystore

In case the certificate chain is incorrect, you can tap on the little info button to show the all certificates.
After the certificate details the issuer or "selfSign" is shown.
A certificate is self signed when the subject and the issuer are the same.
Certificates from a certificate authority (CA) are marked with "[keyCertSign](https://tools.ietf.org/html/rfc5280#section-4.2.1.3)".
Certificates found in the Android key store are marked with "Android".

A valid chain looks like this:

```
Your certificate > zero or more intermediate certificates > CA (root) certificate marked with "Android"
```

Note that a certificate chain will always be invalid when no anchor certificate can be found in the Android key store,
which is fundamental to S/MIME certificate validation.

Please see [here](https://support.google.com/pixelphone/answer/2844832?hl=en) how you can import certificates into the Android key store.

The use of expired keys, inline encrypted/signed messages and hardware security tokens is not supported.

If you are looking for a free (test) S/MIME certificate, see [here](http://kb.mozillazine.org/Getting_an_SMIME_certificate) for the options.
Please be sure to [read this first](https://davidroessli.com/logs/2019/09/free-smime-certificates-in-2019/#update20191219)
if you want to request an S/MIME Actalis certificate.
If you are looking for a cheap S/MIME certificate, I had a good experience with [Certum](https://www.certum.eu/en/smime-certificates/).

How to extract a public key from a S/MIME certificate:

```
openssl pkcs12 -in filename.pfx/p12 -clcerts -nokeys -out cert.pem
```

You can decode S/MIME signatures, etc, [here](https://lapo.it/asn1js/).

<br />

*pretty Easy privacy*

There is still [no approved standard](https://tools.ietf.org/id/draft-birk-pep-00.html) for pretty Easy privacy (p≡p) and not many people are using it.

However, FairEmail can send and receive PGP encrypted messages, which are compatible with p≡p.
Also, FairEmail understands incoming p≡p messages since version 1.1519,
so the encrypted subject will be shown and the embedded message text will be shown more nicely.

<br />

S/MIME sign/encrypt is a pro feature, but all other PGP and S/MIME operations are free to use.

<br />

<a name="faq13"></a>
**(13) How does search on device/server work?**

You can start searching for messages on sender (from), recipient (to, cc, bcc), subject, keywords or message text by using the magnify glass in the action bar of a folder.
You can also search from any app by selecting *Search email* in the copy/paste popup menu.

Searching in the unified inbox will search in all folders of all accounts,
searching in the folder list will search in the associated account only
and searching in a folder will search in that folder only.

Messages will be searched for on the device first.
There will be an action button with a search again icon at the bottom to continue searching on the server.
You can select in which folder to continue the search.

The IMAP protocol doesn't support searching in more than one folder at the same time.
Searching on the server is an expensive operation, therefore it is not possible to select multiple folders.

Searching local messages is case insensitive and on partial text.
The message text of local messages will not be searched if the message text was not downloaded yet.
Searching on the server might be case sensitive or case insensitive and might be on partial text or whole words, depending on the provider.

Some servers cannot handle searching in the message text when there are a large number of messages.
For this case there is an option to disable searching in the message text.

It is possible to use Gmail search operators by prefixing a search command with *raw:*.
If you configured just one Gmail account, you can start a raw search directly on the server by searching from the unified inbox.
If you configured multiple Gmail accounts,
you'll first need to navigate to the folder list or the archive (all messages) folder of the Gmail account you want to search in.
Please [see here](https://support.google.com/mail/answer/7190) for the possible search operators. For example:

``
raw:larger:10M
``

Searching through a large number of messages on the device is not very fast because of two limitations:

* [sqlite](https://www.sqlite.org/), the database engine of Android has a record size limit, preventing message texts from being stored in the database
* Android apps get only limited memory to work with, even if the device has plenty memory available

This means that searching for a message text requires that files containing the message texts need to be opened one by one
to check if the searched text is contained in the file, which is a relatively expensive process.

In the *miscellaneous settings* you can enable *Build search index* to significantly increase the speed of searching on the device,
but be aware that this will increase battery and storage space usage.
The search index is based on words, so searching for partial text is not possible.
Searching using the search index is by default AND, so searching for *apple orange* will search for apple AND orange.
Words separated by commas result in searching for OR, so for example *apple, orange* will search for apple OR orange.
Both can be combined, so searching for *apple, orange banana* will search for apple OR (orange AND banana).
Using the search index is a pro feature.

From version 1.1315 it is possible to use search expressions like this:

```
apple +banana -cherry ?nuts
```

This will result in searching like this:

```
("apple" AND "banana" AND NOT "cherry") OR "nuts"
```

Search expressions can be used for searching on the device via the search index and for searching on the email server,
but not for searching on the device without search index for performance reasons.

Searching on the device is a free feature, using the search index and searching on the server is a pro feature.

<br />

<a name="faq14"></a>
**(14) How can I set up an Outlook / Live / Hotmail account?**

An Outlook / Live / Hotmail account can be set up via the quick setup wizard and selecting *Outlook*.

To use an Outlook, Live or Hotmail account with two factor authentication enabled, you need to create an app password.
See [here](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification) for the details.

See [here](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) for Microsoft's instructions.

For setting up an Office 365 account, please see [this FAQ](#user-content-faq156).

<br />

<a name="faq15"></a>
**(15) Why does the message text keep loading?**

The message header and message body are fetched separately from the server.
The message text of larger messages is not being pre-fetched on metered connections and will be fetched on demand on expanding a message.
The message text will keep loading if there is no connection to the account, see also the next question,
or if there other operations, like synchronizing messages, are being executed.

You can check the account and folder list for the account and folder state (see the legend for the meaning of the icons)
and the operation list accessible via the main navigation menu for pending operations (see [this FAQ](#user-content-faq3) for the meaning of the operations).

If FairEmail is holding off because of prior connectivity issues, please see [this FAQ](#user-content-faq123), you can force synchronization via the three dots menu.

In the receive settings you can set the maximum size for automatically downloading of messages on metered connections.

Mobile connections are almost always metered and some (paid) Wi-Fi hotspots are too.

<br />

<a name="faq16"></a>
**(16) Why are messages not being synchronized?**

Possible causes of messages not being synchronized (sent or received) are:

* The account or folder(s) are not set to synchronize
* The number of days to synchronize message for is set too low
* There is no usable internet connection
* The email server is temporarily not available
* Android stopped the synchronization service

So, check your account and folder settings and check if the accounts/folders are connected (see the legend in the navigation menu for the meaning of the icons).

If there are any error messages, please see [this FAQ](#user-content-faq22).

On some devices, where there are lots of applications competing for memory, Android may stop the synchronization service as a last resort.

Some Android versions stop apps and services too aggressively.
See [this dedicated website](https://dontkillmyapp.com/) and [this Android issue](https://issuetracker.google.com/issues/122098785) for more information.

Disabling battery optimizations (setup step 3) reduces the chance Android will stop the synchronization service.

In case of successive connection errors, FairEmail will hold off increasingly longer to not drain the battery of your device.
This is described in [this FAQ](#user-content-faq123).

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

First of all, **FairEmail is basically free to use** and only some advanced features need to be purchased.

Zuerst, **FairEmail ist grundsätzlich kostenlos** und nur einige erweiterte Funktionen müssen gekauft werden.

Tout d'abord, **FairEmail est au fond gratuit** et seulement quelques fonctionnalités avancées doivent être achetés.

Please see the Play store description of the app or [see here](https://email.faircode.eu/#pro) for a complete list of pro features.

The right question is "*why are there so many taxes and fees?*":

* VAT: 25 % (depending on your country)
* Google fee: 30 %
* Income tax: 50 %
* <sub>Paypal fee: 5-10 % depending on the country/amount</sub>

So, what is left for the developer is just a fraction of what you pay.

Also note that most free apps will appear not to be sustainable in the end, whereas FairEmail is properly maintained and supported,
and that free apps may have a catch, like sending privacy sensitive information to the internet.
There are no privacy violating ads in the app either.

I have been working on FairEmail almost every day for more than two years, so I think the price is more than reasonable.
For this reason there won't be discounts either.

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

Before Android 8 Oreo: there is an advanced option in the notification settings of the app for this.

Android 8 Oreo and later: please see [here](https://developer.android.com/training/notify-user/channels) about how to configure notification channels.
You can use the button *Default channel* in the notification settings of the app to directly go to the right Android notification channel settings.

Note that apps cannot change notification settings, including the notification light setting, on Android 8 Oreo and later anymore.

Sometimes it is necessary to disable the setting *Show message preview in notifications*
or to enable the settings *Show notifications with a preview text only* to workaround bugs in Android.
This might apply to notification sounds and vibrations too.

Setting a light color before Android 8 is not supported and on Android 8 and later not possible.

<br />

<a name="faq22"></a>
**(22) What does account/folder error ... mean?**

FairEmail does not hide errors like similar apps often do, so it is easier to diagnose problems.

FairEmail will automatically try to connect again after a delay.
This delay will be doubled after each failed attempt to prevent draining the battery and to prevent from being locked out permanently.
Please see [this FAQ](#user-content-faq123) for more information about this.

There are general errors and errors specific to Gmail accounts (see below).

**General errors**

<a name="authfailed"></a>
The error *... **Authentication failed** ...* or *... AUTHENTICATE failed ...* likely means that your username or password was incorrect.
Some providers expect as username just *username* and others your full email address *username@example.com*.
When copying/pasting to enter a username or password, invisible characters might be copied, which could cause this problem as well.
Some password managers are known to do this incorrectly too.
The username might be case sensitive, so try lowercase characters only. The password is almost always case sensitive.
Some providers require using an app password instead of the account password, so please check the documentation of the provider.
Sometimes it is necessary to enable external access (IMAP/SMTP) on the website of the provider first.
Other possible causes are that the account is blocked or that logging in has been administratively restricted in some way,
for example by allowing to login from certain networks / IP addresses only.

If needed, you can update a password in the account settings:
navigation menu (left side menu), tap *Settings*, tap *Manual setup*, tap *Accounts* and tap on the account.
Changing the account password will in most cases automatically change the password of related identities too.
If the account was authorized with OAuth via the quick setup wizard instead of with a password,
you can run the quick setup wizard again and tick *Authorize existing account again* to authenticate the account again.
Note that this requires a recent version of the app.

The error *... Too many bad auth attempts ...* likely means that you are using a Yahoo account password instead of an app password.
Please see [this FAQ](#user-content-faq88) about how to set up a Yahoo account.

The message *... +OK ...* likely means that a POP3 port (usually port number 995) is being used for an IMAP account (usually port number 993).

The errors *... invalid greeting ...*, *... requires valid address ...* and *... Parameter to HELO does not conform to RFC syntax ...*
can likely be solved by changing the advanced identity setting *Use local IP address instead of host name*.

The error *... Couldn't connect to host ...* means that there was no response from the email server within a reasonable time (20 seconds by default).
Mostly this indicates internet connectivity issues, possibly caused by a VPN or by a firewall app.
You can try to increase the connection timeout in the connection settings of FairEmail, for when the email server is really slow.

The error *... Connection refused ...* means that the email server
or something between the email server and the app, like a firewall, actively refused the connection.

The error *... Network unreachable ...* means that the email server was not reachable via the current internet connection,
for example because internet traffic is restricted to local traffic only.

The error *... Host is unresolved ...*, *... Unable to resolve host ...* or *... No address associated with hostname ...*
means that the address of the email server could not be resolved into an IP address.
This might be caused by a VPN, ad blocking or an unreachable or not properly working (local) [DNS](https://en.wikipedia.org/wiki/Domain_Name_System) server.

The error *... Software caused connection abort ...*
means that the email server or something between FairEmail and the email server actively terminated an existing connection.
This can for example happen when connectivity was abruptly lost. A typical example is turning on flight mode.

The errors *... BYE Logging out ...*, *... Connection reset ...* mean that the email server
or something between the email server and the app, for example a router or a firewall (app), actively terminated an existing connection.

The error *... Connection closed by peer ...* might be caused by a not updated Exchange server,
see [here](https://blogs.technet.microsoft.com/pki/2010/09/30/sha2-and-windows/) for more information.

The errors *... Read error ...*, *... Write error ...*, *... Read timed out ...*, *... Broken pipe ...* mean that the email server is not responding anymore or that the internet connection is bad.

<a name="connectiondropped"></a>
The error *... Connection dropped by server? ...* means that the email server unexpectedly terminated the connection.
This sometimes happen when there were too many connections in a too short time or when a wrong password was used for too many times.
In this case, please make sure your password is correct and disable receiving in the receive settings for about 30 minutes and try again.
If needed, see [this FAQ](#user-content-faq23) about how you can reduce the number of connections.

The error *... Unexpected end of zlib input stream ...* means that not all data was received, possibly due to a bad or interrupted connection.

The error *... connection failure ...* could indicate [Too many simultaneous connections](#user-content-faq23).

The warning *... Unsupported encoding ...* means that the character set of the message is unknown or not supported.
FairEmail will assume ISO-8859-1 (Latin1), which will in most cases result in showing the message correctly.

The error *... Login Rate Limit Hit ...* means that there were too many login attempts with an incorrect password. Please double check your password or authenticate the account again with the quick setup wizard (OAuth only).

Please [see here](#user-content-faq4) for the errors *... Untrusted ... not in certificate ...*, *... Invalid security certificate (Can't verify identity of server) ...* or *... Trust anchor for certification path not found ...*

Please [see here](#user-content-faq127) for the error *... Syntactically invalid HELO argument(s) ...*.

Please [see here](#user-content-faq41) for the error *... Handshake failed ...*.

See [here](https://linux.die.net/man/3/connect) for what error codes like EHOSTUNREACH and ETIMEDOUT mean.

Possible causes are:

* A firewall or router is blocking connections to the server
* The host name or port number is invalid
* There are problems with the internet connection
* There are problems with resolving domain names (Yandex: try to disable private DNS in the Android settings)
* The email server is refusing to accept (external) connections
* The email server is refusing to accept a message, for example because it is too large or contains unacceptable links
* There are too many connections to the server, see also the next question

Many public Wi-Fi networks block outgoing email to prevent spam.
Sometimes you can workaround this by using another SMTP port. See the documentation of the provider for the usable port numbers.

If you are using a [VPN](https://en.wikipedia.org/wiki/Virtual_private_network),
the VPN provider might block the connection because it is too aggressively trying to prevent spam.
Note that [Google Fi](https://fi.google.com/) is using a VPN too.

**Send errors**

SMTP servers can reject messages for [a variety of reasons](https://en.wikipedia.org/wiki/List_of_SMTP_server_return_codes).
Too large messages and triggering the spam filter of an email server are the most common reasons.

* The attachment size limit for Gmail [is 25 MB](https://support.google.com/mail/answer/6584)
* The attachment size limit for Outlook and Office 365 [is 20 MB](https://support.microsoft.com/en-us/help/2813269/attachment-size-exceeds-the-allowable-limit-error-when-you-add-a-large)
* The attachment size limit for Yahoo [is 25 MB](https://help.yahoo.com/kb/SLN5673.html)
* *554 5.7.1 Service unavailable; Client host xxx.xxx.xxx.xxx blocked*, please [see here](https://docs.gandi.net/en/gandimail/faq/error_types/554_5_7_1_service_unavailable.html)
* *501 Syntax error - line too long* is often caused by using a long Autocrypt header
* *503 5.5.0 Recipient already specified* mostly means that an address is being used both as TO and CC address
* *554 5.7.1 ... not permitted to relay* means that the email server does not recognize the username/email address. Please double check the host name and username/email address in the identity settings.
* *550 Spam message rejected because IP is listed by ...* means that the email server rejected to send a message from the current (public) network address because it was misused to send spam by (hopefully) somebody else before. Please try to enable flight mode for 10 minutes to acquire a new network address.
* *550 We're sorry, but we can't send your email. Either the subject matter, a link, or an attachment potentially contains spam, or phishing or malware.* means that the email provider considers an outgoing message as harmful.
* *571 5.7.1 Message contains spam or virus or sender is blocked ...* means that the email server considered an outgoing message as spam. This probably means that the spam filters of the email server are too strict. You'll need to contact the email provider for support on this.
* *451 4.7.0 Temporary server error. Please try again later. PRX4 ...*: please [see here](https://www.limilabs.com/blog/office365-temporary-server-error-please-try-again-later-prx4) or [see here](https://judeperera.wordpress.com/2019/10/11/fixing-451-4-7-0-temporary-server-error-please-try-again-later-prx4/).
* *571 5.7.1 Relay access denied*: please double check the username and email address in the advanced identity settings (via the manual setup).

If you want to use the Gmail SMTP server to workaround a too strict outgoing spam filter or to improve delivery of messages:

* Verify your email address [here](https://mail.google.com/mail/u/0/#settings/accounts) (you'll need to use a desktop browser for this)
* Change the identity settings like this (Settings, tap Manual setup, tap Identities, tap identity):

&emsp;&emsp;Username: *your Gmail address*<br />
&emsp;&emsp;Password: *[an app password](#user-content-faq6)*<br />
&emsp;&emsp;Host: *smtp.gmail.com*<br />
&emsp;&emsp;Port: *465*<br />
&emsp;&emsp;Encryption: *SSL/TLS*<br />
&emsp;&emsp;Reply to address: *your email address* (advanced identity settings)<br />

<br />

**Gmail errors**

The authorization of Gmail accounts setup with the quick wizard needs to be periodically refreshed
via the [Android account manager](https://developer.android.com/reference/android/accounts/AccountManager).
This requires contact/account permissions and internet connectivity.

In case of errors it is possible to authorize/restore a Gmail account again via the Gmail quick setup wizard.

The error *... Authentication failed ... Account not found ...* means that a previously authorized Gmail account was removed from the device.

The errors *... Authentication failed ... No token ...* means that the Android account manager failed to refresh the authorization of a Gmail account.

The error *... Authentication failed ... network error ...*
means that the Android account manager was not able to refresh the authorization of a Gmail account due to problems with the internet connection

The error *... Authentication failed ... Invalid credentials ...* could be caused by changing the account password
or by having revoked the required account/contacts permissions.
In case the account password was changed, you'll need to authenticate the Google account in the Android account settings again.
In case the permissions were revoked, you can start the Gmail quick setup wizard to grant the required permissions again (you don't need to setup the account again).

The eror *... ServiceDisabled ...* might be caused by enrolling in the [Advanced Protection Program](https://landing.google.com/advancedprotection/):
"*To read your email, you can (must) use Gmail - You won’t be able to use your Google Account with some (all) apps & services that require access to sensitive data like your emails*",
see [here](https://support.google.com/accounts/answer/7519408?hl=en&ref_topic=9264881).

When in doubt, you can ask for [support](#user-content-support).

<br />

<a name="faq23"></a>
**(23) Why do I get alert ... ?**

*General*

Alerts are warning messages sent by email servers.

*Too many simultaneous connections* or *Maximum number of connections exceeded*

This alert will be sent when there are too many folder connections for the same email account at the same time.

Possible causes are:

* There are multiple email clients connected to the same account
* The same email client is connected multiple times to the same account
* Previous connections were terminated abruptly for example by abruptly losing internet connectivity

First try to wait some time to see if the problem resolves itself, else:

* either switch to periodically checking for messages in the receive settings, which will result in opening folders one at a time
* or set some folders to poll instead of synchronize (long press folder in the folder list, edit properties)

An easy way to configure periodically checking for messages for all folders except the inbox
is to use *Apply to all ...* in the three-dots menu of the folder list and to tick the bottom two advanced checkboxes.

The maximum number of simultaneous folder connections for Gmail is 15,
so you can synchronize at most 15 folders simultaneously on *all* your devices at the same time.
For this reason Gmail *user* folders are set to poll by default instead of synchronize always.
When needed or desired, you can change this by long pressing a folder in the folder list and selecting *Edit properties*.
See [here](https://support.google.com/mail/answer/7126229) for details.

When using a Dovecot server,
you might want to change the setting [mail_max_userip_connections](https://doc.dovecot.org/settings/dovecot_core_settings/#mail-max-userip-connections).

Note that it will take the email server a while to discover broken connections, for example due to going out of range of a network,
which means that effectively only half of the folder connections are available. For Gmail this would be just 7 connections.

<br />

<a name="faq24"></a>
**(24) What is browse messages on the server?**

Browse messages on the server will fetch messages from the email server in real time
when you reach the end of the list of synchronized messages, even when the folder is set to not synchronize.
You can disable this feature in the advanced account settings.

<br />

<a name="faq25"></a>
**(25) Why can't I select/open/save an image, attachment or a file?**

When a menu item to select/open/save a file is disabled (dimmed) or when you get the message *Storage access framework not available*,
the [storage access framework](https://developer.android.com/guide/topics/providers/document-provider), a standard Android component, is probably not present.
This might be because your custom ROM does not include it or because it was actively removed (debloated).

FairEmail does not request storage permissions, so this framework is required to select files and folders.
No app, except maybe file managers, targeting Android 4.4 KitKat or later should ask for storage permissions because it would allow access to *all* files.

The storage access framework is provided by the package *com.android.documentsui*,
which is visible as *Files* app on some Android versions (notable OxygenOS).

You can enable the storage access framework (again) with this adb command:

```
pm install -k --user 0 com.android.documentsui
```

Alternatively, you might be able to enable the *Files* app again using the Android app settings.

<br />

<a name="faq26"></a>
**(26) Can I help to translate FairEmail in my own language?**

Yes, you can translate the texts of FairEmail in your own language [on Crowdin](https://crowdin.com/project/open-source-email).
Registration is free.

If you would like your name or alias to be included in the list of contributors in *About* the app,
please [contact me](https://contact.faircode.eu/?product=fairemailsupport).

<br />

<a name="faq27"></a>
**(27) How can I distinguish between embedded and external images?**

External image:

![External image](https://github.com/M66B/FairEmail/blob/master/images/baseline_image_black_48dp.png)

Embedded image:

![Embedded image](https://github.com/M66B/FairEmail/blob/master/images/baseline_photo_library_black_48dp.png)

Broken image:

![Broken image](https://github.com/M66B/FairEmail/blob/master/images/baseline_broken_image_black_48dp.png)

Note that downloading external images from a remote server can be used to record you did see a message, which you likely don't want if the message is spam or malicious.

<br />

<a name="faq28"></a>
**(28) How can I manage status bar notifications?**

In the notification settings you'll find a button *Manage notifications* to directly navigate to the Android notifications settings for FairEmail.

On Android 8.0 Oreo and later you can manage the properties of the individual notification channels,
for example to set a specific notification sound or to show notifications on the lock screen.

FairEmail has the following notification channels:

* Service: used for the notification of the synchronize service, see also [this FAQ](#user-content-faq2)
* Send: used for the notification of the send service
* Notifications: used for new message notifications
* Warning: used for warning notifications
* Error: used for error notifications

See [here](https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels) for details on notification channels.
In short: tap on the notification channel name to access the channel settings.

On Android before Android 8 Oreo you can set the notification sound in the settings.

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

There are shortcuts available to compose a new message to a favorite contact.

Shortcuts require Android 7.1 Nougat or later.
The usage of shortcuts is explained [here](https://support.google.com/android/answer/2781850).

It is also possible to create shortcuts to folders
by long pressing a folder in the folder list of an account and selecting *Add shortcut*.

<br />

<a name="faq32"></a>
**(32) How can I check if reading email is really safe?**

You can use the [Email Privacy Tester](https://www.emailprivacytester.com/) for this.

<br />

<a name="faq33"></a>
**(33) Why are edited sender addresses not working?**

Most providers accept validated addresses only when sending messages to prevent spam.

For example Google modifies the message headers like this for *unverified* addresses:

```
From: Somebody <somebody@example.org>
X-Google-Original-From: Somebody <somebody+extra@example.org>
```

This means that the edited sender address was automatically replaced by a verified address before sending the message.

Note that this is independent of receiving messages.

<br />

<a name="faq34"></a>
**(34) How are identities matched?**

Identities are as expected matched by account.
For incoming messages the *to*, *cc*, *bcc*, *from* and *(X-)delivered/envelope/original-to* addresses will be checked (in this order)
and for outgoing messages (drafts, outbox and sent) only the *from* addresses will be checked.
Equal addresses have precedence over partially matching addresses, except for *delivered-to* addresses.

The matched address will be shown as *via* in the addresses section of received messages (between the message header and message text).

Note that identities needs to be enabled to be able to be matched
and that identities of other accounts will not be considered.

Matching will be done only once on receiving a message, so changing the configuration will not change existing messages.
You could clear local messages by long pressing a folder in the folder list and synchronize the messages again though.

It is possible to configure a [regex](https://en.wikipedia.org/wiki/Regular_expression) in the identity settings
to match **the username** of an email address (the part before the @ sign).

Note that the domain name (the parts after the @ sign) always needs to be equal to the domain name of the identity.

If you like to match a catch-all email address, this regex is mostly okay:

```
.*
```

If you like to match the special purpose email addresses abc@example.com and xyx@example.com
and like to have a fallback email address main@example.com as well, you could do something like this:

* Identity: abc@example.com; regex: **(?i)abc**
* Identity: xyz@example.com; regex: **(?i)xyz**
* Identity: main@example.com; regex: **^(?i)((?!abc|xyz).)\*$**

Matched identities can be used to color code messages.
The identity color takes precedence over the folder and account color.
Setting identity colors is a pro feature.

<br />

<a name="faq35"></a>
**(35) Why should I be careful with viewing images, attachments, the original message, and opening links?**

Viewing remotely stored images (see also [this FAQ](#user-content-faq27)) and opening links might not only tell the sender that you have seen the message,
but will also leak your IP address.
See also this question: [Why email's link is more dangerous than web search's link?](https://security.stackexchange.com/questions/241139/why-emails-link-is-more-dangerous-than-web-searchs-link).

Opening attachments or viewing an original message might load remote content and execute scripts,
that might not only cause privacy sensitive information to leak, but can also be a security risk.

Note that your contacts could unknowingly send malicious messages if they got infected with malware.

FairEmail formats messages again causing messages to look different from the original, but also uncovering phishing links.

Note that reformatted messages are often better readable than original messages because the margins are removed, and font colors and sizes are standardized.

The Gmail app shows images by default by downloading the images through a Google proxy server.
Since the images are downloaded from the source server [in real-time](https://blog.filippo.io/how-the-new-gmail-image-proxy-works-and-what-this-means-for-you/),
this is even less secure because Google is involved too without providing much benefit.

You can show images and original messages by default for trusted senders on a case-by-case basis by checking *Do not ask this again for ...*.

If you want to reset the default *Open with* apps, please [see here](https://www.androidauthority.com/how-to-set-default-apps-android-clear-621269/).

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

All supported Android versions [encrypt all user data](https://source.android.com/security/encryption),
so all data, including usernames, passwords, messages, etc, is stored encrypted.

If the device is secured with a PIN, pattern or password, you can make the account and identity passwords visible.
If this is a problem because you are sharing the device with other people,
consider to use [user profiles](https://www.howtogeek.com/333484/how-to-set-up-multiple-user-profiles-on-android/).

<br />

<a name="faq39"></a>
**(39) How can I reduce the battery usage of FairEmail?**

Recent Android versions by default report *app usage* as a percentage in the Android battery settings screen.
**Confusingly, *app usage* is not the same as *battery usage* and is not even directly related to battery usage!**
The app usage (while in use) will be very high because FairEmail is using a foreground service which is considered as constant app usage by Android.
However, this doesn't mean that FairEmail is constantly using battery power.
The real battery usage can be seen by navigating to this screen:

*Android settings*, *Battery*, three-dots menu *Battery usage*, three-dots menu *Show full device usage*

As a rule of thumb the battery usage should be below or in any case not be much higher than *Mobile network standby*.
If this isn't the case, please turn on *Auto optimize* in the receive settings.
If this doesn't help, please [ask for support](https://contact.faircode.eu/?product=fairemailsupport).

It is inevitable that synchronizing messages will use battery power because it requires network access and accessing the messages database.

If you are comparing the battery usage of FairEmail with another email client, please make sure the other email client is setup similarly.
For example comparing always sync (push messages) and (infrequent) periodic checking for new messages is not a fair comparison.

Reconnecting to an email server will use extra battery power, so an unstable internet connection will result in extra battery usage.
Also, some email servers prematurely terminate idle connections, while [the standard](https://tools.ietf.org/html/rfc2177) says that an idle connection should be kept open for 29 minutes.
In these cases you might want to synchronize periodically, for example each hour, instead of continuously.
Note that polling frequently (more than every 30-60 minutes) will likely use more battery power than synchronizing always
because connecting to the server and comparing the local and remote messages are expensive operations.

[On some devices](https://dontkillmyapp.com/) it is necessary to *disable* battery optimizations (setup step 3) to keep connections to email servers open.
In fact, leaving battery optimizations enabled can result in extra battery usage for all devices, even though this sounds contradictory!

Most of the battery usage, not considering viewing messages, is due to synchronization (receiving and sending) of messages.
So, to reduce the battery usage, set the number of days to synchronize message for to a lower value,
especially if there are a lot of recent messages in a folder.
Long press a folder name in the folders list and select *Edit properties* to access this setting.

If you have at least once a day internet connectivity, it is sufficient to synchronize messages just for one day.

Note that you can set the number of days to *keep* messages for to a higher number than to *synchronize* messages for.
You could for example initially synchronize messages for a large number of days and after this has been completed
reduce the number of days to synchronize messages, but leave the number of days to keep messages.
After decreasing the number of days to keep messages, you might want to run the cleanup in the miscellaneous settings to remove old files.

In the receive settings you can enable to always synchronize starred messages,
which will allow you to keep older messages around while synchronizing messages for a limited number of days.

Disabling the folder option *Automatically download message texts and attachments*
will result in less network traffic and thus less battery usage.
You could disable this option for example for the sent folder and the archive.

Synchronizing messages at night is mostly not useful, so you can save on battery usage by not synchronizing at night.
In the settings you can select a schedule for message synchronization (this is a pro feature).

FairEmail will by default synchronize the folder list on each connection.
Since folders are mostly not created, renamed and deleted very often, you can save some network and battery usage by disabling this in the receive settings.

FairEmail will by default check if old messages were deleted from the server on each connection.
If you don't mind that old messages that were delete from the server are still visible in FairEmail, you can save some network and battery usage by disabling this in the receive settings.

Some providers don't follow the IMAP standard and don't keep connections open long enough, forcing FairEmail to reconnect often, causing extra battery usage.
You can inspect the *Log* via the main navigation menu to check if there are frequent reconnects (connection closed/reset, read/write error/timeout, etc).
You can workaround this by lowering the keep-alive interval in the advanced account settings to for example 9 or 15 minutes.
Note that battery optimizations need to be disabled in setup step 3 to reliably keep connections alive.

Some providers send every two minutes something like '*Still here*' resulting in network traffic and your device to wake up and causing unnecessary extra battery usage.
You can inspect the *Log* via the main navigation menu to check if your provider is doing this.
If your provider is using [Dovecot](https://www.dovecot.org/) as IMAP server,
you could ask your provider to change the [imap_idle_notify_interval](https://wiki.dovecot.org/Timeouts) setting to a higher value or better yet, to disable this.
If your provider is not able or willing to change/disable this, you should consider to switch to periodically instead of continuous synchronization.
You can change this in the receive settings.

If you got the message *This provider does not support push messages* while configuring an account,
consider switching to a modern provider which supports push messages (IMAP IDLE) to reduce battery usage.

If your device has an [AMOLED](https://en.wikipedia.org/wiki/AMOLED) screen,
you can save battery usage while viewing messages by switching to the black theme.

If auto optimize in the receive settings is enabled,
an account will automatically be switched to periodically checking for new messages when the email server:

* Says '*Still here*' within 3 minutes
* The email server does not support push messages
* The keep-alive interval is lower than 12 minutes

In addition, the trash and spam folders will be automatically set to checking for new messages
after three successive [too many simultaneous connections](#user-content-faq23) errors.

<br />

<a name="faq40"></a>
**(40) How can I reduce the data usage of FairEmail?**

You can reduce the data usage basically in the same way as reducing battery usage, see the previous question for suggestions.

It is inevitable that data will be used to synchronize messages.

If the connection to the email server is lost, FairEmail will always synchronize the messages again to make sure no messages were missed.
If the connection is unstable, this can result in extra data usage.
In this case, it is a good idea to decrease the number of days to synchronize messages to a minimum (see the previous question)
or to switch to periodically synchronizing of messages (receive settings).

To reduce data usage, you could change these advanced receive settings:

* Check if old messages were removed from the server: disable
* Synchronize (shared) folder list: disable

By default FairEmail does not download message texts and attachments larger than 256 KiB when there is a metered (mobile or paid Wi-Fi) internet connection.
You can change this in the connection settings.

<br />

<a name="faq41"></a>
**(41) How can I fix the error 'Handshake failed' ?**

There are several possible causes, so please read to the end of this answer.

The error '*Handshake failed ... WRONG_VERSION_NUMBER ...*' might mean that you are trying to connect to an IMAP or SMTP server
without an encrypted connection, typically using port 143 (IMAP) and port 25 (SMTP), or that a wrong protocol (SSL/TLS or STARTTLS) is being used.

Most providers provide encrypted connections using different ports, typically port 993 (IMAP) and port 465/587 (SMTP).

If your provider doesn't support encrypted connections, you should ask to make this possible.
If this isn't an option, you could enable *Allow insecure connections* both in the advanced settings AND the account/identity settings.

See also [this FAQ](#user-content-faq4).

The error '*Handshake failed ... SSLV3_ALERT_ILLEGAL_PARAMETER ...*' is either caused by a bug in the SSL protocol implementation
or by a too short DH key on the email server and can unfortunately not be fixed by FairEmail.

The error '*Handshake failed ... HANDSHAKE_FAILURE_ON_CLIENT_HELLO ...*' might be caused by the provider still using RC4,
which isn't supported since [Android 7](https://developer.android.com/about/versions/nougat/android-7.0-changes.html#tls-ssl) anymore.

The error '*Handshake failed ... UNSUPPORTED_PROTOCOL or TLSV1_ALERT_PROTOCOL_VERSION ...*'
might be caused by enabling hardening connections in the connection settings
or by Android not supporting older protocols anymore, like SSLv3.

Android 8 Oreo and later [do not support](https://developer.android.com/about/versions/oreo/android-8.0-changes#security-all) SSLv3 anymore.
There is no way to workaround lacking RC4 and SSLv3 support because it has completely been removed from Android (which should say something).

You can use [this website](https://ssl-tools.net/mailservers) or [this website](https://www.immuniweb.com/ssl/) to check for SSL/TLS problems of email servers.

<br />

<a name="faq42"></a>
**(42) Can you add a new provider to the list of providers?**

If the provider is used by more than a few people, yes, with pleasure.

The following information is needed:

```
<provider
	name="Gmail"
	link="https://support.google.com/mail/answer/7126229" // link to the instructions of the provider
	type="com.google"> // this is not needed
	<imap
		host="imap.gmail.com"
		port="993"
		starttls="false" />
	<smtp
		host="smtp.gmail.com"
		port="465"
		starttls="false" />
</provider>
```

The EFF [writes](https://www.eff.org/nl/deeplinks/2018/06/announcing-starttls-everywhere-securing-hop-hop-email-delivery):
"*Additionally, even if you configure STARTTLS perfectly and use a valid certificate, there’s still no guarantee your communication will be encrypted.*"

So, pure SSL connections are safer than using [STARTTLS](https://en.wikipedia.org/wiki/Opportunistic_TLS) and therefore preferred.

Please make sure receiving and sending messages works properly before contacting me to add a provider.

See below about how to contact me.

<br />

<a name="faq43"></a>
**(43) Can you show the original ... ?**

Show original, shows the original message as the sender has sent it, including original fonts, colors, margins, etc.
FairEmail does and will not alter this in any way,
except for requesting [TEXT_AUTOSIZING](https://developer.android.com/reference/android/webkit/WebSettings.LayoutAlgorithm),
which will *attempt* to make small text more readable.

<br />

<a name="faq44"></a>
**~~(44) Can you show contact photos / identicons in the sent folder?~~**

~~Contact photos and identicons are always shown for the sender because this is necessary for conversation threads.~~
~~Getting contact photos for both the sender and receiver is not really an option because getting contact photo is an expensive operation.~~

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

The speed of your device and internet connection and the number of days to synchronize messages determine how long synchronization will take.
Note that you shouldn't set the number of days to synchronize messages to more than one day in most cases, see also [this FAQ](#user-content-faq39).

<br />

<a name="faq47"></a>
**(47) How do I solve the error 'No primary account or no drafts folder' ?**

You'll get the error message *No primary account or no drafts folder* when trying to compose a message
while there is no account set to be the primary account or when there is no drafts folder selected for the primary account.
This can happen for example when you start FairEmail to compose a message from another app.
FairEmail needs to know where to store the draft,
so you'll need to select one account to be the primary account and/or you'll need to select a drafts folder for the primary account.

This can also happen when you try to reply to a message or to forward a message from an account with no drafts folder
while there is no primary account or when the primary account does not have a drafts folder.

Please see [this FAQ](#user-content-faq141) for some more information.

<br />

<a name="faq48"></a>
**~~(48) How do I solve the error 'No primary account or no archive folder' ?~~**

~~You'll get the error message *No primary account or no archive folder* when searching for messages from another app.
FairEmail needs to know where to search,
so you'll need to select one account to be the primary account and/or you'll need to select a archive folder for the primary account.~~

<br />

<a name="faq49"></a>
**(49) How do I fix 'An outdated app sent a file path instead of a file stream' ?**

You likely selected or sent an attachment or image with an outdated file manager
or an outdated app which assumes all apps still have storage permissions.
For security and privacy reasons modern apps like FairEmail have no full access to all files anymore.
This can result into the error message *An outdated app sent a file path instead of a file stream*
if a file name instead of a file stream is being shared with FairEmail because FairEmail cannot randomly open files.

You can fix this by switching to an up-to-date file manager or an app designed for recent Android versions.
Alternatively, you can grant FairEmail read access to the storage space on your device in the Android app settings.
Note that this workaround [won't work on Android Q](https://developer.android.com/preview/privacy/scoped-storage) anymore.

See also [question 25](#user-content-faq25)
and [what Google writes about it](https://developer.android.com/training/secure-file-sharing/share-file#RespondToRequest).

<br />

<a name="faq50"></a>
**(50) Can you add an option to synchronize all messages?**

You can synchronize more or even all messages by long pressing a folder (inbox) in the folder list of an account (tap on the account name in the navigation menu)
and selecting *Synchronize more* in the popup menu.

<br />

<a name="faq51"></a>
**(51) How are folders sorted?**

Folders are first sorted on account order (by default on account name)
and within an account with special, system folders on top, followed by folders set to synchronize.
Within each category the folders are sorted on (display) name.
You can set the display name by long pressing a folder in the folder list and selecting *Edit properties*.

The navigation (hamburger) menu item *Order folders* in the settings can be used to manually order the folders.

<br />

<a name="faq52"></a>
**(52) Why does it take some time to reconnect to an account?**

There is no reliable way to know if an account connection was terminated gracefully or forcefully.
Trying to reconnect to an account while the account connection was terminated forcefully too often can result in problems
like [too many simultaneous connections](#user-content-faq23) or even the account being blocked.
To prevent such problems, FairEmail waits 90 seconds until trying to reconnect again.

You can long press *Settings* in the navigation menu to reconnect immediately.

<br />

<a name="faq53"></a>
**(53) Can you stick the message action bar to the top/bottom?**

The message action bar works on a single message and the bottom action bar works on all the messages in the conversation.
Since there is often more than one message in a conversation, this is not possible.
Moreover, there are quite some message specific actions, like forwarding.

Moving the message action bar to the bottom of the message is visually not appealing because there is already a conversation action bar at the bottom of the screen.

Note that there are not many, if any, email apps that display a conversation as a list of expandable messages.
This has a lot of advantages, but the also causes the need for message specific actions.

<br />

<a name="faq54"></a>
**~~(54) How do I use a namespace prefix?~~**

~~A namespace prefix is used to automatically remove the prefix providers sometimes add to folder names.~~

~~For example the Gmail spam folder is called:~~

```
[Gmail]/Spam
```

~~By setting the namespace prefix to *[Gmail]* FairEmail will automatically remove *[Gmail]/* from all folder names.~~

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
so it is not worth a lot of effort to add support for this to FairEmail.

<br />

<a name="faq57"></a>
**(57) Can I use HTML in signatures?**

Yes, you can use [HTML](https://en.wikipedia.org/wiki/HTML).
In the signature editor you can switch to HTML mode via the three-dots menu.

Note that if you switch back to the text editor that not all HTML might be rendered as-is because the Android text editor is not able to render all HTML.
Similarly, if you use the text editor, the HTML might be altered in unexpected ways.

If you want to use preformatted text, like [ASCII art](https://en.wikipedia.org/wiki/ASCII_art), you should wrap the text in a *pre* element, like this:

```
<pre>
  |\_/|
 / @ @ \
( > º < )
 `>>x<<´
 /  O  \
 </pre>
```

<br />

<a name="faq58"></a>
**(58) What does an open/closed email icon mean?**

The email icon in the folder list can be open (outlined) or closed (solid):

![External image](https://github.com/M66B/FairEmail/blob/master/images/baseline_mail_outline_black_48dp.png)

Message bodies and attachments are not downloaded by default.

![External image](https://github.com/M66B/FairEmail/blob/master/images/baseline_email_black_48dp.png)

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

* Did you know that starred messages can be synchronized/kept always? (this can be enabled in the receive settings)
* Did you know that you can long press the 'write message' icon to go to the drafts folder?
* Did you know there is an advanced option to mark messages read when they are moved? (archiving and trashing is also moving)
* Did you know that you can select text (or an email address) in any app on recent Android versions and let FairEmail search for it?
* Did you know that FairEmail has a tablet mode? Rotate your device in landscape mode and conversation threads will be opened in a second column if there is enough screen space.
* Did you know that you can long press a reply template to create a draft message from the template?
* Did you know that you can long press, hold and swipe to select a range of messages?
* Did you know that you can retry sending messages by using pull-down-to-refresh in the outbox?
* Did you know that you can swipe a conversation left or right to go to the next or previous conversation?
* Did you know that you can tap on an image to see where it will be downloaded from?
* Did you know that you can long press the folder icon in the action bar to select an account?
* Did you know that you can long press the star icon in a conversation thread to set a colored star?
* Did you know that you can open the navigation drawer by swiping from the left, even when viewing a conversation?
* Did you know that you can long press the people's icon to show/hide the CC/BCC fields and remember the visibility state for the next time?
* Did you know that you can insert the email addresses of an Android contact group via the three dots overflow menu?
* Did you know that if you select text and hit reply, only the selected text will be quoted?
* Did you know that you can long press the trash icons (both in the message and the bottom action bar) to permanently delete a message or conversation? (version 1.1368+)
* Did you know that you can long press the send action to show the send dialog, even if it was disabled?
* Did you know that you can long press the full screen icon to show the original message text only?

<br />

<a name="faq61"></a>
**(61) Why are some messages shown dimmed?**

Messages shown dimmed (grayed) are locally moved messages for which the move is not confirmed by the server yet.
This can happen when there is no connection to the server or the account (yet).
These messages will be synchronized after a connection to the server and the account has been made
or, if this never happens, will be deleted if they are too old to be synchronized.

You might need to manually synchronize the folder, for example by pulling down.

You can view these messages, but you cannot move these messages again until the previous move has been confirmed.

Pending [operations](#user-content-faq3) are shown in the operations view accessible from the main navigation menu.

<br />

<a name="faq62"></a>
**(62) Which authentication methods are supported?**

The following authentication methods are supported and used in this order:

* CRAM-MD5
* LOGIN
* PLAIN
* NTLM (untested)
* XOAUTH2 ([Gmail](https://developers.google.com/gmail/imap/xoauth2-protocol), [Yandex](https://tech.yandex.com/oauth/))

SASL authentication methods, besides CRAM-MD5, are not supported
because [JavaMail for Android](https://javaee.github.io/javamail/Android) does not support SASL authentication.

If your provider requires an unsupported authentication method, you'll likely get the error message *authentication failed*.

[Client certificates](https://en.wikipedia.org/wiki/Client_certificate) can be selected in the account and identity settings.

[Server Name Indication](https://en.wikipedia.org/wiki/Server_Name_Indication) is supported
by [all supported Android versions](https://developer.android.com/training/articles/security-ssl).

<br />

<a name="faq63"></a>
**(63) How are images resized for displaying on screens?**

Large inline or attached [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) and [JPEG](https://en.wikipedia.org/wiki/JPEG) images
will automatically be resized for displaying on screens.
This is because email messages are limited in size, depending on the provider mostly between 10 and 50 MB.
Images will by default be resized to a maximum width and height of about 1440 pixels and saved with a compression ratio of 90 %.
Images are scaled down using whole number factors to reduce memory usage and to retain image quality.
Automatically resizing of inline and/or attached images and the maximum target image size can be configured in the send settings.

If you want to resize images on a case-by-case basis,
you can use [Send Reduced](https://f-droid.org/en/packages/mobi.omegacentauri.SendReduced/) or a similar app.

<br />

<a name="faq64"></a>
**~~(64) Can you add custom actions for swipe left/right?~~**

~~The most natural thing to do when swiping a list entry left or right is to remove the entry from the list.~~
~~The most natural action in the context of an email app is moving the message out of the folder to another folder.~~
~~You can select the folder to move to in the account settings.~~

~~Other actions, like marking messages read and snoozing messages are available via multiple selection.~~
~~You can long press a message to start multiple selection. See also [this question](#user-content-faq55).~~

~~Swiping left or right to mark a message read or unread is unnatural because the message first goes away and later comes back in a different shape.~~
~~Note that there is an advanced option to mark messages automatically read on moving,~~
~~which is in most cases a perfect replacement for the sequence mark read and move to some folder.~~
~~You can also mark messages read from new message notifications.~~

~~If you want to read a message later, you can hide it until a specific time by using the *snooze* menu.~~

<br />

<a name="faq65"></a>
**(65) Why are some attachments shown dimmed?**

Inline (image) attachments are shown dimmed.
[Inline attachments](https://tools.ietf.org/html/rfc2183) are supposed to be downloaded and shown automatically,
but since FairEmail doesn't always download attachments automatically, see also [this FAQ](#user-content-faq40),
FairEmail shows all attachment types. To distinguish inline and regular attachments, inline attachments are shown dimmed.

<br />

<a name="faq66"></a>
**(66) Is FairEmail available in the Google Play Family Library?**

The one time price of the few pro features is too low, lower than the price of most similar apps,
and there are [too many fees and taxes](#user-content-faq19),
to justify making FairEmail available in the [Google Play Family Library](https://support.google.com/googleone/answer/7007852).
Note that Google promotes the Family libray, but lets developers pay for it.

<br />

<a name="faq67"></a>
**(67) How can I snooze conversations?**

Multiple select one of more conversations (long press to start multiple selecting), tap the three dot button and select *Snooze ...*.
Alternatively, in the expanded message view use *Snooze ...* in the message three-dots 'more' menu or the time-lapse action in the bottom action bar.
Select the time the conversation(s) should snooze and confirm by tapping OK.
The conversations will be hidden for the selected time and shown again afterwards.
You will receive a new message notification as reminder.

It is also possible to snooze messages with [a rule](#user-content-faq71),
which will also allow you to move messages to a folder to let them be auto snoozed.

You can show snoozed messages by unchecking *Filter out* > *Hidden* in the three dot overflow menu.

You can tap on the small snooze icon to see until when a conversation is snoozed.

By selecting a zero snooze duration you can cancel snoozing.

Third party apps do not have access to the Gmail snoozed messages folder.

<br />

<a name="faq68"></a>
**~~(68) Why can Adobe Acrobat reader not open PDF attachments / Microsoft apps not open attached documents?~~**

~~Adobe Acrobat reader and Microsoft apps still expects full access to all stored files,~~
~~while apps should use the [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) since Android KitKat (2013)~~
~~to have access to actively shared files only. This is for privacy and security reasons.~~

~~You can workaround this by saving the attachment and opening it from the Adobe Acrobat reader / Microsoft app,~~
~~but you are advised to install an up-to-date and preferably open source PDF reader / document viewer,~~
~~for example one listed [here](https://github.com/offa/android-foss#-document--pdf-viewer).~~

<br />

<a name="faq69"></a>
**(69) Can you add auto scroll up on new message?**

The message list is automatically scrolled up when navigating from a new message notification or after a manual refresh.
Always automatically scrolling up on arrival of new messages would interfere with your own scrolling,
but if you like you can enable this in the settings.

<br />

<a name="faq70"></a>
**(70) When will messages be auto expanded?**

When navigation to a conversation one message will be expanded if:

* There is just one message in the conversation
* There is exactly one unread message in the conversation
* There is exactly one starred (favorite) message in the conversation (from version 1.1508)

There is one exception: the message was not downloaded yet
and the message is too large to download automatically on a metered (mobile) connection.
You can set or disable the maximum message size on the 'connection' settings tab.

Duplicate (archived) messages, trashed messages and draft messages are not counted.

Messages will automatically be marked read on expanding, unless this was disabled in the individual account settings.

<br />

<a name="faq71"></a>
**(71) How do I use filter rules?**

You can edit filter rules by long pressing a folder in the folder list of an account (tap the account name in the navigation/side menu).

New rules will be applied to new messages received in the folder, not to existing messages.
You can check the rule and apply the rule to existing messages or, alternatively, long press the rule in the rule list and select *Execute now*.

You'll need to give a rule a name and you'll need to define the order in which a rule should be executed relative to other rules.

You can disable a rule and you can stop processing other rules after a rule has been executed.

The following rule conditions are available:

* Sender contains or sender is contact
* Recipient contains
* Subject contains
* Has attachments (optional of specific type)
* Header contains
* Absolute time (received) between (since version 1.1540)
* Relative time (received) between

All the conditions of a rule need to be true for the rule action to be executed.
All conditions are optional, but there needs to be at least one condition, to prevent matching all messages.
If you want to match all senders or all recipients, you can just use the @ character as condition because all email addresses will contain this character.

Note that email addresses are formatted like this:

``
"Somebody" <somebody@example.org>
``

You can use multiple rules, possibly with a *stop processing*, for an *or* or a *not* condition.

Matching is not case sensitive, unless you use [regular expressions](https://en.wikipedia.org/wiki/Regular_expression).
Please see [here](https://developer.android.com/reference/java/util/regex/Pattern) for the documentation of Java regular expressions.
You can test a regex [here](https://regexr.com/).

Note that a regular expression supports an *or* operator, so if you want to match multiple senders, you can do this:

``
.*alice@example\.org.*|.*bob@example\.org.*|.*carol@example\.org.*
``

Note that [dot all mode](https://developer.android.com/reference/java/util/regex/Pattern#DOTALL) is enabled
to be able to match [unfolded headers](https://tools.ietf.org/html/rfc2822#section-3.2.3).

You can select one of these actions to apply to matching messages:

* No action (useful for *not*)
* Mark as read
* Mark as unread
* Hide
* Suppress notification
* Snooze
* Add star
* Set importance (local priority)
* Add keyword
* Move
* Copy (Gmail: label)
* Answer/forward (with template)
* Text-to-speech (sender and subject)
* Automation (Tasker, etc)

Rules are applied directly after the message header has been fetched, but before the message text has been downloaded,
so it is not possible to apply conditions to the message text.
Note that large message texts are downloaded on demand on a metered connection to save on data usage.

If you want to forward a message, consider to use the move action instead.
This will be more reliable than forwarding as well because forwarded messages might be considered as spam.

Since message headers are not downloaded and stored by default to save on battery and data usage and to save storage space
it is not possible to preview which messages would match a header rule condition.

Some common header conditions (regex):

* *.&ast;Auto-Submitted:.&ast;* [RFC3834](https://tools.ietf.org/html/rfc3834)
* *.&ast;Content-Type: multipart/report.&ast;* [RFC3462](https://tools.ietf.org/html/rfc3462)

In the three-dots *more* message menu there is an item to create a rule for a received message with the most common conditions filled in.

The POP3 protocol does not support setting keywords and moving or copying messages.

Using rules is a pro feature.

<br />

<a name="faq72"></a>
**(72) What are primary accounts/identities?**

The primary account is used when the account is ambiguous, for example when starting a new draft from the unified inbox.

Similarly, the primary identity of an account is used when the identity is ambiguous.

There can be just one primary account and there can be just one primary identity per account.

<br />

<a name="faq73"></a>
**(73) Is moving messages across accounts safe/efficient?**

Moving messages across accounts is safe because the raw, original messages will be downloaded and moved
and because the source messages will be deleted only after the target messages have been added

Batch moving messages across accounts is efficient if both the source folder and target folder are set to synchronize,
else FairEmail needs to connect to the folder(s) for each message.

<br />

<a name="faq74"></a>
**(74) Why do I see duplicate messages?**

Some providers, notably Gmail, list all messages in all folders, except trashed messages, in the archive (all messages) folder too.
FairEmail shows all these messages in a non obtrusive way to indicate that these messages are in fact the same message.

Gmail allows one message to have multiple labels, which are presented to FairEmail as folders.
This means that messages with multiple labels will be shown multiple times as well.

<br />

<a name="faq75"></a>
**(75) Can you make an iOS, Windows, Linux, etc version?**

A lot of knowledge and experience is required to successfully develop an app for a specific platform,
which is why I develop apps for Android only.

<br />

<a name="faq76"></a>
**(76) What does 'Clear local messages' do?**

The folder menu *Clear local messages* removes messages from the device which are present on the server too.
It does not delete messages from the server.
This can be useful after changing the folder settings to not download the message content (text and attachments), for example to save space.

<br />

<a name="faq77"></a>
**(77) Why are messages sometimes shown with a small delay?**

Depending on the speed of your device (processor speed and maybe even more memory speed) messages might be displayed with a small delay.
FairEmail is designed to dynamically handle a large number of messages without running out of memory.
This means that messages needs to be read from a database and that this database needs to be watched for changes, both of which might cause small delays.

Some convenience features, like grouping messages to display conversation threads and determining the previous/next message, take a little extra time.
Note that there is no *the* next message because in the meantime a new message might have been arrived.

When comparing the speed of FairEmail with similar apps this should be part of the comparison.
It is easy to write a similar, faster app which just displays a lineair list of messages while possible using too much memory,
but it is not so easy to properly manage resource usage and to offer more advanced features like conversation threading.

FairEmail is based on the state-of-the-art [Android architecture components](https://developer.android.com/topic/libraries/architecture/),
so there is little room for performance improvements.

<br />

<a name="faq78"></a>
**(78) How do I use schedules?**

In the receive settings you can enable scheduling and set a time period and the days of the week *when* messages should be *received*.
Note that an end time equal to or earlier than the start time is considered to be 24 hours later.

Automation, see below, can be used for more advanced schedules,
like for example multiple synchronization periods per day or different synchronization periods for different days.

It is possible to install FairEmail in multiple user profiles, for example a personal and a work profile, and to configure FairEmail differently in each profile,
which is another possibility to have different synchronization schedules and to synchronize a different set of accounts.

It is also possible to create [filter rules](#user-content-faq71) with a time condition and to snooze messages until the end time of the time condition.
This way it is possible to *snooze* business related messages until the start of the business hours.
This also means that the messages will be on your device for when there is (temporarily) no internet connection.

Note that recent Android versions allow overriding DND (Do Not Disturb) per notification channel and per app,
which could be used to (not) silence specific (business) notifications.
Please [see here](https://support.google.com/android/answer/9069335) for more information.

For more complex schemes you could set one or more accounts to manual synchronization
and send this command to FairEmail to check for new messages:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL
```

For a specific account:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL --es account Gmail
```

You can also automate turning receiving messages on and off by sending these commands to FairEmail:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE
```

To enable/disable a specific account:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE --es account Gmail
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE --es account Gmail
```

Note that disabling an account will hide the account and all associated folders and messages.

To set the poll interval:

```
(adb shell) adb shell am start-foreground-service -a eu.faircode.email.INTERVAL --ei minutes nnn
```

Where *nnn* is one of 0, 15, 30, 60, 120, 240, 480, 1440. A value of 0 means push messages.

You can automatically send commands with for example [Tasker](https://tasker.joaoapps.com/userguide/en/intents.html):

```
New task: Something recognizable
Action Category: Misc/Send Intent
Action: eu.faircode.email.ENABLE
Target: Service
```

To enable/disable an account with the name *Gmail*:

```
Extras: account:Gmail
```

Account names are case sensitive.

Scheduling is a pro feature.

<br />

<a name="faq79"></a>
**(79) How do I use synchronize on demand (manual)?**

Normally, FairEmail maintains a connection to the configured email servers whenever possible to receive messages in real-time.
If you don't want this, for example to be not disturbed or to save on battery usage, just disable receiving in the receive settings.
This will stop the background service which takes care of automatic synchronization and will remove the associated status bar notification.

You can also enable *Synchronize manually* in the advanced account settings if you want to manually synchronize specific accounts only.

You can use pull-down-to-refresh in a message list or use the folder menu *Synchronize now* to manually synchronize messages.

If you want to synchronize some or all folders of an account manually, just disable synchronization for the folders (but not of the account).

You'll likely want to disabled [browse on server](#user-content-faq24) too.

<br />

<a name="faq80"></a>
**~~(80) How do I fix the error 'Unable to load BODYSTRUCTURE' ?~~**

~~The error message *Unable to load BODYSTRUCTURE* is caused by bugs in the email server,~~
~~see [here](https://javaee.github.io/javamail/FAQ#imapserverbug) for more details.~~

~~FairEmail already tries to workaround these bugs, but if this fail you'll need to ask for support from your provider.~~

<br />

<a name="faq81"></a>
**~~(81) Can you make the background of the original message dark in the dark theme?~~**

~~The original message is shown as the sender has sent it, including all colors.~~
~~Changing the background color would not only make the original view not original anymore, it can also result in unreadable messages.~~

<br />

<a name="faq82"></a>
**(82) What is a tracking image?**

Please see [here](https://en.wikipedia.org/wiki/Web_beacon) about what a tracking image exactly is.
In short tracking images keep track if you opened a message.

FairEmail will in most cases automatically recognize tracking images and replace them by this icon:

![External image](https://github.com/M66B/FairEmail/blob/master/images/baseline_my_location_black_48dp.png)

Automatic recognition of tracking images can be disabled in the privacy settings.

<br />

<a name="faq84"></a>
**(84) What are local contacts for?**

Local contact information is based on names and addresses found in incoming and outgoing messages.

The main use of the local contacts storage is to offer auto completion when no contacts permission has been granted to FairEmail.

Another use is to generate [shortcuts](#user-content-faq31) on recent Android versions to quickly send a message to frequently contacted people.
This is also why the number of times contacted and the last time contacted is being recorded
and why you can make a contact a favorite or exclude it from favorites by long pressing it.

The list of contacts is sorted on number of times contacted and the last time contacted.

By default only names and addresses to whom you send messages to will be recorded.
You can change this in the send settings.

<br />

<a name="faq85"></a>
**(85) Why is an identity not available?**

An identity is available for sending a new message or replying or forwarding an existing message only if:

* the identity is set to synchronize (send messages)
* the associated account is set to synchronize (receive messages)
* the associated account has a drafts folder

FairEmail will try to select the best identity based on the *to* address of the message replied to / being forwarded.

<br />

<a name="faq86"></a>
**~~(86) What are 'extra privacy features'?~~**

~~The advanced option *extra privacy features* enables:~~

* ~~Looking up the owner of the IP address of a link~~
* ~~Detection and removal of [tracking images](#user-content-faq82)~~

<br />

<a name="faq87"></a>
**(87) What does 'invalid credentials' mean?**

The error message *invalid credentials* means either that the user name and/or password is incorrect,
for example because the password was changed or expired, or that the account authorization has expired.

If the password is incorrect/expired, you will have to update the password in the account and/or identity settings.

If the account authorization has expired, you will have to select the account again.
You will likely need to save the associated identity again as well.

<br />

<a name="faq88"></a>
**(88) How can I use a Yahoo, AOL or Sky account?**

The preferred way to set up a Yahoo account is by using the quick setup wizard,
which will use OAuth instead of a password and is therefore safer (and easier as well).

To authorize a Yahoo, AOL, or Sky account you will need to create an app password.
For instructions, please see here:

* [for Yahoo](https://help.yahoo.com/kb/generate-third-party-passwords-sln15241.html)
* [for AOL](https://help.aol.com/articles/Create-and-manage-app-password)
* [for Sky](https://www.sky.com/help/articles/getting-started-with-sky-yahoo-mail) (under *Other email apps*)

Please see [this FAQ](#user-content-faq111) about OAuth support.

Note that Yahoo, AOL, and Sky do not support standard push messages.
The Yahoo email app uses a proprietary, undocumented protocol for push messages.

Push messages require [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE) and the Yahoo email server does not report IDLE as capability:

```
Y1 CAPABILITY
* CAPABILITY IMAP4rev1 ID MOVE NAMESPACE XYMHIGHESTMODSEQ UIDPLUS LITERAL+ CHILDREN X-MSG-EXT UNSELECT OBJECTID
Y1 OK CAPABILITY completed
```

<br />

<a name="faq89"></a>
**(89) How can I send plain text only messages?**

By default FairEmail sends each message both as plain text and as HTML formatted text because almost every receiver expects formatted messages these days.
If you want/need to send plain text messages only, you can enable this in the advanced identity options.
You might want to create a new identity for this if you want/need to select sending plain text messages on a case-by-case basis.

<br />

<a name="faq90"></a>
**(90) Why are some texts linked while not being a link?**

FairEmail will automatically link not linked web links (http and https) and not linked email addresses (mailto) for your convenience.
However, texts and links are not easily distinguished,
especially not with lots of [top level domains](https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains) being words.
This is why texts with dots are sometimes incorrectly recognized as links, which is better than not recognizing some links.

Links for the tel, geo, rtsp and xmpp protocols will be recognized too,
but links for less usual or less safe protocols like telnet and ftp will not be recognized.
The regex to recognize links is already *very* complex and adding more protocols will make it only slower and possibly cause errors.

Note that original messages are shown exactly as they are, which means also that links are not automatically added.

<br />

<a name="faq91"></a>
**~~(91) Can you add periodical synchronization to save battery power?~~**

~~Synchronizing messages is an expensive proces because the local and remote messages need to be compared,~~
~~so periodically synchronizing messages will not result in saving battery power, more likely the contrary.~~

~~See [this FAQ](#user-content-faq39) about optimizing battery usage.~~

<br />

<a name="faq92"></a>
**(92) Can you add spam filtering, verification of the DKIM signature and SPF authorization?**

Spam filtering, verification of the [DKIM](https://en.wikipedia.org/wiki/DomainKeys_Identified_Mail) signature
and [SPF](https://en.wikipedia.org/wiki/Sender_Policy_Framework) authorization is a task of email servers, not of an email client.
Servers generally have more memory and computing power, so they are much better suited to this task than battery-powered devices.
Also, you'll want spam filtered for all your email clients, possibly including web email, not just one email client.
Moreover, email servers have access to information, like the IP address, etc of the connecting server, which an email client has no access to.

Spam filtering based on message headers might have been feasible,
but unfortunately this technique is [patented by Microsoft](https://patents.google.com/patent/US7543076).

Recent versions of FairEmail can filter spam to a certain extend using a message classifier.
Please see [this FAQ](#user-content-faq163) for more information about this.

Of course you can report messages as spam with FairEmail,
which will move the reported messages to the spam folder and train the spam filter of the provider, which is how it is supposed to work.
This can be done automatically with [filter rules](#user-content-faq71) too.
Blocking the sender will create a filter rule to automatically move future messages of the same sender into the spam folder.

Note that the POP3 protocol gives access to the inbox only. So, it is won't be possible to report spam for POP3 accounts.

Note that you should not delete spam messages, also not from the spam folder,
because the email server uses the messages in the spam folder to "learn" what spam messages are.

If you receive a lot of spam messages in your inbox, the best you can do is to contact the email provider to ask if spam filtering can be improved.

Also, FairEmail can show a small red warning flag
when DKIM, SPF or [DMARC](https://en.wikipedia.org/wiki/DMARC) authentication failed on the receiving server.
You can enable/disable [authentication verification](https://en.wikipedia.org/wiki/Email_authentication) in the display settings.

FairEmail can show a warning flag too if the domain name of the (reply) email address of the sender does not define an MX record pointing to an email server.
This can be enabled in the receive settings. Be aware that this will slow down synchronization of messages significantly.

If the domain name of the sender and the domain name of the reply address differ,
the warning flag will be shown too because this is most often the case with phishing messages.
If desired, this can be disabled in the receive settings (from version 1.1506).

If legitimate messages are failing authentication, you should notify the sender because this will result in a high risk of messages ending up in the spam folder.
Moreover, without proper authentication there is a risk the sender will be impersonated.
The sender might use [this tool](https://www.mail-tester.com/) to check authentication and other things.

<br />

<a name="faq93"></a>
**(93) Can you allow installation/data storage on external storage media (sdcard)?**

FairEmail uses services and alarms, provides widgets and listens for the boot completed event to be started on device start,
so it is not possible to store the app on external storage media, like an sdcard.
See also [here](https://developer.android.com/guide/topics/data/install-location).

Messages, attachments, etc stored on external storage media, like an sdcard, can be accessed by other apps and is therefore not safe.
See [here](https://developer.android.com/training/data-storage) for the details.

When needed you can save (raw) messages via the three-dots menu just above the message text
and save attachments by tapping on the floppy icon.

If you need to save on storage space, you can limit the number of days messages are being synchronized and kept for.
You can change these settings by long pressing a folder in the folder list and selecting *Edit properties*.

<br />

<a name="faq94"></a>
**(94) What does the red/orange stripe at the end of the header mean?**

The red/orange stripe at the left side of the header means that the DKIM, SPF or DMARC authentication failed.
See also [this FAQ](#user-content-faq92).

<br />

<a name="faq95"></a>
**(95) Why are not all apps shown when selecting an attachment or image?**

For privacy and security reasons FairEmail does not have permissions to directly access files,
instead the Storage Access Framework, available and recommended since Android 4.4 KitKat (released in 2013), is used to select files.

If an app is listed depends on if the app implements a [document provider](https://developer.android.com/guide/topics/providers/document-provider).
If the app is not listed, you might need to ask the developer of the app to add support for the Storage Access Framework.

Android Q will make it harder and maybe even impossible to directly access files,
see [here](https://developer.android.com/preview/privacy/scoped-storage) and [here](https://www.xda-developers.com/android-q-storage-access-framework-scoped-storage/) for more details.

<br />

<a name="faq96"></a>
**(96) Where can I find the IMAP and SMTP settings?**

The IMAP settings are part of the (custom) account settings and the SMTP settings are part of the identity settings.

<br />

<a name="faq97"></a>
**(97) What is 'cleanup' ?**

About each four hours FairEmail runs a cleanup job that:

* Removes old message texts
* Removes old attachment files
* Removes old image files
* Removes old local contacts
* Removes old log entries

Note that the cleanup job will only run when the synchronize service is active.

<br />

<a name="faq98"></a>
**(98) Why can I still pick contacts after revoking contacts permissions?**

After revoking contacts permissions Android does not allow FairEmail access to your contacts anymore.
However, picking contacts is delegated to and done by Android and not by FairEmail, so this will still be possible without contacts permissions.

<br />

<a name="faq99"></a>
**(99) Can you add a rich text or markdown editor?**

FairEmail provides common text formatting (bold, italic, underline, text size and color) via a toolbar that appears after selecting some text.

A [Rich text](https://en.wikipedia.org/wiki/Formatted_text) or [Markdown](https://en.wikipedia.org/wiki/Markdown) editor
would not be used by many people on a small mobile device and, more important,
Android doesn't support a rich text editor and most rich text editor open source projects are abandoned.
See [here](https://forum.xda-developers.com/showpost.php?p=79061829&postcount=4919) for some more details about this.

<br />

<a name="faq100"></a>
**(100) How can I synchronize Gmail categories?**

You can synchronize Gmail categories by creating filters to label categorized messages:

* Create a new filter via Gmail > Settings (wheel) > Filters and Blocked Addresses > Create a new filter
* Enter a category search (see below) in the *Has the words* field and click *Create filter*
* Check *Apply the label* and select a label and click *Create filter*

Possible categories:

```
category:social
category:updates
category:forums
category:promotions
```

Unfortunately, this is not possible for snoozed messages folder.

You can use *Force sync* in the three-dots menu of the unified inbox to let FairEmail synchronize the folder list again
and you can long press the folders to enable synchronization.

<br />

<a name="faq101"></a>
**(101) What does the blue/orange dot at the bottom of the conversations mean?**

The dot shows the relative position of the conversation in the message list.
The dot will be show orange when the conversation is the first or last in the message list, else it will be blue.
The dot is meant as an aid when swiping left/right to go to the previous/next conversation.

The dot is disabled by default and can be enabled with the display settings *Show relative conversation position with a dot*.

<br />

<a name="faq102"></a>
**(102) How can I enable auto rotation of images?**

Images will automatically be rotated when automatic resizing of images is enabled in the settings (enabled by default).
However, automatic rotating depends on the [Exif](https://en.wikipedia.org/wiki/Exif) information to be present and to be correct,
which is not always the case. Particularly not when taking a photo with a camara app from FairEmail.

Note that only [JPEG](https://en.wikipedia.org/wiki/JPEG) and [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) images can contain Exif information.

<br />

<a name="faq104"></a>
**(104) What do I need to know about error reporting?**

* Error reports will help improve FairEmail
* Error reporting is optional and opt-in
* Error reporting can be enabled/disabled in the settings, section miscellaneous
* Error reports will automatically be sent anonymously to [Bugsnag](https://www.bugsnag.com/)
* Bugsnag for Android is [open source](https://github.com/bugsnag/bugsnag-android)
* See [here](https://docs.bugsnag.com/platforms/android/automatically-captured-data/) about what data will be sent in case of errors
* See [here](https://docs.bugsnag.com/legal/privacy-policy/) for the privacy policy of Bugsnag
* Error reports will be sent to *sessions.bugsnag.com:443* and *notify.bugsnag.com:443*

<br />

<a name="faq105"></a>
**(105) How does the roam-like-at-home option work?**

FairEmail will check if the country code of the SIM card and the country code of the network
are in the [EU roam-like-at-home countries](https://en.wikipedia.org/wiki/European_Union_roaming_regulations#Territorial_extent)
and assumes no roaming if the country codes are equal and the advanced roam-like-at-home option is enabled.

So, you don't have to disable this option if you don't have an EU SIM or are not connected to an EU network.

<br />

<a name="faq106"></a>
**(106) Which launchers can show a badge count with the number of unread messages?**

Please [see here](https://github.com/leolin310148/ShortcutBadger#supported-launchers)
for a list of launchers which can show the number of unread messages.

Note that Nova Launcher requires Tesla Unread, which is [not supported anymore](https://forum.xda-developers.com/android/general/bad-news-tesla-unread-devoloper-t3920415).

Note that the notification setting *Show launcher icon with number of new messages* needs to be enabled (default enabled).

Only *new* unread messages in folders set to show new message notifications will be counted,
so messages marked unread again and messages in folders set to not show new message notification will not be counted.

Depending on what you want, the notification settings *Let the number of new messages match the number of notifications* needs to be enabled (default disabled).
When enabled the badge count will be the same as the number of new message notifications.
When disabled the badge count will be the number of unread messages, independent if they are shown in a notification or are new.

This feature depends on support of your launcher.
FairEmail merely 'broadcasts' the number of unread messages using the ShortcutBadger library.
If it doesn't work, this cannot be fixed by changes in FairEmail.

Some launchers display a dot or a '1' for [the monitoring notification](#user-content-faq2),
despite FairEmail explicitly requesting not to show a *badge* for this notification.
This could be caused by a bug in the launcher app or in your Android version.
Please double check if the notification dot (badge) is disabled for the receive (service) notification channel.
You can go to the right notification channel settings via the notification settings of FairEmail.
This might not be obvious, but you can tap on the channel name for more settings.

FairEmail does send a new message count intent as well:

```
eu.faircode.email.NEW_MESSAGE_COUNT
```

The number of new, unread messages will be in an integer "*count*" parameter.

<br />

<a name="faq107"></a>
**(107) How do I use colored stars?**

You can set a colored star via the *more* message menu, via multiple selection (started by long pressing a message),
by long pressing a star in a conversation or automatically by using [rules](#user-content-faq71).

You need to know that colored stars are not supported by the IMAP protocol and can therefore not be synchronized to an email server.
This means that colored stars will not be visible in other email clients and will be lost on downloading messages again.
However, the stars (without color) will be synchronized and will be visible in other email clients, when supported.

Some email clients use IMAP keywords for colors.
However, not all servers support IMAP keywords and besides that there are no standard keywords for colors.

<br />

<a name="faq108"></a>
**~~(108) Can you add permanently delete messages from any folder?~~**

~~When you delete messages from a folder the messages will be moved to the trash folder, so you have a chance to restore the messages.~~
~~You can permanently delete messages from the trash folder.~~
~~Permanently delete messages from other folders would defeat the purpose of the trash folder, so this will not be added.~~

<br />

<a name="faq109"></a>
**~~(109) Why is 'select account' available in official versions only?~~**

~~Using *select account* to select and authorize Google accounts require special permission from Google for security and privacy reasons.~~
~~This special permission can only be acquired for apps a developer manages and is responsible for.~~
~~Third party builds, like the F-Droid builds, are managed by third parties and are the responsibility of these third parties.~~
~~So, only these third parties can acquire the required permission from Google.~~
~~Since these third parties do not actually support FairEmail, they are most likely not going to request the required permission.~~

~~You can solve this in two ways:~~

* ~~Switch to the official version of FairEmail, see [here](https://github.com/M66B/FairEmail/blob/master/README.md#downloads) for the options~~
* ~~Use app specific passwords, see [this FAQ](#user-content-faq6)~~

~~Using *select account* in third party builds is not possible in recent versions anymore.~~
~~In older versions this was possible, but it will now result in the error *UNREGISTERED_ON_API_CONSOLE*.~~

<br />

<a name="faq110"></a>
**(110) Why are (some) messages empty and/or attachments corrupt?**

Empty messages and/or corrupt attachments are probably being caused by a bug in the server software.
Older Microsoft Exchange software is known to cause this problem.
Mostly you can workaround this by disabling *Partial fetch* in the advanced account settings:

Settings > Manual setup > Accounts > tap account > tap advanced > Partial fetch > uncheck

After disabling this setting, you can use the message 'more' (three dots) menu to 'resync' empty messages.
Alternatively, you can *Delete local messages* by long pressing the folder(s) in the folder list and synchronize all messages again.

Disabling *Partial fetch* will result in more memory usage.

<br />

<a name="faq111"></a>
**(111) Is OAuth supported?**

OAuth for Gmail is supported via the quick setup wizard.
The Android account manager will be used to fetch and refresh OAuth tokens for selected on-device accounts.
OAuth for non on-device accounts is not supported
because Google requires a [yearly security audit](https://support.google.com/cloud/answer/9110914) ($15,000 to $75,000) for this.
You can read more about this [here](https://www.theregister.com/2019/02/11/google_gmail_developer/).

OAuth for Yandex and Yahoo is supported via the quick setup wizard.

OAuth for Office 365 accounts is supported, but Microsoft does not offer OAuth for Outlook, Live and Hotmail accounts (yet?).

<br />

<a name="faq112"></a>
**(112) Which email provider do you recommend?**

FairEmail is an email client only, so you need to bring your own email address.
Note that this is clearly mentioned in the app description.

There are plenty of email providers to choose from.
Which email provider is best for you depends on your wishes/requirements.
Please see the websites of [Restore privacy](https://restoreprivacy.com/secure-email/) or [Privacy Tools](https://www.privacytools.io/providers/email/)
for a list of privacy oriented email providers with advantages and disadvantages.

Some providers, like ProtonMail, Tutanota, use proprietary email protocols, which make it impossible to use third party email apps.
Please see [this FAQ](#user-content-faq129) for more information.

Using your own (custom) domain name, which is supported by most email providers, will make it easier to switch to another email provider.

<br />

<a name="faq113"></a>
**(113) How does biometric authentication work?**

If your device has a biometric sensor, for example a fingerprint sensor, you can enable/disable biometric authentication in the navigation (hamburger) menu of the settings screen.
When enabled FairEmail will require biometric authentication after a period of inactivity or after the screen has been turned off while FairEmail was running.
Activity is navigation within FairEmail, for example opening a conversation thread.
The inactivity period duration can be configured in the miscellaneous settings.
When biometric authentication is enabled new message notifications will not show any content and FairEmail won't be visible on the Android recents screen.

Biometric authentication is meant to prevent others from seeing your messages only.
FairEmail relies on device encryption for data encryption, see also [this FAQ](#user-content-faq37).

Biometric authentication is a pro feature.

<br />

<a name="faq114"></a>
**(114) Can you add an import for the settings of other email apps?**

The format of the settings files of most other email apps is not documented, so this is difficult.
Sometimes it is possible to reverse engineer the format, but as soon as the settings format changes things will break.
Also, settings are often incompatible.
For example, FairEmail has unlike most other email apps settings for the number of days to synchronize messages
and for the number of days to keep messages, mainly to save on battery usage.
Moreover, setting up an account/identity with the quick setup wizard is simple, so it is not really worth the effort.

<br />

<a name="faq115"></a>
**(115) Can you add email address chips?**

Email address [chips](https://material.io/design/components/chips.html) look nice, but cannot be edited,
which is quite inconvenient when you made a typo in an email address.

Note that FairEmail will select the address only when long pressing an address, which makes it easy to delete an address.

Chips are not suitable for showing in a list
and since the message header in a list should look similar to the message header of the message view it is not an option to use chips for viewing messages.

Reverted [commit](https://github.com/M66B/FairEmail/commit/2c80c25b8aa75af2287f471b882ec87d5a5a5015).

<br />

<a name="faq116"></a>
**~~(116) How can I show images in messages from trusted senders by default?~~**

~~You can show images in messages from trusted senders by default by enabled the display setting *Automatically show images for known contacts*.~~

~~Contacts in the Android contacts list are considered to be known and trusted,~~
~~unless the contact is in the group / has the label '*Untrusted*' (case insensitive).~~

<br />

<a name="faq38"></a>
<a name="faq117"></a>
**(117) Can you help me restore my purchase?**

First of all, a purchase will be available on all devices logged into the same Google account,
*if* the app is installed via the same Google account too. You can select the account in the Play store app.

Google manages all purchases, so as a developer I have little control over purchases.
So, basically the only thing I can do, is give some advice:

* Make sure you have an active, working internet connection
* Make sure you are logged in with the right Google account and that there is nothing wrong with your Google account
* Make sure you installed FairEmail via the right Google account if you configured multiple Google accounts on your device
* Make sure the Play store app is up to date, please [see here](https://support.google.com/googleplay/answer/1050566?hl=en)
* Open the Play store app and wait at least a minute to give it time to synchronize with the Google servers
* Open FairEmail and navigate to the pro features screen to let FairEmail check the purchases; sometimes it help to tap the *buy* button

You can also try to clear the cache of the Play store app via the Android apps settings.
Restarting the device might be necessary to let the Play store recognize the purchase correctly.

Note that:

* If you get *ITEM_ALREADY_OWNED*, the Play store app probably needs to be updated, please [see here](https://support.google.com/googleplay/answer/1050566?hl=en)
* Purchases are stored in the Google cloud and cannot get lost
* There is no time limit on purchases, so they cannot expire
* Google does not expose details (name, e-mail, etc) about buyers to developers
* An app like FairEmail cannot select which Google account to use
* It may take a while until the Play store app has synchronized a purchase to another device
* Play Store purchases cannot be used without the Play Store, which is also not allowed by Play Store rules

If you cannot solve the problem with the purchase, you will have to contact Google about it.

<br />

<a name="faq118"></a>
**(118) What does 'Remove tracking parameters' exactly?**

Checking *Remove tracking parameters* will remove all [UTM parameters](https://en.wikipedia.org/wiki/UTM_parameters) from a link.

<br />

<a name="faq119"></a>
**~~(119) Can you add colors to the unified inbox widget?~~**

~~The widget is designed to look good on most home/launcher screens by making it monochrome and by using a half transparent background.~~
~~This way the widget will nicely blend in, while still being properly readable.~~

~~Adding colors will cause problems with some backgrounds and will cause readability problems, which is why this won't be added.~~

Due to Android limitations it is not possible to dynamically set the opacity of the background and to have rounded corners at the same time.

<br />

<a name="faq120"></a>
**(120) Why are new message notifications not removed on opening the app?**

New message notifications will be removed on swiping notifications away or on marking the associated messages read.
Opening the app will not remove new message notifications.
This gives you a choice to leave new message notifications as a reminder that there are still unread messages.

On Android 7 Nougat and later new message notifications will be [grouped](https://developer.android.com/training/notify-user/group).
Tapping on the summary notification will open the unified inbox.
The summary notification can be expanded to view individual new message notifications.
Tapping on an individual new message notification will open the conversation the message it is part of.
See [this FAQ](#user-content-faq70) about when messages in a conversation will be auto expanded and marked read.

<br />

<a name="faq121"></a>
**(121) How are messages grouped into a conversation?**

By default FairEmail groups messages in conversations. This can be turned of in the display settings.

FairEmail groups messages based on the standard *Message-ID*, *In-Reply-To* and *References* headers.
FairEmail does not group on other criteria, like the subject,
because this could result in grouping unrelated messages and would be at the expense of increased battery usage.

<br />

<a name="faq122"></a>
**~~(122) Why is the recipient name/email address show with a warning color?~~**

~~The recipient name and/or email address in the addresses section will be shown in a warning color~~
~~when the sender domain name and the domain name of the *to* address do not match.~~
~~Mostly this indicates that the message was received *via* an account with another email address.~~

<br />

<a name="faq123"></a>
**(123) What will happen when FairEmail cannot connect to an email server?**

If FairEmail cannot connect to an email server to synchronize messages,
for example if the internet connection is bad or a firewall or a VPN is blocking the connection,
FairEmail will retry one time after waiting 8 seconds while keeping the device awake (=use battery power).
If this fails, FairEmail will schedule an alarm to retry after 15, 30 and eventually every 60 minutes and let the device sleep (=no battery usage).

Note that [Android doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby)
does not allow to wake the device earlier than after 15 minutes.

*Force sync* in the three-dots menu of the unified inbox can be used to let FairEmail attempt to reconnect without waiting.

Sending messages will be retried on connectivity changes only
(reconnecting to the same network or connecting to another network)
to prevent the email server from blocking the connection permanently.
You can pull down the outbox to retry manually.

Note that sending will not be retried in case of authentication problems and when the server rejected the message.
In this case you can pull down the outbox to try again.

<br />

<a name="faq124"></a>
**(124) Why do I get 'Message too large or too complex to display'?**

The message *Message too large or too complex to display* will be shown if there are more than 100,000 characters or more than 500 links in a message.
Reformatting and displaying such messages will take too long. You can try to use the original message view, powered by the browser, instead.

<br />

<a name="faq125"></a>
**(125) What are the current experimental features?**

*Message classification (version 1.1438+)*

Please see [this FAQ](#user-content-faq163) for details.

Since this is an experimental feature, my advice is to start with just one folder.

<br />

*Send hard bounce (version 1.1477+)*

Send a [Delivery Status Notification](https://tools.ietf.org/html/rfc3464) (=hard bounce) via the reply/answer menu.

Hard bounces will mostly be processed automatically because they affect the reputation of the email provider.
The bounce address (=*Return-Path* header) is mostly very specific, so the email server can determine the sending account.

For some background, see for [this Wikipedia article](https://en.wikipedia.org/wiki/Bounce_message).

<br />

<a name="faq126"></a>
**(126) Can message previews be sent to my wearable?**

FairEmail fetches a message in two steps:

1. Fetch message headers
1. Fetch message text and attachments

Directly after the first step new messages will be notified.
However, only until after the second step the message text will be available.
FairEmail updates exiting notifications with a preview of the message text, but unfortunately wearable notifications cannot be updated.

Since there is no guarantee that a message text will always be fetched directly after a message header,
it is not possible to guarantee that a new message notification with a preview text will always be sent to a wearable.

If you think this is good enough, you can enable the notification option *Only send notifications with a message preview to wearables*
and if this does not work, you can try to enable the notification option *Show notifications with a preview text only*.
Note that this applies to wearables not showing a preview text too, even when the Android Wear app says the notification has been sent (bridged).

If you want to have the full message text sent to your wearable, you can enable the notification option *Preview all text*.
Note that some wearables are known to crash with this option enabled.

If you use a Samsung wearable with the Galaxy Wearable (Samsung Gear) app, you might need to enable notifications for FairEmail
when the setting *Notifications*, *Apps installed in the future* is turned off in this app.

<br />

<a name="faq127"></a>
**(127) How can I fix 'Syntactically invalid HELO argument(s)'?**

The error *... Syntactically invalid HELO argument(s) ...* means that the SMTP server rejected the local IP address or host name.
You can likely fix this error by enabling or disabling the advanced indentity option *Use local IP address instead of host name*.

<br />

<a name="faq128"></a>
**(128) How can I reset asked questions, for example to show images?**

You can reset asked questions via the three dots overflow menu in the miscellaneous settings.

<br />

<a name="faq129"></a>
**(129) Are ProtonMail, Tutanota supported?**

ProtonMail uses a proprietary email protocol
and [does not directly support IMAP](https://protonmail.com/support/knowledge-base/imap-smtp-and-pop3-setup/),
so you cannot use FairEmail to access ProtonMail.

Tutanota uses a proprietary email protocol
and [does not support IMAP](https://tutanota.com/faq/#imap),
so you cannot use FairEmail to access Tutanota.

<br />

<a name="faq130"></a>
**(130) What does message error ... mean?**

A series of lines with orangish or red texts with technical information means that debug mode was enabled in the miscellaneous settings.

The warning *No server found at ...* means that there was no email server registered at the indicated domain name.
Replying to the message might not be possible and might result in an error.
This could indicate a falsified email address and/or spam.

The error *... ParseException ...* means that there is a problem with a received message, likely caused by a bug in the sending software.
FairEmail will workaround this is in most cases, so this message can mostly be considered as a warning instead of an error.

The error *...SendFailedException...* means that there was a problem while sending a message.
The error will almost always include a reason. Common reasons are that the message was too big or that one or more recipient addresses were invalid.

The warning *Message too large to fit into the available memory* means that the message was larger than 10 MiB.
Even if your device has plenty of storage space Android provides limited working memory to apps, which limits the size of messages that can be handled.

Please see [here](#user-content-faq22) for other error messages in the outbox.

<br />

<a name="faq131"></a>
**(131) Can you change the direction for swiping to previous/next message?**

If you read from left to right, swiping to the left will show the next message.
Similarly, if you read from right to left, swiping to the right will show the next message.

This behavior seems quite natural to me, also because it is similar to turning pages.

Anyway, there is a behavior setting to reverse the swipe direction.

<br />

<a name="faq132"></a>
**(132) Why are new message notifications silent?**

Notifications are silent by default on some MIUI versions.
Please see [here](http://en.miui.com/thread-3930694-1-1.html) how you can fix this.

There is a bug in some Android versions
causing [setOnlyAlertOnce](https://developer.android.com/reference/android/app/Notification.Builder#setOnlyAlertOnce(boolean)) to mute notifications.
Since FairEmail shows new message notifications right after fetching the message headers
and FairEmail needs to update new message notifications after fetching the message text later, this cannot be fixed or worked around by FairEmail.

Android might rate limit the notification sound, which can cause some new message notifications to be silent.

<br />

<a name="faq133"></a>
**(133) Why is ActiveSync not supported?**

The Microsoft Exchange ActiveSync protocol [is patented](https://en.wikipedia.org/wiki/Exchange_ActiveSync#Licensing) and can therefore not be supported.
For this reason you won't find many, if any, other email clients supporting ActiveSync.

Note that the desciption of FairEmail starts with the remark
that non-standard protocols, like Microsoft Exchange Web Services and Microsoft ActiveSync are not supported.

<br />

<a name="faq134"></a>
**(134) Can you add deleting local messages?**

*POP3*

In the account settings (Settings, tap Manual setup, tap Accounts, tap account) you can enable *Leave deleted messages on server*.

*IMAP*

Since the IMAP protocol is meant to synchronize two ways,
deleting a message from the device would result in fetching the message again when synchronizing again.

However, FairEmail supports hiding messages,
either via the three-dots menu in the action bar just above the message text
or by multiple selecting messages in the message list.
Basically this is the same as "leave on server" of the POP3 protocol
with the advantage that you can show the messages again when needed.

Note that it is possible to set the swipe left or right action to hide a message.

<br />

<a name="faq135"></a>
**(135) Why are trashed messages and drafts shown in conversations?**

Individual messages will rarely be trashed and mostly this happens by accident.
Showing trashed messages in conversations makes it easier to find them back.

You can permanently delete a message using the message three-dots *delete* menu, which will remove the message from the conversation.
Note that this irreversible.

Similarly, drafts are shown in conversations to find them back in the context where they belong.
It is easy to read through the received messages before continuing to write the draft later.

<br />

<a name="faq136"></a>
**(136) How can I delete an account/identity/folder?**

Deleting an account/identity/folder is a little bit hidden to prevent accidents.

* Account: Settings > Manual setup > Accounts > tap account
* Identity: Settings > Manual setup > Identities > tap identity
* Folder: Long press the folder in the folder list > Edit properties

In the three-dots overflow menu at the top right there is an item to delete the account/identity/folder.

<br />

<a name="faq137"></a>
**(137) How can I reset 'Don't ask again'?**

You can reset all questions set to be not asked again in the miscellaneous settings.

<br />

<a name="faq138"></a>
**(138) Can you add calendar/contact/tasks/notes management?**

Calendar, contact, task and note management can better be done by a separate, specialized app.
Note that FairEmail is a specialized email app, not an office suite.

Also, I prefer to do a few things very well, instead of many things only half.
Moreover, from a security perspective, it is not a good idea to grant many permissions to a single app.

You are advised to use the excellent, open source [DAVx⁵](https://f-droid.org/packages/at.bitfire.davdroid/) app to synchronize/manage your calendars/contacts.

Most providers support exporting your contacts.
Please [see here](https://support.google.com/contacts/answer/1069522) about how you can import contacts if synchronizing is not possible.

Note that FairEmail does support replying to calendar invites (a pro feature) and adding calendar invites to your personal calendar.

<br />

<a name="faq83"></a>
<a name="faq139"></a>
**(139) How do I fix 'User is authenticated but not connected'?**

In fact this Microsoft Exchange specific error is an incorrect error message caused by a bug in older Exchange server software.

The error *User is authenticated but not connected* might occur if:

* Push messages are enabled for too many folders: see [this FAQ](#user-content-faq23) for more information and a workaround
* The account password was changed: changing it in FairEmail too should fix the problem
* An alias email address is being used as username instead of the primary email address
* An incorrect login scheme is being used for a shared mailbox: the right scheme is *username@domain\SharedMailboxAlias*

The shared mailbox alias will mostly be the email address of the shared account, like this:

```
you@example.com\shared@example.com
```

Note that it should be a backslash and not a forward slash.

When using a shared mailbox, you'll likely want to enable the option *Synchronize shared folder lists* in the receive settings.

<br />

<a name="faq140"></a>
**(140) Why does the message text contain strange characters?**

Displaying strange characters is almost always caused by specifying no or an invalid character encoding by the sending software.
FairEmail will assume [ISO 8859-1](https://en.wikipedia.org/wiki/ISO/IEC_8859-1)
when no character set or when [US-ASCII](https://en.wikipedia.org/wiki/ASCII) was specified.
Other than that there is no way to reliably determine the correct character encoding automatically,
so this cannot be fixed by FairEmail. The right action is to complain to the sender.

<br />

<a name="faq141"></a>
**(141) How can I fix 'A drafts folder is required to send messages'?**

To store draft messages a drafts folder is required.
In most cases FairEmail will automatically select the drafts folders on adding an account
based on [the attributes](https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml) the email server sends.
However, some email servers are not configured properly and do not send these attributes.
In this case FairEmail tries to identify the drafts folder by name,
but this might fail if the drafts folder has an unusual name or is not present at all.

You can fix this problem by manually selecting the drafts folder in the account settings (Settings, tap Manual setup, tap Accounts, tap account, at the bottom).
If there is no drafts folder at all,
you can create a drafts folder by tapping on the '+' button in the folder list of the account (tap on the account name in the navigation menu).

Some providers, like Gmail, allow enabling/disabling IMAP for individual folders.
So, if a folder is not visible, you might need to enable IMAP for the folder.

Quick link for Gmail (will work on a desktop computer only): [https://mail.google.com/mail/u/0/#settings/labels](https://mail.google.com/mail/u/0/#settings/labels)

<br />

<a name="faq142"></a>
**(142) How can I store sent messages in the inbox?**

Generally, it is not a good idea to store sent messages in the inbox because this is hard to undo and could be incompatible with other email clients.

That said, FairEmail is able to properly handle sent messages in the inbox.
FairEmail will mark outgoing messages with a sent messages icon for example.

The best solution would be to enable showing the sent folder in the unified inbox
by long pressing the sent folder in the folder list and enabling *Show in unified inbox*.
This way all messages can stay where they belong, while allowing to see both incoming and outgoing messages at one place.

If this is not an option, you can [create a rule](#user-content-faq71) to automatically move sent messages to the inbox
or set a default CC/BCC address in the advanced identity settings to send yourself a copy.

<br />

<a name="faq143"></a>
**~~(143) Can you add a trash folder for POP3 accounts?~~**

[POP3](https://en.wikipedia.org/wiki/Post_Office_Protocol) is a very limited protocol.
Basically only messages can be downloaded and deleted from the inbox.
It is not even possible to mark a message read.

Since POP3 does not allow access to the trash folder at all, there is no way to restore trashed messages.

Note that you can hide messages and search for hidden messages, which is similar to a local trash folder,
without suggesting that trashed messages can be restored, while this is actually not possible.

Version 1.1082 added a local trash folder.
Note that trashing a message will permanently remove it from the server and that trashed messages cannot be restored to the server anymore.

<br />

<a name="faq144"></a>
**(144) How can I record voice notes?**

To record voice notes you can press this icon in the bottom action bar of the message composer:

![External image](https://github.com/M66B/FairEmail/blob/master/images/baseline_record_voice_over_black_48dp.png)

This requires a compatible audio recorder app to be installed.
In particular [this common intent](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION)
needs to be supported.

For example [this audio recorder](https://f-droid.org/app/com.github.axet.audiorecorder) is compatible.

Voice notes will automatically be attached.

<br />

<a name="faq145"></a>
**(145) How can I set a notification sound for an account, folder or sender?**

Account:

* Enable *Separate notifications* in the advanced account settings (Settings, tap Manual setup, tap Accounts, tap account, tap Advanced)
* Long press the account in the account list (Settings, tap Manual setup, tap Accounts) and select *Edit notification channel* to change the notification sound

Folder:

* Long press the folder in the folder list and select *Create notification channel*
* Long press the folder in the folder list and select *Edit notification channel* to change the notification sound

Sender:

* Open a message from the sender and expand it
* Expand the addresses section by tapping on the down arrow
* Tap on the bell icon to create or edit a notification channel and to change the notification sound

The order of precendence is: sender sound, folder sound, account sound and default sound.

Setting a notification sound for an account, folder or sender requires Android 8 Oreo or later and is a pro feature.

<br />

<a name="faq146"></a>
**(146) How can I fix incorrect message times?**

Since the sent date/time is optional and can be manipulated by the sender, FairEmail uses the server received date/time by default.

Sometimes the server received date/time is incorrect,
mostly because messages were incorrectly imported from another server and sometimes due to a bug in the email server.

In these rare cases, it is possible to let FairEmail use either the date/time from the *Date* header (sent time) or from the *Received* header as a workaround.
This can be changed in the advanced account settings: Settings, tap Manual setup, tap Accounts, tap account, tap Advanced.

This will not change the time of already synchronized messages.
To solve this, long press the folder(s) in the folder list and select *Delete local messages* and *Synchronize now*.

<br />

<a name="faq147"></a>
**(147) What should I know about third party versions?**

You likely came here because you are using a third party build of FairEmail.

There is **only support** on the latest Play store version, the latest GitHub release and
the F-Droid build, but **only if** the version number of the F-Droid build is the same as the version number of the latest GitHub release.

F-Droid builds irregularly, which can be problematic when there is an important update.
Therefore you are advised to switch to the GitHub release.

The F-Droid version is built from the same source code, but signed differently.
This means that all features are available in the F-Droid version too,
except for using the Gmail quick setup wizard because Google approved (and allows) one app signature only.
For all other email providers, OAuth access is only available in Play Store versions and Github releases,
as the email providers only permit the use of OAuth for official builds.

Note that you'll need to uninstall the F-Droid build first before you can install a GitHub release
because Android refuses to install the same app with a different signature for security reasons.

Note that the GitHub version will automatically check for updates.
When desired, this can be turned off in the miscellaneous settings.

Please [see here](https://github.com/M66B/FairEmail/blob/master/README.md#user-content-downloads) for all download options.

If you have a problem with the F-Droid build, please check if there is a newer GitHub version first.

<br />

<a name="faq148"></a>
**(148) How can I use an Apple iCloud account?**

There is a built-in profile for Apple iCloud, so you should be able to use the quick setup wizard (other provider).
If needed you can find the right settings [here](https://support.apple.com/en-us/HT202304) to manually set up an account.

When using two-factor authentication you might need to use an [app-specific password](https://support.apple.com/en-us/HT204397).

<br />

<a name="faq149"></a>
**(149) How does the unread message count widget work?**

The unread message count widget shows the number of unread messages either for all accounts or for a selected account,
but only for the folders for which new message notifications are enabled.

Tapping on the notification will synchronize all folders for which synchronization is enabled and will open:

* the start screen when all accounts were selected
* a folder list when a specific account was selected and when new message notifications are enabled for multiple folders
* a list of messages when a specific account was selected and when new message notifications are enabled for one folder

<br />

<a name="faq150"></a>
**(150) Can you add cancelling calendar invites?**

Cancelling calendar invites (removing calendar events) requires write calendar permission,
which will result in effectively granting permission to read and write *all* calendar events of *all* calendars.

Given the goal of FairEmail, privacy and security, and given that it is easy to remove a calendar event manually,
it is not a good idea to request this permission for just this reason.

Inserting new calendar events can be done without permissions with special [intents](https://developer.android.com/guide/topics/providers/calendar-provider.html#intents).
Unfortunately, there exists no intent to delete existing calendar events.

<br />

<a name="faq151"></a>
**(151) Can you add backup/restore of messages?**

An email client is meant to read and write messages, not to backup and restore messages.
Note that breaking or losing your device, means losing your messages!

Instead, the email provider/server is responsible for backups.

If you want to make a backup yourself, you could use a tool like [imapsync](https://imapsync.lamiral.info/).

Since version 1.1556 it is possible to export all messages of a POP3 folder in mbox format according to [RFC4155](https://www.ietf.org/rfc/rfc4155.txt),
which might be useful to save sent messages if the email server doesn't.

If you want to import an mbox file to an existing email account,
you can use Thunderbird on a desktop computer and the [ImportExportTools](https://addons.thunderbird.net/nl/thunderbird/addon/importexporttools/) add-on.

<br />

<a name="faq152"></a>
**(152) How can I insert a contact group?**

You can insert the email addresses of all contacts in a contact group via the three dots menu of the message composer.

You can define contact groups with the Android contacts app, please see [here](https://support.google.com/contacts/answer/30970) for instructions.

<br />

<a name="faq153"></a>
**(153) Why does permanently deleting Gmail message not work?**

You might need to change [the Gmail IMAP settings](https://mail.google.com/mail/u/0/#settings/fwdandpop) on a desktop browser to make it work:

* When I mark a message in IMAP as deleted: Auto-Expunge off - Wait for the client to update the server.
* When a message is marked as deleted and expunged from the last visible IMAP folder: Immediately delete the message forever

Note that archived messages can be deleted only by moving them to the trash folder first.

Some background: Gmail seems to have an additional message view for IMAP, which can be different from the main message view.

Another oddity is that a star (favorite message) set via the web interface cannot be removed with the IMAP command

```
STORE <message number> -FLAGS (\Flagged)
```

On the other hand, a star set via IMAP is being shown in the web interface and can be removed via IMAP.

<br />

<a name="faq154"></a>
**~~(154) Can you add favicons as contact photos?~~**

~~Besides that a [favicon](https://en.wikipedia.org/wiki/Favicon) might be shared by many email addresses with the same domain name~~
~~and therefore is not directly related to an email address, favicons can be used to track you.~~

<br />

<a name="faq155"></a>
**(155) What is a winmail.dat file?**

A *winmail.dat* file is sent by an incorrectly configured Outlook client.
It is a Microsoft specific file format ([TNEF](https://en.wikipedia.org/wiki/Transport_Neutral_Encapsulation_Format)) containing a message and possibly attachments.

You can find some more information about this file [here](https://support.mozilla.org/en-US/kb/what-winmaildat-attachment).

You can view it with for example the Android app [Letter Opener](https://play.google.com/store/apps/details?id=app.letteropener).

<br />

<a name="faq156"></a>
**(156) How can I set up an Office 365 account?**

An Office 365 account can be set up via the quick setup wizard and selecting *Office 365 (OAuth)*.

If the wizard ends with *AUTHENTICATE failed*, IMAP and/or SMTP might be disabled for the account.
In this case you should ask the administrator to enable IMAP and SMTP.
The procedure is documented [here](https://docs.microsoft.com/en-in/exchange/troubleshoot/configure-mailboxes/pop3-imap-owa-activesync-office-365).

If you've enabled *security defaults* in your organization, you might need to enable the SMTP AUTH protocol.
Please [see here](https://docs.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/authenticated-client-smtp-submission) about how to.

<br />

<a name="faq157"></a>
**(157) How can I set up an Free.fr account?**

Veuillez [voir ici](https://free.fr/assistance/597.html) pour les instructions.

**SMTP est désactivé par défaut**, veuillez [voir ici](https://free.fr/assistance/2406.html) comment il peut être activé.

Veuillez [voir ici](http://jc.etiemble.free.fr/abc/index.php/trucs-astuces/configurer-smtp-free-fr) pour un guide détaillé.

<br />

<a name="faq103"></a>
<a name="faq158"></a>
**(158) Which camera / audio recorder do you recommend?**

To take photos and to record audio a camera and an audio recorder app are needed.
The following apps are open source cameras and audio recorders:

* [Open Camera](https://play.google.com/store/apps/details?id=net.sourceforge.opencamera) ([F-Droid](https://f-droid.org/en/packages/net.sourceforge.opencamera/))
* [Audio Recorder version 3.3.24+](https://play.google.com/store/apps/details?id=com.github.axet.audiorecorder) ([F-Droid](https://f-droid.org/packages/com.github.axet.audiorecorder/))

To record voice notes, etc, the audio recorder needs to support
[MediaStore.Audio.Media.RECORD_SOUND_ACTION](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media#RECORD_SOUND_ACTION).
Oddly, most audio recorders seem not to support this standard Android action.

<br />

<a name="faq159"></a>
**(159) What are Disconnect's tracker protection lists?**

Please see [here](https://disconnect.me/trackerprotection) for more information about Disconnect's tracker protection lists.

After downloading the lists in the privacy settings, the lists can optionally be used:

* to warn about tracking links on opening links
* to recognize tracking images in messages

Tracking images will be disabled only if the corresponding main 'disable' option is enabled.

Tracking images will not be recognized when the domain is classified as '*Content*',
see [here](https://disconnect.me/trackerprotection#trackers-we-dont-block) for more information.

This command can be sent to FairEmail from an automation app to update the protection lists:

```
(adb shell) am start-foreground-service -a eu.faircode.email.DISCONNECT.ME
```

Updating once a week will probably be sufficient,
please see [here](https://github.com/disconnectme/disconnect-tracking-protection/commits/master) for recent lists changes.

<br />

<a name="faq160"></a>
**(160) Can you add permanent deletion of messages without confirmation?**

Permanent deletion means that messages will *irreversibly* be lost, and to prevent this from happening accidentally, this always needs to be confirmed.
Even with a confirmation, some very angry people who lost some of their messages through their own fault contacted me, which was a rather unpleasant experience :-(

Advanced: the IMAP delete flag in combination with the EXPUNGE command is not supportable
because both email servers and not all people can handle this, risking unexpected loss of messages.
A complicating factor is that not all email servers support [UID EXPUNGE](https://tools.ietf.org/html/rfc4315).

From version 1.1485 it is possible to temporarily enable debug mode in the miscellaneous settings to disable expunging messages.
Note that messages with a *\Deleted* flag will not be shown in FairEmail.

<br />

<a name="faq161"></a>
**(161) Can you add a setting to change the primary and accent color?***

If I could, I would add a setting to select the primary and accent color right away,
but unfortunately Android themes are fixed, see for example [here](https://stackoverflow.com/a/26511725/1794097), so this is not possible.

<br />

<a name="faq162"></a>
**(162) Is IMAP NOTIFY supported?***

Yes, [IMAP NOTIFY](https://tools.ietf.org/html/rfc5465) has been supported since version 1.1413.

IMAP NOTIFY support means that notifications for added, changed or deleted messages of all *subscribed* folders will be requested
and if a notification is received for a subscribed folder, that the folder will be synchronized.
Synchronization for subscribed folders can therefore be disable, saving folder connections to the email server.

**Important**: push messages (=always sync) for the inbox and subscription management (receive settings) need to be enabled.

**Important**: most email servers do not support this! You can check the log via the navigation menu if an email server supports the NOTIFY capability.

<br />

<a name="faq163"></a>
**(163) What is message classification?**

*This is an experimental feature!*

Message classification will attempt to automatically group emails into classes, based on their contents,
using [Bayesian statistics](https://en.wikipedia.org/wiki/Bayesian_statistics).
In the context of FairEmail, a folder is a class. So, for example, the inbox, the spam folder, a 'marketing' folder, etc, etc.

You can enable message classification in the miscellaneous settings. This will enable 'learning' mode only.
The classifier will 'learn' from new messages in the inbox and spam folder by default.
The folder property *Classify new messages in this folder* will enable or disable 'learning' mode for a folder.
You can clear local messages (long press a folder in the folder list of an account) and synchronize the messages again to classify existing messages.

Each folder has an option *Automatically move classified messages to this folder* ('auto classification' for short).
When this is turned on, new messages in other folders which the classifier thinks belong to that folder will be automatically moved.

The option *Use local spam filter* in the report spam dialog will turn on message classification in the miscellaneous settings
and auto classification for the spam folder.
Please understand that this is not a replacement for the spam filter of the email server and
can result in [false positives and false negatives](https://en.wikipedia.org/wiki/False_positives_and_false_negatives).
See also [this FAQ](#user-content-faq92).

A practical example: suppose there is a folder 'marketing' and auto message classification is enabled for this folder.
Each time you move a message into this folder you'll train FairEmail that similar messages belong in this folder.
Each time you move a message out of this folder you'll train FairEmail that similar messages do not belong in this folder.
After moving some messages into the 'marketing' folder, FairEmail will start moving similar messages automatically into this folder.
Or, the other way around, after moving some messages out of the 'marketing' folder, FairEmail will stop moving similar messages automatically into this folder.
This will work best with messages with similar content (email addresses, subject and message text).

Classification should be considered as a best guess - it might be a wrong guess, or the classifier might not be confident enough to make any guess.
If the classifier is unsure, it will simply leave an email where it is.

To prevent the email server from moving a message into the spam folder again and again,
auto classification out of the spam folder will not be done.

The message classifier calculates the probability a message belongs in a folder (class).
There are two options in the miscellaneous settings which control if a message will be automatically moved into a folder,
provided that auto classification is enabled for the folder:

* *Minimum class probability*: a message will only be moved when the confidence it belongs in a folder is greater than this value (default 15 %)
* *Minimum class difference*: a message will only be moved when the difference in confidence between one class and the next most likely class is greater than this value (default 50 %)

Both conditions must be satisfied before a message will be moved.

Considering the default option values:

* Apples 40 % and bananas 30 % would be disregarded because the difference of 25 % is below the minimum of 50 %
* Apples 10 % and bananas 5 % would be disregarded because the probability for apples is below the minimum of 15 %
* Apples 50 % and bananas 20 % would result in selecting apples

Classification is optimized to use as little resources as possible, but will inevitably use some extra battery power.

You can delete all classification data by turning classification in the miscellaneous settings three times off.

[Filter rules](#user-content-faq71) will be executed before classification.

Message classification is a pro feature, except for the spam folder.

<br />

<a name="faq164"></a>
**(164) Can you add customizable themes?**

Unfortunately, Android [does not support](https://stackoverflow.com/a/26511725/1794097) dynamic themes,
which means all themes need [to be predefined](https://github.com/M66B/FairEmail/blob/master/app/src/main/res/values/styles.xml).

Since for each theme there needs to be a light, dark and black variant,
it is not feasible to add for each color combination (literally millions) a predefined theme.

Moreover, a theme is more than just a few colors.
For example themes with a yellow accent color use a darker link color for enough contrast.

The theme colors are based on the color circle of [Johannes Itten](https://en.wikipedia.org/wiki/Johannes_Itten).

<br />

<a name="faq165"></a>
**(165) Is Android Auto supported?**

Yes, Android Auto is supported, but only with the GitHub version,
please [see here](https://forum.xda-developers.com/t/app-5-0-fairemail-fully-featured-open-source-privacy-oriented-email-app.3824168/post-83801249) about why.

For notification (messaging) support you'll need to enable the following notification options:

* *Use Android 'messaging style' notification format*
* Notification actions: *Direct reply* and (mark as) *Read*

You can enable other notification actions too, if you like, but they are not supported by Android Auto.

The developers guide is [here](https://developer.android.com/training/cars/messaging).

<br />

<a name="faq166"></a>
**(166) Can I snooze a message across multiple devices?**

First of all, there is no standard for snoozing messages, so all snooze implementations are custom solutions.

Some email providers, like Gmail, move snoozed messages to a special folder.
Unfortunately, third party apps have no access to this special folder.

Moving a message to another folder and back might fail and might not be possible if there is no internet connection.
This is problematic because a message can be snoozed only after moving the message.

To prevent these issues, snoozing is done locally on the device by hiding the message while it is snoozing.
Unfortunately, it is not possible to hide messages on the email server too.

<br />

## Get support

FairEmail is supported on smartphones, tablets and ChromeOS only.

Only the latest Play store version and latest GitHub release are supported.
The F-Droid build is supported only if the version number is the same as the version number of the latest GitHub release.
This also means that downgrading is not supported.

There is no support on things that are not directly related to FairEmail.

There is no support on building and developing things by yourself.

Requested features should:

* be useful to most people
* not complicate the usage of FairEmail
* fit within the philosophy of FairEmail (privacy oriented, security minded)
* comply with common standards (IMAP, SMTP, etc)

Features not fulfilling these requirements will likely be rejected.
This is also to keep maintenance and support in the long term feasible.

If you have a question, want to request a feature or report a bug, **please use [this form](https://contact.faircode.eu/?product=fairemailsupport)**.

GitHub issues are disabled due to frequent misusage.

<br />

Copyright &copy; 2018-2021 Marcel Bokhorst.
