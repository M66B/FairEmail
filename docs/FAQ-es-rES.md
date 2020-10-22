# j*******5@gmail.com

Si tiene una pregunta, compruebe primero las preguntas frecuentes. En la parte inferior encontrará cómo hacer otras preguntas, solicitar funcionalidades y reportar errores.

## Índice

* [Autorizando cuentas](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-authorizing-accounts)
* [¿Cómo ...?](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-howto)
* [Problemas conocidos](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-known-problems)
* [Características planificadas](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-planned-features)
* [Características solicitadas con frecuencia](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-frequently-requested-features)
* [Preguntas frecuentes](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-frequently-asked-questions)
* [Soporte](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-support)

## Autorizando cuentas

En la mayoría de los casos, la configuración rápida será capaz de identificar automáticamente la configuración correcta.

Si la configuración rápida falla, deberá configurar manualmente una cuenta (para recibir correo electrónico) y una identidad (para enviar correo electrónico). Para esto necesitarás las direcciones IMAP y SMTP y números de puerto, si SSL/TLS o STARTTLS deben ser usados y su nombre de usuario (generalmente, pero no siempre, su dirección de correo electrónico) y su contraseña.

Buscar *IMAP* y el nombre del proveedor es generalmente suficiente para encontrar la documentación correcta.

En algunos casos necesitará habilitar el acceso externo a su cuenta y/o utilizar una contraseña especial de aplicación, por ejemplo cuando la autenticación de dos factores esté habilitada.

Para autorización:

