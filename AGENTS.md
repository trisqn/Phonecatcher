# AGENTS.md

## 1. Dokumendi eesmärk

See fail kirjeldab projekti konteksti, arhitektuuri, ärireegleid, tehnilisi piiranguid ja töökorraldust AI-koodiagentidele ning arendajatele. Kõik projekti juures töötavad agendid peavad enne muudatuste tegemist selle faili läbi lugema ja lähtuma siin kirjeldatud reeglitest.

Projekti põhiosa on Java ja Spring Bootiga loodav tagarakendus. Mobiilirakendus ei kuulu esialgse teostuse kohustuslikku ulatusse. Tagarakenduse funktsionaalsust peab saama demonstreerida Swagger UI, automatiseeritud testide ja API-päringute abil.

Kui käesolev fail, lähtekood, andmebaasimigratsioonid ja README lähevad omavahel vastuollu, ei tohi agent teha vaikimisi oletust. Agent peab esmalt uurima olemasolevat lahendust ning hoidma ärireeglid, migratsioonid, testid ja dokumentatsiooni omavahel kooskõlas.

---

## 2. Projekti lühikirjeldus

Rakendus kogub ja töötleb kasutajate esitatud raporteid telefoninumbrite kohta, mida seostatakse võimalike petukõnede või petusõnumitega.

Autenditud kasutaja saab esitada kahtlase telefoninumbri kohta raporti. Kui sama telefoninumbri kohta koguneb piisavalt erinevate kasutajate arvestatavaid raporteid, loob süsteem automaatselt hoiatuse. Hoiatus sisaldab telefoninumbrit, domineerivat pettusekategooriat, kasutajale kuvatavat pealkirja ja staatust.

Mobiilirakendus saab hoiatusi tagarakendusest perioodiliselt sünkroonida ja salvestada need kohalikku andmebaasi. Telefoninumbri tegelik kontrollimine ning seadmesisese teavituse kuvamine toimub mobiilirakenduses, mitte tagarakenduses.

Autenditud kasutaja saab esitada aktiivse hoiatuse kohta vaidlustuse. Moderaator või administraator saab vaidlustuse rahuldada või tagasi lükata. Rahuldatud vaidlustuse korral märgitakse hoiatus tagasivõetuks. Hoiatused võivad ka perioodiliselt aeguda.

Rakendus ei kinnita telefoninumbri omaniku süüd ega tuvasta konkreetset isikut. Süsteem väljastab kasutajate raportitel ja määratud ärireeglitel põhinevat riskiteavet. Aktiivse hoiatuse puudumine ei tähenda, et telefoninumber on kindlasti ohutu.

---

## 3. Projekti ulatus

### 3.1. MVP-sse kuulub

- kasutajakonto loomine;
- kasutaja autentimine;
- kasutajarollid ja backendis kontrollitav autoriseerimine;
- aktiivsete pettusekategooriate vaatamine;
- raporti esitamine;
- telefoninumbri normaliseerimine;
- hoiatuse automaatne loomine;
- aktiivse hoiatuse domineeriva kategooria uuendamine;
- aktiivsete hoiatuste avalik vaatamine;
- hoiatuste ajapõhine sünkroonimine;
- aktiivsete hoiatuste perioodiline aegumine;
- vaidlustuse esitamine;
- vaidlustuse menetlemine moderaatori või administraatori poolt;
- PostgreSQL-i skeemi versioneerimine Flyway abil;
- konfigureeritav Log4j2 logimine;
- Swagger/OpenAPI dokumentatsioon;
- üksus-, integratsiooni- ja mõned End-to-End testid;
- rakenduse käivitamist kirjeldav README.

### 3.2. Esialgsest MVP-st jääb välja

- mobiilirakenduse teostamine;
- veebipõhine kasutajaliides;
- e-posti kinnitamine;
- parooli taastamine;
- refresh token'id;
- väliste identiteedipakkujate kaudu sisselogimine;
- raportite muutmine ja kustutamine;
- hoiatuste käsitsi kustutamine;
- failide, ekraanipiltide või kõnesalvestiste üleslaadimine;
- eraldi auditilogi alamsüsteem;
- mikroteenusteks jagamine;
- sõnumijärjekorrad;
- keerukas sünkroonimiskursor;
- reaalajas telefoninumbri kontroll tagarakenduses;
- push-teavituste saatmine;
- telefoninumbri omaniku tuvastamine.

Agent ei tohi lisada MVP-st välja jäetud funktsionaalsust ilma selge ülesandeta. Eesmärk on hoida kursuseprojekti maht kontrolli all.

---

## 4. Tehnoloogilised valikud

Projektis kasutatakse eelistatult järgmisi tehnoloogiaid:

- Java 21;
- Spring Boot 3;
- Gradle Wrapper;
- Gradle Groovy DSL, kui projektis ei ole juba valitud Kotlin DSL-i;
- Spring Web;
- Spring Data JPA;
- Hibernate;
- PostgreSQL;
- Flyway;
- Spring Security;
- JWT-põhine autentimine;
- Jakarta Bean Validation;
- Log4j2 koos SLF4J-ga;
- Springdoc OpenAPI ja Swagger UI;
- JUnit 5;
- Mockito;
- AssertJ;
- Spring Security Test;
- Testcontainers PostgreSQL;
- RestAssured E2E testide jaoks;
- JaCoCo testikatte mõõtmiseks;
- Docker Compose kohaliku PostgreSQL-i käivitamiseks;
- Google libphonenumber telefoninumbrite normaliseerimiseks, kui sõltuvus on projektis heaks kiidetud.

