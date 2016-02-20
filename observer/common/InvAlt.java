package common;

import java.io.*;
import java.util.*;
import common.Marshal;
import common.Invazia;

public class InvAlt
	implements Marshal
{
	public int prichod, utocnik, obranca, jednotiek;

	public InvAlt (int _prichod, int _utocnik, int _obranca, int _jednotiek) {
		prichod = _prichod;
		utocnik = _utocnik;
		obranca = _obranca;
		jednotiek = _jednotiek;
	}
	public InvAlt () {
		this(0,0,0,0);
	}
	public InvAlt (Invazia inv) {
		prichod = inv.prichod;
		utocnik = inv.utocnik.id;
		obranca = inv.obranca.id;
		jednotiek = inv.jednotiek;
	}

	public void uloz (PrintStream out) {
		out.format("%d %d %d %d ",prichod,utocnik,obranca,jednotiek);
	}
	public void nacitaj (Scanner sc) {
		prichod = Integer.parseInt(sc.next());
		utocnik = Integer.parseInt(sc.next());
		obranca = Integer.parseInt(sc.next());
		jednotiek = Integer.parseInt(sc.next());
	}
	public void koduj (PrintStream out) {
		out.format("invAlt ");
		uloz(out);
		out.format("\n");
	}
}
