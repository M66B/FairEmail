# Asetusohje

FairEmailin käyttöönotto on melko yksinkertaista. Tarvitset ainakin yhden tilin vastaanottaaksesi sähköpostia ja ainakin yhden tilin lähettääksesi sähköpostia. Pika-asennus lisää samalla sekä tilin että identiteetin. Se toimii useimmilla tunnetuilla palveluntarjoajilla.

## Vaatimukset

Verkkoyhteys tarvitaan tilien ja identiteettien asettamiseksi.

## Pika-asennus

Valitse sopiva palveluntarjoaja tai *Muu palveluntarjoaja* ja syötä nimesi, sähköpostiosoitteesi ja salasanasi ja paina *Tarkista*.

Tämä toimii useimmilla sähköpostipalveluntarjoajilla.

Jos pika-asennus ei toimi, sinun tulee tehdä tili ja identiteetti manuaalisesti alla olevien ohjeiden mukaan.

## Aseta tili - sähköpostien vastaanottamiseksi

Lisääksesi tilin napauta *Manuaalinen asennus ja muita vaihtoehtoja*, napauta *Tilit* ja napauta plus-painiketta alhaalla ja valitse IMAP (tai POP3). Valitse palveluntarjoaja listalta, anna käyttäjätunnus, joka useimmiten on sähköpostiosoite, ja anna salasana. Napauta *Tarkista* antaaksesi FairEmailin yhdistää sähköpostipalvelimeen ja hakea listan järjestelmän kansioista. Järjestelmän kansiovalintojen tarkistuksen jälkeen voit lisätä tilin napauttamalla *Tallenna*.

Mikäli palveluntarjoajasi ei ole luettelossa, on olemassa tuhansia palveluntarjoajia, valitse *Mukautettu*. Lisää verkkotunnus, esimerkiksi *gmail.com* ja napauta *Hae asetukset*. Jos palveluntarjoajasi tukee [auto-discovery](https://tools.ietf.org/html/rfc6186) -toimintoa, FairEmail täyttää verkko-osoitteen ja porttinumeron, muussa tapauksessa katso palveluntarjoajasi asennusohjeista IMAP-nimi, porttinumero ja salausprotokolla (SSL/TLS tai STARTTLS). Katso [täältä](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts) lisätietoja.

## Aseta identiteetti - viestien lähettämiseksi

Samoin, identiteetin asettamiseksi, napauta *Manuaalinen määritys ja lisää vaihtoehtoja*. siitä *Identiteetit* ja napauta plus-painiketta alhaalla. Anna nimi, jonka haluat näkyvän lähettämiesi viestien lähetysosoitteessa ja valitse siihen liittyvä tili. Napauta *Tallenna* lisätäksesi identiteetin.

Jos tili määritettiin manuaalisesti, luultavasti identiteettikin pitää asettaa manuaalisesti. Lisää verkkotunnus, esimerkiksi *gmail.com* ja napauta *Hae asetukset*. Mikäli palveluntarjoajasi tukee [auto-discovery](https://tools.ietf.org/html/rfc6186) -toimintoa, FairEmail täyttää verkko-osoitteen ja porttinumeron, muussa tapauksessa katso palveluntarjoajasi aloitusoppaasta oikea SMTP-nimi, porttinumero ja suojausmenetelmä (SSL/TLS tai STARTTLS).

Lue [tämä usein kysytyt kysymykset](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) aliasten käytöstä.

## Myönnä käyttöoikeudet - yhteystietoihin pääsemiseksi

FairEmail tarvitsee oikeudet yhteystietoihin, mikäli haluat katsoa sähköpostiosoitteita tai saada näkyviin yhteystietojen kuvia. Klikkaa *Myönnä* ja valitse *Salli*.

## Aseta akun käytön optimointi - sähköpostien vastaanottamiseksi ilman keskeytyksiä

Viimeisimmissä Android-versiossa sovellukset laitetaan lepotilaan akun käytön vähentämiseksi, kun näyttö on ollut jonkin aikaa pois päältä. Jos haluat vastaanottaa sähköposteja ilman keskeytyksiä, pitää akun käytön optimointi ottaa pois käytöstä FairEmailille. Napauta *Hallitse* ja seuraa ohjeita.

## Kysymyksiä tai ongelmia

Jos sinulla on kysymys tai ongelma, ole hyvä ja [katso täältä](https://github.com/M66B/FairEmail/blob/master/FAQ.md) apua.