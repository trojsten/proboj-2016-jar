#include <iostream>
#include <unistd.h>

using namespace std;

#include "common.h"
#include "marshal.h"

// main() zavola tuto funkciu, ked chce vediet, aky prikaz chceme vykonat,
// co tato funkcia rozhodne pomocou toho, ako nastavi prikaz;
void zistiTah() {
  
}

int main() {
  // v tejto funkcii su vseobecne veci, nemusite ju menit (ale mozte).

  unsigned seed = time(NULL) * getpid();
  srand(seed);
  fprintf(stderr, "START pid=%d, seed=%u\n", getpid(), seed);

  while (cin.good()) {
		string nieco;
		cin >> nieco;
		while (true) {
			cout << "trololo\n";
		}
	}

  return 0;
}
