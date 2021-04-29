# Ajutor pentru configurare

Configurarea FairEmail este relativ simplă. Va trebui să adăugați cel puțin un cont pentru a primi e-mail și cel puțin o identitate dacă doriți să trimiteți e-mail. Configurarea rapidă va permite adăugarea unui cont și a unei identități într-o singură operațiune pentru majoritatea furnizorilor principali.

## Precondiții

Pentru configurarea conturilor și a identităților este necesară o conexiune la internet.

## Configurare rapidă

Trebuie doar să selectați furnizorul corespunzător sau *Altul furnizor* și introduceți numele, adresa de e-mail și parola, apoi apăsați pe *Check*.

Această metodă este valabilă pentru majoritatea furnizorilor de e-mail.

Dacă configurarea rapidă nu funcționează, va trebui să configurați manual un cont și o identitate; consultați instrucțiunile de mai jos.

## Configurați un cont - pentru a primi e-mailuri

Pentru a adăuga un cont, atingeți *Configurare manuală și mai multe opțiuni*, apăsați *Conturi* și apăsați butonul "plus" din partea de jos și selectați IMAP (sau POP3). Selectați un furnizor din listă, introduceți numele de utilizator, care este în general adresa dvs. de e-mail și introduceți parola. Apăsați *Check* pentru a permite FairEmail să se conecteze la serverul de e-mail și să extragă o listă de dosare de sistem. După ce ați revizuit selecția dosarului de sistem, puteți adăuga contul atingând *Salvare*.

Dacă furnizorul dvs. nu se află în lista de furnizori, există mii de furnizori, selectați *Custom*. Introduceți numele domeniului, de exemplu *gmail.com* și apăsați *Obține setările*. Dacă furnizorul dvs. suportă [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail va completa numele gazdei și numărul de port, în caz contrar, verificați instrucțiunile de configurare ale furnizorului dumneavoastră pentru a afla numele de gazdă IMAP, numărul de port și protocolul de criptare (SSL/TLS sau STARTTLS). Pentru mai multe informații în acest sens, vă rugăm să consultați [ aici](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configurați identitatea - pentru a trimite e-mailuri

În mod similar, pentru a adăuga o identitate, apăsați *Configurare manuală și mai multe opțiuni*, apăsați *Identități* și apăsați butonul "plus" din partea de jos. Introduceți numele care doriți să fie afișat în adresa de la care trimiteți e-mailuri și selectați un cont asociat. Apăsați *Salvare* pentru a adăuga identitatea.

În cazul în care contul a fost configurat manual, probabil că trebuie să configurați și identitatea manual. Introduceți numele domeniului, de exemplu *gmail.com* și apăsați *Obține setările*. Dacă furnizorul dvs. suportă [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail va completa numele gazdei și numărul de port, în caz contrar, verificați instrucțiunile de configurare ale furnizorului dvs. pentru numele de gazdă SMTP, numărul de port și protocolul de criptare (SSL/TLS sau STARTTLS).

Vedeți [acest FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) despre utilizarea aliasurilor.

## Oferirea de permisiuni - pentru a accesa informațiile de contact

Dacă doriți să căutați adrese de e-mail, să afișați fotografii de contact, etc., va trebui să acordați permisiunea de a citi informații de contact pentru FairEmail. Trebuie doar să apăsați *Grant* și să selectați *Allow*.

## Configurați optimizări ale bateriei - pentru a primi continuu e-mailuri

Pe versiunile recente de Android, Android va pune aplicațiile în stare de repaus atunci când ecranul este oprit pentru o perioadă de timp, pentru a reduce consumul bateriei. Dacă doriți să primiți e-mailuri noi fără întârzieri, ar trebui să dezactivați optimizarea bateriei pentru FairEmail. Apăsați *Manage* și urmați instrucțiunile.

## Întrebări sau probleme

Dacă aveți o întrebare sau o problemă, vă rugăm să [vezi aici](https://github.com/M66B/FairEmail/blob/master/FAQ.md) pentru ajutor.