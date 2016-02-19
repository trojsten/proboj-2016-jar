package deque;

import java.util.*;

public class Deque<T>
{
	private ArrayList<T> front, back;

	public int size () {
		return front.size() + back.size();
	}
	private void reorganize (int frontBias) {
		int s = size()/2 + frontBias;
		if (s > size()) {
			s = size();
		}
		if (s < 0) {
			s = 0;
		}
		
		ArrayList<T> all = new ArrayList<T>();
		for (int i=front.size()-1; i>=0; i--) {
			all.add(front.get(i));
		}
		front.clear();
		for (int i=0; i<back.size(); i++) {
			all.add(back.get(i));
		}
		back.clear();
		for (int i=s-1; i>=0; i--) {
			front.add(all.get(i));
		}
		for (int i=s; i<all.size(); i++) {
			back.add(all.get(i));
		}
	}

	public void push_front (T obj) {
		front.add(obj);
	}
	public void push_back (T obj) {
		back.add(obj);
	}
	public void pop_front () {
		if (front.isEmpty()) {
			reorganize(1);
		}
		front.remove(front.size()-1);
	}
	public void pop_back () {
		if (back.isEmpty()) {
			reorganize(0);
		}
		back.remove(back.size()-1);
	}

	public T get (int poz) {
		if (poz < front.size()) {
			poz = front.size() - 1 - poz;
			return front.get(poz);
		}
		poz -= front.size();
		return back.get(poz);
	}
	public boolean isEmpty () {
		return size() == 0;
	}
}
