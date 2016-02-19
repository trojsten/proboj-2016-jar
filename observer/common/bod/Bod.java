package common.bod;

import java.util.*;
import java.math.*;
import java.io.*;
import common.Marshal;

public class Bod
	implements Marshal
{
	public int x,y;

	public Bod (int a,int b) {
		x = a;
		y = b;
	}
	public Bod (Bod vzor) {
		this(vzor.x, vzor.y);
	}
	public Bod () {
		this(0,0);
	}

	public Bod minus (Bod A) {
		Bod res = new Bod(x-A.x, y-A.y);
		return res;
	}
	public double dist () {
		return Math.sqrt(x*x + y*y);
	}

	public void uloz (PrintStream out) {
		out.format("%d %d ",x,y);
	}
	public void nacitaj (Scanner sc) {
		x = Integer.parseInt(sc.next());
		y = Integer.parseInt(sc.next());
	}
	public void koduj (PrintStream out) {
		out.format("bod ");
		uloz(out);
		out.format("%n");
	}
}
