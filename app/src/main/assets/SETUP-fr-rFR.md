# Aide pour la configuration

La configuration de FairEmail est assez simple. Vous devrez ajouter au moins un compte pour recevoir des e-mails et au moins une identité si vous souhaitez en envoyer. L’assistant de configuration rapide ajoutera un compte et une identité en une seule opération pour la plupart des principaux fournisseurs de messagerie.

## Pré-requis

Une connexion Internet est nécessaire pour configurer les comptes et les identités.

## Assistant de configuration rapide

Il vous suffit de sélectionner le fournisseur approprié ou *Autre fournisseur*, d'entrer votre nom, votre adresse e-mail, votre mot de passe et d'appuyer sur *Vérifier*.

Ceci fonctionnera pour la plupart des fournisseurs de messagerie.

Si la configuration rapide ne fonctionne pas, vous devrez configurer un compte et une identité manuellement, voir ci-après pour les instructions.

## Configurer un compte - pour recevoir des e-mails

Pour ajouter un compte, appuyez sur *Configuration manuelle et plus d'options*, appuyez sur *Comptes*, appuyez sur le bouton « Plus » en bas et sélectionnez IMAP (ou POP3). Sélectionnez un fournisseur dans la liste, entrez le nom d’utilisateur qui est en général votre adresse e-mail et saisissez votre mot de passe. Appuyez sur *Vérifier* pour permettre à FairEmail de se connecter au serveur de messagerie et de récupérer la liste des dossiers système. Après avoir vérifié les dossiers système sélectionnés, vous pouvez ajouter le compte en appuyant sur *Enregistrer*.

Si votre fournisseur n'est pas dans la liste des fournisseurs (il y a des milliers de fournisseurs), sélectionnez *Personnalisé*. Entrez le nom de domaine, par exemple *gmail.com* et appuyez sur *Obtenir les paramètres*. Si votre fournisseur gère l'[auto-découverte](https://tools.ietf.org/html/rfc6186), FairEmail remplira le nom d'hôte et le numéro de port, sinon recherchez le nom d'hôte IMAP, le numéro de port et le protocole de chiffrement (SSL/TLS ou STARTTLS) dans les instructions de configuration de votre fournisseur. Pour plus d'informations, veuillez voir [ici](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configurer une identité - pour envoyer des e-mails

De même, pour ajouter une identité, appuyez sur *Configuration manuelle et plus d'options*, appuyez sur *Identités* et appuyez sur le bouton « Plus » en bas. Entrez le nom que vous souhaitez voir apparaître dans le champ « De : » des e-mails que vous enverrez et sélectionnez un compte lié. Appuyez sur *Enregistrer* pour ajouter l'identité.

Si le compte a été configuré manuellement, vous devrez probablement également configurer l’identité manuellement. Entrez le nom de domaine, par exemple *gmail.com* et appuyez sur *Obtenir les paramètres*. Si votre fournisseur gère l'[auto-découverte](https://tools.ietf.org/html/rfc6186), FairEmail remplira le nom d'hôte et le numéro de port, sinon recherchez le nom d'hôte SMTP, le numéro de port et le protocole de chiffrement (SSL/TLS ou STARTTLS) dans les instructions de configuration de votre fournisseur.

Voir [cette FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sur l'utilisation des alias.

## Accorder les autorisations - pour accéder aux informations des contacts

Si vous souhaitez rechercher des adresses e-mail, afficher les photos de contact, etc. vous devrez accorder à FairEmail l’autorisation de lire les contacts. Appuyez simplement sur *Accorder les autorisations* et sélectionnez *Autoriser*.

## Configurer les optimisations de la batterie - pour recevoir des e-mails en continu

Dans ses versions récentes, Android mettra en veille les applications lorsque l’écran sera éteint pendant un certain temps afin de réduire l’utilisation de la batterie. Si vous souhaitez recevoir les nouveaux e-mails sans retard, vous devrez désactiver les optimisations de la batterie pour FairEmail. Appuyez sur *Gérer* et suivez les instructions.

## Questions ou problèmes

Si vous avez une question ou un problème, veuillez [voir ici](https://github.com/M66B/FairEmail/blob/master/FAQ.md) pour obtenir de l'aide.