Ära asenda Gradle'it Maveniga. Ära kasuta H2 andmebaasi integratsioonitestides, kui testitav funktsionaalsus sõltub PostgreSQL-i käitumisest, osalistest indeksitest, ajatüüpidest või piirangutest.

---

## 5. Arhitektuur

### 5.1. Üldine lähenemine

Rakendus on modulaarne monoliit. Seda ei jagata mikroteenusteks.

Valiku põhjused:

- raporti lisamine ja hoiatuse ümberarvutamine vajavad ühist transaktsiooni;
- domeeniosad on omavahel tihedalt seotud;
- üks juurutatav rakendus on kursuseprojekti mahus lihtsam testida ja käitada;
- mikroteenused suurendaksid infrastruktuuri ja hajutatud andmekäitluse keerukust ilma selge praktilise kasuta.

### 5.2. Kohustuslikud kihid

Igas funktsionaalses moodulis tuleb eristada vähemalt:

- controller-kiht;
- service-kiht;
- repository-kiht.

Vajadusel kasutatakse lisaks:

- DTO-sid;
- mappereid;
- entity-klassi;
- valideerijaid;
- turvakomponente;
- scheduler-komponente;
- konfiguratsiooniklasse.

### 5.3. Vastutuse jaotus

#### Controller

Controller vastutab HTTP-kihi eest:

- võtab vastu päringu;
- rakendab sisendi deklaratiivse valideerimise;
- kutsub sobivat teenust;
- tagastab sobiva HTTP staatuse ja DTO;
- ei sisalda äriloogikat;
- ei kasuta repositooriumi otse.

#### Service

Service vastutab äriloogika ja transaktsioonide eest:

- kontrollib ärireegleid;
- koordineerib repositooriume;
- määrab transaktsioonipiirid;
- loob ja uuendab hoiatusi;
- arvutab domineeriva kategooria;
- menetleb vaidlustusi;
- ei sõltu HTTP detailidest.

#### Repository

Repository vastutab andmepääsu eest:

- salvestab ja loeb olemeid;
- sisaldab vajalikke Spring Data, JPQL või native SQL päringuid;
- ei tee ärilisi otsuseid;
- ei tagasta avaliku API vastuseid.

### 5.4. Soovitatud paketistruktuur

Paketid tuleb eelistatult jagada funktsionaalse valdkonna järgi:

- auth;
- user;
- phonenumber;
- category;
- report;
- warning;
- dispute;
- sync;
- security;
- config;
- common.exception;
- common.validation.

Iga mooduli sees võib kasutada alamjaotusi nagu controller, service, repository, entity ja dto. Ärge looge ühte üleprojektilist hiiglaslikku controller-, service- või repository-paketti, kui domeenipõhine jaotus on võimalik.

---

## 6. Peamised domeeniobjektid

### 6.1. User

Kasutaja on tehniline olem autentimise, autoriseerimise ning raportite ja vaidlustuste esitajate tuvastamise jaoks.

Olulised väljad:

- id;
- email;
- passwordHash;
- role;
- enabled;
- createdAt.

Rollid:

- USER;
- MODERATOR;
- ADMIN.

### 6.2. PhoneNumber

Telefoninumbri olem talletab telefoninumbri normaliseeritud kuju.

Olulised väljad:

- id;
- normalizedNumber;
- createdAt.

Sama normaliseeritud telefoninumber tohib andmebaasis esineda ainult ühe korra.
Telefoninumber peab sisaldama rahvusvahelist riigikoodi ning olema salvestatud E.164 kujul.

### 6.3. FraudCategory

Pettusekategooria kirjeldab isikut või rolli, kellena pettur esineb, näiteks politseitöötajat,
postitöötajat või pangatöötajat. Kategooriad lisatakse alguses Flyway algandmetena.

Olulised väljad:

- id;
- code;
- name;
- warningTitle või pealkirja koostamiseks vajalik mall;
- active;
- createdAt;
- updatedAt.

Kasutatud kategooriat ei kustutata. Uutes raportites mittekasutatav kategooria märgitakse mitteaktiivseks.
- Administraator saab kategooriaid luua, muuta, aktiveerida ja deaktiveerida; kategooria kood on kordumatu.

### 6.4. Report

Raport kirjeldab kasutaja teatatud juhtumit.

Olulised väljad:

- id;
- reporter;
- phoneNumber;
- category;
- incidentDate;
- incidentTime, valikuline;
- communicationChannel;
- description, valikuline;
- createdAt.

### 6.5. Warning

Hoiatus on sünkroonitav domeeniobjekt, mis annab telefoninumbriga seotud riskiteavet.

Olulised väljad:

- id;
- phoneNumber;
- dominantCategory;
- title;
- status;
- activatedAt;
- deactivatedAt, valikuline;
- createdAt;
- updatedAt;
- version, kui kasutatakse optimistlikku lukustamist.

Hoiatuse staatused:

- ACTIVE;
- EXPIRED;
- WITHDRAWN;
- THRESHOLD_NOT_MET.

