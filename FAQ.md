Safe email
==========

Frequently Asked Questions
--------------------------

<a name="FAQ1"></a>
**(1) Which permissions are needed and why?**

* Full network access (INTERNET): to send and receive email
* View network connections (ACCESS_NETWORK_STATE): to monitor internet connectivity changes
* Run at startup (RECEIVE_BOOT_COMPLETED): to start monitoring on device start

<a name="FAQ2"></a>
**(2) What are operations?**

The low priority status bar notification shows the number of pending operations, which can be:

* Mark message as seen/unseen in remote folder
* Add message to remote folder
* Move message to another remote folder
* Delete message from remote folder
* Send message
* Download attachment

<a name="FAQ3"></a>
**(3) What is a valid security certificate?**

Valid security certificates are officially signed (not self signed) and have matching a host name.

<a name="FAQ4"></a>
**(4) Why is IMAP IDLE required?**

Without [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE) emails need to be periodically fetched,
which is a waste of battery power and internet bandwidth and will delay notification of new emails.

<br>

If you have another question, you can use [this forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168).
