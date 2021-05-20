# 초기설정 도움말

FairEmail을 설정하는 것은 꽤 간단합니다. 이메일을 받으려면 계정을 하나 이상 추가해야 하며, 이메일을 보내려면 ID를 하나 이상 추가해야 합니다. 빠른 초기 설정은 대부분의 주요 제공업체에 대해 한 번에 계정과 ID를 추가할 것입니다.

## 요구 사항

계정과 신분을 설정하기 위해서는 인터넷 연결이 필요합니다.

## 빠른 설정

적합한 제공업체나 *기타 제공업체*를 선택하고 이름, 이메일 주소, 비밀번호를 입력한 뒤 *확인*을 누릅니다.

대부분의 주요 이메일 제공업체에는 이 방법을 사용하면 됩니다.

빠른 설정이 작동하지 않으면 다른 방법으로 계정 및 ID를 설정해야 합니다. 자세한 내용은 아래를 참조하십시오.

## 계정 설정 - 이메일 수신

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Select a provider from the list, enter the username, which is mostly your email address and enter your password. Tap *Check* to let FairEmail connect to the email server and fetch a list of system folders. After reviewing the system folder selection you can add the account by tapping *Save*.

If your provider is not in the list of providers, there are thousands of providers, select *Custom*. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). For more about this, please see [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## ID 설정 - 이메일 발신

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Enter the name you want to appear in the from address of the emails you send and select a linked account. Tap *Save* to add the identity.

If the account was configured manually, you likely need to configure the identity manually too. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

See [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) about using aliases.

## Grant permissions - to access contact information

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Setup battery optimizations - to continuously receive emails

On recent Android versions, Android will put apps to sleep when the screen is off for some time to reduce battery usage. If you want to receive new emails without delays, you should disable battery optimizations for FairEmail. Tap *Manage* and follow the instructions.

## Questions or problems

If you have a question or problem, please [see here](https://github.com/M66B/FairEmail/blob/master/FAQ.md) for help.