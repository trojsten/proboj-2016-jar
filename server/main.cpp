#include <vector>
#include <fstream>
#include <iostream>
#include <set>
#include <sstream>
#include <unistd.h>
#include <sys/stat.h>
#include <string.h>
#include <algorithm>

using namespace std;

#include "util.h"
#include "common.h"
#include "update.h"
#include "klient.h"
#include "mapa.h"
#include "marshal.h"

#define PRECISION 10000
#define MAX_CITAJ 1024

vector<Klient> klienti;
vector<unsigned> kposy;
vector<string> kodpovede;

string historia;


// tato trapna funkcia existuje len kvoli inicializujSignaly()
// btw, takto sa signal handling nerobi
// len sa s tym nechceme babrat.
void zabiKlientov() {
	fprintf(stderr, "ukoncujem klientov\n");
	for (unsigned i=0; i<klienti.size(); i++) {
		if (klienti[i].getMeno() == "observer") {
			continue;
		}
		klienti[i].zabi();
	}
}

bool validchar (char ch) {
	if (ch>='0' && ch<='9') {
		return false;
	}
	if (ch=='/') {
		return false;
	}
	return true;
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

template<class T> void checkOstream(T& s, string filename) {
  if (s.fail()) {
    fprintf(stderr, "neviem zapisovat do %s\n", filename.c_str());
    zabiKlientov();
    exit(1);
  }
}


int main(int argc, char *argv[]) {
	if (argc < 5) {
    fprintf(stderr, "usage: %s <zaznamovy-adresar> <mapa> <observer-mode> {<adresare-klientov>...}\n", argv[0]);
    return 1;
  }
  
	unsigned int seed = time(NULL) * getpid();
  srand(seed);
  // loguj("startujem server, seed je %u", seed); // seed je nanic, lebo proboj je nedeterministicky
  loguj("startujem server...");
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
	
	{
		string metainfo;
		
		random_shuffle(argv + 4, argv + argc);
		set<string> uzMena;
		
		for (int i=4; i<argc; i++) {
			string klientAdr(argv[i]);
			// meno klienta je cast za poslednym /, za ktorym nieco je
			int r = (int)klientAdr.size() - 1;
			while (r>0 && !validchar(klientAdr[r-1])) {
				r--;
			}
			r++;
			int l = r - 1;
			while (l>0 && klientAdr[l-1]!='/') {
				l--;
			}
			string meno = klientAdr.substr(l, r-l);
			bool dajNahodnuFarbu = false;
			while (uzMena.count(meno)) {
				dajNahodnuFarbu = true;
				meno += "+";
			}
			uzMena.insert(meno);
			string uvodneData = "hrac " + toString<int>(i-4) + "\n";
			klienti.push_back(Klient(meno, uvodneData, klientAdr, zaznAdr));			
			kposy.push_back(0);
			kodpovede.push_back("");

			string riadok;
			if (dajNahodnuFarbu) {
				for (int i=0; i<3; i++) {
					double cl = (PRECISION + (double)(rand()%(2*PRECISION)) )/(4*PRECISION);
					riadok += toString<double>(cl) + " ";
				}
				riadok += "1.0";
			}
			else {
				string clsubor = klientAdr + "/color";
				fstream clstream(clsubor.c_str(), fstream::in);
				getline(clstream, riadok);
				clstream.close();
			}
			metainfo += meno + " " + riadok + "\n";
		}

		// nastav observera-live / hraca-cloveka
		if (strcmp(argv[3],"live")==0 || strcmp(argv[3],"play")==0) {
			int i = strlen(argv[0]);
			while (i>0 && argv[0][i-1]!='/') {
				i--;
			}
			string meno = "observer";
			string uvodneData;
			if (strcmp(argv[3],"play")==0) {
				metainfo += meno + " 1.0 1.0 0.0 1.0\n";
				uvodneData = "hrac " + toString<unsigned>(klienti.size()) + "\n";
			}
			uvodneData = metainfo + "end\n" + uvodneData;
			string adr = string(argv[0]).substr(0,i);
			klienti.push_back(Klient(meno, uvodneData, adr, "./observer.jar", zaznAdr));
			kposy.push_back(0);
			kodpovede.push_back("");
		}
		string metasubor = zaznAdr+"/meta";
		fstream metastream(metasubor.c_str(), fstream::out | fstream::trunc);
		metastream << metainfo << "end\n";
		metastream.close();

		for (unsigned k=0; k<klienti.size(); k++) {
			klienti[k].restartuj();
		}
	}

	// nacita mapu
	stav stavHry;
	string mapAdr(argv[2]);
	unsigned pocetHracov = argc - 4;
	if (strcmp(argv[3],"play")==0) {
		pocetHracov++;
	}
	nacitajMapu(mapAdr, stavHry, pocetHracov);
	
	// zakoduje pociatocny stav a posle ho
	// potom pocka chvilu --- cas na predpocitanie
	stringstream pocStav;
	koduj(pocStav, stavAlt(stavHry));
	observationstream << pocStav.str() << flush;
	for (unsigned k=0; k<klienti.size(); k++) {
		klienti[k].posli(pocStav.str() + "end\n");
	}
	usleep(CAS_NA_INICIALIZACIU*1000ll);

	long long ltime = gettime();
	inicializujStaty(pocetHracov,stavHry);
	
	bool koncim = false;
	while (!koncim) {
		string resetAns;
		{
			stringstream temp;
			koduj(temp, stavAlt(stavHry));
			resetAns = temp.str();
		}
		while (gettime() - ltime < TAH_CAS) {
			for (unsigned k=0; k<klienti.size(); k++) {
				if (!klienti[k].zije() && (klienti[k].getMeno() != "observer")) {
					klienti[k].restartuj();
					klienti[k].posli(resetAns + "end\n");
					continue;
				}
				stringstream riadky(klienti[k].citaj(MAX_CITAJ));
				odpovedaj(k, riadky, resetAns);
			}
		}
		ltime = gettime();
		
		stringstream pokracovanieHistorie;
		koncim = odsimulujKolo(stavHry, kodpovede, pokracovanieHistorie);
		for (unsigned k=0; k<klienti.size(); k++) {
			kodpovede[k].clear();
		}
		historia += pokracovanieHistorie.str();

		observationstream << pokracovanieHistorie.str() << flush;
	}
	
	// cleanup
	observationstream.close();
	zabiKlientov();

	// vypis casy umrtia jednotlivych hracov
	ofstream rankstream((zaznAdr+"/rank").c_str());
	vector<int> casUmrtia;
	ulozUmrtia(casUmrtia);
	rankstream << pocetHracov << "\n";
	for (int i=0; i<pocetHracov; i++) {
		rankstream << klienti[i].getMeno() << " " << casUmrtia[i] << "\n";
	}
	rankstream.close();
	checkOstream(rankstream, zaznAdr+"/rank");

	// +- info o dlzke hry
	loguj("");

  return 0;
}