### 6.6. Dispute

Vaidlustus kirjeldab kasutaja taotlust aktiivse hoiatuse läbivaatamiseks.

Olulised väljad:

- id;
- warning;
- submitter;
- reviewer, enne menetlemist valikuline;
- reason;
- status;
- reviewComment, enne menetlemist valikuline;
- submittedAt;
- reviewedAt, enne menetlemist valikuline.

Vaidlustuse staatused:

- SUBMITTED;
- ACCEPTED;
- REJECTED.

---

## 7. Põhilised ärireeglid

### 7.1. Kasutajad ja turvalisus

- Sama e-posti aadressiga tohib olla ainult üks konto.
- E-posti unikaalsus peab olema tõstutundetu.
- Parooli ei salvestata kunagi avatekstina.
- Raporti või vaidlustuse saab esitada ainult autenditud kasutaja.
- Vaidlustust saab menetleda ainult MODERATOR või ADMIN.
- Kasutaja ei saa muuta teise kasutaja raportit või vaidlustust.
- Õigusi kontrollitakse alati backendis.
- Avalik API ei väljasta raportite esitajate isikuandmeid.

### 7.2. Telefoninumbrid

- Telefoninumber normaliseeritakse enne otsimist ja salvestamist.
- Normaliseeritud number peab olema unikaalne.
- Telefoninumbrit ei kustutata hoiatuse aegumisel ega tagasivõtmisel.
- Tagarakendus ja võimalik mobiilirakendus peavad kasutama funktsionaalselt samu normaliseerimisreegleid.

### 7.3. Raportid

- Raport kuulub täpselt ühele kasutajale, telefoninumbrile ja kategooriale.
- Valida saab ainult aktiivse kategooria.
- Juhtumi kuupäev ja suhtluskanal on kohustuslikud.
- Juhtumi kellaaeg ja kirjeldus on valikulised.
- Tuleviku kuupäevaga juhtumit ei aktsepteerita, kui spetsifikatsioonis ei otsustata teisiti.
- Tõendi või faili lisamist ei nõuta.
- Sama kasutaja korduvad raportid võivad säilida ajaloona.
- Hoiatuse arvutamisel annab üks kasutaja ühe telefoninumbri kohta maksimaalselt ühe arvestatava raporti.

### 7.4. Arvestatava raporti definitsioon

MVP soovituslik reegel on järgmine:

> Iga kasutaja kohta valitakse konkreetse telefoninumbri kõige uuem raport. Seega annab üks kasutaja ühe telefoninumbri kohta maksimaalselt ühe arvestatava raporti.

Sama arvestatavate raportite definitsiooni tuleb kasutada:

- hoiatuse lävendi kontrollimisel;
- kategooriate jaotuse arvutamisel;
- domineeriva kategooria leidmisel;
- viimase arvestatava raporti aja leidmisel;
- hoiatuse aegumise arvutamisel.

Kui projektis valitakse teistsugune definitsioon, tuleb korraga uuendada seda faili, teste ja README-d.

### 7.5. Hoiatuse loomine

- Hoiatus luuakse, kui telefoninumbri kohta on vähemalt seadistatud lävendi jagu erinevate kasutajate arvestatavaid raporteid.
- MVP vaikimisi lävend on 2 ning seda saab muuta ainult administraator.
- Lävendimuutus arvutab olemasolevate numbrite raportid uuesti. Lävendit enam mittetäitvad aktiivsed hoiatused märgitakse THRESHOLD_NOT_MET olekusse.
- Ühe telefoninumbri kohta võib korraga olla maksimaalselt üks ACTIVE hoiatus.
- Hoiatus seotakse kõige rohkem arvestatavaid raporteid saanud kategooriaga.
- Hoiatuse loomine ja raporti salvestamine peavad olema transaktsiooniliselt kooskõlalised.
- Kriitiline unikaalsus tuleb jõustada ka andmebaasis, mitte ainult Java koodis.

### 7.6. Domineeriva kategooria muutmine

- Pärast uue raporti lisamist arvutatakse kategooriate jaotus uuesti.
- Kui mõni uus kategooria saab aktiivse hoiatuse praegusest kategooriast rangelt rohkem arvestatavaid raporteid, muudetakse kategooriat.
- Vajadusel uuendatakse hoiatuse pealkirja.
- Kui kategooriate arv on võrdne, jääb aktiivse hoiatuse senine kategooria alles.
- Muudatuse korral uuendatakse warning.updatedAt väärtust.
- Uue hoiatuse esialgse viigi lahendamise reegel peab olema enne teostust testiga fikseeritud. Lihtne deterministlik variant on valida seotud arvestatavatest raportitest kõige varem loodud raporti kategooria.

### 7.7. Hoiatuse aegumine

- Perioodiline taustatöö käivitatakse vaikimisi kord 24 tunni jooksul.
- MVP vaikimisi aegumisperiood on 60 päeva.
- Aegumisperioodi saab muuta ainult administraator; muutmisel arvutatakse aktiivsete hoiatuste aegumine kohe uuesti.
- Aegumist arvestatakse telefoninumbri viimase arvestatava raporti järgi.
- Kui määratud perioodi jooksul pole uut arvestatavat raportit, muutub ACTIVE hoiatus EXPIRED olekusse.
- Aegunud hoiatusele määratakse deactivatedAt.
- Aegumisel uuendatakse updatedAt.
- Telefoninumbrit, raporteid ja hoiatust ei kustutata.

