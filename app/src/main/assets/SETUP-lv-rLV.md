# Iestatīšanas palīdzība

FairEmail iestatīšana ir diezgan vienkārša. Būs nepieciešams pievienot vismaz vienu kontu, lai saņemtu e-pasta ziņojumus, un vismaz viena identitāte, ja ir vēlēšanās nosūtīt e-pasta ziņojumus. Ātrā iestatīšana pievienos kontu un identitāti vienā piegājienā vairumam lielāko pakalpojumu sniedzēju.

## Prasības

Ir nepieciešams interneta savienojums, lai varētu iestatīt kontus un identitātes.

## Ātrā iestatīšana

Vienkārši jāatlasa atbilstošais pakalpojumu sniedzējs vai *Cits pakalpojumu sniedzējs* un jāievada savs vārds, e-pasta adrese un parole, un tad jāpiesit *Pārbaudīt*.

Tas darbojas ar vairumu e-pasta pakalpojuma sniedzēju.

Ja ātrā iestatīšana nedarbojas, būs nepieciešams pašrocīgi iestatīt kontu un identitāti, norādes ir atrodamas zemāk.

## Iestatīt kontu - e-pasta saņemšanai

Lai pievienotu kontu, jāpiesit *Pašrocīga iestatīšana un parējie iestatījumi*, tad *Konti*, tad apakšā esošajai pogai "+" un jāatlasa IMAP (vai POP3). No saraksta jāatlasa pakalpojumu sniedzējs, jāievada lietotājvārds, kas vairumā gadījumu ir e-pasta adrese, un parole. Jāpiesit *Pārbaudīt*, lai ļautu FairEmail pieslēgties e-pasta serverim un iegūt sistēmas mapju sarakstu. Pēc sistēmas mapju atlases pārskatīšanas kontu var pievienot ar piesišanu uz *Saglabāt*.

Ja sarakstā nav vajadzīgā pakalpojumu sniedzēja, jāatlasa *Pielāgots*, ir tūkstošiem pakalpojumu sniedzēju. Jāievada domēna vārds, piemēram, *proton.me* un jāpiesit *Iegūt iestatījumus*. Ja pakalpojumu sniedzējs nodrošina [automātisko atklāšanu](https://tools.ietf.org/html/rfc6186), FairEmail aizpildīs resursdatora nosaukumu un porta numuru, pretējā gadījumā sava pakalpojumu sniedzēja norādēs jāmeklē pareizais IMAP resursdatora nosaukums, porta numurs un šifrēšanas protokols (SSL/TLS vai STARTTLS). Vairāk par to lūgums skatīt [šeit](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Iestatīt identitāti - e-pasta sūtīšanai

Līdzīgi kā iepriekš, lai pievienotu identitāti, jāpiesit *Pašrocīga iestatīšana un vairāk iespēju*, tad *Identitātes* un tad apakšā esošajai "+" pogai. Jāievada vārds, kuru parādīt nosūtītāja adresē sevis nosūtītajos e-pasta ziņojumos, un jāatlasa saistītais konts. Jāpiesit *Saglabāt*, lai pievienotu identitāti.

Ja konts tika pašrocīgi iestatīts, visdrīzāk, ka tas ir jādara arī ar identitāti. Jāievada domēna vārds, piemēram, *proton.me* un jāpiesit *Iegūt iestatījumus*. Ja pakalpojumu sniedzējs nodrošina [automātisko atklāšanu](https://tools.ietf.org/html/rfc6186), FairEmail aizpildīs resursdatora nosaukumu un porta numuru, pretējā gadījumā sava pakalpojumu sniedzēja norādēs jāmeklē pareizais SMTP resursdatora nosaukums, porta numurs un šifrēšanas protokols (SSL/TLS vai STARTTLS).

Par aizstājvārdu izmantošanu skatīt [šo BUJ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9).

## Piešķirt atļaujas - lai piekļūtu saziņas informācijai

Ja ir vēlēšanas uzmeklēt e-pasta adreses, redzēt kontaktpersonu attēlus utt., būs nepieciešams piešķirt FairEmail atļauju lasīt saziņas informāciju. Vienkārši jāpiesit uz *Piešķirt* un jāizvēlas *Atļaut*.

## Iestatīt akumulatora optimizēšanu - lai pastāvīgi saņemtu e-pasta ziņojumus

Nesenākās Android versijās lietotnes tiek iemidzinātas, kad ekrāns kādu laiku ir izslēgts, lai samazinātu akumulatora lietojumu. Ja ir vēlēšanās nekavējoties saņemt jaunus e-pasta ziņojumus, tad FairEmail lietotnei vajadzētu atspējot akumulatora optimizēšanu. Jāpiesit *Pārvaldīt* un jāseko norādēm.

## Jautājumi vai sarežģījumi

Ja ir kāds jautājums vai sarežģījums, lūgums meklēt palīdzību [šeit](https://github.com/M66B/FairEmail/blob/master/FAQ.md).