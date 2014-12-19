package smulc

object BasicExps {
  val id = Lam("x", Var("x"))
  val mock = Lam("x", App(Var("x"), Var("x")))
  val tru = Lam("x", Lam("y", Var("x")))
  val fals = Lam("x", Lam("y", Var("y")))
  def nat(i: Int) = {
    var count = i
    var inside: Exp = Var("y")
    while(count > 0) {
      inside = App(Var("x"), inside)
      count -= 1
    }
    Lam("x", Lam("y", inside))
  }

  val abbreviations: Map[String, Exp] = Map(
    "true" -> tru,
    "false" -> fals,
    "id" -> id
  )
}

