# Βοήθεια για τη ρύθμιση

Η ρύθμιση του FairEmail είναι αρκετά απλή. Θα χρειαστεί να προσθέσετε τουλάχιστον έναν λογαριασμό για να λάβετε email και τουλάχιστον μία ταυτότητα χρήστη εάν θέλετε να στείλετε email. Αυτός ο γρήγορος οδηγός ρύθμισης θα προσθέσει έναν λογαριασμό και μία ταυτότητα χρήστη ταυτόχρονα, τουλάχιστον για τους περισσότερους γνωστούς παρόχους υπηρεσιών email.

## Απαιτήσεις

Απαιτείται σύνδεση στο internet για να ρυθμιστούν νέοι λογαριασμοί και ταυτότητες χρήστη.

## Γρήγορη ρύθμιση

Απλώς εισαγάγετε το όνομά σας, τη διεύθυνση email σας, και τον κωδικό σας, και πατήστε *Πάμε*.

Αυτό θα λειτουργήσει με επιτυχία για τους περισσότερους γνωστούς παρόχους υπηρεσιών email.

Εάν η γρήγορη ρύθμιση αποτύχει, θα χρειαστεί να ρυθμίσετε έναν λογαριασμό και μία ταυτότητα χρήστη με διαφορετικό τρόπο, δείτε παρακάτω για λεπτομέρειες.

## Set up account - to receive email

To add an account, tap *Manage accounts* and tap the orange *add* button at the bottom. Select a provider from the list, enter the username, which is mostly your email address and enter your password. Tap *Check* to let FairEmail connect to the email server and fetch a list of system folders. After reviewing the system folder selection you can add the account by tapping *Save*.

If your provider is not in the list of providers, select *Custom*. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the hostname and port number, else check the setup instructions of your provider for the right IMAP hostname, port number and protocol (SSL/TLS or STARTTLS). For more about this, please see [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Set up identity - to send email

Similarly, to add an identity, tap *Manage identity* and tap the orange *add* button at the bottom. Enter the name you want to appear in the from address of the emails you send and select a linked account. Tap *Save* to add the identity.

If the account was configured manually, you likely need to configure the identity manually too. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the hostname and port number, else check the setup instructions of your provider for the right SMTP hostname, port number and protocol (SSL/TLS or STARTTLS).

See [this FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) about using aliases.

## Grant permissions - to access contact information

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant read contacts permission to FairEmail. Just tap *Grant permissions* and select *Allow*.

## Setup battery optimizations - to continuously receive emails

On recent Android versions, Android will put apps to sleep when the screen is off for some time to reduce battery usage. If you want to receive new emails without delays, you should disable battery optimizations for FairEmail. Tap *Disable battery optimizations* and follow the instructions.

## Questions or problems

If you have a question or problem, please [see here](https://github.com/M66B/FairEmail/blob/master/FAQ.md) for help.