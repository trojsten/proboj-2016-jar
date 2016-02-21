#ifndef COMMON_H
#define COMMON_H

// vseobecne datove struktury

#include <string>
#include <vector>
#include <deque>
#include <set>

// uroven 0: prototypy

struct bod ;
struct bunka ;
struct invazia ;
struct invAlt ;
struct stav ;
struct stavAlt ;


// uroven 1: trochu viac

extern int velkyCas; // identicke s casom v stave

struct bod {
	int x,y;

	bod () ;
	bod (int a,int b) ;

	bod operator- (const bod A) const ;
	double dist () const ;

	static string nazovtyp () ;
};

struct bunka {
	int populacia; // nepouzivat --- na zistenie populacie je zistiPop()
	int poslCas;

	int id, vlastnik;
	int kapacita, rast;
	int utok, obrana, stena;
	bod pozicia;
	
	bunka () ;

	int zistiPop () ; // jedina vec co zavisi od velkyCas, pre pohodlnost (inak by sa dala spravit s 1 argumentom casom)
	int def () ; // celkova obranna sila bunky

	static string nazovtyp () ;
};

struct invazia {
	int odchod;
	int prichod;
	int vlastnik;
	bunka* od;
	bunka* kam;
	int jednotiek;

	invazia () ;
	invazia (int _odchod, int _prichod, int _vlastnik, bunka* _od, bunka* _kam, int _jednotiek) ;
	
	int atk () ; // celkova utocna sila invazie
	int def () ; // celkova obranna sila obrancu

	static string nazovtyp () ;
};

struct invAlt { // pouzivane len pri komunikacii
	int odchod, prichod, vlastnik, od, kam, jednotiek;

	invAlt () ;
	invAlt (int _odchod, int _prichod, int _vlastnik, int _od, int _kam, int _jednotiek) ;
	invAlt (invazia inv) ;

	static string nazovtyp () ;
};

struct compBunkaPtr {
	bool operator() (const bunka* a, const bunka* b) const ;
};

struct stav {
	int cas;
	vector<bunka> cely; // zoznam vsetkych buniek
	vector<set<bunka*, compBunkaPtr> > vlastnim; // zoznam pointrov na bunky podla majitela
	deque<vector<invazia*> > invPodlaCasu;

	stav () ;
	stav (stavAlt& S) ;

	void urciVlastnictvo () ;
	void nastavBunku (int id, int vlastnik, int populacia) ;
	void nastavCas (int t) ;
	void novaInv (int prichod, int utocnik, int obranca, int jednotiek) ; // rata s tym, ze ma aktualne data a ze invazia vznika v TOMTO OKAMIHU
	void novaInv (invAlt inv) ;

	int vyherca () ;

	static string nazovtyp () ;
};

struct stavAlt { // pouzivane len pri komunikacii
	int cas;
	vector<bunka> cely;
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

reflection(bunka);
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
	member(cely);
	member(invZoznam);
end();

#endif
