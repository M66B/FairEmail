# راهنمای راه‌اندازی

راه‌اندازی FairEmail بسیار آسان است. شما برای دریافت ایمیل به حداقل یک حساب و برای ارسال ایمیل حداقل به یک هویت نیاز دارید. راه‌اندازی سریع یک حساب و یک هویت را برای بیشتر ارائه دهندگان عمده خواهد افزود.

## پیش‌نیاز‌ها

یک اتصال اینترنت برای راه‌اندازی حساب‌ها و هویت‌ها مورد نیاز است.

## راه اندازی سریع

Just enter your name, email address and password and tap *Go*.

This will work for most major email providers.

If the quick setup doesn't work, you'll need to setup an account and an identity in another way, see below for instructions.

## راه‌اندازی حساب برای دریافت ایمیل

To add an account, tap *Manage accounts* and tap the orange *add* button at the bottom. Select a provider from the list, enter the username, which is mostly your email address and enter your password. Tap *Check* to let FairEmail connect to the email server and fetch a list of system folders. After reviewing the system folder selection you can add the account by tapping *Save*.

If your provider is not in the list of providers, select *Custom*. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the hostname and port number, else check the setup instructions of your provider for the right IMAP hostname, port number and protocol (SSL/TLS or STARTTLS). For more about this, please see [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## راه‌اندازی هویت برای ارسال ایمیل

Similarly, to add an identity, tap *Manage identity* and tap the orange *add* button at the bottom. Enter the name you want to appear in the from address of the emails you send and select a linked account. Tap *Save* to add the identity.

If the account was configured manually, you likely need to configure the identity manually too. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the hostname and port number, else check the setup instructions of your provider for the right SMTP hostname, port number and protocol (SSL/TLS or STARTTLS).

See [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) about using aliases.

## اجازه دسترسی به اطلاعات مخاطبین را بدهید

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant read contacts permission to FairEmail. Just tap *Grant permissions* and select *Allow*.

## بهینه‌سازی باتری را تنظیم کنید تا به طور مداوم ایمیل‌ها را دریافت کنید

On recent Android versions, Android will put apps to sleep when the screen is off for some time to reduce battery usage. If you want to receive new emails without delays, you should disable battery optimizations for FairEmail. Tap *Disable battery optimizations* and follow the instructions.

## سوالات یا مشکلات

If you have a question or problem, please [see here](https://github.com/M66B/FairEmail/blob/master/FAQ.md) for help.