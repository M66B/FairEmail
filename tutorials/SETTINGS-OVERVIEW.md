# Settings overview

&#x1F30E; [Google Translate](https://translate.google.com/translate?sl=en&u=https%3A%2F%2Fgithub.com%2FM66B%2FFairEmail%2Fblob%2Fmaster%2Ftutorials%2FSETTINGS-OVERVIEW.md)

In this tutorial, some settings and options in FairEmail will be described without going into too many details.
Be reminded that all settings here are optional. FairEmail works without any modifications.
You can simply add an account and start reading and writing your emails.

**NOTE:** If you have any troubles, check [this extensive FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md).
You may also ask in [this XDA Forum thread](https://forum.xda-developers.com/t/app-5-0-fairemail-fully-featured-open-source-privacy-oriented-email-app.3824168/)
or via [this contact form](https://contact.faircode.eu/?product=fairemailsupport). Bad reviews won't help you or the developer, so please try asking in person first.

**NOTE:** A tutorial on the first configuration of the app and on how to add an account can be found in the [first configuration tutorial](https://github.com/M66B/FairEmail/blob/master/tutorials/FIRST-CONFIG.md)

## Opening the settings

To open the settings, tap the three dashes in the top left corner (hamburger menu) and select "Settings" with the gear icon.
That's it, you're already within the settings.

You will now find multiple categories:

* **Main** - To add a new account
* **Receive** - To adjust the synchronizing and checking of incoming mails
* **Send** - Options related to writing and sending an email
* **Connection** - Options related to using your internet connection and downloading messages
* **Display** - To adjust how the app and the messages look like
* **Behavior** - To adjust how the app should behave
* **Privacy** - To enable / disable options that increase your privacy
* **Encryption** - Options related to encrypted messaging (PGP & S/MIME)
* **Notifications** - Options related to the notifications by FairEmail
* **Miscellaneous** - Some other options such as error reporting

## Index

1. [Adding a new account](#1-adding-a-new-account)
2. [Receiving messages in the daytime only](#2-receiving-messages-in-the-daytime-only)
3. [Enable checking for spam](#3-enable-checking-for-spam)
4. [Edit your email signature](#4-edit-your-email-signature)
5. [Limit mobile data usage](#5-limit-mobile-data-usage)
6. [Changing the theme](#6-changing-the-theme)
7. [Changing start screen](#7-changing-start-screen)
8. [Show an avatar for each sender](#8-show-an-avatar-for-each-sender)
9. [Enable biometric authentication](#9-enable-biometric-authentication)
10. [Enable error reporting](#10-enable-error-reporting)

## 1. Adding a new account

If you want to add a new account, use the wizard in the Main screen. Follow [this first configuration guide](https://github.com/M66B/FairEmail/blob/master/tutorials/FIRST-CONFIG.md).
You can also [add an account manually](https://github.com/M66B/FairEmail/blob/master/tutorials/MANUAL-CONFIG.md). That requires you to enter everything manually, so it's not recommended.

## 2. Receiving messages in the daytime only

You might want to not receive any messages at late hours. It is very easy and straightforward to tell FairEmail to synchronize only during specified days and hours.
You can also exclude some accounts from this scheduling such that they are always synchronized regardless of the day and time.

More information on scheduling can be found in [FAQ #78 How do I use schedules?](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq78).

### 2.1 Set synchronization day & time

Follow the following steps:

1. Go to the "Receive" settings
2. Enable the option "Schedule"
3. Set the days and time-frame messages should be synchronized

### 2.2 Exclude an account from the schedule

You might want to exclude one specific account from the schedule and always receive messages regardless of the day and time.
If so, follow these steps:

1. Go to the "Main" settings
2. Click on "Manual setup and account options" below the wizard
3. Press the "Accounts" button
4. Select the account
5. Scroll down until you find the "Advanced" options (gear icon) and press the button
6. Enable the option "Synchronize outside the schedule too"


## 3. Enable checking for spam

FairEmail can perform various checks on incoming mails. This can be enabled in the "Receive" settings within the "Checks" options.

1. Go to the "Receive" settings
2. Scroll down to "Checks"
3. Enable the option "Check if the sender's domain name is on a spam block list"

**NOTE**: The status of the performed checks can be indicated through an icon for each message.
To enable this, open the "Display" settings enable showing warnings and the status indicator under "Advanced".

## 4. Edit your email signature

The email signature is the text that is always added below the messages you send. The signatures can vary for each identity you send emails from.
To add or edit your signature, do as follows:

1. Go to the "Main" settings
2. Click on "Manual setup and account options" below the wizard
3. Press the "Identities" button
4. Press the "Edit signature" button
5. Enter or edit your signature and save it

## 5. Limit mobile data usage

You can set up whether to use metered connections such as mobile data and how much kilobytes of data to use per message.
You can also disable downloading messages when roaming.

Go to the "Connection" settings and you will find the following "General" options:

* Use metered connections - using mobile data and paid Wi-Fi hotspots
* Download while roaming
* Roam like at home - Roaming within the EU won't be considered as roaming

More information can be found in [FAQ #105 How does the roam-like-at-home option work?](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq105).

## 6. Changing the theme

You can slightly change the way the app looks. To this end, FairEmail comes with multiple pre-installed themes.
You can switch themes like this:

1. Open the menu by tapping the three dashes in the top left corner (hamburger menu)
2. Choose "Settings"
3. At the top bar, select the tab called "Display"
4. Press the "Select theme" button
5. Select the theme of your choice and confirm

**NOTE:** Dynamic themes are not possible on Android.
See [FAQ #164 Can you add customizable themes?](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq164) for further details.

## 7. Changing start screen

By default you will see a unified inbox on your start screen, which will summarize the inboxes of all your mail accounts in FairEmail.
If you prefer to have a classic account view on start, maybe because you manage a greater number of email accounts with FairEmail,
you can do this as follows:

1. Open the menu by tapping the three dashes in the top left corner (hamburger menu)
2. Choose "Settings"
3. At the top bar, select the tab called "Display"
4. Go to the option "Show on start screen" and select an available option within the dropdown menu
5. Use the back key to get back to the start screen

## 8. Show an avatar for each sender

There are multiple ways to show an avatar for each sender. You can enable multiple at once.

1. Go to the "Display" settings
2. Scroll down to the "Message header" section
3. Enable the options of your choice

**NOTE:** Apart from the generated icons, there might be a privacy risk with each available option.
Gravatars and Libravatar might not be available in the PlayStore version as Google falsely flags the app to be a spyware otherwise. Also see [this XDA thread](https://forum.xda-developers.com/t/app-5-0-fairemail-fully-featured-open-source-privacy-oriented-email-app.3824168/page-1125#post-87018167) on the matter.

## 9. Enable biometric authentication

You might want to protect your app via a PIN or a fingerprint to ensure no unauthorized person gains access to your messages.
To do so, follow these steps:

1. Go to the "Privacy" settings
2. Press on the "PIN" or "Enable fingerprint" button
3. Follow the instruction on your screen

More information can be found in [FAQ #113 How does biometric authentication work?](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq113).

## 10. Enable error reporting

Error reporting is disabled by default. However, enabling this helps the developer finding any bugs and improving the app.
If you want to enable error reporting, do as follows:

1. Go to the "Miscellaneous" settings
2. Enable the option "Send error reports"

More information can be found in [FAQ #104 What do I need to know about error reporting?](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq104).

<br>

Thanks @[mkasimd](https://github.com/mkasimd/) for contributing this documentation.
