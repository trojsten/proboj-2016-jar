package struct;

import java.util.*;

class SortingInterval<E> {
	int l,r;
	SortingInterval<E> lch, rch;
	TreeSet<E> elements;

	SortingInterval (int _l, int _r, Comparator<? super E> comp) {
		l = _l;
		r = _r;
		lch = null;
		rch = null;
		elements = new TreeSet<E>(comp);
	}
	SortingInterval (int _l, int _r, SortedSet<E> s) {
		l = _l;
		r = _r;
		lch = null;
		rch = null;
		elements = new TreeSet<E>(s);
	}

	boolean inRange (int pos) {
		return pos>=l && pos<r;
	}
	int elCount () {
		return elements.size();
	}
	Comparator<? super E> comparator () {
		return elements.comparator();
	}

	void giveBirth () {
		if (r-l == 1) {
			return;
		}
		int s = (l+r)/2;
		if (lch == null) {
			lch = new SortingInterval<E>(l, s, comparator());
		}
		if (rch == null) {
			rch = new SortingInterval<E>(s, r, comparator());
		}
	}
	void set (int pos, E val) {
		if (!inRange(pos)) {
			return;
		}
		elements.add(val);
		giveBirth();
		if (lch != null) {
			lch.set(pos,val);
		}
		if (rch != null) {
			rch.set(pos,val);
		}
	}

	SortedSet<E> greaterOrEqual (int ll, int rr, E val) {
		SortedSet<E> res = new TreeSet<E>();
		if (r<=ll || l>=rr) {
			return res;
		}
		if (l>=ll && r<=rr) {
			res.addAll(elements.tailSet(val));
		}
		else {
			if (lch != null) {
				res.addAll(lch.greaterOrEqual(ll,rr,val));
			}
			if (rch != null) {
				res.addAll(rch.greaterOrEqual(ll,rr,val));
			}
		}
		return res;
	}
}

public class SortingIntervalTree<E> {
	SortingInterval<E> root;

	public SortingIntervalTree (Comparator<? super E> comp) {
		root = new SortingInterval<E>(0,1,comp);
	}

	public Comparator<? super E> comparator () {
		return root.comparator();
	}
	public void set (int pos, E val) {
		if (pos < 0) {
			return;
		}
		while (!root.inRange(pos)) {
			SortingInterval<E> nroot = new SortingInterval<E>(0,2*root.r,root.elements);
			nroot.lch = root;
			root = nroot;
		}
		root.set(pos,val);
	}
	public ArrayList<E> greaterOrEqual (int l, int r, E val) {
		ArrayList<E> res = new ArrayList<E>();
		res.addAll(root.greaterOrEqual(l,r,val));
		return res;
	}
}
