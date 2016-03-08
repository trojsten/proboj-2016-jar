#include <math.h>
#include <iostream>

using namespace std;

#include "common.h"

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


mesto::mesto () {}

int mesto::zistiPop () {
	if (vlastnik == -1) {
		poslCas = velkyCas;
		return populacia;
	}
	populacia += (velkyCas - poslCas)*rast;
	if (populacia > kapacita) {
		populacia = kapacita;
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
	return jednotiek * od->utok;
}

int invazia::def () {
	return kam->def();
}


invAlt::invAlt () {}

invAlt::invAlt (int odch, int prich, int vlast, int odkial, int kamze, int jedn)
	: odchod(odch), prichod(prich), vlastnik(vlast), od(odkial), kam(kamze), jednotiek(jedn) {}

invAlt::invAlt (invazia inv)
	: invAlt(inv.odchod, inv.prichod, inv.vlastnik, inv.od->id, inv.kam->id, inv.jednotiek) {}


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
	cas = t;
	velkyCas = t;
}

void stav::nastavInv (int odchod, int prichod, int vlastnik, int od, int kam, int jednotiek) {
	int diff = prichod - cas;
	while ((int)invPodlaCasu.size() <= diff) {
		invPodlaCasu.push_back(vector<invazia*>());
	}
	invazia* ptr = new invazia(odchod, prichod, vlastnik, &mesta[od], &mesta[kam], jednotiek);
	invPodlaCasu[diff].push_back(ptr);
}
void stav::nastavInv (invAlt inva) {
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
