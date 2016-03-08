package struct;

import java.util.*;

public class RStream {
	private StringBuilder ch;
	private int pos;

	public RStream () {
		ch = new StringBuilder();
		pos = 0;
	}

	public int length () {
		return ch.length();
	}
	public boolean hasNext () {
		return pos<length();
	}
	public String nextLine () {
		StringBuilder res = new StringBuilder();
		while (pos<length() && ch.charAt(pos)!='\n') {
			res.append(ch.charAt(pos));
			pos++;
		}
		if (pos<length() && ch.charAt(pos)=='\n') {
			pos++;
		}
		return res.toString();
	}
	public int setPos (int npos) {
		pos = npos;
		if (pos < 0) {
			pos = 0;
		}
		if (pos > length()) {
			pos = length();
		}
		return pos;
	}
	public int getPos () {
		return pos;
	}
	public void append (String str) {
		ch.append(str);
	}
	public String toString () {
		return ch.toString();
	}
}
