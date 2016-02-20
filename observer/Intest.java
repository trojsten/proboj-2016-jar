import java.io.*;

public class Intest {
	public static void main (String args[]) throws IOException {
		System.out.format("available %d\n",System.in.available());
		int bajt = System.in.read();
		while (bajt != -1) {
			System.out.format("znak %c; available %d\n",(char)bajt,System.in.available());
			bajt = System.in.read();
		}
	}
}
