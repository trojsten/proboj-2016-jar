#include <vector>
#include <fstream>
#include <iostream>
#include <set>
#include <sstream>
#include <unistd.h>
#include <sys/stat.h>
#include <string.h>

using namespace std;

#include "util.h"
#include "common.h"
#include "update.h"
#include "klient.h"
#include "mapa.h"
#include "marshal.h"

#define MAX_CITAJ 1024
#define TAH_CAS 10 // v milisekundach


vector<Klient> klienti;
vector<unsigned> kposy;
vector<string> kodpovede;

string historia;


// tato trapna funkcia existuje len kvoli inicializujSignaly()
void zabiKlientov() {
	loguj("ukoncujem klientov");
	for (unsigned i=0; i<klienti.size(); i++) {
		klienti[i].zabi();
	}
}

void odpovedaj (unsigned k, stringstream& ss, string& resetAns) {
	string riadok;
	while (getline(ss, riadok)) {
		if (riadok == "changeDesc") {
			string ans = historia.substr(kposy[k], historia.size());
			klienti[k].posli(ans + "end\n");
			kposy[k] = historia.size();
		}
		else
		if (riadok == "stateDesc") {
			klienti[k].posli(resetAns + "end\n");
			kposy[k] = historia.size();
		}
		else
		if (riadok == "desc") {
			string ans = historia.substr(kposy[k], historia.size());
			if (ans.size() < resetAns.size()) {
				klienti[k].posli(ans + "end\n");
			}
			else {
				klienti[k].posli(resetAns + "end\n");
			}
			kposy[k] = historia.size();
		}
		else {
			kodpovede[k] += riadok + "\n";
		}
	}
}

int main(int argc, char *argv[]) {
	if (argc < 4) {
    fprintf(stderr, "usage: %s <zaznamovy-adresar> <mapa> {<adresare-klientov>...}\n", argv[0]);
    return 1;
  }
  
	unsigned int seed = time(NULL) * getpid();
  srand(seed);
  loguj("startujem server, seed je %u", seed);
  inicializujSignaly(zabiKlientov);

	string zaznAdr(argv[1]);
	if (!jeAdresar(zaznAdr)) {
    if (mkdir(zaznAdr.c_str(), 0777)) {
      fprintf(stderr, "main/mkdir: %s: %s\n", zaznAdr.c_str(), strerror(errno));
      exit(EXIT_FAILURE);
    }
  }
  else {
		fprintf(stderr, "main: prepisujem zaznamovy adresar: %s\n", zaznAdr.c_str());
	}
	string obsubor = zaznAdr+"/observation";
	fstream observationstream(obsubor.c_str(), fstream::out | fstream::trunc);
	if (observationstream.fail()) {
		fprintf(stderr, "main/observationstream: neviem (o|vy)tvorit subor: %s\n", obsubor.c_str());
		exit(EXIT_FAILURE);
	}

	set<string> uzMena;
	for (int i=3; i<argc; i++) {
		string klientAdr(argv[i]);
		// meno klienta je cast za poslednym /, za ktorym nieco je
		int j = (int)klientAdr.size() - 1;
		while (j>0 && klientAdr[j-1]!='/') {
			j--;
		}
		string meno = klientAdr.substr(j);
		while (uzMena.count(meno)) {
			meno += "+";
		}
		uzMena.insert(meno);
		klienti.push_back(Klient(meno, klientAdr, zaznAdr));
		klienti[i-3].restartuj();
		klienti[i-3].posli("hrac " + itos(i-3) + "\n");
		
		kposy.push_back(0);
		kodpovede.push_back("");
	}

	// nacita mapu
	stav stavHry;
	string mapAdr(argv[2]);
	nacitajMapu(mapAdr, stavHry, klienti.size());

	// zakoduje pociatocny stav a posle ho
	stringstream pocStav;
	koduj(pocStav, stavAlt(stavHry));
	observationstream << pocStav.str();
	for (unsigned k=0; k<klienti.size(); k++) {
		klienti[k].posli(pocStav.str());
	}

	long long ltime = gettime();

	while (stavHry.vyherca() == -1) {
		string resetAns;
		{
			stringstream temp;
			koduj(temp, stavAlt(stavHry));
			resetAns = temp.str();
		}
		while (gettime() - ltime < TAH_CAS) {
			for (unsigned k=0; k<klienti.size(); k++) {
				if (!klienti[k].zije()) {
					klienti[k].restartuj();
					continue;
				}
				stringstream riadky(klienti[k].citaj(MAX_CITAJ));
				odpovedaj(k, riadky, resetAns);
			}
		}
		ltime = gettime();
		
		stringstream pokracovanieHistorie;
		odsimulujKolo(stavHry, kodpovede, pokracovanieHistorie);
		for (unsigned k=0; k<klienti.size(); k++) {
			kodpovede[k].clear();
		}
		historia += pokracovanieHistorie.str();

		observationstream << pokracovanieHistorie.str() << "end\n" << flush;
	}

  observationstream.close();

  return 0;
}
