# Kurulum yardımı

FairEmail'i kurmak oldukça basittir. E-posta almak için en az bir hesap, e-posta göndermek istiyorsanız en az bir kimlik eklemeniz gerekir. The quick setup will add an account and an identity in one go for most major providers.

## Gereksinimler

Hesapları ve kimlikleri ayarlamak için bir internet bağlantısı gereklidir.

## Hızlı Kurulum

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Hesap kurulumu - e-posta almak için

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Listeden bir sağlayıcı seçin, çoğunlukla e-posta adresiniz olan kullanıcı adını ve şifrenizi girin. FairEmail'in e-posta sunucusuna bağlanmasını ve sistem klasörlerinin bir listesini almasını sağlamak için *Denetle* düğmesine dokunun. Sistem klasörü seçimini inceledikten sonra *Kaydet* düğmesine dokunarak hesabı ekleyebilirsiniz.

If your provider is not in the list of providers, there are thousands of providers, select *Custom*. Alan adını girin, örneğin *gmail.com* ve *Ayarları al* düğmesine dokunun. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Bunun hakkında daha fazlası için lütfen [burada](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts)'ya bakın.

## Kimlik kurulumu - e-posta göndermek için

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Göndermek istediğiniz e-posta adresinden karşıda görünmesini istediğiniz adı girin ve bağlı bir hesap seçin. Kimliği eklemek için *Kaydet*'e dokunun.

Hesap elle yapılandırıldıysa, kimliği de elle yapılandırmanız gerekir. Alan adını girin, örneğin *gmail.com* ve *Ayarları al* düğmesine dokunun. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Takma ad kullanma hakkında [bu SSS](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9)'a bakın.

## Verilen izinler - kişi bilgilerine erişmek için

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Pil eniyileştirme ayarları - sürekli e-posta almak için

Son Android sürümlerinde batarya kullanımını azaltmak için ekran bir süre kapalı kaldığında uygulamalar uyku moduna geçer. Gecikmesiz yeni e-postalar almak istiyorsanız, FairEmail için pil eniyileştirmelerini devre dışı bırakmalısınız. Tap *Manage* and follow the instructions.

## Sorular veya sorunlar

Bir sorunuz veya probleminiz varsa, lütfen yardım için [buraya bakın](https://github.com/M66B/FairEmail/blob/master/FAQ.md).