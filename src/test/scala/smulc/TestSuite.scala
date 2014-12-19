package smulc

import org.scalatest.FunSuite

class TestSuite extends FunSuite {

  import ExpParsing._
  import molt.syntax.cfg.parsable.ParseCommands._
  import Exp._
  import Statement._

  /*
  test("printing stuff") {
    println(parseFirst[Exp]("\\x. x x").toList)
    println(parseFirst[Exp]("\\x. x x x").toList)
    println(parseFirst[Exp]("y \\x. x x x").toList)
    println(parseFirst[Exp]("z y \\x. x x x").toList)
    println(parseFirst[Exp]("z -> y x").toList)
    println(parseFirst[Exp]("z y -> x").toList)
    println(parseFirst[Exp]("\\n. \\m. \\f. \\x. m f (n f x)").toList)
    println(parseFirst[Statement]("let plus be \\n. \\m. \\f. \\x. m f (n f x)").toList)
    parseFirst[Statement]("let times be \\n. \\m. \\f. m (plus n) 0").toList.map(println)
    parseFirst[Exp]("\\n. \\m. \\f. m (plus n) 0").toList.map(println)
  }
  */

  test("running program") {
    Program.readFromFile("examples/arith.smulc").run()
  }

}