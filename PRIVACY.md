# Privacy policy

[&#x1F30E; Google Translate](https://translate.google.com/translate?hl=&sl=en&u=https%3A%2F%2Fgithub.com%2FM66B%2FFairEmail%2Fblob%2Fmaster%2FPRIVACY.md)

<br />

First of all, FairEmail's main goal is to help you protect your privacy.
What follows is a complete overview of all the data that **can be** sent to the internet,
which in the end is always your choice and therefore optional (except of course connecting to the email server).

Except for error reports (disabled by default), the app does not send any data to the developer.

## Overview

FairEmail **does not** send account information and message data elsewhere than to your email provider.

FairEmail **does not** allow other apps access to message data without your approval.

FairEmail **does not** require unnecessary permissions.
For more information on permissions, see [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq1).

FairEmail **does** follow the recommendations of [this EFF article](https://www.eff.org/deeplinks/2019/01/stop-tracking-my-emails).

FairEmail is 100 % **open source**, see [the license](https://github.com/M66B/FairEmail/blob/master/LICENSE).

Error reporting via Bugsnag **is opt-in**, see [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq104) for more information.

FairEmail **adheres** to the [Google API Services User Data Policy](https://developers.google.com/terms/api-services-user-data-policy#additional_requirements_for_specific_api_scopes),
including the Limited Use requirements. Google API Services are used only to authenticate Gmail accounts through OAuth.

FairEmail **can use** these services if they are explicitly enabled (off by default) or are explicitly used by you:

* [ipinfo.io](https://ipinfo.io/) &#8211; [Privacy policy](https://ipinfo.io/privacy-policy)
* [Spamhaus](https://www.spamhaus.org/) &#8211; [Privacy policy](https://www.spamhaus.org/organization/privacy/)
* [Spamcop](https://www.spamcop.net/) &#8211; [Privacy policy](https://www.spamcop.net/fom-serve/cache/168.html)
* [Barracuda](https://www.barracudacentral.org/rbl/how-to-use) &#8211; [Privacy policy](https://www.barracuda.com/company/legal/trust-center/data-privacy/privacy-policy)
* [Thunderbird autoconfiguration](https://wiki.mozilla.org/Thunderbird:Autoconfiguration) &#8211; [Privacy policy](https://www.mozilla.org/privacy/)
* [DeepL](https://www.deepl.com/) &#8211; [Privacy policy](https://www.deepl.com/privacy/)
* [LanguageTool](https://languagetool.org/) &#8211; [Privacy policy](https://languagetool.org/legal/privacy)
* [VirusTotal](https://www.virustotal.com/) &#8211; [Privacy policy](https://support.virustotal.com/hc/en-us/articles/115002168385-Privacy-Policy)
* [OpenAI](https://openai.com/) (GitHub version only) &#8211; [Privacy policy](https://openai.com/policies/privacy-policy)
* [Gravatar](https://gravatar.com/) (GitHub version only) &#8211; [Privacy policy](https://automattic.com/privacy/)
* [Libravatar](https://www.libravatar.org/) (GitHub version only)  &#8211; [Privacy policy](https://www.libravatar.org/privacy/)
* [GitHub](https://github.com/) (GitHub version only) &#8211; [Privacy policy](https://docs.github.com/en/site-policy/privacy-policies/github-privacy-statement)

FairEmail **can access** the websites at the domain names of email addresses (username@domain.name)
if [Brand Indicators for Message Identification](https://en.wikipedia.org/wiki/Brand_Indicators_for_Message_Identification) (BIMI)
or [favicons](https://en.wikipedia.org/wiki/Favicon)
were explicitly enabled (off by default).

FairEmail **will access** the website at the link address if you tap the *Fetch title* button in the insert link dialog (from version 1.1905).

FairEmail obviously **will access** the configured email servers.

FairEmail **is** [GDPR compliant](https://gdpr.eu/).

<br />

## Summary of shared data

This table provides a complete overview of all shared data and the conditions under which data will be shared:

| Service/function   | Data sent                                                          | When the data will be sent                                                  |
| ------------------ | ------------------------------------------------------------------ | --------------------------------------------------------------------------- |
| Mozilla autoconfig | Domain name of email address of email accounts                     | Upon configuring an email account with the quick setup wizard               |
| Email server       | Login credentials (email address/password), messages sent          | Upon configuring and using an account or identity and upon sending messages |
| ipinfo.io          | IP (network) address of domain names of links or email addresses   | Upon pressing a button in the link confirmation dialog                      |
| Spamhaus           | IP (network) address of domain names of links or email addresses   | If spam blocklists are enabled, upon receiving a message                    |
| Spamcop            | IP (network) address of domain names of links or email addresses   | If spam blocklists are enabled, upon receiving a message                    |
| Barracuda          | IP (network) address of domain names of links or email addresses   | If spam blocklists are enabled, upon receiving a message                    |
| DeepL              | Received or entered message text and target language code          | If translating is enabled, upon pressing a translate button                 |
| LanguageTool       | Entered message texts                                              | If LanguageTools is enabled, upon long pressing the save draft button       |
| VirusTotal         | [SHA-256 hash](https://en.wikipedia.org/wiki/SHA-2) of attachments | If VirusTotal is enabled, upon long pressing a scan button (*)              |
| VirusTotal         | Attached file contents                                             | If VirusTotal is enabled, upon long pressing an upload button (*)           |
| OpenAI             | Received and entered message texts                                 | Upen pressing a button in a navigation bar (*)                              |
| Gravatar           | [MD5 hash](https://en.wikipedia.org/wiki/MD5) of email addresses   | If Gravatars are enabled, upon receiving a message (*)                      |
| Libravatar         | [MD5 hash](https://en.wikipedia.org/wiki/MD5) of email addresses   | If Libravatars are enabled, upon receiving a message (*)                    |
| GitHub             | None, but see the remarks below                                    | Upon downloading Disconnect's Tracker Protection lists                      |
|                    |                                                                    | Upon checking for updates (*)                                               |
| BIMI               | Domain name of email addresses                                     | If BIMI is enabled, upon receiving a message (*)                            |
| Favicons           | Domain name of email addresses                                     | If favicons are enabled, upon receiving a message                           |
| Link title         | Link address                                                       | Upon pressing a download button in the insert link dialog                   |
| Bugsnag            | Information about warnings and errors                              | If error reporting is enabled, upon detecting an abnormal situation         |

(*) Only available in the GitHub version of the app

All data is sent to improve the user experience in some way,
like to simplify account setup, identify spam and malicious messages, display message and sender information, find bugs and errors, etc.

Note that any internet connection reveals your current [network address](https://en.wikipedia.org/wiki/Network_address).
Also, when downloading content, like images and files, the [browser's user agent string](https://en.wikipedia.org/wiki/User_agent) will be sent.
There is a privacy option to minimize the information being sent, but please be aware that this can result in problems in some cases.

<br />

## Definitions of terms

This section defines some terms and words.
Knowing those terms will help you understand the following sections.

* *Data subject* &#8211; the user of the app
* *Personal data* &#8211; any data the data subject could be identified with
* *Data controller* &#8211; the person / entity providing the app
* *Data processor* &#8211; the person / entity providing the app
* *Sub-processor* &#8211; a third party processing data
* *Data protection officer* &#8211; the person responsible for any privacy related enquiries

<br>

## Contact details

Please feel free to contact me if you have any concerns:

```
FairCode BV
Represented by the managing director Marcel Bokhorst
Van Doesburg-Erf 194
3315 RG Dordrecht
the Netherlands
marcel+privacy@faircode.eu
```

FairCode BV is the data controller.
Its data protection officer is Marcel Bokhorst, reachable via the aforementioned address.
For any legal issues, the place of jurisdiction is Dordrecht, the Netherlands.

<br>

## A. General information on data processing

### I. Scope of personal data processing

This privacy policy / data protection declaration applies to the Android app FairEmail.

The data processor only processes personal data insofar as absolutely required for providing a functioning email client as well as the explicitly requested services.
Users' personal data is usually only processed if required for fulfilling contractual or legal obligations or with the user's consent.

### II Purpose of data processing

The purpose of any data processed is to provide you with the service requested.
The app by default exclusively processes data that is necessary for the proper functioning of the app and its intended purpose of being an email client.

### III. Data storage and data deletion

By default, all data (both personal and non-personal) remains on the data subject's Android device for as long as not explicitly sent or shared by the data subject.
The data stored on the data subject's device can be deleted by the data subject at any time.

### IV. Sub-processors

The services of all sub-processors are disabled by default.
The data subject's data is sent to and processed by sub-processors if and only if explicitly enabled or requested by the data subject.

The sub-processors are:

* [Bugsnag](https://www.bugsnag.com/) &#8211; [Privacy policy](https://docs.bugsnag.com/legal/privacy-policy/)

### V. Permissions

The app only requests permissions that are necessary for the expected behavior of an email app.
For more information on permissions, see [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq1).

### VI. Logging

The app does not send any log entries to the data processor by default.
The error reporting system utilizes Bugsnag and is disabled by default.
See [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq104) for more information.

### VII. Legal basis

FairEmail is fully [GDPR compliant](https://gdpr.eu/). The legal basis for any data processing is Art. 6 (1) a - c GDPR.

<br>

## B. Support requests

### I. Description and scope of data processing

The data subject may contact the data processor to request support through channels offered by the data processor.
When the data subject contacts the data processor, any provided personal data is stored by the data controller.

### II. Purpose of data processing

The personal data is exclusively processed for finding a specific solution to support queries whilst recording and/or processing them.
It is essential in this respect for the data controller to be able to contact the person requesting support.

### III. Sub-processors

The data processor utilizes the services of the following sub-processors in order to process support requests:

* Google LLC, if support request sent via email &#8211; [Privacy policy](https://policies.google.com/privacy?hl=en)
* Amazon Web Services EMEA SARL, if support request sent via the contact form &#8211; [Privacy policy](https://aws.amazon.com/privacy/)

### IV. Legal basis

Any support requests are sent voluntarily by the data subject, including any personal data that might be attached.
As such, the explicit consent as outlined in Art. 6 (1) a GDPR forms the legal basis for processing.

Copyright &copy; 2018-2023 Marcel Bokhorst.
