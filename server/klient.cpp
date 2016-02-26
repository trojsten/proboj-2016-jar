#include <iostream>
#include <sstream>
#include <climits>
#include <unistd.h>
using namespace std;

#include "klient.h"
#include "util.h"


#define CAS_DO_RESTARTU 5000ll


Klient::Klient () {}

Klient::Klient (string _meno, string _uvodneData, string adresar, string execCommand, string zaznamovyAdresar)
	: meno(_meno), uvodneData(_uvodneData), poslRestart(-1)
{
  vector<string> command;
  command.push_back(execCommand);
  proces.setProperties(command, adresar, zaznamovyAdresar + "/" + meno + ".log");
}

Klient::Klient (string _meno, string _uvodneData, string adresar, string zaznamovyAdresar)
	: meno(_meno), uvodneData(_uvodneData), poslRestart(-1)
{ // treba skopcit, lebo to co je v zatvorkach akosi nevie mat efekt vo vnorenom konstruktore, tj Klient(...) failne
  vector<string> command;
  command.push_back("./hrac");
  proces.setProperties(command, adresar, zaznamovyAdresar + "/" + meno + ".log");
}

string Klient::getMeno () {
  return meno;
}

void Klient::restartuj () {
	long long ntime = gettime();
	if (ntime - poslRestart < CAS_DO_RESTARTU) {
		return ;
	}
	poslRestart = ntime;
  precitane.clear();
  proces.restartuj();

  posli(uvodneData);
}

string Klient::citaj (unsigned cap) {
	string nove = proces.read(cap);
	int i = nove.size();
	while (i > 0 && nove[i-1]!='\n') { // vraciame iba hotovu odpoved, uzavretu znakom noveho riadku
		i--;
	}
	if (i == 0) {
		precitane += nove;
		return "";
	}
	string res = precitane + nove.substr(0,i);
	precitane = nove.substr(i);
	return res;
}

void Klient::posli (string data) {
  proces.write(data);
}

void Klient::zabi () {
  proces.zabi();
}

bool Klient::zije () {
	return proces.zije();
}
