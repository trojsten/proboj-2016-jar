import java.util.*;
import java.math.*;
import java.io.*;
import common.*;
import struct.*;

import java.nio.file.*;

import java.awt.*;
import javax.swing.*;

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
			S.nastavInv(inva);
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
		S.invPodlaCasu = novy.invPodlaCasu;
		// sc.close();
	}
}

class ObStav {
	int hrac;
	long delay; // kolko trva 1 frame ms
	final Stav S;
	RStream historia;

	CellHistory celh;
	InvHistory invh;
	CasHistory cash;
	CheckHistory chkh;

	int poslCas;

	ObStav () {
		hrac = -1;
		delay = 10;
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
					delay = 0;
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

class Klient {
	Color cl;
	String name;

	Klient () {
		cl = Color.GRAY;
		name = "Anonymus";
	}
	Klient (String str, Color farba) {
		name = str;
		cl = farba;
	}
}

class Visual extends JComponent {
	Stav S;
	ArrayList<Klient> klienti;

	public Visual (Stav vzor, ArrayList<Klient> kvzor) {
		S = vzor;
		klienti = kvzor;
		setPreferredSize(new Dimension(1000,700));
	}

	public void paintComponent (Graphics g) {
		// background
		g.setColor(Color.BLACK);
		g.fillRect(0,0,getWidth(),getHeight());

		// bunky
		if (S.cely != null) {
			for (int i=0; i<S.cely.size(); i++) {
				int vlastnik = S.cely.get(i).vlastnik;
				Color fillcl = Color.GRAY;
				if (vlastnik >= 0) {
					fillcl = klienti.get(vlastnik).cl;
				}
				
				int x = S.cely.get(i).pozicia.x;
				int y = S.cely.get(i).pozicia.y;
				int maxr = (int) Math.sqrt(25 + S.cely.get(i).kapacita) + 1;
				int r = (int) Math.sqrt(25 + S.cely.get(i).zistiPop());
				
				g.setColor(Color.RED);
				g.drawOval(x-maxr, y-maxr, 2*maxr, 2*maxr);
				g.setColor(fillcl);
				g.fillOval(x-r, y-r, 2*r, 2*r);
			}
		}

		// invazie
		if (S.invPodlaCasu != null) {
			for (int i=0; i<S.invPodlaCasu.size(); i++) {
				ArrayList<Invazia> invy = S.invPodlaCasu.get(i);
				for (int j=0; j<invy.size(); j++) {
					Invazia inv = invy.get(j);

					int vlastnik = inv.vlastnik;
					Color fillcl = Color.GRAY;
					if (vlastnik >= 0) {
						fillcl = klienti.get(vlastnik).cl;
					}
					
					Bod povod = inv.od.pozicia;
					Bod ciel = inv.kam.pozicia;
					double uz = (S.cas - inv.odchod)/(double)(inv.prichod - inv.odchod);
					Bod kde = povod.plus(ciel.minus(povod).krat(uz));

					int x = kde.x;
					int y = kde.y;
					int r = (int) Math.sqrt(inv.jednotiek);

					g.setColor(fillcl);
					g.fillOval(x-r, y-r, 2*r, 2*r);
				}
			}
		}
		
	}
}

public class Observer {
	public static void main (String args[]) throws IOException {
		// nacitaj stream a metadata
		Scanner sc = null;
		Scanner msc = null;
		if (args.length == 0) {
			sc = new Scanner(System.in);
			Path kde = Paths.get(Observer.class.getProtectionDomain().getCodeSource().getLocation().getPath());
			kde = kde.resolve("meta");
			msc = new Scanner(kde);
		}
		else {
			Path dir = Paths.get("").resolve(args[0]);
			Path obsubor = dir.resolve("observation");
			sc = new Scanner(obsubor);
			Path metasubor = dir.resolve("meta");
			msc = new Scanner(metasubor);
		}
		ArrayList<Klient> klienti = new ArrayList<Klient>();
		while (msc.hasNext()) {
			String meno = msc.next();
			float r = Float.parseFloat(msc.next());
			float g = Float.parseFloat(msc.next());
			float b = Float.parseFloat(msc.next());
			float a = Float.parseFloat(msc.next());
			klienti.add(new Klient(meno, new Color(r,g,b,a)) );
		}


		// inicializuj GUI
		ObStav obs = new ObStav();
		Visual vis = new Visual(obs.S, klienti);
		JFrame mainFrame = new JFrame("Observer");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.add(vis);
		mainFrame.pack();
		mainFrame.setVisible(true);
		

		// hlavny cyklus
		while (sc.hasNext() || obs.historia.hasNext() || mainFrame.isVisible()) {
			if (sc.hasNext()) {
				while (!obs.ulozStav(sc)) {
					System.out.format("changeDesc\n");
				}
				System.out.format("changeDesc\n");
			}
			long olddate = new Date().getTime();
			while (obs.advanceTime()) {
				mainFrame.repaint();
				while (new Date().getTime() - olddate < obs.delay) {
					
				}
				olddate = new Date().getTime();
			}
		}
		
	}
}
