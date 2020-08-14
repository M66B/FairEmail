# FairEmail-support

Har du et spørgsmål, så tjek venligst de ofte stillede spørgsmål nedenfor først. Nederst kan du se, hvordan du stiller yderligere spørgsmål, anmoder om funktioner samt indrapporterer fejl.

## Indeks

* [Godkendelse af konti](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-authorizing-accounts)
* [Hvordan kan man...?](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-howto)
* [Kendte problemer](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-known-problems)
* [Planlagte funktioner](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-planned-features)
* [Hyppigt anmodede funktioner](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-frequently-requested-features)
* [Ofte stillede spørgsmål](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-frequently-asked-questions)
* [Support](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-support)

## Godkendelse af konti

I de fleste tilfælde vil hurtig opsætningen automatisk kunne identificere den rigtige opsætning.

Mislykkes hurtig opsætningen skal du manuelt opsætte en konto (for at modtage e-mail) samt identitet (for at sende e-mail). Til dette behøver du IMAP- og SMTP-serveradresserne og portnumrene, om enten SSL/TLS eller STARTTLS skal anvendes og dit brugernavn (for det meste, men ikke altid, din e-mailadresse) samt adgangskode.

En søgning på *IMAP* samt navnet på tjenesteleverandøren er ofte tilstrækkeligt til at finde den rette dokumentation.

I visse tilfælde vil du skulle aktivere ekstern adgang til din konto og/eller benytte en speciel (app-)adgangskode, eksempelvis når tofaktorgodkendelse er aktiveret.

Til godkendelse:

