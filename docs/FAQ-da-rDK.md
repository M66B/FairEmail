# FairEmail-support

Har du et spørgsmål, så tjek venligst de ofte stillede spørgsmål nedenfor først. Nederst kan du se, hvordan du stiller yderligere spørgsmål, anmoder om funktioner samt indrapporterer fejl.

## Indeks

* [Godkendelse af konti](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-authorizing-accounts)
* [Hvordan kan man...?](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-howto)
* [Kendte problemer](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-known-problems)
* [Planlagte funktioner](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-planned-features)
* [Hyppigt anmodede funktioner](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-frequently-requested-features)
* [Ofte stillede spørgsmål](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-frequently-asked-questions)
* [Få support](https://github.com/M66B/FairEmail/blob/master/FAQ.md#user-content-get-support)

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
* En forhåndsvisning af en beskedtekst vises ikke (altid) på Samsung-ure, da [setLocalOnly](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder.html#setLocalOnly(boolean))-parameteren synes at blive ignoreret. Beskedforhåndsvisningstekster fremgår korrekt på Pebble 2-, Fitbit Charge 3- og Mi band 3-wearables. Se også [denne FAQ](#user-content-faq126).
* En [fejl i Android](https://issuetracker.google.com/issues/37068143) medfører af og til et nedbrud med "*... Ugyldig forskydning... Gyldigt område er ...* når tekst er valgt og der trykkes uden for den valgte tekst. Denne fejl er rettet i Android 6.0.1.
* Interne (anker-) links vil ikke fungere, da de oprindelige beskeder vises i et indlejret WebView i en rullevisning (samtalelisten). Dette er en Android-begrænsning, der ikke kan rettes eller omgås.
* Sprogdetektering [fungerer ikke længere](https://issuetracker.google.com/issues/173337263) på Pixel-enheder med/ opgraderet til Android 11

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
* [(160) Kan der blive tilføjet permanent sletning af beskeder uden bekræftelse?](#user-content-faq160)
* [(161) Kan der blive tilføjet en indstilling til ændring af primær- og accentfarverne?](#user-content-faq161)

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

* Android 8 Oreo og senere: Tryk på *Modtagekanal*-knappen og deaktivér kanalen via Android-indstillingerne (dette deaktiverer ikke notifikationer for nye besked)
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

Alternativt kan du acceptere fingeraftryk på ugyldige servercertifikater som dette:

1. Sørg for at benytt en betroet Internetforbindelse (ingen offentlige Wi-Fi netværk mv.)
1. Gå til opsætningsskærmen via navigeringsmenuen (stryg fra venstre side indad)
1. Tryk i trin 1 og 2 på *Håndtér* og tryk på den defekte konto og identitet
1. Tjek/gem kontoen og identiteten
1. Markér afkrydsningsfeltet under fejlmeddelelsen og gem igen

Dette vil "fastgøre" servercertifikatet for at forhindre man-in-the-middle angreb.

Bemærk, at ældre Android-versioner muligvis ikke genkender nyere certificeringsmyndigheder såsom Let’s Encrypt, hvorfor forbindelser kan blive betragtet som usikre, tjek også [hér](https://developer.android.com/training/articles/security-ssl).

*Trust anchor til certificeringssti ikke fundet*

*... java.security.cert.CertPathValidatorException: Trust anchor til certificeringssti ikke fundet...* betyder, at Androids standard trust manager ikke var i stand til at bekræfte servercertifikatkæden.

Du bør enten rette serveropsætningen eller acceptere fingeraftrykket vist nedenfor fejlmeddelelsen.

Bemærk, at dette problem kan skyldes af, at serveren ikke sender alle mellemliggende certifikater også.

*Ingen adgangskode*

Dit brugernavn er formentlig let at gætte, så dette er usikkert.

*Simpel tekst-forbindelse*

Dit brugernavn, adgangskode samt alle beskeder sendes og modtages ukrypteret, hvilket er **meget usikkert**, da et [man-im-the-middle angreb](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) nemt udføres på en ukrypteret forbindelse.

Vil du fortsat bruge et ugyldigt sikkerhedscertifikat, ingen adgangskode eller en simpel tekst-forbindelse, så skal usikre forbindelser aktiveres i konto- og/eller identitetsindstillingerne. STARTTLS bør vælges til simpel tekst-forbindelser. Aktiverer du usikre forbindelser, bør du kun oprette forbindelse via private, betroede netværk og aldrig via offentligt tilbudte netværk i lufthavne, hoteller mv.

<br />

<a name="faq5"></a>
**(5) Hvordan tilpasses beskedvisningen?**

Via trepriksmenuen kan du aktivere/deaktivere eller vælge:

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

Bemærk, at kun downloadede beskedtekster kan forhåndsvises. Større beskedtekster downloades som standard ikke på afregnede (primært mobile) netværk. Dette kan ændres under Indstillinger.

I tilfælde af en lang adresseliste kan du benytte *mindre*-ikonet øverst i adresseseafsnittet til at kollapse den.

Nogle har spurgt:

* at få emnet vist med fed tekst, men fed benyttes allerede til fremhævelse af ulæste beskeder
* at få adresse- eller emnevisningen gjort større/mindre, men dette vil forstyrre indstillingen for tekststørrelse
* at få stjernen flyttet til venstre, men det er meget lettere at betjene stjernen på højre side

Det er umuligt at stille alle tilpas, og at tilføje mange indstillinger ville ikke kun være forvirrende, men heller aldrig være tilstrækkeligt.

<br />

<a name="faq6"></a>
**(6) Hvordan logges ind på Gmail/G Suite?**

Du kan benytte hurtig opsætningsguiden til nemt at opsætte en Gmail-konto og -identitet.

Ønsker du ikke at benytte en Gmail-konto på enheden, kan du enten aktivere adgang for "mindre sikre apps" og benytte din kontoadgangskode (ikke anbefalet) eller aktivere tofaktorgodkendelse og benytte en app-specifik adgangskode. For brug af en adgangskode skal du opsætte en konto og identitet via opsætningstrin 1 og 2 i stedet for via hurtigopsætningsguiden.

Tjek i [denne FAQ](#user-content-faq111), hvorfor kun konti på enheden kan benyttes.

Bemærk, at en app-specifik adgangskode kræves, når tofaktorgodkendelse er aktiveret.

<br />

*App-specifik adgangskode*

Tjek [hér](https://support.google.com/accounts/answer/185833), hvordan en app-specifik adgangskode genereres.

<br />

*Aktivér "Mindre sikre apps"*

**Vigtigt**: Brug af denne metode anbefales ikke, da den er mindre pålidelig.

**Vigtigt**: Brugernavn-/adgangskode-godkendte Gsuite-konti vil [i nærmeste fremtid](https://gsuiteupdates.googleblog.com/2019/12/less-secure-apps-oauth-google-username-password-incorrect.html) ophøre med at fungere.

Tjek [hér](https://support.google.com/accounts/answer/6010255), hvordan "Mindre sikre apps" aktiveres eller gå [direkte til Indstillinger](https://www.google.com/settings/security/lesssecureapps).

Benyttes flere Gmail-konti, så tjek indstillingen "Mindre sikre apps" ændres på den/de korrekte kont(o/i).

Bemærk, at du er nødt til at forlade "Mindre sikre apps"-indstillingsskærmen vha. Tilbage-pilen, for at effektuere indstillingen.

Benyttes denne metode, bør du anvende en [stærk adgangskode](https://en.wikipedia.org/wiki/Password_strength) til din Gmail-konto (i øvrigt altid er en god idé). Bemærk, at brug af [standard](https://tools.ietf.org/html/rfc3501) IMAP-protokollen ikke i sig selv er mindre sikker.

Når "Mindre sikre apps" ikke er aktiveret, får du fejlen *Godkendelse mislykkedes - ugyldige akkreditiver* for konti (IMAP) og *Brugernavn og adgangskode ikke accepteret* for identiteter (SMTP).

<br />

*Generelt*

Du får muligvis advarslen "*Log ind via din webbrowser*". Dette sker, når Google anser det netværk, hvormed du forbinder til Internet (hvilket kan være et VPN) som ikke-sikkert. Dette kan forhindres vha. Gmails hurtig opsætningsguide eller en app-specifik adgangskode.

Tjek [hér](https://support.google.com/mail/answer/7126229) for Googles instruktioner, og [hér](https://support.google.com/mail/accounts/answer/78754) for fejlfinding.

<br />

<a name="faq7"></a>
**(7) Hvorfor vises sendte beskeder ikke (direkte) i Sendt-mappen?**

Sendte beskeder flyttes normalt fra udbakken til Sendt-mappen, så snart din udbyder føjer sendte beskeder til Sendt-mappen. Dette kræver, at en Sendt-mappe vælges i kontoindstillingerne, og at Sendt-mappen ligeledes opsættes til synkronisering.

Visse udbydere holder ikke styr på sendte beskeder, eller den anvendte SMTP-server er muligvis ikke relateret til udbyderen. I så tilfælde tilføjer FairEmail automatisk sendte beskeder til Sendt-mappen under synkronisering af denne, hvilket vil ske, efter at en besked er afsendt. Bemærk, at dette vil forøge Internettrafikken.

~~Sker dette ikke, holder din udbyder muligvis ikke styr på sendte beskeder, eller du anvender muligvis en ikke-udbyderrelateret SMTP-server.~~ ~~I så tilfælde kan du aktivere den avancerede identitetsindstilling *Gem sendte beskeder* for at lade FairEmail føje sendte beskeder til Sendt-mappen umiddelbart efter afsendelsen.~~ ~~Bemærk, at aktivering af denne indstilling kan resultere i dubletbeskeder, hvis/når din udbyder føjer sendte beskeder til Sendt-mappen.~~ ~~Bemærk også, at aktivering af indstillingen vil resultere i forøget dataforbrug især ved afsendelse af beskeder med store vedhæftninger.~~

~~Hvis sendte beskeder i udbakken ikke findes i Sendt-mappen ved en fuld synkronisering, flyttes disse også fra udbakken til Sendt-mappen.~~ ~~En fuld synkronisering sker, når der genforbindes til serveren, eller ved periodisk eller manuel synkronisering.~~ ~~Du ønsker sandsynligvis i stedet at aktivere den avancerede indstilling *Gem sendte beskeder* for at flytte beskeder til Sendt-mappen hurtigere.~~

<br />

<a name="faq8"></a>
**(8) Kan en Microsoft Exchange-konto benyttes?**

En Microsoft Exchange-konto kan benyttes, hvis den er tilgængelig via IMAP, hvilket normalt er tilfældet. Tjek [hér](https://support.office.com/en-us/article/what-is-a-microsoft-exchange-account-47f000aa-c2bf-48ac-9bc2-83e5c6036793) for yderligere information.

Tjek [hér](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040) for Microsoft-dokumentation vedr. opsætning af en e-mailklient. Der er også et afsnit om almindelige forbindelsesfejl og løsninger heraf.

Visse ældre Exchange-serverversioner har en fejl, der forårsager tomme beskeder og ødelagte vedhæftninger. Tjek [denne FAQ](#user-content-faq110) for en løsning.

Tjek [denne FAQ](#user-content-faq133) vedr. ActiveSync-understøttelse.

Tjek [denne FAQ](#user-content-faq111) vedr. OAuth-understøttelse.

<br />

<a name="faq9"></a>
**(9) Hvad er identiteter/hvordan tilføjes et alias?**

Identiteter repræsenterer e-mailadresser, du sender *fra* via en (SMTP) e-mailserver.

Visse udbydere tillader brug af flere aliaser. Du kan opsætte disse ved at opsætte e-mailadressefeltet fra en ekstra identitet til aliasadressen og opsætte brugernavnefeltet til din hoved e-mailadresse.

Bemærk, at en identitet kan kopieres vha. et langt tryk på den.

Alternativt kan *Tillad redigering af afsenderadresse* aktiveres i de avancerede indstillinger for en eksisterende identitet for at redigere brugernavnet, når du skriver en ny besked, forudsat din udbyder tillader dette.

FairEmail opdaterer automatisk adgangskoder til relaterede identiteter, når adgangskoden til den tilknyttede konto/relateret identitet opdateres.

Tjek [denne FAQ](#user-content-faq33) vedr. redigering af brugernavnet til e-mailadresser.

<br />

<a name="faq10"></a>
**~~(10) Hvad betyder 'UIDPLUS ikke understøttet'?~~**

~~Fejlmeddelelsen *UIDPLUS ikke understøttet * betyder, at din e-mailudbyder ikke tilbyder IMAP [UIDPLUS-udvidelsen](https://tools.ietf.org/html/rfc4315). Denne IMAP-udvidelse kræves for at implementere en tovejssynkronisering, der ikke er en valgfri funktion. Så medmindre din udbyder kan aktivere denne udvidelse, kan du ikke benytte FairEmail til denne udbyder.~~

<br />

<a name="faq11"></a>
**~~(11) Hvorfor understøttes POP ikke'?~~**

~~Udover at enhver anstændig e-mailudbyder understøtter [IMAP](https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol) i dag,~~ ~~vil brug af [POP](https://en.wikipedia.org/wiki/Post_Office_Protocol) resultere i unødvendig ekstra batteriforbrug samt forsinkede notifikationer om nye beskeder.~~ ~~Desuden er POP uegnet til tovejssynkronisering, og oftere end ikke, læser og skriver folk beskeder på forskellige enheder i dag.~~

~~Grundlæggende understøtter POP kun download og sletning af beskeder fra indbakken.~~ ~~Almindelige operationer, såsom indstilling af besekedattributter (læst, stjernemarkeret, besvaret mv.), tilføjelse (sikkerhedskopiering) samt flytning af beskeder ikke er derfor ikke mulige.~~

~~Tjek også, [hvad Google skriver om dette](https://support.google.com/mail/answer/7104828).~~

~~F.eks. kan [Gmail importere beskeder](https://support.google.com/mail/answer/21289) fra en anden POP-konto,~~ ~~hvilket kan anvendes som en løsning, hvis din udbyder ikke understøtter IMAP.~~

~~tl;dr; overvej skift til IMAP.~~

<br />

<a name="faq12"></a>
**(12) Hvordan fungerer kryptering/dekryptering?**

Kommunikation med e-mailservere er altid krypteret, medmindre du eksplicit har slået dette fra. Dette spørgsmål omhandler valgfri end-to-end kryptering med PGP eller S/MIME. Afsender og modtager bør først aftale dette og udveksle signerede beskeder for at overføre deres offentlige nøgler for at kunne sende krypterede beskeder.

*Generelt*

[Tjek hér](https://en.wikipedia.org/wiki/Public-key_cryptography), hvordan kryptering vha. offentlige/private nøgler fungerer.

Kryptering kort fortalt:

* **Udgående** beskeder krypteres vha. modtagerens **offentlige nøgle**
* **Indgående** beskeder dekrypteres vha. modtagerens **private nøgle**

Signering kort fortalt:

* **Udgående** beskeder signeres med afsenderens **private nøgle**
* **Indgående** beskeder bekræftes vha. afsenderens **offentlige nøgle**

For at signere/kryptere en besked, vælg den passende metode i Send-dialogen. Er der tidliger valgt *Vis ikke igen* til Send-dialogen, kan den altid åbnes igen vha. trepunktsmenuen.

For at bekræfte en signatur eller dekryptere en modtaget besked, så åbn beskeden og tryk blot på gestussen eller hængelåsikonet umiddelbart under beskedhandlingsbjælken.

Første gang du sender en signeret/krypteret besked, anmodes du muligvis om en signaturnøgle. Mhp. efterfølgende brug gemmer FairEmail automatisk den valgte signeringsnøgle i den anvendte identitet. Er der behov for at nulstille signeringsnøglen, så gem blot identiteten eller brug et langt tryk på identiteten på identitetslisten og vælg *Nulstil signeringsnøgle*. Den valgte signeringsnøgle er synlig på identitetslisten. Ved behov for at vælge nøgle fra gang til gang, kan der oprettes flere identiteter for den samme konto med den samme e-mailadresse.

I fortrolighedsindstillingerne kan du vælge standardkrypteringsmetoden (PGP eller S/MIME), aktivere *Signér som standard*, *Kryptér som standard* samt *Dekryptér automatisk beskeder*, men vær opmærksom på, at automatisk dekryptering ikke er mulig, hvis brugerinteraktion kræves, såsom valg af en nøgle eller læsning af et sikkerhedstoken.

Beskedtekster/vedhæftninger, som skal krypteres, samt dekrypterede beskedtekster/vedhæftninger gemmes kun lokalt og overføres aldrig til den eksterne server. Vil du fortryde dekryptering, kan du benytte menupunktet *gensynk* fra trepunktsmenuen på beskedhandlingsbjælken.

*PGP*

Du skal først installere og opsætte [OpenKeychain](https://f-droid.org/en/packages/org.sufficientlysecure.keychain/). FairEmail er aftestet med OpenKeychain version 5.4. Senere versioner er sansynligvis kompatible, men tidligere versioner er muligvis ikke.

**Vigtigt**: OpenKeychain-appen er kendt for at gå ned (upåagtet), når den kaldende app (FairEmail) endnu ikke er godkendt og får en eksisterende offentlig nøgle. Du kan imødegå dette ved at prøve at sende en signeret/krypteret besked til en afsender med en ukendt offentlig nøgle.

**Vigtigt**: Kan OpenKeychain-appen ikke finde en nøgle (længere), skal du muligvis nulstille en tidligere valgt nøgle. Dette kan gøres via et langt tryk på en identitet i identitetsoversigten (Opsætning, trin 2, Håndtér).

**Vigtigt **: For pålideligt at lade apps såsom FairEmail oprette forbindelse til OpenKeychain-tjenesten for beskedkryptering/-dekryptering, kan deaktivering af batterioptimering for OpenKeychain-appen være nødvendig.

**Vigtigt**: OpenKeychain-appen har angiveligt behov for kontaktpersontilladelse for at fungere korrekt.

**Vigtigt**: På visse Android-versioner/-enheder er det nødvendigt at aktivere *Vis popups i baggrundstilstand* i de udvidede tilladelser af Android app-indstillingerne i OpenKeychain-appen. Uden denne tilladelse gemmes udkastet, men OpenKeychain-popup'en til bekræftelse/valg vises måske ikke.

FairEmail vil sende [Autocrypt](https://autocrypt.org/)-headeren til brug for andre e-mailklienter, men kun for signerede og krypterede beskeder, da for mange e-mailservere har problemer med den ofte lange Autocrypt-header. Bemærk, at den mest sikre måde at starte en krypteret e-mailudveksling på, er, ved først at sende signerede beskeder. Modtagne Autocrypt-headers sendes til, og lagres af, OpenKeychain-appen til brug for fremtidige signaturbekræftelser eller beskeddekrypteringer.

Al nøglehåndtering uddelegeres af sikkerhedsårsager til OpenKey-chain appen. Dette betyder også, at FairEmail ikke lagre PGP-nøgler.

Inline-krypteret PGP i modtagne beskeder understøttes, men inline PGP-signaturer og inline PGP i udgående beskeder understøttes ikke, tjek [hér](https://josefsson.org/inline-openpgp-considered-harmful.html) vedr. årsagen.

Kun signerede eller kun krypterede beskeder er ikke en god idé. Tjek hér for årsagen:

* [Overvejelser vedr. OpenPGP, del I](https://k9mail.github.io/2016/11/24/OpenPGP-Considerations-Part-I.html)
* [Overvejelser vedr. OpenPGP, del II](https://k9mail.github.io/2017/01/30/OpenPGP-Considerations-Part-II.html)
* [Overvejelser vedr. OpenPGP, del III, Autocrypt](https://k9mail.github.io/2018/02/26/OpenPGP-Considerations-Part-III-Autocrypt.html)

Kun signarede beskeder understøttes, kun krypterede beskeder understøttes ikke.

Almindelige fejl:

* *Ingen nøgle*: Ingen PGP-nøgle tilgængelig for en af de angivne e-mailadresser
* *Manglende krypteringsnøgle*: Der er sandsynligvis valgt en nøgle i FairEmail, der ikke længere forefindes i OpenKeychain-appen. Nulstilling af nøglen (se ovenfor) løser sandsynligvis dette problem.

*S/MIME*

En offentlig modtagernøgle kræves ved kryptering af en besked. Din private nøgle kræves ved beskedsignering.

Private nøgler lagres af Android og kan importeres via Androids avancerede sikkerhedsindstillinger. Der er en genvej (knap) til dette under fortrolighedsindstillingerne. Android vil bede dig opsætte PIN-kode, mønster eller adgangskode, hvis dette ikke allerede er gjort. Har du en Nokia-enhed med Android 9, så [læs først dette](https://nokiamob.net/2019/08/10/a-bug-prevents-nokia-1-owners-from-unlocking-their-screen-even-with-right-pin-pattern/).

Bemærk, at certifikater kan indeholde flere nøgler til flere formål, f.eks. til godkendelse, kryptering og signering. Android importerer kun den første nøgle. For at importere alle nøgler, skal certifikatet først opdeles. Dette er en vigtig handling, så du rådes til at bede certifikatleverandøren om support.

Bemærk, at S/MIME-signering med andre algoritmer end RSA understøttes, hvilket dog ikke er tilfældet for alle e-mailklienter. S/MIME-kryptering er kun mulig med symmetriske algoritmer, hvilket i praksis betyder brug af RSA.

Standardkrypteringsmetoden er PGP, men den sidst anvendte krypteringsmetode huskes for den valgte identitet. Det kan være nødvendigt igen at aktivere Send-mulighederne i trepriksmenuen for at kunne vælge krypteringsmetode.

For at tillade forskellige private nøgler til den samme e-mailadresse lader FairEmail dig altid vælge en nøgle, når der er flere identiteter til den samme e-mailadresse for den samme konto.

Offentlige nøgler lagres af FairEmail og kan importeres ifm. førstegangsbekræftelse af en signatur eller via fortrolighedsindstillingerne (PEM- eller DER-format).

FairEmail foretager bekræftelse af både signaturen samt den komplette certifikatkæde.

Almindelige fejl:

* *Intet certifikat fundet, der matcher targetContraints*: Dette betyder sandsynligvis, at en gammel version af FairEmail benyttes
* *kunne ikke finde en gyldig certificeringssti til det anmodede mål*: Dette betyder grundlæggende, at der ikke blev fundet en eller flere mellem- eller rodcertifikater
* *Privat nøgle matcher ikke nogle krypteringsnøgler*: Den valgte nøgle kan ikke anvendes til beskeddekrypteringen, da den sandsynligvis ikke er den korrekte nøgle
* *Ingen privat nøgle*: Intet certifikat er valgt eller intet certifikat var tilgængeligt i Android-nøglelageret

Er certifikatkæden forkert, kan du trykke på den lille infoknap for at få vist alle certifikaterne. Udstederen eller "selfSign" vises efter certifikatoplysningerne. Et certifikater selvsigneret, når både emne og udsteder er identiske. Certifikater fra en certifikatmyndighed (CA) er markeret med "[keyCertSign](https://tools.ietf.org/html/rfc5280#section-4.2.1.3)". Certifikater i Android-nøglelageret er markeret med "Android".

En gyldig kæde ser således ud:

```
Dit certifikat > nul eller flere mellemliggende certifikater > CA (root) certifikat markeret med "Android"
```

Bemærk, at en certifikatkæde altid er ugyldig, hvis intet ankercertifikat findes i Android-nøglelageret, hvilket er fundamentalt for S/MIME-certifikatbekræftelse.

Tjek [hér](https://support.google.com/pixelphone/answer/2844832?hl=en), hvordan certifikater kan importeres til Android-nøglelageret.

Brug af udløbne nøgler, inline-krypterede/signerede beskeder samt hardwaresikkerhedstokens er uunderstøttet.

Kigger du efter et gratis (test) S/MIME-certifikat, tjek [hér](http://kb.mozillazine.org/Getting_an_SMIME_certificate) for muligheder. Husk at [læse dette først](https://davidroessli.com/logs/2019/09/free-smime-certificates-in-2019/#update20191219) ved anmodning om et S/MIME Actalis-certifikat. Kigger du efter et billigt S/MIME-certifikat, havde jeg en god oplevelse med [Certum](https://www.certum.eu/en/smime-certificates/).

Sådan udpakkes en offentlig nøgle fra et S/MIME-certifikat:

```
openssl pkcs12 -in filenavn.pfx/p12 -clcerts -nokeys -out cert.pem
```

Du kan afkode S/MIME-signaturer mv. [hér](https://lapo.it/asn1js/).

S/MIME-signering/kryptering er en Pro-funktion, men alle øvrige PGP- og S/MIME-operationer kan frit afbenyttes.

<br />

<a name="faq13"></a>
**(13) Hvordan fungerer søgning på en enhed/server?**

Du kan starte beskedsøgning efter Afsender (fra), Modtager (til, kopi, bcc), Emne, nøgleord eller beskedtekst vha. forstørrelsesglasset i en mappes handlingslbjælke. Du kan også søge fra enhver app ved at vælge *Søg e-mail * i popup-menuen kopiér/indsæt.

Søgning i den fælles indbakke udføres i alle mapper på alle konti, søgning i mappelisten udføres kun for den tilknyttede konto og søgning i en mappe udføres kun i dén mappe.

Beskeder søges først på enheden. Der vil være en handlingsknap med et søg igen-ikon i bunden for at fortsætte søgningen på serveren. Du kan vælge, i hvilken mappe, du vil fortsætte søgningen.

IMAP-protokollen understøtter ikke søgning i flere end én mappe ad gangen. Søgning på serveren er en ressourcekrævende operation, hvorfor flere mapper ikke kan vælges.

Deltekstsøgning i lokale beskeder er versal-/minuskel-ufølsom. Lokle beskedtekster gennemsøges kun, hvis selve beskedteksterne er blevet downloadet. Søgning på serveren kan være både versal-/minuskel-følsom eller -ufølsom og kan være på deltekst eller hele ord, afhængigt af udbyderen.

Visse servere kan ikke håndtere søgning i beskedtekster ved større beskedantal. For sådanne tilfælde findes en mulighed for at deaktivere søgning i beskedtekster.

Det er muligt at bruge Gmail-søgeoperatører vha. søgekommandopræfikset *raw:*. Er der kun opsat én Gmail-konto, kan en en rawsøgning startes direkte på serveren ved at søge i den fælles indbakke. Er flere Gmail-konti opsat, så gå først til mappelisten eller arkivmappen (alle beskeder) i den Gmail-konto, du vil gennemsøge. [Tjek hér](https://support.google.com/mail/answer/7190) vedr. de mulige søgeoperatører. F.eks.:

`
raw:larger:10M`

Gennemsøgning af et stort beskedantal på enheden sker ikke særligt hurtigt grundet to begrænsninger:

* [sqlite](https://www.sqlite.org/), Androids databasemotor har en poststørrelsesbegrænsning, der forhindrer, at beskedtekster gemmes i databasen
* Android-apps får kun begrænset hukommelse at arbejde med, selv hvis enheden har masser af hukommelse til rådighed

Søgning efter en beskedtekst kræver, at filer indeholdende beskedteksterne skal åbnes én efter én for at tjekke, om den søgte tekst optræder i filen, hvilket er en relativt ressourcekrævende proces.

Under *Diverse indstillinger* kan *Byg søgeindeks* aktiveres for markant at øge søgehastigheden på enheden, hvilket dog samtidig øger både strøm- og lagerpladsforbrug. Søgeindekset er baseret på ord, så søgning efter deltekster er ikke mulig. Søgning vha. søgeindekset er som standard OG (AND), så søgning efter f.eks. *æble appelsin* vil søge efter både æble OG appelsin. Ord adskilt med komma resulterer i en ELLER (OR) søgning, så f.eks. *æble, appelsin* vil søge efter enten æble ELLER appelsin. Begge kan kombineres, så søgning efter f.eks. *æble, appelsin banan* vil søge efter æble ELLER (appelsin OG banan). Brug af søgeindekset er en Pro-funktion.

Fra version 1.1315 er det muligt at benytte søgeudtryk såsom:

```
æble +banan-kirsebær?nødder
```

Dette vil resultere i en søgning som denne:

```
("æble" OG "banana" OG IKKE "kirsebær") ELLER "nødder"
```

Søgeudtryk kan benyttes til søgning på enheden via søgeindekset samt til søgning på e-mailserveren, men af ydelsesårsager ikke til søgning på enheden uden søgeindeks.

Søgning på enheden er en gratis funktion vha. søgeindekset, mens søgning på serveren er en Pro-funktion.

<br />

<a name="faq14"></a>
**(14) Hvordan opsættes en Outlook-/Live-/Hotmail-konto?**

En Outlook-/Live-/Hotmail-konto kan opsættes via hurtig opsætningsguiden ved at vælg *Outlook*.

Anvendelse af en Outlook-, Live- eller Hotmail-konto med tofaktorgodkendelse aktiveret kræver oprettelse af en app-adgangskode. Tjek informationen [hér](https://support.microsoft.com/en-us/help/12409/microsoft-account-app-passwords-two-step-verification).

Tjek Microsoft-instruktionerne [hér](https://support.office.com/en-us/article/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040).

Tjek [denne FAQ](#user-content-faq156) vedr. opsætnig af en Office365-konto.

<br />

<a name="faq15"></a>
**(15) Hvorfor genindlæses beskedteksten hele tiden?**

Beskedhovede og -brødtekst hentes separat fra serveren. Brødteksten i større beskeder forudindlæses ikke på takserede forbindelser, men hentes efter behov, når en besked udfoldes. Beskedteksten vil fortsætte med at blive genindlæst ved manglende forbindelse til kontoen. Tjek også næste spørgsmål, eller hvorvidt andre operationer, såsom beskedsynkronisering, afvikles.

Du kan tjekke konto- og mappelisten for kontoen og mappetilstanden (se forklaringen til betydningen af ikonerne) og operationslisten tilgængelig via hovednavigeringsmenuen til afventende operationer (tjek [denne FAQ](#user-content-faq3) for betydningen af operationerne).

If FairEmail is holding off because of prior connectivity issues, please see [this FAQ](#user-content-faq123), you can force synchronization via the three dots menu.

I modtagelsesindstillingerne kan den maksimale størrelse for automatisk download af beskeder på takserede forbindelser indstilles.

Mobilforbindelser er næsten altid takserede, og visse Wi-Fi hotspots (betalte) er det ligeledes.

<br />

<a name="faq16"></a>
**(16) Hvorfor synkroniseres beskeder ikke?**

Mulige årsager til fejl i beskedsynkronisering (sendte eller modtagne):

* Konto eller mappe(r) er ikke opsat til at synkronisere
* Der er indstillet for få synkroniseringsdage
* Ingen tilgængelig Internetforbindelse
* E-mailserveren er midlertidigt utilgængelig
* Android har stoppet synkroniseringstjenesten

Tjek derfor dine konto- og mappeindstillinger, og tjek, at konti/mapper er forbundet (se forklaringen i navigeringsmenuen ang. ikonernes betydning).

Er der evt. fejlmeddelelser, så tjek [denne FAQ](#user-content-faq22).

På visse enheder med mange apps, som slås om hukommelsen, kan Android 'som en sidste udvej muligvis stoppe synkroniseringstjenesten.

Visse Android-versioner stopper apps og tjenester for aggressivt. Tjek [dette dedikerede websted](https://dontkillmyapp.com/) samt [dette Android-problem](https://issuetracker.google.com/issues/122098785) for yderligere information.

Deaktivering af batterioptimering (opsætningstrin 4) reducerer chancen for, at Android stopper synkroniseringstjenesten.

<br />

<a name="faq17"></a>
**~~(17) Hvorfor fungerer manuel synkronisering ikke?~~**

~~Er menuen *Synkronisér nu* nedtonet, er der ingen forbindelse til kontoen.~~

~~Tjek foregående spørgsmål for yderligere information.~~

<br />

<a name="faq18"></a>
**(18) Hvorfor vises beskedforhåndsvisning ikke altid?**

Beskedforhåndsvisningen kan ikke vises, hvis beskedbrødteksten endnu ikke er downloadet. Tjek også [denne FAQ](#user-content-faq15).

<br />

<a name="faq19"></a>
**(19) Hvorfor er Pro-funktionerne så dyre?**

Det rigtige spørgsmål er, "*hvorfor der er så mange skatter og afgifter?*":

* Moms: 25 % (afhængigt af land)
* Google-gebyr: 30 %
* Inkomstskat: 50 %
* <sub>Paypal-gebyr: 5-10 % afhængigt af land/beløb</sub>

Så dét, der er tilbage til udvikleren, er blot en brøkdel af dét, du betaler.

Bemærk, at det kun er nødvendigt at købe visse bekvemmeligheds- og avancerede funktioner, hvilket betyder, at FairEmail grundlæggende er gratis at benytte.

Bemærk også, at de fleste gratis apps ikke ser ud til at være bæredygtige i længden, hvorimod FairEmail vedligeholdes og supporteres korrekt, samt at gratis apps kan have "røde flag", f.eks. læk af fortrolige oplysninger.

Jeg har arbejdet på FairEmail næsten dagligt i flere end to år, så jeg synes, at prisen er mere end rimelig. Af samme årsag vil der heller ikke være rabatter.

<br />

<a name="faq20"></a>
**(20) Kan jeg få refusion?**

Fungerer en købt Pro-funktion ikke som tilsigtet, og dette ikke skyldes et problem i de gratis funktioner, og jeg ikke kan løse problemet rettidigt, så kan du få refusion. I alle øvrigee tilfælde gives ikke refusion. Refusion er under ingen omstændigheder en mulighed for noget problem relateret til de gratis funktioner, da intet er betalt for disse, og da de kan evalueres uden begrænsninger. Jeg tager ansvar som sælger for at levere dét, der er blevet lovet, og jeg forventer, at du tager ansvar for at informere dig om, hvad det er, du køber.

<a name="faq21"></a>
**(21) Hvordan aktiveres notifikationslyset?**

Før Android 8 Oreo: Der findes en avanceret indstilling i opsætningen til dette.

Android 8 Oreo og senere: Tjek [hér](https://developer.android.com/training/notify-user/channels), hvordan du opsætter notifikationskanaler. Du kan benytte knappen *Håndtér notifikationer* i opsætningen for at gå direkte til Android-notifikationsindstillingerne. Bemærk, at fra og med Android 8 Oreo kan apps ikke kan ændre notifikationsindstillinger, herunder indstillinger for notifikationslys. Apps designet/målrettet ældre Android-versioner kan muligvis stadig styre indholdet af notifikationer, men sådanne apps kan ikke længere opdateres, og nyere Android-versioner viser en advarsel om, at sådanne apps er forældede.

Undertiden er det nødvendigt at deaktivere indstillingen *Vis beskedforhåndsvisning i notifikationer* eller at aktivere indstillingen *Vis kun notifikationer med en forhåndsvisningstekst* for at omgå en fejl i Android. Dette gælder muligvis også notifikationslyde samt vibrationer.

Indstilling af lysfarve før Android 8 understøttes ikke og er ikke muligt på Android 8 og senere.

<br />

<a name="faq22"></a>
**(22) Hvad betyder konto-/mappefejl ...?**

For lettere at diagnosticere problemer, skjuler FairEmail ikke fejl (som lignende apps ofte gør).

FairEmail forsøger automatisk at genoprette forbindelse efter en forsinkelse. Denne forsinkelse fordobles efter hvert mislykket forsøg for at forhindre batteridræning samt at blive låst ude permanent.

Der er generelle såvel som specifikke fejl for Gmail-konti (se nedenfor).

**Generelle fejl**

Fejlen *... Godkendelse mislykkedes ...* eller *... GODKENDELSE mislykkedes ...* skyldes sandsynligvis forkert brugernavn/adgangskode. Visse udbydere forventer som brugernavn blot *brugernavn* og andre den fulde e-mail *brugernavn@eksempel.dk*. Benyttes kopiér/indsæt til angivelse af brugernavn/adgangskode kan der muligvis medtages usynlige tegn, hvilket også kan forårsage denne fejl. Visse adgangskodehåndteringer er også kendt for at gøre dette forkert. Brugernavnet kan være minuskel/versal sensitivt, så prøv med kun minuskler. Adgangskoden er fortrinsvis minuskel/versal sensitiv. Visse udbydere kræver brug af en app-adgangskode i stedet for kontoadgangskoden, så tjek din udbyders dokumentation. Nogle gange er det nødvendigt først at muliggøre ekstern adgang (IMAP/SMTP) på udbyderens websted. Øvrige mulige årsager kan være, at kontoen er blokeret, eller at indlogning er administrativt begrænset på en eller anden måde, f.eks. ved kun at tillade indlogning fra bestemte netværk/IP-adresser.

Fejlen *... For mange fejlede godkendelsesforsøg ... *betyder sandsynligvis, at du bruger en Yahoo-kontoadgangskode i stedet for en app ditto. Tjek [denne FAQ](#user-content-faq88) vedr. opsætning af en Yahoo-konto.

Meddelelsen *... + OK ...* betyder sandsynligvis, at en POP3-port (normalt portnr. 995) anvendes til en IMAP-konto (normalt ellers portnr. 993).

Fejlene *... ugyldig hilsen ...*, *... kræver gyldig adresse ...* og *... Parameter til HELO overholder ikke RFC-syntaks ...* kan sandsynligvis løses ved at ændre den avancerede identitetsindstilling *Anvend lokal IP-adresse i stedet for værtsnavn*.

Fejlen *... Kunne ikke oprette forbindelse til vært...* betyder, at der ikke var noget svar fra e-mailserveren inden for en rimelig tid (20 sekunder som standard). Dette indikerer i reglen Internetforbindelsesproblemer, muligvis forårsaget af en VPN- eller firewall-app. Ved virkelig langsomme e-milservere kan du forsøge at øge timeout for forbindelsen i FairEmails forbindelsesindstillinger.

Fejlen *... Forbindelse nægtet ...* betyder, at e-mailserveren, eller noget mellem denne og appen såsom en firewall, aktivt afviste forbindelsen.

Fejlen *... Netværk kan ikke nås ...* betyder, at e-mailserveren ikke kunne nås via den aktuelle Internetforbindelse, f.eks. fordi Internettrafik er begrænset til alene lokal trafik.

Fejlen *... Vært er uopløst ...*, "*... Kan ikke opløse vært...* eller *... Ingen adresse tilknyttet værtsnavn ...* betyder, at adressen på e-mailserveren ikke kunne opløses til en IP-adresse. Dette kan skyldes et VPN, adblocking eller en utilgængelig/ikke korrekt fungerende (lokal) [DNS-server](https://en.wikipedia.org/wiki/Domain_Name_System).

Fejlen *... Softwareforårsaget forbindelsesafbrydelse ...* betyder, at e-mailserveren, eller noget mellem FairEmail og denne, aktivt afsluttede en eksisterende forbindelse. Dette kan f. eks. ske, når tilslutningen mistes pludseligt. Et typisk eksempel er aktivering af Flytilstand.

Fejlene *... BYE, logger ud ...*, *... Forbindelse nulstillet af peer ... * betyder, at e-mailserveren aktivt afsluttede en eksisterende forbindelse.

Fejlen *... Forbindelse lukket af peer ...* kan skyldes en ikke-opdateret Exchange-server, tjek [hér](https://blogs.technet.microsoft.com/pki/2010/09/30/sha2-and-windows/) for yderligere oplysninger.

Fejlene *... Læsefejl ...*, *... Skrivefejl ...*, *... Læsning fik timeout ...*, *... Broken pipe ...* betyder, at e-mailserveren ikke længere svarer, eller at Internetforbindelsen er ringe.

Fejlen *... Uventet afslutning af zlib-inputstrøm ...* betyder, at ikke alle data blev modtaget, muligvis grundet en dårlig/afbrudt forbindelse.

Fejlen *... forbindelsesfejl ...* kan indikere [For mange samtidige forbindelser](#user-content-faq23).

Advarslen *... Uunderstøttet kodning ...* betyder, at beskedens tegnsæt er ukendt eller uunderstøttet. FairEmail gør brug af ISO-8859-1 (Latin1), hvilket i de fleste tilfælde vil resultere i korrekte beskedvisninger.

[Tjek hér](#user-content-faq4) vedr. fejlene *... Ikke-betroet ... ikke i certifikat ...*, * ... Ugyldigt sikkerhedscertifikat (kan ikke bekræfte serveridentitet) ...* eller *... Tillidsanker til certificeringssti ikke fundet ...*

[Tjek hér](#user-content-faq127) vedr. fejlen *... Syntaktisk ugyldig(t/e) HELO-argument(er) ... *.

[Tjek hér](#user-content-faq41) vedr. fejlen *... Handshake mislykkedes ...*.

Tjek [hér](https://linux.die.net/man/3/connect) ang. betydningen af fejlkoder såsom EHOSTUNREACH og ETIMEDOUT.

Mulige årsager:

* Firewall eller router blokerer forbindelser til serveren
* Værtsnavnet eller portnummeret er ugyldigt
* Der er problemer med denne Internetforbindelse
* Der er problemer med at opløse domænenavne (Yandex: Prøv at deaktivere privat DNS i Android-indstillingerne)
* E-mailserveren nægeter at acceptere (eksterne) forbindelser
* E-mai-serveren nægter at acceptere en besked, fordi den f.eks. er for stor eller indeholder uacceptable links
* Der er for mange forbindelser til serveren, se også næste spørgsmål

Mange offentlige Wi-Fi netværk blokerer udgående e-mail for at forhindre spam. Dette kan somme tider løses ved brug af en anden SMTP-port. Se leverandørdokumentationen ang. anvendelige portnumre.

Benyttes et [VPN](https://en.wikipedia.org/wiki/Virtual_private_network), kan VPN-udbyderen muligvis blokere forbindelsen, hvis den for aggressivt forsøger at forhindre spam. Bemærk, at [Google Fi](https://fi.google.com/) også anvender et VPN.

**Afsendelsesfejl**

SMTP-servere kan [af forskellige årsager](https://en.wikipedia.org/wiki/List_of_SMTP_server_return_codes) afvise beskeder. For store beskeder og udløsning af en e-mailservers spamfilteret er de mest almindelige årsager.

* Gmails størrelsesbegrænsning for vedhæftninger [udgør 25 MB](https://support.google.com/mail/answer/6584)
* Outlooks og Office 365' størrelsesbegrænsning for vedhæftninger [udgør 20 MB](https://support.microsoft.com/en-us/help/2813269/attachment-size-exceeds-the-allowable-limit-error-when-you-add-a-large)
* Yahoos størrelsesbegrænsning for vedhæftninger [udgør 25 MB](https://help.yahoo.com/kb/SLN5673.html)
* For *554 5.7.1 Tjeneste utilgængelig; Klient vært xxx.xxx.xxx.xxx blokeret*, tjek venligst [hér](https://docs.gandi.net/en/gandimail/faq/error_types/554_5_7_1_service_unavailable.html)
* *501 Syntaksfejl - linje for lang* er ofte forårsaget af brug af en lang Autocrypt-header
* *503 5.5.0 Modtager allerede angivet* betyder typisk, at en adresse bruges som både TO- og CC-adresse
* *554 5.7.1 ... ikke tilladt at videresende* betyder, at e-mailserveren ikke genkender brugernavnet/e-mailadressen. Dobbelttjek værtsnavn og brugernavn/e-mailadresse i identitetsindstillingerne.
* *550 Spam besked afvist, da IP er listet af ...* betyder, at e-mailserveren har afvist at afsende en besked fra den aktuelle (offentlige) netværksadresse, fordi den tidligere har være misbrugt til spamafsendelse. Prøv at aktivere flytilstand i 10 minutter for at få tildelt en ny netværksadresse.
* *571 5.7.1 Besked indeholder spam eller virus eller afsender er blokeret ...* betyder, at e-mailserveren betragtede en udgående besked som spam. Dette betyder sandsynligvis, at e-mailserverens spamfiltre er for strikse. Kontakt e-mailudbyderen for support vedr. dette.

Ønsker du at bruge Gmail SMTP-serveren til at omgå et for strikst, udgående spamfilter eller til at forbedre beskedleveringen:

* Bekræft din e-mailadresse [hér](https://mail.google.com/mail/u/0/#settings/accounts) (en computerbrowser skal bruges til dette)
* Ændr identitetsindstillingerne på denne måde (opsætning, trin 2, tryk på Håndtéer, tryk på identitet):

&emsp;&emsp;Brugernavn: *Din Gmail-adresse*<br /> &emsp;&emsp;Adgangskode *[En app-adgangskode](#user-content-faq6)*<br /> &emsp;&emsp;Vært: *smtp. mail. om*<br /> &emsp;&emsp;Port: *465*<br /> &emsp;&emsp;Kryptering: *SSL/TLS*<br /> &emsp;&emsp;Svar til adresse: *Din e-mailadresse* (avancerede identitetsindstillinger)<br />

<br />

**Gmail-fejl**

Godkendelsen af Gmail-kontiopsætninger vha. hurtig guiden skal periodisk opfriskes via [Android-kontohåndteringen](https://developer.android.com/reference/android/accounts/AccountManager). Dette kræver kontakt-/konto-tilladelser samt Internetforbindelse.

Fejlen *... Godkendelse mislykkedes... Konto ikke fundet ...* betyder, at en tidligere godkendt Gmail-konto er blevet fjernet fra enheden.

Fejlene *... Godkendelse mislykkedes... Ingen token ...* betyder, at Android-kontohåndteringen ikke kunne opfriske godkendelsen af en Gmail-konto.

Fejlen *... Godkendelse mislykkedes ... netværksfejl ...* betyder, at Android-kontohåndteringen ikke var i stand til at opfriske godkendelsen af en Gmail-konto grundet Internetforbindelsesproblemer

Fejlen *... Godkendelse mislykkedes... Ugyldige akkreditiver ...* kan være forårsaget af et skift af kontoadgangskoden eller ophævelse af de krævede konto-/kontakttilladelser. Er kontoadgangskoden skiftet, så godkend Google-kontoen i indstillingerne for Android-kontoen igen. I tilfælde af ophævede tilladelser, kan du starte hurtig opsætningsguiden til Gmail for at gentildele de nødvendige rettigheder (du behøver ikke opsætte kontoen igen).

Fejlen *... ServiceDisabled ...* kan skyldes din tilmelding til [Avanceret Beskyttelsesprogram](https://landing.google.com/advancedprotection/): "*For at læse din e-mail, skal du benytte Gmail. Du kan ikke benytte din Google-konto med apps og tjenester, som kræver adgang til følsomme data såsom dine e-mails*", tjek mere info [hér](https://support.google.com/accounts/answer/7519408?hl=en&ref_topic=9264881).

Er du i tvivl, kan du anmode om [support](#user-content-support).

<br />

<a name="faq23"></a>
**(23) Hvorfor ses advarslen ... ?**

*Generelt*

Avarsler er advarselsmeddelelser sendt af e-mailservere.

*For mange samtidige forbindelser* eller *Maksimalt antal forbindelser overskred*

Denne advarsel udsendes, når der er for mange samtidige mappeforbindelser til den samme e-mailkonto.

Mulige årsager:

* Flere e-mailklienter er forbundet til den samme konto
* Samme e-mailklient er forbundet flere gange til den samme konto
* Tidligere forbindelser blev brat afsluttet, f.eks. ved en pludselig mistet Internetforbindelse

Prøv først at vente lidt for at se, om problemet løser sig selv, ellers:

* Skift enten til periodisk tjek for beskeder i modtagelsesindstillingerne, hvilket resulterer i, at mapper åbnes én ad gangen
* Eller indstil nogle mapper til polling i stedet for at synkronisation (langt tryk på mappen i mappelisten, redigér egenskaber)

En nem måde at opsætte periodisk tjek for beskeder for alle mapper undtagen indbakken er at bruge *Anvend for alle . .* i trepriksmenuen på mappelisten og at markere de to nederste afkrydsningsfelter.

Det maksimale antal samtidige mappeforbindelser til Gmail udgør 15, så der kan maks. synkronisere 15 mapper samtidigt på tværs af *alle* dine enheder. Af samme grund er *Gmail-brugermapper* som standard opsat til polling fremfor kontinuerlig synkronisering. Om nødvendigt eller ønsket kan dette ændres vha. langt tryk på en mappe i mappelisten og så vælge *Redigér egenskaber*. Tjek oplysningerne [hér](https://support.google.com/mail/answer/7126229).

Ved brug af en Dovecot-server, skal indstillingen [mail_max_userip_connections](https://doc.dovecot.org/settings/dovecot_core_settings/#mail-max-userip-connections) muligvis ændres.

Bemærk, at det vil tage e-mail-serveren et stykke tid at opdage brudte forbindelser, f.eks. grund ophør af netværksdækning, hvilket betyder, at der faktuelt kun er halvdelen af mappeforbindelserne tilgængelige. For Gmail vil dette f.eks. kun være 7 forbindelser.

<br />

<a name="faq24"></a>
**(24) Hvad betyder gennemse beskeder på serveren?**

At gennemse beskeder på serveren henter beskederne fra e-mailserveren i realtid, når du kommer til slutningen af listen over synkroniserede beskeder, også når mappen er indstillet til ikke at synkronisere. Du kan deaktivere denne funktion under Avancerede kontoindstillinger.

<br />

<a name="faq25"></a>
**(25) Hvorfor kan der ikke vælges/åbnes/gemmes et billede, vedhæftning eller fil?**

Når et menupunkt til at vælge/åbne/gemme en fil er deaktiveret (nedtonet), eller når du ser meddelelsen *Lageradgangs-framework ikke tilgængelig*, så er [lageradgangs-framework'en](https://developer.android.com/guide/topics/providers/document-provider), en standard Android-komponent, sandsynligvis ikke til stede. This might be because your custom ROM does not include it or because it was actively removed (debloated).

FairEmail anmoder ikke om lagerpladstilladelser, så denne framework kræves for at kunne vælge filer og mapper. Ingen app, undtagen måske filhåndteringer målrettet Android 4.4 KitKat eller senere, bør anmode om lagerpladstilladelser, da dette giver adgang til *alle* filer.

Framework for lagerpladsadgang leveres af pakken *com.android.documentsui*, der er synlig som *Fil*-app i visse Android-versioner (især OxygenOS).

Du kan aktivere framework'en for lagerpladsadgang (igen) med denne adb-kommando:

```
pm install -k --user 0 com.android.documentsui
```

Alternativt kan du muligvis aktivere *Fil*-appen igen vha. Android app-indstillingerne.

<br />

<a name="faq26"></a>
**(26) Kan jeg hjælpe med at oversætte FairEmail til mit sprog?**

Ja, du kan oversætte FairEmail-teksterne til dit sprog [via Crowdin](https://crowdin.com/project/open-source-email). Tilmelding er gratis.

Ønsker du dit navn/alias inkluderet på listen over bidragsydere i appens *Om*-afsnit, så [kontakt mig](https://contact.faircode.eu/?product=fairemailsupport).

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

I opsætningen findes knappen *Håndtér notifikationer *til direkte navigering til Android-notifikationsindstillingerne for FairEmail.

I Android 8.0 Oreo og senere kan du håndtere egenskaberne for de individuelle notifikationskanaler, f.eks. indstilling af en bestemt notifikationslyd eller notifikationsvisning på låseskærmen.

FairEmail har flg. notifikationskanaler:

* Tjeneste: Benytttes til notifikation om synkroniseringstjenesten, tjek også [denne FAQ](#user-content-faq2)
* Send: Benyttes til sendetjenestenotiifikation
* Notifikationer: Benyttes til notifikation om ny besked
* Advarsel: Benyttes til advarselsnotifikationer
* Fejl: Benyttes til notifikationer om fejl

Tjek [hér](https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels) for detaljer om notifikationskanaler. Kort sagt: Tryk på notifikationskanalnavnet for at tilgå dens indstillinger.

I Android før Android 8 Oreo kan du opsætte notifikationslyden i indstillingerne.

Tjek [denne FAQ](#user-content-faq21), hvis din enhed har et notifikationslys.

<br />

<a name="faq29"></a>
**(29) Hvordan fås notifikationer om nye beskeder for andre mapper?**

Benyt blot et langt tryk på en mappe, vælg *Redigér egenskaber*, og aktivér enten *Vis i den fælles indbakke* eller *Notificér om nye beskeder* (kun tilgængelig på Android 7 Nougat og senere) og tryk på *Gem*.

<br />

<a name="faq30"></a>
**(30) Hvordan kan de medfølgende hurtig indstillinger benyttes?**

Der er hurtig indstillinger (indstillingsfliser) tilgængelige til at:

* globalt aktivere/deaktivere synkronisering
* vise antallet af nye beskeder og markere dem som set (ikke læst)

Hurtig indstillinger kræver Android 7.0 Nougat eller senere. Brug af indstillingsfliserne forklares [hér](https://support.google.com/android/answer/9083864).

<br />

<a name="faq31"></a>
**(31) Hvordan kan de medfølgende genveje benyttes?**

Der er tilgængelige genveje til:

* at skrive en ny besked til en favoritkontakt
* at opsætte konti, identiteter mv.

Genveje kræver Android 7.1 Nougat eller senere. Brug af genveje forklares [hér](https://support.google.com/android/answer/2781850).

<br />

<a name="faq32"></a>
**(32) Hvordan tjekkes, om læsning af e-mails virkelig er sikker?**

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

Bemærk, at dette er uafhængigt af beskedmodtagelse.

<br />

<a name="faq34"></a>
**(34) Hvordan matches identiteter?**

Identiteter matches efter konto. For indgående beskeder tjekkes adresserne *til*, *cc*, *bcc*, *fra* og *(X-)leverede/konvolut/originale-til* (i nævnte rækkefølge), og for udgående beskeder (kladder, udbakke og sendt) tjekkes kun *fra*-adresserne. Matchende adresser har forrang frem for delvist matchende adresser, undtagen *leveret til*-adresser.

Den matchede adresse vises som *via* i adresseafsnittet på modtagne beskeder (mellem besked-header og beskedtekst).

Bemærk, at identiteter skal være aktiverede for at kunne matches, samt at identiteter for andre konti ikke overvejes.

Match udføres kun én gang efter modtagelse af en besked, så evt. opsætningsændringer ændrer ikke eksisterende beskeder. Du kan dog rydde lokale beskeder vha. langt tryk på en mappe i mappelisten og så synkronisere beskederne igen.

Det er muligt at opsætte en [regex](https://en.wikipedia.org/wiki/Regular_expression) i identitetsindstillingerne for at få en e-mailadresses matchende **brugernavn** (delen før @-tegnet).

Bemærk, at domænenavnet (alt efter @) altid skal være identisk med identitetens domænenavn.

Vil du gerne matche en fang-alle e-mailadresse, er denne regex for det meste OK:

```
.*
```

Vil du matche e-mailadresserne til specielle formål, abc@eksemepel.dk og xyx@eksemepel.dk og også gerne have en fallback e-mailadresse, hoved@eksemepel.dk, kan du gøre noget ala dette:

* Identity: abc@eksempel.dk; regex: **(?i)abc**
* Identity: xyz@eksempel.dk; regex: **(?i)xyz**
* Identity: hoved@eskempel.dk; regex: **^(?i)((?!abc|xyz).)\*$**

Matchede identiteter kan benyttes til beskedfarvekodning. Identitetsfarven har forrang over kontofarven. Brug af identitetsfarver er en Pro-funktion.

<br />

<a name="faq35"></a>
**(35) Hvorfor skal man være forsigtig med at få vist billeder, vedhæftninger, originalbeskeder samt åbne links?**

Visning af fjernlagrede billeder (tjek også [denne FAQ](#user-content-faq27)) samt åbning af links fortæller muligvis ikke kun afsenderen, at du har set beskeden, men lækker også din IP-adresse. Se også dette spørgsmål: [Hvorfor et e-mail link er farligere end et websøgningslink?](https://security.stackexchange.com/questions/241139/why-emails-link-is-more-dangerous-than-web-searchs-link).

Åbning af vedhæftninger eller visning af en original besked kan muligvis indlæse eksternt indhold og eksekvere scripts, hvilket ikke alene kan forårsage læk af fortrolige oplysninger, men tillige udgøre en sikkerhedsrisiko.

Bemærk, at dine kontakter, uden deres vidende, kan sende ondsindede beskeder, hvis de er blevet inficeret med malware.

FairEmail genformaterer beskeder, hvilket får dem til at se anderledes ud end originalen, men tillige afslører phishing-links.

Bemærk, at genformaterede beskeder ofte er mere læsbare end originalerne, da margerne er fjernet, og skrifttypefarver og -størrelser er standardiserede.

Gmail-appen viser som standard billeder ved at downloade disse via en Google-proxyserver. Da billederne downloades fra kildeserveren [i realtid](https://blog.filippo.io/how-the-new-gmail-image-proxy-works-and-what-this-means-for-you/), er dette endnu mindre sikkert, da Google også er involveret.

Du kan som standard få vist billeder og originale beskeder for betroede afsendere fra gang til gang ved at markere *Spørg ikke igen om...*.

Vil du nulstille *Åbn med* standard-apps, så tjek [hér](https://www.androidauthority.com/how-to-set-default-apps-android-clear-621269/).

<br />

<a name="faq36"></a>
**(36) Hvordan krypteres indstillingsfiler?**

Kort version: AES 256 bit

Lang version:

* 256 bit-nøglen genereres med *PBKDF2WithHmacSHA1* vha. en 128 bit sikker, tilfældigt salt og 65.536 iterationer
* Cipher'en er *AES/CBC/PKCS5Padding*

<br />

<a name="faq37"></a>
**(37) Hvordan lagres adgangskoderr?**

Alle understøttede Android-versioner [krypterer alle brugerdata](https://source.android.com/security/encryption), så alle data, inkl. brugernavne, adgangskoder, beskeder mv., lagres krypteret.

Er enheden sikret med en PIN-kode, mønster eller adgangskode, kan konto- og identitetsadgangskoder gøres synlige. Er dette er et problem, fordi enheden deles med andre, så overvej at anvende [brugerprofiler](https://www.howtogeek.com/333484/how-to-set-up-multiple-user-profiles-on-android/).

<br />

<a name="faq39"></a>
**(39) Hvordan kan FairEmails batteriforbrug reduceres?**

Som standard rapporterer nyere Android-versioner *app-brug* som en procentdel på Android-batteriindstillingssiden. **Forvirrende nok er *app-brug* ikke det samme som *batteriforbrug* og er ikke engang direkte relateret til batteriforbrug!** App-brugen (mens i brug) vil være meget høj, da FairEmail bruger en forgrundstjeneste, der af Android betragtes som konstant app-brug. Dette betyder dog ikke, at FairEmail konstant forbruger strøm. Det reelle stømforbrug kan ses ved at gå til denne skærm:

*Android-indstillinger*, *Batteri*, trepriksmenu *Batteriforbrug*, trepriksmenu *Vis fuld enhedsbrug*

Som en tommelfingerregel skal strømforbruget være under eller i hvert fald ikke meget højere end for *Mobilnetværksstandby*. Er dette ikke tilfældet, så aktivé *Automatisk optimering* i modtagelsesindstillingerne. Hjælper dette ikke, så [bed om support](https://contact.faircode.eu/?product=fairemailsupport).

Det er uundgåeligt, at beskedsynkronisering forbruger strøm, da det kræver adgang til både netværket og beskeddatabasen.

Sammenlignes batteriforbruget for FairEmail med en anden e-mailklient, skal den anden e-mailklient være opsat på lignende vis. Det er f.eks. ikke retvisende at sammenligne kontinuerlig synk (push-beskeder) og periodisk (ikke-regelmæssig) tjek for nye beskeder.

Gentilslutninger til en e-mailserver forbruger ekstra strøm, hvilket f.eks. en ustabil Internetforbindelse vil resultere i. Visse e-mailservere afslutter også ikke-aktive forbindelser for tidligt, selvom [standarden](https://tools.ietf.org/html/rfc2177) dikterer, at en ikke-aktiv forbindelse bør holdes åben i 29 minutter. I sådanne tilfælde vil du måske kun synkronisere periodisk, f.eks. hver time, i stedet for kontinuerligt. Bemærk, at hyppig polling (mere end hvert ca. 30.-60. min) antageligt vil forbruge mere strøm end kontinuerlig synkronisering, da serverforbindelsesoprettelsen samt lokal-/fjernbeskedsmatchning kræver ressourcer.

[På visse enheder](https://dontkillmyapp.com/) er det nødvendigt at *deaktivere* batterioptimering (opsætningstrin 4) for at holde forbindelser til e-mailservere åbne. Faktisk kan aktiv batterioptimering resultere i ekstra strømforbrug på alle enheder, selvom det lyder selvmodsigende!

Størstedelen af strømforbruget, fraset at kigge beskeder, skyldes synkronisering (modtagelse/afsendelse) af beskeder. For at reducere strømforbruget så indstil antallet af synkroniseringsdage til en lavere værdi, især hvis der er en masse nylige beskeder i en mappe. Langt tryk på et mappenavn i mappelisten, og valg af *Redigér egenskaber* giver adgang til denne indstilling.

Har du mindst én gang om dagen Internetforbindelse, er det tilstrækkeligt at synkronisere beskeder blot for én dag.

Bemærk, det antal dage, hvori beskeder *beholdes* kan indstilles til en større værdi end til det antal dage, hvori beskeder *synkroniseres*. Du kan f.eks. indledningsvis synkronisere beskeder i et større antal dage, og efter at dette er afsluttet reducer antallet af beskedsynkroniseringsdage, men bevare antallet af dage, hvori beskeder skal beholdes. Efter reducering af det antal dage, hvori beskeder beholdes, ønsker du måske at køre oprydning i diverse indstillinger for at fjerne gamle filer.

I modtagelsesindstillingerne kan du aktivere altid at synkronisere stjernemarkerede beskeder, hvilket lader dig beholde ældre beskeder, selvom du synkroniserer beskeder i et begrænset antal dage.

Deaktivering af mappeindstillingen *Download automatisk beskedtekster og vedhæftninger* vil betyde mindre netværkstrafik og dermed lavere strømforbrug. Du kan deaktivere denne indstilling for eksempelvis Sendt-mappen og arkivet.

Synkronisering af beskeder om natten er for det meste unødvendigt, så du kan spare strøm ved ikke at gøre dette. Der kan i indstillingerne vælges en tidsplan for beskedsynkronisering (dette er en Pro-funktion).

FairEmail vil som standard synkronisere mappelisten ved hver tilslutning. Da mapper typisk ikke oprettes/omdøbes/slettes særligt hyppigt, kan der spares netværks- og strømforbrug ved at deaktivere dette i modtagelsesindstillingerne.

Som standard vil FairEmail ved hver tilslutning tjekke, om gamle beskeder er blevet slettet fra serveren. Har du ikke noget imod, at gamle beskederr, som er blevet slettet fra serveren stadig er synlige i FairEmail, kan du spare netværks- og strømforbrug ved at deaktivere dette i modtagelsesindstillingerne.

Visse udbydere følger ikke IMAP-standarden og holder ikke forbindelserne åbne længe nok. Dette tvinger FairEmail til ofte at oprette forbindelse igen, hvilket medfører ekstra strømforbrug. Du kan tjekke *Loggen* via hovednavigeringsmenuen for at se, om der er hyppige forbindelsesgenoprettelser (forbindelse lukket/nulstillet, læse-/skrivefejl/timeout mv.). Du kan omgå dette ved i de avancerede kontoindstillinger at sænke keep-alive intervallet til f.eks. 9 eller 15 min. Bemærk, at batterioptimering skal deaktiveres i opsætningstrin 4 for pålideligt at holde forbindelserne i live.

Some providers send every two minutes something like '*Still here*' resulting in network traffic and your device to wake up and causing unnecessary extra battery usage. Du kan tjekke *Loggen* via hovednavigeringsmenuen for at se, hvorvidt din udbyder gør dette. Benytter din udbyder [Dovecot](https://www.dovecot.org/) som IMAP-server, kan du bede udbyderen ændre indstillingen [imap_idle_notify_interval](https://wiki.dovecot.org/Timeouts) til en højere værdi eller, endnu bedre, deaktivere dette. Er din udbyder ikke er i stand eller villig til at ændre/deaktivere dette, så overvej at skifte til periodisk i stedet for kontinuerlig synkronisering. Du kan ændre dette i modtagelsesindstillingerne.

Ses meddelelsen *Denne udbyder understøtter ikke push-beskeder* under opsætningen af en konto, så overvej mhp. at reducere strømforbruget at skifte til en moderne udbyder, der understøtter push-beskeder (IMAP IDLE).

Har din enhed en [AMOLED](https://en.wikipedia.org/wiki/AMOLED)-skærm, kan du ved at skifte til det sorte tema spare strøm, mens du ser beskeder.

Er autooptimering i modtagelsesindstillingerne aktiveret, omskiftes en konto til periodisk at tjekke for nye beskeder, når e-mailserveren:

* Says '*Still here*' within 3 minutes
* E-mailserveren understøtter ikke push-beskeder
* Keep-alive intervallet er kortere end 12 minutes

Desuden vil Papirkurv- og Spam-mapperne automatisk blive indstillet til at tjekke for nye beskeder efter tre successive [for mange samtidige forbindelser](#user-content-faq23)-fejl.

<br />

<a name="faq40"></a>
**(40) Hvordan kan FairEmails dataforbrug reduceres?**

Man kan grundlæggende reducere netværksforbruget på samme måde som strømforbruget. Tjek forslagene i foregående spørgsmål.

Det er uundgåeligt, at data vil blive forbrugt ved meddelelsessynkronisering.

Mistes forbindelsen til e-mail-serveren, vil FairEmail altid gensynkronisere meddelelserne for at sikre, at de alle er tilgængelige. Er forbindelsen ustabil, kan dette resultere i ekstra dataforbrug. I så tilfælde er det en god idé at reducere antallet af beskedsynkroniseringsdage til et minimum (tjek foregående spørgsmål) eller at skifte til periodisk beskedsynkronisering (modtagelsesindstillinger).

For at reducere dataforbruget, kan disse avancerede modtagelsesindstillinger ændres:

* Tjek, om gamle beskeder er fjernet fra serveren
* Synkronisér (delte) mappelister: Deaktivér

Som standard henter FairEmail ikke beskedtekster og vedhæftninger større end 256 KiB på takserede Internetforbindelser (mobildata eller betalt Wi-Fi). Dette kan ændres i forbindelsesindstillingerne.

<br />

<a name="faq41"></a>
**(41) Hvordan rettes fejlen 'Handshake mislykkedes'?**

Der er flere mulige årsager, så tjek slutningen af dette svar.

Fejlen '*Handshake mislykkedes ... WRONG_VERSION_NUMBER ...*' kan betyde, at du forsøger at oprette forbindelse til en IMAP- eller SMTP-server uden en krypteret forbindelse, typisk via port 143 (IMAP) og port 25 (SMTP), eller at der anvendes en forkert protokol (SSL/TLS eller STARTTLS).

De fleste udbydere leverer krypterede forbindelser vha. forskellige porte, typisk port 993 (IMAP) og port 465/587 (SMTP).

Understøtter din udbyder ikke krypterede forbindelser, bør du anmode om, at dette muliggøres. Er dette ikke en mulighed, kan du aktivere *Tillad usikre forbindelser* i både de avancerede indstillinger OG konto-/identitetsindstillingerne.

Tjek også [denne FAQ](#user-content-faq4).

Fejlen '*Handshake mislykkedes ... SSLV3_ALERT_ILLEGAL_PARAMETER ...*' er enten forårsaget af en fejl i implementeringen af SSL-protokollen eller ved en for kort DH-nøgle på e-mailserveren og kan desværre ikke rettes af FairEmail.

Fejlen '*Handshake mislykkedes ... HANDSHAKE_FAILURE_ON_CLIENT_HELLO ...*' kan være forårsaget af, at udbyderen stadig anvender RC4, hvilket siden [Android 7](https://developer.android.com/about/versions/nougat/android-7.0-changes.html#tls-ssl) ikke længere understøttes.

Fejlen '*Handshake mislykkedes ... UNSUPPORTED_PROTOCOL eller TLSV1_ALERT_PROTOCOL_VERSION ...*' kan forårsages af aktivering af forbindelseshærdning i forbindelsesindstillingerne eller af, at Android ikke længere understøtter ældre protokoller såsom SSLv3.

Android 8 Oreo og senere [understøtter ikke længere](https://developer.android.com/about/versions/oreo/android-8.0-changes#security-all) SSLv3. Der er ingen måde at løse manglende RC4- og SSLv3-understøttelse på, da disse er helt fjernet fra Android.

Du kan benytte [dette websted](https://ssl-tools.net/mailservers) eller [dette websted](https://www.immuniweb.com/ssl/) til at tjekke for SSL-/TLS-problemer på e-mailservere.

<br />

<a name="faq42"></a>
**(42) Kan der blive tilføjet en ny udbyder på udbyderlisten?**

Anvendes udbyderen af flere end blot et par personer, ja gerne.

Flg. oplysninger vil være påkrævet:

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

EFF-[skrivere](https://www.eff.org/nl/deeplinks/2018/06/announcing-starttls-everywhere-securing-hop-hop-email-delivery): "*Der er dog stadig ingen garanti for, at din kommunikation bliver krypteret, selvom du opsætter STARTTLS korrekt samt anvender et gyldigt certifikat.*"

Så rene SSL-forbindelser er sikrere end at anvende [STARTTLS](https://en.wikipedia.org/wiki/Opportunistic_TLS) og derfor foretrukne.

Sørg for at tjekke, at beskedmodtagelse/-afsendelse fungerer korrekt, før du kontakter mig vedr. udbydertilføjelse.

Tjek nedenfor, hvordan du kontakter mig.

<br />

<a name="faq43"></a>
**(43) Kan man vise den originale ... ?**

Vis original, viser den originale besked, således som afsenderen har sendt den, inkl. originale skrifttyper, farver, margner mv. FairEmail ændrer på ingen måde på dette, bortset fra at anmode om [TEXT_AUTOSIZING](https://developer.android.com/reference/android/webkit/WebSettings.LayoutAlgorithm), som *forsøger* at gøre en lille tekst mere læsbar.

<br />

<a name="faq44"></a>
**~~(44) Kan man få vist kontaktfotos/-identikoner i Sendt-mappen?~~**

~~Kontaktfotos og identikoner vises altid for afsenderen, da dette er nødvendigt for konversationstråde.~~ ~~At få kontaktfotos til både afsender og modtager er ikke rigtig en mulighed, da hentning af et kontaktfoto er ressourcekrævende.~~

<br />

<a name="faq45"></a>
**(45) Hvordan retter man problemet 'Denne nøgle er utilgængelig. For at bruge den, skal du importere den som en af dine egne!' ?**

Du får beskeden *Denne nøgle er ikke tilgængelig. For at bruge den, skal du importere den som en af dine egne!*, når du forsøger at dekryptere en besked med en offentlig nøgle. For at løse dette, skal den private nøgle importeres.

<br />

<a name="faq46"></a>
**(46) Hvorfor genindlæses beskedlisten hele tiden?**

Ses en 'spinner' øverst på beskedlisten, synkroniseres mappen stadig med fjernserveren. Status for synkroniseringen kan ses i mappelisten. Tjek forklaringen på, hvad ikoner og tal betyder.

Hastigheden på enhed og Internetforbindelse samt antal beskedsynkroniseringsdage bestemmer, hvor længe synkronisering vil tage. Bemærk, at antal beskedsynkroniseringsdage i de fleste tilfælde ikke bør opsættes til mere end én dag. Tjek også [denne FAQ](#user-content-faq39).

<br />

<a name="faq47"></a>
**(47) Hvordan løses fejlen 'Ingen primær konto eller udkastmappe'?**

Fejlmeddelelsen *Ingen primær konto eller udkastmappe* vises, når en besked forsøges oprettet, uden at nogen primær konto er angivet eller en udkastmappe er valgt for den primære konto. Dette kan f.eks. ske, når FairEmail startes fra en anden app for at skrive en besked. FairEmail skal vide, hvor udkast skal gemmes, så det er nødvendigt at vælge en konto til at udgøre den primære konto samt/eller vælge en udkastmappe for denne.

Dette kan også ske, når man forsøger at svare på en besked, eller videresende en besked fra en konto uden udkastmappe, mens der ikke er nogen primær konto, eller når den primære konto ikke har en udkastmappe.

Tjek [denne FAQ](#user-content-faq141) for yderligere information.

<br />

<a name="faq48"></a>
**~~(48) Hvordan løses fejlen 'Ingen primær konto/arkivmappe'?~~**

~~Fejlmeddelelsen *Ingen primær konto eller ingen arkivmappe* ses, når der søges efter beskeder fra en anden app. FairEmail skal vide, hvor der skal søges, så det er nødvendigt at vælge en konto til at udgøre den primære konto samt/eller vælge en arkivmappe for denne.~~

<br />

<a name="faq49"></a>
**(49) Hvordan løses fejlen 'En forældet app har sendt en filsti i stedet for en fil-stream'?**

Der er sandsynligvis valgt eller afsendt en vedhæftning/billede med en forældet filhåndtering eller en forældet app, der antager, at alle apps stadig har lagerpladstilladelser. Af sikkerheds- og fortrolighedsårsager har moderne apps såsom FairEmail ikke længere fuld adgang til alle filer. Dette kan resultere i fejlmeddelelsen *En forældet app sendte en filsti i stedet for en filstream*, hvis et filnavn i stedet for en filstrøm deles med FairEmail, da denne ikke kan åbne tilfældige filer.

Dette kan løses ved at skifte til en opdateret filhåndtering eller en app designet til de seneste Android-versioner. Alternativt kan FairEmail tildeles læserettighed til enhedens lagerplads i Android app-indstillingerne. Bemærk, at denne løsning [ikke længere fungerer på Android Q](https://developer.android.com/preview/privacy/scoped-storage).

Tjek også [spørgsmål 25](#user-content-faq25) og [hvad Google skriver om det](https://developer.android.com/training/secure-file-sharing/share-file#RespondToRequest).

<br />

<a name="faq50"></a>
**(50) Kan der blive tilføjet en mulighed for at synkronisere alle beskeder?**

En synkronisér alle (download alle) beskeder mulighed vil ikke blive tilføjet, da det nemt kan resultere i hukommelse opbrugt-fejl og den tilgængelige lagerplads opbruges. Det kan også nemt resultere i en masse batteri og dataforbrug. Mobilenheder er ikke særligt velegnede til download og lagring af beskeder for flere år. Funktionen søgningen på server vil være bedre (tjek [spørgsmål 13](#user-content-faq13)), da den er hurtigere og mere effektiv. Bemærk, at gennemsøgning af en masse lokalt lagrede beskeder kun ville forsinke søgning og forbruge ekstra strøm.

<br />

<a name="faq51"></a>
**(51) Hvordan sorteres mapper?**

Mapper sorteres først på efter konto (som standard på kontonavn) og indenfor en konto med specielle, systemmapper øverst, efterfulgt af mapper opsat til synkronisering. Inden for hver kategori sorteres mapperne efter (vist) navn. Om nødvendigt eller ønsket, kan dette ændres vha. langt tryk på en mappe i mappelisten og vælge *Redigér egenskaber*.

Navigation (hamburger-)menupunktet *Sortér mapper* i opsætningen kan bruges til manuelt at sortere mapperne.

<br />

<a name="faq52"></a>
**(52) Hvorfor tager det noget tid at genoprette forbindelsen til en konto?**

Der er ingen pålidelig måde at få oplyst, om en kontoforbindelse blev afsluttet tilsigtet eller utilsigtet. Forsøg på at genoprette forbindelse til en konto, hvor kontoforbindelsen for ofte blev afsluttet abrupt, kan resultere i problemer såsom [for mange samtidige forbindelser](#user-content-faq23) eller endda blokering af kontoen. For at forhindre sådanne problemer, afventer FairEmail 90 sek., inden forbendelsen igen forsøges genoprettet.

Benyt et langt tryk på *Indstillinger* i navigeringsmenuen for straks at genoprette forbindelsen.

<br />

<a name="faq53"></a>
**(53) Kan man fastgøre beskedhandlingsbjælken øverst/nederst?**

Brevhandlingshandlingsbjælken virker på en enkelt besked, og den nederste handlingsbjælke virker på alle beskeder i en konversation. Da der ofte er flere end én besked i en samtale, så er dette ikke muligt. Desuden findes en del beskedspecifikke handlinger, f.eks. videresendelse.

Flytning af beskedhandlingbjælken til bunden af beskeden er visuelt ikke tiltalende, dai der allerede er en konversationshandlingsbjælke nederst på skærmen.

Bemærk, at der ikke er mange (om nogen) e-mail apps, som viser en konversation som en liste over udvidelige beskeder. Det har mange fordele, men det er også årsagen til behovet for beskedspecifikke handlinger.

<br />

<a name="faq54"></a>
**~~~(54) Hvordan benyttes et navneområdepræfiks? ~~**

~~Et navneområdepræfix benyttes til automatisk at fjerne det præfiks, udbydere undertiden tilføjer mappenavne.~~

~~F.eks. betegnes Gmail Spam-mappen:~~

```
[Gmail]/Spam
```

~~Ved at sætte mavneområdepræfikset til *[Gmail]*, vil FairEmail automatisk fjerne *[Gmail]/* fra alle mappenavne.~~

<br />

<a name="faq55"></a>
**(55) Hvordan kan man markere alle beskeder som læst/flyttet eller slette dem alle?**

Man kan bruge flere valg til dette. Tryk og hold fingeren på den første besked og træk ned til den sidste besked. Brug derefter treprikshandlingsknappen til at udføre den ønskede handling.

<br />

<a name="faq56"></a>
**(56) Kan der blive tilføjet understøttelse af JMAP?**

Der er næsten ingen udbydere, som tilbyder [JMAP](https://jmap.io/)-protokollen, så det er ikke umagen værd at tilføje understøttelse for denne i FairEmail.

<br />

<a name="faq57"></a>
**(57) Kan HTML benyttes i signaturer?**

Ja, [HTML](https://en.wikipedia.org/wiki/HTML) kan benyttes. I signaturredigeringsværktøjet kan man via trepriksmenuen skifte til HTML-tilstand.

Bemærk, at skiftes tilbage til teksredigeringsværktøjet, gengives måske ikke alt HTML korrekt, da Android-teksredigeringsværktøjet ikke kan gengive alt HTML. Omvendt, benyttes tekstredigerinsværktøjet, kan HTML blive ændret på uventede måder.

Ønsker man at bruge præformateret tekst, såsom [ASCII art](https://en.wikipedia.org/wiki/ASCII_art), bør teksten ombryde i et *præelement* således:

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

Beskedbrødtekster og vedhæftninger downloades ikke som standard.

![Eksternt billede](https://github.com/M66B/FairEmail/blob/master/images/baseline_email_black_48dp.png)

Beskedbrødtekster og vedhæftninger downloades som standard.

<br />

<a name="faq59"></a>
**(59) Kan originale beskeder åbnes i browseren?**

Af sikkerhedsårsager er filerne med de originale beskedtekster ikke tilgængelige for andre apps, så dette er ikke muligt. I teorien kunne [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) bruges til at dele disse filer, men selv Googles Chrome kan ikke håndtere dette.

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
* Vidste du, at et langt tryk på send-handlingen vil vise Send-dialogen, selv hvis den er deaktiveret?
* Vidste du, at man med et lang tryk på fuldskærmsikonet kan få vist originalbeskedteksten?

<br />

<a name="faq61"></a>
**(61) Hvorfor vises nogle beskeder nedtonet?**

Nedtonede (grå) beskeder er lokalt flyttede beskeder, hvis flytning endnu ikke er bekræftet af serveren. Dette kan ske ved manglende forbindelse til serveren eller kontoen. Disse beskeder synkroniseres, når der er etableret server- og kontoforbindelse eller, hvis dette aldrig sker, slettes, hvis de er for gamle til at blive synkroniseret.

Du skal muligvis synkronisere mappen manuelt, f.eks. ved at trække ned.

Du kan se disse beskeder, men du kan ikke flytte dem igen, før den foregående flytning er blevet bekræftet.

Afventende [operationer](#user-content-faq3) vises i operationsvisningen tilgængelig via hovednavigeringsmenuen.

<br />

<a name="faq62"></a>
**(62) Hvilke godkendelsesmetoder understøttes?**

Flg. godkendelsesmetoder understøttes og anvendes i denne rækkefølge:

* LOGIN
* PLAIN
* CRAM-MD5
* XOAUTH2 ([Gmail](https://developers.google.com/gmail/imap/xoauth2-protocol), [Yandex](https://tech.yandex.com/oauth/))
* NTLM (ikke-testet)

Da [JavaMail til Android](https://javaee.github.io/javamail/Android) ikke understøtter SASL-godkendelsesmetoder, er disse, udover CRAM-MD4, uunderstøttede.

Kræver udbyderen en uunderstøttet godkendelsesmetode, vil fejlmeddelelsen *godkendelse mislykkedes* sandsynligvis ses.

[Server Name Indication](https://en.wikipedia.org/wiki/Server_Name_Indication) understøttes af [alle understøttede Android-versioner](https://developer.android.com/training/articles/security-ssl).

<br />

<a name="faq63"></a>
**(63) Hvordan skaleres billeder til skærmvisning?**

Store indlejrede eller vedhæftede [PNG-](https://en.wikipedia.org/wiki/Portable_Network_Graphics) og [JPEG-](https://en.wikipedia.org/wiki/JPEG)billeder skaleres automatisk mhp. skærmvisning. Dette skyldes, at e-mails er begrænset i størrelse til, afhængigt af udbyderen, fortrinsvis mellem 10 og 50 MB. Billeder skaleres som standard til en maksimal bredde og højde på ca. 1440 pixels og gemmes med et komprimeringsforhold på 90%. Billeder nedskaleres vha. heltalsfaktorer for at reducere hukommelsesforbruget og bevare billedkvalitet. Automatisk ændring af størrelse af indlejrede og/eller vedhæftede billeder og den maksimale målbilledstørrelse kan opsættes i Send-indstillinger.

Ønsker man at ændre størrelsen på billeder fra gang til gang, kan [Send Reduced](https://f-droid.org/en/packages/mobi.omegacentauri.SendReduced/), eller en lignende app, benyttes.

<br />

<a name="faq64"></a>
**~~~(64) Kan der blive tilføjet tilpassede handlinger for venstre/højre stryg?~~**

~~Den mest logiske konsekvens ifm. at stryge en listepost til venstre/højre er at fjerne denne fra listen.~~ ~~Den mest logiske handling ifm. en e-mail-app er at flytte beskeden til en anden mappe.~~ ~~Du kan vælge mappen, der skal flyttes til, via kontoindstillingerne.~~

~~Andre handlinger, såsom markering af beskeder som læste og slumre beskeder, er tilgængelige via multivalg.~~ ~~~Lang tryk på en besked giver adgang til multivalg. Tjek også [dette spørgsmål](#user-content-faq55).~~

~~Strygning til venstre/højre for at markere en besked som læst/ulæst er ulogisk, da beskeden først tildeles én status for senere at få en anden. ~ ~~Bemærk, at der er en avanceret mulighed for automatisk at markere beskeder læste ved flytning,~~ ~~ som for det meste er en perfekt erstatning for sekvensen markér som læst og flyt til anden mappe.~~ ~~Man kan også markere beskeder læst via Nye beskeder-notifikationen.~~

~~Vil man læse en besked senere, kan denne skjules indtil en bestemt tidspunkt ved brug af menuen *slumre*.~~

<br />

<a name="faq65"></a>
**(65) Hvorfor vises nogle beskeder nedtonet?**

Indlejrede (billede) og vedhæftede filer vises nedtonet. [Indlejrede, vedhæftede filer](https://tools.ietf.org/html/rfc2183) er begrenet på automatisk download og visning. men da FairEmail ikke altid downloader vedhæftede filer automatisk (tjek også [denne FAQ](#user-content-faq40)), vil alle vedhæftningstyper blive vist. For at differentiere indlejringer og regulære vedhæftninger, vises indlejrede filer nedtonet.

<br />

<a name="faq66"></a>
**(66) Er FairEmail tilgængelig i Google Play Familie-biblioteket?**

Prisen på de få Pro-funktioner er for lav, lavere end prisen på de fleste lignende apps, og der er [for mange gebyrer og skatter](#user-content-faq19), til at begrunde en tilgængeliggørelse af FairEmail i [Google Play Familie-biblioteket](https://support.google.com/googleone/answer/7007852). Bemærk, at Google promoverer Familie-biblioteket, men samtidig lader udviklere betale for det.

<br />

<a name="faq67"></a>
**(67) Hvordan slumres konversationer?**

Vælg via multivalg én af flere konversationer (langt tryk for at tilgå multivalg), tryk på trepriksknappen og vælg *Slumre ...*. Alternativt kan *Slumre...* benyttes via den udvidede beskedvisning i trepriksmenuenpunktet 'mere' eller tidsforskydningshandlingen i den nederste handlingsbjælke. Vælg konversationsslumretid(er) og bekræft med et tryk på OK. Konversationerne skjules i det valgte tidsrum og vises derefter igen. En Ny besked-notifikation som påmindelse.

Det er også muligt at slumre beskeder vha, [en regel](#user-content-faq71).

Slumrede beskeder kan vises ved at afmarkerere *Bortfiltrér* > *Skjult* i trepriksoverløbsmenuen.

Man kan trykke på det lille slumreikon for at se, hvor længe en konversation slumrer.

Ved at angive nul som slumrevarighed, annulleres slumringen.

<br />

<a name="faq68"></a>
**~~(68) Hvorfor kan Adobe Acrobat Reader/Microsoft-apps ikke åbne PDF-vedhæftninger/dokumentvedhæftninger?~~**

~~Adobe Acrobat Reader og Microsoft apps forventer stadig fuld adgang til alle lagrede filer,~~ ~~selvom apps skal bruge [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) siden Android KitKat (2013)~~ ~~~for at tilgå aktivt delte filer. Dette er af fortroligheds- og sikkerhedsårsager.~~

~~Dette kan løse ved at gemme vedhæftningen og åbne den fra Adobe Acrobat Reader/Microsoft-appen,~~ ~~, men det anbefales at installere en opdateret og helst open-source PDF-læser/dokumentfremviser,~~ ~~f.eks. en af de [hér](https://github.com/offa/android-foss#-document--pdf-viewer) anførte.~~

<br />

<a name="faq69"></a>
**(69) Kan der blive tilføjet auto-rul op ved ny besked?**

Beskedlisten ruller automatisk op, når der navigeres fra en ny beskednotifikation eller efter manuel opfriskning. Altid automatisk rul op ved modtagelse af nye beskeder ville interferere ens egen rulning, men om ønsket, kan dette aktivere i indstillingerne.

<br />

<a name="faq70"></a>
**(70) Hvornår auto-ekspanderes beskeder?**

Ved navigering til en konversation, auto-ekspanderes én besked, hvis:

* Der er kun én besked i konversationen
* Der kun er én ulæst besked i konversationen

Der er én undtagelse: Beskeden er endnu ikke downloadet, og beskeden er for stor til at auto-downloade på en takseret (mobildata-) forbindelse. Maks. beskedstørrelse kan opsættes/deaktiveres på fanen 'Forbindelsesindstillinger'.

Dublet (arkiveret), kasserede og udkastbeskeder indregnes ikke.

Ved ekspandering auto-markeres beskeder som læste, medmindre dette er deaktiveret i de individuelle kontoindstillinger.

<br />

<a name="faq71"></a>
**(71) Hvordan benyttes filterregler?**

Man kan redigere filterregler via et langt tryk på en mappe i kontomappelisten (tryk på kontonavnet i navigering/sidemenuen).

Nye regler effektueres for nye, modtagne beskeder i mappen, ikke for eksisterende beskeder. Man kan tjekke reglen og anvende den på eksisterende beskeder eller, alternativt, via et langt tryk på regellisten vælge *Eksekvér nu*.

En regel skal gives et navn, og rækkefølgen skal defineres, for hvilken en regel skal eksekveres relativt til andre regler.

En regel kan deaktiveres og udførsel af andre regler kan stoppes efter en regel er blevet eksekveret.

Flg. regelbetingelser er tilgængelige:

* Afsender indeholder
* Modtager indeholder
* Emne indeholder
* Har vedhæftninger
* Header indeholder
* Dag/tidspunkt mellem

Alle regelbetingelser skal være imødekommet for udførelse af regelhandlingen. Alle betingelser er valgfrie, men der skal være mindst én betingelse for at undgå matchning af alle beskeder. Ønsker man at matche alle afsendere eller modtagere, kan @-tegnet blot anvendes som betingelse, idet alle e-mailadresser indeholder dette tegn.

Bemærk, at e-mailadresser er formateret således:

`
"Nogen" <somebody@example.org>`

Man kan anvende flere regler, muligvis med en *stop behandling*, for en *eller* eller en *ikke* betingelse.

Matchning er ikke versal-/minuskelfølsom, medmindre [regulære udtryk](https://en.wikipedia.org/wiki/Regular_expression) benyttes. Tjek [hér](https://developer.android.com/reference/java/util/regex/Pattern) for dokumentation af regulære Java-udtryk. En regex kan aftestes [hér](https://regexr.com/).

Bemærk at et regulært udtryk understøtter en *eller*-operatør, for matchning af flere afsendere om ønsket:

`
.*alice@example\.org.*|.*bob@example\.org.*|.*carol@example\.org.*`

Bemærk, at [dot all-tilstand](https://developer.android.com/reference/java/util/regex/Pattern#DOTALL) er aktiveret for at kunne matche [udfoldede headers](https://tools.ietf.org/html/rfc2822#section-3.2.3).

En af disse handlinger kan vælges anvend på matchende beskeder:

* Ingen handling (nyttigt for *ikke*)
* Markér som læst
* Markér som ulæst
* Skjul
* Undertryk notifikationer
* Slumre
* Tilføj stjerne
* Angiv vigtighed (lokal prioritet)
* Tilføj stikord
* Flyt
* Kopiér (Gmail: Etiket)
* Besvar/videresend (med skabelon)
* Tekst-til-tale (afsender og emne)
* Automatisering (Tasker mv.)

Regler effektueres umiddelbart efter besked-header'en er hentet, men inden beskedteksten er blevet downloadet, så brug af betingelser ift. beskedteksten er ikke muligt. Bemærk, at store beskedtekster downloades på forlangende via takserede forbindelser mhp. at reducere dataforbruget.

Ønskes en besked videresendt, så overvej i stedet at bruge flythandlingen. Dette vil også være mere pålideligt end videresendelse, da videresendte beskeder måske betragtes som spam.

Da besked-headers som standard ikke downloades og lagres for at spare strøm- og dataforbrug samt lagerplads, er det ikke muligt at få vist, hvilke beskeder, som evt. matcher en header-regelbetingelse.

Nogle almindelige header-betingelser (regex):

* *.&ast;Auto-Submitted:.&ast;* [RFC3834](https://tools.ietf.org/html/rfc3834)
* *.&ast;Content Type: multipart/report.&ast;* [RFC3462](https://tools.ietf.org/html/rfc3462)

Under *mere* i trepriksbeskedmenuen findes et element til oprettelse af en regel for en modtaget besked med de mest almindelige betingelser udfyldt.

POP3- protokollen understøtter ikke indstilling af nøgleord og beskedflytning/-kopiering.

Brug af regler er en Pro-funktion.

<br />

<a name="faq72"></a>
**(72) Hvad er primære konti/identiteter?**

Den primære konto bruges, når kontoen er tvetydig, f.eks. ved start på et nyt udkast fra den fælles indbakke.

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
Ny opgave: En genkendelig
Handlingskategori: Div./Send-
hensigtshandling: eu.faircode.email.ENABLE
Mål: Tjeneste
```

To enable/disable an account with the name *Gmail*:

```
Ekstra: konto:Gmail
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

The preferred way to set up a Yahoo account is by using the quick setup wizard, which will use OAuth instead of a password and is therefore safer (and easier as well).

To authorize a Yahoo, AOL, or Sky account you will need to create an app password. For instructions, please see here:

* [for Yahoo](https://help.yahoo.com/kb/generate-third-party-passwords-sln15241.html)
* [for AOL](https://help.aol.com/articles/Create-and-manage-app-password)
* [for Sky](https://www.sky.com/help/articles/getting-started-with-sky-yahoo-mail) (under *Other email apps*)

Please see [this FAQ](#user-content-faq111) about OAuth support.

Note that Yahoo, AOL, and Sky do not support standard push messages. The Yahoo email app uses a proprietary, undocumented protocol for push messages.

Push messages require [IMAP IDLE](https://en.wikipedia.org/wiki/IMAP_IDLE) and the Yahoo email server does not report IDLE as capability:

```
Y1-EVNE
* MULIGHED FOR IMAP4rev1 ID FLYT NAVNEOMRÅDE XYMHIGHESTMODSEQ UIDPLUS LITERAL+ CHILDREN X-MSG-EXT AFMARKÉR OBJEKTID
Y1 OK MULIGHED fuldført
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

<br />

<a name="faq91"></a>
**~~(91) Can you add periodical synchronization to save battery power?~~**

~~Synchronizing messages is an expensive proces because the local and remote messages need to be compared,~~ ~~so periodically synchronizing messages will not result in saving battery power, more likely the contrary.~~

~~See [this FAQ](#user-content-faq39) about optimizing battery usage.~~

<br />

<a name="faq92"></a>
**(92) Can you add spam filtering, verification of the DKIM signature and SPF authorization?**

Spam filtering, verification of the [DKIM](https://en.wikipedia.org/wiki/DomainKeys_Identified_Mail) signature and [SPF](https://en.wikipedia.org/wiki/Sender_Policy_Framework) authorization is a task of email servers, not of an email client. Servers generally have more memory and computing power, so they are much better suited to this task than battery-powered devices. Also, you'll want spam filtered for all your email clients, possibly including web email, not just one email client. Moreover, email servers have access to information, like the IP address, etc of the connecting server, which an email client has no access to.

Spam filtrering baseret på meddelelsesoverskrifter kunne have været mulig, men denne teknik er desværre [patenteret af Microsoft](https://patents.google.com/patent/US7543076).

Selvfølgelig kan du rapportere beskeder som spam med FairEmail, som vil flytte de rapporterede meddelelser til spam mappe og træne spam filter af udbyderen, hvilket er, hvordan det skal fungere. Dette kan gøres automatisk med [filterregler](#user-content-faq71) også. Blokering af afsenderen vil oprette en filterregel for automatisk at flytte fremtidige breve fra samme afsender til spam mappen.

Bemærk at du ikke bør slette spam-beskeder, heller ikke fra spam-mappen, fordi e-mail-serveren bruger meddelelserne i spam-mappen til at "lære", hvad spam-beskeder er.

Hvis du modtager en masse spam-beskeder i din indbakke, det bedste, du kan gøre, er at kontakte e-mail-udbyder for at spørge, om spam filtrering kan forbedres.

FairEmail kan også vise et lille rødt advarselsflag når DKIM, SPF eller [DMARC](https://en.wikipedia.org/wiki/DMARC) godkendelse mislykkedes på modtagerserveren. Du kan aktivere/deaktivere [godkendelsesbekræftelse](https://en.wikipedia.org/wiki/Email_authentication) i indstillinger.

FairEmail kan også vise et advarselsflag hvis domænenavnet på afsenderens (svar) e- mail- adresse ikke definerer en MX- post, der peger på en e- mail- server. Dette kan aktiveres i modtagelsesindstillingerne. Vær opmærksom på, at dette vil bremse synkronisering af beskeder betydeligt.

Hvis legitime meddelelser ikke autentificeres, du bør underrette afsenderen, da dette vil resultere i en høj risiko for, at beskeder ender i spammappen. Desuden er der uden ordentlig godkendelse en risiko for, at afsenderen vil blive efterlignet. Afsenderen kan bruge [dette værktøj](https://www.mail-tester.com/) til at kontrollere godkendelse og andre ting.

<br />

<a name="faq93"></a>
**(93) Can you allow installation/data storage on external storage media (sdcard)?**

FairEmail uses services and alarms, provides widgets and listens for the boot completed event to be started on device start, so it is not possible to store the app on external storage media, like an sdcard. Se også [her](https://developer.android.com/guide/topics/data/install-location).

Messages, attachments, etc stored on external storage media, like an sdcard, can be accessed by other apps and is therefore not safe. Se [her](https://developer.android.com/training/data-storage) for detaljerne.

When needed you can save (raw) messages via the three-dots menu just above the message text and save attachments by tapping on the floppy icon.

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

You can use *Force sync* in the three-dots menu of the unified inbox to let FairEmail synchronize the folder list again and you can long press the folders to enable synchronization.

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

Setup > Step 1 > Manage > Tap account > Tap advanced > Partial fetch > uncheck

After disabling this setting, you can use the message 'more' (three dots) menu to 'resync' empty messages. Alternatively, you can *Delete local messages* by long pressing the folder(s) in the folder list and synchronize all messages again.

Disabling *Partial fetch* will result in more memory usage.

<br />

<a name="faq111"></a>
**(111) Is OAuth supported?**

OAuth for Gmail is supported via the quick setup wizard. The Android account manager will be used to fetch and refresh OAuth tokens for selected on-device accounts. OAuth for non on-device accounts is not supported because Google requires a [yearly security audit](https://support.google.com/cloud/answer/9110914) ($15,000 to $75,000) for this. Du kan læse mere om dette [her](https://www.theregister.com/2019/02/11/google_gmail_developer/).

OAuth for Yandex og Yahoo er understøttes via guiden for hurtig opsætning.

OAuth for Office 365 accounts is supported, but Microsoft does not offer OAuth for Outlook, Live and Hotmail accounts (yet?).

<br />

<a name="faq112"></a>
**(112) Which email provider do you recommend?**

FairEmail is an email client only, so you need to bring your own email address.

Der er masser af e-mail-udbydere at vælge imellem. Hvilken e-mail-udbyder er bedst for dig, afhænger af dine ønsker/krav. Se venligst hjemmesiderne for [Gendan privatlivets fred](https://restoreprivacy.com/secure-email/) eller [Privacy Tools](https://www.privacytools.io/providers/email/) for en liste over privatlivsorienterede e-mail-udbydere med fordele og ulemper.

Visse udbydere, såsom ProtonMail, Tutanota, anvender proprietære e-mailprotokoller, som umuliggør brug af tredjeparts e-mail apps. Se [denne FAQ](#user-content-faq129) for yderligere information.

Brug af dit eget (brugerdefineret) domænenavn, som understøttes af de fleste e-mail-udbydere, vil gøre det lettere at skifte til en anden e-mail-udbyder.

<br />

<a name="faq113"></a>
**(113) Hvordan virker biometrisk godkendelse?**

Hvis din enhed har en biometrisk sensor, f. eks. en fingeraftrykssensor, du kan aktivere/deaktivere biometrisk godkendelse i menuen navigation (hamburger) i opsætningsskærmen. Når aktiveret vil FairEmail kræve biometrisk godkendelse efter en periode med inaktivitet, eller efter at skærmen er blevet slukket, mens FairEmail kørte. Aktivitet er navigation i FairEmail, for eksempel at åbne en samtaletråd. Inaktivitet periode varighed kan konfigureres i forskellige indstillinger. Når biometrisk godkendelse er aktiveret, vil nye beskedmeddelelser ikke vise noget indhold, og FairEmail vil ikke være synlig på Android seneste skærm.

Biometrisk godkendelse er beregnet til at forhindre andre i at se dine meddelelser kun. FairEmail er afhængig af enhedskryptering til datakryptering, se også [denne FAQ](#user-content-faq37).

Biometrisk godkendelse er en pro-funktion.

<br />

<a name="faq114"></a>
(114) Kan der tilføjes en importmulighed til indstillinger fra andre e-mail apps

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
* Make sure the Play store app is up to date, please [see here](https://support.google.com/googleplay/answer/1050566?hl=en)
* Open the Play store app and wait at least a minute to give it time to synchronize with the Google servers
* Open FairEmail and navigate to the pro features screen to let FairEmail check the purchases; sometimes it help to tap the *buy* button

You can also try to clear the cache of the Play store app via the Android apps settings. Restarting the device might be necessary to let the Play store recognize the purchase correctly.

Bemærk, at:

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

If FairEmail cannot connect to an email server to synchronize messages, for example if the internet connection is bad or a firewall or a VPN is blocking the connection, FairEmail will retry two times after waiting 4 and 8 seconds while keeping the device awake (=use battery power). If this fails, FairEmail will schedule an alarm to retry after 15, 30 and eventually every 60 minutes and let the device sleep (=no battery usage).

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

* [IMAP NOTIFY](https://tools.ietf.org/html/rfc5465) support

IMAP NOTIFY support means that notifications for added, changed or deleted messages of all *subscribed* folders will be requested and if a notification is received for a subscribed folder, that the folder will be synchronized. Synchronization for subscribed folders can therefore be disable, saving folder connections to the email server.

**Important**: push messages (=always sync) for the inbox needs to be enabled.

**Important**: most email server do not support this! You can check the log via the navigation menu if an email server supports the NOTIFY capability.

You can enable experimental features in the miscellaneous settings.

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

The Microsoft Exchange Web Services protocol [is being phased out](https://techcommunity.microsoft.com/t5/Exchange-Team-Blog/Upcoming-changes-to-Exchange-Web-Services-EWS-API-for-Office-365/ba-p/608055).

Note that the desciption of FairEmail starts with the remark that non-standard protocols, like Microsoft Exchange Web Services and Microsoft ActiveSync are not supported.

<br />

<a name="faq134"></a>
**(134) Can you add deleting local messages?**

*POP3*

In the account settings (Setup, step 1, Manage, tap account) you can enable *Leave deleted messages on server*.

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

* Push messages are enabled for too many folders: see [this FAQ](#user-content-faq23) for more information and a workaround
* The account password was changed: changing it in FairEmail too should fix the problem
* An alias email address is being used as username instead of the primary email address
* An incorrect login scheme is being used for a shared mailbox: the right scheme is *username@domain\SharedMailboxAlias*

The shared mailbox alias will mostly be the email address of the shared account, like this:

```
dig@eksempel.dk\delt@eksempel.dk
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

![External image](https://github.com/M66B/FairEmail/blob/master/images/baseline_record_voice_over_black_48dp.png)

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
(adb shell) am startservice -a eu.faircode.email.DISCONNECT.ME
```

Updating once a week will probably be sufficient, please see [here](https://github.com/disconnectme/disconnect-tracking-protection/commits/master) for recent lists changes.

<br />

<a name="faq160"></a>
**(160) Can you add permanent deletion of messages without confirmation?**

Permanent deletion means that messages will *irreversibly* be lost, and to prevent this from happening accidentally, this always needs to be confirmed. Even with a confirmation, some very angry people who lost some of their messages through their own fault contacted me, which was a rather unpleasant experience :-(

Advanced: the IMAP delete flag in combination with the EXPUNGE command is not supportable because both email servers and not all people can handle this, risking unexpected loss of messages. A complicating factor is that not all email servers support [UID EXPUNGE](https://tools.ietf.org/html/rfc4315).

<br />

<a name="faq161"></a>
**(161) Can you add a setting to change the primary and accent color?***

If I could, I would add a setting to select the primary and accent color right away, but unfortunately Android themes are fixed, see for example [here](https://stackoverflow.com/a/26511725/1794097), so this is not possible.

<br />


## Get support

FairEmail is supported on smartphones, tablets and ChromeOS only.

Only the latest Play store version and latest GitHub release are supported. The F-Droid build is supported only if the version number is the same as the version number of the latest GitHub release. This also means that downgrading is not supported.

There is no support on things that are not directly related to FairEmail.

There is no support on building and developing things by yourself.

Requested features should:

* be useful to most people
* not complicate the usage of FairEmail
* fit within the philosophy of FairEmail (privacy oriented, security minded)
* comply with common standards (IMAP, SMTP, etc)

Features not fulfilling these requirements will likely be rejected. This is also to keep maintenance and support in the long term feasible.

If you have a question, want to request a feature or report a bug, please use [this form](https://contact.faircode.eu/?product=fairemailsupport).

GitHub issues are disabled due to frequent misusage.

<br />

Copyright &copy; 2018-2020 Marcel Bokhorst.
