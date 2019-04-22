/*
 * Regular expression implementation.
 * Supports traditional egrep syntax, plus non-greedy operators.
 * Tracks submatches a la traditional backtracking.
 * 
 * Normally finds leftmost-biased (traditional backtracking) match;
 * run with -l to get leftmost-longest match (but not POSIX submatches).
 *
 * Normally executes repetitions as much as possible, but no more than 
 * necessary -- i.e. no unnecessary repeats that match the empty string --
 * but this differs from Perl.  Run with -p to get exact Perl behavior.
 *
 * yacc -v nfa-perl.y && gcc y.tab.c
 * 	a.out -d    '(a*)+' aaa    # (0,3)(0,3)
 *	a.out -d -p '(a*)+' aaa	   # (0,3)(3,3)
 *	a.out '(a|aa)(a|aa)' aaa   # (0,2)(0,1)(1,2)
 * 
 * Copyright (c) 2007 Russ Cox.
 * Can be distributed under the MIT license, see bottom of file.
 */

%{
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

enum
{
	LeftmostBiased = 0,
	LeftmostLongest = 1,
};

enum
{
	RepeatMinimal = 0,
	RepeatLikePerl = 1,
};

int debug;
int matchtype = LeftmostBiased;
int reptype = RepeatMinimal;

enum
{
	NSUB = 10
};

typedef struct Sub Sub;
struct Sub
{
	char *sp;
	char *ep;
};

enum
{
	Char = 1,
	Any = 2,
	Split = 3,
	LParen = 4,
	RParen = 5,
	Match = 6,
};
typedef struct State State;
typedef struct Thread Thread;
struct State
{
	int op;
	int data;
	State *out;
	State *out1;
	int id;
	int lastlist;
	int visits;
	Thread *lastthread;
};

struct Thread
{
	State *state;
	Sub match[NSUB];
};

typedef struct List List;
struct List
{
	Thread *t;
	int n;
};

State matchstate = { Match };
int nstate;
int listid;
List l1, l2;

/* Allocate and initialize State */
State*
state(int op, int data, State *out, State *out1)
{
	State *s;
	
	nstate++;
	s = malloc(sizeof *s);
	s->lastlist = 0;
	s->op = op;
	s->data = data;
	s->out = out;
	s->out1 = out1;
	s->id = nstate;
	return s;
}

typedef struct Frag Frag;
typedef union Ptrlist Ptrlist;
struct Frag
{
	State *start;
	Ptrlist *out;
};

/* Initialize Frag struct. */
Frag
frag(State *start, Ptrlist *out)
{
	Frag n = { start, out };
	return n;
}

/*
 * Since the out pointers in the list are always 
 * uninitialized, we use the pointers themselves
 * as storage for the Ptrlists.
 */
union Ptrlist
{
	Ptrlist *next;
	State *s;
};

/* Create singleton list containing just outp. */
Ptrlist*
list1(State **outp)
{
	Ptrlist *l;
	
	l = (Ptrlist*)outp;
	l->next = NULL;
	return l;
}

/* Patch the list of states at out to point to start. */
void
patch(Ptrlist *l, State *s)
{
	Ptrlist *next;
	
	for(; l; l=next){
		next = l->next;
		l->s = s;
	}
}

/* Join the two lists l1 and l2, returning the combination. */
Ptrlist*
append(Ptrlist *l1, Ptrlist *l2)
{
	Ptrlist *oldl1;
	
	oldl1 = l1;
	while(l1->next)
		l1 = l1->next;
	l1->next = l2;
	return oldl1;
}

int nparen;
void yyerror(char*);
int yylex(void);
State *start;

Frag
paren(Frag f, int n)
{
	State *s1, *s2;

	if(n >= NSUB)
		return f;
	s1 = state(LParen, n, f.start, NULL);
	s2 = state(RParen, n, NULL, NULL);
	patch(f.out, s2);
	return frag(s1, list1(&s2->out));
}

%}

%union {
	Frag	frag;
	int	c;
	int nparen;
}

%token	<c>	CHAR
%token	EOL

%type	<frag>	alt concat repeat single line
%type	<nparen>	count

%%

line: alt EOL
	{
		State *s;

		$1 = paren($1, 0);
		s = state(Match, 0, NULL, NULL);
		patch($1.out, s);
		start = $1.start;
		return 0;
	}

alt:
	concat
|	alt '|' concat
	{
		State *s = state(Split, 0, $1.start, $3.start);
		$$ = frag(s, append($1.out, $3.out));
	}
