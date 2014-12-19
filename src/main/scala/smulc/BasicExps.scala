package smulc

object BasicExps {
  import molt.syntax.cfg.parsable.ParseCommands._
  import ExpParsers._
  val id = parseForced[Exp]("(\\x. x)")
  val mock = parseForced[Exp]("(\\x. (x x))")
  val tru = parseForced[Exp]("(\\x. \\y. x)")
  val fals = parseForced[Exp]("(\\x. \\y. y)")
  def nat(i: Int) = {
    var count = i
    var inside: Exp = Var("y")
    while(count > 0) {
      inside = App(Var("x"), inside)
      count -= 1
    }
    Lam("x", Lam("y", inside))
  }
}

