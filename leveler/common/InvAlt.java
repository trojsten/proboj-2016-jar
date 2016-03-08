package common;

import java.io.*;
import java.util.*;
import common.Marshal;
import common.Invazia;
import common.Common;

public class InvAlt
	implements Marshal
{
	public int odchod, prichod, vlastnik, od, kam, jednotiek;

	public InvAlt (int _odchod, int _prichod, int _vlastnik, int _od, int _kam, int _jednotiek) {
		odchod = _odchod;
		prichod = _prichod;
		vlastnik = _vlastnik;
		od = _od;
		kam = _kam;
		jednotiek = _jednotiek;
	}
	public InvAlt () {
		this(-1,-1,-1,-1,-1,-1);
	}
	public InvAlt (Invazia inv) {
		odchod = inv.odchod;
		prichod = inv.prichod;
		vlastnik = inv.vlastnik;
		od = inv.od.id;
		kam = inv.kam.id;
		jednotiek = inv.jednotiek;
	}

	public void uloz (PrintStream out) {
		out.format("%d %d %d %d %d %d ",odchod,prichod,vlastnik,od,kam,jednotiek);
	}
	public void nacitaj (Scanner sc) {
		odchod = Integer.parseInt(sc.next());
		prichod = Integer.parseInt(sc.next());
		vlastnik = Integer.parseInt(sc.next());
		od = Integer.parseInt(sc.next());
		kam = Integer.parseInt(sc.next());
		jednotiek = Integer.parseInt(sc.next());
	}
	public void koduj (PrintStream out) {
		out.format("invAlt ");
		uloz(out);
		out.format("\n");
	}
}
