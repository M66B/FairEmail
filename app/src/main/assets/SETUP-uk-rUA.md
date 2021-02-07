# Довідка з налаштування

Налаштування FairEmail досить просте. Вам потрібно буде додати принаймні один обліковий запис для отримання електронної пошти та принаймні одну особу, якщо ви хочете надіслати електронну пошту. The quick setup will add an account and an identity in one go for most providers.

## Вимоги

Для налаштування облікових записів та ідентифікаційних даних потрібне з’єднання з Інтернетом.

## Швидке налаштування

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Налаштування облікового запису - для отримання електронної пошти

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Виберіть постачальника зі списку, введіть ім’я користувача, яке в основному є вашою адресою електронної пошти, і введіть пароль. Натисніть * Перевірити *, щоб дозволити FairEmail підключитися до сервера електронної пошти та отримати список системних папок. Переглянувши вибір системної папки, ви можете додати обліковий запис, натиснувши * Зберегти *.

If your provider is not in the list of providers (there are thousands of providers), select *Custom*. Введіть ім'я домену, наприклад * gmail.com *, і торкніться * Отримати налаштування *. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Докладніше про це див. [ тут ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Встановити ідентифікатор - для надсилання електронного листа

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Введіть ім’я, яке потрібно відобразити в адресі електронної пошти, яку ви надсилаєте, і виберіть пов’язаний обліковий запис. Торкніться * Зберегти *, щоб додати особу.

Якщо обліковий запис було налаштовано вручну, вам, ймовірно, також потрібно налаштувати ідентифікатор вручну. Введіть ім'я домену, наприклад * gmail.com *, і торкніться * Отримати налаштування *. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Див. [ це поширене запитання ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) про використання псевдонімів.

## Надати дозволи - для доступу до контактної інформації

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Налаштування оптимізації батареї - для постійного отримання електронних листів

В останніх версіях Android Android переводить програми в режим сну, коли екран на якийсь час вимкнено, щоб зменшити споживання батареї. Якщо ви хочете отримувати нові електронні листи без затримок, слід відключити оптимізацію батареї для FairEmail. Tap *Manage* and follow the instructions.

## Запитання чи Проблеми

Якщо у вас є питання чи проблема, [ див. Тут ](https://github.com/M66B/FairEmail/blob/master/FAQ.md) за допомогою.