* Gmail/G Suite, se [spørgsmål 6](#user-content-faq6)
* Outlook/Live/Hotmail, se [spørgsmål 14](#user-content-faq14)
* Office365, se [spørgsmål 14](#user-content-faq156)
* Microsoft Exchange, se [spørgsmål 8](#user-content-faq8)
* Yahoo, AOL og Sky, se [spørgsmål 88](#user-content-faq88)
* Apple iCloud, se [spørgsmål 148](#user-content-faq148)
* Free.fr, se [spørgsmål 157](#user-content-faq157)

Se [hér](#user-content-faq22) for almindeligt forekommende fejlmeddelelser og løsninger.

Relaterede spørgsmål:

* [Understøttes OAuth?](#user-content-faq111)
* [Hvorfor understøttes ActiveSync ikke?](#user-content-faq133)

<a name="howto">

## Hvordan kan man...?

* Skifte kontonavnet: Opsætning, trin 1, Håndtér, tryk på konto
* Skift stryg mod venstre/højre mål: Opsætning, Adfærd, Opsæt strygehandlinger
* Skift adgangskode: Opsætning, trin 1, Håndtér, tryk på konto, skift adgangskode
* Opsæt en signatur: Opsætning, trin 2, tryk på identitet, Redigér signatur.
* Tilføj CC- og BCC-adresser: Tryk på folks ikon i slutningen af emnet
* Gå til næste/foregående besked ved arkivering/sletning: Deaktivér *Luk automatisk samtaler* i adfærdsindstillingerne og vælg *Gå til næste/foregående samtale* for *Ved lukning af en samtale*
* Føje en mappe til den fælles indbakke: Langt tryk på mappen i mappelisten og afkryds *Vis i fælles indbakke*
* Føje en mappe til navigeringsmenuen: Langt tryk på mappen i mappelisten og afkryds *Vis i navigeringsmenu*
* Indlæs flere beskeder: Langt tryk på en mappe på mappelisten, vælg *Synkronisér flere beskeder*
* Overspring papirkurv ved beskedsletning: I 3-priksmenuen lige over beskedteksten *Slet*, eller fravælg alternativt papirkurvsmappen i kontoindstillingerne
* Slet en konto/identitet: Opsætningstrin 1/2, Håndtér, tryk på konto/identitet, trepriksmenuen, Slet
* Slet en mappe: Langt tryk på mappen på mappelisten, Redigér egenskaber, trepriksmenuen, Slet
* Fortryd send: Udbakke, tryk på beskeden, tryk på Fortryd-ikonknappen
* Gemme sendte beskeder i indbakken: [Se denne FAQ](#user-content-faq142)
* Skift systemmapper: Opsætning, trin 1, Håndtér, tryk på konto nederst
* Eksport-/importindstillinger: Opsætning, navigering/hamburger-menu

## Kendte problemer

* ~~En [fejl i Android 5.1 og 6](https://issuetracker.google.com/issues/37054851) medfører, at apps sommetider viser et ukorrekt tidsformat. Skiftning af Android-indstillingen *Benyt 24-timers format* kan midlertidigt løse problemet. En løsning blev tilføjet.~~
* ~~En [fejl i Google Drive](https://issuetracker.google.com/issues/126362828) medfører, at filer eksporteret til Google Drive kan være tomme. Google har løst problemet.~~
* ~~En [fejl i AndroidX](https://issuetracker.google.com/issues/78495471) medfører, at FairEmail undertiden går ned ifm. lange tryk eller strygebevægelser. Google har løst problemet.~~
* ~~En [fejl i Android](https://issuetracker.google.com/issues/138441698) medfører af og til et nedbrud med en "*... Undtagelse under behandling af realtids databasedata ... Kunne ikke læse række...*". En løsning blev tilføjet.~~
* En [fejl i Android](https://issuetracker.google.com/issues/119872129) medfører, at FairEmail går ned med en "*... Fejlbehæftet notifikation sendt ... * "på visse enheder en gang efter opdatering af FairEmail og tryk på en notifikation.
* En [fejl i Android](https://issuetracker.google.com/issues/62427912) medfører af og til et nedbrud med "*... ActivityRecord ikke fundet for ... * "efter opdatering af FairEmail. Geninstallering af ([kilde](https://stackoverflow.com/questions/46309428/android-activitythread-reportsizeconfigurations-causes-app-to-freeze-with-black)) kan løse problemet.
* En [fejl i Android](https://issuetracker.google.com/issues/37018931) medfører af og til et nedbrud med "*... InputChannel ikke initialiseret ...* på visse enheder.
* ~~En [fejl i LineageOS](https://review.lineageos.org/c/LineageOS/android_frameworks_base/+/265273) medfører af og til et nedbrud med en "*... java.lang.ArrayIndexOutOfBoundsException: lenght=...; index=... ...*~~
* En fejl i Nova Launcher på Android 5.x får FairEmail til at gå ned med en *java.lang.StackOverflowError* fejl, når Nova Launcher har adgang til tilgængelighedstjenesten.
* ~~Mappevælgeren viser undertiden ingen mapper af endnu ukendte årsager. Dette lader til at være rettet.~~
* ~~En [fejl i AndroidX](https://issuetracker.google.com/issues/64729576) gør det svært at gribe fat i hurtig-rulningsobjektet. En løsning blev tilføjet.~~
* ~~Kryptering med YubiKey resulterer i en uendelig løkke. Dette synes forårsaget at en [fejl i OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2507).~~
* Rulning ned til en internt linket position i originalbeskeder fungerer ikke. Dette kan ikke rettes, da originalbeskedvisningen udgør en del af selve rulningsvisningen.
* En forhåndsvisning af en beskedtekst vises ikke (altid) på Samsung-ure, da [setLocalOnly](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder.html#setLocalOnly(boolean))-parameteren synes at blive ignoreret. Beskedforhåndsvisningstekster bliver vist korrekt på Pebble- 2, Fitbit Charge 3- og Mi band 3-wearables. Se også [denne FAQ](#user-content-faq126).

## Planlagte funktioner

* ~~Synkronisere efter behov (manuelt)~~
* ~~Semiautomatisk kryptering~~
* ~~Kopiere besked~~
* ~~Farvede stjerner~~
* ~~Notifikationsindstillinger pr. mappe~~
* ~~Vælg lokale billeder til underskrifter~~ (ingen af dem tilføjes, da dette kræver billedfilhåndtering, og da billedersom standard alligevel ikke vises i de fleste e-mail klienter)
* ~~Vis beskeder matchet af en regel~~
* ~~[ManageSieve](https://tools.ietf.org/html/rfc5804) ~ ~ (der er ingen vedligeholdte Java-biblioteker med en passende licens og uden afhængigheder, og desuden har FairEmail sine egne filterregler)
* ~~Søg efter beskeder med/uden vedhæftelser~~ (dette kan ikke tilføjes, da IMAP ikke understøtter søgning efter vedhæftninger)
* ~~Søg efter en mappe~~ (filtrering af en hierarkisk mappeliste er problematisk)
* ~~Søgeforslag~~
* ~~[Autokrypteringsopsætningsmeddelelse](https://autocrypt.org/autocrypt-spec-1.0.0.pdf) (afsnit 4.4)~~ (det kan ikke anbefales at lade en e-mail klient håndtere følsomme krypteringsnøgler til en usædvanlig brugsinstans, når OpenKeychain også kan eksportere nøgler)
* ~~Generiske fællesmapper~~
* ~~Nye pr. konto beskednotifikationstidsplaner~~ (implementeret ved tilføjelse af en tidsbetingelse til regler, så beskeder kan slumres i udvalgte perioder)
* ~~Kopiering af konti og identiteter~~
* ~~Knibezoom~~ (ikke muligt på pålidelig vis i en rulleliste, i stedet kan den fulde beskedvisning zoomes)
* ~~Mere kompakt mappevisning~~
* ~~Opret lister og tabeller~~ (dette kræver et tekstredigeringsværktøj til righoldig tekst, se [denne FAQ](#user-content-faq99))
* ~~Tekststørrelsesknibezoom~~
* ~~Vis GIF'er~~
* ~~Temaer~~ (et lysegråt og mørkt tema er tilføjet, da disse serud til at være dem, de fleste ønsker)
* ~~Enhver dag-tidsbetingelse~~ (enhver dag passer ikke rigtig ind i fra/til dato/tid betingelsen)
* ~~Send som vedhæftning~~
* ~~Widget til udvalgt konto~~
* ~~Påmindelse om at vedhæfte filer~~
* ~~Vælg domæner at vise billeder til~~ (dette vil være for kompliceret at bruge)
* ~~Fælles stjernemarkerede beskedervisning~~ (der er allerde en særlig søgestreng herfor)
* ~~Notifikation for flytningshandling~~
* ~~S/MIME-understøttelse~~
* ~~Søg efter indstillinger~~

Alt på denne liste er i tilfældig orden og *bliver muligvis* tilføjet i den nærmeste fremtid.

## Hyppigt anmodede funktioner

Designet er baseret på mange debatter, og er du interesseret, kan du også debatere det [i dette forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168). Designmålet er minimalisme (ingen unødvendige menuer, knapper mv.) og ikke-distraherende (ingen smarte farver, animationer mv.). Alle viste ting skal på en eller anden måde være nyttige og skal placeres omhyggeligt for nem brug. Skrifttyper/-størrelser, farver mv. skal være materielt design, når det er muligt.

## Ofte stillede spørgsmål

* [(1) Hvilke tilladelser kræves, og hvorfor?](#user-content-faq1)
* [(2) Hvorfor optræder der en permanent notifikation?](#user-content-faq2)
* [(3) Hvad er operationer, og hvad afventer de?](#user-content-faq3)
* [(4) Hvordan kan jeg anvende et ugyldigt sikkerhedscertifikat/tom adgangskode/simpel tekstforbindelse?](#user-content-faq4)
* [(5) Hvordan kan jeg tilpasse beskedvisningen?](#user-content-faq5)
* [(6) Hvordan kan jeg logge ind på Gmail/G-suite?](#user-content-faq6)
* [(7) Hvorfor fremgår sendte beskeder ikke (direkte) i Sendt-mappen?](#user-content-faq7)
* [(8) Kan jeg bruge en Microsoft Exchange-konto?](#user-content-faq8)
* [(9) Hvad er identiteter/hvordan tilføjer jeg et alias?](#user-content-faq9)
* [~~(11) Hvorfor understøttes POP ikke?~~](#user-content-faq11)
* [~~(10) Hvad betyder 'UIDPLUS ikke understøttet'?~~](#user-content-faq10)
* [(12) Hvordan fungerer kryptering/dekryptering?](#user-content-faq12)
* [(13) Hvordan fungerer søgning på enhed/server?](#user-content-faq13)
* [(14) Hvordan kan jeg opsætte en Outlook-/Live-/Hotmail-konto?](#user-content-faq14)
* [(15) Hvorfor genindlæses beskedteksten et antal gange?](#user-content-faq15)
* [(16) Hvorfor synkroniseres beskeder ikke?](#user-content-faq16)
* [~~(17) Hvorfor fungerer manuel synkronisering ikke?~~](#user-content-faq17)
* [(18) Hvorfor vises beskedforhåndsvisning ikke altid?](#user-content-faq18)
* [(19) Hvorfor er Pro-funktionerne så dyre?](#user-content-faq19)
* [(20) Kan jeg få pengene tilbage?](#user-content-faq20)
* [(21) Hvordan aktiverer jeg notifikationslyset?](#user-content-faq21)
* [(22) Hvad betyder konto-/mappefejl?](#user-content-faq22)
* [(23) Hvorfor ser jeg en advarsel .. ?](#user-content-faq23)
* [(24) Hvad er browsebeskeder på serveren?](#user-content-faq24)
* [(25) Hvorfor kan jeg ikke vælge/åbne/gemme et billede, vedhæftning ellerr en fil?](#user-content-faq25)
* [(26) Kan jeg hjælpe med at oversætte FairEmail til mit eget sprog?](#user-content-faq26)
* [(27) Hvordan skelnes mellem indlejrede og eksterne billeder?](#user-content-faq27)
* [(28) Hvordan kan jeg håndtere statusbjælkenotifikationer?](#user-content-faq28)
* [(29) Hvordan kan jeg få beskednotifikationer for andre mapper?](#user-content-faq29)
* [(30) Hvordan anvender jeg de tilgængelige hurtig indstillinger?](#user-content-faq30)
* [(31) Hvordan anvender jeg de tilgængelige genveje?](#user-content-faq31)
* [(32) Hvordan tjekker jeg, om det virkelig er sikkert at læse e-mail?](#user-content-faq32)
* [(33) Hvorfor fungerer redigerede afsenderadresser ikke?](#user-content-faq33)
* [(34) Hvordan matches identiteter?](#user-content-faq34)
* [(35) Hvorfor skal man være forsigtig med at få vist billeder, vedhæftninger og den oprindelige besked?](#user-content-faq35)
* [(36) Hvordan krypteres indstillingsfiler?](#user-content-faq36)
* [(37) Hvordan opbevares adgangskoder?](#user-content-faq37)
* [(39) Hvordan kan jeg reducere FairEmails batteriforbrug?](#user-content-faq39)
* [(40) Hvordan kan jeg reducere FairEmails netværksforbrug?](#user-content-faq40)
* [(41) Hvordan retter jeg fejlen 'Handshake miskykkedes' ?](#user-content-faq41)
* [(42) Kan en ny udbyder føjes til listen over udbydere?](#user-content-faq42)
* [(43) Kan man vise den oprindelige ... ?](#user-content-faq43)
* [(44) Kan man vise kontaktfotos/identikoner i Sendt-mappen?](#user-content-faq44)
* [(45) Hvordan retter jeg problemet 'Denne nøgle er utilgængelig'? To use it, you must import it as one of your own!' ?](#user-content-faq45)
* [(46) Hvorfor opfriskes beskedlisten hele tiden?](#user-content-faq46)
* [(47) Hvordan løser jeg problemet 'Ingen primær konto eller ingen udkastmappe'?](#user-content-faq47)
* [~~(48) Hvordan løser jeg problemet 'Ingen primær konto eller ingen arkivmappe'?~~](#user-content-faq48)
* [(49) Hvordan løser jeg problemet 'En forældet app har sendt en filsti i stedet for en fil-stream'?](#user-content-faq49)
* [(50) Kan der tilføjes en mulighed for synkronisering af alle beskeder?](#user-content-faq50)
* [(51) Hvordan bliver mapper sorteret?](#user-content-faq51)
* [(52) Hvorfor tager det noget tid at gentilslutte en konto?](#user-content-faq52)
* [(53) Kan man fastgøre beskedhandlingsbjælken øverst/nederst?](#user-content-faq53)
* [~~(54) Hvodan bruges et navneområdepræfiks?~~](#user-content-faq54)
* [(55) Hvordan kan alle beskeder markeres som læst, flyttes eller slettes?](#user-content-faq55)
* [(56) Kan der tilføjes understøttelse af JMAP?](#user-content-faq56)
* [~~(57) Kan HTML anvendes i signaturer?~~](#user-content-faq57)
* [(58) Hvad betyder et åbent/lukket e-mail ikon?](#user-content-faq58)
* [(59) Kan originalbeskeder åbnes i browseren?](#user-content-faq59)
* [(60) Vidste du, at...?](#user-content-faq60)
* [(61) Hvorfor vises visse beskeder nedtonet?](#user-content-faq61)
* [(62) Hvilke godkendelsesmetoder understøttes?](#user-content-faq62)
* [(63) Hvordan skaleres billeder til visning på skærme?](#user-content-faq63)
* [~~(64) Kan der tilføjes tilpassede handlinger for venstre/højre strygning?~~](#user-content-faq64)
* [(65) Hvorfor vises visse vedhæftninger nedtonet?](#user-content-faq65)
* [(66) Er FairEmail tilgængelig i Google Play Familie-biblioteket?](#user-content-faq66)
* [(67) Hvordan slumres samtaler?](#user-content-faq67)
* [~~(68) Hvorfor kan Adobe Acrobat Reader/Microsoft-apps ikke åbne PDF-vedhæftninger/vedhæftede dokumenter?~~](#user-content-faq68)
* [(69) Kan man føje autorulning opad til nye beskeder?](#user-content-faq69)
* [(70) Hvornår bliver alle beskeder auto-udfoldet?](#user-content-faq70)
* [(71) Hvordan benyttes filterregler?](#user-content-faq71)
* [(72) Hvad er primære konti/identiteter?](#user-content-faq72)
* [(73) Er flytning af beskeder til andre konti sikkert/effektivt?](#user-content-faq73)
* [(74) Hvorfor optræder der dubletbeskeder?](#user-content-faq74)
* [(75) Kan der laves en version til Windows, Linux, iOS mv.?](#user-content-faq75)
* [(76) Hvad betyder 'Ryd lokale beskeder'?](#user-content-faq76)
* [(77) Hvorfor vises beskeder af og til med en lille forsinkelse?](#user-content-faq77)
* [(78) Hvordan benyttes tidsplaner?](#user-content-faq78)
* [(79) Hvordan synkroniseres efter behov (manuelt)?](#user-content-faq79)
* [~~(80) Hvordan løses fejlen 'Indlæsning af BODYSTRUCTURE mislykkedes'?~~](#user-content-faq80)
* [~~(81) Kan man gøre baggrunden på originalbeskeder mørk i det mørke tema?~~](#user-content-faq81)
* [(82) Hvad er et sporingsbillede?](#user-content-faq82)
* [(84) Hvad benyttes lokale kontakter til?](#user-content-faq84)
* [(85) Hvorfor er en identitet utilgængelig?](#user-content-faq85)
* [~~(86) Hvad er ekstra fortrolighedsfunktioner'?~~](#user-content-faq86)
* [(87) Hvad betyder 'ugyldige akkreditiver'?](#user-content-faq87)
* [(88) Hvordan benytter jeg en Yahoo-, AOL- ellerr Sky-konto?](#user-content-faq88)
* [(89) Hvordan sendes beskeder kun med simpelt tekstindhold?](#user-content-faq89)
* [(90) Hvorfor er visse tekster linkede uden at være et link?](#user-content-faq90)
* [~~(91) Kan der tilføjes periodisk synkronisation for at spare på batteriet?~~](#user-content-faq91)
* [(92) Kunne der tilføjes spamfiltrering, DKIM-signaturbekræftelse og SPF-godkendelse?](#user-content-faq92)
* [(93) Kan der tillades installation/datalagring på eksternt lagermedie (SD-kort)?](#user-content-faq93)
* [(94) Hvad betyder den røde/orange stribe for enden af overskriften?](#user-content-faq94)
* [(95) Hvorfor vises alle apps ikke ved valg af en vedhæftning eller billede?](#user-content-faq95)
* [(96) Hvor finder man IMAP- og SMTP-indstillingerne?](#user-content-faq96)
* [(97) Hvad vil 'oprydning' sige?](#user-content-faq97)
* [(98) Hvorfor kan kontakter stadig vælges efter tilbagekaldelse af kontakter-tilladelser?](#user-content-faq98)
* [(99) Kan der tilføjes et redigeringsværktøj til righoldig tekst eller markdown?](#user-content-faq99)
* [(100) Hvordan synkroniseres Gmail-kategorier?](#user-content-faq100)
* [(101) Hvad betyder den blå/orange prik i bunden af samtalen?](#user-content-faq101)
* [(102) Hvordan aktiveres autorotation af billeder?](#user-content-faq102)
* [(103) Hvordan optager man lyd?](#user-content-faq103)
* [(104) Hvad er nødvendigt at vide om fejlrapportering?](#user-content-faq104)
* [(105) Hvordan fungere indstillingen roam-som-på-hjemadressen?](#user-content-faq105)
* [(106) Hvilke launchere kan vise et badge-tal for antallet af ulæste beskeder?](#user-content-faq106)
* [(107) Hvordan anvendes farvede stjerner?](#user-content-faq107)
* [(108) Kan der tilføjes permanent slettede beskeder fra enhver mappe?](#user-content-faq108)
* [~~(109) Hvorfor er 'vælg konto' kun tilgængelig i officielle versioner?~~](#user-content-faq109)
* [(110) Hvorfor er (nogle) beskeder tomme og/eller vedhæftninger ødelagte?](#user-content-faq110)
* [(111) Er OAuth understøttet?](#user-content-faq111)
* [(112) Hvilke e-mailudbydere kan anbefales?](#user-content-faq112)
* [(113) Hvordan fungerer biometrisk godkendelse?](#user-content-faq113)
* [(114) Kan der tilføjes en importmulighed til indstillinger fra andre e-mail apps?](#user-content-faq114)
* [(115) Kan der tilføjes e-mailadress chips?](#user-content-faq115)
* [~~(116) Hvordan kan der som standard vises billeder i beskeder fra betroede afsendere?~~](#user-content-faq116)
* [(117) Kan man få hjælp til gendannelse af et køb?](#user-content-faq117)
* [(118) Hvad gør 'Fjern tracking-parametre' mere præcist?](#user-content-faq118)
* [~~(119) Kan der tilføjes farver til den fælles indbakke-widget?~~](#user-content-faq119)
* [(120) Hvorfor fjernes nye beskednotifikationer ikke, når appen startes?](#user-content-faq120)
* [(121) Hvordan grupperes beskeder ind i en samtale?](#user-content-faq121)
* [~~(122) Hvorfor vises modtagernavnet/e-mailadressen med en advarselsfarve?~~](#user-content-faq122)
* [(123) Hvad sker der, når FairEmail ikke kan oprette forbindelse til en e-mailserver?](#user-content-faq123)
* [(124) Hvorfor ses meddelelsen 'Besked for stor eller kompleks at vise'?](#user-content-faq124)
* [(125) Hvad udgør pt. de eksperimentelle funtioner?](#user-content-faq125)
* [(126) Kan beskedforhåndsvisninger sendes til en wearable?](#user-content-faq126)
* [(127) Hvordan rettes fejlen 'Syntaktisk ugyldig HELO-argument(er)'?](#user-content-faq127)
* [(128) Hvordan nulstilles afgivne spørgsmål, f.eks. for at vise billeder?](#user-content-faq128)
* [(129) Understøttes ProtonMail og Tutanota?](#user-content-faq129)
* [(130) Hvad betyder beskedfejl...?](#user-content-faq130)
* [(131) Kan man ændre retning for strygning til foregånde/næste besked?](#user-content-faq131)
* [(132) Hvorfor er notifikationer om nye beskeder tavse?](#user-content-faq132)
* [(133) Hvorfor er ActiveSync uunderstøttet?](#user-content-faq133)
* [(134) Kan der tilføjes sletning af lokale beskeder?](#user-content-faq134)
* [(135) Hvorfor vises kasserede beskeder og udkast i samtaler?](#user-content-faq135)
* [(136) Hvordan slettes en konto/identitet/mappe?](#user-content-faq136)
* [(137) Hvordan nulstilles 'Spørg ikke igen'?](#user-content-faq137)
* [(138) Kan der tilføjes kalender-/kontakthåndtering/-synkronisering?](#user-content-faq138)
* [(139) Hvordan løses fejlmeddelelsen 'Bruger er godkendt, men ikke tilsluttet'?](#user-content-faq139)
* [(140) Hvorfor indeholder beskedteksten underlige tegn?](#user-content-faq140)
* [(141) Hvordan løses 'En Udkast-mappe kræves for at sende beskeder' fejlmeddelelsen?](#user-content-faq141)
* [(142) Hvordan kan jeg gemme afsendte beskeder i indbakken?](#user-content-faq142)
* [~~(143) Kunne der tilføjes en papirkurvmappe til POP3-konti?~~](#user-content-faq143)
* [(144) Hvordan kan jeg optage stemmenotater?](#user-content-faq144)
* [(145) Hvordan indstilles en notifikationslyd til en konto, mappe eller afsender?](#user-content-faq145)
* [(146) Hvordan løses problemet med forkerte beskedklokkeslæt?](#user-content-faq146)
* [(147) Hvad bør jeg vide om tredjepartsversioner?](#user-content-faq147)
* [(148) Hvordan anvender man en Apple iCloud-konto?](#user-content-faq148)
* [(149) Hvordan fungerer widget'en for ulæst beskedantal?](#user-content-faq149)
* [(150) Kan der tilføjes annullering af kalenderinvitationer?](#user-content-faq150)
* [(151) Kan der tilføjes sikkerhedskopiering/gendannelse af beskeder?](#user-content-faq151)
* [(152) Hvordan indsættes en kontaktgrouppe?](#user-content-faq152)
* [(153) Hvorfor fungerer permanent sletning af Gmail-beskeder ikke?](#user-content-faq153)
* [~~(154) Kan fav-ikonr som kontaktfotos tilføjes?~~](#user-content-faq154)
* [(155) Hvad er en winmail.dat-fil?](#user-content-faq155)
* [(156) Hvordan opsættes en Office365-konto?](#user-content-faq156)
* [(157) Hvordan opsættes en Free.fr-konto?](#user-content-faq157)
* [(158) Hvilken kamera-/lydoptager anbefales?](#user-content-faq158)
* [(159) Hvad er Disconnect's sporingsbeskyttelseslister?](#user-content-faq159)

[Jeg har et andet spørgsmål.](#user-content-support)

<a name="faq1"></a>
**(1) Hvilke tilladelser kræves, og hvorfor?**

Flg. Android-tilladelser kræves:

* *fuld netværksadgang* (INTERNET): For at sende/modtage e-mails
* *se netværksforbindelser* (ACCESS_NETWORK_STATE): For at monitere Internetkonnektivitetsændringer
* *kør ved opstart* (RECEIVE_BOOT_COMPLETED): For at starte monitering ved enhedsstart
* *forgrundstjeneste* (FOREGROUND_SERVICE): For at køre en forgrundstjeneste på Android 9 Pie og senere, se også næste spørgsmål
* *forhindre enhed i at sove* (WAKE_LOCK): For at holde enheden vågen, mens beskeder synkroniseres
* *in-app fakturering* (BILLING): For at tillade køb direkte i appen
* Valgfri: *læse dine kontakter* (READ_CONTACTS): For at autofuldføre adresser samt vise fotos
* Valgfri: *læse indholdet af dit SD-kort* (READ_EXTERNAL_STORAGE): For at acceptere filer fra andre, forældede apps, se også [denne FAQ](#user-content-faq49)
* Valgfri: *benyt fingeraftrykshardware* (USE_FINGERPRINT) og benyt *biometrisk hardware* (USE_BIOMETRIC): For at benytte biometrisk godkendelse
* Valgfri: *find konti på enheden* (GET_ACCOUNTS): For at vælge en konto ifm. Gmails hurtig-opsætning
* Android 5.1 Lollipop og ældre: *Benyt konti på enheden* (USE_CREDENTIALS): For at vælge en konto ifm. Gmails hurtig-opsætning (senere Android-version benytter ikke denne forespørgsel)
* Android 5.1 Lollipop og ældre: *Læse profil* (READ_PROFILE): For at læse dit navn ifm. Gmails hurtig-opsætning (senere Android-version benytter ikke denne forespørgsel)

[Valgfrie tilladelser](https://developer.android.com/training/permissions/requesting) understøttes kun på Android 6 Marshmallow og nyere. Tidligere Android-versioner anmoder om at tildele de valgfri tilladelser ved installation af FairEmail.

Flg. tilladelser kræves for at vise antallet af ulæste beskeder som en badge (se også [denne FAQ](#user-content-faq106)):

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

FairEmail fører en liste over adresser, hvorfra og -til du modtager og sender beskeder, og denne liste benytter til kontaktforslag, når der ikke er tildelt kontakttilladelse i FairEmail. Dette betyder, at du kan benytte FairEmail uden Android-kontaktleverandøren (Kontakter). Bemærk, at du stadig kan vælge kontakter uden at tildele FairEmail kontakter-tilladelser, kontaktforslag alene fungerer dog ikke uden kontakter-tilladelser.

<br />

<a name="faq2"></a>
**(2) Hvorfor vises en permanent notifikation?**

En lavprioritets, permanent statusbjælkenotifikation med antallet af konti, som monitoreres, samt antallet af afventede operationer (se næste spørgsmål) vises for at forhindre Android i at afslutte den tjeneste, der håndterer den løbende modtagelse af e-mail. Dette var [allerede nødvendigt](https://developer.android.com/reference/android/app/Service.html#startForeground(int,%20android.app.Notification)), men med introduktionen af [døsningstilstanden](https://developer.android.com/training/monitoring-device-state/doze-standby) i Android 6 Marshmallow er dette mere end nogensinde nødvendigt. Døsningstilstanden stopper alle apps nogen tid efter, at skærmen slukkes, undtagen for apps, som har startet en forgrundstjeneste, der kræver visning af en statusbjælkenotifikation.

De fleste, hvis ikke alle, øvrige e-mail apps viser ingen notifikation med den "bivirkning", at nye notifikationer ofte ikke (eller for sent) rapporteres, samt at notifikationer ikke sendes (eller sendes for sent).

Android viser ikoner for højprioritets statusnotifikationer først og skjuler FairEmails notifikationsikon, hvis der ikke er plads til at vise yderligere ikoner. I praksis betyder dette, at statusbjælkenotifikationer ikke optager plads på statusbjælken, medmindre der er ledig plads.

Statusnjælkenotifikationen kan deaktiveres via FairEmails notifikationsindstillinger:

* Android 8 Oreo og senere: Tryk på knappen *Tjenestekanal* og deaktivér notifikationskanalen via Android-indstillingerne
* Android 7 Nougat og tidligere: Aktivér *Benyt baggrundstjenester til synkronisering af beskeder*, men husk at læse bemærkningen under indstillingen

For at fjerne notifikationen kan du kan skifte til periodisk beskedsynkronisering i modtagelsesindstillingerne, dog vil dette muligvis forbruger mere strøm. Se [hér](#user-content-faq39) for flere detailjer vedr. batteriforbrug.

Android 8 Oreo viser muæigvis også en statusbjælkenotifikation med teksten *Apps kører i baggrunden*. Tjek [hér](https://www.reddit.com/r/Android/comments/7vw7l4/psa_turn_off_background_apps_notification/) vedr., hvordan du kan deaktivere denne notifikation.

Anvendelse af [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/) (FCM) er foreslået i stedet for en Android-tjeneste med en statusbjælkenotifikation, men dette vil enten kræve e-mail udbydere, som sender FCM-meddelelser, eller en central server, hvor alle meddelelser samles mhp. afsendlse af FCM-meddelelser. Førstnævnte vil ikke komme til at ske, og sidstnævnte ville have betydelige datafortrolighedskonsekvenser.

Er du kommet hertil via et klik på notifikationen, så vil næste klik åbne den fælles indbakke.

<br />

<a name="faq3"></a>
**(3) Hvad er operationer, og hvorfor afventer de?**

Lavprioritets statusbjælkenotifikationen viser antallet af afventende operationer, som kan være:

* *tilføj*: Føj besked til fjernmappe
* *flyt*: Flyt besked til en anden fjernmappe
* *kopiér*: Kopiér besked til en anden fjernmappe
* *hent*: Hent ændret (pushed) besked
* *slet*: Slet besked i fjernmappe
* *set*: Markér beskeder som læste/ulæste i fjernmappe
* *besvaret*: Markér beskeder som besvaret i fjernmappe
* *stjernemarker*: Tilføj/fjern stjerne i fjernmappe
* *nøgleord*: Tilføj/fjern IMAP-markering i fjernmappe
* *etiket*: Sæt/nulstil Gmail-etiket i fjernmappe
* *overskrifter*: Download beskedoverskrifter
* *råformat*: Download beskeder i råformat
* *brødtekst*: Download beskedindholdstekst
* *vedhæftning*: Download vedhæftning
* *synk*: Synk lokal- og fjernbeskeder
* *abonnér*: Abonnér på fjernmappe
* *udrens*: Slet alle beskeder fra fjernmappen
* *send*: Send besked
* *findes*: Tjek om besked findes
* *regel*: Eksekvér regel på brødtekst

Operationer behandles kun, såfremt en forbindelse til e-mailserveren findes, eller under manuel synkronisering. Se også [denne FAQ](#user-content-faq16).

<br />

<a name="faq4"></a>
**(4) Hvordan kan jeg benytte et ugyldigt sikkerhedscertifikat/tom adgangskode/simpel tekstforbindelse?**

*... Ikke-betroet ... ikke i certifikatet ...*
<br />
*... Ugyldigt sikkerhedscertifikat (kan ikke bekræfte identet på server)...*

Du bør forsøge at løse dette ved at kontakte din udbyder eller ved at få et gyldigt sikkerhedscertifikat, da ugyldige sikkerhedscertifikater er usikre og tillader [mand-i-midten-angreb](https://en.wikipedia.org/wiki/Man-in-the-middle_attack). Er penge en hindring, kan du få gratis sikkerhedscertifikater fra [Let's Encrypt](https://letsencrypt.org).

Alternativt kan du acceptere fingeraftrykket vist under fejlmeddelelsen, hvis du opsætter kontoen og/eller identiteten i opsætningstrin 1 og 2 (dette er ikke muligt, når du benytter hurtigopsætningsguiden). Bemærk, at du bør sikre dig, at den anvendte Internetforbindelse er sikker.

Bemærk, at ældre Android-versioner muligvis ikke genkender nyere certificeringsmyndigheder såsom Let’s Encrypt, hvorfor forbindelser kan blive betragtet som usikre, se også [hér](https://developer.android.com/training/articles/security-ssl).

*Trust anchor til certificeringsstien ikke fundet*

*... java.security.cert.CertPathValidatorException: Trust anchor til certificeringsstien ikke fundet...* betyder, at Androids standard trust manager ikke var i stand til at bekræfte servercertifikatkæden.

Du bør enten rette serveropsætningen eller acceptere fingeraftrykket vist neden for fejlmeddelelsen.

Bemærk, at dette problem kan skyldes af, at serveren ikke sender alle mellemliggende certifikater.

*Kryptere adgangskode*

Dit brugernavn er sandsynligvis let at gætte og er dermed ikke sikkert.

*Simpel tekst-forbindelse*

Dit brugernavn og adgangskode samt alle beskeder sendes og modtages ukrypteret, hvilket er **meget usikkert**, da et [mand-i-midten angreb](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) nemt udføres på en ikke-krypteret forbindelse.

Vil du fortsat bruge et ugyldigt sikkerhedscertifikat, en tom adgangskode eller en almindelig tekstforbindelse, så skal usikre forbindelser aktiveres i konto- og/eller identitetsindstillingerne. STARTTLS bør vælges til simpel tekst-forbindelser. Aktiverer du usikre forbindelser, bør du kun oprette forbindelse via private, betroede netværk og aldrig via offentlige netværk på f.eks. hoteller, lufthavne mv.

<br />

<a name="faq5"></a>
**(5) Hvordan tilpasses beskedvisningen?**

I menuen med tre prikker kan du aktivere/deaktivere eller vælge:

* *tekststørrelse*: Til tre forskellige skriftstørrelser
* *kompakt visning*: Til flere kondenserede beskedelementer og en mindre beskedtekst skrifttype

I visningsafsnittet i indstillingerne kan du aktivere eller deaktivere:

* *Fælles indbakke*: For at deaktivere den fælles indbakke og i stedet vise de valgte mapper fra den fælles indbakke
* *Gruppér efter dato*: Vis datooverskrift over beskeder med den samme dato
* *Samtaletråd*: For at deaktivere samtaletråd og i stedet vise beskeder individuelt
* *Vis kontaktfotos*: For at skjule kontaktfotos
* *Vis identikoner*: Til visning af genererede kontaktavatarer
* *Vis navne og e-mailadresser*: For at navne/navne og e-mailadresser
* *Vis emne uden kursiv*: For at vise beskedemnet som alm. tekst
* *Vis stjerner*: For at skjule stjerner (favoritter)
* *Vis beskedforhåndsvisning*: For at vise to linjers beskedtekst
* *Vis som standard adresseoplysninger*: Vis som standard udvidet adresseafsnit
* *Benyt monospatieret skrifttype til beskedtekst *: For brug af en skrifttype med fast bredde til beskedtekster
* *Vis automatisk opridelig besked for kendte kontakter*: Se venligst [denne FAQ](#user-content-faq35) for automatisk at få vist originale beskeder for kontakter på din enhed
* *Vis automatisk billeder for kendte kontakter*: Se venligst [denne FAQ](#user-content-faq35) for automatisk at få vist billeder for kontakter på din enhed
* *Samtalehandlingsbjælke*: For at deaktivere navigeringsbjælken nederst

Bemærk, at beskeder kun kan forhåndsvises, når beeskedteksten er blevet downloadet. Større beskedtekster downloades som standard ikke på afregnede (hovedsaligt mobile) netværk. Du kan ændre dette i indstillingerne.

Er listen over adresser lang, kan du sammenfolde adresseafsnittet vha. *mindre*-ikonet øverst i adresseseafsnittet.

Nogle har bedt om:

* at få emnet vist med fed tekst, men fed benyttes allerede til fremhævelse af ulæste beskeder
* at få adresse- eller emnevisningen gjort større/mindre, men dette vil forstyrre indstillingen for tekststørrelse
* at få stjernen flyttet til venstre, men det er meget lettere at betjene stjernen på højre side

Desværre er det umuligt at stille alle tilpas, og at tilføje mange indstillinger ville ikke kun være forvirrende, men heller aldrig være tilstrækkeligt.

<br />

<a name="faq6"></a>
**(6) Hvordan logges ind på Gmail/G suite?**

Du kan benytte hurtig opsætnings-guiden til nemt at opsætte en Gmail-konto og -identitet.

Vil du ikke benytte en Gmail-konto på enheden, kan du enten aktivere adgang til "mindre sikre apps" og benytte din kontoadgangskode eller aktivere tofaktorgodkendelse og benytte en app-specifik adgangskode. Se [denne FAQ](#user-content-faq111) om, hvorfor kun konti på enheden kan benyttes.

Bemærk, at en app-specifik adgangskode kræves, når tofaktorgodkendelse er aktiveret.

<br />

*App-specifik adgangskode*

Se [hér](https://support.google.com/accounts/answer/185833), hvordan en app-specifik adgangskode genereres.

<br />

*Aktivér "Mindre sikre apps"*

**Vigtigt**: Brug af denne metode anbefales ikke, da den er mindre pålidelig.

**Vigtigt**: Brugernavn/adgangskode godkendte Gsuite-konti vil [i nærmeste fremtid](https://gsuiteupdates.googleblog.com/2019/12/less-secure-apps-oauth-google-username-password-incorrect.html) ophøre med at fungere.

Se [hér](https://support.google.com/accounts/answer/6010255), hvordan "mindre sikre apps"aktiveres eller gå [direkte til Indstillinger](https://www.google.com/settings/security/lesssecureapps).

Benytter du flere Gmail-konti, så sørg for at ændre indstillingen "mindre sikre apps" på de korrekte konti.

Bemærk, at du er nødt til at forlade "mindre sikre apps"-indstillingsskærmen vha. Tilbage-pilen, for at effektuere indstillingen.

Benytter du denne metode, bør du anvende en [stærk adgangskode](https://en.wikipedia.org/wiki/Password_strength) til din Gmail-konto, hvilket i øvrigt altid er en god idé. Bemærk, at brug af [standard](https://tools.ietf.org/html/rfc3501) IMAP-protokollen ikke i sig selv er mindre sikker.

Når "mindre sikre apps" ikke er aktiveret, får du fejlen *Godkendelse mislykkedes - ugyldige akkreditiver* for konti (IMAP) og *Brugernavn og adgangskode ikke accepteret* for identiteter (SMTP).

<br />

*Generelt*

Du får muligvis advarslen "*Log ind via din webbrowser*". Dette sker, når Google anser det netværk, hvormed du forbinder til Internet (hvilket kan være et VPN), som ikke-sikkert. Dette kan forhindres vha. Gmails hurtig opsætningsguide eller en app-specifik adgangskode.

Se [hér](https://support.google.com/mail/answer/7126229) for Googles instruktioner, og [hér](https://support.google.com/mail/accounts/answer/78754) for fejlfinding.

<br />

<a name="faq7"></a>
**(7) Hvorfor vises sendte beskeder ikke (direkte) i Sendt-mappen?**

Sendte beskeder flyttes normalt fra udbakken til Sendt-mappen, så snart din udbyder føjer sendte beskeder til Sendt-mappen. Dette kræver, at en Sendt-mappe vælges i kontoindstillingerne, og at Sendt-mappen ligeledes opsættes til synkronisering.

Visse udbydere holder ikke styr på sendte beskeder, eller den anvendte SMTP-server er muligvis ikke relateret til udbyderen. I så tilfælde tilføjer FairEmail automatisk sendte beskeder til Sendt-mappen unerd synkronisering af denne, hvilket vil ske, efter at en besked er afsendt. Bemærk, at dette vil forøge Internettrafikken.

~~Sker dette ikke, holder din udbyder muligvis ikke styr på sendte beskeder, eller du anvender muligvis en ikke-udbyderrelateret SMTP-server.~~ ~~I så tilfælde kan du aktivere den avancerede identitetsindstilling *Gem sendte beskeder* for at lade FairEmail føje sendte beskeder til Sendt-mappen umiddelbart efter afsendelse heraf.~~ ~~Bemærk, at aktivering af denne indstilling kan resultere i dubletbeskeder, hvis din udbyder føjer sendte beskeder til Sendt-mappen.~~ ~~Bemærk også, at aktivering af indstillingen vil resultere i forøget dataforbrug, især når du sender beskeder med store vedhæftninger.~~

~~Hvis sendte beskeder i udbakken ikke findes i Sendt-mappen ved en fuld synkronisering, flyttes disse også fra udbakken til Sendt-mappen.~~ ~~En fuld synkronisering sker, når der genforbindes til serveren, eller ved periodisk eller manuel synkronisering.~~ ~~Du ønsker sandsynligvis i stedet at aktivere den avancerede indstilling *Gem sendte beskeder* for at flytte beskeder til Sendt-mappen hurtigere.~~

<br />

<a name="faq8"></a>
**(8) Kan en Microsoft Exchange-konto benyttes?**

En Microsoft Exchange-konto kan benyttes, hvis den er tilgængelig via IMAP, hvilket plejer at være tilfældet. Se [hér](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793) for yderligere information.

Se [hér](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) for Microsoft-dokumentation vedr. opsætning af en email klient. Der er også et afsnit om almindelige forbindelsesfejl og løsninger heraf.

Visse ældre Exchange-serverversioner har en fejl, der forårsager tomme beskeder og ødelagte vedhæftninger. Se [denne FAQ](#user-content-faq110) for en løsning.

Se [denne FAQ](#user-content-faq133) vedr. ActiveSync-understøttelse.

Se [denne FAQ](#user-content-faq111) vedr. OAuth-understøttelse.

<br />

<a name="faq9"></a>
**(9) Hvad er identiteter/hvordan tilføjes et alias?**

Identiteter repræsenterer e-mailadresser, du sender *fra* via en e-mailserver (SMTP).

Visse udbydere tillader brug af flere aliaser. Du kan opsætte disse ved at indstille e-mailadressefeltet fra en yderliger identitet til aliasadressen og indstille brugernavnefeltet til din hoved e-mailadresse.

Bemærk, at en identitet kan kopieres vha. et langt tryk på den.

Alternativt kan *Tillad redigering af afsenderadresse* aktiveres i de avancerede indstillinger for en eksisterende identitet for at redigere brugernavnet, når du skriver en ny besked, forudsat din udbyder tillader dette.

FairEmail opdaterer automatisk adgangskoder til relaterede identiteter, når adgangskoden til den tilknyttede konto/relateret identitet opdateres.

Se [denne FAQ](#user-content-faq33) om redigering af brugernavnet til e-mailadresser.

<br />

<a name="faq10"></a>
**~~(10) Hvad betyder 'UIDPLUS ikke understøttet'?~~**

~~Fejlmeddelelsen *UIDPLUS ikke understøttet * betyder, at din e-mailudbyder ikke tilbyder IMAP [UIDPLUS-udvidelsen](https://tools.ietf.org/html/rfc4315). Denne IMAP-udvidelse kræves for at implementere tovejssynkronisering, der ikke er en valgfri funktion. Så medmindre din udbyder kan aktivere denne udvidelse, kan du ikke benytte FairEmail til denne udbyder.~~

<br />

<a name="faq11"></a>
**~~(11) Hvorfor understøttes POP ikke'?~~**

~~Udover at enhver anstændig e-mail-udbyder understøtter [IMAP](https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol) i dag,~~ ~~vil brug af [POP](https://en.wikipedia.org/wiki/Post_Office_Protocol) resultere i unødvendig ekstra batteriforbrug samt forsinkede notifikationer om nye beskeder.~~ ~~Desuden er POP uegnet til tovejssynkronisering, og oftere end ikke, læser og skriver folk beskeder på forskellige enheder i dag.~~

~~Grundlæggende understøtter POP kun download og sletning af beskeder fra indbakken.~~ ~~Dvs., at almindelige operationer, såsom indstilling af besekedattributter (læst, stjernemarkeret, besvaret mv.), tilføjelse (sikkerhedskopiering) og flytning af beskeder ikke er mulige.~~

~~Se også, [hvad Google skriver om det](https://support.google.com/mail/answer/7104828).~~

~~F.eks. kan [Gmail importere beskeder](https://support.google.com/mail/answer/21289) fra en anden POP-konto,~~ ~~hvilket kan anvendes som en løsning, når din udbyder ikke understøtter IMAP.~~

~~tl;dr; overvej at skifte til IMAP.~~

<br />

<a name="faq12"></a>
**(12) Hvordan fungerer kryptering/dekryptering?**

*Generelt*

[Se hér](https://en.wikipedia.org/wiki/Public-key_cryptography), hvordan kryptering vha. offentlige/private nøgler fungerer.

Kryptering kort fortalt:

* **Udgående** beskeder krypteres vha. modtagerens **offentlige nøgle**
* **Indgående** beskeder dekrypteres vha. modtagerens **private nøgle**

Signering kort fortalt:

* **Udgående** beskeder signeres med afsenderens **private nøgle**
* **Indgående** beskeder bekræftes vha. afsenderens **offentlige nøgle**

For at signere/kryptere en besked, vælg den passende metode i Send-dialogen. Har du tidliger valgt *Vis ikke igen*, kan du altid åbne Send-dialogen vha. trepunktsmenuen.

For at bekræfte en signatur eller dekryptere en modtaget besked, så åbn beskeden og tryk blot på gestussen eller på hængelåsikonet umiddelbart under beskedhandlingsbjælken.

Første gang du sender en signeret/krypteret besked, bliver du muligvis bedt om en signaturnøgle. Mhp. efterfølgende brug gemmer FairEmail automatisk den valgte signeringsnøgle i den anvendte identitet. Har du behov for at nulstille signeringsnøglen, så gem blot identiteten eller brug et langt tryk på identiteten på identitetslisten og vælg *Nulstil signeringsnøgle*. Den valgte signeringsnøgle er synlig på identitetslisten. Skal en nøgle vælges en nøgle fra gang til gang, kan du oprette flere identiteter for den samme konto med den samme e-mailadresse.

I fortrolighedsindstillingerne kan du vælge standardkrypteringsmetoden (PGP eller S/MIME), aktivere *Signér som standard*, *Kryptér som standard* samt *Dekryptér automatisk beskeder*, men vær opmærksom på, at automatisk dekryptering ikke er mulig, hvis brugerinteraktion kræves, såsom valg af en nøgle eller læsning af et sikkerhedstoken.

Beskedtekster/vedhæftninger, som skal krypteres, samt dekrypterede beskedtekster/vedhæftninger gemmes kun lokalt og vil aldrig blive tilføjet til den eksterne server. Vil du fortryde dekryptering, kan du benytte menupunktet *gensynk* fra trepunktsmenuen på beskedhandlingsbjælken.

*PGP*

Du skal først installere og opsætte [OpenKeychain](https://f-droid.org/en/packages/org.sufficientlysecure.keychain/). FairEmail er aftestet med OpenKeychain version 5.4. Senere versioner er højest sansynlig kompatible, men tidligere versioner er muligvis ikke.

**Vigtigt**: OpenKeychain-appen er kendt for at gå ned (upåagtet), når den kaldende app (FairEmail) endnu ikke er godkendt og får en eksisterende offentlig nøgle. Du kan løse dette ved at prøve at sende en signeret/krypteret besked til en afsender med en ukendt offentlig nøgle.

**Vigtigt**: Kan OpenKeychain-appen ikke finde en nøgle (længere), skal du muligvis nulstille en tidligere valgt nøgle. Dette kan gøres via et langt tryk på en identitet i identitetsoversigten (Opsætning, trin 2, Håndtér).

**Vigtigt **: For at lade apps såsom FairEmail pålideligt oprette forbindelse til OpenKeychain-tjenesten for at kryptere/dekryptere beskeder, kan deaktivering af batterioptimeringer af OpenKeychain-appen være nødvendig.

**Vigtigt**: På visse Android-versioner/-enheder er det nødvendigt at aktivere *Vis popups i baggrundstilstand * i de udvidede tilladelser til Android-app indstillingerne i OpenKeychain-appen. Uden denne tilladelse, gemmes udkastet, men OpenKeychain-popup'en til bekræftelse/valg vises muligvis ikke.

FairEmail sender [Autokrypt](https://autocrypt.org/)-headere til brug for andre e-mailklienter og sender modtagne Autocrypt-headere til OpenKeychain-appen for opbevaring.

Al nøglehåndtering uddelegeres af sikkerhedsårsager til OpenKey-kæde appen. Dette betyder også, at FairEmail ikke gemmer PGP-nøgler.

Inline-krypteret PGP i modtagne beskeder understøttes, men inline PGP-signaturer og inline PGP i udgående beskeder understøttes ikke, se [hér](https://josefsson.org/inline-openpgp-considered-harmful.html) for årsagen.

Kun signerede eller kun krypterede beskeder er ikke en god idé. Se her for årsagen:

* [Overvejelser vedr. OpenPGP, del I](https://k9mail.github.io/2016/11/24/OpenPGP-Considerations-Part-I.html)
* [Overvejelser vedr. OpenPGP, del II](https://k9mail.github.io/2017/01/30/OpenPGP-Considerations-Part-II.html)
* [Overvejelser vedr. OpenPGP, del III, Autocrypt](https://k9mail.github.io/2018/02/26/OpenPGP-Considerations-Part-III-Autocrypt.html)

Kun signarede beskeder understøttes, kun krypterede beskeder understøttes ikke.

Almindelige fejl:

* *Manglende krypteringsnøgle*: Der er sandsynligvis valgt en nøgle i FairEmail, der ikke længere forefindes i OpenKeychain-appen. Nulstilling af nøglen (se ovenfor) løser sandsynligvis dette problem.

*S/MIME*

Den/de offentlige modtagernøgle(r) kræves ved kryptering af en besked. Din private nøgle kræves ved signering af en besked.

Private nøgler gemmes af Android og kan importeres via Androids avancerede sikkerhedsindstillinger. Der er en genvej (knap) til dette under fortrolighedsindstillingerne. Android beder opsætte en PIN-kode, mønster eller adgangskode, hvis dette ikke allerede er gjort. Har du en Nokia-enhed med Android 9, så [læs først dette](https://nokiamob.net/2019/08/10/a-bug-prevents-nokia-1-owners-from-unlocking-their-screen-even-with-right-pin-pattern/).

Bemærk, at certifikater kan indeholde flere nøgler til flere formål, f.eks. til godkendelse, kryptering og signering. Android importerer kun den første nøgle, så for at importere alle nøgler, skal certifikatet først opdeles. Dette er ikke en almindelig forekommende handling, så du rådes til at bede certifikatleverandøren om support.

Standardkrypteringsmetoden er PGP, men den sidst anvendte krypteringsmetode huskes for den valgte identitet til næste gang. Det kan være nødvendigt igen at aktivere Send-mulighederne i trepriksmenuen for at kunne vælge krypteringsmetoden.

For at tillade forskellige private nøgler til den samme e-mailadresse, lader FairEmail dig altid vælge en nøgle, når der er flere identiteter til den samme e-mailadresse for den samme konto.

Offentlige nøgler gemmes af FairEmail og kan importeres ifm. bekræftelse af en signatur for første gang eller via fortrolighedsindstillingerne (PEM- eller DER-format).

FairEmail foretager bekræftelse af både signaturen samt den komplette certifikatkæde.

Almindelige fejl:

* *Intet certifikat fundet, der matcher targetContraints*: Dette betyder sandsynligvis, at en gammel version af FairEmail benyttes
* *kunne ikke finde en gyldig certificeringssti til det anmodede mål*: Dette betyder grundlæggende, at der ikke blev fundet en eller flere mellem- eller rodcertifikater
* *Privat nøgle matcher ikke nogle krypteringsnøgler*: Den valgte nøgle kan ikke anvendes til beskeddekrypteringen, da den sandsynligvis ikke er den korrekte nøgle
* *Ingen privat nøgle*: Intet certifikat er valgt eller intet certifikat var tilgængeligt i Android-nøglelageret

Er certifikatkæden forkert, kan du trykke på den lille infoknap for at få vist alle certifikaterne. Efter certifikatoplysninger vises udstederen eller "selfSign". Er certifikat er selvsigneret, når både emne og udsteder er identiske. Certifikater fra en certifikatmyndighed (CA) er markeret med "[keyCertSign](https://tools.ietf.org/html/rfc5280#section-4.2.1.3)". Certifikater i Android-nøglelageret, er markeret med "Android".

En gyldig kæde ser således ud:

```
Dit certifikat > nul eller flere mellemliggende certifikater > CA (root) certifikat markeret med "Android"
```

Bemærk, at en certifikatkæde altid er ugyldig, hvis intet ankercertifikat findes i Android-nøglelageret, hvilket er fundamentalt for S/MIME-certifikatbekræftelse.

Se [hér](https://support.google.com/pixelphone/answer/2844832?hl=en), hvordan certifikater kan importeres til Android-nøglelageret.

Brug af udløbne nøgler, inline-krypterede/signerede beskeder samt hardwaresikkerhedstokens er uunderstøttet.

Kigger du efter et gratis (test) S MIME-certifikat, se [hér](http://kb.mozillazine.org/Getting_an_SMIME_certificate) for muligheder. Husk at [læse dette første](https://davidroessli.com/logs/2019/09/free-smime-certificates-in-2019/#update20191219) ved anmodning om et S/MIME Actalis-certifikat.

Sådan udpakkes en offentlig nøgle fra et S/MIME-certifikat:

```
openssl pkcs12 -in filenavn.pfx/p12 -clcerts -nokeys -out cert.pem
```

Du kan afkode S/MIME-signaturer mv. [hér](https://lapo.it/asn1js/).

S/MIME-signering/kryptering er en Pro-funktion, men alle øvrige PGP- og S/MIME-operationer er gratis at benytte.

<br />

<a name="faq13"></a>
**(13) Hvordan fungerer søgning på en enhed/server?**

Du kan begynde at søge efter beskeder efter Afsender (fra), Modtager (til, kopi, bcc), Emne, nøgleord eller beskedtekst vha. forstørrelsesglasset i handlingslbjælken i en mappe. Du kan også søge fra enhver app ved at vælge *Søg e-mail * i popup-menuen kopiér/indsæt.

Søgning i den fælles indbakke udføres i alle mapper på alle konti, søgning i mappelisten udføres kun for den tilknyttede konto og søgning i en mappe udføres kun i dén mappe.

Beskeder søges først på enheden. Der vil være en handlingsknap med et søg igen-ikon i bunden for at fortsætte søgningen på serveren. Du kan vælge, i hvilken mappe, du vil fortsætte søgningen.

IMAP-protokollen understøtter ikke søgning i flere end én mappe samtidigt. Søgning på serveren er en dyr operation, og det er derfor ikke muligt at vælge flere mapper.

Searching local messages is case insensitive and on partial text. Lokle beskedtekster gennemsøges kun, hvis selve beskedteksterne er blevet downloadet. Searching on the server might be case sensitive or case insensitive and might be on partial text or whole words, depending on the provider.

Visse servere kan ikke håndtere søgning i beskedtekster ifm. et stort beskedantal. For sådanne tilfælde findes en mulighed for at deaktivere søgning i beskedtekster.

Det er muligt at bruge Gmail-søgeoperatører vha. søgekommandopræfikset *raw:*. Har du kun opsat én Gmail-konto, kan du starte en rå søgning direkte på serveren ved at søge fra den fælles indbakke. Har du opsat flere Gmail-konti, skal du først navigere til mappelisten eller arkivmappen (alle beskeder) for den Gmail-konto, du vil gennemsøge. [Se hér](https://support.google.com/mail/answer/7190) ang. de mulige søgeoperatorer. F.eks.:

`
raw:larger:10M`

Gennemsøgning af et stort antal beskeder på enheden sker ikke særligt hurtigt grundet to begrænsninger:

* [sqlite](https://www.sqlite.org/), Androids databasemotor har en poststørrelsesbegrænsning, der forhindrer, at beskedtekster gemmes i databasen
* Android-apps får kun begrænset hukommelse at arbejde med, selv hvis enheden har masser af hukommelse til rådighed

Dette betyder, at søgning efter en beskedtekst kræver, at filer indeholdende beskedtekster skal åbnes én efter én for at tjekke, om den søgte tekst optræder i filen, hvilket er en relativt ressourcetung proces.

Under *diverse indstillinger* kan du aktivere *Byg søgeindeks* for markant at øge søgehastigheden på enheden, men dette øger dog samtidig både strøm- og lagerpladsforbruget. Søgeindekset er ordbaseret, så søgning efter deltekster er ikke muligt. Søgning vha. søgeindekset er som standard OG (AND), så søgning efter *æble appelsin* vil søge efter både æble OG appelsin. Ord adskilt med komma resulterer i en ELLER (OR) søgning, så f.eks. *æble, appelsin* vil søge efter enten æble ELLER appelsin. Begge kan kombineres, så søgning efter *æble, appelsin banan* vil søge efter æble ELLER (appelsin OG banan). Brug af søgeindekset er en Pro-funktion.

Beskedsøgning på enheden er en gratis funktion, beskedsøgning på serveren er en Pro-funktion.

<br />

<a name="faq14"></a>
**(14) Hvordan opsættes en Outlook-/Live-/Hotmail-konto?**

En Outlook-/Live-/Hotmail-konto kan opsættes via hurtig opsætningsguiden ved at vælg *Outlook*.

Anvendelse af en Outlook-, Live- eller Hotmail-konto med tofaktorgodkendelse aktiveret kræver oprettelse af en app-adgangskode. Tjek oplysningerne [hér](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification).

Tjek Microsoft-instruktionerne [hér](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040).

Tjek [denne FAQ](#user-content-faq156) for opsætnig af en Office365-konto.

<br />

<a name="faq15"></a>
**(15) Hvorfor genindlæses beskedteksten hele tiden?**

Beskedhovede og -brødtekst hentes separat fra serveren. Større beskeders beskedtekst forudindlæses ikke på takserede forbindelser, og den skal derfor hentes, når beskeden åbnes. Beskedteksten genindlæses kontinuerligt, hvis der ingen forbindelse er til kontoen, se også næste spørgsmål.

Du kan tjekke konto- og mappelisten for kontoen og mappetilstanden (se forklaringen til betydningen af ikonerne) og operationslisten tilgængelig via hovednavigeringsmenuen til afventende operationer (se [denne FAQ](#user-content-faq3) for betydningen af operationerne).

I modtagelsesindstillingerne kan man indstille den maksimale størrelse for automatisk download af beskeder på takserede forbindelser.

Mobilforbindelser er næsten altid takserede, og visse Wi-Fi hotspots (betalte) er det ligeledes.

<br />

<a name="faq16"></a>
**(16) Hvorfor synkroniseres beskeder ikke?**

Mulige årsager til fejl i beskedsynkronisering (sendte eller modtagne) er:

* Konto eller mappe(r) er ikke opsat til at synkronisere
* Der er indstillet for få synkroniseringsdage
* Ingen tilgængelig Internetforbindelse
* E-mailserveren er midlertidigt utilgængelig
* Android har stoppet synkroniseringstjenesten

Tjek derfor dine konto- og mappeindstillinger, og tjek, at konti/mapper er forbundet (se forklaringen i navigeringsmenuen om ikonernes betydning).

Er der nogle fejlmeddelelser, så tjek [denne FAQ](#user-content-faq22).

På enheder, hvor en masse apps som slås om hukommelsen, kan Android muligvis som en sidste udvej stoppe synkroniseringstjenesten.

Visse Android-versioner stopper apps og tjenester for aggressivt. Se [dette dedikerede websted](https://dontkillmyapp.com/) samt [dette Android-problem](https://issuetracker.google.com/issues/122098785) for yderligere information.

Deaktivering af batterioptimering (opsætningstrin 4) reducerer chancen for, at Android stopper synkroniseringstjenesten.

<br />

<a name="faq17"></a>
**~~(17) Hvorfor fungerer manuel synkronisering ikke?~~**

~~Hvis menuen *Synkronisér nu* er nedtonet, er der ingen forbindelse til kontoen.~~

~~Se foregående spørgsmål for yderligere information.~~

<br />

<a name="faq18"></a>
**(18) Hvorfor vises beskedforhåndsvisning ikke altid?**

Beskedforhåndsvisningen kan ikke vises, såfremt beskedbrødteksten endnu ikke er downloadet. Se også [denne FAQ](#user-content-faq15).

<br />

<a name="faq19"></a>
**(19) Hvorfor er Pro-funktionerne så dyre?**

Det rigtige spørgsmål er, "*hvorfor der er så mange skatter og afgifter?*":

* Moms: 25 % (afhængigt af land)
* Google-gebyr: 30 %
* Inkomstskat: 50 %
* <sub>Paypal-gebyr: 5-10 % afhængigt af land/beløb</sub>

Så dét, der er tilbage til udvikleren, er blot en brøkdel af dét, du betaler.

Bemærk, at det kun er nødvendigt at købe visse bekvemmeligheds- og avancerede funktioner, hvilket betyder, at FairEmail grundlæggende er gratis at anvende.

Bemærk også, at de fleste gratis apps ser ud til ikke at være bæredygtige i længden, hvorimod FairEmail vedligeholdes og understøttes korrekt, samt at gratis apps kan have faldgruber, såsom at de lækker fortrolige oplysninger via Internet.

Jeg har arbejdet på FairEmail næsten hver dag i mere end et halvt år, så jeg synes, at prisen er mere end rimelig. Af samme årsag vil der heller ikke være rabatter.

<br />

<a name="faq20"></a>
**(20) Kan jeg få refusion?**

Hvis en købt Pro-funktion ikke fungerer som tilsigtet, og dette ikke skyldes et problem i de gratis funktioner, og jeg ikke kan løse problemet rettidigt, så kan du få en refusion. I alle øvrigee tilfælde er refusion ikke mulig. Under ingen omstændigheder er der mulighed for refusion for noget problem relateret til de gratis funktioner, da der ikke blev betalt noget for dem, og da de kan evalueres uden nogen begrænsning. Jeg tager ansvar som sælger for at levere dét, der er blevet lovet, og jeg forventer, at du tager ansvar for at informere dig om, hvad det er, du køber.

<a name="faq21"></a>
**(21) Hvordan aktiveres notifikationslyset?**

Før Android 8 Oreo: Der findes en avanceret indstilling i opsætningen til dette.

Android 8 Oreo og senere: Tjek [hér](https://developer.android.com/training/notify-user/channels), hvordan du opsætter notifikationskanaler. Du kan benytte knappen *Håndtér notifikationer* i opsætningen for at gå direkte til Android-notifikationsindstillingerne. Bemærk, at apps ikke længere kan ændre notifikationsindstillinger, herunder indstillinger for notifikationslys, på Android 8 Oreo og senere. Apps designet og målrettet ældre Android-versioner kan muligvis stadig styre indholdet af notifikationer, men sådanne apps kan ikke længere opdatere, og nyere Android-versioner viser en advarsel om, at sådanne apps er forældede.

Undertiden er det nødvendigt at deaktivere indstillingen *Vis beskedforhåndsvisning i notifikationer* eller at aktivere indstillingerne *Vis kun notifikationer med en forhåndsvisningstekst* for at omgå en fejl i Android. Dette gælder muligvis også notifikationslyde og vibrationer.

Indstilling af lys-farven før Android 8 understøttes ikke og er ikke muligt på Android 8 og senere.

<br />

<a name="faq22"></a>
**(22) Hvad betyder konto-/mappefejl ...?**

FairEmail skjuler ikke fejl (hvilket lignende apps ofte gør), så det er lettere at diagnosticere problemer.

FairEmail forsøger automatisk at genoprette forbindelse efter en forsinkelse. Denne forsinkelse fordobles efter hvert mislykket forsøg for at forhindre batteridræning samt at blive låst ude permanent.

Der er generelle såvel som specifikke fejl for Gmail-konti (se nedenfor).

**Generelle fejl**

Fejlen *... Godkendelse mislykkedes ...* eller *... GODKENDELSE mislykkedes ...* skyldes sandsynligvis forkert brugernavn/adgangskode. Viss udbydere forventer som brugernavn blot *brugernavn* og andre din fulde e-mail *brugernavn@eksempel.dk*. When copying/pasting to enter a username or password, invisible characters might be copied, which could cause this problem as well. Some providers require using an app password instead of the account password, so please check the documentation of the provider. Sometimes it is necessary to enable external access (IMAP/SMTP) on the website of the provider first. Other possible causes are that the account is blocked or that logging in has been administratively restricted in some way, for example by allowing to login from certain networks / IP addresses only.

The error *... Too many bad auth attempts ...* likely means that you are using a Yahoo account password instead of an app password. Please see [this FAQ](#user-content-faq88) about how to setup a Yahoo account.

The message *... +OK ...* likely means that a POP3 port (usually port number 995) is being used for an IMAP account (usually port number 993).

The errors *... invalid greeting ...*, *... requires valid address ...* and *... Parameter to HELO does not conform to RFC syntax ...* can likely be solved by changing the advanced identity setting *Use local IP address instead of host name*.

The errors *... Couldn't connect to host ...*, *... Connection refused ...* or *... Network unreachable ...* mean that FairEmail was not able to connect to the email server.

The error *... Host is unresolved ...* or "*... Unable to resolve host ...* means that the address of the email server could not be resolved. This might be caused by ad blocking or an unreachable or not properly working [DNS](https://en.wikipedia.org/wiki/Domain_Name_System) server.

Fejlen *... Software caused connection abort ...* means that the email server or something between FairEmail and the email server actively terminated an existing connection. This can for example happen when connectivity was abruptly lost. A typical example is turning on flight mode.

Fejlene *... BYE Logging out ...*, *... Connection reset by peer ...* mean that the email server actively terminated an existing connection.

Fejlen *... Connection closed by peer ...* might be caused by a not updated Exchange server, see [here](https://blogs.technet.microsoft.com/pki/2010/09/30/sha2-and-windows/) for more information.

The errors *... Read error ...*, *... Write error ...*, *... Read timed out ...*, *... Broken pipe ...* mean that the email server is not responding anymore or that the internet connection is bad.

The error *... Unexpected end of zlib input stream ...* means that not all data was received, possibly due to a bad or interrupted connection.

The error *... connection failure ...* could indicate [Too many simultaneous connections](#user-content-faq23).

The warning *... Unsupported encoding ...* means that the character set of the message is unknown or not supported. FairEmail will assume ISO-8859-1 (Latin1), which will in most cases result in showing the message correctly.

Please [see here](#user-content-faq4) for the errors *... Untrusted ... not in certificate ...*, *... Invalid security certificate (Can't verify identity of server) ...* or *... Trust anchor for certification path not found ...*

Please [see here](#user-content-faq127) for the error *... Syntactically invalid HELO argument(s) ...*.

Please [see here](#user-content-faq41) for the error *... Handshake failed ...*.

See [here](https://linux.die.net/man/3/connect) for what error codes like EHOSTUNREACH and ETIMEDOUT mean.

Possible causes are:

* Firewall eller router blokerer forbindelser til serveren
* Værtsnavnet eller portnummeret er ugyldigt
* Problemer med Internetforbindelsen
* E-mailserveren nægeter at acceptere forbindelser
* E-mai-serveren nægter at acceptere en besked, f.eks. fordi den er for stor eller indeholder uacceptable links
* Der er for mange forbindelser til serveren, se også næste spørgsmål

Many public Wi-Fi networks block outgoing email to prevent spam. Sometimes you can workaround this by using another SMTP port. See the documentation of the provider for the usable port numbers.

If you are using a [VPN](https://en.wikipedia.org/wiki/Virtual_private_network), the VPN provider might block the connection because it is too aggressively trying to prevent spam. Note that [Google Fi](https://fi.google.com/) is using a VPN too.

**Send errors**

SMTP-servere kan afvise beskeder [af forskellige årsager](https://en.wikipedia.org/wiki/List_of_SMTP_server_return_codes). Too large messages and triggering the spam filter of an email server are the most common reasons.

* Gmails størrelsesbegrænsning for vedhæftninger [udgør 25 MB](https://support.google.com/mail/answer/6584)
* Outlooks og Office 365' størrelsesbegrænsning for vedhæftninger [udgør 20 MB](https://support.microsoft.com/en-us/help/2813269/attachment-size-exceeds-the-allowable-limit-error-when-you-add-a-large)
* Yahoos størrelsesbegrænsning for vedhæftninger [udgør 25 MB](https://help.yahoo.com/kb/SLN5673.html)
* For *554 5.7.1 Tjeneste utilgængelig; Klient vært xxx.xxx.xxx.xxx blokeret*, tjek venligst [hér](https://docs.gandi.net/en/gandimail/faq/error_types/554_5_7_1_service_unavailable.html)

**Gmail errors**

The authorization of Gmail accounts setup with the quick wizard needs to be periodically refreshed via the [Android account manager](https://developer.android.com/reference/android/accounts/AccountManager). This requires contact/account permissions and internet connectivity.

Fejlen *... Godkendelse mislykkedes ... Account not found ...* means that a previously authorized Gmail account was removed from the device.

The errors *... Godkendelse mislykkedes ... No token on refresh ...* means that the Android account manager failed to refresh the authorization of a Gmail account.

The error *... Authentication failed ... Invalid credentials ... network error ...* means that the Android account manager was not able to refresh the authorization of a Gmail account due to problems with the internet connection

The error *... Authentication failed ... Invalid credentials ...* could be caused by having revoked the required account/contacts permissions. Just start the wizard (but do not select an account) to grant the required permissions again.

The eror *... ServiceDisabled ...* might be caused by enrolling in the [Advanced Protection Program](https://landing.google.com/advancedprotection/): "*To read your email, you can (must) use Gmail - You won’t be able to use your Google Account with some (all) apps & services that require access to sensitive data like your emails*", see [here](https://support.google.com/accounts/answer/7519408?hl=en&ref_topic=9264881).

When in doubt, you can ask for [support](#user-content-support).

<br />

<a name="faq23"></a>
**(23) Why do I get alert ... ?**

*General*

Alerts are warning messages sent by email servers.

*Too many simultaneous connections* or *Maximum number of connections exceeded*

This alert will be sent when there are too many folder connections for the same email account at the same time.

Possible causes are:

* Adskillige e-mailklienter er forbundet til den samme konto
* Samme e-mailklient er forbundet adskillige gange til den samme konto
* Tidligere forbindelser blev brat afsluttet, f.eks. ved en pludselig mistet Internetforbindelse

First try to wait some time to see if the problem resolves itself, else:

* Skift enten til periodisk tjek for beskeder i modtagelsesindstillingerne, hvilket resulterer i, at mapper åbnes én ad gangen
* eller indstil nogle mapper til polling i stedet for at synkronisation (langt tryk på mappen i mappelisten, Redigér egenskaber)

The maximum number of simultaneous folder connections for Gmail is 15, so you can synchronize at most 15 folders simultaneously on *all* your devices at the same time. For this reason Gmail *user* folders are set to poll by default instead of synchronize always. When needed or desired, you can change this by long pressing a folder in the folder list and selecting *Edit properties*. See [here](https://support.google.com/mail/answer/7126229) for details.

When using a Dovecot server, you might want to change the setting [mail_max_userip_connections](https://doc.dovecot.org/settings/dovecot_core_settings/#mail-max-userip-connections).

<br />

<a name="faq24"></a>
**(24) What is browse messages on the server?**

Browse messages on the server will fetch messages from the email server in real time when you reach the end of the list of synchronized messages, even when the folder is set to not synchronize. You can disable this feature in the advanced account settings.

<br />

<a name="faq25"></a>
**(25) Why can't I select/open/save an image, attachment or a file?**

When a menu item to select/open/save a file is disabled (dimmed) or when you get the message *Storage access framework not available*, the [storage access framework](https://developer.android.com/guide/topics/providers/document-provider), a standard Android component, is probably not present. This might be because your custom ROM does not include it or because it was actively removed (debloated).

FairEmail does not request storage permissions, so this framework is required to select files and folders. No app, except maybe file managers, targeting Android 4.4 KitKat or later should ask for storage permissions because it would allow access to *all* files.

The storage access framework is provided by the package *com.android.documentsui*, which is visible as *Files* app on some Android versions (notable OxygenOS).

You can enable the storage access framework (again) with this adb command:

```
pm install -k --user 0 com.android.documentsui
```

Alternatively, you might be able to enable the *Files* app again using the Android app settings.

<br />

<a name="faq26"></a>
**(26) Can I help to translate FairEmail in my own language?**

Yes, you can translate the texts of FairEmail in your own language [on Crowdin](https://crowdin.com/project/open-source-email). Registration is free.

<br />

<a name="faq27"></a>
**(27) How can I distinguish between embedded and external images?**

External image:

![External image](https://raw.githubusercontent.com/google/material-design-icons/master/image/1x_web/ic_image_black_48dp.png)

Embedded image:

![Embedded image](https://raw.githubusercontent.com/google/material-design-icons/master/image/1x_web/ic_photo_library_black_48dp.png)

Broken image:

![Broken image](https://raw.githubusercontent.com/google/material-design-icons/master/image/1x_web/ic_broken_image_black_48dp.png)

Note that downloading external images from a remote server can be used to record you did see a message, which you likely don't want if the message is spam or malicious.

<br />

<a name="faq28"></a>
**(28) How can I manage status bar notifications?**

In the setup you'll find a button *Manage notifications* to directly navigate to the Android notifications settings for FairEmail.

On Android 8.0 Oreo and later you can manage the properties of the individual notification channels, for example to set a specific notification sound or to show notifications on the lock screen.

FairEmail has the following notification channels:

* Tjeneste: Benytttes til notifikation om synkroniseringstjenesten, se også [denne FAQ](#user-content-faq2)
* Send: Benyttes til sendetjenestenotiifikation
* Notifikationer: Benyttes til notifikation om ny besked
* Advarsel: Benyttes til advarselsnotifikationer
* Fejl: Benyttes til notifikationer om fejl

See [here](https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels) for details on notification channels. In short: tap on the notification channel name to access the channel settings.

On Android before Android 8 Oreo you can set the notification sound in the settings.

See [this FAQ](#user-content-faq21) if your device has a notification light.

<br />

<a name="faq29"></a>
**(29) How can I get new message notifications for other folders?**

Just long press a folder, select *Edit properties*, and enable either *Show in unified inbox* or *Notify new messages* (available on Android 7 Nougat and later only) and tap *Save*.

<br />

<a name="faq30"></a>
**(30) How can I use the provided quick settings?**

There are quick settings (settings tiles) available to:

* globally enable/disable synchronization
* show the number of new messages and marking them as seen (not read)

Quick settings require Android 7.0 Nougat or later. The usage of settings tiles is explained [here](https://support.google.com/android/answer/9083864).

<br />

<a name="faq31"></a>
**(31) How can I use the provided shortcuts?**

There are shortcuts available to:

* compose a new message to a favorite contact
* setup accounts, identities, etc

Shortcuts require Android 7.1 Nougat or later. The usage of shortcuts is explained [here](https://support.google.com/android/answer/2781850).

<br />

<a name="faq32"></a>
**(32) How can I check if reading email is really safe?**

You can use the [Email Privacy Tester](https://www.emailprivacytester.com/) for this.

<br />

<a name="faq33"></a>
**(33) Why are edited sender addresses not working?**

Most providers accept validated addresses only when sending messages to prevent spam.

For example Google modifies the message headers like this for *unverified* addresses:

```
Fra: Nogen <somebody@example.org>
X-Google-Originale-Fra: Nogen <somebody+extra@example.org>
```

This means that the edited sender address was automatically replaced by a verified address before sending the message.

Note that this is independent of receiving messages.

<br />

<a name="faq34"></a>
**(34) How are identities matched?**

Identities are as expected matched by account. For incoming messages the *to*, *cc*, *bcc*, *from* and *(X-)delivered/envelope/original-to* addresses will be checked (in this order) and for outgoing messages (drafts, outbox and sent) only the *from* addresses will be checked.

The matched address will be shown as *via* in the addresses section.

Note that identities needs to be enabled to be able to be matched and that identities of other accounts will not be considered.

Matching will be done only once on receiving a message, so changing the configuration will not change existing messages. You could clear local messages by long pressing a folder in the folder list and synchronize the messages again though.

It is possible to configure a [regex](https://en.wikipedia.org/wiki/Regular_expression) in the identity settings to match the username of an email address (the part before the @ sign).

Note that the domain name (the parts after the @ sign) always needs to be equal to the domain name of the identity.

If you like to match the special purpose email addresses abc@example.com and xyx@example.com and like to have a fallback email address main@example.com as well, you could do something like this:

* Identity: abc@example.com; regex: **(?i)abc**
* Identity: xyz@example.com; regex: **(?i)xyz**
* Identity: main@example.com; regex: **^(?i)((?!abc|xyz).)\*$**

Matched identities can be used to color code messages. The identity color takes precedence over the account color. Setting identity colors is a pro feature.

<br />

<a name="faq35"></a>
**(35) Why should I be careful with viewing images, attachments, and the original message?**

Viewing remotely stored images (see also [this FAQ](#user-content-faq27)) might not only tell the sender that you have seen the message, but will also leak your IP address.

Opening attachments or viewing an original message might load remote content and execute scripts, that might not only cause privacy sensitive information to leak, but can also be a security risk.

Note that your contacts could unknowingly send malicious messages if they got infected with malware.

FairEmail formats messages again causing messages to look different from the original, but also uncovering phishing links.

Note that reformatted messages are often better readable than original messages because the margins are removed, and font colors and sizes are standardized.

The Gmail app shows images by default by downloading the images through a Google proxy server. Since the images are downloaded from the source server [in real-time](https://blog.filippo.io/how-the-new-gmail-image-proxy-works-and-what-this-means-for-you/), this is even less secure because Google is involved too without providing much benefit.

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
**(37) How are passwords stored?**

All supported Android versions [encrypt all user data](https://source.android.com/security/encryption), so all data, including usernames, passwords, messages, etc, is stored encrypted.

If the device is secured with a PIN, pattern or password, you can make the account and identity passwords visible. If this is a problem because you are sharing the device with other people, consider to use [user profiles](https://www.howtogeek.com/333484/how-to-set-up-multiple-user-profiles-on-android/).

<br />

<a name="faq39"></a>
**(39) How can I reduce the battery usage of FairEmail?**

Recent Android versions by default report *app usage* as a percentage in the Android battery settings screen. **Confusingly, *app usage* is not the same as *battery usage* and is not even directly related to battery usage!** The app usage (while in use) will be very high because FairEmail is using a foreground service which is considered as constant app usage by Android. However, this doesn't mean that FairEmail is constantly using battery power. The real battery usage can be seen by navigating to this screen:

*Android settings*, *Battery*, three-dots menu *Battery usage*, three-dots menu *Show full device usage*

As a rule of thumb the battery usage should be below or in any case not be much higher than *Mobile network standby*. If this isn't the case, please let me know.

It is inevitable that synchronizing messages will use battery power because it requires network access and accessing the messages database.

If you are comparing the battery usage of FairEmail with another email client, please make sure the other email client is setup similarly. For example comparing always sync (push messages) and (infrequent) periodic checking for new messages is not a fair comparison.

Reconnecting to an email server will use extra battery power, so an unstable internet connection will result in extra battery usage. In this case you might want to synchronize periodically, for example each hour, instead of continuously. Note that polling frequently (more than every 30-60 minutes) will likely use more battery power than synchronizing always because connecting to the server and comparing the local and remotes messages are expensive operations.

[On some devices](https://dontkillmyapp.com/) it is necessary to *disable* battery optimizations (setup step 4) to keep connections to email servers open.

Most of the battery usage, not considering viewing messages, is due to synchronization (receiving and sending) of messages. So, to reduce the battery usage, set the number of days to synchronize message for to a lower value, especially if there are a lot of recent messages in a folder. Long press a folder name in the folders list and select *Edit properties* to access this setting.

If you have at least once a day internet connectivity, it is sufficient to synchronize messages just for one day.

Note that you can set the number of days to *keep* messages for to a higher number than to *synchronize* messages for. You could for example initially synchronize messages for a large number of days and after this has been completed reduce the number of days to synchronize messages for, but leave the number of days to keep messages for.

In the receive settings you can enable to always synchronize starred messages, which will allow you to keep older messages around while synchronizing messages for a limited number of days.

Disabling the folder option *Automatically download message texts and attachments* will result in less network traffic and thus less battery usage. You could disable this option for example for the sent folder and the archive.

Synchronizing messages at night is mostly not useful, so you can save on battery usage by not synchronizing at night. In the settings you can select a schedule for message synchronization (this is a pro feature).

FairEmail will by default synchronize the folder list on each connection. Since folders are mostly not created, renamed and deleted very often, you can save some network and battery usage by disabling this in the receive settings.

FairEmail will by default check if old messages were deleted from the server on each connection. If you don't mind that old messages that were delete from the server are still visible in FairEmail, you can save some network and battery usage by disabling this in the receive settings.

Some providers don't follow the IMAP standard and don't keep connections open long enough, forcing FairEmail to reconnect often, causing extra battery usage. You can inspect the *Log* via the main navigation menu to check if there are frequent reconnects (connection closed/reset, read/write error/timeout, etc). You can workaround this by lowering the keep-alive interval in the advanced account settings to for example 9 or 15 minutes. Note that battery optimizations need to be disabled in setup step 4 to reliably keep connections alive.

Some providers send every two minutes something like '*Still here*' resulting in network traffic and your device to wake up and causing unnecessary extra battery usage. You can inspect the *Log* via the main navigation menu to check if your provider is doing this. If your provider is using [Dovecot](https://www.dovecot.org/) as IMAP server, you could ask your provider to change the [imap_idle_notify_interval](https://wiki.dovecot.org/Timeouts) setting to a higher value or better yet, to disable this. If your provider is not able or willing to change/disable this, you should consider to switch to periodically instead of continuous synchronization. You can change this in the receive settings.

If you got the message *This provider does not support push messages* while configuring an account, consider switching to a modern provider which supports push messages (IMAP IDLE) to reduce battery usage.

If your device has an [AMOLED](https://en.wikipedia.org/wiki/AMOLED) screen, you can save battery usage while viewing messages by switching to the black theme.

By default auto optimize in the receive settings is enabled, which will switch an account to periodically checking for new messages when the email server:

* Says '*Still here*' within 3 minutes
* The email server does not support push messages
* The keep-alive interval is lower than 12 minutes

<br />

<a name="faq40"></a>
**(40) How can I reduce the network usage of FairEmail?**

You can reduce the network usage basically in the same way as reducing battery usage, see the previous question for suggestions.

By default FairEmail does not download message texts and attachments larger than 256 KiB when there is a metered (mobile or paid Wi-Fi) internet connection. You can change this in the connection settings.

<br />

<a name="faq41"></a>
**(41) How can I fix the error 'Handshake failed' ?**

There are several possible causes, so please read to the end of this answer.

The error '*Handshake failed ... WRONG_VERSION_NUMBER ...*' might mean that you are trying to connect to an IMAP or SMTP server without an encrypted connection, typically using port 143 (IMAP) and port 25 (SMTP), or that a wrong protocol (SSL/TLS or STARTTLS) is being used.

Most providers provide encrypted connections using different ports, typically port 993 (IMAP) and port 465/587 (SMTP).

If your provider doesn't support encrypted connections, you should ask to make this possible. If this isn't an option, you could enable *Allow insecure connections* both in the advanced settings AND the account/identity settings.

See also [this FAQ](#user-content-faq4).

The error '*Handshake failed ... SSLV3_ALERT_ILLEGAL_PARAMETER ...*' is either caused by a bug in the SSL protocol implementation or by a too short DH key on the email server and can unfortunately not be fixed by FairEmail.

The error '*Handshake failed ... HANDSHAKE_FAILURE_ON_CLIENT_HELLO ...*' might be caused by the provider still using RC4, which isn't supported since [Android 7](https://developer.android.com/about/versions/nougat/android-7.0-changes.html#tls-ssl) anymore.

The error '*Handshake failed ... UNSUPPORTED_PROTOCOL ...*' might be caused by enabling hardening connections in the connection settings or by Android not supporting older protocols anymore, like SSLv3.

Android 8 Oreo and later [do not support](https://developer.android.com/about/versions/oreo/android-8.0-changes#security-all) SSLv3 anymore. There is no way to workaround lacking RC4 and SSLv3 support because it has completely been removed from Android (which should say something).

You can use [this website](https://ssl-tools.net/mailservers) or [this website](https://www.immuniweb.com/ssl/) to check for SSL/TLS problems of email servers.

<br />

<a name="faq42"></a>
**(42) Can you add a new provider to the list of providers?**

If the provider is used by more than a few people, yes, with pleasure.

The following information is needed:

```
<provider
    name="Gmail"
    link="https://support.google.com/mail/answer/7126229" // link to the instructions of the provider
    type="com.google"> // dette kræves ikke
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
**(43) Can you show the original ... ?**

Show original, shows the original message as the sender has sent it, including original fonts, colors, margins, etc. FairEmail does and will not alter this in any way, except for requesting [TEXT_AUTOSIZING](https://developer.android.com/reference/android/webkit/WebSettings.LayoutAlgorithm), which will *attempt* to make small text more readable.

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

The speed of your device and internet connection and the number of days to synchronize messages for determine how long synchronization will take. Note that you shouldn't set the number of days to synchronize messages for to more than one day in most cases, see also [this FAQ](#user-content-faq39).

<br />

<a name="faq47"></a>
**(47) How do I solve the error 'No primary account or no drafts folder' ?**

You'll get the error message *No primary account or no drafts folder* when trying to compose a message while there is no account set to be the primary account or when there is no drafts folder selected for the primary account. This can happen for example when you start FairEmail to compose a message from another app. FairEmail needs to know where to store the draft, so you'll need to select one account to be the primary account and/or you'll need to select a drafts folder for the primary account.

This can also happen when you try to reply to a message or to forward a message from an account with no drafts folder while there is no primary account or when the primary account does not have a drafts folder.

Please see [this FAQ](#user-content-faq141) for some more information.

<br />

<a name="faq48"></a>
**~~(48) How do I solve the error 'No primary account or no archive folder' ?~~**

~~You'll get the error message *No primary account or no archive folder* when searching for messages from another app. FairEmail needs to know where to search, so you'll need to select one account to be the primary account and/or you'll need to select a archive folder for the primary account.~~

<br />

<a name="faq49"></a>
**(49) How do I fix 'An outdated app sent a file path instead of a file stream' ?**

You likely selected or sent an attachment or image with an outdated file manager or an outdated app which assumes all apps still have storage permissions. For security and privacy reasons modern apps like FairEmail have no full access to all files anymore. This can result into the error message *An outdated app sent a file path instead of a file stream* if a file name instead of a file stream is being shared with FairEmail because FairEmail cannot randomly open files.

You can fix this by switching to an up-to-date file manager or an app designed for recent Android versions. Alternatively, you can grant FairEmail read access to the storage space on your device in the Android app settings. Note that this workaround [won't work on Android Q](https://developer.android.com/preview/privacy/scoped-storage) anymore.

See also [question 25](#user-content-faq25) and [what Google writes about it](https://developer.android.com/training/secure-file-sharing/share-file#RespondToRequest).

<br />

<a name="faq50"></a>
**(50) Can you add an option to synchronize all messages?**

A synchronize all (download all) messages will not be added because it can easily result in out of memory errors and the available storage space filling up. It can also easily result in a lot of battery and data usage. Mobile devices are just not very suitable to download and store years of messages. You can better use the search on server function (see [question 13](#user-content-faq13)), which is faster and more efficient. Note that searching through a lot of messages stored locally would only delay searching and use extra battery power.

<br />

<a name="faq51"></a>
**(51) How are folders sorted?**

Folders are first sorted on account order (by default on account name) and within an account with special, system folders on top, followed by folders set to synchronize. Within each category the folders are sorted on (display) name. You can set the display name by long pressing a folder in the folder list and selecting *Edit properties*.

The navigation (hamburger) menu item *Order folders* in the setup can be used to manually order the folders.

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
**(55) How can I mark all messages as read / move or delete all messages?**

You can use multiple select for this. Long press the first message, don't lift your finger and slide down to the last message. Then use the three dot action button to execute the desired action.

<br />

<a name="faq56"></a>
**(56) Can you add support for JMAP?**

There are almost no providers offering the [JMAP](https://jmap.io/) protocol, so it is not worth a lot of effort to add support for this to FairEmail.

<br />

<a name="faq57"></a>
**(57) ~~Can I use HTML in signatures?~~**

~~Yes, you can use HTML in signatures if you paste formatted text into the signature field or use the *Edit as HTML* menu to enter HTML manually.~~

~~Note that including links and images in messages will increase the likelihood that a message will be seen as spam,~~ ~~especially when you send a message to someone for the first time.~~

~~See [here](https://stackoverflow.com/questions/44410675/supported-html-tags-on-android-textview) for which HTML tags are supported.~~

<br />

<a name="faq58"></a>
**(58) What does an open/closed email icon mean?**

The email icon in the folder list can be open (outlined) or closed (solid):

![External image](https://raw.githubusercontent.com/google/material-design-icons/master/communication/1x_web/ic_mail_outline_black_48dp.png)

Message bodies and attachments are not downloaded by default.

![External image](https://raw.githubusercontent.com/google/material-design-icons/master/communication/1x_web/ic_email_black_48dp.png)

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
* Did you know that if you select text and hit reply, only the selected text will be quoted?

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

* LOGIN
* PLAIN
* CRAM-MD5
* XOAUTH2 ([Gmail](https://developers.google.com/gmail/imap/xoauth2-protocol), [Yandex](https://tech.yandex.com/oauth/))
* NTLM (untested)

SASL authentication methods, besides CRAM-MD5, are not supported because [JavaMail for Android](https://javaee.github.io/javamail/Android) does not support SASL authentication.

If your provider requires an unsupported authentication method, you'll likely get the error message *authentication failed*.

[Server Name Indication](https://en.wikipedia.org/wiki/Server_Name_Indication) is supported by [all supported Android versions](https://developer.android.com/training/articles/security-ssl).

<br />

<a name="faq63"></a>
**(63) How are images resized for displaying on screens?**

Large inline or attached [PNG](https://en.wikipedia.org/wiki/Portable_Network_Graphics) and [JPEG](https://en.wikipedia.org/wiki/JPEG) images will automatically be resized for displaying on screens. This is because email messages are limited in size, depending on the provider mostly between 10 and 50 MB. Images will by default be resized to a maximum width and height of about 1440 pixels and saved with a compression ratio of 90 %. Images are scaled down using whole number factors to reduce memory usage and to retain image quality. Automatically resizing of inline and/or attached images and the maximum target image size can be configured in the send settings.

If you want to resize images on a case-by-case basis, you can use [Send Reduced](https://f-droid.org/en/packages/mobi.omegacentauri.SendReduced/) or a similar app.

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
**(66) Is FairEmail available in the Google Play Family Library?**

The price of FairEmail is too low, lower than that of most similar apps, and there are [too many fees and taxes](#user-content-faq19), Google alone already takes 30 %, to justify making FairEmail available in the [Google Play Family Library](https://support.google.com/googleone/answer/7007852).

<br />

<a name="faq67"></a>
**(67) How can I snooze conversations?**

Multiple select one of more conversations (long press to start multiple selecting), tap the three dot button and select *Snooze ...*. Alternatively, in the expanded message view use *Snooze ...* in the message three-dots 'more' menu or the timelapse action in the bottom action bar. Select the time the conversation(s) should snooze and confirm by tapping OK. The conversations will be hidden for the selected time and shown again afterwards. You will receive a new message notification as reminder.

It is also possible to snooze messages with [a rule](#user-content-faq71).

You can show snoozed messages by unchecking *Filter out* > *Hidden* in the three dot overflow menu.

You can tap on the small snooze icon to see until when a conversation is snoozed.

By selecting a zero snooze duration you can cancel snoozing.

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

There is one exception: the message was not downloaded yet and the message is too large to download automatically on a metered (mobile) connection. You can set or disable the maximum message size on the 'connection' settings tab.

Duplicate (archived) messages, trashed messages and draft messages are not counted.

Messages will automatically be marked read on expanding, unless this was disabled in the individual account settings.

<br />

<a name="faq71"></a>
**(71) How do I use filter rules?**

You can edit filter rules by long pressing a folder in the folder list.

New rules will be applied to new messages received in the folder, not to existing messages. You can check the rule and apply the rule to existing messages or, alternatively, long press the rule in the rule list and select *Execute now*.

You'll need to give a rule a name and you'll need to define the order in which a rule should be executed relative to other rules.

You can disable a rule and you can stop processing other rules after a rule has been executed.

The following rule conditions are available:

* Sender contains
* Recipient contains
* Subject contains
* Has attachments
* Header contains
* Day/time between

All the conditions of a rule need to be true for the rule action to be executed. All conditions are optional, but there needs to be at least one condition, to prevent matching all messages. If you want to match all senders or all recipients, you can just use the @ character as condition because all email addresses will contain this character.

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
* Answer (with template)
* Text-to-speech (sender and subject)
* Automation (Tasker, etc)

Rules are applied directly after the message header has been fetched, but before the message text has been downloaded, so it is not possible to apply conditions to the message text. Note that large message texts are downloaded on demand on a metered connection to save on data usage.

If you want to forward a message, consider to use the move action instead. This will be more reliable than forwarding as well because forwarded messages might be considered as spam.

Since message headers are not downloaded and stored by default to save on battery and data usage and to save storage space it is not possible to preview which messages would match a header rule condition.

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

In the receive settings you can enable scheduling and set the time period and the day of weeks when messages should be received.

Note that an end time equal to or earlier than the start time is considered to be 24 hours later.

For more complex schemes you could set one or more accounts to manual synchronization and send this command to FairEmail to check for new messages:

```
(adb shell) am startservice -a eu.faircode.email.POLL
```

For a specific account:

```
(adb shell) am startservice -a eu.faircode.email.POLL --es account Gmail
```

You can also automate turning receiving messages on and off by sending these commands to FairEmail:

```
(adb shell) am startservice -a eu.faircode.email.ENABLE
(adb shell) am startservice -a eu.faircode.email.DISABLE
```

To enable/disable a specific account:

```
(adb shell) am startservice -a eu.faircode.email.ENABLE --es account Gmail
(adb shell) am startservice -a eu.faircode.email.DISABLE --es account Gmail
```

Note that disabling an account will hide the account and all associated folders and messages.

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

Automation can be used for more advanced schedules, like for example multiple synchronization periods per day or different synchronization periods for different days.

It is possible to install FairEmail in multiple user profiles, for example a personal and a work profile, and to configure FairEmail differently in each profile, which is another possibility to have different synchronization schedules and to synchronize a different set of accounts.

It is also possible to create [rules](#user-content-faq71) with a time condition and to snooze messages until the end time of the time condition. This way it is possible to snooze business related messages until the start of the business hours. This also means that the messages will be on your device for when there is no internet connection, for example when flying.


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

![External image](https://raw.githubusercontent.com/google/material-design-icons/master/maps/1x_web/ic_my_location_black_48dp.png)

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

* the identity is set to synchronize (send messages)
* the associated account is set to synchronize (receive messages)
* the associated account has a drafts folder

FairEmail will try to select the best identity based on the *to* address of the message replied to / being forwarded.

<br />

<a name="faq86"></a>
**~~(86) What are 'extra privacy features'?~~**

~~The advanced option *extra privacy features* enables:~~

* ~~Looking up the owner of the IP address of a link~~
* ~~Detection and removal of [tracking images](#user-content-faq82)~~

<br />

<a name="faq87"></a>
**(87) What does 'invalid credentials' mean?**

The error message *invalid credentials* means either that the user name and/or password is incorrect, for example because the password was changed or expired, or that the account authorization has expired.

If the password is incorrect/expired, you will have to update the password in the account and/or identity settings.

If the account authorization has expired, you will have to select the account again. You will likely need to save the associated identity again as well.

<br />

<a name="faq88"></a>
**(88) How can I use a Yahoo, AOL or Sky account?**

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

Links for less usual protocols like telnet and ftp will not automatically be linked.

<br />

<a name="faq91"></a>
**~~(91) Can you add periodical synchronization to save battery power?~~**

~~Synchronizing messages is an expensive proces because the local and remote messages need to be compared,~~ ~~so periodically synchronizing messages will not result in saving battery power, more likely the contrary.~~

~~See [this FAQ](#user-content-faq39) about optimizing battery usage.~~


<br />

<a name="faq92"></a>
**(92) Can you add spam filtering, verification of the DKIM signature and SPF authorization?**

Spam filtering, verification of the [DKIM](https://en.wikipedia.org/wiki/DomainKeys_Identified_Mail) signature and [SPF](https://en.wikipedia.org/wiki/Sender_Policy_Framework) authorization is a task of email servers, not of an email client. Servers generally have more memory and computing power, so they are much better suited to this task than battery-powered devices. Also, you'll want spam filtered for all your email clients, possibly including web email, not just one email client. Moreover, email servers have access to information, like the IP address, etc of the connecting server, which an email client has no access to.

Of course you can report messages as spam with FairEmail, which will move the reported messages to the spam folder and train the spam filter of the provider, which is how it is supposed to work. This can be done automatically with [filter rules](#user-content-faq71) too. Blocking the sender will create a filter rule to automatically move future messages of the same sender into the spam folder.

Note that you should not delete spam messages, also not from the spam folder, because the email server uses the messages in the spam folder to "learn" what spam messages are.

If you receive a lot of spam messages in your inbox, the best you can do is to contact the email provider to ask if spam filtering can be improved.

Also, FairEmail can show a small red warning flag when DKIM, SPF or [DMARC](https://en.wikipedia.org/wiki/DMARC) authentication failed on the receiving server. You can enable/disable [authentication verification](https://en.wikipedia.org/wiki/Email_authentication) in the display settings.

FairEmail can show a warning flag too if the domain name of the (reply) email address of the sender does not define an MX record pointing to an email server. This can be enabled in the receive settings. Be aware that this will slow down synchronization of messages significantly.

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

<a name="faq103"></a>
**(103) How can I record audio?**

You can record audio if you have a recording app installed which supports the [RECORD_SOUND_ACTION](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media#RECORD_SOUND_ACTION) intent. If no supported app is installed, FairEmail will not show a record audio action/icon.

Unfortunately and surprisingly, most recording apps do not seem to support this intent (they should).

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

Note that the notification setting *Show launcher icon with number of new messages* needs to be enabled (default enabled).

Only *new* unread messages in folders set to show new message notifications will be counted, so messages marked unread again and messages in folders set to not show new message notification will not be counted.

Depending on what you want, the notification settings *Let the number of new messages match the number of notifications* needs to be enabled or disabled.

This feature depends on support of your launcher. FairEmail merely 'broadcasts' the number of unread messages using the ShortcutBadger library. If it doesn't work, this cannot be fixed by changes in FairEmail.

Some launchers display '1' for [the monitoring notification](#user-content-faq2), despite FairEmail explicitly requesting not to show a badge for this notification. This could be caused by a bug in the launcher app or in your Android version. Please double check if the notification dot is disabled for the receive (service) notification channel. You can go to the right notification channel settings via the notification settings of FairEmail. This might not be obvious, but you can tap on the channel name for more settings.

Note that Tesla Unread is [not supported anymore](https://forum.xda-developers.com/android/general/bad-news-tesla-unread-devoloper-t3920415).

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

Setup > Step 1 > Manage > Tap account > Tap advanced > Partial fetch > uncheck

After disabling this setting, you can use the message 'more' (three dots) menu to 'resync' empty messages. Alternatively, you can *Delete local messages* by long pressing the folder(s) in the folder list and synchronize all messages again.

Disabling *Partial fetch* will result in more memory usage.

<br />

<a name="faq111"></a>
**(111) Is OAuth supported?**

OAuth for Gmail is supported via the quick setup wizard. The Android account manager will be used to fetch and refresh OAuth tokens for selected on-device accounts. OAuth for non on-device accounts is not supported because Google requires a [yearly security audit](https://support.google.com/cloud/answer/9110914) ($15,000 to $75,000) for this.

OAuth for Yandex is supported via the quick setup wizard.

OAuth for Office 365 accounts is supported, but Microsoft does not offer OAuth for Outlook, Live and Hotmail accounts (yet?).

OAuth access for Yahoo was requested, but Yahoo never responded to the request. OAuth for AOL [was deactivated](https://www.programmableweb.com/api/aol-open-auth) by AOL. Verizon owns both AOL and Yahoo, collectively named [Oath inc](https://en.wikipedia.org/wiki/Verizon_Media). So, it is reasonable to assume that OAuth is not supported by Yahoo anymore too.

<br />

<a name="faq112"></a>
**(112) Which email provider do you recommend?**

FairEmail is an email client only, so you need to bring your own email address.

There are plenty of email providers to choose from. Which email provider is best for you depends on your wishes/requirements. Please see the websites of [Restore privacy](https://restoreprivacy.com/secure-email/) or [Privacy Tools](https://www.privacytools.io/providers/email/) for a list of privacy oriented email providers with advantages and disadvantages.

Be aware that not all providers support standard email protocols, see [this FAQ](#user-content-faq129) for more information.

Using your own (custom) domain name, which is supported by most email providers, will make it easier to switch to another email provider.

<br />

<a name="faq113"></a>
**(113) How does biometric authentication work?**

If your device has a biometric sensor, for example a fingerprint sensor, you can enable/disable biometric authentication in the navigation (hamburger) menu of the setup screen. When enabled FairEmail will require biometric authentication after a period of inactivity or after the screen has been turned off while FairEmail was running. Activity is navigation within FairEmail, for example opening a conversation thread. The inactivity period duration can be configured in the miscellaneous settings. When biometric authentication is enabled new message notifications will not show any content and FairEmail won't be visible on the Android recents screen.

Biometric authentication is meant to prevent others from seeing your messages only. FairEmail relies on device encryption for data encryption, see also [this FAQ](#user-content-faq37).

Biometric authentication is a pro feature.

<br />

<a name="faq114"></a>
**(114) Can you add an import for the settings of other email apps?**

The format of the settings files of most other email apps is not documented, so this is difficult. Sometimes it is possible to reverse engineer the format, but as soon as the settings format changes things will break. Also, settings are often incompatible. For example, FairEmail has unlike most other email apps settings for the number of days to synchronize messages for and for the number of days to keep messages for, mainly to save on battery usage. Moreover, setting up an account/identity with the quick setup is simple, so it is not really worth the effort.

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

Google manages all purchases, so as a developer I have little control over purchases. So, basically the only thing I can do, is give some advice:

* Make sure you have an active, working internet connection
* Make sure you are logged in with the right Google account and that there is nothing wrong with your Google account
* Make sure you installed FairEmail via the right Google account if you configured multiple Google accounts on your device
* Open the Play store app and wait at least a minute to give it time to synchronize with the Google servers
* Open FairEmail and navigate to the pro features screen to let FairEmail check the purchases

You can also try to clear the cache of the Play store app via the Android apps settings. Restarting the device might be necessary to let the Play store recognize the purchase correctly.

Note that:

* Purchases are stored in the Google cloud and cannot get lost
* There is no time limit on purchases, so they cannot expire
* Google does not expose details (name, e-mail, etc) about buyers to developers
* An app like FairEmail cannot select which Google account to use
* It may take a while until the Play store app has synchronized a purchase to another device

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

When FairEmail cannot connect to an email server to receive messages, for example when the internet connection is bad or a firewall or a VPN is blocking the connection, FairEmail will wait 8, 16 and 32 seconds while keeping the device awake (=use battery power) and try again to connect. If this fails, FairEmail will schedule an alarm to retry after 15, 30 and 60 minutes and let the device sleep (=no battery usage).

Between connectivity changes there is a wait of 90 seconds to give the email server the opportunity to discover the old connection is broken. This is necessary because the internet connection of a mobile device is often lost abruptly and to prevent the problem described in [this FAQ](#user-content-faq23).

Note that [Android doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby) does not allow to wake the device earlier than after 15 minutes.

*Force sync* in the three-dots menu of the unified inbox can be used to let FairEmail attempt to reconnect without waiting.

Sending messages will be retried on connectivity changes only (reconnecting to the same network or connecting to another network) to prevent the email server from blocking the connection permanently. You can pull down the outbox to retry manually.

Note that sending will not be retried in case of authentication problems and when the server rejected the message. In this case you can open/expand the message and use the undo icon to move the message to the drafts folder, possible change it and send it again.

<br />

<a name="faq124"></a>
**(124) Why do I get 'Message too large or too complex to display'?**

The message *Message too large or too complex to display* will be shown if there are more than 100,000 characters or more than 500 links in a message. Reformatting and displaying such messages will take too long. You can try to use the original message view, powered by the browser, instead.

<br />

<a name="faq125"></a>
**(125) What are the current experimental features?**

* ...

<br />

<a name="faq126"></a>
**(126) Can message previews be sent to my wearable?**

FairEmail fetches a message in two steps:

1. Fetch message headers
1. Fetch message text and attachments

Directly after the first step new messages will be notified. However, only until after the second step the message text will be available. FairEmail updates exiting notifications with a preview of the message text, but unfortunately wearable notifications cannot be updated.

Since there is no guarantee that a message text will always be fetched directly after a message header, it is not possible to guarantee that a new message notification with a preview text will always be sent to a wearable.

If you think this is good enough, you can enable the notification option *Only send notifications with a message preview to wearables*.

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

The Microsoft Exchange Web Services protocol [is being phased out](https://techcommunity.microsoft.com/t5/Exchange-Team-Blog/Upcoming-changes-to-Exchange-Web-Services-EWS-API-for-Office-365/ba-p/608055).

Note that the desciption of FairEmail starts with the remark that non-standard protocols, like Microsoft Exchange Web Services and Microsoft ActiveSync are not supported.

<br />

<a name="faq134"></a>
**(134) Can you add deleting local messages?**

*POP3*

In the account settings (Setup, step 1, Manage, tap account) you can enable *Leave deleted messages on server*.

*IMAP*

Since the IMAP protocol is meant to synchronize two ways, deleting a message from the device would result in fetching the message again when synchronizing again.

However, FairEmail supports hiding messages, either via the three-dots menu in the action bar just above the message text or by multiple selecting messages in the message list.

It is also possible to set the swipe left or right action to hide a message.

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

* Account: Setup > Step 1 > Manage > Tap account
* Identity: Setup > Step 2 > Manage > Tap identity
* Folder: Long press the folder in the folder list > Edit properties

In the three-dots overflow menu at the top right there is an item to delete the account/identity/folder.

<br />

<a name="faq137"></a>
**(137) How can I reset 'Don't ask again'?**

You can reset all questions set to be not asked again in the miscellaneous settings.

<br />

<a name="faq138"></a>
**(138) Can you add calendar/contact management/synchronizing?**

Calendar and contact management can better be done by a separate, specialized app. Note that FairEmail is a specialized email app, not an office suite.

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

* The account password was changed: changing it in FairEmail too should fix the problem
* Push messages are enabled for too many folders: see [this FAQ](#user-content-faq23) for more information and a workaround
* An alias email address is being used as username instead of the primary email address
* An incorrect login scheme is being used for a shared mailbox: the right scheme is *username@domain\SharedMailboxAlias*

The shared mailbox alias will mostly be the email address of the shared account, like this:

```
you@example.com\shared@example.com
```

Note that it should be a backslash and not a forward slash.

<br />

<a name="faq140"></a>
**(140) Why does the message text contain strange characters?**

Displaying strange characters is almost always caused by specifying no or an invalid character encoding by the sending software. FairEmail will assume [ISO 8859-1](https://en.wikipedia.org/wiki/ISO/IEC_8859-1) when no character set or when [US-ASCII](https://en.wikipedia.org/wiki/ASCII) was specified. Other than that there is no way to reliably determine the correct character encoding automatically, so this cannot be fixed by FairEmail. The right action is to complain to the sender.

<br />

<a name="faq141"></a>
**(141) How can I fix 'A drafts folder is required to send messages'?**

To store draft messages a drafts folder is required. In most cases FairEmail will automatically select the drafts folders on adding an account based on [the attributes](https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml) the email server sends. However, some email servers are not configured properly and do not send these attributes. In this case FairEmail tries to identify the drafts folder by name, but this might fail if the drafts folder has an unusual name or is not present at all.

You can fix this problem by manually selecting the drafts folder in the account settings (Setup, step 1, tap account, at the bottom). If there is no drafts folder at all, you can create a drafts folder by tapping on the '+' button in the folder list of the account (tap on the account name in the navigation menu).

Some providers, like Gmail, allow enabling/disabling IMAP for individual folders. So, if a folder is not visible, you might need to enable IMAP for the folder.

Quick link for Gmail: [https://mail.google.com/mail/u/0/#settings/labels](https://mail.google.com/mail/u/0/#settings/labels)

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

![External image](https://raw.githubusercontent.com/google/material-design-icons/master/action/1x_web/ic_record_voice_over_black_48dp.png)

This requires a compatible audio recorder app to be installed. In particular [this common intent](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION) needs to be supported.

For example [this audio recorder](https://f-droid.org/app/com.github.axet.audiorecorder) is compatible.

Voice notes will automatically be attached.

<br />

<a name="faq145"></a>
**(145) How can I set a notification sound for an account, folder or sender?**

Account:

* Enable *Separate notifications* in the advanced account settings (Setup, step 1, Manage, tap account, tap Advanced)
* Long press the account in the account list (Setup, step 1, Manage) and select *Edit notification channel* to change the notification sound

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

In these rare cases, it is possible to let FairEmail use either the date/time from the *Date* header (sent time) or from the *Received* header as a workaround. This can be changed in the advanced account settings: Setup, step 1, Manage, tap account, tap Advanced.

This will not change the time of already synchronized messages. To solve this, long press the folder(s) in the folder list and select *Delete local messages* and *Synchronize now*.

<br />

<a name="faq147"></a>
**(147) What should I know about third party versions?**

You likely came here because you are using a third party build of FairEmail.

The F-Droid build is supported, but any other unofficial build is not supported.

F-Droid builds irregularly, which can be problematic when there is an important update. Therefore you are advised to switch to the GitHub release.

The F-Droid version is built from the same source code, but signed differently. This means that all features are available in the F-Droid version too, except for using the Gmail quick setup wizard because Google approved (and allows) one signature only.

Note that you'll need to uninstall the F-Droid build first before you can install a GitHub release because Android refuses to install the same app with a different signature for security reasons.

Note that the GitHub version will automatically check for updates. When desired, this can be turned off in the miscellaneous settings.

Please [see here](https://github.com/M66B/FairEmail/blob/master/README.md#user-content-downloads) for all download options.

If you have a problem with the F-Droid build, please check if there is a newer GitHub version first.

<br />

<a name="faq148"></a>
**(148) How can I use an Apple iCloud account?**

There is a built-in profile for Apple iCloud, but if needed you can find the right settings [here](https://support.apple.com/en-us/HT202304).

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

<br />

<a name="faq157"></a>
**(157) How can I set up an Free.fr account?**

Veuillez [voir ici](https://free.fr/assistance/597.html) pour les instructions.

**SMTP est désactivé par défaut**, veuillez [voir ici](https://free.fr/assistance/2406.html) comment il peut être activé.

Veuillez [voir ici](http://jc.etiemble.free.fr/abc/index.php/trucs-astuces/configurer-smtp-free-fr) pour un guide détaillé.

<br />

<a name="faq158"></a>
**(158) Which camera / audio recorder do you recommend?**

To take photos and to record audio a camera and an audio recorder app are needed. The following apps are open source cameras and audio recorders:

* [Open Camera](https://play.google.com/store/apps/details?id=net.sourceforge.opencamera) ([F-Droid](https://f-droid.org/en/packages/net.sourceforge.opencamera/))
* [Audio Recorder](https://play.google.com/store/apps/details?id=com.github.axet.audiorecorder) ([F-Droid](https://f-droid.org/packages/com.github.axet.audiorecorder/))

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
(adb shell) am startservice -a eu.faircode.email.DISCONNECT.ME
```

Updating once a week will probably be sufficient, please see [here](https://github.com/disconnectme/disconnect-tracking-protection/commits/master) for recent lists changes.

<br />

## Support

Only the latest Play store version and latest GitHub release are supported. This also means that downgrading is not supported.

Requested features should:

* be useful to most people
* not complicate the usage of FairEmail
* fit within the philosophy of FairEmail (privacy oriented, security minded)
* comply with common standards (IMAP, SMTP, etc)

Features not fulfilling these requirements will likely be rejected. This is also to keep maintenance and support in the long run feasible.

If you have a question, want to request a feature or report a bug, please use [this form](https://contact.faircode.eu/?product=fairemailsupport).

GitHub issues are disabled due to frequent misusage.

Copyright &copy; 2018-2020 Marcel Bokhorst.
