package common;

import java.util.*;
import java.io.*;
import common.Bunka;
import common.Invazia;
import common.InvAlt;
import common.StavAlt;
import common.Common;
import struct.RADeque;

class CompBunka implements Comparator<Bunka> {
	public int compare (Bunka a, Bunka b) {
		return a.id - b.id;
	}
}

public class Stav
{
	public int cas;
	public ArrayList<Bunka> cely;
	public ArrayList<TreeSet<Bunka> > vlastnim;
	public RADeque<ArrayList<Invazia> > invPodlaCasu;

	public Stav () {
		cas = 0;
		cely = new ArrayList<Bunka>();
		vlastnim = new ArrayList<TreeSet<Bunka> >();
		invPodlaCasu = new RADeque<ArrayList<Invazia> >();
	}
	public Stav (StavAlt S) {
		this();
		cas = S.cas;
		for (int i=0; i<S.cely.size(); i++) {
			Bunka cel = new Bunka(S.cely.get(i));
			cely.add(cel);
		}
		urciVlastnictvo();
		for (int i=0; i<S.invZoznam.size(); i++) {
			InvAlt inva = S.invZoznam.get(i);
			novaInv(inva);
		}
	}

	public void urciVlastnictvo () {
		for (int i=0; i<cely.size(); i++) {
			int vlastnik = cely.get(i).vlastnik;
			if (vlastnik < 0) {
				continue;
			}
			while (vlastnim.size() <= vlastnik) {
				TreeSet<Bunka> ts = new TreeSet<Bunka>(new CompBunka());
				vlastnim.add(ts);
			}
			vlastnim.get(vlastnik).add(cely.get(i));
		}
	}
	public void nastavBunku (int id, int vlastnik, int populacia) {
		int old = cely.get(id).vlastnik;
		if (old >= 0) {
			vlastnim.get(old).remove(cely.get(id));
		}
		cely.get(id).vlastnik = vlastnik;
		if (vlastnik >= 0) {
			while (vlastnim.size() <= vlastnik) {
				TreeSet<Bunka> ts = new TreeSet<Bunka>(new CompBunka());
				vlastnim.add(ts);
			}
			vlastnim.get(vlastnik).add(cely.get(id));
		}

		cely.get(id).populacia = populacia;
		cely.get(id).poslCas = Common.velkyCas;
	}
	public void nastavCas (int t) {
		int diff = t - cas;
		for (int i=0; i<diff && !invPodlaCasu.isEmpty(); i++) {
			invPodlaCasu.pop_front();
		}
		cas = t;
		Common.velkyCas = t;
	}
	
	public void nastavInv (int odchod, int prichod, int vlastnik, int od, int kam, int jednotiek) {
		// natvrdo nastavi invaziu, dobre ked sa udiala/udeje nie v tomto okamihu
		int diff = prichod - cas;
		while (invPodlaCasu.size() <= diff) {
			ArrayList<Invazia> novy = new ArrayList<Invazia>();
			invPodlaCasu.push_back(novy);
		}
		Bunka utk = cely.get(od);
		Bunka obr = cely.get(kam);
		Invazia inv = new Invazia(odchod, prichod, vlastnik, utk, obr, jednotiek);
		invPodlaCasu.get(diff).add(inv);
	}
	public void nastavInv (InvAlt inva) {
		nastavInv(inva.odchod, inva.prichod, inva.vlastnik, inva.od, inva.kam, inva.jednotiek);
	}
	public void novaInv (int prichod, int od, int kam, int jednotiek) {
		// rata s tym, ze ma aktualne data a ze invazia vznika v TOMTO OKAMIHU
		nastavInv(cas, prichod, cely.get(od).vlastnik, od, kam, jednotiek);
	}
	public void novaInv (InvAlt inva) {
		novaInv(inva.prichod, inva.od, inva.kam, inva.jednotiek);
	}

	public int vyherca () {
		int kto = -2;
		for (int i=0; i<vlastnim.size(); i++) {
			if (vlastnim.get(i).isEmpty()) {
				continue;
			}
			if (kto != -2) {
				return -1;
			}
			kto = i;
		}
		return kto;
	}

	public void dekodujStav (Scanner sc) {
		while (sc.hasNext()) {
			String prikaz = sc.next();
			if (prikaz.equals("end")) {
				break;
			}
			if (prikaz.equals("bunka")) {
				Bunka cel = new Bunka();
				cel.nacitaj(sc);
				nastavBunku(cel.id, cel.vlastnik, cel.zistiPop());
			}
			if (prikaz.equals("invAlt")) {
				InvAlt inva = new InvAlt();
				inva.nacitaj(sc);
				novaInv(inva);
			}
			if (prikaz.equals("stavAlt")) {
				StavAlt salt = new StavAlt();
				salt.nacitaj(sc);

				// debilne skopiruj stav
				Stav novy = new Stav(salt);
				cas = novy.cas;
				cely = novy.cely;
				vlastnim = novy.vlastnim;
				invPodlaCasu = novy.invPodlaCasu;
			}
			if (prikaz.equals("cas")) {
				int t = Integer.parseInt(sc.next());
				nastavCas(t);
			}
		}
	}
}
