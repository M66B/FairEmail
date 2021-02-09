# Axuda da configuración

Configurar FairEmail é sinxelo. Precisará polo menos unha conta para recibir correo e polo menos unha identidade se quere enviar correo. The quick setup will add an account and an identity in one go for most major providers.

## Requisitos

Requírese unha conexión a internet para configurar as contas e identidades.

## Configuración rápida

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Crear unha conta - para recibir correo

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Seleccione un provedor da lista, escriba o nome de usuario, que moitas veces será o seu enderezo de correo, e logo escriba o seu contrasinal. Toque en *Comprobar* para que FairEmail conecte ao servidor de correo e colla a lista de cartafoles de sistema. Despois de revisar a selección de cartafoles de sistema, pode engadir a conta tocando *Gardar*.

If your provider is not in the list of providers, there are thousands of providers, select *Custom*. Escriba o nome do dominio, por exemplo *gmail.com* e toque en *Obter configuración*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Para máis información, vexa [aquí](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Crear unha identidade - para enviar correo

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Escriba o nome que quere que apareza no remitente dos correos que envíe e seleccione a conta relacionada. Toque *Gardar* para engadir a identidade.

Se a conta foi configurada manualmente, é probable que teña que configurar tamén a identidade de xeito manual. Escriba o nome do dominio, por exemplo *gmail.com* e toque en *Obter configuración*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Vexa [esta pregunta frecuente](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sobre o uso de alcumes.

## Conceder permisos - para acceder á información dos contactos

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Configuración de optimizacións da batería - para recibir correos de maneira continua

Nas versións recentes de Android, Android pon a durmir ás aplicacións cando a pantalla leva apagada un tempo para reducir o consumo de batería. Se quere recibir as mensaxes sen retardos, pode desactivar as optimizacións da batería para FairEmail. Tap *Manage* and follow the instructions.

## Preguntas ou problemas

Se ten algunha dúbida ou problema, [vaia aquí](https://github.com/M66B/FairEmail/blob/master/FAQ.md) para atopar axuda.