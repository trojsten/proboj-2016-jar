import common.*;
import struct.*;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import javax.swing.*;

class Kreslo extends JComponent {
	Stav S;
	boolean zmenaPolohy;
	Bunka ktora;
	int step;

	public Kreslo () {
		S = new Stav();
		setPreferredSize(new Dimension(1000,500));
		zmenaPolohy = true;
		ktora = null;
		step = 1;
		
		addMouseListener(new pressHandler());
		addMouseMotionListener(new motionHandler());
		addKeyListener(new keyHandler());

		setFont(new Font("Monospace", Font.BOLD, 13));
	}

	////////////////////////////////////////////////
	// veci k handlovaniu level-creatovania ////////
	////////////////////////////////////////////////

	void urciBunku () {
		if (!zmenaPolohy) {
			return;
		}
		zmenaPolohy = false;

		Point pt = getMousePosition();
		if (pt == null) {
			return;
		}
		
		Bod kurzor = new Bod((int)pt.getX(), (int)pt.getY());
		Bunka buf = null;
		double best = 1023456789.00;
		for (int i=0; i<S.cely.size(); i++) {
			Bunka cel = S.cely.get(i);
			Bod diff = kurzor.minus(cel.pozicia);
			if (!leziVBunke(cel, kurzor) && diff.dist()>50.0) {
				continue;
			}
			if (diff.dist() < best) {
				best = diff.dist();
				buf = cel;
			}
		}
		ktora = buf;
	}

	boolean leziVBunke (Bunka cel, Bod poz) {
		Bod diff = poz.minus(cel.pozicia);
		return (diff.dist() <= maxPolomerBunky(cel));
	}
	class pressHandler extends MouseAdapter {
		public void mouseClicked (MouseEvent e) {
			if (ktora != null) {
				return;
			}
			Bunka nova = new Bunka();
			nova.populacia = 100;
			nova.kapacita = 400;
			nova.obrana = 1;
			nova.pozicia = new Bod(e.getX(), e.getY());
			nova.id = S.cely.size();
			S.cely.add(nova);
			
			repaint();
		}
	}
	class motionHandler extends MouseMotionAdapter {
		public void mouseMoved (MouseEvent e) {
			zmenaPolohy = true;
		}
	}
	class keyHandler extends KeyAdapter {
		public void keyPressed (KeyEvent e) {
			int key = e.getKeyCode();

			if (ktora != null) {
				// nastav populaciu
				if (key == KeyEvent.VK_Q) {
					ktora.populacia += step;
					if (ktora.populacia > ktora.kapacita) {
						ktora.populacia = ktora.kapacita;
					}
				}
				if (key == KeyEvent.VK_A) {
					ktora.populacia -= step;
					if (ktora.populacia < 0) {
						ktora.populacia = 0;
					}
				}
				// nastav kapacitu
				if (key == KeyEvent.VK_W) {
					ktora.kapacita += step;
				}
				if (key == KeyEvent.VK_S) {
					ktora.kapacita -= step;
					if (ktora.kapacita < 0) {
						ktora.kapacita = 0;
					}
					if (ktora.populacia > ktora.kapacita) {
						ktora.populacia = ktora.kapacita;
					}
				}
				// nasledovne "generujuce" parametre ignoruju step
				// nastav rast
				if (key == KeyEvent.VK_E) {
					ktora.rast++;
				}
				if (key == KeyEvent.VK_D) {
					ktora.rast--;
				}
				// nastav utok
				if (key == KeyEvent.VK_R) {
					ktora.utok++;
				}
				if (key == KeyEvent.VK_F) {
					ktora.utok--;
					if (ktora.utok < 0) {
						ktora.utok = 0;
					}
				}
				// nastav obranu
				if (key == KeyEvent.VK_T) {
					ktora.obrana++;
				}
				if (key == KeyEvent.VK_G) {
					ktora.obrana--;
				}
				// nastav stenu
				if (key == KeyEvent.VK_Y) {
					ktora.stena += step;
				}
				if (key == KeyEvent.VK_H) {
					ktora.stena -= step;
				}
				// nastav spawnovost
				if (key == KeyEvent.VK_SPACE) {
					if (ktora.vlastnik == -1) {
						ktora.vlastnik = 0;
					}
					else {
						ktora.vlastnik = -1;
					}
				}
				// nastav step
				if (key>=KeyEvent.VK_0 && key<=KeyEvent.VK_9) {
					int exponent = key - KeyEvent.VK_0;
					step = 1;
					for (int i=0; i<exponent; i++) {
						step *= 2;
					}
				}
				// zmaz bunku
				if (key == KeyEvent.VK_DELETE) {
					S.cely.remove(ktora);
					ktora = null;
				}
			}
		}
	}

