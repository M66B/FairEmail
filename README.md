<div align="center">
<h1>FairEmail</h1>
<img src="https://github.com/M66B/FairEmail/raw/master/images/banner7_long.png" />
<p>
	<a href="#downloads">Downloads</a> &bull;
	<a href="https://github.com/M66B/FairEmail/blob/master/PRIVACY.md#fairemail">Privacy</a> &bull;
	<a href="https://github.com/M66B/FairEmail/blob/master/PRIVACY.md#fairemail">Support</a> &bull;
	<a href="https://github.com/M66B/FairEmail/blob/master/PRIVACY.md#fairemail">License</a>
<p>
<img src="https://img.shields.io/github/license/m66b/fairemail.svg" alt="Github">
<img src="https://img.shields.io/github/release/M66B/FairEmail.svg" alt="Github">
<img src="https://img.shields.io/github/commits-since/M66B/FairEmail/0.1.svg">
<p><i>Fully featured, open-source, privacy oriented, e-mail client for Android</i></p>
</div>

<!-- FairEmail is easy to set up and works with virtually all email providers, including Gmail, Outlook and Yahoo! -->
<!-- *FairEmail is only an e-mail client, so you need to use your own email address.* -->
<!-- *FairEmail does not support non-standard protocols, like Microsoft Exchange Web Services and Microsoft ActiveSync.* -->

## Main features

