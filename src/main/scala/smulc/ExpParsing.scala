package smulc

import molt.syntax.cfg.parsable._
import molt.syntax.cfg._
import CFGParserHelpers._


object ExpParsing {
  import Exp._
  object VarCategory extends RegexLexicalCategory("[a-zA-Z][a-zA-Z0-9]*")

  val AbbrevCategory = SetCategory(BasicExps.abbreviations.keySet)

  object ExpAbbreviationParser extends ComplexCFGParsable[Exp] {
    final override val synchronousProductions: Map[List[CFGParsable[_]], (List[AST[CFGParsable[_]]] => Option[Exp])] = Map(
      List(AbbrevCategory) -> (c => for {
        s <- AbbrevCategory.fromAST(c(0))
      } yield BasicExps.abbreviations(s)),
      List(Numeric) -> (c => for {
        n <- Numeric.fromAST(c(0))
      } yield BasicExps.nat(n.toInt))
    )
  }

  implicit object ExpParser extends ComplexCFGParsable[Exp] {
    final override val synchronousProductions: Map[List[CFGParsable[_]], (List[AST[CFGParsable[_]]] => Option[Exp])] = Map(
      List(VarCategory) -> (c => for {
        v <- VarCategory.fromAST(c(0))
      } yield Var(v)),
      List(Terminal("\\"), VarCategory, Terminal("."), ExpParser) -> (c => for {
        p <- VarCategory.fromAST(c(1))
        t <- ExpParser.fromAST(c(3))
      } yield Lam(p, t)),
      List(ExpParser, ExpParser) -> (c => for {
        t1 <- ExpParser.fromAST(c(0))
        t2 <- ExpParser.fromAST(c(1))
      } yield App(t1, t2)),
      List(ExpParser, Terminal("->"), ExpParser) -> (c => for {
          t1 <- ExpParser.fromAST(c(0))
          t2 <- ExpParser.fromAST(c(2))
        } yield Act(t1, t2)),
      List(ExpAbbreviationParser) -> (c => for {
          t <- ExpAbbreviationParser.fromAST(c(0))
        } yield t),
      List(Parenthesize(ExpParser)) -> (c => for {
          t <- Parenthesize(ExpParser).fromAST(c(0))
        } yield t)
    )

    import molt.syntax.cfg._
    import molt.syntax.cnf._
    import Exp.girth
    final override val schedulingParams: SmartParseParameters[CNFAST[CNFConversionTag[CFGParsable[_]]]] = new PenaltyBasedCFGSmartParse[CFGParsable[_]] {
      val penalties = List(
        ( (tree: AST[CFGParsable[_]]) => ExpParser.fromAST(tree) match {
            case None => 0.0
            case Some(t) => t match {
              // Application associates left
              case App(l, _) => girth(l).toDouble
              // Action associates left
              case Act(l, _) => girth(l).toDouble
              // Lambdas extend as far as possible
              case l@Lam(_, _) => 1.0 / girth(l)
              case _ => 0
            }
          }
        )
      )
    }
  }

}