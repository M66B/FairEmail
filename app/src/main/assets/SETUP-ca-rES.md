# Posada en marxa i ajuda

Configurar FairEmail és força senzill. Haureu d’afegir almenys un compte per rebre correu electrònic i almenys una identitat si voleu enviar-lo. The quick setup will add an account and an identity in one go for most major providers.

## Requeriments

És necessària una connexió a Internet per configurar identitats i comptes.

## Configuració ràpida

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Configura compte - per rebre correu electrònic

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Seleccioneu un proveïdor de la llista, introduïu el nom d’usuari, que és principalment la vostra adreça de correu electrònic i introduïu la vostra contrasenya. Premeu *Comproveu* per deixar que FairEmail es connecti al servidor de correu electrònic i obtingui una llista de carpetes del sistema. Després de revisar la selecció de la carpeta del sistema, podeu afegir el compte si premeu *Desa*.

If your provider is not in the list of providers, there are thousands of providers, select *Custom*. Introduïu el nom de domini, per exemple *gmail.com* i toqueu *Obteniu la configuració*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Per obtenir més informació sobre això, consulteu [aquí](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configura una identitat - per enviar correu electrònic

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Introduïu el nom que vulgueu aparèixer a la direcció dels correus electrònics que envieu i seleccioneu un compte enllaçat. Premeu *Desar* per afegir una identitat.

Si el compte s'ha configurat manualment, és probable que també configureu la identitat manualment. Introduïu el nom de domini, per exemple *gmail.com* i toqueu *Obteniu la configuració*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Consulteu [aquestes Preguntes Freqüents](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sobre com utilitzar àlies.

## Concedir permisos: per accedir a la informació de contacte

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Configuració d'optimitzacions de bateria: per rebre correus electrònics de forma continuada

En les versions recents d'Android, Android posarà les aplicacions en suspensió quan la pantalla estigui apagada durant un temps per reduir l’ús de la bateria. Si voleu rebre nous correus electrònics sense retards, haureu de desactivar les optimitzacions de bateries de FairEmail. Tap *Manage* and follow the instructions.

## Preguntes o problemes

Si tens alguna pregunta o problema, si us plau [llegeix això](https://github.com/M66B/FairEmail/blob/master/FAQ.md) per ajudar-te.