# Axuda da configuración

Configurar FairEmail é sinxelo. Precisará polo menos unha conta para recibir correo e polo menos unha identidade se quere enviar correo. A configuración rápida engadirá unha conta e identidade dunha soa vez para a maioría dos provedores.

## Requisitos

Requírese unha conexión a internet para configurar as contas e identidades.

## Configuración rápida

Simplemente seleccione o provedor axeitado ou *Outro provedor* e insira o seu nome, enderezo electrónico e contrasinal, e logo toque *Comprobar*.

Isto funcionará ca maioría dos provedores.

Se a configuración rápida non funciona, terá que configurar a conta e a identidade de outro modo, vexa máis adiante as instrucións.

## Crear unha conta - para recibir correo

Para engadir unha conta, toque *Configuración manual e máis opcións*, toque *Contas* e logo toque o botón '+' ao fondo, e seleccione IMAP (ou POP3). Seleccione un provedor da lista, escriba o nome de usuario, que moitas veces será o seu enderezo de correo, e logo escriba o seu contrasinal. Toque en *Comprobar* para que FairEmail conecte ao servidor de correo e colla a lista de cartafoles de sistema. Despois de revisar a selección de cartafoles de sistema, pode engadir a conta tocando *Gardar*.

Se ou seu provedor non está na lista de provedores, porque hai milleiros deles, seleccione *Personalizado*. Escriba o nome do dominio, por exemplo *gmail.com* e toque en *Obter configuración*. Se o seu servidor ten [auto-descubrimento](https://tools.ietf.org/html/rfc6186), FairEmail encherá o nome do servidor e o porto, de non ser así, lea as instrucións do seu provedor para o nome axeitado de IMAP, o seu porto e o protocolo (SSL/TLS ou STARTTLS). Para máis información, vexa [aquí](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Crear unha identidade - para enviar correo

Da mesma maneira, para engadir unha identidade, toque *Configuración manual e máis opcións*, toque *Identidades* e logo toque o botón '+' no fondo. Escriba o nome que quere que apareza no remitente dos correos que envíe e seleccione a conta relacionada. Toque *Gardar* para engadir a identidade.

Se a conta foi configurada manualmente, é probable que teña que configurar tamén a identidade de xeito manual. Escriba o nome do dominio, por exemplo *gmail.com* e toque en *Obter configuración*. Se o seu servidor ten [auto-descubrimento](https://tools.ietf.org/html/rfc6186), FairEmail encherá o nome do servidor e o porto, de non ser así, lea as instrucións do seu provedor para o nome axeitado de SMTP, o seu porto e o protocolo (SSL/TLS ou STARTTLS).

Vexa [esta pregunta frecuente](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sobre o uso de alcumes.

## Conceder permisos - para acceder á información dos contactos

Se quere buscar enderezos de correo, que se mostren as fotos dos contactos, etc, precisará conceder o permiso para ler os contactos a FairEmail. Simplemente toque *Conceder* e seleccione *Permitir*.

## Configuración de optimizacións da batería - para recibir correos de maneira continua

Nas versións recentes de Android, Android pon a durmir ás aplicacións cando a pantalla leva apagada un tempo para reducir o consumo de batería. Se quere recibir as mensaxes sen retardos, pode desactivar as optimizacións da batería para FairEmail. Toque *Arranxar* e siga as instrucións.

## Preguntas ou problemas

Se ten algunha dúbida ou problema, [vaia aquí](https://github.com/M66B/FairEmail/blob/master/FAQ.md) para atopar axuda.