* 100% [open source](https://github.com/M66B/FairEmail/blob/master/LICENSE) and [privacy oriented](https://github.com/M66B/FairEmail/blob/master/PRIVACY.md)
* Unlimited accounts and e-mail addresses
* Unified inbox (or accounts and folders)
* [Conversation threading](https://en.wikipedia.org/wiki/Conversation_threading)
* 2-way synchronization
* Always active push notifications to never miss e-mails
* [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE) (push messages) supported
* Offline storage
* Text formatting - colour, font size, lists and more
* Minimal - only ~20MB, light on data and battery usage
* Material design (including dark/black theme)
* Actively mainted and supported

## Privacy features

* Encryption/decryption ([OpenPGP](https://www.openpgp.org/) and [S/MIME](https://en.wikipedia.org/wiki/S/MIME))
* Reformat messages to prevent [phishing](https://en.wikipedia.org/wiki/Phishing)
* Confirm showing/opening images and links to stop tracking and phishing
* Attempt to recognize and disable tracking images
* Warning if messages could not be [authenticated](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq92)

## Simple

* Quick setup
* Easy navigation
* Minimalist design to focus on writing e-mails

## Secure

* No data storage on third party servers
* Only open standards (IMAP, POP3, SMTP, OpenPGP, S/MIME, etc)
* Safe message view (Unsafe HTML/CSS/JS are stripped out)
* No special permissions required
* No ads
* No analytics and no tracking ([error reporting](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq104) via Bugsnag is opt-in)
* No [Google backup](https://developer.android.com/guide/topics/data/backup) or [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)

## Pro features

All pro features are convenience or advanced features.

* Account/identity/folder colors
* ([Coloured stars](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq107))
* Notification settings (sounds) per account/folder/sender (requires Android 8 Oreo) ([instructions](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq145))
* Configurable notification actions
* Snooze messages ([instructions](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq67))
* Send messages after selected time
* Synchronization scheduling ([instructions](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq78))
* Reply templates
* Accept/decline calendar invitations
* Add message to calendar
* ([Filter rules](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq71))
* Automatic message classification ([instructions](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq163))
* Search indexing, search on server ([instructions](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq13))
* Keyword management
* S/MIME sign/encrypt ([instructions](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq12))
* Biometric/PIN authentication ([instructions](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq113))
* Message list widget
* Settings exporting

## Screenshots

Please look [here](https://email.faircode.eu/#screenshots) for screenshots.

## Downloads

Supported download locations:

* [GitHub](https://github.com/M66B/FairEmail/releases)
* [Play store](https://play.google.com/store/apps/details?id=eu.faircode.email)
* [Play store](https://play.google.com/apps/testing/eu.faircode.email) (test)
* [F-Droid](https://f-droid.org/en/packages/eu.faircode.email/) ([last build status](https://f-droid.org/wiki/page/eu.faircode.email/lastbuild)) ([F-Droid app](https://f-droid.org/))
<!-- * ~~[AppGallery](https://wap3.hispace.hicloud.com/uowap/index.jsp#/detailApp/C101678151) (the AppGallery app can be downloaded [here](https://huaweimobileservices.com/appgallery/))~~ -->
<!-- * ~~[Amazon](https://www.amazon.com/gp/product/B0983R6MH2)~~ (the APK file repackaged by Amazon is incomplete! An issue report was never answered by Amazon.) -->

All versions provide the same features, except that there is no Android Auto support in the Play store version.
Please look [here](https://forum.xda-developers.com/showpost.php?p=83801249&postcount=16542) about why it's not.

**Important**: after enrolling in the [Advanced Protection Program](https://landing.google.com/advancedprotection/)
you cannot use third party email apps anymore, please see [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq22) for more information.

The Gmail quick setup wizard can be used in official releases only (Play store or GitHub) because Google approved the use of OAuth for one app signature only.

**Important**: Gsuite accounts authorized with a username/password will stop working
[in the near future](https://gsuiteupdates.googleblog.com/2019/12/less-secure-apps-oauth-google-username-password-incorrect.html).
Gsuite accounts authorized with the quick setup wizard (OAuth) will keep working.

To download a GitHub release you might need to expand the assets section to download the [APK file](https://en.wikipedia.org/wiki/Android_application_package).

The GitHub version is being updated more often than the Play store version.
The GitHub release will automatically check for updates on GitHub (this can be turned off in the miscellaneous settings).

F-Droid builds new versions irregularly and you'll need the F-Droid client to get update notifications.
To get updates in a timely fashion you are advised to use the GitHub release.

**Important**: There is support on the F-Droid build only if the version number of the F-Droid build is the same as the version number of the latest GitHub release.
Please [see here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq147) for more information on third-party builds.

Because F-Droid builds and GitHub releases are signed differently, an F-Droid build needs to be uninstalled first to be able to update to a GitHub release.

Certificate fingerprints:

```
MD5: 64:90:8E:2C:0D:25:29:B0:D0:26:2D:24:D8:BB:66:56
SHA1: 17:BA:15:C1:AF:55:D9:25:F9:8B:99:CE:A4:37:5D:4C:DF:4C:17:4B
SHA256: E0:20:67:24:9F:5A:35:0E:0E:C7:03:FE:9D:F4:DD:68:2E:02:91:A0:9F:0C:2E:04:10:50:BB:E7:C0:64:F5:C9
```

One line command to display certificate fingerprints:

```unzip -p fairemail.apk META-INF/CERT.RSA | keytool -printcert```

I do not hand over the signing keys of my apps to Google.
However, in the second half of 2021 [I am required](https://commonsware.com/blog/2020/09/23/uncomfortable-questions-app-signing.html) to hand over my signing keys to Google.

## Compatibility

FairEmail requires at least Android 5 Lollipop.

Individual message notifications are available on Android 7 Nougat and later only
because earlier Android versions do not support notification grouping.

Notification settings (sounds) per account/folder/sender are available on Android 8 Oreo and later only
because earlier Android versions do not support notification channels.

FairEmail will work properly on devices without any Google service installed.

Please see [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-known-problems) for known problems.

## Contributing

*Documentation*

Contributions to this document and the frequently asked questions
are preferred in the form of [pull requests](https://help.github.com/articles/creating-a-pull-request/).

*Translations*

* You can translate the in-app texts of FairEmail [on Crowdin](https://crowdin.com/project/open-source-email)
* If your language is not listed, please send a message through [this contact form](https://contact.faircode.eu/?product=other)

*Source code*

Building FairEmail from source code is straightforward with [Android Studio](http://developer.android.com/sdk/).
It is expected that you can solve build problems yourself, so there is no support on building.

Source code contributions are preferred in the form of [pull requests](https://help.github.com/articles/creating-a-pull-request/).
Please [contact me](https://contact.faircode.eu/?product=other) first to tell me what your plans are.

Please note that by contributing you agree to the license below, including the copyright, without any additional terms or conditions.

## Attribution

See [here](https://github.com/M66B/FairEmail/blob/master/ATTRIBUTION.md) for a list of used libraries and associated license information.

Online translations are supported by:

![Crowdin logo](/images/localization-at-white-rounded-bordered@1x.png)

Error reporting is sponsored by:

![Bugsnag logo](/images/bugsnag_logo_navy.png)

[Bugsnag](https://www.bugsnag.com/) monitors application stability
and is used to [help improve FairEmail](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq104).
Error reporting is disabled by default, see also [the privacy policy](https://github.com/M66B/FairEmail/blob/master/PRIVACY.md#fairemail).

Copyright &copy; 2018-2021 Marcel Bokhorst. All rights reserved.