	////////////////////////////////////////////////
	// veci k vykreslovaniu ////////////////////////
	////////////////////////////////////////////////
	
	// veci k bunke
	int polomerBunky (Bunka cel) {
		return (int) Math.sqrt(25 + cel.zistiPop());
	}
	int maxPolomerBunky (Bunka cel) {
		return (int) Math.sqrt(25 + cel.kapacita) + 1;
	}
	void nakresliBunku (Bunka cel, Graphics g) {
		Color fillcl = Color.GRAY;
		if (cel.vlastnik != -1) {
			fillcl = Color.GREEN.darker();
		}
		int x = cel.pozicia.x;
		int y = cel.pozicia.y;
		int maxr = maxPolomerBunky(cel);
		int r = polomerBunky(cel);
		g.setColor(Color.RED);
		g.drawOval(x-maxr, y-maxr, 2*maxr, 2*maxr);
		g.setColor(fillcl);
		g.fillOval(x-r, y-r, 2*r, 2*r);

		// je bunka oznacena ?
		if (cel == ktora) { // porovnavat pointery (referencie) je OK
			g.setColor(Color.WHITE);
			r = maxr + 2;
			g.drawOval(x-r, y-r, 2*r, 2*r);
		}

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
		if (hsbvals[2] >= 0.75) {
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

	public void paintComponent (Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0,0,getWidth(),getHeight());
		if (S.cely != null) {
			for (int i=0; i<S.cely.size(); i++) {
				nakresliBunku(S.cely.get(i), g);
			}
		}
	}
}

class Univerzum {
	String[] args;

	// GUI cast
	JFrame mainFrame;
	Kreslo vykres;

	void init (String[] _args) {
		args = _args;
		vykres = new Kreslo();
		try {
			Scanner sc = new Scanner(new File(args[0]));
			StavAlt salt = new StavAlt();
			salt.nacitaj(sc);
			vykres.S = new Stav(salt);
		}
		catch (Exception exc) { // takto sa to nerobii
			System.err.format("nepodarilo sa otvorit mapu (asi neexistuje) --- vytvaram\n");
		}
		mainFrame = new JFrame("Leveler");
		mainFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		mainFrame.add(vykres);
		mainFrame.pack();
		mainFrame.repaint();
		vykres.requestFocusInWindow();
		mainFrame.setVisible(true);
	}

	void hlavnyCyklus () {
		while (mainFrame.isVisible()) {
			long oldcas = new Date().getTime();
			vykres.urciBunku();
			mainFrame.repaint();
			while (new Date().getTime() - oldcas < 10) {
			}
		}
	}
	void ukoncuj () {
		// vypis do suboru
		try {
			PrintStream out = new PrintStream(args[0]);
			StavAlt salt = new StavAlt(vykres.S);
			salt.uloz(out);
			out.close();
		}
		catch (FileNotFoundException exc) {
			System.err.format("file not found exception\n");
		}
	}
}

public class Leveler {
	public static void main (String args[]) {
		if (args.length == 0) {
			System.out.format("Ocakavam nazov mapy == suboru, do ktoreho zapisovat, resp. z ktoreho citat. Napriklad \"template.map\"\n");
			return;
		}

		Univerzum U = new Univerzum();
		U.init(args);
		U.hlavnyCyklus();
		U.ukoncuj();

		// ukoncime to, lebo swing je blby a nevie sa vypnut sam
		System.exit(0);
	}
}
