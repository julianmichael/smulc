SMULC
=====

SMULC, not to be confused with Sailor Moon Urban Legends Central, is the Self-Modifying Untyped
λ-Calculus. It is an esoteric, pointless extension of the untyped λ-calculus. An executable
interpreter may be downloaded from [here](http://cs.utexas.edu/~julianjm/assets/smulc "smulc").

You might be thinking: "Self-modifying? The λ-calculus is functional; it doesn't make sense for
*anything* to be modified, let alone the code." Yeah, I guess you're right in a way. So here's how I
thought of it: what does it mean if code is self-modifying? It means that *each successive time you
use a piece of code, it might be different.* In the λ-calculus, "the same" piece of code may be used
multiple times by the process of substitution in β-reduction: i.e., I say the input ```x``` is used
twice in the function ```λx. x x```. So to allow code to be "self-modifying" is to allow it to say
"hey, each successive time you use me in a function, I will change in ```X``` way."

Thus my new syntactic construct was born: ```f -> x``` may be read "```f``` acting on ```x```".
An example reduction is as follows:

      (λx . x x x) (f -> y) ⤳ y (f y) (f (f y))

The "action" ```f``` specifies how ```y``` changes with each use.

Otherwise, the core language is exactly the CBV untyped λ-calculus, with a few abbreviations (for
booleans and natural numbers) for convenience.

Usage
-----

For ease of writing programs, the backslash ```\``` is used instead of λ, and a program consists of
a series of either bare lambdas or let-be expressions, each on its own line. A let-be expression
binds a term to a name, which can be used later in the program. But be wary...it might change! (see
the example in
[```examples/demo.smulc```](https://github.com/julianmichael/smulc/blob/master/examples/demo.smulc
"demo.smulc").)

To run the interpreter, called ```smulc```, just execute ```smulc``` for a REPL (which remembers
your let-bindings during your session), or execute ```smulc file1.smulc file2.smulc ...``` to
execute the contents of ```file1.smulc``` and ```file2.smulc```, etc.

To build the interpreter, you need to have ```sbt```. If you do, just clone this repo and run
```deploy.sh``` from the root directory. The interpreter will be at ```target/smulc```.


Formalities
-----------

Here is a formal syntax and semantics of the core component of SMULC. If you find any discrepancies
between the interpreter's behavior and these formalities, please raise an issue. I might fix it. Or
fix it yourself and submit a pull request.

Syntax:

      var   ::= [a-zA-Z][a-zA-Z0-9]*
      term  ::= var
              | \ var . term
              | term term
              | term -> term
      value ::= var
              | \ var . term
              | var term
              | value -> value

Substitution is defined as normal (with α-substitution and all) and additionally the "action"
effects I mentioned above. I'm too lazy to think of the best way to write it formally. (...So much
for "formalities"...)

Semantics (big-step):

      v ⇓ v

      t1 ⇓ \p. t1'   t2 ⇓ v2   [p ↦ v2]t1' ⇓ v3
      -----------------------------------------
                     t1 t2 ⇓ v3

       t1 ⇓ v1   t2 ⇓ v2
      -------------------
      t1 -> t2 ⇓ v1 -> v2

      t1 ⇓ v1 -> v2   t2 ⇓ v3
      -----------------------
       t1 t2 ⇓ v1 -> (v2 v3)

(Here any ```t``` is a term and any ```v``` is a value.)

Small-step semantics vaguely exist in my head (and in the code). Look in
[Exp.scala](https://github.com/julianmichael/smulc/blob/master/src/main/scala/smulc/Exp.scala
"Exp.scala") if you're interested.