### 7.8. Vaidlustused

- Vaidlustada saab ainult olemasolevat ACTIVE hoiatust.
- Vaidlustuse esitamiseks peab kasutaja olema autenditud.
- Põhjendus on kohustuslik.
- Ühel kasutajal ei tohi ühe hoiatuse kohta olla korraga mitut SUBMITTED vaidlustust.
- Vaidlustuse esitamine ei muuda automaatselt hoiatuse staatust.
- USER ei saa vaidlustust menetleda.
- MODERATOR või ADMIN võib vaidlustuse rahuldada või tagasi lükata.
- Otsusele lisatakse läbivaataja kommentaar.
- Tagasilükkamisel muutub vaidlustus REJECTED olekusse ja hoiatus jääb ACTIVE olekusse.
- Rahuldamisel muutub vaidlustus ACCEPTED olekusse ja hoiatus WITHDRAWN olekusse.
- Tagasivõetud hoiatusele määratakse deactivatedAt ja uuendatakse updatedAt.
- Menetlemise kommentaar on kohustuslik.
- Menetluse ajalugu säilitatakse.

### 7.9. Uue hoiatuse loomine pärast deaktiveerimist

Aegunud või tagasivõetud hoiatust ei muudeta uuesti aktiivseks ega kirjutata üle. Tulevikus võib sama telefoninumbri kohta tekkida uus hoiatus.

Vanad raportid ei tohi põhjustada tagasivõetud või aegunud hoiatuse kohest taasloomist. Uue hoiatuse lävendi arvutamisel tuleb arvestada ainult pärast eelmise hoiatuse deactivatedAt väärtust loodud sobivaid raporteid. See reegel tuleb enne teostamist integratsioonitestiga kinnitada.

---

## 8. Sünkroonimine

### 8.1. Eesmärk

Sünkroonimisliides võimaldab mobiilirakendusel laadida tagarakendusest aktiivsed ning hiljem muutunud hoiatused. Mobiilirakendus säilitab nende piiratud koopia oma kohalikus andmebaasis.

Tagarakenduse andmebaas on keskne ja usaldusväärne andmeallikas.

### 8.2. Päringu põhimõte

Klient saadab GET-päringus viimase edukalt lõpetatud sünkroonimise aja. Aeg edastatakse query-parameetrina, mitte tingimata path variable'ina.

Kontseptuaalne liides:

- esimene sünkroonimine: GET /api/v1/sync/warnings;
- järgmine sünkroonimine: GET /api/v1/sync/warnings?updatedAfter=<ISO-8601 UTC aeg>.

Endpoint'i lõplik nimi peab vastama projekti olemasolevale URL-konventsioonile.

### 8.3. Esimene sünkroonimine

Kui updatedAfter puudub:

- tagastatakse kõik ACTIVE hoiatused;
- ajaloolisi EXPIRED ja WITHDRAWN hoiatusi ei ole vaja uuele kliendile saata;
- vastus sisaldab serveri määratud syncTime väärtust.

### 8.4. Järgmised sünkroonimised

Kui updatedAfter on määratud:

- leitakse hoiatused, mille updatedAt on eelmisest edukast sünkroonimisest hilisem;
- tagastatakse nii ACTIVE, EXPIRED kui ka WITHDRAWN hoiatused;
- klient kasutab staatust kohaliku kirje lisamiseks, muutmiseks või mitteaktiivseks märkimiseks;
- vastus sisaldab serveri uut syncTime väärtust.

### 8.5. Serveri aja kasutamine

Klient ei tohi järgmise sünkroonimise alusena kasutada telefoni enda hetkeaega. Server määrab päringu töötlemise alguses või kokkulepitud hetkel syncTime väärtuse.

Soovituslik ajavahemik on:

- updatedAt > previousSyncTime;
- updatedAt <= currentServerSyncTime.

Klient salvestab currentServerSyncTime väärtuse ainult pärast seda, kui vastus on edukalt kohalikku andmebaasi rakendatud. Ebaõnnestunud sünkroonimise korral säilitab klient eelmise eduka aja.

### 8.6. Aja formaat

- Kasuta ISO 8601 vormingut.
- Kasuta UTC aega.
- Java domeenis eelista Instant tüüpi.
- Näidis: 2026-11-25T08:30:00Z.

### 8.7. Sünkroonimisvastuse minimaalne sisu

Iga hoiatuse kohta tagastatakse vähemalt:

- hoiatuse id;
- normaliseeritud telefoninumber;
- kategooria kood või id;
- kategooria nimi, kui klient seda vajab;
- pealkiri;
- staatus;
- updatedAt.

Üldvastus sisaldab vähemalt:

- items;
- syncTime.

MVP-s ei ole keerukas kodeeritud kursor kohustuslik. Kui vastused muutuvad suureks, võib hiljem lisada lehekülgede kaupa pärimise, kuid seda ei tohi lisada ennetavalt ilma vajaduseta.

### 8.8. Sünkroonimise piirangud

