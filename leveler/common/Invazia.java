package common;

import common.Bunka;
import common.InvAlt;

public class Invazia
{
	public int odchod;
	public int prichod;
	public int vlastnik;
	public Bunka od; // v skutocnosti pointery
	public Bunka kam;
	public int jednotiek;

	public Invazia () {
		odchod = -1;
		prichod = -1;
		vlastnik = -1;
		od = new Bunka();
		kam = new Bunka();
		jednotiek = -1;
	}
	public Invazia (int _odchod, int _prichod, int _vlastnik, Bunka _od, Bunka _kam, int _jednotiek) {
		odchod = _odchod;
		prichod = _prichod;
		vlastnik = _vlastnik;
		od = _od;
		kam = _kam;
		jednotiek = _jednotiek;
	}

	public int atk () {
		return jednotiek * od.utok;
	}
	public int def () {
		return kam.def();
	}
}
