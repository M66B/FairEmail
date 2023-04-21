# Posada en marxa i ajuda

Configurar FairEmail és força senzill. Haureu d’afegir almenys un compte per rebre correu electrònic i almenys una identitat si voleu enviar-lo. La configuració ràpida afegirà un compte i una identitat simultània per a la majoria de proveïdors principals.

## Requeriments

És necessària una connexió a Internet per configurar identitats i comptes.

## Configuració ràpida

Tan sols seleccioneu el proveïdor apropiat o *Altre proveïdor* i escriviu el vostre nom, adreça de correu i contrasenya i premeu *Fet*.

Funcionarà per a la majoria de proveïdors de correu electrònic.

Si la configuració ràpida no funciona, haureu de configurar un compte i una identitat de forma manual, vegeu a continuació les instruccions.

## Configura compte - per rebre correu electrònic

Per afegir un compte, toca *Configuració manual i més opcions*, toca*Comptes* i toca el botó "més" a la part inferior i selecciona IMAP (o POP3). Seleccioneu un proveïdor de la llista, introduïu el nom d’usuari, que és principalment la vostra adreça de correu electrònic i introduïu la vostra contrasenya. Premeu *Comproveu* per deixar que FairEmail es connecti al servidor de correu electrònic i obtingui una llista de carpetes del sistema. Després de revisar la selecció de la carpeta del sistema, podeu afegir el compte si premeu *Desa*.

Si el teu proveïdor no està a la llista de proveïdors, hi ha milers de proveïdors, selecciona *Personalitzat*. Introduïu el nom de domini, per exemple *gmail.com* i toqueu *Obteniu la configuració*. Si el teu proveïdor suporta [detecció automàtica](https://tools.ietf.org/html/rfc6186), FairEmail omplirà el nom de l'amfitrió i el número de port, si no, comprova les instruccions de configuració del teu proveïdor per obtenir el nom de l'amfitrió IMAP adequat, el número de port i el protocol d'encriptació (SSL/TLS o STARTTLS). Per obtenir més informació sobre això, consulteu [aquí](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configura una identitat - per enviar correu electrònic

De manera similar, per afegir una identitat, prem *Configuració manual i més opcions*, prem *Identitats* i prem el botó de "mes" a la part inferior. Introduïu el nom que vulgueu aparèixer a la direcció dels correus electrònics que envieu i seleccioneu un compte enllaçat. Premeu *Desar* per afegir una identitat.

Si el compte s'ha configurat manualment, és probable que també configureu la identitat manualment. Introduïu el nom de domini, per exemple *gmail.com* i toqueu *Obteniu la configuració*. Si el teu proveïdor suporta [detecció automàtica](https://tools.ietf.org/html/rfc6186), FairEmail omplirà el nom de l'amfitrió i el número de port, si no, comprova les instruccions de configuració del teu proveïdor per obtenir el nom de l'amfitrió IMAP adequat, el número de port i el protocol d'encriptació (SSL/TLS o STARTTLS).

Consulteu [aquestes Preguntes Freqüents](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sobre com utilitzar àlies.

## Concedir permisos: per accedir a la informació de contacte

Si voleu cercar adreces de correu electrònic, mostrar fotos de contacte, etc., haureu de concedir el permís de contactes de lectura a FairEmail. Només heu de tocar *Concedir permisos* i seleccionar *Permetre*.

## Configuració d'optimitzacions de bateria: per rebre correus electrònics de forma continuada

En les versions recents d'Android, Android posarà les aplicacions en suspensió quan la pantalla estigui apagada durant un temps per reduir l’ús de la bateria. Si voleu rebre nous correus electrònics sense retards, haureu de desactivar les optimitzacions de bateries de FairEmail. Prem *Gestiona* i segueix les instruccions.

## Preguntes o problemes

Si tens alguna pregunta o problema, si us plau [llegeix això](https://github.com/M66B/FairEmail/blob/master/FAQ.md) per ajudar-te.