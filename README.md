This is really slow regex matcher, and an attempt to answer [this question](https://branchfree.org/2019/04/04/question-is-matching-fixed-regexes-with-back-references-in-p/), which supports backrefences in [deterministic polynomial time](https://en.wikipedia.org/wiki/P_(complexity)) _in the 
size of the input text_ for a fixed number of backreferences in the pattern. That is, varying only the input size, not the pattern<sup>1</sup> the 
running time will be in P. In fact, since (like many other engines) only 9 backreferences (`\1` through `\9`) are supported, we
can say that this engine is _always_ runs in P due to the cap on the number of backreferences (at the moment not technically true depending on how diligent you are at using non-capturing groups due to [issue #1](https://github.com/travisdowns/polyregex/issues/1)).

The basic idea is to duplicate the underlying backreference-unaware NFA (hereafter the "base NFA") for all combinations of start/stop points in the 
input for every captured group. If there are `k` captured groups, that's `O(n^2k)` copies of the base NFA which is polynomial in `n` for fixed `k`. Since the
underlying NFA simulation process is also polynomial in `n`, total time is polynomial.

Filling in a few more details (below) we ultimately we get a bound of `O(n^(2k+2))` time and `O(n^(2k+1))` space. So for the full complement of 9 backreferences, that's `O(n^20)` time and `O(n^19)` space - not fast either theoretically or actually. A derivation of the order based on the code is found below. 

## Don't Actually Use This

This is intended entirely as a proof of concept to make it easy to verify by implementation the [original proof
sketch](https://twitter.com/trav_downs/status/1114977163093139456) - it is not a practical implementation at all. It is very slow and especially the "eager" moder uses many GB of memory to parse even relatively
simple expressions with a few captured groups. If you want to implement a poly time (under the definition above) regex engine
that handles backreferences, don't do it like this. Do it with backtracking with memoization or perhaps psuedo-NFA simulation with
an expanded and dynamic state space that includes capture information in the state (these ideas are further elaborated [below](#more-practical-solutions)).

## Complexity Proof Sketch

Here's proof sketch for this particular Java implementation operating in polynomial time (in the sense given above).

The basic idea we step over each character and do a polynomial amount of work at each step, since the amount of work is bounded by the number of active states, itself bounded by the total number of all states (which is polynomial in size). The details follow.

We consider one call to `new BackrefMatcher(pattern, true).matches(input)`. The length of the input is `n` and the length<sup>2</sup> of the pattern is `m`. The number of captured groups is `k` (in the code `k` is `BackrefMatcher.groupCount`). The number of backreference instances (occurences of a backreference in the pattern) is `b`. For example, the pattern `(.)\1\1\1` has one captured group (`k=1`) and three backreference instances (`b=3`).

For this sketch we use a `BackrefMatcher` with `isEager == true`, which generates the every possible (and many impossible) subNFA graph up front. This is terribly slow but makes it slightly easier to analyze. You can activate this mode at runtime by passing `-DBackrefMatcher.eager=true` to the JVM. The default mode is lazy which generates subNFA graphs only as needed, which is usually several orders of magnitude faster. 

First we examine the `BackrefMatcher` constructor: it doesn't deal with the input text at all, but just parses and compiles the pattern. This involves O(m) work, but we won't go into details because this is all basic NFA stuff and the O(m) term is going to disappear in the final results anyways.

The bulk of the actual matching work then happens in `new BackrefRunner(input)` and then the call to `BackrefRunner.matches()`. One of these runner objects is created for each match operation.

The `BackrefRunner(input)` constructor calls `buildSubNFAs`. This function creates one subNFA graph for every possible (and many impossible) `CaptureState` (hereafter "capstate"). A capstate object records the start and stop position for each of the `k` matches (including the special position `-1` indicating "group not captured"). That's `(n+1)^2` possible positions for each group, or `(n+1)^2k`, for all `k` groups. To see that reflected in the code, each invocation of `buildSubNFA` has two nested loops running from -1 to n, for `n^2` total iterations, ending in a recursive call to `buildSubNFA`. This recursion ends after k steps, giving `O(n^2k)` total subNFAs, matching the bound calculated above.

Now, when the recursion ends, we have the following work for each subNFA:


```
CaptureState cstate = new CaptureState(starts.clone(), ends.clone(), text.length());
capToSub.put(cstate, new SubNFA(start, cstate));
```

Each capstate object has O(k) data (the `starts` and `ends` array both have length `k`) and takes O(k) time to create. The `SubNFA(State start, CaptureState capstate)` constructor is more complicated. The key elements are: calls to `State.cloneGraph` and `State.expandBackrefs` on the cloned graph, in addition to other work which has the same or lesser order than those calls. This costs ends up dominated by the `SubNFA` construction work we look at next (assuming `k` <= `b*n`).

The `cloneGraph` call creates a clone of the base NFA (with O(m) objects), except where the `State` objects are upgrade to `StateEx` which are created in O(1) time.

The `State.expandBackrefs` call operates on this O(m)-sized graph, and expands each of the `b` backreference instances into distinct nodes, one per character in the captured group as represented in the capstate. Each are at most `n` characters, so the cost of this call is O(m + b\*n). The total number of nodes in each SubNFA graph is also `O(m + b*n)` (this will be important later).

Summarizing, `SubNFA` constructor does `O(m + b\*n)` work (same for space) and there are `O(n^2k)` SubNFAs, so we have `O(m*n^2k + b*n^(2k+1))` work and space. We generally expect the second term to dominate as it is higher order in `n`. The total number of nodes (possible states) in the entire graph has the same order. We will call this `O(m*n^2k + b*n^(2k+1))` term `t` for brevity in the following analysis.  

Next, we examine the operation of `BackrefRunner.matches()`. We omit the analysis of the `startlist()` call, since it is equivalent in cost to one call to `step()` which we examine next.

The `step()` call occurs in a loop, called once for each of the `n` characters in the input. Each `step()` call iterates over the current state list (`clist` in the code), whose size is bounded by `t`. Each iteration may call `addstate()` which adds new reachable states to `nlist`, the state list for the next step. Considering as a whole all the `addstate()` calls<sup>3</sup>, they do at most `O(t)` work since the `O(1)` body of the method executes once every time a state is added to the `StateList.visited` set, and this set can have at most `O(t)` elements (the total number of states). 

Now then we have the total work: `n` calls to `step()` each of which take `O(t)` work, for `O(n*t)` total work. Total space is `O(t)`, i.e., the total number of states. Expanding out `t`, we get  `O(m*n^(2k+1) + b*n^(2k+2)` work and `O(m*n^2k + b*n^(2k+1)` space. Under the assumption that `b`, `k` are fixed constants and that the size of the input `n` is not "much smaller" than the size of the regex `m`<sup>4</sup>, only the last term remains and it simplifies to `O(n^(2k+2))` time and `O(n^(2k+1))` space.

## More Practical Solutions

As mentioned several times above, the current implementaiton is not meant to be a practical one. A practical solution might look something like one of these two ideas:

### NFA With Additional State

This is the closest to the existing solution. The solution in this repository still encodes all the information for each state as a single `StateEx` pointer, which means that you need to massively duplicate states (all the `SubNFA` objects), which is a big waste of time and memory (indeed, many of the created states may never be reachable). This lets the `step()` state transition function basically work like a traditional NFA simulation, which is the most familiar and makes the proof easy.

Another approach, however, would be not to duplicate any NFA nodes, but to augment each state object with the necessarily extra information to carry along capture related information. The transitions would use the base NFA, but there could be multiple states pointing to the same NFA node, which differ in their capture information and each one would be transitioned separately. Matching backreference instances could be accomodated not by "expanding" the NFA nodes as in the current solution, but by tracking state that indicates, when the state is matching a backreference, how many of the backreference characters have been matched so far. Each `step()` would try to transition the state by matching the next character, or finally leaving the backreference match node if all characters have been matched.

This approach has other advantages such as the ability to track other state that might be useful, e.g., a "count" state for matching counted repetition like `a{1,5}` - which is hard to implement efficiently in an NFA (the naive solution requires to duplicate the states for the repeated pattern 5 times in this example). 

I think this would achieve the same or better bound as the current solution, more efficiently. 

### Backtracking with memoizing

In principle, I think a backtracking implementation, which is generally simpler all-around, could also achieve running time in P if memorized all the states it had seen, since repeated visiting of identical states is ultimately behind exponential blowup. Some care would have to be given not to use excessive space, however, since a naive implementation would save more states than an NFA simulation based approach since it doesn't visit every character one-by-one, so states earlier in the string may be saved that can never be accessed.

Russ Cox metnions that Perl 5 regular expressions are supposed to use memoization, but he (and I) still find exponential blowup on simple regexes like `(a|b)?(a|b)?...(a|b)(a|b)...`. 

## Thanks

Russ Cox's [regex resources](https://swtch.com/~rsc/regexp/) were invaluable in understanding how to handle NFA similuation and other concerns.

---

<sup>1</sup> In particular, not varying the number of captured groups later referred to by a backreference. 

<sup>2</sup> Technically, we measure the length of the pattern in terms of nodes in the underlying _base NFA_, rather than characters in the input, although there is almost a 1:1 correspondence between characters and nodes. Certainly the number of nodes is at most linear in the pattern character length. Some exceptions from the 1:1 rule are non-capturing parenthesis, which do not create any node at all, and backreferences like `\1` which are two characters but create a single node in the base NFA. If we implement a larger subset of regular expression syntax, more exceptions would arise.

<sup>3</sup> It is important to consider them as a whole, since if you consider one `addstate` call at a time, you'll also get `O(t)` work, for a total of `O(n*t)` work for the `step()` call. The calls are not independent though because the `visited` set bound the total work across all `addstate` calls within any `step()`.

<sup>4</sup> In particular, it is enough for the size of the regex to be no more than quadratic in the size of the input, i.e, that m is `O(n^2)`.
