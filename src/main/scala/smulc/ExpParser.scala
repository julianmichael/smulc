package smulc

import molt.syntax.cfg.parsable._
import molt.syntax.cfg._
import CFGParserHelpers._

object ExpParsers {

  object VarCategory extends RegexLexicalCategory("[a-zA-Z][a-zA-Z0-9]*")

  object LamParser extends ComplexCFGParsable[Lam] {
    final override val synchronousProductions: Map[List[CFGParsable[_]], (List[AST[CFGParsable[_]]] => Option[Lam])] = Map(
      List(Terminal("\\"), VarCategory, Terminal("."), SimpleExpParser) -> (c => for {
        p <- VarCategory.fromAST(c(1))
        t <- SimpleExpParser.fromAST(c(3))
      } yield Lam(p, t))
    )
  }

  // TODO when Plus is changed to NonEmpty list fix this up
  object AppParser extends ComplexCFGParsable[App] {
    def makeExpByApps(ts: List[Exp]): Exp = (ts: @unchecked) match {
      case head :: tail => tail.foldLeft(head)(App(_, _))
    }
    final override val synchronousProductions: Map[List[CFGParsable[_]], (List[AST[CFGParsable[_]]] => Option[App])] = Map(
      List(Plus(SimpleExpParser), SimpleExpParser) -> (c => for {
        ts <- Plus(SimpleExpParser).fromAST(c(0))
        t <- SimpleExpParser.fromAST(c(1))
      } yield App(makeExpByApps(ts), t))
    )
  }

  object ActParser extends ComplexCFGParsable[Act] {
    def makeActByActs(ts: List[Exp]): Act = (ts: @unchecked) match {
      case first :: second :: tail => tail.foldLeft(Act(first, second))(Act(_, _))
    }
    final override val synchronousProductions: Map[List[CFGParsable[_]], (List[AST[CFGParsable[_]]] => Option[Act])] = Map(
      List(MultiDelimitedList("->", SimpleExpParser)) -> (c => for {
        ts <- MultiDelimitedList("->", SimpleExpParser).fromAST(c(0))
      } yield makeActByActs(ts))
    )
  }

  object SimpleExpParser extends ComplexCFGParsable[Exp] {
    final override val synchronousProductions: Map[List[CFGParsable[_]], (List[AST[CFGParsable[_]]] => Option[Exp])] = Map(
      List(VarCategory) -> (c => for {
        v <- VarCategory.fromAST(c(0))
      } yield Var(v)),
      List(LamParser) -> (c => for {
        l <- LamParser.fromAST(c(0))
      } yield l),
      List(Parenthesize(ExpParser)) -> (c => for {
        t <- Parenthesize(ExpParser).fromAST(c(0))
      } yield t),
      List(Numeric) -> (c => for {
        i <- Numeric.fromAST(c(0))
      } yield BasicExps.nat(i.toInt)),
      List(Terminal("true")) -> (c => Some(BasicExps.tru)),
      List(Terminal("false")) -> (c => Some(BasicExps.fals))
    )
  }

  implicit object ExpParser extends ComplexCFGParsable[Exp] {
    final override val synchronousProductions: Map[List[CFGParsable[_]], (List[AST[CFGParsable[_]]] => Option[Exp])] = Map(
      List(SimpleExpParser) -> (c => for {
        t <- SimpleExpParser.fromAST(c(0))
      } yield t),
      List(AppParser) -> (c => for {
        a <- AppParser.fromAST(c(0))
      } yield a),
      List(ActParser) -> (c => for {
        a <- ActParser.fromAST(c(0))
      } yield a)
    )
  }

}
