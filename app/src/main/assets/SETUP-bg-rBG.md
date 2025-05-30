# Помощ при настройване

Настройването на FairEmail е сравнително лесно. Ще трябва да добавите поне един акаунт за получаване на е-поща и поне една самоличност, ако искате да изпращате е-писмо. Бързите настройки ще добавят акаунт и самоличност наведнъж за повечето големи доставчици.

## Изисквания

Изисква се интернет връзка за настройка на акаунтите и самоличности.

## Бързи настройки

Просто изберете подходящ доставчик или *Друг доставчик* и въведете своето име, имейл адрес и парола и чукнете на *Проверка*.

Това ще работи за основните доставчици на имейл услуги.

Ако бързата настройка не работи, то ще трябва да настроите акаунта и самоличността ръчно, вижте по-долу за указания.

## Настройка на акаунт - за получаване на имейл

Чукнете на *Ръчна настройка и още опции*, за да добавите акаунт, чукнете *Акаунти* и чукнете на долното копче „плюс“ и изберете IMAP (или POP3). Изберете доставчик от списъка, въведете потребител, който обикновено е вашият имейл адрес и въведете парола. Натиснете *Проверка*, за да позволите на FairEmail да се свърже със сървъра на е-поща и да се извлече списъка със системни папки. След като прегледате системните папки можете да добавите акаунта, като натиснете * Запази*.

Ако доставчикът ви не е вписан сред доставчиците, то има хиляди други, изберете *Персонализиране*. Въведете име на домейн, например *gmail.com* и натиснете *Получи настройки*. Ако доставчикът ви поддържа [автоматично откриване](https://tools.ietf.org/html/rfc6186), то FairEmail ще попълни името на хоста и номера на порта, инак проверете указанията за настройка на доставчика си за точно име на хоста IMAP, номер на порта и протокол за криптиране (SSL/TLS или STARTTLS). За повече информация, моля вижте [тук](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Настройка на самоличност- за изпращане на имейл

Аналогично, за да добавите самоличност, чукнете на *Ръчна настройка и още опции*, тогава на *Самоличности* и долното копче „плюс“. Въведете името, което искате да се покаже в адреса на имейлите, които изпращате, и изберете свързан акаунт. Изберете *Запазване* за добавяне на самоличност.

Ако акаунтът е бил конфигуриран ръчно, вероятно ще трябва да конфигурирате и самоличността ръчно. Въведете име на домейн, например *gmail.com* и натиснете *Получи настройки*. Ако доставчикът ви поддържа [автоматично откриване](https://tools.ietf.org/html/rfc6186), то FairEmail ще попълни името на хоста и номера на порта, инак проверете указанията за настройка на доставчика си за точно име на хоста SMTP, номер на порта и протокол за криптиране (SSL/TLS или STARTTLS).

Вижте [ЧЗВ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) относно употребата на псевдоним.

## Предоставяне на разрешения - за достъп до контакти

Ако искате да търсите имейл адреси, да показвате снимки на контакти и т.н., ще трябва да дадете позволение за четене на сведенията за контакт на FairEmail. Натиснете *Разрешаване* и изберете *Позволение*.

## Настройте оптимизацията на батерията - за непрекъснато получаване на имейли

В последните версии Android поставя приложенията в спящ режим когато екранът е изключен за известно време за да намали разхода на батерията. Ако искате да получавате нови имейли без закъснения, трябва да деактивирате оптимизациите на батерията за FairEmail. Чукнете *Управление* и последвайте указанията.

## Въпроси и проблеми

Ако имате въпрос или проблем, моля, [ вижте тук ](https://github.com/M66B/FairEmail/blob/master/FAQ.md) за помощ.