
using namespace std;

#include "marshal.h"

template<> void uloz<int> (ostream& buf, const int& in) {
  buf << in << " ";
}

template<> void nacitaj<int> (istream& buf, int& out) {
  buf >> out;
}

// uloz() pre (niektore) struktury z common.h
#define reflection(T) template<> void uloz<T> (ostream& out, const T& in) {
#define member(x) uloz(out, in.x);
#define end() }
#include "common.h"
#undef reflection
#undef member
#undef end

// nacitaj() pre (niektore) struktury z common.h
#define reflection(T) template<> void nacitaj<T> (istream& in, T& out) {
#define member(x) nacitaj(in, out.x);
#define end() }
#include "common.h"
#undef reflection
#undef member
#undef end

void dekodujStav (istream& buf, stav& out) {
	string prikaz;
	buf >> prikaz;
	while (!(prikaz == "end")) {
		if (prikaz == "bunka") {
			bunka cel;
			nacitaj(buf,cel);
			out.nastavBunku(cel.id, cel.vlastnik, cel.zistiPop());
		}
		if (prikaz == "invAlt") {
			invAlt inva;
			nacitaj(buf,inva);
			out.novaInv(inva);
		}
		if (prikaz == "stavAlt") {
			stavAlt salt;
			nacitaj(buf,salt);
			out = stav(salt);
		}
		if (prikaz == "cas") {
			int ncas;
			buf >> ncas;
			out.nastavCas(ncas);
		}
		buf >> prikaz;
	}
}
