<a name="top"></a>

# Besoin d'aide pour fairEmail

Avez-vous checker les FAQ au cas où . [En bas](#user-content-get-support), Si les FAQ n'ont pas répondues a vos interrogation, posez nous vos questions ici.

Allez voir les FAQ, la reponse que vous chercher s'y trouve peut-etre . [ Tout en bas ](#user-content-get-support), vous trouverez comment poser d'autres questions, demander des fonctionnalités et signaler des erreurs.

## Index

* [Autorisation des comptes](#user-content-authorizing-accounts)
* [Comment... ?](#user-content-howto)
* [Problèmes connus](#user-content-known-problems)
* [Fonction a venir, nous y travaillons](#user-content-planned-features)
* [Les fonctionnalités les plus demandés](#user-content-frequently-requested-features)
* [Foire aux questions](#user-content-frequently-asked-questions)
* [Aidez-moi je suis mal pris](#user-content-get-support)

<h2><a name="authorizing-accounts"></a>Autorisation des comptes</h2>

Fiez vous a l'assistant de configuration, il est sur la coche.

Si l'assistant est pas capable vous devrez l'aider. Pour ce faire, vous aurez besoin des adresses du serveur IMAP et SMTP, des numéros de port, de savoir si SSL/TLS ou STARTTLS doivent être utilisés, de votre nom d'utilisateur (habituellement votre adresse e-mail) et de votre mot de passe.

Faire une recherche avec *IMAP* et le nom du fournisseur est généralement suffisant pour trouver la documentation appropriée.

Dans certains cas, vous aurez besoin d'activer l'accès externe à votre compte et/ou d'utiliser un mot de passe spécial (mot de passe d'application), par exemple lorsque l'authentification à deux facteurs est activée.

Pour l'autorisation :

* Gmail / G suite, voir [question 6](#user-content-faq6)
* Outlook / Live / Hotmail, voir [question 14](#user-content-faq14)
* Office 365, voir [question 14](#user-content-faq156)
* Voir les reponse au sujet fe Microsoft exchange
* Pour yahoo, AOL and sky que personnes n'utilise cliqué ici
* Pour Icloud clique la
* Free.fr , qui utilise ca, si c'est toi clique la

Check les troubleshooting.

En rapport avec:

* [OAuth what is that](#user-content-faq111)
* [Veux tu ben me dire pourquoi ActiveSync n'est pas supporter ](#user-content-faq133)

<a name="howto">

## Comment... ?

* Modifier le nom du compte : Paramètres, appuyez sur Configuration manuelle et plus d'options, appuyez sur Comptes, appuyez sur le compte
* Modifiez la cible du balayage gauche/droite : Paramètres, Comportement de l'onglet, Définir les actions de balayage
* Changer le mot de passe : Paramètres, appuyez sur Configuration manuelle, appuyez sur Comptes, appuyez sur le compte, changez le mot de passe
* Définir une signature : Paramètres, appuyez sur Configuration manuelle, appuyez sur Identités, appuyez sur l'identité, modifiez la signature.
* Ajouter des adresses CC et CCI : appuyez sur l'icône des personnes à la fin de l'objet
* Allez au message suivant/précédent aaprès l'archivage/suppression : dans les paramètres de comportement, désactivez *Fermez automatiquement les conversations* et sélectionnez *Allez à la conversation suivante/précédente* pour *Lors de la fermeture d'une conversation*
* Ajouter un dossier à la boîte de réception unifiée : appuyez longuement sur le dossier dans la liste des dossiers et cochez *Afficher dans la boîte de réception unifiée*
* Ajouter un dossier au menu de navigation : appuyez longuement sur le dossier dans la liste des dossiers et cochez *Afficher dans le menu de navigation*
* Charger plus de messages : appuyez longuement sur un dossier dans la liste de dossiers, sélectionnez *Récupérer plus de messages*
* Supprimer un message, en ignorant la corbeille : appuyez longuement sur l'icône de la corbeille
* Supprimer un compte/une identité : Paramètres, appuyez sur Configuration manuelle et plus d'options, appuyez sur Comptes/Identités, appuyez sur le compte/l'identité, appuyez sur l'icône de la corbeille en haut à droite
* Supprimer un dossier : appuyez longuement sur le dossier dans la liste de dossiers, appuyez sur Modifier les propriétés, appuyez sur l'icône de la corbeille en haut à droite
* Annuler l'envoi : Boîte d'envoi, faites glisser le message dans la liste à gauche ou à droite
* Stocker les messages envoyés dans la boîte de réception : veuillez [consulter cette FAQ](#user-content-faq142)
* Changer les dossiers système : Paramètres, appuyez sur Configuration manuelle et plus d'options, appuyez sur Comptes, appuyez sur le compte, les options se trouvent en bas
* Exporter/importer les paramètres : Paramètres, panneau de navigation (icône à trois traits)

<h2><a name="known-problems"></a>Problème connu</h2>

* ~~Un [bogue dans Android 5.1 et 6](https://issuetracker.google.com/issues/37054851) fait que les applications affichent parfois un mauvais format d'heure. Changer les paramètres de Android sur *Utiliser le format 24 heures* pourrait résoudre temporairement le problème. Une solution de contournement a été ajoutée.~~
* ~~Un [bogue dans Google Drive](https://issuetracker.google.com/issues/126362828) vide le dossier des fichiers exportés vers Google Drive. Google a corrigé ceci.~~
* ~~Un [bogue dans AndroidX](https://issuetracker.google.com/issues/78495471) provoque un plantage occasionnel de FairEmail en appui long ou en glissant. Google a corrigé ceci.~~
* ~~Une [erreur dans AndroidX ROOM](https://issuetracker.google.com/issues/138441698) provoque parfois un plantage avec "*... Exception lors du calcul de la base de données en direct... Impossible de lire la ligne ...*". Une solution de contournement a été ajoutée.~~
* Un [bug dans Android](https://issuetracker.google.com/issues/119872129) provoque parfois un plantage de FairEmail avec "*... Mauvaise notification postée ...*" sur certains appareils une fois après la mise à jour de FairEmail et en appuyant sur une notification.
* Un [bug dans Android](https://issuetracker.google.com/issues/62427912) provoque parfois un plantage avec "*... ActivityRecord introuvable pour ...*" après la mise à jour de FairEmail. La réinstallation([source](https://stackoverflow.com/questions/46309428/android-activitythread-reportsizeconfigurations-causes-app-to-freeze-with-black)) pourrait résoudre le problème.
* Un [bug dans Android](https://issuetracker.google.com/issues/37018931) provoque parfois un plantage avec *... InputChannel n'est pas initialisé ...* sur certains appareils.
* ~~Un [bogue dans LineageOS](https://review.lineageos.org/c/LineageOS/android_frameworks_base/+/265273) provoque parfois un plantage avec *... java.lang.ArrayIndexOutOfBoundsException: length=...; index=... ...*.~~
* Un bug dans Nova Launcher sur Android 5.x provoque le plantage de FairEmail avec une exception *java.lang.StackOverflowError* lorsque Nova Launcher a accès au service d'accessibilité.
* ~~Le sélecteur de dossier ne montre parfois aucun dossier pour des raisons encore inconnues. Cela semble être réparé.~~
* ~~Une [erreur dans AndroidX](https://issuetracker.google.com/issues/64729576) rend difficile le défilement rapide. Une solution de contournement a été ajoutée.~~
* ~~Le chiffrement avec YubiKey se traduit par une boucle infinie. Cela semble être causé par un [bug dans OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2507).~~
* Le défilement vers un emplacement lié en interne dans les messages originaux ne fonctionne pas. Ceci ne peut pas être corrigé car la vue du message d'origine est contenue dans une vue déroulante.
* Un aperçu d'un message texte n'apparaît pas (toujours) sur les montres Samsung car [setLocalOnly](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder.html#setLocalOnly(boolean)) semble être ignoré. Les textes de prévisualisation des messages sont censés être affichés correctement sur les objets connectés Pebble 2, Fitbit Charge 3, Mi band 3 et Xiaomi Amazfit BIP. Voir aussi [cette FAQ](#user-content-faq126).
* Une [erreur sur Android 6.0](https://issuetracker.google.com/issues/37068143) provoque un plantage *... Invalid offset: ... Valid range is ...* lorsque du texte est sélectionné et en tapotant à côté de ce dernier. Ce bogue a été corrigé dans Android 6.0.1.
* Les liens internes (ancrés) ne fonctionneront pas parce que les messages originaux sont affichés dans une WebView intégrée dans une vue défilante (la liste des conversations). Il s'agit d'une limitation Android qui ne peut être ni corrigée ni contournée.
* La détection de la langue [ne fonctionne plus](https://issuetracker.google.com/issues/173337263) sur les appareils Pixel avec (mise à jour vers ?) Android 11
* Un bogue [dans OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2688) provoque des signatures PGP invalides lors de l'utilisation d'un jeton matériel.

<h2><a name="planned-features"></a>Fonctionnalités prévues</h2>

* ~~Synchronisation à la demande (manuel)~~
* ~~Chiffrement semi-automatique~~
* ~~Copie de message~~
* ~~Couleurs de suivi~~
* ~~Paramètres de notification par dossier~~
* ~~Sélection d'images locales pour les signatures~~ (cela ne sera pas ajouté car cela nécessite une gestion des fichiers image et parce que les images ne sont pas affichées par défaut dans la plupart des clients de messagerie de toute façon)
* ~~Affichage des messages correspondant à une règle~~
* ~~[ManageSieve](https://tools.ietf.org/html/rfc5804)~~ (il n'y a pas de librairie Java maintenue avec une licence adaptée et sans dépendance et parallèlement à ça, FairEmail a ses propres règles de filtrage)
* ~~Recherche de messages avec/sans pièces jointes~~ (ceci ne peut pas être ajouté car IMAP ne supporte pas la recherche de pièces jointes)
* ~~Recherche de dossier~~ (filtrer une liste de dossiers hiérarchique est problématique)
* ~~Suggestions de recherche~~
* ~~[Autocrypt Setup Message](https://autocrypt.org/autocrypt-spec-1.0.0.pdf) (section 4.4)~~ (A mon avis, ce n'est pas une bonne idée de laisser un client de messagerie manipuler des clés de chiffrement sensibles pour un cas d'usage exceptionnel alors que OpenKeychain peut également exporter des clés)
* ~~Dossiers unifiés génériques~~
* ~~Nouveaux calendriers de notification par message de compte~~ (implémentés en ajoutant une condition de temps aux règles pour que les messages puissent être reportés pendant les périodes sélectionnées)
* ~~Copie de comptes et identités~~
* ~~Zoom par pincement~~ (non possible de manière fiable dans une liste de défilement ; la vue complète du message peut être zoomée à la place)
* ~~Vue de dossier plus compacte~~
* ~~Composition de listes et de tables~~ (ceci nécessite un éditeur de texte riche, voir [cette FAQ](#user-content-faq99))
* ~~Taille du texte en zoom par pincement~~
* ~~Affichage des GIFs~~
* ~~Thèmes~~ (des thèmes gris clair et sombre ont été ajoutés parce que c'est ce que la plupart des gens semblaient vouloir)
* ~~Condition de n'importe quel jour~~ (n'importe quel jour ne correspond pas vraiment à la condition de/à date/heure)
* ~~Envoyer comme pièce jointe~~
* ~~Widget pour un compte sélectionné~~
* ~~Rappel de l'ajout des pièces jointes~~
* ~~Sélection des domaines pour lesquels afficher les images~~ (ceci sera trop compliqué à utiliser)
* ~~Vue unifiée des messages suivis~~ (il y a déjà une recherche spéciale pour ceci)
* ~~Déplacer l'action de notification~~
* ~~support S/MIME ~~
* ~~Rechercher des paramètres~~

Tout ce qui se trouve dans cette liste est dans un ordre aléatoire et *pourrait* être ajouté dans un avenir proche.

<h2><a name="frequently-requested-features"></a>Fonctionnalités fréquemment demandées</h2>

La conception est basée sur de nombreuses discussions et si vous le souhaitez, vous pouvez également en discuter [dans ce forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168). Le but de la conception est d'être minimaliste (pas de menus inutiles, boutons, etc.) et non distrayant (pas de couleurs fantaisie, animations, etc.). Toutes les informations affichées devraient être utiles d'une manière ou d'une autre et devraient être soigneusement positionnées pour une utilisation facile. Les polices, tailles, couleurs, etc. devraient être Material Design autant que possible.

<h2><a name="frequently-asked-questions"></a>Foire aux questions</h2>

* [(1) Quelles autorisations Android sont nécessaires et pourquoi ?](#user-content-faq1)
* [(2) Pourquoi y a-t-il une notification permanente ?](#user-content-faq2)
* [(3) Que sont les opérations et pourquoi sont-elles en attente ?](#user-content-faq3)
* [(4) Comment puis-je utiliser un certificat de sécurité non valide / un mot de passe vide / une connexion en texte clair ?](#user-content-faq4)
* [(5) Comment puis-je paramétrer la vue des messages ?](#user-content-faq5)
* [(6) Comment puis-je me connecter à Gmail / G suite ?](#user-content-faq6)
* [(7) Pourquoi les messages envoyés n'apparaissent pas (directement) dans le dossier Envoyés ?](#user-content-faq7)
* [(8) Puis-je utiliser un compte Microsoft Exchange ?](#user-content-faq8)
* [(9) Que sont les identités / comment ajouter un alias ?](#user-content-faq9)
* [~~(11) Pourquoi POP n'est pas pris en charge ?~~](#user-content-faq11)
* [~~(10) Que signifie "UIDPLUS n'est pas pris en charge" ?~~](#user-content-faq10)
* [(12) Comment fonctionne le chiffrement/déchiffrement ?](#user-content-faq12)
* [(13) Comment fonctionne la recherche sur l'appareil/le serveur ?](#user-content-faq13)
* [(14) Comment puis-je configurer un compte Outlook / Live / Hotmail ?](#user-content-faq14)
* [(15) Pourquoi le texte du message continue-t-il de se charger indéfiniment ?](#user-content-faq15)
* [(16) Pourquoi les messages ne sont-ils pas synchronisés ?](#user-content-faq16)
* [~~(17) Pourquoi la synchronisation manuelle ne fonctionne pas ?~~](#user-content-faq17)
* [(18) Pourquoi l'aperçu du message n'est-il pas toujours affiché ?](#user-content-faq18)
* [(19) Pourquoi les fonctionnalités pro sont-elles si chères ?](#user-content-faq19)
* [(20) Puis-je obtenir un remboursement ?](#user-content-faq20)
* [(21) Comment activer le voyant de notification ?](#user-content-faq21)
* [(22) Que signifie l'erreur de compte/dossier ... ?](#user-content-faq22)
* [(23) Pourquoi est-ce que je reçois une alerte ? ?](#user-content-faq23)
* [(24) Qu'est-ce que parcourir les messages sur le serveur ?](#user-content-faq24)
* [(25) Pourquoi ne puis-je pas sélectionner/ouvrir/enregistrer une image, une pièce jointe ou un fichier ?](#user-content-faq25)
* [(26) Puis-je aider à traduire FairEmail dans ma propre langue ?](#user-content-faq26)
* [(27) Comment faire la distinction entre les images intégrées et les images externes ?](#user-content-faq27)
* [(28) Comment puis-je gérer les notifications dans la barre d'état ?](#user-content-faq28)
* [(29) Comment puis-je recevoir des notifications de nouveaux messages pour d'autres dossiers ?](#user-content-faq29)
* [(30) Comment puis-je utiliser les paramètres rapides fournis ?](#user-content-faq30)
* [(31) Comment puis-je utiliser les raccourcis fournis ?](#user-content-faq31)
* [(32) Comment puis-je vérifier si la lecture des courriels est vraiment sécurisée ?](#user-content-faq32)
* [(33) Pourquoi la modification des adresses de l'expéditeur ne fonctionne-t-elle pas ?](#user-content-faq33)
* [(34) Comment les identités correspondent-elles ?](#user-content-faq34)
* [(35) Pourquoi devrais-je faire attention à la visualisation des images, des pièces jointes, du message original et à l'ouverture des liens ?](#user-content-faq35)
* [(36) Comment les fichiers de configuration sont-ils chiffrés ?](#user-content-faq36)
* [(37) Comment les mots de passe sont-ils stockés ?](#user-content-faq37)
* [(39) Comment puis-je réduire l'utilisation de la batterie de FairEmail ?](#user-content-faq39)
* [(40) Comment puis-je réduire l'utilisation des données par FairEmail ?](#user-content-faq40)
* [(41) Comment puis-je corriger l'erreur 'Échec de la prise de main' ?](#user-content-faq41)
* [(42) Pouvez-vous ajouter un nouveau fournisseur à la liste des fournisseurs ?](#user-content-faq42)
* [(43) Pouvez-vous montrer l'original ... ?](#user-content-faq43)
* [(44) Pouvez-vous montrer les photos / identicons de contact dans le dossier envoyé ?](#user-content-faq44)
* [(45) Comment puis-je corriger « Cette clé n'est pas disponible. Pour l'utiliser, vous devez l'importer comme l'un des vôtres ! » ?](#user-content-faq45)
* [(46) Pourquoi la liste des messages continue-t-elle à se rafraîchir ?](#user-content-faq46)
* [(47) Comment puis-je résoudre l'erreur « Aucun compte principal ou aucun dossier brouillon » ?](#user-content-faq47)
* [~~(48) Comment résoudre l'erreur « Aucun compte principal ou aucun dossier d'archives » ?~~](#user-content-faq48)
* [(49) Comment puis-je réparer « Une application obsolète a envoyé un chemin de fichier au lieu d'un flux de fichiers » ?](#user-content-faq49)
* [(50) Pouvez-vous ajouter une option pour synchroniser tous les messages ?](#user-content-faq50)
* [(51) Comment les dossiers sont-ils triés ?](#user-content-faq51)
* [(52) Pourquoi faut-il un peu de temps pour se reconnecter à un compte ?](#user-content-faq52)
* [(53) Pouvez-vous coller la barre d'action de message en haut/bas ?](#user-content-faq53)
* [~~(54) Comment utiliser un préfixe d'espace de noms ?~~](#user-content-faq54)
* [(55) Comment puis-je marquer tous les messages comme lus / déplacer ou supprimer tous les messages ?](#user-content-faq55)
* [(56) Pouvez-vous ajouter un support pour JMAP ?](#user-content-faq56)
* [(57) Puis-je utiliser HTML dans les signatures ?](#user-content-faq57)
* [(58) Que signifie une icône de courriel ouverte/fermée ?](#user-content-faq58)
* [(59) Peut-on ouvrir des messages originaux dans le navigateur ?](#user-content-faq59)
* [(60) Saviez-vous ...?](#user-content-faq60)
* [(61) Pourquoi certains messages sont-ils affichés grisés ?](#user-content-faq61)
* [(62) Quelles méthodes d'authentification sont supportées ?](#user-content-faq62)
* [(63) Comment les images sont-elles redimensionnées pour l'affichage sur les écrans ?](#user-content-faq63)
* [~~(64) Pouvez-vous ajouter des actions personnalisées pour glisser vers la gauche/vers la droite ?~~](#user-content-faq64)
* [(65) Pourquoi certaines pièces jointes sont-elles affichées grisées ?](#user-content-faq65)
* [(66) FairEmail est-il disponible dans la bibliothèque familiale Google Play ?](#user-content-faq66)
* [(67) Comment puis-je répéter les conversations ?](#user-content-faq67)
* [~~(68) Pourquoi le lecteur Adobe Acrobat n'ouvre-t-il pas les pièces jointes PDF / Les applications Microsoft n'ouvrent pas les documents ?~~](#user-content-faq68)
* [(69) Pouvez-vous ajouter le défilement automatique vers le haut sur un nouveau message ?](#user-content-faq69)
* [(70) Quand les messages seront-ils automatiquement étendus ?](#user-content-faq70)
* [(71) Comment utiliser les règles de filtrage ?](#user-content-faq71)
* [(72) Quelles sont les comptes/identités principales ?](#user-content-faq72)
* [(73) Est-ce que le transfert de messages entre les comptes est sécurisé et efficace ?](#user-content-faq73)
* [(74) Pourquoi est-ce que je vois des messages en double ?](#user-content-faq74)
* [(75) Pouvez-vous créer une version iOS, Windows, Linux, etc ?](#user-content-faq75)
* [(76) Que fait 'Effacer les messages locaux' ?](#user-content-faq76)
* [(77) Pourquoi les messages sont-ils parfois affichés avec un petit retard ?](#user-content-faq77)
* [(78) Comment utiliser les horaires ?](#user-content-faq78)
* [(79) Comment utiliser la synchronisation à la demande (manuel) ?](#user-content-faq79)
* [~~(80) Comment puis-je corriger l'erreur 'Impossible de charger BODYSTRUCTURE' ?~~](#user-content-faq80)
* [~~(81) Pouvez-vous mettre le fond du message original sombre dans le thème sombre ?~~](#user-content-faq81)
* [(82) Qu'est-ce qu'une image de suivi ?](#user-content-faq82)
* [(84) À quoi servent les contacts locaux ?](#user-content-faq84)
* [(85) Pourquoi une identité n'est-elle pas disponible ?](#user-content-faq85)
* [~~(86) Que sont les 'fonctionnalités de confidentialité supplémentaires' ?~~](#user-content-faq86)
* [(87) Que signifient les 'identifiants invalides' ?](#user-content-faq87)
* [(88) Comment puis-je utiliser un compte Yahoo, AOL ou Sky ?](#user-content-faq88)
* [(89) Comment puis-je envoyer des messages en texte brut uniquement ?](#user-content-faq89)
* [(90) Pourquoi certains textes sont-ils liés sans être liés ?](#user-content-faq90)
* [~~(91) Pouvez-vous ajouter une synchronisation périodique pour économiser la batterie ?~~](#user-content-faq91)
* [(92) Pouvez-vous ajouter le filtrage de spam, la vérification de la signature DKIM et l'autorisation SPF ?](#user-content-faq92)
* [(93) Pouvez-vous autoriser l'installation/stockage de données sur un support de stockage externe (Carte SD) ?](#user-content-faq93)
* [(94) Que signifie la bande rouge/orange à la fin de l'en-tête ?](#user-content-faq94)
* [(95) Pourquoi toutes les applications ne sont-elles pas affichées lors de la sélection d'une pièce jointe ou d'une image ?](#user-content-faq95)
* [(96) Où puis-je trouver les paramètres IMAP et SMTP ?](#user-content-faq96)
* [(97) Qu'est-ce que le "nettoyage" ?](#user-content-faq97)
* [(98) Pourquoi puis-je toujours choisir des contacts après avoir révoqué les autorisations de contacts ?](#user-content-faq98)
* [(99) Pouvez-vous ajouter un texte riche ou un éditeur de markdown ?](#user-content-faq99)
* [(100) Comment puis-je synchroniser les catégories Gmail ?](#user-content-faq100)
* [(101) Que signifie le point bleu/orange au bas des conversations ?](#user-content-faq101)
* [(102) Comment puis-je activer la rotation automatique des images ?](#user-content-faq102)
* [(103) Comment puis-je enregistrer de l'audio ?](#user-content-faq158)
* [(104) Que dois-je savoir au sujet du signalement d'erreur ?](#user-content-faq104)
* [(105) Comment fonctionne l’option itinérance à domicile ?](#user-content-faq105)
* [(106) Quels lanceurs peuvent afficher un nombre de badges avec le nombre de messages non lus ?](#user-content-faq106)
* [(107) Comment utiliser les étoiles colorées ?](#user-content-faq107)
* [~~(108) Pouvez-vous ajouter la suppression des messages de façon définitive à partir de n'importe quel dossier ?~~](#user-content-faq108)
* [~~(109) Pourquoi 'sélectionner le compte' est-il uniquement disponible dans les versions officielles ?~~](#user-content-faq109)
* [(110) Pourquoi (certains) les messages sont-ils vides et/ou les pièces jointes corrompues ?](#user-content-faq110)
* [(111) OAuth est-il pris en charge ?](#user-content-faq111)
* [(112) Quel fournisseur de messagerie recommandez-vous ?](#user-content-faq112)
* [(113) Comment fonctionne l'authentification biométrique ?](#user-content-faq113)
* [(114) Pouvez-vous ajouter une importation pour les paramètres des autres applications de messagerie ?](#user-content-faq114)
* [(115) Pouvez-vous ajouter des puces d'adresse e-mail ?](#user-content-faq115)
* [~~(116) Comment puis-je afficher les images dans les messages des expéditeurs de confiance par défaut ?~~](#user-content-faq116)
* [(117) Pouvez-vous m'aider à restaurer mon achat ?](#user-content-faq117)
* [(118) Qu'est-ce que 'Supprimer les paramètres de suivi' exactement ?](#user-content-faq118)
* [~~(119) Pouvez-vous ajouter des couleurs au widget de la boîte de réception unifiée ?~~](#user-content-faq119)
* [(120) Pourquoi les notifications de nouveaux messages ne sont-elles pas supprimées à l'ouverture de l'application ?](#user-content-faq120)
* [(121) Comment les messages sont-ils regroupés en conversation ?](#user-content-faq121)
* [~~(122) Pourquoi le nom du destinataire/adresse e-mail est-il affiché avec une couleur d'avertissement ?~~](#user-content-faq122)
* [(123) Que se passe-t-il lorsque FairEmail ne peut pas se connecter à un serveur de messagerie ?](#user-content-faq123)
* [(124) Pourquoi est-ce que je reçois "Message trop grand ou trop complexe à afficher" ?](#user-content-faq124)
* [(125) Quelles sont les fonctionnalités expérimentales actuelles ?](#user-content-faq125)
* [(126) Peut-on envoyer des aperçus de messages à mon portable ?](#user-content-faq126)
* [(127) Comment puis-je corriger 'Arguments HELO syntaxiquement invalides' ?](#user-content-faq127)
* [(128) Comment réinitialiser les questions posées, par exemple pour afficher les images ?](#user-content-faq128)
* [(129) ProtonMail, Tutanota est-il pris en charge ?](#user-content-faq129)
* [(130) Qu'est-ce que l'erreur de message ... signifie ?](#user-content-faq130)
* [(131) Pouvez-vous changer la direction pour glisser vers le message précédent/suivant ?](#user-content-faq131)
* [(132) Pourquoi les notifications de nouveaux messages sont-elles silencieuses ?](#user-content-faq132)
* [(133) Pourquoi ActiveSync n'est-il pas pris en charge ?](#user-content-faq133)
* [(134) Pouvez-vous ajouter la suppression des messages locaux ?](#user-content-faq134)
* [(135) Pourquoi les messages de la corbeille et les brouillons sont-ils affichés dans les conversations ?](#user-content-faq135)
* [(136) Comment puis-je supprimer un compte/identité/dossier ?](#user-content-faq136)
* [(137) Comment puis-je réinitialiser "Ne plus demander à nouveau" ?](#user-content-faq137)
* [(138) Peut-on ajouter une gestion de calendrier/contact/tâches/notes ?](#user-content-faq138)
* [(139) Comment puis-je corriger "L'utilisateur est authentifié mais pas connecté" ?](#user-content-faq139)
* [(140) Pourquoi le texte du message contient-il des caractères étranges ?](#user-content-faq140)
* [(141) Comment puis-je réparer le dossier 'Un brouillon est requis pour envoyer des messages' ?](#user-content-faq141)
* [(142) Comment stocker les messages envoyés dans la boîte de réception ?](#user-content-faq142)
* [~~(143) Peux-tu ajouter un dossier corbeille pour les comptes POP3 ?~~](#user-content-faq143)
* [(144) Comment puis-je enregistrer des notes vocales ?](#user-content-faq144)
* [(145) Comment puis-je définir un son de notification pour un compte, un dossier ou un expéditeur ?](#user-content-faq145)
* [(146) Comment puis-je corriger les heures de messages incorrects ?](#user-content-faq146)
* [(147) Que devrais-je savoir des versions tierces ?](#user-content-faq147)
* [(148) Comment puis-je utiliser un compte Apple iCloud ?](#user-content-faq148)
* [(149) Comment fonctionne le widget nombre de messages non lus ?](#user-content-faq149)
* [(150) Pouvez-vous ajouter des invitations au calendrier d'annulation ?](#user-content-faq150)
* [(151) Pouvez-vous ajouter une sauvegarde/restauration de messages ?](#user-content-faq151)
* [(152) Comment puis-je insérer un groupe de contacts ?](#user-content-faq152)
* [(153) Pourquoi la suppression définitive du message Gmail ne fonctionne-t-elle pas ?](#user-content-faq153)
* [~~(154) Peut-on ajouter des favicons comme photos de contact ?~~](#user-content-faq154)
* [(155) Qu'est-ce qu'un fichier winmail.dat ?](#user-content-faq155)
* [(156) Comment puis-je configurer un compte Office365 ?](#user-content-faq156)
* [(157) Comment puis-je créer un compte gratuit ?](#user-content-faq157)
* [(158) Quelle caméra / enregistreur audio recommandez-vous ?](#user-content-faq158)
* [(159) Que sont les listes anti-tracker de Disconnect ?](#user-content-faq159)
* [(160) Pouvez-vous ajouter la suppression définitive des messages sans confirmation ?](#user-content-faq160)
* [(161) Pouvez-vous ajouter un paramètre pour changer les couleurs primaire et d'accentuation ?](#user-content-faq161)
* [(162) L'IMAP NOTIFY est-il pris en charge ?](#user-content-faq162)
* [(163) Qu'est-ce que la classification des messages ?](#user-content-faq163)
* [(164) Pouvez-vous ajouter des thèmes personnalisables ?](#user-content-faq164)
* [(165) Est-ce qu'Android Auto est pris en charge ?](#user-content-faq165)
* [(166) Puis-je répéter un message sur plusieurs appareils ?](#user-content-faq166)

[J’ai une autre question.](#user-content-support)

<a name="faq1"></a>
**(1) Quelles autorisations sont nécessaires et pourquoi ?**

Les autorisations Android suivantes sont nécessaires :

* *bénéficier d'un accès complet au réseau* (INTERNET) : pour envoyer et recevoir des e-mails
* *afficher les connexions réseau* (ACCESS_NETWORK_STATE): pour surveiller les changements de connectivité à internet
* *s'exécuter au démarrage* (RECEIVE_BOOT_COMPLETED) : pour commencer la surveillance au démarrage de l'appareil
* *exécuter un service de premier plan* (FOREGROUND_SERVICE) : pour exécuter un service de premier plan sur Android 9 Pie et ultérieur. Voir aussi la question suivante
* *empêcher le téléphone de passer en mode veille* (WAKE_LOCK): pour garder l'appareil éveillé lors de la synchronisation des messages
* *Service de facturation Google Play* (BILLING): pour permettre les achats dans l'application
* *planifier une alarme exacte* (SCHEDULE_EXACT_ALARM) : pour utiliser la planification exacte de l'alarme (Android 12 et ultérieur)
* Optionnel : *lire vos contacts* (READ_CONTACTS) : pour compléter automatiquement les adresses, pour afficher les photos des contacts et [pour choisir les contacts](https://developer.android.com/guide/components/intents-common#PickContactDat)
* Facultatif : *lire le contenu de votre carte SD* (READ_EXTERNAL_STORAGE): pour accepter des fichiers provenant d'autres applications obsolètes, voir aussi [cette FAQ](#user-content-faq49)
* Facultatif : *utilisez le lecteur d'empreinted digitales* (USE_FINGERPRINT) et utilisez *le matériel biométrique* (USE_BIOMETRIC) : pour utiliser l'authentification biométrique
* Facultatif : *rechercher des comptes sur l'appareil* (GET_ACCOUNTS): pour sélectionner un compte lors de la configuration rapide de Gmail
* Android 5.1 Lollipop et antérieur: *utiliser les comptes sur l'appareil* (USE_CREDENTIALS): pour sélectionner un compte lors de la configuration rapide de Gmail (non demandée pour les versions ultérieures d'Android)
* Android 5.1 Lollipop et antérieur : *Lire le profil* (READ_PROFILE): pour lire votre nom lorsque vous utilisez la configuration rapide de Gmail (non requis dans les versions ultérieures d'Android)

[Les autorisations facultatives](https://developer.android.com/training/permissions/requesting) ne sont prises en charge qu'à partir de Android 6 Marshmallwo. Sur les versions antérieures d'Android, il vous sera demandé d'accorder les autorisations facultatives lors de l'installation de FairEmail.

Les autorisations suivantes sont nécessaires pour afficher le nombre de messages non lus dans un badge (voir aussi [cette question](#user-content-faq106)) :

* *com.sec.android.provider.badge.permission.READ*
* *com.sec.android provider.badge.permission.WRITE*
* *com.htc.launcher.permission.READ_SETTINGS*
* *com.htc.launcher.permission.UPDATE_SHORTCUT*
* *permission.permission.BADCAST_BADGE*
* *com.sonymobile.HomeKit*
* *com.anddoes.launcher.permission.UPDATE_COUNT*
* *uPDATE_BADGE*
* *com.huawei.android.launcher.permission.CHANGE_BADGE*
* *cOM.huawei.android.launcher.permission.READ_SETTINGS*
* *cOM.huawei.android.launcher.permission.WRITE_SETTINGS*
* *android.permission.READ_APP_BADGE*
* *com.oppo.launcher.permission.READ_SETTINGS*
* *com.oppo.launcher.permission.WRITE_SETTINGS*
* *me.everything.badger.permission.BADGE_COUNT_READ*
* *me.everything.badger.permission.BADGE_COUNT_WRITE*

FairEmail tiendra une liste des adresses venant des messages que vous recevez et envoyez, et utilisera cette liste pour faire des suggestions de contacts lorsqu'aucune autorisation d'accès aux contacts n'est accordée à FairEmail. Ceci signifie que vous pouvez utiliser FairEmail sans le fournisseur de contacts d'Android (carnet d'adresses). Notez que vous pouvez toujours choisir des contacts sans accorder les autorisations de contact à FairEmail, seule la suggestion de contacts ne fonctionnera pas sans les autorisations.

<br />

<a name="faq2"></a>
**(2) Pourquoi y a-t-il une notification permanente affichée ?**

Une notification dans la barre d'état, permanente, de faible priorité, avec le nombre de comptes surveillés et le nombre d'opérations en attente (voir la question suivante) est affichée pour empêcher Android de tuer le service qui prend en charge la réception continue d'e-mail. Ceci était [déjà nécessaire](https://developer.android.com/reference/android/app/Service.html#startForeground(int,%20android.app.Notification)), mais avec l'introduction du [mode veille](https://developer.android.com/training/monitoring-device-state/doze-standby) dans Android 6 Marshmallow, c'est plus que jamais nécessaire. Le mode veille arrêtera toutes les applications lorsque l'écran est éteint depuis un certain temps sauf si l'application a démarré un service de premier plan ce qui nécessite l'affichage d'une notification dans la barre d'état.

La plupart, sinon la totalité, des autres applications de messagerie électronique n'affichent pas de notification, avec comme "effet secondaire" que les nouveaux messages ne sont souvent pas signalés ou le sont avec retard et que les messages ne sont pas envoyés ou le sont avec retard.

Android affiche d'abord les icônes des notifications de haute priorité dans la barre d'état et masquera l'icône de notification de FairEmail s'il n'y a plus d'espace pour afficher les icônes. En pratique, ceci signifie que la notification ne prend pas de place dans la barre d'état, à moins qu'il y ait de la place disponible.

La notification dans la barre d'état peut être désactivée via les paramètres de notification de FairEmail :

* Android 8 Oreo et supérieur : appuyez sur le bouton *Canal de réception* et désactivez le canal via les paramètres Android (cela ne désactivera pas les notifications de nouveaux messages)
* Android 7 Nougat et inférieur : activez *Utiliser le service d'arrière-plan pour synchroniser les messages*, mais veillez à lire la remarque en dessous du paramètre

Vous pouvez changer pour une synchronisation périodique des messages dans les paramètres de réception de FairEmail pour supprimer la notification mais sachez que cela peut augmenter l'utilisation de la batterie. Voir [ici](#user-content-faq39) pour plus de détails sur l'utilisation de la batterie.

Android 8 Oreo pourrait également afficher une notification dans la barre d'état avec le texte *Les applications sont en cours d'exécution en arrière-plan*. Veuillez voir [ici](https://www.reddit.com/r/Android/comments/7vw7l4/psa_turn_off_background_apps_notification/) pour savoir comment désactiver cette notification.

Certaines personnes ont suggéré d'utiliser [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/) (FCM) au lieu d'un service Android avec une notification dans la barre d'état, mais cela nécessite que les fournisseurs de messagerie envoient des messages FCM, ou bien un serveur central où tous les messages sont collectés puis qui renvoie des messages FCM. Le premier cas ne se produira pas et le second aurait des implications significatives sur la vie privée.

Si vous êtes arrivé·e ici en cliquant sur la notification, vous devez savoir que le prochain clic ouvrira la boîte de réception unifiée.

<br />

<a name="faq3"></a>
**(3) Que sont les opérations et pourquoi sont-elles en attente ?**

La notification de faible priorité dans la barre d'état montre le nombre d'opérations en attente, ce qui peut être :

* *ajouter* : ajouter un message au dossier distant
* *déplacer* : déplacer le message vers un autre dossier distant
* *copie* : copier le message dans un autre dossier distant
* *récupérer* : récupérer un message modifié (poussé)
* *supprimer* : supprimer le message du dossier distant
* *vu* : marquer le message comme lu/non lu dans le dossier distant
* *répondu* : marquer le message comme répondu dans le dossier distant
* *drapeau* : ajouter/supprimer une étoile dans le dossier distant
* *mot-clé* : ajouter/supprimer le drapeau IMAP dans le dossier distant
* *label*: définir/réinitialiser le label Gmail dans le dossier distant
* *en-têtes* : télécharger les en-têtes de message
* *brut* : télécharger le message brut
* *corps* : télécharger le texte du message
* *pièce jointe* : télécharger la pièce jointe
* *Sync* : synchroniser les messages locaux et distants
* *s'abonner* : s'abonner au dossier distant
* *purger* : supprimer tous les messages du dossier distant
* *envoyer* : envoyer un message
* *existe* : vérifiez si le message existe
* *règle* : exécute la règle sur le corps du texte
* *expunge* : supprimer définitivement les messages

Les opérations ne sont traitées que lorsqu'il y a une connexion au serveur de messagerie ou lors d'une synchronisation manuelle. Voir aussi [cette question](#user-content-faq16).

<br />

<a name="faq4"></a>
**(4) Comment puis-je utiliser un certificat de sécurité non valide / un mot de passe vide / une connexion en clair ?**

*... Non fiable ... pas dans le certificat ...*
<br />
*... Certificat de sécurité invalide (impossible de vérifier l'identité du serveur) ...*

Ceci peut être dû à l'utilisation d'un nom de serveur incorrect, il faut donc d'abord vérifier le nom du serveur dans les paramètres avancés du compte/de l'identité (Configuration manuelle et plus d'options). Veuillez consulter la documentation de votre fournisseur de messagerie concernant le nom du serveur.

Vous pouvez résoudre le problème en contactant votre hébergeur ou en obtenant un certificat valide. En effet, les certificats de sécurité non valide ne sont pas sécurisés et permettent des [Attaque de l'homme du milieu](https://en.wikipedia.org/wiki/Man-in-the-middle_attack). Si vous souhaitez une alternative gratuite, vous pouvez obtenir un certificat gratuit depuis [Let’s Encrypt](https://letsencrypt.org).

L'option la plus rapide mais également la moins sécurisée (non recommandée), consiste à activer *les connexions non sécurisées* dans les paramètres avancés de l'identité (menu de navigation, *Paramètres*, *Configuration manuelle et plus d'options*, *Identités*, sélectionnez l'identité *Avancé*).

Vous pouvez également accepter l'empreinte de certificats de sécurité invalide comme suit:

1. Assurez-vous que vous utilisez une connexion internet fiable (pas de réseaux Wi-Fi publics, etc)
1. Aller à l'écran de configuration rapide via le menu de navigation (Faites glisser depuis le côté gauche vers l'intérieur)
1. Appuyez sur Configuration manuelle, appuyez sur Comptes/Identités et appuyez sur le compte défectueux et l'identité
1. Contrôlez/enregistrez le compte et l'identité
1. Cochez la case en dessous du message d'erreur et enregistrez à nouveau

Ceci gardera le certificat du serveur pour éviter une attaque de l'homme du milieu.

Notez que d'anciennes versions d'Android pourraient ne pas reconnaître les plus récentes autorités de certification comme Let's Encrypt résultant d'un connexion classée comme non sécurisée, voir [plus d'information](https://developer.android.com/training/articles/security-ssl).

<br />

*Ancre de confiance pour le chemin de certification introuvable*

*... java.security.cert.CertPathValidatorException: Ancre de confiance pour le chemin de certification introuvable... * signifie que le gestionnaire de confiance Android par défaut n'a pas pu vérifier la chaîne de certificats du serveur.

Ceci peut être dû au fait que le certificat racine n'est pas installé sur votre appareil ou parce que des certificats intermédiaires sont manquants, par exemple parce que le serveur de messagerie ne les a pas envoyés.

Vous pouvez résoudre le premier problème en téléchargeant et installant le certificat racine à partir du site web du fournisseur du certificat.

Le deuxième problème doit être résolu en modifiant la configuration du serveur ou en important les certificats intermédiaires sur votre appareil.

Vous pouvez également épingler le certificat, voir ci-dessus.

<br />

*Mot de passe vide*

Votre nom d'utilisateur peut être facilement deviné, donc c'est assez peu sûr, sauf si le serveur SMTP n'est disponible que via un réseau local restreint ou un VPN.

*Connexion en clair*

Votre nom d'utilisateur et votre mot de passe ainsi que tous les messages seront envoyés et reçus non chiffrés, qui est **très peu sécurisé** car une [attaque man-in-the-middle](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) est très facile sur une connexion non chiffrée.

Si vous voulez toujours utiliser un certificat de sécurité invalide, un mot de passe vide ou une connexion en clair, vous aurez besoin d'activer les connexions non sécurisées dans les paramètres du compte et/ou de l'identité. STARTTLS doit être sélectionné pour les connexions en clair. Si vous activez les connexions non sécurisées, vous devrez vous connecter via des réseaux privés et de confiance uniquement et jamais via des réseaux publics comme ceux offerts dans les hôtels, les aéroports, etc.

<br />

<a name="faq5"></a>
**(5) Comment puis-je personnaliser la vue des messages ?**

Dans le menu à trois points, vous pouvez activer ou désactiver ou sélectionner :

* *taille de texte* : pour trois tailles de police différentes
* *Vue compacte* : pour des éléments de message plus condensés et une police de texte de message plus petite

Dans l'onglet Affichage des paramètres, vous pouvez activer ou désactiver par exemple :

* *Boîte de réception unifiée* : pour désactiver la boîte de réception unifiée et pour lister les dossiers sélectionnés pour la boîte de réception unifiée
* *Style tabulaire*: afficher une liste linéaire au lieu de cartes
* *Grouper par date*: affiche la date en en-tête au-dessus des messages ayant la même date
* *Afficher en mode conversation*: pour désactiver l'affichage en mode conversation et afficher à la place les messages de manière individuelle
* *Barre d'action de conversation*: pour désactiver la barre de navigation du bas
* *Couleur de surbrillance*: pour sélectionner une couleur pour l'expéditeur des messages non lus
* *Afficher la photo des contacts*: pour masquer la photo des contacts
* *Afficher les noms et les adresses e-mail*: pour afficher les noms ou afficher les noms et les adresses e-mail
* *Afficher l’objet en italique*: pour afficher l'objet du message en texte normal
* *Afficher les étoiles de suivi*: pour masquer les étoiles de suivi (favoris)
* *Afficher l'aperçu du message*: pour afficher 1-4 lignes du corps du message
* *Afficher par défaut les détails de l'adresse*: pour développer par défaut la section d'adresses
* *Afficher automatiquement le message original pour les contacts connus*: pour automatiquement afficher les messages originaux pour les contacts de votre appareil, merci de lire [cette FAQ](#user-content-faq35)
* *Afficher automatiquement les images pour les contacts connus*: pour automatiquement afficher les images pour les contacts de votre appareil, merci de lire [cette FAQ](#user-content-faq35)

Notez que l'aperçu du message ne peut être affiché que lorsque le texte de celui-ci a été téléchargé. Les textes de messages plus volumineux ne sont pas téléchargés par défaut sur les réseaux limités (généralement mobiles). Vous pouvez changer ceci dans les paramètres de connexion.

Certaines personnes demandent :

* d'afficher l'objet en gras, mais l'affichage en gras est déjà utilisé pour mettre en évidence les messages non lus
* de déplacer l'étoile à gauche, mais il est beaucoup plus facile de manipuler l'étoile sur le côté droit

<br />

<a name="faq6"></a>
**(6) Comment puis-je me connecter à Gmail / G suite ?**

Si vous utilisez la version Play Store ou GitHub de FairEmail, vous pouvez utiliser l'assistant de configuration rapide pour configurer facilement un compte Gmail et une identité. La mise en place rapide de Gmail n'est pas disponible pour les versions tierces, dont celles venant de F-Droid car Google approuve l'utilisation de l'OAuth uniquement sur les versions officielles.

Si vous ne voulez ou ne pouvez pas utiliser de compte Google sur votre appareil, par exemple sur les appareils Huawei récents, vous pouvez soit autoriser l'accès pour les "applications moins sécurisées" et utiliser le mot de passe de votre compte (non recommandé) ou activer la double authentification et utiliser le mot de passe d'application. Pour utiliser un mot de passe vous devez créer un compte et vous identifier via la configuration manuelle au lieu de l'assistant de configuration.

**Important**: Quelquefois Google envoie cette alerte:

*[ALERT] Veuillez vous connecter via votre navigateur web : https://support.google.com/mail/accounts/answer/78754 (Echec)*

Cette vérification de sécurité de Google est plus souvent déclenchée lorsque l'option *applications moins sécurisées* est activée, elle l'est moins avec un mot de passe d'application et elle ne l'est presque jamais lorsque vous utilisez un compte sur l'appareil (OAuth).

Référez-vous à [cette FAQ](#user-content-faq111) pour savoir pourquoi seuls les comptes sur appareils peuvent être utilisés.

Notez qu'un mot de passe spécifique à l'application est nécessaire lorsque l'authentification à deux facteurs est activée.

<br />

*Mot de passe spécifique à l'application*

Voir [ici](https://support.google.com/accounts/answer/185833) sur la façon de générer un mot de passe spécifique à l'application.

<br />

*Activer "Applications moins sécurisées"*

**Important**: utiliser cette méthode n'est pas recommandé car elle est moins fiable.

**Important**: Les comptes Gsuite autorisés avec un nom d'utilisateur/mot de passe cesseront de fonctionner [dans un avenir proche](https://gsuiteupdates.googleblog.com/2019/12/less-secure-apps-oauth-google-username-password-incorrect.html).

Voir [ici](https://support.google.com/accounts/answer/6010255) comment autoriser les "applications moins sécurisées" ou aller [directement à l'ajustement du paramètre](https://www.google.com/settings/security/lesssecureapps).

Si vous utilisez plusieurs comptes Gmail, assurez-vous de modifier le paramètre "Autoriser les applications moins sécurisées" du ou des comptes concerné(s).

Faites attention à bien quitter l'écran de réglage des "applications moins sécurisées" en utilisant la flèche retour arriere pour enregistrer les modifications.

Si vous utilisez cette méthode, vous devriez utiliser un [mot de passe robuste](https://en.wikipedia.org/wiki/Password_strength) pour votre compte Gmail, ce qui est de toute façon une bonne idée. Notez que l'utilisation du protocole [standard](https://tools.ietf.org/html/rfc3501) IMAP n'est en soi pas moins sûre .

Si les "applications moins sécurisées" ne sont pas activées, vous recevrez l'erreur *l'authentification a échoué - identifiants non valides* pour les comptes (IMAP) et *nom d'utilisateur et mot de passe non acceptés* pour les identités (SMTP).

<br />

*Général*

Il se peut que vous receviez l'alerte "*S'il vous plaît connectez-vous via votre navigateur web*". Cela se produit lorsque Google considère que le réseau sur lequel vous vous connectez à Internet (il peut s'agir d'un VPN) n'est pas sûr. Cela peut être évité en utilisant l'assistant de configuration rapide de Gmail ou un mot de passe spécifique à l'application.

Voir [ici](https://support.google.com/mail/answer/7126229) pour les instructions de Google et [ici](https://support.google.com/mail/accounts/answer/78754) pour le dépannage.

<br />

<a name="faq7"></a>
**(7) Pourquoi les messages envoyés n'apparaissent-ils pas (directement) dans le dossier « Envoyés » ? **

Les messages envoyés sont normalement déplacés de la boîte d'envoi vers le dossier "Envoyés" dès que votre fournisseur ajoute les messages envoyés au dossier "Envoyés". Ceci nécessite qu'un dossier "Envoyés" soit sélectionné dans les paramètres du compte et que le dossier "Envoyés" soit configuré pour être synchronisé.

Certains fournisseurs ne gardent pas de trace des messages envoyés ou le serveur SMTP utilisé peut ne pas être lié au fournisseur. Dans ce cas, FairEmail ajoutera automatiquement les messages envoyés au dossier "Envoyés" lors de la synchronisation du dossier "Envoyés" ce qui se produira après l'envoi d'un message. Notez que cela entraînera un trafic Internet supplémentaire.

~~Si cela ne se produit pas , il se peut que votre fournisseur ne garde pas de trace des messages envoyés ou bien vous utilisez un serveur SMTP qui n'est pas lié au fournisseur.~~ ~~Dans ces cas, vous pouvez activer le paramètre avancé d'identité *Stocker les messages envoyés* pour permettre à FairEmail d'ajouter les messages envoyés au dossier envoyé juste après avoir envoyé un message.~~ ~~Notez que l'activation de ce paramètre peut entraîner des messages en double si votre fournisseur ajoute également les messages envoyés au dossier envoyé.~~ ~~Soyez également conscient que l'activation de ce paramètre entraînera une consommation supplémentaire de données, en particulier lorsque vous envoyez des messages avec de pièces jointes volumineuses.~~

~~Si des messages envoyés dans la boîte d'envoi ne sont pas retrouvés dans le dossier "Envoyés" lors d'une synchronisation complète, ils seront également déplacés de la boîte d'envoi vers le dossier "Envoyés".~~ ~~Une synchronisation complète se produit lors de la reconnexion au serveur ou lors de la synchronisation périodique ou manuelle.~~ ~~Vous préférerez probablement activer le paramètre avancé *Stocker les messages envoyés* à la place, pour déplacer les messages vers le dossier envoyé plus rapidement.~~

<br />

<a name="faq8"></a>
**(8) Puis-je utiliser un compte Microsoft Exchange ?**

Le protocole "Microsoft Exchange Web Services" [est en cours de suppression](https://techcommunity.microsoft.com/t5/Exchange-Team-Blog/Upcoming-changes-to-Exchange-Web-Services-EWS-API-for-Office-365/ba-p/608055). Il y a donc peu d’intérêt à ajouter ce protocole aujourd'hui.

Vous pouvez utiliser un compte Microsoft Exchange si il est accessible par IMAP, ce qui est généralement le cas. Voir [ici](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793) pour plus d'informations.

Veuillez notez que le début de la description de FairEmail souligne que les protocoles non-standard, tel que Microsoft Exchange Web Services et Microsoft ActiveSync ne sont pas supportés.

Veuillez consulter [ici](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) la documentation de Microsoft sur la configuration d'un client de messagerie. Il y a également une section sur les erreurs de connexion courantes et leurs solutions.

Certaines anciennes versions du serveur Exchange souffrent d'un bug causant des messages vides et des pièces jointes corrompues. Referez-vous à [cette FAQ](#user-content-faq110) pour la solution de rechange .

Veuillez consulter [cette FAQ](#user-content-faq133) sur la compatibilité avec ActiveSync.

Veuillez consulter [cette FAQ](#user-content-faq111) sur la compatibilité avec OAuth.

<br />

<a name="faq9"></a>
**(9) Que sont les identités / comment ajouter un alias ?**

Les identités sont les adresses mails *depuis* lesquelles vous envoyez un mail via un serveur mail (SMTP).

Certains fournisseurs vous permettent d'avoir plusieurs alias. Vous pouvez les configurer en attribuant le champ d'adresse e-mail d'une identité supplémentaire à l'adresse alias et en définissant le champ nom d'utilisateur à votre adresse e-mail principale.

Notez que vous pouvez copier une identité en appuyant longuement dessus.

Autrement, vous pouvez activer *Autoriser l'édition de l'adresse de l'expéditeur* dans les paramètres avancés d'une identité existante pour modifier le nom d'utilisateur lors de la rédaction d'un nouveau message. si votre fournisseur le permet.

FairEmail mettra automatiquement à jour les mots de passe des identités liées lorsque vous mettez à jour le mot de passe du compte associé ou d'une identité liée.

Voir [cette FAQ](#user-content-faq33) sur la modification du nom d'utilisateur des adresses e-mail.

<br />

<a name="faq10"></a>
**~~(10) Que signifie "UIDPLUS n'est pas pris en charge" ?~~**

~~Le message d'erreur *UIDPLUS non pris en charge* signifie que votre fournisseur de messagerie ne fournit pas l'extension IMAP [UIDPLUS](https://tools.ietf.org/html/rfc4315). Cette extension IMAP est nécessaire pour implémenter la synchronisation bidirectionnelle qui n'est pas une fonctionnalité optionnelle. Ainsi, à moins que votre fournisseur ne puisse activer cette extension, vous ne pouvez pas utiliser FairEmail avec ce fournisseur.~~

<br />

<a name="faq11"></a>
**~~(11) Pourquoi POP n'est-il pas pris en charge ?~~**

~~En outre, tout fournisseur de messagerie décent prend en charge [IMAP](https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol) de nos jours,~~ ~~ainsi l'utilisation de [POP](https://en.wikipedia.org/wiki/Post_Office_Protocol) entraînera un usage supplémentaire inutile de la batterie et un retard dans les notifications de nouveaux messages.~~ ~~De plus, le POP ne convient pas à la synchronisation bidirectionnelle alors que, de nos jours, les gens lisent et écrivent souvent leurs messages sur des appareils différents..~~

~~Fondamentalement, POP ne prend en charge que le téléchargement et la suppression des messages de la boîte de réception.~~ ~~Ainsi, les opérations courantes comme la configuration des attributs de message (lu, étoilé, répondu, etc), l'ajout (sauvegarde) et le déplacement des messages ne sont pas possibles.~~

~~Voir aussi [ce que Google écrit à ce sujet](https://support.google.com/mail/answer/7104828).~~

~~Par exemple [Gmail peut importer des messages](https://support.google.com/mail/answer/21289) depuis un autre compte POP~~ ~~ce qui peut être utilisé comme solution de contournement lorsque votre fournisseur ne supporte pas IMAP.~~

~~tl;dr; pensez à passer à IMAP.~~

<br />

<a name="faq12"></a>
**(12) Comment fonctionne le chiffrement/déchiffrement ?**

La communication avec les serveurs de messagerie est toujours chiffrée, à moins que vous ne l'ayez explicitement désactivée. Cette question concerne le chiffrement de bout en bout optionnel avec PGP ou S/MIME. L'expéditeur et le destinataire devraient d'abord s'entendre sur cela et échanger des messages signés pour transférer leur clé publique afin de pouvoir envoyer des messages chiffrés.

<br />

*Généralités*

Veuillez [voir ici](https://en.wikipedia.org/wiki/Public-key_cryptography) le fonctionnement du chiffrement par des clé publique/privée.

Le chiffrement en bref :

* Les messages **sortants** sont chiffrés avec la **clé publique** du destinataire
* Les messages **entrants** sont déchiffrés avec la **clé privée** du destinataire

La signature en bref :

* Les messages **sortants** sont signés avec la **clé privée** de l'expéditeur
* Les messages **entrants** sont vérifiés avec la **clé publique** de l'expéditeur

Pour signer/chiffrer un message, il suffit de sélectionner la méthode appropriée dans la boîte de dialogue d'envoi. Vous pouvez toujours ouvrir la boîte de dialogue d'envoi en utilisant le menu déroulant symbolisé par trois points dans le cas où vous avez sélectionné *Ne plus afficher* avant.

Pour vérifier une signature ou déchiffrer un message reçu, ouvrez le message et appuyez simplement sur l'icône du geste ou du cadenas juste en dessous de la barre d'action du message.

La première fois que vous envoyez un message signé/chiffré, il se peut que l'on vous demande une clé de signature. FairEmail stockera automatiquement la clé de signature sélectionnée dans l'identité utilisée pour la prochaine fois. Si vous avez besoin de réinitialiser la clé de signature, enregistrez simplement l'identité ou appuyez longuement sur l'identité dans la liste des identités et sélectionnez *réinitialiser la clé de signature*. La clé de signature sélectionnée est visible dans la liste des identités. Si vous avez besoin de sélectionner une clé au cas par cas, vous pouvez créer plusieurs identités pour le même compte avec la même adresse e-mail.

Dans les paramètres de chiffrement, vous pouvez sélectionner la méthode de cryptage par défaut (PGP ou S/MIME), activer *Signer par défaut*, *Chiffrer par défaut* et *Déchiffrer automatiquement les messages*, mais sachez que le décryptage automatique n'est pas possible si l'interaction de l'utilisateur est requise, comme la sélection d'une clé ou la lecture d'un jeton de sécurité.

Les textes/pièces jointes de message à chiffrer et les textes/pièces jointes du message déchiffré sont stockés localement seulement et ne seront jamais ajoutés au serveur distant. Si vous voulez annuler le déchiffrement, vous pouvez utiliser l'option *resynchroniser* dans le menu à trois points de la barre d'action des messages.

<br />

*PGP*

Vous devrez d'abord installer et configurer [OpenKeychain](https://f-droid.org/en/packages/org.sufficientlysecure.keychain/). FairEmail a été testé avec OpenKeychain version 5.4. Les versions ultérieures seront probablement compatibles, mais les versions antérieures pourraient ne pas l'être.

**Important**: l'application OpenKeychain est connue pour planter (silencieusement) lorsque l'application qui l'appelle (FairEmail) n'est pas encore autorisée et obtient une clé publique existante. Vous pouvez contourner cela en essayant d'envoyer un message signé/chiffré à un expéditeur avec une clé publique inconnue.

**Important**: si l'application OpenKeychain ne peut (plus) trouver de clé, vous devrez peut-être réinitialiser une clé précédemment sélectionnée. Cela peut être fait en appuyant longuement sur une identité dans la liste des identités (Paramètres, appuyez sur Configuration manuelle, appuyez sur Identités).

**Important**: pour permettre aux applications comme FairEmail de se connecter de manière fiable au service OpenKeychain pour chiffrer/déchiffrer les messages, il peut être nécessaire de désactiver les optimisations de batterie pour l'application OpenKeychain.

**Important**: l'application OpenKeychain aurait besoin d'une autorisation de contact pour fonctionner correctement.

**Important**: sur certaines versions d'Android / appareils, il est nécessaire d'activer *Afficher les fenêtres pop-up lors de l'exécution en arrière-plan* dans les autorisations supplémentaires des paramètres de l'application Android pour l'application OpenKeychain. Sans cette autorisation, le brouillon sera enregistré, mais la fenêtre popup d'OpenKeychain pour confirmer/sélectionner pourrait ne pas apparaître.

FairEmail enverra l'en-tête [Autocrypt](https://autocrypt.org/) pour utilisation par d'autres clients de messagerie, mais seulement pour les messages signés et chiffrés parce que trop de serveurs de messagerie ont des problèmes avec l'en-tête souvent longue Autocrypt. Notez que le moyen le plus sûr de démarrer un échange de messages chiffrés est d'envoyer d'abord des messages signés. Les en-têtes Autocrypt reçus seront envoyés à l'application OpenKeychain pour être stockés lors de la vérification d'une signature ou du déchiffrement d'un message.

Bien que cela ne devrait pas être nécessaire pour la plupart des clients de messagerie, vous pouvez joindre votre clé publique à un message et si vous utilisez l'extension *.key* , le type mime *application/pgp-keys* sera correct.

Toute la gestion des clés est déléguée à l'application OpenKey Chain pour des raisons de sécurité. Cela signifie également que FairEmail ne stocke pas les clés PGP.

Le PGP chiffré en ligne dans les messages reçus est pris en charge, mais les signatures PGP en ligne et PGP en ligne dans les messages sortants ne sont pas pris en charge, voir [ici](https://josefsson.org/inline-openpgp-considered-harmful.html) la raison.

Les messages uniquement signés ou uniquement chiffrés ne sont pas une bonne idée, veuillez en voir ici la raison :

* [Considérations sur OpenPGP Partie I](https://k9mail.github.io/2016/11/24/OpenPGP-Considerations-Part-I.html)
* [Considérations sur OpenPGP Partie II](https://k9mail.github.io/2017/01/30/OpenPGP-Considerations-Part-II.html)
* [Considérations sur OpenPGP Partie III Autocrypt](https://k9mail.github.io/2018/02/26/OpenPGP-Considerations-Part-III-Autocrypt.html)

Les messages uniquement signés sont pris en charge, les messages sera uniquement chiffrés ne sont pas pris en charge.

Erreurs courantes :

* *Pas de clé*: il n'y a pas de clé PGP disponible pour l'une des adresses e-mail listées
* *Clé de chiffrement manquente*: il y a probablement une clé sélectionnée dans FairEmail qui n'existe plus dans l'application OpenKeychain. La réinitialisation de la clé (voir ci-dessus) résoudra probablement ce problème.
* *La clé pour la vérification de la signature est manquante*: la clé publique pour l'expéditeur n'est pas disponible dans l'application OpenKeychain. Cela peut également être causé par la désactivation d'Autocrypt dans les paramètres de cryptage ou par le non-envoi de l'en-tête Autocrypt.

<br />

*S/MIME*

Chiffrer un message nécessite la ou les clés publiques du ou des destinataires. La signature d'un message nécessite votre clé privée.

Les clés privées sont stockées par Android et peuvent être importées via les paramètres de sécurité avancés Android. Il y a un raccourci (bouton) pour cela dans les paramètres de chiffrement. Android vous demandera de définir un code PIN, un schéma ou un mot de passe si vous ne l''avez pas fait avant. Si vous avez un appareil Nokia avec Android 9, veuillez [d'abord lire ceci](https://nokiamob.net/2019/08/10/a-bug-prevents-nokia-1-owners-from-unlocking-their-screen-even-with-right-pin-pattern/).

Notez que les certificats peuvent contenir plusieurs clés à des fins multiples, par exemple pour l'authentification, le chiffrement et la signature. Android importe seulement la première clé, afin d'importer toutes les clés, le certificat doit d'abord être divisé. Ce n'est pas très trivial et il est conseillé de demander l'aide du fournisseur de certificats.

Notez que la signature S/MIME avec d'autres algorithmes que RSA est prise en charge, mais soyez conscient que d'autres clients de messagerie pourraient ne pas la prendre en charge. Le chiffrement S/MIME n'est possible qu'avec des algorithmes symétriques ce qui signifie dans la pratique, l'utilisation de RSA.

La méthode de cryptage par défaut est PGP, mais la dernière méthode de cryptage utilisée sera mémorisée pour l'identité sélectionnée pour la prochaine fois. Vous pouvez appuyer longuement sur le bouton Envoyer pour modifier la méthode de chiffrement pour une identité. Si vous utilisez à la fois le chiffrement PGP et S/MIME pour la même adresse e-mail, il peut être utile de copier l'identité afin que vous puissiez changer la méthode de chiffrement en sélectionnant l'une des deux identités. Vous pouvez appuyer longuement sur une identité dans la liste des identités (via la configuration manuelle dans l'écran principal de configuration) pour copier une identité.

Pour autoriser différentes clés privées pour la même adresse e-mail, FairEmail vous permettra toujours de sélectionner une clé lorsqu'il y a plusieurs identités avec la même adresse e-mail pour le même compte.

Les clés publiques sont stockées par FairEmail et peuvent être importées lors de la vérification d'une signature pour la première fois ou via les paramètres de chiffrement (format PEM ou DER).

FairEmail vérifie à la fois la signature et la chaîne complète de certificats.

Erreurs courantes :

* *Aucun certificat ne correspond à targetContraints*: cela signifie probablement que vous utilisez une ancienne version de FairEmail
* *impossible de trouver un chemin de certification valide pour la cible demandée*: fondamentalement cela signifie qu'un ou plusieurs certificats intermédiaires ou racine n'ont pas été trouvés
* *La clé privée ne correspond à aucune clé de chiffrement*: la clé sélectionnée ne peut pas être utilisée pour déchiffrer le message, probablement parce que c'est une clé incorrecte
* *Aucune clé privée*: aucun certificat n'a été sélectionné ou aucun certificat n'était disponible dans le magasin de clés Android

Si la chaîne de certificats est incorrecte, vous pouvez appuyer sur le petit bouton d'information pour afficher tous les certificats. À la suite des détails du certificat, l'émetteur ou "selfSign" est affiché. Un certificat est auto-signé lorsque le sujet et l'émetteur sont les mêmes. Les certificats d'une autorité de certification (CA) sont marqués avec "[keyCertSign](https://tools.ietf.org/html/rfc5280#section-4.2.1.3)". Les certificats trouvés dans le magasin de clés Android sont marqués avec "Android".

Une chaîne valide ressemble à ceci :

```
Votre certificat > zéro ou plus de certificats intermédiaires > CA (racine) marqué par "Android"
```

Notez qu'une chaîne de certificats sera toujours invalide si aucun certificat d'ancrage ne peut être trouvé dans le magasin de clés Android, ce qui est fondamental à la validation du certificat S/MIME.

Veuillez voir [ici](https://support.google.com/pixelphone/answer/2844832?hl=en) comment vous pouvez importer des certificats dans le magasin de clés Android.

L'utilisation de clés expirées, de messages chiffrés/signés en ligne et de jetons de sécurité matériels n'est pas prise en charge.

Si vous recherchez un certificat gratuit (test) S/MIME, voir [ici](http://kb.mozillazine.org/Getting_an_SMIME_certificate) pour les options. S'il vous plaît assurez-vous de [lire ceci en premier](https://davidroessli.com/logs/2019/09/free-smime-certificates-in-2019/#update20191219) si vous voulez demander un certificat S/MIME Actalis. Si vous recherchez un certificat S/MIME bon marché, j'ai eu une bonne expérience avec [Certum](https://www.certum.eu/en/smime-certificates/).

Comment extraire une clé publique d'un certificat S/MIME:

```
openssl pkcs12 -in filename.pfx/p12 -clcerts -nokeys -out cert.pem
```

Vous pouvez décoder les signatures S/MIME, etc, [ici](https://lapo.it/asn1js/).

<br />

*pretty Easy privacy*

Il n'y a pour le moment [aucune norme approuvée](https://tools.ietf.org/id/draft-birk-pep-00.html) pour Pretty Easy Privacy (p≡p) et peu de gens l'utilisent.

Cependant, FairEmail peut envoyer et recevoir des messages chiffrés par PGP, qui sont compatibles avec p≡p. De plus, FairEmail comprend les messages entrants de p≡p depuis la version 1. 519, ainsi le sujet chiffré sera affiché et le texte du message intégré sera mieux affiché.

<br />

La signature / l'encryptage S/MIME est une fonctionnalité pro, mais toutes les autres opérations PGP et S/MIME sont libres d'utilisation.

<br />

<a name="faq13"></a>
**(13) Comment fonctionne la recherche sur l'appareil/le serveur ?**

Vous pouvez commencer par rechercher des messages sur l'expéditeur (de), le destinataire (à, cc, cci), le sujet, mots clés ou le texte du message en utilisant la loupe dans la barre d'action d'un dossier. Vous pouvez également rechercher depuis n'importe quelle application en sélectionnant *Chercher un mél* dans le menu contextuel copier/coller.

La recherche dans la boîte de réception unifiée recherchera dans tous les dossiers de tous les comptes, la recherche dans la liste de dossiers va chercher dans le compte associé seulement et la recherche dans un dossier ne sera effectuée que dans ce dossier.

Les messages seront d'abord recherchés sur l'appareil. Il y aura un bouton en bas avec une icône "rechercher à nouveau" pour continuer la recherche sur le serveur. Vous pouvez choisir dans quel dossier continuer la recherche.

Le protocole IMAP ne prend pas en charge la recherche dans plusieurs dossiers en même temps. La recherche sur le serveur est une opération coûteuse, il n'est donc pas possible de sélectionner plusieurs dossiers.

La recherche de messages locaux est insensible à la casse et au texte partiel. Le texte du message des messages locaux ne sera pas recherché si le texte du message n'a pas encore été téléchargé. La recherche sur le serveur peut être sensible à la casse ou insensible à la casse et peut être sur du texte partiel ou des mots entiers, selon le fournisseur.

Certains serveurs ne peuvent pas gérer la recherche dans le texte du message lorsqu'il y a un grand nombre de messages. Dans ce cas, il y a une option pour désactiver la recherche dans le texte du message.

Il est possible d'utiliser les opérateurs de recherche Gmail en préfixant une commande de recherche avec *raw :*. Si vous avez configuré un seul compte Gmail, vous pouvez lancer une recherche brute directement sur le serveur en recherchant dans la boîte de réception unifiée. Si vous avez configuré plusieurs comptes Gmail, vous devrez d'abord naviguer dans la liste des dossiers ou dans le dossier d'archives (tous les messages) du compte Gmail dans lequel vous souhaitez effectuer une recherche. Veuillez [voir ici](https://support.google.com/mail/answer/7190) pour les éventuels opérateurs de recherche. Par exemple :

`
raw:larger:10M`

La recherche d'un grand nombre de messages sur l'appareil n'est pas très rapide en raison de deux limitations :

* [sqlite](https://www.sqlite.org/), le moteur de base de données d'Android a une limite de taille d'enregistrement, empêchant les textes de message d'être stockés dans la base de données
* Les applications Android ne disposent que d'une mémoire limitée pour fonctionner, même si l'appareil a beaucoup de mémoire disponible

Cela signifie que la recherche d'un texte de message nécessite que les fichiers contenant le texte du message soient être ouverts un par un pour vérifier si le texte recherché est contenu dans le fichier, qui est un processus relativement coûteux.

Dans les *paramètres divers* vous pouvez activer *Construire l'index de recherche* pour augmenter significativement la vitesse de recherche sur l'appareil, mais soyez conscient que cela augmentera l'utilisation de la batterie et de l'espace de stockage. L'index de recherche est basé sur des mots, donc la recherche de texte partiel n'est pas possible. La recherche à l'aide de l'index de recherche est par défaut ET, donc la recherche de *pomme orange* recherche la pomme ET l'orange. Les mots séparés par des virgules génèrent une recherche avec OU. Par exemple * pomme, orange * recherchera pomme OU orange. Les deux peuvent être combinées, donc la recherche de *pomme, banane orange* va chercher pomme OU (orange ET banane). L'utilisation de l'index de recherche est une fonctionnalité pro.

Depuis la version 1.1315, il est possible d'utiliser des expressions de recherche telles que:

```
pomme +banane -cerise ?noix
```

Cela se traduira par une recherche comme ceci :

```
("pomme" ET "banane" ET PAS "cerise") OU "noix"
```

Les expressions de recherche peuvent être utilisées pour rechercher sur l'appareil via l'index de recherche et pour rechercher sur le serveur de messagerie, mais pas pour rechercher sur l'appareil sans index de recherche pour des raisons de performance.

La recherche sur l'appareil est une fonctionnalité gratuite, l'utilisation de l'index de recherche et la recherche sur le serveur sont une fonctionnalité pro.

<br />

<a name="faq14"></a>
**(14) Comment puis-je configurer un compte Outlook / Live / Hotmail ?**

Un compte Outlook / Live / Hotmail peut être configuré via l'assistant de configuration rapide et en sélectionnant *Outlook*.

Pour utiliser un compte Outlook, Live ou Hotmail avec l'authentification à deux facteurs activée, vous devez créer un mot de passe d'application. Voir [ici](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification) pour les détails.

Voir [ici](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) pour les instructions de Microsoft.

Pour configurer un compte Office 365, veuillez consulter [cette FAQ](#user-content-faq156).

<br />

<a name="faq15"></a>
**(15) Pourquoi le texte du message continue-t-il de se charger indéfiniment ?**

L'en-tête du message et le corps du message sont récupérés séparément du serveur. Le texte du message des messages de plus grande taille n'est pas pré-récupéré sur les connexions limitées et sera récupéré à la demande lors de l'expansion d'un message. Le texte du message continuera à se charger s'il n'y a pas de connexion au compte, voir aussi la question suivante, ou s'il y a d'autres opérations en cours d'exécution comme la synchronisation des messages.

Vous pouvez vérifier le compte et la liste des dossiers pour le compte, et l'état du dossier (voir la légende pour la signification des icônes) ainsi que la liste des opérations accessibles via le menu de navigation principal pour les opérations en attente (voir [cette FAQ](#user-content-faq3) pour la signification des opérations).

Si FairEmail est suspendu en raison de problèmes de connectivité antérieurs, veuillez consulter [cette FAQ](#user-content-faq123), vous pouvez forcer la synchronisation via le menu à trois points.

Dans les paramètres de réception, vous pouvez définir la taille maximale pour le téléchargement automatique des messages sur les connexions limitées.

Les connexions mobiles sont presque toujours limitées et certains points d'accès Wi-Fi (payants) le sont aussi.

<br />

<a name="faq16"></a>
**(16) Pourquoi les messages ne sont-ils pas synchronisés ?**

Les causes possibles de non-synchronisation des messages (envoyés ou reçus) sont:

* Le compte ou les dossier(s) ne sont pas configurés pour synchroniser
* Le nombre de jours pour synchroniser le message est trop faible
* Il n'y a pas de connexion Internet utilisable
* Le serveur de messagerie est temporairement indisponible
* Android a arrêté le service de synchronisation

Vérifiez donc les paramètres de votre compte et de votre dossier et vérifiez si les comptes/dossiers sont connectés (voir la légende dans le menu de navigation pour la signification des icônes).

S'il y a des messages d'erreur, veuillez consulter [cette FAQ](#user-content-faq22).

Sur certains appareils, où il y a beaucoup d'applications en concurrence pour la mémoire, Android peut arrêter le service de synchronisation en dernier recours.

Certaines versions d'Android arrêtes les applications et services trop brutalement. Voir [ ce site](https://dontkillmyapp.com/) et [ce ticket Android](https://issuetracker.google.com/issues/122098785) pour plus d'informations.

Désactiver l'optimisation de la batterie (mis en place à l'étape 3) réduit les chances qu'Android stoppe le service de synchronisation.

Dans le cas d'erreurs de connexion successives, FairEmail attendra de plus en plus longtemps entre pour réduire l'usage de batterie. Voir [cette FAQ](#user-content-faq123).

<br />

<a name="faq17"></a>
**~~(17) Pourquoi la synchronisation manuelle ne fonctionne-t-elle pas ?~~**

~~Si le menu *Synchroniser maintenant* est grisé, il n'y a pas de connexion au compte.~~

~~Voir la question précédente pour plus de renseignements.~~

<br />

<a name="faq18"></a>
**(18) Pourquoi la prévisualisation du message n'est-elle pas toujours affichée ?**

L'aperçu du texte du message ne peut pas être affiché si le corps du message n'a pas encore été téléchargé. Voir aussi [cette FAQ](#user-content-faq15).

<br />

<a name="faq19"></a>
**(19) Pourquoi les fonctionnalités pro sont-elles si chères ?**

Tout d'abord, **FairEmail est essentiellement gratuit** et seules quelques fonctionnalités avancées doivent être achetées.

Tout d'abord, ** FairEmail est essentiellement gratuit ** et seules certaines fonctionnalités avancées doivent être achetées.

Tout d'abord, **FairEmail est fondamentalement gratuit** et seules certaines fonctionnalités avancées doivent être achetées.

Veuillez consulter la description du Play Store de l'application ou [voir ici](https://email.faircode.eu/#pro) pour une liste complète des fonctionnalités pro.

La bonne question est "*pourquoi y a-t-il autant de taxes et de frais ?*":

* TVA : 25 % (selon votre pays)
* Frais Google : 30 %
* Impôt sur le revenu: 50 %
* <sub>Frais Paypal : 5-10 % en fonction du pays/montant</sub>

Donc, ce qui reste pour le développeur n'est qu'une fraction de ce que vous payez.

Notez également que la plupart des applications gratuites ne dureront probablement pas dans le temps, alors que FairEmail est correctement maintenue et soutenu, et que les applications gratuites peuvent avoir des pièges, comme envoyer des informations confidentielles sur Internet. Il n'y a pas de pub qui viole la vie privée dans l'application.

Je travaillé sur FairEmail presque tous les jours depuis plus de deux ans, je pense donc que le prix est plus que raisonnable. Pour cette raison, il n'y aura pas non plus de réductions.

<br />

<a name="faq20"></a>
**(20) Puis-je obtenir un remboursement ?**

Si une fonctionnalité pro ne marche pas comme prévu et que ce n'est pas causé par un problème dans les fonctionnalités gratuites et que je ne peux résoudre ce problème rapidement, vous pouvez obtenir un remboursement. Autrement, il n'y a pas de possibilité de remboursement. En aucun cas il n'y a de remboursement possible pour tout problème lié aux fonctionnalités gratuites, étant donné que rien a été payé pour les obtenir et parce qu'elles peuvent être évaluées sans aucune limitation. Je prends ma responsabilité en tant que vendeur de délivrer ce qui a été promis et je pars du principe que vous prenez la responsabilité de vous informer de ce que vous achetez.

<a name="faq21"></a>
**(21) Comment activer le voyant de notification ?**

Avant Android 8 Oreo : il y a une option avancée dans les paramètres de notification de l'application pour cela.

Android 8 Oreo et plus tard : veuillez voir [ici](https://developer.android.com/training/notify-user/channels) comment configurer les canaux de notification. Vous pouvez utiliser le bouton *Canal par défaut* dans les paramètres de notification de l'application pour aller directement dans les paramètres du canal de notification Android.

Notez que les applications ne peuvent pas modifier les paramètres de notification, y compris les paramètres de lumière de notification, sur Android 8 Oreo et après.

Parfois, il est nécessaire de désactiver le paramètre *Afficher l'aperçu du message dans les notifications* ou d'activer les paramètres *Afficher les notifications avec un texte d'aperçu seulement* pour contourner les bugs dans Android. Cela peut également s'appliquer aux sons de notification et aux vibrations.

Définir une couleur de lumière avant Android 8 n'est pas pris en charge et n'est pas possible sur Android 8 et après.

<br />

<a name="faq22"></a>
**(22) Que signifie l'erreur de compte/dossier ... ?**

FairEmail ne masque pas les erreurs comme les applications similaires le font souvent, il est donc plus facile de diagnostiquer les problèmes.

FairEmail essaiera automatiquement de se reconnecter après un délai. Ce délai sera doublé après chaque tentative échouée pour éviter de drainer la batterie et pour éviter d'être bloquée définitivement. Veuillez consulter [cette FAQ](#user-content-faq123) pour plus d'informations à ce sujet.

Il y a des erreurs générales et des erreurs spécifiques aux comptes Gmail (voir ci-dessous).

**Erreurs générales**

<a name="authfailed"></a>
L'erreur *... **L'authentification a échoué** ...* ou *... CERTIFICATION a échoué...* signifie probablement que votre nom d'utilisateur ou votre mot de passe était incorrect. Certains fournisseurs attendent comme nom d'utilisateur seulement *nom d'utilisateur* et d'autres votre adresse e-mail complète *nom d'utilisateur@exemple.com*. Lors de la copier/coller pour entrer un nom d'utilisateur ou un mot de passe, des caractères invisibles peuvent être copiés, ce qui peut également causer ce problème. Certains gestionnaires de mots de passe dont connus de le faire d'une manière incorrecte. Le nom d'utilisateur peut être sensible à la casse, donc essayez uniquement les minuscules. Le mot de passe est presque toujours sensible à la casse. Certains fournisseurs nécessitent l'utilisation d'un mot de passe d'application au lieu du mot de passe du compte, donc veuillez consulter la documentation du fournisseur. Parfois, il est nécessaire d'activer l'accès externe (IMAP/SMTP) sur le site Web du fournisseur d'abord. D'autres causes possibles sont que le compte est bloqué ou que la connexion a été restreinte administrativement d'une certaine manière, par exemple en permettant de se connecter à partir de certains réseaux / adresses IP seulement.

Si nécessaire, vous pouvez mettre à jour un mot de passe dans les paramètres du compte : menu de navigation (menu latéral), appuyez sur *Paramètres*, appuyez sur *Configuration manuelle*, appuyez sur *Comptes* et appuyez sur le compte. Le changement du mot de passe du compte changera automatiquement, dans la plupart des cas, le mot de passe des identités associées. Si le compte a été autorisé avec OAuth via l'assistant de configuration rapide au lieu d'un mot de passe, vous pouvez relancer l'assistant de configuration rapide et cocher *Autorisez à nouveau le compte existant* pour l'authentifier à nouveau. Notez que cela nécessite une version récente de l'application.

L'erreur *... Trop de mauvaises tentatives d'authentification...* signifie probablement que vous utilisez un mot de passe de compte Yahoo au lieu d'un mot de passe de l'application. Veuillez consulter [cette FAQ](#user-content-faq88) sur la façon de créer un compte Yahoo.

Le message *... +OK ...* signifie probablement qu'un port POP3 (généralement le numéro de port 995) est utilisé pour un compte IMAP (généralement le numéro de port 993).

Les erreurs *... salutation non valide ...*, *... nécessite une adresse valide ...* et *... Le paramètre vers HELO n'est pas conforme à la syntaxe RFC...* peut probablement être résolu en changeant le paramètre d'identité avancé *Utiliser une adresse IP locale au lieu du nom d'hôte*.

L'erreur *... Impossible de se connecter à l'hôte...* signifie qu'il n'y a pas eu de réponse du serveur de messagerie dans un délai raisonnable (20 secondes par défaut). Cela indique généralement des problèmes de connectivité Internet, éventuellement causés par un VPN ou par une application pare-feu. Vous pouvez essayer d'augmenter le délai de connexion dans les paramètres de connexion de FairEmail, pour les cas de serveur de messagerie vraiment lent.

L'erreur *... Connexion refusée...* signifie que le serveur de messagerie ou quelque chose entre le serveur de messagerie et l'application, comme un pare-feu, a activement refusé la connexion.

L'erreur *... Réseau injoignable...* signifie que le serveur de messagerie n'a pas été joignable via la connexion internet actuelle, par exemple parce que le trafic Internet est limité au trafic local seulement.

L'erreur *... L'hôte n'est pas résolu ...*, *... Impossible de résoudre l'hôte ...* ou *... Aucune adresse associée au nom d'hôte ...* signifie que l'adresse du serveur de messagerie n'a pas pu être résolue en une adresse IP. Cela peut être causé par un VPN, un [DNS](https://en.wikipedia.org/wiki/Domain_Name_System) (local) bloqueur de publicités ou injoignable ou pas fonctionnant correctement.

L'erreur *... Arrêt de la connexion dû à un logiciel...* signifie que le serveur de messagerie ou quelque chose entre FairEmail et le serveur de messagerie a mis fin activement à une connexion existante. Cela peut se produire par exemple lorsque la connectivité a été brusquement perdue. Un exemple typique est d'activer le mode avion.

Les erreurs *... BYE Déconnexion ...*, *... Connexion refusée...* signifie que le serveur de messagerie ou quelque chose entre le serveur de messagerie et l'application, comme un pare-feu, a activement refusé la connexion.

L'erreur *... Connexion fermée par le pair ...* peut être causée par un serveur Exchange non mis à jour, voir [ici](https://blogs.technet.microsoft.com/pki/2010/09/30/sha2-and-windows/) pour plus d'informations.

L'erreur *... Erreur de lecture...*, *... Erreur d'écriture ...*, *... La lecture a expiré ...*, *... Broken pipe ...* signifie que le serveur de messagerie ne répond plus ou que la connexion internet est mauvaise.

<a name="connectiondropped"></a>
L'erreur *... Connexion interrompue par le serveur? ...* signifie que le serveur de messagerie a interrompu la connexion de façon inattendue. Cela se produit lorsqu'il y a trop de tentatives de connexion dans un court laps de temps ou quand un mauvais mot de passe est utilisé trop de fois. Vérifiez que votre mot de passe est correct et désactivez la réception de message dans l'onglet "réception" des paramètres pendant 30 minutes puis réessayez. Si besoin, reportez-vous [à cette FAQ](#user-content-faq23) sur comment réduire le nombre de connexions.

L'erreur *... Fin inattendue du flux d'entrée zlib...* signifie que toutes les données n'ont pas été reçues, peut-être en raison d'une mauvaise connexion ou d'une interruption de la connexion.

L'erreur *...échec connection  ...* pourrait indiquer [Trop de connexions simultanées](#user-content-faq23).

L'avertissement *... Encodage non supporté ...* signifie que le jeu de caractères du message est inconnu ou non pris en charge. FairEmail se basera sur la norme ISO-8859-1 (Latin1), ce qui permettra dans la plupart des cas d'afficher le message correctement.

L'erreur *... Taux de connexion Limite d'accès ...* signifie qu'il y a eu trop de tentatives de connexion avec un mot de passe incorrect. Veuillez vérifier votre mot de passe ou authentifier à nouveau le compte avec l'assistant de configuration rapide (OAuth uniquement).

Veuillez [voir ici](#user-content-faq4) pour les erreurs *... Non fiable ... pas dans le certificat ... *, *... Certificat de sécurité non valide (Impossible de vérifier l'identité du serveur) ...* ou *... Ancre de confiance pour le chemin de certification introuvable...*

Veuillez [voir ici](#user-content-faq127) pour l'erreur *... Syntaxe de(s) argument(s) HELO invalide...*.

Veuillez [voir ici](#user-content-faq41) pour l'erreur *... Echec de l'établissement d'une liaison...*.

Voir [ici](https://linux.die.net/man/3/connect) pour les explications des codes d'erreur comme EHOSTUNREACH et ETIMEDOUT.

Les causes possibles sont:

* Un pare-feu ou un routeur bloque les connexions au serveur
* Le nom d'hôte ou le numéro de port est invalide
* Il y a des problèmes avec la connexion Internet
* Il y a des problèmes avec la résolution des noms de domaine (Yandex : essayez de désactiver le DNS privé dans les paramètres Android)
* Le serveur de messagerie refuse d'accepter des connexions (externes)
* Le serveur de messagerie refuse d'accepter un message, par exemple parce qu'il est trop grand ou qu'il contient des liens inacceptables
* Il y a trop de connexions au serveur, voir aussi la question suivante

De nombreux réseaux Wi-Fi publics bloquent les courriels sortants pour empêcher les spams. Parfois, vous pouvez contourner cela en utilisant un autre port SMTP. Voir la documentation du fournisseur d'accès pour les numéros de port utilisables.

Si vous utilisez un [VPN](https://en.wikipedia.org/wiki/Virtual_private_network), le fournisseur de VPN peut bloquer la connexion car il essaye de prévenir le spam de manière trop agressive. Notez que [Google Fi](https://fi.google.com/) utilise également un VPN.

**Erreur d'envoi**

Les serveurs SMTP peuvent rejeter les messages pour [une variété de raisons](https://en.wikipedia.org/wiki/List_of_SMTP_server_return_codes). Les messages trop volumineux et le déclenchement du filtre de spam d'un serveur de messagerie sont les raisons les plus courantes.

* La limite de taille de la pièce jointe pour Gmail [est de 25 Mo](https://support.google.com/mail/answer/6584)
* La limite de taille des pièces jointes pour Outlook et Office 365 [est de 20 Mo](https://support.microsoft.com/en-us/help/2813269/attachment-size-exceeds-the-allowable-limit-error-when-you-add-a-large)
* La limite de taille de la pièce jointe pour Yahoo [est de 25 Mo](https://help.yahoo.com/kb/SLN5673.html)
* *554 Service 5.7.1 indisponible ; Hôte client xxx.xxx.xxx.xxx bloqué*, [voir ici](https://docs.gandi.net/en/gandimail/faq/error_types/554_5_7_1_service_unavailable.html)
* *501 Erreur de syntaxe - ligne trop longue* est souvent provoquée par l'utilisation d'un en-tête Autocrypt long
* *503 5.5.0 Recipient already specified* signifie généralement qu'une adresse est utilisée à la fois comme adresse de destinataire et en copie
* *554 5.7.1 ... not permitted to relay* signifie que le serveur de messagerie ne reconnaît pas le nom d'utilisateur/adresse e-mail. Veuillez vérifier le nom d’hôte et le nom d’utilisateur/adresse e-mail dans les paramètres d’identité.
* *550 Spam message rejeté parce que l'adresse IP est listée par ...* signifie que le serveur de messagerie a rejeté d'envoyer un message à partir de l'adresse réseau actuelle (publique) car il a été utilisé à mauvais escient pour envoyer du spam par (espérons-le) quelqu'un d'autre avant. Veuillez essayer d'activer le mode de vol pendant 10 minutes pour acquérir une nouvelle adresse réseau.
* *550 Nous sommes désolés, mais nous ne pouvons pas envoyer votre email. Soit l'objet, un lien ou une pièce jointe contiennent potentiellement de l'indésirable, du hameçonnage ou un logiciel malveillant.* signifie que le fournisseur de messagerie considère qu'un message sortant est dangereux.
* *571 5.7.1 Le message contient du spam ou du virus ou de l'expéditeur est bloqué...* signifie que le serveur de messagerie considéré comme un message sortant comme du spam. Cela signifie probablement que les filtres de spam du serveur de messagerie sont trop stricts. Vous devrez contacter le fournisseur de messagerie pour obtenir de l'aide.
* *Erreur de serveur temporaire 451 4.7.0. Veuillez réessayer plus tard. PRX4 ...* : veuillez [voir ici](https://www.limilabs.com/blog/office365-temporary-server-error-please-try-again-later-prx4) ou [voir ici](https://judeperera.wordpress.com/2019/10/11/fixing-451-4-7-0-temporary-server-error-please-try-again-later-prx4/).
* *571 5.7.1 Relay access denied*: veuillez vérifier le nom d'utilisateur et l'adresse courriel dans les paramètres avancés d'identité (via la configuration manuelle).

Si vous voulez utiliser le serveur SMTP Gmail pour contourner un filtre de spam sortant trop strict ou pour améliorer la livraison des messages :

* Vérifiez votre adresse e-mail [ici](https://mail.google.com/mail/u/0/#settings/accounts) (vous devrez utiliser un navigateur de bureau pour cela)
* Modifiez les paramètres d'identité comme ceci (Paramètres, appuyez sur Configuration manuelle, appuyez sur Identités, appuyez sur l'identité) :

&emsp;&emsp;Nom d'utilisateur : *votre adresse Gmail*<br /> &emsp;&emsp;Mot de passe : *[un mot de passe de l'application](#user-content-faq6)*<br /> &emsp;&emsp;Hôte : *smtp.gmail.com*<br /> &emsp;&emsp;Port : *465*<br /> &emsp;&emsp;Chiffrement : *SSL/TLS*<br /> &emsp;&emsp;Répondre à l'adresse : *votre adresse e-mail* (paramètres d'identité avancés)<br />

<br />

**Erreurs Gmail**

L'autorisation de la configuration des comptes Gmail avec l'assistant rapide doit être périodiquement actualisée via le [gestionnaire de comptes Android](https://developer.android.com/reference/android/accounts/AccountManager). Cela nécessite des autorisations de contact/compte et une connexion internet.

En cas d'erreur, il est possible d'autoriser/restaurer un compte Gmail à nouveau via l'assistant de configuration rapide de Gmail.

L'erreur *... Échec de l'authentification ... Compte introuvable ...* signifie qu'un compte Gmail précédemment autorisé a été supprimé de l'appareil.

L'erreur *... Échec de l'authentification ... Aucun jeton ...* signifie que le gestionnaire de comptes Android n'a pas réussi à actualiser l'autorisation d'un compte Gmail.

L'erreur *... Échec de l'authentification... erreur de réseau...* signifie que le gestionnaire de compte Android n'a pas pu actualiser l'autorisation d'un compte Gmail en raison de problèmes de connexion internet

L'erreur *... Échec de l'authentification ... Identifiants invalides ...* peuvent être causés par la modification du mot de passe du compte ou par la révocation des autorisations de compte/contacts requises. Si le mot de passe du compte a été modifié, vous devrez authentifier le compte Google dans les paramètres du compte Android à nouveau. Si les autorisations ont été révoquées, vous pouvez démarrer l'assistant d'installation rapide de Gmail pour accorder à nouveau les autorisations requises (vous n'avez pas besoin de reconfigurer le compte).

L'erreur *... ServiceDésactivé...* peut être causé par l'inscription au [Programme de Protection Avancée](https://landing.google.com/advancedprotection/): "*Pour lire votre e-mail, vous pouvez (devez) utiliser Gmail - Vous ne serez pas en mesure d'utiliser votre compte Google avec certaines (toutes) apps & services qui nécessitent un accès à des données sensibles comme vos e-mails*", voir [ici](https://support.google.com/accounts/answer/7519408?hl=en&ref_topic=9264881).

En cas de doute, vous pouvez demander le support [](#user-content-support).

<br />

<a name="faq23"></a>
**(23) Pourquoi est-ce que je reçois une alerte ?**

*Généralités*

Les alertes sont des messages d'avertissement envoyés par les serveurs de messagerie.

*Trop de connexions simultanées* ou *Nombre maximal de connexions dépassées*

Cette alerte sera envoyée quand il y a trop de connexions de dossier pour le même compte de messagerie simultanément.

Les causes possibles sont:

* Il y a plusieurs clients de messagerie connectés au même compte
* Le même client de messagerie est connecté plusieurs fois au même compte
* Les connexions précédentes ont été interrompues brusquement, par exemple en perdant brusquement la connectivité Internet.

Tout d'abord, essayez d'attendre un peu de temps pour voir si le problème se résout par lui-même, sinon:

* soit vous basculez vers la vérification périodique des messages dans les paramètres de réception, ce qui entraînera l'ouverture des dossiers un à la fois
* ou vous définissez certains dossiers à interroger au lieu de les synchroniser (appuyez longuement sur le dossier dans la liste des dossiers, modifiez les propriétés)

Un moyen facile de configurer la vérification périodique des messages pour tous les dossiers sauf la boîte de réception est d'utiliser *Appliquer à tous…* dans le menu à trois points de la liste des dossiers et de cocher les deux cases à cocher de la section Avancé.

Le nombre maximum de connexions simultanées de dossiers pour Gmail est de 15, donc vous pouvez synchroniser au maximum 15 dossiers simultanément sur *tous* vos appareils en même temps. C'est pour cette raison que les dossiers *utilisateur* Gmail sont réglés par défaut sur interroger au lieu de toujours synchroniser. Si nécessaire ou désiré, vous pouvez modifier cela en appuyant longuement sur un dossier dans la liste des dossiers et en sélectionnant *Modifier les propriétés*. Voir [ici](https://support.google.com/mail/answer/7126229) pour les détails.

Lors de l'utilisation d'un serveur Dovecot, vous pouvez modifier le paramètre [mail_max_userip_connections](https://doc.dovecot.org/settings/dovecot_core_settings/#mail-max-userip-connections).

Notez qu'il faudra un certain temps au serveur de messagerie pour découvrir des connexions cassées, par exemple si vous perdez la connexion au réseau, ce qui signifie que, de fait, la moitié seulement des connexions aux dossiers sont disponibles. Pour Gmail, ce ne sont que 7 connexions.

<br />

<a name="faq24"></a>
**(24) Qu'est-ce que parcourir les messages sur le serveur ?**

Parcourir les messages sur le serveur récupérera les messages du serveur de messagerie en temps réel lorsque vous atteindrez la fin de la liste des messages synchronisés, même lorsque le dossier est configuré pour ne pas synchroniser. Vous pouvez désactiver cette fonctionnalité dans les paramètres avancés du compte.

<br />

<a name="faq25"></a>
**(25) Pourquoi ne puis-je pas sélectionner/ouvrir/sauvegarder une image, une pièce jointe ou un fichier ?**

Lorsqu'un élément de menu pour sélectionner/ouvrir/sauvegarder un fichier est désactivé (grisé) ou lorsque vous recevez le message *Storage access framework non disponible*, le [Storage access framework](https://developer.android.com/guide/topics/providers/document-provider) (la structure d'accès au stockage), un composant Android standard, n'est probablement pas présent. Cela peut être dû au fait que votre ROM personnalisée ne l'inclut pas ou qu'elle a été activement supprimée (dégonflée).

FairEmail ne demande pas d'autorisations de stockage, donc ce framework est requis pour sélectionner des fichiers et des dossiers. Aucune application, sauf peut-être les gestionnaires de fichiers fonctionnant sous Android 4.4 KitKat ou une version ultérieure, ne devrait demander des autorisations de stockage car cela permettrait d'accéder à *tous* les fichiers.

Le Storage access framework est fourni par le paquet *com.android.documentsui* qui est visible comme une application *Fichiers* sur certaines versions d'Android (notamment OxygenOS).

Vous pouvez (ré)activer le Storage access framework avec cette commande adb :

```
pm install -k --user 0 com.android.documentsui
```

Ou bien, vous devriez être en mesure d'activer à nouveau l'application *Fichiers* en utilisant l'application Paramètres d'Android.

<br />

<a name="faq26"></a>
**(26) Puis-je aider à traduire FairEmail dans ma propre langue ?**

Oui, vous pouvez traduire les textes de FairEmail dans votre propre langue [sur Crowdin](https://crowdin.com/project/open-source-email). L'inscription est gratuite.

Si vous souhaitez que votre nom ou votre alias soit inclus dans la liste des contributeurs dans *À propos* de l'application, s'il vous plaît [contactez-moi](https://contact.faircode.eu/?product=fairemailsupport).

<br />

<a name="faq27"></a>
**(27) Comment puis-je distinguer les images intégrées et externes ?**

Image externe:

![Image externe](https://github.com/M66B/FairEmail/blob/master/images/baseline_image_black_48dp.png)

Image intégrée:

![Image intégrée](https://github.com/M66B/FairEmail/blob/master/images/baseline_photo_library_black_48dp.png)

Image corrompue:

![Image brisée](https://github.com/M66B/FairEmail/blob/master/images/baseline_broken_image_black_48dp.png)

Notez que le téléchargement d'images externes à partir d'un serveur distant peut être utilisé pour enregistrer que vous avez vu le message ce que vous ne voudrez probablement pas si le message est indésirable ou malveillant.

<br />

<a name="faq28"></a>
**(28) Comment gérer les notifications dans la barre d'état ?**

Dans les paramètres de notification, vous trouverez un bouton *Gérer les notifications* pour naviguer directement dans les paramètres de notifications Android pour FairEmail.

Sur Android 8.0 Oreo et ultérieurs, vous pouvez gérer les propriétés des canaux de notification individuels, par exemple pour définir un son de notification spécifique ou pour afficher des notifications sur l'écran de verrouillage.

FairEmail dispose des canaux de notification suivants :

* Service : utilisé pour la notification du service de synchronisation, voir aussi [cette FAQ](#user-content-faq2)
* Envoyer: utilisé pour la notification du service d'envoi
* Notifications: utilisé pour les notifications de nouveaux messages
* Avertissement: utilisé pour les notifications d'avertissement
* Erreur: utilisé pour les notifications d'erreur

Voir [ici](https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels) pour plus de détails sur les canaux de notification. En bref: appuyez sur le nom du canal de notification pour accéder aux paramètres du canal.

Sur Android avant Android 8 Oreo vous pouvez définir le son de notification dans les paramètres.

Consultez [cette FAQ](#user-content-faq21) si votre appareil a un voyant de notification.

<br />

<a name="faq29"></a>
(29) Comment puis-je recevoir des notifications de nouveaux messages pour d'autres dossiers ?

Appuyez longuement sur un dossier, sélectionnez *Modifier les propriétés*, et activez soit *Afficher dans la boîte de réception unifiée* soit *Notifier les nouveaux messages* (disponible sur Android 7 Nougat et versions suivantes seulement) puis appuyez sur *Enregistrer*.

<br />

<a name="faq30"></a>
**(30) Comment puis-je utiliser les paramètres rapides fournis ?**

Il y a des paramètres rapides (tuiles de paramètres) disponibles à :

* activer/désactiver globalement la synchronisation
* afficher le nombre de nouveaux messages et les marquer comme vus (non lu)

Les paramètres rapides nécessitent Android 7.0 Nougat ou supérieur. L'utilisation des tuiles de paramètres est expliquée [ici](https://support.google.com/android/answer/9083864).

<br />

<a name="faq31"></a>
**(31) Comment puis-je utiliser les raccourcis fournis ?**

Il y a des raccourcis disponibles pour écrire un nouveau message à un contact favori.

Les raccourcis requièrent Android 7.1 Nougat ou supérieur. L'utilisation des raccourcis est expliquée [ici](https://support.google.com/android/answer/2781850).

Il est également possible de créer des raccourcis vers des dossiers en appuyant longuement sur un dossier dans la liste des dossiers d'un compte et en sélectionnant *Ajouter un raccourci*.

<br />

<a name="faq32"></a>
**(32) Comment puis-je vérifier si la lecture des e-mails est vraiment sécurisée ?**

Vous pouvez utiliser le [Testeur de confidentialité des courriels](https://www.emailprivacytester.com/) pour cela.

<br />

<a name="faq33"></a>
**(33) Pourquoi les adresses de l'expéditeur modifiées ne fonctionnent pas ?**

La plupart des fournisseurs n'acceptent les adresses validées que lors de l'envoi de messages pour éviter les spams.

Par exemple, Google modifie les en-têtes de message comme celui-ci pour les adresses *non vérifiées*:

```
De: Quelqu'un <somebody@example.org>
X-Google-Original-From: Quelqu'un <somebody+extra@example.org>
```

Cela signifie que l'adresse de l'expéditeur modifiée a été automatiquement remplacée par une adresse vérifiée avant d'envoyer le message.

Notez que cela est indépendant de la réception de messages.

<br />

<a name="faq34"></a>
**(34) Comment les identités sont-elles correspondantes ?**

Les identités sont comme attendues assorties par compte. Pour les messages entrants, les adresses *à*, *cc*, *cci*, *de* et *(X-)livrés/enveloppe/original-to* seront vérifiées (dans cet ordre) et pour les messages sortants (brouillons, boîte d'envoi et envoyés) seulement les adresses *de* seront vérifiées. Les adresses identiques ont la priorité sur les adresses partiellement correspondantes, à l'exception des adresses *delivered-to*.

L'adresse correspondante sera affichée comme *via* dans la section des adresses des messages reçus (entre l'en-tête du message et le corps du message).

Notez que les identités doivent être activées pour pouvoir être associées et que les identités des autres comptes ne seront pas prises en compte.

La correspondance ne sera effectuée qu'une fois à la réception d'un message, de sorte que la modification de la configuration ne changera pas les messages existants. Vous pouvez effacer les messages locaux en appuyant longuement sur un dossier dans la liste des dossiers et en synchronisant les messages à nouveau.

Il est possible de configurer un [regex](https://en.wikipedia.org/wiki/Regular_expression) (NdT : une expression régulière) dans les paramètres d'identité pour faire correspondre **le nom d'utilisateur** d'une adresse e-mail (la partie avant le signe @).

Notez que le nom de domaine (la partie après le signe @) doit toujours être identique au nom de domaine de l'identité.

If you like to match a catch-all email address, this regex is mostly okay:

```
.*
```

If you like to match the special purpose email addresses abc@example.com and xyx@example.com and like to have a fallback email address main@example.com as well, you could do something like this:

* Identité : abc@exemple.com; regex: **(?i)abc**
* Identité : xyz@exemple.com; regex: **(?i)xyz**
* Identité : contact@exemple.com; regex: **^(?i)((?!abc|xyz).)\*$**

Les identités correspondantes peuvent être utilisées pour attribuer des couleurs aux messages. Les couleurs d’identité ont la priorité sur les couleurs de dossiers et de comptes. Définir les couleurs d'identité est une fonctionnalité pro.

<br />

<a name="faq35"></a>
**(35) Pourquoi devrais-je faire attention à la visualisation des images, des pièces jointes, du message original et à l'ouverture des liens ?**

Visualiser les images stockées à distance (voir aussi [cette FAQ](#user-content-faq27)) et ouvrir les liens pourrait non seulement indiquer à l'expéditeur que vous avez vu le message, mais dévoilera également votre adresse IP. Voir aussi cette question : [Pourquoi un lien depuis un courrier électronique est-il plus dangereux qu'un lien d'une recherche sur le web ?](https://security.stackexchange.com/questions/241139/why-emails-link-is-more-dangerous-than-web-searchs-link).

Ouvrir des pièces jointes ou afficher un message original peut charger du contenu distant et exécuter des scripts, qui pourraient non seulement causer des fuites d'informations sensibles sur la vie privée, mais peuvent aussi représenter un risque de sécurité.

Note that your contacts could unknowingly send malicious messages if they got infected with malware.

FairEmail formats messages again causing messages to look different from the original, but also uncovering phishing links.

Note that reformatted messages are often better readable than original messages because the margins are removed, and font colors and sizes are standardized.

L'application Gmail affiche par défaut les images en les téléchargeant via un serveur proxy Google. Since the images are downloaded from the source server [in real-time](https://blog.filippo.io/how-the-new-gmail-image-proxy-works-and-what-this-means-for-you/), this is even less secure because Google is involved too without providing much benefit.

You can show images and original messages by default for trusted senders on a case-by-case basis by checking *Do not ask this again for ...*.

If you want to reset the default *Open with* apps, please [see here](https://www.androidauthority.com/how-to-set-default-apps-android-clear-621269/).

<br />

<a name="faq36"></a>
**(36) How are settings files encrypted?**

Short version: AES 256 bit

Long version:

* The 256 bit key is generated with *PBKDF2WithHmacSHA1* using a 128 bit secure random salt and 65536 iterations
* The cipher is *AES/CBC/PKCS5Padding*

<br />

<a name="faq37"></a>
**(37) Comment les mots de passe sont-ils stockés ?**

Toutes les versions Android supportées [chiffrent toutes les données utilisateur](https://source.android.com/security/encryption), donc toutes les données, y compris les noms d'utilisateurs, mots de passe, messages, etc. sont stockées chiffrées.

Si l'appareil est sécurisé à l'aide d'un code PIN, d'un schéma ou d'un mot de passe, vous pouvez rendre visible le compte et les mots de passe d'identité. Si c'est un problème parce que vous partagez l'appareil avec d'autres personnes, envisagez d'utiliser des [profils d'utilisateurs](https://www.howtogeek.com/333484/how-to-set-up-multiple-user-profiles-on-android/).

<br />

<a name="faq39"></a>
**(39) Comment puis-je réduire l'utilisation de la batterie par FairEmail ?**

Recent Android versions by default report *app usage* as a percentage in the Android battery settings screen. **Confusingly, *app usage* is not the same as *battery usage* and is not even directly related to battery usage!** The app usage (while in use) will be very high because FairEmail is using a foreground service which is considered as constant app usage by Android. However, this doesn't mean that FairEmail is constantly using battery power. The real battery usage can be seen by navigating to this screen:

*Android settings*, *Battery*, three-dots menu *Battery usage*, three-dots menu *Show full device usage*

As a rule of thumb the battery usage should be below or in any case not be much higher than *Mobile network standby*. If this isn't the case, please turn on *Auto optimize* in the receive settings. If this doesn't help, please [ask for support](https://contact.faircode.eu/?product=fairemailsupport).

It is inevitable that synchronizing messages will use battery power because it requires network access and accessing the messages database.

If you are comparing the battery usage of FairEmail with another email client, please make sure the other email client is setup similarly. For example comparing always sync (push messages) and (infrequent) periodic checking for new messages is not a fair comparison.

Reconnecting to an email server will use extra battery power, so an unstable internet connection will result in extra battery usage. Also, some email servers prematurely terminate idle connections, while [the standard](https://tools.ietf.org/html/rfc2177) says that an idle connection should be kept open for 29 minutes. In these cases you might want to synchronize periodically, for example each hour, instead of continuously. Note that polling frequently (more than every 30-60 minutes) will likely use more battery power than synchronizing always because connecting to the server and comparing the local and remote messages are expensive operations.

[On some devices](https://dontkillmyapp.com/) it is necessary to *disable* battery optimizations (setup step 3) to keep connections to email servers open. In fact, leaving battery optimizations enabled can result in extra battery usage for all devices, even though this sounds contradictory!

Most of the battery usage, not considering viewing messages, is due to synchronization (receiving and sending) of messages. So, to reduce the battery usage, set the number of days to synchronize message for to a lower value, especially if there are a lot of recent messages in a folder. Long press a folder name in the folders list and select *Edit properties* to access this setting.

Si vous avez au moins une fois par jour une connexion Internet, il suffit de synchroniser les messages pour une seule journée.

Note that you can set the number of days to *keep* messages for to a higher number than to *synchronize* messages for. Vous pourriez par exemple initialement synchroniser les messages pendant un grand nombre de jours et après que cela ait été terminé réduire le nombre de jours pour synchroniser les messages, mais laissez le nombre de jours pour garder les messages. Après avoir réduit le nombre de jours pour conserver les messages, vous pouvez exécuter le nettoyage dans les paramètres divers pour supprimer les anciens fichiers.

Dans les paramètres de réception, vous pouvez activer la synchronisation systématique des messages marqués d'une étoile, ce qui vous permettra de conserver les anciens messages tout en synchronisant les messages pendant un nombre limité de jours.

La désactivation de l'option de dossier *Téléchargement automatique des textes et des pièces jointes des messages*. Entraînera une diminution du trafic réseau et donc de l'utilisation de la batterie. Vous pouvez désactiver cette option, par exemple pour le dossier envoyé et l'archive.

La synchronisation des messages pendant la nuit n'est généralement pas utile, vous pouvez donc économiser sur l'utilisation de la batterie en ne synchronisant pas la nuit. Dans les paramètres, vous pouvez sélectionner un calendrier pour la synchronisation des messages (ceci est une fonctionnalité pro).

FairEmail synchronisera par défaut la liste des dossiers à chaque connexion. Comme les dossiers ne sont pas créés, renommés ou supprimés très souvent, vous pouvez économiser une certaine consommation de données et de batterie en désactivant ceci dans les paramètres de réception.

FairEmail vérifiera par défaut si les anciens messages ont été supprimés du serveur à chaque connexion. Si cela ne vous dérange pas que les anciens messages qui ont été supprimés du serveur soient toujours visibles dans FairEmail, vous pouvez économiser une certaine consommation de données et de batterie en désactivant ceci dans les paramètres de réception.

Certains fournisseurs ne suivent pas la norme IMAP et ne maintiennent pas les connexions suffisamment longtemps ouvertes, forçant FairEmail à se reconnecter souvent, causant une utilisation supplémentaire de la batterie. You can inspect the *Log* via the main navigation menu to check if there are frequent reconnects (connection closed/reset, read/write error/timeout, etc). You can workaround this by lowering the keep-alive interval in the advanced account settings to for example 9 or 15 minutes. Notez que les optimisations de batterie doivent être désactivées à l'étape 3 de configuration pour garder les connexions actives.

Certains fournisseurs envoient toutes les deux minutes quelque chose comme '*toujours ici*' ce qui entraîne du trafic réseau et le reveil de votre appareil et cause une consommation supplémentaire inutile de la batterie. You can inspect the *Log* via the main navigation menu to check if your provider is doing this. If your provider is using [Dovecot](https://www.dovecot.org/) as IMAP server, you could ask your provider to change the [imap_idle_notify_interval](https://wiki.dovecot.org/Timeouts) setting to a higher value or better yet, to disable this. If your provider is not able or willing to change/disable this, you should consider to switch to periodically instead of continuous synchronization. You can change this in the receive settings.

If you got the message *This provider does not support push messages* while configuring an account, consider switching to a modern provider which supports push messages (IMAP IDLE) to reduce battery usage.

If your device has an [AMOLED](https://en.wikipedia.org/wiki/AMOLED) screen, you can save battery usage while viewing messages by switching to the black theme.

If auto optimize in the receive settings is enabled, an account will automatically be switched to periodically checking for new messages when the email server:

* Says '*Still here*' within 3 minutes
* Le serveur de messagerie ne supporte pas les messages push
* The keep-alive interval is lower than 12 minutes

In addition, the trash and spam folders will be automatically set to checking for new messages after three successive [too many simultaneous connections](#user-content-faq23) errors.

<br />

<a name="faq40"></a>
**(40) Comment puis-je réduire l'utilisation des données par FairEmail ?**

Vous pouvez réduire l'utilisation des données de la même manière que pour la réduction de l'utilisation de la batterie, voir la question précédente pour des suggestions.

Il est inévitable que des données soient utilisées lors de la synchronisation des messages.

Si la connexion au serveur de messagerie est perdue, FairEmail synchronisera à nouveau les messages pour s'assurer qu'aucun n'a été manqué. Si la connexion est instable, cela peut entraîner une utilisation supplémentaire des données. Dans ce cas, c'est une bonne idée de réduire le nombre de jours pour synchroniser les messages au minimum (voir la question précédente) ou de passer à la synchronisation périodique des messages (paramètres de réception).

Pour réduire l'utilisation des données, vous pouvez modifier ces paramètres avancés de réception :

* Vérifier si les anciens messages ont été supprimés du serveur: désactiver
* Synchroniser la liste des dossiers (partagés) : désactiver

Par défaut, FairEmail ne télécharge pas les textes et les pièces jointes de plus de 256 KiB des messages lorsqu'il y a une connexion Internet limitée (mobile ou Wi-Fi payant). Vous pouvez changer ceci dans les paramètres de connexion.

<br />

<a name="faq41"></a>
**(41) How can I fix the error 'Handshake failed' ?**

There are several possible causes, so please read to the end of this answer.

The error '*Handshake failed ... WRONG_VERSION_NUMBER ...*' might mean that you are trying to connect to an IMAP or SMTP server without an encrypted connection, typically using port 143 (IMAP) and port 25 (SMTP), or that a wrong protocol (SSL/TLS or STARTTLS) is being used.

Most providers provide encrypted connections using different ports, typically port 993 (IMAP) and port 465/587 (SMTP).

Si votre fournisseur ne prend pas en charge les connexions chiffrées, vous devriez demander que cela soit rendu possible. If this isn't an option, you could enable *Allow insecure connections* both in the advanced settings AND the account/identity settings.

See also [this FAQ](#user-content-faq4).

The error '*Handshake failed ... SSLV3_ALERT_ILLEGAL_PARAMETER ...*' is either caused by a bug in the SSL protocol implementation or by a too short DH key on the email server and can unfortunately not be fixed by FairEmail.

The error '*Handshake failed ... HANDSHAKE_FAILURE_ON_CLIENT_HELLO ...*' might be caused by the provider still using RC4, which isn't supported since [Android 7](https://developer.android.com/about/versions/nougat/android-7.0-changes.html#tls-ssl) anymore.

The error '*Handshake failed ... UNSUPPORTED_PROTOCOL or TLSV1_ALERT_PROTOCOL_VERSION ...*' might be caused by enabling hardening connections in the connection settings or by Android not supporting older protocols anymore, like SSLv3.

Android 8 Oreo and later [do not support](https://developer.android.com/about/versions/oreo/android-8.0-changes#security-all) SSLv3 anymore. There is no way to workaround lacking RC4 and SSLv3 support because it has completely been removed from Android (which should say something).

You can use [this website](https://ssl-tools.net/mailservers) or [this website](https://www.immuniweb.com/ssl/) to check for SSL/TLS problems of email servers.

<br />

<a name="faq42"></a>
**(42) Can you add a new provider to the list of providers?**

If the provider is used by more than a few people, yes, with pleasure.

Les informations suivantes sont nécessaires :

```
<provider
    name="Gmail"
    link="https://support.google.com/mail/answer/7126229" // link to the instructions of the provider
    type="com.google"> // this is not needed
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

The EFF [writes](https://www.eff.org/nl/deeplinks/2018/06/announcing-starttls-everywhere-securing-hop-hop-email-delivery): "*Additionally, even if you configure STARTTLS perfectly and use a valid certificate, there’s still no guarantee your communication will be encrypted.*"

So, pure SSL connections are safer than using [STARTTLS](https://en.wikipedia.org/wiki/Opportunistic_TLS) and therefore preferred.

Please make sure receiving and sending messages works properly before contacting me to add a provider.

See below about how to contact me.

<br />

<a name="faq43"></a>
**(43) Pouvez-vous montrer l'original ... ?**

Afficher l'original, affiche le message original comme l'expéditeur l'a envoyé, y compris les polices d'origine, les couleurs, les marges, etc. FairEmail ne modifie et ne modifiera en aucune manière le message sauf pour demander [le redimensionnement automatique du texte](https://developer.android.com/reference/android/webkit/WebSettings.LayoutAlgorithm), qui va *tenter* de rendre le texte écrit en petit plus lisible.

<br />

<a name="faq44"></a>
**~~(44) Can you show contact photos / identicons in the sent folder?~~**

~~Contact photos and identicons are always shown for the sender because this is necessary for conversation threads.~~ ~~Getting contact photos for both the sender and receiver is not really an option because getting contact photo is an expensive operation.~~

<br />

<a name="faq45"></a>
**(45) How can I fix 'This key is not available. To use it, you must import it as one of your own!' ?**

You'll get the message *This key is not available. To use it, you must import it as one of your own!* when trying to decrypt a message with a public key. To fix this you'll need to import the private key.

<br />

<a name="faq46"></a>
**(46) Why does the message list keep refreshing?**

If you see a 'spinner' at the top of the message list, the folder is still being synchronized with the remote server. You can see the progress of the synchronization in the folder list. See the legend about what the icons and numbers mean.

La vitesse de votre appareil et de la connexion Internet et le nombre de jours pour synchroniser les messages déterminent la durée de la synchronisation. Notez que vous ne devriez pas définir le nombre de jours pour synchroniser les messages à plus d'un jour dans la plupart des cas, voir aussi [cette FAQ](#user-content-faq39).

<br />

<a name="faq47"></a>
**(47) How do I solve the error 'No primary account or no drafts folder' ?**

You'll get the error message *No primary account or no drafts folder* when trying to compose a message while there is no account set to be the primary account or when there is no drafts folder selected for the primary account. This can happen for example when you start FairEmail to compose a message from another app. FairEmail needs to know where to store the draft, so you'll need to select one account to be the primary account and/or you'll need to select a drafts folder for the primary account.

This can also happen when you try to reply to a message or to forward a message from an account with no drafts folder while there is no primary account or when the primary account does not have a drafts folder.

Please see [this FAQ](#user-content-faq141) for some more information.

<br />

<a name="faq48"></a>
**~~(48) Comment puis-je résoudre l'erreur 'Aucun compte principal ou aucun dossier d'archivage' ?~~**

~~Vous obtiendrez le message d'erreur *Aucun compte principal ou aucun dossier d'archives* lors de la recherche de messages depuis une autre application. FairEmail doit savoir où chercher, donc vous devrez sélectionner un compte pour être le compte principal et/ou vous devrez sélectionner un dossier d'archive pour le compte principal.~~

<br />

<a name="faq49"></a>
**(49) How do I fix 'An outdated app sent a file path instead of a file stream' ?**

You likely selected or sent an attachment or image with an outdated file manager or an outdated app which assumes all apps still have storage permissions. For security and privacy reasons modern apps like FairEmail have no full access to all files anymore. This can result into the error message *An outdated app sent a file path instead of a file stream* if a file name instead of a file stream is being shared with FairEmail because FairEmail cannot randomly open files.

You can fix this by switching to an up-to-date file manager or an app designed for recent Android versions. Alternatively, you can grant FairEmail read access to the storage space on your device in the Android app settings. Note that this workaround [won't work on Android Q](https://developer.android.com/preview/privacy/scoped-storage) anymore.

See also [question 25](#user-content-faq25) and [what Google writes about it](https://developer.android.com/training/secure-file-sharing/share-file#RespondToRequest).

<br />

<a name="faq50"></a>
**(50) Can you add an option to synchronize all messages?**

You can synchronize more or even all messages by long pressing a folder (inbox) in the folder list of an account (tap on the account name in the navigation menu) and selecting *Synchronize more* in the popup menu.

<br />

<a name="faq51"></a>
**(51) How are folders sorted?**

Folders are first sorted on account order (by default on account name) and within an account with special, system folders on top, followed by folders set to synchronize. Within each category the folders are sorted on (display) name. You can set the display name by long pressing a folder in the folder list and selecting *Edit properties*.

The navigation (hamburger) menu item *Order folders* in the settings can be used to manually order the folders.

<br />

<a name="faq52"></a>
**(52) Why does it take some time to reconnect to an account?**

There is no reliable way to know if an account connection was terminated gracefully or forcefully. Trying to reconnect to an account while the account connection was terminated forcefully too often can result in problems like [too many simultaneous connections](#user-content-faq23) or even the account being blocked. To prevent such problems, FairEmail waits 90 seconds until trying to reconnect again.

You can long press *Settings* in the navigation menu to reconnect immediately.

<br />

<a name="faq53"></a>
**(53) Can you stick the message action bar to the top/bottom?**

The message action bar works on a single message and the bottom action bar works on all the messages in the conversation. Since there is often more than one message in a conversation, this is not possible. Moreover, there are quite some message specific actions, like forwarding.

Moving the message action bar to the bottom of the message is visually not appealing because there is already a conversation action bar at the bottom of the screen.

Note that there are not many, if any, email apps that display a conversation as a list of expandable messages. This has a lot of advantages, but the also causes the need for message specific actions.

<br />

<a name="faq54"></a>
**~~(54) How do I use a namespace prefix?~~**

~~A namespace prefix is used to automatically remove the prefix providers sometimes add to folder names.~~

~~For example the Gmail spam folder is called:~~

```
[Gmail]/Spam
```

~~By setting the namespace prefix to *[Gmail]* FairEmail will automatically remove *[Gmail]/* from all folder names.~~

<br />

<a name="faq55"></a>
**(55) Comment puis-je marquer tous les messages comme lus / déplacer ou supprimer tous les messages ?**

Vous pouvez utiliser la sélection multiple pour cela. Appuyez longuement sur le premier message, ne levez pas votre doigt et faites glisser vers le bas jusqu'au dernier message. Utilisez ensuite le bouton à trois points pour exécuter l'action souhaitée.

<br />

<a name="faq56"></a>
**(56) Pouvez-vous ajouter un support pour JMAP ?**

Il n'y a presque aucun fournisseur proposant le protocole [JMAP](https://jmap.io/) , cela ne vaut donc pas la peine de l'ajouter et de le maintenir dans FairEmail.

<br />

<a name="faq57"></a>
**(57) Puis-je utiliser du HTML dans les signatures ?**

Oui, vous pouvez utiliser du [HTML](https://en.wikipedia.org/wiki/HTML). Dans l'éditeur de signatures, vous pouvez passer en mode HTML via le menu à trois points.

Notez que si vous retournez à l'éditeur de texte, tout le HTML ne pourrait pas être rendu tel quel car l'éditeur de texte Android n'est pas en mesure de d'afficher tout le HTML. De même, si vous utilisez l'éditeur de texte, le HTML pourrait être modifié de manière inattendue.

Si vous voulez utiliser du texte préformaté, comme [l'art ASCII](https://en.wikipedia.org/wiki/ASCII_art), vous devriez envelopper ce texte dans un élément *pré* comme ceci :

```
<pre>
  |\_/|
 / @ @ \
( > º < )
 `>>x<<´
 /  O  \
 </pre>
```

<br />

<a name="faq58"></a>
**(58) What does an open/closed email icon mean?**

The email icon in the folder list can be open (outlined) or closed (solid):

![Image externe](https://github.com/M66B/FairEmail/blob/master/images/baseline_mail_outline_black_48dp.png)

Message bodies and attachments are not downloaded by default.

![Image externe](https://github.com/M66B/FairEmail/blob/master/images/baseline_email_black_48dp.png)

Message bodies and attachments are downloaded by default.

<br />

<a name="faq59"></a>
**(59) Can original messages be opened in the browser?**

For security reasons the files with the original message texts are not accessible to other apps, so this is not possible. In theory the [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) could be used to share these files, but even Google's Chrome cannot handle this.

<br />

<a name="faq60"></a>
**(60) Did you know ... ?**

* Did you know that starred messages can be synchronized/kept always? (this can be enabled in the receive settings)
* Did you know that you can long press the 'write message' icon to go to the drafts folder?
* Did you know there is an advanced option to mark messages read when they are moved? (archiving and trashing is also moving)
* Did you know that you can select text (or an email address) in any app on recent Android versions and let FairEmail search for it?
* Did you know that FairEmail has a tablet mode? Rotate your device in landscape mode and conversation threads will be opened in a second column if there is enough screen space.
* Did you know that you can long press a reply template to create a draft message from the template?
* Did you know that you can long press, hold and swipe to select a range of messages?
* Did you know that you can retry sending messages by using pull-down-to-refresh in the outbox?
* Did you know that you can swipe a conversation left or right to go to the next or previous conversation?
* Did you know that you can tap on an image to see where it will be downloaded from?
* Did you know that you can long press the folder icon in the action bar to select an account?
* Did you know that you can long press the star icon in a conversation thread to set a colored star?
* Did you know that you can open the navigation drawer by swiping from the left, even when viewing a conversation?
* Did you know that you can long press the people's icon to show/hide the CC/BCC fields and remember the visibility state for the next time?
* Did you know that you can insert the email addresses of an Android contact group via the three dots overflow menu?
* Did you know that if you select text and hit reply, only the selected text will be quoted?
* Did you know that you can long press the trash icons (both in the message and the bottom action bar) to permanently delete a message or conversation? (version 1.1368+)
* Did you know that you can long press the send action to show the send dialog, even if it was disabled?
* Did you know that you can long press the full screen icon to show the original message text only?
* Saviez-vous que vous pouvez appuyer longuement sur le bouton de réponse pour répondre à l'expéditeur? (depuis la version 1.1562)

<br />

<a name="faq61"></a>
**(61) Why are some messages shown dimmed?**

Messages shown dimmed (grayed) are locally moved messages for which the move is not confirmed by the server yet. This can happen when there is no connection to the server or the account (yet). These messages will be synchronized after a connection to the server and the account has been made or, if this never happens, will be deleted if they are too old to be synchronized.

You might need to manually synchronize the folder, for example by pulling down.

You can view these messages, but you cannot move these messages again until the previous move has been confirmed.

Pending [operations](#user-content-faq3) are shown in the operations view accessible from the main navigation menu.

<br />

<a name="faq62"></a>
**(62) Which authentication methods are supported?**

The following authentication methods are supported and used in this order:

* CRAM-MD5
* LOGIN
* PLAIN
* NTLM (untested)
* XOAUTH2 ([Gmail](https://developers.google.com/gmail/imap/xoauth2-protocol), [Yandex](https://tech.yandex.com/oauth/))

SASL authentication methods, besides CRAM-MD5, are not supported because [JavaMail for Android](https://javaee.github.io/javamail/Android) does not support SASL authentication.

If your provider requires an unsupported authentication method, you'll likely get the error message *authentication failed*.

[Client certificates](https://en.wikipedia.org/wiki/Client_certificate) can be selected in the account and identity settings.

[Server Name Indication](https://en.wikipedia.org/wiki/Server_Name_Indication) is supported by [all supported Android versions](https://developer.android.com/training/articles/security-ssl).

<br />

<a name="faq63"></a>
**(63) Comment les images sont-elles redimensionnées pour l'affichage sur les écrans ?**

Les images de grande taille, en liens ou en pièces jointes et au format [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) et [JPEG](https://en.wikipedia.org/wiki/JPEG) seront automatiquement redimensionnées pour être affichées sur les écrans. Cela est dû au fait que la taille des e-mails est limitée suivant les fournisseurs, la plupart du temps entre 10 et 50 Mo. Par défaut, les images seront redimensionnées à une largeur et une hauteur maximales d'environ 1440 pixels et enregistrées avec un ratio de compression de 90 %. Les images sont réduites à l'aide de facteurs de nombre entier pour réduire l'utilisation de la mémoire et conserver la qualité de l'image. Le redimensionnement automatique des images en ligne et/ou en pièce jointe et la taille maximale de l'image cible peuvent être configurés dans les paramètres d'envoi.

Si vous voulez redimensionner les images au cas par cas, vous pouvez utiliser [Send Reduced](https://f-droid.org/en/packages/mobi.omegacentauri.SendReduced/) ou une application similaire.

<br />

<a name="faq64"></a>
**~~(64) Can you add custom actions for swipe left/right?~~**

~~The most natural thing to do when swiping a list entry left or right is to remove the entry from the list.~~ ~~The most natural action in the context of an email app is moving the message out of the folder to another folder.~~ ~~You can select the folder to move to in the account settings.~~

~~Other actions, like marking messages read and snoozing messages are available via multiple selection.~~ ~~You can long press a message to start multiple selection. See also [this question](#user-content-faq55).~~

~~Swiping left or right to mark a message read or unread is unnatural because the message first goes away and later comes back in a different shape.~~ ~~Note that there is an advanced option to mark messages automatically read on moving,~~ ~~which is in most cases a perfect replacement for the sequence mark read and move to some folder.~~ ~~You can also mark messages read from new message notifications.~~

~~If you want to read a message later, you can hide it until a specific time by using the *snooze* menu.~~

<br />

<a name="faq65"></a>
**(65) Why are some attachments shown dimmed?**

Inline (image) attachments are shown dimmed. [Inline attachments](https://tools.ietf.org/html/rfc2183) are supposed to be downloaded and shown automatically, but since FairEmail doesn't always download attachments automatically, see also [this FAQ](#user-content-faq40), FairEmail shows all attachment types. To distinguish inline and regular attachments, inline attachments are shown dimmed.

<br />

<a name="faq66"></a>
**(66) FairEmail est-il disponible dans la bibliothèque familiale Google Play ?**

*Vous ne pouvez pas partager des achats dans-l'ppli et des applications gratuites avec les membres de votre famille.*

Voir [ici](https://support.google.com/googleone/answer/7007852) sous "*Voir si le contenu est admissible à être ajouté à la bibliothèque familiale*",*Appli & jeux*".

<br />

<a name="faq67"></a>
**(67) Comment puis-je répéter les conversations ?**

Sélectionnez plusieurs conversations (appui long pour démarrer la sélection multiple), appuyez sur le bouton à trois points et sélectionnez *Rappel ...*. Alternatively, in the expanded message view use *Snooze ...* in the message three-dots 'more' menu or the time-lapse action in the bottom action bar. Select the time the conversation(s) should snooze and confirm by tapping OK. The conversations will be hidden for the selected time and shown again afterwards. You will receive a new message notification as reminder.

It is also possible to snooze messages with [a rule](#user-content-faq71), which will also allow you to move messages to a folder to let them be auto snoozed.

You can show snoozed messages by unchecking *Filter out* > *Hidden* in the three dot overflow menu.

You can tap on the small snooze icon to see until when a conversation is snoozed.

By selecting a zero snooze duration you can cancel snoozing.

Third party apps do not have access to the Gmail snoozed messages folder.

<br />

<a name="faq68"></a>
**~~(68) Why can Adobe Acrobat reader not open PDF attachments / Microsoft apps not open attached documents?~~**

~~Adobe Acrobat reader and Microsoft apps still expects full access to all stored files,~~ ~~while apps should use the [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) since Android KitKat (2013)~~ ~~to have access to actively shared files only. This is for privacy and security reasons.~~

~~You can workaround this by saving the attachment and opening it from the Adobe Acrobat reader / Microsoft app,~~ ~~but you are advised to install an up-to-date and preferably open source PDF reader / document viewer,~~ ~~for example one listed [here](https://github.com/offa/android-foss#-document--pdf-viewer).~~

<br />

<a name="faq69"></a>
**(69) Can you add auto scroll up on new message?**

The message list is automatically scrolled up when navigating from a new message notification or after a manual refresh. Always automatically scrolling up on arrival of new messages would interfere with your own scrolling, but if you like you can enable this in the settings.

<br />

<a name="faq70"></a>
**(70) When will messages be auto expanded?**

When navigation to a conversation one message will be expanded if:

* There is just one message in the conversation
* There is exactly one unread message in the conversation
* Il y a exactement un message favori (étoile) dans la conversation (à partir de la version 1.1508)

There is one exception: the message was not downloaded yet and the message is too large to download automatically on a metered (mobile) connection. You can set or disable the maximum message size on the 'connection' settings tab.

Duplicate (archived) messages, trashed messages and draft messages are not counted.

Messages will automatically be marked read on expanding, unless this was disabled in the individual account settings.

<br />

<a name="faq71"></a>
**(71) How do I use filter rules?**

You can edit filter rules by long pressing a folder in the folder list of an account (tap the account name in the navigation/side menu).

New rules will be applied to new messages received in the folder, not to existing messages. You can check the rule and apply the rule to existing messages or, alternatively, long press the rule in the rule list and select *Execute now*.

You'll need to give a rule a name and you'll need to define the order in which a rule should be executed relative to other rules.

You can disable a rule and you can stop processing other rules after a rule has been executed.

The following rule conditions are available:

* Sender contains or sender is contact
* Recipient contains
* Subject contains
* Has attachments (optional of specific type)
* Header contains
* Temps absolu (reçu) entre (depuis la version 1.1540)
* Temps relatif (reçu) entre

All the conditions of a rule need to be true for the rule action to be executed. All conditions are optional, but there needs to be at least one condition, to prevent matching all messages. If you want to match all senders or all recipients, you can just use the @ character as condition because all email addresses will contain this character. Si vous voulez faire correspondre un nom de domaine, vous pouvez utiliser une condition comme *@example.org*

Note that email addresses are formatted like this:

`
"Somebody" <somebody@example.org>`

You can use multiple rules, possibly with a *stop processing*, for an *or* or a *not* condition.

Matching is not case sensitive, unless you use [regular expressions](https://en.wikipedia.org/wiki/Regular_expression). Please see [here](https://developer.android.com/reference/java/util/regex/Pattern) for the documentation of Java regular expressions. You can test a regex [here](https://regexr.com/).

Note that a regular expression supports an *or* operator, so if you want to match multiple senders, you can do this:

`
.*alice@example\.org.*|.*bob@example\.org.*|.*carol@example\.org.*`

Note that [dot all mode](https://developer.android.com/reference/java/util/regex/Pattern#DOTALL) is enabled to be able to match [unfolded headers](https://tools.ietf.org/html/rfc2822#section-3.2.3).

You can select one of these actions to apply to matching messages:

* No action (useful for *not*)
* Mark as read
* Mark as unread
* Hide
* Suppress notification
* Snooze
* Add star
* Set importance (local priority)
* Add keyword
* Move
* Copy (Gmail: label)
* Answer/forward (with template)
* Text-to-speech (sender and subject)
* Automation (Tasker, etc)

Une erreur dans une condition de règle peut mener à un désastre, ainsi les actions irréversibles ne sont pas prises en charge.

Les règles sont appliquées dès que l'en-tête du message a été récupéré, mais avant que le texte du message ait été téléchargé, donc il n'est pas possible d'appliquer des conditions au texte du message. Notez que les textes de gros messages sont téléchargés à la demande sur une connexion limitée pour économiser sur l'utilisation des données.

Si vous voulez transférer un message, pensez à utiliser l'action de déplacement à la place. Cela sera plus fiable que le transfert car les messages transmis peuvent être considérés comme des pourriels.

Comme les en-têtes de message ne sont pas téléchargés et stockés par défaut pour économiser sur l'utilisation de la batterie et des données et pour économiser de l'espace de stockage il n'est pas possible de prévisualiser quels messages correspondent à une condition de règle d'en-tête.

Quelques conditions d'en-tête communes (regex):

* *.&ast;Auto-Submitted:.&ast;* [RFC3834](https://tools.ietf.org/html/rfc3834)
* *.&ast;Content-Type: multipart/report.&ast;* [RFC3462](https://tools.ietf.org/html/rfc3462)

Dans le menu à trois points *plus* du message il y a un élément pour créer une règle pour un message reçu avec les conditions les plus courantes remplies.

Le protocole POP3 ne prend pas en charge la définition de mots-clés et le déplacement ou la copie des messages.

L'utilisation des règles est une fonctionnalité pro.

<br />

<a name="faq72"></a>
**(72) Que sont les comptes/identités primaires ?**

Le compte principal est utilisé lorsque le compte est ambigu, par exemple lors du démarrage d'un nouveau brouillon à partir de la boîte de réception unifiée.

De même, l'identité principale d'un compte est utilisée lorsque l'identité est ambiguë.

Il peut n'y avoir qu'un seul compte principal et il peut n'y avoir qu'une seule identité principale par compte.

<br />

<a name="faq73"></a>
**(73) Est-ce que le déplacement des messages entre les comptes est sûr/efficace ?**

Le déplacement des messages entre les comptes est sûr car les messages originaux bruts seront téléchargés et déplacés et parce que les messages source ne seront supprimés qu'après l'ajout des messages cibles

Le déplacement de messages par lots à travers les comptes est efficace si le dossier source et le dossier cible sont configurés pour synchroniser, sinon FairEmail a besoin de se connecter au(x) dossier(s) pour chaque message.

<br />

<a name="faq74"></a>
**(74) Pourquoi est-ce que je vois des messages en double ?**

Certains fournisseurs, notamment Gmail, listent tous les messages dans tous les dossiers, à l'exception des messages mis à la corbeille, dans le dossier archives (tous les messages) également. FairEmail montre tous ces messages de manière discrète pour indiquer que ces messages sont en fait le même message.

Gmail permet à un message d'avoir plusieurs étiquettes, qui sont présentés à FairEmail comme des dossiers. Cela signifie que les messages avec plusieurs étiquettes seront également affichés plusieurs fois.

<br />

<a name="faq75"></a>
**(75) Pouvez-vous faire une version iOS, Windows, Linux, etc ?**

Beaucoup de connaissances et d'expérience sont nécessaires pour développer avec succès une application pour une plate-forme spécifique. cC'est pourquoi je ne développe que des applications pour Android.

<br />

<a name="faq76"></a>
**(76) Que fait 'Effacer les messages locaux' ?**

Le menu dossiers *Effacer les messages locaux* supprime les messages de l'appareil qui sont présents sur le serveur. Il ne supprime pas les messages du serveur. Cela peut être utile après avoir modifié les paramètres du dossier pour ne pas télécharger le contenu du message (texte et pièces jointes), par exemple pour économiser de l'espace.

<br />

<a name="faq77"></a>
**(77) Pourquoi les messages sont-ils parfois affichés avec un petit retard ?**

Selon la vitesse de votre appareil (vitesse du processeur et probablement surtout la vitesse de la mémoire) des messages peuvent s'afficher avec un petit délai. FairEmail est conçu pour gérer dynamiquement un grand nombre de messages sans epuiser la totalité de la mémoire. Cela signifie que les messages doivent être lus depuis une base de données et que cette base de données doit être surveillée pour des modifications. toutes deux peuvent entraîner de petits retards.

Certaines fonctionnalités pratiques comme le regroupement des messages pour afficher les fils de conversation et déterminer le message précédent/suivant, prennent un peu plus de temps. Notez qu'il n'y a pas *le* message suivant car entre-temps un nouveau message pourrait être arrivé.

Lorsque vous comparez la vitesse de FairEmail avec des applications similaires, cela devrait faire partie de la comparaison. Il est facile d'écrire une application similaire et plus rapide qui affiche simplement une liste de messages, probablement en utilisant trop de mémoire, mais il n'est pas si facile de gérer correctement l'utilisation des ressources et d'offrir des fonctionnalités plus avancées comme les fils de conversations.

FairEmail est basé sur les [composants d'architecture Android dernier cri](https://developer.android.com/topic/libraries/architecture/), donc il y a peu de place pour des améliorations de performance.

<br />

<a name="faq78"></a>
**(78) How do I use schedules?**

In the receive settings you can enable scheduling and set a time period and the days of the week *when* messages should be *received*. Note that an end time equal to or earlier than the start time is considered to be 24 hours later.

Automation, see below, can be used for more advanced schedules, like for example multiple synchronization periods per day or different synchronization periods for different days.

It is possible to install FairEmail in multiple user profiles, for example a personal and a work profile, and to configure FairEmail differently in each profile, which is another possibility to have different synchronization schedules and to synchronize a different set of accounts.

It is also possible to create [filter rules](#user-content-faq71) with a time condition and to snooze messages until the end time of the time condition. This way it is possible to *snooze* business related messages until the start of the business hours. This also means that the messages will be on your device for when there is (temporarily) no internet connection.

Note that recent Android versions allow overriding DND (Do Not Disturb) per notification channel and per app, which could be used to (not) silence specific (business) notifications. Please [see here](https://support.google.com/android/answer/9069335) for more information.

For more complex schemes you could set one or more accounts to manual synchronization and send this command to FairEmail to check for new messages:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL
```

For a specific account:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL --es account Gmail
```

You can also automate turning receiving messages on and off by sending these commands to FairEmail:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE
```

To enable/disable a specific account:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE --es account Gmail
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE --es account Gmail
```

Note that disabling an account will hide the account and all associated folders and messages.

To set the poll interval:

```
(adb shell) adb shell am start-foreground-service -a eu.faircode.email.INTERVAL --ei minutes nnn
```

Where *nnn* is one of 0, 15, 30, 60, 120, 240, 480, 1440. A value of 0 means push messages.

You can automatically send commands with for example [Tasker](https://tasker.joaoapps.com/userguide/en/intents.html):

```
New task: Something recognizable
Action Category: Misc/Send Intent
Action: eu.faircode.email.ENABLE
Target: Service
```

To enable/disable an account with the name *Gmail*:

```
Extras: account:Gmail
```

Account names are case sensitive.

Scheduling is a pro feature.

<br />

<a name="faq79"></a>
**(79) How do I use synchronize on demand (manual)?**

Normally, FairEmail maintains a connection to the configured email servers whenever possible to receive messages in real-time. If you don't want this, for example to be not disturbed or to save on battery usage, just disable receiving in the receive settings. This will stop the background service which takes care of automatic synchronization and will remove the associated status bar notification.

You can also enable *Synchronize manually* in the advanced account settings if you want to manually synchronize specific accounts only.

You can use pull-down-to-refresh in a message list or use the folder menu *Synchronize now* to manually synchronize messages.

If you want to synchronize some or all folders of an account manually, just disable synchronization for the folders (but not of the account).

You'll likely want to disabled [browse on server](#user-content-faq24) too.

<br />

<a name="faq80"></a>
**~~(80) How do I fix the error 'Unable to load BODYSTRUCTURE' ?~~**

~~The error message *Unable to load BODYSTRUCTURE* is caused by bugs in the email server,~~ ~~see [here](https://javaee.github.io/javamail/FAQ#imapserverbug) for more details.~~

~~FairEmail already tries to workaround these bugs, but if this fail you'll need to ask for support from your provider.~~

<br />

<a name="faq81"></a>
**~~(81) Can you make the background of the original message dark in the dark theme?~~**

~~The original message is shown as the sender has sent it, including all colors.~~ ~~Changing the background color would not only make the original view not original anymore, it can also result in unreadable messages.~~

<br />

<a name="faq82"></a>
**(82) Qu'est-ce qu'une image de suivi ?**

Veuillez consulter [ici](https://en.wikipedia.org/wiki/Web_beacon) pour savoir ce qu'est exactement une image de suivi. En bref, les images de suivi gardent trace de l'ouverture d'un message.

Dans la plupart des cas, FairEmail reconnaîtra automatiquement les images de suivi et les remplacera par cette icône :

![Image externe](https://github.com/M66B/FairEmail/blob/master/images/baseline_my_location_black_48dp.png)

La reconnaissance automatique des images de suivi peut être désactivée dans les paramètres de confidentialité.

<br />

<a name="faq84"></a>
**(84) What are local contacts for?**

Local contact information is based on names and addresses found in incoming and outgoing messages.

The main use of the local contacts storage is to offer auto completion when no contacts permission has been granted to FairEmail.

Another use is to generate [shortcuts](#user-content-faq31) on recent Android versions to quickly send a message to frequently contacted people. This is also why the number of times contacted and the last time contacted is being recorded and why you can make a contact a favorite or exclude it from favorites by long pressing it.

The list of contacts is sorted on number of times contacted and the last time contacted.

By default only names and addresses to whom you send messages to will be recorded. You can change this in the send settings.

<br />

<a name="faq85"></a>
**(85) Why is an identity not available?**

An identity is available for sending a new message or replying or forwarding an existing message only if:

* the identity is set to synchronize (send messages)
* the associated account is set to synchronize (receive messages)
* the associated account has a drafts folder

FairEmail will try to select the best identity based on the *to* address of the message replied to / being forwarded.

<br />

<a name="faq86"></a>
**~~(86) Quelles sont les 'fonctionnalités de confidentialité supplémentaires'?~~**

~L'option avancée *fonctionnalités de confidentialité supplémentaires* permet :~~

* ~~Recherche du propriétaire de l'adresse IP d'un lien~~
* ~~Détection et suppression des [images de suivi](#user-content-faq82)~~

<br />

<a name="faq87"></a>
**(87) What does 'invalid credentials' mean?**

The error message *invalid credentials* means either that the user name and/or password is incorrect, for example because the password was changed or expired, or that the account authorization has expired.

If the password is incorrect/expired, you will have to update the password in the account and/or identity settings.

If the account authorization has expired, you will have to select the account again. You will likely need to save the associated identity again as well.

<br />

<a name="faq88"></a>
**(88) How can I use a Yahoo, AOL or Sky account?**

The preferred way to set up a Yahoo account is by using the quick setup wizard, which will use OAuth instead of a password and is therefore safer (and easier as well).

To authorize a Yahoo, AOL, or Sky account you will need to create an app password. For instructions, please see here:

* [for Yahoo](https://help.yahoo.com/kb/generate-third-party-passwords-sln15241.html)
* [for AOL](https://help.aol.com/articles/Create-and-manage-app-password)
* [for Sky](https://www.sky.com/help/articles/getting-started-with-sky-yahoo-mail) (under *Other email apps*)

Please see [this FAQ](#user-content-faq111) about OAuth support.

Note that Yahoo, AOL, and Sky do not support standard push messages. The Yahoo email app uses a proprietary, undocumented protocol for push messages.

Push messages require [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE) and the Yahoo email server does not report IDLE as capability:

```
Y1 CAPABILITY
* CAPABILITY IMAP4rev1 ID MOVE NAMESPACE XYMHIGHESTMODSEQ UIDPLUS LITERAL+ CHILDREN X-MSG-EXT UNSELECT OBJECTID
Y1 OK CAPABILITY completed
```

<br />

<a name="faq89"></a>
**(89) How can I send plain text only messages?**

By default FairEmail sends each message both as plain text and as HTML formatted text because almost every receiver expects formatted messages these days. If you want/need to send plain text messages only, you can enable this in the advanced identity options. You might want to create a new identity for this if you want/need to select sending plain text messages on a case-by-case basis.

<br />

<a name="faq90"></a>
**(90) Why are some texts linked while not being a link?**

FairEmail will automatically link not linked web links (http and https) and not linked email addresses (mailto) for your convenience. However, texts and links are not easily distinguished, especially not with lots of [top level domains](https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains) being words. This is why texts with dots are sometimes incorrectly recognized as links, which is better than not recognizing some links.

Links for the tel, geo, rtsp and xmpp protocols will be recognized too, but links for less usual or less safe protocols like telnet and ftp will not be recognized. The regex to recognize links is already *very* complex and adding more protocols will make it only slower and possibly cause errors.

Note that original messages are shown exactly as they are, which means also that links are not automatically added.

<br />

<a name="faq91"></a>
**~~(91) Can you add periodical synchronization to save battery power?~~**

~~Synchronizing messages is an expensive proces because the local and remote messages need to be compared,~~ ~~so periodically synchronizing messages will not result in saving battery power, more likely the contrary.~~

~~See [this FAQ](#user-content-faq39) about optimizing battery usage.~~

<br />

<a name="faq92"></a>
**(92) Can you add spam filtering, verification of the DKIM signature and SPF authorization?**

Spam filtering, verification of the [DKIM](https://en.wikipedia.org/wiki/DomainKeys_Identified_Mail) signature and [SPF](https://en.wikipedia.org/wiki/Sender_Policy_Framework) authorization is a task of email servers, not of an email client. Servers generally have more memory and computing power, so they are much better suited to this task than battery-powered devices. Also, you'll want spam filtered for all your email clients, possibly including web email, not just one email client. Moreover, email servers have access to information, like the IP address, etc of the connecting server, which an email client has no access to.

Spam filtering based on message headers might have been feasible, but unfortunately this technique is [patented by Microsoft](https://patents.google.com/patent/US7543076).

Recent versions of FairEmail can filter spam to a certain extend using a message classifier. Please see [this FAQ](#user-content-faq163) for more information about this.

Of course you can report messages as spam with FairEmail, which will move the reported messages to the spam folder and train the spam filter of the provider, which is how it is supposed to work. This can be done automatically with [filter rules](#user-content-faq71) too. Blocking the sender will create a filter rule to automatically move future messages of the same sender into the spam folder.

Note that the POP3 protocol gives access to the inbox only. So, it is won't be possible to report spam for POP3 accounts.

Note that you should not delete spam messages, also not from the spam folder, because the email server uses the messages in the spam folder to "learn" what spam messages are.

If you receive a lot of spam messages in your inbox, the best you can do is to contact the email provider to ask if spam filtering can be improved.

Also, FairEmail can show a small red warning flag when DKIM, SPF or [DMARC](https://en.wikipedia.org/wiki/DMARC) authentication failed on the receiving server. You can enable/disable [authentication verification](https://en.wikipedia.org/wiki/Email_authentication) in the display settings.

FairEmail can show a warning flag too if the domain name of the (reply) email address of the sender does not define an MX record pointing to an email server. This can be enabled in the receive settings. Be aware that this will slow down synchronization of messages significantly.

If the domain name of the sender and the domain name of the reply address differ, the warning flag will be shown too because this is most often the case with phishing messages. If desired, this can be disabled in the receive settings (from version 1.1506).

If legitimate messages are failing authentication, you should notify the sender because this will result in a high risk of messages ending up in the spam folder. Moreover, without proper authentication there is a risk the sender will be impersonated. The sender might use [this tool](https://www.mail-tester.com/) to check authentication and other things.

<br />

<a name="faq93"></a>
**(93) Can you allow installation/data storage on external storage media (sdcard)?**

FairEmail uses services and alarms, provides widgets and listens for the boot completed event to be started on device start, so it is not possible to store the app on external storage media, like an sdcard. See also [here](https://developer.android.com/guide/topics/data/install-location).

Messages, attachments, etc stored on external storage media, like an sdcard, can be accessed by other apps and is therefore not safe. See [here](https://developer.android.com/training/data-storage) for the details.

When needed you can save (raw) messages via the three-dots menu just above the message text and save attachments by tapping on the floppy icon.

If you need to save on storage space, you can limit the number of days messages are being synchronized and kept for. You can change these settings by long pressing a folder in the folder list and selecting *Edit properties*.

<br />

<a name="faq94"></a>
**(94) What does the red/orange stripe at the end of the header mean?**

The red/orange stripe at the left side of the header means that the DKIM, SPF or DMARC authentication failed. See also [this FAQ](#user-content-faq92).

<br />

<a name="faq95"></a>
**(95) Why are not all apps shown when selecting an attachment or image?**

For privacy and security reasons FairEmail does not have permissions to directly access files, instead the Storage Access Framework, available and recommended since Android 4.4 KitKat (released in 2013), is used to select files.

If an app is listed depends on if the app implements a [document provider](https://developer.android.com/guide/topics/providers/document-provider). If the app is not listed, you might need to ask the developer of the app to add support for the Storage Access Framework.

Android Q will make it harder and maybe even impossible to directly access files, see [here](https://developer.android.com/preview/privacy/scoped-storage) and [here](https://www.xda-developers.com/android-q-storage-access-framework-scoped-storage/) for more details.

<br />

<a name="faq96"></a>
**(96) Where can I find the IMAP and SMTP settings?**

The IMAP settings are part of the (custom) account settings and the SMTP settings are part of the identity settings.

<br />

<a name="faq97"></a>
**(97) What is 'cleanup' ?**

About each four hours FairEmail runs a cleanup job that:

* Removes old message texts
* Removes old attachment files
* Removes old image files
* Removes old local contacts
* Removes old log entries

Note that the cleanup job will only run when the synchronize service is active.

<br />

<a name="faq98"></a>
**(98) Why can I still pick contacts after revoking contacts permissions?**

After revoking contacts permissions Android does not allow FairEmail access to your contacts anymore. However, picking contacts is delegated to and done by Android and not by FairEmail, so this will still be possible without contacts permissions.

<br />

<a name="faq99"></a>
**(99) Can you add a rich text or markdown editor?**

FairEmail provides common text formatting (bold, italic, underline, text size and color) via a toolbar that appears after selecting some text.

A [Rich text](https://en.wikipedia.org/wiki/Formatted_text) or [Markdown](https://en.wikipedia.org/wiki/Markdown) editor would not be used by many people on a small mobile device and, more important, Android doesn't support a rich text editor and most rich text editor open source projects are abandoned. See [here](https://forum.xda-developers.com/showpost.php?p=79061829&postcount=4919) for some more details about this.

<br />

<a name="faq100"></a>
**(100) How can I synchronize Gmail categories?**

You can synchronize Gmail categories by creating filters to label categorized messages:

* Create a new filter via Gmail > Settings (wheel) > Filters and Blocked Addresses > Create a new filter
* Enter a category search (see below) in the *Has the words* field and click *Create filter*
* Check *Apply the label* and select a label and click *Create filter*

Possible categories:

```
category:social
category:updates
category:forums
category:promotions
```

Unfortunately, this is not possible for snoozed messages folder.

You can use *Force sync* in the three-dots menu of the unified inbox to let FairEmail synchronize the folder list again and you can long press the folders to enable synchronization.

<br />

<a name="faq101"></a>
**(101) What does the blue/orange dot at the bottom of the conversations mean?**

The dot shows the relative position of the conversation in the message list. The dot will be show orange when the conversation is the first or last in the message list, else it will be blue. The dot is meant as an aid when swiping left/right to go to the previous/next conversation.

The dot is disabled by default and can be enabled with the display settings *Show relative conversation position with a dot*.

<br />

<a name="faq102"></a>
**(102) How can I enable auto rotation of images?**

Images will automatically be rotated when automatic resizing of images is enabled in the settings (enabled by default). However, automatic rotating depends on the [Exif](https://en.wikipedia.org/wiki/Exif) information to be present and to be correct, which is not always the case. Particularly not when taking a photo with a camara app from FairEmail.

Note that only [JPEG](https://en.wikipedia.org/wiki/JPEG) and [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) images can contain Exif information.

<br />

<a name="faq104"></a>
**(104) What do I need to know about error reporting?**

* Error reports will help improve FairEmail
* Error reporting is optional and opt-in
* Error reporting can be enabled/disabled in the settings, section miscellaneous
* Error reports will automatically be sent anonymously to [Bugsnag](https://www.bugsnag.com/)
* Bugsnag for Android is [open source](https://github.com/bugsnag/bugsnag-android)
* See [here](https://docs.bugsnag.com/platforms/android/automatically-captured-data/) about what data will be sent in case of errors
* See [here](https://docs.bugsnag.com/legal/privacy-policy/) for the privacy policy of Bugsnag
* Error reports will be sent to *sessions.bugsnag.com:443* and *notify.bugsnag.com:443*

<br />

<a name="faq105"></a>
**(105) How does the roam-like-at-home option work?**

FairEmail will check if the country code of the SIM card and the country code of the network are in the [EU roam-like-at-home countries](https://en.wikipedia.org/wiki/European_Union_roaming_regulations#Territorial_extent) and assumes no roaming if the country codes are equal and the advanced roam-like-at-home option is enabled.

So, you don't have to disable this option if you don't have an EU SIM or are not connected to an EU network.

<br />

<a name="faq106"></a>
**(106) Which launchers can show a badge count with the number of unread messages?**

Please [see here](https://github.com/leolin310148/ShortcutBadger#supported-launchers) for a list of launchers which can show the number of unread messages.

Note that Nova Launcher requires Tesla Unread, which is [not supported anymore](https://forum.xda-developers.com/android/general/bad-news-tesla-unread-devoloper-t3920415).

Note that the notification setting *Show launcher icon with number of new messages* needs to be enabled (default enabled).

Only *new* unread messages in folders set to show new message notifications will be counted, so messages marked unread again and messages in folders set to not show new message notification will not be counted.

Depending on what you want, the notification settings *Let the number of new messages match the number of notifications* needs to be enabled (default disabled). When enabled the badge count will be the same as the number of new message notifications. When disabled the badge count will be the number of unread messages, independent if they are shown in a notification or are new.

This feature depends on support of your launcher. FairEmail merely 'broadcasts' the number of unread messages using the ShortcutBadger library. If it doesn't work, this cannot be fixed by changes in FairEmail.

Some launchers display a dot or a '1' for [the monitoring notification](#user-content-faq2), despite FairEmail explicitly requesting not to show a *badge* for this notification. This could be caused by a bug in the launcher app or in your Android version. Please double check if the notification dot (badge) is disabled for the receive (service) notification channel. You can go to the right notification channel settings via the notification settings of FairEmail. This might not be obvious, but you can tap on the channel name for more settings.

FairEmail does send a new message count intent as well:

```
eu.faircode.email.NEW_MESSAGE_COUNT
```

The number of new, unread messages will be in an integer "*count*" parameter.

<br />

<a name="faq107"></a>
**(107) How do I use colored stars?**

You can set a colored star via the *more* message menu, via multiple selection (started by long pressing a message), by long pressing a star in a conversation or automatically by using [rules](#user-content-faq71).

You need to know that colored stars are not supported by the IMAP protocol and can therefore not be synchronized to an email server. This means that colored stars will not be visible in other email clients and will be lost on downloading messages again. However, the stars (without color) will be synchronized and will be visible in other email clients, when supported.

Some email clients use IMAP keywords for colors. However, not all servers support IMAP keywords and besides that there are no standard keywords for colors.

<br />

<a name="faq108"></a>
**~~(108) Can you add permanently delete messages from any folder?~~**

~~When you delete messages from a folder the messages will be moved to the trash folder, so you have a chance to restore the messages.~~ ~~You can permanently delete messages from the trash folder.~~ ~~Permanently delete messages from other folders would defeat the purpose of the trash folder, so this will not be added.~~

<br />

<a name="faq109"></a>
**~~(109) Why is 'select account' available in official versions only?~~**

~~Using *select account* to select and authorize Google accounts require special permission from Google for security and privacy reasons.~~ ~~This special permission can only be acquired for apps a developer manages and is responsible for.~~ ~~Third party builds, like the F-Droid builds, are managed by third parties and are the responsibility of these third parties.~~ ~~So, only these third parties can acquire the required permission from Google.~~ ~~Since these third parties do not actually support FairEmail, they are most likely not going to request the required permission.~~

~~You can solve this in two ways:~~

* ~~Switch to the official version of FairEmail, see [here](https://github.com/M66B/FairEmail/blob/master/README.md#downloads) for the options~~
* ~~Use app specific passwords, see [this FAQ](#user-content-faq6)~~

~~Using *select account* in third party builds is not possible in recent versions anymore.~~ ~~In older versions this was possible, but it will now result in the error *UNREGISTERED_ON_API_CONSOLE*.~~

<br />

<a name="faq110"></a>
**(110) Why are (some) messages empty and/or attachments corrupt?**

Empty messages and/or corrupt attachments are probably being caused by a bug in the server software. Older Microsoft Exchange software is known to cause this problem. Mostly you can workaround this by disabling *Partial fetch* in the advanced account settings:

Settings > Manual setup > Accounts > tap account > tap advanced > Partial fetch > uncheck

After disabling this setting, you can use the message 'more' (three dots) menu to 'resync' empty messages. Alternatively, you can *Delete local messages* by long pressing the folder(s) in the folder list and synchronize all messages again.

Disabling *Partial fetch* will result in more memory usage.

<br />

<a name="faq111"></a>
**(111) Is OAuth supported?**

OAuth for Gmail is supported via the quick setup wizard. The Android account manager will be used to fetch and refresh OAuth tokens for selected on-device accounts. OAuth for non on-device accounts is not supported because Google requires a [yearly security audit](https://support.google.com/cloud/answer/9110914) ($15,000 to $75,000) for this. You can read more about this [here](https://www.theregister.com/2019/02/11/google_gmail_developer/).

OAuth for Outlook/Office 365, Yahoo, Mail.ru and Yandex is supported via the quick setup wizard.

<br />

<a name="faq112"></a>
**(112) Which email provider do you recommend?**

FairEmail is an email client only, so you need to bring your own email address. Note that this is clearly mentioned in the app description.

There are plenty of email providers to choose from. Which email provider is best for you depends on your wishes/requirements. Please see the websites of [Restore privacy](https://restoreprivacy.com/secure-email/) or [Privacy Tools](https://www.privacytools.io/providers/email/) for a list of privacy oriented email providers with advantages and disadvantages.

Some providers, like ProtonMail, Tutanota, use proprietary email protocols, which make it impossible to use third party email apps. Please see [this FAQ](#user-content-faq129) for more information.

Using your own (custom) domain name, which is supported by most email providers, will make it easier to switch to another email provider.

<br />

<a name="faq113"></a>
**(113) How does biometric authentication work?**

If your device has a biometric sensor, for example a fingerprint sensor, you can enable/disable biometric authentication in the navigation (hamburger) menu of the settings screen. When enabled FairEmail will require biometric authentication after a period of inactivity or after the screen has been turned off while FairEmail was running. Activity is navigation within FairEmail, for example opening a conversation thread. The inactivity period duration can be configured in the miscellaneous settings. When biometric authentication is enabled new message notifications will not show any content and FairEmail won't be visible on the Android recents screen.

Biometric authentication is meant to prevent others from seeing your messages only. FairEmail relies on device encryption for data encryption, see also [this FAQ](#user-content-faq37).

Biometric authentication is a pro feature.

<br />

<a name="faq114"></a>
**(114) Can you add an import for the settings of other email apps?**

The format of the settings files of most other email apps is not documented, so this is difficult. Sometimes it is possible to reverse engineer the format, but as soon as the settings format changes things will break. Also, settings are often incompatible. For example, FairEmail has unlike most other email apps settings for the number of days to synchronize messages and for the number of days to keep messages, mainly to save on battery usage. Moreover, setting up an account/identity with the quick setup wizard is simple, so it is not really worth the effort.

<br />

<a name="faq115"></a>
**(115) Can you add email address chips?**

Email address [chips](https://material.io/design/components/chips.html) look nice, but cannot be edited, which is quite inconvenient when you made a typo in an email address.

Note that FairEmail will select the address only when long pressing an address, which makes it easy to delete an address.

Chips are not suitable for showing in a list and since the message header in a list should look similar to the message header of the message view it is not an option to use chips for viewing messages.

Reverted [commit](https://github.com/M66B/FairEmail/commit/2c80c25b8aa75af2287f471b882ec87d5a5a5015).

<br />

<a name="faq116"></a>
**~~(116) How can I show images in messages from trusted senders by default?~~**

~~You can show images in messages from trusted senders by default by enabled the display setting *Automatically show images for known contacts*.~~

~~Contacts in the Android contacts list are considered to be known and trusted,~~ ~~unless the contact is in the group / has the label '*Untrusted*' (case insensitive).~~

<br />

<a name="faq38"></a>
<a name="faq117"></a>
**(117) Can you help me restore my purchase?**

First of all, a purchase will be available on all devices logged into the same Google account, *if* the app is installed via the same Google account too. You can select the account in the Play store app.

Google manages all purchases, so as a developer I have little control over purchases. So, basically the only thing I can do, is give some advice:

* Make sure you have an active, working internet connection
* Make sure you are logged in with the right Google account and that there is nothing wrong with your Google account
* Make sure you installed FairEmail via the right Google account if you configured multiple Google accounts on your device
* Make sure the Play store app is up to date, please [see here](https://support.google.com/googleplay/answer/1050566?hl=en)
* Open the Play store app and wait at least a minute to give it time to synchronize with the Google servers
* Open FairEmail and navigate to the pro features screen to let FairEmail check the purchases; sometimes it help to tap the *buy* button

You can also try to clear the cache of the Play store app via the Android apps settings. Restarting the device might be necessary to let the Play store recognize the purchase correctly.

Note that:

* If you get *ITEM_ALREADY_OWNED*, the Play store app probably needs to be updated, please [see here](https://support.google.com/googleplay/answer/1050566?hl=en)
* Purchases are stored in the Google cloud and cannot get lost
* There is no time limit on purchases, so they cannot expire
* Google does not expose details (name, e-mail, etc) about buyers to developers
* An app like FairEmail cannot select which Google account to use
* It may take a while until the Play store app has synchronized a purchase to another device
* Play Store purchases cannot be used without the Play Store, which is also not allowed by Play Store rules

If you cannot solve the problem with the purchase, you will have to contact Google about it.

<br />

<a name="faq118"></a>
**(118) What does 'Remove tracking parameters' exactly?**

Checking *Remove tracking parameters* will remove all [UTM parameters](https://en.wikipedia.org/wiki/UTM_parameters) from a link.

<br />

<a name="faq119"></a>
**~~(119) Can you add colors to the unified inbox widget?~~**

~~The widget is designed to look good on most home/launcher screens by making it monochrome and by using a half transparent background.~~ ~~This way the widget will nicely blend in, while still being properly readable.~~

~~Adding colors will cause problems with some backgrounds and will cause readability problems, which is why this won't be added.~~

Due to Android limitations it is not possible to dynamically set the opacity of the background and to have rounded corners at the same time.

<br />

<a name="faq120"></a>
**(120) Why are new message notifications not removed on opening the app?**

New message notifications will be removed on swiping notifications away or on marking the associated messages read. Opening the app will not remove new message notifications. This gives you a choice to leave new message notifications as a reminder that there are still unread messages.

On Android 7 Nougat and later new message notifications will be [grouped](https://developer.android.com/training/notify-user/group). Tapping on the summary notification will open the unified inbox. The summary notification can be expanded to view individual new message notifications. Tapping on an individual new message notification will open the conversation the message it is part of. See [this FAQ](#user-content-faq70) about when messages in a conversation will be auto expanded and marked read.

<br />

<a name="faq121"></a>
**(121) How are messages grouped into a conversation?**

By default FairEmail groups messages in conversations. This can be turned of in the display settings.

FairEmail groups messages based on the standard *Message-ID*, *In-Reply-To* and *References* headers. FairEmail does not group on other criteria, like the subject, because this could result in grouping unrelated messages and would be at the expense of increased battery usage.

<br />

<a name="faq122"></a>
**~~(122) Why is the recipient name/email address show with a warning color?~~**

~~The recipient name and/or email address in the addresses section will be shown in a warning color~~ ~~when the sender domain name and the domain name of the *to* address do not match.~~ ~~Mostly this indicates that the message was received *via* an account with another email address.~~

<br />

<a name="faq123"></a>
**(123) What will happen when FairEmail cannot connect to an email server?**

If FairEmail cannot connect to an email server to synchronize messages, for example if the internet connection is bad or a firewall or a VPN is blocking the connection, FairEmail will retry one time after waiting 8 seconds while keeping the device awake (=use battery power). If this fails, FairEmail will schedule an alarm to retry after 15, 30 and eventually every 60 minutes and let the device sleep (=no battery usage).

Note that [Android doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby) does not allow to wake the device earlier than after 15 minutes.

*Force sync* in the three-dots menu of the unified inbox can be used to let FairEmail attempt to reconnect without waiting.

Sending messages will be retried on connectivity changes only (reconnecting to the same network or connecting to another network) to prevent the email server from blocking the connection permanently. You can pull down the outbox to retry manually.

Note that sending will not be retried in case of authentication problems and when the server rejected the message. In this case you can pull down the outbox to try again.

<br />

<a name="faq124"></a>
**(124) Why do I get 'Message too large or too complex to display'?**

The message *Message too large or too complex to display* will be shown if there are more than 100,000 characters or more than 500 links in a message. Reformatting and displaying such messages will take too long. You can try to use the original message view, powered by the browser, instead.

<br />

<a name="faq125"></a>
**(125) What are the current experimental features?**

*Message classification (version 1.1438+)*

Please see [this FAQ](#user-content-faq163) for details.

Since this is an experimental feature, my advice is to start with just one folder.

<br />

*Send hard bounce (version 1.1477+)*

Send a [Delivery Status Notification](https://tools.ietf.org/html/rfc3464) (=hard bounce) via the reply/answer menu.

Hard bounces will mostly be processed automatically because they affect the reputation of the email provider. The bounce address (=*Return-Path* header) is mostly very specific, so the email server can determine the sending account.

For some background, see for [this Wikipedia article](https://en.wikipedia.org/wiki/Bounce_message).

<br />

<a name="faq126"></a>
**(126) Can message previews be sent to my wearable?**

FairEmail fetches a message in two steps:

1. Fetch message headers
1. Fetch message text and attachments

Directly after the first step new messages will be notified. However, only until after the second step the message text will be available. FairEmail updates exiting notifications with a preview of the message text, but unfortunately wearable notifications cannot be updated.

Since there is no guarantee that a message text will always be fetched directly after a message header, it is not possible to guarantee that a new message notification with a preview text will always be sent to a wearable.

If you think this is good enough, you can enable the notification option *Only send notifications with a message preview to wearables* and if this does not work, you can try to enable the notification option *Show notifications with a preview text only*. Note that this applies to wearables not showing a preview text too, even when the Android Wear app says the notification has been sent (bridged).

If you want to have the full message text sent to your wearable, you can enable the notification option *Preview all text*. Note that some wearables are known to crash with this option enabled.

If you use a Samsung wearable with the Galaxy Wearable (Samsung Gear) app, you might need to enable notifications for FairEmail when the setting *Notifications*, *Apps installed in the future* is turned off in this app.

<br />

<a name="faq127"></a>
**(127) How can I fix 'Syntactically invalid HELO argument(s)'?**

The error *... Syntactically invalid HELO argument(s) ...* means that the SMTP server rejected the local IP address or host name. You can likely fix this error by enabling or disabling the advanced indentity option *Use local IP address instead of host name*.

<br />

<a name="faq128"></a>
**(128) How can I reset asked questions, for example to show images?**

You can reset asked questions via the three dots overflow menu in the miscellaneous settings.

<br />

<a name="faq129"></a>
**(129) Are ProtonMail, Tutanota supported?**

ProtonMail uses a proprietary email protocol and [does not directly support IMAP](https://protonmail.com/support/knowledge-base/imap-smtp-and-pop3-setup/), so you cannot use FairEmail to access ProtonMail.

Tutanota uses a proprietary email protocol and [does not support IMAP](https://tutanota.com/faq/#imap), so you cannot use FairEmail to access Tutanota.

<br />

<a name="faq130"></a>
**(130) What does message error ... mean?**

A series of lines with orangish or red texts with technical information means that debug mode was enabled in the miscellaneous settings.

The warning *No server found at ...* means that there was no email server registered at the indicated domain name. Replying to the message might not be possible and might result in an error. This could indicate a falsified email address and/or spam.

The error *... ParseException ...* means that there is a problem with a received message, likely caused by a bug in the sending software. FairEmail will workaround this is in most cases, so this message can mostly be considered as a warning instead of an error.

The error *...SendFailedException...* means that there was a problem while sending a message. The error will almost always include a reason. Common reasons are that the message was too big or that one or more recipient addresses were invalid.

The warning *Message too large to fit into the available memory* means that the message was larger than 10 MiB. Even if your device has plenty of storage space Android provides limited working memory to apps, which limits the size of messages that can be handled.

Please see [here](#user-content-faq22) for other error messages in the outbox.

<br />

<a name="faq131"></a>
**(131) Can you change the direction for swiping to previous/next message?**

If you read from left to right, swiping to the left will show the next message. Similarly, if you read from right to left, swiping to the right will show the next message.

This behavior seems quite natural to me, also because it is similar to turning pages.

Anyway, there is a behavior setting to reverse the swipe direction.

<br />

<a name="faq132"></a>
**(132) Why are new message notifications silent?**

Notifications are silent by default on some MIUI versions. Please see [here](http://en.miui.com/thread-3930694-1-1.html) how you can fix this.

There is a bug in some Android versions causing [setOnlyAlertOnce](https://developer.android.com/reference/android/app/Notification.Builder#setOnlyAlertOnce(boolean)) to mute notifications. Since FairEmail shows new message notifications right after fetching the message headers and FairEmail needs to update new message notifications after fetching the message text later, this cannot be fixed or worked around by FairEmail.

Android might rate limit the notification sound, which can cause some new message notifications to be silent.

<br />

<a name="faq133"></a>
**(133) Why is ActiveSync not supported?**

The Microsoft Exchange ActiveSync protocol [is patented](https://en.wikipedia.org/wiki/Exchange_ActiveSync#Licensing) and can therefore not be supported. For this reason you won't find many, if any, other email clients supporting ActiveSync.

Note that the desciption of FairEmail starts with the remark that non-standard protocols, like Microsoft Exchange Web Services and Microsoft ActiveSync are not supported.

<br />

<a name="faq134"></a>
**(134) Can you add deleting local messages?**

*POP3*

In the account settings (Settings, tap Manual setup, tap Accounts, tap account) you can enable *Leave deleted messages on server*.

*IMAP*

Since the IMAP protocol is meant to synchronize two ways, deleting a message from the device would result in fetching the message again when synchronizing again.

However, FairEmail supports hiding messages, either via the three-dots menu in the action bar just above the message text or by multiple selecting messages in the message list. Basically this is the same as "leave on server" of the POP3 protocol with the advantage that you can show the messages again when needed.

Note that it is possible to set the swipe left or right action to hide a message.

<br />

<a name="faq135"></a>
**(135) Why are trashed messages and drafts shown in conversations?**

Individual messages will rarely be trashed and mostly this happens by accident. Showing trashed messages in conversations makes it easier to find them back.

You can permanently delete a message using the message three-dots *delete* menu, which will remove the message from the conversation. Note that this irreversible.

Similarly, drafts are shown in conversations to find them back in the context where they belong. It is easy to read through the received messages before continuing to write the draft later.

<br />

<a name="faq136"></a>
**(136) How can I delete an account/identity/folder?**

Deleting an account/identity/folder is a little bit hidden to prevent accidents.

* Compte : Paramètres > Configuration manuelle > Comptes > Appuyez sur compte
* Identité : Paramètres > Configuration manuelle > Identités > appuyez sur l'identité
* Folder: Long press the folder in the folder list > Edit properties

In the three-dots overflow menu at the top right there is an item to delete the account/identity/folder.

<br />

<a name="faq137"></a>
**(137) How can I reset 'Don't ask again'?**

You can reset all questions set to be not asked again in the miscellaneous settings.

<br />

<a name="faq138"></a>
**(138) Can you add calendar/contact/tasks/notes management?**

Calendar, contact, task and note management can better be done by a separate, specialized app. Note that FairEmail is a specialized email app, not an office suite.

Also, I prefer to do a few things very well, instead of many things only half. Moreover, from a security perspective, it is not a good idea to grant many permissions to a single app.

You are advised to use the excellent, open source [DAVx⁵](https://f-droid.org/packages/at.bitfire.davdroid/) app to synchronize/manage your calendars/contacts.

Most providers support exporting your contacts. Please [see here](https://support.google.com/contacts/answer/1069522) about how you can import contacts if synchronizing is not possible.

Note that FairEmail does support replying to calendar invites (a pro feature) and adding calendar invites to your personal calendar.

<br />

<a name="faq83"></a>
<a name="faq139"></a>
**(139) How do I fix 'User is authenticated but not connected'?**

In fact this Microsoft Exchange specific error is an incorrect error message caused by a bug in older Exchange server software.

The error *User is authenticated but not connected* might occur if:

* Push messages are enabled for too many folders: see [this FAQ](#user-content-faq23) for more information and a workaround
* The account password was changed: changing it in FairEmail too should fix the problem
* An alias email address is being used as username instead of the primary email address
* An incorrect login scheme is being used for a shared mailbox: the right scheme is *username@domain\SharedMailboxAlias*

The shared mailbox alias will mostly be the email address of the shared account, like this:

```
you@example.com\shared@example.com
```

Note that it should be a backslash and not a forward slash.

When using a shared mailbox, you'll likely want to enable the option *Synchronize shared folder lists* in the receive settings.

<br />

<a name="faq140"></a>
**(140) Why does the message text contain strange characters?**

Displaying strange characters is almost always caused by specifying no or an invalid character encoding by the sending software. FairEmail will assume [ISO 8859-1](https://en.wikipedia.org/wiki/ISO/IEC_8859-1) when no character set or when [US-ASCII](https://en.wikipedia.org/wiki/ASCII) was specified. Other than that there is no way to reliably determine the correct character encoding automatically, so this cannot be fixed by FairEmail. The right action is to complain to the sender.

<br />

<a name="faq141"></a>
**(141) How can I fix 'A drafts folder is required to send messages'?**

To store draft messages a drafts folder is required. In most cases FairEmail will automatically select the drafts folders on adding an account based on [the attributes](https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml) the email server sends. However, some email servers are not configured properly and do not send these attributes. In this case FairEmail tries to identify the drafts folder by name, but this might fail if the drafts folder has an unusual name or is not present at all.

You can fix this problem by manually selecting the drafts folder in the account settings (Settings, tap Manual setup, tap Accounts, tap account, at the bottom). If there is no drafts folder at all, you can create a drafts folder by tapping on the '+' button in the folder list of the account (tap on the account name in the navigation menu).

Some providers, like Gmail, allow enabling/disabling IMAP for individual folders. So, if a folder is not visible, you might need to enable IMAP for the folder.

Quick link for Gmail (will work on a desktop computer only): [https://mail.google.com/mail/u/0/#settings/labels](https://mail.google.com/mail/u/0/#settings/labels)

<br />

<a name="faq142"></a>
**(142) How can I store sent messages in the inbox?**

Generally, it is not a good idea to store sent messages in the inbox because this is hard to undo and could be incompatible with other email clients.

That said, FairEmail is able to properly handle sent messages in the inbox. FairEmail will mark outgoing messages with a sent messages icon for example.

The best solution would be to enable showing the sent folder in the unified inbox by long pressing the sent folder in the folder list and enabling *Show in unified inbox*. This way all messages can stay where they belong, while allowing to see both incoming and outgoing messages at one place.

If this is not an option, you can [create a rule](#user-content-faq71) to automatically move sent messages to the inbox or set a default CC/BCC address in the advanced identity settings to send yourself a copy.

<br />

<a name="faq143"></a>
**~~(143) Can you add a trash folder for POP3 accounts?~~**

[POP3](https://en.wikipedia.org/wiki/Post_Office_Protocol) is a very limited protocol. Basically only messages can be downloaded and deleted from the inbox. It is not even possible to mark a message read.

Since POP3 does not allow access to the trash folder at all, there is no way to restore trashed messages.

Note that you can hide messages and search for hidden messages, which is similar to a local trash folder, without suggesting that trashed messages can be restored, while this is actually not possible.

Version 1.1082 added a local trash folder. Note that trashing a message will permanently remove it from the server and that trashed messages cannot be restored to the server anymore.

<br />

<a name="faq144"></a>
**(144) How can I record voice notes?**

To record voice notes you can press this icon in the bottom action bar of the message composer:

![Image externe](https://github.com/M66B/FairEmail/blob/master/images/baseline_record_voice_over_black_48dp.png)

This requires a compatible audio recorder app to be installed. In particular [this common intent](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION) needs to be supported.

For example [this audio recorder](https://f-droid.org/app/com.github.axet.audiorecorder) is compatible.

Voice notes will automatically be attached.

<br />

<a name="faq145"></a>
**(145) How can I set a notification sound for an account, folder or sender?**

Account:

* Activer *Séparer les notifications* dans les paramètres de compte avancé (Paramètres, appui sur Configuration manuelle, appui sur Comptes, appui sur compte, appui sur Avancé)
* Long press the account in the account list (Settings, tap Manual setup, tap Accounts) and select *Edit notification channel* to change the notification sound

Folder:

* Long press the folder in the folder list and select *Create notification channel*
* Long press the folder in the folder list and select *Edit notification channel* to change the notification sound

Sender:

* Open a message from the sender and expand it
* Expand the addresses section by tapping on the down arrow
* Tap on the bell icon to create or edit a notification channel and to change the notification sound

The order of precendence is: sender sound, folder sound, account sound and default sound.

Setting a notification sound for an account, folder or sender requires Android 8 Oreo or later and is a pro feature.

<br />

<a name="faq146"></a>
**(146) How can I fix incorrect message times?**

Since the sent date/time is optional and can be manipulated by the sender, FairEmail uses the server received date/time by default.

Sometimes the server received date/time is incorrect, mostly because messages were incorrectly imported from another server and sometimes due to a bug in the email server.

In these rare cases, it is possible to let FairEmail use either the date/time from the *Date* header (sent time) or from the *Received* header as a workaround. This can be changed in the advanced account settings: Settings, tap Manual setup, tap Accounts, tap account, tap Advanced.

This will not change the time of already synchronized messages. To solve this, long press the folder(s) in the folder list and select *Delete local messages* and *Synchronize now*.

<br />

<a name="faq147"></a>
**(147) What should I know about third party versions?**

You likely came here because you are using a third party build of FairEmail.

There is **only support** on the latest Play store version, the latest GitHub release and the F-Droid build, but **only if** the version number of the F-Droid build is the same as the version number of the latest GitHub release.

F-Droid builds irregularly, which can be problematic when there is an important update. Therefore you are advised to switch to the GitHub release.

The F-Droid version is built from the same source code, but signed differently. This means that all features are available in the F-Droid version too, except for using the Gmail quick setup wizard because Google approved (and allows) one app signature only. For all other email providers, OAuth access is only available in Play Store versions and Github releases, as the email providers only permit the use of OAuth for official builds.

Note that you'll need to uninstall the F-Droid build first before you can install a GitHub release because Android refuses to install the same app with a different signature for security reasons.

Note that the GitHub version will automatically check for updates. When desired, this can be turned off in the miscellaneous settings.

Please [see here](https://github.com/M66B/FairEmail/blob/master/README.md#user-content-downloads) for all download options.

If you have a problem with the F-Droid build, please check if there is a newer GitHub version first.

<br />

<a name="faq148"></a>
**(148) How can I use an Apple iCloud account?**

There is a built-in profile for Apple iCloud, so you should be able to use the quick setup wizard (other provider). If needed you can find the right settings [here](https://support.apple.com/en-us/HT202304) to manually set up an account.

When using two-factor authentication you might need to use an [app-specific password](https://support.apple.com/en-us/HT204397).

<br />

<a name="faq149"></a>
**(149) How does the unread message count widget work?**

The unread message count widget shows the number of unread messages either for all accounts or for a selected account, but only for the folders for which new message notifications are enabled.

Tapping on the notification will synchronize all folders for which synchronization is enabled and will open:

* the start screen when all accounts were selected
* a folder list when a specific account was selected and when new message notifications are enabled for multiple folders
* a list of messages when a specific account was selected and when new message notifications are enabled for one folder

<br />

<a name="faq150"></a>
**(150) Can you add cancelling calendar invites?**

Cancelling calendar invites (removing calendar events) requires write calendar permission, which will result in effectively granting permission to read and write *all* calendar events of *all* calendars.

Given the goal of FairEmail, privacy and security, and given that it is easy to remove a calendar event manually, it is not a good idea to request this permission for just this reason.

Inserting new calendar events can be done without permissions with special [intents](https://developer.android.com/guide/topics/providers/calendar-provider.html#intents). Unfortunately, there exists no intent to delete existing calendar events.

<br />

<a name="faq151"></a>
**(151) Can you add backup/restore of messages?**

An email client is meant to read and write messages, not to backup and restore messages. Note that breaking or losing your device, means losing your messages!

Instead, the email provider/server is responsible for backups.

If you want to make a backup yourself, you could use a tool like [imapsync](https://imapsync.lamiral.info/).

Since version 1.1556 it is possible to export all messages of a POP3 folder in mbox format according to [RFC4155](https://www.ietf.org/rfc/rfc4155.txt), which might be useful to save sent messages if the email server doesn't.

If you want to import an mbox file to an existing email account, you can use Thunderbird on a desktop computer and the [ImportExportTools](https://addons.thunderbird.net/nl/thunderbird/addon/importexporttools/) add-on.

<br />

<a name="faq152"></a>
**(152) How can I insert a contact group?**

You can insert the email addresses of all contacts in a contact group via the three dots menu of the message composer.

You can define contact groups with the Android contacts app, please see [here](https://support.google.com/contacts/answer/30970) for instructions.

<br />

<a name="faq153"></a>
**(153) Why does permanently deleting Gmail message not work?**

You might need to change [the Gmail IMAP settings](https://mail.google.com/mail/u/0/#settings/fwdandpop) on a desktop browser to make it work:

* When I mark a message in IMAP as deleted: Auto-Expunge off - Wait for the client to update the server.
* When a message is marked as deleted and expunged from the last visible IMAP folder: Immediately delete the message forever

Note that archived messages can be deleted only by moving them to the trash folder first.

Some background: Gmail seems to have an additional message view for IMAP, which can be different from the main message view.

Another oddity is that a star (favorite message) set via the web interface cannot be removed with the IMAP command

```
STORE <message number> -FLAGS (\Flagged)
```

On the other hand, a star set via IMAP is being shown in the web interface and can be removed via IMAP.

<br />

<a name="faq154"></a>
**~~(154) Can you add favicons as contact photos?~~**

~~Besides that a [favicon](https://en.wikipedia.org/wiki/Favicon) might be shared by many email addresses with the same domain name~~ ~~and therefore is not directly related to an email address, favicons can be used to track you.~~

<br />

<a name="faq155"></a>
**(155) What is a winmail.dat file?**

A *winmail.dat* file is sent by an incorrectly configured Outlook client. It is a Microsoft specific file format ([TNEF](https://en.wikipedia.org/wiki/Transport_Neutral_Encapsulation_Format)) containing a message and possibly attachments.

You can find some more information about this file [here](https://support.mozilla.org/en-US/kb/what-winmaildat-attachment).

You can view it with for example the Android app [Letter Opener](https://play.google.com/store/apps/details?id=app.letteropener).

<br />

<a name="faq156"></a>
**(156) How can I set up an Office 365 account?**

An Office 365 account can be set up via the quick setup wizard and selecting *Office 365 (OAuth)*.

If the wizard ends with *AUTHENTICATE failed*, IMAP and/or SMTP might be disabled for the account. In this case you should ask the administrator to enable IMAP and SMTP. The procedure is documented [here](https://docs.microsoft.com/en-in/exchange/troubleshoot/configure-mailboxes/pop3-imap-owa-activesync-office-365).

If you've enabled *security defaults* in your organization, you might need to enable the SMTP AUTH protocol. Please [see here](https://docs.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/authenticated-client-smtp-submission) about how to.

<br />

<a name="faq157"></a>
**(157) How can I set up an Free.fr account?**

Veuillez [voir ici](https://free.fr/assistance/597.html) pour les instructions.

**SMTP est désactivé par défaut**, veuillez [voir ici](https://free.fr/assistance/2406.html) comment il peut être activé.

Veuillez [voir ici](http://jc.etiemble.free.fr/abc/index.php/trucs-astuces/configurer-smtp-free-fr) pour un guide détaillé.

<br />

<a name="faq103"></a>
<a name="faq158"></a>
**(158) Which camera / audio recorder do you recommend?**

To take photos and to record audio a camera and an audio recorder app are needed. The following apps are open source cameras and audio recorders:

* [Open Camera](https://play.google.com/store/apps/details?id=net.sourceforge.opencamera) ([F-Droid](https://f-droid.org/en/packages/net.sourceforge.opencamera/))
* [Audio Recorder version 3.3.24+](https://play.google.com/store/apps/details?id=com.github.axet.audiorecorder) ([F-Droid](https://f-droid.org/packages/com.github.axet.audiorecorder/))

To record voice notes, etc, the audio recorder needs to support [MediaStore.Audio.Media.RECORD_SOUND_ACTION](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media#RECORD_SOUND_ACTION). Oddly, most audio recorders seem not to support this standard Android action.

<br />

<a name="faq159"></a>
**(159) What are Disconnect's tracker protection lists?**

Please see [here](https://disconnect.me/trackerprotection) for more information about Disconnect's tracker protection lists.

After downloading the lists in the privacy settings, the lists can optionally be used:

* to warn about tracking links on opening links
* to recognize tracking images in messages

Tracking images will be disabled only if the corresponding main 'disable' option is enabled.

Tracking images will not be recognized when the domain is classified as '*Content*', see [here](https://disconnect.me/trackerprotection#trackers-we-dont-block) for more information.

This command can be sent to FairEmail from an automation app to update the protection lists:

```
(adb shell) am start-foreground-service -a eu.faircode.email.DISCONNECT.ME
```

Updating once a week will probably be sufficient, please see [here](https://github.com/disconnectme/disconnect-tracking-protection/commits/master) for recent lists changes.

<br />

<a name="faq160"></a>
**(160) Can you add permanent deletion of messages without confirmation?**

Permanent deletion means that messages will *irreversibly* be lost, and to prevent this from happening accidentally, this always needs to be confirmed. Even with a confirmation, some very angry people who lost some of their messages through their own fault contacted me, which was a rather unpleasant experience :-(

Advanced: the IMAP delete flag in combination with the EXPUNGE command is not supportable because both email servers and not all people can handle this, risking unexpected loss of messages. A complicating factor is that not all email servers support [UID EXPUNGE](https://tools.ietf.org/html/rfc4315).

From version 1.1485 it is possible to temporarily enable debug mode in the miscellaneous settings to disable expunging messages. Note that messages with a *\Deleted* flag will not be shown in FairEmail.

<br />

<a name="faq161"></a>
**(161) Can you add a setting to change the primary and accent color?***

If I could, I would add a setting to select the primary and accent color right away, but unfortunately Android themes are fixed, see for example [here](https://stackoverflow.com/a/26511725/1794097), so this is not possible.

<br />

<a name="faq162"></a>
**(162) Is IMAP NOTIFY supported?***

Yes, [IMAP NOTIFY](https://tools.ietf.org/html/rfc5465) has been supported since version 1.1413.

IMAP NOTIFY support means that notifications for added, changed or deleted messages of all *subscribed* folders will be requested and if a notification is received for a subscribed folder, that the folder will be synchronized. Synchronization for subscribed folders can therefore be disable, saving folder connections to the email server.

**Important**: push messages (=always sync) for the inbox and subscription management (receive settings) need to be enabled.

**Important**: most email servers do not support this! You can check the log via the navigation menu if an email server supports the NOTIFY capability.

<br />

<a name="faq163"></a>
**(163) What is message classification?**

*This is an experimental feature!*

Message classification will attempt to automatically group emails into classes, based on their contents, using [Bayesian statistics](https://en.wikipedia.org/wiki/Bayesian_statistics). In the context of FairEmail, a folder is a class. So, for example, the inbox, the spam folder, a 'marketing' folder, etc, etc.

You can enable message classification in the miscellaneous settings. This will enable 'learning' mode only. The classifier will 'learn' from new messages in the inbox and spam folder by default. The folder property *Classify new messages in this folder* will enable or disable 'learning' mode for a folder. You can clear local messages (long press a folder in the folder list of an account) and synchronize the messages again to classify existing messages.

Each folder has an option *Automatically move classified messages to this folder* ('auto classification' for short). When this is turned on, new messages in other folders which the classifier thinks belong to that folder will be automatically moved.

The option *Use local spam filter* in the report spam dialog will turn on message classification in the miscellaneous settings and auto classification for the spam folder. Please understand that this is not a replacement for the spam filter of the email server and can result in [false positives and false negatives](https://en.wikipedia.org/wiki/False_positives_and_false_negatives). See also [this FAQ](#user-content-faq92).

A practical example: suppose there is a folder 'marketing' and auto message classification is enabled for this folder. Each time you move a message into this folder you'll train FairEmail that similar messages belong in this folder. Each time you move a message out of this folder you'll train FairEmail that similar messages do not belong in this folder. After moving some messages into the 'marketing' folder, FairEmail will start moving similar messages automatically into this folder. Or, the other way around, after moving some messages out of the 'marketing' folder, FairEmail will stop moving similar messages automatically into this folder. This will work best with messages with similar content (email addresses, subject and message text).

Classification should be considered as a best guess - it might be a wrong guess, or the classifier might not be confident enough to make any guess. If the classifier is unsure, it will simply leave an email where it is.

To prevent the email server from moving a message into the spam folder again and again, auto classification out of the spam folder will not be done.

The message classifier calculates the probability a message belongs in a folder (class). There are two options in the miscellaneous settings which control if a message will be automatically moved into a folder, provided that auto classification is enabled for the folder:

* *Minimum class probability*: a message will only be moved when the confidence it belongs in a folder is greater than this value (default 15 %)
* *Minimum class difference*: a message will only be moved when the difference in confidence between one class and the next most likely class is greater than this value (default 50 %)

Both conditions must be satisfied before a message will be moved.

Considering the default option values:

* Apples 40 % and bananas 30 % would be disregarded because the difference of 25 % is below the minimum of 50 %
* Apples 10 % and bananas 5 % would be disregarded because the probability for apples is below the minimum of 15 %
* Apples 50 % and bananas 20 % would result in selecting apples

Classification is optimized to use as little resources as possible, but will inevitably use some extra battery power.

You can delete all classification data by turning classification in the miscellaneous settings three times off.

[Filter rules](#user-content-faq71) will be executed before classification.

Message classification is a pro feature, except for the spam folder.

<br />

<a name="faq164"></a>
**(164) Can you add customizable themes?**

Unfortunately, Android [does not support](https://stackoverflow.com/a/26511725/1794097) dynamic themes, which means all themes need [to be predefined](https://github.com/M66B/FairEmail/blob/master/app/src/main/res/values/styles.xml).

Since for each theme there needs to be a light, dark and black variant, it is not feasible to add for each color combination (literally millions) a predefined theme.

Moreover, a theme is more than just a few colors. For example themes with a yellow accent color use a darker link color for enough contrast.

The theme colors are based on the color circle of [Johannes Itten](https://en.wikipedia.org/wiki/Johannes_Itten).

<br />

<a name="faq165"></a>
**(165) Is Android Auto supported?**

Yes, Android Auto is supported, but only with the GitHub version, please [see here](https://forum.xda-developers.com/t/app-5-0-fairemail-fully-featured-open-source-privacy-oriented-email-app.3824168/post-83801249) about why.

For notification (messaging) support you'll need to enable the following notification options:

* *Use Android 'messaging style' notification format*
* Notification actions: *Direct reply* and (mark as) *Read*

You can enable other notification actions too, if you like, but they are not supported by Android Auto.

The developers guide is [here](https://developer.android.com/training/cars/messaging).

<br />

<a name="faq166"></a>
**(166) Can I snooze a message across multiple devices?**

First of all, there is no standard for snoozing messages, so all snooze implementations are custom solutions.

Some email providers, like Gmail, move snoozed messages to a special folder. Unfortunately, third party apps have no access to this special folder.

Moving a message to another folder and back might fail and might not be possible if there is no internet connection. This is problematic because a message can be snoozed only after moving the message.

To prevent these issues, snoozing is done locally on the device by hiding the message while it is snoozing. Unfortunately, it is not possible to hide messages on the email server too.

<br />

<h2><a name="get-support"></a>Obtenir de l'aide</h2>

FairEmail est pris en charge uniquement sur des smartphones et tablettes Android et sur ChromeOS.

Only the latest Play store version and latest GitHub release are supported. The F-Droid build is supported only if the version number is the same as the version number of the latest GitHub release. This also means that downgrading is not supported.

Il n'y a pas de support sur des choses qui ne sont pas directement liées à FairEmail.

Il n'y a pas de soutien pour construire et développer des choses par soi-même.

Les fonctionnalités demandées devraient :

* Être utile à la plupart des gens
* Ne pas compliquer l'utilisation de FairEmail
* S’inscrire dans la philosophie de FairEmail (respect de la vie privée, sécurité)
* Respecter les normes communes (IMAP, SMTP, etc.)

Les caractéristiques ne répondant pas à ces exigences seront probablement rejetées. Cela permet également de maintenir la maintenance et le soutien à long terme.

Si vous avez une question, si vous souhaitez demander une fonctionnalité ou signaler un bug, **veuillez utiliser ce formulaire**

Les questions GitHub sont désactivées en raison d'une mauvaise utilisation fréquente.

<br />

Copyright &copy; 2018-2021 Marcel Bokhorst.
