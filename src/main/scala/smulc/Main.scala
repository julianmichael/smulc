package smulc

object Main extends App {
  if(args.length == 0) Program.repl
  else args foreach (Program.readFromFile(_).run())
}