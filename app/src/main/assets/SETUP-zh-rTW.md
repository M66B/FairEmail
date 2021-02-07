# 設定說明

設定FairEmail相當簡單。 你需要新增至少一個帳號來接收電子郵件，如果想要寄電子郵件則需要新增至少一個身分。 The quick setup will add an account and an identity in one go for most providers.

## 需求

需要一個網際網路連線來設定帳戶和身分。

## 快速設定

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## 設定帳戶以接收電子郵件

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). 從列表中選擇一個提供商，輸入用戶名（主要是您的電子郵件地址）並輸入密碼。 點擊*檢查*，以使FairEmail連接到電子郵件服務器並獲取系統文件夾列表。 查看系統文件夾選擇後，您可以通過點擊*保存*添加帳戶。

If your provider is not in the list of providers (there are thousands of providers), select *Custom*. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). For more about this, please see [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## 設置顯示名稱 - 寄送電子郵件

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Enter the name you want to appear in the from address of the emails you send and select a linked account. 輕觸 *保存* 完成新增顯示名稱。

如果帳號為手動設定，您也需要手動設定顯示名稱。 輸入網域名稱，例如 *gmail.com* 之後輕觸 *設定*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

See [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) about using aliases.

## 取得權限 - 存取聯絡人資訊

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## 關閉電池效能最佳化 - 持續接收電子郵件

在近期Android版本中，為了減少電池使用量，Android在關閉螢幕後有時會使應用程式進入睡眠。 如果您想要實時接收新電子郵件通知，您應關閉FailEmail的電池最佳化設定。 Tap *Manage* and follow the instructions.

## 問題

如果你有任何問題，請見[這裡](https://github.com/M66B/FairEmail/blob/master/FAQ.md)來尋求幫助。