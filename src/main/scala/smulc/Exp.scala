package smulc

sealed trait Exp {
  import Exp._
  override def toString: String = this match {
    case Var(x) => x
    case Lam(p, t) => s"(\\$p. $t)"
    case App(t1, t2) => s"($t1 $t2)"
    case Act(t1, t2) => s"($t1 -> $t2)"
  }
}
object Exp {
  case class Var(name: String) extends Exp
  case class Lam(param: String, t: Exp) extends Exp
  case class App(t1: Exp, t2: Exp) extends Exp
  case class Act(t1: Exp, t2: Exp) extends Exp

  import BasicExps._

  def isValue(t: Exp): Boolean = t match {
    case App(Var(_), _) => true
    case App(_, _) => false
    case Act(x, y) => isValue(x) && isValue(y)
    case _ => true
  }

  def girth(t: Exp): Int = t match {
    case Var(_) => 1
    case Lam(_, t1) => 1 + girth(t1)
    case App(t1, t2) => 1 + girth(t1) + girth(t2)
    case Act(t1, t2) => 1 + girth(t1) + girth(t2)
  }

  def size(t: Exp): Int = t match {
    case Var(_) => 1
    case Lam(_, t1) => 1 + size(t1)
    case App(t1, t2) => 1 + math.max(girth(t1), girth(t2))
    case Act(t1, t2) => 1 + math.max(girth(t1), girth(t2))
  }

  def intsFrom(i: Int): Stream[Int] = i #:: intsFrom(i+1)
  val nats = intsFrom(0).map(_.toString)
  def freshVar(prohib: Set[String]) = nats.filterNot(prohib).head

  def freeVars(t: Exp): Set[String] = t match {
    case Var(name) => Set(name)
    case Lam(p, t) => freeVars(t) - p
    case App(t1, t2) => freeVars(t1) ++ freeVars(t2)
    case Act(t1, t2) => freeVars(t1) ++ freeVars(t2)
  }

  def isClosed(t: Exp): Boolean = freeVars(t).isEmpty

  def alpha(prohib: Set[String], lam: Lam): Lam = lam match {
    case Lam(param, t) if(prohib(param)) => {
      val newVar = freshVar(prohib)
      Lam(newVar, substitute(t, param, Var(newVar)))
    }
    case x => x
  }

  import scalaz._
  import Scalaz._
  type SubstituteState[A] = State[Exp, A]
  def actionSubstituter(t1: Exp, x: String, actor: Exp): SubstituteState[Exp] = t1 match {
    case Var(y) if y == x => for {
      curSub <- get[Exp]
      _ <- put[Exp](App(actor, curSub))
    } yield curSub
    case v@Var(_) => state(v)
    case l@Lam(_, _) => for {
      curSub <- get[Exp]
      Lam(newP, newT) = alpha(freeVars(actor) ++ freeVars(curSub) ++ freeVars(l), l)
      subbedTerm <- actionSubstituter(newT, x, actor)
    } yield Lam(newP, subbedTerm)
    case App(tt1, tt2) => for {
      newT1 <- actionSubstituter(tt1, x, actor)
      newT2 <- actionSubstituter(tt2, x, actor)
    } yield App(newT1, newT2)
    case Act(tt1, tt2) => for {
      newT1 <- actionSubstituter(tt1, x, actor)
      newT2 <- actionSubstituter(tt2, x, actor)
    } yield Act(newT1, newT2)
  }
  def primitiveSubstitute(t1: Exp, x: String, t2: Exp): Exp = t1 match {
    case Var(y) if y == x => t2
    case v@Var(_) => v
    case l@Lam(_, _) => {
      val Lam(newP, newT) = alpha(freeVars(t2) ++ freeVars(l), l)
      val subbedTerm = primitiveSubstitute(newT, x, t2)
      Lam(newP, subbedTerm)
    }
    case App(tt1, tt2) => {
      val newT1 = primitiveSubstitute(tt1, x, t2)
      val newT2 = primitiveSubstitute(tt2, x, t2)
      App(newT1, newT2)
    }
    case Act(tt1, tt2) => {
      val newT1 = primitiveSubstitute(tt1, x, t2)
      val newT2 = primitiveSubstitute(tt2, x, t2)
      Act(newT1, newT2)
    }
  }
  def substitute(t1: Exp, x: String, subbed: Exp): Exp = subbed match {
    case Act(actor, actee) => actionSubstituter(t1, x, actor).eval(actee)
    case t => primitiveSubstitute(t1, x, t)
  }

  def step(t: Exp): Exp = t match {
    case v if isValue(v) => v
    case App(t1, t2) if !isValue(t1) => App(step(t1), t2)
    case App(v1, t2) if !isValue(t2) => App(v1, step(t2))
    case App(Lam(p, t), v2) => substitute(t, p, v2)
    case App(Act(vv1, vv2), v2) => Act(vv1, App(vv2, v2))
    case Act(f, x) if !isValue(f) => Act(step(f), x)
    case Act(f, x) if !isValue(x) => Act(f, step(x))
    // otherwise we're stuck (free variable) or looping
  }

  def smallStepEval(t: Exp): Exp = {
    var term = t
    var nextTerm = step(t)
    while(term != nextTerm) {
      term = nextTerm
      nextTerm = step(nextTerm)
    }
    term
  }

  def bigStepEval(t: Exp): Exp = t match {
    case v if isValue(v) => v
    case App(t1, t2) => {
      val v1 = bigStepEval(t1)
      val v2 = bigStepEval(t2)
      (v1: @unchecked) match {
        case Lam(p, t) => bigStepEval(substitute(t, p, v2))
        case Act(vv1, vv2) => Act(vv1, bigStepEval(App(vv2, v2)))
        // otherwise we're stuck (free variable) or looping
      }
    }
    case Act(f, t1) => Act(bigStepEval(f), bigStepEval(t1))
  }

  def superEval(t: Exp): Exp = t match {
    case v@Var(_) => v
    case l@Lam(p, t1) => Lam(p, superEval(t1))
    case App(t1, t2) => {
      val v1 = superEval(t1)
      val v2 = superEval(t2)
      (v1: @unchecked) match {
        case Lam(p, tt) => superEval(substitute(tt, p, v2))
        case Act(tt1, tt2) => Act(superEval(tt1), superEval(App(tt2, v2)))
        case _ => App(v1, v2)
      }
    }
    case Act(t1, t2) => Act(superEval(t1), superEval(t2))
  }

}