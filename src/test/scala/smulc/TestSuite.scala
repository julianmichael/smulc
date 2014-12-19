package smulc

import org.scalatest.FunSuite

class TestSuite extends FunSuite {

  // next: TODO a REPL
  test("running programs") {
    Program.readFromFile("examples/arith.smulc").run()
    Program.readFromFile("examples/action.smulc").run()
  }

}