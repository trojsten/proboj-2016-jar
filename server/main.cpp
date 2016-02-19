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

// tato trapna funkcia existuje len kvoli inicializujSignaly()
void zabiKlientov() {
	loguj("ukoncujem klientov");
	for (unsigned i=0; i<klienti.size(); i++) {
		klienti[i].zabi();
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
	}
	
	stav stavHry;
	string mapAdr(argv[2]);
	nacitajMapu(mapAdr, stavHry, klienti.size());
	koduj(observationstream, stavAlt(stavHry));

	long long ltime = gettime();

	while (stavHry.vyherca() == -1) {
		vector<string> odpovede;
		vector<bool> reseteri;
		for (unsigned k=0; k<klienti.size(); k++) {
			odpovede.push_back("");
			reseteri.push_back(false);
			if (!klienti[k].zije()) {
				klienti[k].restartuj();
				continue;
			}
			odpovede[k] = klienti[k].citaj(MAX_CITAJ);
		}
		stringstream normAns, resetAns;
		odsimulujKolo(stavHry, odpovede, normAns, reseteri);
		koduj(resetAns, stavAlt(stavHry));
		for (unsigned k=0; k<klienti.size(); k++) {
			if (reseteri[k]) {
				klienti[k].posli(resetAns.str());
			}
			else {
				klienti[k].posli(normAns.str());
			}
		}
		observationstream << normAns.str() << flush; // treba flushnut kvoli usleep... preco? neviem
		
		long long delay = ltime + TAH_CAS - gettime();
		if (delay < 0) {
			delay = 0;
		}
		if (usleep(delay*1000)!=0) {
			fprintf(stderr, "main/hlavny_cyklus/usleep: %s\n", strerror(errno));
			exit(EXIT_FAILURE);
		}
		ltime = gettime();
	}

  observationstream.close();

  return 0;
}
