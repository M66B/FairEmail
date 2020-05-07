# Aide pour la configuration

La configuration de FairEmail est assez simple. Vous devrez ajouter au moins un compte pour recevoir des e-mails et au moins une identité si vous souhaitez en envoyer. L’assistant de configuration rapide ajoutera un compte et une identité en une seule opération pour la plupart des principaux fournisseurs de messagerie.

## Pré-requis

Une connexion Internet est nécessaire pour configurer les comptes et les identités.

## Assistant de configuration rapide

Saisissez simplement votre nom, votre adresse e-mail et votre mot de passe et appuyez sur *Vérifier*.

Ceci fonctionnera pour la plupart des principaux fournisseurs de messagerie.

Si la configuration rapide ne fonctionne pas, vous devrez utiliser une autre méthode pour configurer un compte et une identité, voir ci-après pour les instructions.

## Configurer un compte - pour recevoir des e-mails

Pour ajouter un compte, appuyez sur *Gérer les comptes* puis sur le bouton orange *Ajouter* en bas. Sélectionnez un fournisseur dans la liste, entrez le nom d’utilisateur, qui est en général votre adresse e-mail, et saisissez votre mot de passe. Appuyez sur *Vérifier* pour permettre à FairEmail de se connecter au serveur de messagerie et de récupérer la liste des dossiers système. Après avoir vérifié les dossiers système sélectionnés, vous pouvez ajouter le compte en appuyant sur *Enregistrer*.

Si votre fournisseur de messagerie n’apparaît pas dans la liste, sélectionnez *Personnalisé*. Entrez le nom de domaine, par exemple *gmail.com* et appuyez sur *Obtenir les paramètres*. Si votre fournisseur prend en charge l’[auto-découverte](https://tools.ietf.org/html/rfc6186), FairEmail remplira le nom d’hôte et le numéro de port, sinon, référez-vous aux instructions de configuration de votre fournisseur pour connaître le nom d’hôte IMAP, le numéro de port ainsi que le protocole (SSL/TLS ou STARTTLS). Pour plus d'informations, veuillez voir [ici](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configurer une identité - pour envoyer des e-mails

De même, pour ajouter une identité, appuyez sur *Gérer les identités* puis sur le bouton orange *Ajouter* en bas. Entrez le nom que vous souhaitez voir apparaître dans le champ « De : » des e-mails que vous enverrez et sélectionnez un compte lié. Appuyez sur *Enregistrer* pour ajouter l'identité.

Si le compte a été configuré manuellement, vous devrez probablement également configurer l’identité manuellement. Entrez le nom de domaine, par exemple *gmail.com* et appuyez sur *Obtenir les paramètres*. Si votre fournisseur prend en charge l’[auto-découverte](https://tools.ietf.org/html/rfc6186), FairEmail remplira le nom d’hôte et le numéro de port, sinon, référez-vous aux instructions de configuration de votre fournisseur pour connaître le nom d’hôte SMTP, le numéro de port ainsi que le protocole (SSL/TLS ou STARTTLS).

Voir [cette FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sur l'utilisation des alias.

## Accorder les autorisations - pour accéder aux informations des contacts

Si vous souhaitez rechercher des adresses e-mail, afficher les photos de contact, etc. vous devrez accorder à FairEmail l’autorisation de lire les contacts. Appuyez simplement sur *Accorder les autorisations* et sélectionnez *Autoriser*.

## Configurer les optimisations de la batterie - pour recevoir des e-mails en continu

Dans ses versions récentes, Android mettra en veille les applications lorsque l’écran sera éteint pendant un certain temps afin de réduire l’utilisation de la batterie. Si vous souhaitez recevoir les nouveaux e-mails sans retard, vous devrez désactiver les optimisations de la batterie pour FairEmail. Appuyez sur *Désactiver les optimisations de batterie* et suivez les instructions.

## Questions ou problèmes

Si vous avez une question ou un problème, veuillez [voir ici](https://github.com/M66B/FairEmail/blob/master/FAQ.md) pour obtenir de l'aide.