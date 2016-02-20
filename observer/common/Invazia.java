package common;

import common.Bunka;
import common.InvAlt;

public class Invazia
{
	public int prichod;
	public Bunka utocnik; // v skutocnosti pointery
	public Bunka obranca;
	public int jednotiek;

	public Invazia () {
		prichod = 0;
		utocnik = new Bunka();
		obranca = new Bunka();
		jednotiek = 0;
	}
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
