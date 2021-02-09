# Aide de configuration

La configuration de FairEmail est assez simple. Vous devrez ajouter au moins un compte pour recevoir des courriels et au moins une identité si vous voulez envoyer des courriels. L’assistant de configuration rapide ajoutera un compte et une identité en une seule opération pour la plupart des principaux fournisseurs de messagerie.

## Prérequis

Une connexion Internet est nécessaire pour configurer les comptes et les identités.

## Configuration rapide

Il vous suffit de sélectionner le fournisseur approprié ou *Autre fournisseur*, d'entrer votre nom, votre adresse courriel, votre mot de passe et d'appuyer sur *Vérifier*.

Ceci fonctionnera pour la plupart des fournisseurs de messagerie.

Si la configuration rapide ne fonctionne pas, vous devrez configurer un compte et une identité manuellement, voir ci-après pour les instructions.

## Configuration d’un compte – recevoir des courriels

Pour ajouter un compte, appuyez sur *Configuration manuelle et plus d'options*, appuyez sur *Comptes*, appuyez sur le bouton « Plus » en bas et sélectionnez IMAP (ou POP3). Sélectionnez un fournisseur dans la liste, entrez le nom d’utilisateur, qui est en général votre adresse courriel, ainsi que votre mot de passe. Appuyez sur *Vérifier* pour permettre à FairEmail de se connecter au serveur de messagerie et de récupérer une liste des dossiers système. Après avoir examiné la sélection des dossiers système, vous pouvez ajouter le compte en appuyant sur *Enregistrer*.

Si votre fournisseur n'est pas dans la liste des fournisseurs, il y a des milliers de fournisseurs, sélectionnez *Personnaliser*. Entrez le nom de domaine, par exemple *gmail.com* et appuyez sur *Obtenir les paramètres*. Si votre fournisseur gère l'[auto-découverte](https://tools.ietf.org/html/rfc6186), FairEmail remplira le nom d'hôte et le numéro de port, sinon recherchez le nom d'hôte IMAP, le numéro de port et le protocole de chiffrement (SSL/TLS ou STARTTLS) dans les instructions de configuration de votre fournisseur. Pour plus d’informations, veuillez voir [ici](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configuration d’une identité – envoyer des courriels

De même, pour ajouter une identité, appuyez sur *Configuration manuelle et plus d'options*, appuyez sur *Identités* et appuyez sur le bouton « Plus » en bas. Entrez le nom que vous souhaitez voir apparaître dans le champ de : pour les courriels que vous envoyez et sélectionnez un compte lié. Appuyez sur *Enregistrer* pour ajouter l’identité.

Si le compte a été configuré manuellement, vous devez probablement configurer également l'identité manuellement. Entrez le nom de domaine, par exemple *gmail.com* et appuyez sur *Obtenir les paramètres*. Si votre fournisseur gère l'[auto-découverte](https://tools.ietf.org/html/rfc6186), FairEmail remplira le nom d'hôte et le numéro de port, sinon recherchez le nom d'hôte SMTP, le numéro de port et le protocole de chiffrement (SSL/TLS ou STARTTLS) dans les instructions de configuration de votre fournisseur.

Voir [cette FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sur l’utilisation des alias.

## Accorder les permissions – accéder aux informations de contact

Si vous souhaitez rechercher des adresses courriel, afficher les photos de contact, etc. vous devrez accorder à FairEmail l’autorisation de lire les contacts. Appuyez simplement sur *Accorder les autorisations* et sélectionnez *Autoriser*.

## Configurer les optimisations de pile – recevoir des courriels en continu

Sur les versions d’Android récentes, Android mettra les applications en veille lorsque l’écran sera éteint pendant un certain temps pour réduire l’utilisation de la pile. Si vous souhaitez recevoir de nouveaux courriels sans retard, vous devez désactiver les optimisations de pile pour FairEmail. Appuyez sur *Gérer* et suivez les instructions.

## Questions ou problèmes

Si vous avez une question ou un problème, veuillez [voir ici](https://github.com/M66B/FairEmail/blob/master/FAQ.md) pour obtenir de l’aide.