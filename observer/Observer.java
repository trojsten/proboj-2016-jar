import java.util.*;
import java.io.*;
import common.*;
import struct.*;

class Pair {
	int first;
	int second;

	Pair (int a,int b) {
		first = a;
		second = b;
	}
	Pair () {
		this(-1,-1);
	}
}

class CompPair implements Comparator<Pair> {
	public int compare (Pair a, Pair b) {
		if (a.first != b.first) {
			return (a.first < b.first ? -1 : 1);
		}
		if (a.second != b.second) {
			return (a.second < b.second ? -1 : 1);
		}
		return 0;
	}
	public boolean equals (Pair a, Pair b) {
		return (a.first == b.first) && (a.second == b.second);
	}
}


class CellHistory {
	ArrayList<TreeSet<Pair> > hist;

	CellHistory () {
		hist = new ArrayList<TreeSet<Pair> >();
	}

	void updatniPocet (int n) {
		for (int i=hist.size(); i<n; i++) {
			TreeSet<Pair> ts = new TreeSet<Pair>(new CompPair());
			hist.add(ts);
		}
	}

	void nastav (int cas, int pos, Bunka cel) {
		hist.get(cel.id).add(new Pair(cas,pos));
	}
	void seek (int lcas, int rcas, Stav S, RStream historia) {
		for (int i=0; i<S.cely.size(); i++) {
			Pair caspos = hist.get(i).lower(new Pair(rcas,-1));
			if (caspos == null) {
				continue;
			}
			if (caspos.first < lcas) {
				continue;
			}
			int npos = caspos.second;
			historia.setPos(npos);
			String riadok = historia.nextLine();
			Scanner sc = new Scanner(riadok);
			sc.next(); // zbav sa dekoratora "bunka"
			S.cely.get(i).nacitaj(sc);
			// sc.close();
		}
	}
}

class InvHistory {
	SortingIntervalTree<Pair> hist;

	InvHistory () {
		hist = new SortingIntervalTree<Pair>(new CompPair());
	}

	void novaInv (int cas, int pos, InvAlt inva) {
		hist.set(cas, new Pair(inva.prichod, pos));
	}
	void seek (int lcas, int rcas, Stav S, RStream historia) {
		ArrayList<Pair> invy = hist.greaterOrEqual(lcas, rcas, new Pair(rcas,-1));
		for (int i=0; i<invy.size(); i++) {
			Pair caspos = invy.get(i);
			if (caspos.first < lcas) {
				continue;
			}
			int npos = caspos.second;
			historia.setPos(npos);
			String riadok = historia.nextLine();
			Scanner sc = new Scanner(riadok);
			sc.next(); // zbav sa dekoratora "invAlt"
			InvAlt inva = new InvAlt();
			inva.nacitaj(sc);
			S.novaInv(inva);
			// sc.close();
		}
	}
}

class CasHistory {
	TreeSet<Pair> hist;

	CasHistory () {
		hist = new TreeSet<Pair>(new CompPair());
	}

	void nastav (int cas, int pos) {
		hist.add(new Pair(cas,pos));
	}
	boolean seek (int cas, Stav S, RStream historia) {
		Pair caspos = hist.ceiling(new Pair(cas,-1));
		if (caspos == null) {
			return false;
		}
		if (caspos.first != cas) {
			return false;
		}
		int npos = caspos.second;
		historia.setPos(npos);
		historia.nextLine(); // tento riadok uz bude spracovany -- zbav sa ho
		S.nastavCas(cas);
		// sc.close();
		return true;
	}
}

class CheckHistory {
	TreeSet<Pair> hist;

	CheckHistory () {
		hist = new TreeSet<Pair>(new CompPair());
	}

