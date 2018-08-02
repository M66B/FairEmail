Safe email
==========

Frequently Asked Questions
--------------------------

<a name="FAQ1"></a>
**(1) Which email providers are supported?**

* Gmail
* Outlook

<a name="FAQ2"></a>
**(2) What is a valid security certificate?**

Valid security certificates are officially signed (not self signed) and have matching a host name.

<a name="FAQ3"></a>
**(3) Which permissions are needed and why?**

* Full network access (INTERNET): to send and receive email
* View network connections (ACCESS_NETWORK_STATE): to monitor internet connectivity changes
* Run at startup (RECEIVE_BOOT_COMPLETED): to start monitoring on device start

<a name="FAQ4"></a>
**(4) What are operations?**

The low priority status bar notification shows the number of pending operations, which can be:

* Mark message as seen/unseen in remote folder
* Add message to remote folder
* Move message to another remote folder
* Delete message from remote folder
* Send message

<a name="FAQ5"></a>
**(5) What happens if a message could not be sent?**

If a message could not be sent, it will be placed in the drafts folder again.

<br>

If you have another question, you can use [this forum](https://forum.xda-developers.com/).
