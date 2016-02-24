#ifndef UPDATE_H
#define UPDATE_H

#include <string>
#include <sstream>
#include <vector>

#include "common.h"

void inicializujStaty (unsigned pocHrac, stav& stavHry) ;

bool odsimulujKolo (stav& stavHry, const vector<string>& odpovede, stringstream& pokrac) ;

#endif
