
using namespace std;

#include <sstream>
#include <algorithm>
#include <math.h>

#include "common.h"
#include "update.h"
#include "marshal.h"


bool oprav (invAlt& inva, int hrac, stav& stavHry) {
	if (inva.utocnik<0 || inva.utocnik>=(int)stavHry.cely.size()) {
		return false;
	}
	if (inva.obranca<0 || inva.obranca>=(int)stavHry.cely.size()) {
		return false;
	}
	if (inva.utocnik == inva.obranca) {
		return false;
	}
	if (stavHry.cely[inva.utocnik].vlastnik != hrac) {
		return false;
	}
	if (inva.jednotiek < 0) {
		return false;
	}
	int popcap = stavHry.cely[inva.utocnik].zistiPop();
	if (inva.jednotiek > stavHry.cely[inva.utocnik].zistiPop()) {
		inva.jednotiek = popcap;
	}
	stavHry.nastavBunku(inva.utocnik, (inva.jednotiek == popcap ? -1 : hrac), popcap - inva.jednotiek);
	
	bod smer = stavHry.cely[inva.utocnik].pozicia - stavHry.cely[inva.obranca].pozicia;
	int prichod = stavHry.cas + int(ceil(smer.dist()) );
	inva.prichod = prichod;
	
	return true;
}

void vykonaj (invazia inv, stav& stavHry) {
	int kto[2] = {inv.utocnik->vlastnik, inv.obranca->vlastnik};
	if (kto[0] == kto[1]) {
		stavHry.nastavBunku(inv.obranca->id, kto[0], inv.obranca->zistiPop() + inv.jednotiek);
		return;
	}
	int povjedn[2] = {inv.jednotiek, inv.obranca->zistiPop()};
	int povpow[2] = {inv.atk(), inv.def()};
	int pow[2];
	for (int i=0; i<2; i++) {
		if (povpow[i]<0) {
			povpow[i] = 0;
		}
		pow[i] = povpow[i];
	}
	int nenulovych = 0;
	for (int i=0; i<2; i++) {
		nenulovych += (pow[i]>0);
	}
	while (nenulovych > 1) {
		int fight = 1 + rand()%3;
		for (int i=0; i<2; i++) {
			pow[i] -= fight%2;
			fight /= 2;
			nenulovych -= (pow[i]==0);
		}
	}
	int zost[2];
	for (int i=0; i<2; i++) {
		if (povpow[i] == 0) {
			zost[i] = 0;
			continue;
		}
		zost[i] = povjedn[i]*pow[i] / povpow[i];
		int zvys = (povjedn[i]*pow[i]) % povpow[i];
		zost[i] += (rand()%povpow[i] < zvys);
	}
	for (int i=0; i<2; i++) {
		if (zost[i]==0) {
			continue;
		}
		stavHry.nastavBunku(inv.obranca->id, kto[i], zost[i]);
		return ;
	}
	stavHry.nastavBunku(inv.obranca->id, -1, 0);
}

void odsimulujKolo (stav& stavHry, const vector<string>& odpovede, stringstream& pokrac) {
	vector<bool> zmenene;
	for (unsigned i=0; i<stavHry.cely.size(); i++) {
		zmenene.push_back(false);
	}

	// spracuj nove prikazy
	for (unsigned i=0; i<odpovede.size(); i++) {
		stringstream ss(odpovede[i]);
		string prikaz;
		while (ss >> prikaz) {
			if (prikaz == "invazia") {
				invAlt inva;
				nacitaj(ss,inva);
				if (oprav(inva, i, stavHry)) {
					stavHry.novaInv(inva);
					koduj(pokrac,inva);
					zmenene[inva.utocnik] = true;
				}
			}
		}
	}

	// odsimuluj invazie, co prave dosli do ciela
	if (stavHry.invPodlaCasu.size() > 0) {
		vector<invazia*>* invy = &stavHry.invPodlaCasu[0];
		random_shuffle(invy->begin(), invy->end());
		for (unsigned i=0; i<invy->size(); i++) {
			invazia* ptr = (*invy)[i];
			vykonaj(*ptr, stavHry);
			zmenene[ptr->obranca->id] = true;
		}
	}
	
	// info o zmenenych bunkach
	for (unsigned i=0; i<zmenene.size(); i++) {
		if (!zmenene[i]) {
			continue;
		}
		koduj(pokrac,stavHry.cely[i]);
	}

	// casova zmena
	stavHry.nastavCas(stavHry.cas + 1);
	pokrac << "cas " << stavHry.cas << "\n";
}
