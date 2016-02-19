#include <fstream>
#include <string>
#include <vector>
#include <algorithm>

using namespace std;

#include "common.h"
#include "marshal.h"

void nacitajMapu (string filename, stav& S, const int pocHrac) {
	fstream subor(filename.c_str(), fstream::in);
	if (subor.fail()) {
		fprintf(stderr, "nacitanie mapy: neviem otvorit subor: %s\n", filename.c_str());
		exit(EXIT_FAILURE);
	}
	nacitaj(subor, S.cely);
	subor.close();

	vector<int> starty;
	for (int i=0; i<(int)S.cely.size(); i++) {
		S.cely[i].id = i;
		if (S.cely[i].vlastnik != -1) {
			starty.push_back(i);
		}
	}
	random_shuffle(starty.begin(), starty.end());
	for (int i=0; i<(int)starty.size(); i++) {
		int kto = starty[i];
		S.cely[kto].vlastnik = (i < pocHrac ? i : -1);
	}

	S.urciVlastnictvo();
}
