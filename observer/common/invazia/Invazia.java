package common.invazia;

import common.bunka.*;
import common.invalt.*;

public class Invazia
{
	public int prichod;
	public Bunka utocnik; // v skutocnosti pointery
	public Bunka obranca;
	public int jednotiek;

	public Invazia () {}
	public Invazia (int _prichod, Bunka _utocnik, Bunka _obranca, int _jednotiek) {
		prichod = _prichod;
		utocnik = _utocnik;
		obranca = _obranca;
		jednotiek = _jednotiek;
	}

	public int atk () {
		return jednotiek*utocnik.utok;
	}
	public int def () {
		return obranca.def();
	}
}
