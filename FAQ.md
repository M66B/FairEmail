<a name="top"></a>

# FairEmail support

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F)

&#x1F1EC;&#x1F1E7; If you have a question, please check the following frequently asked questions first.
[At the bottom](#get-support),
you can find out how to ask other questions, request features, and report bugs.
You will receive an answer in your own language.

&#x1F1E9;&#x1F1EA; Wenn Sie eine Frage haben, überprüfen Sie bitte zuerst die folgenden häufig gestellten Fragen.
[Unten](#get-support) erfahren Sie, wie Sie andere Fragen stellen, Funktionen anfordern und Fehler melden können.
Sie erhalten eine Antwort in Ihrer eigenen Sprache.

&#x1F1EB;&#x1F1F7; Si vous avez une question, veuillez d'abord vérifier les questions fréquemment posées suivantes.
[En bas](#get-support), vous pouvez découvrir comment poser d'autres questions, demander des fonctionnalités et signaler des bogues.
Vous recevrez une réponse dans votre propre langue.

<br>

**Important**

There is a lot of technical information in this FAQ, mostly for specific problems or specific use cases.
For other, more common questions, please see the tutorials below or contact me via *Get support* below.

<br />

## Tutorials

Please [see here](https://github.com/M66B/FairEmail/tree/master/tutorials) for tutorials &#x1F4D6;.

<br />

## Index

* [Authorizing accounts](#authorizing-accounts)
* [How to ...?](#howto)
* [Known problems](#known-problems)
* [Planned features](#planned-features)
* [Frequently Asked Questions](#frequently-asked-questions) (FAQ)
* [Get support](#get-support)

<br />

<h2><a name="authorizing-accounts"></a>Authorizing accounts</h2>

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23authorizing-accounts)

In most cases, the quick setup wizard will be able to automatically identify the correct configuration.

If the quick setup wizard fails, you'll need to manually set up an account (to receive email) and an identity (to send email).
For this you'll need the IMAP and SMTP server addresses and port numbers, whether SSL/TLS or STARTTLS should be used
and your username (mostly, but not always, your email address) and your password.

Searching for *IMAP* and the name of the provider is mostly sufficient to find the right documentation.

In some cases, you'll need to enable external access to your account and/or to use a special (app) password,
for instance when two-factor authentication is enabled.

For authorizing:

* Gmail / G suite, see [question 6](#faq6)
* Outlook / Live / Hotmail, see [question 14](#faq14)
* Office 365, see [question 156](#faq156)
* Microsoft Exchange, see [question 8](#faq8)
* Yahoo, AOL and Sky, see [question 88](#faq88)
* Apple iCloud, see [question 148](#faq148) ([German](https://support.apple.com/de-de/HT204397))
* Free.fr, see [question 157](#faq157)
* Posteo: please check if [additional email account protection](https://posteo.de/en/help/activating-additional-email-account-protection) ([German](https://posteo.de/hilfe/zusaetzlichen-postfachschutz-deaktivieren)) isn't enabled
* Posteo: not that there is [no spam folder](https://posteo.de/en/help/how-does-the-posteo-spam-filter-work) ([German](https://posteo.de/hilfe/wie-funktioniert-der-posteo-spamfilter))
* Web.de: please check if [IMAP is enabled](https://hilfe.web.de/pop-imap/imap/imap-serverdaten.html)
* Web.de: with two factor authentication you'll need to use [an app password](https://web.de/email/sicherheit/zwei-faktor-authentifizierung/)
* Web.de: if you are missing the spam messages folder, you should enable spam filtering via the website of web.de again
* GMX: please check if [IMAP is enabled](https://support.gmx.com/pop-imap/toggle.html) ([German](https://hilfe.gmx.net/pop-imap/einschalten.html)). Reportedly, you need to do this on a desktop computer.
* GMX: with two factor authentication you'll need to use [an app password](https://support.gmx.com/security/2fa/application-specific-passwords.html) ([German](https://hilfe.gmx.net/sicherheit/2fa/anwendungsspezifisches-passwort.html)). Not that enabling two-factor authentication does not automatically enable IMAP.
* T-online.de: please make sure you use [an email password](https://www.telekom.de/hilfe/festnetz-internet-tv/e-mail/e-mail-adresse-passwoerter-und-sicherheit/passwort-fuer-e-mail-programme-einrichten) (German) and not your account password
* Ionos (1und1): please make sure you use [an email password](https://www.ionos.de/hilfe/e-mail/problemloesungen-mail-basicmail-business/passwort-fuer-e-mail-konto-bei-11-ionos-aendern/) (German) and not your account password
* Yandex: please check if [IMAP is enabled](https://yandex.com/support/mail/mail-clients/others.html)
* Comcast/Xfinity: please check if [third party email access](https://www.xfinity.com/support/articles/third-party-email-access) is enabled

Please see [here](#faq22) for common error messages and solutions.

Related questions:

* [Is OAuth supported?](#faq111)
* [Why is ActiveSync &trade; not supported?](#faq133)

<br />

<a name="howto">

## How to ...?

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23howto)

* Change the account name: (Main) Settings, tap Manual setup, tap Accounts, tap account
* Change the swipe left/right target: (Main) Settings, tab page Behavior, Set swipe actions (*)
* Change password: (Main) Settings, tap Manual setup, tap Accounts, tap account, change password
* Set a signature: (Main) Settings, tap Manual setup, tap Identities, tap identity, Edit signature.
* Set a default CC or BCC address: (Main) Settings, tap Manual setup, tap Identities, tap identity, tap Advanced.
* Add CC and BCC addresses: tap the people's icon at the end of the subject
* Go to the next/previous message on archive/delete: in the behavior settings disable *Automatically close conversations* and select *Go to next/previous conversation* for *On closing a conversation*
* Create a folder: tap the account name in the navigation menu (left side menu) and tap the button at the bottom right
* Add a folder to the unified inbox: long press the folder in the folder list and tick *Show in unified inbox*
* Add a folder to the navigation menu: long press the folder in the folder list and tick *Show in navigation menu*
* Load more messages: long press a folder in the folder list, select *Fetch more messages*
* Delete a message, skipping trash: long press the trash icon
* Delete an account/identity: (Main) Settings, tap Manual setup, tap Accounts/Identities, tap the account/identity, tap the trash icon in the top right corner
* Delete a folder: long press the folder in the folder list, Edit properties, tap the trash icon in the top right corner
* Undo send: Outbox, swipe the message in the list left or right
* Delete a contact: please [see this FAQ](#faq171)
* Store sent messages in the inbox: please [see this FAQ](#faq142)
* Change system folders: (Main) Settings, tap Manual setup, tap Accounts, tap account, at the bottom
* Export/import settings: via the backup settings tab page (last tab page)

(*) Swipe actions for individual and POP3 accounts can be configured in the account setting: (Main) Settings, tap Manual setup, tap Accounts, tap account

<br />

<h2><a name="known-problems"></a>Known problems</h2>

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23known-problems)

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
* A preview of a message text doesn't (always) appear on Samsung watches because [setLocalOnly](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder.html#setLocalOnly(boolean)) seem to be ignored. Message preview texts are known to be displayed correctly on Pebble 2, Fitbit Charge 3, Mi band 3, and Xiaomi Amazfit BIP smartwatches. See also [this FAQ](#faq126).
* A [bug in Android 6.0](https://issuetracker.google.com/issues/37068143) causes a crash with *... Invalid offset: ... Valid range is ...* when text is selected and tapping outside of the selected text. This bug has been fixed in Android 6.0.1.
* Internal (anchor) links will not work because original messages are shown in an embedded WebView in a scrolling view (the conversation list). This is an Android limitation which cannot be fixed or worked around.
* Language detection [is not working anymore](https://issuetracker.google.com/issues/173337263) on Pixel devices with (upgraded to?) Android 11
* A [bug in OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2688) causes invalid PGP signatures when using a hardware token.
* Search suggestions causes the keyboard losing focus on Android 12L.
* ~~[A bug](https://techcommunity.microsoft.com/t5/outlook/outlook-office-365-imap-idle-is-broken/m-p/3616242) in the Outlook IMAP server causes delayed new message notifications.~~
* Updating the Material You colors sometimes require restarting the app / the device, which is caused by [a bug](https://issuetracker.google.com/issues/386671298) in the Android WebView.

<a name="redmi"></a>
<a name="realme"></a>
<a name="oneplus"></a>
<a name="oppo"></a>

<br />

**Xiaomi Redmi / Realme / OnePlus / Oppo**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23redmi)

On some Xiaomi Redmi (Note) devices, some Realme devices, some OnePlus devices, some Oppo devices, and some Samsung devices running Android 12,
the database occasionally gets corrupted, especially after installing an update,
resulting in total data loss (on the device only, unless you are using a POP3 account with the option *Leave messages on server* disabled).

The cause of this problem are disk I/O errors due to an Android bug (more likely) or maybe a hardware issue (less likely),
please [see here](https://www.sqlite.org/rescode.html#ioerr_write).

"*This error might result from a hardware malfunction or because a filesystem came unmounted while the file was open.*"

This can't be fixed by the app and must be fixed by the device manufacturer with an Android update.

**Please do not blame the app for this!**

For the record, the stack trace:

```
android.database.sqlite.SQLiteDiskIOException: disk I/O error (code 778)
	at io.requery.android.database.sqlite.SQLiteConnection.nativeExecute(SourceFile:-2)
	at io.requery.android.database.sqlite.SQLiteConnection.execute(SQLiteConnection:595)
	at io.requery.android.database.sqlite.SQLiteSession.endTransactionUnchecked(SQLiteSession:447)
	at io.requery.android.database.sqlite.SQLiteSession.endTransaction(SQLiteSession:411)
	at io.requery.android.database.sqlite.SQLiteDatabase.endTransaction(SQLiteDatabase:551)
	at androidx.room.RoomDatabase.internalEndTransaction(RoomDatabase:594)
	at androidx.room.RoomDatabase.endTransaction(RoomDatabase:584)
```

This will affect other apps which use a local database intensively too.
Most apps store their data in the cloud instead of on the device, which is why this isn't occurring frequently.

<br />

<h2><a name="planned-features"></a>Planned features</h2>

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23planned-features)

* &#x2714; ~~Synchronize on demand (manual)~~
* &#x2714; ~~Semi-automatic encryption~~
* &#x2714; ~~Copy message~~
* &#x2714; ~~Colored stars~~
* &#x2714; ~~Notification settings per folder~~
* &#x2714; ~~Select local images for signatures~~
* &#x2714; ~~Show messages matched by a rule~~
* &#x274C; ~~[ManageSieve](https://tools.ietf.org/html/rfc5804)~~ (there are no maintained Java libraries with a suitable license and without dependencies and besides that, FairEmail has its own rules)
* &#x2714; ~~Search for messages with/without attachments~~ (on-device only because IMAP doesn't support searching for attachments)
* &#x2714; ~~Search for a folder~~
* &#x2714; ~~Search suggestions~~
* &#x274C; ~~[Autocrypt Setup Message](https://autocrypt.org/autocrypt-spec-1.0.0.pdf) (section 4.4)~~ (IMO it is not a good idea to let an email client handle sensitive encryption keys for an exceptional use case while OpenKeychain can export keys, too)
* &#x2714; ~~Generic unified folders~~
* &#x2714; ~~New per-account message notification schedules~~ (implemented by adding a time condition to rules, so that messages can be snoozed during selected periods)
* &#x2714; ~~Copy accounts and identities~~
* &#x2714; ~~Pinch to zoom~~
* &#x2714; ~~More compact folder view~~
* &#x2714; ~~Compose lists~~
* &#x274C; ~~Compose tables~~ (the Android editor doesn't support tables)
* &#x2714; ~~Pinch to zoom text size~~
* &#x2714; ~~Display GIFs~~
* &#x2714; ~~Themes~~
* &#x274C; ~~Any day time condition~~ (any day doesn't really fit into the from/to date/time condition)
* &#x2714; ~~Send as attachment~~
* &#x2714; ~~Widget for selected account~~
* &#x2714; ~~Remind to attach files~~
* &#x2714; ~~Select domains to show images for~~
* &#x2714; ~~Unified starred messages view~~ (implemented as saved search)
* &#x2714; ~~Move notification action~~
* &#x2714; ~~S/MIME support~~
* &#x2714; ~~Search for settings~~
* &#x274C; ~~POP3 folders~~
* &#x2714; ~~Bottom action bar~~

Anything on this list is in random order and *might* be added in the near future.

<br />

<h2><a name="frequently-asked-questions"></a>Frequently Asked Questions</h2>

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23frequently-asked-questions)

* [(1) Which permissions are needed and why?](#faq1)
* [(2) Why is there a permanent notification shown?](#faq2)
* [(3) What are operations and why are they pending?](#faq3)
* [(4) How can I use an invalid security certificate / empty password / plain text connection?](#faq4)
* [(5) How can I customize the message view?](#faq5)
* [(6) How can I login to Gmail / G suite?](#faq6)
* [(7) Why are sent messages not appearing (immediately) in the sent folder?](#faq7)
* [(8) Can I use a Microsoft Exchange account?](#faq8)
* [(9) What are identities / how do I add an alias / configure a default CC or BCC address?](#faq9)
* [~~(11) Why is POP not supported?~~](#faq11)
* [~~(10) What does 'UIDPLUS not supported' mean?~~](#faq10)
* [(12) How does encryption/decryption work?](#faq12)
* [(13) How does search on device/server work?](#faq13)
* [(14) How can I set up an Outlook / Live / Hotmail account?](#faq14)
* [(15) Why does the message text keep loading?](#faq15)
* [(16) Why are messages not being synchronized?](#faq16)
* [~~(17) Why does manual synchronize not work?~~](#faq17)
* [(18) Why is the message preview not always shown?](#faq18)
* [(19) Why are the pro features so expensive?](#faq19)
* [(20) Can I get a refund?](#faq20)
* [(21) How do I enable the notification light?](#faq21)
* [(22) What does account/folder error ... mean?](#faq22)
* [(23) Why do I get alert ...?](#faq23)
* [(24) What is browse messages on the server?](#faq24)
* [(25) Why can't I select/open/save an image, attachment or a file?](#faq25)
* [(26) Can I help to translate FairEmail in my own language?](#faq26)
* [(27) How can I distinguish between embedded and external images?](#faq27)
* [(28) How can I manage status bar notifications?](#faq28)
* [(29) How can I get new message notifications for other folders?](#faq29)
* [(30) How can I use the provided quick settings?](#faq30)
* [(31) How can I use the provided shortcuts?](#faq31)
* [(32) How can I check if reading email is really safe?](#faq32)
* [(33) Why are edited sender addresses not working?](#faq33)
* [(34) How are identities matched?](#faq34)
* [(35) Why should I be careful with viewing images, attachments, the original message, and opening links?](#faq35)
* [(36) How are settings files encrypted?](#faq36)
* [(37) How are passwords stored?](#faq37)
* [(39) How can I reduce the battery usage of FairEmail?](#faq39)
* [(40) How can I reduce the data usage of FairEmail?](#faq40)
* [(41) How can I fix the error 'Handshake failed'?](#faq41)
* [(42) Can you add a new provider to the list of providers?](#faq42)
* [(43) Can you show the original ...?](#faq43)
* [(44) Can you show contact photos / identicons in the sent folder?](#faq44)
* [(45) How can I fix 'This key is not available. To use it, you must import it as one of your own!'?](#faq45)
* [(46) Why does the message list keep refreshing?](#faq46)
* [(47) How do I solve the error 'No primary account or no drafts folder'?](#faq47)
* [~~(48) How do I solve the error 'No primary account or no archive folder'?~~](#faq48)
* [(49) How do I fix 'An outdated app sent a file path instead of a file stream'?](#faq49)
* [(50) Can you add an option to synchronize all messages?](#faq50)
* [(51) How are folders sorted?](#faq51)
* [(52) Why does it take some time to reconnect to an account?](#faq52)
* [(53) Can you stick the message action bar to the top/bottom?](#faq53)
* [~~(54) How do I use a namespace prefix?~~](#faq54)
* [(55) How can I mark all messages as read / move or delete all messages?](#faq55)
* [(56) Can you add support for JMAP?](#faq56)
* [(57) Can I use HTML in signatures?](#faq57)
* [(58) What does an open/closed email icon mean?](#faq58)
* [(59) Can original messages be opened in the browser?](#faq59)
* [(60) Did you know ...?](#faq60)
* [(61) Why are some messages shown dimmed?](#faq61)
* [(62) Which authentication methods are supported?](#faq62)
* [(63) How are images resized for displaying on screens?](#faq63)
* [~~(64) Can you add custom actions for swipe left/right?~~](#faq64)
* [(65) Why are some attachments shown dimmed?](#faq65)
* [(66) Is FairEmail available in the Google Play Family Library?](#faq66)
* [(67) How can I snooze conversations?](#faq67)
* [~~(68) Why can Adobe Acrobat reader not open PDF attachments / Microsoft apps not open attached documents?~~](#faq68)
* [(69) Can you add auto scroll up on new message?](#faq69)
* [(70) When will messages be auto expanded?](#faq70)
* [(71) How do I use rules?](#faq71)
* [(72) What are primary accounts/identities?](#faq72)
* [(73) Is moving messages across accounts safe/efficient?](#faq73)
* [(74) Why do I see duplicate messages?](#faq74)
* [(75) Can you make an iOS, Windows, Linux, etc version?](#faq75)
* [(76) What does 'Clear local messages' do?](#faq76)
* [(77) Why are messages sometimes shown with a small delay?](#faq77)
* [(78) How do I use schedules?](#faq78)
* [(79) How do I use synchronize on demand (manual)?](#faq79)
* [~~(80) How do I fix the error 'Unable to load BODYSTRUCTURE'?~~](#faq80)
* [(81) Can you make the background of the original message view dark in dark themes?](#faq81)
* [(82) What is a tracking image?](#faq82)
* [(84) What are local contacts for?](#faq84)
* [(85) Why is an identity not available?](#faq85)
* [~~(86) What are 'extra privacy features'?~~](#faq86)
* [(87) What does 'invalid credentials' mean?](#faq87)
* [(88) How can I use a Yahoo/AT&T, AOL or Sky account?](#faq88)
* [(89) How can I send plain text only messages?](#faq89)
* [(90) Why are some texts linked while not being a link?](#faq90)
* [~~(91) Can you add periodical synchronization to save battery power?~~](#faq91)
* [(92) Can you add spam filtering, verification of the DKIM signature and SPF authorization?](#faq92)
* [(93) Can you allow installation/data storage on external storage media (sdcard)?](#faq93)
* [(94) What does the red/orange stripe at the end of the header mean?](#faq94)
* [(95) Why are not all apps shown when selecting an attachment or image?](#faq95)
* [(96) Where can I find the IMAP and SMTP settings?](#faq96)
* [(97) What is 'cleanup'?](#faq97)
* [(98) Why can I still pick contacts after revoking contacts permissions?](#faq98)
* [(99) Can you add a rich text or markdown editor?](#faq99)
* [(100) How can I synchronize Gmail categories?](#faq100)
* [(101) What does the blue/orange dot at the bottom of the conversations mean?](#faq101)
* [(102) How can I enable auto rotation of images?](#faq102)
* [(103) How can I record audio?](#faq158)
* [(104) What do I need to know about error reporting?](#faq104)
* [(105) How does the roam-like-at-home option work?](#faq105)
* [(106) Which launchers can show a badge count with the number of unread messages?](#faq106)
* [(107) How do I use colored stars?](#faq107)
* [~~(108) Can you add permanently delete messages from any folder?~~](#faq108)
* [~~(109) Why is 'select account' available in official versions only?~~](#faq109)
* [(110) Why are (some) messages empty and/or attachments corrupted?](#faq110)
* [(111) Is OAuth supported?](#faq111)
* [(112) Which email provider do you recommend?](#faq112)
* [(113) How does biometric authentication work?](#faq113)
* [(114) Can you add an import for the settings of other email apps?](#faq114)
* [~~(115) Can you add email address chips?~~](#faq115)
* [~~(116) How can I show images in messages from trusted senders by default?~~](#faq116)
* [(117) How can I restore a purchase (on another device) ?](#faq117)
* [(118) What does 'Remove tracking parameters' exactly?](#faq118)
* [~~(119) Can you add colors to the unified inbox widget?~~](#faq119)
* [(120) Why are new message notifications not removed on opening the app?](#faq120)
* [(121) How are messages grouped into a conversation?](#faq121)
* [~~(122) Why is the recipient name/email address show with a warning color?~~](#faq122)
* [(123) What will happen when FairEmail cannot connect to an email server?](#faq123)
* [(124) Why do I get 'Message too large or too complex to display'?](#faq124)
* [(125) What are the current experimental features?](#faq125)
* [(126) Can message previews be sent to my smartwatch?](#faq126)
* [(127) How can I fix 'Syntactically invalid HELO argument(s)'?](#faq127)
* [(128) How can I reset asked questions, for example to show images?](#faq128)
* [(129) Are ProtonMail, Tutanota, etc supported?](#faq129)
* [(130) What does message error ... mean?](#faq130)
* [(131) Can you change the direction for swiping to previous/next message?](#faq131)
* [(132) Why are new message notifications silent?](#faq132)
* [(133) Why is ActiveSync &trade; not supported?](#faq133)
* [(134) Can you add leave messages on the server?](#faq134)
* [(135) Why are trashed messages and drafts shown in conversations?](#faq135)
* [(136) How can I delete an account/identity/folder?](#faq136)
* [(137) How can I reset 'Don't ask again'?](#faq137)
* [(138) Can you add calendar/contact/tasks/notes management?](#faq138)
* [(139) How do I fix 'User is authenticated but not connected'?](#faq139)
* [(140) Why does the message text contain strange characters?](#faq140)
* [(141) How can I fix 'A drafts folder is required to send messages'?](#faq141)
* [(142) How can I store sent messages in the inbox?](#faq142)
* [~~(143) Can you add a trash folder for POP3 accounts?~~](#faq143)
* [(144) How can I record voice notes?](#faq144)
* [(145) How can I set a notification sound for an account, folder, sender or condition?](#faq145)
* [(146) How can I fix incorrect message times?](#faq146)
* [(147) What should I know about third party versions?](#faq147)
* [(148) How can I use an Apple iCloud account?](#faq148)
* [(149) How does the unread message count widget work?](#faq149)
* [(150) Can you add cancelling calendar invites?](#faq150)
* [(151) Can you add backup/restore of messages?](#faq151)
* [(152) How can I insert a contact group?](#faq152)
* [(153) Why does permanently deleting Gmail message not work?](#faq153)
* [(154) Is there support for favicons as contact photos?](#faq154)
* [(155) What is a winmail.dat file?](#faq155)
* [(156) How can I set up an Office 365 account?](#faq156)
* [(157) How can I set up an Free.fr account?](#faq157)
* [(158) Which camera / audio recorder do you recommend?](#faq158)
* [(159) What are Disconnect's tracker protection lists?](#faq159)
* [(160) Can you add permanent deletion of messages without confirmation?](#faq160)
* [(161) Can you add a setting to change the primary and accent color?](#faq161)
* [(162) Is IMAP NOTIFY supported?](#faq162)
* [(163) What is message classification?](#faq163)
* [(164) Can you add customizable themes?](#faq164)
* [(165) Is Android Auto supported?](#faq165)
* [(166) Can I snooze a message across multiple devices?](#faq166)
* [(167) How can I use DeepL?](#faq167)
* [(168) What is a spam block list?](#faq168)
* [(169) Why does the app not start automatically?](#faq169)
* [(170) Why can't folders be created with POP3?](#faq170)
* [(171) How can I delete a contact?](#faq171)
* [(172) How can I import contacts?](#faq172)
* [(173) What is the difference between Play store / GitHub / F-Droid version?](#faq173)
* [(174) Is auto discovery supported?](#faq174)
* [(175) Why should battery optimizations be disabled?](#faq175)
* [(176) When will a message be considered safely transported?](#faq176)
* [(177) What does 'Sensitivity' mean?](#faq177)
* [(178) Why are widgets not updating?](#faq178)
* [(179) What are reply templates?](#faq179)
* [(180) How do I use LanguageTool?](#faq180)
* [(181) How do I use VirusTotal?](#faq181)
* [(182) How can I select how a link should be opened?](#faq182)
* [(183) How do I use Send?](#faq183)
* [(184) How do I password protect content?](#faq184)
* [(185) Can I install FairEmail on Windows?](#faq185)
* [(186) How can I let the app auto store iCalendar invitations?](#faq186)
* [(187) Are colored stars synchronized across devices?](#faq187)
* [~~(188) Why is Google backup disabled?~~](#faq188)
* [(189) What is cloud sync?](#faq189)
* [(190) How do I use OpenAI (ChatGPT)?](#faq190)
* [(191) How do I download and keep older messages on my device?](#faq191)
* [(192) How can I resolve 'Couldn't connect to host, port: ...; timeout ...;' ?](#faq192)
* [(193) How can I import Outlook contacts?](#faq193)
* [(194) How can I set up automatic deletion of old messages?](#faq194)
* [(195) Why are all messages in the archive folder of Gmail?](#faq195)
* [(196) Can you add empty trash on leaving the app?](#faq196)
* [(197) How can I print a message?](#faq197)
* [(198) Can you add spell checking?](#faq198)
* [(199) Can you add proxy support?](#faq199)
* [(200) How can I use Adguard to remove tracking parameters?](#faq200)
* [(201) What is certificate transparency?](#faq201)
* [(202) What is DNSSEC and what is DANE?](#faq202)
* [(203) Where is my sent message?](#faq203)
* [(204) How do I use Gemini?](#faq204)
* [(205) How do I check the integrity of an APK file?](#faq205)
* [(206) How can I move or copy messages from one account to another?](#faq206)

[I have another question.](#get-support)

<a name="faq1"></a>
**(1) Which permissions are needed and why?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq1)

The following Android permissions are **required**:

* *have full network access* (INTERNET): to send and receive via the internet
* *view network connections* (ACCESS_NETWORK_STATE): to monitor connectivity changes (mobile data, WiFi)
* *run at startup* (RECEIVE_BOOT_COMPLETED): to start sending and receiving on device start
* *run foreground service* (FOREGROUND_SERVICE/DATA_SYNC/SPECIAL_USE): to run a foreground service on Android 9 Pie and later, see also the next question
* *run foreground service* (FOREGROUND_SERVICE_MEDIA_PLAYBACK): for text-to-speech
* *schedule exact alarm* (SCHEDULE_EXACT_ALARM): to use exact alarm scheduling (Android 12 and later), for example to snooze messages
* *prevent device from sleeping* (WAKE_LOCK): to keep the device awake while performing actions, like synchronization of messages
* *use fingerprint hardware* (USE_FINGERPRINT) and *use biometric hardware* (USE_BIOMETRIC): to use biometric authentication (fingerprint, face unlock, etc.)
* *ask to ignore battery optimizations* (REQUEST_IGNORE_BATTERY_OPTIMIZATIONS): to disable battery optimizations, please see [this FAQ](#faq175) for more information
* *allow the app to show notifications* (POST_NOTIFICATIONS): to show new message notifications and (account) warnings and errors (Android 13 and later only)
* *Google Play (in-app) billing service* (BILLING): for in-app purchases

<br />

The following Android permissions are **optional**:

* *read your contacts* (READ_CONTACTS): to auto-complete addresses, to show contact photos and [to select contacts](https://developer.android.com/guide/components/intents-common#PickContactDat)
* *find accounts on the device* (GET_ACCOUNTS): to select an account when using the Gmail quick setup
* *read the contents of your shared storage (SD card)* (READ_EXTERNAL_STORAGE): to accept files from other, outdated apps, see also [this FAQ](#faq49)
* Android 5.1 Lollipop and before: *use accounts on the device* (USE_CREDENTIALS): to select an account when using the Gmail quick setup (not requested on later Android versions)
* Android 5.1 Lollipop and before: *Read profile* (READ_PROFILE): to read your name when using the Gmail quick setup (not requested on later Android versions)
* GitHub version only: *read and write calendar data* (READ_CALENDAR/WRITE_CALENDAR): to [auto-store invitations](#faq186)

[Optional permissions](https://developer.android.com/training/permissions/requesting) are supported on Android 6 Marshmallow and later only.
On earlier Android versions, you will be asked to grant the permissions on installing FairEmail.

<br />

The following permissions are needed to show the count of unread messages as a badge (see also [this FAQ](#faq106)):

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
* *com.vivo.notification.permission.BADGE_ICON*

<br />

FairEmail will keep a list of addresses you receive messages from and send messages to
and will use this list for contact suggestions when no contacts permission is granted to FairEmail.
This means you can use FairEmail without the Android contacts provider (address book).
Note that you can still select contacts without granting the contacts permission to FairEmail,
only suggesting contacts won't work without contacts permission.

<br />

<a name="faq2"></a>
**(2) Why is there a permanent notification shown?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq2)

*If you came here by clicking on the "monitoring" notification, you should know that the next click will open the unified inbox.*

To reliably receive messages in the background, the app needs to start a service,
which will let Android display a status bar notification notifying you about potential battery usage.

The service can't be started without a notification and the app can't disable the notification either.
However, you can disable the notification yourself, without side effects, via the notification settings of FairEmail:

* Android 8 Oreo and later: tap the *Monitoring channel* button and disable the channel via the Android settings (this won't disable new message notifications)
* Android 7 Nougat and before: enabled *Use background service to synchronize messages*, but be sure to read the remark below the setting first

You can also switch to periodical synchronization of messages in the receive settings, in order to remove the notification, but be aware that this might use more battery power.
See [here](#faq39) for more details about battery usage.

Android 8 Oreo might also show a status bar notification with the text *Apps are running in the background*.
Please see [here](https://www.reddit.com/r/Android/comments/7vw7l4/psa_turn_off_background_apps_notification/) about how you can disable this notification.

*Background*

The service is a [foreground service](https://developer.android.com/reference/android/app/Service.html#startForeground(int,%20android.app.Notification))
and is needed to prevent Android from stopping the service when the device is sleeping
([doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby)).

Most, if not all, other email apps don’t show a notification,
which leads to new messages often not being shown or reported later and messages not being sent at all or sent later.

Some other email apps download all your messages to their servers first and push messages via
[Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/) to your device.
For your privacy this is not a nice solution ...

<br />

<a name="faq3"></a>
**(3) What are operations and why are they pending?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq3)

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
* *detach*: delete attachment
* *sync*: synchronize local and remote messages
* *subscribe*: subscribe to remote folder
* *purge*: delete all messages from remote folder
* *send*: send message
* *exists*: check if message exists
* *rule*: execute rule on body text
* *expunge*: permanently delete messages
* *report*: process delivery or read receipt (experimental)
* *download*: async download of text and attachments (experimental)
* *subject*: update subject

Operations are processed only when there is a connection to the email server or when manually synchronizing.
See also [this FAQ](#faq16).

<br />

<a name="faq4"></a>
**(4) How can I use an invalid security certificate / empty password / plain text connection?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq4)

<!-- **Since version 1.2137:
Due to [Google's Play Store policies](https://support.google.com/faqs/answer/6346016),
it is no longer possible to support insecure connections to email servers with certificate issues
for the version of FairEmail distributed in the Play Store.
Therefore, this issue can only be resolved by your email provider,
or by installing the GitHub version of the app (as an update) and enabling insecure connections in the account/identity settings.** -->

*... Untrusted ... not in certificate ...*<br />
*... Invalid security certificate (Can't verify identity of server) ...*<br />
*... Chain validation failed ... timestamp check failed ... Certificate expired at ...*<br />

This can be caused by using an incorrect host name, so first double-check the host name in the advanced identity/account settings (tap *Manual setup and account options*).
Please see the documentation of the email provider about the right host name.
Sometimes the right host name is in the error message.

You should try to fix this by contacting your provider or by getting a valid security certificate
because invalid security certificates are insecure and allow [man-in-the-middle attacks](https://en.wikipedia.org/wiki/Man-in-the-middle_attack).
If money is an obstacle, you can get free security certificates from [Let’s Encrypt](https://letsencrypt.org).

The quick, but unsafe solution (not advised), is to enable *Insecure connections* in the advanced identity settings
(navigation menu, tap *Settings*, tap *Manual setup*, tap *Identities*, tap the identity, tap *Advanced*).

Alternatively, you can accept the fingerprint of invalid server certificates like this:

1. Make sure you are using a trusted internet connection (no public Wi-Fi networks, etc.)
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

You can pin the certificate, too, see above.

<br />

*Empty password*

Your username is likely easily guessed, so this is pretty insecure, unless the SMTP server is available via a restricted local network or a VPN only.

*Plain text connection*

Your username and password and all messages will be sent and received unencrypted, which is **very insecure**
because a [man-in-the-middle attack](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) is very simple on an unencrypted connection.

If you still want to use an invalid security certificate, an empty password or a plain text connection,
you'll need to enable insecure connections in the account and/or identity settings.
STARTTLS should be selected for plain text connections.
If you enable insecure connections, you should connect via private, trusted networks only and never via public networks, such as are offered in hotels, airports, etc.

<br />

<a name="faq5"></a>
**(5) How can I customize the message view?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq5)

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
* *Automatically show original message for known contacts*: to automatically show original messages for contacts on your device, please read [this FAQ](#faq35)
* *Automatically show images for known contacts*: to automatically show images for contacts on your device, please read [this FAQ](#faq35)

Note that messages can be previewed only when the message text was downloaded.
Larger message texts are not downloaded by default on metered (generally mobile) networks.
You can change this in the connection settings.

Some people ask:

* to show the subject text in bold font, but that is already being used as an indicator for unread messages
* to move the star to the left, but it is much easier to toggle the star on the right side

<br />

<a name="faq6"></a>
**(6) How can I login to Gmail / G suite?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq6)

**FairEmail fully supports OAuth through the quick setup wizard and will continue to work after May 30, please see below what to do if you used your account password to set up an account.**

>
> **Important: using your *account* password [won't be possible anymore from May 30, 2022](https://support.google.com/accounts/answer/6010255).**
>
> "*To help keep your account secure, starting May 30, 2022, ​​Google will no longer support the use of third-party apps or devices which ask you to sign in to your Google Account using only your username and password.*"
>
> &#x2705; If you authorized your Gmail account with the quick setup wizard or manually with an app password, your account will keep being synchronized after May 30, 2022.
>
> &#x274C; If you enabled *Less secure apps* in the Google account settings and authorized your Gmail account manually with your normal account password, your account can't be synchronized from May 30, 2022 anymore. The app will show the error **Invalid credentials (Failure)**.
>
> How to check:
>
> * Go to the *Settings* via the navigation menu (left side menu)
> * Tap on *Manual setup and account options*
> * Tap on *Accounts*
> * Find the account in the list
> * If there is a shield icon before the account name (=OAuth), the account will keep working
>
> How to fix:
>
> * Go to the *Settings* via the navigation menu (left side menu)
> * Tap on the *Wizard* button and select *Gmail (OAuth)*
> * Tick the checkbox to authenticate an existing account (else you'll create a new account!)
> * Fill in the fields and follow the steps
> * Repeat for each account
>
> **Note that other email apps, possibly on other devices, which still use your account password could cause your account to be blocked!**
>

If you use the Play store or GitHub version of FairEmail,
you can use the quick setup wizard to easily setup a Gmail account and identity.
The Gmail quick setup wizard is not available for third party builds, such as the F-Droid build,
because Google approved the use of OAuth for official builds only.

When using OAuth with multiple Google accounts, other Google accounts probably need to be logged out first.

The "*Gmail (Android)*" quick setup wizard won't work if the Android account manager doesn't work or doesn't support Google accounts,
which is typically the case if the account selection is being *canceled* right away.

If you don't want to use or can't use OAuth or an on-device Google account, for example on recent Huawei devices,
you can ~~either enable access for "less secure apps" and use your account password (not advised) or~~
enable two factor authentication and use an app specific password.
To use a password, you can use the quick setup wizard and select *Other provider*.

**Important**: sometimes Google issues this alert:

*[ALERT] Please log in via your web browser: https://support.google.com/mail/accounts/answer/78754 (Failure)*

This Google security check is triggered more often with *less secure apps* enabled, even less with an app password, and hardly ever when using an on-device account (OAuth).
You might see the error *OAUTH2 asked for more*, which basically says the connection is temporarily blocked, until you confirm it is you.

Note that an app specific password is required when two factor authentication is enabled.
After enabling two factor authentication there will be this error message:

*[ALERT] Application-specific password required: https://support.google.com/mail/accounts/answer/185833 (Failure)*

The error message "*Authentication failed - Invalid credentials*" or *Token refresh required* means that the Android account manager was not able to refresh the access token,
or that getting an access token was not allowed,
for example when the account is a [Family Link](https://support.google.com/families/answer/7101025) account, in which case you can use the Gmail app only.
A common cause for this problem is using a VPN, a firewall app or an ad blocker which blocks internet access for the Android account manager.
Please make sure permissions were granted to the app via setup step 2.
You can try to work around this issue by using the quick setup wizard *Gmail (Oauth)* or by using an app password.

*[ALERT] IMAP access is not allowed for this account.*

This means that it is a [Google Family Link](https://families.google.com/familylink/) account for a child under the age of 13.
Please see [here](https://www.reddit.com/r/ios/comments/x0dklz/i_give_up_adding_google_email_account_to_my/) for a solution.

<br />

<a name="faq6-app"></a>

*App specific password*

See [here](https://support.google.com/accounts/answer/185833) about how to generate an app specific password.

To configure a new Gmail account with an app password, please tap on the wizard button in the main settings screen
and select *Other provider* (not Gmail!) and follow the steps (paste the app password in the password field).

To configure an existing Gmail account with an app password, please tap on *Manual setup and account options* in the main settings screen,
tap on *Accounts*, tap on the account and tap on the pencil icon after the password and select *Switch to password authentication* and paste the app password.

<br />

~~*Enable "Less secure apps"*~~

~~**Important**: using this method is not recommended because it is less reliable.~~

~~**Important**: Gsuite accounts authorized with a username/password will stop working [in the near future](https://gsuiteupdates.googleblog.com/2019/12/less-secure-apps-oauth-google-username-password-incorrect.html).~~

~~See [here](https://support.google.com/accounts/answer/6010255) about how to enable "less secure apps"~~
~~or go [directy to the setting](https://www.google.com/settings/security/lesssecureapps).~~

~~If you use multiple Gmail accounts, make sure you change the "less secure apps" setting of the right account(s).~~

~~Be aware that you need to leave the "less secure apps" settings screen by using the back arrow to apply the setting.~~

~~If you use this method, you should use a [strong password](https://en.wikipedia.org/wiki/Password_strength) for your Gmail account, which is a good idea anyway.~~
~~Note that using the [standard](https://tools.ietf.org/html/rfc3501) IMAP protocol in itself is not less secure.~~

~~When "less secure apps" is not enabled,~~
~~you'll get the error *Authentication failed - invalid credentials* for accounts (IMAP)~~
~~and *Username and Password not accepted* for identities (SMTP).~~

<br />

*General*

You might get the alert "*Please log in via your web browser*".
This happens when Google considers the network that connects you to the internet (this could be a VPN) to be unsafe.
This can be prevented by using the Gmail quick setup wizard or an app specific password.

See [here](https://support.google.com/mail/answer/7126229) for Google's instructions
and [here](https://support.google.com/mail/accounts/answer/78754) for troubleshooting.

<br />

<a name="faq7"></a>
**(7) Why are sent messages not appearing (immediately) in the sent folder?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq7)

Sent messages are normally moved from the outbox to the sent folder as soon as your provider adds sent messages to the sent folder.
This requires a sent folder to be selected in the account settings and the sent folder to be set to synchronizing.

Some providers do not keep track of sent messages, or the used SMTP server might not be related to the provider.
In these cases, FairEmail, will automatically add sent messages to the sent folder on synchronizing the sent folder, which will happen after a message has been sent.
Note that this will result in extra internet traffic.

~~If this doesn't happen, your provider might not keep track of sent messages or you might be using an SMTP server not related to the provider.~~
~~In these cases, you can enable the advanced identity setting *Store sent messages* to let FairEmail add sent messages to the sent folder right after sending a message.~~
~~Note that enabling this setting might result in duplicate messages if your provider adds sent messages to the sent folder, too.~~
~~Also beware of enabling this setting since it will result in extra data usage, especially when sending messages with large attachments.~~

~~If sent messages in the outbox are not found in the sent folder on a full synchronize, they will be moved from the outbox to the sent folder too.~~
~~A full synchronize happens when reconnecting to the server or when synchronizing periodically or manually.~~
~~You'll likely want to enable the advanced setting *Store sent messages* instead to move messages to the sent folder sooner.~~

<br />

<a name="faq8"></a>
**(8) Can I use a Microsoft Exchange account?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq8)

Firstly, Exchange *protocol* (EWS) is not the same as Exchange *server* or Exchange *account*.

The Microsoft Exchange Web Services &trade; (EWS) protocol [is being phased out](https://techcommunity.microsoft.com/t5/Exchange-Team-Blog/Upcoming-changes-to-Exchange-Web-Services-EWS-API-for-Office-365/ba-p/608055).
Microsoft stopped updating the EWS libraries [in 2016](https://github.com/OfficeDev/ews-java-api).
So, it makes little sense to add this protocol anymore.
Moreover, Microsoft announced that [EWS will be retired on October 1, 2026](https://techcommunity.microsoft.com/t5/exchange-team-blog/retirement-of-exchange-web-services-in-exchange-online/ba-p/3924440).
Please don't leave a bad review for this. This cannot be added because Microsoft no longer provides the tools for this!

You can use a Microsoft Exchange account if it is accessible via IMAP, which is almost always the case because all Exchange servers support the standard IMAP protocol.
See [here](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793) for more information.

Note that the desciption of FairEmail starts with the remark
that non-standard protocols, like Microsoft Exchange Web Services &trade; and Microsoft ActiveSync &trade; are not supported.

Please see [here](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040)
for the Microsoft documentation about configuring an email client.
There is also a section about common connection errors and solutions.

Some older Exchange server versions have a bug causing empty message and corrupt attachments.
Please see [this FAQ](#faq110) for a workaround.

Please see [this FAQ](#faq133) about ActiveSync &trade; support.

Please see [this FAQ](#faq111) about OAuth support.

<br />

<a name="faq9"></a>
**(9) What are identities / how do I add an alias / configure a default CC or BCC address?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq9)

Identities represent email addresses you are sending *from* via an email (SMTP) server.

Some providers allow you to have multiple aliases addresses.

The easiest way to create an alias address is to use the mini *Create alias* wizard.
For this, please go to the settings via the navigation menu (left side menu),
tap on *Manual setup and account options*, tap on *Identities*, long press the main identity, and select *Create alias*.

For more options, you can copy the main identity by long pressing on it, and change the email address and perhaps the (display) name.
You should not change the username!

**Important**: In the case of an Outlook account, you should first authenticate the account again with the "Office 365" wizard. You don't need to remove the account for this.

**In many cases, an alias address must first be verified via the website of the mail provider**

Alternatively, you can enable *Allow editing sender address* in the advanced settings of an existing identity to edit the username when composing a new message,
if your provider allows this. Considering the email address test@example.org you can use these special username formats:

* "*+extra*" will result in the email address "*test+extra@example.org*"
* "*@extra*" will result in the email address "*test@extra.example.org*"
* "*Some name, username*" will result in the email address "*Some name, &lt;username@example.org&gt;*" (since version 1.2032)

You can configure a default CC, BCC and/or reply-to address in the advanced identity settings.

FairEmail will automatically update the passwords of related identities when you update the password of the associated account or a related identity.

See [this FAQ](#faq33) on editing the username of email addresses.

<br />

<a name="faq10"></a>
**~~(10) What does 'UIDPLUS not supported' mean?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq10)

~~The error message *UIDPLUS not supported* means that your email provider does not provide the IMAP [UIDPLUS extension](https://tools.ietf.org/html/rfc4315).
This IMAP extension is required to implement two way synchronization, which is not an optional feature.
So, unless your provider can enable this extension, you cannot use FairEmail for this provider.~~

<br />

<a name="faq11"></a>
**~~(11) Why is POP not supported?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq11)

POP3 is supported!

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq12)

Communication with email servers is always encrypted, unless you explicitly turned this off.
This question is about optional end-to-end encryption with PGP or S/MIME.

The sender and recipient should first agree on this and exchange signed messages to transfer their public key to be able to send encrypted messages.
There is a gesture icon button just above the text of a received message on the right to verify a signature and store the public key.

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
The simplest way to show the send dialog (again) is to long press the *Send* button in the bottom action bar.
It might be necessary to disable signing/encryption with the padlock icon in the top action bar first, to prevent the select key dialog, etc. from being in the way.
The encryption method will be remembered for the selected identity (at the top of the message editor).

To verify a signature or to decrypt a received message, open the message and just tap the gesture or padlock icon just below the message action bar.

The first time you send a signed/encrypted message, you might be asked for a sign key.
FairEmail will automatically store the selected sign key in the used identity for the next time.
If you need to reset the sign key, just save the identity or long press the identity in the list of identities and select *Reset sign key*.
The selected sign key is visible in the list of identities.
If you need to select a key on a case by case basis, you can create multiple identities for the same account with the same email address.

In the encryption settings, you can select the default encryption method (PGP or S/MIME),
enable *Sign by default*, *Encrypt by default* and *Automatically decrypt messages*,
but be aware that automatic decryption is not possible if user interaction is required, like selecting a key or reading a security token.

The to be encrypted message text/attachments and the decrypted message text/attachments are stored locally only and will never be added to the remote server.
If you want to undo decryption, you can tap on the "close" padlock icon.

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
There is an encryption option to send signed messages by default.
Alternatively, this can enabled for a specific identity in the advanced identity settings.
Received Autocrypt headers will be sent to the OpenKeychain app for storage on verifying a signature or decrypting a message.

Although this shouldn't be necessary for most email clients, you can attach your public key to a message
and if you use *.key* as extension, the mime type will correctly be *application/pgp-keys*.

All key handling is delegated to the OpenKey chain app for security reasons. This also means that FairEmail does not store PGP keys.

Inline encrypted PGP in received messages is supported, but inline PGP signatures and inline PGP in outgoing messages is not supported,
see [here](https://josefsson.org/inline-openpgp-considered-harmful.html) about why not.

If you wish to verify a signature manually, check *Show inline attachments* and save the files *content.asc* (the signed content) and *signature.asc* (the digital signature).
Install [GnuPG](https://www.gnupg.org/) on your preferred operating system and execute this command:

```gpg --verify signature.asc.pgp content.asc```

Signed-only or encrypted-only messages are not a good idea, please see here about why not:

* [OpenPGP Considerations Part I](https://www.openkeychain.org/openpgp-considerations-part-i)
* [OpenPGP Considerations Part II](https://www.openkeychain.org/openpgp-considerations-part-ii)
* [OpenPGP Considerations Part III Autocrypt](https://www.openkeychain.org/openpgp-considerations-part-iii-autocrypt)

Signed-only messages are supported, and encrypted-only messages are supported since version 1.2053.

Common errors:

* *No key*: there is no PGP key available for one of the listed email addresses.
* *No key found!*: the PGP key stored in the identity probably doesn't exist anymore. Resetting the key (see above) will probably fix this problem.
* *Missing key for encryption*: there is probably a key selected in FairEmail that does not exist in the OpenKeychain app anymore. Resetting the key (see above) will probably fix this problem.
* *Key for signature verification is missing*: the public key for the sender is not available in the OpenKeychain app. This can also be caused by Autocrypt being disabled in the encryption settings or by the Autocrypt header not being sent.
* *Message signature valid but not confirmed*: the signature is okay, but the public key still needs to be confirmed in the OpenKeychain app.
* *OpenPgp error 0: null* / *OpenPgp error 0: General error*: please check the key in the OpenKeychain app and make sure there are no conflicting identities for the key and make sure the email address exactly matches the key, including lower/upper case. Also, make sure the key can be used to sign/encrypt and isn't for encrypting/signing only.
* *OpenPgp error 0: Encountered an error reading input data!*: your public key has the [AEAD](https://en.wikipedia.org/wiki/Authenticated_encryption) flag set, but the message was encrypted in the older MDC (Modification Detection Code) mode by the sender. For example the Posteo email server does this erroneously. Workaround: [remove the AEAD flag](https://github.com/keybase/keybase-issues/issues/4025#issuecomment-853933127) from the key.

**Important**: if *Don't keep activities* is enabled in the Android developer options,
FairEmail and the OpenKeychain app cannot run at the same time, causing PGP operations to fail.
If needed, please [see here](https://developer.android.com/studio/debug/dev-options#enable) about how to enable the developer options.

**Important**: Android 8.1.0 on Tecno CF8 Camon 11 Pro devices blocks communication between apps
with the message "*AutoStart limited*" in the system logcat.
So, FairEmail (or any other app) can't communicate with the OpenKeychain app.

<br />

*S/MIME*

Encrypting a message requires the public key(s) of the recipient(s). Signing a message requires your private key.

Private keys are stored by Android and can be imported via the Android advanced security settings
(Encryption & credentials, Install a certificate, VPN & app user certificate).
There is a shortcut (button) for this in the encryption settings for Android version 10 and before.
Android will ask you to set a PIN, pattern, or password if you didn't before.
If you have a Nokia device with Android 9, please [read this first](https://nokiamob.net/2019/08/10/a-bug-prevents-nokia-1-owners-from-unlocking-their-screen-even-with-right-pin-pattern/).

**Important**: If Android doesn't accept the password of a certificate, it probably means it doesn't support the certificate's encryption method.
The solution is to convert the certificate like this:

```
openssl pkcs12 -nodes <your.p12 >certbag.pem
openssl pkcs12 -export -legacy -in certbag.pem >legacy.p12
```

Note that .p12 and .pfx are interchangeable.
You can find more information about this issue [here](https://stackoverflow.com/questions/71872900/installing-pcks12-certificate-in-android-wrong-password-bug)

<br>

Note that certificates can contains multiple keys for multiple purposes,  for example for authentication, encryption and signing.
Android only imports the first key, so to import all the keys, the certificate must first be split.
This is not very trivial and you are advised to ask the certificate supplier for support.

If you renewed a certificate, you should import the renewed certificate and reset the key.
This can be done by long pressing an identity in the list of identities (Settings, tap Manual setup, tap Identities).

Note that S/MIME signing with other algorithms than RSA is supported, but be aware that other email clients might not support this.
S/MIME encryption is possible with asymmetric algorithms only, which means in practice using RSA.

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
* *Memory allocation failed*: Android supports keys up to 4096 bits only (Android [issue 199605614](https://issuetracker.google.com/issues/199605614))
* *message-digest attribute value does not match calculated value*: the signature doesn't match the message, possibly because the message was changed, or because an incorrect or key was used

In case the certificate chain is incorrect, you can tap on the little info button to show the all certificates.
After the certificate details the issuer or "selfSign" is shown.
A certificate is self signed when the subject and the issuer are the same.
Certificates from a certificate authority (CA) are marked with "[keyCertSign](https://tools.ietf.org/html/rfc5280#section-4.2.1.3)".
You can find the description of other key usage bits, like *cRLSign*, via this same link.
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

Posteo offers cheap certificates, [see here](https://posteo.de/blog/neu-bei-posteo-smime-zertifikate-erstellen-und-sofort-nutzen) (German).

How to extract a public key from a S/MIME certificate:

```
openssl pkcs12 -in filename.pfx/p12 -clcerts -nokeys -out cert.pem
```

You can verify the signature of a raw message file (EML file) like this:

```
openssl smime -verify <xxx.eml
```

You can decode S/MIME signatures, etc, [here](https://lapo.it/asn1js/).

<br />

*Planck Security* (formerly: *pretty Easy privacy* or p≡p)

There is still no approved standard for p≡p and not many people are using it.

However, FairEmail can send and receive PGP encrypted messages, which are compatible with p≡p.
Also, FairEmail understands incoming p≡p messages since version 1.1519,
so the encrypted subject will be shown and the embedded message text will be shown more nicely.

<br />

S/MIME sign/encrypt is a pro feature, but all other PGP and S/MIME operations are free to use.

<br />

<a name="faq13"></a>
**(13) How does search on device/server work?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq13)

You can start searching for messages on sender (from), recipient (to, cc, bcc), subject, keywords or message text by using the magnify glass in the action bar of a folder.
You can also search from any app by selecting *Search email* in the copy/paste popup menu.

Searching in the unified inbox will search in all folders of all accounts,
searching in the folder list will search in the associated account only
and searching in a folder will search in that folder only.

Messages will be searched for on the device first, unless you use a complex expression, see below.

<br>

You can download more messages on the device via the three-dots overflow menu in the start screen.
There will be an action button with a search again icon at the bottom to continue searching on the server.
You can select in which folder to continue the search.

<br>

The IMAP protocol doesn't support searching in more than one folder at the same time.
Searching on the server is an expensive operation, therefore it is not possible to select multiple folders.

The POP3 protocol doesn't support searching on the server at all.

<br>

Searching local (on-device) messages is case insensitive and on partial text.
The message text of local messages will not be searched if the message text was not downloaded yet.
Searching on the server might be case sensitive or case insensitive and might be on partial text or whole words, depending on the provider.

<br>

Some servers cannot handle searching in the message text when there are a large number of messages.
For this case there is an option to disable searching in the message text.
Since version 1.1888 a popup message will be shown and the search will automatically be retried without searching in the message text.

<br>

It is possible to use Gmail search operators by prefixing a search command with *raw:*.
If you configured just one Gmail account, you can start a raw search directly on the server by searching from the unified inbox.
If you configured multiple Gmail accounts,
you'll first need to navigate to the folder list or the archive (all messages) folder of the Gmail account you want to search in.
Please [see here](https://support.google.com/mail/answer/7190) for the possible search operators. For example:

``
raw:larger:10M
``

<br>

Searching through a large number of messages on the device is not very fast because of two limitations:

* [sqlite](https://www.sqlite.org/), the database engine of Android has a record size limit, preventing message texts from being stored in the database
* Android apps get only limited memory to work with, even if the device has plenty memory available

This means that searching for a message text requires that files containing the message texts need to be opened one by one
to check if the searched text is contained in the file, which is a relatively expensive process.

In the *miscellaneous settings* you can enable *Build search index* to significantly increase the speed of searching on the device,
but be aware that this will increase battery and storage space usage.
The search index is based on <ins>whole</ins> words in all message parts,
so searching for partial text and searching in specific messages parts (from, subject, text, etc) is not possible.

Note that only messages for which the message text was downloaded will be indexed.
In the connection settings can be configured up to which size messages texts will be downloaded automatically
when using a metered connection (generally mobile data or paid WiFi).
In the folder properties (long press on a folder in the folder list of an account) downloading of messages texts can be enabled/disabled.

Using the search index is a pro feature.

<br />

Since version 1.1315 it is possible to use search expressions like this:

```
apple +banana -cherry ?nuts
```

This will result in searching in the subject or text (only) like this:

```
("apple" AND "banana" AND NOT "cherry") OR "nuts"
```

Since version 1.1980 it is possible to use these prefixes as a search expression:

```
from:<email address>
to:<email address>
cc:<email address>
bcc:<email address>
keyword:<keyword>
```

There should be <ins>no space</ins> between the prefix and the search term, which will be applied as an AND-condition.

Only AND conditions (+) and NOT conditions (-) can be used for on-device searching (since version 1.1981).
If you try to use other search expressions, you get the error *Select a folder for a complex search*,
which means that a folder in an account's folder list must be selected in order to perform the search on the server.

<br>

Since version 1.1733 it is possible to save searches, which means that a named entry in the navigation menu will be created to repeat the same search later.
You can save a search after searching by tapping on the save button in the top action bar.
After repeating a search there will be a delete button at the same place to delete a saved search again.
A saved search might be useful to quickly search for starred messages, or for messages from a specific email address, etc.

Using the search index is a pro feature.

<br />

<a name="faq14"></a>
**(14) How can I set up an Outlook / Live / Hotmail account?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq14)

<br />

In the case of the error '*User is authenticated but not connected*', please see [this FAQ](#faq139).

<br>

>
> **IMPORTANT: If you came here via a warning in the app, you MUST take action!**
>
> **IMPORTANT: Microsoft seems to disable (app) passwords ahead of schedule for more and more accounts, resulting in "*Authentication failed*" error messages.**
>

<br />

>
> &#x1F1EC;&#x1F1E7; In short: go to settings via the navigation menu (left side menu), tap the wizard button, select "*Outlook/Office 365 (OAuth)*" and follow the steps.
>
> &#x1F1E9;&#x1F1EA; Kurz gesagt: Gehen Sie über das Navigationsmenü (Menü auf der linken Seite) zu den Einstellungen, tippen Sie auf die Schaltfläche Assistent, wählen Sie "*Outlook/Office 365 (OAuth)*" und folgen Sie den Schritten.
>
> &#x1F1EB;&#x1F1F7; En bref : allez dans les paramètres via le menu de navigation (menu de gauche), appuyez sur le bouton de l'assistant, sélectionnez "*Outlook/Office 365 (OAuth)*" et suivez les étapes.
>

<br />

> It takes just two minutes to be prepared (and to resolve "*Authentication failed*" errors).
>
> If the app said your Outlook accounts will continue to work, you don't need to do anything.
>

<br />

>
> tl;dr; go to settings via the navigation menu (left side menu), tap the wizard button, select "*Outlook/Office 365 (OAuth)*" and follow the steps.
>
> <sub>If you don't use the account anymore, you can delete it via a button in the "extra" section at the bottom of the main settings screen.</sub>
>

<br />

>
> **Important**: Basic Authentication [will be turned off for Office 365 accounts from October 1, 2022](https://techcommunity.microsoft.com/t5/exchange-team-blog/basic-authentication-deprecation-in-exchange-online-may-2022/ba-p/3301866).
>
> **Important**: Basic Authentication **[will be turned off for Outlook/Hotmail/Live accounts from September 16, 2024](https://techcommunity.microsoft.com/t5/outlook-blog/keeping-our-outlook-personal-email-users-safe-reinforcing-our/ba-p/4164184)**.
>

<br />

>
> Microsoft calls password authentication "*Basic authentication*" and authentication with OAuth "*Modern authentication*".
> Confusingly, using [OAuth](https://en.wikipedia.org/wiki/OAuth) also requires entering a password.
>

<br />

>
> &#x2705; If you authorized your Outlook/Office 365 account with the quick setup wizard, your account will keep being synchronized after September 16, 2024.
>
> &#x274C; If you authorized your Outlook/Office 365 account manually with a password, **your account won't be synchronized from September 16, 2024 anymore!**
>

<br />

>
> How to check:
>
> * Go to the *Settings* via the navigation menu (left side menu)
> * Tap on *Manual setup and account options*
> * Tap on *Accounts*
> * Find the account in the list
> * If there is a shield icon before the account name (=OAuth), the account will keep working
>

<br />

>
> How to fix:
>
> * Go to the *Settings* via the navigation menu (left side menu)
> * Tap on the *Wizard* button and select "*Outlook/Office 365 (OAuth)*".
> * Tick the checkbox to authenticate an existing account (else you'll create a new account!)
> * Fill in the fields and follow the steps
> * Repeat for each account
>

<br />

An Outlook / Live / Hotmail account can be set up via the quick setup wizard and selecting "*Outlook/Office 365 (OAuth)*".

**Important**: a personal and a business account can have the same email address, but have different mailboxes (folders). So, please make sure you select the right option.

Microsoft said "*We have turned off SMTP AUTH for millions of tenants not using it*", which can result in this error message:

```
535 5.7.139 Authentication unsuccessful, SmtpClientAuthentication is disabled for the Tenant.
Visit https://aka.ms/smtp_auth_disabled for more information.
```

SMTP AUTH is necessary for third party email clients, which is also documented in [the referenced article](https://aka.ms/smtp_auth_disabled).
So, basically this means that Microsoft is pushing you to their own products. Isn't this a bit of a monopoly?

To use an Outlook, Live or Hotmail account with two factor authentication enabled, you might need to create an app password.
See [here](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification) for the details.

**Important**: Microsoft will drop support for app passwords from September 16, 2024.

See [here](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) for Microsoft's instructions.

Please see [this FAQ](#faq139) for possible causes of the error *... User is authenticated but not connected ...*.

For unknown reasons, some Outlook/Hotmail accounts cannot send messages
because of the server error '*535 5.7.3 Authentication unsuccessful*'.
This can be resolved by authenticating the account with an (app) password (see above) instead of with OAuth.
You should use the "*Other provider*" wizard instead of "*Outlook / Office 365 (OAuth)*" in this case.

For setting up an Office 365 account, please see [this FAQ](#faq156).

<br />

<a name="faq15"></a>
**(15) Why does the message text keep loading?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq15)

The message header and message body are fetched separately from the server.
The message text of larger messages is not being pre-fetched on metered connections and will be fetched on demand on expanding a message.
The message text will keep loading if there is no connection to the account, see also the next question,
or if there other operations, like synchronizing messages, are being executed.

You can check the account and folder list for the account and folder state (see the legend in the navigation menu for the meaning of the icons)
and the operation list accessible via the main navigation menu for pending operations (see [this FAQ](#faq3) for the meaning of the operations).

If FairEmail is holding off because of prior connectivity issues, please see [this FAQ](#faq123), you can force synchronization via the three dots menu.

In the receive settings you can set the maximum size for automatically downloading of messages on metered connections.

Mobile connections are almost always metered and some (paid) Wi-Fi hotspots are too.

<br />

<a name="faq16"></a>
**(16) Why are messages not being synchronized?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq16)

Synchronization problems are seldom the fault of the app, but are almost always caused by internet connectivity or email server problems.
The first thing to check is whether battery optimizations are **disabled** via setup step 3 in the main settings screen.
If you can't solve the problem, [please contact me](#get-support), and I'll explain how to send the debug info for a proper anaylysis.

Possible causes of messages not being synchronized (sent or received) are:

* The account or folder(s) are not set to synchronize
* The number of days to synchronize message for is set too low
* There is no usable internet connection
* The email server is temporarily not available
* Battery optimizations were not disable via setup step 3
* Android stopped the synchronization service
* A memory management app stopped the synchronization service

So, check your account and folder settings and check if the accounts/folders are connected (see the legend in the navigation menu for the meaning of the icons).

If there are any error messages, please see [this FAQ](#faq22).

On some devices, where there are lots of applications competing for memory, Android may stop the synchronization service as a last resort.

Some Android versions, especially those of Samsung, OnePlus, Huawei and Xiaomi, stop apps and services too aggressively.
See [this dedicated website](https://dontkillmyapp.com/) "*Don't kill my app*" for solutions,
and [this Android issue](https://issuetracker.google.com/issues/122098785) (requires logging in with a Google account) for more information.

If you have a **Doogee** device, please [see here](https://android.stackexchange.com/questions/214639/background-apps-get-killed-by-something-other-than-battery-optimization).

On recent **Honor** devices you should disable *Manage all automatically* under *App launch* in the Android settings,
and enable all manual options for FairEmail by tapping on the app entry, to allow the app to run in the backrgound.

Disabling battery optimizations (setup step 3) reduces the chance Android will stop the synchronization service.

In case of successive connection errors, FairEmail will hold off increasingly longer to not drain the battery of your device.
This is described in [this FAQ](#faq123).

<br />

<a name="faq17"></a>
**~~(17) Why does manual synchronize not work?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq17)

~~If the *Synchronize now* menu is dimmed, there is no connection to the account.~~

~~See the previous question for more information.~~

<br />

<a name="faq18"></a>
**(18) Why is the message preview not always shown?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq18)

The preview of the message text cannot be shown if the message body has not been downloaded yet.
See also [this FAQ](#faq15).

<br />

<a name="faq19"></a>
**(19) Why are the pro features so expensive?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq19)

**FairEmail is basically free to use** and only some advanced features need to be purchased.

**FairEmail ist grundsätzlich kostenlos** und nur einige erweiterte Funktionen müssen gekauft werden.

**FairEmail est au fond gratuit** et seulement quelques fonctionnalités avancées doivent être achetés.

Please see the Play store description of the app or [see here](https://email.faircode.eu/#pro) for a complete list of pro features.

The right question is "*why are there so many taxes and fees?*":

* VAT: 25 % (depending on your country)
* Google fee: 15-30 %
* Income tax: 50 %
* <sub>Paypal fee: 5-10 % depending on the country/amount</sub>

So, what is left for the developer is just a fraction of what you pay.

Also note that most free apps will appear not to be sustainable in the end, whereas FairEmail is properly maintained and supported,
and that free apps may have a catch, like sending privacy sensitive information to the internet.
There are no privacy violating ads in the app either.

I have been working on FairEmail almost every day for more than four years, so I think the price is more than reasonable.
For this reason there won't be discounts either.

<br />

<a name="faq20"></a>
**(20) Can I get a refund?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq20)

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq21)

Before Android 8 Oreo: there is an advanced option in the notification settings of the app for this.

Android 8 Oreo and later: please see [here](https://developer.android.com/training/notify-user/channels) about how to configure notification channels.
You can use the button *Default channel* in the notification settings of the app to directly go to the right Android notification channel settings.

Note that apps cannot change notification settings, including the notification light setting, on Android 8 Oreo and later anymore.

Sometimes it is necessary to disable the setting *Show message preview in notifications*
or to enable the settings *Show notifications with a preview text only* to workaround bugs in Android.
This might apply to notification sounds and vibrations too.

Setting a light color before Android 8 is not supported and on Android 8 and later not possible.

Some apps create a notification channel for each selectable color and let you (indirectly) select these channels when selecting a color.
Since FairEmail uses notification channels for configurable notification properties for accounts, folders and senders ([see this FAQ](#faq145)),
this is not a feasible solution for FairEmail because it would result in an unmanageable number of notification channels.

<br />

<a name="faq22"></a>
**(22) What does account/folder error ... mean?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq22)

FairEmail does not hide errors like similar apps often do, so it is easier to diagnose problems.

FairEmail will automatically try to connect again after a delay.
This delay will be doubled after each failed attempt to prevent draining the battery and to prevent from being locked out permanently.
Please see [this FAQ](#faq123) for more information about this.

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

<br />

**In the case of an existing account with an authentication error, please try to use the quick setup wizard button to authenticate the account again.**

<br />

* **Free.fr**: please see [this FAQ](#faq157)
* **Gmail / G suite**: please see [this FAQ](#faq6)
* **iCloud**: please see [this FAQ](#faq148)
* **Posteo**: please check if [additional email account protection](https://posteo.de/en/help/activating-additional-email-account-protection) isn't enabled.
* **Yahoo, AOL and Sky**: please see [this FAQ](#faq88)

If needed, you can update a password in the account settings:
navigation menu (left side menu), tap *Settings*, tap *Manual setup*, tap *Accounts* and tap on the account.
Changing the account password will in most cases automatically change the password of related identities too.
If the account was authorized with OAuth via the quick setup wizard instead of with a password,
you can run the quick setup wizard again and tick *Authorize existing account again* to authenticate the account again.
Note that this requires a recent version of the app.

The error *... Too many bad auth attempts ...* likely means that you are using a Yahoo account password instead of an app password.
Please see [this FAQ](#faq88) about how to set up a Yahoo account.

The message *... +OK ...* likely means that a POP3 port (usually port number 995) is being used for an IMAP account (usually port number 993).

The errors *... invalid greeting ...*, *... requires valid address ...* and *... Parameter to HELO does not conform to RFC syntax ...*
can likely be solved by changing the advanced identity setting *Use local IP address instead of host name*.

The error *... Couldn't connect to host ...* means that there was no response from the email server within a reasonable time (20 seconds by default).
Mostly this indicates internet connectivity issues, possibly caused by a VPN or by a firewall app.
You can try to increase the connection timeout in the connection settings of FairEmail, for when the email server is really slow.
Some devices have a firewall, which you can access like this:

Android *Settings, Data usage, Three-dots overflow menu, Data usage control*

The error *... Connection refused ...* (ECONNREFUSED) means that the email server
or something between the email server and the app, like a firewall, actively refused the connection.

The error *... Network unreachable ...* means that the email server was not reachable via the current internet connection,
for example because internet traffic is restricted to local traffic only.

The error *... Host is unresolved ...*, *... Unable to resolve host ...* or *... No address associated with hostname ... android_getaddrinfo failed: EAI_NODATA*
means that the address of the email server could not be resolved into an IP address.
This might be caused by a VPN, ad blocking or an unreachable or not properly working (local) [DNS](https://en.wikipedia.org/wiki/Domain_Name_System) server.
An incorrect Android private DNS network setting can cause this too.

The error *... Software caused connection abort ...*
means that the email server or something between FairEmail and the email server actively terminated an existing connection.
This can for example happen when connectivity was abruptly lost. A typical example is turning on flight mode.

The errors *... BYE Logging out ...*, *... Connection reset ...* mean that the email server
or something between the email server and the app, for example a router or a firewall (app), actively terminated an existing connection.

The error *... Connection closed by peer ...* means that the email server actively closed the connection.
This might be caused by a not updated Exchange server, see [here](https://blogs.technet.microsoft.com/pki/2010/09/30/sha2-and-windows/) for more information.

The errors *... Read error ...* (sometimes combined with *BAD_DECRYPT* / *DECRYPTION_FAILED_OR_BAD_RECORD_MAC*),
*... Write error ...*, *... Read timed out ...*, *... Broken pipe ...* mean that the email server is not responding anymore or that the internet connection is bad.

<a name="connectiondropped"></a>
The error *... Connection dropped by server? ...* means that the email server unexpectedly terminated the connection.
This sometimes happen when there were too many (simultaneous) connections in a too short time or when a wrong password was used too often.
In this case, please make sure your password is correct and disable receiving in the receive settings for about 30 minutes and try again.
If needed, see [this FAQ](#faq23) about how you can reduce the number of connections.

The error *... Unexpected end of zlib input stream ...* means that not all data was received, possibly due to a bad or interrupted connection.

The error *... connection failure ...* could indicate [Too many simultaneous connections](#faq23).

The warning *... Unsupported encoding ...* means that the character set of the message is unknown or not supported.
FairEmail will assume ISO-8859-1 (Latin1), which will in most cases result in showing the message correctly.

The error *... Login Rate Limit Hit ...* means that there were too many login attempts with an incorrect password. Please double check your password or authenticate the account again with the quick setup wizard (OAuth only).

The error *... NO mailbox selected READ-ONLY ...* indicates [this Zimbra problem](https://sebastian.marsching.com/wiki/Network/Zimbra#Mailbox_Selected_READ-ONLY_Error_in_Thunderbird).

The Outlook specific error *... Command Error. 10 ...* probably means that the OAuth token expired or was invalidated.
Authenticating the account again with the quick setup wizard will probably resolve this condition.
Another possible cause is a bug in an older Exchange version, please [see here](https://bugzilla.mozilla.org/show_bug.cgi?id=886261).
In this case the system administrator needs to update the server software.

The error *... Invalid ID Token Issued at time is more than 10 minutes before or after the current time ...*
means that the clock time of the device deviates too much from the clock time of the email server.
Please make sure the clock time, including the time zone, of the device is correct.

Please [see here](#faq4) for the errors *... Untrusted ... not in certificate ...*, *... Invalid security certificate (Can't verify identity of server) ...* or *... Trust anchor for certification path not found ...*

Please [see here](#faq127) for the error *... Syntactically invalid HELO argument(s) ...*.

Please [see here](#faq41) for the error *... Handshake failed ...*.

See [here](https://linux.die.net/man/3/connect) for what error codes like EHOSTUNREACH and ETIMEDOUT mean.

The error *... connect failed: EACCES (Permission denied) ...* means that  *Restrict data usage* was disabled in the Android MIUI app settings for FairEmail.
On Samsung, and possible other devices, also check: Android settings > Battery > Battery manager / Unmonitored apps.

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
Similarly, the email server might block connections via a VPN because it was misused for sending spam.
Some VPN providers have "cleaner" IP addresses than others, so switching to another VPN provider might be useful.
Note that [Google Fi](https://fi.google.com/) is using a VPN too.

The error '*You must use stronger authentication such as AUTH or APOP to connect to this server*'
can be fixed by enabling debug mode (last option in the miscellaneous settings tab page),
and in the debug panel that appears enabling the APOP option.
After that, debug mode can be disabled again.

**Send errors**

SMTP servers can reject messages for [a variety of reasons](https://en.wikipedia.org/wiki/List_of_SMTP_server_return_codes).
Too large messages and triggering the spam filter of an email server are the most common reasons.

* The error *... Socket is closed ...* might be caused by sending a too large message / attachments
* The attachment size limit for Gmail [is 25 MB](https://support.google.com/mail/answer/6584)
* The attachment size limit for Outlook and Office 365 [is 20 MB](https://support.microsoft.com/en-us/help/2813269/attachment-size-exceeds-the-allowable-limit-error-when-you-add-a-large)
* The attachment size limit for Yahoo [is 25 MB](https://help.yahoo.com/kb/SLN5673.html)
* *554 5.7.1 Service unavailable; Client host xxx.xxx.xxx.xxx blocked*, please [see here](https://docs.gandi.net/en/gandimail/faq/error_types/554_5_7_1_service_unavailable.html)
* *501 Syntax error - line too long* is often caused by using a long Autocrypt header
* *503 5.5.0 Recipient already specified* mostly means that an address is being used both as TO and CC address
* *554 5.7.1 ... not permitted to relay* means that the email server does not recognize the username/email address. Please double check the host name and username/email address in the identity settings.
* *550 Spam message rejected because IP is listed by ...* means that the email server rejected to send a message from the current (public) network address because it was misused to send spam by (hopefully) somebody else before. Please try to enable flight mode for 10 minutes to acquire a new network address.
* *550 We're sorry, but we can't send your email. Either the subject matter, a link, or an attachment potentially contains spam, or phishing or malware.* means that the email provider considers an outgoing message as harmful.
* *550 ...*, [see here](https://www.crazydomains.com.au/help/550-blocked-error-explained/) for a list of possible causes
* *571 5.7.1 Message contains spam or virus or sender is blocked ...* means that the email server considered an outgoing message as spam. This probably means that the spam filters of the email server are too strict. You'll need to contact the email provider for support on this.
* *451 4.7.0 Temporary server error. Please try again later. PRX4 ...* indicates a server configuration problem, please [see here](https://www.limilabs.com/blog/office365-temporary-server-error-please-try-again-later-prx4) or [see here](https://judeperera.wordpress.com/2019/10/11/fixing-451-4-7-0-temporary-server-error-please-try-again-later-prx4/).
* *451 4.7.0 Temporary server error. Please try again later. PRX5 ...* indicates a server configuration problem, please [see here](https://www.limilabs.com/qa/4471/451-4-7-0-temporary-server-error-please-try-again-later-prx5)
* *571 5.7.1 Relay access denied*: please double check the username and email address in the advanced identity settings (via the manual setup).
* *550 5.7.1.You must check for new mail before sending mail ... Local senders are prohibited to send to local recipients without authentication*: did you enter a username/password and did you select either SSL/TLS or STARTTLS in the identity settings? You can try the identity option *Login before sending* (since version 1.2184).
* Please [see here](https://support.google.com/a/answer/3726730) for more information and other SMTP error codes

If you want to use the Gmail SMTP server to workaround a too strict outgoing spam filter or to improve delivery of messages:

* Verify your email address [here](https://mail.google.com/mail/u/0/#settings/accounts) (you'll need to use a desktop browser for this)
* Change the identity settings like this (Settings, tap Manual setup, tap Identities, tap identity):

&emsp;&emsp;Username: *your Gmail address*<br />
&emsp;&emsp;Password: *[an app password](#faq6)*<br />
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

The error *... 334 ... OAUTH2 asked for more ...* probably means that the account needs to be authorized again, which you can do with the quick setup wizard in the settings.

When in doubt, you can ask for [support](#get-support).

<br />

<a name="faq23"></a>
**(23) Why do I get alert ...?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq23)

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq24)

Browse messages on the server will fetch messages from the email server in real time
when you reach the end of the list of synchronized messages, even when the folder is set to not synchronize.
You can disable this feature in the advanced account settings.

<br />

<a name="faq25"></a>
**(25) Why can't I select/open/save an image, attachment or a file?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq25)

When a menu item to select/open/save a file is disabled (dimmed) or when you get the message *Storage access framework not available*,
the [storage access framework](https://developer.android.com/guide/topics/providers/document-provider), a standard Android component, is probably not present.
This might be because your custom ROM does not include it or because it was actively removed (debloated).
Note that this will result in similar problems in other apps too.

FairEmail does not request storage permissions, so this framework is required to select files and folders.
No app, except maybe file managers, targeting Android 4.4 KitKat or later should ask for storage permissions because it would allow access to *all* files.
Moreover, recent Android versions disallow access to all files for apps, except, under specific conditions, for file managers.

To resolve this problem, the system component Google Play Services may need to be updated.
Please [see here](https://support.google.com/googleplay/answer/9037938?hl=en) on how.
Please note that you will need to restart your device after the update!

The storage access framework is provided by the package *com.android.documentsui*,
which is visible as *Files* app on some Android versions (notably OxygenOS).

You can enable the storage access framework (again) with this adb command:

```
pm install -k --user 0 com.android.documentsui
```

Alternatively, you might be able to enable the *Files* app again using the Android app settings.

<br />

In the case of the error *com.android.externalstorage has no access to content://...*,
please enable this Android option for *com.android.externalstorage* and *com.android.sharedstorage*:

*Settings* > *Privacy* > *Permission management* > *Files and media* > *See more apps that can access all files*

<br />

OneDrive doesn't support *view*, only *share*, which means that if you want to open an attachment in OneDrive you need to long press on an attachment.

<br />

<a name="faq26"></a>
**(26) Can I help to translate FairEmail in my own language?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq26)

Yes, you can translate the texts of FairEmail in your own language [on Crowdin](https://crowdin.com/project/open-source-email).
Registration is free.

If you would like a new language to be added, or your name or alias to be included in the list of contributors in *About* the app,
please [contact me](https://contact.faircode.eu/?product=fairemailsupport).

<br />

<a name="faq27"></a>
**(27) How can I distinguish between embedded and external images?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq27)

External image:

<img alt="External image" src="https://raw.githubusercontent.com/M66B/FairEmail/master/images/baseline_image_black_48dp.png" width="48" height="48" />

Embedded image:

<img alt="Embedded image" src="https://raw.githubusercontent.com/M66B/FairEmail/master/images/baseline_photo_library_black_48dp.png" width="48" height="48" />

Broken image:

<img alt="Broken image" src="https://raw.githubusercontent.com/M66B/FairEmail/master/images/baseline_broken_image_black_48dp.png" width="48" height="48" />

Note that downloading external images from a remote server can be used to record you did see a message, which you likely don't want if the message is spam or malicious.

<br />

<a name="faq28"></a>
**(28) How can I manage status bar notifications?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq28)

In the notification settings you'll find a button *Manage notifications* to directly navigate to the Android notifications settings for FairEmail.

Samsung / One UI: you need to enable: Notification > Advanced settings > Manage notification categories for each app

On Android 8.0 Oreo and later you can manage the properties of the individual notification channels,
for example to set a specific notification sound or to show notifications on the lock screen.

FairEmail has the following notification channels:

* Service: used for the notification of the synchronize service, see also [this FAQ](#faq2)
* Send: used for the notification of the send service
* Notifications: used for new message notifications
* Warning: used for warning notifications
* Error: used for error notifications

See [here](https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels) for details on notification channels.
In short: tap on the notification channel name to access the channel settings.

On Android before Android 8 Oreo you can set the notification sound in the settings.

See [this FAQ](#faq21) if your device has a notification light.

<br />

<a name="faq29"></a>
**(29) How can I get new message notifications for other folders?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq29)

Just long press a folder, select *Edit properties*,
and enable either *Show in unified inbox*
or *Notify new messages* (available on Android 7 Nougat and later only)
and tap *Save*.

<br />

<a name="faq30"></a>
**(30) How can I use the provided quick settings?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq30)

There are quick settings (settings tiles) available to:

* globally enable/disable synchronization
* show the number of new messages and marking them as seen (not read)
* clear all app data

Quick settings require Android 7.0 Nougat or later.
The usage of settings tiles is explained [here](https://support.google.com/android/answer/9083864).

Note that not all devices (manufacturers) support settings tiles, for example, Rephone doesn't.

<br />

<a name="faq31"></a>
**(31) How can I use the provided shortcuts?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq31)

There are shortcuts available to compose a new message to a favorite contact.

Shortcuts require Android 7.1 Nougat or later.
The usage of shortcuts is explained [here](https://support.google.com/android/answer/2781850).

It is also possible to create shortcuts to folders
by long pressing a folder in the folder list of an account and selecting *Add shortcut*.

<br />

<a name="faq32"></a>
**(32) How can I check if reading email is really safe?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq32)

You can use the [Email Privacy Tester](https://www.emailprivacytester.com/) for this.

<br />

<a name="faq33"></a>
**(33) Why are edited sender addresses not working?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq33)

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq34)

Matched identities are used to select the correct (matched) identity when replying to a message.

Identities are as expected matched by account.
For incoming messages the *to*, *cc*, *bcc*, *from* and *(X-)delivered/envelope/original-to* addresses will be checked (in this order)
and for outgoing messages (drafts, outbox and sent) only the *from* addresses will be checked.
Equal addresses have precedence over partially matching addresses, except for *delivered-to* addresses.

The matched address will be shown as *via* in the addresses section of received messages (between the message header and message text).

Note that identities need to be enabled to be able to be matched
and that identities of other accounts will not be considered.

Matching will be done only once on receiving a message, so changing the configuration will not change existing messages.
You could clear local messages by long pressing a folder in the folder list and synchronize the messages again, though.

It is possible to configure a [regex](https://en.wikipedia.org/wiki/Regular_expression) in the advanced identity settings
(Navigation menu > Settings > Manual setup and account options > Identities > tap the identity > Advanced)
to match **the username** of an email address (the part before the @ sign).

Note that the domain name (the parts after the @ sign) always needs to be equal to the domain name of the identity.
This also implies that the username will be copied from a received message only if it matches the domain name of the matched identity.
Since version 1.1640, it is possible to match the full email address with a regex by including the @ character in the regex, which can be useful for matching alias domain names.
The username will not be copied in this case because the domain name of the *from* address and the the domain name of the identity must be the same.
If you want the username to be copied, you should define an identity for each domain, which will allow you to send new messages from a specific domain name too.

If you want to match a catch-all email address, this regex is usually fine, provided all usernames for the domain are yours:

```
.*
```

The username of a message being replied to will be used as the default username when editing of usernames is enabled in the advanced identity settings.

Please see [this FAQ](#faq9) about editing the email address when composing a message.

If you want to *not* match specific addresses, you can use something like this:

```
^(?!marcel$|johanna$).*
```

If you like to match the special purpose email addresses abc@example.com and xyx@example.com
and like to have a fallback email address main@example.com as well, you could do something like this:

* Identity: abc@example.com; regex: **(?i)abc**
* Identity: xyz@example.com; regex: **(?i)xyz**
* Identity: main@example.com; regex: **^(?i)((?!abc|xyz).)\*$**

You can test a regex [here](https://regexr.com/).

Matched identities can be used to color code messages.

The identity color takes precedence over the folder and account color.
This means that the color of the color stripe will be determined by first checking if there is a color set for the 'via' (matched) identity,
and after that if there is a color set for the folder containing the message, and finally if there is a color set for the account the message belongs to.

Setting identity colors is a pro feature.

<br />

<a name="faq35"></a>
**(35) Why should I be careful with viewing images, attachments, the original message, and opening links?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq35)

Viewing remotely stored images (see also [this FAQ](#faq27)) and opening links might not only tell the sender that you have seen the message,
but will also leak your IP address.
See also this question: [Why email's link is more dangerous than web search's link?](https://security.stackexchange.com/questions/241139/why-emails-link-is-more-dangerous-than-web-searchs-link).

This BBC article is worth reading as well: [Spy pixels in emails have become endemic](https://www.bbc.com/news/technology-56071437).

Opening attachments or viewing an original message might load remote content and execute scripts,
that might not only cause privacy sensitive information to leak, but can also be a security risk.

Note that your contacts could unknowingly send malicious messages if they got infected with malware.

FairEmail formats messages again, causing messages to look different from the original, but also uncovering phishing links, etc.

Note that reformatted messages are often easier to read than original messages because the margins are removed, and font colors and sizes are standardized.

The Gmail app shows images by default by downloading the images through a Google proxy server.
Since the images are downloaded from the source server [in real-time](https://blog.filippo.io/how-the-new-gmail-image-proxy-works-and-what-this-means-for-you/),
this is even less secure, because Google is involved, too, without providing much benefit.

You can show images and original messages by default for trusted senders on a case-by-case basis by checking *Do not ask this again for ...*.
You might need to reset the questions via a button in the miscellaneous-settings tab page.

<br />

<a name="faq36"></a>
**(36) How are settings files encrypted?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq36)

Short version: AES 256 bit

Long version:

~~*Before version 1.1987*~~

* A 256 bit key is derived with *PBKDF2WithHmacSHA1* using a 128 bit secure random salt and 65536 iterations
* The used cipher is *AES/CBC/PKCS5Padding*

~~*Since version 1.1987*~~

* ~~A 256 bit key is derived with *PBKDF2WithHmacSHA512* using a 128 bit secure random salt and 120000 iterations~~
* ~~The used cipher is *AES/GCM/NoPadding*~~

<br />

<a name="faq37"></a>
**(37) How are passwords stored?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq37)

All supported Android versions [encrypt all user data](https://source.android.com/security/encryption),
so all data, including usernames, passwords, messages, etc, is stored encrypted.

If the device is secured with a PIN, pattern or password, you can make the account and identity passwords visible.
If this is a problem because you are sharing the device with other people,
consider to use [user profiles](https://www.howtogeek.com/333484/how-to-set-up-multiple-user-profiles-on-android/).

<br />

<a name="faq39"></a>
**(39) How can I reduce the battery usage of FairEmail?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq39)

Recent Android versions by default report *app usage* as a percentage in the Android battery settings screen.
**Confusingly, *app usage* is not the same as *battery usage* and is not even directly related to battery usage!**
The app usage (while in use) will be very high because FairEmail is using a foreground service which is considered as constant app usage by Android.
However, this doesn't mean that FairEmail is constantly using battery power.
The real battery usage can be seen by navigating to this screen:

*Android settings*, *Battery*, three-dots menu *Battery usage*, three-dots menu *Show full device usage*

Alternatively: tap on the *App settings* button in the main settings screen of the app and tap on *Battery*.

As a rule of thumb, the battery usage should be below or in any case not be much higher than *Mobile network standby* for one account with a stable network connection.
If this isn't the case, please turn on *Auto optimize* in the receive settings tab page.
If this doesn't help, please [ask for support](https://contact.faircode.eu/?product=fairemailsupport).

It is inevitable that synchronizing messages will use battery power because it requires network access and accessing the messages' database.
Since the app needs to wait for responses of the email server, which requires the processor (CPU) to be active, slower email servers will result in more battery usage.

If you are comparing the battery usage of FairEmail with another email client, please make sure the other email client is set up similarly.
For example, comparing always sync (push messages) and (infrequent) periodic checking for new messages is not a fair comparison.

If you are comparing the battery usage of FairEmail with an app like Whatsapp,
please understand that most apps use [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging) (FCM) for push messages,
which means that no battery usage will be contributed to an app for push messages, but instead to the Google Play Services system component.
Apart from the privacy concerns, it is not possible to use FCM for email because email servers do not support FCM.
The only way would be to download all your messages to a third-party server first and use FCM to push notifications to the app.
The privacy and security implications would be significant, though.

Reconnecting to an email server will use extra battery power, so an unstable internet connection will result in extra battery usage.
Also, some email servers prematurely terminate idle connections, while [the standard](https://tools.ietf.org/html/rfc2177) says that an idle connection should be kept open for 29 minutes.
In these cases you might want to synchronize periodically, for example each hour, instead of continuously.
Note that polling frequently (more than every 30-60 minutes) will likely use more battery power than synchronizing always
because connecting to the server and comparing the local and remote messages are expensive operations.

If you know that the connection (reception) is bad, it might be worthwhile to decrease the timeout value in the connection-settings tab page to 10–20 seconds,
so that the app discovers earlier that no connection is possible,
so that the mechanism as described in [this FAQ](#faq123) is used faster.

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

In the receive settings tab page you can enable to always synchronize starred messages,
which will allow you to keep older messages around while synchronizing messages for a limited number of days.

Disabling the folder option *Automatically download message texts and attachments*
will result in less network traffic and thus less battery usage.
You could disable this option, for example for the sent folder and the archive.

Synchronizing messages at night is mostly not useful, so you can save on battery usage by not synchronizing at night.
In the settings, you can select a schedule for message synchronization (this is a pro feature).

FairEmail will by default synchronize the folder list on each connection.
Since folders are mostly not created, renamed and deleted very often, you can save some network and battery usage by disabling this in the receive settings tab page.

FairEmail will by default check if old messages were deleted from the server on each connection.
If you don't mind that old messages that were deleted from the server are still visible in FairEmail,
you can save some network and battery usage by disabling this in the receive settings tab page.

Some providers don't follow the IMAP standard and [don't keep connections open](https://datatracker.ietf.org/doc/html/rfc3501#section-5.4) long enough, forcing FairEmail to reconnect often, causing extra battery usage.
You can inspect the *Log* via the main navigation menu to check if there are frequent reconnects (connection closed/reset, read/write error/timeout, etc.).
You can work around this by lowering the keep-alive interval in the advanced account settings to for example 9 or 15 minutes.
Note that battery optimizations need to be disabled in setup step 3 to reliably keep connections alive.

Some providers send every two minutes something like '*Still here*' resulting in network traffic and your device to wake up and causing unnecessary extra battery usage.
You can inspect the *Log* via the main navigation menu to check if your provider is doing this.
If your provider is using [Dovecot](https://www.dovecot.org/) as IMAP server,
you could ask your provider to change the [imap_idle_notify_interval](https://wiki.dovecot.org/Timeouts) setting to a higher value or better yet, to disable this.
If your provider is not able or willing to change/disable this, you should consider switching to periodically instead of continuous synchronization.
You can change this in the receive settings tab page.

If you got the message, *This provider does not support push messages* while configuring an account,
consider switching to a modern provider which supports push messages (IMAP IDLE) to reduce battery usage.

If your device has an [AMOLED](https://en.wikipedia.org/wiki/AMOLED) screen,
you can save battery usage while viewing messages by switching to the black theme.

If auto optimize in the receive settings tab page is enabled,
an account will automatically be switched to periodically checking for new messages when the email server:

* Says '*Still here*' within 3 minutes
* The email server does not support push messages
* The keep-alive interval is lower than 12 minutes

<br />

<a name="faq40"></a>
**(40) How can I reduce the data usage of FairEmail?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq40)

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

You could enable to download only plain text only parts, but all messages will be without formatting (styling),
and besides that, a plain text only part is not always sent, and worse, it is sometimes only a part of the message text, containing HTML and CCS.

In case of POP3, you can reduce the maximum number of messages to download.

<br />

<a name="faq41"></a>
**(41) How can I fix the error 'Handshake failed'?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq41)

There are several possible causes, so please read to the end of this answer.

The error '*Handshake failed ... WRONG_VERSION_NUMBER ...*' might mean that you are trying to connect to an IMAP or SMTP server
without an encrypted connection, typically using port 143 (IMAP) and port 25 (SMTP), or that a wrong protocol (SSL/TLS or STARTTLS) is being used.

Most providers provide encrypted connections using different ports, typically port 993 (IMAP) and port 465/587 (SMTP).

If your provider doesn't support encrypted connections, you should ask to make this possible.
If this isn't an option, you could enable *Allow insecure connections* both in the advanced settings AND the account/identity settings.

See also [this FAQ](#faq4).

The error '*Handshake failed ... SSLV3_ALERT_ILLEGAL_PARAMETER ...*' is either caused by a bug in the SSL protocol implementation
or by a too short DH key on the email server and can unfortunately not be fixed by FairEmail.

The error '*Handshake failed ... HANDSHAKE_FAILURE_ON_CLIENT_HELLO ...*' might be caused by the provider still using RC4,
which isn't supported since [Android 7](https://developer.android.com/about/versions/nougat/android-7.0-changes.html#tls-ssl) anymore.

The error '*Handshake failed SSL handshake terminated ... SSLV3_ALERT_HANDSHAKE_FAILURE ... HANDSHAKE_FAILURE_ON_CLIENT_HELLO*'
can be caused by [this Android 7.0 bug](https://issuetracker.google.com/issues/37122132). This can unfortunately not be fixed by FairEmail.

The error '*Handshake failed ... UNSUPPORTED_PROTOCOL or TLSV1_ALERT_PROTOCOL_VERSION or SSLV3_ALERT_HANDSHAKE_FAILURE ...*'
might be caused by enabling **hardening connections** or **Bouncy Castle** in the connection settings tab page,
or by Android not supporting older protocols anymore, like SSLv3 and TLSv1.

The error '*javax.net.ssl.SSLHandshakeException: Read error: ... CERT_LENGTH_MISMATCH*' means that there is something wrong with the email server setup.
Try to switch to port 993 (IMAP) or 465 (SMTP) with SSL/TLS.

Android 8 Oreo and later [do not support](https://developer.android.com/about/versions/oreo/android-8.0-changes#security-all) SSLv3 anymore.
There is no way to workaround lacking RC4 and SSLv3 support because it has completely been removed from Android (which should say something).
Since version 1.2121 the Bouncy Castle secure socket provider ([JSSE](https://en.wikipedia.org/wiki/Java_Secure_Socket_Extension)) is bundled.
Enabling this socket provider in the connection-settings tab and enabling '*Allow insecure connections*' in the account/identity settings *might* solve this problem.

The error '*javax.net.ssl.SSLHandshakeException: Read error: ... TLSV1_ALERT_INTERNAL_ERROR*' means that Android and the email server share no common protocol versions and/or common ciphers.

Please [see here](https://developer.android.com/reference/javax/net/ssl/SSLSocket) for an overview of supported protocols and cipher suites by Android version.

You can use [this website](https://ssl-tools.net/mailservers) or [this website](https://www.immuniweb.com/ssl/) to check for SSL/TLS problems of email servers.

<br />

<a name="faq42"></a>
**(42) Can you add a new provider to the list of providers?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq42)

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
**(43) Can you show the original ...?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq43)

Show original, shows the original message as the sender has sent it, including original fonts, colors, margins, etc.
FairEmail does and will not alter this in any way,
except for requesting [TEXT_AUTOSIZING](https://developer.android.com/reference/android/webkit/WebSettings.LayoutAlgorithm),
which will *attempt* to make small text more readable.

<br />

<a name="faq44"></a>
**~~(44) Can you show contact photos / identicons in the sent folder?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq44)

~~Contact photos and identicons are always shown for the sender because this is necessary for conversation threads.~~
~~Getting contact photos for both the sender and receiver is not really an option because getting contact photo is an expensive operation.~~

<br />

<a name="faq45"></a>
**(45) How can I fix 'This key is not available. To use it, you must import it as one of your own!'?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq45)

You'll get the message *This key is not available. To use it, you must import it as one of your own!*
when trying to decrypt a message with a public key. To fix this you'll need to import the private key.

<br />

<a name="faq46"></a>
**(46) Why does the message list keep refreshing?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq46)

If you see a 'spinner' at the top of the message list, the folder is still being synchronized with the remote server.
You can see the progress of the synchronization in the folder list. See the legend in the navigation menu about what the icons and numbers mean.

The speed of your device and internet connection and the number of days to synchronize messages determine how long synchronization will take.
Note that you shouldn't set the number of days to synchronize messages to more than one day in most cases, see also [this FAQ](#faq39).

<br />

<a name="faq47"></a>
**(47) How do I solve the error 'No primary account or no drafts folder'?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq47)

You'll get the error message *No primary account or no drafts folder* when trying to compose a message
while there is no account set to be the primary account or when there is no drafts folder selected for the primary account.
This can happen for example when you start FairEmail to compose a message from another app.
FairEmail needs to know where to store the draft,
so you'll need to select one account to be the primary account and/or you'll need to select a drafts folder for the primary account.

This can also happen when you try to reply to a message or to forward a message from an account with no drafts folder
while there is no primary account or when the primary account does not have a drafts folder.

Please see [this FAQ](#faq141) for some more information.

<br />

<a name="faq48"></a>
**~~(48) How do I solve the error 'No primary account or no archive folder'?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq48)

~~You'll get the error message *No primary account or no archive folder* when searching for messages from another app.
FairEmail needs to know where to search,
so you'll need to select one account to be the primary account and/or you'll need to select a archive folder for the primary account.~~

<br />

<a name="faq49"></a>
**(49) How do I fix 'An outdated app sent a file path instead of a file stream'?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq49)

You likely selected or sent an attachment or image with an outdated file manager
or an outdated app which assumes all apps still have storage permissions.
For security and privacy reasons modern apps like FairEmail have no full access to all files anymore.
This can result into the error message *An outdated app sent a file path instead of a file stream*
if a file name instead of a file stream is being shared with FairEmail because FairEmail cannot randomly open files.

You can fix this by switching to an up-to-date file manager or an app designed for recent Android versions.
Alternatively, you can grant FairEmail read access to the storage space on your device in the Android app settings.
Note that this workaround [won't work on Android Q](https://developer.android.com/preview/privacy/scoped-storage) anymore.

See also [question 25](#faq25)
and [what Google writes about it](https://developer.android.com/training/secure-file-sharing/share-file#RespondToRequest).

<br />

<a name="faq50"></a>
**(50) Can you add an option to synchronize all messages?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq50)

You can synchronize more or even all messages by long pressing a folder (inbox) in the folder list of an account (tap on the account name in the navigation menu)
and selecting *Synchronize more* in the popup menu.

<br />

<a name="faq51"></a>
**(51) How are folders sorted?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq51)

Folders are first sorted on account order (by default on account name)
and within an account with special, system folders on top, followed by folders set to synchronize.
Within each category the folders are sorted on (display) name.
You can set the display name by long pressing a folder in the folder list and selecting *Edit properties*.

The navigation (hamburger) menu item *Order folders* in the settings, or,
alternatively, a button in the *Extra* section at the bottom of the main settings tab page,
can be used to manually order the folders.

<br />

<a name="faq52"></a>
**(52) Why does it take some time to reconnect to an account?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq52)

There is no reliable way to know if an account connection was terminated gracefully or forcefully.
Trying to reconnect to an account while the account connection was terminated forcefully too often can result in problems
like [too many simultaneous connections](#faq23) or even the account being blocked.
To prevent such problems, FairEmail waits 90 seconds until trying to reconnect again.

You can long press *Settings* in the navigation menu to reconnect immediately.

<br />

<a name="faq53"></a>
**(53) Can you stick the message action bar to the top/bottom?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq53)

The message action bar works on a single message and the bottom action bar works on all the messages in the conversation.
Since there is often more than one message in a conversation, this is not possible.
Moreover, there are quite some message specific actions, like forwarding.

Moving the message action bar to the bottom of the message is visually not appealing because there is already a conversation action bar at the bottom of the screen.

Note that there are not many, if any, email apps that display a conversation as a list of expandable messages.
This has a lot of advantages, but the also causes the need for message specific actions.

<br />

<a name="faq54"></a>
**~~(54) How do I use a namespace prefix?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq54)

~~A namespace prefix is used to automatically remove the prefix providers sometimes add to folder names.~~

~~For example the Gmail spam folder is called:~~

```
[Gmail]/Spam
```

~~By setting the namespace prefix to *[Gmail]* FairEmail will automatically remove *[Gmail]/* from all folder names.~~

<br />

<a name="faq55"></a>
**(55) How can I mark all messages as read / move or delete all messages?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq55)

You can use multiple select for this.
Long press the first message, don't lift your finger and slide down to the last message.
Then use the three dot action button to execute the desired action.

<br />

<a name="faq56"></a>
**(56) Can you add support for JMAP?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq56)

There are almost no providers offering the [JMAP](https://jmap.io/) protocol,
so it is not worth a lot of effort to add support for this to FairEmail.

Moreover, the only available [Java JMAP library](https://github.com/iNPUTmice/jmap) seems not to be maintained anymore.

<br />

<a name="faq57"></a>
**(57) Can I use HTML in signatures?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq57)

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

If you want to resize an image, you could do it like this:

```
<img src="..." width="..." height="...">
```

The recipient of your message might not like large images in messages,
so it is better to resize images with an image editor first.

<br />

<a name="faq58"></a>
**(58) What does an open/closed email icon mean?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq58)

The email icon in the folder list can be open (outlined) or closed (solid):

<img alt="Mail outline image" src="https://raw.githubusercontent.com/M66B/FairEmail/master/images/baseline_mail_outline_black_48dp.png" width="48" height="48" />

Message bodies and attachments are not downloaded by default.

<img alt="Mail image" src="https://raw.githubusercontent.com/M66B/FairEmail/master/images/baseline_email_black_48dp.png" width="48" height="48" />

Message bodies and attachments are downloaded by default.

<br />

<a name="faq59"></a>
**(59) Can original messages be opened in the browser?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq59)

For security reasons the files with the original message texts are not accessible to other apps, so this is not possible.
In theory the [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) could be used to share these files,
but even Google's Chrome cannot handle this.

<br />

<a name="faq60"></a>
**(60) Did you know ...?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq60)

* Did you know that starred messages can be synchronized/kept always? (this can be enabled in the receive settings)
* Did you know that you can long press the 'write message' icon to go to the drafts folder?
* Did you know there is an advanced option to mark messages read when they are moved? (archiving and trashing is also moving)
* Did you know that you can select text (or an email address) in any app on recent Android versions and let FairEmail search for it? (this can be enabled in the miscellaneous settings)
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
* Did you know that if you select text and reply, only the selected text will be quoted? (this works for reformatted messages only because of [this issue](https://issuetracker.google.com/issues/36939405))
* Did you know that you can long press the trash icons (both in the message and the bottom action bar) to permanently delete a message or conversation? (version 1.1368+)
* Did you know that you can long press the archive button in the bottom action bar to move a conversation? (version 1.2160+)
* Did you know that you can long press the send action to show the send dialog, even if it was disabled?
* Did you know that you can long press the full screen icon to show the original message text only?
* Did you know that you can long press the answer button to reply to the sender? (since version 1.1562; since version 1.1839 you can configure the action in the send settings)
* Did you know that you can long press the message move button to move across accounts? (since version 1.1702)
* Did you know that you can long press the folder name in the message header when viewing a conversation to navigate to the folder? (since version 1.1720)
* Did you know that you can long press the add contact button in the message composer to insert a contact group? (since version 1.1721)
* Did you know that you can long press the image action to show the image dialog, even if it was disabled? (since version 1.1772)
* Did you know that you can long press the "] \[" button to fit original messages to the screen width? (this might result in "thin" messages)
* Did you know that you can long press on the save drafts button for a grammar, style, and spell check via [LanguageTools](https://languagetool.org/)?
* Did you know that you can long press a folder in the folder selection dialog to copy instead of move a message?
* Did you know that you can long press a quick search button in the search dialog to copy the search term?

<br />

<a name="faq61"></a>
**(61) Why are some messages shown dimmed?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq61)

Messages shown dimmed (grayed) are locally moved messages for which the move is not confirmed by the server yet.
This can happen when there is no connection to the server or the account (yet).
These messages will be synchronized after a connection to the server and the account has been made
or, if this never happens, will be deleted if they are too old to be synchronized.

You might need to manually synchronize the folder, for example by pulling down.

You can view these messages, but you cannot move these messages again until the previous move has been confirmed.

Pending [operations](#faq3) are shown in the operations view accessible from the main navigation menu.

<br />

<a name="faq62"></a>
**(62) Which authentication methods are supported?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq62)

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq63)

You can add images to a message in two ways:

* As attachment, by tapping on the paperclip icon button in the bottom action bar
* As image, by tapping on the image icon button in the bottom action bar

If you disabled the bottom action bar, you can enable it again via the three-dots menu at the top right of the message editor.

An attachment will always be sent as-is.
An image can be added as attachment or inserted into a message and you can select to reduce the size of the image and to remove privacy sensitive information.

Images will be scaled down using whole number factors to reduce memory usage and to retain the image quality.
Scaled images will be saved with a compression ratio of 90 %.

If the image options do not appear, you can enable them again via the three-dots menu.

Note that most email providers limit the total message size to 10-50 MB.

Automatically resizing of inline and/or attached images and the maximum target image size can be configured in the send settings.

<br />

<a name="faq64"></a>
**~~(64) Can you add custom actions for swipe left/right?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq64)

~~The most natural thing to do when swiping a list entry left or right is to remove the entry from the list.~~
~~The most natural action in the context of an email app is moving the message out of the folder to another folder.~~
~~You can select the folder to move to in the account settings.~~

~~Other actions, like marking messages read and snoozing messages are available via multiple selection.~~
~~You can long press a message to start multiple selection. See also [this question](#faq55).~~

~~Swiping left or right to mark a message read or unread is unnatural because the message first goes away and later comes back in a different shape.~~
~~Note that there is an advanced option to mark messages automatically read on moving,~~
~~which is in most cases a perfect replacement for the sequence mark read and move to some folder.~~
~~You can also mark messages read from new message notifications.~~

~~If you want to read a message later, you can hide it until a specific time by using the *snooze* menu.~~

<br />

<a name="faq65"></a>
**(65) Why are some attachments shown dimmed?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq65)

Inline (image) attachments are shown dimmed.
[Inline attachments](https://tools.ietf.org/html/rfc2183) are supposed to be downloaded and shown automatically,
but since FairEmail doesn't always download attachments automatically, see also [this FAQ](#faq40),
FairEmail shows all attachment types. To distinguish inline and regular attachments, inline attachments are shown dimmed.

<br />

<a name="faq66"></a>
**(66) Is FairEmail available in the Google Play Family Library?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq66)

"*You can't share in-app purchases and free apps with your family members.*"

See [here](https://support.google.com/googleone/answer/7007852)
under "*See if content is eligible to be added to Family Library*", "*Apps & games*".

In other words, only subscriptions can be shared and since there is no subscription, FairEmail is not shareable via the Google Play Family Library.

<br />

<a name="faq67"></a>
**(67) How can I snooze conversations?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq67)

Multiple select one of more conversations (long press to start multiple selecting), tap the three dot button and select *Snooze ...*.
Alternatively, in the expanded message view use *Snooze ...* in the message three-dots 'more' menu or the time-lapse action in the bottom action bar.
Select the time the conversation(s) should snooze and confirm by tapping OK.
The conversations will be hidden for the selected time and shown again afterwards.
You will receive a new message notification as reminder.

It is also possible to snooze messages with [a rule](#faq71),
which will also allow you to move messages to a folder to let them be auto snoozed.

You can show snoozed messages by unchecking *Filter out* > *Hidden* in the three dot overflow menu.

You can tap on the small snooze icon to see until when a conversation is snoozed.

By selecting a zero snooze duration you can cancel snoozing.

Third party apps do not have access to the Gmail snoozed messages folder.

<br />

<a name="faq68"></a>
**~~(68) Why can Adobe Acrobat reader not open PDF attachments / Microsoft apps not open attached documents?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq68)

~~Adobe Acrobat reader and Microsoft apps still expects full access to all stored files,~~
~~while apps should use the [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) since Android KitKat (2013)~~
~~to have access to actively shared files only. This is for privacy and security reasons.~~

~~You can workaround this by saving the attachment and opening it from the Adobe Acrobat reader / Microsoft app,~~
~~but you are advised to install an up-to-date and preferably open source PDF reader / document viewer,~~
~~for example one listed [here](https://github.com/offa/android-foss#-document--pdf-viewer).~~

<br />

<a name="faq69"></a>
**(69) Can you add auto scroll up on new message?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq69)

The message list is automatically scrolled up when navigating from a new message notification or after a manual refresh.
Always automatically scrolling up on arrival of new messages would interfere with your own scrolling,
but if you like you can enable this in the settings.

<br />

<a name="faq70"></a>
**(70) When will messages be auto expanded?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq70)

When navigation to a conversation one message will be expanded if:

* There is just one message in the conversation
* There is exactly one unread message in the conversation
* There is exactly one starred (favorite) message in the conversation (since version 1.1508)

There is one exception: the message was not downloaded yet
and the message is too large to download automatically on a metered (mobile) connection.
You can set or disable the maximum message size on the 'connection' settings tab.

Duplicate (archived) messages, trashed messages and draft messages are not counted.

Messages will automatically be marked read on expanding, unless this was disabled in the individual account settings.

<br />

<a name="faq71"></a>
**(71) How do I use rules?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq71)

You can edit rules by long pressing a folder in the folder list of an account (tap the account name in the navigation/side menu).

New rules will be applied to new messages received in the folder, not to existing messages.
You can check the rule and apply the rule to existing messages or, alternatively, long press the rule in the rule list and select *Execute now*.

You'll need to give a rule a name and you'll need to define the order in which a rule should be executed relative to other rules.

You can disable a rule and you can stop processing other rules after a rule has been executed, which can be used to create a *not* condition.

Since version 1.2061 rules can be part of a named group.
Group names will be displayed in the list of rules.
If a rule is part of a group, stop processing means stop processing the group.

Since version 1.2018 there is a rule option to run rules daily on messages (around 1:00am) older than xxx.

<br>

**Conditions**

The following rule conditions are available:

* Sender (from, reply-to) contains or sender is contact
* Recipient (to, cc, bcc) contains
* Subject contains
* Has attachments (optional of specific type)
* Header contains
* Text contains (since version 1.1785)
* Absolute time (received) between (since version 1.1540)
* Relative time (received) between
* Expression (since version 1.2174)

All the conditions of a rule need to be true for the rule action to be executed.
All conditions are optional, but there needs to be at least one condition, to prevent matching all messages.

If you want to match all senders or all recipients, you can just use the @ character as condition because all email addresses will contain this character.
If you want to match a domain name, you can use as a condition something like *@example.org*

Note that email addresses are formatted like this:

``
"Somebody" <somebody@example.org>
``

When using a regex, you need to take care to match the complete address.

Note that message texts are normalized when not using a regex, which means that all whitespaces (spaces, tabs, line breaks, etc) are replaced by a single space.
This makes it easier to match texts on multiple lines or when the line break is at different places.

Since version 1.1996 it is possible to use [Jsoup selectors](https://jsoup.org/cookbook/extracting-data/selector-syntax) to match HTML elements,
by prefixing the selector by *jsoup:* and entering it as text contains condition, like for example:

```
jsoup:html > body > div > a[href=https://example.org]
```

You can use multiple rules, possibly with a *stop processing*, for an *or* or a *not* condition.
Since version 1.2173 there is a *NOT* option for conditions that accept a regex.

Matching is not case sensitive, unless you use [regular expressions](https://en.wikipedia.org/wiki/Regular_expression).
Please see [here](https://developer.android.com/reference/java/util/regex/Pattern) for the documentation of Java regular expressions.
Note that you need to match the complete text from the first to the last character.
You can test a regex [here](https://regexr.com/).

You can use a regex condition like this to match a top-level domain (tld):

```
.*@.*\.xyz>
```

Note that a regular expression supports an *or* operator, so if you want to match multiple senders, you can do this:

```
.*alice@example\.org.*|.*bob@example\.org.*|.*carol@example\.org.*
```

Note that [dot all mode](https://developer.android.com/reference/java/util/regex/Pattern#DOTALL) is enabled
to be able to match [unfolded headers](https://tools.ietf.org/html/rfc2822#section-3.2.3).

<br />

<a name="expression_conditions"></a>
**Expressions**

Since version 1.2174 it is possible to use expression conditions like:

```from contains "@example.com" && subject contains "Example"```

Please [see here](https://ezylang.github.io/EvalEx/references/references.html) about which constants, operators and functions are available.

The following extra variables are available:

* *received* (long, unix epoch in milliseconds; since version 1.2179)
* *from* (array of strings)
* *to* (array of strings)
* *subject* (string)
* *text* (string)
* *hasAttachments* (boolean; deprecated, use function *attachments()* instead)

The following extra operators are available:

* *contains* (string/array of strings contains substring)
* *matches* (string/array of strings matches regex)

The following extra functions are available:

* *header(name)* (returns an array of header values for the named header)
* *blocklist()* (version 1.2176-1.2178; deprecated, use *onBlocklist()* instead)
* *onBlocklist()* (returns a boolean indicating if the sender/server is on a DNS blocklist; since version 1.2179)
* *hasMx()* (returns a boolean indicating if the from/reply-to address has an associated MX record; since version 1.2179)
* *attachments(regex)* (returns an integer indicating number of attachments; since version 1.2179; optional regex since version 1.2194)
* *Jsoup()* (returns an array of selected strings; since version 1.2179)
* *Size(array)* (returns the number of items in an array; since version 1.2179)
* *knownContact()* (returns a boolean indicating that the from/reply-to address is in the Android address book or in the local contacts database)
* *AI(prompt)* (perform interference with the configured AI model using the specified prompt, returning the result as a string; since version 1.2243)

Example conditions:

```header("X-Mailer") contains "Open-Xchange" && from matches ".*service@.*"```

```!onBlocklist() && hasMx() && attachments() > 0```

```(received + 7*24*60*60*1000) < DT_DATE_TO_EPOCH(DT_NOW())```

```AI("Is the message below a scam? Answer in English with just yes or no.") contains "yes"```

<br>

**Actions**

You can select one of these actions to apply to matching messages:

* No action (useful for *not*)
* Mark as read
* Mark as unread
* Hide
* Suppress notification
* Silent notification (since version 1.2150)
* Snooze
* Add star
* Set importance (local priority)
* Add keyword
* Add local notes (since version 1.2094)
* Move
* Copy (Gmail: label)
* Delete permanently (since version 1.1801)
* Play sound (since version 1.1803; experimental)
* Answer/forward (with template)
* Text-to-speech (sender and subject)
* Automation (Tasker, etc)
* Webhook (since version 1.2107)

**Important**: permanent deletion is **irreversible**.
Instead, consider to move messages to the trash folder
and to set up auto deletion for the trash folder in the folder properties (long press the folder in the folder list of an account).

If you want to forward a message, consider to use a *move* action instead.
This will be more reliable than forwarding because forwarded messages might be considered as spam.

<br>

It is possible to use a [Jsoup selector](https://jsoup.org/cookbook/extracting-data/selector-syntax) to select the text for notes, for example:

```
jsoup:td > span:containsOwn(€)
```

The text of the first matched HTML element up to 512 characters will be used as text for the local note.

<br>

A *move* action can optionally create subfolders (since version 1.1966) to move messages to, for which you can use the following placeholders:

```
$day$ (since version 1.2030)
$week$
$month$
$year$
$domain$
$group$ (since version 1.2030)
```

$group$ will be replaced with the contact group name of the sender, provided that the related contact is assigned to one contact group only.
Note that the Android contact provider isn't very fast, so using this placeholder can slow down fetching messages.

<br />

Since version 1.2132 it is possible to use the following placeholders in keywords:

```
$day$
$week$
$month$
$year$
```

<br />

Since message headers are not downloaded and stored by default to save on battery and data usage and to save storage space
it is by default not possible to preview which messages would match a header rule condition.
You can enable downloading message headers in the connection settings and check headers conditions anyway (since version 1.1599).

Some common header conditions (regex):

* *.&ast;To:.&ast;undisclosed-recipients.&ast;*
* *.&ast;Cc:.&ast;test@example.com.&ast;*
* *.&ast;Envelope-to:.&ast;test@example.com.&ast;*
* *.&ast;Auto-Submitted:.&ast;* [RFC3834](https://tools.ietf.org/html/rfc3834)
* *.&ast;List-Unsubscribe:.&ast;* [RFC3834](https://datatracker.ietf.org/doc/html/rfc2369)
* *.&ast;Content-Type:.&ast;multipart/report.&ast;* [RFC3462](https://tools.ietf.org/html/rfc3462)

To match *set* IMAP flags (keywords) via a header condition (since version 1.1777):

```
$<keyword>$
```

To match *set* message flags via a header condition (since version 1.1777):

```
$$seen$ (read)
$$answered$ (replied to)
$$flagged$ (starred, favorite)
$$deleted$ (marked as deleted)
```

To match *passed* message checks via a header condition (since version 1.1787):

```
$$tls$ (since version 1.1826)
$$dkim$
$$spf$
$$dmarc$
$$auth$ (since version 1.2141)
$$mx$
$$blocklist$
$$replydomain$
$$nofrom$ (since version 1.1791)
$$multifrom$ (since version 1.1791)
$$automatic$ (since version 1.1862)
$$lowpriority$ (since version 1.1958)
$$highpriority$ (since version 1.1958)
$$signed$ (since version 1.1981)
$$encrypted$ (since version 1.1981)
$$aligned$ (since version 1.2049)
```

Note that *regex* should be disabled and that there should be no white space.

Please be aware that a difference in the *from* and *reply-to* domain, and no or multi *from* addresses isn't a good indication of spam.

Since the app sets the keyword *$Filtered$* after the rules have been executed for a message,
you can create a rule to prevent the rules from being executed again (which is sometimes desirable):

* Name: anything you like
* Order: lower than all other rules, for example 0
* Stop processing rules after executing this rule: enabled
* Header contains: *$$Filtered$* (no spaces)
* Action: No action

Note that not all email servers support IMAP keywords.

<br />

The automation action will broadcast the intent *eu.faircode.email.AUTOMATION* with the following string extras:

* *rule*
* *received* (ISO 8601 date/time)
* *sender*
* *subject*
* *preview* (since version 1.2222)

An app like Tasker can listen for this intent and perform some action.
Please [see here](https://tasker.joaoapps.com/userguide/en/intents.html) about receiving intents in Tasker.

<br />

You can long-press a rule in the list of rules to copy it, which can be useful if you need a rule with the same condition but a different action.

<br />

In the three-dots *more* message menu there is an item to create a rule for a received message with the most common conditions filled in.

<br />

If you want to set up archiving by week, month, year, etc,
you can do this with rules with an absolute time condition on a 'jump' archive folder where archived messages are being moved to as a first step.
Such a rule will move the messages to a (sub) archive folder as a second step.

The POP3 protocol does not support setting keywords and moving or copying messages.

<br />

Since version 1.2061 it is possible to execute rules with an automation app, like for example Tasker.


```
(adb shell) am start-foreground-service -a eu.faircode.email.RULE --es account <account name> -es rule <unique rule name>
```

<br />

Since version 1.2107 it is possible to executed webhooks.
The supported HTTP methods are GET, HEAD, OPTIONS, POST and POST.
The query parameters will be sent as body in the case of POST and PUT.
Note that execution of webhooks won't be retried. So, don't use this for anything critical.

This website might be useful for testing webhooks:

[https://webhook.site/](https://webhook.site/)

<br />

<a name="autoanswer"></a>
**Auto reply/answer**

First, create a template with the text to reply/answer with via the navigation menu (left side menu).

After creating a template text, go to the rules via the navigation menu (left side menu), and create a rule for the inbox like this:

* **Name**: anything you like to find back the rule later
* **Sender contains**: @ (this will match all senders)
* **Action**: Reply/forward
* **Identity**: select the email address to reply with
* **Reply template**: select the created text template
* **Save** the rule with the button in the bottom action bar

<br />

Using rules is a pro feature.

<br />

<a name="faq72"></a>
**(72) What are primary accounts/identities?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq72)

The primary account is used when the account is ambiguous, for example when starting a new draft from the unified inbox.

Similarly, the primary identity of an account is used when the identity is ambiguous.

The default email address is the email address of the primary identity of the primary account.

There can be just one primary account and there can be just one primary identity per account.

You can set an account or an identity to be primary by long pressing it in the list of accounts or identities.
You can go to the list of accounts via *Manual setup and account options* in the main setup screen.

<br />

<a name="faq73"></a>
**(73) Is moving messages across accounts safe/efficient?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq73)

Moving messages across accounts is safe because the raw, original messages will be downloaded and moved
and because the source messages will be deleted only after the target messages have been added

Batch moving messages across accounts is efficient if both the source folder and target folder are set to synchronize,
else FairEmail needs to connect to the folder(s) for each message.

<br />

<a name="faq74"></a>
**(74) Why do I see duplicate messages?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq74)

Some providers, notably Gmail, list all messages in all folders, except trashed messages, in the archive (all messages) folder too.
FairEmail shows all these messages in a non obtrusive way to indicate that these messages are in fact the same message.

Gmail allows one message to have multiple labels, which are presented to FairEmail as folders.
This means that messages with multiple labels will be shown multiple times as well.

<br />

<a name="faq75"></a>
**(75) Can you make an iOS, Windows, Linux, etc version?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq75)

A lot of knowledge and experience is required to successfully develop an app for a specific platform,
which is why I develop apps for Android only.

You can install FairEmail on recent Windows versions, though, see [here](#faq185), and also on ChromeOS via the Play Store.

<br />

<a name="faq76"></a>
**(76) What does 'Clear local messages' do?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq76)

The folder menu *Clear local messages* removes messages from the device which are present on the server too.
It does not delete messages from the server.
This can be useful after changing the folder settings to not download the message content (text and attachments), for example to save space.

<br />

<a name="faq77"></a>
**(77) Why are messages sometimes shown with a small delay?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq77)

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq78)

In the receive settings you can enable scheduling and set a time period and the days of the week *when* messages should be *received*.

The schedule section shows two parts, *Workdays* and *Weekend*.
Each of them has a start time and a finish time.
Pressing on any one of these means the time can be edited.

Below these sections, there is a list of day names, *Sunday* through to *Saturday*.
Some of these may be in bold text.
The ones that are in bold are set using the cog next to the *Weekend* section.
If you press the cog, you can choose which days you define to be the weekend.
Typically, it will be *Sunday* and *Saturday*, but you can choose any day.
Ticking one of these means that that will now be shown as bold in the list of days.

The times under the *Weekend* section apply to those days which are in bold.
The days which are not in bold are regarded as the workdays.

You can untick any of the days in the list of days and that means that the schedule will not apply to those days.

Note that an end time equal to or earlier than the start time is considered to be 24 hours later.

Since version 1.1880 is is possible to exclude accounts from scheduling in the advanced account settings.
This means you can synchronize a business account during business hours only and make an exception for personal accounts.

Automation, see below, can be used for more advanced schedules,
like for example multiple synchronization periods per day or different synchronization periods for different days.

It is possible to install FairEmail in multiple user profiles, for example a personal and a work profile, and to configure FairEmail differently in each profile,
which is another possibility to have different synchronization schedules and to synchronize a different set of accounts.

It is also possible to create [rules](#faq71) with a time condition and to snooze messages until the end time of the time condition.
This way it is possible to *snooze* business related messages until the start of the business hours.
This also means that the messages will be on your device for when there is (temporarily) no internet connection. How to:

* Go to the folder list of an account by tapping on the account name in the navigation menu (left side menu)
* Long press the inbox in the folder list and select *Edit rules*
* Tap om the big *plus* button at the bottom to create a new rule
* Enter as name for example *Snooze business messages*
* Under relative time select a start and end date/time, for example Friday 17:00 and Monday 09:00
* Select *Snooze* as action
* Save the rule with the save button in the bottom action bar

Note that recent Android versions allow overriding DND (Do Not Disturb) per notification channel and per app,
which could be used to (not) silence specific (business) notifications.
Please [see here](https://support.google.com/android/answer/9069335) for more information.

Since version 1.2150 it is possible to create [rules](#faq71) to silence specific new message notifications.

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
Since version 1.1600 an account will be disabled/enabled by setting the account to manual/automatic sync, so the folders and messages keep being accessible.

To set the poll interval:

```
(adb shell) adb shell am start-foreground-service -a eu.faircode.email.INTERVAL --ei minutes nnn
```

Where *nnn* is one of 0, 5, 15, 30, 60, 120, 240, 480, 1440. A value of 0 means push messages.

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq79)

Normally, FairEmail maintains a connection to the configured email servers whenever possible to receive messages in real-time.
If you don't want this, for example to be not disturbed or to save on battery usage, just disable receiving in the receive settings.
This will stop the background service which takes care of automatic synchronization and will remove the associated status bar notification.

You can also enable *Synchronize manually* in the advanced account settings if you want to manually synchronize specific accounts only.

You can use pull-down-to-refresh in a message list or use the folder menu *Synchronize now* to manually synchronize messages.

If you want to synchronize some or all folders of an account manually, just disable synchronization for the folders (but not of the account).

You'll likely want to disabled [browse on server](#faq24) too.

<br />

<a name="faq80"></a>
**~~(80) How do I fix the error 'Unable to load BODYSTRUCTURE'?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq80)

~~The error message *Unable to load BODYSTRUCTURE* is caused by bugs in the email server,~~
~~see [here](https://javaee.github.io/javamail/FAQ#imapserverbug) for more details.~~

~~FairEmail already tries to workaround these bugs, but if this fail you'll need to ask for support from your provider.~~

<br />

<a name="faq81"></a>
**(81) Can you make the background of the original message view dark in dark themes?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq81)

The original message view will use a dark background when using a dark theme for Android version 10 and later.

~~For Android before version 10 Google removed this feature from the [Android System WebView](https://play.google.com/store/apps/details?id=com.google.android.webview),~~
~~even though it worked fine in most cases.~~
~~Please see [this issue](https://issuetracker.google.com/issues/237785596) (requires a Google account login) requesting to restore this feature again for more information.~~

<br />

<a name="faq82"></a>
**(82) What is a tracking image?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq82)

Please see [here](https://en.wikipedia.org/wiki/Web_beacon) about what a tracking image is.
In short, companies, organizations and even governments use tracking images to see if and when you opened a message.

The BBC article '[Spy pixels in emails have become endemic](https://www.bbc.com/news/technology-56071437)' is worth reading.

FairEmail will in most cases automatically recognize tracking images and replace them by this icon:

<img alt="Tracking image" src="https://raw.githubusercontent.com/M66B/FairEmail/master/images/baseline_my_location_black_48dp.png" width="48" height="48" />

Automatic recognition of tracking images can be disabled in the privacy-settings tab page.

<br />

<a name="faq84"></a>
**(84) What are local contacts for?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq84)

Local contact information is based on names and addresses found in incoming and outgoing messages.

The main use of the local contacts storage is to offer auto completion when no contacts permission has been granted to FairEmail.

Another use is to generate [shortcuts](#faq31) on recent Android versions to quickly send a message to frequently contacted people.
This is also why the number of times contacted and the last time contacted is being recorded
and why you can make a contact a favorite or exclude it from favorites by long pressing it.

The list of contacts is sorted on number of times contacted and the last time contacted.

By default only names and addresses to whom you send messages to will be recorded.
You can change this in the send settings.

<br />

<a name="faq85"></a>
**(85) Why is an identity not available?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq85)

An identity is available for sending a new message or replying or forwarding an existing message only if:

* the identity is set to synchronize (send messages)
* the associated account is set to synchronize (receive messages)
* the associated account has a drafts folder

FairEmail will try to select the best identity based on the *to* address of the message replied to / being forwarded.

<br />

<a name="faq86"></a>
**~~(86) What are 'extra privacy features'?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq86)

~~The advanced option *extra privacy features* enables:~~

* ~~Looking up the owner of the IP address of a link~~
* ~~Detection and removal of [tracking images](#faq82)~~

<br />

<a name="faq87"></a>
**(87) What does 'invalid credentials' mean?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq87)

The error message *invalid credentials* means either that the user name and/or password is incorrect,
for example because the password was changed or expired, or that the account authorization has expired, for example due to logging out.

If the password is incorrect/expired, you will have to update the password in the account and/or identity settings.

If the account authorization has expired, you will have to select the account again.
You will likely need to save the associated identity again as well.

In the case of a Gmail account the error *Invalid credentials (Failure)* can be the result of *accounts.google.com* being blocked for the Android account manager,
for example due to using a VPN, firewall, ad blocker, or similar.
So, try to disable all VPN based apps or allow this address.

<br />

<a name="faq88"></a>
**(88) How can I use a Yahoo/AT&T, AOL or Sky account?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq88)

The preferred way to set up a Yahoo account is by using the quick setup wizard,
which will use OAuth instead of a password and is therefore safer (and easier as well).

To authorize a Yahoo/AT&T, AOL, or Sky account you need to use an app password instead of your normal account password.
For instructions about how to create an app password, please see here:

* [for Yahoo/AT&T](https://help.yahoo.com/kb/generate-third-party-passwords-sln15241.html)
* [for AOL](https://help.aol.com/articles/Create-and-manage-app-password) ~~**Important**: app password generation is broken, [frustrating many people](https://aol.uservoice.com/forums/912886-aol-mail/suggestions/45235399-i-wanted-to-generate-a-third-party-app-password-bu) because this means you can use the AOL app and the browser only.~~
* [for Sky](https://www.sky.com/help/articles/getting-started-with-sky-yahoo-mail) (under *Other email apps*)

Please see [this FAQ](#faq111) about OAuth support.

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq89)

By default FairEmail sends each message both as plain text and as HTML formatted text because almost every receiver expects formatted messages these days.
If you want/need to send plain text only messages, you can enable this in the send options.
You can enable/disable sending plain text only messages in the send dialog on a case by case basis as well.
If you disabled the send dialog, you can long press the *Send* button to show it again.

<br />

<a name="faq90"></a>
**(90) Why are some texts linked while not being a link?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq90)

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq91)

~~Synchronizing messages is an expensive proces because the local and remote messages need to be compared,~~
~~so periodically synchronizing messages will not result in saving battery power, more likely the contrary.~~

~~See [this FAQ](#faq39) about optimizing battery usage.~~

<br />

<a name="faq92"></a>
**(92) Can you add spam filtering, verification of the DKIM signature and SPF authorization?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq92)

Spam filtering, verification of the [DKIM](https://en.wikipedia.org/wiki/DomainKeys_Identified_Mail) signature
and [SPF](https://en.wikipedia.org/wiki/Sender_Policy_Framework) authorization is a task of email servers,
not of an email client, which is basically a viewer for messages on an email server.
Servers generally have more memory and computing power, so they are much better suited to this task than battery-powered devices.
Also, you'll want spam filtered for all your email clients, possibly including web email, not just for one email client on one device.
Moreover, email servers have access to information, like the IP address, etc. of the connecting server, which an email client has no access to.
Furthermore, an email server can inspect all messages of all email accounts, while an email client can inspect messages in your email account only.

If you are receiving a significant amount of spam, the first thing you should do is consider switching to another email provider.
Some email servers excell at filtering spam, and others are really bad at it.
Switching to another e-mail provider is no fun, but neither is wading through piles of spam on a daily basis.

Spam filtering based on message headers might have been feasible,
but unfortunately this technique is [patented by Microsoft](https://patents.google.com/patent/US7543076).

Recent versions of FairEmail can filter spam to a certain extent using a message classifier.
Please see [this FAQ](#faq163) for more information about this.

Of course you can report messages as spam with FairEmail,
which will move the reported messages to the spam folder and train the spam filter of the provider, which is how it is supposed to work.
This can be done automatically with [rules](#faq71) too.
Blocking the sender will create a filter rule to automatically move future messages of the same sender into the spam folder.

Note that the POP3 protocol gives access to the inbox only. So, it is won't be possible to report spam for POP3 accounts.

Note that you should not delete spam messages, also not from the spam folder,
because the email server uses the messages in the spam folder to "learn" what spam messages are.

If you are wondering why a message was moved into the spam folder, these are all possible reasons:

* The email server of your email provider moved the message into the spam folder. The app can't undo this automatically because this can result in an endless loop.
* The message classifier (miscellaneous settings tab page) moved the message into the spam folder.
* A filter rule (navigation menu = left side menu of the start screen), for example a block domain name rule, moved the message into the spam folder.
* An email address or a network address is on a block list (receive settings tab page).

Note that a sender will automatically be blocked when a message is moved into the spam folder.
You can disable this behavior by disabling the option *Automatically block the sender when reporting spam* in the behavior settings tab page.

Since version 1.2143, there is an "*Unblock all*" button in the receive-settings tab page, which will reset all above options.

If you receive a lot of spam messages in your inbox, the best you can do is to contact the email provider to ask if spam filtering can be improved.

Also, FairEmail can show a small red warning flag
when DKIM, SPF or [DMARC](https://en.wikipedia.org/wiki/DMARC) authentication failed on the receiving server.
You can enable/disable [authentication verification](https://en.wikipedia.org/wiki/Email_authentication) in the display settings.
The feature depends on the header [Authentication-Results](https://datatracker.ietf.org/doc/html/rfc7601), which the receiving email server should add.
The shield will be green only if DMARC passes (=alignment)
and either [SPF](https://en.wikipedia.org/wiki/Sender_Policy_Framework) or [DKIM](https://en.wikipedia.org/wiki/DomainKeys_Identified_Mail) passes.

If the email server doesn't add an *Authentication-Results* header, which is optional,
you can enable native DKIM in the debug panel, which appears when you enable debug mode in the miscellaneous settings tab page (last option).
In this case, the shield will be green only when DKIM passes and the signer domain matches that of the sender (=alignment).
Please be aware that this option will increase both data and battery usage.

FairEmail can show a warning flag too if the domain name of the (reply) email address of the sender does not define an MX record pointing to an email server.
This can be enabled in the receive settings. Be aware that this will slow down synchronization of messages significantly.

If the domain name of the sender and the domain name of the reply address differ,
the warning flag will be shown too because this is most often the case with phishing messages.
If desired, this can be disabled in the receive settings (since version 1.1506).

If legitimate messages are failing authentication, you should notify the sender because this will result in a high risk of messages ending up in the spam folder.
Moreover, without proper authentication there is a risk the sender will be impersonated.
The sender might use [this tool](https://www.mail-tester.com/) to check authentication and other things.

<br />

<a name="faq93"></a>
**(93) Can you allow installation/data storage on external storage media (sdcard)?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq93)

FairEmail uses services and alarms, provides widgets and listens for the boot completed event to be started on device start,
so it is not possible to store the app on external storage media, like an sdcard.
See also [here](https://developer.android.com/guide/topics/data/install-location).

Since the app is small anyway and the data files will be stored in the same place, the benefit would be limited anyway.

Messages, attachments, etc stored on external storage media, like an sdcard, can be accessed by other apps and is therefore not safe.
See [here](https://developer.android.com/training/data-storage) for the details.
Instead, consider to use [adoptable storage](https://source.android.com/devices/storage/adoptable).

Since version 1.1829 is it possible to store attachments to external storage space private to the app (except for file managers) via an option in the debug panel.
You can enable the debug panel by enabling debug mode in the miscellaneous settings (last option).
To prevent ongoing operations from storing attachments at the old location
you should disable receiving messages in the receive settings and wait until all operations have been completed before changing this option.
Please be aware that removing the storage space will inevitably result in problems, which is one of the reasons why this option is hidden.

Please note that more often than not the external storage space is emulated on recent Android versions and recent devices.
and that there is no longer a viable way to get permission to write to other locations.

Moving messages to an sdcard is not an option because this would significantly reduce the response times of the app.

When needed you can save (raw) messages via the three-dots menu just above the message text
and save attachments by tapping on the floppy icon.

If you need to save on storage space, you can limit the number of days messages are being synchronized and kept on your device
and disable downloading and storing of message texts and attachments (which means only message headers will be stored).
You can change these settings by long pressing a folder in the folder list and selecting *Edit properties*.

<br />

<a name="faq94"></a>
**(94) What does the red/orange stripe at the end of the header mean?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq94)

The red/orange stripe at the left side of the header means that the DKIM, SPF or DMARC authentication failed.
See also [this FAQ](#faq92).

<br />

<a name="faq95"></a>
**(95) Why are not all apps shown when selecting an attachment or image?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq95)

For privacy and security reasons FairEmail does not have permissions to directly access files,
instead the Storage Access Framework, available and recommended since Android 4.4 KitKat (released in 2013), is used to select files.

If an app is listed depends on if the app implements a [document provider](https://developer.android.com/guide/topics/providers/document-provider).
If the app is not listed, you might need to ask the developer of the app to add support for the Storage Access Framework.

Android Q will make it harder and maybe even impossible to directly access files,
see [here](https://developer.android.com/preview/privacy/scoped-storage) and [here](https://www.xda-developers.com/android-q-storage-access-framework-scoped-storage/) for more details.

If you use MIUI, please make sure [MIUI optimization](https://android.stackexchange.com/questions/191228/what-is-miui-optimization) is enabled in the developer settings.
You can enable the developer options by tapping a few times on the MIUI version number in the settings, About phone.

See also [this FAQ](#faq25).

<br />

<a name="faq96"></a>
**(96) Where can I find the IMAP and SMTP settings?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq96)

The IMAP settings are part of the (custom) account settings and the SMTP settings are part of the identity settings.

<br />

<a name="faq97"></a>
**(97) What is 'cleanup'?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq97)

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq98)

After revoking contacts permissions Android does not allow FairEmail access to your contacts anymore.
However, picking contacts is delegated to and done by Android and not by FairEmail, so this will still be possible without contacts permissions.

<br />

<a name="faq99"></a>
**(99) Can you add a rich text or markdown editor?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq99)

FairEmail provides common text formatting (bold, italic, underline, text size and color) via a toolbar that appears after selecting some text.

A [Rich text](https://en.wikipedia.org/wiki/Formatted_text) or [Markdown](https://en.wikipedia.org/wiki/Markdown) editor
would not be used by many people on a small mobile device and, more important,
Android doesn't support a rich text editor and most rich text editor open source projects are abandoned.
See [here](https://forum.xda-developers.com/showpost.php?p=79061829&postcount=4919) for some more details about this.

<br />

<a name="faq100"></a>
**(100) How can I synchronize Gmail categories?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq100)

You can synchronize Gmail categories by creating filters to label categorized messages:

* Create a new filter via Gmail > Settings wheel > See all settings > Filters and Blocked Addresses > Create a new filter
* Enter a category search (see below) in the *Has the words* field and click *Create filter*
* Check *Apply the label* and select a label and click *Create filter*

Possible categories:

```
category:social
category:updates
category:forums
category:promotions
```

Documentation: [see here](https://support.google.com/mail/answer/7190).

Unfortunately, this is not possible for the snoozed messages folder.

You can use *Force sync* in the three-dots menu of the unified inbox to let FairEmail synchronize the folder list again
and you can long press the folders to enable synchronization.

<br />

<a name="faq101"></a>
**(101) What does the blue/orange dot at the bottom of the conversations mean?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq101)

The dot shows the relative position of the conversation in the message list.
The dot will be show orange when the conversation is the first or last in the message list, else it will be blue.
The dot is meant as an aid when swiping left/right to go to the previous/next conversation.

The dot is disabled by default and can be enabled with the display settings *Show relative conversation position with a dot*.

<br />

<a name="faq102"></a>
**(102) How can I enable auto rotation of images?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq102)

Images will automatically be rotated when automatic resizing of images is enabled in the settings (enabled by default).
However, automatic rotating depends on the [Exif](https://en.wikipedia.org/wiki/Exif) information to be present and to be correct,
which is not always the case. Particularly not when taking a photo with a camara app from FairEmail.

Note that only [JPEG](https://en.wikipedia.org/wiki/JPEG) and [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) images can contain Exif information.

<br />

<a name="faq104"></a>
**(104) What do I need to know about error reporting?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq104)

* Error reports will help improve FairEmail
* Error reporting is optional and opt-in
* Error reporting can be enabled/disabled via the *miscellaneous* settings tab page
* Error reports will automatically be sent anonymously to [Bugsnag](https://www.bugsnag.com/)
* [Telemetry](https://docs.bugsnag.com/platforms/android/configuration-options/#telemetry) is always disabled
* Bugsnag for Android is [open source](https://github.com/bugsnag/bugsnag-android)
* See [here](https://docs.bugsnag.com/platforms/android/automatically-captured-data/) about what data will be sent in case of errors; FairEmail uses a random user ID.
* See [here](https://docs.bugsnag.com/legal/privacy-policy/) for the privacy policy of Bugsnag
* Error reports will be sent to *sessions.bugsnag.com:443* and *notify.bugsnag.com:443*

Error reports have helped to find otherwise hard to find bugs and therefore improve the overall stability of the app.

<br />

<a name="faq105"></a>
**(105) How does the roam-like-at-home option work?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq105)

FairEmail will check if the country code of the SIM card and the country code of the network
are in the [EU roam-like-at-home countries](https://en.wikipedia.org/wiki/European_Union_roaming_regulations#Territorial_extent)
and assumes no roaming if the country codes are equal and the advanced roam-like-at-home option is enabled.

So, you don't have to disable this option if you don't have an EU SIM or are not connected to an EU network.

<br />

<a name="faq106"></a>
**(106) Which launchers can show a badge count with the number of unread messages?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq106)

Please [see here](https://github.com/leolin310148/ShortcutBadger#supported-launchers)
for a list of launchers which can show the number of unread messages.
Standard Android [does not support this](https://developer.android.com/training/notify-user/badges).

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

An alternative is to use the unread messages count home screen widget.
You can add this widget by long pressing on an empty place on the home screen.

If you are using Nova launcher and you want to show the number of notifications in the launcher icon (maximum 10; imposed by Nova launcher),
you'll need to enable *Notification access* in the Android *Special app access* settings for Nova launcher on recent Android versions.

Some launchers display a dot or a '1' for [the monitoring notification](#faq2),
despite FairEmail explicitly requesting not to show a *badge* for this notification.
This could be caused by a bug in the launcher app or in your Android version.
Please double check if the notification dot (badge) is disabled for the receive (service) notification channel.
You can go to the right notification channel settings via the notification settings of FairEmail.
This might not be obvious, but you can tap on the channel name for more settings.

FairEmail does send a new message count *broadcast* intent as well:

```
eu.faircode.email.NEW_MESSAGE_COUNT
```

The number of new, unread messages will be in an integer "*count*" parameter.

<br />

<a name="faq107"></a>
**(107) How do I use colored stars?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq107)

You can set a colored star via the *more* message menu, via multiple selection (started by long pressing a message),
by long pressing a star in a conversation or automatically by using [rules](#faq71).

You need to know that colored stars are not supported by the IMAP protocol and can therefore not be synchronized to an email server.
This means that colored stars will not be visible in other email clients and will be lost on downloading messages again.
However, the stars (without color) will be synchronized and will be visible in other email clients, when supported.

Some email clients use IMAP keywords for colors.
However, not all servers support IMAP keywords and besides that there are no standard keywords for colors.

<br />

<a name="faq108"></a>
**~~(108) Can you add permanently delete messages from any folder?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq108)

~~When you delete messages from a folder the messages will be moved to the trash folder, so you have a chance to restore the messages.~~
~~You can permanently delete messages from the trash folder.~~
~~Permanently delete messages from other folders would defeat the purpose of the trash folder, so this will not be added.~~

<br />

<a name="faq109"></a>
**~~(109) Why is 'select account' available in official versions only?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq109)

~~Using *select account* to select and authorize Google accounts require special permission from Google for security and privacy reasons.~~
~~This special permission can only be acquired for apps a developer manages and is responsible for.~~
~~Third party builds, like the F-Droid builds, are managed by third parties and are the responsibility of these third parties.~~
~~So, only these third parties can acquire the required permission from Google.~~
~~Since these third parties do not actually support FairEmail, they are most likely not going to request the required permission.~~

~~You can solve this in two ways:~~

* ~~Switch to the official version of FairEmail, see [here](https://github.com/M66B/FairEmail/blob/master/README.md#downloads) for the options~~
* ~~Use app specific passwords, see [this FAQ](#faq6)~~

~~Using *select account* in third party builds is not possible in recent versions anymore.~~
~~In older versions this was possible, but it will now result in the error *UNREGISTERED_ON_API_CONSOLE*.~~

<br />

<a name="faq110"></a>
**(110) Why are (some) messages empty and/or attachments corrupt?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq110)

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq111)

OAuth for Gmail is supported via the quick setup wizard.
~~The Android account manager will be used to fetch and refresh OAuth tokens for selected on-device accounts.~~
~~OAuth for non on-device accounts is not supported~~
~~because Google requires a [yearly security audit](https://support.google.com/cloud/answer/9110914) ($15,000 to $75,000) for this.~~
~~Since FairEmail is basically offered free of charge, it is not an option to pay such an amount annually for a security audit.~~
~~You can read more about this [here](https://www.theregister.com/2019/02/11/google_gmail_developer/).~~

OAuth for Outlook/Office 365, Yahoo, Mail.ru and Yandex is supported via the quick setup wizard.

The OAuth [jump page](https://oauth.faircode.eu/) exists
for when [Android App Links](https://developer.android.com/training/app-links/verify-site-associations)
are not available, for example when using a non Play store version of the app, or do not work for some reason.

OAuth is not supported for third party builds like the F-Droid build, please [see here](#faq147) about why not.

Since version 1.1859 there is support for custom OAuth.
To use custom OAuth, an XML file containing the server and OAuth data, like the client secret, should be created and imported.
Please [see here](https://github.com/M66B/FairEmail/blob/master/app/src/main/res/xml/providers.xml) about how the XML file should look like.
The XML file can be imported via a button in the debug panel of the miscellaneous settings of the app.
To show the debug panel, debug mode mode should temporarily be enabled.
After importing, you can use the quick setup wizard to configure an account.

<br />

<a name="faq112"></a>
**(112) Which email provider do you recommend?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq112)

FairEmail is an email client only, so you need to bring your own email address.
Note that this is clearly mentioned in the app description.

There are plenty of email providers to choose from.
Which email provider is best for you depends on your wishes/requirements.
Please see these websites for lists of privacy oriented email providers with advantages and disadvantages:

* [Restore privacy](https://restoreprivacy.com/secure-email/)
* [Privacy Guides](https://www.privacyguides.org/en/email/)
* [Privacy Tools](https://www.privacytools.io/providers/email/)

**Important**: Some providers, like ProtonMail and Tutanota, use proprietary email protocols, which make it impossible to use third party email apps.
Please see [this FAQ](#faq129) for more information.

Using your own (custom) domain name, which is supported by most email providers, will make it easier to switch to another email provider.

<br />

<a name="faq113"></a>
**(113) How does biometric authentication work?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq113)

If your device has a biometric sensor, for example a fingerprint sensor, you can enable/disable biometric authentication in the navigation (hamburger) menu of the settings screen.
When enabled FairEmail will require biometric authentication after a period of inactivity or after the screen has been turned off while FairEmail was running.
Activity is navigation within FairEmail, for example opening a conversation thread.
The inactivity period duration can be configured in the miscellaneous settings.
When biometric authentication is enabled new message notifications will not show any content and FairEmail won't be visible on the Android recents screen.

Biometric authentication is meant to prevent others from seeing your messages only.
FairEmail relies on device encryption for data encryption, see also [this FAQ](#faq37).

Biometric authentication is a pro feature.

<br />

<a name="faq114"></a>
**(114) Can you add an import for the settings of other email apps?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq114)

The format of the settings files of most other email apps is not documented, so this is difficult.
Sometimes it is possible to reverse engineer the format, but as soon as the settings format changes things will break.
Also, settings are often incompatible.
For example, FairEmail has unlike most other email apps settings for the number of days to synchronize messages
and for the number of days to keep messages, mainly to save on battery usage.
Moreover, setting up an account/identity with the quick setup wizard is simple, so it is not really worth the effort.

<br />

<a name="faq115"></a>
**~~(115) Can you add email address chips?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq115)

~~Email address [chips](https://material.io/design/components/chips.html) look nice, but cannot be edited,~~
~~which is quite inconvenient when you made a typo in an email address.~~

~~Note that FairEmail will select the address only when long pressing an address, which makes it easy to delete an address.~~

~~Chips are not suitable for showing in a list~~
~~and since the message header in a list should look similar to the message header of the message view it is not an option to use chips for viewing messages.~~

~~Reverted [commit](https://github.com/M66B/FairEmail/commit/2c80c25b8aa75af2287f471b882ec87d5a5a5015).~~

<br />

<a name="faq116"></a>
**~~(116) How can I show images in messages from trusted senders by default?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq116)

~~You can show images in messages from trusted senders by default by enabled the display setting *Automatically show images for known contacts*.~~

~~Contacts in the Android contacts list are considered to be known and trusted,~~
~~unless the contact is in the group / has the label '*Untrusted*' (case insensitive).~~

<br />

<a name="faq38"></a>
<a name="faq117"></a>
**(117) How can I restore a purchase (on another device) ?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq117)

Firstly, a purchase will be available on all devices logged into the same Google account,
*if* (this is important!) the app is installed via the same Google account too.
You can select the account in the Play store app by tapping on the avatar at the top right, but only *before* installing the app.
It is not possible to change the linked Google account after the app has been installed.
Google doesn't support moving a Play Store purchase from one to another account either.
This means you have to reinstall the app if it was installed under a different Google account.

Google manages all purchases, so as a developer I have little control over purchases.
So, basically, the only thing I can do, is suggest some things:

* Make sure you have an active, working internet connection, and turn off any VPN based app because it might prevent the Play store from checking purchases
* Make sure you are logged in with the right Google account and that there is nothing wrong with your Google account
* Make sure you installed FairEmail via the right Google account if you configured multiple Google accounts on your device (you might need to reinstall the app)
* Make sure the Play store app is up to date, please [see here](https://support.google.com/googleplay/answer/1050566?hl=en)
* Open the Play store app and wait at least a minute to give it time to synchronize with the Google servers
* Open FairEmail and navigate to the pro features screen to let FairEmail check the purchases; sometimes it helps to tap the *buy* button

You can also try to clear the cache of the Play store app via the Android apps settings.
Restarting the device might be necessary to let the Play store recognize the purchase correctly.

Note that:

* If you did not yet configure an email account in the app, you can long press *About* in the navigation menu (left side menu) of the settings screen to go to the pro features screen
* If you get *ITEM_ALREADY_OWNED*, the Play store app probably needs to be updated, please [see here](https://support.google.com/googleplay/answer/1050566?hl=en)
* If you get *BILLING_UNAVAILABLE Google Play In-app Billing API version is less than 3*, the Play store app might not be logged into the account used to install the app
* Purchases are stored in the Google cloud and cannot get lost
* There is no time limit on purchases, so they cannot expire
* Google does not expose details (name, e-mail, etc) about buyers to developers
* An app like FairEmail cannot select which Google account to use
* It may take a while until the Play store app has synchronized a purchase to another device
* Play Store purchases cannot be used without the Play Store app/services
* Play Store purchases cannot be transferred to another account
* You can't restore purchases with [microG](https://microg.org/)

Please [see here](https://support.google.com/googleplay/answer/4646404) about how to add, remove, or edit your Google Play payment method.

Sometimes the Play Store assigns a purchase incorrectly to another Google account configured on the device, resulting in the purchase not being recognized anymore.
Some people reported that force stopping the Play Store app / Google Play Services, clearing the data and the cache, and starting the Play Store again resolves this problem.

If you cannot restore a purchase,
please contact me via [this contact form](https://contact.faircode.eu/?product=fairemailsupport),
mentioning the email address of the Google account used for the purchase.

<br />

<a name="faq118"></a>
**(118) What does 'Remove tracking parameters' exactly?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq118)

Checking *Remove tracking parameters* will remove all [UTM parameters](https://en.wikipedia.org/wiki/UTM_parameters) from a link.

<br />

<a name="faq119"></a>
**~~(119) Can you add colors to the unified inbox widget?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq119)

~~The widget is designed to look good on most home/launcher screens by making it monochrome and by using a half transparent background.~~
~~This way the widget will nicely blend in, while still being properly readable.~~

~~Adding colors will cause problems with some backgrounds and will cause readability problems, which is why this won't be added.~~

Due to Android limitations it is not possible to dynamically set the opacity of the background and to have rounded corners at the same time.

<br />

<a name="faq120"></a>
**(120) Why are new message notifications not removed on opening the app?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq120)

New message notifications will be removed on swiping notifications away or on marking the associated messages read.
Opening the app will not remove new message notifications.
This gives you a choice to leave new message notifications as a reminder that there are still unread messages.

On Android 7 Nougat and later new message notifications will be [grouped](https://developer.android.com/training/notify-user/group).
Tapping on the summary notification will open the unified inbox.
The summary notification can be expanded to view individual new message notifications.
Tapping on an individual new message notification will open the conversation the message it is part of.
See [this FAQ](#faq70) about when messages in a conversation will be auto expanded and marked read.

<br />

<a name="faq121"></a>
**(121) How are messages grouped into a conversation?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq121)

By default FairEmail groups messages in conversations. This can be turned of in the display settings.

FairEmail groups messages based on the standard *Message-ID*, *In-Reply-To* and *References* headers.
FairEmail does not group on other criteria, like the subject,
because this could result in grouping unrelated messages and would be at the expense of increased battery usage.

<br />

<a name="faq122"></a>
**~~(122) Why is the recipient name/email address show with a warning color?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq122)

~~The recipient name and/or email address in the addresses section will be shown in a warning color~~
~~when the sender domain name and the domain name of the *to* address do not match.~~
~~Mostly this indicates that the message was received *via* an account with another email address.~~

<br />

<a name="faq123"></a>
**(123) What will happen when FairEmail cannot connect to an email server?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq123)

If FairEmail cannot connect to an email server to synchronize messages,
for example, if the internet connection is bad or a firewall or a VPN is blocking or aborting the connection,
FairEmail will retry one time after waiting 8 seconds while keeping the device awake (=use battery power).
If this fails, FairEmail will schedule an alarm to retry after 5, 15, 30 and eventually every 60 minutes and let the device sleep (=no battery usage).

By temporarily enabling debug mode in the miscellaneous settings, you can disable this logarithmic back-off scheme (since version 1.1855).
This will result in using a linear back-off scheme, which means that after each successive failure the waiting time will be
increased by 1 minute the first 5 minutes and thereafter by 5 minutes up to 60 minutes.
This might increase the battery usage significantly!

Note that [Android doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby)
does not allow to wake the device earlier than after 15 minutes when doze mode is active.

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq124)

The message *Message too large or too complex to display* will be shown if there are more than 100,000 characters or more than 500 links in a message.
Reformatting and displaying such messages will take too long. You can try to use the original message view, powered by the browser, instead.

<br />

<a name="faq125"></a>
**(125) What are the current experimental features?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq125)

Experimental features can be enabled in the miscellaneous-settings tab page.

<br />

*Send hard bounce (version 1.1477+)*

Send a [Delivery Status Notification](https://tools.ietf.org/html/rfc3464) (=hard bounce) via the reply/answer menu.

Hard bounces will mostly be processed automatically because they affect the reputation of the email provider.
The bounce address (=*Return-Path* header) is mostly very specific, so the email server can determine the sending account.

Some email servers, reportedly the Outlook.com email server, respond with a hard bounce to a hard bounce. In other words, hard bounces are being rejected.

For some background, see for [this Wikipedia article](https://en.wikipedia.org/wiki/Bounce_message).

<br />

*Resend (version 1.1788+)*

Resend a message as defined in [rfc2822 section 3.6.6](https://datatracker.ietf.org/doc/html/rfc2822#section-3.6.6) via the reply/answer menu.

To resend a message, the original message headers are required.
You can enable downloading of message headers for all messages in the connection settings of the app.
Alternatively, you can show messages headers via the horizontal three-dots menu just above the message text to download the message headers for a single message.
The resend menu item will be shown grayed (dimmed) if the message headers are not available (yet).

Remarks:

* Messages to, CC or BCC *undisclosed-recipients:* cannot be resent
* The original subject is sent as-is, unless it is being changed
* The original message text will be sent as-is, unless text is being entered
* The original attachments are sent as they are, unless attachments are being added or removed
* Default CC and BCC addresses will not be applied
* Read and delivery receipts will be requested when enabled, they could go to the original sender or to you
* The email server might refuse or incorrectly process resent messages
* DKIM, SPF and DMARC will likely fail, often causing resent messages to be considered as spam

<br />

*Process delivery/read receipt (version 1.1797+)*

On receiving a delivery or read receipt, the related message will be looked up in the sent messages folder
and the following keywords will be set depending on the contents of the report:

```
$Delivered
$NotDelivered
$Displayed
$NotDisplayed
```

* Delivered: action = *delivered*, *relayed*, or *expanded*, [see here](https://datatracker.ietf.org/doc/html/rfc3464#section-2.3.3)
* Displayed: disposition = *displayed*, [see here](https://datatracker.ietf.org/doc/html/rfc3798#section-3.2.6)

It is probably a good idea to enable *Show keywords in message header* in the display settings.

Note that the email server needs to support IMAP flags (keywords) for this feature.

Rules will be applied to the received receipt, so it is possible to move/archive the receipt.
See [this FAQ](#faq71) for a header condition to recognize receipts.

<br />

*Block toolbar (version 1.1967+)*

When enabled in the three-dots overflow menu of the message editor,
a toolbar to perform operations (align text, insert list, indent text, insert blockquote) on a block of text (consecutive non-empty lines) will be shown.

<br />

*Edit subject (1.2046+)*

The subject of a received message can be edited, also on the email server, via the horizontal three-dots button just above the message text near the left side of the screen.

<br />

*Markdown (1.2061+)*

Composing messages using [Markdown](https://en.wikipedia.org/wiki/Markdown) can be enabled via the three-dots overflow menu of the message editor.

<br />

*Fast forward to (1.2226+)*

Show (fast) *Forward to* in the answer menu, with addresses recently used for forwarding messages (if any).
You can show the answer menu by tapping on the answer button at the bottom right of an opened/expanded message.

<br />

*Force light for reformatted message view (1.2254+)*

Show force light menu item / button (when configured) to force a light theme for reformatted messages.

<br />

*Basic image editor (1.2257+)*

Display a basic image editor when tapping an inserted image.

<br />

<a name="faq126"></a>
**(126) Can message previews be sent to my smartwatch?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq126)

FairEmail fetches a message in two steps:

1. Fetch message headers
1. Fetch message text and attachments

Directly after the first step new messages will be notified.
However, only until after the second step the message text will be available.
FairEmail updates existing notifications with a preview of the message text, but unfortunately smartwatch notifications cannot be updated.

Since there is no guarantee that a message text will always be fetched directly after a message header,
it is not possible to guarantee that a new message notification with a preview text will always be sent to a smartwatch.

If you think this is good enough, you can enable the notification option *Only send notifications with a message preview to smartwatches*
and if this does not work, you can try to enable the notification option *Show notifications with a preview text only*.
Note that this applies to smartwatches not showing a preview text too, even when the Android Wear app says the notification has been sent (bridged).

If you want to have the full message text sent to your smartwatch, you can enable the notification option *Preview all text*.
Note that some smartwatches are known to crash with this option enabled.

If you use a Samsung smartwatch with the Galaxy smartwatch (Samsung Gear) app, you might need to enable notifications for FairEmail
when the setting *Notifications*, *Apps installed in the future* is turned off in this app.

Some companion apps ignore [local only](https://developer.android.com/training/wearables/notifications/bridger#non-bridged) notifications,
causing the summary notification (*nnn new messages*) to be bridged.
Unfortunately, it is not possible to workaround this problem.

Some smartwatches do not display notifications with non-[ASCII](https://en.wikipedia.org/wiki/ASCII) characters,
in which can you can enable the option *ASCII text only* in the display settings tab page.

Ongoing notifications shouldn't be bridged, but some companion apps bridge all notifications.
This results in the "monitoring" status bar notification to be bridged.
The workaround is to disable this notification, see [this FAQ](#faq2).

<br />

<a name="faq127"></a>
**(127) How can I fix 'Syntactically invalid HELO argument(s)'?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq127)

The error *... Syntactically invalid HELO argument(s) ...* means that the SMTP server rejected the local IP address or host name.
You can likely fix this error by enabling or disabling the advanced identity option *Use local IP address instead of host name*.

<br />

<a name="faq128"></a>
**(128) How can I reset asked questions, for example to show images?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq128)

You can reset asked questions via the three dots overflow menu in the miscellaneous settings.

<br />

<a name="faq129"></a>
**(129) Are ProtonMail, Tutanota, etc supported?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq129)

**ProtonMail** uses a proprietary email protocol
and [does not directly support IMAP](https://protonmail.com/support/knowledge-base/imap-smtp-and-pop3-setup/),
so you cannot use FairEmail or any other Android email client to access ProtonMail.
Unfortunately, Proton has no plans to add [a mail bridge for Android](https://github.com/ProtonMail/proton-bridge/issues/427).

**Tutanota** uses a proprietary email protocol
and [does not support IMAP](https://tutanota.com/faq/#imap),
so you cannot use FairEmail or any other email client to access Tutanota.

**Cyberfear** does not support IMAP, so you'll need to manually configure a POP3 account.

**Skiff** uses a proprietary email protocol
and [does not support IMAP](https://www.skiff.com/blog/tutanota-alternatives-comparison)
so you cannot use FairEmail or any other email client to access Skiff.

**Tildamail** uses a proprietary email protocol and does not support IMAP,
so you cannot use FairEmail or any other email client to access Tildamail.

**Criptext** uses a proprietary email protocol
and [does not directly support IMAP](https://www.reddit.com/r/privacy/comments/chs82k/comment/ewrxxcn/),
so you cannot use FairEmail or any other email email client to access Criptext.

**OnMail** uses a proprietary email protocol
and [does not support IMAP](https://support.onmail.com/hc/en-us/articles/360048879012-How-do-I-connect-my-OnMail-address-to-a-third-party-email-app-),
so you cannot use FairEmail or any other email client to access OnMail, except for one (but please read the privacy policy carefully).

<br />

<a name="faq130"></a>
**(130) What does message error ... mean?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq130)

If you don't understand an error, please [contact me](https://contact.faircode.eu/?product=fairemailsupport) for support.

The warning *No server found at ...* means that there was no email server registered at the indicated domain name.
Replying to the message might not be possible and might result in an error.
This could indicate a falsified email address and/or spam.

The error *... ParseException ...* means that there is a problem with a received message, likely caused by a bug in the sending software.
FairEmail will workaround this is in most cases, so this message can mostly be considered as a warning instead of an error.

The error *...SendFailedException...* means that there was a problem while sending a message.
The error will almost always include a reason. Common reasons are that the message was too big or that one or more recipient addresses were invalid.

The warning *Message too large to fit into the available memory* means that the message was larger than 10 MiB.
Even if your device has plenty of storage space Android provides limited working memory to apps, which limits the size of messages that can be handled.

Please see [here](#faq22) for other error messages.

<br />

<a name="faq131"></a>
**(131) Can you change the direction for swiping to previous/next message?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq131)

If you read from left to right, swiping to the left will show the next message.
Similarly, if you read from right to left, swiping to the right will show the next message.

This behavior seems quite natural to me, also because it is similar to turning pages.

Anyway, there is a behavior setting to reverse the swipe direction.

<br />

<a name="faq132"></a>
**(132) Why are new message notifications silent?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq132)

Notifications are silent by default on some MIUI versions.
Please see [here](http://en.miui.com/thread-3930694-1-1.html) how you can fix this.

There is a bug in some Android versions
causing [setOnlyAlertOnce](https://developer.android.com/reference/android/app/Notification.Builder#setOnlyAlertOnce(boolean)) to mute notifications.
Since FairEmail shows new message notifications right after fetching the message headers
and FairEmail needs to update new message notifications after fetching the message text later, this cannot be fixed or worked around by FairEmail.

Android might rate limit the notification sound, which can cause some new message notifications to be silent.

<br />

<a name="faq133"></a>
**(133) Why is ActiveSync &trade; not supported?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq133)

The Microsoft Exchange ActiveSync &trade; protocol [is patented](https://en.wikipedia.org/wiki/Exchange_ActiveSync#Licensing) and Microsoft doesn't give out licenses anymore.
Therefore, it isn't possible to support ActiveSync &trade; anymore.
For this same reason, you won't find many, if any, other email clients supporting ActiveSync &trade;.

Note that the desciption of FairEmail starts with the remark
that non-standard protocols, like Microsoft Exchange Web Services &trade; and Microsoft ActiveSync &trade; are not supported.

<br />

<a name="faq134"></a>
**(134) Can you add leave messages on the server?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq134)

*POP3*

In the account settings (Settings, tap Manual setup, tap Accounts, tap account) you can enable *Leave messages on server* and *Leave deleted messages on server*.

*IMAP*

Since the IMAP protocol is meant to synchronize two ways, from server to client and from client to server,
trashing (=move to trash) and (permanently) deleting messages will be performed on the email server too,
so that the message list is the same for all connected clients.
Deleting a message from the device only won't work because it would be synchronized again later.

Some email apps pretend they can do this, but, in fact, messages are hidden on the device.
FairEmail can do this too, but in a more explicit way, also allowing you to show the messages again.

You can hide messages either via the three-dots menu in the action bar just above the message text (you can configure a button for this via the same menu)
or by multiple selecting messages in the message list.

Alternatively, you can disable AUTO EXPUNGE, which will result in marking messages being deleted on the server (by setting the *deleted* flag), but not expunging (deleting) them.
You can find this option in the debug panel, which can be shown by (temporarily) enabling debug mode in the miscellaneous settings.
Note that with AUTO EXPUNGE disabled, all messages in all folders of all accounts will remain on the email server (with the deleted flag set),
also in the draft-messages folder and when emptying the trash-messages or spam-messages folder.
You'll need to manually perform the EXPUNGE command, which you can do by long pressing on a folder, or via the three-dots overflow menu of a messages list.

Note that it is possible to set the swipe left or right action to hide a message.
There is a button in the behavior settings to quickly configure the swipe left and right actions for all IMAP accounts.

<br />

<a name="faq135"></a>
**(135) Why are trashed messages and drafts shown in conversations?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq135)

Individual messages will rarely be trashed and mostly this happens by accident.
Showing trashed messages in conversations makes it easier to find them back.

You can permanently delete a message using the message three-dots *delete* menu, which will remove the message from the conversation.
Note that this is irreversible.

Similarly, drafts are shown in conversations to find them back in the context where they belong.
It is easy to read through the received messages before continuing to write the draft later.

<br />

<a name="faq136"></a>
**(136) How can I delete an account/identity/folder?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq136)

Deleting an account/identity/folder is a little bit hidden to prevent accidents.

* Account: Settings > Manual setup > Accounts > tap account
* Identity: Settings > Manual setup > Identities > tap identity
* Folder: Long press the folder in the folder list > Edit properties

In the three-dots overflow menu at the top right there is an item to delete the account/identity/folder.

<br />

<a name="faq137"></a>
**(137) How can I reset 'Don't ask again'?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq137)

You can reset all questions set to be not asked again in the miscellaneous settings.

<br />

<a name="faq138"></a>
**(138) Can you add calendar/contact/tasks/notes management?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq138)

Calendar, contact, task and note management can better be done by a separate, specialized app.
Note that FairEmail is a specialized email app, not an office suite.

Also, I prefer to do a few things very well, instead of many things only half.
Moreover, from a security perspective, it is not a good idea to grant many permissions to a single app.

You are advised to use the excellent, open source [DAVx⁵](https://f-droid.org/packages/at.bitfire.davdroid/) app to synchronize/manage your calendars/contacts.

If you want to synchronize Outlook contacts and you have access to Google Workspace,
please [see here](https://support.google.com/a/users/answer/156595) about how you can set up contact syncing.

Most providers support exporting your contacts.
Please [see here](https://support.google.com/contacts/answer/1069522) about how you can import contacts if synchronizing is not possible.

Note that FairEmail does support replying to calendar invites (a pro feature) and adding calendar invites to your personal calendar.

<br />

<a name="faq83"></a>
<a name="faq139"></a>
**(139) How do I fix 'User is authenticated but not connected'?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq139)

The confusing Microsoft specific server error *User is authenticated but not connected* might occur if:

**Consumer Outlook/Hotmail/Live account**

* IMAP is disabled, which is the default for new Outlook accounts now

To fix this:

* Go to the [Outlook website](https://outlook.live.com/)
* Tap on the settings wheel at the top right
* Select '*Mail*'
* Select '*Forwarding and IMAP*'
* In the section '*POP and IMAP*' enable '*Let devices and apps use IMAP*'

<br>

**Corporate, education, etc. account**

* External access is administratively disabled, please see [this article](https://docs.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/client-access-rules/client-access-rules) about how an administrator can enable it again
* Access by third-party apps is administratively disabled or allowed for specific apps only
* IMAP is administratively disabled, please see [this article](https://learn.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/pop3-and-imap4/enable-or-disable-pop3-or-imap4-access) about how an administrator can enable it again
* SMTP is administratively disabled, please see [this article](https://docs.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/authenticated-client-smtp-submission) about how an administrator can enable it again
* A security policy is blocking the login, for example because only specific network connections are allowed, please see [this article](https://learn.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/client-access-rules/client-access-rules) about how an administrator can enable it again
* Required server components are disabled, please see [this article](https://learn.microsoft.com/en-us/exchange/troubleshoot/user-and-shared-mailboxes/pop3-imap-owa-activesync-office-365) about enabling IMAP, MAPI, etc.
* Push messages are enabled for too many folders: see [this FAQ](#faq23) for more information and a workaround
* There were too many login attempts in a too short time, for example by using multiple email clients at the same time
* The wrong account was selected in the Microsoft account selector, for example an account with a different email address or a personal instead of a business account
* An ad blocker or DNS changer is being used
* Devices in another time zone are connected to the same account
* There is a problem with the Exchange server license: it might be expired or for another server edition
* An alias email address is being used as username instead of the primary email address
* An incorrect login scheme is being used for a shared mailbox: the right scheme is *username@domain\SharedMailboxAlias*

The shared mailbox alias will mostly be the email address of the shared account, like this:

```
you@example.com\shared@example.com
```

Note that it should be a backslash and not a forward slash.

The Outlook/Office 365 quick setup wizard supports setting up shared mailboxes.

When using a shared mailbox, you might want to enable the option *Synchronize shared folder lists* in the receive settings.

Sometimes it helps to use the *Other provider* wizard instead of the *Outlook/Office 365* wizard.
You might need an app password for this, please see [this FAQ](#faq14).

Background: this error happens if logging in to an account succeeded (with OAuth), but logging in to the email (IMAP/SMTP) server fails for some reason.

<br />

<a name="faq140"></a>
**(140) Why does the message text contain strange characters?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq140)

Displaying strange characters is almost always caused by specifying no or an invalid character encoding by the sending software.
FairEmail will assume [ISO 8859-1](https://en.wikipedia.org/wiki/ISO/IEC_8859-1)
when no character set or when [US-ASCII](https://en.wikipedia.org/wiki/ASCII) was specified.
Other than that there is no way to reliably determine the correct character encoding automatically,
so this cannot be fixed by FairEmail. The right action is to complain to the sender.

<br />

<a name="faq141"></a>
**(141) How can I fix 'A drafts folder is required to send messages'?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq141)

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq142)

Generally, it is not a good idea to store sent messages in the inbox because this is hard to undo and could be incompatible with other email clients.

That said, FairEmail is able to properly handle sent messages in the inbox.
FairEmail will mark outgoing messages with a sent messages icon for example.

The best solution would be to enable showing the sent folder in the unified inbox
by long pressing the sent folder in the folder list and enabling *Show in unified inbox*.
This way all messages can stay where they belong, while allowing to see both incoming and outgoing messages at one place.

If this is not an option, you can [create a rule](#faq71) to automatically move sent messages to the inbox
or set a default CC or BCC address in the advanced identity settings (via the manual setup in the main setup screen) to send yourself a copy.

<br />

<a name="faq143"></a>
**~~(143) Can you add a trash folder for POP3 accounts?~~**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq143)

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq144)

To record voice notes you can press this icon in the bottom action bar of the message composer:

<img alt="Record image" src="https://raw.githubusercontent.com/M66B/FairEmail/master/images/baseline_record_voice_over_black_48dp.png" width="48" height="48" />

This requires a compatible audio recorder app to be installed.
In particular [this common intent](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION)
needs to be supported.

For example [this audio recorder](https://f-droid.org/app/com.github.axet.audiorecorder) is compatible.

Voice notes will automatically be attached.

<br />

<a name="faq145"></a>
**(145) How can I set a notification sound for an account, folder, sender or condition?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq145)

Account:

* ~~Version 1.1927-: enable *Separate notifications* in the advanced account settings~~
* Version 1.1927+: long press the account in the account list and select *Create notification channel*
* Long press the account in the account list and select *Edit notification channel* to change the notification sound

To go to the account list: navigation menu (left side menu), tap *Settings*, tap *Manual setup and account options* and tap *Accounts*.<br>
To go to the advanced account settings from the account list: tap on the account and tap on *Advanced*.

Folder:

* Long press the folder in the folder list and select *Notify on new messages*
* Long press the folder in the folder list and select *Create notification channel*
* Long press the folder in the folder list and select *Edit notification channel* to change the notification sound

To go to the folder list: tap on the account name in the navigation menu (left side menu).

Sender:

* Open a message from the sender and expand it
* Expand the addresses section by tapping on the down arrow
* Tap on the bell icon to create or edit a notification channel and to change the notification sound

Conditional: (since version 1.1803; experimental)

* Long press the folder (inbox) in the folder list and select *Edit rules*
* Add a rule with the big 'plus' button at the bottom right
* Configure a rule condition, select *Play sound* as rule action and select a sound
* For more information about rules, please [see here](#faq71)

The order of precendence is: conditional sound, sender sound, folder sound, account sound and (default) notification sound.

Setting a notification sound for an account, folder or sender requires Android 8 Oreo or later and is a pro feature.

<br />

<a name="faq146"></a>
**(146) How can I fix incorrect message times?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq146)

Since the sent date/time is optional and can be manipulated by the sender, FairEmail uses the server received date/time by default.

Sometimes the server received date/time is incorrect,
mostly because messages were incorrectly imported from another server and sometimes due to a bug in the email server.

In these rare cases, it is possible to let FairEmail use either the date/time from the *Date* header (sent time) or from the *Received* header as a workaround.
This can be changed in the advanced account settings: go to the Settings, tap *Manual setup and account options*, tap *Accounts*, tap the account, tap *Advanced*.

This will not change the time of already synchronized messages.
To solve this, long press the folder(s) in the folder list and select *Delete local messages* and *Synchronize now*.

<br />

<a name="faq147"></a>
**(147) What should I know about third party versions?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq147)

You likely came here because you are using a third party build of FairEmail.

There is **only support** on the latest Play store version, the latest GitHub release and
the F-Droid build, but **only if** the version number of the F-Droid build is the same as the version number of the latest GitHub release.

F-Droid builds irregularly, which can be problematic if there is an important update.
Therefore you are advised to switch to the GitHub release.
F-Droid isn't as secure as you might think anyway, [see here](https://privsec.dev/posts/android/f-droid-security-issues/).

Note that developers have no control over F-Droid builds and the F-Droid infrastructure (apps, forums, etc.).

OAuth access is available only for Play Store and Github releases
because email providers permitted the use of OAuth for these releases only.
The responsible for a release, for the F-Droid build this is the F-Droid organization, needs to ask for OAuth permissions,
which mostly involves signing a contract with binding terms and conditions,
often with the clause that the use of OAuth is exclusive.

Note that you'll need to uninstall the F-Droid build first before you can install a GitHub release
because Android refuses to install ("*App was not installed*") the same app with a different signature for security reasons.

At the start of 2024, the Play Store app started to update all apps, including apps not installed via the Play Store.
Since the F-Droid build is signed by the F-Droid organization, which basically means the F-Droid build is another app, updating the F-Droid build will fail.
Unfortunately, there is no way to resolve this.

Note that the GitHub version will automatically check for updates.
When desired, this can be turned off in the miscellaneous settings.

Please [see here](https://github.com/M66B/FairEmail/blob/master/README.md#downloads) for all download option
and [see here](#faq173) for the differences between the different release types.

If you have a problem with the F-Droid build, please check if there is a newer GitHub version first.

You can see the source of the app in *About* of the navigation menu (left side menu),
either *Play store*, *GitHub*, *F-Droid*, or *?* (for example in the case of a custom build).

[IzzyOnDroid](https://apt.izzysoft.de/fdroid/) hosts the GitHub release of the app.
[Aurora Store](https://f-droid.org/packages/com.aurora.store/) hosts the Play store version of the app,
even though the Aurora Store app was downloaded from F-Droid.

Please [see here](https://forum.f-droid.org/t/help-wanted-how-to-create-a-reproducible-build-fairemail/8860) why reproducible F-Droid builds are not an option.
Instead, you can check the integrity of an APK file produced by a GitHub workflow, [see here](#faq205) on how.

<br />

<a name="faq148"></a>
**(148) How can I use an Apple iCloud account?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq148)

There is a built-in profile for Apple iCloud, so you should be able to use the quick setup wizard (other provider).
If needed you can find the right settings [here](https://support.apple.com/en-us/HT202304) to manually set up an account.

When using two-factor authentication you might need to use an [app-specific password](https://support.apple.com/en-us/HT204397).

Please make sure you use the main email address and not an alias address.
It is not possible to authenticate an iCloud account with an alias address.
If you want to create an alias address, first configure the main account, and after that see [this FAQ](#faq9).

<br />

<a name="faq149"></a>
**(149) How does the unread message count widget work?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq149)

The unread message count widget shows the number of unread messages either for all accounts or for a selected account,
but only for the folders for which new message notifications are enabled.

Tapping on the notification will synchronize all folders for which synchronization is enabled and will open:

* the start screen when all accounts were selected
* a folder list when a specific account was selected and when new message notifications are enabled for multiple folders
* a list of messages when a specific account was selected and when new message notifications are enabled for one folder

<br />

<a name="faq150"></a>
**(150) Can you add cancelling calendar invites?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq150)

Cancelling calendar invites (removing calendar events) requires write calendar permission,
which will result in effectively granting permission to read and write *all* calendar events of *all* calendars.

Given the goal of FairEmail, privacy and security, and given that it is easy to remove a calendar event manually,
it is not a good idea to request this permission for just this reason.

Inserting new calendar events can be done without permissions with special [intents](https://developer.android.com/guide/topics/providers/calendar-provider.html#intents).
Unfortunately, there exists no intent to delete existing calendar events.

<br />

<a name="faq151"></a>
**(151) Can you add backup/restore of messages?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq151)

An email client is meant to read and write messages, not to back up and restore messages.
In other words, an email client is a viewer for messages on an email server, and not a backup tool.
Instead, the email provider/server is responsible for backups.

If you want to make a backup yourself, you could use a tool like [imapsync](https://imapsync.lamiral.info/).

Since version 1.1556 it is possible to export all messages of a POP3 folder in mbox format according to [RFC4155](https://www.ietf.org/rfc/rfc4155.txt),
which might be useful to backup sent messages if the email server doesn't (which is risky because breaking or losing your device, means losing your sent messages!).
For this, please long press on the folder in the folder list of an account (tap on the account name in the navigation menu).

Since version 1.2160 it is possible to import messages in an mbox file conforming to [RFC4155](https://www.ietf.org/rfc/rfc4155.txt) into a POP3 folder.
Note that imported messages won't be uploaded to the email server because this is not possible with POP3.

If you want to import an mbox file into an existing email account,
you can use Thunderbird on a desktop computer and the [ImportExportTools NG](https://addons.thunderbird.net/de/thunderbird/addon/importexporttools-ng/) add-on.

Note that in case of IMAP, all messages on your device are also on the email server.

<br />

<a name="faq152"></a>
**(152) How can I insert a contact group?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq152)

You can insert the email addresses of all contacts in a contact group via the three dots menu of the message composer.
You can also long press the person-add icon at the end of the to/cc/bcc/field.

You can define contact groups with the Android contacts app, please see [here](https://support.google.com/contacts/answer/30970) for instructions.

<br />

<a name="faq153"></a>
**(153) Why does permanently deleting Gmail message not work?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq153)

~~You might need to change [the Gmail IMAP settings](https://mail.google.com/mail/u/0/#settings/fwdandpop) on a desktop browser to make it work:~~

* ~~When I mark a message in IMAP as deleted: Auto-Expunge off - Wait for the client to update the server.~~
* ~~When a message is marked as deleted and expunged from the last visible IMAP folder: Immediately delete the message forever~~

~~Note that archived messages can be deleted only by moving them to the trash folder first.~~

~~Some background: Gmail seems to have an additional message view for IMAP, which can be different from the main message view.~~

Unfortunately, the above doesn't work anymore.

Since version 1.2216 there is an option *When permanently deleting a message, delete all Gmail labels* in the behavior-settings tab page,
which will result in permanently deleting a message from all folders (=labels), including the archive (=all messages) folder.

<br />

Another oddity is that a star (favorite message) set via the web interface cannot be removed with the IMAP command

```
STORE <message number> -FLAGS (\Flagged)
```

On the other hand, a star set via IMAP is being shown in the web interface and can be removed via IMAP.

<br />

<a name="faq154"></a>
**(154) Is there support for favicons as contact photos?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq154)

Yes, favicons can be enabled in the display-settings tab page.

To reduce privacy concerns, the app fetches favicons directly from the internet using the domain name of email addresses.
In other words, no third-party service is used.

If no favicon is shown for an email address, please check for favicons via [this service](https://realfavicongenerator.net/),
and if there are any, please [contact me](https://contact.faircode.eu/?product=fairemailsupport).

Since version 1.2210 it is also possible to use DuckDuckGo's icon service (GitHub version only).
There are concerns about [privacy](https://github.com/duckduckgo/Android/issues/527).

Since version 1.2213 it is possible to configure an alternative URI to fetch favicons in the debug options
by temporarily enabling debug mode in the miscellaneous-settings tab page.
For example:


```
https://icons.duckduckgo.com/ip3/{domain}.ico
https://www.google.com/s2/favicons?sz=128&domain={domain}
https://favicon.yandex.net/favicon/{domain}
```

<br />

<a name="faq155"></a>
**(155) What is a winmail.dat file?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq155)

A *winmail.dat* file is sent by an incorrectly configured Outlook client.
It is a Microsoft specific file format ([TNEF](https://en.wikipedia.org/wiki/Transport_Neutral_Encapsulation_Format)) containing a message and possibly attachments.

You can find some more information about this file [here](https://support.mozilla.org/en-US/kb/what-winmaildat-attachment).

FairEmail has limited support for this file type.

You can view it with for example the Android app [Letter Opener](https://play.google.com/store/apps/details?id=app.letteropener).

<br />

<a name="faq156"></a>
**(156) How can I set up an Office 365 account?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq156)

An Office 365 account can be set up via the quick setup wizard and selecting *Office 365 (OAuth)*.

If the wizard ends with *AUTHENTICATE failed*, IMAP and/or SMTP might be disabled for the account.
In this case you should ask the administrator to enable IMAP and SMTP.
The procedure is documented [here](https://docs.microsoft.com/en-in/exchange/troubleshoot/configure-mailboxes/pop3-imap-owa-activesync-office-365).

If you've enabled *security defaults* in your organization, you might need to enable the SMTP AUTH protocol.
Please [see here](https://docs.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/authenticated-client-smtp-submission) about how to.

<br />

<a name="faq157"></a>
**(157) How can I set up an Free.fr account?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq157)

Veuillez [voir ici](https://free.fr/assistance/597.html) pour les instructions.

**SMTP est désactivé par défaut**, veuillez [voir ici](https://free.fr/assistance/2406.html) comment il peut être activé.

Veuillez [voir ici](http://jc.etiemble.free.fr/abc/index.php/trucs-astuces/configurer-smtp-free-fr) pour un guide détaillé.

<br />

<a name="faq103"></a>
<a name="faq158"></a>
**(158) Which camera / audio recorder do you recommend?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq158)

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq159)

Please see [here](https://disconnect.me/trackerprotection) for more information about Disconnect's tracker protection lists.
For the last updates, please [see here](https://github.com/disconnectme/disconnect-tracking-protection/commits).

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

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq160)

Permanent deletion means that messages will *irreversibly* be lost, and to prevent this from happening accidentally, this always needs to be confirmed.
Even with a confirmation, some very angry people who lost some of their messages through their own fault contacted me, which was a rather unpleasant experience :-(

Please [see here](https://forum.xda-developers.com/t/app-5-0-fairemail-fully-featured-open-source-privacy-oriented-email-app.3824168/post-86984471) for more background information.

Since version 1.1601 it is possible to disable confirmation of permanent deletion of individual messages after reading a big warning.

Note that the POP3 protocol can download messages from the inbox only.
So, deleted messages cannot be uploaded to the inbox again.
This means that messages can only be permanently deleted in case of a POP3 account.

Advanced: the IMAP delete flag in combination with the EXPUNGE command is not supportable
because both email servers and not all people can handle this, risking unexpected loss of messages.
A complicating factor is that not all email servers support [UID EXPUNGE](https://tools.ietf.org/html/rfc4315).

Since version 1.1485 it is possible to temporarily enable debug mode in the miscellaneous settings to disable auto expunging messages.
Note that messages with a *\Deleted* flag will not be shown if AUTO EXPUNGE is enabled.

In the debug panel, it is also possible to disable permanent delete confirmation and enable permanent delete from notifications (since version 1.2163).

<br />

<a name="faq161"></a>
**(161) Can you add a setting to change the primary and accent color?***

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq161)

If I could, I would add a setting to select the primary and accent color right away,
but unfortunately Android themes are fixed, see for example [here](https://stackoverflow.com/a/26511725/1794097), so this is not possible.

<br />

<a name="faq162"></a>
**(162) Is IMAP NOTIFY supported?***

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq162)

Yes, [IMAP NOTIFY](https://tools.ietf.org/html/rfc5465) has been supported since version 1.1413.

IMAP NOTIFY support means that notifications for added, changed or deleted messages of all *subscribed* folders will be requested
and if a notification is received for a subscribed folder, that the folder will be synchronized.
Synchronization for subscribed folders can therefore be disable, saving folder connections to the email server.

**Important**: push messages (=always sync) for the inbox and subscription management (receive settings) need to be enabled.

**Important**: most email servers do not support this! You can check the log via the navigation menu if an email server supports the NOTIFY capability.

<br />

<a name="faq163"></a>
**(163) What is message classification?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq163)

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
See also [this FAQ](#faq92).

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

If all previously classified messages were the same and the message being evaluated is also the same, the confidence would be 100%.

Both conditions must be satisfied before a message will be moved.

Considering the default option values:

* Apples 40 % and bananas 30 % would be disregarded because the difference of 25 % is below the minimum of 50 %
* Apples 10 % and bananas 5 % would be disregarded because the probability for apples is below the minimum of 15 %
* Apples 50 % and bananas 20 % would result in selecting apples

Classification is optimized to use as little resources as possible, but will inevitably use some extra battery power.
This is also why only folders of the same account will be considered.

You can delete all classification data by turning classification in the miscellaneous settings three times off.
This will be necessary when classification for a folder is enabled or disabled (or when a folder is deleted)
because classification is based on comparision.

[Rules](#faq71) will be executed before classification.
If one or more rules were executed for a message, message classification will be skipped
because it is assumed that the message will be processed by the rules in this case.

Message classification is a pro feature, except for the spam folder.

<br />

<a name="faq164"></a>
**(164) Can you add customizable themes?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq164)

Unfortunately, Android [does not support](https://stackoverflow.com/a/26511725/1794097) dynamic themes,
which means all themes need [to be predefined](https://github.com/M66B/FairEmail/blob/master/app/src/main/res/values/styles.xml).

Since for each theme there needs to be a light, dark and black variant,
it is not feasible to add for each color combination (literally millions) a predefined theme.

Moreover, a theme is more than just a few colors.
For example themes with a yellow accent color use a darker link color for enough contrast.

Most people like the beige background for the light themes, but if you don't like it, it can be disabled in the display settings.

The [Material You](https://material.io/blog/announcing-material-you) theme,
a more dynamic theme based on the selected background image ("Monet"),
which was introduced in Android 12 on Google Pixel devices,
is supported and can be selected in the theme selection dialog (via the three-dots menu of the start screen).

The theme colors are based on the color circle of [Johannes Itten](https://en.wikipedia.org/wiki/Johannes_Itten).

The *Solarized* theme is described in [this article](https://en.wikipedia.org/wiki/Solarized_(color_scheme)).

The beige background is in fact [Cosmic Latte](https://en.wikipedia.org/wiki/Cosmic_latte),
the average color of the galaxies of the universe as perceived from the Earth

<br />

<a name="faq165"></a>
**(165) Is Android Auto supported?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq165)

Yes, Android Auto is supported, but only with the GitHub version of the app
because Google rejected the application of FairEmail for this reason:

*App category not permitted -
At this time, we are only accepting apps within the Media, short form Messaging,
or categories supported by the Android for Cars App Library.*

Also, not all Android Auto versions support this (as installed in the car).

**Use of this feature with the Github version is expressly at your own risk!**

For notification (messaging) support you'll need to enable the following notification options:

* *Use Android 'messaging style' notification format*
* Notification actions: *Direct reply* and (mark as) *Read* (this might be optional now, but this is untested)

You can enable other notification actions too, if you like, but they are not supported by Android Auto.

The developers guide is [here](https://developer.android.com/training/cars/messaging).

<br />

<a name="faq166"></a>
**(166) Can I snooze a message across multiple devices?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq166)

First of all, there is no standard for snoozing messages, so all snooze implementations are custom solutions.

Some email providers, like Gmail, move snoozed messages to a special folder.
Unfortunately, third party apps have no access to this special folder.

Moving a message to another folder and back might fail and might not be possible if there is no internet connection.
This is problematic because a message can be snoozed only after moving the message.

To prevent these issues, snoozing is done locally on the device by hiding the message while it is snoozing.
Unfortunately, it is not possible to hide messages on the email server too.
Deleting messages from the server and restoring them later could result in losing messages when the device breaks.

<br />

<a name="faq167"></a>
**(167) How can I use DeepL?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq167)

DeepL offers free translation of 500,000 characters (~100,000 words; ~250 pages) every month.

1. Make sure you have the latest version of the app installed
1. Check if [DeepL](https://www.deepl.com/) supports your language
1. Enable DeepL support in the integration settings
1. [Sign up](https://www.deepl.com/en/signup) to use [DeepL API Free](https://www.deepl.com/en/pro-api) "*Get started for free*" (credit card required for verification; won't be charged)
1. [Copy](https://www.deepl.com/en/your-account/keys) the authentication key
1. In the message composer tap on the translate button (文A) in the top action bar, select *Configure* and paste the key

Note that DeepL seems to be hiding the DeepL API Free plan. It is still there, though!

This feature requires an internet connection.

Note that you can't use [the regular pro plans](https://www.deepl.com/pro).
The error *403 forbidden* means that the key and/or plan in invalid.

Note that when reading a message, you can use the horizontal three-dots menu to translate too.
If you use this frequently, you can configure a button for this.

Some people have asked to add Google Translate on the assumption that it is free to use, but that is not the case if it is integrated into an app.
Apart from that, DeepL is much easier to configure and the translations are of better quality.

<br />

<a name="faq168"></a>
**(168) What is a spam block list?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq168)

A spam block list is basically a list of domain names which have been used to send spam or to spread malware.

A spam block list is checked by resolving the server name from the last *Received* header into an IP address
and looking up the IP address with a DNS request:

```
Received:
	... from smtp.gmail.com ...
smtp.gmail.com ->
	142.250.27.108
DNS lookup 108.27.250.142.zen.spamhaus.org ->
	127.0.0.2: spam
	NXDOMAIN: not spam
```

NXDOMAIN = no such domain

For more information, please see [this article](https://en.wikipedia.org/wiki/Domain_Name_System-based_blackhole_list).

You can check common block lists for example [here](https://mxtoolbox.com/blacklists.aspx).

FairEmail currently uses the following block lists:

* [Spamhaus](https://www.spamhaus.org/) &#8211; [Terms of Use](https://www.spamhaus.org/organization/dnsblusage/) &#8212; [Privacy policy](https://www.spamhaus.org/organization/privacy/)
* [Spamcop](https://www.spamcop.net/) &#8211; [Legal info](https://www.spamcop.net/fom-serve/cache/297.html) &#8212; [Privacy policy](https://www.spamcop.net/fom-serve/cache/168.html)
* [Barracuda](https://www.barracudacentral.org/rbl/how-to-use) &#8211; [Request Access](https://www.barracudacentral.org/account/register) &#8211; [Privacy policy](https://www.barracuda.com/company/legal/trust-center/data-privacy/privacy-policy)

Since version 1.1627 it is possible to enable/disable individual blocklists in the receive settings of the app.

<br />

<a name="faq169"></a>
**(169) Why does the app not start automatically?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq169)

FairEmail requests Android to start the app when the device starts up.
Obviously, this depends on Android as the app cannot start itself.

Some Android versions, such as EMUI, have settings to enable or disable auto starting apps.
So, if the app isn't started automatically, please check the Android settings.

For example for Huawei/EMUI, please [see here](https://dontkillmyapp.com/huawei) for a guide.

<br>

<a name="faq170"></a>
**(170) Why can't folders be created with POP3?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq170)

The POP3 protocol has commands to download and delete messages from the inbox only.
There are no commands to access any other folder.

Creating a local folder only and storing messages on your device only is a bad idea
because losing or breaking your device would mean losing your messages.
Imagine you've put a lot of time organizing your messages, and you need to reinstall the app on another device, only to discover all the work you did was a waste of time.
Therefore, this isn't supportable.

Please note that FairEmail doesn't store a copy of your messages in the cloud for privacy reasons.

If you want to store messages in folders, please use IMAP,
so that there is always a copy of the messages on the email server.

You could register, for example, a Gmail account, and configure Gmail to import messages from a POP3 account,
automatically label them (=put them in a folder), and configure the Gmail account in FairEmail.
Please [see here](https://support.google.com/mail/answer/21289?hl=en&co=GENIE.Platform%3DDesktop) for instructions.

If you are concerned about deleting messages, with IMAP messages are moved to the trash messages folder first, which means that deleted messages can be restored.
You can also hide instead of delete messages, please [see this FAQ](#faq134).

Some people believe POP3 is safer than IMAP, but security wise, there is no difference between POP3 and IMAP.
The connection to the email server is always encrypted, and the account is protected by a password in both cases.

If your email provider only offers POP3 access, tell them the POP3 protocol is 40+ years old ([defined in 1984](https://datatracker.ietf.org/doc/html/rfc918)).

<br />

<a name="faq171"></a>
**(171) How can I delete a contact?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq171)

A contact can be stored in the Android address book or in the local contact database.

When a contact is stored in the Android address book, you can use the Android contacts app to delete the contact.

When a contact is stored in the local contact database, you can delete it like this:

* Go to the settings via the navigation menu (left side menu)
* Go to the send settings tab page
* Tap on the *Manage* button under *Suggest locally stored contacts*
* Locate the contact (you can use the magnifier glass in the top action bar to search for it)
* Long press the contact and select to delete it

<br />

<a name="faq172"></a>
**(172) How can I import contacts?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq172)

Like most Android apps, FairEmail consults the Android address book for contact information.

There is also a local contact database, which is filled with contacts from sent and received messages.
You can enable/disable this in the send settings of the app.

If you want to import contacts into the local contact database,
this is possible (in recent versions of the app) by tapping on the *Manage* button in the send settings.
In the three-dots menu at the top right there is an import (and also an export) [vCard](https://en.wikipedia.org/wiki/VCard)s menu item.

The Android address book is managed by the Android Contacts app (or a replacement for this app).
Please see [this article](https://support.google.com/contacts/answer/1069522) about importing contacts to the Android address book.

Note that some vendors, notably Microsoft, try to lock you into their contact (data) storage.
This is not how it should work on Android, which is more open to sharing information.

Related questions:

* [What are local contacts for?](#faq84)
* [Why can I still pick contacts after revoking contacts permissions?](#faq98)
* [Can you add calendar/contact/tasks/notes management?](#faq138)
* [How can I insert a contact group?](#faq152)
* [How can I delete a contact?](#faq171)

<br />

<a name="faq173"></a>
**(173) What is the difference between Play store / GitHub / F-Droid version?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq173)

* The Play Store version does not support Android Auto, see [this FAQ](#faq165) for more information
* The Play Store version does not support Amazon devices with Android 5 Lollipop because there are critical bugs in this Android version of Amazon
* The Play Store version does not support Gravatars/Libravatars/BIMI due to Play Store policies
* The Play Store version does not support auto-storing iCalendar invitations, see [this FAQ](#faq186) for more information
* The Play Store version is released about once a month only because I am tired of 1-star ratings for *Too many updates*. If you want to receive more updates, you can join the [Play Store test program](https://play.google.com/apps/testing/eu.faircode.email).
* The GitHub version will check for [updates on GitHub](https://github.com/M66B/FairEmail/releases) and is updated more frequently, but updates need to be installed manually
* The GitHub version has some different links, some more features and options, and some different default values (more geared to advanced users)
* The GitHub version can be installed as an update over the Play store version, whereas the F-Droid build can't (see below for more details)
* The F-Droid build does not support OAuth, see [this FAQ](#faq147) about why not
* The F-Droid build does not include [Google Play Billing](https://developer.android.com/google/play/billing/integrate), so Play store purchases cannot be reused
* The F-Droid build is supported only if the version number is the same as the version number of the latest GitHub version, see also [this FAQ](#faq147)

The Play store and GitHub version are signed with the [same digital signature](https://github.com/M66B/FairEmail#downloads) (security certificate).
The F-Droid build is signed by the F-Droid organization with a different digital key.
This means you can't update the F-Droid build with the Play store or GitHub version or the other way around without reinstalling.
However, it is possible to install the GitHub version over the Play store version,
and the Play store app will do the same, when auto-updating isn't disabled for the app in the app description.

The version in the [Play Store test program](https://play.google.com/apps/testing/eu.faircode.email) is more often updated,
but not all GitHub releases will be released as Play Store test version.

The GitHub version will automatically check for updates and notify you when there is an update available.
Since apps can't update themselves, updates can't be automatically installed.
However, you can easily install an update via a button in the update available notification.
You could use the [IzzyOnDroid F-Droid Repository](https://apt.izzysoft.de/fdroid/) to manage GitHub updates.

<br />

<a name="faq174"></a>
**(174) Is auto discovery supported?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq174)

Yes, multiple methods of auto discovery are available.

The preferred, simplest and fastest method is using DNS records,
please see see [RFC6186](https://tools.ietf.org/html/rfc6186) and [RFC8314](https://tools.ietf.org/html/rfc8314) for the details.

Example DNS records (SRV=record type, 0=priority, 1=weight, 993/587=port number):

```
_imaps._tcp SRV 0 1 993 imap.example.com.
_submission._tcp SRV 0 1 587 smtp.example.com.
```

[Mozilla's autoconfiguration](https://wiki.mozilla.org/Thunderbird:Autoconfiguration) is supported too,
but only if the configuration file is accessible via a secure (https) connection.
The Play Store version of the app will query Mozilla's database only to comply with Play Store policies.
If you want to query your own server on your own domain name, you should install or update to the GitHub version of the app.

FairEmail will also check the [MX record](https://en.wikipedia.org/wiki/MX_record) and if common email ports (143/993, 465/587) are open.

There is no auto discovery for POP3.

<br />

<a name="faq175"></a>
**(175) Why should battery optimizations be disabled?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq175)

If battery optimizations are enabled ([Doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby)),
Android will stop the app when it is running in the background, resulting in no more messages being sent and received in the background anymore.

Manufacturers also tweak Android rather often to stop apps in the background for ostensibly better battery life, please [see here](https://dontkillmyapp.com/) for more details.

<br />

<a name="faq176"></a>
**(176) When will a message be considered safely transported?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq176)

If the receive option *Check transport layer security (TLS)* is enabled,
a green shield will be shown only if a message was transported securely by all servers.

To show shields, the option *Show authentication status indicator* in the display settings should be enabled.

A message will be consired safely transported if *every* [Received](https://datatracker.ietf.org/doc/html/rfc2821#section-4.4) header:

* contains the phrase 'using TLS', 'via HTTP', 'version=TLS'
* contains the phrase '(qmail <nnn> invoked by uid <nnn>)'
* contains the phrase '(Postfix, from userid nnn)'
* has a *by* with a local address
* has a *by* xxx.google.com
* has a *from* with a local address
* has a *via* with the value '[Frontend Transport](https://social.technet.microsoft.com/wiki/contents/articles/50370.exchange-2016-what-is-the-front-end-transport-service-on-the-mailbox-role.aspx)'
* has a *with* with the value 'TLS'
* has a *with* with the value 'local', '[LMTPx](https://en.wikipedia.org/wiki/Local_Mail_Transfer_Protocol), '[MAPI](https://en.wikipedia.org/wiki/MAPI)'
* has a *with* with the value 'HTTP', 'HTTPS', or 'HTTPREST'
* has a *with* with the value '[xMTPSx](https://datatracker.ietf.org/doc/html/rfc3848)' ('xMTPAx' is considered insecure)

A local address is a local host address, a site local address or a link local address.

Example:

```
Received: brown.elm.relay.mailchannels.net (brown.elm.relay.mailchannels.net. [23.83.212.23])
	by mx.google.com with ESMTPS id d10si6675855pgb.5.2021.12.24.13.20.38
	for <test@example.org> (version=TLS1_2 cipher=ECDHE-ECDSA-AES128-GCM-SHA256 bits=128/128);
```

<br />

<a name="faq177"></a>
**(177) What does 'Sensitivity' mean?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq177)

The sensitivity of a message indicates the confidentiality of a message.

* Personal: for you only
* Private: for you and trusted people only
* Confidential: for a company, organization or department only

Please see [this article](https://support.microsoft.com/en-us/office/mark-your-email-as-normal-personal-private-or-confidential-4a76d05b-6c29-4a0d-9096-71784a6b12c1) for more information.

The sensitivity indication is sent as [a message header](https://datatracker.ietf.org/doc/html/rfc4021#section-2.1.55).

<br />

<a name="faq178"></a>
**(178) Why are widgets not updating?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq178)

Apps provide the layout and data for widgets on demand, but the homescreen app/launcher manages all widgets, with a little help from Android.

If widgets are not being updated, this is often caused by missing permission.
Please see [this video](https://www.youtube.com/watch?v=ywQrYJ6rtnM) about how to fix this.

<br />

<a name="faq179"></a>
**(179) What are reply templates?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq179)

Reply templates are predefined answer texts. They can be defined via the main navigation menu (left side menu).

You can reply with a template, insert a template via the three-dots overflow menu in the message editor,
and long press on an open space to insert a snippet (the latter requires Android 6 Marshmallow or later).

Templates can have the following options:

* *Default*: standard template to use when writing a new message
* *Use as read receipt*: template to use instead of the default read receipt text
* *Favorite*: template will be added in the main reply popup menu
* *Snippet*: template will be used as text fragment (since version 1.1857)
* *Hide from menus*: template will be hidden (disabled)

Since version 1.2068 it is possible to send a template message with an intent:

```
(adb shell) am start-foreground-service -a eu.faircode.email.TEMPLATE --es template <template name> --es identity <identity display name> --es to <email address> --es cc <email address> --es subject <subject>
```

**Important**: you need to configure a display name for the identity, and use this to identify the identity.

<br />

<a name="faq180"></a>
**(180) How do I use LanguageTool?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq180)

LanguageTool integration needs to be enabled in the integration settings.

After writing some text, you can long press on the save draft button to perform a grammar, style, and spell check via [LanguageTool](https://languagetool.org/).
Texts with suggestions will be marked and if you tap on a marked suggestion,
it will be shown by the keyboard if the keyboard supports this,
else you can double tap or long press the marked text to show suggestions.

Since version 1.1974 there is an option to check paragraphs after a new line.

The suboption *Use formal form* can be enabled to let LanguageTool suggest more formal text (business, legal, etc).

Since version 1.2000 you can configure a username and an API key to access the premium features.
If you are looking for the API key, please [go here](https://languagetool.org/editor/settings/access-tokens).

You can long press text to select a word, and add it to or remove it from the personal dictionary via the *copy/paste* pop-up menu.

<br />

<a name="faq181"></a>
**(181) How do I use VirusTotal?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq181)

VirusTotal integration needs to be enabled in the integration settings and an API key needs to be entered.
To get an API key, you'll need to sign up via the [VirusTotal website](https://www.virustotal.com/).

When integration is enabled and an API key is available, a *scan* icon button will be shown for each attachment.
Tapping on the scan button will calculate the SHA-256 hash of the attachment and lookup the file via the VirusTotal API.
If the file is known by VirusTotal, the number of virus scanners considering the file as malicious will be shown.
If the file isn't known by VirusTotal, an upload button will be shown to upload the file for analysis by VirusTotal.

This feature was added in version 1.1942 and is available in non Play store versions of the app only.

<br />


<a name="faq182"></a>
**(182) How can I select how a link should be opened?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq182)

When clicking on a link, by default, a confirmation dialog will be shown.
The available browser(s) will be listed and if a browser supports [Custom Tabs](https://developer.chrome.com/docs/android/custom-tabs/), it will be listed twice,
once with an "open external" icon (the browser will be started standalone, independent of the app)
and once without this icon (the browser will be started embedded as "Custom Tab", which looks like being part of the app, but in fact isn't).

In addition, *Select app* will be listed, which means that the link will be handed over to Android, which will select how to open the link.
In most cases, this will be with the default browser, which you can select in the Android settings.
If there is choice, Android will ask you how to open the link. You can select *Always* or *Just Once*.
If you want to reset *Always*, please [see here](https://support.google.com/pixelphone/answer/6271667) about how to.
Note that Android will always use the default browser as selected in the Android settings and therefore will never ask which browser to use.

You can confirm with *Ok* or select *Open with*, which behaves in the same way as the *Select app* option.

If you ticked *Do not ask this again for [domain name]*, you can undo this by using the *Reset questions* button in the miscellaneous settings tab page of the app.

If you disabled confirming links, you can enable this (temporarily) again in the privacy settings tab page of the app (*Confirm opening links*: off).

Note that you might need to enable confirming links and reset questions to show the link confirmation dialog again.

Please see [this FAQ](#faq35) on why you should be careful when opening links.

<br />

<a name="faq183"></a>
**(183) How do I use Send?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq183)

[Send](https://github.com/timvisee/send) is designed as temporary end-to-end encrypted file storage.
Only people with a link to a file can download and decrypt a file.
See for some more information [Wikipedia](https://en.wikipedia.org/wiki/Firefox_Send).

Send integration needs to be enabled in the integration settings.

Optionally, you can change the host address of the Send server.
Please [see here](https://github.com/timvisee/send-instances) for a list of public instances.

To upload a file and insert a link, you can use the insert link button in the message editor.

Send is only available in non-Play Store versions of the app (since version 1.1947).

<br />

<a name="faq184"></a>
**(184) How do I password protect content?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq184)

Password protected content is a simple, yet secure form of end-to-end encryption that requires no configuration.

How to use: select some text by long pressing it, and in the style toolbar at the bottom tap on the padlock-button and select *Password protect* in the pop-up menu.
This will replace the selected content with a link that the recipient can click to decrypt the content on a dedicated static web page.

Password protected content is sent as a [URI fragment](https://en.wikipedia.org/wiki/URI_fragment) and decrypted in the browser with JavaScript.
In other words, protected content is never stored on or seen by third party servers.
Since version 1.1990 received protected content will be decrypted by the app, with as fallback decryption in the browser for other email clients.

Password protected content is encrypted with [AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) / [GCM](https://en.wikipedia.org/wiki/Galois/Counter_Mode)
with a 256 bits key derived with [PBKDF2](https://en.wikipedia.org/wiki/PBKDF2) / [SHA](https://en.wikipedia.org/wiki/Secure_Hash_Algorithms)-512 with 120,000 iterations.
With a sufficiently long/complex password, which is communicated to the recipient securely, this is considered safe in 2022 and for the foreseeable future.

Due to [length limitations](https://stackoverflow.com/a/417184/1794097) of [URL](https://en.wikipedia.org/wiki/URL)s and
Android [binder limitations](https://developer.android.com/reference/android/os/TransactionTooLargeException),
the maximum content size is 1,500 bytes, which includes [HTML](https://en.wikipedia.org/wiki/HTML) formatting tags.
Images will be replaced with placeholders to reduce the content size, but other formatting, like bold, italic, links, etc., will be retained.
If the content is too long, there will be a popup *Text too long*.

The content size limit is also why complete messages (possibly including a long reply chain) cannot be password protected.
You can use [PGP](https://en.wikipedia.org/wiki/Pretty_Good_Privacy) or [S/MIME](https://en.wikipedia.org/wiki/S/MIME) encryption for this (see [this FAQ](#faq12)).

[Cross-site scripting](https://en.wikipedia.org/wiki/Cross-site_scripting) is prevented by using [DOMPurify](https://github.com/cure53/DOMPurify) (Apache License Version 2.0).

Protected content is only available in non-Play Store versions of the app (since version 1.1985) and requires Android 8 Oreo or later.

Sending protected content is a pro feature, decrypting protected content is a free feature.

<br />

<a name="faq185"></a>
**(185) Can I install FairEmail on Windows?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq185)

Yes, you can if you use Windows 11 or later and install the [Windows Subsystem for Android](https://learn.microsoft.com/en-us/windows/android/wsa/).

You'll need to [download the GitHub version](https://github.com/M66B/FairEmail/releases) of the app and sideload it,
which means that you need to enable developer mode, please [see here](https://learn.microsoft.com/en-us/windows/android/wsa/#test-and-debug),
and that you need to install adb (platform tools), [see here](https://developer.android.com/studio/command-line/adb).

You can install the app via the Windows command line like this:

```
cd /path/to/platform-tools
adb connect 127.0.0.1:58526
adb install /path/to/FairEmail-xxx.apk
```

It is also possible to install the Play Store, but this is more complicated.

The app isn't available in the Amazon store because Amazon rebuilds all Android apps, and unfortunately, the app doesn't work correctly after rebuilding anymore.
Amazon never responded to an issue reported about this.

<br />

<a name="faq186"></a>
**(186) How can I let the app auto store iCalendar invitations?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq186)

* Install the [GitHub version](https://github.com/M66B/FairEmail/releases) of the app
* Grant permissions via setup step 2 of the main settings screen
* Select a calendar in the accounts settings under *Manual setup and account options* (you can use the *Reset* button to disable storing invitations)

New invitations, with both a start and end date, will be stored automatically as *tentative*, with no alarms and reminders set.
If you accept or decline an invitation, the status will be updated accordingly, after the accept/decline message has been sent successfully.
Received updates and cancellations will be processed as well.

Since version 1.2115 it is possible to disable storing invitations tentatively with an option in the miscellaneous-settings tab page.
In this case, the event will be stored after it has been accepted.

Please make sure synchronizing calendars is enabled in the Android account settings
if you want to synchronize events to other devices.

This feature is available since version 1.1996.

This feature is not available in the Play store version of the app due to the permissions required.

This is a pro feature.

<br />

<a name="faq187"></a>
**(187) Are colored stars synchronized across devices?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq187)

Colored stars can't be stored on email servers because email protocols do not support this.
In other words, the color of stars is stored on your device only, and won't be synchronized across devices.

<br />

<a name="faq188"></a>
**(188) Why is Google backup disabled?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq188)

~~Google backup is disabled to prevent privacy-sensitive information, like account credentials and email addresses,~~
~~from [automatically being sent to Google](https://developer.android.com/guide/topics/data/autobackup).~~

~~In theory, there is client-side encryption, but there is no specification available about what this means.~~
~~Moreover, many people do not trust Google.~~

~~Unfortunately, it is not possible to enable cloud backup for other backup software without enabling Google backup.~~
~~Whether Google backup is enabled needs to be specified in the app manifest. So, unfortunately, it isn't possible to add an option for this.~~

~~As a replacement, you can back up and restore all settings, including the account settings and credentials, via the backup-settings tab page.~~
~~This backup export uses a proper encryption method, [see here](#faq36).~~

<br />

<a name="faq189"></a>
**(189) What is cloud sync?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq189)

Cloud sync is meant to synchronize configuration data across devices.
It can be used to restore configuration data onto a new device too.

Cloud sync is based on a cloud account.
You can register/login by entering a username and a password and using the *Login* button.

A cloud sync account needs to be activated, which is to prevent misusing the cloud sync server.
To activate a cloud sync account, use the *Activate* button to send an email to a special email address.
The email needs to come from an address used to activate the pro features before.
You'll receive an email in response indicating whether the activation was succesful or not.

The app will automatically synchronize once a day around 1:30 AM, provided there is an internet connection,
otherwise synchronization will be postponed until after an internet connection becomes available.
You can also manually synchronize with the opposite arrows button.

Synchronization will currently add and update enabled accounts and identities only,
but on the roadmap is synchronizing blocked senders and rules too.

Updating includes enabling/disabling accounts and identities.

Existing accounts or identities will never be deleted

Please note that accounts are only considered the same if they are cloud synced and never if the same account is configured on different devices.

All data is [end-to-end encrypted](https://en.wikipedia.org/wiki/End-to-end_encryption),
which means that the cloud server, currently powered by AWS, can't see the data contents.
The used encryption method is [AES-GCM-SIV](https://en.wikipedia.org/wiki/AES-GCM-SIV)
using a 256 bit key derived from the username and password with [PBKDF2](https://en.wikipedia.org/wiki/PBKDF2) using SHA256 and 310,000 iterations.

Cloud sync is an experimental feature. It is not available for the Play Store version of the app, yet.

<br>

<a name="faq190"></a>
**(190) How do I use OpenAI (ChatGPT)?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq190)

OpenAI can only be used if configured and enabled.

**Note that using OpenAI is not free (anymore) !**

<br>

**Setup**

* Create an account [here](https://platform.openai.com/signup)
* Pay some money via [here](https://platform.openai.com/settings/organization/billing/overview)
* Create an API key [here](https://platform.openai.com/account/api-keys)
* Copy the API key and paste it in the corresponding field of the integration settings
* Enable the OpenAI switch

<br>

**Usage**

*Editor*

Tap the robot button in the top action bar of the message editor.
A dialog box will appear (since version 1.2259).
You can select the prompt and select whether to input the message you typed and/or the message you are replying to.

For example: create a new draft and enter the text "*How far is the sun?*", and tap on the robot button in the top action bar.

Since version 1.2191 it is possible to define AI templates, which you can select after tapping on the robot icon.

<br>

*Summarize* (since version 1.2178)

You can request a summary via the horizontal three-dots button just above the message text.
It is possible to configure a button for this (a robot icon).

The summary prompt text can be configured in the receive-settings tab page.
The default is *Summarize the following text:*.

Since version 1.2182, it is possible to configure a quick action button and a swipe action to summarize a message.
Since version 1.2183, it is possible to use a rule action to summarize a message and use it as a preview text.

<br>

OpenAI isn't very fast, so be patient. Sometimes a timeout error occurs because the app is not receiving a response from OpenAI.

<br>

If you exceed [your usage limit](https://platform.openai.com/docs/guides/rate-limits), there will be an error message like this:

*Error 429: Too Many Requests insufficient_quota: You exceeded your current quota, please check your plan and billing details*

Note that you are required to switch to a [paid plan](https://openai.com/api/pricing/) after the testing period.

<br>

An alternative to OpenAI is using [Groq](https://groq.com/).

* Endpoint: **https://api.groq.com/openai/v1/**
* Suggested [model](https://console.groq.com/docs/models): **gemma2-9b-it** or **llama-3.3-70b-versatile**

It's faster and free to use for now.

<br>

You can select the [model](https://platform.openai.com/docs/models/overview),
and configure the [temperature](https://platform.openai.com/docs/api-reference/chat/create#chat/create-temperature).

Please read the [privacy policy](https://openai.com/policies/privacy-policy) of OpenAI,
and perhaps [this article](https://katedowninglaw.com/2023/03/10/openais-massive-data-grab/)
and [this article](https://www.ncsc.gov.uk/blog-post/chatgpt-and-large-language-models-whats-the-risk) too.

FairEmail does not use third-party libraries to avoid being tracked when OpenAI is not being used.

<br>

It is possible to use **DeepInfra** too (since version 1.2132).

* Create an account on the [DeepInfra website](https://deepinfra.com/) and deploy a model, for example, *meta-llama/Llama-2-13b-chat-hf*
* In the integration settings enter the URI https://api.deepinfra.com/v1/openai, an API key and the model name

<br>

<br>

If you are looking for Google Gemini support, please see [this FAQ](#faq204).

This feature is experimental and requires version 1.2053 or later for the GitHub version and version 1.2182 or later for the Play Store version.

<br>

<a name="faq191"></a>
**(191) How do I download and keep older messages on my device?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq191)

You can download and keep older messages in the unified inbox folders by using *Fetch more messages* in the three-dots overflow menu of the start screen.
For other folders, you can long press the folder in the folder list of the account (tap on the account name in the navigation menu = left side menu).
When you long press on a parent folder, there will be a subfolders option with a menu item to fetch more messages for all child folders.
Please read the remark in the confirmation dialog box.

Note that starred (favorite) messages will be kept on your device "forever".

Instead of downloading many messages to your device, consider [searching for messages on the email server](#faq13).

Longer explanation: the app has a sync and a keep window per folder.
The *Fetch more messages* menu item is, in fact, a mini wizard to change the keep window of one or more folders at the same time,
and to initiate a corresponding synchronization operation.
If you long press on a folder in the folder list and select *Edit properties*, you can change the sync and keep window of the folder.
The sync window determines which messages will be checked with a standard sync operation (default 7 days),
and the keep window determines how long messages will be kept on the device (default 30 days).
If you sync another time within 30 seconds, all messages in the keep window will be checked,
which is useful when you have been moving around messages with another email client, for example on your desktop computer.
It is mostly pointless to check all messages all the time, especially considering that this will use battery power,
which is why there is a separate sync and keep window, and why the app checks messages younger than a week only.

<br>

<a name="faq192"></a>
**(192) How can I resolve 'Couldn't connect to host, port: ...; timeout ...;' ?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq192)

This error message means that the app didn't receive a response from the email server of your email provider.
The email server might not be responding, for example because it is offline for maintenance, or the response might not arrive, for example due to internet connectivity issues.

So, please check if your email provider didn't announce server maintenance, and if your internet connection is working correctly. Also, try to switch to mobile data or Wi-Fi.

If you are using a VPN, firewall, ad blocker, or similar, please try to disable it, or make an exception for FairEmail.
Email servers often block connections via a VPN, and in general from foreign countries.

<br>

<a name="faq193"></a>
**(193) How can I import Outlook contacts?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq193)

If there are one or more Outlook or Office 365 accounts configured,
there will be a button in the main settings screen in setup step 2 to download Outlook contacts (since version 1.2076).
After tapping on this button, you can select the account to download contacts for.
Microsoft will ask for permission to read the contacts,
and after granting this permission, the app will download the contacts with an email address into the local contacts' database.

In the message editor, type the first few letters of the email address or name in any of the email address fields, and the downloaded addresses will be suggested.

For privacy and security reasons, FairEmail doesn't have permissions to write into the Android address book, and also not to write in the address book of Outlook.
This means that contacts can be downloaded as local contacts only, and can't be synchronized two ways.
If you are looking to synchronize your Outlook contacts with the Android address book, you should look for a sync app in the Play Store which can do this.

<br>

<a name="faq194"></a>
**(194) How can I set up automatic deletion of old messages?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq194)

To set up automatic deletion of old messages:

* Tap on the account name in the navigation menu (left side menu)
* Long press the folder you want to set up auto deletion for, and select to edit the folder properties
* Near the bottom of the properties screen, there is an option to enable auto deletion

Messages in the trash and spam folder will be **permanently** deleted, and messages in other folders will be moved to the trash folder.

Unread, starred and snoozed messages and messages younger than 24 hours will not be automatically deleted.
You can enable auto deletion of unread messages in the receive-settings tab page (option *Delete old unread messages*).

Note that it isn't a good idea to automatically delete recent messages, especially not for the spam folder because there might be legitimate messages in the spam folder.
There is a button or menu item (depending on the screen size) in the top action bar/menu of the trash and spam folder to empty the folder.
This way there is at least a visual check.

Auto deletion will be done on a full sync only.
For the inboxes, you can use *Force sync* in the three-dots overflow menu of the start screen.
For other folders, you can long press the folder in the folder list of the account (*not* the navigation menu), and select *Synchronize now* in the pop-up menu.
You can also pull down the messages list of any folder to sync it, and repeat this again within 30 seconds for a full sync.

<br>

<a name="faq195"></a>
**(195) Why are all messages in the archive folder of Gmail?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq195)

The Gmail server stores all messages, except the messages in the draft, sent, trash and spam messages folder in the all messages' folder (=archive folder).
FairEmail is an email client, which basically displays what is on the email server, and therefore it will show these messages too.

To be clear: FairEmail does not store the messages in the archive folder, unless you explicitly archive a message.

This has advantages, though, because it makes searching in all messages easier.

<br>

<a name="faq196"></a>
**(196) Can you add empty trash on leaving the app?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq196)

Besides that leaving an app is an ambiguous action, automatically deleting trashed messages is a risky action because deleted messages can't be restored anymore.
A message could accidentally be trashed, and you could switch to another app, which could be interpreted as leaving the app, and the message would be gone forever.

Instead, you can configure auto-deletion of older messages, which is safer because the messages won't be deleted immediately.
For this, please tap on the account name in the navigation menu (left side menu) to go to the folder list of the account.
In the folder list, long press the trash messages folder and select to edit the folder properties.
Near the end of the properties screen, there is a checkbox to enable auto-deletion.
You might want to change the number of days to keep messages on your device.

Note that the reference time is the time the message was first stored on the device, not the date/time of the message itself.

<br>


<a name="faq197"></a>
**(197) How can I print a message?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq197)

You can print a message, both to a PDF or a printer, by tapping on the horizontal three-dots button just above the message text near the left side.
You might need to tap on the '>' button to show the message actions again.

Note that you can configure a button for printing via the same pop-up menu.

Printing is managed by Android for all apps. You should be able to select the paper size, etc.
You might need to update or install a driver app for your printer via the Play Store.
For example, for [HP printers](https://play.google.com/store/apps/details?id=com.hp.android.printservice).

A message is printed as-is, which means that the sender of the message determines the margins, etc.

<br>

<a name="faq198"></a>
**(198) Can you add spell checking?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq198)

Spell checking should be provided by the keyboard app for all other apps.
Sometimes, particularly on ChromeOS, spell checking needs to be enabled in the settings.

That said, LanguageTool, which can be enabled in the integration-settings tab, is an excellent alternative to style and spell checking.

<br>

<a name="faq199"></a>
**(199) Can you add proxy support?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq199)

In fact, (HTTP, HTTPS, SOCKS) proxy support was removed because it is not possible to let an app proxy DNS requests,
or in other words, an in-app proxy will always leak host names and therefore give a false sense of security.

If you want to proxy traffic, for example to use Tor,
you should use an Android VPN-service based app, which is the only way to reliably proxy traffic.

Please note that if you want to use a .onion address, you will need to disable private DNS in the Android network settings.

<br>

<a name="faq200"></a>
**(200) How can I use Adguard to remove tracking parameters?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq200)

To use [Adguard](https://adguard.com/) to remove tracking parameters from links:

1. Enable confirming links in the privacy-settings tab page
1. Download and enable [the Adguard filter list](https://github.com/AdguardTeam/FiltersRegistry), also via the privacy-settings tab page
1. When you tap on a link, the app will check the filter list
1. If a list entry is found for the link, the app will suggest to *Remove tracking parameters* in the confirmation dialog box

Note that the Adguard filter list contains over 2,000 entries, which takes a few moments to scan,
visible as a short delay between tapping on a link and the link confirmation dialog box appearing.

<br>

<a name="faq201"></a>
**(201) What is certificate transparency?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq201)


Please see [this article](https://certificate.transparency.dev/howctworks/) about what certificate transparency is.
Alternatively, see [this Wikipedia article](https://en.wikipedia.org/wiki/Certificate_Transparency).

When certificate transparency is enabled in the connection-settings tab page of the app,
the [Chrome Certificate Transparency Policy](https://github.com/GoogleChrome/CertificateTransparency/blob/master/ct_policy.md) will be applied.
The CT log will be downloaded from [https://www.gstatic.com/](https://www.gstatic.com/ct/log_list/v3/all_logs_list.json).

FairEmail uses [this library](https://github.com/appmattus/certificatetransparency) to implement certificate transparency via a custom trust manager.

<br>

<a name="faq202"></a>
**(202) What is DNSSEC and what is DANE?**

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23faq202)

Please see [this Wikipedia article](https://en.wikipedia.org/wiki/Domain_Name_System_Security_Extensions) about what DNSSEC is.

Please see [this article](https://github.com/internetstandards/toolbox-wiki/blob/main/DANE-for-SMTP-how-to.md) about what DANE is.
Alternatively, see [this Wikipedia article](https://en.wikipedia.org/wiki/DNS-based_Authentication_of_Named_Entities).

You can use [this tool](https://ssl-tools.net/tlsa-generator) to generate TLSA DNS records for DANE (select either PKIX-EE or DANE-EE).

You can enable enforcing DNSSEC and/or DANA in the (advanced) account and identity settings (since version 1.2149).

Note that only some email providers support DANE and that only a limited number of DNS servers support DNSSEC (January 2024: ~30%), which is required for DANE.
Most private DNS providers support DNSSEC, though. You can configure private DNS in the Android network settings (since Android 9).
To be sure that private DNS is being used, better configure a host name like *dns.google*, *1dot1dot1dot1.cloudflare-dns.com* or *dns.quad9.net*.
An alternative is using Certificate Transparency, see the previous FAQ.

Some email providers known to support DANE for client-to-server traffic:

* [Disroot.org](https://disroot.org/)
* [Freenet.de](https://email.freenet.de/)
* [Mailbox.org](https://mailbox.org/)
* [Posteo.de](https://posteo.de/)
* [web.de](https://web.de/email/)
* [GMX](https://www.gmx.net/mail)

This is not a complete and exhaustive list.

Please see [this article](https://www.zivver.com/blog/why-cisos-and-security-professionals-can-no-longer-rely-on-regular-email-for-the-sharing-of-personal-information) about why DANE is important.

Note that DNSSEC and DANE are available in the GitHub version only.

<br>

<a name="faq203"></a>
**(203) Where is my sent message?**

When you write a message, it will be stored in the draft messages folder.

When you send a message, it will be in the outbox first and later in the sent messages folder.

The outbox is a queue of messages to be transferred to the email server of your email provider.
After a message has been transferred to the email server, it will be stored in the sent messages folder.

The sent messages folder can be selected in the account settings: navigation menu (left side menu) > Settings > Manual setup and account options > Accounts > tap the account.

The email server will take care of sending the message to the recipient.

If a message could not be sent to the recipient, you'll in most cases receive a non-delivery notification message,
a special email, indicating the reason, like user (email address) unknown.
FairEmail will decode non-delivery notification messages, so you can see all the details.

Basically, an outgoing message is either in the draft messages folder, the outbox, or the sent messages folder.

<br>

<a name="faq204"></a>
**(204) How do I use Gemini?**

Gemini can only be used if configured and enabled.

**Note that using Gemini is not free (anymore) !**

<br>

To use [Gemini](https://gemini.google.com/), please follow these steps:

1. **Check if your country [is supported](https://ai.google.dev/available_regions)**
1. Get an API key via [here](https://ai.google.dev/tutorials/setup)
1. Enter the API key in the integration settings tab page
1. Enable Gemini integration in the integration settings tab page

Please note that connections through a VPN are mostly refused.

<br>

For usage instructions, please see [this FAQ](#faq190).

Please read the privacy policy of [Gemini](https://support.google.com/gemini/answer/13594961).
FairEmail does not use third-party libraries to avoid being tracked when Gemini is not being used.

This feature is experimental and requires version 1.2171 or later for the GitHub version and version 1.2182 or later for the Play Store version.

<br>

<a name="faq205"></a>
**(205) How do I check the integrity of an APK file?**

"*Artifact attestations enable you to create unfalsifiable provenance and integrity guarantees for the software you build.*
*In turn, people who consume your software can verify where and how your software was built.*"

Please [see here](https://docs.github.com/en/actions/security-guides/using-artifact-attestations-to-establish-provenance-for-builds) for details.

You can verify in this way that an APK file was built and signed by a GitHub workflow:

1. Install the [GitHub CLI](https://cli.github.com/)
2. Download and extract the [APK files](https://github.com/M66B/FairEmail/actions)
3. [Verify](https://docs.github.com/en/actions/security-guides/using-artifact-attestations-to-establish-provenance-for-builds#verifying-artifact-attestations-with-the-github-cli) attestation of an APK file


```
gh attestation verify xyz.apk -R M66B/FairEmail
```

Attestation of APK files is available from version 1.2209.

<br>

<a name="faq206"></a>
**(206) How can I move or copy messages from one account to another?**

There are two options for this:

1. Long press a message in the message list to select it, tap the three-dot button that appears, scroll to the bottom of the pop-up menu and select the target account.
2. Long press the move-to button just above the message text to select the target account first.

To copy a message to another account, long press the target folder.

<br>

<h2><a name="get-support"></a>Get support</h2>

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fm66b.github.io%2FFairEmail%2F%23get-support)

FairEmail is supported on Android smartphones and tablets and ChromeOS only.

There is support only if the app was downloaded via one of the [supported download locations](https://github.com/M66B/FairEmail#downloads).

Only the latest Play store version and latest GitHub release are supported.
The F-Droid build is supported only if the version number is the same as the version number of the latest GitHub release.
This also means that downgrading is not supported.

There is no support for things that are not directly related to FairEmail.

There is no support on building and developing things by yourself.

<a name="feature_request"></a>

A change will be considered only if more than a few people ask for it.
Changes in the past resulted too often in lots of complaints of other people.

Requested features should:

* be within the scope of the app
* be useful to more than a few people
* fit logically and consistently within the app
* fit within the philosophy of the app (privacy-oriented, security-minded)
* comply with common standards (IMAP, SMTP, etc.)
* comply with the [Core app quality guidelines](https://developer.android.com/docs/quality-guidelines/core-app-quality)

Features unrelated to email, including reading newsgroups and reading RSS feeds, fall outside the scope of the project.

The goal of the design is to be minimalistic (no unnecessary menus, buttons, etc) and non distracting (no fancy colors, animations, etc).
All displayed things should be useful in one or another way and should be carefully positioned for easy usage.
Fonts, sizes, colors, etc should be material design whenever possible.

There are requests daily to change the appearance in this or that way, but the problem is that these requests are more often than not conflicting.
To prevent making other people unhappy, changes in the appearance always need to clearly and objectively contribute to the usability of the app to be considered.

A feature will be considered useful to most people if more than 0.5% of the users request a feature, which in practice means about 2500 people.
Assuming that only 10% of the app's users will ask for a new feature, about 250 requests are needed to add a new feature.

Features not fulfilling these requirements will likely be rejected.
This is also to keep maintenance and support in the long term feasible.
Please see also this Wikipedia article about [feature creep](https://en.wikipedia.org/wiki/Feature_creep).

Note that there are already more features and options in FairEmail than in any other Android email client.

<br />

&#x1F6DF; &#x1F6DF; &#x1F6DF; &#x1F6DF; &#x1F6DF;

&#x1F1EC;&#x1F1E7; If you have a question, want to request a feature or report a bug, **please use [this form](https://contact.faircode.eu/?product=fairemailsupport)**.

&#x1F1E9;&#x1F1EA; Wenn Sie eine Frage haben, eine Funktion anfordern oder einen Fehler melden möchten, **verwenden Sie bitte [dieses Formular](https://contact.faircode.eu/?product=fairemailsupport)**.

&#x1F1EB;&#x1F1F7; Si vous avez une question, souhaitez demander une fonctionnalité ou signaler un bogue, **veuillez utiliser [ce formulaire](https://contact.faircode.eu/?product=fairemailsupport)**.

<br />

**GitHub issues and GitHub pull requests are disabled due to frequent misusage.**

<br />

Copyright &copy; 2018-2024 Marcel Bokhorst.
