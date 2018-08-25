FairEmail
=========

Frequently Asked Questions
--------------------------

<a name="FAQ1"></a>
**(1) Which permissions are needed and why?**

* Full network access (INTERNET): to send and receive email
* View network connections (ACCESS_NETWORK_STATE): to monitor internet connectivity changes
* Run at startup (RECEIVE_BOOT_COMPLETED): to start monitoring on device start
* Optional: read your contacts (READ_CONTACTS): to autocomplete addresses
* ... (FOREGROUND_SERVICE): to run a foreground service on Android 9 Pie and later, see also the next question.

<a name="FAQ2"></a>
**(2) Why is there a permanent notification shown?**

A permanent status bar notification with the number of accounts being synchronized and the number of operations pending is shown
to prevent Android from killing the service that takes care of receiving and sending email.

Most, if not all, other email apps don't show a notification with the "side effect" that new email is often not or late being reported.

<a name="FAQ3"></a>
**(3) What are operations?**

The low priority status bar notification shows the number of pending operations, which can be:

* SEEN: mark message as seen/unseen in remote folder
* ADD: add message to remote folder
* MOVE: move message to another remote folder
* DELETE: delete message from remote folder
* SEND: send message
* ATTACHMENT download attachment

<a name="FAQ4"></a>
**(4) What is a valid security certificate?**

Valid security certificates are officially signed (not self signed) and have matching a host name.

<a name="FAQ5"></a>
**(5) What does 'no IDLE support' mean?**

Without [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE) emails need to be periodically fetched,
which is a waste of battery power and internet bandwidth and will delay notification of new emails.

<a name="FAQ6"></a>
**(6) How can I login to Gmail / G suite?**

To login to Gmail / G suite you'll often need an app password, for example when two factor authentication is enabled.
See here for instructions: [https://support.google.com/accounts/answer/185833](https://support.google.com/accounts/answer/185833).

<a name="FAQ7"></a>
**(7) Why are messages in the outbox not moved to the sent folder?**

Messages in the outbox are moved to the sent folder as soon as your provider adds the message to the sent folder.
If this doesn't happen, your provider might not keep track of sent messages or you might be using an SMTP server not related to the provider.
In these cases you can enable the account option *Store sent messages* to let the app move messages from the outbox to the sent folder after sending.

<br>

If you have another question, you can use [this forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168).

If you have a feature request or found a bug, you can report it [as an issue](https://github.com/M66B/open-source-email/issues).
