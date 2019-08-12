# Ayuda de la configuración

Configurar FairEmail es bastante sencillo. Deberás añadir al menos una cuenta para recibir correo electrónico y al menos una identidad si quieres enviarlo. La configuración rápida añadirá una cuenta y una identidad en un sólo paso para la mayoría de los proveedores más populares.

## Requisitos

Se requiere una conexión a Internet para configurar cuentas e identidades.

## Configuración rápida

Sólo tienes que introducir tu nombre, dirección de correo electrónico, y contraseña, y pulsar *Ir*.

Esto funcionará para la mayoría de los proveedores más populares.

Si la configuración rápida no funciona, necesitarás configurar una cuenta y una identidad de otra manera (ver abajo para las instrucciones).

## Configurar cuenta - para recibir correo

Para añadir una cuenta, pulse *Administrar cuentas* y pulse el botón *añadir* naranja en la parte inferior. Selecciona un proveedor de la lista, introduce tu nombre de usuario (que es por lo general tu dirección de correo) e introduce tu contraseña. Pulsa *Check* para permitir que FairEmail se conecte al servidor de correo y obtenga una lista de carpetas de sistema. Luego de revisar la selección de carpetas de sistema puedes añadir la cuenta pulsando *Guardar*.

Si tu proveedor no está en la lista de proveedores, selecciona *Personalizado*. Introduce el nombre de dominio, por ejemplo *gmail.com* y pulsa *Obtener configuración*. Si tu proveedor soporta [auto-descubrimiento](https://tools.ietf.org/html/rfc6186), FairEmail rellenará el nombre de host y el número de puerto. En caso contrario comprueba el nombre de host IMAP, número de puerto, y protocolo (SSL/TLS o STARTTLS) en las instrucciones de configuración de tu proveedor. Para más información, por favor ve [aquí](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configurar identidad - para enviar correo

De manera similar, para añadir una identidad, pulse *Administrar identidad* y pulse el botón *añadir* naranja en la parte inferior. Introduce el nombre que deseas que aparezca en los correos que envías y selecciona una cuenta vinculada. Pulsa *Guardar* para añadir la identidad.

Si la cuenta fue configurada manualmente, probablemente necesitas configurar la identidad manualmente también. Introduce el nombre de dominio, por ejemplo *gmail.com* y pulsa *Obtener configuración*. Si tu proveedor soporta [auto-descubrimiento](https://tools.ietf.org/html/rfc6186), FairEmail rellenará el nombre de host y el número de puerto. En caso contrario comprueba el nombre de host SMTP, número de puerto, y protocolo (SSL/TLS o STARTTLS) en las instrucciones de configuración de tu proveedor.

Ve [esta FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sobre el uso de alias.

## Conceder permisos - para acceder a la información de contacto

Si deseas buscar direcciones de correo electrónico, que se muestren fotos de contactos, etc, necesitarás conceder permisos de lectura de contactos a FairEmail. Simplemente pulsa *Conceder permisos* y selecciona *Permitir*.

## Configurar optimizaciones de batería - para recibir correo continuamente

En versiones recientes de Android, éste pondrá aplicaciones a dormir cuando la pantalla esté apagada durante algún tiempo para reducir el uso de batería. Si deseas recibir nuevos correos sin retrasos, deberías desactivar las optimizaciones de batería para FairEmail. Pulsa *Desactivar optimizaciones de batería* y sigue las instrucciones.

## Preguntas o problemas

Si tienes una pregunta o problema, por favor [ve aquí](https://github.com/M66B/FairEmail/blob/master/FAQ.md) o utiliza [este formulario de contacto](https://contact.faircode.eu/?product=fairemailsupport) para pedir ayuda (puedes utilizar el número de transacción "*ayuda de configuración*").