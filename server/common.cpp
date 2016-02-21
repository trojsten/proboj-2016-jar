#include <math.h>
#include <iostream>

using namespace std;

#include "common.h"

int velkyCas;


string bod::nazovtyp () {
	return "bod";
}
string bunka::nazovtyp () {
	return "bunka";
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


bunka::bunka () {}

int bunka::zistiPop () {
	if (vlastnik == -1) { // neobsadene bunky nerastu
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

int bunka::def () {
	return obrana*zistiPop() + stena;
}


invazia::invazia () {}

invazia::invazia (int _odchod, int _prichod, int _vlastnik, bunka* _od, bunka* _kam, int _jednotiek)
	: odchod(_odchod), prichod(_prichod), vlastnik(_vlastnik), od(_od), kam(_kam), jednotiek(_jednotiek) {}

int invazia::atk () {
	return jednotiek * od->utok;
}

int invazia::def () {
	return kam->def();
}


invAlt::invAlt () {}

invAlt::invAlt (int _odchod, int _prichod, int _vlastnik, int _od, int _kam, int _jednotiek)
	: odchod(_odchod), prichod(_prichod), vlastnik(_vlastnik), od(_od), kam(_kam), jednotiek(_jednotiek) {}

invAlt::invAlt (invazia inv) {
	invAlt(inv.odchod, inv.prichod, inv.vlastnik, inv.od->id, inv.kam->id, inv.jednotiek);
}


bool compBunkaPtr::operator() (const bunka* a, const bunka* b) const {
	return a->id < b->id;
}


stav::stav () {
	nastavCas(0);
}

stav::stav (stavAlt& S) {
	nastavCas(S.cas);
	for (unsigned i=0; i<S.cely.size(); i++) {
		cely.push_back(S.cely[i]);
	}
	urciVlastnictvo();
	for (unsigned i=0; i<S.invZoznam.size(); i++) {
		novaInv(S.invZoznam[i]);
	}
}

void stav::urciVlastnictvo () {
	for (unsigned i=0; i<cely.size(); i++) {
		int vlastnik = cely[i].vlastnik;
		if (vlastnik < 0) {
			continue;
		}
		while ((int)vlastnim.size() <= vlastnik) {
			vlastnim.push_back(set<bunka*, compBunkaPtr>());
		}
		vlastnim[vlastnik].insert(&cely[i]);
	}
}

void stav::nastavBunku (int id, int vlastnik, int populacia) {
	int old = cely[id].vlastnik;
	if (old >= 0) {
		vlastnim[old].erase(&cely[id]);
	}
	cely[id].vlastnik = vlastnik;
	if (vlastnik >= 0) {
		while ((int)vlastnim.size() <= vlastnik) {
			vlastnim.push_back(set<bunka*, compBunkaPtr>());
		}
		vlastnim[vlastnik].insert(&cely[id]);
	}
	
	cely[id].populacia = populacia;
	cely[id].poslCas = velkyCas;
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

void stav::novaInv (int prichod, int od, int kam, int jednotiek) {
	int diff = prichod - cas;
	while ((int)invPodlaCasu.size() <= diff) {
		invPodlaCasu.push_back(vector<invazia*>(0));
	}
	invazia* ptr = new invazia;
	*ptr = invazia(cas, prichod, cely[od].vlastnik, &cely[od], &cely[kam], jednotiek);
	invPodlaCasu[diff].push_back(ptr);
}

void stav::novaInv (invAlt inva) {
	novaInv(inva.prichod, inva.od, inva.kam, inva.jednotiek);
}

int stav::vyherca () { // -1 == hra este bezi, -2 == nikto
	int kto = -2;
	for (unsigned i=0; i<vlastnim.size(); i++) {
		if (vlastnim[i].empty()) {
			continue;
		}
		if (kto != -2) {
			return -1;
		}
		kto = i;
	}
	return kto;
}


stavAlt::stavAlt () : cas(0) {}

stavAlt::stavAlt (stav& S) {
	cas = S.cas;
	for (unsigned i=0; i<S.cely.size(); i++) {
		cely.push_back(S.cely[i]);
	}
	for (unsigned t=0; t<S.invPodlaCasu.size(); t++) {
		for (unsigned i=0; i<S.invPodlaCasu[t].size(); i++) {
			invazia inv = *S.invPodlaCasu[t][i];
			invZoznam.push_back(invAlt(inv));
		}
	}
}
