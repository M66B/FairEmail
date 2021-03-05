# セットアップヘルプ

FairEmailのセットアップは非常に簡単です。 メールを受信するためにはIDを、送信する場合はアカウントをそれぞれ少なくとも1つ設定する必要があります。 クイックセットアップでは、ほとんどの主要プロバイダのアカウントとIDが一度に追加されます。

## 必要な条件

アカウントとIDを設定するには、インターネット接続が必要です。

## クイックセットアップ

該当するプロバイダを選択するか、 *他のプロバイダ* を選択し、名前、メールアドレス、パスワードを入力し、 ** をタップします。

これはほとんどのメールプロバイダーで機能します。

クイックセットアップが機能しない場合は、アカウントとIDを手動で設定する必要があります。手順については以下を参照してください。

## アカウントの設定 - メールを受信するために

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). リストからプロバイダーを選択し、ユーザー名(ほとんどはメールアドレス)、パスワードを入力します。 *確認*をタップしてFairEmailがメールサーバーに接続し、システムフォルダーのリストを取得できるようにします。 システムフォルダーの選択を確認後、*保存*をタップしてアカウントを追加できます。

If your provider is not in the list of providers, there are thousands of providers, select *Custom*. *gmail.com*などのドメイン名を入力し、*設定を取得*をタップします。 If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). 詳細は [こちら](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts)

## IDの設定 - メールを送信するために

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. 送信するメールのアドレスから表示する名前を入力し、リンクされたアカウントを選択します。 *保存*をタップしてIDを追加します。

アカウントを手動で設定した場合、おそらくIDも手動で設定する必要があります。 *gmail.com*などのドメイン名を入力し、*設定を取得*をタップします。 If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

エイリアスの使用については [FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) を確認してください。

## 権限の許可 - 連絡先情報にアクセスするために

メールアドレスを検索したり、連絡先の写真を表示する場合はFairEmailに連絡先の読み取りを許可をする必要があります。 Just tap *Grant* and select *Allow*.

## 電池の最適化の設定 - 継続的にメールを受信するために

最近のAndroidバージョンでは、電池の消費を抑えるために画面オフ後しばらくするとアプリをスリープ状態にします。 新着メールを遅延なく受信するには、FairEmailの電池の最適化を無効にする必要があります。 Tap *Manage* and follow the instructions.

## 質問または問題

質問や問題がある場合は、[こちら](https://github.com/M66B/FairEmail/blob/master/FAQ.md)をご覧ください。