- Sünkroonimisliides ei väljasta raportite kirjeldusi.
- Sünkroonimisliides ei väljasta raportite esitajate andmeid.
- Sünkroonimisliides ei tee reaalajas telefoninumbri kontrolli.
- Tagarakendus ei saada telefonikõne saabumise hetkel teavitust.
- Kohalikust andmebaasist puuduva hoiatuse põhjal ei tohi järeldada, et number on kindlasti ohutu.

---

## 9. API kujundamise põhimõtted

- Kasuta versioonitud baasteed, näiteks /api/v1.
- Kasuta ressursipõhiseid URL-e.
- Kasuta HTTP meetodeid nende tavapärases tähenduses.
- Loomisel tagasta 201 Created.
- Vigase sisendi puhul tagasta 400 Bad Request.
- Autentimata kaitstud päringu puhul tagasta 401 Unauthorized.
- Ebapiisavate õiguste puhul tagasta 403 Forbidden.
- Puuduva ressursi puhul tagasta 404 Not Found.
- Unikaalsus- või olekukonflikti puhul kasuta vajadusel 409 Conflict.
- Ära väljasta JPA entity-objekte otse.
- Kasuta eraldi request- ja response-DTO-sid.
- Väldi tundlike sisemiste väljade sattumist JSON vastustesse.
- Kasuta ühtset veavastuse formaati.
- Veavastus võib sisaldada näiteks aega, staatust, veakoodi, sõnumit, päringu teed ja väljade valideerimisvigu.

Kontrollerite endpoint'id peavad olema Swagger/OpenAPI annotatsioonidega dokumenteeritud vähemalt niivõrd, et kasutaja saab Swagger UI kaudu põhivood läbi teha.

---

## 10. Andmebaas ja migratsioonid

### 10.1. Üldreeglid

- Andmebaas on PostgreSQL.
- Kõik skeemimuudatused tehakse Flyway migratsioonidega.
- Hibernate ei tohi tootmis- ega tavakeskkonnas skeemi automaatselt luua või muuta.
- Soovitatud Hibernate seadistus on skeemi valideerimine.
- Olemi muudatusega peab kaasnema vastav migratsioon.
- Juba ühises repositooriumis rakendatud migratsiooni ei muudeta tagantjärele. Lisatakse uus migratsioon.

### 10.2. Piirangud

Andmebaasis tuleb jõustada vähemalt:

- kasutaja e-posti unikaalsus;
- normaliseeritud telefoninumbri unikaalsus;
- kategooria koodi unikaalsus;
- vajalikud NOT NULL piirangud;
- võõrvõtmed;
- maksimaalselt üks ACTIVE hoiatus telefoninumbri kohta;
- maksimaalselt üks SUBMITTED vaidlustus kasutaja ja hoiatuse paari kohta.

Ärireegleid kontrollitakse teenuses, kuid kriitilised terviklikkusreeglid dubleeritakse andmebaasipiirangutena, et kaitsta paralleelsete päringute eest.

### 10.3. Indeksid

Indekseeri vähemalt väljad, mida kasutatakse sagedasti:

- user.email;
- phone_number.normalized_number;
- report.phone_number_id;
- report.reporter_id;
- report.created_at;
- warning.phone_number_id;
- warning.status;
- warning.updated_at;
- dispute.warning_id;
- dispute.status.

Ära lisa indekseid oletuse põhjal igale väljale. Indeksite valik peab vastama tegelikele päringutele.

---

## 11. Transaktsioonid ja samaaegsus

- Kirjutavad ärilised teenusemeetodid peavad kasutama põhjendatud transaktsioonipiire.
- Raporti salvestamine ja hoiatuse ümberarvutamine peavad moodustama kooskõlalise operatsiooni.
- Kahe samaaegse raporti korral ei tohi tekkida kaks aktiivset hoiatust.
- Ära looda ainult rakenduse taseme eelkontrollile; kasuta andmebaasi unikaalset piirangut.
- Vajadusel kasuta optimistlikku lukustamist Warning olemi version-väljaga.
- Väldi pikki transaktsioone ja väliseid võrgukutseid transaktsiooni sees.
- Andmebaasikonfliktid tuleb teisendada kontrollitud domeeni- või API veaks.

---

## 12. Turvalisus

### 12.1. Autentimine

- Kasutaja autentimiseks kasutatakse e-posti ja parooli.
- Parool räsitakse Spring Security toetatud tugeva algoritmiga, näiteks BCrypt või Argon2.
- Eduka autentimise korral väljastatakse JWT ligipääsutõend.
- JWT saladust või privaatvõtit ei lisata Git-repositooriumisse.
- Sisselogimise veateade ei tohi avaldada, kas vale oli e-post või parool.

### 12.2. Autoriseerimine

- Avalikud endpoint'id peavad olema selgelt lubatud.
- Kõik muud endpoint'id peavad vaikimisi nõudma autentimist.
- Moderaatori ja administraatori toiminguid kaitstakse backendis.
- URL-i nimi ei ole turvameede.
- Vajadusel kasuta meetoditaseme autoriseerimist.

### 12.3. Andmete kaitse

Ära logi ega väljasta:

- avateksti paroole;
- parooliräsi;
- JWT token'eid;
- Authorization päise väärtusi;
- täielikke vabatekstilisi kirjeldusi logides;
- kasutaja e-posti avalikus hoiatuses või sünkroonimisvastuses.

