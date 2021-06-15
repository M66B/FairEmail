<a name="top"></a>

# FairEmail-support

[<img src="https://github.com/M66B/FairEmail/raw/master/app/src/main/resExtra/drawable/language_de.png" /> Deutsch](https://github.com/M66B/FairEmail/blob/master/docs/FAQ-de-rDE.md)<br /> [<img src="https://github.com/M66B/FairEmail/raw/master/app/src/main/resExtra/drawable/language_fr.png" /> Français](https://github.com/M66B/FairEmail/blob/master/docs/FAQ-fr-rFR.md)<br /> [<img src="https://github.com/M66B/FairEmail/raw/master/app/src/main/resExtra/drawable/language_es.png" /> Español](https://github.com/M66B/FairEmail/blob/master/docs/FAQ-es-rES.md)<br /> [<img src="https://github.com/M66B/FairEmail/raw/master/images/outline_translate_black_24dp.png" /> Other languages](https://github.com/M66B/FairEmail/blob/master/docs/)

Ved evt. spørgsmål, tjek først de ofte stillede spørgsmål nedenfor. [Allernederst](#user-content-get-support) findes info om, hvordan der stilles andre spørgsmål, anmodes om funktioner og indrapporteres fejl.

## Indeks

* [Godkendelse af konti](#user-content-authorizing-accounts)
* [Hvordan kan man...?](#user-content-howto)
* [Kendte problemer](#user-content-known-problems)
* [Planlagte funktioner](#user-content-planned-features)
* [Hyppigt anmodede funktioner](#user-content-frequently-requested-features)
* [Ofte stillede spørgsmål](#user-content-frequently-asked-questions)
* [Få support](#user-content-get-support)

<h2><a name="authorizing-accounts"></a>Godkendelse af konti</h2>

I de fleste tilfælde vil hurtigopsætningen automatisk kunne identificere den korrekte opsætning.

Fejler hurtigopsætningen, så opsæt manuelt en konto (til e-mailmodtagelse) samt identitet (til e-mailafsendelse). Hertil kræves info om IMAP- og SMTP-serveradresserne og portnumrene, hvorvidt SSL/TLS eller STARTTLS skal anvendes og brugernavn (oftest ens e-mailadresse) samt adgangskode.

Søgning på *IMAP* og udbydernavnet er ofte tilstrækkeligt til at finde den rette dokumentation.

I visse tilfælde vil ekstern kontoadgang skulle aktiveres og/eller en speciel (app-)adgangskode benyttes, f.eks. ved brug af tofaktorgodkendelse.

Til godkendelse:

* Gmail/G Suite, se [spørgsmål 6](#user-content-faq6)
* Outlook/Live/Hotmail, se [spørgsmål 14](#user-content-faq14)
* Office 365, see [question 156](#user-content-faq156)
* Microsoft Exchange, se [spørgsmål 8](#user-content-faq8)
* Yahoo, AOL og Sky, se [spørgsmål 88](#user-content-faq88)
* Apple iCloud, se [spørgsmål 148](#user-content-faq148)
* Free.fr, se [spørgsmål 157](#user-content-faq157)

Tjek [hér](#user-content-faq22) for alm. forekommende fejlmeddelelser og løsninger.

Relaterede spørgsmål:

* [Understøttes OAuth?](#user-content-faq111)
* [Hvorfor understøttes ActiveSync ikke?](#user-content-faq133)

<a name="howto">

## Hvordan kan man...?

* Skift af kontonavn: Tryk på Indstillinger > Manuel opsætning > Konti > konto
* Skift venstre/højre strygehandling: Indstillinger, tryk på Adfærd, Opsæt strygehandlinger
* Skift af adgangskode: Tryk på Indstillinger > Manuel opsætning > Konti > konto > skift adgangskode
* Opsæt en signatur: Tryk på Indstillinger > Manuel opsætning > Identiteter > identitet > Redigér signatur.
* Tilføj CC- og BCC-adresser: Tryk på folks ikon i slutningen af emnet
* Gå til næste/foregående besked ved arkivering/sletning: Deaktivér *Luk automatisk samtaler* i adfærdsindstillingerne og vælg *Gå til næste/foregående samtale* for *Ved lukning af en samtale*
* Føje en mappe til den fælles indbakke: Langt tryk på mappen i mappelisten og afkryds *Vis i fælles indbakke*
* Føje en mappe til navigeringsmenuen: Langt tryk på mappen i mappelisten og afkryds *Vis i navigeringsmenu*
* Indlæs flere beskeder: Langt tryk på en mappe på mappelisten, vælg *Hent flere beskeder*
* Slet en besked, overspring papirkurv: Langt tryk på papirkurv-ikonet
* Slet konto/identitet: Tryk på Indstillinger > Manuel opsætning > Konti/identitet > konto/identitet > papirkurv-ikon øverst til højre
* Slet en mappe: Langt tryk på mappen på mappelisten > Redigér egenskaber > papirkurv-ikon øverst til højre
* Fortryd afsendelse: Udbakke, stryg beskeden på listen til venstre/højre
* Gemme sendte beskeder i indbakken: [Se denne FAQ](#user-content-faq142)
* Skift af systemmapper: Tryk på Indstillinger > Manuel opsætning > Konti > konto nederst
* Eksport-/importindstillinger: Indstillinger &#062 (venstre) navigeringsmenu

<h2><a name="known-problems"></a>Kendte problemer</h2>

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
* En forhåndsvisning af en beskedtekst vises ikke (altid) på Samsung-ure, da [setLocalOnly](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder.html#setLocalOnly(boolean))-parameteren synes at blive ignoreret. Beskedforhåndsvisningstekster fremgår korrekt på Pebble 2-, Fitbit Charge 3- og Mi band 3-wearables. Se også [denne FAQ](#user-content-faq126).
* En [fejl i Android](https://issuetracker.google.com/issues/37068143) medfører af og til et nedbrud med "*... Ugyldig forskydning... Gyldigt område er ...* når tekst er valgt og der trykkes uden for den valgte tekst. Denne fejl er rettet i Android 6.0.1.
* Interne (anker-) links vil ikke fungere, da de oprindelige beskeder vises i et indlejret WebView i en rullevisning (samtalelisten). Dette er en Android-begrænsning, der ikke kan rettes eller omgås.
* Sprogdetektering [fungerer ikke længere](https://issuetracker.google.com/issues/173337263) på Pixel-enheder med/ opgraderet til Android 11
* A [bug in OpenKeychain](https://github.com/open-keychain/open-keychain/issues/2688) causes invalid PGP signatures when using a hardware token.

<h2><a name="planned-features"></a>Planlagte funktioner</h2>

* ~~Synkronisere efter behov (manuelt)~~
* ~~Semiautomatisk kryptering~~
* ~~Kopiere besked~~
* ~~Farvede stjerner~~
* ~~Notifikationsindstillinger pr. mappe~~
* ~~Vælg lokale billeder til underskrifter~~ (ingen af dem tilføjes, da dette kræver billedfilhåndtering, og da billedersom standard alligevel ikke vises i de fleste e-mail klienter)
* ~~Vis beskeder matchet af en regel~~
* ~~[ManageSieve](https://tools.ietf.org/html/rfc5804) ~~ (der er ingen vedligeholdte Java-biblioteker med en passende licens og uden afhængigheder, og desuden har FairEmail sine egne filterregler)
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

Alt listeindhold er tilfældigt ordnet og tilføjes *måske* i nærmeste fremtid.

<h2><a name="frequently-requested-features"></a>Hyppigt anmodede funktioner</h2>

Designet baserer sig på mange debatter, så ønsks det debatteret yderligere, tjek [dette forum](https://forum.xda-developers.com/android/apps-games/source-email-t3824168). Designmålet er minimalisme (ingen unødvendige menuer, knapper mv.) og ikke-distraherende (ingen smarte farver, animationer mv.). Alle viste objekter skal have nytteværdi og bør placeres omhyggeligt for nem brug. Skrifttype/størrelser, farver mv. bør, når muligt, være materielt design.

<h2><a name="frequently-asked-questions"></a>Ofte stillede spørgsmål</h2>

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
* [(35) Hvorfor skal man være forsigtig med at få vist billeder, vedhæftninger, originalbeskeder og åbne links?](#user-content-faq35)
* [(36) Hvordan krypteres indstillingsfiler?](#user-content-faq36)
* [(37) Hvordan opbevares adgangskoder?](#user-content-faq37)
* [(39) Hvordan kan jeg reducere FairEmails batteriforbrug?](#user-content-faq39)
* [(40) Hvordan reduceres FairEmails dataforbrug?](#user-content-faq40)
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
* [(57) Kan HTML benyttes i signaturer?](#user-content-faq57)
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
* [(103) Hvordan optager man lyd?](#user-content-faq158)
* [(104) Hvad er nødvendigt at vide om fejlrapportering?](#user-content-faq104)
* [(105) Hvordan fungere indstillingen roam-som-på-hjemadressen?](#user-content-faq105)
* [(106) Hvilke launchere kan vise et badge-tal for antallet af ulæste beskeder?](#user-content-faq106)
* [(107) Hvordan anvendes farvede stjerner?](#user-content-faq107)
* [~~(108) Vil mulighed for permanent slettede beskeder fra enhver mappe blive tilføjet?~~](#user-content-faq108)
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
* [(138) Can you add calendar/contact/tasks/notes management?](#user-content-faq138)
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
* [(160) Kan der blive tilføjet permanent sletning af beskeder uden bekræftelse?](#user-content-faq160)
* [(161) Kan der blive tilføjet en indstilling til ændring af primær- og accentfarverne?](#user-content-faq161)
* [(162) Understøttes IMAP NOTIFICERING?](#user-content-faq162)
* [(163) Hvad er beskedklassificering?](#user-content-faq163)
* [(164) Kan der blive tilføjet tilpasselige temaer?](#user-content-faq164)
* [(165) Is Android Auto supported?](#user-content-faq165)
* [(166) Kan en besked slumres på tværs af flere enheder?](#user-content-faq166)

[Har et andet spørgsmål.](#user-content-support)

<a name="faq1"></a>
**(1) Hvilke tilladelser kræves, og hvorfor?**

Flg. Android-tilladelser kræves:

* *fuld netværksadgang* (INTERNET): For at sende/modtage e-mails
* *se netværksforbindelser* (ACCESS_NETWORK_STATE): For at monitere Internetkonnektivitetsændringer
* *kør ved opstart* (RECEIVE_BOOT_COMPLETED): For at starte monitering ved enhedsstart
* *forgrundstjeneste* (FOREGROUND_SERVICE): For at køre en forgrundstjeneste på Android 9 Pie og senere, se også næste spørgsmål
* *forhindre enhed i at sove* (WAKE_LOCK): For at holde enheden vågen, mens beskeder synkroniseres
* *in-app fakturering* (BILLING): For at tillade køb direkte i appen
* *planlæg eksakt alarm* (SCHEDULE_EXACT_ALARM): For præcis alarmplanlægning (Android 12 og senere)
* Valgfrit: *Læs kontakter* (READ_CONTACTS): For automatisk adresseautofuldførelse, kontaktfotovisning samt [kontaktvalg](https://developer.android.com/guide/components/intents-common#PickContactDat)
* Valgfri: *Læs SD-kortindhold* (READ_EXTERNAL_STORAGE): For at acceptere filer fra andre, forældede apps (tjek også [denne FAQ](#user-content-faq49))
* Valgfri: Benyt *fingeraftrykshardware* (USE_FINGERPRINT) og *biometrisk hardware* (USE_BIOMETRIC): For at benytte biometrisk godkendelse
* Valgfri: *Find konti på enheden* (GET_ACCOUNTS): For at vælge en konto ifm. Gmails hurtig-opsæning
* Op til Android 5.1 Lollipop: *Benyt konti på enheden* (USE_CREDENTIALS): For kontovalg ifm. Gmails hurtigopsætning (senere OS-version benytter ikke denne forespørgsel)
* Op til Android 5.1 Lollipop: *Læs profil* (READ_PROFILE): For at læse brugerens navn ifm. Gmails hurtigopsætning (senere OS-version benytter ikke denne forespørgsel)

[Valgfrie tilladelser](https://developer.android.com/training/permissions/requesting) understøttes kun på Android 6 Marshmallow og nyere. Tidligere Android-versioner anmoder om at tildele de valgfri tilladelser ved installation af FairEmail.

Flg. tilladelser kræves for at vise antal ulæste beskeder som et badge (tjek også [denne FAQ](#user-content-faq106)):

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

FairEmail fører en liste over adresser, hvortil/-fra beskeder er sendt/modtaget og benytter denne til kontaktforslag, når appen ikke er tildelt kontakttilladelser. FairEmail kan derfor benytte uden Android-kontaktleverandør (Kontakter). Bemærk, at kontakter stadig kan vælges uden at tildele FairEmail kontakttilladelser, dog vil forslag fra Kontakter ikke fungerer.

<br />

<a name="faq2"></a>
**(2) Hvorfor vises en permanent notifikation?**

En lavprioritets, permanent statusbjælkenotifikation med det kontoantal, som monitoreres og antal afventede operationer (tjek næste spørgsmål) vises for at forhindre Android i at afslutte tjenesten, der håndterer den løbende modtagelse af e-mail. Dette har [længe været nødvendigt](https://developer.android.com/reference/android/app/Service.html#startForeground(int,%20android.app.Notification)), og med Android 6 (Marshmallow) introduktionen af [slumretilstanden](https://developer.android.com/training/monitoring-device-state/doze-standby), er dette endnu mere nødvendigt. Slumretilstanden stopper alle apps nogen tid efter, at skærmen slukkes, undtagen for apps, som har startet en forgrundstjeneste, der kræver visning af en statusbjælkenotifikation.

De fleste, hvis ikke alle, andre e-mail-apps viser ingen notifikation med "bivirkningen" af ingen/for sene adviseringer om nye beskeder, og at beskeder ikke/for sent afsendes.

Android viser ikoner for højprioritets statusnotifikationer først og skjuler FairEmails notifikationsikon, hvis der ikke er plads til at vise yderligere ikoner. Statusbjælkenotifikationen vil derfor kun fremgå på statusbjælken, såfremt der er ledig plads.

Statusnjælkenotifikationen kan deaktiveres via FairEmails notifikationsindstillinger:

* Android 8 Oreo og senere: Tryk på *Modtagekanal*-knappen og deaktivér kanalen via Android-indstillingerne (dette deaktiverer ikke notifikationer for nye besked)
* Android 7 Nougat og tidligere: Aktivér *Benyt baggrundstjenester til synkronisering af beskeder*, men husk at læse bemærkningen under indstillingen

For at fjerne notifikationen kan der skiftes til periodisk beskedsynkning i modtagelsesindstillingerne, men bemærk, at dette muligvis forbruger mere strøm. Tjek [hér](#user-content-faq39) for mere info vedr. strømforbrug.

Android 8 Oreo viser muligvis også en statusbjælkenotifikation med teksten *Apps kører i baggrunden*. Tjek [hér](https://www.reddit.com/r/Android/comments/7vw7l4/psa_turn_off_background_apps_notification/), hvordan denne notifikation deaktiveres.

Anvendelse af [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/) (FCM) er foreslået i stedet for en Android-tjeneste med en statusbjælkenotifikation, men dette vil enten kræve e-mail udbydere, som sender FCM-meddelelser, eller en central server, hvor alle meddelelser samles mhp. afsendlse af FCM-meddelelser. Førstnævnte kommer ikke til at ske, og sidstnævnte ville have betydelige fortrolighedskonsekvenser.

Er dette sted nået via et tryk på notifikationen, vil næste tryk åbne den fælles indbakke.

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
* *expunge*: Slet beskeder permanent

Operationer behandles kun, såfremt der er forbindelse til e-mailserveren, eller ved manuel synk. Tjek også [denne FAQ](#user-content-faq16).

<br />

<a name="faq4"></a>
**(4) Hvordan kan et ugyldigt sikkerhedscertifikat/tom adgangskode/simpel tekstforbindelse benyttes?**

*... Ikke-betroet ... ikke i certifikatet ...*
<br />
*... Ugyldigt sikkerhedscertifikat (kan ikke bekræfte serveridentet)...*

Dette kan skyldes brug af forkert værtsnavn, så dobbelttjek først dette i de avancerede identitets-/kontoindstillinger (via Manuel opsætning). Tjek dokumentationen fra e-mailleverandøren for korrekt værtsnavn.

Dette bør forsøges løst ved at kontakte udbyderen eller ved at få et gyldigt sikkerhedscertifikat, da ugyldige sikkerhedscertifikater er usikre og tillader [mand-i-midten-angreb](https://en.wikipedia.org/wiki/Man-in-the-middle_attack). Er penge en hindring, kan et gratis sikkerhedscertifikater erhverves fra [Let's Encrypt](https://letsencrypt.org).

Den hurtige, men usikre løsning (ikke anbefalet), er at aktivere *usikre forbindelser* i de avancerede identitetsindstillinger (navigeringsmenu > *Indstillinger* > *Manuel opsætning* > *Identiteter* > identiteten > *Avanceret*).

Alternativt kan du acceptere fingeraftryk på ugyldige servercertifikater som dette:

1. Sørg for at benytt en betroet Internetforbindelse (ingen offentlige Wi-Fi netværk mv.)
1. Gå til opsætningsskærmen via navigeringsmenuen (stryg fra venstre side indad)
1. Tryk på Manuel opsætning > Konti/Identiteter > den defekte konto og identitet
1. Tjek/gem kontoen og identiteten
1. Markér afkrydsningsfeltet under fejlmeddelelsen og gem igen

Dette vil "fastgøre" servercertifikatet for at forhindre man-in-the-middle angreb.

Bemærk, at ældre Android-versioner muligvis ikke genkender nyere certificeringsmyndigheder såsom Let’s Encrypt, hvorfor forbindelser kan blive betragtet som usikre, se også [hér](https://developer.android.com/training/articles/security-ssl).

<br />

*Trust anchor til certificeringsstien ikke fundet*

*... java.security.cert.CertPathValidatorException: Trust anchor til certificeringsstien ikke fundet...* betyder, at Androids standard trust manager ikke var i stand til at bekræfte servercertifikatkæden.

This could be due to the root certificate not being installed on your device or because intermediate certificates are missing, for example because the email server didn't send them.

You can fix the first problem by downloading and installing the root certificate from the website of the provider of the certificate.

The second problem should be fixed by changing the server configuration or by importing the intermediate certificates on your device.

You can pin the certificate too, see above.

<br />

*Kryptere adgangskode*

Your username is likely easily guessed, so this is pretty insecure, unless the SMTP server is available via a restricted local network or a VPN only.

*Simpel tekst-forbindelse*

Dit brugernavn og adgangskode samt alle beskeder sendes og modtages ukrypteret, hvilket er **meget usikkert**, da et [mand-i-midten angreb](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) nemt udføres på en ikke-krypteret forbindelse.

Vil du fortsat bruge et ugyldigt sikkerhedscertifikat, en tom adgangskode eller en almindelig tekstforbindelse, så skal usikre forbindelser aktiveres i konto- og/eller identitetsindstillingerne. STARTTLS bør vælges til simpel tekst-forbindelser. Aktiverer du usikre forbindelser, bør du kun oprette forbindelse via private, betroede netværk og aldrig via offentlige netværk på f.eks. hoteller, lufthavne mv.

<br />

<a name="faq5"></a>
**(5) Hvordan tilpasses beskedvisningen?**

I menuen med tre prikker kan du aktivere/deaktivere eller vælge:

* *tekststørrelse*: Til tre forskellige skriftstørrelser
* *kompakt visning*: Til flere kondenserede beskedelementer og en mindre beskedtekst skrifttype

I visningsafsnittet i indstillingerne kan man aktivere/deaktivere f.eks.:

* *Fælles indbakke*: For at deaktivere den fælles indbakke og i stedet vise de valgte mapper fra den fælles indbakke
* *Tabelform*: Til at vise en lineær liste i stedet for kort
* *Gruppér efter dato*: Vis datooverskrift over beskeder med den samme dato
* *Konversationstråd*: Til at deaktivere konversationstråd og i stedet vise beskeder individuelt
* *Konversationshandlingsbjælke*: Til at deaktivere navigeringsbjælken nederst
* *Fremhæv farve*: Til valg af afsenderfarve for ulæste beskeder
* *Vis kontaktfotos*: Til at skjule kontaktfotos
* *Vis navne og e-mailadresser*: Til at navne/navne og e-mailadresser
* *Vis emne uden kursiv*: Til at vise beskedemnet som alm. tekst
* *Vis stjerner*: Til at skjule stjerner (favoritter)
* *Vis beskedforhåndsvisning*: Til at vise 1-4 linjers beskedtekst
* *Vis som standard adresseoplysninger*: Vis som standard udvidet adresseafsnit
* *Vis automatisk opridelig besked for kendte kontakter*: Tjek [denne FAQ](#user-content-faq35) ang. at få vist originale beskeder automatisk for kontakter på din enhed
* *Vis automatisk billeder for kendte kontakter*: Tjek [denne FAQ](#user-content-faq35) for at få vist billeder automatisk for kontakter på din enhed

Bemærk, at beskeder kun kan forhåndsvises, når beeskedteksten er blevet downloadet. Større beskedtekster downloades som standard ikke på afregnede (hovedsaligt mobile) netværk. Du kan ændre dette i forbindelsesindstillingerne.

Nogle har bedt om:

* at få emnet vist med fed tekst, men fed benyttes allerede til fremhævelse af ulæste beskeder
* at få stjernen flyttet til venstre, men det er meget lettere at betjene stjernen på højre side

<br />

<a name="faq6"></a>
**(6) Hvordan logges ind på Gmail/G suite?**

Bruges Play Butik- eller GitHub-versionen af FairEmail, kan hurtigopsætningsguiden bruges for nem opsætning af en Gmail-konto og identitet. Gmail-hurtigopsætningsguiden er utilgængelig for tredjeparts-builds såsom F-Droid ditto, da Google kun har godkendte brugen af OAuth for officielle builds.

Vil/kan der ikke bruges en enhedsbaseret Gmail-konto på f.eks. nyere Huawei-enheder, aktivér da enten adgang for "mindre sikre apps" med brug af kontoadgangskode (ikke anbefalet), eller tofaktorgodkendelse med brug af en app-specifik adgangskode. For brug af en adgangskode skal der opsættes en konto og identitet via den manuelle opsætning i stedet for hurtigopsætningsguiden.

**Vigtigt**: Google udsender af og til denne advarsel:

*[ALERT] Log ind via din webbrowser: https://support.google.com/mail/accounts/answer/78754 (Fejlet)*

Dette sikkerhedstjek fra Google udløses oftere med *mindre sikre apps* aktiveret, mindre med en app-adgangskode og næsten aldrig ved brug af en konto på enheden (OAuth).

Se [denne FAQ](#user-content-faq111) om, hvorfor kun konti på enheden kan benyttes.

Bemærk, at en app-specifik adgangskode kræves, når tofaktorgodkendelse er aktiveret. After enabling two factor authentication there will be this error message:

*[ALERT] Application-specific password required: https://support.google.com/mail/accounts/answer/185833 (Failure)*

<br />

*App-specifik adgangskode*

Tjek [hér](https://support.google.com/accounts/answer/185833), hvordan en app-specifik adgangskode genereres.

<br />

*Aktivér "Mindre sikre apps"*

**Vigtigt**: Brug af denne metode anbefales ikke, da den er mindre pålidelig.

**Vigtigt**: Brugernavn/adgangskode godkendte G Suite-konti vil [i nærmeste fremtid](https://gsuiteupdates.googleblog.com/2019/12/less-secure-apps-oauth-google-username-password-incorrect.html) ophøre med at fungere.

Tjek [hér](https://support.google.com/accounts/answer/6010255), hvordan "Mindre sikre apps"aktiveres eller gå [direkte til Indstillinger](https://www.google.com/settings/security/lesssecureapps).

Benyttes flere Gmail-konti, så sørg for at ændre indstillingen "Mindre sikre apps" på de korrekte konti.

Bemærk, at man er nødt til at forlade "Mindre sikre apps"-indstillingsskærmen vha. Tilbage-pilen, for at effektuere indstillingen.

Benyttes denne metode, bør en [stærk adgangskode](https://en.wikipedia.org/wiki/Password_strength) anvendes til Gmail-kontoen (generelt altid er en god idé). Bemærk, at brug af [standard](https://tools.ietf.org/html/rfc3501) IMAP-protokollen ikke i sig selv er mindre sikker.

Når "Mindre sikre apps" ikke er aktiveret, ses fejlen *Godkendelse mislykkedes - ugyldige akkreditiver* for konti (IMAP) og *Brugernavn og adgangskode ikke accepteret* for identiteter (SMTP).

<br />

*Generelt*

Muligvis ses advarslen "*Log ind via din webbrowser*". Dette sker, når Google anser det netværk, hvormed der forbindes til Internet (hvilket kan være et VPN), som ikke-sikkert. Dette kan forhindres vha. Gmails hurtig opsætningsguide eller en app-specifik adgangskode.

Tjek [hér](https://support.google.com/mail/answer/7126229) for Googles instruktioner, og [hér](https://support.google.com/mail/accounts/answer/78754) for fejlfinding.

<br />

<a name="faq7"></a>
**(7) Hvorfor vises sendte beskeder ikke (direkte) i Sendt-mappen?**

Sendte beskeder flyttes normalt fra udbakken til Sendt-mappen, så snart udbyderen føjer sendte beskeder til Sendt-mappen. Dette kræver, at en Sendt-mappe vælges i kontoindstillingerne, og at Sendt-mappen ligeledes opsættes til synkronisering.

Visse udbydere holder ikke styr på sendte beskeder, eller den anvendte SMTP-server er muligvis ikke relateret til udbyderen. I så tilfælde tilføjer FairEmail automatisk sendte beskeder til Sendt-mappen under synkronisering af denne, hvilket vil ske, efter at en besked er afsendt. Bemærk, at dette vil forøge Internettrafikken.

~~Sker dette ikke, holder din udbyder muligvis ikke styr på sendte beskeder, eller der anvendes muligvis en ikke-udbyderrelateret SMTP-server.~~ ~~I så tilfælde kan man aktivere den avancerede identitetsindstilling *Gem sendte beskeder* for at lade FairEmail føje sendte beskeder til Sendt-mappen umiddelbart efter beskedafsendelsen.~~ ~~Bemærk, at aktivering af denne indstilling kan resultere i dubletbeskeder, hvis udbyderen også føjer sendte beskeder til Sendt-mappen.~~ ~~Bemærk også, at aktivering af indstillingen vil resultere i forøget datatrafik, især når der sendes beskeder med store vedhæftninger.~~

~~Hvis afsendte beskeder i udbakken ikke findes i Sendt-mappen ved en fuld synkronisering, flytes disse også fra udbakken til Sendt-mappen.~~ ~~En fuld synkronisering sker, når der genforbindes til serveren, eller ved periodisk eller manuel synkronisering.~~ ~~Der ønskes sandsynligvis i stedet at aktivere den avancerede indstilling *Gem sendte beskeder* for hurtigere at flytte beskeder til Sendt-mappen.~~

<br />

<a name="faq8"></a>
**(8) Kan en Microsoft Exchange-konto benyttes?**

The Microsoft Exchange Web Services protocol [is being phased out](https://techcommunity.microsoft.com/t5/Exchange-Team-Blog/Upcoming-changes-to-Exchange-Web-Services-EWS-API-for-Office-365/ba-p/608055). So, it makes little sense to add this protocol anymore.

En Microsoft Exchange-konto kan benyttes, såfremt den er tilgængelig via IMAP (er normalt tilfældet). Tjek [hér](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793) for yderligere information.

Note that the desciption of FairEmail starts with the remark that non-standard protocols, like Microsoft Exchange Web Services and Microsoft ActiveSync are not supported.

Tjek [hér](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) for Microsoft-dokumentation vedr. opsætning af e-mail klient. Der er også et afsnit om almindelige forbindelsesfejl og løsninger.

Visse ældre Exchange-serverversioner har en fejl, der forårsager tomme beskeder og ødelagte vedhæftninger. Tjek [denne FAQ](#user-content-faq110) for en løsning.

Tjek [denne FAQ](#user-content-faq133) vedr. ActiveSync-understøttelse.

Tjek [denne FAQ](#user-content-faq111) vedr. OAuth-understøttelse.

<br />

<a name="faq9"></a>
**(9) Hvad er identiteter/hvordan tilføjes et alias?**

Identiteter repræsenteres af e-mailadresser, du sender *fra* via en e-mailserver (SMTP).

Visse udbydere tillader brug af flere aliaser. Disse kan opsættes ved at indstille e-mailadressefeltet fra en ekstra identitet som aliasadressen og indstille brugernavnefeltet til hoved e-mailadresse.

Bemærk, at en identitet kan kopieres vha. et langt tryk på den.

Alternativt kan *Tillad redigering af afsenderadresse* aktiveres under Avancerede indstillinger for en eksisterende identitet for at redigere brugernavnet ved beskedoprettelsen, såfremt udbyderen tillader dette.

FairEmail opdaterer automatisk adgangskoder til relaterede identiteter, når adgangskoden til den tilknyttede konto/relateret identitet opdateres.

Tjek [denne FAQ](#user-content-faq33) om redigering af brugernavnet til e-mailadresser.

<br />

<a name="faq10"></a>
**~~(10) Hvad betyder 'UIDPLUS ikke understøttet'?~~**

~~Fejlmeddelelsen *UIDPLUS ikke understøttet * betyder, at e-mailudbyderen ikke tilbyder IMAP [UIDPLUS-udvidelsen](https://tools.ietf.org/html/rfc4315). Denne IMAP-udvidelse kræves for at implementere tovejssynkronisering, der ikke er en valgfri funktion. Så medmindre udbyderen kan aktivere denne udvidelse, kan FairEmail ikke benytte med denne.~~

<br />

<a name="faq11"></a>
**~~(11) Hvorfor er POP uunderstøttet?~~**

~~Udover at enhver anstændig e-mail udbyder i dag understøtter [IMAP](https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol),~~ ~~vil brug af [POP](https://en.wikipedia.org/wiki/Post_Office_Protocol) resultere i unødvendig ekstra strømforbrug samt forsinkede notifikationer om nye beskeder.~~ ~~Desuden er POP uegnet til tovejssynkronisering, og oftere end ikke, læses og skrives der i dag beskeder på forskellige enheder.~~

~~Grundlæggende understøtter POP kun download og sletning af beskeder fra indbakken.~~ ~~Dvs., at almindelige operationer, såsom indstilling af besekedattributter (læst, stjernemarkeret, besvaret mv.), tilføjelse (sikkerhedskopiering) og flytning af beskeder ikke er mulige.~~

~~Tjek også, [hvad Google skriver om det](https://support.google.com/mail/answer/7104828).~~

~~F.eks. kan [Gmail importere beskeder](https://support.google.com/mail/answer/21289) fra en anden POP-konto,~~ ~~hvilket kan anvendes som en løsning, når en udbyder ikke understøtter IMAP.~~

~~tl;dr; overvej at skifte til IMAP.~~

<br />

<a name="faq12"></a>
**(12) Hvordan fungerer kryptering/dekryptering?**

Kommunikation med e-mailservere er altid krypteret, medmindre du eksplicit har slået dette fra. Dette spørgsmål omhandler valgfri end-to-end kryptering med PGP eller S/MIME. Afsender og modtager bør først aftale dette og udveksle signerede beskeder for at overføre deres offentlige nøgler for at kunne sende krypterede beskeder.

<br />

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

In the encryption settings you can select the default encryption method (PGP or S/MIME), enable *Sign by default*, *Encrypt by default* and *Automatically decrypt messages*, but be aware that automatic decryption is not possible if user interaction is required, like selecting a key or reading a security token.

Beskedtekster/vedhæftninger, som skal krypteres, samt dekrypterede beskedtekster/vedhæftninger gemmes kun lokalt og vil aldrig blive tilføjet til den eksterne server. Vil du fortryde dekryptering, kan du benytte menupunktet *gensynk* fra trepunktsmenuen på beskedhandlingsbjælken.

<br />

*PGP*

Du skal først installere og opsætte [OpenKeychain](https://f-droid.org/en/packages/org.sufficientlysecure.keychain/). FairEmail er aftestet med OpenKeychain version 5.4. Senere versioner er højest sansynlig kompatible, men tidligere versioner er muligvis ikke.

**Vigtigt**: OpenKeychain-appen er kendt for at gå ned (upåagtet), når den kaldende app (FairEmail) endnu ikke er godkendt og får en eksisterende offentlig nøgle. Du kan løse dette ved at prøve at sende en signeret/krypteret besked til en afsender med en ukendt offentlig nøgle.

**Vigtigt**: Kan OpenKeychain-appen ikke finde en nøgle (længere), skal du muligvis nulstille en tidligere valgt nøgle. Dette kan gøres via at langt tryk på en identitet i identitetslisten (Indstillinger > Manuel opsætning > Identiteter).

**Vigtigt **: For at lade apps såsom FairEmail pålideligt oprette forbindelse til OpenKeychain-tjenesten for at kryptere/dekryptere beskeder, kan deaktivering af batterioptimeringer af OpenKeychain-appen være nødvendig.

**Vigtigt**: OpenKeychain-appen har angiveligt behov for kontakttilladelse for at fungere korrekt.

**Vigtigt**: På visse Android-versioner/-enheder er det nødvendigt at aktivere *Vis popups i baggrundstilstand * i de udvidede tilladelser til Android-app indstillingerne i OpenKeychain-appen. Uden denne tilladelse, gemmes udkastet, men OpenKeychain-popup'en til bekræftelse/valg vises muligvis ikke.

FairEmail vil sende [Autocrypt](https://autocrypt.org/)-headeren til brug for andre e-mail-klienter, men kun for signerede og krypterede beskeder, da for mange e-mailservere har problemer med den ofte lange Autocrypt-header. Bemærk, at den mest sikre måde at starte en krypteret e-mailudveksling på er ved først at sende signerede beskeder. Modtagne Autocrypt-headers sendes til og lagres af OpenKeychain-appen til brug for fremtidige signaturbekræftelser eller beskeddekrypteringer.

Although this shouldn't be necessary for most email clients, you can attach your public key to a message and if you use *.key* as extension, the mime type will correctly be *application/pgp-keys*.

Al nøglehåndtering uddelegeres af sikkerhedsårsager til OpenKey-kæde appen. Dette betyder også, at FairEmail ikke gemmer PGP-nøgler.

Inline-krypteret PGP i modtagne beskeder understøttes, men inline PGP-signaturer og inline PGP i udgående beskeder understøttes ikke, se [hér](https://josefsson.org/inline-openpgp-considered-harmful.html) for årsagen.

Kun signerede eller kun krypterede beskeder er ikke en god idé. Se her for årsagen:

* [Overvejelser vedr. OpenPGP, del I](https://k9mail.github.io/2016/11/24/OpenPGP-Considerations-Part-I.html)
* [Overvejelser vedr. OpenPGP, del II](https://k9mail.github.io/2017/01/30/OpenPGP-Considerations-Part-II.html)
* [Overvejelser vedr. OpenPGP, del III, Autocrypt](https://k9mail.github.io/2018/02/26/OpenPGP-Considerations-Part-III-Autocrypt.html)

Kun signarede beskeder understøttes, kun krypterede beskeder understøttes ikke.

Almindelige fejl:

* *Ingen nøgle*: Ingen PGP-nøgle tilgængelig for en af de angivne e-mailadresser
* *Manglende krypteringsnøgle*: Der er sandsynligvis valgt en nøgle i FairEmail, der ikke længere forefindes i OpenKeychain-appen. Nulstilling af nøglen (se ovenfor) løser sandsynligvis dette problem.
* *Key for signature verification is missing*: the public key for the sender is not available in the OpenKeychain app. This can also be caused by Autocrypt being disabled in the encryption settings or by the Autocrypt header not being sent.

<br />

*S/MIME*

Den/de offentlige modtagernøgle(r) kræves ved kryptering af en besked. Din private nøgle kræves ved signering af en besked.

Private nøgler gemmes af Android og kan importeres via Androids avancerede sikkerhedsindstillinger. There is a shortcut (button) for this in the encryption settings. Android beder opsætte en PIN-kode, mønster eller adgangskode, hvis dette ikke allerede er gjort. Har du en Nokia-enhed med Android 9, så [læs først dette](https://nokiamob.net/2019/08/10/a-bug-prevents-nokia-1-owners-from-unlocking-their-screen-even-with-right-pin-pattern/).

Bemærk, at certifikater kan indeholde flere nøgler til flere formål, f.eks. til godkendelse, kryptering og signering. Android importerer kun den første nøgle, så for at importere alle nøgler, skal certifikatet først opdeles. Dette er ikke en almindelig forekommende handling, så du rådes til at bede certifikatleverandøren om support.

Bemærk, at selvom S/MIME-signering med andre algoritmer end RSA understøttes, gælder dette måske ikke for andre e-mailklienter. S/MIME encryption is possible with asymmetric algorithms only, which means in practice using RSA.

Standardkrypteringsmetoden er PGP, men den sidst anvendte krypteringsmetode huskes for den valgte identitet til næste gang. Langt tryk på Send-knappen for at ændre en identitets krypteringsmetode. Bruges både PGP- og S/MIME-kryptering for samme e-mailadresse, kan det være nyttigt at kopiere identiteten, så krypteringsmetoden kan ændres ved at vælge en af de to identiteter. Langt tryk på en identitet på identitetslisten (via manuel opsætning i hovedopsætningsskærmen) for at kopiere denne.

For at tillade forskellige private nøgler til den samme e-mailadresse, lader FairEmail dig altid vælge en nøgle, når der er flere identiteter til den samme e-mailadresse for den samme konto.

Public keys are stored by FairEmail and can be imported when verifying a signature for the first time or via the encryption settings (PEM or DER format).

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

<br />

*pretty Easy privacy*

There is still [no approved standard](https://tools.ietf.org/id/draft-birk-pep-00.html) for pretty Easy privacy (p≡p) and not many people are using it.

However, FairEmail can send and receive PGP encrypted messages, which are compatible with p≡p. Also, FairEmail understands incoming p≡p messages since version 1.1519, so the encrypted subject will be shown and the embedded message text will be shown more nicely.

<br />

S/MIME-signering/kryptering er en Pro-funktion, men alle øvrige PGP- og S/MIME-operationer er gratis at benytte.

<br />

<a name="faq13"></a>
**(13) Hvordan fungerer søgning på en enhed/server?**

Du kan begynde at søge efter beskeder efter Afsender (fra), Modtager (til, kopi, bcc), Emne, nøgleord eller beskedtekst vha. forstørrelsesglasset i handlingslbjælken i en mappe. Du kan også søge fra enhver app ved at vælge *Søg e-mail * i popup-menuen kopiér/indsæt.

Søgning i den fælles indbakke udføres i alle mapper på alle konti, søgning i mappelisten udføres kun for den tilknyttede konto og søgning i en mappe udføres kun i dén mappe.

Beskeder søges først på enheden. Der vil være en handlingsknap med et søg igen-ikon i bunden for at fortsætte søgningen på serveren. Du kan vælge, i hvilken mappe, du vil fortsætte søgningen.

IMAP-protokollen understøtter ikke søgning i flere end én mappe samtidigt. Søgning på serveren er en dyr operation, og det er derfor ikke muligt at vælge flere mapper.

Søgning i lokale beskeder er versal/minuskel ufølsom på deltekst. Lokle beskedtekster gennemsøges kun, hvis selve beskedteksterne er blevet downloadet. Søgning på serveren kan være både versal/minuskel følsom eller ufølsom og kan være på deltekst eller hele ord, afhængigt af udbyderen.

Visse servere kan ikke håndtere søgning i beskedtekster ifm. et stort beskedantal. For sådanne tilfælde findes en mulighed for at deaktivere søgning i beskedtekster.

Det er muligt at bruge Gmail-søgeoperatører vha. søgekommandopræfikset *raw:*. Har du kun opsat én Gmail-konto, kan du starte en rå søgning direkte på serveren ved at søge fra den fælles indbakke. Har du opsat flere Gmail-konti, skal du først navigere til mappelisten eller arkivmappen (alle beskeder) for den Gmail-konto, du vil gennemsøge. [Se hér](https://support.google.com/mail/answer/7190) ang. de mulige søgeoperatorer. F.eks.:

`
raw:larger:10M`

Gennemsøgning af et stort antal beskeder på enheden sker ikke særligt hurtigt grundet to begrænsninger:

* [sqlite](https://www.sqlite.org/), Androids databasemotor har en poststørrelsesbegrænsning, der forhindrer, at beskedtekster gemmes i databasen
* Android-apps får kun begrænset hukommelse at arbejde med, selv hvis enheden har masser af hukommelse til rådighed

Dette betyder, at søgning efter en beskedtekst kræver, at filer indeholdende beskedtekster skal åbnes én efter én for at tjekke, om den søgte tekst optræder i filen, hvilket er en relativt dyr proces.

I *diverse indstillinger* kan du aktivere *Byg søgeindeks* for markant at øge søgehastigheden på enheden, men dette øger dog samtidig forbruget af både batteri samt lagerplads. Søgeindekset er baseret på ord, så søgning efter deltekster er ikke muligt. Søgning vha. søgeindekset er som standard OG (AND), så søgning efter *æble appelsin* vil søge efter både æble OG appelsin. Ord adskilt med komma resulterer i en ELLER (OR) søgning, så f.eks. *æble, appelsin* vil søge efter enten æble ELLER appelsin. Begge kan kombineres, så søgning efter *æble, appelsin banan* vil søge efter æble ELLER (appelsin OG banan). Brug af søgeindekset er en Pro-funktion.

Fra version 1.1315 er det muligt at benytte søgeudtryk som dette:

```
æble +banan-kirsebær?nødder
```

Dette vil resultere i en søgning som denne:

```
("æble" OG "banana" OG IKKE "kirsebær") ELLER "nødder"
```

Søgeudtryk kan benyttes søgning på enheden via søgeindekset samt til søgning på e-mailserveren, men af ydelsesårsager ikke til søgning på enheden uden søgeindeks.

Søgning på enheden er en gratis funktion vha. søgeindekset, mens søgning på serveren er en Pro-funktion. Note that you can download as many messages to your device as you like. The easiest way is to use the menu item *Fetch more messages* in the three-dots menu of the start screen.

<br />

<a name="faq14"></a>
**(14) Hvordan opsættes en Outlook-/Live-/Hotmail-konto?**

En Outlook-/Live-/Hotmail-konto kan opsættes via hurtig opsætnings guiden ved valg af *Outlook*.

Anvendelse af en Outlook-, Live- eller Hotmail-konto med tofaktorgodkendelse aktiveret kræver oprettelse af en app-adgangskode. Tjek informationen [hér](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification).

Tjek Microsoft-instruktionerne [hér](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040).

Tjek [denne FAQ](#user-content-faq156) for opsætnig af en Office365-konto.

<br />

<a name="faq15"></a>
**(15) Hvorfor genindlæses beskedteksten hele tiden?**

Beskedhovede og -brødtekst hentes separat fra serveren. Beskedteksten i større beskeder forudindlæses ikke på takserede forbindelser, men hentes efter behov, når en besked udfoldes. Konstant genindlæsning af beskedteksten vil fortsætte, hvis der ikke er forbindelse til kontoen. Tjek også næste spørgsmål, eller hvorvidt andre operationer, såsom beskedsynkronisering, afvikles.

Du kan tjekke konto- og mappelisten for kontoen og mappetilstanden (se forklaringen til betydningen af ikonerne) og operationslisten tilgængelig via hovednavigeringsmenuen til afventende operationer (se [denne FAQ](#user-content-faq3) for betydningen af operationerne).

Har FairEmail stoppet synk grundet forudgående konnektivitetsproblemer, så tjek [denne FAQ](#user-content-faq123). Synkronisering kan også gennemtvinges via trepriksmenuen.

I modtagelsesindstillingerne kan den maksimale størrelse for automatisk download af beskeder på takserede forbindelser indstilles.

Mobilforbindelser er næsten altid takserede, og visse (betalte) Wi-Fi hotspots er det ligeledes.

<br />

<a name="faq16"></a>
**(16) Hvorfor synkroniseres beskeder ikke?**

Mulige årsager til fejl i beskedsynkronisering (sendet eller modtagne) er:

* Konto eller mappe(r) er ikke opsat til at synkronisere
* Der er indstillet for få synkroniseringsdage
* Ingen tilgængelig Internetforbindelse
* E-mailserveren er midlertidigt utilgængelig
* Android har stoppet synkroniseringstjenesten

Tjek derfor dine konto- og mappeindstillinger, og tjek, at konti/mapper er forbundet (se forklaringen i navigeringsmenuen om ikonernes betydning).

Er der nogle fejlmeddelelser, så tjek [denne FAQ](#user-content-faq22).

På visse enheder med en masse apps, som slås om hukommelsen, kan Android muligvis stoppe synkroniseringstjenesten som en sidste udvej.

Visse Android-versioner stopper apps og tjenester for aggressivt. Se [dette dedikerede websted](https://dontkillmyapp.com/) samt [dette Android-problem](https://issuetracker.google.com/issues/122098785) for yderligere information.

Deaktivering af batterioptimering (opsætningstrin 3) mindsker risikoen for, at Android stopper synk-tjenesten.

Ved gentagne forbindelsesfejl pauser FairEmail synk i stadigt længere intervaller for ikke at dræne enhedens batteri. Dette beskrives i [denne FAQ](#user-content-faq123).

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

First of all, **FairEmail is basically free to use** and only some advanced features need to be purchased.

Zuerst, **FairEmail ist grundsätzlich kostenlos** und nur einige erweiterte Funktionen müssen gekauft werden.

Tout d'abord, **FairEmail est au fond gratuit** et seulement quelques fonctionnalités avancées doivent être achetés.

Please see the Play store description of the app or [see here](https://email.faircode.eu/#pro) for a complete list of pro features.

Det rigtige spørgsmål er, "*hvorfor der er så mange skatter og afgifter?*":

* Moms: 25 % (afhængigt af land)
* Google-gebyr: 30 %
* Inkomstskat: 50 %
* <sub>Paypal-gebyr: 5-10 % afhængigt af land/beløb</sub>

Så dét, der er tilbage til udvikleren, er blot en brøkdel af dét, du betaler.

Bemærk også, at de fleste gratis apps ser ud til ikke at være bæredygtige i længden, hvorimod FairEmail vedligeholdes og understøttes korrekt, samt at gratis apps kan have faldgruber, såsom at de lækker fortrolige oplysninger via Internet. There are no privacy violating ads in the app either.

Jeg har arbejdet på FairEmail næsten dagligt i mere end et halvt år, så jeg synes, at prisen er mere end rimelig. Af samme årsag vil der heller ikke være rabatter.

<br />

<a name="faq20"></a>
**(20) Kan jeg få refusion?**

Hvis en købt Pro-funktion ikke fungerer som tilsigtet, og dette ikke skyldes et problem i de gratis funktioner, og jeg ikke kan løse problemet rettidigt, så kan du få en refusion. I alle øvrigee tilfælde er refusion ikke mulig. Under ingen omstændigheder er der mulighed for refusion for noget problem relateret til de gratis funktioner, da der ikke blev betalt noget for dem, og da de kan evalueres uden nogen begrænsning. Jeg tager ansvar som sælger for at levere dét, der er blevet lovet, og jeg forventer, at du tager ansvar for at informere dig om, hvad det er, du køber.

<a name="faq21"></a>
**(21) Hvordan aktiveres notifikationslyset?**

Før Android 8 Oreo: Der findes en avanceret indstilling i opsætningen til dette.

Android 8 Oreo og senere: Tjek [hér](https://developer.android.com/training/notify-user/channels), hvordan notifikationskanaler opsættes. Man kan vis knappen *Standard kanal* i appens notifikationsindstillinger gå til direkte til de relaterede Android-notifikationskanalindstillinger.

Bemærk, at apps ikke længere kan ændre notifikationsindstillinger, herunder indstillinger for notifikationslys, på Android 8 Oreo og senere.

Det er undertiden nødvendigt at deaktivere indstillingen *Vis beskedforhåndsvisning i notifikationer* eller at aktivere indstillingerne *Vis kun notifikationer med en forhåndsvisningstekst* for at omgå fejl i Android. Dette gælder muligvis også notifikationslyde og vibrationer.

Indstilling af lys-farven før Android 8 understøttes ikke og er ikke muligt på Android 8 og senere.

<br />

<a name="faq22"></a>
**(22) Hvad betyder konto-/mappefejl ...?**

FairEmail skjuler ikke fejl (hvilket lignende apps ofte gør), så det er lettere at diagnosticere problemer.

FairEmail forsøger automatisk at genoprette forbindelse efter en forsinkelse. Denne forsinkelse fordobles efter hvert mislykket forsøg for at forhindre batteridræning samt at blive låst ude permanent. Tjek [denne FAQ](#user-content-faq123) for yderligere information.

Der er generelle såvel som specifikke fejl for Gmail-konti (se nedenfor).

**Generelle fejl**

<a name="authfailed"></a>
The error *... **Authentication failed** ...* or *... GODKENDELSE mislykkedes ...* skyldes sandsynligvis forkert brugernavn/adgangskode. Viss udbydere forventer som brugernavn blot *brugernavn* og andre din fulde e-mail *brugernavn@eksempel.dk*. Benyttes kopiér/indsæt til angivelse af brugernavn/adgangskode kan der muligvis medtages usynlige tegn, hvilket også kan forårsage denne fejl. Visse adgangskodehåndteringer er kendt for at gøre dette forkert også. Brugernavnet kan være minuskel/versal sensitivt, så prøv kun med minuskler. Adgangskoden er fortrinsvis minuskel/versal sensitivt. Visse udbydere kræver brug af en app-adgangskode i stedet for kontoadgangskoden, så tjek din leverandørs dokumentation. Det nogle gange nødvendigt først at muliggøre ekstern adgang (IMAP/SMTP) på udbyderens websted. Øvrige mulige årsager er, at kontoen er blokeret, eller at indlogning er administrativt begrænset på en eller anden måde, f.eks. ved kun at tillade indlogning fra bestemte netværk/IP-adresser.

Om nødvendigt, kan en adgangskode opdateres i kontoindstillingerne: Navigeringsmenu (venstre sidemenu) > *Indstillinger* > *Manuel opsætning* > *Konti* > relevant konto. Changing the account password will in most cases automatically change the password of related identities too. If the account was authorized with OAuth via the quick setup wizard instead of with a password, you can run the quick setup wizard again and tick *Authorize existing account again* to authenticate the account again. Note that this requires a recent version of the app.

Fejlen *... For mange fejlede godkendelsesforsøg ... *betyder sandsynligvis, at du bruger en Yahoo-kontoadgangskode i stedet for en app ditto. Tjek [denne FAQ](#user-content-faq88) vedr. opsætning af en Yahoo-konto.

Meddelelsen *... + OK ...* betyder sandsynligvis, at en POP3-port (normalt portnummer 995) anvendes til en IMAP-konto (normalt ellers portnummer 993).

Fejlene *... ugyldig hilsen ...*, *... kræver gyldig adresse ...* og *... Parameter til HELO overholder ikke RFC-syntaks ...* kan sandsynligvis løses ved at ændre den avancerede identitetsindstilling *Anvend lokal IP-adresse i stedet for værtsnavn*.

Fejlen *... Kunne ikke oprette forbindelse til vært...* betyder, at der ikke var noget svar fra e-mailserveren inden for en rimelig tid (20 sekunder som standard). Dette indikerer i reglen Internetforbindelsesproblemer, muligvis forårsaget af en VPN- eller firewall-app. Du kan forsøge at øge timeout for forbindelsen i forbindelsesindstillingerne for FairEmail, til når e-mailserveren er virkelig langsom.

Fejlen *... Forbindelse nægtet ...* betyder, at e-mailserveren eller noget mellem e-mailserveren og appen, såsom en firewall, aktivt afviste forbindelsen.

Fejlen *... Netværk utilgængeligt ...* betyder, at e-mailserveren ikke kunne nås via den aktuelle Internetforbindelse, f.eks. fordi Internettrafik er begrænset til alene lokal trafik.

Fejlen *... Vært er uopløst ...*, "*... Kan ikke opløse vært...* eller *... Ingen adresse tilknyttet værtsnavn ...* betyder, at adressen på e-mailserveren ikke kunne opløses til en IP-adresse. Dette kan skyldes et VPN, adblocking eller en utilgængelig/ikke korrekt fungerende (lokal) [DNS-server](https://en.wikipedia.org/wiki/Domain_Name_System).

Fejlen *... Software forårsaget forbindelsesafbrydelse ...* betyder, at e-mailserveren, eller noget mellem FairEmail og e-mailserveren, aktivt afsluttede en eksisterende forbindelse. Dette kan f.eks. ske, når tilslutningen pludselig blev mistet. Et typisk eksempel er aktivering af Flytilstand.

Fejlene *... BYE, logger ud ...*, *... Forbindelse nulstillet ...* betyder, at e-mailserveren eller noget mellem denne og appen, såsom en router eller firewall(-app), aktivt afsluttede forbindelsen.

Fejlen *... Forbindelse lukket af peer ...* kan forårsages af en ikke-opdateret Exchange-server, se [hér](https://blogs.technet.microsoft.com/pki/2010/09/30/sha2-and-windows/) for yderligere oplysninger.

Fejlene *... Læsefejl ...*, *... Skrivefejl ...*, *... Læsning fik timeout ...*, *... Ødelagt pipe ...* betyder, at e-mailserveren ikke længere svarer, eller at Internetforbindelsen er ringe.

<a name="connectiondropped"></a>
Fejlen *... Forbindelse droppet af server? ...* betyder, at e-mailserveren uventet afsluttede forbindelsen. Dette sker af og til ved for mange forbindelser på for kort tid, eller ved brug af en forkert adgangskode for mange gange. I så tilfælde bør man sikre sig, at adgangskoden er korrekt, deaktivere modtagelse i modtagelsesindstillingerne i omkring 30 minutter og forsøge igen. Hvis nødvendigt, tjek [denne FAQ](#user-content-faq23) om, hvordan forbindelsesantallet kan reduceres.

Fejlen *... Uventet afslutning af zlib-inputstrøm ...* betyder, at ikke alle data blev modtaget, muligvis grundet en dårlig/afbrudt forbindelse.

Fejlen *... forbindelsesfejl ...* kan indikere [For mange samtidige forbindelser](#user-content-faq23).

Advarslen *... Uunderstøttet kodning ...* betyder, at beskedens tegnsæt er ukendt eller uunderstøttet. FairEmail benytter generelt ISO-8859-1 (Latin1), hvilket i de fleste tilfælde vil resultere i korrekte beskedvisninger.

Fejlen *... Login Rate Limit Hit ...* means that there were too many login attempts with an incorrect password. Please double check your password or authenticate the account again with the quick setup wizard (OAuth only).

Fejlen *... NO mailbox selected READ-ONLY ...* indicates [this Zimbra problem](https://sebastian.marsching.com/wiki/Network/Zimbra#Mailbox_Selected_READ-ONLY_Error_in_Thunderbird).

[Se hér](#user-content-faq4) for fejlene *... Ikke-betroet ... ikke i certifikat ...*, * ... Ugyldigt sikkerhedscertifikat (kan ikke bekræfte serveridentitet) ...* eller *... Betroet anker til certificeringssti ikke fundet ...*

[Se hér](#user-content-faq127) vedr. fejlen *... Syntaktisk ugyldigt HELO-argument(er) ... *.

[Se hér](#user-content-faq41) vedr. fejlen *... Handshake mislykkedes ...*.

Se [hér](https://linux.die.net/man/3/connect) ang. betydningen af fejlkoder såsom EHOSTUNREACH og ETIMEDOUT.

Mulige årsager:

* Firewall eller router blokerer forbindelser til serveren
* Værtsnavnet eller portnummeret er ugyldigt
* Der er problemer med denne Internetforbindelse
* Der er problemer med at opløse domænenavne (Yandex: Prøv at deaktivere privat DNS i Android-indstillingerne)
* E-mailserveren nægeter at acceptere (eksterne) forbindelser
* E-mai-serveren nægter at acceptere en besked, fordi den f.eks. er for stor eller indeholder uacceptable links
* Der er for mange forbindelser til serveren, se også næste spørgsmål

Mange offentlige Wi-Fi netværk blokerer udgående e-mail for at forhindre spam. Dette kan somme tider omgås ved brug af en anden SMTP-port. Se leverandørdokumentationen ang. anvendelige portnumre.

Benytter du et [VPN](https://en.wikipedia.org/wiki/Virtual_private_network), kan VPN-udbyderen muligvis blokere forbindelsen, hvis den for aggressivt forsøger at forhindre spam. Bemærk, at [Google Fi](https://fi.google.com/) også benytter et VPN.

**Afsendelsesfejl**

SMTP-servere kan afvise beskeder [af forskellige årsager](https://en.wikipedia.org/wiki/List_of_SMTP_server_return_codes). For store beskeder og udløsning af en e-mailservers spamfilteret er de mest almindelige årsager.

* Gmails størrelsesbegrænsning for vedhæftninger [udgør 25 MB](https://support.google.com/mail/answer/6584)
* Outlooks og Office 365' størrelsesbegrænsning for vedhæftninger [udgør 20 MB](https://support.microsoft.com/en-us/help/2813269/attachment-size-exceeds-the-allowable-limit-error-when-you-add-a-large)
* Yahoos størrelsesbegrænsning for vedhæftninger [udgør 25 MB](https://help.yahoo.com/kb/SLN5673.html)
* For *554 5.7.1 Tjeneste utilgængelig; Klient vært xxx.xxx.xxx.xxx blokeret*, tjek venligst [hér](https://docs.gandi.net/en/gandimail/faq/error_types/554_5_7_1_service_unavailable.html)
* *501 Syntaksfejl - linje for lang* er ofte forårsaget af brug af en lang Autocrypt-header
* *503 5.5.0 Modtager allerede angivet* betyder typisk, at en adresse bruges som både TO- og CC-adresse
* *554 5.7.1 ... ikke tilladt at videresende* betyder, at e-mailserveren ikke genkender brugernavnet/e-mailadressen. Dobbelttjek værtsnavn og brugernavn/e-mailadresse i identitetsindstillingerne.
* *550 Spam besked afvist, da IP er listet af ...* betyder, at e-mailserveren har afvist at afsende en besked fra den aktuelle (offentlige) netværksadresse, fordi den tidligere har være misbrugt til spamafsendelse. Prøv at aktivere flytilstand i 10 minutter for at få tildelt en ny netværksadresse.
* *550 Beklager, din e-mail kan ikke afsendes. Either the subject matter, a link, or an attachment potentially contains spam, or phishing or malware.* means that the email provider considers an outgoing message as harmful.
* *571 5.7.1 Besked indeholder spam eller virus eller afsender er blokeret ...* betyder, at e-mailserveren betragtede en udgående besked som spam. Dette betyder sandsynligvis, at e-mailserverens spamfiltre er for strikse. Kontakt e-mailudbyderen for support vedr. dette.
* *451 4.7.0 Temporary server error. Please try again later. PRX4 ...*: Tjek [hér](https://www.limilabs.com/blog/office365-temporary-server-error-please-try-again-later-prx4) eller [hér](https://judeperera.wordpress.com/2019/10/11/fixing-451-4-7-0-temporary-server-error-please-try-again-later-prx4/).
* *571 5.7.1 Videresendelsesadgang nægtet*: Dobbelttjek brugernavn og e-mailadresse i de avancerede identitetsindstillinger (via den manuelle opsætning).

Ønskes Gmail SMTP-serveren brugt mhp. at omgå et for strikst, udgående spamfilter eller til at forbedre beskedleveringen:

* Bekræft din e-mailadresse [hér](https://mail.google.com/mail/u/0/#settings/accounts) (en computerbrowser skal bruges til dette)
* Skift af identitetsindstillinger (Indstillinger > Manuel opsætning > Identiteter > identitet):

&emsp;&emsp;Username: *ens Gmail-adresse*<br /> &emsp;&emsp;Password: *[en app-adgangskode](#user-content-faq6)*<br /> &emsp;&emsp;Host: *smtp.gmail.com*<br /> &emsp;&emsp;Port: *465*<br /> &emsp;&emsp;Encryption: *SSL/TLS*<br /> &emsp;&emsp;Reply to address: *ens e-mailaddresse* (avanceret identititetsindstillinger)<br />

<br />

**Gmail-fejl**

Godkendelsen af Gmail-kontiopsætninger vha. hurtigguiden skal periodisk opfriskes via [Android-kontohåndteringen](https://developer.android.com/reference/android/accounts/AccountManager). Dette kræver kontakt-/konto-tilladelser samt Internetforbindelse.

I tilfælde af fejl er det muligt igen at godkende/gendanne en Gmail-konto via Gmail-guiden til hurtigopsætning.

Fejlen *... Godkendelse mislykkedes ... Konto ikke fundet ...* betyder, at en tidligere godkendt Gmail-konto er blevet fjernet fra enheden.

Fejlene *... Godkendelse mislykkedes ... Ingen token ...* betyder, at Android-kontohåndteringen ikke kunne opfriske godkendelsen af en Gmail-konto.

Fejlen *... Ugyldige akkreditiverer ... netværksfejl ...* betyder, at Android-kontohåndteringen var ude af stand til at opfriske godkendelsen af en Gmail-konto grundet Internetforbindelsesproblemer

Fejlen *... Godkendelse mislykkedes ... Ugyldige akkreditiver ...* kan være forårsaget af et skift af kontoadgangskoden eller ophævelse af de krævede konto-/kontakttilladelser. Er kontoadgangskoden skiftet, så godkend Google-kontoen i indstillingerne for Android-kontoen igen. I tilfælde af at tilladelserne er tilbagekaldt, kan du starte hurtigopsætningsguiden til Gmail for at gentildele de nødvendige tilladelser (du behøver ikke opsætte kontoen igen).

Fejlen *... ServiceDisabled ...* kan skyldes din tilmelding til [Avanceret Beskyttelsesprogram](https://landing.google.com/advancedprotection/): "*For at læse din e-mail, skal du benytte Gmail - Du kan ikke benytte din Google-konto med apps og tjenester, som kræver adgang til følsomme data såsom dine e-mails*", se [hér](https://support.google.com/accounts/answer/7519408?hl=en&ref_topic=9264881).

The error *... 334 ... OAUTH2 asked for more ...* probably means that the account needs to be authorized again, which you can do with the quick setup wizard in the settings.

Hvis du er i tvivl, kan du anmode om [support](#user-content-support).

<br />

<a name="faq23"></a>
**(23) Hvorfor ses advarslen ... ?**

*Generelt*

Avarsler er advarselsmeddelelser sendt af e-mailservere.

*For mange samtidige forbindelser* eller *Maksimalt antal forbindelser overskred*

Denne advarsel udsendes, når der er for mange mappeforbindelser til den samme e-mail-konto på samme tid.

Mulige årsager:

* Flere e-mailklienter er forbundet til den samme konto
* Samme e-mailklient er forbundet flere gange til den samme konto
* Tidligere forbindelser blev brat afsluttet, f.eks. ved en pludselig mistet Internetforbindelse

Prøv først at vente lidt for at se, om problemet løser sig selv, ellers:

* Skift enten til periodisk tjek for beskeder i modtagelsesindstillingerne, hvilket resulterer i, at mapper åbnes én ad gangen
* Eller indstil nogle mapper til polling i stedet for at synkronisation (langt tryk på mappen i mappelisten, redigér egenskaber)

En nem måde at opsætte periodisk tjek for beskeder for alle mapper undtagen indbakken er at bruge *Anvend for alle . .* i trepriksmenuen på mappelisten og at markere de to nederste afkrydsningsfelter.

Det maksimale antal samtidige mappeforbindelser til Gmail udgør 15, så du kan synkronisere maks. 15 mapper samtidigt på tværs af *alle* dine enheder. Af samme grund er Gmail *brugermapper* som standard opsat til polling fremfor altid at synkronisere. Om nødvendigt eller ønsket, kan dette ændres vha. langt tryk på en mappe i mappelisten og vælge *Redigér egenskaber*. Tjek oplysningerne [hér](https://support.google.com/mail/answer/7126229).

Ved brug af en Dovecot-server, skal indstillingen [mail_max_userip_connections](https://doc.dovecot.org/settings/dovecot_core_settings/#mail-max-userip-connections) muligvis ændres.

Bemærk, at det vil tage e-mailserver et stykke tid at opdage brudte forbindelser, f.eks. grundet ophør af netværksdækning, hvilket betyder, at kun halvdelen af mappeforbindelserne reelt er tilgængelige. For Gmail vil det kun være 7 forbindelser.

<br />

<a name="faq24"></a>
**(24) Hvad betyder gennemse beskeder på serveren?**

Gennemse beskeder på serveren henter beskeder fra e-mailserveren i realtid, når du kommer til slutningen af listen over synkroniserede beskeder, også når mappen er indstillet til ikke at synkronisere. Du kan deaktivere denne funktion under Avancerede kontoindstillinger.

<br />

<a name="faq25"></a>
**(25) Hvorfor kan der ikke vælges/åbnes/gemmes et billede, vedhæftning eller fil?**

Når et menupunkt til at vælge/åbne/gemme en fil er deaktiveret (nedtonet), eller når du ser meddelelsen *Lageradgangs-framework ikke tilgængelig*, så er [lageradgangs-framework'en](https://developer.android.com/guide/topics/providers/document-provider), en standard Android-komponent, sandsynligvis ikke til stede. Dette kan skyldes, at din brugerdefinerede ROM ikke inkluderer den, eller fordi den er blevet fjernet (debloated).

FairEmail anmoder ikke om lagerpladstilladelser, så denne framework kræves for at kunne vælge filer og mapper. Ingen app, undtagen måske filhåndteringer målrettet Android 4.4 KitKat eller senere, bør anmode om lagerpladstilladelser, da dette giver adgang til *alle* filer.

Framework for lagerpladsadgang leveres af pakken *com.android.documentsui*, der er synlig som *Fil-*app i visse Android-versioner (specifikt OxygenOS).

Du kan aktivere framework'en for lagerpladsadgang (igen) med denne adb-kommando:

```
pm install -k --user 0 com.android.documentsui
```

Alternativt kan du muligvis aktivere *Fil-*appen igen vha. Android app-indstillingerne.

<br />

<a name="faq26"></a>
**(26) Kan jeg hjælpe med at oversætte FairEmail til mit sprog?**

Ja, du kan oversætte FairEmail-teksterne til dit sprog [via Crowdin](https://crowdin.com/project/open-source-email). Tilmelding er gratis.

Ønsker du dit navn/alias inkluderet på listen over bidragsydere i appens *Om*-afsnit, så [kontakt mig](https://contact.faircode.eu/?product=fairemailsupport) venligst.

<br />

<a name="faq27"></a>
**(27) Hvordan skelnes mellem indlejrede eller eksterne billeder?**

Eksternt billede:

![Eksternt billede](https://github.com/M66B/FairEmail/blob/master/images/baseline_image_black_48dp.png)

Indlejret billede:

![Indlejret billede](https://github.com/M66B/FairEmail/blob/master/images/baseline_photo_library_black_48dp.png)

Ødelagt billede:

![Defekt billede](https://github.com/M66B/FairEmail/blob/master/images/baseline_broken_image_black_48dp.png)

Bemærk, at download af eksterne billeder fra en fjernserver kan bruges til at registrere, at du har set en besked, hvilket du sandsynligvis ikke ønsker, såfremt beskeden er spam eller ondsindet.

<br />

<a name="faq28"></a>
**(28) Hvordan håndteres statusbjælkenotifikationer?**

I opsætningen findes knappen *Håndtér notifikationer* til at gå direkte til Android-notifikationsindstillingerne for FairEmail.

I Android 8.0 Oreo og senere kan du håndtere egenskaberne for de individuelle notifikationskanaler, f.eks. indstilling af en bestemt notifikationslyd eller for at vise notifikationer på låseskærmen.

FairEmail har flg. beskedkanaler:

* Tjeneste: Benytttes til notifikation om synkroniseringstjenesten, tjek også [denne FAQ](#user-content-faq2)
* Send: Benyttes til sendetjenestenotiifikation
* Notifikationer: Benyttes til notifikation om ny besked
* Advarsel: Benyttes til advarselsnotifikationer
* Fejl: Benyttes til notifikationer om fejl

Se [hér](https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels) for detaljer om notifikationskanaler. Kort sagt: Tryk på notifikationskanalens navn for at få adgang til kanalindstillingerne.

På Android før Android 8 Oreo kan du opsætte notifikationslyden i indstillingerne.

Se [denne FAQ](#user-content-faq21), hvis din enhed har et notifikationslys.

<br />

<a name="faq29"></a>
**(29) Hvordan fås notifikationer om nye beskeder for andre mapper?**

Benyt blot et langt tryk på en mappe, vælg *Redigér egenskaber*, og aktivér enten *Vis i den fælles indbakke* eller *Notificér om nye beskeder* (kun tilgængelig på Android 7 Nougat og senere) og tryk på *Gem*.

<br />

<a name="faq30"></a>
**(30) Hvordan kan de medfølgende hurtige indstillinger benyttes?**

Der er hurtige indstillinger (indstillingsfliser) tilgængelig for:

* globalt aktivere/deaktivere synkronisering
* vise antallet af nye beskeder og markere dem som set (ikke læst)

Hurtige indstillinger kræver Android 7.0 Nougat eller senere. Brug af indstillingsfliserne forklares [hér](https://support.google.com/android/answer/9083864).

<br />

<a name="faq31"></a>
**(31) Hvordan kan de medfølgende genveje benyttes?**

Der findes genveje til oprettelse af en ny besked til en favoritkontakt.

Genveje kræver Android 7.1 Nougat eller senere. Brug af genveje forklares [hér](https://support.google.com/android/answer/2781850).

Det er også muligt at oprette genveje til mapper med et langt tryk på en mappe i kontolisten og valg af *Tilføj genvej*.

<br />

<a name="faq32"></a>
**(32) Hvordan tjekkes, om læsning af e-mails virkelig er sikkert?**

Man kan benytte [E-mail Fortrolighedstester](https://www.emailprivacytester.com/) til dette.

<br />

<a name="faq33"></a>
**(33) Hvorfor fungerer redigerede afsenderadresser ikke?**

For at forhindre spam accepterer de fleste udbydere kun bekræftede adresser ifm. afsendelser.

F.eks. ændrer Google beskedhoveder som dette for *ikke-bekræftede* adresser:

```
Fra: Nogen <somebody@example.org>
X-Google-Originale-Fra: Nogen <somebody+extra@example.org>
```

Dette betyder, at den redigerede afsenderadresse automatisk erstattes af en bekræftet adresse inden bekedafsendelsen.

Bemærk, at dette er uafhængigt af at modtage beskeder.

<br />

<a name="faq34"></a>
**(34) Hvordan matches identiteter?**

Identiteter matches som forventet efter konto. For indgående beskeder adresserne *til*, *cc*, *bcc*, *fra* og *(X-)leverede/konvolut/originale-til* blive tjekket (i denne rækkefølge) og for udgående beskeder (kladder, udbakke og sendt) tjekkes kun *fra*-adresserne. Matchende adresser har forrang frem for delvist matchende ditto, undtagen *leveret til*-adresser.

Den matchede adresse vil blive vist som *via* i adresseafsnittet for modtagne beskeder (mellem beskedens header og beskedens tekst).

Bemærk, at identiteter skal aktiveres for at kunne matches, osamt at identiteter på andre konti ikke overvejes.

Match udføres kun én gang efter modtagelse af en besked, så ændring af opsætningen ændrer ikke eksisterende beskeder. Du kan dog rydde lokale beskeder vha. langt tryk på en mappe i mappelisten og synkronisere beskederne igen.

Det er muligt at opsætte en [regex](https://en.wikipedia.org/wiki/Regular_expression) i identitetsindstillingerne for at få en e-mailadresses matchende **brugernavn** (delen før @-tegnet).

Bemærk, at domænenavnet (tekst/tegn efter @) altid skal være identisk med identitetens domænenavn.

Vil man gerne matche en fang-alle e-mailadresse, er denne regex for det meste OK:

```
.*
```

Vil du matche e-mailadresserne til specielle formål, abc@eksemepel.dk og xyx@eksemepel.dk og også gerne have en fallback e-mailadresse, hoved@eksemepel.dk, kan du gøre noget ala dette:

* Identity: abc@eksempel.dk; regex: **(?i)abc**
* Identity: xyz@eksempel.dk; regex: **(?i)xyz**
* Identity: hoved@eskempel.dk; regex: **^(?i)((?!abc|xyz).)\*$**

Matchede identiteter kan benyttes til beskedfarvekodning. Identitetsfarven har forrang over mappe- og kontofarver. Brug af identitetsfarver er en Pro-funktion.

<br />

<a name="faq35"></a>
**(35) Hvorfor skal man være forsigtig med at få vist billeder, vedhæftninger, originalbeskeder og åbne links?**

Visning af fjernlagrede billeder (se også [denne FAQ](#user-content-faq27)) og åbning af links fortæller muligvis ikke kun afsenderen, at du har set beskeden, men lækker også din IP-adresse. Se også dette spørgsmål: [Hvorfor et e-mail link er farligere end et websøgningslink?](https://security.stackexchange.com/questions/241139/why-emails-link-is-more-dangerous-than-web-searchs-link).

Åbning af vedhæftninger eller visning af en original besked kan muligvis indlæse eksternt indhold og eksekvere scripts, hvilket ikke alene kan forårsage læk af fortrolige informationer, men tillige udgøre en sikkerhedsrisiko.

Bemærk, at dine kontakter, uden deres vidende, kan sende ondsindede beskeder, hvis de er blevet inficeret med malware.

FairEmail genformaterer beskeder, hvilket får dem til at se anderledes ud end originalen, men også afslører phishing-links.

Bemærk, at genformaterede beskeder ofte er mere læsbare end originalerne, da margerne er fjernet, og skrifttypefarver og -størrelser er standardiserede.

Gmail-appen viser som standard billeder ved at downloade disse via en Google-proxyserver. Da billederne downloades fra kildeserveren [i realtid](https://blog.filippo.io/how-the-new-gmail-image-proxy-works-and-what-this-means-for-you/), er dette endnu mindre sikkert, da Google uden nogen større fordel også er involveret.

Du kan som standard få vist billeder og originale beskeder for betroede afsendere fra gang til gang ved at markere *Spørg ikke igen om ...*.

Vil du nulstille standard *Åbn med* apps, så tjek [hér](https://support.google.com/pixelphone/answer/6271667).

<br />

<a name="faq36"></a>
**(36) Hvordan krypteres indstillingsfiler?**

Kort version: AES 256 bit

Lang version:

* 256 bit nøglen genereres med *PBKDF2WithHmacSHA1* vha. en 128 bit, sikker tilfældigt salt samt 65.536 iterationer
* Cipher'en er *AES/CBC/PKCS5Padding*

<br />

<a name="faq37"></a>
**(37) Hvordan lagres adgangskoderr?**

Alle understøttede Android-versioner [krypterer alle brugerdata](https://source.android.com/security/encryption), så alle data, inkl. brugernavne, adgangskoder, beskeder mv., lagres krypteret.

Er enheden sikret med en PIN-kode, mønster eller adgangskode, kan konto- og identitetsadgangskoder gøres. synlige. Er dette er et problem, fordi enheden deles med andre, så overvej at anvende [brugerprofiler](https://www.howtogeek.com/333484/how-to-set-up-multiple-user-profiles-on-android/).

<br />

<a name="faq39"></a>
**(39) Hvordan kan FairEmails batteriforbrug reduceres?**

Som standard rapporterer nyere Android-versioner *app-brug* som en procentdel på Android-batteriindstillingssiden. **Forvirrende nok er *app-brug* ikke det samme som *batteriforbrug* og er ikke engang direkte relateret til batteriforbrug!** App-brugen (under i brug) vil være meget høj, da FairEmail bruger en forgrundstjeneste, der af Android betragtes som konstant app-brug. Dette betyder dog ikke, at FairEmail konstant forbruger strøm. Det reelle stømforbrug kan ses ved at gå til denne side:

*Android-indstillinger*, *Batteri*, trepriksmenu *Batteriforbrug*, trepriksmenu *Vis fuld enhedsbrug*

Som en tommelfingerregel skal strømforbruget være under eller i hvert fald ikke meget højere end for *Mobilnetværks-standby*. Er dette ikke tilfældet, så aktivé *Automatisk optimering* i modtagelsesindstillingerne. Hjælper dette ikke, så [anmod om support](https://contact.faircode.eu/?product=fairemailsupport).

Det er uundgåeligt, at synkronisering af bbeskeder forbruger strøm, da det kræver adgang til både netværket og beskeddatabasen.

Sammenlignes batteriforbruget for FairEmail med en anden e-mailklient, skal den anden e-mailklient er opsat på lignende vis. Det er eksempelvis ikke retvisende at sammenligne kontinuerlig syn (push-beskeder) og periodisk (ikke-regelmæssig) kontrol for nye beskeder.

Gentilslutninger til en e-mailserver forbruger ekstra strøm, hvilket f.eks. en ustabil Internetforbindelse vil forårsage. Visse e-mailservere afslutter også ikke-aktive forbindelser for tidligt, selvom [standarden](https://tools.ietf.org/html/rfc2177) dikterer, at en ikke-aktiv forbindelse bør holdes åben i 29 minutter. I sådanne tilfælde vil du måske kun synkronisere periodisk, f.eks. hver time, i stedet for kontinuerligt. Bemærk, at hyppig polling (udover hvert ca. 30.-60. min) antageligt vil forbruge mere strøm end kontinuerlig synkronisering, da serverforbindelsesoprettelsen samt lokal-/fjernbeskeds matchning kræver ressourcer.

[På visse enheder](https://dontkillmyapp.com/) er det nødvendigt at *deaktivere* batterioptimering (opsætningstrin 3) for at holde e-mailserverforbindelser åbne. Faktisk kan aktiv batterioptimering resultere i ekstra strømforbrug på alle enheder, selvom det lyder selvmodsigende!

Størstedelen af strømforbruget, fraset at se beskeder, skyldes synkronisering (modtagelse/afsendelse) af beskeder. For at reducere strømforbruget så indstil antallet af synkroniseringsdage til en lavere værdi, især hvis der er en masse nylige beskeder i en mappe. Langt tryk på et mappenavn i mappelisten, og valg af *Redigér egenskaber* giver adgang til denne indstilling.

Har du mindst én gang om dagen Internetforbindelse, er det tilstrækkeligt at synkronisere beskeder blot for én dag.

Bemærk, det antal dage, hvori beskeder *beholdes* maksimalt kan indstilles til det antal dage, hvori beskeder *synkroniseres*. Indledningsvis bør man f.eks. synke beskeder i et stort antal dage, og efter at dette er sket, reducere antallet af beskedsynkdage, men bevare antallet af dage, hvori beskeder beholdes. Efter reducering af det antal dage, hvori beskeder beholdes, ønsker man måske via Diverse indstillinger at køre en oprydning for at fjerne gamle filer.

I modtagelsesindstillingerne kan du aktivere altid at synkronisere stjernemarkerede beskeder, hvilket lader dig beholde ældre beskeder, selvom du synkroniserer beskeder i et begrænset antal dage.

Deaktivering af mappeindstillingen *Download automatisk beskedtekster og vedhæftninger* vil betyde mindre netværkstrafik og dermed mindre strømforbrug. Du kan deaktivere denne indstilling for eksempelvis Sendt-mappen og arkivet.

Synkronisering af beskeder om natten er for det meste unødvendigt, så du kan spare på strømmen ved ikke at gøre dette. Du kan i indstillingerne vælge en tidsplan for beskedsynkronisering (dette er en Pro-funktion).

FairEmail vil som standard synkronisere mappelisten ved hver tilslutning. Da mapper stort set ikke oprettes, omdøbes og slettes særligt ofte, kan du spare på netværks- og strømforbruget ved at deaktivere dette i modtagelsesindstillingerne.

Som standard vil FairEmail ved hver tilslutning tjekke, om gamle beskeder blev slettet fra serveren. Hvis du ikke har noget imod, at gamle beskederr, som blev slettet fra serveren stadig er synlige i FairEmail, kan du spare netværks- og strømforbrug ved at deaktivere dette i modtagelsesindstillingerne.

Visse udbydere følger ikke IMAP-standarden og holder ikke forbindelserne åbne længe nok. Det tvinger FairEmail til ofte at oprette forbindelse igen, hvilket medfører ekstra strømforbrug. Du kan tjekke *Loggen* via hovednavigeringsmenuen for at se, om der er hyppige forbindelsesgenoprettelser (forbindelse lukket/nulstillet, læse-/skrivefejl/timeout mv.). Du kan omgå dette ved i de avancerede kontoindstillinger at sænke keep-alive intervallet til f.eks. 9 eller 15 min. Bemærk, at batterioptimering skal deaktiveres i opsætningstrin 3 for pålideligt at holde forbindelser i live.

Visse udbydere sender hvert 2. minut noget i retning af et '*Stadig her*', hvilket resulterer i netværkstrafik samt vækning af din enhed og forårsager unødigt ekstra strømforbrug. Du kan tjekke *Loggen* via hovednavigeringsmenuen for at se, hvorvidt din udbyder gør dette. Benytter din udbyder [Dovecot](https://www.dovecot.org/) som IMAP-server, kan du bede din udbyder om at ændre indstillingen [imap_idle_notify_interval](https://wiki.dovecot.org/Timeouts) til en højere værdi eller endnu bedre, deaktivere dette. Er din udbyder ikke er i stand til eller villig til at ændre/deaktivere dette, så overvej at skifte til periodisk i stedet for kontinuerlig synkronisering. Du kan ændre dette i modtagelsesindstillingerne.

Ser du under opsætningen af en konto meddelelsen *Denne udbyder understøtter ikke push-beskeder*, så overvej at skifte til en moderne udbyder, der understøtter push-beskeder (IMAP IDLE) for at reducere strømforbruget.

Har din enhed en [AMOLED](https://en.wikipedia.org/wiki/AMOLED)-skærm, kan du ved at skifte til det sorte tema spare strøm, mens du ser beskeder.

Er autooptimering i modtagelsesindstillingerne aktiveret, omskiftes en konto automatisk til periodisk tjek for nye beskeder, når e-mailserveren:

* Sender et '*Stadig her*' inden for 3 minutter
* Ikke understøtter push-beskeder
* Keep-alive intervallet er kortere end 12 min

Desuden vil Papirkurv- og Spam-mapperne automatisk blive indstillet til at tjekke for nye beskeder efter tre successive [for mange samtidige forbindelser](#user-content-faq23)-fejl.

<br />

<a name="faq40"></a>
**(40) Hvordan kan FairEmails dataforbrug reduceres?**

Man kan grundlæggende reducere netværksforbruget på samme måde som strømforbruget. Tjek forslagene ifm. foregående spørgsmål.

Det er uundgåeligt, at data vil blive forbrugt ved meddelelses-synk.

Mistes forbindelsen til e-mail-serveren, vil FairEmail altid gen-synke beskederne for at sikre, at de alle er tilgængelige. Er forbindelsen ustabil, kan dette medføre ekstra dataforbrug. I så tilfælde er det en god idé at reducere antallet af beskedsynkdage til et minimum (tjek foregående spørgsmål) eller at skifte til periodisk beskedsynk (modtagelsesindstillinger).

For at reducere dataforbrug, kan disse avancerede modtagelsesindstillinger ændres:

* Tjek, om gamle beskeder er fjernet fra serveren
* Synk (delte) mappelister: Deaktivér

Som standard henter FairEmail ikke beskedtekster og vedhæftninger større end 256 KiB, når Internetforbindelsen er takseret (mobildata eller betalt Wi-Fi). Du kan ændre dette i forbindelsesindstillingerne.

<br />

<a name="faq41"></a>
**(41) Hvordan rettes fejlen 'Handshake mislykkedes'?**

Der er flere mulige årsager, så tjek slutningen af dette svar.

Fejlen '*Handshake mislykkedes ... WRONG_VERSION_NUMBER ...*' kan betyde, at du forsøger at oprette forbindelse til en IMAP- eller SMTP-server uden en krypteret forbindelse, typisk via port 143 (IMAP) og port 25 (SMTP), eller at der anvendes en forkert protokol (SSL/TLS eller STARTTLS).

De fleste udbydere leverer krypterede forbindelser vha. forskellige porte, typisk port 993 (IMAP) og port 465/587 (SMTP).

Understøtter din udbyder ikke krypterede forbindelser, bør du anmode om, at dette gøres muligt. Er dette ikke en mulighed, kan du aktivere *Tillad usikre forbindelser* i både de avancerede indstillinger OG konto-/identitetsindstillingerne.

Se også [denne FAQ](#user-content-faq4).

Fejlen '*Handshake mislykkedes ... SSLV3_ALERT_ILLEGAL_PARAMETER ...*' er enten forårsaget af en fejl i implementeringen af SSL-protokollen eller ved en for kort DH-nøgle på e-mailserveren og kan desværre ikke rettes af FairEmail.

Fejlen '*Handshake mislykkedes ... HANDSHAKE_FAILURE_ON_CLIENT_HELLO ...*' kan være forårsaget af, at udbyderen stadig anvender RC4, hvilket siden [Android 7](https://developer.android.com/about/versions/nougat/android-7.0-changes.html#tls-ssl) ikke længere understøttes.

Fejlen '*Handshake mislykkedes ... UNSUPPORTED_PROTOCOL eller TLSV1_ALERT_PROTOCOL_VERSION ...*' kan forårsages af aktivering af forbindelseshærdning i forbindelsesindstillingerne eller af, at Android ikke længere understøtter ældre protokoller såsom SSLv3.

Android 8 Oreo og senere [understøtter ikke længere](https://developer.android.com/about/versions/oreo/android-8.0-changes#security-all) SSLv3. Der er ingen måde at løse manglende RC4- og SSLv3-understøttelse på, da disse er helt fjernet fra Android.

Du kan benytte [dette websted](https://ssl-tools.net/mailservers) eller [dette websted](https://www.immuniweb.com/ssl/) til at tjekke for SSL-/TLS-problemer på e-mailservere.

<br />

<a name="faq42"></a>
**(42) Kan der tilføjes en ny udbyder på udbyderlisten?**

Anvendes udbyderen af flere end blot et par personer, ja, med glæde.

Flg. oplysninger kræves:

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

EFF-[skrivere](https://www.eff.org/nl/deeplinks/2018/06/announcing-starttls-everywhere-securing-hop-hop-email-delivery): "*Derudover er der stadig ingen garanti for, at din kommunikation bliver krypteret, selvom du opsætter STARTTLS korrekt samt anvender et gyldigt certifikat.*"

Så rene SSL-forbindelser er sikrere end at anvende [STARTTLS](https://en.wikipedia.org/wiki/Opportunistic_TLS) og derfor foretrukne.

Sørg for at tjekke, at beskedmodtagelse/-afsendelse fungerer korrekt, før du kontakter mig vedr. udbydertilføjelse.

Se nedenfor, hvordan du kontakter mig.

<br />

<a name="faq43"></a>
**(43) Kan du vise den originale ... ?**

Vis original, viser den originale besked, som afsenderen har sendt den, inkl. originale skrifttyper, farver, marginer mv. FairEmail ændrer på ingen måde på dette, bortset fra at anmode om [TEXT_AUTOSIZING](https://developer.android.com/reference/android/webkit/WebSettings.LayoutAlgorithm), som *forsøger* at gøre en lille tekst mere læsbar.

<br />

<a name="faq44"></a>
**~~(44) Kan man få vist kontaktfotos/-identikoner i Sendt-mappen?~~**

~~Kontaktfotos og identikoner vises altid for afsenderen, da dette er nødvendigt for samtaletråde.~~ ~~At få kontaktfotos til både afsender og modtager er ikke rigtig en mulighed, da hentning af et kontaktfoto er en dyr operation.~~

<br />

<a name="faq45"></a>
**(45) Hvordan rettes problemet 'Denne nøgle er utilgængelig. To use it, you must import it as one of your own!' ?**

Man får meddelelsen *Denne nøgle er ikke tilgængelig. For at bruge den, skal den importere som en af ens egne!*, når en besked forsøges dekrypteret med en offentlig nøgle. For at løse dette, skal den private nøgle importeres.

<br />

<a name="faq46"></a>
**(46) Hvorfor genindlæses beskedlisten hele tiden?**

Ses en 'spinner' øverst på beskedlisten, synkes mappen stadig med fjernserveren. Forløbsstatus for synk kan ses i mappelisten. Tjek symbolforklaringen for betydninger af ikoner og tal.

Enhedens hastighed og Internetforbindelse samt antal beskedsynkdage bestemmer, hvor langt tid synkning tager. Bemærk, at antal beskedsynkdage i de fleste tilfælde ikke bør opsættes til mere end én dag. Tjek også [denne FAQ](#user-content-faq39).

<br />

<a name="faq47"></a>
**(47) Hvordan løser jeg fejlen 'Ingen primær konto/udkastmappe'?**

Fejlmeddelelsen *Ingen primær konto eller udkastmappe* vises, når en besked forsøges oprettet, uden at nogen primær konto er angivet eller en udkastmappe er valgt for den primære konto. Dette kan f.eks. ske, når FairEmail startes fra en anden app mhp. at skrive en besked. FairEmail skal vide, hvor udkast skal gemmes, så det er nødvendigt at vælge en konto til at udgøre den primære konto samt/eller vælge en udkastmappe for denne.

Dette kan også ske, når man forsøger at svare på en besked, eller videresende en besked fra en konto uden udkastmappe, mens der ikke er nogen primær konto, eller når den primære konto ikke har en udkastmappe.

Tjek [denne FAQ](#user-content-faq141) for yderligere information.

<br />

<a name="faq48"></a>
**~~(48) Hvordan løser jeg fejlen 'Ingen primær konto/arkivmappe'?~~**

~~Fejlmeddelelsen *Ingen primær konto eller arkivmappe* ses, når der søges efter beskeder fra en anden app. FairEmail skal vide, hvor der skal søges, så det er nødvendigt at vælge en konto til at udgøre den primære konto samt/eller vælge en arkivmappe for denne.~~

<br />

<a name="faq49"></a>
**(49) Hvordan løser jeg fejlen 'En forældet app har sendt en filsti i stedet for en fil-stream'?**

Der er sandsynligvis valgt eller afsendt en vedhæftning/billede vha. en forældet filhåndtering eller en forældet app, der antager, at alle apps stadig har lagerpladstilladelser. Af sikkerheds- og fortrolighedsårsager har moderne apps såsom FairEmail ikke længere fuld adgang til alle filer. Dette kan resultere i fejlmeddelelsen *En forældet app sendte en filsti i stedet for en filstream*, hvis et filnavn i stedet for en filstream deles med FairEmail, da denne ikke kan åbne tilfældige filer.

Dette kan løses ved at skifte til en opdateret filhåndtering/app designet til de seneste Android-versioner. Alternativt kan FairEmail tildeles læserettighed til enhedens lagerplads i Androids app-indstillinger. Bemærk, at denne omgåelse [ikke længere fungerer i Android Q](https://developer.android.com/preview/privacy/scoped-storage).

Tjek også [spørgsmål 25](#user-content-faq25) og [Googles information herom](https://developer.android.com/training/secure-file-sharing/share-file#RespondToRequest).

<br />

<a name="faq50"></a>
**(50) Vil der kunne blive tilføjet en mulighed for at synke alle beskeder?**

Flere, eller endda alle, beskeder kan synkes via langt tryk på en mappe (indbakke) i mappelisten over en konto (tryk på kontonavnet i navigationsmenuen) og så vælge *Synkronisér flere* i popup-menuen.

<br />

<a name="faq51"></a>
**(51) Hvordan sorteres mapper?**

Mapper sorteres først efter kontorækkefølge (som standard kontonavnet) og i selve kontoen med særlige systemmapper øverst, efterfulgt af mapper opsat til synkning. Inden for hver kategori sorteres mapperne efter (visnings-)navn. Visningsnavn kan angives vha. langt tryk på en mappe i mappelisten og så vælge *Rediger egenskaber*.

The navigation (hamburger) menu item *Order folders* in the settings can be used to manually order the folders.

<br />

<a name="faq52"></a>
**(52) Hvorfor tager det noget tid at genforbinde til en konto?**

Der er ingen pålidelig måde at få oplyst, om en kontoforbindelse blev afsluttet tilsigtet eller utilsigtet. Forsøg på at genforbinde til en konto, hvis forbindelse for ofte er blevet afsluttet abrupt, kan resultere i problemer såsom [for mange samtidige forbindelser](#user-content-faq23) eller endda blokering af kontoen. For at forhindre sådanne problemer, venter FairEmail 90 sek., inden forbindelsesgenoprettelse igen forsøges.

Benyt langt tryk på *Indstillinger* i navigeringsmenuen for straks at genoprette forbindelsen.

<br />

<a name="faq53"></a>
**(53) Kan beskedhandlingsbjælken fastgøres øverst/nederst?**

Beskedhandlingshandlingsbjælken virker på én enkelt besked, og den nederste handlingsbjælke virker på alle beskeder i en konversation. Da der ofte er flere end én besked i en konversation, er dette ikke muligt. Desuden findes en del beskedspecifikke handlinger, f.eks. videresendelse.

Flytning af beskedhandlingbjælken til bunden af beskeden er visuelt ikke tiltalende, dai der allerede er en konversationshandlingsbjælke nederst på skærmen.

Bemærk, at der ikke er mange (om nogle) e-mail apps, som viser en konversation som en liste over udvidelige beskeder. Dette har mange fordele, men er også årsagen til behovet for beskedspecifikke handlinger.

<br />

<a name="faq54"></a>
**~~~(54) Hvordan benyttes et navneområdepræfiks? ~~**

~~Et navneområdepræfix benyttes til automatisk at fjerne de præfikser, udbydere undertiden tilføjer mappenavne.~~

~~F.eks. betegnes Gmail Spam-mappen:~~

```
[Gmail]/Spam
```

~~Ved at sætte mavneområdepræfikset til *[Gmail]*, fjerner FairEmail automatisk *[Gmail]/* fra alle mappenavne.~~

<br />

<a name="faq55"></a>
**(55) Hvordan kan man markere alle beskeder som læst/flyttet eller slette dem alle?**

Man kan bruge flervalgsmuligheden til dette. Tryk og hold på den første besked og træk, uden at løfte fingeren, ned til den sidste besked. Brug derefter treprikshandlingsknappen til at udføre den ønskede handling.

<br />

<a name="faq56"></a>
**(56) Kan der blive tilføjet understøttelse af JMAP?**

Der er næsten ingen udbydere, som tilbyder [JMAP](https://jmap.io/)-protokollen, så det er ikke umagen værd at tilføje understøttelse for denne i FairEmail.

<br />

<a name="faq57"></a>
**(57) Kan HTML benyttes i signaturer?**

Ja, [HTML](https://en.wikipedia.org/wiki/HTML) kan benyttes. I signaturredigeringsværktøjet kan du via trepriksmenuen skifte til HTML-tilstand.

Bemærk, at skifter du tilbage til teksredigeringsværktøjet, vil alt HTML måske fremstå korrekt, da Android-teksredigeringsværktøjet ikke kan håndtere alt HTML. Tilsvarende, hvis du bruger tekstredigerinsværktøjet, kan HTML blive ændret på uventede måder.

Ønsker su at bruge præformateret tekst, såsom [ASCII art](https://en.wikipedia.org/wiki/ASCII_art), bør du ombryde teksten i et *præelement* således:

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
**(58) Hvad er et åbn/luk e-mailikon?**

E-mailikonet i mappelisten kan åbnes (omrids) eller lukkes (udfyldt):

![Eksternt billede](https://github.com/M66B/FairEmail/blob/master/images/baseline_mail_outline_black_48dp.png)

Beskedtekster og vedhæftninger downloades ikke som standard.

![Eksternt billede](https://github.com/M66B/FairEmail/blob/master/images/baseline_email_black_48dp.png)

Beskedtekster og vedhæftninger downloades som standard.

<br />

<a name="faq59"></a>
**(59) Kan originale beskeder åbnes i browseren?**

Af sikkerhedsårsager er filerne med de originale beskedtekster ikke tilgængelige for andre apps, så dette er ikke muligt. I teorien kunne [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) bruges til at dele disse filer, men selv Google Chrome kan ikke håndtere dette.

<br />

<a name="faq60"></a>
**(60) Vidste du ... ?**

* Vidste du, at stjernemarkerede beskeder kan synkroniseres/beholdes for altid? (dette kan aktiveres i modtagelsesindstillingerne)
* Vidste du, at du et langt tryk på ikonet 'Skriv besked' tager dig til Udkast-mappen?
* Vidste du, at der er en avanceret mulighed for at markere beskeder som læst, når de flyttes? (arkivering og kassering er også i flytning)
* Vidste du, at du kan vælge tekst (eller en e-mailadresse) i en app i de seneste Android-versioner og lade FairEmail søge efter den?
* Vidste du, at FairEmail har en tablettilstand? Rotér din enhed til liggende tilstand og konversationstråde åbnes i en 2. kolonne, hvis der er tilstrækkelig skærmplads.
* Vidste du, at et langt tryk kan bruges på en svarskabelon for at oprette en udkastbesked fra denne?
* Vidste du, at man kan trykke og holde, og dernæst stryge for at vælge en række beskeder?
* Vidste du, at man kan prøve at gensende en besked ved at bruge træk-ned-for-at-opfriske i Udbakken?
* Vidste du, at man kan stryge en konversation til venstre/højre for at gå til den næste/forrige?
* Vidste du, at man kan trykke på et billede for at se, hvorfra det downloades?
* Vidste du, at et langt tryk på mappeikonet i handlingsbjælken muliggør valg af konto?
* Vidste du, at et langt tryk på stjerneikonet i en konversationstråd muliggør valg af stjernefarve?
* Vidste du, at navigeringsskuffen kan åbnes med et stryge fra venstre, selv når man kigger på en konversation?
* Vidste du, at et langt tryk på personers ikon viser/skjuler CC-/BCC-felter samt huske synlighedstilstanden til næste gang?
* Vidste du, at man kan indsætte en Android-kontaktgruppes e-mailadresser via trepriksoverløbsmenuen?
* Vidste du, at vælger man tekst og trykker på svar, så vil kun den valgte tekst blive citeret?
* Vidste du, at med et langt tryk på papirkurvsikoner (både i beskeden og den nederste handlingsbjæle) kan man permanent slette en besked/konversation? (version 1.1368+)
* Vidste du, at et langt tryk på Afsend vil vise Send- dialogen, selvom den er blevet deaktiveret?
* Vidste du, at du kan få vist kun originalbeskedteksten via et langt tryk på fuldskærmsikonet?
* Vidste du, at et langt tryk på svar-knappen muliggør at svare afsenderen? (siden version 1.1562)

<br />

<a name="faq61"></a>
**(61) Hvorfor vises visse beskeder nedtonet?**

Nedtonede (gråtonet) beskeder er lokalt flyttede beskeder, hvis flytning endnu ikke er bekræftet af serveren. Dette kan opstå ifm.(midlertidig) manglende server- eller kontoforbindelse. Disse beskeder synkes, når der er etableret server- og kontoforbindelse eller, hvis dette aldrig sker, slettes, hvis de er for gamle til at blive synket.

Mappen skal muligvis synkes manuelt, f.eks. ved at trække ned fra toppen.

Disse beskeder kan ses, men de kan ikke flytte igen, før den foregående flytning er blevet bekræftet.

Afventende [operationer](#user-content-faq3) vises i operationsvisningen, tilgængelig via hovednavigeringsmenuen.

<br />

<a name="faq62"></a>
**(62) Hvilke godkendelsesmetoder understøttes?**

Flg. godkendelsesmetoder understøttes og anvendes i denne rækkefølge:

* CRAM-MD5
* LOGIN
* PLAIN
* NTLM (ikke-testet)
* XOAUTH2 ([Gmail](https://developers.google.com/gmail/imap/xoauth2-protocol), [Yandex](https://tech.yandex.com/oauth/))

Da [JavaMail til Android](https://javaee.github.io/javamail/Android) ikke understøtter SASL-godkendelsesmetoder, er disse, udover CRAM-MD4, uunderstøttede.

Kræver udbyderen en uunderstøttet godkendelsesmetode, vises fejlmeddelelsen *Godkendelse mislykkedes* sandsynligvis.

[Client certificates](https://en.wikipedia.org/wiki/Client_certificate) can be selected in the account and identity settings.

[Server Name Indication](https://en.wikipedia.org/wiki/Server_Name_Indication) understøttes af [alle understøttede Android-versioner](https://developer.android.com/training/articles/security-ssl).

<br />

<a name="faq63"></a>
**(63) Hvordan skaleres billeder til skærmvisning?**

Store indlejrede eller vedhæftede [PNG-](https://en.wikipedia.org/wiki/Portable_Network_Graphics) og [JPEG-](https://en.wikipedia.org/wiki/JPEG)billeder skaleres automatisk mhp. skærmvisning. Dette skyldes, at e-mails er begrænset i størrelse til, afhængigt af udbyderen, typisk mellem 10 og 50 MB. Billeder skaleres som standard til en maksimal bredde og højde på ca. 1.440 pixels og gemmes med et komprimeringsforhold på 90%. Billeder nedskaleres vha. heltalsfaktorer for at reducere hukommelsesforbruget samt bevare billedkvaliteten. Automatisk ændring af størrelse af indlejrede og/eller vedhæftede billeder og den maksimale målbilledstørrelse kan opsættes i Send-indstillinger.

Ønskes størrelsen på billeder ændret fra gang til gang, kan [Send Reduced](https://f-droid.org/en/packages/mobi.omegacentauri.SendReduced/), eller en lignende app, bruges.

<br />

<a name="faq64"></a>
**~~~(64) Kan der blive tilføjet tilpassede handlinger for venstre/højre strygning?~~**

~~Den mest logiske konsekvens ifm. at stryge en listepost til venstre/højre er at fjerne denne fra listen.~~ ~~Den mest logiske handling ifm. en e-mail-app er at flytte beskeden til en anden mappe.~~ ~~Mappen, der skal flyttes til, kan vælges via kontoindstillingerne.~~

~~Andre handlinger, såsom markering af beskeder som læste og slumre beskeder, er tilgængelige via multivalg.~~ ~~~Lang tryk på en besked giver adgang til multivalg. Tjek også [dette spørgsmål](#user-content-faq55).~~

~~Strygning til venstre/højre for at markere en besked som læst/ulæst er ulogisk, da beskeden først forsvinder for senere dukke op igen (med en anden status).~~ ~~Bemærk, at der er en avanceret mulighed for automatisk at markere beskeder som læste ved flytning,~~ ~~ hvilket for det meste er en perfekt erstatning for sekvensen markér som læst og flyt til anden mappe.~~ ~~Beskeder kan også markeres som læste via Nye beskeder-notifikationen.~~

~~Vil man læse en besked senere, kan den skjules indtil et bestemt tidspunkt vha. menuen *slumre*.~~

<br />

<a name="faq65"></a>
**(65) Hvorfor vises visse beskeder nedtonet?**

Indlejrede (billed-)vedhæftninger vises nedtonet. [Indlejrede vedhæftninger](https://tools.ietf.org/html/rfc2183) er begrenet på automatisk download og visning. men da FairEmail ikke altid downloader vedhæftninger automatisk (tjek også [denne FAQ](#user-content-faq40)), vises alle typer vedhæftninger. For at differentiere indlejringer og regulære vedhæftninger, vises indlejrede filer nedtonet.

<br />

<a name="faq66"></a>
**(66) Er FairEmail tilgængelig i Google Play Familie-biblioteket?**

"*You can't share in-app purchases and free apps with your family members.*"

See [here](https://support.google.com/googleone/answer/7007852) under "*See if content is eligible to be added to Family Library*", "*Apps & games*".

<br />

<a name="faq67"></a>
**(67) Hvordan slumres konversationer?**

Vælg via multivalg én af flere konversationer (langt tryk for at tilgå multivalg), tryk på trepriksknappen og vælg *Slumre ...*. Alternatively, in the expanded message view use *Snooze ...* in the message three-dots 'more' menu or the time-lapse action in the bottom action bar. Vælg konversationsslumretid(er) og bekræft med et tryk på OK. Konversationerne skjules i det valgte tidsrum og vises derefter igen. En Ny besked-notifikation vil blive vist som påmindelse.

It is also possible to snooze messages with [a rule](#user-content-faq71), which will also allow you to move messages to a folder to let them be auto snoozed.

Slumrede beskeder kan vises ved at afmarkerere *Bortfiltrér* > *Skjulte* i trepriksoverløbsmenuen.

Man kan trykke på det lille slumreikon for at se en konversations slumrevarighed.

Ved at angive nul som slumrevarighed, afbrydes slumringen.

Third party apps do not have access to the Gmail snoozed messages folder.

<br />

<a name="faq68"></a>
**~~(68) Hvorfor kan Adobe Acrobat Reader/Microsoft-apps ikke åbne PDF-/dokumentvedhæftninger?~~**

~~Adobe Acrobat Reader og Microsoft apps forventer stadig fuld adgang til alle lagrede filer,~~ ~~selvom det for apps har krævet brug af [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) siden Android KitKat (2013)~~ ~~~for alene at tilgå aktivt delte filer. Dette er af fortroligheds- og sikkerhedsårsager.~~

~~Dette kan omgås ved at gemme vedhæftningen og åbne den fra Adobe Acrobat Reader/Microsoft-appen,~~ ~~, men det anbefales at installere en opdateret og helst open-source PDF-læser/dokumentfremviser,~~ ~~f.eks. en af de [hér](https://github.com/offa/android-foss#-document--pdf-viewer) anførte.~~

<br />

<a name="faq69"></a>
**(69) Kunne der blive tilføjet auto-rul op ved ny besked?**

Beskedlisten ruller automatisk op, når der navigeres fra en ny beskednotifikation eller efter manuel opfriskning. Kontinuerlig automatisk rul op ved modtagelse af nye beskeder ville forstyrre brugerens rulning, men om ønsket, kan dette aktivere i indstillingerne.

<br />

<a name="faq70"></a>
**(70) Hvornår auto-ekspanderes beskeder?**

Ved navigering til en konversation, auto-ekspanderes en besked, hvis:

* Der er kun én besked i konversationen
* Der kun er én ulæst besked i konversationen
* Der er præcis én stjernemarkeret (favorit) besked i konversationen (fra version 1.1508)

Der er én undtagelse: Beskeden er ikke downloadet endnu, og beskeden er for stor til at downloade automatisk på en takseret (mobildata) forbindelse. Maks. beskedstørrelse kan opsættes/deaktiveres på fanen 'Forbindelsesindstillinger'.

Beskeder, som er dubletter (arkiverede), kasserede og udgør udkast, medregnes ikke.

Ved ekspandering auto-markeres beskeder som læste, medmindre dette er deaktiveret i de individuelle kontoindstillinger.

<br />

<a name="faq71"></a>
**(71) Hvordan benyttes filterregler?**

Filterregler kan redigere via et langt tryk på en mappe i kontomappelisten (tryk på kontonavnet i navigerings-/sidemenuen).

Nye regler effektueres for nye, modtagne beskeder i mappen, ikke for eksisterende beskeder. Reglen kan tjekkes og anvendes på eksisterende beskeder eller alternativt, via et langt tryk på regellisten, kan *Eksekvér nu* vælges.

En regel skal navngives, og rækkefølgen, for hvilken en regel skal eksekveres relativt til andre ditto, skal defineres.

En regel kan deaktiveres og udførsel af andre regler kan stoppes efter en regel er blevet eksekveret.

Flg. regelbetingelser er tilgængelige:

* Sender contains or sender is contact
* Modtager indeholder
* Emne indeholder
* Has attachments (optional of specific type)
* Overskrift indeholder
* Absolut tid (modtaget) mellem (siden version 1.1540)
* Relativ tid (modtaget) mellem

Alle regelbetingelser skal være imødekommet for udførelse af regelhandlingen. Alle betingelser er valgfrie, men der skal være mindst én betingelse for at undgå matchning af alle beskeder. Ønskes alle afsendere eller modtagere matchet, kan @-tegnet blot anvendes som betingelse, idet alle e-mailadresser indeholder dette tegn. Ønsker at et domænenavn matchet, kan en betingelse i stil med *@eksempel.dk* bruges

Bemærk, at e-mailadresser er formateret således:

`
"Nogen" <somebody@example.org>`

Man kan anvende flere regler, muligvis med et *eksekveringsstop*, for en *eller* eller en *ikke* betingelse.

Matchning er ikke versal-/minuskelfølsom undtagen for [regulære udtryk](https://en.wikipedia.org/wiki/Regular_expression). Tjek [hér](https://developer.android.com/reference/java/util/regex/Pattern) ang. dokumentation for regulære Java-udtryk. En regex kan aftestes [hér](https://regexr.com/).

Bemærk, at et regulært udtryk understøtter en *eller*-operatør, for matchning af flere afsendere om ønsket:

`
.*anette@eksempel\.org.*|.*bo@eksempel\.org.*|.*karen@eksempel\.org.*`

Bemærk, at [dot all-tilstand](https://developer.android.com/reference/java/util/regex/Pattern#DOTALL) er aktiveret for at kunne matche [udfoldede headers](https://tools.ietf.org/html/rfc2822#section-3.2.3).

En af disse handlinger kan vælges anvendt på matchende beskeder:

* Ingen handling (nyttigt for *ikke*)
* Markér som læst
* Markér som ulæst
* Skjul
* Undertryk notifikationer
* Udsæt
* Tilføj stjerne
* Angiv vigtighed (lokal prioritet)
* Tilføj stikord
* Flyt
* Kopiér (Gmail: Etiket)
* Besvar/videresend (med skabelon)
* Tekst-til-tale (afsender og emne)
* Automatisering (Tasker mv.)

An error in a rule condition can lead to a disaster, therefore irreversible actions are not supported.

Regler effektueres umiddelbart efter beskedhovedet er hentet, men inden beskedteksten er blevet downloadet, så brug af betingelser på beskedteksten er derfor ikke muligt. Bemærk, at store beskedtekster downloades på forlangende via takserede forbindelser mhp. at reducere dataforbruget.

Ønskes en besked videresendt, så overvej i stedet at bruge flythandlingen. Dette vil også være mere pålideligt end videresendelse, da videresendte beskeder måske opfattes som spam.

Since message headers are not downloaded and stored by default to save on battery and data usage and to save storage space it is by default not possible to preview which messages would match a header rule condition. You can enable downloading message headers in the connection settings and check headers conditions anyway (since version 1.1599).

Nogle almindelige header-betingelser (regex):

* *.&ast;Auto-Submitted:.&ast;* [RFC3834](https://tools.ietf.org/html/rfc3834)
* *.&ast;Content Type: multipart/report.&ast;* [RFC3462](https://tools.ietf.org/html/rfc3462)

I trepriks-beskedmenuens *mere*-menu findes et element til oprettelse af en regel for en modtaget besked med de mest almindelige betingelser udfyldt.

POP3- protokollen understøtter ikke opsætning af nøgleord og beskedflytning/-kopiering.

Brug af regler er en Pro-funktion.

<br />

<a name="faq72"></a>
**(72) Hvad er primære konti/identiteter?**

Den primære konto bruges, når kontoen er tvetydig, f.eks. ved start på et nyt udkast fra den fælles indbakke.

Omvendt anvendes en kontos primære identitet, når identiteten er tvetydig.

Der kan kun være én primær konto, og der kan kun være én primær identitet pr. konto.

<br />

<a name="faq73"></a>
**(73) Er beskedflytning på tværs af konti sikkert/effektiv?**

Beskedflytning på tværs af konti er sikkert, da de rå originalbeskeder downloades og flyttes, og da kildebeskeder først slettes, efter at målbeskeder er tilføjet

Massebeskedflytning på tværs af konti er effektivt, hvis både kilde- og målmappen er opsat til synk, da FairEmail ellers vil skulle oprette mappeforbindelse(r) for hver besked.

<br />

<a name="faq74"></a>
**(74) Hvorfor optræder der dubletbeskeder'?**

Visse udbydere, især Gmail, oplister også alle beskeder i alle mapper, undtagen i papirkurven, i arkivmappen (alle beskeder). FairEmail viser alle disse beskeder på en ikke-påtrængende måde for at indikere, at de i virkeligheden er identiske.

Gmail tillader en besked at have flere etiketter, som præsenteres for FairEmail som mapper. Det betyder også, at beskeder med flere etiketter vil fremgå flere gange.

<br />

<a name="faq75"></a>
**(75) Kan der blive lavet en Windows-, Linux-, iOS-version mv.?**

En masse viden og erfaring kræves for at udvikle en velfungerende app til en bestemt platform, hvilket er grunden til, at jeg kun udvikler apps til Android.

<br />

<a name="faq76"></a>
**(76) Hvad gør 'Ryd lokale beskeder'?**

Mappemenuen *Ryd lokale beskeder* fjerner beskeder fra enheden, som også er til stede på serveren. Den sletter ikke beskeder fra serveren. Af f.eks. pladshensyn kan dette kan være nyttigt efter ændring af mappeindstillinger for ikke at downloade beskedindhold (tekst og vedhæftninger).

<br />

<a name="faq77"></a>
**(77) Hvorfor vises beskeder nogle gange med en lille forsinkelse?**

Afhængigt af enhedens hastighed (CPU-hastighed og måske især hukommelses ditto) kan beskeder blive vist med en lille forsinkelse. FairEmail er designet til dynamisk at håndtere et stort beskedantal uden at løbe tør for hukommelse. Det betyder, at beskeder skal læses fra en database, og at denne skal monitoreres for ændringer, og begge kan forårsage små forsinkelser.

Visse bekvemmelighedsfunktioner, såsom beskedgruppering for at vise konversationstråde samt bestemmelse af den foregående/næste besked, tager lidt ekstra tid. Bemærk, at der ikke er nogen *næste* besked, da der i mellemtiden kan være modtaget en ny besked.

Ved hastighedssammenligning af FairEmail med lignende apps, bør dette indgå i sammenligningen. Det er nemt at kode en lignende, hurtigere app, der blot viser en lineær beskedliste og muligvis samtidigt bruger for megen hukommelse, men det er ikke så nemt at håndtere ressourceforbrug korrekt og at tilbyde mere avancerede funktioner såsom samtaletråde.

FairEmail er baseret på de nyeste [Android-arkitekturkomponenter](https://developer.android.com/topic/libraries/architecture/), hvilket ikke giver de store muligheder for ydelsesforbedringer.

<br />

<a name="faq78"></a>
**(78) Hvordan anvendes tidsplaner?**

Man kan i modtagelsesindstillingerne aktivere tidsplanlægning og indstille en tidsperiode og ugedage for, *hvornår* beskeder skal *modtages*. Bemærk, at et sluttidspunkt lig med eller før starttidspunktet, anses for 24 timer senere.

Automatisering (tjek nedenfor) kan bruges til mere avancerede tidsplaner, såsom flere synk-perioder pr. dag eller forskellige synk-perioder på forskellige dage.

Det er muligt at installere FairEmail i flerbrugerprofiler, f.eks. en personlig og en arbejdsprofil, samt at opsætte FairEmail forskelligt i hver profil, hvilket er en anden mulighed for at have forskellige synk-tidsplaner samt synke et andet sæt konti.

Det er også muligt at oprette [filterregler](#user-content-faq71) med en tidsbetingelse og at slumre beskeder indtil sluttidspunktet for tidsbetingelsen. Det er på denne måde muligt at *slumre* forretningsrelaterede beskeder indtil arbejdstidsstart. Det betyder også, at beskederne vil befinde sig på enheend, selv hvis der (midlertidigt) ingen Internetforbindelse er.

Bemærk, at nyere Android-versioner tillader tilsidesættelse af DND (Forstyr ikke) pr. notifikationskanal og app, hvilket kan bruges til (ikke) at forstumme bestemte (forretnings-)notifikationer. Tjek [hér](https://support.google.com/android/answer/9069335) for yderligere information.

For mere komplekse tidsplaner kan man opsætte en eller flere konti til manuel synk og sende denne kommando til FairEmail for at tjekke for nye beskeder:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL
```

For en specifik konto:

```
(adb shell) am start-foreground-service -a eu.faircode.email.POLL --es account Gmail
```

Man kan også automatisere at slå beskedmodtagelse til og fra ved at sende disse kommandoer til FairEmail:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE
```

For at aktivere/deaktivere en bestemt konto:

```
(adb shell) am start-foreground-service -a eu.faircode.email.ENABLE --es account Gmail
(adb shell) am start-foreground-service -a eu.faircode.email.DISABLE --es account Gmail
```

Bemærk, at deaktivering af en konto vil skjule kontoen og alle tilknyttet indhold. From version 1.1600 an account will be disabled/enabled by setting the account to manual/automatic sync, so the folders and messages keep being accessible.

To set the poll interval:

```
(adb shell) adb shell am start-foreground-service -a eu.faircode.email.INTERVAL --ei minutes nnn
```

Where *nnn* is one of 0, 15, 30, 60, 120, 240, 480, 1440. A value of 0 means push messages.

Kommandoer kan automatisk sendes med f.eks. [Tasker](https://tasker.joaoapps.com/userguide/en/intents.html):

```
Ny opgave: En genkendelig
Handlingskategori: Div./Send-
hensigtshandling: eu.faircode.email.ENABLE
Mål: Tjeneste
```

For at aktivere/deaktivere en konto navngivet *Gmail*:

```
Ekstra: konto:Gmail
```

Kontonavne er minuskel-/versalfølsomme.

Tidsplanlægning er en Pro-funktion.

<br />

<a name="faq79"></a>
**(79) Hvordan bruges synk på forlangende (manuelt)?**

Normalt opretholder FairEmail, når det er muligt, en forbindelse til de opsatte e-mailservere mhp. beskedmodtagelse i realtid. Ønsker dette ikke, f.eks. for ikke at blive forstyrret eller for at spare strøm, så deaktivér modtagelse i modtagelsesindstillinger. Dette stopper baggrundstjenesten, der tager sig af automatisk synk, og fjerner den tilhørende statuslinjenotifikation.

Man kan også aktivere *Synkronisér manuelt* i de avancerede kontoindstillinger, hvis man kun vil synke specifikke konti manuelt.

Man kan bruge træk-ned-for-at-opfriske i en beskedliste eller bruge mappemenuen *Synkronisér nu* til manuelt at synke beskeder.

Ønsker man at synke visse eller alle kontomapper manuelt, så deaktivér blot synk for disse (men ikke for selve kontoen).

[Gennemse på server](#user-content-faq24) vil sandsynligvis også ønskes deaktiveret.

<br />

<a name="faq80"></a>
**~~(80) Hvordan løses fejlen 'Indlæsning af BODYSTRUCTURE mislykkedes'?~~**

~~Fejlmeddelelsen *Kan ikke indlæse BODYSTRUCTURE* forårsages af e-mailserverfejl,~~ ~~~~tjek [hér](https://javaee.github.io/javamail/FAQ#imapserverbug) for flere detaljer.~~

~~FairEmail forsøger allerede at løse disse fejl, men lykkes dette ikke, så anmod udbyder om support. ~~

<br />

<a name="faq81"></a>
**~~(81) Kan baggrunden på originalbeskeder blive gjort mørk i det mørke tema?~~**

~~Den oprindelige besked vises som afsenderen har sendt den, inkl. alle farver.~~ ~~Baggrundsfarveændring vil ikke blot ændre visning af oprindelige beskeder, men kan også gøre dem ulæselige.~~

<br />

<a name="faq82"></a>
**(82) Hvad er et sporingsbillede?**

Tjek [hér](https://en.wikipedia.org/wiki/Web_beacon) vedr., hvad et sporingsbillede præcist er. Kort fortalt, sporingsbilleder registrerer, om en besked er blevet åbnet.

FairEmail vil i de fleste tilfælde automatisk genkende sporingsbilleder og erstatte disse med flg. ikon:

![Eksternt billede](https://github.com/M66B/FairEmail/blob/master/images/baseline_my_location_black_48dp.png)

Automatisk detektering af sporingsbilleder kan deaktiveres i fortrolighedsindstillinger.

<br />

<a name="faq84"></a>
**(84) Hvad bruges lokale kontakter til?**

Lokale kontaktoplysninger er baseret på navne og adresser indeholdt i ind- og udgående beskeder.

Hovedanvendelsen for det lokale kontaktlager er at tilbyde autoudfyldelse, hvis FairEmail ikke har fået tildelt kotakttilladelse.

En anden anvendelse på nyere Android-versioner er at generere [genveje](#user-content-faq31)- for hurtig beskedafsendelse til ofte brugte kontakter. Dette er også grunden til registreringen af antal gange kontaktet og senest kontaktet, og hvorfor en kontakt via langt at tryk kan gøres til favorit eller udelukkes fra favoritter.

Kontaktoversigten er sorteret efter antal gange kontaktet og senest kontaktet.

Som standard registreres kun de navne og adresser, til hvem beskeder sendes. Dette kan ændre i afsendelsesindstillingerne.

<br />

<a name="faq85"></a>
**(85) Hvorfor er en identitet ikke tilgængelig?**

En identitet er kun tilgængelig for afsendelse af en ny besked, besvarelse eller videresendelse af en eksisterende besked, hvis:

* identiteten er opsat til synkronisering (afsendte beskeder)
* den tilknyttede konto er opsat til synkronisering (modtagne beskeder)
* den tilknyttede konto har en udkastmappe

FairEmail vil forsøge at vælge den bedste identitet baseret på *Til*-adressen på den besvarede/videresendte beskede.

<br />

<a name="faq86"></a>
**~~~(86) Hvad er 'ekstra fortrolighedsfunktioner'?~~**

~~Den avancerede mulighed *ekstra fortrolighedsfunktioner* aktiverer:~~

* ~~Opslag af ejeren af IP-adressen for et link~~
* ~~Detektering og fjernelse af [sporingsbilleder](#user-content-faq82)~~

<br />

<a name="faq87"></a>
**(87) Hvad betyder 'ugyldige akkreditiver'?**

Fejlmeddelelsen *ugyldige akkreditiver* betyder, at enten brugernavn og/eller adgangskode er forkert, f.eks. grundet skiftet/udløbet adgangskode eller udløbet kontogodkendelse.

Er adgangskoden forkert/udløbet, skal denne opdatere i konto- og/eller identitetsindstillingerne.

Er kontotilladelsen udløbet, skal kontoen vælges igen. Den tilknyttede identitet skal sandsynligvis også gemmes igen.

<br />

<a name="faq88"></a>
**(88) Hvordan benytter jeg en Yahoo-, AOL- ellerr Sky-konto?**

Den foretrukne måde at opsætte en Yahoo-konto på er vha. hurtigopsætningsguiden, der benytter OAuth i stedet for en adgangskode og derfor er sikrere (samt nemmere).

For at godkende en Yahoo-, AOL- eller Sky-konto skal en app-adgangskode oprettes. For instruktioner, tjek venligst hér:

* [til Yahoo](https://help.yahoo.com/kb/generate-third-party-passwords-sln15241.html)
* [til AOL](https://help.aol.com/articles/Create-and-manage-app-password)
* [til Sky](https://www.sky.com/help/articles/getting-started-with-sky-yahoo-mail) (under *Andre e-mail apps*)

Tjek [denne FAQ](#user-content-faq111) vedr. OAuth-understøttelse.

Bemærk, at Yahoo, AOL og Sky ikke understøtter standard push-beskeder. Yahoo e-mail appen bruger en proprietær, ikke-dokumenteret protokol til push-beskeder.

Push-beskeder kræver [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE), og Yahoos e-mailservere angiver ikke IDLE som en mulighed:

```
Y1-EVNE
* MULIGHED FOR IMAP4rev1 ID FLYT NAVNEOMRÅDE XYMHIGHESTMODSEQ UIDPLUS LITERAL+ CHILDREN X-MSG-EXT AFMARKÉR OBJEKTID
Y1 OK MULIGHED fuldført
```

<br />

<a name="faq89"></a>
**(89) Hvordan kan rene teksbeskeder sendes?**

Som standard sender FairEmail hver besked både som almindelig tekst og HTML-formateret, da næsten alle modtagere i dag forventer formaterede beskeder. Ønskes eller er der behov for kun at sende simple tekstbeskeder, kan dette aktiveres i avancerede identitetsindstillinger. En ny identitet ønskes måske oprettet, hvis man fra gang til gang vil/har behov for at vælge at sende simple tekstbeskeder.

<br />

<a name="faq90"></a>
**(90) Hvorfor er nogle tekster linket uden at være et link?**

FairEmail linker automatisk ikke-linkede weblinks (HTTP og HTTPS) og ikke-linkede e-mailadresser (mailto) for din bekvemmelighed. Tekster og links er imidlertid ikke lette at skelne imellem, da masser af [topniveaudomæner](https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains) er reelle ord. Dette er grunden til, at tekster med prikker undertiden fejlfortolkes som links, hvilket er bedre end ikke at genkende nogle links.

Links til TEL-, GEO-, RTSP- og XMPP-protokoller genkendes også, hvorimod links til mindre almindelige eller sikre protokoller, såsom TELNET og FTP, ikke genkendes. Regex til at genkende links er allerede *meget* kompleks og tilføjelse af flere protokoller vil kun gøre den langsommere og muligvis forårsage fejl.

Bemærk, at originale beskeder vises, præcist som de er, hvilket også betyder, at links ikke automatisk tilføjes.

<br />

<a name="faq91"></a>
**~~(91) Kan der blive tilføjet periodisk synkronisering for at spare støm?~~**

~~Beskedsynkronisering er en ressourcetung proces, da lokale og eksterne beskeder skal sammenlignes, ~ ~~så periodisk beskedsynkronisering vil ikke være strømbesparende, sandsynligvis snarere tværtimod.~~

~~Tjek [denne FAQ](#user-content-faq39) om optimering af strømforbrug.~~

<br />

<a name="faq92"></a>
**(92) Kunne der blive tilføjet spamfiltrering, DKIM-signaturbekræftelse og SPF-godkendelse?**

Spamfiltrering, [DKIM](https://en.wikipedia.org/wiki/DomainKeys_Identified_Mail)-signaturbekræftelse og [SPF](https://en.wikipedia.org/wiki/Sender_Policy_Framework)-godkendelse er e-mailserveropgaver, ikke e-mailklient ditto. Servere har generelt mere hukommelse og computerkraft, så de er meget bedre egnet til disse opgaver end batteridrevne enheder. Spamfiltrering for alle e-mailklienter, f.eks. også web e-mail, vil give bedre mening end kun for én e-mailklient. Desuden har e-mailservere adgang til oplysninger ( f.eks. IP-adresse mv.) om en forbindende server, hvilket en e-mailklient ikke har.

Spam filtrering baseret på meddelelsesoverskrifter kunne have været mulig, men denne teknik er desværre [patenteret af Microsoft](https://patents.google.com/patent/US7543076).

Nyere versioner af FairEmail kan filtrere spam til en vis grad vha. en beskedklassifikator. Tjek [denne FAQ](#user-content-faq163) for yderligere information.

Selvfølgelig kan du rapportere beskeder som spam med FairEmail, som vil flytte de rapporterede meddelelser til spam mappe og træne spam filter af udbyderen, hvilket er, hvordan det skal fungere. Dette kan gøres automatisk med [filterregler](#user-content-faq71) også. Blokering af afsenderen vil oprette en filterregel for automatisk at flytte fremtidige breve fra samme afsender til spam mappen.

Bemærk at POP3- protokollen kun giver adgang til indbakken. Så det er ikke muligt at rapportere spam for POP3-konti.

Bemærk at du ikke bør slette spam-beskeder, heller ikke fra spam-mappen, fordi e-mail-serveren bruger meddelelserne i spam-mappen til at "lære", hvad spam-beskeder er.

Hvis du modtager en masse spam-beskeder i din indbakke, det bedste, du kan gøre, er at kontakte e-mail-udbyder for at spørge, om spam filtrering kan forbedres.

FairEmail kan også vise et lille rødt advarselsflag når DKIM, SPF eller [DMARC](https://en.wikipedia.org/wiki/DMARC) godkendelse mislykkedes på modtagerserveren. Du kan aktivere/deaktivere [godkendelsesbekræftelse](https://en.wikipedia.org/wiki/Email_authentication) i indstillinger.

FairEmail kan også vise et advarselsflag hvis domænenavnet på afsenderens (svar) e- mail- adresse ikke definerer en MX- post, der peger på en e- mail- server. Dette kan aktiveres i modtagelsesindstillingerne. Vær opmærksom på, at dette vil bremse synkronisering af beskeder betydeligt.

Er domænenavnene for afsender og svaradresse forskellige, vises advarselsflaget også, da dette oftest vil indikere phishing-beskeder. Om ønsket, kan dette deaktiveres i modtagelsesindstillingerne (fra v1.1506).

Hvis legitime meddelelser ikke autentificeres, du bør underrette afsenderen, da dette vil resultere i en høj risiko for, at beskeder ender i spammappen. Desuden er der uden ordentlig godkendelse en risiko for, at afsenderen vil blive efterlignet. Afsenderen kan bruge [dette værktøj](https://www.mail-tester.com/) til at kontrollere godkendelse og andre ting.

<br />

<a name="faq93"></a>
**(93) Kan der blive tilladt installation/datalagring på eksternt lagermedie (SD-kort)?**

FairEmail bruger tjenester og alarmer, leverer widgets og monitorerer for fuldført opstartsbegivenhed, så den kan starte ved start af enheden, intet af hvilket er muligt fra et eksternt SD-kort. Se også [her](https://developer.android.com/guide/topics/data/install-location).

Beskeder, vedhæftninger mv. gemt på f.eks. et eksternt SD-kortd kan tilgås af andre apps, og sådanne data er derfor ikke sikre. Se [her](https://developer.android.com/training/data-storage) for detaljerne.

Om ønsket, kan du gemme (rå) beskeder via trepriksmenuen ovenover beskedteksten samt gemme vedhæftninger ved at trykke på disketteikonet.

If you need to save on storage space, you can limit the number of days messages are being synchronized and kept for. You can change these settings by long pressing a folder in the folder list and selecting *Edit properties*.

<br />

<a name="faq94"></a>
**(94) What does the red/orange stripe at the end of the header mean?**

The red/orange stripe at the left side of the header means that the DKIM, SPF or DMARC authentication failed. Se også [denne FAQ](#user-content-faq92).

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
**(97) Hvad er 'oprydning' ?**

Hver fire timer kører FairEmail en oprydning job, at:

* Removes old message texts
* Removes old attachment files
* Removes old image files
* Removes old local contacts
* Removes old log entries

Bemærk, at oprydningsjobbet kun vil køre, når synkroniseringstjenesten er aktiv.

<br />

<a name="faq98"></a>
**(98) Hvorfor kan kontakter stadig vælges efter tilbagekaldelse af kontakter-tilladelser**

Efter tilbagekaldelse af kontakttilladelserne tillader Android ikke længere FairEmail adgang til dine kontaktpersoner. However, picking contacts is delegated to and done by Android and not by FairEmail, so this will still be possible without contacts permissions.

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

Mulige kategorier:

```
category:social
category:updates
category:forums
category:promotions
```

Unfortunately, this is not possible for snoozed messages folder.

Du kan benytte *Tvangssynk* i trepriksmenuen i den fælles indbakke for at lade FairEmail synkronisere mappelisten igen, og du kan vha. et langt tryk på mapperne aktivere synkronisering.

<br />

<a name="faq101"></a>
**(101) Hvad betyder det blå/orange punktum i bunden af samtalerne?**

Punktet viser den relative position for samtalen i meddelelseslisten. Punktet vil blive vist orange når samtalen er den første eller sidste i meddelelseslisten, ellers vil den være blå. The dot is meant as an aid when swiping left/right to go to the previous/next conversation.

The dot is disabled by default and can be enabled with the display settings *Show relative conversation position with a dot*.

<br />

<a name="faq102"></a>
**(102) How can I enable auto rotation of images?**

Billeder roteres automatisk, når automatisk ændring af billedstørrelse er aktiveret i indstillingerne (aktiveret som standard). Automatisk rotation afhænger dog af [Exif](https://en.wikipedia.org/wiki/Exif) -oplysningerne, der skal være til stede og være korrekte,, hvilket ikke altid er tilfældet. Particularly not when taking a photo with a camara app from FairEmail.

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

Please [see here](https://github.com/leolin310148/ShortcutBadger#supported-launchers) for a list of launchers which can show the number of unread messages. Standard Android [does not support this](https://developer.android.com/training/notify-user/badges).

Bemærk, at Nova Launcher kræver Tesla Unread, hvilket [ikke længere understøttes](https://forum.xda-developers.com/android/general/bad-news-tesla-unread-devoloper-t3920415).

Bemærk, at notifikationsindstillingen *Vis launcher-ikon med antallet af nye beskeder* skal være aktiveret (standard aktiveret).

Kun *nye* ulæste beskeder i mapper sat til at vise nye beskednotifikationer vil blive talt, mens beskeder markeret som ulæste igen og beskeder i mapper sat til ikke at vise ny besked-notifikation ikke tælles.

Afhængigt af ønske, vil notifikationsindstillingen *Lad antallet af nye beskeder matcher antallet af notifikationer* skulle aktiveres eller deaktiveres (standard er inaktiv). Når aktiveret, vil badgeantallet være lig antallet af nye beskednotifikationer. Når deaktiveret, vil badgeantallet være lig antallet af ulæste beskeder, uanset om de vises i en notifikation eller er nye.

Denne funktion afhænger af understøttelsen af din launcher. FairEmail 'udsendelser' blot antallet af ulæste beskeder vha. ShortcutBadger-biblioteket. Fungerer det ikke, kan dette ikke løses ved ændringer i FairEmail.

Visse launchers viser en prik eller '1'' for [moniteringsnotifikationen](#user-content-faq2) trods FairEmails eksplicitte anmodning om íkke om at vise et *badge* til denne notifikation. Dette kan skyldes en fejl i launcher-appen eller i din Android-version. Dobbelttjek, at notifikationsprikken (badge) er deaktiveret for notifikationsmodtagelseskanalen (tjenesten). Du kan gå til de rigtige notifikationskanalindstillinger via notifikationsindstillingerne i FairEmail. Det er måske ikke indlysende, men du kan trykke på kanalnavnet for yderligere indstillinger.

FairEmail does send a new message count intent as well:

```
eu.faircode.email.NEW_MESSAGE_COUNT
```

The number of new, unread messages will be in an integer "*count*" parameter.

<br />

<a name="faq107"></a>
**(107) Hvordan bruger jeg farvede stjerner?**

You can set a colored star via the *more* message menu, via multiple selection (started by long pressing a message), by long pressing a star in a conversation or automatically by using [rules](#user-content-faq71).

Du skal vide, at farvede stjerner ikke understøttes af IMAP-protokollen og kan derfor ikke synkroniseres til en e-mail-server. Det betyder, at farvede stjerner ikke vil være synlige i andre e-mail-klienter og vil gå tabt ved at downloade beskeder igen. Men stjernerne (uden farve) vil blive synkroniseret og vil være synlige i andre e-mail-klienter, når de understøttes.

Nogle e-mail-klienter bruger IMAP-søgeord til farver. Men ikke alle servere understøtter IMAP søgeord og udover at der ikke er nogen standard søgeord for farver.

<br />

<a name="faq108"></a>
**~~(108) Vil mulighed for permanent slettede beskeder fra enhver mappe blive tilføjet?~~**

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

Indstillinger > Manuel opsætning > Konti > konto > avanceret > Delvis hentning > afmarkér

Efter deaktivering af indstillingen kan du benytte trepriks-beskedmenuens 'mere'-menu til at 'gensynke' tomme beskeder. Alternatively, you can *Delete local messages* by long pressing the folder(s) in the folder list and synchronize all messages again.

Disabling *Partial fetch* will result in more memory usage.

<br />

<a name="faq111"></a>
**(111) Is OAuth supported?**

OAuth for Gmail is supported via the quick setup wizard. The Android account manager will be used to fetch and refresh OAuth tokens for selected on-device accounts. OAuth for non on-device accounts is not supported because Google requires a [yearly security audit](https://support.google.com/cloud/answer/9110914) ($15,000 to $75,000) for this. Du kan læse mere om dette [her](https://www.theregister.com/2019/02/11/google_gmail_developer/).

OAuth for Outlook/Office 365, Yahoo, Mail.ru and Yandex is supported via the quick setup wizard.

The OAuth [jump page](https://oauth.faircode.eu/) exists for when [Android App Links](https://developer.android.com/training/app-links/verify-site-associations) are not available, for example when using a non Play store version of the app, or do not work for some reason.

<br />

<a name="faq112"></a>
**(112) Which email provider do you recommend?**

FairEmail is an email client only, so you need to bring your own email address. Note that this is clearly mentioned in the app description.

Der er masser af e-mail-udbydere at vælge imellem. Hvilken e-mail-udbyder er bedst for dig, afhænger af dine ønsker/krav. Se venligst hjemmesiderne for [Gendan privatlivets fred](https://restoreprivacy.com/secure-email/) eller [Privacy Tools](https://www.privacytools.io/providers/email/) for en liste over privatlivsorienterede e-mail-udbydere med fordele og ulemper.

VIsse udbydere, såsom ProtonMail, Tutanota, anvender proprietære e-mailprotokoller, som umuliggør brug af tredjeparts e-mail apps. Se [denne FAQ](#user-content-faq129) for yderligere information.

Brug af dit eget (brugerdefineret) domænenavn, som understøttes af de fleste e-mail-udbydere, vil gøre det lettere at skifte til en anden e-mail-udbyder.

<br />

<a name="faq113"></a>
**(113) Hvordan virker biometrisk godkendelse?**

If your device has a biometric sensor, for example a fingerprint sensor, you can enable/disable biometric authentication in the navigation (hamburger) menu of the settings screen. Når aktiveret vil FairEmail kræve biometrisk godkendelse efter en periode med inaktivitet, eller efter at skærmen er blevet slukket, mens FairEmail kørte. Aktivitet er navigation i FairEmail, for eksempel at åbne en samtaletråd. Inaktivitet periode varighed kan konfigureres i forskellige indstillinger. Når biometrisk godkendelse er aktiveret, vil nye beskedmeddelelser ikke vise noget indhold, og FairEmail vil ikke være synlig på Android seneste skærm.

Biometrisk godkendelse er beregnet til at forhindre andre i at se dine meddelelser kun. FairEmail er afhængig af enhedskryptering til datakryptering, se også [denne FAQ](#user-content-faq37).

Biometrisk godkendelse er en pro-funktion.

<br />

<a name="faq114"></a>
(114) Kan der tilføjes en importmulighed til indstillinger fra andre e-mail apps

The format of the settings files of most other email apps is not documented, so this is difficult. Sometimes it is possible to reverse engineer the format, but as soon as the settings format changes things will break. Also, settings are often incompatible. F.eks. har FairEmail, i modsætning til de fleste andre e-mail-apps, indstillinger for antallet af synkdage for beskeder samt for antallet af dage til at beholde beskeder, hovedsageligt for at spare strøm. Moreover, setting up an account/identity with the quick setup wizard is simple, so it is not really worth the effort.

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

Google håndterer alle køb, så som udvikler har jeg ringe kontrol over køb. So, basically the only thing I can do, is give some advice:

* Make sure you have an active, working internet connection
* Make sure you are logged in with the right Google account and that there is nothing wrong with your Google account
* Har du flere aktive Google-konti på din enhed, så sørg for at benytte den korrekte under installationen af FairEmail
* Sørg for, at Play Butik-appen er opdateret, [tjek hér](https://support.google.com/googleplay/answer/1050566?hl=en)
* Åbn Play Butik-appen, og vent mindst ét minut på at give den tid til at synkronisere med Google-serverne
* Åbn FairEmail og gå til Pro-funktionsskærmen for at lade FairEmail tjekke købet. Nogle gange hjælper det at trykke på knappen *Køb*

You can also try to clear the cache of the Play store app via the Android apps settings. Restarting the device might be necessary to let the Play store recognize the purchase correctly.

Bemærk, at:

* Ser du *VARE ALLEREDE KØBT*, skal Play Butik-appen sandsynligvis opdateres, [tjek hér](https://support.google.com/googleplay/answer/1050566?hl=en)
* Køb gemmes i Google-skyen og kan ikke gå tabt
* Der er ingen tidsbegrænsning på køb, hvorfor de aldrig udløber
* Google afslører ikke oplysninger (navn, e-mail mv.) om købere til udviklere
* En app som FairEmail kan ikke vælge, hvilken Google-konto, der skal benyttes
* Der kan gå et stykke tid, før Play Butik-appen har synkroniseret et køb til en anden enhed
* Play Butik-køb kan ikke anvendes uden Play Butik, hvilket heller ikke er tilladt jf. Play Butik-reglerne

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

På grund af Android-begrænsninger er det ikke muligt dynamisk at indstille opaciteten af baggrunden og at have afrundede hjørner på samme tid.

<br />

<a name="faq120"></a>
**(120) Hvorfor fjernes nye beskednotifikationer ikke, når appen startes?**

Nye beskednotifikationer vil blive fjernet ved at stryge notifikationer væk eller ved at markere de tilknyttede beskeder som læst. Opening the app will not remove new message notifications. This gives you a choice to leave new message notifications as a reminder that there are still unread messages.

On Android 7 Nougat and later new message notifications will be [grouped](https://developer.android.com/training/notify-user/group). Tapping on the summary notification will open the unified inbox. The summary notification can be expanded to view individual new message notifications. Tapping on an individual new message notification will open the conversation the message it is part of. Se [denne FAQ](#user-content-faq70) om, hvornår beskeder i en samtale vil blive udvidet automatisk og markeret læst.

<br />

<a name="faq121"></a>
**(121) Hvordan er beskeder grupperet i en samtale?**

Som standard vil FairEmail grupper beskeder i samtaler. Dette kan slås til i indstillinger.

FairEmail grupper beskeder baseret på standard *Message-ID*, *In-Reply-To* og *Referencer* overskrifter. FairEmail does not group on other criteria, like the subject, because this could result in grouping unrelated messages and would be at the expense of increased battery usage.

<br />

<a name="faq122"></a>
**~~(122) Why is the recipient name/email address show with a warning color?~~**

~~The recipient name and/or email address in the addresses section will be shown in a warning color~~ ~~when the sender domain name and the domain name of the *to* address do not match.~~ ~~Mostly this indicates that the message was received *via* an account with another email address.~~

<br />

<a name="faq123"></a>
**(123) What will happen when FairEmail cannot connect to an email server?**

Kan FairEmail ikke forbinde til en e-mail-server for at synke beskeder, (grundet dårlig Internetforbindelse eller en firewall/VPN, der blokerer forbindelsen e.l.), vil FairEmail prøve én gang efter 8 sek. afventning, mens enheden holdes vågen (=bruger strømi). If this fails, FairEmail will schedule an alarm to retry after 15, 30 and eventually every 60 minutes and let the device sleep (=no battery usage).

Note that [Android doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby) does not allow to wake the device earlier than after 15 minutes.

*Tvangssynk* i trepriksmenuen i den fælles indbakke kan bruges til at lade FairEmail forsøge at oprette forbindelse igen uden at vente.

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

Send en [Leveringsstatusnotifikation](https://tools.ietf.org/html/rfc3464) (=hard bounce) via besvar-/svarmenuen.

Hard bounces will mostly be processed automatically because they affect the reputation of the email provider. The bounce address (=*Return-Path* header) is mostly very specific, so the email server can determine the sending account.

For some background, see for [this Wikipedia article](https://en.wikipedia.org/wiki/Bounce_message).

<br />

*Translate button (version 1.1600+)*

Please see [this FAQ](#user-content-faq167) about how to configure DeepL.

<br />

<a name="faq126"></a>
**(126) Can message previews be sent to my wearable?**

FairEmail fetches a message in two steps:

1. Fetch message headers
1. Fetch message text and attachments

Directly after the first step new messages will be notified. However, only until after the second step the message text will be available. FairEmail updates exiting notifications with a preview of the message text, but unfortunately wearable notifications cannot be updated.

Since there is no guarantee that a message text will always be fetched directly after a message header, it is not possible to guarantee that a new message notification with a preview text will always be sent to a wearable.

Mener du, at dette er tilstrækkeligt, du kan aktivere notifikationsindstillingen *Send kun notifikationer med beskedforhåndsvisning til wearables*, og fungerer dette ikke, du kan prøve at aktivere notifikationsindstillingen *Vis kun notifikationer med forhåndsvisningstekst*. Note that this applies to wearables not showing a preview text too, even when the Android Wear app says the notification has been sent (bridged).

If you want to have the full message text sent to your wearable, you can enable the notification option *Preview all text*. Note that some wearables are known to crash with this option enabled.

If you use a Samsung wearable with the Galaxy Wearable (Samsung Gear) app, you might need to enable notifications for FairEmail when the setting *Notifications*, *Apps installed in the future* is turned off in this app.

<br />

<a name="faq127"></a>
**(127) How can I fix 'Syntactically invalid HELO argument(s)'?**

Fejlen *... Syntactically invalid HELO argument(s) ...* means that the SMTP server rejected the local IP address or host name. You can likely fix this error by enabling or disabling the advanced indentity option *Use local IP address instead of host name*.

<br />

<a name="faq128"></a>
**(128) How can I reset asked questions, for example to show images?**

Du kan nulstille stillede spørgsmål via trepriksoverløbsmenuen under diverse indstillinger.

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

Fejlen *... ParseException ...* means that there is a problem with a received message, likely caused by a bug in the sending software. FairEmail will workaround this is in most cases, so this message can mostly be considered as a warning instead of an error.

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

I kontoindstillingerne (Settings > Manuel opsætning > Konti > konto) kan *Behold slettede beskeder på serveren* aktiveres.

*IMAP*

Since the IMAP protocol is meant to synchronize two ways, deleting a message from the device would result in fetching the message again when synchronizing again.

FairEmail understøtter imidlertid skjulning af beskeer, enten via trepriksmenuen i handlingsbjælken ovenover beskedteksten eller ved at vælge flere beskeder på beskedlisten. Basically this is the same as "leave on server" of the POP3 protocol with the advantage that you can show the messages again when needed.

Note that it is possible to set the swipe left or right action to hide a message.

<br />

<a name="faq135"></a>
**(135) Why are trashed messages and drafts shown in conversations?**

Individual messages will rarely be trashed and mostly this happens by accident. Showing trashed messages in conversations makes it easier to find them back.

Du kan slette en besked permanent vha. trepriksbeskedmenuen *slet*, hvilket fjerner beskeden fra samtalen. Note that this irreversible.

Similarly, drafts are shown in conversations to find them back in the context where they belong. It is easy to read through the received messages before continuing to write the draft later.

<br />

<a name="faq136"></a>
**(136) How can I delete an account/identity/folder?**

Deleting an account/identity/folder is a little bit hidden to prevent accidents.

* Konto: Indstillinger > Manuel opsætning > Konti > konto
* Identitet: Indstillinger > Manuel opsætning > Identiteter > identitet
* Folder: Long press the folder in the folder list > Edit properties

I trepriksoverløbsmenuen, øverst til højre, er der et element til sletning af kontoen/identiteten/mappen.

<br />

<a name="faq137"></a>
**(137) How can I reset 'Don't ask again'?**

You can reset all questions set to be not asked again in the miscellaneous settings.

<br />

<a name="faq138"></a>
**(138) Can you add calendar/contact/tasks/notes management?**

Calendar, contact, task and note management can better be done by a separate, specialized app. Bemærk, at FairEmail er en specialiseret e-mail app, ikke en kontorpakke.

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

* Push-beskeder aktiveret på for mange mapper: Se [denne FAQ](#user-content-faq23) for yderligere information og omgåelse
* Kontoadgangskoden blev skiftet: Skift af den i FairEmail også skulle løse problemet
* An alias email address is being used as username instead of the primary email address
* An incorrect login scheme is being used for a shared mailbox: the right scheme is *username@domain\SharedMailboxAlias*

The shared mailbox alias will mostly be the email address of the shared account, like this:

```
dig@eksempel.dk\delt@eksempel.dk
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

Dette problem kan løsee ved manuelt at vælge udkastmappen i kontoindstillingerne (Settings > Manuel opsætning > Konti > konto nederst). If there is no drafts folder at all, you can create a drafts folder by tapping on the '+' button in the folder list of the account (tap on the account name in the navigation menu).

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

![Eksternt billede](https://github.com/M66B/FairEmail/blob/master/images/baseline_record_voice_over_black_48dp.png)

This requires a compatible audio recorder app to be installed. In particular [this common intent](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION) needs to be supported.

For example [this audio recorder](https://f-droid.org/app/com.github.axet.audiorecorder) is compatible.

Voice notes will automatically be attached.

<br />

<a name="faq145"></a>
**(145) How can I set a notification sound for an account, folder or sender?**

Konto:

* Aktivere *Separate notifikationer* i de avancerede kontoindstillinger (Indstillinger > Manuel opsætning > Konti > konto > Avanceret)
* Langt tryk på kontoen på kontolisten (Indstillinger > Manuel opsætning > Konti), og vælg *Redigér notifikationskanal* for at ændre notifikationslyd

Mappe:

* Long press the folder in the folder list and select *Create notification channel*
* Long press the folder in the folder list and select *Edit notification channel* to change the notification sound

Afsender:

* Open a message from the sender and expand it
* Expand the addresses section by tapping on the down arrow
* Tap on the bell icon to create or edit a notification channel and to change the notification sound

The order of precendence is: sender sound, folder sound, account sound and default sound.

Setting a notification sound for an account, folder or sender requires Android 8 Oreo or later and is a pro feature.

<br />

<a name="faq146"></a>
**(146) How can I fix incorrect message times?**

Da afsendelsesdato/-tid er valgfrie og kan ændres af afsender, benytter FairEmail som standard postserverens modtagelsesdato/-tid.

Nogle gange er postserverens modtagelsesdato/-tid dog forkert, primært fordi beskeder blev forkert importeret fra en anden server, men undertiden også grundet en fejl i postserveren.

I sådanne sjældne tilfælde er det som en løsning muligt at lade FairEmail benytte enten dato/tid fra *Dato*-overskriften (afsendelsestidspunkt) eller fra *Modtaget*-overskriften. Dette kan ændres i de avancerede kontoindstillinger: Indstillinger > Manuel opsætning > Konti > konto > Avanceret.

This will not change the time of already synchronized messages. To solve this, long press the folder(s) in the folder list and select *Delete local messages* and *Synchronize now*.

<br />

<a name="faq147"></a>
**(147) What should I know about third party versions?**

You likely came here because you are using a third party build of FairEmail.

Der er **kun support** i den seneste Play Butik-version, den seneste GitHub-udgivelse og F-Droid-build, men **kun**, hvis versionsnummeret for F-Droid-build er det samme som versionsnummeret for den seneste GitHub-udgivelse.

F-Droid builds irregularly, which can be problematic when there is an important update. Therefore you are advised to switch to the GitHub release.

The F-Droid version is built from the same source code, but signed differently. Dvs., at alle funktioner også er tilgængelige i F-Droid versionen bortset brug af Gmail-hurtigopsætningsguiden, da Google har godkendt (og alee tillader) én app-signatur. For alle andre e-mailudbydere, er OAuth-adgang kun tilgængelig i Play Butik- og Github-versioner, da e-mailudbydere kun tillader brug af OAuth for officielle builds.

Note that you'll need to uninstall the F-Droid build first before you can install a GitHub release because Android refuses to install the same app with a different signature for security reasons.

Note that the GitHub version will automatically check for updates. When desired, this can be turned off in the miscellaneous settings.

Please [see here](https://github.com/M66B/FairEmail/blob/master/README.md#user-content-downloads) for all download options.

Har du et problem med F-Droid-builden, så tjek først, om der er en nyere GitHub-version.

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

Du kan indsætte e-mailadresserne for alle kontakter i en kontaktgruppe via trepriksmenuen i beskedskrivningsværktøjet.

You can define contact groups with the Android contacts app, please see [here](https://support.google.com/contacts/answer/30970) for instructions.

<br />

<a name="faq153"></a>
**(153) Why does permanently deleting Gmail message not work?**

You might need to change [the Gmail IMAP settings](https://mail.google.com/mail/u/0/#settings/fwdandpop) on a desktop browser to make it work:

* When I mark a message in IMAP as deleted: Auto-Expunge off - Wait for the client to update the server.
* When a message is marked as deleted and expunged from the last visible IMAP folder: Immediately delete the message forever

Note that archived messages can be deleted only by moving them to the trash folder first.

Some background: Gmail seems to have an additional message view for IMAP, which can be different from the main message view.

En anden mærkværdighed er, at en stjerne (favoritbesked) sat via webbrugerfladen ikke kan fjernes med IMAP-kommandoen

```
STORE <message number> -FLAGS (\Flagged)
```

På den anden side, bliver en stjerne sat via IMAP vist i webbrugerfladen og kan fjernes via IMAP.

<br />

<a name="faq154"></a>
**~~(154) Kan der blive tilføjet favikoner som kontaktfotos?~~**

~~Udover at et [favikon](https://en.wikipedia.org/wiki/Favicon) kan deles af mange e-mailadresser med samme domænenavn,~~ ~~~og derfor ikke er direkte relateret til en e-mailadresse, kan disse bruges til at spore brugeren.~~

<br />

<a name="faq155"></a>
**(155) Hvad er en winmail.dat-fil?**

En *winmail.dat-fil* sendes af en forkert opsat Outlook-klient. Det er et Microsoft-specifikt filformat ([TNEF](https://en.wikipedia.org/wiki/Transport_Neutral_Encapsulation_Format)) indeholdende en besked og muligvis vedhæftninger.

Yderligere oplysninger om denne fil kan findes [hér](https://support.mozilla.org/en-US/kb/what-winmaildat-attachment).

Filen kan vises med en Android-app såsom [Letter Opener](https://play.google.com/store/apps/details?id=app.letteropener).

<br />

<a name="faq156"></a>
**(156) Hvordan opsættes en Office 365-konto?**

En Office 365-konto kan oprettes via guiden hurtigopsætning ved valg af *Office 365 (OAuth)*.

Afsluttes guiden med *AUTHENTICATE failed*, kan IMAP og/eller SMTP være deaktiveret for kontoen. I så tilfælde skal administratoren anmodes om at aktivere IMAP og SMTP. Proceduren dokumenteres [hér](https://docs.microsoft.com/en-in/exchange/troubleshoot/configure-mailboxes/pop3-imap-owa-activesync-office-365).

Er *sikkerhedsstandarder* aktiveret i din organisation, kan du være nødt til at aktivere SMTP AUTH-protokollen. Tjek [hér](https://docs.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/authenticated-client-smtp-submission) ang. hvordan.

<br />

<a name="faq157"></a>
**(157) Hvordan opsættes en Free.fr-konto?**

Tjek [hér](https://free.fr/assistance/597.html) ang. instruktioner.

** SMTP er som standard deaktiveret **, tjek [hér](https://free.fr/assistance/2406.html) ang. aktivering af den.

Tjek [hér](http://jc.etiemble.free.fr/abc/index.php/trucs-astuces/configurer-smtp-free-fr) ang. detaljeret vejledning.

<br />

<a name="faq103"></a>
<a name="faq158"></a>
**(158) Hvilke(t/n) kamera/lydoptager anbefaler du?**

For at tage fotos og optage lyd kræves en kamera- samt lydoptager-app. Flg. er open-source kamera- og lydoptager-apps:

* [Open Camera](https://play.google.com/store/apps/details?id=net.sourceforge.opencamera) ([F-Droid](https://f-droid.org/en/packages/net.sourceforge.opencamera/))
* [Audio Recorder version 3.3.24+](https://play.google.com/store/apps/details?id=com.github.axet.audiorecorder) ([F-Droid](https://f-droid.org/packages/com.github.axet.audiorecorder/))

For at optage stemmenotater mv., skal lydoptageren understøtte [MediaStore.Audio.Media.RECORD_SOUND_ACTION](https://developer.android.com/reference/android/provider/MediaStore.Audio.Media#RECORD_SOUND_ACTION). Mærkværdigvis synes de fleste lydoptagere ikke at understøtte denne standard Android-handling.

<br />

<a name="faq159"></a>
**(159) Hvad er Disconnect's tracker-beskyttelseslister?**

Tjek [hér](https://disconnect.me/trackerprotection) for yderligere oplysninger om Disconnect's tracker-beskyttelseslister.

Efter download af listerne via fortrolighedsindstillingerne, kan disse anvendes:

* to warn about tracking links on opening links
* to recognize tracking images in messages

Trackingsbilleder deaktiveres kun, såfremt den tilsvarende hovedindstilling 'deaktivere' er slået til.

Tracking-billeder genkendes ikke, såfremt domænet er klassificeret som '*Indhold*', tjek [hér](https://disconnect.me/trackerprotection#trackers-we-dont-block) for yderligere oplysninger.

Denne kommando kan sendes til FairEmail fra en automatiserings-app for at opdatere beskyttelseslisterne:

```
(adb shell) am start-foreground-service -a eu.faircode.email.DISCONNECT.ME
```

Opdatring én gang ugentligt vil formentlig være tilstrækkeligt, se [hér](https://github.com/disconnectme/disconnect-tracking-protection/commits/master) for nylige listeændringer.

<br />

<a name="faq160"></a>
**(160) Kan der blive tilføjet permanent sletning af beskeder uden bekræftelse?**

Permanent sletning af beskeder er en *irreversibel handling*, så for at forhindre utilsigtet tab af beskeder, skal sletning altid bekræftes. Selv med en bekræftelse, har nogle meget vrede personer, som mistede nogle af deres beskeder grundet egne fejl, kontaktede mig, hvilket var en ret ubehagelig oplevelse :-(

Since version 1.1601 it is possible to disable confirmation of permanent deletion of individual messages.

Note that the POP3 protocol can download messages from the inbox only. So, deleted messages cannot be uploaded to the inbox again. This means that messages can only be permanently deleted when using a POP3 account.

Avanceret: IMAP-sletteflaget i kombination med EXPUNGE-kommandoen kan ikke understøttes, da hverken e-mailservere eller alle personer kan håndtere dette med risiko for uventet tab af beskeder til følge. En yderligere komplikation er, at ikke alle e-mailservere understøtter [UID EXPUNGE](https://tools.ietf.org/html/rfc4315).

Fra version 1.1485 er det muligt midlertidigt at aktivere fejlfindingstilstand i diverse indstillingerne for at deaktivere beskedtømning. Bemærk, at beskeder med et *\Slettet*-flag ikke bliver vist i FairEmail.

<br />

<a name="faq161"></a>
**(161) Kan der blive tilføjet en indstilling til ændring af primær- og accentfarverne?***

Om muligt blev der med det samme tilføjet en indstilling til valg af primær- og accentfarver, men Android-temaer er desværre statiske, tjek f.eks. [hér](https://stackoverflow.com/a/26511725/1794097), så dette er ikke muligt.

<br />

<a name="faq162"></a>
**(162) Understøttes IMAP NOTIFY?***

Ja, [IMAP NOTIFY](https://tools.ietf.org/html/rfc5465) har været understøttet siden version 1.1413.

IMAP NOTIFY-understøttelse betyder, at der anmodes om notifikationer for tilføjede, ændrede eller slettede beskeder fra alle *abonnerede* mapper, og modtages en notifikation for en abonnentmappe, så synkes denne mappen. Synk af abonnentmapper kan derfor deaktiveres, hvilket reducerer mappeforbindelser til e-mailserveren.

**Vigtig**: Push- beskeder (=always sync) for indbakken og abonnementshåndtering (modtagelsesindstillinger) skal aktiveres.

**Vigtigt**: De fleste e-mailservere understøtter ikke dette! Loggen kan via navigationsmenuen tjekkes for, om en e-mailserver understøtter NOTIFY-muligheden.

<br />

<a name="faq163"></a>
**(163) What is message classification?**

*Dette er en eksperimentel funktion!*

Beskedklassificering vil forsøge automatisk at gruppere e-mails i klasser baseret på deres indhold vha. [Bayesian-statistik](https://en.wikipedia.org/wiki/Bayesian_statistics). I FairEmail-kontekst udgør en mappe en klasse. Så f.eks. indbakken, Spam-mappen, en 'markedsføringsmappe' mv.

Beskedklassificering kan aktiveres under diverse indstillinger. Dette aktiverer kun indlæringstilstand. Klassifikatoren vil som standard 'lære' af nye beskeder i indbakken og Spam-mappen. Mappeegenskaben *Klassificér nye beskeder i denne mappe* vil aktivere eller deaktivere 'indlæringstilstand' for en mappe. Lokale beskeder kan ryddes (langt tryk på en kontos mappe i mappelisten) og herefter gensynkroniseres for at klassificere beskederne.

Hver mappe har indstillingen *Flyt automatisk klassificerede beskeder til denne mappe* ('auto-klassificering', kort sagt). Når dette er slået til, vil nye beskeder i andre mapper, som klassifikatoren mener hører til den mappe, automatisk blive flyttet.

The option *Use local spam filter* in the report spam dialog will turn on message classification in the miscellaneous settings and auto classification for the spam folder. Please understand that this is not a replacement for the spam filter of the email server and can result in [false positives and false negatives](https://en.wikipedia.org/wiki/False_positives_and_false_negatives). Se også [denne FAQ](#user-content-faq92).

Et praktisk eksempel: Antag, at mappen 'markedsføring' findes og automatisk beskedklassificering er aktiveret for denne. Hver gang en besked flyttes til denne mappe, trænes FairEmail i, at lignende beskeder hører til hér. Hver gang en besked flyttes fra denne mappe, trænes FairEmail i, at lignende beskeder ikke hører til hér. After moving some messages into the 'marketing' folder, FairEmail will start moving similar messages automatically into this folder. Or, the other way around, after moving some messages out of the 'marketing' folder, FairEmail will stop moving similar messages automatically into this folder. This will work best with messages with similar content (email addresses, subject and message text).

Klassificering bør betragtes som et bedste gæt - det kan være et forkert gæt, eller klassificatoren er måske for usikker til at foretage gæt. Er klassificatoren usikker, lader den blot en e-mail blive, hvor den er.

For at forhindre e-mailserveren i at flytte en besked til Spam-mappen igen og igen, vil autoklassificering ud af Spam-mappen ikke ske.

Beskedklassifikatoren beregner sandsynligheden for, at en besked hører til i en mappe (klasse). Der er to indstillinger i diverse indstillingerne, som styrer, om en besked auto-flyttes til en mappe, for hvilken auto-klassificering er aktiv:

* *Minimum class probability*: a message will only be moved when the confidence it belongs in a folder is greater than this value (default 15 %)
* *Minimum class difference*: a message will only be moved when the difference in confidence between one class and the next most likely class is greater than this value (default 50 %)

Begge betingelser skal være opfyldt, før en besked flyttes.

Considering the default option values:

* Æbler 40% og bananer 30% vil blive tilsidesat, da forskellen på 25% ligger under et minimum på 50%
* Æbler 10% og bananer 5% vil blive tilsidesat, da sandsynligheden for æbker er under et minimum på 15%
* Æbler 50% og bananer 20% vil resultere i valg af æbler

Klassificeringen er optimeret til brug af så få ressourcer som muligt, men vil uundgåeligt bruge ekstra strøm.

Alle klassificeringsdata kan slettes ved i diverse indstillingerne at slå klassificeringen fra tre gange.

[Filtreringsregler](#user-content-faq71) eksekveres før klassificering.

Beskedklassificering er en Pro-funktion, undtagen for Spam-mappen.

<br />

<a name="faq164"></a>
**(164) Kan der blive tilføjet tilpasselige temaer?**

Unfortunately, Android [does not support](https://stackoverflow.com/a/26511725/1794097) dynamic themes, which means all themes need [to be predefined](https://github.com/M66B/FairEmail/blob/master/app/src/main/res/values/styles.xml).

Da hvert tema kræver en lys, mørk og sort variant, er det ikke muligt at tilføje et prædefineret tema for hver farvekombination (bogstaveligt talt millioner).

Derudover er et tema mere end blot et par farver. F.eks. bruger temaer med gul fremhævningsfarve en mørkere linkfarve af kontrasthensyn.

Temafarverne er baseret på farvecirklen fra [Johannes Itten](https://en.wikipedia.org/wiki/Johannes_Itten).

<br />

<a name="faq165"></a>
**(165) Is Android Auto supported?**

Yes, Android Auto is supported, but only with the GitHub version, please [see here](https://forum.xda-developers.com/t/app-5-0-fairemail-fully-featured-open-source-privacy-oriented-email-app.3824168/post-83801249) about why.

For notification (messaging) support you'll need to enable the following notification options:

* *Benyt Android 'beskedstil' notifikationsformat*
* Notification actions: *Direct reply* and (mark as) *Read*

You can enable other notification actions too, if you like, but they are not supported by Android Auto.

The developers guide is [here](https://developer.android.com/training/cars/messaging).

<br />

<a name="faq166"></a>
**(166) Kan en besked slumres på tværs af flere enheder?**

Da der ingen standarder for beskedslumring, er alle slumringsimplementeringer er tilpassede løsninger.

Nogle e-mailudbydere flytte slumrede beskeder til en særlig mappe. Desværre har tredjeparts-apps ingen adgang til denne specielle mappe.

Flytning af en besked til en anden mappe og tilbage mislykkes måske og er ikke mulig uden Internetforbindelse. Dette er problematisk, da en besked kun kan slumres efter at være blevet flyttet.

For at forhindre disse problemer, sker slumring lokalt på enheden ved at skjule beskeden under slumring. Desværre er det ikke muligt også at skjule beskeder på e-mailserveren.

<br />

<a name="faq167"></a>
**(167) How can I use DeepL?**

1. Enable [experimental features](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-faq125) in the miscellaneous settings
1. [Subscribe to](https://www.deepl.com/pro#developer) the DeepL API Free or Pro plan (credit card required)
1. [Copy](https://www.deepl.com/pro-account/plan) the authentication key
1. In the message composer tap on the faint translate button (文A), select *Configure* and paste the key

You might want to read the [privacy policy](https://www.deepl.com/privacy/) of DeepL.

This feature requires an internet connection and is not available in the Play store version.

<br />

<h2><a name="get-support"></a>Få support</h2>

FairEmail is supported on Android smartphones and tablets and ChromeOS only.

Kun seneste Play Butik- og GitHub-versioner understøttes. F-Droid build understøttes kun, hvis versionsnummeret er det samme som versionsnummeret for den seneste GitHub-udgivelse. Dette betyder også, at nedgradering ikke understøttes.

Der er ingen support for ting, som ikke er direkte relateret til FairEmail.

Der er ingen support til bygning og udvikling af ting fra dig selv.

Anmodede funktioner skal:

* være til gavn for flest brugere
* ikke komplicere brugen af FairEmail
* passer ind i FairMail-filosofien (fortroligheds- og sikkerhedsorienteret)
* overholde fællesstandarder (IMAP, SMTP mv.)

Funktioner, som ikke opfylder disse krav, afvises sandsynligvis. Dette er også for at muliggøre vedligeholdelse og support i det lange løb.

Har du spørgsmål, ønsker til en funktion eller vil anmelde en fejl, **så benyt [denne formular](https://contact.faircode.eu/?product=fairemailsupport)**.

GitHub-problemstillinger er deaktiveret grundet hyppigt misbrug.

<br />

Ophavsrettigheder &copy; 2018-2021 Marcel Bokhorst.
