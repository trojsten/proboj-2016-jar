
using namespace std;

#include <vector>
#include <sstream>
#include <algorithm>
#include <math.h>

#include "common.h"
#include "update.h"
#include "marshal.h"

// cast 1 --- uchovavanie si info o hre pre rychly retrieval a rychle rozhodovanie, ci hra uz skoncila
// a pomocne funkcie, existencia tohto je sposobena tym, ze nechceme clutterovat common vecami iba
// pre server --- ten obsahuje veci vyuzivane aj klientom

struct staty {
	int cas;
	int zostHracov;
	vector<int> casUmrtia; // v [t,t+1) umrel
	vector<int> pocZivych;

	void add (int vlastnik, int val) {
		if (vlastnik<0 || vlastnik>=(int)pocZivych.size()) {
			return;
		}
		pocZivych[vlastnik] += val;
		if (pocZivych[vlastnik] == 0) {
			casUmrtia[vlastnik] = cas;
			zostHracov--;
		}
		else
		if (pocZivych[vlastnik] == val) {
			casUmrtia[vlastnik] = -1;
			zostHracov++;
		}
	}

	staty () {}
	staty (unsigned pocHrac, stav& stavHry) {
		cas = 0;
		zostHracov = 0;
		for (unsigned i=0; i<pocHrac; i++) {
			casUmrtia.push_back(0);
			pocZivych.push_back(0);
		}
		for (unsigned i=0; i<stavHry.mesta.size(); i++) {
			int vlastnik = stavHry.mesta[i].vlastnik;
			add(vlastnik, 1);
		}
		for (unsigned t=0; t<stavHry.invPodlaCasu.size(); t++) {
			for (unsigned i=0; i<stavHry.invPodlaCasu[t].size(); i++) {
				add(stavHry.invPodlaCasu[t][i]->vlastnik, 1);
			}
		}
	}

	bool koniec () {
		/*
		for (unsigned i=0; i<pocZivych.size(); i++) {
			cout << pocZivych[i] << "," << pocInv[i] << "  ";
		}
		cout << "\n";
		*/
		return (zostHracov <= 1);
	}
};

staty stats;

void inicializujStaty (unsigned pocHrac,stav& stavHry) {
	stats = staty(pocHrac,stavHry);
}

void nastavMesto (int id, int vlastnik, int populacia, stav& stavHry) {
	if (populacia == 0) {
		vlastnik = -1;
	}
	stats.add(stavHry.mesta[id].vlastnik, -1);
	stavHry.nastavMesto(id, vlastnik, populacia);
	stats.add(vlastnik, 1);
}

void novaInv (invAlt inva, stav& stavHry) {
	stats.add(inva.vlastnik, 1);
	stavHry.nastavInv(inva);
}

void advanceCas (stav& stavHry) {
	if (stavHry.invPodlaCasu.size() > 0) {
		for (unsigned i=0; i<stavHry.invPodlaCasu[0].size(); i++) {
			stats.add(stavHry.invPodlaCasu[0][i]->vlastnik, -1);
		}
	}
	stats.cas++;
	stavHry.nastavCas(stavHry.cas + 1);
}

/////////////////////////////////////////////////////////////////////
// cast 2 --- simulovanie hry ///////////////////////////////////////
/////////////////////////////////////////////////////////////////////

bool oprav (invAlt& inva, int hrac, stav& stavHry) {
	if (inva.od<0 || inva.od>=(int)stavHry.mesta.size()) {
		return false;
	}
	if (inva.kam<0 || inva.kam>=(int)stavHry.mesta.size()) {
		return false;
	}
	if (inva.od == inva.kam) {
		return false;
	}
	if (stavHry.mesta[inva.od].vlastnik != hrac) {
		return false;
	}
	
	int popcap = stavHry.mesta[inva.od].zistiPop();
	if (inva.jednotiek > popcap) {
		inva.jednotiek = popcap;
	}
	if (inva.jednotiek <= 0) {
		return false;
	}
	
	bod smer = stavHry.mesta[inva.kam].pozicia - stavHry.mesta[inva.od].pozicia;
	inva.prichod = stavHry.cas + int(ceil(smer.dist()) );
	inva.odchod = stavHry.cas;
	inva.vlastnik = hrac;
	return true;
}

void vykonaj (invazia inv, stav& stavHry) {
	int kto[2] = {inv.vlastnik, inv.kam->vlastnik};
	if (kto[0] == kto[1]) {
		nastavMesto(inv.kam->id, kto[0], inv.kam->zistiPop() + inv.jednotiek, stavHry);
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
		nastavMesto(inv.kam->id, kto[i], zost[i], stavHry);
		return ;
	}
	nastavMesto(inv.kam->id, -1, 0, stavHry);
}

bool odsimulujKolo (stav& stavHry, const vector<string>& odpovede, stringstream& pokrac) {
	vector<bool> zmenene;
	for (unsigned i=0; i<stavHry.mesta.size(); i++) {
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
					novaInv(inva,stavHry);
					{
						int vlastnikOd = stavHry.mesta[inva.od].vlastnik;
						int npop = stavHry.mesta[inva.od].zistiPop() - inva.jednotiek;
						nastavMesto(inva.od, vlastnikOd, npop, stavHry);
					}
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
	
	// info o zmenenych mestach
	for (unsigned i=0; i<zmenene.size(); i++) {
		if (!zmenene[i]) {
			continue;
		}
		koduj(pokrac,stavHry.mesta[i]);
	}
	
	// casova zmena
	advanceCas(stavHry);
	pokrac << "cas " << stavHry.cas << "\n";
	
	return stats.koniec();
}
