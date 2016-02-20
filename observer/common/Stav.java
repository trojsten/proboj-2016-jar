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
	public ArrayList<ArrayList<RADeque<Invazia> > > invPodlaHrany;
	public RADeque<ArrayList<Invazia> > invPodlaCasu;

	public Stav () {
		cas = 0;
		cely = new ArrayList<Bunka>();
		vlastnim = new ArrayList<TreeSet<Bunka> >();
		invPodlaHrany = new ArrayList<ArrayList<RADeque<Invazia> > >();
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
		vlastnim.get(old).remove(cely.get(id));
		cely.get(id).vlastnik = vlastnik;
		vlastnim.get(vlastnik).add(cely.get(id));

		cely.get(id).populacia = populacia;
		cely.get(id).poslCas = Common.velkyCas;
	}
	public void nastavCas (int t) {
		int diff = t - cas;
		for (int i=0; i<diff && !invPodlaCasu.isEmpty(); i++) {
			ArrayList<Invazia> invy = invPodlaCasu.get(0);
			for (int j=0; j<invy.size(); j++) {
				Invazia inv = invy.get(j);
				int od = inv.utocnik.id;
				int kam = inv.obranca.id;
				invPodlaHrany.get(od).get(kam).pop_front();
			}
			invPodlaCasu.pop_front();
		}
		cas = t;
		Common.velkyCas = t;
	}
	public void novaInv (int prichod, int utocnik, int obranca, int jednotiek) {
		int diff = prichod - cas;
		while (invPodlaCasu.size() <= diff) {
			ArrayList<Invazia> novy = new ArrayList<Invazia>();
			invPodlaCasu.push_back(novy);
		}
		Bunka utk = cely.get(utocnik);
		Bunka obr = cely.get(obranca);
		Invazia inv = new Invazia(prichod,utk,obr,jednotiek);
		invPodlaCasu.get(diff).add(inv);
		invPodlaHrany.get(utocnik).get(obranca).push_back(inv);
	}
	public void novaInv (InvAlt inva) {
		novaInv(inva.prichod, inva.utocnik, inva.obranca, inva.jednotiek);
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
				invPodlaHrany = novy.invPodlaHrany;
				invPodlaCasu = novy.invPodlaCasu;
			}
			if (prikaz.equals("cas")) {
				int t = Integer.parseInt(sc.next());
				nastavCas(t);
			}
		}
	}
}
