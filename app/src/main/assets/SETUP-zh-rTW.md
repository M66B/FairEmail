# 設定說明

設定FairEmail相當簡單。 你需要新增至少一個帳號來接收電子郵件，如果想要寄電子郵件則需要新增至少一個身分。 這個快速設定將會新增一個適用於大部分電子郵件服務商的帳戶和身分。

## 需求

需要一個網際網路連線來設定帳戶和身分。

## 快速設定

只需要輸入你的名字、電子郵件地址和密碼然後按一下*Go*。

這將適用於大部分的電子郵件服務商。

如果快速設定失敗，你則需要用其他方法來設定帳戶和身分，請看以下說明。

## 設定帳戶以接收電子郵件

若要增加一個帳戶，按一下*帳戶管理*然後點一下位於下方的橘色*新增*按鈕。 Select a provider from the list, enter the username, which is mostly your email address and enter your password. Tap *Check* to let FairEmail connect to the email server and fetch a list of system folders. After reviewing the system folder selection you can add the account by tapping *Save*.

If your provider is not in the list of providers, select *Custom*. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the hostname and port number, else check the setup instructions of your provider for the right IMAP hostname, port number and protocol (SSL/TLS or STARTTLS). For more about this, please see [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Set up identity - to send email

Similarly, to add an identity, tap *Manage identity* and tap the orange *add* button at the bottom. Enter the name you want to appear in the from address of the emails you send and select a linked account. Tap *Save* to add the identity.

If the account was configured manually, you likely need to configure the identity manually too. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the hostname and port number, else check the setup instructions of your provider for the right SMTP hostname, port number and protocol (SSL/TLS or STARTTLS).

See [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) about using aliases.

## Grant permissions - to access contact information

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant read contacts permission to FairEmail. Just tap *Grant permissions* and select *Allow*.

## Setup battery optimizations - to continuously receive emails

On recent Android versions, Android will put apps to sleep when the screen is off for some time to reduce battery usage. If you want to receive new emails without delays, you should disable battery optimizations for FairEmail. Tap *Disable battery optimizations* and follow the instructions.

## 問題

如果你有任何問題，請見[這裡](https://github.com/M66B/FairEmail/blob/master/FAQ.md)來尋求幫助。