# Seadistamise abi

FairEmaili algseadistamine on üsna lihtne. Pead lisama vähemalt ühe konto, et saada kirju, ja vähemalt ühe identiteedi, kui soovid kirju saata. Kiirseadistaja lisab enamike suurte teenusepakkujate jaoks ühe tegevusega nii konto kui ka identiteedi.

## Nõuded

Kontode ja identiteetide algseadistamiseks on vajalik internetiühendus.

## Kiirseadistamine

Vali vastav teenusepakkuja või *Muu teenusepakkuja* ja sisesta oma nimi, e-postiaadress ja salasõna and tap *Check*.

See töötab enamike e-posti teenusepakkujate puhul.

Kui kiirseadistamine ei toimi, pead seadistama konto ja identiteedi käsitsi. Loe juhiseid allpool.

## Seadista konto, et saada e-kirju

Konto lisamiseks toksa *Käsitsi seadistamine ja Veel valikuid*, toksa *Kontod* ja toksa "pluss" button at the bottom and select IMAP (or POP3). Vali loetelust teenusepakkuja, sisesta kasutajanimi, mis on enamasti su e-postiaadress, ja sisesta oma salasõna. Toksa *Kontrolli*, et FairEmail ühenduks meiliserveriga ja hangiks süsteemikaustade loetelu. Pärast süsteemikaustade valiku üle vaatamist saad konto lisada, toksates *Salvesta*.

Kui su teenusepakkuja pole teenusepakkujate loetelus, olemas on neid tuhandeid, vali *Kohandatud*. Sisesta domeeni nimi, näiteks *gmail.com* ja toksa *Hangi seaded*. Kui su teenusepakkuja toetab [automaatset tuvastamist](https://tools.ietf.org/html/rfc6186), täidab FairEmail hostinime ja pordinumbri. Vastasel juhul leia oma teenusepakkuja seadistamisjuhenditest õige IMAPi hostinimi, pordi number ja krüpteerimisprotokoll (SSL/TLS või STARTTLS) Rohkem sellest vaata palun [siit](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Seadista identiteet - et saata e-kirju

Sarnaselt toksa identiteedi lisamiseks valikut *Käsitsi seadistamine ja Veel valikuid*, toksa valikut *Identiteedid* ja allosas toksa nuppu "pluss". Sisesta nimi, mida peaks näidatama sinu saadetavate e-kirjade saatja aadressi juures, ja vali lingitav konto. Toksa *Salvesta*, et identiteet lisada.

Kui konto seadistati käsitsi, pead tõenäoliselt ka identiteedi käsitsi häälestama. Sisesta domeeni nimi, näiteks *gmail.com*, ja toksa *Hangi seaded*. Kui su teenusepakkuja toetab [automaatset tuvastamist](https://tools.ietf.org/html/rfc6186), täidab FairEmail hostinime ja pordinumbri. Vastasel juhul leia oma teenusepakkuja seadistamisjuhenditest õige SMTP hostinimi, pordi number ja krüpteerimisprotokoll (SSL/TLS või STARTTLS).

Aliaste kasutamise kohta vaata [seda KKK-d](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9).

## Võimalda õigused - et pääseda ligi kontaktide infole

Kui soovid otsida e-posti aadresse, näha kontaktide fotosid jne, pead andma FairEmailile loa kontaktandmete lugemiseks. Toksa lihtsalt *Taga* ja vali *Luba*.

## Seadista aku optimeerimine - et saada e-kirju katkestusteta

Uuemates Androidi versioonides lülitab Android rakendused puhkeolekusse, kui ekraan on olnud mõnda aega välja lülitatud, et vähendada aku tarbimist. Kui soovid saada kirju ilma viivituseta, peaksid FairEmaili jaoks aku optimeerimise välja lülitama. Toksa *Halda* ja järgi juhiseid.

## Küsimused või probleemid

Kui sul on küsimus või probleem, vaata palun [abiks siia](https://github.com/M66B/FairEmail/blob/master/FAQ.md).