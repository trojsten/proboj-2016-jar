
using namespace std;

#include <sstream>
#include <algorithm>
#include <math.h>

#include "common.h"
#include "update.h"
#include "marshal.h"


bool oprav (invAlt& inva, int hrac, stav& stavHry) {
	if (inva.od<0 || inva.od>=(int)stavHry.cely.size()) {
		return false;
	}
	if (inva.kam<0 || inva.kam>=(int)stavHry.cely.size()) {
		return false;
	}
	if (inva.od == inva.kam) {
		return false;
	}
	if (stavHry.cely[inva.od].vlastnik != hrac) {
		return false;
	}
	if (inva.jednotiek < 0) {
		return false;
	}
	int popcap = stavHry.cely[inva.od].zistiPop();
	if (inva.jednotiek > stavHry.cely[inva.od].zistiPop()) {
		inva.jednotiek = popcap;
	}
	stavHry.nastavBunku(inva.od, (inva.jednotiek == popcap ? -1 : hrac), popcap - inva.jednotiek);
	
	bod smer = stavHry.cely[inva.kam].pozicia - stavHry.cely[inva.od].pozicia;
	inva.prichod = stavHry.cas + int(ceil(smer.dist()) );
	
	/* // toto v skutocnosti netreba opravovat --- stav si to sam urci
	inva.odchod = stavHry.cas;
	inva.vlastnik = hrac;
	*/
	
	return true;
}

void vykonaj (invazia inv, stav& stavHry) {
	int kto[2] = {inv.vlastnik, inv.kam->vlastnik};
	if (kto[0] == kto[1]) {
		stavHry.nastavBunku(inv.kam->id, kto[0], inv.kam->zistiPop() + inv.jednotiek);
		return;
	}
	int povjedn[2] = {inv.jednotiek, inv.kam->zistiPop()};
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
		stavHry.nastavBunku(inv.kam->id, kto[i], zost[i]);
		return ;
	}
	stavHry.nastavBunku(inv.kam->id, -1, 0);
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
					zmenene[inva.od] = true;
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
			zmenene[ptr->kam->id] = true;
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