;

concat:
	repeat
|	concat repeat
	{
		patch($1.out, $2.start);
		$$ = frag($1.start, $2.out);
	}
;

repeat:
	single
|	single '*'
	{
		State *s = state(Split, 0, $1.start, NULL);
		patch($1.out, s);
		$$ = frag(s, list1(&s->out1));
	}
|	single '*' '?'
	{
		State *s = state(Split, 0, NULL, $1.start);
		patch($1.out, s);
		$$ = frag(s, list1(&s->out));
	}
|	single '+'
	{
		State *s = state(Split, 0, $1.start, NULL);
		patch($1.out, s);
		$$ = frag($1.start, list1(&s->out1));
	}
|	single '+' '?'
	{
		State *s = state(Split, 0, NULL, $1.start);
		patch($1.out, s);
		$$ = frag($1.start, list1(&s->out));
	}
|	single '?'
	{
		State *s = state(Split, 0, $1.start, NULL);
		$$ = frag(s, append($1.out, list1(&s->out1)));
	}
|	single '?' '?'
	{
		State *s = state(Split, 0, NULL, $1.start);
		$$ = frag(s, append($1.out, list1(&s->out)));
	}
;

count:	{ $$ = ++nparen; }

single:
	'(' count alt ')'
	{
		$$ = paren($3, $2);
	}
|	'(' '?' ':' alt ')'
	{
		$$ = $4;
	}
|	CHAR
	{
		State *s = state(Char, $1, NULL, NULL);
		$$ = frag(s, list1(&s->out));
	}
|	'.'
	{
		State *s = state(Any, 0, NULL, NULL);
		$$ = frag(s, list1(&s->out));
	}
;

%%

char *input;
char *text;
void dumplist(List*);

int
yylex(void)
{
	int c;

	if(input == NULL || *input == 0)
		return EOL;
	c = *input++;
	if(strchr("|*+?():.", c))
		return c;
	yylval.c = c;
	return CHAR;
}

void
yyerror(char *s)
{
	fprintf(stderr, "parse error: %s\n", s);
	exit(1);
}

void
printmatch(Sub *m, int n)
{
	int i;
	
	for(i=0; i<n; i++){
		if(m[i].sp && m[i].ep)
			printf("(%d,%d)", m[i].sp - text, m[i].ep - text);
		else if(m[i].sp)
			printf("(%d,?)", m[i].sp - text);
		else
			printf("(?,?)");
	}
}

void
dumplist(List *l)
{
	int i;
	Thread *t;

	for(i=0; i<l->n; i++){
		t = &l->t[i];
		if(t->state->op != Char && t->state->op != Any && t->state->op != Match)
			continue;
		printf("  ");
		printf("%d ", t->state->id);
		printmatch(t->match, nparen+1);
		printf("\n");
	}
}

/*
 * Is match a longer than match b?
 * If so, return 1; if not, 0.
 */
int
longer(Sub *a, Sub *b)
{
	if(a[0].sp == NULL)
		return 0;
	if(b[0].sp == NULL || a[0].sp < b[0].sp)
		return 1;
	if(a[0].sp == b[0].sp && a[0].ep > b[0].ep)
		return 1;
	return 0;
}

/*
 * Add s to l, following unlabeled arrows.
 * Next character to read is p.
 */
void
addstate(List *l, State *s, Sub *m, char *p)
{
	Sub save;

	if(s == NULL)
		return;

	if(s->lastlist == listid){
		switch(matchtype){
		case LeftmostBiased:
			if(reptype == RepeatMinimal || ++s->visits > 2)
				return;
			break;
		case LeftmostLongest:
			if(!longer(m, s->lastthread->match))
				return;
			break;
		}
	}else{
		s->lastlist = listid;
		s->lastthread = &l->t[l->n++];
		s->visits = 1;
	}
	if(s->visits == 1){
		s->lastthread->state = s;
		memmove(s->lastthread->match, m, NSUB*sizeof m[0]);
	}

	switch(s->op){
	case Split:
		/* follow unlabeled arrows */
		addstate(l, s->out, m, p);
		addstate(l, s->out1, m, p);
		break;
	
	case LParen:
		/* record left paren location and keep going */
		save = m[s->data];
		m[s->data].sp = p;
		m[s->data].ep = NULL;
		addstate(l, s->out, m, p);
		/* restore old information before returning. */
		m[s->data] = save;
		break;
	
	case RParen:
		/* record right paren location and keep going */
		save = m[s->data];
		m[s->data].ep = p;
		addstate(l, s->out, m, p);
		/* restore old information before returning. */
		m[s->data] = save;
		break;
	}
}

