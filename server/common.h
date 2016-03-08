#ifndef COMMON_H
#define COMMON_H

// vseobecne datove struktury

#include <string>
#include <vector>
#include <deque>
#include <set>

using namespace std;

extern int velkyCas; // identicke s casom v stave

struct bod ;
struct mesto ;
struct invazia ;
struct invAlt ;
struct stav ;
struct stavAlt ;


struct bod {
	int x,y;

	bod () ;
	bod (int a,int b) ;

	bod operator- (const bod A) const ;
	double dist () const ;

	static string nazovtyp () ;
};

struct mesto {
	int populacia; //WARNING nepouzivat --- na zistenie populacie je zistiPop() !!!!!!!!!
	int poslCas;

	int id, vlastnik;
	int kapacita, rast;
	int utok, obrana, stena;
	bod pozicia;
	
	mesto () ;

	int zistiPop () ;
	int def () ; // celkova obranna sila mesta (vypocitana zo zistiPop(), obrany a steny)

	static string nazovtyp () ;
};

struct invazia {
	int odchod;
	int prichod;
	int vlastnik;
	mesto* od;
	mesto* kam;
	int jednotiek;

	invazia () ;
	invazia (int odch, int prich, int vlast, mesto* odkial, mesto* kamze, int jedn) ;
	
	int atk () ; // celkova utocna sila invazie
	int def () ; // celkova obranna sila obrancu

	static string nazovtyp () ;
};

struct invAlt { // alternativna reprezentacia invazie
	int odchod, prichod, vlastnik, od, kam, jednotiek;

	invAlt () ;
	invAlt (int odch, int prich, int vlast, int odkial, int kamze, int jedn) ;
	invAlt (invazia inv) ;

	static string nazovtyp () ;
};

struct stav {
	int cas;
	vector<mesto> mesta;
	deque<vector<invazia*> > invPodlaCasu;

	stav () ;
	stav (stavAlt& S) ;
	
	void nastavMesto (int id, int vlastnik, int populacia) ;
	void nastavCas (int t) ;

	void nastavInv (int odchod, int prichod, int vlastnik, int od, int kam, int jednotiek) ;
	void nastavInv (invAlt inv) ;

	static string nazovtyp () ;
};

struct stavAlt { // alternativna reprezentacia stavu
	int cas;
	vector<mesto> mesta;
	vector<invAlt> invZoznam;

	stavAlt () ;
	stavAlt (stav& S) ;

	static string nazovtyp () ;
};

#endif


#ifdef reflection
// tieto udaje pouziva marshal.cpp aby vedel ako tie struktury ukladat a nacitavat

reflection(bod);
  member(x);
  member(y);
end();

reflection(mesto);
	member(populacia);
	member(poslCas);
	member(id);
	member(vlastnik);
	member(kapacita);
	member(rast);
	member(utok);
	member(obrana);
	member(stena);
	member(pozicia);
end();

reflection(invAlt);
	member(odchod);
	member(prichod);
	member(vlastnik);
	member(od);
	member(kam);
	member(jednotiek);
end();

reflection(stavAlt);
	member(cas);
	member(mesta);
	member(invZoznam);
end();

#endif