	void nastav (int cas, int pos) {
		hist.add(new Pair(cas,pos));
	}
	void seek (int cas, Stav S, RStream historia) {
		Pair caspos = hist.floor(new Pair(cas,1023456789));
		if (caspos == null) { // how could this happen...
			return;
		}
		int npos = caspos.second;
		historia.setPos(npos);
		String riadok = historia.nextLine();
		Scanner sc = new Scanner(riadok);
		sc.next(); // zbav sa dekoratora "stavAlt"
		StavAlt salt = new StavAlt();
		salt.nacitaj(sc);
		
		// debilne skopiruj stav, lebo java...
		Stav novy = new Stav(salt);
		S.cas = novy.cas;
		S.cely = novy.cely;
		S.vlastnim = novy.vlastnim;
		S.invPodlaHrany = novy.invPodlaHrany;
		S.invPodlaCasu = novy.invPodlaCasu;
		// sc.close();
	}
}

class ObStav {
	int hrac;
	Stav S;
	RStream historia;

	CellHistory celh;
	InvHistory invh;
	CasHistory cash;
	CheckHistory chkh;

	int poslCas;

	ObStav () {
		hrac = -1;
		S = new Stav();
		historia = new RStream();
		celh = new CellHistory();
		invh = new InvHistory();
		cash = new CasHistory();
		chkh = new CheckHistory();
		poslCas = 0;
	}

	boolean ulozStav (Scanner sc) {
		boolean asponJeden = false;
		while (sc.hasNext()) {
			String riadok = sc.nextLine() + "\n";
			
			Scanner riad = new Scanner(riadok);
			String prikaz = riad.next();
			if (prikaz.equals("end")) {
				break;
			}
			asponJeden = true;
			if (hrac == -1) {
				if (prikaz.equals("hrac")) {
					hrac = Integer.parseInt(riad.next());
					continue;
				}
				if (prikaz.equals("bunka")) {
					Bunka cel = new Bunka();
					cel.nacitaj(riad);
					celh.nastav(poslCas, historia.length(), cel);
				}
				if (prikaz.equals("invAlt")) {
					InvAlt inva = new InvAlt();
					inva.nacitaj(riad);
					invh.novaInv(poslCas, historia.length(), inva);
				}
				if (prikaz.equals("stavAlt")) {
					StavAlt salt = new StavAlt();
					salt.nacitaj(riad);
					poslCas = salt.cas;
					chkh.nastav(poslCas, historia.length());
					celh.updatniPocet(salt.cely.size());
				}
				if (prikaz.equals("cas")) {
					poslCas = Integer.parseInt(riad.next());
					cash.nastav(poslCas, historia.length());
				}
			}
			historia.append(riadok);
		}
		return asponJeden;
	}

	void seek (int cas) {
		if (hrac!=-1 || cas<0 || cas>poslCas) {
			return;
		}
		chkh.seek(cas, S, historia);
		int npos = historia.getPos();
		celh.seek(S.cas, cas, S, historia);
		invh.seek(S.cas, cas, S, historia);
		if (!cash.seek(cas, S, historia)) {
			historia.setPos(npos);
		}
	}

	boolean advanceTime () {
		int oldcas = S.cas;
		while (historia.hasNext() && S.cas == oldcas) {
			String riadok = historia.nextLine();
			Scanner sc = new Scanner(riadok);
			S.dekodujStav(sc);
		}
		return (S.cas != oldcas);
	}
}

public class Observer {
	public static void main (String args[]) throws IOException {
		ObStav obs = new ObStav();
		Scanner sc = new Scanner(System.in);
		long casSkoncenia = -1;
		while (sc.hasNext() || obs.historia.hasNext() || (new Date().getTime() - casSkoncenia < 2000)) {
			if (sc.hasNext()) {
				while (!obs.ulozStav(sc)) {
					// akcie pre hraca-cloveka
				}
				System.out.format("changeDesc\n");
			}
			while (obs.advanceTime()) {
				// bez hraca-cloveka
			}

			// aby to na konci hned neskapalo
			if (obs.historia.hasNext()) {
				casSkoncenia = -1;
			}
			else
			if (!sc.hasNext() && !obs.historia.hasNext() && casSkoncenia == -1) {
				casSkoncenia = new Date().getTime();
			}
		}
	}
}