Kõik vabatekstiväljad tuleb käsitleda tavatekstina. Neid ei tohi renderdada usaldatud HTML-ina.

---

## 13. Logimine

Logimine on kohustuslik ja konfigureeritav.

### 13.1. Raamistik

- Kasuta SLF4J API-t.
- Rakenduse logimise implementatsioon on Log4j2.
- Väldi samaaegselt mitme logimise implementatsiooni kaasamist.
- Logitasemed peavad olema konfiguratsioonist muudetavad.

### 13.2. Mida logida

INFO tasemel võib logida:

- kasutaja konto loomise fakti ilma paroolita;
- raporti loomise id-d ja seoste tehnilisi id-sid;
- hoiatuse loomise;
- hoiatuse kategooria muutmise;
- hoiatuse aegumise;
- vaidlustuse esitamise;
- vaidlustuse otsuse;
- perioodilise töö alguse, lõpu ja töödeldud kirjete arvu;
- sünkroonimisel tagastatud kirjete arvu.

WARN tasemel võib logida:

- ärilisi konflikte;
- korduva vaidlustuse katset;
- kehtetu või mitteaktiivse kategooria kasutamise katset;
- turvareeglite järgi ebatavalisi, kuid käsitletud olukordi.

ERROR tasemel logitakse ootamatud tehnilised vead koos stack trace'iga.

### 13.3. Mida mitte logida

- paroole;
- JWT-sid;
- autentimispäiseid;
- raporti täielikku kirjeldust;
- vaidlustuse täielikku põhjendust;
- rohkem isikuandmeid, kui vea uurimiseks vaja.

Ära koosta logisõnumeid stringide liitmisega. Kasuta parametriseeritud logimist.

---

## 14. Testimisstrateegia

Järgi testimispüramiidi:

- palju üksusteste;
- mõõdukalt integratsiooniteste;
- paar E2E testi.

### 14.1. Üksustestid

Üksustestid peavad eelkõige katma äriloogikat ilma terve Springi konteksti käivitamata.

Olulised testitavad juhtumid:

- üks kasutaja ei täida kahe kasutaja lävendit;
- kaks erinevat kasutajat täidavad lävendi;
- sama kasutaja korduvad raportid annavad ühe arvestatava raporti;
- mitteaktiivset kategooriat ei saa kasutada;
- domineeriv kategooria muutub ainult rangelt suurema arvu korral;
- viigi korral säilib olemasolev kategooria;
- aegumise piirkuupäev arvutatakse korrektselt;
- USER ei saa vaidlustust menetleda;
- rahuldatud vaidlustus võtab hoiatuse tagasi;
- tagasilükatud vaidlustus jätab hoiatuse aktiivseks;
- telefoninumber normaliseeritakse korrektselt;
- kehtetu telefoninumber lükatakse tagasi;
- sünkroonimisvahemiku piirid on korrektsed.

### 14.2. Integratsioonitestid

Integratsioonitestides kasuta PostgreSQL Testcontainersit.

Testi vähemalt:

- Flyway migratsioonide rakendumist;
- JPA seoseid;
- repositooriumide keerukamaid päringuid;
- unikaalseid piiranguid;
- osalist unikaalset indeksit aktiivsele hoiatusele;
- avatud vaidlustuse unikaalsust;
- Spring Security 401 ja 403 käitumist;
- transaktsioonide rollback'i;
- sünkroonimise updatedAt päringut;
- aegunud ja tagasivõetud hoiatuste jõudmist sünkroonimisvastuses.

### 14.3. E2E testid

E2E testid käivitavad kogu rakenduse juhuslikul pordil ja kasutavad päris PostgreSQL Testcontainerit.

Minimaalne E2E voog 1:

1. Registreeri kasutaja A.
2. Registreeri kasutaja B.
3. Logi sisse kasutajana A.
4. Esita raport.
5. Kontrolli, et hoiatust veel ei ole.
6. Logi sisse kasutajana B.
7. Esita sama numbri kohta raport.
8. Kontrolli, et hoiatus loodi.
9. Kontrolli, et hoiatus tuleb esimese sünkroonimise vastuses.

Minimaalne E2E voog 2:

1. Loo aktiivne hoiatus.
2. Esita tavakasutajana vaidlustus.
3. Kontrolli, et tavakasutaja ei saa seda menetleda.
4. Logi sisse moderaatorina.
5. Rahulda vaidlustus.
6. Kontrolli, et hoiatus on WITHDRAWN.
7. Kontrolli, et muudatus tuleb järgneva sünkroonimise vastuses.

### 14.4. Testide kvaliteet

- Testi käitumist, mitte privaatmeetodite teostust.
- Testinimed peavad kirjeldama tingimust ja oodatud tulemust.
- Ära kasuta Thread.sleep'i ajapõhise loogika testimiseks.
- Ajapõhistes teenustes eelista süstitavat Clock objekti.
- Ära muuda testide läbimiseks tootmiskoodi turvareegleid nõrgemaks.
- Iga parandatud vea juurde lisa regressioonitest.

---

## 15. Dokumentatsioon

### 15.1. JavaDoc

Dokumenteeri vähemalt avalikud meetodid, välja arvatud ilmselged getter'id ja setter'id.

JavaDoc peab kirjeldama:

