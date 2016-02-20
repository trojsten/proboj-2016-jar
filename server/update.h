#ifndef UPDATE_H
#define UPDATE_H

#include <string>
#include <sstream>
#include <vector>

#include "common.h"

string stavDesc (stav& stavHry) ;

void odsimulujKolo (stav& stavHry, const vector<string>& odpovede, stringstream& pokrac) ;

#endif
