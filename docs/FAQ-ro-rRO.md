<a name="top"></a>

# Asistență FairEmail

Dacă aveți o nelămurire, vă rugăm să consultați mai întâi următoarele întrebări frecvente. [În partea de jos](#user-content-get-support), puteți afla cum să puneți alte întrebări, să solicitați caracteristici și să raportați erori.

Dacă aveți o problemă, vă rugăm să consultați mai întâi întrebările frecvente (FAQ) de mai jos. [În partea de jos veți afla](#user-content-get-support), cum să puneți mai multe întrebări, să solicitați caracteristici și să raportați erori.

## Index

* [Autorizarea conturilor](#user-content-authorizing-accounts)
* [Cum să...?](#user-content-howto)
* [Probleme cunoscute](#user-content-known-problems)
* [Funcțiile planificate](#user-content-planned-features)
* [Funcțiile solicitate frecvent](#user-content-frequently-requested-features)
* [Întrebări puse frecvent](#user-content-frequently-asked-questions)
* [Obțineți asistență](#user-content-get-support)

<h2><a name="authorizing-accounts"></a>Autorizarea conturilor</h2>

În majoritatea cazurilor, asistentul de configurare rapidă va fi capabil să identifice automat configurația corectă.

Dacă expertul de configurare rapidă eșuează, va fi necesar să configurați manual un cont (pentru a primi e-mailuri) și o identitate (pentru a trimite e-mailuri). Pentru aceasta veți avea nevoie de adresele și numerele de port ale serverelor IMAP și SMTP, dacă trebuie utilizat SSL/TLS sau STARTTLS și numele dvs. de utilizator (în general, dar nu întotdeauna, adresa dvs. de e-mail) și parola.

Căutarea *IMAP* și a numelui furnizorului este de cele mai multe ori suficientă pentru a găsi documentația corectă.

În unele cazuri, va trebui să activați accesul extern la contul dvs. și/sau să utilizați o parolă specială (aplicație), de exemplu atunci când este activată autentificarea cu doi factori.

Pentru autorizare:

* Gmail / G suite, vezi [întrebarea 6](#user-content-faq6)
* Outlook / Live / Hotmail, vedeți [întrebarea 14](#user-content-faq14)
* Office 365, consultați [întrebarea 14](#user-content-faq156)
* Microsoft Exchange, consultați [întrebarea 8](#user-content-faq8)
* Yahoo, AOL și Sky, vezi [întrebarea 88](#user-content-faq88)
* Apple iCloud, vezi [întrebarea 148](#user-content-faq148)
* Free.fr, a se vedea [întrebarea 157](#user-content-faq157)

Vă rugăm să consultați [aici](#user-content-faq22) pentru mesaje de eroare comune și soluții.

Întrebări conexe:

* [Este acceptat OAuth?](#user-content-faq111)
* [De ce nu este acceptat ActiveSync?](#user-content-faq133)

<a name="howto">

## Cum să...?

* Schimbați numele contului: Setări, apăsați Configurare manuală, apăsați Conturi, apăsați cont
* Modificați ținta de glisare stânga/dreapta: Setări, pagina de tabulare Comportament, Setați acțiunile de glisare
* Schimbarea parolei: Setări, apăsați Configurare manuală, apăsați Conturi, apăsați Conturi, apăsați Cont, schimbați parola
* Setați o semnătură: Setări, apăsați Configurare manuală, apăsați Identități, apăsați Identitate, Editați semnătura.
* Adăugați adrese CC și BCC: apăsați pe pictograma persoanelor de la sfârșitul subiectului
* Treceți la mesajul următor/precedent la arhivare/eliminare: în setările de comportament dezactivați *Închideți automat conversațiile* și selectați *Mergeți la conversația următoare/precedentă* pentru *La închiderea unei conversații*
* Adăugați un dosar în căsuța poștală unificată: apăsați lung dosarul din lista de dosare și bifați *Arată în căsuța poștală unificată*
* Adăugați un dosar în meniul de navigare: apăsați lung pe dosarul din lista de dosare și bifați *Afișează în meniul de navigare*
* Încărcați mai multe mesaje: apăsați lung pe un dosar din lista de dosare, selectați *Căutați mai multe mesaje*
* Ștergeți un mesaj, trecând peste coșul de gunoi: apăsați lung pe pictograma coșului de gunoi
* Ștergeți un cont/identitate: Setări, apăsați Configurare manuală, apăsați Conturi/Identități, apăsați contul/identitatea, pictograma coș de gunoi în dreapta sus
* Ștergerea unui dosar: apăsați lung pe dosar în lista de dosare, Editare proprietăți, pictograma coș de gunoi în dreapta sus
* Anulați trimiterea: Outbox, glisați mesajul din listă spre stânga sau spre dreapta
* Stocarea mesajelor trimise în căsuța de primire: vă rugăm să [vedeți acest FAQ](#user-content-faq142)
* Schimbați dosarele de sistem: Setări, apăsați pe Configurare manuală, apăsați pe Conturi, apăsați pe Cont, în partea de jos
* Setări de export/import: Setări, meniul de navigare (partea stângă)

<h2><a name="known-problems"></a>Probleme cunoscute</h2>

* ~~A [bug în Android 5.1 și 6](https://issuetracker.google.com/issues/37054851) face ca aplicațiile să afișeze uneori un format de timp greșit. Comutând setarea Android *Folosiți formatul 24 de ore* ar putea rezolva temporar problema. O soluție a fost adăugată.~~
* ~~Un [bug în Google Drive](https://issuetracker.google.com/issues/126362828) face ca fișierele exportate în Google Drive să fie goale. Google a rezolvat acest lucru.~~
* ~~A [bug în AndroidX](https://issuetracker.google.com/issues/78495471) face ca FairEmail să se blocheze ocazional la apăsarea lungă sau la glisare. Google a rezolvat acest lucru.~~
* ~~Un [bug în AndroidX ROOM](https://issuetracker.google.com/issues/138441698) provoacă uneori o blocare cu "*... Excepție în timpul calculării datelor live ale bazei de date ... Nu s-a putut citi rândul ...*". A fost adăugată o soluție de rezolvare.~~
* Un [bug în Android](https://issuetracker.google.com/issues/119872129) face ca FairEmail să se prăbușească cu "*... O notificare greșită a fost postată ...*" pe unele dispozitive o dată după ce ați actualizat FairEmail și ați apăsat pe o notificare.
* Un [bug din Android](https://issuetracker.google.com/issues/62427912) cauzează uneori o prăbușire cu "*... Înregistrarea activității nu a fost găsită pentru ...*" după actualizarea FairEmail. Reinstalarea ([sursa](https://stackoverflow.com/questions/46309428/android-activitythread-reportsizeconfigurations-causes-app-to-freeze-with-black)) ar putea rezolva problema.
* Un [bug din Android](https://issuetracker.google.com/issues/37018931) provoacă uneori o prăbușire cu *... InputChannel nu este inițializat ...* pe unele dispozitive.
* ~~Un [bug în LineageOS](https://review.lineageos.org/c/LineageOS/android_frameworks_base/+/265273) provoacă uneori o eroare cu *... java.lang.ArrayIndexOutOfOfBoundsException: length=...; index=... ...*.~~
* O eroare în Nova Launcher pe Android 5.x face ca FairEmail să se prăbușească cu un *java.lang.StackOverflowError* atunci când Nova Launcher are acces la serviciul de accesibilitate.
* ~~Selectorul de dosare nu afișează uneori niciun dosar din motive încă necunoscute. Acest lucru pare a fi rezolvat.~~
* ~~Un [bug în AndroidX](https://issuetracker.google.com/issues/64729576) face dificilă apucarea scroll-ului rapid. A fost adăugată o soluție de rezolvare.~~
* ~~Criptarea cu YubiKey duce la o buclă infinită. Acest lucru pare să fie cauzat de un [bug în OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2507).~~
* Derularea către o locație legată intern în mesajele originale nu funcționează. Acest lucru nu poate fi rezolvat deoarece vizualizarea mesajului original este inclusă într-o vizualizare cu derulare.
* O previzualizare a textului unui mesaj nu apare (întotdeauna) pe ceasurile Samsung, deoarece [setLocalOnly](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder.html#setLocalOnly(boolean)) pare să fie ignorat. Se știe că textele de previzualizare a mesajelor sunt afișate corect pe dispozitivele portabile Pebble 2, Fitbit Charge 3, Mi band 3 și Xiaomi Amazfit BIP. A se vedea, de asemenea, [acest FAQ](#user-content-faq126).
* Un [bug din Android 6.0](https://issuetracker.google.com/issues/37068143) provoacă o blocare cu *... Decalaj invalid: ... Intervalul valabil este ...* atunci când textul este selectat și atingerea în afara textului selectat. Această eroare a fost corectată în Android 6.0.1.
* Legăturile interne (cu ancoră) nu vor funcționa deoarece mesajele originale sunt afișate într-un WebView încorporat într-o vizualizare derulantă (lista de conversații). Aceasta este o limitare a sistemului Android care nu poate fi reparată sau evitată.
* Detectarea limbii [nu mai funcționează](https://issuetracker.google.com/issues/173337263) pe dispozitivele Pixel cu (actualizat la?) Android 11
* Un [bug din OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2688) provoacă semnături PGP invalide atunci când se utilizează un token hardware.

<h2><a name="planned-features"></a>Funcții planificate</h2>

* ~~Sincronizare la cerere (manuală)~~
* ~~Criptare semi-automată~~
* ~~Copiază mesajul~~
* ~~Stele colorate~~
* ~~Setări de notificare pentru fiecare dosar~~
* ~~Selectați imagini locale pentru semnături~~ (acest lucru nu va fi adăugat pentru că necesită gestionarea fișierelor de imagine și pentru că imaginile nu sunt afișate în mod implicit în majoritatea clienților de e-mail oricum)
* ~~Afișează mesajele care corespund unei reguli~~
* ~~[ManageSieve](https://tools.ietf.org/html/rfc5804)~~~ (nu există biblioteci Java întreținute cu o licență adecvată și fără dependențe și în afară de asta, FairEmail are propriile reguli de filtrare)
* ~~Cercetarea mesajelor cu/fără atașamente~~ (acest lucru nu poate fi adăugat deoarece IMAP nu acceptă căutarea de atașamente)
* ~~Căutați un dosar~~ (filtrarea unei liste de dosare ierarhice este problematică)
* ~~Sugestii de căutare~~
* ~~[Autocrypt Setup Message](https://autocrypt.org/autocrypt-spec-1.0.0.pdf) (secțiunea 4.4)~~ (IMO nu este o idee bună să lași un client de e-mail să se ocupe de cheile de criptare sensibile pentru un caz de utilizare excepțională, în timp ce OpenKeychain poate exporta și chei)
* ~~Dosare unificate generice~~
* ~~ Noile programe de notificare a mesajelor pentru fiecare cont~~ (implementate prin adăugarea unei condiții de timp la reguli, astfel încât mesajele să poată fi amânate în perioadele selectate)
* ~~Copiază conturile și identitățile~~
* ~~Micșorarea cu zoom~~ (nu este posibilă în mod fiabil într-o listă derulantă; în schimb, se poate mări vizualizarea completă a mesajului)
* ~~O vizualizare mai compactă a dosarelor~~
* ~~Compuneți liste și tabele~~ (acest lucru necesită un editor de text bogat, consultați [acest FAQ](#user-content-faq99))
* ~~Dimensiunea textului cu zoom ~~
* ~~Afișează GIF-uri~~
* ~~Themes~~~ (au fost adăugate o temă gri deschis și o temă întunecată pentru că asta este ceea ce majoritatea oamenilor par să vrea)
* ~~Condiție de timp pentru orice zi~~ (orice zi nu se potrivește cu adevărat în condiția de la/la data/ora)
* ~~Trimiteți ca atașament ~~
* ~~Widget pentru contul selectat~~
* ~~Amintiți-vă să atașați fișiere ~~
* ~~Selectați domeniile pentru care doriți să afișați imagini~~ (acest lucru va fi prea complicat de utilizat)
* ~~ Vizualizare unificată a mesajelor cu stele~~ (există deja o căutare specială pentru acest lucru)
* ~~Mutarea acțiunii de notificare~~
* ~~S/MIME suport~~
* ~~ Căutați setările ~~

Orice lucru de pe această listă este în ordine aleatorie și *poate* fi adăugat în viitorul apropiat.

<h2><a name="frequently-requested-features"></a>Funcții solicitate frecvent</h2>

Designul se bazează pe multe discuții și dacă doriți puteți discuta despre el [ și pe acest forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168) de asemenea. Scopul designului este de a fi minimalist (fără meniuri inutile, butoane, etc.) și de a nu distrage atenția (fără culori fanteziste, animații, etc.). Toate lucrurile expuse ar trebui să fie utile într-un fel sau altul și ar trebui să fie poziționate cu grijă pentru a fi utilizate cu ușurință. Fonturile, dimensiunile, culorile etc. ar trebui să fie de tip material design ori de câte ori este posibil.

<h2><a name="frequently-asked-questions"></a>Întrebări puse frecvent</h2>

* [(1) Ce permisiuni sunt necesare și de ce?](#user-content-faq1)
* [(2) De ce este afișată o notificare permanentă?](#user-content-faq2)
* [(3) Ce sunt operațiunile și de ce sunt în așteptare?](#user-content-faq3)
* [(4) Cum pot folosi un certificat de securitate invalid / o parolă goală / o conexiune în text simplu?](#user-content-faq4)
* [(5) Cum pot personaliza vizualizarea mesajelor?](#user-content-faq5)
* [(6) Cum pot să mă conectez la Gmail / G suite?](#user-content-faq6)
* [(7) De ce mesajele trimise nu apar (direct) în dosarul trimis?](#user-content-faq7)
* [(8) Pot folosi un cont Microsoft Exchange?](#user-content-faq8)
* [(9) Ce sunt identitățile / cum pot adăuga un alias?](#user-content-faq9)
* [~~(11) De ce nu este acceptat POP?~~](#user-content-faq11)
* [~~(10) Ce înseamnă "UIDPLUS nu este acceptat"?~~](#user-content-faq10)
* [(12) Cum funcționează criptarea/decriptarea?](#user-content-faq12)
* [(13) Cum funcționează căutarea pe dispozitiv/server?](#user-content-faq13)
* [(14) Cum pot configura un cont Outlook / Live / Hotmail?](#user-content-faq14)
* [(15) De ce se tot încarcă textul mesajului?](#user-content-faq15)
* [(16) De ce nu sunt sincronizate mesajele?](#user-content-faq16)
* [~~(17) De ce nu funcționează sincronizarea manuală?~~](#user-content-faq17)
* [(18) De ce nu este afișată întotdeauna previzualizarea mesajului?](#user-content-faq18)
* [(19) De ce sunt atât de scumpe funcțiile pro?](#user-content-faq19)
* [(20) Pot obține o rambursare?](#user-content-faq20)
* [(21) Cum pot activa lumina de notificare?](#user-content-faq21)
* [(22) Ce înseamnă eroare de cont/folder?](#user-content-faq22)
* [(23) De ce primesc alerte.. ?](#user-content-faq23)
* [(24) Ce sunt mesajele de navigare pe server?](#user-content-faq24)
* [(25) De ce nu pot selecta/opri/salva o imagine, un atașament sau un fișier?](#user-content-faq25)
* [(26) Pot să ajut la traducerea FairEmail în limba mea?](#user-content-faq26)
* [(27) Cum pot distinge între imaginile încorporate și cele externe?](#user-content-faq27)
* [(28) Cum pot gestiona notificările din bara de stare?](#user-content-faq28)
* [(29) Cum pot primi notificări de mesaje noi pentru alte dosare?](#user-content-faq29)
* [(30) Cum pot utiliza setările rapide furnizate?](#user-content-faq30)
* [(31) Cum pot utiliza comenzile rapide furnizate?](#user-content-faq31)
* [(32) Cum pot verifica dacă citirea unui e-mail este cu adevărat sigură?](#user-content-faq32)
* [(33) De ce nu funcționează adresele de expeditor editate?](#user-content-faq33)
* [(34) Cum se potrivesc identitățile?](#user-content-faq34)
* [(35) De ce ar trebui să fiu atent la vizualizarea imaginilor, a atașamentelor, a mesajului original și la deschiderea legăturilor?](#user-content-faq35)
* [(36) Cum sunt criptate fișierele de setări?](#user-content-faq36)
* [(37) Cum sunt stocate parolele?](#user-content-faq37)
* [(39) Cum pot reduce consumul de baterie al FairEmail?](#user-content-faq39)
* [(40) Cum pot reduce consumul de date al FairEmail?](#user-content-faq40)
* [(41) Cum pot remedia eroarea "Handshake failed"?](#user-content-faq41)
* [(42) Puteți adăuga un nou furnizor în lista de furnizori?](#user-content-faq42)
* [(43) Puteți arăta originalul ... ?](#user-content-faq43)
* [(44) Puteți afișa fotografiile / identitățile de contact în folderul trimis?](#user-content-faq44)
* [(45) Cum pot rezolva problema 'Această cheie nu este disponibilă. Pentru a o folosi, trebuie să o importați ca fiind a voastră!' ?](#user-content-faq45)
* [(46) De ce se reîmprospătează mereu lista de mesaje?](#user-content-faq46)
* [(47) Cum pot rezolva eroarea 'Nu există un cont principal sau nu există un dosar de ciorne' ?](#user-content-faq47)
* [~~(48) Cum pot rezolva eroarea 'Nici un cont primar sau nici un dosar de arhivă'?~~](#user-content-faq48)
* [(49) Cum se rezolvă problema 'O aplicație depășită a trimis o cale de fișier în loc de un flux de fișiere'?](#user-content-faq49)
* [(50) Puteți adăuga o opțiune pentru a sincroniza toate mesajele?](#user-content-faq50)
* [(51) Cum sunt sortate dosarele?](#user-content-faq51)
* [(52) De ce durează ceva timp până la reconectarea la un cont?](#user-content-faq52)
* [(53) Puteți lipi bara de acțiune a mesajelor în partea de sus/jos?](#user-content-faq53)
* [~~(54) Cum pot folosi un prefix de spațiu de nume?~~](#user-content-faq54)
* [(55) Cum pot marca toate mesajele ca fiind citite / muta sau șterge toate mesajele?](#user-content-faq55)
* [(56) Puteți adăuga suport pentru JMAP?](#user-content-faq56)
* [(57) Pot folosi HTML în semnături?](#user-content-faq57)
* [(58) Ce înseamnă o pictogramă de e-mail deschisă/închisă?](#user-content-faq58)
* [(59) Pot fi deschise mesajele originale în browser?](#user-content-faq59)
* [(60) Știați că...?](#user-content-faq60)
* [(61) De ce sunt afișate unele mesaje în umbră?](#user-content-faq61)
* [(62) Ce metode de autentificare sunt acceptate?](#user-content-faq62)
* [(63) Cum sunt redimensionate imaginile pentru a fi afișate pe ecrane?](#user-content-faq63)
* [~~(64) Puteți adăuga acțiuni personalizate pentru glisarea la stânga/dreapta?~~](#user-content-faq64)
* [(65) De ce unele atașamente sunt afișate estompate?](#user-content-faq65)
* [(66) Este FairEmail disponibil în Biblioteca de Familie Google Play?](#user-content-faq66)
* [(67) Cum pot să amân conversațiile?](#user-content-faq67)
* [~~(68) De ce Adobe Acrobat reader nu poate deschide atașamentele PDF / aplicațiile Microsoft nu pot deschide documentele atașate?~~](#user-content-faq68)
* [(69) Puteți adăuga derulare automată în sus la un mesaj nou?](#user-content-faq69)
* [(70) Când vor fi extinse automat mesajele?](#user-content-faq70)
* [(71) Cum se utilizează regulile de filtrare?](#user-content-faq71)
* [(72) Ce sunt conturile/identitățile primare?](#user-content-faq72)
* [(73) Este sigură/eficientă deplasarea mesajelor între conturi?](#user-content-faq73)
* [(74) De ce văd mesaje duplicate?](#user-content-faq74)
* [(75) Puteți face o versiune pentru iOS, Windows, Linux, etc?](#user-content-faq75)
* [(76) Ce face "Clear local messages"?](#user-content-faq76)
* [(77) De ce sunt afișate uneori mesajele cu o mică întârziere?](#user-content-faq77)
* [(78) Cum se utilizează orarele?](#user-content-faq78)
* [(79) Cum se utilizează sincronizarea la cerere (manuală)?](#user-content-faq79)
* [~~(80) Cum pot remedia eroarea 'Unable to load BODYSTRUCTURE'?~~](#user-content-faq80)
* [~~(81) Puteți face ca fundalul mesajului original să fie întunecat în tema întunecată?~~](#user-content-faq81)
* [(82) Ce este o imagine de urmărire?](#user-content-faq82)
* [(84) La ce servesc contactele locale?](#user-content-faq84)
* [(85) De ce nu este disponibilă o identitate?](#user-content-faq85)
* [~~(86) Ce sunt 'caracteristici suplimentare de confidențialitate'?~~](#user-content-faq86)
* [(87) Ce înseamnă 'acreditări invalide'?](#user-content-faq87)
* [(88) Cum pot folosi un cont Yahoo, AOL sau Sky?](#user-content-faq88)
* [(89) Cum pot trimite doar mesaje în text simplu?](#user-content-faq89)
* [(90) De ce unele texte sunt linkate, dar nu sunt un link?](#user-content-faq90)
* [~~(91) Puteți adăuga sincronizarea periodică pentru a economisi energia bateriei?~~](#user-content-faq91)
* [(92) Puteți adăuga filtrarea spam-ului, verificarea semnăturii DKIM și autorizarea SPF?](#user-content-faq92)
* [(93) Puteți permite instalarea/stocarea datelor pe medii de stocare externe (sdcard)?](#user-content-faq93)
* [(94) Ce înseamnă dunga roșie/portocalie de la sfârșitul antetului?](#user-content-faq94)
* [(95) De ce nu sunt afișate toate aplicațiile atunci când se selectează un atașament sau o imagine?](#user-content-faq95)
* [(96) Unde pot găsi setările IMAP și SMTP?](#user-content-faq96)
* [(97) Ce este 'curățarea'?](#user-content-faq97)
* [(98) De ce pot alege în continuare contacte după ce am revocat permisiunile pentru contacte?](#user-content-faq98)
* [(99) Puteți adăuga un editor de text bogat sau un editor de marcaj?](#user-content-faq99)
* [(100) Cum pot sincroniza categoriile Gmail?](#user-content-faq100)
* [(100) Cum pot sincroniza categoriile Gmail?](#user-content-faq101)
* [(102) Cum pot activa rotirea automată a imaginilor?](#user-content-faq102)
* [(103) Cum pot înregistra audio?](#user-content-faq158)
* [(104) Ce trebuie să știu despre raportarea erorilor?](#user-content-faq104)
* [(105) Cum funcționează opțiunea roam-like-at-home?](#user-content-faq105)
* [(106) Ce lansatoare pot afișa un număr de insigne cu numărul de mesaje necitite?](#user-content-faq106)
* [(107) Cum se folosesc stelele colorate?](#user-content-faq107)
* [~~(108) Puteți adăuga mesaje șterse permanent din orice dosar?~~](#user-content-faq108)
* [~~(109) De ce 'select account' este disponibil doar în versiunile oficiale?~~](#user-content-faq109)
* [(110) De ce sunt (unele) mesaje goale și/sau atașamente corupte?](#user-content-faq110)
* [(111) Este acceptat OAuth?](#user-content-faq111)
* [(112) Ce furnizor de e-mail recomandați?](#user-content-faq112)
* [(113) Cum funcționează autentificarea biometrică?](#user-content-faq113)
* [(114) Puteți adăuga o funcție de import pentru setările altor aplicații de e-mail?](#user-content-faq114)
* [(115) Puteți adăuga cipuri de adrese de e-mail?](#user-content-faq115)
* [~~(116) Cum pot afișa implicit imagini în mesajele de la expeditori de încredere?~~](#user-content-faq116)
* [(117) Mă puteți ajuta să îmi recuperez achiziția?](#user-content-faq117)
* [(118) Ce înseamnă mai exact " Eliminați parametrii de urmărire "?](#user-content-faq118)
* [~~(119) Poți să adaugi culori la widgetul unificat pentru căsuța poștală? ~~](#user-content-faq119)
* [(120) De ce nu sunt eliminate notificările de mesaje noi la deschiderea aplicației?](#user-content-faq120)
* [(121) Cum sunt grupate mesajele într-o conversație?](#user-content-faq121)
* [~~(122) De ce numele destinatarului/adresa de e-mail este afișată cu o culoare de avertizare?~~](#user-content-faq122)
* [(123) Ce se va întâmpla atunci când FairEmail nu se poate conecta la un server de e-mail?](#user-content-faq123)
* [(124) De ce primesc 'Mesaj prea mare sau prea complex pentru a fi afișat'?](#user-content-faq124)
* [(125) Care sunt caracteristicile experimentale actuale?](#user-content-faq125)
* [(126) Pot fi trimise previzualizări ale mesajelor către dispozitivul meu portabil?](#user-content-faq126)
* [(127) Cum pot rezolva problema 'Argument(e) HELO invalid sintactic'?](#user-content-faq127)
* [(128) Cum pot reseta întrebările adresate, de exemplu pentru a afișa imagini?](#user-content-faq128)
* [(129) Sunt acceptate ProtonMail, Tutanota?](#user-content-faq129)
* [(130) Mesajul de eroare ... ce înseamnă ?](#user-content-faq130)
* [(131) Se poate schimba direcția de glisare spre mesajul anterior/succesiv?](#user-content-faq131)
* [(132) De ce sunt silențioase notificările de mesaje noi?](#user-content-faq132)
* [(133) De ce nu este acceptat ActiveSync?](#user-content-faq133)
* [(134) Puteți adăuga ștergerea mesajelor locale?](#user-content-faq134)
* [(135) De ce sunt afișate în conversații mesajele și ciornele aruncate la gunoi?](#user-content-faq135)
* [(136) Cum pot șterge un cont/identitate/folder?](#user-content-faq136)
* [(137) Cum pot reseta 'Nu intreba din nou'?](#user-content-faq137)
* [(138) Puteți adăuga gestionarea calendarului/contactelor/ sarcinilor/notelor?](#user-content-faq138)
* [(139) Cum se rezolvă problema 'Utilizatorul este autentificat, dar nu este conectat'?](#user-content-faq139)
* [(140) De ce textul mesajului conține caractere ciudate?](#user-content-faq140)
* [(141) Cum pot rezolva problema 'Este necesar un dosar de ciorne pentru a trimite mesaje'?](#user-content-faq141)
* [(142) Cum pot stoca mesajele trimise în căsuța de primire?](#user-content-faq142)
* [~~(143) Puteți adăuga un dosar de gunoi pentru conturile POP3?~~](#user-content-faq143)
* [(144) Cum pot înregistra note vocale?](#user-content-faq144)
* [(145) Cum pot seta un sunet de notificare pentru un cont, dosar sau expeditor?](#user-content-faq145)
* [(146) Cum pot repara orele incorecte ale mesajelor?](#user-content-faq146)
* [(147) Ce ar trebui să știu despre versiunile terților?](#user-content-faq147)
* [(148) Cum pot utiliza un cont Apple iCloud?](#user-content-faq148)
* [(149) Cum funcționează widget-ul pentru numărul de mesaje necitite?](#user-content-faq149)
* [(150) Puteți adăuga anularea invitațiilor din calendar?](#user-content-faq150)
* [(151) Puteți adăuga backup/restaurare a mesajelor?](#user-content-faq151)
* [(152) Cum pot introduce un grup de contacte?](#user-content-faq152)
* [(153) De ce nu funcționează ștergerea permanentă a mesajelor din Gmail?](#user-content-faq153)
* [~~(154) Poți să adaugi pictogramele favorite ca fotografii de contact?~~](#user-content-faq154)
* [(155) Ce este un fișier winmail.dat?](#user-content-faq155)
* [(156) Cum pot să configurez un cont Office 365?](#user-content-faq156)
* [(157) Cum îmi pot crea un cont Free.fr?](#user-content-faq157)
* [(158) Ce aparat foto/înregistrator audio recomandați?](#user-content-faq158)
* [(159) Ce sunt listele de protecție a urmăritorilor de la Disconnect?](#user-content-faq159)
* [(160) Puteți adăuga ștergerea permanentă a mesajelor fără confirmare?](#user-content-faq160)
* [(161) Puteți adăuga o setare pentru a schimba culoarea principală și cea de subliniere?](#user-content-faq161)
* [(162) Se acceptă IMAP NOTIFY?](#user-content-faq162)
* [(163) Ce este clasificarea mesajelor?](#user-content-faq163)
* [(164) Puteți adăuga teme personalizabile?](#user-content-faq164)
* [(165) Este acceptat Android Auto?](#user-content-faq165)
* [(166) Pot să amân un mesaj pe mai multe dispozitive?](#user-content-faq166)

[Am o altă întrebare.](#user-content-support)

<a name="faq1"></a>
**(1) Ce permisiuni sunt necesare și de ce?**

Sunt necesare următoarele permisiuni Android:

* *au acces complet la rețea* (INTERNET): pentru a trimite și primi e-mailuri
* *view network connections* (ACCESS_NETWORK_STATE): pentru a monitoriza schimbările de conectivitate la internet
* *run at startup* (RECEIVE_BOOT_COMPLETED): pentru a începe monitorizarea la pornirea dispozitivului
* *serviciu de prim-plan* (FOREGROUND_SERVICE): pentru a rula un serviciu de prim-plan pe Android 9 Pie și ulterior, vedeți și următoarea întrebare
* *împiedică dispozitivul să doarmă* (WAKE_LOCK): pentru a menține dispozitivul treaz în timpul sincronizării mesajelor
* *facturare în aplicație* (BILLING): pentru a permite achiziții în aplicație
* *programează alarma exactă* (SCHEDULE_EXACT_ALARM): pentru a utiliza programarea exactă a alarmei (Android 12 și ulterior)
* Opțional: *citiți contactele* (READ_CONTACTS): pentru a completa automat adresele, pentru a afișa fotografiile contactelor și [pentru a alege contactele](https://developer.android.com/guide/components/intents-common#PickContactDat)
* Opțional: *citiți conținutul cardului SD* (READ_EXTERNAL_STORAGE): pentru a accepta fișiere de la alte aplicații, depășite, vezi și [acest FAQ](#user-content-faq49)
* Opțional: *utilizarea hardware-ului de amprentă digitală* (USE_FINGERPRINT) și utilizarea * hardware-ului biometric* (USE_BIOMETRIC): pentru a utiliza autentificarea biometrică
* Opțional: *găsiți conturi pe dispozitiv* (GET_ACCOUNTS): pentru a selecta un cont atunci când se utilizează configurarea rapidă a Gmail
* Android 5.1 Lollipop și versiunile anterioare: *Citește profilul* (READ_PROFILE): pentru a citi numele dvs. atunci când utilizați configurarea rapidă a Gmail (nu este necesar pe versiunile ulterioare de Android)
* Android 5.1 Lollipop și versiunile anterioare: *Citește profilul* (READ_PROFILE): pentru a citi numele dvs. atunci când utilizați configurarea rapidă a Gmail (nu este necesar pe versiunile ulterioare de Android)

[Permisiunile opționale](https://developer.android.com/training/permissions/requesting) sunt acceptate numai pe Android 6 Marshmallow și următoarele. Pe versiunile anterioare de Android vi se va cere să acordați permisiunile opționale la instalarea FairEmail.

Următoarele permisiuni sunt necesare pentru a afișa numărul de mesaje necitite sub formă de insignă (a se vedea și [acest FAQ](#user-content-faq106)):

* *com.sec.android.provider.badge.permission.READ*
* *com.sec.android.provider.badge.permission.WRITE*
* *com.htc.launcher.permission.READ_SETTINGS*
* *com.htc.launcher.permission.UPDATE_SHORTCUT*
* *com.sonyericsson.home.permission.BROADCAST_BADGE*
* *com.sonymobile.home.permission.PROVIDER_INSERT_BADGE*
* *com.anddoes.launcher.permission.UPDATE_COUNT*
* *com.majeur.launcher.permission.UPDATE_BADGE*
* *com.huawei.android.launcher.permission.CHANGE_BADGE*
* *com.huawei.android.launcher.permission.READ_SETTINGS*
* *com.huawei.android.launcher.permission.WRITE_SETTINGS*
* *android.permission.READ_APP_BADGE*
* *com.oppo.launcher.permission.READ_SETTINGS*
* *com.oppo.launcher.permission.WRITE_SETTINGS*
* *me.everything.badger.permission.BADGE_COUNT_READ*
* *me.everything.badger.permission.BADGE_COUNT_WRITE*

FairEmail va păstra o listă cu adresele de la care primești mesaje și la care trimiți mesaje. și va folosi această listă pentru sugestii de contacte atunci când FairEmail nu are permisiuni de contact. Acest lucru înseamnă că puteți utiliza FairEmail fără furnizorul de contacte Android (agendă). Rețineți că puteți alege în continuare contacte fără a acorda permisiuni de contacte pentru FairEmail, numai că sugerarea contactelor nu va funcționa fără permisiuni de contacte.

<br />

<a name="faq2"></a>
**(2) De ce este afișată o notificare permanentă?**

Se afișează o notificare permanentă pe bara de stare cu prioritate scăzută, cu numărul de conturi monitorizate și numărul de operațiuni în așteptare (a se vedea întrebarea următoare). pentru a împiedica Android să ucidă serviciul care se ocupă de primirea continuă a e-mailurilor. Acest lucru era [deja necesar](https://developer.android.com/reference/android/app/Service.html#startForeground(int,%20android.app.Notification)), dar, odată cu introducerea [modului doze](https://developer.android.com/training/monitoring-device-state/doze-standby) în Android 6 Marshmallow, acest lucru este mai mult ca niciodată necesar. Modul Doze va opri toate aplicațiile atunci când ecranul este oprit pentru o anumită perioadă de timp, cu excepția cazului în care aplicația a inițiat un serviciu în prim-plan, ceea ce necesită afișarea unei notificări în bara de stare.

Cele mai multe, dacă nu toate celelalte aplicații de e-mail nu afișează o notificare cu "efectul secundar" că, adesea, mesajele noi nu sunt raportate sau sunt raportate cu întârziere și că mesajele nu sunt trimise sau sunt trimise cu întârziere.

Android afișează mai întâi pictogramele notificărilor cu prioritate ridicată din bara de stare și va ascunde pictograma notificării FairEmail dacă nu mai există spațiu pentru a afișa pictogramele. În practică, acest lucru înseamnă că notificarea din bara de stare nu ocupă spațiu în bara de stare, cu excepția cazului în care există spațiu disponibil.

Notificarea din bara de stare poate fi dezactivată prin intermediul setărilor de notificare din FairEmail:

* Android 8 Oreo și versiunile ulterioare: apăsați pe butonul *Canal de recepție* și dezactivați canalul prin intermediul setărilor Android (acest lucru nu va dezactiva notificările de mesaje noi)
* Android 7 Nougat și înainte: activați *Utilizați serviciul de fundal pentru a sincroniza mesajele*, dar asigurați-vă că citiți observația de sub setare

Puteți trece la sincronizarea periodică a mesajelor în setările de recepție pentru a elimina notificarea, dar trebuie să știți că acest lucru ar putea consuma mai multă energie din baterie. Consultați [aici](#user-content-faq39) pentru mai multe detalii despre utilizarea bateriei.

Android 8 Oreo ar putea afișa, de asemenea, o notificare în bara de stare cu textul *Aplicațiile rulează în fundal*. Vă rugăm să consultați [aici](https://www.reddit.com/r/Android/comments/7vw7l4/psa_turn_off_background_apps_notification/) despre cum puteți dezactiva această notificare.

Unele persoane au sugerat să utilizați [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/) (FCM) în locul unui serviciu Android cu o notificare în bara de stare, dar acest lucru ar necesita ca furnizorii de e-mail să trimită mesaje FCM sau un server central unde sunt colectate toate mesajele care trimit mesaje FCM. Prima variantă nu se va întâmpla, iar ultima ar avea implicații semnificative asupra vieții private.

Dacă ați ajuns aici făcând clic pe notificare, trebuie să știți că următorul clic va deschide căsuța de primire unificată.

<br />

<a name="faq3"></a>
**(3) Ce sunt operațiunile și de ce sunt în așteptare?**

Notificarea din bara de stare cu prioritate scăzută arată numărul de operațiuni în așteptare, care pot fi:

* *add*: adaugă mesajul la folderul de la distanță
* *move*: mută mesajul în alt folder la distanță
* *copy*: copiază mesajul într-un alt folder la distanță
* *fetch*: preluarea mesajului modificat (împins)
* *delete*: șterge mesajul din folderul de la distanță
* *seen*: marchează mesajul ca fiind citit/necitit în folderul de la distanță
* *răspuns*: marchează mesajul ca răspuns în folderul de la distanță
* *flag*: adăugare/eliminare stea în folderul de la distanță
* *cuvânt cheie*: adăugarea/eliminarea indicatorului IMAP în folderul la distanță
* *label*: setează/resetează eticheta Gmail în folderul de la distanță
* *anteturi*: descarcă anteturile mesajului
* *neprelucrat*: descarcă mesajul original
* *body*: descarcă textul mesajului
* *atașament*: descărcare atașament
* *sincronizare*: sincronizarea mesajelor locale și la distanță
* *abonare*: abonare la folderul de la distanță
* *curăţare*: șterge toate mesajele din folderul de la distanță
* *trimite*: trimite un mesaj
* *există*: verifică dacă mesajul există
* *regulă*: execută regula pe corpul textului
* *expunere*: șterge permanent mesajele

Operațiunile sunt procesate numai atunci când există o conexiune la serverul de e-mail sau atunci când se efectuează o sincronizare manuală. A se vedea, de asemenea, [acest FAQ](#user-content-faq16).

<br />

<a name="faq4"></a>
**(4) Cum pot folosi un certificat de securitate invalid / o parolă goală / o conexiune în text simplu?**

*... Neacreditat ... nu este în certificat...*
<br />
*... Certificat de securitate invalid (Nu se poate verifica identitatea serverului) ...*

Acest lucru poate fi cauzat de utilizarea unui nume de gazdă incorect, așa că mai întâi verificați de două ori numele de gazdă în setările avansate de identitate/cont ( apăsați Configurare manuală). Vă rugăm să consultați documentația furnizorului de e-mail cu privire la numele de gazdă corect.

Ar trebui să încercați să remediați această problemă contactând furnizorul dvs. sau obținând un certificat de securitate valabil deoarece certificatele de securitate nevalabile sunt nesigure și permit [ atacuri de tip "man-in-the-middle" ](https://en.wikipedia.org/wiki/Man-in-the-middle_attack). Dacă banii reprezintă un obstacol, puteți obține certificate de securitate gratuite de la [Let's Encrypt](https://letsencrypt.org).

Soluția rapidă, dar nesigură (nerecomandată), este de a activa *Conexiuni nesigure* în setările avansate de identitate (meniul de navigare, atingeți *Settings*, atingeți *Manual setup*, atingeți *Identities*, atingeți identitatea, atingeți *Advanced*).

Alternativ, puteți accepta amprenta digitală a certificatelor de server nevalabile în felul următor:

1. Asigurați-vă că folosiți o conexiune de internet de încredere (fără rețele Wi-Fi publice etc.)
1. Accesați ecranul de configurare prin intermediul meniului de navigare (glisați din partea stângă spre interior)
1. Atingeți Configurare manuală, atingeți Conturi/Identități și atingeți contul și identitatea defecte
1. Verificarea/salvarea contului și a identității
1. Bifați caseta de selectare de sub mesajul de eroare și salvați din nou

Acest lucru va "prinde" certificatul serverului pentru a preveni atacurile de tip man-in-the-middle.

Rețineți că este posibil ca versiunile mai vechi de Android să nu recunoască autoritățile de certificare mai noi, cum ar fi Let's Encrypt, ceea ce face ca conexiunile să fie considerate nesigure, a se vedea, de asemenea, [ aici](https://developer.android.com/training/articles/security-ssl).

<br />

*Nu s-a găsit ancora de încredere pentru calea de certificare*

*... java.security.cert.CertPathValidatorException: Nu s-a găsit ancora de încredere pentru calea de certificare ...* înseamnă că managerul implicit de încredere Android nu a reușit să verifice lanțul de certificate al serverului.

Acest lucru se poate datora faptului că certificatul rădăcină nu este instalat pe dispozitivul dvs. sau pentru că lipsesc certificatele intermediare, de exemplu, pentru că serverul de e-mail nu le-a trimis.

Puteți rezolva prima problemă descărcând și instalând certificatul rădăcină de pe site-ul web al furnizorului de certificate.

A doua problemă ar trebui să fie rezolvată prin modificarea configurației serverului sau prin importarea certificatelor intermediare pe dispozitiv.

Puteți să fixați și certificatul, a se vedea mai sus.

<br />

*Parolă goală*

Numele dvs. de utilizator este probabil ușor de ghicit, așa că acest lucru este destul de nesigur, cu excepția cazului în care serverul SMTP este disponibil doar prin intermediul unei rețele locale restricționate sau al unui VPN.

*Conexiune în text simplu*

Numele tău de utilizator și parola și toate mesajele vor fi trimise și primite necriptate, ceea ce este **foarte nesigur** deoarece un [man-in-the-middle atac](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) este foarte simplu pe o conexiune necriptată.

Dacă doriți totuși să utilizați un certificat de securitate invalid, o parolă goală sau o conexiune în text simplu va trebui să activați conexiunile nesigure în setările contului și/sau ale identității. STARTTLS trebuie să fie selectat pentru conexiunile de text simplu. Dacă activați conexiunile nesigure, trebuie să vă conectați numai prin intermediul rețelelor private, de încredere și niciodată prin intermediul rețelelor publice, cum ar fi cele oferite în hoteluri, aeroporturi etc.

<br />

<a name="faq5"></a>
**(5) Cum pot personaliza vizualizarea mesajelor?**

În meniul cu trei puncte overflow puteți activa sau dezactiva sau selecta:

* *mărimea textului*: pentru trei mărimi de font diferite
* *vedere compactă*: pentru elemente de mesaj mai condensate și un font mai mic pentru textul mesajului

În secțiunea de afișare a setărilor puteți activa sau dezactiva, de exemplu:

* *Bucată de primire unificată*: pentru a dezactiva căsuța de primire unificată și pentru a lista în schimb folderele selectate pentru căsuța de primire unificată
* *Stilul tabelar*: pentru a afișa o listă liniară în loc de carduri
* *Grupează după dată*: afișează antetul cu data deasupra mesajelor cu aceeași dată
* *Transmiterea conversației*: pentru a dezactiva citirea conversației și pentru a afișa în schimb mesaje individuale
* *Bara de acțiune a conversației*: pentru a dezactiva bara de navigare de jos
* *Culoare de evidențiere*: pentru a selecta o culoare pentru expeditorul mesajelor necitite
* *Show contact photos*: pentru a ascunde fotografiile de contact
* *Show names and email addresses*: pentru a afișa nume sau pentru a afișa nume și adrese de e-mail
* *Afișează subiectul în italic*: pentru a afișa subiectul mesajului ca text normal
* *Arată stele*: pentru a ascunde stelele (favorite)
* *Show message preview*: pentru a afișa 1-4 rânduri din textul mesajului
* *Show address details by default*: pentru a extinde implicit secțiunea de adrese
* *Afișarea automată a mesajului original pentru contactele cunoscute*: pentru a afișa automat mesajele originale pentru contactele de pe dispozitiv, vă rugăm să citiți [acest FAQ](#user-content-faq35)
* *Afișarea automată a imaginilor pentru contactele cunoscute*: pentru a afișa automat imagini pentru contactele de pe dispozitiv, vă rugăm să citiți [acest FAQ](#user-content-faq35)

Rețineți că mesajele pot fi previzualizate numai atunci când textul mesajului a fost descărcat. Textele cu mesaje mai mari nu sunt descărcate în mod implicit în rețelele cu contorizare (în general mobile). Puteți modifica acest lucru în setările de conectare.

Unii oameni întreabă:

* pentru a afișa subiectul cu bold, dar boldul este deja folosit pentru a evidenția mesajele necitite
* pentru a muta steaua spre stânga, dar este mult mai ușor să acționați steaua pe partea dreaptă

<br />

<a name="faq6"></a>
**(6) Cum pot să mă conectez la Gmail / G suite?**

Dacă folosiți versiunea FairEmail din Play Store sau GitHub, puteți utiliza expertul de configurare rapidă pentru a configura cu ușurință un cont și o identitate Gmail. Expertul de configurare rapidă Gmail nu este disponibil pentru versiunile terțe, cum ar fi versiunea F-Droid. deoarece Google a aprobat utilizarea OAuth doar pentru compilările oficiale.

Dacă nu doriți sau nu puteți utiliza un cont Google pe dispozitiv, de exemplu pe dispozitivele Huawei recente, puteți fie să activați accesul pentru "aplicații mai puțin sigure" și să folosiți parola contului dvs. (nerecomandat) fie activați autentificarea cu doi factori și utilizați o parolă specifică pentru o aplicație. Pentru a utiliza o parolă, va trebui să configurați un cont și o identitate prin configurarea manuală în loc de expertul de configurare rapidă.

**Important**: uneori Google emite această alertă:

*[ALERT] Vă rugăm să vă conectați prin intermediul browserului dvs. web: https://support.google.com/mail/accounts/answer/78754 (eșec)*

Această verificare de securitate Google este declanșată mai des cu *aplicații mai puțin sigure* activate, mai puțin cu o parolă de aplicație și cu greu atunci când se utilizează un cont pe dispozitiv (OAuth).

Vă rugăm să consultați [acest FAQ](#user-content-faq111) pentru a afla de ce pot fi utilizate doar conturile de pe dispozitiv.

Rețineți că este necesară o parolă specifică aplicației atunci când este activată autentificarea în doi factori.

<br />

*Parolă specifică aplicației*

Consultați [aici](https://support.google.com/accounts/answer/185833) despre cum să generați o parolă specifică aplicației.

<br />

*Activați "Aplicații mai puțin sigure"*

**Important**: utilizarea acestei metode nu este recomandată, deoarece este mai puțin fiabilă.

**Important**: Conturile Gsuite autorizate cu un nume de utilizator/parolă nu vor mai funcționa [în viitorul apropiat](https://gsuiteupdates.googleblog.com/2019/12/less-secure-apps-oauth-google-username-password-incorrect.html).

Vedeți [aici](https://support.google.com/accounts/answer/6010255) despre cum să activați "aplicații mai puțin sigure" sau mergeți [direct la setare](https://www.google.com/settings/security/lesssecureapps).

Dacă utilizați mai multe conturi Gmail, asigurați-vă că modificați setarea "Aplicații mai puțin sigure" a contului (conturilor) potrivit.

Rețineți că trebuie să părăsiți ecranul de setări "aplicații mai puțin sigure" folosind săgeata înapoi pentru a aplica setările.

Dacă utilizați această metodă, ar trebui să folosiți o parolă [puternică](https://en.wikipedia.org/wiki/Password_strength) pentru contul dvs. de Gmail, ceea ce este oricum o idee bună. Rețineți că utilizarea protocolului [standard](https://tools.ietf.org/html/rfc3501) IMAP în sine nu este mai puțin sigură.

Atunci când opțiunea "Aplicații mai puțin sigure" nu este activată, veți primi eroarea *Autentificare eșuată - acreditări invalide* pentru conturi (IMAP) și *Nume de utilizator și parolă neacceptate* pentru identități (SMTP).

<br />

*Informații generale*

Este posibil să primiți alerta "*Vă rugăm să vă conectați prin intermediul browserului web*". Acest lucru se întâmplă atunci când Google consideră că rețeaua care vă conectează la internet (poate fi un VPN) nu este sigură. Acest lucru poate fi evitat prin utilizarea asistentului de configurare rapidă Gmail sau a unei parole specifice aplicației.

Vedeți [aici](https://support.google.com/mail/answer/7126229) pentru instrucțiunile de la Google și [aici](https://support.google.com/mail/accounts/answer/78754) pentru depanare.

<br />

<a name="faq7"></a>
**(7) De ce mesajele trimise nu apar (direct) în dosarul trimis?**

În mod normal, mesajele trimise sunt mutate din căsuța de ieșire în dosarul de expediere imediat ce furnizorul dvs. adaugă mesajele trimise în dosarul de expediere. Acest lucru necesită selectarea unui dosar trimis în setările contului, iar dosarul trimis trebuie să fie setat pentru sincronizare.

Unii furnizori nu țin evidența mesajelor trimise sau este posibil ca serverul SMTP utilizat să nu aibă legătură cu furnizorul. În aceste cazuri, FairEmail, va adăuga automat mesajele trimise la dosarul trimis la sincronizarea dosarului trimis, care va avea loc după ce un mesaj a fost trimis. Rețineți că acest lucru va avea ca rezultat un trafic de internet suplimentar.

~~Dacă acest lucru nu se întâmplă, este posibil ca furnizorul dumneavoastră să nu țină evidența mesajelor trimise sau să folosiți un server SMTP care nu are legătură cu furnizorul.~~ ~~În aceste cazuri puteți activa setarea de identitate avansată *Stocarea mesajelor trimise* pentru a permite FairEmail să adauge mesajele trimise în folderul de mesaje trimise imediat după trimiterea unui mesaj.~~ ~~Rețineți că activarea acestei setări ar putea duce la duplicarea mesajelor dacă furnizorul dvs. adaugă și mesajele trimise în folderul de mesaje trimise.~~ ~~Atenție, de asemenea, că activarea acestei setări va duce la o utilizare suplimentară a datelor, în special atunci când trimiteți mesaje cu atașamente mari.~~

~~Dacă mesajele trimise din căsuța de ieșire nu sunt găsite în dosarul trimis la o sincronizare completă, acestea vor fi mutate și din căsuța de ieșire în dosarul trimis.~~ ~~O sincronizare completă are loc la reconectarea la server sau la sincronizarea periodică sau manuală.~~ ~~ Probabil că veți dori să activați în schimb setarea avansată *Stocare mesaje trimise* pentru a muta mai repede mesajele în dosarul trimis.~~

<br />

<a name="faq8"></a>
**(8) Pot folosi un cont Microsoft Exchange?**

Protocolul Microsoft Exchange Web Services [este în curs de eliminare progresivă](https://techcommunity.microsoft.com/t5/Exchange-Team-Blog/Upcoming-changes-to-Exchange-Web-Services-EWS-API-for-Office-365/ba-p/608055). Prin urmare, nu prea mai are sens să se adauge acest protocol.

Puteți utiliza un cont Microsoft Exchange dacă acesta este accesibil prin IMAP, ceea ce este în general cazul. Consultați [aici](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793) pentru mai multe informații.

Rețineți că descrierea lui FairEmail începe cu observația că protocoalele non-standard, cum ar fi Microsoft Exchange Web Services și Microsoft ActiveSync, nu sunt acceptate.

Vă rugăm să consultați [aici](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) pentru documentația Microsoft privind configurarea unui client de e-mail. Există, de asemenea, o secțiune despre erorile comune de conectare și soluții.

Unele versiuni mai vechi ale serverului Exchange au o eroare care provoacă mesaje goale și atașamente corupte. Vă rugăm să consultați [acest FAQ](#user-content-faq110) pentru o soluție de rezolvare.

Vă rugăm să consultați [acest FAQ](#user-content-faq133) despre suportul ActiveSync.

Vă rugăm să consultați [acest FAQ](#user-content-faq111) despre suportul OAuth.

<br />

<a name="faq9"></a>
**(9) Ce sunt identitățile / cum pot adăuga un alias?**

Identitățile reprezintă adresele de e-mail pe care le trimiteți *de la* prin intermediul unui server de e-mail (SMTP).

Unii furnizori vă permit să aveți mai multe pseudonime. Le puteți configura prin setarea câmpului de adresă de e-mail al unei identități suplimentare la adresa alias-ului și setarea câmpului nume de utilizator la adresa dvs. de e-mail principală.

Rețineți că puteți copia o identitate prin apăsarea lungă a acesteia.

Alternativ, puteți activa *Permiterea editării adresei expeditorului* în setările avansate ale unei identități existente pentru a edita numele de utilizator atunci când compuneți un mesaj nou, dacă furnizorul dvs. permite acest lucru.

FairEmail va actualiza automat parolele identităților conexe atunci când actualizați parola contului asociat sau a unei identități conexe.

Consultați [acest FAQ](#user-content-faq33) privind editarea numelui de utilizator al adreselor de e-mail.

<br />

<a name="faq10"></a>
**~~(10) Ce înseamnă "UIDPLUS nu este suportat"?~~**

~~Mesajul de eroare *UIDPLUS nu este acceptat* înseamnă că furnizorul dvs. de e-mail nu oferă extensia IMAP [UIDPLUS](https://tools.ietf.org/html/rfc4315). Această extensie IMAP este necesară pentru a implementa sincronizarea bidirecțională, care nu este o caracteristică opțională. Deci, cu excepția cazului în care furnizorul dvs. poate activa această extensie, nu puteți utiliza FairEmail pentru acest furnizor.~~

<br />

<a name="faq11"></a>
**~~~(11) De ce nu este acceptat POP?~~**

~~În afară de faptul că orice furnizor de e-mail decent suportă [IMAP](https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol) în aceste zile,~~ ~~ utilizarea [POP](https://en.wikipedia.org/wiki/Post_Office_Protocol) va duce la o utilizare suplimentară inutilă a bateriei și la notificări întârziate ale mesajelor noi.~~ ~~În plus, POP este nepotrivit pentru sincronizarea bidirecțională și de cele mai multe ori oamenii citesc și scriu mesaje pe dispozitive diferite în zilele noastre.~~

~~În principiu, POP acceptă doar descărcarea și ștergerea mesajelor din căsuța de primire.~~ ~~Astfel, nu este posibilă efectuarea unor operații obișnuite, cum ar fi setarea atributelor mesajelor (citit, cu stele, răspuns, etc.), adăugarea (backup) și mutarea mesajelor.~~

~~Vezi și [ce scrie Google despre asta](https://support.google.com/mail/answer/7104828).~~

~~De exemplu [Gmail poate importa mesaje](https://support.google.com/mail/answer/21289) dintr-un alt cont POP,~~ ~~care poate fi folosit ca o soluție de avarie atunci când furnizorul tău nu suportă IMAP.~~

~~tl;dr; ia în considerare pentru a trece la IMAP.~~

<br />

<a name="faq12"></a>
**(12) Cum funcționează criptarea/decriptarea?**

Comunicarea cu serverele de e-mail este întotdeauna criptată, cu excepția cazului în care ați dezactivat acest lucru în mod explicit. Această întrebare se referă la criptarea opțională end-to-end cu PGP sau S/MIME. Expeditorul și destinatarul trebuie mai întâi să cadă de acord asupra acestui lucru și să facă schimb de mesaje semnate pentru a-și transfera cheia publică pentru a putea trimite mesaje criptate.

<br />

*Informații generale*

Vă rugăm să [vezi aici](https://en.wikipedia.org/wiki/Public-key_cryptography) despre cum funcționează criptarea cu cheie publică/privată.

Criptarea pe scurt:

* **Mesajele de ieșire** sunt criptate cu **cheia publică** a destinatarului
* **Mesajele primite** sunt decriptate cu **cheia privată** a destinatarului

Semnarea pe scurt:

* **Mesajele de ieșire** sunt semnate cu **cheia privată** a expeditorului
* **Mesajele primite** sunt verificate cu **cheia publică** a expeditorului

Pentru a semna/cripta un mesaj, trebuie doar să selectați metoda corespunzătoare în fereastra de dialog de trimitere. Puteți oricând să deschideți dialogul de trimitere folosind meniul de suprapunere cu trei puncte, în cazul în care ați selectat *Don't show again* înainte.

Pentru a verifica o semnătură sau pentru a decripta un mesaj primit, deschideți mesajul și atingeți pictograma de gest sau de lacăt aflată chiar sub bara de acțiune a mesajului.

Prima dată când trimiteți un mesaj semnat/criptat este posibil să vi se ceară o cheie de semnare. FairEmail va stoca automat cheia de semnare selectată în identitatea utilizată pentru data viitoare. Dacă aveți nevoie să resetați cheia de semnătură, trebuie doar să salvați identitatea sau să apăsați lung pe identitatea din lista de identități și să selectați *Reset sign key*. Cheia de semnătură selectată este vizibilă în lista de identități. Dacă aveți nevoie să selectați o cheie de la caz la caz, puteți crea mai multe identități pentru același cont cu aceeași adresă de e-mail.

În setările de criptare puteți selecta metoda de criptare implicită (PGP sau S/MIME), activați *Semnare implicită*, *Criptare implicită* și *Decriptare automată a mesajelor*, dar rețineți că decriptarea automată nu este posibilă dacă este necesară interacțiunea utilizatorului, cum ar fi selectarea unei chei sau citirea unui token de securitate.

Textul/atașamentele mesajelor care urmează să fie criptate și textul/atașamentele mesajelor decriptate sunt stocate numai la nivel local și nu vor fi adăugate niciodată pe serverul de la distanță. Dacă doriți să anulați decriptarea, puteți utiliza elementul de meniu *resync* din meniul cu trei puncte din bara de acțiune a mesajului.

<br />

*PGP*

Va trebui să instalați și să configurați mai întâi [OpenKeychain](https://f-droid.org/en/packages/org.sufficientlysecure.keychain/). FairEmail a fost testat cu OpenKeychain versiunea 5.4. Versiunile ulterioare vor fi cel mai probabil compatibile, dar este posibil ca versiunile anterioare să nu fie compatibile.

**Important**: se știe că aplicația OpenKeychain se blochează (în tăcere) atunci când aplicația apelantă (FairEmail) nu este încă autorizată și primește o cheie publică existentă. Puteți rezolva această problemă încercând să trimiteți un mesaj semnat/criptat către un expeditor cu o cheie publică necunoscută.

**Important**: dacă aplicația OpenKeychain nu (mai) poate găsi o cheie, este posibil să fie nevoie să resetați o cheie selectată anterior. Acest lucru se poate face prin apăsarea lungă a unei identități din lista de identități (Setări, atingeți Configurare manuală, atingeți Identități).

**Important**: pentru a permite aplicațiilor precum FairEmail să se conecteze în mod fiabil la serviciul OpenKeychain pentru a cripta/decripta mesajele, ar putea fi necesar să dezactivați optimizările de baterie pentru aplicația OpenKeychain.

**Important**: se pare că aplicația OpenKeychain are nevoie de permisiunea contactelor pentru a funcționa corect.

**Important**: pe unele versiuni / dispozitive Android este necesar să se activeze *Afișează ferestrele pop-up în timp ce rulează în fundal* în permisiunile suplimentare din setările aplicației Android ale aplicației OpenKeychain. Fără această permisiune, proiectul va fi salvat, dar este posibil să nu apară fereastra pop-up OpenKeychain pentru confirmare/selectare.

FairEmail va trimite antetul [Autocrypt](https://autocrypt.org/) pentru a fi folosit de alți clienți de e-mail, dar numai pentru mesajele semnate și criptate, deoarece prea multe servere de e-mail au probleme cu antetul Autocrypt, care este adesea lung. Rețineți că cel mai sigur mod de a începe un schimb de e-mailuri criptate este de a trimite mai întâi mesaje semnate. Antetele Autocrypt primite vor fi trimise către aplicația OpenKeychain pentru a fi stocate la verificarea unei semnături sau la decriptarea unui mesaj.

Deși acest lucru nu ar trebui să fie necesar pentru majoritatea clienților de e-mail, puteți atașa cheia dvs. publică la un mesaj și dacă folosiți *.key* ca extensie, tipul mime va fi corect *application/pgp-keys*.

Din motive de securitate, toată gestionarea cheilor este delegată aplicației OpenKey chain. Acest lucru înseamnă, de asemenea, că FairEmail nu stochează chei PGP.

Se acceptă PGP criptat în linie în mesajele primite, dar nu se acceptă semnăturile PGP în linie și PGP în linie în mesajele de ieșire, a se vedea [aici](https://josefsson.org/inline-openpgp-considered-harmful.html) despre motivul pentru care nu se acceptă.

Mesajele doar semnate sau doar criptate nu sunt o idee bună; vedeți aici de ce nu:

* [Considerații privind OpenPGP Partea I](https://k9mail.github.io/2016/11/24/OpenPGP-Considerations-Part-I.html)
* [Considerații privind OpenPGP Partea II](https://k9mail.github.io/2017/01/30/OpenPGP-Considerations-Part-II.html)
* [Considerații privind OpenPGP Partea a III-a Autocrypt](https://k9mail.github.io/2018/02/26/OpenPGP-Considerations-Part-III-Autocrypt.html)

Sunt acceptate doar mesajele semnate, iar mesajele criptate nu sunt acceptate.

Erori frecvente:

* *Nici o cheie*: nu există o cheie PGP disponibilă pentru una dintre adresele de e-mail listate
* *Ceava lipsă pentru criptare*: există probabil o cheie selectată în FairEmail care nu mai există în aplicația OpenKeychain. Resetarea cheii (a se vedea mai sus) va rezolva probabil această problemă.
* *Key for signature verification is missing*: cheia publică pentru expeditor nu este disponibilă în aplicația OpenKeychain. Acest lucru poate fi cauzat și de faptul că Autocrypt este dezactivat în setările de criptare sau că antetul Autocrypt nu este trimis.

<br />

*S/MIME*

Criptarea unui mesaj necesită cheia (cheile) publică a destinatarului (destinatarilor). Semnarea unui mesaj necesită cheia dvs. privată.

Cheile private sunt stocate de Android și pot fi importate prin intermediul setărilor avansate de securitate Android. Există o comandă rapidă (buton) pentru aceasta în setările de criptare. Android vă va cere să setați un PIN, un model sau o parolă, dacă nu ați făcut-o până acum. Dacă aveți un dispozitiv Nokia cu Android 9, vă rugăm să [citiți mai întâi acest lucru](https://nokiamob.net/2019/08/10/a-bug-prevents-nokia-1-owners-from-unlocking-their-screen-even-with-right-pin-pattern/).

Rețineți că certificatele pot conține mai multe chei pentru mai multe scopuri, de exemplu pentru autentificare, criptare și semnare. Android importă doar prima cheie, astfel încât, pentru a importa toate cheile, certificatul trebuie mai întâi să fie divizat. Acest lucru nu este foarte banal și vă sfătuim să solicitați asistență din partea furnizorului de certificate.

Rețineți că este acceptată semnarea S/MIME  cu alți algoritmi decât RSA, dar rețineți că este posibil ca alți clienți de e-mail să nu accepte acest lucru. Criptarea S/MIME este posibilă numai cu algoritmi asimetrici, ceea ce înseamnă, în practică, utilizarea RSA.

Metoda de criptare implicită este PGP, dar ultima metodă de criptare utilizată va fi reținută pentru identitatea selectată pentru următoarea dată. Puteți apăsa lung pe butonul de trimitere pentru a schimba metoda de criptare pentru o identitate. Dacă utilizați atât criptarea PGP, cât și S/MIME pentru aceeași adresă de e-mail, ar putea fi util să copiați identitatea, astfel încât să puteți schimba metoda de criptare prin selectarea uneia dintre cele două identități. Puteți apăsa lung pe o identitate din lista de identități (prin configurare manuală în ecranul principal de configurare) pentru a copia o identitate.

Pentru a permite chei private diferite pentru aceeași adresă de e-mail, FairEmail vă va permite întotdeauna să selectați o cheie atunci când există mai multe identități cu aceeași adresă de e-mail pentru același cont.

Cheile publice sunt stocate de FairEmail și pot fi importate atunci când se verifică o semnătură pentru prima dată sau prin intermediul setărilor de criptare (format PEM sau DER).

FairEmail verifică atât semnătura, cât și întregul lanț de certificate.

Erori frecvente:

* *Niciun certificat găsit care să corespundă cu targetContraints*: acest lucru înseamnă probabil că folosiți o versiune veche a FairEmail
* *unable to find valid certification path to requested target*: în principiu asta înseamnă că unul sau mai multe certificate intermediare sau rădăcină nu au fost găsite
* *Cheia privată nu se potrivește cu nicio cheie de criptare*: cheia selectată nu poate fi utilizată pentru decriptarea mesajului, probabil pentru că este o cheie incorectă
* *Nici o cheie privată*: nu a fost selectat niciun certificat sau nu era disponibil niciun certificat în keystore-ul Android

În cazul în care lanțul de certificate este incorect, puteți apăsa pe butonul mic de informații pentru a afișa toate certificatele. După detaliile certificatului este afișat emitentul sau "selfSign". Un certificat este autofirmat atunci când subiectul și emitentul sunt identice. Certificatele de la o autoritate de certificare (CA) sunt marcate cu "[keyCertSign](https://tools.ietf.org/html/rfc5280#section-4.2.1.3)". Certificatele găsite în magazinul de chei Android sunt marcate cu "Android".

Un lanț valid arată în felul următor:

```
Certificatul dumneavoastră > zero sau mai multe certificate intermediare > certificatul CA (root) marcat cu "Android"
```

Rețineți că un lanț de certificate va fi întotdeauna invalid atunci când nu se găsește niciun certificat de ancorare în magazinul de chei Android, ceea ce este fundamental pentru validarea certificatelor S/MIME.

Vă rugăm să vedeți [aici](https://support.google.com/pixelphone/answer/2844832?hl=en) cum puteți importa certificate în magazinul de chei Android.

Nu se acceptă utilizarea cheilor expirate, a mesajelor criptate/semnate în linie și a token-urilor de securitate hardware.

Dacă sunteți în căutarea unui certificat S/MIME gratuit (de test), consultați [ aici](http://kb.mozillazine.org/Getting_an_SMIME_certificate) pentru opțiuni. Vă rugăm să vă asigurați că [citiți acest lucru mai întâi](https://davidroessli.com/logs/2019/09/free-smime-certificates-in-2019/#update20191219) dacă doriți să solicitați un certificat S/MIME Actalis. Dacă sunteți în căutarea unui certificat S/MIME ieftin, am avut o experiență bună cu [Certum](https://www.certum.eu/en/smime-certificates/).

Cum se extrage o cheie publică dintr-un certificat S/MIME:

```
openssl pkcs12 -in filename.pfx/p12 -nokeys -nokeys -out cert.pem
```

Puteți decoda semnăturile S/MIME, etc., [ aici](https://lapo.it/asn1js/).

<br />

*destul de intimitate Easy*

Încă nu există [nici un standard aprobat](https://tools.ietf.org/id/draft-birk-pep-00.html) pentru confidențialitatea destul de ușoară (p≡p) și nu sunt mulți cei care îl folosesc.

Cu toate acestea, FairEmail poate trimite și primi mesaje criptate PGP, care sunt compatibile cu p≡p. De asemenea, FairEmail înțelege mesajele p≡p primite începând cu versiunea 1.1519, astfel încât subiectul criptat va fi afișat și textul mesajului încorporat va fi afișat mai frumos.

<br />

Semnarea/cifrarea S/MIME este o caracteristică pro, dar toate celelalte operațiuni PGP și S/MIME sunt gratuite.

<br />

<a name="faq13"></a>
**(13) Cum funcționează căutarea pe dispozitiv/server?**

Puteți începe căutarea mesajelor în funcție de expeditor (de la), destinatar (la, cc, bcc), subiect, cuvinte cheie sau textul mesajului utilizând lupa din bara de acțiune a unui dosar. De asemenea, puteți efectua căutări din orice aplicație selectând *Cercetare e-mail* în meniul pop-up copy/paste.

Căutarea în căsuța de primire unificată va căuta în toate dosarele din toate conturile, căutarea în lista de dosare va căuta numai în contul asociat. iar căutarea într-un dosar va căuta numai în dosarul respectiv.

Mesajele vor fi căutate mai întâi pe dispozitiv. În partea de jos va exista un buton de acțiune cu o pictogramă de căutare din nou pentru a continua căutarea pe server. Puteți selecta în ce dosar să continuați căutarea.

Protocolul IMAP nu acceptă căutarea în mai mult de un dosar în același timp. Căutarea pe server este o operațiune costisitoare, de aceea nu este posibilă selectarea mai multor dosare.

Căutarea mesajelor locale este insensibilă la majuscule și la text parțial. Textul mesajului din mesajele locale nu va fi căutat dacă textul mesajului nu a fost încă descărcat. Căutarea pe server poate fi sensibilă sau insensibilă la majuscule și minuscule și poate fi efectuată pe text parțial sau pe cuvinte întregi, în funcție de furnizor.

Unele servere nu pot gestiona căutarea în textul mesajului atunci când există un număr mare de mesaje. În acest caz, există o opțiune pentru a dezactiva căutarea în textul mesajului.

Este posibil să se utilizeze operatorii de căutare Gmail prin prefixarea unei comenzi de căutare cu *raw:*. Dacă ați configurat doar un singur cont Gmail, puteți începe o căutare brută direct pe server, căutând din căsuța de primire unificată. Dacă ați configurat mai multe conturi Gmail, va trebui mai întâi să navigați în lista de dosare sau în dosarul de arhivă (toate mesajele) al contului Gmail în care doriți să căutați. Vă rugăm să [vezi aici](https://support.google.com/mail/answer/7190) pentru operatorii de căutare posibili. De exemplu:

`
raw:mai mare:10M`

Căutarea printre un număr mare de mesaje pe dispozitiv nu este foarte rapidă din cauza a două limitări:

* [sqlite](https://www.sqlite.org/), motorul de baze de date din Android are o limită de dimensiune a înregistrărilor, ceea ce împiedică stocarea textelor mesajelor în baza de date
* Aplicațiile Android au la dispoziție doar o memorie limitată, chiar dacă dispozitivul are multă memorie disponibilă

Acest lucru înseamnă că, pentru a căuta un text de mesaj, este necesar ca fișierele care conțin textele mesajelor să fie deschise unul câte unul. pentru a verifica dacă textul căutat este conținut în fișier, ceea ce reprezintă un proces relativ costisitor.

În *Setări diverse* puteți activa *Construiește un index de căutare* pentru a crește semnificativ viteza de căutare pe dispozitiv, dar rețineți că acest lucru va crește consumul de baterie și de spațiu de stocare. Indexul de căutare se bazează pe cuvinte, astfel încât nu este posibilă căutarea unui text parțial. Căutarea cu ajutorul indexului de căutare este în mod implicit AND, astfel încât dacă se caută *apple orange* se va căuta apple AND orange. Cuvintele separate prin virgule au ca rezultat căutarea OR, astfel încât, de exemplu, *apple, orange* va căuta apple OR orange. Ambele pot fi combinate, astfel încât dacă se caută *apple, orange banana* se va căuta apple OR (orange AND banana). Utilizarea indexului de căutare este o caracteristică profesională.

Începând cu versiunea 1.1315, este posibil să se utilizeze expresii de căutare ca aceasta:

```
măr +banană - cireșe ?nuci
```

Acest lucru va avea ca rezultat o căutare de acest tip:

```
("măr" ȘI "banană" ȘI NU "cireșe") SAU "nuci"
```

Expresiile de căutare pot fi utilizate pentru căutarea pe dispozitiv prin intermediul indexului de căutare și pentru căutarea pe serverul de e-mail, dar nu și pentru căutarea pe dispozitiv fără index de căutare, din motive de performanță.

Căutarea pe dispozitiv este o funcție gratuită, utilizarea indexului de căutare și căutarea pe server este o funcție pro.

<br />

<a name="faq14"></a>
**(14) Cum pot configura un cont Outlook / Live / Hotmail?**

Un cont Outlook / Live / Hotmail poate fi configurat prin intermediul asistentului de configurare rapidă și prin selectarea *Outlook*.

Pentru a utiliza un cont Outlook, Live sau Hotmail cu autentificarea în doi factori activată, trebuie să creați o parolă de aplicație. Consultați [aici](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification) pentru detalii.

Consultați [aici](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) pentru instrucțiunile Microsoft.

Pentru configurarea unui cont Office 365, vă rugăm să consultați [acest FAQ](#user-content-faq156).

<br />

<a name="faq15"></a>
**(15) De ce se tot încarcă textul mesajului?**

Antetul și corpul mesajului sunt preluate separat de pe server. Textul mesajului pentru mesajele mai mari nu este preluat în prealabil în cazul conexiunilor cu contorizare și va fi preluat la cerere la extinderea unui mesaj. Textul mesajului va continua să se încarce dacă nu există nicio conexiune la cont, a se vedea și următoarea întrebare, sau dacă sunt în curs de executare alte operațiuni, cum ar fi sincronizarea mesajelor.

Puteți verifica în lista de conturi și dosare starea contului și a dosarului (consultați legenda pentru semnificația pictogramelor) și lista de operațiuni accesibilă prin intermediul meniului principal de navigare pentru operațiunile în așteptare (a se vedea [aceste FAQ](#user-content-faq3) pentru semnificația operațiunilor).

Dacă FairEmail se reține din cauza unor probleme de conectivitate anterioare, vă rugăm să consultați [acest FAQ](#user-content-faq123), puteți forța sincronizarea prin intermediul meniului cu trei puncte.

În setările de recepție puteți seta dimensiunea maximă pentru descărcarea automată a mesajelor în cazul conexiunilor contorizate.

Conexiunile mobile sunt aproape întotdeauna contorizate, iar unele hotspoturi Wi-Fi (cu plată) sunt și ele contorizate.

<br />

<a name="faq16"></a>
**(16) De ce nu sunt sincronizate mesajele?**

Cauzele posibile pentru care mesajele nu sunt sincronizate (trimise sau primite) sunt:

* Contul sau folderul (folderele) nu sunt setate pentru sincronizare
* Numărul de zile pentru care se sincronizează mesajul este setat prea mic
* Nu există o conexiune la internet utilizabilă
* Serverul de e-mail nu este temporar disponibil
* Android a oprit serviciul de sincronizare

Prin urmare, verificați setările contului și ale dosarului și verificați dacă conturile/dosarele sunt conectate (consultați legenda din meniul de navigare pentru semnificația pictogramelor).

Dacă apar mesaje de eroare, vă rugăm să consultați [acest FAQ](#user-content-faq22).

Pe unele dispozitive, în cazul în care există o mulțime de aplicații care concurează pentru memorie, Android poate opri serviciul de sincronizare ca ultimă soluție.

Unele versiuni Android opresc aplicațiile și serviciile prea agresiv. Consultați [acest site dedicat](https://dontkillmyapp.com/) și [acest număr Android](https://issuetracker.google.com/issues/122098785) pentru mai multe informații.

Dezactivarea optimizărilor bateriei (etapa de configurare 3) reduce șansele ca Android să oprească serviciul de sincronizare.

În cazul unor erori de conectare succesive, FairEmail va aștepta din ce în ce mai mult timp pentru a nu consuma bateria dispozitivului dumneavoastră. Acest lucru este descris în [acest FAQ](#user-content-faq123).

<br />

<a name="faq17"></a>
**~~~(17) De ce nu funcționează sincronizarea manuală?~~**

~~În cazul în care meniul *Sincronizează acum* este întunecat, nu există nici o conexiune cu contul.~~

~~See the previous question for more information.~~

<br />

<a name="faq18"></a>
**(18) De ce nu este afișată întotdeauna previzualizarea mesajului?**

Previzualizarea textului mesajului nu poate fi afișată în cazul în care corpul mesajului nu a fost încă descărcat. A se vedea, de asemenea, [acest FAQ](#user-content-faq15).

<br />

<a name="faq19"></a>
**(19) De ce sunt atât de scumpe caracteristicile pro?**

În primul rând, **FairEmail este practic gratuit pentru utilizare** și doar unele caracteristici avansate trebuie achiziționate.

În primul rând, **FairEmail este practic gratuit** și doar unele caracteristici avansate trebuie să fie achiziționate.

În primul rând, **FairEmail este practic gratuit** și doar unele caracteristici avansate trebuie să fie achiziționate.

Vă rugăm să consultați descrierea aplicației din Play Store sau [vezi aici](https://email.faircode.eu/#pro) pentru o listă completă a funcțiilor pro.

Întrebarea corectă este "*de ce există atât de multe taxe și impozite?*":

* TVA: 25 % (în funcție de țara dumneavoastră)
* Taxa Google: 30 %
* Impozitul pe venit: 50 %
* <sub>Taxa Paypal: 5-10 % în funcție de țară/montantă</sub>

Astfel, ceea ce rămâne pentru dezvoltator este doar o fracțiune din ceea ce plătiți dumneavoastră.

Rețineți, de asemenea, că majoritatea aplicațiilor gratuite par să nu fie sustenabile în cele din urmă, în timp ce FairEmail este întreținut și susținut în mod corespunzător, și că aplicațiile gratuite pot avea o capcană, cum ar fi trimiterea de informații sensibile la confidențialitate pe internet. Nici în aplicație nu există reclame care încalcă confidențialitatea.

Am lucrat la FairEmail aproape zilnic timp de mai bine de doi ani, așa că prețul mi se pare mai mult decât rezonabil. Din acest motiv, nu vor exista nici reduceri.

<br />

<a name="faq20"></a>
**(20) Pot obține o rambursare?**

Dacă o caracteristică pro achiziționată nu funcționează așa cum a fost concepută și aceasta nu este cauzată de o problemă a funcțiilor gratuite și nu pot rezolva problema în timp util, puteți obține o rambursare. În toate celelalte cazuri, nu este posibilă nicio rambursare. În nici un caz nu este posibilă o rambursare pentru orice problemă legată de funcțiile gratuite, deoarece nu s-a plătit nimic pentru ele și deoarece pot fi evaluate fără nicio limitare. Îmi asum responsabilitatea ca vânzător de a livra ceea ce am promis. și mă aștept ca dumneavoastră să vă asumați responsabilitatea de a vă informa cu privire la ceea ce cumpărați.

<a name="faq21"></a>
**(21) Cum pot activa lumina de notificare?**

Înainte de Android 8 Oreo: există o opțiune avansată în setările de notificare ale aplicației pentru acest lucru.

Android 8 Oreo și versiunile ulterioare: consultați [aici](https://developer.android.com/training/notify-user/channels) despre modul de configurare a canalelor de notificare. Puteți utiliza butonul *Canal implicit* din setările de notificare ale aplicației pentru a merge direct la setările corecte ale canalului de notificare Android.

Rețineți că aplicațiile nu mai pot modifica setările de notificare, inclusiv setarea luminii de notificare, pe Android 8 Oreo și ulterior.

Uneori este necesar să dezactivați setarea *Afișează previzualizarea mesajelor în notificări*. sau să activați setările *Afișează notificările doar cu o previzualizare a textului* pentru a remedia erori din Android. Acest lucru se poate aplica și la sunetele de notificare și la vibrații.

Setarea unei culori deschise înainte de Android 8 nu este acceptată, iar pe Android 8 și ulterior nu este posibilă.

<br />

<a name="faq22"></a>
**(22) Ce înseamnă eroare de cont/folder ...?**

FairEmail nu ascunde erorile, așa cum fac adesea aplicațiile similare, astfel încât este mai ușor de diagnosticat problemele.

FairEmail va încerca automat să se conecteze din nou după o întârziere. Această întârziere va fi dublată după fiecare încercare nereușită pentru a preveni epuizarea bateriei și pentru a împiedica blocarea permanentă. Vă rugăm să consultați [acest FAQ](#user-content-faq123) pentru mai multe informații în acest sens.

Există erori generale și erori specifice conturilor Gmail (a se vedea mai jos).

**Erori generale**

<a name="authfailed"></a>
Eroarea *... **Autentificarea a eșuat** ...* sau *... AUTHENTICATE failed ...* înseamnă probabil că numele de utilizator sau parola dvs. a fost incorectă. Unii furnizori se așteaptă ca nume de utilizator doar *username*, iar alții se așteaptă la adresa de e-mail completă *username@example.com*. La copierea/lipirea pentru a introduce un nume de utilizator sau o parolă, pot fi copiate caractere invizibile, ceea ce ar putea cauza și această problemă. Se știe că și unii manageri de parole fac acest lucru în mod incorect. Este posibil ca numele de utilizator să fie sensibil la majuscule și minuscule, așa că încercați să folosiți numai caractere minuscule. Parola este aproape întotdeauna sensibilă la majuscule și minuscule. Unii furnizori solicită utilizarea unei parole de aplicație în loc de parola contului, așa că vă rugăm să verificați documentația furnizorului. Uneori este necesar să activați mai întâi accesul extern (IMAP/SMTP) pe site-ul web al furnizorului. Alte cauze posibile sunt faptul că contul este blocat sau că logarea a fost restricționată din punct de vedere administrativ într-un anumit fel, de exemplu, permițând conectarea numai din anumite rețele/ adrese IP.

Dacă este necesar, puteți actualiza o parolă în setările contului: meniul de navigare (meniul din stânga), atingeți *Settings*, atingeți *Manual setup*, atingeți *Accounts* și atingeți contul. În majoritatea cazurilor, schimbarea parolei contului va schimba automat și parola identităților conexe. În cazul în care contul a fost autorizat cu OAuth prin intermediul expertului de configurare rapidă în loc de o parolă, puteți să rulați din nou expertul de configurare rapidă și să bifați *Autorize existing account again* pentru a autentifica din nou contul. Rețineți că acest lucru necesită o versiune recentă a aplicației.

Eroarea *... Prea multe încercări de autentificare greșite ...* înseamnă probabil că utilizați o parolă de cont Yahoo în loc de o parolă de aplicație. Vă rugăm să consultați [acest FAQ](#user-content-faq88) despre cum să vă creați un cont Yahoo.

Mesajul *... +OK ...* înseamnă probabil că un port POP3 (de obicei, portul 995) este utilizat pentru un cont IMAP (de obicei, portul 993).

Erorile *... salut invalid ...*, *... necesită o adresă validă ...* și *... Parametrul la HELO nu este conform cu sintaxa RFC ...* poate fi probabil rezolvat prin modificarea setării avansate de identitate *Utilizează adresa IP locală în loc de numele de gazdă*.

Eroarea *... Couldn't connect to host ...* înseamnă că nu a existat niciun răspuns de la serverul de e-mail într-un interval de timp rezonabil (20 de secunde în mod implicit). În general, acest lucru indică probleme de conectivitate la internet, posibil cauzate de un VPN sau de o aplicație firewall. Puteți încerca să măriți timpul de așteptare a conexiunii în setările de conexiune ale FairEmail, pentru cazurile în care serverul de e-mail este foarte lent.

Eroarea *... Conexiune refuzată ...* înseamnă că serverul de e-mail sau ceva între serverul de e-mail și aplicație, cum ar fi un firewall, a refuzat în mod activ conexiunea.

Eroarea *... Network unreachable ...* înseamnă că serverul de e-mail nu a putut fi accesat prin intermediul conexiunii curente la internet, de exemplu, deoarece traficul de internet este restricționat doar la traficul local.

Eroarea *... Gazda este nerezolvată ...*, *... Nu se poate rezolva găzduirea ...* sau *... Nici o adresă asociată cu numele de gazdă ...* înseamnă că adresa serverului de e-mail nu a putut fi rezolvată într-o adresă IP. Acest lucru poate fi cauzat de un VPN, de blocarea reclamelor sau de un server [DNS](https://en.wikipedia.org/wiki/Domain_Name_System) (local) inaccesibil sau care nu funcționează corect.

Eroarea *... Software-ul a cauzat întreruperea conexiunii ...* înseamnă că serverul de e-mail sau ceva între FairEmail și serverul de e-mail a încheiat în mod activ o conexiune existentă. Acest lucru se poate întâmpla, de exemplu, atunci când conectivitatea a fost pierdută brusc. Un exemplu tipic este activarea modului de zbor.

Erorile *... BYE Deconectare ...*, *... Resetarea conexiunii ...* înseamnă că serverul de e-mail sau ceva între serverul de e-mail și aplicație, de exemplu un router sau un firewall (aplicație), a încheiat în mod activ o conexiune existentă.

Eroarea *... Conexiunea închisă de omolog ...* poate fi cauzată de un server Exchange neactualizat, consultați [aici](https://blogs.technet.microsoft.com/pki/2010/09/30/sha2-and-windows/) pentru mai multe informații.

Erorile *... Eroare de citire ...*, *... Eroare de scriere ...*, *... Citirea a expirat ...*, *... Broken pipe ...* înseamnă că serverul de e-mail nu mai răspunde sau că conexiunea la internet este proastă.

<a name="connectiondropped"></a>
Eroarea *... Conexiune abandonată de server? ...* înseamnă că serverul de e-mail a întrerupt conexiunea în mod neașteptat. Acest lucru se întâmplă uneori atunci când au existat prea multe conexiuni într-un timp prea scurt sau când o parolă greșită a fost folosită de prea multe ori. În acest caz, asigurați-vă că parola dvs. este corectă și dezactivați recepția în setările de recepție timp de aproximativ 30 de minute și încercați din nou. Dacă este necesar, consultați [acest FAQ](#user-content-faq23) despre cum puteți reduce numărul de conexiuni.

Eroarea *... Sfârșitul neașteptat al fluxului de intrare zlib ...* înseamnă că nu au fost primite toate datele, posibil din cauza unei conexiuni proaste sau întrerupte.

Eroarea *... connection failure ...* ar putea indica [Prea multe conexiuni simultane](#user-content-faq23).

Avertismentul *... Codificare neacceptată ...* înseamnă că setul de caractere al mesajului este necunoscut sau neacceptat. FairEmail va lua în considerare ISO-8859-1 (Latin1), ceea ce, în majoritatea cazurilor, va duce la afișarea corectă a mesajului.

Eroarea *... Login Rate Limit Hit ...* înseamnă că au existat prea multe încercări de conectare cu o parolă incorectă. Vă rugăm să verificați de două ori parola sau să autentificați din nou contul cu ajutorul expertului de configurare rapidă (numai OAuth).

Vă rugăm să [vezi aici](#user-content-faq4) pentru erorile *... Neîncredere ... nu în certificat ...*, *... Certificat de securitate invalid (Nu se poate verifica identitatea serverului) ...* sau *... Nu s-a găsit ancora de încredere pentru calea de certificare ...*

Vă rugăm să [vezi aici](#user-content-faq127) pentru eroarea *... Argument(e) HELO invalid din punct de vedere sintactic ...*.

Vă rugăm să [vezi aici](#user-content-faq41) pentru eroarea *... Handshake eșuat ...*.

Consultați [aici](https://linux.die.net/man/3/connect) pentru a afla ce înseamnă codurile de eroare precum EHOSTUNREACH și ETIMEDOUT.

Cauzele posibile sunt:

* Un firewall sau un router blochează conexiunile la server
* Numele de gazdă sau numărul de port nu este valid
* Există probleme cu conexiunea la internet
* Există probleme cu rezolvarea numelor de domenii (Yandex: încercați să dezactivați DNS privat în setările Android)
* Serverul de e-mail refuză să accepte conexiuni (externe)
* Serverul de e-mail refuză să accepte un mesaj, de exemplu, pentru că este prea mare sau conține link-uri inacceptabile
* Există prea multe conexiuni la server, consultați și următoarea întrebare

Multe rețele Wi-Fi publice blochează e-mailurile de ieșire pentru a preveni spam-ul. Uneori, puteți rezolva această problemă utilizând un alt port SMTP. Consultați documentația furnizorului pentru numerele de port utilizabile.

Dacă utilizați un [VPN](https://en.wikipedia.org/wiki/Virtual_private_network), furnizorul VPN ar putea bloca conexiunea deoarece încearcă prea agresiv să prevină spam-ul. Rețineți că și [Google Fi](https://fi.google.com/) utilizează un VPN de asemenea.

**Trimiteți erori**

Serverele SMTP pot respinge mesajele pentru [o varietate de motive](https://en.wikipedia.org/wiki/List_of_SMTP_server_return_codes). Cele mai frecvente motive sunt mesajele prea mari și declanșarea filtrului de spam al unui server de e-mail.

* Limita de dimensiune a atașamentelor pentru Gmail [este de 25 MB](https://support.google.com/mail/answer/6584)
* Limita de dimensiune a atașamentelor pentru Outlook și Office 365 [este de 20 MB](https://support.microsoft.com/en-us/help/2813269/attachment-size-exceeds-the-allowable-limit-error-when-you-add-a-large)
* Limita de dimensiune a atașamentelor pentru Yahoo [este de 25 MB](https://help.yahoo.com/kb/SLN5673.html)
* *554 5.7.1 Serviciu indisponibil; gazdă client xxx.xxx.xxx.xxx.xxx blocată*, vă rugăm [vezi aici](https://docs.gandi.net/en/gandimail/faq/error_types/554_5_7_1_service_unavailable.html)
* *501 Eroare de sintaxă - linie prea lungă* este adesea cauzată de utilizarea unui antet Autocrypt lung
* *503 5.5.0 Destinatar deja specificat* în general înseamnă că o adresă este folosită atât ca adresă TO cât și CC
* *554 5.7.1 ... not permitted to relay* înseamnă că serverul de e-mail nu recunoaște numele de utilizator/adresa de e-mail. Vă rugăm să verificați de două ori numele de gazdă și numele de utilizator/adresa de e-mail în setările de identitate.
* *550 Mesaj spam respins deoarece IP-ul este listat de ...* înseamnă că serverul de e-mail a respins trimiterea unui mesaj de la adresa de rețea curentă (publică) deoarece aceasta a fost folosită în mod abuziv pentru a trimite spam de către (sperăm) altcineva înainte. Vă rugăm să încercați să activați modul de zbor timp de 10 minute pentru a obține o nouă adresă de rețea.
* *550 Ne pare rău, dar nu vă putem trimite e-mailul. Fie subiectul, fie un link sau un atașament poate conține spam, fie phishing sau malware.* înseamnă că furnizorul de e-mail consideră un mesaj de ieșire ca fiind dăunător.
* *571 5.7.1 Mesajul conține spam sau virus sau expeditorul este blocat ...* înseamnă că serverul de e-mail a considerat un mesaj de ieșire ca fiind spam. Acest lucru înseamnă probabil că filtrele de spam ale serverului de e-mail sunt prea stricte. Va trebui să contactați furnizorul de e-mail pentru asistență în acest sens.
* *451 4.7.0 Eroare temporară a serverului. Vă rugăm să încercați din nou mai târziu. PRX4 ...*: vă rugăm să [vezi aici](https://www.limilabs.com/blog/office365-temporary-server-error-please-try-again-later-prx4) sau [vezi aici](https://judeperera.wordpress.com/2019/10/11/fixing-451-4-7-0-temporary-server-error-please-try-again-later-prx4/).
* *571 5.7.1 Accesul la releu refuzat*: vă rugăm să verificați de două ori numele de utilizator și adresa de e-mail în setările avansate de identitate (prin intermediul configurării manuale).

Dacă doriți să utilizați serverul SMTP Gmail pentru a evita un filtru de spam de ieșire prea strict sau pentru a îmbunătăți livrarea mesajelor:

* Verifică-ți adresa de e-mail [aici](https://mail.google.com/mail/u/0/#settings/accounts) (va trebui să folosești un browser desktop pentru asta)
* Modificați setările de identitate astfel (Setări, atingeți Configurare manuală, atingeți Identități, atingeți Identitate):

&emsp;&emsp;Nume de utilizator: *adresa ta de Gmail*<br /> &emsp;&emsp;Parola: *[o parolă de aplicație](#user-content-faq6)*<br />. &emsp;&emsp;Host: *smtp.gmail.com*<br />. &emsp;&emsp;Port: *465*<br /> &emsp;&emsp;Criptare: *SSL/TLS*<br /> &emsp;&emsp;Răspundeți la adresa: *adresa dvs. de e-mail* (setări avansate de identitate)<br />

<br />

**Erori Gmail**

Autorizarea conturilor Gmail configurate cu ajutorul expertului rapid trebuie să fie actualizată periodic. prin intermediul [Android account manager](https://developer.android.com/reference/android/accounts/AccountManager). Acest lucru necesită permisiuni pentru contacte/conturi și conectivitate la internet.

În caz de erori, este posibil să autorizați/restaurați din nou un cont Gmail prin intermediul asistentului de configurare rapidă Gmail.

Eroarea *... Autentificarea a eșuat ... Contul nu a fost găsit ...* înseamnă că un cont Gmail autorizat anterior a fost eliminat din dispozitiv.

Erorile *... Autentificarea a eșuat ... No token ...* înseamnă că managerul de cont Android nu a reușit să reîmprospăteze autorizația unui cont Gmail.

Eroarea *... Autentificarea a eșuat ... eroare de rețea ...* înseamnă că managerul de cont Android nu a putut reîmprospăta autorizarea unui cont Gmail din cauza unor probleme cu conexiunea la internet

Eroarea *... Autentificarea a eșuat ... Credențiale invalide ...* ar putea fi cauzate de schimbarea parolei contului sau prin revocarea permisiunilor necesare pentru cont/contacte. În cazul în care parola contului a fost schimbată, va trebui să autentificați din nou contul Google în setările contului Android. În cazul în care permisiunile au fost revocate, puteți lansa expertul de configurare rapidă Gmail pentru a acorda din nou permisiunile necesare (nu este necesar să configurați din nou contul).

Eroarea *... ServiceDisabled ...* ar putea fi cauzată de înscrierea în [Programul de protecție avansată](https://landing.google.com/advancedprotection/): "*Pentru a vă citi e-mailurile, puteți (trebuie) să utilizați Gmail - Nu veți putea utiliza contul Google cu unele (toate) aplicațiile & servicii care necesită acces la date sensibile, cum ar fi e-mailurile dvs *"", a se vedea [aici](https://support.google.com/accounts/answer/7519408?hl=en&ref_topic=9264881).

Atunci când aveți îndoieli, puteți solicita [sprijin](#user-content-support).

<br />

<a name="faq23"></a>
**(23) De ce primesc alerte... ?**

*Informații generale*

Alertele sunt mesaje de avertizare trimise de serverele de e-mail.

*Prea multe conexiuni simultane* sau *Numărul maxim de conexiuni depășit*

Această alertă va fi trimisă atunci când există prea multe conexiuni de dosare pentru același cont de e-mail în același timp.

Cauzele posibile sunt:

* Există mai mulți clienți de e-mail conectați la același cont
* Același client de e-mail este conectat de mai multe ori la același cont
* Conexiunile anterioare au fost întrerupte brusc, de exemplu prin pierderea bruscă a conectivității la internet

Încercați mai întâi să așteptați ceva timp pentru a vedea dacă problema se rezolvă de la sine, altfel:

* fie treceți la verificarea periodică a mesajelor în setările de primire, ceea ce va duce la deschiderea dosarelor unul câte unul
* sau setați unele dosare pentru a fi interogate în loc de sincronizare (apăsați lung pe dosar în lista de dosare, editați proprietățile)

O modalitate simplă de a configura verificarea periodică a mesajelor pentru toate dosarele, cu excepția căsuței de primire este de a utiliza *Aplică la toate ...* în meniul cu trei puncte din lista de dosare și de a bifa cele două căsuțe de selectare avansate din partea de jos.

Numărul maxim de conexiuni simultane la dosare pentru Gmail este de 15, astfel încât puteți sincroniza cel mult 15 dosare simultan pe *toate* dispozitivele dvs. în același timp. Din acest motiv, folderele Gmail *user* sunt setate în mod implicit pentru interogare în loc de sincronizare permanentă. Atunci când este necesar sau dorit, puteți modifica acest lucru prin apăsarea lungă a unui dosar din lista de dosare și prin selectarea *Edit properties*. Consultați [aici](https://support.google.com/mail/answer/7126229) pentru detalii.

Atunci când se utilizează un server Dovecot, este posibil să doriți să modificați setarea [mail_max_userip_connections](https://doc.dovecot.org/settings/dovecot_core_settings/#mail-max-userip-connections).

Rețineți că serverul de e-mail va avea nevoie de ceva timp pentru a descoperi conexiunile întrerupte, de exemplu din cauza ieșirii din raza de acoperire a unei rețele, ceea ce înseamnă că, efectiv, doar jumătate din conexiunile de dosare sunt disponibile. Pentru Gmail, aceasta ar fi doar 7 conexiuni.

<br />

<a name="faq24"></a>
**(24) Ce sunt mesajele de navigare pe server?**

Răsfoirea mesajelor de pe server va prelua mesaje de pe serverul de e-mail în timp real. atunci când ajungeți la sfârșitul listei de mesaje sincronizate, chiar și atunci când dosarul este setat să nu se sincronizeze. Puteți dezactiva această funcție în setările avansate ale contului.

<br />

<a name="faq25"></a>
**(25) De ce nu pot selecta/opri/salva o imagine, un atașament sau un fișier?**

Atunci când un element de meniu pentru selectarea/deschiderea/salvarea unui fișier este dezactivat (atenuat) sau când primiți mesajul *Cadru de acces la stocare nu este disponibil*, probabil că [storage access framework](https://developer.android.com/guide/topics/providers/document-provider), o componentă standard Android, nu este prezentă. Acest lucru se poate datora faptului că ROM-ul dvs. personalizat nu o include sau pentru că a fost eliminată în mod activ (debloated).

FairEmail nu solicită permisiuni de stocare, astfel încât acest cadru este necesar pentru a selecta fișiere și foldere. Nicio aplicație, cu excepția poate a managerilor de fișiere, care vizează Android 4.4 KitKat sau o versiune ulterioară nu ar trebui să ceară permisiuni de stocare, deoarece ar permite accesul la *toate* fișierele.

Cadrul de acces la stocare este furnizat de pachetul *com.android.documentsui*, care este vizibil ca aplicația *Files* pe unele versiuni Android (notabil OxygenOS).

Puteți activa cadrul de acces la stocare (din nou) cu această comandă adb:

```
pm install -k --user 0 com.android.documentsui
```

Alternativ, este posibil să puteți activa din nou aplicația *Files* utilizând setările aplicației Android.

<br />

<a name="faq26"></a>
**(26) Pot să ajut la traducerea FairEmail în limba mea?**

Da, puteți traduce textele din FairEmail în limba dumneavoastră [pe Crowdin](https://crowdin.com/project/open-source-email). Înregistrarea este gratuită.

Dacă doriți ca numele sau pseudonimul dvs să fie inclus în lista de contribuitori din *Despre <0> aplicația, vă rog să mă [contactați](https://contact.faircode.eu/?product=fairemailsupport).</p>

<br />

<a name="faq27"></a>
**(27) Cum pot distinge între imaginile încorporate și cele externe?**

Imagine externă:

![Imagine externă](https://github.com/M66B/FairEmail/blob/master/images/baseline_image_black_48dp.png)

Imagine încorporată:

![Imagine încorporată](https://github.com/M66B/FairEmail/blob/master/images/baseline_photo_library_black_48dp.png)

Imagine ruptă:

![Imagine coruptă](https://github.com/M66B/FairEmail/blob/master/images/baseline_broken_image_black_48dp.png)

Rețineți că descărcarea imaginilor externe de pe un server de la distanță poate fi utilizată pentru a înregistra faptul că ați văzut un mesaj, ceea ce probabil nu doriți dacă mesajul este spam sau rău intenționat.

<br />

<a name="faq28"></a>
**(28) Cum pot gestiona notificările din bara de stare?**

În setările de notificare veți găsi un buton *Manage notifications* pentru a naviga direct la setările de notificări Android pentru FairEmail.

Pe Android 8.0 Oreo și versiunile ulterioare, puteți gestiona proprietățile canalelor de notificare individuale, de exemplu, pentru a seta un anumit sunet de notificare sau pentru a afișa notificările pe ecranul de blocare.

FairEmail are următoarele canale de notificare:

* Serviciu: utilizat pentru notificarea serviciului de sincronizare, a se vedea și [acest FAQ](#user-content-faq2)
* Trimitere: utilizat pentru notificarea serviciului de trimitere
* Notificări: folosit pentru notificări de mesaje noi
* Avertizare: utilizat pentru notificări de avertizare
* Eroare: utilizat pentru notificările de eroare

Consultați [aici](https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels) pentru detalii despre canalele de notificare. Pe scurt: apăsați pe numele canalului de notificare pentru a accesa setările canalului.

Pe Android înainte de Android 8 Oreo, puteți seta sunetul de notificare în setări.

Consultați [acest FAQ](#user-content-faq21) dacă dispozitivul dvs. are o lumină de notificare.

<br />

<a name="faq29"></a>
**(29) Cum pot primi notificări de mesaje noi pentru alte dosare?**

Trebuie doar să apăsați lung pe un dosar, selectați *Editați proprietățile*, și activați fie *Afișare în căsuța de primire unificată* ori *Notificați mesajele noi* (disponibilă numai pe Android 7 Nougat și versiunile ulterioare) și apăsați *Salvare*.

<br />

<a name="faq30"></a>
**(30) Cum pot utiliza setările rapide furnizate?**

Există setări rapide (dale de setări) disponibile pentru:

* activarea/dezactivarea globală a sincronizării
* afișarea numărului de mesaje noi și marcarea lor ca fiind văzute (nu citite)

Setările rapide necesită Android 7.0 Nougat sau o versiune ulterioară. Utilizarea plăcilor de setări este explicată [ aici](https://support.google.com/android/answer/9083864).

<br />

<a name="faq31"></a>
**(31) Cum pot utiliza comenzile rapide furnizate?**

Sunt disponibile comenzi rapide pentru a compune un mesaj nou către un contact preferat.

Comenzile rapide necesită Android 7.1 Nougat sau o versiune ulterioară. Utilizarea scurtăturilor este explicată [ aici](https://support.google.com/android/answer/2781850).

De asemenea, este posibil să creați comenzi rapide către dosare prin apăsarea lungă a unui dosar din lista de dosare a unui cont și prin selectarea *Add shortcut*.

<br />

<a name="faq32"></a>
**(32) Cum pot verifica dacă citirea e-mailului este cu adevărat sigură?**

Pentru aceasta, puteți utiliza [Email Privacy Tester](https://www.emailprivacytester.com/).

<br />

<a name="faq33"></a>
**(33) De ce nu funcționează adresele de expeditor editate?**

Majoritatea furnizorilor acceptă adrese validate numai pentru trimiterea de mesaje, pentru a preveni spam-ul.

De exemplu, Google modifică anteturile mesajelor în felul următor pentru adresele *unverified*:

```
De la: Cineva <somebody@example.org>
X-Google-Original-From: Cineva <somebody+extra@example.org>
```

Aceasta înseamnă că adresa de expeditor editată a fost înlocuită automat cu o adresă verificată înainte de trimiterea mesajului.

Rețineți că acest lucru este independent de primirea de mesaje.

<br />

<a name="faq34"></a>
**(34) Cum se potrivesc identitățile?**

Identitățile sunt, așa cum era de așteptat, corespondente în funcție de cont. Pentru mesajele primite se vor verifica adresele *to*, *cc*, *bcc*, *de* și *(X-)livrat/în plic/original la* (în această ordine) iar pentru mesajele de ieșire (ciorne, outbox și trimise) vor fi verificate numai adresele *from*. Adresele egale au prioritate față de adresele care se potrivesc parțial, cu excepția adreselor *delivered-to*.

Adresa corespunzătoare va fi afișată sub forma *via* în secțiunea de adrese a mesajelor primite (între antetul și textul mesajului).

Note that identities needs to be enabled to be able to be matched and that identities of other accounts will not be considered.

Potrivirea se va face o singură dată la primirea unui mesaj, astfel încât modificarea configurației nu va modifica mesajele existente. Totuși, puteți șterge mesajele locale prin apăsarea lungă a unui dosar din lista de dosare și să sincronizați din nou mesajele.

Este posibilă configurarea unui [regex](https://en.wikipedia.org/wiki/Regular_expression) în setările de identitate pentru a se potrivi cu **numele de utilizator** al unei adrese de e-mail (partea de dinaintea semnului @).

Rețineți că numele de domeniu (părțile de după semnul @) trebuie să fie întotdeauna egal cu numele de domeniu al identității.

Dacă doriți să potriviți o adresă de e-mail de tip "catch-all", acest regex este în mare parte în regulă:

```
.*
```

Dacă doriți să potriviți adresele de e-mail cu scop special abc@example.com și xyx@example.com și doriți să aveți și o adresă de e-mail de rezervă main@example.com, puteți face ceva de genul următor:

* Identitate: abc@example.com; regex: **(?i)abc**
* Identitate: xyz@example.com; regex: **(?i)xyz**
* Identitate: main@example.com; regex: **^(?i)((?!abc|xyz).)\*$**

Identitățile potrivite pot fi utilizate pentru a codifica mesajele prin culoare. Culoarea identității are prioritate față de culoarea dosarului și a contului. Setarea culorilor de identitate este o caracteristică profesională.

<br />

<a name="faq35"></a>
**(35) De ce ar trebui să fiu atent la vizualizarea imaginilor, a atașamentelor, a mesajului original și la deschiderea legăturilor?**

Vizualizarea imaginilor stocate de la distanță (a se vedea și [acest FAQ](#user-content-faq27)) și deschiderea linkurilor ar putea nu numai să îi spună expeditorului că ați văzut mesajul, dar va dezvălui și adresa dvs. de IP. A se vedea, de asemenea, această întrebare: [De ce linkul de e-mail este mai periculos decât linkul de căutare web?](https://security.stackexchange.com/questions/241139/why-emails-link-is-more-dangerous-than-web-searchs-link).

Deschiderea atașamentelor sau vizualizarea unui mesaj original ar putea încărca conținut de la distanță și executa scripturi, care nu numai că ar putea cauza scurgeri de informații sensibile din punct de vedere al confidențialității, dar poate reprezenta și un risc de securitate.

Rețineți că persoanele de contact ar putea trimite, fără să știe, mesaje malițioase dacă au fost infectate cu programe malware.

FairEmail formatează din nou mesajele, ceea ce face ca acestea să aibă un aspect diferit de cel original, dar descoperă și link-uri de phishing.

Rețineți că mesajele reformatate sunt adesea mai ușor de citit decât mesajele originale, deoarece marginile sunt eliminate, iar culorile și dimensiunile fonturilor sunt standardizate.

Aplicația Gmail afișează imagini în mod implicit prin descărcarea imaginilor prin intermediul unui server proxy Google. Deoarece imaginile sunt descărcate de pe serverul sursă [în timp real](https://blog.filippo.io/how-the-new-gmail-image-proxy-works-and-what-this-means-for-you/), acest lucru este și mai puțin sigur, deoarece și Google este implicat fără a oferi prea multe beneficii.

Puteți afișa imagini și mesaje originale în mod implicit pentru expeditorii de încredere, de la caz la caz, bifând *Nu mai cereți acest lucru pentru ...*.

Dacă doriți să resetați aplicațiile implicite *Deschidere cu*, vă rugăm să [vezi aici](https://www.androidauthority.com/how-to-set-default-apps-android-clear-621269/).

<br />

<a name="faq36"></a>
**(36) Cum sunt criptate fișierele de setări?**

Versiunea scurtă: AES 256 bit

Versiunea lungă:

* Cheia de 256 de biți este generată cu *PBKDF2WithHmacSHA1* folosind o sare aleatoare sigură de 128 de biți și 65536 iterații
* Cifrarea este *AES/CBC/PKCS5Padding*

<br />

<a name="faq37"></a>
**(37) Cum sunt stocate parolele?**

Toate versiunile Android acceptate [cryptă toate datele utilizatorului](https://source.android.com/security/encryption), astfel încât toate datele, inclusiv numele de utilizator, parolele, mesajele etc., sunt stocate criptate.

Dacă dispozitivul este securizat cu un cod PIN, un model sau o parolă, puteți face vizibile parolele de cont și de identitate. Dacă acest lucru reprezintă o problemă deoarece partajați dispozitivul cu alte persoane, luați în considerare posibilitatea de a utiliza [profile de utilizator](https://www.howtogeek.com/333484/how-to-set-up-multiple-user-profiles-on-android/).

<br />

<a name="faq39"></a>
**(39) Cum pot reduce consumul de baterie al FairEmail?**

Versiunile recente de Android raportează în mod implicit *utilizarea aplicațiilor* sub formă de procent în ecranul de setări al bateriei Android. **Confusingly, *app usage* is not the same as *battery usage* and is not even directly related to battery usage!** The app usage (while in use) will be very high because FairEmail is using a foreground service which is considered as constant app usage by Android. Totuși, acest lucru nu înseamnă că FairEmail folosește în mod constant energia bateriei. Utilizarea reală a bateriei poate fi văzută prin navigarea pe acest ecran:

*Setări Android*, *Baterie*, meniu cu trei puncte *Utilizarea bateriei*, meniu cu trei puncte *Afișează utilizarea completă a dispozitivului*

Ca regulă generală, utilizarea bateriei ar trebui să fie sub sau, în orice caz, să nu fie cu mult mai mare decât *Mobile network standby*. Dacă nu este cazul, vă rugăm să activați *Optimizare automată* în setările de recepție. Dacă acest lucru nu vă ajută, vă rugăm [solicitați asistență](https://contact.faircode.eu/?product=fairemailsupport).

Este inevitabil ca sincronizarea mesajelor să utilizeze energia bateriei, deoarece necesită accesul la rețea și accesarea bazei de date a mesajelor.

Dacă comparați utilizarea bateriei FairEmail cu un alt client de e-mail, vă rugăm să vă asigurați că celălalt client de e-mail este configurat în mod similar. De exemplu, compararea între sincronizarea permanentă (mesaje push) și verificarea periodică (puțin frecventă) a mesajelor noi nu este o comparație corectă.

Reconectarea la un server de e-mail va utiliza o cantitate suplimentară de energie a bateriei, astfel încât o conexiune la internet instabilă va duce la o utilizare suplimentară a bateriei. De asemenea, unele servere de e-mail încheie prematur conexiunile inactive, în timp ce [standardul](https://tools.ietf.org/html/rfc2177) spune că o conexiune inactivă ar trebui să fie menținută deschisă timp de 29 de minute. În aceste cazuri, este posibil să doriți o sincronizare periodică, de exemplu, la fiecare oră, în loc de o sincronizare continuă. Rețineți că o interogare frecventă (mai mult de la fiecare 30-60 de minute) va consuma probabil mai multă energie a bateriei decât o sincronizare permanentă. deoarece conectarea la server și compararea mesajelor locale și la distanță sunt operațiuni costisitoare.

[La unele dispozitive](https://dontkillmyapp.com/) este necesar să *dezactivați* optimizările bateriei (pasul 3 de configurare) pentru a menține deschise conexiunile la serverele de e-mail. De fapt, lăsarea optimizărilor de baterie activate poate duce la o utilizare suplimentară a bateriei pentru toate dispozitivele, chiar dacă acest lucru pare contradictoriu!

Cea mai mare parte a utilizării bateriei, fără a lua în considerare vizualizarea mesajelor, se datorează sincronizării (primirea și trimiterea) mesajelor. Așadar, pentru a reduce consumul de baterie, setați numărul de zile pentru care se sincronizează mesajele la o valoare mai mică, mai ales dacă există multe mesaje recente într-un dosar. Apăsați lung pe un nume de dosar din lista de dosare și selectați *Edit properties* pentru a accesa această setare.

Dacă aveți conectivitate la internet cel puțin o dată pe zi, este suficient să sincronizați mesajele doar pentru o zi.

Rețineți că puteți seta numărul de zile pentru care să *păstrați* mesajele la un număr mai mare decât pentru care să *sincronizați* mesajele. De exemplu, puteți sincroniza inițial mesajele pentru un număr mare de zile, iar după ce acest lucru a fost finalizat să reduceți numărul de zile de sincronizare a mesajelor, dar să lăsați numărul de zile de păstrare a mesajelor. După ce ați redus numărul de zile de păstrare a mesajelor, este posibil să doriți să executați funcția de curățare din setările diverse pentru a elimina fișierele vechi.

În setările de primire puteți activa sincronizarea permanentă a mesajelor cu stele, ceea ce vă va permite să păstrați mesajele mai vechi, în timp ce sincronizați mesajele pentru un număr limitat de zile.

Dezactivarea opțiunii de folder *Descărcarea automată a textelor și atașamentelor mesajelor*. va avea ca rezultat un trafic de rețea mai mic și, prin urmare, o utilizare mai redusă a bateriei. Puteți dezactiva această opțiune, de exemplu, pentru dosarul trimis și pentru arhivă.

Sincronizarea mesajelor pe timp de noapte nu este de cele mai multe ori utilă, așa că puteți economisi bateria dacă nu o sincronizați noaptea. În setări, puteți selecta un program pentru sincronizarea mesajelor (aceasta este o caracteristică pro).

FairEmail va sincroniza în mod implicit lista de dosare la fiecare conexiune. Deoarece folderele nu sunt create, redenumite și șterse foarte des, puteți economisi din consumul de rețea și de baterie dacă dezactivați acest lucru în setările de recepție.

FairEmail va verifica în mod implicit dacă mesajele vechi au fost șterse de pe server la fiecare conexiune. Dacă nu vă deranjează faptul că mesajele vechi, care au fost șterse de pe server, sunt încă vizibile în FairEmail, puteți economisi ceva rețea și baterie dezactivând acest lucru în setările de recepție.

Unii furnizori nu respectă standardul IMAP și nu mențin conexiunile deschise suficient de mult timp, ceea ce obligă FairEmail să se reconecteze des, cauzând o utilizare suplimentară a bateriei. Puteți inspecta *Log* prin intermediul meniului principal de navigare pentru a verifica dacă există reconectări frecvente (conexiune închisă/restabilită, eroare de citire/scriere/timeout, etc.). Puteți rezolva această problemă reducând intervalul de păstrare a legăturii în setările avansate ale contului la, de exemplu, 9 sau 15 minute. Rețineți că optimizările bateriei trebuie să fie dezactivate în etapa 3 de configurare pentru a menține conexiunile în viață în mod fiabil.

Unii furnizori trimit la fiecare două minute ceva de genul "*Încă sunt aici*", ceea ce duce la creșterea traficului de rețea și la trezirea dispozitivului dvs. și provoacă o utilizare suplimentară inutilă a bateriei. Puteți inspecta *Log* prin intermediul meniului principal de navigare pentru a verifica dacă furnizorul dvs. face acest lucru. Dacă furnizorul dvs. utilizează [Dovecot](https://www.dovecot.org/) ca server IMAP, ați putea cere furnizorului dvs. să modifice setarea [imap_idle_notify_interval](https://wiki.dovecot.org/Timeouts) la o valoare mai mare sau, mai bine, să o dezactiveze. Dacă furnizorul dvs. nu poate sau nu este dispus să schimbe/dezactiveze acest lucru, ar trebui să vă gândiți să treceți la sincronizarea periodică în loc de cea continuă. Puteți modifica acest lucru în setările de recepție.

Dacă ați primit mesajul *Acest furnizor nu acceptă mesaje push* în timpul configurării unui cont, luați în considerare trecerea la un furnizor modern care acceptă mesaje push (IMAP IDLE) pentru a reduce consumul de baterie.

Dacă dispozitivul dvs. are un ecran [AMOLED](https://en.wikipedia.org/wiki/AMOLED), puteți economisi consumul de baterie în timpul vizualizării mesajelor prin trecerea la tema neagră.

Dacă este activată optimizarea automată în setările de recepție, un cont va trece automat la verificarea periodică a mesajelor noi atunci când serverul de e-mail:

* Spune '*Încă aici*' în 3 minute
* Serverul de e-mail nu acceptă mesaje push
* Intervalul keep-alive este mai mic de 12 minute

În plus, dosarele de gunoi și spam vor fi setate automat pentru a verifica dacă există mesaje noi. după trei erori succesive de [prea multe conexiuni simultane](#user-content-faq23).

<br />

<a name="faq40"></a>
**(40) Cum pot reduce consumul de date al FairEmail?**

Puteți reduce consumul de date practic în același mod în care reduceți consumul de baterie, consultați întrebarea anterioară pentru sugestii.

Este inevitabil ca datele să fie utilizate pentru a sincroniza mesajele.

În cazul în care conexiunea cu serverul de e-mail este pierdută, FairEmail va sincroniza din nou mesajele pentru a se asigura că niciun mesaj nu a fost pierdut. În cazul în care conexiunea este instabilă, aceasta poate duce la o utilizare suplimentară a datelor. În acest caz, este o idee bună să reduceți la minimum numărul de zile de sincronizare a mesajelor (a se vedea întrebarea anterioară). sau să treceți la sincronizarea periodică a mesajelor (setări de recepție).

Pentru a reduce consumul de date, puteți modifica aceste setări avansate de recepție:

* Verificați dacă mesajele vechi au fost eliminate de pe server: dezactivați
* Sincronizarea listei de dosare (partajate): dezactivați

În mod implicit, FairEmail nu descarcă texte de mesaje și atașamente mai mari de 256 KiB atunci când există o conexiune la internet contorizată (mobilă sau Wi-Fi cu plată). Puteți modifica acest lucru în setările de conectare.

<br />

<a name="faq41"></a>
**(41) Cum pot remedia eroarea 'Handshake failed' ?**

Există mai multe cauze posibile, așa că vă rugăm să citiți până la sfârșitul acestui răspuns.

Eroarea '*Handshake a eșuat... WRONG_VERSION_NUMBER ...*' poate însemna că încercați să vă conectați la un server IMAP sau SMTP. fără o conexiune criptată, utilizând de obicei portul 143 (IMAP) și portul 25 (SMTP), sau că se utilizează un protocol greșit (SSL/TLS sau STARTTLS).

Majoritatea furnizorilor oferă conexiuni criptate folosind diferite porturi, de obicei portul 993 (IMAP) și portul 465/587 (SMTP).

În cazul în care furnizorul dvs. nu acceptă conexiuni criptate, ar trebui să solicitați ca acest lucru să fie posibil. Dacă aceasta nu este o opțiune, ați putea activa *Allow insecure connections* atât în setările avansate, cât și în setările contului/identității.

A se vedea, de asemenea, [acest FAQ](#user-content-faq4).

Eroarea '*Handshake a eșuat ... SSLV3_ALERT_ILLEGAL_PARAMETER ...*' este cauzată fie de o eroare în implementarea protocolului SSL fie de o cheie DH prea scurtă pe serverul de e-mail și, din păcate, nu poate fi reparată de FairEmail.

Eroarea '*Handshake a eșuat... HANDSHAKE_FAILURE_ON_CLIENT_HELLO ...*" ar putea fi cauzată de faptul că furnizorul folosește încă RC4, care nu mai este acceptat de la [Android 7](https://developer.android.com/about/versions/nougat/android-7.0-changes.html#tls-ssl).

Eroarea '*Handshake a eșuat ... UNSUPPORTED_PROTOCOL sau TLSV1_ALERT_PROTOCOL_VERSION ...*' ar putea fi cauzată de activarea conexiunilor de întărire în setările de conexiune sau de faptul că Android nu mai acceptă protocoale mai vechi, cum ar fi SSLv3.

Android 8 Oreo și versiunile ulterioare [nu mai suportă](https://developer.android.com/about/versions/oreo/android-8.0-changes#security-all) SSLv3. Nu există nicio modalitate de a rezolva problema lipsei suportului RC4 și SSLv3, deoarece acesta a fost complet eliminat din Android (ceea ce ar trebui să spună ceva).

Puteți utiliza [acest site web](https://ssl-tools.net/mailservers) sau [acest site web](https://www.immuniweb.com/ssl/) pentru a verifica dacă serverele de e-mail au probleme cu SSL/TLS.

<br />

<a name="faq42"></a>
**(42) Puteți adăuga un nou furnizor în lista de furnizori?**

Dacă furnizorul este folosit de mai mult de câteva persoane, da, cu plăcere.

Sunt necesare următoarele informații:

```
<provider
    name="Gmail"
    link="https://support.google.com/mail/answer/7126229" // link to the instructions of the provider
    type="com.google"> // acest lucru nu este necesar
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

FEP [scrie](https://www.eff.org/nl/deeplinks/2018/06/announcing-starttls-everywhere-securing-hop-hop-email-delivery): "*În plus, chiar dacă configurați perfect STARTTLS și folosiți un certificat valid, tot nu există nicio garanție că comunicația dvs. va fi criptată.*"

Astfel, conexiunile SSL pure sunt mai sigure decât utilizarea [STARTTLS](https://en.wikipedia.org/wiki/Opportunistic_TLS) și, prin urmare, sunt preferate.

Vă rugăm să vă asigurați că primirea și trimiterea mesajelor funcționează corect înainte de a mă contacta pentru a adăuga un furnizor.

Vedeți mai jos cum mă puteți contacta.

<br />

<a name="faq43"></a>
**(43) Puteți arăta originalul ... ?**

Show original, afișează mesajul original așa cum a fost trimis de expeditor, inclusiv fonturile, culorile, marginile etc. originale. FairEmail nu modifică și nu va modifica acest lucru în nici un fel, cu excepția solicitării [TEXT_AUTOSIZING](https://developer.android.com/reference/android/webkit/WebSettings.LayoutAlgorithm), care va *încerca* să facă textul mic mai ușor de citit.

<br />

<a name="faq44"></a>
**~~~(44) Puteți afișa fotografiile de contact / identicons în dosarul trimis?~~**

~~Fotografiile de contact și identiconii sunt întotdeauna afișate pentru expeditor, deoarece acest lucru este necesar pentru firele de conversație.~~ ~~Obținerea fotografiilor de contact atât pentru expeditor, cât și pentru destinatar nu este o opțiune, deoarece obținerea fotografiilor de contact este o operațiune costisitoare.~~

<br />

<a name="faq45"></a>
**(45) Cum pot repara 'Această cheie nu este disponibilă. Pentru a o folosi, trebuie să o importați ca fiind a voastră!' ?**

Veți primi mesajul *Această cheie nu este disponibilă. Pentru a o folosi, trebuie să o importați ca fiind una din propriile dvs.* atunci când încercați să decriptați un mesaj cu o cheie publică. Pentru a remedia acest lucru, va trebui să importați cheia privată.

<br />

<a name="faq46"></a>
**(46) De ce se reîmprospătează mereu lista de mesaje?**

Dacă vedeți un 'spinner' în partea de sus a listei de mesaje, înseamnă că dosarul este încă în curs de sincronizare cu serverul la distanță. Puteți vedea progresul sincronizării în lista de dosare. Consultați legenda pentru a afla ce înseamnă pictogramele și numerele.

Viteza dispozitivului dvs. și a conexiunii la internet, precum și numărul de zile de sincronizare a mesajelor determină cât va dura sincronizarea. Rețineți că, în majoritatea cazurilor, nu ar trebui să setați numărul de zile de sincronizare a mesajelor la mai mult de o zi; consultați și [acest FAQ](#user-content-faq39).

<br />

<a name="faq47"></a>
**(47) Cum pot rezolva eroarea "Nu există un cont principal sau un dosar de ciorne" ?**

Veți primi mesajul de eroare *Nici un cont principal sau nici un dosar de ciorne* atunci când încercați să compuneți un mesaj în timp ce nu există un cont setat ca fiind contul principal sau când nu există un dosar de ciorne selectat pentru contul principal. Acest lucru se poate întâmpla, de exemplu, atunci când porniți FairEmail pentru a compune un mesaj dintr-o altă aplicație. FairEmail trebuie să știe unde să stocheze proiectul, așa că va trebui să selectați un cont care să fie contul principal și/sau să selectați un dosar de ciorne pentru contul principal.

Acest lucru se poate întâmpla, de asemenea, atunci când încercați să răspundeți la un mesaj sau să transmiteți un mesaj de pe un cont fără dosar de ciorne. în timp ce nu există un cont principal sau când contul principal nu are un dosar de ciorne.

Vă rugăm să consultați [acest FAQ](#user-content-faq141) pentru mai multe informații.

<br />

<a name="faq48"></a>
**~~~(48) Cum se rezolvă eroarea 'No primary account or no archive folder' ?~~**

~~Voi primi mesajul de eroare *Nu există un cont principal sau un dosar de arhivă* atunci când cauți mesaje dintr-o altă aplicație. FairEmail trebuie să știe unde să caute, așa că va trebui să selectați un cont care să fie contul principal și/sau să selectați un dosar de arhivă pentru contul principal.~~

<br />

<a name="faq49"></a>
**(49) Cum repar 'An outdated app sent a file path instead of a file stream' ?**

Probabil că ați selectat sau trimis un atașament sau o imagine cu un manager de fișiere neactualizat sau o aplicație depășită care presupune că toate aplicațiile au încă permisiuni de stocare. Din motive de securitate și confidențialitate, aplicațiile moderne precum FairEmail nu mai au acces complet la toate fișierele. Acest lucru poate avea ca rezultat mesajul de eroare *O aplicație depășită a trimis o cale de fișier în loc de un flux de fișiere*. în cazul în care un nume de fișier în loc de un flux de fișiere este partajat cu FairEmail, deoarece FairEmail nu poate deschide fișiere la întâmplare.

Puteți remedia acest lucru trecând la un manager de fișiere actualizat sau la o aplicație concepută pentru versiunile recente de Android. Alternativ, puteți acorda FairEmail acces de citire la spațiul de stocare de pe dispozitivul dvs. în setările aplicației Android. Rețineți că această soluție de rezolvare [nu va mai funcționa pe Android Q](https://developer.android.com/preview/privacy/scoped-storage).

A se vedea și [întrebarea 25](#user-content-faq25). și [ce scrie Google despre aceasta](https://developer.android.com/training/secure-file-sharing/share-file#RespondToRequest).

<br />

<a name="faq50"></a>
**(50) Puteți adăuga o opțiune pentru a sincroniza toate mesajele?**

Puteți sincroniza mai multe sau chiar toate mesajele prin apăsarea lungă a unui dosar (inbox) din lista de dosare a unui cont (atingeți ușor numele contului în meniul de navigare). și selectând *Synchronize more* ontextual.

<br />

<a name="faq51"></a>
**(51) Cum sunt sortate dosarele?**

Dosarele sunt sortate mai întâi în ordinea contului (în mod implicit după numele contului). și în cadrul unui cont, cu folderele speciale, de sistem în partea de sus, urmate de folderele setate pentru sincronizare. În cadrul fiecărei categorii, dosarele sunt sortate în funcție de numele (afișat). Puteți seta numele de afișare prin apăsarea lungă a unui dosar din lista de dosare și prin selectarea *Edit properties*.

Elementul de meniu de navigare (hamburger) *Order folders* din setări poate fi utilizat pentru a ordona manual folderele.

<br />

<a name="faq52"></a>
**(52) De ce durează ceva timp până la reconectarea la un cont?**

Nu există nicio modalitate sigură de a ști dacă o conexiune de cont a fost încheiată în mod elegant sau forțat. Încercarea de a se reconecta la un cont în timp ce conexiunea contului a fost încheiată cu forța prea des poate duce la probleme precum [prea multe conexiuni simultane](#user-content-faq23) sau chiar blocarea contului. Pentru a preveni astfel de probleme, FairEmail așteaptă 90 de secunde până când încearcă să se reconecteze din nou.

Puteți apăsa lung *Settings* în meniul de navigare pentru a vă reconecta imediat.

<br />

<a name="faq53"></a>
**(53) Puteți lipi bara de acțiune a mesajelor în partea de sus/jos?**

Bara de acțiune a mesajului funcționează pentru un singur mesaj, iar bara de acțiune de jos funcționează pentru toate mesajele din conversație. Deoarece într-o conversație există adesea mai multe mesaje, acest lucru nu este posibil. În plus, există câteva acțiuni specifice mesajelor, cum ar fi redirecționarea.

Mutarea barei de acțiune a mesajului în partea de jos a mesajului nu este atractivă din punct de vedere vizual, deoarece există deja o bară de acțiune a conversației în partea de jos a ecranului.

Rețineți că nu există prea multe aplicații de e-mail care să afișeze o conversație sub forma unei liste de mesaje extensibile. Acest lucru are o mulțime de avantaje, dar determină, de asemenea, necesitatea unor acțiuni specifice mesajelor.

<br />

<a name="faq54"></a>
**~~~(54) Cum pot folosi un prefix de spațiu de nume?~~**

~~Un prefix de spațiu de nume este folosit pentru a elimina automat prefixul pe care furnizorii îl adaugă uneori la numele dosarelor.~~

~~De exemplu, dosarul de spam Gmail se numește:~~

```
[Gmail]/Spam
```

~~Prin setarea prefixului namespace la *[Gmail]* FairEmail va elimina automat *[Gmail]/* din toate numele de dosare.~~

<br />

<a name="faq55"></a>
**(55) Cum pot marca toate mesajele ca fiind citite / muta sau șterge toate mesajele?**

Pentru aceasta, puteți utiliza selecția multiplă. Apăsați lung primul mesaj, nu ridicați degetul și glisați în jos până la ultimul mesaj. Apoi, utilizați butonul de acțiune cu trei puncte pentru a executa acțiunea dorită.

<br />

<a name="faq56"></a>
**(56) Puteți adăuga suport pentru JMAP?**

Nu există aproape niciun furnizor care să ofere protocolul [JMAP](https://jmap.io/), așa că nu merită un efort prea mare pentru a adăuga suport pentru acest protocol în FairEmail.

<br />

<a name="faq57"></a>
**(57) Pot folosi HTML în semnături?**

Da, puteți utiliza [HTML](https://en.wikipedia.org/wiki/HTML). În editorul de semnături, puteți trece la modul HTML prin intermediul meniului cu trei puncte.

Rețineți că, dacă reveniți la editorul de text, este posibil ca nu tot codul HTML să fie redat ca atare, deoarece editorul de text Android nu este capabil să redea tot codul HTML. În mod similar, dacă folosiți editorul de text, HTML-ul poate fi modificat în moduri neașteptate.

Dacă doriți să utilizați text preformatat, cum ar fi [ASCII art](https://en.wikipedia.org/wiki/ASCII_art), trebuie să înfășurați textul într-un element *pre*, astfel:

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
**(58) Ce înseamnă o pictogramă de e-mail deschisă/închisă?**

Pictograma de e-mail din lista de dosare poate fi deschisă (subliniată) sau închisă (solidă):

![Imagine externă](https://github.com/M66B/FairEmail/blob/master/images/baseline_mail_outline_black_48dp.png)

Corpul mesajelor și atașamentele nu sunt descărcate în mod implicit.

![Imagine externă](https://github.com/M66B/FairEmail/blob/master/images/baseline_email_black_48dp.png)

Corpul mesajelor și atașamentele sunt descărcate în mod implicit.

<br />

<a name="faq59"></a>
**(59) Pot fi deschise mesajele originale în browser?**

Din motive de securitate, fișierele cu textele originale ale mesajelor nu sunt accesibile altor aplicații, astfel încât acest lucru nu este posibil. Teoretic, [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) ar putea fi folosit pentru a partaja aceste fișiere, dar nici măcar Google Chrome de la Google nu poate gestiona acest lucru.

<br />

<a name="faq60"></a>
**(60) Știați că ... ?**

* Știați că mesajele marcate cu stea pot fi sincronizate/păstrate mereu? (acest lucru poate fi activat în setările de recepție)
* Știați că puteți apăsa lung pe pictograma "Scrieți un mesaj" pentru a merge în dosarul de ciorne?
* Știați că există o opțiune avansată pentru a marca mesajele citite atunci când sunt mutate? (arhivarea și aruncarea la gunoi este, de asemenea, în mișcare)
* Știați că puteți selecta un text (sau o adresă de e-mail) în orice aplicație de pe versiunile recente de Android și lăsați FairEmail să-l caute?
* Știați că FairEmail are un mod tabletă? Rotiți dispozitivul în modul peisaj, iar firele de conversație vor fi deschise într-o a doua coloană, dacă există suficient spațiu pe ecran.
* Știați că puteți apăsa lung pe un șablon de răspuns pentru a crea un proiect de mesaj din acel șablon?
* Știați că puteți apăsa lung, țineți apăsat și glisați pentru a selecta o serie de mesaje?
* Știați că puteți încerca din nou să trimiteți mesaje folosind pull-down-to-refresh în outbox?
* Știați că puteți glisa o conversație spre stânga sau spre dreapta pentru a trece la conversația următoare sau anterioară?
* Știați că puteți apăsa pe o imagine pentru a vedea de unde va fi descărcată?
* Știați că puteți apăsa lung pe pictograma de dosar din bara de acțiune pentru a selecta un cont?
* Știați că puteți apăsa lung pe pictograma stea într-un fir de conversație pentru a seta o stea colorată?
* Știați că puteți deschide sertarul de navigare glisând din stânga, chiar și atunci când vizualizați o conversație?
* Știați că puteți apăsa lung pe pictograma persoanelor pentru a afișa/ ascunde câmpurile CC/BCC și pentru a reține starea de vizibilitate pentru data viitoare?
* Știați că puteți insera adresele de e-mail ale unui grup de contacte Android prin intermediul meniului cu trei puncte de depășire?
* Știați că, dacă selectați un text și apăsați Răspundeți, doar textul selectat va fi citat?
* Știați că puteți apăsa lung pe pictogramele coș de gunoi (atât în mesaj, cât și în bara de acțiuni din partea de jos) pentru a șterge definitiv un mesaj sau o conversație? (versiunea 1.1368+)
* Știați că puteți apăsa lung pe acțiunea de trimitere pentru a afișa dialogul de trimitere, chiar dacă acesta a fost dezactivat?
* Știați că puteți apăsa lung pe pictograma de ecran complet pentru a afișa doar textul original al mesajului?
* Știați că puteți apăsa lung butonul de răspuns pentru a răspunde expeditorului? (de la versiunea 1.1562)

<br />

<a name="faq61"></a>
**(61) De ce sunt afișate unele mesaje în întuneric?**

Mesajele afișate întunecate (gri) sunt mesaje mutate local pentru care mutarea nu a fost încă confirmată de server. Acest lucru se poate întâmpla atunci când nu există (încă) o conexiune la server sau la cont. Aceste mesaje vor fi sincronizate după ce se realizează o conexiune la server și la cont. sau, dacă acest lucru nu se întâmplă niciodată, vor fi șterse dacă sunt prea vechi pentru a fi sincronizate.

Este posibil să fie nevoie să sincronizați manual dosarul, de exemplu, trăgând în jos.

Puteți vizualiza aceste mesaje, dar nu le puteți muta din nou până când nu este confirmată mutarea anterioară.

Operațiunile în așteptare [operațiuni](#user-content-faq3) sunt afișate în vizualizarea operațiunilor, accesibilă din meniul principal de navigare.

<br />

<a name="faq62"></a>
**(62) Ce metode de autentificare sunt acceptate?**

Următoarele metode de autentificare sunt acceptate și utilizate în această ordine:

* CRAM-MD5
* AUTENTIFICARE
* PLANE
* NTLM (netestat)
* XOAUTH2 ([Gmail](https://developers.google.com/gmail/imap/xoauth2-protocol), [Yandex](https://tech.yandex.com/oauth/))

Metodele de autentificare SASL, în afară de CRAM-MD5, nu sunt acceptate. deoarece [JavaMail pentru Android](https://javaee.github.io/javamail/Android) nu acceptă autentificarea SASL.

Dacă furnizorul dvs. solicită o metodă de autentificare neacceptată, veți primi probabil mesajul de eroare *authentication failed*.

[Certificatele clientului](https://en.wikipedia.org/wiki/Client_certificate) pot fi selectate în setările contului și ale identității.

[Se acceptă indicarea numelui serverului](https://en.wikipedia.org/wiki/Server_Name_Indication). de [toate versiunile Android acceptate](https://developer.android.com/training/articles/security-ssl).

<br />

<a name="faq63"></a>
**(63) Cum sunt redimensionate imaginile pentru a fi afișate pe ecrane?**

Imagini mari în linie sau atașate [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) și [JPEG](https://en.wikipedia.org/wiki/JPEG) imagini mari vor fi redimensionate automat pentru afișarea pe ecrane. Acest lucru se datorează faptului că dimensiunea mesajelor de e-mail este limitată, în funcție de furnizor, în general între 10 și 50 MB. Imaginile vor fi redimensionate în mod implicit la o lățime și o înălțime maximă de aproximativ 1440 de pixeli și vor fi salvate cu un raport de compresie de 90 %. Imaginile sunt redimensionate folosind factori de numere întregi pentru a reduce utilizarea memoriei și pentru a păstra calitatea imaginii. Redimensionarea automată a imaginilor în linie și/sau atașate și dimensiunea maximă a imaginii țintă pot fi configurate în setările de trimitere.

Dacă doriți să redimensionați imaginile de la caz la caz, puteți utiliza [Send Reduced](https://f-droid.org/en/packages/mobi.omegacentauri.SendReduced/) sau o aplicație similară.

<br />

<a name="faq64"></a>
**~~(64) Puteți adăuga acțiuni personalizate pentru glisarea la stânga/dreapta?~~**

~~ Cel mai natural lucru pe care trebuie să-l faci atunci când glisezi o intrare din listă spre stânga sau spre dreapta este să elimini intrarea din listă.~~ ~~Acțiunea cea mai naturală în contextul unei aplicații de e-mail este mutarea mesajului din folder în alt folder.~~ ~~Puteți selecta dosarul în care să mutați în setările contului.~~

~~ Alte acțiuni, cum ar fi marcarea mesajelor citite și mesaje de așteptare sunt disponibile prin selecție multiplă.~~ ~~Puteți apăsa lung pe un mesaj pentru a începe selecția multiplă. Vezi și [această întrebare](#user-content-faq55).~~

~~Să glisezi spre stânga sau spre dreapta pentru a marca un mesaj citit sau necitit este nefiresc, deoarece mesajul mai întâi dispare și apoi revine într-o formă diferită.~~ ~~Rețineți că există o opțiune avansată pentru a marca automat mesajele citite la deplasare,~~ ~~care în cele mai multe cazuri este un înlocuitor perfect pentru secvența marchează citite și mutate într-un anumit dosar.~~ ~~ De asemenea, puteți marca mesajele citite din notificările de mesaje noi.~~

~~Dacă doriți să citiți un mesaj mai târziu, îl puteți ascunde până la o anumită oră folosind meniul *snooze*.~~

<br />

<a name="faq65"></a>
**(65) De ce unele atașamente sunt afișate atenuate?**

Atașamentele în linie (imagine) sunt afișate în întuneric. [Arhivele atașate în linie](https://tools.ietf.org/html/rfc2183) ar trebui să fie descărcate și afișate automat, dar cum FairEmail nu descarcă întotdeauna atașamentele automat, consultați și [acest FAQ](#user-content-faq40), FairEmail afișează toate tipurile de atașamente. Pentru a face distincția între atașamentele în linie și cele obișnuite, atașamentele în linie sunt afișate în întuneric.

<br />

<a name="faq66"></a>
**(66) Este FairEmail disponibil în Google Play Family Library?**

"*Nu puteți partaja achizițiile din aplicație și aplicațiile gratuite cu membrii familiei dumneavoastră.*"

Vezi [aici](https://support.google.com/googleone/answer/7007852) la "*Vezi dacă conținutul este eligibil pentru a fi adăugat la Biblioteca familiei*", "*Aplicații & jocuri*".

<br />

<a name="faq67"></a>
**(67) Cum pot să amân conversațiile?**

Selectați în mod multiplu una sau mai multe conversații (apăsați lung pentru a începe selecția multiplă), atingeți butonul cu trei puncte și selectați *Snooze ...*. Alternativ, în vizualizarea extinsă a mesajului, utilizați *Snooze ...* în meniul "mai mult" cu trei puncte din mesaj sau acțiunea time-lapse din bara de acțiune de jos. Selectați ora la care conversația (conversațiile) trebuie să fie amânată și confirmați atingând OK. Conversațiile vor fi ascunse pentru perioada de timp selectată și vor fi afișate din nou după aceea. Veți primi o notificare de mesaj nou ca memento.

De asemenea, este posibil să se amâne mesajele cu [o regulă](#user-content-faq71), care vă va permite, de asemenea, să mutați mesajele într-un dosar pentru a le permite să fie amânate automat.

Puteți să afișați mesajele amânate debifând *Filtrează* > *Ascunde* în meniul de suprapunere cu trei puncte.

Puteți apăsa pe pictograma mică de amânare pentru a vedea până când o conversație este amânată.

Prin selectarea unei durate zero a somnului, puteți anula somnul.

Aplicațiile terță parte nu au acces la dosarul de mesaje amânate din Gmail.

<br />

<a name="faq68"></a>
**~~(68) De ce nu poate Adobe Acrobat reader să deschidă atașamentele PDF / aplicațiile Microsoft nu pot deschide documentele atașate?~~**

~~Adobe Acrobat reader și aplicațiile Microsoft așteaptă în continuare acces complet la toate fișierele stocate,~~ ~~ în timp ce aplicațiile ar trebui să utilizeze [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) începând cu Android KitKat (2013)~~ ~~pentru a avea acces doar la fișierele partajate în mod activ. Acest lucru se face din motive de confidențialitate și securitate.~~

~~ Puteți rezolva acest lucru salvând fișierul atașat și deschizându-l din Adobe Acrobat reader / Microsoft app,~~ ~~dar vi se recomandă să instalați un cititor de PDF / vizualizator de documente actualizat și, de preferință, open source,~~ ~~de exemplu unul listat [ aici](https://github.com/offa/android-foss#-document--pdf-viewer).~~

<br />

<a name="faq69"></a>
**(69) Puteți adăuga derulare automată în sus la un mesaj nou?**

Lista de mesaje este derulată automat în sus atunci când navigați de la o notificare de mesaj nou sau după o reîmprospătare manuală. Derularea automată în sus la sosirea noilor mesaje ar interfera cu propria derulare, dar, dacă doriți, puteți activa acest lucru în setări.

<br />

<a name="faq70"></a>
**(70) Când vor fi extinse automat mesajele?**

La navigarea într-o conversație, un mesaj va fi extins dacă:

* Există doar un singur mesaj în conversație
* Există exact un mesaj necitit în conversație
* Există exact un mesaj marcat cu stea (favorit) în conversație (din versiunea 1.1508)

Există o singură excepție: mesajul nu a fost încă descărcat. și mesajul este prea mare pentru a fi descărcat automat pe o conexiune cu contorizare (mobilă). Puteți seta sau dezactiva dimensiunea maximă a mesajului în fila de setări "connection".

Mesajele duplicate (arhivate), mesajele aruncate la coșul de gunoi și mesajele provizorii nu sunt luate în considerare.

Mesajele vor fi marcate automat ca fiind citite la extindere, cu excepția cazului în care acest lucru a fost dezactivat în setările contului individual.

<br />

<a name="faq71"></a>
**(71) Cum se utilizează regulile de filtrare?**

Puteți edita regulile de filtrare prin apăsarea lungă a unui dosar din lista de dosare a unui cont (atingeți ușor numele contului în meniul de navigare/lateral).

Noile reguli se vor aplica la mesajele noi primite în dosar, nu la mesajele existente. Puteți să verificați regula și să o aplicați la mesajele existente sau, alternativ, apăsați lung pe regula din lista de reguli și selectați *Execută acum*.

Va trebui să dați un nume unei reguli și să definiți ordinea în care o regulă trebuie să fie executată în raport cu alte reguli.

Puteți dezactiva o regulă și puteți opri procesarea altor reguli după ce o regulă a fost executată.

Sunt disponibile următoarele condiții de regulă:

* Expeditorul conține sau expeditorul este un contact
* Destinatarul conține
* Subiectul conține
* Are atașamente (opțional de tip specific)
* Antetul conține
* Timp absolut (primit) între (de la versiunea 1.1540)
* Timp relativ (primit) între

Toate condițiile unei reguli trebuie să fie adevărate pentru ca acțiunea regulii să fie executată. Toate condițiile sunt opționale, dar trebuie să existe cel puțin o condiție, pentru a preveni potrivirea tuturor mesajelor. Dacă doriți să vă potriviți cu toți expeditorii sau toți destinatarii, puteți utiliza doar caracterul @ ca și condiție, deoarece toate adresele de e-mail vor conține acest caracter. Dacă doriți să potriviți un nume de domeniu, puteți folosi ca și condiție ceva de genul *@exemplu.org*

Rețineți că adresele de e-mail sunt formatate astfel:

`
"Cineva" <somebody@example.org>`

Puteți utiliza mai multe reguli, eventual cu o *oprire a procesării*, pentru o condiție *sau* sau *nu*.

Potrivirea nu este sensibilă la majuscule și minuscule, cu excepția cazului în care folosiți [expresii regulate](https://en.wikipedia.org/wiki/Regular_expression). Vă rugăm să consultați [aici](https://developer.android.com/reference/java/util/regex/Pattern) pentru documentația expresiilor regulate Java. Puteți testa un regex [ aici](https://regexr.com/).

Rețineți că o expresie regulată acceptă un operator *or*, astfel încât, dacă doriți să potriviți mai mulți expeditori, puteți face acest lucru:

`
.*alice@example\.org.*|.*bob@example\.org.*|.*carol@example\.org.*`

Rețineți că modul [dot all mode](https://developer.android.com/reference/java/util/regex/Pattern#DOTALL) este activat pentru a putea să se potrivească cu [în antetele neîndoite](https://tools.ietf.org/html/rfc2822#section-3.2.3).

Puteți selecta una dintre aceste acțiuni pentru a le aplica mesajelor corespunzătoare:

* Nici o acțiune (util pentru *nu*)
* Marcați ca fiind citit
* Marcați ca necitit
* Ascundeți
* Suprimarea notificării
* Amână
* Adaugă stea
* Setează importanța (prioritate locală)
* Adăugă cuvânt cheie
* Mută
* Copiați (Gmail: eticheta)
* Răspundeți/înaintează (cu șablon)
* Text-to-speech (expeditor și subiect)
* Automatizare (Tasker, etc)

O eroare în condițiile unei reguli poate duce la un dezastru, prin urmare, acțiunile ireversibile nu sunt acceptate.

Regulile se aplică imediat după ce a fost preluat antetul mesajului, dar înainte ca textul mesajului să fie descărcat, astfel încât nu este posibilă aplicarea de condiții la textul mesajului. Rețineți că mesajele text mari sunt descărcate la cerere pe o conexiune contorizată pentru a economisi date.

Dacă doriți să transmiteți un mesaj, luați în considerare utilizarea acțiunii de mutare. Acest lucru va fi mai fiabil decât redirecționarea, deoarece mesajele redirecționate pot fi considerate ca fiind spam.

Deoarece antetele mesajelor nu sunt descărcate și stocate în mod implicit pentru a economisi bateria și datele și pentru a economisi spațiu de stocare. nu este posibilă previzualizarea mesajelor care s-ar potrivi cu o condiție a unei reguli de antet.

Câteva condiții comune pentru antet (regex):

* *.&ast;Auto-Submitted:.&ast;* [RFC3834](https://tools.ietf.org/html/rfc3834)
* *.&ast;Content-Type: multipart/report.&ast;* [RFC3462](https://tools.ietf.org/html/rfc3462)

În meniul de mesaje cu trei puncte *mai mult* există un element care permite crearea unei reguli pentru un mesaj primit, cu cele mai frecvente condiții completate.

Protocolul POP3 nu acceptă setarea cuvintelor cheie și mutarea sau copierea mesajelor.

Utilizarea regulilor este o caracteristică profesională.

<br />

<a name="faq72"></a>
**(72) Ce sunt conturile/identitățile primare?**

Contul principal este utilizat atunci când contul este ambiguu, de exemplu atunci când începeți un nou proiect din căsuța de primire unificată.

În mod similar, identitatea primară a unui cont este utilizată atunci când identitatea este ambiguă.

Nu poate exista decât un singur cont principal și nu poate exista decât o singură identitate principală pentru fiecare cont.

<br />

<a name="faq73"></a>
**(73) Este deplasarea mesajelor între conturi sigură/eficientă?**

Mutarea mesajelor între conturi este sigură, deoarece mesajele originale, brute, vor fi descărcate și mutate. și pentru că mesajele sursă vor fi șterse numai după ce mesajele țintă au fost adăugate

Mutarea pe loturi a mesajelor între conturi este eficientă dacă atât dosarul sursă, cât și dosarul țintă sunt setate pentru sincronizare, în caz contrar, FairEmail trebuie să se conecteze la dosarul (dosarele) pentru fiecare mesaj.

<br />

<a name="faq74"></a>
**(74) De ce văd mesaje duplicate?**

Unii furnizori, în special Gmail, listează toate mesajele din toate dosarele, cu excepția celor aruncate la gunoi, și în dosarul de arhivă (toate mesajele). FairEmail afișează toate aceste mesaje într-un mod care nu este deranjant pentru a indica faptul că aceste mesaje sunt de fapt același mesaj.

Gmail permite ca un mesaj să aibă mai multe etichete, care sunt prezentate în FairEmail sub formă de dosare. Acest lucru înseamnă că mesajele cu mai multe etichete vor fi afișate de mai multe ori.

<br />

<a name="faq75"></a>
**(75) Puteți face o versiune pentru iOS, Windows, Linux, etc?**

Pentru a dezvolta cu succes o aplicație pentru o anumită platformă sunt necesare multe cunoștințe și experiență, motiv pentru care eu dezvolt aplicații doar pentru Android.

<br />

<a name="faq76"></a>
**(76) Ce face 'Clear local messages'?**

Meniul de foldere *Clear local messages* elimină mesajele din dispozitiv care sunt prezente și pe server. Aceasta nu șterge mesajele de pe server. Acest lucru poate fi util după ce ați modificat setările dosarului pentru a nu descărca conținutul mesajului (text și atașamente), de exemplu pentru a economisi spațiu.

<br />

<a name="faq77"></a>
**(77) De ce sunt afișate uneori mesajele cu o mică întârziere?**

În funcție de viteza dispozitivului dvs. (viteza procesorului și poate chiar mai mult viteza memoriei), este posibil ca mesajele să fie afișate cu o mică întârziere. FairEmail este conceput pentru a gestiona în mod dinamic un număr mare de mesaje fără a rămâne fără memorie. Aceasta înseamnă că mesajele trebuie să fie citite dintr-o bază de date și că această bază de date trebuie să fie supravegheată pentru modificări, ceea ce poate cauza mici întârzieri.

Unele funcții de comoditate, cum ar fi gruparea mesajelor pentru a afișa firele de conversație și determinarea mesajului anterior/următor, necesită puțin timp suplimentar. Țineți cont că nu există *următorul mesaj* pentru că, între timp, ar fi putut fi trimis un nou mesaj.

Atunci când se compară viteza FairEmail cu aplicații similare, acest lucru ar trebui să facă parte din comparație. Este ușor să scrieți o aplicație similară, mai rapidă, care să afișeze doar o listă liniară de mesaje, dar care să utilizeze prea multă memorie, dar nu este la fel de ușor să gestionezi în mod corespunzător utilizarea resurselor și să oferi caracteristici mai avansate, cum ar fi conversația în fir.

FairEmail se bazează pe componentele de ultimă generație ale arhitecturii [Android](https://developer.android.com/topic/libraries/architecture/), astfel încât există puțin loc pentru îmbunătățirea performanțelor.

<br />

<a name="faq78"></a>
**(78) Cum se utilizează programele?**

În setările de primire puteți activa programarea și puteți seta o perioadă de timp și zilele săptămânii *când* mesajele ar trebui să fie *recepționate*. Rețineți că o oră de sfârșit egală sau anterioară orei de începere este considerată a fi cu 24 de ore mai târziu.

Automatizarea, a se vedea mai jos, poate fi utilizată pentru programări mai avansate, cum ar fi, de exemplu, mai multe perioade de sincronizare pe zi sau perioade de sincronizare diferite pentru zile diferite.

Este posibil să instalați FairEmail în mai multe profiluri de utilizator, de exemplu un profil personal și unul de lucru, și să configurați FairEmail în mod diferit în fiecare profil, ceea ce reprezintă o altă posibilitate de a avea programe de sincronizare diferite și de a sincroniza un set diferit de conturi.

De asemenea, este posibil să se creeze [reguli de filtrare](#user-content-faq71) cu o condiție de timp și să se suspende mesajele până la ora de sfârșit a condiției de timp. În acest fel, este posibil să *snoozeziem* mesajele legate de afaceri până la începerea orelor de lucru. Acest lucru înseamnă, de asemenea, că mesajele vor fi stocate pe dispozitivul dvs. atunci când nu există (temporar) o conexiune la internet.

Rețineți că versiunile recente de Android permit suprascrierea DND (Do Not Disturb) pentru fiecare canal de notificare și pentru fiecare aplicație, ceea ce ar putea fi utilizat pentru a (nu) reduce la tăcere anumite notificări (de afaceri). Vă rugăm să [vezi aici](https://support.google.com/android/answer/9069335) pentru mai multe informații.

Pentru scheme mai complexe, puteți seta unul sau mai multe conturi pentru sincronizare manuală. și să trimiteți această comandă către FairEmail pentru a verifica dacă există mesaje noi:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL
```

Pentru un anumit cont:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL --es account Gmail
```

De asemenea, puteți automatiza activarea și dezactivarea primirii mesajelor prin trimiterea acestor comenzi către FairEmail:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE
```

Pentru a activa/dezactiva un anumit cont:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE --es account Gmail
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE --es account Gmail
```

Rețineți că dezactivarea unui cont va ascunde contul și toate folderele și mesajele asociate.

Pentru a seta intervalul de interogare:

```
(adb shell) adb shell am start-foreground-service -a eu.faircode.email.INTERVAL --ei minutes nnn
```

Unde *nnn* este unul dintre 0, 15, 30, 60, 120, 240, 480, 1440. O valoare de 0 înseamnă mesaje push.

Puteți trimite automat comenzi cu, de exemplu, [Tasker](https://tasker.joaoapps.com/userguide/en/intents.html):

```
O nouă sarcină: Ceva recognoscibil
Categoria de acțiune: "Acțiune": Diverse/Anunțuri de trimitere
Acțiune: eu.faircode.email.ENABLE
Țintă: Serviciul
```

Pentru a activa/dezactiva un cont cu numele *Gmail*:

```
Extras: cont:Gmail
```

Numele conturilor sunt sensibile la majuscule și minuscule.

Programarea este o caracteristică profesională.

<br />

<a name="faq79"></a>
**(79) Cum se utilizează sincronizarea la cerere (manuală)?**

În mod normal, FairEmail menține o conexiune la serverele de e-mail configurate ori de câte ori este posibil pentru a primi mesajele în timp real. Dacă nu doriți acest lucru, de exemplu pentru a nu fi deranjat sau pentru a economisi bateria, dezactivați recepția în setările de recepție. Acest lucru va opri serviciul din fundal care se ocupă de sincronizarea automată și va elimina notificarea asociată din bara de stare.

De asemenea, puteți activa *Sincronizare manuală* în setările avansate ale contului dacă doriți să sincronizați manual doar anumite conturi.

Puteți utiliza pull-down-to-refresh într-o listă de mesaje sau puteți utiliza meniul de foldere *Sincronizare acum* pentru a sincroniza manual mesajele.

Dacă doriți să sincronizați manual unele sau toate folderele unui cont, dezactivați sincronizarea pentru foldere (dar nu și pentru cont).

Probabil că veți dori să dezactivați și [browse on server](#user-content-faq24).

<br />

<a name="faq80"></a>
**~~~(80) Cum pot remedia eroarea 'Unable to load BODYSTRUCTURE' ?~~**

~~ Mesajul de eroare *Unable to load BODYSTRUCTURE* este cauzat de erori în serverul de e-mail,~~ ~~vezi [ aici](https://javaee.github.io/javamail/FAQ#imapserverbug) pentru mai multe detalii.~~

~~FairEmail încearcă deja să rezolve aceste erori, dar dacă acest lucru nu reușește, va trebui să solicitați asistență din partea furnizorului dvs..~~

<br />

<a name="faq81"></a>
**~~(81) Puteți face fundalul mesajului original întunecat în tema întunecată?~~**

~~ Mesajul original este afișat așa cum l-a trimis expeditorul, inclusiv toate culorile.~~ ~~Modificarea culorii de fundal nu numai că ar face ca vizualizarea originală să nu mai fie originală, dar poate duce și la mesaje ilizibile.~~

<br />

<a name="faq82"></a>
**(82) Ce este o imagine de urmărire?**

Vă rugăm să consultați [aici](https://en.wikipedia.org/wiki/Web_beacon) despre ce este exact o imagine de urmărire. Pe scurt, imaginile de urmărire urmăresc dacă ați deschis un mesaj.

În majoritatea cazurilor, FairEmail va recunoaște automat imaginile de urmărire și le va înlocui cu această pictogramă:

![Imagine externă](https://github.com/M66B/FairEmail/blob/master/images/baseline_my_location_black_48dp.png)

Recunoașterea automată a imaginilor de urmărire poate fi dezactivată în setările de confidențialitate.

<br />

<a name="faq84"></a>
**(84) Pentru ce sunt contactele locale?**

Informațiile de contact locale se bazează pe numele și adresele găsite în mesajele primite și trimise.

Principala utilizare a stocării contactelor locale este de a oferi autocompletare atunci când FairEmail nu a primit permisiunea pentru contacte.

O altă utilizare este aceea de a genera [shortcut-uri](#user-content-faq31) pe versiunile recente de Android pentru a trimite rapid un mesaj către persoanele contactate frecvent. Acesta este și motivul pentru care se înregistrează numărul de contacte și ultima dată când a fost contactat. și de ce puteți face dintr-un contact un favorit sau îl puteți exclude din favorite prin apăsarea lungă a acestuia.

Lista de contacte este sortată în funcție de numărul de contacte și de ultima dată când a fost contactată.

În mod implicit, vor fi înregistrate doar numele și adresele celor cărora le trimiteți mesaje. Puteți modifica acest lucru în setările de trimitere.

<br />

<a name="faq85"></a>
**(85) De ce nu este disponibilă o identitate?**

O identitate este disponibilă pentru a trimite un mesaj nou sau pentru a răspunde sau a redirecționa un mesaj existent numai dacă:

* identitatea este setată pentru sincronizare (trimiterea de mesaje)
* contul asociat este setat să se sincronizeze (să primească mesaje)
* contul asociat are un dosar de ciorne

FairEmail va încerca să selecteze cea mai bună identitate pe baza adresei *to* a mesajului la care s-a răspuns / care este redirecționat.

<br />

<a name="faq86"></a>
**~~~(86) Ce sunt 'caracteristici suplimentare de confidențialitate'?~~**

~~Opțiunea avansată *funcții suplimentare de confidențialitate* permite:~~

* ~~ Căutarea proprietarului adresei IP a unui link ~~
* ~~Detectarea și eliminarea [imaginilor de urmărire](#user-content-faq82)~~

<br />

<a name="faq87"></a>
**(87) Ce înseamnă 'acreditări invalide'?**

Mesajul de eroare *credințe invalide* înseamnă fie că numele de utilizator și/sau parola sunt incorecte, de exemplu, pentru că parola a fost schimbată sau a expirat, fie că autorizația contului a expirat.

Dacă parola este incorectă/expirată, va trebui să o actualizați în setările contului și/sau ale identității.

În cazul în care autorizația contului a expirat, va trebui să selectați din nou contul. Probabil că va trebui, de asemenea, să salvați din nou identitatea asociată.

<br />

<a name="faq88"></a>
**(88) Cum pot folosi un cont Yahoo, AOL sau Sky?**

Modalitatea preferată de a configura un cont Yahoo este utilizarea asistentului de configurare rapidă, care va folosi OAuth în loc de parolă și, prin urmare, este mai sigur (și mai ușor).

Pentru a autoriza un cont Yahoo, AOL sau Sky, va trebui să creați o parolă pentru aplicație. Pentru instrucțiuni, vă rugăm să consultați aici:

* [pentru Yahoo](https://help.yahoo.com/kb/generate-third-party-passwords-sln15241.html)
* [pentru AOL](https://help.aol.com/articles/Create-and-manage-app-password)
* [pentru Sky](https://www.sky.com/help/articles/getting-started-with-sky-yahoo-mail) (sub *Alte aplicații de e-mail*)

Vă rugăm să consultați [acest FAQ](#user-content-faq111) despre suportul OAuth.

Rețineți că Yahoo, AOL și Sky nu acceptă mesaje push standard. Aplicația de e-mail Yahoo utilizează un protocol proprietar, nedocumentat, pentru mesajele push.

Mesajele Push necesită [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE), iar serverul de e-mail Yahoo nu raportează IDLE ca fiind capabil:

```
CAPACITATEA Y1
* CAPABILITY IMAP4rev1 ID MOVE NAMESPACE XYMHIGHESTMODSEQ UIDPLUS LITERAL+ CHILDREN X-MSG-EXT UNSELECT OBJECTID
Y1 OK CAPABILITY finalizat
```

<br />

<a name="faq89"></a>
**(89) Cum pot trimite mesaje doar în text simplu?**

În mod implicit, FairEmail trimite fiecare mesaj atât sub formă de text simplu, cât și sub formă de text formatat HTML, deoarece aproape toți destinatarii se așteaptă la mesaje formatate în zilele noastre. Dacă doriți/necesitați să trimiteți numai mesaje text simplu, puteți activa acest lucru în opțiunile avansate de identitate. Este posibil să doriți să creați o nouă identitate pentru acest lucru dacă doriți/trebuie să selectați trimiterea de mesaje text simplu de la caz la caz.

<br />

<a name="faq90"></a>
**(90) De ce unele texte sunt legate, dar nu sunt o legătură?**

FairEmail va lega automat legăturile web nelegate (http și https) și adresele de e-mail nelegate (mailto) pentru confortul dumneavoastră. Cu toate acestea, textele și legăturile nu sunt ușor de distins, mai ales când multe [domenii de nivel superior](https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains) sunt cuvinte. Acesta este motivul pentru care textele cu puncte sunt uneori recunoscute incorect ca fiind linkuri, ceea ce este mai bine decât să nu recunoaștem unele linkuri.

Vor fi recunoscute și legăturile pentru protocoalele tel, geo, rtsp și xmpp, dar nu vor fi recunoscute linkurile pentru protocoale mai puțin obișnuite sau mai puțin sigure, cum ar fi telnet și ftp. Regexul de a recunoaşte link-urile este deja *foarte* complex şi adăugarea mai multor protocoale va face ca acesta să fie doar mai lent şi posibil să cauzeze erori.

Rețineți că mesajele originale sunt afișate exact așa cum sunt, ceea ce înseamnă, de asemenea, că legăturile nu sunt adăugate automat.

<br />

<a name="faq91"></a>
**~~~(91) Puteți adăuga sincronizarea periodică pentru a economisi energia bateriei?~~**

~~Sincronizarea mesajelor este un proces costisitor deoarece mesajele locale și cele de la distanță trebuie comparate,~~ ~~ deci sincronizarea periodică a mesajelor nu va duce la economisirea energiei bateriei, ci dimpotrivă.~~

~~Vezi [această Întrebări frecvente](#user-content-faq39) despre optimizarea utilizării bateriei~~

<br />

<a name="faq92"></a>
**(92) Puteți adăuga filtrarea spam-ului, verificarea semnăturii DKIM și autorizarea SPF?**

Filtrarea spam-ului, verificarea semnăturii [DKIM](https://en.wikipedia.org/wiki/DomainKeys_Identified_Mail). și autorizarea [SPF](https://en.wikipedia.org/wiki/Sender_Policy_Framework) este o sarcină a serverelor de e-mail, nu a unui client de e-mail. În general, serverele au mai multă memorie și putere de calcul, așa că sunt mult mai potrivite pentru această misiune decât dispozitivele alimentate cu baterii. De asemenea, veți dori ca spamul să fie filtrat pentru toți clienții de e-mail, inclusiv pentru e-mailurile web, nu doar pentru un singur client de e-mail. În plus, serverele de e-mail au acces la informații, cum ar fi adresa IP etc. a serverului de conectare, la care un client de e-mail nu are acces.

Filtrarea spam-ului pe baza antetului mesajelor ar fi putut fi fezabilă, dar, din păcate, această tehnică este [patentată de Microsoft](https://patents.google.com/patent/US7543076).

Versiunile recente ale FairEmail pot filtra spam-ul până la un anumit punct folosind un clasificator de mesaje. Vă rugăm să consultați [acest FAQ](#user-content-faq163) pentru mai multe informații în acest sens.

Bineînțeles că puteți raporta mesajele ca fiind spam cu FairEmail, care va muta mesajele raportate în dosarul de spam și va antrena filtrul de spam al furnizorului, așa cum ar trebui să funcționeze. Acest lucru se poate face automat și cu [regulile de filtrare](#user-content-faq71). Blocarea expeditorului va crea o regulă de filtrare care va muta automat mesajele viitoare ale aceluiași expeditor în dosarul de spam.

Rețineți că protocolul POP3 oferă acces numai la căsuța de primire. Astfel, nu va fi posibilă raportarea spam-ului pentru conturile POP3.

Rețineți că nu trebuie să ștergeți mesajele spam, nici din dosarul de spam, deoarece serverul de e-mail folosește mesajele din dosarul de spam pentru a "învăța" ce sunt mesajele spam.

Dacă primiți o mulțime de mesaje spam în căsuța poștală, cel mai bun lucru pe care îl puteți face este să contactați furnizorul de e-mail pentru a întreba dacă filtrarea spam-ului poate fi îmbunătățită.

De asemenea, FairEmail poate afișa un mic steag roșu de avertizare atunci când autentificarea DKIM, SPF sau [DMARC](https://en.wikipedia.org/wiki/DMARC) a eșuat pe serverul destinatar. Puteți activa/dezactiva [verificarea autentificării](https://en.wikipedia.org/wiki/Email_authentication) în setările de afișare.

FairEmail poate afișa un steguleț de avertizare dacă numele de domeniu al adresei de e-mail (de răspuns) a expeditorului nu definește o înregistrare MX îndreptată către un server de e-mail. Acest lucru poate fi activat în setările de recepție. Rețineți că acest lucru va încetini semnificativ sincronizarea mesajelor.

Dacă numele de domeniu al expeditorului și numele de domeniu al adresei de răspuns diferă, se va afișa și stegulețul de avertizare, deoarece acesta este cel mai adesea cazul mesajelor de phishing. Dacă se dorește, acest lucru poate fi dezactivat în setările de recepție (începând cu versiunea 1.1506).

În cazul în care mesajele legitime nu reușesc să se autentifice, ar trebui să notificați expeditorul, deoarece acest lucru va duce la un risc ridicat ca mesajele să ajungă în dosarul de spam. În plus, în lipsa unei autentificări adecvate, există riscul ca expeditorul să se dea drept persoană. Expeditorul ar putea folosi [acest instrument](https://www.mail-tester.com/) pentru a verifica autentificarea și alte lucruri.

<br />

<a name="faq93"></a>
**(93) Puteți permite instalarea/stocarea datelor pe medii de stocare externe (sdcard)?**

FairEmail folosește servicii și alarme, oferă widget-uri și ascultă pentru ca evenimentul de pornire finalizat să fie pornit de la pornirea dispozitivului, deci nu este posibilă stocarea aplicației pe suportul extern de stocare, cum ar fi un sdcard. A se vedea și [aici](https://developer.android.com/guide/topics/data/install-location).

Mesajele, atașamentele etc. stocate pe un suport de stocare extern, cum ar fi un sdcard, pot fi accesate de alte aplicații și, prin urmare, nu sunt sigure. Consultați [aici](https://developer.android.com/training/data-storage) pentru detalii.

La nevoie, puteți salva mesajele (brute) prin intermediul meniului cu trei puncte de deasupra textului mesajului. și să salvați atașamentele atingând pictograma dischetă.

Dacă aveți nevoie să economisiți spațiu de stocare, puteți limita numărul de zile în care mesajele sunt sincronizate și păstrate. Puteți modifica aceste setări prin apăsarea lungă a unui dosar din lista de dosare și prin selectarea *Edit properties*.

<br />

<a name="faq94"></a>
**(94) Ce înseamnă dunga roșie/portocalie de la sfârșitul antetului?**

Fâșia roșie/portocalie din partea stângă a antetului înseamnă că autentificarea DKIM, SPF sau DMARC a eșuat. A se vedea, de asemenea, [acest FAQ](#user-content-faq92).

<br />

<a name="faq95"></a>
**(95) De ce nu sunt afișate toate aplicațiile atunci când se selectează un atașament sau o imagine?**

Din motive de confidențialitate și securitate, FairEmail nu are permisiuni de acces direct la fișiere, în schimb, pentru selectarea fișierelor se utilizează Storage Access Framework, disponibil și recomandat începând cu Android 4.4 KitKat (lansat în 2013).

Dacă o aplicație este listată depinde de faptul dacă aplicația implementează un [document provider](https://developer.android.com/guide/topics/providers/document-provider). Dacă aplicația nu este listată, este posibil să trebuiască să solicitați dezvoltatorului aplicației să adauge suport pentru Storage Access Framework.

Android Q va face mai dificilă și poate chiar imposibilă accesarea directă a fișierelor, a se vedea [aici](https://developer.android.com/preview/privacy/scoped-storage) și [aici](https://www.xda-developers.com/android-q-storage-access-framework-scoped-storage/) pentru mai multe detalii.

<br />

<a name="faq96"></a>
**(96) Unde pot găsi setările IMAP și SMTP?**

Setările IMAP fac parte din setările contului (personalizat), iar setările SMTP fac parte din setările de identitate.

<br />

<a name="faq97"></a>
**(97) Ce este 'curățarea'?**

Aproximativ la fiecare patru ore FairEmail rulează o sarcină de curățare care:

* Elimină textele de mesaje vechi
* Elimină fișierele atașate vechi
* Elimină fișierele de imagine vechi
* Elimină contactele locale vechi
* Elimină intrările vechi din jurnal

Rețineți că sarcina de curățare se va executa numai atunci când serviciul de sincronizare este activ.

<br />

<a name="faq98"></a>
**(98) De ce pot alege în continuare contacte după ce am revocat permisiunile de contacte?**

După revocarea permisiunilor contactelor, Android nu mai permite accesul FairEmail la contactele tale. Cu toate acestea, selectarea contactelor este delegată și efectuată de Android și nu de FairEmail, astfel încât acest lucru va fi în continuare posibil fără permisiuni pentru contacte.

<br />

<a name="faq99"></a>
**(99) Puteți adăuga un editor de text bogat sau markdown?**

FairEmail oferă formatarea obișnuită a textului (bold, italic, subliniere, dimensiunea și culoarea textului) prin intermediul unei bare de instrumente care apare după selectarea unui text.

Un editor [Rich text](https://en.wikipedia.org/wiki/Formatted_text) sau [Markdown](https://en.wikipedia.org/wiki/Markdown). nu ar fi folosit de mulți oameni pe un dispozitiv mobil de mici dimensiuni și, mai important, Android nu suportă un editor de text bogat, iar majoritatea proiectelor open source de editor de text bogat sunt abandonate. Consultați [aici](https://forum.xda-developers.com/showpost.php?p=79061829&postcount=4919) pentru mai multe detalii despre acest lucru.

<br />

<a name="faq100"></a>
**(100) Cum pot sincroniza categoriile Gmail?**

Puteți sincroniza categoriile Gmail prin crearea de filtre pentru a eticheta mesajele clasificate:

* Creați un filtru nou prin Gmail > Setări (roată) > Filtre și adrese blocate > Creați un filtru nou
* Introduceți o căutare pe categorii (a se vedea mai jos) în câmpul *Cuvintele* și faceți clic pe *Creare filtru*
* Bifați *Aplicați eticheta* și selectați o etichetă și faceți clic pe *Creare filtru*

Categorii posibile:

```
category:social
categoria:actualizări
category:forumuri
categoria:promoții
```

Din păcate, acest lucru nu este posibil pentru dosarul de mesaje amânate.

Puteți folosi *Sincronizare forțată* în meniul cu trei puncte din căsuța de primire unificată pentru a permite FairEmail să sincronizeze din nou lista de dosare și puteți apăsa lung pe dosare pentru a activa sincronizarea.

<br />

<a name="faq101"></a>
**(101) Ce înseamnă punctul albastru/portocaliu din partea de jos a conversațiilor?**

Punctul indică poziția relativă a conversației în lista de mesaje. Punctul va fi portocaliu atunci când conversația este prima sau ultima din lista de mesaje, altfel va fi albastru. Punctul este menit să fie un ajutor atunci când glisați spre stânga/dreapta pentru a trece la conversația anterioară/următoare.

Punctul este dezactivat în mod implicit și poate fi activat cu setările de afișare *Afișează poziția relativă a conversației cu un punct*.

<br />

<a name="faq102"></a>
**(102) Cum pot activa rotirea automată a imaginilor?**

Imaginile vor fi rotite automat atunci când redimensionarea automată a imaginilor este activată în setări (activată în mod implicit). Cu toate acestea, rotirea automată depinde de prezența și corectitudinea informațiilor [Exif](https://en.wikipedia.org/wiki/Exif), ceea ce nu este întotdeauna cazul. Mai ales nu atunci când faceți o fotografie cu o aplicație camara de la FairEmail.

Rețineți că numai imaginile [JPEG](https://en.wikipedia.org/wiki/JPEG) și [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) pot conține informații Exif.

<br />

<a name="faq104"></a>
**(104) Ce trebuie să știu despre raportarea erorilor?**

* Rapoartele de eroare vor ajuta la îmbunătățirea FairEmail
* Raportarea erorilor este opțională și opt-in
* Raportarea erorilor poate fi activată/dezactivată în setări, secțiunea Diverse
* Rapoartele de eroare vor fi trimise automat în mod anonim către [Bugsnag](https://www.bugsnag.com/)
* Bugsnag pentru Android este [open source](https://github.com/bugsnag/bugsnag-android)
* Vezi [ aici](https://docs.bugsnag.com/platforms/android/automatically-captured-data/) despre ce date vor fi trimise în caz de erori
* Consultați [aici](https://docs.bugsnag.com/legal/privacy-policy/) pentru politica de confidențialitate a Bugsnag
* Rapoartele de eroare vor fi trimise la *sessions.bugsnag.com:443* și *notify.bugsnag.com:443*

<br />

<a name="faq105"></a>
**(105) Cum funcționează opțiunea roam-like-at-home?**

FairEmail va verifica dacă codul de țară al cartelei SIM și codul de țară al rețelei se află în țările [EU roam-like-at-home](https://en.wikipedia.org/wiki/European_Union_roaming_regulations#Territorial_extent). și presupune că nu există roaming dacă codurile de țară sunt egale și dacă opțiunea avansată roam-like-at-home este activată.

Prin urmare, nu trebuie să dezactivați această opțiune dacă nu aveți un SIM UE sau dacă nu sunteți conectat la o rețea UE.

<br />

<a name="faq106"></a>
**(106) Ce lansatoare pot afișa un număr de insigne cu numărul de mesaje necitite?**

Vă rugăm să [vezi aici](https://github.com/leolin310148/ShortcutBadger#supported-launchers) pentru o listă de lansatoare care pot afișa numărul de mesaje necitite.

Rețineți că Nova Launcher necesită Tesla Unread, care nu mai este [nu mai este acceptat](https://forum.xda-developers.com/android/general/bad-news-tesla-unread-devoloper-t3920415).

Rețineți că setarea de notificare *Show launcher icon with number of new messages* trebuie să fie activată (implicit activată).

Se vor număra numai mesajele necitite *noi* din dosarele setate să afișeze notificări de mesaje noi, astfel încât mesajele marcate din nou ca necitite și mesajele din dosarele setate să nu afișeze notificări de mesaje noi nu vor fi luate în considerare.

În funcție de ceea ce doriți, setările de notificare *Lasă numărul de mesaje noi să corespundă cu numărul de notificări* trebuie să fie activate (implicit dezactivate). Atunci când este activată, numărul de insigne va fi același cu numărul de notificări de mesaje noi. Atunci când este dezactivată, numărul de insigne va fi numărul de mesaje necitite, indiferent dacă acestea sunt afișate într-o notificare sau sunt noi.

Această funcție depinde de suportul pe care îl oferă lansatorul dumneavoastră. FairEmail pur și simplu 'difuzează' numărul de mesaje necitite folosind biblioteca ShortcutBadger. Dacă nu funcționează, acest lucru nu poate fi rezolvat prin modificări în FairEmail.

Unele lansatoare afișează un punct sau un '1' pentru [notificarea de monitorizare](#user-content-faq2), în ciuda faptului că FairEmail a cerut în mod explicit să nu afișeze un *badge* pentru această notificare. Acest lucru ar putea fi cauzat de o eroare în aplicația de lansare sau în versiunea Android. Vă rugăm să verificați de două ori dacă punctul de notificare (insigna) este dezactivat pentru canalul de notificare a primirii (serviciului). Puteți merge la setările canalului de notificare din dreapta prin intermediul setărilor de notificare din FairEmail. Poate că acest lucru nu este evident, dar puteți apăsa pe numele canalului pentru mai multe setări.

FairEmail trimite o nouă intenție de numărare a mesajelor, de asemenea:

```
eu.faircode.email.NEW_MESSAGE_COUNT
```

Numărul de mesaje noi, necitite, se va afla într-un parametru întreg "*count*".

<br />

<a name="faq107"></a>
**(107) Cum pot folosi stelele colorate?**

Puteți seta o stea colorată prin intermediul meniului de mesaje *more*, prin selecție multiplă (pornită prin apăsarea lungă a unui mesaj), prin apăsarea lungă a unei stele într-o conversație sau automat prin utilizarea [regulilor](#user-content-faq71).

Trebuie să știți că stelele colorate nu sunt acceptate de protocolul IMAP și, prin urmare, nu pot fi sincronizate cu un server de e-mail. Acest lucru înseamnă că stelele colorate nu vor fi vizibile în alți clienți de e-mail și vor fi pierdute la descărcarea mesajelor din nou. Cu toate acestea, stelele (fără culoare) vor fi sincronizate și vor fi vizibile în alți clienți de e-mail, atunci când sunt acceptate.

Unii clienți de e-mail utilizează cuvinte cheie IMAP pentru culori. Cu toate acestea, nu toate serverele acceptă cuvinte cheie IMAP și, în plus, nu există cuvinte cheie standard pentru culori.

<br />

<a name="faq108"></a>
**~~~(108) Puteți adăuga mesaje șterse permanent din orice dosar?~~**

~~Când ștergeți mesajele dintr-un dosar, mesajele vor fi mutate în dosarul de gunoi, astfel încât să aveți o șansă de a le restaura.~~ ~~Puteți șterge permanent mesajele din dosarul coș de gunoi.~~ ~~ Ștergerea permanentă a mesajelor din alte dosare ar anula scopul dosarului coș de gunoi, așa că acesta nu va fi adăugat.~~

<br />

<a name="faq109"></a>
**~~~(109) De ce "select account" este disponibil doar în versiunile oficiale?~~**

~~Utilizarea *select account* pentru a selecta și autoriza conturile Google necesită permisiune specială din partea Google din motive de securitate și confidențialitate.~~ ~~Această permisiune specială poate fi obținută numai pentru aplicațiile pe care un dezvoltator le gestionează și de care este responsabil.~~ ~~Aplicațiile de la terți, cum ar fi cele de la F-Droid, sunt gestionate de terți și sunt responsabilitatea acestora.~~ ~~Prin urmare, numai aceste terțe părți pot obține permisiunea necesară de la Google.~~ ~~Din moment ce aceste terțe părți nu suportă FairEmail, cel mai probabil nu vor solicita permisiunea necesară.~~

~~Puteți rezolva acest lucru în două moduri:~~

* ~~ Treceți la versiunea oficială a FairEmail, vedeți [ aici](https://github.com/M66B/FairEmail/blob/master/README.md#downloads) pentru opțiuni~~
* ~~Utilizați parole specifice aplicației, consultați [acest FAQ](#user-content-faq6)~~

~~Utilizarea *selectare cont* în compilările terților nu mai este posibilă în versiunile recente.~~ ~~În versiunile mai vechi acest lucru era posibil, dar acum va rezulta în eroarea *UNREGISTERED_ON_API_CONSOLE*.~~

<br />

<a name="faq110"></a>
**(110) De ce sunt (unele) mesaje goale și/sau atașamente corupte?**

Mesajele goale și/sau atașamentele corupte sunt probabil cauzate de o eroare în software-ul serverului. Software-ul Microsoft Exchange mai vechi este cunoscut ca fiind cauza acestei probleme. În cea mai mare parte, puteți rezolva această problemă dezactivând *Parțial fetch* în setările avansate ale contului:

Setări > Configurare manuală > Conturi > atingeți cont > atingeți avansat > Preluare parțială > debifați

După dezactivarea acestei setări, puteți utiliza meniul "mai multe" (trei puncte) pentru a 'resincroniza' mesajele goale. Alternativ, puteți *Șterge mesajele locale* prin apăsarea lungă a dosarului (dosarelor) din lista de dosare și sincroniza din nou toate mesajele.

Dezactivarea *Parțial fetch* va duce la o utilizare mai mare a memoriei.

<br />

<a name="faq111"></a>
**(111) Este OAuth acceptat?**

OAuth pentru Gmail este acceptat prin intermediul expertului de configurare rapidă. Managerul de conturi Android va fi utilizat pentru a prelua și reîmprospăta token-urile OAuth pentru conturile selectate pe dispozitiv. OAuth pentru conturile care nu sunt pe dispozitiv nu este acceptat deoarece Google solicită un [audit de securitate anual](https://support.google.com/cloud/answer/9110914) (între 15.000 și 75.000 de dolari) pentru acest lucru. Puteți citi mai multe despre acest lucru [ aici](https://www.theregister.com/2019/02/11/google_gmail_developer/).

OAuth pentru Outlook/Office 365, Yahoo, Mail.ru și Yandex este acceptat prin intermediul expertului de configurare rapidă.

<br />

<a name="faq112"></a>
**(112) Ce furnizor de e-mail recomandați?**

FairEmail este doar un client de e-mail, așa că trebuie să vă aduceți propria adresă de e-mail. Rețineți că acest lucru este menționat în mod clar în descrierea aplicației.

Există o mulțime de furnizori de e-mail din care puteți alege. Care furnizor de e-mail este cel mai bun pentru dumneavoastră depinde de dorințele/cerințele dumneavoastră. Vă rugăm să consultați site-urile web ale [Restabilirea confidențialității](https://restoreprivacy.com/secure-email/) sau [Instrumente de confidențialitate](https://www.privacytools.io/providers/email/). pentru o listă de furnizori de e-mail orientați spre confidențialitate, cu avantaje și dezavantaje.

Unii furnizori, cum ar fi ProtonMail, Tutanota, utilizează protocoale de e-mail proprietare, ceea ce face imposibilă utilizarea aplicațiilor de e-mail ale unor terțe părți. Vă rugăm să consultați [acest FAQ](#user-content-faq129) pentru mai multe informații.

Folosind propriul nume de domeniu (personalizat), care este acceptat de majoritatea furnizorilor de e-mail, va fi mai ușor să treceți la un alt furnizor de e-mail.

<br />

<a name="faq113"></a>
**(113) Cum funcționează autentificarea biometrică?**

Dacă dispozitivul dvs. are un senzor biometric, de exemplu un senzor de amprentă digitală, puteți activa/dezactiva autentificarea biometrică în meniul de navigare (hamburger) din ecranul de setări. Atunci când este activat, FairEmail va solicita autentificarea biometrică după o perioadă de inactivitate sau după ce ecranul a fost oprit în timp ce FairEmail era în funcțiune. Activitatea este navigarea în FairEmail, de exemplu, deschiderea unui fir de conversație. Durata perioadei de inactivitate poate fi configurată în setările diverse. Atunci când autentificarea biometrică este activată, notificările de mesaje noi nu vor afișa niciun conținut, iar FairEmail nu va fi vizibil pe ecranul de recenzii de pe Android.

Autentificarea biometrică are rolul de a împiedica alte persoane să vă vadă doar mesajele. FairEmail se bazează pe criptarea dispozitivelor pentru criptarea datelor, vezi și [acest FAQ](#user-content-faq37).

Autentificarea biometrică este o caracteristică pro.

<br />

<a name="faq114"></a>
**(114) Puteți adăuga un import pentru setările altor aplicații de e-mail?**

Formatul fișierelor de setări ale majorității celorlalte aplicații de e-mail nu este documentat, așa că acest lucru este dificil. Uneori este posibil să se facă inginerie inversă a formatului, dar de îndată ce se schimbă formatul de setări, lucrurile se vor strica. De asemenea, setările sunt adesea incompatibile. De exemplu, FairEmail are, spre deosebire de majoritatea celorlalte aplicații de e-mail, setări pentru numărul de zile de sincronizare a mesajelor. și pentru numărul de zile pentru păstrarea mesajelor, în principal pentru a economisi bateria. În plus, configurarea unui cont/identitate cu ajutorul asistentului de configurare rapidă este simplă, așa că nu merită cu adevărat efortul.

<br />

<a name="faq115"></a>
**(115) Puteți adăuga jetoane de adrese de e-mail?**

Adresa de e-mail [chips](https://material.io/design/components/chips.html) arată bine, dar nu poate fi editată, ceea ce este destul de incomod atunci când ați făcut o greșeală de tipar într-o adresă de e-mail.

Rețineți că FairEmail va selecta adresa numai atunci când apăsați lung o adresă, ceea ce facilitează ștergerea unei adrese.

Cipurile nu sunt potrivite pentru a fi afișate într-o listă și, deoarece antetul mesajului dintr-o listă trebuie să arate similar cu antetul mesajului din vizualizarea mesajelor, nu este o opțiune de a utiliza cipuri pentru vizualizarea mesajelor.

S-a revenit la [commit](https://github.com/M66B/FairEmail/commit/2c80c25b8aa75af2287f471b882ec87d5a5a5015).

<br />

<a name="faq116"></a>
**~~~(116) Cum pot afișa implicit imagini în mesajele de la expeditori de încredere?~~**

~~Puteți afișa implicit imagini în mesajele de la expeditori de încredere prin activarea setării de afișare *Afișare automată a imaginilor pentru contactele cunoscute*.~~

~~Contactele din lista de contacte Android sunt considerate a fi cunoscute și de încredere,~~ ~~, cu excepția cazului în care contactul se află în grup / are eticheta "*Untrusted*" (insensibil la majuscule și minuscule).~~

<br />

<a name="faq38"></a>
<a name="faq117"></a>
**(117) Mă puteți ajuta să îmi restabilesc achiziția?**

În primul rând, o achiziție va fi disponibilă pe toate dispozitivele conectate la același cont Google, *dacă* și aplicația este instalată prin intermediul aceluiași cont Google. Puteți selecta contul în aplicația Play Store.

Google gestionează toate achizițiile, așa că, în calitate de dezvoltator, am puțin control asupra achizițiilor. Deci, practic, singurul lucru pe care îl pot face este să vă dau câteva sfaturi:

* Asigurați-vă că aveți o conexiune la internet activă și funcțională
* Asigurați-vă că v-ați conectat cu contul Google corect și că nu există nicio problemă cu contul Google
* Asigurați-vă că ați instalat FairEmail prin intermediul contului Google corect dacă ați configurat mai multe conturi Google pe dispozitivul dvs
* Asigurați-vă că aplicația Play store este actualizată, vă rugăm [vezi aici](https://support.google.com/googleplay/answer/1050566?hl=en)
* Deschideți aplicația Magazin Play și așteptați cel puțin un minut pentru a-i da timp să se sincronizeze cu serverele Google
* Deschide FairEmail și navighează în ecranul de caracteristici pro pentru a permite FairEmail să verifice achizițiile; uneori ajută să atingi butonul *buy*

De asemenea, puteți încerca să ștergeți memoria cache a aplicației Magazin Play prin intermediul setărilor aplicațiilor Android. Ar putea fi necesară repornirea dispozitivului pentru a permite magazinului Play să recunoască achiziția în mod corect.

Rețineți că:

* Dacă primiți *ITEM_ALREADY_OWNED*, probabil că aplicația magazinului Play trebuie să fie actualizată, vă rugăm să [vezi aici](https://support.google.com/googleplay/answer/1050566?hl=en)
* Achizițiile sunt stocate în cloud-ul Google și nu se pot pierde
* Nu există o limită de timp pentru achiziții, deci acestea nu pot expira
* Google nu expune dezvoltatorilor detalii (nume, e-mail, etc.) despre cumpărători
* O aplicație precum FairEmail nu poate selecta ce cont Google să folosească
* Este posibil să dureze ceva timp până când aplicația Magazin Play sincronizează o achiziție pe un alt dispozitiv
* Achizițiile din Play Store nu pot fi utilizate fără Play Store, ceea ce nu este permis nici de regulile Play Store

Dacă nu puteți rezolva problema cu achiziția, va trebui să contactați Google în acest sens.

<br />

<a name="faq118"></a>
**(118) Ce înseamnă mai exact 'Remove tracking parameters'?**

Dacă se bifează *Remove tracking parameters* se vor elimina toți parametrii [UTM](https://en.wikipedia.org/wiki/UTM_parameters) dintr-o legătură.

<br />

<a name="faq119"></a>
**~~~(119) Puteți adăuga culori la widgetul unificat pentru căsuța de primire?~~**

~~Widgetul este proiectat pentru a arăta bine pe majoritatea ecranelor de pornire/pornire, făcându-l monocrom și folosind un fundal pe jumătate transparent.~~ ~~În acest fel, widget-ul se va amesteca frumos, fiind în același timp lizibil.~~

~~ Adăugarea de culori va cauza probleme cu unele fundaluri și va cauza probleme de lizibilitate, motiv pentru care aceasta nu va fi adăugată.~~

Din cauza limitărilor Android, nu este posibil să setați în mod dinamic opacitatea fundalului și să aveți colțuri rotunjite în același timp.

<br />

<a name="faq120"></a>
**(120) De ce nu sunt eliminate notificările de mesaje noi la deschiderea aplicației?**

Notificările de mesaje noi vor fi eliminate la îndepărtarea notificărilor prin glisare sau la marcarea mesajelor asociate ca fiind citite. Deschiderea aplicației nu va elimina notificările de mesaje noi. Acest lucru vă oferă posibilitatea de a lăsa notificările de mesaje noi pentru a vă reaminti că există încă mesaje necitite.

Pe Android 7 Nougat și ulterior, notificările de mesaje noi vor fi [grupate](https://developer.android.com/training/notify-user/group). Dacă apăsați pe notificarea sumară, se va deschide căsuța de primire unificată. Notificarea sumară poate fi extinsă pentru a vizualiza notificările individuale de mesaje noi. Atingerea unei notificări individuale de mesaj nou va deschide conversația din care face parte mesajul respectiv. Consultați [acest FAQ](#user-content-faq70) despre momentul în care mesajele dintr-o conversație vor fi extinse automat și marcate ca fiind citite.

<br />

<a name="faq121"></a>
**(121) Cum sunt grupate mesajele într-o conversație?**

În mod implicit, FairEmail grupează mesajele în conversații. Acest lucru poate fi dezactivat în setările de afișare.

FairEmail grupează mesajele pe baza antetelor standard *Message-ID*, *In-Reply-To* și *References*. FairEmail nu grupează pe alte criterii, cum ar fi subiectul, deoarece acest lucru ar putea duce la gruparea unor mesaje care nu au legătură între ele și ar fi în detrimentul unei utilizări mai mari a bateriei.

<br />

<a name="faq122"></a>
**~~~(122) De ce numele destinatarului/adresa de e-mail este afișată cu o culoare de avertizare?~~**

~~Numele destinatarului și/sau adresa de e-mail din secțiunea adrese vor fi afișate într-o culoare de avertizare~~. ~~când numele de domeniu al expeditorului și numele de domeniu al adresei *la* nu se potrivesc.~~ ~~În general, acest lucru indică faptul că mesajul a fost primit *prin* un cont cu o altă adresă de e-mail.~~

<br />

<a name="faq123"></a>
**(123) Ce se va întâmpla când FairEmail nu se poate conecta la un server de email?**

Dacă FairEmail nu se poate conecta la un server de e-mail pentru a sincroniza mesajele, de exemplu, dacă conexiunea la internet este proastă sau dacă un firewall sau un VPN blochează conexiunea, FairEmail va încerca din nou o singură dată după ce așteaptă 8 secunde, menținând dispozitivul treaz (=utilizează energia bateriei). În cazul în care acest lucru nu reușește, FairEmail va programa o alarmă pentru a încerca din nou după 15, 30 și, eventual, la fiecare 60 de minute și va lăsa dispozitivul să doarmă (= fără utilizarea bateriei).

Rețineți că [Android doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby) nu permite trezirea dispozitivului mai devreme de 15 minute.

*Force sync* din meniul cu trei puncte al căsuței de primire unificate poate fi folosit pentru a permite FairEmail să încerce să se reconecteze fără să aștepte.

Trimiterea mesajelor va fi reluată numai în cazul în care se schimbă conectivitatea (reconectarea la aceeași rețea sau conectarea la o altă rețea) pentru a împiedica serverul de e-mail să blocheze permanent conexiunea. Puteți coborî căsuța de ieșire pentru a încerca din nou manual.

Rețineți că trimiterea nu va fi reluată în caz de probleme de autentificare și în cazul în care serverul a respins mesajul. În acest caz, puteți coborî căsuța de ieșire pentru a încerca din nou.

<br />

<a name="faq124"></a>
**(124) De ce primesc 'Message too large or too complex to display'?**

Mesajul *Mesaj prea mare sau prea complex pentru a fi afișat* va fi afișat dacă există mai mult de 100.000 de caractere sau mai mult de 500 de legături într-un mesaj. Reformatarea și afișarea unor astfel de mesaje va dura prea mult timp. În schimb, puteți încerca să utilizați vizualizarea originală a mesajelor, alimentată de browser.

<br />

<a name="faq125"></a>
**(125) Care sunt caracteristicile experimentale actuale?**

*Message classification (version 1.1438+)*

Vă rugăm să consultați [acest FAQ](#user-content-faq163) pentru detalii.

Since this is an experimental feature, my advice is to start with just one folder.

<br />

*Trimiteți hard bounce (versiunea 1.1477+)*

Trimiteți o notificare [Delivery Status Notification](https://tools.ietf.org/html/rfc3464) (=hard bounce) prin intermediul meniului reply/answer.

Restituirile de tip "hard bounces" vor fi de cele mai multe ori procesate automat, deoarece acestea afectează reputația furnizorului de e-mail. Adresa de respingere (=*Return-Path* antet) este de cele mai multe ori foarte specifică, astfel încât serverul de e-mail poate determina contul expeditor.

Pentru câteva informații de fond, a se vedea [acest articol din Wikipedia](https://en.wikipedia.org/wiki/Bounce_message).

<br />

<a name="faq126"></a>
**(126) Pot fi trimise previzualizări ale mesajelor către dispozitivul meu portabil?**

FairEmail preia un mesaj în doi pași:

1. Preluarea antetelor de mesaj
1. Preluarea textului mesajului și a atașamentelor

Imediat după primul pas vor fi notificate noi mesaje. Cu toate acestea, textul mesajului va fi disponibil doar până după cea de-a doua etapă. FairEmail actualizează notificările de ieșire cu o previzualizare a textului mesajului, dar, din păcate, notificările purtabile nu pot fi actualizate.

Deoarece nu există nicio garanție că textul unui mesaj va fi întotdeauna preluat direct după antetul mesajului, nu este posibil să se garanteze că o notificare de mesaj nou cu un text de previzualizare va fi întotdeauna trimisă către un dispozitiv portabil.

Dacă vi se pare suficient de bine, puteți activa opțiunea de notificare *Să trimiteți notificări cu o previzualizare a mesajului numai la dispozitivele portabile*. iar dacă acest lucru nu funcționează, puteți încerca să activați opțiunea de notificare *Show notifications with a preview text only*. Rețineți că acest lucru se aplică și în cazul dispozitivelor purtabile care nu afișează un text de previzualizare, chiar și atunci când aplicația Android Wear indică faptul că notificarea a fost trimisă (bridged).

Dacă doriți ca textul integral al mesajului să fie trimis pe dispozitivul portabil, puteți activa opțiunea de notificare *Preview all text*. Rețineți că se știe că unele dispozitive portabile se blochează cu această opțiune activată.

Dacă folosești un dispozitiv portabil Samsung cu aplicația Galaxy Wearable (Samsung Gear), s-ar putea să fie nevoie să activezi notificările pentru FairEmail atunci când setarea *Notificații*, *Aplicații instalate în viitor* este dezactivată în această aplicație.

<br />

<a name="faq127"></a>
**(127) Cum pot repara 'Syntactically invalid HELO argument(s)'?**

Eroarea *... Argumentul (argumentele) HELO invalid din punct de vedere sintactic ...* înseamnă că serverul SMTP a respins adresa IP locală sau numele de gazdă. Probabil că puteți remedia această eroare prin activarea sau dezactivarea opțiunii de indentitate avansată *Utilizați adresa IP locală în loc de numele de gazdă*.

<br />

<a name="faq128"></a>
**(128) Cum pot reseta întrebările adresate, de exemplu pentru a afișa imagini?**

Puteți reseta întrebările adresate prin intermediul meniului cu trei puncte din setările diverse.

<br />

<a name="faq129"></a>
**(129) Sunt acceptate ProtonMail, Tutanota? **

ProtonMail folosește un protocol de e-mail proprietar și [nu acceptă direct IMAP](https://protonmail.com/support/knowledge-base/imap-smtp-and-pop3-setup/), astfel încât nu puteți utiliza FairEmail pentru a accesa ProtonMail.

Tutanota folosește un protocol de e-mail proprietar și [nu acceptă IMAP](https://tutanota.com/faq/#imap), deci nu puteți utiliza FairEmail pentru a accesa Tutanota.

<br />

<a name="faq130"></a>
**(130) Ce înseamnă mesajul de eroare ...?**

O serie de linii cu texte portocalii sau roșii cu informații tehnice înseamnă că modul de depanare a fost activat în setările diverse.

Avertismentul *Niciun server găsit la ...* înseamnă că nu a existat un server de e-mail înregistrat la numele de domeniu indicat. Este posibil ca răspunsul la mesaj să nu fie posibil și să genereze o eroare. Acest lucru ar putea indica o adresă de e-mail falsificată și/sau spam.

Eroarea *... ParseException ...* înseamnă că există o problemă cu un mesaj primit, cauzată probabil de o eroare în software-ul de trimitere. FairEmail va rezolva această problemă în cele mai multe cazuri, astfel încât acest mesaj poate fi considerat ca un avertisment în loc de o eroare.

Eroarea *...SendFailedException...* înseamnă că a existat o problemă la trimiterea unui mesaj. Eroarea va include aproape întotdeauna un motiv. Motivele frecvente sunt că mesajul era prea mare sau că una sau mai multe adrese de destinatar nu erau valide.

Avertismentul *Mesaj prea mare pentru a încăpea în memoria disponibilă* înseamnă că mesajul a fost mai mare de 10 MiB. Chiar dacă dispozitivul dvs. are suficient spațiu de stocare, Android oferă aplicațiilor o memorie de lucru limitată, ceea ce limitează dimensiunea mesajelor care pot fi gestionate.

Vă rugăm să consultați [aici](#user-content-faq22) pentru alte mesaje de eroare din outbox.

<br />

<a name="faq131"></a>
**(131) Puteți schimba direcția de glisare către mesajul anterior/succesiv?**

Dacă citiți de la stânga la dreapta, dacă glisați spre stânga se va afișa următorul mesaj. În mod similar, dacă citiți de la dreapta la stânga, glisarea spre dreapta va afișa următorul mesaj.

Acest comportament mi se pare destul de natural, inclusiv pentru că este similar cu întoarcerea paginilor.

Oricum, există o setare de comportament pentru a inversa direcția de glisare.

<br />

<a name="faq132"></a>
**(132) De ce sunt silențioase notificările de mesaje noi?**

Notificările sunt silențioase în mod implicit în unele versiuni MIUI. Vă rugăm să vedeți [aici](http://en.miui.com/thread-3930694-1-1.html) cum puteți remedia acest lucru.

Există o eroare în unele versiuni Android care face ca [setOnlyAlertOnce](https://developer.android.com/reference/android/app/Notification.Builder#setOnlyAlertOnce(boolean)) să dezactiveze notificările. Deoarece FairEmail afișează notificările de mesaje noi imediat după ce a preluat antetul mesajului iar FairEmail trebuie să actualizeze notificările de mesaje noi după ce a preluat ulterior textul mesajului, această problemă nu poate fi rezolvată sau rezolvată de FairEmail.

Este posibil ca Android să limiteze rata sunetului de notificare, ceea ce poate face ca unele notificări de mesaje noi să fie silențioase.

<br />

<a name="faq133"></a>
**(133) De ce nu este acceptat ActiveSync?**

Protocolul Microsoft Exchange ActiveSync [este patentat](https://en.wikipedia.org/wiki/Exchange_ActiveSync#Licensing) și, prin urmare, nu poate fi acceptat. Din acest motiv, nu veți găsi mulți sau chiar niciun alt client de e-mail care acceptă ActiveSync.

Rețineți că descrierea lui FairEmail începe cu observația că protocoalele non-standard, cum ar fi Microsoft Exchange Web Services și Microsoft ActiveSync, nu sunt acceptate.

<br />

<a name="faq134"></a>
**(134) Puteți adăuga ștergerea mesajelor locale?**

*POP3*

În setările contului (Setări, apăsați Configurare manuală, apăsați Conturi, apăsați Cont) puteți activa *Lasă mesajele șterse pe server*.

*IMAP*

Deoarece protocolul IMAP este conceput pentru a sincroniza în două moduri, ștergerea unui mesaj de pe dispozitiv ar duce la recuperarea mesajului din nou la o nouă sincronizare.

Cu toate acestea, FairEmail acceptă ascunderea mesajelor, fie prin intermediul meniului cu trei puncte din bara de acțiune, chiar deasupra textului mesajului fie prin selectarea multiplă a mesajelor din lista de mesaje. Practic, este același lucru cu " lăsați pe server " din protocolul POP3. cu avantajul că puteți afișa din nou mesajele atunci când este necesar.

Rețineți că este posibil să setați acțiunea de glisare spre stânga sau spre dreapta pentru a ascunde un mesaj.

<br />

<a name="faq135"></a>
**(135) De ce sunt afișate în conversații mesajele și ciornele aruncate la gunoi?**

Mesajele individuale vor fi rareori aruncate la gunoi și, de cele mai multe ori, acest lucru se întâmplă din greșeală. Afișarea mesajelor aruncate la gunoi în conversații facilitează regăsirea lor.

Puteți șterge definitiv un mesaj utilizând meniul cu trei puncte *delete* din mesaj, care va elimina mesajul din conversație. Rețineți că acest lucru este ireversibil.

În mod similar, ciornele sunt afișate în conversații pentru a le regăsi în contextul în care își au locul. Este ușor să citiți mesajele primite înainte de a continua să scrieți proiectul mai târziu.

<br />

<a name="faq136"></a>
**(136) Cum pot șterge un cont/identitate/folder?**

Ștergerea unui cont/identitate/folder este un pic ascunsă pentru a preveni accidentele.

* Cont: Setări > Configurare manuală > Conturi > apăsați cont
* Identitate: Setări > Configurare manuală > Identități > atingeți identitatea
* Folder: Apăsați lung folderul din lista de foldere > Editare proprietăți

În meniul cu trei puncte din dreapta sus există o opțiune pentru a șterge contul/identitatea/folderul.

<br />

<a name="faq137"></a>
**(137) Cum pot reseta 'Nu întreba din nou'?**

Puteți reseta toate întrebările setate pentru a nu mai fi adresate din nou în setările diverse.

<br />

<a name="faq138"></a>
**(138) Puteți adăuga gestionarea calendarului/contactului/ sarcinilor/notelor?**

Gestionarea calendarului, a contactelor, a sarcinilor și a notelor poate fi mai bine realizată de o aplicație separată, specializată. Rețineți că FairEmail este o aplicație de e-mail specializată, nu o suită office.

De asemenea, prefer să fac câteva lucruri foarte bine, în loc să fac multe lucruri doar pe jumătate. În plus, din punct de vedere al securității, nu este o idee bună să acordați mai multe permisiuni unei singure aplicații.

Vă sfătuim să folosiți excelenta aplicație open source [DAVx⁵](https://f-droid.org/packages/at.bitfire.davdroid/) pentru a vă sincroniza/gestiona calendarele/contactele.

Majoritatea furnizorilor acceptă exportul contactelor. Vă rugăm să [vedeți aici](https://support.google.com/contacts/answer/1069522) despre cum puteți importa contacte dacă sincronizarea nu este posibilă.

Rețineți că FairEmail acceptă să răspundeți la invitațiile din calendar (o caracteristică pro) și să adăugați invitații în calendarul personal.

<br />

<a name="faq83"></a>
<a name="faq139"></a>
**(139) Cum se rezolvă problema 'Utilizatorul este autentificat, dar nu este conectat'?**

De fapt, această eroare specifică Microsoft Exchange este un mesaj de eroare incorect cauzat de o eroare în software-ul mai vechi al serverului Exchange.

Eroarea *Utilizatorul este autentificat, dar nu este conectat* poate apărea dacă:

* Mesajele push sunt activate pentru prea multe dosare: consultați [acest FAQ](#user-content-faq23) pentru mai multe informații și o soluție de rezolvare
* Parola contului a fost schimbată: schimbarea ei și în FairEmail ar trebui să rezolve problema
* O adresă de e-mail alias este utilizată ca nume de utilizator în loc de adresa de e-mail principală
* Se utilizează o schemă de conectare incorectă pentru o căsuță poștală partajată: schema corectă este *username@domain\SharedMailboxAlias*

Aliasul căsuței poștale partajate va fi în general adresa de e-mail a contului partajat, astfel:

```
tu@exemplue.com\shared@example.com
```

Rețineți că trebuie să fie o bară oblică inversă și nu o bară oblică directă.

Atunci când folosiți o căsuță poștală partajată, probabil că veți dori să activați opțiunea *Sincronizarea listelor de dosare partajate* în setările de primire.

<br />

<a name="faq140"></a>
**(140) De ce textul mesajului conține caractere ciudate?**

Afișarea caracterelor ciudate este aproape întotdeauna cauzată de specificarea unei codificări de caractere inexistente sau invalide de către software-ul de trimitere. FairEmail va presupune [ISO 8859-1](https://en.wikipedia.org/wiki/ISO/IEC_8859-1) atunci când nu există un set de caractere sau când a fost specificat [US-ASCII](https://en.wikipedia.org/wiki/ASCII). În afară de aceasta, nu există nicio modalitate de a determina în mod fiabil și automat codificarea corectă a caracterelor, astfel încât acest lucru nu poate fi fixat de FairEmail. Acțiunea corectă este de a depune o plângere la expeditor.

<br />

<a name="faq141"></a>
**(141) Cum pot rezolva problema 'Este necesar un dosar de ciorne pentru a trimite mesaje'?**

Pentru a stoca proiecte de mesaje, este necesar un dosar de proiecte. În cele mai multe cazuri, FairEmail va selecta automat dosarele de ciorne la adăugarea unui cont. în funcție de [atributele](https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml) pe care le trimite serverul de e-mail. Cu toate acestea, unele servere de e-mail nu sunt configurate corespunzător și nu trimit aceste atribute. În acest caz, FairEmail încearcă să identifice dosarul drafturi după nume, dar acest lucru ar putea eșua dacă dosarul "drafturi" are un nume neobișnuit sau nu este prezent deloc.

Puteți remedia această problemă prin selectarea manuală a dosarului de ciorne în setările contului (Setări, atingeți Configurare manuală, atingeți Conturi, atingeți Cont, în partea de jos). Dacă nu există niciun dosar de proiecte, puteți crea un dosar de ciorne apăsând pe butonul "+" din lista de dosare a contului (apăsați pe numele contului în meniul de navigare).

Unii furnizori, cum ar fi Gmail, permit activarea/dezactivarea IMAP pentru foldere individuale. Prin urmare, dacă un dosar nu este vizibil, este posibil să trebuiască să activați IMAP pentru acel dosar.

Legătură rapidă pentru Gmail (funcționează numai pe un computer desktop): [https://mail.google.com/mail/u/0/#settings/labels](https://mail.google.com/mail/u/0/#settings/labels)

<br />

<a name="faq142"></a>
**(142) Cum pot stoca mesajele trimise în inbox?**

În general, nu este o idee bună să stocați mesajele trimise în căsuța de primire, deoarece acest lucru este greu de anulat și ar putea fi incompatibil cu alți clienți de e-mail.

Acestea fiind spuse, FairEmail este capabil să gestioneze în mod corespunzător mesajele trimise în inbox. FairEmail va marca mesajele de ieșire cu o pictogramă de mesaje trimise, de exemplu.

Cea mai bună soluție ar fi să permiteți afișarea dosarului trimis în căsuța de primire unificată prin apăsarea lungă a dosarului trimis din lista de dosare și activarea *Show in unified inbox*. În acest fel, toate mesajele pot rămâne acolo unde le este locul, permițând în același timp vizualizarea atât a mesajelor primite, cât și a celor trimise într-un singur loc.

Dacă aceasta nu este o opțiune, puteți [crea o regulă](#user-content-faq71) pentru a muta automat mesajele trimise în căsuța de primire sau să setați o adresă CC/BCC implicită în setările avansate de identitate pentru a vă trimite o copie.

<br />

<a name="faq143"></a>
**~~~(143) Puteți adăuga un folder de gunoi pentru conturile POP3?~~**

[POP3](https://en.wikipedia.org/wiki/Post_Office_Protocol) este un protocol foarte limitat. Practic, numai mesajele pot fi descărcate și șterse din căsuța de primire. Nici măcar nu este posibil să se marcheze un mesaj ca fiind citit.

Deoarece POP3 nu permite accesul la folderul de gunoi, nu există nicio modalitate de a restaura mesajele aruncate la gunoi.

Rețineți că puteți ascunde mesajele și puteți căuta mesajele ascunse, ceea ce este similar cu un dosar de gunoi local, fără a sugera că mesajele aruncate la coșul de gunoi pot fi restabilite, deși acest lucru nu este de fapt posibil.

Versiunea 1.1082 a adăugat un folder de gunoi local. Rețineți că distrugerea unui mesaj îl va elimina definitiv de pe server și că mesajele distruse nu mai pot fi restaurate pe server.

<br />

<a name="faq144"></a>
**(144) Cum pot înregistra note vocale?**

Pentru a înregistra notițe vocale, puteți apăsa această pictogramă în bara de acțiune din partea de jos a compozitorului de mesaje:

![Imagine externă](https://github.com/M66B/FairEmail/blob/master/images/baseline_record_voice_over_black_48dp.png)

Acest lucru necesită instalarea unei aplicații de înregistrare audio compatibile. În special [această intenție comună](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION). trebuie să fie susținută.

De exemplu, [acest înregistrator audio](https://f-droid.org/app/com.github.axet.audiorecorder) este compatibil.

Notele vocale vor fi atașate automat.

<br />

<a name="faq145"></a>
**(145) Cum pot seta un sunet de notificare pentru un cont, dosar sau expeditor?**

Cont:

* Activați * Notificări separate* în setările avansate ale contului (Setări, atingeți Configurare manuală, atingeți Conturi, atingeți Cont, atingeți Cont, atingeți Avansat)
* Apăsați lung contul din lista de conturi (Setări, atingeți Configurare manuală, atingeți Conturi) și selectați *Modifică canalul de notificare* pentru a schimba sunetul de notificare

Folder:

* Apăsați lung folderul din lista de foldere și selectați *Creare canal de notificare*
* Apăsați lung folderul din lista de foldere și selectați *Edit notification channel* pentru a schimba sunetul de notificare

Expeditor:

* Deschideți un mesaj de la expeditor și extindeți-l
* Extindeți secțiunea Adrese apăsând pe săgeata în jos
* Atingeți pictograma clopot pentru a crea sau edita un canal de notificare și pentru a modifica sunetul de notificare

Ordinea de preponderență este următoarea: sunetul expeditorului, sunetul dosarului, sunetul contului și sunetul implicit.

Setarea unui sunet de notificare pentru un cont, un dosar sau un expeditor necesită Android 8 Oreo sau o versiune ulterioară și este o funcție pro.

<br />

<a name="faq146"></a>
**(146) Cum pot repara timpii incorecți ai mesajelor?**

Deoarece data/ora trimisă este opțională și poate fi manipulată de către expeditor, FairEmail utilizează în mod implicit data/ora primită de server.

Uneori, data/ora primită de server este incorectă, de cele mai multe ori pentru că mesajele au fost importate incorect de pe un alt server și uneori din cauza unei erori a serverului de e-mail.

În aceste cazuri rare, este posibil să lăsați FairEmail să folosească fie data/ora din antetul *Date* (ora de trimitere), fie din antetul *Received* ca o soluție de rezolvare. Acest lucru poate fi modificat în setările avansate ale contului: Setări, atingeți Configurare manuală, atingeți Conturi, atingeți Cont, atingeți Cont, atingeți Avansat.

Acest lucru nu va modifica ora mesajelor deja sincronizate. Pentru a rezolva acest lucru, apăsați lung pe folderul (folderele) din lista de foldere și selectați *Șterge mesajele locale* și *Sincronizează acum*.

<br />

<a name="faq147"></a>
**(147) Ce ar trebui să știu despre versiunile terțe?**

Probabil ai ajuns aici pentru că folosești o versiune terță parte a FairEmail.

Există **suport doar** pe cea mai recentă versiune din Magazinul Play, cea mai recentă versiune GitHub și versiunea F-Droid, dar **numai dacă** numărul versiunii de construcţie F-Droid este acelaşi cu numărul versiunii celei mai recente versiuni GitHub.

F-Droid se construiește neregulat, ceea ce poate fi problematic atunci când există o actualizare importantă. Prin urmare, vă sfătuim să treceți la versiunea GitHub.

Versiunea F-Droid este construită din același cod sursă, dar este semnată diferit. Acest lucru înseamnă că toate caracteristicile sunt disponibile și în versiunea F-Droid, cu excepția utilizării asistentului de configurare rapidă Gmail, deoarece Google a aprobat (și permite) o singură semnătură de aplicație. Pentru toți ceilalți furnizori de e-mail, accesul OAuth este disponibil doar în versiunile Play Store și în versiunile Github, deoarece furnizorii de e-mail permit utilizarea OAuth doar pentru versiunile oficiale.

Rețineți că va trebui să dezinstalați mai întâi versiunea F-Droid înainte de a putea instala o versiune GitHub. deoarece, din motive de securitate, Android refuză să instaleze aceeași aplicație cu o semnătură diferită.

Rețineți că versiunea GitHub va verifica automat dacă există actualizări. Dacă doriți, acest lucru poate fi dezactivat în setările diverse.

Vă rugăm să [vezi aici](https://github.com/M66B/FairEmail/blob/master/README.md#user-content-downloads) pentru toate opțiunile de descărcare.

Dacă aveți o problemă cu compilarea F-Droid, vă rugăm să verificați mai întâi dacă există o versiune GitHub mai nouă.

<br />

<a name="faq148"></a>
**(148) Cum pot utiliza un cont Apple iCloud?**

Există un profil încorporat pentru Apple iCloud, astfel încât ar trebui să puteți utiliza expertul de configurare rapidă (alt furnizor). Dacă este necesar, puteți găsi setările corecte [ aici](https://support.apple.com/en-us/HT202304) pentru a configura manual un cont.

Atunci când utilizați autentificarea cu doi factori, este posibil să fie necesar să folosiți o parolă [ specifică aplicației](https://support.apple.com/en-us/HT204397).

<br />

<a name="faq149"></a>
**(149) Cum funcționează widget-ul pentru numărul de mesaje necitite?**

Widgetul de numărare a mesajelor necitite afișează numărul de mesaje necitite, fie pentru toate conturile, fie pentru un cont selectat, dar numai pentru dosarele pentru care sunt activate notificările de mesaje noi.

Dacă apăsați pe notificare, se vor sincroniza toate dosarele pentru care este activată sincronizarea și se vor deschide:

* ecranul de start atunci când au fost selectate toate conturile
* o listă de dosare atunci când a fost selectat un anumit cont și când notificările de mesaje noi sunt activate pentru mai multe dosare
* o listă de mesaje atunci când a fost selectat un anumit cont și când sunt activate notificările de mesaje noi pentru un dosar

<br />

<a name="faq150"></a>
**(150) Puteți adăuga anularea invitațiilor din calendar?**

Anularea invitațiilor din calendar (eliminarea evenimentelor din calendar) necesită permisiunea de scriere în calendar, ceea ce va avea ca rezultat acordarea efectivă a permisiunii de citire și scriere a *toate* evenimentelor calendaristice din *toate* calendare.

Având în vedere scopul FairEmail, confidențialitatea și securitatea, și având în vedere că este ușor să eliminați manual un eveniment din calendar, nu este o idee bună să solicitați această permisiune doar din acest motiv.

Inserarea de noi evenimente din calendar se poate face fără permisiuni cu ajutorul unor [intente speciale](https://developer.android.com/guide/topics/providers/calendar-provider.html#intents). Din păcate, nu există nicio intenție de a șterge evenimentele existente din calendar.

<br />

<a name="faq151"></a>
**(151) Puteți adăuga backup/restaurare a mesajelor?**

Un client de e-mail este menit să citească și să scrie mesaje, nu să facă copii de rezervă și să restaureze mesaje. Rețineți că ruperea sau pierderea dispozitivului înseamnă pierderea mesajelor!

În schimb, furnizorul/serverul de e-mail este responsabil pentru copiile de rezervă.

Dacă doriți să faceți o copie de rezervă dumneavoastră, puteți utiliza un instrument precum [imapsync](https://imapsync.lamiral.info/).

Începând cu versiunea 1.1556, este posibilă exportarea tuturor mesajelor dintr-un dosar POP3 în format mbox în conformitate cu [RFC4155](https://www.ietf.org/rfc/rfc4155.txt), ceea ce ar putea fi util pentru a salva mesajele trimise dacă serverul de e-mail nu o face.

Dacă doriți să importați un fișier mbox într-un cont de e-mail existent, puteți utiliza Thunderbird pe un computer de birou și add-on-ul [ImportExportTools](https://addons.thunderbird.net/nl/thunderbird/addon/importexporttools/).

<br />

<a name="faq152"></a>
**(152) Cum pot introduce un grup de contacte?**

Puteți insera adresele de e-mail ale tuturor contactelor dintr-un grup de contacte prin intermediul meniului cu trei puncte din compozitorul de mesaje.

Puteți defini grupuri de contacte cu ajutorul aplicației de contacte Android, consultați [aici](https://support.google.com/contacts/answer/30970) pentru instrucțiuni.

<br />

<a name="faq153"></a>
**(153) De ce nu funcționează ștergerea permanentă a mesajelor Gmail?**

S-ar putea să fie nevoie să modificați [configurarea Gmail IMAP](https://mail.google.com/mail/u/0/#settings/fwdandpop) pe un browser de desktop pentru a o face să funcționeze:

* Când marchez un mesaj în IMAP ca fiind șters: Auto-Expunge off - Așteptați ca clientul să actualizeze serverul.
* Atunci când un mesaj este marcat ca fiind șters și eliminat din ultimul dosar IMAP vizibil: Ștergeți imediat mesajul pentru totdeauna

Rețineți că mesajele arhivate pot fi șterse numai dacă le mutați mai întâi în folderul coș de gunoi.

Câteva detalii: Gmail pare să aibă o vizualizare suplimentară a mesajelor pentru IMAP, care poate fi diferită de vizualizarea principală a mesajelor.

O altă ciudățenie este că o stea (mesaj favorit) setată prin interfața web nu poate fi eliminată cu comanda IMAP

```
STOCARE <message number> -FLAGS (\Flagged)
```

Pe de altă parte, o stea setată prin IMAP este afișată în interfața web și poate fi eliminată prin IMAP.

<br />

<a name="faq154"></a>
**~~(154) Puteți adăuga favicons ca fotografii de contact?~~**

~~În afară de faptul că un [favicon](https://en.wikipedia.org/wiki/Favicon) ar putea fi partajat de mai multe adrese de e-mail cu același nume de domeniu~ ~~ ~~ și, prin urmare, nu este direct legat de o adresă de e-mail, faviconurile pot fi folosite pentru a vă urmări.~~

<br />

<a name="faq155"></a>
**(155) Ce este un fișier winmail.dat?**

Un fișier *winmail.dat* este trimis de un client Outlook configurat incorect. Este un format de fișier specific Microsoft ([TNEF](https://en.wikipedia.org/wiki/Transport_Neutral_Encapsulation_Format)) care conține un mesaj și, eventual, atașamente.

Puteți găsi mai multe informații despre acest fișier [ aici](https://support.mozilla.org/en-US/kb/what-winmaildat-attachment).

Îl puteți vizualiza, de exemplu, cu aplicația Android [Letter Opener](https://play.google.com/store/apps/details?id=app.letteropener).

<br />

<a name="faq156"></a>
**(156) Cum pot să configurez un cont Office 365?**

Un cont Office 365 poate fi configurat prin intermediul expertului de configurare rapidă și prin selectarea *Office 365 (OAuth)*.

Dacă expertul se încheie cu *AUTHENTICATE failed*, este posibil ca IMAP și/sau SMTP să fie dezactivate pentru cont. În acest caz, trebuie să solicitați administratorului să activeze IMAP și SMTP. Procedura este documentată [aici](https://docs.microsoft.com/en-in/exchange/troubleshoot/configure-mailboxes/pop3-imap-owa-activesync-office-365).

Dacă ați activat *security defaults* în organizația dvs., este posibil să fie necesar să activați protocolul SMTP AUTH. Vă rugăm să [vezi aici](https://docs.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/authenticated-client-smtp-submission) despre cum să.

<br />

<a name="faq157"></a>
**(157) Cum îmi pot crea un cont Free.fr?**

Vă rugăm să [vezi aici](https://free.fr/assistance/597.html) pentru instrucțiuni.

**SMTP este dezactivat în mod implicit**, vă rugăm să [vezi aici](https://free.fr/assistance/2406.html) cum poate fi activat.

Vă rugăm să [vedeți aici](http://jc.etiemble.free.fr/abc/index.php/trucs-astuces/configurer-smtp-free-fr) pentru un ghid detaliat.

<br />

<a name="faq103"></a>
<a name="faq158"></a>
**(158) Ce aparat foto / înregistrator audio recomandați?**

Pentru a face fotografii și pentru a înregistra audio sunt necesare o cameră foto și o aplicație de înregistrare audio. Următoarele aplicații sunt camere și aparate de înregistrare audio open source:

* [Deschideți camera](https://play.google.com/store/apps/details?id=net.sourceforge.opencamera) ([F-Droid](https://f-droid.org/en/packages/net.sourceforge.opencamera/))
* [Audio Recorder versiunea 3.3.24+](https://play.google.com/store/apps/details?id=com.github.axet.audiorecorder) ([F-Droid](https://f-droid.org/packages/com.github.axet.audiorecorder/))

Pentru a înregistra note vocale etc., înregistratorul audio trebuie să suporte [MediaStore.Audio.Media.RECORD_SOUND_ACTION](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media#RECORD_SOUND_ACTION). În mod ciudat, majoritatea aparatelor de înregistrare audio nu par să suporte această acțiune standard Android.

<br />

<a name="faq159"></a>
**(159) Care sunt listele de protecție a urmăritorilor de la Disconnect?**

Vă rugăm să consultați [aici](https://disconnect.me/trackerprotection) pentru mai multe informații despre listele de protecție a urmăritorilor de la Disconnect.

După descărcarea listelor în setările de confidențialitate, listele pot fi utilizate opțional:

* pentru a avertiza cu privire la legăturile de urmărire la deschiderea legăturilor
* pentru a recunoaște imaginile de urmărire din mesaje

Imaginile de urmărire vor fi dezactivate numai dacă este activată opțiunea principală "disable" corespunzătoare.

Imaginile de urmărire nu vor fi recunoscute atunci când domeniul este clasificat ca fiind "*Content*", a se vedea [aici](https://disconnect.me/trackerprotection#trackers-we-dont-block) pentru mai multe informații.

Această comandă poate fi trimisă către FairEmail de la o aplicație de automatizare pentru a actualiza listele de protecție:

```
(adb shell) am start-foreground-service -a eu.faircode.email.DISCONNECT.ME
```

O actualizare o dată pe săptămână va fi probabil suficientă, vă rugăm să consultați [aici](https://github.com/disconnectme/disconnect-tracking-protection/commits/master) pentru modificările recente ale listelor.

<br />

<a name="faq160"></a>
**(160) Puteți adăuga ștergerea permanentă a mesajelor fără confirmare?**

Ștergerea permanentă înseamnă că mesajele vor fi pierdute *irreversibil* și, pentru a preveni ca acest lucru să se întâmple accidental, acest lucru trebuie să fie confirmat întotdeauna. Chiar și cu o confirmare, m-au contactat niște oameni foarte supărați că au pierdut o parte din mesajele lor din vina lor, ceea ce a fost o experiență destul de neplăcută :-(

Avansat: nu se acceptă indicatorul de ștergere IMAP în combinație cu comanda EXPUNGE. deoarece atât serverele de e-mail, cât și nu toate persoanele pot gestiona acest lucru, existând riscul pierderii neașteptate a mesajelor. Un factor care complică situația este faptul că nu toate serverele de e-mail acceptă [UID EXPUNGE](https://tools.ietf.org/html/rfc4315).

Începând cu versiunea 1.1485, este posibil să se activeze temporar modul de depanare în setările diverse pentru a dezactiva ștergerea mesajelor. Rețineți că mesajele cu un steag *\Deleted* nu vor fi afișate în FairEmail.

<br />

<a name="faq161"></a>
**(161) Puteți adăuga o setare pentru a schimba culoarea principală și cea de accent?***

Dacă aș putea, aș adăuga o setare pentru a selecta imediat culoarea principală și cea de accent, dar, din păcate, temele Android sunt fixe, a se vedea, de exemplu, [aici](https://stackoverflow.com/a/26511725/1794097), așa că acest lucru nu este posibil.

<br />

<a name="faq162"></a>
**(162) Este IMAP NOTIFY acceptat?***

Da, [IMAP NOTIFY](https://tools.ietf.org/html/rfc5465) a fost acceptat începând cu versiunea 1.1413.

Suportul IMAP NOTIFY înseamnă că vor fi solicitate notificări pentru mesajele adăugate, modificate sau șterse din toate dosarele *subscrise*. iar dacă se primește o notificare pentru un dosar abonat, acel dosar va fi sincronizat. Prin urmare, sincronizarea pentru dosarele abonate poate fi dezactivată, economisind conexiunile dosarelor la serverul de e-mail.

**Important**: mesajele push (=întotdeauna sincronizate) pentru căsuța de primire și gestionarea abonamentelor (setări de primire) trebuie să fie activate.

**Important**: majoritatea serverelor de e-mail nu acceptă acest lucru! Puteți verifica jurnalul prin intermediul meniului de navigare în cazul în care un server de e-mail acceptă capacitatea NOTIFY.

<br />

<a name="faq163"></a>
**(163) Ce este clasificarea mesajelor?**

*Aceasta este o caracteristică experimentală!*

Clasificarea mesajelor va încerca să grupeze automat e-mailurile în clase, pe baza conținutului acestora, folosind [Statistica Bayesiană](https://en.wikipedia.org/wiki/Bayesian_statistics). În contextul FairEmail, un folder este o clasă. Astfel, de exemplu, căsuța de primire, dosarul de spam, un dosar "marketing" etc. etc.

Puteți activa clasificarea mesajelor în setările diverse. Acest lucru va activa doar modul 'învățare'. Clasificatorul va 'învăța' în mod implicit din mesajele noi din căsuța de primire și din dosarul de spam. Proprietatea de folder *Clasifică mesajele noi în acest folder* va activa sau dezactiva modul 'învățare' pentru un folder. Puteți șterge mesajele locale (apăsați lung pe un dosar din lista de dosare a unui cont) și sincroniza din nou mesajele pentru a clasifica mesajele existente.

Fiecare dosar are o opțiune *Mutarea automată a mesajelor clasificate în acest dosar* ('clasificare automată' pe scurt). Când această opțiune este activată, mesajele noi din alte dosare despre care clasificatorul crede că aparțin acelui dosar vor fi mutate automat.

Opțiunea *Utilizează filtrul local de spam* din fereastra de dialog Raportează spam va activa clasificarea mesajelor în setările diverse. și clasificarea automată pentru dosarul de spam. Vă rugăm să înțelegeți că acest lucru nu este un înlocuitor pentru filtrul de spam al serverului de e-mail și poate avea ca rezultat [falsi pozitive și false negative](https://en.wikipedia.org/wiki/False_positives_and_false_negatives). A se vedea, de asemenea, [acest FAQ](#user-content-faq92).

Un exemplu practic: să presupunem că există un folder 'marketing' și că este activată clasificarea automată a mesajelor pentru acest folder. De fiecare dată când mutați un mesaj în acest dosar, veți instrui FairEmail că mesajele similare aparțin acestui dosar. De fiecare dată când mutați un mesaj din acest dosar, veți instrui FairEmail că mesajele similare nu au ce căuta în acest dosar. După mutarea unor mesaje în folderul 'marketing', FairEmail va începe să mute automat mesaje similare în acest folder. Sau, invers, după mutarea unor mesaje din folderul 'marketing', FairEmail nu va mai muta automat mesaje similare în acest folder. Acest lucru va funcționa cel mai bine cu mesaje cu conținut similar (adrese de e-mail, subiect și text al mesajului).

Clasificarea ar trebui considerată ca fiind cea mai bună presupunere - ar putea fi o presupunere greșită sau clasificatorul ar putea să nu fie suficient de încrezător pentru a face orice presupunere. În cazul în care clasificatorul nu este sigur, va lăsa pur și simplu un e-mail acolo unde este.

Pentru a împiedica serverul de e-mail să mute din nou și din nou un mesaj în dosarul de spam, clasificarea automată în afara dosarului de spam nu se va face.

Clasificatorul de mesaje calculează probabilitatea ca un mesaj să aparțină unui dosar (clasă). Există două opțiuni în setările diverse care controlează dacă un mesaj va fi mutat automat într-un dosar, cu condiția ca clasificarea automată să fie activată pentru folderul respectiv:

* *Probabilitatea minimă a clasei*: un mesaj va fi mutat numai atunci când încrederea că aparține unui dosar este mai mare decât această valoare (implicit 15 %)
* *Diferența minimă a clasei*: un mesaj va fi mutat numai atunci când diferența de încredere între o clasă și următoarea clasă cea mai probabilă este mai mare decât această valoare (implicit 50 %)

Ambele condiții trebuie să fie îndeplinite înainte ca un mesaj să fie mutat.

Având în vedere valorile implicite ale opțiunilor:

* Merele 40 % și bananele 30 % nu vor fi luate în considerare, deoarece diferența de 25 % este mai mică decât minimul de 50 %
* Merele 10 % și bananele 5 % nu vor fi luate în considerare, deoarece probabilitatea pentru mere este sub minimul de 15 %
* Merele 50 % și bananele 20 % ar duce la selectarea merelor

Clasificarea este optimizată pentru a utiliza cât mai puține resurse posibil, dar, în mod inevitabil, va utiliza o cantitate suplimentară de energie din baterie.

Puteți șterge toate datele de clasificare prin dezactivarea de trei ori a clasificării din Setări diverse.

[Reguli de filtrare](#user-content-faq71) vor fi executate înainte de clasificare.

Clasificarea mesajelor este o caracteristică pro, cu excepția dosarului de spam.

<br />

<a name="faq164"></a>
**(164) Puteți adăuga teme personalizabile?**

Din păcate, Android [nu acceptă](https://stackoverflow.com/a/26511725/1794097) teme dinamice, ceea ce înseamnă că toate temele trebuie [să fie predefinite](https://github.com/M66B/FairEmail/blob/master/app/src/main/res/values/styles.xml).

Deoarece pentru fiecare temă trebuie să existe o variantă deschisă, una închisă și una neagră, nu este fezabil să se adauge pentru fiecare combinație de culori (literalmente milioane) o temă predefinită.

În plus, o temă este mai mult decât câteva culori. De exemplu, temele cu o culoare de accent galben folosesc o culoare de legătură mai închisă pentru un contrast suficient.

Culorile temei se bazează pe cercul cromatic al lui [Johannes Itten](https://en.wikipedia.org/wiki/Johannes_Itten).

<br />

<a name="faq165"></a>
**(165) Este acceptat Android Auto?**

Da, Android Auto este acceptat, dar numai cu versiunea GitHub, vă rugăm să [vezi aici](https://forum.xda-developers.com/t/app-5-0-fairemail-fully-featured-open-source-privacy-oriented-email-app.3824168/post-83801249) despre motivul pentru care.

Pentru suportul pentru notificări (mesagerie), va trebui să activați următoarele opțiuni de notificare:

* *Folosiți formatul de notificare Android în 'stil mesagerie'*
* Acțiuni de notificare: *Răspuns direct* și (marcare ca) *Citește*

Dacă doriți, puteți activa și alte acțiuni de notificare, dar acestea nu sunt acceptate de Android Auto.

Ghidul dezvoltatorilor este [aici](https://developer.android.com/training/cars/messaging).

<br />

<a name="faq166"></a>
**(166) Pot să repet un mesaj pe mai multe dispozitive?**

În primul rând, nu există un standard pentru mesajele de snooze, astfel încât toate implementările de snooze sunt soluții personalizate.

Anumiți furnizori de e-mail, cum ar fi Gmail, mută mesajele care au fost amânate într-un dosar special. Din păcate, aplicațiile terțe nu au acces la acest dosar special.

Mutarea unui mesaj în alt dosar și înapoi ar putea eșua și ar putea să nu fie posibilă dacă nu există o conexiune la internet. Acest lucru este problematic, deoarece un mesaj poate fi amânat numai după mutarea mesajului.

Pentru a preveni aceste probleme, snoozing-ul se face local pe dispozitiv prin ascunderea mesajului în timpul snoozing-ului. Din păcate, nu este posibilă ascunderea mesajelor și pe serverul de e-mail.

<br />

<h2><a name="get-support"></a>Obțineți asistență</h2>

FairEmail este suportat doar pe smartphone-uri și tablete Android și ChromeOS.

Sunt acceptate doar cea mai recentă versiune a magazinului Play și cea mai recentă versiune GitHub. Construcția F-Droid este acceptată numai dacă numărul versiunii este același cu cel al celei mai recente versiuni GitHub. Acest lucru înseamnă, de asemenea, că nu este posibilă retrogradarea.

Nu există suport pentru lucruri care nu sunt direct legate de FairEmail.

Nu există niciun sprijin pentru a construi și dezvolta lucruri de unul singur.

Caracteristicile solicitate ar trebui:

* să fie utile pentru majoritatea oamenilor
* să nu îngreuneze utilizarea FairEmail
* se încadrează în filozofia FairEmail (orientată spre confidențialitate, orientată spre securitate)
* respectă standardele comune (IMAP, SMTP, etc.)

Caracteristicile care nu îndeplinesc aceste cerințe vor fi probabil respinse. Acest lucru este, de asemenea, pentru ca întreținerea și asistența pe termen lung să rămână fezabile.

Dacă aveți o întrebare, doriți să solicitați o caracteristică sau să raportați o eroare, **vă rugăm să folosiți [acest formular](https://contact.faircode.eu/?product=fairemailsupport)**.

Problemele de pe GitHub sunt dezactivate din cauza utilizării abuzive frecvente.

<br />

Copyright &copy; 2018-2021 Marcel Bokhorst.
