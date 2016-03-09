import common.*;
import struct.*;

import java.util.*;
import java.math.*;
import java.io.*;
import java.nio.file.*;

import java.awt.*;
import java.awt.Font;
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
			sc.next(); // zbav sa dekoratora "mesto"
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
				if (prikaz.equals("mesto")) {
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

class Stats extends JComponent {
	Stav S;
	ArrayList<Klient> klienti;
	String ktoryStat;

	Stats (Stav vzor, ArrayList<Klient> kvzor) {
		S = vzor;
		klienti = kvzor;
		ktoryStat = "populacia";
		setPreferredSize(new Dimension(1000, 100));

		setFont(new Font("Monospace", Font.BOLD, 13));
	}

	void zistiStaty (int[] staty) {
		if (ktoryStat.equals("populacia")) {
			for (int i=0; i<S.cely.size(); i++) {
				int vlastnik = S.cely.get(i).vlastnik;
				if (vlastnik<0 || vlastnik>=staty.length) {
					continue;
				}
				staty[vlastnik] += S.cely.get(i).zistiPop();
			}
			for (int i=0; i<S.invPodlaCasu.size(); i++) {
				ArrayList<Invazia> invy = S.invPodlaCasu.get(i);
				if (invy != null) { // z neznameho dovodu tu mozu byt nullptr? asi nieco paralelne
					for (int j=0; j<invy.size(); j++) {
						int vlastnik = invy.get(j).vlastnik;
						if (vlastnik<0 || vlastnik>=staty.length) {
							continue;
						}
						staty[vlastnik] += invy.get(j).jednotiek;
					}
				}
			}
			return;
		}
	}

	public void paintComponent (Graphics g) {
		int[] staty = new int[ klienti.size() ];
		zistiStaty(staty);
		int maxStat = 1;
		for (int i=0; i<klienti.size(); i++) {
			if (staty[i] > maxStat) {
				maxStat = staty[i];
			}
		}

		if (ktoryStat.equals("populacia")) {
			g.setColor(new Color(0, 32, 0, 255));
		}
		else {
			g.setColor(Color.BLACK);
		}
		g.fillRect(0, 0, getWidth(), getHeight());

		int okraj = 10;
		int hrubka = (getHeight() - 2*okraj) / klienti.size();
		for (int i=0; i<klienti.size(); i++) {
			int x = okraj;
			int y = okraj + (getHeight() - 2*okraj)*(1+i) / klienti.size();
			int dlzka = (getWidth() - 2*okraj)*staty[i] / maxStat;
			Color cl = klienti.get(i).cl;
			g.setColor(cl);
			g.fillRect(x, y-hrubka, dlzka, hrubka);
		}
		for (int i=0; i<klienti.size(); i++) {
			int x = okraj;
			int y = okraj + (getHeight() - 2*okraj)*(1+2*i) / (2*klienti.size());
			Color cl = klienti.get(i).cl;
			float[] hsbvals = new float[3];
			Color.RGBtoHSB(cl.getRed(), cl.getGreen(), cl.getBlue(), hsbvals);
			if (hsbvals[2] >= 0.95) {
				cl = cl.darker().darker();
			}
			else {
				cl = cl.brighter().brighter();
			}
			g.setColor(cl);
			g.drawString(klienti.get(i).name, x, y);
		}
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

		setFont(new Font("Monospace", Font.BOLD, 13));
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
		else
		if (vlastnik == -2) {
			fillcl = new Color(92, 0, 92, 255);
		}
		int x = cel.pozicia.x;
		int y = cel.pozicia.y;
		int maxr = maxPolomerBunky(cel);
		int r = polomerBunky(cel);
		g.setColor(Color.RED);
		g.drawOval(x-maxr, y-maxr, 2*maxr, 2*maxr);
		g.setColor(fillcl);
		g.fillOval(x-r, y-r, 2*r, 2*r);

		// vypiseme ciselka == parametre bunky
		// natvrdo konstanty o pozicii -- nac sa s tym srat
		Color clutok = new Color(255, 0, 0, 255);
		Color clobr = new Color(0, 0, 255, 255);
		Color clsten = new Color(0, 255, 255, 255);
		Color clrast = new Color(0, 255, 0, 255);
		Color cltotalobr = new Color(255, 255, 255, 255);
		float[] hsbvals = new float[3];
		Color.RGBtoHSB(fillcl.getRed(), fillcl.getGreen(), fillcl.getBlue(), hsbvals);
		// System.err.format("%d %d %d, %f\n",fillcl.getRed(), fillcl.getGreen(), fillcl.getBlue(), hsbvals[2]);
		if (hsbvals[2] >= 0.95) {
			clutok = clutok.darker();
			clobr = clobr.darker();
			clsten = clsten.darker();
			clrast = clrast.darker();
			cltotalobr = cltotalobr.darker();
			cltotalobr = cltotalobr.darker();
		}
		g.setColor(clutok);
		g.drawString(Integer.toString(cel.utok), x-18, y-8);
		g.setColor(clobr);
		g.drawString(Integer.toString(cel.obrana), x-18, y+18);
		g.setColor(clsten);
		g.drawString(Integer.toString(cel.stena), x+8, y-8);
		g.setColor(clrast);
		g.drawString(Integer.toString(cel.rast), x+8, y+18);
		g.setColor(cltotalobr);
		g.drawString(Integer.toString(cel.def()), x-18, y+5);
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
		if (vlastnik == -2) { // temni rytieri nie su v ziadnom pripade normalny pripad!!!
			Color fillcl = new Color(92, 0, 92, 255);
			Bod kde = inv.kam.pozicia;
			int x = kde.x;
			int y = kde.y;
			int r = 1 + (inv.prichod-S.cas)*maxPolomerBunky(inv.kam)/(inv.prichod-inv.odchod);
			g.setColor(fillcl);
			for (int hrubka=0; hrubka<1; hrubka++) {
				g.drawOval(x-r-hrubka, y-r-hrubka, 2*(r+hrubka), 2*(r+hrubka));
			}
			return;
		}
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

		/*
		// vypiseme ciselka o invazii
		Color cltotalatk = new Color(255, 255, 255, 255);
		float[] hsbvals = new float[3];
		Color.RGBtoHSB(fillcl.getRed(), fillcl.getGreen(), fillcl.getBlue(), hsbvals);
		if (hsbvals[2] >= 0.75) {
			cltotalatk = cltotalatk.darker();
			cltotalatk = cltotalatk.darker();
		}
		g.setColor(cltotalatk);
		g.drawString(Integer.toString(inv.atk()), x-15, y+5);
		*/
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
	Stats sts;
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
				if (key == KeyEvent.VK_ESCAPE) {
					System.exit(0);
				}
			}
		}
	}
	void pridajListenerov () {
		// komponentov je malo, robime rucne
		keyHandler handler = new keyHandler();
		// mainFrame.addKeyListener(handler);
		vis.addKeyListener(handler);
		// timb.addKeyListener(handler);
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
		sts = new Stats(obs.S, klienti);
		mainFrame = new JFrame("Observer");
		mainFrame.setBounds(1500,200,200,200);
		mainFrame.getContentPane().setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS));
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.add(vis);
		mainFrame.add(timb);
		mainFrame.add(sts);
		mainFrame.pack();
		vis.requestFocusInWindow();
		mainFrame.repaint();
		mainFrame.setVisible(true);
		
		pridajListenerov();
	}
	void hlavnyCyklus () {
		long koncovyCas = -1;
		while (true) {
			long olddate = new Date().getTime();

			// automaticke vypnutie hry po skonceni
			if ((koncovyCas != -1) && (olddate - koncovyCas > 3000) && !obs.paused) {
				break;
			}
			if (obs.historia.hasNext()) {
				koncovyCas = -1;
			}
			else {
				if (koncovyCas == -1 && obs.endOfStream) {
					koncovyCas = new Date().getTime();
				}
			}

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
			while (new Date().getTime() - olddate < Common.TAH_CAS) ;
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
		System.exit(0);
	}
}

public class Observer { // iba spustac, nakolko v static maine sa blbo robi s vecami mimo...
	public static void main (String args[]) throws IOException {
		Vesmir V = new Vesmir();
		V.spusti(args);
	}
}