/*
 * Step the NFA from the states in clist
 * past the character c,
 * to create next NFA state set nlist.
 * Record best match so far in match.
 */
void
step(List *clist, int c, char *p, List *nlist, Sub *match)
{
	int i;
	Thread *t;
	static Sub m[NSUB];

	if(debug){
		dumplist(clist);
		printf("%c (%d)\n", c, c);
	}

	listid++;
	nlist->n = 0;

	for(i=0; i<clist->n; i++){
		t = &clist->t[i];
		if(matchtype == LeftmostLongest){
			/*
			 * stop any threads that are worse than the 
			 * leftmost longest found so far.  the threads
			 * will end up ordered on the list by start point,
			 * so if this one is too far right, all the rest are too.
			 */
			if(match[0].sp && match[0].sp < t->match[0].sp)
				break;
		}
		switch(t->state->op){
		case Char:
			if(c == t->state->data)
				addstate(nlist, t->state->out, t->match, p);
			break;

		case Any:
			addstate(nlist, t->state->out, t->match, p);
			break;

		case Match:
			switch(matchtype){
			case LeftmostBiased:
				/* best so far ... */
				memmove(match, t->match, NSUB*sizeof match[0]);
				/* ... because we cut off the worse ones right now! */
				return;
			case LeftmostLongest:
				if(longer(t->match, match))
					memmove(match, t->match, NSUB*sizeof match[0]);
				break;
			}
			break;
		}
	}
	
	/* start a new thread if no match yet */
	if(match == NULL || match[0].sp == NULL)
		addstate(nlist, start, m, p);
}

/* Compute initial thread list */
List*
startlist(State *start, char *p, List *l)
{
	List empty = {NULL, 0};
	step(&empty, 0, p, l, NULL);
	return l;
}	

int
match(State *start, char *p, Sub *m)
{
	int c;
	List *clist, *nlist, *t;
	
	clist = startlist(start, p, &l1);
	nlist = &l2;
	memset(m, 0, NSUB*sizeof m[0]);
	for(; *p && clist->n > 0; p++){
		c = *p & 0xFF;
		step(clist, c, p+1, nlist, m);
		t = clist; clist = nlist; nlist = t;
	}
	step(clist, 0, p, nlist, m);
	return m[0].sp != NULL;
}

void
dump(State *s)
{
	if(s == NULL || s->lastlist == listid)
		return;
	s->lastlist = listid;
	printf("%d| ", s->id);
	switch(s->op){
	case Char:
		printf("'%c' -> %d\n", s->data, s->out->id);
		break;

	case Any:
		printf(". -> %d\n", s->out->id);
		break;

	case Split:
		printf("| -> %d, %d\n", s->out->id, s->out1->id);
		break;
	
	case LParen:
		printf("( %d -> %d\n", s->data, s->out->id);
		break;
	
	case RParen:
		printf(") %d -> %d\n", s->data, s->out->id);
		break;

	case Match:
		printf("match\n");
		break;

	default:
		printf("??? %d\n", s->op);
		break;
	}

	dump(s->out);
	dump(s->out1);
}

int
main(int argc, char **argv)
{
	int i;
	Sub m[NSUB];

	for(;;){
		if(argc > 1 && strcmp(argv[1], "-d") == 0){
			debug++;
			argv[1] = argv[0]; argc--; argv++;
		}
		if(argc > 1 && strcmp(argv[1], "-l") == 0){
			matchtype = LeftmostLongest;
			argv[1] = argv[0]; argc--; argv++;
		}
		else if(argc > 1 && strcmp(argv[1], "-p") == 0){
			reptype = RepeatLikePerl;
			argv[1] = argv[0]; argc--; argv++;
		}
		else
			break;
	}

	if(argc < 3){
		fprintf(stderr, "usage: %s regexp string...\n", argv[0]);
		return 1;
	}
	
	input = argv[1];
	yyparse();
	
	++listid;
	if(debug)
		dump(start);
	
	l1.t = malloc(nstate*sizeof l1.t[0]);
	l2.t = malloc(nstate*sizeof l2.t[0]);
	for(i=2; i<argc; i++){
		text = argv[i];	/* used by printmatch */
		if(match(start, argv[i], m)){
			printf("%s: ", argv[i]);
			printmatch(m, nparen+1);
			printf("\n");
		}
	}
	return 0;
}

/*
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall
 * be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
