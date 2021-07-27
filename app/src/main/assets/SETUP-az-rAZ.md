# Quraşdırma köməyi

FairEmail-i quraşdırmaq olduqca sadədir. E-poçt almaq üçün ən azı bir hesab və e-poçt göndərmək istəsəniz ən azı bir kimlik əlavə etməyiniz lazımdır. The quick setup will add an account and an identity in one go for most major providers.

## Tələblər

Hesabları və kimlikləri qurmaq üçün bir internet bağlantısı tələb olunur.

## Cəld quraşdırma

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Hesabın quraşdırılması - e-poçt almaq

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Siyahıdan bir təchizatçı seçin, əsasən e-poçt ünvanınız olan olan istifadəçi adınızı və şifrənizi daxil edin. FairEmail ilə e-poçt serveri arasında bağlantı qurulması və sistem qovluqlarının bir siyahısının alınması üçün *Yoxla* düyməsinə toxunun. Sistem qovluq seçiminə nəzər saldıqdan sonra *Saxla* düyməsinə toxunaraq hesab əlavə edə bilərsiniz.

If your provider is not in the list of providers, there are thousands of providers, select *Custom*. Domen adını daxil edin, məsələn *gmail.com* və *Tənzimləmələri al* düyməsinə toxunun. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Daha ətraflı, zəhmət olmasa [bura](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts) baxın.

## Kimlik quraşdırma - e-poçt göndərmə

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Enter the name you want to appear in the from address of the emails you send and select a linked account. Tap *Save* to add the identity.

If the account was configured manually, you likely need to configure the identity manually too. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

See [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) about using aliases.

## Grant permissions - to access contact information

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Setup battery optimizations - to continuously receive emails

On recent Android versions, Android will put apps to sleep when the screen is off for some time to reduce battery usage. If you want to receive new emails without delays, you should disable battery optimizations for FairEmail. Tap *Manage* and follow the instructions.

## Questions or problems

If you have a question or problem, please [see here](https://github.com/M66B/FairEmail/blob/master/FAQ.md) for help.