# مساعدة الإعداد

إعداد FairEmail سهل للغاية. ستحتاج إلى إضافة حساب واحد على الأقل لتلقي البريد الإلكتروني وهوية واحدة على الأقل إذا كنت ترغب في إرسال بريد إلكتروني. سيضيف الإعداد السريع حساباً وهوية واحدة بضغطة واحده لدى معظم مقدمي الخدمات الرئيسيين.

## المتطلبات

يجب أن تكون متصل بالانترنت لإنشاء الحساب والهوية الخاصة بك.

## الإعداد السريع

فقط حدد المزود المناسب أو *المزود الآخر* وأدخل اسمك وعنوان البريد الإلكتروني وكلمة المرور وانقر على *تحقق*.

هذا سوف يعمل لمعظم مقدمي خدمات البريد الإلكتروني.

إذا لم ينجح الإعداد السريع، فستحتاج إلى إعداد حساب وهوية يدوياً، انظر للتعليمات أدناه.

## إعداد الحساب - لتتلقى البريد الإلكتروني

لإضافة حساب، انقر على *الإعداد اليدوي والمزيد من الخيارات*، انقر على *الحسابات* وانقر على زر "زائد" في الأسفل وحدد IMAP (أو POP3). حدد موفر من القائمة، أدخل اسم المستخدم، الذي هو في الغالب عنوان بريدك الإلكتروني وأدخل كلمة المرور الخاصة بك. انقر فوق *تحقق* للسماح لـ FairEmail بالاتصال بخادم البريد الإلكتروني وجلب قائمة مجلدات النظام. After reviewing the system folder selection you can add the account by tapping *Save*.

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