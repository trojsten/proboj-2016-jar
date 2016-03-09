#include <math.h>
#include <iostream>

using namespace std;

#include "common.h"
#include "update.h"

int velkyCas;


string bod::nazovtyp () {
	return "bod";
}
string mesto::nazovtyp () {
	return "mesto";
}
string invazia::nazovtyp () {
	return "invazia";
}
string invAlt::nazovtyp () {
	return "invAlt";
}
string stav::nazovtyp () {
	return "stav";
}
string stavAlt::nazovtyp () {
	return "stavAlt";
}


bod::bod () : x(0), y(0) {}

bod::bod (int a,int b) : x(a), y(b) {}

bod bod::operator- (const bod A) const {
	return bod(x-A.x, y-A.y);
}

double bod::dist () const {
	return sqrt((double)x*x + (double)y*y);
}

int bod::casCestovania () const {
	return int(ceil(dist()/RYCHLOST_JEDNOTIEK) );
}

mesto::mesto () {}

int mesto::zistiPop () {
	if (vlastnik < 0) {
		poslCas = velkyCas;
		return populacia;
	}
	populacia += ((int)(velkyCas/POMALOST_RASTU) - (int)(poslCas/POMALOST_RASTU))*rast;
	if (populacia > kapacita) {
		populacia = kapacita;
	}
	if (populacia < 0) {
		populacia = 0;
	}
	poslCas = velkyCas;
	return populacia;
}

int mesto::def () {
	return obrana*zistiPop() + stena;
}


invazia::invazia () {}

invazia::invazia (int odch, int prich, int vlast, mesto* odkial, mesto* kamze, int jedn)
	: odchod(odch), prichod(prich), vlastnik(vlast), od(odkial), kam(kamze), jednotiek(jedn) {}

int invazia::atk () {
	int utok = (vlastnik < 0 ? UTOK_TEMNYCH : od->utok);
	return jednotiek * utok;
}

int invazia::def () {
	return kam->def();
}


invAlt::invAlt () {}

invAlt::invAlt (int odch, int prich, int vlast, int odkial, int kamze, int jedn)
	: odchod(odch), prichod(prich), vlastnik(vlast), od(odkial), kam(kamze), jednotiek(jedn) {}

invAlt::invAlt (invazia inv) {
	odchod = inv.odchod;
	prichod = inv.prichod;
	vlastnik = inv.vlastnik;
	od = (inv.od == NULL ? -2 : inv.od->id);
	kam = inv.kam->id;
	jednotiek = inv.jednotiek;
}


stav::stav () {
	nastavCas(0);
}

stav::stav (stavAlt& S) {
	cas = S.cas;
	for (unsigned i=0; i<S.mesta.size(); i++) {
		mesta.push_back(S.mesta[i]);
	}
	for (unsigned i=0; i<S.invZoznam.size(); i++) {
		nastavInv(S.invZoznam[i]);
	}
}

void stav::nastavMesto (int id, int vlastnik, int populacia) {
	mesta[id].vlastnik = vlastnik;
	mesta[id].populacia = populacia;
	mesta[id].poslCas = velkyCas;
}

void stav::nastavCas (int t) {
	int diff = t - cas;
	for (int i=0; i<diff && !invPodlaCasu.empty(); i++) {
		for (unsigned j=0; j<invPodlaCasu[0].size(); j++) {
			invazia* ptr = invPodlaCasu[0][j];
			delete ptr;
		}
		invPodlaCasu.pop_front();
	}
	if (invPodlaCasu.empty()) { // aby nebolo prazdne a nedostavali decka EXC za pristupovanie ku front
		invPodlaCasu.push_back(vector<invazia*>());
	}
	cas = t;
	velkyCas = t;
}

bool stav::nastavInv (int odchod, int prichod, int vlastnik, int od, int kam, int jednotiek) {
	// par vojakov vykrocilo lavou nohou...
	jednotiek = jednotiek*VYTRVALOST_VOJAKOV / 100;
	jednotiek += (rand()%100 < (jednotiek*VYTRVALOST_VOJAKOV)%100);
	if (jednotiek == 0) {
		return false;
	}

	int diff = prichod - cas;
	while ((int)invPodlaCasu.size() <= diff) {
		invPodlaCasu.push_back(vector<invazia*>());
	}
	mesto* odkial = (od < 0 ? NULL : &mesta[od]);
	invazia* ptr = new invazia(odchod, prichod, vlastnik, odkial, &mesta[kam], jednotiek);
	invPodlaCasu[diff].push_back(ptr);
	return true;
}
bool stav::nastavInv (invAlt inva) {
	nastavInv(inva.odchod, inva.prichod, inva.vlastnik, inva.od, inva.kam, inva.jednotiek);
}


stavAlt::stavAlt () : cas(0) {}

stavAlt::stavAlt (stav& S) {
	cas = S.cas;
	for (unsigned i=0; i<S.mesta.size(); i++) {
		mesta.push_back(S.mesta[i]);
	}
	for (unsigned t=0; t<S.invPodlaCasu.size(); t++) {
		for (unsigned i=0; i<S.invPodlaCasu[t].size(); i++) {
			invazia inv = *(S.invPodlaCasu[t][i]);
			invZoznam.push_back(invAlt(inv));
		}
	}
}