- meetodi eesmärki;
- olulisi parameetreid;
- tagastatavat väärtust;
- olulisi domeenierandeid;
- ebaselgeid kõrvalmõjusid.

Ära lisa sisutühja JavaDoc'i, mis kordab ainult meetodi nime.

### 15.2. Swagger/OpenAPI

Dokumenteeri:

- endpoint'i eesmärk;
- autentimise vajadus;
- nõutavad rollid;
- request body;
- peamised vastused;
- tavalisemad veastaatused.

Swagger UI kaudu peab saama demonstreerida registreerimise, sisselogimise, raporti esitamise, hoiatuse vaatamise, sünkroonimise ja vaidlustuse põhivood.

### 15.3. README

README peab sisaldama vähemalt:

- projekti kirjeldust;
- põhifunktsioone;
- tehnoloogiaid;
- arhitektuuri lühikirjeldust;
- eeltingimusi;
- PostgreSQL-i käivitamist;
- rakenduse käivitamist Gradle Wrapperiga;
- testide käivitamist;
- keskkonnamuutujaid;
- Flyway kasutamist;
- Swagger UI asukohta;
- kasutajarolle;
- näidiskasutaja või algandmete loomise juhiseid;
- teadaolevaid piiranguid;
- sünkroonimise toimimise kirjeldust.

Kõik käsud peavad kasutama Gradle Wrapperit, mitte eeldama süsteemset Gradle'i paigaldust.

---

## 16. Gradle'i reeglid

- Kasuta ./gradlew käske.
- Ära lisa Maveniga seotud faile.
- Hoia sõltuvuste versioonid keskelt hallatavad.
- Eelista Spring Booti dependency management'i või version catalog'i, kui projekt seda juba kasutab.
- Ära lisa sõltuvust, kui sama tulemus on olemasoleva teegiga mõistlikult saavutatav.
- Iga uue sõltuvuse puhul kontrolli selle eesmärki, litsentsi ja vajalikkust.
- Testide tavakäsk peab töötama puhtas checkout'is.
- Soovitatud kontroll enne muudatuse lõpetamist on vähemalt clean test või projekti kokkulepitud check task.
- JaCoCo raport peab olema eraldi genereeritav või check protsessi osa vastavalt projekti konfiguratsioonile.

---

## 17. Konfiguratsioon ja keskkonnad

Kasuta eraldi konfiguratsiooni vähemalt kohaliku arenduse ja testide jaoks. Tootmislaadne konfiguratsioon peab lugema saladused keskkonnamuutujatest.

Repositooriumisse ei lisata:

- päris andmebaasiparooli;
- JWT saladust;
- tootmiskeskkonna ühendusandmeid;
- isiklikku .env faili;
- logifaile;
- build kausta.

Võib lisada .env.example faili koos näidisvõtmete ja ohutute placeholder'itega.

Hoiatuse lävend ja aegumisperiood võivad MVP-s olla konfiguratsioonis:

- warning threshold: 2;
- expiration period: 60 päeva;
- expiration job: kord 24 tunni jooksul.

Administraatori dünaamiline seadistusliides ei ole MVP-s kohustuslik.

---

## 18. Koodistiil ja kvaliteet

- Kasuta selgeid ingliskeelseid klassi-, meetodi- ja väljanimesid.
- Kasutajale kuvatavad tekstid võivad olla projekti valitud keeles.
- Väldi hiiglaslikke service-klassi meetodeid.
- Erista keerukas hoiatuse arvutamine vajadusel eraldi domeeniteenuseks.
- Eelista constructor injection'it.
- Väldi field injection'it.
- Ära kasuta Optional'i entity väljana.
- Ära kasuta Lombokit automaatselt, kui projektis pole seda juba teadlikult valitud.
- Ära avalda entity seoseid otse JSON-is.
- Kasuta enum'e fikseeritud staatuste ja rollide jaoks.
- Kasuta Instant tüüpi tehniliste ajatemplite jaoks.
- Kasuta LocalDate ja LocalTime tüüpe juhtumi kohaliku kuupäeva ja kellaaja jaoks.
- Väldi süsteemiaja otsest kutsumist keerukalt testitavas äriloogikas; eelista Clock'i.
- Meetod peab tegema ühte selgelt kirjeldatavat asja.
- Kommentaar peab selgitama põhjust, mitte kordama koodi.
- Ära jäta kommenteeritud väljalülitatud koodi repositooriumisse.

---

## 19. AI-agendi töökorraldus

### 19.1. Enne muudatuse tegemist

Agent peab:

1. lugema AGENTS.md faili;
2. uurima olemasolevat paketistruktuuri;
3. kontrollima Gradle'i konfiguratsiooni;
4. kontrollima olemasolevaid migratsioone;
5. otsima sama funktsiooni olemasolevat teostust;
6. uurima olemasolevaid teste;
7. tuvastama, kas muudatus mõjutab API-t, skeemi, turvet või ärireegleid.

### 19.2. Muudatuse tegemisel

Agent peab:

- tegema võimalikult väikese ja eesmärgipärase muudatuse;
- hoidma kihtide vastutused lahus;
- lisama või uuendama migratsiooni, kui skeem muutub;
- lisama või uuendama teste;
- säilitama olemasoleva koodistiili;
- vältima kõrvalist refaktoreerimist;
- mitte lisama MVP-st välja jäetud funktsionaalsust;
- mitte muutma avalikku API-t vaikimisi;
- mitte nõrgendama turvareegleid testide lihtsustamiseks.

