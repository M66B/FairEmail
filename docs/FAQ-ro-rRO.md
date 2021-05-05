<a name="top"></a>

# Asistență FairEmail

Dacă aveți o întrebare, vă rugăm să consultați mai întâi următoarele întrebări frecvente. [At the bottom](#user-content-get-support), you can find out how to ask other questions, request features, and report bugs.

Dacă aveți o întrebare, vă rugăm să consultați mai întâi întrebările frecvente (FAQ) de mai jos. [Ganz unten erfahren Sie](#user-content-get-support), wie Sie weitere Fragen stellen, Funktionen anfordern und Fehler melden können.

## Index

* [Autorizarea conturilor](#user-content-authorizing-accounts)
* [Cum să...?](#user-content-howto)
* [Probleme cunoscute](#user-content-known-problems)
* [Funcții planificate](#user-content-planned-features)
* [Funcții solicitate frecvent](#user-content-frequently-requested-features)
* [Întrebări puse frecvent](#user-content-frequently-asked-questions)
* [Obțineți asistență](#user-content-get-support)

## Autorizarea conturilor

În cele mai multe cazuri, asistentul de configurare rapidă va fi capabil să identifice automat configurația corectă.

Dacă expertul de configurare rapidă eșuează, va trebui să configurați manual un cont (pentru a primi e-mailuri) și o identitate (pentru a trimite e-mailuri). Pentru aceasta veți avea nevoie de adresele și numerele de port ale serverelor IMAP și SMTP, dacă trebuie utilizat SSL/TLS sau STARTTLS și numele dvs. de utilizator (în general, dar nu întotdeauna, adresa dvs. de e-mail) și parola.

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
* Schimbarea parolei: Setări, apăsați Configurare manuală, apăsați Conturi, apăsați Cont, apăsați Cont, schimbați parola
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

## Probleme cunoscute

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

## Funcții planificate

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

## Funcții solicitate frecvent

Designul se bazează pe multe discuții și dacă doriți puteți discuta despre el [ și pe acest forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168) de asemenea. Scopul designului este de a fi minimalist (fără meniuri inutile, butoane, etc.) și de a nu distrage atenția (fără culori fanteziste, animații, etc.). Toate lucrurile expuse ar trebui să fie utile într-un fel sau altul și ar trebui să fie poziționate cu grijă pentru a fi utilizate cu ușurință. Fonturile, dimensiunile, culorile etc. ar trebui să fie de tip material design ori de câte ori este posibil.

## Întrebări puse frecvent

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

Numele dvs. de utilizator este probabil ușor de ghicit, deci acest lucru este nesigur.

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

Rețineți că este acceptată semnarea S/MIME  cu alți algoritmi decât RSA, dar rețineți că este posibil ca alți clienți de e-mail să nu accepte acest lucru. Criptarea S/MIME este posibilă numai cu algoritmi simetrici, ceea ce înseamnă, în practică, utilizarea RSA.

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

An error in a rule condition can lead to a disaster, therefore irreversible actions are not supported.

Rules are applied directly after the message header has been fetched, but before the message text has been downloaded, so it is not possible to apply conditions to the message text. Note that large message texts are downloaded on demand on a metered connection to save on data usage.

If you want to forward a message, consider to use the move action instead. This will be more reliable than forwarding as well because forwarded messages might be considered as spam.

Since message headers are not downloaded and stored by default to save on battery and data usage and to save storage space it is not possible to preview which messages would match a header rule condition.

Some common header conditions (regex):

* *.&ast;Auto-Submitted:.&ast;* [RFC3834](https://tools.ietf.org/html/rfc3834)
* *.&ast;Content-Type: multipart/report.&ast;* [RFC3462](https://tools.ietf.org/html/rfc3462)

In the three-dots *more* message menu there is an item to create a rule for a received message with the most common conditions filled in.

The POP3 protocol does not support setting keywords and moving or copying messages.

Using rules is a pro feature.

<br />

<a name="faq72"></a>
**(72) What are primary accounts/identities?**

The primary account is used when the account is ambiguous, for example when starting a new draft from the unified inbox.

Similarly, the primary identity of an account is used when the identity is ambiguous.

There can be just one primary account and there can be just one primary identity per account.

<br />

<a name="faq73"></a>
**(73) Is moving messages across accounts safe/efficient?**

Moving messages across accounts is safe because the raw, original messages will be downloaded and moved and because the source messages will be deleted only after the target messages have been added

Batch moving messages across accounts is efficient if both the source folder and target folder are set to synchronize, else FairEmail needs to connect to the folder(s) for each message.

<br />

<a name="faq74"></a>
**(74) Why do I see duplicate messages?**

Some providers, notably Gmail, list all messages in all folders, except trashed messages, in the archive (all messages) folder too. FairEmail shows all these messages in a non obtrusive way to indicate that these messages are in fact the same message.

Gmail allows one message to have multiple labels, which are presented to FairEmail as folders. This means that messages with multiple labels will be shown multiple times as well.

<br />

<a name="faq75"></a>
**(75) Can you make an iOS, Windows, Linux, etc version?**

A lot of knowledge and experience is required to successfully develop an app for a specific platform, which is why I develop apps for Android only.

<br />

<a name="faq76"></a>
**(76) What does 'Clear local messages' do?**

The folder menu *Clear local messages* removes messages from the device which are present on the server too. It does not delete messages from the server. This can be useful after changing the folder settings to not download the message content (text and attachments), for example to save space.

<br />

<a name="faq77"></a>
**(77) Why are messages sometimes shown with a small delay?**

Depending on the speed of your device (processor speed and maybe even more memory speed) messages might be displayed with a small delay. FairEmail is designed to dynamically handle a large number of messages without running out of memory. This means that messages needs to be read from a database and that this database needs to be watched for changes, both of which might cause small delays.

Some convenience features, like grouping messages to display conversation threads and determining the previous/next message, take a little extra time. Note that there is no *the* next message because in the meantime a new message might have been arrived.

When comparing the speed of FairEmail with similar apps this should be part of the comparison. It is easy to write a similar, faster app which just displays a lineair list of messages while possible using too much memory, but it is not so easy to properly manage resource usage and to offer more advanced features like conversation threading.

FairEmail is based on the state-of-the-art [Android architecture components](https://developer.android.com/topic/libraries/architecture/), so there is little room for performance improvements.

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
O nouă sarcină: Ceva recognoscibil
Categoria de acțiune: "Acțiune": Diverse/Anunțuri de trimitere
Acțiune: eu.faircode.email.ENABLE
Țintă: Serviciul
```

To enable/disable an account with the name *Gmail*:

```
Extras: cont:Gmail
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
**(82) What is a tracking image?**

Please see [here](https://en.wikipedia.org/wiki/Web_beacon) about what a tracking image exactly is. In short tracking images keep track if you opened a message.

FairEmail will in most cases automatically recognize tracking images and replace them by this icon:

![External image](https://github.com/M66B/FairEmail/blob/master/images/baseline_my_location_black_48dp.png)

Automatic recognition of tracking images can be disabled in the privacy settings.

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

* identitatea este setată pentru sincronizare (trimiterea de mesaje)
* contul asociat este setat să se sincronizeze (să primească mesaje)
* contul asociat are un dosar de ciorne

FairEmail will try to select the best identity based on the *to* address of the message replied to / being forwarded.

<br />

<a name="faq86"></a>
**~~(86) What are 'extra privacy features'?~~**

~~The advanced option *extra privacy features* enables:~~

* ~~ Căutarea proprietarului adresei IP a unui link ~~
* ~~Detectarea și eliminarea [imaginilor de urmărire](#user-content-faq82)~~

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

* [pentru Yahoo](https://help.yahoo.com/kb/generate-third-party-passwords-sln15241.html)
* [pentru AOL](https://help.aol.com/articles/Create-and-manage-app-password)
* [pentru Sky](https://www.sky.com/help/articles/getting-started-with-sky-yahoo-mail) (sub *Alte aplicații de e-mail*)

Please see [this FAQ](#user-content-faq111) about OAuth support.

Note that Yahoo, AOL, and Sky do not support standard push messages. The Yahoo email app uses a proprietary, undocumented protocol for push messages.

Push messages require [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE) and the Yahoo email server does not report IDLE as capability:

```
CAPACITATEA Y1
* CAPABILITY IMAP4rev1 ID MOVE NAMESPACE XYMHIGHESTMODSEQ UIDPLUS LITERAL+ CHILDREN X-MSG-EXT UNSELECT OBJECTID
Y1 OK CAPABILITY finalizat
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

* Elimină textele de mesaje vechi
* Elimină fișierele atașate vechi
* Elimină fișierele de imagine vechi
* Elimină contactele locale vechi
* Elimină intrările vechi din jurnal

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

* Creați un filtru nou prin Gmail > Setări (roată) > Filtre și adrese blocate > Creați un filtru nou
* Introduceți o căutare pe categorii (a se vedea mai jos) în câmpul *Cuvintele* și faceți clic pe *Creare filtru*
* Bifați *Aplicați eticheta* și selectați o etichetă și faceți clic pe *Creare filtru*

Possible categories:

```
category:social
categoria:actualizări
category:forumuri
categoria:promoții
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

* ~~ Treceți la versiunea oficială a FairEmail, vedeți [ aici](https://github.com/M66B/FairEmail/blob/master/README.md#downloads) pentru opțiuni~~
* ~~Utilizați parole specifice aplicației, consultați [acest FAQ](#user-content-faq6)~~

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

* Asigurați-vă că aveți o conexiune la internet activă și funcțională
* Asigurați-vă că v-ați conectat cu contul Google corect și că nu există nicio problemă cu contul Google
* Asigurați-vă că ați instalat FairEmail prin intermediul contului Google corect dacă ați configurat mai multe conturi Google pe dispozitivul dvs
* Asigurați-vă că aplicația Play store este actualizată, vă rugăm [vezi aici](https://support.google.com/googleplay/answer/1050566?hl=en)
* Deschideți aplicația Magazin Play și așteptați cel puțin un minut pentru a-i da timp să se sincronizeze cu serverele Google
* Deschide FairEmail și navighează în ecranul de caracteristici pro pentru a permite FairEmail să verifice achizițiile; uneori ajută să atingi butonul *buy*

You can also try to clear the cache of the Play store app via the Android apps settings. Restarting the device might be necessary to let the Play store recognize the purchase correctly.

Note that:

* Dacă primiți *ITEM_ALREADY_OWNED*, probabil că aplicația magazinului Play trebuie să fie actualizată, vă rugăm să [vezi aici](https://support.google.com/googleplay/answer/1050566?hl=en)
* Achizițiile sunt stocate în cloud-ul Google și nu se pot pierde
* Nu există o limită de timp pentru achiziții, deci acestea nu pot expira
* Google nu expune dezvoltatorilor detalii (nume, e-mail, etc.) despre cumpărători
* O aplicație precum FairEmail nu poate selecta ce cont Google să folosească
* Este posibil să dureze ceva timp până când aplicația Magazin Play sincronizează o achiziție pe un alt dispozitiv
* Achizițiile din Play Store nu pot fi utilizate fără Play Store, ceea ce nu este permis nici de regulile Play Store

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

1. Preluarea antetelor de mesaj
1. Preluarea textului mesajului și a atașamentelor

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

* Cont: Setări > Configurare manuală > Conturi > apăsați cont
* Identitate: Setări > Configurare manuală > Identități > atingeți identitatea
* Folder: Apăsați lung folderul din lista de foldere > Editare proprietăți

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

* Mesajele push sunt activate pentru prea multe dosare: consultați [acest FAQ](#user-content-faq23) pentru mai multe informații și o soluție de rezolvare
* Parola contului a fost schimbată: schimbarea ei și în FairEmail ar trebui să rezolve problema
* O adresă de e-mail alias este utilizată ca nume de utilizator în loc de adresa de e-mail principală
* Se utilizează o schemă de conectare incorectă pentru o căsuță poștală partajată: schema corectă este *username@domain\SharedMailboxAlias*

The shared mailbox alias will mostly be the email address of the shared account, like this:

```
tu@exemplue.com\shared@example.com
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

![External image](https://github.com/M66B/FairEmail/blob/master/images/baseline_record_voice_over_black_48dp.png)

This requires a compatible audio recorder app to be installed. In particular [this common intent](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION) needs to be supported.

For example [this audio recorder](https://f-droid.org/app/com.github.axet.audiorecorder) is compatible.

Voice notes will automatically be attached.

<br />

<a name="faq145"></a>
**(145) How can I set a notification sound for an account, folder or sender?**

Account:

* Activați * Notificări separate* în setările avansate ale contului (Setări, atingeți Configurare manuală, atingeți Conturi, atingeți Cont, atingeți Cont, atingeți Avansat)
* Apăsați lung contul din lista de conturi (Setări, atingeți Configurare manuală, atingeți Conturi) și selectați *Modifică canalul de notificare* pentru a schimba sunetul de notificare

Folder:

* Apăsați lung folderul din lista de foldere și selectați *Creare canal de notificare*
* Apăsați lung folderul din lista de foldere și selectați *Edit notification channel* pentru a schimba sunetul de notificare

Sender:

* Deschideți un mesaj de la expeditor și extindeți-l
* Extindeți secțiunea Adrese apăsând pe săgeata în jos
* Atingeți pictograma clopot pentru a crea sau edita un canal de notificare și pentru a modifica sunetul de notificare

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

* ecranul de start atunci când au fost selectate toate conturile
* o listă de dosare atunci când a fost selectat un anumit cont și când notificările de mesaje noi sunt activate pentru mai multe dosare
* o listă de mesaje atunci când a fost selectat un anumit cont și când sunt activate notificările de mesaje noi pentru un dosar

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

* Când marchez un mesaj în IMAP ca fiind șters: Auto-Expunge off - Așteptați ca clientul să actualizeze serverul.
* Atunci când un mesaj este marcat ca fiind șters și eliminat din ultimul dosar IMAP vizibil: Ștergeți imediat mesajul pentru totdeauna

Note that archived messages can be deleted only by moving them to the trash folder first.

Some background: Gmail seems to have an additional message view for IMAP, which can be different from the main message view.

Another oddity is that a star (favorite message) set via the web interface cannot be removed with the IMAP command

```
STOCARE <message number> -FLAGS (\Flagged)
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

* [Deschideți camera](https://play.google.com/store/apps/details?id=net.sourceforge.opencamera) ([F-Droid](https://f-droid.org/en/packages/net.sourceforge.opencamera/))
* [Audio Recorder versiunea 3.3.24+](https://play.google.com/store/apps/details?id=com.github.axet.audiorecorder) ([F-Droid](https://f-droid.org/packages/com.github.axet.audiorecorder/))

To record voice notes, etc, the audio recorder needs to support [MediaStore.Audio.Media.RECORD_SOUND_ACTION](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media#RECORD_SOUND_ACTION). Oddly, most audio recorders seem not to support this standard Android action.

<br />

<a name="faq159"></a>
**(159) What are Disconnect's tracker protection lists?**

Please see [here](https://disconnect.me/trackerprotection) for more information about Disconnect's tracker protection lists.

After downloading the lists in the privacy settings, the lists can optionally be used:

* pentru a avertiza cu privire la legăturile de urmărire la deschiderea legăturilor
* pentru a recunoaște imaginile de urmărire din mesaje

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

* *Probabilitatea minimă a clasei*: un mesaj va fi mutat numai atunci când încrederea că aparține unui dosar este mai mare decât această valoare (implicit 15 %)
* *Diferența minimă a clasei*: un mesaj va fi mutat numai atunci când diferența de încredere între o clasă și următoarea clasă cea mai probabilă este mai mare decât această valoare (implicit 50 %)

Both conditions must be satisfied before a message will be moved.

Considering the default option values:

* Merele 40 % și bananele 30 % nu vor fi luate în considerare, deoarece diferența de 25 % este mai mică decât minimul de 50 %
* Merele 10 % și bananele 5 % nu vor fi luate în considerare, deoarece probabilitatea pentru mere este sub minimul de 15 %
* Merele 50 % și bananele 20 % ar duce la selectarea merelor

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

* *Folosiți formatul de notificare Android în 'stil mesagerie'*
* Acțiuni de notificare: *Răspuns direct* și (marcare ca) *Citește*

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

## Obțineți asistență

FairEmail is supported on smartphones, tablets and ChromeOS only.

Only the latest Play store version and latest GitHub release are supported. The F-Droid build is supported only if the version number is the same as the version number of the latest GitHub release. This also means that downgrading is not supported.

There is no support on things that are not directly related to FairEmail.

There is no support on building and developing things by yourself.

Requested features should:

* să fie utile pentru majoritatea oamenilor
* să nu îngreuneze utilizarea FairEmail
* se încadrează în filozofia FairEmail (orientată spre confidențialitate, orientată spre securitate)
* respectă standardele comune (IMAP, SMTP, etc.)

Features not fulfilling these requirements will likely be rejected. This is also to keep maintenance and support in the long term feasible.

If you have a question, want to request a feature or report a bug, **please use [this form](https://contact.faircode.eu/?product=fairemailsupport)**.

GitHub issues are disabled due to frequent misusage.

<br />

Copyright &copy; 2018-2021 Marcel Bokhorst.
