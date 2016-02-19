package common.stavalt;

import java.io.*;
import java.util.*;
import common.Marshal;
import common.bunka.*;
import common.invalt.*;
import common.invazia.*;
import common.stav.*;
import common.Common;

public class StavAlt
	implements Marshal
{
	public int cas;
	public ArrayList<Bunka> cely;
	public ArrayList<InvAlt> invZoznam;

	public StavAlt () {}
	public StavAlt (Stav S) {
		cas = S.cas;
		for (int i=0; i<S.cely.size(); i++) {
			Bunka cel = new Bunka(S.cely.get(i));
			cely.add(cel);
		}
		for (int i=0; i<S.invPodlaCasu.size(); i++) {
			ArrayList<Invazia> invy = S.invPodlaCasu.get(i);
			for (int j=0; j<invy.size(); j++) {
				Invazia inv = invy.get(j);
				InvAlt inva = new InvAlt(inv);
				invZoznam.add(inva);
			}
		}
	}

	public void uloz (PrintStream out) {
		out.format("%d %d ",cas,cely.size());
		for (int i=0; i<cely.size(); i++) {
			cely.get(i).uloz(out);
		}
		out.format("%d ",invZoznam.size());
		for (int i=0; i<invZoznam.size(); i++) {
			invZoznam.get(i).uloz(out);
		}
	}
	public void nacitaj (Scanner sc) {
		cas = Integer.parseInt(sc.next());
		int n = Integer.parseInt(sc.next());
		for (int i=0; i<n; i++) {
			Bunka cel = new Bunka();
			cel.nacitaj(sc);
			cely.add(cel);
		}
		n = Integer.parseInt(sc.next());
		for (int i=0; i<n; i++) {
			InvAlt inva = new InvAlt();
			inva.nacitaj(sc);
			invZoznam.add(inva);
		}
	}
	public void koduj (PrintStream out) {
		out.format("stavAlt ");
		uloz(out);
		out.format("%n");
	}
}
