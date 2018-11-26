# FairEmail

*Open source, privacy friendly email app*

This email app might be for you if your current email app:

* takes long to receive or show messages
* can manage only one email address
* cannot show conversations
* cannot work offline
* looks outdated
* is not maintained
* stores your email on their servers
* is closed source, potentially violating your privacy

This app is minimalistic by design, so you can concentrate on reading and writing messages.

This app starts a foreground service with a low priority status bar notification to make sure you'll never miss new email.
See also [this FAQ](https://github.com/M66B/open-source-email/blob/master/FAQ.md#user-content-FAQ2).

## Main features

* 100 % [open source](https://github.com/M66B/open-source-email/blob/master/LICENSE)
* Multiple accounts (inboxes)
* Multiple identities (outboxes)
* Unified inbox
* Flat [conversation threading](https://en.wikipedia.org/wiki/Conversation_threading)
* Two way synchronization
* Offline storage and operations
* Battery friendly
* Low data usage
* Material design

## Pro features

* Signatures
* Dark/black theme
* Account/identity colors
* Notifications per account
* Reply templates
* Search on server
* Preview sender/subject/photo in new message notifications
* Keyword management
* Encryption/decryption ([OpenPGP](https://www.openpgp.org/))
* Export settings

## Simple

* Easy navigation
* No unnecessary settings
* No bells and whistles

## Secure

* Allow encrypted connections only
* Accept valid security certificates only
* Authentication required
* Safe message view (styling, scripting and unsafe HTML removed)
* Confirm opening links, images and attachments
* No special permissions required
* No advertisements
* No analytics and no tracking
* No [Google backup](https://developer.android.com/guide/topics/data/backup)

## Efficient

* [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE) (push messages) supported
* Built with latest development tools and libraries
* Android 6 Marshmallow or later required

## Screenshots

Please [see here](https://email.faircode.eu/#screenshots).

## Downloads

* [GitHub](https://github.com/M66B/open-source-email/releases)
* [Play store](https://play.google.com/apps/testing/eu.faircode.email)

Certificate fingerprints:

* MD5: 64:90:8E:2C:0D:25:29:B0:D0:26:2D:24:D8:BB:66:56
* SHA1: 17:BA:15:C1:AF:55:D9:25:F9:8B:99:CE:A4:37:5D:4C:DF:4C:17:4B
* SHA256: E0:20:67:24:9F:5A:35:0E:0E:C7:03:FE:9D:F4:DD:68:2E:02:91:A0:9F:0C:2E:04:10:50:BB:E7:C0:64:F5:C9

## Compatibility

FairEmail requires at least Android 6 Marshmallow.

FairEmail will work properly on devices without any Google service installed.

See [here](https://github.com/M66B/open-source-email/blob/master/FAQ.md#known-problems) for known problems.

## Support / frequently asked questions

See [here](https://github.com/M66B/open-source-email/blob/master/FAQ.md) for a list of often asked questions and about how to get support.

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

Please note that you agree to the license below by contributing, including the copyright.

## Attribution

FairEmail uses:

* [JavaMail](https://javaee.github.io/javamail/). Copyright (c) 1997-2018 Oracle® and/or its affiliates. All rights reserved. [GPLv2+CE license](https://javaee.github.io/javamail/JavaMail-License).
* [jsoup](https://jsoup.org/). Copyright © 2009 - 2017 Jonathan Hedley. [MIT license](https://jsoup.org/license).
* [JCharset](http://www.freeutils.net/source/jcharset/). Copyright © 2005-2015 Amichai Rothman. [GNU General Public License](http://www.freeutils.net/source/jcharset/#license)
* [Android Support Library](https://developer.android.com/tools/support-library/). Copyright (C) 2011 The Android Open Source Project. [Apache license](https://android.googlesource.com/platform/frameworks/support/+/master/LICENSE.txt).
* [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/). Copyright 2018 The Android Open Source Project, Inc. [Apache license](https://github.com/googlesamples/android-architecture-components/blob/master/LICENSE).
* [colorpicker](https://android.googlesource.com/platform/frameworks/opt/colorpicker). Copyright (C) 2013 The Android Open Source Project. [Apache license](https://android.googlesource.com/platform/frameworks/opt/colorpicker/+/master/src/com/android/colorpicker/ColorPickerDialog.java).
* [dnsjava](http://www.xbill.org/dnsjava/). Copyright (c) 1998-2011, Brian Wellington. [BSD License](https://sourceforge.net/p/dnsjava/code/HEAD/tree/trunk/LICENSE).
* [OpenPGP API library](https://github.com/open-keychain/openpgp-api). Copyright (C) 2014-2015 Dominik Schürmann. [Apache License 2.0](https://github.com/open-keychain/openpgp-api/blob/master/LICENSE).
* [Android SQLite support library](https://github.com/requery/sqlite-android). Copyright (C) 2017 requery.io. [Apache License 2.0](https://github.com/requery/sqlite-android/blob/master/LICENSE).
* [App shortcut icon generator](https://romannurik.github.io/AndroidAssetStudio/icons-app-shortcut.html). Copyright ???. [Apache License 2.0](https://github.com/romannurik/AndroidAssetStudio/blob/master/LICENSE).

## License

[GNU General Public License version 3](https://www.gnu.org/licenses/gpl.txt)

Copyright (c) 2018 Marcel Bokhorst. All rights reserved

FairEmail is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FairEmail is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FairEmail. If not, see [https://www.gnu.org/licenses/](https://www.gnu.org/licenses/).
