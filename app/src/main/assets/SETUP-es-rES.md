# Ayuda de la configuración

Configurar FairEmail es bastante sencillo. Deberá añadir al menos una cuenta para recibir correo electrónico y al menos una identidad si quiere enviarlo. The quick setup will add an account and an identity in one go for most providers.

## Requisitos

Se requiere una conexión a Internet para configurar cuentas e identidades.

## Configuración rápida

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Configurar cuenta - para recibir correo

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Seleccione un proveedor de la lista, introduzca su nombre de usuario (que es por lo general su dirección de correo) e introduzca su contraseña. Pulse *Comprobar* para permitir que FairEmail se conecte al servidor de correo y obtenga una lista de carpetas de sistema. Luego de revisar la selección de carpetas de sistema puede añadir la cuenta pulsando *Guardar*.

If your provider is not in the list of providers (there are thousands of providers), select *Custom*. Introduzca el nombre de dominio, por ejemplo *gmail.com* y pulse *Obtener configuración*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Para más información, por favor vea [aquí](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configurar identidad - para enviar correo

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Introduzca el nombre que desea que aparezca en los correos que envía y seleccione una cuenta vinculada. Pulse *Guardar* para añadir la identidad.

Si la cuenta fue configurada manualmente, probablemente necesita configurar la identidad manualmente también. Introduzca el nombre de dominio, por ejemplo *gmail.com* y pulse *Obtener configuración*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Vea [estas Preguntas Frecuentes](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sobre el uso de alias.

## Conceder permisos - para acceder a la información de contacto

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Configurar optimizaciones de batería - para recibir correo continuamente

En versiones recientes de Android, éste pondrá aplicaciones a dormir cuando la pantalla esté apagada durante algún tiempo para reducir el uso de batería. Si desea recibir nuevos correos sin retrasos, debería desactivar las optimizaciones de batería para FairEmail. Tap *Manage* and follow the instructions.

## Preguntas o problemas

Si tiene alguna pregunta o problema, por favor [consulte aquí](https://github.com/M66B/FairEmail/blob/master/FAQ.md) para obtener ayuda.