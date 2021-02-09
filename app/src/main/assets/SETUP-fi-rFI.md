# Asetusohje

FairEmailin käyttöönotto on melko yksinkertaista. Tarvitset ainakin yhden tilin vastaanottaaksesi sähköpostia ja ainakin yhden tilin lähettääksesi sähköpostia. The quick setup will add an account and an identity in one go for most major providers.

## Vaatimukset

Verkkoyhteys tarvitaan tilien ja identiteettien asettamiseksi.

## Pika-asennus

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Aseta tili - sähköpostien vastaanottamiseksi

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Valitse palveluntarjoaja listalta, anna käyttäjätunnus, joka useimmiten on sähköpostiosoite, ja anna salasana. Napauta *Tarkista* antaaksesi FairEmailin yhdistää sähköpostipalvelimeen ja hakea listan järjestelmän kansioista. Järjestelmän kansiovalintojen tarkistuksen jälkeen voit lisätä tilin napauttamalla *Tallenna*.

If your provider is not in the list of providers, there are thousands of providers, select *Custom*. Lisää verkkotunnus, esimerkiksi *gmail.com* ja napauta *Hae asetukset*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Katso [täältä](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts) lisätietoja.

## Aseta identiteetti - viestien lähettämiseksi

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Anna nimi, jonka haluat näkyvän lähettämiesi viestien lähetysosoitteessa ja valitse siihen liittyvä tili. Napauta *Tallenna* lisätäksesi identiteetin.

Jos tili määritettiin manuaalisesti, luultavasti identiteettikin pitää asettaa manuaalisesti. Lisää verkkotunnus, esimerkiksi *gmail.com* ja napauta *Hae asetukset*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Lue [tämä usein kysytyt kysymykset](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) aliasten käytöstä.

## Myönnä käyttöoikeudet - yhteystietoihin pääsemiseksi

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Aseta akun käytön optimointi - sähköpostien vastaanottamiseksi ilman keskeytyksiä

Viimeisimmissä Android-versiossa sovellukset laitetaan lepotilaan akun käytön vähentämiseksi, kun näyttö on ollut jonkin aikaa pois päältä. Jos haluat vastaanottaa sähköposteja ilman keskeytyksiä, pitää akun käytön optimointi ottaa pois käytöstä FairEmailille. Tap *Manage* and follow the instructions.

## Kysymyksiä tai ongelmia

Jos sinulla on kysymys tai ongelma, ole hyvä ja [katso täältä](https://github.com/M66B/FairEmail/blob/master/FAQ.md) apua.