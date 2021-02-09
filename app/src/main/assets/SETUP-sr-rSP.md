# Помоћ подешавања

Подешавање FairEmail апликације је крајње једноставно. Морате да додате бар један налог да примате поруке и бар један идентитет ако желите да их шаљете. The quick setup will add an account and an identity in one go for most major providers.

## Захтеви

За поставку налога и идентитета је потреба интернет конекција.

## Брзо подешавање

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Подесите налог - да примате поруке

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Одаберите провајдера са листе, унесите корисничко име, што је обично Ваша е-мејл адреса и унесите лозинку. Кликните *Провери* да пустите FairEmail да проба да се повеже на е-мејл сервер и дохвати листу системских фасцикли. После одабира системских фасцикли, можете додати налог кликом на *Сачувај*.

If your provider is not in the list of providers, there are thousands of providers, select *Custom*. Унесите име домена, нпр. *gmail.com* и кликните *Дохвати поставке*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). За више о овоме, погледајте [овде](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Подесите идентитет - да шаљете поруке

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Унесите име које желите да се појављује када други виде Ваш е-мејл и одаберите повезани налог. Кликните *Сачувај* да додате идентитет.

Ако је налог ручно подешен, вероватно ћете требати да подесите и идентитет ручно. Унесите име домена, нпр. *gmail.com* и кликните *Дохвати поставке*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Погледајте [често постављана питања](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) око коришћења алијаса.

## Одобрите овлашћења - да приступите информацијама о контактима

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Подесите оптимизацију батерије - да стално примате е-мејлове

На скорашњим верзијама Андроида, апликација ће бити стављене у режим спавања ако је екран угашен неко време да би се смањила потрошња батерије. Ако желите да примате нове поруке без задршке, треба да искључите оптимизацију батерије за FairEmail апликацију. Tap *Manage* and follow the instructions.

## Имате питања или проблем

Уколико имате питања или проблем, [погледајте овде](https://github.com/M66B/FairEmail/blob/master/FAQ.md) за помоћ.