#ifndef KLIENT_H
#define KLIENT_H

#include "proces.h"

class Klient {
  private:
    std::string meno;
    std::string uvodneData;
    Proces proces;
    std::string precitane;
    long long poslRestart;
    
  public:
    Klient () ;
    Klient (std::string _label, std::string uvodneData, std::string adresar, std::string nazovExec, std::string logAdresar) ;
    Klient (std::string _label, std::string uvodneData, std::string adresar, std::string logAdresar) ;

    std::string getMeno () ;
    std::string citaj (unsigned cap) ;
    void posli (std::string data) ;
    void restartuj () ;
    void zabi () ;

    bool zije () ;
};

#endif
