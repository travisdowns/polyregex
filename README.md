A really slow regex matcher, which supports backrefences in [polynomial time](https://en.wikipedia.org/wiki/P_(complexity)) _in the 
size of the input text_ for a fixed number of backreferences in the pattern. That is, varying only the input size, not the pattern<sup>1</sup> the 
running time will be in poly time. In fact, since like many engines, since only 9 backreferences (`\1` through `\9`) are supported, we
can say that this engine is _always_ runs in P due to the cap on the number of backreferences.  

The basic idea is to duplicate the underlying backreference-unaware NFA for all combinations of start/stop points in the 
input for every captured group. If there are c captured groups, that's O(n^2c) which is a polynomial number of NFAs. Since the
underlying NFA simulation process is also polynomail. Ultimately we get a bound something like O(n^20) time and O(n^19) space to
support 9 backreferences.

This is intended entirely as a proof of concept to make it easily to verify by implementation the original proof
sketch - it is not a practical implementation at all. It is very slow and uses many GB of memory to parse even relatively
simple expressions with a few captured groups. If you want to implement a poly time (under  the definition above) regex engine
that handles backreferences, don't do it like this. Do it with backtracking with memoization or perhaps psuedo-NFA simulation with
an expanded and dynamic state space that includes capture information in the state.

Russ Cox's [regex resources](https://swtch.com/~rsc/regexp/) were invaluable in understanding how to handle NFA similuation and other concerns.  
 

---

<sup>1</sup> In particular, not varying the number of captured groups later referred to by a backreference. 