### 19.3. Pärast muudatust

Agent peab:

1. käivitama asjakohased üksustestid;
2. käivitama mõjutatud integratsioonitestid;
3. võimalusel käivitama kogu Gradle test või check töövoo;
4. kontrollima vormindust ja kompileerimist;
5. kontrollima, et migratsioonid töötavad tühja PostgreSQL andmebaasi peal;
6. uuendama Swaggerit või README-d, kui kasutusviis muutus;
7. kirjeldama lühidalt tehtud muudatust ja testitulemust.

Agent ei tohi väita, et testid läbisid, kui neid tegelikult ei käivitatud.

---

## 20. Keelatud või välditavad muudatused

Ilma selge ülesandeta ei tohi:

- muuta Gradle'it Maveniks;
- muuta PostgreSQL-i H2-ks;
- eemaldada Flywayd;
- lubada Hibernate ddl-auto create või update kasutamist tavakeskkonnas;
- eemaldada backendipoolset autoriseerimist;
- salvestada paroole avatekstina;
- logida token'eid või paroole;
- tagastada entity-objekte otse API-st;
- kustutada raporteid, hoiatusi või vaidlustuste ajalugu;
- muuta aegunud hoiatust uuesti aktiivseks;
- lisada mikroteenuseid;
- lisada Kafka, RabbitMQ või muud infrastruktuuri ilma kinnitatud vajaduseta;
- ehitada mobiilirakendust;
- lisada keerukat üldist raamistikku ühe lihtsa kasutusjuhu jaoks;
- muuta olemasolevat rakendatud Flyway migratsiooni;
- jätta skeemi muudatus migratsioonita;
- jätta ärireegli muudatus testita.

---

## 21. Valmiskriteeriumid

Funktsionaalsus on valmis ainult siis, kui:

- nõue on teostatud sobivas kihis;
- sisend valideeritakse;
- õigused kontrollitakse backendis;
- vajalik andmebaasipiirang on olemas;
- migratsioon on lisatud, kui skeem muutus;
- äriloogika üksustestid läbivad;
- vajalikud integratsioonitestid läbivad;
- API veavastused on kontrollitud;
- uut tundlikku infot ei logita;
- Swagger vastab endpoint'ile;
- README on uuendatud, kui käivitamine või kasutamine muutus;
- Gradle'i build või vähemalt kokkulepitud testitöövoog läbib.

---

## 22. MVP prioriteedid

Kui aeg on piiratud, järgi seda järjekorda:

1. projekti Gradle'i baas ja PostgreSQL;
2. Flyway migratsioonid;
3. kasutajad, autentimine ja rollid;
4. telefoninumbrid ja kategooriad;
5. raporti esitamine;
6. hoiatuse automaatne loomine;
7. domineeriva kategooria uuendamine;
8. aktiivsete hoiatuste vaatamine;
9. lihtne ajapõhine sünkroonimine;
10. vaidlustuse esitamine ja menetlemine;
11. perioodiline aegumine;
12. testide täiendamine;
13. Swagger, JavaDoc ja README viimistlemine.

Kui tähtaeg läheneb, kärbi esmalt mugavusfunktsioone, mitte kohustuslikku turvet, migratsioone, kihilist arhitektuuri, põhivoogude teste või dokumentatsiooni.

---

## 23. Soovituslik ajakulu piiratud MVP-le

Piiratud ja hästi juhitud MVP eeldatav maht on ligikaudu 60–85 tundi, sõltuvalt arendaja Springi kogemusest.

Ligikaudne jaotus:

- planeerimise lõpetamine: 4–6 tundi;
- Gradle, PostgreSQL, Flyway, Log4j2 ja Swagger: 6–9 tundi;
- autentimine ja rollid: 9–13 tundi;
- telefoninumbrid, kategooriad ja raportid: 8–11 tundi;
- hoiatuste äriloogika: 9–13 tundi;
- lihtne sünkroonimine: 3–5 tundi;
- vaidlustused ja aegumine: 7–10 tundi;
- testid: 9–14 tundi;
- README, JavaDoc ja lõppviimistlus: 5–7 tundi.

AI kasutamine võib vähendada korduvat kirjutamist ja dokumentatsiooni koostamist, kuid ei asenda ärireeglite otsustamist, turvakontrolli, testimist ega koodi mõistmist.

---

## 24. Lõplikud põhimõtted

- Hoia lahendus kursuseprojekti mahus.
- Eelista lihtsat ja testitavat lahendust.
- Ärireeglid kuuluvad teenusekihti.
- Andmete terviklikkus peab olema kaitstud ka andmebaasis.
- Kõik skeemimuudatused läbivad Flyway.
- Turvet ei rakendata ainult kasutajaliideses.
- Sünkroonimine põhineb warning.updatedAt väärtusel ja serveri antud syncTime'il.
- Ajalugu säilitatakse; hoiatusi, raporteid ja vaidlustusi ei kustutata tavavoo käigus.
- Kõige olulisemad ärireeglid peavad olema automatiseeritud testidega tõendatud.
- Agent peab jätma projekti pärast muudatust kompileeruvasse, testitavasse ja dokumenteeritud olekusse.
