package common;

import java.util.*;
import java.io.*;
import common.Bunka;
import common.Invazia;
import common.InvAlt;
import common.StavAlt;
import common.Common;
import struct.RADeque;

public class Stav
{
	public int cas;
	public ArrayList<Bunka> cely;
	public RADeque<ArrayList<Invazia> > invPodlaCasu;

	public Stav () {
		cas = 0;
		cely = new ArrayList<Bunka>();
		invPodlaCasu = new RADeque<ArrayList<Invazia> >();
	}
	public Stav (StavAlt S) {
		this();
		cas = S.cas;
		for (int i=0; i<S.cely.size(); i++) {
			Bunka cel = new Bunka(S.cely.get(i));
			cely.add(cel);
		}
		for (int i=0; i<S.invZoznam.size(); i++) {
			InvAlt inva = S.invZoznam.get(i);
			nastavInv(inva);
		}
	}

	public void nastavBunku (int id, int vlastnik, int populacia) {
		cely.get(id).vlastnik = vlastnik;
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

	public void dekodujStav (Scanner sc) {
		while (sc.hasNext()) {
			String prikaz = sc.next();
			if (prikaz.equals("end")) {
				break;
			}
			if (prikaz.equals("mesto")) {
				Bunka cel = new Bunka();
				cel.nacitaj(sc);
				nastavBunku(cel.id, cel.vlastnik, cel.zistiPop());
			}
			if (prikaz.equals("invAlt")) {
				InvAlt inva = new InvAlt();
				inva.nacitaj(sc);
				nastavInv(inva);
			}
			if (prikaz.equals("stavAlt")) {
				StavAlt salt = new StavAlt();
				salt.nacitaj(sc);

				// debilne skopiruj stav
				Stav novy = new Stav(salt);
				cas = novy.cas;
				cely = novy.cely;
				invPodlaCasu = novy.invPodlaCasu;
			}
			if (prikaz.equals("cas")) {
				int t = Integer.parseInt(sc.next());
				nastavCas(t);
			}
		}
	}
}
