import java.util.*;
import java.math.*;
import java.io.*;
import common.*;
import struct.*;

import java.nio.file.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.SwingUtilities;

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
	TreeMap<Integer,TreeSet<Pair> > hist;

	InvHistory () {
		hist = new TreeMap<Integer,TreeSet<Pair> >();
	}

	void novaInv (int cas, int pos, InvAlt inva) {
		Integer dlzka = new Integer(inva.prichod - inva.odchod);
		if (hist.get(dlzka) == null) {
			hist.put(dlzka, new TreeSet<Pair>(new CompPair()) );
		}
		hist.get(dlzka).add(new Pair(cas,pos));
	}
	void seek (int lcas, int rcas, Stav S, RStream historia) {
		Set<Integer> dlzky = hist.keySet();
		Iterator<Integer> it = dlzky.iterator();
		while (it.hasNext()) {
			Integer dlzka = it.next();
			Pair lbound = new Pair(Math.max(lcas, rcas - dlzka.intValue()), -1);
			Pair rbound = new Pair(rcas, -1);
			SortedSet<Pair> dobri = hist.get(dlzka).subSet(lbound, rbound);

			Iterator<Pair> itp = dobri.iterator();
			while (itp.hasNext()) {
				Pair caspos = itp.next();
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
		S.invPodlaCasu = novy.invPodlaCasu;
		// sc.close();
	}
}

class ObStav {
	int hrac;
	long delay; // kolko trva 1 frame ms
	boolean paused;
	boolean endOfStream;
	
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
		paused = false;
		endOfStream = false;
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
		try {
			while (true) {
				String riadok = sc.nextLine() + "\n";
				
				Scanner riad = new Scanner(riadok);
				String prikaz = riad.next();

				if (prikaz.equals("end")) {
					break;
				}			
				asponJeden = true;
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
				historia.append(riadok);
			}
		}
		catch (NoSuchElementException | IllegalStateException exc) {
			hrac = -1;
			delay = 10;
			endOfStream = true;
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
		if (paused) {
			return false;
		}
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

class Timebar extends JComponent {
	ObStav O;
	int zmena;

	class clickHandler extends MouseAdapter {
		public void mouseClicked (MouseEvent e) {
			double part = e.getX()/(double)getWidth();
			zmena = (int)(part * O.poslCas);
		}
	}
	public Timebar (ObStav vzor) {
		O = vzor;
		zmena = -1;
		setPreferredSize(new Dimension(1000,20));
		addMouseListener(new clickHandler());
	}

	void rewinduj () {
		if (zmena == -1) {
			return;
		}
		O.seek(zmena);
		zmena = -1;
	}
	public void paintComponent (Graphics g) {
		// background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());

		// fill
		double part = (1 + O.S.cas)/(double)(1 + O.poslCas);
		g.setColor(Color.BLUE);
		g.fillRect(0, 0, (int)(part * getWidth()), getHeight());
	}
}

class Visual extends JComponent {
	Stav S;
	ArrayList<Klient> klienti;

	// pre hraca-cloveka
	boolean[] aktivni;
	int mbState; // -1 == nic, 1 == pressed, 0 == just released
	class pressHandler extends MouseAdapter {
		public void mousePressed (MouseEvent e) {
			mbState = 1;
		}
		public void mouseReleased (MouseEvent e) {
			mbState = 0;
		}
	}
	class motionHandler extends MouseMotionAdapter {
		public void mouseMoved (MouseEvent e) {
			mbState = -1;
		}
		public void mouseDragged (MouseEvent e) {
			mbState = 1;
		}
	}
	void clovekuj () {
		Point kurzor = getMousePosition();
		if (kurzor == null || mbState == -1) {
			return;
		}
		Bod kde = new Bod((int)kurzor.getX(), (int)kurzor.getY());
		if (mbState == 1) {
			for (int i=0; i<S.cely.size(); i++) {
				if (leziVBunke(S.cely.get(i), kde)) {
					aktivni[i] = true;
					return;
				}
			}
		}
		if (mbState == 0) {
			int kam = -1;
			for (int i=0; i<S.cely.size(); i++) {
				if (leziVBunke(S.cely.get(i), kde)) {
					kam = i;
					break;
				}
			}
			if (kam == -1) {
				return;
			}
			for (int i=0; i<S.cely.size(); i++) {
				if (!aktivni[i]) {
					continue;
				}
				aktivni[i] = false;
				int jednotiek = S.cely.get(i).zistiPop()/2;
				InvAlt inva = new InvAlt(-1,-1,-1, i, kam, jednotiek);
				System.out.format("invazia ");
				inva.uloz(System.out);
				System.out.format("\n");
			}
		}
	}

	public Visual (Stav vzor, ArrayList<Klient> kvzor) {
		S = vzor;
		klienti = kvzor;
		setPreferredSize(new Dimension(1000,500));
		
		aktivni = new boolean[ S.cely.size() ];
		mbState = -1;
		addMouseListener(new pressHandler());
		addMouseMotionListener(new motionHandler());
	}

	// veci k bunke
	int polomerBunky (Bunka cel) {
		return (int) Math.sqrt(25 + cel.zistiPop());
	}
	int maxPolomerBunky (Bunka cel) {
		return (int) Math.sqrt(25 + cel.kapacita) + 1;
	}
	boolean leziVBunke (Bunka cel, Bod poz) {
		Bod diff = poz.minus(cel.pozicia);
		return (diff.dist() <= maxPolomerBunky(cel));
	}
	void clovekujBunku (Bunka cel, Graphics g) {
		if (!aktivni[cel.id]) {
			return;
		}
		Point kurzor = getMousePosition();
		if (kurzor == null) {
			return;
		}
		int x = cel.pozicia.x;
		int y = cel.pozicia.y;
		int r = maxPolomerBunky(cel) + 1;
		int kx = (int)kurzor.getX();
		int ky = (int)kurzor.getY();
		g.setColor(Color.WHITE);
		g.drawLine(kx, ky, x, y);
		g.drawOval(x-r, y-r, 2*r, 2*r);
	}
	void nakresliBunku (Bunka cel, Graphics g) {
		int vlastnik = cel.vlastnik;
		Color fillcl = Color.GRAY;
		if (vlastnik >= 0) {
			fillcl = klienti.get(vlastnik).cl;
		}
		int x = cel.pozicia.x;
		int y = cel.pozicia.y;
		int maxr = maxPolomerBunky(cel);
		int r = polomerBunky(cel);
		g.setColor(Color.RED);
		g.drawOval(x-maxr, y-maxr, 2*maxr, 2*maxr);
		g.setColor(fillcl);
		g.fillOval(x-r, y-r, 2*r, 2*r);
	}

	// veci k invaziam
	void posliInv (int od, int kam, int jednotiek) {
		InvAlt inva = new InvAlt(-1,-1,-1,od,kam,jednotiek);
		System.out.format("invazia ");
		inva.uloz(System.out);
		System.out.format("\n");
	}
	int polomerInv (Invazia inv) {
		return (int) Math.sqrt(inv.jednotiek);
	}
	Bod poziciaInv (Invazia inv) {
		Bod povod = inv.od.pozicia;
		Bod ciel = inv.kam.pozicia;
		double uz = (S.cas - inv.odchod)/(double)(inv.prichod - inv.odchod);
		return povod.plus(ciel.minus(povod).krat(uz));
	}
	void nakresliInv (Invazia inv, Graphics g) {
		int vlastnik = inv.vlastnik;
		Color fillcl = Color.GRAY;
		if (vlastnik >= 0) {
			fillcl = klienti.get(vlastnik).cl;
		}

		Bod kde = poziciaInv(inv);
		int x = kde.x;
		int y = kde.y;
		int r = polomerInv(inv);

		g.setColor(fillcl);
		g.fillOval(x-r, y-r, 2*r, 2*r);
	}


	public void paintComponent (Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0,0,getWidth(),getHeight());
		if (S.cely != null) {
			for (int i=0; i<S.cely.size(); i++) {
				clovekujBunku(S.cely.get(i), g);
			}
			for (int i=0; i<S.cely.size(); i++) {
				nakresliBunku(S.cely.get(i), g);
			}
		}
		if (S.invPodlaCasu != null) {
			for (int i=0; i<S.invPodlaCasu.size(); i++) {
				ArrayList<Invazia> invy = S.invPodlaCasu.get(i);
				if (invy != null) {
					for (int j=0; j<invy.size(); j++) {
						nakresliInv(invy.get(j), g);
					}
				}
			}
		}
	}
}

class Vesmir {
	String[] args; // passed in arguments
	Scanner sc, msc; // scanner pre normalne veci (stav hry), metaveci
	
	// graficke veci
	JFrame mainFrame;
	ObStav obs;
	Visual vis;
	Timebar timb;

	class keyHandler extends KeyAdapter {
		public void keyPressed (KeyEvent e) {
			int key = e.getKeyCode();
			if (obs.hrac == -1) {
				if (key == KeyEvent.VK_P) {
					obs.paused = !obs.paused;
				}
				if (key == KeyEvent.VK_ADD) {
					if (obs.delay > 1) {
						obs.delay--;
					}
				}
				if (key == KeyEvent.VK_SUBTRACT) {
					obs.delay++;
				}
			}
		}
	}
	void pridajListenerov () {
		// komponentov je malo, robime rucne
		keyHandler handler = new keyHandler();
		mainFrame.addKeyListener(handler);
		vis.addKeyListener(handler);
		timb.addKeyListener(handler);
	}
	void init () throws IOException {
		// nacitanie uvodnych dat
		if (args.length == 0) {
			sc = new Scanner(System.in);
			msc = sc;
		}
		else {
			Path dir = Paths.get("").resolve(args[0]);
			Path obsubor = dir.resolve("observation");
			sc = new Scanner(obsubor);
			Path metasubor = dir.resolve("meta");
			msc = new Scanner(metasubor);
		}
		ArrayList<Klient> klienti = new ArrayList<Klient>();
		String riadok = msc.nextLine();
		while (!riadok.equals("end")) {
			Scanner riad = new Scanner(riadok);
			String meno = riad.next();
			float r = Float.parseFloat(riad.next());
			float g = Float.parseFloat(riad.next());
			float b = Float.parseFloat(riad.next());
			float a = Float.parseFloat(riad.next());
			klienti.add(new Klient(meno, new Color(r,g,b,a)) );
			riadok = msc.nextLine();
		}

		// spracovanie uvodnych dat, spravenie GUI
		obs = new ObStav();
		obs.ulozStav(sc);
		obs.advanceTime();
		vis = new Visual(obs.S, klienti);
		timb = new Timebar(obs);
		mainFrame = new JFrame("Observer");
		mainFrame.getContentPane().setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS));
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.add(vis);
		mainFrame.add(timb);
		mainFrame.pack();
		mainFrame.repaint();
		mainFrame.setVisible(true);
		
		pridajListenerov();
	}
	void hlavnyCyklus () {
		while (true) {
			long olddate = new Date().getTime();

			boolean este = true;
			while (este && !obs.endOfStream) {
				System.out.format("changeDesc\n");
				este = !obs.ulozStav(sc);
			}
			if (obs.hrac == -1) {
				timb.rewinduj();
			}
			else {
				timb.zmena = -1;
				vis.clovekuj();
			}
			mainFrame.repaint();
			do {
				long pred = new Date().getTime();
				obs.advanceTime();
				while (new Date().getTime() - pred < obs.delay) {
				}
			}
			while (new Date().getTime() - olddate < 10) ;
			// mainFrame.repaint(); // tu to dava divne artifakty pri rewindovani, asi preto, ze repaint nie je okamzity
		}
	}
	void spusti (String _args[]) throws IOException {
		args = _args;
		if (args.length > 0) {
			if (args[0].equals("help")) {
				System.out.format("zadaj mi cestu k zaznamovemu adresaru --- napriklad ak sa nachadzas v proboj-2016-jar/observer, tak zadaj napriklad ../zaznamy/01\n");
				return;
			}
		}
		init();
		hlavnyCyklus();
	}
}

public class Observer { // iba spustac, nakolko v static maine sa blbo robi s vecami mimo...
	public static void main (String args[]) throws IOException {
		Vesmir V = new Vesmir();
		V.spusti(args);
	}
}
