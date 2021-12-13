# Помощ при инсталиране

Настройването на FairEmail е сравнително лесно. Трябва да добавите поне един профил за получаване на поща и поне една идентичност за изпращане на поща. The quick setup will add an account and an identity in one go for most major providers.

## Изисквания

Изисква се интернет връзка за настройка на профили и идентичности.

## Бързи настройки

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

Това ще работи за основните доставчици на имейл услуги.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Настройка на профил - за получаване на имейл

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Изберете доставчик от списъка, въведете потребител, който обикновено е вашият имейл адрес и въведете парола. Натиснете *Проверка*, за да може FairEmail да се свърже с имейл сървъра и да извлече списъка със системни папки. След като прегледате системните папки можете да добавите акаунта, като натиснете * Запази*.

If your provider is not in the list of providers, there are thousands of providers, select *Custom*. Въведете име на домейн, например *gmail.com* и натиснете *Получи настройки*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). За повече информация, моля вижте [тук](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Настройка на идентичност - за изпращане на имейл

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Въведете името, което искате да се покаже в адреса на имейлите, които изпращате, и изберете свързан акаунт. Натисни *Запази* за добавяне на идентичност.

Ако акаунтът е бил конфигуриран ръчно, вероятно ще трябва да конфигурирате и самоличността ръчно. Въведете име на домейн, например *gmail.com* и натиснете *Получи настройки*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Виж [ЧЗВ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) относно използването на псевдоним.

## Предоставяне на разрешения - за достъп до контакти

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Настройте оптимизацията на батерията - за непрекъснато получаване на имейли

В последните версии Android поставя приложенията в спящ режим когато екранът е изключен за известно време за да намали разхода на батерията. Ако искате да получавате нови имейли без закъснения, трябва да деактивирате оптимизациите на батерията за FairEmail. Tap *Manage* and follow the instructions.

## Въпроси и проблеми

Ако имате въпрос или проблем, моля, [ вижте тук ](https://github.com/M66B/FairEmail/blob/master/FAQ.md) за помощ.