* Gmail / G suite, ver [pregunta 6](#user-content-faq6)
* Outlook / Live / Hotmail, ver [pregunta 14](#user-content-faq14)
* Office 365, ver [pregunta 14](#user-content-faq156)
* Microsoft Exchange: ver [pregunta 8](#user-content-faq8)
* Yahoo, AOL y Sky, ver [pregunta 88](#user-content-faq88)
* Apple iCloud, ver [pregunta 148](#user-content-faq148)
* Free.fr, ver [pregunta 157](#user-content-faq157)

Por favor vea [aquí](#user-content-faq22) para mensajes de error y soluciones más comunes.

Preguntas relacionadas:

* [¿Es compatible con OAuth?](#user-content-faq111)
* [¿Por qué ActiveSync no es compatible?](#user-content-faq133)

<a name="howto">

## ¿Cómo ...?

* Cambiar el nombre de la cuenta: Ajustes, paso 1, Gestionar, tocar cuenta
* Cambiar el objetivo del deslizamiento a la izquierda/derecha: Ajustes, comportamiento, Definir acciones de deslizamiento
* Cambiar contraseña: Ajustes, paso 1, Gestionar, tocar cuenta, cambiar contraseña
* Establecer una firma: Ajustes, paso 2, Gestionar, tocar identidad, Editar firma.
* Añadir dirección CC y CCO: toque el icono de la persona al final del asunto
* Ir al mensaje siguiente/anterior en archivar/eliminar: en la configuración de comportamiento desactive *Cerrar conversaciones automáticamente* y seleccione *Ir a la siguiente/anterior conversación* para *Al cerrar una conversación*
* Añadir una carpeta a la bandeja de entrada unificada: mantenga presionada la carpeta en la lista de carpetas y marque *Mostrar en la bandeja de entrada unificada*
* Añadir una carpeta al menú de navegación: mantenga presionada la carpeta en la lista de carpetas y marque *Mostrar en el menú de navegación*
* Cargar más mensajes: mantenga presionada una carpeta en la lista de carpetas, seleccione *Sincronizar más mensajes*
* Eliminar un mensaje, omitiendo la papelera: en el menú de 3 puntos justo encima del texto del mensaje *Eliminar* o, alternativamente, desmarque la carpeta de la papelera en la configuración de la cuenta
* Eliminar una cuenta/identidad: Ajustes paso 1/2, Gestionar, tocar cuenta/identidad, menú de tres puntos, Eliminar
* Eliminar una carpeta: mantenga pulsada la carpeta de la lista de carpetas, Editar propiedades, menú de tres puntos, Eliminar
* Deshacer enviar: Bandeja de salida, toque mensaje, toque el icono deshacer
* Guardar mensajes enviados en la bandeja de entrada: por favor [vea estas Preguntas Frecuentes](#user-content-faq142)
* Cambiar carpetas del sistema: Ajustes, paso 1, gestión, toque cuenta, en la parte inferior
* Importar/Exportar ajustes: Ajustes, menu de navegación

## Problemas conocidos

* ~~Un [error en Android 5.1 y 6](https://issuetracker.google.com/issues/37054851) hace que las aplicaciones muestren a veces un formato de hora incorrecto. Cambiar la configuración de Android *Usar formato de 24 horas* podría resolver temporalmente el problema. Una solución fue añadida.~~
* ~~~Un [error en Google Drive](https://issuetracker.google.com/issues/126362828) hace que los archivos exportados a Google Drive estén vacíos. Google ha corregido esto.~~
* ~~Un [error en AndroidX](https://issuetracker.google.com/issues/78495471) hace que FairEmail ocasionalmente se cierre al mantener presionado o deslizar. Google ha corregido esto.~~
* ~~Un [error en AndroidX ROOM](https://issuetracker.google.com/issues/138441698) causa a veces un cierre con "*... Excepción al computar datos en vivo de base de datos ... No se pudo leer la fila...*". Se añadió una solución alternativa.~~
* Un [error en Android](https://issuetracker.google.com/issues/119872129) hace que FairEmail falle con "*... Notificación errónea publicada ...*" en algunos dispositivos una vez después de actualizar FairEmail y pulsar en una notificación.
* Un [error en Android](https://issuetracker.google.com/issues/62427912) ocasiona a veces un error con "*... ActivityRecord no encontrado para ...*" después de actualizar FairEmail. Reinstalando ([fuente](https://stackoverflow.com/questions/46309428/android-activitythread-reportsizeconfigurations-causes-app-to-freeze-with-black)) podría solucionar el problema.
* Un [error en Android](https://issuetracker.google.com/issues/37018931) ocasiona a veces un error con *... InputChannel no está inicializado ...* en algunos dispositivos.
* ~~Un [error en LineageOS](https://review.lineageos.org/c/LineageOS/android_frameworks_base/+/265273) causa a veces un error con *... java.lang.ArrayIndexOutOfBoundsException: length=...; index=... ...*.~~
* Un error en Nova Launcher en Android 5.x hace que FairEmail falle con un *java.lang.StackOverflowError* cuando Nova Launcher tiene acceso al servicio de accesibilidad.
* ~~El selector de carpetas a veces no muestra carpetas por razones desconocidas. Esto parece estar arreglado.~~
* ~~Un [error en AndroidX](https://issuetracker.google.com/issues/64729576) hace difícil agarrar el desplazamiento rápido. Una solución fue añadida.~~
* ~~El cifrado con YubiKey resulta en un bucle infinito. Esto parece ser causado por un [error en OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2507).~~
* Desplazar a una ubicación vinculada internamente en mensajes originales no funciona. Esto no se puede arreglar porque la vista original del mensaje está contenida en una vista de desplazamiento.
* La vista previa del texto de los mensajes no aparece (siempre) en los relojes de Samsung porque [setLocalOnly](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder.html#setLocalOnly(boolean)) parece ser ignorado. El texto de vista previa de los mensajes se muestra correctamente en Pebble 2, Fitbit Charge 3 y Mi band 3. Ver también [estas Preguntas Frecuentes](#user-content-faq126).
* Un [error en Android 6.0](https://issuetracker.google.com/issues/37068143) causa un error con *... Offset inválido: ... El rango válido es ...* cuando el texto está seleccionado y se toca fuera del texto seleccionado. Este error ha sido corregido en Android 6.0.1.
* Los enlaces internos (anchor) no funcionarán porque los mensajes originales se muestran en una WebView embebida en una vista de desplazamiento (la lista de conversaciones). Esta es una limitación de Android que no se puede arreglar o eludir.

## Características planificadas

* ~~Sincronizar bajo demanda (manual)~~
* ~~Cifrado semi automático ~~
* ~~Copiar mensaje~~
* ~~Estrellas de colores~~
* ~~Ajustes de notificación por carpeta~~
* ~~Seleccionar imágenes locales para firmas~~ (esto no se añadirá porque requiere administración de archivos de imagen y porque las imágenes no se muestran por defecto en la mayoría de los clientes de correo electrónico)
* ~~Mostrar mensajes que coincidan con una regla~~
* ~~[ManageSieve](https://tools.ietf.org/html/rfc5804)~~ (no hay librerías Java mantenidas con una licencia adecuada y sin dependencias y, además, FairEmail tiene sus propias reglas de filtro)
* ~~Buscar mensajes con/sin archivos adjuntos~~ (esto no puede ser añadido porque IMAP no soporta la búsqueda de archivos adjuntos)
* ~~Buscar una carpeta~~ (filtrar una lista jerárquica de carpetas es problemático)
* ~~Sugerencias de búsqueda~~
* ~~[Mensaje de configuración de autocifrado](https://autocrypt.org/autocrypt-spec-1.0.0.pdf) (sección 4.4)~~ (en mi opinión no es una buena idea permitir que un cliente de correo electrónico maneje claves de cifrado sensibles para un caso de uso excepcional mientras que OpenKeychain también puede exportar claves)
* ~~Carpetas unificadas genéricas~~
* ~~Nuevo programa de notificación de mensajes por cuenta~~ (implementado añadiendo una condición de tiempo a las reglas para que los mensajes puedan ser pospuestos durante los periodos seleccionados)
* ~~Copiar cuentas e identidades~~
* ~~Zoom via pellizco~~ (no es posible en una lista de desplazamiento; la vista completa del mensaje puede ser agrandada en su lugar)
* ~~Vista de carpetas más compacta~~
* ~~Componer listas y tablas~~ (requiere un editor de texto enriquecido, vea [estas FAQ](#user-content-faq99))
* ~~Pellizcar para hacer zoom en el tamaño de texto~~
* ~~Mostrar GIFs~~
* ~~Temas~~ (un tema gris claro y uno oscuro fueron añadidos porque esto es lo que la mayoría de la gente parece querer)
* ~~Cualquier condición de hora del día~~ (cualquier día no encaja realmente en la condición de fecha/hora)
* ~~Enviar como adjunto~~
* ~~Widget para la cuenta seleccionada~~
* ~~Recordar adjuntar archivos~~
* ~~Seleccionar dominios para los cuales mostrar imágenes~~ (esto será demasiado complicado de usar)
* ~~Vista de mensajes favoritos unificada~~ (ya hay una búsqueda especial para esto)
* ~~Mover acción de notificación~~
* ~~Soporte para S/MIMEe~~
* ~~Buscar ajustes~~

Cualquier cosa en esta lista está en orden aleatorio y *podría* ser añadida en un futuro próximo.

## Características solicitadas con frecuencia

El diseño está basado en muchas discusiones y si lo deseas puedes unirte a la discusión [en este foro](https://forum.xda-developers.com/android/apps-games/source-email-t3824168). El objetivo del diseño es ser minimalista (sin menús innecesarios, botones, etc.) y no distraer (colores brillantes, animaciones, etc.). Todas las cosas mostradas deben ser útiles de una u otra manera y deben posicionarse cuidadosamente para un uso fácil. Fuentes, tamaños, colores, etc debe seguir el diseño material siempre que sea posible.

## Preguntas frecuentes

* [(1) ¿Qué permisos se necesitan y por qué?](#user-content-faq1)
* [(2) ¿Por qué se muestra una notificación permanente?](#user-content-faq2)
* [(3) ¿Qué son las operaciones y por qué están pendientes?](#user-content-faq3)
* [(4) ¿Cómo puedo utilizar un certificado de seguridad no válido / contraseña vacía / conexión de texto plano?](#user-content-faq4)
* [(5) ¿Cómo puedo personalizar la vista de mensajes?](#user-content-faq5)
* [(6) ¿Cómo puedo iniciar sesión en Gmail / G suite?](#user-content-faq6)
* [(7) ¿Por qué los mensajes enviados no aparecen (directamente) en la carpeta de enviados?](#user-content-faq7)
* [(8) ¿Puedo utilizar una cuenta de Microsoft Exchange?](#user-content-faq8)
* [(9) ¿Qué son las identidades / cómo agregar un alias?](#user-content-faq9)
* [~~(11) ¿Por qué no se admite POP?~~](#user-content-faq11)
* [~~(10) ¿Qué significa 'UIDPLUS no soportado'?~~](#user-content-faq10)
* [(12) ¿Cómo funciona el cifrado/descifrado?](#user-content-faq12)
* [(13) ¿Cómo funciona la búsqueda en dispositivo/servidor?](#user-content-faq13)
* [(14) ¿Cómo puedo configurar una cuenta de Outlook / Live / Hotmail?](#user-content-faq14)
* [(15) ¿Por qué sigue cargando el texto del mensaje?](#user-content-faq15)
* [(16) ¿Por qué no se sincronizan los mensajes?](#user-content-faq16)
* [~~(17) ¿Por qué no funciona la sincronización manual?~~](#user-content-faq17)
* [(18) ¿Por qué no se muestra siempre la vista previa del mensaje?](#user-content-faq18)
* [(19) ¿Por qué las funcionalidades "pro" son tan caras?](#user-content-faq19)
* [(20) ¿Puedo obtener un reembolso?](#user-content-faq20)
* [(21) ¿Cómo activo la luz de notificaciones?](#user-content-faq21)
* [(22) ¿Qué significa el error cuenta/carpeta?](#user-content-faq22)
* [(23) ¿Por qué recibo una alerta.. ?](#user-content-faq23)
* [(24) ¿Qué es explorar mensajes en el servidor?](#user-content-faq24)
* [(25) ¿Por qué no puedo seleccionar/abrir/guardar una imagen, adjunto o un archivo?](#user-content-faq25)
* [(26) ¿Puedo ayudar a traducir FairEmail a mi propio idioma?](#user-content-faq26)
* [(27) ¿Cómo distingo entre imágenes embebidas y externas?](#user-content-faq27)
* [(28) ¿Cómo puedo administrar las notificaciones de la barra de estado?](#user-content-faq28)
* [(29) ¿Cómo puedo recibir notificaciones de mensajes nuevos para otras carpetas?](#user-content-faq29)
* [(30) ¿Cómo puedo utilizar los ajustes rápidos proporcionados?](#user-content-faq30)
* [(31) ¿Cómo puedo utilizar los atajos proporcionados?](#user-content-faq31)
* [(32) ¿Cómo puedo comprobar si la lectura del correo electrónico es realmente segura?](#user-content-faq32)
* [(33) ¿Por qué no funcionan las direcciones de remitentes editadas?](#user-content-faq33)
* [(34) ¿Cómo coinciden las identidades?](#user-content-faq34)
* [(35) ¿Por qué debo de tener cuidado con ver imágenes, archivos adjuntos y el mensaje original?](#user-content-faq35)
* [(36) ¿Cómo se cifran los archivos de configuración?](#user-content-faq36)
* [(37) ¿Cómo se almacenan las contraseñas?](#user-content-faq37)
* [(39) ¿Cómo puedo reducir el uso de la batería de FairEmail?](#user-content-faq39)
* [(40) ¿Cómo puedo reducir el uso de datos de FairEmail?](#user-content-faq40)
* [(41) ¿Cómo puedo arreglar el error "Handshake falló"?](#user-content-faq41)
* [(42) ¿Puedes añadir un nuevo proveedor a la lista de proveedores?](#user-content-faq42)
* [(43) ¿Puedes mostrar el original ... ?](#user-content-faq43)
* [(44) ¿Puedes mostrar fotos de contacto / identicons en la carpeta enviada?](#user-content-faq44)
* [(45) ¿Cómo puedo arreglar 'Esta clave no está disponible'? Para utilizarlo, ¡debes importarla como una propia!" ?](#user-content-faq45)
* [(46) ¿Por qué la lista de mensajes sigue actualizándose?](#user-content-faq46)
* [(47) ¿Cómo resuelvo el error "No hay cuenta primaria o carpeta de borradores"?](#user-content-faq47)
* [~~(48) ¿Cómo resuelvo el error "No hay cuenta primaria o carpeta de archivo"? ~~](#user-content-faq48)
* [(49) ¿Cómo puedo arreglar 'Una app obsoleta ha enviado la ruta a un archivo en lugar de una secuencia de archivos'?](#user-content-faq49)
* [(50) ¿Puedes añadir una opción para sincronizar todos los mensajes?](#user-content-faq50)
* [(51) ¿Cómo se ordenan las carpetas?](#user-content-faq51)
* [(52) ¿Por qué tomar algún tiempo volver a conectar a una cuenta?](#user-content-faq52)
* [(53) ¿Puedes pegar la barra de acción del mensaje en la parte superior/inferior?](#user-content-faq53)
* [~~(54) ¿Cómo uso un prefijo de espacio de nombres?~~](#user-content-faq54)
* [(55) ¿Cómo puedo marcar todos los mensajes como leídos / moverlos o borrarlos?](#user-content-faq55)
* [(56) ¿Puedes añadir soporte para JMAP?](#user-content-faq56)
* [~~(57) ¿Puedo usar HTML en firmas?~~](#user-content-faq57)
* [(58) ¿Qué significa un icono de correo electrónico abierto/cerrado?](#user-content-faq58)
* [(59) ¿Pueden abrirse mensajes originales en el navegador?](#user-content-faq59)
* [(60) ¿Sabía que ...?](#user-content-faq60)
* [(61) ¿Por qué algunos mensajes se muestran atenuados?](#user-content-faq61)
* [(62) ¿Cuáles métodos de autenticación están soportados?](#user-content-faq62)
* [(63) ¿Cómo se redimensionan las imágenes para mostrarlas en pantalla?](#user-content-faq63)
* [~~(64) ¿Puedes añadir acciones personalizadas para deslizar hacia la izquierda/derecha?~~](#user-content-faq64)
* [(65) ¿Por qué algunos archivos adjuntos se muestran atenuados?](#user-content-faq65)
* [(66) ¿Está FairEmail disponible en la Biblioteca de la Familia de Google Play?](#user-content-faq66)
* [(67) ¿Cómo puedo posponer las conversaciones?](#user-content-faq67)
* [~~(68) ¿Por qué el lector Adobe Acrobat no puede abrir archivos adjuntos PDF / aplicaciones de Microsoft no pueden abrir documentos adjuntos?~~](#user-content-faq68)
* [(69) ¿Puedes añadir desplazamiento automático hasta arriba en un nuevo mensaje?](#user-content-faq69)
* [(70) ¿Cuándo se expandirán automáticamente los mensajes?](#user-content-faq70)
* [(71) ¿Cómo uso las reglas de filtro?](#user-content-faq71)
* [(72) ¿Qué son las cuentas o identidades principales?](#user-content-faq72)
* [(73) ¿Es seguro/eficiente mover mensajes a través de cuentas?](#user-content-faq73)
* [(74) ¿Por qué veo mensajes duplicados?](#user-content-faq74)
* [(75) ¿Puedes hacer una versión para iOS, Windows, Linux, etc?](#user-content-faq75)
* [(76) ¿Qué es lo que 'Limpiar mensajes locales' hace?](#user-content-faq76)
* [(77) ¿Por qué los mensajes a veces se muestran con un pequeño retraso?](#user-content-faq77)
* [(78) ¿Cómo uso programas?](#user-content-faq78)
* [(79) ¿Cómo uso la sincronización bajo demanda (manual)?](#user-content-faq79)
* [~~(80) ¿Cómo arreglar el error 'Unable to load BODYSTRUCTURE'?~~](#user-content-faq80)
* [~~(81) ¿Puedes hacer el fondo del mensaje original oscuro en el tema oscuro?~~](#user-content-faq81)
* [(82) ¿Qué es una imagen de rastreo?](#user-content-faq82)
* [(84) ¿Para qué sirven los contactos locales?](#user-content-faq84)
* [(85) ¿Por qué no está disponible una identidad?](#user-content-faq85)
* [~~(86) ¿Qué son las 'características de privacidad adicionales'?~~](#user-content-faq86)
* [(87) ¿Qué significa 'credenciales no válidas'?](#user-content-faq87)
* [(88) ¿Cómo puedo utilizar una cuenta de Yahoo, AOL o Sky?](#user-content-faq88)
* [(89) ¿Cómo puedo enviar mensajes de sólo texto plano?](#user-content-faq89)
* [(90) ¿Por qué algunos textos están enlazados sin ser un enlace?](#user-content-faq90)
* [~~(91) ¿Puedes añadir sincronización periódica para ahorrar batería?~~](#user-content-faq91)
* [(92) ¿Puede añadir filtro de spam, verificación de la firma DKIM y autorización SPF?](#user-content-faq92)
* [(93) ¿Puede permitir la instalación/almacenamiento de datos en medios de almacenamiento externo (sdcard)?](#user-content-faq93)
* [(94) ¿Qué significa la banda roja/naranja al final de la cabecera?](#user-content-faq94)
* [(95) ¿Por qué no se muestran todas las aplicaciones al seleccionar un archivo adjunto o una imagen?](#user-content-faq95)
* [(96) ¿Dónde puedo encontrar los ajustes IMAP y SMTP?](#user-content-faq96)
* [(97) ¿Qué es la 'limpieza'?](#user-content-faq97)
* [(98) ¿Por qué todavía puedo elegir contactos después de revocar los permisos de los contactos?](#user-content-faq98)
* [(99) ¿Puedes añadir un editor de texto enriquecido o de markdown?](#user-content-faq99)
* [(100) ¿Cómo puedo sincronizar las categorías de Gmail?](#user-content-faq100)
* [(101) ¿Qué significa el punto azul/naranja en la parte inferior de las conversaciones?](#user-content-faq101)
* [(102) ¿Cómo puedo habilitar la rotación automática de imágenes?](#user-content-faq102)
* [(103) ¿Cómo puedo grabar audio?](#user-content-faq103)
* [(104) ¿Qué necesito saber sobre los informes de errores?](#user-content-faq104)
* [(105) ¿Cómo funciona la opción de roam-como-en-casa?](#user-content-faq105)
* [(106) ¿Qué lanzadores pueden mostrar un contador con el número de mensajes no leídos?](#user-content-faq106)
* [(107) ¿Cómo utilizo estrellas de colores?](#user-content-faq107)
* [(108) ¿Puedes añadir eliminar mensajes permanentemente desde cualquier carpeta?](#user-content-faq108)
* [~~(109) ¿Por qué 'seleccionar cuenta' está disponible sólo en versiones oficiales?~~](#user-content-faq109)
* [(110) ¿Por qué hay (algunos) mensajes vacíos y/o adjuntos corruptos?](#user-content-faq110)
* [(111) ¿Es compatible OAuth?](#user-content-faq111)
* [(112) ¿Qué proveedor de correo electrónico recomendas?](#user-content-faq112)
* [(113) ¿Cómo funciona la autenticación biométrica?](#user-content-faq113)
* [(114) ¿Puedes añadir una importación para la configuración de otras aplicaciones de correo electrónico?](#user-content-faq114)
* [(115) ¿Puedes añadir chips de direcciones de correo electrónico?](#user-content-faq115)
* [~~(116) ¿Cómo puedo mostrar imágenes en mensajes de remitentes de confianza por defecto?~~](#user-content-faq116)
* [(117) ¿Puede ayudarme a restaurar mi compra?](#user-content-faq117)
* [(118) ¿Qué es exactamente 'Quitar parámetros de seguimiento'?](#user-content-faq118)
* [~~(119) ¿Puedes añadir colores al widget de bandeja de entrada unificada?~~](#user-content-faq119)
* [(120) ¿Por qué no son eliminadas las notificaciones de nuevos mensajes al abrir la aplicación?](#user-content-faq120)
* [(121) ¿Cómo se agrupan los mensajes en una conversación?](#user-content-faq121)
* [~~(122) ¿Por qué se muestra el nombre/correo-e del destinatario con un color de advertencia?~~](#user-content-faq122)
* [(123) ¿Qué pasará cuando FairEmail no pueda conectarse a un servidor de correo-e?](#user-content-faq123)
* [(124) ¿Por qué recibo 'Mensaje muy grande o muy complejo para mostrar?](#user-content-faq124)
* [(125) ¿Cuáles son las características experimentales actuales?](#user-content-faq125)
* [(126) ¿Pueden enviarse las previsualizaciones de mensajes a mi vestible?](#user-content-faq126)
* [(127) ¿Cómo puedo arreglar 'argumento(s) HELO sintácticamente invalido'?](#user-content-faq127)
* [(128) ¿Cómo puedo reiniciar las preguntas, por ejemplo para mostrar imágenes?](#user-content-faq128)
* [(129) ¿ProtonMail, Tutanota son apoyadas?](#user-content-faq129)
* [(130) ¿Qué significa error de mensaje..?](#user-content-faq130)
* [(131) ¿Puedes cambiar la dirección para deslizar al mensaje previo/siguiente?](#user-content-faq131)
* [(132) ¿Por qué se silencian las notificaciones de mensajes nuevos?](#user-content-faq132)
* [(133) ¿Por qué ActiveSync no es compatible?](#user-content-faq133)
* [(134) ¿Puedes añadir borrado de mensajes locales?](#user-content-faq134)
* [(135) ¿Por qué se muestran mensajes basura y borradores en las conversaciones?](#user-content-faq135)
* [(136) ¿Cómo puedo eliminar una cuenta/identidad/carpeta?](#user-content-faq136)
* [(137) ¿Cómo puedo reiniciar 'No preguntar de nuevo'?](#user-content-faq137)
* [(138) ¿Puedes añadir calendario/gestión de contactos/sincronización?](#user-content-faq138)
* [(139) ¿Cómo arreglo 'El usuario está autenticado pero no conectado'?](#user-content-faq139)
* [(140) ¿Por qué el texto del mensaje contiene caracteres extraños?](#user-content-faq140)
* [(141) ¿Cómo puedo arreglar 'Una carpeta de borradores es necesaria para enviar mensajes'?](#user-content-faq141)
* [(142) ¿Cómo puedo guardar los mensajes enviados en la bandeja de entrada?](#user-content-faq142)
* [~~(143) ¿Puedes añadir una carpeta de papelera para cuentas POP3?~~](#user-content-faq143)
* [(144) ¿Cómo puedo grabar notas de voz?](#user-content-faq144)
* [(145) ¿Cómo puedo establecer un sonido de notificación para una cuenta, carpeta o remitente?](#user-content-faq145)
* [(146) ¿Cómo puedo arreglar tiempos incorrectos de los mensajes?](#user-content-faq146)
* [(147) ¿Qué debo saber sobre las versiones de terceros?](#user-content-faq147)
* [(148) ¿Cómo puedo usar una cuenta iCloud de Apple?](#user-content-faq148)
* [(149) ¿Cómo funciona el widget de conteo de mensajes no leídos?](#user-content-faq149)
* [(150) ¿Puedes añadir la cancelación de invitaciones de calendario?](#user-content-faq150)
* [(151) ¿Puedes añadir copia de seguridad/restauración de mensajes?](#user-content-faq151)
* [(152) ¿Cómo puedo insertar un grupo de contactos?](#user-content-faq152)
* [(153) ¿Por qué la eliminación permanente de mensajes de Gmail no funciona?](#user-content-faq153)
* [~~(154) ¿Puedes añadir favicons como fotos de contacto?~~](#user-content-faq154)
* [(155) ¿Qué es un archivo winmail.dat?](#user-content-faq155)
* [(156) ¿Cómo puedo configurar una cuenta de Office 365?](#user-content-faq156)
* [(157) ¿Cómo puedo configurar una cuenta de Free.fr?](#user-content-faq157)
* [(158) ¿Qué cámara / grabador de audio recomienda?](#user-content-faq158)
* [(159) ¿Qué son las listas de protección de rastreadores de Disconnect?](#user-content-faq159)
* [(160) ¿Puedes añadir el borrado permanente de mensajes sin confirmación?](#user-content-faq160)
* [(161) ¿Puedes añadir un ajuste para cambiar el color principal y de acento?](#user-content-faq161)

[Tengo otra pregunta.](#user-content-support)

<a name="faq1"></a>
**(1) ¿Qué permisos son necesarios y por qué?**

Se necesitan los siguientes permisos de Android:

* *tiene acceso a la red completa* (INTERNET): para enviar y recibir correo electrónico
* *ver conexiones de red* (ACCESS_NETWORK_STATE): para monitorizar los cambios de conectividad a Internet
* *ejecutar al inicio* (RECEIVE_BOOT_COMPLETED): para iniciar la monitorización al iniciar el dispositivo
* *servicio de primer plano* (FOREGROUND_SERVICE): para ejecutar un servicio de primer plano en Android 9 Pie y posterior, ver también la siguiente pregunta
* *evitar que el dispositivo duerme* (WAKE_LOCK): para mantener el dispositivo despierto mientras sincroniza los mensajes
* *facturación in-app* (BILLING): para permitir compras en la app
* Opcional: *lee tus contactos* (READ_CONTACTS): para autocompletar direcciones y para mostrar fotos
* Opcional: *lee el contenido de tu tarjeta SD* (READ_EXTERNAL_STORAGE): para aceptar archivos de otras aplicaciones desactualizadas, consulta también [este FAQ](#user-content-faq49)
* Opcional: *usar hardware de huella dactilar* (USE_FINGERPRINT) y usar *hardware biométrico* (USE_BIOMETRIC): para usar autenticación biométrica
* Opcional: *encontrar cuentas en el dispositivo* (GET_ACCOUNTS) para seleccionar una cuenta cuando se utiliza la configuración rápida de Gmail
* Android 5. Lollipop y antes: *usar cuentas en el dispositivo* (USE_CREDENTIALS): para seleccionar una cuenta al usar la configuración rápida de Gmail (no solicitada en versiones posteriores de Android)
* Android 5. Lollipop y antes: *usar cuentas en el dispositivo* (USE_CREDENTIALS): para seleccionar una cuenta al usar la configuración rápida de Gmail (no solicitada en versiones posteriores de Android)

[Permisos opcionales](https://developer.android.com/training/permissions/requesting) son compatibles con Android 6 Marshmallow y sólo posteriores. En versiones anteriores de Android se le pedirá que conceda los permisos opcionales para instalar FairEmail.

Los siguientes permisos son necesarios para mostrar el recuento de mensajes no leídos como una insignia (ver también [este FAQ](#user-content-faq106)):

* *com.sec.android.provider.badge.permission.READ*
* *com.sec.android.provider.badge.permission.WRITE*
* *com.htc.launcher.permission.READ_SETINGS*
* *com.htc.launcher.permission► PDATE_SHORTCUT*
* *com.sonyericsson.home.permission.BROADCAST_BADGE*
* *com.sonymobile.home.permission.PROVIDER_INSERT_BADGE*
* *com.anddoes.launcher.permisos PDATE_COUNT*
* *com.majeur.launcher.permission► PDATE_BADGE*
* *com.huawei.android.launcher.permission.CHANGE_BADGE*
* *com.huawei.android.launcher.permission.READ_SETTINGS*
* *com.huawei.android.launcher.permission.WRITE_SETTINGS*
* *android.permission.READ_APP_BADGE*
* *com.oppo.launcher.permission.READ_SETINGS*
* *com.oppo.launcher.permission.WRITE_SETINGS*
* *me.everything.badger.permission.BADGE_COUNT_READ*
* *me.everything.badger.permission.BADGE_COUNT_WRITE*

FairEmail mantendrá una lista de direcciones de las que recibe y a las que envía mensajes y utilizará esta lista para sugerencias de contactos cuando no se otorgue permiso de contactos a FairEmail. Esto significa que puede utilizar FairEmail sin el proveedor de contactos Android (libreta de direcciones). Ten en cuenta que todavía puedes elegir contactos sin conceder sus permisos a FairEmail, solo sugerir que los contactos no funcionarán sin los permisos de contactos.

<br />

<a name="faq2"></a>
**(2) ¿Por qué se muestra una notificación permanente?**

Una notificación permanente de baja prioridad con el número de cuentas monitoreadas y el número de operaciones pendientes (ver la siguiente pregunta) se muestra para evitar que Android mate el servicio que se encarga de recibir correo electrónico continuamente. Esto [ya era necesario](https://developer.android.com/reference/android/app/Service.html#startForeground(int,%20android.app.Notification)), pero con la introducción de [modo doze](https://developer.android.com/training/monitoring-device-state/doze-standby) en Android 6 Marshmallow esto es más necesario que nunca. El modo Doze detendrá todas las aplicaciones cuando la pantalla esté apagada por algún tiempo a menos que la aplicación inicie un servicio de primer plano, lo que requiere mostrar una notificación en la barra de estado.

La mayoría, si no todas, otras aplicaciones de correo electrónico no muestran una notificación, con el "efecto secundario" de que los mensajes nuevos a menudo no son reportados o son reportados tarde y que los mensajes no son enviados o son enviados tarde.

Android muestra primero los iconos de las notificaciones de la barra de estado de alta prioridad y ocultará el icono de la notificación de FairEmail si ya no hay espacio para mostrar los iconos. En la práctica esto significa que la notificación de la barra de estado no ocupa espacio en la barra de estado, a menos que haya espacio disponible.

La notificación de la barra de estado se puede desactivar a través de la configuración de notificación de FairEmail:

* Android 8 Oreo y posteriores: toque el botón *Canal de recepción* y desactive el canal a través de la configuración de Android (esto no deshabilitará las notificaciones de nuevos mensajes)
* Android 7 Nougat y antes: habilite *Usa el servicio de segundo plano para sincronizar mensajes*, pero asegúrese de leer el comentario debajo de la configuración

Puede cambiar a sincronización periódica de mensajes en la configuración de recepción para eliminar la notificación, pero tenga en cuenta que esto podría aumentar el uso de batería. Vea [aquí](#user-content-faq39) para más detalles sobre el uso de la batería.

Android 8 Oreo también podría mostrar una notificación de barra de estado con el texto *Las aplicaciones se están ejecutando en segundo plano*. Por favor, consulte [aquí](https://www.reddit.com/r/Android/comments/7vw7l4/psa_turn_off_background_apps_notification/) sobre cómo puede desactivar esta notificación.

Algunas personas sugirieron usar [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/) (FCM) en lugar de un servicio Android con una notificación en la barra de estado, pero esto requeriría que los proveedores de correo enviaran mensajes FCM o un servidor central donde todos los mensajes sean recogidos enviando mensajes FCM. La primera no va a suceder y la última tendría importantes implicaciones en materia de privacidad.

Si vino aquí haciendo clic en la notificación, debe saber que el siguiente clic abrirá la bandeja de entrada unificada.

<br />

<a name="faq3"></a>
**(3) ¿Qué son las operaciones y por qué están pendientes?**

La notificación de la barra de estado de baja prioridad muestra el número de operaciones pendientes, que pueden ser:

* *añadir*: añadir mensaje a la carpeta remota
* *mover*: mover mensaje a otra carpeta remota
* *copiar*: copiar mensaje a otra carpeta remota
* *obtener*: obtención de mensajes cambiados (pushed)
* *borrar*: borrar mensaje de la carpeta remota
* *visto*: marcar mensaje como leído/no leído en la carpeta remota
* *respondido*: marca el mensaje como respondido en la carpeta remota
* *marcar*: añadir/remover estrella en la carpeta remota
* *palabra clave*: añadir/eliminar marca IMAP en carpeta remota
* *etiqueta*: establecer/restablecer etiqueta Gmail en carpeta remota
* *encabezados*: descargar encabezados de mensajes
* *raw*: descargar mensaje raw
* *cuerpo*: descargar texto del mensaje
* *adjunto*: descargar adjunto
* *sincronizar*: sincronizar mensajes locales y remotos
* *suscribir*: suscribirse a la carpeta remota
* *purgar*: borrar todos los mensajes de la carpeta remota
* *enviar*: enviar mensaje
* *existe*: comprueba si el mensaje existe
* *regla*: ejecutar regla en el cuerpo del texto

Las operaciones sólo se procesan cuando hay una conexión al servidor de correo electrónico o cuando se sincroniza manualmente. Ver también [estas Preguntas Frecuentes](#user-content-faq16).

<br />

<a name="faq4"></a>
**(4) ¿Cómo puedo utilizar un certificado de seguridad inválido / contraseña vacía / conexión de texto plano?**

*... No confiable ... no en el certificado ...*
<br />
*... Certificado de seguridad inválido (no se puede verificar la identidad del servidor) ...*

Debería intentar arreglar esto poniéndose en contacto con su proveedor u obteniendo un certificado de seguridad válido porque los certificados de seguridad inválidos no son seguros y permiten [ataques man-in-the-middle](https://en.wikipedia.org/wiki/Man-in-the-middle_attack). Si el dinero es un obstáculo, puede obtener certificados de seguridad gratuitos de [Let’s Encrypt](https://letsencrypt.org).

Alternativamente, puede aceptar la huella digital mostrada a continuación del mensaje de error si configura la cuenta y/o identidad en los pasos de configuración 1 y 2 (esto no es posible cuando utiliza el asistente de configuración rápido). Tenga en cuenta que debe asegurarse de que la conexión a Internet que está utilizando es segura.

Tenga en cuenta que las versiones antiguas de Android podrían no reconocer las nuevas autoridades de certificación como Let’s Encrypt que causan que las conexiones se consideren inseguras, vea también [aquí](https://developer.android.com/training/articles/security-ssl).

*Trust anchor for certification path not found*

*... java.security.cert.CertPathValidatorException: Trust anchor for certification path not found ...* significa que el administrador de confianza por defecto de Android no pudo verificar la cadena de certificados del servidor.

Debería reparar la configuración del servidor o aceptar la huella digital mostrada debajo del mensaje de error.

Tenga en cuenta que este problema también puede ser causado por el servidor no enviando todos los certificados intermedios.

*Contraseña vacía*

Su nombre de usuario probablemente sea fácilmente adivinado, por lo que esto es inseguro.

*Conexión de texto simple*

Su nombre de usuario y contraseña y todos los mensajes serán enviados y recibidos sin cifrar, el cual es **muy inseguro** porque un [ataque de intermediario](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) es muy simple en una conexión no cifrada.

Si aún quiere usar un certificado de seguridad invalido, una contraseña vacía o una conexión de texto simple necesitará habilitar conexiones inseguras en la cuenta y/o configuraciones de identidad. STARTTLS debería ser seleccionado para conexiones de texto simple. Si activa conexiones inseguras, sólo debería conectarse a través de redes privadas y confiables y nunca a través de redes públicas, como las ofrecidas en hoteles, aeropuertos, etc.

<br />

<a name="faq5"></a>
**(5) ¿Cómo puedo personalizar la vista del mensaje?**

En el menú de tres puntos puede activar o desactivar o seleccionar:

* *tamaño de texto*: para tres tamaños de fuente diferentes
* *vista compacta*: para elementos de mensaje más condensados y una fuente de texto más pequeña

En la sección de visualización de los ajustes puede activar o desactivar:

* *Bandeja de entrada unificada*: para desactivar la bandeja de entrada unificada y para listar las carpetas seleccionadas para la bandeja de entrada unificada en su lugar
* *Agrupar por fecha*: mostrar el encabezado de fecha sobre los mensajes con la misma fecha
* *Hilos de conversación*: para desactivar la vista de conversación y mostrar mensajes individuales en su lugar
* *Mostrar fotos de contactos*: para ocultar fotos de contactos
* *Mostrar identicons*: para mostrar avatares de contactos generados
* *Mostrar nombres y direcciones de correo electrónico*: mostrar nombres o mostrar nombres y direcciones de correo electrónico
* *Mostrar asunto en cursiva*: para mostrar el asunto del mensaje como texto normal
* *Mostrar estrellas*: para ocultar estrellas (favoritos)
* *Mostrar vista previa del mensaje*: para mostrar dos líneas del texto del mensaje
* *Mostrar detalles de dirección por defecto*: para expandir la sección de direcciones por defecto
* *Usa una fuente monospaciada para el texto del mensaje*: para usar una fuente de ancho fijo para el texto de los mensajes
* *Mostrar automáticamente el mensaje original para los contactos conocidos*: para mostrar automáticamente los mensajes originales para los contactos en su dispositivo, por favor lea [estas Preguntas Frecuentes](#user-content-faq35)
* *Mostrar automáticamente el mensaje original para los contactos conocidos*: para mostrar automáticamente los mensajes originales para los contactos en su dispositivo, por favor lea [estas Preguntas Frecuentes](#user-content-faq35)
* *Barra de acción de conversación*: para desactivar la barra de navegación inferior

Tenga en cuenta que los mensajes sólo se pueden previsualizar cuando se haya descargado el texto del mensaje. Los textos de mensajes más grandes no se descargan por defecto en redes medidas (generalmente móviles). Puedes cambiar esto en la configuración.

Si la lista de direcciones es larga, puede contraer la sección de direcciones con el icono *menos* en la parte superior de la sección de direcciones.

Algunas personas piden:

* mostrar la el asunto en negrita, pero la negrita ya está siendo usada para resaltar los mensajes no leídos
* mostrar la dirección o el asunto más grande/más pequeño, pero esto interferiría con la opción de tamaño de texto
* mover la estrella a la izquierda, pero es mucho más fácil tener la estrella en el lado derecho

Por desgracia, es imposible hacer feliz a todo el mundo y añadir un montón de ajustes no sólo sería confuso, sino que nunca sería suficiente.

<br />

<a name="faq6"></a>
**(6) ¿Cómo puedo iniciar sesión en Gmail / G suite?**

Puede utilizar el asistente de configuración rápida para configurar fácilmente una cuenta e identidad de Gmail.

Si no quiere usar una cuenta Gmail del dispositivo, puede habilitar el acceso para "aplicaciones menos seguras" y utilizar la contraseña de su cuenta (no recomendado) o habilitar la autenticación de dos factores y utilizar una contraseña específica de la aplicación. Para utilizar una contraseña necesitará configurar una cuenta e identidad a través del paso de configuración 1 y 2 en lugar del asistente de configuración rápida.

Consulte [estas Preguntas Frecuentes](#user-content-faq111) sobre por qué sólo se pueden utilizar las cuentas en el dispositivo.

Tenga en cuenta que se requiere una contraseña específica de la aplicación cuando la autenticación de dos factores está habilitada.

<br />

*Contraseña específica de la aplicación*

Vea [aquí](https://support.google.com/accounts/answer/185833) sobre cómo generar una contraseña específica de la aplicación.

<br />

*Habilitar "Aplicaciones menos seguras"*

**Importante**: no se recomienda usar este método porque es menos confiable.

**Importante**: Las cuentas de Gsuite autorizadas con un nombre de usuario/contraseña dejarán de funcionar [en un futuro próximo](https://gsuiteupdates.googleblog.com/2019/12/less-secure-apps-oauth-google-username-password-incorrect.html).

Vea [aquí](https://support.google.com/accounts/answer/6010255) acerca de cómo habilitar "aplicaciones menos seguras" o vaya [directamente a la configuración](https://www.google.com/settings/security/lesssecureapps).

Si usa múltiples cuentas de Gmail, asegúrese de cambiar la configuración de "aplicaciones menos seguras" de la(s) cuenta(s) correcta(s).

Tenga en cuenta que necesita salir de la pantalla de ajustes de "aplicaciones menos seguras" usando la flecha hacia atrás para aplicar la configuración.

Si usa este método, debería usar una [contraseña fuerte](https://en.wikipedia.org/wiki/Password_strength) para tu cuenta de Gmail, lo cual es una buena idea de todos modos. Tenga en cuenta que usar el protocolo IMAP [estándar](https://tools.ietf.org/html/rfc3501) en sí mismo no es menos seguro.

Cuando "aplicaciones menos seguras" no está activado, obtendrá el error *La autenticación falló - credenciales inválidas* para cuentas (IMAP) y *Nombre de usuario y contraseña no aceptados* para identidades (SMTP).

<br />

*General*

Puede obtener la alerta "*Inicie sesión a través de su navegador web*". Esto sucede cuando Google considera que la red que lo conecta a Internet (esto podría ser una VPN) es insegura. Esto se puede evitar usando el asistente de configuración rápida de Gmail o una contraseña específica de la aplicación.

Mire [aquí](https://support.google.com/mail/answer/7126229) las instrucciones de Google y [aquí](https://support.google.com/mail/accounts/answer/78754) para solucionar problemas.

<br />

<a name="faq7"></a>
**(7) ¿Por qué los mensajes enviados no aparecen (directamente) en la carpeta enviados?**

Los mensajes enviados normalmente se mueven de la bandeja de salida a la carpeta enviados tan pronto como su proveedor agrega los mensajes enviados a la carpeta enviados. Esto requiere que se seleccione una carpeta enviados en la configuración de la cuenta y que la carpeta enviados se configure para sincronizar.

Algunos proveedores no llevan un seguimiento de los mensajes enviados o el servidor SMTP usado podría no estar relacionado con el proveedor. En estos casos FairEmail automáticamente añadirá mensajes enviados a la carpeta enviados al sincronizar la carpeta enviados, que sucederá después de que se haya enviado un mensaje. Tenga en cuenta que esto resultará en tráfico extra de Internet.

~~Si esto no sucede, es posible que su proveedor no esté al tanto de los mensajes enviados o que esté utilizando un servidor SMTP no relacionado con el proveedor. ~ ~~En estos casos puede habilitar la configuración avanzada de identidad *Guardar mensajes enviados* para que FairEmail añada mensajes enviados a la carpeta enviados justo después de enviar un mensaje. ~ ~~Tenga en cuenta que habilitar esta configuración puede resultar en mensajes duplicados si su proveedor añade mensajes enviados a la carpeta enviados. ~ ~~Además tenga cuidado de que habilitar esta configuración dará como resultado un uso adicional de datos, especialmente cuando se envíen mensajes con grandes archivos adjuntos.~~

~~Si los mensajes enviados en la bandeja de salida no se encuentran en la carpeta enviados en una sincronización completa, también se moverán de la bandeja de salida a la carpeta enviados. ~ ~~Una sincronización completa ocurre al reconectar al servidor o al sincronizar periódicamente o manualmente. ~ ~~Probablemente querrá habilitar la configuración avanzada *Almacenar mensajes enviados* en su lugar para mover mensajes a la carpeta enviados más pronto.~~

<br />

<a name="faq8"></a>
**(8) ¿Puedo usar una cuenta de Microsoft Exchange?**

Puede utilizar una cuenta de Microsoft Exchange si es accesible a través de IMAP, lo que es usualmente el caso. Vea [aquí](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793) para más información.

Por favor consulte [aquí](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) la documentación de Microsoft acerca de la configuración de un cliente de correo electrónico. También hay una sección sobre errores de conexión comunes y soluciones.

Algunas versiones antiguas del servidor Exchange tienen un error que causa mensajes vacíos y adjuntos corruptos. Consulte [esta sección de Preguntas Frecuentes](#user-content-faq110) para una solución provisional.

Consulte [esta sección de Preguntas Frecuentes](#user-content-faq133) sobre el soporte de ActiveSync.

Consulte [esta sección de Preguntas Frecuentes](#user-content-faq111) sobre el soporte de OAuth.

<br />

<a name="faq9"></a>
**(9) ¿Qué son las identidades / cómo añadir un alias?**

Las identidades representan las direcciones de correo electrónico *desde* las que está enviando a través de un servidor de correo electrónico (SMTP).

Algunos proveedores le permiten tener múltiples alias. Puede configurarlos estableciendo el campo de dirección de correo electrónico de una identidad adicional a la dirección de alias y configurando el campo nombre de usuario a su dirección de correo electrónico principal.

Tenga en cuenta que puede copiar una identidad manteniéndola presionada.

Alternativamente, puede habilitar *Permitir editar la dirección del remitente* en la configuración avanzada de una identidad existente para editar el nombre de usuario cuando se compone un nuevo mensaje, si su proveedor lo permite.

FairEmail actualizará automáticamente las contraseñas de las identidades relacionadas cuando actualice la contraseña de la cuenta asociada o una identidad relacionada.

Vea [estas Preguntas Frecuentes](#user-content-faq33) sobre la edición del nombre de usuario de las direcciones de correo electrónico.

<br />

<a name="faq10"></a>
**~~(10) ¿Qué significa 'UIDPLUS no soportado'?~~**

~~El mensaje de error *UIDPLUS no soportado* significa que su proveedor de correo no proporciona la extensión IMAP [UIDPLUS](https://tools.ietf.org/html/rfc4315). Esta extensión IMAP es necesaria para implementar la sincronización de dos vías, que no es una característica opcional. Así que, a menos que su proveedor pueda habilitar esta extensión, no puede usar FairEmail para este proveedor.~~

<br />

<a name="faq11"></a>
**~~(11) ¿Por qué no se admite POP?~~**

~~Además de que cualquier proveedor de correo electrónico decente soporta [IMAP](https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol) estos días,~~ ~~usar [POP](https://en.wikipedia.org/wiki/Post_Office_Protocol) dará como resultado un uso adicional innecesario de la batería y un retraso en las notificaciones de nuevos mensajes.~~ ~~Además, POP es inadecuado para la sincronización de dos vías y la mayoría de las veces la gente lee y escribe mensajes en diferentes dispositivos hoy en día.~~

~~Básicamente, POP sólo soporta descargar y borrar mensajes de la bandeja de entrada.~~ ~~Entonces, las operaciones comunes como configurar los atributos del mensaje (leer, marcar, responder, etc.), añadir (respaldar) y mover mensajes no es posible.~~

~~Ver también [lo que Google escribe sobre eso](https://support.google.com/mail/answer/7104828).~~

~~Por ejemplo [Gmail puede importar mensajes](https://support.google.com/mail/answer/21289) desde otra cuenta POP,~~ ~~que puede ser usado como solución temporal para cuando su proveedor no soporta IMAP.~~

~~tl;dr; considere cambiar a IMAP.~~

<br />

<a name="faq12"></a>
**(12) ¿Cómo funciona el cifrado/descifrado?**

*General*

Por favor, [vea aquí](https://en.wikipedia.org/wiki/Public-key_cryptography) sobre cómo funciona el cifrado con clave pública/privada.

Cifrado en resumen:

* Los mensajes **salientes** se cifran con la **clave pública** del destinatario
* Los mensajes **entrantes** se descifran con la **clave privada** del destinatario

Firma en resumen:

* Los mensajes **salientes** se firman con la **clave privada** del remitente
* Los mensajes **entrantes** se verifican con la **clave pública** del remitente

Para firmar/cifrar un mensaje, simplemente seleccione el método apropiado en el diálogo de enviar. También puede abrir el diálogo de enviar usando el menú de tres puntos en caso de que haya seleccionado *No volver a mostrar* antes.

Para verificar una firma o descifrar un mensaje recibido, abra el mensaje y pulse el icono de gesto o candado justo debajo de la barra de acción del mensaje.

La primera vez que envíe un mensaje firmado/cifrado puede que le pidan una clave de firma. FairEmail almacenará automáticamente la clave de firma seleccionada en la identidad utilizada para la próxima vez. Si necesita restablecer la clave de firma, simplemente guarde la identidad o mantenga pulsada la identidad en la lista de identidades y seleccione *Restablecer clave de firma*. La clave de firma seleccionada es visible en la lista de identidades. Si es necesario seleccionar una clave caso por caso puede crear múltiples identidades para la misma cuenta con la misma dirección de correo electrónico.

En la configuración de privacidad puede seleccionar el método de cifrado predeterminado (PGP o S/MIME), active *Firmar por defecto*, *Cifrar por defecto* y *Descifrar mensajes automáticamente*, pero tenga en cuenta que el descifrado automático no es posible si la interacción del usuario es requerida, como seleccionar una clave o leer un token de seguridad.

El texto/adjuntos a cifrar y el texto/adjunto descifrados se almacenan localmente sólo y nunca se añadirán al servidor remoto. Si quiere deshacer el descifrado, puedes usar el elemento de menú *Resincronizar* en el menú de tres puntos de la barra de acción del mensaje.

*PGP*

Necesitará instalar y configurar [OpenKeychain](https://f-droid.org/en/packages/org.sufficientlysecure.keychain/) primero. FairEmail ha sido probado con OpenKeychain versión 5.4. Es muy probable que las versiones posteriores sean compatibles, pero las versiones anteriores podrían no serlo.

**Importante**: la aplicación OpenKeychain es conocida por (silenciosamente) cerrarse cuando la aplicación que llama (FairEmail) no está autorizada aún y está recibiendo una clave pública existente. Puede solucionar esto intentando enviar un mensaje firmado/cifrado a un remitente con una clave pública desconocida.

**Importante**: si la aplicación OpenKeychain (ya) no puede encontrar una clave, puede que deba restablecer una clave previamente seleccionada. Esto se puede hacer manteniendo presionada una identidad en la lista de identidades (Configuración, paso 2, Administrar).

**Importante**: para permitir que aplicaciones como FairEmail se conecten de forma confiable al servicio OpenKeychain para cifrar/descifrar mensajes, podría ser necesario desactivar las optimizaciones de batería para la aplicación OpenKeychain.

**Importante**: la aplicación OpenKeychain necesita el permiso de contactos para funcionar correctamente.

**Importante**: en algunas versiones / dispositivos Android es necesario habilitar *Mostrar ventanas emergentes mientras se ejecuta en segundo plano* en los permisos adicionales de la configuración de la aplicación Android de la aplicación OpenKeychain. Sin este permiso el borrador será guardado, pero la ventana emergente de OpenKeychain para confirmar/seleccionar podría no aparecer.

FairEmail enviará el encabezado [Autocrypt](https://autocrypt.org/) para su uso por otros clientes de correo electrónico, pero sólo para mensajes firmados y cifrados porque demasiados servidores de correo tienen problemas con el a menudo largo encabezado Autocrypt. Tenga en cuenta que la forma más segura de iniciar un intercambio de correo electrónico cifrado es enviando mensajes firmados primero. Los encabezados Autocrypt recibidos serán enviados a la aplicación OpenKeychain para su almacenamiento al verificar una firma o descifrar un mensaje.

Toda la gestión de claves se delega a la aplicación OpenKeychain por razones de seguridad. Esto también significa que FairEmail no almacena claves PGP.

Se admite cifrado PGP embebido en los mensajes recibidos, pero las firmas PGP embebidas y PGP embebido en los mensajes salientes no son soportados, mire [aquí](https://josefsson.org/inline-openpgp-considered-harmful.html) sobre por qué no.

Los mensajes sólo firmados o sólo cifrados no son una buena idea, por favor vea aquí por qué no:

* [Consideraciones sobre OpenPGP Parte I](https://k9mail.github.io/2016/11/24/OpenPGP-Considerations-Part-I.html)
* [Consideraciones sobre OpenPGP Parte II](https://k9mail.github.io/2017/01/30/OpenPGP-Considerations-Part-II.html)
* [Consideraciones sobre OpenPGP Parte III Autocrypt](https://k9mail.github.io/2018/02/26/OpenPGP-Considerations-Part-III-Autocrypt.html)

Se admiten mensajes sólo firmados, los mensajes sólo cifrados no están soportados.

Errores comunes:

* *Sin clave*: no hay ninguna clave PGP disponible para una de las direcciones de correo electrónico listadas
* *Falta la clave para el cifrado*: probablemente hay una clave seleccionada en FairEmail que ya no existe en la aplicación OpenKeychain. Restablecer la clave (ver arriba) probablemente solucione este problema.

*S/MIME*

Cifrar un mensaje requiere la(s) clave(s) pública(s) del destinatario(s). Firmar un mensaje requiere su clave privada.

Las claves privadas son almacenadas por Android y se pueden importar a través de la configuración de seguridad avanzada de Android. Hay un acceso directo (botón) para esto en la configuración de privacidad. Android le pedirá que establezca un PIN, patrón o contraseña si no lo ha hecho antes. Si tiene un dispositivo Nokia con Android 9, por favor [lea esto primero](https://nokiamob.net/2019/08/10/a-bug-prevents-nokia-1-owners-from-unlocking-their-screen-even-with-right-pin-pattern/).

Tenga en cuenta que los certificados pueden contener múltiples claves para múltiples propósitos, por ejemplo para la autenticación, el cifrado y la firma. Android sólo importa la primera clave, así que para importar todas las claves, el certificado primero debe ser dividido. Esto no es muy trivial y se le aconseja que pida ayuda al proveedor del certificado.

Nótese que la firma S/MIME  con otros algoritmos que RSA es soportada, pero tenga en cuenta que otros clientes de correo electrónico tal vez no lo soporten. El cifrado S/MIME es posible únicamente con algoritmos simétricos, lo que significa que en la práctica se utiliza RSA.

El método de cifrado por defecto es PGP, pero el último método de cifrado utilizado será recordado para la identidad seleccionada para la próxima vez. Puede que necesite activar las opciones de envío en el menú de tres puntos de nuevo para poder seleccionar el método de cifrado.

Para permitir diferentes claves privadas para la misma dirección de correo electrónico, FairEmail siempre le permitirá seleccionar una clave cuando haya múltiples identidades con la misma dirección de correo electrónico para la misma cuenta.

Las claves públicas son almacenadas por FairEmail y pueden ser importadas al verificar una firma por primera vez o a través de la configuración de privacidad (formatos PEM o DER).

FairEmail verifica tanto la firma como la cadena completa de certificados.

Errores comunes:

* *No se ha encontrado ningún certificado que coincida con targetContraints*: esto seguramente significa que está usando una versión antigua de FairEmail
* *incapaz de encontrar una ruta de certificación válida para el objetivo solicitado*: básicamente esto significa que uno o más certificados intermedios o raíz no fueron encontrados
* *La clave privada no coincide con ninguna clave de cifrado*: la clave seleccionada no puede utilizarse para descifrar el mensaje, probablemente porque es la clave incorrecta
* *No hay clave privada*: no se ha seleccionado ningún certificado o no hay ningún certificado disponible en el almacén de claves Android

En caso de que la cadena de certificados sea incorrecta, puede pulsar el pequeño botón de información para mostrar todos los certificados. Después de los detalles del certificado se muestra el emisor o "auto-Firmado". Un certificado es auto-Firmado cuando el sujeto y el emisor son los mismos. Los certificados de una autoridad certificadora (CA) están marcados con "[keyCertSign](https://tools.ietf.org/html/rfc5280#section-4.2.1.3)". Los certificados encontrados en el almacén de claves Android están marcados con "Android".

Una cadena válida se ve así:

```
Su certificado > cero o más certificados intermedios > CA (raíz) certificado marcado con "Android"
```

Tenga en cuenta que una cadena de certificados siempre será inválida cuando no se pueda encontrar ningún certificado raíz en el almacén de claves Android, que es fundamental para la validación del certificado S/MIME.

Consulte [aquí](https://support.google.com/pixelphone/answer/2844832?hl=en) cómo puede importar certificados en la tienda de claves Android.

El uso de claves caducadas, mensajes cifrados/firmados embebidos y tokens de seguridad de hardware no está soportado.

Si está buscando un certificado S/MIME gratuito (de prueba), consulta [aquí](http://kb.mozillazine.org/Getting_an_SMIME_certificate) para ver las opciones. Por favor, asegúrese de [leer esto antes](https://davidroessli.com/logs/2019/09/free-smime-certificates-in-2019/#update20191219) si desea solicitar un certificado de S/MIME Actalis. Si está buscando un certificado S/MIME barato, he tenido buena experiencia con [Certum](https://www.certum.eu/en/smime-certificates/).

Cómo extraer una clave pública de un certificado S/MIME:

```
openssl pkcs12 -in filename.pfx/p12 -clcerts -nokeys -out cert.pem
```

Puedes decodificar firmas S/MIME, etc, [aquí](https://lapo.it/asn1js/).

La firma/cifrado S/MIME es una característica pro, pero todas las demás operaciones PGP y S/MIME son gratis para usar.

<br />

<a name="faq13"></a>
**(13) ¿Cómo funciona la búsqueda en dispositivo/servidor?**

Puede empezar a buscar mensajes en el remitente (de), destinatario (a, cc, cco), asunto, palabras clave o texto de mensaje mediante el uso de la lupa en la barra de acción de una carpeta. También puede buscar desde cualquier aplicación seleccionando *Buscar correo electrónico* en el menú emergente.

Buscar en la bandeja de entrada unificada buscará en todas las carpetas de todas las cuentas, buscar en la lista de carpetas sólo buscará en la cuenta asociada y buscar en una carpeta sólo buscará en esa carpeta.

Los mensajes se buscarán primero en el dispositivo. Habrá un botón de acción con un icono de buscar de nuevo en la parte inferior para continuar la búsqueda en el servidor. Puede seleccionar en qué carpeta continuar la búsqueda.

El protocolo IMAP no permite buscar en más de una carpeta al mismo tiempo. Buscar en el servidor es una operación costosa, por lo tanto no es posible seleccionar múltiples carpetas.

La búsqueda de mensajes locales no distingue mayúsulas/minúsculas y es sobre texto parcial. El texto del mensaje de los mensajes locales no se buscará si el texto del mensaje no se ha descargado todavía. Buscar en el servidor puede ser sensible a mayúsculas o minúsculas y puede estar en texto parcial o palabras enteras, dependiendo del proveedor.

Algunos servidores no pueden manejar la búsqueda en el texto del mensaje cuando hay un gran número de mensajes. Para este caso hay una opción para desactivar la búsqueda en el texto del mensaje.

Es posible usar operadores de búsqueda de Gmail prefijando un comando de búsqueda con *raw:*. Si ha configurado sólo una cuenta de Gmail, puede iniciar una búsqueda directa directamente en el servidor buscando desde la bandeja de entrada unificada. Si configuró varias cuentas de Gmail, primero tendrá que navegar a la lista de carpetas o a la carpeta de archivo (todos los mensajes) de la cuenta de Gmail en la que quiere buscar. Por favor, [vea aquí](https://support.google.com/mail/answer/7190) para los posibles operadores de búsqueda. Por ejemplo:

`
raw:larger:10M`

Buscar a través de un gran número de mensajes en el dispositivo no es muy rápido debido a dos limitaciones:

* [sqlite](https://www.sqlite.org/), el motor de base de datos de Android tiene un límite de tamaño de registro, evitando que los mensajes de texto se almacenen en la base de datos
* Las aplicaciones Android tienen memoria limitada para trabajar, incluso si el dispositivo tiene suficiente memoria disponible

Esto significa que la búsqueda de un texto de mensaje requiere que los archivos que contengan los textos del mensaje deban abrirse uno por uno para comprobar si el texto buscado está contenido en el archivo, que es un proceso relativamente costoso.

En la *configuración miscelánea* puede habilitar *Construir índice de búsqueda* para aumentar significativamente la velocidad de búsqueda en el dispositivo, pero tenga en cuenta que esto aumentará el uso de la batería y el espacio de almacenamiento. El índice de búsqueda se basa en palabras, por lo que no es posible buscar texto parcial. Buscar usando el índice de búsqueda es por defecto Y, así que buscar *manzana naranja* buscará manzana Y naranja. Las palabras separadas por comas resultan en la búsqueda de OR, así que por ejemplo *manzana, naranja* buscará manzana O naranja. Ambos se pueden combinar, así que buscar *manzana, naranja banana* buscará manzana O (naranja Y banana). Usar el índice de búsqueda es una característica pro.

Desde la versión 1.1315 es posible utilizar expresiones de búsqueda como esta:

```
manzana +banana -cereza ?nueces
```

Esto resultará en buscar de esta manera:

```
("manzana" Y "banana" Y NO "cereza") O "nueces"
```

Las expresiones de búsqueda pueden utilizarse para buscar en el dispositivo a través del índice de búsqueda y para buscar en el servidor de correo electrónico, pero no para buscar en el dispositivo sin índice de búsqueda por razones de rendimiento.

Buscar en el dispositivo es una característica gratuita, usar el índice de búsqueda y la búsqueda en el servidor es una característica pro.

<br />

<a name="faq14"></a>
**(14) ¿Cómo puedo configurar una cuenta de Outlook / Live / Hotmail?**

Se puede configurar una cuenta de Outlook / Live / Hotmail a través del asistente de configuración rápida y seleccionando *Outlook*.

Para utilizar una cuenta de Outlook, Live o Hotmail con autenticación de dos factores activada, necesita crear una contraseña de la aplicación. Vea [aquí](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification) para más detalles.

Vea [aquí](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) para las instrucciones de Microsoft.

Para configurar una cuenta de Office 365, consulte [esta sección de Preguntas Frecuentes](#user-content-faq156).

<br />

<a name="faq15"></a>
**(15) ¿Por qué sigue cargando el texto del mensaje?**

El encabezado del mensaje y el cuerpo del mensaje se obtienen por separado del servidor. El texto del mensaje de los mensajes más grandes no está siendo pre-obtenido en conexiones medidas y necesita ser obtenido bajo demanda al abrir el mensaje. El texto del mensaje seguirá cargándose si no hay conexión con la cuenta, vea también la siguiente pregunta, o si hay otras operaciones, como mensajes sincronizando, se están ejecutando.

Puede comprobar la lista de cuentas y carpetas para el estado de las cuentas y las carpetas (ver la leyenda para el significado de los iconos) y la lista de operaciones accesible a través del menú de navegación principal para operaciones pendientes (ver [estas Preguntas Frecuentes](#user-content-faq3) para el significado de las operaciones).

Si FairEmail se está retrasando debido a problemas de conectividad previos, por favor vea [esta sección de Preguntas Frecuentes](#user-content-faq123), puede forzar la sincronización a través del menú de tres puntos.

En la configuración de recepción puede establecer el tamaño máximo para la descarga automática de mensajes en conexiones medidas.

Las conexiones móviles son casi siempre medidas y algunos puntos de acceso Wi-Fi (de pago) también lo son.

<br />

<a name="faq16"></a>
**(16) ¿Por qué no se sincronizan los mensajes?**

Las posibles causas de que los mensajes no sean sincronizados (enviados o recibidos) son:

* La cuenta o carpeta(s) no están configuradas para sincronizar
* El número de días para sincronizar mensajes es demasiado bajo
* No hay conexión a Internet utilizable
* El servidor de correo electrónico no está disponible temporalmente
* Android detuvo el servicio de sincronización

Por lo tanto, compruebe la configuración de su cuenta y carpetas y compruebe si las cuentas/carpetas están conectadas (vea la leyenda en el menú de navegación para ver el significado de los iconos).

Si hay algún mensaje de error, por favor consulte [éstas Preguntas Frecuentes](#user-content-faq22).

En algunos dispositivos, donde hay muchas aplicaciones que compiten por la memoria, Android puede detener el servicio de sincronización como último recurso.

Algunas versiones de Android detienen aplicaciones y servicios en forma demasiado agresiva. Vea [este sitio web dedicado](https://dontkillmyapp.com/) y [este problema de Android](https://issuetracker.google.com/issues/122098785) para más información.

Deshabilitar las optimizaciones de batería (paso 4 de la configuración) reduce la posibilidad de que Android detenga el servicio de sincronización.

<br />

<a name="faq17"></a>
**~~(17) ¿Por qué no funciona la sincronización manual?~~**

~~Si el menú *Sincronizar ahora* está atenuado, no hay conexión con la cuenta.~~

~~Vea la pregunta anterior para más información.~~

<br />

<a name="faq18"></a>
**(18) ¿Por qué no siempre se muestra la vista previa del mensaje?**

La vista previa del texto del mensaje no se puede mostrar si el cuerpo del mensaje no ha sido descargado todavía. Vea también [estas Preguntas Frecuentes](#user-content-faq15).

<br />

<a name="faq19"></a>
**(19) ¿Por qué las características Pro son tan caras?**

La pregunta correcta es "*¿por qué hay tantos impuestos y tasas?*":

* IVA: 25 % (dependiendo de su país)
* Comisión de Google: 30 %
* Impuesto sobre la renta: 50 %
* <sub>Comisión de Paypal: 5-10 % dependiendo del país/monto</sub>

Por lo tanto, lo que queda para el desarrollador es sólo una fracción de lo que usted paga.

Tenga en cuenta que sólo algunas características avanzadas y de comodidad necesitan ser compradas, lo que significa que FairEmail es básicamente gratis de usar.

También tenga en cuenta que la mayoría de las aplicaciones gratuitas parecerán no ser sostenibles al final, mientras que FairEmail está correctamente mantenido y con soporte, y que las aplicaciones gratuitas pueden tener una trampa, como enviar información confidencial privada a Internet.

He estado trabajando en FairEmail casi todos los días por más de dos años, por lo que creo que el precio es más que razonable. Por esta razón tampoco habrá descuentos.

<br />

<a name="faq20"></a>
**(20) ¿Puedo obtener un reembolso?**

Si una característica Pro comprada no funciona como está previsto y esto no es causado por un problema en las características gratuitas y no puedo arreglar el problema de forma oportuna, usted puede obtener un reembolso. En todos los demás casos no es posible el reembolso. En ninguna circunstancia hay un reembolso posible para cualquier problema relacionado con las características gratuitas, ya que no se les pagó nada y porque pueden ser evaluadas sin ninguna limitación. Asumo mi responsabilidad como vendedor de entregar lo que se ha prometido y espero que usted se responsabilice de informarse de lo que está comprando.

<a name="faq21"></a>
**(21) ¿Cómo puedo activar la luz de notificación?**

Antes de Android 8 Oreo: hay una opción avanzada en la configuración para esto.

Android 8 Oreo y posteriores: vea [aquí](https://developer.android.com/training/notify-user/channels) sobre cómo configurar los canales de notificación. Puede utilizar el botón *Administrar notificaciones* en la configuración para ir directamente a los ajustes de notificación de Android. Tenga en cuenta que las aplicaciones no pueden cambiar los ajustes de notificación, incluyendo la configuración de la luz de notificación, en Android 8 Oreo y posteriores. Las aplicaciones diseñadas y orientadas a versiones antiguas de Android podrían seguir siendo capaces de controlar el contenido de las notificaciones, pero estas aplicaciones ya no se pueden actualizar y las versiones recientes de Android mostrarán una advertencia de que dichas aplicaciones están desactualizadas.

A veces es necesario deshabilitar la configuración *Mostrar vista previa del mensaje en notificaciones* o habilitar la configuración *Mostrar notificaciones sólo con un texto de vista previa* para solucionar un error en Android. Esto podría aplicarse también a los sonidos y vibración de notificaciones.

Establecer un color de luz antes de Android 8 no es compatible y no es posible en Android 8 y posteriores.

<br />

<a name="faq22"></a>
**(22) ¿Qué significa el error de cuenta/carpeta?**

FairEmail no oculta errores como aplicaciones similares a menudo lo hacen, por lo que es más fácil diagnosticar problemas.

FairEmail intentará reconectarse automáticamente después de un retraso. Este retraso se duplicará tras cada intento fallido de evitar que se agote la batería y que se bloquee permanentemente el acceso.

Hay errores generales y errores específicos para las cuentas de Gmail (ver abajo).

**Errores generales**

El error *... La autenticación falló ...* o *... AUTHENTICATE falló...* probablemente significa que su nombre de usuario o contraseña era incorrecto. Algunos proveedores esperan como nombre de usuario sólo *nombre de usuario* y otros tu dirección de correo electrónico completa *usuario@ejemplo.com*. Al copiar/pegar para introducir un nombre de usuario o contraseña, pueden copiarse caracteres invisibles, lo que también podría causar este problema. También se sabe que algunos administradores de contraseñas hacen esto incorrectamente. El nombre de usuario puede ser sensible a mayúsculas, así que intente sólo caracteres en minúsculas. La contraseña es casi siempre sensible a mayúsculas y minúsculas. Algunos proveedores requieren usar una contraseña de aplicación en lugar de la contraseña de la cuenta, así que por favor revise la documentación del proveedor. A veces es necesario habilitar primero el acceso externo (IMAP/SMTP) en el sitio web del proveedor. Otras posibles causas son que la cuenta está bloqueada o que el inicio de sesión ha sido restringido administrativamente de alguna manera, por ejemplo permitiendo iniciar sesión desde ciertas redes / direcciones IP solamente.

El error *... Demasiados intentos de autenticación incorrectos ...* probablemente significa que está utilizando una contraseña de cuenta de Yahoo en lugar de una contraseña de la aplicación. Por favor, consulte [estas Preguntas Frecuentes](#user-content-faq88) sobre cómo configurar una cuenta Yahoo.

El mensaje *... +OK ...* probablemente significa que un puerto POP3 (normalmente el número de puerto 995) está siendo usado para una cuenta IMAP (normalmente el puerto número 993).

Los errores *... saludo inválido ...*, *... requiere una dirección válida ...* y *... Parámetro HELO no se ajusta a la sintaxis RFC ...* puede resolverse cambiando la configuración de identidad avanzada *Utilice la dirección IP local en lugar del nombre de host*.

El error *... No se pudo conectar al host ...* significa que no hubo respuesta del servidor de correo en un tiempo razonable (20 segundos por defecto). Esto indica principalmente problemas de conectividad a internet, posiblemente causados por una VPN o por una aplicación de firewall. Puede intentar aumentar el tiempo de espera de conexión en la configuración de conexión de FairEmail, para cuando el servidor de correo es realmente lento.

El error *... Conexión rechazada...* significa que el servidor de correo electrónico o algo entre el servidor de correo y la aplicación, como un cortafuegos, rechazó activamente la conexión.

El error *... Red inaccesible ...* significa que el servidor de correo electrónico no fue accesible a través de la conexión a internet actual, por ejemplo, porque el tráfico de internet sólo está restringido al tráfico local.

El error *... Host no resuelto ...* o "*... No se puede resolver el host ...* significa que la dirección del servidor de correo no se ha podido resolver. Esto puede ser causado por el bloqueo de anuncios o por un servidor [DNS](https://en.wikipedia.org/wiki/Domain_Name_System) inaccesible o que no funciona correctamente.

El error *... Software causó fin de conexión ...* significa que el servidor de correo o algo entre FairEmail y el servidor de correo terminó activamente una conexión existente. Esto puede suceder, por ejemplo, cuando la conectividad se perdió abruptamente. Un ejemplo típico es activar el modo avión.

Los errores *... BYE Cerrando sesión ...*, *... Conexión restablecida por el par ...* significa que el servidor de correo ha terminado activamente una conexión existente.

El error *... Conexión cerrada por el par ...* podría ser causada por un servidor de Exchange no actualizado, vea [aquí](https://blogs.technet.microsoft.com/pki/2010/09/30/sha2-and-windows/) para más información.

Los errores *... Error de lectura ...*, *... Error de escritura ...*, *... Tiempo de lectura agotado ...*, *... Conexión rota ...* significa que el servidor de correo electrónico ya no responde o que la conexión a internet es mala.

El error *... Fin inesperado de flujo de entrada zlib ...* significa que no todos los datos fueron recibidos, posiblemente debido a una conexión mala o interrumpida.

El error *... fallo de conexión ...* podría indicar [Demasiadas conexiones simultáneas](#user-content-faq23).

La advertencia *... Codificación no soportada...* significa que el conjunto de caracteres del mensaje es desconocido o no soportado. FairEmail asumirá ISO-8859-1 (Latin1), que en la mayoría de los casos resultará en mostrar el mensaje correctamente.

Por favor, [vea aquí](#user-content-faq4) para los errores *... No confiable ... no en el certificado ...*, *... Certificado de seguridad no válido (no se puede verificar la identidad del servidor) ...* o *... Trust anchor for certification path not found ...*

Por favor, [vea aquí](#user-content-faq127) para el error *... Argumento(s) HELO sintácticamente inválido(s) ...*.

Por favor, [vea aquí](#user-content-faq41) para el error *... Handshake falló ...*.

Vea [aquí](https://linux.die.net/man/3/connect) para qué significan códigos de error como EHOSTUNREACH y ETIMEDOUT.

Las causas posibles son:

* Un cortafuegos o router está bloqueando conexiones al servidor
* El nombre de host o número de puerto no es válido
* Hay problemas con la conexión a internet
* Hay problemas con la resolución de nombres de dominio (Yandex: intente desactivar DNS privado en la configuración de Android)
* El servidor de correo electrónico se niega a aceptar conexiones (externas)
* El servidor de correo electrónico se niega a aceptar un mensaje, por ejemplo porque es demasiado grande o contiene enlaces inaceptables
* Hay demasiadas conexiones al servidor, vea también la siguiente pregunta

Muchas redes Wi-Fi públicas bloquean el correo electrónico saliente para prevenir el spam. A veces puede solucionar esto usando otro puerto SMTP. Consulte la documentación del proveedor para ver los números de puerto utilizables.

Si estás usando una [VPN](https://en.wikipedia.org/wiki/Virtual_private_network), el proveedor de VPN puede bloquear la conexión porque está intentando prevenir el spam de forma demasiado agresiva. Tenga en cuenta que [Google Fi](https://fi.google.com/) también está usando una VPN.

**Enviar errores**

Los servidores SMTP pueden rechazar los mensajes por [varias razones](https://en.wikipedia.org/wiki/List_of_SMTP_server_return_codes). Los mensajes demasiado grandes y el filtro de spam de un servidor de correo electrónico son las razones más comunes.

* El límite de tamaño del archivo adjunto para Gmail [es de 25 MB](https://support.google.com/mail/answer/6584)
* El límite de tamaño de archivo adjunto para Outlook y Office 365 [es de 20 MB](https://support.microsoft.com/en-us/help/2813269/attachment-size-exceeds-the-allowable-limit-error-when-you-add-a-large)
* El límite de tamaño del archivo adjunto para Yahoo [es de 25 MB](https://help.yahoo.com/kb/SLN5673.html)
* *554 5.7.1 Servicio no disponible; Host cliente xxxx.xx.xx.xxx bloqueado*, por favor [vea aquí](https://docs.gandi.net/en/gandimail/faq/error_types/554_5_7_1_service_unavailable.html)
* *501 Error de sintaxis - línea demasiado larga* es a menudo causada por el uso de un encabezado Autocrypt largo
* *503 5.5.0 Destinatario ya especificado* significa principalmente que una dirección está siendo utilizada como dirección A y CC
* *554 5.7.1 ... no se permite transmitir* significa que el servidor de correo electrónico no reconoce el nombre de usuario/dirección de correo electrónico. Por favor, compruebe el nombre de host y el nombre de usuario/dirección de correo electrónico en la configuración de identidad.

**Errores de Gmail**

La autorización de las cuentas de Gmail configuradas con el asistente rápido debe actualizarse periódicamente a través del [administrador de cuentas de Android](https://developer.android.com/reference/android/accounts/AccountManager). Esto requiere permisos de contactos/cuenta y conectividad a internet.

El error *... La autenticación falló ... Cuenta no encontrada ...* significa que una cuenta de Gmail previamente autorizada fue eliminada del dispositivo.

Los errores *... La autenticación falló ... Ningún token al actualizar...* significa que el administrador de cuentas de Android no pudo actualizar la autorización de una cuenta de Gmail.

El error *... La autenticación falló ... Credenciales inválidas... error de red...* significa que el administrador de cuentas de Android no pudo actualizar la autorización de una cuenta de Gmail debido a problemas con la conexión a Internet

El error *... La autenticación falló ... Credenciales inválidas ...* podrían ser causadas por haber revocado los permisos requeridos de cuenta/contactos. Simplemente inicie el asistente (pero no seleccione una cuenta) para conceder los permisos necesarios de nuevo.

El error *... ServiceDisabled ...* puede ser causado por inscribirse en el [Programa de Protección Avanzada](https://landing.google.com/advancedprotection/): "*Para leer su correo electrónico, puede (debe) usar Gmail - No podrá usar su cuenta de Google con algunas (todas) aplicaciones & servicios que requieren acceso a datos sensibles como sus correos electrónicos*", ver [aquí](https://support.google.com/accounts/answer/7519408?hl=en&ref_topic=9264881).

En caso de duda, puede solicitar [soporte](#user-content-support).

<br />

<a name="faq23"></a>
**(23) ¿Por qué me alerta ... ?**

*General*

Las alertas son mensajes de advertencia enviados por los servidores de correo electrónico.

*Demasiadas conexiones simultáneas* o *Número máximo de conexiones excedido*

Esta alerta se enviará cuando haya demasiadas conexiones de carpetas para la misma cuenta de correo electrónico al mismo tiempo.

Las causas posibles son:

* Hay varios clientes de correo electrónico conectados a la misma cuenta
* El mismo cliente de correo está conectado varias veces a la misma cuenta
* Las conexiones anteriores terminaron abruptamente, por ejemplo, al perder abruptamente la conectividad a Internet

Primero trate de esperar algún tiempo para ver si el problema se resuelve por sí mismo, de lo contrario:

* cambie a comprobar periódicamente los mensajes en la configuración de recepción, lo que dará como resultado abrir carpetas una a la vez
* o configure algunas carpetas para sondear en lugar de sincronizar (mantenga presioanda una carpeta de la lista de carpetas, editar propiedades)

El número máximo de conexiones de carpetas simultáneas para Gmail es 15, para que pueda sincronizar como máximo 15 carpetas simultáneamente en *todos* sus dispositivos al mismo tiempo. Por esta razón, las carpetas de *usuario* de Gmail están configuradas para sondear por defecto en lugar de sincronizar siempre. Cuando sea necesario o deseado, puede cambiar esto manteniendo presionada una carpeta en la lista de carpetas y seleccionando *Editar propiedades*. Vea [aquí](https://support.google.com/mail/answer/7126229) para más detalles.

Al usar un servidor Dovecot, puede que quiera cambiar la configuración [mail_max_userip_connections](https://doc.dovecot.org/settings/dovecot_core_settings/#mail-max-userip-connections).

<br />

<a name="faq24"></a>
**(24) ¿Qué es explorar mensajes en el servidor?**

Explorar mensajes en el servidor obtendrá los mensajes del servidor de correo en tiempo real cuando llegue al final de la lista de mensajes sincronizados, incluso cuando la carpeta está configurada para no sincronizar. Puede desactivar esta función en la configuración avanzada de la cuenta.

<br />

<a name="faq25"></a>
**(25) ¿Por qué no puedo seleccionar/abrir/guardar una imagen, adjunto o archivo?**

Cuando un elemento de menú para seleccionar/abrir/guardar un archivo está deshabilitado (atenuado) o cuando recibe el mensaje *El framework de acceso a almacenamiento no está disponible*, probablemente no está presente el [framework de acceso a almacenamiento](https://developer.android.com/guide/topics/providers/document-provider), un componente estándar de Android. Esto puede deberse a que su ROM personalizada no la incluye o porque fue eliminada activamente (debloated).

FairEmail no solicita permisos de almacenamiento, por lo que este framework es necesario para seleccionar archivos y carpetas. Ninguna aplicación, excepto tal vez gestores de archivos, dirigidos a Android 4.4 KitKat o posterior debería pedir permisos de almacenamiento porque permitiría el acceso a *todos* los archivos.

El framework de acceso al almacenamiento es proporcionado por el paquete *com.android.documentsui*, que es visible como la aplicación *Archivos* en algunas versiones de Android (por ejemplo en OxygenOS).

Puede habilitar (otra vez) el framework de acceso de almacenamiento con este comando adb:

```
pm install -k --user 0 com.android.documentsui
```

Alternativamente, puede ser capaz de habilitar la aplicación *Archivos* de nuevo usando la configuración de la aplicación de Android.

<br />

<a name="faq26"></a>
**(26) ¿Puedo ayudar a traducir FairEmail en mi propio idioma?**

Sí, puede traducir los textos de FairEmail a tu propio idioma [en Crowdin](https://crowdin.com/project/open-source-email). El registro es gratuito.

Si desea que su nombre o alias sea incluido en la lista de colaboradores en *Acerca de* de la aplicación, por favor [póngase en contacto conmigo](https://contact.faircode.eu/?product=fairemailsupport).

<br />

<a name="faq27"></a>
**(27) ¿Cómo puedo distinguir entre imágenes incrustadas y externas?**

Imagen externa:

![Imagen externa](https://github.com/M66B/FairEmail/blob/master/images/baseline_image_black_48dp.png)

Imagen incrustada:

![Imagen incrustada](https://github.com/M66B/FairEmail/blob/master/images/baseline_photo_library_black_48dp.png)

Imagen rota:

![Imagen rota](https://github.com/M66B/FairEmail/blob/master/images/baseline_broken_image_black_48dp.png)

Tenga en cuenta que la descarga de imágenes externas desde un servidor remoto puede ser usada para registrar que sí vio un mensaje, lo que probablemente no quiera si el mensaje es spam o malicioso.

<br />

<a name="faq28"></a>
**(28) ¿Cómo puedo administrar las notificaciones de la barra de estado?**

En la configuración encontrará un botón *Administrar notificaciones* para navegar directamente a la configuración de notificaciones de Android para FairEmail.

En Android 8.0 Oreo y posteriores puede administrar las propiedades de los canales de notificación individuales, por ejemplo para establecer un sonido de notificación específico o para mostrar notificaciones en la pantalla de bloqueo.

FairEmail tiene los siguientes canales de notificación:

* Servicio: usado para la notificación del servicio de sincronización, vea también [éstas Preguntas Frecuentes](#user-content-faq2)
* Enviar: usado para la notificación del servicio de envío
* Notificaciones: usado para notificaciones de mensajes nuevos
* Alertas: usado para notificaciones de advertencia
* Errores: usado para notificaciones de error

Vea [aquí](https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels) para más detalles sobre los canales de notificación. Resumiendo: toque el nombre del canal de notificación para acceder a la configuración del canal.

En Android antes de Android 8.0 Oreo puede configurar el sonido de notificación en los ajustes.

Vea [estas Preguntas Frecuentes](#user-content-faq21) si su dispositivo tiene una luz de notificación.

<br />

<a name="faq29"></a>
**(29) ¿Cómo puedo recibir notificaciones de mensajes nuevos para otras carpetas?**

Sólo mantenga presionada una carpeta, seleccione *Editar propiedades*, y habilite *Mostrar en bandeja de entrada unificada* o *Notificar mensajes nuevos* (disponible en Android 7 Nougat y posteriores solamente) y toque *Guardar*.

<br />

<a name="faq30"></a>
**(30) ¿Cómo puedo usar los ajustes rápidos proporcionados?**

Hay ajustes rápidos (mosaicos de configuración) disponibles para:

* activar/desactivar la sincronización globalmente
* mostrar el número de mensajes nuevos y marcarlos como vistos (no leídos)

Los ajustes rápidos requieren Android 7.0 Nougat o posterior. El uso de mosaicos de configuración se explica [aquí](https://support.google.com/android/answer/9083864).

<br />

<a name="faq31"></a>
**(31) ¿Cómo puedo utilizar los accesos directos proporcionados?**

Hay accesos directos disponibles para:

* redactar un nuevo mensaje a un contacto favorito
* configurar cuentas, identidades, etc

Los accesos directos requieren Android 7.1 Nougat o posterior. El uso de accesos directos se explica [aquí](https://support.google.com/android/answer/2781850).

<br />

<a name="faq32"></a>
**(32) ¿Cómo puedo comprobar si leer el correo electrónico es realmente seguro?**

Puede utilizar [Email Privacy Tester](https://www.emailprivacytester.com/) para esto.

<br />

<a name="faq33"></a>
**(33) ¿Por qué no funcionan las direcciones del remitente editadas?**

La mayoría de los proveedores sólo aceptan direcciones validadas cuando envían mensajes para prevenir el spam.

Por ejemplo, Google modifica los encabezados de mensajes como éste para direcciones *no verificadas*:

```
De: Alguien <somebody@example.org>
X-Google-Original-From: Alguien <somebody+extra@example.org>
```

Esto significa que la dirección del remitente editada fue automáticamente reemplazada por una dirección verificada antes de enviar el mensaje.

Tenga en cuenta que esto es independiente de recibir mensajes.

<br />

<a name="faq34"></a>
**(34) ¿Cómo se emparejan las identidades?**

Como es esperable, las identidades se emparejan por cuenta. Para mensajes entrantes serán comprobadas las direcciones *a*, *cc*, *cco*, *de* y *(X-)delivered/envelope/original-to* (en este orden) y para mensajes salientes (borradores, bandeja de salida y enviados) solo se comprobarán las direcciones *de*.

La dirección coincidente se mostrará como *vía* en la sección de direcciones de los mensajes recibidos (entre el encabezado del mensaje y el texto del mensaje).

Tenga en cuenta que las identidades deben estar habilitadas para poder ser emparejadas y que las identidades de otras cuentas no serán consideradas.

El emparejamiento sólo se hará una vez al recibir un mensaje, por lo que cambiar la configuración no cambiará los mensajes existentes. Sin embargo, puede borrar los mensajes locales manteniendo presionada una carpeta en la lista de carpetas y sincronizando los mensajes de nuevo.

Es posible configurar una [expresión regular](https://en.wikipedia.org/wiki/Regular_expression) en la configuración de identidad para que coincida con el nombre de usuario de una dirección de correo electrónico (la parte antes del signo @).

Tenga en cuenta que el nombre de dominio (la parte después del signo @) siempre debe ser igual al nombre de dominio de la identidad.

Si desea emparejar una dirección de correo electrónico comodín, ésta expresión regular está generalmente bien:

```
.*
```

Si desea emparejar con las direcciones de correo electrónico de propósito especial abc@ejemplo.com y xyx@ejemplo.com y también le gustaría tener una dirección de correo electrónico de respaldo main@ejemplo.com, podría hacer algo así:

* Identidad: abc@ejemplo.com; regex: **(?i)abc**
* Identidad: xyz@ejemplo.com; regex: **(?i)xyz**
* Identidad: main@ejemplo.com; regex: **^(?i)((?!abc|xyz).)\*$**

Las identidades coincidentes se pueden utilizar para colorear los mensajes. El color de identidad tiene prioridad sobre el color de la cuenta. Establecer colores de identidad es una característica pro.

<br />

<a name="faq35"></a>
**(35) ¿Por qué debo tener cuidado con ver imágenes, archivos adjuntos y el mensaje original?**

Ver imágenes almacenadas de forma remota (vea también [estas Preguntas Frecuentes](#user-content-faq27)) podría no solo decirle al remitente que ha visto el mensaje, sino también filtrar su dirección IP.

Abrir archivos adjuntos o ver un mensaje original podría cargar contenido remoto y ejecutar scripts, que no sólo puede causar fugas de información privada, sino que también puede ser un riesgo para la seguridad.

Tenga en cuenta que sus contactos podrían enviar sin saberlo mensajes maliciosos si se infectan con malware.

FairEmail formatea mensajes nuevamente causando que los mensajes se vean diferentes de los originales, pero también descubriendo enlaces de phishing.

Tenga en cuenta que los mensajes reformateados son a menudo más legibles que los mensajes originales porque los márgenes se eliminan, y los colores y tamaños de las fuentes se estandarizan.

La aplicación de Gmail muestra imágenes por defecto al descargar las imágenes a través de un servidor proxy de Google. Dado que las imágenes se descargan desde el servidor de origen [en tiempo real](https://blog.filippo.io/how-the-new-gmail-image-proxy-works-and-what-this-means-for-you/), esto es aún menos seguro porque Google también está implicado sin proporcionar muchos beneficios.

Puede mostrar imágenes y mensajes originales por defecto para los remitentes de confianza en cada caso marcando *No volver a preguntar esto para ...*.

Si desea restablecer las aplicaciones para *Abrir con*, por favor [vea aquí](https://www.androidauthority.com/how-to-set-default-apps-android-clear-621269/).

<br />

<a name="faq36"></a>
**(36) ¿Cómo se cifran los archivos de configuración?**

Versión corta: AES 256 bit

Versión larga:

* La clave de 256 bits se genera con *PBKDF2WithHmacSHA1* usando un salt aleatorio segura de 128 bits y 65536 iteraciones
* El cifrado es *AES/CBC/PKCS5Padding*

<br />

<a name="faq37"></a>
**(37) ¿Cómo se almacenan las contraseñas?**

Todas las versiones de Android compatibles [cifran todos los datos de usuario](https://source.android.com/security/encryption), así que todos los datos, incluyendo nombres de usuario, contraseñas, mensajes, etc, se almacenan cifrados.

Si el dispositivo está protegido con un PIN, patrón o contraseña, puede hacer que las contraseñas de cuenta e identidad sean visibles. Si esto es un problema porque está compartiendo el dispositivo con otras personas, considere usar [perfiles de usuario](https://www.howtogeek.com/333484/how-to-set-up-multiple-user-profiles-on-android/).

<br />

<a name="faq39"></a>
**(39) ¿Cómo puedo reducir el uso de la batería de FairEmail?**

Versiones recientes de Android reportan por defecto *uso de aplicaciones* como porcentaje en la pantalla de configuración de batería de Android. **Confusamente, *uso de la aplicación* no es lo mismo que *uso de la batería* y ¡ni siquiera está directamente relacionado con el uso de la batería!** El uso de la aplicación (mientras está en uso) será muy alto porque FairEmail está utilizando un servicio en primer plano que es considerado como uso constante de aplicaciones por Android. Sin embargo, esto no significa que FairEmail esté usando constantemente energía de la batería. El uso real de la batería se puede ver en esta pantalla:

*Configuración de Android*, *Batería*, menú de tres puntos *Uso de batería*, menú de tres puntos *Mostrar uso completo del dispositivo*

Como regla general, el uso de la batería debería estar por debajo o, en cualquier caso, no ser mucho más alto que *Red móvil en espera*. Si este no es el caso, por favor hágamelo saber.

Es inevitable que sincronizar mensajes use energía de batería porque requiere acceso a la red y acceso a la base de datos de mensajes.

Si está comparando el uso de la batería de FairEmail con otro cliente de correo electrónico, por favor asegúrese de que el otro cliente de correo esté configurado de forma similar. Por ejemplo, comparar sincronizar siempre (mensajes push) y la comprobación periódica (poco frecuente) de nuevos mensajes no es una comparación justa.

Reconectar a un servidor de correo electrónico consumirá energía extra de la batería, por lo que una conexión a internet inestable resultará en un uso adicional de la batería. En este caso puede que quiera sincronizar periódicamente, por ejemplo cada hora, en lugar de continuamente. Tenga en cuenta que sondear con frecuencia (más que cada 30-60 minutos) probablemente usará más energía de batería que sincronizar siempre porque conectarse al servidor y comparar los mensajes locales y remotos son operaciones costosas.

[En algunos dispositivos](https://dontkillmyapp.com/) es necesario *desactivar* optimizaciones de batería (configuración paso 4) para mantener las conexiones a los servidores de correo electrónico abiertos.

La mayor parte del uso de la batería, sin considerar la visualización de mensajes, se debe a la sincronización (recepción y envío) de mensajes. Por lo tanto, para reducir el uso de la batería, establezca el número de días para sincronizar el mensaje a un valor más bajo, especialmente si hay muchos mensajes recientes en una carpeta. Mantenga presionado el nombre de carpeta en la lista de carpetas y seleccione *Editar propiedades* para acceder a esta configuración.

Si tiene conexión a internet al menos una vez al día, es suficiente para sincronizar los mensajes sólo por un día.

Tenga en cuenta que puede establecer el número de días para *mantener* mensajes a un número mayor que para *sincronizar* mensajes. Por ejemplo, puede sincronizar los mensajes inicialmente para un gran número de días y después de que esto se haya completado reducir el número de días para sincronizar mensajes, pero dejar el número de días para mantener los mensajes.

En los ajustes de recepción puede habilitar sincronizar siempre los mensajes destacados, lo que le permitirá mantener mensajes antiguos mientras sincroniza mensajes durante un número limitado de días.

Desactivar la opción de carpeta *Descargar automáticamente los mensajes y archivos adjuntos* dará como resultado menos tráfico de red y, por tanto, menos consumo de batería. Puede desactivar esta opción, por ejemplo, para la carpeta enviados y archivo.

Sincronizar mensajes por la noche en general no es útil, por lo que puede ahorrar en el uso de la batería si no se sincroniza por la noche. En los ajustes puede seleccionar un programa para la sincronización de mensajes (esta es una característica Pro).

FairEmail sincronizará por defecto la lista de carpetas en cada conexión. Ya que las carpetas no son creadas, renombradas y eliminadas a menudo, puede ahorrar algo de uso de la red y la batería desactivando esto en los ajustes de recepción.

FairEmail verificará por defecto si los mensajes antiguos fueron borrados del servidor en cada conexión. Si no le importa que los mensajes antiguos que fueron borrados del servidor sigan siendo visibles en FairEmail, puede ahorrar algo de uso de la red y batería desactivando esto en los ajustes de recepción.

Algunos proveedores no siguen el estándar IMAP y no mantienen las conexiones abiertas lo suficiente, lo que obliga a FairEmail a reconectarse a menudo, causando un uso adicional de la batería. Puede inspeccionar el *Registro* a través del menú de navegación principal para comprobar si hay conexiones frecuentes (conexión cerrada/restablecida, error de/tiempo de lectura/escritura agotado, etc). Puede solucionar esto bajando el intervalo de keep-alive en la configuración avanzada de la cuenta, por ejemplo a 9 o 15 minutos. Tenga en cuenta que las optimizaciones de la batería necesitan ser desactivadas en el paso 4 de configuración para mantener vivas las conexiones.

Algunos proveedores envían cada dos minutos algo como '*Todavía aquí*' dando como resultado tráfico de red y que el dispositivo se despierte y causando un uso adicional innecesario de la batería. Puede inspeccionar el *Registro* a través del menú de navegación principal para comprobar si su proveedor está haciendo esto. Si su proveedor está usando [Dovecot](https://www.dovecot.org/) como servidor IMAP, podría pedir a su proveedor que cambie la configuración [imap_idle_notify_interval](https://wiki.dovecot.org/Timeouts) a un valor mayor o mejor aún, deshabilitarlo. Si su proveedor no es capaz o no está dispuesto a cambiar/deshabilitar esto, debería considerar cambiar a sincronizar periódicamente en lugar de continuamente. Puede cambiar esto en la configuración de recepción.

Si recibió el mensaje *Este proveedor no soporta mensajes push* mientras configura una cuenta, considere cambiar a un proveedor moderno que soporte mensajes push (IMAP IDLE) para reducir el uso de la batería.

Si su dispositivo tiene una pantalla [AMOLED](https://en.wikipedia.org/wiki/AMOLED), puede ahorrar batería mientras ve mensajes cambiando al tema negro.

Si la optimización automática en la configuración de recepción está activada, una cuenta se cambiará automáticamente a comprobar periódicamente si hay nuevos mensajes cuando el servidor de correo electrónico:

* Dice '*Todavía aquí*' dentro de 3 minutos
* El servidor de correo no soporta mensajes push
* El intervalo de keep-alive es inferior a 12 minutos

Además, las carpetas papelera y spam se establecerán automáticamente para comprobar si hay nuevos mensajes después de tres errores sucesivos de [demasiadas conexiones simultáneas](#user-content-faq23).

<br />

<a name="faq40"></a>
**(40) ¿Cómo puedo reducir el uso de datos de FairEmail?**

Puede reducir el uso de datos básicamente de la misma manera que reducir el uso de la batería, vea la pregunta anterior para sugerencias.

Es inevitable que se utilicen los datos para sincronizar mensajes.

Si se pierde la conexión con el servidor de correo electrónico, FairEmail siempre sincronizará los mensajes de nuevo para asegurarse de que no haya mensajes omitidos. Si la conexión es inestable, esto puede resultar en un uso adicional de datos. En este caso, es una buena idea reducir el número de días para sincronizar los mensajes al mínimo (ver la pregunta anterior) o cambiar a sincronización periódica de los mensajes (ajustes de recepción).

Por defecto, FairEmail no descarga mensajes de texto y archivos adjuntos mayores de 256 KiB cuando hay una conexión a internet medida (móvil o Wi-Fi de pago). Puede cambiar esto en los ajustes de conexión.

<br />

<a name="faq41"></a>
**(41) ¿Cómo puedo corregir el error 'Handshake falló' ?**

Hay varias causas posibles, así que por favor lea hasta el final de esta respuesta.

El error '*Handshake falló ... WRONG_VERSION_NUMBER ...*' puede significar que está intentando conectarse a un servidor IMAP o SMTP sin una conexión cifrada, típicamente usando el puerto 143 (IMAP) y el puerto 25 (SMTP), o que se está utilizando un protocolo equivocado (SSL/TLS o STARTTLS).

La mayoría de los proveedores proporcionan conexiones cifradas usando diferentes puertos, normalmente el puerto 993 (IMAP) y el puerto 465/587 (SMTP).

Si su proveedor no soporta conexiones cifradas, debería solicitar que lo haga posible. Si esto no es una opción, puede habilitar *Permitir conexiones inseguras* en la configuración avanzada Y en la de cuenta/identidad.

Vea también [estas Preguntas Frecuentes](#user-content-faq4).

El error '*Handshake falló ... SLV3_ALERT_ILEGAL_PARAMETER ...*' es causado por un error en la implementación del protocolo SSL o por una clave DH demasiado corta en el servidor de correo electrónico y lamentablemente no puede ser arreglado por FairEmail.

El error '*Handshake falló ... HANDSHAKE_FAILURE_ON_CLIENT_HELLO ...*' puede ser causado por el proveedor que todavía utiliza RC4, que ya no está soportado desde [Android 7](https://developer.android.com/about/versions/nougat/android-7.0-changes.html#tls-ssl).

El error '*Handshake falló ... UNSUPPORTED_PROTOCOL o TLSV1_ALERT_PROTOCOL_VERSION ...*' puede ser causado por la activación de conexiones endurecidas en la configuración de conexión o por Android que ya no soporta protocolos antiguos, como SSLv3.

Android 8 Oreo y posteriores [ya no soportan](https://developer.android.com/about/versions/oreo/android-8.0-changes#security-all) SSLv3. No hay manera de solucionar problemas por falta RC4 y SSLv3 porque se ha eliminado completamente de Android (que debería decir algo).

Puede utilizar [este sitio web](https://ssl-tools.net/mailservers) o [este sitio web](https://www.immuniweb.com/ssl/) para comprobar los problemas de SSL/TLS de los servidores de correo electrónico.

<br />

<a name="faq42"></a>
**(42) ¿Puedes añadir un nuevo proveedor a la lista de proveedores?**

Si el proveedor es utilizado por más que unas pocas personas, sí, con gusto.

Se necesita la siguiente información:

```
<provider
    name="Gmail"
    link="https://support.google.com/mail/answer/7126229" // link to the instructions of the provider
    type="com.google"> // esto no es necesario
    <imap
        host="imap.gmail.com"
        port="993"
        starttls="false" />
    <smtp
        host="smtp.gmail.com"
        port="465"
        starttls="false" />
</provider>
```

La EFF [escribe](https://www.eff.org/nl/deeplinks/2018/06/announcing-starttls-everywhere-securing-hop-hop-email-delivery): "*Adicionalmente, incluso si configura STARTTLS perfectamente y utiliza un certificado válido, todavía no hay garantía de que su comunicación sea cifrada.*"

Por lo tanto, las conexiones SSL puras son más seguras que usar [STARTTLS](https://en.wikipedia.org/wiki/Opportunistic_TLS) y por lo tanto preferidas.

Por favor, asegúrese de que recibir y enviar mensajes funciona correctamente antes de contactarme para agregar un proveedor.

Vea abajo sobre cómo contactarme.

<br />

<a name="faq43"></a>
**(43) ¿Puedes mostrar el original ... ?**

Mostrar original, muestra el mensaje original como el remitente lo ha enviado, incluyendo fuentes originales, colores, márgenes, etc. FairEmail lo hace y no modificará esto de ninguna manera, excepto para solicitar [TEXT_AUTOSIZING](https://developer.android.com/reference/android/webkit/WebSettings.LayoutAlgorithm), que *intentará* hacer el texto pequeño más legible.

<br />

<a name="faq44"></a>
**~~(44) ¿Puedes mostrar fotos de contacto / identiconos en la carpeta enviados?~~**

~~Fotos de contactos e identicons siempre se muestran para el remitente porque esto es necesario para los hilos de conversación.~~ ~~Obtener fotos de contacto para el remitente y el receptor no es realmente una opción porque obtener fotos de contacto es una operación costosa.~~

<br />

<a name="faq45"></a>
**(45) ¿Cómo puedo arreglar 'Esta clave no está disponible. Para utilizarla, ¡debes importarla como una propia!' ?**

Recibirá el mensaje *Esta clave no está disponible. Para usarla ¡debes importarla como una propia!* al intentar descifrar un mensaje con una clave pública. Para arreglar esto necesitará importar la clave privada.

<br />

<a name="faq46"></a>
**(46) ¿Por qué la lista de mensajes sigue actualizándose?**

Si ve un icono 'girando' en la parte superior de la lista de mensajes, la carpeta todavía está siendo sincronizada con el servidor remoto. Puede ver el progreso de la sincronización en la lista de carpetas. Vea la leyenda sobre lo que significan los iconos y números.

La velocidad de su dispositivo y conexión a internet y el número de días para sincronizar los mensajes determinarán cuánto tiempo tardará la sincronización. Tenga en cuenta que no debería establecer el número de días para sincronizar mensajes a más de un día en la mayoría de los casos, vea también [estas Preguntas Frecuentes](#user-content-faq39).

<br />

<a name="faq47"></a>
**(47) ¿Cómo puedo resolver el error 'No hay cuenta principal o no hay carpeta de borradores'?**

Recibirá el mensaje de error *No hay una cuenta principal o No hay carpeta de borradores* al intentar redactar un mensaje mientras no haya una cuenta establecida para ser la cuenta principal o cuando no haya ninguna carpeta de borradores seleccionada para la cuenta principal. Esto puede suceder, por ejemplo, cuando inicia FairEmail para redactar un mensaje desde otra aplicación. FairEmail necesita saber dónde almacenar el borrador, por lo que necesitará seleccionar una cuenta para ser la cuenta principal y/o tendrá que seleccionar una carpeta de borradores para la cuenta principal.

Esto también puede suceder cuando intenta responder a un mensaje o reenviar un mensaje desde una cuenta sin carpeta de borradores mientras no haya una cuenta principal o cuando la cuenta principal no tiene una carpeta de borradores.

Consulte [estas Preguntas Frecuentes](#user-content-faq141) para obtener más información.

<br />

<a name="faq48"></a>
**~~(48) ¿Cómo puedo resolver el error 'No hay cuenta principal o no hay carpeta de archivo' ?~~**

~~Obtendrás el mensaje de error *No hay cuenta principal o no hay carpeta de archivo* al buscar mensajes desde otra aplicación. FairEmail necesita saber dónde buscar, así que tendrá que seleccionar una cuenta para ser la cuenta principal y/o tendrá que seleccionar una carpeta de archivo para la cuenta principal.~~

<br />

<a name="faq49"></a>
**(49) ¿Cómo arreglar 'Una aplicación desactualizada envió una ruta de archivo en lugar de una secuencia de archivos'?**

Probablemente haya seleccionado o enviado un archivo adjunto o una imagen con un administrador de archivos desactualizado o una aplicación desactualizada que asume que todas las aplicaciones todavía tienen permisos de almacenamiento. Por razones de seguridad y privacidad, las aplicaciones modernas como FairEmail ya no tienen acceso completo a todos los archivos. Esto puede resultar en el mensaje de error *Una aplicación desactualizada envió una ruta de archivo en lugar de un flujo de archivo* si un nombre de archivo en lugar de un flujo de archivo está siendo compartido con FairEmail porque FairEmail no puede abrir archivos al azar.

Puede arreglar esto cambiando a un gestor de archivos actualizado o a una aplicación diseñada para versiones recientes de Android. Alternativamente, puede conceder acceso de lectura de FairEmail al espacio de almacenamiento en su dispositivo en la configuración de la aplicación de Android. Tenga en cuenta que esta solución provisional [ya no funcionará en Android Q](https://developer.android.com/preview/privacy/scoped-storage).

Vea también [la pregunta 25](#user-content-faq25) y [lo que Google escribe sobre eso](https://developer.android.com/training/secure-file-sharing/share-file#RespondToRequest).

<br />

<a name="faq50"></a>
**(50) ¿Puedes añadir una opción para sincronizar todos los mensajes?**

No se añadirá la opción sincronizar todos los mensajes (descargar todos) porque puede resultar fácilmente en errores de memoria y en el llenado del espacio de almacenamiento disponible. También puede resultar fácilmente en mucho uso de batería y datos. Los dispositivos móviles no son muy aptos para descargar y almacenar años de mensajes. Puede utilizar mejor la búsqueda en la función del servidor (ver [pregunta 13](#user-content-faq13)), que es más rápida y eficiente. Tenga en cuenta que buscar a través de un montón de mensajes almacenados localmente sólo retrasaría la búsqueda y usaría energía adicional de la batería.

<br />

<a name="faq51"></a>
**(51) ¿Cómo se ordenan las carpetas?**

Las carpetas se ordenan primero por orden de cuenta (por defecto en nombre de la cuenta) y dentro de una cuenta, con carpetas especiales del sistema en la parte superior, seguidos de carpetas configuradas para sincronizar. Dentro de cada categoría las carpetas se ordenan por nombre a mostrar. Puede establecer el nombre a mostrar manteniendo presionada una carpeta en la lista de carpetas y seleccionando *Editar propiedades*.

El elemento del menú de navegación (hamburguesa) *Ordenar carpetas* en la configuración pueden utilizarse para ordenar manualmente las carpetas.

<br />

<a name="faq52"></a>
**(52) ¿Por qué toma algún tiempo volver a conectar a una cuenta?**

No hay una manera confiable de saber si una conexión a una cuenta fue terminada de forma correcta o forzada. Intentar volver a conectar con una cuenta mientras la conexión de la cuenta se terminó forzadamente con demasiada frecuencia puede resultar en problemas como [demasiadas conexiones simultáneas](#user-content-faq23) o incluso la cuenta siendo bloqueada. Para prevenir estos problemas, FairEmail espera 90 segundos para intentar volver a conectarse.

Puede mantener pulsado *Configuración* en el menú de navegación para volver a conectar inmediatamente.

<br />

<a name="faq53"></a>
**(53) ¿Puedes pegar la barra de acción del mensaje en la parte superior/inferior?**

La barra de acción de mensajes funciona en un solo mensaje y la barra de acción inferior funciona en todos los mensajes de la conversación. Dado que a menudo hay más de un mensaje en una conversación, esto no es posible. Por otra parte, hay bastantes acciones específicas de mensajes, como por ejemplo reenviar.

Mover la barra de acción del mensaje hacia la parte inferior del mensaje no es atractivo visualmente porque ya hay una barra de acción de conversación en la parte inferior de la pantalla.

Tenga en cuenta que no hay muchas aplicaciones de correo electrónico, si las hay, que muestren una conversación como una lista de mensajes expandibles. Esto tiene muchas ventajas, pero también causa la necesidad de acciones específicas del mensaje.

<br />

<a name="faq54"></a>
**~~(54) ¿Cómo uso un prefijo de espacio de nombres?~~**

~~Un prefijo de espacio de nombres se utiliza para eliminar automáticamente los prefijos que a veces añaden los proveedores a los nombres de carpetas.~~

~~Por ejemplo la carpeta de spam de Gmail es llamada:~~

```
[Gmail]/Spam
```

~~Al establecer el prefijo del espacio de nombres a *[Gmail]* FairEmail automáticamente eliminará *[Gmail]/* de todos los nombres de carpetas.~~

<br />

<a name="faq55"></a>
**(55) ¿Cómo puedo marcar todos los mensajes como leídos / moverlos o borrarlos?**

Puedes usar selección múltiple para esto. Mantenga pulsado el primer mensaje, no levante el dedo y deslice hasta el último mensaje. Luego use el botón de tres puntos para ejecutar la acción deseada.

<br />

<a name="faq56"></a>
**(56) ¿Puedes añadir soporte para JMAP?**

Casi no hay proveedores que ofrezcan el protocolo [JMAP](https://jmap.io/), por lo que no vale la pena hacer un gran esfuerzo añadir soporte para esto en FairEmail.

<br />

<a name="faq57"></a>
**(57) ~~¿Puedo usar HTML en firmas?~~**

~~Sí, puede usar HTML en firmas si pega texto formateado en el campo de firma o usa el menú *Editar como HTML* para introducir HTML manualmente.~~

~~Tenga en cuenta que incluir enlaces e imágenes en los mensajes aumentará la probabilidad de que un mensaje sea visto como spam,~~ ~~especialmente cuando envía un mensaje a alguien por primera vez.~~

~~Vea [aquí](https://stackoverflow.com/questions/44410675/supported-html-tags-on-android-textview) para ver qué etiquetas HTML son soportadas.~~

<br />

<a name="faq58"></a>
**(58) ¿Qué significa un icono de correo electrónico abierto/cerrado?**

El icono de correo electrónico en la lista de carpetas puede estar abierto (contorneado) o cerrado (sólido):

![Imagen externa](https://github.com/M66B/FairEmail/blob/master/images/baseline_mail_outline_black_48dp.png)

Los cuerpos de los mensajes y los archivos adjuntos no se descargan por defecto.

![Imagen externa](https://github.com/M66B/FairEmail/blob/master/images/baseline_email_black_48dp.png)

Los cuerpos de los mensajes y los archivos adjuntos se descargan de forma predeterminada.

<br />

<a name="faq59"></a>
**(59) ¿Se pueden abrir mensajes originales en el navegador?**

Por razones de seguridad, los archivos con los textos originales del mensaje no son accesibles para otras aplicaciones, por lo que esto no es posible. En teoría el [Framework de Acceso a Almacenamiento](https://developer.android.com/guide/topics/providers/document-provider) podría utilizarse para compartir estos archivos, pero incluso Chrome de Google no puede manejar esto.

<br />

<a name="faq60"></a>
**(60) ¿Sabía que... ?**

* ¿Sabía que los mensajes favoritos pueden sincronizarse/mantenerse siempre? (esto puede ser activado en la configuración de recepción)
* ¿Sabía que puede mantener presionado el icono de 'redactar mensaje' para ir a la carpeta de borradores?
* ¿Sabía que hay una opción avanzada para marcar mensajes leídos al ser movidos? (archivar y enviar a papelera también es moverlos)
* ¿Sabía que puede seleccionar texto (o una dirección de correo electrónico) en cualquier aplicación en versiones recientes de Android y dejar que FairEmail lo busque?
* ¿Sabía que FairEmail tiene un modo tablet? Gire su dispositivo a modo apaisado y los hilos de conversación se abrirán en una segunda columna si hay suficiente espacio en la pantalla.
* ¿Sabía que puede mantener presionada una plantilla de respuesta para crear un borrador de mensaje a partir de la plantilla?
* ¿Sabía que puede dejar pulsado, mantener y deslizar para seleccionar una gama de mensajes?
* ¿Sabía que puede volver a intentar enviar mensajes tirando hacia abajo para actualizar en la bandeja de salida?
* ¿Sabía que puede deslizar una conversación a la izquierda o a la derecha para ir a la conversación siguiente o anterior?
* ¿Sabía que puede pulsar en una imagen para ver de dónde se descargará?
* ¿Sabía que puede mantener pulsado el icono de la carpeta en la barra de acciones para seleccionar una cuenta?
* ¿Sabía que puede mantener presionado el icono de la estrella en un hilo de conversación para establecer una estrella de color?
* ¿Sabía que puede abrir el cajón de navegación deslizando desde la izquierda, incluso cuando ve una conversación?
* ¿Sabía que puede mantener pulsado el icono de la persona para mostrar/ocultar los campos CC/CCO y recordar el estado de visibilidad para la próxima vez?
* ¿Sabía que puede insertar las direcciones de correo electrónico de un grupo de contactos Android a través del menú de tres puntos?
* ¿Sabía que si selecciona texto y pulsa responder, sólo se citará el texto seleccionado?

<br />

<a name="faq61"></a>
**(61) ¿Por qué algunos mensajes se muestran atenuados?**

Los mensajes atenuados (grises) son mensajes movidos localmente para los que el servidor aún no confirma el movimiento. Esto puede suceder cuando (todavía) no hay conexión con el servidor o la cuenta. Estos mensajes se sincronizarán después de una conexión con el servidor y la cuenta se haya realizado o, si esto nunca sucede, será borrado si son demasiado viejos para ser sincronizados.

Puede que necesite sincronizar manualmente la carpeta, por ejemplo tirando hacia abajo.

Puede ver estos mensajes, pero no puede volver a mover estos mensajes hasta que el movimiento anterior haya sido confirmado.

Las [operaciones pendientes](#user-content-faq3) se muestran en la vista de operaciones accesible desde el menú de navegación principal.

<br />

<a name="faq62"></a>
**(62) ¿Qué métodos de autenticación son compatibles?**

Los siguientes métodos de autenticación están soportados y utilizados en este orden:

* LOGIN
* PLAIN
* CRAM-MD5
* XOAUTH2 ([Gmail](https://developers.google.com/gmail/imap/xoauth2-protocol), [Yandex](https://tech.yandex.com/oauth/))
* NTLM (no probado)

Los métodos de autenticación SASL, aparte de CRAM-MD5, no son compatibles porque [JavaMail para Android](https://javaee.github.io/javamail/Android) no soporta autenticación SASL.

Si su proveedor requiere un método de autenticación no soportado, probablemente obtendrá el mensaje de error *La autenticación falló*.

[Indicación de nombre de servidor](https://en.wikipedia.org/wiki/Server_Name_Indication) es soportada por [todas las versiones de Android soportadas](https://developer.android.com/training/articles/security-ssl).

<br />

<a name="faq63"></a>
**(63) ¿Cómo se redimensionan las imágenes para mostrarlas en pantalla?**

Imágenes [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) y [JPEG](https://en.wikipedia.org/wiki/JPEG) embebidas o adjuntas grandes se redimensionarán automáticamente para mostrarlas en pantalla. Esto se debe a que los mensajes de correo electrónico están limitados en tamaño, dependiendo del proveedor mayormente entre 10 y 50 MB. Las imágenes se redimensionarán de forma predeterminada a un ancho y altura máximos de 1440 píxeles y se guardarán con un ratio de compresión del 90 %. Las imágenes se reducen usando factores numéricos enteros para reducir el uso de memoria y para conservar la calidad de imagen. Redimensionar automáticamente las imágenes adjuntas y/o embebidas y el tamaño máximo de la imagen de destino se puede configurar en los ajustes de envío.

Si desea cambiar el tamaño de las imágenes para cada caso, puede utilizar [Send Reduced](https://f-droid.org/en/packages/mobi.omegacentauri.SendReduced/) o una aplicación similar.

<br />

<a name="faq64"></a>
**~~(64) ¿Puedes añadir acciones personalizadas para deslizar hacia la izquierda/derecha?~~**

~~La cosa más natural que hacer al deslizar una entrada de la lista a la izquierda o a la derecha es eliminar la entrada de la lista.~~ ~~La acción más natural en el contexto de una aplicación de correo electrónico es mover el mensaje a otra carpeta.~~ ~~Puede seleccionar la carpeta a la que quiere moverlo en la configuración de la cuenta.~~

~~Otras acciones, como marcar mensajes como leídos y posponer mensajes están disponibles a través de selección múltiple.~~ ~~Puede mantener pulsado un mensaje para iniciar la selección múltiple. Vea también [esta pregunta](#user-content-faq55).~~

~~Deslizar a la izquierda o a la derecha para marcar un mensaje como leído o no leído no es natural porque el mensaje primero desaparece y luego regresa en una forma diferente.~~ ~~Note que hay una opción avanzada para marcar mensajes automáticamente como leídos al moverlos,~~ ~~que es en la mayoría de los casos un reemplazo perfecto para la secuencia de marcar como leído y mover a alguna carpeta.~~ ~~También puede marcar mensajes como leídos desde las notificaciones de nuevos mensajes.~~

~~Si quiere leer un mensaje más tarde, puede esconderlo hasta una hora específica usando el menú *posponer*.~~

<br />

<a name="faq65"></a>
**(65) ¿Por qué se muestran atenuados algunos archivos adjuntos?**

Los archivos adjuntos embebidos (imágenes) se muestran atenuados. Se supone que [los adjuntos embebidos](https://tools.ietf.org/html/rfc2183) se descargarán y mostrarán automáticamente, pero dado que FairEmail no siempre descarga archivos adjuntos automáticamente, vea también [estas Preguntas Frecuentes](#user-content-faq40), FairEmail muestra todos los tipos de archivos adjuntos. Para distinguir los archivos adjuntos embebidos y regulares, los archivos adjuntos embebidos se muestran atenuados.

<br />

<a name="faq66"></a>
**(66) ¿Está disponible FairEmail en la Biblioteca Familiar de Google Play?**

El precio de FairEmail es demasiado bajo, inferior al de la mayoría de aplicaciones similares, y hay [demasiadas comisiones e impuestos](#user-content-faq19), Google solo ya toma el 30 %, como para justificar que FairEmail esté disponible en la [Biblioteca Familiar de Google Play](https://support.google.com/googleone/answer/7007852). Tenga en cuenta que Google promueve la Biblioteca Familiar, pero hace que los desarrolladores la paguen y no contribuye nada.

<br />

<a name="faq67"></a>
**(67) ¿Cómo puedo posponer conversaciones?**

Seleccione una o más conversaciones (pulsación larga para iniciar la selección múltiple), pulse el botón de tres puntos y seleccione *Posponer ...*. Alternativamente, en la vista de mensajes expandida use *Posponer ...* en el mensaje menú de tres puntos 'más' o la acción de lapso de tiempo en la barra de acción inferior. Seleccione el tiempo que la conversación(s) debe posponerse y confirme tocando Aceptar. Las conversaciones se ocultarán durante el tiempo seleccionado y se mostrarán de nuevo después. Recibirá una notificación de nuevo mensaje como recordatorio.

También es posible posponer mensajes con [una regla](#user-content-faq71).

Puede mostrar mensajes pospuestos desmarcando *Filtrar fuera* > *Ocultar* en el menú de tres puntos.

Puedes pulsar en el pequeño icono de posponer para ver hasta cuándo está pospuesta una conversación.

Al seleccionar una duración cero de posponer puede cancelar el aplazamiento.

<br />

<a name="faq68"></a>
**~~(68) ¿Por qué el lector Adobe Acrobat no puede abrir archivos adjuntos PDF / aplicaciones Microsoft no abren documentos adjuntos?~~**

~~Adobe Acrobat Reader y las aplicaciones Microsoft todavía esperan acceso completo a todos los archivos almacenados.~~ ~~mientras que las aplicaciones deben usar el [framework de acceso a almacenamiento](https://developer.android.com/guide/topics/providers/document-provider) desde Android KitKat (2013)~~ ~~para tener acceso a archivos compartidos activamente. Esto es por razones de privacidad y seguridad.~~

~~Puede solucionar esto guardando el archivo adjunto y abriéndolo desde el lector de Adobe Acrobat / aplicación de Microsoft,~~ ~~pero se recomienda instalar un lector de documentos PDF actualizado y preferiblemente de código abierto,~~ ~~por ejemplo uno listado [aquí](https://github.com/offa/android-foss#-document--pdf-viewer).~~

<br />

<a name="faq69"></a>
**(69) ¿Puedes añadir desplazamiento automático hacia arriba al llegar un nuevo mensaje?**

La lista de mensajes se desplaza automáticamente hacia arriba al navegar desde una notificación de nuevo mensaje o después de una actualización manual. Siempre desplazarse automáticamente ante la llegada de nuevos mensajes interferiría con su propio desplazamiento, pero si lo desea, puede activarlo en la configuración.

<br />

<a name="faq70"></a>
**(70) ¿Cuándo se expandirán los mensajes automáticamente?**

Al navegar a una conversación un mensaje se expandirá si:

* Sólo hay un mensaje en la conversación
* Hay exactamente un mensaje no leído en la conversación

Hay una excepción: el mensaje no se ha descargado todavía y el mensaje es demasiado grande para descargar automáticamente en una conexión (móvil) medida. Puede establecer o desactivar el tamaño máximo del mensaje en la pestaña de 'conexión' de los ajustes.

Mensajes duplicados (archivados), mensajes en papelera y borradores no se cuentan.

Los mensajes se marcarán automáticamente como leídos al expandirse, a menos que esto se haya desactivado en la configuración de cada cuenta.

<br />

<a name="faq71"></a>
**(71) ¿Cómo uso las reglas de filtro?**

Puede editar las reglas de filtro manteniendo presionada una carpeta en la lista de carpetas.

Las nuevas reglas se aplicarán a los nuevos mensajes recibidos en la carpeta, no a los mensajes existentes. Puede marcar la regla y aplicarla a los mensajes existentes o, alternativamente, mantenga presionada la regla en la lista de reglas y seleccione *Ejecutar ahora*.

Necesitará dar un nombre a una regla y necesitará definir el orden en el que una regla debe ser ejecutada en relación a otras reglas.

Puede desactivar una regla y dejar de procesar otras reglas después de que se haya ejecutado una regla.

Las siguientes condiciones de regla están disponibles:

* El remitente contiene
* El destinatario contiene
* El asunto contiene
* Tiene archivos adjuntos
* El encabezado contiene
* Día/hora entre

Todas las condiciones de una regla deben ser verdaderas para que la acción de la regla se ejecute. Todas las condiciones son opcionales, pero debe haber al menos una condición, para evitar que coincida con todos los mensajes. Si quiere coincidir con todos los remitentes o todos los destinatarios, puede usar el carácter @ como condición porque todas las direcciones de correo electrónico contendrán este carácter.

Tenga en cuenta que las direcciones de correo electrónico están formateadas así:

`
"Alguien" <somebody@example.org>`

Puede usar varias reglas, posiblemente con un *dejar de procesar*, para una condición *o* o una condición *no*.

La coincidencia no es sensible a mayúsculas y minúsculas, a menos que utilice [expresiones regulares](https://en.wikipedia.org/wiki/Regular_expression). Consulte [aquí](https://developer.android.com/reference/java/util/regex/Pattern) para ver la documentación de las expresiones regulares de Java. Puede probar una regex [aquí](https://regexr.com/).

Tenga en cuenta que una expresión regular soporta un operador *o*, así que si quiere coincidir con varios remitentes, puede hacer esto:

`
.*alice@ejemplo\.org.*|.*bob@ejemplo\.org.*|.*carol@ejemplo\.org.*`

Tenga en cuenta que [punto modo todos](https://developer.android.com/reference/java/util/regex/Pattern#DOTALL) está habilitado para ser capaz de emparejar [encabezados desplegados](https://tools.ietf.org/html/rfc2822#section-3.2.3).

Puede seleccionar una de estas acciones para aplicar a los mensajes coincidentes:

* Ninguna acción (útil para *no*)
* Marcar como leído
* Marcar como no leído
* Ocultar
* Suprimir notificación
* Posponer
* Destacar
* Establecer importancia (prioridad local)
* Añadir palabra clave
* Mover
* Copiar (Gmail: etiqueta)
* Responder (con plantilla)
* Texto a voz (remitente y asunto)
* Automatización (Tasker, etc.)

Las reglas se aplican directamente después de que el encabezado del mensaje ha sido obtenido, pero antes de que el texto del mensaje haya sido descargado, por lo que no es posible aplicar las condiciones al texto del mensaje. Tenga en cuenta que los textos de mensajes grandes se descargan bajo demanda en una conexión medida para ahorrar en el uso de datos.

Si desea reenviar un mensaje, considere utilizar la acción mover en su lugar. Esto será más confiable que reenviar, ya que los mensajes reenviados pueden ser considerados como spam.

Dado que los encabezados de los mensajes no se descargan y almacenan por defecto para ahorrar batería y datos y para ahorrar espacio de almacenamiento no es posible previsualizar qué mensajes coincidirían con una condición de regla de encabezado.

Algunas condiciones comunes de encabezado (regex):

* *.&ast;Auto-Submitted:.&ast;* [RFC3834](https://tools.ietf.org/html/rfc3834)
* *.&ast;Content-Type: multipart/report.&ast;* [RFC3462](https://tools.ietf.org/html/rfc3462)

En el menú de mensajes de tres puntos *más* hay un elemento para crear una regla para un mensaje recibido con las condiciones más comunes ya ingresadas.

El protocolo POP3 no admite el establecer palabras clave y el movimiento o la copia de mensajes.

Usar reglas es una característica pro.

<br />

<a name="faq72"></a>
**(72) ¿Qué son las cuentas o identidades principales?**

La cuenta principal se utiliza cuando la cuenta es ambigua, por ejemplo cuando se inicia un nuevo borrador desde la bandeja de entrada unificada.

De la misma manera, la identidad primaria de una cuenta se utiliza cuando la identidad es ambigua.

Sólo puede haber una cuenta principal y sólo puede haber una identidad primaria por cuenta.

<br />

<a name="faq73"></a>
**(73) ¿Es seguro/eficiente mover mensajes a través de cuentas?**

Mover mensajes entre cuentas es seguro porque los mensajers originales raw serán descargados y movidos y porque los mensajes de origen serán eliminados sólo después de que los mensajes destino hayan sido añadidos

Mover mensajes en lote a través de cuentas es eficiente si tanto la carpeta de origen como la carpeta de destino están configurados para sincronizar, sino FairEmail necesita conectarse a la(s) carpeta(s) para cada mensaje.

<br />

<a name="faq74"></a>
**(74) ¿Por qué veo mensajes duplicados?**

Algunos proveedores, especialmente Gmail, listan todos los mensajes en todas las carpetas, excepto mensajes en papelera, también en la carpeta de archivados (todos los mensajes). FairEmail muestra todos estos mensajes de una manera no intrusiva para indicar que estos mensajes son de hecho el mismo mensaje.

Gmail permite que un mensaje tenga varias etiquetas, que se presentan a FairEmail como carpetas. Esto significa que los mensajes con múltiples etiquetas también se mostrarán varias veces.

<br />

<a name="faq75"></a>
**(75) ¿Puedes hacer una versión para iOS, Windows, Linux, etc?**

Se requiere mucho conocimiento y experiencia para desarrollar con éxito una aplicación para una plataforma específica, que es la razón por la que solo desarrollo aplicaciones para Android.

<br />

<a name="faq76"></a>
**(76) ¿Qué hace 'Limpiar mensajes locales'?**

El menú de carpeta *Limpiar mensajes locales* elimina los mensajes del dispositivo que también están presentes en el servidor. No elimina mensajes del servidor. Esto puede ser útil después de cambiar la configuración de la carpeta para no descargar el contenido del mensaje (texto y archivos adjuntos), por ejemplo para ahorrar espacio.

<br />

<a name="faq77"></a>
**(77) ¿Por qué a veces se muestran los mensajes con un pequeño retraso?**

Dependiendo de la velocidad de su dispositivo (velocidad del procesador y tal vez incluso más velocidad de memoria) los mensajes podrían mostrarse con un pequeño retraso. FairEmail está diseñado para manejar dinámicamente un gran número de mensajes sin agotar la memoria. Esto significa que los mensajes deben ser leídos desde una base de datos y que esta base de datos necesita ser vigilada por cambios, ambas cosas podrían causar pequeños retrasos.

Algunas características de comodidad, como agrupar mensajes para mostrar hilos de conversación y determinar el mensaje anterior/siguiente, toman un poco de tiempo extra. Tenga en cuenta que no hay *el* siguiente mensaje porque mientras tanto un nuevo mensaje podría haber sido recibido.

Al comparar la velocidad de FairEmail con aplicaciones similares, esto debería ser parte de la comparación. Es fácil escribir una aplicación similar, más rápida que sólo muestra una lista de mensajes linear mientras que posible usando demasiada memoria, pero no es tan fácil administrar adecuadamente el uso de recursos y ofrecer características más avanzadas como el hilo de conversación.

FairEmail se basa en los más avanzados [componentes de arquitectura Android](https://developer.android.com/topic/libraries/architecture/), así que hay poco espacio para mejoras de rendimiento.

<br />

<a name="faq78"></a>
**(78) ¿Cómo uso programas?**

En la configuración de recepción puede habilitar la programación y establecer el período de tiempo y el día de la en que los mensajes deben ser recibidos.

Tenga en cuenta que una hora de fin igual o anterior a la hora de inicio se considera 24 horas más tarde.

Para esquemas más complejos, puede establecer una o más cuentas a sincronización manual y enviar este comando a FairEmail para buscar nuevos mensajes:

```
(adb shell) am startservice -a eu.faircode.email.POLL
```

Para una cuenta específica:

```
(adb shell) am startservice -a eu.faircode.email.POLL --es account Gmail
```

También puede automatizar la activación y desactivación de la recepción de mensajes enviando estos comandos a FairEmail:

```
(adb shell) am startservice -a eu.faircode.email.ENABLE
(adb shell) am startservice -a eu.faircode.email.DISABLE
```

Para activar/desactivar una cuenta específica:

```
(adb shell) am startservice -a eu.faircode.email.ENABLE --es account Gmail
(adb shell) am startservice -a eu.faircode.email.DISABLE --es account Gmail
```

Tenga en cuenta que deshabilitar una cuenta ocultará la cuenta y todas las carpetas y mensajes asociados.

Puede enviar automáticamente comandos con [Tasker](https://tasker.joaoapps.com/userguide/en/intents.html) por ejemplo:

```
Nueva tarea: Algo reconocible
Categoría de acción: Misc/Intento de envío
Acción: eu.faircode.email.ENABLE
Objetivo: Servicio
```

Para activar/desactivar una cuenta con el nombre *Gmail*:

```
Extras: account:Gmail
```

Los nombres de cuenta son sensibles a mayúsculas y minúsculas.

La automatización puede utilizarse para programas más avanzados, como por ejemplo varios períodos de sincronización por día o diferentes periodos de sincronización para diferentes días.

Es posible instalar FairEmail en varios perfiles de usuario, por ejemplo un perfil personal y de trabajo, y para configurar FairEmail de manera diferente en cada perfil, que es otra posibilidad de tener diferentes programas de sincronización y sincronizar un conjunto diferente de cuentas.

También es posible crear [reglas](#user-content-faq71) con una condición de tiempo y posponer mensajes hasta la hora de finalización de la condición de hora. De esta forma es posible silenciar los mensajes relacionados con el negocio hasta el inicio de las horas de trabajo. Esto también significa que los mensajes estarán en su dispositivo cuando no haya conexión a internet, por ejemplo cuando vuele.


La programación es una característica pro.

<br />

<a name="faq79"></a>
**(79) ¿Cómo uso la sincronización bajo demanda (manual)?**

Normalmente, FairEmail mantiene una conexión a los servidores de correo electrónico configurados siempre que sea posible para recibir mensajes en tiempo real. Si no quiere esto, por ejemplo, para no ser molestado o ahorrar uso de la batería, simplemente deshabilite la recepción en los ajustes de recepción. Esto detendrá el servicio en segundo plano que se encarga de la sincronización automática y eliminará la notificación asociada a la barra de estado.

También puede habilitar *Sincronizar manualmente* en la configuración avanzada de la cuenta si desea sincronizar manualmente cuentas específicas.

Puede utilizar tire-hacia-abajo-para-actualizar en una lista de mensajes o utilizar el menú de carpeta *Sincronizar ahora* para sincronizar manualmente los mensajes.

Si desea sincronizar algunas o todas las carpetas de una cuenta manualmente, deshabilite la sincronización de las carpetas (pero no de la cuenta).

Probablemente querrá desactivar [explorar en el servidor](#user-content-faq24) también.

<br />

<a name="faq80"></a>
**~~(80) ¿Cómo arreglar el error 'Unable to load BODYSTRUCTURE'?~~**

~~El mensaje de error *Unable to load BODYSTRUCTURE* es causado por errores en el servidor de correo,~~ ~~vea [aquí](https://javaee.github.io/javamail/FAQ#imapserverbug) para más detalles.~~

~~FairEmail ya intenta solucionar estos errores, pero si esto falla, necesitará pedir ayuda a su proveedor.~~

<br />

<a name="faq81"></a>
**~~(81) ¿Puedes hacer el fondo del mensaje original oscuro en el tema oscuro?~~**

~~El mensaje original se muestra como el remitente lo ha enviado, incluyendo todos los colores. ~ ~~Cambiar el color de fondo no sólo haría que la vista original ya no sea original, también puede resultar en mensajes ilegibles.~~

<br />

<a name="faq82"></a>
**(82) ¿Qué es una imagen de seguimiento?**

Por favor, vea [aquí](https://en.wikipedia.org/wiki/Web_beacon) sobre lo que es exactamente una imagen de seguimiento. En resumen, las imágenes de seguimiento mantienen un seguimiento sobre si abrió un mensaje.

En la mayoría de los casos, FairEmail reconocerá automáticamente las imágenes de seguimiento y las reemplazará por este icono:

![Imagen externa](https://github.com/M66B/FairEmail/blob/master/images/baseline_my_location_black_48dp.png)

El reconocimiento automático de imágenes de seguimiento puede desactivarse en la configuración de privacidad.

<br />

<a name="faq84"></a>
**(84) ¿Para qué son los contactos locales?**

La información de contactos locales se basa en nombres y direcciones encontrados en mensajes entrantes y salientes.

El principal uso del almacenamiento de contactos locales es ofrecer la autocompleción cuando no se ha concedido permiso de contactos a FairEmail.

Otro uso es generar [accesos directos](#user-content-faq31) en versiones recientes de Android para enviar rápidamente un mensaje a personas con contacto frecuente. Esta es también la razón por la que se está registrando el número de veces que se ha contactado y la última vez que se ha contactado y por la que puede hacer que un contacto sea un favorito o excluirlo de los favoritos manteniéndolo presionado.

La lista de contactos es ordenada por número de veces contactado y la última vez contactado.

Por defecto, sólo los nombres y direcciones a las que envía mensajes serán grabados. Puede cambiar esto en la configuración de envío.

<br />

<a name="faq85"></a>
**(85) ¿Por qué no está disponible una identidad?**

Una identidad está disponible para enviar un nuevo mensaje o responder o reenviar un mensaje existente si:

* la identidad está configurada para sincronizar (enviar mensajes)
* la cuenta asociada está configurada para sincronizar (recibir mensajes)
* la cuenta asociada tiene una carpeta de borradores

FairEmail intentará seleccionar la mejor identidad basado en la dirección *a* del mensaje contestado / reenviado.

<br />

<a name="faq86"></a>
**~~(86) ¿Cuáles son las 'características extra de privacidad'?~~**

~~La opción avanzada *características extra de privacidad* activa:~~

* ~~Buscar al dueño de la dirección IP de un enlace~~
* ~~Detección y eliminación de [imágenes de seguimiento](#user-content-faq82)~~

<br />

<a name="faq87"></a>
**(87) ¿Qué significa 'credenciales no válidas'?**

El mensaje de error *credenciales no válidas* significa que el nombre de usuario y/o la contraseña son incorrectos, por ejemplo porque la contraseña fue cambiada o caducada, o que la autorización de la cuenta ha caducado.

Si la contraseña es incorrecta/caducada, tendrá que actualizar la contraseña en la cuenta y/o configuración de identidad.

Si la autorización de la cuenta ha caducado, tendrá que seleccionar la cuenta de nuevo. Es probable que también tenga que guardar la identidad asociada de nuevo.

<br />

<a name="faq88"></a>
**(88) ¿Cómo puedo usar una cuenta de Yahoo, AOL o Sky?**

Para autorizar una cuenta de Yahoo, AOL o Sky tendrá que crear una contraseña de la aplicación. Para las instrucciones, por favor vea aquí:

* [para Yahoo](https://help.yahoo.com/kb/generate-third-party-passwords-sln15241.html)
* [para AOL](https://help.aol.com/articles/Create-and-manage-app-password)
* [para Sky](https://www.sky.com/help/articles/getting-started-with-sky-yahoo-mail) (bajo *Otras aplicaciones de correo*)

Por favor vea [estas Preguntas Frecuentes](#user-content-faq111) sobre el soporte de OAuth.

Tenga en cuenta que Yahoo, AOL y Sky no son compatibles con mensajes push estándar. La aplicación de correo electrónico de Yahoo utiliza un protocolo para mensajes push propietario, indocumentado.

Los mensajes push requieren [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE) y el servidor de correo electrónico de Yahoo no reporta IDLE como capacidad:

```
Y1 CAPABILITY
* CAPABILITY IMAP4rev1 ID MOVE NAMESPACE XYMHIGHESTMODSEQ UIDPLUS LITERAL+ CHILDREN X-MSG-EXT UNSELECT OBJECTID
Y1 OK CAPABILITY completed
```

<br />

<a name="faq89"></a>
**(89) ¿Cómo puedo enviar mensajes de sólo texto plano?**

Por defecto, FairEmail envía cada mensaje tanto en texto plano como en formato HTML porque casi todos los destinatarios esperan mensajes con formato en estos días. Si quiere/necesita enviar mensajes de texto plano solamente, puede activarlo en las opciones de identidad avanzadas. Puede que desee crear una nueva identidad para esto si desea o necesita seleccionar el envío de mensajes de texto plano caso por caso.

<br />

<a name="faq90"></a>
**(90) ¿Por qué algunos textos están enlazados sin ser un enlace?**

FairEmail enlazará automáticamente los enlaces web no vinculados (http y https) y las direcciones de correo electrónico no enlazadas (mailto) para su comodidad. Sin embargo, texto y enlaces no son fácilmente distinguibles, especialmente con tantos [dominios de nivel superior](https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains) siendo palabras. Es por esto que los textos con puntos a veces se reconocen incorrectamente como enlaces, que es mejor que no reconocer algunos enlaces.

También se reconocerán enlaces para los protocolos tel, geo, rtsp y xmpp pero no se reconocen enlaces para protocolos menos habituales o menos seguros, como telnet y ftp.

<a name="faq91"></a>
**~~(91) ¿Puedes añadir sincronización periódica para ahorrar batería?~~**

~~Sincronizar mensajes es un proceso costoso porque los mensajes locales y remotos necesitan ser comparados,~~ ~~entonces sincronizar mensajes periódicamente no producirá un ahorro de energía de la batería, más probablemente al contrario.~~

~~Vea [estas Preguntas Frecuentes](#user-content-faq39) sobre cómo optimizar el uso de la batería.~~


<br />

<a name="faq92"></a>
**(92) ¿Puedes añadir filtro de spam, verificación de la firma DKIM y autorización SPF?**

El filtrado de spam, verificación de la firma [DKIM](https://en.wikipedia.org/wiki/DomainKeys_Identified_Mail) y autorización [SPF](https://en.wikipedia.org/wiki/Sender_Policy_Framework) son tareas de servidores de correo electrónico, no de un cliente de correo electrónico. Los servidores generalmente tienen más memoria y potencia de cómputo, por lo que son mucho más adecuados para esta tarea que los dispositivos con batería. Además, querrá que el correo no deseado sea filtrado para todos tus clientes de correo electrónico, incluyendo posiblemente el webmail, no sólo un solo cliente de correo electrónico. Además, los servidores de correo electrónico tienen acceso a la información, como la dirección IP, etc del servidor de conexión, a la que un cliente de correo electrónico no tiene acceso.

Por supuesto puede reportar mensajes como spam con FairEmail, que moverá los mensajes reportados a la carpeta de spam y entrena el filtro de spam del proveedor, que es como se supone que debe funcionar. Esto también se puede hacer automáticamente con [reglas de filtro](#user-content-faq71). Bloquear el remitente creará una regla de filtro para mover automáticamente los mensajes futuros del mismo remitente a la carpeta de spam.

Tenga en cuenta que no debe borrar mensajes de spam, tampoco de la carpeta de spam, porque el servidor de correo electrónico utiliza los mensajes en la carpeta de spam para "aprender" cuáles son los mensajes de spam.

Si recibe un montón de mensajes spam en su bandeja de entrada, lo mejor que puede hacer es contactar con el proveedor de correo electrónico para preguntar si el filtrado de spam puede mejorarse.

Además, FairEmail puede mostrar una pequeña bandera roja de advertencia cuando la autenticación DKIM, SPF o [DMARC](https://en.wikipedia.org/wiki/DMARC) falló en el servidor receptor. Puede activar/desactivar [verificación de autenticación](https://en.wikipedia.org/wiki/Email_authentication) en la configuración de mostrar.

FairEmail también puede mostrar una bandera de advertencia si el nombre de dominio de la (respuesta) dirección de correo electrónico del remitente no define un registro MX apuntando a un servidor de correo electrónico. Esto se puede activar en la configuración de recepción. Tenga en cuenta que esto ralentizará significativamente la sincronización de mensajes.

Si los mensajes legítimos fallan en la autenticación, debería notificar al remitente porque esto resultará en un alto riesgo de que los mensajes terminen en la carpeta de spam. Además, sin la debida autenticación hay un riesgo de que el remitente sea suplantado. El remitente puede usar [esta herramienta](https://www.mail-tester.com/) para verificar la autenticación y otras cosas.

<br />

<a name="faq93"></a>
**(93) ¿Puedes permitir la instalación/almacenamiento de datos en medios de almacenamiento externo (sdcard)?**

FairEmail utiliza servicios y alarmas, proporciona widgets y espera que el arranque se haya completado para ser iniciado al iniciar el dispositivo por lo que no es posible almacenar la aplicación en medios de almacenamiento externo, como una tarjeta sdcard. Vea también [aquí](https://developer.android.com/guide/topics/data/install-location).

Mensajes, archivos adjuntos, etc almacenados en medios de almacenamiento externo, como una tarjeta sdcard, pueden ser accedidos por otras aplicaciones y por lo tanto no son seguros. Vea [aquí](https://developer.android.com/training/data-storage) para más detalles.

Cuando sea necesario, puede guardar mensajes (raw) a través del menú de tres puntos justo encima del texto del mensaje y guardar los archivos adjuntos tocando en el ícono de disquete.

Si necesita guardar en el espacio de almacenamiento, puede limitar el número de mensajes de días que están siendo sincronizados y mantenidos. Puede cambiar estos ajustes manteniendo presionada una carpeta en la lista de carpetas y seleccionando *Editar propiedades*.

<br />

<a name="faq94"></a>
**(94) ¿Qué significa la banda roja/naranja al final del encabezado?**

La banda roja/naranja en el lado izquierdo del encabezado significa que la autenticación DKIM, SPF o DMARC falló. Vea también [estas preguntas frecuentes](#user-content-faq92).

<br />

<a name="faq95"></a>
**(95) ¿Por qué no se muestran todas las aplicaciones al seleccionar un archivo adjunto o una imagen?**

Por razones de privacidad y seguridad, FairEmail no tiene permisos para acceder directamente a los archivos, en su lugar se utiliza el Framework de Acceso al Almacenamiento, disponible y recomendado desde Android 4.4 KitKat (publicado en 2013), para seleccionar archivos.

Si una aplicación está listada depende de si la aplicación implementa un [proveedor de documentos](https://developer.android.com/guide/topics/providers/document-provider). Si la aplicación no aparece en la lista, puede que necesite pedir al desarrollador de la aplicación que añada soporte para el framework de acceso al almacenamiento.

Android Q hará más difícil e incluso imposible acceder directamente a los archivos, vea [aquí](https://developer.android.com/preview/privacy/scoped-storage) y [aquí](https://www.xda-developers.com/android-q-storage-access-framework-scoped-storage/) para más detalles.

<br />

<a name="faq96"></a>
**(96) ¿Dónde puedo encontrar los ajustes IMAP y SMTP?**

Los ajustes IMAP son parte de la configuración (personalizada) de la cuenta y los ajustes SMTP son parte de la configuración de identidad.

<br />

<a name="faq97"></a>
**(97) ¿Qué es la 'limpieza'?**

Cada cuatro horas FairEmail ejecuta un trabajo de limpieza que:

* Elimina los textos de mensajes antiguos
* Elimina archivos adjuntos antiguos
* Elimina archivos de imagen antiguos
* Elimina contactos locales antiguos
* Elimina entradas antiguas del log

Tenga en cuenta que el trabajo de limpieza sólo se ejecutará cuando el servicio de sincronización esté activo.

<br />

<a name="faq98"></a>
**(98) ¿Por qué todavía puedo elegir contactos después de revocar los permisos de los contactos?**

Después de revocar los permisos de contactos, Android ya no permite el acceso de FairEmail a tus contactos. Sin embargo, la selección de contactos es delegada a y realizada por Android y no por FairEmail, por lo que esto será posible sin permisos de contacto.

<br />

<a name="faq99"></a>
**(99) ¿Puedes añadir un editor de texto enriquecido o markdown?**

FairEmail proporciona un formato de texto común (negrita, cursiva, subrayado, tamaño de texto y color) mediante una barra de herramientas que aparece después de seleccionar algún texto.

Un editor de [texto enriquecido](https://en.wikipedia.org/wiki/Formatted_text) o [Markdown](https://en.wikipedia.org/wiki/Markdown) no sería utilizado por mucha gente en un pequeño dispositivo móvil y, más importante, Android no soporta un editor de texto enriquecido y la mayoría de los proyectos de código abierto del editor de texto son abandonados. Vea [aquí](https://forum.xda-developers.com/showpost.php?p=79061829&postcount=4919) para más detalles sobre esto.

<br />

<a name="faq100"></a>
**(100) ¿Cómo puedo sincronizar las categorías de Gmail?**

Puede sincronizar las categorías de Gmail creando filtros para etiquetar los mensajes categorizados:

* Crear un nuevo filtro vía Gmail > Configuración > Ver toda la configuración > Filtros y Direcciones Bloqueadas > Crear un nuevo filtro
* Introduzca una búsqueda de categoría (ver abajo) en el campo *Tiene las palabras* y haga clic en *Crear filtro*
* Marque *Aplicar la etiqueta* y seleccione una etiqueta y haga clic en *Crear filtro*

Categorías posibles:

```
categoría:social
categoría:actualizaciones
categoría:foros
categoría:promociones
```

Desafortunadamente, esto no es posible para la carpeta de mensajes pospuestos.

Puede utilizar *Forzar sincronización* en el menú de tres puntos de la bandeja de entrada unificada para permitir que FairEmail sincronice de nuevo la lista de carpetas y puede mantener pulsado las carpetas para habilitar la sincronización.

<br />

<a name="faq101"></a>
**(101) ¿Qué significa el punto azul/naranja en la parte inferior de las conversaciones?**

El punto muestra la posición relativa de la conversación en la lista de mensajes. El punto se mostrará naranja cuando la conversación sea la primera o la última en la lista de mensajes, de lo contrario será azul. El punto está pensado como ayuda cuando se desliza hacia la izquierda/derecha para ir a la conversación anterior/siguiente.

El punto está deshabilitado por defecto y puede ser activado con los ajustes de pantalla *Mostrar la posición de conversación relativa con un punto*.

<br />

<a name="faq102"></a>
**(102) ¿Cómo puedo habilitar la rotación automática de imágenes?**

Las imágenes se girarán automáticamente cuando el redimensionamiento automático de las imágenes esté activado en los ajustes (activado por defecto). Sin embargo, la rotación automática depende de que la información [Exif](https://en.wikipedia.org/wiki/Exif) esté presente y sea correcta, lo que no siempre es el caso. Particularmente no cuando se toma una foto con una aplicación de cámara desde FairEmail.

Tenga en cuenta que sólo [imágenes JPEG](https://en.wikipedia.org/wiki/JPEG) y [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) pueden contener información Exif.

<br />

<a name="faq103"></a>
**(103) ¿Cómo puedo grabar audio?**

Puede grabar audio si tiene una aplicación de grabación instalada que soporte el intento [RECORD_SOUND_ACTION](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media#RECORD_SOUND_ACTION). Si no se instala ninguna aplicación compatible, FairEmail no mostrará una acción/icono de audio.

Desafortunadamente y sorprendentemente, la mayoría de las aplicaciones de grabación no parecen soportar este intento (deberían).

<br />

<a name="faq104"></a>
**(104) ¿Qué necesito saber sobre el reporte de errores?**

* Los informes de error ayudarán a mejorar FairEmail
* El reporte de errores es opcional y opt-in
* El informe de errores puede ser activado/desactivado en la configuración, sección miscelánea
* Los informes de error se enviarán automáticamente de forma anónima a [Bugsnag](https://www.bugsnag.com/)
* Bugsnag para Android es [código abierto](https://github.com/bugsnag/bugsnag-android)
* Mire [aquí](https://docs.bugsnag.com/platforms/android/automatically-captured-data/) sobre qué datos se enviarán en caso de errores
* Vea [aquí](https://docs.bugsnag.com/legal/privacy-policy/) para ver la política de privacidad de Bugsnag
* Los informes de error se enviarán a *sessions.bugsnag.com:443* y *notify.bugsnag.com:443*

<br />

<a name="faq105"></a>
**(105) ¿Cómo funciona la opción Itinerancia como en casa?**

FairEmail verificará si el código de país de la tarjeta SIM y el código de país de la red están en los [países de la UE itinerancia como en casa](https://en.wikipedia.org/wiki/European_Union_roaming_regulations#Territorial_extent) y no asume itinerancia si los códigos de país son iguales y la opción avanzada de itinerancia como en casa está activada.

Por lo tanto, no tiene que desactivar esta opción si no tiene una SIM de la UE o no está conectado a una red de la UE.

<br />

<a name="faq106"></a>
**(106) ¿Qué lanzadores pueden mostrar una insignia con el número de mensajes no leídos?**

Por favor, [vea aquí](https://github.com/leolin310148/ShortcutBadger#supported-launchers) para ver una lista de lanzadores que pueden mostrar el número de mensajes no leídos.

Tenga en cuenta que el ajuste de notificación *Mostrar icono del lanzador con el número de nuevos mensajes* necesita estar habilitado (por defecto habilitado).

Sólo se contarán *nuevos* mensajes no leídos en carpetas configuradas para mostrar notificaciones de nuevos mensajes, de modo que los mensajes marcados de nuevo como no leídos y los mensajes en las carpetas configuradas para no mostrar notificaciones de mensajes nuevos no serán contados.

Dependiendo de lo que quiera, el ajuste de notificación *Permitir que el número de nuevos mensajes coincida con el número de notificaciones* debe ser activado o desactivado.

Esta característica depende del soporte de su lanzador. FairEmail simplemente "emite mensajes" el número de mensajes no leídos usando la librería ShortcutBadger. Si no funciona, esto no se puede arreglar con cambios en FairEmail.

Algunos lanzadores muestran '1' para [la notificación de monitoreo](#user-content-faq2), a pesar de que FairEmail solicita explícitamente no mostrar una insignia para esta notificación. Esto podría ser causado por un error en la aplicación del lanzador o en su versión de Android. Por favor, compruebe si el punto de notificación está desactivado para el canal de notificación de recepción (servicio). Puede ir a la configuración del canal de notificación correcta a través de la configuración de notificaciones de FairEmail. Esto puede no ser obvio, pero puede pulsar en el nombre del canal para más ajustes.

Tenga en cuenta que Tesla Unread [ya no es compatible](https://forum.xda-developers.com/android/general/bad-news-tesla-unread-devoloper-t3920415).

FairEmail también envía un intento de conteo de nuevos mensajes:

```
eu.faircode.email.NEW_MESAGE_COUNT
```

El número de mensajes nuevos no leídos estará en un parámetro entero "*count*".

<br />

<a name="faq107"></a>
**(107) ¿Cómo uso estrellas coloreadas?**

Puede establecer una estrella de color a través del menú de mensajes *más*, a través de una selección múltiple (iniciada por mantener pulsado un mensaje), haciendo una pulsación larga de una estrella en una conversación o automáticamente usando [reglas](#user-content-faq71).

Necesita saber que las estrellas coloreadas no están soportadas por el protocolo IMAP y por lo tanto no pueden sincronizarse con un servidor de correo electrónico. Esto significa que las estrellas coloreadas no serán visibles en otros clientes de correo electrónico y se perderán al descargar mensajes de nuevo. Sin embargo, las estrellas (sin color) se sincronizarán y serán visibles en otros clientes de correo electrónico, cuando sean compatibles.

Algunos clientes de correo electrónico utilizan palabras clave IMAP para colores. Sin embargo, no todos los servidores soportan palabras clave IMAP y además no hay palabras clave estándar para colores.

<br />

<a name="faq108"></a>
**~~(108) ¿Puedes añadir eliminar mensajes permanentemente desde cualquier carpeta?~~**

~~Cuando elimina mensajes de una carpeta, los mensajes se moverán a la papelera, así tiene la oportunidad de restaurar los mensajes.~~ ~~Puede eliminar permanentemente mensajes de la carpeta de papelera.~~ ~~Eliminar mensajes permanentemente desde otras carpetas quitaría el propósito de la papelera, por lo que esto no será añadido.~~

<br />

<a name="faq109"></a>
**~~(109) ¿Por qué 'seleccionar cuenta' sólo está disponible en versiones oficiales?~~**

~~Usar *seleccionar cuenta* para seleccionar y autorizar cuentas de Google requiere un permiso especial de Google por razones de seguridad y privacidad.~~ ~~Este permiso especial sólo puede ser adquirido para aplicaciones que un desarrollador gestiona y por las que es responsable.~~ ~~Compilaciones de terceros, como las de F-Droid, son gestionadas por terceros y son responsabilidad de estos terceros.~~ ~~Por lo tanto, sólo estos terceros pueden adquirir el permiso requerido de Google. ~ ~~Dado que estos terceros no soportan FairEmail, es muy probable que no soliciten el permiso requerido.~~

~~Puede resolver esto de dos maneras:~~

* ~~Cambiar a la versión oficial de FairEmail, vea [aquí](https://github.com/M66B/FairEmail/blob/master/README.md#downloads) para las opciones~~
* ~~Use contraseñas específicas de aplicación, vea [estas Preguntas Frecuentes](#user-content-faq6)~~

~~Usar *seleccionar cuenta* en versiones de terceros ya no es posible en versiones recientes.~~ ~~En versiones anteriores esto era posible, pero ahora resultará en el error *UNREGISTERED_ON_API_CONSOLE*.~~

<br />

<a name="faq110"></a>
**(110) ¿Por qué (algunos) mensajes están vacíos y/o adjuntos están corruptos?**

Mensajes vacíos y/o archivos adjuntos corruptos probablemente estén siendo causados por un error en el software del servidor. Se sabe que el software antiguo de Microsoft Exchange causa este problema. La mayoría de las veces puedes solucionar esto desactivando *la obtención parcial* en la configuración avanzada de la cuenta:

Configurar > Paso 1 > Administrar > Tocar cuenta > Tocar avanzado > Obtención parcial > desmarcar

Después de desactivar esta configuración, puede utilizar el menú del mensaje 'más' (tres puntos) para 'resincronizar' mensajes vacíos. Alternativamente, puede *eliminar mensajes locales* manteniendo presionado la(s) carpeta(s) en la lista de carpetas y sincronizar todos los mensajes de nuevo.

Desactivar la *obtención parcial* dará como resultado un mayor uso de memoria.

<br />

<a name="faq111"></a>
**(111) ¿Es compatible OAuth?**

OAuth para Gmail es compatible usando el asistente de configuración rápida. El gestor de cuentas de Android se utilizará para obtener y actualizar los tokens de OAuth para las cuentas seleccionadas en el dispositivo. OAuth para cuentas que no están en el dispositivo no está soportado porque Google requiere una [auditoría de seguridad anual](https://support.google.com/cloud/answer/9110914) ($15,000 a $75,000) para esto.

OAuth para Yandex es compatible con el asistente de configuración rápida.

Se admite OAuth para cuentas Office 365, pero Microsoft no ofrece OAuth para cuentas Outlook, Live y Hotmail (¿aún?).

El acceso OAuth para Yahoo fue solicitado, pero Yahoo nunca respondió a la petición. OAuth para AOL [fue desactivado](https://www.programmableweb.com/api/aol-open-auth) por AOL. Verizon es dueño de AOL y Yahoo, llamado colectivamente [Oath inc](https://en.wikipedia.org/wiki/Verizon_Media). Por lo tanto, es razonable asumir que OAuth ya no está soportado por Yahoo tampoco.

<br />

<a name="faq112"></a>
**(112) ¿Qué proveedor de correo electrónico recomiendas?**

FairEmail es un cliente de correo electrónico solamente, así que necesita traer su propia dirección de correo electrónico.

Hay muchos proveedores de correo electrónico entre los que elegir. Qué proveedor de correo electrónico es mejor para usted depende de sus deseos/requisitos. Consulte los sitios web de [Restore privacy](https://restoreprivacy.com/secure-email/) o [Privacy Tools](https://www.privacytools.io/providers/email/) para obtener una lista de proveedores de correo electrónico orientados a la privacidad con ventajas y desventajas.

Algunos proveedores, como ProtonMail, Tutanota, usan protocolos de correo electrónico propietarios, que hacen imposible el uso de aplicaciones de correo electrónico de terceros. Consulte [estas Preguntas Frecuentes](#user-content-faq129) para obtener más información.

Usar su propio (personalizado) nombre de dominio que es soportado por la mayoría de los proveedores de correo electrónico, hará más fácil cambiar a otro proveedor de correo electrónico.

<br />

<a name="faq113"></a>
**(113) ¿Cómo funciona la autenticación biométrica?**

Si su dispositivo tiene un sensor biométrico, por ejemplo un sensor de huella dactilar, puede activar/desactivar la autenticación biométrica en el menú de navegación (hamburguesa) de la pantalla de configuración. Cuando esté activado FairEmail requerirá autenticación biométrica después de un período de inactividad o después de que la pantalla haya sido apagada mientras FairEmail se ejecutaba. La actividad es la navegación dentro de FairEmail, por ejemplo abriendo un hilo de conversación. La duración del período de inactividad puede ser configurada en ajustes misceláneos. Cuando la autenticación biométrica está habilitada, las notificaciones de mensajes nuevos no mostrarán ningún contenido y FairEmail no será visible en la pantalla de recientes Android.

La autenticación biométrica está pensada para evitar que otros vean sus mensajes solamente. FairEmail se basa en el cifrado del dispositivo para el cifrado de datos, vea también [estas Preguntas Frecuentes](#user-content-faq37).

La autenticación biométrica es una característica pro.

<br />

<a name="faq114"></a>
**(114) ¿Puedes añadir una opcinó para importar la configuración de otras aplicaciones de correo electrónico?**

El formato de los archivos de configuración de la mayoría de las otras aplicaciones de correo electrónico no está documentado, por lo que esto es difícil. A veces es posible obtener el formato con ingeniería inversa, pero tan pronto como el formato de configuración cambie las cosas se romperán. Además, los ajustes son a menudo incompatibles. Por ejemplo, a diferencia de la mayoría de las otras aplicaciones de correo FairEmail tiene ajustes para el número de días para sincronizar los mensajes y para el número de días para mantener los mensajes, principalmente para ahorrar en uso de la batería. Además, configurar una cuenta/identidad con la configuración rápida es sencillo, por lo que realmente no vale la pena el esfuerzo.

<br />

<a name="faq115"></a>
**(115) ¿Puedes añadir chips de dirección de correo electrónico?**

Los [chips](https://material.io/design/components/chips.html) de dirección de correo electrónico se ven bien, pero no se pueden editar, lo cual es bastante inconveniente cuando escribió un error tipográfico en una dirección de correo electrónico.

Tenga en cuenta que FairEmail seleccionará la dirección sólo cuando se mantenga presionada una dirección, lo que facilita la eliminación de una dirección.

Los chips no son aptos para mostrar en una lista y dado que el encabezado del mensaje en una lista debería verse similar al encabezado del mensaje de la vista del mensaje no es una opción usar chips para ver mensajes.

Revertido [commit](https://github.com/M66B/FairEmail/commit/2c80c25b8aa75af2287f471b882ec87d5a5a5015).

<br />

<a name="faq116"></a>
**~~(116) ¿Cómo puedo mostrar imágenes en los mensajes de los remitentes confiables por defecto?~~**

~~Puede mostrar imágenes en los mensajes de los remitentes de confianza por defecto activando la configuración de mostrar*Mostrar automáticamente imágenes para contactos conocidos*.~~

~~Los contactos en la lista de contactos de Android se consideran conocidos y de confianza,~~ ~~a menos que el contacto esté en el grupo / tiene la etiqueta '*No confiable*' (no distingue mayúsculas).~~

<br />

<a name="faq38"></a>
<a name="faq117"></a>
**(117) ¿Puedes ayudarme a restaurar mi compra?**

Google gestiona todas las compras, por lo que como desarrollador tengo poco control sobre las compras. Por lo tanto, básicamente lo único que puedo hacer es dar algunos consejos:

* Asegúrese de tener una conexión activa a internet
* Asegúrese de que ha iniciado sesión con la cuenta de Google correcta y de que no hay ningún problema con su cuenta de Google
* Asegúrese de instalar FairEmail a través de la cuenta de Google correcta si configuró varias cuentas de Google en su dispositivo
* Abra la aplicación Play Store y espere al menos un minuto para darle tiempo para sincronizar con los servidores de Google
* Abrir FairEmail y navegar a la pantalla de características pro para permitir que FairEmail compruebe las compras

También puede intentar limpiar la caché de la aplicación Play Store a través de los ajustes de las aplicaciones de Android. Reiniciar el dispositivo puede ser necesario para que la Play Store reconozca la compra correctamente.

Tenga en cuenta que:

* Las compras se almacenan en la nube de Google y no se pueden perder
* No hay límite de tiempo en las compras, por lo que no pueden expirar
* Google no expone detalles (nombre, correo electrónico, etc.) sobre los compradores a los desarrolladores
* Una aplicación como FairEmail no puede seleccionar qué cuenta de Google utilizar
* Puede tomar un tiempo hasta que la aplicación Play Store haya sincronizado una compra a otro dispositivo
* Las compras en Play Store no se pueden utilizar sin la Play Store, que tampoco está permitido por las reglas de la Play Store

Si no puede resolver el problema con la compra, tendrá que ponerse en contacto con Google al respecto.

<br />

<a name="faq118"></a>
**(118) ¿Qué hace exactamente 'Eliminar parámetros de seguimiento'?**

Al marcar *Eliminar parámetros de seguimiento* se eliminarán todos los [parámetros UTM](https://en.wikipedia.org/wiki/UTM_parameters) de un enlace.

<br />

<a name="faq119"></a>
**~~(119) ¿Puedes añadir colores al widget de bandeja de entrada unificada?~~**

~~El widget está diseñado para lucir bien en la mayoría de pantallas de inicio/lanzadores haciéndolo monocromático y usando un fondo medio transparente.~~ ~~De esta manera el widget se integrará bien, permitiendo ademas leer correctamente.~~

~~Añadir colores causará problemas con algunos fondos y causará problemas de legibilidad, razón por la cual esto no será añadido.~~

Debido a limitaciones de Android, no es posible establecer dinámicamente la opacidad del fondo y tener esquinas redondeadas al mismo tiempo.

<br />

<a name="faq120"></a>
**(120) ¿Por qué no se eliminan las notificaciones de nuevos mensajes al abrir la aplicación?**

Las notificaciones de mensajes nuevos se eliminarán al deslizar las notificaciones hacia fuera o al marcar los mensajes asociados como leídos. Abrir la aplicación no eliminará las notificaciones de nuevos mensajes. Esto le da la opción de dejar las notificaciones de nuevos mensajes como un recordatorio de que todavía hay mensajes sin leer.

En Android 7 Nougat y después las notificaciones de nuevos mensajes serán [agrupadas](https://developer.android.com/training/notify-user/group). Al pulsar en la notificación de resumen se abrirá la bandeja de entrada unificada. La notificación de resumen se puede ampliar para ver las notificaciones individuales de nuevos mensajes. Pulsar en una notificación individual de nuevo mensaje abrirá la conversación en la que forma parte. Vea [estas Preguntas Frecuentes](#user-content-faq70) sobre cuándo los mensajes de una conversación se expandirán automáticamente y se marcarán como leídos.

<br />

<a name="faq121"></a>
**(121) ¿Cómo se agrupan los mensajes en una conversación?**

Por defecto FairEmail agrupa mensajes en conversaciones. Esto se puede cambiar en los ajustes de mostrar.

FairEmail agrupa los mensajes basado en los encabezados estándar *Message-ID* estándar, *In-Reply-To* y *References*. FairEmail no agrupa según otros criterios, como el asunto, porque esto podría resultar en agrupar mensajes no relacionados y sería a expensas del aumento del uso de la batería.

<br />

<a name="faq122"></a>
**~~(122) ¿Por qué se muestra el nombre del destinatario/dirección de correo electrónico con un color de advertencia?~~**

~~El nombre del destinatario y/o la dirección de correo electrónico en la sección de direcciones se mostrarán en un color de advertencia~~ ~~cuando el nombre del dominio del remitente y el nombre del dominio de la dirección *a* no coinciden.~~ ~~principalmente esto indica que el mensaje fue recibido *vía* una cuenta con otra dirección de correo electrónico.~~

<br />

<a name="faq123"></a>
**(123) ¿Qué pasará cuando FairEmail no pueda conectarse a un servidor de correo electrónico?**

Cuando FairEmail no puede conectarse a un servidor de correo para recibir mensajes, por ejemplo cuando la conexión a Internet es mala o un cortafuegos o una VPN está bloqueando la conexión, FairEmail esperará 8, 16 y 32 segundos mientras mantiene el dispositivo despierto (=use batería) e intentará conectarse de nuevo. Si esto falla, FairEmail programará una alarma para volver a intentarlo después de 15, 30 y 60 minutos y dejar que el dispositivo duerma (=sin uso de batería).

Entre los cambios de conectividad hay una espera de 90 segundos para dar al servidor de correo la oportunidad de descubrir la conexión antigua está rota. Esto es necesario porque la conexión a internet de un dispositivo móvil a menudo se pierde abruptamente y para prevenir el problema descrito en [estas Preguntas Frecuentes](#user-content-faq23).

Tenga en cuenta que [modo doze de Android](https://developer.android.com/training/monitoring-device-state/doze-standby) no permite despertar el dispositivo antes de 15 minutos.

*Forzar sincronización* en el menú de tres puntos de la bandeja de entrada unificada puede utilizarse para permitir que FairEmail intente reconectarse sin esperar.

El envío de mensajes se reintentará sólo en los cambios de conectividad (reconectando a la misma red o conectándose a otra red) para evitar que el servidor de correo bloquee la conexión permanentemente. Puede tirar hacia abajo la bandeja de salida para volver a intentarlo manualmente.

Tenga en cuenta que el envío no se reintentará en caso de problemas de autenticación y cuando el servidor rechazó el mensaje. En este caso puede abrir/expandir el mensaje y usar el icono de deshacer para mover el mensaje a la carpeta de borradores, puede cambiarlo y enviarlo de nuevo.

<br />

<a name="faq124"></a>
**(124) ¿Por qué recibo 'Mensaje demasiado grande o demasiado complejo para mostrar'?**

El mensaje *Mensaje demasiado grande o demasiado complejo para mostrar* se mostrará si hay más de 100.000 caracteres o más de 500 enlaces en un mensaje. Reformatear y mostrar estos mensajes tomará demasiado tiempo. Puede intentar usar la vista original de mensajes, realizada por el navegador, en su lugar.

<br />

<a name="faq125"></a>
**(125) ¿Cuáles son las características experimentales actuales?**

* ...

<br />

<a name="faq126"></a>
**(126) ¿Se pueden enviar previsualizaciones de mensajes a mi wearable?**

FairEmail obtiene un mensaje en dos pasos:

1. Obtiene encabezados del mensaje
1. Obtiene texto del mensaje y archivos adjuntos

Justo después del primer paso se notificarán nuevos mensajes. Sin embargo, sólo hasta después del segundo paso el texto del mensaje estará disponible. FairEmail actualiza las notificaciones existentes con una vista previa del texto del mensaje, pero desafortunadamente las notificaciones en wearables no pueden ser actualizadas.

Dado que no hay garantía de que un texto del mensaje se obtenga siempre después de un encabezado del mensaje, no es posible garantizar que una notificación de nuevo mensaje con un texto de vista previa se enviará siempre a un wearable.

Si usted piensa que esto es suficientemente bueno, puede activar la opción de notificación *Sólo enviar notificaciones con vista previa de mensaje a dispositivos wearables* y si esto no funciona, puede intentar activar la opción de notificación *Mostrar notificaciones sólo con un texto de vista previa*.

Si desea que se le envíe el mensaje completo a su wearable, puede activar la opción de notificación *Vista previa de todo el texto*. Tenga en cuenta que se sabe que algunos wearables fallan con esta opción habilitada.

Si usa un wearable Samsung con la aplicación Galaxy Wearable (Samsung Gear), puede necesitar habilitar las notificaciones para FairEmail cuando el ajuste *Notificaciones*, *Las aplicaciones instaladas en el futuro* esté desactivado en esta aplicación.

<br />

<a name="faq127"></a>
**(127) ¿Cómo puedo arreglar 'argumento(s) HELO sintácticamente invalido(s)'?**

El error *... Argumento(s) HELO sintácticamente invalido(s) ...* significa que el servidor SMTP rechazó la dirección IP local o el nombre del host. Probablemente puede corregir este error activando o deshabilitando la opción de indentidad avanzada *Use la dirección IP local en lugar del nombre de host*.

<br />

<a name="faq128"></a>
**(128) ¿Cómo puedo restablecer las preguntas, por ejemplo para mostrar imágenes?**

Puede restablecer las preguntas formuladas a través del menú de tres puntos en los ajustes misceláneos.

<br />

<a name="faq129"></a>
**(129) ¿Son compatibles ProtonMail, Tutanota?**

ProtonMail utiliza un protocolo de correo electrónico propietario y [no soporta directamente IMAP](https://protonmail.com/support/knowledge-base/imap-smtp-and-pop3-setup/), por lo que no puede usar FairEmail para acceder a ProtonMail.

Tutanota utiliza un protocolo de correo electrónico propietario y [no soporta IMAP](https://tutanota.com/faq/#imap), por lo que no puede usar FairEmail para acceder a Tutanota.

<br />

<a name="faq130"></a>
**(130) ¿Qué significa el error ... del mensaje?**

Una serie de líneas con textos naranjas o rojos con información técnica significa que el modo de depuración estaba habilitado en los ajustes misceláneos.

La advertencia *Ningún servidor encontrado en...* significa que no había ningún servidor de correo electrónico registrado en el nombre de dominio indicado. Responder al mensaje podría no ser posible y podría resultar en un error. Esto podría indicar una dirección de correo electrónico falsificada y/o spam.

El error *... ParseException ...* significa que hay un problema con un mensaje recibido, probablemente causado por un error en el software de envío. FairEmail resolverá esto en la mayoría de los casos, por lo que este mensaje puede ser considerado como una advertencia en lugar de un error.

El error *...SendFailedException...* significa que hubo un problema al enviar un mensaje. El error incluirá casi siempre una razón. Las razones comunes son que el mensaje era demasiado grande o que una o más direcciones de destinatario no eran válidas.

La advertencia *El mensaje es demasiado grande para caber en la memoria disponible* significa que el mensaje es mayor de 10 MiB. Incluso si su dispositivo tiene suficiente espacio de almacenamiento Android proporciona memoria de trabajo limitada a las aplicaciones, que limita el tamaño de los mensajes que se pueden manejar.

Consulte [aquí](#user-content-faq22) para ver otros mensajes de error en la bandeja de salida.

<br />

<a name="faq131"></a>
**(131) ¿Puedes cambiar la dirección para deslizar al mensaje anterior/siguiente?**

Si lee de izquierda a derecha, deslizar hacia la izquierda mostrará el siguiente mensaje. De la misma manera, si lee de derecha a izquierda, deslizar a derecha mostrará el siguiente mensaje.

Este comportamiento me parece bastante natural, porque es similar a pasar páginas.

De todos modos, hay un ajuste de comportamiento para invertir la dirección del deslizamiento.

<br />

<a name="faq132"></a>
**(132) ¿Por qué las notificaciones de nuevos mensajes son silenciosas?**

Las notificaciones son silenciosas por defecto en algunas versiones MIUI. Por favor vea [aquí](http://en.miui.com/thread-3930694-1-1.html) cómo puede arreglar esto.

Hay un error en algunas versiones de Android causando que [setOnlyAlertOnce](https://developer.android.com/reference/android/app/Notification.Builder#setOnlyAlertOnce(boolean)) silencie las notificaciones. Ya que FairEmail muestra las notificaciones de nuevos mensajes justo después de obtener los encabezados de los mensajes y FairEmail necesita actualizar las notificaciones de nuevos mensajes después de obtener el texto del mensaje más tarde, esto no puede ser arreglado o solucionado por FairEmail.

Android podría limitar el sonido de las notificaciones, lo que puede causar que algunas notificaciones de mensajes nuevos sean silenciosas.

<br />

<a name="faq133"></a>
**(133) ¿Por qué ActiveSync no es compatible?**

El protocolo ActiveSync de Microsoft Exchange [está patentado](https://en.wikipedia.org/wiki/Exchange_ActiveSync#Licensing) y por lo tanto no puede ser soportado. Por esta razón no encontrará muchos otros clientes de correo electrónico que soporten ActiveSync.

El protocolo de Servicios Web de Microsoft Exchange [se está eliminando](https://techcommunity.microsoft.com/t5/Exchange-Team-Blog/Upcoming-changes-to-Exchange-Web-Services-EWS-API-for-Office-365/ba-p/608055).

Tenga en cuenta que la descripción de FairEmail comienza con la observación de que los protocolos no estándar, como Microsoft Exchange Web Services y Microsoft ActiveSync no son compatibles.

<br />

<a name="faq134"></a>
**(134) ¿Puedes añadir eliminar mensajes locales?**

*POP3*

En la configuración de la cuenta (Configuración, paso 1, Administrar, tocar cuenta) puede activar *Dejar mensajes eliminados en el servidor*.

*IMAP*

Dado que el protocolo IMAP está destinado a sincronizar en doble vía, eliminar un mensaje del dispositivo resultaría en recuperar el mensaje de nuevo al sincronizar de nuevo.

Sin embargo, FairEmail soporta ocultar mensajes, a través del menú de tres puntos en la barra de acción justo encima del texto del mensaje o seleccionando varios mensajes en la lista de mensajes. Básicamente esto es lo mismo que "dejar en el servidor" del protocolo POP3 con la ventaja de que puede mostrar los mensajes de nuevo cuando sea necesario.

Tenga en cuenta que es posible establecer la acción del deslizamiento a la izquierda o derecha para ocultar un mensaje.

<br />

<a name="faq135"></a>
**(135) ¿Por qué se muestran mensajes en papelera y borradores en conversaciones?**

Los mensajes individuales rara vez serán borrados y, en su mayoría, esto sucede por accidente. Mostrar mensajes en papelera en conversaciones hace más fácil encontrarlos.

Puede eliminar permanentemente un mensaje usando el menú de tres puntos *borrar*, el cual eliminará el mensaje de la conversación. Tenga en cuenta que esto es irreversible.

Del mismo modo, los borradores se muestran en conversaciones para encontrarlos de nuevo en el contexto en el que pertenecen. Es fácil leer los mensajes recibidos antes de seguir escribiendo el borrador más tarde.

<br />

<a name="faq136"></a>
**(136) ¿Cómo puedo eliminar una cuenta/identidad/carpeta?**

Eliminar una cuenta/identidad/carpeta está un poco oculto para prevenir accidentes.

* Cuenta: Configurar > Paso 1 > Administrar > Tocar cuenta
* Identidad: Configurar > Paso 2 > Administrar > Tocar cuenta
* Carpeta: mantenga pulsada la carpeta en la lista de carpetas > Editar propiedades

En el menú de tres puntos en la parte superior derecha hay un elemento para eliminar la cuenta/identidad/carpeta.

<br />

<a name="faq137"></a>
**(137) ¿Cómo puedo restablecer 'No preguntar de nuevo'?**

Puede restablecer todas las preguntas configuradas para que no se vuelvan a hacer en los ajustes misceláneos.

<br />

<a name="faq138"></a>
**(138) ¿Puedes añadir administración/sincronización de contactos/calendario?**

La administración de contactos y calendario pueden hacerse mejor por medio de una aplicación separada y especializada. Tenga en cuenta que FairEmail es una aplicación de correo electrónico especializada, no una suite de oficina.

Además, prefiero hacer algunas cosas muy bien, en lugar de muchas cosas sólo a la mitad. Además, desde una perspectiva de seguridad, no es una buena idea conceder muchos permisos a una sola aplicación.

Se le recomienda utilizar la excelente aplicación de código abierto [DAVx5](https://f-droid.org/packages/at.bitfire.davdroid/) para sincronizar/administrar sus calendarios/contactos.

La mayoría de los proveedores admiten exportar sus contactos. Por favor, [vea aquí](https://support.google.com/contacts/answer/1069522) sobre cómo puede importar contactos si la sincronización no es posible.

Tenga en cuenta que FairEmail admite responder a invitaciones de calendario (una característica pro) y añadir invitaciones de calendario a su calendario personal.

<br />

<a name="faq83"></a>
<a name="faq139"></a>
**(139) ¿Cómo arreglar 'El usuario está autenticado pero no está conectado'?**

De hecho, este error específico de Microsoft Exchange es un mensaje de error incorrecto causado por un error en el software de servidor de Exchange antiguo.

El error *El usuario está autenticado pero no conectado* puede ocurrir si:

* Los mensajes push están habilitados para demasiadas carpetas: vea [estas Preguntas Frecuentes](#user-content-faq23) para más información y una solución
* La contraseña de la cuenta fue cambiada: cambiarla en FairEmail también debería solucionar el problema
* Una dirección de correo electrónico de alias se está utilizando como nombre de usuario en lugar de la dirección de correo principal
* Se está utilizando un esquema de inicio de sesión incorrecto para un buzón compartido: el esquema correcto es *usuario@dominio\AliasBuzónCompartido*

El alias del buzón compartido será generalmente la dirección de correo electrónico de la cuenta compartida, así:

```
usted@ejemplo.com\compartido@ejemplo.com
```

Tenga en cuenta que debería ser una barra invertida y no una barra hacia delante.

<br />

<a name="faq140"></a>
**(140) ¿Por qué el texto del mensaje contiene caracteres extraños?**

La visualización de caracteres extraños casi siempre es causada por la no especificación de o una codificación de caracteres no válida por el software de envío. FairEmail asumirá [ISO 8859-1](https://en.wikipedia.org/wiki/ISO/IEC_8859-1) cuando no haya un conjunto de caracteres o cuando [US-ASCII](https://en.wikipedia.org/wiki/ASCII) fue especificado. Aparte de eso, no hay manera de determinar de forma confiable la codificación de caracteres correcta automáticamente, por lo que esto no puede ser arreglado por FairEmail. La acción correcta consiste en quejarse al remitente.

<br />

<a name="faq141"></a>
**(141) ¿Cómo puedo arreglar 'Se requiere una carpeta de borradores para enviar mensajes'?**

Para almacenar los borradores de mensajes se requiere una carpeta de borradores. En la mayoría de los casos, FairEmail seleccionará automáticamente las carpetas de borradores al añadir una cuenta basado en [los atributos](https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml) que envía el servidor de correo. Sin embargo, algunos servidores de correo electrónico no están configurados correctamente y no envían estos atributos. En este caso FairEmail intenta identificar la carpeta de borradores por nombre, pero esto puede fallar si la carpeta de borradores tiene un nombre inusual o no está presente en absoluto.

Puede solucionar este problema seleccionando manualmente la carpeta de borradores en la configuración de la cuenta (Configuración, paso 1, tocar cuenta, en la parte inferior). Si no hay ninguna carpeta de borradores puede crear una carpeta de borradores tocando en el botón '+' de la lista de carpetas de la cuenta (pulse en el nombre de la cuenta en el menú de navegación).

Algunos proveedores, como Gmail, permiten activar/desactivar IMAP para carpetas individuales. Por lo tanto, si una carpeta no es visible, puede que necesite activar IMAP para la carpeta.

Enlace rápido para Gmail: [https://mail.google.com/mail/u/0/#settings/labels](https://mail.google.com/mail/u/0/#settings/labels)

<br />

<a name="faq142"></a>
**(142) ¿Cómo puedo almacenar mensajes enviados en la bandeja de entrada?**

Generalmente, no es una buena idea almacenar mensajes enviados en la bandeja de entrada porque esto es difícil de deshacer y podría ser incompatible con otros clientes de correo electrónico.

Dicho esto, FairEmail es capaz de manejar correctamente los mensajes enviados en la bandeja de entrada. FairEmail marcará los mensajes salientes con un icono de mensajes enviados, por ejemplo.

La mejor solución sería permitir mostrar la carpeta enviados en la bandeja de entrada unificada manteniendo pulsado la carpeta enviados en la lista de carpetas y habilitando *Mostrar en la bandeja de entrada unificada*. De esta manera, todos los mensajes pueden permanecer donde pertenecen, permitiendo al mismo tiempo ver mensajes entrantes y salientes en un solo lugar.

Si esta no es una opción, puede [crear una regla](#user-content-faq71) para mover automáticamente los mensajes enviados a la bandeja de entrada o establecer una dirección CC/CCO predeterminada en la configuración de identidad avanzada para enviarse una copia.

<br />

<a name="faq143"></a>
**~~(143) ¿Puedes añadir una carpeta de papelera para cuentas POP3?~~**

[POP3](https://en.wikipedia.org/wiki/Post_Office_Protocol) es un protocolo muy limitado. Básicamente, sólo los mensajes pueden ser descargados y borrados de la bandeja de entrada. Ni siquiera es posible marcar un mensaje leído.

Dado que POP3 no permite el acceso a la carpeta de papelera, no hay forma de restaurar los mensajes de papelera.

Tenga en cuenta que puede ocultar mensajes y buscar mensajes ocultos, que son similares a una carpeta local de la papelera, sin sugerir que los mensajes basura pueden ser restaurados, mientras que esto no es posible.

La versión 1.1082 añadió una carpeta de papelera local. Tenga en cuenta que eliminar un mensaje lo eliminará permanentemente del servidor y que los mensajes borrados no podrán ser restaurados al servidor.

<br />

<a name="faq144"></a>
**(144) ¿Cómo puedo grabar notas de voz?**

Para grabar notas de voz puede pulsar este icono en la barra de acción inferior del redactor de mensajes:

![Imagen externa](https://github.com/M66B/FairEmail/blob/master/images/baseline_record_voice_over_black_48dp.png)

Esto requiere que una aplicación de grabación de audio compatible sea instalada. En particular [este intento común](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION) necesita ser soportado.

Por ejemplo [esta grabadora de audio](https://f-droid.org/app/com.github.axet.audiorecorder) es compatible.

Las notas de voz se adjuntarán automáticamente.

<br />

<a name="faq145"></a>
**(145) ¿Cómo puedo establecer un sonido de notificación para una cuenta, carpeta o remitente?**

Cuenta:

* Habilitar *notificaciones separadas* en la configuración avanzada de la cuenta (configuración, paso 1, administrar, tocar cuenta, tocar avanzado)
* Mantenga presionada la cuenta en la lista de cuentas (Configuración, paso 1, Administrar) y seleccione *Editar canal de notificación* para cambiar el sonido de notificación

Carpeta:

* Mantenga pulsado la carpeta en la lista de carpetas y seleccione *Crear canal de notificación*
* Mantenga presionado la carpeta en la lista de carpetas y seleccione *Editar canal de notificación* para cambiar el sonido de notificación

Remitente:

* Abra un mensaje del remitente y expándalo
* Expanda la sección de direcciones tocando en la flecha hacia abajo
* Toca el icono de la campana para crear o editar un canal de notificación y cambiar el sonido de notificación

El orden de prioridad es: sonido del remitente, sonido de la carpeta, sonido de la cuenta y sonido por defecto.

Configurar un sonido de notificación para una cuenta, carpeta o remitente requiere Android 8 Oreo o posterior y es una característica pro.

<br />

<a name="faq146"></a>
**(146) ¿Cómo puedo arreglar tiempos de mensajes incorrectos?**

Dado que la fecha/hora de envío es opcional y puede ser manipulada por el remitente, FairEmail utiliza la fecha y hora recibidas del servidor por defecto.

A veces la fecha/hora de recepción del servidor es incorrecta, principalmente porque los mensajes fueron importados incorrectamente desde otro servidor y a veces debido a un error en el servidor de correo.

En estos raros casos, es posible dejar que FairEmail use la fecha/hora del encabezado *Fecha* (hora enviada) o desde el encabezado *Recibido* como una solución. Esto se puede cambiar en la configuración avanzada de la cuenta: Configuración, paso 1, Administrar, tocar cuenta, tocar avanzado.

Esto no cambiará el tiempo de los mensajes ya sincronizados. Para resolver esto, mantenga pulsado la(s) carpeta(s) en la lista de carpetas y selecciona *Eliminar mensajes locales* y *Sincronizar ahora*.

<br />

<a name="faq147"></a>
**(147) ¿Qué debo saber sobre las versiones de terceros?**

Probablemente llegó aquí porque está usando una versión de terceros de FairEmail.

**Sólo hay soporte** para la última versión de Play Store, la última versión de GitHub y la compilación de F-Droid, pero **sólo si** el número de versión de la compilación de F-Droid es el mismo que el número de versión de la última versión de GitHub.

F-Droid compila de forma irregular, lo que puede ser problemático cuando hay una actualización importante. Por lo tanto se le aconseja cambiar a la versión de GitHub.

La versión de F-Droid se compila con el mismo código fuente, pero se firma de forma diferente. Esto significa que todas las características están disponibles también en la versión de F-Droid excepto para usar el asistente de configuración rápida de Gmail porque Google aprueba (y permite) una sola firma de la aplicación. Para todos los demás proveedores de correo electrónico, el acceso OAuth sólo está disponible en las versiones de Play Store y en las versiones de Github, dado que los proveedores de correo electrónico sólo han permitido que las compilaciones oficiales usen OAuth.

Tenga en cuenta que primero tendrá que desinstalar la compilación de F-Droid antes de instalar una versión de GitHub porque Android se niega a instalar la misma aplicación con una firma diferente por razones de seguridad.

Tenga en cuenta que la versión de GitHub comprobará automáticamente las actualizaciones. Si lo desea, esto puede desactivarse en los ajustes misceláneos.

Por favor, [vea aquí](https://github.com/M66B/FairEmail/blob/master/README.md#user-content-downloads) para todas las opciones de descarga.

Si tiene un problema con la compilación de F-Droid, compruebe primero si hay una versión más reciente de GitHub.

<br />

<a name="faq148"></a>
**(148) ¿Cómo puedo usar una cuenta iCloud de Apple?**

Hay un perfil integrado para Apple iCloud, pero si es necesario puede encontrar la configuración correcta [aquí](https://support.apple.com/en-us/HT202304).

Al utilizar la autenticación de dos factores puede que necesite utilizar una [contraseña específica de la aplicación](https://support.apple.com/en-us/HT204397).

<br />

<a name="faq149"></a>
**(149) ¿Cómo funciona el widget de conteo de mensajes no leídos?**

El widget de conteo de mensajes no leídos muestra el número de mensajes no leídos para todas las cuentas o para una cuenta seleccionada, pero sólo para las carpetas para las que están habilitadas las notificaciones de mensajes nuevos.

Pulsar en la notificación sincronizará todas las carpetas para las que la sincronización está activada y abrirá:

* la pantalla de inicio cuando todas las cuentas fueron seleccionadas
* una lista de carpetas cuando una cuenta específica fue seleccionada y cuando las notificaciones de mensajes nuevos están habilitadas para múltiples carpetas
* una lista de mensajes cuando una cuenta específica fue seleccionada y cuando las notificaciones de mensajes nuevos están habilitadas para una carpeta

<br />

<a name="faq150"></a>
**(150) ¿Puedes añadir la cancelación de las invitaciones al calendario?**

La cancelación de invitaciones al calendario (eliminar eventos del calendario) requiere el permiso de escritura del calendario, que dará como resultado la concesión efectiva de permiso para leer y escribir *todos* los eventos del calendario de *todos* calendarios.

Dada la meta de FairEmail, privacidad y seguridad, y dado que es fácil eliminar un evento de calendario manualmente, no es una buena idea solicitar este permiso solo por esta razón.

La inserción de nuevos eventos del calendario se puede hacer sin permisos con [intentos](https://developer.android.com/guide/topics/providers/calendar-provider.html#intents) especiales. Desafortunadamente, no existe ningún intento para eliminar los eventos del calendario existentes.

<br />

<a name="faq151"></a>
**(151) ¿Puedes añadir copia de seguridad/restauración de mensajes?**

Un cliente de correo electrónico está destinado a leer y escribir mensajes, no a respaldar y restaurar mensajes. ¡Tenga en cuenta que romper o perder tu dispositivo significa perder sus mensajes!

En su lugar, el proveedor/servidor de correo electrónico es responsable de las copias de seguridad.

Si quiere hacer una copia de seguridad por usted mismo, puede usar una herramienta como [imapsync](https://imapsync.lamiral.info/).

Si desea importar un archivo mbox a una cuenta de correo electrónico existente, puede usar Thunderbird en un ordenador de escritorio y el complemento [ImportExportTools](https://addons.thunderbird.net/nl/thunderbird/addon/importexporttools/).

<br />

<a name="faq152"></a>
**(152) ¿Cómo puedo insertar un grupo de contactos?**

Puede insertar las direcciones de correo electrónico de todos los contactos en un grupo de contactos a través del menú de tres puntos del redactor de mensajes.

Puede definir grupos de contactos con la aplicación de contactos de Android, consulte [aquí](https://support.google.com/contacts/answer/30970) para obtener instrucciones.

<br />

<a name="faq153"></a>
**(153) ¿Por qué la eliminación permanente de mensajes de Gmail no funciona?**

Puede que necesite cambiar [la configuración IMAP de Gmail](https://mail.google.com/mail/u/0/#settings/fwdandpop) en un navegador de escritorio para que funcione:

* Cuando marco un mensaje en IMAP como eliminado: Auto-Expunge off - Espere a que el cliente actualice el servidor.
* Cuando un mensaje está marcado como borrado y eliminado de la última carpeta IMAP visible: Borrar el mensaje inmediatamente para siempre

Tenga en cuenta que los mensajes archivados sólo se pueden eliminar moviéndolos a la carpeta de la papelera primero.

Algo de contexto: Gmail parece tener una vista adicional de mensajes para IMAP, que puede ser diferente de la vista principal de mensajes.

<br />

<a name="faq154"></a>
**~~(154) ¿Puedes añadir favicons como fotos de contacto?~~**

~~Además de que un [favicon](https://en.wikipedia.org/wiki/Favicon) podría ser compartido por muchas direcciones de correo electrónico con el mismo nombre de dominio~~ ~~y, por lo tanto, no está directamente relacionado con una dirección de correo electrónico, los iconos favoritos pueden ser usados para rastrearle.~~

<br />

<a name="faq155"></a>
**(155) ¿Qué es un archivo winmail.dat?**

Un archivo *winmail.dat* es enviado por un cliente Outlook configurado incorrectamente. Es un formato de archivo específico de Microsoft ([TNEF](https://en.wikipedia.org/wiki/Transport_Neutral_Encapsulation_Format)) que contiene un mensaje y, posiblemente, archivos adjuntos.

Puede encontrar más información sobre este archivo [aquí](https://support.mozilla.org/en-US/kb/what-winmaildat-attachment).

Puede verlo por ejemplo con la aplicación Android [Letter Opener](https://play.google.com/store/apps/details?id=app.letteropener).

<br />

<a name="faq156"></a>
**(156) ¿Cómo puedo configurar una cuenta de Office 365?**

Se puede configurar una cuenta de Office 365 mediante el asistente de configuración rápida y seleccionando *Office 365 (OAuth)*.

Si el asistente termina con *AUTHENTICATE falló*, IMAP y/o SMTP podrían estar deshabilitados para la cuenta. En este caso debería pedir al administrador que active IMAP y SMTP. El procedimiento está documentado [aquí](https://docs.microsoft.com/en-in/exchange/troubleshoot/configure-mailboxes/pop3-imap-owa-activesync-office-365).

Si ha activado *valores predeterminados de seguridad* en su organización, puede que necesite habilitar el protocolo SMTP AUTH. Por favor, [vea aquí](https://docs.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/authenticated-client-smtp-submission) sobre cómo hacerlo.

<br />

<a name="faq157"></a>
**(157) ¿Cómo puedo configurar una cuenta Free.fr?**

Veuillez [voir ici](https://free.fr/assistance/597.html) pour les instructions.

**SMTP est désactivé par défaut**, veuillez [voir ici](https://free.fr/assistance/2406.html) comment il peut être activé.

Veuillez [voir ici](http://jc.etiemble.free.fr/abc/index.php/trucs-astuces/configurer-smtp-free-fr) pour un guide détaillé.

<br />

<a name="faq158"></a>
**(158) ¿Qué cámara / grabadora de audio recomiendas?**

Para tomar fotos y grabar audio se necesita una cámara y una aplicación de grabación de audio. Las siguientes aplicaciones son cámaras y grabadoras de audio de código abierto:

* [Open Camera](https://play.google.com/store/apps/details?id=net.sourceforge.opencamera) ([F-Droid](https://f-droid.org/en/packages/net.sourceforge.opencamera/))
* [Audio Recorder](https://play.google.com/store/apps/details?id=com.github.axet.audiorecorder) ([F-Droid](https://f-droid.org/packages/com.github.axet.audiorecorder/))

Para grabar notas de voz, etc, la grabadora de audio necesita soportar [MediaStore.Audio.Media.RECORD_SOUND_ACTION](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media#RECORD_SOUND_ACTION). Curiosamente, la mayoría de las grabadoras de audio parecen no soportar esta acción estándar de Android.

<br />

<a name="faq159"></a>
**(159) ¿Qué son las listas de protección del rastreador de Disconnect?**

Consulte [aquí](https://disconnect.me/trackerprotection) para obtener más información sobre las listas de protección del rastreador de Disconnect.

Después de descargar las listas en la configuración de privacidad, las listas pueden ser usadas opcionalmente:

* para avisar sobre el enlaces de seguimiento al abrir enlaces
* para reconocer imágenes de seguimiento en los mensajes

Las imágenes de seguimiento sólo se desactivarán si la opción principal correspondiente 'desactivar' está habilitada.

Las imágenes de seguimiento no serán reconocidas cuando el dominio se clasifique como '*Contenido*', vea [aquí](https://disconnect.me/trackerprotection#trackers-we-dont-block) para más información.

Este comando puede ser enviado a FairEmail desde una aplicación de automatización para actualizar las listas de protección:

```
(adb shell) am startservice -a eu.faircode.email.DISCONNECT.ME
```

Probablemente sea suficiente actualizar una vez a la semana, consulte [aquí](https://github.com/disconnectme/disconnect-tracking-protection/commits/master) para ver los cambios recientes en las listas.

<br />

<a name="faq160"></a>
**(160) ¿Puedes añadir eliminación permanente de mensajes sin confirmación?**

La eliminación permanente significa que los mensajes se perderán *irreversiblemente*, y para evitar que esto ocurra accidentalmente, esto siempre necesita ser confirmado. Incluso con una confirmación, algunas personas muy enojadas que perdieron algunos de sus mensajes por culpa propia se pusieron en contacto conmigo, lo que fue una experiencia bastante desagradable :-(

<br />

<a name="faq161"></a>
**(161) ¿Puedes añadir un ajuste para cambiar el color principal y de acento?***

Si pudiera, añadiría un ajuste para seleccionar el color principal y de acento de inmediato, pero desafortunadamente los temas de Android están fijados, vea por ejemplo [aquí](https://stackoverflow.com/a/26511725/1794097), por lo que esto no es posible.

<br />


## Soporte

Sólo la última versión de Play Store y la última versión de GitHub son soportadas. Esto también significa que bajar de versión no está soportado.

Las características solicitadas deberían:

* ser útiles para la mayoría de la gente
* no complicar el uso de FairEmail
* encajar dentro de la filosofía de FairEmail (orientado a la privacidad, pensando en seguridad)
* cumplir con estándares comunes (IMAP, SMTP, etc.)

Es probable que las características que no cumplan estos requisitos sean rechazadas. Esto también es para permitir el mantenimiento y el soporte a largo plazo.

Si tiene una pregunta, quiere solicitar una característica o reportar un error, por favor use [este formulario](https://contact.faircode.eu/?product=fairemailsupport).

Los incidentes de GitHub están desactivados debido a un uso frecuente indebido.

Copyright &copy; 2018-2020 Marcel Bokhorst.
