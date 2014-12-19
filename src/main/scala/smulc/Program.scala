package smulc

import Exp._
import ExpParsing._
import molt.syntax.cfg._
import molt.syntax.cfg.parsable._
import molt.syntax.cfg.parsable.ParseCommands._
import molt.syntax.cfg.parsable.CFGParserHelpers._

sealed trait Statement
case class Let(name: String, term: Exp) extends Statement
case class Anon(term: Exp) extends Statement
object Statement {
  implicit object StatementParser extends ComplexCFGParsable[Statement] {
    final override val synchronousProductions: Map[List[CFGParsable[_]], (List[AST[CFGParsable[_]]] => Option[Statement])] = Map(
      List(Terminal("let"), VarCategory, Terminal("be"), ExpParser) -> (c => for {
        v <- VarCategory.fromAST(c(1))
        t <- ExpParser.fromAST(c(3))
      } yield Let(v, t)),
      List(ExpParser) -> (c => for {
        t <- ExpParser.fromAST(c(0))
      } yield Anon(t))
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

case class Program(statements: List[Statement]) {
  def evalExp(term: Exp): Exp = {
    val newTerm = statements.foldLeft(term)((t, stmt) => stmt match {
      case Let(name, value) => App(Lam(name, t), value)
      case Anon(_) => t
    })
    val newValue = Exp.smallStepEval(newTerm)
    newValue
  }
  def addStatement(stmt: Statement, print: Boolean = true): Program = stmt match {
    case Let(name, term) => {
      val value = evalExp(term)
      if(print) println(s"$name = $value")
      Program(Let(name, value) :: statements)
    }
    case Anon(term) => {
      val value = evalExp(term)
      if(print) println(s"$value")
      Program(Anon(value) :: statements)
    }
  }

  def run(print: Boolean = true): Program = {
    val init = Program(List.empty[Statement])
    statements.foldLeft(init)((prog, stmt) =>
      prog.addStatement(stmt, print)
    )
  }
}
object Program {
  def readFromFile(path: String): Program = {
    import Statement._
    val lines = io.Source.fromFile(path).getLines.filterNot(_.trim.isEmpty).map(parseUnique[Statement])
    val stmts = lines.foldRight(List.empty[Statement]) {
      case (Some(s), l) => s :: l
      case (None, l) => throw new RuntimeException(s"syntax error near line ${l.length}")
    }
    Program(stmts)
  }
  def repl: Program = {
    import Statement._
    var prog = Program(Nil)
    for (line <- io.Source.stdin.getLines) {
      val next = parseUnique[Statement](line)
      next match {
        case None => println("Syntax error. Try again.")
        case Some(s) => prog = prog.addStatement(s)
      }
    }
    prog
  }
}
