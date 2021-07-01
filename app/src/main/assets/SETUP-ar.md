# مساعدة في الإعدادات

إعداد FairEmail سهل للغاية. You'll need to add at least one account to receive email and at least one identity if you want to send email. سيضيف الإعداد السريع حساباً وستذهب هوية في واحد لمعظم مقدمي الخدمات الرئيسيين.

## المتطلبات

يجب أن تكون متصل بالانترنت لإنشاء الحساب والهوية الخاصة بك.

## إعدادات سريعة

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

هذا سوف يعمل لمعظم مقدمي خدمات البريد الإلكتروني.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## إعداد الحساب - لتلقي البريد الإلكتروني

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Select a provider from the list, enter the username, which is mostly your email address and enter your password. Tap *Check* to let FairEmail connect to the email server and fetch a list of system folders. After reviewing the system folder selection you can add the account by tapping *Save*.

If your provider is not in the list of providers, there are thousands of providers, select *Custom*. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). For more about this, please see [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## إعداد الهوية - لإرسال البريد الإلكتروني

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Enter the name you want to appear in the from address of the emails you send and select a linked account. Tap *Save* to add the identity.

If the account was configured manually, you likely need to configure the identity manually too. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

See [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) about using aliases.

## منح الأذونات - للوصول إلى معلومات الاتصال

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## إعداد تحسينات البطارية - لتلقي رسائل البريد الإلكتروني باستمرار

On recent Android versions, Android will put apps to sleep when the screen is off for some time to reduce battery usage. If you want to receive new emails without delays, you should disable battery optimizations for FairEmail. Tap *Manage* and follow the instructions.

## أسئلة أو مشاكل؟

إذا كان لديك سؤال أو مشكلة، يرجى [الاطلاع هنا](https://github.com/M66B/FairEmail/blob/master/FAQ.md) للحصول على المساعدة.