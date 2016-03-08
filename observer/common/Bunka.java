package common;

import java.io.*;
import java.util.*;
import common.Marshal;
import common.Bod;
import common.Common;

public class Bunka
	implements Marshal
{
	public int populacia;
	public int poslCas;

	public int id, vlastnik;
	public int kapacita, rast;
	public int utok, obrana, stena;
	public Bod pozicia;

	public Bunka () {
		populacia = 0;
		poslCas = 0;
		id = -1;
		vlastnik = -1;
		kapacita = 0;
		rast = 0;
		utok = 0;
		obrana = 0;
		stena = 0;
		pozicia = new Bod();
	}
	public Bunka (Bunka vzor) {
		populacia = vzor.populacia;
		poslCas = vzor.poslCas;
		id = vzor.id;
		vlastnik = vzor.vlastnik;
		kapacita = vzor.kapacita;
		rast = vzor.rast;
		utok = vzor.utok;
		obrana = vzor.obrana;
		stena = vzor.stena;
		pozicia = new Bod(vzor.pozicia);
	}

	public int zistiPop () {
		if (vlastnik == -1) {
			poslCas = Common.velkyCas;
			return populacia;
		}
		populacia += (Common.velkyCas - poslCas)*rast;
		if (populacia > kapacita) {
			populacia = kapacita;
		}
		if (populacia < 0) {
			populacia = 0;
		}
		poslCas = Common.velkyCas;
		return populacia;
	}
	public int def () {
		return obrana*zistiPop() + stena;
	}

	public void uloz (PrintStream out) {
		out.format("%d %d %d %d %d %d %d %d %d ",populacia,poslCas,id,vlastnik,kapacita,rast,utok,obrana,stena);
		pozicia.uloz(out);
	}
	public void nacitaj (Scanner sc) {
		populacia = Integer.parseInt(sc.next());
		poslCas = Integer.parseInt(sc.next());
		id = Integer.parseInt(sc.next());
		vlastnik = Integer.parseInt(sc.next());
		kapacita = Integer.parseInt(sc.next());
		rast = Integer.parseInt(sc.next());
		utok = Integer.parseInt(sc.next());
		obrana = Integer.parseInt(sc.next());
		stena = Integer.parseInt(sc.next());
		pozicia.nacitaj(sc);
	}
	public void koduj (PrintStream out) {
		out.format("mesto ");
		uloz(out);
		out.format("\n");
	}
}
