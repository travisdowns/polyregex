package travisdowns.github.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Joiner;

/**
 * Based on match and associated functions from https://swtch.com/~rsc/regexp/nfa.c.txt
 * originally written by Russ Cox, converted to Java by Travis Downs.
 * 
 * MIT license, see LICENSE file.
 */
public class NFARunner {

	private class StateList {
		final ArrayList<State> list;
		final Set<Integer> ids;

		public StateList() {
			this.list = new ArrayList<>();
			this.ids = new HashSet<>();
		}

		/* Add s to l, following unlabeled arrows. */
		void addstate(State s) {
			checkNotNull(s);
			if (!ids.contains(s.id)) {
				ids.add(s.id);
				if (s.type == State.Type.SPLIT){
					/* follow unlabeled arrows */
					addstate(s.out.s);
					addstate(s.out1.s);
				} else {
					list.add(s);
				}
			}
		}

		/* Check whether state list contains a match. */
		boolean ismatch()
		{
			return list.stream().anyMatch(s -> s == State.MATCHSTATE);
		}
		
		@Override
		public String toString() {
			return Joiner.on(", ").join(list);
		}
	}
	/*
	typedef struct List List;
	struct List
	{
		State **s;
		int n;
	};

	List l1, l2;
	static int listid;

	void addstate(List*, State*);
	void step(List*, int, List*);
	 */

	/* Compute initial state list */
	private StateList startlist(State start) {
		// TODO: currently we can only run once against State objects because we 
		// don't reset the generation counter - just get rid of that mechanism?
		StateList l = new StateList();
		l.addstate(start);
		return l;
	}

	/*
	 * Step the NFA from the states in clist
	 * past the character c, returns the new NFA state list.
	 */
	private StateList step(StateList clist, int c) {
		StateList nlist = new StateList();
		for (State s : clist.list) {
			if (s.type == State.Type.ANY || (s.type == State.Type.CHAR && s.c == c)) {
				nlist.addstate(s.out.s);
			}
		}
		return nlist;
	}

	/* Run NFA to determine whether it matches s. */
	private boolean match(State start, String str) {
		StateList list = startlist(start);
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			list = step(list, c);
		}
		return list.ismatch();
	}

	public static boolean matches(State start, String str) {
		return new NFARunner().match(start, str);
	}


}
