#ifndef UPDATE_H
#define UPDATE_H

// herne konstanty
#define RYCHLOST_JEDNOTIEK 2.0
#define POMALOST_RASTU 1

#include <string>
#include <sstream>
#include <ostream>
#include <vector>

#include "common.h"

void inicializujStaty (unsigned pocHrac, stav& stavHry) ;

void ulozUmrtia (vector<int>& V) ;

bool odsimulujKolo (stav& stavHry, const vector<string>& odpovede, stringstream& pokrac) ;

#endif
