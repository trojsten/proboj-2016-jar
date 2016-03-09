
Čo je proboj
------------

Proboj je počítačová hra, ktorej hráčmi nie ste vy, ale programy, čo napíšete.


Zdrojáky
--------

Štandardný hráč čiže klient (v adresári `klienti/template`) sa skladá z jediného
zdrojáku `main.cpp`. Ale môžte ho rozdeliť aj na viacero.

V serveri je tiež zopár zdrojákov, čo vás bude zaujímať.

- `common.h` obsahuje základné štruktúry, čo váš klient dostane k dispozícii.
- `update.h` obsahuje všetky herné konštanty, a tiež
  implementáciu väčšiny herných pravidiel, takže ak v pravidlách nie je niečo
  jasné, skúste sa tam pozrieť.
- v `main.cpp` sú tiež nejaké pravidlá (ako sa ťahá apod.), ale to je asi menej
  dôležité.

Kľudne si prečítajte aj ostatné zdrojáky, ja sa len poteším, ale pri kódení
vášho klienta vám asi nepomôžu.


Ako kódiť klienta
-----------------

Skopírujte obsah `klienti/template` do iného adresára a niečo v ňom nakóďte.

V koreni proboju spustite `make`, čím všetko skompilujete. (Ak váš klient nie je
vnútri `klienti`, nastavte v jeho `Makefile` správny `SERVERDIR` a spustite
`make` aj v ňom.)

Potom spustite `./server/server zaznamy/01 mapy/mapa1.map asdf klienti/vasklient
klienti/vasklient klienti/hlupy` To spustí hru s troma hráčmi (vaším, druhým
vaším a hlúpym) a uloží záznam do `zaznamy/01`. Ten si môžete pozrieť s príkazom
`./server/observer.jar zaznamy/01`. Ak sa vam neda pustit observer hentak, skuste 
`java -classpath observer Observer zaznamy/01`.

Keď server spustíte u vás, je to len na skúšku. Na hlavnom počítači to beží na
ostro. Je tam aj webové rozhranie, cez ktoré môžete uploadovať vašich klientov.
Uploadujú sa zdrojáky v .gz (vypľuté vaším make-om) a tie sa potom skompilujú
(konkrétne sa spustí `make naserveri SERVERDIR=/adresar/kde/je/server`).


O spúštaní servera
------------------

Asi ste si všimli zvláštne "asdf" pri spúšťaní servera. Ak namiesto "asdf" napíšete
isté špeciálne slová, stanú sa zaujímavé veci. Konkrétne

- "live": Spolu so serverom spustí observera, ktorým je možné sledovať zápas naživo.
Takéto pozeranie zápasu je ale pomalšie, ako keby ste len pozerali záznam.
- "play": Môžete si zahrať proti počítaču. Konkrétne proti hráčom, ktorých ste
určili poslednou sadou argumentov pri spúšťaní servera.


Aký je proboj
-------------

V tomto proboji budete rozširovať svoj vplyv aj do ostatných kútov sveta.
#TODO
Bude sa hrať virus wars.

Pravidlá hry
------------

Máme mapku. A v nej sú na nejakých pozíciách mestá, každé pod kontrolou niektorého
hráča, pričom je jeden špeciálny typ hráča "neutrál".

Každé mesto má svoje parametre -- aktuálna populácia, kapacita, populačný prírastok
za jednotku času, útok, obrana a stena. Každú jednotku času sa populácia mesta
zvýši o prírastok, pričom nemôže presiahnuť kapacitu mesta. Toto neplatí pre
neutrálne mestá.

Hocikedy sa hráč môže rozhodnúť vyslať z niektorého mesta, ktoré kontroluje, ľubovoľný
počet jednotiek do ľubovoľného iného mesta. Keď vyslané jednotky dôjdu do cieľa,
môžu sa udiať dve rôzne veci:

- Ak je cieľ pod kontrolou toho istého hráča, vyslané jednotky sa pridajú k populácii
cieľového meste. Jednotky, ktoré sa do mesta nezmestia, umrú.
- Ak je cieľ pod kontrolou iného hráča, odohrá sa bitka. Bitka prebieha nasledovne:
  - Najprv sa určí obranná sila brániaceho sa mesta ako $populacia \cdot obrana + stena$.
  - Tiež sa určí útočná sila útočníkov ako $pocet\ jednotiek \cdot (utok\ zdrojoveho\ mesta)$.
  - Kým sú obe sily väčšie ako 0, náhodne sa pre rozhodne, ktorá sa zníži o 1.
  - Podľa toho, ktorá sila je na konci kladná, sa nastaví vlastník mesta. (Ak sú obe
nulové, vlastníkom mesta sa stáva neutrál.) Podľa toho, aká časť sily víťazovi
ostala, sa nastaví aj nová populácia mesta. (Presný vzorec nájdete niekde v update.cpp,
prípadne sa spýtajte.)

Cieľom hry je prežiť dlhšie, ako ostatní (normálni, teda nie neutrálni) hráči.


Ako sa ťahá
-----------

Hrá sa real time. To znamená, že keď pošlete ťah, tak sa ťah urobí. To znamená, že môžete vynechať nejaké kolo.

Čo to presne znamená? Klient si môže v ľubovoľnom momente vyžiadať od servera informácie o hre,
a server mu ich pošle najskôr ako vie. Server ich vie posielať dvomi spôsobmi: buď vám
pošle celý stav hry, alebo zmeny v stave hry. Váš klient ho vie požiadať o konkrétny
spôsob prenosu.

To popri inom znamená, že ak sa vám veľmi chce, môžete spraviť nejakého "event-driven"
bota -- teda by sa vedel rozhodovať podľa nových udalostí. Ak by ste teda chceli nejakého
takého bota nakódiť, odporúčam pozrieť sa do server/marshal.cpp/dekodujStav .


Mapy
----

Mapy sú úplne textové a majú príponu ".map" . Môžete si vytvárať vlastné mapy pomocou
nástroja "Leveler" (spustíte ho z probojového adresára ako "leveler/leveler.jar <nazov_mapy>").
V README.txt sú nejaké stručné pokyny, ako ho používať.

Ak ju nerobite rucne a chcete ju robit, chodte za bujom


