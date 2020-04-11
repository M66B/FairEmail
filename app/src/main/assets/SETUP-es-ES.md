# Ayuda de la configuración

Configurar FairEmail es bastante sencillo. Deberá añadir al menos una cuenta para recibir correo electrónico y al menos una identidad si quiere enviarlo. The quick setup will add an account and an identity in one go for most major providers.

## Requisitos

Se requiere una conexión a Internet para configurar cuentas e identidades.

## Configuración rápida

Sólo tiene que introducir su nombre, dirección de correo electrónico, y contraseña, y pulsar *Ir*.

Esto funcionará para la mayoría de los proveedores más populares.

Si la configuración rápida no funciona, necesitará configurar una cuenta y una identidad de otra manera (ver abajo para las instrucciones).

## Configurar cuenta - para recibir correo

Para añadir una cuenta, pulse *Administrar cuentas* y pulse el botón *añadir* naranja en la parte inferior. Seleccione un proveedor de la lista, introduzca su nombre de usuario (que es por lo general su dirección de correo) e introduzca su contraseña. Pulse *Comprobar* para permitir que FairEmail se conecte al servidor de correo y obtenga una lista de carpetas de sistema. Luego de revisar la selección de carpetas de sistema puede añadir la cuenta pulsando *Guardar*.

Si su proveedor no está en la lista de proveedores, seleccione *Personalizado*. Introduzca el nombre de dominio, por ejemplo *gmail.com* y pulse *Obtener configuración*. Si su proveedor soporta [auto-descubrimiento](https://tools.ietf.org/html/rfc6186), FairEmail rellenará el nombre de host y el número de puerto. En caso contrario compruebe el nombre de host IMAP, número de puerto, y protocolo (SSL/TLS o STARTTLS) en las instrucciones de configuración de su proveedor. Para más información, por favor vaya [aquí](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configurar identidad - para enviar correo

De manera similar, para añadir una identidad, pulse *Administrar identidad* y pulse el botón *añadir* naranja en la parte inferior. Introduzca el nombre que desea que aparezca en los correos que envía y seleccione una cuenta vinculada. Pulse *Guardar* para añadir la identidad.

Si la cuenta fue configurada manualmente, probablemente necesita configurar la identidad manualmente también. Introduzca el nombre de dominio, por ejemplo *gmail.com* y pulse *Obtener configuración*. Si su proveedor soporta [auto-descubrimiento](https://tools.ietf.org/html/rfc6186), FairEmail rellenará el nombre de host y el número de puerto. En caso contrario comprueba el nombre de host SMTP, número de puerto, y protocolo (SSL/TLS o STARTTLS) en las instrucciones de configuración de su proveedor.

Vea [esta FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sobre el uso de alias.

## Conceder permisos - para acceder a la información de contacto

Si desea buscar direcciones de correo electrónico, que se muestren fotos de contactos, etc, necesitará conceder permisos de lectura de contactos a FairEmail. Simplemente pulse *Conceder permisos* y seleccione *Permitir*.

## Configurar optimizaciones de batería - para recibir correo continuamente

En versiones recientes de Android, éste pondrá aplicaciones a dormir cuando la pantalla esté apagada durante algún tiempo para reducir el uso de batería. Si desea recibir nuevos correos sin retrasos, debería desactivar las optimizaciones de batería para FairEmail. Pulse *Desactivar optimizaciones de batería* y siga las instrucciones.

## Preguntas o problemas

Si tiene alguna pregunta o problema, por favor [consulte aquí](https://github.com/M66B/FairEmail/blob/master/FAQ.md) para obtener ayuda.