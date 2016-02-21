#include <iostream>
#include <unistd.h>
#include <algorithm>

using namespace std;

#include "common.h"
#include "marshal.h"

int ja; // cislo hraca
stav S; // stav hry

void prikaz (int od, int kam, int jednotiek) {
	invAlt inva(-1, -1, -1, od, kam, jednotiek);
	cout << "invazia ";
	uloz(cout,inva);
	cout << "\n";
}

// main() zavola tuto funkciu, ked chce vediet, aky prikaz chceme vykonat,
// co tato funkcia rozhodne pomocou toho, ako nastavi prikaz;
void zistiTah() {
  vector<int> moje, ine;
  for (unsigned i=0; i<S.cely.size(); i++) {
		if (S.cely[i].vlastnik == ja) {
			moje.push_back(i);
		}
		else {
			ine.push_back(i);
		}
	}
	if (ine.size() == 0) {
		return;
	}
	int ciel = ine[ rand()%ine.size() ];
	for (unsigned i=0; i<moje.size(); i++) {
		int kto = moje[i];
		int jednotiek = S.cely[kto].zistiPop()/2;
		prikaz(kto, ciel, jednotiek);
	}
}

int main() {
  // v tejto funkcii su vseobecne veci, nemusite ju menit (ale mozte).

  unsigned seed = time(NULL) * getpid();
  srand(seed);
  fprintf(stderr, "START pid=%d, seed=%u\n", getpid(), seed);

	string temp;
	cin >> temp;
	if (temp == "hrac") {
		cin >> ja;
	}
	else {
		cerr << "cakal som cislo hraca, dostal som ale toto: " << temp << "\n";
		return 1;
	}

	dekodujStav(cin,S);
	cout << "desc\n";
	zistiTah();

  while (cin.good()) {
		while (!dekodujStav(cin,S)) {
			cout << "desc\n";
		}
		cout << "desc\n";
		zistiTah();
	}

  return 0;
}
